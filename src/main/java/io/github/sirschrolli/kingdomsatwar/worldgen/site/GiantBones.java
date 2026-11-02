package io.github.sirschrolli.kingdomsatwar.worldgen.site;

import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.VillageBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * Riesengebeine: Im Seelensandtal liegen die Knochen gewaltiger Wesen aus alter Zeit, halb im Sand versunken – ein
 * Brustkorb, unter dessen Rippen man hindurchgeht, ein gehörnter Schädel mit Seelenfeuer hinter den Augenhöhlen, das
 * Rückgrat einer Riesenschlange, das sich in Bögen durch den Sand windet, oder eine Knochenhand, die aus dem Boden
 * greift. Manchmal liegt ein vertrockneter Ghast dabei (an Stelle der Netherfossilien).
 *
 * <p>Eigene Koordinaten: (0, 0, 0) ist der erste freie Block über dem Boden in der Mitte, vorn ist +z.
 */
final class GiantBones {
    static final int RIBCAGE = 0;
    static final int SKULL = 1;
    static final int SERPENT = 2;
    static final int CLAW = 3;
    static final int VARIANTS = 4;
    /** Wie weit die Gebeine höchstens reichen (in eigenen Koordinaten). */
    private static final int REACH_X = 10;
    private static final int REACH_Z = 22;
    private static final int[][] PROBES = {{6, 0}, {-6, 0}, {0, 6}, {0, -6}, {4, 4}, {-4, 4}, {4, -4}, {-4, -4}, {0, 10}, {0, -10}};

    private GiantBones() {}

    static Optional<Structure.GenerationStub> plan(Structure.GenerationContext context, SiteTerrain terrain, long seed) {
        ChunkPos chunk = context.chunkPos();
        RandomSource random = RandomSource.create(seed);
        int x = chunk.getMinBlockX() + 4 + random.nextInt(8);
        int z = chunk.getMinBlockZ() + 4 + random.nextInt(8);
        int sea = terrain.seaLevel();
        int top = Math.min(terrain.minY() + context.heightAccessor().getHeight(), 120) - 12;
        for (int attempt = 0; attempt < 4; attempt++) {
            int start = sea + 6 + random.nextInt(Math.max(1, top - sea - 6));
            int floor = floor(terrain, x, z, start, sea + 1);
            if (floor == Integer.MIN_VALUE || !roomy(terrain, x, z, floor) || !terrain.valid(x, floor, z)) {
                continue;
            }
            int variant = Math.floorMod((int) (seed >>> 17), VARIANTS);
            Rotation rotation = Rotation.values()[Math.floorMod((int) (seed >>> 5), 4)];
            BlockPos origin = new BlockPos(x, floor, z);
            SitePiece piece = piece(origin, rotation, seed, variant);
            return Optional.of(new Structure.GenerationStub(origin, builder -> builder.addPiece(piece)));
        }
        return Optional.empty();
    }

    /** Der erste freie Block über festem Grund, von {@code start} abwärts gesucht (nicht tiefer als {@code lowest}). */
    private static int floor(SiteTerrain terrain, int x, int z, int start, int lowest) {
        boolean air = false;
        for (int y = start; y > lowest; y--) {
            boolean rock = terrain.rock(x, y - 1, z);
            boolean free = !terrain.rock(x, y, z) && !terrain.fluid(x, y, z);
            if (free) {
                air = true;
            }
            if (air && free && rock) {
                return y;
            }
        }
        return Integer.MIN_VALUE;
    }

