package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.entity.creature.golem.CopperServant;
import io.github.sirschrolli.kingdomsatwar.entity.creature.golem.HouseSprite;
import io.github.sirschrolli.kingdomsatwar.entity.creature.golem.IronSentinel;
import io.github.sirschrolli.kingdomsatwar.entity.creature.golem.Snowman;
import io.github.sirschrolli.kingdomsatwar.entity.creature.golem.VillageSentinels;
import io.github.sirschrolli.kingdomsatwar.entity.villager.VillageProfession;
import io.github.sirschrolli.kingdomsatwar.entity.villager.VillagerEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import java.util.HashMap;
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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.golem.CopperGolem;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.animal.golem.SnowGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/** Tests der eigenen Golems und Wichtel (E4d). */
final class GolemGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SPAWN =
            ModGameTests.TEST_FUNCTIONS.register("golem_spawn", () -> GolemGameTests::spawn);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CONVERSION =
            ModGameTests.TEST_FUNCTIONS.register("golem_vanilla_replaced", () -> GolemGameTests::vanillaReplaced);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> BUILT_IRON =
            ModGameTests.TEST_FUNCTIONS.register("golem_built_iron", () -> GolemGameTests::builtIron);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> BUILT_SNOW =
            ModGameTests.TEST_FUNCTIONS.register("golem_built_snow", () -> GolemGameTests::builtSnow);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> BUILT_COPPER =
            ModGameTests.TEST_FUNCTIONS.register("golem_built_copper", () -> GolemGameTests::builtCopper);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LOOKS =
            ModGameTests.TEST_FUNCTIONS.register("golem_looks", () -> GolemGameTests::looks);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> DEFEND =
            ModGameTests.TEST_FUNCTIONS.register("golem_defends_villagers", () -> GolemGameTests::defendsVillagers);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SENTINEL =
            ModGameTests.TEST_FUNCTIONS.register("golem_village_sentinel", () -> GolemGameTests::villageSentinel);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SPRITE_COPY =
            ModGameTests.TEST_FUNCTIONS.register("golem_sprite_duplicate", () -> GolemGameTests::spriteDuplicate);

    private GolemGameTests() {}

    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var test : List.of(SPAWN, CONVERSION, BUILT_IRON, BUILT_SNOW, BUILT_COPPER, LOOKS, DEFEND, SENTINEL, SPRITE_COPY)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 300, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
    }

    /** Eigene Golems und Wichtel und die Vanilla-Wesen, die sie ersetzen. */
    private static Map<EntityType<?>, EntityType<? extends Mob>> pairs() {
        Map<EntityType<?>, EntityType<? extends Mob>> map = new LinkedHashMap<>();
        map.put(EntityTypes.IRON_GOLEM, ModEntities.IRON_GOLEM.get());
        map.put(EntityTypes.SNOW_GOLEM, ModEntities.SNOW_GOLEM.get());
        map.put(EntityTypes.COPPER_GOLEM, ModEntities.COPPER_GOLEM.get());
        map.put(EntityTypes.ALLAY, ModEntities.ALLAY.get());
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
        return new AABB(helper.absolutePos(new BlockPos(4, 1, 4))).inflate(8.0);
    }

    private static <T extends Mob> T reload(ServerLevel level, T mob, EntityType<T> type) {
        TagValueOutput out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
        mob.saveWithoutId(out);
        CompoundTag tag = out.buildResult();
        T loaded = type.create(level, EntitySpawnReason.LOAD);
        loaded.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), tag));
        return loaded;
    }

    // =====================================================================================

    /**
     * Alle vier erscheinen und leben; Beute wie bei Vanilla, dieselben Vanilla-Tags und dieselbe Kategorie; die eigenen
     * Dorfbewohner bekommen Mohn geschenkt, Kupferknechte eine Blume auf den Blitzableiter; Namen je Gestalt.
     */
    private static void spawn(GameTestHelper helper) {
        floor(helper);
        ServerLevel level = helper.getLevel();
        var registries = level.getServer().reloadableRegistries();
        int i = 0;
        List<Mob> mobs = new java.util.ArrayList<>();
        for (var pair : pairs().entrySet()) {
            EntityType<? extends Mob> ours = pair.getValue();
            ResourceKey<LootTable> key = ours.getDefaultLootTable().orElseThrow();
            helper.assertTrue(registries.getLootTable(key) != LootTable.EMPTY || pair.getKey() == EntityTypes.ALLAY,
                    "Beute fehlt: " + key.identifier());
            pair.getKey().builtInRegistryHolder().tags().forEach(tag -> helper.assertTrue(ours.builtInRegistryHolder().is(tag),
                    ours.getDescriptionId() + " fehlt im Tag " + tag.location()));
            helper.assertTrue(ours.getCategory() == pair.getKey().getCategory(), ours.getDescriptionId() + " in falscher Kategorie");
            Mob mob = helper.spawnWithNoFreeWill(ours, new BlockPos(1 + i * 2, 1, 4));
            mob.setPersistenceRequired();
            mobs.add(mob);
            i++;
        }
        helper.assertTrue(ModEntities.VILLAGER.get().builtInRegistryHolder().is(EntityTypeTags.CANDIDATE_FOR_IRON_GOLEM_GIFT),
                "Eisenwächter schenken den Dorfbewohnern keinen Mohn");
        helper.assertTrue(ModEntities.COPPER_GOLEM.get().builtInRegistryHolder().is(EntityTypeTags.ACCEPTS_IRON_GOLEM_GIFT),
                "Kupferknechte nehmen keine Blume an");
        IronSentinel sentinel = (IronSentinel) mobs.getFirst();
        sentinel.setLook(IronSentinel.Look.MOOSWAECHTER);
        helper.assertTrue(sentinel.getName().getString().equals(Component.translatable("entity.kingdomsatwar.iron_golem.mooswaechter").getString()),
                "Name des Mooswächters: " + sentinel.getName().getString());
        HouseSprite sprite = (HouseSprite) mobs.get(3);
        sprite.setLook(HouseSprite.Look.PILZWICHTEL);
        helper.assertTrue(sprite.getName().getString().equals(Component.translatable("entity.kingdomsatwar.allay.pilzwichtel").getString()),
                "Name des Pilzwichtels: " + sprite.getName().getString());
        helper.runAfterDelay(10, () -> {
            for (Mob mob : mobs) {
                helper.assertTrue(mob.isAlive() && !mob.isRemoved(), mob.getType().getDescriptionId() + " lebt nicht mehr");
            }
            helper.succeed();
        });
    }

    /** Erscheint ein Vanilla-Golem oder -Hilfsgeist, steht an seiner Stelle das eigene Wesen – mit derselben UUID und demselben Namen. */
    private static void vanillaReplaced(GameTestHelper helper) {
        floor(helper);
        ServerLevel level = helper.getLevel();
        Map<UUID, EntityType<?>> expected = new HashMap<>();
        UUID ironId = null;
        int i = 0;
        for (var pair : pairs().entrySet()) {
            Entity entity = pair.getKey().create(level, EntitySpawnReason.SPAWN_ITEM_USE);
            helper.assertTrue(entity instanceof Mob, "kein " + pair.getKey().getDescriptionId());
            Mob mob = (Mob) entity;
            mob.setCustomName(Component.literal("Probe " + i));
            mob.setNoAi(true);
            if (mob instanceof IronGolem golem) {
                golem.setPlayerCreated(true);
                ironId = golem.getUUID();
            }
            BlockPos pos = helper.absolutePos(new BlockPos(1 + i * 2, 1, 4));
            mob.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0f, 0.0f);
            expected.put(mob.getUUID(), pair.getValue());
            level.addFreshEntity(mob);
            i++;
        }
        UUID iron = ironId;
        helper.runAfterDelay(2, () -> {
            for (var entry : expected.entrySet()) {
                Entity found = level.getEntity(entry.getKey());
                helper.assertTrue(found != null && found.getType() == entry.getValue(),
                        "statt " + entry.getValue().getDescriptionId() + " steht da " + (found == null ? "nichts" : found.getType().getDescriptionId()));
                helper.assertTrue(found.hasCustomName() && found.getCustomName().getString().startsWith("Probe"), "Name verloren");
            }
            helper.assertTrue(level.getEntity(iron) instanceof IronSentinel sentinel && sentinel.isPlayerCreated(),
                    "der gebaute Eisenwächter weiß nicht mehr, dass ihn ein Spieler gebaut hat");
            for (Mob mob : level.getEntitiesOfClass(Mob.class, arena(helper))) {
                helper.assertTrue(!pairs().containsKey(mob.getType()), "noch ein Vanilla-Wesen: " + mob.getType().getDescriptionId());
            }
            helper.succeed();
        });
    }

    /** Vier Eisenblöcke und ein geschnitzter Kürbis ergeben einen (vom Spieler gebauten) Eisenwächter. */
    private static void builtIron(GameTestHelper helper) {
        floor(helper);
        helper.setBlock(4, 1, 4, Blocks.IRON_BLOCK);
        helper.setBlock(4, 2, 4, Blocks.IRON_BLOCK);
        helper.setBlock(3, 2, 4, Blocks.IRON_BLOCK);
        helper.setBlock(5, 2, 4, Blocks.IRON_BLOCK);
        helper.setBlock(4, 3, 4, Blocks.CARVED_PUMPKIN);
        helper.succeedWhen(() -> {
            var golems = helper.getLevel().getEntitiesOfClass(IronGolem.class, arena(helper));
            golems.forEach(golem -> golem.setNoAi(true));                // nicht in die Nachbartests laufen
            helper.assertTrue(golems.size() == 1 && golems.getFirst() instanceof IronSentinel, "kein Eisenwächter: " + golems);
            helper.assertTrue(golems.getFirst().isPlayerCreated(), "nicht als gebaut markiert");
            helper.assertBlockNotPresent(Blocks.IRON_BLOCK, new BlockPos(4, 2, 4));
        });
    }

    /** Zwei Schneeblöcke und ein geschnitzter Kürbis ergeben einen Schneemann mit Kürbis auf dem Kopf. */
    private static void builtSnow(GameTestHelper helper) {
        floor(helper);
        helper.setBlock(4, 1, 4, Blocks.SNOW_BLOCK);
        helper.setBlock(4, 2, 4, Blocks.SNOW_BLOCK);
        helper.setBlock(4, 3, 4, Blocks.CARVED_PUMPKIN);
        helper.succeedWhen(() -> {
            var golems = helper.getLevel().getEntitiesOfClass(SnowGolem.class, arena(helper));
            golems.forEach(golem -> golem.setNoAi(true));                // keine Schneebälle in die Nachbartests
            helper.assertTrue(golems.size() == 1 && golems.getFirst() instanceof Snowman, "kein Schneemann: " + golems);
            helper.assertTrue(golems.getFirst().hasPumpkin(), "ohne Kürbis");
        });
    }

    /**
     * Ein angelaufener Kupferblock und ein geschnitzter Kürbis ergeben einen Kupferknecht – angelaufen wie der Block
     * (die Verwitterung bekommt der Golem erst nach dem Erscheinen); der Block wird zur Kupfertruhe.
     */
    private static void builtCopper(GameTestHelper helper) {
        floor(helper);
        helper.setBlock(4, 1, 4, Blocks.COPPER_BLOCK.weathering().exposed());
        helper.setBlock(4, 2, 4, Blocks.CARVED_PUMPKIN);
        helper.succeedWhen(() -> {
            var golems = helper.getLevel().getEntitiesOfClass(CopperGolem.class, arena(helper));
            golems.forEach(golem -> golem.setNoAi(true));
            helper.assertTrue(golems.size() == 1 && golems.getFirst() instanceof CopperServant, "kein Kupferknecht: " + golems);
            helper.assertTrue(golems.getFirst().getWeatherState() == WeatheringCopper.WeatherState.EXPOSED,
                    "Verwitterung nicht übernommen: " + golems.getFirst().getWeatherState());
            helper.assertTrue(helper.getBlockState(new BlockPos(4, 1, 4)).getBlock() instanceof net.minecraft.world.level.block.CopperChestBlock,
                    "keine Kupfertruhe: " + helper.getBlockState(new BlockPos(4, 1, 4)));
        });
    }

    /** Die Gestalt des Eisenwächters richtet sich nach dem Land; Gestalten bleiben beim Speichern und Laden erhalten. */
    private static void looks(GameTestHelper helper) {
        floor(helper);
        ServerLevel level = helper.getLevel();
        var biomes = level.registryAccess().lookupOrThrow(Registries.BIOME);
        helper.assertTrue(IronSentinel.lookFor(biomes.getOrThrow(Biomes.SNOWY_PLAINS)) == IronSentinel.Look.FROSTWAECHTER, "Schnee: kein Frostwächter");
        helper.assertTrue(IronSentinel.lookFor(biomes.getOrThrow(Biomes.JUNGLE)) == IronSentinel.Look.MOOSWAECHTER, "Dschungel: kein Mooswächter");
        helper.assertTrue(IronSentinel.lookFor(biomes.getOrThrow(Biomes.SWAMP)) == IronSentinel.Look.MOOSWAECHTER, "Sumpf: kein Mooswächter");
        helper.assertTrue(IronSentinel.lookFor(biomes.getOrThrow(Biomes.BEACH)) == IronSentinel.Look.ROSTWAECHTER, "Strand: kein Rostwächter");
        helper.assertTrue(IronSentinel.lookFor(biomes.getOrThrow(Biomes.PLAINS)) == IronSentinel.Look.SCHMIEDEEISEN, "Ebene: kein Schmiedeeisen");
        IronSentinel sentinel = helper.spawnWithNoFreeWill(ModEntities.IRON_GOLEM.get(), new BlockPos(2, 1, 2));
        sentinel.setLook(IronSentinel.Look.ROSTWAECHTER);
        Snowman snowman = helper.spawnWithNoFreeWill(ModEntities.SNOW_GOLEM.get(), new BlockPos(5, 1, 2));
        snowman.setLook(Snowman.Look.GELB);
        HouseSprite sprite = helper.spawnWithNoFreeWill(ModEntities.ALLAY.get(), new BlockPos(7, 1, 2));
        sprite.setLook(HouseSprite.Look.STERNWICHTEL);
        CopperServant servant = helper.spawnWithNoFreeWill(ModEntities.COPPER_GOLEM.get(), new BlockPos(4, 1, 6));
        servant.setWeatherState(WeatheringCopper.WeatherState.WEATHERED);
        helper.assertTrue(reload(level, sentinel, ModEntities.IRON_GOLEM.get()).getLook() == IronSentinel.Look.ROSTWAECHTER, "Rostwächter verloren");
        helper.assertTrue(reload(level, snowman, ModEntities.SNOW_GOLEM.get()).getLook() == Snowman.Look.GELB, "gelber Schal verloren");
        helper.assertTrue(reload(level, sprite, ModEntities.ALLAY.get()).getLook() == HouseSprite.Look.STERNWICHTEL, "Sternwichtel verloren");
        helper.assertTrue(reload(level, servant, ModEntities.COPPER_GOLEM.get()).getWeatherState() == WeatheringCopper.WeatherState.WEATHERED,
                "Verwitterung verloren");
        helper.succeed();
    }

    /**
     * Wer einen Dorfbewohner angreift, wird zum Ziel des Eisenwächters: ein Monster immer, ein Spieler nur bei einem
     * Wächter des Dorfs – ein von Spielern gebauter Eisenwächter greift keine Spieler an.
     */
    private static void defendsVillagers(GameTestHelper helper) {
        floor(helper);
        ServerLevel level = helper.getLevel();
        level.getServer().setDifficulty(Difficulty.HARD, true);
        VillagerEntity villager = helper.spawnWithNoFreeWill(ModEntities.VILLAGER.get(), new BlockPos(4, 1, 4));
        villager.setup(VillageProfession.FARMER, 0, "Probe", helper.absolutePos(new BlockPos(4, 1, 4)), "Testdorf", null, null, 1L);
        IronSentinel village = helper.spawn(ModEntities.IRON_GOLEM.get(), new BlockPos(1, 1, 1));
        IronSentinel built = helper.spawn(ModEntities.IRON_GOLEM.get(), new BlockPos(7, 1, 1));
        built.setPlayerCreated(true);
        village.setHomeTo(helper.absolutePos(new BlockPos(4, 1, 4)), 4);        // in der eigenen Arena bleiben
        built.setHomeTo(helper.absolutePos(new BlockPos(4, 1, 4)), 4);
        var zombie = helper.spawnWithNoFreeWill(ModEntities.ZOMBIE.get(), new BlockPos(4, 1, 7));
        zombie.setNoAi(true);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.snapTo(helper.absoluteVec(new net.minecraft.world.phys.Vec3(6.5, 1, 6.5)));
        villager.hurtServer(level, level.damageSources().playerAttack(player), 1.0f);
        helper.runAfterDelay(10, () -> {
            helper.assertTrue(village.getTarget() == player, "der Wächter des Dorfs stellt den Spieler nicht: " + village.getTarget());
            helper.assertTrue(built.getTarget() != player, "der gebaute Eisenwächter greift den Spieler an");
            village.setTarget(null);
            built.setTarget(null);
            villager.setLastHurtByMob(null);
            villager.setInvulnerableTime(0);
            villager.hurtServer(level, level.damageSources().mobAttack(zombie), 1.0f);
            helper.runAfterDelay(10, () -> {
                helper.assertTrue(built.getTarget() == zombie, "der gebaute Eisenwächter verteidigt den Bewohner nicht vor Monstern: "
                        + built.getTarget());
                village.discard();
                built.discard();
                helper.succeed();
            });
        });
    }

    /** Die Dorfwache holt den Eisenwächter herbei – an eine freie Stelle und nur einmal je Dorf. */
    private static void villageSentinel(GameTestHelper helper) {
        floor(helper);
        ServerLevel level = helper.getLevel();
        BlockPos post = helper.absolutePos(new BlockPos(4, 1, 4));
        helper.setBlock(4, 1, 4, Blocks.OAK_FENCE);                      // der Posten selbst ist belegt
        VillageSentinels.summon(level, post, 6.0);                       // (Eisenwächter benachbarter Tests zählen nicht)
        VillageSentinels.summon(level, post, 6.0);
        var golems = level.getEntitiesOfClass(IronSentinel.class, arena(helper));
        helper.assertTrue(golems.size() == 1, "Eisenwächter im Dorf: " + golems.size());
        IronSentinel golem = golems.getFirst();
        helper.assertTrue(golem.hasHome() && golem.getHomePosition().equals(post), "der Eisenwächter bleibt nicht im Dorf");
        helper.assertTrue(!golem.isPlayerCreated(), "der Wächter des Dorfs gilt als gebaut");
        helper.assertTrue(level.getBlockState(golem.blockPosition()).isAir() && !golem.blockPosition().equals(post),
                "der Eisenwächter steht im Zaun");
        helper.succeed();
    }

    /** Ein verdoppelter Wichtel trägt dieselbe Mütze wie sein Vorbild (der Vanilla-Hilfsgeist erscheint genau an dessen Stelle). */
    private static void spriteDuplicate(GameTestHelper helper) {
        floor(helper);
        ServerLevel level = helper.getLevel();
        HouseSprite parent = helper.spawnWithNoFreeWill(ModEntities.ALLAY.get(), new BlockPos(4, 2, 4));
        parent.setLook(HouseSprite.Look.LAUBWICHTEL);
        var copy = EntityTypes.ALLAY.create(level, EntitySpawnReason.BREEDING);
        helper.assertTrue(copy != null, "kein Hilfsgeist");
        copy.snapTo(parent.getX(), parent.getY(), parent.getZ(), 0.0f, 0.0f);
        UUID id = copy.getUUID();
        level.addFreshEntity(copy);
        helper.runAfterDelay(2, () -> {
            helper.assertTrue(level.getEntity(id) instanceof HouseSprite sprite && sprite.getLook() == HouseSprite.Look.LAUBWICHTEL,
                    "der neue Wichtel trägt eine andere Mütze: " + level.getEntity(id));
            helper.succeed();
        });
    }
}
