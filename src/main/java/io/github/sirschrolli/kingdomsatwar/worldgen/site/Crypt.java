package io.github.sirschrolli.kingdomsatwar.worldgen.site;

import io.github.sirschrolli.kingdomsatwar.loot.ModLootTables;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.VillageBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.WallSkullBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * Die Gruft der Vergessenen (seit 1.15): Unter einem verwitterten Mausoleum führt eine Wendeltreppe tief in den Fels. Unten
 * liegt eine Vorhalle, dann ein Gang mit Grabnischen, links und rechts davon zwei Beinhäuser (Wände aus Knochen, je ein
 * Spawner der Gerippe und eine Truhe), und schließlich die große Gruft: eine gewölbte Halle aus Tiefenschiefer mit zwei
 * Reihen Pfeilern, Sarkophagen, Grabnischen voller Schädel, Seelenlaternen und purpurnen Bannern. Am Ende, auf einem Podest
 * zwischen zwei Seelenfeuern, sitzt Morvan, der Gruftkönig, auf seinem Thron und schläft – neben ihm der Hort der Gruft.
 *
 * <p>Eigene Koordinaten: Mitte der Halle bei (0, 0, 0); y = 0 ist die Schicht des Fußbodens (man steht auf y = 1). Der Thron
 * steht im Norden (−z), Gang, Vorhalle und Treppe liegen im Süden (+z). {@code data[0]}: Höhe der Oberfläche über der Treppe
 * (eigene Höhe des Bodens des Mausoleums) – 0, wenn es keinen Weg hinauf gibt (zu tief, unter Wasser, zu steil).
 */
final class Crypt {
    /** Die Halle innen: x von −7 bis 7, z von −13 bis 7. */
    static final int HW = 7;
    static final int NORTH = -13;
    static final int SOUTH = 7;
    /** Mitte der Wendeltreppe (in der Vorhalle). */
    static final int SHAFT_Z = 19;
    /** Höchstens so hoch führt die Treppe hinauf. */
    static final int MAX_SHAFT = 56;
    /** Der Thron (die Sitzfläche) und wo die Pfeiler stehen. */
    static final BlockPos THRONE = new BlockPos(0, 3, -12);
    static final int[] PILLARS_Z = {4, 0, -4, -8};
    /** Die Runde der Wendeltreppe um den Mittelpfeiler (Versatz zur Mitte der Treppe). */
    static final int[][] SPIRAL = {{1, -1}, {1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}, {0, -1}};

    private Crypt() {}

    static Optional<Structure.GenerationStub> plan(Structure.GenerationContext context, SiteTerrain terrain, long seed) {
        BlockPos origin = DeepLair.origin(context, terrain, seed, -28, 37, 24, 16);
        if (origin == null) {
            return Optional.empty();
        }
        Rotation rotation = DeepLair.rotation(seed);
        BlockPos shaft = origin.offset(new BlockPos(0, 0, SHAFT_Z).rotate(rotation));
        int ground = terrain.ground(shaft.getX(), shaft.getZ());
        int[] around = terrain.span(shaft.getX(), shaft.getZ(), 4, 2);
        int surface = ground - origin.getY();
        boolean reachable = surface >= 12 && surface <= MAX_SHAFT && !terrain.water(shaft.getX(), shaft.getZ()) && around[1] - around[0] <= 4;
        int top = reachable ? surface : 0;
        BoundingBox box = box(origin, rotation, top);
        return Optional.of(new Structure.GenerationStub(origin,
                builder -> builder.addPiece(new SitePiece(SiteKind.CRYPT, 0, origin, rotation, seed, new int[] {top}, box))));
    }

    static BoundingBox box(BlockPos origin, Rotation rotation, int surface) {
        return SitePiece.box(origin, rotation, -10, -3, -16, 10, Math.max(14, surface + 11), 25);
    }

    static void build(VillageBuilder b, SitePiece piece) {
        new Builder(b, piece.data(0)).build();
    }

    private static final class Builder {
        private final VillageBuilder b;
        private final int surface;

        Builder(VillageBuilder b, int surface) {
            this.b = b;
            this.surface = surface;
        }

