package io.github.sirschrolli.kingdomsatwar.worldgen.site;

import io.github.sirschrolli.kingdomsatwar.loot.ModLootTables;
import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.VillageBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BellAttachType;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

/**
 * Das verschüttete Kloster: Ein Bergsturz hat vor Jahrhunderten ein Kloster begraben. Aus dem Waldboden ragt nur noch die
 * Spitze des Glockenturms mit der alten Glocke; im Turm führt eine Leiter hinab in den Kreuzgang. Unten liegen der
 * Kreuzgang mit seinen Bögen, das Skriptorium mit Pulten und Regalen und die Kapelle – alles halb voll Geröll. Im Kies
 * stecken Scherben, Schmiedevorlagen und andere Funde der Mönche, die man mit dem Pinsel freilegen kann.
 *
 * <p>Eigene Koordinaten: Mitte des Kreuzgangs bei (0, 0, 0), y = 0 ist der Boden des Klosters (tief unter der Erde), die
 * Oberfläche liegt bei y = {@code data[0]}.
 */
final class BuriedAbbey {
    static final int DEPTH = 9;
    static final int TOWER_X = 9;
    static final int TOWER_Z = -9;

    private BuriedAbbey() {}

    static Optional<Structure.GenerationStub> plan(Structure.GenerationContext context, SiteTerrain terrain, long seed) {
        ChunkPos chunk = context.chunkPos();
        int x = chunk.getMiddleBlockX();
        int z = chunk.getMiddleBlockZ();
        int[] span = terrain.span(x, z, 9, 3);
        if (terrain.water(x, z) || span[1] - span[0] > 6) {
            return Optional.empty();
        }
        Rotation rotation = Rotation.values()[Math.floorMod((int) (seed >>> 7), 4)];
        // die Höhe des Waldbodens über dem Glockenturm (der Turm steht in der Nordostecke des Klosters)
        BlockPos tower = new BlockPos(TOWER_X, 0, TOWER_Z).rotate(rotation);
        int ground = terrain.ground(x + tower.getX(), z + tower.getZ());
        int floor = span[0] - DEPTH;
        if (floor < terrain.minY() + 10) {
            return Optional.empty();
        }
        BlockPos origin = new BlockPos(x, floor, z);
        BoundingBox box = new BoundingBox(x - 13, floor - 2, z - 13, x + 13, Math.max(ground, floor + 9) + 9, z + 13);
        return Optional.of(new Structure.GenerationStub(origin,
                builder -> builder.addPiece(new SitePiece(SiteKind.BURIED_ABBEY, 0, origin, rotation, seed, new int[] {ground - floor}, box))));
    }

    static void build(VillageBuilder b, SitePiece piece) {
        new Abbey(b, piece.data(0)).build();
    }

    private static final class Abbey {
        private final VillageBuilder b;
        private final int surface;

        Abbey(VillageBuilder b, int surface) {
            this.b = b;
            this.surface = surface;
        }

        BlockState wall(int x, int y, int z) {
            float n = this.b.noise(x, y, z);
            return n < 0.4f ? Blocks.STONE_BRICKS.defaultBlockState() : n < 0.62f ? Blocks.MOSSY_STONE_BRICKS.defaultBlockState()
                    : n < 0.78f ? Blocks.COBBLESTONE.defaultBlockState() : n < 0.9f ? Blocks.MOSSY_COBBLESTONE.defaultBlockState()
                    : Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
        }

        void build() {
            this.cloister();
            this.rooms();
            this.tower();
            this.rubble();
            this.lights();
            this.ruins();
        }

