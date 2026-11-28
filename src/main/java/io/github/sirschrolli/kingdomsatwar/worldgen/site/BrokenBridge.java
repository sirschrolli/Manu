package io.github.sirschrolli.kingdomsatwar.worldgen.site;

import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.VillageBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * Die Zerbrochene Brücke (seit 2.0): Über das Lavameer spannt sich eine gewaltige Bogenbrücke aus Netherziegeln – Pfeiler
 * mit Bögen dazwischen, Brüstungen mit Seelenlaternen, an ihrem einen Ende der Zollturm mit Durchgang, Zinnen und roten
 * Bannern, am anderen ein verfallenes Torhaus. In der Mitte aber ist sie eingestürzt: Die Fahrbahn bricht ausgefranst ab,
 * Trümmer ragen aus der Lava, und über der Lücke kreisen Glutgeister. Wer hinüber will, muss springen – oder bauen.
 *
 * <p>Eigene Koordinaten: y = 0 ist die Höhe über der Lava (die Lava steht bis y = -1); die Fahrbahn liegt auf y = 6 (man
 * geht auf y = 7) und läuft entlang z; der Zollturm steht im Süden (-z), das Torhaus im Norden (+z).
 */
final class BrokenBridge {
    static final int DECK = 6;
    static final int END = 21;
    static final int[] PIERS = {-15, -5, 5, 15};
    static final BlockPos CHEST = new BlockPos(0, 11, -24);

    private BrokenBridge() {}

    static void register() {
        Wonders.add(new Wonders.Wonder(SiteKind.BROKEN_BRIDGE, "zerbrochene_bruecke", "Broken Bridge", "Zerbrochene Brücke", 2, Wonders.Ground.LAVA,
                false, BrokenBridge::plan, BrokenBridge::build, origin -> piece(origin, Rotation.NONE, 0x8B1DL),
                new double[][] {Wonders.view(-26, 16, -2, 0, 4, 0), Wonders.view(0, DECK + 2.6, -12, 0, DECK, 4), Wonders.view(18, 5, 6, 0, 3, -2),
                        Wonders.view(0, 12.6, -23, 0, 11.5, -26), Wonders.view(-12, 22, -36, 0, 6, -16), Wonders.view(9, 11, 33, 0, 7, 22)}));
    }

    static Optional<Structure.GenerationStub> plan(Structure.GenerationContext context, SiteTerrain terrain, long seed) {
        BlockPos origin = Kit.lavaSea(context, terrain, 18, 14);
        return origin == null ? Optional.empty() : Kit.stub(piece(origin, Kit.rotation(seed), seed));
    }

    static SitePiece piece(BlockPos origin, Rotation rotation, long seed) {
        return new SitePiece(SiteKind.BROKEN_BRIDGE, 0, origin, rotation, seed, new int[0], SitePiece.box(origin, rotation, -9, -26, -END - 9, 9, 20, END + 9));
    }

    static void build(VillageBuilder b, SitePiece piece) {
        new Bridge(b).build();
    }

    private static final class Bridge {
        private final VillageBuilder b;

        Bridge(VillageBuilder b) {
            this.b = b;
        }

        void build() {
            this.piers();
            this.deck();
            this.breach();
            this.tollTower();
            this.gatehouse();
            this.lights();
            this.dwellers();
        }

        private BlockState brick(int x, int y, int z) {
            return this.b.chance(x, y, z, 0.2f) ? Kit.s(Blocks.CRACKED_NETHER_BRICKS) : Kit.s(Blocks.NETHER_BRICKS);
        }

        /** Ist (z) in der eingestürzten Mitte? Am Rand ausgefranst. */
        private boolean broken(int x, int y, int z) {
            int edge = 3 + (this.b.chance(x, y + 40, z, 0.5f) ? 1 : 0) + (y < DECK - 2 ? -1 : 0);
            return Math.abs(z) <= edge;
        }

