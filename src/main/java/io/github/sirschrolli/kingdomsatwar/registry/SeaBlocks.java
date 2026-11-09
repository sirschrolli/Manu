package io.github.sirschrolli.kingdomsatwar.registry;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.sea.AtollCoreBlock;
import io.github.sirschrolli.kingdomsatwar.block.sea.AtollCoreBlockEntity;
import io.github.sirschrolli.kingdomsatwar.block.sea.EmbeddedSpearBlock;
import io.github.sirschrolli.kingdomsatwar.block.sea.GiantKelpBlock;
import io.github.sirschrolli.kingdomsatwar.block.sea.GiantKelpPlantBlock;
import io.github.sirschrolli.kingdomsatwar.block.sea.GlowAlgaeBlock;
import io.github.sirschrolli.kingdomsatwar.block.sea.HydrothermalVentBlock;
import io.github.sirschrolli.kingdomsatwar.block.sea.SeaAnemoneBlock;
import io.github.sirschrolli.kingdomsatwar.block.sea.SeashellBlock;
import io.github.sirschrolli.kingdomsatwar.block.sea.TubeWormsBlock;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import io.github.sirschrolli.kingdomsatwar.item.sea.TideHornItem;
import io.github.sirschrolli.kingdomsatwar.item.sea.TideSpearItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * Was das Meer schenkt: die Beute der Meereswesen, die Tiefseelaterne, der Eimer mit Leuchtqualle und die Spawn-Eier –
 * alles im Kreativ-Tab „Das Meer“.
 */
public final class SeaBlocks {
    private SeaBlocks() {}

    /** Inhalt des Kreativ-Tabs „Das Meer“ (in Reihenfolge). */
    static final List<Supplier<? extends ItemLike>> TAB = new ArrayList<>();

    /** Lädt die Klasse, damit sich alles anmeldet, bevor die Register gefüllt werden. */
    public static void init() {}

    public static List<Supplier<? extends ItemLike>> tab() {
        return java.util.Collections.unmodifiableList(TAB);
    }

    // =====================================================================================
    // Blöcke
    // =====================================================================================

    /** Tiefseelaterne: ein Gehäuse aus Prismarin und Eisen, in dem der Leuchtköder eines Anglerfischs weiterleuchtet. */
    public static final DeferredBlock<LanternBlock> DEEP_SEA_LANTERN = ModBlocks.blockWithItem("deep_sea_lantern", LanternBlock::new,
            p -> p.mapColor(MapColor.COLOR_CYAN).forceSolidOn().strength(3.5f).sound(SoundType.LANTERN).lightLevel(s -> 15).noOcclusion()
                    .pushReaction(PushReaction.POPPED),
            UnaryOperator.identity(), TAB);