        /**
         * Der Kreuzgang: ein Hof (7 × 7, voll Geröll) mit einem Gang ringsum unter Rundbögen, außen die Mauern. Alles in
         * die Erde gegraben; über der Decke bleibt das Erdreich.
         */
        void cloister() {
            for (int x = -12; x <= 12; x++) {
                for (int z = -12; z <= 12; z++) {
                    int ring = Math.max(Math.abs(x), Math.abs(z));
                    for (int y = -1; y <= 6; y++) {
                        BlockState s;
                        if (y == -1) {
                            s = ring <= 3 ? Blocks.GRAVEL.defaultBlockState() : Blocks.STONE_BRICKS.defaultBlockState();
                        } else if (ring == 12 || y == 6) {
                            s = this.wall(x, y, z);
                        } else if (ring <= 3) {
                            // der Hof: oben offen gewesen, jetzt voller Geröll
                            s = y <= 3 ? Blocks.GRAVEL.defaultBlockState() : Blocks.DIRT.defaultBlockState();
                        } else if (ring == 4) {
                            // Bögen zum Hof: Pfeiler an jedem dritten Block
                            int along = Math.abs(x) == 4 ? z : x;
                            boolean pillar = Math.floorMod(along, 3) == 0 || Math.abs(x) == 4 && Math.abs(z) == 4;
                            s = pillar || y >= 4 ? this.wall(x, y, z) : y == 3 ? VillageBuilder.stairs(Blocks.STONE_BRICK_STAIRS,
                                    Math.floorMod(along, 3) == 1 ? (Math.abs(x) == 4 ? Direction.NORTH : Direction.WEST)
                                            : (Math.abs(x) == 4 ? Direction.SOUTH : Direction.EAST), Half.TOP) : Blocks.AIR.defaultBlockState();
                        } else if (ring == 7 && (!(Math.abs(x) <= 1 || Math.abs(z) <= 1) || y >= 4)) {
                            // die Mauer zwischen Gang und Räumen, in jeder Mitte ein Durchgang (drei hoch)
                            s = y == 4 && (Math.abs(x) <= 1 || Math.abs(z) <= 1) ? Blocks.CHISELED_STONE_BRICKS.defaultBlockState()
                                    : this.wall(x, y, z);
                        } else {
                            s = y == 0 ? (Math.floorMod(x + z, 2) == 0 ? Blocks.PACKED_MUD.defaultBlockState() : Blocks.MUD_BRICKS.defaultBlockState())
                                    : Blocks.AIR.defaultBlockState();
                        }
                        this.b.set(x, y, z, s);
                    }
                }
            }
        }

