package io.github.sirschrolli.kingdomsatwar.worldgen.site;

import io.github.sirschrolli.kingdomsatwar.loot.ModLootTables;
import io.github.sirschrolli.kingdomsatwar.registry.AnniBlocks;
import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.VillageBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * Die Fehlerzone: ein Stück Oberwelt, das kaputtgegangen ist. In der Mitte eine runde, eingeebnete Arena, deren Boden
 * aus den falschen Biomen zusammengewürfelt ist (Gras neben Schnee, Sand neben Myzel, hier und da Netherrack, Endstein,
 * fehlende Textur und Bildrauschen – manche Felder um einen Block verrutscht); darin steht auf einem Sockel aus fehlender
 * Textur der Fehlerkern, aus dem Anni der Troll geladen wird. Ringsum schweben herausgerissene Chunkfetzen (manche mit
 * einem Baum, der kopfüber nach unten wächst), in den Boden sind Schächte gestanzt, wo Chunks nicht geladen wurden, einzelne
 * Blöcke sind zu dünnen Säulen in die Höhe gestreckt, lose Pixel aus Beton hängen in der Luft, Bäume tragen Laub aus
 * fehlender Textur, zwei Zeilen Bildrauschen ziehen sich über den Boden – und auf einer Seite steht in großen Lettern
 * „404“. Der Fehler verliert sich nach außen hin. Am Rand der Arena liegt eine Truhe mit Fundstücken.
 *
 * <p>Eigene Koordinaten: Mitte bei (0, 0, 0), y = 0 ist die Höhe direkt über dem Boden der Arena.
 */
final class ErrorZone {
    /** Halbmesser der eingeebneten Arena und der ganzen Zone. */
    static final int ARENA = 13;
    static final int ZONE = 24;
    /** Boden je Gegend (Oberfläche, darunter): Gras, Sand, Schnee, roter Sand. */
    private static final Block[] HOME = {Blocks.GRASS_BLOCK, Blocks.SAND, Blocks.SNOW_BLOCK, Blocks.RED_SAND};
    private static final Block[] SUB = {Blocks.DIRT, Blocks.SANDSTONE, Blocks.DIRT, Blocks.RED_SANDSTONE};
    /** Boden aus den falschen Biomen (die letzten drei aus den falschen Welten – selten). */
    private static final Block[] FOREIGN = {Blocks.GRASS_BLOCK, Blocks.SAND, Blocks.SNOW_BLOCK, Blocks.RED_SAND, Blocks.MYCELIUM, Blocks.PODZOL,
            Blocks.MOSS_BLOCK, Blocks.MUD, Blocks.COARSE_DIRT, Blocks.TERRACOTTA, Blocks.PACKED_ICE, Blocks.NETHERRACK, Blocks.END_STONE,
            Blocks.WARPED_NYLIUM};
    /** Oberseite schwebender Fetzen (nichts, was herabfallen würde). */
    private static final Block[] TOPS = {Blocks.GRASS_BLOCK, Blocks.SANDSTONE, Blocks.SNOW_BLOCK, Blocks.MOSS_BLOCK, Blocks.PODZOL,
            Blocks.RED_SANDSTONE, Blocks.MYCELIUM};
    /** Die losen Pixel: Rot, Grün, Blau, Magenta, Cyan, Gelb. */
    private static final DyeColor[] PIXELS = {DyeColor.RED, DyeColor.LIME, DyeColor.BLUE, DyeColor.MAGENTA, DyeColor.CYAN, DyeColor.YELLOW};
    /** In die Höhe gestreckte Blöcke. */
    private static final Block[] STRETCHED = {Blocks.GRASS_BLOCK, Blocks.OAK_LOG, Blocks.BIRCH_LOG, Blocks.STONE, Blocks.MOSSY_COBBLESTONE,
            Blocks.BOOKSHELF};
    /** „404“ in einer Pixelschrift (je Ziffer 3 × 5, die oberste Zeile zuerst). */
    private static final String[] GLYPH_4 = {"#.#", "#.#", "###", "..#", "..#"};
    private static final String[] GLYPH_0 = {"###", "#.#", "#.#", "#.#", "###"};

