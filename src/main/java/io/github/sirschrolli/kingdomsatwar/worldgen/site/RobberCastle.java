package io.github.sirschrolli.kingdomsatwar.worldgen.site;

import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;

import io.github.sirschrolli.kingdomsatwar.loot.ModLootTables;
import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.VillageBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * Die Raubritterburg: Mitten im dunklen Wald hat ein Raubritter eine Burg auf einen aufgeschütteten Hügel gesetzt. Ein
 * trockener Graben läuft rings um den Hügel, über ihn führt eine Brücke zum Torhaus mit hochgezogenem Fallgitter. Die
 * Ringmauer mit Wehrgang und Zinnen verbindet vier Rundtürme (die beiden nördlichen mit spitzen Schieferdächern); im Norden
 * steht der Bergfried mit Kammern übereinander und einem Verlies, in dem Hellegeister gefangen sind. Im Hof der Palas mit
 * Tafel, Kamin und Thron, ein Brunnen, Übungspuppen und das Lager der Knechte. Diener, Hexer und Armbrustschützen des
 * Raubritters hausen hier – und die Truhen sind voll mit Raubgut.
 *
 * <p>Eigene Koordinaten: Mitte des Burghofs bei (0, 0, 0); y = 0 ist die oberste Schicht des Hofs (man steht auf y = 1).
 * Das Tor liegt im Süden (+z), der Bergfried im Norden (−z).
 */
final class RobberCastle {
    static final int REACH = 32;

    private RobberCastle() {}

    static Optional<Structure.GenerationStub> plan(Structure.GenerationContext context, SiteTerrain terrain, long seed) {
        ChunkPos chunk = context.chunkPos();
        int x = chunk.getMiddleBlockX();
        int z = chunk.getMiddleBlockZ();
        if (terrain.water(x, z)) {
            return Optional.empty();
        }
        int[] span = terrain.span(x, z, 16, 4);
        int[] outer = terrain.span(x, z, 26, 13);
        if (span[1] - span[0] > 9 || outer[1] - outer[0] > 16) {
            return Optional.empty();
        }
        int base = (span[0] + span[1]) / 2 + 4;
        Rotation rotation = Rotation.values()[Math.floorMod((int) (seed >>> 7), 4)];
        BlockPos origin = new BlockPos(x, base, z);
        return Optional.of(new Structure.GenerationStub(origin,
                builder -> builder.addPiece(new SitePiece(SiteKind.ROBBER_CASTLE, 0, origin, rotation, seed, new int[0], box(origin)))));
    }

    static BoundingBox box(BlockPos o) {
        return new BoundingBox(o.getX() - REACH, o.getY() - 16, o.getZ() - REACH, o.getX() + REACH, o.getY() + 34, o.getZ() + REACH);
    }

    static void build(VillageBuilder b, SitePiece piece) {
        new Castle(b).build();
    }

    private static final class Castle {
        /** Mitten der vier Rundtürme. */
        private static final int[][] TOWERS = {{10, -10}, {10, 10}, {-10, 10}, {-10, -10}};
        private final VillageBuilder b;

        Castle(VillageBuilder b) {
            this.b = b;
        }

        void build() {
            this.motte();
            this.curtain();
            for (int i = 0; i < TOWERS.length; i++) {
                this.tower(TOWERS[i][0], TOWERS[i][1], TOWERS[i][1] < 0);
            }
            this.gatehouse();
            this.bridge();
            this.keep();
            this.dungeon();
            this.hall();
            this.barracks();
            this.yard();
            this.garrison();
        }

        // =================================================================================
        // Hilfen
        // =================================================================================

        static BlockState state(Block block) {
            return block.defaultBlockState();
        }

        static double r(double x, double z) {
            return Math.sqrt(x * x + z * z);
        }

        BlockState stone(int x, int y, int z) {
            float n = this.b.noise(x, y, z);
            return n < 0.58f ? state(Blocks.STONE_BRICKS) : n < 0.74f ? state(Blocks.MOSSY_STONE_BRICKS)
                    : n < 0.84f ? state(Blocks.CRACKED_STONE_BRICKS) : n < 0.95f ? state(Blocks.COBBLESTONE) : state(Blocks.ANDESITE);
        }

        BlockState rock(int x, int y, int z) {
            float n = this.b.noise(x, y, z);
            return n < 0.45f ? state(Blocks.STONE) : n < 0.65f ? state(Blocks.ANDESITE) : n < 0.85f ? state(Blocks.COBBLESTONE)
                    : state(Blocks.MOSSY_COBBLESTONE);
        }

        /** Natürliche Bodenhöhe (eigene Höhe des obersten Bodenblocks) in einer Säule des aktuellen Chunks. */
        int natural(int x, int z) {
            BlockPos p = this.b.world(x, 0, z);
            return this.b.level.getHeight(Heightmap.Types.OCEAN_FLOOR, p.getX(), p.getZ()) - 1 - this.b.origin.getY();
        }

