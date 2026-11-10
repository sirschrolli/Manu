package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.entity.monster.AshWraithEntity;
import io.github.sirschrolli.kingdomsatwar.entity.monster.EmberWyrmEntity;
import io.github.sirschrolli.kingdomsatwar.entity.monster.MireShamblerEntity;
import io.github.sirschrolli.kingdomsatwar.entity.monster.MonsterShot;
import io.github.sirschrolli.kingdomsatwar.entity.monster.WerewolfEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import java.util.Deque;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Vorführung der neuen Monster (0.69): eine Waldlichtung im Abendrot mit Werwölfen, ein Moor mit Moorschlurfern, ein
 * Lavasee mit Glutwürmern und ein Seelensandtal mit Aschengeistern – jede Bühne von vorn, aus der Nähe und mitten im
 * Angriff. Die Wesen erscheinen erst, wenn die Kamera da ist (Zustände wie "aufgerichtet" überstehen kein Neuladen).
 */
final class BeastShowcase {
    private static final String STAGE_TAG = "kaw_beast_stage";
    private static final int OZ = 1400;
    /** Mitten der vier Bühnen (x), weit genug auseinander, dass keine in die Bilder der anderen ragt. */
    private static final int FOREST = 0;
    private static final int MOOR = 90;
    private static final int LAVA = 180;
    private static final int SOUL = 270;

    private BeastShowcase() {}