    // ---------------------------------------------------------------- Meeresgrund (0.62)
    /** Seeanemonen in vier Farben: wiegende Fangarme mit leuchtenden Spitzen, nur unter Wasser. */
    public static final DeferredBlock<SeaAnemoneBlock> PURPLE_SEA_ANEMONE = anemone("purple_sea_anemone", MapColor.COLOR_PURPLE);
    public static final DeferredBlock<SeaAnemoneBlock> RED_SEA_ANEMONE = anemone("red_sea_anemone", MapColor.COLOR_RED);
    public static final DeferredBlock<SeaAnemoneBlock> GREEN_SEA_ANEMONE = anemone("green_sea_anemone", MapColor.COLOR_GREEN);
    public static final DeferredBlock<SeaAnemoneBlock> ORANGE_SEA_ANEMONE = anemone("orange_sea_anemone", MapColor.COLOR_ORANGE);
    /** Muscheln: Herzmuschel, Kammmuschel und Wellhornschnecke – ein bis drei Schalen am Strand oder am Grund. */
    public static final DeferredBlock<SeashellBlock> COCKLE_SHELLS = shells("cockle_shells", MapColor.SAND);
    public static final DeferredBlock<SeashellBlock> SCALLOP_SHELLS = shells("scallop_shells", MapColor.COLOR_ORANGE);
    public static final DeferredBlock<SeashellBlock> WHELK_SHELLS = shells("whelk_shells", MapColor.TERRACOTTA_BROWN);
    /** Röhrenwürmer an den heißen Quellen der Tiefsee. */
    public static final DeferredBlock<TubeWormsBlock> TUBE_WORMS = ModBlocks.blockWithItem("tube_worms", TubeWormsBlock::new,
            p -> p.mapColor(MapColor.COLOR_RED).noCollision().instabreak().sound(SoundType.WET_GRASS).pushReaction(PushReaction.POPPED)
                    .offsetType(net.minecraft.world.level.block.state.BlockBehaviour.OffsetType.XZ),
            UnaryOperator.identity(), TAB);
    /** Schlotgestein: dunkles, poröses Gestein der heißen Quellen. */
    public static final DeferredBlock<net.minecraft.world.level.block.Block> VENT_ROCK = ModBlocks.blockWithItem("vent_rock",
            net.minecraft.world.level.block.Block::new,
            p -> p.mapColor(MapColor.COLOR_BLACK).requiresCorrectToolForDrops().strength(1.5f, 6.0f).sound(SoundType.BASALT),
            UnaryOperator.identity(), TAB);
    /** Schwefelkruste: gelbe und weiße Krusten, die sich um die Schlote ablagern. */
    public static final DeferredBlock<net.minecraft.world.level.block.Block> SULFUR_CRUST = ModBlocks.blockWithItem("sulfur_crust",
            net.minecraft.world.level.block.Block::new,
            p -> p.mapColor(MapColor.COLOR_YELLOW).requiresCorrectToolForDrops().strength(1.0f, 3.0f).sound(SoundType.CALCITE),
            UnaryOperator.identity(), TAB);
    /** Heißer Schlot: glüht, raucht und trägt eine Blasensäule nach oben. */
    public static final DeferredBlock<HydrothermalVentBlock> HYDROTHERMAL_VENT = ModBlocks.blockWithItem("hydrothermal_vent",
            HydrothermalVentBlock::new,
            p -> p.mapColor(MapColor.NETHER).requiresCorrectToolForDrops().strength(2.0f, 6.0f).sound(SoundType.BASALT).lightLevel(s -> 12)
                    .isValidSpawn((state, level, pos, type) -> false),
            UnaryOperator.identity(), TAB);
    /** Leuchtalgen: ein leuchtender Belag am Grund der Tiefsee. */
    public static final DeferredBlock<GlowAlgaeBlock> GLOW_ALGAE = ModBlocks.blockWithItem("glow_algae", GlowAlgaeBlock::new,
            p -> p.mapColor(MapColor.COLOR_CYAN).replaceable().noCollision().instabreak().sound(SoundType.MOSS_CARPET).lightLevel(s -> 7)
                    .pushReaction(PushReaction.POPPED),
            UnaryOperator.identity(), TAB);
    /** Riesentang: goldbraune Tangwälder bis zur Oberfläche (Spitze mit Gegenstand, Stängel ohne). */
    public static final DeferredBlock<GiantKelpBlock> GIANT_KELP = ModBlocks.blockWithItem("giant_kelp", GiantKelpBlock::new,
            p -> p.mapColor(MapColor.WATER).replaceable().noCollision().randomTicks().instabreak().sound(SoundType.WET_GRASS)
                    .pushReaction(PushReaction.POPPED),
            UnaryOperator.identity(), TAB);
    public static final DeferredBlock<GiantKelpPlantBlock> GIANT_KELP_PLANT = ModBlocks.BLOCKS.registerBlock("giant_kelp_plant",
            GiantKelpPlantBlock::new, () -> net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(MapColor.WATER)
                    .replaceable().noCollision().instabreak().sound(SoundType.WET_GRASS).pushReaction(PushReaction.POPPED));

    // ---------------------------------------------------------------- das Versunkene Atoll (0.62)
    /** Herz des Atolls: verborgen unter der Lagune; weckt Karkinos und merkt sich seinen Sieg. Unzerstörbar. */
    public static final DeferredBlock<AtollCoreBlock> ATOLL_CORE = ModBlocks.BLOCKS.registerBlock("atoll_core", AtollCoreBlock::new,
            p -> p.mapColor(MapColor.COLOR_CYAN).strength(-1.0f, 3_600_000.0f).noLootTable().sound(SoundType.STONE).lightLevel(s -> 9)
                    .pushReaction(PushReaction.IMMOVEABLE));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AtollCoreBlockEntity>> ATOLL_CORE_ENTITY =
            ModBlockEntities.BLOCK_ENTITIES.register("atoll_core", () -> new BlockEntityType<>(AtollCoreBlockEntity::new, ATOLL_CORE.get()));
    /** Der Gezeiten-Speer, wie er aus dem Hügel in der Lagune ragt (nur Zierde: wer ihn greifen will, weckt den Koloss). */
    public static final DeferredBlock<EmbeddedSpearBlock> EMBEDDED_TIDE_SPEAR = ModBlocks.BLOCKS.registerBlock("embedded_tide_spear",
            EmbeddedSpearBlock::new, p -> p.mapColor(MapColor.COLOR_CYAN).strength(-1.0f, 3_600_000.0f).noLootTable().noOcclusion()
                    .sound(SoundType.AMETHYST).lightLevel(s -> 10).pushReaction(PushReaction.IMMOVEABLE));

