package io.github.sirschrolli.kingdomsatwar.worldgen.site;

import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.VillageBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * Der Alchemistenturm (seit 2.0): Im Sumpf oder im dunklen Wald steht ein runder Turm aus Bruch- und Ziegelstein, der sich
 * Stockwerk für Stockwerk ein wenig zur Seite neigt; Ranken klettern an ihm hinauf, das Kegeldach ist zur Hälfte
 * eingestürzt. Unten das Labor mit Braustand, Kesseln über Feuer und Glut, Zauberkreis und Fässern voller Zutaten; darüber
 * die Bibliothek; ganz oben die Stube der Alchemistin mit ihrem Fernrohr und ihrer Truhe. Draußen ein Kräutergarten. Sie
 * selbst ist noch da.
 *
 * <p>Eigene Koordinaten: Mitte des Turmfußes bei (0, 0, 0), y = 0 ist der Fußboden; vorn (+z) die Tür. Jedes Stockwerk
 * (sechs Blöcke) steht einen Block weiter in +x.
 */
final class AlchemistTower {
    /** Halbmesser außen und innen. */
    static final double OUT = 4.6, IN = 3.5;
    /** Gehhöhen der drei Stockwerke. */
    static final int[] LEVELS = {0, 6, 12};
    static final int ROOF = 18;
    static final BlockPos HOARD = new BlockPos(2 + 1, 12, -2);

    private AlchemistTower() {}

    static void register() {
        Wonders.add(new Wonders.Wonder(SiteKind.ALCHEMIST_TOWER, "alchemistenturm", "Alchemist's Tower", "Alchemistenturm", 3,
                Wonders.Ground.LAND, false, AlchemistTower::plan, AlchemistTower::build, origin -> piece(origin, Rotation.NONE, 0xA1C3L),
                new double[][] {Wonders.view(-14, 10, 16, 1, 10, 0), Wonders.view(15, 18, -8, 2, 14, 0), Wonders.view(-2, 1.6, 2, 0, 1, -3),
                        Wonders.view(2, 7.6, 2, -1, 7, -2), Wonders.view(1, 13.6, 2.5, 4, 13, -2)}));
    }

    static Optional<Structure.GenerationStub> plan(Structure.GenerationContext context, SiteTerrain terrain, long seed) {
        BlockPos origin = Kit.land(context, terrain, 9, 4);
        return origin == null ? Optional.empty() : Kit.stub(piece(origin, Kit.rotation(seed), seed));
    }

    static SitePiece piece(BlockPos origin, Rotation rotation, long seed) {
        return new SitePiece(SiteKind.ALCHEMIST_TOWER, 0, origin, rotation, seed, new int[0],
                SitePiece.box(origin, rotation, -16, -5, -16, 16, ROOF + 10, 16));
    }

    static void build(VillageBuilder b, SitePiece piece) {
        new Tower(b).build();
    }

    /** Die Neigung: ab welchem Versatz in x die Mauer in Höhe y steht. */
    static int lean(int y) {
        return Math.max(0, y) / 6;
    }

    private static final class Tower {
        private final VillageBuilder b;
        private final Kit.Wood dark = Kit.darkOak();

        Tower(VillageBuilder b) {
            this.b = b;
        }

        void build() {
            Kit.pad(this.b, 9.5, Kit.s(Blocks.GRASS_BLOCK), Kit.s(Blocks.DIRT), 24);
            this.plinth();
            this.walls();
            this.floors();
            this.stairs();
            this.roof();
            this.lab();
            this.library();
            this.study();
            this.garden();
            this.vines();
            Kit.mob(this.b, ModEntities.WITCH.get(), 1, 0, 1, Direction.SOUTH);
        }

        private BlockState stone(int x, int y, int z) {
            if (y % 6 == 5) {
                return Kit.s(Blocks.DEEPSLATE_BRICKS);
            }
            return Kit.weathered(this.b, x, y, z, Kit.s(Blocks.STONE_BRICKS), Kit.s(Blocks.MOSSY_STONE_BRICKS), 0.3f, Kit.s(Blocks.CRACKED_STONE_BRICKS),
                    0.15f);
        }