    private ErrorZone() {}

    static Optional<Structure.GenerationStub> plan(Structure.GenerationContext context, SiteTerrain terrain, long seed) {
        ChunkPos chunk = context.chunkPos();
        int x = chunk.getMiddleBlockX();
        int z = chunk.getMiddleBlockZ();
        int[] span = terrain.span(x, z, ARENA, 4);
        if (span[1] - span[0] > 6) {
            return Optional.empty();
        }
        for (int[] d : new int[][] {{0, 0}, {ARENA, 0}, {-ARENA, 0}, {0, ARENA}, {0, -ARENA}, {9, 9}, {-9, 9}, {9, -9}, {-9, -9}}) {
            if (terrain.water(x + d[0], z + d[1])) {
                return Optional.empty();
            }
        }
        // die Arena liegt auf der mittleren Höhe (was höher ist, wird abgetragen, was tiefer ist, aufgefüllt)
        int ground = (span[0] + span[1] + 1) / 2;
        var biome = terrain.biome(x, ground, z);
        int home = biome.is(Biomes.DESERT) ? 1
                : biome.is(BiomeTags.IS_BADLANDS) ? 3
                : biome.is(Biomes.SNOWY_PLAINS) || biome.is(Biomes.SNOWY_TAIGA) || biome.is(Biomes.GROVE) ? 2 : 0;
        Rotation rotation = Rotation.values()[Math.floorMod((int) (seed >>> 9), 4)];
        BlockPos origin = new BlockPos(x, ground + 1, z);
        BoundingBox box = new BoundingBox(x - ZONE - 2, ground - 14, z - ZONE - 2, x + ZONE + 2, ground + 34, z + ZONE + 2);
        return Optional.of(new Structure.GenerationStub(origin,
                builder -> builder.addPiece(new SitePiece(SiteKind.ERROR_ZONE, 0, origin, rotation, seed, new int[] {home}, box))));
    }

    static void build(VillageBuilder b, SitePiece piece) {
        new Zone(b, Math.clamp(piece.data(0), 0, HOME.length - 1)).build();
    }

    private static final class Zone {
        private final VillageBuilder b;
        private final int home;
        private final BlockState missing = AnniBlocks.MISSING_BLOCK.get().defaultBlockState();
        private final BlockState noise = AnniBlocks.STATIC_BLOCK.get().defaultBlockState();

        Zone(VillageBuilder b, int home) {
            this.b = b;
            this.home = home;
        }

        void build() {
            // Zufall, der über mehrere Blöcke entscheidet, in fester Reihenfolge vorab (jeder Chunk erhält dasselbe)
            int[][] chunks = this.planChunks();
            int[][] pits = this.planPits();
            int[][] pillars = this.planPillars();
            int[][] pixels = this.planPixels();
            this.arena();
            this.surroundings();
            this.scanline(-19);
            this.scanline(21);
            this.text404(15);
            for (int[] p : pits) {
                this.pit(p[0], p[1], p[2], p[3], p[4]);
            }
            for (int[] p : pillars) {
                this.pillar(p[0], p[1], p[2], p[3]);
            }
            for (int[] c : chunks) {
                this.floatingChunk(c[0], c[1], c[2], c[3], c[4], c[5], c[6]);
            }
            for (int[] p : pixels) {
                this.b.set(p[0], p[1], p[2], Blocks.CONCRETE.pick(PIXELS[p[3]]).defaultBlockState());
            }
            this.pedestal();
            this.b.chest(8, 0, -8, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH), ModLootTables.SITE_ERROR_ZONE);
        }

        // ------------------------------------------------------------------ Planung

