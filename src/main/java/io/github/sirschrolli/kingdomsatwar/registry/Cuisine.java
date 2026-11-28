package io.github.sirschrolli.kingdomsatwar.registry;

import io.github.sirschrolli.kingdomsatwar.block.CheeseWheelBlock;
import io.github.sirschrolli.kingdomsatwar.block.EmberPepperBlock;
import io.github.sirschrolli.kingdomsatwar.block.crop.BerryBushBlock;
import io.github.sirschrolli.kingdomsatwar.block.crop.RiceBlock;
import io.github.sirschrolli.kingdomsatwar.block.crop.SeaGrapeBlock;
import io.github.sirschrolli.kingdomsatwar.block.crop.ShadowTuberBlock;
import io.github.sirschrolli.kingdomsatwar.item.ChaosFood;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
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
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
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

    // =====================================================================================
    // Die Feldfrüchte der Welten (2.0): in jeder Dimension etwas, das man findet, pflanzt und kocht
    // =====================================================================================

    // ---------------------------------------------------------------- Oberwelt: Reis (im seichten Wasser)

    public static final DeferredBlock<RiceBlock> RICE_PLANT = ModBlocks.BLOCKS.registerBlock("rice", p -> new RiceBlock(() -> Cuisine.RICE.get(), p),
            p -> p.mapColor(MapColor.PLANT).noCollision().randomTicks().instabreak().sound(SoundType.CROP).pushReaction(PushReaction.POPPED));
    /** Reis: das Korn ist zugleich der Samen – in seichtes Wasser über Erde oder Schlamm gepflanzt. Roh nicht essbar. */
    public static final DeferredItem<BlockItem> RICE = plantable("rice", RICE_PLANT, null, null);
    /** Milchreis: in Milch gekocht, mit Zucker – mild und sättigend. */
    public static final DeferredItem<Item> RICE_PUDDING = bowl("rice_pudding", 7, 0.7f, null);
    /** Sushi: Reis und roher Fisch in getrocknetem Seetang gerollt – schnell gegessen. */
    public static final DeferredItem<Item> SUSHI = item("sushi", p -> p.food(foodValues(4, 0.6f, false), Consumables.defaultFood().consumeSeconds(0.8f).build()));

    // ---------------------------------------------------------------- Urmeer: Meertrauben (unter Wasser)

    public static final DeferredBlock<SeaGrapeBlock> SEA_GRAPE_PLANT = ModBlocks.BLOCKS.registerBlock("sea_grape_plant",
            p -> new SeaGrapeBlock(() -> Cuisine.SEA_GRAPES.get(), p),
            p -> p.mapColor(MapColor.WATER).noCollision().randomTicks().instabreak().sound(SoundType.WET_GRASS).lightLevel(SeaGrapeBlock::light)
                    .pushReaction(PushReaction.POPPED));
    /** Meertrauben: salzig und knackig – ein paar Atemzüge länger unter Wasser; unter Wasser auf Sand gepflanzt, wachsen sie nach. */
    public static final DeferredItem<BlockItem> SEA_GRAPES = plantable("sea_grapes", SEA_GRAPE_PLANT, foodValues(2, 0.4f, false),
            eat(1.0f, new MobEffectInstance(MobEffects.WATER_BREATHING, 200, 0)));
    /** Meertraubensalat: Meertrauben und Seetang mit etwas Kabeljau – eine Minute lang Kiemen. */
    public static final DeferredItem<Item> SEA_GRAPE_SALAD = bowl("sea_grape_salad", 7, 0.7f, new MobEffectInstance(MobEffects.WATER_BREATHING, 1200, 0));

    // ---------------------------------------------------------------- End: Schwebebeeren (auf Endstein)

    public static final DeferredBlock<BerryBushBlock> FLOAT_BERRY_BUSH = bush("float_berry_bush", () -> Cuisine.FLOAT_BERRIES.get(),
            state -> state.is(Blocks.END_STONE), MapColor.COLOR_PURPLE, 0, 4);
    /** Schwebebeeren: leicht wie Luft – kurz fällt man wie eine Feder; auf Endstein gepflanzt, wächst ein neuer Strauch. */
    public static final DeferredItem<BlockItem> FLOAT_BERRIES = plantable("float_berries", FLOAT_BERRY_BUSH, foodValues(2, 0.3f, false),
            eat(1.0f, new MobEffectInstance(MobEffects.SLOW_FALLING, 100, 0)));
    /** Schwebebeerentörtchen: man springt danach höher und landet sanft. */
    public static final DeferredItem<Item> FLOAT_BERRY_TART = item("float_berry_tart",
            p -> p.food(foodValues(6, 0.6f, false), eat(1.6f, new MobEffectInstance(MobEffects.SLOW_FALLING, 600, 0),
                    new MobEffectInstance(MobEffects.JUMP_BOOST, 600, 0))).component(DataComponents.LORE, ModItems.lore("float_berry_tart", 1)));

    // ---------------------------------------------------------------- Schattenwelt: Schattenknollen (auch im Dunkeln)

    public static final DeferredBlock<ShadowTuberBlock> SHADOW_TUBERS = ModBlocks.BLOCKS.registerBlock("shadow_tubers",
            p -> new ShadowTuberBlock(() -> Cuisine.SHADOW_TUBER.get(), p),
            p -> p.mapColor(MapColor.COLOR_PURPLE).noCollision().randomTicks().instabreak().sound(SoundType.CROP).lightLevel(ShadowTuberBlock::light)
                    .pushReaction(PushReaction.POPPED));
    /** Schattenknolle: roh nur ein Bissen, geröstet ein gutes Essen; wächst auch in völliger Finsternis. */
    public static final DeferredItem<BlockItem> SHADOW_TUBER = plantable("shadow_tuber", SHADOW_TUBERS, foodValues(1, 0.3f, false), null);
    public static final DeferredItem<Item> ROASTED_SHADOW_TUBER = food("roasted_shadow_tuber", 6, 0.6f, null);
    /** Knolleneintopf: Schattenknollen mit Hammel – macht zäh wie die Umbrer. */
    public static final DeferredItem<Item> SHADOW_TUBER_STEW = bowl("shadow_tuber_stew", 10, 0.8f, new MobEffectInstance(MobEffects.RESISTANCE, 1200, 0));

    // ---------------------------------------------------------------- Leere: Sternfrüchte (auf Leerenmoos und Sternenstaub)

    public static final DeferredBlock<BerryBushBlock> STARFRUIT_BUSH = bush("starfruit_bush", () -> Cuisine.STARFRUIT.get(),
            state -> state.is(VoidBlocks.VOID_MOSS.get()) || state.is(VoidBlocks.STARDUST.get()), MapColor.COLOR_CYAN, 3, 7);
    /** Sternfrucht: leuchtet von innen – man sieht danach eine Weile im Dunkeln. */
    public static final DeferredItem<BlockItem> STARFRUIT = plantable("starfruit", STARFRUIT_BUSH, foodValues(3, 0.5f, false),
            eat(1.2f, new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 0)));
    /** Sternfruchtsaft: drei Minuten Nachtsicht und eine halbe Minute flink. */
    public static final DeferredItem<Item> STARFRUIT_JUICE = drink("starfruit_juice", 3, 0.4f, 0.0f, new MobEffectInstance(MobEffects.NIGHT_VISION, 3600, 0),
            new MobEffectInstance(MobEffects.SPEED, 600, 0));

    // ---------------------------------------------------------------- Abgrund: Chaosbohnen (auf allem, woraus die Splitter sind)

    public static final DeferredBlock<BerryBushBlock> CHAOS_BEAN_BUSH = bush("chaos_bean_bush", () -> Cuisine.CHAOS_BEANS.get(), Cuisine::chaosSoil,
            MapColor.COLOR_MAGENTA, 0, 3);
    /** Chaosbohnen: jede wirkt anders – nicht immer gut. */
    public static final DeferredItem<ChaosFood.Beans> CHAOS_BEANS = addTo(ModItems.ITEMS.registerItem("chaos_beans",
            p -> new ChaosFood.Beans(CHAOS_BEAN_BUSH.get(), p),
            () -> new Item.Properties().useItemDescriptionPrefix().food(foodValues(2, 0.2f, true), Consumables.defaultFood().consumeSeconds(1.0f).build())
                    .compostable(ContextIntProviders.COMPOSTABLE_MEDIUM).component(DataComponents.LORE, ModItems.lore("chaos_beans", 1))));
    /** Chaosbohneneintopf: wirkt jedes Mal anders – aber immer gut, und anderthalb Minuten lang. */
    public static final DeferredItem<ChaosFood.Stew> CHAOS_BEAN_STEW = addTo(ModItems.ITEMS.registerItem("chaos_bean_stew", ChaosFood.Stew::new,
            () -> new Item.Properties().food(foodValues(9, 0.8f, false)).usingConvertsTo(Items.BOWL).stacksTo(1)
                    .component(DataComponents.LORE, ModItems.lore("chaos_bean_stew", 1))));

    // ---------------------------------------------------------------- Feenreich: Feenbeeren (auf Feengras)

    public static final DeferredBlock<BerryBushBlock> FAE_BERRY_BUSH = bush("fae_berry_bush", () -> Cuisine.FAE_BERRIES.get(),
            state -> state.is(FaeBlocks.FAE_GRASS.get()) || state.is(FaeBlocks.FAE_SOIL.get()), MapColor.COLOR_PINK, 0, 3);
    /** Feenbeeren: süß und heilsam. */
    public static final DeferredItem<BlockItem> FAE_BERRIES = plantable("fae_berries", FAE_BERRY_BUSH, foodValues(2, 0.3f, false),
            eat(1.0f, new MobEffectInstance(MobEffects.REGENERATION, 100, 0)));
    /** Feenpfannkuchen mit Feenbeeren: heilen eine Weile. */
    public static final DeferredItem<Item> FAE_PANCAKES = item("fae_pancakes",
            p -> p.food(foodValues(8, 0.6f, false), eat(1.6f, new MobEffectInstance(MobEffects.REGENERATION, 300, 0)))
                    .component(DataComponents.LORE, ModItems.lore("fae_pancakes", 1)));

    /** Worauf die Chaosbohne wächst: Erde und Gras, Nyzel, Endstein, Chaosstein, Weltnaht, Schattengras und Leerenmoos. */
    private static boolean chaosSoil(BlockState state) {
        return state.is(net.minecraft.tags.BlockTags.SUBSTRATE_OVERWORLD) || state.is(net.minecraft.tags.BlockTags.NYLIUM) || state.is(Blocks.END_STONE)
                || state.is(AbyssBlocks.CHAOS_STONE.base().get()) || state.is(AbyssBlocks.WORLD_SEAM.get()) || state.is(ShadowBlocks.SHADOW_GRASS.get())
                || state.is(ShadowBlocks.SHADOW_SOIL.get()) || state.is(VoidBlocks.VOID_MOSS.get());
    }

    /** Alle neuen Speisen (ohne Mehl und Reis), in der Reihenfolge des Kreativ-Tabs – für Modelle, Tests und Vorführung. */
    public static List<DeferredItem<? extends Item>> foods() {
        return List.of(CHEESE, CHEESE_SPAETZLE, PRETZEL, RAW_SAUSAGE, SAUSAGE, RUM, EMBER_PEPPER, EMBER_CHILI, FUNGUS_SKEWER, HARDTACK,
                CHORUS_JAM, DUSK_PORRIDGE, SOUL_BERRY_PIE, STAR_CANDY, UPSIDE_DOWN_CAKE, PILGRIM_RATION, FISH_SOUP, FISH_ROLL, GINGERBREAD,
                FAE_TEA, GLIMMER_CUPCAKE, RICE_PUDDING, SUSHI, SEA_GRAPES, SEA_GRAPE_SALAD, FLOAT_BERRIES, FLOAT_BERRY_TART, SHADOW_TUBER,
                ROASTED_SHADOW_TUBER, SHADOW_TUBER_STEW, STARFRUIT, STARFRUIT_JUICE, CHAOS_BEANS, CHAOS_BEAN_STEW, FAE_BERRIES, FAE_PANCAKES);
    }

    /** Was man pflanzt (ihre Gegenstandsmodelle kommen mit den Pflanzen). */
    public static List<DeferredItem<? extends Item>> plantables() {
        return List.of(EMBER_PEPPER, RICE, SEA_GRAPES, FLOAT_BERRIES, SHADOW_TUBER, STARFRUIT, CHAOS_BEANS, FAE_BERRIES);
    }

    /** Die Fruchtsträucher der fremden Welten. */
    public static List<DeferredBlock<BerryBushBlock>> bushes() {
        return List.of(FLOAT_BERRY_BUSH, STARFRUIT_BUSH, CHAOS_BEAN_BUSH, FAE_BERRY_BUSH);
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

    /** Was man pflanzt: der Gegenstand der Pflanze, essbar oder nicht, mit einer Zeile darüber, wo es wächst. */
    private static DeferredItem<BlockItem> plantable(String name, DeferredBlock<? extends Block> plant, @Nullable FoodProperties food,
            @Nullable Consumable consumable) {
        return addTo(ModItems.ITEMS.registerItem(name, p -> new BlockItem(plant.get(), p), () -> {
            Item.Properties properties = new Item.Properties().useItemDescriptionPrefix().compostable(ContextIntProviders.COMPOSTABLE_MEDIUM)
                    .component(DataComponents.LORE, ModItems.lore(name, 1));
            return food == null ? properties : consumable == null ? properties.food(food) : properties.food(food, consumable);
        }));
    }

    /** Ein Fruchtstrauch: halbreif leuchtet er mit {@code halfLight}, reif mit {@code ripeLight}. */
    private static DeferredBlock<BerryBushBlock> bush(String name, Supplier<? extends ItemLike> fruit, Predicate<BlockState> soil, MapColor color,
            int halfLight, int ripeLight) {
        return ModBlocks.BLOCKS.registerBlock(name, p -> new BerryBushBlock(fruit, soil, p),
                p -> p.mapColor(color).randomTicks().noCollision().strength(0.2f).sound(SoundType.SWEET_BERRY_BUSH)
                        .lightLevel(state -> switch (state.getValue(BerryBushBlock.AGE)) {
                            case 2 -> halfLight;
                            case 3 -> ripeLight;
                            default -> 0;
                        }).pushReaction(PushReaction.POPPED));
    }

    private static DeferredBlock<CheeseWheelBlock> block(String name) {
        DeferredBlock<CheeseWheelBlock> block = ModBlocks.BLOCKS.registerBlock(name, CheeseWheelBlock::new,
                p -> p.mapColor(MapColor.COLOR_YELLOW).strength(0.5f).sound(SoundType.WOOL).noOcclusion().pushReaction(PushReaction.POPPED));
        ModItems.ITEMS.registerSimpleBlockItem(name, block, p -> p.stacksTo(16).component(DataComponents.LORE, ModItems.lore(name, 1)));
        ModItems.FARMING_TAB.add(block);
        return block;
    }
}
