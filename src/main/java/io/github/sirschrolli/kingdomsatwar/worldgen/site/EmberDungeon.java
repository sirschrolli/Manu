package io.github.sirschrolli.kingdomsatwar.worldgen.site;

import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.VillageBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * Der Glutkerker (seit 2.0): In einer Höhle der Unterwelt steht ein Kerker aus poliertem Schwarzstein – eine Ringmauer mit
 * Zinnen und vier Ecktürmen, das Tor mit hochgezogenem Fallgitter, im Hof ein Becken voll Lava und ein Galgen, hinten der
 * Wachturm der Schergen. In seinem Boden führt eine breite Treppe hinab zum Zellengang: sechs Zellen hinter Gittern, mit
 * Ketten, Gebeinen und einem Spawner, an seinem Ende die Halle des Kerkermeisters mit Folterbank, Lavakesseln und seiner Truhe.
 * Brandgebeine halten Wache.
 *
 * <p>Eigene Koordinaten: Mitte des Hofs bei (0, 0, 0), y = 0 ist der Boden des Hofs; vorn (+z) das Tor. Der Zellengang liegt
 * unter dem Wachturm und dem Hof (Boden y = -7, Decke y = -2).
 */
final class EmberDungeon {
    /** Die Ringmauer: x und z von -RING bis RING. */
    static final int RING = 11;
    /** Der Wachturm (hinten): x -7..7, z -10..-2. */
    static final int KX = 7, KZ0 = -10, KZ1 = -2;
    /** Unten: Boden des Zellengangs. */
    static final int DEEP = -7;
    /** Die Halle des Kerkermeisters (unten, vorn): z 9..14. */
    static final int HALL_Z0 = 9, HALL_Z1 = 14;
    static final BlockPos KEEP_CHEST = new BlockPos(5, 0, -9);
    static final BlockPos VAULT_CHEST = new BlockPos(0, DEEP + 2, HALL_Z1);
    /** Hier wird später der Kerkermeister stehen (Mitte der Halle). */
    static final BlockPos MASTER = new BlockPos(0, DEEP + 1, 10);
    static final double PAD = 13.5;

    private EmberDungeon() {}

    static void register() {
        Wonders.add(new Wonders.Wonder(SiteKind.EMBER_DUNGEON, "glutkerker", "Ember Dungeon", "Glutkerker", 3, Wonders.Ground.LAND, false,
                EmberDungeon::plan, EmberDungeon::build, origin -> piece(origin, Rotation.NONE, 0x61D7L),
                new double[][] {Wonders.view(-14, 13, 24, 0, 2, 0), Wonders.view(0, 1.6, 9, 0, 2, -6), Wonders.view(0, 1.6, -4, 0, -3, -9),
                        Wonders.view(0, DEEP + 2.6, -2, 0, DEEP + 1.5, 7), Wonders.view(-2, DEEP + 2.6, 9, 3, DEEP + 1.5, 14),
                        Wonders.view(16, 9, -18, 0, 2, -4)}));
    }

    static Optional<Structure.GenerationStub> plan(Structure.GenerationContext context, SiteTerrain terrain, long seed) {
        BlockPos origin = Kit.cavern(context, terrain, seed, 12, 4, 13);
        return origin == null ? Optional.empty() : Kit.stub(piece(origin, Kit.rotation(seed), seed));
    }

    static SitePiece piece(BlockPos origin, Rotation rotation, long seed) {
        int r = (int) PAD + 5;
        return new SitePiece(SiteKind.EMBER_DUNGEON, 0, origin, rotation, seed, new int[0], SitePiece.box(origin, rotation, -r, DEEP - 2, -r, r, 13, r));
    }

    static void build(VillageBuilder b, SitePiece piece) {
        new Dungeon(b).build();
    }

    private static final class Dungeon {
        private final VillageBuilder b;

        Dungeon(VillageBuilder b) {
            this.b = b;
        }

