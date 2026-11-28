package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.CheeseWheelBlock;
import io.github.sirschrolli.kingdomsatwar.block.EmberPepperBlock;
import io.github.sirschrolli.kingdomsatwar.registry.Cuisine;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Tests der Küche der Welten (seit 2.0): Jede neue Speise sättigt, Getränke und Marmelade lassen die Flasche übrig, Gerichte
 * die Schüssel, der Spieß den Stock; die Wirkungen stimmen. Der Käselaib gibt vier Stücke, dann ist er fort (unangeschnitten
 * gibt er sich beim Abbauen selbst). Die Glutschote wächst nur auf Seelenerde und Seelensand und trägt reif zwei bis vier
 * Schoten. Die Rezepte sind geladen.
 */
final class CuisineGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FOODS =
            ModGameTests.TEST_FUNCTIONS.register("cuisine_foods", () -> CuisineGameTests::foods);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CHEESE_WHEEL =
            ModGameTests.TEST_FUNCTIONS.register("cuisine_cheese_wheel", () -> CuisineGameTests::cheeseWheel);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> EMBER_PEPPERS =
            ModGameTests.TEST_FUNCTIONS.register("cuisine_ember_peppers", () -> CuisineGameTests::emberPeppers);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> RECIPES =
            ModGameTests.TEST_FUNCTIONS.register("cuisine_recipes", () -> CuisineGameTests::recipes);

    private CuisineGameTests() {}

    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var test : List.of(FOODS, CHEESE_WHEEL, EMBER_PEPPERS, RECIPES)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 100, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
    }

    private static void floor(GameTestHelper helper) {
        for (int x = 0; x < 9; x++) {
            for (int z = 0; z < 9; z++) {
                helper.setBlock(x, 0, z, Blocks.STONE);
            }
        }
    }

    /** Alle Speisen: sättigen, lassen das Richtige übrig, wirken. */
    private static void foods(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        Map<Item, Item> rests = Map.of(Cuisine.RUM.get(), Items.GLASS_BOTTLE, Cuisine.FAE_TEA.get(), Items.GLASS_BOTTLE,
                Cuisine.CHORUS_JAM.get(), Items.GLASS_BOTTLE, Cuisine.CHEESE_SPAETZLE.get(), Items.BOWL, Cuisine.EMBER_CHILI.get(), Items.BOWL,
                Cuisine.DUSK_PORRIDGE.get(), Items.BOWL, Cuisine.FISH_SOUP.get(), Items.BOWL, Cuisine.FUNGUS_SKEWER.get(), Items.STICK);
        Map<Item, Holder<MobEffect>> effects = Map.of(Cuisine.RUM.get(), MobEffects.RESISTANCE, Cuisine.EMBER_PEPPER.get(), MobEffects.FIRE_RESISTANCE,
                Cuisine.EMBER_CHILI.get(), MobEffects.FIRE_RESISTANCE, Cuisine.CHORUS_JAM.get(), MobEffects.SLOW_FALLING,
                Cuisine.DUSK_PORRIDGE.get(), MobEffects.NIGHT_VISION, Cuisine.STAR_CANDY.get(), MobEffects.SPEED,
                Cuisine.UPSIDE_DOWN_CAKE.get(), MobEffects.SLOW_FALLING, Cuisine.FISH_SOUP.get(), MobEffects.WATER_BREATHING,
                Cuisine.FAE_TEA.get(), MobEffects.REGENERATION, Cuisine.GLIMMER_CUPCAKE.get(), MobEffects.JUMP_BOOST);
        for (var food : Cuisine.foods()) {
            Item item = food.get();
            var props = new ItemStack(item).get(DataComponents.FOOD);
            helper.assertTrue(props != null && props.nutrition() > 0, item + " ist kein Essen");
            player.removeAllEffects();
            player.getFoodData().setFoodLevel(0);
            ItemStack rest = new ItemStack(item).finishUsingItem(level, player);
            helper.assertTrue(player.getFoodData().getFoodLevel() == props.nutrition(),
                    item + " sättigt nicht richtig: " + player.getFoodData().getFoodLevel() + " statt " + props.nutrition());
            Item expected = rests.get(item);
            if (expected != null) {
                helper.assertTrue(rest.is(expected), item + " lässt nicht " + expected + " übrig, sondern " + rest);
            }
            Holder<MobEffect> effect = effects.get(item);
            if (effect != null) {
                helper.assertTrue(player.hasEffect(effect), item + " wirkt nicht (" + effect.getRegisteredName() + ")");
            }
        }
        helper.assertTrue(Cuisine.PILGRIM_RATION.get().getDefaultInstance().getUseDuration(player) > Items.BREAD.getDefaultInstance()
                .getUseDuration(player), "Die Pilgerration isst sich nicht langsamer als Brot");
        helper.succeed();
    }

    /** Der Käselaib: vier Stücke, dann fort; unangeschnitten gibt er sich selbst, angeschnitten nichts. */
    private static void cheeseWheel(GameTestHelper helper) {
        floor(helper);
        ServerLevel level = helper.getLevel();
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.getFoodData().setFoodLevel(0);
        BlockPos wheel = new BlockPos(2, 1, 2);
        helper.setBlock(wheel, Cuisine.CHEESE_WHEEL.get());
        for (int i = 0; i < CheeseWheelBlock.SERVINGS; i++) {
            helper.assertBlockPresent(Cuisine.CHEESE_WHEEL.get(), wheel);
            helper.useBlock(wheel, player);
        }
        helper.assertBlockNotPresent(Cuisine.CHEESE_WHEEL.get(), wheel);
        helper.assertTrue(player.getFoodData().getFoodLevel() == CheeseWheelBlock.SERVINGS * CheeseWheelBlock.NUTRITION,
                "Käselaib sättigt nicht richtig: " + player.getFoodData().getFoodLevel());
        // Beute: ganz → der Laib, angeschnitten → nichts
        BlockState whole = Cuisine.CHEESE_WHEEL.get().defaultBlockState();
        helper.assertTrue(drops(helper, whole).stream().anyMatch(s -> s.is(Cuisine.CHEESE_WHEEL.get().asItem())), "Ganzer Laib gibt sich nicht selbst");
        helper.assertTrue(drops(helper, whole.setValue(CheeseWheelBlock.EATEN, 1)).isEmpty(), "Angeschnittener Laib gibt etwas");
        // steht nur auf festem Grund
        helper.setBlock(new BlockPos(5, 2, 5), Cuisine.CHEESE_WHEEL.get());
        helper.assertTrue(!Cuisine.CHEESE_WHEEL.get().defaultBlockState().canSurvive(level, helper.absolutePos(new BlockPos(5, 2, 5))),
                "Käselaib schwebt in der Luft");
        helper.succeed();
    }

    private static List<ItemStack> drops(GameTestHelper helper, BlockState state) {
        ServerLevel level = helper.getLevel();
        LootParams.Builder params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(helper.absolutePos(BlockPos.ZERO)))
                .withParameter(LootContextParams.TOOL, ItemStack.EMPTY);
        return state.getDrops(params);
    }

    /** Die Glutschote: wächst auf Seelenerde und Seelensand, nicht auf Erde; reif zwei bis vier Schoten, unreif eine. */
    private static void emberPeppers(GameTestHelper helper) {
        floor(helper);
        ServerLevel level = helper.getLevel();
        BlockState crop = Cuisine.EMBER_PEPPERS.get().defaultBlockState();
        helper.setBlock(new BlockPos(1, 1, 1), Blocks.SOUL_SOIL);
        helper.setBlock(new BlockPos(3, 1, 1), Blocks.SOUL_SAND);
        helper.setBlock(new BlockPos(5, 1, 1), Blocks.DIRT);
        helper.assertTrue(crop.canSurvive(level, helper.absolutePos(new BlockPos(1, 2, 1))), "Glutschote wächst nicht auf Seelenerde");
        helper.assertTrue(crop.canSurvive(level, helper.absolutePos(new BlockPos(3, 2, 1))), "Glutschote wächst nicht auf Seelensand");
        helper.assertTrue(!crop.canSurvive(level, helper.absolutePos(new BlockPos(5, 2, 1))), "Glutschote wächst auf Erde");
        // mit der Schote auf Seelenerde gepflanzt
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Cuisine.EMBER_PEPPER.get()));
        helper.placeAt(player, new ItemStack(Cuisine.EMBER_PEPPER.get()), new BlockPos(1, 1, 1), net.minecraft.core.Direction.UP);
        helper.assertBlockPresent(Cuisine.EMBER_PEPPERS.get(), new BlockPos(1, 2, 1));
        BlockState ripe = crop.setValue(EmberPepperBlock.AGE, 3);
        helper.assertTrue(ripe.getLightEmission(level, BlockPos.ZERO) > 0, "Reife Glutschoten glühen nicht");
        for (int i = 0; i < 12; i++) {
            int count = drops(helper, ripe).stream().filter(s -> s.is(Cuisine.EMBER_PEPPER.get())).mapToInt(ItemStack::getCount).sum();
            helper.assertTrue(count >= 2 && count <= 4, "Reife Glutschote gibt " + count + " Schoten");
        }
        int young = drops(helper, crop).stream().filter(s -> s.is(Cuisine.EMBER_PEPPER.get())).mapToInt(ItemStack::getCount).sum();
        helper.assertTrue(young == 1, "Unreife Glutschote gibt " + young + " Schoten");
        helper.succeed();
    }

    /** Die Rezepte der neuen Speisen sind geladen. */
    private static void recipes(GameTestHelper helper) {
        var recipes = helper.getLevel().getServer().getRecipeManager().recipeMap();
        for (String name : List.of("flour", "flour_from_barley", "cheese_wheel", "cheese_wheel_from_cheese", "cheese", "cheese_spaetzle", "pretzel",
                "raw_sausage", "sausage", "sausage_from_smoking", "sausage_from_campfire_cooking", "rum", "ember_chili", "fungus_skewer", "hardtack",
                "chorus_jam", "dusk_porridge", "soul_berry_pie", "star_candy", "upside_down_cake", "pilgrim_ration", "fish_soup_from_cod",
                "fish_soup_from_salmon", "fish_roll_from_cod", "fish_roll_from_salmon", "gingerbread", "fae_tea", "glimmer_cupcake")) {
            ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, KingdomsAtWar.id(name));
            helper.assertTrue(recipes.byKey(key) != null, "Rezept fehlt: " + key.identifier());
        }
        helper.succeed();
    }
}
