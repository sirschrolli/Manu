package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.sea.WitchCauldronBlockEntity;
import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.entity.sea.InkBoltEntity;
import io.github.sirschrolli.kingdomsatwar.entity.sea.SeaWitchEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.magic.fx.SpellFxEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.SeaGear;
import io.github.sirschrolli.kingdomsatwar.registry.SeaWitch;
import io.github.sirschrolli.kingdomsatwar.urmeer.UrmeerDimension;
import io.github.sirschrolli.kingdomsatwar.urmeer.UrmeerTerrain;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures;
import io.github.sirschrolli.kingdomsatwar.worldgen.witch.WitchGrottoPiece;
import java.util.Deque;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Vorführung ({@code sea_witch}): die Hexengrotte im Urmeer – von außen, der Eingang mit dem Knochenbogen, innen der Kessel
 * und das Wrack – und die Meerhexe: in Trance über ihrem Kessel, ihr Erwachen, die Bossleiste, alle Angriffe gegen einen
 * (zähen) Ritter, ihr Zorn, ihr Tod und der erkaltete Kessel; dazu die Taucherrüstung aus Aquamarin, der Hexenstab und
 * Aquamarin am Meeresgrund.
 */
final class SeaWitchShowcase {
    private static final String TAG = "kaw_witch_show";

    private SeaWitchShowcase() {}

    private static final class Grotto {
        BlockPos center;
        BlockPos cauldron;
        BlockPos ore;
        Direction door = Direction.SOUTH;
        int witch = -1;
        int knight = -1;
        boolean ready;
    }

    private static ServerLevel sea(ServerLevel any) {
        return any.getServer().getLevel(UrmeerDimension.LEVEL);
    }