        void lantern(int x, int y, int z, boolean hanging) {
            this.b.set(x, y, z, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, hanging));
        }

        void flag(int x, int y, int z) {
            for (int h = 0; h < 3; h++) {
                this.b.set(x, y + h, z, state(Blocks.DARK_OAK_FENCE));
            }
            this.b.set(x + 1, y + 2, z, Blocks.WOOL.pick(DyeColor.RED).defaultBlockState());
            this.b.set(x + 2, y + 2, z, Blocks.WOOL.pick(DyeColor.RED).defaultBlockState());
            this.b.set(x + 1, y + 1, z, Blocks.WOOL.pick(DyeColor.BLACK).defaultBlockState());
        }

        // =================================================================================
        // Gelände
        // =================================================================================

        /**
         * Der Burghügel: oben eben (der Hof aus festgetretener Erde, Kies und Pflaster, kein Boden, auf dem Bäume wachsen),
         * die Flanke aus Fels, ringsum ein trockener Graben mit Pfützen, außen eine Böschung zurück auf den Waldboden.
         */
        void motte() {
            for (int x = -REACH + 2; x <= REACH - 2; x++) {
                for (int z = -REACH + 2; z <= REACH - 2; z++) {
                    if (!SiteCave.column(this.b, x, z)) {
                        continue;
                    }
                    double r = r(x, z);
                    if (r > 26) {
                        continue;
                    }
                    int g = this.natural(x, z);
                    int top;
                    if (r <= 15.5) {
                        top = 0;
                    } else if (r <= 17.5) {
                        top = (int) Math.round(-(r - 15.5) * 4);
                    } else if (r <= 20.5) {
                        top = -8;
                    } else {
                        double t = (r - 20.5) / 5.5;
                        t = t * t * (3 - 2 * t);
                        top = (int) Math.round(-8 + (g + 8) * t);
                    }
                    int from = Math.min(g, top) - 3;
                    for (int y = from; y <= top; y++) {
                        BlockState s;
                        if (r <= 15.5) {
                            s = y == top ? this.yardFloor(x, z) : y >= top - 2 ? state(Blocks.DIRT) : this.rock(x, y, z);
                        } else if (r <= 17.5) {
                            s = this.rock(x, y, z);
                        } else if (r <= 20.5) {
                            float n = this.b.noise(x, 3, z);
                            s = y < top ? this.rock(x, y, z) : n < 0.07f && r > 18.2 && r < 19.8 ? state(Blocks.WATER)
                                    : n < 0.6f ? state(Blocks.GRAVEL) : state(Blocks.PACKED_MUD);
                        } else {
                            s = y == top ? state(Blocks.GRASS_BLOCK) : y >= top - 2 ? state(Blocks.DIRT) : this.rock(x, y, z);
                        }
                        this.b.set(x, y, z, s);
                    }
                    for (int y = top + 1; y <= Math.max(g, top) + 2; y++) {
                        if (!this.b.get(x, y, z).isAir()) {
                            this.b.set(x, y, z, Blocks.AIR.defaultBlockState());
                        }
                    }
                }
            }
        }

        BlockState yardFloor(int x, int z) {
            float n = this.b.noise(x, 1, z);
            double s = SiteCave.smooth(this.b, x, 0, z, 5, 5);
            if (Math.abs(x) <= 1 && z > -6) {
                return n < 0.7f ? state(Blocks.COBBLESTONE) : state(Blocks.MOSSY_COBBLESTONE);
            }
            return s < 0.4 ? state(Blocks.GRAVEL) : n < 0.6f ? state(Blocks.DIRT_PATH) : n < 0.85f ? state(Blocks.PACKED_MUD) : state(Blocks.COBBLESTONE);
        }

        // =================================================================================
        // Mauern und Türme
        // =================================================================================

        /** Die Ringmauer: zwei Blöcke dick, sieben hoch, oben der Wehrgang hinter einer Brustwehr mit Zinnen. */
        void curtain() {
            for (int x = -15; x <= 15; x++) {
                for (int z = -15; z <= 15; z++) {
                    double r = r(x, z);
                    if (r < 12.5 || r >= 14.5) {
                        continue;
                    }
                    for (int y = 1; y <= 7; y++) {
                        this.b.set(x, y, z, y == 7 && r < 13.5 ? state(Blocks.STONE_BRICKS) : this.stone(x, y, z));
                    }
                    if (r >= 13.5) {
                        this.b.set(x, 8, z, this.stone(x, 8, z));
                        double arc = Math.atan2(z, x) * 14;
                        if (Math.floorMod((int) Math.round(arc / 1.5), 2) == 0) {
                            this.b.set(x, 9, z, this.stone(x, 9, z));
                        }
                        // ein Sockel, der nach außen ausläuft
                        this.b.set(x, 0, z, this.rock(x, 0, z));
                    }
                }
            }
        }

