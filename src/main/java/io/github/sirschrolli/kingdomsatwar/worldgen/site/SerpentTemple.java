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
import net.minecraft.world.level.block.Rotation;
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
        BlockPos origin = new BlockPos(x, y, z);
        BoundingBox box = SitePiece.box(origin, rotation, -17, -10, -13, 17, 32, 31);
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
            this.hall();
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

        /**
         * Die Halle der Schlangenpriester (unter dem Nacken): Säulen als aufgerichtete Schlangen, ein Graben um den Altar,
         * zwei Opfertruhen; hinter rissigem Mauerwerk der Ostwand eine dritte, versteckte Truhe.
         */
        void hall() {
            int f = -7;
            for (int x = -6; x <= 6; x++) {
                for (int z = 16; z <= 28; z++) {
                    for (int y = f - 1; y <= f + 7; y++) {
                        boolean shell = Math.abs(x) == 6 || z == 28 || y == f - 1 || y == f + 7 || z == 16 && Math.abs(x) > 1;
                        this.b.set(x, y, z, shell ? this.scale(x, y, z + 80) : Blocks.AIR.defaultBlockState());
                    }
                    if (Math.abs(x) < 6 && z > 16 && z < 28) {
                        this.b.set(x, f - 1, z, Math.floorMod(x + z, 2) == 0 ? Blocks.MOSSY_STONE_BRICKS.defaultBlockState()
                                : Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
                    }
                }
            }
            // Schlangensäulen: Schaft aus Mauern, Kopf aus grüner Keramik mit einer Laterne darunter
            for (int[] c : new int[][] {{-4, 19}, {4, 19}, {-4, 24}, {4, 24}}) {
                for (int y = f; y <= f + 4; y++) {
                    this.b.set(c[0], y, c[1], y == f ? Blocks.CHISELED_STONE_BRICKS.defaultBlockState()
                            : Blocks.MOSSY_STONE_BRICK_WALL.defaultBlockState());
                }
                this.b.set(c[0], f + 5, c[1], Blocks.GLAZED_TERRACOTTA.pick(DyeColor.GREEN).defaultBlockState());
                this.b.set(c[0], f + 6, c[1], Blocks.MOSSY_STONE_BRICKS.defaultBlockState());
                this.b.set(c[0] + (c[0] < 0 ? 1 : -1), f + 5, c[1], Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, false));
            }
            // Graben um den Altar (z 23..27), eine Brücke
            for (int x = -3; x <= 3; x++) {
                for (int z = 22; z <= 27; z++) {
                    boolean moat = (Math.abs(x) == 3 || z == 22) && !(x == 0 && z == 22);
                    if (moat) {
                        this.b.set(x, f - 1, z, Blocks.WATER.defaultBlockState());
                        this.b.set(x, f - 2, z, Blocks.MOSSY_COBBLESTONE.defaultBlockState());
                    }
                }
            }
            this.b.set(0, f - 1, 22, Blocks.MOSSY_STONE_BRICKS.defaultBlockState());
            // Altar mit Opfergaben
            for (int x = -1; x <= 1; x++) {
                this.b.set(x, f, 26, Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
                this.b.set(x, f + 1, 26, x == 0 ? Blocks.GOLD_BLOCK.defaultBlockState() : Blocks.MOSSY_STONE_BRICK_SLAB.defaultBlockState());
            }
            this.b.set(-1, f + 2, 26, Blocks.DYED_CANDLE.pick(DyeColor.LIME).defaultBlockState().setValue(CandleBlock.CANDLES, 3)
                    .setValue(CandleBlock.LIT, true));
            this.b.set(1, f + 2, 26, Blocks.DYED_CANDLE.pick(DyeColor.LIME).defaultBlockState().setValue(CandleBlock.CANDLES, 3)
                    .setValue(CandleBlock.LIT, true));
            this.b.chest(-2, f, 26, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.NORTH), ModLootTables.SITE_SERPENT_TEMPLE);
            this.b.chest(2, f, 26, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.NORTH), ModLootTables.SITE_SERPENT_TEMPLE);
            // versteckte Nische in der Ostwand
            for (int y = f; y <= f + 1; y++) {
                this.b.set(6, y, 20, Blocks.CRACKED_STONE_BRICKS.defaultBlockState());
                this.b.set(7, y, 20, Blocks.AIR.defaultBlockState());
            }
            for (int[] c : new int[][] {{8, 20}, {7, 19}, {7, 21}}) {
                for (int y = f - 1; y <= f + 2; y++) {
                    this.b.set(c[0], y, c[1], this.scale(c[0], y, c[1] + 80));
                }
            }
            this.b.set(7, f - 1, 20, Blocks.MOSSY_STONE_BRICKS.defaultBlockState());
            this.b.set(7, f + 2, 20, Blocks.MOSSY_STONE_BRICKS.defaultBlockState());
            this.b.chest(7, f, 20, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.WEST), ModLootTables.SITE_SERPENT_TEMPLE);
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