    public static List<DeferredBlock<SeaAnemoneBlock>> anemones() {
        return List.of(PURPLE_SEA_ANEMONE, RED_SEA_ANEMONE, GREEN_SEA_ANEMONE, ORANGE_SEA_ANEMONE);
    }

    public static List<DeferredBlock<SeashellBlock>> shells() {
        return List.of(COCKLE_SHELLS, SCALLOP_SHELLS, WHELK_SHELLS);
    }

    private static DeferredBlock<SeaAnemoneBlock> anemone(String name, MapColor color) {
        return ModBlocks.blockWithItem(name, SeaAnemoneBlock::new,
                p -> p.mapColor(color).noCollision().instabreak().sound(SoundType.WET_GRASS).lightLevel(s -> 6).pushReaction(PushReaction.POPPED)
                        .offsetType(net.minecraft.world.level.block.state.BlockBehaviour.OffsetType.XZ),
                UnaryOperator.identity(), TAB);
    }

    private static DeferredBlock<SeashellBlock> shells(String name, MapColor color) {
        return ModBlocks.blockWithItem(name, SeashellBlock::new,
                p -> p.mapColor(color).noCollision().strength(0.2f).sound(SoundType.BONE_BLOCK).pushReaction(PushReaction.POPPED),
                UnaryOperator.identity(), TAB);
    }

    // =====================================================================================
    // Gegenstände
    // =====================================================================================

    /** Leuchtgelee: von der Leuchtqualle – mit einem seltsamen Trank wird daraus ein Trank der Nachtsicht. */
    public static final DeferredItem<Item> GLOW_JELLY = item("glow_jelly", p -> p.component(DataComponents.LORE, ModItems.lore("glow_jelly", 1)));
    /** Leuchtköder: der Köder eines Anglerfischs, der auch nach seinem Tod weiterleuchtet. */
    public static final DeferredItem<Item> ANGLER_LURE = item("angler_lure", p -> p.rarity(Rarity.UNCOMMON)
            .component(DataComponents.LORE, ModItems.lore("angler_lure", 1)));
    /** Haizahn: dreieckig und scharf – mit Stock und Feder ergibt er sechs Pfeile. */
    public static final DeferredItem<Item> SHARK_TOOTH = item("shark_tooth", p -> p.component(DataComponents.LORE, ModItems.lore("shark_tooth", 1)));
    /** Krabbenschere: in der Zweithand greift man anderthalb Blöcke weiter (und einen Block weiter nach Wesen). */
    public static final DeferredItem<Item> CRAB_CLAW = item("crab_claw", p -> p.stacksTo(1).rarity(Rarity.UNCOMMON)
            .component(DataComponents.LORE, ModItems.lore("crab_claw", 1))
            .attributes(ItemAttributeModifiers.builder()
                    .add(Attributes.BLOCK_INTERACTION_RANGE, new AttributeModifier(KingdomsAtWar.id("crab_claw_block_reach"), 1.5,
                            AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.OFFHAND)
                    .add(Attributes.ENTITY_INTERACTION_RANGE, new AttributeModifier(KingdomsAtWar.id("crab_claw_entity_reach"), 1.0,
                            AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.OFFHAND)
                    .build()));
    public static final DeferredItem<Item> CRAB_MEAT = food("crab_meat", 2, 0.3f);
    public static final DeferredItem<Item> COOKED_CRAB_MEAT = food("cooked_crab_meat", 6, 0.8f);
    /** Eimer mit Leuchtqualle: setzt sie wieder ins Wasser (behält ihre Farbe). */
    public static final DeferredItem<MobBucketItem> GLOW_JELLYFISH_BUCKET = add(ModItems.ITEMS.registerItem("glow_jellyfish_bucket",
            p -> new MobBucketItem(ModEntities.GLOW_JELLYFISH.get(), Fluids.WATER, SoundEvents.BUCKET_EMPTY_FISH, p),
            () -> new Item.Properties().stacksTo(1).component(DataComponents.BUCKET_ENTITY_DATA, CustomData.EMPTY)));

    // ---------------------------------------------------------------- Beute des Riffkolosses
    /** Werkstoff des Gezeiten-Speers: zwischen Diamant und Netherit, mit Prismarinkristallen zu reparieren. */
    public static final ToolMaterial TIDE_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 2031, 8.5f, 3.5f, 18,
            ModTags.Items.REPAIRS_TIDE_SPEAR);
    /** Der Gezeiten-Speer: legendäre Waffe des Gezeitenkönigs (Stoß und Ansturm wie ein Speer, stärker im Wasser und Regen). */
    public static final DeferredItem<TideSpearItem> TIDE_SPEAR = add(ModItems.ITEMS.registerItem("tide_spear", TideSpearItem::new,
            () -> new Item.Properties().spear(TIDE_MATERIAL, 1.05f, 1.15f, 0.45f, 2.75f, 9.5f, 6.0f, 5.1f, 9.4f, 4.6f).rarity(Rarity.EPIC)
                    .fireResistant().component(DataComponents.LORE, ModItems.lore("tide_spear", 3))));
    /** Das Gezeitenhorn: zeigt den Weg zum Versunkenen Atoll und ruft den besiegten Koloss zurück. */
    public static final DeferredItem<TideHornItem> TIDE_HORN = add(ModItems.ITEMS.registerItem("tide_horn", TideHornItem::new,
            () -> new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON).component(DataComponents.LORE, ModItems.lore("tide_horn", 2))));

