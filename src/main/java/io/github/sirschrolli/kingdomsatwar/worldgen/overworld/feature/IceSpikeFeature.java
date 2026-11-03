package io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * Eisnadeln der Frostgipfel: zwei bis vier spitze Kegel aus Packeis, die aus dem Schnee brechen und sich leicht
 * auseinanderneigen – die mittlere am höchsten. Durch das Eis ziehen sich Adern aus Blaueis, und auf den Spitzen
 * glimmt ein Frostkristall. Am Fuß liegt Eis, das ein wenig über den Schnee reicht.
 */
public record IceSpikeFeature(int minHeight, int maxHeight) implements Feature {
    public static final MapCodec<IceSpikeFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.intRange(3, 40).fieldOf("min_height").forGetter(IceSpikeFeature::minHeight),
            Codec.intRange(3, 40).fieldOf("max_height").forGetter(IceSpikeFeature::maxHeight)
    ).apply(i, IceSpikeFeature::new));

    @Override
    public MapCodec<IceSpikeFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        if (!Kit.dryGround(level, origin)) {
            return false;
        }
        BlockState ice = Blocks.PACKED_ICE.defaultBlockState();
        BlockState vein = Blocks.BLUE_ICE.defaultBlockState();
        BlockState crystal = ModBlocks.FROST_CRYSTAL.get().defaultBlockState();
        int spikes = 2 + random.nextInt(3);
        double turn = random.nextDouble() * Math.PI * 2.0;
        for (int s = 0; s < spikes; s++) {
            boolean main = s == 0;
            double angle = turn + s * Math.PI * 2.0 / Math.max(1, spikes - 1) + (random.nextDouble() - 0.5) * 0.8;
            double offset = main ? 0.0 : 1.4 + random.nextDouble() * 1.6;
            double x = origin.getX() + 0.5 + Math.cos(angle) * offset;
            double z = origin.getZ() + 0.5 + Math.sin(angle) * offset;
            int span = Math.max(1, this.maxHeight - this.minHeight + 1);
            int height = main ? this.maxHeight - random.nextInt(Math.max(1, span / 3)) : this.minHeight + random.nextInt(Math.max(1, span * 2 / 3));
            double radius = main ? 1.3 + random.nextDouble() * 0.4 : 0.8 + random.nextDouble() * 0.4;
            double lean = main ? 0.04 : 0.08 + random.nextDouble() * 0.08;
            int ground = Kit.surface(level, (int) Math.floor(x), (int) Math.floor(z));
            int veinPhase = random.nextInt(5);
            BlockPos tip = null;
            for (int k = -2; k < height; k++) {
                double t = Math.max(0, k) / (double) height;
                double r = radius * Math.pow(1.0 - t, 1.35) + 0.3;
                double px = x + Math.cos(angle) * lean * Math.max(0, k);
                double pz = z + Math.sin(angle) * lean * Math.max(0, k);
                int ir = (int) Math.ceil(r);
                for (int dx = -ir; dx <= ir; dx++) {
                    for (int dz = -ir; dz <= ir; dz++) {
                        double cx = Math.floor(px) + dx + 0.5 - px;
                        double cz = Math.floor(pz) + dz + 0.5 - pz;
                        if (cx * cx + cz * cz > r * r) {
                            continue;
                        }
                        BlockPos pos = new BlockPos((int) Math.floor(px) + dx, ground + k, (int) Math.floor(pz) + dz);
                        BlockState old = level.getBlockState(pos);
                        if (k < 0 ? !Kit.ground(old) : !Kit.open(old)) {
                            continue;
                        }
                        boolean veined = (k + veinPhase) % 5 == 0 && (dx + dz) % 2 == 0 || dx == 0 && dz == 0 && k % 3 == 1;
                        level.setBlock(pos, veined ? vein : ice, 2);
                        if (dx == 0 && dz == 0) {
                            tip = pos;
                        }
                    }
                }
            }
            if (tip != null && Kit.open(level.getBlockState(tip.above()))) {
                level.setBlock(tip.above(), crystal, 2);
            }
        }
        // Eis am Fuß, das ein Stück über den Schnee reicht
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                if (dx * dx + dz * dz > 10 || random.nextFloat() < 0.45f) {
                    continue;
                }
                int x = origin.getX() + dx;
                int z = origin.getZ() + dz;
                BlockPos top = new BlockPos(x, Kit.surface(level, x, z) - 1, z);
                if (Kit.ground(level.getBlockState(top)) && !level.getBlockState(top).is(Blocks.PACKED_ICE)) {
                    level.setBlock(top, random.nextFloat() < 0.75f ? ice : ModBlocks.GLACIER_ICE.get().defaultBlockState(), 2);
                }
            }
        }
        return true;
    }
}