        /** Der Sockel: ein Kranz aus Bruchstein, der den schiefen Turm trägt. */
        private void plinth() {
            for (int x = -6; x <= 7; x++) {
                for (int z = -6; z <= 6; z++) {
                    double d = Math.sqrt((x - 0.4) * (x - 0.4) + z * z);
                    if (d <= 5.6) {
                        Kit.column(this.b, x, z, d > 4.6 ? Kit.s(Blocks.MOSSY_COBBLESTONE) : Kit.s(Blocks.COBBLESTONE), Kit.s(Blocks.COBBLESTONE), 0);
                    }
                }
            }
        }

        /** Die runde Mauer, Stockwerk für Stockwerk versetzt; Fenster, manche zerbrochen; die Tür. */
        private void walls() {
            for (int y = 0; y < ROOF; y++) {
                int cx = lean(y);
                for (int dx = -5; dx <= 5; dx++) {
                    for (int dz = -5; dz <= 5; dz++) {
                        double d = Math.sqrt(dx * dx + dz * dz);
                        if (d <= OUT && d > IN) {
                            this.b.set(cx + dx, y, dz, this.stone(cx + dx, y, dz));
                        } else if (d <= IN) {
                            this.b.set(cx + dx, y, dz, Kit.s(Blocks.AIR));
                        }
                    }
                }
            }
            this.b.door(0, 0, 4, Blocks.DARK_OAK_DOOR, Direction.NORTH);
            this.b.set(0, 2, 4, VillageBuilder.stairs(Blocks.STONE_BRICK_STAIRS, Direction.NORTH, Half.TOP));
            for (int level : LEVELS) {
                int cx = lean(level);
                int y = level + 2;
                for (int[] w : new int[][] {{4, 0}, {-4, 0}, {0, -4}}) {
                    BlockState pane = this.b.chance(cx + w[0], y, w[1], 0.35f) ? Kit.s(Blocks.AIR) : Kit.s(Blocks.GLASS_PANE);
                    this.b.set(cx + w[0], y, w[1], pane);
                    this.b.set(cx + w[0], y + 1, w[1], Kit.s(Blocks.GLASS_PANE));
                }
            }
        }

        /** Die Böden zwischen den Stockwerken (decken beide versetzten Grundrisse), mit Öffnung für die Treppe. */
        private void floors() {
            for (int level : LEVELS) {
                int y = level - 1;
                for (int cx : new int[] {lean(level - 1), lean(level)}) {
                    for (int dx = -4; dx <= 4; dx++) {
                        for (int dz = -4; dz <= 4; dz++) {
                            if (dx * dx + dz * dz <= IN * IN + 0.5) {
                                this.b.set(cx + dx, y, dz, level == 0 ? Kit.s(Blocks.STONE_BRICKS) : Kit.s(Blocks.DARK_OAK_PLANKS));
                            }
                        }
                    }
                }
            }
        }

        /** Gerade Treppen an der Nordseite (z = -1) von Stockwerk zu Stockwerk; darüber eine Öffnung im Boden. */
        private void stairs() {
            for (int level : new int[] {0, 6}) {
                int cx = lean(level);
                for (int k = 0; k < 6; k++) {
                    int x = cx - 3 + k;
                    int y = level + k;
                    this.b.set(x, y, -1, VillageBuilder.stairs(this.dark.stairs(), Direction.EAST, Half.BOTTOM));
                    if (k >= 1 && k <= 4) {
                        this.b.set(x, level + 5, -1, Kit.s(Blocks.AIR));
                    }
                    if (k == 0) {
                        this.b.set(x - 1, level + 5, -1, Kit.s(Blocks.AIR));
                    }
                }
                // ein Geländer an der Öffnung oben
                for (int k = 1; k <= 3; k++) {
                    this.b.set(cx - 3 + k, level + 6, -2, this.dark.fence());
                }
            }
        }

