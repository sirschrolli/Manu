package io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * Eine Verwehung aus Schichten ({@code layer}, z. B. Asche oder Schnee): ein länglicher, weicher Hügel, in der Mitte bis
 * zu {@code maxLayers} Schichten hoch, nach außen flacher – wie vom Wind zusammengetragen.
 */
public record DuneFeature(BlockState layer, float radius, int maxLayers) implements Feature {
    public static final MapCodec<DuneFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            BlockState.CODEC.fieldOf("layer").forGetter(DuneFeature::layer),
            Codec.floatRange(1.0f, 12.0f).fieldOf("radius").forGetter(DuneFeature::radius),
            Codec.intRange(1, 8).fieldOf("max_layers").forGetter(DuneFeature::maxLayers)
    ).apply(i, DuneFeature::new));

    @Override
    public MapCodec<DuneFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        if (!this.layer.hasProperty(SnowLayerBlock.LAYERS)) {
            return false;
        }
        double angle = random.nextDouble() * Math.PI;
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double long_ = this.radius * (1.0 + random.nextDouble() * 0.6);
        double short_ = this.radius * (0.5 + random.nextDouble() * 0.3);
        int reach = (int) Math.ceil(long_) + 1;
        boolean placed = false;
        for (int dx = -reach; dx <= reach; dx++) {
            for (int dz = -reach; dz <= reach; dz++) {
                double u = (dx * cos + dz * sin) / long_;
                double v = (-dx * sin + dz * cos) / short_;
                double d = Math.sqrt(u * u + v * v);
                if (d >= 1.0) {
                    continue;
                }
                int layers = (int) Math.round(this.maxLayers * Math.pow(1.0 - d, 1.2) + (random.nextDouble() - 0.5));
                if (layers <= 0) {
                    continue;
                }
                int x = origin.getX() + dx;
                int z = origin.getZ() + dz;
                BlockPos pos = new BlockPos(x, Kit.surface(level, x, z), z);
                BlockState here = level.getBlockState(pos);
                if (!here.isAir() && !(here.canBeReplaced() && here.getFluidState().isEmpty())) {
                    continue;
                }
                BlockState below = level.getBlockState(pos.below());
                if (!below.isFaceSturdy(level, pos.below(), net.minecraft.core.Direction.UP) || !level.getFluidState(pos).isEmpty()) {
                    continue;
                }
                level.setBlock(pos, this.layer.setValue(SnowLayerBlock.LAYERS, Math.min(8, layers)), 2);
                placed = true;
            }
        }
        return placed;
    }
}
