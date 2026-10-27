package io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature;

import com.mojang.serialization.MapCodec;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * Ein Glutriss der Aschefelder: ein schmaler, gezackter Spalt, der sich ein Dutzend Blöcke durch den Boden zieht – in
 * der Mitte drei, vier Blöcke tief, zu den Enden hin flach. Unten glüht das Gestein, die Wände sind aus Vulkangestein,
 * am Rand liegt Aschenerde; hier und da raucht eine Glutspalte. Nachts sieht man die Risse von weitem leuchten.
 */
public record FissureFeature() implements Feature {
    public static final MapCodec<FissureFeature> CODEC = MapCodec.unit(FissureFeature::new);

    @Override
    public MapCodec<FissureFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        if (!Kit.dryGround(level, origin)) {
            return false;
        }
        BlockState rock = ModBlocks.VOLCANIC_ROCK.base().get().defaultBlockState();
        BlockState glow = ModBlocks.GLOWING_VOLCANIC_ROCK.get().defaultBlockState();
        BlockState vent = ModBlocks.EMBER_VENT.get().defaultBlockState();
        BlockState cinder = ModBlocks.CINDER_SOIL.get().defaultBlockState();
        int length = 9 + random.nextInt(9);
        double angle = random.nextDouble() * Math.PI * 2.0;
        // vom Ursprung aus in beide Richtungen, damit der Riss um ihn herum liegt
        double x = origin.getX() + 0.5 - Math.cos(angle) * length / 2.0;
        double z = origin.getZ() + 0.5 - Math.sin(angle) * length / 2.0;
        boolean placed = false;
        BlockPos last = null;
        for (int i = 0; i < length; i++) {
            angle += (random.nextDouble() - 0.5) * 0.7;
            x += Math.cos(angle);
            z += Math.sin(angle);
            int bx = (int) Math.floor(x);
            int bz = (int) Math.floor(z);
            BlockPos cell = new BlockPos(bx, 0, bz);
            if (cell.equals(last)) {
                continue;
            }
            last = cell;
            int top = Kit.surface(level, bx, bz) - 1;
            // nur dort, wo trockener Boden ist und das Gelände nicht springt
            if (Math.abs(top - (origin.getY() - 1)) > 3 || !Kit.ground(level.getBlockState(new BlockPos(bx, top, bz)))) {
                continue;
            }
            double t = i / (double) (length - 1);
            int depth = 1 + (int) Math.round(3.0 * Math.sin(Math.PI * t)) + (random.nextFloat() < 0.3f ? 1 : 0);
            if (!this.cut(level, bx, top, bz, depth, rock, glow, vent, cinder, random, i % 6 == 3)) {
                continue;
            }
            placed = true;
            // ab und zu wird der Riss zwei Blöcke breit
            if (depth >= 3 && random.nextFloat() < 0.35f) {
                Direction side = random.nextBoolean() ? Direction.from2DDataValue((int) Math.floor(angle / (Math.PI / 2.0)) + 1)
                        : Direction.from2DDataValue((int) Math.floor(angle / (Math.PI / 2.0)) + 3);
                int sx = bx + side.getStepX();
                int sz = bz + side.getStepZ();
                int sideTop = Kit.surface(level, sx, sz) - 1;
                if (Math.abs(sideTop - top) <= 1) {
                    this.cut(level, sx, sideTop, sz, depth - 1, rock, glow, vent, cinder, random, false);
                }
            }
        }
        return placed;
    }

    /** Schneidet eine Spalte des Risses: Luft, darunter Glut, die Wände aus Vulkangestein, am Rand Aschenerde. */
    private boolean cut(WorldGenLevel level, int x, int top, int z, int depth, BlockState rock, BlockState glow, BlockState vent,
            BlockState cinder, RandomSource random, boolean ventHere) {
        BlockPos floor = new BlockPos(x, top - depth, z);
        // der Boden des Risses muss fest sein und darf nicht in eine Höhle durchbrechen
        if (!Kit.ground(level.getBlockState(floor)) || !Kit.ground(level.getBlockState(floor.below()))) {
            return false;
        }
        for (int k = 0; k < depth; k++) {
            BlockPos p = new BlockPos(x, top - k, z);
            if (!level.getFluidState(p).isEmpty()) {
                return false;
            }
        }
        for (int k = 0; k < depth; k++) {
            level.setBlock(new BlockPos(x, top - k, z), Blocks.AIR.defaultBlockState(), 2);
        }
        // was oben auf dem Riss stand (Gras, Asche), fällt mit hinein
        BlockPos above = new BlockPos(x, top + 1, z);
        if (Kit.open(level.getBlockState(above)) && !level.getBlockState(above).isAir()) {
            level.setBlock(above, Blocks.AIR.defaultBlockState(), 2);
        }
        level.setBlock(floor, ventHere ? vent : glow, 2);
        level.setBlock(floor.below(), glow, 2);
        // Wände
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            for (int k = 0; k <= depth; k++) {
                BlockPos wall = new BlockPos(x + dir.getStepX(), top - k, z + dir.getStepZ());
                BlockState old = level.getBlockState(wall);
                if (Kit.ground(old) && !old.is(glow.getBlock()) && !old.is(vent.getBlock())) {
                    level.setBlock(wall, k == depth && random.nextFloat() < 0.5f ? glow : rock, 2);
                }
            }
            // Aschenerde und Gestein am Rand
            BlockPos rim = new BlockPos(x + dir.getStepX() * 2, top, z + dir.getStepZ() * 2);
            BlockState old = level.getBlockState(rim);
            if (Kit.ground(old) && level.getBlockState(rim.above()).isAir() && random.nextFloat() < 0.45f) {
                level.setBlock(rim, random.nextFloat() < 0.6f ? cinder : rock, 2);
            }
        }
        return true;
    }
}