        /** Das Kegeldach, zur Hälfte eingestürzt: dort ragen die Sparren in den Himmel. */
        private void roof() {
            int cx = lean(ROOF);
            for (int k = 0; k <= 7; k++) {
                double rr = 5.6 * (1.0 - k / 7.5);
                for (int dx = -6; dx <= 6; dx++) {
                    for (int dz = -6; dz <= 6; dz++) {
                        double d = Math.sqrt(dx * dx + dz * dz);
                        if (d > rr + 0.35 || d <= rr - 1.2) {
                            continue;
                        }
                        boolean broken = dx > 0 && dz < 1 && this.b.noise(cx + dx, ROOF + k, dz) < 0.7f;
                        if (!broken) {
                            this.b.set(cx + dx, ROOF + k, dz, this.b.chance(cx + dx, ROOF + k, dz, 0.2f) ? Kit.s(Blocks.DEEPSLATE_TILES)
                                    : Kit.s(Blocks.DEEPSLATE_TILE_SLAB).setValue(net.minecraft.world.level.block.SlabBlock.TYPE, SlabType.DOUBLE));
                        }
                    }
                }
            }
            // die Sparren an der eingestürzten Seite
            for (int i = 0; i <= 5; i++) {
                this.b.set(cx + 5 - i, ROOF + i, -1, this.dark.log(Direction.Axis.X));
                this.b.set(cx + 4 - i, ROOF + i, -4 + i / 2, this.dark.fence());
            }
            this.b.set(cx, ROOF + 8, 0, Kit.s(Blocks.LIGHTNING_ROD.waxed().exposed()));
            // der Boden unter dem Dach
            for (int dx = -4; dx <= 4; dx++) {
                for (int dz = -4; dz <= 4; dz++) {
                    if (dx * dx + dz * dz <= IN * IN + 0.5 && !(dx > 1 && dz < 0 && this.b.chance(cx + dx, ROOF - 1, dz, 0.5f))) {
                        this.b.set(cx + dx, ROOF - 1, dz, Kit.s(Blocks.DARK_OAK_PLANKS));
                    }
                }
            }
        }

        /** Das Labor: Braustände, Kessel über Feuer und Glut, ein Zauberkreis im Boden, Fässer mit Zutaten. */
        private void lab() {
            for (int z = 0; z <= 1; z++) {
                this.b.set(-3, 0, z, VillageBuilder.stairs(Blocks.POLISHED_ANDESITE_STAIRS, Direction.EAST, Half.TOP));
                this.b.set(-3, 1, z, Kit.s(Blocks.BREWING_STAND));
            }
            Kit.candles(this.b, -2, 0, -2, Blocks.DYED_CANDLE.pick(DyeColor.PURPLE), 3, true);
            this.b.set(2, 0, 2, Kit.s(Blocks.CAMPFIRE));
            this.b.set(2, 1, 2, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 2));
            this.b.set(3, 0, 1, Kit.s(Blocks.LAVA_CAULDRON));
            Kit.barrel(this.b, -1, 0, 3, Direction.UP);
            Kit.lootBarrel(this.b, -2, 0, 2, Kit.loot("alchemist_tower"));
            this.b.set(3, 0, -1, Kit.s(Blocks.CRAFTING_TABLE));
            // der Zauberkreis in der Mitte des Bodens
            for (int[] c : new int[][] {{0, 0}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
                this.b.set(c[0], -1, c[1], c[0] == 0 && c[1] == 0 ? Kit.s(Blocks.AMETHYST_BLOCK) : Kit.s(Blocks.CHISELED_STONE_BRICKS));
            }
            this.b.set(1, 0, -1, Kit.s(Blocks.SMALL_AMETHYST_BUD));
            this.b.set(-1, 0, 1, Kit.s(Blocks.POTTED_RED_MUSHROOM));
            this.b.hangingLantern(0, 4, 0, Blocks.SOUL_LANTERN);
        }

        /** Die Bibliothek: Regale ringsum, ein Lesepult, Kerzen. */
        private void library() {
            int cx = lean(6);
            for (int dx = -3; dx <= 3; dx++) {
                for (int dz = -3; dz <= 3; dz++) {
                    double d = Math.sqrt(dx * dx + dz * dz);
                    if (d > 2.6 && d <= IN && dz >= 0) {
                        for (int y = 6; y <= 8; y++) {
                            if (this.b.get(cx + dx, y, dz).isAir()) {
                                this.b.set(cx + dx, y, dz, Kit.s(y == 7 && (dx + dz) % 3 == 0 ? Blocks.CHISELED_BOOKSHELF : Blocks.BOOKSHELF));
                            }
                        }
                    }
                }
            }
            this.b.set(cx, 6, 0, Kit.s(Blocks.LECTERN));
            Kit.chair(this.b, cx, 6, 1, this.dark, Direction.NORTH);
            Kit.candles(this.b, cx + 1, 6, 0, Blocks.CANDLE, 2, true);
            this.b.hangingLantern(cx, 10, 0, Blocks.LANTERN);
        }