        /** Schwebende Chunkfetzen rund um die Arena: Lage, Höhe, Maße, Oberseite und ob ein Baum kopfüber daran hängt. */
        int[][] planChunks() {
            int n = 7 + this.b.random.nextInt(3);
            int[][] out = new int[n][];
            double turn = this.b.random.nextDouble() * Math.PI * 2.0;
            for (int i = 0; i < n; i++) {
                double a = turn + i * Math.PI * 2.0 / n + (this.b.random.nextDouble() - 0.5) * 0.5;
                double r = 16.0 + this.b.random.nextDouble() * 5.5;
                int w = 3 + this.b.random.nextInt(3);
                int d = 3 + this.b.random.nextInt(3);
                int h = 3 + this.b.random.nextInt(3);
                int y = 7 + this.b.random.nextInt(9);
                int top = this.b.random.nextInt(TOPS.length);
                int tree = this.b.random.nextInt(3) == 0 ? 1 : 0;
                out[i] = new int[] {(int) Math.round(Math.cos(a) * r), y, (int) Math.round(Math.sin(a) * r), w, d, h, top | tree << 8};
            }
            return out;
        }

        /** Schächte, wo Chunks nicht geladen wurden: Lage, Maße und Tiefe. */
        int[][] planPits() {
            int n = 3 + this.b.random.nextInt(2);
            int[][] out = new int[n][];
            double turn = this.b.random.nextDouble() * Math.PI * 2.0;
            for (int i = 0; i < n; i++) {
                double a = turn + i * Math.PI * 2.0 / n + (this.b.random.nextDouble() - 0.5) * 0.6;
                double r = 16.5 + this.b.random.nextDouble() * 4.0;
                out[i] = new int[] {(int) Math.round(Math.cos(a) * r), (int) Math.round(Math.sin(a) * r), 3 + this.b.random.nextInt(2),
                        3 + this.b.random.nextInt(3), 7 + this.b.random.nextInt(4)};
            }
            return out;
        }

        /** In die Höhe gestreckte Blöcke: Lage, Höhe und Stoff. */
        int[][] planPillars() {
            int n = 4 + this.b.random.nextInt(3);
            int[][] out = new int[n][];
            for (int i = 0; i < n; i++) {
                double a = this.b.random.nextDouble() * Math.PI * 2.0;
                double r = 15.0 + this.b.random.nextDouble() * 7.0;
                out[i] = new int[] {(int) Math.round(Math.cos(a) * r), (int) Math.round(Math.sin(a) * r), 10 + this.b.random.nextInt(13),
                        this.b.random.nextInt(STRETCHED.length)};
            }
            return out;
        }

        /** Lose Pixel in der Luft (über der Arena erst ab fünf Blöcken Höhe, damit Anni darunter Platz hat). */
        int[][] planPixels() {
            int n = 34 + this.b.random.nextInt(14);
            int[][] out = new int[n][];
            for (int i = 0; i < n; i++) {
                double a = this.b.random.nextDouble() * Math.PI * 2.0;
                double r = 4.0 + this.b.random.nextDouble() * (ZONE - 3.0);
                int y = (r < ARENA + 1 ? 6 : 4) + this.b.random.nextInt(r < ARENA + 1 ? 13 : 12);
                out[i] = new int[] {(int) Math.round(Math.cos(a) * r), y, (int) Math.round(Math.sin(a) * r), this.b.random.nextInt(PIXELS.length)};
            }
            return out;
        }

        // ------------------------------------------------------------------ Boden

        /** Ein Boden-Stoff aus den falschen Biomen für ein Feld (Felder von 4 × 4 Blöcken, gegeneinander versetzt). */
        BlockState patch(int x, int z, float strength) {
            int cx = Math.floorDiv(x + (Math.floorDiv(z, 4) % 2 == 0 ? 0 : 2), 4);
            int cz = Math.floorDiv(z, 4);
            float k = this.b.noise(cx, 911, cz);
            if (k > strength) {
                return HOME[this.home].defaultBlockState();
            }
            float pick = this.b.noise(cx, 912, cz);
            if (pick < 0.09f) {
                return this.missing;
            }
            if (pick < 0.17f) {
                return this.noise;
            }
            // Netherrack, Endstein und Wirrnyzel sind die letzten drei: seltener
            int common = FOREIGN.length - 3;
            int index = pick < 0.9f ? (int) ((pick - 0.17f) / 0.73f * common) : common + (int) ((pick - 0.9f) / 0.1f * 3);
            return FOREIGN[Math.clamp(index, 0, FOREIGN.length - 1)].defaultBlockState();
        }

