package io.github.sirschrolli.kingdomsatwar.worldgen.village;

import io.github.sirschrolli.kingdomsatwar.registry.BuildingSet;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.VillageBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;

/**
 * Baukasten für die Dorfhäuser: Fundament, Böden, Wände, Fenster, Türen, Dächer und Kleinteile – jeweils so, wie es die
 * Bauweise des Dorfs verlangt. Gebaut wird in den Koordinaten des {@link VillageBuilder} (Vorderseite bei z = 0, nach
 * Norden; x nach rechts; y = 0 ist der Fußboden). Was ein Gebäude selbst entscheidet (welches Dach, welche Läden,
 * Kalk- oder Lehmputz), wird beim Anlegen einmal gewürfelt – so baut jeder Chunk dasselbe Haus.
 */
public final class Arch {
    public final VillageBuilder b;
    public final VillagePalette p;
    public final VillageStyle style;
    public final RandomSource random;
    /** Dachdeckung dieses Hauses (meist die des Dorfs, manchmal die zweite). */
    public final BuildingSet roof;
    public final Block shutter;
    /** Nur bei Lehmbauten: weiß gekalkt statt lehmfarben. */
    public final boolean whitewashed;
    /** Blumenkästen unter den Fenstern. */
    public final boolean flowerBoxes;

    public Arch(VillageBuilder b, VillagePalette p) {
        this.b = b;
        this.p = p;
        this.style = p.style;
        this.random = b.random;
        this.roof = this.random.nextFloat() < 0.3f ? p.roofAlt : p.roof;
        this.shutter = this.style == VillageStyle.TIMBER && this.random.nextFloat() < 0.35f
                ? (p.shutter == ModBlocks.GREEN_SHUTTERS.get() ? ModBlocks.RED_SHUTTERS.get() : ModBlocks.GREEN_SHUTTERS.get())
                : p.shutter;
        this.whitewashed = this.style == VillageStyle.CLAY && this.random.nextFloat() < 0.45f;
        this.flowerBoxes = this.style != VillageStyle.STILT && this.random.nextFloat() < 0.55f;
    }

    // =====================================================================================
    // Baustoffe dieses Hauses
    // =====================================================================================

    public BlockState stone(int x, int y, int z) {
        return this.b.chance(x, y, z, 0.2f) ? this.p.stoneAlt() : this.p.stone();
    }

    /** Putz bzw. Füllung der Wände (Lehmbau: Lehm- oder Kalkputz, Pfahlbau: Bretter). */
    public BlockState fill() {
        return switch (this.style) {
            case TIMBER -> ModBlocks.TIMBER_FRAME.get().defaultBlockState();
            case NORDIC -> this.p.planks();
            case CLAY -> (this.whitewashed ? ModBlocks.WHITE_PLASTER : ModBlocks.CLAY_PLASTER).base().get().defaultBlockState();
            case STILT -> this.p.planks();
        };
    }

    public BuildingSet plaster() {
        return this.whitewashed || this.style != VillageStyle.CLAY ? ModBlocks.WHITE_PLASTER : ModBlocks.CLAY_PLASTER;
    }

    public BlockState roofBlock() {
        return this.roof.base().get().defaultBlockState();
    }

    public Block roofStairs() {
        return this.roof.stairs().get();
    }

    public BlockState roofSlab(SlabType type) {
        return this.roof.slab().get().defaultBlockState().setValue(SlabBlock.TYPE, type);
    }

    public static BlockState stairs(Block block, Direction facing, Half half) {
        return block.defaultBlockState().setValue(StairBlock.FACING, facing).setValue(StairBlock.HALF, half);
    }

    public static BlockState slab(Block block, SlabType type) {
        return block.defaultBlockState().setValue(SlabBlock.TYPE, type);
    }

    // =====================================================================================
    // Fundament und Boden
    // =====================================================================================

