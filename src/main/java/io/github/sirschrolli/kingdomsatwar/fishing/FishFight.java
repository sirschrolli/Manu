package io.github.sirschrolli.kingdomsatwar.fishing;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

/**
 * Der Drill (seit 0.79): Beißt ein Fisch und der Spieler zieht an, wird er nicht einfach herausgehoben – er muss gehalten
 * werden. In einer Wassersäule schwimmt der Fisch auf und ab, jede Art auf ihre Weise ({@link FishSpecies.Behavior}); der
 * Spieler hebt die <b>Fangzone</b>, solange er die Maustaste hält, und lässt sie sinken, wenn er loslässt – jeder Klick gibt
 * ihr dazu einen kleinen Schubs nach oben ({@link #kick()}, seit 0.83), so lässt sie sich auch antippen. Liegt der Fisch in der
 * Zone, füllt sich die Leiste – sonst leert sie sich. Voll: gefangen. Leer: entkommen (der Haken bleibt im Wasser). Liegt
 * die Zone ungehalten am Boden, hängt die Schnur durch und nichts füllt sich: Wer nichts tut, fängt nichts.
 *
 * <p>Wer den Fisch keinen Augenblick aus der Zone lässt, fängt <b>perfekt</b> – das Tier ist dann größer. Manchmal treibt eine
 * <b>Truhe</b> in der Säule: Wer die Zone lange genug über ihr hält, hebt mit dem Fisch einen Schatz.
 *
 * <p>Die Rechnung läuft beim Spieler (jedes Bild, in festen Schritten von {@value #STEP} s); der Server prüft nur, ob ein
 * gemeldeter Fang überhaupt möglich war ({@link #minTicks}).
 */
public final class FishFight {
    /** Zeitschritt der Rechnung (Sekunden). */
    public static final float STEP = 1.0f / 120.0f;
    /** So voll ist die Leiste zu Beginn. */
    public static final float START = 0.3f;
    /** Ein Drill dauert höchstens so lange (Ticks) – dann reißt die Schnur. */
    public static final int TIMEOUT = 20 * 150;
    /** So lange (Sekunden) muss die Zone über der Truhe liegen. */
    public static final float CHEST_SECONDS = 1.7f;
    private static final float LIFT = 2.5f;
    private static final float GRAVITY = 2.1f;
    private static final float MAX_SPEED = 1.5f;
    /** So viel Schwung gibt ein Klick der Zone nach oben (seit 0.83). */
    public static final float KICK = 0.45f;
    /** Zu Beginn sinkt die Leiste noch nicht (Sekunden). */
    private static final float GRACE = 0.6f;
    /** Der Fisch zählt noch als gehalten, wenn er so weit (Anteil der Säule) über den Rand der Zone ragt. */
    private static final float EDGE = 0.012f;

    /** Sekunden mit dem Fisch in der Zone, bis die leere Leiste voll wäre. */
    public static float catchSeconds(float difficulty) {
        return 3.4f + 3.2f * difficulty;
    }

    /** Sekunden mit dem Fisch außerhalb, bis die volle Leiste leer wäre. */
    public static float loseSeconds(float difficulty) {
        return 6.6f - 3.2f * difficulty;
    }

    /** Höhe der Fangzone (Anteil der Säule). */
    public static float zone(float difficulty, float bonus) {
        return Mth.clamp((0.38f - 0.19f * difficulty) * (1.0f + bonus), 0.15f, 0.5f);
    }

    /** So viele Ticks dauert ein Fang mindestens (mit Spielraum für Verzögerungen): Schneller gemeldete Fänge zählen nicht. */
    public static int minTicks(float difficulty) {
        return (int) (catchSeconds(difficulty) * 20.0f * (1.0f - START) * 0.6f);
    }

    private final FishSpecies.Behavior base;
    private final float difficulty;
    private final RandomSource random;
    private final float zoneSize;
    private final boolean hasChest;

