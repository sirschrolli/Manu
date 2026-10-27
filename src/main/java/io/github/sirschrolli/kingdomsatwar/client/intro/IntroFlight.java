package io.github.sirschrolli.kingdomsatwar.client.intro;

import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.entity.dragon.DragonEntity;
import io.github.sirschrolli.kingdomsatwar.entity.dragon.DragonStage;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Die Kamerafahrt des Intros: Ein uralter Feuerdrache fliegt in einem weiten Bogen über die Landschaft,
 * die Kamera folgt ihm. Im Drachen-Teil stoßen die fünf anderen Drachenarten in Keilformation dazu, dann
 * stürzt sich der Drache tief über das Land, überfliegt eine Abteilung salutierender Soldaten und steigt
 * zum Schluss in den Himmel, während die Kamera stehen bleibt und ihm nachsieht.
 *
 * <p>Alles existiert nur auf dem eigenen Bildschirm. Die Flughöhe passt sich dem Gelände an
 * (der Drache steigt rechtzeitig vor Bergen und Bäumen), ebenso die Kamera.
 */
final class IntroFlight {
    private static final int FIRST_DRAGON_ID = -2_100_000_000;
    private static final int FIRST_SOLDIER_ID = -2_050_000_000;
    private static final DragonType[] ESCORT = {DragonType.ICE, DragonType.LIGHTNING, DragonType.NATURE, DragonType.EARTH, DragonType.WATER};
    /** Keilformation hinter dem Leitdrachen: seitlich, zurück, höher. */
    private static final double[][] FORMATION = {{-6, 7, 1}, {6, 7, 1.5}, {-11, 14, 0}, {11, 14, 0.5}, {0, 18, 2.5}};

    private final ClientLevel level;
    private final Vec3 home;
    private final double startAngle;
    private final Keys path;
    private final Keys camera;
    private final double dragonsStart;
    private final double wonderStart;
    private final double knightsStart;
    private final double finaleStart;
    private final double length;

    private @Nullable DragonEntity leader;
    private final List<DragonEntity> escort = new ArrayList<>();
    private final List<SoldierEntity> soldiers = new ArrayList<>();
    private Vec3 soldierCenter = Vec3.ZERO;
    private double lift;
    private double cameraLift;
    private boolean saluted;

    IntroFlight(ClientLevel level, Vec3 home, float playerYaw, IntroScript script) {
        this.level = level;
        this.home = home;
        this.length = script.length();
        this.dragonsStart = script.section("dragons", 20.0);
        this.wonderStart = script.section("wonder", 36.667);
        this.knightsStart = script.section("knights", 53.333);
        this.finaleStart = script.section("finale", 60.0);
        // der Flug beginnt dort, wohin der Spieler schaut
        this.startAngle = playerYaw + 90.0;
        double w = script.section("world", 6.667);
        double d = this.dragonsStart;
        double o = this.wonderStart;
        double k = this.knightsStart;
        double f = this.finaleStart;
        double end = this.length;
        // Flugbahn: Zeit, Winkel um den Spieler (Grad), Abstand, Höhe über dem Spieler
        this.path = new Keys(new double[][] {
                {0, 0, 62, 30}, {w, 18, 60, 28}, {(w + d) / 2, 45, 54, 26}, {d, 68, 50, 25},
                {(d + o) / 2, 108, 46, 24}, {o, 145, 42, 20}, {o + (k - o) * 0.35, 175, 32, 10},
                {o + (k - o) * 0.75, 205, 24, 8}, {k, 228, 18, 8}, {f, 262, 22, 14},
                {f + (end - f) * 0.45, 290, 46, 30}, {end, 312, 72, 52}});
        // Kamera relativ zum Drachen: Zeit, zurück, seitlich, hoch
        this.camera = new Keys(new double[][] {
                {0, 17, 5, 7}, {w, 15, 3, 5}, {d - 2, 13, -6, 5}, {d + 4, 4, -36, 6}, {o - 4, 6, -34, 7},
                {o + 1, 12, 2, 4}, {o + (k - o) * 0.5, 9, 3, 2.5}, {k - 2, 10, 2, 4}, {k + 1, 9, 6, 5},
                {f, 12, 4, 6}, {end, 12, 4, 6}});
    }

    // =====================================================================================
    // Bahn
    // =====================================================================================

