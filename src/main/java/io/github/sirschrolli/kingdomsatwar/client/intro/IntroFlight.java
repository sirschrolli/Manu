package io.github.sirschrolli.kingdomsatwar.client.intro;

import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.entity.IntroVisionEntity;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossAnims;
import io.github.sirschrolli.kingdomsatwar.entity.boss.DragonBossEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModParticles;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleSupplier;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Das Erzähl-Intro „Das Siegel“ als Bilderfolge in der eigenen Welt – alles nur auf dem eigenen Bildschirm:
 * <ol>
 *     <li><b>Die Dunkelheit:</b> Vor dem Spieler steigt eine Säule aus Schatten aus der Erde, in ihr erhebt sich der
 *     Schattendrache; Schwaden kriechen über das Land, das Bild wird dunkel.</li>
 *     <li><b>Die sechs Drachen:</b> Pyrrhon, Voltarion, Verdantis, Glaciara und Thalassa fliegen aus allen Richtungen
 *     heran, Terragon bricht aus dem Boden. Sie bilden ein Sechseck um die Dunkelheit, aus ihren Herzen laufen Strahlen in
 *     ihren Farben zum Schattendrachen; eine Lichtkugel umschließt ihn und drückt ihn mitsamt der Säule unter die Erde.
 *     Blitz, Druckwelle – und am Boden leuchtet das Siegel. Sechs Splitter fliegen in die Herzen der Drachen.</li>
 *     <li><b>Die Last des Siegels:</b> Die Drachen werden wild und ziehen davon; in schnellen Schnitten jeder in seinem
 *     Element (Asche, Eis, Sturm, Hain, Fels, Meer).</li>
 *     <li><b>Der Auftrag:</b> Eine Abteilung Soldaten salutiert, am Horizont stehen sechs Lichtsäulen über den Nestern.</li>
 *     <li><b>Die Warnung:</b> Unter der Erde pocht es – die Dunkelheit wartet. Dann der Titel.</li>
 * </ol>
 * Die Zeitpunkte richten sich nach den Zeilen des Erzählers in der gewählten Sprache. Die Flughöhen passen sich dem
 * Gelände an.
 */
final class IntroFlight {
    /** Die Drachen im Sechseck (wie im Siegelkreis: Gegensätze liegen sich gegenüber). */
    static final DragonType[] RING = {DragonType.FIRE, DragonType.LIGHTNING, DragonType.NATURE, DragonType.ICE, DragonType.WATER,
            DragonType.EARTH};
    /** Reihenfolge der Schnitte in Zeile sieben: Asche, Eis, Sturmgipfel, Hain, Fels, Meer. */
    static final DragonType[] MONTAGE = {DragonType.FIRE, DragonType.ICE, DragonType.LIGHTNING, DragonType.NATURE, DragonType.EARTH,
            DragonType.WATER};
    static final int HEARTBEATS = 4;
    private static final int FIRST_ID = -2_100_000_000;
    private static final int FIRST_SOLDIER_ID = -2_050_000_000;
    /** Halbmesser des Sechsecks der Drachen. */
    private static final double RADIUS = 30.0;
    /** Abstand der Mitte vor dem Spieler – alles bleibt nahe genug, dass es auch bei kleiner Sichtweite geladen ist. */
    private static final double AHEAD = 42.0;
    private static final int EARTH = indexOf(DragonType.EARTH);
    private static final int WATER = indexOf(DragonType.WATER);
    private static final int FIRE = indexOf(DragonType.FIRE);

    private static @Nullable IntroFlight active;

    /** Ton abspielen (die Bilderfolge kennt den Bildschirm nicht). */
    interface Sounds {
        void play(SoundEvent sound, float volume, float pitch);
    }

    private final ClientLevel level;
    private final DoubleSupplier clock;
    private final Sounds sounds;
    private final RandomSource random = RandomSource.create(4711L);
    private final Vec3 home;
    private final Vec3 forward;
    private final Vec3 right;
    private final Vec3 center;
    private final double hover;
    private final double baseAngle;
    private final double length;
    private final double[] lineStart = new double[11];
    private final double[] lineEnd = new double[11];

    // Zeitmarken (Sekunden)
    private final double riseStart;
    private final double riseEnd;
    private final double spreadStart;
    private final double arriveStart;
    private final double arriveEnd;
    private final double beamStart;
    private final double sinkStart;
    private final double seal;
    private final double shardStart;
    private final double shardEnd;
    private final double scatter;
    private final double montageStart;
    private final double montageEnd;
    private final double homeStart;
    private final double salute;
    private final double pillarsStart;
    private final double warnStart;
    private final double finalStart;
    private final double[] cuts;

    private final DragonBossEntity[] dragons = new DragonBossEntity[6];
    private final Vec3[] groundSpot = new Vec3[6];
    private @Nullable DragonBossEntity nyxar;
    private @Nullable IntroVisionEntity vision;
    private final List<SoldierEntity> soldiers = new ArrayList<>();
    private Vec3 soldierCenter;
    private final boolean[] done = new boolean[48];
    private double cameraLift;
    private int lastSegment = -1;

