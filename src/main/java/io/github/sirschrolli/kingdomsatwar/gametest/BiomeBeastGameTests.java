package io.github.sirschrolli.kingdomsatwar.gametest;

import com.mojang.authlib.GameProfile;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.entity.creature.biome.ButterflyEntity;
import io.github.sirschrolli.kingdomsatwar.entity.creature.biome.EmberSalamanderEntity;
import io.github.sirschrolli.kingdomsatwar.entity.creature.biome.FlamingoEntity;
import io.github.sirschrolli.kingdomsatwar.entity.creature.biome.GriffinEntity;
import io.github.sirschrolli.kingdomsatwar.entity.creature.biome.MireSnapperEntity;
import io.github.sirschrolli.kingdomsatwar.entity.creature.biome.PrimevalElkEntity;
import io.github.sirschrolli.kingdomsatwar.entity.creature.biome.SnowLeopardEntity;
import io.github.sirschrolli.kingdomsatwar.entity.creature.biome.StormGullEntity;
import io.github.sirschrolli.kingdomsatwar.registry.BiomeBeasts;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModTags;
import io.github.sirschrolli.kingdomsatwar.worldgen.overworld.OverworldBiomes;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attributes;
import io.github.sirschrolli.kingdomsatwar.entity.creature.farm.Hare;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Tests der Tiere der eigenen Biome (0.85): Grundlagen (Werte, Beute, Spawn-Eier, Biome), der Schneeleopard (zähmen, jagen),
 * der Urelch (Truhe, Sattel, reiten; der Bulle stürmt auf seinen Angreifer), die Sturmmöwe (fliegt auf, klaut Fisch aus der
 * Hand) und der Greif (jagt im Sturzflug, wehrt sich am Boden).
 */
