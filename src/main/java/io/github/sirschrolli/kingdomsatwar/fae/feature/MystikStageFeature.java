package io.github.sirschrolli.kingdomsatwar.fae.feature;

import com.mojang.serialization.MapCodec;
import io.github.sirschrolli.kingdomsatwar.fae.MystikStage;
import io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.MystikBlocks;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * Die Zauberbühne: eine verlassene Freilichtbühne im Feenreich, auf der MissMystik in ihrem großen Zylinder wartet.
 * <ul>
 *     <li>Die Lichtung wird eingeebnet (Feengras und Moos) und geht am Rand sanft ins Gelände über.</li>
 *     <li>Eine Bühne aus Glimmerholz auf einem Sockel aus Mondsteinziegeln, zwei Blöcke hoch, nach vorn mit einer runden
 *     Vorbühne; an ihrer Kante leuchten Rampenlichter aus gemeißeltem Mondstein, vorn führen Stufen hinauf.</li>
 *     <li>Ein Bühnenportal aus Mondstein (Säulen, Querbalken mit Giebel und einem leuchtenden Stern), darunter eine
 *     Bordüre und zu beiden Seiten schwere Samtvorhänge mit Goldfransen; hinten ein Vorhang über die ganze Breite.</li>
 *     <li>Mitten auf der Bühne der große Zauberzylinder in einem Kreis aus Teppich mit vier goldenen Strahlen.</li>
 *     <li>Davor ein Zuschauerrund: drei ansteigende Reihen Bänke im Halbkreis mit einem Gang in der Mitte, Laternenpfähle,
 *     ein Weg aus Feensteinziegeln; zu beiden Seiten der Bühne Riesenpilze, Leuchtranken hängen vom Gerüst.</li>
 * </ul>
 * Wo die Bühnen liegen, bestimmt {@link MystikStage} (höchstens eine pro Feld, immer in der Mitte eines Chunks).
 */
public record MystikStageFeature() implements Feature {
    public static final MapCodec<MystikStageFeature> CODEC = MapCodec.unit(MystikStageFeature::new);
    /** Bis hierher ist die Lichtung eben … */
    static final double INNER = 16.5;
    /** … bis hierher geht sie in das Gelände über. */
    static final double OUTER = 23.5;
    /** Lage des Zylinders: so weit hinter der Mitte (nach Norden) und so hoch über dem Boden der Lichtung. */
    public static final int HAT_DZ = -6;
    public static final int HAT_HEIGHT = 3;

