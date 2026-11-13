package io.github.sirschrolli.kingdomsatwar.worldgen.site;

import io.github.sirschrolli.kingdomsatwar.loot.ModLootTables;
import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.VillageBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.SpeleothemBlock;
import net.minecraft.world.level.block.TripWireBlock;
import net.minecraft.world.level.block.TripWireHookBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SpeleothemThickness;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

/**
 * Der Schlangentempel: Aus dem Boden des Dschungels ragt der gewaltige Kopf einer steinernen Schlange, bemoost und von
 * Ranken überwachsen, mit leuchtend grünen Augen, einem Kamm aus Dornen und weit offenem Maul voller Fänge. Durch das Maul
 * führt der Schlund – mit einer Stolperdrahtfalle, die Pfeile aus den Wänden schießt – eine Treppe hinab in die Halle der
 * Schlangenpriester: Säulen in Gestalt aufgerichteter Schlangen, ein Wassergraben um den Altar, die Opfertruhen; hinter
 * rissigem Mauerwerk ist eine weitere Truhe versteckt.
 *
 * <p>Eigene Koordinaten: die Schnauze vorn bei z = 0 (das Maul öffnet sich nach Norden), x quer, y = 0 ist der Boden.
 */
final class SerpentTemple {
    /** Mitte der Schlangengrube (z), Radius der Grube, der Insel und des Grabens. */
    static final int PIT_Z = 40;
    static final int PIT_R = 14;
    static final double ISLAND_R = 6.0;
    static final double MOAT_R = 8.5;
    /** Bodenblöcke der Grube (man steht eine Höhe darüber), die Decke, der Boden der Empore. */
    static final int PIT_FLOOR = -14;
    static final int PIT_CEILING = -2;
    static final int GALLERY_FLOOR = -8;
    /** Hier beginnt die Treppe von der Empore hinab. */
    static final int STAIRS_Z = 22;
    /** Wo der Schlangenaltar steht: mitten auf der Insel. */
    static final BlockPos ALTAR = new BlockPos(0, PIT_FLOOR + 2, PIT_Z);

    private SerpentTemple() {}

    static Optional<Structure.GenerationStub> plan(Structure.GenerationContext context, SiteTerrain terrain, long seed) {
        ChunkPos chunk = context.chunkPos();
        int x = chunk.getMiddleBlockX();
        int z = chunk.getMiddleBlockZ();
        Rotation rotation = Rotation.values()[Math.floorMod((int) (seed >>> 5), 4)];
        BlockPos mid = new BlockPos(0, 0, 10).rotate(rotation);
        int[] span = terrain.span(x + mid.getX(), z + mid.getZ(), 9, 3);
        if (terrain.water(x, z) || span[1] - span[0] > 7) {
            return Optional.empty();
        }
        int y = terrain.ground(x, z);
        // über der Grube muss Erde liegen (sonst ragte ihre Decke aus dem Boden)
        BlockPos pit = new BlockPos(0, 0, PIT_Z).rotate(rotation);
        if (terrain.span(x + pit.getX(), z + pit.getZ(), 15, 5)[0] < y - 2) {
            return Optional.empty();
        }
        BlockPos origin = new BlockPos(x, y, z);
        BoundingBox box = SitePiece.box(origin, rotation, -18, PIT_FLOOR - 3, -13, 18, 32, PIT_Z + 17);
        return Optional.of(new Structure.GenerationStub(origin,
                builder -> builder.addPiece(new SitePiece(SiteKind.SERPENT_TEMPLE, 0, origin, rotation, seed, new int[0], box))));
    }

    static void build(VillageBuilder b, SitePiece piece) {
        new Serpent(b).build();
    }

    private static final class Serpent {
        private final VillageBuilder b;

        Serpent(VillageBuilder b) {
            this.b = b;
        }

        /** Halbe Breite und Höhe des Kopfes an der Stelle z (vorn schmal und flach, hinten breit, am Nacken im Boden). */
        double width(int z) {
            return 3.6 + 2.6 * Math.sin(Math.min(1.0, z / 9.0) * Math.PI / 2);
        }

        double height(int z) {
            double h = 5.0 + 4.0 * Math.min(1.0, z / 10.0);
            return z > 14 ? h - (z - 14) * 1.7 : h;
        }

        boolean inside(int x, int y, int z) {
            if (z < 0 || z > 19 || y < 0) {
                return false;
            }
            double w = this.width(z);
            double h = this.height(z);
            if (h <= 0) {
                return false;
            }
            double ax = Math.abs(x) / w;
            double ay = y / h;
            return Math.pow(ax, 2.6) + Math.pow(ay, 2.6) <= 1.0;
        }

