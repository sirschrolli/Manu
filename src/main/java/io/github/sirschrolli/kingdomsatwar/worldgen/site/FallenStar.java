package io.github.sirschrolli.kingdomsatwar.worldgen.site;

import io.github.sirschrolli.kingdomsatwar.loot.ModLootTables;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.VillageBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.world.level.block.EndRodBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.LightningRodBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * Der Sternensturz: Vor langer Zeit fiel ein Stück des Endes vom Himmel. Es schlug einen weiten Krater, durchbrach den
 * Fels und blieb tief unten liegen – in seinem Kern stecken die zwölf Rahmen des Tors ins Ende. Sterndeuter haben einen
 * hölzernen Wendelsteg an der Wand des Schachts hinab gebaut, oben im Krater ihr Lager mit Fernrohr aufgeschlagen und
 * unten neben dem Tor eine Schreibstube eingerichtet. Enderaugen ziehen zum Stern zurück und weisen so den Weg.
 *
 * <p>Auf festem Land ist es ein Krater mit Wall, Auswurf und Bruchstücken des Sterns; wo Wasser in der Nähe ist (oder das
 * Land zu steil), ragt stattdessen der Sternfels auf: ein zerklüfteter Turm aus Sternengestein mit dem Schacht in der
 * Mitte, außen eine Leiter.
 *
 * <p>Eigene Koordinaten: Ursprung in der Mitte, y = 0 ist die Höhe des umliegenden Landes (beim Sternfels: seine Krone).
 */
final class FallenStar {
    static final int CRATER = 0;
    static final int ROCK = 1;

    /** Radius des Kraters (Kamm des Walls). */
    static final int R = 20;
    /** Tiefe der Mulde in der Mitte. */
    static final int DEPTH = 8;
    /** So weit reicht das Bauwerk (Wall, Übergang ins Land, Auswurf). */
    static final int REACH = R + 7;
    /** Radius des Schachts; der Steg liegt zwischen INNER und SHAFT, bis RAIL ist Geländer. */
    static final double SHAFT = 6.5;
    static final double INNER = 3.5;
    static final double RAIL = 4.3;
    /** Laufradius des Stegs (für Länge und Gefälle). */
    static final double MID = 5.0;
    /** Radius der Kernkammer. */
    static final double CHAMBER = 12.0;

    private FallenStar() {}

    // =====================================================================================
    // Planung
    // =====================================================================================

    static Optional<Structure.GenerationStub> plan(Structure.GenerationContext context, SiteTerrain terrain, long seed) {
        ChunkPos chunk = context.chunkPos();
        int cx = chunk.getMiddleBlockX();
        int cz = chunk.getMiddleBlockZ();
        RandomSource random = RandomSource.create(seed);
        int sea = terrain.seaLevel();
        // Wasser in der Nähe oder steiles Land: der Sternfels; sonst der Krater
        boolean wet = terrain.water(cx, cz);
        int[] heights = new int[13];
        heights[0] = terrain.ground(cx, cz);
        for (int i = 0; i < 12; i++) {
            double a = i * Math.PI / 6;
            int x = cx + (int) Math.round(Math.cos(a) * (R + 3));
            int z = cz + (int) Math.round(Math.sin(a) * (R + 3));
            wet |= terrain.water(x, z);
            heights[i + 1] = terrain.ground(cx + (int) Math.round(Math.cos(a) * 10), cz + (int) Math.round(Math.sin(a) * 10));
        }
        int[] sorted = heights.clone();
        java.util.Arrays.sort(sorted);
        boolean steep = sorted[12] - sorted[0] > 16;
        int variant = wet || steep ? ROCK : CRATER;
        int y0 = variant == CRATER ? sorted[6] : Math.max(sea, heights[0]) + 9;
        int floor = variant == CRATER ? -30 : Math.min(heights[0] - 18, y0 - 30) - y0;
        if (y0 + floor < terrain.minY() + 8) {
            floor = terrain.minY() + 8 - y0;
        }
        int a0 = random.nextInt(6283);
        int campAxis = random.nextInt(4);
        BlockPos origin = new BlockPos(cx, y0, cz);
        BoundingBox box = new BoundingBox(cx - REACH, y0 + floor - 3, cz - REACH, cx + REACH, y0 + 24, cz + REACH);
        int[] data = {variant, floor, a0, sea, campAxis};
        return Optional.of(new Structure.GenerationStub(origin,
                builder -> builder.addPiece(new SitePiece(SiteKind.FALLEN_STAR, 0, origin, Rotation.NONE, seed, data, box))));
    }

    // =====================================================================================
    // Bauen
    // =====================================================================================

    static void build(VillageBuilder b, SitePiece piece) {
        new Star(b, piece).build();
    }

    /** Die Ausrichtung eines Lagers an einer der vier Achsen: eigene z-Achse zeigt zur Mitte des Kraters. */
    static Rotation inward(int axis) {
        return switch (axis & 3) {
            case 1 -> Rotation.CLOCKWISE_90;
            case 2 -> Rotation.CLOCKWISE_180;
            case 3 -> Rotation.COUNTERCLOCKWISE_90;
            default -> Rotation.NONE;
        };
    }

    static int[] axis(int axis) {
        return switch (axis & 3) {
            case 1 -> new int[] {1, 0};
            case 2 -> new int[] {0, 1};
            case 3 -> new int[] {-1, 0};
            default -> new int[] {0, -1};
        };
    }