    @Override
    public MapCodec<MystikStageFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        int chunkX = SectionPos.blockToSectionCoord(origin.getX());
        int chunkZ = SectionPos.blockToSectionCoord(origin.getZ());
        if (!MystikStage.isStageChunk(level.getSeed(), chunkX, chunkZ)) {
            return false;
        }
        return build(level, generator, random, SectionPos.sectionToBlockCoord(chunkX) + 8, SectionPos.sectionToBlockCoord(chunkZ) + 8);
    }

    /** Baut die Bühne um ({@code cx}, {@code cz}) – ohne Prüfung des Felds (für Tests). */
    public static boolean build(WorldGenLevel level, ChunkGenerator generator, RandomSource random, int cx, int cz) {
        int floor = MondhainFeature.floorHeight(level, cx, cz);
        new Stage(level, generator, random, cx, cz, floor).build();
        return true;
    }

    /** Liegt (dx, dz) auf der Bühne (Hauptbühne und runde Vorbühne)? */
    static boolean onStage(int dx, int dz) {
        if (Math.abs(dx) <= 9 && dz >= -11 && dz <= -3) {
            return true;
        }
        return dz > -3 && dx * dx + (dz + 3) * (dz + 3) <= 49;
    }

    private static final class Stage {
        private final WorldGenLevel level;
        private final ChunkGenerator generator;
        private final RandomSource random;
        private final int cx;
        private final int cz;
        private final int floor;
        private final int top;
        private final BlockPos origin;
        private final Set<Long> reserved = new HashSet<>();

        Stage(WorldGenLevel level, ChunkGenerator generator, RandomSource random, int cx, int cz, int floor) {
            this.level = level;
            this.generator = generator;
            this.random = random;
            this.cx = cx;
            this.cz = cz;
            this.floor = floor;
            this.top = floor + HAT_HEIGHT;
            this.origin = new BlockPos(cx, floor, cz);
        }

        void build() {
            this.terrain();
            this.path();
            this.platform();
            this.steps();
            this.frame();
            this.curtains();
            this.hat();
            this.audience();
            this.lamps();
            this.mushrooms();
            this.vines();
            this.plants();
        }

        // ------------------------------------------------------------ Hilfen

        private BlockPos at(int dx, int y, int dz) {
            return new BlockPos(this.cx + dx, y, this.cz + dz);
        }

        private void set(int dx, int y, int dz, BlockState state) {
            FaeBuild.force(this.level, this.origin, this.at(dx, y, dz), state);
        }

        private void reserve(int dx, int dz) {
            this.reserved.add(BlockPos.asLong(this.cx + dx, 0, this.cz + dz));
        }

        private boolean isReserved(int dx, int dz) {
            return this.reserved.contains(BlockPos.asLong(this.cx + dx, 0, this.cz + dz));
        }

        private static BlockState state(Supplier<? extends Block> block) {
            return block.get().defaultBlockState();
        }

        private static BlockState stairs(Supplier<? extends StairBlock> block, Direction facing) {
            return block.get().defaultBlockState().setValue(StairBlock.FACING, facing);
        }

        private static BlockState slab(Supplier<? extends SlabBlock> slab, SlabType type) {
            return slab.get().defaultBlockState().setValue(SlabBlock.TYPE, type);
        }

        private BlockState moonstone() {
            return this.random.nextFloat() < 0.1f ? state(FaeBlocks.MOSSY_FAESTONE_BRICKS) : state(FaeBlocks.MOONSTONE_BRICKS.base());
        }

        // ------------------------------------------------------------ Gelände

        /** Ebnet die Lichtung ein (abtragen oder aufschütten) und lässt sie am Rand sanft ins Gelände übergehen. */
        private void terrain() {
            int reach = (int) Math.ceil(OUTER);
            for (int dx = -reach; dx <= reach; dx++) {
                for (int dz = -reach; dz <= reach; dz++) {
                    double r = Math.sqrt(dx * dx + dz * dz);
                    if (r > OUTER) {
                        continue;
                    }
                    int x = this.cx + dx;
                    int z = this.cz + dz;
                    if (!FaeBuild.inReach(this.origin, new BlockPos(x, this.floor, z))) {
                        continue;
                    }
                    int surfaceTop = FaeBuild.surface(this.level, x, z) - 1;
                    int ground = MondhainFeature.groundAt(this.level, x, z);
                    double t = r <= INNER ? 0.0 : smooth((r - INNER) / (OUTER - INNER));
                    int target = (int) Math.round(Mth.lerp(t, this.floor, ground));
                    boolean water = surfaceTop > ground && this.level.getFluidState(new BlockPos(x, surfaceTop, z)).is(FluidTags.WATER);
                    boolean underwater = water && target < surfaceTop && r > INNER;
                    if (target == ground && !water && t > 0.0) {
                        continue;
                    }
                    if (ground > target || !underwater) {
                        int clearTo = water ? Math.max(surfaceTop, ground) + 1 : ground + 6;
                        for (int y = target + 1; y <= clearTo; y++) {
                            BlockPos pos = new BlockPos(x, y, z);
                            BlockState old = this.level.getBlockState(pos);
                            if (old.isAir() || (r > INNER && (old.is(BlockTags.LEAVES) || old.is(BlockTags.LOGS)))) {
                                continue;
                            }
                            FaeBuild.force(this.level, this.origin, pos, underwater && y <= surfaceTop ? Blocks.WATER.defaultBlockState()
                                    : Blocks.AIR.defaultBlockState());
                        }
                    }
                    for (int y = ground + 1; y <= target; y++) {
                        FaeBuild.force(this.level, this.origin, new BlockPos(x, y, z), y < target - 3 ? state(FaeBlocks.FAESTONE.base())
                                : state(FaeBlocks.FAE_SOIL));
                    }
                    if (underwater) {
                        continue;
                    }
                    BlockState surface = r <= INNER && this.random.nextFloat() < 0.25f ? state(FaeBlocks.FAE_MOSS) : state(FaeBlocks.FAE_GRASS);
                    FaeBuild.force(this.level, this.origin, new BlockPos(x, target, z), surface);
                    for (int y = target - 1; y >= target - 2; y--) {
                        BlockPos pos = new BlockPos(x, y, z);
                        if (!FaeBuild.soil(this.level.getBlockState(pos))) {
                            FaeBuild.force(this.level, this.origin, pos, state(FaeBlocks.FAE_SOIL));
                        }
                    }
                }
            }
        }

        private static double smooth(double t) {
            t = Mth.clamp(t, 0.0, 1.0);
            return t * t * (3.0 - 2.0 * t);
        }

        /** Ein Weg aus alten Feensteinziegeln vom Rand der Lichtung durch den Gang der Zuschauer bis zu den Stufen. */
        private void path() {
            for (int dz = 5; dz <= 22; dz++) {
                for (int dx = -1; dx <= 1; dx++) {
                    if (dx * dx + dz * dz > OUTER * OUTER) {
                        continue;
                    }
                    this.reserve(dx, dz);
                    if (this.random.nextFloat() < 0.85f) {
                        this.set(dx, this.floor, dz, this.random.nextFloat() < 0.3f ? state(FaeBlocks.MOSSY_FAESTONE_BRICKS)
                                : state(FaeBlocks.FAESTONE_BRICKS.base()));
                    }
                }
            }
        }

        // ------------------------------------------------------------ Bühne

        private static boolean edge(int dx, int dz) {
            return onStage(dx, dz) && (!onStage(dx + 1, dz) || !onStage(dx - 1, dz) || !onStage(dx, dz + 1) || !onStage(dx, dz - 1));
        }

        /** Sockel aus Mondsteinziegeln, oben Glimmerholz; an der Vorderkante Rampenlichter aus gemeißeltem Mondstein. */
        private void platform() {
            for (int dx = -10; dx <= 10; dx++) {
                for (int dz = -12; dz <= 5; dz++) {
                    if (!onStage(dx, dz)) {
                        continue;
                    }
                    this.reserve(dx, dz);
                    boolean rim = edge(dx, dz);
                    this.set(dx, this.floor, dz, state(FaeBlocks.FAESTONE_BRICKS.base()));
                    this.set(dx, this.floor + 1, dz, rim ? this.moonstone() : state(FaeBlocks.FAESTONE_BRICKS.base()));
                    BlockState topBlock;
                    if (rim) {
                        boolean front = dz > -3;
                        boolean light = front ? (dx + dz) % 2 == 0 : Math.floorMod(dx, 3) == 0;
                        topBlock = light ? state(FaeBlocks.CHISELED_MOONSTONE) : state(FaeBlocks.MOONSTONE_BRICKS.base());
                    } else {
                        topBlock = state(FaeBlocks.GLIMMERWOOD_PLANKS.base());
                    }
                    this.set(dx, this.floor + 2, dz, topBlock);
                    for (int y = this.top; y <= this.top + 12; y++) {
                        this.set(dx, y, dz, Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }

        /** Stufen vorn in der Mitte (aus dem Gang der Zuschauer hinauf). */
        private void steps() {
            for (int dx = -1; dx <= 1; dx++) {
                this.reserve(dx, 5);
                this.reserve(dx, 6);
                this.set(dx, this.floor + 1, 6, stairs(FaeBlocks.GLIMMERWOOD_PLANKS.stairs(), Direction.NORTH));
                this.set(dx, this.floor + 1, 5, state(FaeBlocks.GLIMMERWOOD_PLANKS.base()));
                this.set(dx, this.floor + 2, 5, stairs(FaeBlocks.GLIMMERWOOD_PLANKS.stairs(), Direction.NORTH));
                this.set(dx, this.floor + 2, 4, state(FaeBlocks.GLIMMERWOOD_PLANKS.base()));
            }
        }

        /**
         * Das Gerüst: vier Säulen aus Mondstein, oben ein umlaufender Balken; vorn das Bühnenportal mit Giebel und einem
         * leuchtenden Stern obenauf.
         */
        private void frame() {
            int beam = this.floor + 11;
            for (int sx = -1; sx <= 1; sx += 2) {
                for (int dz : new int[] {-4, -11}) {
                    int dx = 10 * sx;
                    this.reserve(dx, dz);
                    for (int y = this.floor; y < beam; y++) {
                        boolean capital = y == this.floor + 4 || y == this.floor + 8;
                        this.set(dx, y, dz, capital ? state(FaeBlocks.CHISELED_MOONSTONE) : this.moonstone());
                    }
                }
            }
            for (int dx = -10; dx <= 10; dx++) {
                boolean middle = Math.abs(dx) <= 1;
                this.set(dx, beam, -4, middle ? state(FaeBlocks.CHISELED_MOONSTONE) : state(FaeBlocks.MOONSTONE_BRICKS.base()));
                this.set(dx, beam, -11, Math.floorMod(dx, 4) == 0 ? state(FaeBlocks.CHISELED_MOONSTONE) : state(FaeBlocks.MOONSTONE_BRICKS.base()));
            }
            for (int dz = -10; dz <= -5; dz++) {
                this.set(-10, beam, dz, state(FaeBlocks.MOONSTONE_BRICKS.base()));
                this.set(10, beam, dz, state(FaeBlocks.MOONSTONE_BRICKS.base()));
            }
            // Giebel über dem Portal: Treppen zu beiden Seiten, oben ein leuchtender Stern
            for (int dx = -5; dx <= 5; dx++) {
                int rise = 5 - Math.abs(dx);
                int height = rise / 2;
                for (int h = 1; h <= height; h++) {
                    this.set(dx, beam + h, -4, state(FaeBlocks.MOONSTONE_BRICKS.base()));
                }
                if (dx != 0) {
                    BlockState cap = rise % 2 == 1 ? stairs(FaeBlocks.MOONSTONE_BRICKS.stairs(), dx < 0 ? Direction.EAST : Direction.WEST)
                            : slab(FaeBlocks.MOONSTONE_BRICKS.slab(), SlabType.BOTTOM);
                    this.set(dx, beam + height + 1, -4, cap);
                }
            }
            this.set(0, beam + 3, -4, state(FaeBlocks.CHISELED_MOONSTONE));
            this.set(0, beam + 4, -4, state(FaeBlocks.CHISELED_MOONSTONE));
        }

        /** Bordüre unter dem Portal, schwere Seitenvorhänge mit Fransen, hinten ein Vorhang über die ganze Breite. */
        private void curtains() {
            BlockState velvet = state(MystikBlocks.VELVET_CURTAIN);
            BlockState fringe = state(MystikBlocks.VELVET_CURTAIN_FRINGE);
            int beam = this.floor + 11;
            for (int dx = -9; dx <= 9; dx++) {
                this.set(dx, beam - 1, -4, Math.abs(dx) >= 7 ? velvet : fringe);
            }
            for (int sx = -1; sx <= 1; sx += 2) {
                for (int k = 7; k <= 9; k++) {
                    int dx = k * sx;
                    // gerafft: der innere Streifen endet höher (wie ein zur Seite gezogener Vorhang)
                    int bottom = k == 7 ? this.top + 2 : this.top;
                    for (int y = bottom; y < beam - 1; y++) {
                        this.set(dx, y, -4, y == bottom ? fringe : velvet);
                    }
                }
            }
            for (int dx = -9; dx <= 9; dx++) {
                for (int y = this.top; y < beam; y++) {
                    this.set(dx, y, -11, y == this.top ? fringe : velvet);
                }
            }
        }

        /** Der große Zauberzylinder in einem Kreis aus magentafarbenem Teppich mit vier goldenen Strahlen. */
        private void hat() {
            for (int dx = -4; dx <= 4; dx++) {
                for (int dz = HAT_DZ - 4; dz <= HAT_DZ + 4; dz++) {
                    double r = Math.sqrt(dx * dx + (dz - HAT_DZ) * (dz - HAT_DZ));
                    boolean ring = r >= 1.4 && r < 2.6;
                    boolean ray = r >= 2.6 && r < 4.3 && (dx == 0 || dz == HAT_DZ);
                    if (ring) {
                        this.set(dx, this.top, dz, Blocks.CARPET.pick(net.minecraft.world.item.DyeColor.MAGENTA).defaultBlockState());
                    } else if (ray) {
                        this.set(dx, this.top, dz, Blocks.CARPET.pick(net.minecraft.world.item.DyeColor.YELLOW).defaultBlockState());
                    } else if (r < 1.4 && !(dx == 0 && dz == HAT_DZ)) {
                        this.set(dx, this.top, dz, Blocks.CARPET.pick(net.minecraft.world.item.DyeColor.PURPLE).defaultBlockState());
                    }
                }
            }
            this.set(0, this.top, HAT_DZ, state(MystikBlocks.STAGE_HAT));
        }

        // ------------------------------------------------------------ Zuschauer

        /** Drei ansteigende Reihen Bänke im Halbkreis vor der Bühne, in der Mitte ein Gang. */
        private void audience() {
            double[] radii = {11.0, 13.0, 15.0};
            for (int dx = -16; dx <= 16; dx++) {
                for (int dz = 5; dz <= 13; dz++) {
                    if (Math.abs(dx) <= 1) {
                        continue;
                    }
                    double d = Math.sqrt(dx * dx + (dz + 3) * (dz + 3));
                    for (int row = 0; row < radii.length; row++) {
                        if (d < radii[row] - 0.5 || d >= radii[row] + 0.5) {
                            continue;
                        }
                        this.reserve(dx, dz);
                        for (int y = this.floor + 1; y < this.floor + 1 + row; y++) {
                            this.set(dx, y, dz, state(FaeBlocks.GLIMMERWOOD_PLANKS.base()));
                        }
                        Direction facing = Math.abs(dx) > (dz + 3) ? (dx < 0 ? Direction.WEST : Direction.EAST) : Direction.SOUTH;
                        this.set(dx, this.floor + 1 + row, dz, stairs(FaeBlocks.GLIMMERWOOD_PLANKS.stairs(), facing));
                    }
                }
            }
        }

        /** Laternenpfähle: zwei Zaunpfosten aus Glimmerholz, obenauf gemeißelter Mondstein. */
        private void lamps() {
            int[][] spots = {{-12, 2}, {12, 2}, {-15, 9}, {15, 9}, {-4, 15}, {4, 15}, {-12, -12}, {12, -12}};
            for (int[] p : spots) {
                if (this.isReserved(p[0], p[1])) {
                    continue;
                }
                this.reserve(p[0], p[1]);
                this.set(p[0], this.floor + 1, p[1], state(FaeBlocks.GLIMMERWOOD_FENCE));
                this.set(p[0], this.floor + 2, p[1], state(FaeBlocks.GLIMMERWOOD_FENCE));
                this.set(p[0], this.floor + 3, p[1], state(FaeBlocks.CHISELED_MOONSTONE));
            }
        }

        /** Zu beiden Seiten der Bühne ein Riesenpilz. */
        private void mushrooms() {
            GiantMushroomFeature mushroom = new GiantMushroomFeature(GiantMushroomFeature.Kind.RANDOM, false);
            for (int sx = -1; sx <= 1; sx += 2) {
                BlockPos at = this.at(15 * sx, this.floor + 1, -8);
                mushroom.place(this.level, this.generator, this.random, at);
            }
        }

        /** Leuchtranken hängen vom Gerüst über den Seiten der Bühne. */
        private void vines() {
            int beam = this.floor + 11;
            for (int sx = -1; sx <= 1; sx += 2) {
                for (int dz : new int[] {-9, -7, -6}) {
                    FaeBuild.hangVines(this.level, this.origin, this.at(10 * sx, beam, dz), 5, this.random);
                }
            }
        }

        /** Blumen am Rand der Lichtung (nicht auf Wegen, Bänken und der Bühne). */
        private void plants() {
            BlockState[] flowers = {state(FaeBlocks.MOONBELL), state(FaeBlocks.FAE_ROSE), state(FaeBlocks.FAE_SPROUTS)};
            for (int i = 0; i < 70; i++) {
                double a = this.random.nextDouble() * Math.PI * 2.0;
                double r = 10.0 + this.random.nextDouble() * 9.0;
                int dx = (int) Math.round(Math.cos(a) * r);
                int dz = (int) Math.round(Math.sin(a) * r);
                if (this.isReserved(dx, dz) || onStage(dx, dz)) {
                    continue;
                }
                FaeBuild.plant(this.level, this.origin, this.at(dx, this.floor + 1, dz), flowers[this.random.nextInt(flowers.length)]);
            }
        }
    }
}
