package io.github.sirschrolli.kingdomsatwar.registry;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.BattlementBlock;
import io.github.sirschrolli.kingdomsatwar.block.ChandelierBlock;
import io.github.sirschrolli.kingdomsatwar.block.ElementalDragonEggBlock;
import io.github.sirschrolli.kingdomsatwar.block.FeastBlock;
import io.github.sirschrolli.kingdomsatwar.block.FurnitureShapes;
import io.github.sirschrolli.kingdomsatwar.block.ModCropBlock;
import io.github.sirschrolli.kingdomsatwar.block.RecruitmentTableBlock;
import io.github.sirschrolli.kingdomsatwar.block.SeatBlock;
import io.github.sirschrolli.kingdomsatwar.block.TableBlock;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Alle Blöcke der Mod. Zu jedem Block mit Gegenstandsform wird hier auch das {@code BlockItem} registriert.
 */
public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(KingdomsAtWar.MODID);

    // Reihenfolge der Einträge in den Kreativ-Tabs
    static final List<Supplier<? extends ItemLike>> BUILDING_TAB = new ArrayList<>();
    static final List<Supplier<? extends ItemLike>> RESOURCE_TAB = new ArrayList<>();
    static final List<Supplier<? extends ItemLike>> DRAGON_TAB = new ArrayList<>();
    static final List<Supplier<? extends ItemLike>> FARMING_TAB = new ArrayList<>();
    static final List<Supplier<? extends ItemLike>> KINGDOM_TAB = new ArrayList<>();
    /** Blöcke der Nest-Biome (Aschelande und später die übrigen fünf). */
    static final List<Supplier<? extends ItemLike>> NATURE_TAB = new ArrayList<>();
    /** Blöcke der Schattenwelt (siehe {@link ShadowBlocks}). */
    static final List<Supplier<? extends ItemLike>> SHADOW_TAB = new ArrayList<>();

    /** Inhalt des Schattenwelt-Tabs (Blöcke und Gegenstände, in Reihenfolge). */
    public static List<Supplier<? extends ItemLike>> shadowTab() {
        return java.util.Collections.unmodifiableList(SHADOW_TAB);
    }

    private static final List<BuildingSet> BUILDING_SETS_MUTABLE = new ArrayList<>();
    /** Alle Baustoff-Sets (Vollblock, Treppe, Stufe, Mauer). */
    public static final List<BuildingSet> BUILDING_SETS = Collections.unmodifiableList(BUILDING_SETS_MUTABLE);

    // =====================================================================================
    // Kingdoms at War – Burgen & Bauen (Bild "Neue Blöcke")
    // =====================================================================================

    // 1. Neue Steinblöcke für Burgen und Festungen
    public static final BuildingSet MEDIEVAL_STONE = stoneSet("medieval_stone", "medieval_stone",
            MapColor.STONE, SoundType.STONE, 1.5f);
    public static final BuildingSet ROUGH_FIELDSTONE = stoneSet("rough_fieldstone", "rough_fieldstone",
            MapColor.STONE, SoundType.STONE, 2.0f);
    public static final BuildingSet HEWN_SANDSTONE = stoneSet("hewn_sandstone", "hewn_sandstone",
            MapColor.SAND, SoundType.STONE, 0.8f);
    public static final BuildingSet DARK_STONE_BRICKS = stoneSet("dark_stone_bricks", "dark_stone_brick",
            MapColor.DEEPSLATE, SoundType.DEEPSLATE_BRICKS, 2.5f);
    public static final BuildingSet LIMESTONE_BRICKS = stoneSet("limestone_bricks", "limestone_brick",
            MapColor.TERRACOTTA_WHITE, SoundType.CALCITE, 1.2f);
    public static final BuildingSet WEATHERED_STONE_BRICKS = stoneSet("weathered_stone_bricks", "weathered_stone_brick",
            MapColor.STONE, SoundType.STONE, 1.5f);
    public static final BuildingSet MOSSY_WALL_STONES = stoneSet("mossy_wall_stones", "mossy_wall_stone",
            MapColor.STONE, SoundType.STONE, 1.5f);
    public static final BuildingSet CRACKED_STONE_BLOCKS = stoneSet("cracked_stone_blocks", "cracked_stone_block",
            MapColor.STONE, SoundType.STONE, 1.5f);

    public static final DeferredBlock<RotatedPillarBlock> STONE_PILLAR = blockWithItem("stone_pillar",
            RotatedPillarBlock::new,
            p -> p.mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops().strength(1.5f, 6.0f).sound(SoundType.STONE),
            UnaryOperator.identity(), BUILDING_TAB);

    // 2. Holzblöcke und Fachwerk
    public static final DeferredBlock<RotatedPillarBlock> TIMBER_BEAM = blockWithItem("timber_beam",
            RotatedPillarBlock::new,
            p -> p.mapColor(MapColor.COLOR_BROWN).instrument(NoteBlockInstrument.BASS)
                    .strength(2.0f, 3.0f).sound(SoundType.WOOD).ignitedByLava(),
            ip -> ip.cookingFuel(ContextIntProviders.COOKING_TIME_WOOD_BLOCKS), BUILDING_TAB);
    public static final DeferredBlock<Block> TIMBER_FRAME = timberFrame("timber_frame");
    public static final DeferredBlock<Block> TIMBER_FRAME_DIAGONAL = timberFrame("timber_frame_diagonal");
    public static final DeferredBlock<Block> TIMBER_FRAME_CROSS = timberFrame("timber_frame_cross");

    // 3. Dachblöcke
    public static final BuildingSet RED_ROOF_TILES = roofSet("red_roof_tiles", "red_roof_tile",
            p -> p.mapColor(MapColor.TERRACOTTA_RED).instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops().strength(1.25f, 4.2f).sound(SoundType.STONE), BuildingSet.Tool.PICKAXE);
    public static final BuildingSet BROWN_ROOF_TILES = roofSet("brown_roof_tiles", "brown_roof_tile",
            p -> p.mapColor(MapColor.TERRACOTTA_BROWN).instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops().strength(1.25f, 4.2f).sound(SoundType.STONE), BuildingSet.Tool.PICKAXE);
    public static final BuildingSet DARK_SLATE_SHINGLES = roofSet("dark_slate_shingles", "dark_slate_shingle",
            p -> p.mapColor(MapColor.DEEPSLATE).instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops().strength(2.0f, 6.0f).sound(SoundType.DEEPSLATE_TILES), BuildingSet.Tool.PICKAXE);
    public static final BuildingSet WOODEN_SHINGLES = roofSet("wooden_shingles", "wooden_shingle",
            p -> p.mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS)
                    .strength(2.0f, 3.0f).sound(SoundType.WOOD).ignitedByLava(), BuildingSet.Tool.AXE);
    public static final BuildingSet LIGHT_ROOF_TILES = roofSet("light_roof_tiles", "light_roof_tile",
            p -> p.mapColor(MapColor.TERRACOTTA_WHITE).instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops().strength(1.25f, 4.2f).sound(SoundType.STONE), BuildingSet.Tool.PICKAXE);
    public static final BuildingSet COPPER_ROOF = roofSet("copper_roof", "copper_roof",
            p -> p.mapColor(MapColor.COLOR_ORANGE)
                    .requiresCorrectToolForDrops().strength(3.0f, 6.0f).sound(SoundType.COPPER), BuildingSet.Tool.PICKAXE);

    // 4. Putz, Fenster, Türen und Fensterläden
    public static final BuildingSet WHITE_PLASTER = roofSet("white_plaster", "white_plaster",
            p -> p.mapColor(MapColor.SNOW).instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops().strength(1.0f, 3.0f).sound(SoundType.STONE), BuildingSet.Tool.PICKAXE);
    public static final BuildingSet CLAY_PLASTER = roofSet("clay_plaster", "clay_plaster",
            p -> p.mapColor(MapColor.DIRT).instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops().strength(1.0f, 3.0f).sound(SoundType.PACKED_MUD), BuildingSet.Tool.PICKAXE);

    public static final DeferredBlock<TransparentBlock> LEADED_GLASS = blockWithItem("leaded_glass", TransparentBlock::new,
            p -> p.instrument(NoteBlockInstrument.HAT).strength(0.3f).sound(SoundType.GLASS).noOcclusion()
                    .isValidSpawn((state, level, pos, type) -> false).isRedstoneConductor((state, level, pos) -> false)
                    .isSuffocating((state, level, pos) -> false),
            UnaryOperator.identity(), BUILDING_TAB);
    public static final DeferredBlock<IronBarsBlock> LEADED_GLASS_PANE = blockWithItem("leaded_glass_pane", IronBarsBlock::new,
            p -> p.instrument(NoteBlockInstrument.HAT).strength(0.3f).sound(SoundType.GLASS).noOcclusion(),
            UnaryOperator.identity(), BUILDING_TAB);

    /** Schwere Eichentür mit Eisenbeschlägen und vergittertem Guckfenster. */
    public static final DeferredBlock<DoorBlock> CASTLE_DOOR = door("castle_door");
    public static final DeferredBlock<TrapDoorBlock> GREEN_SHUTTERS = shutters("green_shutters", MapColor.COLOR_GREEN);
    public static final DeferredBlock<TrapDoorBlock> RED_SHUTTERS = shutters("red_shutters", MapColor.COLOR_RED);

    // 5. Licht
    public static final DeferredBlock<LanternBlock> STEEL_LANTERN = lantern("steel_lantern");
    /** Leuchtet ohne Flamme: das Licht kommt vom Aetherit-Kristall. */
    public static final DeferredBlock<LanternBlock> AETHERIT_LANTERN = lantern("aetherit_lantern");
    public static final DeferredBlock<ChandelierBlock> CHANDELIER = blockWithItem("chandelier", ChandelierBlock::new,
            p -> p.mapColor(MapColor.METAL).strength(2.0f).sound(SoundType.LANTERN).lightLevel(state -> 15)
                    .noOcclusion().pushReaction(PushReaction.POPPED),
            UnaryOperator.identity(), BUILDING_TAB);

    // 6. Zinnen für die Burgmauern
    public static final DeferredBlock<BattlementBlock> MEDIEVAL_STONE_BATTLEMENT = battlement("medieval_stone_battlement", MEDIEVAL_STONE);
    public static final DeferredBlock<BattlementBlock> DARK_STONE_BRICK_BATTLEMENT = battlement("dark_stone_brick_battlement", DARK_STONE_BRICKS);
    public static final DeferredBlock<BattlementBlock> LIMESTONE_BRICK_BATTLEMENT = battlement("limestone_brick_battlement", LIMESTONE_BRICKS);

    /** Zinnen mit dem Baustoff, aus dem sie gehauen werden (feste Reihenfolge). */
    public static final Map<DeferredBlock<BattlementBlock>, BuildingSet> BATTLEMENTS;

    static {
        Map<DeferredBlock<BattlementBlock>, BuildingSet> battlements = new LinkedHashMap<>();
        battlements.put(MEDIEVAL_STONE_BATTLEMENT, MEDIEVAL_STONE);
        battlements.put(DARK_STONE_BRICK_BATTLEMENT, DARK_STONE_BRICKS);
        battlements.put(LIMESTONE_BRICK_BATTLEMENT, LIMESTONE_BRICKS);
        BATTLEMENTS = Collections.unmodifiableMap(battlements);
    }

    // 7. Möbel (zum Draufsetzen)
    public static final DeferredBlock<SeatBlock> OAK_CHAIR = seat("oak_chair", FurnitureShapes.CHAIR, FurnitureShapes.CHAIR_SEAT, MapColor.WOOD);
    public static final DeferredBlock<SeatBlock> OAK_BENCH = seat("oak_bench", FurnitureShapes.BENCH, FurnitureShapes.BENCH_SEAT, MapColor.WOOD);
    public static final DeferredBlock<TableBlock> OAK_TABLE = blockWithItem("oak_table", TableBlock::new,
            p -> p.mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.0f, 3.0f).sound(SoundType.WOOD)
                    .noOcclusion().ignitedByLava(),
            UnaryOperator.identity(), BUILDING_TAB);
    public static final DeferredBlock<SeatBlock> THRONE = blockWithItem("throne",
            p -> new SeatBlock(FurnitureShapes.THRONE, FurnitureShapes.THRONE_SEAT, p),
            p -> p.mapColor(MapColor.COLOR_RED).instrument(NoteBlockInstrument.BASS).strength(2.5f, 3.0f).sound(SoundType.WOOD)
                    .noOcclusion().ignitedByLava(),
            ip -> ip.rarity(Rarity.RARE).component(DataComponents.LORE, ModItems.lore("throne", 1)), BUILDING_TAB);

    // =====================================================================================
    // Dragon Warfare – Erze & Rohstoffe (Bild "Dragon Warfare – Erze")
    // =====================================================================================

    public static final DeferredBlock<DropExperienceBlock> CARBON_ORE = stoneOre("carbon_ore", 0, 2, 0);
    public static final DeferredBlock<DropExperienceBlock> DEEPSLATE_CARBON_ORE = deepslateOre("deepslate_carbon_ore", 0, 2, 0);
    public static final DeferredBlock<DropExperienceBlock> GEMSTONE_ORE = stoneOre("gemstone_ore", 3, 7, 0);
    public static final DeferredBlock<DropExperienceBlock> DEEPSLATE_GEMSTONE_ORE = deepslateOre("deepslate_gemstone_ore", 3, 7, 0);
    public static final DeferredBlock<DropExperienceBlock> KONTERIT_ORE = stoneOre("konterit_ore", 2, 5, 0);
    public static final DeferredBlock<DropExperienceBlock> DEEPSLATE_KONTERIT_ORE = deepslateOre("deepslate_konterit_ore", 2, 5, 0);
    public static final DeferredBlock<DropExperienceBlock> AETHERIT_ORE = stoneOre("aetherit_ore", 2, 5, 3);
    public static final DeferredBlock<DropExperienceBlock> DEEPSLATE_AETHERIT_ORE = deepslateOre("deepslate_aetherit_ore", 2, 5, 3);
    public static final DeferredBlock<DropExperienceBlock> DRACONIUM_ORE = stoneOre("draconium_ore", 4, 8, 5);
    public static final DeferredBlock<DropExperienceBlock> DEEPSLATE_DRACONIUM_ORE = deepslateOre("deepslate_draconium_ore", 4, 8, 5);
    /** Wyvernit kommt nur hoch in den Bergen vor, darum gibt es keine Tiefenschiefer-Variante. */
    public static final DeferredBlock<DropExperienceBlock> WYVERNIT_ORE = stoneOre("wyvernit_ore", 3, 7, 0);

    public static final DeferredBlock<Block> CARBON_BLOCK = blockWithItem("carbon_block", Block::new,
            p -> p.mapColor(MapColor.COLOR_BLACK).instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops().strength(5.0f, 6.0f).sound(SoundType.STONE),
            ip -> ip.cookingFuel(ContextIntProviders.COOKING_TIME_COAL_BLOCK), RESOURCE_TAB);
    public static final DeferredBlock<Block> GEMSTONE_BLOCK = crystalBlock("gemstone_block", MapColor.COLOR_PURPLE, 0);
    public static final DeferredBlock<Block> KONTERIT_BLOCK = crystalBlock("konterit_block", MapColor.COLOR_MAGENTA, 0);
    public static final DeferredBlock<Block> AETHERIT_BLOCK = crystalBlock("aetherit_block", MapColor.COLOR_CYAN, 7);
    public static final DeferredBlock<Block> DRACONIUM_BLOCK = crystalBlock("draconium_block", MapColor.COLOR_RED, 9);
    public static final DeferredBlock<Block> WYVERNIT_BLOCK = crystalBlock("wyvernit_block", MapColor.COLOR_LIGHT_BLUE, 0);
    public static final DeferredBlock<Block> STEEL_BLOCK = blockWithItem("steel_block", Block::new,
            p -> p.mapColor(MapColor.METAL).instrument(NoteBlockInstrument.IRON_XYLOPHONE)
                    .requiresCorrectToolForDrops().strength(5.0f, 6.0f).sound(SoundType.METAL),
            UnaryOperator.identity(), RESOURCE_TAB);

    // =====================================================================================
    // Dragon Warfare – Drachen (Bild "Dragon Warfare – Sechs Drachenarten")
    // =====================================================================================

    private static final Map<DragonType, DeferredBlock<ElementalDragonEggBlock>> DRAGON_EGGS_MUTABLE = new EnumMap<>(DragonType.class);
    /** Ein Drachenei je Drachenart. */
    public static final Map<DragonType, DeferredBlock<ElementalDragonEggBlock>> DRAGON_EGGS = Collections.unmodifiableMap(DRAGON_EGGS_MUTABLE);

    static {
        for (DragonType type : DragonType.values()) {
            String name = type.id() + "_dragon_egg";
            DRAGON_EGGS_MUTABLE.put(type, blockWithItem(name,
                    p -> new ElementalDragonEggBlock(type, p),
                    p -> p.mapColor(type.mapColor()).strength(3.0f, 9.0f).lightLevel(state -> 1)
                            .noOcclusion().sound(SoundType.STONE).pushReaction(PushReaction.IMMOVEABLE),
                    ip -> ip.rarity(Rarity.EPIC).stacksTo(16)
                            .component(DataComponents.LORE, ModItems.lore(name, 3)),
                    DRAGON_TAB));
        }
    }

    // =====================================================================================
    // Kingdoms at War – Landwirtschaft (Bild "Nahrung & Pflanzen")
    // =====================================================================================

    public static final DeferredBlock<ModCropBlock> TOMATOES = crop("tomatoes", () -> ModItems.TOMATO_SEEDS.get());
    public static final DeferredBlock<ModCropBlock> CABBAGES = crop("cabbages", () -> ModItems.CABBAGE_SEEDS.get());
    public static final DeferredBlock<ModCropBlock> ONIONS = crop("onions", () -> ModItems.ONION.get());
    public static final DeferredBlock<ModCropBlock> BARLEY = crop("barley", () -> ModItems.BARLEY_SEEDS.get());
    /** Hopfen rankt an einer Stange, Weintrauben an einem Rebpfahl; Knoblauch wächst wie Zwiebeln. */
    public static final DeferredBlock<ModCropBlock> HOPS = crop("hops", () -> ModItems.HOPS.get());
    public static final DeferredBlock<ModCropBlock> GRAPES = crop("grapes", () -> ModItems.GRAPES.get());
    public static final DeferredBlock<ModCropBlock> GARLIC_CROP = crop("garlic_crop", () -> ModItems.GARLIC.get());

    /** Alle Feldfrüchte. */
    public static final List<DeferredBlock<ModCropBlock>> CROPS = List.of(TOMATOES, CABBAGES, ONIONS, BARLEY, HOPS, GRAPES, GARLIC_CROP);

    // 5. Dekorative Pflanzen
    public static final DeferredBlock<FlowerBlock> LAVENDER = flower("lavender", MobEffects.REGENERATION, 8.0f);
    public static final DeferredBlock<FlowerBlock> GARDEN_ROSE = flower("garden_rose", MobEffects.NIGHT_VISION, 5.0f);
    public static final DeferredBlock<FlowerBlock> FIRE_LILY = flower("fire_lily", MobEffects.FIRE_RESISTANCE, 4.0f);
    public static final DeferredBlock<FlowerBlock> FROST_FLOWER = flower("frost_flower", MobEffects.SLOWNESS, 6.0f);
    /** Leuchtet nachts schwach bläulich – wächst dort, wo auch Irrlichter tanzen. */
    public static final DeferredBlock<FlowerBlock> MOONFLOWER = flower("moonflower", MobEffects.SLOW_FALLING, 7.0f, 7);
    /** Hainglocke: hohe Blume mit blassblauen, schwach leuchtenden Glocken – wächst im Uralten Hain. */
    public static final DeferredBlock<FlowerBlock> GROVE_BELL = flower("grove_bell", MobEffects.LUCK, 8.0f, 5);
    /** Lebensblüte: aus ihr schöpft Verdantis Heilung – wer sie zertritt, schwächt die Hüterin. */
    public static final DeferredBlock<FlowerBlock> LIFE_BLOSSOM = blockWithItem("life_blossom",
            p -> new io.github.sirschrolli.kingdomsatwar.block.LifeBlossomBlock(MobEffects.REGENERATION, 6.0f, p),
            p -> p.mapColor(MapColor.COLOR_PINK).noCollision().instabreak().sound(SoundType.SPORE_BLOSSOM)
                    .offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.POPPED).lightLevel(state -> 10),
            ip -> ip.compostable(ContextIntProviders.COMPOSTABLE_MEDIUM), FARMING_TAB);

    public static final DeferredBlock<FlowerPotBlock> POTTED_LAVENDER = pottedFlower("potted_lavender", LAVENDER);
    public static final DeferredBlock<FlowerPotBlock> POTTED_GARDEN_ROSE = pottedFlower("potted_garden_rose", GARDEN_ROSE);
    public static final DeferredBlock<FlowerPotBlock> POTTED_FIRE_LILY = pottedFlower("potted_fire_lily", FIRE_LILY);
    public static final DeferredBlock<FlowerPotBlock> POTTED_FROST_FLOWER = pottedFlower("potted_frost_flower", FROST_FLOWER);
    public static final DeferredBlock<FlowerPotBlock> POTTED_MOONFLOWER = pottedFlower("potted_moonflower", MOONFLOWER, 7);
    public static final DeferredBlock<FlowerPotBlock> POTTED_GROVE_BELL = pottedFlower("potted_grove_bell", GROVE_BELL, 5);
    public static final DeferredBlock<FlowerPotBlock> POTTED_LIFE_BLOSSOM = pottedFlower("potted_life_blossom", LIFE_BLOSSOM, 10);

    /** Zierblumen mit ihrem Blumentopf (feste Reihenfolge, damit die erzeugten Dateien gleich bleiben). */
    public static final Map<DeferredBlock<FlowerBlock>, DeferredBlock<FlowerPotBlock>> FLOWERS;

    static {
        Map<DeferredBlock<FlowerBlock>, DeferredBlock<FlowerPotBlock>> flowers = new LinkedHashMap<>();
        flowers.put(LAVENDER, POTTED_LAVENDER);
        flowers.put(GARDEN_ROSE, POTTED_GARDEN_ROSE);
        flowers.put(FIRE_LILY, POTTED_FIRE_LILY);
        flowers.put(FROST_FLOWER, POTTED_FROST_FLOWER);
        flowers.put(MOONFLOWER, POTTED_MOONFLOWER);
        flowers.put(GROVE_BELL, POTTED_GROVE_BELL);
        flowers.put(LIFE_BLOSSOM, POTTED_LIFE_BLOSSOM);
        FLOWERS = Collections.unmodifiableMap(flowers);
    }

    /** Festmahl: Braten mit Beilagen auf einem Brett, vier Portionen zum Essen direkt vom Tisch. */
    public static final DeferredBlock<FeastBlock> FEAST = blockWithItem("feast", FeastBlock::new,
            p -> p.mapColor(MapColor.WOOD).strength(0.5f).sound(SoundType.WOOD).noOcclusion().pushReaction(PushReaction.POPPED),
            ip -> ip.stacksTo(1).component(DataComponents.LORE, ModItems.lore("feast", 1)), FARMING_TAB);

    // =====================================================================================
    // Kingdoms at War – Armee
    // =====================================================================================

    /** Hier wirbt man Soldaten an. */
    /** Nestkern unter jedem Drachennest (unzerstörbar, ohne Gegenstand). */
    // =====================================================================================
    // Aschelande: das Biom um Pyrrhons Vulkankrater
    // =====================================================================================

    /** Feine Asche – rieselt wie Sand. */
    public static final DeferredBlock<net.minecraft.world.level.block.ColoredFallingBlock> ASH_BLOCK = blockWithItem("ash_block",
            p -> new net.minecraft.world.level.block.ColoredFallingBlock(new net.minecraft.util.ColorRGBA(0xFF5A5652), p),
            p -> p.mapColor(MapColor.COLOR_GRAY).instrument(NoteBlockInstrument.SNARE).strength(0.5f).sound(SoundType.SAND),
            UnaryOperator.identity(), NATURE_TAB);
    /** Ascheschicht in bis zu acht Lagen (wie Schnee, schmilzt aber nicht). */
    public static final DeferredBlock<io.github.sirschrolli.kingdomsatwar.block.AshLayerBlock> ASH_LAYER = blockWithItem("ash_layer",
            io.github.sirschrolli.kingdomsatwar.block.AshLayerBlock::new,
            p -> p.mapColor(MapColor.COLOR_GRAY).replaceable().forceSolidOff().strength(0.1f).sound(SoundType.SAND)
                    .isViewBlocking((state, level, pos, box) -> state.getValue(net.minecraft.world.level.block.SnowLayerBlock.LAYERS) >= 8)
                    .pushReaction(PushReaction.POPPED),
            UnaryOperator.identity(), NATURE_TAB);
    /** Verbrannte Erde unter der Asche. */
    public static final DeferredBlock<Block> CINDER_SOIL = blockWithItem("cinder_soil", Block::new,
            p -> p.mapColor(MapColor.TERRACOTTA_BLACK).strength(0.6f).sound(SoundType.ROOTED_DIRT),
            UnaryOperator.identity(), NATURE_TAB);
    /** Poröses Vulkangestein (auch als Treppe, Stufe und Mauer zum Bauen). */
    public static final BuildingSet VOLCANIC_ROCK = stoneSet("volcanic_rock", "volcanic_rock", MapColor.COLOR_BLACK, SoundType.TUFF, 1.5f);
    /** Vulkangestein mit glühenden Rissen – heiß wie Magma. */
    public static final DeferredBlock<net.minecraft.world.level.block.MagmaBlock> GLOWING_VOLCANIC_ROCK = blockWithItem("glowing_volcanic_rock",
            net.minecraft.world.level.block.MagmaBlock::new,
            p -> p.mapColor(MapColor.NETHER).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5f, 6.0f)
                    .sound(SoundType.TUFF).lightLevel(state -> 6).isValidSpawn((state, level, pos, type) -> type.fireImmune()),
            UnaryOperator.identity(), NATURE_TAB);
    /** Verkohlter Baumstamm – was vom Wald übrig blieb. */
    public static final DeferredBlock<RotatedPillarBlock> CHARRED_LOG = blockWithItem("charred_log", RotatedPillarBlock::new,
            p -> p.mapColor(MapColor.COLOR_BLACK).instrument(NoteBlockInstrument.BASS).strength(1.5f).sound(SoundType.WOOD),
            UnaryOperator.identity(), NATURE_TAB);
    /** Glutspalte: Schlot mit Rauchsäule und spritzender Glut. */
    public static final DeferredBlock<io.github.sirschrolli.kingdomsatwar.block.EmberVentBlock> EMBER_VENT = blockWithItem("ember_vent",
            io.github.sirschrolli.kingdomsatwar.block.EmberVentBlock::new,
            p -> p.mapColor(MapColor.COLOR_BLACK).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0f, 6.0f)
                    .sound(SoundType.TUFF).lightLevel(state -> 10).isValidSpawn((state, level, pos, type) -> type.fireImmune()),
            UnaryOperator.identity(), NATURE_TAB);

    // =====================================================================================
    // Frostkessel: das Biom um Glaciaras Gletscherkessel
    // =====================================================================================

    /** Frostkristall: durchscheinend, leuchtet eisblau. */
    public static final DeferredBlock<net.minecraft.world.level.block.HalfTransparentBlock> FROST_CRYSTAL = blockWithItem("frost_crystal",
            net.minecraft.world.level.block.HalfTransparentBlock::new,
            p -> p.mapColor(MapColor.ICE).strength(1.5f).sound(SoundType.AMETHYST).noOcclusion().lightLevel(s -> 8)
                    .isValidSpawn((state, level, pos, type) -> false).isRedstoneConductor((state, level, pos) -> false)
                    .isSuffocating((state, level, pos) -> false).isViewBlocking((state, level, pos, box) -> false),
            UnaryOperator.identity(), NATURE_TAB);
    /** Gletschereis: uraltes, dichtes Eis in Schichten – schmilzt nie, sehr glatt. */
    public static final DeferredBlock<Block> GLACIER_ICE = blockWithItem("glacier_ice", Block::new,
            p -> p.mapColor(MapColor.ICE).strength(2.8f).friction(0.985f).sound(SoundType.GLASS),
            UnaryOperator.identity(), NATURE_TAB);
    /** Raureifstein: Stein unter einer Kruste aus Reif (auch als Treppe, Stufe und Mauer). */
    public static final BuildingSet RIME_STONE = stoneSet("rime_stone", "rime_stone", MapColor.SNOW, SoundType.STONE, 1.5f);
    /** Frosterde: gefrorener Boden, auf dem Frostblumen wachsen. */
    public static final DeferredBlock<Block> FROZEN_SOIL = blockWithItem("frozen_soil", Block::new,
            p -> p.mapColor(MapColor.SNOW).strength(0.7f).sound(SoundType.ROOTED_DIRT),
            UnaryOperator.identity(), NATURE_TAB);
    /** Eiszapfen, die unter Überhängen hängen. */
    public static final DeferredBlock<io.github.sirschrolli.kingdomsatwar.block.IcicleBlock> ICICLE = blockWithItem("icicle",
            io.github.sirschrolli.kingdomsatwar.block.IcicleBlock::new,
            p -> p.mapColor(MapColor.ICE).noCollision().noOcclusion().strength(0.3f).sound(SoundType.GLASS).pushReaction(PushReaction.POPPED),
            UnaryOperator.identity(), NATURE_TAB);

    // =====================================================================================
    // Uralter Hain: das Biom um Verdantis' Nest
    // =====================================================================================

    /** Urholz: Stamm der uralten Hainbäume – graugrüne, tief gefurchte Borke mit Moos und Flechten. */
    public static final DeferredBlock<RotatedPillarBlock> ANCIENT_LOG = blockWithItem("ancient_log", RotatedPillarBlock::new,
            p -> p.mapColor(MapColor.TERRACOTTA_GREEN).instrument(NoteBlockInstrument.BASS).strength(2.5f).sound(SoundType.WOOD).ignitedByLava(),
            ip -> ip.cookingFuel(ContextIntProviders.COOKING_TIME_WOOD_BLOCKS), NATURE_TAB);
    /** Urholzbretter (auch als Treppe und Stufe). */
    public static final BuildingSet ANCIENT_PLANKS = roofSet("ancient_planks", "ancient_plank",
            p -> p.mapColor(MapColor.TERRACOTTA_GREEN).instrument(NoteBlockInstrument.BASS).strength(2.0f, 3.0f).sound(SoundType.WOOD)
                    .ignitedByLava(),
            BuildingSet.Tool.AXE);
    /** Uraltes Laub: tiefgrün mit goldenen Blättchen. */
    public static final DeferredBlock<net.minecraft.world.level.block.UntintedParticleLeavesBlock> ANCIENT_LEAVES = blockWithItem("ancient_leaves",
            p -> new net.minecraft.world.level.block.UntintedParticleLeavesBlock(0.02f,
                    net.minecraft.core.particles.ColorParticleOption.create(net.minecraft.core.particles.ParticleTypes.TINTED_LEAVES, 0xFF2F6A32),
                    net.minecraft.world.level.block.sounds.AmbientLeavesBlockSoundPlayer.noAmbientSound(), p),
            p -> p.mapColor(MapColor.PLANT).strength(0.2f).randomTicks().sound(SoundType.AZALEA_LEAVES).noOcclusion()
                    .isValidSpawn((state, level, pos, type) -> false).isSuffocating((state, level, pos) -> false)
                    .isViewBlocking((state, level, pos, box) -> false).ignitedByLava().pushReaction(PushReaction.POPPED)
                    .isRedstoneConductor((state, level, pos) -> false),
            UnaryOperator.identity(), NATURE_TAB);
    /** Leuchtmoos: dichtes Moos, dessen Sporen im Dunkeln schimmern. */
    public static final DeferredBlock<Block> GLOWING_MOSS = blockWithItem("glowing_moss", Block::new,
            p -> p.mapColor(MapColor.COLOR_GREEN).strength(0.1f).sound(SoundType.MOSS).lightLevel(state -> 6),
            UnaryOperator.identity(), NATURE_TAB);
    /** Leuchtkappen: kleine Pilze, die türkis leuchten. */
    public static final DeferredBlock<io.github.sirschrolli.kingdomsatwar.block.GlowcapBlock> GLOWCAP = blockWithItem("glowcap",
            io.github.sirschrolli.kingdomsatwar.block.GlowcapBlock::new,
            p -> p.mapColor(MapColor.COLOR_CYAN).noCollision().instabreak().sound(SoundType.FUNGUS).lightLevel(state -> 11)
                    .offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.POPPED),
            ip -> ip.compostable(ContextIntProviders.COMPOSTABLE_LOW), NATURE_TAB);
    /** Dornengestrüpp: verschlungene Ranken voller Dornen – bremst und sticht. */
    public static final DeferredBlock<io.github.sirschrolli.kingdomsatwar.block.BrambleBlock> BRAMBLE = blockWithItem("bramble",
            io.github.sirschrolli.kingdomsatwar.block.BrambleBlock::new,
            p -> p.mapColor(MapColor.COLOR_BROWN).noCollision().strength(0.3f).sound(SoundType.SWEET_BERRY_BUSH)
                    .offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.POPPED).ignitedByLava(),
            UnaryOperator.identity(), NATURE_TAB);
    /** Knorrige Wurzeln: dick verschlungen, man sieht hindurch. */
    public static final DeferredBlock<Block> GNARLED_ROOTS = blockWithItem("gnarled_roots", Block::new,
            p -> p.mapColor(MapColor.PODZOL).instrument(NoteBlockInstrument.BASS).strength(1.2f).sound(SoundType.MANGROVE_ROOTS).noOcclusion()
                    .isSuffocating((state, level, pos) -> false).isViewBlocking((state, level, pos, box) -> false).ignitedByLava(),
            UnaryOperator.identity(), NATURE_TAB);

    // =====================================================================================
    // Zerklüftete Weiten: das Biom um Terragons Felsschlucht
    // =====================================================================================

    /** Bernsteinkristall: durchscheinend, leuchtet goldgelb. */
    public static final DeferredBlock<net.minecraft.world.level.block.HalfTransparentBlock> AMBER_CRYSTAL = blockWithItem("amber_crystal",
            net.minecraft.world.level.block.HalfTransparentBlock::new,
            p -> p.mapColor(MapColor.COLOR_ORANGE).strength(1.5f).sound(SoundType.AMETHYST).noOcclusion().lightLevel(s -> 9)
                    .isValidSpawn((state, level, pos, type) -> false).isRedstoneConductor((state, level, pos) -> false)
                    .isSuffocating((state, level, pos) -> false).isViewBlocking((state, level, pos, box) -> false),
            UnaryOperator.identity(), NATURE_TAB);
    /** Bernsteindruse: ein Grüppchen spitzer Kristalle. */
    public static final DeferredBlock<io.github.sirschrolli.kingdomsatwar.block.CrystalClusterBlock> AMBER_CLUSTER = blockWithItem("amber_cluster",
            p -> new io.github.sirschrolli.kingdomsatwar.block.CrystalClusterBlock(0xFFC65A, p),
            p -> p.mapColor(MapColor.COLOR_ORANGE).noCollision().noOcclusion().strength(0.5f).sound(SoundType.AMETHYST_CLUSTER)
                    .lightLevel(s -> 5).offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.POPPED),
            UnaryOperator.identity(), NATURE_TAB);
    /** Schiefer: dünn geschichteter, graubrauner Fels (auch als Treppe, Stufe und Mauer). */
    public static final BuildingSet SHALE = stoneSet("shale", "shale", MapColor.COLOR_GRAY, SoundType.DEEPSLATE, 1.6f);
    /** Geröll: grobe, lose Steinbrocken – rutscht wie Kies. */
    public static final DeferredBlock<net.minecraft.world.level.block.ColoredFallingBlock> RUBBLE = blockWithItem("rubble",
            p -> new net.minecraft.world.level.block.ColoredFallingBlock(new net.minecraft.util.ColorRGBA(0xFF6A6058), p),
            p -> p.mapColor(MapColor.STONE).instrument(NoteBlockInstrument.SNARE).strength(0.8f).sound(SoundType.GRAVEL),
            UnaryOperator.identity(), NATURE_TAB);
    /** Spaltgestein: dunkler Fels mit glimmenden Bernsteinrissen. */
    public static final DeferredBlock<Block> RIFT_STONE = blockWithItem("rift_stone", Block::new,
            p -> p.mapColor(MapColor.COLOR_BLACK).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0f, 6.0f)
                    .sound(SoundType.DEEPSLATE).lightLevel(s -> 5),
            UnaryOperator.identity(), NATURE_TAB);

    // =====================================================================================
    // Sturmklippen: das Biom um Voltarions Sturmgipfel
    // =====================================================================================

    /** Sturmstein: dunkler, bläulich schimmernder Fels (auch als Treppe, Stufe und Mauer). */
    public static final BuildingSet STORMSTONE = stoneSet("stormstone", "stormstone", MapColor.COLOR_BLUE, SoundType.DEEPSLATE, 2.0f);
    /** Geladener Sturmstein: mit gleißenden Blitzadern – knistert und teilt Schläge aus. */
    public static final DeferredBlock<io.github.sirschrolli.kingdomsatwar.block.ChargedStoneBlock> CHARGED_STORMSTONE = blockWithItem(
            "charged_stormstone", io.github.sirschrolli.kingdomsatwar.block.ChargedStoneBlock::new,
            p -> p.mapColor(MapColor.COLOR_LIGHT_BLUE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0f, 6.0f)
                    .sound(SoundType.DEEPSLATE).lightLevel(s -> 5),
            UnaryOperator.identity(), NATURE_TAB);
    /** Fulgurit: vom Blitz geschmolzener Sand, eine glasige Röhre. */
    public static final DeferredBlock<RotatedPillarBlock> FULGURITE = blockWithItem("fulgurite", RotatedPillarBlock::new,
            p -> p.mapColor(MapColor.SAND).instrument(NoteBlockInstrument.HAT).strength(1.0f).sound(SoundType.GLASS),
            UnaryOperator.identity(), NATURE_TAB);
    /** Sturmkristall: eisblau leuchtende Kristalle, die nach jedem Einschlag wachsen. */
    public static final DeferredBlock<io.github.sirschrolli.kingdomsatwar.block.CrystalClusterBlock> STORM_CRYSTAL = blockWithItem("storm_crystal",
            p -> new io.github.sirschrolli.kingdomsatwar.block.CrystalClusterBlock(0x5AC8F0, p),
            p -> p.mapColor(MapColor.COLOR_LIGHT_BLUE).noCollision().noOcclusion().strength(0.5f).sound(SoundType.AMETHYST_CLUSTER)
                    .lightLevel(s -> 7).offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.POPPED),
            UnaryOperator.identity(), NATURE_TAB);

    // =====================================================================================
    // Abgrundgrotte: das Biom um Thalassas Versunkene Grotte
    // =====================================================================================

    /** Abgrundstein: dunkler, blaugrüner Fels der Tiefsee mit Muschelsplittern (auch als Treppe, Stufe und Mauer). */
    public static final BuildingSet ABYSSAL_STONE = stoneSet("abyssal_stone", "abyssal_stone", MapColor.COLOR_CYAN, SoundType.DEEPSLATE, 1.8f);
    /** Abgrundsand: feiner, dunkler Tiefseesand. */
    public static final DeferredBlock<net.minecraft.world.level.block.ColoredFallingBlock> ABYSSAL_SAND = blockWithItem("abyssal_sand",
            p -> new net.minecraft.world.level.block.ColoredFallingBlock(new net.minecraft.util.ColorRGBA(0xFF3A4242), p),
            p -> p.mapColor(MapColor.COLOR_GRAY).instrument(NoteBlockInstrument.SNARE).strength(0.5f).sound(SoundType.SAND),
            UnaryOperator.identity(), NATURE_TAB);
    /** Leuchtkoralle: türkis leuchtend, im Wasser wie an der Luft. */
    public static final DeferredBlock<io.github.sirschrolli.kingdomsatwar.block.GlowCoralBlock> GLOW_CORAL = blockWithItem("glow_coral",
            io.github.sirschrolli.kingdomsatwar.block.GlowCoralBlock::new,
            p -> p.mapColor(MapColor.COLOR_CYAN).noCollision().noOcclusion().instabreak().sound(SoundType.WET_GRASS).lightLevel(s -> 10)
                    .pushReaction(PushReaction.POPPED),
            UnaryOperator.identity(), NATURE_TAB);
    /** Perlmutt: schimmernde Schichten in Weiß, Rosa und Hellblau. */
    public static final DeferredBlock<Block> NACRE_BLOCK = blockWithItem("nacre_block", Block::new,
            p -> p.mapColor(MapColor.QUARTZ).instrument(NoteBlockInstrument.CHIME).requiresCorrectToolForDrops().strength(1.5f, 6.0f)
                    .sound(SoundType.CALCITE),
            UnaryOperator.identity(), NATURE_TAB);

    // =====================================================================================
    // Siegelkreis: uralter Siegelstein, Monolithen, Runen, das Siegel und das Schattentor
    // =====================================================================================

    /** Uralter, dunkler Siegelstein (auch als Treppe, Stufe und Mauer). */
    public static final BuildingSet SEALSTONE = stoneSet("sealstone", "sealstone", MapColor.TERRACOTTA_CYAN, SoundType.DEEPSLATE, 2.0f);
    /** Ziegel aus Siegelstein. */
    public static final BuildingSet SEALSTONE_BRICKS = stoneSet("sealstone_bricks", "sealstone_brick", MapColor.TERRACOTTA_CYAN,
            SoundType.DEEPSLATE_BRICKS, 2.0f);
    public static final DeferredBlock<Block> CRACKED_SEALSTONE_BRICKS = sealstone("cracked_sealstone_bricks");
    public static final DeferredBlock<Block> MOSSY_SEALSTONE_BRICKS = sealstone("mossy_sealstone_bricks");
    public static final DeferredBlock<Block> POLISHED_SEALSTONE = sealstone("polished_sealstone");
    /** Gemeißelter Siegelstein mit dem Zeichen des Siegels. */
    public static final DeferredBlock<Block> CHISELED_SEALSTONE = sealstone("chiseled_sealstone");
    /** Stein der Monolithen am Siegelkreis (unzerstörbar). */
    public static final DeferredBlock<Block> SEAL_MONOLITH = BLOCKS.registerBlock("seal_monolith", Block::new,
            p -> p.mapColor(MapColor.TERRACOTTA_CYAN).instrument(NoteBlockInstrument.BASEDRUM).strength(-1.0f, 3600000.0f).noLootTable()
                    .sound(SoundType.DEEPSLATE_BRICKS));
    /** Rune eines Elements am Monolithen – leuchtet, sobald der Splitter eingesetzt ist (unzerstörbar). */
    public static final DeferredBlock<io.github.sirschrolli.kingdomsatwar.block.SealMarkBlock> SEAL_RUNE = BLOCKS.registerBlock("seal_rune",
            io.github.sirschrolli.kingdomsatwar.block.SealMarkBlock::new,
            p -> p.mapColor(MapColor.TERRACOTTA_CYAN).instrument(NoteBlockInstrument.BASEDRUM).strength(-1.0f, 3600000.0f).noLootTable()
                    .sound(SoundType.DEEPSLATE_BRICKS)
                    .lightLevel(s -> s.getValue(io.github.sirschrolli.kingdomsatwar.block.SealMarkBlock.LIT) ? 13 : 2));
    /** Eingelegtes Zeichen im Boden des Siegelkreises (unzerstörbar). */
    public static final DeferredBlock<io.github.sirschrolli.kingdomsatwar.block.SealMarkBlock> SEAL_INLAY = BLOCKS.registerBlock("seal_inlay",
            io.github.sirschrolli.kingdomsatwar.block.SealMarkBlock::new,
            p -> p.mapColor(MapColor.TERRACOTTA_CYAN).instrument(NoteBlockInstrument.BASEDRUM).strength(-1.0f, 3600000.0f).noLootTable()
                    .sound(SoundType.DEEPSLATE_BRICKS)
                    .lightLevel(s -> s.getValue(io.github.sirschrolli.kingdomsatwar.block.SealMarkBlock.LIT) ? 9 : 0));
    /** Siegelsockel: hier wird der Splitter eingesetzt (unzerstörbar). */
    public static final DeferredBlock<io.github.sirschrolli.kingdomsatwar.block.SealSocketBlock> SEAL_SOCKET = BLOCKS.registerBlock("seal_socket",
            io.github.sirschrolli.kingdomsatwar.block.SealSocketBlock::new,
            p -> p.mapColor(MapColor.TERRACOTTA_CYAN).instrument(NoteBlockInstrument.BASEDRUM).strength(-1.0f, 3600000.0f).noLootTable()
                    .sound(SoundType.DEEPSLATE_BRICKS)
                    .lightLevel(s -> s.getValue(io.github.sirschrolli.kingdomsatwar.block.SealSocketBlock.FILLED) ? 11 : 3));
    /** Eine der 25 Platten des Siegels (unzerstörbar). */
    public static final DeferredBlock<io.github.sirschrolli.kingdomsatwar.block.SealPlateBlock> SEAL_PLATE = BLOCKS.registerBlock("seal_plate",
            io.github.sirschrolli.kingdomsatwar.block.SealPlateBlock::new,
            p -> p.mapColor(MapColor.COLOR_BLACK).instrument(NoteBlockInstrument.BASEDRUM).strength(-1.0f, 3600000.0f).noLootTable()
                    .sound(SoundType.DEEPSLATE_TILES).lightLevel(s -> 4));
    /** Das Schattentor nach dem Siegelbruch. */
    public static final DeferredBlock<io.github.sirschrolli.kingdomsatwar.block.ShadowPortalBlock> SHADOW_PORTAL = BLOCKS.registerBlock("shadow_portal",
            io.github.sirschrolli.kingdomsatwar.block.ShadowPortalBlock::new,
            p -> p.mapColor(MapColor.COLOR_PURPLE).noCollision().strength(-1.0f, 3600000.0f).noLootTable().sound(SoundType.GLASS)
                    .lightLevel(s -> 11).pushReaction(PushReaction.IMMOVEABLE));

    public static final DeferredBlock<io.github.sirschrolli.kingdomsatwar.block.DragonLairCoreBlock> DRAGON_LAIR_CORE = BLOCKS.registerBlock(
            "dragon_lair_core", io.github.sirschrolli.kingdomsatwar.block.DragonLairCoreBlock::new,
            p -> p.mapColor(MapColor.COLOR_BLACK).strength(-1.0f, 3600000.0f).noLootTable().sound(SoundType.DEEPSLATE).lightLevel(s -> 6));

    /** Thronkern unter Nyxars Thron in der Schattenzitadelle (unzerstörbar, ohne Gegenstand). */
    public static final DeferredBlock<io.github.sirschrolli.kingdomsatwar.block.ShadowThroneCoreBlock> SHADOW_THRONE_CORE = BLOCKS.registerBlock(
            "shadow_throne_core", io.github.sirschrolli.kingdomsatwar.block.ShadowThroneCoreBlock::new,
            p -> p.mapColor(MapColor.COLOR_PURPLE).strength(-1.0f, 3600000.0f).noLootTable().sound(SoundType.DEEPSLATE).lightLevel(s -> 6));

    public static final DeferredBlock<RecruitmentTableBlock> RECRUITMENT_TABLE = blockWithItem("recruitment_table",
            RecruitmentTableBlock::new,
            p -> p.mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.5f).sound(SoundType.WOOD)
                    .noOcclusion().ignitedByLava(),
            ip -> ip.component(DataComponents.LORE, ModItems.lore("recruitment_table", 2)),
            KINGDOM_TAB);

    /** Stadtbanner: gründet die eigene Siedlung; Rechtsklick öffnet das Stadtfenster. */
    /** Magietisch: schreibt Zaubertafeln in den Aetherit-Stab ein; über ihm kreisen Runen, der Stab schwebt darüber. */
    public static final DeferredBlock<io.github.sirschrolli.kingdomsatwar.block.MagicTableBlock> MAGIC_TABLE = blockWithItem("magic_table",
            io.github.sirschrolli.kingdomsatwar.block.MagicTableBlock::new,
            p -> p.mapColor(MapColor.COLOR_PURPLE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops()
                    .strength(5.0f, 1200.0f).sound(SoundType.DEEPSLATE_BRICKS).lightLevel(state -> 7).noOcclusion(),
            ip -> ip.rarity(net.minecraft.world.item.Rarity.EPIC).component(DataComponents.LORE, ModItems.lore("magic_table", 2)),
            KINGDOM_TAB);

    public static final DeferredBlock<io.github.sirschrolli.kingdomsatwar.block.TownBannerBlock> TOWN_BANNER = blockWithItem("town_banner",
            io.github.sirschrolli.kingdomsatwar.block.TownBannerBlock::new,
            p -> p.mapColor(MapColor.COLOR_BLUE).instrument(NoteBlockInstrument.BASS).strength(2.0f, 1200.0f).sound(SoundType.WOOD)
                    .noOcclusion().pushReaction(net.minecraft.world.level.material.PushReaction.IMMOVEABLE),
            ip -> ip.stacksTo(16).rarity(net.minecraft.world.item.Rarity.UNCOMMON)
                    .component(DataComponents.LORE, ModItems.lore("town_banner", 3)),
            KINGDOM_TAB);

    private ModBlocks() {}

    /**
     * Alle Holzblöcke: Holzbalken, Fachwerk, Möbel und die Holzschindeln mit Treppe und Stufe.
     * Sie fangen Feuer und brennen ab ({@code ModDataMapProvider}). Türen und Fensterläden brennen wie
     * die Vanilla-Türen nicht.
     */
    public static List<DeferredBlock<? extends Block>> woodenBlocks() {
        List<DeferredBlock<? extends Block>> blocks = new ArrayList<>(
                List.of(TIMBER_BEAM, TIMBER_FRAME, TIMBER_FRAME_DIAGONAL, TIMBER_FRAME_CROSS, OAK_CHAIR, OAK_BENCH, OAK_TABLE, THRONE));
        for (BuildingSet set : BUILDING_SETS) {
            if (set.tool() == BuildingSet.Tool.AXE) {
                blocks.addAll(set.all());
            }
        }
        return blocks;
    }

    // =====================================================================================
    // Hilfsmethoden
    // =====================================================================================

    static <B extends Block> DeferredBlock<B> blockWithItem(String name,
            Function<BlockBehaviour.Properties, ? extends B> factory,
            Supplier<BlockBehaviour.Properties> properties,
            UnaryOperator<Item.Properties> itemProperties,
            List<Supplier<? extends ItemLike>> tab) {
        DeferredBlock<B> block = BLOCKS.registerBlock(name, factory, properties);
        ModItems.ITEMS.registerSimpleBlockItem(name, block, itemProperties);
        tab.add(block);
        return block;
    }

    static <B extends Block> DeferredBlock<B> blockWithItem(String name,
            Function<BlockBehaviour.Properties, ? extends B> factory,
            UnaryOperator<BlockBehaviour.Properties> properties,
            UnaryOperator<Item.Properties> itemProperties,
            List<Supplier<? extends ItemLike>> tab) {
        return blockWithItem(name, factory, () -> properties.apply(BlockBehaviour.Properties.of()), itemProperties, tab);
    }

    private static DeferredBlock<Block> sealstone(String name) {
        return blockWithItem(name, Block::new,
                p -> p.mapColor(MapColor.TERRACOTTA_CYAN).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops()
                        .strength(2.0f, 6.0f).sound(SoundType.DEEPSLATE_BRICKS),
                UnaryOperator.identity(), BUILDING_TAB);
    }

    private static BuildingSet stoneSet(String name, String variantPrefix, MapColor color, SoundType sound, float hardness) {
        return stoneSet(name, variantPrefix, color, sound, hardness, BUILDING_TAB);
    }

    static BuildingSet stoneSet(String name, String variantPrefix, MapColor color, SoundType sound, float hardness,
            List<Supplier<? extends ItemLike>> tab) {
        return buildingSet(name, variantPrefix,
                p -> p.mapColor(color).instrument(NoteBlockInstrument.BASEDRUM)
                        .requiresCorrectToolForDrops().strength(hardness, 6.0f).sound(sound),
                true, BuildingSet.Tool.PICKAXE, tab);
    }

    private static BuildingSet roofSet(String name, String variantPrefix,
            UnaryOperator<BlockBehaviour.Properties> properties, BuildingSet.Tool tool) {
        return buildingSet(name, variantPrefix, properties, false, tool);
    }

    private static BuildingSet buildingSet(String name, String variantPrefix,
            UnaryOperator<BlockBehaviour.Properties> properties, boolean withWall, BuildingSet.Tool tool) {
        return buildingSet(name, variantPrefix, properties, withWall, tool, BUILDING_TAB);
    }

    static BuildingSet buildingSet(String name, String variantPrefix,
            UnaryOperator<BlockBehaviour.Properties> properties, boolean withWall, BuildingSet.Tool tool,
            List<Supplier<? extends ItemLike>> tab) {
        // Holz-Sets (mit der Axt abzubauen) taugen wie Holzbretter als Brennstoff im Ofen.
        boolean wooden = tool == BuildingSet.Tool.AXE;
        UnaryOperator<Item.Properties> blockFuel = wooden
                ? ip -> ip.cookingFuel(ContextIntProviders.COOKING_TIME_WOOD_BLOCKS)
                : UnaryOperator.identity();
        UnaryOperator<Item.Properties> slabFuel = wooden
                ? ip -> ip.cookingFuel(ContextIntProviders.COOKING_TIME_WOOD_SLABS)
                : UnaryOperator.identity();
        DeferredBlock<Block> base = blockWithItem(name, Block::new, properties, blockFuel, tab);
        DeferredBlock<StairBlock> stairs = blockWithItem(variantPrefix + "_stairs",
                p -> new StairBlock(base.get().defaultBlockState(), p),
                () -> BlockBehaviour.Properties.ofFullCopy(base.get()),
                blockFuel, tab);
        DeferredBlock<SlabBlock> slab = blockWithItem(variantPrefix + "_slab", SlabBlock::new,
                () -> BlockBehaviour.Properties.ofFullCopy(base.get()),
                slabFuel, tab);
        DeferredBlock<WallBlock> wall = withWall
                ? blockWithItem(variantPrefix + "_wall", WallBlock::new,
                        () -> BlockBehaviour.Properties.ofFullCopy(base.get()).forceSolidOn(),
                        UnaryOperator.identity(), tab)
                : null;
        BuildingSet set = new BuildingSet(name, base, stairs, slab, wall, tool);
        BUILDING_SETS_MUTABLE.add(set);
        return set;
    }

    private static DeferredBlock<DoorBlock> door(String name) {
        DeferredBlock<DoorBlock> block = BLOCKS.registerBlock(name, p -> new DoorBlock(BlockSetType.DARK_OAK, p),
                p -> p.mapColor(MapColor.COLOR_BROWN).instrument(NoteBlockInstrument.BASS).strength(4.0f, 6.0f)
                        .noOcclusion().ignitedByLava().pushReaction(PushReaction.POPPED));
        // Wie bei Vanilla-Türen: ein Gegenstand, der beim Setzen auch die obere Hälfte freiräumt
        ModItems.ITEMS.registerItem(name, p -> new DoubleHighBlockItem(block.get(), p), Item.Properties::useBlockDescriptionPrefix);
        BUILDING_TAB.add(block);
        return block;
    }

    private static DeferredBlock<TrapDoorBlock> shutters(String name, MapColor color) {
        return blockWithItem(name, p -> new TrapDoorBlock(BlockSetType.SPRUCE, p),
                p -> p.mapColor(color).instrument(NoteBlockInstrument.BASS).strength(3.0f).noOcclusion()
                        .isValidSpawn((state, level, pos, type) -> false).ignitedByLava(),
                UnaryOperator.identity(), BUILDING_TAB);
    }

    private static DeferredBlock<LanternBlock> lantern(String name) {
        return blockWithItem(name, LanternBlock::new,
                p -> p.mapColor(MapColor.METAL).forceSolidOn().strength(3.5f).sound(SoundType.LANTERN)
                        .lightLevel(state -> 15).noOcclusion().pushReaction(PushReaction.POPPED),
                UnaryOperator.identity(), BUILDING_TAB);
    }

    private static DeferredBlock<BattlementBlock> battlement(String name, BuildingSet material) {
        return blockWithItem(name, BattlementBlock::new,
                () -> BlockBehaviour.Properties.ofFullCopy(material.base().get()),
                UnaryOperator.identity(), BUILDING_TAB);
    }

    private static DeferredBlock<SeatBlock> seat(String name, VoxelShape shape, double seatHeight, MapColor color) {
        return blockWithItem(name, p -> new SeatBlock(shape, seatHeight, p),
                p -> p.mapColor(color).instrument(NoteBlockInstrument.BASS).strength(2.0f, 3.0f).sound(SoundType.WOOD)
                        .noOcclusion().ignitedByLava(),
                ip -> ip.component(DataComponents.LORE, ModItems.lore("seat", 1)), BUILDING_TAB);
    }

    private static DeferredBlock<Block> timberFrame(String name) {
        return blockWithItem(name, Block::new,
                p -> p.mapColor(MapColor.TERRACOTTA_WHITE).instrument(NoteBlockInstrument.BASS)
                        .strength(1.5f, 3.0f).sound(SoundType.WOOD).ignitedByLava(),
                UnaryOperator.identity(), BUILDING_TAB);
    }

    private static DeferredBlock<DropExperienceBlock> stoneOre(String name, int minXp, int maxXp, int light) {
        return blockWithItem(name, p -> new DropExperienceBlock(UniformInt.of(minXp, maxXp), p),
                p -> p.mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM)
                        .requiresCorrectToolForDrops().strength(3.0f, 3.0f).lightLevel(state -> light),
                UnaryOperator.identity(), RESOURCE_TAB);
    }

    private static DeferredBlock<DropExperienceBlock> deepslateOre(String name, int minXp, int maxXp, int light) {
        return blockWithItem(name, p -> new DropExperienceBlock(UniformInt.of(minXp, maxXp), p),
                p -> p.mapColor(MapColor.DEEPSLATE).instrument(NoteBlockInstrument.BASEDRUM)
                        .requiresCorrectToolForDrops().strength(4.5f, 3.0f).sound(SoundType.DEEPSLATE)
                        .lightLevel(state -> light),
                UnaryOperator.identity(), RESOURCE_TAB);
    }

    private static DeferredBlock<Block> crystalBlock(String name, MapColor color, int light) {
        return blockWithItem(name, Block::new,
                p -> p.mapColor(color).requiresCorrectToolForDrops().strength(5.0f, 6.0f)
                        .sound(SoundType.AMETHYST).lightLevel(state -> light),
                UnaryOperator.identity(), RESOURCE_TAB);
    }

    private static DeferredBlock<ModCropBlock> crop(String name, Supplier<? extends ItemLike> seed) {
        return BLOCKS.registerBlock(name, p -> new ModCropBlock(seed, p),
                p -> p.mapColor(MapColor.PLANT).noCollision().randomTicks().instabreak()
                        .sound(SoundType.CROP).pushReaction(PushReaction.POPPED));
    }

    private static DeferredBlock<FlowerBlock> flower(String name, Holder<MobEffect> stewEffect, float seconds) {
        return flower(name, stewEffect, seconds, 0);
    }

    private static DeferredBlock<FlowerBlock> flower(String name, Holder<MobEffect> stewEffect, float seconds, int light) {
        return blockWithItem(name, p -> new FlowerBlock(stewEffect, seconds, p),
                p -> p.mapColor(MapColor.PLANT).noCollision().instabreak().sound(SoundType.GRASS)
                        .offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.POPPED).lightLevel(state -> light),
                ip -> ip.compostable(ContextIntProviders.COMPOSTABLE_MEDIUM), FARMING_TAB);
    }

    private static DeferredBlock<FlowerPotBlock> pottedFlower(String name, DeferredBlock<FlowerBlock> flower) {
        return pottedFlower(name, flower, 0);
    }

    private static DeferredBlock<FlowerPotBlock> pottedFlower(String name, DeferredBlock<FlowerBlock> flower, int light) {
        return BLOCKS.registerBlock(name,
                p -> new FlowerPotBlock(() -> (FlowerPotBlock) Blocks.FLOWER_POT, flower, p),
                p -> p.instabreak().noOcclusion().pushReaction(PushReaction.POPPED).lightLevel(state -> light));
    }
}
