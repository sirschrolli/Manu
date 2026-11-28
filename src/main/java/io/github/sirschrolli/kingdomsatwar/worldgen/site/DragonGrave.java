package io.github.sirschrolli.kingdomsatwar.worldgen.site;

import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.VillageBuilder;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EndRodBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * Das Drachengrab (seit 2.0): das Gerippe eines riesigen Drachen, über vierzig Blöcke lang, auf einer Ebene aus Endstein
 * gestürzt. Die Wirbelsäule mit ihren Dornen windet sich vom Schwanz über den Brustkorb zum Hals, die Rippen wölben sich
 * wie ein Gewölbe, die Flügelknochen liegen ausgebreitet – einer gebrochen – und zwischen ihren Fingern faulen Fetzen der
 * Flughaut. Der hohle Schädel trägt zurückgebogene Hörner, in den Augenhöhlen glimmt weinender Obsidian, aus dem Oberkiefer
 * ragen Zähne aus Endstäben. Ringsum stecken Splitter aus Obsidian im Boden; neben dem Schädel ein kleiner Schrein mit der
 * Truhe. Leermilben kriechen über die Knochen.
 *
 * <p>Eigene Koordinaten: y = 0 ist der Boden (Fußboden y = -1); der Kopf liegt vorn (+z), der Schwanz hinten.
 */
final class DragonGrave {
    static final BlockPos CHEST = new BlockPos(10, 1, 12);
    static final double PAD = 21.5;

    private DragonGrave() {}

    static void register() {
        Wonders.add(new Wonders.Wonder(SiteKind.DRAGON_GRAVE, "drachengrab", "Dragon Grave", "Drachengrab", 2, Wonders.Ground.LAND, false,
                DragonGrave::plan, DragonGrave::build, origin -> piece(origin, Rotation.NONE, 0x7A41L),
                new double[][] {Wonders.view(-22, 14, 22, 0, 2, 0), Wonders.view(5, 4, 27, 0, 3, 14), Wonders.view(24, 9, -14, 0, 3, -2),
                        Wonders.view(0.5, 1.6, -1, 0, 4, 6), Wonders.view(14, 2.6, 16, 10, 1, 12)}));
    }

    static Optional<Structure.GenerationStub> plan(Structure.GenerationContext context, SiteTerrain terrain, long seed) {
        BlockPos origin = Kit.land(context, terrain, 16, 6);
        return origin == null ? Optional.empty() : Kit.stub(piece(origin, Kit.rotation(seed), seed));
    }

    static SitePiece piece(BlockPos origin, Rotation rotation, long seed) {
        int r = (int) PAD + 5;
        return new SitePiece(SiteKind.DRAGON_GRAVE, 0, origin, rotation, seed, new int[0], SitePiece.box(origin, rotation, -r, -6, -r, r, 16, r));
    }

    static void build(VillageBuilder b, SitePiece piece) {
        new Skeleton(b).build();
    }

    private static final class Skeleton {
        private final VillageBuilder b;
        /** Wo schon Knochen liegen (damit sich Knochen nicht gegenseitig umdrehen). */
        private final Set<Long> bones = new HashSet<>();

        Skeleton(VillageBuilder b) {
            this.b = b;
        }

        void build() {
            Kit.pad(this.b, PAD, Kit.s(Blocks.END_STONE), Kit.s(Blocks.END_STONE), 14);
            this.scorch();
            this.spine();
            this.ribs();
            this.legs();
            this.wings();
            this.skull();
            this.shards();
            this.shrine();
            this.scatter();
            Kit.mob(this.b, ModEntities.ENDERMITE.get(), 0, 0, -2, Direction.SOUTH);
            Kit.mob(this.b, ModEntities.ENDERMITE.get(), -5, 0, -14, Direction.EAST);
            Kit.mob(this.b, ModEntities.ENDERMITE.get(), 3, 0, 15, Direction.WEST);
            Kit.mob(this.b, ModEntities.ENDERMAN.get(), -12, 0, 6, Direction.EAST);
            Kit.mob(this.b, ModEntities.RIFT_HOUND.get(), 13, 0, 9, Direction.WEST);
        }

        private BlockState boneBlock(Direction.Axis axis) {
            return Blocks.BONE_BLOCK.defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis);
        }

        private void put(int x, int y, int z, Direction.Axis axis) {
            long key = BlockPos.asLong(x, y, z);
            if (this.bones.add(key)) {
                this.b.set(x, y, z, this.boneBlock(axis));
            }
        }

