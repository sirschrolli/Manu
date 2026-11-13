package io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature;

import com.mojang.serialization.MapCodec;
import io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * Ein Feld heißer Quellen am Grund der Tiefsee: drei bis sechs Schlote aus dunklem Schlotgestein (zwei bis zehn Blöcke
 * hoch, unten breiter, mit Krusten aus Schwefel), auf jedem die glühende Öffnung, aus der schwarzer Rauch und eine
 * Blasensäule steigen. Der Grund ringsum ist von Gestein und Kruste überzogen, dazwischen wachsen Röhrenwürmer.
 */
public record VentFieldFeature() implements Feature {
    public static final MapCodec<VentFieldFeature> CODEC = MapCodec.unit(VentFieldFeature::new);

    @Override
    public MapCodec<VentFieldFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        int floor0 = level.getHeight(Heightmap.Types.OCEAN_FLOOR, origin.getX(), origin.getZ());
        if (SeaLevel.of(generator) - floor0 < 14) {
            return false;   // nur in der Tiefe
        }
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        // der Grund: Schlotgestein und Kruste in einem unregelmäßigen Fleck
        int radius = 6 + random.nextInt(3);
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz) / radius;
                if (d > 1.0 || random.nextFloat() < d * 0.8) {
                    continue;
                }
                int x = origin.getX() + dx;
                int z = origin.getZ() + dz;
                int floor = level.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z);
                pos.set(x, floor - 1, z);
                if (Kit.ground(level.getBlockState(pos))) {
                    level.setBlock(pos, random.nextFloat() < 0.8f ? SeaBlocks.VENT_ROCK.get().defaultBlockState()
                            : SeaBlocks.SULFUR_CRUST.get().defaultBlockState(), 2);
                    // im heißen Wasser wächst kein Tang und kein Seegras
                    clearPlants(level, pos.above());
                }
            }
        }
        int chimneys = 3 + random.nextInt(4);
        int placed = 0;
        for (int i = 0; i < chimneys; i++) {
            int x = origin.getX() + (i == 0 ? 0 : random.nextInt(11) - 5);
            int z = origin.getZ() + (i == 0 ? 0 : random.nextInt(11) - 5);
            int floor = level.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z);
            int height = (i == 0 ? 6 : 2) + random.nextInt(i == 0 ? 5 : 6);
            if (this.chimney(level, random, new BlockPos(x, floor, z), height)) {
                placed++;
            }
        }
        // Röhrenwürmer in Büscheln zwischen den Schloten
        for (int i = 0; i < 22; i++) {
            int x = origin.getX() + random.nextInt(13) - 6;
            int z = origin.getZ() + random.nextInt(13) - 6;
            int floor = level.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z);
            pos.set(x, floor, z);
            BlockPos below = pos.below();
            if (level.getBlockState(pos).is(Blocks.WATER) && level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)) {
                level.setBlock(pos, SeaBlocks.TUBE_WORMS.get().defaultBlockState(), 2);
            }
        }
        return placed > 0;
    }

    /** Entfernt Tang, Riesentang und Seegras über dieser Stelle (sie werden zu Wasser). */
    static void clearPlants(WorldGenLevel level, BlockPos from) {
        BlockPos.MutableBlockPos p = from.mutable();
        for (int i = 0; i < 48; i++) {
            BlockState state = level.getBlockState(p);
            if (state.is(Blocks.KELP) || state.is(Blocks.KELP_PLANT) || state.is(Blocks.SEAGRASS) || state.is(Blocks.TALL_SEAGRASS)
                    || state.is(SeaBlocks.GIANT_KELP.get()) || state.is(SeaBlocks.GIANT_KELP_PLANT.get())) {
                level.setBlock(p, Blocks.WATER.defaultBlockState(), 2);
                p.move(Direction.UP);
            } else {
                break;
            }
        }
    }

    /** Ein Schlot: unten zwei mal zwei Blöcke, dann einer, oben die glühende Öffnung; hier und da Kruste. */
    private boolean chimney(WorldGenLevel level, RandomSource random, BlockPos base, int height) {
        int water = 0;
        while (water < height + 3 && level.getBlockState(base.above(water)).is(Blocks.WATER)) {
            water++;
        }
        if (water < height + 2) {
            return false;
        }
        BlockState rock = SeaBlocks.VENT_ROCK.get().defaultBlockState();
        BlockState crust = SeaBlocks.SULFUR_CRUST.get().defaultBlockState();
        int foot = Math.max(1, height / 3);
        int ox = random.nextBoolean() ? 1 : -1;
        int oz = random.nextBoolean() ? 1 : -1;
        for (int y = 0; y < height; y++) {
            BlockPos p = base.above(y);
            level.setBlock(p, random.nextFloat() < 0.18f ? crust : rock, 2);
            if (y < foot) {
                for (BlockPos side : new BlockPos[] {p.offset(ox, 0, 0), p.offset(0, 0, oz), p.offset(ox, 0, oz)}) {
                    if (level.getBlockState(side).is(Blocks.WATER) && random.nextFloat() < 0.85f) {
                        level.setBlock(side, random.nextFloat() < 0.25f ? crust : rock, 2);
                    }
                }
            } else if (random.nextFloat() < 0.2f) {
                // eine kleine Krustennase an der Seite
                Direction d = Direction.Plane.HORIZONTAL.getRandomDirection(random);
                if (level.getBlockState(p.relative(d)).is(Blocks.WATER)) {
                    level.setBlock(p.relative(d), crust, 2);
                }
            }
        }
        level.setBlock(base.above(height), SeaBlocks.HYDROTHERMAL_VENT.get().defaultBlockState(), 2);
        return true;
    }
}