        BlockState scale(int x, int y, int z) {
            float n = this.b.noise(x, y, z);
            if (n < 0.36f) {
                return Blocks.MOSSY_COBBLESTONE.defaultBlockState();
            }
            if (n < 0.6f) {
                return Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
            }
            if (n < 0.74f) {
                return Blocks.STONE_BRICKS.defaultBlockState();
            }
            if (n < 0.84f) {
                return Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
            }
            return Blocks.DYED_TERRACOTTA.pick(DyeColor.GREEN).defaultBlockState();
        }

        void build() {
            this.glade();
            this.coils();
            this.head();
            this.mouth();
            this.throat();
            this.pit();
            this.vines();
        }

        /**
         * Eine Lichtung um den Kopf: Der Tempel entsteht nach den Bäumen des Dschungels; Stämme, Laub und Ranken in seiner
         * Nähe weichen (abgetrenntes Laub weiter draußen welkt von selbst), damit man den Kopf von weitem sieht.
         */
        void glade() {
            for (int x = -16; x <= 16; x++) {
                for (int z = -12; z <= 30; z++) {
                    double dx = x / 16.0;
                    double dz = (z - 9) / 21.0;
                    if (dx * dx + dz * dz > 1.0) {
                        continue;
                    }
                    for (int y = 0; y <= 32; y++) {
                        BlockState s = this.b.get(x, y, z);
                        if (s.is(net.minecraft.tags.BlockTags.LOGS) || s.is(net.minecraft.tags.BlockTags.LEAVES) || s.is(Blocks.VINE)
                                || s.is(Blocks.COCOA) || s.is(Blocks.BAMBOO) || s.is(Blocks.MELON)) {
                            this.b.set(x, y, z, Blocks.AIR.defaultBlockState());
                        }
                    }
                }
            }
        }

        /**
         * Der Leib der Schlange: neben dem Kopf tauchen drei Windungen aus dem Boden auf und wieder ab, jede kleiner als
         * die vorige, mit einem Kamm aus Dornen – so erkennt man schon von fern, was hier liegt.
         */
        void coils() {
            for (int k = 0; k < 3; k++) {
                double zc = 5 + k * 7.5;
                double xc = 11.5 - k * 1.2;
                double radius = 3.8 - k * 0.5;
                double thick = 1.7 - k * 0.25;
                for (int x = (int) (xc - thick - 1); x <= (int) (xc + thick + 1); x++) {
                    for (int z = (int) (zc - radius - thick - 1); z <= (int) (zc + radius + thick + 1); z++) {
                        for (int y = -1; y <= radius + thick + 1; y++) {
                            double ring = Math.sqrt((z - zc) * (z - zc) + y * y) - radius;
                            double d = Math.sqrt((x - xc) * (x - xc) + ring * ring);
                            if (d <= thick) {
                                this.b.set(x, y, z, this.scale(x, y, z + 120));
                            }
                        }
                    }
                }
                // Dornen auf dem Scheitel der Windung
                int top = (int) Math.round(radius + thick);
                for (int dz = -2; dz <= 2; dz += 2) {
                    this.b.set((int) Math.round(xc), top + 1 - Math.abs(dz) / 2, (int) Math.round(zc) + dz, Blocks.MOSSY_STONE_BRICK_WALL.defaultBlockState());
                }
            }
        }

        /** Der Kopf: eine gerundete Schale aus bemoostem Stein, innen hohl, unten der Unterkiefer als Boden. */
        void head() {
            for (int x = -8; x <= 8; x++) {
                for (int z = -1; z <= 20; z++) {
                    // Fundament bis zum Grund
                    for (int y = -1; y >= -6; y--) {
                        if (this.b.get(x, y, z).isSolidRender() && y < -1) {
                            break;
                        }
                        if (this.inside(x, 0, z)) {
                            this.b.set(x, y, z, Blocks.MOSSY_COBBLESTONE.defaultBlockState());
                        }
                    }
                    for (int y = 0; y <= 10; y++) {
                        if (!this.inside(x, y, z)) {
                            continue;
                        }
                        boolean shell = !this.inside(x + 1, y, z) || !this.inside(x - 1, y, z) || !this.inside(x, y + 1, z)
                                || !this.inside(x, y, z + 1) || !this.inside(x, y, z - 1) || y == 0;
                        this.b.set(x, y, z, shell ? this.scale(x, y, z) : Blocks.AIR.defaultBlockState());
                    }
                }
            }
            // Kamm aus Dornen auf dem Scheitel
            for (int z = 4; z <= 16; z++) {
                int top = (int) Math.floor(this.height(z) * 0.999);
                if (top < 1) {
                    continue;
                }
                this.b.set(0, top + 1, z, Blocks.MOSSY_STONE_BRICK_WALL.defaultBlockState());
                if (z % 3 == 1) {
                    this.b.set(0, top + 2, z, Blocks.MOSSY_STONE_BRICK_WALL.defaultBlockState());
                }
            }
            // Augen: grünes Glas vor einem Licht, dazu eine Braue
            for (int side : new int[] {-1, 1}) {
                int z = 8;
                int y = 6;
                int x = side * (int) Math.round(this.width(z) * Math.pow(Math.max(0, 1 - Math.pow(y / this.height(z), 2.6)), 1 / 2.6));
                for (int dy = 0; dy <= 1; dy++) {
                    for (int dz = 0; dz <= 1; dz++) {
                        this.b.set(x, y - dy, z + dz, Blocks.STAINED_GLASS.pick(dy == dz ? DyeColor.LIME : DyeColor.GREEN).defaultBlockState());
                        this.b.set(x - side, y - dy, z + dz, Blocks.SEA_LANTERN.defaultBlockState());
                    }
                }
                this.b.set(x, y + 1, z - 1, VillageBuilder.stairs(Blocks.MOSSY_STONE_BRICK_STAIRS, side > 0 ? Direction.WEST : Direction.EAST,
                        Half.TOP));
                // Nüstern
                this.b.set(side * 2, (int) Math.floor(this.height(1)) , 1, Blocks.AIR.defaultBlockState());
            }
        }