        /** Ein Knochen von a nach b: Knochenblöcke entlang der Strecke, nach ihrer Hauptrichtung ausgerichtet. */
        private void bone(double x0, double y0, double z0, double x1, double y1, double z1) {
            double dx = x1 - x0;
            double dy = y1 - y0;
            double dz = z1 - z0;
            double ax = Math.abs(dx);
            double ay = Math.abs(dy);
            double az = Math.abs(dz);
            Direction.Axis axis = ay >= ax && ay >= az ? Direction.Axis.Y : ax >= az ? Direction.Axis.X : Direction.Axis.Z;
            int n = Math.max(1, (int) Math.ceil(Math.max(ax, Math.max(ay, az)) * 1.5));
            for (int i = 0; i <= n; i++) {
                double t = (double) i / n;
                this.put((int) Math.round(x0 + dx * t), (int) Math.round(y0 + dy * t), (int) Math.round(z0 + dz * t), axis);
            }
        }

        /** Eine Kette von Knochen durch die Punkte {x, y, z}. */
        private void chain(double[][] points) {
            for (int i = 0; i + 1 < points.length; i++) {
                this.bone(points[i][0], points[i][1], points[i][2], points[i + 1][0], points[i + 1][1], points[i + 1][2]);
            }
        }

        /** Die Wirbelsäule vom Schwanz bis zum Schädel, alle zwei Blöcke ein Wirbel mit Fortsätzen und einem Rückendorn. */
        private void spine() {
            double[][] path = {{0, 0, -19}, {-2, 0, -16.4}, {-3.4, 0, -13}, {-3, 1, -10}, {-1.5, 3, -7}, {0, 5, -4}, {0, 5.5, 0}, {0, 5, 4}, {0, 7, 7},
                    {0, 7, 9}, {0, 5, 11}};
            this.chain(path);
            // die Wirbel: Querfortsätze und Dornen, zum Schwanz hin kleiner, über den Schultern am größten
            double walked = 0;
            double next = 1;
            for (int i = 0; i + 1 < path.length; i++) {
                double[] a = path[i];
                double[] c = path[i + 1];
                double len = Math.sqrt(Math.pow(c[0] - a[0], 2) + Math.pow(c[1] - a[1], 2) + Math.pow(c[2] - a[2], 2));
                while (next <= walked + len) {
                    double t = (next - walked) / len;
                    int x = (int) Math.round(a[0] + (c[0] - a[0]) * t);
                    int y = (int) Math.round(a[1] + (c[1] - a[1]) * t);
                    int z = (int) Math.round(a[2] + (c[2] - a[2]) * t);
                    boolean tail = z < -9;
                    boolean neck = z > 5;
                    if (!tail) {
                        this.put(x - 1, y, z, Direction.Axis.X);
                        this.put(x + 1, y, z, Direction.Axis.X);
                    }
                    int spike = tail ? 1 : neck ? 1 : z > -4 && z < 4 ? 3 : 2;
                    for (int k = 1; k <= spike; k++) {
                        this.put(x, y + k, z, Direction.Axis.Y);
                    }
                    next += 2.2;
                }
                walked += len;
            }
        }

        /** Der Brustkorb: Rippen in weiten Bögen von der Wirbelsäule zum Boden, an den Enden Spitzen aus Endstäben; einige gebrochen. */
        private void ribs() {
            int[] rows = {-5, -3, -1, 1, 3};
            for (int rz : rows) {
                for (int side = -1; side <= 1; side += 2) {
                    boolean broken = side < 0 && rz == 1 || side > 0 && rz == -5;
                    int steps = broken ? 16 : 30;
                    double[][] arc = new double[steps + 1][];
                    for (int i = 0; i <= steps; i++) {
                        double a = Math.toRadians(i * 3.0);
                        arc[i] = new double[] {side * (1.0 + 5.4 * Math.sin(a)), 4.6 * Math.cos(a), rz};
                    }
                    this.chain(arc);
                    if (broken) {
                        // das abgebrochene Ende liegt am Boden
                        this.bone(side * 6.0, 0, rz + 1, side * 7.0, 0, rz + 3);
                    } else {
                        // die Spitze der Rippe steckt im Boden
                        this.b.set(side * 7, 0, rz, Blocks.END_ROD.defaultBlockState().setValue(EndRodBlock.FACING, side > 0 ? Direction.EAST : Direction.WEST));
                    }
                }
            }
        }

