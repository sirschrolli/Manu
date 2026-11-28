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
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> BUSHES =
            ModGameTests.TEST_FUNCTIONS.register("cuisine_bushes", () -> CuisineGameTests::bushes);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> WATER_CROPS =
            ModGameTests.TEST_FUNCTIONS.register("cuisine_water_crops", () -> CuisineGameTests::waterCrops);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SHADOW_TUBERS =
            ModGameTests.TEST_FUNCTIONS.register("cuisine_shadow_tubers", () -> CuisineGameTests::shadowTubers);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CHAOS_BEANS =
            ModGameTests.TEST_FUNCTIONS.register("cuisine_chaos_beans", () -> CuisineGameTests::chaosBeans);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> WILD_CROPS =
            ModGameTests.TEST_FUNCTIONS.register("cuisine_wild_crops", () -> CuisineGameTests::wildCrops);

    private CuisineGameTests() {}

    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var test : List.of(FOODS, CHEESE_WHEEL, EMBER_PEPPERS, RECIPES, BUSHES, WATER_CROPS, SHADOW_TUBERS, CHAOS_BEANS, WILD_CROPS)) {
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
        Map<Item, Item> rests = Map.ofEntries(Map.entry(Cuisine.RUM.get(), Items.GLASS_BOTTLE), Map.entry(Cuisine.FAE_TEA.get(), Items.GLASS_BOTTLE),
                Map.entry(Cuisine.CHORUS_JAM.get(), Items.GLASS_BOTTLE), Map.entry(Cuisine.CHEESE_SPAETZLE.get(), Items.BOWL),
                Map.entry(Cuisine.EMBER_CHILI.get(), Items.BOWL), Map.entry(Cuisine.DUSK_PORRIDGE.get(), Items.BOWL), Map.entry(Cuisine.FISH_SOUP.get(), Items.BOWL),
                Map.entry(Cuisine.FUNGUS_SKEWER.get(), Items.STICK), Map.entry(Cuisine.RICE_PUDDING.get(), Items.BOWL),
                Map.entry(Cuisine.SEA_GRAPE_SALAD.get(), Items.BOWL), Map.entry(Cuisine.SHADOW_TUBER_STEW.get(), Items.BOWL),
                Map.entry(Cuisine.CHAOS_BEAN_STEW.get(), Items.BOWL), Map.entry(Cuisine.STARFRUIT_JUICE.get(), Items.GLASS_BOTTLE));
        Map<Item, Holder<MobEffect>> effects = Map.ofEntries(Map.entry(Cuisine.RUM.get(), MobEffects.RESISTANCE),
                Map.entry(Cuisine.EMBER_PEPPER.get(), MobEffects.FIRE_RESISTANCE), Map.entry(Cuisine.EMBER_CHILI.get(), MobEffects.FIRE_RESISTANCE),
                Map.entry(Cuisine.CHORUS_JAM.get(), MobEffects.SLOW_FALLING), Map.entry(Cuisine.DUSK_PORRIDGE.get(), MobEffects.NIGHT_VISION),
                Map.entry(Cuisine.STAR_CANDY.get(), MobEffects.SPEED), Map.entry(Cuisine.UPSIDE_DOWN_CAKE.get(), MobEffects.SLOW_FALLING),
                Map.entry(Cuisine.FISH_SOUP.get(), MobEffects.WATER_BREATHING), Map.entry(Cuisine.FAE_TEA.get(), MobEffects.REGENERATION),
                Map.entry(Cuisine.GLIMMER_CUPCAKE.get(), MobEffects.JUMP_BOOST), Map.entry(Cuisine.SEA_GRAPES.get(), MobEffects.WATER_BREATHING),
                Map.entry(Cuisine.SEA_GRAPE_SALAD.get(), MobEffects.WATER_BREATHING), Map.entry(Cuisine.FLOAT_BERRIES.get(), MobEffects.SLOW_FALLING),
                Map.entry(Cuisine.FLOAT_BERRY_TART.get(), MobEffects.JUMP_BOOST), Map.entry(Cuisine.SHADOW_TUBER_STEW.get(), MobEffects.RESISTANCE),
                Map.entry(Cuisine.STARFRUIT.get(), MobEffects.NIGHT_VISION), Map.entry(Cuisine.STARFRUIT_JUICE.get(), MobEffects.SPEED),
                Map.entry(Cuisine.FAE_BERRIES.get(), MobEffects.REGENERATION), Map.entry(Cuisine.FAE_PANCAKES.get(), MobEffects.REGENERATION));
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
            if (item == Cuisine.CHAOS_BEANS.get() || item == Cuisine.CHAOS_BEAN_STEW.get()) {
                helper.assertTrue(!player.getActiveEffects().isEmpty(), item + " wirkt nicht");
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
                "fish_soup_from_salmon", "fish_roll_from_cod", "fish_roll_from_salmon", "gingerbread", "fae_tea", "glimmer_cupcake", "rice_pudding",
                "sushi_from_cod", "sushi_from_salmon", "sea_grape_salad", "float_berry_tart", "roasted_shadow_tuber", "roasted_shadow_tuber_from_smoking",
                "roasted_shadow_tuber_from_campfire_cooking", "shadow_tuber_stew", "starfruit_juice", "chaos_bean_stew", "fae_pancakes")) {
            ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, KingdomsAtWar.id(name));
            helper.assertTrue(recipes.byKey(key) != null, "Rezept fehlt: " + key.identifier());
        }
        helper.succeed();
    }

    /** Zählt die Gegenstände {@code item}, die um {@code pos} herum liegen (und räumt sie fort). */
    private static int picked(GameTestHelper helper, BlockPos pos, Item item) {
        var area = new net.minecraft.world.phys.AABB(helper.absolutePos(pos)).inflate(2.0);
        int count = 0;
        for (var entity : helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, area)) {
            if (entity.getItem().is(item)) {
                count += entity.getItem().getCount();
            }
            entity.discard();
        }
        return count;
    }

    /**
     * Die Sträucher der fremden Welten: jeder wächst nur auf seinem Grund, wird mit seiner Frucht gepflanzt, reift mit
     * Knochenmehl, gibt reif beim Pflücken zwei bis drei Früchte und trägt danach von neuem; reif leuchtet die Sternfrucht.
     */
    private static void bushes(GameTestHelper helper) {
        floor(helper);
        ServerLevel level = helper.getLevel();
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        Map<net.minecraft.world.level.block.Block, net.minecraft.world.level.block.Block> soils = Map.of(
                Cuisine.FLOAT_BERRY_BUSH.get(), Blocks.END_STONE,
                Cuisine.STARFRUIT_BUSH.get(), io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.VOID_MOSS.get(),
                Cuisine.CHAOS_BEAN_BUSH.get(), io.github.sirschrolli.kingdomsatwar.registry.AbyssBlocks.CHAOS_STONE.base().get(),
                Cuisine.FAE_BERRY_BUSH.get(), io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks.FAE_GRASS.get());
        int x = 1;
        for (var deferred : Cuisine.bushes()) {
            var bush = deferred.get();
            Item fruit = bush.fruit().asItem();
            BlockPos soil = new BlockPos(x, 1, 2);
            BlockPos at = soil.above();
            helper.setBlock(soil, Blocks.STONE);
            helper.assertTrue(!bush.defaultBlockState().canSurvive(level, helper.absolutePos(at)), bush + " wächst auf Stein");
            helper.setBlock(soil, soils.get(bush));
            helper.assertTrue(bush.defaultBlockState().canSurvive(level, helper.absolutePos(at)), bush + " wächst nicht auf " + soils.get(bush));
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(fruit));
            helper.placeAt(player, new ItemStack(fruit), soil, net.minecraft.core.Direction.UP);
            helper.assertBlockPresent(bush, at);
            for (int i = 0; i < 3; i++) {
                net.minecraft.world.item.BoneMealItem.growCrop(new ItemStack(Items.BONE_MEAL, 1), level, helper.absolutePos(at));
            }
            helper.assertTrue(helper.getBlockState(at).getValue(io.github.sirschrolli.kingdomsatwar.block.crop.BerryBushBlock.AGE) == 3,
                    bush + " reift nicht mit Knochenmehl");
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            helper.useBlock(at, player);
            int count = picked(helper, at, fruit);
            helper.assertTrue(count >= 2 && count <= 3, bush + " gibt reif " + count + " Früchte");
            helper.assertTrue(helper.getBlockState(at).getValue(io.github.sirschrolli.kingdomsatwar.block.crop.BerryBushBlock.AGE) == 1,
                    bush + " trägt nach dem Pflücken nicht von neuem");
            x += 2;
        }
        BlockState ripeStar = Cuisine.STARFRUIT_BUSH.get().defaultBlockState().setValue(io.github.sirschrolli.kingdomsatwar.block.crop.BerryBushBlock.AGE, 3);
        helper.assertTrue(ripeStar.getLightEmission(level, BlockPos.ZERO) > 0, "Reife Sternfrüchte leuchten nicht");
        helper.succeed();
    }

    /**
     * Unter Wasser: Meertrauben wachsen nur im Wasser auf Sand (nicht an Land), werden mit den Trauben gepflanzt und reif
     * gepflückt. Reis wächst nur im seichten Wasser (ein Block tief über Erde), nicht im tiefen und nicht an Land; reif gibt er
     * mehrere Körner.
     */
    private static void waterCrops(GameTestHelper helper) {
        floor(helper);
        ServerLevel level = helper.getLevel();
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        // ein Becken: Sand am Grund, zwei Blöcke Wasser, ringsum Glas
        for (int x = 0; x < 5; x++) {
            for (int z = 0; z < 5; z++) {
                boolean rim = x == 0 || z == 0 || x == 4 || z == 4;
                helper.setBlock(x, 1, z, rim ? Blocks.GLASS : Blocks.SAND);
                helper.setBlock(x, 2, z, rim ? Blocks.GLASS : Blocks.WATER);
                helper.setBlock(x, 3, z, rim ? Blocks.GLASS : Blocks.WATER);
            }
        }
        BlockState grapes = Cuisine.SEA_GRAPE_PLANT.get().defaultBlockState();
        helper.assertTrue(grapes.canSurvive(level, helper.absolutePos(new BlockPos(2, 2, 2))), "Meertrauben wachsen nicht unter Wasser auf Sand");
        helper.setBlock(6, 1, 6, Blocks.SAND);
        helper.assertTrue(!grapes.canSurvive(level, helper.absolutePos(new BlockPos(6, 2, 6))), "Meertrauben wachsen an Land");
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Cuisine.SEA_GRAPES.get()));
        helper.placeAt(player, new ItemStack(Cuisine.SEA_GRAPES.get()), new BlockPos(2, 1, 2), net.minecraft.core.Direction.UP);
        helper.assertBlockPresent(Cuisine.SEA_GRAPE_PLANT.get(), new BlockPos(2, 2, 2));
        helper.setBlock(new BlockPos(2, 2, 2), grapes.setValue(io.github.sirschrolli.kingdomsatwar.block.crop.SeaGrapeBlock.AGE, 3));
        helper.assertTrue(helper.getBlockState(new BlockPos(2, 2, 2)).getFluidState().isSource(), "Meertrauben stehen nicht im Wasser");
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        helper.useBlock(new BlockPos(2, 2, 2), player);
        int count = picked(helper, new BlockPos(2, 2, 2), Cuisine.SEA_GRAPES.get());
        helper.assertTrue(count >= 2 && count <= 3, "Reife Meertrauben geben " + count);
        // Reis: im tiefen Wasser nicht, im seichten über Erde ja, an Land nicht
        BlockState rice = Cuisine.RICE_PLANT.get().defaultBlockState();
        helper.setBlock(new BlockPos(3, 1, 3), Blocks.DIRT);
        helper.assertTrue(!rice.canSurvive(level, helper.absolutePos(new BlockPos(3, 2, 3))), "Reis wächst im tiefen Wasser");
        helper.setBlock(new BlockPos(3, 3, 3), Blocks.AIR);
        helper.assertTrue(rice.canSurvive(level, helper.absolutePos(new BlockPos(3, 2, 3))), "Reis wächst nicht im seichten Wasser über Erde");
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Cuisine.RICE.get()));
        helper.placeAt(player, new ItemStack(Cuisine.RICE.get()), new BlockPos(3, 1, 3), net.minecraft.core.Direction.UP);
        helper.assertBlockPresent(Cuisine.RICE_PLANT.get(), new BlockPos(3, 2, 3));
        helper.setBlock(7, 1, 7, Blocks.DIRT);
        helper.assertTrue(!rice.canSurvive(level, helper.absolutePos(new BlockPos(7, 2, 7))), "Reis wächst an Land");
        BlockState ripe = rice.setValue(net.minecraft.world.level.block.CropBlock.AGE, 7);
        for (int i = 0; i < 8; i++) {
            int grains = drops(helper, ripe).stream().filter(st -> st.is(Cuisine.RICE.get())).mapToInt(ItemStack::getCount).sum();
            helper.assertTrue(grains >= 2 && grains <= 5, "Reifer Reis gibt " + grains + " Körner");
        }
        helper.succeed();
    }

    /** Schattenknollen: auf Ackerland, Schattengras und Schattenerde (nicht auf Stein); reif mehrere Knollen; geröstet ein Essen. */
    private static void shadowTubers(GameTestHelper helper) {
        floor(helper);
        ServerLevel level = helper.getLevel();
        BlockState crop = Cuisine.SHADOW_TUBERS.get().defaultBlockState();
        helper.setBlock(new BlockPos(1, 1, 1), Blocks.FARMLAND);
        helper.setBlock(new BlockPos(3, 1, 1), io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks.SHADOW_GRASS.get());
        helper.setBlock(new BlockPos(5, 1, 1), io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks.SHADOW_FARMLAND.get());
        helper.assertTrue(crop.canSurvive(level, helper.absolutePos(new BlockPos(1, 2, 1))), "Schattenknollen wachsen nicht auf Ackerland");
        helper.assertTrue(crop.canSurvive(level, helper.absolutePos(new BlockPos(3, 2, 1))), "Schattenknollen wachsen nicht auf Schattengras");
        helper.assertTrue(crop.canSurvive(level, helper.absolutePos(new BlockPos(5, 2, 1))), "Schattenknollen wachsen nicht auf Schattenackerland");
        helper.assertTrue(!crop.canSurvive(level, helper.absolutePos(new BlockPos(7, 1, 1))), "Schattenknollen wachsen auf Stein");
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Cuisine.SHADOW_TUBER.get()));
        helper.placeAt(player, new ItemStack(Cuisine.SHADOW_TUBER.get()), new BlockPos(1, 1, 1), net.minecraft.core.Direction.UP);
        helper.assertBlockPresent(Cuisine.SHADOW_TUBERS.get(), new BlockPos(1, 2, 1));
        BlockState ripe = crop.setValue(net.minecraft.world.level.block.CropBlock.AGE, 7);
        helper.assertTrue(ripe.getLightEmission(level, BlockPos.ZERO) > 0, "Reife Schattenknollen schimmern nicht");
        for (int i = 0; i < 8; i++) {
            int tubers = drops(helper, ripe).stream().filter(st -> st.is(Cuisine.SHADOW_TUBER.get())).mapToInt(ItemStack::getCount).sum();
            helper.assertTrue(tubers >= 2 && tubers <= 5, "Reife Schattenknollen geben " + tubers);
        }
        int young = drops(helper, crop).stream().filter(st -> st.is(Cuisine.SHADOW_TUBER.get())).mapToInt(ItemStack::getCount).sum();
        helper.assertTrue(young == 1, "Junge Schattenknollen geben " + young);
        helper.succeed();
    }

    /** Chaosbohnen: roh mal diese, mal jene Wirkung (aus der Liste, auch schlechte), der Eintopf immer eine gute. */
    private static void chaosBeans(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        java.util.Set<Holder<MobEffect>> seen = new java.util.HashSet<>();
        for (int i = 0; i < 40; i++) {
            player.removeAllEffects();
            new ItemStack(Cuisine.CHAOS_BEANS.get()).finishUsingItem(level, player);
            helper.assertTrue(player.getActiveEffects().size() == 1, "Eine Chaosbohne gibt " + player.getActiveEffects().size() + " Wirkungen");
            Holder<MobEffect> effect = player.getActiveEffects().iterator().next().getEffect();
            helper.assertTrue(io.github.sirschrolli.kingdomsatwar.item.ChaosFood.possible(false).contains(effect), "Unbekannte Wirkung " + effect);
            seen.add(effect);
        }
        helper.assertTrue(seen.size() >= 4, "Chaosbohnen wirken zu gleichförmig: " + seen.size());
        for (int i = 0; i < 20; i++) {
            player.removeAllEffects();
            ItemStack rest = new ItemStack(Cuisine.CHAOS_BEAN_STEW.get()).finishUsingItem(level, player);
            helper.assertTrue(rest.is(Items.BOWL), "Der Chaosbohneneintopf lässt keine Schüssel übrig");
            Holder<MobEffect> effect = player.getActiveEffects().iterator().next().getEffect();
            helper.assertTrue(effect.value().isBeneficial(), "Der Eintopf wirkt schlecht: " + effect.getRegisteredName());
        }
        helper.succeed();
    }

    /** Die wilden Grüppchen sind da (als Platzierungen geladen), und jedes wächst dort, wo es wachsen soll. */
    private static void wildCrops(GameTestHelper helper) {
        var placed = helper.getLevel().registryAccess().lookupOrThrow(Registries.PLACED_FEATURE);
        for (var patch : io.github.sirschrolli.kingdomsatwar.worldgen.WildCrops.PATCHES) {
            helper.assertTrue(placed.get(patch.placedKey()).isPresent(), "Wilde Pflanzen fehlen: " + patch.placedKey().identifier());
        }
        helper.succeed();
    }
}