    /** Genug Platz: über der Mitte neun Blöcke frei, ringsum Boden in ähnlicher Höhe. */
    private static boolean roomy(SiteTerrain terrain, int x, int z, int floor) {
        for (int y = floor; y <= floor + 9; y++) {
            if (terrain.rock(x, y, z) || terrain.fluid(x, y, z)) {
                return false;
            }
        }
        int misses = 0;
        for (int[] p : PROBES) {
            int found = Integer.MIN_VALUE;
            for (int y = floor + 3; y >= floor - 4; y--) {
                if (!terrain.rock(x + p[0], y, z + p[1]) && !terrain.fluid(x + p[0], y, z + p[1]) && terrain.rock(x + p[0], y - 1, z + p[1])) {
                    found = y;
                    break;
                }
            }
            if (found == Integer.MIN_VALUE || terrain.rock(x + p[0], found + 3, z + p[1])) {
                misses++;
            }
        }
        return misses <= 2;
    }

    static SitePiece piece(BlockPos origin, Rotation rotation, long seed, int variant) {
        return new SitePiece(SiteKind.GIANT_BONES, variant, origin, rotation, seed, new int[] {variant},
                SitePiece.box(origin, rotation, -REACH_X, -6, -REACH_Z, REACH_X, 13, REACH_Z));
    }

    static void build(VillageBuilder b, SitePiece piece) {
        new Bones(b, piece.seed(), piece.data(0)).build();
    }

    private static final class Bones {
        private final VillageBuilder b;
        private final RandomSource random;
        private final int variant;
        private final boolean ghast;
        /** Wo das Gerippe Platz für einen vertrockneten Ghast lässt. */
        private final List<int[]> ghastSpots = new ArrayList<>();
        private int lx = Integer.MIN_VALUE;
        private int ly;
        private int lz;

        Bones(VillageBuilder b, long seed, int variant) {
            this.b = b;
            // alles, was der Zufall entscheidet, wird vor dem Bauen gezogen: jeder Chunk baut dasselbe Gerippe
            this.random = RandomSource.create(seed ^ 0x6A09E667F3BCC908L);
            this.variant = Math.floorMod(variant, VARIANTS);
            this.ghast = this.random.nextFloat() < 0.5f;
        }

        void build() {
            switch (this.variant) {
                case RIBCAGE -> this.ribcage();
                case SKULL -> this.skull();
                case SERPENT -> this.serpent();
                default -> this.claw();
            }
            this.fragments();
            this.soil();
            if (this.ghast && !this.ghastSpots.isEmpty()) {
                int[] spot = this.ghastSpots.get(Math.floorMod((int) (this.b.noise(7, 7, 7) * 1000), this.ghastSpots.size()));
                this.driedGhast(spot[0], spot[1]);
            }
        }

        // =====================================================================================
        // Zeichnen
        // =====================================================================================

        static BlockState bone(Direction.Axis axis) {
            return Blocks.BONE_BLOCK.defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis);
        }

        private static int round(double v) {
            return (int) Math.floor(v + 0.5);
        }

        private static Direction.Axis axis(double dx, double dy, double dz) {
            double ax = Math.abs(dx);
            double ay = Math.abs(dy);
            double az = Math.abs(dz);
            return ay >= ax && ay >= az ? Direction.Axis.Y : ax >= az ? Direction.Axis.X : Direction.Axis.Z;
        }

        /** Ein Knochenblock (unter dem Boden versinkt er einfach im Sand). */
        void put(int x, int y, int z, Direction.Axis axis) {
            if (y >= -5) {
                this.b.set(x, y, z, bone(axis));
            }
        }

        /** Beginnt einen neuen Knochen (der nächste Strich schließt nicht an den letzten an). */
        void lift() {
            this.lx = Integer.MIN_VALUE;
        }