        /** Unter dem Gerippe ist der Boden verglast: Obsidian und weinender Obsidian, zum Rand hin wieder Endstein. */
        private void scorch() {
            for (int x = -16; x <= 16; x++) {
                for (int z = -24; z <= 24; z++) {
                    double d = sq(x / 12.5) + sq((z - 1.0) / 22.0) + (Kit.smooth(this.b, x, z, 4.0, 80) - 0.5) * 0.6;
                    if (d > 1.0) {
                        continue;
                    }
                    double n = Kit.smooth(this.b, x, z, 2.6, 81);
                    if (n < 0.78 - d * 0.45) {
                        this.b.set(x, -1, z, n < 0.2 && this.b.chance(x, 82, z, 0.35f) ? Kit.s(Blocks.CRYING_OBSIDIAN) : Kit.s(Blocks.OBSIDIAN));
                    }
                }
            }
        }

        /** Die Beine, eingeknickt unter dem Leib, die Zehen gespreizt. */
        private void legs() {
            for (int side = -1; side <= 1; side += 2) {
                // vorn
                this.chain(new double[][] {{side * 2, 4, 4}, {side * 5, 2, 6}, {side * 5, 0, 9}});
                this.bone(side * 5, 0, 9, side * 4, 0, 12);
                this.bone(side * 5, 0, 9, side * 6.5, 0, 11.5);
                // hinten
                this.chain(new double[][] {{side * 2 - 1.5, 3, -7}, {side * 5.5 - 1.5, 2, -4.5}, {side * 5.5 - 1.5, 0, -9}});
                this.bone(side * 5.5 - 1.5, 0, -9, side * 4.5 - 1.5, 0, -12);
                this.bone(side * 5.5 - 1.5, 0, -9, side * 7.5 - 1.5, 0, -11);
            }
        }

        /** Die Flügel: rechts ausgebreitet mit drei Fingern, links gebrochen; dazwischen Fetzen der Flughaut am Boden. */
        private void wings() {
            // rechts: Oberarm, Unterarm, Daumenkralle, drei Finger bis zum Boden
            this.chain(new double[][] {{2, 5, 3}, {7, 8, 1}, {13, 6, -3}});
            this.bone(13, 6, -3, 14, 8, -1);
            double[][] tips = {{16, 0, -12}, {12, 0, -15}, {7, 0, -12}};
            for (double[] tip : tips) {
                this.bone(13, 6, -3, tip[0], tip[1], tip[2]);
            }
            this.membrane(new int[][] {{13, -3}, {16, -12}, {12, -15}});
            this.membrane(new int[][] {{13, -3}, {12, -15}, {7, -12}});
            this.membrane(new int[][] {{3, 1}, {13, -3}, {7, -12}});
            // links: der Unterarm abgebrochen, die Finger liegen verstreut am Boden
            this.chain(new double[][] {{-2, 5, 3}, {-7, 8, 1}, {-10.5, 6.5, -1.5}});
            this.bone(-10, 0, -5, -14, 0, -12);
            this.bone(-8, 0, -8, -10, 0, -15);
            this.bone(-12, 0, -3, -13, 2, -1);
            this.membrane(new int[][] {{-6, -2}, {-14, -12}, {-10, -15}});
        }

