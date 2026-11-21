package io.github.sirschrolli.kingdomsatwar.arena;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.creator.CreatorAltarBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EndRodBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * Die drei Bühnen der Schöpfung (seit 0.78): Mit jeder Gestalt Schrollis wechselt sein Hof – in einem weißen Licht, in dem
 * alles neu entsteht (siehe {@code SchrolliEntity}):
 * <ol>
 *     <li><b>der Hof</b> ({@link CreatorArena}): Quarz und Gold, der Thron, zwölf Säulen, Brücken zu vier Welten;</li>
 *     <li><b>das Firmament</b> (die Wahre Gestalt): der Himmel selbst wird zum Boden – eine Scheibe aus Nacht mit einem
 *     Polarstern, dem Himmelsäquator aus Amethyst, zwölf Sternbildern und dem Band der zwölf Zeichen; zwölf Obelisken aus
 *     Obsidian (Schutz vor der Supernova, wo im Hof die Säulen standen), hinter dem Altar eine Armillarsphäre, ringsum
 *     schweben fünf Welten. Die Brücken sind fort;</li>
 *     <li><b>das Herz der Sonne</b> (der Allmächtige): eine Scheibe aus Sonnenglut – ein weißer Kern, Körnung aus Gold,
 *     sechzehn Strahlen, Sonnenflecken, ein Kranz aus Flammen –, acht Sonnenpylonen, vier Bögen aus Glut am Rand, hinter
 *     dem Altar eine gewaltige Sonnenscheibe und hoch darüber eine zweite Sonne.</li>
 * </ol>
 * Der Altar (das Abbild vor dem Thron, der den Kampf hält) bleibt bei jedem Wechsel stehen – auf einem Sockel der neuen
 * Bühne. Die Räder des Himmels, die schwebenden Felsen und die vier Welten bleiben. Der Boden liegt immer gleich hoch, so
 * finden Herausforderer und Gestürzte ihre Plätze wie im Hof.
 */
public final class CreatorStages {
    public enum Stage {
        COURT, FIRMAMENT, SUN_HEART;

        public static Stage of(int ordinal) {
            Stage[] all = values();
            return all[Mth.clamp(ordinal, 0, all.length - 1)];
        }
    }

    /** So weit um die Mitte wird beim Wechsel geräumt (seitlich, nach unten, nach oben). */
    public static final int CLEAR_RADIUS = 62;
    static final int CLEAR_BELOW = 40;
    static final int CLEAR_ABOVE = 46;
    private static final int RADIUS = CreatorArena.RADIUS;
    private static final int QUIET = ArenaIsland.QUIET;

    private CreatorStages() {}

    /**
     * Verwandelt den Hof um {@code center} (ein Block über dem Boden) in die Bühne {@code stage}; der Block bei
     * {@code altar} (das Abbild des Altars) bleibt stehen und merkt sich die Bühne.
     */
    public static void transform(ServerLevel level, BlockPos center, Stage stage, @Nullable BlockPos altar) {
        long started = System.nanoTime();
        BlockPos floor = center.below();
        clear(level, center, altar);
        switch (stage) {
            case COURT -> CreatorArena.build(level, center);
            case FIRMAMENT -> firmament(level, floor, altar);
            case SUN_HEART -> sunHeart(level, floor, altar);
        }
        if (stage != Stage.COURT) {
            CreatorArena.scenery(level, floor);
        }
        if (altar != null && level.getBlockEntity(altar) instanceof CreatorAltarBlockEntity be) {
            be.setArenaStage(stage.ordinal());
        }
        KingdomsAtWar.LOGGER.info("Schöpfung: Bühne {} bei {} gebaut ({} ms)", stage, center, (System.nanoTime() - started) / 1_000_000L);
    }

    /** Räumt die Bühne (der Altar bleibt) samt den Brücken zu den vier Welten. */
    public static void clear(ServerLevel level, BlockPos center, @Nullable BlockPos altar) {
        ArenaIsland.clear(level, center, CLEAR_RADIUS, CLEAR_BELOW, CLEAR_ABOVE, altar);
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        int y0 = center.getY() - 1;
        for (int k = 0; k < 4; k++) {
            double a = Math.toRadians(45.0 + k * 90.0);
            double ux = Math.cos(a);
            double uz = Math.sin(a);
            for (double s = CLEAR_RADIUS - 2.0; s <= CreatorArena.ISLAND_DISTANCE - CreatorArena.ISLAND_RADIUS - 1.0; s += 0.5) {
                for (double w = -3.0; w <= 3.0; w += 0.5) {
                    int x = center.getX() + (int) Math.round(ux * s - uz * w);
                    int z = center.getZ() + (int) Math.round(uz * s + ux * w);
                    for (int y = y0 - 2; y <= y0 + 1; y++) {
                        if (!level.getBlockState(p.set(x, y, z)).isAir()) {
                            level.setBlock(p, Blocks.AIR.defaultBlockState(), QUIET);
                        }
                    }
                }
            }
        }
    }

