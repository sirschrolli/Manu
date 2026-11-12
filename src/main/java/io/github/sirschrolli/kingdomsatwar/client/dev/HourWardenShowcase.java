package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.abyss.AbyssGate;
import io.github.sirschrolli.kingdomsatwar.block.abyss.ClockHeartBlockEntity;
import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.entity.abyss.ClockGearEntity;
import io.github.sirschrolli.kingdomsatwar.entity.abyss.ClockPendulumEntity;
import io.github.sirschrolli.kingdomsatwar.entity.abyss.HourWardenEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.magic.fx.SpellFxEntity;
import io.github.sirschrolli.kingdomsatwar.registry.AbyssGear;
import io.github.sirschrolli.kingdomsatwar.registry.ClockTower;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModTags;
import io.github.sirschrolli.kingdomsatwar.worldgen.clock.ClockTowerPiece;
import io.github.sirschrolli.kingdomsatwar.worldgen.clock.ClockTowerStructure;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures;
import java.util.Deque;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Rotations;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Vorführung ({@code hour_warden}): der Uhrturm im Abgrund – von oben, vom Zifferblatt zum Turm, von der Seite, die
 * abgebrochene Turmspitze, Uhrwerkkammer und Glockenstube, das Uhrwerkherz, die schwebenden Zahnräder und der Kegel mit den
 * Uhrgewichten – und der Stundenwächter: erstarrt über dem Herzen, sein Erwachen, die Bossleiste, alle Angriffe gegen einen
 * (zähen) Ritter, Rückspulen und Taumeln, die Zwölfte Stunde, sein Tod und das stillstehende Herz; dazu die Rüstung der
 * Zeitwächter mit dem Minutenzeiger.
 *
 * <p>Lage auf dem Zifferblatt: {@code along} zählt nach Süden (der Turm steht im Norden), {@code side} nach Osten, {@code up}
 * über dem Zifferblatt.
 */
final class HourWardenShowcase {
    private static final String TAG = "kaw_clock_show";

    private HourWardenShowcase() {}

    private static final class Dial {
        BlockPos center;
        int warden = -1;
        int knight = -1;
        boolean ready;
    }

    private static ServerLevel abyss(ServerLevel any) {
        return any.getServer().getLevel(AbyssGate.THE_ABYSS);
    }

