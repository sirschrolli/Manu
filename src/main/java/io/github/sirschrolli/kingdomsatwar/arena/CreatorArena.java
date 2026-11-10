package io.github.sirschrolli.kingdomsatwar.arena;

import io.github.sirschrolli.kingdomsatwar.block.creator.CreatorAltarBlock;
import io.github.sirschrolli.kingdomsatwar.block.creator.CreatorAltarBlockEntity;
import io.github.sirschrolli.kingdomsatwar.registry.CreatorBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChorusPlantBlock;
import net.minecraft.world.level.block.EndRodBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Baut die Schöpfung, den Hof Schrollis des Schöpfers – die größte aller Arenen, frei über einem Meer aus goldenen Wolken:
 * <ul>
 *     <li>der <b>Schöpfungskreis</b> (Halbmesser 42): ein Boden aus weißem Quarz und Gold – in der Mitte eine Sonne mit
 *     zwölf Strahlen, Ringe aus Gold, ein Sternenfeld, zwölf Felder wie die Zeichen des Himmels; darunter ein Kegel aus
 *     Kalzit mit Goldadern, an seiner Spitze ein Kern aus Licht;</li>
 *     <li>zwölf <b>Säulen</b> mit Flammen aus Licht (hinter ihnen findet man Schutz vor der Supernova);</li>
 *     <li>im Norden der <b>Thron</b> auf seinen Stufen, dahinter eine Sonnenscheibe – davor das Abbild des Altars;</li>
 *     <li>vier <b>Brücken</b> zu vier <b>Welten</b>: ein Garten, eine Glutwelt, eine Eiswelt und ein Stück End;</li>
 *     <li>hoch darüber die <b>Räder des Himmels</b> (drei Ringe aus Gold) und ringsum schwebende Felsen.</li>
 * </ul>
 * Alle Blöcke werden ohne Nachbar-Aktualisierungen gesetzt (Pflanzen, Chorus und Laub halten so, wie sie gebaut sind).
 */
public final class CreatorArena {
    /** Höhe des Bodens (die Mitte der Arena liegt einen Block darüber). */
    public static final int FLOOR_Y = 140;
    public static final int RADIUS = 42;
    /** Wer so nah am Altar steht, wird mitgezogen. */
    public static final double CALL_RADIUS = 16.0;
    /** Der Altar vor dem Thron (relativ zur Mitte): drei Stufen hoch, 33 Blöcke nördlich. */
    public static final int THRONE_Z = -36;
    public static final int ALTAR_Z = -31;
    public static final int DAIS_HEIGHT = 3;
    static final int PILLAR_RING = 35;
    static final int ISLAND_DISTANCE = 80;
    static final int ISLAND_RADIUS = 15;
    private static final int QUIET = ArenaIsland.QUIET;

    private CreatorArena() {}

    /** Baut den Hof um {@code center} (ein Block über dem Boden) und gibt zurück, wo das Abbild des Altars steht. */
    public static BlockPos build(ServerLevel level, BlockPos center) {
        int cx = center.getX();
        int cz = center.getZ();
        int y0 = center.getY() - 1;
        BlockPos floor = new BlockPos(cx, y0, cz);
        platform(level, floor);
        for (int k = 0; k < 12; k++) {
            double a = Math.toRadians(15.0 + k * 30.0);
            pillar(level, floor.offset((int) Math.round(Math.cos(a) * PILLAR_RING), 0, (int) Math.round(Math.sin(a) * PILLAR_RING)));
        }
        BlockPos altar = throne(level, floor);
        for (int k = 0; k < 4; k++) {
            double a = Math.toRadians(45.0 + k * 90.0);
            bridge(level, floor, a);
            BlockPos island = floor.offset((int) Math.round(Math.cos(a) * ISLAND_DISTANCE), 0, (int) Math.round(Math.sin(a) * ISLAND_DISTANCE));
            island(level, island, k);
        }
        rings(level, floor);
        rocks(level, floor);
        if (level.getBlockEntity(altar) instanceof CreatorAltarBlockEntity be) {
            be.setup(Vec3.atBottomCenterOf(center));
        }
        return altar;
    }