    private static void set(ServerLevel level, BlockPos.MutableBlockPos p, int x, int y, int z, BlockState state, @Nullable BlockPos keep) {
        if (keep != null && keep.getX() == x && keep.getY() == y && keep.getZ() == z) {
            return;
        }
        level.setBlock(p.set(x, y, z), state, QUIET);
    }

    private static double hash(int x, int z, int salt) {
        return ArenaIsland.rand(x, z, salt);
    }

    private static BlockState rod(Direction facing) {
        return Blocks.END_ROD.defaultBlockState().setValue(EndRodBlock.FACING, facing);
    }

    /** Was auf einer Scheibe an welcher Stelle liegt: (Abstand, Winkel in Grad, dx, dz) → Block. */
    @FunctionalInterface
    private interface Surface {
        BlockState at(double r, double deg, int dx, int dz);
    }

    /** Was die Unterseite bildet: (Tiefe, Ader 0–1, x, z) → Block. */
    @FunctionalInterface
    private interface Depth {
        BlockState at(int d, double vein, int x, int z);
    }

    /**
     * Eine Scheibe (Halbmesser {@link CreatorArena#RADIUS}, leicht unruhiger Rand) mit ihrer Oberfläche, zwei Lagen darunter
     * und einem hohlen Kegel als Unterseite (wie im Hof), der in einem Kern aus Licht endet.
     */
    private static void disc(ServerLevel level, BlockPos floor, Surface top, BlockState under, Depth depth, BlockState core, BlockState coreAlt) {
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        int y0 = floor.getY();
        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                double r = Math.sqrt(dx * dx + dz * dz);
                double edge = RADIUS + (hash(dx, dz, 3) - 0.5) * 0.6;
                if (r > edge) {
                    continue;
                }
                int x = floor.getX() + dx;
                int z = floor.getZ() + dz;
                double deg = (Math.toDegrees(Math.atan2(dz, dx)) + 360.0) % 360.0;
                level.setBlock(p.set(x, y0, z), top.at(r, deg, dx, dz), QUIET);
                level.setBlock(p.set(x, y0 - 1, z), under, QUIET);
                level.setBlock(p.set(x, y0 - 2, z), under, QUIET);
                int cone = 32;
                for (int d = 3; d <= cone; d++) {
                    double k = d / (double) cone;
                    double radius = RADIUS * (1.0 - Math.pow(k, 1.25)) + (ArenaIsland.noise(dx * 0.7, d * 1.3, 0.25, 5) - 0.5) * 2.4;
                    if (r > radius) {
                        break;
                    }
                    if (r < radius - 2.2 && d < cone - 2) {
                        continue;
                    }
                    double vein = ArenaIsland.noise(dx + d * 0.6, dz - d * 0.4, 0.18, 9);
                    level.setBlock(p.set(x, y0 - d, z), depth.at(d, vein, x, z), QUIET);
                }
            }
        }
        int tip = y0 - 34;
        for (int dy = 0; dy < 5; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (Math.abs(dx) + Math.abs(dz) <= (dy < 2 ? 2 : dy < 4 ? 1 : 0)) {
                        level.setBlock(p.set(floor.getX() + dx, tip - dy, floor.getZ() + dz), dy % 2 == 0 ? core : coreAlt, QUIET);
                    }
                }
            }
        }
    }

    /** Ein runder Sockel in Stufen unter dem Altar (der Altar selbst bleibt): drei Lagen, oben mit einem leuchtenden Ring. */
    private static void dais(ServerLevel level, BlockPos floor, @Nullable BlockPos altar, BlockState step, BlockState rim, BlockState top,
            BlockState ring) {
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        int cx = floor.getX();
        int cz = floor.getZ() + CreatorArena.ALTAR_Z;
        double[] radii = {7.5, 5.6, 3.6};
        for (int layer = 0; layer < CreatorArena.DAIS_HEIGHT; layer++) {
            double rr = radii[layer];
            int n = (int) Math.ceil(rr);
            for (int dx = -n; dx <= n; dx++) {
                for (int dz = -n; dz <= n; dz++) {
                    double r = Math.sqrt(dx * dx + dz * dz);
                    if (r > rr) {
                        continue;
                    }
                    BlockState s = r > rr - 1.0 ? rim : layer == CreatorArena.DAIS_HEIGHT - 1 ? (Math.abs(r - 2.2) < 0.6 ? ring : top) : step;
                    set(level, p, cx + dx, floor.getY() + 1 + layer, cz + dz, s, altar);
                }
            }
        }
    }

    /** Eine Kugel (Halbmesser {@code radius}) – jeder Block nach seiner Lage gewählt: (Breite −1…1, Rauschen, ob Außenhaut). */
    @FunctionalInterface
    private interface Globe {
        BlockState at(double latitude, double noise, boolean shell);
    }

    private static void sphere(ServerLevel level, double cx, double cy, double cz, double radius, int salt, Globe paint) {
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        int n = (int) Math.ceil(radius);
        for (int dx = -n; dx <= n; dx++) {
            for (int dy = -n; dy <= n; dy++) {
                for (int dz = -n; dz <= n; dz++) {
                    double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
                    if (d > radius) {
                        continue;
                    }
                    int x = (int) Math.floor(cx) + dx;
                    int y = (int) Math.floor(cy) + dy;
                    int z = (int) Math.floor(cz) + dz;
                    double noise = ArenaIsland.noise(x + dy * 0.7, z - dy * 0.5, 0.45, salt);
                    level.setBlock(p.set(x, y, z), paint.at(dy / radius, noise, d > radius - 1.2), QUIET);
                }
            }
        }
    }

    // =====================================================================================
    // Das Firmament
    // =====================================================================================

    /** Die zwölf Zeichen, je 5 × 5 Felder (die erste Zeile liegt außen). */
    private static final String[][] SIGNS = {
            {"X...X", ".X.X.", "..X..", "..X..", "..X.."},            // Widder
            {"X...X", ".XXX.", "X...X", "X...X", ".XXX."},            // Stier
            {"XXXXX", ".X.X.", ".X.X.", ".X.X.", "XXXXX"},            // Zwillinge
            {".XXX.", "X....", ".X.X.", "....X", ".XXX."},            // Krebs
            {".XX..", "X..X.", "X..X.", ".X..X", "..XX."},            // Löwe
            {"XX.X.", "XXXXX", "X.X.X", "X.X.X", "X.X.X"},            // Jungfrau
            {".XXX.", "X...X", "XX.XX", ".....", "XXXXX"},            // Waage
            {"X.X..", "XXXX.", "X.X..", "X.X.X", "X.XX."},            // Skorpion
            {"..XXX", "...XX", "..X.X", ".X...", "X...."},            // Schütze
            {"X.X..", "XX.X.", "X..X.", "...XX", "..XX."},            // Steinbock
            {".X.X.", "X.X.X", ".....", ".X.X.", "X.X.X"},            // Wassermann
            {"X...X", ".X.X.", "XXXXX", ".X.X.", "X...X"},            // Fische
    };

    private static boolean sign(int k, int gx, int gy) {
        return SIGNS[k % SIGNS.length][gy].charAt(gx) == 'X';
    }

    private static void firmament(ServerLevel level, BlockPos floor, @Nullable BlockPos altar) {
        BlockState black = Blocks.CONCRETE.pick(DyeColor.BLACK).defaultBlockState();
        BlockState obsidian = Blocks.OBSIDIAN.defaultBlockState();
        BlockState crying = Blocks.CRYING_OBSIDIAN.defaultBlockState();
        BlockState blue = Blocks.CONCRETE.pick(DyeColor.BLUE).defaultBlockState();
        BlockState lapis = Blocks.LAPIS_BLOCK.defaultBlockState();
        BlockState purple = Blocks.CONCRETE.pick(DyeColor.PURPLE).defaultBlockState();
        BlockState white = Blocks.CONCRETE.pick(DyeColor.WHITE).defaultBlockState();
        BlockState lantern = Blocks.SEA_LANTERN.defaultBlockState();
        BlockState glow = Blocks.GLOWSTONE.defaultBlockState();
        BlockState gold = Blocks.GOLD_BLOCK.defaultBlockState();
        BlockState amethyst = Blocks.AMETHYST_BLOCK.defaultBlockState();
        BlockState purpur = Blocks.PURPUR_BLOCK.defaultBlockState();
        BlockState endBricks = Blocks.END_STONE_BRICKS.defaultBlockState();
        disc(level, floor, (r, deg, dx, dz) -> {
            double h = hash(dx, dz, 61);
            if (r <= 1.6) {
                return lantern;                                                         // der Polarstern
            }
            if (r <= 5.2) {
                double ray = Math.abs(((deg + 22.5) % 45.0) - 22.5);
                double width = 22.5 * (1.0 - r / 5.6);
                return ray < width ? (r < 3.2 ? lantern : white) : black;
            }
            if (r < 6.3) {
                return gold;
            }
            if (r < 14.0) {
                // der innere Himmel: von Blau zu Nacht, mit Nebeln und Sternen
                if (h > 0.975) {
                    return h > 0.992 ? glow : lantern;
                }
                double neb = ArenaIsland.noise(dx, dz, 0.22, 63);
                if (neb > 0.7) {
                    return lapis;
                }
                return r < 9.0 + (neb - 0.5) * 6.0 ? blue : neb < 0.3 ? purple : black;
            }
            if (r < 15.3) {
                return amethyst;                                                        // der Himmelsäquator
            }
            if (r < 27.4) {
                if (h > 0.986) {
                    return crying;
                }
                return ArenaIsland.noise(dx, dz, 0.3, 64) > 0.55 ? obsidian : black;
            }
            if (r < 28.6) {
                return gold;
            }
            if (r < 38.2) {
                // das Band der zwölf Zeichen: Felder aus Purpur und Nacht, getrennt von Goldlinien
                double seam = Math.abs(((deg + 15.0) % 30.0) - 15.0);
                int field = (int) (((deg + 15.0) % 360.0) / 30.0);
                if (seam > 15.0 - 31.5 / r) {
                    return gold;
                }
                if (h > 0.97) {
                    return lantern;
                }
                return field % 2 == 0 ? purpur : purple;
            }
            if (r < 40.2) {
                return ((int) (deg / 7.5)) % 2 == 0 ? endBricks : obsidian;
            }
            return gold;
        }, obsidian, (d, vein, x, z) -> {
            if (vein > 0.72) {
                return amethyst;
            }
            if (vein > 0.69) {
                return crying;
            }
            return d % 7 == 0 ? Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState() : hash(x, z + d * 31, 13) > 0.3 ? obsidian
                    : Blocks.BLACKSTONE.defaultBlockState();
        }, lantern, glow);
        constellations(level, floor);
        signs(level, floor, lantern);
        for (int k = 0; k < 12; k++) {
            double a = Math.toRadians(15.0 + k * 30.0);
            obelisk(level, floor.offset((int) Math.round(Math.cos(a) * CreatorArena.PILLAR_RING), 0,
                    (int) Math.round(Math.sin(a) * CreatorArena.PILLAR_RING)), altar);
        }
        dais(level, floor, altar, purpur, endBricks, Blocks.PURPUR_PILLAR.defaultBlockState(), lantern);
        armillary(level, floor, altar);
        planets(level, floor);
        // Sternenlicht tropft unter dem Rand hinab
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        for (int k = 0; k < 36; k++) {
            double a = Math.toRadians(k * 10.0 + 5.0);
            int x = floor.getX() + (int) Math.round(Math.cos(a) * (RADIUS - 3));
            int z = floor.getZ() + (int) Math.round(Math.sin(a) * (RADIUS - 3));
            level.setBlock(p.set(x, floor.getY() - 3 - (k % 3), z), rod(Direction.DOWN), QUIET);
        }
    }

    /** Zwölf Sternbilder zwischen Himmelsäquator und Goldring: Sterne aus Seelaternen, verbunden durch weiße Linien. */
    private static void constellations(ServerLevel level, BlockPos floor) {
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        BlockState line = Blocks.CONCRETE.pick(DyeColor.WHITE).defaultBlockState();
        BlockState star = Blocks.SEA_LANTERN.defaultBlockState();
        int y = floor.getY();
        for (int k = 0; k < 12; k++) {
            int count = 4 + (int) (hash(k, 7, 71) * 3.0);
            double[][] stars = new double[count][2];
            for (int i = 0; i < count; i++) {
                double deg = 30.0 * k + 3.0 + 24.0 * (i + hash(k, i, 72) * 0.6) / count;
                double r = 17.0 + hash(k, i, 73) * 9.4;
                stars[i][0] = Math.cos(Math.toRadians(deg)) * r;
                stars[i][1] = Math.sin(Math.toRadians(deg)) * r;
            }
            for (int i = 0; i + 1 < count; i++) {
                // ein Ast zurück zum ersten Stern bei manchen Bildern
                int to = i == count - 2 && hash(k, 9, 74) > 0.5 ? 0 : i + 1;
                double[] a = stars[i];
                double[] b = stars[to];
                double length = Math.hypot(b[0] - a[0], b[1] - a[1]);
                for (double t = 0.0; t <= 1.0; t += 0.35 / Math.max(length, 0.1)) {
                    int x = floor.getX() + (int) Math.round(Mth.lerp(t, a[0], b[0]));
                    int z = floor.getZ() + (int) Math.round(Mth.lerp(t, a[1], b[1]));
                    level.setBlock(p.set(x, y, z), line, QUIET);
                }
            }
            for (double[] s : stars) {
                int x = floor.getX() + (int) Math.round(s[0]);
                int z = floor.getZ() + (int) Math.round(s[1]);
                level.setBlock(p.set(x, y, z), star, QUIET);
                for (Direction d : Direction.Plane.HORIZONTAL) {
                    level.setBlock(p.set(x + d.getStepX(), y, z + d.getStepZ()), line, QUIET);
                }
            }
        }
    }

    /** Die zwölf Zeichen in ihren Feldern (aus Licht, nach außen gewandt). */
    private static void signs(ServerLevel level, BlockPos floor, BlockState light) {
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        for (int k = 0; k < 12; k++) {
            double a = Math.toRadians(k * 30.0 + 15.0);
            double ux = Math.cos(a);
            double uz = Math.sin(a);
            double cr = 31.0;
            for (int gy = 0; gy < 5; gy++) {
                for (int gx = 0; gx < 5; gx++) {
                    if (!sign(k, gx, gy)) {
                        continue;
                    }
                    double along = 2.0 - gy;           // oben im Zeichen = außen
                    double side = gx - 2.0;
                    int x = floor.getX() + (int) Math.round(ux * (cr + along) - uz * side);
                    int z = floor.getZ() + (int) Math.round(uz * (cr + along) + ux * side);
                    level.setBlock(p.set(x, floor.getY(), z), light, QUIET);
                }
            }
        }
    }

    /** Ein Obelisk aus Obsidian: Sockel, Schaft mit Bändern aus Amethyst, eine Spitze aus Amethyst und Licht, darüber ein Stern. */
    private static void obelisk(ServerLevel level, BlockPos base, @Nullable BlockPos altar) {
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        int x = base.getX();
        int y = base.getY();
        int z = base.getZ();
        int height = 14;
        BlockState bricks = Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (Math.abs(dx) + Math.abs(dz) <= 3) {
                    set(level, p, x + dx, y + 1, z + dz, bricks, altar);
                }
            }
        }
        for (int dy = 2; dy <= height; dy++) {
            BlockState s = dy % 4 == 0 ? Blocks.AMETHYST_BLOCK.defaultBlockState() : dy == 2 ? Blocks.POLISHED_BLACKSTONE.defaultBlockState()
                    : Blocks.OBSIDIAN.defaultBlockState();
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    boolean corner = Math.abs(dx) == 1 && Math.abs(dz) == 1;
                    set(level, p, x + dx, y + dy, z + dz, corner && dy % 4 != 0 ? Blocks.CRYING_OBSIDIAN.defaultBlockState() : s, altar);
                }
            }
        }
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                set(level, p, x + dx, y + height + 1, z + dz, Blocks.AMETHYST_BLOCK.defaultBlockState(), altar);
                if (Math.abs(dx) + Math.abs(dz) <= 1) {
                    set(level, p, x + dx, y + height + 2, z + dz, Blocks.AMETHYST_BLOCK.defaultBlockState(), altar);
                }
            }
        }
        set(level, p, x, y + height + 3, z, Blocks.SEA_LANTERN.defaultBlockState(), altar);
        set(level, p, x, y + height + 4, z, rod(Direction.UP), altar);
        for (Direction d : Direction.Plane.HORIZONTAL) {
            set(level, p, x + d.getStepX(), y + height + 3, z + d.getStepZ(),
                    Blocks.AMETHYST_CLUSTER.defaultBlockState().setValue(AmethystClusterBlock.FACING, d), altar);
        }
        set(level, p, x, y + height + 8, z, Blocks.SEA_LANTERN.defaultBlockState(), altar);
    }

    /** Hinter dem Altar: eine Armillarsphäre aus Gold auf einer Säule – drei Ringe um einen Kern aus Licht. */
    private static void armillary(ServerLevel level, BlockPos floor, @Nullable BlockPos altar) {
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        int cx = floor.getX();
        int cz = floor.getZ() + CreatorArena.THRONE_Z - 2;
        int cy = floor.getY() + 15;
        BlockState pillar = Blocks.PURPUR_PILLAR.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (Math.abs(dx) + Math.abs(dz) <= 3) {
                    set(level, p, cx + dx, floor.getY() + 1, cz + dz, Blocks.END_STONE_BRICKS.defaultBlockState(), altar);
                }
            }
        }
        for (int y = floor.getY() + 2; y <= cy - 8; y++) {
            set(level, p, cx, y, cz, pillar, altar);
        }
        double radius = 7.0;
        for (int i = 0; i < 160; i++) {
            double t = i * Math.PI * 2.0 / 160.0;
            double c = Math.cos(t) * radius;
            double s = Math.sin(t) * radius;
            // senkrecht zur Arena, senkrecht quer dazu, und schräg geneigt (die Ekliptik)
            set(level, p, cx + (int) Math.round(c), cy + (int) Math.round(s), cz, Blocks.GOLD_BLOCK.defaultBlockState(), altar);
            set(level, p, cx, cy + (int) Math.round(s), cz + (int) Math.round(c), Blocks.GOLD_BLOCK.defaultBlockState(), altar);
            double tilt = Math.toRadians(23.5);
            set(level, p, cx + (int) Math.round(c), cy + (int) Math.round(s * Math.sin(tilt)), cz + (int) Math.round(s * Math.cos(tilt)),
                    i % 20 == 0 ? Blocks.SEA_LANTERN.defaultBlockState() : Blocks.RAW_GOLD_BLOCK.defaultBlockState(), altar);
        }
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (Math.abs(dx) + Math.abs(dy) + Math.abs(dz) <= 1) {
                        set(level, p, cx + dx, cy + dy, cz + dz, Blocks.SEA_LANTERN.defaultBlockState(), altar);
                    }
                }
            }
        }
        set(level, p, cx, cy + 8, cz, rod(Direction.UP), altar);
        set(level, p, cx, cy - 8, cz, rod(Direction.DOWN), altar);
    }

    /** Fünf Welten schweben um das Firmament: eine Glutwelt, eine Meereswelt, eine Ringwelt, eine Eiswelt und ein Mond. */
    private static void planets(ServerLevel level, BlockPos floor) {
        double fx = floor.getX() + 0.5;
        double fz = floor.getZ() + 0.5;
        double y0 = floor.getY();
        // die Glutwelt
        double a = Math.toRadians(20.0);
        sphere(level, fx + Math.cos(a) * 53.0, y0 + 15.0, fz + Math.sin(a) * 53.0, 4.6, 81, (lat, n, shell) -> !shell
                ? Blocks.MAGMA_BLOCK.defaultBlockState() : n > 0.62 ? Blocks.MAGMA_BLOCK.defaultBlockState() : n > 0.42
                ? Blocks.NETHERRACK.defaultBlockState() : Blocks.RED_NETHER_BRICKS.defaultBlockState());
        // die Meereswelt mit Inseln
        a = Math.toRadians(112.0);
        sphere(level, fx + Math.cos(a) * 55.0, y0 + 21.0, fz + Math.sin(a) * 55.0, 5.2, 82, (lat, n, shell) -> n > 0.7 && shell
                ? Blocks.MOSS_BLOCK.defaultBlockState() : Math.abs(lat) > 0.82 && shell ? Blocks.SNOW_BLOCK.defaultBlockState()
                : n > 0.55 ? Blocks.PRISMARINE.defaultBlockState() : n > 0.25 ? Blocks.DARK_PRISMARINE.defaultBlockState()
                : Blocks.SEA_LANTERN.defaultBlockState());
        // die Ringwelt
        a = Math.toRadians(205.0);
        double rx = fx + Math.cos(a) * 54.0;
        double rz = fz + Math.sin(a) * 54.0;
        double ry = y0 + 23.0;
        sphere(level, rx, ry, rz, 4.2, 83, (lat, n, shell) -> {
            int band = (int) Math.floor((lat + 1.0) * 4.0 + (n - 0.5) * 1.2);
            return band % 3 == 0 ? Blocks.SMOOTH_SANDSTONE.defaultBlockState() : band % 3 == 1 ? Blocks.SANDSTONE.defaultBlockState()
                    : Blocks.CUT_RED_SANDSTONE.defaultBlockState();
        });
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        double tilt = Math.toRadians(22.0);
        for (int i = 0; i < 220; i++) {
            double t = i * Math.PI * 2.0 / 220.0;
            for (double rr = 6.2; rr <= 8.2; rr += 0.7) {
                double x = Math.cos(t) * rr;
                double z = Math.sin(t) * rr;
                level.setBlock(p.set((int) Math.floor(rx + x), (int) Math.floor(ry + z * Math.sin(tilt)), (int) Math.floor(rz + z * Math.cos(tilt))),
                        rr > 7.5 ? Blocks.SMOOTH_SANDSTONE.defaultBlockState() : Blocks.CUT_SANDSTONE.defaultBlockState(), QUIET);
            }
        }
        // die Eiswelt
        a = Math.toRadians(292.0);
        sphere(level, fx + Math.cos(a) * 50.0, y0 + 11.0, fz + Math.sin(a) * 50.0, 3.6, 84, (lat, n, shell) -> n > 0.6
                ? Blocks.BLUE_ICE.defaultBlockState() : n > 0.35 ? Blocks.PACKED_ICE.defaultBlockState() : Blocks.SNOW_BLOCK.defaultBlockState());
        // der Mond
        a = Math.toRadians(160.0);
        sphere(level, fx + Math.cos(a) * 47.5, y0 + 7.0, fz + Math.sin(a) * 47.5, 2.6, 85, (lat, n, shell) -> n > 0.66
                ? Blocks.END_STONE_BRICKS.defaultBlockState() : Blocks.END_STONE.defaultBlockState());
    }

    // =====================================================================================
    // Das Herz der Sonne
    // =====================================================================================

    /** Die Sonnenflecken (Winkel, Abstand, Größe). */
    private static final double[][] SPOTS = {{40.0, 21.0, 2.6}, {162.0, 18.5, 1.9}, {255.0, 23.0, 2.2}, {318.0, 17.5, 1.6}};

    private static void sunHeart(ServerLevel level, BlockPos floor, @Nullable BlockPos altar) {
        BlockState pearl = Blocks.PEARLESCENT_FROGLIGHT.defaultBlockState();
        BlockState ochre = Blocks.OCHRE_FROGLIGHT.defaultBlockState();
        BlockState gold = Blocks.GOLD_BLOCK.defaultBlockState();
        BlockState rawGold = Blocks.RAW_GOLD_BLOCK.defaultBlockState();
        BlockState honey = Blocks.HONEYCOMB_BLOCK.defaultBlockState();
        BlockState shroom = Blocks.SHROOMLIGHT.defaultBlockState();
        BlockState orange = Blocks.CONCRETE.pick(DyeColor.ORANGE).defaultBlockState();
        BlockState yellow = Blocks.CONCRETE.pick(DyeColor.YELLOW).defaultBlockState();
        BlockState copper = Blocks.CUT_COPPER.waxed().pick(WeatheringCopper.WeatherState.UNAFFECTED).defaultBlockState();
        BlockState terracotta = Blocks.DYED_TERRACOTTA.pick(DyeColor.ORANGE).defaultBlockState();
        BlockState umbra = Blocks.DYED_TERRACOTTA.pick(DyeColor.BROWN).defaultBlockState();
        disc(level, floor, (r, deg, dx, dz) -> {
            double h = hash(dx, dz, 91);
            if (r <= 2.2) {
                return pearl;                                                           // der weiße Kern
            }
            if (r <= 4.8) {
                return ochre;
            }
            if (r < 5.8) {
                return gold;
            }
            if (r < 13.5) {
                // Körnung: helle Zellen, dunklere Ränder
                double grain = ArenaIsland.noise(dx, dz, 0.42, 92) * 0.7 + ArenaIsland.noise(dx, dz, 0.9, 93) * 0.3;
                return grain > 0.64 ? ochre : grain > 0.48 ? gold : grain > 0.36 ? rawGold : honey;
            }
            if (r < 14.7) {
                return shroom;
            }
            if (r < 27.4) {
                // die Flecken: dunkler Kern, oranger Hof
                for (double[] spot : SPOTS) {
                    double sx = Math.cos(Math.toRadians(spot[0])) * spot[1];
                    double sz = Math.sin(Math.toRadians(spot[0])) * spot[1];
                    double d = Math.hypot(dx - sx, dz - sz) + (h - 0.5) * 0.6;
                    if (d < spot[2] * 0.55) {
                        return umbra;
                    }
                    if (d < spot[2]) {
                        return terracotta;
                    }
                }
                // sechzehn Strahlen, nach außen breiter
                double ray = Math.abs(((deg + 11.25) % 22.5) - 11.25);
                if (ray < 1.4 + (r - 14.7) * 0.12) {
                    return ray < 0.6 + (r - 14.7) * 0.05 ? yellow : orange;
                }
                double grain = ArenaIsland.noise(dx, dz, 0.35, 94);
                return h > 0.985 ? shroom : grain > 0.6 ? gold : grain > 0.4 ? rawGold : honey;
            }
            if (r < 28.6) {
                return ochre;
            }
            if (r < 38.2) {
                // der Kranz aus Flammen: vierundzwanzig Zungen aus Licht auf Kupfer
                double tongue = Math.abs(((deg + 7.5) % 15.0) - 7.5);
                double reach = (38.2 - r) * 0.75;
                if (tongue < reach * 0.45) {
                    return r < 31.0 ? shroom : yellow;
                }
                if (tongue < reach) {
                    return orange;
                }
                return h > 0.97 ? shroom : copper;
            }
            if (r < 40.4) {
                return ((int) (deg / 7.5)) % 2 == 0 ? gold : rawGold;
            }
            return h > 0.8 ? shroom : gold;
        }, gold, (d, vein, x, z) -> {
            if (vein > 0.72) {
                return shroom;
            }
            if (vein > 0.68) {
                return Blocks.MAGMA_BLOCK.defaultBlockState();
            }
            if (d % 6 == 0) {
                return Blocks.COPPER_BLOCK.waxed().pick(WeatheringCopper.WeatherState.UNAFFECTED).defaultBlockState();
            }
            return hash(x, z + d * 31, 13) > 0.35 ? rawGold : terracotta;
        }, pearl, ochre);
        for (int k = 0; k < 8; k++) {
            double a = Math.toRadians(22.5 + k * 45.0);
            pylon(level, floor.offset((int) Math.round(Math.cos(a) * 34.0), 0, (int) Math.round(Math.sin(a) * 34.0)), altar);
        }
        for (int k = 0; k < 4; k++) {
            prominence(level, floor, Math.toRadians(45.0 + k * 90.0), altar);
        }
        dais(level, floor, altar, gold, rawGold, ochre, pearl);
        sunDisc(level, floor, altar);
        // hoch darüber eine zweite Sonne
        sphere(level, floor.getX() + 0.5, floor.getY() + 40.0, floor.getZ() + 0.5, 6.2, 95, (lat, n, shell) -> !shell
                ? Blocks.PEARLESCENT_FROGLIGHT.defaultBlockState() : n > 0.66 ? Blocks.SHROOMLIGHT.defaultBlockState() : n > 0.4
                ? Blocks.OCHRE_FROGLIGHT.defaultBlockState() : Blocks.PEARLESCENT_FROGLIGHT.defaultBlockState());
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        for (int k = 0; k < 36; k++) {
            double a = Math.toRadians(k * 10.0 + 5.0);
            int x = floor.getX() + (int) Math.round(Math.cos(a) * (RADIUS - 3));
            int z = floor.getZ() + (int) Math.round(Math.sin(a) * (RADIUS - 3));
            level.setBlock(p.set(x, floor.getY() - 3 - (k % 3), z), rod(Direction.DOWN), QUIET);
        }
    }

    /** Ein Sonnenpylon: goldener Sockel, ein Schaft aus Gold und Glut, oben Licht und Strahlen. */
    private static void pylon(ServerLevel level, BlockPos base, @Nullable BlockPos altar) {
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        int x = base.getX();
        int y = base.getY();
        int z = base.getZ();
        int height = 12;
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (Math.abs(dx) + Math.abs(dz) <= 3) {
                    set(level, p, x + dx, y + 1, z + dz, Math.abs(dx) + Math.abs(dz) == 3 ? Blocks.RAW_GOLD_BLOCK.defaultBlockState()
                            : Blocks.GOLD_BLOCK.defaultBlockState(), altar);
                }
            }
        }
        for (int dy = 2; dy <= height; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    boolean corner = Math.abs(dx) == 1 && Math.abs(dz) == 1;
                    BlockState s = corner ? Blocks.GOLD_BLOCK.defaultBlockState() : dy % 3 == 0 ? Blocks.SHROOMLIGHT.defaultBlockState()
                            : Blocks.CUT_COPPER.waxed().pick(WeatheringCopper.WeatherState.UNAFFECTED).defaultBlockState();
                    set(level, p, x + dx, y + dy, z + dz, s, altar);
                }
            }
        }
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                set(level, p, x + dx, y + height + 1, z + dz, Blocks.GOLD_BLOCK.defaultBlockState(), altar);
            }
        }
        set(level, p, x, y + height + 2, z, Blocks.OCHRE_FROGLIGHT.defaultBlockState(), altar);
        set(level, p, x, y + height + 3, z, Blocks.PEARLESCENT_FROGLIGHT.defaultBlockState(), altar);
        set(level, p, x, y + height + 4, z, rod(Direction.UP), altar);
        for (Direction d : Direction.Plane.HORIZONTAL) {
            set(level, p, x + d.getStepX(), y + height + 2, z + d.getStepZ(), rod(d), altar);
        }
    }

    /** Ein Bogen aus Glut am Rand (eine Protuberanz): ein Halbkreis aus Pilzlicht in einer Hülle aus orangem Glas. */
    private static void prominence(ServerLevel level, BlockPos floor, double angle, @Nullable BlockPos altar) {
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        double cx = floor.getX() + 0.5 + Math.cos(angle) * 40.5;
        double cz = floor.getZ() + 0.5 + Math.sin(angle) * 40.5;
        double tx = -Math.sin(angle);
        double tz = Math.cos(angle);
        double radius = 8.5;
        for (int i = 0; i <= 120; i++) {
            double t = i * Math.PI / 120.0;
            double along = Math.cos(t) * radius;
            double up = Math.sin(t) * radius * 1.35;
            for (double w = -1.2; w <= 1.2; w += 0.6) {
                for (double v = -1.2; v <= 1.2; v += 0.6) {
                    double d = Math.hypot(w, v);
                    if (d > 1.3) {
                        continue;
                    }
                    // quer zum Bogen: nach außen (w) und entlang der Biegung (v)
                    double ox = Math.cos(angle) * w + tx * v * Math.cos(t);
                    double oz = Math.sin(angle) * w + tz * v * Math.cos(t);
                    double oy = v * Math.sin(t);
                    int x = (int) Math.floor(cx + tx * along + ox);
                    int y = floor.getY() + 1 + (int) Math.floor(up + oy);
                    int z = (int) Math.floor(cz + tz * along + oz);
                    BlockState s = d < 0.7 ? Blocks.SHROOMLIGHT.defaultBlockState() : Blocks.STAINED_GLASS.pick(DyeColor.ORANGE).defaultBlockState();
                    if (y > floor.getY() && level.getBlockState(p.set(x, y, z)).isAir() || d < 0.7) {
                        set(level, p, x, Math.max(y, floor.getY() + 1), z, s, altar);
                    }
                }
            }
        }
    }

    /** Hinter dem Altar: eine gewaltige Sonnenscheibe (vierundzwanzig Strahlen) auf zwei goldenen Stützen. */
    private static void sunDisc(ServerLevel level, BlockPos floor, @Nullable BlockPos altar) {
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        int x0 = floor.getX();
        int z = floor.getZ() + CreatorArena.THRONE_Z - 3;
        int cy = floor.getY() + 16;
        for (int dx = -11; dx <= 11; dx++) {
            for (int dy = -11; dy <= 11; dy++) {
                double r = Math.sqrt(dx * dx + dy * dy);
                double deg = (Math.toDegrees(Math.atan2(dy, dx)) + 360.0) % 360.0;
                BlockState s = null;
                if (r <= 2.5) {
                    s = Blocks.PEARLESCENT_FROGLIGHT.defaultBlockState();
                } else if (r <= 4.6) {
                    s = Blocks.OCHRE_FROGLIGHT.defaultBlockState();
                } else if (r <= 6.0) {
                    s = Blocks.GOLD_BLOCK.defaultBlockState();
                } else if (r <= 6.9) {
                    s = Blocks.RAW_GOLD_BLOCK.defaultBlockState();
                } else if (r <= 11.0 && Math.abs(((deg + 7.5) % 15.0) - 7.5) < 3.6 - (r - 6.9) * 0.75) {
                    s = r > 9.6 ? Blocks.SHROOMLIGHT.defaultBlockState() : Blocks.GOLD_BLOCK.defaultBlockState();
                }
                if (s != null) {
                    set(level, p, x0 + dx, cy + dy, z, s, altar);
                }
            }
        }
        for (int side : new int[] {-4, 4}) {
            for (int y = floor.getY() + 1; y < cy - 5; y++) {
                set(level, p, x0 + side, y, z, Blocks.GOLD_BLOCK.defaultBlockState(), altar);
                set(level, p, x0 + side, y, z + 1, Blocks.RAW_GOLD_BLOCK.defaultBlockState(), altar);
            }
        }
    }
}