    /** Position des Leitdrachen ohne Geländeausgleich. */
    private Vec3 basePosition(double t) {
        double[] k = this.path.at(t);
        double angle = Math.toRadians(this.startAngle + k[0]);
        return new Vec3(this.home.x + Math.cos(angle) * k[1], this.home.y + k[2], this.home.z + Math.sin(angle) * k[1]);
    }

    /** Wie hoch muss der Drache an dieser Stelle mindestens fliegen? (Gelände rundum + Abstand) */
    private double safeHeight(Vec3 pos, double clearance) {
        int max = this.level.getMinY();
        int x0 = Mth.floor(pos.x);
        int z0 = Mth.floor(pos.z);
        for (int dx = -6; dx <= 6; dx += 3) {
            for (int dz = -6; dz <= 6; dz += 3) {
                max = Math.max(max, this.level.getHeight(Heightmap.Types.MOTION_BLOCKING, x0 + dx, z0 + dz));
            }
        }
        return max + clearance;
    }

    private Vec3 leaderPosition(double t) {
        return this.basePosition(t).add(0.0, this.lift, 0.0);
    }

    private Vec3 direction(double t) {
        Vec3 a = this.basePosition(t - 0.2);
        Vec3 b = this.basePosition(t + 0.2);
        Vec3 d = b.subtract(a);
        double horizontal = Math.sqrt(d.x * d.x + d.z * d.z);
        return horizontal < 1.0e-4 ? new Vec3(1, 0, 0) : new Vec3(d.x / horizontal, 0.0, d.z / horizontal);
    }

    /**
     * Ein Schritt (1 Tick): Geländeausgleich nachführen – vorausschauend schnell steigen, langsam sinken –,
     * Drachen, Begleiter und Soldaten bewegen.
     */
    void tick(double t) {
        double needed = 0.0;
        for (double ahead = 0.0; ahead <= 3.0; ahead += 0.5) {
            Vec3 base = this.basePosition(t + ahead);
            needed = Math.max(needed, this.safeHeight(base, 8.0) - base.y);
        }
        this.lift = needed > this.lift ? Math.min(needed, this.lift + 0.6) : Math.max(needed, this.lift - 0.08);
        this.updateLeader(t);
        this.updateEscort(t);
        this.updateSoldiers(t);
    }

    private void updateLeader(double t) {
        if (this.leader == null) {
            this.leader = this.spawnDragon(DragonType.FIRE, DragonStage.ELDER, 0, this.leaderPosition(t));
        }
        this.moveDragon(this.leader, t, this.leaderPosition(t), this.leaderPosition(t + 0.25));
    }

    private void moveDragon(DragonEntity dragon, double t, Vec3 position, Vec3 ahead) {
        Vec3 velocity = ahead.subtract(position);
        double horizontal = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
        float yaw = (float) Math.toDegrees(Math.atan2(-velocity.x, velocity.z));
        float pitch = (float) Mth.clamp(-Math.toDegrees(Math.atan2(velocity.y, Math.max(horizontal, 1.0e-3))), -25.0, 25.0);
        dragon.moveOrInterpolateTo(position, yaw, pitch);
        dragon.setYBodyRot(yaw);
        dragon.setYHeadRot(yaw);
        dragon.setDeltaMovement(velocity.scale(0.2)); // nur für die Animation (Gleiten beim Sinken)
    }

    /** Die fünf anderen Drachenarten: fliegen heran, halten die Keilformation und drehen wieder ab. */
    private void updateEscort(double t) {
        double join = this.dragonsStart - 1.0;
        double leave = this.wonderStart - 2.0;
        if (t < join || t > leave + 5.0) {
            this.removeEscort();
            return;
        }
        if (this.escort.isEmpty()) {
            for (int i = 0; i < ESCORT.length; i++) {
                this.escort.add(this.spawnDragon(ESCORT[i], DragonStage.ADULT, i + 1, this.leaderPosition(t)));
            }
        }
        for (int i = 0; i < this.escort.size(); i++) {
            Vec3 now = this.escortPosition(t, FORMATION[i], i, join, leave);
            Vec3 next = this.escortPosition(t + 0.25, FORMATION[i], i, join, leave);
            this.moveDragon(this.escort.get(i), t, now, next);
        }
    }