        /**
         * Ein Knochen von a nach b, Block an Block (ohne Lücken über Eck), jeder Block nach der Richtung des Knochens
         * ausgerichtet.
         */
        void line(double x0, double y0, double z0, double x1, double y1, double z1) {
            double dx = x1 - x0;
            double dy = y1 - y0;
            double dz = z1 - z0;
            Direction.Axis axis = axis(dx, dy, dz);
            int n = (int) Math.ceil(Math.max(Math.abs(dx), Math.max(Math.abs(dy), Math.abs(dz))) * 2.0) + 1;
            for (int i = 0; i <= n; i++) {
                double t = i / (double) n;
                int x = round(x0 + dx * t);
                int y = round(y0 + dy * t);
                int z = round(z0 + dz * t);
                if (this.lx != Integer.MIN_VALUE) {
                    if (x == this.lx && y == this.ly && z == this.lz) {
                        continue;
                    }
                    // über Eck: Schritt für Schritt, erst entlang der Hauptrichtung, damit der Knochen zusammenhängt
                    int cx = this.lx;
                    int cy = this.ly;
                    int cz = this.lz;
                    for (int pass = 0; pass < 3; pass++) {
                        Direction.Axis a = pass == 0 ? axis : pass == 1 ? (axis == Direction.Axis.Y ? Direction.Axis.X : Direction.Axis.Y)
                                : (axis == Direction.Axis.Z ? Direction.Axis.X : Direction.Axis.Z);
                        if (a == Direction.Axis.X && cx != x) {
                            cx = x;
                        } else if (a == Direction.Axis.Y && cy != y) {
                            cy = y;
                        } else if (a == Direction.Axis.Z && cz != z) {
                            cz = z;
                        } else {
                            continue;
                        }
                        if (cx != x || cy != y || cz != z) {
                            this.put(cx, cy, cz, axis);
                        }
                    }
                }
                this.put(x, y, z, axis);
                this.lx = x;
                this.ly = y;
                this.lz = z;
            }
        }

        /** Ein Knochen durch mehrere Punkte ({x, y, z} je Punkt). */
        void path(double[]... points) {
            this.lift();
            for (int i = 1; i < points.length; i++) {
                this.line(points[i - 1][0], points[i - 1][1], points[i - 1][2], points[i][0], points[i][1], points[i][2]);
            }
            this.lift();
        }

        /** Wo ein Knochen auf den Boden trifft: liegt der Grund tiefer, reicht er bis hinunter (höchstens fünf Blöcke). */
        void anchor(int x, int y, int z) {
            for (int k = 1; k <= 5; k++) {
                BlockState below = this.b.get(x, y - k, z);
                if (!below.isAir() && below.getFluidState().isEmpty() && !below.canBeReplaced()) {
                    return;
                }
                this.put(x, y - k, z, Direction.Axis.Y);
            }
        }

        /** Seelenfeuer auf Seelenerde. */
        void soulFire(int x, int y, int z) {
            this.b.set(x, y - 1, z, Blocks.SOUL_SOIL.defaultBlockState());
            this.b.set(x, y, z, Blocks.SOUL_FIRE.defaultBlockState());
        }

        /** Der vertrocknete Ghast: auf dem Boden an dieser Stelle, wenn dort Platz ist. */
        void driedGhast(int x, int z) {
            for (int y = 2; y >= -3; y--) {
                BlockState here = this.b.get(x, y, z);
                BlockState below = this.b.get(x, y - 1, z);
                if ((here.isAir() || here.canBeReplaced() && here.getFluidState().isEmpty()) && below.isSolidRender()) {
                    Direction facing = Direction.from2DDataValue(Math.floorMod((int) (this.b.noise(x, 9, z) * 4), 4));
                    this.b.set(x, y, z, Blocks.DRIED_GHAST.defaultBlockState()
                            .setValue(net.minecraft.world.level.block.DriedGhastBlock.FACING, facing));
                    return;
                }
            }
        }

        // =====================================================================================
        // Der Brustkorb
        // =====================================================================================

        /** Das Rückgrat eines Riesen, der auf dem Bauch liegt: vorn hoch, Hals und Schwanz tauchen in den Sand. */
        private double spineY(double z, double half, double height, double apex) {
            double s = (z - apex) / (half + 2.0);
            return -1.5 + (height + 1.5) * Math.sqrt(Math.max(0.0, 1.0 - s * s));
        }