        /** Ist das Feld um einen Block verrutscht (nach oben)? */
        boolean raised(int x, int z) {
            int cx = Math.floorDiv(x + (Math.floorDiv(z, 4) % 2 == 0 ? 0 : 2), 4);
            int cz = Math.floorDiv(z, 4);
            return this.b.noise(cx, 913, cz) < 0.14f;
        }

        /** Die Arena: eingeebnet, frei bis in große Höhe, Boden als Flickenteppich der falschen Biome. */
        void arena() {
            BlockState sub = SUB[this.home].defaultBlockState();
            for (int x = -ARENA - 2; x <= ARENA + 2; x++) {
                for (int z = -ARENA - 2; z <= ARENA + 2; z++) {
                    double r = Math.sqrt(x * x + z * z);
                    double edge = ARENA + (this.b.noise(Math.floorDiv(x, 3), 901, Math.floorDiv(z, 3)) - 0.5) * 2.5;
                    if (r > edge) {
                        continue;
                    }
                    for (int y = 0; y <= 26; y++) {
                        if (!this.b.get(x, y, z).isAir()) {
                            this.b.set(x, y, z, Blocks.AIR.defaultBlockState());
                        }
                    }
                    for (int y = -2; y >= -12; y--) {
                        BlockState s = this.b.get(x, y, z);
                        if (s.isSolidRender() && s.getFluidState().isEmpty() && !s.is(BlockTags.LEAVES)) {
                            break;
                        }
                        this.b.set(x, y, z, y > -5 ? sub : Blocks.STONE.defaultBlockState());
                    }
                    BlockState top = this.patch(x, z, 0.55f);
                    // verrutschte Felder – nicht um den Kern (dort setzt sich Anni zusammen) und nicht unter der Truhe
                    boolean lifted = r > 6.5 && r < edge - 1.5 && this.raised(x, z) && !(Math.abs(x - 8) <= 1 && Math.abs(z + 8) <= 1);
                    this.b.set(x, -1, z, lifted ? sub : top);
                    if (lifted) {
                        this.b.set(x, 0, z, top);
                    }
                }
            }
        }

        /**
         * Außerhalb der Arena verliert sich der Fehler: Boden-Felder aus den falschen Biomen, Laub aus fehlender Textur (oder mit
         * Löchern), hier und da ein Stamm aus Bildrauschen – je weiter draußen, desto seltener.
         */
        void surroundings() {
            for (int x = -ZONE; x <= ZONE; x++) {
                for (int z = -ZONE; z <= ZONE; z++) {
                    double r = Math.sqrt(x * x + z * z);
                    if (r <= ARENA + 1.5 || r > ZONE) {
                        continue;
                    }
                    double t = (r - ARENA - 1.5) / (ZONE - ARENA - 1.5);
                    float strength = (float) (0.55 * Math.pow(1.0 - t, 1.4));
                    int top = this.top(x, z);
                    if (top != Integer.MIN_VALUE) {
                        BlockState patch = this.patch(x, z, strength);
                        if (!patch.is(HOME[this.home])) {
                            this.b.set(x, top, z, patch);
                            BlockState above = this.b.get(x, top + 1, z);
                            if (!above.isAir() && !above.isSolid()) {
                                this.b.set(x, top + 1, z, Blocks.AIR.defaultBlockState());
                            }
                        }
                    }
                    for (int y = -6; y <= 24; y++) {
                        BlockState s = this.b.get(x, y, z);
                        if (s.is(BlockTags.LEAVES)) {
                            float k = this.b.noise(x, y, z);
                            if (k < strength * 0.5f) {
                                this.b.set(x, y, z, this.missing);
                            } else if (k < strength * 0.8f) {
                                this.b.set(x, y, z, Blocks.AIR.defaultBlockState());
                            }
                        } else if (s.is(BlockTags.LOGS) && this.b.noise(x, y + 40, z) < strength * 0.25f) {
                            this.b.set(x, y, z, this.noise);
                        }
                    }
                }
            }
        }

