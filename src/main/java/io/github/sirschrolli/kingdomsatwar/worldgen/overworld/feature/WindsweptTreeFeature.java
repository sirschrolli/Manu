package io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature;

import com.mojang.serialization.MapCodec;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * Eine Windkiefer der Sturmküste: vom ewigen Wind gebeugt, der Stamm schräg, die Krone flach und fahnenartig nach Lee
 * gezogen. Alle Bäume einer Gegend neigen sich in dieselbe Richtung – dorthin weht der Wind.
 */
public record WindsweptTreeFeature() implements Feature {
    public static final MapCodec<WindsweptTreeFeature> CODEC = MapCodec.unit(WindsweptTreeFeature::new);

    @Override
    public MapCodec<WindsweptTreeFeature> codec() {
        return CODEC;
    }

    /** Die Windrichtung einer Gegend (Bogenmaß): gleich in großen Feldern, damit die Bäume beieinander gleich stehen. */
    static double wind(BlockPos pos) {
        long cell = Mth.getSeed(pos.getX() >> 8, 0, pos.getZ() >> 8);
        return (cell & 0xFFFF) / 65536.0 * Math.PI * 2.0;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        if (!Kit.dryGround(level, origin)) {
            return false;
        }
        BlockState log = Blocks.SPRUCE_LOG.defaultBlockState();
        BlockState leaf = Blocks.SPRUCE_LEAVES.defaultBlockState();
        Set<BlockPos> logs = new HashSet<>();
        Set<BlockPos> leaves = new HashSet<>();
        double angle = wind(origin) + (random.nextDouble() - 0.5) * 0.5;
        double wx = Math.cos(angle);
        double wz = Math.sin(angle);
        int height = 4 + random.nextInt(4);
        double slant = 0.45 + random.nextDouble() * 0.25;
        double x = origin.getX() + 0.5;
        double z = origin.getZ() + 0.5;
        double y = origin.getY();
        double lastX = x;
        double lastZ = z;
        for (int k = 0; k < height; k++) {
            BlockPos pos = BlockPos.containing(x, y + k, z);
            if (!Kit.setIfOpen(level, pos, log)) {
                if (k == 0) {
                    return false;
                }
                break;
            }
            logs.add(pos);
            lastX = x;
            lastZ = z;
            if (k >= 1) {
                double nx = x + wx * slant;
                double nz = z + wz * slant;
                // ein Seitenschritt verbindet die Stammstücke, wenn der Stamm einen Block weiter rückt
                BlockPos side = BlockPos.containing(nx, y + k, nz);
                if (!side.equals(pos) && Kit.setIfOpen(level, side, Kit.log(log, Kit.axis(wx, 0, wz)))) {
                    logs.add(side);
                }
                x = nx;
                z = nz;
            }
        }
        double topY = y + height;
        // fahnenartige Krone nach Lee, flach und breit
        Kit.leafBlob(level, leaves, leaf, lastX + wx * 1.6, topY - 0.5, lastZ + wz * 1.6, 2.8, 1.3, random, 0.25);
        Kit.leafBlob(level, leaves, leaf, lastX + wx * 3.2, topY - 1.0, lastZ + wz * 3.2, 1.9, 1.0, random, 0.3);
        Kit.leafBlob(level, leaves, leaf, lastX - wx * 0.3, topY + 0.3, lastZ - wz * 0.3, 1.5, 1.0, random, 0.3);
        // ein niedriger Seitenast nach Lee
        if (height > 4) {
            double by = y + height / 2.0;
            double bx = origin.getX() + 0.5 + wx * slant * height / 2.0;
            double bz = origin.getZ() + 0.5 + wz * slant * height / 2.0;
            Kit.branch(level, logs, log, bx, by, bz, bx + wx * 2.5, by + 0.8, bz + wz * 2.5);
            Kit.leafBlob(level, leaves, leaf, bx + wx * 3.0, by + 1.2, bz + wz * 3.0, 1.8, 0.9, random, 0.3);
        }
        Kit.settleLeaves(level, logs, leaves);
        return true;
    }
}
