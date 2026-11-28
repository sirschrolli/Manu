package io.github.sirschrolli.kingdomsatwar.worldgen.site;

import io.github.sirschrolli.kingdomsatwar.loot.ModLootTables;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.VillageBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.CopperBulbBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * Die Zwergenhalle (seit 1.15): eine große Halle, die ein verschollenes Zwergenvolk tief in den Fels gehauen hat. Zwei Reihen
 * mächtiger Pfeiler mit Kupferbändern tragen eine Decke mit steinernen Balken; ein Fries aus Runen und alten Kupferleuchten
 * läuft rund um die Wände. In der Mitte steht die große Esse – ein Lavakern hinter Gittern, Hochöfen, Ambosse, ein Schleifstein
 * und Löschbottiche –, im Süden laden lange Tafeln unter Kupferleuchtern zum Gelage, im Norden stehen Pulte und Bücherregale
 * der Runenmeister. Zwei Stollen mit Schienen führen nach Osten und Westen zu kleinen Erzkammern. Am Nordende wacht der
 * Runenwächter auf seinem Sockel vor der Tür zur Schatzkammer (sie öffnet sich mit einem Knopf).
 *
 * <p>Eigene Koordinaten: Mitte der Halle bei (0, 0, 0); y = 0 ist die Schicht des Bodens (man steht auf y = 1). Die
 * Schatzkammer liegt im Norden (−z), das zugeschüttete Haupttor im Süden.
 */
final class DwarfHall {
    /** Die Halle innen: x von −8 bis 8, z von −16 bis 15, bis y = 11. */
    static final int HW = 8;
    static final int NORTH = -16;
    static final int SOUTH = 15;
    static final int H = 11;
    /** Die Pfeiler (Paare in z) und wo der Runenwächter steht. */
    static final int[] PILLARS_Z = {-12, -5, 2, 9};
    static final BlockPos WARDEN = new BlockPos(0, 2, -14);
    /** Die Tür zur Schatzkammer (unten). */
    static final BlockPos VAULT_DOOR = new BlockPos(0, 1, NORTH - 1);
    /** Wie weit die Stollen reichen (bis zur Rückwand der Erzkammer). */
    static final int GALLERY_END = 22;

    private DwarfHall() {}

    static Optional<Structure.GenerationStub> plan(Structure.GenerationContext context, SiteTerrain terrain, long seed) {
        BlockPos origin = DeepLair.origin(context, terrain, seed, -52, 38, 24, H + 12);
        if (origin == null) {
            return Optional.empty();
        }
        Rotation rotation = DeepLair.rotation(seed);
        BoundingBox box = box(origin, rotation);
        return Optional.of(new Structure.GenerationStub(origin,
                builder -> builder.addPiece(new SitePiece(SiteKind.DWARF_HALL, 0, origin, rotation, seed, new int[0], box))));
    }

    static BoundingBox box(BlockPos origin, Rotation rotation) {
        return SitePiece.box(origin, rotation, -GALLERY_END - 1, -2, NORTH - 9, GALLERY_END + 1, H + 3, SOUTH + 3);
    }

    static void build(VillageBuilder b, SitePiece piece) {
        new Builder(b).build();
    }

    private static final class Builder {
        private final VillageBuilder b;

        Builder(VillageBuilder b) {
            this.b = b;
        }

        void build() {
            this.hall();
            this.pillars();
            this.frieze();
            this.forge();
            this.tables();
            this.library();
            this.pedestal();
            this.vault();
            this.treasury();
            this.gallery(1);
            this.gallery(-1);
            this.gate();
            DeepLair.boss(this.b, ModEntities.RUNE_WARDEN.get(), WARDEN.getX(), WARDEN.getY(), WARDEN.getZ(), 0.0, Direction.SOUTH);
        }

        // =================================================================================
        // Stoffe
        // =================================================================================

