package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.shadow.SoulBellBlockEntity;
import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.entity.shadow.SoulShepherdEntity;
import io.github.sirschrolli.kingdomsatwar.entity.shadow.SoulWispEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.magic.fx.SpellFxEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModTags;
import io.github.sirschrolli.kingdomsatwar.registry.ShadowGear;
import io.github.sirschrolli.kingdomsatwar.registry.SoulShepherd;
import io.github.sirschrolli.kingdomsatwar.shadow.ShadowTerrain;
import io.github.sirschrolli.kingdomsatwar.world.ShadowGate;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures;
import io.github.sirschrolli.kingdomsatwar.worldgen.shepherd.SoulPenPiece;
import io.github.sirschrolli.kingdomsatwar.worldgen.shepherd.SoulPenStructure;
import java.util.Deque;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Vorführung ({@code soul_shepherd}): der Seelenpferch im Seelensumpf der Schattenwelt – von oben, vom Steg vor dem Haupttor,
 * der Glockenstuhl, die Hütte – und der Seelenhirte: in Andacht vor seiner Glocke, sein Erwachen, die Bossleiste, alle Angriffe
 * gegen einen (zähen) Ritter, sein Zorn, sein Tod und die verstummte Glocke; dazu die Dämmerrüstung mit dem Hirtenstab und
 * Dämmerquarz am Podest.
 *
 * <p>Lage im Pferch: {@code along} zählt nach Süden (zum Haupttor), {@code side} nach Osten.
 */
final class SoulShepherdShowcase {
    private static final String TAG = "kaw_shepherd_show";

    private SoulShepherdShowcase() {}

    private static final class Pen {
        BlockPos center;
        BlockPos bell;
        int shepherd = -1;
        int knight = -1;
        boolean ready;
    }

    private static ServerLevel shadow(ServerLevel any) {
        return any.getServer().getLevel(ShadowGate.SHADOW_WORLD);
    }