    private float zonePos;
    private float zoneVel;
    private float fishPos;
    private float fishVel;
    private float fishTarget;
    private float retarget;
    private FishSpecies.Behavior behavior;
    private float behaviorTime;
    private boolean dashing;
    private float center;
    private float centerTime;
    private float progress = START;
    private boolean perfect = true;
    private boolean inZone = true;
    private boolean reeling;
    private float chestPos = -1.0f;
    private float chestAt;
    private float chestProgress;
    private boolean chestCollected;
    private float time;
    private int result;
    private float dart;
    private int kicks;

    public FishFight(FishSpecies.Behavior behavior, float difficulty, float zoneBonus, boolean chest, RandomSource random) {
        this.base = behavior;
        this.behavior = behavior == FishSpecies.Behavior.MIXED ? FishSpecies.Behavior.STEADY : behavior;
        this.difficulty = difficulty;
        this.random = random;
        this.zoneSize = zone(difficulty, zoneBonus);
        this.hasChest = chest;
        this.chestAt = 1.6f + random.nextFloat() * 2.6f;
        // der Fisch beginnt in der Zone (unten): Wer von Anfang an mithält, kann perfekt fangen
        this.fishPos = this.zoneSize * 0.5f;
        this.fishTarget = this.fishPos;
        this.center = 0.3f + random.nextFloat() * 0.4f;
        this.retarget = 0.45f;
        this.behaviorTime = 2.5f + random.nextFloat() * 2.0f;
    }

    /** Ein Zeitschritt ({@link #STEP}); {@code held}: Der Spieler hält die Taste. */
    public void step(boolean held) {
        if (this.result != 0) {
            return;
        }
        this.time += STEP;
        this.dart = Math.max(0.0f, this.dart - STEP * 2.5f);
        // die Fangzone: steigt beim Halten, fällt sonst, prallt am Boden weich ab
        this.zoneVel = Mth.clamp(this.zoneVel + (held ? LIFT : -GRAVITY) * STEP, -MAX_SPEED, MAX_SPEED);
        this.zonePos += this.zoneVel * STEP;
        float top = 1.0f - this.zoneSize;
        if (this.zonePos < 0.0f) {
            this.zonePos = 0.0f;
            this.zoneVel = this.zoneVel < -0.3f ? -this.zoneVel * 0.42f : 0.0f;
        } else if (this.zonePos > top) {
            this.zonePos = top;
            this.zoneVel = 0.0f;
        }
        this.moveFish();
        this.inZone = this.fishPos >= this.zonePos - EDGE && this.fishPos <= this.zonePos + this.zoneSize + EDGE;
        // liegt die Zone ungehalten am Boden, hängt die Schnur durch: Dann zieht nichts – so fängt niemand, der nichts tut
        this.reeling = this.inZone && (held || this.zonePos > 0.004f || this.zoneVel > 0.0f);
        if (this.inZone) {
            if (this.reeling) {
                this.progress += STEP / catchSeconds(this.difficulty);
            }
        } else {
            if (this.time > 0.35f) {
                this.perfect = false;
            }
            if (this.time > GRACE) {
                this.progress -= STEP / loseSeconds(this.difficulty);
            }
        }
        if (this.hasChest && !this.chestCollected) {
            if (this.chestPos < 0.0f && this.time >= this.chestAt) {
                // möglichst weit weg vom Fisch: Die Truhe kostet Mut
                this.chestPos = this.fishPos > 0.5f ? 0.08f + this.random.nextFloat() * 0.3f : 0.62f + this.random.nextFloat() * 0.3f;
            }
            if (this.chestPos >= 0.0f) {
                boolean covered = this.chestPos >= this.zonePos && this.chestPos <= this.zonePos + this.zoneSize;
                this.chestProgress = Mth.clamp(this.chestProgress + (covered ? STEP / CHEST_SECONDS : -STEP / (CHEST_SECONDS * 3.0f)), 0.0f, 1.0f);
                if (this.chestProgress >= 1.0f) {
                    this.chestCollected = true;
                }
            }
        }
        if (this.progress >= 1.0f) {
            this.progress = 1.0f;
            this.result = 1;
        } else if (this.progress <= 0.0f) {
            this.progress = 0.0f;
            this.result = -1;
        }
    }