        /** Die Pfeiler: vom Grund der Lava bis unter die Fahrbahn, mit Wellenbrechern an beiden Seiten. */
        private void piers() {
            for (int p : PIERS) {
                for (int x = -3; x <= 3; x++) {
                    for (int z = p - 1; z <= p + 1; z++) {
                        this.b.column(x, z, this.brick(x, -1, z), 0);
                        for (int y = -1; y < DECK; y++) {
                            this.b.set(x, y, z, y == 0 ? Kit.s(Blocks.RED_NETHER_BRICKS) : this.brick(x, y, z));
                        }
                    }
                }
                // Wellenbrecher: an beiden Längsseiten spitz zulaufend
                for (int side : new int[] {-1, 1}) {
                    this.b.column(side * 4, p, this.brick(side * 4, -1, p), 0);
                    for (int y = -1; y <= 2; y++) {
                        this.b.set(side * 4, y, p, this.brick(side * 4, y, p));
                    }
                    this.b.set(side * 4, 3, p, VillageBuilder.stairs(Blocks.NETHER_BRICK_STAIRS, side < 0 ? Direction.EAST : Direction.WEST, Half.BOTTOM));
                }
            }
        }

        /** Die Fahrbahn mit ihren Bögen: zwischen den Pfeilern hängt der Bogen tief an den Pfeilern und steigt zur Mitte. */
        private void deck() {
            for (int z = -END; z <= END; z++) {
                int[] span = this.span(z);
                for (int x = -3; x <= 3; x++) {
                    int bottom = DECK;
                    if (span != null) {
                        double t = (z - span[0] + 0.5) / (span[1] - span[0] + 1);
                        double arch = 1.0 + 4.0 * Math.sqrt(Math.max(0.0, 1.0 - (2 * t - 1) * (2 * t - 1)));
                        bottom = (int) Math.round(arch);
                    }
                    for (int y = Math.min(bottom, DECK); y <= DECK; y++) {
                        if (!this.broken(x, y, z)) {
                            this.b.set(x, y, z, y == DECK && Math.abs(x) <= 1 ? Kit.s(Blocks.RED_NETHER_BRICKS) : this.brick(x, y, z));
                        }
                    }
                    for (int y = DECK + 1; y <= DECK + 4; y++) {
                        this.b.set(x, y, z, Kit.s(Blocks.AIR));
                    }
                    // die Unterseite des Bogens: wo er zum Nachbarn hin ansteigt, eine umgedrehte Stufe (sieht gerundet aus)
                    if (span != null && bottom < DECK && bottom > 1 && !this.broken(x, bottom, z)) {
                        int prev = this.bottomAt(z - 1);
                        int next = this.bottomAt(z + 1);
                        if (prev > bottom && next <= bottom) {
                            this.b.set(x, bottom, z, VillageBuilder.stairs(Blocks.NETHER_BRICK_STAIRS, Direction.SOUTH, Half.TOP));
                        } else if (next > bottom && prev <= bottom) {
                            this.b.set(x, bottom, z, VillageBuilder.stairs(Blocks.NETHER_BRICK_STAIRS, Direction.NORTH, Half.TOP));
                        }
                    }
                }
                // die Brüstung: Mauerstücke aus Netherziegeln, dazwischen Zaun
                if (!this.broken(0, DECK, z)) {
                    for (int side : new int[] {-1, 1}) {
                        this.b.set(side * 3, DECK + 1, z, z % 4 == 0 ? Kit.s(Blocks.NETHER_BRICK_WALL) : Kit.s(Blocks.NETHER_BRICK_FENCE));
                    }
                }
            }
        }

        /** Die Unterkante der Brücke über z (an Pfeilern und Enden die Fahrbahn selbst). */
        private int bottomAt(int z) {
            int[] span = this.span(z);
            if (span == null) {
                return -1;
            }
            double t = (z - span[0] + 0.5) / (span[1] - span[0] + 1);
            return (int) Math.round(1.0 + 4.0 * Math.sqrt(Math.max(0.0, 1.0 - (2 * t - 1) * (2 * t - 1))));
        }