        void build() {
            Kit.pad(this.b, PAD, Kit.s(Blocks.NETHERRACK), Kit.s(Blocks.NETHERRACK), 13);
            this.underground();
            this.courtyard();
            this.ring();
            this.gate();
            this.keep();
            this.stair();
            this.cells();
            this.hall();
            this.guards();
        }

        /** Mauerwerk: polierte Schwarzsteinziegel, manche rissig, selten vergoldet. */
        private BlockState brick(int x, int y, int z) {
            if (this.b.chance(x, y + 300, z, 0.02f)) {
                return Kit.s(Blocks.GILDED_BLACKSTONE);
            }
            return this.b.chance(x, y, z, 0.22f) ? Kit.s(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS) : Kit.s(Blocks.POLISHED_BLACKSTONE_BRICKS);
        }

        private BlockState pillar() {
            return Blocks.POLISHED_BASALT.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        }

        /** Unten zuerst ein voller Block Mauerwerk (so dringt keine Lava ein), die Räume werden daraus gehöhlt. */
        private void underground() {
            for (int x = -KX; x <= KX; x++) {
                for (int z = KZ0; z <= HALL_Z1 + 1; z++) {
                    for (int y = DEEP; y <= -2; y++) {
                        this.b.set(x, y, z, this.brick(x, y, z));
                    }
                }
            }
        }

        /** Der Hof: Basaltpflaster mit Wegen aus Schwarzstein, links ein Lavabecken mit Feuerschalen, rechts der Galgen. */
        private void courtyard() {
            for (int x = -RING + 1; x <= RING - 1; x++) {
                for (int z = -RING + 1; z <= RING - 1; z++) {
                    boolean path = Math.abs(x) <= 1 || (z >= -1 && z <= 1 && Math.abs(x) <= 6);
                    BlockState floor = path ? Kit.s(this.b.chance(x, 7, z, 0.3f) ? Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS : Blocks.POLISHED_BLACKSTONE_BRICKS)
                            : this.b.chance(x, 8, z, 0.15f) ? Kit.s(Blocks.BLACKSTONE) : Kit.s(Blocks.SMOOTH_BASALT);
                    this.b.set(x, -1, z, floor);
                    for (int y = 0; y <= 10; y++) {
                        this.b.set(x, y, z, Kit.s(Blocks.AIR));
                    }
                }
            }
            // das Lavabecken (die "Glut" im Namen): Einfassung aus Stufen, an den Ecken Feuerschalen
            for (int x = -8; x <= -5; x++) {
                for (int z = 2; z <= 6; z++) {
                    boolean rim = x == -8 || x == -5 || z == 2 || z == 6;
                    if (rim) {
                        this.b.set(x, 0, z, Kit.s(Blocks.POLISHED_BLACKSTONE_BRICK_SLAB));
                    } else {
                        this.b.set(x, -1, z, Kit.s(Blocks.LAVA));
                        this.b.set(x, -2, z, Kit.s(Blocks.MAGMA_BLOCK));
                    }
                }
            }
            for (int[] c : new int[][] {{-8, 2}, {-5, 2}, {-8, 6}, {-5, 6}}) {
                this.b.set(c[0], 0, c[1], Kit.s(Blocks.POLISHED_BLACKSTONE_WALL));
                Kit.campfire(this.b, c[0], 1, c[1], Blocks.SOUL_CAMPFIRE, true);
            }
            // der Galgen: zwei Pfosten aus Karmesinstielen, ein Querbalken, eine Kette
            for (int z : new int[] {3, 6}) {
                for (int y = 0; y <= 4; y++) {
                    this.b.set(7, y, z, Kit.s(Blocks.CRIMSON_STEM));
                }
            }
            for (int z = 3; z <= 6; z++) {
                this.b.set(7, 5, z, Blocks.STRIPPED_CRIMSON_STEM.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z));
            }
            this.b.set(7, 4, 5, Kit.s(Blocks.IRON_CHAIN));
            this.b.set(7, 3, 5, Kit.s(Blocks.IRON_CHAIN));
            this.b.set(6, 0, 4, Kit.s(Blocks.POLISHED_BLACKSTONE_BRICK_SLAB));
            this.b.set(8, 0, 4, Kit.s(Blocks.SKELETON_SKULL));
            // Fässer und eine Karre Seelensand an der Mauer
            Kit.barrel(this.b, 9, 0, 9, Direction.UP);
            Kit.barrel(this.b, 9, 0, 8, Direction.WEST);
            Kit.barrel(this.b, 9, 1, 9, Direction.UP);
            this.b.set(-9, 0, 9, Kit.s(Blocks.SOUL_SAND));
            this.b.set(-9, 0, 8, Kit.s(Blocks.SOUL_SAND));
        }