        BlockState wall(int x, int y, int z) {
            if (y == 1) {
                return Blocks.POLISHED_DEEPSLATE.defaultBlockState();
            }
            if (y == H) {
                return Blocks.POLISHED_DEEPSLATE.defaultBlockState();
            }
            return this.b.noise(x, y, z) < 0.08f ? Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState() : Blocks.DEEPSLATE_BRICKS.defaultBlockState();
        }

        BlockState floor(int x, int z) {
            if (Math.abs(x) <= 2) {
                return Math.abs(x) == 2 ? Blocks.POLISHED_TUFF.defaultBlockState() : Blocks.TUFF_BRICKS.defaultBlockState();
            }
            boolean checker = ((Math.floorDiv(x, 2) + Math.floorDiv(z, 2)) & 1) == 0;
            return checker ? Blocks.POLISHED_DEEPSLATE.defaultBlockState() : Blocks.DEEPSLATE_TILES.defaultBlockState();
        }

        static BlockState bulb() {
            return Blocks.COPPER_BULB.waxed().oxidized().defaultBlockState().setValue(CopperBulbBlock.LIT, true);
        }

        static BlockState copper() {
            return Blocks.CUT_COPPER.waxed().unaffected().defaultBlockState();
        }

        BlockState rock(int x, int y, int z) {
            float n = this.b.noise(x, y + 300, z);
            return n < 0.5f ? Blocks.DEEPSLATE.defaultBlockState() : n < 0.8f ? Blocks.TUFF.defaultBlockState() : Blocks.COBBLED_DEEPSLATE.defaultBlockState();
        }

        // =================================================================================
        // Die Halle
        // =================================================================================

        void hall() {
            for (int x = -HW - 1; x <= HW + 1; x++) {
                for (int z = NORTH - 1; z <= SOUTH + 1; z++) {
                    boolean side = Math.abs(x) > HW || z < NORTH || z > SOUTH;
                    this.b.set(x, 0, z, side ? Blocks.POLISHED_DEEPSLATE.defaultBlockState() : this.floor(x, z));
                    for (int y = 1; y <= H; y++) {
                        this.b.set(x, y, z, side ? this.wall(x, y, z) : Blocks.AIR.defaultBlockState());
                    }
                    this.b.set(x, H + 1, z, Blocks.DEEPSLATE_TILES);
                }
            }
            // Balken über den Pfeilern, dazwischen Kassetten
            for (int z = NORTH; z <= SOUTH; z++) {
                boolean beam = false;
                for (int pz : PILLARS_Z) {
                    if (z == pz || z == pz + 1) {
                        beam = true;
                    }
                }
                for (int x = -HW; x <= HW; x++) {
                    if (beam) {
                        this.b.set(x, H, z, Blocks.POLISHED_DEEPSLATE);
                    } else if (Math.abs(x) == 2 || Math.abs(x) == HW) {
                        this.b.set(x, H, z, Blocks.DEEPSLATE_TILE_SLAB.defaultBlockState().setValue(BlockStateProperties.SLAB_TYPE, SlabType.TOP));
                    }
                }
            }
        }

        void pillars() {
            for (int sx : new int[] {-1, 1}) {
                for (int pz : PILLARS_Z) {
                    for (int x = 5; x <= 6; x++) {
                        for (int z = pz; z <= pz + 1; z++) {
                            for (int y = 1; y < H; y++) {
                                BlockState s = y == 1 ? Blocks.DEEPSLATE_TILES.defaultBlockState()
                                        : y == 3 || y == 8 ? copper()
                                        : y >= H - 1 ? Blocks.CHISELED_DEEPSLATE.defaultBlockState()
                                        : Blocks.POLISHED_DEEPSLATE.defaultBlockState();
                                this.b.set(sx * x, y, z, s);
                            }
                        }
                    }
                    // Banner der Zwerge an den Pfeilern, zur Mitte hin
                    Direction face = sx > 0 ? Direction.WEST : Direction.EAST;
                    Block banner = (pz / 7) % 2 == 0 ? Blocks.WALL_BANNER.pick(DyeColor.ORANGE) : Blocks.WALL_BANNER.pick(DyeColor.BROWN);
                    this.b.set(sx * 4, 7, pz, banner.defaultBlockState().setValue(WallBannerBlock.FACING, face));
                }
            }
        }

