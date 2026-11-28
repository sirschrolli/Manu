package io.github.sirschrolli.kingdomsatwar.worldgen;

import com.mojang.serialization.MapCodec;
import io.github.sirschrolli.kingdomsatwar.block.BattlementBlock;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.BanditEntity;
import io.github.sirschrolli.kingdomsatwar.loot.ModLootTables;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * Burgruine: die verfallenen Mauern eines kleinen Bergfrieds (9×9) mit Ecktürmen, eingestürzten Abschnitten,
 * Fensterlöchern, einem Tor und Zinnen auf den noch hohen Mauerstücken. Ringsum liegt Geröll, Ranken wachsen an den
 * Steinen. Drinnen steht eine Truhe mit Beute – und manchmal haben sich Banditen eingenistet.
 */
public record CastleRuinFeature() implements Feature {
    public static final MapCodec<CastleRuinFeature> CODEC = MapCodec.unit(CastleRuinFeature::new);
    private static final int HALF = 4;

    @Override
    public MapCodec<CastleRuinFeature> codec() {
        return CODEC;
    }

    private static BlockState wallBlock(RandomSource random) {
        float r = random.nextFloat();
        if (r < 0.4f) {
            return ModBlocks.MEDIEVAL_STONE.base().get().defaultBlockState();
        } else if (r < 0.65f) {
            return ModBlocks.MOSSY_WALL_STONES.base().get().defaultBlockState();
        } else if (r < 0.85f) {
            return ModBlocks.CRACKED_STONE_BLOCKS.base().get().defaultBlockState();
        }
        return ModBlocks.WEATHERED_STONE_BRICKS.base().get().defaultBlockState();
    }

