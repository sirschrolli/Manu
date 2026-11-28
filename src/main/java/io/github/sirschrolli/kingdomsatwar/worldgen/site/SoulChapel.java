package io.github.sirschrolli.kingdomsatwar.worldgen.site;

import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.VillageBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BellAttachType;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * Die Seelenkapelle (seit 2.0): Im Seelensandtal steht eine Kapelle aus poliertem Schwarzstein, das Schiff gestützt von
 * Basaltpfeilern, die schmalen, hohen Fenster aus türkisem Glas, das Dach steil. Vorn der Glockenturm mit offener Glockenstube
 * und einem spitzen Helm, auf dem eine Seelenlaterne brennt. Drinnen Bänke, der Altar mit schwarzen Kerzen und einem
 * Schädel, Beinhausnischen voller Knochen; unter dem Schiff die Gruft mit zwei Sarkophagen und der Truhe der Toten. Ringsum
 * ein Friedhof auf Seelenerde mit Grabsteinen und Seelenfackeln – und Skelette, die ihn bewachen.
 *
 * <p>Eigene Koordinaten: y = 0 ist der Boden des Schiffs (Fußboden y = -1); vorn (+z) der Turm mit dem Eingang.
 */
final class SoulChapel {
    /** Das Schiff: x -3..3, z -9..4; der Turm davor: x -2..2, z 5..9; die Gruft darunter. */
    static final int NX = 3, NZ0 = -9, NZ1 = 4, TZ0 = 5, TZ1 = 9;
    static final int CRYPT = -5;
    static final BlockPos CHEST = new BlockPos(0, CRYPT, -8);
    static final double PAD = 12.5;

    private SoulChapel() {}

    static void register() {
        Wonders.add(new Wonders.Wonder(SiteKind.SOUL_CHAPEL, "seelenkapelle", "Soul Chapel", "Seelenkapelle", 3, Wonders.Ground.LAND, false,
                SoulChapel::plan, SoulChapel::build, origin -> piece(origin, Rotation.NONE, 0x5C4AL),
                new double[][] {Wonders.view(-14, 9, 20, 0, 6, -1), Wonders.view(15, 12, -16, 0, 5, 0), Wonders.view(0, 1.6, 3, 0, 1.5, -8),
                        Wonders.view(2, CRYPT + 1.6, -2, -1, CRYPT + 0.5, -8), Wonders.view(9, 2, 12, 2, 1, 3)}));
    }

    static Optional<Structure.GenerationStub> plan(Structure.GenerationContext context, SiteTerrain terrain, long seed) {
        BlockPos origin = Kit.cavern(context, terrain, seed, 11, 4, 18);
        return origin == null ? Optional.empty() : Kit.stub(piece(origin, Kit.rotation(seed), seed));
    }

    static SitePiece piece(BlockPos origin, Rotation rotation, long seed) {
        int r = (int) PAD + 5;
        return new SitePiece(SiteKind.SOUL_CHAPEL, 0, origin, rotation, seed, new int[0], SitePiece.box(origin, rotation, -r, CRYPT - 2, -r, r, 22, r));
    }

    static void build(VillageBuilder b, SitePiece piece) {
        new Chapel(b).build();
    }

    private static final class Chapel {
        private final VillageBuilder b;

        Chapel(VillageBuilder b) {
            this.b = b;
        }

        void build() {
            Kit.pad(this.b, PAD, Kit.s(Blocks.SOUL_SOIL), Kit.s(Blocks.SOUL_SAND), 18);
            this.crypt();
            this.nave();
            this.tower();
            this.inside();
            this.graveyard();
        }

        private BlockState brick(int x, int y, int z) {
            return this.b.chance(x, y, z, 0.15f) ? Kit.s(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS) : Kit.s(Blocks.POLISHED_BLACKSTONE_BRICKS);
        }

        private BlockState basalt() {
            return Blocks.POLISHED_BASALT.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        }