    static Rotation inverse(Rotation rotation) {
        return switch (rotation) {
            case CLOCKWISE_90 -> Rotation.COUNTERCLOCKWISE_90;
            case COUNTERCLOCKWISE_90 -> Rotation.CLOCKWISE_90;
            default -> rotation;
        };
    }

    private static final class Star {
        private final VillageBuilder b;
        private final SitePiece piece;
        private final int variant;
        /** Boden der Kernkammer (eigene Höhe). */
        private final int floor;
        /** Wo der Steg oben beginnt (Winkel) und wo er unten ankommt. */
        private final double a0;
        private final double tEnd;
        /** Laufhöhe am oberen Ende des Stegs (eigene Höhe, in ganzen Blöcken). */
        private final int start;
        private final int seaLocal;
        private final int campAxis;
        private final BlockPos origin;

        Star(VillageBuilder b, SitePiece piece) {
            this.b = b;
            this.piece = piece;
            this.variant = piece.data(0);
            this.floor = piece.data(1);
            this.a0 = piece.data(2) / 1000.0;
            this.seaLocal = piece.data(3) - piece.origin().getY();
            this.campAxis = piece.data(4);
            this.origin = piece.origin();
            this.start = this.variant == CRATER ? crater(SHAFT, 0.0) + 1 : 1;
            this.tEnd = 2.0 * (this.start - (this.floor + 1)) / MID;
        }

        // ---- Formen -------------------------------------------------------------------------

        /** Höhe der Kratermulde (oberster Block) im Abstand r, ohne Rauheit. */
        static int crater(double r, double rough) {
            double bowl = r <= R ? -DEPTH * (1 - (r / R) * (r / R)) : 0;
            double rim = 2.2 * Math.exp(-((r - R) / 3.0) * ((r - R) / 3.0));
            return (int) Math.round(bowl + rim + rough);
        }

        int craterAt(int x, int z) {
            double r = Math.sqrt(x * x + z * z);
            return crater(r, (this.b.noise(x, 7, z) - 0.5) * 1.2);
        }

        /** Oberster Luftblock der Kernkammer im Abstand r. */
        int dome(double r) {
            return this.floor + 1 + (int) Math.round(9 * Math.sqrt(Math.max(0, 1 - (r / CHAMBER) * (r / CHAMBER))));
        }

        /** Krone des Sternfelsens (eigene Höhe des obersten Blocks). */
        int rockTop(int x, int z, double r) {
            double phi = Math.atan2(z, x);
            if (this.nearStart(phi, 0.36)) {
                return 0;
            }
            double jag = 2.0 * Math.sin(5 * phi + this.a0 * 3) + 1.4 * Math.sin(3 * phi + 1.7) + (this.b.noise(x, 9, z) - 0.5) * 2.4;
            int top = (int) Math.round(jag) - (r > 8.5 ? 1 + (int) (r - 8.5) : 0);
            return r <= 8.5 ? Math.max(top, this.seaLocal + 2) : top;
        }

        /** Umriss des Sternfelsens: unregelmäßig, aber nie so schmal, dass Wasser an den Schacht käme. */
        double edge(double phi) {
            return Math.max(8.7, 10.0 + 1.3 * Math.sin(3 * phi + 0.7 + this.a0) + 0.8 * Math.sin(5 * phi + 2.1));
        }

        boolean nearStart(double phi, double width) {
            double d = Math.abs(Math.IEEEremainder(phi - this.a0, Math.PI * 2));
            return d < width;
        }

        /** Weiche Flecken (für Adern im Gestein): Mittelwert der Zufallswerte an den Ecken eines groben Gitters. */
        float smooth(int x, int y, int z) {
            int gx = Math.floorDiv(x, 5);
            int gy = Math.floorDiv(y, 5);
            int gz = Math.floorDiv(z, 5);
            float fx = (Math.floorMod(x, 5) + 0.5f) / 5f;
            float fy = (Math.floorMod(y, 5) + 0.5f) / 5f;
            float fz = (Math.floorMod(z, 5) + 0.5f) / 5f;
            fx = fx * fx * (3 - 2 * fx);
            fy = fy * fy * (3 - 2 * fy);
            fz = fz * fz * (3 - 2 * fz);
            float v = 0;
            for (int i = 0; i < 8; i++) {
                int dx = i & 1;
                int dy = (i >> 1) & 1;
                int dz = (i >> 2) & 1;
                float w = (dx == 1 ? fx : 1 - fx) * (dy == 1 ? fy : 1 - fy) * (dz == 1 ? fz : 1 - fz);
                v += w * this.b.noise(gx + dx + 4000, gy + dy + 4000, gz + dz + 4000);
            }
            return v * 0.88f + this.b.noise(x, y, z) * 0.12f;
        }

        /** Sternengestein: Endstein mit Adern aus Obsidian, weinendem Obsidian, Amethyst, Tuff und Schwarzstein. */
        BlockState rock(int x, int y, int z) {
            float n = this.smooth(x, y, z);
            if (n < 0.24f) {
                return Blocks.OBSIDIAN.defaultBlockState();
            }
            if (n < 0.28f) {
                return Blocks.CRYING_OBSIDIAN.defaultBlockState();
            }
            if (n < 0.36f) {
                return Blocks.SMOOTH_BASALT.defaultBlockState();
            }
            if (n < 0.66f) {
                return Blocks.END_STONE.defaultBlockState();
            }
            if (n < 0.72f) {
                return Blocks.TUFF.defaultBlockState();
            }
            if (n < 0.78f) {
                return Blocks.END_STONE.defaultBlockState();
            }
            if (n < 0.82f) {
                return Blocks.AMETHYST_BLOCK.defaultBlockState();
            }
            return Blocks.END_STONE.defaultBlockState();
        }

