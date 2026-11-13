package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.economy.Coins;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Tests der Wirtschaft: Münzen (Bezahlen mit Wechselgeld, Umtauschen, Beute).
 */
final class EconomyGameTests {
    private static final Identifier PLATFORM = KingdomsAtWar.id("test_platform");
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> COINS_PAY =
            ModGameTests.TEST_FUNCTIONS.register("coins_pay", () -> EconomyGameTests::coinsPay);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> COINS_EXCHANGE =
            ModGameTests.TEST_FUNCTIONS.register("coins_exchange", () -> EconomyGameTests::coinsExchange);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> COIN_LOOT =
            ModGameTests.TEST_FUNCTIONS.register("coin_loot", () -> EconomyGameTests::coinLoot);

    private EconomyGameTests() {}

    /** Lädt die Klasse (meldet die Testfunktionen an). */
    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var test : List.of(COINS_PAY, COINS_EXCHANGE, COIN_LOOT)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 200, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
    }

    /**
     * Bezahlen: erst mit Bronze, dann Silber, dann Gold – reicht es nicht genau, wird gewechselt und das Wechselgeld
     * kommt zurück. Ohne genug Geld wird nichts abgezogen.
     */
    private static void coinsPay(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.getInventory().add(new ItemStack(ModItems.GOLD_COIN.get(), 1));
        player.getInventory().add(new ItemStack(ModItems.BRONZE_COIN.get(), 3));
        helper.assertValueEqual(Coins.total(player), 84, "Geld zu Beginn (1 Gold = 81 Bronze)");
        helper.assertTrue(Coins.pay(player, 15), "15 Bronze lassen sich nicht bezahlen");
        helper.assertValueEqual(Coins.total(player), 69, "Geld nach dem Bezahlen");
        helper.assertValueEqual(player.getInventory().countItem(ModItems.GOLD_COIN.get()), 0, "Goldmünze nicht gewechselt");
        helper.assertValueEqual(player.getInventory().countItem(ModItems.SILVER_COIN.get()), 7, "Wechselgeld in Silber");
        helper.assertValueEqual(player.getInventory().countItem(ModItems.BRONZE_COIN.get()), 6, "Wechselgeld in Bronze");
        helper.assertTrue(!Coins.pay(player, 70), "Mehr bezahlen als man hat darf nicht gehen");
        helper.assertValueEqual(Coins.total(player), 69, "Fehlgeschlagene Zahlung hat Geld gekostet");
        helper.assertTrue(Coins.pay(player, 69), "Alles bezahlen geht nicht");
        helper.assertValueEqual(Coins.total(player), 0, "Nach dem Bezahlen von allem bleibt Geld übrig");
        // Beträge als Münzen: größte zuerst, mehr als 64 einer Art in mehreren Stapeln
        List<ItemStack> stacks = Coins.stacks(7_345);
        helper.assertValueEqual(stacks.size(), 4, "Stapel für 7345 Bronze");
        helper.assertTrue(stacks.get(0).is(ModItems.GOLD_COIN.get()) && stacks.get(0).getCount() == 64, "Erster Stapel: 64 Gold");
        helper.assertTrue(stacks.get(1).is(ModItems.GOLD_COIN.get()) && stacks.get(1).getCount() == 26, "Zweiter Stapel: 26 Gold");
        helper.assertTrue(stacks.get(2).is(ModItems.SILVER_COIN.get()) && stacks.get(2).getCount() == 6, "6 Silber");
        helper.assertTrue(stacks.get(3).is(ModItems.BRONZE_COIN.get()) && stacks.get(3).getCount() == 1, "1 Bronze");
        Coins.give(player, 7_345);
        helper.assertValueEqual(Coins.total(player), 7_345, "Geschenktes Geld");
        helper.succeed();
    }

    /**
     * Beute: Monster, die ein Spieler besiegt, lassen Münzen fallen (von 24 Zombies sicher einige); Truhen der Bauwerke
     * enthalten Münzen; die Drachen-Bosse Gold.
     */
    private static void coinLoot(GameTestHelper helper) {
        var level = helper.getLevel();
        var registries = level.getServer().reloadableRegistries();
        helper.assertTrue(registries.getLootTable(io.github.sirschrolli.kingdomsatwar.loot.ModLootTables.MONSTER_COINS)
                != net.minecraft.world.level.storage.loot.LootTable.EMPTY, "Münzbeute der Monster fehlt");
        helper.assertTrue(net.minecraft.world.entity.EntityTypes.ZOMBIE.builtInRegistryHolder()
                .is(io.github.sirschrolli.kingdomsatwar.registry.ModTags.EntityTypes.DROPS_COINS), "Zombies lassen keine Münzen fallen");
        // Truhe einer Burgruine: immer Bronze
        var params = new net.minecraft.world.level.storage.loot.LootParams.Builder(level)
                .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN, helper.absoluteVec(net.minecraft.world.phys.Vec3.ZERO))
                .create(net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.CHEST);
        // seit 1.3 (seltenere Schätze) sind Truhen karg – nicht jede hat Münzen, aber zwanzig zusammen genug
        int chestCoins = 0;
        for (int i = 0; i < 20; i++) {
            for (ItemStack stack : registries.getLootTable(io.github.sirschrolli.kingdomsatwar.loot.ModLootTables.CASTLE_RUIN_CHEST).getRandomItems(params)) {
                chestCoins += Coins.value(stack);
            }
        }
        helper.assertTrue(chestCoins >= 10 && chestCoins <= 250, "Zwanzig Truhen der Burgruine mit " + chestCoins + " Münzen");
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        // jeder zehnte Zombie lässt eine Münze fallen: drei Wellen zu 24 (dass keiner von 72 etwas gibt, ist so gut wie
        // ausgeschlossen – bei nur 24 käme es in jedem zwölften Lauf vor)
        for (int wave = 0; wave < 3; wave++) {
            helper.runAfterDelay(1 + wave * 4, () -> {
                for (int i = 0; i < 24; i++) {
                    var zombie = helper.spawnWithNoFreeWill(io.github.sirschrolli.kingdomsatwar.registry.ModEntities.ZOMBIE.get(),
                            new net.minecraft.core.BlockPos(2 + i % 6, 1, 2 + i / 6));
                    zombie.hurtServer(level, level.damageSources().playerAttack(player), 1000.0f);
                }
            });
        }
        helper.runAfterDelay(60, () -> {
            int coins = 0;
            for (var item : helper.getEntities(net.minecraft.world.entity.EntityTypes.ITEM)) {
                coins += Coins.value(item.getItem());
            }
            helper.assertTrue(coins > 0, "Keine Münzen von 72 Zombies");
            helper.succeed();
        });
    }

    /** Umtauschen: neun Münzen per Rechtsklick in eine größere, mit Schleichen eine in neun kleinere. */
    private static void coinsExchange(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.BRONZE_COIN.get(), 12));
        player.getMainHandItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertValueEqual(player.getInventory().countItem(ModItems.BRONZE_COIN.get()), 3, "Bronze nach dem Wechseln");
        helper.assertValueEqual(player.getInventory().countItem(ModItems.SILVER_COIN.get()), 1, "Silber nach dem Wechseln");
        // mit nur drei Münzen geht es nicht
        player.getMainHandItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertValueEqual(Coins.total(player), 12, "Wechseln von drei Münzen hat etwas verändert");
        // eine Goldmünze in neun Silbermünzen
        player.getInventory().clearContent();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.GOLD_COIN.get(), 1));
        player.setShiftKeyDown(true);
        player.getMainHandItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        player.setShiftKeyDown(false);
        helper.assertValueEqual(player.getInventory().countItem(ModItems.GOLD_COIN.get()), 0, "Gold nach dem Wechseln");
        helper.assertValueEqual(player.getInventory().countItem(ModItems.SILVER_COIN.get()), 9, "Silber aus Gold");
        helper.succeed();
    }
}