        /** Die Gruft unter dem Schiff: gewölbt, zwei Sarkophage, Nischen mit Knochen und Schädeln, die Truhe am Ende. */
        private void crypt() {
            for (int x = -NX; x <= NX; x++) {
                for (int z = NZ0; z <= NZ1; z++) {
                    for (int y = CRYPT - 1; y <= -1; y++) {
                        boolean shell = Math.abs(x) == NX || z == NZ0 || z == NZ1 || y == CRYPT - 1 || y == -1;
                        this.b.set(x, y, z, shell ? this.brick(x, y, z) : Kit.s(Blocks.AIR));
                    }
                }
            }
            // ein Gewölbe: an den Seiten oben Stufen
            for (int z = NZ0 + 1; z <= NZ1 - 1; z++) {
                this.b.set(-NX + 1, -2, z, VillageBuilder.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Direction.EAST, Half.TOP));
                this.b.set(NX - 1, -2, z, VillageBuilder.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Direction.WEST, Half.TOP));
                this.b.set(0, CRYPT - 1, z, Kit.s(Blocks.POLISHED_BLACKSTONE));
            }
            // zwei Sarkophage (je zwei Blöcke lang, mit Deckel)
            for (int z : new int[] {-6, -2}) {
                for (int x : new int[] {-2, 2}) {
                    this.b.set(x, CRYPT, z, Kit.s(Blocks.POLISHED_BLACKSTONE));
                    this.b.set(x, CRYPT, z + 1, Kit.s(Blocks.POLISHED_BLACKSTONE));
                    this.b.set(x, CRYPT + 1, z, VillageBuilder.slab(Blocks.POLISHED_BLACKSTONE_SLAB, SlabType.BOTTOM));
                    this.b.set(x, CRYPT + 1, z + 1, VillageBuilder.slab(Blocks.POLISHED_BLACKSTONE_SLAB, SlabType.BOTTOM));
                }
            }
            this.b.set(-2, CRYPT + 1, -6, Kit.s(Blocks.SKELETON_SKULL).setValue(SkullBlock.ROTATION, 4));
            // Nischen in den Wänden: Knochen und Schädel
            for (int z = NZ0 + 2; z <= NZ1 - 2; z += 3) {
                for (int side : new int[] {-1, 1}) {
                    this.b.set(side * NX, CRYPT + 1, z, Kit.s(Blocks.BONE_BLOCK));
                    this.b.set(side * NX, CRYPT + 2, z, Kit.s(Blocks.SKELETON_SKULL)
                            .setValue(SkullBlock.ROTATION, side < 0 ? 12 : 4));
                }
            }
            Kit.chest(this.b, CHEST.getX(), CHEST.getY(), CHEST.getZ(), Direction.SOUTH, Kit.loot("soul_chapel"));
            this.b.set(-1, CRYPT, -8, Kit.s(Blocks.SOUL_CAMPFIRE));
            this.b.set(1, CRYPT, -8, Kit.s(Blocks.SOUL_CAMPFIRE));
            this.b.hangingLantern(0, -2, -4, Blocks.SOUL_LANTERN);
            this.b.hangingLantern(0, -2, 1, Blocks.SOUL_LANTERN);
            Kit.cobwebs(this.b, -NX + 1, -2, NZ0 + 1, NX - 1, -2, NZ1 - 1, 0.08f);
            // der Abgang: eine Leiter hinter einer Falltür im Boden des Schiffs
            this.b.ladder(NX - 1, CRYPT, -1, NZ1 - 1, Direction.NORTH);
        }

        /** Das Schiff: Mauern aus polierten Schwarzsteinziegeln, Basaltpfeiler außen, hohe Fenster, ein steiles Dach. */
        private void nave() {
            for (int x = -NX; x <= NX; x++) {
                for (int z = NZ0; z <= NZ1; z++) {
                    boolean wall = Math.abs(x) == NX || z == NZ0 || z == NZ1;
                    if (!wall && !(x == NX - 1 && z == NZ1 - 1)) {
                        this.b.set(x, -1, z, x == 0 ? Kit.s(Blocks.POLISHED_BLACKSTONE_BRICKS) : Kit.s(Blocks.POLISHED_BLACKSTONE));
                    }
                    for (int y = 0; y <= 6; y++) {
                        this.b.set(x, y, z, wall ? (y == 6 ? Kit.s(Blocks.CHISELED_POLISHED_BLACKSTONE) : this.brick(x, y, z)) : Kit.s(Blocks.AIR));
                    }
                }
            }
            this.b.set(NX - 1, -1, NZ1 - 1, Blocks.DARK_OAK_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.HALF, Half.TOP));
            // Pfeiler außen an den Langseiten, oben mit einer Stufe
            for (int z = NZ0; z <= NZ1; z += 3) {
                for (int side : new int[] {-1, 1}) {
                    int x = side * (NX + 1);
                    this.b.column(x, z, this.basalt(), 0);
                    for (int y = 0; y <= 5; y++) {
                        this.b.set(x, y, z, this.basalt());
                    }
                    this.b.set(x, 6, z, VillageBuilder.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, side < 0 ? Direction.EAST : Direction.WEST, Half.BOTTOM));
                }
            }
            // hohe, schmale Fenster aus türkisem Glas
            BlockState glass = Kit.s(Blocks.STAINED_GLASS_PANE.pick(net.minecraft.world.item.DyeColor.CYAN));
            for (int z = NZ0 + 2; z <= NZ1 - 2; z += 3) {
                Kit.window(this.b, -NX, 2, z, 3, glass);
                Kit.window(this.b, NX, 2, z, 3, glass);
            }
            // das Rundfenster hinten über dem Altar
            for (int[] w : new int[][] {{-1, 3}, {0, 3}, {1, 3}, {-1, 4}, {1, 4}, {-1, 5}, {0, 5}, {1, 5}, {0, 4}}) {
                this.b.set(w[0], w[1], NZ0, Kit.s(w[0] == 0 && w[1] == 4 ? Blocks.STAINED_GLASS_PANE.pick(net.minecraft.world.item.DyeColor.LIGHT_BLUE)
                        : Blocks.STAINED_GLASS_PANE.pick(net.minecraft.world.item.DyeColor.CYAN)));
            }
            // das steile Dach (First entlang z) mit Giebeln aus Schwarzstein
            this.b.gableAlongZ(-NX, NX, NZ0, NZ1, 6, Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Kit.s(Blocks.POLISHED_BLACKSTONE_BRICKS),
                    Kit.s(Blocks.POLISHED_BLACKSTONE_BRICK_SLAB), Kit.s(Blocks.POLISHED_BLACKSTONE_BRICKS));
            for (int x = -NX + 1; x <= NX - 1; x++) {
                for (int z = NZ0 + 1; z <= NZ1 - 1; z++) {
                    this.b.set(x, 7, z, Kit.s(Blocks.AIR));
                }
            }
        }

        /** Der Glockenturm vor dem Schiff: der Eingang durch seinen Fuß, eine Leiter zur offenen Glockenstube, ein spitzer Helm. */
        private void tower() {
            int top = 13;
            for (int x = -2; x <= 2; x++) {
                for (int z = TZ0; z <= TZ1; z++) {
                    boolean wall = Math.abs(x) == 2 || z == TZ0 || z == TZ1;
                    boolean corner = Math.abs(x) == 2 && (z == TZ0 || z == TZ1);
                    this.b.column(x, z, this.brick(x, -2, z), 0);
                    this.b.set(x, -1, z, Kit.s(Blocks.POLISHED_BLACKSTONE_BRICKS));
                    for (int y = 0; y < top; y++) {
                        this.b.set(x, y, z, corner ? this.basalt() : wall ? (y == 9 ? Kit.s(Blocks.CHISELED_POLISHED_BLACKSTONE) : this.brick(x, y, z))
                                : Kit.s(Blocks.AIR));
                    }
                    // die Glockenstube: Boden, offene Bögen zwischen den Eckpfeilern, darüber der Helm
                    this.b.set(x, top - 1, z, Kit.s(Blocks.POLISHED_BLACKSTONE_BRICKS));
                    for (int y = top; y <= top + 3; y++) {
                        this.b.set(x, y, z, corner ? this.basalt() : Kit.s(Blocks.AIR));
                    }
                    if (wall && !corner) {
                        this.b.set(x, top + 3, z, VillageBuilder.slab(Blocks.POLISHED_BLACKSTONE_BRICK_SLAB, SlabType.TOP));
                        this.b.set(x, top, z, Kit.s(Blocks.POLISHED_BLACKSTONE_BRICK_WALL));
                    }
                }
            }
            this.b.hipRoof(-3, TZ0 - 1, 3, TZ1 + 1, top + 3, Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Kit.s(Blocks.POLISHED_BLACKSTONE_BRICK_SLAB));
            int peak = top + 6;
            for (int y = top + 4; y <= peak; y++) {
                this.b.set(0, y, 7, Kit.s(Blocks.POLISHED_BLACKSTONE_BRICKS));
            }
            this.b.set(0, peak + 1, 7, Kit.s(Blocks.POLISHED_BLACKSTONE_WALL));
            this.b.set(0, peak + 2, 7, Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, false));
            // die Glocke unter dem Helm
            this.b.set(0, top + 3, 7, Kit.s(Blocks.POLISHED_BLACKSTONE_BRICKS));
            this.b.set(0, top + 2, 7, Blocks.BELL.defaultBlockState().setValue(BellBlock.ATTACHMENT, BellAttachType.CEILING)
                    .setValue(BellBlock.FACING, Direction.SOUTH));
            // der Eingang: vorn ein Bogen, hinten offen ins Schiff
            for (int y = 0; y <= 2; y++) {
                this.b.set(0, y, TZ1, Kit.s(Blocks.AIR));
                this.b.set(0, y, NZ1, Kit.s(Blocks.AIR));
                this.b.set(0, y, TZ0, Kit.s(Blocks.AIR));
            }
            this.b.set(-1, 2, TZ1, VillageBuilder.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Direction.EAST, Half.TOP));
            this.b.set(1, 2, TZ1, VillageBuilder.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Direction.WEST, Half.TOP));
            this.b.set(0, -1, TZ1 + 1, VillageBuilder.stairs(Blocks.POLISHED_BLACKSTONE_STAIRS, Direction.NORTH, Half.BOTTOM));
            // die Leiter zur Glockenstube, an der Westwand
            this.b.ladder(-1, 0, top - 1, TZ1 - 1, Direction.NORTH);
            this.b.set(-1, top - 1, TZ1 - 1, Blocks.LADDER.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.LadderBlock.FACING, Direction.NORTH));
            this.b.set(1, 0, TZ0 + 1, Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, false));
            // seitliche Schlitzfenster
            for (int y : new int[] {4, 8}) {
                this.b.set(-2, y, 7, Kit.s(Blocks.IRON_BARS));
                this.b.set(2, y, 7, Kit.s(Blocks.IRON_BARS));
            }
        }

        /** Bänke, der Altar mit Kerzen und Schädel, Beinhausnischen, Seelenlaternen. */
        private void inside() {
            for (int z = -4; z <= 2; z += 2) {
                for (int x : new int[] {-2, -1, 1, 2}) {
                    this.b.set(x, 0, z, VillageBuilder.stairs(Blocks.CRIMSON_STAIRS, Direction.SOUTH, Half.BOTTOM));
                }
            }
            // ein roter Läufer im Mittelgang
            for (int z = NZ0 + 4; z <= NZ1 - 1; z++) {
                this.b.set(0, 0, z, Kit.s(Blocks.CARPET.pick(net.minecraft.world.item.DyeColor.RED)));
            }
            // der Altarraum: eine Stufe höher
            for (int x = -NX + 1; x <= NX - 1; x++) {
                for (int z = NZ0 + 1; z <= NZ0 + 3; z++) {
                    this.b.set(x, 0, z, VillageBuilder.slab(Blocks.POLISHED_BLACKSTONE_SLAB, SlabType.BOTTOM));
                }
            }
            for (int x = -1; x <= 1; x++) {
                this.b.set(x, 0, NZ0 + 2, Kit.s(Blocks.CHISELED_POLISHED_BLACKSTONE));
                Kit.candles(this.b, x, 1, NZ0 + 2, Blocks.DYED_CANDLE.pick(net.minecraft.world.item.DyeColor.BLACK), x == 0 ? 1 : 3, true);
            }
            this.b.set(0, 1, NZ0 + 2, Kit.s(Blocks.WITHER_SKELETON_SKULL).setValue(SkullBlock.ROTATION, 8));
            this.b.set(-2, 0, NZ0 + 1, Kit.s(Blocks.SOUL_CAMPFIRE));
            this.b.set(2, 0, NZ0 + 1, Kit.s(Blocks.SOUL_CAMPFIRE));
            Kit.lootBarrel(this.b, 2, 0, NZ0 + 3, Kit.loot("soul_chapel_altar"));
            // Beinhausnischen in den Langseiten zwischen den Fenstern
            for (int z = NZ0 + 4; z <= NZ1 - 1; z += 3) {
                for (int side : new int[] {-1, 1}) {
                    this.b.set(side * NX, 1, z, Kit.s(Blocks.BONE_BLOCK));
                    this.b.set(side * NX, 2, z, Kit.s(Blocks.SKELETON_SKULL).setValue(SkullBlock.ROTATION, side < 0 ? 12 : 4));
                }
            }
            for (int z = -6; z <= 2; z += 4) {
                this.b.hang(0, 6, z, Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
            }
            Kit.cobwebs(this.b, -NX + 1, 6, NZ0 + 1, NX - 1, 6, NZ1 - 1, 0.04f);
        }

        /** Der Friedhof: Grabsteine aus Schwarzstein, verdorrte Büsche, Knochen im Seelensand, Seelenfackeln; Skelette wachen. */
        private void graveyard() {
            int[][] graves = {{-8, -6}, {-8, -2}, {-8, 2}, {-6, 6}, {6, 6}, {8, -6}, {8, -2}, {8, 2}, {-10, 6}, {10, 6}, {-6, -9}, {6, -9}};
            for (int i = 0; i < graves.length; i++) {
                int x = graves[i][0];
                int z = graves[i][1];
                // der Grabstein, davor die Grabstelle aus Seelensand
                this.b.set(x, 0, z, i % 3 == 0 ? Kit.s(Blocks.POLISHED_BLACKSTONE_BRICK_WALL) : Kit.s(Blocks.POLISHED_BLACKSTONE_WALL));
                if (i % 2 == 0) {
                    this.b.set(x, 1, z, VillageBuilder.slab(Blocks.POLISHED_BLACKSTONE_SLAB, SlabType.BOTTOM));
                }
                int dz = z > 0 ? 1 : -1;
                if (Math.abs(x) >= 8) {
                    this.b.set(x + (x > 0 ? -1 : 1), -1, z, Kit.s(Blocks.SOUL_SAND));
                } else {
                    this.b.set(x, -1, z + dz, Kit.s(Blocks.SOUL_SAND));
                }
                if (i % 4 == 1) {
                    Kit.plant(this.b, x + 1, 0, z, Kit.s(Blocks.DEAD_BUSH));
                }
            }
            for (int[] t : new int[][] {{-10, -4}, {10, -4}, {-10, 1}, {10, 1}, {-4, 11}, {4, 11}}) {
                this.b.set(t[0], 0, t[1], Kit.s(Blocks.POLISHED_BLACKSTONE_WALL));
                this.b.set(t[0], 1, t[1], Kit.s(Blocks.SOUL_TORCH));
            }
            for (int[] bone : new int[][] {{-11, -1}, {11, 4}, {-5, 9}}) {
                this.b.set(bone[0], -1, bone[1], Kit.s(Blocks.BONE_BLOCK));
            }
            Kit.mob(this.b, ModEntities.SKELETON.get(), -6, 0, 9, Direction.SOUTH);
            Kit.mob(this.b, ModEntities.SKELETON.get(), 7, 0, -4, Direction.EAST);
        }
    }
}