    static void boss(Deque<Step> steps) {
        Dial g = new Dial();
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> {
            minecraft.options.renderDistance().set(12);
            minecraft.options.gamma().set(1.0);
        }));
        steps.add(Step.command("gamerule spawn_monsters false"));
        steps.add(Step.command("gamerule spawn_mobs false"));
        steps.add(Step.command("difficulty normal"));
        steps.add(Step.command("gamemode spectator"));
        steps.add(Step.server(any -> locate(abyss(any), g)));
        steps.add(Step.until(() -> g.ready, 12000));
        steps.add(Step.server(any -> clean(abyss(any), g)));
        int tower = ClockTowerPiece.TOWER_Z;
        // der Uhrturm: von oben, vom Zifferblatt, von der Seite, die Spitze, innen, das Herz, Zahnräder, von unten
        boolean first = true;
        for (Object[] view : new Object[][] {
                {"clock_aerial", new double[] {24.0, 27.0, 15.0, -9.0, 2.0, 0.0}},
                {"clock_dial_top", new double[] {9.0, 21.0, 2.0, -3.0, 0.0, 0.0}},
                {"clock_dial", new double[] {13.0, 4.5, 6.0, tower, 14.0, 0.0}},
                {"clock_tower_side", new double[] {-12.0, 13.0, -25.0, tower, 18.0, 0.0}},
                {"clock_tower_top", new double[] {tower + 16.0, ClockTowerPiece.ROOF + 10.0, 14.0, tower, ClockTowerPiece.ROOF + 5.0, 0.0}},
                {"clock_gear_room", new double[] {tower - 3.5, ClockTowerPiece.GEAR_ROOM + 2.2, 3.5, tower + 1.0, ClockTowerPiece.GEAR_ROOM + 2.0, -1.0}},
                {"clock_bell_room", new double[] {tower + 3.6, ClockTowerPiece.BELL_ROOM + 2.0, -3.6, tower, ClockTowerPiece.ROOF - 4.0, 0.0}},
                {"clock_heart", new double[] {3.6, 2.4, 2.4, 0.0, -0.6, 0.0}},
                {"clock_gears", new double[] {22.0, -3.0, -21.0, 0.0, 3.0, 0.0}},
                {"clock_below", new double[] {16.0, -15.0, 12.0, 0.0, -9.0, 0.0}}}) {
            String name = (String) view[0];
            double[] v = (double[]) view[1];
            if (!Step.wanted(name)) {
                continue;
            }
            steps.add(look(g, v[0], v[1], v[2], v[3], v[4], v[5]));
            shot(steps, name, first);
            first = false;
        }

        // der Wächter erstarrt über dem Herzen
        steps.add(Step.server(any -> {
            ServerLevel level = abyss(any);
            if (level != null && level.getBlockEntity(ClockTowerPiece.heart(g.center)) instanceof ClockHeartBlockEntity heart) {
                heart.reset(level);
                HourWardenEntity warden = heart.spawn(level, false);
                if (warden != null) {
                    g.warden = warden.getId();
                }
            }
        }));
        steps.add(look(g, 8.5, 3.2, 3.5, 0.0, 2.6, 0.0));
        steps.add(Step.waitTicks(first ? 120 : 60));
        if (first) {
            steps.add(Step.waitForChunks(1500));
        }
        steps.add(Step.screenshot("warden_dormant"));
        steps.add(look(g, 3.4, 4.2, 1.2, 0.0, 4.0, 0.0));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screenshot("warden_dormant_close"));
        // er erwacht: die Glocke schlägt
        steps.add(Step.server(any -> {
            if (abyss(any).getEntity(g.warden) instanceof HourWardenEntity warden) {
                warden.awaken();
            }
        }));
        steps.add(look(g, 9.0, 3.0, 3.0, 0.0, 2.6, 0.0));
        steps.add(attackAt(g, HourWardenEntity.Attack.AWAKEN, 20));
        steps.add(Step.screenshot("warden_awaken_wind"));
        steps.add(attackAt(g, HourWardenEntity.Attack.AWAKEN, 39));
        steps.add(Step.screenshot("warden_awaken"));
        steps.add(Step.until(() -> {
            var minecraft = net.minecraft.client.Minecraft.getInstance();
            return minecraft.level != null && minecraft.level.getEntity(g.warden) instanceof HourWardenEntity warden
                    && warden.currentAttack() != HourWardenEntity.Attack.AWAKEN;
        }, 200));
        // ein Ritter als Gegner
        steps.add(Step.server(any -> {
            ServerLevel level = abyss(any);
            Mob knight = ModEntities.SOLDIERS.get(SoldierType.KNIGHT).get().create(level, EntitySpawnReason.EVENT);
            if (knight == null) {
                return;
            }
            Vec3 p = rel(g, 2.0, 0.0, 1.0);
            knight.snapTo(p.x, p.y, p.z, 90.0f, 0.0f);
            knight.goalSelector.removeAllGoals(goal -> true);
            knight.targetSelector.removeAllGoals(goal -> true);
            knight.setSilent(true);
            knight.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 1_000_000, 4, false, false));
            knight.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(net.minecraft.world.item.Items.IRON_SWORD));
            knight.addTag(TAG);
            level.addFreshEntity(knight);
            g.knight = knight.getId();
        }));
        steps.add(stage(g, 6.0, 1.0f));
        steps.add(Step.hud(true));
        steps.add(follow(g, 40.0, 9.0, 2.0, 0.3));
        steps.add(Step.waitTicks(40));
        steps.add(follow(g, 40.0, 9.0, 2.0, 0.3));
        steps.add(Step.waitTicks(4));
        steps.add(Step.screenshot("warden_bossbar"));
        steps.add(Step.hud(false));

        // die Angriffe: {Angriff, Tick, Name, Winkel (P: quer zum Pendel), Abstand, Höhe, Blick zum Ritter (beim Pendel: Höhe des
        // Blicks), Abstand des Ritters, Kraft, Beginn (F: erzwingen, S: Rückspulen und hart treffen, Z: die Zwölfte Stunde)}
        java.util.Set<String> started = new java.util.HashSet<>();
        for (String[] shot : new String[][] {
                {"SWEEP", "9", "warden_sweep_wind", "65", "7.5", "1.6", "0.4", "3.6", "1", "F"},
                {"SWEEP", "13", "warden_sweep", "60", "7.5", "1.6", "0.4", "3.6", "1", "F"},
                {"STRIKE", "15", "warden_strike_raise", "70", "8.0", "1.8", "0.5", "3.2", "1", "F"},
                {"STRIKE", "22", "warden_strike", "55", "9.0", "2.4", "0.5", "3.2", "1", "F"},
                {"GEARS", "22", "warden_gears", "40", "10.0", "3.0", "0.5", "10.0", "1", "F"},
                {"GEARS", "34", "warden_gears_roll", "55", "9.0", "2.6", "0.7", "10.0", "1", "F"},
                {"TIME_STOP", "24", "warden_timestop_charge", "55", "8.0", "1.8", "0.3", "6.0", "1", "F"},
                {"TIME_STOP", "37", "warden_timestop", "35", "10.0", "3.0", "0.5", "6.0", "1", "F"},
                {"TIME_STOP", "53", "warden_timestop_strike", "80", "7.0", "1.8", "0.7", "6.0", "1", "F"},
                {"PENDULUM", "24", "warden_pendulum_appear", "P", "22.0", "4.0", "7.0", "7.0", "1", "F"},
                {"PENDULUM", "41", "warden_pendulum", "P", "14.0", "2.0", "4.5", "7.0", "1", "F"},
                {"HOURGLASS", "36", "warden_hourglass", "40", "10.0", "4.0", "0.6", "5.0", "1", "F"},
                {"BLINK", "9", "warden_blink", "60", "8.0", "2.0", "0.6", "6.0", "1", "F"},
                {"REWIND", "34", "warden_rewind", "25", "6.5", "1.2", "0.0", "8.0", "0.6", "F"},
                {"STAGGER", "12", "warden_stagger", "45", "7.0", "1.6", "0.0", "8.0", "0.62", "S"},
                {"FURY", "16", "warden_fury", "35", "8.0", "1.4", "0.0", "7.0", "0.45", "F"},
                {"TOLLS", "34", "warden_tolls", "45", "14.0", "6.0", "0.2", "7.0", "0.45", "Z"}}) {
            if (!Step.wanted(shot[2])) {
                continue;
            }
            HourWardenEntity.Attack attack = HourWardenEntity.Attack.valueOf(shot[0]);
            int tick = Integer.parseInt(shot[1]);
            String mode = shot[9];
            // die Glockenschläge folgen der Zwölften Stunde von selbst
            boolean begin = started.add(shot[0]) && !(mode.equals("Z") && started.contains("FURY"));
            if (begin) {
                steps.add(stage(g, Double.parseDouble(shot[7]), Float.parseFloat(shot[8])));
                steps.add(Step.waitTicks(2));
                HourWardenEntity.Attack opening = switch (mode) {
                    case "S" -> HourWardenEntity.Attack.REWIND;
                    case "Z" -> HourWardenEntity.Attack.FURY;
                    default -> attack;
                };
                steps.add(Step.server(any -> {
                    if (abyss(any).getEntity(g.warden) instanceof HourWardenEntity warden) {
                        warden.forceAttack(opening);
                    }
                }));
                if (mode.equals("S")) {
                    // Taumeln wie im Kampf: beim Rückspulen hart getroffen
                    steps.add(Step.waitTicks(16));
                    steps.add(Step.server(any -> {
                        ServerLevel level = abyss(any);
                        if (level.getEntity(g.warden) instanceof HourWardenEntity warden) {
                            warden.damageCooldownTime = 0;
                            warden.hurtServer(level, level.damageSources().magic(), warden.getMaxHealth() * 0.08f
                                    / io.github.sirschrolli.kingdomsatwar.entity.boss.BossHealth.scale(warden));
                            KingdomsAtWar.LOGGER.info("[Showcase] Rückspulen unterbrochen: {}", warden.currentAttack());
                        }
                    }));
                }
            }
            steps.add(attackAt(g, attack, tick - 3));
            if (shot[3].equals("P")) {
                steps.add(pendulumView(g, Double.parseDouble(shot[4]), Double.parseDouble(shot[5]), Double.parseDouble(shot[6])));
            } else {
                steps.add(follow(g, Double.parseDouble(shot[3]), Double.parseDouble(shot[4]), Double.parseDouble(shot[5]),
                        Double.parseDouble(shot[6])));
            }
            steps.add(Step.waitTicks(1));
            steps.add(Step.screenshot(shot[2]));
        }
        // besiegt: er sinkt auf die Knie, dann zerspringt er
        steps.add(Step.waitTicks(20));
        steps.add(Step.server(any -> {
            ServerLevel level = abyss(any);
            for (Entity e : level.getEntitiesOfClass(Entity.class, new AABB(g.center).inflate(48, 40, 48),
                    e -> e instanceof SpellFxEntity || e instanceof ClockGearEntity || e instanceof ClockPendulumEntity || e.getId() == g.knight)) {
                e.discard();
            }
            if (level.getEntity(g.warden) instanceof HourWardenEntity warden) {
                Vec3 at = rel(g, 3.0, 0.0, 0.0);
                warden.forceAttack(HourWardenEntity.Attack.NONE);
                warden.snapTo(at.x, at.y, at.z, 0.0f, 0.0f);
                warden.setYBodyRot(0.0f);
                warden.setYHeadRot(0.0f);
                warden.setDeltaMovement(Vec3.ZERO);
                warden.setInvulnerableTime(0);
                warden.setHealth(1.0f);
                warden.hurtServer(level, level.damageSources().genericKill(), 1000.0f);
            }
        }));
        steps.add(look(g, 10.0, 2.6, 3.0, 3.0, 1.6, 0.0));
        steps.add(Step.waitTicks(45));
        steps.add(Step.screenshot("warden_death"));
        steps.add(Step.waitTicks(HourWardenEntity.DEATH_TICKS - 49));
        steps.add(Step.screenshot("warden_shatter"));
        steps.add(Step.waitTicks(40));
        steps.add(Step.server(any -> {
            for (ItemEntity item : abyss(any).getEntitiesOfClass(ItemEntity.class, new AABB(g.center).inflate(30, 30, 30))) {
                item.discard();
            }
        }));
        steps.add(look(g, 3.6, 2.4, 2.4, 0.0, -0.6, 0.0));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("clock_heart_stopped"));
        // die Rüstung der Zeitwächter mit dem Minutenzeiger am Rüstungsständer
        steps.add(Step.server(any -> {
            ServerLevel level = abyss(any);
            Vec3 p = rel(g, 6.0, 0.0, -5.0);
            ArmorStand stand = new ArmorStand(level, p.x, p.y, p.z);
            stand.setShowArms(true);
            stand.setNoBasePlate(true);
            stand.setNoGravity(true);
            stand.setRightArmPose(new Rotations(-20, 0, 8));
            Vec3 cam = rel(g, 9.6, 1.9, -2.6);
            float yaw = (float) Math.toDegrees(Math.atan2(-(cam.x - p.x), cam.z - p.z)) + 20.0f;
            stand.setYRot(yaw);
            stand.setYBodyRot(yaw);
            stand.setYHeadRot(yaw);
            List<EquipmentSlot> slots = List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);
            var set = AbyssGear.timewardenSet();
            for (int i = 0; i < slots.size(); i++) {
                stand.setItemSlot(slots.get(i), new ItemStack(set.get(i).get()));
            }
            stand.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ClockTower.MINUTE_HAND.get()));
            stand.addTag(TAG);
            level.addFreshEntity(stand);
        }));
        steps.add(look(g, 9.6, 1.9, -2.6, 6.0, 1.1, -5.0));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("timewarden_minute_hand"));
        steps.add(look(g, 7.6, 1.6, -3.6, 6.0, 1.3, -5.0));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screenshot("timewarden_minute_hand_close"));
        steps.add(Step.server(any -> {
            for (Entity e : abyss(any).getEntitiesOfClass(Entity.class, new AABB(g.center).inflate(48, 40, 48), e -> e.entityTags().contains(TAG))) {
                e.discard();
            }
        }));
    }

    private static void shot(Deque<Step> steps, String name, boolean first) {
        if (first) {
            steps.add(Step.waitTicks(60));
            steps.add(Step.waitForChunks(1500));
        }
        steps.add(Step.waitTicks(first ? 60 : 30));
        steps.add(Step.screenshot(name));
    }

    /** Sucht den nächsten Uhrturm (notfalls baut er selbst einen) und lässt seine Chunks entstehen. */
    private static void locate(ServerLevel level, Dial g) {
        if (level == null) {
            g.ready = true;
            return;
        }
        var structure = level.registryAccess().lookupOrThrow(Registries.STRUCTURE).getOrThrow(ModStructures.CLOCK_TOWER_STRUCTURE);
        var found = level.getChunkSource().getGenerator().findNearestMapStructure(level, HolderSet.direct(structure), new BlockPos(0, 110, 0), 60,
                false);
        if (found != null) {
            BlockPos pos = found.getFirst();
            var chunk = level.getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.STRUCTURE_STARTS);
            StructureStart start = chunk.getStartForStructure(structure.value());
            if (start != null && start.isValid() && start.getPieces().getFirst() instanceof ClockTowerPiece piece) {
                g.center = piece.center();
            }
        }
        if (g.center == null) {
            build(level, g);
        }
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                level.setChunkForced((g.center.getX() >> 4) + dx, (g.center.getZ() >> 4) + dz, true);
                level.getChunk((g.center.getX() >> 4) + dx, (g.center.getZ() >> 4) + dz);
            }
        }
        KingdomsAtWar.LOGGER.info("[Showcase] Uhrturm bei {} ({})", g.center, level.getBlockState(ClockTowerPiece.heart(g.center)));
        g.ready = true;
    }

    /** Notfalls: einen Platz im Abgrund suchen (wie beim Erzeugen der Welt) und dort selbst bauen. */
    private static void build(ServerLevel level, Dial g) {
        var generator = level.getChunkSource().getGenerator();
        var biomes = generator.getBiomeSource().createUncachedResolver(level.getChunkSource().randomState());
        var tagged = level.registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(ModTags.Biomes.HAS_CLOCK_TOWER);
        BlockPos spot = ClockTowerStructure.site(biomes, tagged::contains, new ChunkPos(40, 40), 1_234_567L);
        if (spot == null) {
            spot = new BlockPos(648, 112, 648);
        }
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                level.getChunk((spot.getX() >> 4) + dx, (spot.getZ() >> 4) + dz);
            }
        }
        long seed = 31_337L;
        ClockTowerPiece.build(level, ClockTowerPiece.makeBox(spot), spot, seed, net.minecraft.util.RandomSource.create(seed), null);
        g.center = spot;
        KingdomsAtWar.LOGGER.info("[Showcase] kein Uhrturm in Reichweite – selbst gebaut bei {}", spot);
    }

    /** Fremde Wesen fort, damit nichts durchs Bild läuft. */
    private static void clean(ServerLevel level, Dial g) {
        if (level == null || g.center == null) {
            return;
        }
        for (Entity e : level.getEntitiesOfClass(Entity.class, new AABB(g.center).inflate(56, 40, 56), e -> e instanceof Mob || e instanceof ItemEntity)) {
            e.discard();
        }
    }

    /** Ein Punkt relativ zur Mitte: {@code along} nach Süden, {@code up} über dem Zifferblatt, {@code side} nach Osten. */
    private static Vec3 rel(Dial g, double along, double up, double side) {
        return new Vec3(g.center.getX() + 0.5 + side, g.center.getY() + up, g.center.getZ() + 0.5 + along);
    }

    /** Kamera an (along, up, side), Blick auf (talong, tup, tside) – im Abgrund. */
    private static Step look(Dial g, double along, double up, double side, double talong, double tup, double tside) {
        return Step.server(any -> {
            ServerLevel level = abyss(any);
            if (g.center == null || level == null || any.getServer().getPlayerList().getPlayers().isEmpty()) {
                return;
            }
            Vec3 eye = rel(g, along, up, side);
            Vec3 look = rel(g, talong, tup, tside).subtract(eye);
            float yaw = (float) (-Math.atan2(look.x, look.z) * 180.0 / Math.PI);
            float pitch = (float) (-Math.atan2(look.y, Math.sqrt(look.x * look.x + look.z * look.z)) * 180.0 / Math.PI);
            var player = any.getServer().getPlayerList().getPlayers().getFirst();
            player.teleportTo(level, eye.x, eye.y - 1.62, eye.z, java.util.Set.of(), yaw, pitch, true);
        });
    }

    /**
     * Stellt Wächter und Ritter für den nächsten Angriff auf: der Wächter auf dem Zifferblatt (nach Osten blickend), der Ritter
     * {@code ahead} Blöcke vor ihm (er greift den Wächter an); beide geheilt (der Wächter auf {@code health} seiner Kraft), Reste
     * früherer Angriffe fort.
     */
    private static Step stage(Dial g, double ahead, float health) {
        return Step.server(any -> {
            ServerLevel level = abyss(any);
            for (Entity e : level.getEntitiesOfClass(Entity.class, new AABB(g.center).inflate(48, 40, 48),
                    e -> e instanceof SpellFxEntity || e instanceof ClockGearEntity || e instanceof ClockPendulumEntity)) {
                e.discard();
            }
            if (!(level.getEntity(g.warden) instanceof HourWardenEntity warden) || !(level.getEntity(g.knight) instanceof Mob knight)) {
                return;
            }
            Vec3 k = rel(g, 2.0, 0.0, -5.0 + ahead);
            knight.teleportTo(k.x, k.y, k.z);
            knight.setDeltaMovement(Vec3.ZERO);
            knight.setHealth(knight.getMaxHealth());
            knight.removeAllEffects();
            knight.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 1_000_000, 4, false, false));
            knight.setTarget(warden);
            float yaw = -90.0f;
            Vec3 a = rel(g, 2.0, 0.0, -5.0);
            warden.forceAttack(HourWardenEntity.Attack.NONE);
            warden.snapTo(a.x, a.y, a.z, yaw, 0.0f);
            warden.setYBodyRot(yaw);
            warden.setYHeadRot(yaw);
            warden.setDeltaMovement(Vec3.ZERO);
            warden.setHealth(warden.getMaxHealth() * health);
            warden.setTarget(knight);
        });
    }

    /** Wartet, bis der Wächter (im Client) den Angriff {@code attack} seit mindestens {@code tick} Ticks zeigt – oder ihn beendet hat. */
    private static Step attackAt(Dial g, HourWardenEntity.Attack attack, int tick) {
        boolean[] seen = {false};
        return Step.until(() -> {
            var minecraft = net.minecraft.client.Minecraft.getInstance();
            if (minecraft.level == null || !(minecraft.level.getEntity(g.warden) instanceof HourWardenEntity warden)) {
                return false;
            }
            if (warden.currentAttack() == attack) {
                seen[0] = true;
                return warden.attackTime(0.0f) >= tick;
            }
            return seen[0];
        }, 400);
    }

    /** Kamera quer zur Bahn des Pendels: {@code distance} neben dem Ritter, {@code height} hoch, Blick auf {@code lookUp} über ihm. */
    private static Step pendulumView(Dial g, double distance, double height, double lookUp) {
        return Step.server(any -> {
            ServerLevel level = abyss(any);
            if (level == null || !(level.getEntity(g.knight) instanceof Entity knight) || !(level.getEntity(g.warden) instanceof Entity warden)
                    || any.getServer().getPlayerList().getPlayers().isEmpty()) {
                return;
            }
            Vec3 line = knight.position().subtract(warden.position()).multiply(1.0, 0.0, 1.0);
            Vec3 across = line.lengthSqr() < 1.0e-3 ? new Vec3(0.0, 0.0, 1.0) : new Vec3(-line.z, 0.0, line.x).normalize();
            Vec3 eye = knight.position().add(across.scale(distance)).add(0.0, height, 0.0);
            Vec3 look = knight.position().add(0.0, lookUp, 0.0).subtract(eye);
            float yaw = (float) (-Math.atan2(look.x, look.z) * 180.0 / Math.PI);
            float pitch = (float) (-Math.atan2(look.y, Math.sqrt(look.x * look.x + look.z * look.z)) * 180.0 / Math.PI);
            var player = any.getServer().getPlayerList().getPlayers().getFirst();
            player.teleportTo(level, eye.x, eye.y - 1.62, eye.z, java.util.Set.of(), yaw, pitch, true);
        });
    }

    /**
     * Kamera beim Wächter: im Abstand {@code distance}, um {@code angle} Grad aus der Linie Wächter → Ritter gedreht,
     * {@code height} über ihm; Blick auf ihn ({@code toward} 0) oder auf den Ritter ({@code toward} 1).
     */
    private static Step follow(Dial g, double angle, double distance, double height, double toward) {
        return Step.server(any -> {
            ServerLevel level = abyss(any);
            if (level == null || !(level.getEntity(g.warden) instanceof Entity warden) || any.getServer().getPlayerList().getPlayers().isEmpty()) {
                return;
            }
            if (warden instanceof HourWardenEntity hw) {
                KingdomsAtWar.LOGGER.info("[Showcase] Wächter: {} ({}) bei {}", hw.currentAttack(), hw.attackTime(0.0f), hw.position().subtract(rel(g, 0.0, 0.0, 0.0)));
            }
            Vec3 c = warden.position().add(0.0, 2.0, 0.0);
            Vec3 k = level.getEntity(g.knight) instanceof Entity knight ? knight.position().add(0.0, 1.0, 0.0) : c.add(0.0, 0.0, 1.0);
            Vec3 line = k.subtract(c).multiply(1.0, 0.0, 1.0);
            if (line.lengthSqr() < 1.0e-3) {
                line = new Vec3(0.0, 0.0, 1.0);
            }
            Vec3 focus = c.add(k.subtract(c).scale(toward));
            Vec3 dir = line.normalize().yRot((float) Math.toRadians(-angle));
            Vec3 eye = focus.add(dir.scale(distance)).add(0.0, height, 0.0);
            eye = new Vec3(eye.x, Math.max(eye.y, g.center.getY() + 1.0), eye.z);
            Vec3 look = focus.subtract(eye);
            float yaw = (float) (-Math.atan2(look.x, look.z) * 180.0 / Math.PI);
            float pitch = (float) (-Math.atan2(look.y, Math.sqrt(look.x * look.x + look.z * look.z)) * 180.0 / Math.PI);
            var player = any.getServer().getPlayerList().getPlayers().getFirst();
            player.teleportTo(level, eye.x, eye.y - 1.62, eye.z, java.util.Set.of(), yaw, pitch, true);
        });
    }
}