        void build() {
            this.hall();
            this.pillars();
            this.niches();
            this.sarcophagi();
            this.dais();
            this.corridor();
            this.ossuary(1);
            this.ossuary(-1);
            this.antechamber();
            if (this.surface > 0) {
                this.shaft();
                this.mausoleum();
            }
            this.lights();
            this.webs();
            DeepLair.boss(this.b, ModEntities.CRYPT_KING.get(), THRONE.getX(), THRONE.getY(), THRONE.getZ(), 0.5, Direction.SOUTH);
        }

        // =================================================================================
        // Stoffe
        // =================================================================================

        BlockState wall(int x, int y, int z) {
            float n = this.b.noise(x, y, z);
            return n < 0.12f ? Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState()
                    : n < 0.2f ? Blocks.DEEPSLATE_TILES.defaultBlockState() : Blocks.DEEPSLATE_BRICKS.defaultBlockState();
        }

        BlockState floor(int x, int z) {
            float n = this.b.noise(x, -1, z);
            return n < 0.14f ? Blocks.CRACKED_DEEPSLATE_TILES.defaultBlockState() : Blocks.DEEPSLATE_TILES.defaultBlockState();
        }

        BlockState stone(int x, int y, int z) {
            float n = this.b.noise(x, y + 500, z);
            return n < 0.25f ? Blocks.MOSSY_STONE_BRICKS.defaultBlockState()
                    : n < 0.4f ? Blocks.CRACKED_STONE_BRICKS.defaultBlockState() : Blocks.STONE_BRICKS.defaultBlockState();
        }

        /** Ein hohler Kasten: Boden, Wände und Decke aus Gestein der Gruft, innen Luft. */
        void shell(int x0, int y0, int z0, int x1, int y1, int z1) {
            for (int x = x0; x <= x1; x++) {
                for (int y = y0; y <= y1; y++) {
                    for (int z = z0; z <= z1; z++) {
                        boolean edge = x == x0 || x == x1 || y == y0 || y == y1 || z == z0 || z == z1;
                        if (!edge) {
                            this.b.set(x, y, z, Blocks.AIR);
                        } else if (y == y0) {
                            this.b.set(x, y, z, this.floor(x, z));
                        } else {
                            this.b.set(x, y, z, this.wall(x, y, z));
                        }
                    }
                }
            }
        }

        /** Wie hoch die Luft der Halle über (x) reicht: ein flaches Tonnengewölbe (Scheitel bei 10, an den Wänden 8). */
        static int vault(int x) {
            return 6 + (int) Math.round(Math.sqrt(Math.max(0.0, 1.0 - x * x / 64.0)) * 4.0);
        }

        // =================================================================================
        // Die Halle
        // =================================================================================

        void hall() {
            for (int x = -HW - 1; x <= HW + 1; x++) {
                for (int z = NORTH - 1; z <= SOUTH + 1; z++) {
                    boolean side = x < -HW || x > HW || z < NORTH || z > SOUTH;
                    this.b.set(x, 0, z, this.floor(x, z));
                    int top = side ? 0 : vault(x);
                    for (int y = 1; y <= 12; y++) {
                        if (!side && y <= top) {
                            this.b.set(x, y, z, Blocks.AIR);
                        } else if (y <= 11 || !side) {
                            this.b.set(x, y, z, y > top && !side ? Blocks.DEEPSLATE_TILES.defaultBlockState() : this.wall(x, y, z));
                        }
                    }
                }
            }
            // Gurtbögen über den Pfeilern und an den Enden
            for (int z : new int[] {4, 0, -4, -8, -12}) {
                for (int x = -HW; x <= HW; x++) {
                    this.b.set(x, vault(x), z, Blocks.POLISHED_DEEPSLATE);
                }
            }
            // der Läufer zum Thron: purpur, schwarz gesäumt
            for (int z = -8; z <= SOUTH; z++) {
                this.b.set(0, 1, z, Blocks.CARPET.pick(DyeColor.PURPLE));
                this.b.set(-1, 1, z, Blocks.CARPET.pick(DyeColor.BLACK));
                this.b.set(1, 1, z, Blocks.CARPET.pick(DyeColor.BLACK));
            }
            // der Durchgang zum Gang (Süden)
            this.b.air(-1, 1, SOUTH + 1, 1, 3, SOUTH + 1);
            this.b.set(0, 4, SOUTH + 1, Blocks.AIR);
            this.b.set(-1, 4, SOUTH + 1, VillageBuilder.stairs(Blocks.DEEPSLATE_BRICK_STAIRS, Direction.WEST, Half.TOP));
            this.b.set(1, 4, SOUTH + 1, VillageBuilder.stairs(Blocks.DEEPSLATE_BRICK_STAIRS, Direction.EAST, Half.TOP));
        }

