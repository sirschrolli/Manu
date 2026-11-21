package io.github.sirschrolli.kingdomsatwar.voidrealm.feature;

import com.mojang.serialization.MapCodec;
import io.github.sirschrolli.kingdomsatwar.loot.ModLootTables;
import io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * Die schwerelosen Ruinen der Alten: Trümmer aus hellem Sternenmarmor auf den Bruchstücken der Leere – Säulenhallen mit
 * gebrochenen Säulen, große Torbögen, kleine Schreine mit Altar und Truhe, Wendeltreppen, die ins Nichts führen, und
 * zerfallene Türme. Über allen schweben Mauerstücke, Säulentrommeln und Treppenstufen frei in der Luft, als hätte die Leere
 * die Schwere aus ihnen gezogen.
 */
public record VoidRuinFeature() implements Feature {
    public static final MapCodec<VoidRuinFeature> CODEC = MapCodec.unit(VoidRuinFeature::new);

    @Override
    public MapCodec<VoidRuinFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        if (!level.getBlockState(origin.below()).isSolidRender()) {
            return false;
        }
        // nur auf einigermaßen ebenem Grund: die Ecken um höchstens zwei Blöcke versetzt
        for (int[] o : new int[][] {{-5, -5}, {5, -5}, {-5, 5}, {5, 5}}) {
            BlockPos corner = origin.offset(o[0], 0, o[1]);
            if (!VoidBuild.inReach(origin, corner)) {
                return false;
            }
            boolean solidBelow = level.getBlockState(corner.below()).isSolidRender() || level.getBlockState(corner.below(2)).isSolidRender()
                    || level.getBlockState(corner).isSolidRender();
            if (!solidBelow) {
                return false;
            }
        }
        Materials m = new Materials();
        int kind = random.nextInt(5);
        switch (kind) {
            case 0 -> colonnade(level, origin, random, m);
            case 1 -> arch(level, origin, random, m);
            case 2 -> shrine(level, origin, random, m);
            case 3 -> stair(level, origin, random, m);
            default -> tower(level, origin, random, m);
        }
        debris(level, origin, random, m, 3 + random.nextInt(4));
        // ein ruhender Splitterwächter: meist vor dem Schrein (er bewacht die Truhe), manchmal in der Säulenhalle
        int chance = switch (kind) {
            case 2 -> 2;
            case 0 -> 3;
            case 4 -> 5;
            default -> 0;
        };
        if (chance > 0 && random.nextInt(chance) == 0) {
            guardian(level, origin, random);
        }
        return true;
    }

    /** Stellt einen ruhenden Splitterwächter auf einen freien Platz nahe der Ruine (er bleibt, wo er steht). */
    private static void guardian(WorldGenLevel level, BlockPos origin, RandomSource random) {
        for (int attempt = 0; attempt < 12; attempt++) {
            BlockPos base = origin.offset(random.nextInt(9) - 4, 0, random.nextInt(9) - 4);
            for (int dy = 3; dy >= -2; dy--) {
                BlockPos feet = base.above(dy);
                if (!VoidBuild.inReach(origin, feet) || !level.getBlockState(feet.below()).isSolidRender()) {
                    continue;
                }
                if (!level.getBlockState(feet).isAir() || !level.getBlockState(feet.above()).isAir()
                        || !level.getBlockState(feet.above(2)).isAir()) {
                    continue;
                }
                var warden = io.github.sirschrolli.kingdomsatwar.registry.ModEntities.SHARD_WARDEN.get().create(level.getLevel(),
                        net.minecraft.world.entity.EntitySpawnReason.STRUCTURE);
                if (warden == null) {
                    return;
                }
                float yaw = random.nextFloat() * 360.0f;
                warden.snapTo(feet.getX() + 0.5, feet.getY(), feet.getZ() + 0.5, yaw, 0.0f);
                warden.yBodyRot = yaw;
                warden.setYHeadRot(yaw);
                warden.finalizeSpawn(level, level.getCurrentDifficultyAt(feet), net.minecraft.world.entity.EntitySpawnReason.STRUCTURE, null);
                warden.setPersistenceRequired();
                level.addFreshEntityWithPassengers(warden);
                return;
            }
        }
    }

    /** Die Baustoffe der Alten. */
    private static final class Materials {
        final BlockState marble = VoidBlocks.ASTRAL_MARBLE.base().get().defaultBlockState();
        final BlockState bricks = VoidBlocks.ASTRAL_MARBLE_BRICKS.base().get().defaultBlockState();
        final BlockState brickSlab = VoidBlocks.ASTRAL_MARBLE_BRICKS.slab().get().defaultBlockState();
        final BlockState brickStairs = VoidBlocks.ASTRAL_MARBLE_BRICKS.stairs().get().defaultBlockState();
        final BlockState chiseled = VoidBlocks.CHISELED_ASTRAL_MARBLE.get().defaultBlockState();
        final BlockState pillar = VoidBlocks.ASTRAL_PILLAR.get().defaultBlockState();
        final BlockState crystal = VoidBlocks.VOID_CRYSTAL.get().defaultBlockState();
        final BlockState cluster = VoidBlocks.VOID_CRYSTAL_CLUSTER.get().defaultBlockState();
        final BlockState voidBricks = VoidBlocks.VOID_STONE_BRICKS.base().get().defaultBlockState();
        final BlockState cracked = VoidBlocks.CRACKED_VOID_STONE_BRICKS.get().defaultBlockState();

        BlockState pillar(Direction.Axis axis) {
            return this.pillar.setValue(RotatedPillarBlock.AXIS, axis);
        }

        /** Boden der Alten: meist Marmorziegel, dazwischen dunkle, rissige Leerenstein-Ziegel. */
        BlockState floor(RandomSource random) {
            int r = random.nextInt(10);
            return r < 6 ? this.bricks : r < 8 ? this.marble : r < 9 ? this.voidBricks : this.cracked;
        }
    }

    /** Ein Bodenstück: Kreis mit Radius {@code r}, ausgefranst; fehlende Platten lassen den Fels darunter sehen. */
    private static void floor(WorldGenLevel level, BlockPos origin, RandomSource random, Materials m, double r) {
        int ir = (int) Math.ceil(r);
        for (int dx = -ir; dx <= ir; dx++) {
            for (int dz = -ir; dz <= ir; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d > r || d > r - 1.5 && random.nextFloat() < 0.45f || random.nextFloat() < 0.08f) {
                    continue;
                }
                BlockPos pos = origin.offset(dx, -1, dz);
                if (VoidBuild.inReach(origin, pos) && level.getBlockState(pos).isSolidRender()) {
                    level.setBlock(pos, m.floor(random), 2);
                }
                // Pflanzen auf dem Boden weg
                BlockPos above = pos.above();
                if (VoidBuild.inReach(origin, above) && level.getBlockState(above).canBeReplaced()) {
                    level.setBlock(above, Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }
    }

    /** Eine Säule: Sockel, Schaft, ggf. Kapitell und Kristall – oder abgebrochen. */
    private static void column(WorldGenLevel level, BlockPos origin, BlockPos base, int height, boolean broken, RandomSource random, Materials m) {
        VoidBuild.place(level, origin, base, m.bricks);
        for (int y = 1; y < height; y++) {
            VoidBuild.place(level, origin, base.above(y), m.pillar(Direction.Axis.Y));
        }
        if (!broken) {
            VoidBuild.place(level, origin, base.above(height), m.chiseled);
            if (random.nextInt(3) == 0) {
                VoidBuild.place(level, origin, base.above(height + 1), m.cluster);
            }
        } else if (random.nextBoolean()) {
            VoidBuild.place(level, origin, base.above(height), m.brickSlab);
        }
    }

    private static void colonnade(WorldGenLevel level, BlockPos origin, RandomSource random, Materials m) {
        floor(level, origin, random, m, 6.5);
        boolean alongX = random.nextBoolean();
        Direction along = alongX ? Direction.EAST : Direction.SOUTH;
        Direction across = along.getClockWise();
        for (int row = -1; row <= 1; row += 2) {
            for (int i = -2; i <= 2; i++) {
                BlockPos base = origin.relative(along, i * 3).relative(across, row * 3);
                boolean broken = random.nextInt(3) > 0;
                int h = broken ? 1 + random.nextInt(5) : 6;
                column(level, origin, base, h, broken, random, m);
            }
        }
        // umgestürzte Säulentrommeln
        for (int k = 0; k < 2; k++) {
            Direction lie = random.nextBoolean() ? along : across;
            BlockPos start = origin.relative(along, random.nextInt(9) - 4).relative(across, random.nextInt(3) - 1);
            int len = 2 + random.nextInt(3);
            for (int l = 0; l < len; l++) {
                VoidBuild.place(level, origin, start.relative(lie, l), m.pillar(lie.getAxis()));
            }
        }
        // ein Stück Gebälk schwebt noch über der Halle
        int y = 7 + random.nextInt(4);
        for (int i = -3; i <= 2; i++) {
            VoidBuild.place(level, origin, origin.relative(along, i).relative(across, 3).above(y + (i > 0 ? 1 : 0)), m.bricks);
        }
    }

    private static void arch(WorldGenLevel level, BlockPos origin, RandomSource random, Materials m) {
        floor(level, origin, random, m, 5.5);
        Direction side = random.nextBoolean() ? Direction.EAST : Direction.SOUTH;
        int half = 3 + random.nextInt(2);
        int pier = 4 + random.nextInt(3);
        boolean broken = random.nextBoolean();
        for (int s = -1; s <= 1; s += 2) {
            BlockPos base = origin.relative(side, s * half);
            int h = broken && s > 0 ? 2 + random.nextInt(2) : pier;
            for (int y = 0; y < h; y++) {
                VoidBuild.place(level, origin, base.above(y), y % 3 == 2 ? m.marble : m.bricks);
                VoidBuild.place(level, origin, base.above(y).relative(side, s), y == 0 ? m.bricks : m.marble);
            }
        }
        // Bogen (Halbkreis) – beim gebrochenen Bogen nur die linke Hälfte
        for (int i = 0; i <= 18; i++) {
            double a = Math.PI * i / 18.0;
            if (broken && a < Math.PI / 2 - 0.1) {
                continue;
            }
            int dx = (int) Math.round(Math.cos(a) * half);
            int dy = (int) Math.round(Math.sin(a) * half);
            BlockPos at = origin.relative(side, dx).above(pier + dy - 1);
            VoidBuild.place(level, origin, at, i == 9 ? m.chiseled : m.bricks);
        }
        // der Schlussstein des gebrochenen Bogens schwebt darüber
        if (broken) {
            VoidBuild.place(level, origin, origin.above(pier + half + 3), m.chiseled);
            VoidBuild.place(level, origin, origin.relative(side, 1).above(pier + half + 2), m.bricks);
        }
    }

    private static void shrine(WorldGenLevel level, BlockPos origin, RandomSource random, Materials m) {
        floor(level, origin, random, m, 5.0);
        // Stufenpodest
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                boolean edge = Math.abs(dx) == 2 || Math.abs(dz) == 2;
                VoidBuild.place(level, origin, origin.offset(dx, 0, dz), edge
                        ? m.brickSlab.setValue(SlabBlock.TYPE, SlabType.BOTTOM) : m.bricks);
            }
        }
        // Altar mit Kristall
        VoidBuild.place(level, origin, origin.above(), m.chiseled);
        VoidBuild.place(level, origin, origin.above(2), m.crystal);
        // vier Kristallsäulen
        for (int[] c : new int[][] {{-3, -3}, {3, -3}, {-3, 3}, {3, 3}}) {
            column(level, origin, origin.offset(c[0], 0, c[1]), 2 + random.nextInt(3), random.nextInt(3) == 0, random, m);
        }
        // Truhe hinter dem Altar
        Direction back = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        BlockPos chestPos = origin.relative(back).above();
        if (VoidBuild.place(level, origin, chestPos, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, back.getOpposite()))
                && level.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
            chest.setLootTable(ModLootTables.VOID_RUIN_CHEST, random.nextLong());
        }
    }

    private static void stair(WorldGenLevel level, BlockPos origin, RandomSource random, Materials m) {
        floor(level, origin, random, m, 4.0);
        int height = 8 + random.nextInt(6);
        for (int y = 0; y < height; y++) {
            VoidBuild.place(level, origin, origin.above(y), m.pillar(Direction.Axis.Y));
        }
        Direction[] around = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
        int start = random.nextInt(4);
        for (int y = 0; y < height; y++) {
            if (random.nextInt(6) == 0) {
                continue;   // fehlende Stufe
            }
            Direction d = around[(start + y) % 4];
            Direction next = around[(start + y + 1) % 4];
            BlockPos step = origin.relative(d).above(y);
            VoidBuild.place(level, origin, step, m.brickStairs.setValue(StairBlock.FACING, next).setValue(StairBlock.HALF, Half.BOTTOM));
            VoidBuild.place(level, origin, origin.relative(d).relative(next).above(y), m.brickSlab);
        }
        // oben endet die Treppe an einer schwebenden Plattform
        BlockPos top = origin.above(height + 2);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (random.nextInt(5) > 0) {
                    VoidBuild.place(level, origin, top.offset(dx, 0, dz), m.bricks);
                }
            }
        }
        VoidBuild.place(level, origin, top.above(), m.crystal);
    }

    private static void tower(WorldGenLevel level, BlockPos origin, RandomSource random, Materials m) {
        floor(level, origin, random, m, 5.5);
        int radius = 3;
        Direction door = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        for (int i = 0; i < 24; i++) {
            double a = Math.PI * 2 * i / 24;
            int dx = (int) Math.round(Math.cos(a) * radius);
            int dz = (int) Math.round(Math.sin(a) * radius);
            int h = 3 + (int) (4 * (0.5 + 0.5 * Math.sin(a * 2 + random.nextDouble()))) + random.nextInt(3);
            for (int y = 0; y < h; y++) {
                BlockPos at = origin.offset(dx, y, dz);
                boolean doorway = y < 3 && Math.abs(dx - door.getStepX() * radius) + Math.abs(dz - door.getStepZ() * radius) <= 1;
                boolean window = y == 4 && i % 6 == 0;
                if (!doorway && !window) {
                    VoidBuild.place(level, origin, at, y % 4 == 3 ? m.marble : m.bricks);
                }
            }
        }
        // Schutt und eine Truhe drinnen
        for (int k = 0; k < 4; k++) {
            VoidBuild.place(level, origin, origin.offset(random.nextInt(9) - 4, 0, random.nextInt(9) - 4),
                    random.nextBoolean() ? m.brickSlab : m.bricks);
        }
        BlockPos chestPos = origin.relative(door.getOpposite(), 2);
        if (random.nextInt(3) > 0 && VoidBuild.place(level, origin, chestPos, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, door))
                && level.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
            chest.setLootTable(ModLootTables.VOID_RUIN_CHEST, random.nextLong());
        }
    }

    /** Schwebende Trümmer über der Ruine: Mauerstücke, Säulentrommeln, Stufen. */
    static void debris(WorldGenLevel level, BlockPos origin, RandomSource random, Materials m, int count) {
        for (int k = 0; k < count; k++) {
            BlockPos at = origin.offset(random.nextInt(13) - 6, 4 + random.nextInt(10), random.nextInt(13) - 6);
            switch (random.nextInt(4)) {
                case 0 -> {
                    Direction.Axis axis = random.nextBoolean() ? Direction.Axis.X : Direction.Axis.Z;
                    for (int l = 0; l < 2 + random.nextInt(2); l++) {
                        VoidBuild.place(level, origin, at.relative(axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH, l),
                                m.pillar(axis));
                    }
                }
                case 1 -> {
                    for (int dx = 0; dx < 2 + random.nextInt(2); dx++) {
                        for (int dy = 0; dy < 2; dy++) {
                            if (random.nextInt(4) > 0) {
                                VoidBuild.place(level, origin, at.offset(dx, dy, 0), m.bricks);
                            }
                        }
                    }
                }
                case 2 -> VoidBuild.place(level, origin, at, m.brickStairs.setValue(StairBlock.FACING,
                        Direction.Plane.HORIZONTAL.getRandomDirection(random)).setValue(StairBlock.HALF, random.nextBoolean() ? Half.TOP : Half.BOTTOM));
                default -> {
                    VoidBuild.place(level, origin, at, m.marble);
                    VoidBuild.place(level, origin, at.above(), m.cluster);
                }
            }
        }
    }

    /** Für FloatingDebrisFeature: schwebende Trümmer allein (ohne Ruine darunter). */
    static void debrisOnly(WorldGenLevel level, BlockPos origin, RandomSource random, int count) {
        debris(level, origin, random, new Materials(), count);
    }
}