        /** Welcher Bogen über z liegt (Anfang und Ende), oder null, wenn dort ein Pfeiler steht. */
        private int[] span(int z) {
            int[] edges = {-END, PIERS[0] - 1, PIERS[0] + 1, PIERS[1] - 1, PIERS[1] + 1, PIERS[2] - 1, PIERS[2] + 1, PIERS[3] - 1, PIERS[3] + 1, END};
            for (int i = 0; i < edges.length; i += 2) {
                if (z > edges[i] && z < edges[i + 1]) {
                    return new int[] {edges[i] + 1, edges[i + 1] - 1};
                }
            }
            return null;
        }

        /** Die eingestürzte Mitte: ausgefranste Kanten, rissige Ziegel, Trümmer in der Lava, eine herabhängende Kette. */
        private void breach() {
            for (int z = -6; z <= 6; z++) {
                for (int x = -3; x <= 3; x++) {
                    for (int y = 1; y <= DECK; y++) {
                        BlockState here = this.b.get(x, y, z);
                        if (!here.isAir() && this.b.chance(x, y + 70, z, 0.3f)) {
                            this.b.set(x, y, z, Kit.s(Blocks.CRACKED_NETHER_BRICKS));
                        }
                    }
                }
            }
            // Trümmer: Brocken ragen aus der Lava
            int[][] rubble = {{-2, -1}, {1, 1}, {3, -2}, {-1, 2}, {2, 0}, {-3, 1}, {0, -2}};
            for (int i = 0; i < rubble.length; i++) {
                int x = rubble[i][0];
                int z = rubble[i][1];
                this.b.column(x, z, this.brick(x, -2, z), 0);
                this.b.set(x, -1, z, this.brick(x, -1, z));
                if (i % 2 == 0) {
                    this.b.set(x, 0, z, i % 4 == 0 ? Kit.s(Blocks.NETHER_BRICK_STAIRS) : Kit.s(Blocks.CRACKED_NETHER_BRICKS));
                }
            }
        }

        /** Der Zollturm am Südende: Durchgang auf Höhe der Fahrbahn, darüber die Zollstube mit der Truhe, Zinnen, Banner. */
        private void tollTower() {
            int z0 = -END - 6;
            int z1 = -END;
            for (int x = -4; x <= 4; x++) {
                for (int z = z0; z <= z1; z++) {
                    boolean corner = Math.abs(x) == 4 && (z == z0 || z == z1);
                    this.b.column(x, z, this.brick(x, -1, z), 0);
                    for (int y = -1; y <= 15; y++) {
                        this.b.set(x, y, z, corner ? Kit.s(Blocks.RED_NETHER_BRICKS) : this.brick(x, y, z));
                    }
                    if ((Math.abs(x) == 4 || z == z0 || z == z1) && (x + z) % 2 == 0) {
                        this.b.set(x, 16, z, Kit.s(Blocks.NETHER_BRICK_WALL));
                    }
                }
            }
            // der Durchgang (drei breit, drei hoch) mit Spitzbogen
            for (int z = z0; z <= z1; z++) {
                for (int x = -1; x <= 1; x++) {
                    this.b.set(x, DECK, z, Kit.s(Blocks.RED_NETHER_BRICKS));
                    for (int y = DECK + 1; y <= DECK + 3; y++) {
                        this.b.set(x, y, z, Kit.s(Blocks.AIR));
                    }
                }
                this.b.set(-1, DECK + 3, z, VillageBuilder.stairs(Blocks.NETHER_BRICK_STAIRS, Direction.EAST, Half.TOP));
                this.b.set(1, DECK + 3, z, VillageBuilder.stairs(Blocks.NETHER_BRICK_STAIRS, Direction.WEST, Half.TOP));
            }
            // die Zollstube darüber (Boden y 10), Leiter in einer Nische
            for (int x = -3; x <= 3; x++) {
                for (int z = z0 + 1; z <= z1 - 1; z++) {
                    for (int y = 11; y <= 14; y++) {
                        this.b.set(x, y, z, Kit.s(Blocks.AIR));
                    }
                }
            }
            for (int y = DECK + 1; y <= 10; y++) {
                this.b.set(3, y, -24, Blocks.LADDER.defaultBlockState().setValue(net.minecraft.world.level.block.LadderBlock.FACING, Direction.WEST));
            }
            for (int y = DECK + 1; y <= DECK + 2; y++) {
                this.b.set(2, y, -24, Kit.s(Blocks.AIR));
            }
            Kit.chest(this.b, CHEST.getX(), CHEST.getY(), CHEST.getZ(), Direction.NORTH, Kit.loot("broken_bridge"));
            this.b.set(-1, 11, -26, Kit.s(Blocks.LECTERN));
            Kit.barrel(this.b, -3, 11, -26, Direction.UP);
            Kit.barrel(this.b, -3, 11, -25, Direction.EAST);
            this.b.hangingLantern(0, 14, -24, Blocks.SOUL_LANTERN);
            for (int x : new int[] {-2, 2}) {
                this.b.set(x, 12, z0, Kit.s(Blocks.IRON_BARS));
                this.b.set(x, 12, z1, Kit.s(Blocks.IRON_BARS));
            }
            // rote Banner an der Außenseite
            for (int x : new int[] {-3, 3}) {
                this.b.set(x, 12, z0 - 1, Blocks.WALL_BANNER.pick(DyeColor.RED).defaultBlockState()
                        .setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH));
            }
            this.b.set(0, 15, -24, Kit.s(Blocks.RED_NETHER_BRICKS));
            Kit.campfire(this.b, 0, 16, -24, Blocks.SOUL_CAMPFIRE, true);
        }