        /** Das offene Maul: oben Fänge aus Tropfstein, unten die Zahnreihe des Unterkiefers. */
        void mouth() {
            for (int x = -3; x <= 3; x++) {
                for (int z = -1; z <= 4; z++) {
                    for (int y = 1; y <= 3; y++) {
                        this.b.set(x, y, z, Blocks.AIR.defaultBlockState());
                    }
                }
            }
            for (int x = -3; x <= 3; x++) {
                this.b.set(x, 4, 0, this.scale(x, 4, 0));
                this.b.set(x, 0, 0, Blocks.MOSSY_STONE_BRICKS.defaultBlockState());
            }
            for (int x : new int[] {-3, 3}) {
                this.b.set(x, 3, 0, this.fang(Direction.DOWN, SpeleothemThickness.FRUSTUM));
                this.b.set(x, 2, 0, this.fang(Direction.DOWN, SpeleothemThickness.TIP));
            }
            for (int x : new int[] {-1, 1}) {
                this.b.set(x, 3, 0, this.fang(Direction.DOWN, SpeleothemThickness.TIP));
            }
            for (int x : new int[] {-2, 2}) {
                this.b.set(x, 1, 0, this.fang(Direction.UP, SpeleothemThickness.TIP));
            }
        }

        BlockState fang(Direction dir, SpeleothemThickness thickness) {
            return Blocks.POINTED_DRIPSTONE.defaultBlockState().setValue(SpeleothemBlock.TIP_DIRECTION, dir)
                    .setValue(SpeleothemBlock.THICKNESS, thickness);
        }

        /**
         * Der Schlund: ein enger Gang (drei breit) mit der Falle – ein Stolperdraht quer über den Weg, in den Wänden vier
         * Werfer voller Pfeile –, dann eine Treppe hinab in die Halle.
         */
        void throat() {
            for (int z = 5; z <= 9; z++) {
                for (int x = -2; x <= 2; x++) {
                    for (int y = 0; y <= 4; y++) {
                        boolean wall = Math.abs(x) == 2 || y == 0 || y == 4;
                        this.b.set(x, y, z, wall ? this.scale(x, y, z + 40) : Blocks.AIR.defaultBlockState());
                    }
                }
            }
            // die Falle bei z = 7
            this.b.set(-1, 1, 7, Blocks.TRIPWIRE_HOOK.defaultBlockState().setValue(TripWireHookBlock.FACING, Direction.EAST)
                    .setValue(TripWireHookBlock.ATTACHED, true));
            this.b.set(1, 1, 7, Blocks.TRIPWIRE_HOOK.defaultBlockState().setValue(TripWireHookBlock.FACING, Direction.WEST)
                    .setValue(TripWireHookBlock.ATTACHED, true));
            this.b.set(0, 1, 7, Blocks.TRIPWIRE.defaultBlockState().setValue(TripWireBlock.ATTACHED, true).setValue(TripWireBlock.EAST, true)
                    .setValue(TripWireBlock.WEST, true));
            this.b.set(-2, 1, 7, Blocks.MOSSY_STONE_BRICKS.defaultBlockState());
            this.b.set(2, 1, 7, Blocks.MOSSY_STONE_BRICKS.defaultBlockState());
            for (int side : new int[] {-1, 1}) {
                Direction shoot = side < 0 ? Direction.EAST : Direction.WEST;
                for (int[] at : new int[][] {{side * 2, 2, 7}, {side * 2, 1, 6}}) {
                    this.b.chest(at[0], at[1], at[2], Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING, shoot),
                            BuiltInLootTables.JUNGLE_TEMPLE_DISPENSER);
                }
            }
            // Treppe hinab (z 10..16, sechs Stufen)
            for (int z = 10; z <= 16; z++) {
                int floor = -(z - 10);
                for (int x = -2; x <= 2; x++) {
                    for (int y = floor - 1; y <= floor + 5; y++) {
                        boolean wall = Math.abs(x) == 2 || y == floor - 1 || y == floor + 5;
                        this.b.set(x, y, z, wall ? this.scale(x, y, z + 40) : Blocks.AIR.defaultBlockState());
                    }
                    if (Math.abs(x) <= 1 && z < 16) {
                        this.b.set(x, floor, z, VillageBuilder.stairs(Blocks.MOSSY_STONE_BRICK_STAIRS, Direction.NORTH, Half.BOTTOM));
                    }
                }
            }
        }