        void pillars() {
            for (int side : new int[] {-4, 4}) {
                for (int z : PILLARS_Z) {
                    int top = vault(side) - 1;
                    for (int y = 1; y <= top; y++) {
                        BlockState s = y == 1 ? Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
                                : y == top ? Blocks.CHISELED_DEEPSLATE.defaultBlockState() : Blocks.POLISHED_DEEPSLATE.defaultBlockState();
                        this.b.set(side, y, z, s);
                    }
                }
            }
        }

        /** Grabnischen in den Seitenwänden: zwei Reihen, darin Schädel, Gebeine, Kerzen und Spinnweben. */
        void niches() {
            for (int sx : new int[] {-1, 1}) {
                int x = sx * (HW + 1);
                for (int z : new int[] {6, 2, -2, -6, -10}) {
                    for (int y : new int[] {2, 4}) {
                        this.b.set(x + sx, y, z, this.wall(x + sx, y, z));
                        this.b.set(x, y, z, Blocks.AIR);
                        this.b.set(x, y - 1, z, Blocks.POLISHED_DEEPSLATE_SLAB.defaultBlockState().setValue(BlockStateProperties.SLAB_TYPE, SlabType.TOP));
                        float n = this.b.noise(x, y, z);
                        Direction face = sx > 0 ? Direction.WEST : Direction.EAST;
                        if (n < 0.45f) {
                            this.b.set(x, y, z, Blocks.SKELETON_SKULL.defaultBlockState().setValue(SkullBlock.ROTATION,
                                    (face == Direction.WEST ? 4 : 12) + (int) (n * 6) - 1 & 15));
                        } else if (n < 0.6f) {
                            this.b.set(x, y, z, Blocks.COBWEB);
                        } else if (n < 0.75f) {
                            this.b.set(x, y, z, candle(Blocks.DYED_CANDLE.pick(DyeColor.BLACK), 1 + (int) (n * 10) % 3));
                        } else if (n < 0.85f) {
                            this.b.set(x, y, z, Blocks.BONE_BLOCK.defaultBlockState().setValue(BlockStateProperties.AXIS, Direction.Axis.Z));
                        }
                    }
                }
                // Banner zwischen den Nischen
                for (int z : new int[] {4, -4}) {
                    BlockState banner = Blocks.WALL_BANNER.pick(DyeColor.PURPLE).defaultBlockState().setValue(WallBannerBlock.FACING, sx > 0 ? Direction.WEST : Direction.EAST);
                    this.b.set(x - sx, 6, z + (z > 0 ? -2 : 2), banner);
                }
            }
        }

        static BlockState candle(Block block, int count) {
            return block.defaultBlockState().setValue(CandleBlock.CANDLES, Math.max(1, Math.min(4, count))).setValue(CandleBlock.LIT, true);
        }

        /** Sarkophage in den Seitenschiffen (in jedem Schiff ist einer aufgebrochen); am Kopfende ein Stein mit einer Kerze. */
        void sarcophagi() {
            for (int sx : new int[] {-1, 1}) {
                for (int i = 0; i < 3; i++) {
                    int z0 = new int[] {1, -3, -7}[i];
                    boolean broken = (i == 1) == (sx > 0);
                    for (int x = 5; x <= 6; x++) {
                        for (int z = z0; z <= z0 + 2; z++) {
                            int wx = sx * x;
                            this.b.set(wx, 1, z, Blocks.POLISHED_DEEPSLATE);
                            if (z == z0) {
                                this.b.set(wx, 2, z, Blocks.CHISELED_DEEPSLATE);
                            } else if (broken && z == z0 + 1) {
                                this.b.set(wx, 2, z, x == 5 ? Blocks.COBWEB.defaultBlockState() : Blocks.AIR.defaultBlockState());
                            } else {
                                this.b.set(wx, 2, z, VillageBuilder.slab(Blocks.DEEPSLATE_TILE_SLAB, SlabType.BOTTOM));
                            }
                        }
                    }
                    if (broken) {
                        // der Deckel liegt daneben, ein Schädel schaut heraus
                        this.b.set(sx * 6, 2, z0 + 1, Blocks.SKELETON_SKULL.defaultBlockState().setValue(SkullBlock.ROTATION, sx > 0 ? 4 : 12));
                        this.b.set(sx * 4, 1, z0 + 1, VillageBuilder.slab(Blocks.DEEPSLATE_TILE_SLAB, SlabType.BOTTOM));
                    }
                    this.b.set(sx * 6, 3, z0, candle(broken ? Blocks.DYED_CANDLE.pick(DyeColor.BLACK) : Blocks.DYED_CANDLE.pick(DyeColor.PURPLE), broken ? 1 : 2));
                }
            }
        }