        /**
         * Strahlen aus Auswurf: Wie bei jedem großen Einschlag ziehen helle Bahnen aus Endstein und Kalzit vom Schacht
         * über den dunklen Kraterboden hinaus bis weit ins Land – von oben erkennt man den Sternensturz daran schon von
         * fern. Gibt den Block des Strahls zurück oder {@code null}.
         */
        BlockState ray(int x, int z, double r) {
            if (r < SHAFT + 2.5) {
                return null;
            }
            double phi = Math.atan2(z, x);
            for (int i = 0; i < 9; i++) {
                double theta = this.a0 * 3.1 + i * (Math.PI * 2 / 9) + (this.b.noise(i, 31, 7) - 0.5) * 0.5;
                double width = (0.8 + this.b.noise(i, 32, 7) * 1.1 + r * 0.02) / r;
                double d = Math.abs(Math.IEEEremainder(phi - theta, Math.PI * 2));
                if (d > width) {
                    continue;
                }
                double reach = R + 4 + this.b.noise(i, 33, 7) * 10;
                if (r > reach) {
                    continue;
                }
                double p = r <= R ? 0.7 : 0.7 * (reach - r) / (reach - R);
                if (d > width * 0.6) {
                    p *= 0.5;
                }
                if (this.b.chance(x, 21, z, (float) p)) {
                    return this.b.chance(x, 22, z, 0.3f) ? Blocks.CALCITE.defaultBlockState() : Blocks.END_STONE.defaultBlockState();
                }
            }
            return null;
        }

        /** Boden der Kratermulde: verglaster, verschlackter Fels nahe der Mitte, Geröll und Tuff weiter außen. */
        BlockState craterSurface(int x, int z, double r) {
            float n = this.smooth(x, 3, z);
            float f = this.b.noise(x, 11, z);
            if (r < 11) {
                if (f < 0.03f) {
                    return Blocks.MAGMA_BLOCK.defaultBlockState();
                }
                if (f < 0.07f) {
                    return Blocks.CRYING_OBSIDIAN.defaultBlockState();
                }
                if (n < 0.35f) {
                    return Blocks.SMOOTH_BASALT.defaultBlockState();
                }
                if (n < 0.55f) {
                    return Blocks.BLACKSTONE.defaultBlockState();
                }
                if (n < 0.68f) {
                    return Blocks.OBSIDIAN.defaultBlockState();
                }
                return Blocks.TUFF.defaultBlockState();
            }
            if (f < 0.04f) {
                return Blocks.END_STONE.defaultBlockState();
            }
            if (n < 0.33f) {
                return Blocks.TUFF.defaultBlockState();
            }
            if (n < 0.58f) {
                return Blocks.GRAVEL.defaultBlockState();
            }
            if (n < 0.72f) {
                return Blocks.ANDESITE.defaultBlockState();
            }
            if (n < 0.84f) {
                return Blocks.SMOOTH_BASALT.defaultBlockState();
            }
            return Blocks.COBBLED_DEEPSLATE.defaultBlockState();
        }

        // ---- Ablauf -------------------------------------------------------------------------

        void build() {
            BoundingBox box = this.b.chunkBox;
            for (int wx = box.minX(); wx <= box.maxX(); wx++) {
                for (int wz = box.minZ(); wz <= box.maxZ(); wz++) {
                    int x = wx - this.origin.getX();
                    int z = wz - this.origin.getZ();
                    if (Math.abs(x) > REACH || Math.abs(z) > REACH) {
                        continue;
                    }
                    double r = Math.sqrt(x * x + z * z);
                    if (r > REACH) {
                        continue;
                    }
                    int top = this.variant == CRATER ? this.craterColumn(x, z, r, wx, wz) : this.rockColumn(x, z, r, wx, wz);
                    this.chamberColumn(x, z, r);
                    this.shaftColumn(x, z, r, top);
                    this.sealColumn(x, z, r, top);
                }
            }
            this.walkway();
            this.dais();
            this.study();
            this.chamberDetails();
            if (this.variant == CRATER) {
                this.spikes();
                this.boulders();
                this.camp();
            } else {
                this.ladder();
                this.landing();
            }
            this.rimFence();
        }

