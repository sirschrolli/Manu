package io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature;

import com.mojang.serialization.MapCodec;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * Ein verkohlter Baum der Aschefelder: ein schwarzer Stamm, der sich leicht windet, mit zwei bis vier kahlen, nach
 * oben gereckten Ästen; manche sind oben abgebrochen. Am Fuß häuft sich Asche, ein, zwei Wurzeln liegen frei.
 */
public record CharredTreeFeature() implements Feature {
    public static final MapCodec<CharredTreeFeature> CODEC = MapCodec.unit(CharredTreeFeature::new);

    @Override
    public MapCodec<CharredTreeFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        if (!Kit.dryGround(level, origin)) {
            return false;
        }
        BlockState log = ModBlocks.CHARRED_LOG.get().defaultBlockState();
        Set<BlockPos> logs = new HashSet<>();
        boolean broken = random.nextFloat() < 0.25f;
        int height = broken ? 3 + random.nextInt(3) : 6 + random.nextInt(5);
        // Stamm mit leichtem Schwung
        double x = origin.getX() + 0.5;
        double z = origin.getZ() + 0.5;
        double driftX = (random.nextDouble() - 0.5) * 0.35;
        double driftZ = (random.nextDouble() - 0.5) * 0.35;
        double[][] trunk = new double[height][];
        BlockPos previous = null;
        for (int k = 0; k < height; k++) {
            BlockPos pos = BlockPos.containing(x, origin.getY() + k, z);
            if (!Kit.setIfOpen(level, pos, log) && k == 0) {
                return false;
            }
            logs.add(pos);
            // rückt der Stamm zur Seite, hält ein Block auf der alten Höhe ihn zusammen
            if (previous != null && (previous.getX() != pos.getX() || previous.getZ() != pos.getZ())) {
                BlockPos joint = new BlockPos(pos.getX(), previous.getY(), pos.getZ());
                if (Kit.setIfOpen(level, joint, log)) {
                    logs.add(joint);
                }
                if (previous.getX() != pos.getX() && previous.getZ() != pos.getZ()) {
                    BlockPos corner = new BlockPos(pos.getX(), previous.getY(), previous.getZ());
                    if (Kit.setIfOpen(level, corner, log)) {
                        logs.add(corner);
                    }
                }
            }
            previous = pos;
            trunk[k] = new double[] {x, origin.getY() + k, z};
            if (k > 1) {
                x += driftX;
                z += driftZ;
            }
        }
        // Äste: schräg nach oben, mit einem Knick
        int branches = broken ? 1 + random.nextInt(2) : 3 + random.nextInt(3);
        for (int b = 0; b < branches; b++) {
            int from = Math.max(1, (int) (height * (0.45 + random.nextDouble() * 0.45)));
            from = Math.min(from, height - 1);
            double[] start = trunk[from];
            double a = random.nextDouble() * Math.PI * 2.0;
            double len = 2.0 + random.nextDouble() * 2.5;
            double ex = start[0] + Math.cos(a) * len;
            double ez = start[2] + Math.sin(a) * len;
            double ey = start[1] + len * (0.5 + random.nextDouble() * 0.6);
            Kit.branch(level, logs, log, start[0], start[1], start[2], ex, ey, ez);
            if (random.nextBoolean()) {
                // kurzer Zweig vom Astende
                double a2 = a + (random.nextBoolean() ? 0.9 : -0.9);
                Kit.branch(level, logs, log, ex, ey, ez, ex + Math.cos(a2) * 1.5, ey + 1.2, ez + Math.sin(a2) * 1.5);
            }
        }
        // freiliegende Wurzeln
        int roots = random.nextInt(3);
        for (int r = 0; r < roots; r++) {
            Direction dir = Direction.Plane.HORIZONTAL.getRandomDirection(random);
            BlockPos root = origin.relative(dir);
            if (Kit.open(level.getBlockState(root)) && Kit.ground(level.getBlockState(root.below()))) {
                level.setBlock(root, Kit.log(log, dir.getAxis()), 2);
            }
        }
        // Asche am Fuß
        BlockState ash = ModBlocks.ASH_LAYER.get().defaultBlockState();
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                int d = Math.abs(dx) + Math.abs(dz);
                if (d == 0 || d > 3 || random.nextFloat() < 0.35f) {
                    continue;
                }
                BlockPos p = origin.offset(dx, 0, dz);
                for (int k = 1; k >= -1; k--) {
                    BlockPos at = p.above(k);
                    if (level.getBlockState(at).isAir() && Kit.ground(level.getBlockState(at.below()))) {
                        int layers = Math.max(1, 4 - d + random.nextInt(2));
                        level.setBlock(at, ash.setValue(SnowLayerBlock.LAYERS, Math.min(8, layers)), 2);
                        break;
                    }
                }
            }
        }
        return true;
    }
}