        /** Die Stube oben: Bett, Schreibtisch mit Sternkarten, das Fernrohr am Fenster, die Truhe. */
        private void study() {
            int cx = lean(12);
            this.b.bed(cx - 2, 12, 1, Blocks.BED.pick(DyeColor.PURPLE), Direction.EAST);
            this.b.set(cx + 2, 12, 1, Kit.s(Blocks.CARTOGRAPHY_TABLE));
            Kit.chair(this.b, cx + 1, 12, 1, this.dark, Direction.EAST);
            Kit.chest(this.b, HOARD.getX(), HOARD.getY(), HOARD.getZ(), Direction.WEST, Kit.loot("alchemist_hoard"));
            // das Fernrohr: drei Beine, ein Rohr aus Kupfer
            this.b.set(cx + 2, 12, -1, this.dark.fence());
            this.b.set(cx + 2, 13, -1, Kit.s(Blocks.LIGHTNING_ROD.waxed().exposed()));
            this.b.set(cx - 1, 12, -2, Kit.s(Blocks.POTTED_FERN));
        }

        /** Der Kräutergarten im Osten: Pilze auf Podsol, Beeren, Netherwarzen auf Seelensand, ein Zaun. */
        private void garden() {
            int x0 = 6;
            int x1 = 11;
            int z0 = -2;
            int z1 = 6;
            for (int x = x0; x <= x1; x++) {
                for (int z = z0; z <= z1; z++) {
                    boolean edge = x == x0 || x == x1 || z == z0 || z == z1;
                    if (edge) {
                        if (!(x == x0 && z == 2)) {
                            this.b.set(x, 0, z, this.dark.fence());
                        }
                        continue;
                    }
                    int kind = Math.floorMod(x + z * 2, 4);
                    switch (kind) {
                        case 0 -> {
                            this.b.set(x, -1, z, Kit.s(Blocks.PODZOL));
                            this.b.set(x, 0, z, Kit.s(this.b.chance(x, 0, z, 0.5f) ? Blocks.RED_MUSHROOM : Blocks.BROWN_MUSHROOM));
                        }
                        case 1 -> {
                            this.b.set(x, -1, z, Kit.s(Blocks.SOUL_SAND));
                            this.b.set(x, 0, z, Blocks.NETHER_WART.defaultBlockState().setValue(net.minecraft.world.level.block.NetherWartBlock.AGE, 3));
                        }
                        case 2 -> this.b.set(x, 0, z, Blocks.SWEET_BERRY_BUSH.defaultBlockState()
                                .setValue(net.minecraft.world.level.block.SweetBerryBushBlock.AGE, 2));
                        default -> this.b.set(x, -1, z, Kit.s(Blocks.COARSE_DIRT));
                    }
                }
            }
        }

        /** Ranken an der Mauer, von unten bis zur Mitte des Turms. */
        private void vines() {
            for (int y = 1; y < 14; y++) {
                int cx = lean(y);
                for (int[] w : new int[][] {{5, 1, 0}, {5, -1, 0}, {-5, 2, 1}, {1, 5, 2}, {-2, -5, 3}}) {
                    if (!this.b.chance(w[0], y, w[1], 0.55f - y * 0.03f)) {
                        continue;
                    }
                    int x = cx + w[0];
                    int z = w[1];
                    if (!this.b.get(x, y, z).isAir()) {
                        continue;
                    }
                    BlockState vine = switch (w[2]) {
                        case 0 -> Blocks.VINE.defaultBlockState().setValue(VineBlock.WEST, true);
                        case 1 -> Blocks.VINE.defaultBlockState().setValue(VineBlock.EAST, true);
                        case 2 -> Blocks.VINE.defaultBlockState().setValue(VineBlock.NORTH, true);
                        default -> Blocks.VINE.defaultBlockState().setValue(VineBlock.SOUTH, true);
                    };
                    this.b.set(x, y, z, vine);
                }
            }
        }
    }
}
