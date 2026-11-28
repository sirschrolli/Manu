package io.github.sirschrolli.kingdomsatwar.registry;

import io.github.sirschrolli.kingdomsatwar.block.CheeseWheelBlock;
import io.github.sirschrolli.kingdomsatwar.block.EmberPepperBlock;
import java.util.List;
import java.util.function.UnaryOperator;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import org.jspecify.annotations.Nullable;

/**
 * Die Küche der Welten (seit 1.16): Speisen und Getränke aus allen Dimensionen – aus der Sennhütte Bergkäse, Käsespätzle,
 * Brezeln und Bratwurst, aus Schmugglerhöhle und Galeone der Rum, aus der Unterwelt die Glutschote und was man aus ihr
 * kocht, aus dem End Schiffszwieback und Chorusmarmelade, aus der Schattenwelt Dämmerbrei und Seelenbeerkuchen, aus der
 * Leere Sternenkandis, aus dem Abgrund Sturzkuchen und Pilgerration, aus dem Urmeer Fischsuppe und Fischbrötchen, aus dem
 * Feenreich Lebkuchen, Feentee und Glimmerküchlein. Man findet sie in den Bauwerken der jeweiligen Welt und kocht sie nach
 * (Rezepte in {@code ModRecipeProvider}); alle stehen im Kreativ-Tab der Landwirtschaft.
 */
public final class Cuisine {
    private Cuisine() {}

    /** Lädt die Klasse, damit sich alles anmeldet, bevor die Register gefüllt werden. */
    public static void init() {}

    // ---------------------------------------------------------------- Zutaten

    /** Mehl: gemahlener Weizen – die Grundlage für Brezel, Spätzle, Zwieback und Gebäck. */
    public static final DeferredItem<Item> FLOUR = item("flour", p -> p.compostable(ContextIntProviders.COMPOSTABLE_MEDIUM));

    // ---------------------------------------------------------------- Oberwelt (Sennhütte)

    /** Ein Stück Bergkäse. */
    public static final DeferredItem<Item> CHEESE = food("cheese", 3, 0.6f, null);
    /** Der ganze Laib zum Aufstellen: vier Stücke direkt vom Block. */
    public static final DeferredBlock<CheeseWheelBlock> CHEESE_WHEEL = block("cheese_wheel");
    /** Käsespätzle: Spätzle mit geschmolzenem Bergkäse und Röstzwiebeln – das Leibgericht der Sennen. */
    public static final DeferredItem<Item> CHEESE_SPAETZLE = bowl("cheese_spaetzle", 10, 0.8f, null);
    public static final DeferredItem<Item> PRETZEL = food("pretzel", 5, 0.6f, null);
    public static final DeferredItem<Item> RAW_SAUSAGE = food("raw_sausage", 2, 0.3f, null);
    public static final DeferredItem<Item> SAUSAGE = food("sausage", 7, 0.8f, null);

    // ---------------------------------------------------------------- Schmugglerhöhle und Galeone

    /** Rum aus Zuckerrohr: macht mutig und zäh – aber wer zu viel trinkt, dem dreht sich die Welt. */
    public static final DeferredItem<Item> RUM = drink("rum", 2, 0.3f, 0.4f, new MobEffectInstance(MobEffects.RESISTANCE, 900, 0));

    // ---------------------------------------------------------------- Unterwelt (Schreiterhof)