        /** Ein Rundturm auf der Mauer: Leiter innen, Türen zum Hof und zum Wehrgang, oben Zinnen oder ein spitzes Dach. */
        void tower(int cx, int cz, boolean roof) {
            for (int x = -4; x <= 4; x++) {
                for (int z = -4; z <= 4; z++) {
                    double d = r(x, z);
                    if (d > 3.3) {
                        continue;
                    }
                    boolean wall = d > 1.9;
                    double ring = r(cx + x, cz + z);
                    this.foundation(cx + x, cz + z, -1);
                    for (int y = 1; y <= 12; y++) {
                        BlockState s;
                        if (wall) {
                            boolean slit = (y == 3 || y == 4 || y == 9 || y == 10) && (x == 0 && Math.abs(z) == 3 || z == 0 && Math.abs(x) == 3)
                                    && ring > r(cx, cz);
                            // wo der Wehrgang auf den Turm trifft, ein Durchgang
                            boolean walk = (y == 8 || y == 9) && ring >= 12.5 && ring < 13.5;
                            s = slit || walk ? Blocks.AIR.defaultBlockState() : this.stone(cx + x, y, cz + z);
                        } else {
                            s = y == 7 || y == 12 ? state(Blocks.SPRUCE_PLANKS) : Blocks.AIR.defaultBlockState();
                        }
                        this.b.set(cx + x, y, cz + z, s);
                    }
                    this.b.set(cx + x, 0, cz + z, wall ? this.rock(cx + x, 0, cz + z) : state(Blocks.SPRUCE_PLANKS));
                    if (roof) {
                        continue;
                    }
                    // Zinnenkranz
                    if (d > 2.4) {
                        this.b.set(cx + x, 13, cz + z, this.stone(cx + x, 13, cz + z));
                        if (Math.floorMod(x + z, 2) == 0) {
                            this.b.set(cx + x, 14, cz + z, this.stone(cx + x, 14, cz + z));
                        }
                    }
                }
            }
            if (roof) {
                // ein spitzes Kegeldach aus Schiefer
                for (int x = -4; x <= 4; x++) {
                    for (int z = -4; z <= 4; z++) {
                        double d = r(x, z);
                        if (d > 4.0) {
                            continue;
                        }
                        int top = 13 + (int) Math.round((4.0 - d) * 1.7);
                        for (int y = 13; y < top; y++) {
                            this.b.set(cx + x, y, cz + z, state(Blocks.DEEPSLATE_TILES));
                        }
                        Direction in = Math.abs(x) >= Math.abs(z) ? (x > 0 ? Direction.WEST : Direction.EAST) : (z > 0 ? Direction.NORTH : Direction.SOUTH);
                        this.b.set(cx + x, top, cz + z, x == 0 && z == 0 ? state(Blocks.DEEPSLATE_TILES)
                                : VillageBuilder.stairs(Blocks.DEEPSLATE_TILE_STAIRS, in, Half.BOTTOM));
                    }
                }
                this.flag(cx, 21, cz);
            }
            // Leiter an der Innenwand zur Hofseite, Türen
            int sx = -Integer.signum(cx);
            int sz = -Integer.signum(cz);
            int lx = cx - sx;
            this.b.ladder(lx, 1, 12, cz, sx > 0 ? Direction.EAST : Direction.WEST);
            // Tür zum Hof (in der Wand zur Mitte hin)
            int dz = cz + sz * 2;
            this.b.door(cx, 1, dz, Blocks.SPRUCE_DOOR, sz > 0 ? Direction.NORTH : Direction.SOUTH);
            this.b.set(cx, 0, dz + sz, this.yardFloor(cx, dz + sz));
            this.lantern(cx + sx, 6, cz, true);
            this.lantern(cx + sx, 11, cz + sz, true);
        }

        /** Unter einem Bauteil bis auf festen Grund auffüllen (die Türme stehen am Hang des Hügels). */
        void foundation(int x, int z, int from) {
            if (!SiteCave.column(this.b, x, z)) {
                return;
            }
            for (int y = from; y >= -16; y--) {
                if (this.b.get(x, y, z).isSolidRender()) {
                    return;
                }
                this.b.set(x, y, z, this.rock(x, y, z));
            }
        }