    static void boss(Deque<Step> steps) {
        Pen g = new Pen();
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> {
            minecraft.options.renderDistance().set(12);
            minecraft.options.gamma().set(1.0);
        }));
        steps.add(Step.command("gamerule spawn_monsters false"));
        steps.add(Step.command("gamerule spawn_mobs false"));
        steps.add(Step.command("difficulty normal"));
        steps.add(Step.command("gamemode spectator"));
        steps.add(Step.server(any -> locate(shadow(any), g)));
        steps.add(Step.until(() -> g.ready, 12000));
        steps.add(Step.server(any -> clean(shadow(any), g)));
        // der Pferch von oben und vom Steg aus
        steps.add(look(g, 30.0, 22.0, 18.0, 0.0, 2.0, 0.0));
        shot(steps, "pen_aerial", true);
        steps.add(look(g, SoulPenPiece.PATH_END + 1.0, 2.6, 0.0, 0.0, 4.5, 0.0));
        shot(steps, "pen_gate", false);
        steps.add(look(g, 9.0, 4.5, -7.5, 0.0, 5.0, 0.0));
        shot(steps, "pen_belfry", false);
        steps.add(look(g, -10.0, 4.0, 9.0, -SoulPenPiece.HUT, 3.5, 0.0));
        shot(steps, "pen_hut", false);
        steps.add(look(g, -SoulPenPiece.HUT + 1.8, 1.7, -1.6, -SoulPenPiece.HUT - 1.5, 0.8, 2.0));
        shot(steps, "pen_hut_inside", false);
        steps.add(look(g, 3.6, 1.4, 3.2, 2.0, -0.6, 2.0));
        shot(steps, "pen_dusk_quartz", false);

        // der Hirte in Andacht
        steps.add(Step.server(any -> {
            ServerLevel level = shadow(any);
            if (level.getBlockEntity(g.bell) instanceof SoulBellBlockEntity bell) {
                bell.reset(level);
                SoulShepherdEntity shepherd = bell.spawn(level, false);
                if (shepherd != null) {
                    g.shepherd = shepherd.getId();
                }
            }
        }));
        steps.add(look(g, 6.5, 2.4, 2.4, 1.0, 3.2, 0.0));
        steps.add(Step.waitTicks(60));
        steps.add(Step.screenshot("shepherd_prayer"));
        steps.add(look(g, 12.0, 3.4, -5.0, 0.5, 3.6, 0.0));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screenshot("shepherd_prayer_far"));
        steps.add(look(g, 3.4, 1.0, 1.0, 1.0, 3.6, 0.0));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screenshot("shepherd_prayer_close"));
        // er erwacht
        steps.add(Step.server(any -> {
            if (shadow(any).getEntity(g.shepherd) instanceof SoulShepherdEntity shepherd) {
                shepherd.awaken();
            }
        }));
        steps.add(look(g, 7.0, 2.6, 2.0, 1.0, 3.6, 0.0));
        steps.add(attackAt(g, SoulShepherdEntity.Attack.AWAKEN, 30));
        steps.add(Step.screenshot("shepherd_awaken"));
        steps.add(attackAt(g, SoulShepherdEntity.Attack.AWAKEN, 46));
        steps.add(Step.screenshot("shepherd_awaken_bell"));
        steps.add(Step.until(() -> {
            var minecraft = net.minecraft.client.Minecraft.getInstance();
            return minecraft.level != null && minecraft.level.getEntity(g.shepherd) instanceof SoulShepherdEntity shepherd
                    && shepherd.currentAttack() != SoulShepherdEntity.Attack.AWAKEN;
        }, 200));
        // ein Ritter als Gegner
        steps.add(Step.server(any -> {
            ServerLevel level = shadow(any);
            Mob knight = ModEntities.SOLDIERS.get(SoldierType.KNIGHT).get().create(level, EntitySpawnReason.EVENT);
            if (knight == null) {
                return;
            }
            Vec3 p = rel(g, 7.0, 0.0, 2.0);
            knight.snapTo(p.x, p.y, p.z, 90.0f, 0.0f);
            knight.goalSelector.removeAllGoals(goal -> true);
            knight.targetSelector.removeAllGoals(goal -> true);
            knight.setSilent(true);
            knight.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 1_000_000, 4, false, false));
            knight.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 1_000_000, 0, false, false));
            knight.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(net.minecraft.world.item.Items.IRON_SWORD));
            knight.addTag(TAG);
            level.addFreshEntity(knight);
            g.knight = knight.getId();
        }));
        steps.add(stage(g, 7.0, false));
        steps.add(Step.hud(true));
        steps.add(follow(g, 40.0, 8.0, 1.5, 0.3));
        steps.add(Step.waitTicks(80));
        steps.add(follow(g, 40.0, 8.0, 1.5, 0.3));
        steps.add(Step.waitTicks(4));
        steps.add(Step.screenshot("shepherd_bossbar"));
        steps.add(Step.hud(false));
        // die Angriffe: {Angriff, Tick, Name, Winkel, Abstand, Höhe, Blick zum Ritter, Abstand des Ritters, Zorn}
        java.util.Set<String> started = new java.util.HashSet<>();
        for (String[] shot : new String[][] {
                {"HOOK", "13", "shepherd_hook", "70", "8.0", "1.2", "0.5", "10.0", ""},
                {"HOOK", "23", "shepherd_hook_strike", "60", "6.5", "1.0", "0.3", "10.0", ""},
                {"LANTERN", "14", "shepherd_lantern", "55", "7.5", "1.6", "0.3", "3.5", ""},
                {"WISPS", "20", "shepherd_wisps_call", "35", "7.5", "1.0", "0.0", "9.0", ""},
                {"WISPS", "34", "shepherd_wisps", "70", "9.0", "1.5", "0.5", "9.0", ""},
                {"BELL", "17", "shepherd_bell", "45", "9.5", "3.5", "0.2", "6.0", ""},
                {"FOG", "8", "shepherd_fog", "45", "8.0", "1.2", "0.2", "6.0", ""},
                {"FOG", "23", "shepherd_fog_strike", "80", "7.0", "1.4", "0.6", "6.0", ""},
                {"PEN", "14", "shepherd_pen_cast", "40", "8.0", "1.2", "0.2", "8.0", ""},
                {"PEN", "30", "shepherd_pen", "60", "10.5", "3.5", "0.8", "8.0", ""},
                {"CRASH", "17", "shepherd_crash", "70", "7.5", "1.2", "0.4", "3.0", ""},
                {"FURY", "6", "shepherd_fury_roar", "30", "6.5", "0.6", "0.0", "7.0", ""},
                {"FURY", "28", "shepherd_fury", "40", "9.5", "3.0", "0.0", "7.0", ""},
                {"STORM", "34", "shepherd_storm", "45", "10.5", "4.0", "0.2", "6.0", "1"}}) {
            SoulShepherdEntity.Attack attack = SoulShepherdEntity.Attack.valueOf(shot[0]);
            int tick = Integer.parseInt(shot[1]);
            if (started.add(shot[0])) {
                steps.add(stage(g, Double.parseDouble(shot[7]), !shot[8].isEmpty()));
                steps.add(Step.waitTicks(2));
                steps.add(Step.server(any -> {
                    if (shadow(any).getEntity(g.shepherd) instanceof SoulShepherdEntity shepherd) {
                        shepherd.forceAttack(attack);
                    }
                }));
            }
            steps.add(attackAt(g, attack, tick - 3));
            steps.add(follow(g, Double.parseDouble(shot[3]), Double.parseDouble(shot[4]), Double.parseDouble(shot[5]),
                    Double.parseDouble(shot[6])));
            steps.add(Step.waitTicks(1));
            steps.add(Step.screenshot(shot[2]));
        }
        // besiegt
        steps.add(Step.waitTicks(30));
        steps.add(Step.server(any -> {
            ServerLevel level = shadow(any);
            for (Entity e : level.getEntitiesOfClass(Entity.class, new AABB(g.center).inflate(40, 24, 40),
                    e -> e instanceof SpellFxEntity || e instanceof SoulWispEntity || e.getId() == g.knight)) {
                e.discard();
            }
            if (level.getEntity(g.shepherd) instanceof SoulShepherdEntity shepherd) {
                Vec3 home = Vec3.atBottomCenterOf(SoulPenPiece.home(g.center));
                shepherd.snapTo(home.x, home.y, home.z, SoulPenPiece.GATE.toYRot(), 0.0f);
                shepherd.setYBodyRot(SoulPenPiece.GATE.toYRot());
                shepherd.setDeltaMovement(Vec3.ZERO);
                shepherd.setInvisible(false);
                shepherd.damageCooldownTime = 0;
                shepherd.setHealth(1.0f);
                shepherd.hurtServer(level, level.damageSources().genericKill(), 1000.0f);
            }
        }));
        steps.add(look(g, 6.5, 2.6, 2.4, 1.0, 2.6, 0.0));
        steps.add(Step.waitTicks(34));
        steps.add(Step.screenshot("shepherd_death"));
        steps.add(Step.waitTicks(SoulShepherdEntity.DEATH_TICKS));
        steps.add(Step.server(any -> {
            for (ItemEntity item : shadow(any).getEntitiesOfClass(ItemEntity.class, new AABB(g.center).inflate(30, 30, 30))) {
                item.discard();
            }
        }));
        steps.add(look(g, 3.2, 4.4, 1.6, 0.0, 6.2, 0.0));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("pen_bell_silent"));
        // die Dämmerrüstung und der Hirtenstab am Rüstungsständer
        steps.add(Step.server(any -> {
            ServerLevel level = shadow(any);
            Vec3 p = rel(g, 6.0, 0.0, -4.0);
            ArmorStand stand = new ArmorStand(level, p.x, p.y, p.z);
            stand.setShowArms(true);
            stand.setNoBasePlate(true);
            stand.setNoGravity(true);
            stand.setRightArmPose(new Rotations(-20, 0, 8));
            // zur Kamera gedreht (schräg von vorn)
            Vec3 cam = rel(g, 9.6, 1.9, -2.6);
            float yaw = (float) Math.toDegrees(Math.atan2(-(cam.x - p.x), cam.z - p.z)) + 20.0f;
            stand.setYRot(yaw);
            stand.setYBodyRot(yaw);
            stand.setYHeadRot(yaw);
            stand.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ShadowGear.DUSK_HOOD.get()));
            stand.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ShadowGear.DUSK_CLOAK.get()));
            stand.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ShadowGear.DUSK_LEGGINGS.get()));
            stand.setItemSlot(EquipmentSlot.FEET, new ItemStack(ShadowGear.DUSK_BOOTS.get()));
            stand.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(SoulShepherd.CROOK.get()));
            stand.addTag(TAG);
            level.addFreshEntity(stand);
        }));
        steps.add(look(g, 9.6, 1.9, -2.6, 6.0, 1.1, -4.0));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("dusk_gear_crook"));
        steps.add(look(g, 7.6, 1.6, -3.4, 6.0, 1.3, -4.0));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screenshot("dusk_gear_close"));
        steps.add(Step.server(any -> {
            for (Entity e : shadow(any).getEntitiesOfClass(Entity.class, new AABB(g.center).inflate(48, 40, 48), e -> e.entityTags().contains(TAG))) {
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

    /** Sucht den nächsten Seelenpferch (notfalls baut er selbst einen im Sumpf) und lässt seine Chunks entstehen. */
    private static void locate(ServerLevel level, Pen g) {
        if (level == null) {
            g.ready = true;
            return;
        }
        var structure = level.registryAccess().lookupOrThrow(Registries.STRUCTURE).getOrThrow(ModStructures.SOUL_PEN_STRUCTURE);
        var found = level.getChunkSource().getGenerator().findNearestMapStructure(level, HolderSet.direct(structure),
                new BlockPos(0, ShadowTerrain.SEA_LEVEL, 0), 60, false);
        if (found != null) {
            BlockPos pos = found.getFirst();
            var chunk = level.getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.STRUCTURE_STARTS);
            StructureStart start = chunk.getStartForStructure(structure.value());
            if (start != null && start.isValid() && start.getPieces().getFirst() instanceof SoulPenPiece piece) {
                g.center = piece.center();
            }
        }
        if (g.center == null) {
            build(level, g);
        }
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                level.setChunkForced((g.center.getX() >> 4) + dx, (g.center.getZ() >> 4) + dz, true);
                level.getChunk((g.center.getX() >> 4) + dx, (g.center.getZ() >> 4) + dz);
            }
        }
        g.bell = SoulPenPiece.bell(g.center);
        KingdomsAtWar.LOGGER.info("[Showcase] Seelenpferch bei {}, Glocke {} ({})", g.center, g.bell, level.getBlockState(g.bell));
        g.ready = true;
    }

    /** Notfalls: einen passenden Platz im Seelensumpf suchen (wie beim Erzeugen der Welt) und dort selbst bauen. */
    private static void build(ServerLevel level, Pen g) {
        var generator = level.getChunkSource().getGenerator();
        var random = level.getChunkSource().randomState();
        var biomes = generator.getBiomeSource().createUncachedResolver(random);
        var marsh = level.registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(ModTags.Biomes.HAS_SOUL_PEN);
        BlockPos spot = null;
        for (int r = 0; r < 24 && spot == null; r++) {
            for (int i = -r; i <= r && spot == null; i++) {
                for (int j = -r; j <= r && spot == null; j++) {
                    if (Math.max(Math.abs(i), Math.abs(j)) != r) {
                        continue;
                    }
                    spot = SoulPenStructure.find(generator, level, random, biomes, marsh::contains, new ChunkPos(i * 8, j * 8), 7L * i + j);
                }
            }
        }
        if (spot == null) {
            spot = new BlockPos(8, ShadowTerrain.SEA_LEVEL + 1, 8);
        }
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                level.getChunk((spot.getX() >> 4) + dx, (spot.getZ() >> 4) + dz);
            }
        }
        long seed = 98_311L;
        SoulPenPiece.build(level, SoulPenPiece.makeBox(spot), spot, seed, ShadowTerrain.SEA_LEVEL, net.minecraft.util.RandomSource.create(seed), null);
        g.center = spot;
        KingdomsAtWar.LOGGER.info("[Showcase] kein Seelenpferch in Reichweite – selbst gebaut bei {}", spot);
    }

    /** Fremde Wesen im Pferch fort (Reiher, Motten …), damit nichts durchs Bild läuft. */
    private static void clean(ServerLevel level, Pen g) {
        if (level == null || g.center == null) {
            return;
        }
        for (Entity e : level.getEntitiesOfClass(Mob.class, new AABB(g.center).inflate(48, 24, 48))) {
            e.discard();
        }
    }

    /** Ein Punkt relativ zur Mitte: {@code along} nach Süden (zum Haupttor), {@code up} über dem Boden, {@code side} nach Osten. */
    private static Vec3 rel(Pen g, double along, double up, double side) {
        return new Vec3(g.center.getX() + 0.5 + side, g.center.getY() + up, g.center.getZ() + 0.5 + along);
    }

    /** Kamera an (along, up, side), Blick auf (talong, tup, tside) – in der Schattenwelt. */
    private static Step look(Pen g, double along, double up, double side, double talong, double tup, double tside) {
        return Step.server(any -> {
            ServerLevel level = shadow(any);
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
     * Stellt Hirte und Ritter für den nächsten Angriff auf: der Hirte vor der Glocke (zum Tor blickend), der Ritter
     * {@code ahead} Blöcke vor ihm; beide geheilt, Reste früherer Angriffe fort.
     */
    private static Step stage(Pen g, double ahead, boolean enraged) {
        return Step.server(any -> {
            ServerLevel level = shadow(any);
            for (Entity e : level.getEntitiesOfClass(Entity.class, new AABB(g.center).inflate(40, 24, 40),
                    e -> e instanceof SpellFxEntity || e instanceof SoulWispEntity)) {
                e.discard();
            }
            if (!(level.getEntity(g.shepherd) instanceof SoulShepherdEntity shepherd) || !(level.getEntity(g.knight) instanceof LivingEntity knight)) {
                return;
            }
            // auf der Weide südlich des Glockenstuhls: der Hirte blickt nach Osten, der Ritter steht vor ihm (die Kameras stehen
            // weiter südlich, der Glockenstuhl ist im Hintergrund)
            Vec3 k = rel(g, 7.0, 0.0, -5.0 + ahead);
            knight.teleportTo(k.x, k.y, k.z);
            knight.setDeltaMovement(Vec3.ZERO);
            knight.setHealth(knight.getMaxHealth());
            knight.removeAllEffects();
            knight.clearFire();
            knight.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 1_000_000, 4, false, false));
            knight.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 1_000_000, 0, false, false));
            Vec3 a = rel(g, 7.0, 0.0, -5.0);
            float yaw = -90.0f;
            shepherd.snapTo(a.x, a.y, a.z, yaw, 0.0f);
            shepherd.setYBodyRot(yaw);
            shepherd.setYHeadRot(yaw);
            shepherd.setDeltaMovement(Vec3.ZERO);
            shepherd.setInvisible(false);
            shepherd.setHealth(enraged ? shepherd.getMaxHealth() * 0.6f : shepherd.getMaxHealth());
            if (enraged) {
                shepherd.forceEnraged();
            }
            shepherd.setTarget(knight);
            shepherd.forceAttack(SoulShepherdEntity.Attack.NONE);
        });
    }

    /** Wartet, bis der Hirte (im Client) den Angriff {@code attack} seit mindestens {@code tick} Ticks zeigt – oder ihn beendet hat. */
    private static Step attackAt(Pen g, SoulShepherdEntity.Attack attack, int tick) {
        boolean[] seen = {false};
        return Step.until(() -> {
            var minecraft = net.minecraft.client.Minecraft.getInstance();
            if (minecraft.level == null || !(minecraft.level.getEntity(g.shepherd) instanceof SoulShepherdEntity shepherd)) {
                return false;
            }
            if (shepherd.currentAttack() == attack) {
                seen[0] = true;
                return shepherd.attackTime(0.0f) >= tick;
            }
            return seen[0];
        }, 400);
    }

    /**
     * Kamera beim Hirten: im Abstand {@code distance}, um {@code angle} Grad aus der Linie Hirte → Ritter gedreht, {@code height}
     * über seiner Brust; Blick auf ihn ({@code toward} 0) oder auf den Ritter ({@code toward} 1).
     */
    private static Step follow(Pen g, double angle, double distance, double height, double toward) {
        return Step.server(any -> {
            ServerLevel level = shadow(any);
            if (level == null || !(level.getEntity(g.shepherd) instanceof Entity shepherd) || any.getServer().getPlayerList().getPlayers().isEmpty()) {
                return;
            }
            Vec3 c = shepherd.position().add(0.0, 2.4, 0.0);
            Vec3 k = level.getEntity(g.knight) instanceof Entity knight ? knight.position().add(0.0, 1.0, 0.0) : c.add(0.0, 0.0, 1.0);
            Vec3 line = k.subtract(c).multiply(1.0, 0.0, 1.0);
            if (line.lengthSqr() < 1.0e-3) {
                line = new Vec3(0.0, 0.0, 1.0);
            }
            Vec3 focus = c.add(k.subtract(c).scale(toward));
            // die Kamera steht südlich der Linie (der Glockenstuhl im Hintergrund) und im Pferch (nie hinter dem Zaun)
            Vec3 dir = line.normalize().yRot((float) Math.toRadians(-angle));
            Vec3 eye = focus.add(dir.scale(distance)).add(0.0, height, 0.0);
            Vec3 mid = Vec3.atBottomCenterOf(g.center);
            for (int i = 0; i < 30 && Math.hypot(eye.x - mid.x, eye.z - mid.z) > 12.0; i++) {
                eye = eye.add(focus.subtract(eye).multiply(0.08, 0.0, 0.08));
            }
            eye = new Vec3(eye.x, Math.max(eye.y, g.center.getY() + 1.0), eye.z);
            Vec3 look = focus.subtract(eye);
            float yaw = (float) (-Math.atan2(look.x, look.z) * 180.0 / Math.PI);
            float pitch = (float) (-Math.atan2(look.y, Math.sqrt(look.x * look.x + look.z * look.z)) * 180.0 / Math.PI);
            var player = any.getServer().getPlayerList().getPlayers().getFirst();
            player.teleportTo(level, eye.x, eye.y - 1.62, eye.z, java.util.Set.of(), yaw, pitch, true);
        });
    }
}
