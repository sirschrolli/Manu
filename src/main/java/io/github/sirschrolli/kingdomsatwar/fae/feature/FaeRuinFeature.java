package io.github.sirschrolli.kingdomsatwar.fae.feature;

import com.mojang.serialization.MapCodec;
import io.github.sirschrolli.kingdomsatwar.loot.ModLootTables;
import io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * Die Ruinen des Feenreichs – Reste eines Volkes, das hier lange vor den Feen Mondstein brach:
 * <ul>
 *     <li><b>Mondsteinkreis</b> – ein Ring aus stehenden Steinen mit leuchtenden Mondsicheln obenauf, einige
 *     umgestürzt, in der Mitte ein Altar;</li>
 *     <li><b>Feenpavillon</b> – ein runder Boden mit Säulen, von deren Gebälk nur Bruchstücke blieben, in der Mitte ein
 *     Brunnenbecken;</li>
 *     <li><b>Feentor</b> – ein frei stehender Bogen, umrankt von Leuchtranken, ringsum Trümmer;</li>
 *     <li><b>Turmstumpf</b> – ein runder Turm, dessen Mauern unterschiedlich hoch abgebrochen sind, mit Resten einer
 *     Wendeltreppe.</li>
 * </ul>
 * In vielen liegt eine Truhe mit Feenstaub, Mondstein, Gold und Büchern.
 */
public record FaeRuinFeature() implements Feature {
    public static final MapCodec<FaeRuinFeature> CODEC = MapCodec.unit(FaeRuinFeature::new);