        /** Das Torhaus im Süden: zwei Flankentürme, dazwischen die Durchfahrt mit Fallgitter, darüber die Torstube. */
        void gatehouse() {
            for (int x = -5; x <= 5; x++) {
                for (int z = 11; z <= 16; z++) {
                    int ax = Math.abs(x);
                    boolean passage = ax <= 1;
                    boolean shell = ax == 5 || z == 11 || z == 16 || ax == 2;
                    this.foundation(x, z, -1);
                    for (int y = 1; y <= 11; y++) {
                        BlockState s;
                        if (passage && y <= 4) {
                            s = Blocks.AIR.defaultBlockState();
                        } else if (passage && y == 5) {
                            s = x == 0 ? state(Blocks.STONE_BRICKS) : VillageBuilder.stairs(Blocks.STONE_BRICK_STAIRS, x > 0 ? Direction.EAST : Direction.WEST,
                                    Half.TOP);
                        } else if ((y == 6 || y == 10) && !shell) {
                            s = state(Blocks.SPRUCE_PLANKS);
                        } else if (shell) {
                            boolean slit = (y == 8) && (z == 16 || z == 11) && (ax == 3 || ax == 4) || y == 3 && ax == 5 && z == 13;
                            s = slit ? Blocks.AIR.defaultBlockState() : this.stone(x, y, z);
                        } else {
                            s = Blocks.AIR.defaultBlockState();
                        }
                        this.b.set(x, y, z, s);
                    }
                    this.b.set(x, 0, z, passage ? state(Blocks.COBBLESTONE) : this.rock(x, 0, z));
                    // Zinnen
                    if (ax >= 2 && (ax == 5 || z == 11 || z == 16)) {
                        this.b.set(x, 11, z, this.stone(x, 11, z));
                        if (Math.floorMod(x + z, 2) == 0) {
                            this.b.set(x, 12, z, this.stone(x, 12, z));
                        }
                    } else {
                        this.b.set(x, 11, z, Blocks.AIR.defaultBlockState());
                    }
                }
            }
            // das hochgezogene Fallgitter
            for (int x = -1; x <= 1; x++) {
                this.b.set(x, 4, 15, state(Blocks.IRON_BARS));
            }
            // Leiter in die Torstube, Banner über dem Tor
            this.b.ladder(-4, 1, 10, 15, Direction.NORTH);
            this.b.door(-2, 1, 13, Blocks.SPRUCE_DOOR, Direction.WEST);
            this.b.set(-1, 7, 17, Blocks.WALL_BANNER.pick(DyeColor.RED).defaultBlockState().setValue(WallBannerBlock.FACING, Direction.SOUTH));
            this.b.set(1, 7, 17, Blocks.WALL_BANNER.pick(DyeColor.BLACK).defaultBlockState().setValue(WallBannerBlock.FACING, Direction.SOUTH));
            this.lantern(0, 4, 12, true);
            this.flag(-4, 12, 13);
            this.flag(4, 12, 13);
        }