        /** Der Fries rund um die Halle: gemeißelte Runensteine, alle vier Blöcke eine alte Kupferleuchte. */
        void frieze() {
            for (int x = -HW - 1; x <= HW + 1; x++) {
                for (int z : new int[] {NORTH - 1, SOUTH + 1}) {
                    this.b.set(x, 6, z, Math.floorMod(x, 4) == 0 ? bulb() : Blocks.CHISELED_TUFF_BRICKS.defaultBlockState());
                }
            }
            for (int z = NORTH; z <= SOUTH; z++) {
                for (int x : new int[] {-HW - 1, HW + 1}) {
                    this.b.set(x, 6, z, Math.floorMod(z, 4) == 0 ? bulb() : Blocks.CHISELED_TUFF_BRICKS.defaultBlockState());
                }
            }
        }

        /** Die große Esse in der Mitte. */
        void forge() {
            // der Lavakern hinter Gittern
            this.b.set(0, 1, 0, Blocks.LAVA);
            for (Direction d : Direction.Plane.HORIZONTAL) {
                this.b.set(d.getStepX(), 1, d.getStepZ(), Blocks.IRON_BARS);
            }
            for (int x : new int[] {-1, 1}) {
                for (int z : new int[] {-1, 1}) {
                    this.b.set(x, 1, z, Blocks.POLISHED_BLACKSTONE_BRICKS);
                }
            }
            // die Haube und der Schlot bis zur Decke
            for (int y = 2; y <= H; y++) {
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        BlockState s = y == 2 ? Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
                                : y == 3 ? copper()
                                : (Math.abs(x) == 1 && Math.abs(z) == 1) ? Blocks.POLISHED_DEEPSLATE.defaultBlockState()
                                : Blocks.DEEPSLATE_BRICKS.defaultBlockState();
                        this.b.set(x, y, z, s);
                    }
                }
            }
            // Hochöfen, Ambosse, Schleifstein, Schmiedetisch, Löschbottiche
            for (int sx : new int[] {-1, 1}) {
                Direction out = sx > 0 ? Direction.EAST : Direction.WEST;
                this.b.set(sx * 2, 1, 0, Blocks.BLAST_FURNACE.defaultBlockState().setValue(AbstractFurnaceBlock.FACING, out)
                        .setValue(AbstractFurnaceBlock.LIT, true));
            }
            this.b.set(0, 1, 2, Blocks.ANVIL.defaultBlockState().setValue(AnvilBlock.FACING, Direction.EAST));
            this.b.set(0, 1, -2, Blocks.CHIPPED_ANVIL.defaultBlockState().setValue(AnvilBlock.FACING, Direction.WEST));
            this.b.set(2, 1, 2, Blocks.SMITHING_TABLE);
            this.b.set(-2, 1, -2, Blocks.GRINDSTONE.defaultBlockState().setValue(BlockStateProperties.ATTACH_FACE, AttachFace.FLOOR)
                    .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
            this.b.set(2, 1, -2, Blocks.LAVA_CAULDRON);
            this.b.set(-2, 1, 2, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3));
        }

        /** Zwei lange Tafeln im Süden mit Bänken, Kerzen und Kupferleuchtern darüber. */
        void tables() {
            for (int sx : new int[] {-1, 1}) {
                int x = sx * 3;
                for (int z = 6; z <= 12; z++) {
                    this.b.set(x, 1, z, VillageBuilder.slab(Blocks.SPRUCE_SLAB, SlabType.TOP));
                    if (z == 6 || z == 12) {
                        continue;
                    }
                    this.b.set(x - sx, 1, z, VillageBuilder.stairs(Blocks.SPRUCE_STAIRS, sx > 0 ? Direction.WEST : Direction.EAST, Half.BOTTOM));
                    this.b.set(x + sx, 1, z, VillageBuilder.stairs(Blocks.SPRUCE_STAIRS, sx > 0 ? Direction.EAST : Direction.WEST, Half.BOTTOM));
                }
                for (int z = 7; z <= 11; z += 2) {
                    this.b.set(x, 2, z, Blocks.DYED_CANDLE.pick(DyeColor.ORANGE).defaultBlockState().setValue(CandleBlock.CANDLES, 1 + (z % 3))
                            .setValue(CandleBlock.LIT, true));
                }
                this.b.set(x, 2, 8, Blocks.LANTERN);
                this.b.hangingLantern(x, 8, 7, Blocks.COPPER_LANTERN.waxed().exposed());
                this.b.hangingLantern(x, 8, 11, Blocks.COPPER_LANTERN.waxed().exposed());
            }
        }

        /** Im Norden: Pulte und Bücherregale der Runenmeister. */
        void library() {
            for (int sx : new int[] {-1, 1}) {
                for (int z = -10; z <= -7; z++) {
                    for (int y = 1; y <= 3; y++) {
                        this.b.set(sx * HW, y, z, y == 2 && z == -9 ? Blocks.CHISELED_BOOKSHELF.defaultBlockState() : Blocks.BOOKSHELF.defaultBlockState());
                    }
                }
                this.b.set(sx * 3, 1, -8, Blocks.LECTERN.defaultBlockState().setValue(LecternBlock.FACING, sx > 0 ? Direction.EAST : Direction.WEST));
                this.b.set(sx * 3, 1, -10, Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, Direction.UP));
                this.b.chest(sx * 7, 1, -2, Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, Direction.UP), ModLootTables.SITE_DWARF_HALL);
            }
        }

        /** Der Sockel des Runenwächters vor der Tür zur Schatzkammer, umgeben von einem Runenkreis im Boden. */
        void pedestal() {
            for (int x = -1; x <= 1; x++) {
                for (int z = WARDEN.getZ() - 1; z <= WARDEN.getZ() + 1; z++) {
                    this.b.set(x, 1, z, x == 0 && z == WARDEN.getZ() ? Blocks.CHISELED_TUFF.defaultBlockState()
                            : Blocks.CHISELED_TUFF_BRICKS.defaultBlockState());
                }
            }
            for (int a = 0; a < 360; a += 15) {
                int x = (int) Math.round(Math.cos(Math.toRadians(a)) * 3.5);
                int z = WARDEN.getZ() + (int) Math.round(Math.sin(Math.toRadians(a)) * 3.5);
                if (z >= NORTH && Math.abs(x) <= HW) {
                    this.b.set(x, 0, z, Blocks.CHISELED_TUFF);
                }
            }
        }

        /** Die Tür zur Schatzkammer in der Nordwand: eine Eisentür in einem Rahmen aus Stein, Gold und Kupfer, mit Knöpfen. */
        void vault() {
            int z = NORTH - 1;
            for (int y = 1; y <= 5; y++) {
                for (int x = -2; x <= 2; x++) {
                    BlockState s = Math.abs(x) == 2 || y == 5 ? Blocks.CHISELED_DEEPSLATE.defaultBlockState()
                            : Math.abs(x) == 1 || y >= 3 ? Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState() : Blocks.AIR.defaultBlockState();
                    this.b.set(x, y, z, s);
                }
            }
            this.b.set(0, 3, z, Blocks.GOLD_BLOCK);
            this.b.set(0, 5, z, bulb());
            this.b.set(-2, 6, z, copper());
            this.b.set(2, 6, z, copper());
            this.b.door(0, 1, z, Blocks.IRON_DOOR, Direction.NORTH);
            BlockState button = Blocks.STONE_BUTTON.defaultBlockState().setValue(ButtonBlock.FACE, AttachFace.WALL);
            this.b.set(1, 2, z + 1, button.setValue(ButtonBlock.FACING, Direction.SOUTH));
            this.b.set(-1, 2, z - 1, button.setValue(ButtonBlock.FACING, Direction.NORTH));
        }

        /** Die Schatzkammer: Truhen, Gold, rohes Erz und Fässer unter Laternen. */
        void treasury() {
            int z1 = NORTH - 2;
            int z0 = NORTH - 7;
            for (int x = -5; x <= 5; x++) {
                for (int z = z0 - 1; z <= z1; z++) {
                    boolean edge = Math.abs(x) == 5 || z == z0 - 1;
                    boolean inlay = !edge && (x == 0 || z == z0 + 3) && Math.floorMod(x + z, 2) == 0;
                    this.b.set(x, 0, z, inlay ? Blocks.GILDED_BLACKSTONE.defaultBlockState() : Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
                    for (int y = 1; y <= 5; y++) {
                        this.b.set(x, y, z, edge ? this.wall(x, y, z) : Blocks.AIR.defaultBlockState());
                    }
                    this.b.set(x, 6, z, Blocks.DEEPSLATE_TILES);
                }
            }
            BlockState chest = Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH);
            for (int x : new int[] {-2, 0, 2}) {
                this.b.chest(x, 1, z0, chest, ModLootTables.SITE_DWARF_HOARD);
            }
            for (int sx : new int[] {-1, 1}) {
                this.b.set(sx * 4, 1, z0, Blocks.GOLD_BLOCK);
                this.b.set(sx * 4, 2, z0, Blocks.RAW_GOLD_BLOCK);
                this.b.set(sx * 4, 1, z0 + 1, Blocks.RAW_IRON_BLOCK);
                this.b.set(sx * 3, 1, z0, Blocks.RAW_COPPER_BLOCK);
                this.b.chest(sx * 4, 1, z1, Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, Direction.UP), ModLootTables.SITE_DWARF_HALL);
                this.b.hangingLantern(sx * 2, 4, z0 + 2, Blocks.LANTERN);
            }
        }

        /** Ein Stollen mit Schienen nach Osten ({@code sx} = 1) oder Westen, am Ende eine kleine Erzkammer. */
        void gallery(int sx) {
            // die Tür in der Hallenwand mit Rahmen
            for (int y = 1; y <= 4; y++) {
                for (int z = -2; z <= 2; z++) {
                    boolean open = Math.abs(z) <= 1 && y <= 3;
                    this.b.set(sx * (HW + 1), y, z, open ? Blocks.AIR.defaultBlockState() : Blocks.CHISELED_DEEPSLATE.defaultBlockState());
                }
            }
            for (int i = HW + 2; i <= GALLERY_END; i++) {
                int x = sx * i;
                boolean chamber = i >= GALLERY_END - 4;
                int w = chamber ? 2 : 1;
                int h = chamber ? 4 : 3;
                for (int z = -w - 1; z <= w + 1; z++) {
                    for (int y = 0; y <= h + 1; y++) {
                        boolean inside = Math.abs(z) <= w && y >= 1 && y <= h && i < GALLERY_END;
                        if (inside) {
                            this.b.set(x, y, z, Blocks.AIR);
                        } else if (chamber) {
                            this.b.set(x, y, z, this.ore(x, y, z));
                        } else if (y == 0) {
                            this.b.set(x, y, z, Blocks.TUFF);
                        } else {
                            this.b.set(x, y, z, this.rock(x, y, z));
                        }
                    }
                }
                if (!chamber) {
                    this.b.set(x, 1, 0, Blocks.RAIL.defaultBlockState().setValue(RailBlock.SHAPE, RailShape.EAST_WEST));
                    if (i % 6 == 2) {
                        // eine Fackel an der Stollenwand, abwechselnd links und rechts
                        int side = (i / 6) % 2 == 0 ? 1 : -1;
                        this.b.set(x, 2, side, Blocks.WALL_TORCH.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING,
                                side > 0 ? Direction.NORTH : Direction.SOUTH));
                    }
                    if (i % 3 == 0) {
                        // Stützen: zwei Pfosten und ein Balken
                        this.b.set(x, 1, -1, Blocks.SPRUCE_FENCE);
                        this.b.set(x, 2, -1, Blocks.SPRUCE_FENCE);
                        this.b.set(x, 1, 1, Blocks.SPRUCE_FENCE);
                        this.b.set(x, 2, 1, Blocks.SPRUCE_FENCE);
                        for (int z = -1; z <= 1; z++) {
                            this.b.set(x, 3, z, Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState().setValue(BlockStateProperties.AXIS, Direction.Axis.Z));
                        }
                    }
                }
            }
            // in der Erzkammer: eine Truhe, ein Prellbock, eine Laterne
            int end = sx * (GALLERY_END - 1);
            this.b.chest(end, 1, 0, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, sx > 0 ? Direction.WEST : Direction.EAST),
                    ModLootTables.SITE_DWARF_HALL);
            this.b.set(sx * (GALLERY_END - 4), 1, 0, VillageBuilder.slab(Blocks.SPRUCE_SLAB, SlabType.BOTTOM));
            this.b.hangingLantern(sx * (GALLERY_END - 2), 4, 1, Blocks.LANTERN);
        }

        /** Die Wand der Erzkammer: Tiefenschiefer mit Adern aus Eisen, Kupfer, Gold – und ganz selten einem Diamanten. */
        BlockState ore(int x, int y, int z) {
            float n = this.b.noise(x, y + 900, z);
            return n < 0.14f ? Blocks.DEEPSLATE_IRON_ORE.defaultBlockState()
                    : n < 0.24f ? Blocks.DEEPSLATE_COPPER_ORE.defaultBlockState()
                    : n < 0.3f ? Blocks.DEEPSLATE_GOLD_ORE.defaultBlockState()
                    : n < 0.31f ? Blocks.DEEPSLATE_DIAMOND_ORE.defaultBlockState()
                    : n < 0.36f ? Blocks.DEEPSLATE_REDSTONE_ORE.defaultBlockState() : Blocks.DEEPSLATE.defaultBlockState();
        }

        /** Das alte Haupttor im Süden: ein mächtiger Torbogen, das Gitter herabgelassen, dahinter verschüttet. */
        void gate() {
            int z = SOUTH + 1;
            for (int x = -3; x <= 3; x++) {
                for (int y = 1; y <= 7; y++) {
                    boolean frame = Math.abs(x) == 3 || y == 7;
                    boolean open = Math.abs(x) <= 2 && y <= 6;
                    if (frame) {
                        this.b.set(x, y, z, y == 7 && x == 0 ? Blocks.GOLD_BLOCK.defaultBlockState() : Blocks.CHISELED_DEEPSLATE.defaultBlockState());
                    } else if (open) {
                        this.b.set(x, y, z, Blocks.IRON_BARS);
                        this.b.set(x, y, z + 1, this.b.noise(x, y, z + 1) < 0.5f ? Blocks.GRAVEL.defaultBlockState() : Blocks.COBBLED_DEEPSLATE.defaultBlockState());
                    }
                }
            }
            this.b.set(0, 8, z, bulb());
            // Schutt vor dem Tor, innen
            for (int x = -2; x <= 2; x++) {
                if (this.b.noise(x, 1, SOUTH) < 0.5f) {
                    this.b.set(x, 1, SOUTH, Blocks.COBBLED_DEEPSLATE_SLAB.defaultBlockState());
                }
            }
        }
    }
}
