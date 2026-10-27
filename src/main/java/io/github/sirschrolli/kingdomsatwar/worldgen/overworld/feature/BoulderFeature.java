package io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * Ein Findling: zwei, drei ineinander gewachsene, unregelmäßige Felsbuckel, zu einem Drittel im Boden versunken. Der Fels
 * ist gesprenkelt ({@code secondary}), hier und da sitzt etwas Besonderes darin ({@code accent} – Glutrisse, Frostkristall,
 * Bernstein), und obenauf kann Moos oder Schnee liegen ({@code top}, Luft für nichts). Auch in flachem Wasser
 * ({@code inWater}).
 */
public record BoulderFeature(BlockState stone, BlockState secondary, float secondaryChance, BlockState accent, float accentChance, BlockState top,
        float minRadius, float maxRadius, boolean inWater) implements Feature {
    public static final MapCodec<BoulderFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            BlockState.CODEC.fieldOf("stone").forGetter(BoulderFeature::stone),
            BlockState.CODEC.fieldOf("secondary").forGetter(BoulderFeature::secondary),
            Codec.floatRange(0.0f, 1.0f).fieldOf("secondary_chance").forGetter(BoulderFeature::secondaryChance),
            BlockState.CODEC.fieldOf("accent").forGetter(BoulderFeature::accent),
            Codec.floatRange(0.0f, 1.0f).fieldOf("accent_chance").forGetter(BoulderFeature::accentChance),
            BlockState.CODEC.fieldOf("top").forGetter(BoulderFeature::top),
            Codec.floatRange(0.5f, 8.0f).fieldOf("min_radius").forGetter(BoulderFeature::minRadius),
            Codec.floatRange(0.5f, 8.0f).fieldOf("max_radius").forGetter(BoulderFeature::maxRadius),
            Codec.BOOL.fieldOf("in_water").forGetter(BoulderFeature::inWater)
    ).apply(i, BoulderFeature::new));

    @Override
    public MapCodec<BoulderFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        if (!this.inWater && !level.getFluidState(origin).isEmpty() || !Kit.ground(level.getBlockState(origin.below()))) {
            return false;
        }
        double radius = this.minRadius + random.nextDouble() * (this.maxRadius - this.minRadius);
        int lumps = 1 + random.nextInt(3);
        double[][] centers = new double[lumps][4];
        // der Hauptbuckel, dann kleinere daneben
        centers[0] = new double[] {0.0, radius * 0.15, 0.0, radius};
        for (int i = 1; i < lumps; i++) {
            double a = random.nextDouble() * Math.PI * 2.0;
            double r = radius * (0.5 + random.nextDouble() * 0.25);
            centers[i] = new double[] {Math.cos(a) * radius * 0.65, -r * 0.1, Math.sin(a) * radius * 0.65, r};
        }
        double squash = 0.7 + random.nextDouble() * 0.2;
        int reach = (int) Math.ceil(radius * 1.8) + 1;
        int ox = origin.getX();
        int oy = origin.getY() - (int) Math.round(radius * 0.35);
        int oz = origin.getZ();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dy = -reach; dy <= reach; dy++) {
            for (int dx = -reach; dx <= reach; dx++) {
                for (int dz = -reach; dz <= reach; dz++) {
                    if (!this.inside(centers, dx + 0.5, dy + 0.5, dz + 0.5, squash, random)) {
                        continue;
                    }
                    pos.set(ox + dx, oy + dy, oz + dz);
                    BlockState old = level.getBlockState(pos);
                    if (!(this.inWater ? Kit.openOrWater(old) : Kit.open(old)) && !Kit.ground(old)) {
                        continue;
                    }
                    BlockState state = random.nextFloat() < this.accentChance ? this.accent
                            : random.nextFloat() < this.secondaryChance ? this.secondary : this.stone;
                    level.setBlock(pos, state, 2);
                }
            }
        }
        // obenauf Moos oder Schnee
        if (!this.top.isAir()) {
            for (int dx = -reach; dx <= reach; dx++) {
                for (int dz = -reach; dz <= reach; dz++) {
                    for (int dy = reach; dy >= -reach; dy--) {
                        pos.set(ox + dx, oy + dy, oz + dz);
                        BlockState here = level.getBlockState(pos);
                        if (here == this.stone || here == this.secondary) {
                            BlockPos above = pos.above();
                            if (level.getBlockState(above).isAir() && random.nextFloat() < 0.8f) {
                                if (this.top.isSolidRender()) {
                                    level.setBlock(pos, this.top, 2);           // Moosblock, Schneeblock: die Kuppe selbst
                                } else if (this.top.canSurvive(level, above)) {
                                    level.setBlock(above, this.top, 2);         // Teppich, Schicht: obendrauf
                                }
                            }
                            break;
                        }
                        if (!here.isAir()) {
                            break;
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean inside(double[][] centers, double x, double y, double z, double squash, RandomSource random) {
        for (double[] c : centers) {
            double r = c[3];
            double ex = (x - c[0]) / r;
            double ey = (y - c[1]) / (r * squash);
            double ez = (z - c[2]) / (r * 0.9);
            if (ex * ex + ey * ey + ez * ez <= 1.0 - 0.18 * random.nextDouble()) {
                return true;
            }
        }
        return false;
    }
}