    @Override
    public MapCodec<FaeRuinFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        if (!level.getFluidState(origin).isEmpty() || !FaeBuild.ground(level.getBlockState(origin.below()))) {
            return false;
        }
        // nur auf einigermaßen ebenem Grund
        for (int dx = -5; dx <= 5; dx += 5) {
            for (int dz = -5; dz <= 5; dz += 5) {
                int y = FaeBuild.surface(level, origin.getX() + dx, origin.getZ() + dz);
                if (Math.abs(y - origin.getY()) > 3) {
                    return false;
                }
            }
        }
        Ruin ruin = new Ruin(level, origin, random);
        switch (random.nextInt(4)) {
            case 0 -> ruin.henge();
            case 1 -> ruin.pavilion();
            case 2 -> ruin.arch();
            default -> ruin.tower();
        }
        return true;
    }

    private static final class Ruin {
        private final WorldGenLevel level;
        private final BlockPos origin;
        private final RandomSource random;

        Ruin(WorldGenLevel level, BlockPos origin, RandomSource random) {
            this.level = level;
            this.origin = origin;
            this.random = random;
        }

        // ------------------------------------------------------------ Baustoffe

        private BlockState bricks() {
            float r = this.random.nextFloat();
            return r < 0.35f ? FaeBlocks.MOSSY_FAESTONE_BRICKS.get().defaultBlockState()
                    : r < 0.45f ? FaeBlocks.FAESTONE.base().get().defaultBlockState()
                    : FaeBlocks.FAESTONE_BRICKS.base().get().defaultBlockState();
        }

        private BlockState moonstone() {
            return this.random.nextFloat() < 0.15f ? FaeBlocks.MOSSY_FAESTONE_BRICKS.get().defaultBlockState()
                    : FaeBlocks.MOONSTONE_BRICKS.base().get().defaultBlockState();
        }

        private static BlockState chiseled() {
            return FaeBlocks.CHISELED_MOONSTONE.get().defaultBlockState();
        }

        private static BlockState stairs(Direction facing, boolean top) {
            return FaeBlocks.MOONSTONE_BRICKS.stairs().get().defaultBlockState().setValue(StairBlock.FACING, facing)
                    .setValue(StairBlock.HALF, top ? Half.TOP : Half.BOTTOM);
        }

        private static BlockState slab(boolean top) {
            return FaeBlocks.MOONSTONE_BRICKS.slab().get().defaultBlockState().setValue(SlabBlock.TYPE, top ? SlabType.TOP : SlabType.BOTTOM);
        }

        // ------------------------------------------------------------ Werkzeuge

        private void set(BlockPos pos, BlockState state) {
            FaeBuild.force(this.level, this.origin, pos, state);
        }

        /** Ein Block mit Fundament: darunter wird bis zum festen Grund aufgefüllt (höchstens sechs Blöcke). */
        private void footed(BlockPos pos, BlockState state) {
            this.set(pos, state);
            BlockPos.MutableBlockPos below = pos.mutable().move(Direction.DOWN);
            for (int i = 0; i < 6 && FaeBuild.openOrWater(this.level.getBlockState(below)); i++) {
                this.set(below, this.bricks());
                below.move(Direction.DOWN);
            }
        }

        /** Der Boden an dieser Stelle (Höhe der ersten Luft über dem Grund). */
        private BlockPos ground(int x, int z) {
            return new BlockPos(x, FaeBuild.surface(this.level, x, z), z);
        }

        /** Räumt Pflanzen und Gras weg. */
        private void clear(BlockPos pos, int height) {
            for (int h = 0; h < height; h++) {
                BlockPos p = pos.above(h);
                BlockState state = this.level.getBlockState(p);
                if (!state.isAir() && FaeBuild.open(state)) {
                    this.set(p, Blocks.AIR.defaultBlockState());
                }
            }
        }

        /** Eine Säule aus {@code height} Blöcken, oben (wenn heil) eine leuchtende Mondsichel. */
        private void pillar(BlockPos base, int height, boolean crowned) {
            this.footed(base, this.bricks());
            for (int h = 1; h < height; h++) {
                this.set(base.above(h), h == height - 1 && crowned ? chiseled() : this.moonstone());
            }
        }

        /** Eine Säule, die umgestürzt ist und nun in Richtung {@code dir} auf dem Boden liegt. */
        private void fallen(BlockPos base, Direction dir, int length) {
            for (int k = 1; k <= length; k++) {
                BlockPos at = base.relative(dir, k);
                BlockPos ground = this.ground(at.getX(), at.getZ());
                if (Math.abs(ground.getY() - base.getY()) <= 2) {
                    this.clear(ground, 2);
                    this.set(ground, k == length ? chiseled() : this.moonstone());
                }
            }
        }

        /** Leuchtranken und Moos: die Natur holt sich die Ruine zurück. */
        private void overgrow(int radius, int height) {
            for (int i = 0; i < radius * 6; i++) {
                int x = this.origin.getX() + this.random.nextInt(radius * 2 + 1) - radius;
                int z = this.origin.getZ() + this.random.nextInt(radius * 2 + 1) - radius;
                for (int y = this.origin.getY() + height; y > this.origin.getY(); y--) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = this.level.getBlockState(pos);
                    if (!state.isAir()) {
                        if (state.isSolidRender() && this.random.nextFloat() < 0.5f) {
                            FaeBuild.hangVines(this.level, this.origin, pos, 4, this.random);
                        }
                        break;
                    }
                }
            }
            BlockState rose = FaeBlocks.FAE_ROSE.get().defaultBlockState();
            BlockState lantern = FaeBlocks.LANTERN_MUSHROOM.get().defaultBlockState();
            for (int i = 0; i < radius * 2; i++) {
                int x = this.origin.getX() + this.random.nextInt(radius * 2 + 3) - radius - 1;
                int z = this.origin.getZ() + this.random.nextInt(radius * 2 + 3) - radius - 1;
                BlockPos pos = this.ground(x, z);
                if (Math.abs(pos.getY() - this.origin.getY()) <= 3) {
                    FaeBuild.plant(this.level, this.origin, pos, this.random.nextBoolean() ? rose : lantern);
                }
            }
        }

        private void chest(BlockPos pos, Direction facing) {
            this.clear(pos, 2);
            this.set(pos, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing));
            if (this.level.getBlockEntity(pos) instanceof ChestBlockEntity chest) {
                chest.setLootTable(ModLootTables.FAE_RUIN_CHEST, this.random.nextLong());
            }
        }

        // ------------------------------------------------------------ Mondsteinkreis

        void henge() {
            int stones = 7 + this.random.nextInt(3);
            double radius = 5.5 + this.random.nextDouble() * 1.5;
            for (int i = 0; i < stones; i++) {
                double a = (i + this.random.nextDouble() * 0.3) * Math.PI * 2.0 / stones;
                int x = this.origin.getX() + (int) Math.round(Math.cos(a) * radius);
                int z = this.origin.getZ() + (int) Math.round(Math.sin(a) * radius);
                BlockPos base = this.ground(x, z);
                if (Math.abs(base.getY() - this.origin.getY()) > 3) {
                    continue;
                }
                this.clear(base, 5);
                float state = this.random.nextFloat();
                if (state < 0.2f) {
                    // umgestürzt: liegt nach außen
                    this.footed(base, this.bricks());
                    Direction out = Direction.getApproximateNearest(Math.cos(a), 0.0, Math.sin(a));
                    this.fallen(base, out, 3 + this.random.nextInt(2));
                } else if (state < 0.4f) {
                    this.pillar(base, 1 + this.random.nextInt(2), false);    // abgebrochen
                } else {
                    this.pillar(base, 3 + this.random.nextInt(3), true);
                }
            }
            // der Altar
            BlockPos center = this.ground(this.origin.getX(), this.origin.getZ());
            this.clear(center, 3);
            this.footed(center, FaeBlocks.FAESTONE_BRICKS.base().get().defaultBlockState());
            this.set(center.above(), slab(false));
            for (Direction side : Direction.Plane.HORIZONTAL) {
                if (this.random.nextFloat() < 0.7f) {
                    BlockPos step = center.relative(side);
                    this.clear(step, 2);
                    this.footed(step, stairs(side.getOpposite(), false));
                }
            }
            if (this.random.nextFloat() < 0.55f) {
                Direction side = Direction.Plane.HORIZONTAL.getRandomDirection(this.random);
                this.chest(center.relative(side, 2), side);
            }
            // Moos auf dem Grund im Kreis
            for (int dx = -6; dx <= 6; dx++) {
                for (int dz = -6; dz <= 6; dz++) {
                    if (dx * dx + dz * dz < 26 && this.random.nextFloat() < 0.45f) {
                        BlockPos g = this.ground(this.origin.getX() + dx, this.origin.getZ() + dz).below();
                        if (this.level.getBlockState(g).is(FaeBlocks.FAE_GRASS.get())) {
                            this.set(g, FaeBlocks.FAE_MOSS.get().defaultBlockState());
                        }
                    }
                }
            }
            this.overgrow(6, 6);
        }

        // ------------------------------------------------------------ Feenpavillon

        void pavilion() {
            int r = 4;
            int floorY = this.origin.getY();
            // der Boden: ein runder Ziegelboden mit Moosflecken
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (dx * dx + dz * dz > r * r + 1) {
                        continue;
                    }
                    BlockPos pos = new BlockPos(this.origin.getX() + dx, floorY - 1, this.origin.getZ() + dz);
                    this.footed(pos, this.random.nextFloat() < 0.12f ? FaeBlocks.FAE_MOSS.get().defaultBlockState() : this.bricks());
                    this.clear(pos.above(), 6);
                }
            }
            // das Brunnenbecken in der Mitte
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos pos = new BlockPos(this.origin.getX() + dx, floorY - 1, this.origin.getZ() + dz);
                    if (dx == 0 && dz == 0) {
                        this.set(pos, Blocks.WATER.defaultBlockState());
                        this.set(pos.below(), this.moonstone());
                    } else {
                        this.set(pos.above(), slab(false));
                    }
                }
            }
            this.set(new BlockPos(this.origin.getX(), floorY - 2, this.origin.getZ()), chiseled());
            // die Säulen und das, was vom Gebälk blieb
            int columns = 6;
            double turn = this.random.nextDouble() * Math.PI;
            int roof = 5;
            BlockPos[] tops = new BlockPos[columns];
            for (int i = 0; i < columns; i++) {
                double a = turn + i * Math.PI * 2.0 / columns;
                BlockPos base = new BlockPos(this.origin.getX() + (int) Math.round(Math.cos(a) * (r - 0.5)), floorY,
                        this.origin.getZ() + (int) Math.round(Math.sin(a) * (r - 0.5)));
                boolean whole = this.random.nextFloat() < 0.6f;
                int height = whole ? roof : 1 + this.random.nextInt(roof - 1);
                for (int h = 0; h < height; h++) {
                    this.set(base.above(h), h == 0 ? FaeBlocks.FAESTONE_BRICKS.base().get().defaultBlockState() : this.moonstone());
                }
                tops[i] = whole ? base.above(roof) : null;
                if (!whole) {
                    Direction out = Direction.getApproximateNearest(Math.cos(a), 0.0, Math.sin(a));
                    this.fallen(base.above(-1), out, 2 + this.random.nextInt(2));
                }
            }
            // Gebälk zwischen benachbarten heilen Säulen
            for (int i = 0; i < columns; i++) {
                BlockPos a = tops[i];
                BlockPos b = tops[(i + 1) % columns];
                if (a == null || b == null || this.random.nextFloat() < 0.25f) {
                    continue;
                }
                int steps = Math.max(Math.abs(a.getX() - b.getX()), Math.abs(a.getZ() - b.getZ()));
                for (int s = 0; s <= steps; s++) {
                    double t = steps == 0 ? 0 : s / (double) steps;
                    BlockPos p = BlockPos.containing(a.getX() + (b.getX() - a.getX()) * t + 0.5, a.getY(), a.getZ() + (b.getZ() - a.getZ()) * t + 0.5);
                    this.set(p, s == 0 || s == steps ? chiseled() : this.moonstone());
                }
            }
            if (this.random.nextFloat() < 0.6f) {
                Direction side = Direction.Plane.HORIZONTAL.getRandomDirection(this.random);
                this.chest(new BlockPos(this.origin.getX(), floorY, this.origin.getZ()).relative(side, 2), side.getOpposite());
            }
            this.overgrow(5, 7);
        }

        // ------------------------------------------------------------ Feentor

        void arch() {
            Direction facing = Direction.Plane.HORIZONTAL.getRandomDirection(this.random);
            Direction side = facing.getClockWise();
            int half = 2;
            int height = 6;
            BlockPos center = this.ground(this.origin.getX(), this.origin.getZ());
            for (int s = -1; s <= 1; s += 2) {
                BlockPos base = center.relative(side, s * (half + 1));
                base = this.ground(base.getX(), base.getZ());
                this.clear(base, height + 1);
                this.footed(base, this.bricks());
                for (int h = 1; h < height; h++) {
                    this.set(base.above(h), this.moonstone());
                }
                // Bogenansatz
                this.set(center.relative(side, s * half).above(height - 2), stairs(s > 0 ? side : side.getOpposite(), true));
                this.set(center.relative(side, s * half).above(height - 1), this.moonstone());
            }
            boolean broken = this.random.nextFloat() < 0.35f;
            for (int k = -half + 1; k <= half - 1; k++) {
                if (broken && k > 0) {
                    break;
                }
                this.set(center.relative(side, k).above(height - 1), k == 0 ? chiseled() : this.moonstone());
                this.set(center.relative(side, k).above(height), slab(false));
            }
            // Trümmer davor und dahinter
            for (int i = 0; i < 7; i++) {
                int x = center.getX() + this.random.nextInt(9) - 4;
                int z = center.getZ() + this.random.nextInt(9) - 4;
                BlockPos g = this.ground(x, z);
                if (Math.abs(g.getY() - center.getY()) <= 2 && this.level.getBlockState(g).isAir()) {
                    this.set(g, this.random.nextBoolean() ? this.moonstone() : slab(false));
                }
            }
            if (this.random.nextFloat() < 0.25f) {
                this.chest(center.relative(facing.getOpposite(), 2), facing);
            }
            this.overgrow(4, height + 1);
        }

        // ------------------------------------------------------------ Turmstumpf

        void tower() {
            int r = 3;
            BlockPos center = this.origin;
            Direction door = Direction.Plane.HORIZONTAL.getRandomDirection(this.random);
            int top = 3 + this.random.nextInt(7);
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    double d = Math.sqrt(dx * dx + dz * dz);
                    BlockPos floor = center.offset(dx, -1, dz);
                    if (d > r + 0.5) {
                        continue;
                    }
                    if (d > r - 0.6) {
                        // die Mauer: oben unregelmäßig abgebrochen
                        int height = Math.max(1, top - this.random.nextInt(4) - (int) Math.round(Math.abs(Math.atan2(dz, dx)) * 1.2));
                        boolean doorway = dx == door.getStepX() * r && dz == door.getStepZ() * r
                                || dx == door.getStepX() * r + door.getClockWise().getStepX() && dz == door.getStepZ() * r + door.getClockWise().getStepZ();
                        this.footed(floor, this.bricks());
                        for (int h = 0; h < height; h++) {
                            if (doorway && h < 3) {
                                this.clear(floor.above(h + 1), 1);
                                continue;
                            }
                            this.set(floor.above(h + 1), this.bricks());
                        }
                    } else {
                        this.footed(floor, this.random.nextFloat() < 0.3f ? FaeBlocks.FAE_MOSS.get().defaultBlockState() : this.bricks());
                        this.clear(floor.above(), 8);
                    }
                }
            }
            // Reste der Wendeltreppe an der Innenwand
            Direction step = door.getOpposite();
            BlockPos at = center.relative(step, r - 1);
            for (int h = 0; h < Math.min(top - 1, 6); h++) {
                this.set(at.above(h), stairs(step.getClockWise(), false));
                step = step.getClockWise();
                at = center.relative(step, r - 1).above(0);
            }
            this.chest(center.relative(door.getOpposite()), door);
            this.set(center.above(-1), chiseled());
            this.overgrow(4, top + 1);
        }
    }
}
