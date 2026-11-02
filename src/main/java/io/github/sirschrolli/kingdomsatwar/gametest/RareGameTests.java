package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.economy.citizen.Trading;
import io.github.sirschrolli.kingdomsatwar.entity.creature.farm.FarmAnimalConversion;
import io.github.sirschrolli.kingdomsatwar.entity.creature.rare.CloudJelly;
import io.github.sirschrolli.kingdomsatwar.entity.creature.rare.PrimevalDigger;
import io.github.sirschrolli.kingdomsatwar.entity.villager.TravelingMerchant;
import io.github.sirschrolli.kingdomsatwar.entity.villager.VillageProfession;
import io.github.sirschrolli.kingdomsatwar.entity.villager.VillagerEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/** Tests der seltenen Wesen, der umgewandelten Dorfbewohner und des fahrenden Händlers (E4d-2). */
final class RareGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SPAWN =
            ModGameTests.TEST_FUNCTIONS.register("rare_spawn", () -> RareGameTests::spawn);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CONVERSION =
            ModGameTests.TEST_FUNCTIONS.register("rare_vanilla_replaced", () -> RareGameTests::vanillaReplaced);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> VILLAGERS =
            ModGameTests.TEST_FUNCTIONS.register("rare_villager_professions", () -> RareGameTests::villagerProfessions);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TRADER =
            ModGameTests.TEST_FUNCTIONS.register("rare_trader_replaced", () -> RareGameTests::traderReplaced);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> MERCHANT =
            ModGameTests.TEST_FUNCTIONS.register("rare_merchant_trades", () -> RareGameTests::merchantTrades);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> HARNESS =
            ModGameTests.TEST_FUNCTIONS.register("rare_cloud_harness", () -> RareGameTests::cloudHarness);

    private RareGameTests() {}

    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var test : List.of(SPAWN, CONVERSION, VILLAGERS, TRADER, MERCHANT, HARNESS)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 300, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
    }

    /** Die neuen eigenen Wesen und die Vanilla-Wesen, die sie ersetzen. */
    private static Map<EntityType<?>, EntityType<? extends Mob>> pairs() {
        Map<EntityType<?>, EntityType<? extends Mob>> map = new LinkedHashMap<>();
        map.put(EntityTypes.SNIFFER, ModEntities.SNIFFER.get());
        map.put(EntityTypes.HAPPY_GHAST, ModEntities.HAPPY_GHAST.get());
        map.put(EntityTypes.WANDERING_TRADER, ModEntities.WANDERING_TRADER.get());
        map.put(EntityTypes.VILLAGER, ModEntities.VILLAGER.get());
        return map;
    }

    private static void floor(GameTestHelper helper) {
        for (int x = 0; x < 9; x++) {
            for (int z = 0; z < 9; z++) {
                helper.setBlock(x, 0, z, Blocks.GRASS_BLOCK);
            }
        }
    }

    private static AABB arena(GameTestHelper helper) {
        return new AABB(helper.absolutePos(new BlockPos(4, 1, 4))).inflate(10.0);
    }

    // =====================================================================================

    /**
     * Urwühler, Wolkenqualle und fahrender Händler erscheinen und leben; dieselben Vanilla-Tags (Geschirr, kein
     * Fallschaden, Freunde zum Folgen …) und dieselbe Kategorie wie ihre Vorbilder; Namen je Gestalt.
     */
    private static void spawn(GameTestHelper helper) {
        floor(helper);
        for (var pair : pairs().entrySet()) {
            EntityType<? extends Mob> ours = pair.getValue();
            helper.assertTrue(ours.getDefaultLootTable().isPresent(), "keine Beute-Tabelle: " + ours.getDescriptionId());
            helper.assertTrue(ours.getCategory() == pair.getKey().getCategory(), ours.getDescriptionId() + " in falscher Kategorie");
            if (pair.getKey() != EntityTypes.VILLAGER) {                   // Dorfbewohner werden nicht alt oder jung
                pair.getKey().builtInRegistryHolder().tags().forEach(tag -> helper.assertTrue(ours.builtInRegistryHolder().is(tag),
                        ours.getDescriptionId() + " fehlt im Tag " + tag.location()));
            }
        }
        PrimevalDigger digger = helper.spawnWithNoFreeWill(ModEntities.SNIFFER.get(), new BlockPos(2, 1, 2));
        digger.setLook(PrimevalDigger.Look.FROSTWUEHLER);
        helper.assertTrue(digger.getName().getString().equals(Component.translatable("entity.kingdomsatwar.sniffer.frostwuehler").getString()),
                "Name des Frostwühlers: " + digger.getName().getString());
        CloudJelly jelly = helper.spawnWithNoFreeWill(ModEntities.HAPPY_GHAST.get(), new BlockPos(6, 2, 6));
        jelly.setLook(CloudJelly.Look.REGENWOLKE);
        helper.assertTrue(jelly.getName().getString().equals(Component.translatable("entity.kingdomsatwar.happy_ghast.regenwolke").getString()),
                "Name des Regenwölkchens: " + jelly.getName().getString());
        TravelingMerchant merchant = helper.spawn(ModEntities.WANDERING_TRADER.get(), new BlockPos(2, 1, 6), EntitySpawnReason.SPAWN_ITEM_USE);
        merchant.setNoAi(true);
        helper.assertTrue(merchant.isInitialized() && merchant.hasCustomName(), "Händler ohne Namen");
        helper.runAfterDelay(10, () -> {
            for (Mob mob : List.<Mob>of(digger, jelly, merchant)) {
                helper.assertTrue(mob.isAlive() && !mob.isRemoved(), mob.getType().getDescriptionId() + " lebt nicht mehr");
            }
            helper.succeed();
        });
    }

    /**
     * Ein Schnüffler-Junges (wie aus dem Ei), ein Ghastling (wie aus dem eingeweichten Block) und ein glücklicher Ghast
     * mit Geschirr werden zu Urwühler und Wolkenqualle – mit derselben UUID, jung bleibt jung, das Geschirr bleibt.
     */
    private static void vanillaReplaced(GameTestHelper helper) {
        floor(helper);
        ServerLevel level = helper.getLevel();
        Sniffer sniffer = EntityTypes.SNIFFER.create(level, EntitySpawnReason.BREEDING);
        sniffer.setBaby(true);
        sniffer.setNoAi(true);
        place(helper, sniffer, new BlockPos(2, 1, 2));
        HappyGhast ghastling = EntityTypes.HAPPY_GHAST.create(level, EntitySpawnReason.BREEDING);
        ghastling.setBaby(true);
        ghastling.setNoAi(true);
        place(helper, ghastling, new BlockPos(6, 1, 2));
        HappyGhast ghast = EntityTypes.HAPPY_GHAST.create(level, EntitySpawnReason.SPAWN_ITEM_USE);
        ghast.setNoAi(true);
        ghast.setItemSlot(EquipmentSlot.BODY, new ItemStack(Items.HARNESS.pick(DyeColor.RED)));
        ghast.setCustomName(Component.literal("Wolke"));
        place(helper, ghast, new BlockPos(4, 2, 6));
        UUID snifferId = sniffer.getUUID();
        UUID ghastlingId = ghastling.getUUID();
        UUID ghastId = ghast.getUUID();
        helper.runAfterDelay(2, () -> {
            helper.assertTrue(level.getEntity(snifferId) instanceof PrimevalDigger digger && digger.isBaby(),
                    "statt des Schnüffler-Jungen: " + level.getEntity(snifferId));
            helper.assertTrue(level.getEntity(ghastlingId) instanceof CloudJelly jelly && jelly.isBaby(),
                    "statt des Ghastlings: " + level.getEntity(ghastlingId));
            helper.assertTrue(level.getEntity(ghastId) instanceof CloudJelly jelly && !jelly.isBaby()
                    && jelly.getItemBySlot(EquipmentSlot.BODY).is(Items.HARNESS.pick(DyeColor.RED))
                    && "Wolke".equals(jelly.getCustomName().getString()), "Wolkenqualle ohne Geschirr oder Namen: " + level.getEntity(ghastId));
            for (Mob mob : level.getEntitiesOfClass(Mob.class, arena(helper))) {
                helper.assertTrue(!pairs().containsKey(mob.getType()), "noch ein Vanilla-Wesen: " + mob.getType().getDescriptionId());
            }
            helper.succeed();
        });
    }

    /**
     * Vanilla-Dorfbewohner werden eigene Bewohner mit dem passenden Beruf: Bibliothekar → Schreiber, Geistlicher →
     * Kräuterkundige, Lederer → Gerber, Nichtsnutz → Tagelöhner; ein Namensschild bleibt, sonst gibt es einen Namen. Auch
     * ein Kind wird umgewandelt (es gibt keine Vanilla-Dorfbewohner mehr).
     */
    private static void villagerProfessions(GameTestHelper helper) {
        floor(helper);
        ServerLevel level = helper.getLevel();
        Map<UUID, VillageProfession> expected = new LinkedHashMap<>();
        Object[][] cases = {
                {VillagerProfession.LIBRARIAN, VillageProfession.SCRIBE, "Bertram", false},
                {VillagerProfession.CLERIC, VillageProfession.HERBALIST, null, false},
                {VillagerProfession.LEATHERWORKER, VillageProfession.TANNER, null, false},
                {VillagerProfession.NITWIT, VillageProfession.PEASANT, null, false},
                {VillagerProfession.FARMER, VillageProfession.FARMER, null, true}};
        UUID named = null;
        int i = 0;
        for (Object[] c : cases) {
            Villager villager = EntityTypes.VILLAGER.create(level, EntitySpawnReason.SPAWN_ITEM_USE);
            @SuppressWarnings("unchecked")
            var key = (net.minecraft.resources.ResourceKey<VillagerProfession>) c[0];
            villager.setVillagerData(villager.getVillagerData().withProfession(level.registryAccess()
                    .lookupOrThrow(Registries.VILLAGER_PROFESSION).getOrThrow(key)));
            if (c[2] != null) {
                villager.setCustomName(Component.literal((String) c[2]));
                named = villager.getUUID();
            }
            if ((Boolean) c[3]) {
                villager.setBaby(true);
            }
            villager.setNoAi(true);
            place(helper, villager, new BlockPos(1 + i * 2, 1, 4));
            expected.put(villager.getUUID(), (VillageProfession) c[1]);
            i++;
        }
        UUID bertram = named;
        helper.runAfterDelay(2, () -> {
            for (var entry : expected.entrySet()) {
                Entity found = level.getEntity(entry.getKey());
                helper.assertTrue(found instanceof VillagerEntity v && v.getProfession() == entry.getValue(),
                        "statt " + entry.getValue() + ": " + (found instanceof VillagerEntity v ? v.getProfession() : found));
                helper.assertTrue(found.hasCustomName(), "Bewohner ohne Namen");
            }
            helper.assertTrue(level.getEntity(bertram) instanceof VillagerEntity v && "Bertram".equals(v.getName().getString()),
                    "Namensschild verloren");
            helper.assertTrue(level.getEntitiesOfClass(Villager.class, arena(helper)).isEmpty(), "noch ein Vanilla-Dorfbewohner");
            helper.assertTrue(FarmAnimalConversion.professionFor("weaponsmith") == VillageProfession.WEAPONSMITH
                    && FarmAnimalConversion.professionFor("fisherman") == VillageProfession.FISHER
                    && FarmAnimalConversion.professionFor("none") == VillageProfession.PEASANT, "Berufe falsch zugeordnet");
            helper.succeed();
        });
    }

    /**
     * Der fahrende Händler (Vanilla) bekommt Aufenthaltsdauer und Ziel erst nach dem Erscheinen – am Ende des Ticks
     * steht der eigene Händler mit derselben UUID, derselben Aufenthaltsdauer und demselben Ziel da.
     */
    private static void traderReplaced(GameTestHelper helper) {
        floor(helper);
        ServerLevel level = helper.getLevel();
        WanderingTrader trader = EntityTypes.WANDERING_TRADER.create(level, EntitySpawnReason.EVENT);
        trader.setNoAi(true);
        place(helper, trader, new BlockPos(4, 1, 4));
        BlockPos target = helper.absolutePos(new BlockPos(1, 1, 1));
        trader.setDespawnDelay(30000);                                    // wie beim Erscheinen durch Vanilla: erst danach
        trader.setWanderTarget(target);
        UUID id = trader.getUUID();
        helper.runAfterDelay(3, () -> {
            helper.assertTrue(level.getEntity(id) instanceof TravelingMerchant, "statt des Händlers: " + level.getEntity(id));
            TravelingMerchant merchant = (TravelingMerchant) level.getEntity(id);
            helper.assertTrue(merchant.getDespawnDelay() > 29000, "Aufenthaltsdauer verloren: " + merchant.getDespawnDelay());
            net.minecraft.world.level.storage.TagValueOutput out = net.minecraft.world.level.storage.TagValueOutput.createWithContext(
                    net.minecraft.util.ProblemReporter.DISCARDING, level.registryAccess());
            merchant.saveWithoutId(out);
            helper.assertTrue(out.buildResult().read("wander_target", BlockPos.CODEC).map(target::equals).orElse(false), "Ziel verloren");
            helper.assertTrue(merchant.isInitialized() && merchant.hasCustomName(), "Händler ohne Namen");
            helper.assertTrue(level.getEntitiesOfClass(WanderingTrader.class, arena(helper), t -> !(t instanceof TravelingMerchant)).isEmpty(),
                    "noch ein Vanilla-Händler");
            helper.succeed();
        });
    }

    /**
     * Der Händler handelt gegen Münzen: Setzlinge, Blumen, Farben und Waren aus fernen Ländern, dazu eine Kostbarkeit;
     * er kauft Proviant. Dieselben Waren bei jedem Blick in die Kiepe; nach genug Handel macht er Pause.
     */
    private static void merchantTrades(GameTestHelper helper) {
        floor(helper);
        TravelingMerchant merchant = helper.spawn(ModEntities.WANDERING_TRADER.get(), new BlockPos(4, 1, 4), EntitySpawnReason.SPAWN_ITEM_USE);
        merchant.setNoAi(true);
        List<Trading.Offer> offers = merchant.baseOffers();
        helper.assertTrue(offers.size() >= 18, "zu wenige Waren: " + offers.size());
        helper.assertTrue(offers.stream().anyMatch(o -> o.get().is(ItemTags.SAPLINGS)), "keine Setzlinge");
        helper.assertTrue(offers.stream().anyMatch(o -> o.get().is(net.minecraft.tags.BlockItemTags.SMALL_FLOWERS.item())), "keine Blumen");
        helper.assertTrue(offers.stream().anyMatch(Trading.Offer::selling), "kauft nichts an");
        helper.assertTrue(offers.stream().allMatch(o -> o.selling() ? o.getCoins() > 0 : o.giveCoins() > 0), "Angebot ohne Preis");
        List<Trading.Offer> again = merchant.baseOffers();
        for (int i = 0; i < offers.size(); i++) {
            helper.assertTrue(ItemStack.matches(offers.get(i).get(), again.get(i).get()), "Waren wechseln beim zweiten Blick");
        }
        helper.assertTrue(merchant.canTrade() && !merchant.isClosed(), "Händler handelt nicht");
        for (int i = 0; i < Trading.limit(); i++) {
            merchant.recordTrade(Trading.limit(), 60_000L);
        }
        helper.assertTrue(merchant.isClosed(), "keine Pause nach " + Trading.limit() + " Käufen");
        helper.assertTrue(merchant.skinTexture().endsWith("traveler_" + (merchant.getVariant() + 1) + ".png"), "falsche Haut: " + merchant.skinTexture());
        helper.succeed();
    }

    /** Die Wolkenqualle trägt Geschirr (wie der glückliche Ghast) – andere Tiere nicht. */
    private static void cloudHarness(GameTestHelper helper) {
        floor(helper);
        CloudJelly jelly = helper.spawnWithNoFreeWill(ModEntities.HAPPY_GHAST.get(), new BlockPos(4, 2, 4));
        ItemStack harness = new ItemStack(Items.HARNESS.pick(DyeColor.BLUE));
        helper.assertTrue(jelly.isEquippableInSlot(harness, EquipmentSlot.BODY), "Wolkenqualle nimmt kein Geschirr");
        PrimevalDigger digger = helper.spawnWithNoFreeWill(ModEntities.SNIFFER.get(), new BlockPos(1, 1, 1));
        helper.assertFalse(digger.isEquippableInSlot(harness, EquipmentSlot.BODY), "der Urwühler nimmt Geschirr");
        jelly.setItemSlot(EquipmentSlot.BODY, harness);
        helper.assertTrue(jelly.isWearingBodyArmor() || !jelly.getItemBySlot(EquipmentSlot.BODY).isEmpty(), "Geschirr nicht angelegt");
        helper.succeed();
    }

    private static void place(GameTestHelper helper, Mob mob, BlockPos relative) {
        BlockPos pos = helper.absolutePos(relative);
        mob.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0f, 0.0f);
        helper.getLevel().addFreshEntity(mob);
    }
}
