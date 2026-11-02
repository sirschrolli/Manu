package io.github.sirschrolli.kingdomsatwar.worldgen.site;

import io.github.sirschrolli.kingdomsatwar.loot.ModLootTables;
import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.VillageBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * Die Wasserstollen der Alten: Unter der Erde liegen die Wasserwerke eines längst vergangenen Königreichs – gewölbte
 * Kanäle mit einer Rinne in der Mitte und Gehwegen zu beiden Seiten, Gurtbögen aus Tuff, Laternen an Ketten,
 * vergitterte Zuläufe, aus denen es noch tropft. Wo sich Kanäle treffen, liegen Zisternen: Säulenhallen über einem
 * tiefen, von unten leuchtenden Becken. Treppen führen in tiefere Ebenen. Den Zugang bildet eine Brunnenkammer, deren
 * Schacht bis hinauf an die Oberfläche reicht, wo ein kleines Brunnenhaus steht.
 *
 * <p>Eigene Koordinaten je Bauteil: z zeigt in Laufrichtung (ungedreht nach Süden), x quer dazu, y = 0 ist der Boden.
 */
final class Aqueduct {
    static final int WELL = 0;
    static final int TUNNEL = 1;
    static final int CISTERN = 2;
    static final int STAIRS = 3;

    /** Merkmale eines Kanals (data[1]). */
    static final int ALCOVE = 1;
    static final int COLLAPSED = 2;
    static final int END = 4;
    static final int INLETS = 8;

    /** Ausgänge einer Halle (Bitmaske, data[0]). */
    static int bit(Direction dir) {
        return switch (dir) {
            case NORTH -> 1;
            case EAST -> 2;
            case SOUTH -> 4;
            default -> 8;
        };
    }

    private Aqueduct() {}

    // =====================================================================================
    // Planung
    // =====================================================================================

    /** Ein offenes Ende des Netzes: hier kann ein Kanal beginnen. {@code parent}: Daten des Teils, an dem er hängt. */
    private record Node(BlockPos pos, Direction dir, int depth, int[] parent, int parentPart) {}