        /** Skriptorium (Norden), Kapelle (Süden), Zellen (Osten), Vorratskammer (Westen) zwischen Ring 8 und 11. */
        void rooms() {
            // Skriptorium
            for (int x = -9; x <= 9; x += 3) {
                this.b.set(x, 1, -10, Blocks.LECTERN.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.SOUTH));
                for (int y = 1; y <= 3; y++) {
                    this.b.set(x + 1, y, -11, Blocks.BOOKSHELF);
                }
            }
            this.b.chest(0, 1, -11, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH), ModLootTables.SITE_BURIED_ABBEY);
            // Kapelle: Altar, Kerzen, Bänke
            this.b.set(0, 1, 11, Blocks.CHISELED_STONE_BRICKS);
            this.b.set(-1, 1, 11, Blocks.CANDLE.defaultBlockState().setValue(net.minecraft.world.level.block.CandleBlock.CANDLES, 3));
            this.b.set(1, 1, 11, Blocks.CANDLE.defaultBlockState().setValue(net.minecraft.world.level.block.CandleBlock.CANDLES, 2));
            for (int x = -4; x <= 4; x++) {
                if (x == 0) {
                    continue;
                }
                this.b.set(x, 1, 9, VillageBuilder.stairs(Blocks.SPRUCE_STAIRS, Direction.SOUTH, Half.BOTTOM));
            }
            this.b.set(0, 2, 11, Blocks.DYED_CANDLE.pick(DyeColor.WHITE).defaultBlockState()
                    .setValue(net.minecraft.world.level.block.CandleBlock.CANDLES, 4));
            // Zellen: Strohlager
            for (int z = -9; z <= 9; z += 4) {
                this.b.set(10, 1, z, Blocks.HAY_BLOCK);
                this.b.set(11, 1, z, Blocks.CARPET.pick(DyeColor.BROWN));
            }
            // Vorratskammer
            this.b.set(-10, 1, -3, Blocks.BARREL);
            this.b.set(-11, 1, -3, Blocks.BARREL);
            this.b.set(-10, 1, 3, Blocks.DECORATED_POT);
            this.b.set(-11, 1, 2, Blocks.DECORATED_POT);
        }

        /**
         * Der Glockenturm in der Nordostecke: vom Kloster bis über die Erde (die Spitze mit der Glocke schaut heraus), innen
         * eine Leiter – der Weg hinab.
         */
        void tower() {
            int cx = TOWER_X;
            int cz = TOWER_Z;
            int top = Math.max(this.surface + 6, 12);
            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    boolean shell = Math.abs(x) == 2 || Math.abs(z) == 2;
                    boolean corner = Math.abs(x) == 2 && Math.abs(z) == 2;
                    for (int y = 0; y <= top; y++) {
                        if (shell) {
                            // die Glockenstube: auf jeder Seite ein Schallfenster, drei breit
                            boolean opening = y >= top - 2 && y < top && !corner && (Math.abs(x) == 2 ? Math.abs(z) <= 1 : Math.abs(x) <= 1);
                            BlockState wall = corner ? Blocks.STONE_BRICKS.defaultBlockState() : this.wall(cx + x, y, cz + z);
                            this.b.set(cx + x, y, cz + z, opening ? Blocks.AIR.defaultBlockState() : wall);
                        } else if (y == 0) {
                            this.b.set(cx + x, y, cz + z, Math.floorMod(x + z, 2) == 0 ? Blocks.PACKED_MUD.defaultBlockState()
                                    : Blocks.MUD_BRICKS.defaultBlockState());
                        } else if (y == top - 3 && !(x == 0 && z == -1)) {
                            // der Boden der Glockenstube, mit einer Luke für die Leiter
                            this.b.set(cx + x, y, cz + z, Blocks.SPRUCE_PLANKS.defaultBlockState());
                        } else {
                            this.b.set(cx + x, y, cz + z, Blocks.AIR.defaultBlockState());
                        }
                    }
                    // das Dach: eine steile Pyramide in zwei Stufen
                    if (Math.abs(x) == 2 || Math.abs(z) == 2) {
                        Direction in = Math.abs(x) == 2 ? (x < 0 ? Direction.EAST : Direction.WEST) : (z < 0 ? Direction.SOUTH : Direction.NORTH);
                        this.b.set(cx + x, top + 1, cz + z, VillageBuilder.stairs(Blocks.STONE_BRICK_STAIRS, in, Half.BOTTOM));
                    } else if (Math.abs(x) == 1 || Math.abs(z) == 1) {
                        this.b.set(cx + x, top + 1, cz + z, Blocks.STONE_BRICKS.defaultBlockState());
                        Direction in = Math.abs(x) == 1 && Math.abs(z) != 1 ? (x < 0 ? Direction.EAST : Direction.WEST)
                                : Math.abs(z) == 1 && Math.abs(x) != 1 ? (z < 0 ? Direction.SOUTH : Direction.NORTH) : (x < 0 ? Direction.EAST : Direction.WEST);
                        this.b.set(cx + x, top + 2, cz + z, VillageBuilder.stairs(Blocks.STONE_BRICK_STAIRS, in, Half.BOTTOM));
                    } else {
                        this.b.set(cx, top + 1, cz, Blocks.STONE_BRICKS.defaultBlockState());
                        this.b.set(cx, top + 2, cz, Blocks.STONE_BRICKS.defaultBlockState());
                    }
                }
            }
            this.b.set(cx, top + 3, cz, Blocks.STONE_BRICK_WALL);
            this.b.set(cx, top + 4, cz, Blocks.LIGHTNING_ROD.waxed().oxidized().defaultBlockState());
            // der Glockenbalken und die Glocke
            for (int x = -1; x <= 1; x++) {
                this.b.set(cx + x, top, cz, Blocks.SPRUCE_LOG.defaultBlockState()
                        .setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS, Direction.Axis.X));
            }
            this.b.set(cx, top - 1, cz, Blocks.BELL.defaultBlockState().setValue(BellBlock.ATTACHMENT, BellAttachType.CEILING)
                    .setValue(BellBlock.FACING, Direction.NORTH));
            // die Leiter an der Nordwand, vom Kloster bis in die Glockenstube
            for (int y = 1; y <= top - 3; y++) {
                this.b.set(cx, y, cz - 2, this.wall(cx, y, cz - 2));
                this.b.set(cx, y, cz - 1, Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.SOUTH));
            }
            // die Türen vom Turm in den Gang, mit Laternen daneben
            for (int y = 1; y <= 2; y++) {
                this.b.set(cx - 2, y, cz, Blocks.AIR.defaultBlockState());
                this.b.set(cx, y, cz + 2, Blocks.AIR.defaultBlockState());
            }
            this.b.set(cx + 1, 1, cz + 1, Blocks.LANTERN.defaultBlockState());
        }

        /**
         * Oben im Wald: rings um den Glockenturm Geröll (darauf wächst kein Baum, so bleibt eine kleine Lichtung), ein paar
         * herabgefallene Steine, und an drei Ecken ragen Stümpfe der alten Mauern aus dem Waldboden.
         */
        void ruins() {
            for (int x = TOWER_X - 6; x <= TOWER_X + 6; x++) {
                for (int z = TOWER_Z - 6; z <= TOWER_Z + 6; z++) {
                    int dx = x - TOWER_X;
                    int dz = z - TOWER_Z;
                    double d = Math.sqrt(dx * dx + dz * dz);
                    if (d > 5.6 || Math.abs(dx) <= 2 && Math.abs(dz) <= 2 || !SiteCave.column(this.b, x, z)) {
                        continue;
                    }
                    int y = this.surfaceAt(x, z);
                    if (y < 7) {
                        continue;
                    }
                    float n = this.b.noise(x, 90, z);
                    this.b.set(x, y, z, n < 0.4f ? Blocks.GRAVEL.defaultBlockState() : n < 0.65f ? Blocks.COBBLESTONE.defaultBlockState()
                            : n < 0.85f ? Blocks.MOSSY_COBBLESTONE.defaultBlockState() : Blocks.MOSSY_STONE_BRICKS.defaultBlockState());
                    if (this.b.chance(x, 91, z, 0.12f) && this.b.get(x, y + 1, z).isAir()) {
                        this.b.set(x, y + 1, z, this.b.chance(x, 92, z, 0.5f) ? Blocks.MOSSY_COBBLESTONE.defaultBlockState()
                                : Blocks.MOSSY_STONE_BRICK_SLAB.defaultBlockState());
                    }
                }
            }
            for (int[] c : new int[][] {{-12, -12, 1, 0}, {-12, 12, 0, -1}, {12, 12, -1, 0}}) {
                for (int i = 0; i <= 2; i++) {
                    int x = c[0] + c[2] * i;
                    int z = c[1] + c[3] * i;
                    if (!SiteCave.column(this.b, x, z)) {
                        continue;
                    }
                    int ground = this.surfaceAt(x, z);
                    int stub = ground + 2 - i;
                    for (int y = 7; y <= stub; y++) {
                        this.b.set(x, y, z, this.wall(x, y, z));
                    }
                }
            }
        }

        /** Oberster fester Block (eigene Höhe) in einer Säule des aktuellen Chunks. */
        int surfaceAt(int x, int z) {
            BlockPos p = this.b.world(x, 0, z);
            return this.b.level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.OCEAN_FLOOR, p.getX(), p.getZ()) - 1 - this.b.origin.getY();
        }

        /** Ein wenig Licht: Laternen an den Durchgängen, Kerzen auf den Pulten des Skriptoriums. */
        void lights() {
            for (int[] c : new int[][] {{0, -7}, {0, 7}, {-7, 0}, {7, 0}}) {
                this.b.set(c[0], 3, c[1], Blocks.LANTERN.defaultBlockState().setValue(net.minecraft.world.level.block.LanternBlock.HANGING, true));
            }
            for (int[] c : new int[][] {{-5, -5}, {5, 5}, {-5, 5}}) {
                this.b.set(c[0], 5, c[1], Blocks.IRON_CHAIN);
                this.b.set(c[0], 4, c[1], Blocks.LANTERN.defaultBlockState().setValue(net.minecraft.world.level.block.LanternBlock.HANGING, true));
            }
            for (int x = -9; x <= 6; x += 3) {
                if (x != 0) {
                    this.b.set(x, 1, -11, Blocks.CANDLE.defaultBlockState().setValue(net.minecraft.world.level.block.CandleBlock.CANDLES, 1 + Math.floorMod(x, 3)));
                }
            }
        }

        /** Geröll in Gängen und Räumen, darin verdächtiger Kies mit den Funden der Mönche. */
        void rubble() {
            for (int x = -11; x <= 11; x++) {
                for (int z = -11; z <= 11; z++) {
                    int ring = Math.max(Math.abs(x), Math.abs(z));
                    if (ring <= 3) {
                        // im Hof: Funde im Kies
                        if (this.b.chance(x, 1, z, 0.22f)) {
                            this.brushable(x, 1 + (int) (this.b.noise(x, 2, z) * 2), z, this.b.chance(x, 3, z, 0.1f));
                        }
                        continue;
                    }
                    if (!this.b.get(x, 1, z).isAir()) {
                        continue;
                    }
                    float n = this.b.noise(x, 41, z);
                    if (n < 0.18f) {
                        this.b.set(x, 1, z, Blocks.GRAVEL.defaultBlockState());
                        if (n < 0.05f) {
                            this.brushable(x, 1, z, false);
                        }
                    } else if (n < 0.24f) {
                        this.b.set(x, 1, z, Blocks.COBBLESTONE.defaultBlockState());
                    }
                }
            }
        }

        void brushable(int x, int y, int z, boolean rare) {
            BlockPos pos = this.b.world(x, y, z);
            if (!this.b.inside(pos)) {
                return;
            }
            this.b.set(x, y, z, Blocks.SUSPICIOUS_GRAVEL.defaultBlockState());
            if (this.b.level.getBlockEntity(pos) instanceof BrushableBlockEntity brushable) {
                brushable.setLootTable(rare ? BuiltInLootTables.TRAIL_RUINS_ARCHAEOLOGY_RARE : BuiltInLootTables.TRAIL_RUINS_ARCHAEOLOGY_COMMON,
                        pos.asLong());
            }
        }
    }
}