        /**
         * Ein Brustkorb, groß genug, um darunter hindurchzugehen: ein Rückgrat im Bogen, alle drei Blöcke ein Rippenpaar in
         * weitem, sauberem Bogen bis in den Sand, hinten das Becken; vorn steckt der Schädel im Sand.
         */
        void ribcage() {
            double half = 11 + this.random.nextInt(3);
            double height = 8 + this.random.nextInt(2);
            double width = 5.0 + this.random.nextDouble();
            double apex = half * 0.15;
            int brokenRib = this.random.nextInt(5);
            int brokenSide = this.random.nextBoolean() ? 1 : -1;
            double breakAt = 0.8 + this.random.nextDouble() * 0.4;
            boolean horns = this.random.nextBoolean();

            // das Rückgrat
            int z0 = (int) Math.floor(apex - half - 2.5);
            int z1 = (int) Math.ceil(apex + half + 2.5);
            double[][] spine = new double[z1 - z0 + 1][];
            for (int z = z0; z <= z1; z++) {
                spine[z - z0] = new double[] {0, this.spineY(z, half, height, apex), z};
            }
            this.path(spine);

            // die Rippen: alle drei Blöcke ein Paar, zu den Enden hin kürzer
            int k = 0;
            for (double zr = apex - half + 4; zr <= apex + half - 3; zr += 3, k++) {
                double ys = Math.round(this.spineY(zr, half, height, apex));
                if (ys < 4) {
                    continue;
                }
                this.put(0, (int) ys + 1, (int) zr, Direction.Axis.Y);             // Dornfortsatz
                double f = Math.sqrt(Math.max(0.0, 1.0 - Math.pow((zr - apex) / (half + 1), 2)));
                double r = width * (0.55 + 0.45 * f);
                double yc = ys * 0.4;
                for (int sx : new int[] {-1, 1}) {
                    boolean snap = k == brokenRib && sx == brokenSide;
                    double end = snap ? breakAt : 2.3;
                    this.lift();
                    double px = 0;
                    double py = ys;
                    double pz = zr;
                    for (int step = 1; step <= 18; step++) {
                        double th = end * step / 18.0;
                        double x = sx * r * Math.sin(th);
                        double y = yc + (ys - yc) * Math.cos(th);
                        double z = zr - 0.4 * th / 2.3;
                        this.line(px, py, pz, x, y, z);
                        px = x;
                        py = y;
                        pz = z;
                    }
                    this.lift();
                    if (snap) {
                        // das abgebrochene Stück liegt darunter im Sand
                        this.path(new double[] {sx * (r - 0.5), 0, zr + 1}, new double[] {sx * (r + 1.5), 0, zr - 1.5});
                    } else {
                        this.anchor(round(px), round(py), round(pz));
                    }
                }
                this.ghastSpots.add(new int[] {0, (int) zr});
            }

            // das Becken: zwei Bögen vom Rückgrat in den Sand
            double zp = apex - half + 1;
            double yp = Math.round(this.spineY(zp, half, height, apex));
            if (yp >= 2) {
                for (int sx : new int[] {-1, 1}) {
                    this.path(new double[] {0, yp, zp}, new double[] {sx * 2.5, yp - 0.5, zp - 0.5}, new double[] {sx * 3.8, yp - 2.5, zp - 0.5},
                            new double[] {sx * 4.2, -1, zp + 0.5});
                }
            }

            // vorn steckt der Schädel im Sand: nur die Stirn mit den Augenhöhlen schaut heraus – und vielleicht die Hörner
            int zh = (int) Math.round(apex + half + 3.5);
            for (int x = -3; x <= 3; x++) {
                for (int z = zh; z <= zh + 3; z++) {
                    boolean corner = Math.abs(x) == 3 && (z == zh || z == zh + 3);
                    boolean eye = Math.abs(x) >= 1 && Math.abs(x) <= 2 && z == zh + 1;
                    if (eye) {
                        this.soulFire(x, 0, z);
                    } else if (!corner) {
                        this.put(x, 0, z, Direction.Axis.Y);
                    }
                }
            }
            for (int z = zh; z <= zh + 2; z++) {
                this.put(0, 1, z, Direction.Axis.Z);                               // Scheitelkamm
            }
            if (horns) {
                for (int sx : new int[] {-1, 1}) {
                    this.path(new double[] {sx * 3, 0, zh + 1}, new double[] {sx * 4.5, 2, zh}, new double[] {sx * 5.5, 4, zh - 1},
                            new double[] {sx * 5.2, 5.6, zh - 2.6});
                }
            }
        }

