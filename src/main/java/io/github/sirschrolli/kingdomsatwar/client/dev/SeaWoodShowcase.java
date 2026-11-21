package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.sea.SeaSaplingBlock;
import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.registry.SeaWoods;
import io.github.sirschrolli.kingdomsatwar.urmeer.feature.SeaTreeFeature;
import java.util.Deque;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;

/**
 * Nur zur Prüfung der Meereshölzer (0.92), in der flachen Testwelt: alle Blöcke jedes Holzes in einer Reihe, dann ein
 * gläsernes Becken voll Wasser, in dem die vier Meeresbäume wachsen – von außen, unter Wasser und jeder aus der Nähe.
 */
final class SeaWoodShowcase {
    private static final int BASE_X = 3000;
    /** Das Becken: so lang (x), tief (z) und hoch (Wasser). */
    private static final int TANK_X = 56;
    private static final int TANK_Z = 18;
    private static final int TANK_Y = 26;

    private SeaWoodShowcase() {}

    /** Kamera nach (x, Boden + dy, z), Blick auf (tx, Boden + tdy, tz); der Boden wird erst beim Ausführen gelesen. */
    private static Step look(double x, double dy, double z, double tx, double tdy, double tz) {
        return Step.once(minecraft -> {
            double y = Showcase.ground + dy;
            double lx = tx - x;
            double ly = tdy - dy;
            double lz = tz - z;
            float yaw = (float) (-Math.atan2(lx, lz) * 180.0 / Math.PI);
            float pitch = (float) (-Math.atan2(ly, Math.sqrt(lx * lx + lz * lz)) * 180.0 / Math.PI);
            minecraft.player.connection.sendCommand(String.format(Locale.ROOT, "tp @s %.2f %.2f %.2f %.1f %.1f", x, y - 1.62, z, yaw, pitch));
        });
    }