        // =================================================================================
        // Die Schlangengrube (seit 1.10)
        // =================================================================================

        /** Abstand (waagerecht) von der Mitte der Grube. */
        static double radius(int x, int z) {
            double dz = z - PIT_Z;
            return Math.sqrt(x * x + dz * dz);
        }

        /** Winkel um die Mitte der Grube (0 = Süden, zur Rückwand). */
        static double angle(int x, int z) {
            return Math.atan2(x, z - PIT_Z);
        }

        /**
         * Die Schlangengrube: eine runde Halle tief unter dem Tempel, umschlossen von doppelten Mauern. Oben eine Empore, von der
         * eine Treppe hinabführt; unten ein Wassergraben um eine Insel, auf ihr der Schlangenaltar, um den Quetzara schläft. Acht
         * Säulen, um die sich steinerne Schlangen winden, Augen in den Wänden, ein Fries aus Federn, an der Decke eine goldene
         * Sonne. Die Opfertruhen stehen hinter dem Altar, eine weitere hinter rissigem Mauerwerk der Ostwand.
         */
        void pit() {
            for (int x = -17; x <= 17; x++) {
                for (int z = PIT_Z - 17; z <= PIT_Z + 17; z++) {
                    double r = radius(x, z);
                    if (r > PIT_R + 2.0) {
                        continue;
                    }
                    boolean inner = r <= PIT_R;
                    for (int y = PIT_FLOOR - 3; y <= PIT_CEILING; y++) {
                        BlockState state;
                        if (!inner) {
                            state = r <= PIT_R + 1.0 ? this.wallStone(x, y, z) : Blocks.MOSSY_COBBLESTONE.defaultBlockState();
                        } else if (y < PIT_FLOOR) {
                            state = Blocks.MOSSY_COBBLESTONE.defaultBlockState();
                        } else if (y == PIT_FLOOR) {
                            state = this.floorStone(x, z, r);
                        } else if (y == PIT_CEILING) {
                            state = this.ceilingStone(x, z, r);
                        } else {
                            state = Blocks.AIR.defaultBlockState();
                        }
                        this.b.set(x, y, z, state);
                    }
                }
            }
            this.gallery();
            this.moat();
            this.island();
            for (int k = 0; k < 8; k++) {
                double a = Math.toRadians(22.5 + 45.0 * k);
                this.pillar((int) Math.round(Math.sin(a) * 11.2), PIT_Z + (int) Math.round(Math.cos(a) * 11.2), a, k);
            }
            this.wallEyes();
            this.hiddenChest();
            this.overgrowth();
        }

        /** Die innere Mauer: bemooster Stein, ein Fries aus grünen und goldenen Federn, oben ein Band aus gemeißeltem Stein. */
        BlockState wallStone(int x, int y, int z) {
            if (y == PIT_FLOOR + 4 || y == PIT_FLOOR + 5) {
                // der Fries: Federn im Zickzack, nach Winkel und Höhe
                int k = (int) Math.floor((angle(x, z) + Math.PI) / (Math.PI * 2.0) * 72.0);
                int band = Math.floorMod(k + (y == PIT_FLOOR + 5 ? 1 : 0), 3);
                return switch (band) {
                    case 0 -> Blocks.DYED_TERRACOTTA.pick(DyeColor.LIME).defaultBlockState();
                    case 1 -> Blocks.DYED_TERRACOTTA.pick(DyeColor.GREEN).defaultBlockState();
                    default -> Blocks.DYED_TERRACOTTA.pick(DyeColor.YELLOW).defaultBlockState();
                };
            }
            if (y == PIT_FLOOR + 3 || y == PIT_FLOOR + 6 || y == PIT_CEILING - 1) {
                return Blocks.CHISELED_STONE_BRICKS.defaultBlockState();
            }
            return this.scale(x, y, z + 160);
        }