    /**
     * Ein Klick: Die Zone bekommt einen kleinen Schubs nach oben – fällt sie gerade, fängt der Schubs sie ab. So lässt sie sich
     * auch durch Antippen führen; wer die Taste hält, steigt wie bisher.
     */
    public void kick() {
        if (this.result != 0) {
            return;
        }
        this.zoneVel = Math.min(MAX_SPEED, Math.max(this.zoneVel, 0.0f) + KICK);
        this.kicks++;
    }

    /** So oft wurde schon geklickt (für Klänge). */
    public int kicks() {
        return this.kicks;
    }

    private float between(float min, float max) {
        return min + this.random.nextFloat() * (max - min);
    }

    private void moveFish() {
        this.retarget -= STEP;
        this.behaviorTime -= STEP;
        float d = this.difficulty;
        // schwierige Fische überlegen kürzer
        float pace = 1.25f - 0.6f * d;
        if (this.base == FishSpecies.Behavior.MIXED && this.behaviorTime <= 0.0f) {
            FishSpecies.Behavior[] moods = {FishSpecies.Behavior.STEADY, FishSpecies.Behavior.FLOATER, FishSpecies.Behavior.SINKER,
                    FishSpecies.Behavior.JITTER, FishSpecies.Behavior.DARTER};
            this.behavior = moods[this.random.nextInt(moods.length)];
            this.behaviorTime = between(3.0f, 5.0f);
            this.retarget = 0.0f;
            this.dashing = false;
            this.center = this.fishPos;
        }
        float stiffness;
        float maxSpeed;
        switch (this.behavior) {
            case CALM -> {
                if (this.retarget <= 0.0f) {
                    float step = between(0.10f, 0.30f) * (this.random.nextBoolean() ? 1.0f : -1.0f);
                    float target = this.fishPos + step;
                    this.fishTarget = target < 0.04f || target > 0.96f ? this.fishPos - step : target;
                    this.retarget = between(1.4f, 2.8f) * pace;
                }
                stiffness = 6.0f + 6.0f * d;
                maxSpeed = 0.22f + 0.35f * d;
            }
            case FLOATER -> {
                if (this.retarget <= 0.0f) {
                    this.fishTarget = this.random.nextFloat() < 0.78f ? between(0.5f, 0.96f) : between(0.12f, 0.5f);
                    this.retarget = between(0.9f, 1.8f) * pace;
                }
                stiffness = 9.0f + 9.0f * d;
                maxSpeed = 0.35f + 0.55f * d;
            }
            case SINKER -> {
                if (this.retarget <= 0.0f) {
                    this.fishTarget = this.random.nextFloat() < 0.78f ? between(0.04f, 0.5f) : between(0.5f, 0.88f);
                    this.retarget = between(0.9f, 1.8f) * pace;
                }
                stiffness = 9.0f + 9.0f * d;
                maxSpeed = 0.35f + 0.55f * d;
            }
            case JITTER -> {
                this.centerTime -= STEP;
                if (this.centerTime <= 0.0f) {
                    this.center = between(0.14f, 0.86f);
                    this.centerTime = between(1.8f, 3.4f) * pace;
                }
                if (this.retarget <= 0.0f) {
                    this.fishTarget = this.center + between(-1.0f, 1.0f) * Math.min(0.05f + 0.05f * d, this.zoneSize * 0.33f);
                    this.retarget = between(0.14f, 0.3f);
                }
                stiffness = 38.0f + 30.0f * d;
                maxSpeed = 0.45f + 0.6f * d;
            }
            case DARTER -> {
                if (this.retarget <= 0.0f) {
                    if (this.dashing) {
                        // nach dem Schuss steht er still – schwierige Fische schießen manchmal gleich noch einmal
                        this.dashing = false;
                        this.retarget = this.random.nextFloat() < 0.3f * d ? 0.1f : between(1.0f, 1.9f) * (1.1f - 0.1f * d);
                    } else {
                        this.dashing = true;
                        float distance = between(0.22f, 0.26f + 0.22f * d);
                        boolean up = this.random.nextBoolean();
                        if (this.fishPos + distance > 0.95f) {
                            up = false;
                        }
                        if (this.fishPos - distance < 0.05f) {
                            up = true;
                        }
                        this.fishTarget = this.fishPos + (up ? distance : -distance);
                        this.retarget = between(0.45f, 0.7f);
                        this.dart = 1.0f;
                    }
                }
                stiffness = this.dashing ? 30.0f + 30.0f * d : 18.0f;
                maxSpeed = this.dashing ? 0.7f + 0.6f * d : 0.12f;
            }
            default -> {
                if (this.retarget <= 0.0f) {
                    float step = between(0.15f, 0.45f) * (this.random.nextBoolean() ? 1.0f : -1.0f);
                    float target = this.fishPos + step;
                    this.fishTarget = target < 0.05f || target > 0.95f ? this.fishPos - step : target;
                    this.retarget = between(1.0f, 2.0f) * pace;
                }
                stiffness = 9.0f + 9.0f * d;
                maxSpeed = 0.35f + 0.55f * d;
            }
        }
        this.fishTarget = Mth.clamp(this.fishTarget, 0.03f, 0.97f);
        // gedämpfte Feder zum Ziel hin (fast ohne Überschwingen)
        float accel = (this.fishTarget - this.fishPos) * stiffness - this.fishVel * 1.8f * Mth.sqrt(stiffness);
        this.fishVel = Mth.clamp(this.fishVel + accel * STEP, -maxSpeed, maxSpeed);
        this.fishPos += this.fishVel * STEP;
        if (this.fishPos < 0.02f) {
            this.fishPos = 0.02f;
            this.fishVel = 0.0f;
        } else if (this.fishPos > 0.98f) {
            this.fishPos = 0.98f;
            this.fishVel = 0.0f;
        }
    }

