package io.github.sirschrolli.kingdomsatwar.fae.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * Die Dornenwildnis:
 * <ul>
 *     <li><b>Dornbaum</b> – ein schwarzer, verdrehter Stamm, dessen knorrige Äste in Ballen aus violetten Dornenhecken
 *     enden; aus ihnen hängen Leuchtranken;</li>
 *     <li><b>Dornendickicht</b> – kniehohe bis mannshohe Wälle aus Dornenhecken, die sich in Bögen über den Boden ziehen,
 *     dazwischen Dornenranken.</li>
 * </ul>
 */
public record ThornTreeFeature(Kind kind) implements Feature {
    public static final MapCodec<ThornTreeFeature> CODEC = Kind.CODEC.fieldOf("kind").xmap(ThornTreeFeature::new, ThornTreeFeature::kind);

    public enum Kind implements StringRepresentable {
        TREE("tree"),
        THICKET("thicket");

        public static final Codec<Kind> CODEC = StringRepresentable.fromEnum(Kind::values);
        private final String name;

        Kind(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    @Override
    public MapCodec<ThornTreeFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        if (!FaeBuild.soil(level.getBlockState(origin.below())) || !FaeBuild.open(level.getBlockState(origin))) {
            return false;
        }
        return this.kind == Kind.TREE ? tree(level, random, origin) : thicket(level, random, origin);
    }

    private static boolean tree(WorldGenLevel level, RandomSource random, BlockPos origin) {
        BlockState log = Blocks.DARK_OAK_LOG.defaultBlockState();
        BlockState wood = Blocks.DARK_OAK_WOOD.defaultBlockState();
        BlockState hedge = FaeBlocks.THORN_HEDGE.get().defaultBlockState();
        FaeTree tree = new FaeTree(origin);
        int height = 5 + random.nextInt(4);
        // der Stamm windet sich: alle ein, zwei Blöcke ein Stück zur Seite
        double x = origin.getX() + 0.5;
        double z = origin.getZ() + 0.5;
        double y = origin.getY();
        double heading = random.nextDouble() * Math.PI * 2.0;
        java.util.List<double[]> trunk = new java.util.ArrayList<>();
        trunk.add(new double[] {x, y, z});
        for (int i = 0; i < height; i += 2) {
            heading += (random.nextDouble() - 0.5) * 2.2;
            double nx = x + Math.cos(heading) * 0.9;
            double nz = z + Math.sin(heading) * 0.9;
            tree.branch(log, x, y, z, nx, y + 2, nz);
            x = nx;
            z = nz;
            y += 2;
            trunk.add(new double[] {x, y, z});
        }
        // knorrige Äste mit einem Knick, an den Enden Dornenballen
        int branches = 2 + random.nextInt(3);
        for (int b = 0; b < branches; b++) {
            double[] start = trunk.get(Math.max(1, trunk.size() - 1 - random.nextInt(Math.min(3, trunk.size() - 1))));
            double a = random.nextDouble() * Math.PI * 2.0;
            double l1 = 1.5 + random.nextDouble() * 1.5;
            double kx = start[0] + Math.cos(a) * l1;
            double kz = start[2] + Math.sin(a) * l1;
            double ky = start[1] + 0.5 + random.nextDouble();
            tree.branch(wood, start[0], start[1], start[2], kx, ky, kz);
            a += (random.nextDouble() - 0.5) * 1.6;
            double l2 = 1.2 + random.nextDouble() * 1.4;
            BlockPos tip = tree.branch(wood, kx, ky, kz, kx + Math.cos(a) * l2, ky + 1.0 + random.nextDouble(), kz + Math.sin(a) * l2);
            tree.blob(tip, 1.1 + random.nextDouble() * 0.5, 0.9, hedge, random, 0.55);
        }
        BlockPos top = BlockPos.containing(x, y, z);
        tree.blob(top.above(), 1.3 + random.nextDouble() * 0.4, 1.0, hedge, random, 0.5);
        if (!tree.place(level, random, 0.18f, 4)) {
            return false;
        }
        briars(level, random, origin, 4, 6);
        return true;
    }

    private static boolean thicket(WorldGenLevel level, RandomSource random, BlockPos origin) {
        BlockState hedge = FaeBlocks.THORN_HEDGE.get().defaultBlockState();
        int length = 6 + random.nextInt(8);
        double heading = random.nextDouble() * Math.PI * 2.0;
        double x = origin.getX() + 0.5;
        double z = origin.getZ() + 0.5;
        int placed = 0;
        for (int i = 0; i < length; i++) {
            heading += (random.nextDouble() - 0.5) * 0.9;
            x += Math.cos(heading);
            z += Math.sin(heading);
            int width = random.nextInt(3) == 0 ? 2 : 1;
            for (int w = 0; w < width; w++) {
                int bx = (int) Math.floor(x + (w == 0 ? 0 : -Math.sin(heading)));
                int bz = (int) Math.floor(z + (w == 0 ? 0 : Math.cos(heading)));
                BlockPos base = new BlockPos(bx, FaeBuild.surface(level, bx, bz), bz);
                if (Math.abs(base.getY() - origin.getY()) > 4 || !FaeBuild.soil(level.getBlockState(base.below()))) {
                    continue;
                }
                int tall = 1 + (random.nextFloat() < 0.55f ? 1 : 0) + (random.nextFloat() < 0.12f ? 1 : 0);
                for (int h = 0; h < tall; h++) {
                    if (FaeBuild.place(level, origin, base.above(h), hedge)) {
                        placed++;
                    }
                }
            }
        }
        briars(level, random, origin, 5, 8);
        return placed > 0;
    }

    /** Dornenranken rundum am Boden. */
    private static void briars(WorldGenLevel level, RandomSource random, BlockPos origin, int spread, int count) {
        BlockState briar = FaeBlocks.THORNBRIAR.get().defaultBlockState();
        for (int i = 0; i < count; i++) {
            int x = origin.getX() + random.nextInt(spread * 2 + 1) - spread;
            int z = origin.getZ() + random.nextInt(spread * 2 + 1) - spread;
            BlockPos pos = new BlockPos(x, FaeBuild.surface(level, x, z), z);
            if (Math.abs(pos.getY() - origin.getY()) <= 3) {
                FaeBuild.plant(level, origin, pos, briar);
            }
        }
    }
}