        /** Der Boden: bemooste Ziegel, ein Ring aus gemeißeltem Stein um den Graben. */
        BlockState floorStone(int x, int z, double r) {
            if (r > MOAT_R + 0.6 && r <= MOAT_R + 1.6) {
                return Blocks.CHISELED_STONE_BRICKS.defaultBlockState();
            }
            float n = this.b.noise(x, PIT_FLOOR, z + 200);
            if (n < 0.45f) {
                return Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
            }
            if (n < 0.8f) {
                return Blocks.STONE_BRICKS.defaultBlockState();
            }
            return n < 0.92f ? Blocks.CRACKED_STONE_BRICKS.defaultBlockState() : Blocks.MOSS_BLOCK.defaultBlockState();
        }

        /** Die Decke: Strahlen aus gemeißeltem Stein um eine goldene Sonne über dem Altar. */
        BlockState ceilingStone(int x, int z, double r) {
            if (r <= 1.6) {
                return Blocks.OCHRE_FROGLIGHT.defaultBlockState();
            }
            if (r <= 2.7) {
                return Blocks.DYED_TERRACOTTA.pick(DyeColor.YELLOW).defaultBlockState();
            }
            double a = angle(x, z);
            double ray = Math.abs(((a / (Math.PI / 8.0)) % 1.0 + 1.0) % 1.0 - 0.5);
            if (ray > 0.38 && r < PIT_R - 1.0) {
                return Blocks.CHISELED_STONE_BRICKS.defaultBlockState();
            }
            return this.scale(x, PIT_CEILING, z + 260);
        }