        // =====================================================================================
        // Der Schädel
        // =====================================================================================

        private static final double CR_X = 4.6;
        private static final double CR_Y = 4.0;
        private static final double CR_Z = 4.4;
        private static final double CR_CY = 3.2;
        private static final double CR_CZ = -3.2;

        /** Die Schädelkapsel: ein abgerundeter Quader (Superellipsoid). */
        private static double cranium(double x, double y, double z) {
            return Math.pow(Math.abs(x / CR_X), 2.6) + Math.pow(Math.abs((y - CR_CY) / CR_Y), 2.6) + Math.pow(Math.abs((z - CR_CZ) / CR_Z), 2.6);
        }

        /** Halbe Breite und Oberkante der Schnauze bei z (vorn schmaler und flacher). */
        private static double snoutWidth(double z) {
            return 2.6 - Math.max(0.0, z - 2) * 0.18;
        }

        private static double snoutTop(double z) {
            return 4.4 - Math.max(0.0, z - 1) * 0.2;
        }

        /** Der Oberkiefer: von der Stirn nach vorn, unten die Zahnreihe eine Schicht über dem Unterkiefer. */
        private static boolean snout(int x, int y, int z) {
            return z >= -1 && z <= 7 && y >= 1 && y <= snoutTop(z) && Math.abs(x) <= snoutWidth(z);
        }

        private static boolean skullInside(int x, int y, int z) {
            return y >= 0 && (cranium(x, y, z) <= 1.0 || snout(x, y, z));
        }