    /** Die Pflanze der Glutschote (wächst auf Seelenerde und Seelensand). */
    public static final DeferredBlock<EmberPepperBlock> EMBER_PEPPERS = ModBlocks.BLOCKS.registerBlock("ember_peppers",
            p -> new EmberPepperBlock(() -> Cuisine.EMBER_PEPPER.get(), p),
            p -> p.mapColor(MapColor.COLOR_RED).noCollision().randomTicks().instabreak().sound(SoundType.NETHER_WART)
                    .lightLevel(state -> state.getValue(EmberPepperBlock.AGE) >= 3 ? 5 : 0).pushReaction(PushReaction.POPPED));
    /** Glutschote: brennend scharf – kurz Feuerresistenz; auf Seelenerde gepflanzt, wächst ein neuer Strauch. */
    public static final DeferredItem<BlockItem> EMBER_PEPPER = addTo(ModItems.ITEMS.registerItem("ember_pepper",
            p -> new BlockItem(EMBER_PEPPERS.get(), p),
            () -> new Item.Properties().useItemDescriptionPrefix()
                    .food(foodValues(1, 0.2f, false), eat(1.0f, new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 200, 0)))
                    .compostable(ContextIntProviders.COMPOSTABLE_MEDIUM)
                    .component(DataComponents.LORE, ModItems.lore("ember_pepper", 1))));
    /** Glutchili: ein Eintopf aus Glutschoten, Fleisch und Tomaten – eine Minute lang macht einem Lava nichts aus. */
    public static final DeferredItem<Item> EMBER_CHILI = bowl("ember_chili", 8, 0.7f, new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 1200, 0));
    /** Gerösteter Pilzspieß: Karmesin- und Wirrpilz am Stock, über Lohenpulver geröstet. */
    public static final DeferredItem<Item> FUNGUS_SKEWER = item("fungus_skewer",
            p -> p.food(foodValues(6, 0.6f, false)).usingConvertsTo(Items.STICK));

    // ---------------------------------------------------------------- End (Luftschiffwrack, Chorusgarten)

    /** Schiffszwieback: zweimal gebacken, hart wie Holz, hält ewig – und sättigt lange. */
    public static final DeferredItem<Item> HARDTACK = food("hardtack", 3, 1.0f, null);
    /** Chorusmarmelade: süß und leicht – danach fällt man eine Weile sanft wie eine Feder. */
    public static final DeferredItem<Item> CHORUS_JAM = item("chorus_jam",
            p -> p.food(foodValues(4, 0.4f, true), eat(1.6f, new MobEffectInstance(MobEffects.SLOW_FALLING, 400, 0)))
                    .usingConvertsTo(Items.GLASS_BOTTLE).stacksTo(16).component(DataComponents.LORE, ModItems.lore("chorus_jam", 1)));

    // ---------------------------------------------------------------- Schattenwelt (Inselkloster, Nachtkarawane)

    /** Dämmerbrei: Dämmerkorn in Milch mit Seelenbeeren – die Morgenspeise der Mönche, man sieht danach im Dunkeln. */
    public static final DeferredItem<Item> DUSK_PORRIDGE = bowl("dusk_porridge", 8, 0.8f, new MobEffectInstance(MobEffects.NIGHT_VISION, 1800, 0));
    public static final DeferredItem<Item> SOUL_BERRY_PIE = item("soul_berry_pie",
            p -> p.food(foodValues(8, 0.6f, false), eat(1.6f, new MobEffectInstance(MobEffects.NIGHT_VISION, 900, 0)))
                    .component(DataComponents.LORE, ModItems.lore("soul_berry_pie", 1)));

    // ---------------------------------------------------------------- Leere (Leuchtfeuer)

    /** Sternenkandis: knistert auf der Zunge, und für eine halbe Minute ist man flink wie ein Stern. */
    public static final DeferredItem<Item> STAR_CANDY = item("star_candy",
            p -> p.food(foodValues(2, 0.2f, true), eat(0.8f, new MobEffectInstance(MobEffects.SPEED, 600, 0)))
                    .rarity(Rarity.UNCOMMON).component(DataComponents.LORE, ModItems.lore("star_candy", 1)));

    // ---------------------------------------------------------------- Abgrund (Verkehrtes Haus, Kettenfeste)

    /** Sturzkuchen: ein gestürzter Apfelkuchen – wer ihn isst, fällt danach ein wenig langsamer. */
    public static final DeferredItem<Item> UPSIDE_DOWN_CAKE = item("upside_down_cake",
            p -> p.food(foodValues(7, 0.6f, false), eat(1.6f, new MobEffectInstance(MobEffects.SLOW_FALLING, 200, 0)))
                    .component(DataComponents.LORE, ModItems.lore("upside_down_cake", 1)));
    /** Pilgerration: Dörrfleisch, Beeren und Brot in Tang gewickelt – kaut man lange, sättigt aber wie kaum etwas. */
    public static final DeferredItem<Item> PILGRIM_RATION = item("pilgrim_ration",
            p -> p.food(foodValues(9, 1.0f, false), Consumables.defaultFood().consumeSeconds(2.4f).build()));

    // ---------------------------------------------------------------- Urmeer (Fischerinsel)

    /** Fischsuppe: wärmt bis in die Zehen – und man hält danach länger die Luft an. */
    public static final DeferredItem<Item> FISH_SOUP = bowl("fish_soup", 9, 0.8f, new MobEffectInstance(MobEffects.WATER_BREATHING, 600, 0));
    public static final DeferredItem<Item> FISH_ROLL = food("fish_roll", 7, 0.7f, null);

    // ---------------------------------------------------------------- Feenreich (Dornenschloss, Pilzteestube)

    public static final DeferredItem<Item> GINGERBREAD = food("gingerbread", 5, 0.5f, null);
    /** Feentee aus Mondglöckchen: belebt und heilt ein wenig. */
    public static final DeferredItem<Item> FAE_TEA = drink("fae_tea", 2, 0.4f, 0.0f, new MobEffectInstance(MobEffects.REGENERATION, 200, 0),
            new MobEffectInstance(MobEffects.SPEED, 900, 0));
    /** Glimmerküchlein: mit Feenstaub gebacken – man springt danach wie auf Wolken. */
    public static final DeferredItem<Item> GLIMMER_CUPCAKE = item("glimmer_cupcake",
            p -> p.food(foodValues(4, 0.6f, false), eat(1.2f, new MobEffectInstance(MobEffects.JUMP_BOOST, 900, 0)))
                    .rarity(Rarity.UNCOMMON).component(DataComponents.LORE, ModItems.lore("glimmer_cupcake", 1)));

    /** Alle neuen Speisen (ohne Mehl), in der Reihenfolge des Kreativ-Tabs – für Modelle, Tests und Vorführung. */
    public static List<DeferredItem<? extends Item>> foods() {
        return List.of(CHEESE, CHEESE_SPAETZLE, PRETZEL, RAW_SAUSAGE, SAUSAGE, RUM, EMBER_PEPPER, EMBER_CHILI, FUNGUS_SKEWER, HARDTACK,
                CHORUS_JAM, DUSK_PORRIDGE, SOUL_BERRY_PIE, STAR_CANDY, UPSIDE_DOWN_CAKE, PILGRIM_RATION, FISH_SOUP, FISH_ROLL, GINGERBREAD,
                FAE_TEA, GLIMMER_CUPCAKE);
    }

    // ---------------------------------------------------------------- Hilfen

    private static <T extends Item> DeferredItem<T> addTo(DeferredItem<T> item) {
        ModItems.FARMING_TAB.add(item);
        return item;
    }

    private static DeferredItem<Item> item(String name, UnaryOperator<Item.Properties> properties) {
        return addTo(ModItems.ITEMS.registerSimpleItem(name, properties));
    }

    private static FoodProperties foodValues(int nutrition, float saturation, boolean always) {
        FoodProperties.Builder builder = new FoodProperties.Builder().nutrition(nutrition).saturationModifier(saturation);
        return (always ? builder.alwaysEdible() : builder).build();
    }

    /** Essen mit Wirkung(en), {@code seconds} lang gekaut. */
    private static Consumable eat(float seconds, MobEffectInstance... effects) {
        Consumable.Builder builder = Consumables.defaultFood().consumeSeconds(seconds);
        for (MobEffectInstance effect : effects) {
            builder.onConsume(new ApplyStatusEffectsConsumeEffect(effect));
        }
        return builder.build();
    }

    /** Einfaches Essen; mit {@code effect} auch eine Wirkung (und eine Zeile Beschreibung). */
    private static DeferredItem<Item> food(String name, int nutrition, float saturation, @Nullable MobEffectInstance effect) {
        return item(name, p -> effect == null ? p.food(foodValues(nutrition, saturation, false))
                : p.food(foodValues(nutrition, saturation, false), eat(1.6f, effect)).component(DataComponents.LORE, ModItems.lore(name, 1)));
    }

    /** Ein Gericht in der Schüssel (die Schüssel bleibt). */
    private static DeferredItem<Item> bowl(String name, int nutrition, float saturation, @Nullable MobEffectInstance effect) {
        return item(name, p -> {
            Item.Properties properties = effect == null ? p.food(foodValues(nutrition, saturation, false))
                    : p.food(foodValues(nutrition, saturation, false), eat(1.6f, effect)).component(DataComponents.LORE, ModItems.lore(name, 1));
            return properties.usingConvertsTo(Items.BOWL).stacksTo(1);
        });
    }

    /** Getränk in der Flasche: immer trinkbar, gibt seine Wirkungen, mit etwas Pech auch Übelkeit; die Flasche bleibt. */
    private static DeferredItem<Item> drink(String name, int nutrition, float saturation, float nauseaChance, MobEffectInstance... effects) {
        Consumable.Builder builder = Consumables.defaultDrink();
        for (MobEffectInstance effect : effects) {
            builder.onConsume(new ApplyStatusEffectsConsumeEffect(effect));
        }
        if (nauseaChance > 0.0f) {
            builder.onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.NAUSEA, 140, 0), nauseaChance));
        }
        Consumable consumable = builder.build();
        return item(name, p -> p.food(foodValues(nutrition, saturation, true), consumable).usingConvertsTo(Items.GLASS_BOTTLE).stacksTo(16)
                .component(DataComponents.LORE, ModItems.lore(name, 1)));
    }

    private static DeferredBlock<CheeseWheelBlock> block(String name) {
        DeferredBlock<CheeseWheelBlock> block = ModBlocks.BLOCKS.registerBlock(name, CheeseWheelBlock::new,
                p -> p.mapColor(MapColor.COLOR_YELLOW).strength(0.5f).sound(SoundType.WOOL).noOcclusion().pushReaction(PushReaction.POPPED));
        ModItems.ITEMS.registerSimpleBlockItem(name, block, p -> p.stacksTo(16).component(DataComponents.LORE, ModItems.lore(name, 1)));
        ModItems.FARMING_TAB.add(block);
        return block;
    }
}