        /** Das Podest mit dem Thron, den Seelenfeuern und dem Hort. */
        void dais() {
            for (int x = -5; x <= 5; x++) {
                this.b.set(x, 1, -9, VillageBuilder.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Direction.NORTH, Half.BOTTOM));
                for (int z = NORTH; z <= -10; z++) {
                    this.b.set(x, 1, z, Blocks.POLISHED_BLACKSTONE_BRICKS);
                }
            }
            for (int x = -3; x <= 3; x++) {
                this.b.set(x, 2, -10, VillageBuilder.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Direction.NORTH, Half.BOTTOM));
                for (int z = NORTH; z <= -11; z++) {
                    this.b.set(x, 2, z, Blocks.POLISHED_BLACKSTONE);
                }
            }
            // der Thron: Sitz, Lehne mit Krone aus Gold, Armlehnen
            this.b.set(THRONE.getX(), THRONE.getY(), THRONE.getZ(), VillageBuilder.slab(Blocks.POLISHED_BLACKSTONE_SLAB, SlabType.BOTTOM));
            for (int y = 3; y <= 6; y++) {
                this.b.set(0, y, NORTH, Blocks.POLISHED_BLACKSTONE_BRICKS);
            }
            this.b.set(0, 7, NORTH, Blocks.CHISELED_POLISHED_BLACKSTONE);
            this.b.set(0, 8, NORTH, Blocks.GOLD_BLOCK);
            this.b.set(-1, 6, NORTH, VillageBuilder.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Direction.EAST, Half.BOTTOM));
            this.b.set(1, 6, NORTH, VillageBuilder.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Direction.WEST, Half.BOTTOM));
            for (int y = 3; y <= 5; y++) {
                this.b.set(-1, y, NORTH, Blocks.POLISHED_BLACKSTONE_BRICKS);
                this.b.set(1, y, NORTH, Blocks.POLISHED_BLACKSTONE_BRICKS);
            }
            this.b.set(-1, 3, -12, Blocks.POLISHED_BLACKSTONE_BRICK_WALL);
            this.b.set(1, 3, -12, Blocks.POLISHED_BLACKSTONE_BRICK_WALL);
            // Seelenfeuer vorn auf dem Podest
            BlockState fire = Blocks.SOUL_CAMPFIRE.defaultBlockState().setValue(CampfireBlock.FACING, Direction.SOUTH);
            this.b.set(-3, 3, -11, fire);
            this.b.set(3, 3, -11, fire);
            // der Hort der Gruft neben dem Thron
            BlockState chest = Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH);
            this.b.chest(-3, 3, NORTH, chest, ModLootTables.SITE_CRYPT_HOARD);
            this.b.chest(3, 3, NORTH, chest, ModLootTables.SITE_CRYPT_HOARD);
            this.b.set(-2, 3, NORTH, candle(Blocks.DYED_CANDLE.pick(DyeColor.PURPLE), 3));
            this.b.set(2, 3, NORTH, candle(Blocks.DYED_CANDLE.pick(DyeColor.PURPLE), 3));
            // Banner hinter dem Thron
            for (int x : new int[] {-4, 4}) {
                this.b.set(x, 7, NORTH, Blocks.WALL_BANNER.pick(DyeColor.PURPLE).defaultBlockState().setValue(WallBannerBlock.FACING, Direction.SOUTH));
            }
        }

        // =================================================================================
        // Gang, Beinhäuser, Vorhalle
        // =================================================================================

        void corridor() {
            int z0 = SOUTH + 2;
            int z1 = SHAFT_Z - 4;
            for (int z = z0; z <= z1; z++) {
                for (int x = -2; x <= 2; x++) {
                    this.b.set(x, 0, z, this.floor(x, z));
                    for (int y = 1; y <= 5; y++) {
                        boolean inner = Math.abs(x) <= 1 && (y <= 3 || (y == 4 && x == 0));
                        this.b.set(x, y, z, inner ? Blocks.AIR.defaultBlockState() : this.wall(x, y, z));
                    }
                }
                this.b.set(-1, 4, z, VillageBuilder.stairs(Blocks.DEEPSLATE_BRICK_STAIRS, Direction.WEST, Half.TOP));
                this.b.set(1, 4, z, VillageBuilder.stairs(Blocks.DEEPSLATE_BRICK_STAIRS, Direction.EAST, Half.TOP));
            }
            // Schädel an den Wänden des Gangs
            for (int sx : new int[] {-1, 1}) {
                for (int z : new int[] {z0 + 1, z1 - 1}) {
                    this.b.set(sx, 3, z, Blocks.SKELETON_WALL_SKULL.defaultBlockState().setValue(WallSkullBlock.FACING,
                            sx > 0 ? Direction.WEST : Direction.EAST));
                }
            }
        }

        /** Ein Beinhaus am Gang ({@code sx}: 1 Osten, −1 Westen): Wände aus Knochen, ein Spawner der Gerippe, eine Truhe. */
        void ossuary(int sx) {
            for (int i = 2; i <= 8; i++) {
                int x = sx * i;
                for (int z = 9; z <= 15; z++) {
                    for (int y = 0; y <= 5; y++) {
                        boolean edge = i == 8 || z == 9 || z == 15 || y == 0 || y == 5 || i == 2;
                        if (!edge) {
                            this.b.set(x, y, z, Blocks.AIR);
                        } else if (y == 0) {
                            this.b.set(x, y, z, this.floor(x, z));
                        } else if (i == 2) {
                            if (!(z == 12 && y <= 2)) {
                                this.b.set(x, y, z, this.wall(x, y, z));
                            }
                        } else {
                            this.b.set(x, y, z, this.b.noise(x, y, z) < 0.6f
                                    ? Blocks.BONE_BLOCK.defaultBlockState().setValue(BlockStateProperties.AXIS,
                                            i == 8 ? Direction.Axis.Z : Direction.Axis.X)
                                    : this.wall(x, y, z));
                        }
                    }
                }
            }
            // der Durchgang vom Gang
            this.b.air(sx * 2, 1, 12, sx * 2, 2, 12);
            // Schädel in den Wänden (Nischen)
            for (int z : new int[] {10, 12, 14}) {
                this.b.set(sx * 7, 3, z, Blocks.SKELETON_WALL_SKULL.defaultBlockState().setValue(WallSkullBlock.FACING,
                        sx > 0 ? Direction.WEST : Direction.EAST));
            }
            for (int i : new int[] {4, 6}) {
                this.b.set(sx * i, 3, 9 + 1, Blocks.SKELETON_WALL_SKULL.defaultBlockState().setValue(WallSkullBlock.FACING, Direction.SOUTH));
                this.b.set(sx * i, 3, 15 - 1, Blocks.SKELETON_WALL_SKULL.defaultBlockState().setValue(WallSkullBlock.FACING, Direction.NORTH));
            }
            DeepLair.spawner(this.b, sx * 5, 1, 12, ModEntities.SKELETON.get());
            this.b.chest(sx * 7, 1, 14, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, sx > 0 ? Direction.WEST : Direction.EAST),
                    ModLootTables.SITE_CRYPT);
            this.b.set(sx * 7, 1, 10, Blocks.BONE_BLOCK);
            this.b.set(sx * 7, 2, 10, Blocks.SKELETON_SKULL.defaultBlockState().setValue(SkullBlock.ROTATION, sx > 0 ? 4 : 12));
            this.b.set(sx * 3, 1, 10, Blocks.COBWEB);
            this.b.set(sx * 3, 4, 14, Blocks.COBWEB);
            this.b.hangingLantern(sx * 5, 4, 12, Blocks.SOUL_LANTERN);
        }

        void antechamber() {
            int z0 = SHAFT_Z - 4;
            int z1 = SHAFT_Z + 4;
            this.shell(-4, 0, z0, 4, 6, z1);
            // die Durchgänge: zum Gang
            this.b.air(-1, 1, z0, 1, 3, z0);
            this.b.set(0, 4, z0, Blocks.AIR);
            // Mittelpfeiler der Wendeltreppe (ohne Treppe: eine Säule mit einer Seelenlaterne)
            int top = this.surface > 0 ? this.surface : 5;
            for (int y = 1; y <= top; y++) {
                this.b.set(0, y, SHAFT_Z, Blocks.POLISHED_DEEPSLATE);
            }
            if (this.surface <= 0) {
                this.b.set(0, 6, SHAFT_Z, Blocks.CHISELED_DEEPSLATE);
                this.b.set(0, 1, SHAFT_Z + 2, Blocks.SOUL_LANTERN);
            }
            // in den Ecken Gebeine und Spinnweben
            this.b.set(-3, 1, z1 - 1, Blocks.BONE_BLOCK);
            this.b.set(3, 1, z1 - 1, Blocks.SKELETON_SKULL.defaultBlockState().setValue(SkullBlock.ROTATION, 6));
            this.b.set(-3, 5, z0 + 1, Blocks.COBWEB);
            this.b.set(3, 5, z1 - 1, Blocks.COBWEB);
            BlockState torch = Blocks.SOUL_WALL_TORCH.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH);
            this.b.set(-2, 3, z0 + 1, torch);
            this.b.set(2, 3, z0 + 1, torch);
        }

        // =================================================================================
        // Treppe und Mausoleum
        // =================================================================================

        /** Die Wendeltreppe: eine Stufe je Block Höhe, acht Stufen je Runde um den Mittelpfeiler, die letzte bündig mit dem Boden oben. */
        void shaft() {
            int s = this.surface;
            for (int y = 6; y < s; y++) {
                for (int x = -2; x <= 2; x++) {
                    for (int z = SHAFT_Z - 2; z <= SHAFT_Z + 2; z++) {
                        boolean edge = Math.abs(x) == 2 || Math.abs(z - SHAFT_Z) == 2;
                        if (edge) {
                            this.b.set(x, y, z, this.stone(x, y, z));
                        } else if (!(x == 0 && z == SHAFT_Z)) {
                            this.b.set(x, y, z, Blocks.AIR);
                        }
                    }
                }
            }
            for (int k = 0; k < s; k++) {
                int[] c = SPIRAL[k % SPIRAL.length];
                int[] p = SPIRAL[(k + SPIRAL.length - 1) % SPIRAL.length];
                Direction up = direction(c[0] - p[0], c[1] - p[1]);
                this.b.set(c[0], 1 + k, SHAFT_Z + c[1], VillageBuilder.stairs(Blocks.STONE_BRICK_STAIRS, up, Half.BOTTOM));
            }
        }

        static Direction direction(int dx, int dz) {
            if (Math.abs(dx) >= Math.abs(dz)) {
                return dx > 0 ? Direction.EAST : Direction.WEST;
            }
            return dz > 0 ? Direction.SOUTH : Direction.NORTH;
        }

        /** Das Mausoleum über der Treppe: Mauern aus altem Steinziegel, ein Satteldach, ein Schädel über der Tür. */
        void mausoleum() {
            int s = this.surface;
            int z0 = SHAFT_Z - 3;
            int z1 = SHAFT_Z + 3;
            // Fundament bis hinab auf festen Grund, Boden (mit dem Treppenloch), innen Luft
            for (int x = -3; x <= 3; x++) {
                for (int z = z0; z <= z1; z++) {
                    for (int y = s - 1; y > s - 12 && y > 6; y--) {
                        if (Math.abs(x) <= 2 && Math.abs(z - SHAFT_Z) <= 2) {
                            break;
                        }
                        BlockState old = this.b.get(x, y, z);
                        if (VillageBuilder.isSolidGround(old)) {
                            break;
                        }
                        this.b.set(x, y, z, this.stone(x, y, z));
                    }
                    boolean hole = Math.abs(x) <= 1 && Math.abs(z - SHAFT_Z) <= 1;
                    if (!hole) {
                        this.b.set(x, s, z, this.stone(x, s, z));
                    }
                    boolean edge = Math.abs(x) == 3 || z == z0 || z == z1;
                    for (int y = s + 1; y <= s + 4; y++) {
                        this.b.set(x, y, z, edge ? this.stone(x, y, z) : Blocks.AIR.defaultBlockState());
                    }
                }
            }
            // die Ecken: Pfeiler aus gemeißeltem Stein
            for (int x : new int[] {-3, 3}) {
                for (int z : new int[] {z0, z1}) {
                    for (int y = s + 1; y <= s + 4; y++) {
                        this.b.set(x, y, z, Blocks.CHISELED_STONE_BRICKS);
                    }
                }
            }
            // Tür im Süden, Schlitzfenster an den Seiten
            this.b.air(0, s + 1, z1, 0, s + 2, z1);
            this.b.set(-1, s + 3, z1, VillageBuilder.stairs(Blocks.STONE_BRICK_STAIRS, Direction.EAST, Half.TOP));
            this.b.set(1, s + 3, z1, VillageBuilder.stairs(Blocks.STONE_BRICK_STAIRS, Direction.WEST, Half.TOP));
            this.b.set(0, s + 3, z1, Blocks.CHISELED_STONE_BRICKS);
            this.b.set(0, s + 3, z1 + 1, Blocks.SKELETON_WALL_SKULL.defaultBlockState().setValue(WallSkullBlock.FACING, Direction.SOUTH));
            for (int x : new int[] {-3, 3}) {
                this.b.set(x, s + 2, SHAFT_Z, Blocks.IRON_BARS);
            }
            // Satteldach entlang z (vorn und hinten einen Block über), gefüllt
            for (int i = 0; i <= 3; i++) {
                int y = s + 5 + i;
                for (int z = z0 - 1; z <= z1 + 1; z++) {
                    this.b.set(-4 + i, y, z, VillageBuilder.stairs(Blocks.STONE_BRICK_STAIRS, Direction.EAST, Half.BOTTOM));
                    this.b.set(4 - i, y, z, VillageBuilder.stairs(Blocks.STONE_BRICK_STAIRS, Direction.WEST, Half.BOTTOM));
                    for (int x = -3 + i; x <= 3 - i; x++) {
                        if (z >= z0 && z <= z1) {
                            this.b.set(x, y, z, this.stone(x, y, z));
                        }
                    }
                }
            }
            for (int z = z0 - 1; z <= z1 + 1; z++) {
                this.b.set(0, s + 8, z, Blocks.STONE_BRICKS);
                this.b.set(0, s + 9, z, VillageBuilder.slab(Blocks.STONE_BRICK_SLAB, SlabType.BOTTOM));
            }
            // die Decke innen: schlicht flach unter dem Dach
            for (int x = -2; x <= 2; x++) {
                for (int z = z0 + 1; z <= z1 - 1; z++) {
                    this.b.set(x, s + 5, z, Blocks.STONE_BRICKS);
                }
            }
            // Geländer um das Treppenloch (nicht an der Ankunft), eine Seelenlaterne auf dem Mittelpfeiler
            int last = s - 1;
            for (int k = 0; k < SPIRAL.length; k++) {
                int[] c = SPIRAL[k];
                boolean approach = false;
                for (int back = 0; back <= 2; back++) {
                    int[] a = SPIRAL[Math.floorMod(last - back, SPIRAL.length)];
                    if (a[0] == c[0] && a[1] == c[1]) {
                        approach = true;
                    }
                }
                if (!approach) {
                    this.b.set(c[0], s + 1, SHAFT_Z + c[1], Blocks.STONE_BRICK_WALL);
                }
            }
            this.b.set(0, s + 1, SHAFT_Z, Blocks.SOUL_LANTERN);
            this.b.set(-2, s + 1, z0 + 1, Blocks.COBWEB);
            this.b.set(2, s + 1, z0 + 1, candle(Blocks.DYED_CANDLE.pick(DyeColor.BLACK), 2));
        }

        // =================================================================================
        // Licht und Spinnweben
        // =================================================================================

        void lights() {
            for (int z : new int[] {6, 2, -2, -6}) {
                for (int x : new int[] {-2, 2}) {
                    this.b.hangingLantern(x, 7, z, Blocks.SOUL_LANTERN);
                }
            }
            for (int z = SOUTH + 3; z <= SHAFT_Z - 5; z += 4) {
                this.b.hangingLantern(0, 3, z, Blocks.SOUL_LANTERN);
            }
        }

        void webs() {
            for (int x = -HW; x <= HW; x++) {
                for (int z = NORTH; z <= SOUTH; z++) {
                    int top = vault(x);
                    if ((Math.abs(x) >= HW - 1 || z <= NORTH + 1 || z >= SOUTH - 1) && this.b.noise(x, 77, z) < 0.16f
                            && this.b.get(x, top, z).isAir()) {
                        this.b.set(x, top, z, Blocks.COBWEB);
                    }
                }
            }
        }
    }
}