    IntroFlight(ClientLevel level, Vec3 home, float playerYaw, IntroScript script, List<IntroScript.Line> lines, DoubleSupplier clock,
            Sounds sounds) {
        this.level = level;
        this.clock = clock;
        this.sounds = sounds;
        this.home = home;
        this.length = script.length();
        double yaw = Math.toRadians(playerYaw);
        this.forward = new Vec3(-Math.sin(yaw), 0.0, Math.cos(yaw));
        this.right = new Vec3(-this.forward.z, 0.0, this.forward.x);
        this.baseAngle = Math.atan2(this.forward.z, this.forward.x);
        // Mitte der Vision: vor dem Spieler, auf dem Boden
        Vec3 c = home.add(this.forward.scale(AHEAD));
        int ground = this.ground(c.x, c.z, (int) home.y);
        this.center = new Vec3(Math.floor(c.x) + 0.5, ground, Math.floor(c.z) + 0.5);
        int highest = ground;
        for (int dx = -40; dx <= 40; dx += 8) {
            for (int dz = -40; dz <= 40; dz += 8) {
                highest = Math.max(highest, this.ground(this.center.x + dx, this.center.z + dz, ground));
            }
        }
        this.hover = Math.max(highest + 16.0, ground + 27.0);
        this.soldierCenter = this.clearing(home.add(this.forward.scale(9.0)));
        for (int i = 0; i < 6; i++) {
            Vec3 spot = this.center.add(this.dir(i).scale(RADIUS));
            this.groundSpot[i] = new Vec3(spot.x, this.ground(spot.x, spot.z, ground), spot.z);
        }

        // Zeilen des Erzählers (fehlen welche, werden sie aus den Abschnitten geschätzt)
        double world = script.section("world", 6.7);
        double dragonsAt = script.section("dragons", 17.0);
        double wonder = script.section("wonder", 33.0);
        double knights = script.section("knights", 47.0);
        double finale = script.section("finale", 60.0);
        double[] guess = {world, world + (dragonsAt - world) * 0.5, dragonsAt, dragonsAt + (wonder - dragonsAt) * 0.33,
                dragonsAt + (wonder - dragonsAt) * 0.66, wonder, wonder + (knights - wonder) * 0.4, knights,
                knights + (finale - knights) * 0.5, finale, finale + 4.0};
        for (int i = 0; i < 11; i++) {
            if (i < lines.size()) {
                this.lineStart[i] = lines.get(i).start();
                this.lineEnd[i] = lines.get(i).end();
            } else {
                this.lineStart[i] = guess[i];
                this.lineEnd[i] = guess[i] + 3.5;
            }
        }
        double[] l = this.lineStart;
        double[] e = this.lineEnd;
        this.riseStart = Math.max(0.8, l[0] - 2.5);
        this.riseEnd = l[1];
        this.spreadStart = l[1] - 0.2;
        this.arriveStart = l[2] - 1.4;
        this.arriveEnd = Math.max(l[3] - 0.3, this.arriveStart + 5.0);
        this.beamStart = Math.max(l[3] + 0.2, this.arriveEnd);
        this.sinkStart = this.beamStart + 1.6;
        this.seal = Math.max(e[3] + 0.3, this.sinkStart + 2.6);
        this.shardStart = Math.max(l[4] + 0.1, this.seal + 0.9);
        this.shardEnd = Math.max(e[4] + 0.2, this.shardStart + 3.0);
        this.scatter = Math.max(l[5] + 0.2, this.shardEnd);
        this.montageStart = Math.max(l[6] - 0.15, this.scatter + 2.5);
        this.homeStart = Math.max(Math.min(e[6] + 0.45, l[7] - 0.8), this.montageStart + 6.0);
        this.montageEnd = this.homeStart;
        this.salute = l[7] + 1.0;
        this.pillarsStart = l[8] - 0.4;
        this.warnStart = l[9] - 0.3;
        this.finalStart = l[10] - 0.5;
        double shot = (this.montageEnd - this.montageStart) / 6.0;
        // erst sieht man von oben alle sechs Splitter aus dem Siegel aufsteigen, dann Pyrrhon aus der Nähe
        double closeUp = Math.min(Math.max(this.shardStart + 3.6, e[4] - 0.4), this.scatter - 1.5);
        this.cuts = new double[] {closeUp, this.montageStart, this.montageStart + shot, this.montageStart + 2 * shot,
                this.montageStart + 3 * shot, this.montageStart + 4 * shot, this.montageStart + 5 * shot, this.homeStart};
        active = this;
    }

    static @Nullable IntroFlight active() {
        return active;
    }

    /** Sekunden seit Beginn (fließend, für die Lichteffekte). */
    double now() {
        return this.clock.getAsDouble();
    }

    Vec3 center() {
        return this.center;
    }

    private static int indexOf(DragonType type) {
        for (int i = 0; i < RING.length; i++) {
            if (RING[i] == type) {
                return i;
            }
        }
        return 0;
    }

    /** Winkel (Bogenmaß, in der x-z-Ebene) des Platzes im Sechseck: Pyrrhon steht auf der anderen Seite der Mitte. */
    double ringAngle(int i) {
        return this.baseAngle + i * Math.PI / 3.0;
    }

    private Vec3 dir(int i) {
        double a = this.ringAngle(i);
        return new Vec3(Math.cos(a), 0.0, Math.sin(a));
    }

    /** Höchster Punkt (mit Baumkronen) rund um eine Stelle. */
    private double clearance(Vec3 pos) {
        int max = this.ground(pos.x, pos.z, (int) pos.y - 3);
        for (int dx = -6; dx <= 6; dx += 3) {
            for (int dz = -6; dz <= 6; dz += 3) {
                max = Math.max(max, this.ground(pos.x + dx, pos.z + dz, max));
            }
        }
        return max;
    }