    static void boss(Deque<Step> steps) {
        Grotto g = new Grotto();
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> {
            minecraft.options.renderDistance().set(12);
            minecraft.options.gamma().set(1.0);
        }));
        steps.add(Step.command("gamerule spawn_monsters false"));
        steps.add(Step.command("gamerule spawn_mobs false"));
        steps.add(Step.command("difficulty normal"));
        steps.add(Step.command("gamemode spectator"));
        steps.add(Step.command("effect give @s minecraft:night_vision infinite 0 true"));
        steps.add(Step.command("effect give @s minecraft:conduit_power infinite 0 true"));
        steps.add(Step.command("time set 6000"));
        steps.add(Step.server(any -> locate(sea(any), g)));
        steps.add(Step.until(() -> g.ready, 12000));
        // die Grotte von außen (in ihrer toten Zone)
        steps.add(look(g, 27.0, 13.0, 11.0, 3.0, 4.0, 0.0));
        shot(steps, "grotto_outside", true);
        steps.add(look(g, 8.0, 26.0, 17.0, 0.0, 5.0, 0.0));
        shot(steps, "grotto_dome", false);
        steps.add(look(g, WitchGrottoPiece.REACH + 1.0, 5.0, 2.5, WitchGrottoPiece.RADIUS - 2.0, 3.6, 0.0));
        shot(steps, "grotto_entrance", false);
        // innen
        steps.add(Step.server(any -> {
            ServerLevel level = sea(any);
            if (g.center == null) {
                return;
            }
            for (Entity e : level.getEntitiesOfClass(Mob.class, new AABB(g.center).inflate(40, 24, 40))) {
                e.discard();
            }
        }));
        steps.add(lookIn(g, WitchGrottoPiece.RADIUS - 5.0, 5.5, 3.0, 0.0, 3.6, 0.0));
        shot(steps, "grotto_inside", false);
        steps.add(lookIn(g, 4.2, 4.6, 2.8, 0.0, 2.6, 0.0));
        shot(steps, "grotto_cauldron", false);
        steps.add(lookIn(g, 0.5, 4.8, 4.5, -10.0, 2.8, 0.0));
        shot(steps, "grotto_wreck", false);
        steps.add(lookIn(g, 1.5, 3.0, 1.0, -3.0, 7.5, 5.0));
        shot(steps, "grotto_bubbles", false);
        steps.add(Step.once(minecraft -> {
            if (g.ore == null) {
                return;
            }
            Vec3 eye = Vec3.atCenterOf(g.ore).add(2.4, 1.6, 1.8);
            Vec3 to = Vec3.atCenterOf(g.ore).subtract(eye);
            tp(minecraft, eye, to);
        }));
        shot(steps, "aquamarine_seabed", true);

        // die Hexe in Trance
        steps.add(Step.server(any -> {
            ServerLevel level = sea(any);
            if (level.getBlockEntity(g.cauldron) instanceof WitchCauldronBlockEntity cauldron) {
                cauldron.reset(level);
                SeaWitchEntity witch = cauldron.spawn(level, false);
                if (witch != null) {
                    g.witch = witch.getId();
                }
            }
        }));
        steps.add(lookIn(g, 7.0, 5.6, 2.4, 0.0, 6.4, 0.0));
        steps.add(Step.waitTicks(40));
        steps.add(Step.screenshot("witch_trance"));
        steps.add(lookIn(g, 3.6, 3.0, -3.0, 0.0, 7.0, 0.0));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screenshot("witch_trance_below"));
        // sie erwacht
        steps.add(Step.server(any -> {
            if (sea(any).getEntity(g.witch) instanceof SeaWitchEntity witch) {
                witch.awaken();
            }
        }));
        steps.add(lookIn(g, 6.2, 7.0, 1.6, 0.0, 6.8, 0.0));
        steps.add(attackAt(g, SeaWitchEntity.Attack.AWAKEN, 28));
        steps.add(Step.screenshot("witch_awaken"));
        steps.add(attackAt(g, SeaWitchEntity.Attack.AWAKEN, 44));
        steps.add(Step.screenshot("witch_laugh"));
        steps.add(Step.until(() -> {
            var minecraft = net.minecraft.client.Minecraft.getInstance();
            return minecraft.level != null && minecraft.level.getEntity(g.witch) instanceof SeaWitchEntity witch
                    && witch.currentAttack() != SeaWitchEntity.Attack.AWAKEN;
        }, 200));
        // ein Ritter als Gegner
        steps.add(Step.server(any -> {
            ServerLevel level = sea(any);
            Mob knight = ModEntities.SOLDIERS.get(SoldierType.KNIGHT).get().create(level, EntitySpawnReason.EVENT);
            if (knight == null) {
                return;
            }
            Vec3 p = rel(g, 6.0, 4.0, 0.0);
            knight.snapTo(p.x, p.y, p.z, 0.0f, 0.0f);
            knight.setNoAi(true);
            knight.setNoGravity(true);
            knight.setSilent(true);
            knight.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 1_000_000, 4, false, false));
            knight.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 1_000_000, 0, false, false));
            knight.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(net.minecraft.world.item.Items.IRON_SWORD));
            knight.addTag(TAG);
            level.addFreshEntity(knight);
            g.knight = knight.getId();
        }));
        steps.add(stage(g, 7.0, false));
        steps.add(Step.hud(true));
        steps.add(follow(g, 40.0, 8.0, 1.0, 0.3));
        steps.add(Step.waitTicks(80));
        steps.add(follow(g, 40.0, 8.0, 1.0, 0.3));
        steps.add(Step.waitTicks(4));
        steps.add(Step.screenshot("witch_bossbar"));
        steps.add(Step.hud(false));
        // die Angriffe: {Angriff, Tick, Name, Winkel, Abstand, Höhe, Blick zum Ritter, Abstand des Ritters, Zorn}
        java.util.Set<String> started = new java.util.HashSet<>();
        for (String[] shot : new String[][] {
                {"INK", "13", "witch_ink_raise", "50", "6.5", "0.6", "0.2", "8.0", ""},
                {"INK", "22", "witch_ink", "70", "8.5", "1.0", "0.5", "8.0", ""},
                {"WHIRLPOOL", "34", "witch_whirlpool", "55", "8.5", "2.5", "0.7", "8.0", ""},
                {"SONG", "34", "witch_song", "30", "6.5", "0.2", "0.1", "8.0", ""},
                {"BUBBLE", "24", "witch_bubble", "40", "8.0", "1.0", "0.55", "7.0", ""},
                {"SHARKS", "27", "witch_sharks", "35", "10.5", "2.5", "0.3", "8.0", ""},
                {"CORAL", "24", "witch_coral", "50", "9.0", "3.0", "0.5", "8.0", ""},
                {"TAIL", "11", "witch_tail", "75", "6.5", "0.6", "0.3", "2.6", ""},
                {"FURY", "16", "witch_fury_scream", "35", "6.5", "0.4", "0.0", "7.0", ""},
                {"FURY", "25", "witch_fury", "35", "9.5", "4.5", "0.0", "7.0", ""},
                {"CURSE", "30", "witch_curse", "55", "8.0", "2.0", "0.2", "7.0", "1"}}) {
            SeaWitchEntity.Attack attack = SeaWitchEntity.Attack.valueOf(shot[0]);
            int tick = Integer.parseInt(shot[1]);
            if (started.add(shot[0])) {
                steps.add(stage(g, Double.parseDouble(shot[7]), !shot[8].isEmpty()));
                steps.add(Step.waitTicks(2));
                steps.add(Step.server(any -> {
                    if (sea(any).getEntity(g.witch) instanceof SeaWitchEntity witch) {
                        witch.forceAttack(attack);
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
            ServerLevel level = sea(any);
            for (Entity e : level.getEntitiesOfClass(Entity.class, new AABB(g.center).inflate(40, 24, 40),
                    e -> e instanceof SpellFxEntity || e instanceof InkBoltEntity || e.entityTags().contains(SeaWitchEntity.SHARK_TAG)
                            || e.getId() == g.knight)) {
                e.discard();
            }
            if (level.getEntity(g.witch) instanceof SeaWitchEntity witch) {
                Vec3 home = Vec3.atBottomCenterOf(g.cauldron.above(WitchGrottoPiece.HOVER - WitchGrottoPiece.CAULDRON_Y));
                witch.snapTo(home.x, home.y, home.z, yawToward(g), 0.0f);
                witch.setDeltaMovement(Vec3.ZERO);
                witch.damageCooldownTime = 0;
                witch.setHealth(1.0f);
                witch.hurtServer(level, level.damageSources().genericKill(), 1000.0f);
            }
        }));
        steps.add(lookIn(g, 6.0, 6.0, 1.8, 0.0, 6.0, 0.0));
        steps.add(Step.waitTicks(28));
        steps.add(Step.screenshot("witch_death"));
        steps.add(Step.waitTicks(SeaWitchEntity.DEATH_TICKS));
        steps.add(Step.server(any -> {
            for (ItemEntity item : sea(any).getEntitiesOfClass(ItemEntity.class, new AABB(g.center).inflate(30, 30, 30))) {
                item.discard();
            }
        }));
        steps.add(lookIn(g, 3.0, 4.2, 2.2, 0.0, 2.4, 0.0));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("grotto_cauldron_cold"));
        // die Taucherrüstung und der Hexenstab am Rüstungsständer, daneben Aquamarin
        steps.add(Step.server(any -> {
            ServerLevel level = sea(any);
            Vec3 p = rel(g, 6.5, 1.0, 0.0);
            BlockPos floor = BlockPos.containing(p.x, p.y - 1.0, p.z);
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    level.setBlock(floor.offset(dx, 0, dz), io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.TIDE_STONE_BRICKS.base().get()
                            .defaultBlockState(), Block.UPDATE_ALL);
                }
            }
            Vec3 side = rel(g, 6.5, 0.0, 2.2);
            BlockPos s = BlockPos.containing(side.x, floor.getY(), side.z);
            level.setBlock(s, SeaGear.AQUAMARINE_BLOCK.get().defaultBlockState(), Block.UPDATE_ALL);
            level.setBlock(s.above(), SeaGear.AQUAMARINE_ORE.get().defaultBlockState(), Block.UPDATE_ALL);
            ArmorStand stand = new ArmorStand(level, p.x, floor.getY() + 1.0, p.z);
            stand.setShowArms(true);
            stand.setNoBasePlate(true);
            stand.setNoGravity(true);
            stand.setRightArmPose(new Rotations(-25, 0, 10));
            float yaw = g.door.toYRot() - 25.0f;
            stand.setYRot(yaw);
            stand.setYBodyRot(yaw);
            stand.setYHeadRot(yaw);
            stand.setItemSlot(EquipmentSlot.HEAD, new ItemStack(SeaGear.AQUAMARINE_HELMET.get()));
            stand.setItemSlot(EquipmentSlot.CHEST, new ItemStack(SeaGear.AQUAMARINE_CHESTPLATE.get()));
            stand.setItemSlot(EquipmentSlot.LEGS, new ItemStack(SeaGear.AQUAMARINE_LEGGINGS.get()));
            stand.setItemSlot(EquipmentSlot.FEET, new ItemStack(SeaGear.AQUAMARINE_BOOTS.get()));
            stand.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(SeaWitch.WITCH_STAFF.get()));
            stand.addTag(TAG);
            level.addFreshEntity(stand);
        }));
        steps.add(lookIn(g, 11.0, 2.9, 2.6, 6.5, 2.0, 0.4));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("diving_gear"));
        steps.add(lookIn(g, 9.3, 2.4, 0.9, 6.5, 2.1, 0.3));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screenshot("diving_gear_close"));
        steps.add(lookIn(g, 3.6, 2.8, -1.8, 6.5, 1.7, 0.2));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screenshot("diving_gear_back"));
        steps.add(Step.server(any -> {
            for (Entity e : sea(any).getEntitiesOfClass(Entity.class, new AABB(g.center).inflate(48, 40, 48), e -> e.entityTags().contains(TAG))) {
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

    /** Sucht die nächste Hexengrotte, lässt ihre Chunks entstehen und merkt sich Mitte, Kessel, Eingang und eine Aquamarinader. */
    private static void locate(ServerLevel level, Grotto g) {
        if (level == null) {
            g.ready = true;
            return;
        }
        var structure = level.registryAccess().lookupOrThrow(Registries.STRUCTURE).getOrThrow(ModStructures.WITCH_GROTTO_STRUCTURE);
        // die nächsten Kandidaten der Reihe nach prüfen (nicht jeder trägt eine Grotte)
        BlockPos origin = new BlockPos(0, UrmeerTerrain.SEA_LEVEL, 0);
        for (int tries = 0; tries < 8 && g.center == null; tries++) {
            var found = level.getChunkSource().getGenerator().findNearestMapStructure(level, HolderSet.direct(structure), origin, 40, false);
            if (found == null) {
                break;
            }
            BlockPos pos = found.getFirst();
            var chunk = level.getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.STRUCTURE_STARTS);
            StructureStart start = chunk.getStartForStructure(structure.value());
            if (start != null && start.isValid() && start.getPieces().get(0) instanceof WitchGrottoPiece piece) {
                g.center = piece.center();
                g.door = piece.door();
            } else {
                origin = pos.offset(500, 0, 0);
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
        g.cauldron = WitchGrottoPiece.cauldron(g.center);
        // ein Aquamarinerz, das offen im Wasser liegt (außerhalb der Kuppel)
        double best = Double.MAX_VALUE;
        for (BlockPos p : BlockPos.betweenClosed(g.center.offset(-40, -6, -40), g.center.offset(40, 14, 40))) {
            int keep = WitchGrottoPiece.RADIUS + 4;
            if (!level.getBlockState(p).is(SeaGear.AQUAMARINE_ORE.get()) || p.distSqr(g.center) < keep * keep) {
                continue;
            }
            if (level.getBlockState(p.above()).is(net.minecraft.world.level.block.Blocks.WATER)) {
                double d = p.distSqr(g.center);
                if (d < best) {
                    best = d;
                    g.ore = p.immutable();
                }
            }
        }
        KingdomsAtWar.LOGGER.info("[Showcase] Hexengrotte bei {} (Eingang {}), Kessel {} ({}), Aquamarin bei {}", g.center, g.door, g.cauldron,
                level.getBlockState(g.cauldron), g.ore);
        g.ready = true;
    }

    /** Notfalls (keine Grotte in Reichweite): an einer ebenen, tiefen Stelle des Grunds selbst eine bauen. */
    private static void build(ServerLevel level, Grotto g) {
        var generator = level.getChunkSource().getGenerator();
        var random = level.getChunkSource().randomState();
        BlockPos spot = null;
        for (int i = 0; i < 400 && spot == null; i++) {
            int x = (i % 20) * 64 - 632;
            int z = (i / 20) * 64 - 632;
            int[] hs = new int[25];
            int k = 0;
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    hs[k++] = generator.getBaseHeight(x + dx * 8, z + dz * 8, net.minecraft.world.level.levelgen.Heightmap.Types.OCEAN_FLOOR_WG,
                            level, random);
                }
            }
            java.util.Arrays.sort(hs);
            int floor = hs[12] - 1;
            if (hs[22] - hs[2] <= 4 && UrmeerTerrain.SEA_LEVEL - floor >= WitchGrottoPiece.HEIGHT + 8) {
                spot = new BlockPos(x, floor, z);
            }
        }
        if (spot == null) {
            spot = new BlockPos(8, UrmeerTerrain.SEA_LEVEL - 30, 8);
        }
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                level.getChunk((spot.getX() >> 4) + dx, (spot.getZ() >> 4) + dz);
            }
        }
        long seed = 97_301L;
        WitchGrottoPiece.build(level, WitchGrottoPiece.makeBox(spot), spot, seed, UrmeerTerrain.SEA_LEVEL,
                net.minecraft.util.RandomSource.create(seed), null);
        g.center = spot;
        g.door = WitchGrottoPiece.entrance(seed);
        KingdomsAtWar.LOGGER.info("[Showcase] keine Hexengrotte in Reichweite – selbst gebaut bei {}", spot);
    }

    /** Ein Punkt relativ zur Mitte: {@code along} Blöcke Richtung Eingang, {@code up} hoch, {@code side} nach rechts (vom Inneren aus). */
    private static Vec3 rel(Grotto g, double along, double up, double side) {
        Direction s = g.door.getClockWise();
        return new Vec3(g.center.getX() + 0.5 + g.door.getStepX() * along + s.getStepX() * side, g.center.getY() + up,
                g.center.getZ() + 0.5 + g.door.getStepZ() * along + s.getStepZ() * side);
    }

    private static float yawToward(Grotto g) {
        return g.door.toYRot();
    }

    /** Kamera an (along, up, side), Blick auf (talong, tup, tside) – im Urmeer. */
    private static Step look(Grotto g, double along, double up, double side, double talong, double tup, double tside) {
        return Step.once(minecraft -> {
            if (g.center == null) {
                return;
            }
            Vec3 eye = rel(g, along, up, side);
            Vec3 to = rel(g, talong, tup, tside).subtract(eye);
            tp(minecraft, eye, to);
        });
    }

    /** Wie {@link #look}, aber die Kamera bleibt im Inneren der Kuppel (nie in der Wand). */
    private static Step lookIn(Grotto g, double along, double up, double side, double talong, double tup, double tside) {
        return Step.once(minecraft -> {
            if (g.center == null) {
                return;
            }
            Vec3 eye = WitchGrottoPiece.keepInside(g.center, rel(g, along, up, side), 0.2, 0.9);
            Vec3 to = rel(g, talong, tup, tside).subtract(eye);
            tp(minecraft, eye, to);
        });
    }

    private static void tp(net.minecraft.client.Minecraft minecraft, Vec3 eye, Vec3 to) {
        float yaw = (float) (-Math.atan2(to.x, to.z) * 180.0 / Math.PI);
        float pitch = (float) (-Math.atan2(to.y, Math.sqrt(to.x * to.x + to.z * to.z)) * 180.0 / Math.PI);
        minecraft.player.connection.sendCommand(String.format(java.util.Locale.ROOT,
                "execute in kingdomsatwar:urmeer run tp @s %.2f %.2f %.2f %.1f %.1f", eye.x, eye.y - 1.62, eye.z, yaw, pitch));
    }

    /**
     * Stellt Hexe und Ritter für den nächsten Angriff auf: die Hexe über dem Kessel (zum Eingang blickend), der Ritter
     * {@code ahead} Blöcke vor ihr; beide geheilt, Reste früherer Angriffe fort.
     */
    private static Step stage(Grotto g, double ahead, boolean enraged) {
        return Step.server(any -> {
            ServerLevel level = sea(any);
            for (Entity e : level.getEntitiesOfClass(Entity.class, new AABB(g.center).inflate(40, 24, 40),
                    e -> e instanceof SpellFxEntity || e instanceof InkBoltEntity || e.entityTags().contains(SeaWitchEntity.SHARK_TAG))) {
                e.discard();
            }
            if (!(level.getEntity(g.witch) instanceof SeaWitchEntity witch) || !(level.getEntity(g.knight) instanceof LivingEntity knight)) {
                return;
            }
            Vec3 k = rel(g, ahead - 2.0, 4.5, 0.0);
            knight.teleportTo(k.x, k.y, k.z);
            knight.setDeltaMovement(Vec3.ZERO);
            knight.setHealth(knight.getMaxHealth());
            knight.removeAllEffects();
            knight.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 1_000_000, 4, false, false));
            knight.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 1_000_000, 0, false, false));
            Vec3 a = rel(g, -2.0, WitchGrottoPiece.HOVER, 0.0);
            float yaw = yawToward(g);
            witch.snapTo(a.x, a.y, a.z, yaw, 0.0f);
            witch.setYBodyRot(yaw);
            witch.setYHeadRot(yaw);
            witch.setDeltaMovement(Vec3.ZERO);
            witch.setHealth(enraged ? witch.getMaxHealth() * 0.6f : witch.getMaxHealth());
            if (enraged) {
                witch.forceEnraged();
            }
            witch.setTarget(knight);
            witch.forceAttack(SeaWitchEntity.Attack.NONE);
        });
    }

    /** Wartet, bis die Hexe (im Client) den Angriff {@code attack} seit mindestens {@code tick} Ticks zeigt – oder ihn beendet hat. */
    private static Step attackAt(Grotto g, SeaWitchEntity.Attack attack, int tick) {
        boolean[] seen = {false};
        return Step.until(() -> {
            var minecraft = net.minecraft.client.Minecraft.getInstance();
            if (minecraft.level == null || !(minecraft.level.getEntity(g.witch) instanceof SeaWitchEntity witch)) {
                return false;
            }
            if (witch.currentAttack() == attack) {
                seen[0] = true;
                return witch.attackTime(0.0f) >= tick;
            }
            return seen[0];
        }, 400);
    }

    /**
     * Kamera bei der Hexe: im Abstand {@code distance}, um {@code angle} Grad aus der Linie Hexe → Ritter gedreht, {@code height}
     * über ihrer Brust; Blick auf sie ({@code toward} 0) oder auf den Ritter ({@code toward} 1).
     */
    private static Step follow(Grotto g, double angle, double distance, double height, double toward) {
        return Step.server(any -> {
            ServerLevel level = sea(any);
            if (level == null || !(level.getEntity(g.witch) instanceof Entity witch) || any.getServer().getPlayerList().getPlayers().isEmpty()) {
                return;
            }
            Vec3 c = witch.position().add(witch.getDeltaMovement().scale(2.0)).add(0.0, 2.0, 0.0);
            Vec3 k = level.getEntity(g.knight) instanceof Entity knight ? knight.position().add(0.0, 1.0, 0.0) : c.add(1.0, 0.0, 0.0);
            Vec3 line = k.subtract(c).multiply(1.0, 0.0, 1.0);
            if (line.lengthSqr() < 1.0e-3) {
                line = new Vec3(1.0, 0.0, 0.0);
            }
            Vec3 focus = c.add(k.subtract(c).scale(toward));
            Vec3 dir = line.normalize().yRot((float) Math.toRadians(angle));
            Vec3 eye = WitchGrottoPiece.keepInside(g.center, focus.add(dir.scale(distance)).add(0.0, height, 0.0), 0.2, 0.9);
            Vec3 look = focus.subtract(eye);
            float yaw = (float) (-Math.atan2(look.x, look.z) * 180.0 / Math.PI);
            float pitch = (float) (-Math.atan2(look.y, Math.sqrt(look.x * look.x + look.z * look.z)) * 180.0 / Math.PI);
            var player = any.getServer().getPlayerList().getPlayers().getFirst();
            player.teleportTo(level, eye.x, eye.y - 1.62, eye.z, java.util.Set.of(), yaw, pitch, true);
        });
    }
}