    /** Anflug von weit oben und außen, Formation, zum Schluss nach vorn oben davon (nicht an der Kamera vorbei). */
    private Vec3 escortPosition(double t, double[] slot, int i, double join, double leave) {
        double in = 1.0 - smooth((t - join - i * 0.4) / 5.0);
        double out = smooth((t - leave - i * 0.5) / 5.0);
        double lag = slot[1] / 9.0;
        Vec3 base = this.leaderPosition(t - lag);
        Vec3 forward = this.direction(t - lag);
        Vec3 right = new Vec3(-forward.z, 0.0, forward.x);
        double bob = Math.sin(t * 0.9 + i * 1.7) * 0.8;
        double spread = 1.0 + in * 3.5 + out * 0.6;
        double rise = in * 25.0 + out * 38.0;
        return base.add(right.scale(slot[0] * spread)).add(forward.scale(out * (30.0 + i * 6.0)))
                .add(0.0, slot[2] + rise + bob, 0.0);
    }

    /** Eine kleine Abteilung Soldaten nahe beim Spieler, die salutiert, wenn der Drache über sie hinwegfliegt. */
    private void updateSoldiers(double t) {
        double appear = this.wonderStart + (this.knightsStart - this.wonderStart) * 0.6;
        if (t < appear || t > this.finaleStart + 6.0) {
            this.removeSoldiers();
            return;
        }
        if (this.soldiers.isEmpty()) {
            this.spawnSoldiers();
        }
        if (!this.saluted && t >= this.knightsStart + 0.8) {
            this.saluted = true;
            for (SoldierEntity soldier : this.soldiers) {
                soldier.handleEntityEvent(soldier.getSoldierType() == SoldierType.CAPTAIN ? SoldierEntity.EVENT_CHEER : SoldierEntity.EVENT_SALUTE);
            }
        }
    }