final class BiomeBeastGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> BASICS =
            ModGameTests.TEST_FUNCTIONS.register("biome_beast_basics", () -> BiomeBeastGameTests::basics);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LEOPARD_TAME =
            ModGameTests.TEST_FUNCTIONS.register("biome_beast_leopard_taming", () -> BiomeBeastGameTests::leopardTaming);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LEOPARD_HUNT =
            ModGameTests.TEST_FUNCTIONS.register("biome_beast_leopard_hunt", () -> BiomeBeastGameTests::leopardHunt);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ELK_RIDE =
            ModGameTests.TEST_FUNCTIONS.register("biome_beast_elk_ride", () -> BiomeBeastGameTests::elkRide);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ELK_CHARGE =
            ModGameTests.TEST_FUNCTIONS.register("biome_beast_elk_charge", () -> BiomeBeastGameTests::elkCharge);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> GULL_FLEE =
            ModGameTests.TEST_FUNCTIONS.register("biome_beast_gull_flee", () -> BiomeBeastGameTests::gullFlee);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> GULL_STEAL =
            ModGameTests.TEST_FUNCTIONS.register("biome_beast_gull_steal", () -> BiomeBeastGameTests::gullSteal);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> GRIFFIN_HUNT =
            ModGameTests.TEST_FUNCTIONS.register("biome_beast_griffin_hunt", () -> BiomeBeastGameTests::griffinHunt);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> GRIFFIN_DEFEND =
            ModGameTests.TEST_FUNCTIONS.register("biome_beast_griffin_defend", () -> BiomeBeastGameTests::griffinDefend);
    // 0.86
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> BASICS_86 =
            ModGameTests.TEST_FUNCTIONS.register("biome_beast_basics_86", () -> BiomeBeastGameTests::basics86);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SALAMANDER_LAVA =
            ModGameTests.TEST_FUNCTIONS.register("biome_beast_salamander_lava", () -> BiomeBeastGameTests::salamanderLava);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SALAMANDER_BURNS =
            ModGameTests.TEST_FUNCTIONS.register("biome_beast_salamander_burns", () -> BiomeBeastGameTests::salamanderBurns);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SNAPPER_SNAP =
            ModGameTests.TEST_FUNCTIONS.register("biome_beast_snapper_snap", () -> BiomeBeastGameTests::snapperSnap);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SNAPPER_SHELL =
            ModGameTests.TEST_FUNCTIONS.register("biome_beast_snapper_shell", () -> BiomeBeastGameTests::snapperShell);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FLAMINGO_WADE =
            ModGameTests.TEST_FUNCTIONS.register("biome_beast_flamingo_wade", () -> BiomeBeastGameTests::flamingoWade);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> BUTTERFLY_PERCH =
            ModGameTests.TEST_FUNCTIONS.register("biome_beast_butterfly_perch", () -> BiomeBeastGameTests::butterflyPerch);

    private BiomeBeastGameTests() {}

    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        event.registerTest(BASICS.getId(), new FunctionGameTestInstance(BASICS.key(),
                new TestData<>(environment, Level.OVERWORLD, ARENA, 100, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        for (var test : List.of(LEOPARD_TAME, ELK_RIDE, GULL_FLEE, BASICS_86, SALAMANDER_LAVA, SALAMANDER_BURNS, SNAPPER_SHELL, FLAMINGO_WADE)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 200, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
        for (var test : List.of(LEOPARD_HUNT, ELK_CHARGE, GULL_STEAL, GRIFFIN_HUNT, GRIFFIN_DEFEND, SNAPPER_SNAP, BUTTERFLY_PERCH)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 900, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
    }

    // =====================================================================================
    // Hilfen
    // =====================================================================================

    /** Boden und eine Mauer rundum (drei hoch), damit niemand davonläuft; darüber frei. */
    private static void pen(GameTestHelper helper, Block ground) {
        for (int x = 0; x < 9; x++) {
            for (int z = 0; z < 9; z++) {
                helper.setBlock(x, 0, z, ground);
                boolean edge = x == 0 || z == 0 || x == 8 || z == 8;
                for (int y = 1; y < 6; y++) {
                    helper.setBlock(x, y, z, edge && y < 4 ? Blocks.GLASS : Blocks.AIR);
                }
            }
        }
    }

    /** Die eigene Arena, nach oben weit (fliegende Tiere); die Nachbartests liegen 14 Blöcke weiter. */
    private static AABB area(GameTestHelper helper) {
        return new AABB(Vec3.atLowerCornerOf(helper.absolutePos(new BlockPos(-2, -1, -2))),
                Vec3.atLowerCornerOf(helper.absolutePos(new BlockPos(11, 40, 11))));
    }

    private static void cleanup(GameTestHelper helper, Entity... extra) {
        for (Entity e : helper.getLevel().getEntitiesOfClass(Entity.class, area(helper), e -> !(e instanceof Player))) {
            e.discard();
        }
        for (Entity e : extra) {
            e.discard();
        }
    }

    private static ServerPlayer player(GameTestHelper helper, BlockPos pos, float yaw) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        return place(helper, player, pos, yaw);
    }

    /** Ein Spieler, der wirklich im Überlebensmodus ist (der Test-Spieler des Frameworks gilt immer als kreativ). */
    private static ServerPlayer survivor(GameTestHelper helper, BlockPos pos, float yaw) {
        ServerLevel level = helper.getLevel();
        CommonListenerCookie cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "test-survivor"), false);
        ServerPlayer player = new ServerPlayer(level.getServer(), level, cookie.gameProfile(), cookie.clientInformation()) {
            @Override
            public GameType gameMode() {
                return GameType.SURVIVAL;
            }
        };
        Connection connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        level.getServer().getPlayerList().placeNewPlayer(connection, player, cookie);
        return place(helper, player, pos, yaw);
    }

    private static ServerPlayer place(GameTestHelper helper, ServerPlayer player, BlockPos pos, float yaw) {
        Vec3 at = helper.absoluteVec(Vec3.atBottomCenterOf(pos));
        player.snapTo(at.x, at.y, at.z, yaw, 0.0f);
        player.setYHeadRot(yaw);
        return player;
    }

    private static void remove(GameTestHelper helper, ServerPlayer player) {
        helper.getLevel().getServer().getPlayerList().remove(player);
    }

    private static void expect(GameTestHelper helper, HolderLookup.RegistryLookup<Biome> biomes, ResourceKey<Biome> biome, TagKey<Biome> tag,
            boolean in) {
        helper.assertTrue(biomes.getOrThrow(biome).is(tag) == in, biome.identifier().getPath() + (in ? " fehlt in " : " gehört nicht in ")
                + tag.location().getPath());
    }

    // =====================================================================================
    // Grundlagen
    // =====================================================================================

    /** Alle vier sind Tiere mit Beutetabelle und Spawn-Ei, mit ihren Werten, und leben in ihren Biomen (und nur dort). */
    private static void basics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        List<EntityType<? extends Mob>> types = List.of(BiomeBeasts.SNOW_LEOPARD.get(), BiomeBeasts.PRIMEVAL_ELK.get(),
                BiomeBeasts.STORM_GULL.get(), BiomeBeasts.GRIFFIN.get());
        double[] health = {26.0, 48.0, 6.0, 40.0};
        for (int i = 0; i < types.size(); i++) {
            EntityType<? extends Mob> type = types.get(i);
            String name = BuiltInRegistries.ENTITY_TYPE.getKey(type).getPath();
            helper.assertTrue(type.getCategory() == MobCategory.CREATURE, name + ": keine Tiergruppe");
            helper.assertTrue(type.getDefaultLootTable().isPresent(), name + ": keine Beute");
            helper.assertTrue(BuiltInRegistries.ITEM.containsKey(KingdomsAtWar.id(name + "_spawn_egg")), name + ": kein Spawn-Ei");
            Mob mob = helper.spawnWithNoFreeWill(type, new BlockPos(1 + i * 2, 1, 4));
            helper.assertTrue(Math.abs(mob.getAttributeValue(Attributes.MAX_HEALTH) - health[i]) < 1.0e-3,
                    name + ": Lebenspunkte " + mob.getAttributeValue(Attributes.MAX_HEALTH) + " statt " + health[i]);
        }
        var biomes = level.registryAccess().lookupOrThrow(Registries.BIOME);
        expect(helper, biomes, OverworldBiomes.FROST_PEAKS, ModTags.Biomes.HAS_SNOW_LEOPARDS, true);
        expect(helper, biomes, Biomes.SNOWY_SLOPES, ModTags.Biomes.HAS_SNOW_LEOPARDS, true);
        expect(helper, biomes, Biomes.PLAINS, ModTags.Biomes.HAS_SNOW_LEOPARDS, false);
        expect(helper, biomes, OverworldBiomes.PRIMEVAL_FOREST, ModTags.Biomes.HAS_PRIMEVAL_ELKS, true);
        expect(helper, biomes, Biomes.OLD_GROWTH_SPRUCE_TAIGA, ModTags.Biomes.HAS_PRIMEVAL_ELKS, true);
        expect(helper, biomes, Biomes.DESERT, ModTags.Biomes.HAS_PRIMEVAL_ELKS, false);
        expect(helper, biomes, OverworldBiomes.STORM_COAST, ModTags.Biomes.HAS_STORM_GULLS, true);
        expect(helper, biomes, Biomes.STONY_SHORE, ModTags.Biomes.HAS_STORM_GULLS, true);
        expect(helper, biomes, Biomes.FOREST, ModTags.Biomes.HAS_STORM_GULLS, false);
        expect(helper, biomes, OverworldBiomes.ROCK_GORGES, ModTags.Biomes.HAS_GRIFFINS, true);
        expect(helper, biomes, Biomes.WINDSWEPT_SAVANNA, ModTags.Biomes.HAS_GRIFFINS, true);
        expect(helper, biomes, Biomes.SWAMP, ModTags.Biomes.HAS_GRIFFINS, false);
        helper.assertTrue(StormGullEntity.isStealable(new ItemStack(Items.COD)), "Kabeljau ist nicht zu stehlen");
        helper.assertFalse(StormGullEntity.isStealable(new ItemStack(Items.PUFFERFISH)), "Kugelfisch wird gestohlen");
        helper.assertFalse(StormGullEntity.isStealable(new ItemStack(Items.BREAD)), "Brot wird gestohlen");
        cleanup(helper);
        helper.succeed();
    }

    // =====================================================================================
    // Schneeleopard
    // =====================================================================================

    /** Mit rohem Hammelfleisch lässt er sich zähmen (nicht beim ersten Versuch sicher, aber bald). */
    private static void leopardTaming(GameTestHelper helper) {
        pen(helper, Blocks.SNOW_BLOCK);
        SnowLeopardEntity leopard = helper.spawnWithNoFreeWill(BiomeBeasts.SNOW_LEOPARD.get(), new BlockPos(4, 1, 5));
        ServerPlayer owner = player(helper, new BlockPos(4, 1, 3), 0.0f);
        for (int i = 0; i < 60 && !leopard.isTame(); i++) {
            owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.MUTTON, 4));
            leopard.mobInteract(owner, InteractionHand.MAIN_HAND);
        }
        helper.assertTrue(leopard.isTame(), "Der Leopard lässt sich nicht zähmen");
        helper.assertTrue(leopard.isOwnedBy(owner), "Der Leopard gehört nicht dem Spieler");
        helper.assertTrue(leopard.getMaxHealth() > 30.0f, "Gezähmt hat er nicht mehr Leben");
        remove(helper, owner);
        cleanup(helper);
        helper.succeed();
    }

    /** Hungrig springt er einen Hasen an und erlegt ihn. */
    private static void leopardHunt(GameTestHelper helper) {
        pen(helper, Blocks.SNOW_BLOCK);
        Hare rabbit = helper.spawnWithNoFreeWill(ModEntities.HARE.get(), new BlockPos(6, 1, 6));
        SnowLeopardEntity leopard = helper.spawn(BiomeBeasts.SNOW_LEOPARD.get(), new BlockPos(2, 1, 2));
        leopard.setHungry(true);
        helper.succeedWhen(() -> {
            helper.assertFalse(rabbit.isAlive(), "Der Hase lebt noch");
            cleanup(helper, leopard);
        });
    }

    // =====================================================================================
    // Urelch
    // =====================================================================================

    /** Gezähmt nimmt er eine Truhe, mit Sattel lässt er sich reiten (der Reiter lenkt). */
    private static void elkRide(GameTestHelper helper) {
        pen(helper, Blocks.GRASS_BLOCK);
        PrimevalElkEntity elk = helper.spawnWithNoFreeWill(BiomeBeasts.PRIMEVAL_ELK.get(), new BlockPos(4, 1, 4));
        ServerPlayer rider = player(helper, new BlockPos(2, 1, 4), 270.0f);
        helper.assertTrue(elk.tameWithName(rider), "Der Elch lässt sich nicht zähmen");
        rider.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.CHEST));
        elk.mobInteract(rider, InteractionHand.MAIN_HAND);
        helper.assertTrue(elk.hasChest(), "Der Elch nimmt keine Truhe");
        elk.setItemSlot(EquipmentSlot.SADDLE, new ItemStack(Items.SADDLE));
        helper.assertTrue(elk.isSaddled(), "Der Elch trägt keinen Sattel");
        helper.assertTrue(rider.startRiding(elk, true, true), "Man kann nicht aufsteigen");
        helper.assertTrue(elk.getControllingPassenger() == rider, "Der Reiter lenkt nicht");
        rider.stopRiding();
        remove(helper, rider);
        cleanup(helper);
        helper.succeed();
    }

    /** Ein wilder Bulle stürmt mit gesenkten Schaufeln auf den, der ihn angreift. */
    private static void elkCharge(GameTestHelper helper) {
        pen(helper, Blocks.GRASS_BLOCK);
        Mob attacker = helper.spawnWithNoFreeWill(ModEntities.HUSK.get(), new BlockPos(6, 1, 6));
        PrimevalElkEntity elk = helper.spawn(BiomeBeasts.PRIMEVAL_ELK.get(), new BlockPos(2, 1, 2));
        elk.setBull(true);
        float before = attacker.getHealth();
        // nicht im allerersten Takt (sonst gleicht der Zeitstempel dem Anfangswert des Ziels und es greift nicht)
        helper.runAfterDelay(5, () -> elk.hurtServer(helper.getLevel(), helper.getLevel().damageSources().mobAttack(attacker), 1.0f));
        helper.succeedWhen(() -> {
            helper.assertTrue(attacker.getHealth() < before || !attacker.isAlive(), "Der Bulle hat nicht zugestoßen");
            cleanup(helper, elk);
        });
    }

    // =====================================================================================
    // Sturmmöwe
    // =====================================================================================

    /** Kommt man ihr zu nahe (ohne zu schleichen), fliegt sie auf. */
    private static void gullFlee(GameTestHelper helper) {
        pen(helper, Blocks.GRAVEL);
        StormGullEntity gull = helper.spawn(BiomeBeasts.STORM_GULL.get(), new BlockPos(4, 1, 5));
        ServerPlayer walker = player(helper, new BlockPos(4, 1, 3), 0.0f);
        helper.succeedWhen(() -> {
            helper.assertTrue(gull.isFlying(), "Die Möwe fliegt nicht auf");
            remove(helper, walker);
            cleanup(helper, gull);
        });
    }

    /** Wer einen Kabeljau in der Hand hält, dem schnappt sie ihn weg – und fliegt mit ihm davon. */
    private static void gullSteal(GameTestHelper helper) {
        pen(helper, Blocks.GRAVEL);
        ServerPlayer fisher = survivor(helper, new BlockPos(4, 1, 2), 0.0f);
        fisher.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.COD));
        StormGullEntity gull = helper.spawn(BiomeBeasts.STORM_GULL.get(), new BlockPos(4, 4, 6));
        gull.setFlying(true);
        gull.setStealCooldown(0);
        helper.succeedWhen(() -> {
            helper.assertTrue(gull.getCarried().is(Items.COD), "Die Möwe hat den Fisch nicht");
            helper.assertTrue(fisher.getMainHandItem().isEmpty(), "Der Fisch ist noch in der Hand");
            remove(helper, fisher);
            cleanup(helper, gull);
        });
    }

    // =====================================================================================
    // Greif
    // =====================================================================================

    /** Hungrig steigt er auf und stößt im Sturzflug auf einen Hasen herab. */
    private static void griffinHunt(GameTestHelper helper) {
        pen(helper, Blocks.COARSE_DIRT);
        Hare rabbit = helper.spawnWithNoFreeWill(ModEntities.HARE.get(), new BlockPos(5, 1, 5));
        GriffinEntity griffin = helper.spawn(BiomeBeasts.GRIFFIN.get(), new BlockPos(1, 1, 1));
        griffin.setHungry(true);
        boolean[] dived = new boolean[1];
        helper.onEachTick(() -> dived[0] |= griffin.isDiving());
        helper.succeedWhen(() -> {
            helper.assertFalse(rabbit.isAlive(), "Der Hase lebt noch");
            helper.assertTrue(dived[0], "Er hat nicht im Sturzflug gejagt");
            cleanup(helper, griffin);
        });
    }

    /** Am Boden angegriffen wehrt er sich mit den Krallen (satt: Beute kümmert ihn nicht). */
    private static void griffinDefend(GameTestHelper helper) {
        pen(helper, Blocks.COARSE_DIRT);
        Mob attacker = helper.spawnWithNoFreeWill(ModEntities.HUSK.get(), new BlockPos(5, 1, 4));
        GriffinEntity griffin = helper.spawn(BiomeBeasts.GRIFFIN.get(), new BlockPos(3, 1, 4));
        griffin.setHungry(false);
        float before = attacker.getHealth();
        helper.runAfterDelay(5, () -> griffin.hurtServer(helper.getLevel(), helper.getLevel().damageSources().mobAttack(attacker), 1.0f));
        helper.succeedWhen(() -> {
            LivingEntity target = griffin.getTarget();
            helper.assertTrue(attacker.getHealth() < before || !attacker.isAlive(), "Der Greif wehrt sich nicht"
                    + (target == null ? " (kein Ziel)" : ""));
            cleanup(helper, griffin);
        });
    }

    // =====================================================================================
    // 0.86: Glutsalamander, Moorschnapper, Flamingo, Schmetterlinge
    // =====================================================================================

    /** Werte, Gruppen, Spawn-Eier, Feuerfestigkeit und Biome der vier Wesen von 0.86. */
    private static void basics86(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        List<EntityType<? extends Mob>> types = List.of(BiomeBeasts.EMBER_SALAMANDER.get(), BiomeBeasts.MIRE_SNAPPER.get(),
                BiomeBeasts.FLAMINGO.get(), BiomeBeasts.BUTTERFLY.get());
        double[] health = {12.0, 30.0, 10.0, 2.0};
        for (int i = 0; i < types.size(); i++) {
            EntityType<? extends Mob> type = types.get(i);
            String name = BuiltInRegistries.ENTITY_TYPE.getKey(type).getPath();
            helper.assertTrue(type.getCategory() == (i == 3 ? MobCategory.AMBIENT : MobCategory.CREATURE), name + ": falsche Gruppe");
            helper.assertTrue(type.getDefaultLootTable().isPresent(), name + ": keine Beute");
            helper.assertTrue(BuiltInRegistries.ITEM.containsKey(KingdomsAtWar.id(name + "_spawn_egg")), name + ": kein Spawn-Ei");
            Mob mob = helper.spawnWithNoFreeWill(type, new BlockPos(1 + i * 2, 1, 4));
            helper.assertTrue(Math.abs(mob.getAttributeValue(Attributes.MAX_HEALTH) - health[i]) < 1.0e-3,
                    name + ": Lebenspunkte " + mob.getAttributeValue(Attributes.MAX_HEALTH) + " statt " + health[i]);
        }
        helper.assertTrue(BiomeBeasts.EMBER_SALAMANDER.get().fireImmune(), "Der Glutsalamander brennt");
        var biomes = level.registryAccess().lookupOrThrow(Registries.BIOME);
        expect(helper, biomes, OverworldBiomes.ASH_FIELDS, ModTags.Biomes.HAS_EMBER_SALAMANDERS, true);
        expect(helper, biomes, Biomes.PLAINS, ModTags.Biomes.HAS_EMBER_SALAMANDERS, false);
        expect(helper, biomes, OverworldBiomes.MIST_MOOR, ModTags.Biomes.HAS_MIRE_SNAPPERS, true);
        expect(helper, biomes, Biomes.SWAMP, ModTags.Biomes.HAS_MIRE_SNAPPERS, true);
        expect(helper, biomes, Biomes.DESERT, ModTags.Biomes.HAS_MIRE_SNAPPERS, false);
        expect(helper, biomes, OverworldBiomes.CORAL_COAST, ModTags.Biomes.HAS_FLAMINGOS, true);
        expect(helper, biomes, Biomes.MANGROVE_SWAMP, ModTags.Biomes.HAS_FLAMINGOS, true);
        expect(helper, biomes, Biomes.TAIGA, ModTags.Biomes.HAS_FLAMINGOS, false);
        expect(helper, biomes, OverworldBiomes.ROYAL_MEADOWS, ModTags.Biomes.HAS_BUTTERFLIES, true);
        expect(helper, biomes, Biomes.FLOWER_FOREST, ModTags.Biomes.HAS_BUTTERFLIES, true);
        expect(helper, biomes, Biomes.SNOWY_PLAINS, ModTags.Biomes.HAS_BUTTERFLIES, false);
        // natürliches Auftreten in Gruppen (wie der Spawner: die Gruppendaten werden weitergereicht)
        List<EntityType<? extends Mob>> all = List.of(BiomeBeasts.SNOW_LEOPARD.get(), BiomeBeasts.PRIMEVAL_ELK.get(),
                BiomeBeasts.STORM_GULL.get(), BiomeBeasts.GRIFFIN.get(), BiomeBeasts.EMBER_SALAMANDER.get(),
                BiomeBeasts.MIRE_SNAPPER.get(), BiomeBeasts.FLAMINGO.get(), BiomeBeasts.BUTTERFLY.get());
        BlockPos at = helper.absolutePos(new BlockPos(4, 1, 4));
        for (EntityType<? extends Mob> type : all) {
            SpawnGroupData group = null;
            for (int n = 0; n < 4; n++) {
                Mob mob = type.create(level, EntitySpawnReason.NATURAL);
                helper.assertTrue(mob != null, BuiltInRegistries.ENTITY_TYPE.getKey(type).getPath() + ": nicht erzeugt");
                mob.snapTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, 0.0f, 0.0f);
                group = mob.finalizeSpawn(level, level.getCurrentDifficultyAt(at), EntitySpawnReason.NATURAL, group);
                mob.discard();
            }
        }
        cleanup(helper);
        helper.succeed();
    }

    /** Der Glutsalamander läuft über Lava und nimmt keinen Schaden. */
    private static void salamanderLava(GameTestHelper helper) {
        pen(helper, Blocks.STONE);
        for (int x = 1; x < 8; x++) {
            for (int z = 1; z < 8; z++) {
                helper.setBlock(x, 0, z, Blocks.LAVA);
            }
        }
        EmberSalamanderEntity salamander = helper.spawn(BiomeBeasts.EMBER_SALAMANDER.get(), new BlockPos(4, 1, 4));
        double floor = helper.absoluteVec(Vec3.ZERO).y + 0.4;                    // er steht einen halben Block tief auf der Lava
        helper.runAfterDelay(100, () -> {
            helper.assertTrue(salamander.isAlive(), "Der Salamander ist in der Lava umgekommen");
            helper.assertTrue(salamander.getHealth() >= salamander.getMaxHealth() - 1.0e-3, "Die Lava hat ihm geschadet");
            helper.assertTrue(salamander.getY() > floor, "Er ist in der Lava versunken (y=" + salamander.getY() + ")");
            cleanup(helper, salamander);
            helper.succeed();
        });
    }

    /** Wer ihn mit der Hand schlägt, verbrennt sich an seiner glühenden Haut. */
    private static void salamanderBurns(GameTestHelper helper) {
        pen(helper, Blocks.STONE);
        EmberSalamanderEntity salamander = helper.spawnWithNoFreeWill(BiomeBeasts.EMBER_SALAMANDER.get(), new BlockPos(4, 1, 5));
        ServerPlayer player = survivor(helper, new BlockPos(4, 1, 3), 0.0f);
        helper.runAfterDelay(5, () -> {
            player.attack(salamander);
            helper.assertTrue(player.getRemainingFireTicks() > 0, "Der Spieler brennt nicht");
            remove(helper, player);
            cleanup(helper);
            helper.succeed();
        });
    }

    /** Der Moorschnapper schnappt nach dem, der vor seine Nase gerät. */
    private static void snapperSnap(GameTestHelper helper) {
        pen(helper, Blocks.MUD);
        MireSnapperEntity snapper = helper.spawn(BiomeBeasts.MIRE_SNAPPER.get(), new BlockPos(4, 1, 3));
        Vec3 at = helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(4, 1, 3)));
        snapper.snapTo(at.x, at.y, at.z, 0.0f, 0.0f);
        snapper.setYBodyRot(0.0f);
        snapper.setYHeadRot(0.0f);
        Mob victim = helper.spawnWithNoFreeWill(ModEntities.HUSK.get(), new BlockPos(4, 1, 5));
        float before = victim.getHealth();
        helper.succeedWhen(() -> {
            helper.assertTrue(victim.getHealth() < before || !victim.isAlive(), "Er hat nicht zugeschnappt");
            cleanup(helper, snapper);
        });
    }

    /** Getroffen zieht er sich in den Panzer zurück; dort richtet man kaum etwas aus. */
    private static void snapperShell(GameTestHelper helper) {
        pen(helper, Blocks.MUD);
        MireSnapperEntity snapper = helper.spawnWithNoFreeWill(BiomeBeasts.MIRE_SNAPPER.get(), new BlockPos(4, 1, 4));
        Mob attacker = helper.spawnWithNoFreeWill(ModEntities.HUSK.get(), new BlockPos(4, 1, 7));
        ServerLevel level = helper.getLevel();
        helper.runAfterDelay(5, () -> {
            snapper.hurtServer(level, level.damageSources().mobAttack(attacker), 4.0f);
            helper.assertTrue(snapper.isWithdrawn(), "Er zieht sich nicht in den Panzer zurück");
            float full = snapper.getHealth();
            helper.runAfterDelay(12, () -> {
                snapper.hurtServer(level, level.damageSources().mobAttack(attacker), 10.0f);
                float lost = full - snapper.getHealth();
                helper.assertTrue(lost > 0.0f && lost < 5.0f, "Im Panzer verliert er zu viel (" + lost + ")");
                cleanup(helper);
                helper.succeed();
            });
        });
    }

    /** Der Flamingo steht im flachen Wasser (schwimmt nicht auf). */
    private static void flamingoWade(GameTestHelper helper) {
        pen(helper, Blocks.SAND);
        for (int x = 1; x < 8; x++) {
            for (int z = 1; z < 8; z++) {
                helper.setBlock(x, 1, z, Blocks.WATER);
            }
        }
        FlamingoEntity flamingo = helper.spawn(BiomeBeasts.FLAMINGO.get(), new BlockPos(4, 1, 4));
        double bottom = helper.absoluteVec(Vec3.ZERO).y + 1.0;
        helper.runAfterDelay(80, () -> {
            helper.assertFalse(flamingo.isFlying(), "Er ist aufgeflogen");
            helper.assertTrue(flamingo.getY() < bottom + 0.4, "Er schwimmt, statt zu stehen (y=" + (flamingo.getY() - bottom) + ")");
            cleanup(helper, flamingo);
            helper.succeed();
        });
    }

    /** Ein Schmetterling setzt sich bald auf eine Blume. */
    private static void butterflyPerch(GameTestHelper helper) {
        pen(helper, Blocks.GRASS_BLOCK);
        for (int x = 1; x < 8; x++) {
            for (int z = 1; z < 8; z++) {
                helper.setBlock(x, 1, z, (x + z) % 2 == 0 ? Blocks.POPPY : Blocks.DANDELION);
            }
        }
        ButterflyEntity butterfly = helper.spawn(BiomeBeasts.BUTTERFLY.get(), new BlockPos(4, 3, 4));
        helper.succeedWhen(() -> {
            helper.assertTrue(butterfly.isPerched(), "Er setzt sich nicht");
            cleanup(helper, butterfly);
        });
    }
}