    /**
     * Ein freier Platz für die Soldaten nahe der gewünschten Stelle: möglichst ohne Laub darüber, nicht im Wasser und
     * eben (im Wald stünden sie sonst unsichtbar unter den Bäumen).
     */
    private Vec3 clearing(Vec3 wanted) {
        Vec3 best = wanted;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (int dx = -18; dx <= 18; dx += 3) {
            for (int dz = -18; dz <= 18; dz += 3) {
                double x = wanted.x + dx;
                double z = wanted.z + dz;
                int free = 0;
                int min = Integer.MAX_VALUE;
                int max = Integer.MIN_VALUE;
                for (int ox = -3; ox <= 3; ox += 2) {
                    for (int oz = -4; oz <= 2; oz += 2) {
                        int bx = Mth.floor(x + ox);
                        int bz = Mth.floor(z + oz);
                        int open = this.level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, bx, bz);
                        int top = this.level.getHeight(Heightmap.Types.MOTION_BLOCKING, bx, bz);
                        boolean water = !this.level.getFluidState(new BlockPos(bx, open - 1, bz)).isEmpty();
                        if (top == open && !water) {
                            free++;
                        }
                        min = Math.min(min, open);
                        max = Math.max(max, open);
                    }
                }
                double score = free * 3.0 - (max - min) * 2.0 - Math.sqrt(dx * dx + dz * dz) * 0.15;
                if (score > bestScore) {
                    bestScore = score;
                    best = new Vec3(Math.floor(x) + 0.5, wanted.y, Math.floor(z) + 0.5);
                }
            }
        }
        return best;
    }

    private int ground(double x, double z, int fallback) {
        int h = this.level.getHeight(Heightmap.Types.MOTION_BLOCKING, Mth.floor(x), Mth.floor(z));
        return h <= this.level.getMinY() + 1 ? fallback : h;
    }

    // =====================================================================================
    // Ablauf
    // =====================================================================================

    void tick(double t) {
        if (this.vision == null) {
            this.vision = ModEntities.INTRO_VISION.get().create(this.level, EntitySpawnReason.LOAD);
            if (this.vision != null) {
                this.vision.setId(FIRST_ID - 2000);
                this.vision.snapTo(this.center.x, this.center.y, this.center.z, 0.0f, 0.0f);
                this.level.addEntity(this.vision);
            }
        }
        this.updateNyxar(t);
        for (int i = 0; i < 6; i++) {
            this.updateDragon(i, t);
        }
        this.updateSoldiers(t);
        this.particles(t);
        this.events(t);
    }

    private boolean once(int id, double t, double at) {
        if (!this.done[id] && t >= at) {
            this.done[id] = true;
            return true;
        }
        return false;
    }

    /** Töne und einmalige Ereignisse. */
    private void events(double t) {
        if (this.once(0, t, this.riseStart + 0.3)) {
            this.sounds.play(SoundEvents.WARDEN_EMERGE, 0.9f, 0.6f);
        }
        if (this.once(1, t, this.lineStart[0] + 3.0) && this.nyxar != null) {
            this.nyxar.cinematicAnim(BossAnims.ROAR);
            this.sounds.play(ModSounds.DRAGON_ROAR.get(), 0.9f, 0.5f);
        }
        if (this.once(2, t, this.spreadStart)) {
            this.sounds.play(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_MOOD.value(), 0.8f, 0.7f);
        }
        for (int i = 0; i < 6; i++) {
            if (this.once(3 + i, t, this.arrivalEnd(i) - 0.6)) {
                DragonBossEntity dragon = this.dragons[i];
                if (dragon != null) {
                    dragon.cinematicAnim(BossAnims.ROAR);
                }
                if (i % 2 == 0) {
                    this.sounds.play(ModSounds.DRAGON_ROAR.get(), 0.6f, 0.8f + i * 0.05f);
                }
            }
        }
        if (this.once(9, t, this.beamStart)) {
            this.sounds.play(SoundEvents.BEACON_ACTIVATE, 1.0f, 0.7f);
        }
        if (this.once(10, t, this.sinkStart) && this.nyxar != null) {
            this.nyxar.cinematicAnim(BossAnims.ROAR);
            this.sounds.play(ModSounds.DRAGON_ROAR.get(), 0.8f, 0.42f);
        }
        if (this.once(11, t, this.seal)) {
            this.sounds.play(SoundEvents.LIGHTNING_BOLT_THUNDER, 0.8f, 0.75f);
            this.sounds.play(SoundEvents.BEACON_POWER_SELECT, 1.0f, 0.6f);
            this.sounds.play(SoundEvents.GENERIC_EXPLODE.value(), 0.45f, 0.6f);
            this.burst();
        }
        for (int i = 0; i < 6; i++) {
            if (this.once(12 + i, t, this.shardArrival(i))) {
                this.sounds.play(SoundEvents.AMETHYST_BLOCK_CHIME, 1.0f, 0.7f + i * 0.08f);
            }
        }
        if (this.once(18, t, this.scatter + 0.2)) {
            DragonBossEntity fire = this.dragons[FIRE];
            if (fire != null) {
                fire.cinematicAnim(BossAnims.ROAR);
            }
            this.sounds.play(ModSounds.DRAGON_ROAR.get(), 0.9f, 0.68f);
        }
        for (int j = 0; j < 6; j++) {
            if (this.once(19 + j, t, this.cuts[1 + j])) {
                this.montageSound(MONTAGE[j]);
            }
        }
        if (this.once(25, t, this.salute)) {
            for (SoldierEntity soldier : this.soldiers) {
                soldier.handleEntityEvent(soldier.getSoldierType() == SoldierType.CAPTAIN ? SoldierEntity.EVENT_CHEER : SoldierEntity.EVENT_SALUTE);
            }
        }
        if (this.once(26, t, this.pillarsStart)) {
            this.sounds.play(SoundEvents.BEACON_AMBIENT, 1.0f, 0.8f);
        }
        for (int k = 0; k < HEARTBEATS; k++) {
            if (this.once(27 + k, t, this.heartbeat(k))) {
                this.sounds.play(SoundEvents.WARDEN_HEARTBEAT, 1.0f, 0.75f);
                if (k == 0) {
                    this.sounds.play(SoundEvents.WARDEN_EMERGE, 0.35f, 0.5f);
                }
            }
        }
        if (this.once(31, t, this.finalStart + 1.5)) {
            this.sounds.play(ModSounds.DRAGON_ROAR.get(), 0.35f, 0.7f);
        }
    }

    private void montageSound(DragonType type) {
        switch (type) {
            case FIRE -> this.sounds.play(SoundEvents.FIRECHARGE_USE, 0.7f, 0.6f);
            case ICE -> this.sounds.play(SoundEvents.GLASS_BREAK, 0.6f, 0.55f);
            case LIGHTNING -> this.sounds.play(SoundEvents.LIGHTNING_BOLT_IMPACT, 0.5f, 0.8f);
            case NATURE -> this.sounds.play(SoundEvents.AZALEA_LEAVES_BREAK, 1.0f, 0.6f);
            case EARTH -> this.sounds.play(SoundEvents.ROOTED_DIRT_BREAK, 1.0f, 0.5f);
            default -> this.sounds.play(SoundEvents.GENERIC_SPLASH, 0.8f, 0.6f);
        }
    }

    // =====================================================================================
    // Der Schattendrache in der Säule
    // =====================================================================================

    private Vec3 nyxarPosition(double t) {
        double top = this.hover + 8.0;
        double y;
        if (t < this.sinkStart) {
            double k = ease((t - this.riseStart - 0.6) / Math.max(1.0, this.riseEnd - 0.5 - this.riseStart - 0.6));
            y = Mth.lerp(k, this.center.y - 24.0, top) + Math.sin(t * 0.9) * 0.8 * k;
        } else {
            double k = Mth.clamp((t - this.sinkStart) / Math.max(0.5, this.seal - this.sinkStart), 0.0, 1.0);
            y = Mth.lerp(k * k, top, this.center.y - 28.0);
        }
        double wobble = t >= this.sinkStart ? Math.sin(t * 7.0) * 0.6 : 0.0;
        return this.center.add(this.right.scale(wobble)).with(net.minecraft.core.Direction.Axis.Y, y);
    }

    private void updateNyxar(double t) {
        boolean visible = t >= this.riseStart && t < this.seal + 0.05;
        if (!visible) {
            if (this.nyxar != null) {
                this.level.removeEntity(this.nyxar.getId(), Entity.RemovalReason.DISCARDED);
                this.nyxar = null;
            }
            return;
        }
        Vec3 pos = this.nyxarPosition(t);
        if (this.nyxar == null) {
            this.nyxar = this.spawnBoss(ModEntities.NYXAR.get(), 10, pos, DragonBossEntity.MoveMode.AIR, 1);
        }
        if (this.nyxar != null) {
            // blickt dem Betrachter entgegen
            this.move(this.nyxar, pos, (float) Math.toDegrees(Math.atan2(this.forward.x, -this.forward.z)), 0.0f);
        }
    }

    // =====================================================================================
    // Die sechs Drachen
    // =====================================================================================

    private double arrivalStart(int i) {
        return this.arriveStart + 0.45 * i;
    }

    private double arrivalEnd(int i) {
        return this.arriveEnd - 0.25 * (5 - i);
    }

    private double scatterStart(int i) {
        return this.scatter + 0.3 * i;
    }

    /** Wann Terragon im Schnitt „tief im Fels“ versinkt. */
    private double earthDive() {
        return this.cuts[1 + 4] + 0.15;
    }

    private Vec3 hoverPosition(int i, double t) {
        double low = i == WATER ? -4.0 : 0.0;
        return this.center.add(this.dir(i).scale(RADIUS)).with(net.minecraft.core.Direction.Axis.Y,
                this.hover + low + Math.sin(t * 0.8 + i * 1.1) * 0.7);
    }

    private @Nullable Vec3 dragonPosition(int i, double t) {
        if (i == EARTH) {
            return this.earthPosition(t);
        }
        double a0 = this.arrivalStart(i);
        if (t < a0) {
            return null;
        }
        Vec3 hoverAt = this.hoverPosition(i, t);
        double a1 = this.arrivalEnd(i);
        if (t < a1) {
            double u = easeOut((t - a0) / (a1 - a0));
            Vec3 d = this.dir(i);
            Vec3 swing = rotate(d, Math.toRadians(38.0));
            // aus großer Höhe herab (nicht von weit her: dort wäre die Welt nicht geladen)
            Vec3 start = this.center.add(d.scale(48.0)).with(net.minecraft.core.Direction.Axis.Y, this.hover + 58.0);
            Vec3 mid = this.center.add(swing.scale(44.0)).with(net.minecraft.core.Direction.Axis.Y, this.hover + 16.0);
            return bezier(start, mid, hoverAt, u);
        }
        double s0 = this.scatterStart(i);
        if (t < s0) {
            return hoverAt;
        }
        double u = t - s0;
        double radius = Math.min(50.0, RADIUS + 8.0 * u + 0.8 * u * u);
        double angle = this.ringAngle(i) + 0.17 * u;
        double rise = Math.min(30.0, 3.4 * u) + (i == WATER ? -10.0 * Math.min(1.0, u / 4.0) : 0.0);
        return new Vec3(this.center.x + Math.cos(angle) * radius, this.hover + rise + Math.sin(t * 0.8 + i) * 0.7,
                this.center.z + Math.sin(angle) * radius);
    }

    /** Terragon fliegt nicht: Er bricht aus dem Boden, steht im Sechseck und versinkt später wieder im Fels. */
    private @Nullable Vec3 earthPosition(double t) {
        double emerge = this.arrivalStart(EARTH) + 1.0;
        if (t < emerge - 0.2) {
            return null;
        }
        Vec3 spot = this.groundSpot[EARTH];
        double up = easeOut((t - emerge) / 2.4);
        double down = smooth((t - this.earthDive()) / 1.7);
        if (down >= 1.0) {
            return null;
        }
        return spot.add(0.0, -15.0 * (1.0 - up) - 16.0 * down, 0.0);
    }

    private void updateDragon(int i, double t) {
        Vec3 pos = this.dragonPosition(i, t);
        boolean gone = pos == null || t >= this.homeStart;
        if (gone) {
            if (this.dragons[i] != null) {
                this.level.removeEntity(this.dragons[i].getId(), Entity.RemovalReason.DISCARDED);
                this.dragons[i] = null;
            }
            return;
        }
        DragonType type = RING[i];
        DragonBossEntity dragon = this.dragons[i];
        if (dragon == null) {
            EntityType<? extends DragonBossEntity> entityType = ModEntities.bossType(type);
            DragonBossEntity.MoveMode mode = i == EARTH ? DragonBossEntity.MoveMode.GROUND
                    : i == WATER ? DragonBossEntity.MoveMode.WATER : DragonBossEntity.MoveMode.AIR;
            dragon = entityType == null ? null : this.spawnBoss(entityType, i, pos, mode, 1);
            this.dragons[i] = dragon;
            if (dragon == null) {
                return;
            }
        }
        if (this.once(32 + i, t, this.scatterStart(i))) {
            // die Last des Siegels: wild und wütend
            dragon.cinematic(i == EARTH ? DragonBossEntity.MoveMode.GROUND : i == WATER ? DragonBossEntity.MoveMode.WATER
                    : DragonBossEntity.MoveMode.AIR, 3);
            dragon.setHealth(dragon.getMaxHealth() * 0.2f);
        }
        float yaw;
        float pitch = 0.0f;
        boolean hovering = t >= this.arrivalEnd(i) && t < this.scatterStart(i);
        if (i == EARTH) {
            Vec3 look = t < this.scatterStart(i) ? this.center.subtract(pos) : this.dir(i);
            yaw = yawOf(look);
        } else if (hovering) {
            yaw = yawOf(this.center.subtract(pos));
        } else {
            Vec3 ahead = this.dragonPosition(i, t + 0.25);
            Vec3 velocity = ahead == null ? this.dir(i) : ahead.subtract(pos);
            yaw = yawOf(velocity);
            double horizontal = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
            pitch = (float) Mth.clamp(-Math.toDegrees(Math.atan2(velocity.y, Math.max(horizontal, 1.0e-3))), -25.0, 25.0);
            // weich in die Blickrichtung zur Mitte übergehen, kurz bevor der Drache seinen Platz erreicht
            double settle = smooth((t - (this.arrivalEnd(i) - 1.2)) / 1.2);
            if (t < this.arrivalEnd(i) && settle > 0.0) {
                yaw = (float) (yaw + Mth.wrapDegrees(yawOf(this.center.subtract(pos)) - yaw) * settle);
            }
        }
        this.move(dragon, pos, yaw, pitch);
    }

    private <T extends DragonBossEntity> @Nullable T spawnBoss(EntityType<T> type, int index, Vec3 pos, DragonBossEntity.MoveMode mode,
            int phase) {
        T boss = type.create(this.level, EntitySpawnReason.LOAD);
        if (boss == null) {
            return null;
        }
        // Nummern für den Boss und seine Trefferzonen (wie beim Enderdrachen: die Teile folgen dem Körper)
        int id = FIRST_ID - index * 64;
        boss.setId(id);
        net.neoforged.neoforge.entity.PartEntity<?>[] parts = boss.getParts();
        for (int k = 0; parts != null && k < parts.length; k++) {
            parts[k].setId(id - 1 - k);
        }
        boss.setSilent(true);
        boss.setNoAi(true);
        boss.snapTo(pos.x, pos.y, pos.z, 0.0f, 0.0f);
        boss.cinematic(mode, phase);
        this.level.addEntity(boss);
        return boss;
    }

    private void move(DragonBossEntity boss, Vec3 pos, float yaw, float pitch) {
        Vec3 velocity = pos.subtract(boss.position());
        boss.moveOrInterpolateTo(pos, yaw, pitch);
        boss.setYBodyRot(yaw);
        boss.setYHeadRot(yaw);
        boss.setDeltaMovement(velocity.length() > 8.0 ? Vec3.ZERO : velocity); // nur für die Animation
    }

    /** Herz eines Drachen (Ausgang der Strahlen, Ziel der Splitter). */
    @Nullable Vec3 chest(int i, float partial) {
        DragonBossEntity dragon = this.dragons[i];
        if (dragon == null) {
            return null;
        }
        Vec3 pos = dragon.getPosition(partial);
        float yaw = dragon.getYRot();
        Vec3 ahead = new Vec3(-Mth.sin(yaw * Mth.DEG_TO_RAD), 0.0, Mth.cos(yaw * Mth.DEG_TO_RAD));
        return pos.add(ahead.scale(2.5)).add(0.0, dragon.getBbHeight() * 0.55, 0.0);
    }

    /** Herz des Schattendrachen (Ziel der Strahlen). */
    @Nullable Vec3 focus(float partial) {
        if (this.nyxar == null) {
            return null;
        }
        return this.nyxar.getPosition(partial).add(0.0, this.nyxar.getBbHeight() * 0.5, 0.0);
    }

    // =====================================================================================
    // Soldaten
    // =====================================================================================

    private void updateSoldiers(double t) {
        if (t < this.homeStart - 0.1) {
            return;
        }
        if (this.soldiers.isEmpty() && !this.done[40]) {
            this.done[40] = true;
            this.spawnSoldiers();
        }
    }

    private void spawnSoldiers() {
        Vec3 facing = this.forward;
        float yaw = yawOf(facing);
        SoldierType[][] rows = {
                {SoldierType.CAPTAIN},
                {SoldierType.KNIGHT, SoldierType.SWORDSMAN, SoldierType.SWORDSMAN, SoldierType.KNIGHT},
                {SoldierType.ARCHER, SoldierType.ARCHER, SoldierType.ARCHER, SoldierType.ARCHER}};
        int id = 0;
        for (int row = 0; row < rows.length; row++) {
            for (int col = 0; col < rows[row].length; col++) {
                double side = (col - (rows[row].length - 1) / 2.0) * 1.8;
                Vec3 spot = this.soldierCenter.add(facing.scale(2.5 - row * 2.0)).add(this.right.scale(side));
                BlockPos column = BlockPos.containing(spot);
                int ground = this.level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, column.getX(), column.getZ());
                if (!this.level.getFluidState(new BlockPos(column.getX(), ground - 1, column.getZ())).isEmpty()) {
                    continue; // nicht auf dem Wasser
                }
                SoldierType type = rows[row][col];
                SoldierEntity soldier = ModEntities.SOLDIERS.get(type).get().create(this.level, EntitySpawnReason.LOAD);
                if (soldier == null) {
                    continue;
                }
                soldier.setId(FIRST_SOLDIER_ID - id++);
                soldier.equip();
                soldier.setVariant((row * 3 + col) % type.variants());
                soldier.setNoAi(true);
                soldier.snapTo(spot.x, ground, spot.z, yaw, 0.0f);
                soldier.setYBodyRot(yaw);
                soldier.setYHeadRot(yaw);
                this.level.addEntity(soldier);
                this.soldiers.add(soldier);
            }
        }
        if (!this.soldiers.isEmpty()) {
            double y = 0.0;
            for (SoldierEntity soldier : this.soldiers) {
                y += soldier.getY();
            }
            this.soldierCenter = this.soldierCenter.with(net.minecraft.core.Direction.Axis.Y, y / this.soldiers.size());
        } else {
            this.soldierCenter = this.soldierCenter.with(net.minecraft.core.Direction.Axis.Y,
                    this.ground(this.soldierCenter.x, this.soldierCenter.z, (int) this.home.y));
        }
    }

    // =====================================================================================
    // Teilchen
    // =====================================================================================

    private void add(ParticleOptions particle, double x, double y, double z, double dx, double dy, double dz) {
        this.level.addAlwaysVisibleParticle(particle, true, x, y, z, dx, dy, dz);
    }

    private void particles(double t) {
        // Säule und Schwaden der Dunkelheit
        float height = this.columnHeight(t);
        float radius = this.columnRadius(t);
        if (height > 1.0f && t < this.seal) {
            for (int k = 0; k < 16; k++) {
                double a = this.random.nextDouble() * Mth.TWO_PI;
                double r = this.random.nextDouble() * radius * 1.4;
                double y = this.center.y - this.columnSink(t) + this.random.nextDouble() * height;
                this.add(ModParticles.BOSS_SHADOW.get(), this.center.x + Math.cos(a) * r, y, this.center.z + Math.sin(a) * r,
                        -Math.sin(a) * 0.12, 0.08 + this.random.nextDouble() * 0.18, Math.cos(a) * 0.12);
            }
            // dichter, dunkler Rauch steigt in der Säule auf (die großen Rauchschwaden der Signalfeuer)
            for (int k = 0; k < 3; k++) {
                double a = this.random.nextDouble() * Mth.TWO_PI;
                double r = this.random.nextDouble() * radius * 1.1;
                this.add(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, this.center.x + Math.cos(a) * r, this.center.y - 1.0,
                        this.center.z + Math.sin(a) * r, 0.0, 0.16 + this.random.nextDouble() * 0.08, 0.0);
            }
            for (int k = 0; k < 2; k++) {
                double a = this.random.nextDouble() * Mth.TWO_PI;
                double r = radius * (1.0 + this.random.nextDouble() * 2.0);
                this.add(ParticleTypes.SQUID_INK, this.center.x + Math.cos(a) * r, this.center.y + 0.3, this.center.z + Math.sin(a) * r,
                        0.0, 0.08, 0.0);
            }
        }
        if (t >= this.spreadStart && t < this.seal && ((int) (t * 20.0)) % 2 == 0) {
            double reach = Math.min(70.0, 8.0 + 9.0 * (t - this.spreadStart));
            for (int k = 0; k < 44; k++) {
                double a = this.random.nextDouble() * Mth.TWO_PI;
                double r = reach * (0.82 + this.random.nextDouble() * 0.18);
                double x = this.center.x + Math.cos(a) * r;
                double z = this.center.z + Math.sin(a) * r;
                this.add(ModParticles.BOSS_SHADOW.get(), x, this.ground(x, z, (int) this.center.y) + 0.4, z, Math.cos(a) * 0.05,
                        0.03 + this.random.nextDouble() * 0.05, Math.sin(a) * 0.05);
            }
        }
        // Funken entlang der Strahlen
        double beams = this.beams(t);
        Vec3 focus = this.focus(1.0f);
        if (beams > 0.2 && focus != null) {
            for (int i = 0; i < 6; i++) {
                Vec3 chest = this.chest(i, 1.0f);
                if (chest == null) {
                    continue;
                }
                double k = this.random.nextDouble();
                Vec3 p = chest.lerp(focus, k);
                this.add(ParticleTypes.END_ROD, p.x, p.y, p.z, (this.random.nextDouble() - 0.5) * 0.05, 0.02,
                        (this.random.nextDouble() - 0.5) * 0.05);
            }
        }
        // Spuren der Drachen in ihrem Element (beim Anflug und in den schnellen Schnitten)
        for (int i = 0; i < 6; i++) {
            DragonBossEntity dragon = this.dragons[i];
            if (dragon == null) {
                continue;
            }
            boolean arriving = t < this.arrivalEnd(i) + 0.5;
            boolean starring = this.montageIndex(t) >= 0 && MONTAGE[this.montageIndex(t)] == RING[i];
            if (!arriving && !starring) {
                continue;
            }
            // die Spur zieht hinter dem Drachen her (nicht vor seinem Kopf – sonst verdeckte sie ihn in der Nahaufnahme)
            Vec3 ahead = this.dragonPosition(i, t + 0.25);
            Vec3 back = ahead == null ? Vec3.ZERO : dragon.position().subtract(ahead);
            Vec3 behind = back.lengthSqr() < 1.0e-4 ? Vec3.ZERO : back.normalize();
            int count = starring ? 3 : 2;
            for (int k = 0; k < count; k++) {
                double off = 6.0 + this.random.nextDouble() * 10.0;
                double x = dragon.getX() + behind.x * off + (this.random.nextDouble() - 0.5) * 4.0;
                double y = dragon.getY() + this.random.nextDouble() * dragon.getBbHeight() * 0.7 + behind.y * off;
                double z = dragon.getZ() + behind.z * off + (this.random.nextDouble() - 0.5) * 4.0;
                this.add(trail(RING[i]), x, y, z, (this.random.nextDouble() - 0.5) * 0.06, this.random.nextDouble() * 0.04,
                        (this.random.nextDouble() - 0.5) * 0.06);
            }
        }
        // Terragon: Staub und Brocken beim Auftauchen und Versinken
        Vec3 spot = this.groundSpot[EARTH];
        double emerge = this.arrivalStart(EARTH) + 1.0;
        boolean digging = (t > emerge - 0.2 && t < emerge + 2.6) || (t > this.earthDive() - 0.1 && t < this.earthDive() + 1.9);
        if (digging) {
            for (int k = 0; k < 10; k++) {
                double a = this.random.nextDouble() * Mth.TWO_PI;
                double r = 2.0 + this.random.nextDouble() * 7.0;
                double x = spot.x + Math.cos(a) * r;
                double z = spot.z + Math.sin(a) * r;
                this.add(ModParticles.BOSS_DUST.get(), x, spot.y + 0.3, z, Math.cos(a) * 0.12, 0.12 + this.random.nextDouble() * 0.15,
                        Math.sin(a) * 0.12);
                this.add(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.COARSE_DIRT.defaultBlockState()), x, spot.y + 0.5, z,
                        Math.cos(a) * 0.2, 0.35 + this.random.nextDouble() * 0.3, Math.sin(a) * 0.2);
            }
        }
        // die Splitter ziehen eine Lichtspur
        for (int i = 0; i < 6; i++) {
            double k = this.shard(i, t);
            Vec3 p = k >= 0.0 ? this.shardPosition(i, k, 1.0f) : null;
            if (p != null) {
                this.add(ParticleTypes.END_ROD, p.x, p.y, p.z, 0.0, 0.0, 0.0);
            }
        }
        // Warnung: Schatten sickern aus dem Boden, wo die Druckwellen vorbeiziehen
        for (int k = 0; k < HEARTBEATS; k++) {
            double age = this.heartbeatAge(k, t);
            if (age < 0.0 || age > 2.2) {
                continue;
            }
            double reach = age * 30.0;
            for (int n = 0; n < 14; n++) {
                double a = this.random.nextDouble() * Mth.TWO_PI;
                double x = this.center.x + Math.cos(a) * reach;
                double z = this.center.z + Math.sin(a) * reach;
                this.add(ModParticles.BOSS_SHADOW.get(), x, this.ground(x, z, (int) this.center.y) + 0.3, z, 0.0, 0.06, 0.0);
            }
        }
    }

    /** Lichtblitz beim Schmieden des Siegels: Funken nach allen Seiten, die Schwaden reißen auseinander. */
    private void burst() {
        this.add(net.minecraft.core.particles.ColorParticleOption.create(ParticleTypes.FLASH, 0xFFFFF4FF), this.center.x, this.center.y + 3.0,
                this.center.z, 0.0, 0.0, 0.0);
        for (int k = 0; k < 90; k++) {
            double a = this.random.nextDouble() * Mth.TWO_PI;
            double up = this.random.nextDouble() * 0.6;
            this.add(ParticleTypes.END_ROD, this.center.x, this.center.y + 2.0, this.center.z, Math.cos(a) * 0.9, up, Math.sin(a) * 0.9);
        }
    }

    private static ParticleOptions trail(DragonType type) {
        return switch (type) {
            case FIRE -> ModParticles.BOSS_FLAME.get();
            case ICE -> ModParticles.BOSS_FROST.get();
            case LIGHTNING -> ModParticles.BOSS_SPARK.get();
            case NATURE -> ModParticles.BOSS_SPORE.get();
            case EARTH -> ModParticles.BOSS_DUST.get();
            default -> ModParticles.BOSS_WATER.get();
        };
    }

    // =====================================================================================
    // Werte für die Lichteffekte (IntroVisionRenderer) und den Bildschirm
    // =====================================================================================

    float columnHeight(double t) {
        if (t < this.riseStart || t >= this.seal + 0.4) {
            return 0.0f;
        }
        return (float) (6.0 + 64.0 * easeOut((t - this.riseStart) / Math.max(1.0, this.riseEnd - this.riseStart)));
    }

    float columnRadius(double t) {
        double grow = smooth((t - this.riseStart) / 3.0);
        double shrink = 1.0 - smooth((t - this.sinkStart) / Math.max(0.5, this.seal - this.sinkStart)) * 0.85;
        return (float) (4.2 * grow * shrink);
    }

    /** Wie tief die Säule schon unter die Erde gedrückt ist. */
    float columnSink(double t) {
        double k = Mth.clamp((t - this.sinkStart) / Math.max(0.5, this.seal - this.sinkStart), 0.0, 1.0);
        return (float) (k * k * 72.0);
    }

    double beams(double t) {
        if (t < this.beamStart || t > this.seal + 0.2) {
            return 0.0;
        }
        return smooth((t - this.beamStart) / 1.4);
    }

    double sphere(double t) {
        if (t < this.beamStart + 0.6 || t > this.seal) {
            return 0.0;
        }
        return Mth.clamp((t - this.beamStart - 0.6) / Math.max(0.5, this.seal - this.beamStart - 0.6), 0.0, 1.0);
    }

    double flash(double t) {
        if (t < this.seal) {
            return 0.0;
        }
        return Math.max(0.0, 1.0 - (t - this.seal) / 1.3);
    }

    /** Halbmesser der Druckwelle beim Schmieden (−1: keine). */
    double shockwave(double t) {
        double age = t - this.seal;
        return age < 0.0 || age > 3.5 ? -1.0 : age * 36.0;
    }

    /** Helligkeit des Siegels am Boden. */
    double glyph(double t) {
        if (t < this.seal - 0.1) {
            return 0.0;
        }
        return smooth((t - this.seal + 0.1) / 0.6) * (1.0 - smooth((t - this.shardEnd - 0.8) / 1.6));
    }

    private double shardLaunch(int i) {
        return this.shardStart + 0.25 * i;
    }

    private double shardArrival(int i) {
        return this.shardLaunch(i) + 1.9;
    }

    /** Leuchten des Splitters im Herzen von Drache i (0..1), nachdem er angekommen ist – bis die Drachen wild werden. */
    double heart(int i, double t) {
        double arrived = t - this.shardArrival(i);
        if (arrived < 0.0 || this.dragons[i] == null) {
            return 0.0;
        }
        double flare = Math.exp(-arrived * 2.5) * 0.8;
        double fade = 1.0 - smooth((t - this.scatterStart(i) - 0.5) / 1.5);
        return Mth.clamp((0.55 + 0.25 * Math.sin(t * 5.0 + i) + flare) * fade, 0.0, 1.2);
    }

    /** Fortschritt des Splitters zu Drache i (0..1; −1: nicht unterwegs). */
    double shard(int i, double t) {
        double k = (t - this.shardLaunch(i)) / (this.shardArrival(i) - this.shardLaunch(i));
        return k < 0.0 || k >= 1.0 ? -1.0 : k;
    }

    @Nullable Vec3 shardPosition(int i, double k, float partial) {
        Vec3 chest = this.chest(i, partial);
        if (chest == null) {
            return null;
        }
        Vec3 from = this.center.add(0.0, 0.8, 0.0);
        double e = ease(k);
        Vec3 p = from.lerp(chest, e);
        return p.add(0.0, Math.sin(e * Math.PI) * 9.0, 0.0);
    }

    double pillars(double t) {
        if (t < this.pillarsStart) {
            return 0.0;
        }
        return smooth((t - this.pillarsStart) / 2.4) * (1.0 - smooth((t - (this.length - 1.6)) / 1.5));
    }

    /** Fuß der Lichtsäule über dem fernen Nest j (fächerförmig am Horizont vor den Soldaten). */
    Vec3 pillar(int j) {
        double angle = Math.toRadians(-52.0 + j * 20.8);
        Vec3 d = rotate(this.forward, angle);
        double distance = 74.0 + (j % 2) * 9.0;
        Vec3 at = this.soldierCenter.add(d.scale(distance));
        return new Vec3(at.x, this.home.y - 12.0, at.z);
    }

    private double heartbeat(int k) {
        return this.warnStart + 0.5 + k * 0.85;
    }

    /** Sekunden seit dem k-ten Herzschlag unter der Erde (−1: noch nicht). */
    double heartbeatAge(int k, double t) {
        double age = t - this.heartbeat(k);
        return age < 0.0 ? -1.0 : age;
    }

    /** Wie dunkel das Bild ist (0..1). */
    double darkness(double t) {
        double up = smooth((t - this.riseStart) / Math.max(1.0, this.riseEnd - this.riseStart)) * 0.36
                + smooth((t - this.spreadStart) / Math.max(1.0, this.arriveStart - this.spreadStart)) * 0.22;
        double gone = smooth((t - this.seal) / 0.7);
        return up * (1.0 - gone);
    }

    /** Weißer Blitz über das ganze Bild (0..1). */
    double screenFlash(double t) {
        if (t < this.seal) {
            return 0.0;
        }
        double age = t - this.seal;
        return age < 0.08 ? age / 0.08 : Math.max(0.0, 1.0 - (age - 0.08) / 1.1);
    }

    /** Kurzes Abblenden bei harten Schnitten (0..1). */
    double dip(double t) {
        double dip = 0.0;
        for (double cut : this.cuts) {
            double d = Math.abs(t - cut);
            if (d < 0.16) {
                dip = Math.max(dip, 1.0 - d / 0.16);
            }
        }
        return dip;
    }

    /** Violettes Pochen am Bildrand (0..1). */
    double pulse(double t) {
        double p = 0.0;
        for (int k = 0; k < HEARTBEATS; k++) {
            double age = this.heartbeatAge(k, t);
            if (age >= 0.0) {
                p = Math.max(p, Math.exp(-age * 3.5));
            }
        }
        return p;
    }

    private int montageIndex(double t) {
        if (t < this.montageStart || t >= this.montageEnd) {
            return -1;
        }
        int j = (int) ((t - this.montageStart) / ((this.montageEnd - this.montageStart) / 6.0));
        return Mth.clamp(j, 0, 5);
    }

    // =====================================================================================
    // Kamera
    // =====================================================================================

    /** Ein Bild der Kamera: Standort, Blickziel und ob hier hart geschnitten wird. */
    record Shot(Vec3 position, Vec3 target, boolean cut) {}

    private record View(Vec3 position, Vec3 target) {}

    Shot camera(double t) {
        int segment;
        View view;
        if (t < this.arriveStart + 0.6) {
            segment = 0;
            view = this.darkShot(t);
        } else if (t < this.beamStart) {
            segment = 1;
            view = blend(this.darkShot(t), this.wideShot(t), smooth((t - this.arriveStart - 0.6) / 3.2));
        } else if (t < this.seal + 0.5) {
            segment = 2;
            view = blend(this.wideShot(t), this.orbitShot(t), smooth((t - this.beamStart) / 2.2));
        } else if (t < this.cuts[0]) {
            segment = 2;
            view = blend(this.orbitShot(t), this.glyphShot(t), smooth((t - this.seal - 0.5) / 1.6));
        } else if (t < this.scatter) {
            segment = 3;
            view = this.shardShot(t);
        } else if (t < this.montageStart) {
            segment = 4;
            view = blend(this.shardShot(t), this.chaseShot(FIRE, t, 1.0, 22.0, 6.0), smooth((t - this.scatter) / 1.4));
        } else if (t < this.montageEnd) {
            int j = this.montageIndex(t);
            segment = 5 + j;
            view = this.montageShot(j, t);
        } else if (t < this.warnStart) {
            segment = 11;
            view = this.homeShot(t);
        } else if (t < this.finalStart) {
            segment = 12;
            view = blend(this.homeShot(t), this.warnShot(t), smooth((t - this.warnStart) / 2.0));
        } else {
            segment = 13;
            view = blend(this.warnShot(t), this.finalShot(t), smooth((t - this.finalStart) / 2.6));
        }
        boolean cut = segment != this.lastSegment && (segment == 3 || segment >= 5 && segment <= 11);
        this.lastSegment = segment;
        // nicht im Gelände oder in Baumkronen stecken
        Vec3 pos = view.position();
        double ground = this.clearance(pos) + 2.5;
        double needed = Math.max(0.0, ground - pos.y);
        this.cameraLift = cut ? needed : this.cameraLift + (needed - this.cameraLift) * 0.3;
        return new Shot(pos.add(0.0, this.cameraLift, 0.0), view.target(), cut);
    }

    private static View blend(View a, View b, double k) {
        if (k <= 0.0) {
            return a;
        }
        if (k >= 1.0) {
            return b;
        }
        return new View(a.position().lerp(b.position(), k), a.target().lerp(b.target(), k));
    }

    /** Die Dunkelheit steigt: langsam auf die Säule zu. */
    private View darkShot(double t) {
        double k = smooth(t / Math.max(1.0, this.arriveStart + 0.6));
        double rise = this.hover - this.center.y;
        Vec3 pos = this.center.subtract(this.forward.scale(Mth.lerp(k, 74.0, 60.0))).add(this.right.scale(Mth.lerp(k, -6.0, 12.0)))
                .add(0.0, Mth.lerp(k, 9.0, 16.0), 0.0);
        Vec3 target = this.center.add(0.0, Mth.lerp(smooth((t - this.riseStart) / 6.0), 8.0, rise * 0.75), 0.0);
        return new View(pos, target);
    }

    /** Die Drachen kommen: weit und hoch, das ganze Sechseck im Bild. */
    private View wideShot(double t) {
        double rise = this.hover - this.center.y;
        double drift = (t - this.arriveStart) * 0.6;
        Vec3 pos = this.center.subtract(this.forward.scale(84.0 - drift)).add(this.right.scale(26.0)).add(0.0, rise + 22.0, 0.0);
        return new View(pos, this.center.add(0.0, rise - 5.0, 0.0));
    }

    /** Das Siegel wird geschmiedet: langsam um die Mitte herum, der Blick folgt der Lichtkugel nach unten. */
    private View orbitShot(double t) {
        double k = smooth((t - this.beamStart) / Math.max(1.0, this.seal + 0.5 - this.beamStart));
        double angle = Math.atan2(-this.forward.z, -this.forward.x) + Math.toRadians(Mth.lerp(k, 22.0, 62.0));
        double radius = Mth.lerp(k, 72.0, 58.0);
        double rise = this.hover - this.center.y;
        Vec3 pos = new Vec3(this.center.x + Math.cos(angle) * radius, this.center.y + Mth.lerp(k, rise + 14.0, rise * 0.55),
                this.center.z + Math.sin(angle) * radius);
        Vec3 focus = this.focus(1.0f);
        Vec3 target = focus == null ? this.center.add(0.0, 4.0, 0.0)
                : new Vec3(this.center.x, Math.max(this.center.y + 3.0, focus.y), this.center.z);
        return new View(pos, target);
    }

    /** Das Siegel am Boden und die sechs Splitter, die aus ihm in die Herzen der Drachen fliegen: schräg von oben. */
    private View glyphShot(double t) {
        double rise = this.hover - this.center.y;
        double k = smooth((t - this.seal) / Math.max(1.0, this.cuts[0] - this.seal));
        Vec3 pos = this.center.subtract(this.forward.scale(Mth.lerp(k, 46.0, 40.0))).add(this.right.scale(Mth.lerp(k, 18.0, 10.0)))
                .add(0.0, rise + Mth.lerp(k, 24.0, 30.0), 0.0);
        return new View(pos, this.center.add(0.0, rise * Mth.lerp(k, 0.15, 0.4), 0.0));
    }

    /** Pyrrhon aus der Nähe: der Splitter leuchtet in seinem Herzen. */
    private View shardShot(double t) {
        Vec3 chest = this.chest(FIRE, 1.0f);
        if (chest == null) {
            return this.wideShot(t);
        }
        Vec3 inward = this.dir(FIRE).scale(-1.0);
        Vec3 side = new Vec3(-inward.z, 0.0, inward.x);
        double drift = (t - this.cuts[0]) * 0.8;
        Vec3 pos = chest.add(inward.scale(19.0 - drift)).add(side.scale(9.0)).add(0.0, 2.5, 0.0);
        return new View(pos, chest.add(0.0, 1.0, 0.0));
    }

    /** Verfolgung eines Drachen von hinten-seitlich. */
    private View chaseShot(int i, double t, double sideSign, double back, double up) {
        Vec3 pos = this.dragonPosition(i, t);
        Vec3 ahead = this.dragonPosition(i, t + 0.3);
        if (pos == null || ahead == null) {
            return new View(this.center.add(0.0, 30.0, -40.0), this.center);
        }
        Vec3 velocity = ahead.subtract(pos);
        Vec3 fwd = velocity.lengthSqr() < 1.0e-6 ? this.dir(i) : new Vec3(velocity.x, 0.0, velocity.z).normalize();
        Vec3 side = new Vec3(-fwd.z, 0.0, fwd.x);
        Vec3 cam = pos.subtract(fwd.scale(back)).add(side.scale(sideSign * back * 0.45)).add(0.0, up, 0.0);
        return new View(cam, pos.add(fwd.scale(6.0)).add(0.0, 3.0, 0.0));
    }

    /** Die schnellen Schnitte: jeder Drache in seinem Element. */
    private View montageShot(int j, double t) {
        DragonType type = MONTAGE[j];
        int i = indexOf(type);
        if (i == EARTH) {
            // Terragon versinkt im Fels: Blick von schräg unten über den Boden
            Vec3 spot = this.groundSpot[EARTH];
            Vec3 out = this.dir(EARTH);
            Vec3 side = new Vec3(-out.z, 0.0, out.x);
            Vec3 pos = spot.add(out.scale(-17.0)).add(side.scale(9.0)).add(0.0, 3.5, 0.0);
            return new View(pos, spot.add(0.0, 4.0, 0.0));
        }
        double sideSign = j % 2 == 0 ? 1.0 : -1.0;
        double shotStart = this.cuts[1 + j];
        double push = (t - shotStart) * 1.5;
        return this.chaseShot(i, t, sideSign, 20.0 - push, 4.0 + (j % 3));
    }

    /** Der Auftrag: über die salutierenden Soldaten hinweg, der Blick zum Horizont. */
    private View homeShot(double t) {
        double k = smooth((t - this.homeStart) / Math.max(2.0, this.warnStart - this.homeStart));
        Vec3 s = this.soldierCenter;
        Vec3 a = s.subtract(this.forward.scale(20.0)).add(this.right.scale(6.0)).add(0.0, 16.0, 0.0);
        Vec3 b = s.subtract(this.forward.scale(7.0)).add(this.right.scale(2.0)).add(0.0, 8.0, 0.0);
        Vec3 c = s.add(this.forward.scale(3.0)).add(this.right.scale(-2.0)).add(0.0, 10.0, 0.0);
        Vec3 pos = bezier(a, b, c, k);
        Vec3 near = s.add(0.0, 1.2, 0.0);
        Vec3 far = s.add(this.forward.scale(90.0)).add(0.0, 26.0, 0.0);
        Vec3 target = near.lerp(far, smooth((k - 0.35) / 0.55));
        return new View(pos, target);
    }

    /** Die Warnung: tief über dem Boden vor den Soldaten, der Blick dorthin, wo das Siegel liegt. */
    private View warnShot(double t) {
        Vec3 s = this.soldierCenter;
        double k = smooth((t - this.warnStart) / 4.0);
        Vec3 pos = s.add(this.forward.scale(Mth.lerp(k, 7.0, 4.0))).add(this.right.scale(-2.0)).add(0.0, Mth.lerp(k, 8.0, 3.2), 0.0);
        return new View(pos, this.center.add(0.0, Mth.lerp(k, 12.0, 1.5), 0.0));
    }

    /** Zum Schluss: die Kamera steigt über das Land, Lichtsäulen am Horizont, dann der Titel. */
    private View finalShot(double t) {
        Vec3 s = this.soldierCenter;
        double rise = (t - this.finalStart) * 1.4;
        Vec3 pos = s.subtract(this.forward.scale(9.0 + rise * 0.6)).add(0.0, 9.0 + rise, 0.0);
        Vec3 target = s.add(this.forward.scale(90.0)).add(0.0, 22.0 + rise * 0.4, 0.0);
        return new View(pos, target);
    }

    // =====================================================================================
    // Aufräumen
    // =====================================================================================

    void removeAll() {
        for (int i = 0; i < this.dragons.length; i++) {
            if (this.dragons[i] != null) {
                this.level.removeEntity(this.dragons[i].getId(), Entity.RemovalReason.DISCARDED);
                this.dragons[i] = null;
            }
        }
        if (this.nyxar != null) {
            this.level.removeEntity(this.nyxar.getId(), Entity.RemovalReason.DISCARDED);
            this.nyxar = null;
        }
        if (this.vision != null) {
            this.level.removeEntity(this.vision.getId(), Entity.RemovalReason.DISCARDED);
            this.vision = null;
        }
        for (SoldierEntity soldier : this.soldiers) {
            this.level.removeEntity(soldier.getId(), Entity.RemovalReason.DISCARDED);
        }
        this.soldiers.clear();
        if (active == this) {
            active = null;
        }
    }

    // =====================================================================================
    // Mathematik
    // =====================================================================================

    static double smooth(double v) {
        double c = Mth.clamp(v, 0.0, 1.0);
        return c * c * (3.0 - 2.0 * c);
    }

    private static double ease(double v) {
        double c = Mth.clamp(v, 0.0, 1.0);
        return c < 0.5 ? 4.0 * c * c * c : 1.0 - Math.pow(-2.0 * c + 2.0, 3.0) / 2.0;
    }

    private static double easeOut(double v) {
        double c = Mth.clamp(v, 0.0, 1.0);
        return 1.0 - (1.0 - c) * (1.0 - c) * (1.0 - c);
    }

    private static Vec3 bezier(Vec3 a, Vec3 b, Vec3 c, double u) {
        double v = 1.0 - u;
        return a.scale(v * v).add(b.scale(2.0 * v * u)).add(c.scale(u * u));
    }

    private static Vec3 rotate(Vec3 v, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return new Vec3(v.x * cos - v.z * sin, v.y, v.x * sin + v.z * cos);
    }

    /** Blickwinkel (Grad, wie bei Minecraft) für eine Richtung. */
    private static float yawOf(Vec3 direction) {
        return (float) Math.toDegrees(Math.atan2(-direction.x, direction.z));
    }
}