    private void spawnSoldiers() {
        Vec3 over = this.basePosition(this.knightsStart);
        Vec3 from = this.basePosition(this.knightsStart - 3.0);
        // Aufstellung unter dem Überflug, Blick dem Drachen entgegen
        this.soldierCenter = new Vec3(over.x, this.home.y, over.z);
        Vec3 facing = new Vec3(from.x - over.x, 0.0, from.z - over.z).normalize();
        Vec3 right = new Vec3(-facing.z, 0.0, facing.x);
        float yaw = (float) Math.toDegrees(Math.atan2(-facing.x, facing.z));
        SoldierType[][] rows = {
                {SoldierType.CAPTAIN},
                {SoldierType.KNIGHT, SoldierType.SWORDSMAN, SoldierType.SWORDSMAN, SoldierType.KNIGHT},
                {SoldierType.ARCHER, SoldierType.ARCHER, SoldierType.ARCHER, SoldierType.ARCHER}};
        int id = 0;
        for (int row = 0; row < rows.length; row++) {
            for (int col = 0; col < rows[row].length; col++) {
                double side = (col - (rows[row].length - 1) / 2.0) * 1.8;
                Vec3 spot = this.soldierCenter.add(facing.scale(2.5 - row * 2.0)).add(right.scale(side));
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
    }

    private DragonEntity spawnDragon(DragonType type, DragonStage stage, int index, Vec3 position) {
        DragonEntity dragon = ModEntities.DRAGONS.get(type).get().create(this.level, EntitySpawnReason.LOAD);
        dragon.setId(FIRST_DRAGON_ID - index);
        dragon.applyStage(stage, false);
        dragon.setFlying(true);
        dragon.setSilent(true);
        dragon.snapTo(position.x, position.y, position.z, 0.0f, 0.0f);
        this.level.addEntity(dragon);
        return dragon;
    }

    // =====================================================================================
    // Kamera
    // =====================================================================================

    /** Kamera: [Position, Blickziel] für den Zeitpunkt t. */
    Vec3[] camera(double t, boolean snap) {
        Vec3 dragon = this.leaderPosition(t);
        Vec3 forward = this.direction(t);
        Vec3 right = new Vec3(-forward.z, 0.0, forward.x);
        double[] c = this.camera.at(t);
        Vec3 chase = dragon.subtract(forward.scale(c[0])).add(right.scale(c[1])).add(0.0, c[2], 0.0);
        Vec3 target = dragon.add(forward.scale(4.0)).add(0.0, 1.5, 0.0);
        // solange die anderen Drachen dabei sind: Blick auf die Mitte der Formation
        double formation = smooth((t - this.dragonsStart) / 3.0) * (1.0 - smooth((t - (this.wonderStart - 3.0)) / 3.0));
        if (formation > 0.0) {
            target = target.subtract(forward.scale(13.0 * formation)).add(0.0, 1.5 * formation, 0.0);
        }
        // über den Soldaten: Blick halb auf die Abteilung
        double troops = smooth((t - (this.knightsStart - 2.5)) / 2.0) * (1.0 - smooth((t - (this.knightsStart + 3.5)) / 2.0));
        if (troops > 0.0 && !this.soldiers.isEmpty()) {
            target = target.lerp(this.soldierCenter.add(0.0, 1.0, 0.0), troops * 0.7);
        }
        // Finale: die Kamera bleibt über dem Land stehen, steigt langsam und sieht dem Drachen nach
        double hold = smooth((t - this.finaleStart) / 4.0);
        if (hold > 0.0) {
            Vec3 vantage = this.vantage(t);
            chase = chase.lerp(vantage, hold);
        }
        // nicht im Gelände stecken
        double ground = this.safeHeight(chase, 3.0);
        double neededLift = Math.max(0.0, ground - chase.y);
        this.cameraLift = snap ? neededLift : this.cameraLift + (neededLift - this.cameraLift) * 0.3;
        chase = chase.add(0.0, this.cameraLift, 0.0);
        return new Vec3[] {chase, target};
    }

    /** Standpunkt im Finale: über dem Platz der Soldaten, langsam steigend. */
    private Vec3 vantage(double t) {
        Vec3 at = this.basePosition(this.finaleStart).add(0.0, this.lift, 0.0);
        Vec3 forward = this.direction(this.finaleStart);
        double rise = (t - this.finaleStart) * 0.6;
        return at.subtract(forward.scale(14.0)).add(0.0, 6.0 + rise, 0.0);
    }

    // =====================================================================================
    // Aufräumen
    // =====================================================================================

    void removeAll() {
        if (this.leader != null) {
            this.level.removeEntity(this.leader.getId(), Entity.RemovalReason.DISCARDED);
            this.leader = null;
        }
        this.removeEscort();
        this.removeSoldiers();
    }

    private void removeEscort() {
        for (DragonEntity dragon : this.escort) {
            this.level.removeEntity(dragon.getId(), Entity.RemovalReason.DISCARDED);
        }
        this.escort.clear();
    }

    private void removeSoldiers() {
        for (SoldierEntity soldier : this.soldiers) {
            this.level.removeEntity(soldier.getId(), Entity.RemovalReason.DISCARDED);
        }
        this.soldiers.clear();
    }

    static double smooth(double v) {
        double c = Mth.clamp(v, 0.0, 1.0);
        return c * c * (3.0 - 2.0 * c);
    }

    /** Schlüsselbilder mit weichen Übergängen (kubische Hermite-Kurve, Steigungen nach Catmull-Rom). */
    private static final class Keys {
        private final double[][] keys;

        Keys(double[][] keys) {
            this.keys = keys;
        }

        double[] at(double t) {
            int n = this.keys.length;
            int channels = this.keys[0].length - 1;
            if (t <= this.keys[0][0]) {
                return java.util.Arrays.copyOfRange(this.keys[0], 1, channels + 1);
            }
            if (t >= this.keys[n - 1][0]) {
                return java.util.Arrays.copyOfRange(this.keys[n - 1], 1, channels + 1);
            }
            int i = 0;
            while (t > this.keys[i + 1][0]) {
                i++;
            }
            double[] p0 = this.keys[Math.max(0, i - 1)];
            double[] p1 = this.keys[i];
            double[] p2 = this.keys[i + 1];
            double[] p3 = this.keys[Math.min(n - 1, i + 2)];
            double span = p2[0] - p1[0];
            double u = (t - p1[0]) / span;
            double h00 = 2 * u * u * u - 3 * u * u + 1;
            double h10 = u * u * u - 2 * u * u + u;
            double h01 = -2 * u * u * u + 3 * u * u;
            double h11 = u * u * u - u * u;
            double[] out = new double[channels];
            for (int c = 1; c <= channels; c++) {
                double m1 = (p2[c] - p0[c]) / Math.max(1.0e-6, p2[0] - p0[0]) * span;
                double m2 = (p3[c] - p1[c]) / Math.max(1.0e-6, p3[0] - p1[0]) * span;
                out[c - 1] = h00 * p1[c] + h10 * m1 + h01 * p2[c] + h11 * m2;
            }
            return out;
        }
    }
}