        /** Eine Säule des Kraters: Mulde ausheben oder aufschütten, Wall, Übergang ins umliegende Land. */
        private int craterColumn(int x, int z, double r, int wx, int wz) {
            int ground = this.b.level.getHeight(Heightmap.Types.OCEAN_FLOOR, wx, wz) - 1 - this.origin.getY();
            int water = this.b.level.getHeight(Heightmap.Types.WORLD_SURFACE, wx, wz) - 1 - this.origin.getY();
            int craterTop = this.craterAt(x, z);
            int[] camp = this.campLocal(x, z);
            if (camp != null && Math.abs(camp[0]) <= 5 && camp[1] >= -4 && camp[1] <= 4) {
                // die Terrasse des Lagers
                craterTop = crater(12, 0.0);
            } else if (r <= SHAFT + 3 && this.nearStart(Math.atan2(z, x), 0.55)) {
                // eben dort, wo der Steg beginnt
                craterTop = crater(SHAFT, 0.0);
            }
            int top;
            if (r <= R + 1) {
                top = craterTop;
            } else {
                double w = (r - R - 1) / 6.0;
                top = (int) Math.round(craterTop * (1 - w) + ground * w);
            }
            BlockState natural = this.b.get(x, ground, z);
            for (int y = top + 1; y <= Math.max(ground, water) + 1; y++) {
                this.b.set(x, y, z, Blocks.AIR.defaultBlockState());
            }
            for (int y = ground + 1; y <= top; y++) {
                this.b.set(x, y, z, y >= top - 2 ? Blocks.DIRT.defaultBlockState() : Blocks.STONE.defaultBlockState());
            }
            if (r <= SHAFT + 2) {
                // der Rand des Schachts: verschmolzenes Sternengestein
                this.b.set(x, top, z, this.rock(x, top, z));
            } else if (r <= R + 2) {
                BlockState ray = camp == null || Math.abs(camp[0]) > 5 || camp[1] < -4 || camp[1] > 4 ? this.ray(x, z, r) : null;
                this.b.set(x, top, z, ray != null ? ray : this.craterSurface(x, z, r));
                for (int y = top - 2; y < top; y++) {
                    this.b.set(x, y, z, this.b.chance(x, y, z, 0.6f) ? Blocks.TUFF.defaultBlockState() : Blocks.STONE.defaultBlockState());
                }
            } else {
                // Übergang: das Land behält seine Decke (Gras, Sand …), hier und da liegt Auswurf
                BlockState cover = natural.isAir() || !natural.getFluidState().isEmpty() ? Blocks.GRASS_BLOCK.defaultBlockState() : natural;
                BlockState ray = this.ray(x, z, r);
                if (ray != null) {
                    cover = ray;
                } else if (this.b.chance(x, 13, z, (float) (0.35 * (REACH - r) / 5.0))) {
                    cover = this.b.chance(x, 14, z, 0.5f) ? Blocks.GRAVEL.defaultBlockState()
                            : this.b.chance(x, 15, z, 0.5f) ? Blocks.TUFF.defaultBlockState() : Blocks.END_STONE.defaultBlockState();
                }
                if (top != ground || cover != natural) {
                    this.b.set(x, top, z, cover);
                }
                if (top < ground) {
                    this.b.set(x, top - 1, z, Blocks.DIRT.defaultBlockState());
                    this.b.set(x, top - 2, z, Blocks.DIRT.defaultBlockState());
                }
            }
            return top;
        }

        /** Eine Säule des Sternfelsens: vom Grund (oder Meeresboden) bis zur zerklüfteten Krone, unten ein Sockel. */
        private int rockColumn(int x, int z, double r, int wx, int wz) {
            int ground = this.b.level.getHeight(Heightmap.Types.OCEAN_FLOOR, wx, wz) - 1 - this.origin.getY();
            double edge = this.edge(Math.atan2(z, x));
            if (r <= edge) {
                int top = this.rockTop(x, z, r);
                for (int y = ground + 1; y <= top; y++) {
                    this.b.set(x, y, z, this.rock(x, y, z));
                }
                // was oberhalb noch stand (Hang an Land), fällt weg
                for (int y = top + 1; y <= top + 12 && r > SHAFT; y++) {
                    if (y <= this.seaLocal) {
                        break;
                    }
                    this.b.set(x, y, z, Blocks.AIR.defaultBlockState());
                }
                return r <= SHAFT ? Math.max(top, ground) : top;
            }
            if (r <= edge + 3.5) {
                int skirt = ground + (int) Math.round((edge + 3.5 - r) * 1.6 + this.b.noise(x, 5, z) * 1.2);
                for (int y = ground + 1; y <= skirt; y++) {
                    this.b.set(x, y, z, this.rock(x, y, z));
                }
            }
            return ground;
        }

        /** Kernkammer und ihre Hülle (in beiden Formen gleich). */
        private void chamberColumn(int x, int z, double r) {
            if (r > CHAMBER + 1.5) {
                return;
            }
            if (r <= CHAMBER) {
                int dome = this.dome(r);
                this.b.set(x, this.floor - 1, z, this.rock(x, this.floor - 1, z));
                float vein = this.smooth(x, this.floor, z);
                this.b.set(x, this.floor, z, vein < 0.2f ? Blocks.CRYING_OBSIDIAN.defaultBlockState()
                        : vein < 0.32f ? Blocks.OBSIDIAN.defaultBlockState() : vein > 0.8f ? Blocks.AMETHYST_BLOCK.defaultBlockState()
                        : vein > 0.72f ? Blocks.END_STONE_BRICKS.defaultBlockState() : Blocks.END_STONE.defaultBlockState());
                for (int y = this.floor + 1; y <= dome; y++) {
                    this.b.set(x, y, z, Blocks.AIR.defaultBlockState());
                }
                if (r > SHAFT) {
                    this.b.set(x, dome + 1, z, this.rock(x, dome + 1, z));
                }
            } else {
                for (int y = this.floor - 1; y <= this.floor + 1; y++) {
                    this.b.set(x, y, z, this.rock(x, y, z));
                }
            }
        }

        /** Gehört die Stelle zum Hohlraum von Kammer und unterem Schacht? */
        boolean hollow(int x, int y, int z) {
            if (y <= this.floor) {
                return false;
            }
            double r = Math.sqrt(x * x + z * z);
            return r <= CHAMBER && y <= this.dome(r) || r <= SHAFT && y <= this.floor + 14;
        }