    static void beasts(Deque<Step> steps) {
        Mob[] m = new Mob[8];
        boolean[] ready = new boolean[1];
        steps.add(Step.command("difficulty hard"));
        steps.add(Step.command("gamerule spawn_monsters false"));
        steps.add(Step.server(level -> {
            int g = Showcase.ground;
            forest(level, g);
            moor(level, g);
            lava(level, g);
            soul(level, g);
            ready[0] = true;
        }));
        steps.add(Step.until(() -> ready[0], 3000));
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> minecraft.options.fov().set(55)));

        // ------------------------------------------------------------------ Waldlichtung im Abendrot
        steps.add(Step.command("time set 12350"));
        arrive(steps, FOREST + 0.5, 1.2, OZ - 8.5, FOREST + 0.5, 1.7, OZ + 3.0);
        steps.add(Step.server(level -> {
            int g = Showcase.ground;
            m[0] = creature(level, ModEntities.WEREWOLF.get(), FOREST - 3.0, g, OZ + 2.0, 200.0f, w -> { });
            m[1] = creature(level, ModEntities.WEREWOLF.get(), FOREST + 3.5, g, OZ + 1.0, 150.0f, w -> w.forceRunning(true));
            m[2] = creature(level, ModEntities.WEREWOLF.get(), FOREST + 0.5, g, OZ + 6.0, 180.0f, w -> { });
        }));
        steps.add(Step.waitTicks(30));
        steps.add(Step.server(level -> ((WerewolfEntity) m[2]).forceHowl()));
        steps.add(Step.waitTicks(17));
        steps.add(Step.screenshot("beast_werewolf"));
        // der heulende Werwolf allein, von schräg unten
        steps.add(Step.server(level -> {
            m[0].discard();
            m[1].discard();
        }));
        steps.add(look(FOREST - 2.2, 0.4, OZ + 2.6, FOREST + 0.5, 3.0, OZ + 6.0));
        steps.add(Step.waitTicks(25));
        steps.add(Step.server(level -> ((WerewolfEntity) m[2]).forceHowl()));
        steps.add(Step.waitTicks(18));
        steps.add(Step.screenshot("beast_werewolf_howl"));
        // sein Gesicht aus der Nähe
        steps.add(look(FOREST + 1.3, 1.0, OZ + 3.3, FOREST + 0.5, 2.55, OZ + 5.8));
        steps.add(Step.waitTicks(45));
        steps.add(Step.screenshot("beast_werewolf_close"));
        // der Sprung: mitten in der Luft auf einen Ritter zu, von der Seite
        steps.add(Step.server(level -> {
            int g = Showcase.ground;
            m[2].discard();
            Mob knight = soldier(level, FOREST + 6.0, g, OZ + 1.0, 90.0f);
            m[3] = knight;
            m[1] = creature(level, ModEntities.WEREWOLF.get(), FOREST + 1.2, g + 1.1, OZ + 1.0, 0.0f, w -> w.setNoGravity(true));
            float yaw = yawTo(m[1].position(), knight.position());
            m[1].snapTo(m[1].getX(), m[1].getY(), m[1].getZ(), yaw, 0.0f);
            m[1].setYBodyRot(yaw);
            m[1].setYHeadRot(yaw);
            knight.snapTo(knight.getX(), knight.getY(), knight.getZ(), yawTo(knight.position(), m[1].position()), 0.0f);
            knight.setYBodyRot(knight.getYRot());
            knight.setYHeadRot(knight.getYRot());
        }));
        steps.add(look(FOREST + 3.4, 0.9, OZ - 5.6, FOREST + 3.4, 1.7, OZ + 1.0));
        steps.add(Step.waitTicks(30));
        steps.add(Step.server(level -> event(level, m[1], WerewolfEntity.EVENT_POUNCE)));
        steps.add(Step.waitTicks(9));
        steps.add(Step.screenshot("beast_werewolf_pounce"));
        steps.add(Step.server(BeastShowcase::clearStage));

        // ------------------------------------------------------------------ Moor am späten Nachmittag
        steps.add(Step.command("time set 11200"));
        arrive(steps, MOOR + 0.5, 1.3, OZ - 8.5, MOOR + 0.5, 1.3, OZ + 3.5);
        steps.add(Step.server(level -> {
            int g = Showcase.ground;
            m[0] = creature(level, ModEntities.MIRE_SHAMBLER.get(), MOOR - 1.0, g, OZ + 3.0, 185.0f, s -> { });
            m[1] = creature(level, ModEntities.MIRE_SHAMBLER.get(), MOOR + 5.0, g, OZ + 6.0, 215.0f, s -> { });
            m[2] = creature(level, ModEntities.MIRE_SHAMBLER.get(), MOOR - 6.0, g, OZ + 0.5, 160.0f, MireShamblerEntity::forceSubmerge);
        }));
        steps.add(Step.waitTicks(30));
        steps.add(Step.server(level -> event(level, m[1], MireShamblerEntity.EVENT_THROW)));
        steps.add(Step.waitTicks(15));
        steps.add(Step.server(level -> {
            Vec3 f = forward(m[1]);
            hang(MonsterShot.shoot(level, m[1], MonsterShot.Kind.MUD, m[1].position().add(f.scale(1.5)).add(0.0, 2.7, 0.0), Vec3.ZERO, 0.0f));
        }));
        steps.add(Step.waitTicks(2));
        steps.add(Step.screenshot("beast_moor"));
        steps.add(look(MOOR + 1.6, 1.0, OZ + 0.2, MOOR - 1.0, 2.2, OZ + 3.0));
        steps.add(Step.waitTicks(40));
        steps.add(Step.screenshot("beast_moor_close"));
        // der Lauernde steigt auf
        steps.add(look(MOOR - 3.4, 0.7, OZ - 2.9, MOOR - 6.0, 1.3, OZ + 0.5));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("beast_moor_lurk"));
        steps.add(Step.server(level -> m[2].hurtServer(level, level.damageSources().generic(), 0.5f)));
        steps.add(Step.waitTicks(33));
        steps.add(Step.screenshot("beast_moor_rise"));
        // der Wurzelgriff, von der Seite
        steps.add(Step.server(level -> {
            int g = Showcase.ground;
            Mob knight = soldier(level, MOOR - 1.0, g, OZ + 0.9, 0.0f);
            m[3] = knight;
            ((MireShamblerEntity) m[0]).forceGrasp(knight);
        }));
        steps.add(look(MOOR + 3.6, 1.0, OZ + 1.4, MOOR - 1.0, 1.5, OZ + 2.0));
        steps.add(Step.waitTicks(24));
        steps.add(Step.screenshot("beast_moor_grasp"));
        steps.add(Step.server(BeastShowcase::clearStage));

        // ------------------------------------------------------------------ Lavasee bei Nacht
        steps.add(Step.command("time set 18000"));
        arrive(steps, LAVA + 0.5, 1.4, OZ - 9.5, LAVA + 0.5, 2.0, OZ + 4.0);
        steps.add(Step.server(level -> {
            int g = Showcase.ground;
            m[3] = soldier(level, LAVA + 4.5, g, OZ - 4.0, 330.0f);
            m[0] = creature(level, ModEntities.EMBER_WYRM.get(), LAVA + 0.5, g - 1.05, OZ + 4.0, 200.0f, w -> w.setNoGravity(true));
            ((EmberWyrmEntity) m[0]).forceSurface((Mob) m[3]);
            m[1] = creature(level, ModEntities.EMBER_WYRM.get(), LAVA - 6.0, g - 1.05, OZ + 8.0, 110.0f, w -> w.setNoGravity(true));
        }));
        steps.add(Step.waitTicks(40));
        steps.add(Step.server(level -> event(level, m[0], EmberWyrmEntity.EVENT_SPIT)));
        steps.add(Step.waitTicks(10));
        steps.add(Step.server(level -> {
            Vec3 mouth = ((EmberWyrmEntity) m[0]).headPosition();
            Vec3 toward = m[3].position().add(0.0, 1.0, 0.0).subtract(mouth).normalize();
            hang(MonsterShot.shoot(level, m[0], MonsterShot.Kind.MAGMA, mouth.add(toward.scale(1.8)), Vec3.ZERO, 0.0f));
        }));
        steps.add(Step.waitTicks(2));
        steps.add(Step.screenshot("beast_wyrm"));
        steps.add(look(LAVA - 2.6, 1.6, OZ - 1.6, LAVA + 0.5, 4.0, OZ + 3.4));
        steps.add(Step.waitTicks(30));
        steps.add(Step.server(level -> event(level, m[0], EmberWyrmEntity.EVENT_SPIT)));
        steps.add(Step.waitTicks(9));
        steps.add(Step.screenshot("beast_wyrm_close"));
        steps.add(look(LAVA + 6.4, 1.2, OZ + 0.6, LAVA + 0.5, 3.0, OZ + 3.4));
        steps.add(Step.waitTicks(30));
        steps.add(Step.server(level -> event(level, m[0], EmberWyrmEntity.EVENT_BITE)));
        steps.add(Step.waitTicks(13));
        steps.add(Step.screenshot("beast_wyrm_bite"));
        steps.add(look(LAVA - 9.6, 0.6, OZ + 3.4, LAVA - 6.0, 0.0, OZ + 8.0));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("beast_wyrm_swim"));
        steps.add(Step.server(BeastShowcase::clearStage));

        // ------------------------------------------------------------------ Seelensandtal bei Nacht
        arrive(steps, SOUL + 1.0, 1.3, OZ - 8.5, SOUL + 1.0, 2.0, OZ + 3.0);
        steps.add(Step.server(level -> {
            int g = Showcase.ground;
            m[0] = creature(level, ModEntities.ASH_WRAITH.get(), SOUL - 3.0, g + 1.2, OZ + 3.0, 195.0f, w -> w.setNoGravity(true));
            m[1] = creature(level, ModEntities.ASH_WRAITH.get(), SOUL + 2.5, g + 1.8, OZ + 1.5, 210.0f, w -> w.setNoGravity(true));
            m[2] = creature(level, ModEntities.ASH_WRAITH.get(), SOUL + 7.0, g + 1.5, OZ + 6.5, 165.0f, w -> w.setNoGravity(true));
            m[3] = soldier(level, SOUL + 6.0, g, OZ + 2.5, 20.0f);
        }));
        steps.add(Step.waitTicks(30));
        steps.add(Step.server(level -> {
            event(level, m[1], AshWraithEntity.EVENT_BOLT);
            event(level, m[2], AshWraithEntity.EVENT_CLOUD);
        }));
        steps.add(Step.waitTicks(10));
        steps.add(Step.server(level -> {
            Vec3 f = forward(m[1]);
            hang(MonsterShot.shoot(level, m[1], MonsterShot.Kind.SOUL, m[1].position().add(f.scale(1.6)).add(0.0, 1.5, 0.0), Vec3.ZERO, 0.0f));
            ashCloud(level, m[3].position());
        }));
        steps.add(Step.waitTicks(4));
        steps.add(Step.screenshot("beast_wraith"));
        steps.add(look(SOUL - 2.0, 1.5, OZ + 0.2, SOUL - 3.0, 3.1, OZ + 3.0));
        steps.add(Step.waitTicks(40));
        steps.add(Step.screenshot("beast_wraith_close"));
        steps.add(look(SOUL + 2.6, 1.3, OZ - 3.0, SOUL + 6.6, 2.0, OZ + 4.0));
        steps.add(Step.waitTicks(20));
        steps.add(Step.server(level -> {
            event(level, m[2], AshWraithEntity.EVENT_CLOUD);
            ashCloud(level, m[3].position());
        }));
        steps.add(Step.waitTicks(14));
        steps.add(Step.screenshot("beast_wraith_cloud"));
        steps.add(Step.server(BeastShowcase::clearStage));
    }

    /** Kamera hin, warten bis die Chunks da sind. */
    private static void arrive(Deque<Step> steps, double x, double dy, double z, double tx, double tdy, double tz) {
        steps.add(look(x, dy, z, tx, tdy, tz));
        steps.add(Step.waitTicks(60));
        steps.add(Step.waitForChunks(600));
        steps.add(Step.waitTicks(30));
    }

    // =====================================================================================
    // Bühnen
    // =====================================================================================

    /** Boden der Bühne (und ein weiter Rand desselben Bodens, damit die flache Welt nicht ins Bild ragt). */
    private static void ground(ServerLevel level, int cx, int g, BlockState floor) {
        Showcase.fill(level, new BlockPos(cx - 42, g - 1, OZ - 26), new BlockPos(cx + 42, g - 1, OZ + 52), floor);
        Showcase.fill(level, new BlockPos(cx - 42, g, OZ - 26), new BlockPos(cx + 42, g + 20, OZ + 52), Blocks.AIR.defaultBlockState());
    }

    private static void set(ServerLevel level, int x, int y, int z, BlockState state) {
        level.setBlock(new BlockPos(x, y, z), state, Block.UPDATE_CLIENTS);
    }

    private static boolean inFront(int x, int z, int cx) {
        return Math.abs(x - cx) < 11 && z > OZ - 12 && z < OZ + 11;
    }

    /** Waldlichtung: Gras, Farn und Laub, ein gestürzter bemooster Stamm, Findlinge – ringsum dichter Fichtenwald. */
    private static void forest(ServerLevel level, int g) {
        int cx = FOREST;
        ground(level, cx, g, Blocks.GRASS_BLOCK.defaultBlockState());
        RandomSource random = RandomSource.create(11);
        for (int x = cx - 42; x <= cx + 42; x++) {
            for (int z = OZ - 26; z <= OZ + 52; z++) {
                int r = random.nextInt(100);
                if (r < 10) {
                    set(level, x, g - 1, z, Blocks.PODZOL.defaultBlockState());
                } else if (r < 14) {
                    set(level, x, g - 1, z, Blocks.COARSE_DIRT.defaultBlockState());
                }
                boolean clearing = inFront(x, z, cx);
                int d = random.nextInt(100);
                if (d < (clearing ? 16 : 34)) {
                    set(level, x, g, z, Blocks.SHORT_GRASS.defaultBlockState());
                } else if (d < (clearing ? 21 : 44)) {
                    set(level, x, g, z, Blocks.FERN.defaultBlockState());
                } else if (d < (clearing ? 24 : 50)) {
                    set(level, x, g, z, Blocks.LEAF_LITTER.defaultBlockState());
                } else if (d < (clearing ? 25 : 52)) {
                    set(level, x, g, z, (random.nextBoolean() ? Blocks.BROWN_MUSHROOM : Blocks.RED_MUSHROOM).defaultBlockState());
                }
            }
        }
        // dichter Fichtenwald um die Lichtung (vorn bleibt der Blick frei)
        for (int i = 0; i < 260; i++) {
            int x = cx - 40 + random.nextInt(81);
            int z = OZ - 24 + random.nextInt(75);
            double dx = (x - cx) / 13.0;
            double dz = (z - OZ - 3) / 10.0;
            if (dx * dx + dz * dz < 1.0 || z < OZ - 6 && Math.abs(x - cx) < 16) {
                continue;
            }
            spruce(level, x, g, z, 8 + random.nextInt(7), random);
        }
        // ein gestürzter, bemooster Stamm
        BlockState log = Blocks.SPRUCE_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X);
        for (int x = cx - 9; x <= cx - 5; x++) {
            set(level, x, g, OZ + 5, log);
            set(level, x, g + 1, OZ + 5, (x % 2 == 0 ? Blocks.MOSS_CARPET : Blocks.AIR).defaultBlockState());
        }
        // Findlinge
        for (int[] b : new int[][] {{7, 7}, {-8, -1}, {9, 2}}) {
            set(level, cx + b[0], g, OZ + b[1], Blocks.MOSSY_COBBLESTONE.defaultBlockState());
            set(level, cx + b[0] + 1, g, OZ + b[1], Blocks.COBBLESTONE.defaultBlockState());
            set(level, cx + b[0], g, OZ + b[1] + 1, Blocks.MOSSY_COBBLESTONE.defaultBlockState());
            set(level, cx + b[0], g + 1, OZ + b[1], Blocks.MOSS_CARPET.defaultBlockState());
        }
    }

    private static void spruce(ServerLevel level, int x, int g, int z, int height, RandomSource random) {
        BlockState log = Blocks.SPRUCE_LOG.defaultBlockState();
        BlockState leaves = Blocks.SPRUCE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
        for (int y = 0; y < height; y++) {
            set(level, x, g + y, z, log);
        }
        for (int y = 3; y <= height; y++) {
            int fromTop = height - y;
            int radius = fromTop == 0 ? 0 : (fromTop % 2 == 1 ? Math.min(3, 1 + fromTop / 2) : Math.max(1, fromTop / 2));
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if ((dx != 0 || dz != 0) && Math.abs(dx) + Math.abs(dz) <= radius + (radius > 1 ? 1 : 0)
                            && !(Math.abs(dx) == radius && Math.abs(dz) == radius && random.nextBoolean())) {
                        BlockPos pos = new BlockPos(x + dx, g + y, z + dz);
                        if (level.getBlockState(pos).isAir()) {
                            level.setBlock(pos, leaves, Block.UPDATE_CLIENTS);
                        }
                    }
                }
            }
        }
        set(level, x, g + height, z, leaves);
        set(level, x, g + height + 1, z, leaves);
    }

    /** Moor: Schlamm, Wurzeln und Moos, Tümpel mit Seerosen, Schilf, Mangroven mit hängendem Moos als Kulisse. */
    private static void moor(ServerLevel level, int g) {
        int cx = MOOR;
        ground(level, cx, g, Blocks.MUD.defaultBlockState());
        RandomSource random = RandomSource.create(23);
        int[][] pools = {{-11, 3, 3, 2}, {4, 11, 6, 3}, {-4, 15, 4, 2}, {10, 2, 2, 3}, {-15, 12, 3, 3}, {15, 13, 4, 3}, {-20, 4, 3, 4},
                {20, 5, 3, 3}, {0, 22, 7, 3}};
        for (int[] p : pools) {
            for (int dx = -p[2]; dx <= p[2]; dx++) {
                for (int dz = -p[3]; dz <= p[3]; dz++) {
                    if (dx * dx / (double) (p[2] * p[2]) + dz * dz / (double) (p[3] * p[3]) <= 1.0) {
                        set(level, cx + p[0] + dx, g - 1, OZ + p[1] + dz, Blocks.WATER.defaultBlockState());
                        if (random.nextInt(4) == 0) {
                            set(level, cx + p[0] + dx, g, OZ + p[1] + dz, Blocks.LILY_PAD.defaultBlockState());
                        }
                    }
                }
            }
        }
        for (int x = cx - 42; x <= cx + 42; x++) {
            for (int z = OZ - 26; z <= OZ + 52; z++) {
                if (level.getBlockState(new BlockPos(x, g - 1, z)).is(Blocks.WATER)) {
                    continue;
                }
                int r = random.nextInt(100);
                if (r < 14) {
                    set(level, x, g - 1, z, Blocks.MUDDY_MANGROVE_ROOTS.defaultBlockState());
                } else if (r < 20) {
                    set(level, x, g - 1, z, Blocks.PACKED_MUD.defaultBlockState());
                } else if (r < 24) {
                    set(level, x, g - 1, z, Blocks.MOSS_BLOCK.defaultBlockState());
                }
                boolean nearWater = false;
                for (Direction d : Direction.Plane.HORIZONTAL) {
                    nearWater |= level.getBlockState(new BlockPos(x, g - 1, z).relative(d)).is(Blocks.WATER);
                }
                boolean front = inFront(x, z, cx);
                int d = random.nextInt(100);
                if (nearWater && d < 55 && !(front && z < OZ + 8)) {
                    int h = 2 + random.nextInt(2);
                    for (int y = 0; y < h; y++) {
                        set(level, x, g + y, z, Blocks.SUGAR_CANE.defaultBlockState());
                    }
                } else if (d < (front ? 7 : 18)) {
                    set(level, x, g, z, Blocks.SHORT_GRASS.defaultBlockState());
                } else if (d < (front ? 10 : 22)) {
                    set(level, x, g, z, Blocks.MOSS_CARPET.defaultBlockState());
                } else if (d < (front ? 11 : 24)) {
                    set(level, x, g, z, Blocks.SHORT_DRY_GRASS.defaultBlockState());
                }
            }
        }
        // Mangroven mit hängendem Moos rings um die Bühne
        for (int i = 0; i < 70; i++) {
            int x = cx - 40 + random.nextInt(81);
            int z = OZ - 24 + random.nextInt(75);
            double dx = (x - cx) / 14.0;
            double dz = (z - OZ - 3) / 11.0;
            if (dx * dx + dz * dz < 1.0 || z < OZ - 6 && Math.abs(x - cx) < 16) {
                continue;
            }
            mangrove(level, x, g, z, 6 + random.nextInt(5), random);
        }
        // ein toter Baum auf der Bühne
        int tx = cx - 9;
        int tz = OZ + 10;
        for (int y = 0; y < 8; y++) {
            set(level, tx, g + y, tz, Blocks.MANGROVE_LOG.defaultBlockState());
        }
        BlockState branchX = Blocks.MANGROVE_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X);
        set(level, tx + 1, g + 5, tz, branchX);
        set(level, tx + 2, g + 6, tz, branchX);
        set(level, tx - 1, g + 6, tz, branchX);
        set(level, tx - 2, g + 7, tz, branchX);
        for (int[] h : new int[][] {{2, 5}, {1, 4}, {-1, 5}, {-2, 6}}) {
            set(level, tx + h[0], g + h[1], tz, Blocks.PALE_HANGING_MOSS.defaultBlockState());
        }
    }

    private static void mangrove(ServerLevel level, int x, int g, int z, int height, RandomSource random) {
        for (int y = 0; y < height; y++) {
            set(level, x, g + y, z, Blocks.MANGROVE_LOG.defaultBlockState());
        }
        for (int[] r : new int[][] {{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
            if (random.nextBoolean()) {
                set(level, x + r[0], g, z + r[1], Blocks.MANGROVE_ROOTS.defaultBlockState());
            }
        }
        BlockState leaves = Blocks.MANGROVE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
        int top = g + height;
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                for (int dy = -2; dy <= 1; dy++) {
                    double d = dx * dx / 9.0 + dz * dz / 9.0 + (dy + 0.5) * (dy + 0.5) / 2.5;
                    if (d < 1.0 && random.nextInt(6) != 0) {
                        BlockPos pos = new BlockPos(x + dx, top + dy, z + dz);
                        if (level.getBlockState(pos).isAir()) {
                            level.setBlock(pos, leaves, Block.UPDATE_CLIENTS);
                            if (dy == -2 && random.nextInt(3) == 0 && level.getBlockState(pos.below()).isAir()) {
                                level.setBlock(pos.below(), Blocks.PALE_HANGING_MOSS.defaultBlockState(), Block.UPDATE_CLIENTS);
                            }
                        }
                    }
                }
            }
        }
    }

    /** Lavasee: Schwarzstein und Basalt, ein See zwei Lagen tief, Basaltsäulen, Feuer auf Netherrack, hinten Felswände. */
    private static void lava(ServerLevel level, int g) {
        int cx = LAVA;
        ground(level, cx, g, Blocks.BLACKSTONE.defaultBlockState());
        RandomSource random = RandomSource.create(37);
        for (int x = cx - 42; x <= cx + 42; x++) {
            for (int z = OZ - 26; z <= OZ + 52; z++) {
                double lake = (x - cx) * (x - cx) / 132.0 + (z - OZ - 6) * (z - OZ - 6) / 49.0;
                if (lake <= 1.0) {
                    set(level, x, g - 1, z, Blocks.LAVA.defaultBlockState());
                    set(level, x, g - 2, z, Blocks.LAVA.defaultBlockState());
                    set(level, x, g - 3, z, Blocks.BLACKSTONE.defaultBlockState());
                    continue;
                }
                int r = random.nextInt(100);
                BlockState floor = lake < 1.25 && r < 45 ? Blocks.MAGMA_BLOCK.defaultBlockState()
                        : r < 30 ? Blocks.BASALT.defaultBlockState() : r < 42 ? Blocks.SMOOTH_BASALT.defaultBlockState()
                        : r < 47 ? Blocks.NETHERRACK.defaultBlockState() : Blocks.BLACKSTONE.defaultBlockState();
                set(level, x, g - 1, z, floor);
                if (floor.is(Blocks.NETHERRACK) && random.nextInt(3) == 0 && !inFront(x, z, cx)) {
                    set(level, x, g, z, Blocks.FIRE.defaultBlockState());
                }
            }
        }
        // Basaltsäulen ringsum und Felswände dahinter
        for (int i = 0; i < 90; i++) {
            int x = cx - 40 + random.nextInt(81);
            int z = OZ - 20 + random.nextInt(71);
            double dx = (x - cx) / 15.0;
            double dz = (z - OZ - 6) / 10.0;
            if (dx * dx + dz * dz < 1.0 || z < OZ - 6 && Math.abs(x - cx) < 16) {
                continue;
            }
            int h = 3 + random.nextInt(z > OZ + 16 ? 12 : 6);
            for (int y = 0; y < h; y++) {
                set(level, x, g + y, z, (y > h - 2 && random.nextBoolean() ? Blocks.MAGMA_BLOCK : Blocks.BASALT).defaultBlockState());
                if (random.nextInt(4) == 0) {
                    set(level, x + 1, g + y / 2, z, Blocks.BASALT.defaultBlockState());
                }
            }
        }
    }

    /** Seelensandtal: Seelenerde und Seelensand, Basaltsäulen mit Seelenlaternen, Seelenfeuer, im Hintergrund ein Gerippe. */
    private static void soul(ServerLevel level, int g) {
        int cx = SOUL;
        ground(level, cx, g, Blocks.SOUL_SOIL.defaultBlockState());
        RandomSource random = RandomSource.create(41);
        for (int x = cx - 42; x <= cx + 42; x++) {
            for (int z = OZ - 26; z <= OZ + 52; z++) {
                int r = random.nextInt(100);
                if (r < 35) {
                    set(level, x, g - 1, z, Blocks.SOUL_SAND.defaultBlockState());
                } else if (r < 39) {
                    set(level, x, g - 1, z, Blocks.BASALT.defaultBlockState());
                }
                if (random.nextInt(45) == 0 && !inFront(x, z, cx)) {
                    set(level, x, g - 1, z, Blocks.SOUL_SOIL.defaultBlockState());
                    set(level, x, g, z, Blocks.SOUL_FIRE.defaultBlockState());
                }
            }
        }
        // Rippen eines riesigen Gerippes, halb im Sand
        for (int i = 0; i < 5; i++) {
            int z = OZ + 16 + i * 2;
            for (int y = 0; y <= 6; y++) {
                int dx = y < 4 ? 5 : (y < 6 ? 4 : 3);
                set(level, cx - 9 - dx, g + y, z, Blocks.BONE_BLOCK.defaultBlockState());
                set(level, cx - 9 + dx, g + y, z, Blocks.BONE_BLOCK.defaultBlockState());
            }
            for (int x = cx - 11; x <= cx - 7; x++) {
                set(level, x, g + 7, z, Blocks.BONE_BLOCK.defaultBlockState());
            }
        }
        for (int z = OZ + 15; z <= OZ + 25; z++) {
            set(level, cx - 9, g + 8, z, Blocks.BONE_BLOCK.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z));
        }
        // Basaltsäulen, manche mit einer Seelenlaterne an der Kette
        for (int i = 0; i < 80; i++) {
            int x = cx - 40 + random.nextInt(81);
            int z = OZ - 20 + random.nextInt(71);
            double dx = (x - cx) / 15.0;
            double dz = (z - OZ - 4) / 10.0;
            if (dx * dx + dz * dz < 1.0 || z < OZ - 6 && Math.abs(x - cx) < 16) {
                continue;
            }
            int h = 3 + random.nextInt(z > OZ + 14 ? 11 : 6);
            for (int y = 0; y < h; y++) {
                set(level, x, g + y, z, Blocks.BASALT.defaultBlockState());
            }
            set(level, x, g + h, z, Blocks.POLISHED_BASALT.defaultBlockState());
            if (random.nextInt(3) == 0 && h > 4) {
                set(level, x - 1, g + h - 1, z, Blocks.IRON_CHAIN.defaultBlockState());
                set(level, x - 1, g + h - 2, z, Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
            }
        }
        set(level, cx - 1, g, OZ + 9, Blocks.SOUL_CAMPFIRE.defaultBlockState());
    }

    // =====================================================================================
    // Hilfen
    // =====================================================================================

    private static <T extends Mob> T creature(ServerLevel level, EntityType<T> type, double x, double y, double z, float yaw,
            Consumer<T> setup) {
        T mob = type.create(level, EntitySpawnReason.COMMAND);
        if (mob == null) {
            throw new IllegalStateException("Wesen lässt sich nicht erzeugen: " + type);
        }
        mob.setNoAi(true);
        mob.setPersistenceRequired();
        mob.addTag(STAGE_TAG);
        mob.snapTo(x, y, z, yaw, 0);
        mob.setYBodyRot(yaw);
        mob.setYHeadRot(yaw);
        level.addFreshEntity(mob);
        setup.accept(mob);
        return mob;
    }

    private static Mob soldier(ServerLevel level, double x, double y, double z, float yaw) {
        Mob knight = creature(level, ModEntities.SOLDIERS.get(SoldierType.KNIGHT).get(), x, y, z, yaw, k -> { });
        knight.finalizeSpawn(level, level.getCurrentDifficultyAt(knight.blockPosition()), EntitySpawnReason.COMMAND, null);
        knight.setNoAi(true);
        return knight;
    }

    /** Hält ein Geschoss für das Bild in der Luft an. */
    private static void hang(Entity shot) {
        shot.setNoGravity(true);
        shot.setDeltaMovement(Vec3.ZERO);
        shot.addTag(STAGE_TAG);
    }

    private static void ashCloud(ServerLevel level, Vec3 at) {
        AreaEffectCloud cloud = new AreaEffectCloud(level, at.x, at.y, at.z);
        cloud.setRadius(2.6f);
        cloud.setDuration(200);
        cloud.setWaitTime(0);
        cloud.setCustomParticle(ParticleTypes.WHITE_ASH);
        cloud.addTag(STAGE_TAG);
        level.addFreshEntity(cloud);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, at.x, at.y + 0.6, at.z, 40, 1.3, 0.6, 1.3, 0.01);
        level.sendParticles(ParticleTypes.WHITE_ASH, at.x, at.y + 1.0, at.z, 80, 1.5, 0.9, 1.5, 0.01);
    }

    /** Räumt die Wesen und Geschosse der Vorführung fort. */
    private static void clearStage(ServerLevel level) {
        int g = Showcase.ground;
        AABB all = new AABB(FOREST - 50, g - 6, OZ - 30, SOUL + 50, g + 30, OZ + 60);
        for (Entity e : level.getEntitiesOfClass(Entity.class, all, e -> e.entityTags().contains(STAGE_TAG) || e instanceof MonsterShot)) {
            e.discard();
        }
    }

    private static void event(ServerLevel level, Entity entity, byte id) {
        level.broadcastEntityEvent(entity, id);
    }

    private static Vec3 forward(Entity entity) {
        float yaw = entity.getYRot() * Mth.DEG_TO_RAD;
        return new Vec3(-Mth.sin(yaw), 0.0, Mth.cos(yaw));
    }

    private static float yawTo(Vec3 from, Vec3 to) {
        return (float) (Mth.atan2(to.z - from.z, to.x - from.x) * Mth.RAD_TO_DEG) - 90.0f;
    }

    /** Kamera mit den Füßen bei (x, ground + dy, z), Blick auf (tx, ground + tdy, tz). */
    private static Step look(double x, double dy, double z, double tx, double tdy, double tz) {
        return Step.once(minecraft -> {
            int g = Showcase.ground;
            double lx = tx - x;
            double ly = tdy - (dy + 1.62);
            double lz = tz - z;
            float yaw = (float) (-Math.atan2(lx, lz) * 180.0 / Math.PI);
            float pitch = (float) (-Math.atan2(ly, Math.sqrt(lx * lx + lz * lz)) * 180.0 / Math.PI);
            minecraft.player.connection.sendCommand(String.format(Locale.ROOT, "tp @s %.2f %.2f %.2f %.1f %.1f", x, g + dy, z, yaw, pitch));
        });
    }
}
