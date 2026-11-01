package io.github.sirschrolli.kingdomsatwar.worldgen.site;

import io.github.sirschrolli.kingdomsatwar.loot.ModLootTables;
import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.VillageBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.BaseCoralWallFanBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * Der Gezeitenschrein: Am Strand, halb vom Sand verweht, steht ein kleiner Rundtempel aus Prismarin – acht Säulen, auf
 * einigen noch Reste des Gebälks, in der Mitte ein Altar mit einem Becken, das von unten leuchtet. Wer unter dem Altar
 * gräbt, findet die versiegelte Kammer mit dem Opferschatz: dem Herz des Meeres. Die Seekarten aus den Gebeinen der
 * Seedrachen zeigen hierher (das rote Kreuz liegt auf dem Altar).
 *
 * <p>Eigene Koordinaten: Mitte des Altars bei (0, 0, 0), y = 0 ist die Oberfläche des Strands.
 */
final class TideShrine {
    private static final int[][] PILLARS = {{4, 0}, {3, 3}, {0, 4}, {-3, 3}, {-4, 0}, {-3, -3}, {0, -4}, {3, -3}};

    private TideShrine() {}

    static Optional<Structure.GenerationStub> plan(Structure.GenerationContext context, SiteTerrain terrain, long seed) {
        ChunkPos chunk = context.chunkPos();
        int x = chunk.getBlockX(9);
        int z = chunk.getBlockZ(9);
        int ground = terrain.ground(x, z);
        int[] span = terrain.span(x, z, 4, 4);
        // nur auf trockenem, ebenem Strand knapp über dem Meer
        if (terrain.water(x, z) || ground < terrain.seaLevel() - 1 || ground > terrain.seaLevel() + 5 || span[1] - span[0] > 2) {
            return Optional.empty();
        }
        BlockPos center = new BlockPos(x, ground, z);
        Rotation rotation = Rotation.values()[Math.floorMod((int) (seed >>> 9), 4)];
        BoundingBox box = new BoundingBox(x - 6, ground - 5, z - 6, x + 6, ground + 10, z + 6);
        return Optional.of(new Structure.GenerationStub(center,
                builder -> builder.addPiece(new SitePiece(SiteKind.TIDE_SHRINE, 0, center, rotation, seed, new int[0], box))));
    }

