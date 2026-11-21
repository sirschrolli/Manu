package io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature;

import com.mojang.serialization.MapCodec;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * Eine Glutspalte der Aschefelder: der Schlot sitzt eben im Boden, ringsum ein Kranz aus Vulkangestein mit glühenden
 * Stellen, außen Aschenerde und ausgeworfene Asche. Aus dem Schlot steigt Rauch, ab und zu spritzt Glut.
 */
public record VentFeature() implements Feature {
    public static final MapCodec<VentFeature> CODEC = MapCodec.unit(VentFeature::new);

    @Override
    public MapCodec<VentFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        BlockPos floor = origin.below();
        if (!Kit.dryGround(level, origin) || !level.getBlockState(origin.above()).isAir()) {
            return false;
        }
        BlockState rock = ModBlocks.VOLCANIC_ROCK.base().get().defaultBlockState();
        BlockState glow = ModBlocks.GLOWING_VOLCANIC_ROCK.get().defaultBlockState();
        BlockState ash = ModBlocks.ASH_LAYER.get().defaultBlockState();
        // der Kranz liegt eben im Boden: innen Vulkangestein mit Glutstellen, außen Aschenerde und Gestein
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                int d2 = dx * dx + dz * dz;
                if (d2 == 0 || d2 > 10) {
                    continue;
                }
                int x = origin.getX() + dx;
                int z = origin.getZ() + dz;
                BlockPos top = new BlockPos(x, Kit.surface(level, x, z) - 1, z);
                if (!Kit.ground(level.getBlockState(top)) || Math.abs(top.getY() - floor.getY()) > 1) {
                    continue;
                }
                if (d2 <= 2) {
                    level.setBlock(top, random.nextFloat() < 0.35f ? glow : rock, 2);
                } else if (d2 <= 5 ? random.nextFloat() < 0.75f : random.nextFloat() < 0.35f) {
                    level.setBlock(top, random.nextFloat() < 0.6f ? rock : ModBlocks.CINDER_SOIL.get().defaultBlockState(), 2);
                }
                // feine Asche, die der Schlot ausgeworfen hat
                BlockPos above = top.above();
                if (d2 > 2 && random.nextFloat() < 0.3f && level.getBlockState(above).isAir()) {
                    level.setBlock(above, ash.setValue(SnowLayerBlock.LAYERS, 1 + random.nextInt(2)), 2);
                }
            }
        }
        level.setBlock(floor, ModBlocks.EMBER_VENT.get().defaultBlockState(), 2);
        level.setBlock(floor.below(), glow, 2);
        if (Kit.open(level.getBlockState(origin))) {
            level.setBlock(origin, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 2);
        }
        return true;
    }
}
