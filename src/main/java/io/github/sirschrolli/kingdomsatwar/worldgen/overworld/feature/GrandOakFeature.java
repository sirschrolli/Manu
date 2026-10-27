package io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature;

import com.mojang.serialization.MapCodec;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * Eine Königseiche: der mächtige, frei stehende Baum der Königsauen. Ein kurzer, dicker Stamm auf Wurzelanläufen teilt
 * sich in fünf bis sieben ausladende Äste; die Krone ist weit und gewölbt, viel breiter als hoch – darunter liegt ein
 * Schattenkreis, in dem man im Sommer rasten möchte.
 */
public record GrandOakFeature() implements Feature {
    public static final MapCodec<GrandOakFeature> CODEC = MapCodec.unit(GrandOakFeature::new);

    @Override
    public MapCodec<GrandOakFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        if (!Kit.dryGround(level, origin)) {
            return false;
        }
        for (int dx = 0; dx < 2; dx++) {
            for (int dz = 0; dz < 2; dz++) {
                if (!Kit.ground(level.getBlockState(origin.offset(dx, -1, dz))) && !Kit.ground(level.getBlockState(origin.offset(dx, -2, dz)))) {
                    return false;
                }
            }
        }
        BlockState log = Blocks.OAK_LOG.defaultBlockState();
        BlockState leaf = Blocks.OAK_LEAVES.defaultBlockState();
        Set<BlockPos> logs = new HashSet<>();
        Set<BlockPos> leaves = new HashSet<>();
        int trunk = 5 + random.nextInt(3);
        // Stamm 2×2, im Boden verankert
        for (int k = -2; k < trunk; k++) {
            for (int dx = 0; dx < 2; dx++) {
                for (int dz = 0; dz < 2; dz++) {
                    BlockPos pos = origin.offset(dx, k, dz);
                    BlockState old = level.getBlockState(pos);
                    if (Kit.open(old) || k < 0 && Kit.ground(old)) {
                        level.setBlock(pos, log, 2);
                        logs.add(pos);
                    }
                }
            }
        }
        // Wurzelanläufe
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            int bx = origin.getX() + (dir.getStepX() > 0 ? 2 : dir.getStepX() < 0 ? -1 : random.nextInt(2));
            int bz = origin.getZ() + (dir.getStepZ() > 0 ? 2 : dir.getStepZ() < 0 ? -1 : random.nextInt(2));
            int len = 1 + random.nextInt(2);
            for (int s = 0; s < len; s++) {
                BlockPos pos = new BlockPos(bx + dir.getStepX() * s, origin.getY() + (s == 0 ? random.nextInt(2) : 0), bz + dir.getStepZ() * s);
                if (Kit.open(level.getBlockState(pos))) {
                    level.setBlock(pos, Kit.log(log, dir.getAxis()), 2);
                    logs.add(pos);
                }
            }
        }
        double cx = origin.getX() + 1.0;
        double cz = origin.getZ() + 1.0;
        double top = origin.getY() + trunk;
        // ausladende Äste mit Laubwolken
        int arms = 5 + random.nextInt(3);
        double turn = random.nextDouble() * Math.PI * 2.0;
        for (int a = 0; a < arms; a++) {
            double angle = turn + a * Math.PI * 2.0 / arms + (random.nextDouble() - 0.5) * 0.5;
            double len = 5.0 + random.nextDouble() * 3.0;
            double startY = top - 1 - random.nextInt(2);
            double midX = cx + Math.cos(angle) * len * 0.55;
            double midZ = cz + Math.sin(angle) * len * 0.55;
            double midY = startY + 2.2 + random.nextDouble();
            double endX = cx + Math.cos(angle) * len;
            double endZ = cz + Math.sin(angle) * len;
            double endY = midY + 0.3 + random.nextDouble() * 1.2;
            Kit.branch(level, logs, log, cx - 0.5, startY, cz - 0.5, midX, midY, midZ);
            Kit.branch(level, logs, log, midX, midY, midZ, endX, endY, endZ);
            Kit.leafBlob(level, leaves, leaf, endX, endY + 1.0, endZ, 3.4, 2.2, random, 0.22);
            Kit.leafBlob(level, leaves, leaf, midX, midY + 1.6, midZ, 2.8, 1.8, random, 0.25);
        }
        // die Kuppel über dem Stamm: höher als die Astenden, so wölbt sich die Krone
        Kit.branch(level, logs, log, cx - 0.5, top - 1, cz - 0.5, cx - 0.5, top + 3, cz - 0.5);
        Kit.leafBlob(level, leaves, leaf, cx, top + 5.0, cz, 4.4, 3.2, random, 0.2);
        Kit.settleLeaves(level, logs, leaves);
        return true;
    }
}
