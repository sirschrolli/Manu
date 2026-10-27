package io.github.sirschrolli.kingdomsatwar.worldgen.village;

import com.mojang.serialization.Codec;
import io.github.sirschrolli.kingdomsatwar.registry.BuildingSet;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import java.util.Locale;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * Die vier Bauweisen der Dörfer – jede passt zu ihrem Land:
 * <ul>
 *   <li><b>Fachwerk</b> (Ebenen, Wiesen, Laubwälder): weißer Kalkputz zwischen dunklen Balken auf einem Sockel aus
 *       Mauerstein, rote oder braune Ziegeldächer, Bleiglasfenster mit grünen oder roten Läden;</li>
 *   <li><b>Nordisch</b> (Taiga, Schneeland): Blockhäuser aus liegenden Fichtenstämmen mit verkämmten Ecken auf einem
 *       Feldsteinsockel, steile Dächer aus dunklem Schiefer oder Holzschindeln;</li>
 *   <li><b>Lehm</b> (Wüste, Savanne): Lehm- und Kalkputz auf behauenem Sandstein, Flachdächer mit Brüstung, dunkle
 *       Balken, die aus den Wänden ragen, bunte Sonnensegel;</li>
 *   <li><b>Pfahlbau</b> (Sumpf, Moor): Holzhäuser auf Pfählen über Wasser und Schlamm, Stege statt Wegen,
 *       Schindeldächer.</li>
 * </ul>
 * Jedes Dorf würfelt eine Spielart ({@code variant}, 0–2): Dachfarbe, Läden, Holz.
 */
public enum VillageStyle implements StringRepresentable {
    TIMBER,
    NORDIC,
    CLAY,
    STILT;

    public static final Codec<VillageStyle> CODEC = StringRepresentable.fromEnum(VillageStyle::values);

    public String id() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    @Override
    public String getSerializedName() {
        return this.id();
    }

    /** Die Baustoffe dieser Bauweise in der Spielart {@code variant} (0–2). */
    public VillagePalette palette(int variant) {
        int v = Math.floorMod(variant, 3);
        return switch (this) {
            case TIMBER -> new VillagePalette(this, v,
                    ModBlocks.MEDIEVAL_STONE, ModBlocks.ROUGH_FIELDSTONE,
                    v == 2 ? WoodSet.DARK_OAK : WoodSet.SPRUCE, WoodSet.SPRUCE,
                    v == 1 ? ModBlocks.BROWN_ROOF_TILES : ModBlocks.RED_ROOF_TILES,
                    v == 1 ? ModBlocks.RED_ROOF_TILES : ModBlocks.BROWN_ROOF_TILES,
                    v == 0 ? ModBlocks.GREEN_SHUTTERS.get() : ModBlocks.RED_SHUTTERS.get(),
                    ModBlocks.LEADED_GLASS_PANE.get(), ModBlocks.STEEL_LANTERN.get());
            case NORDIC -> new VillagePalette(this, v,
                    ModBlocks.ROUGH_FIELDSTONE, ModBlocks.MOSSY_WALL_STONES,
                    WoodSet.SPRUCE, WoodSet.SPRUCE,
                    v == 2 ? ModBlocks.WOODEN_SHINGLES : ModBlocks.DARK_SLATE_SHINGLES,
                    v == 2 ? ModBlocks.DARK_SLATE_SHINGLES : ModBlocks.WOODEN_SHINGLES,
                    v == 2 ? ModBlocks.GREEN_SHUTTERS.get() : ModBlocks.RED_SHUTTERS.get(), Blocks.GLASS_PANE, Blocks.LANTERN);
            case CLAY -> new VillagePalette(this, v,
                    ModBlocks.HEWN_SANDSTONE, ModBlocks.HEWN_SANDSTONE,
                    WoodSet.ACACIA, WoodSet.DARK_OAK,
                    v == 2 ? ModBlocks.BROWN_ROOF_TILES : ModBlocks.LIGHT_ROOF_TILES,
                    ModBlocks.BROWN_ROOF_TILES,
                    v == 1 ? Blocks.WARPED_TRAPDOOR : Blocks.SPRUCE_TRAPDOOR, Blocks.GLASS_PANE, Blocks.LANTERN);
            case STILT -> new VillagePalette(this, v,
                    ModBlocks.ROUGH_FIELDSTONE, ModBlocks.MOSSY_WALL_STONES,
                    v == 1 ? WoodSet.MANGROVE : WoodSet.SPRUCE, WoodSet.DARK_OAK,
                    ModBlocks.WOODEN_SHINGLES, v == 2 ? ModBlocks.DARK_SLATE_SHINGLES : ModBlocks.WOODEN_SHINGLES,
                    v == 2 ? Blocks.SPRUCE_TRAPDOOR : Blocks.MANGROVE_TRAPDOOR, Blocks.GLASS_PANE, Blocks.LANTERN);
        };
    }