        /** Die Brücke über den Graben, gestützt von einem Joch, dahinter eine Rampe hinab auf den Waldboden. */
        void bridge() {
            for (int z = 17; z <= 21; z++) {
                for (int x = -2; x <= 2; x++) {
                    if (Math.abs(x) <= 1) {
                        this.b.set(x, 0, z, state(Blocks.SPRUCE_PLANKS));
                        this.b.set(x, 1, z, Blocks.AIR.defaultBlockState());
                    } else {
                        this.b.set(x, 0, z, state(Blocks.SPRUCE_LOG).setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z));
                        this.b.set(x, 1, z, state(Blocks.SPRUCE_FENCE));
                    }
                }
            }
            for (int x : new int[] {-2, 2}) {
                for (int y = -9; y <= -1; y++) {
                    this.b.set(x, y, 19, state(Blocks.SPRUCE_LOG));
                }
            }
            for (int x = -2; x <= 2; x++) {
                this.b.set(x, -1, 19, state(Blocks.SPRUCE_LOG).setValue(RotatedPillarBlock.AXIS, Direction.Axis.X));
            }
            // die Rampe: eine Stufe je Block, bis sie den Waldboden erreicht
            for (int z = 22; z <= REACH - 2; z++) {
                if (!SiteCave.column(this.b, 0, z) && !SiteCave.column(this.b, -1, z) && !SiteCave.column(this.b, 1, z)) {
                    continue;
                }
                int y = -(z - 21);
                int g = this.natural(0, z);
                if (y < g - 1) {
                    break;
                }
                for (int x = -1; x <= 1; x++) {
                    this.b.set(x, y, z, VillageBuilder.stairs(Blocks.COBBLESTONE_STAIRS, Direction.NORTH, Half.BOTTOM));
                    for (int d = y - 1; d >= y - 12; d--) {
                        if (this.b.get(x, d, z).isSolidRender()) {
                            break;
                        }
                        this.b.set(x, d, z, this.rock(x, d, z));
                    }
                    for (int d = y + 1; d <= y + 4; d++) {
                        this.b.set(x, d, z, Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }

        /**
         * Der Bergfried im Norden, in die Mauer gesetzt: Vorratskammer, Wachstube (mit Türen zum Wehrgang), die Kammer des
         * Raubritters, eine Turmstube; oben eine Plattform mit Zinnen und einem Türmchen.
         */
        void keep() {
            for (int x = -4; x <= 4; x++) {
                for (int z = -15; z <= -7; z++) {
                    boolean wall = Math.abs(x) == 4 || z == -15 || z == -7;
                    this.foundation(x, z, -7);
                    for (int y = 0; y <= 22; y++) {
                        BlockState s;
                        if (wall) {
                            boolean slit = (y == 9 || y == 10 || y == 15 || y == 16 || y == 20)
                                    && (x == 0 && (z == -15 || z == -7) || z == -11 && Math.abs(x) == 4);
                            s = slit ? state(Blocks.IRON_BARS) : this.stone(x, y, z);
                        } else {
                            s = y == 0 ? state(Blocks.COBBLESTONE) : y == 7 || y == 13 || y == 18 ? state(Blocks.SPRUCE_PLANKS)
                                    : y == 22 ? state(Blocks.STONE_BRICKS) : Blocks.AIR.defaultBlockState();
                        }
                        this.b.set(x, y, z, s);
                    }
                    // Kragsteine unter der Brustwehr
                    if (wall) {
                        this.b.set(x, 23, z, state(Blocks.STONE_BRICK_WALL));
                        if (Math.floorMod(x + z, 2) == 0) {
                            this.b.set(x, 24, z, state(Blocks.STONE_BRICK_WALL));
                        }
                    }
                }
            }
            for (int i = -4; i <= 4; i++) {
                this.b.set(i, 21, -16, VillageBuilder.stairs(Blocks.STONE_BRICK_STAIRS, Direction.SOUTH, Half.TOP));
                this.b.set(i, 21, -6, VillageBuilder.stairs(Blocks.STONE_BRICK_STAIRS, Direction.NORTH, Half.TOP));
                this.b.set(-5, 21, -11 + i, VillageBuilder.stairs(Blocks.STONE_BRICK_STAIRS, Direction.EAST, Half.TOP));
                this.b.set(5, 21, -11 + i, VillageBuilder.stairs(Blocks.STONE_BRICK_STAIRS, Direction.WEST, Half.TOP));
            }
            // Leiter an der Ostwand durch alle Stockwerke
            this.b.ladder(3, 1, 22, -8, Direction.WEST);
            // Türen: unten zum Hof, auf dem Wehrgang zu beiden Seiten
            this.b.door(0, 1, -7, Blocks.SPRUCE_DOOR, Direction.NORTH);
            this.b.door(-4, 8, -12, Blocks.SPRUCE_DOOR, Direction.EAST);
            this.b.door(4, 8, -12, Blocks.SPRUCE_DOOR, Direction.WEST);
            // das Türmchen auf der Plattform
            for (int x = -2; x <= 2; x++) {
                for (int z = -13; z <= -9; z++) {
                    boolean shell = Math.abs(x) == 2 || z == -13 || z == -9;
                    for (int y = 23; y <= 25; y++) {
                        this.b.set(x, y, z, shell && !(y == 23 || y == 24) ? state(Blocks.SPRUCE_PLANKS)
                                : shell && !(x == 0 && z == -9) ? state(Blocks.STRIPPED_SPRUCE_LOG) : Blocks.AIR.defaultBlockState());
                    }
                }
            }
            this.b.set(0, 23, -9, Blocks.AIR.defaultBlockState());
            this.b.set(0, 24, -9, Blocks.AIR.defaultBlockState());
            this.b.hipRoof(-3, -14, 3, -8, 26, Blocks.DEEPSLATE_TILE_STAIRS, state(Blocks.DEEPSLATE_TILES));
            this.flag(0, 30, -11);
            this.b.set(3, 23, -8, Blocks.SPRUCE_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.HALF, Half.BOTTOM).setValue(TrapDoorBlock.OPEN, true)
                    .setValue(TrapDoorBlock.FACING, Direction.WEST));
            // Vorratskammer
            for (int x = -3; x <= -1; x++) {
                this.b.set(x, 1, -14, Blocks.BARREL);
            }
            this.b.set(-3, 2, -14, Blocks.BARREL);
            this.b.set(-3, 1, -13, state(Blocks.HAY_BLOCK));
            this.b.chest(1, 1, -14, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH), ModLootTables.SITE_ROBBER_CASTLE);
            this.lantern(0, 6, -11, true);
            // die Luke ins Verlies
            this.b.set(-3, 0, -8, Blocks.SPRUCE_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.HALF, Half.TOP)
                    .setValue(TrapDoorBlock.FACING, Direction.NORTH));
            // Wachstube
            this.b.set(-3, 8, -14, state(Blocks.SMITHING_TABLE));
            this.b.set(-2, 8, -14, state(Blocks.GRINDSTONE));
            this.b.set(-3, 8, -13, state(Blocks.ANVIL));
            this.b.set(1, 8, -14, Blocks.BARREL);
            this.b.set(2, 8, -14, Blocks.BARREL);
            this.lantern(0, 12, -11, true);
            // Kammer des Raubritters
            this.b.bed(-2, 14, -13, Blocks.BED.pick(DyeColor.RED), Direction.NORTH);
            this.b.set(-3, 14, -14, Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.CANDLES, 3).setValue(CandleBlock.LIT, true));
            this.b.chest(1, 14, -14, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH), ModLootTables.SITE_ROBBER_CASTLE_HOARD);
            for (int x = -3; x <= 1; x++) {
                for (int z = -11; z <= -9; z++) {
                    this.b.set(x, 14, z, Blocks.CARPET.pick(DyeColor.RED).defaultBlockState());
                }
            }
            this.b.set(0, 16, -14, Blocks.WALL_BANNER.pick(DyeColor.BLACK).defaultBlockState().setValue(WallBannerBlock.FACING, Direction.SOUTH));
            this.lantern(-1, 17, -11, true);
            // Turmstube
            this.b.set(-3, 19, -14, state(Blocks.CARTOGRAPHY_TABLE));
            this.b.set(-2, 19, -14, Blocks.BARREL);
            this.lantern(0, 21, -11, true);
        }

        /** Das Verlies unter dem Bergfried: zwei vergitterte Zellen mit gefangenen Hellegeistern, Ketten, eine Leiter hinauf. */
        void dungeon() {
            for (int x = -4; x <= 4; x++) {
                for (int z = -15; z <= -7; z++) {
                    boolean wall = Math.abs(x) == 4 || z == -15 || z == -7;
                    for (int y = -6; y <= -1; y++) {
                        this.b.set(x, y, z, wall || y == -6 ? (this.b.chance(x, y, z, 0.3f) ? state(Blocks.MOSSY_COBBLESTONE) : state(Blocks.COBBLESTONE))
                                : Blocks.AIR.defaultBlockState());
                    }
                }
            }
            // Zellen im Norden, vorn Gitter
            for (int y = -5; y <= -2; y++) {
                for (int x = -3; x <= 3; x++) {
                    this.b.set(x, y, -11, x == 0 ? state(Blocks.COBBLESTONE) : state(Blocks.IRON_BARS));
                }
                for (int z = -14; z <= -12; z++) {
                    this.b.set(0, y, z, state(Blocks.COBBLESTONE));
                }
            }
            this.b.set(-2, -5, -14, state(Blocks.HAY_BLOCK));
            this.b.set(2, -5, -14, state(Blocks.HAY_BLOCK));
            this.b.ladder(-3, -5, -1, -8, Direction.NORTH);
            this.b.set(3, -5, -8, state(Blocks.IRON_CHAIN));
            this.b.set(3, -4, -8, state(Blocks.IRON_CHAIN));
            this.b.set(2, -5, -9, Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, false));
            this.b.chest(3, -5, -10, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.WEST), ModLootTables.SITE_ROBBER_CASTLE);
            this.spawn(ModEntities.ALLAY.get(), -2, -5, -13);
            this.spawn(ModEntities.ALLAY.get(), 2, -5, -13);
        }

        /** Der Palas an der Ostmauer: steinerne Halle mit Satteldach, Kamin, langer Tafel und dem Thron des Raubritters. */
        void hall() {
            int x0 = 4;
            int x1 = 11;
            int z0 = -5;
            int z1 = 5;
            for (int x = x0; x <= x1; x++) {
                for (int z = z0; z <= z1; z++) {
                    boolean wall = x == x0 || x == x1 || z == z0 || z == z1;
                    this.b.set(x, 0, z, wall ? this.stone(x, 0, z) : state(Blocks.SPRUCE_PLANKS));
                    for (int y = 1; y <= 5; y++) {
                        BlockState s;
                        if (wall) {
                            boolean corner = (x == x0 || x == x1) && (z == z0 || z == z1);
                            boolean window = x == x0 && (z == -3 || z == 3) && y >= 2 && y <= 3;
                            s = window ? Blocks.STAINED_GLASS_PANE.pick(DyeColor.BROWN).defaultBlockState()
                                    : corner || y == 5 ? state(Blocks.STRIPPED_DARK_OAK_LOG) : this.stone(x, y, z);
                        } else {
                            s = Blocks.AIR.defaultBlockState();
                        }
                        this.b.set(x, y, z, s);
                    }
                }
            }
            this.b.gableAlongZ(x0, x1, z0, z1, 6, Blocks.DARK_OAK_STAIRS, state(Blocks.DARK_OAK_PLANKS), VillageBuilder.slab(Blocks.DARK_OAK_SLAB, SlabType.BOTTOM),
                    state(Blocks.DARK_OAK_PLANKS));
            this.b.door(x0, 1, 0, Blocks.DARK_OAK_DOOR, Direction.EAST);
            // Kamin an der Nordwand: der Schornstein springt nach außen vor, das Feuer brennt unten in ihm
            for (int x = 7; x <= 9; x++) {
                for (int z = z0 - 2; z <= z0 - 1; z++) {
                    this.foundation(x, z, -1);
                    for (int y = 0; y <= 12; y++) {
                        boolean flue = x == 8 && z == z0 - 1 && y >= 1;
                        this.b.set(x, y, z, flue ? Blocks.AIR.defaultBlockState() : state(Blocks.BRICKS));
                    }
                }
            }
            this.b.set(8, 1, z0 - 1, Blocks.CAMPFIRE.defaultBlockState());
            this.b.set(8, 1, z0, Blocks.AIR.defaultBlockState());
            this.b.set(8, 2, z0, Blocks.AIR.defaultBlockState());
            this.b.set(7, 3, z0 + 1, VillageBuilder.slab(Blocks.BRICK_SLAB, SlabType.TOP));
            this.b.set(9, 3, z0 + 1, VillageBuilder.slab(Blocks.BRICK_SLAB, SlabType.TOP));
            // die Tafel und Bänke
            for (int z = -2; z <= 2; z++) {
                this.b.set(7, 1, z, VillageBuilder.slab(Blocks.DARK_OAK_SLAB, SlabType.TOP));
                this.b.set(8, 1, z, VillageBuilder.slab(Blocks.DARK_OAK_SLAB, SlabType.TOP));
                this.b.set(6, 1, z, VillageBuilder.stairs(Blocks.SPRUCE_STAIRS, Direction.WEST, Half.BOTTOM));
                this.b.set(9, 1, z, VillageBuilder.stairs(Blocks.SPRUCE_STAIRS, Direction.EAST, Half.BOTTOM));
            }
            this.b.set(7, 2, -1, Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.CANDLES, 2).setValue(CandleBlock.LIT, true));
            this.b.set(8, 2, 1, Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.CANDLES, 3).setValue(CandleBlock.LIT, true));
            // der Thron am Südende
            this.b.set(7, 1, 4, VillageBuilder.stairs(Blocks.DARK_OAK_STAIRS, Direction.SOUTH, Half.BOTTOM));
            this.b.set(8, 1, 4, VillageBuilder.stairs(Blocks.DARK_OAK_STAIRS, Direction.SOUTH, Half.BOTTOM));
            this.b.set(6, 3, 4, Blocks.WALL_BANNER.pick(DyeColor.RED).defaultBlockState().setValue(WallBannerBlock.FACING, Direction.NORTH));
            this.b.set(9, 3, 4, Blocks.WALL_BANNER.pick(DyeColor.BLACK).defaultBlockState().setValue(WallBannerBlock.FACING, Direction.NORTH));
            this.b.chest(10, 1, 4, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.WEST), ModLootTables.SITE_ROBBER_CASTLE);
            for (int[] p : new int[][] {{7, -1}, {8, 2}}) {
                for (int y = 8; y <= 9; y++) {
                    this.b.set(p[0], y, p[1], state(Blocks.IRON_CHAIN));
                }
                this.lantern(p[0], 7, p[1], true);
            }
        }

        /** Das Lager der Knechte an der Westmauer: ein Holzschuppen mit Betten, Heu und Truhen. */
        void barracks() {
            for (int x = -11; x <= -6; x++) {
                for (int z = -3; z <= 6; z++) {
                    boolean post = x == -6 && (z == -3 || z == 1 || z == 6) || x == -11 && (z == -3 || z == 6);
                    for (int y = 1; y <= 4; y++) {
                        this.b.set(x, y, z, post ? state(Blocks.SPRUCE_LOG) : z == -3 && x < -6 || z == 6 && x < -6 || x == -11
                                ? state(Blocks.SPRUCE_PLANKS) : Blocks.AIR.defaultBlockState());
                    }
                    this.b.set(x, 5, z, x == -6 ? VillageBuilder.stairs(Blocks.SPRUCE_STAIRS, Direction.WEST, Half.BOTTOM)
                            : VillageBuilder.slab(Blocks.SPRUCE_SLAB, SlabType.BOTTOM));
                }
            }
            this.b.bed(-10, 1, -2, Blocks.BED.pick(DyeColor.BROWN), Direction.EAST);
            this.b.bed(-10, 1, 0, Blocks.BED.pick(DyeColor.BROWN), Direction.EAST);
            this.b.set(-10, 1, 2, state(Blocks.HAY_BLOCK));
            this.b.set(-10, 2, 2, state(Blocks.HAY_BLOCK));
            this.b.set(-9, 1, 2, state(Blocks.HAY_BLOCK));
            this.b.chest(-10, 1, 4, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.EAST), ModLootTables.SITE_ROBBER_CASTLE);
            this.b.set(-10, 1, 5, Blocks.BARREL);
            this.lantern(-8, 4, 1, true);
        }

        /** Der Hof: Brunnen, Übungspuppen, Feuerstelle, Raubgut, Laternen auf Pfählen. */
        void yard() {
            // Brunnen
            for (int x = -4; x <= -2; x++) {
                for (int z = 3; z <= 5; z++) {
                    boolean mid = x == -3 && z == 4;
                    this.b.set(x, 0, z, mid ? state(Blocks.WATER) : state(Blocks.COBBLESTONE));
                    this.b.set(x, 1, z, mid ? Blocks.AIR.defaultBlockState() : state(Blocks.COBBLESTONE_WALL));
                    for (int y = -4; y <= -1; y++) {
                        this.b.set(x, y, z, mid ? state(Blocks.WATER) : state(Blocks.COBBLESTONE));
                    }
                }
            }
            for (int z : new int[] {3, 5}) {
                this.b.set(-3, 2, z, state(Blocks.SPRUCE_FENCE));
                this.b.set(-3, 3, z, state(Blocks.SPRUCE_FENCE));
            }
            for (int z = 2; z <= 6; z++) {
                this.b.set(-3, 4, z, VillageBuilder.slab(Blocks.SPRUCE_SLAB, SlabType.BOTTOM));
            }
            this.b.set(-3, 3, 4, state(Blocks.IRON_CHAIN));
            // Übungspuppen
            for (int[] p : new int[][] {{3, 7}, {5, 8}}) {
                this.b.set(p[0], 1, p[1], state(Blocks.SPRUCE_FENCE));
                this.b.set(p[0], 2, p[1], state(Blocks.HAY_BLOCK));
                this.b.set(p[0], 3, p[1], Blocks.CARVED_PUMPKIN.defaultBlockState().setValue(CarvedPumpkinBlock.FACING, Direction.NORTH));
            }
            // Feuerstelle mit Sitzklötzen
            this.b.set(2, 1, 3, Blocks.CAMPFIRE.defaultBlockState());
            this.b.set(2, 1, 1, state(Blocks.SPRUCE_LOG).setValue(RotatedPillarBlock.AXIS, Direction.Axis.X));
            this.b.set(3, 1, 4, state(Blocks.SPRUCE_LOG).setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z));
            // Raubgut vor dem Palas
            this.b.chest(3, 1, -5, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.WEST), ModLootTables.SITE_ROBBER_CASTLE);
            this.b.set(2, 1, -6, Blocks.BARREL);
            this.b.set(3, 1, -6, Blocks.BARREL);
            this.b.set(3, 2, -6, Blocks.BARREL);
            // Laternen auf Pfählen
            for (int[] p : new int[][] {{-2, -3}, {2, 9}, {-6, 8}}) {
                this.b.set(p[0], 1, p[1], state(Blocks.SPRUCE_FENCE));
                this.b.set(p[0], 2, p[1], state(Blocks.SPRUCE_FENCE));
                this.lantern(p[0], 3, p[1], false);
            }
        }

        // =================================================================================
        // Besatzung
        // =================================================================================

        /** Die Leute des Raubritters: Diener mit Äxten, zwei Hexer, Armbrustschützen auf den Südtürmen. */
        void garrison() {
            this.spawn(ModEntities.VINDICATOR.get(), 5, 1, -3);
            this.spawn(ModEntities.VINDICATOR.get(), 10, 1, -2);
            this.spawn(ModEntities.VINDICATOR.get(), 0, 8, -10);
            this.spawn(ModEntities.VINDICATOR.get(), 0, 1, 10);
            this.spawn(ModEntities.EVOKER.get(), 7, 1, 3);
            this.spawn(ModEntities.EVOKER.get(), 0, 14, -10);
            this.spawn(ModEntities.PILLAGER.get(), 10, 13, 10);
            this.spawn(ModEntities.PILLAGER.get(), -10, 13, 10);
        }

        void spawn(EntityType<? extends Mob> type, int x, int y, int z) {
            BlockPos pos = this.b.world(x, y, z);
            if (!this.b.inside(pos)) {
                return;
            }
            Mob mob = type.create(this.b.level.getLevel(), EntitySpawnReason.STRUCTURE);
            if (mob == null) {
                return;
            }
            mob.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, this.b.noise(x, y, z) * 360.0f, 0.0f);
            mob.finalizeSpawn(this.b.level, this.b.level.getCurrentDifficultyAt(pos), EntitySpawnReason.STRUCTURE, null);
            mob.setPersistenceRequired();
            this.b.level.addFreshEntityWithPassengers(mob);
        }
    }
}