    private static void set(ServerLevel level, BlockPos.MutableBlockPos p, int x, int y, int z, BlockState state) {
        level.setBlock(p.set(x, y, z), state, QUIET);
    }

    private static double hash(int x, int z, int salt) {
        return ArenaIsland.rand(x, z, salt);
    }

    // =====================================================================================
    // Der Schöpfungskreis
    // =====================================================================================

    private static void platform(ServerLevel level, BlockPos floor) {
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        int y0 = floor.getY();
        BlockState smooth = Blocks.SMOOTH_QUARTZ.defaultBlockState();
        BlockState quartz = Blocks.QUARTZ_BLOCK.defaultBlockState();
        BlockState bricks = Blocks.QUARTZ_BRICKS.defaultBlockState();
        BlockState chiseled = Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState();
        BlockState calcite = Blocks.CALCITE.defaultBlockState();
        BlockState gold = Blocks.GOLD_BLOCK.defaultBlockState();
        BlockState rawGold = Blocks.RAW_GOLD_BLOCK.defaultBlockState();
        BlockState glow = Blocks.GLOWSTONE.defaultBlockState();
        BlockState pearl = Blocks.PEARLESCENT_FROGLIGHT.defaultBlockState();
        BlockState ochre = Blocks.OCHRE_FROGLIGHT.defaultBlockState();
        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                double r = Math.sqrt(dx * dx + dz * dz);
                double edge = RADIUS + (hash(dx, dz, 3) - 0.5) * 0.6;
                if (r > edge) {
                    continue;
                }
                int x = floor.getX() + dx;
                int z = floor.getZ() + dz;
                double a = Math.atan2(dz, dx);
                double deg = (Math.toDegrees(a) + 360.0) % 360.0;
                BlockState top;
                if (r <= 1.2) {
                    top = pearl;
                } else if (r <= 3.5) {
                    top = gold;
                } else if (r <= 4.6) {
                    top = rawGold;
                } else if (r < 13.5) {
                    // zwölf Sonnenstrahlen, nach außen schmaler
                    double ray = Math.abs(((deg + 15.0) % 30.0) - 15.0);
                    double width = 7.5 - (r - 4.6) * 0.55;
                    top = ray < width ? (ray < width * 0.35 && r > 6.0 ? ochre : gold) : smooth;
                } else if (r < 14.6) {
                    top = gold;
                } else if (r < 27.5) {
                    // das Sternenfeld: zwölf feine Linien und Sterne aus Licht
                    double line = Math.abs((deg % 30.0) - 15.0);
                    if (line < 28.6 / r && r > 16.0) {
                        top = chiseled;
                    } else {
                        double h = hash(dx, dz, 11);
                        top = h > 0.965 ? glow : h > 0.93 ? pearl : smooth;
                    }
                } else if (r < 28.6) {
                    top = gold;
                } else if (r < 38.0) {
                    // zwölf Felder wie die Zeichen des Himmels, mit Goldlinien getrennt
                    double seam = Math.abs(((deg + 15.0) % 30.0) - 15.0);
                    int field = (int) (((deg + 15.0) % 360.0) / 30.0);
                    if (seam > 15.0 - 31.5 / r) {
                        top = gold;
                    } else if (Math.abs(r - 33.2) < 0.6 && Math.abs(seam) < 2.0) {
                        top = glow;
                    } else {
                        top = field % 2 == 0 ? bricks : calcite;
                    }
                } else if (r < 40.0) {
                    top = ((int) (deg / 10.0)) % 2 == 0 ? chiseled : gold;
                } else {
                    top = bricks;
                }
                set(level, p, x, y0, z, top);
                set(level, p, x, y0 - 1, z, quartz);
                set(level, p, x, y0 - 2, z, calcite);
                underside(level, p, x, z, y0, r, dx, dz);
            }
        }
        // der Kern aus Licht an der Spitze des Kegels
        int tip = y0 - 34;
        for (int dy = 0; dy < 5; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (Math.abs(dx) + Math.abs(dz) <= (dy < 2 ? 2 : dy < 4 ? 1 : 0)) {
                        set(level, p, floor.getX() + dx, tip - dy, floor.getZ() + dz, dy % 2 == 0 ? pearl : ochre);
                    }
                }
            }
        }
        for (int k = 0; k < 8; k++) {
            double a = Math.toRadians(k * 45.0);
            int x = floor.getX() + (int) Math.round(Math.cos(a) * 2.0);
            int z = floor.getZ() + (int) Math.round(Math.sin(a) * 2.0);
            set(level, p, x, tip - 2, z, Blocks.END_ROD.defaultBlockState().setValue(EndRodBlock.FACING, Direction.DOWN));
        }
    }

    /** Die Unterseite: ein hohler Kegel aus Kalzit und Quarz mit Goldadern (zwei Blöcke dick), der in einer Spitze endet. */
    private static void underside(ServerLevel level, BlockPos.MutableBlockPos p, int x, int z, int y0, double r, int dx, int dz) {
        int depth = 32;
        for (int d = 3; d <= depth; d++) {
            double k = d / (double) depth;
            double radius = RADIUS * (1.0 - Math.pow(k, 1.25)) + (ArenaIsland.noise(dx * 0.7, d * 1.3, 0.25, 5) - 0.5) * 2.4;
            if (r > radius) {
                return;
            }
            if (r < radius - 2.2 && d < depth - 2) {
                continue;
            }
            double vein = ArenaIsland.noise(dx + d * 0.6, dz - d * 0.4, 0.18, 9);
            BlockState state;
            if (vein > 0.72) {
                state = Blocks.RAW_GOLD_BLOCK.defaultBlockState();
            } else if (vein > 0.69) {
                state = Blocks.GOLD_BLOCK.defaultBlockState();
            } else if (d % 7 == 0) {
                state = Blocks.QUARTZ_BRICKS.defaultBlockState();
            } else {
                state = hash(x, z + d * 31, 13) > 0.25 ? Blocks.CALCITE.defaultBlockState() : Blocks.SMOOTH_QUARTZ.defaultBlockState();
            }
            set(level, p, x, y0 - d, z, state);
        }
    }

    /** Eine Säule: Sockel aus gemeißeltem Quarz, Schaft aus Quarzsäulen, ein goldenes Kapitell und darauf eine Flamme aus Licht. */
    private static void pillar(ServerLevel level, BlockPos base) {
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        int x = base.getX();
        int y = base.getY();
        int z = base.getZ();
        int height = 15;
        BlockState shaft = Blocks.QUARTZ_PILLAR.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (Math.abs(dx) + Math.abs(dz) <= 3) {
                    set(level, p, x + dx, y + 1, z + dz, Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState());
                }
            }
        }
        for (int dy = 2; dy <= height; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    boolean corner = Math.abs(dx) == 1 && Math.abs(dz) == 1;
                    if (corner && dy % 4 != 0) {
                        continue;
                    }
                    set(level, p, x + dx, y + dy, z + dz, corner ? Blocks.GOLD_BLOCK.defaultBlockState() : shaft);
                }
            }
        }
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (Math.abs(dx) + Math.abs(dz) <= 3) {
                    set(level, p, x + dx, y + height + 1, z + dz, Math.abs(dx) + Math.abs(dz) == 3 ? Blocks.RAW_GOLD_BLOCK.defaultBlockState()
                            : Blocks.GOLD_BLOCK.defaultBlockState());
                }
            }
        }
        set(level, p, x, y + height + 2, z, Blocks.PEARLESCENT_FROGLIGHT.defaultBlockState());
        set(level, p, x, y + height + 3, z, Blocks.OCHRE_FROGLIGHT.defaultBlockState());
        set(level, p, x, y + height + 4, z, Blocks.END_ROD.defaultBlockState().setValue(EndRodBlock.FACING, Direction.UP));
        for (Direction d : Direction.Plane.HORIZONTAL) {
            set(level, p, x + d.getStepX(), y + height + 2, z + d.getStepZ(), Blocks.END_ROD.defaultBlockState().setValue(EndRodBlock.FACING, d));
        }
    }

    // =====================================================================================
    // Der Thron
    // =====================================================================================

    /** Der Thron im Norden: drei Stufen, ein goldener Sitz, eine hohe Lehne mit der Sonnenscheibe. Gibt den Platz des Altars zurück. */
    private static BlockPos throne(ServerLevel level, BlockPos floor) {
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        int x0 = floor.getX();
        int y0 = floor.getY();
        int z0 = floor.getZ();
        // die Stufen (halbrund nach Süden)
        for (int step = 1; step <= DAIS_HEIGHT; step++) {
            double reach = 11.5 - step * 1.6;
            for (int dx = -12; dx <= 12; dx++) {
                for (int dz = -12; dz <= 6; dz++) {
                    double r = Math.sqrt(dx * dx + Math.max(0, dz) * Math.max(0, dz) * 1.6);
                    if (r > reach || dz < -9 + step) {
                        continue;
                    }
                    BlockState s = r > reach - 1.0 ? Blocks.GOLD_BLOCK.defaultBlockState() : step == DAIS_HEIGHT ? Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState()
                            : Blocks.QUARTZ_BRICKS.defaultBlockState();
                    set(level, p, x0 + dx, y0 + step, z0 + THRONE_Z + 4 + dz, s);
                }
            }
        }
        int ty = y0 + DAIS_HEIGHT + 1;
        int tz = z0 + THRONE_Z;
        // Sitz und Armlehnen
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = 0; dz <= 2; dz++) {
                set(level, p, x0 + dx, ty, tz + dz, Blocks.GOLD_BLOCK.defaultBlockState());
            }
            set(level, p, x0 + dx, ty - 1, tz + 3, Blocks.GOLD_BLOCK.defaultBlockState());
        }
        for (int side : new int[] {-3, 3}) {
            for (int dz = 0; dz <= 2; dz++) {
                set(level, p, x0 + side, ty + 1, tz + dz, Blocks.RAW_GOLD_BLOCK.defaultBlockState());
                set(level, p, x0 + side, ty, tz + dz, Blocks.QUARTZ_PILLAR.defaultBlockState());
            }
            set(level, p, x0 + side, ty + 2, tz + 2, Blocks.PEARLESCENT_FROGLIGHT.defaultBlockState());
        }
        // die hohe Lehne, oben die Sonnenscheibe mit Strahlen
        for (int dy = 1; dy <= 8; dy++) {
            int half = dy < 6 ? 2 : 1;
            for (int dx = -half; dx <= half; dx++) {
                set(level, p, x0 + dx, ty + dy, tz - 1, dx == 0 && dy % 2 == 0 ? Blocks.OCHRE_FROGLIGHT.defaultBlockState()
                        : Blocks.GOLD_BLOCK.defaultBlockState());
            }
        }
        int sy = ty + 13;
        for (int dx = -7; dx <= 7; dx++) {
            for (int dy = -7; dy <= 7; dy++) {
                double r = Math.sqrt(dx * dx + dy * dy);
                double deg = (Math.toDegrees(Math.atan2(dy, dx)) + 360.0) % 360.0;
                BlockState s = null;
                if (r <= 1.5) {
                    s = Blocks.PEARLESCENT_FROGLIGHT.defaultBlockState();
                } else if (r <= 3.6) {
                    s = Blocks.GOLD_BLOCK.defaultBlockState();
                } else if (r <= 4.5) {
                    s = Blocks.RAW_GOLD_BLOCK.defaultBlockState();
                } else if (r <= 7.4 && Math.abs(((deg + 15.0) % 30.0) - 15.0) < 4.5 - (r - 4.5) * 0.9) {
                    s = r > 6.5 ? Blocks.GLOWSTONE.defaultBlockState() : Blocks.GOLD_BLOCK.defaultBlockState();
                }
                if (s != null) {
                    set(level, p, x0 + dx, sy + dy, tz - 2, s);
                }
            }
        }
        // das Abbild des Altars vor dem Thron, zur Mitte gewandt
        BlockPos altar = new BlockPos(x0, y0 + DAIS_HEIGHT + 1, z0 + ALTAR_Z);
        level.setBlock(altar, CreatorBlocks.CREATOR_ALTAR.get().defaultBlockState().setValue(CreatorAltarBlock.FACING, Direction.SOUTH)
                .setValue(CreatorAltarBlock.PHASE, CreatorAltarBlock.Phase.ARMED), QUIET);
        return altar;
    }

    // =====================================================================================
    // Brücken und Welten
    // =====================================================================================

    /** Eine Brücke aus Quarz mit goldener Mittellinie und Lichtern an den Rändern, vom Kreis zu einer der vier Welten. */
    private static void bridge(ServerLevel level, BlockPos floor, double angle) {
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        double ux = Math.cos(angle);
        double uz = Math.sin(angle);
        double vx = -uz;
        double vz = ux;
        for (double s = RADIUS - 2.0; s <= ISLAND_DISTANCE - ISLAND_RADIUS + 2.0; s += 0.5) {
            for (double w = -2.6; w <= 2.6; w += 0.5) {
                int x = floor.getX() + (int) Math.round(ux * s + vx * w);
                int z = floor.getZ() + (int) Math.round(uz * s + vz * w);
                double aw = Math.abs(w);
                BlockState state = aw < 0.6 ? Blocks.GOLD_BLOCK.defaultBlockState() : aw > 2.0 ? Blocks.QUARTZ_BRICKS.defaultBlockState()
                        : Blocks.SMOOTH_QUARTZ.defaultBlockState();
                set(level, p, x, floor.getY(), z, state);
                set(level, p, x, floor.getY() - 1, z, aw > 2.0 ? Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState() : Blocks.QUARTZ_BLOCK.defaultBlockState());
            }
            int step = (int) Math.round(s * 2.0);
            if (step % 12 == 0) {
                for (int side : new int[] {-1, 1}) {
                    int x = floor.getX() + (int) Math.round(ux * s + vx * 2.6 * side);
                    int z = floor.getZ() + (int) Math.round(uz * s + vz * 2.6 * side);
                    set(level, p, x, floor.getY() + 1, z, Blocks.END_ROD.defaultBlockState().setValue(EndRodBlock.FACING, Direction.UP));
                    set(level, p, x, floor.getY() - 2, z, Blocks.END_ROD.defaultBlockState().setValue(EndRodBlock.FACING, Direction.DOWN));
                }
            }
        }
    }

    /** Eine der vier Welten: ein Garten, eine Glutwelt, eine Eiswelt oder ein Stück End – mit einem Weltenkern in der Mitte. */
    private static void island(ServerLevel level, BlockPos c, int kind) {
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        BlockState top;
        BlockState sub;
        BlockState rock;
        switch (kind) {
            case 0 -> {
                top = Blocks.GRASS_BLOCK.defaultBlockState();
                sub = Blocks.DIRT.defaultBlockState();
                rock = Blocks.STONE.defaultBlockState();
            }
            case 1 -> {
                top = Blocks.CRIMSON_NYLIUM.defaultBlockState();
                sub = Blocks.NETHERRACK.defaultBlockState();
                rock = Blocks.BLACKSTONE.defaultBlockState();
            }
            case 2 -> {
                top = Blocks.SNOW_BLOCK.defaultBlockState();
                sub = Blocks.PACKED_ICE.defaultBlockState();
                rock = Blocks.PACKED_ICE.defaultBlockState();
            }
            default -> {
                top = Blocks.END_STONE.defaultBlockState();
                sub = Blocks.END_STONE.defaultBlockState();
                rock = Blocks.END_STONE.defaultBlockState();
            }
        }
        int y0 = c.getY();
        for (int dx = -ISLAND_RADIUS; dx <= ISLAND_RADIUS; dx++) {
            for (int dz = -ISLAND_RADIUS; dz <= ISLAND_RADIUS; dz++) {
                double r = Math.sqrt(dx * dx + dz * dz);
                double edge = ISLAND_RADIUS - 1.0 + ArenaIsland.angleNoise(Math.atan2(dz, dx), 5.0, 17 + kind) * 2.0;
                if (r > edge) {
                    continue;
                }
                int x = c.getX() + dx;
                int z = c.getZ() + dz;
                BlockState surface = top;
                if (kind == 1 && hash(x, z, 21) > 0.7) {
                    surface = Blocks.NETHERRACK.defaultBlockState();
                } else if (kind == 3 && hash(x, z, 22) > 0.8) {
                    surface = Blocks.END_STONE_BRICKS.defaultBlockState();
                }
                set(level, p, x, y0, z, surface);
                int depth = (int) (3 + (edge - r) * 1.5 + hash(x, z, 23) * 2.0);
                for (int d = 1; d <= depth; d++) {
                    set(level, p, x, y0 - d, z, d <= 2 ? sub : rock);
                }
                double h = hash(x, z, 24);
                switch (kind) {
                    case 0 -> {
                        if (r > 3.0 && h > 0.86) {
                            BlockState flower = switch ((int) (hash(x, z, 25) * 6)) {
                                case 0 -> Blocks.POPPY.defaultBlockState();
                                case 1 -> Blocks.DANDELION.defaultBlockState();
                                case 2 -> Blocks.CORNFLOWER.defaultBlockState();
                                case 3 -> Blocks.ALLIUM.defaultBlockState();
                                case 4 -> Blocks.AZURE_BLUET.defaultBlockState();
                                default -> Blocks.OXEYE_DAISY.defaultBlockState();
                            };
                            set(level, p, x, y0 + 1, z, flower);
                        } else if (r > 3.0 && h > 0.6) {
                            set(level, p, x, y0 + 1, z, Blocks.SHORT_GRASS.defaultBlockState());
                        }
                    }
                    case 1 -> {
                        if (r > 3.0 && h > 0.93) {
                            set(level, p, x, y0 + 1, z, Blocks.CRIMSON_FUNGUS.defaultBlockState());
                        } else if (r > 3.0 && h > 0.86) {
                            set(level, p, x, y0 + 1, z, Blocks.CRIMSON_ROOTS.defaultBlockState());
                        }
                    }
                    case 2 -> {
                        if (r > 3.0 && h > 0.5) {
                            set(level, p, x, y0 + 1, z, Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, 1 + (int) (hash(x, z, 26) * 2)));
                        }
                    }
                    default -> {
                    }
                }
            }
        }
        // je Welt ihre Wahrzeichen
        switch (kind) {
            case 0 -> {
                tree(level, c.offset(-6, 1, 4), 6);
                tree(level, c.offset(5, 1, -6), 5);
                tree(level, c.offset(7, 1, 6), 4);
            }
            case 1 -> {
                for (int k = 0; k < 5; k++) {
                    double a = k * 1.3 + 0.4;
                    int bx = c.getX() + (int) Math.round(Math.cos(a) * (7 + k % 3));
                    int bz = c.getZ() + (int) Math.round(Math.sin(a) * (7 + k % 3));
                    int h = 3 + k % 4;
                    for (int dy = 1; dy <= h; dy++) {
                        set(level, p, bx, y0 + dy, bz, Blocks.BASALT.defaultBlockState());
                    }
                    set(level, p, bx, y0 + h + 1, bz, Blocks.SHROOMLIGHT.defaultBlockState());
                }
                fungusTree(level, c.offset(-5, 1, -5));
                fungusTree(level, c.offset(6, 1, 4));
            }
            case 2 -> {
                for (int k = 0; k < 7; k++) {
                    double a = k * 0.9 + 0.2;
                    int bx = c.getX() + (int) Math.round(Math.cos(a) * (5 + k % 4 * 2));
                    int bz = c.getZ() + (int) Math.round(Math.sin(a) * (5 + k % 4 * 2));
                    int h = 4 + (k * 5) % 7;
                    for (int dy = 1; dy <= h; dy++) {
                        int w = dy < h / 3 ? 1 : 0;
                        for (int dx = -w; dx <= w; dx++) {
                            for (int dz = -w; dz <= w; dz++) {
                                set(level, p, bx + dx, y0 + dy, bz + dz, dy > h - 2 ? Blocks.BLUE_ICE.defaultBlockState()
                                        : Blocks.PACKED_ICE.defaultBlockState());
                            }
                        }
                    }
                }
            }
            default -> {
                for (int k = 0; k < 4; k++) {
                    double a = k * Math.PI / 2.0 + 0.6;
                    int bx = c.getX() + (int) Math.round(Math.cos(a) * 8);
                    int bz = c.getZ() + (int) Math.round(Math.sin(a) * 8);
                    for (int dy = 1; dy <= 6; dy++) {
                        set(level, p, bx, y0 + dy, bz, Blocks.PURPUR_PILLAR.defaultBlockState());
                    }
                    set(level, p, bx, y0 + 7, bz, Blocks.END_ROD.defaultBlockState().setValue(EndRodBlock.FACING, Direction.UP));
                }
                chorus(level, c.offset(-5, 1, 5));
                chorus(level, c.offset(6, 1, -4));
                chorus(level, c.offset(-6, 1, -6));
            }
        }
        // der Weltenkern: ein goldener Sockel mit einem Licht darauf
        set(level, p, c.getX(), y0 + 1, c.getZ(), Blocks.GOLD_BLOCK.defaultBlockState());
        set(level, p, c.getX(), y0 + 2, c.getZ(), Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState());
        set(level, p, c.getX(), y0 + 3, c.getZ(), switch (kind) {
            case 0 -> Blocks.VERDANT_FROGLIGHT.defaultBlockState();
            case 1 -> Blocks.OCHRE_FROGLIGHT.defaultBlockState();
            case 2 -> Blocks.SEA_LANTERN.defaultBlockState();
            default -> Blocks.PEARLESCENT_FROGLIGHT.defaultBlockState();
        });
    }

    private static void tree(ServerLevel level, BlockPos base, int height) {
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        BlockState leaves = Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
        for (int dy = 0; dy < height; dy++) {
            set(level, p, base.getX(), base.getY() + dy, base.getZ(), Blocks.OAK_LOG.defaultBlockState());
        }
        int top = base.getY() + height;
        for (int dy = -2; dy <= 1; dy++) {
            int r = dy <= -1 ? 2 : 1;
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (Math.abs(dx) == r && Math.abs(dz) == r && (dy == 1 || hash(dx + base.getX(), dz + dy * 7, 31) > 0.5)) {
                        continue;
                    }
                    if (dx == 0 && dz == 0 && dy < 0) {
                        continue;
                    }
                    set(level, p, base.getX() + dx, top + dy, base.getZ() + dz, leaves);
                }
            }
        }
    }

    private static void fungusTree(ServerLevel level, BlockPos base) {
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        for (int dy = 0; dy < 5; dy++) {
            set(level, p, base.getX(), base.getY() + dy, base.getZ(), Blocks.CRIMSON_STEM.defaultBlockState());
        }
        int top = base.getY() + 5;
        for (int dy = -1; dy <= 0; dy++) {
            int r = dy == -1 ? 2 : 1;
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (dy == -1 && Math.abs(dx) < 2 && Math.abs(dz) < 2) {
                        continue;
                    }
                    set(level, p, base.getX() + dx, top + dy, base.getZ() + dz, hash(dx, dz + dy, 33) > 0.85 ? Blocks.SHROOMLIGHT.defaultBlockState()
                            : Blocks.NETHER_WART_BLOCK.defaultBlockState());
                }
            }
        }
    }

    private static void chorus(ServerLevel level, BlockPos base) {
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        BlockState plant = Blocks.CHORUS_PLANT.defaultBlockState();
        for (int dy = 0; dy < 4; dy++) {
            BlockState s = plant.setValue(ChorusPlantBlock.DOWN, true).setValue(ChorusPlantBlock.UP, dy < 3);
            if (dy == 2) {
                s = s.setValue(ChorusPlantBlock.EAST, true).setValue(ChorusPlantBlock.WEST, true);
            }
            set(level, p, base.getX(), base.getY() + dy, base.getZ(), s);
        }
        set(level, p, base.getX(), base.getY() + 4, base.getZ(), Blocks.CHORUS_FLOWER.defaultBlockState());
        for (int side : new int[] {-1, 1}) {
            BlockState arm = plant.setValue(side < 0 ? ChorusPlantBlock.EAST : ChorusPlantBlock.WEST, true).setValue(ChorusPlantBlock.UP, true);
            set(level, p, base.getX() + side, base.getY() + 2, base.getZ(), arm);
            set(level, p, base.getX() + side, base.getY() + 3, base.getZ(), Blocks.CHORUS_FLOWER.defaultBlockState());
        }
    }

    // =====================================================================================
    // Die Räder des Himmels und die schwebenden Felsen
    // =====================================================================================

    /** Drei Ringe aus Gold hoch über dem Hof, schräg gegeneinander geneigt (mit Lichtern darin). */
    private static void rings(ServerLevel level, BlockPos floor) {
        ring(level, floor.above(46), 64.0, 0.0, 0.0);
        ring(level, floor.above(40), 72.0, 22.0, 0.0);
        ring(level, floor.above(52), 56.0, 0.0, -28.0);
    }

    private static void ring(ServerLevel level, BlockPos c, double radius, double tiltX, double tiltZ) {
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        double ax = Math.toRadians(tiltX);
        double az = Math.toRadians(tiltZ);
        int steps = (int) (radius * 2.0 * Math.PI * 2.2);
        for (int i = 0; i < steps; i++) {
            double t = i * 2.0 * Math.PI / steps;
            double x = Math.cos(t) * radius;
            double y = 0.0;
            double z = Math.sin(t) * radius;
            // um X neigen …
            double y1 = y * Math.cos(ax) - z * Math.sin(ax);
            double z1 = y * Math.sin(ax) + z * Math.cos(ax);
            // … und um Z
            double x2 = x * Math.cos(az) - y1 * Math.sin(az);
            double y2 = x * Math.sin(az) + y1 * Math.cos(az);
            int bx = c.getX() + (int) Math.round(x2);
            int by = c.getY() + (int) Math.round(y2);
            int bz = c.getZ() + (int) Math.round(z1);
            set(level, p, bx, by, bz, i % 37 == 0 ? Blocks.GLOWSTONE.defaultBlockState() : Blocks.GOLD_BLOCK.defaultBlockState());
        }
    }

    /** Schwebende Felsen aus Kalzit mit Goldadern ringsum. */
    private static void rocks(ServerLevel level, BlockPos floor) {
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        for (int k = 0; k < 22; k++) {
            double a = k * 2.399963 + 0.3;
            double dist = 52.0 + hash(k, 1, 41) * 66.0;
            int cx = floor.getX() + (int) Math.round(Math.cos(a) * dist);
            int cz = floor.getZ() + (int) Math.round(Math.sin(a) * dist);
            int cy = floor.getY() - 22 + (int) (hash(k, 2, 42) * 56.0);
            // nicht in die Welten und Brücken
            double angle = (Math.toDegrees(a) % 360.0 + 360.0) % 360.0;
            double toBridge = Math.abs(((angle - 45.0) % 90.0 + 90.0) % 90.0 - 45.0);
            if (toBridge > 32.0 && cy > floor.getY() - 12 && cy < floor.getY() + 14) {
                cy += 18;
            }
            double rad = 1.6 + hash(k, 3, 43) * 2.6;
            int r = (int) Math.ceil(rad);
            for (int dx = -r; dx <= r; dx++) {
                for (int dy = -r; dy <= r; dy++) {
                    for (int dz = -r; dz <= r; dz++) {
                        double d = dx * dx / (rad * rad) + dy * dy / (rad * rad * 0.55) + dz * dz / (rad * rad);
                        if (d > 1.0) {
                            continue;
                        }
                        double h = hash(cx + dx, cz + dz + dy * 17, 44);
                        BlockState s = h > 0.86 ? Blocks.RAW_GOLD_BLOCK.defaultBlockState() : dy >= r - 1 && h > 0.5 ? Blocks.SMOOTH_QUARTZ.defaultBlockState()
                                : Blocks.CALCITE.defaultBlockState();
                        set(level, p, cx + dx, cy + dy, cz + dz, s);
                    }
                }
            }
            if (k % 3 == 0) {
                set(level, p, cx, cy - (int) Math.ceil(rad * 0.75) - 1, cz, Blocks.END_ROD.defaultBlockState().setValue(EndRodBlock.FACING, Direction.DOWN));
            }
        }
    }
}