        /** Gehört der Block zur Hülle des Schädels (innen, aber an das Äußere grenzend, auch über Eck; nicht nach unten)? */
        private static boolean skullShell(int x, int y, int z) {
            if (!skullInside(x, y, z)) {
                return false;
            }
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = 0; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (!skullInside(x + dx, y + dy, z + dz) && (dy > 0 || y + dy >= 1 || cranium(x + dx, y, z + dz) > 1.0)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        /**
         * Ein gehörnter Schädel, der auf seinem Unterkiefer ruht: eine runde Schädelkapsel, davor die lange Schnauze mit
         * einem Nasenrücken, zwei Nasenlöchern und einer Reihe Zähne, tiefe Augenhöhlen, in denen Seelenfeuer brennt,
         * Jochbögen an den Seiten und zwei mächtige, geschwungene Hörner. Hinten ist er aufgebrochen; drinnen glüht es.
         */
        void skull() {
            int hornStyle = this.random.nextInt(2);
            for (int x = -6; x <= 6; x++) {
                for (int y = 0; y <= 8; y++) {
                    for (int z = -9; z <= 8; z++) {
                        if (!skullShell(x, y, z)) {
                            continue;
                        }
                        boolean snoutPart = cranium(x, y, z) > 1.0;
                        if (y == 1 && snoutPart) {
                            // die Zahnreihe: Zahn an Zahn mit Lücken, vorn zwei Fangzähne
                            boolean front = z == 7 || !snout(x, y, z + 1);
                            if (front ? Math.abs(x) != 1 : Math.floorMod(z, 2) == 1 || Math.abs(x) < snoutWidth(z) - 1.0) {
                                continue;
                            }
                        } else if (y == 0 && Math.pow(Math.abs(x / CR_X), 2.6) + Math.pow(Math.abs((z - CR_CZ) / CR_Z), 2.6) < 0.6) {
                            continue;                       // innen ist Boden, kein Knochen
                        }
                        if (z <= -6 && y <= 3 && Math.abs(x) <= 1) {
                            continue;                       // aufgebrochen: hier geht es hinein
                        }
                        this.put(x, y, z, Direction.Axis.Y);
                    }
                }
            }
            // der Unterkiefer im Sand: zwei Äste unter der Zahnreihe, vorn verbunden
            for (int z = 0; z <= 7; z++) {
                int w = (int) Math.floor(snoutWidth(z) + 0.01);
                this.put(w, 0, z, Direction.Axis.Z);
                this.put(-w, 0, z, Direction.Axis.Z);
            }
            for (int x = -1; x <= 1; x++) {
                this.put(x, 0, 7, Direction.Axis.X);
            }
            // der Nasenrücken und die Nasenlöcher
            for (int z = 0; z <= 5; z++) {
                this.put(0, (int) Math.floor(snoutTop(z)) + 1, z, Direction.Axis.Z);
            }
            for (int sx : new int[] {-1, 1}) {
                for (int z = 6; z <= 7; z++) {
                    this.b.set(sx, (int) Math.floor(snoutTop(z)), z, Blocks.AIR.defaultBlockState());
                }
            }
            for (int sx : new int[] {-1, 1}) {
                // die Augenhöhlen: tief, mit Seelenerde am Grund und Seelenfeuer darin
                for (int x = 2; x <= 3; x++) {
                    for (int y = 5; y <= 6; y++) {
                        for (int z = -2; z <= 1; z++) {
                            this.b.set(sx * x, y, z, Blocks.AIR.defaultBlockState());
                        }
                    }
                    for (int z = -2; z <= 0; z++) {
                        this.b.set(sx * x, 4, z, Blocks.SOUL_SOIL.defaultBlockState());
                    }
                    this.b.set(sx * x, 5, -1, Blocks.SOUL_FIRE.defaultBlockState());
                }
                // Jochbogen
                this.path(new double[] {sx * 5, 3, -2.5}, new double[] {sx * 4, 2, 1}, new double[] {sx * 3, 1.5, 3});
                // Seelenfeuer drinnen
                this.soulFire(sx * 2, 0, -2);
            }
            this.soulFire(0, 0, -5);
            // die Hörner: an der Wurzel doppelt, dann weit ausladend und nach vorn gebogen (oder steil nach hinten)
            for (int sx : new int[] {-1, 1}) {
                double[][] horn = hornStyle == 0
                        ? new double[][] {{sx * 2.5, 6.5, -3.5}, {sx * 4.5, 7.6, -4.2}, {sx * 6.5, 8.6, -4.0}, {sx * 8.0, 9.8, -2.6},
                                {sx * 8.6, 11.0, -0.8}, {sx * 8.4, 11.6, 0.8}}
                        : new double[][] {{sx * 2.5, 6.5, -4.0}, {sx * 3.8, 8.2, -5.2}, {sx * 4.4, 9.8, -7.0}, {sx * 4.2, 10.8, -9.0},
                                {sx * 3.6, 10.8, -10.6}};
                this.path(horn);
                this.path(new double[] {horn[0][0], horn[0][1] - 1, horn[0][2]}, new double[] {horn[1][0], horn[1][1] - 1, horn[1][2]},
                        new double[] {horn[2][0], horn[2][1] - 0.6, horn[2][2]});
            }
            this.ghastSpots.add(new int[] {0, -3});
            this.ghastSpots.add(new int[] {1, -6});
        }

        // =====================================================================================
        // Die Riesenschlange
        // =====================================================================================

        void serpent() {
            double half = 10 + this.random.nextInt(3);
            double amp = 2.4 + this.random.nextDouble() * 1.2;
            double wave = 14 + this.random.nextDouble() * 4;
            double hill = 11 + this.random.nextDouble() * 3;
            double rise = 3.0 + this.random.nextDouble() * 0.6;
            // Hals und Kopf liegen vorn bei z = half auf dem Boden in der Mitte
            double phase = -2.0 * Math.PI * half / wave;
            double vphase = 2.544 - 2.0 * Math.PI * half / hill;
            double[][] spine = new double[(int) (2 * half) + 1][];
            for (int i = 0; i < spine.length; i++) {
                double z = -half + i;
                double x = amp * Math.sin(2.0 * Math.PI * z / wave + phase) * (0.5 + 0.5 * (i / (double) spine.length));
                double y = rise * Math.sin(2.0 * Math.PI * z / hill + vphase) - 0.6;
                if (i < 4) {
                    y -= (4 - i) * 0.6;                     // die Schwanzspitze verschwindet im Sand
                }
                spine[i] = new double[] {x, y, z};
            }
            this.path(spine);
            for (int i = 2; i < spine.length - 1; i++) {
                int x = round(spine[i][0]);
                int y = round(spine[i][1]);
                int z = round(spine[i][2]);
                if (y < 0 || i % 2 != 0) {
                    continue;
                }
                // Dornfortsatz und kurze Rippen, zum Schwanz hin kleiner
                this.put(x, y + 1, z, Direction.Axis.Y);
                if (i > spine.length / 3) {
                    for (int sx : new int[] {-1, 1}) {
                        this.put(x + sx, y, z, Direction.Axis.X);
                        if (y >= 1) {
                            this.put(x + sx * 2, y, z, Direction.Axis.X);
                            this.put(x + sx * 2, y - 1, z, Direction.Axis.Y);
                        }
                    }
                }
                if (y >= 2) {
                    this.ghastSpots.add(new int[] {x + (i % 4 == 0 ? 2 : -2), z});
                }
            }
            // der Kopf: lang und flach, hinten die Hirnschale mit den Augenhöhlen, das Maul offen, darin Seelenfeuer
            int z0 = (int) half + 1;
            for (int z = z0; z <= z0 + 4; z++) {
                for (int x = -1; x <= 1; x++) {
                    this.put(x, 2, z, Direction.Axis.Z);                     // Oberkiefer
                    if (Math.abs(x) == 1 || z == z0 + 4) {
                        this.put(x, 0, z, Direction.Axis.Z);                 // Unterkiefer
                    }
                    if (z <= z0 + 1) {
                        this.put(x, 3, z, Direction.Axis.Z);                 // Hirnschale
                        if (Math.abs(x) == 1) {
                            this.put(x, 1, z, Direction.Axis.Y);             // Kiefergelenk
                        }
                    }
                }
            }
            for (int sx : new int[] {-1, 1}) {
                this.b.set(sx, 3, z0 + 1, Blocks.AIR.defaultBlockState());   // Augenhöhle
                this.put(sx * 2, 2, z0, Direction.Axis.Z);                   // Wangenknochen
                this.put(sx, 1, z0 + 3, Direction.Axis.Y);                   // Fangzahn
                this.path(new double[] {sx, 4, z0}, new double[] {sx * 1.6, 4.8, z0 - 1.2}, new double[] {sx * 1.9, 5.2, z0 - 2.6});
            }
            this.soulFire(0, 1, z0 + 2);
        }

        // =====================================================================================
        // Die Knochenhand
        // =====================================================================================

        /**
         * Eine Knochenhand, die aus dem Sand greift: Elle und Speiche schräg aus dem Boden, die Handwurzel, vier weit
         * gespreizte Finger aus drei Gliedern mit gekrümmten Krallen und ein abgespreizter Daumen.
         */
        void claw() {
            double reach = 0.95 + this.random.nextDouble() * 0.2;
            boolean curled = this.random.nextBoolean();
            // Elle und Speiche
            this.path(new double[] {-1.6, -4, -6.0}, new double[] {-0.9, 3.5, -1.0});
            this.path(new double[] {1.6, -4, -6.5}, new double[] {0.9, 3.5, -1.2});
            // die Handwurzel
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 0; z++) {
                    this.put(x, 4, z, Direction.Axis.X);
                }
            }
            // vier Finger: Mittelhand, drei Glieder, die Kralle nach unten gekrümmt; die mittleren länger
            double[] fingers = {-4.6, -1.6, 1.6, 4.6};
            for (int i = 0; i < fingers.length; i++) {
                double fx = fingers[i];
                double lift = i == 1 || i == 2 ? 1.0 : 0.0;
                double[] knuckle = {fx, 6, 2};
                double[] middle = {fx * 1.1, 8.5 + lift, 3 * reach};
                double[] upper = {fx * 1.15, 10 + lift, (5 + lift) * reach};
                double[] tip = curled ? new double[] {fx * 1.2, 8.6 + lift, (7 + lift) * reach}
                        : new double[] {fx * 1.2, 10.4 + lift, (7.4 + lift) * reach};
                this.path(new double[] {fx * 0.3, 4.5, 0}, knuckle, middle, upper, tip);
            }
            // der Daumen
            this.path(new double[] {-1.4, 4, -0.5}, new double[] {-4.5, 4.5, -1}, new double[] {-6.5, 5.5, 0}, new double[] {-7.2, 5.2, 1.6});
            // der Boden, aus dem sie greift, ist aufgewühlt
            for (int x = -4; x <= 4; x++) {
                for (int z = -9; z <= -3; z++) {
                    if (x * x + (z + 6) * (z + 6) <= 10 && this.b.chance(x, -1, z, 0.6f)) {
                        this.b.set(x, -1, z, Blocks.SOUL_SOIL.defaultBlockState());
                    }
                }
            }
            this.ghastSpots.add(new int[] {0, 4});
            this.ghastSpots.add(new int[] {-3, 5});
            this.ghastSpots.add(new int[] {4, -2});
        }

