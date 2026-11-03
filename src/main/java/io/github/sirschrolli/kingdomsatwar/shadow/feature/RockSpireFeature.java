package io.github.sirschrolli.kingdomsatwar.shadow.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * Felsnadel: eine schlanke, leicht geneigte Nadel aus Schattenstein mit dunklen Bändern aus Tiefschattenstein, die sich
 * nach oben verjüngt – auf hohen Nadeln liegt Aschschnee.
 */
public record RockSpireFeature(int minHeight, int maxHeight, float baseRadius) implements Feature {
    public static final MapCodec<RockSpireFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.intRange(3, 64).fieldOf("min_height").forGetter(RockSpireFeature::minHeight),
            Codec.intRange(3, 64).fieldOf("max_height").forGetter(RockSpireFeature::maxHeight),
            Codec.floatRange(0.5f, 6.0f).fieldOf("base_radius").forGetter(RockSpireFeature::baseRadius)
    ).apply(i, RockSpireFeature::new));

    @Override
    public MapCodec<RockSpireFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        if (io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.UmbrerVillageStructure.isNearVillage(level, origin, 6)) {
            return false;
        }
        BlockState ground = level.getBlockState(origin.below());
        if (!ground.isSolidRender() || !level.getFluidState(origin).isEmpty()) {
            return false;
        }
        int height = this.minHeight + random.nextInt(Math.max(1, this.maxHeight - this.minHeight + 1));
        double radius = this.baseRadius * (0.8 + random.nextDouble() * 0.4);
        double leanX = (random.nextDouble() - 0.5) * 0.5;
        double leanZ = (random.nextDouble() - 0.5) * 0.5;
        int band = random.nextInt(5);
        BlockState stone = ShadowBlocks.SHADOW_STONE.base().get().defaultBlockState();
        BlockState deep = ShadowBlocks.DEEP_SHADOW_STONE.get().defaultBlockState();
        // Fuß im Boden verankern
        for (int y = -3; y < height; y++) {
            double t = Math.max(0, y) / (double) height;
            double r = radius * (1.0 - t) + 0.45;
            double cx = origin.getX() + 0.5 + leanX * Math.max(0, y);
            double cz = origin.getZ() + 0.5 + leanZ * Math.max(0, y);
            int ir = (int) Math.ceil(r);
            for (int dx = -ir; dx <= ir; dx++) {
                for (int dz = -ir; dz <= ir; dz++) {
                    double px = Math.floor(cx) + dx + 0.5 - cx;
                    double pz = Math.floor(cz) + dz + 0.5 - cz;
                    if (px * px + pz * pz > r * r) {
                        continue;
                    }
                    BlockPos pos = BlockPos.containing(cx + dx, origin.getY() + y, cz + dz);
                    BlockState old = level.getBlockState(pos);
                    if (y < 0 ? !old.isSolidRender() : old.canBeReplaced() || old.isAir()) {
                        level.setBlock(pos, (y + band) % 7 < 2 ? deep : stone, 2);
                    }
                }
            }
        }
        BlockPos tip = BlockPos.containing(origin.getX() + 0.5 + leanX * height, origin.getY() + height, origin.getZ() + 0.5 + leanZ * height);
        if (height > 14 && level.isEmptyBlock(tip)) {
            level.setBlock(tip, ShadowBlocks.ASH_SNOW.get().defaultBlockState(), 2);
        }
        return true;
    }
}