        /** Die oberste feste Bodenhöhe einer Säule (Laub und Stämme zählen nicht), sonst MIN_VALUE. */
        int top(int x, int z) {
            for (int y = 16; y >= -12; y--) {
                BlockState s = this.b.get(x, y, z);
                if (!s.isAir() && s.isSolid() && !s.is(BlockTags.LEAVES) && !s.is(BlockTags.LOGS) && s.getFluidState().isEmpty()) {
                    return y;
                }
            }
            return Integer.MIN_VALUE;
        }

        /** Eine Zeile Bildrauschen quer über den Boden (gestrichelt). */
        void scanline(int z) {
            for (int x = -ZONE + 1; x <= ZONE - 1; x++) {
                double r = Math.sqrt(x * x + z * z);
                if (r > ZONE || this.b.noise(Math.floorDiv(x, 3), 921, z) < 0.22f) {
                    continue;
                }
                int top = this.top(x, z);
                if (top != Integer.MIN_VALUE && top > -8 && top < 10) {
                    this.b.set(x, top, z, this.noise);
                }
            }
        }

        /** „404“ in großen Lettern aus fehlender Textur auf dem Boden, quer vor der Arena. */
        void text404(int z0) {
            String[][] glyphs = {GLYPH_4, GLYPH_0, GLYPH_4};
            for (int g = 0; g < 3; g++) {
                for (int row = 0; row < 5; row++) {
                    for (int col = 0; col < 3; col++) {
                        if (glyphs[g][row].charAt(col) != '#') {
                            continue;
                        }
                        int x = -6 + g * 5 + col;
                        int z = z0 + row;
                        int top = this.top(x, z);
                        if (top != Integer.MIN_VALUE && top > -8 && top < 10) {
                            this.b.set(x, top, z, this.missing);
                            BlockState above = this.b.get(x, top + 1, z);
                            if (!above.isAir() && !above.isSolid()) {
                                this.b.set(x, top + 1, z, Blocks.AIR.defaultBlockState());
                            }
                        }
                    }
                }
            }
        }

        // ------------------------------------------------------------------ Bauteile