    static Optional<Structure.GenerationStub> plan(Structure.GenerationContext context, SiteTerrain terrain, long seed) {
        ChunkPos chunk = context.chunkPos();
        RandomSource random = RandomSource.create(seed);
        int cx = chunk.getMiddleBlockX();
        int cz = chunk.getMiddleBlockZ();
        int ground = terrain.ground(cx, cz);
        int y = Math.max(terrain.minY() + 16, ground - 22 - random.nextInt(16));
        if (y > ground - 20) {
            return Optional.empty();
        }
        // nur zu manchen Stollen führt ein Brunnenschacht bis an die Oberfläche (nie im Wasser); die übrigen liegen verborgen
        int shaft = terrain.water(cx, cz) || random.nextFloat() < 0.6f ? 0 : ground - y;
        List<SitePiece> pieces = new ArrayList<>();
        List<BoundingBox> taken = new ArrayList<>();
        BlockPos start = new BlockPos(cx, y, cz);
        int[] wellData = {0, shaft};
        BoundingBox wellBox = new BoundingBox(cx - 8, y - 2, cz - 8, cx + 8, y + Math.max(8, shaft + 6), cz + 8);
        pieces.add(new SitePiece(SiteKind.AQUEDUCT, WELL, start, Rotation.NONE, random.nextLong(), wellData, wellBox));
        // belegt: die Kammer bis zu ihrer Wand (die Kanäle beginnen in der Wand, eine Lage überlappt)
        taken.add(new BoundingBox(cx - 7, y - 2, cz - 7, cx + 7, y + 8, cz + 7));
        List<int[]> tunnels = new ArrayList<>();
        List<Node> open = new ArrayList<>();
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            if (random.nextFloat() < 0.8f) {
                open.add(new Node(start.relative(dir, 7), dir, 0, wellData, WELL));
            }
        }
        int guard = 0;
        while (!open.isEmpty() && pieces.size() < 40 && guard++ < 200) {
            Node node = open.removeFirst();
            if (node.depth > 7 || node.pos.distManhattan(start) > 120) {
                continue;
            }
            Rotation rotation = rotationOf(node.dir);
            if (node.depth > 0 && node.parentPart == TUNNEL && random.nextFloat() < 0.2f) {
                // Treppe hinab (oder hinauf), vier Blöcke Höhenunterschied
                int dy = random.nextBoolean() || node.pos.getY() > ground - 20 ? -1 : 1;
                if (node.pos.getY() - 4 < terrain.minY() + 12) {
                    dy = 1;
                }
                BoundingBox box = SitePiece.box(node.pos, rotation, -4, Math.min(0, dy * 4) - 2, 0, 4, Math.max(0, dy * 4) + 6, 7);
                if (overlaps(taken, box) || shallow(terrain, box)) {
                    continue;
                }
                int[] data = {8, dy};
                pieces.add(new SitePiece(SiteKind.AQUEDUCT, STAIRS, node.pos, rotation, random.nextLong(), data, box));
                taken.add(box);
                connected(node);
                open.add(new Node(node.pos.relative(node.dir, 8).above(dy * 4), node.dir, node.depth + 1, data, STAIRS));
                continue;
            }
            int len = 12 + 6 * random.nextInt(4);
            BoundingBox box = SitePiece.box(node.pos, rotation, -5, -2, 0, 5, 6, len - 1);
            while ((overlaps(taken, box) || shallow(terrain, box)) && len > 12) {
                len -= 6;
                box = SitePiece.box(node.pos, rotation, -5, -2, 0, 5, 6, len - 1);
            }
            if (overlaps(taken, box) || shallow(terrain, box)) {
                continue;
            }
            float roll = random.nextFloat();
            boolean end = node.depth >= 6 || roll < 0.2f;
            int flags = (random.nextFloat() < 0.35f ? ALCOVE : 0) | (random.nextFloat() < 0.5f ? INLETS : 0)
                    | (end && random.nextFloat() < 0.5f ? COLLAPSED : 0);
            int[] data = {len, flags};
            tunnels.add(data);
            pieces.add(new SitePiece(SiteKind.AQUEDUCT, TUNNEL, node.pos, rotation, random.nextLong(), data, box));
            taken.add(box);
            connected(node);
            if (end) {
                continue;
            }
            BlockPos far = node.pos.relative(node.dir, len);
            if (roll < 0.55f) {
                // eine Zisterne mit weiteren Kanälen
                BlockPos center = far.relative(node.dir, 7);
                BoundingBox hall = new BoundingBox(center.getX() - 8, center.getY() - 4, center.getZ() - 8, center.getX() + 8, center.getY() + 9,
                        center.getZ() + 8);
                BoundingBox hallTaken = new BoundingBox(center.getX() - 7, center.getY() - 4, center.getZ() - 7, center.getX() + 7,
                        center.getY() + 9, center.getZ() + 7);
                if (overlaps(taken, hallTaken) || shallow(terrain, hall)) {
                    continue;
                }
                int[] hallData = {bit(node.dir.getOpposite())};
                pieces.add(new SitePiece(SiteKind.AQUEDUCT, CISTERN, center, Rotation.NONE, random.nextLong(), hallData, hall));
                taken.add(hallTaken);
                data[1] |= 16;   // geht weiter
                for (Direction next : new Direction[] {node.dir, node.dir.getClockWise(), node.dir.getCounterClockWise()}) {
                    if (random.nextFloat() < (next == node.dir ? 0.6f : 0.75f)) {
                        open.add(new Node(center.relative(next, 7), next, node.depth + 1, hallData, CISTERN));
                    }
                }
            } else {
                open.add(new Node(far, node.dir, node.depth + 1, data, TUNNEL));
            }
        }
        // Kanäle, an die nichts mehr anschließt, enden an einem Gitter (oder sind eingestürzt)
        for (int[] data : tunnels) {
            if ((data[1] & (16 | COLLAPSED)) == 0) {
                data[1] |= END;
            }
        }
        if (pieces.size() < 6) {
            return Optional.empty();
        }
        return Optional.of(new Structure.GenerationStub(start, builder -> pieces.forEach(builder::addPiece)));
    }

    /** Ein Kanal (oder eine Treppe) schließt an: am Brunnen und an der Zisterne öffnet sich die Wand, der vorige Kanal geht weiter. */
    private static void connected(Node node) {
        if (node.parentPart == WELL || node.parentPart == CISTERN) {
            node.parent[0] |= bit(node.dir);
        } else if (node.parentPart == TUNNEL) {
            node.parent[1] |= 16;
        }
    }

    private static Rotation rotationOf(Direction dir) {
        return switch (dir) {
            case WEST -> Rotation.CLOCKWISE_90;
            case NORTH -> Rotation.CLOCKWISE_180;
            case EAST -> Rotation.COUNTERCLOCKWISE_90;
            default -> Rotation.NONE;
        };
    }

    /**
     * Käme ein Bauteil der Oberfläche zu nahe? Über seiner Decke sollen überall mindestens sechs Blöcke Gestein liegen
     * (geprüft an den Ecken und in der Mitte) – sonst bräche der Stollen an Hängen, in Tälern und unter Flüssen auf.
     */
    private static boolean shallow(SiteTerrain terrain, BoundingBox box) {
        int[][] probes = {{box.minX(), box.minZ()}, {box.maxX(), box.minZ()}, {box.minX(), box.maxZ()}, {box.maxX(), box.maxZ()},
                {(box.minX() + box.maxX()) / 2, (box.minZ() + box.maxZ()) / 2}};
        for (int[] p : probes) {
            if (terrain.ground(p[0], p[1]) < box.maxY() + 6) {
                return true;
            }
        }
        return false;
    }

    private static boolean overlaps(List<BoundingBox> taken, BoundingBox box) {
        BoundingBox inner = box.inflatedBy(-1);
        for (BoundingBox other : taken) {
            if (other.intersects(inner)) {
                return true;
            }
        }
        return false;
    }

    // =====================================================================================
    // Bauen
    // =====================================================================================

    static void build(VillageBuilder b, SitePiece piece) {
        Works w = new Works(b, piece);
        switch (piece.part()) {
            case WELL -> w.well();
            case TUNNEL -> w.tunnel();
            case CISTERN -> w.cistern();
            case STAIRS -> w.stairs();
            default -> {
            }
        }
    }

    private static final class Works {
        private final VillageBuilder b;
        private final SitePiece piece;

        Works(VillageBuilder b, SitePiece piece) {
            this.b = b;
            this.piece = piece;
        }

        // ---- Baustoffe ------------------------------------------------------------------------

        BlockState wall(int x, int y, int z) {
            float n = this.b.noise(x, y, z);
            if (n < 0.52f) {
                return Blocks.STONE_BRICKS.defaultBlockState();
            }
            if (n < 0.72f) {
                return Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
            }
            if (n < 0.86f) {
                return Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
            }
            return Blocks.TUFF_BRICKS.defaultBlockState();
        }

        BlockState walkway(int x, int y, int z) {
            float n = this.b.noise(x, y + 50, z);
            if (Math.abs(x) == 1) {
                return Blocks.POLISHED_TUFF.defaultBlockState();
            }
            return n < 0.7f ? Blocks.POLISHED_ANDESITE.defaultBlockState() : n < 0.85f ? Blocks.ANDESITE.defaultBlockState()
                    : Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
        }

        /** Luft schaffen; Wasser und Lava ringsum werden zu Fels (nach unten sorgen Boden und Rinne dafür). */
        void carve(int x, int y, int z) {
            this.b.set(x, y, z, Blocks.AIR.defaultBlockState());
            this.seal(x, y, z);
        }

        void seal(int x, int y, int z) {
            for (int[] d : SIDES) {
                BlockState next = this.b.get(x + d[0], y + d[1], z + d[2]);
                if (!next.getFluidState().isEmpty() && !next.is(Blocks.WATER) || next.is(Blocks.WATER) && !this.ours(x + d[0], y + d[1], z + d[2])) {
                    this.b.set(x + d[0], y + d[1], z + d[2], this.b.world(x, y, z).getY() < 0 ? Blocks.DEEPSLATE.defaultBlockState()
                            : Blocks.STONE.defaultBlockState());
                }
            }
        }

        /** Gehört dieses Wasser zu den Werken (Rinne, Becken, Zulauf)? Dann bleibt es. */
        boolean ours(int x, int y, int z) {
            return switch (this.piece.part()) {
                case TUNNEL -> x == 0 && y == 0 || Math.abs(x) == 4 && y >= 1 && y <= 2;
                case CISTERN -> Math.abs(x) <= 4 && Math.abs(z) <= 4 && y <= 0 || x == 0 && Math.abs(z) <= 7 && y == 0
                        || z == 0 && Math.abs(x) <= 7 && y == 0;
                case WELL -> Math.abs(x) <= 1 && Math.abs(z) <= 1 && y <= 0 || (x == 0 || z == 0) && y == 0;
                default -> false;
            };
        }

        private static final int[][] SIDES = {{1, 0, 0}, {-1, 0, 0}, {0, 0, 1}, {0, 0, -1}, {0, 1, 0}};

        /** Unter dem Boden: wo kein Fels ist (Höhle), tragen Pfeiler aus Ziegeln bis zum Grund. */
        void pier(int x, int y, int z) {
            for (int d = 1; d < 30; d++) {
                BlockState s = this.b.get(x, y - d, z);
                if (s.isSolidRender()) {
                    return;
                }
                this.b.set(x, y - d, z, this.wall(x, y - d, z));
            }
        }

        BlockState water() {
            return Blocks.WATER.defaultBlockState();
        }

        // ---- Kanal ---------------------------------------------------------------------------

        /**
         * Ein Kanal: Tonnengewölbe (sieben breit), Rinne in der Mitte, Gehwege zu beiden Seiten, alle sechs Blöcke ein
         * Gurtbogen aus Tuff mit einer Laterne an der Kette; vergitterte Zuläufe, eine Nische mit Truhe; am Ende ein
         * Gitter oder Geröll.
         */
        void tunnel() {
            int len = this.piece.data(0);
            int flags = this.piece.data(1);
            boolean collapsed = (flags & COLLAPSED) != 0;
            boolean end = (flags & END) != 0;
            int last = collapsed ? len - 4 : len;
            for (int z = 0; z < len; z++) {
                boolean rib = Math.floorMod(z, 6) == 3;
                this.section(z, rib, z < last);
                if (z >= last) {
                    // eingestürzt: Geröll bis unter das Gewölbe
                    for (int x = -2; x <= 2; x++) {
                        int h = 1 + (int) (this.b.noise(x, 1, z) * 3) + (z - last);
                        for (int y = 0; y <= Math.min(4, h); y++) {
                            float n = this.b.noise(x, y, z + 99);
                            this.b.set(x, y, z, n < 0.45f ? Blocks.GRAVEL.defaultBlockState() : n < 0.75f ? Blocks.COBBLESTONE.defaultBlockState()
                                    : Blocks.MOSSY_COBBLESTONE.defaultBlockState());
                        }
                    }
                    continue;
                }
                if (rib && Math.floorMod(z, 12) == 3) {
                    this.b.set(0, 4, z, Blocks.IRON_CHAIN);
                    this.b.set(0, 3, z, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
                }
                if ((flags & INLETS) != 0 && Math.floorMod(z, 12) == 9) {
                    this.inlet(z, this.b.chance(0, 9, z, 0.5f) ? 1 : -1);
                }
                // Glimmflechte an den Wänden: ein mattes, grünes Licht
                for (int x : new int[] {-2, 2}) {
                    if (this.b.chance(x, 2, z, 0.08f)) {
                        this.b.set(x, 1 + (int) (this.b.noise(x, 3, z) * 2), z, Blocks.GLOW_LICHEN.defaultBlockState()
                                .setValue(MultifaceBlock.getFaceProperty(x < 0 ? Direction.WEST : Direction.EAST), true));
                    }
                }
            }
            if ((flags & ALCOVE) != 0 && last > 8) {
                this.alcove(last / 2 + (last / 2 % 6 == 3 ? 1 : 0), this.b.chance(1, 1, 1, 0.5f) ? 1 : -1);
            }
            if (end && !collapsed) {
                // Ablaufgitter am Ende
                int z = len - 1;
                for (int x = -2; x <= 2; x++) {
                    for (int y = 1; y <= 4; y++) {
                        this.b.set(x, y, z, Math.abs(x) <= 1 && y <= 3 ? Blocks.IRON_BARS.defaultBlockState() : this.wall(x, y, z));
                    }
                }
                this.b.set(0, 0, z, Blocks.STONE_BRICKS);
            }
        }

        /** Ein Querschnitt des Kanals bei z (Wände, Gewölbe, Gehwege, Rinne). */
        void section(int z, boolean rib, boolean open) {
            for (int x = -3; x <= 3; x++) {
                // unter dem Boden
                this.b.set(x, -1, z, x == 0 ? Blocks.SMOOTH_STONE.defaultBlockState() : this.wall(x, -1, z));
                if (Math.abs(x) == 3) {
                    for (int y = 0; y <= 5; y++) {
                        this.b.set(x, y, z, rib ? (y == 0 || y == 4 ? Blocks.CHISELED_TUFF_BRICKS.defaultBlockState()
                                : Blocks.TUFF_BRICKS.defaultBlockState()) : this.wall(x, y, z));
                    }
                    continue;
                }
                if (open) {
                    for (int y = 1; y <= (Math.abs(x) == 2 ? 3 : 4); y++) {
                        this.carve(x, y, z);
                    }
                }
                if (x == 0) {
                    this.b.set(0, 0, z, open ? this.water() : Blocks.GRAVEL.defaultBlockState());
                } else {
                    this.b.set(x, 0, z, this.walkway(x, 0, z));
                }
                if (Math.floorMod(z, 6) == 0 && Math.abs(x) == 2) {
                    this.pier(x, -1, z);
                }
            }
            // Gewölbe
            BlockState spring = rib ? Blocks.TUFF_BRICK_STAIRS.defaultBlockState() : Blocks.STONE_BRICK_STAIRS.defaultBlockState();
            this.b.set(-2, 4, z, VillageBuilder.stairs(spring.getBlock(), Direction.WEST, Half.TOP));
            this.b.set(2, 4, z, VillageBuilder.stairs(spring.getBlock(), Direction.EAST, Half.TOP));
            this.b.set(-1, 5, z, VillageBuilder.stairs(spring.getBlock(), Direction.WEST, Half.TOP));
            this.b.set(1, 5, z, VillageBuilder.stairs(spring.getBlock(), Direction.EAST, Half.TOP));
            this.b.set(0, 5, z, rib ? Blocks.CHISELED_TUFF.defaultBlockState() : Blocks.STONE_BRICKS.defaultBlockState());
            this.b.set(-2, 5, z, this.wall(-2, 5, z));
            this.b.set(2, 5, z, this.wall(2, 5, z));
            for (int x = -1; x <= 1; x++) {
                this.b.set(x, 6, z, this.wall(x, 6, z));
            }
        }

        /** Ein vergitterter Zulauf in der Wand: hinter dem Gitter steht Wasser. */
        void inlet(int z, int side) {
            int wx = 3 * side;
            for (int y = 1; y <= 2; y++) {
                this.b.set(wx, y, z, Blocks.IRON_BARS);
                this.b.set(wx + side, y, z, this.water());
                this.b.set(wx + 2 * side, y, z, this.wall(wx + 2 * side, y, z));
                this.b.set(wx + side, y, z - 1, this.wall(wx + side, y, z - 1));
                this.b.set(wx + side, y, z + 1, this.wall(wx + side, y, z + 1));
            }
            this.b.set(wx + side, 0, z, this.wall(wx + side, 0, z));
            this.b.set(wx + side, 3, z, this.wall(wx + side, 3, z));
            this.b.set(wx - side, 1, z, Blocks.MOSS_CARPET);
        }

        /** Eine Nische für die Wasserwärter: Truhe, Fass, Schleifstein, Laterne. */
        void alcove(int z, int side) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int d = 3; d <= 5; d++) {
                    for (int y = 0; y <= 4; y++) {
                        boolean inside = d <= 4 && y >= 1 && y <= 3;
                        this.b.set(d * side, y, z + dz, inside ? Blocks.AIR.defaultBlockState() : this.wall(d * side, y, z + dz));
                    }
                }
            }
            for (int dz = -1; dz <= 1; dz++) {
                this.b.set(3 * side, 0, z + dz, this.walkway(2, 0, z + dz));
                this.b.set(4 * side, 0, z + dz, this.walkway(2, 0, z + dz));
            }
            Direction facing = side > 0 ? Direction.WEST : Direction.EAST;
            this.b.chest(4 * side, 1, z, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing), ModLootTables.SITE_AQUEDUCT_ALCOVE);
            this.b.set(4 * side, 1, z - 1, Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, Direction.UP));
            this.b.set(4 * side, 1, z + 1, Blocks.GRINDSTONE.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.GrindstoneBlock.FACE, net.minecraft.world.level.block.state.properties.AttachFace.FLOOR));
            this.b.set(4 * side, 3, z, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
            this.b.set(4 * side, 4, z, Blocks.IRON_CHAIN);
        }

        // ---- Treppe ---------------------------------------------------------------------------

        /** Eine Treppe zwischen zwei Ebenen: Gewölbe und Wände steigen mit, die Rinne endet davor. */
        void stairs() {
            int len = this.piece.data(0);
            int dy = this.piece.data(1);
            for (int z = 0; z < len; z++) {
                int h = dy > 0 ? z / 2 : -((z + 1) / 2);
                boolean rib = Math.floorMod(z, 4) == 2;
                for (int x = -3; x <= 3; x++) {
                    this.b.set(x, h - 1, z, this.wall(x, h - 1, z));
                    if (Math.abs(x) == 3) {
                        for (int y = h; y <= h + 6; y++) {
                            this.b.set(x, y, z, rib ? Blocks.TUFF_BRICKS.defaultBlockState() : this.wall(x, y, z));
                        }
                        continue;
                    }
                    for (int y = h + 1; y <= h + (Math.abs(x) == 2 ? 4 : 5); y++) {
                        this.carve(x, y, z);
                    }
                    this.b.set(x, h, z, this.walkway(2, h, z));
                    if (z % 2 == 1) {
                        this.b.set(x, h + 1, z, VillageBuilder.stairs(Blocks.STONE_BRICK_STAIRS, dy > 0 ? Direction.SOUTH : Direction.NORTH,
                                Half.BOTTOM));
                    }
                }
                // das Gewölbe steigt eine Stufe höher an (eine Lage mehr Luft über den Stufen)
                Direction w = Direction.WEST;
                Direction e = Direction.EAST;
                this.b.set(-2, h + 5, z, VillageBuilder.stairs(Blocks.STONE_BRICK_STAIRS, w, Half.TOP));
                this.b.set(2, h + 5, z, VillageBuilder.stairs(Blocks.STONE_BRICK_STAIRS, e, Half.TOP));
                this.b.set(-1, h + 6, z, VillageBuilder.stairs(Blocks.STONE_BRICK_STAIRS, w, Half.TOP));
                this.b.set(1, h + 6, z, VillageBuilder.stairs(Blocks.STONE_BRICK_STAIRS, e, Half.TOP));
                this.b.set(0, h + 6, z, rib ? Blocks.CHISELED_TUFF.defaultBlockState() : Blocks.STONE_BRICKS.defaultBlockState());
                for (int x = -2; x <= 2; x++) {
                    this.b.set(x, h + 7, z, this.wall(x, h + 7, z));
                }
                if (z == 4) {
                    this.b.set(0, h + 5, z, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
                }
            }
        }

        // ---- Zisterne -------------------------------------------------------------------------

        /**
         * Eine Zisterne: eine Halle (15 × 15) mit einem drei Blöcke tiefen Becken in der Mitte, aus dem vier Säulen mit
         * Bögen aufsteigen, vier weitere stehen an den Ecken des Umgangs; unter Wasser leuchten Meereslaternen, die Rinnen
         * der Kanäle münden ins Becken. Auf dem Grund liegt eine Truhe mit Opfergaben.
         */
        void cistern() {
            int exits = this.piece.data(0);
            int top = 9;
            for (int x = -7; x <= 7; x++) {
                for (int z = -7; z <= 7; z++) {
                    boolean wallCell = Math.abs(x) == 7 || Math.abs(z) == 7;
                    boolean pool = Math.abs(x) <= 4 && Math.abs(z) <= 4;
                    this.b.set(x, -4, z, this.wall(x, -4, z));
                    if (wallCell) {
                        for (int y = -3; y <= top; y++) {
                            this.b.set(x, y, z, this.wall(x, y, z));
                        }
                        continue;
                    }
                    if (pool) {
                        this.b.set(x, -3, z, Blocks.SMOOTH_STONE.defaultBlockState());
                        for (int y = -2; y <= 0; y++) {
                            this.b.set(x, y, z, this.water());
                        }
                    } else {
                        for (int y = -3; y <= -1; y++) {
                            this.b.set(x, y, z, this.wall(x, y, z));
                        }
                        boolean channel = x == 0 || z == 0;
                        this.b.set(x, 0, z, channel ? this.water() : this.walkway(Math.abs(x) == 5 || Math.abs(z) == 5 ? 1 : 2, 0, z + x));
                    }
                    for (int y = 1; y < top; y++) {
                        this.carve(x, y, z);
                    }
                    this.b.set(x, top, z, this.wall(x, top, z));
                }
            }
            // Ausgänge zu den Kanälen (sonst eine Blendnische)
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                boolean exit = (exits & bit(dir)) != 0;
                for (int s = -2; s <= 2; s++) {
                    int x = dir.getStepX() * 7 + (dir.getAxis() == Direction.Axis.Z ? s : 0);
                    int z = dir.getStepZ() * 7 + (dir.getAxis() == Direction.Axis.X ? s : 0);
                    for (int y = 1; y <= 4; y++) {
                        if (exit) {
                            this.b.set(x, y, z, Blocks.AIR.defaultBlockState());
                        } else if (Math.abs(s) <= 1 && y <= 3) {
                            this.b.set(x, y, z, Math.abs(s) == 0 ? Blocks.CHISELED_STONE_BRICKS.defaultBlockState()
                                    : Blocks.MOSSY_STONE_BRICKS.defaultBlockState());
                        }
                    }
                    if (exit) {
                        this.b.set(x, 0, z, s == 0 ? this.water() : this.walkway(2, 0, z));
                    }
                }
                if (exit) {
                    // ein Bogen über dem Ausgang
                    int x = dir.getStepX() * 6;
                    int z = dir.getStepZ() * 6;
                    Direction l = dir.getClockWise();
                    this.b.set(x + l.getStepX() * 2, 5, z + l.getStepZ() * 2, VillageBuilder.stairs(Blocks.TUFF_BRICK_STAIRS, l, Half.TOP));
                    this.b.set(x - l.getStepX() * 2, 5, z - l.getStepZ() * 2, VillageBuilder.stairs(Blocks.TUFF_BRICK_STAIRS, l.getOpposite(),
                            Half.TOP));
                    for (int s = -1; s <= 1; s++) {
                        this.b.set(x + l.getStepX() * s, 6, z + l.getStepZ() * s, Blocks.TUFF_BRICKS.defaultBlockState());
                    }
                }
            }
            // Säulen: vier im Becken, vier an den Ecken des Umgangs; mit Basis und Kapitell
            for (int[] c : new int[][] {{-2, -2}, {2, -2}, {-2, 2}, {2, 2}, {-5, -5}, {5, -5}, {-5, 5}, {5, 5}}) {
                for (int y = Math.abs(c[0]) == 2 ? -3 : 1; y < top; y++) {
                    BlockState s = y == 1 || y == 6 ? Blocks.CHISELED_STONE_BRICKS.defaultBlockState()
                            : y == 7 ? Blocks.TUFF_BRICKS.defaultBlockState() : Blocks.STONE_BRICKS.defaultBlockState();
                    this.b.set(c[0], y, c[1], s);
                }
            }
            // Bögen zwischen den Säulen im Becken (in beiden Richtungen), Scheitel auf Höhe 7
            for (int a : new int[] {-2, 2}) {
                this.b.set(-1, 7, a, VillageBuilder.stairs(Blocks.STONE_BRICK_STAIRS, Direction.WEST, Half.TOP));
                this.b.set(1, 7, a, VillageBuilder.stairs(Blocks.STONE_BRICK_STAIRS, Direction.EAST, Half.TOP));
                this.b.set(0, 7, a, Blocks.CHISELED_TUFF.defaultBlockState());
                this.b.set(a, 7, -1, VillageBuilder.stairs(Blocks.STONE_BRICK_STAIRS, Direction.NORTH, Half.TOP));
                this.b.set(a, 7, 1, VillageBuilder.stairs(Blocks.STONE_BRICK_STAIRS, Direction.SOUTH, Half.TOP));
                this.b.set(a, 7, 0, Blocks.CHISELED_TUFF.defaultBlockState());
                for (int i = -1; i <= 1; i++) {
                    this.b.set(i, 8, a, Blocks.TUFF_BRICKS.defaultBlockState());
                    this.b.set(a, 8, i, Blocks.TUFF_BRICKS.defaultBlockState());
                }
                this.b.set(0, 6, a, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
                this.b.set(a, 6, 0, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
            }
            // Licht: Meereslaternen unter Wasser (ein Kreuz), über der Mitte eine Laterne an langer Kette
            for (int[] c : new int[][] {{0, 0}, {0, -3}, {0, 3}, {-3, 0}, {3, 0}}) {
                this.b.set(c[0], -3, c[1], Blocks.SEA_LANTERN);
            }
            this.b.set(0, 8, 0, Blocks.IRON_CHAIN);
            this.b.set(0, 7, 0, Blocks.IRON_CHAIN);
            this.b.set(0, 6, 0, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
            // auf dem Grund eine Truhe mit Opfergaben
            this.b.chest(3, -2, -1, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH)
                    .setValue(BlockStateProperties.WATERLOGGED, true), ModLootTables.SITE_AQUEDUCT_CISTERN);
            for (int x = -6; x <= 6; x++) {
                for (int z = -6; z <= 6; z++) {
                    boolean edge = Math.abs(x) == 6 || Math.abs(z) == 6;
                    if (edge && this.b.chance(x, 1, z, 0.06f) && this.b.get(x, 1, z).isAir()) {
                        this.b.set(x, 1, z, Blocks.MOSS_CARPET);
                    }
                }
            }
        }

        // ---- Brunnenkammer ------------------------------------------------------------------------

        /**
         * Die Brunnenkammer: ein runder Raum mit Kuppel, in der Mitte ein Becken unter dem Brunnenschacht (mit Leiter),
         * Werkbank, Fässer und eine Truhe; oben über der Erde ein kleines Brunnenhaus mit Haspel.
         */
        void well() {
            int exits = this.piece.data(0);
            int shaft = this.piece.data(1);
            for (int x = -7; x <= 7; x++) {
                for (int z = -7; z <= 7; z++) {
                    double r = Math.sqrt(x * x + z * z);
                    if (r > 7.5) {
                        continue;
                    }
                    int dome = 5 + (int) Math.round(2 * Math.sqrt(Math.max(0, 1 - (r / 6.6) * (r / 6.6))));
                    this.b.set(x, -1, z, this.wall(x, -1, z));
                    if (r > 6.4) {
                        for (int y = 0; y <= dome + 1; y++) {
                            this.b.set(x, y, z, this.wall(x, y, z));
                        }
                        continue;
                    }
                    boolean basin = Math.abs(x) <= 1 && Math.abs(z) <= 1;
                    this.b.set(x, 0, z, basin ? this.water() : (Math.floorMod(x + z, 2) == 0 ? Blocks.POLISHED_ANDESITE.defaultBlockState()
                            : Blocks.STONE_BRICKS.defaultBlockState()));
                    for (int y = 1; y <= dome; y++) {
                        this.carve(x, y, z);
                    }
                    this.b.set(x, dome + 1, z, this.wall(x, dome + 1, z));
                }
            }
            // Brunnenrand um das Becken
            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    if (Math.abs(x) == 2 || Math.abs(z) == 2) {
                        this.b.set(x, 1, z, (x + z) % 2 == 0 ? Blocks.STONE_BRICK_WALL.defaultBlockState() : Blocks.STONE_BRICKS.defaultBlockState());
                    }
                }
            }
            this.b.set(0, 1, -2, Blocks.AIR.defaultBlockState());
            // Ausgänge
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                if ((exits & bit(dir)) == 0) {
                    continue;
                }
                for (int s = -2; s <= 2; s++) {
                    for (int d = 6; d <= 7; d++) {
                        int x = dir.getStepX() * d + (dir.getAxis() == Direction.Axis.Z ? s : 0);
                        int z = dir.getStepZ() * d + (dir.getAxis() == Direction.Axis.X ? s : 0);
                        for (int y = 1; y <= 4; y++) {
                            this.carve(x, y, z);
                        }
                        this.b.set(x, 0, z, s == 0 ? this.water() : this.walkway(2, 0, z));
                    }
                }
                // eine Rinne vom Becken zum Ausgang (unter dem Brunnenrand hindurch)
                for (int d = 2; d <= 5; d++) {
                    this.b.set(dir.getStepX() * d, 0, dir.getStepZ() * d, this.water());
                }
            }
            // Einrichtung
            this.b.set(-4, 1, -3, Blocks.CRAFTING_TABLE);
            this.b.set(-5, 1, -2, Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, Direction.UP));
            this.b.set(-5, 1, -1, Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, Direction.UP));
            this.b.set(-5, 2, -2, Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, Direction.UP));
            this.b.chest(4, 1, -3, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.WEST), ModLootTables.SITE_AQUEDUCT_ALCOVE);
            this.b.set(4, 1, 3, Blocks.CAULDRON);
            this.b.set(-4, 5, 4, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
            this.b.set(4, 5, -4, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
            if (shaft <= 0) {
                return;
            }
            // der Schacht hinauf, mit Leiter, und das Brunnenhaus
            int dome = 7;
            for (int y = dome; y <= shaft; y++) {
                for (int x = -2; x <= 2; x++) {
                    for (int z = -2; z <= 2; z++) {
                        boolean inner = Math.abs(x) <= 1 && Math.abs(z) <= 1;
                        this.b.set(x, y, z, inner ? Blocks.AIR.defaultBlockState() : this.wall(x, y, z));
                    }
                }
                this.b.set(0, y, -1, Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.SOUTH));
            }
            for (int y = 1; y < dome; y++) {
                this.b.set(0, y, -1, Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.SOUTH));
                this.b.set(0, y, -2, y == 1 ? Blocks.STONE_BRICKS.defaultBlockState() : this.wall(0, y, -2));
            }
            this.house(shaft);
        }

        /** Das Brunnenhaus über dem Schacht: Rand aus Ziegeln, vier Pfosten, ein Satteldach, eine Haspel mit Kette. */
        void house(int g) {
            for (int x = -3; x <= 3; x++) {
                for (int z = -3; z <= 3; z++) {
                    boolean inner = Math.abs(x) <= 1 && Math.abs(z) <= 1;
                    if (!inner) {
                        this.b.set(x, g, z, Math.abs(x) == 3 || Math.abs(z) == 3 ? Blocks.STONE_BRICKS.defaultBlockState()
                                : Blocks.POLISHED_ANDESITE.defaultBlockState());
                        for (int y = g - 3; y < g; y++) {
                            if (!this.b.get(x, y, z).isSolidRender()) {
                                this.b.set(x, y, z, this.wall(x, y, z));
                            }
                        }
                    }
                    for (int y = g + 1; y <= g + 6; y++) {
                        this.b.set(x, y, z, Blocks.AIR.defaultBlockState());
                    }
                }
            }
            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    if (Math.abs(x) == 2 || Math.abs(z) == 2) {
                        this.b.set(x, g + 1, z, Blocks.STONE_BRICK_WALL);
                    }
                }
            }
            // oben am Ende der Leiter eine Lücke im Brunnenrand
            this.b.set(0, g + 1, -2, Blocks.AIR.defaultBlockState());
            for (int[] c : new int[][] {{-2, -2}, {2, -2}, {-2, 2}, {2, 2}}) {
                for (int y = g + 2; y <= g + 4; y++) {
                    this.b.set(c[0], y, c[1], Blocks.SPRUCE_LOG);
                }
            }
            for (int x : new int[] {-2, 2}) {
                for (int y = g + 2; y <= g + 3; y++) {
                    this.b.set(x, y, 0, Blocks.SPRUCE_FENCE);
                }
            }
            // Satteldach längs x
            for (int x = -3; x <= 3; x++) {
                this.b.set(x, g + 5, -2, VillageBuilder.stairs(Blocks.SPRUCE_STAIRS, Direction.SOUTH, Half.BOTTOM));
                this.b.set(x, g + 5, 2, VillageBuilder.stairs(Blocks.SPRUCE_STAIRS, Direction.NORTH, Half.BOTTOM));
                this.b.set(x, g + 5, -1, Blocks.SPRUCE_PLANKS);
                this.b.set(x, g + 5, 1, Blocks.SPRUCE_PLANKS);
                this.b.set(x, g + 6, -1, VillageBuilder.stairs(Blocks.SPRUCE_STAIRS, Direction.SOUTH, Half.BOTTOM));
                this.b.set(x, g + 6, 1, VillageBuilder.stairs(Blocks.SPRUCE_STAIRS, Direction.NORTH, Half.BOTTOM));
                this.b.set(x, g + 6, 0, Blocks.SPRUCE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM));
            }
            for (int x = -3; x <= 3; x++) {
                this.b.set(x, g + 5, 0, Blocks.AIR.defaultBlockState());
            }
            // die Haspel: ein Balken quer über dem Schacht, eine Kette mit Eimer (Laterne) hinab
            for (int x = -2; x <= 2; x++) {
                this.b.set(x, g + 4, 0, Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X));
            }
            this.b.set(0, g + 3, 0, Blocks.IRON_CHAIN);
            this.b.set(0, g + 2, 0, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
        }
    }
}