        /** Fetzen der Flughaut: Teppiche in Violett, Schwarz und Grau, löchrig, auf dem Boden im Dreieck zwischen den Knochen. */
        private void membrane(int[][] tri) {
            int minX = Math.min(tri[0][0], Math.min(tri[1][0], tri[2][0]));
            int maxX = Math.max(tri[0][0], Math.max(tri[1][0], tri[2][0]));
            int minZ = Math.min(tri[0][1], Math.min(tri[1][1], tri[2][1]));
            int maxZ = Math.max(tri[0][1], Math.max(tri[1][1], tri[2][1]));
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (!inTriangle(x, z, tri) || !this.b.get(x, 0, z).isAir() || !this.b.chance(x, 61, z, 0.55f)) {
                        continue;
                    }
                    float n = this.b.noise(x * 2, 62, z * 2);
                    DyeColor color = n < 0.4f ? DyeColor.PURPLE : n < 0.75f ? DyeColor.BLACK : DyeColor.GRAY;
                    this.b.set(x, 0, z, Kit.s(Blocks.CARPET.pick(color)));
                }
            }
        }

        private static boolean inTriangle(int x, int z, int[][] t) {
            double d1 = sign(x, z, t[0], t[1]);
            double d2 = sign(x, z, t[1], t[2]);
            double d3 = sign(x, z, t[2], t[0]);
            boolean neg = d1 < 0 || d2 < 0 || d3 < 0;
            boolean pos = d1 > 0 || d2 > 0 || d3 > 0;
            return !(neg && pos);
        }

        private static double sign(int x, int z, int[] a, int[] c) {
            return (x - c[0]) * (double) (a[1] - c[1]) - (a[0] - c[0]) * (double) (z - c[1]);
        }

        /**
         * Der Schädel: ein hohler Hirnschädel mit gerundeten Kanten, davor die lange, zur Spitze hin flachere Schnauze; seitlich
         * die Augenhöhlen (dahinter glimmt weinender Obsidian), oben zwei Paar zurückgebogene Hörner, Zähne aus Endstäben und
         * der Unterkiefer am Boden.
         */
        private void skull() {
            // der Hirnschädel: x -3..3, y 1..5, z 11..15, ohne die Kanten
            for (int x = -3; x <= 3; x++) {
                for (int y = 1; y <= 5; y++) {
                    for (int z = 11; z <= 15; z++) {
                        int edges = (Math.abs(x) == 3 ? 1 : 0) + (y == 1 || y == 5 ? 1 : 0) + (z == 11 || z == 15 ? 1 : 0);
                        boolean shell = edges >= 1;
                        if (shell && edges < 2) {
                            this.put(x, y, z, y == 1 || y == 5 ? Direction.Axis.Y : Math.abs(x) == 3 ? Direction.Axis.X : Direction.Axis.Z);
                        }
                    }
                }
            }
            // die Schnauze: x -2..2, unten offen (das Maul), oben zur Spitze hin flacher
            for (int z = 15; z <= 21; z++) {
                int top = z <= 18 ? 4 : 3;
                for (int x = -2; x <= 2; x++) {
                    for (int y = 2; y <= top; y++) {
                        int edges = (Math.abs(x) == 2 ? 1 : 0) + (y == top ? 1 : 0) + (z == 21 ? 1 : 0);
                        boolean bottom = y == 2 && Math.abs(x) <= 1 && z >= 16;
                        if (edges == 1 || bottom && edges == 0) {
                            this.put(x, y, z, y == top ? Direction.Axis.Y : Math.abs(x) == 2 ? Direction.Axis.X : Direction.Axis.Z);
                        }
                    }
                }
            }
            for (int side = -1; side <= 1; side += 2) {
                // die Augenhöhlen, dahinter weinender Obsidian
                this.b.set(side * 3, 4, 13, Kit.s(Blocks.AIR));
                this.b.set(side * 3, 3, 13, Kit.s(Blocks.AIR));
                this.b.set(side * 2, 3, 13, Kit.s(Blocks.CRYING_OBSIDIAN));
                // Brauenwülste über den Augen
                this.b.set(side * 3, 5, 13, VillageBuilder.stairs(Blocks.END_STONE_BRICK_STAIRS, side > 0 ? Direction.WEST : Direction.EAST, Half.BOTTOM));
                // Nasenlöcher an der Spitze
                this.b.set(side, 3, 21, Kit.s(Blocks.AIR));
                // die Hörner: ein langes und ein kurzes je Seite, nach hinten gebogen
                this.chain(new double[][] {{side * 2, 6, 12}, {side * 3, 7.4, 10}, {side * 3.6, 8.4, 7.6}, {side * 3.6, 8.6, 5.6}});
                this.chain(new double[][] {{side * 3, 4, 11}, {side * 4.4, 4.6, 9.4}, {side * 4.6, 5.4, 7.6}});
                // Zähne aus Endstäben am Oberkiefer, dazwischen die des Unterkiefers
                for (int z : new int[] {16, 20}) {
                    this.b.set(side * 2, 1, z, Blocks.END_ROD.defaultBlockState().setValue(EndRodBlock.FACING, Direction.DOWN));
                }
                // der Unterkiefer am Boden, leicht geöffnet
                this.chain(new double[][] {{side * 3, 0, 12}, {side * 2.6, 0, 16}, {side * 1.4, 0, 21}});
                this.b.set(side * 2, 1, 18, Blocks.END_ROD.defaultBlockState().setValue(EndRodBlock.FACING, Direction.UP));
            }
            this.put(0, 0, 21, Direction.Axis.X);
            this.put(0, 0, 22, Direction.Axis.X);
        }

        private static double sq(double v) {
            return v * v;
        }

        /** Splitter aus Obsidian, schräg im Boden, an den Spitzen weinend. */
        private void shards() {
            int[][] spikes = {{-12, 11, 7, 1, -1}, {15, -17, 9, -1, 1}, {-15, -6, 6, 1, 0}, {7, -21, 8, 0, 1}, {-7, -20, 5, 1, 1}, {16, 6, 6, -1, 0}};
            for (int[] s : spikes) {
                int h = s[2];
                for (int y = -2; y <= h; y++) {
                    double t = Math.max(0, y) / (double) h;
                    double r = 1.5 * (1 - t) + 0.35;
                    double cx = s[0] + s[3] * 2.2 * t;
                    double cz = s[1] + s[4] * 2.2 * t;
                    for (int x = (int) Math.floor(cx - r); x <= (int) Math.ceil(cx + r); x++) {
                        for (int z = (int) Math.floor(cz - r); z <= (int) Math.ceil(cz + r); z++) {
                            if (sq(x - cx) + sq(z - cz) <= r * r) {
                                this.b.set(x, y, z, t > 0.7 && this.b.chance(x, y, z, 0.6f) ? Kit.s(Blocks.CRYING_OBSIDIAN) : Kit.s(Blocks.OBSIDIAN));
                            }
                        }
                    }
                }
            }
        }

        /** Der Schrein neben dem Schädel: ein Podest aus Endsteinziegeln, Purpursäulen mit Endstäben, Kerzen, die Truhe. */
        private void shrine() {
            int cx = CHEST.getX();
            int cz = CHEST.getZ();
            for (int x = cx - 2; x <= cx + 2; x++) {
                for (int z = cz - 2; z <= cz + 2; z++) {
                    this.b.set(x, -1, z, Kit.s(Blocks.END_STONE_BRICKS));
                    boolean edge = Math.abs(x - cx) == 2 || Math.abs(z - cz) == 2;
                    if (!edge) {
                        this.b.set(x, 0, z, Kit.s(Blocks.PURPUR_BLOCK));
                    }
                }
            }
            for (int[] d : new int[][] {{-2, 0, 1, 0}, {2, 0, -1, 0}, {0, -2, 0, 1}, {0, 2, 0, -1}}) {
                // Stufen hinauf zum Podest (an den Seitenmitten)
                Direction up = d[2] > 0 ? Direction.EAST : d[2] < 0 ? Direction.WEST : d[3] > 0 ? Direction.SOUTH : Direction.NORTH;
                this.b.set(cx + d[0], 0, cz + d[1], VillageBuilder.stairs(Blocks.PURPUR_STAIRS, up, Half.BOTTOM));
            }
            for (int[] c : new int[][] {{-2, -2}, {2, -2}, {-2, 2}, {2, 2}}) {
                for (int y = 0; y <= 2; y++) {
                    this.b.set(cx + c[0], y, cz + c[1], Blocks.PURPUR_PILLAR.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y));
                }
                this.b.set(cx + c[0], 3, cz + c[1], Blocks.END_ROD.defaultBlockState().setValue(EndRodBlock.FACING, Direction.UP));
            }
            Kit.chest(this.b, cx, CHEST.getY(), cz, Direction.WEST, Kit.loot("dragon_grave"));
            Kit.candles(this.b, cx - 1, 1, cz - 1, Blocks.DYED_CANDLE.pick(DyeColor.PURPLE), 3, true);
            Kit.candles(this.b, cx - 1, 1, cz + 1, Blocks.DYED_CANDLE.pick(DyeColor.MAGENTA), 2, true);
            Kit.candles(this.b, cx + 1, 1, cz + 1, Blocks.DYED_CANDLE.pick(DyeColor.PURPLE), 1, true);
            this.b.set(cx + 1, 1, cz - 1, Kit.s(Blocks.SKELETON_SKULL));
        }

        /** Verstreute Knochen auf der Ebene. */
        private void scatter() {
            int[][] spots = {{-9, 14}, {6, -17}, {-14, 3}, {11, 4}, {-3, -15}, {4, 18}, {-17, -12}, {17, -4}};
            for (int[] s : spots) {
                if (this.b.get(s[0], 0, s[1]).isAir()) {
                    Direction.Axis axis = this.b.chance(s[0], 70, s[1], 0.5f) ? Direction.Axis.X : Direction.Axis.Z;
                    this.put(s[0], 0, s[1], axis);
                    if (this.b.chance(s[0], 71, s[1], 0.5f)) {
                        this.put(s[0] + (axis == Direction.Axis.X ? 1 : 0), 0, s[1] + (axis == Direction.Axis.Z ? 1 : 0), axis);
                    }
                }
            }
        }
    }
}
