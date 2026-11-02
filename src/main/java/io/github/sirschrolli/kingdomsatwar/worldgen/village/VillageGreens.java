package io.github.sirschrolli.kingdomsatwar.worldgen.village;

import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.VillageBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Die Dorfwiesen: der lange <b>Anger</b> des Angerdorfs (Brunnen bzw. Maibaum am einen Ende, die Dorflinde in der
 * Mitte, am anderen Ende ein Teich mit Seerosen und Schilf, Bäume an den Rändern, ein Trampelpfad längs hindurch – im
 * Lehmland eine Oase mit Palmen) und die runde <b>Dorfwiese</b> des Rundlings (in der Mitte die Linde, der Maibaum oder
 * der Springbrunnen, ringsum ein Pfad, Bänke und Laternen).
 */
final class VillageGreens {
    private VillageGreens() {}

    // =====================================================================================
    // Anger
    // =====================================================================================

    static void anger(VillageBuilder b, VillagePalette p, KingdomVillagePiece piece) {
        int hw = piece.radius();
        int hl = piece.heights().length > 0 ? piece.heights()[0] : 14;
        boolean alongX = piece.direction().getAxis() == Direction.Axis.X;
        Arch a = new Arch(b, p);
        Green g = new Green(b, alongX);
        int wellAt = -hl / 2 - 1;
        int pondAt = hl / 2 + 1;
        int pondU = Math.max(2, hw - 2);
        int pondV = 4;
        // Rasen (im Lehmland Sand), darunter aufgefüllt
        for (int v = -hl; v <= hl; v++) {
            for (int u = -hw; u <= hw; u++) {
                g.column(u, v, subsoil(p), 14);
                g.set(u, 0, v, turf(b, p, g.x(u, v), g.z(u, v), pondDistance(u, v - pondAt, pondU, pondV)));
            }
        }
        // Trampelpfad längs durch den Anger: von den Enden zu Brunnen, Linde und Teich
        for (int v = -hl; v <= hl; v++) {
            g.set(0, 0, v, path(b, p, g.x(0, v), g.z(0, v)));
        }
        // Teich am einen Ende
        pond(b, p, g, 0, pondAt, pondU, pondV);
        // Brunnen (nordisch: Maibaum, im Lehmland ein Springbrunnen) am anderen Ende
        VillageBuilder well = g.sub(0, wellAt);
        switch (p.style) {
            case NORDIC -> VillageSquare.maypole(well, new Arch(well, p), p);
            case CLAY -> VillageSquare.fountain(well, new Arch(well, p), p, true);
            default -> VillageSquare.well(well, new Arch(well, p), p);
        }
        well.finish();
        // in der Mitte die Dorflinde (nordisch: zwei Fichten, im Lehmland Palmen)
        switch (p.style) {
            case NORDIC -> {
                spruce(b, g, -(hw - 2), 0);
                spruce(b, g, hw - 2, 2);
            }
            case CLAY -> {
                palm(b, g, -(hw - 1), -2, 1);
                palm(b, g, hw - 1, 1, -1);
            }
            default -> {
                VillageBuilder linden = g.sub(0, 0);
                VillageSquare.linden(linden, new Arch(linden, p), p);
                linden.finish();
            }
        }
        // Bäume: zu beiden Seiten hinter dem Brunnen und hinter dem Teich (wo der Anger lang genug ist), im Lehmland Palmen
        // am Teich
        if (p.style == VillageStyle.CLAY) {
            palm(b, g, -(pondU + 1), pondAt - 2, -1);
            palm(b, g, pondU + 1, pondAt + 2, 1);
        } else {
            for (int v : new int[] {wellAt - 5, pondAt + pondV + 2}) {
                if (v < -hl || v > hl) {
                    continue;
                }
                for (int su = -1; su <= 1; su += 2) {
                    if (p.style == VillageStyle.NORDIC) {
                        spruce(b, g, su * (hw - 2), v);
                    } else {
                        broadleaf(b, g, su * (hw - 2), v, su > 0);
                    }
                }
            }
        }
        // Bänke am Teich, mit Blick auf das Wasser
        g.set(-(pondU + 1), 1, pondAt - pondV + 1, a.bench(g.dir(Direction.EAST)));
        g.set(pondU + 1, 1, pondAt + 1, a.bench(g.dir(Direction.WEST)));
        // Laternen an den vier Ecken
        for (int su = -1; su <= 1; su += 2) {
            for (int sv = -1; sv <= 1; sv += 2) {
                VillageSquare.lampPost(b, p, g.x(su * hw, sv * hl), g.z(su * hw, sv * hl));
            }
        }
        flowers(b, p, g, hw, hl);
        // zwei Wächter auf dem Anger
        piece.guard(b, g.x(1, wellAt + 4), 1, g.z(1, wellAt + 4), false);
        piece.guard(b, g.x(-1, pondAt - pondV - 2), 1, g.z(-1, pondAt - pondV - 2), b.random.nextBoolean());
    }

