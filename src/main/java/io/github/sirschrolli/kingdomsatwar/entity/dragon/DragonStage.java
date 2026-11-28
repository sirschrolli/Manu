package io.github.sirschrolli.kingdomsatwar.entity.dragon;

/**
 * Die fünf Wachstumsstufen eines Drachen.
 *
 * @param scale       Größe relativ zum ausgewachsenen Modell
 * @param maxHealth   Lebenspunkte
 * @param attack      Biss-Schaden
 * @param armor       natürliche Rüstung (Schuppen)
 * @param growthTicks Zeit bis zur nächsten Stufe (1 Minecraft-Tag = 24000 Ticks); -1 = letzte Stufe
 * @param canFly      kann fliegen
 * @param rideable    kann geritten werden
 * @param breath      kann Odem speien
 * @param breathRange Reichweite des Odems in Blöcken
 * @param camera      Kameraabstand in der Außenansicht beim Reiten
 */
public enum DragonStage {
    HATCHLING(1, 0.28f, 16, 2, 0, 24000, false, false, false, 0, 4),
    JUVENILE(2, 0.46f, 36, 4, 2, 36000, false, false, false, 0, 4),
    ADOLESCENT(3, 0.70f, 70, 7, 5, 48000, true, false, true, 6, 6),
    ADULT(4, 1.0f, 140, 12, 9, 96000, true, true, true, 10, 9),
    ELDER(5, 1.32f, 230, 18, 13, -1, true, true, true, 14, 12);

    private final int number;
    private final float scale;
    private final double maxHealth;
    private final double attack;
    private final double armor;
    private final int growthTicks;
    private final boolean canFly;
    private final boolean rideable;
    private final boolean breath;
    private final double breathRange;
    private final double camera;

    DragonStage(int number, float scale, double maxHealth, double attack, double armor, int growthTicks, boolean canFly,
            boolean rideable, boolean breath, double breathRange, double camera) {
        this.number = number;
        this.scale = scale;
        this.maxHealth = maxHealth;
        this.attack = attack;
        this.armor = armor;
        this.growthTicks = growthTicks;
        this.canFly = canFly;
        this.rideable = rideable;
        this.breath = breath;
        this.breathRange = breathRange;
        this.camera = camera;
    }

    public int number() {
        return number;
    }

    public float scale() {
        return scale;
    }

    public double maxHealth() {
        return maxHealth;
    }

    public double attack() {
        return attack;
    }

    public double armor() {
        return armor;
    }

    public int growthTicks() {
        return growthTicks;
    }

    public boolean canFly() {
        return canFly;
    }

    public boolean rideable() {
        return rideable;
    }

    public boolean canBreathe() {
        return breath;
    }

    public double breathRange() {
        return breathRange;
    }

    public double cameraDistance() {
        return camera;
    }

    public boolean isLast() {
        return growthTicks < 0;
    }

    public DragonStage next() {
        return isLast() ? this : values()[ordinal() + 1];
    }

    public static DragonStage byNumber(int number) {
        for (DragonStage stage : values()) {
            if (stage.number == number) {
                return stage;
            }
        }
        return HATCHLING;
    }

    /** Übersetzungsschlüssel, z. B. {@code dragon.kingdomsatwar.stage.3}. */
    public String translationKey() {
        return "dragon.kingdomsatwar.stage." + number;
    }
}
