package io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HangingMossBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * Ein Baum des Nebelmoors: eine krumme Moorbirke mit lichter, zerzauster Krone aus kleinen Laubbüscheln – oder
 * ({@code dead}) ein abgestorbener, dunkler Baum: der Stamm oben abgebrochen, ringsum knorrige Äste, die schräg
 * ausgreifen und an den Enden nach oben knicken, am Fuß freiliegende Wurzeln. An beiden hängt fahles Moos.
 */
public record MoorTreeFeature(boolean dead) implements Feature {
    public static final MapCodec<MoorTreeFeature> CODEC = Codec.BOOL.fieldOf("dead").xmap(MoorTreeFeature::new, MoorTreeFeature::dead);

    /** Der goldene Winkel: Äste, die ihm folgen, verteilen sich gleichmäßig rund um den Stamm. */
    private static final double GOLDEN = Math.PI * (3.0 - Math.sqrt(5.0));

    @Override
    public MapCodec<MoorTreeFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        if (!Kit.dryGround(level, origin)) {
            return false;
        }
        BlockState log = (this.dead ? Blocks.DARK_OAK_LOG : Blocks.BIRCH_LOG).defaultBlockState();
        BlockState leaf = Blocks.BIRCH_LEAVES.defaultBlockState();
        Set<BlockPos> logs = new HashSet<>();
        Set<BlockPos> leaves = new HashSet<>();
        int height = this.dead ? 6 + random.nextInt(5) : 6 + random.nextInt(4);
        // der Stamm: wandert oben ein wenig zur Seite
        double driftX = (random.nextDouble() - 0.5) * (this.dead ? 0.7 : 0.45);
        double driftZ = (random.nextDouble() - 0.5) * (this.dead ? 0.7 : 0.45);
        double[][] trunk = new double[height][];
        double x = origin.getX() + 0.5;
        double z = origin.getZ() + 0.5;
        int built = 0;
        for (int k = 0; k < height; k++) {
            BlockPos pos = BlockPos.containing(x, origin.getY() + k, z);
            if (!Kit.setIfOpen(level, pos, log)) {
                if (k == 0) {
                    return false;
                }
                break;
            }
            logs.add(pos);
            trunk[k] = new double[] {x, origin.getY() + k + 0.5, z};
            built = k + 1;
            if (k > 1 && random.nextFloat() < 0.55f) {
                BlockPos before = pos;
                x += driftX;
                z += driftZ;
                // ein Seitenschritt bleibt mit dem Stamm verbunden
                BlockPos side = BlockPos.containing(x, origin.getY() + k, z);
                if (!side.equals(before) && Kit.setIfOpen(level, side, log)) {
                    logs.add(side);
                }
            }
        }
        if (built < 4) {
            return true;
        }
        double turn = random.nextDouble() * Math.PI * 2.0;
        if (this.dead) {
            int branches = 2 + random.nextInt(2);
            for (int b = 0; b < branches; b++) {
                int at = Math.min(built - 1, (int) (built * (0.35 + 0.5 * b / Math.max(1, branches - 1))) + random.nextInt(2));
                double[] start = trunk[at];
                double a = turn + b * GOLDEN * 2.0 + (random.nextDouble() - 0.5) * 0.4;
                double len = 1.8 + random.nextDouble() * 1.4;
                double ex = start[0] + Math.cos(a) * len;
                double ez = start[2] + Math.sin(a) * len;
                double ey = start[1] + len * (0.6 + random.nextDouble() * 0.4);
                Kit.branch(level, logs, log, start[0], start[1], start[2], ex, ey, ez);
                if (random.nextFloat() < 0.6f) {
                    // die Spitze knickt nach oben
                    Kit.branch(level, logs, log, ex, ey + 1.0, ez, ex, ey + 1.0 + random.nextInt(2), ez);
                }
            }
            // abgebrochene Spitze: ein kurzer, schief sitzender Stummel
            double[] crown = trunk[built - 1];
            double a = random.nextDouble() * Math.PI * 2.0;
            Kit.branch(level, logs, log, crown[0], crown[1], crown[2], crown[0] + Math.cos(a) * 1.1, crown[1] + 1.4, crown[2] + Math.sin(a) * 1.1);
            // freiliegende Wurzeln
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                if (random.nextFloat() < 0.55f) {
                    BlockPos root = origin.relative(dir);
                    if (Kit.open(level.getBlockState(root)) && Kit.ground(level.getBlockState(root.below()))) {
                        level.setBlock(root, Kit.log(log, dir.getAxis()), 2);
                        logs.add(root);
                    }
                }
            }
        } else {
            // zerzauste Krone aus kleinen Büscheln an kurzen Ästen, dazu ein Büschel auf der Spitze
            int tufts = 3 + random.nextInt(2);
            for (int b = 0; b < tufts; b++) {
                int at = Math.min(built - 1, (int) (built * (0.5 + 0.4 * b / Math.max(1, tufts - 1))));
                double[] start = trunk[at];
                double a = turn + b * GOLDEN * 2.0;
                double len = 1.2 + random.nextDouble() * 1.3;
                double ex = start[0] + Math.cos(a) * len;
                double ez = start[2] + Math.sin(a) * len;
                double ey = start[1] + len * 0.6;
                Kit.branch(level, logs, log, start[0], start[1], start[2], ex, ey, ez);
                Kit.leafBlob(level, leaves, leaf, ex, ey + 0.6, ez, 1.5 + random.nextDouble() * 0.6, 1.2 + random.nextDouble() * 0.4, random, 0.5);
            }
            double[] crown = trunk[built - 1];
            Kit.leafBlob(level, leaves, leaf, crown[0], crown[1] + 1.2, crown[2], 1.8, 1.5, random, 0.45);
        }
        // fahles Moos hängt von Ästen und Laub
        Set<BlockPos> holders = new HashSet<>(logs);
        holders.addAll(leaves);
        for (BlockPos holder : holders) {
            if (holder.getY() < origin.getY() + 2 || random.nextFloat() > (this.dead ? 0.3f : 0.14f)) {
                continue;
            }
            BlockPos below = holder.below();
            if (!level.getBlockState(below).isAir()) {
                continue;
            }
            int length = 1 + random.nextInt(3);
            for (int k = 0; k < length; k++) {
                BlockPos at = below.below(k);
                if (!level.getBlockState(at).isAir()) {
                    break;
                }
                boolean tip = k == length - 1 || !level.getBlockState(at.below()).isAir();
                level.setBlock(at, Blocks.PALE_HANGING_MOSS.defaultBlockState().setValue(HangingMossBlock.TIP, tip), 2);
                if (tip) {
                    break;
                }
            }
        }
        if (!leaves.isEmpty()) {
            Kit.settleLeaves(level, logs, leaves);
        }
        return true;
    }
}