    /**
     * Unterbau: unter der Grundfläche bis zum Gelände Sockelstein (bei Pfahlbauten nur Pfähle an den Ecken und alle
     * drei Blöcke, dazwischen bleibt es frei), darüber {@code clear} Blöcke hoch alles freiräumen.
     */
    public void foundation(int x0, int z0, int x1, int z1, int clear) {
        if (this.style == VillageStyle.STILT) {
            for (int x = x0; x <= x1; x++) {
                for (int z = z0; z <= z1; z++) {
                    boolean post = (x == x0 || x == x1 || (x - x0) % 3 == 0) && (z == z0 || z == z1 || (z - z0) % 3 == 0)
                            && (x == x0 || x == x1 || z == z0 || z == z1);
                    this.stilt(x, z, post);
                    this.clearAbove(x, z, clear);
                }
            }
            return;
        }
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                this.b.column(x, z, this.stone(x, 0, z), clear);
            }
        }
    }

    /** Ein Pfahl bis zum Grund (Moor, Wasser) oder – ohne Pfahl – nur das Freiräumen der Stelle unter dem Boden. */
    public void stilt(int x, int z, boolean post) {
        BlockPos top = this.b.world(x, 0, z);
        if (!this.b.chunkBox.isInside(top.getX(), this.b.chunkBox.minY(), top.getZ())) {
            return;
        }
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int y = -1; y > -20; y--) {
            pos.set(top.getX(), top.getY() + y, top.getZ());
            BlockState old = this.b.level.getBlockState(pos);
            if (VillageBuilder.isSolidGround(old)) {
                if (post) {
                    // ein Stück in den Grund getrieben
                    this.b.level.setBlock(pos, this.p.post(Direction.Axis.Y), Block.UPDATE_CLIENTS);
                }
                break;
            }
            if (post) {
                this.b.level.setBlock(pos, this.p.post(Direction.Axis.Y), Block.UPDATE_CLIENTS);
            } else if (!old.isAir() && old.getFluidState().isEmpty()) {
                // Pflanzen, Laub und Äste unter dem Haus weg (Wasser bleibt)
                this.b.level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
        }
    }

    private void clearAbove(int x, int z, int clear) {
        for (int y = 0; y < clear; y++) {
            if (!this.b.get(x, y, z).isAir()) {
                this.b.set(x, y, z, Blocks.AIR.defaultBlockState());
            }
        }
    }

    /** Fußboden bei y = 0: innen Dielen, am Rand Stein (Pfahlbau: Rand aus Balken). */
    public void floor(int x0, int z0, int x1, int z1) {
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                boolean rim = x == x0 || x == x1 || z == z0 || z == z1;
                BlockState state;
                if (!rim) {
                    state = this.p.planks();
                } else if (this.style == VillageStyle.STILT) {
                    state = this.p.beam(x == x0 || x == x1 ? Direction.Axis.Z : Direction.Axis.X);
                } else {
                    state = this.stone(x, 0, z);
                }
                this.b.set(x, 0, z, state);
            }
        }
    }

    // =====================================================================================
    // Wände
    // =====================================================================================

    /** Obergeschoss: die unterste Reihe ist kein Steinsockel, sondern eine Schwelle (Fachwerk) bzw. gewöhnliche Wand. */
    private boolean upper;

    /** Alle vier Wände um das Rechteck von y0 bis y1 (y0 = unterste Reihe über dem Fußboden). */
    public void walls(int x0, int z0, int x1, int z1, int y0, int y1) {
        this.walls(x0, z0, x1, z1, y0, y1, false);
    }

    /** Wie {@link #walls(int, int, int, int, int, int)}; {@code upper}: Wände eines Obergeschosses (ohne Steinsockel). */
    public void walls(int x0, int z0, int x1, int z1, int y0, int y1, boolean upper) {
        this.upper = upper;
        this.wallX(x0, x1, z0, y0, y1);
        this.wallX(x0, x1, z1, y0, y1);
        this.wallZ(z0, z1, x0, y0, y1);
        this.wallZ(z0, z1, x1, y0, y1);
        this.upper = false;
    }

    /** Wand entlang x (bei z) von x0 bis x1, Reihen y0..y1; die Enden sind Ecken. */
    public void wallX(int x0, int x1, int z, int y0, int y1) {
        for (int x = x0; x <= x1; x++) {
            for (int y = y0; y <= y1; y++) {
                this.b.set(x, y, z, this.wallBlock(x == x0 || x == x1, x - x0, x1 - x, y, y0, y1, Direction.Axis.X, x, z));
            }
        }
    }

    /** Wand entlang z (bei x) von z0 bis z1. */
    public void wallZ(int z0, int z1, int x, int y0, int y1) {
        for (int z = z0; z <= z1; z++) {
            for (int y = y0; y <= y1; y++) {
                this.b.set(x, y, z, this.wallBlock(z == z0 || z == z1, z - z0, z1 - z, y, y0, y1, Direction.Axis.Z, x, z));
            }
        }
    }

    /**
     * Der Block einer Wand an einer Stelle: {@code fromStart}/{@code fromEnd} = Abstand zu den Ecken, {@code axis} =
     * Richtung der Wand.
     */
    private BlockState wallBlock(boolean corner, int fromStart, int fromEnd, int y, int y0, int y1, Direction.Axis axis, int x, int z) {
        switch (this.style) {
            case TIMBER -> {
                if (corner) {
                    return ModBlocks.TIMBER_BEAM.get().defaultBlockState().setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS,
                            Direction.Axis.Y);
                }
                if (y == y0 && !this.upper) {
                    return this.stone(x, y, z);
                }
                if (y == y1 || y == y0) {
                    return ModBlocks.TIMBER_BEAM.get().defaultBlockState().setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS, axis);
                }
                // Streben neben den Ecken in der obersten Gefachreihe
                if (y == y1 - 1 && y1 - y0 >= 3 && (fromStart == 1 || fromEnd == 1)) {
                    return ModBlocks.TIMBER_FRAME_DIAGONAL.get().defaultBlockState();
                }
                return ModBlocks.TIMBER_FRAME.get().defaultBlockState();
            }
            case NORDIC -> {
                if (y == y0 && !this.upper) {
                    return this.stone(x, y, z);
                }
                if (corner) {
                    // verkämmte Ecke: die dunklen Stämme liegen abwechselnd in beiden Richtungen
                    return this.p.post((y & 1) == 0 ? Direction.Axis.X : Direction.Axis.Z);
                }
                // dazwischen geschälte, helle Stämme
                return this.p.beam(axis);
            }
            case CLAY -> {
                if (y == y0 && !this.upper || corner && (y & 1) == 0) {
                    return this.p.stone();
                }
                return this.fill();
            }
            case STILT -> {
                if (corner) {
                    return this.p.post(Direction.Axis.Y);
                }
                if (y == y1 || y == y0) {
                    return this.p.beam(axis);
                }
                return this.p.planks();
            }
        }
        return this.fill();
    }

    /** Aus der Wand ragende Deckenbalken (Lehmbau) entlang der Wand bei z, auf Höhe y, nach {@code out}. */
    public void vigas(int x0, int x1, int z, int y, Direction out) {
        if (this.style != VillageStyle.CLAY) {
            return;
        }
        for (int x = x0 + 1; x < x1; x += 2) {
            this.b.set(x + out.getStepX(), y, z + out.getStepZ(), this.p.beam(out.getAxis()));
        }
    }

    /** Wie {@link #vigas(int, int, int, int, Direction)}, für Wände entlang z (bei x). */
    public void vigasZ(int z0, int z1, int x, int y, Direction out) {
        if (this.style != VillageStyle.CLAY) {
            return;
        }
        for (int z = z0 + 1; z < z1; z += 2) {
            this.b.set(x + out.getStepX(), y, z + out.getStepZ(), this.p.beam(out.getAxis()));
        }
    }

    // =====================================================================================
    // Türen und Fenster
    // =====================================================================================

    /**
     * Haustür bei (x, 1, z); {@code in} ist die Richtung ins Haus. Darüber ein Sturz (Fachwerk: Andreaskreuz, Lehm: Balken).
     */
    public void door(int x, int z, Direction in) {
        this.door(x, z, in, this.p.door());
    }

    public void door(int x, int z, Direction in, Block door) {
        this.b.door(x, 1, z, door, in);
        if (this.b.entrance == null && in == Direction.SOUTH) {
            this.b.entrance = new BlockPos(x, 1, z - 1);
        }
        switch (this.style) {
            case TIMBER -> this.b.set(x, 3, z, ModBlocks.TIMBER_FRAME_CROSS.get());
            case CLAY -> {
                // Sturz aus Sandstein und ein buntes Sonnensegel über der Tür
                this.b.set(x, 3, z, Blocks.CUT_SANDSTONE.defaultBlockState());
                Direction along = in.getClockWise();
                int ox = x - in.getStepX();
                int oz = z - in.getStepZ();
                for (int w = -1; w <= 1; w++) {
                    Block wool = w == 0 ? Blocks.WOOL_SLAB.white() : this.awning();
                    this.b.set(ox + along.getStepX() * w, 3, oz + along.getStepZ() * w, slab(wool, SlabType.TOP));
                }
            }
            default -> {
            }
        }
    }

    /**
     * Fenster in der Wand bei (x, y..y+h-1, z); {@code out} zeigt hinaus. Je nach Bauweise mit offenen Läden, einem
     * Blumenkasten darunter, einem Balken darüber oder einem kleinen Schutzdach.
     */
    public void window(int x, int y, int z, Direction out, int h) {
        for (int i = 0; i < h; i++) {
            this.b.set(x, y + i, z, this.p.pane);
        }
        Direction along = out.getClockWise();
        int ox = x + out.getStepX();
        int oz = z + out.getStepZ();
        switch (this.style) {
            case TIMBER, NORDIC -> {
                for (int side = -1; side <= 1; side += 2) {
                    for (int i = 0; i < h; i++) {
                        this.shutter(ox + along.getStepX() * side, y + i, oz + along.getStepZ() * side, out, i == 0 ? Half.BOTTOM : Half.TOP);
                    }
                }
            }
            case CLAY -> {
                this.b.set(x, y + h, z, Blocks.CUT_SANDSTONE.defaultBlockState());
                if (this.p.shutter == Blocks.WARPED_TRAPDOOR) {
                    for (int side = -1; side <= 1; side += 2) {
                        for (int i = 0; i < h; i++) {
                            this.shutter(ox + along.getStepX() * side, y + i, oz + along.getStepZ() * side, out, i == 0 ? Half.BOTTOM : Half.TOP);
                        }
                    }
                }
            }
            case STILT -> {
                for (int side = -1; side <= 1; side += 2) {
                    for (int i = 0; i < h; i++) {
                        this.shutter(ox + along.getStepX() * side, y + i, oz + along.getStepZ() * side, out, i == 0 ? Half.BOTTOM : Half.TOP);
                    }
                }
            }
        }
        if (this.flowerBoxes && y >= 2 && this.style != VillageStyle.CLAY) {
            this.flowerBox(ox, y - 1, oz);
        }
    }

    /** Kleines Schutzdach: eine geschlossene Klappe in der oberen Blockhälfte (über Verkaufsfenstern). */
    public BlockState shutterAwning() {
        return this.shutter.defaultBlockState().setValue(TrapDoorBlock.HALF, Half.TOP).setValue(TrapDoorBlock.OPEN, false);
    }

    /** Offener Fensterladen vor der Wand (das Brett liegt flach an der Wand). */
    public void shutter(int x, int y, int z, Direction out, Half half) {
        // ein offener Laden liegt an der Seite seines Blocks, die seiner Blickrichtung gegenüberliegt – Blickrichtung
        // nach draußen heißt: das Brett liegt an der Hauswand
        this.b.set(x, y, z, this.shutter.defaultBlockState().setValue(TrapDoorBlock.FACING, out).setValue(TrapDoorBlock.OPEN, true)
                .setValue(TrapDoorBlock.HALF, half));
    }

    /** Blumenkasten: eine Holzstufe mit einem Blumentopf darauf, außen unter dem Fenster. */
    public void flowerBox(int x, int y, int z) {
        if (!this.b.get(x, y, z).isAir() && !this.b.get(x, y, z).canBeReplaced()) {
            return;
        }
        this.b.set(x, y, z, slab(this.p.trim.slab(), SlabType.TOP));
        this.b.set(x, y + 1, z, this.flower(x, y, z));
    }

    /** Ein Topf mit einer Blume, die zum Land passt. */
    public BlockState flower(int x, int y, int z) {
        Block[] choices = switch (this.style) {
            case TIMBER -> new Block[] {Blocks.POTTED_RED_TULIP, Blocks.POTTED_POPPY, ModBlocks.POTTED_LAVENDER.get(), ModBlocks.POTTED_GARDEN_ROSE.get(),
                    Blocks.POTTED_OXEYE_DAISY, Blocks.POTTED_CORNFLOWER};
            case NORDIC -> new Block[] {Blocks.POTTED_SPRUCE_SAPLING, Blocks.POTTED_FERN, Blocks.POTTED_LILY_OF_THE_VALLEY, Blocks.POTTED_BLUE_ORCHID,
                    ModBlocks.POTTED_FROST_FLOWER.get()};
            case CLAY -> new Block[] {Blocks.POTTED_CACTUS, Blocks.POTTED_DEAD_BUSH, Blocks.POTTED_ORANGE_TULIP, ModBlocks.POTTED_FIRE_LILY.get(),
                    Blocks.POTTED_ACACIA_SAPLING};
            case STILT -> new Block[] {Blocks.POTTED_BLUE_ORCHID, Blocks.POTTED_FERN, Blocks.POTTED_BROWN_MUSHROOM, Blocks.POTTED_RED_MUSHROOM,
                    ModBlocks.POTTED_MOONFLOWER.get()};
        };
        Block pick = choices[(int) (this.b.noise(x, y, z) * choices.length) % choices.length];
        return pick instanceof FlowerPotBlock ? pick.defaultBlockState() : Blocks.FLOWER_POT.defaultBlockState();
    }

    // =====================================================================================
    // Dächer
    // =====================================================================================

    /**
     * Satteldach über dem Rechteck (Wände bei x0/x1 und z0/z1, Mauerkrone auf Höhe {@code y}). {@code alongZ}: der First
     * läuft entlang z (Giebel vorn und hinten), sonst entlang x (Giebel links und rechts). {@code steep}: doppelt so
     * steil (nordische Häuser). Die Traufe steht einen Block über, ebenso das Dach an den Giebeln. Gibt die Höhe des
     * Firsts zurück.
     */
    public int gable(int x0, int z0, int x1, int z1, int y, boolean alongZ, boolean steep) {
        int rise = steep ? 2 : 1;
        // in Dach-Koordinaten: u quer zum First (dort steigt das Dach), v entlang des Firsts
        int u0 = alongZ ? x0 : z0;
        int u1 = alongZ ? x1 : z1;
        int v0 = alongZ ? z0 : x0;
        int v1 = alongZ ? z1 : x1;
        int lo = u0 - 1;
        int hi = u1 + 1;
        int top = 0;
        for (int u = lo; u <= hi; u++) {
            int i = Math.min(u - lo, hi - u);
            int imax = (hi - lo) / 2;
            boolean apex = i == imax && (hi - lo) % 2 == 0;
            Direction up = u - lo < hi - u ? (alongZ ? Direction.EAST : Direction.SOUTH) : (alongZ ? Direction.WEST : Direction.NORTH);
            int s = stepHeight(y, i, rise);
            int base = i == 0 ? s : Math.max(y + 1, stepHeight(y, i - 1, rise) + 1);
            for (int v = v0 - 1; v <= v1 + 1; v++) {
                int x = alongZ ? u : v;
                int z = alongZ ? v : u;
                if (apex) {
                    int ridge = stepHeight(y, i - 1, rise);
                    for (int h = Math.max(y + 1, ridge); h < ridge + rise; h++) {
                        this.b.set(x, h, z, this.roofBlock());
                    }
                    this.b.set(x, ridge + rise, z, this.roofSlab(SlabType.BOTTOM));
                    top = ridge + rise;
                } else {
                    for (int h = base; h < s; h++) {
                        this.b.set(x, h, z, this.roofBlock());
                    }
                    this.b.set(x, s, z, stairs(this.roofStairs(), up, Half.BOTTOM));
                    top = Math.max(top, s);
                }
            }
            // Giebelfelder vorn und hinten (nur innerhalb der Wände)
            if (u >= u0 && u <= u1) {
                int under = apex ? Math.max(y + 1, stepHeight(y, i - 1, rise)) : (i == 0 ? s : base);
                for (int h = y + 1; h < under; h++) {
                    for (int v : new int[] {v0, v1}) {
                        int x = alongZ ? u : v;
                        int z = alongZ ? v : u;
                        this.b.set(x, h, z, this.gableFill(u == u0 || u == u1, h, y));
                    }
                }
            }
        }
        return top;
    }

    private static int stepHeight(int y, int i, int rise) {
        return rise == 1 || i == 0 ? y + i : y + 2 * i - 1;
    }

    /** Füllung der Giebel: Fachwerk, Bretter, Putz. */
    private BlockState gableFill(boolean edge, int h, int y) {
        return switch (this.style) {
            case TIMBER -> h == y + 1 ? ModBlocks.TIMBER_FRAME.get().defaultBlockState() : ModBlocks.TIMBER_FRAME.get().defaultBlockState();
            case NORDIC -> this.p.planks();
            case CLAY -> this.fill();
            case STILT -> edge ? this.p.post(Direction.Axis.Y) : this.p.planks();
        };
    }

    /**
     * Zeltdach bzw. Walmdach über dem Rechteck: Ring für Ring nach innen, ein Block Überstand. Gibt die Höhe der Spitze
     * zurück.
     */
    public int hip(int x0, int z0, int x1, int z1, int y) {
        return this.b.hipRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, y, this.roofStairs(), this.roofBlock());
    }

    /**
     * Flachdach (Lehmbau): eine Decke aus Putz bei y, außen herum eine niedrige Brüstung, hier und da ein Wasserspeier.
     */
    public void flatRoof(int x0, int z0, int x1, int z1, int y) {
        BuildingSet plaster = this.plaster();
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                boolean rim = x == x0 || x == x1 || z == z0 || z == z1;
                this.b.set(x, y, z, rim ? this.p.stone() : plaster.base().get().defaultBlockState());
                if (rim) {
                    boolean corner = (x == x0 || x == x1) && (z == z0 || z == z1);
                    this.b.set(x, y + 1, z, corner ? plaster.base().get().defaultBlockState() : slab(plaster.slab().get(), SlabType.BOTTOM));
                    // Töpfe mit Pflanzen auf einigen Ecken der Brüstung
                    if (corner && this.b.chance(x, y + 2, z, 0.5f)) {
                        this.b.set(x, y + 2, z, this.flower(x, y + 2, z));
                    }
                }
            }
        }
    }

    // =====================================================================================
    // Kleinteile
    // =====================================================================================

    /** Schornstein von y0 bis y1 (Ziegel bzw. Feldstein), oben eine Stufe. */
    public void chimney(int x, int z, int y0, int y1) {
        BlockState brick = this.style == VillageStyle.NORDIC ? this.p.stone() : Blocks.BRICKS.defaultBlockState();
        for (int y = y0; y <= y1; y++) {
            this.b.set(x, y, z, brick);
        }
        this.b.set(x, y1 + 1, z, this.style == VillageStyle.NORDIC ? slab(this.p.stoneSlab(), SlabType.BOTTOM)
                : slab(Blocks.BRICK_SLAB, SlabType.BOTTOM));
    }

    public void hangingLantern(int x, int y, int z) {
        this.b.set(x, y, z, this.p.lantern.defaultBlockState().setValue(LanternBlock.HANGING, true));
    }

    public void standingLantern(int x, int y, int z) {
        this.b.set(x, y, z, this.p.lantern.defaultBlockState().setValue(LanternBlock.HANGING, false));
    }

    /** Laterne an einem kleinen Ausleger neben der Tür (Ausleger bei (x, y, z), die Laterne hängt darunter). */
    public void bracketLantern(int x, int y, int z) {
        this.b.set(x, y, z, this.p.fence());
        this.hangingLantern(x, y - 1, z);
    }

    /** Stufe vor der Tür (Treppe, die zum Haus hin ansteigt), außen bei (x, 0, z). */
    public void doorStep(int x, int z, Direction toHouse) {
        this.b.set(x, 0, z, stairs(this.p.stoneStairs(), toHouse, Half.BOTTOM));
    }

    /** Ein Holzstuhl (Treppe) mit Blick nach {@code facing}. */
    public BlockState chair(Direction facing) {
        return ModBlocks.OAK_CHAIR.get().defaultBlockState().setValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING, facing);
    }

    public BlockState bench(Direction facing) {
        return ModBlocks.OAK_BENCH.get().defaultBlockState().setValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING, facing);
    }

    public BlockState table() {
        return ModBlocks.OAK_TABLE.get().defaultBlockState();
    }

    /** Farbe der Sonnensegel im Lehmdorf (je Haus eine). */
    public Block awning() {
        Block[] colors = {Blocks.WOOL_SLAB.orange(), Blocks.WOOL_SLAB.cyan(), Blocks.WOOL_SLAB.yellow(), Blocks.WOOL_SLAB.red(),
                Blocks.WOOL_SLAB.blue()};
        return colors[Math.floorMod((int) (this.b.noise(0, 99, 0) * 1000), colors.length)];
    }

    /** Teppichfarbe des Hauses. */
    public Block carpet() {
        Block[] carpets = switch (this.style) {
            case TIMBER -> new Block[] {Blocks.CARPET.red(), Blocks.CARPET.green(), Blocks.CARPET.brown()};
            case NORDIC -> new Block[] {Blocks.CARPET.gray(), Blocks.CARPET.lightGray(), Blocks.CARPET.red()};
            case CLAY -> new Block[] {Blocks.CARPET.orange(), Blocks.CARPET.cyan(), Blocks.CARPET.yellow()};
            case STILT -> new Block[] {Blocks.CARPET.green(), Blocks.CARPET.brown(), Blocks.CARPET.lime()};
        };
        return carpets[Math.floorMod(this.p.variant + (this.roof == this.p.roof ? 0 : 1), carpets.length)];
    }

    /** Bettfarbe (die einfachen Häuser schlafen auf Stroh). */
    public Block bed(boolean straw) {
        if (straw) {
            return Blocks.STRAW_BED;
        }
        return switch (this.style) {
            case TIMBER -> this.p.variant == 0 ? Blocks.BED.red() : Blocks.BED.green();
            case NORDIC -> Blocks.BED.gray();
            case CLAY -> Blocks.BED.orange();
            case STILT -> Blocks.BED.brown();
        };
    }
}
