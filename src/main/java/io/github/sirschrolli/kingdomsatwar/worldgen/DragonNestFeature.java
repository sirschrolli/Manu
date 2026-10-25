package io.github.sirschrolli.kingdomsatwar.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.entity.dragon.DragonEntity;
import io.github.sirschrolli.kingdomsatwar.entity.dragon.DragonStage;
import io.github.sirschrolli.kingdomsatwar.loot.ModLootTables;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import java.util.Arrays;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * Drachennest: eine flache Mulde mit einem Wall aus dem Nestmaterial der Art (Basalt und Magma beim Feuerdrachen,
 * Packeis beim Eisdrachen, Calcit und Kupfer mit Blitzableiter beim Blitzdrachen, Moos beim Naturdrachen, Erz und
 * Geröll beim Erddrachen, Prismarin und Sand beim Wasserdrachen). Knochen liegen herum, im Kies verbergen sich
 * Schuppen und Eierschalen (mit dem Pinsel freilegen), in der Mitte liegen oft Dracheneier – und manchmal wacht ein
 * wilder Drache darüber.
 */
public record DragonNestFeature(DragonType type) implements Feature {
    private static final Codec<DragonType> TYPE_CODEC = Codec.STRING.xmap(
            id -> Arrays.stream(DragonType.values()).filter(t -> t.id().equals(id)).findFirst().orElse(DragonType.FIRE), DragonType::id);
    public static final MapCodec<DragonNestFeature> CODEC = RecordCodecBuilder.mapCodec(
            i -> i.group(TYPE_CODEC.fieldOf("element").forGetter(DragonNestFeature::type)).apply(i, DragonNestFeature::new));

    @Override
    public MapCodec<DragonNestFeature> codec() {
        return CODEC;
    }

    /** Baustoffe des Nests: Wall, Boden, Füllung darunter, Kies mit Fundstücken. */
    private record Palette(BlockState[] rim, BlockState[] floor, BlockState fill, Block suspicious) {
        BlockState rim(RandomSource random) {
            return this.rim[random.nextInt(this.rim.length)];
        }

        BlockState floor(RandomSource random) {
            return this.floor[random.nextInt(this.floor.length)];
        }
    }

    private static BlockState[] weighted(Object... blockAndWeight) {
        java.util.List<BlockState> states = new java.util.ArrayList<>();
        for (int i = 0; i < blockAndWeight.length; i += 2) {
            BlockState state = blockAndWeight[i] instanceof Block block ? block.defaultBlockState() : (BlockState) blockAndWeight[i];
            for (int k = 0; k < (int) blockAndWeight[i + 1]; k++) {
                states.add(state);
            }
        }
        return states.toArray(BlockState[]::new);
    }

    private Palette palette() {
        BlockState leaves = Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
        return switch (this.type) {
            case FIRE -> new Palette(weighted(Blocks.BASALT, 4, Blocks.BLACKSTONE, 3, Blocks.MAGMA_BLOCK, 2),
                    weighted(Blocks.BLACKSTONE, 3, Blocks.MAGMA_BLOCK, 1, Blocks.GRAVEL, 2), Blocks.BLACKSTONE.defaultBlockState(),
                    Blocks.SUSPICIOUS_GRAVEL);
            case ICE -> new Palette(weighted(Blocks.PACKED_ICE, 4, Blocks.BLUE_ICE, 1, Blocks.SNOW_BLOCK, 3),
                    weighted(Blocks.SNOW_BLOCK, 3, Blocks.PACKED_ICE, 2, Blocks.GRAVEL, 1), Blocks.PACKED_ICE.defaultBlockState(),
                    Blocks.SUSPICIOUS_GRAVEL);
            case LIGHTNING -> new Palette(weighted(Blocks.CALCITE, 4, Blocks.COPPER_BLOCK.weathering().weathered(), 2,
                    Blocks.COPPER_BLOCK.weathering().oxidized(), 1, Blocks.TUFF, 2),
                    weighted(Blocks.GRAVEL, 2, Blocks.TUFF, 2, Blocks.CALCITE, 1), Blocks.TUFF.defaultBlockState(), Blocks.SUSPICIOUS_GRAVEL);
            case NATURE -> new Palette(weighted(Blocks.MOSS_BLOCK, 4, Blocks.ROOTED_DIRT, 2, leaves, 2),
                    weighted(Blocks.MOSS_BLOCK, 3, Blocks.ROOTED_DIRT, 2, Blocks.COARSE_DIRT, 1), Blocks.ROOTED_DIRT.defaultBlockState(),
                    Blocks.SUSPICIOUS_GRAVEL);
            case EARTH -> new Palette(weighted(Blocks.COBBLESTONE, 3, Blocks.TUFF, 2, Blocks.COARSE_DIRT, 2, Blocks.RAW_IRON_BLOCK, 1),
                    weighted(Blocks.COARSE_DIRT, 3, Blocks.GRAVEL, 2, Blocks.ROOTED_DIRT, 1), Blocks.COARSE_DIRT.defaultBlockState(),
                    Blocks.SUSPICIOUS_GRAVEL);
            case WATER -> new Palette(weighted(Blocks.PRISMARINE, 3, Blocks.PRISMARINE_BRICKS, 1, Blocks.SAND, 3),
                    weighted(Blocks.SAND, 3, Blocks.GRAVEL, 1, Blocks.PRISMARINE, 1), Blocks.SANDSTONE.defaultBlockState(),
                    Blocks.SUSPICIOUS_SAND);
        };
    }