        /**
         * Die Hülle um Kammer und Schacht: Jeder Block, der an den Hohlraum grenzt, wird Sternengestein – so dringt weder
         * Wasser noch Lava aus Höhlen ein (auch über Chunkgrenzen hinweg, denn die Form ergibt sich aus der Rechnung, nicht
         * aus den Nachbarblöcken).
         */
        private void sealColumn(int x, int z, double r, int top) {
            if (r > CHAMBER + 2) {
                return;
            }
            for (int y = this.floor; y <= this.floor + 14 && y < top; y++) {
                if (this.hollow(x, y, z)) {
                    continue;
                }
                if (this.hollow(x + 1, y, z) || this.hollow(x - 1, y, z) || this.hollow(x, y, z + 1) || this.hollow(x, y, z - 1)
                        || this.hollow(x, y - 1, z)) {
                    BlockState here = this.b.get(x, y, z);
                    if (y > this.floor || !here.isSolidRender()) {
                        this.b.set(x, y, z, this.rock(x, y, z));
                    }
                }
            }
        }

        /** Der Schacht: innen frei bis hinab in die Kammer, ringsum eine Wand aus Sternengestein. */
        private void shaftColumn(int x, int z, double r, int top) {
            if (r <= SHAFT) {
                for (int y = this.floor + 1; y <= Math.max(top, this.start + 2); y++) {
                    this.b.set(x, y, z, Blocks.AIR.defaultBlockState());
                }
            } else if (r <= SHAFT + 2) {
                int from = r <= CHAMBER ? this.dome(r) + 1 : this.floor;
                for (int y = from; y < top; y++) {
                    this.b.set(x, y, z, this.rock(x, y, z));
                }
            }
        }

        // ---- Wendelsteg -----------------------------------------------------------------------

        /**
         * Der Steg an der Wand des Schachts: fällt je Block Weg um einen halben Block (Platten und Planken im Wechsel),
         * innen ein Geländer mit Laternen; im Schacht tragen Balken in der Wand ihn, in der Kammer Pfosten bis zum Boden.
         */
        private void walkway() {
            BlockState planks = Blocks.SPRUCE_PLANKS.defaultBlockState();
            BlockState slab = Blocks.SPRUCE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
            BlockState fence = Blocks.SPRUCE_FENCE.defaultBlockState();
            int turns = (int) Math.ceil(this.tEnd / (Math.PI * 2)) + 1;
            int topChamber = this.dome(SHAFT);
            for (int x = -7; x <= 7; x++) {
                for (int z = -7; z <= 7; z++) {
                    double r = Math.sqrt(x * x + z * z);
                    if (r < INNER || r > SHAFT) {
                        continue;
                    }
                    double base = Math.floorMod((long) Math.round((Math.atan2(z, x) - this.a0) * 1e6), (long) Math.round(Math.PI * 2e6)) / 1e6;
                    for (int k = 0; k < turns; k++) {
                        double t = base + Math.PI * 2 * k;
                        if (t > this.tEnd) {
                            break;
                        }
                        int half = (int) Math.floor(t * MID);
                        int s2 = 2 * this.start - half;
                        int walk = Math.floorDiv(s2, 2);   // Höhe, in der man etwa steht
                        if (walk <= this.floor + 4 && Math.max(Math.abs(x), Math.abs(z)) <= 3) {
                            continue;   // unten nicht über das Podest
                        }
                        boolean rail = r < RAIL;
                        int floorY;
                        if (s2 % 2 == 0) {
                            floorY = s2 / 2 - 1;
                            this.b.set(x, floorY, z, planks);
                        } else {
                            floorY = (s2 - 1) / 2;
                            this.b.set(x, floorY, z, rail ? planks : slab);
                        }
                        for (int y = floorY + 1; y <= floorY + 3; y++) {
                            this.b.set(x, y, z, Blocks.AIR.defaultBlockState());
                        }
                        if (rail) {
                            this.b.set(x, floorY + 1, z, fence);
                            if (Math.floorMod(half, 10) == 5) {
                                this.b.set(x, floorY + 2, z, Blocks.LANTERN.defaultBlockState());
                            }
                        }
                        if (floorY < topChamber) {
                            // in der Kammer: Pfosten bis zum Boden
                            if (Math.floorMod(half, 5) == 0 && (rail || r > 5.6)) {
                                for (int y = this.floor + 1; y < floorY; y++) {
                                    this.b.set(x, y, z, Blocks.SPRUCE_LOG.defaultBlockState());
                                }
                            }
                        } else if (r > 5.6 && Math.floorMod(half, 4) == 0) {
                            // im Schacht: ein Balken in der Wand darunter
                            this.b.set(x, floorY - 1, z, Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState()
                                    .setValue(RotatedPillarBlock.AXIS, Math.abs(x) > Math.abs(z) ? Direction.Axis.X : Direction.Axis.Z));
                        }
                    }
                }
            }
        }

        // ---- Das Tor ---------------------------------------------------------------------------