        /** Das Torhaus am Nordende: halb verfallen, der Durchgang noch offen, ein Fass mit Zollgut. */
        private void gatehouse() {
            int z0 = END;
            int z1 = END + 4;
            for (int x = -4; x <= 4; x++) {
                for (int z = z0; z <= z1; z++) {
                    this.b.column(x, z, this.brick(x, -1, z), 0);
                    for (int y = -1; y <= 11; y++) {
                        this.b.set(x, y, z, Math.abs(x) == 4 && (z == z0 || z == z1) ? Kit.s(Blocks.RED_NETHER_BRICKS) : this.brick(x, y, z));
                    }
                }
            }
            for (int z = z0; z <= z1; z++) {
                for (int x = -1; x <= 1; x++) {
                    this.b.set(x, DECK, z, Kit.s(Blocks.RED_NETHER_BRICKS));
                    for (int y = DECK + 1; y <= DECK + 3; y++) {
                        this.b.set(x, y, z, Kit.s(Blocks.AIR));
                    }
                }
            }
            Kit.crumble(this.b, -4, z0, 4, z1, 8, 11, 0.45f);
            Kit.lootBarrel(this.b, 1, DECK + 1, z0 + 2, Kit.loot("broken_bridge_toll"));
            Kit.barrel(this.b, 1, DECK + 2, z0 + 2, Direction.UP);
        }

        /** Seelenlaternen auf der Brüstung, alle acht Blöcke. */
        private void lights() {
            for (int z : new int[] {-19, -10, 10, 19}) {
                for (int side : new int[] {-1, 1}) {
                    this.b.set(side * 3, DECK + 1, z, Kit.s(Blocks.NETHER_BRICK_WALL));
                    this.b.set(side * 3, DECK + 2, z, Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, false));
                }
            }
        }

        /** Über der Lücke kreisen Glutgeister, im Zollturm wacht ein Brandgebein. */
        private void dwellers() {
            Kit.mob(this.b, ModEntities.BLAZE.get(), -1, DECK + 2, 0, Direction.SOUTH);
            Kit.mob(this.b, ModEntities.BLAZE.get(), 2, DECK + 4, 2, Direction.NORTH);
            Kit.mob(this.b, ModEntities.WITHER_SKELETON.get(), 0, DECK + 1, -18, Direction.NORTH);
        }
    }
}