        /** Der Sockel des Fehlerkerns: fehlende Textur auf Bildrauschen, darüber der Kern. */
        void pedestal() {
            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    if (Math.abs(x) == 2 && Math.abs(z) == 2) {
                        continue;
                    }
                    this.b.set(x, -1, z, this.noise);
                }
            }
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    this.b.set(x, 0, z, this.missing);
                }
            }
            this.b.set(0, 1, 0, AnniBlocks.ERROR_CORE.get().defaultBlockState());
            BlockPos core = this.b.world(0, 1, 0);
            if (this.b.inside(core)) {
                // den Block-Wächter gleich anlegen (er beginnt mit dem Warten)
                this.b.level.getBlockEntity(core);
            }
        }

        /** Ein Schacht, wo ein Chunk nicht geladen wurde: senkrecht ausgestanzt, unten Bildrauschen. */
        void pit(int cx, int cz, int w, int d, int depth) {
            for (int x = cx; x < cx + w; x++) {
                for (int z = cz; z < cz + d; z++) {
                    for (int y = 8; y > -depth; y--) {
                        this.b.set(x, y, z, Blocks.AIR.defaultBlockState());
                    }
                    this.b.set(x, -depth, z, this.noise);
                    this.b.set(x, -depth - 1, z, Blocks.CONCRETE.pick(DyeColor.BLACK).defaultBlockState());
                }
            }
        }

        /** Ein Block, in die Höhe gestreckt: eine dünne Säule aus immer demselben Block, oben ein Stück fehlende Textur. */
        void pillar(int x, int z, int height, int kind) {
            Block block = STRETCHED[kind];
            for (int y = -3; y < height; y++) {
                BlockState s = block.defaultBlockState();
                if (s.hasProperty(RotatedPillarBlock.AXIS)) {
                    s = s.setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
                }
                this.b.set(x, y, z, s);
            }
            this.b.set(x, height, z, this.missing);
        }

        /**
         * Ein herausgerissener Chunkfetzen, der in der Luft hängt: oben Boden, darunter Erde und Stein (mit Erz), die Unterseite
         * zerfranst. Manchmal wächst ein Baum kopfüber aus seiner Unterseite.
         */
        void floatingChunk(int cx, int y0, int cz, int w, int d, int h, int info) {
            int topKind = info & 0xFF;
            boolean tree = (info >> 8) != 0;
            int x0 = cx - w / 2;
            int z0 = cz - d / 2;
            for (int x = x0; x < x0 + w; x++) {
                for (int z = z0; z < z0 + d; z++) {
                    // die Unterseite zerfranst: manche Säulen reichen tiefer, manche weniger tief
                    int bottom = y0 - h + 1 + (this.b.noise(x, 931, z) < 0.3f ? 1 : 0) - (this.b.noise(x, 932, z) < 0.25f ? 1 : 0);
                    for (int y = bottom; y <= y0; y++) {
                        BlockState s;
                        if (y == y0) {
                            s = this.b.noise(x, 933, z) < 0.12f ? this.missing : TOPS[topKind].defaultBlockState();
                        } else if (y >= y0 - 1) {
                            s = topKind == 1 ? Blocks.SANDSTONE.defaultBlockState() : topKind == 5 ? Blocks.RED_SANDSTONE.defaultBlockState()
                                    : Blocks.DIRT.defaultBlockState();
                        } else {
                            float k = this.b.noise(x, y, z);
                            s = k < 0.06f ? Blocks.IRON_ORE.defaultBlockState() : k < 0.1f ? Blocks.REDSTONE_ORE.defaultBlockState()
                                    : k < 0.13f ? Blocks.COAL_ORE.defaultBlockState() : k < 0.2f ? this.noise
                                    : k < 0.5f ? Blocks.COBBLESTONE.defaultBlockState() : Blocks.STONE.defaultBlockState();
                        }
                        this.b.set(x, y, z, s);
                    }
                    // eine Kante fehlt: dort ist der Fetzen nur fehlende Textur
                    if (x == x0 && this.b.noise(cx, 934, cz) < 0.35f) {
                        for (int y = bottom; y <= y0; y++) {
                            this.b.set(x, y, z, this.missing);
                        }
                    }
                }
            }
            if (tree) {
                // ein Baum kopfüber: der Stamm hängt aus der Unterseite, die Krone unten
                int bx = cx;
                int bz = cz;
                int start = y0 - h;
                int trunk = 3 + (int) (this.b.noise(cx, 935, cz) * 2.0f);
                for (int y = start; y > start - trunk; y--) {
                    this.b.set(bx, y, bz, Blocks.OAK_LOG.defaultBlockState());
                }
                int crown = start - trunk;
                for (int x = -2; x <= 2; x++) {
                    for (int z = -2; z <= 2; z++) {
                        for (int y = -1; y <= 1; y++) {
                            int dist = Math.abs(x) + Math.abs(z) + Math.abs(y);
                            if (dist > 3 || (Math.abs(x) == 2 && Math.abs(z) == 2)) {
                                continue;
                            }
                            if (x == 0 && z == 0 && y >= 0) {
                                continue;
                            }
                            BlockState leaves = this.b.noise(bx + x, crown + y, bz + z) < 0.15f ? this.missing
                                    : Blocks.OAK_LEAVES.defaultBlockState().setValue(net.minecraft.world.level.block.LeavesBlock.PERSISTENT, true);
                            this.b.set(bx + x, crown + y, bz + z, leaves);
                        }
                    }
                }
                this.b.set(bx, crown, bz, Blocks.OAK_LOG.defaultBlockState());
            }
        }
    }
}