    // ---------------------------------------------------------------- Spawn-Eier der Meereswesen
    public static final DeferredItem<SpawnEggItem> MANTA_RAY_SPAWN_EGG = egg("manta_ray", () -> ModEntities.MANTA_RAY.get());
    public static final DeferredItem<SpawnEggItem> GLOW_JELLYFISH_SPAWN_EGG = egg("glow_jellyfish", () -> ModEntities.GLOW_JELLYFISH.get());
    public static final DeferredItem<SpawnEggItem> SHARK_SPAWN_EGG = egg("shark", () -> ModEntities.SHARK.get());
    public static final DeferredItem<SpawnEggItem> ANGLERFISH_SPAWN_EGG = egg("anglerfish", () -> ModEntities.ANGLERFISH.get());
    public static final DeferredItem<SpawnEggItem> GIANT_CRAB_SPAWN_EGG = egg("giant_crab", () -> ModEntities.GIANT_CRAB.get());
    public static final DeferredItem<SpawnEggItem> REEF_COLOSSUS_SPAWN_EGG = egg("reef_colossus", () -> ModEntities.REEF_COLOSSUS.get());

    public static List<DeferredItem<SpawnEggItem>> spawnEggs() {
        return List.of(MANTA_RAY_SPAWN_EGG, GLOW_JELLYFISH_SPAWN_EGG, SHARK_SPAWN_EGG, ANGLERFISH_SPAWN_EGG, GIANT_CRAB_SPAWN_EGG,
                REEF_COLOSSUS_SPAWN_EGG);
    }

    /** Alle schlichten Gegenstände (flaches Modell). */
    public static List<DeferredItem<? extends Item>> flatItems() {
        return List.of(GLOW_JELLY, ANGLER_LURE, SHARK_TOOTH, CRAB_CLAW, CRAB_MEAT, COOKED_CRAB_MEAT, GLOW_JELLYFISH_BUCKET, TIDE_HORN);
    }

    private static DeferredItem<SpawnEggItem> egg(String name, Supplier<EntityType<? extends Mob>> type) {
        return add(ModItems.ITEMS.registerItem(name + "_spawn_egg", SpawnEggItem::new, p -> p.spawnEgg(type.get())));
    }

    private static DeferredItem<Item> item(String name, UnaryOperator<Item.Properties> properties) {
        return add(ModItems.ITEMS.registerSimpleItem(name, properties));
    }

    private static DeferredItem<Item> food(String name, int nutrition, float saturation) {
        FoodProperties food = new FoodProperties.Builder().nutrition(nutrition).saturationModifier(saturation).build();
        return item(name, p -> p.food(food));
    }

    private static <T extends Item> DeferredItem<T> add(DeferredItem<T> item) {
        TAB.add(item);
        return item;
    }
}