        /**
         * Das Podest in der Mitte der Kammer: Endsteinziegel mit Stufen ringsum, darauf die zwölf Rahmen um weinenden
         * Obsidian, an den Ecken Endstäbe. Mit etwas Glück trägt schon der eine oder andere Rahmen ein Auge.
         */
        private void dais() {
            int y = this.floor + 1;
            for (int x = -3; x <= 3; x++) {
                for (int z = -3; z <= 3; z++) {
                    boolean edgeX = Math.abs(x) == 3;
                    boolean edgeZ = Math.abs(z) == 3;
                    BlockState state;
                    if (edgeX && edgeZ) {
                        state = Blocks.CRYING_OBSIDIAN.defaultBlockState();
                    } else if (z == -3) {
                        state = VillageBuilder.stairs(Blocks.END_STONE_BRICK_STAIRS, Direction.SOUTH, Half.BOTTOM);
                    } else if (z == 3) {
                        state = VillageBuilder.stairs(Blocks.END_STONE_BRICK_STAIRS, Direction.NORTH, Half.BOTTOM);
                    } else if (x == -3) {
                        state = VillageBuilder.stairs(Blocks.END_STONE_BRICK_STAIRS, Direction.EAST, Half.BOTTOM);
                    } else if (x == 3) {
                        state = VillageBuilder.stairs(Blocks.END_STONE_BRICK_STAIRS, Direction.WEST, Half.BOTTOM);
                    } else if (Math.abs(x) <= 1 && Math.abs(z) <= 1) {
                        state = Blocks.CRYING_OBSIDIAN.defaultBlockState();
                    } else {
                        state = Blocks.END_STONE_BRICKS.defaultBlockState();
                    }
                    this.b.set(x, y, z, state);
                    for (int up = 1; up <= 4; up++) {
                        if (!(Math.abs(x) <= 2 && Math.abs(z) <= 2 && up == 1)) {
                            this.b.set(x, y + up, z, Blocks.AIR.defaultBlockState());
                        }
                    }
                }
            }
            boolean all = true;
            for (int i = -1; i <= 1; i++) {
                all &= this.frame(i, y + 1, -2, Direction.SOUTH);
                all &= this.frame(i, y + 1, 2, Direction.NORTH);
                all &= this.frame(-2, y + 1, i, Direction.EAST);
                all &= this.frame(2, y + 1, i, Direction.WEST);
            }
            for (int[] c : new int[][] {{-2, -2}, {2, -2}, {-2, 2}, {2, 2}}) {
                this.b.set(c[0], y + 1, c[1], Blocks.END_ROD.defaultBlockState().setValue(EndRodBlock.FACING, Direction.UP));
            }
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    this.b.set(x, y + 1, z, all ? Blocks.END_PORTAL.defaultBlockState() : Blocks.AIR.defaultBlockState());
                }
            }
        }

        private boolean frame(int x, int y, int z, Direction facing) {
            boolean eye = this.b.chance(x, y, z, 0.1f);
            this.b.set(x, y, z, Blocks.END_PORTAL_FRAME.defaultBlockState().setValue(EndPortalFrameBlock.FACING, facing)
                    .setValue(EndPortalFrameBlock.HAS_EYE, eye));
            return eye;
        }

        /** Amethyst an Boden und Decke der Kammer, Laternen an den Pfosten. */
        private void chamberDetails() {
            for (int x = -12; x <= 12; x++) {
                for (int z = -12; z <= 12; z++) {
                    double r = Math.sqrt(x * x + z * z);
                    if (r > CHAMBER - 0.5) {
                        continue;
                    }
                    if (r > 8.5 && this.b.chance(x, this.floor + 1, z, 0.13f) && this.b.get(x, this.floor + 1, z).isAir()) {
                        this.b.set(x, this.floor + 1, z, Blocks.AMETHYST_CLUSTER.defaultBlockState().setValue(AmethystClusterBlock.FACING, Direction.UP));
                    }
                    int dome = this.dome(r);
                    if (r > SHAFT + 0.5 && this.b.chance(x, dome, z, 0.07f) && this.b.get(x, dome, z).isAir()) {
                        this.b.set(x, dome, z, (this.b.chance(x, dome, z, 0.03f) ? Blocks.AMETHYST_CLUSTER : Blocks.LARGE_AMETHYST_BUD)
                                .defaultBlockState().setValue(AmethystClusterBlock.FACING, Direction.DOWN));
                    }
                }
            }
        }

        /** Die Schreibstube der Sterndeuter neben dem Tor: Pult, Stuhl, Regale, Lesepult, Kerzen und eine Truhe. */
        private void study() {
            int axis = (this.campAxis + 2) & 3;
            int[] a = axis(axis);
            BlockPos at = this.origin.offset(a[0] * 9, this.floor, a[1] * 9);
            VillageBuilder s = new VillageBuilder(this.b.level, this.b.chunkBox, at, inward(axis), this.piece.seed() ^ 0x57D1L);
            for (int x = -3; x <= 3; x++) {
                for (int z = -1; z <= 2; z++) {
                    for (int y = 1; y <= 3; y++) {
                        s.set(x, y, z, Blocks.AIR.defaultBlockState());
                    }
                }
            }
            s.set(-1, 1, 0, ModBlocks.OAK_TABLE.get().defaultBlockState());
            s.set(0, 1, 0, ModBlocks.OAK_TABLE.get().defaultBlockState());
            s.set(0, 1, 1, ModBlocks.OAK_CHAIR.get().defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH));
            s.set(-1, 2, 0, Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.CANDLES, 3).setValue(CandleBlock.LIT, true));
            s.set(2, 1, 0, Blocks.LECTERN.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.SOUTH));
            for (int x : new int[] {-3, -2}) {
                for (int y = 1; y <= 2; y++) {
                    s.set(x, y, -1, Blocks.BOOKSHELF);
                }
            }
            s.set(-3, 3, -1, Blocks.DYED_CANDLE.pick(DyeColor.PURPLE).defaultBlockState().setValue(CandleBlock.CANDLES, 2)
                    .setValue(CandleBlock.LIT, true));
            s.set(-1, 1, 1, Blocks.CARPET.pick(DyeColor.PURPLE));
            s.set(1, 1, 1, Blocks.CARPET.pick(DyeColor.PURPLE));
            s.chest(1, 1, -1, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH), ModLootTables.SITE_FALLEN_STAR_CORE);
            s.set(3, 1, 1, Blocks.SPRUCE_FENCE);
            s.set(3, 2, 1, Blocks.LANTERN);
            s.finish();
        }

        // ---- Krater ----------------------------------------------------------------------------

        /** Lage einer Säule im Rahmen des Lagers (eigene Koordinaten des Lagers) oder {@code null}, wenn weit weg. */
        int[] campLocal(int x, int z) {
            int[] a = axis(this.campAxis);
            int rx = x - a[0] * 12;
            int rz = z - a[1] * 12;
            if (Math.abs(rx) > 7 || Math.abs(rz) > 7) {
                return null;
            }
            BlockPos local = new BlockPos(rx, 0, rz).rotate(inverse(inward(this.campAxis)));
            return new int[] {local.getX(), local.getZ()};
        }

        /**
         * Das Lager der Sterndeuter auf einer Terrasse im Krater: ein gestreiftes Zelt, ein Feuer mit Sitzstämmen, ein
         * Fernrohr auf dem Stativ, auf den Stern gerichtet, ein Lesepult, Fässer und eine Truhe.
         */
        private void camp() {
            int[] a = axis(this.campAxis);
            int level = crater(12, 0.0);
            BlockPos at = this.origin.offset(a[0] * 12, level, a[1] * 12);
            VillageBuilder c = new VillageBuilder(this.b.level, this.b.chunkBox, at, inward(this.campAxis), this.piece.seed() ^ 0xCA4FL);
            for (int x = -5; x <= 5; x++) {
                for (int z = -4; z <= 4; z++) {
                    for (int y = 1; y <= 4; y++) {
                        c.set(x, y, z, Blocks.AIR.defaultBlockState());
                    }
                    c.set(x, 0, z, c.chance(x, 0, z, 0.55f) ? Blocks.DIRT_PATH.defaultBlockState() : Blocks.GRAVEL.defaultBlockState());
                }
            }
            // das Zelt (First längs, vorn offen)
            BlockState white = Blocks.WOOL.pick(DyeColor.WHITE).defaultBlockState();
            BlockState blue = Blocks.WOOL.pick(DyeColor.LIGHT_BLUE).defaultBlockState();
            for (int z = -3; z <= 1; z++) {
                BlockState cloth = Math.floorMod(z, 2) == 0 ? white : blue;
                c.set(-5, 1, z, cloth);
                c.set(-4, 2, z, cloth);
                c.set(-3, 3, z, cloth);
                c.set(-2, 2, z, cloth);
                c.set(-1, 1, z, cloth);
            }
            c.set(-4, 1, -3, white);
            c.set(-3, 1, -3, white);
            c.set(-2, 1, -3, white);
            c.set(-3, 2, -3, white);
            c.set(-3, 1, -1, Blocks.CARPET.pick(DyeColor.RED));
            c.set(-3, 1, 0, Blocks.CARPET.pick(DyeColor.RED));
            c.set(-4, 1, 1, Blocks.SPRUCE_FENCE);
            c.set(-2, 1, 1, Blocks.SPRUCE_FENCE);
            // Feuer und Sitzstämme
            c.set(2, 1, 1, Blocks.CAMPFIRE);
            c.set(2, 1, 3, Blocks.SPRUCE_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X));
            c.set(4, 1, 1, Blocks.SPRUCE_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z));
            // Fernrohr auf dem Stativ, zum Stern gerichtet
            c.set(3, 1, -2, Blocks.SPRUCE_FENCE);
            c.set(3, 2, -2, Blocks.SPRUCE_FENCE);
            c.set(3, 3, -2, Blocks.LIGHTNING_ROD.waxed().exposed().defaultBlockState().setValue(LightningRodBlock.FACING, Direction.SOUTH));
            c.set(1, 1, -2, Blocks.LECTERN.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.SOUTH));
            c.set(4, 1, -3, Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, Direction.UP));
            c.set(3, 1, -3, Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, Direction.UP));
            c.chest(4, 1, -1, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.WEST), ModLootTables.SITE_FALLEN_STAR_CAMP);
            c.set(0, 1, 2, Blocks.SPRUCE_FENCE);
            c.set(0, 2, 2, Blocks.SPRUCE_FENCE);
            c.set(0, 3, 2, Blocks.LANTERN);
            c.finish();
        }

        /** Drei Bruchstücke des Sterns ragen schräg aus dem Kraterboden. */
        private void spikes() {
            double aCamp = Math.atan2(axis(this.campAxis)[1], axis(this.campAxis)[0]);
            for (double off : new double[] {1.7, 3.3, 4.8}) {
                double phi = aCamp + off;
                double cx = Math.cos(phi) * 10.5;
                double cz = Math.sin(phi) * 10.5;
                int h = 4 + (int) (this.b.noise((int) (off * 10), 1, 0) * 4);
                int base = crater(10.5, 0.0);
                for (int i = 0; i <= h; i++) {
                    double lean = i * 0.3;
                    double px = cx + Math.cos(phi) * lean;
                    double pz = cz + Math.sin(phi) * lean;
                    double rad = 1.7 * (h - i) / h + 0.35;
                    for (int x = (int) Math.floor(px - rad); x <= (int) Math.ceil(px + rad); x++) {
                        for (int z = (int) Math.floor(pz - rad); z <= (int) Math.ceil(pz + rad); z++) {
                            double d = Math.hypot(x + 0.5 - px - 0.5, z + 0.5 - pz - 0.5);
                            if (d <= rad) {
                                this.b.set(x, base + i, z, this.rock(x, base + i, z));
                            }
                        }
                    }
                }
            }
        }

        /** Brocken des Sterns, die beim Einschlag bis an den Wall flogen. */
        private void boulders() {
            double aCamp = Math.atan2(axis(this.campAxis)[1], axis(this.campAxis)[0]);
            for (int i = 0; i < 6; i++) {
                double phi = aCamp + 0.9 + i * 0.85 + this.b.noise(i, 2, 0) * 0.4;
                double dist = 14 + this.b.noise(i, 3, 0) * 4.5;
                double rad = 1.1 + this.b.noise(i, 4, 0) * 0.9;
                double cx = Math.cos(phi) * dist;
                double cz = Math.sin(phi) * dist;
                int base = crater(dist, 0.0);
                for (int x = (int) Math.floor(cx - rad); x <= (int) Math.ceil(cx + rad); x++) {
                    for (int z = (int) Math.floor(cz - rad); z <= (int) Math.ceil(cz + rad); z++) {
                        for (int y = 0; y <= (int) Math.ceil(rad); y++) {
                            double d = Math.sqrt((x - cx) * (x - cx) + (z - cz) * (z - cz) + y * y * 1.6);
                            if (d <= rad) {
                                this.b.set(x, base + y, z, this.rock(x, base + y, z));
                            }
                        }
                    }
                }
            }
        }

        /** Ein Zaun um die Öffnung des Schachts (an der Stelle, wo der Steg beginnt, offen), mit Laternen. */
        private void rimFence() {
            for (int x = -8; x <= 8; x++) {
                for (int z = -8; z <= 8; z++) {
                    double r = Math.sqrt(x * x + z * z);
                    if (r < SHAFT || r >= SHAFT + 1.0) {
                        continue;
                    }
                    double phi = Math.atan2(z, x);
                    if (this.nearStart(phi, 0.5)) {
                        continue;
                    }
                    int top = this.variant == CRATER ? this.craterAt(x, z) : this.rockTop(x, z, r);
                    this.b.set(x, top + 1, z, Blocks.SPRUCE_FENCE);
                    if (this.b.chance(x, top + 2, z, 0.12f)) {
                        this.b.set(x, top + 2, z, Blocks.LANTERN);
                    }
                }
            }
        }

        // ---- Sternfels ---------------------------------------------------------------------------

        /** Außen am Fels eine Leiter vom Wasser (oder Boden) bis hinauf zur Kerbe, an der der Steg beginnt. */
        private void ladder() {
            double cx = Math.cos(this.a0);
            double cz = Math.sin(this.a0);
            Direction out = Math.abs(cx) > Math.abs(cz) ? (cx > 0 ? Direction.EAST : Direction.WEST) : (cz > 0 ? Direction.SOUTH : Direction.NORTH);
            double edge = this.edge(this.a0);
            int lx = (int) Math.round(cx * (edge - 0.3)) + out.getStepX();
            int lz = (int) Math.round(cz * (edge - 0.3)) + out.getStepZ();
            for (int y = Math.min(this.seaLocal, 0) - 8; y <= 0; y++) {
                this.b.set(lx - out.getStepX(), y, lz - out.getStepZ(), this.rock(lx, y, lz));
            }
            BlockPos column = this.origin.offset(lx, 0, lz);
            if (this.b.chunkBox.isInside(column.getX(), this.b.chunkBox.minY() + 1, column.getZ())) {
                int ground = this.b.level.getHeight(Heightmap.Types.OCEAN_FLOOR, column.getX(), column.getZ()) - this.origin.getY();
                for (int y = Math.max(Math.min(this.seaLocal, 0) - 1, ground); y <= 0; y++) {
                    this.b.set(lx, y, lz, Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, out));
                }
            }
            this.b.set(lx, 1, lz, Blocks.AIR.defaultBlockState());
        }

        /** Oben in der Kerbe ein Absatz aus Planken mit dem Vorrat der Sterndeuter. */
        private void landing() {
            for (int x = -11; x <= 11; x++) {
                for (int z = -11; z <= 11; z++) {
                    double r = Math.sqrt(x * x + z * z);
                    if (r <= SHAFT || r > this.edge(Math.atan2(z, x)) || !this.nearStart(Math.atan2(z, x), 0.34)) {
                        continue;
                    }
                    this.b.set(x, 0, z, Blocks.SPRUCE_PLANKS);
                    for (int y = 1; y <= 3; y++) {
                        this.b.set(x, y, z, Blocks.AIR.defaultBlockState());
                    }
                }
            }
            double side = this.a0 + 0.22;
            int bx = (int) Math.round(Math.cos(side) * 8.6);
            int bz = (int) Math.round(Math.sin(side) * 8.6);
            this.b.chest(bx, 1, bz, Blocks.CHEST.defaultBlockState(), ModLootTables.SITE_FALLEN_STAR_CAMP);
            double other = this.a0 - 0.22;
            int ox = (int) Math.round(Math.cos(other) * 8.6);
            int oz = (int) Math.round(Math.sin(other) * 8.6);
            this.b.set(ox, 1, oz, Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, Direction.UP));
            this.b.set(ox, 2, oz, Blocks.LANTERN);
        }
    }
}