        /**
         * Die Empore am Ende der Treppe aus dem Schlund (man steht eine Ebene unter dem Schlund) und die Treppe hinab in die
         * Grube; zu beiden Seiten der Treppe ragt die Empore als Balkon über die Grube (mit Brüstung).
         */
        void gallery() {
            for (int x = -6; x <= 6; x++) {
                for (int z = 16; z <= PIT_Z - 13; z++) {
                    if (radius(x, z) <= PIT_R) {
                        continue;
                    }
                    boolean side = Math.abs(x) == 6;
                    for (int y = PIT_FLOOR - 3; y <= PIT_CEILING; y++) {
                        BlockState state;
                        if (z == 16) {
                            // die Nordwand (die Öffnung zum Schlund in der Mitte)
                            if (Math.abs(x) <= 2 && y > GALLERY_FLOOR - 2) {
                                continue;
                            }
                            state = this.scale(x, y, z + 300);
                        } else if (side || y == PIT_CEILING) {
                            state = this.scale(x, y, z + 300);
                        } else if (y < GALLERY_FLOOR) {
                            state = Blocks.MOSSY_COBBLESTONE.defaultBlockState();
                        } else if (y == GALLERY_FLOOR) {
                            state = Math.floorMod(x + z, 2) == 0 ? Blocks.MOSSY_STONE_BRICKS.defaultBlockState()
                                    : Blocks.CHISELED_STONE_BRICKS.defaultBlockState();
                        } else {
                            state = Blocks.AIR.defaultBlockState();
                        }
                        this.b.set(x, y, z, state);
                    }
                }
            }
            // die Stufe aus dem Schlund herab
            for (int x = -1; x <= 1; x++) {
                this.b.set(x, GALLERY_FLOOR + 1, 17, VillageBuilder.stairs(Blocks.MOSSY_STONE_BRICK_STAIRS, Direction.NORTH, Half.BOTTOM));
            }
            // die Treppe hinab (sechs Stufen), darunter voll
            for (int k = 0; k <= 5; k++) {
                int z = STAIRS_Z + k;
                int top = GALLERY_FLOOR - k;
                for (int x = -2; x <= 2; x++) {
                    for (int y = PIT_FLOOR; y < top; y++) {
                        this.b.set(x, y, z, Blocks.MOSSY_STONE_BRICKS.defaultBlockState());
                    }
                    this.b.set(x, top, z, VillageBuilder.stairs(Blocks.MOSSY_STONE_BRICK_STAIRS, Direction.NORTH, Half.BOTTOM));
                    for (int y = top + 1; y < PIT_CEILING; y++) {
                        this.b.set(x, y, z, Blocks.AIR.defaultBlockState());
                    }
                }
            }
            // Brüstungen: an der Kante der Empore zur Grube und neben der Treppe
            for (int x = -5; x <= 5; x++) {
                for (int z = 17; z <= PIT_Z - 13; z++) {
                    if (radius(x, z) <= PIT_R || Math.abs(x) <= 2 && z >= STAIRS_Z) {
                        continue;
                    }
                    boolean edge = false;
                    for (int[] d : new int[][] {{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
                        int nx = x + d[0];
                        int nz = z + d[1];
                        if (radius(nx, nz) <= PIT_R && !(Math.abs(nx) <= 2 && nz >= STAIRS_Z) || Math.abs(nx) <= 2 && nz >= STAIRS_Z && nz <= STAIRS_Z + 5
                                && Math.abs(x) == 3) {
                            edge = true;
                        }
                    }
                    if (edge) {
                        this.b.set(x, GALLERY_FLOOR + 1, z, Blocks.MOSSY_STONE_BRICK_WALL.defaultBlockState());
                    }
                }
            }
            // zwei Schlangenwächter an der Treppe: Säulen mit Köpfen aus grüner Keramik und Laternen
            for (int side : new int[] {-1, 1}) {
                int x = side * 4;
                int z = STAIRS_Z - 2;
                this.b.set(x, GALLERY_FLOOR + 1, z, Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
                this.b.set(x, GALLERY_FLOOR + 2, z, Blocks.MOSSY_STONE_BRICK_WALL.defaultBlockState());
                this.b.set(x, GALLERY_FLOOR + 3, z, Blocks.GLAZED_TERRACOTTA.pick(DyeColor.GREEN).defaultBlockState());
                this.b.set(x, GALLERY_FLOOR + 4, z, Blocks.LANTERN.defaultBlockState());
            }
        }

        /** Der Wassergraben um die Insel (zwei tief), mit vier Brücken. */
        void moat() {
            for (int x = -10; x <= 10; x++) {
                for (int z = PIT_Z - 10; z <= PIT_Z + 10; z++) {
                    double r = radius(x, z);
                    if (r <= ISLAND_R || r > MOAT_R) {
                        continue;
                    }
                    boolean bridge = Math.abs(x) <= 1 || Math.abs(z - PIT_Z) <= 1;
                    if (bridge) {
                        this.b.set(x, PIT_FLOOR, z, Blocks.MOSSY_STONE_BRICKS.defaultBlockState());
                        this.b.set(x, PIT_FLOOR - 1, z, Blocks.MOSSY_COBBLESTONE.defaultBlockState());
                    } else {
                        this.b.set(x, PIT_FLOOR, z, Blocks.WATER.defaultBlockState());
                        this.b.set(x, PIT_FLOOR - 1, z, Blocks.WATER.defaultBlockState());
                        this.b.set(x, PIT_FLOOR - 2, z, Blocks.MOSSY_COBBLESTONE.defaultBlockState());
                    }
                }
            }
        }

        /**
         * Die Insel, eine Stufe über dem Boden: in der Mitte der Schlangenaltar (das Schlangenbild blickt zur Treppe), ein Ring
         * aus gemeißeltem Stein, Knochen früherer Opfer am Rand, hinter dem Altar die Opfertruhen.
         */
        void island() {
            for (int x = -7; x <= 7; x++) {
                for (int z = PIT_Z - 7; z <= PIT_Z + 7; z++) {
                    double r = radius(x, z);
                    if (r > ISLAND_R) {
                        continue;
                    }
                    BlockState top;
                    if (r > ISLAND_R - 0.8) {
                        top = Blocks.CHISELED_STONE_BRICKS.defaultBlockState();
                    } else if (r > 3.6 && r <= 4.4) {
                        top = Blocks.DYED_TERRACOTTA.pick(DyeColor.GREEN).defaultBlockState();
                    } else {
                        top = this.b.noise(x, PIT_FLOOR + 1, z + 400) < 0.5f ? Blocks.MOSSY_STONE_BRICKS.defaultBlockState()
                                : Blocks.MOSS_BLOCK.defaultBlockState();
                    }
                    this.b.set(x, PIT_FLOOR + 1, z, top);
                }
            }
            // an den Brücken eine Stufe hinauf
            for (int[] d : new int[][] {{0, -1}, {0, 1}, {-1, 0}, {1, 0}}) {
                for (int w = -1; w <= 1; w++) {
                    int x = d[0] * 6 + (d[0] == 0 ? w : 0);
                    int z = PIT_Z + d[1] * 6 + (d[1] == 0 ? w : 0);
                    Direction toward = d[0] < 0 ? Direction.EAST : d[0] > 0 ? Direction.WEST : d[1] < 0 ? Direction.SOUTH : Direction.NORTH;
                    this.b.set(x, PIT_FLOOR + 1, z, VillageBuilder.stairs(Blocks.MOSSY_STONE_BRICK_STAIRS, toward, Half.BOTTOM));
                }
            }
            // der Schlangenaltar
            this.b.set(ALTAR.getX(), ALTAR.getY(), ALTAR.getZ(), io.github.sirschrolli.kingdomsatwar.registry.SerpentGear.SERPENT_ALTAR.get()
                    .defaultBlockState().setValue(io.github.sirschrolli.kingdomsatwar.block.serpent.SerpentAltarBlock.FACING, Direction.NORTH));
            // Knochen früherer Opfer
            int[][] bones = {{-4, 3}, {5, -2}, {3, 4}, {-5, -2}};
            for (int i = 0; i < bones.length; i++) {
                int x = bones[i][0];
                int z = PIT_Z + bones[i][1];
                if (i % 2 == 0) {
                    this.b.set(x, PIT_FLOOR + 2, z, Blocks.SKELETON_SKULL.defaultBlockState().setValue(SkullBlock.ROTATION, (i * 5 + 3) % 16));
                } else {
                    this.b.set(x, PIT_FLOOR + 2, z, Blocks.BONE_BLOCK.defaultBlockState().setValue(RotatedPillarBlock.AXIS,
                            i == 1 ? Direction.Axis.X : Direction.Axis.Z));
                }
            }
            // die Opfertruhen hinter dem Altar, Kerzen daneben
            this.b.chest(-2, PIT_FLOOR + 2, PIT_Z + 5, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.NORTH),
                    ModLootTables.SITE_SERPENT_TEMPLE);
            this.b.chest(2, PIT_FLOOR + 2, PIT_Z + 5, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.NORTH),
                    ModLootTables.SITE_SERPENT_TEMPLE);
            for (int x : new int[] {-3, 0, 3}) {
                this.b.set(x, PIT_FLOOR + 2, PIT_Z + 5, Blocks.DYED_CANDLE.pick(DyeColor.LIME).defaultBlockState()
                        .setValue(CandleBlock.CANDLES, x == 0 ? 4 : 2).setValue(CandleBlock.LIT, true));
            }
        }

        /**
         * Eine Säule: ein Schaft aus bemoosten Ziegeln, um den sich eine steinerne Schlange windet (zwei Blöcke breit, einmal
         * rundherum alle acht Blöcke); oben blickt ihr Kopf zur Mitte, vor ihm hängt eine Laterne von der Decke.
         */
        void pillar(int px, int pz, double a, int k) {
            int[][] ring = {{1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}, {0, -1}, {1, -1}};
            for (int y = PIT_FLOOR + 1; y < PIT_CEILING; y++) {
                boolean cap = y == PIT_FLOOR + 1 || y == PIT_CEILING - 1;
                this.b.set(px, y, pz, cap ? Blocks.CHISELED_STONE_BRICKS.defaultBlockState()
                        : y % 3 == 0 ? Blocks.CRACKED_STONE_BRICKS.defaultBlockState() : Blocks.MOSSY_STONE_BRICKS.defaultBlockState());
            }
            for (int y = PIT_FLOOR + 2; y <= PIT_CEILING - 3; y++) {
                for (int w = 0; w < 2; w++) {
                    int[] d = ring[Math.floorMod(y + k * 3 + w, 8)];
                    if (this.b.get(px + d[0], y, pz + d[1]).isAir()) {
                        this.b.set(px + d[0], y, pz + d[1], w == 0 ? Blocks.DYED_TERRACOTTA.pick(DyeColor.GREEN).defaultBlockState()
                                : Blocks.DYED_TERRACOTTA.pick(DyeColor.LIME).defaultBlockState());
                    }
                }
            }
            // der Kopf: zur Mitte der Grube
            int dx = (int) Math.round(-Math.sin(a));
            int dz = (int) Math.round(-Math.cos(a));
            this.b.set(px + dx, PIT_CEILING - 2, pz + dz, Blocks.GLAZED_TERRACOTTA.pick(DyeColor.GREEN).defaultBlockState());
            this.b.set(px + 2 * dx, PIT_CEILING - 1, pz + 2 * dz, Blocks.IRON_CHAIN.defaultBlockState());
            this.b.set(px + 2 * dx, PIT_CEILING - 2, pz + 2 * dz, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
        }

        /** Acht Augen in der Wand (grünes Glas vor einem Licht), zwischen den Säulen. */
        void wallEyes() {
            for (int k = 0; k < 8; k++) {
                double a = Math.toRadians(45.0 * k);
                if (k == 4) {
                    continue; // im Norden liegt die Empore
                }
                int x = (int) Math.round(Math.sin(a) * (PIT_R + 0.6));
                int z = PIT_Z + (int) Math.round(Math.cos(a) * (PIT_R + 0.6));
                int lx = (int) Math.round(Math.sin(a) * (PIT_R + 1.6));
                int lz = PIT_Z + (int) Math.round(Math.cos(a) * (PIT_R + 1.6));
                for (int y = PIT_FLOOR + 8; y <= PIT_FLOOR + 9; y++) {
                    this.b.set(x, y, z, Blocks.STAINED_GLASS.pick(y == PIT_FLOOR + 9 ? DyeColor.LIME : DyeColor.GREEN).defaultBlockState());
                }
                this.b.set(lx, PIT_FLOOR + 8, lz, Blocks.SEA_LANTERN.defaultBlockState());
                this.b.set(lx, PIT_FLOOR + 9, lz, Blocks.MOSSY_COBBLESTONE.defaultBlockState());
            }
        }

        /** Hinter rissigem Mauerwerk der Ostwand: eine Nische mit einer Truhe. */
        void hiddenChest() {
            int z = PIT_Z;
            for (int y = PIT_FLOOR + 1; y <= PIT_FLOOR + 2; y++) {
                this.b.set(PIT_R + 1, y, z, Blocks.CRACKED_STONE_BRICKS.defaultBlockState());
                this.b.set(PIT_R + 2, y, z, Blocks.AIR.defaultBlockState());
            }
            for (int[] c : new int[][] {{PIT_R + 3, z}, {PIT_R + 2, z - 1}, {PIT_R + 2, z + 1}}) {
                for (int y = PIT_FLOOR; y <= PIT_FLOOR + 3; y++) {
                    this.b.set(c[0], y, c[1], this.scale(c[0], y, c[1] + 80));
                }
            }
            this.b.set(PIT_R + 2, PIT_FLOOR, z, Blocks.MOSSY_STONE_BRICKS.defaultBlockState());
            this.b.set(PIT_R + 2, PIT_FLOOR + 3, z, Blocks.MOSSY_STONE_BRICKS.defaultBlockState());
            this.b.chest(PIT_R + 2, PIT_FLOOR + 1, z, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.WEST),
                    ModLootTables.SITE_SERPENT_TEMPLE);
        }

        /** Ranken hängen von der Mauer, Wurzeln von der Decke, Leuchtflechten an den Wänden. */
        void overgrowth() {
            for (int x = -PIT_R; x <= PIT_R; x++) {
                for (int z = PIT_Z - PIT_R; z <= PIT_Z + PIT_R; z++) {
                    double r = radius(x, z);
                    if (r > PIT_R || r <= MOAT_R + 1.0) {
                        continue;
                    }
                    // an der Mauer: in welcher Richtung liegt sie?
                    Direction wall = null;
                    for (Direction d : Direction.Plane.HORIZONTAL) {
                        if (radius(x + d.getStepX(), z + d.getStepZ()) > PIT_R) {
                            wall = d;
                        }
                    }
                    if (wall != null && this.b.chance(x, 7, z, 0.28f) && this.b.get(x, PIT_CEILING - 1, z).isAir()) {
                        BlockState vine = Blocks.VINE.defaultBlockState().setValue(VineBlock.getPropertyForFace(wall), true);
                        int len = 2 + (int) (this.b.noise(x, 9, z) * 5);
                        for (int d = 1; d <= len && this.b.get(x, PIT_CEILING - d, z).isAir(); d++) {
                            this.b.set(x, PIT_CEILING - d, z, vine);
                        }
                    } else if (wall != null && this.b.chance(x, 11, z, 0.12f) && this.b.get(x, PIT_FLOOR + 2, z).isAir()) {
                        this.b.set(x, PIT_FLOOR + 2, z, Blocks.GLOW_LICHEN.defaultBlockState().setValue(MultifaceBlock.getFaceProperty(wall), true));
                    } else if (wall == null && this.b.chance(x, 13, z, 0.05f) && this.b.get(x, PIT_CEILING - 1, z).isAir()) {
                        this.b.set(x, PIT_CEILING - 1, z, Blocks.HANGING_ROOTS.defaultBlockState());
                    }
                }
            }
        }

        /** Ranken an den Seiten des Kopfes. */
        void vines() {
            for (int z = 2; z <= 17; z++) {
                for (int side : new int[] {-1, 1}) {
                    for (int y = 2; y <= 8; y++) {
                        int x = side * ((int) Math.ceil(this.width(z)) + 1);
                        while (Math.abs(x) > 0 && !this.inside(x - side, y, z)) {
                            x -= side;
                        }
                        if (!this.inside(x - side, y, z) || this.inside(x, y, z) || !this.b.chance(x, y, z, 0.3f)) {
                            continue;
                        }
                        Direction toWall = side > 0 ? Direction.WEST : Direction.EAST;
                        BlockState vine = Blocks.VINE.defaultBlockState().setValue(VineBlock.getPropertyForFace(toWall), true);
                        int len = 1 + (int) (this.b.noise(x, y, z + 7) * 3);
                        for (int d = 0; d < len && y - d >= 1; d++) {
                            if (this.b.get(x, y - d, z).isAir()) {
                                this.b.set(x, y - d, z, vine);
                            }
                        }
                    }
                }
            }
        }
    }
}