    private static BlockState rubble(RandomSource random) {
        float r = random.nextFloat();
        if (r < 0.35f) {
            return Blocks.COBBLESTONE.defaultBlockState();
        } else if (r < 0.6f) {
            return Blocks.MOSSY_COBBLESTONE.defaultBlockState();
        } else if (r < 0.8f) {
            return ModBlocks.CRACKED_STONE_BLOCKS.slab().get().defaultBlockState();
        }
        return Blocks.GRAVEL.defaultBlockState();
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        int y0 = origin.getY();
        int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
        for (int dx = -HALF; dx <= HALF; dx += HALF) {
            for (int dz = -HALF; dz <= HALF; dz += HALF) {
                int h = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, origin.getX() + dx, origin.getZ() + dz);
                min = Math.min(min, h);
                max = Math.max(max, h);
            }
        }
        if (max - min > 2 || !level.getFluidState(origin.below()).isEmpty() || !level.getFluidState(origin).isEmpty()) {
            return false;
        }
        Direction gate = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        // Fundament und Boden
        for (int dx = -HALF; dx <= HALF; dx++) {
            for (int dz = -HALF; dz <= HALF; dz++) {
                BlockPos column = origin.offset(dx, 0, dz);
                int ground = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, column.getX(), column.getZ());
                for (int y = Math.max(ground - 1, y0 - 4); y < y0 - 1; y++) {
                    level.setBlock(column.atY(y), ModBlocks.ROUGH_FIELDSTONE.base().get().defaultBlockState(), Block.UPDATE_CLIENTS);
                }
                boolean edge = Math.abs(dx) == HALF || Math.abs(dz) == HALF;
                BlockState floor = edge ? wallBlock(random) : random.nextFloat() < 0.15f ? Blocks.GRASS_BLOCK.defaultBlockState()
                        : random.nextBoolean() ? ModBlocks.CRACKED_STONE_BLOCKS.base().get().defaultBlockState()
                        : ModBlocks.ROUGH_FIELDSTONE.base().get().defaultBlockState();
                level.setBlock(column.atY(y0 - 1), floor, Block.UPDATE_CLIENTS);
                for (int y = y0; y <= y0 + 7; y++) {
                    if (!level.getBlockState(column.atY(y)).isAir()) {
                        level.setBlock(column.atY(y), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                    }
                }
            }
        }
        // Mauern: Höhe je Abschnitt verschieden, manche eingestürzt; Tor auf einer Seite
        double phase = random.nextDouble() * 10;
        for (int dx = -HALF; dx <= HALF; dx++) {
            for (int dz = -HALF; dz <= HALF; dz++) {
                boolean corner = Math.abs(dx) == HALF && Math.abs(dz) == HALF;
                boolean edge = Math.abs(dx) == HALF || Math.abs(dz) == HALF;
                if (!edge) {
                    continue;
                }
                BlockPos column = origin.offset(dx, 0, dz);
                int along = (Math.abs(dx) == HALF ? dz : dx) + HALF;
                int height;
                if (corner) {
                    height = 4 + random.nextInt(3);
                } else {
                    double wave = Math.sin(phase + along * 0.9) + Math.sin(phase * 1.7 + along * 0.43);
                    height = Math.max(0, (int) Math.round(2.2 + wave * 1.6 + random.nextFloat() - 0.5));
                }
                boolean isGateSide = (gate == Direction.NORTH && dz == -HALF) || (gate == Direction.SOUTH && dz == HALF)
                        || (gate == Direction.WEST && dx == -HALF) || (gate == Direction.EAST && dx == HALF);
                boolean inGate = isGateSide && !corner && Math.abs(along - HALF) <= 1;
                for (int y = 0; y < height; y++) {
                    if (inGate && y < 3) {
                        continue;
                    }
                    BlockPos pos = column.above(y);
                    BlockState state = corner ? ModBlocks.STONE_PILLAR.get().defaultBlockState()
                            .setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y) : wallBlock(random);
                    boolean window = !corner && y == 2 && along % 3 == 1 && random.nextBoolean();
                    if (!window) {
                        level.setBlock(pos, state, Block.UPDATE_CLIENTS);
                    }
                }
                // Zinnen auf den hohen, noch geschlossenen Mauerstücken
                if (!corner && !inGate && height >= 4 && random.nextFloat() < 0.7f) {
                    Direction outward = Math.abs(dx) == HALF ? (dx > 0 ? Direction.EAST : Direction.WEST)
                            : (dz > 0 ? Direction.SOUTH : Direction.NORTH);
                    level.setBlock(column.above(height), ModBlocks.MEDIEVAL_STONE_BATTLEMENT.get().defaultBlockState()
                            .setValue(BattlementBlock.FACING, outward.getOpposite()), Block.UPDATE_CLIENTS);
                }
                // Ranken an der Außenseite
                if (height >= 2 && random.nextFloat() < 0.25f) {
                    Direction outward = Math.abs(dx) == HALF ? (dx > 0 ? Direction.EAST : Direction.WEST)
                            : (dz > 0 ? Direction.SOUTH : Direction.NORTH);
                    BlockPos vinePos = column.relative(outward).above(height - 1);
                    for (int k = 0; k < height - 1 && level.getBlockState(vinePos).isAir(); k++) {
                        level.setBlock(vinePos, Blocks.VINE.defaultBlockState()
                                .setValue(VineBlock.getPropertyForFace(outward.getOpposite()), true), Block.UPDATE_CLIENTS);
                        vinePos = vinePos.below();
                    }
                }
            }
        }
        // Geröll ringsum und im Innenhof
        for (int i = 0; i < 18; i++) {
            int dx = random.nextInt(13) - 6;
            int dz = random.nextInt(13) - 6;
            if (Math.abs(dx) == HALF || Math.abs(dz) == HALF) {
                continue;
            }
            BlockPos.MutableBlockPos scan = origin.offset(dx, 3, dz).mutable();
            while (scan.getY() > origin.getY() - 4 && level.getBlockState(scan.below()).isAir()) {
                scan.move(0, -1, 0);
            }
            BlockPos pos = scan.immutable();
            if (level.getBlockState(pos).isAir() && !level.getBlockState(pos.below()).isAir()
                    && level.getFluidState(pos.below()).isEmpty()) {
                BlockState block = rubble(random);
                if (block.getBlock() instanceof SlabBlock) {
                    block = block.setValue(SlabBlock.WATERLOGGED, false);
                }
                level.setBlock(pos, block, Block.UPDATE_CLIENTS);
            }
        }
        // Truhe in einer Ecke des Innenhofs
        int cx = random.nextBoolean() ? HALF - 1 : -(HALF - 1);
        int cz = random.nextBoolean() ? HALF - 1 : -(HALF - 1);
        BlockPos chest = origin.offset(cx, 0, cz);
        Direction facing = Math.abs(cx) > 0 ? (cx > 0 ? Direction.WEST : Direction.EAST) : Direction.NORTH;
        level.setBlock(chest, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing), Block.UPDATE_CLIENTS);
        RandomizableContainer.setBlockEntityLootTable(level, random, chest, ModLootTables.CASTLE_RUIN_CHEST);
        level.setBlock(origin.offset(-cx, 0, -cz), Blocks.COBWEB.defaultBlockState(), Block.UPDATE_CLIENTS);
        // Banditen
        if (random.nextFloat() < 0.45f) {
            for (int i = 0; i < 1 + random.nextInt(2); i++) {
                BanditEntity bandit = (i == 0 ? ModEntities.BANDIT : ModEntities.BANDIT_ARCHER).get()
                        .create(level.getLevel(), EntitySpawnReason.STRUCTURE);
                if (bandit != null) {
                    bandit.snapTo(origin.getX() + 0.5 + random.nextInt(3) - 1, y0, origin.getZ() + 0.5 + random.nextInt(3) - 1,
                            random.nextFloat() * 360.0f, 0.0f);
                    bandit.finalizeSpawn(level, level.getCurrentDifficultyAt(origin), EntitySpawnReason.STRUCTURE, null);
                    bandit.setPersistenceRequired();
                    level.addFreshEntityWithPassengers(bandit);
                }
            }
        }
        return true;
    }
}