    static void build(VillageBuilder b, SitePiece piece) {
        // Fundament und versiegelte Kammer
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                for (int y = -5; y <= -1; y++) {
                    boolean chamber = Math.abs(x) <= 1 && Math.abs(z) <= 1 && y >= -3 && y <= -2;
                    b.set(x, y, z, chamber ? Blocks.AIR.defaultBlockState()
                            : Math.abs(x) <= 2 && Math.abs(z) <= 2 ? Blocks.PRISMARINE_BRICKS.defaultBlockState() : Blocks.SANDSTONE.defaultBlockState());
                }
            }
        }
        b.set(0, -2, -2, Blocks.SEA_LANTERN);
        b.set(0, -2, 2, Blocks.SEA_LANTERN);
        b.chest(0, -3, 0, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH), ModLootTables.SITE_TIDE_SHRINE);
        b.set(-1, -3, -1, Blocks.SEA_PICKLE.defaultBlockState().setValue(SeaPickleBlock.PICKLES, 3)
                .setValue(BlockStateProperties.WATERLOGGED, false));
        b.set(1, -3, 1, Blocks.SEA_PICKLE.defaultBlockState().setValue(SeaPickleBlock.PICKLES, 2)
                .setValue(BlockStateProperties.WATERLOGGED, false));
        // die runde Plattform: Prismarinziegel, außen ein Ring aus dunklem Prismarin, hier und da Sand darüber
        for (int x = -5; x <= 5; x++) {
            for (int z = -5; z <= 5; z++) {
                double r = Math.sqrt(x * x + z * z);
                if (r > 5.2) {
                    continue;
                }
                BlockState top = r > 4.2 ? Blocks.DARK_PRISMARINE.defaultBlockState()
                        : (Math.floorMod(x + z, 2) == 0 ? Blocks.PRISMARINE_BRICKS.defaultBlockState() : Blocks.PRISMARINE.defaultBlockState());
                if (r > 2.5 && b.chance(x, 0, z, 0.18f)) {
                    top = Blocks.SAND.defaultBlockState();
                }
                b.set(x, 0, z, top);
                b.set(x, -1, z, r > 3.5 ? Blocks.SANDSTONE.defaultBlockState() : b.get(x, -1, z));
                for (int y = 1; y <= 9; y++) {
                    b.set(x, y, z, Blocks.AIR.defaultBlockState());
                }
            }
        }
        // Säulen mit Kapitellen, einige gebrochen; auf manchen Paaren liegt noch ein Stück Gebälk
        boolean[] whole = new boolean[PILLARS.length];
        for (int i = 0; i < PILLARS.length; i++) {
            int[] p = PILLARS[i];
            whole[i] = !b.chance(p[0], 9, p[1], 0.35f);
            int h = whole[i] ? 4 : 1 + (int) (b.noise(p[0], 8, p[1]) * 3);
            for (int y = 1; y <= h; y++) {
                b.set(p[0], y, p[1], y == 1 ? Blocks.DARK_PRISMARINE.defaultBlockState() : Blocks.PRISMARINE_BRICKS.defaultBlockState());
            }
            if (whole[i]) {
                b.set(p[0], 5, p[1], Blocks.DARK_PRISMARINE.defaultBlockState());
            } else {
                b.set(p[0], h + 1, p[1], Blocks.PRISMARINE_BRICK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM));
                // ein Trümmerstück daneben im Sand
                int fx = p[0] + Integer.signum(p[0]);
                int fz = p[1] + Integer.signum(p[1]);
                b.set(fx, 1, fz, Blocks.PRISMARINE_BRICK_STAIRS.defaultBlockState()
                        .setValue(net.minecraft.world.level.block.StairBlock.FACING, Direction.from2DDataValue(i & 3)));
            }
        }
        for (int i = 0; i < PILLARS.length; i++) {
            int j = (i + 1) % PILLARS.length;
            if (!whole[i] || !whole[j]) {
                continue;
            }
            int[] a = PILLARS[i];
            int[] c = PILLARS[j];
            // Gebälk aus Platten zwischen zwei Säulen
            int steps = Math.max(Math.abs(c[0] - a[0]), Math.abs(c[1] - a[1]));
            for (int s = 1; s < steps; s++) {
                int x = a[0] + (c[0] - a[0]) * s / steps;
                int z = a[1] + (c[1] - a[1]) * s / steps;
                b.set(x, 5, z, Blocks.PRISMARINE_BRICK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP));
            }
        }
        // der Altar: ein Becken auf einem leuchtenden Stein, eingefasst von Platten, davor Stufen
        b.set(0, 0, 0, Blocks.SEA_LANTERN);
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) {
                    b.set(0, 1, 0, Blocks.WATER.defaultBlockState());
                } else {
                    b.set(x, 1, z, Blocks.DARK_PRISMARINE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM));
                }
            }
        }
        for (int x = -1; x <= 1; x++) {
            b.set(x, 1, 2, VillageBuilder.stairs(Blocks.PRISMARINE_BRICK_STAIRS, Direction.NORTH, Half.BOTTOM));
        }
        // hinter dem Altar das Zeichen der Gezeiten: ein Dreizack aus Prismarin (Schaft, Querstück, drei Zinken)
        for (int y = 1; y <= 5; y++) {
            b.set(0, y, -2, Blocks.PRISMARINE_WALL.defaultBlockState());
        }
        b.set(0, 6, -2, Blocks.DARK_PRISMARINE.defaultBlockState());
        b.set(-1, 6, -2, Blocks.DARK_PRISMARINE_STAIRS.defaultBlockState().setValue(net.minecraft.world.level.block.StairBlock.FACING,
                Direction.EAST).setValue(net.minecraft.world.level.block.StairBlock.HALF, Half.TOP));
        b.set(1, 6, -2, Blocks.DARK_PRISMARINE_STAIRS.defaultBlockState().setValue(net.minecraft.world.level.block.StairBlock.FACING,
                Direction.WEST).setValue(net.minecraft.world.level.block.StairBlock.HALF, Half.TOP));
        for (int y = 7; y <= 8; y++) {
            b.set(-1, y, -2, Blocks.PRISMARINE_WALL.defaultBlockState());
            b.set(1, y, -2, Blocks.PRISMARINE_WALL.defaultBlockState());
        }
        for (int y = 7; y <= 9; y++) {
            b.set(0, y, -2, Blocks.PRISMARINE_WALL.defaultBlockState());
        }
        // Gaben und Schmuck: Seegurken auf den Platten, Korallenfächer an den Säulen
        b.set(1, 2, -1, Blocks.SEA_PICKLE.defaultBlockState().setValue(SeaPickleBlock.PICKLES, 2).setValue(BlockStateProperties.WATERLOGGED, false));
        b.set(-1, 2, 1, Blocks.SEA_PICKLE.defaultBlockState().setValue(SeaPickleBlock.PICKLES, 1).setValue(BlockStateProperties.WATERLOGGED, false));
        for (int i = 0; i < PILLARS.length; i += 2) {
            int[] p = PILLARS[i];
            Direction out = Math.abs(p[0]) > Math.abs(p[1]) ? (p[0] > 0 ? Direction.EAST : Direction.WEST) : (p[1] > 0 ? Direction.SOUTH : Direction.NORTH);
            if (b.chance(p[0], 2, p[1], 0.7f)) {
                b.set(p[0] + out.getStepX(), 2, p[1] + out.getStepZ(), Blocks.DEAD_TUBE_CORAL_WALL_FAN.defaultBlockState()
                        .setValue(BaseCoralWallFanBlock.FACING, out).setValue(BlockStateProperties.WATERLOGGED, false));
            }
        }
    }
}