        /** Die Ringmauer mit Zinnen und vier Ecktürmen (Seelenlaternen oben). */
        private void ring() {
            for (int x = -RING; x <= RING; x++) {
                for (int z = -RING; z <= RING; z++) {
                    if (Math.abs(x) != RING && Math.abs(z) != RING) {
                        continue;
                    }
                    for (int y = -1; y <= 5; y++) {
                        this.b.set(x, y, z, y == 3 ? Kit.s(Blocks.CHISELED_POLISHED_BLACKSTONE) : this.brick(x, y, z));
                    }
                    // nach unten bis zum Grund verlängern (die Mauer steht, auch wo der Boden abfällt)
                    this.b.column(x, z, this.brick(x, -2, z), 0);
                    if ((x + z) % 2 == 0) {
                        this.b.set(x, 6, z, Kit.s(Blocks.POLISHED_BLACKSTONE_BRICK_WALL));
                    }
                }
            }
            Kit.crumble(this.b, -RING + 2, -RING, RING - 2, -RING, 5, 6, 0.18f);
            Kit.crumble(this.b, -RING, -RING + 2, -RING, RING - 2, 5, 6, 0.18f);
            Kit.crumble(this.b, RING, -RING + 2, RING, RING - 2, 5, 6, 0.18f);
            Kit.crumble(this.b, -RING + 2, RING, -5, RING, 5, 6, 0.18f);
            Kit.crumble(this.b, 5, RING, RING - 2, RING, 5, 6, 0.18f);
            for (int sx : new int[] {-1, 1}) {
                for (int sz : new int[] {-1, 1}) {
                    int cx = sx * RING;
                    int cz = sz * RING;
                    for (int x = cx - 1; x <= cx + 1; x++) {
                        for (int z = cz - 1; z <= cz + 1; z++) {
                            boolean corner = x != cx && z != cz;
                            for (int y = -1; y <= 8; y++) {
                                this.b.set(x, y, z, corner ? this.pillar() : this.brick(x, y, z));
                            }
                            this.b.column(x, z, this.brick(x, -2, z), 0);
                            if (corner) {
                                this.b.set(x, 9, z, Kit.s(Blocks.POLISHED_BLACKSTONE_BRICK_WALL));
                            }
                        }
                    }
                    this.b.set(cx, 9, cz, Kit.s(Blocks.POLISHED_BLACKSTONE_BRICK_SLAB));
                    this.b.set(cx, 10, cz, Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, false));
                    // Schießscharten nach außen
                    this.b.set(cx + sx * 1, 5, cz, Kit.s(Blocks.IRON_BARS));
                    this.b.set(cx, 5, cz + sz * 1, Kit.s(Blocks.IRON_BARS));
                }
            }
        }

        /** Das Tor: drei breit, darüber der Bogen und das hochgezogene Fallgitter, beiderseits Tortürme mit Feuerschalen. */
        private void gate() {
            for (int x = -1; x <= 1; x++) {
                for (int y = 0; y <= 2; y++) {
                    this.b.set(x, y, RING, Kit.s(Blocks.AIR));
                }
                this.b.set(x, 3, RING, Kit.s(Blocks.IRON_BARS));
                this.b.set(x, -1, RING, Kit.s(Blocks.POLISHED_BLACKSTONE_BRICKS));
            }
            for (int sx : new int[] {-1, 1}) {
                for (int x = sx * 2; Math.abs(x) <= 3; x += sx) {
                    for (int z = RING; z <= RING + 1; z++) {
                        for (int y = -1; y <= 7; y++) {
                            this.b.set(x, y, z, Math.abs(x) == 3 && z == RING + 1 ? this.pillar() : this.brick(x, y, z));
                        }
                        this.b.column(x, z, this.brick(x, -2, z), 0);
                    }
                }
                this.b.set(sx * 3, 8, RING + 1, Kit.s(Blocks.POLISHED_BLACKSTONE_BRICK_WALL));
                Kit.campfire(this.b, sx * 2, 8, RING + 1, Blocks.SOUL_CAMPFIRE, true);
                this.b.set(sx * 2, 1, RING + 2, Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, false));
                this.b.set(sx * 2, 0, RING + 2, Kit.s(Blocks.POLISHED_BLACKSTONE_WALL));
            }
            for (int x = -1; x <= 1; x++) {
                this.b.set(x, 4, RING + 1, VillageBuilder.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Direction.NORTH, Half.TOP));
                for (int y = 5; y <= 6; y++) {
                    this.b.set(x, y, RING + 1, this.brick(x, y, RING + 1));
                }
                this.b.set(x, 7, RING + 1, x == 0 ? Kit.s(Blocks.CHISELED_POLISHED_BLACKSTONE) : Kit.s(Blocks.POLISHED_BLACKSTONE_BRICK_WALL));
            }
            // ein Pfad vor das Tor
            for (int z = RING + 1; z <= RING + 5; z++) {
                for (int x = -1; x <= 1; x++) {
                    if (this.b.chance(x, 9, z, 0.8f)) {
                        this.b.set(x, -1, z, Kit.s(Blocks.BLACKSTONE));
                    }
                }
            }
        }

        /** Der Wachturm der Schergen: zwei Geschosse unter einem Zinnendach, unten die Wachstube und der Abgang. */
        private void keep() {
            for (int x = -KX; x <= KX; x++) {
                for (int z = KZ0; z <= KZ1; z++) {
                    boolean wall = Math.abs(x) == KX || z == KZ0 || z == KZ1;
                    this.b.set(x, -1, z, Kit.s(Blocks.POLISHED_BLACKSTONE));
                    for (int y = 0; y <= 8; y++) {
                        boolean corner = Math.abs(x) == KX && (z == KZ0 || z == KZ1);
                        this.b.set(x, y, z, corner ? this.pillar() : wall ? this.brick(x, y, z) : Kit.s(Blocks.AIR));
                    }
                    this.b.set(x, 4, z, wall ? Kit.s(Blocks.CHISELED_POLISHED_BLACKSTONE) : Kit.s(Blocks.POLISHED_BLACKSTONE_BRICK_SLAB));
                    this.b.set(x, 9, z, wall && (x + z) % 2 == 0 ? Kit.s(Blocks.POLISHED_BLACKSTONE_BRICK_WALL)
                            : wall ? Kit.s(Blocks.AIR) : Kit.s(Blocks.POLISHED_BLACKSTONE_BRICK_SLAB));
                    if (!wall) {
                        this.b.set(x, 8, z, Kit.s(Blocks.POLISHED_BLACKSTONE_BRICKS));
                    }
                }
            }
            // die Decke zwischen den Geschossen: Stufen oben im Block, nur über der Wachstube (der Rest ist offen bis oben)
            for (int x = -KX + 1; x <= KX - 1; x++) {
                for (int z = KZ0 + 1; z <= KZ1 - 1; z++) {
                    this.b.set(x, 4, z, x >= -2 && x <= 2 ? Kit.s(Blocks.AIR) : VillageBuilder.slab(Blocks.POLISHED_BLACKSTONE_BRICK_SLAB, SlabType.TOP));
                }
            }
            // zwei Türen beiderseits (in der Mitte liegt drinnen die Treppe), dazwischen ein vergittertes Fenster
            for (int x : new int[] {-4, 4}) {
                this.b.set(x, 0, KZ1, Kit.s(Blocks.AIR));
                this.b.set(x, 1, KZ1, Kit.s(Blocks.AIR));
                this.b.set(x, 2, KZ1, VillageBuilder.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Direction.SOUTH, Half.TOP));
                Kit.window(this.b, x, 6, KZ1, 1, Kit.s(Blocks.IRON_BARS));
            }
            Kit.window(this.b, 0, 1, KZ1, 2, Kit.s(Blocks.IRON_BARS));
            Kit.window(this.b, 0, 6, KZ1, 1, Kit.s(Blocks.IRON_BARS));
            for (int z : new int[] {-8, -5}) {
                Kit.window(this.b, -KX, 1, z, 2, Kit.s(Blocks.IRON_BARS));
                Kit.window(this.b, KX, 1, z, 2, Kit.s(Blocks.IRON_BARS));
                Kit.window(this.b, -KX, 6, z, 1, Kit.s(Blocks.IRON_BARS));
                Kit.window(this.b, KX, 6, z, 1, Kit.s(Blocks.IRON_BARS));
            }
            // die Wachstube: Tisch mit Bänken, Waffenbank (Schleifstein, Amboss), die Truhe, eine Feuerstelle
            for (int x = 4; x <= 5; x++) {
                this.b.set(x, 0, -6, VillageBuilder.slab(Blocks.POLISHED_BLACKSTONE_SLAB, SlabType.TOP));
                this.b.set(x, 0, -5, VillageBuilder.stairs(Blocks.CRIMSON_STAIRS, Direction.SOUTH, Half.BOTTOM));
                this.b.set(x, 0, -7, VillageBuilder.stairs(Blocks.CRIMSON_STAIRS, Direction.NORTH, Half.BOTTOM));
            }
            Kit.candles(this.b, 4, 1, -6, Blocks.CANDLE, 2, true);
            this.b.set(6, 0, -4, Kit.s(Blocks.GRINDSTONE));
            this.b.set(6, 0, -5, Kit.s(Blocks.ANVIL));
            Kit.chest(this.b, KEEP_CHEST.getX(), KEEP_CHEST.getY(), KEEP_CHEST.getZ(), Direction.SOUTH, Kit.loot("ember_dungeon"));
            Kit.lootBarrel(this.b, 6, 0, -9, Kit.loot("ember_dungeon_cell"));
            this.b.set(6, 0, -8, Kit.s(Blocks.CRIMSON_SHELF));
            // die Feuerstelle im Westen
            this.b.set(-6, 0, -4, Kit.s(Blocks.POLISHED_BLACKSTONE_BRICKS));
            Kit.campfire(this.b, -6, 1, -4, Blocks.SOUL_CAMPFIRE, true);
            this.b.set(-6, 2, -4, Kit.s(Blocks.POLISHED_BLACKSTONE_BRICK_WALL));
            this.b.set(-6, 3, -4, Kit.s(Blocks.POLISHED_BLACKSTONE_BRICK_WALL));
            Kit.barrel(this.b, -6, 0, -9, Direction.UP);
            Kit.barrel(this.b, -5, 0, -9, Direction.SOUTH);
            // oben: die Schlafstätte der Schergen
            this.b.bed(-5, 5, -9, Blocks.BED.pick(net.minecraft.world.item.DyeColor.BLACK), Direction.SOUTH);
            this.b.bed(-3, 5, -9, Blocks.BED.pick(net.minecraft.world.item.DyeColor.RED), Direction.SOUTH);
            Kit.barrel(this.b, 5, 5, -9, Direction.UP);
            this.b.ladder(-6, 0, 4, -6, Direction.EAST);
            this.b.set(-6, 4, -6, Kit.s(Blocks.AIR));
            this.b.hangingLantern(0, 3, -6, Blocks.SOUL_LANTERN);
            this.b.hangingLantern(4, 7, -5, Blocks.SOUL_LANTERN);
        }

        /** Die breite Treppe hinab: hinten im Wachturm beginnt sie und führt nach vorn in den Zellengang; ein Geländer ringsum. */
        private void stair() {
            for (int k = 0; k <= 5; k++) {
                int z = KZ0 + 1 + k;
                for (int x = -1; x <= 1; x++) {
                    // über der Stufe Luft (Kopfhöhe), die Stufe selbst als Treppe nach vorn (Süden) gerichtet
                    for (int y = -1 - k + 1; y <= 0; y++) {
                        this.b.set(x, y, z, Kit.s(Blocks.AIR));
                    }
                    this.b.set(x, -1 - k, z, VillageBuilder.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Direction.NORTH, Half.BOTTOM));
                    this.b.set(x, -2 - k, z, this.brick(x, -2 - k, z));
                }
            }
            // die Öffnung im Boden: ein Geländer an den Seiten und vorn; betreten wird die oberste Stufe von der Seite
            for (int z = KZ0 + 2; z <= KZ0 + 7; z++) {
                this.b.set(-2, 0, z, Kit.s(Blocks.POLISHED_BLACKSTONE_BRICK_WALL));
                this.b.set(2, 0, z, Kit.s(Blocks.POLISHED_BLACKSTONE_BRICK_WALL));
            }
            for (int x = -1; x <= 1; x++) {
                this.b.set(x, 0, KZ0 + 7, Kit.s(Blocks.POLISHED_BLACKSTONE_BRICK_WALL));
                this.b.set(x, -1, KZ0 + 7, Kit.s(Blocks.POLISHED_BLACKSTONE));
            }
        }

        /**
         * Der Zellengang: vom Fuß der Treppe nach vorn, drei Blöcke breit; beiderseits drei Zellen hinter Gittern (eine mit
         * gebrochenen Stäben, eine mit dem Spawner), dazwischen Säulen; Ketten und Seelenlaternen an der Decke.
         */
        private void cells() {
            int z0 = KZ0 + 7;
            int z1 = HALL_Z0 - 1;
            // der Gang (auch unter der Treppe weiter bis zur Wand)
            for (int z = z0; z <= z1; z++) {
                for (int x = -1; x <= 1; x++) {
                    for (int y = DEEP + 1; y <= -3; y++) {
                        this.b.set(x, y, z, Kit.s(Blocks.AIR));
                    }
                    this.b.set(x, DEEP, z, this.b.chance(x, 12, z, 0.25f) ? Kit.s(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS) : Kit.s(Blocks.POLISHED_BLACKSTONE));
                }
            }
            // Zellen: beiderseits, je drei (z-Spannen), Gitter zum Gang
            int[][] spans = {{-3, -1}, {1, 3}, {5, 7}};
            for (int side : new int[] {-1, 1}) {
                for (int i = 0; i < spans.length; i++) {
                    int za = spans[i][0];
                    int zb = spans[i][1];
                    for (int x = 3; x <= 6; x++) {
                        for (int z = za; z <= zb; z++) {
                            for (int y = DEEP + 1; y <= -3; y++) {
                                this.b.set(side * x, y, z, Kit.s(Blocks.AIR));
                            }
                            this.b.set(side * x, DEEP, z, Kit.s(this.b.chance(x * side, 13, z, 0.5f) ? Blocks.SOUL_SOIL : Blocks.BLACKSTONE));
                        }
                    }
                    boolean broken = side < 0 && i == 1;
                    for (int z = za; z <= zb; z++) {
                        for (int y = DEEP + 1; y <= -3; y++) {
                            boolean gap = broken && z == za + 1 && y <= DEEP + 2;
                            this.b.set(side * 2, y, z, gap ? Kit.s(Blocks.AIR) : Kit.s(Blocks.IRON_BARS));
                        }
                    }
                    // Einrichtung: Ketten an der Wand, Gebeine, Spinnweben, eine Pritsche, ein Fass
                    int bx = side * 6;
                    this.b.set(bx, -3, za, Kit.s(Blocks.IRON_CHAIN));
                    this.b.set(bx, -4, za, Kit.s(Blocks.IRON_CHAIN));
                    this.b.set(side * 5, DEEP + 1, zb, Kit.s(Blocks.SKELETON_SKULL).setValue(SkullBlock.ROTATION, Math.floorMod(i * 5 + side * 3, 16)));
                    this.b.set(side * 3, DEEP + 1, za, Kit.s(Blocks.CARPET.pick(net.minecraft.world.item.DyeColor.BROWN)));
                    this.b.set(side * 4, DEEP + 1, za, Kit.s(Blocks.CARPET.pick(net.minecraft.world.item.DyeColor.BROWN)));
                    if (i == 2) {
                        Kit.lootBarrel(this.b, side * 6, DEEP + 1, zb, Kit.loot("ember_dungeon_cell"));
                    }
                    Kit.cobwebs(this.b, Math.min(side * 6, side * 4), -3, za, Math.max(side * 6, side * 4), -3, zb, 0.25f);
                }
            }
            // der Spawner in der mittleren Zelle rechts: Glutgeister hinter Gittern
            this.b.set(5, DEEP + 1, 2, Kit.s(Blocks.AIR));
            DeepLair.spawner(this.b, 5, DEEP + 1, 2, ModEntities.BLAZE.get());
            // Säulen zwischen den Zellen, Laternen und Ketten im Gang
            for (int z : new int[] {0, 4, 8}) {
                for (int side : new int[] {-2, 2}) {
                    for (int y = DEEP + 1; y <= -3; y++) {
                        this.b.set(side, y, z, this.pillar());
                    }
                }
            }
            for (int z : new int[] {-1, 3, 7}) {
                this.b.hang(0, -3, z, Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
            }
            for (int z : new int[] {1, 5}) {
                this.b.set(-1, -3, z, Kit.s(Blocks.IRON_CHAIN));
                this.b.set(1, -3, z, Kit.s(Blocks.IRON_CHAIN));
            }
        }

        /** Die Halle des Kerkermeisters: Säulen, eine Folterbank, Lavakessel, Käfige, der Thron und seine Truhe. */
        private void hall() {
            for (int x = -KX + 1; x <= KX - 1; x++) {
                for (int z = HALL_Z0; z <= HALL_Z1; z++) {
                    for (int y = DEEP + 1; y <= -3; y++) {
                        this.b.set(x, y, z, Kit.s(Blocks.AIR));
                    }
                    boolean pattern = (x + z) % 2 == 0;
                    this.b.set(x, DEEP, z, Kit.s(pattern ? Blocks.POLISHED_BLACKSTONE_BRICKS : Blocks.POLISHED_BLACKSTONE));
                }
            }
            // der Durchgang vom Zellengang
            for (int x = -1; x <= 1; x++) {
                for (int y = DEEP + 1; y <= -4; y++) {
                    this.b.set(x, y, HALL_Z0 - 1, Kit.s(Blocks.AIR));
                }
            }
            for (int[] p : new int[][] {{-4, 10}, {4, 10}, {-4, 13}, {4, 13}}) {
                for (int y = DEEP + 1; y <= -3; y++) {
                    this.b.set(p[0], y, p[1], this.pillar());
                }
            }
            // der Thron am Ende, davor die Truhe (sie steht auf dem Podest)
            for (int x = -2; x <= 2; x++) {
                this.b.set(x, DEEP + 1, HALL_Z1, VillageBuilder.slab(Blocks.POLISHED_BLACKSTONE_BRICK_SLAB, SlabType.BOTTOM));
            }
            this.b.set(0, DEEP + 1, HALL_Z1, Kit.s(Blocks.POLISHED_BLACKSTONE_BRICKS));
            Kit.chest(this.b, VAULT_CHEST.getX(), VAULT_CHEST.getY(), VAULT_CHEST.getZ(), Direction.NORTH, Kit.loot("ember_dungeon_vault"));
            this.b.set(-1, DEEP + 2, HALL_Z1, VillageBuilder.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Direction.NORTH, Half.BOTTOM));
            this.b.set(1, DEEP + 2, HALL_Z1, VillageBuilder.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Direction.NORTH, Half.BOTTOM));
            this.b.set(-2, DEEP + 2, HALL_Z1, Kit.s(Blocks.GILDED_BLACKSTONE));
            this.b.set(2, DEEP + 2, HALL_Z1, Kit.s(Blocks.GILDED_BLACKSTONE));
            this.b.set(-2, DEEP + 3, HALL_Z1, Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, false));
            this.b.set(2, DEEP + 3, HALL_Z1, Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, false));
            // die Folterbank, Lavakessel, Amboss, Schleifstein
            for (int z = 10; z <= 12; z++) {
                this.b.set(-5, DEEP + 1, z, VillageBuilder.slab(Blocks.CRIMSON_SLAB, SlabType.TOP));
            }
            this.b.set(-5, -3, 10, Kit.s(Blocks.IRON_CHAIN));
            this.b.set(-5, -3, 12, Kit.s(Blocks.IRON_CHAIN));
            this.b.set(5, DEEP + 1, 10, Kit.s(Blocks.LAVA_CAULDRON));
            this.b.set(5, DEEP + 1, 12, Kit.s(Blocks.LAVA_CAULDRON));
            this.b.set(5, DEEP + 1, 11, Kit.s(Blocks.ANVIL));
            this.b.set(-6, DEEP + 1, 14, Kit.s(Blocks.GRINDSTONE));
            // zwei Käfige aus Gittern
            for (int[] c : new int[][] {{-6, 9}, {6, 14}}) {
                for (int y = DEEP + 1; y <= -4; y++) {
                    this.b.set(c[0], y, c[1], Kit.s(Blocks.IRON_BARS));
                }
                this.b.set(c[0], -3, c[1], Kit.s(Blocks.IRON_CHAIN));
            }
            this.b.set(6, DEEP + 1, 9, Kit.s(Blocks.WITHER_SKELETON_SKULL).setValue(SkullBlock.ROTATION, 6));
            Kit.campfire(this.b, 0, DEEP + 1, 12, Blocks.SOUL_CAMPFIRE, true);
            this.b.hang(0, -3, 11, Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
            Kit.cobwebs(this.b, -6, -3, HALL_Z0, -4, -3, HALL_Z1, 0.15f);
            Kit.cobwebs(this.b, 4, -3, HALL_Z0, 6, -3, HALL_Z1, 0.15f);
        }

        /** Brandgebeine halten Wache: zwei im Hof vor dem Wachturm, eines im Zellengang. */
        private void guards() {
            for (int[] g : new int[][] {{-3, 0, 1}, {3, 0, 1}, {0, DEEP + 1, 5}}) {
                Mob guard = Kit.mob(this.b, ModEntities.WITHER_SKELETON.get(), g[0], g[1], g[2], Direction.SOUTH);
                if (guard != null && guard.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()) {
                    guard.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.STONE_SWORD));
                }
            }
        }
    }
}