    // =====================================================================================
    // Runde Dorfwiese
    // =====================================================================================

    static void round(VillageBuilder b, VillagePalette p, KingdomVillagePiece piece) {
        int r = piece.radius();
        Arch a = new Arch(b, p);
        Green g = new Green(b, false);
        for (int x = -r; x <= r; x++) {
            for (int z = -r; z <= r; z++) {
                double d = Math.sqrt(x * x + z * z);
                if (d > r + 0.5) {
                    continue;
                }
                g.column(x, z, subsoil(p), 14);
                // ringsum ein Pfad
                boolean ring = d > r - 2.5 && d <= r - 1.5;
                b.set(x, 0, z, ring ? path(b, p, x, z) : turf(b, p, x, z, 99.0));
            }
        }
        // in der Mitte: Dorflinde, Maibaum oder Springbrunnen
        switch (p.style) {
            case NORDIC -> VillageSquare.maypole(b, a, p);
            case CLAY -> VillageSquare.fountain(b, a, p, true);
            default -> VillageSquare.linden(b, a, p);
        }
        // Bänke mit Blick zur Mitte
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            int bx = dir.getStepX() * (r - 4);
            int bz = dir.getStepZ() * (r - 4);
            if (b.get(bx, 1, bz).isAir()) {
                b.set(bx, 1, bz, a.bench(dir.getOpposite()));
            }
        }
        // Laternen an den Diagonalen, Bäume dazwischen
        int lamp = Math.max(4, (int) Math.round((r - 2.5) / Math.sqrt(2.0)));
        for (int sx = -1; sx <= 1; sx += 2) {
            for (int sz = -1; sz <= 1; sz += 2) {
                VillageSquare.lampPost(b, p, sx * lamp, sz * lamp);
            }
        }
        int[][] trees = {{-(r - 4), -2}, {r - 4, 2}, {2, r - 4}, {-2, -(r - 4)}};
        for (int i = 0; i < trees.length; i++) {
            if ((b.random.nextInt(3) == 0) && i > 1) {
                continue;
            }
            int[] t = trees[i];
            switch (p.style) {
                case NORDIC -> spruce(b, g, t[0], t[1]);
                case CLAY -> palm(b, g, t[0], t[1], t[0] > 0 ? -1 : 1);
                default -> broadleaf(b, g, t[0], t[1], i % 2 == 0);
            }
        }
        flowers(b, p, g, r, r);
        piece.guard(b, 3, 1, -(r - 3), false);
        piece.guard(b, -3, 1, r - 3, b.random.nextBoolean());
    }

    // =====================================================================================
    // Bausteine
    // =====================================================================================

    /** Koordinaten auf der Wiese: u quer, v längs (beim Anger in Richtung seiner Achse). */
    private record Green(VillageBuilder b, boolean alongX) {
        int x(int u, int v) {
            return this.alongX ? v : u;
        }

        int z(int u, int v) {
            return this.alongX ? u : v;
        }

        void set(int u, int y, int v, BlockState state) {
            this.b.set(this.x(u, v), y, this.z(u, v), state);
        }

        BlockState get(int u, int y, int v) {
            return this.b.get(this.x(u, v), y, this.z(u, v));
        }

        void column(int u, int v, BlockState fill, int clear) {
            this.b.column(this.x(u, v), this.z(u, v), fill, clear);
        }

        /** Eine Richtung im Rahmen der Wiese (Osten = +u) als Richtung im Rahmen des Platzes. */
        Direction dir(Direction uv) {
            if (!this.alongX) {
                return uv;
            }
            // u ist z, v ist x: Ost (+u) wird Süd (+z), Süd (+v) wird Ost (+x)
            return switch (uv) {
                case EAST -> Direction.SOUTH;
                case WEST -> Direction.NORTH;
                case SOUTH -> Direction.EAST;
                case NORTH -> Direction.WEST;
                default -> uv;
            };
        }

        /** Ein Bauhelfer, dessen Ursprung an (u, v) liegt (für Brunnen, Linde und Maibaum). */
        VillageBuilder sub(int u, int v) {
            BlockPos origin = this.b.world(this.x(u, v), 0, this.z(u, v));
            return new VillageBuilder(this.b.level, this.b.chunkBox, origin, this.b.rotation, this.b.random.nextLong());
        }
    }

    /** Wie weit (u, v) vom Teich entfernt ist (unter 1: im Wasser). */
    private static double pondDistance(int u, int v, int ru, int rv) {
        double eu = u / (double) ru;
        double ev = v / (double) rv;
        return Math.sqrt(eu * eu + ev * ev);
    }

    private static BlockState subsoil(VillagePalette p) {
        return p.style == VillageStyle.CLAY ? Blocks.SANDSTONE.defaultBlockState() : Blocks.DIRT.defaultBlockState();
    }

    /** Rasen; im Lehmland Sand, nur nahe am Wasser Gras (die Oase). */
    private static BlockState turf(VillageBuilder b, VillagePalette p, int x, int z, double pond) {
        float n = b.noise(x, 0, z);
        return switch (p.style) {
            case CLAY -> pond < 1.9 || n < 0.12f ? Blocks.GRASS_BLOCK.defaultBlockState()
                    : n < 0.3f ? Blocks.COARSE_DIRT.defaultBlockState() : Blocks.SAND.defaultBlockState();
            case NORDIC -> n < 0.08f ? Blocks.PODZOL.defaultBlockState() : n < 0.14f ? Blocks.COARSE_DIRT.defaultBlockState()
                    : Blocks.GRASS_BLOCK.defaultBlockState();
            default -> Blocks.GRASS_BLOCK.defaultBlockState();
        };
    }

    private static BlockState path(VillageBuilder b, VillagePalette p, int x, int z) {
        return switch (p.style) {
            case CLAY -> Blocks.SMOOTH_SANDSTONE.defaultBlockState();
            case NORDIC -> b.noise(x, 1, z) < 0.5f ? Blocks.GRAVEL.defaultBlockState() : Blocks.DIRT_PATH.defaultBlockState();
            default -> Blocks.DIRT_PATH.defaultBlockState();
        };
    }

    /** Ein Teich: in der Mitte zwei Blöcke tief, mit Seerosen; am Ufer Schilf (Zuckerrohr) und ein paar Steine. */
    private static void pond(VillageBuilder b, VillagePalette p, Green g, int cu, int cv, int ru, int rv) {
        for (int v = cv - rv - 1; v <= cv + rv + 1; v++) {
            for (int u = cu - ru - 1; u <= cu + ru + 1; u++) {
                double d = pondDistance(u - cu, v - cv, ru, rv);
                int x = g.x(u, v);
                int z = g.z(u, v);
                if (d < 1.0) {
                    boolean deep = d < 0.6;
                    g.set(u, deep ? -2 : -1, v, p.style == VillageStyle.CLAY ? Blocks.SAND.defaultBlockState() : Blocks.CLAY.defaultBlockState());
                    if (deep) {
                        g.set(u, -1, v, Blocks.WATER.defaultBlockState());
                    }
                    g.set(u, 0, v, Blocks.WATER.defaultBlockState());
                    g.set(u, 1, v, b.noise(x, 2, z) < 0.14f && deep ? Blocks.LILY_PAD.defaultBlockState() : Blocks.AIR.defaultBlockState());
                } else if (d < 1.3) {
                    float n = b.noise(x, 3, z);
                    if (n < 0.22f && p.style != VillageStyle.NORDIC) {
                        int height = 2 + (n < 0.1f ? 1 : 0);
                        for (int y = 1; y <= height; y++) {
                            g.set(u, y, v, Blocks.SUGAR_CANE.defaultBlockState());
                        }
                    } else if (n < 0.32f) {
                        g.set(u, 0, v, Blocks.MOSSY_COBBLESTONE.defaultBlockState());
                    } else if (n < 0.5f && p.style == VillageStyle.NORDIC) {
                        g.set(u, 1, v, Blocks.FERN.defaultBlockState());
                    }
                }
            }
        }
    }

    /** Blumen und Gras auf dem Rasen (nur wo Rasen liegt und darüber frei ist). */
    private static void flowers(VillageBuilder b, VillagePalette p, Green g, int hw, int hl) {
        Block[] kinds = switch (p.style) {
            case NORDIC -> new Block[] {Blocks.FERN, Blocks.SHORT_GRASS, Blocks.LILY_OF_THE_VALLEY, Blocks.SHORT_GRASS};
            case CLAY -> new Block[] {Blocks.DEAD_BUSH, Blocks.SHORT_GRASS, Blocks.ORANGE_TULIP};
            default -> new Block[] {Blocks.SHORT_GRASS, Blocks.DANDELION, Blocks.POPPY, Blocks.OXEYE_DAISY, Blocks.CORNFLOWER, Blocks.AZURE_BLUET,
                    Blocks.SHORT_GRASS};
        };
        for (int v = -hl; v <= hl; v++) {
            for (int u = -hw; u <= hw; u++) {
                int x = g.x(u, v);
                int z = g.z(u, v);
                float n = b.noise(x, 5, z);
                if (n > 0.16f) {
                    continue;
                }
                BlockState ground = g.get(u, 0, v);
                boolean soil = ground.is(Blocks.GRASS_BLOCK) || ground.is(Blocks.PODZOL) || ground.is(Blocks.SAND) && kinds[0] == Blocks.DEAD_BUSH;
                if (soil && g.get(u, 1, v).isAir() && g.get(u, 2, v).isAir()) {
                    Block plant = kinds[(int) (n / 0.16f * kinds.length) % kinds.length];
                    if (plant == Blocks.DEAD_BUSH && !ground.is(Blocks.SAND)) {
                        plant = Blocks.SHORT_GRASS;
                    }
                    g.set(u, 1, v, plant.defaultBlockState());
                }
            }
        }
    }

    private static BlockState persistent(Block leaves) {
        return leaves.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
    }

    /** Ein Laubbaum (Eiche oder Birke): Stamm und eine runde Krone. */
    private static void broadleaf(VillageBuilder b, Green g, int u, int v, boolean birch) {
        BlockState log = (birch ? Blocks.BIRCH_LOG : Blocks.OAK_LOG).defaultBlockState();
        BlockState leaves = persistent(birch ? Blocks.BIRCH_LEAVES : Blocks.OAK_LEAVES);
        int height = birch ? 6 : 5;
        for (int y = 1; y <= height; y++) {
            g.set(u, y, v, log);
        }
        for (int dy = height - 2; dy <= height + 1; dy++) {
            int rad = dy >= height + 1 ? 1 : 2;
            for (int du = -rad; du <= rad; du++) {
                for (int dv = -rad; dv <= rad; dv++) {
                    if (Math.abs(du) == rad && Math.abs(dv) == rad && (rad == 2 || dy == height + 1)) {
                        continue;
                    }
                    if (g.get(u + du, dy, v + dv).isAir()) {
                        g.set(u + du, dy, v + dv, leaves);
                    }
                }
            }
        }
    }

    /** Eine Fichte: hoher Stamm, nach oben schmaler werdende Ringe aus Nadeln. */
    private static void spruce(VillageBuilder b, Green g, int u, int v) {
        BlockState log = Blocks.SPRUCE_LOG.defaultBlockState();
        BlockState leaves = persistent(Blocks.SPRUCE_LEAVES);
        int height = 7;
        for (int y = 1; y <= height; y++) {
            g.set(u, y, v, log);
        }
        int[] radius = {0, 0, 0, 2, 1, 2, 1, 1, 0};
        for (int y = 3; y <= height + 1; y++) {
            int rad = y < radius.length ? radius[y] : 0;
            for (int du = -rad; du <= rad; du++) {
                for (int dv = -rad; dv <= rad; dv++) {
                    if (Math.abs(du) + Math.abs(dv) > rad + (rad > 1 ? 1 : 0)) {
                        continue;
                    }
                    if (g.get(u + du, y, v + dv).isAir()) {
                        g.set(u + du, y, v + dv, leaves);
                    }
                }
            }
        }
        g.set(u, height + 1, v, leaves);
    }

    /** Eine Palme: leicht geneigter Stamm (Richtung {@code lean} quer zur Wiese), oben ein Kranz aus Wedeln. */
    private static void palm(VillageBuilder b, Green g, int u, int v, int lean) {
        BlockState log = Blocks.JUNGLE_LOG.defaultBlockState();
        BlockState leaves = persistent(Blocks.JUNGLE_LEAVES);
        int top = 6;
        int cu = u;
        for (int y = 1; y <= top; y++) {
            if (y == 4) {
                cu += lean;
            }
            g.set(cu, y, v, log);
        }
        g.set(cu, top + 1, v, leaves);
        for (int[] d : new int[][] {{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
            g.set(cu + d[0], top + 1, v + d[1], leaves);
            g.set(cu + 2 * d[0], top, v + 2 * d[1], leaves);
            g.set(cu + 3 * d[0], top - 1, v + 3 * d[1], leaves);
        }
        for (int[] d : new int[][] {{1, 1}, {-1, 1}, {1, -1}, {-1, -1}}) {
            g.set(cu + d[0], top, v + d[1], leaves);
        }
    }
}