    /** Eine Holzart mit allem, was ein Dorf daraus baut. */
    public enum WoodSet {
        SPRUCE,
        DARK_OAK,
        ACACIA,
        MANGROVE,
        OAK;

        public Block planks() {
            return switch (this) {
                case SPRUCE -> Blocks.SPRUCE_PLANKS;
                case DARK_OAK -> Blocks.DARK_OAK_PLANKS;
                case ACACIA -> Blocks.ACACIA_PLANKS;
                case MANGROVE -> Blocks.MANGROVE_PLANKS;
                case OAK -> Blocks.OAK_PLANKS;
            };
        }

        public Block stairs() {
            return switch (this) {
                case SPRUCE -> Blocks.SPRUCE_STAIRS;
                case DARK_OAK -> Blocks.DARK_OAK_STAIRS;
                case ACACIA -> Blocks.ACACIA_STAIRS;
                case MANGROVE -> Blocks.MANGROVE_STAIRS;
                case OAK -> Blocks.OAK_STAIRS;
            };
        }

        public Block slab() {
            return switch (this) {
                case SPRUCE -> Blocks.SPRUCE_SLAB;
                case DARK_OAK -> Blocks.DARK_OAK_SLAB;
                case ACACIA -> Blocks.ACACIA_SLAB;
                case MANGROVE -> Blocks.MANGROVE_SLAB;
                case OAK -> Blocks.OAK_SLAB;
            };
        }

        public Block log() {
            return switch (this) {
                case SPRUCE -> Blocks.SPRUCE_LOG;
                case DARK_OAK -> Blocks.DARK_OAK_LOG;
                case ACACIA -> Blocks.ACACIA_LOG;
                case MANGROVE -> Blocks.MANGROVE_LOG;
                case OAK -> Blocks.OAK_LOG;
            };
        }

        public Block strippedLog() {
            return switch (this) {
                case SPRUCE -> Blocks.STRIPPED_SPRUCE_LOG;
                case DARK_OAK -> Blocks.STRIPPED_DARK_OAK_LOG;
                case ACACIA -> Blocks.STRIPPED_ACACIA_LOG;
                case MANGROVE -> Blocks.STRIPPED_MANGROVE_LOG;
                case OAK -> Blocks.STRIPPED_OAK_LOG;
            };
        }

        public Block fence() {
            return switch (this) {
                case SPRUCE -> Blocks.SPRUCE_FENCE;
                case DARK_OAK -> Blocks.DARK_OAK_FENCE;
                case ACACIA -> Blocks.ACACIA_FENCE;
                case MANGROVE -> Blocks.MANGROVE_FENCE;
                case OAK -> Blocks.OAK_FENCE;
            };
        }

        public Block fenceGate() {
            return switch (this) {
                case SPRUCE -> Blocks.SPRUCE_FENCE_GATE;
                case DARK_OAK -> Blocks.DARK_OAK_FENCE_GATE;
                case ACACIA -> Blocks.ACACIA_FENCE_GATE;
                case MANGROVE -> Blocks.MANGROVE_FENCE_GATE;
                case OAK -> Blocks.OAK_FENCE_GATE;
            };
        }

        public Block door() {
            return switch (this) {
                case SPRUCE -> Blocks.SPRUCE_DOOR;
                case DARK_OAK -> Blocks.DARK_OAK_DOOR;
                case ACACIA -> Blocks.ACACIA_DOOR;
                case MANGROVE -> Blocks.MANGROVE_DOOR;
                case OAK -> Blocks.OAK_DOOR;
            };
        }

        public Block trapdoor() {
            return switch (this) {
                case SPRUCE -> Blocks.SPRUCE_TRAPDOOR;
                case DARK_OAK -> Blocks.DARK_OAK_TRAPDOOR;
                case ACACIA -> Blocks.ACACIA_TRAPDOOR;
                case MANGROVE -> Blocks.MANGROVE_TRAPDOOR;
                case OAK -> Blocks.OAK_TRAPDOOR;
            };
        }

        public Block shelf() {
            return switch (this) {
                case SPRUCE -> Blocks.SPRUCE_SHELF;
                case DARK_OAK -> Blocks.DARK_OAK_SHELF;
                case ACACIA -> Blocks.ACACIA_SHELF;
                case MANGROVE -> Blocks.MANGROVE_SHELF;
                case OAK -> Blocks.OAK_SHELF;
            };
        }

        public Block hangingSign() {
            return switch (this) {
                case SPRUCE -> Blocks.SPRUCE_WALL_HANGING_SIGN;
                case DARK_OAK -> Blocks.DARK_OAK_WALL_HANGING_SIGN;
                case ACACIA -> Blocks.ACACIA_WALL_HANGING_SIGN;
                case MANGROVE -> Blocks.MANGROVE_WALL_HANGING_SIGN;
                case OAK -> Blocks.OAK_WALL_HANGING_SIGN;
            };
        }
    }

    /** Hilfe für die Paletten: die Steinsorte eines Baustoff-Satzes. */
    static Block base(BuildingSet set) {
        return set.base().get();
    }
}