        // =====================================================================================
        // Ringsum
        // =====================================================================================

        /** Ein paar verstreute Knochen ringsum, halb im Sand. */
        void fragments() {
            int n = 3 + this.random.nextInt(3);
            for (int i = 0; i < n; i++) {
                double a = this.random.nextDouble() * Math.PI * 2;
                double d = 6.5 + this.random.nextDouble() * 2.5;
                int x = round(Math.cos(a) * d * 0.9);
                int z = round(Math.sin(a) * d);
                int len = 1 + this.random.nextInt(2);
                boolean alongX = this.random.nextBoolean();
                int y = this.random.nextFloat() < 0.4f ? -1 : 0;
                for (int k = 0; k < len; k++) {
                    int px = alongX ? x + k : x;
                    int pz = alongX ? z : z + k;
                    if (Math.abs(px) <= REACH_X && Math.abs(pz) <= REACH_Z) {
                        this.put(px, this.groundAt(px, pz, y), pz, alongX ? Direction.Axis.X : Direction.Axis.Z);
                    }
                }
            }
        }

        /** Die Höhe für einen Knochen, der auf dem Boden liegt ({@code dy} = 0) oder halb darin (−1). */
        private int groundAt(int x, int z, int dy) {
            for (int y = 3; y >= -4; y--) {
                BlockState here = this.b.get(x, y, z);
                BlockState below = this.b.get(x, y - 1, z);
                if ((here.isAir() || here.canBeReplaced() && here.getFluidState().isEmpty()) && below.isSolidRender()) {
                    return y + dy;
                }
            }
            return dy;
        }

        /** Unter den Gebeinen ist der Sand dunkler: hier und da Seelenerde. */
        void soil() {
            for (int x = -REACH_X + 2; x <= REACH_X - 2; x++) {
                for (int z = -REACH_Z + 4; z <= REACH_Z - 4; z++) {
                    if (this.b.noise(x, -1, z) < 0.18f && this.b.get(x, -1, z).is(Blocks.SOUL_SAND)) {
                        this.b.set(x, -1, z, Blocks.SOUL_SOIL.defaultBlockState());
                    }
                }
            }
        }
    }
}
