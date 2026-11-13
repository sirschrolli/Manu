package io.github.sirschrolli.kingdomsatwar.voidrealm.feature;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * Reste einer verlorenen Welt auf einem Bruchstück der Weltenreste – je nachdem, welche Welt es war:
 * <ul>
 *     <li>auf Wiese: ein toter Baum oder die Ecke eines Bauernhauses mit eingestürztem Dach, ein Zaunrest, ein Brunnen;</li>
 *     <li>auf Wüstensand: halb verschüttete Sandsteinsäulen, tote Büsche, ein Stück Mauer;</li>
 *     <li>auf Schnee: Fichtenstümpfe, eine kleine Eisnadel, eine eingefallene Steinmauer.</li>
 * </ul>
 */
public record WorldRemnantFeature() implements Feature {
    public static final MapCodec<WorldRemnantFeature> CODEC = MapCodec.unit(WorldRemnantFeature::new);

    @Override
    public MapCodec<WorldRemnantFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        BlockState ground = level.getBlockState(origin.below());
        if (ground.is(Blocks.GRASS_BLOCK)) {
            if (random.nextInt(3) == 0) {
                cottage(level, origin, random);
            } else {
                deadTree(level, origin, random, Blocks.OAK_LOG.defaultBlockState(), 5 + random.nextInt(4));
            }
            return true;
        }
        if (ground.is(Blocks.SAND)) {
            desertRuin(level, origin, random);
            return true;
        }
        if (ground.is(Blocks.SNOW_BLOCK)) {
            if (random.nextBoolean()) {
                deadTree(level, origin, random, Blocks.SPRUCE_LOG.defaultBlockState(), 2 + random.nextInt(3));
            } else {
                iceSpike(level, origin, random);
            }
            return true;
        }
        return false;
    }

    private static void deadTree(WorldGenLevel level, BlockPos origin, RandomSource random, BlockState log, int height) {
        for (int y = 0; y < height; y++) {
            VoidBuild.place(level, origin, origin.above(y), log);
        }
        int branches = height > 4 ? 2 + random.nextInt(2) : 0;
        for (int k = 0; k < branches; k++) {
            Direction d = Direction.Plane.HORIZONTAL.getRandomDirection(random);
            BlockPos start = origin.above(height - 2 - random.nextInt(2));
            for (int l = 1; l <= 1 + random.nextInt(3); l++) {
                VoidBuild.place(level, origin, start.relative(d, l).above(l / 2), log.setValue(RotatedPillarBlock.AXIS, d.getAxis()));
            }
        }
    }

    private static void cottage(WorldGenLevel level, BlockPos origin, RandomSource random) {
        BlockState cobble = Blocks.COBBLESTONE.defaultBlockState();
        BlockState mossy = Blocks.MOSSY_COBBLESTONE.defaultBlockState();
        BlockState planks = Blocks.OAK_PLANKS.defaultBlockState();
        BlockState log = Blocks.OAK_LOG.defaultBlockState();
        BlockState roof = Blocks.DARK_OAK_STAIRS.defaultBlockState();
        Direction a = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        Direction b = a.getClockWise();
        // zwei Wände einer Hausecke, nach außen hin abgebrochen
        for (int i = 0; i < 6; i++) {
            int h = Math.max(1, 4 - i / 2 - random.nextInt(2));
            for (int y = 0; y < h; y++) {
                BlockState wall = y == 0 ? (random.nextBoolean() ? cobble : mossy) : planks;
                if (i == 0) {
                    wall = log;
                }
                VoidBuild.place(level, origin, origin.relative(a, i).above(y), wall);
                if (i > 0) {
                    VoidBuild.place(level, origin, origin.relative(b, i).above(y), i == 2 && y == 1 ? Blocks.GLASS_PANE.defaultBlockState() : wall);
                }
            }
        }
        // eingestürztes Dach: ein paar Treppenstufen schräg auf den Mauerresten
        for (int i = 0; i < 4; i++) {
            BlockPos at = origin.relative(a, i).relative(b, 1).above(4 - i / 2);
            VoidBuild.place(level, origin, at, roof.setValue(StairBlock.FACING, b.getOpposite()).setValue(StairBlock.HALF, Half.BOTTOM));
        }
        // Zaunrest und eine umgestürzte Truhe der alten Bewohner
        for (int i = 2; i < 5; i++) {
            if (random.nextInt(3) > 0) {
                VoidBuild.place(level, origin, origin.relative(a, i).relative(b, -2), Blocks.OAK_FENCE.defaultBlockState());
            }
        }
        if (random.nextBoolean()) {
            VoidBuild.place(level, origin, origin.relative(a, 1).relative(b, 1), Blocks.BARREL.defaultBlockState());
        }
    }

    private static void desertRuin(WorldGenLevel level, BlockPos origin, RandomSource random) {
        BlockState pillar = Blocks.CUT_SANDSTONE.defaultBlockState();
        BlockState chiseled = Blocks.CHISELED_SANDSTONE.defaultBlockState();
        for (int k = 0; k < 3; k++) {
            BlockPos base = origin.offset(random.nextInt(7) - 3, 0, random.nextInt(7) - 3);
            int h = 1 + random.nextInt(4);
            for (int y = 0; y < h; y++) {
                VoidBuild.place(level, origin, base.above(y), y == h - 1 && random.nextBoolean() ? chiseled : pillar);
            }
        }
        Direction d = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        for (int i = 0; i < 4; i++) {
            VoidBuild.place(level, origin, origin.relative(d, i).relative(d.getClockWise(), 3), Blocks.SANDSTONE_WALL.defaultBlockState());
        }
        for (int k = 0; k < 3; k++) {
            BlockPos at = origin.offset(random.nextInt(9) - 4, 0, random.nextInt(9) - 4);
            if (level.getBlockState(at.below()).is(Blocks.SAND)) {
                VoidBuild.place(level, origin, at, Blocks.DEAD_BUSH.defaultBlockState());
            }
        }
    }

    private static void iceSpike(WorldGenLevel level, BlockPos origin, RandomSource random) {
        int h = 4 + random.nextInt(5);
        BlockState ice = Blocks.PACKED_ICE.defaultBlockState();
        for (int y = 0; y < h; y++) {
            double r = 1.5 * (1.0 - y / (double) h);
            int ir = (int) Math.ceil(r);
            for (int dx = -ir; dx <= ir; dx++) {
                for (int dz = -ir; dz <= ir; dz++) {
                    if (dx * dx + dz * dz <= r * r + 0.2) {
                        VoidBuild.place(level, origin, origin.offset(dx, y, dz), ice);
                    }
                }
            }
        }
        // eine eingefallene Steinmauer daneben
        Direction d = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        for (int i = 0; i < 4; i++) {
            int wh = random.nextInt(3);
            for (int y = 0; y < wh; y++) {
                VoidBuild.place(level, origin, origin.relative(d, 3).relative(d.getClockWise(), i - 1).above(y),
                        Blocks.STONE_BRICKS.defaultBlockState());
            }
        }
    }
}