    /** Höhe der Fangzone (Anteil der Säule). */
    public float zoneSize() {
        return this.zoneSize;
    }

    /** Unterkante der Fangzone (0 = Boden der Säule). */
    public float zonePos() {
        return this.zonePos;
    }

    public float zoneVel() {
        return this.zoneVel;
    }

    /** Lage des Fischs (0 = Boden, 1 = Oberfläche). */
    public float fishPos() {
        return this.fishPos;
    }

    public float fishVel() {
        return this.fishVel;
    }

    public float progress() {
        return this.progress;
    }

    public boolean inZone() {
        return this.inZone;
    }

    /** Füllt sich die Leiste gerade? (Der Fisch ist in der Zone und die Schnur gespannt – die Zone liegt nicht lose am Boden.) */
    public boolean reeling() {
        return this.reeling;
    }

    /** Bisher nie aus der Zone gelassen? */
    public boolean perfect() {
        return this.perfect;
    }

    /** Lage der Truhe (unter 0: noch keine da). */
    public float chestPos() {
        return this.chestCollected ? -1.0f : this.chestPos;
    }

    public float chestProgress() {
        return this.chestProgress;
    }

    public boolean chestCollected() {
        return this.chestCollected;
    }

    /** 0: läuft noch, 1: gefangen, -1: entkommen. */
    public int result() {
        return this.result;
    }

    /** Sekunden seit Beginn. */
    public float time() {
        return this.time;
    }

    /** Gerade losgeschossen (1, klingt in knapp einer halben Sekunde ab)? Für Spritzer und Ruck. */
    public float dart() {
        return this.dart;
    }

    /** Nur für Vorführungen und Prüfungen: den Stand setzen. */
    public void pose(float zonePos, float fishPos, float progress, float chestPos, float chestProgress) {
        this.zonePos = Mth.clamp(zonePos, 0.0f, 1.0f - this.zoneSize);
        this.fishPos = fishPos;
        this.fishTarget = fishPos;
        this.progress = progress;
        this.chestPos = chestPos;
        this.chestProgress = chestProgress;
        this.inZone = fishPos >= this.zonePos - EDGE && fishPos <= this.zonePos + this.zoneSize + EDGE;
        this.reeling = this.inZone;
    }
}