    private static void shot(Deque<Step> steps, String name, double x, double dy, double z, double tx, double tdy, double tz) {
        steps.add(look(x, dy, z, tx, tdy, tz));
        steps.add(Step.waitTicks(30));
        steps.add(Step.waitForChunks(800));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot(name));
    }

    static void seaWoods(Deque<Step> steps) {
        int[] g = new int[1];
        steps.add(Step.hud(false));
        steps.add(Step.command("gamemode spectator"));
        steps.add(Step.command("effect give @s minecraft:night_vision infinite 0 true"));
        steps.add(Step.command("effect give @s minecraft:conduit_power infinite 0 true"));
        steps.add(Step.once(minecraft -> minecraft.options.renderDistance().set(10)));
        steps.add(look(BASE_X + 20, 12, -6, BASE_X + 20, 0, -16));
        steps.add(Step.waitTicks(40));
        steps.add(Step.waitForChunks(600));
        steps.add(Step.server(level -> {
            g[0] = Showcase.ground;
            build(level, g[0]);
        }));
        steps.add(Step.waitTicks(20));
        // alle Blöcke, Reihe für Reihe
        int y = 0;
        shot(steps, "meereshoelzer_bloecke", BASE_X + 13.5, y + 9.0, -3.0, BASE_X + 13.5, y + 0.5, -16.0);
        shot(steps, "meereshoelzer_nah_1", BASE_X + 6.5, y + 3.5, -7.0, BASE_X + 6.5, y + 1.0, -12.0);
        shot(steps, "meereshoelzer_nah_2", BASE_X + 19.5, y + 3.5, -7.0, BASE_X + 19.5, y + 1.0, -12.0);
        // das Becken von außen und unter Wasser
        shot(steps, "meeresbaeume_becken", BASE_X + TANK_X / 2.0, y + 14.0, -16.0, BASE_X + TANK_X / 2.0, y + 11.0, TANK_Z / 2.0);
        String[] names = {"tangbaum", "korallenbaum", "perlbaum", "tiefseebaum"};
        for (int i = 0; i < 4; i++) {
            double tx = BASE_X + 7 + i * 14 + 0.5;
            shot(steps, "meeresbaum_" + names[i], Math.min(tx + 6.5, BASE_X + TANK_X - 1.5), y + 7.0, TANK_Z - 1.5, tx, y + 8.0, 7.5);
        }
        shot(steps, "meeresbaeume_alle", BASE_X + 2.5, y + 12.0, TANK_Z - 1.5, BASE_X + 30.0, y + 9.0, 7.0);
        steps.add(Step.command("time set 18000"));
        shot(steps, "meeresbaeume_nacht", BASE_X + TANK_X - 1.5, y + 7.0, TANK_Z - 1.5, BASE_X + 7 + 3 * 14 + 0.5, y + 8.0, 7.5);
        steps.add(Step.command("time set 6000"));
    }

    private static void set(ServerLevel level, BlockPos pos, BlockState state) {
        level.setBlock(pos, state, Block.UPDATE_CLIENTS);
    }

    /** Baut die Reihen und das Becken, pflanzt die Bäume. */
    private static void build(ServerLevel level, int ground) {
        // --- die Reihen: je Holz alle Blöcke (vorn links nach rechts) ---
        int row = 0;
        for (SeaWoods.SeaWood wood : SeaWoods.ALL) {
            int z = -10 - row * 3;
            int x = BASE_X;
            BlockPos at = new BlockPos(x, ground, z);
            set(level, at, wood.log.get().defaultBlockState());
            set(level, at.above(), wood.log.get().defaultBlockState());
            set(level, at.east(2), wood.wood.get().defaultBlockState());
            set(level, at.east(4), wood.strippedLog.get().defaultBlockState());
            set(level, at.east(4).above(), wood.strippedLog.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X));
            set(level, at.east(6), wood.strippedWood.get().defaultBlockState());
            set(level, at.east(8), wood.planks.base().get().defaultBlockState());
            set(level, at.east(9), wood.planks.stairs().get().defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH)
                    .setValue(StairBlock.HALF, Half.BOTTOM));
            set(level, at.east(10), wood.planks.slab().get().defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM));
            set(level, at.east(12), wood.fence.get().defaultBlockState());
            set(level, at.east(13), wood.fenceGate.get().defaultBlockState().setValue(FenceGateBlock.FACING, Direction.SOUTH));
            set(level, at.east(14), wood.fence.get().defaultBlockState());
            set(level, at.east(16), wood.door.get().defaultBlockState().setValue(DoorBlock.FACING, Direction.SOUTH)
                    .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER));
            set(level, at.east(16).above(), wood.door.get().defaultBlockState().setValue(DoorBlock.FACING, Direction.SOUTH)
                    .setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
            set(level, at.east(18), wood.planks.base().get().defaultBlockState());
            set(level, at.east(18).above(), wood.trapdoor.get().defaultBlockState().setValue(TrapDoorBlock.FACING, Direction.SOUTH)
                    .setValue(TrapDoorBlock.HALF, Half.BOTTOM));
            set(level, at.east(20), wood.leaves.get().defaultBlockState().setValue(LeavesBlock.PERSISTENT, true));
            set(level, at.east(20).above(), wood.leaves.get().defaultBlockState().setValue(LeavesBlock.PERSISTENT, true));
            set(level, at.east(21), wood.leaves.get().defaultBlockState().setValue(LeavesBlock.PERSISTENT, true));
            set(level, at.east(23).below(), Blocks.SAND.defaultBlockState());
            set(level, at.east(23), wood.sapling.get().defaultBlockState().setValue(SeaSaplingBlock.WATERLOGGED, false));
            set(level, at.east(25), wood.pottedSapling.get().defaultBlockState());
            row++;
        }
        // --- das Becken: Glaswände, Sandgrund, Wasser ---
        for (int x = -1; x <= TANK_X; x++) {
            for (int z = -1; z <= TANK_Z; z++) {
                boolean wall = x == -1 || z == -1 || x == TANK_X || z == TANK_Z;
                BlockPos foot = new BlockPos(BASE_X + x, ground - 1, z);
                set(level, foot, wall ? Blocks.GLASS.defaultBlockState() : Blocks.SAND.defaultBlockState());
                for (int y = 0; y < TANK_Y; y++) {
                    set(level, foot.above(1 + y), wall ? Blocks.GLASS.defaultBlockState() : Blocks.WATER.defaultBlockState());
                }
            }
        }
        // ein wenig Seegras und Sand zwischen den Bäumen
        RandomSource random = RandomSource.create(9_200L);
        for (int k = 0; k < 140; k++) {
            int x = random.nextInt(TANK_X);
            int z = random.nextInt(TANK_Z);
            BlockPos p = new BlockPos(BASE_X + x, ground, z);
            if (level.getBlockState(p).is(Blocks.WATER)) {
                set(level, p, Blocks.SEAGRASS.defaultBlockState());
            }
        }
        // --- die Bäume ---
        SeaTreeFeature.Kind[] kinds = SeaTreeFeature.Kind.values();
        for (int i = 0; i < kinds.length; i++) {
            BlockPos origin = new BlockPos(BASE_X + 7 + i * 14, ground, 7);
            set(level, origin, Blocks.WATER.defaultBlockState());
            boolean placed = false;
            for (int attempt = 0; attempt < 8 && !placed; attempt++) {
                placed = new SeaTreeFeature(kinds[i]).place(level, level.getChunkSource().getGenerator(), RandomSource.create(4_000L + i * 31 + attempt),
                        origin);
            }
            KingdomsAtWar.LOGGER.info("[Showcase] Meeresbaum {}: {}", kinds[i].getSerializedName(), placed);
        }
    }
}
