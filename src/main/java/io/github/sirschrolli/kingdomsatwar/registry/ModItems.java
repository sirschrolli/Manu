package io.github.sirschrolli.kingdomsatwar.registry;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jspecify.annotations.Nullable;

/**
 * Alle Gegenstände der Mod, die keine Blöcke sind. Block-Gegenstände werden in {@link ModBlocks} registriert.
 */
public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(KingdomsAtWar.MODID);

    static final List<Supplier<? extends ItemLike>> RESOURCE_TAB = new ArrayList<>();
    static final List<Supplier<? extends ItemLike>> DRAGON_TAB = new ArrayList<>();
    static final List<Supplier<? extends ItemLike>> FARMING_TAB = new ArrayList<>();

    // =====================================================================================
    // Dragon Warfare – Erze & Rohstoffe
    // =====================================================================================

    /** Kohlenstoff: Brennstoff (wie Kohle) und Grundlage für Stahl. */
    public static final DeferredItem<Item> CARBON = item("carbon",
            p -> p.cookingFuel(ContextIntProviders.COOKING_TIME_COAL)
                    .component(DataComponents.LORE, lore("carbon", 1)), RESOURCE_TAB);
    public static final DeferredItem<Item> GEMSTONE = item("gemstone",
            p -> p.component(DataComponents.LORE, lore("gemstone", 1)), RESOURCE_TAB);
    public static final DeferredItem<Item> KONTERIT = item("konterit",
            p -> p.component(DataComponents.LORE, lore("konterit", 1)), RESOURCE_TAB);
    public static final DeferredItem<Item> AETHERIT = item("aetherit",
            p -> p.rarity(Rarity.UNCOMMON).component(DataComponents.LORE, lore("aetherit", 1)), RESOURCE_TAB);
    public static final DeferredItem<Item> DRACONIUM = item("draconium",
            p -> p.rarity(Rarity.RARE).fireResistant().component(DataComponents.LORE, lore("draconium", 1)), RESOURCE_TAB);
    public static final DeferredItem<Item> WYVERNIT = item("wyvernit",
            p -> p.rarity(Rarity.UNCOMMON).component(DataComponents.LORE, lore("wyvernit", 1)), RESOURCE_TAB);
    public static final DeferredItem<Item> STEEL_BLEND = item("steel_blend", UnaryOperator.identity(), RESOURCE_TAB);
    public static final DeferredItem<Item> STEEL_INGOT = item("steel_ingot",
            p -> p.component(DataComponents.LORE, lore("steel_ingot", 1)), RESOURCE_TAB);

    // =====================================================================================
    // Dragon Warfare – Drachen
    // =====================================================================================

    public static final DeferredItem<Item> DRAGON_EGG_FRAGMENT = item("dragon_egg_fragment",
            p -> p.rarity(Rarity.RARE).component(DataComponents.LORE, lore("dragon_egg_fragment", 1)), DRAGON_TAB);

    private static final Map<DragonType, DeferredItem<Item>> DRAGON_SCALES_MUTABLE = new EnumMap<>(DragonType.class);
    /** Eine Schuppensorte je Drachenart – Material für spätere Drachenrüstungen. */
    public static final Map<DragonType, DeferredItem<Item>> DRAGON_SCALES = Collections.unmodifiableMap(DRAGON_SCALES_MUTABLE);

    static {
        for (DragonType type : DragonType.values()) {
            DRAGON_SCALES_MUTABLE.put(type, item(type.id() + "_dragon_scale",
                    p -> p.rarity(Rarity.UNCOMMON).component(DataComponents.LORE, lore("dragon_scale", 1)), DRAGON_TAB));
        }
    }

    /** Drachenfutter – heilt Drachen und lässt sie schneller wachsen. */
    public static final DeferredItem<Item> DRAGON_FOOD = item("dragon_food",
            p -> p.component(DataComponents.LORE, lore("dragon_food", 1)), DRAGON_TAB);

    private static final Map<DragonType, DeferredItem<SpawnEggItem>> DRAGON_SPAWN_EGGS_MUTABLE = new EnumMap<>(DragonType.class);
    /** Spawn-Eier (Kreativmodus) für einen Schlüpfling jeder Drachenart. */
    public static final Map<DragonType, DeferredItem<SpawnEggItem>> DRAGON_SPAWN_EGGS = Collections.unmodifiableMap(DRAGON_SPAWN_EGGS_MUTABLE);

    static {
        for (DragonType type : DragonType.values()) {
            DRAGON_SPAWN_EGGS_MUTABLE.put(type, addTo(DRAGON_TAB, ITEMS.registerItem(type.id() + "_dragon_spawn_egg", SpawnEggItem::new,
                    p -> p.spawnEgg(ModEntities.DRAGONS.get(type).get()))));
        }
    }

    // =====================================================================================
    // Kingdoms at War – Landwirtschaft & Nahrung
    // =====================================================================================

    // 1. Neue Feldfrüchte und Nutzpflanzen
    public static final DeferredItem<BlockItem> TOMATO_SEEDS = seeds("tomato_seeds", () -> ModBlocks.TOMATOES.get());
    public static final DeferredItem<Item> TOMATO = food("tomato", 3, 0.4f, ContextIntProviders.COMPOSTABLE_MEDIUM);
    public static final DeferredItem<BlockItem> CABBAGE_SEEDS = seeds("cabbage_seeds", () -> ModBlocks.CABBAGES.get());
    public static final DeferredItem<Item> CABBAGE = food("cabbage", 2, 0.4f, ContextIntProviders.COMPOSTABLE_MEDIUM);
    /** Zwiebeln werden direkt gepflanzt, wie Karotten. */
    public static final DeferredItem<BlockItem> ONION = addTo(FARMING_TAB, ITEMS.registerItem("onion",
            p -> new BlockItem(ModBlocks.ONIONS.get(), p),
            () -> new Item.Properties().useItemDescriptionPrefix()
                    .food(new FoodProperties.Builder().nutrition(2).saturationModifier(0.3f).build())
                    .compostable(ContextIntProviders.COMPOSTABLE_MEDIUM)));
    public static final DeferredItem<BlockItem> BARLEY_SEEDS = seeds("barley_seeds", () -> ModBlocks.BARLEY.get());
    public static final DeferredItem<Item> BARLEY = item("barley",
            p -> p.compostable(ContextIntProviders.COMPOSTABLE_MEDIUM), FARMING_TAB);

    // 2. Neue Lebensmittel und Gerichte
    public static final DeferredItem<Item> BREAD_ROLL = food("bread_roll", 4, 0.5f, null);
    public static final DeferredItem<Item> VEGETABLE_STEW = item("vegetable_stew",
            p -> p.food(new FoodProperties.Builder().nutrition(9).saturationModifier(0.7f).build())
                    .usingConvertsTo(Items.BOWL).stacksTo(1), FARMING_TAB);
    public static final DeferredItem<Item> MEAT_PIE = food("meat_pie", 8, 0.8f, null);
    public static final DeferredItem<Item> APPLE_PIE = food("apple_pie", 7, 0.6f, null);

    private ModItems() {}

    // =====================================================================================
    // Hilfsmethoden
    // =====================================================================================

    private static <T extends Supplier<? extends ItemLike>> T addTo(List<Supplier<? extends ItemLike>> tab, T entry) {
        tab.add(entry);
        return entry;
    }

    private static DeferredItem<Item> item(String name, UnaryOperator<Item.Properties> properties,
            List<Supplier<? extends ItemLike>> tab) {
        return addTo(tab, ITEMS.registerSimpleItem(name, properties));
    }

    private static DeferredItem<Item> food(String name, int nutrition, float saturation,
            @Nullable ResourceKey<ContextIntProvider> compostChance) {
        FoodProperties food = new FoodProperties.Builder().nutrition(nutrition).saturationModifier(saturation).build();
        return item(name, p -> compostChance == null ? p.food(food) : p.food(food).compostable(compostChance), FARMING_TAB);
    }

    private static DeferredItem<BlockItem> seeds(String name, Supplier<? extends Block> crop) {
        return addTo(FARMING_TAB, ITEMS.registerItem(name,
                p -> new BlockItem(crop.get(), p),
                () -> new Item.Properties().useItemDescriptionPrefix().compostable(ContextIntProviders.COMPOSTABLE_LOW)));
    }

    /**
     * Erzeugt eine graue Beschreibung (Lore) aus den Übersetzungsschlüsseln
     * {@code lore.kingdomsatwar.<name>.1} bis {@code .<lines>}.
     */
    public static ItemLore lore(String name, int lines) {
        List<Component> components = new ArrayList<>();
        for (int i = 1; i <= lines; i++) {
            components.add(Component.translatable(loreKey(name, i))
                    .withStyle(style -> style.withColor(0xA0A0A0).withItalic(false)));
        }
        return new ItemLore(components);
    }

    public static String loreKey(String name, int line) {
        return "lore." + KingdomsAtWar.MODID + "." + name + "." + line;
    }
}