    /** Erster freier Block über dem Nest an dieser Stelle (unabhängig davon, ob die Höhenkarte schon nachgezogen ist). */
    private static BlockPos surface(WorldGenLevel level, BlockPos column) {
        BlockPos.MutableBlockPos pos = column.mutable().move(0, 5, 0);
        while (pos.getY() > column.getY() - 4 && level.getBlockState(pos.below()).isAir()) {
            pos.move(0, -1, 0);
        }
        return pos.immutable();
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        int y0 = origin.getY();
        // nur auf einigermaßen ebenem, trockenem Boden
        int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
        for (int dx = -5; dx <= 5; dx += 5) {
            for (int dz = -5; dz <= 5; dz += 5) {
                int h = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, origin.getX() + dx, origin.getZ() + dz);
                min = Math.min(min, h);
                max = Math.max(max, h);
            }
        }
        if (max - min > 3 || !level.getFluidState(origin.below()).isEmpty() || !level.getFluidState(origin).isEmpty()) {
            return false;
        }
        Palette palette = this.palette();
        double wobble = random.nextDouble() * Math.PI * 2;
        for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -5; dz <= 5; dz++) {
                double angle = Math.atan2(dz, dx);
                double d = Math.sqrt(dx * dx + dz * dz) + 0.35 * Math.sin(angle * 3 + wobble);
                if (d > 5.1) {
                    continue;
                }
                BlockPos column = origin.offset(dx, 0, dz);
                // Untergrund bis zum Nestboden auffüllen (kein schwebendes Nest)
                int ground = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, column.getX(), column.getZ());
                for (int y = Math.max(ground - 1, y0 - 5); y < y0 - 1; y++) {
                    level.setBlock(column.atY(y), palette.fill(), Block.UPDATE_CLIENTS);
                }
                int top;
                if (d <= 3.1) {
                    level.setBlock(column.atY(y0 - 1), palette.floor(random), Block.UPDATE_CLIENTS);
                    top = y0 - 1;
                } else {
                    int height = (d < 4.3 ? 2 : 1) + (random.nextInt(3) == 0 ? 1 : 0);
                    for (int y = y0 - 1; y < y0 - 1 + height; y++) {
                        level.setBlock(column.atY(y), palette.rim(random), Block.UPDATE_CLIENTS);
                    }
                    top = y0 - 2 + height;
                }
                for (int y = top + 1; y <= y0 + 4; y++) {
                    if (!level.getBlockState(column.atY(y)).isAir()) {
                        level.setBlock(column.atY(y), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                    }
                }
            }
        }
        // Knochen am Rand, Fundstücke im Kies des Bodens
        for (int i = 0; i < 2 + random.nextInt(3); i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            int r = 3 + random.nextInt(2);
            BlockPos bone = surface(level, origin.offset((int) Math.round(Math.cos(angle) * r), 0, (int) Math.round(Math.sin(angle) * r)));
            Direction.Axis axis = Direction.Axis.values()[random.nextInt(3)];
            level.setBlock(bone, Blocks.BONE_BLOCK.defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis), Block.UPDATE_CLIENTS);
        }
        for (int i = 0; i < 3; i++) {
            BlockPos spot = origin.offset(random.nextInt(5) - 2, -1, random.nextInt(5) - 2);
            if (spot.getX() == origin.getX() && spot.getZ() == origin.getZ()) {
                continue;
            }
            level.setBlock(spot, palette.suspicious().defaultBlockState(), Block.UPDATE_CLIENTS);
            if (level.getBlockEntity(spot) instanceof BrushableBlockEntity brushable) {
                brushable.setLootTable(ModLootTables.DRAGON_NEST_ARCHAEOLOGY.get(this.type), random.nextLong());
            }
        }
        if (this.type == DragonType.LIGHTNING) {
            // Blitzableiter auf dem Wall
            BlockPos rod = surface(level, origin.offset(4, 0, 0));
            level.setBlock(rod, Blocks.LIGHTNING_ROD.weathering().weathered().defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        // Eier: meist eins, manchmal zwei, gelegentlich schon geschlüpft
        BlockState egg = ModBlocks.DRAGON_EGGS.get(this.type).get().defaultBlockState();
        int eggs = random.nextFloat() < 0.25f ? 0 : random.nextFloat() < 0.2f ? 2 : 1;
        if (eggs >= 1) {
            level.setBlock(origin, egg, Block.UPDATE_CLIENTS);
        }
        if (eggs == 2) {
            level.setBlock(origin.offset(1, 0, random.nextBoolean() ? 1 : -1), egg, Block.UPDATE_CLIENTS);
        }
        // Wächter: ein wilder Drache (ausgewachsen = angriffslustig, halbwüchsig = verteidigt sich nur)
        float roll = random.nextFloat();
        if (roll < 0.6f) {
            DragonEntity guard = ModEntities.DRAGONS.get(this.type).get().create(level.getLevel(), EntitySpawnReason.STRUCTURE);
            if (guard != null) {
                guard.applyStage(roll < 0.3f ? DragonStage.ADULT : DragonStage.ADOLESCENT, true);
                guard.snapTo(origin.getX() + 0.5 + 2.5, y0, origin.getZ() + 0.5, random.nextFloat() * 360.0f, 0.0f);
                guard.setPersistenceRequired();
                level.addFreshEntityWithPassengers(guard);
            }
        }
        return true;
    }
}
