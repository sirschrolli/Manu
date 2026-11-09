package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.entity.voidrealm.DistortlingEntity;
import io.github.sirschrolli.kingdomsatwar.entity.voidrealm.ShardWardenEntity;
import io.github.sirschrolli.kingdomsatwar.entity.voidrealm.StarRayEntity;
import io.github.sirschrolli.kingdomsatwar.entity.voidrealm.VoidMawEntity;
import io.github.sirschrolli.kingdomsatwar.entity.voidrealm.WardenShardEntity;
import io.github.sirschrolli.kingdomsatwar.magic.fx.SpellFxEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks;
import io.github.sirschrolli.kingdomsatwar.voidrealm.RiftState;
import io.github.sirschrolli.kingdomsatwar.voidrealm.VoidBiomes;
import io.github.sirschrolli.kingdomsatwar.voidrealm.VoidDimension;
import io.github.sirschrolli.kingdomsatwar.voidrealm.VoidGate;
import io.github.sirschrolli.kingdomsatwar.voidrealm.VoidTerrain;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/** Tests der Leere: Dimension, Gelände und Biome, der Riss und die Wesen der Leere. */
final class VoidGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> VOID_WORLD =
            ModGameTests.TEST_FUNCTIONS.register("void_world", () -> VoidGameTests::voidWorld);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> VOID_CREATURES =
            ModGameTests.TEST_FUNCTIONS.register("void_creatures", () -> VoidGameTests::voidCreatures);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> VOID_MAW_PULLS =
            ModGameTests.TEST_FUNCTIONS.register("void_maw_pulls", () -> VoidGameTests::voidMawPulls);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SHARD_WARDEN_GUARDS =
            ModGameTests.TEST_FUNCTIONS.register("shard_warden_guards", () -> VoidGameTests::shardWardenGuards);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> DISTORTLING_BLINKS =
            ModGameTests.TEST_FUNCTIONS.register("distortling_blinks", () -> VoidGameTests::distortlingBlinks);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> STAR_RAY_RIDES =
            ModGameTests.TEST_FUNCTIONS.register("star_ray_rides", () -> VoidGameTests::starRayRides);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SHATTERED_KING_FIGHT =
            ModGameTests.TEST_FUNCTIONS.register("shattered_king_fight", () -> VoidGameTests::shatteredKingFight);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> VOID_THRONE =
            ModGameTests.TEST_FUNCTIONS.register("void_throne", () -> VoidGameTests::voidThrone);

    private VoidGameTests() {}

    /** Lädt die Klasse (meldet die Testfunktionen an). */
    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var test : List.of(VOID_WORLD, VOID_CREATURES, VOID_MAW_PULLS, SHARD_WARDEN_GUARDS, DISTORTLING_BLINKS, STAR_RAY_RIDES,
                VOID_THRONE)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 400, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
        // der Kampf gegen den König dauert länger (Erwachen, vier Angriffe, Zerfallen)
        event.registerTest(SHATTERED_KING_FIGHT.getId(), new FunctionGameTestInstance(SHATTERED_KING_FIGHT.key(),
                new TestData<>(environment, Level.OVERWORLD, ARENA, 900, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
    }

    private static void floor(GameTestHelper helper, int size) {
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                helper.setBlock(x, 0, z, VoidBlocks.VOID_MOSS.get());
            }
        }
    }

    /**
     * Die Leere: Dimensionstyp und Gelände sind registriert; alle Biome kommen vor; es gibt treibende Bruchstücke und
     * dazwischen leere Strecken ohne jeden Grund. Gibt es die Welt (nicht auf jedem Test-Server), führt ein Riss hinein –
     * auf eine Ankerinsel mit dem Rückweg – und wieder zurück.
     */
    private static void voidWorld(GameTestHelper helper) {
        ServerLevel overworld = helper.getLevel();
        var server = overworld.getServer();
        var registries = server.registryAccess();
        helper.assertTrue(registries.lookupOrThrow(Registries.DIMENSION_TYPE).get(VoidDimension.TYPE).isPresent(),
                "Der Dimensionstyp der Leere fehlt");
        Holder<NoiseGeneratorSettings> settings = registries.lookupOrThrow(Registries.NOISE_SETTINGS).getOrThrow(VoidTerrain.SETTINGS);
        ServerLevel voidLevel = server.getLevel(VoidGate.THE_VOID);
        ChunkGenerator generator;
        RandomState random;
        LevelHeightAccessor height;
        if (voidLevel != null) {
            generator = voidLevel.getChunkSource().getGenerator();
            random = voidLevel.getChunkSource().randomState();
            height = voidLevel;
        } else {
            generator = new NoiseBasedChunkGenerator(MultiNoiseBiomeSource.createFromList(
                    VoidBiomes.parameters(registries.lookupOrThrow(Registries.BIOME))), settings);
            random = RandomState.create(registries.lookupOrThrow(Registries.NOISE), 20_261_108L, settings.value());
            height = LevelHeightAccessor.create(-64, 384);
        }
        var possible = generator.getBiomeSource().possibleBiomes();
        for (ResourceKey<Biome> biome : VoidBiomes.ALL) {
            helper.assertTrue(possible.stream().anyMatch(holder -> holder.is(biome)), "Biom fehlt in der Leere: " + biome.identifier());
        }
        // Gelände: 100 Säulen in einem weiten Raster – Land und Nichts im Wechsel, Land nie unter Höhe 0 oder über 250
        BiomeResolver biomes = generator.getBiomeSource().createUncachedResolver(random);
        int land = 0;
        int open = 0;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                int x = (i - 5) * 300 + 41;
                int z = (j - 5) * 300 - 17;
                int top = generator.getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, height, random);
                if (top > -40) {
                    land++;
                    helper.assertTrue(top > 0 && top < 252, "Bruchstück außerhalb der erlaubten Höhen: " + top);
                    biomes.getNoiseBiome(x >> 2, top >> 2, z >> 2);
                } else {
                    open++;
                }
            }
        }
        helper.assertTrue(land >= 8, "Zu wenig Bruchstücke in der Leere: " + land + " von 100");
        helper.assertTrue(open >= 30, "Zu wenig Nichts in der Leere: " + open + " von 100");
        // die Leerenschlünde und Sternenrochen schweben im Nichts zwischen den Bruchstücken
        var biomeLookup = registries.lookupOrThrow(Registries.BIOME);
        assertSpawns(helper, biomeLookup.getOrThrow(VoidBiomes.EXPANSE).value(), MobCategory.MONSTER, ModEntities.VOID_MAW.get());
        assertSpawns(helper, biomeLookup.getOrThrow(VoidBiomes.EXPANSE).value(), MobCategory.CREATURE, ModEntities.STAR_RAY.get());
        assertSpawns(helper, biomeLookup.getOrThrow(VoidBiomes.WEIGHTLESS_RUINS).value(), MobCategory.MONSTER, ModEntities.SHARD_WARDEN.get());
        assertSpawns(helper, biomeLookup.getOrThrow(VoidBiomes.CRYSTAL_FOREST).value(), MobCategory.MONSTER, ModEntities.DISTORTLING.get());
        assertSpawns(helper, biomeLookup.getOrThrow(VoidBiomes.WORLD_REMNANTS).value(), MobCategory.MONSTER, ModEntities.DISTORTLING.get());

        // der Riss: gebaut, verzeichnet und (wenn es die Leere gibt) ein Weg hin und zurück
        floor(helper, 9);
        BlockPos bottom = helper.absolutePos(new BlockPos(4, 1, 4));
        VoidGate.build(overworld, bottom, Direction.Axis.X);
        helper.assertTrue(overworld.getBlockState(bottom).is(VoidBlocks.VOID_RIFT.get()), "Im Riss wogt nichts");
        RiftState state = RiftState.of(overworld);
        RiftState.Rift rift = new RiftState.Rift(overworld.dimension(), bottom, Direction.Axis.X, Optional.empty());
        state.add(rift);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        TeleportTransition there = VoidGate.destination(overworld, player, bottom);
        if (voidLevel != null) {
            helper.assertTrue(there != null && there.newLevel() == voidLevel, "Der Riss führt nicht in die Leere");
            BlockPos landing = BlockPos.containing(there.position());
            helper.assertTrue(voidLevel.getBlockState(landing.below()).isSolid(), "In der Leere fehlt die Ankerinsel unter dem Ziel");
            RiftState.Rift back = state.near(VoidGate.THE_VOID, landing, 6);
            helper.assertTrue(back != null && back.partner().isPresent(), "In der Leere fehlt der Riss zurück");
            TeleportTransition home = VoidGate.destination(voidLevel, player, back.bottom());
            helper.assertTrue(home != null && home.newLevel() == overworld, "Der Riss in der Leere führt nicht zurück");
            state.remove(back);
        } else {
            helper.assertTrue(there == null, "Ohne Leere darf der Riss nirgendwohin führen");
        }
        RiftState.Rift stored = state.at(overworld.dimension(), bottom);
        if (stored != null) {
            state.remove(stored);
        }
        state.save(overworld);
        helper.succeed();
    }

    private static void assertSpawns(GameTestHelper helper, Biome biome, MobCategory category, EntityType<?> type) {
        MobSpawnSettings spawns = biome.getAttributes().applyModifier(EnvironmentAttributes.NATURAL_MOB_SPAWNS, MobSpawnSettings.EMPTY);
        helper.assertTrue(spawns.getMobsToSpawn(category).unwrap().stream().anyMatch(entry -> entry.value().type() == type),
                "Biom ohne " + type.getDescriptionId());
    }

    /** Die vier Wesen: lassen sich erzeugen, haben Beute, Spawn-Eier und überstehen die ersten Ticks. */
    private static void voidCreatures(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper, 9);
        var loot = level.getServer().reloadableRegistries();
        List<EntityType<? extends Mob>> types = List.of(ModEntities.VOID_MAW.get(), ModEntities.SHARD_WARDEN.get(),
                ModEntities.DISTORTLING.get(), ModEntities.STAR_RAY.get());
        List<Mob> mobs = new java.util.ArrayList<>();
        for (int i = 0; i < types.size(); i++) {
            var type = types.get(i);
            mobs.add(helper.spawnWithNoFreeWill(type, new BlockPos(2 + (i % 2) * 4, 1, 2 + (i / 2) * 4)));
            ResourceKey<LootTable> key = type.getDefaultLootTable().orElseThrow();
            helper.assertTrue(loot.getLootTable(key) != LootTable.EMPTY, "Beute fehlt: " + key.identifier());
        }
        for (var egg : VoidBlocks.spawnEggs()) {
            helper.assertTrue(new ItemStack(egg.get()).getItem() instanceof net.minecraft.world.item.SpawnEggItem, "Kein Spawn-Ei: " + egg.getId());
        }
        helper.assertTrue(mobs.get(0).is(net.minecraft.tags.EntityTypeTags.FALL_DAMAGE_IMMUNE), "Der Schlund nimmt Fallschaden");
        helper.assertTrue(mobs.get(3).is(net.minecraft.tags.EntityTypeTags.FALL_DAMAGE_IMMUNE), "Der Rochen nimmt Fallschaden");
        helper.assertTrue(((ShardWardenEntity) mobs.get(1)).isDormant(), "Ein neuer Splitterwächter ruht nicht");
        helper.runAfterDelay(10, () -> {
            for (Mob mob : mobs) {
                helper.assertTrue(mob.isAlive(), mob.getType().getDescriptionId() + " lebt nicht mehr");
            }
            helper.succeed();
        });
    }

    /**
     * Leerenschlund: sein Sog zieht die Beute zum Maul (geduckt kaum), und sein Biss trifft; Schleichen schwächt den Sog.
     */
    private static void voidMawPulls(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper, 9);
        VoidMawEntity maw = helper.spawnWithNoFreeWill(ModEntities.VOID_MAW.get(), new BlockPos(4, 5, 4));
        maw.setNoGravity(true);
        var target = helper.spawnWithNoFreeWill(ModEntities.DISTORTLING.get(), new BlockPos(4, 1, 8));
        Vec3 before = target.getDeltaMovement();
        maw.pull(target);
        Vec3 pulled = target.getDeltaMovement().subtract(before);
        Vec3 toMaw = maw.mouthPosition().subtract(target.position());
        helper.assertTrue(pulled.dot(toMaw) > 0.0, "Der Sog zieht nicht zum Maul: " + pulled);
        double strong = pulled.length();
        target.setDeltaMovement(Vec3.ZERO);
        target.setShiftKeyDown(true);
        maw.pull(target);
        helper.assertTrue(target.getDeltaMovement().length() < strong * 0.5, "Ducken schwächt den Sog nicht");
        target.setShiftKeyDown(false);
        float health = target.getHealth();
        maw.bite(level, target);
        helper.assertTrue(target.getHealth() < health, "Der Biss des Schlunds trifft nicht");
        helper.succeed();
    }

    /**
     * Splitterwächter: ruht, erwacht, wenn man ihn trifft (ruhend halbiert er den Schaden), schleudert Kristallsplitter
     * und stößt mit dem Erdstoß alles ringsum fort.
     */
    private static void shardWardenGuards(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper, 13);
        ShardWardenEntity warden = helper.spawn(ModEntities.SHARD_WARDEN.get(), new BlockPos(6, 1, 6));
        warden.setDormant(true);
        helper.assertTrue(warden.isDormant(), "Der Wächter ruht nicht");
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.snapTo(warden.getX() + 2.0, warden.getY(), warden.getZ(), 90.0f, 0.0f);
        float health = warden.getHealth();
        warden.hurtServer(level, level.damageSources().playerAttack(player), 10.0f);
        helper.assertTrue(warden.getHealth() > health - 10.0f + 0.01f, "Ruhend nimmt der Wächter vollen Schaden");
        helper.assertTrue(warden.getTarget() == player, "Der Wächter merkt nicht, wer ihn trifft");
        helper.runAfterDelay(4, () -> {
            helper.assertFalse(warden.isDormant(), "Der Wächter erwacht nicht, als man ihn trifft");
            // Kristallsplitter
            int shards = level.getEntitiesOfClass(WardenShardEntity.class, warden.getBoundingBox().inflate(20)).size();
            var target = helper.spawnWithNoFreeWill(ModEntities.DISTORTLING.get(), new BlockPos(6, 1, 12));
            WardenShardEntity.shoot(level, warden, warden.position().add(0, 3, 0), target.getEyePosition().subtract(warden.position().add(0, 3, 0)),
                    1.15, 6.0f);
            helper.assertTrue(level.getEntitiesOfClass(WardenShardEntity.class, warden.getBoundingBox().inflate(20)).size() > shards,
                    "Der Wächter schleudert keine Splitter");
            helper.succeed();
        });
    }

    /** Zerrling: springt an eine freie Stelle auf festem Grund (nicht in Wände, nicht ins Nichts). */
    private static void distortlingBlinks(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper, 9);
        DistortlingEntity distortling = helper.spawnWithNoFreeWill(ModEntities.DISTORTLING.get(), new BlockPos(1, 1, 1));
        Vec3 goal = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(6, 1, 6)));
        helper.assertTrue(distortling.blinkNear(level, goal, 1.0), "Der Zerrling springt nicht");
        BlockPos feet = distortling.blockPosition();
        helper.assertTrue(feet.distSqr(BlockPos.containing(goal)) <= 8, "Der Zerrling landet zu weit vom Ziel: " + feet);
        helper.assertTrue(level.getBlockState(feet.below()).is(VoidBlocks.VOID_MOSS.get()), "Der Zerrling steht nicht auf festem Grund");
        helper.assertTrue(level.getBlockState(feet).isAir() && level.getBlockState(feet.above()).isAir(), "Der Zerrling steckt in der Wand");
        // in der Luft ohne Boden darunter springt er nicht
        Vec3 air = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(4, 20, 4)));
        BlockPos was = distortling.blockPosition();
        distortling.blinkNear(level, air, 0.5);
        helper.assertTrue(distortling.blockPosition().getY() < helper.absolutePos(new BlockPos(0, 10, 0)).getY() || was.equals(distortling.blockPosition()),
                "Der Zerrling springt ins Nichts");
        helper.succeed();
    }

    /**
     * Der Zerbrochene König: das Thronsiegel weckt ihn (beim Erwachen unverwundbar), danach treffen Faustschlag,
     * Kronensplitter, Ruf der Wächter, Risse der Leere, Leerenbeben und Sternenstrahl; besiegt zerfällt er, lässt seine
     * Krone fallen und der Thron bleibt leer.
     */
    private static void shatteredKingFight(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper, 9);
        BlockPos sealPos = helper.absolutePos(new BlockPos(4, 0, 8));
        level.setBlock(sealPos, VoidBlocks.THRONE_SEAL.get().defaultBlockState(), net.minecraft.world.level.block.Block.UPDATE_ALL);
        if (!(level.getBlockEntity(sealPos) instanceof io.github.sirschrolli.kingdomsatwar.block.voidrealm.ThroneSealBlockEntity seal)) {
            helper.fail("Das Thronsiegel hat keine Blockentität");
            return;
        }
        seal.setup(helper.absolutePos(new BlockPos(4, 1, 4)));
        helper.assertTrue(seal.phase() == io.github.sirschrolli.kingdomsatwar.block.voidrealm.ThroneSealBlockEntity.Phase.DORMANT,
                "Der Thron ruht nicht");
        var king = seal.awaken(level);
        helper.assertTrue(king != null, "Das Siegel weckt den König nicht");
        helper.assertTrue(king.isAwakening(), "Der König erwacht nicht");
        helper.assertTrue(seal.phase() == io.github.sirschrolli.kingdomsatwar.block.voidrealm.ThroneSealBlockEntity.Phase.AWAKE,
                "Der Thron merkt das Erwachen nicht");
        float before = king.getHealth();
        king.hurtServer(level, level.damageSources().generic(), 20.0f);
        helper.assertTrue(king.getHealth() >= before, "Beim Erwachen ist der König verwundbar");
        var soldier = helper.spawnWithNoFreeWill(ModEntities.SOLDIERS.get(io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType.KNIGHT).get(),
                new BlockPos(4, 1, 7));
        helper.runAfterDelay(io.github.sirschrolli.kingdomsatwar.entity.voidrealm.ShatteredKingEntity.AWAKEN_TICKS + 4, () -> {
            helper.assertFalse(king.isAwakening(), "Der König erhebt sich nicht ganz");
            float health = soldier.getHealth();
            king.forceAttack(level, "slam_left", soldier, true);
            helper.runAfterDelay(io.github.sirschrolli.kingdomsatwar.entity.voidrealm.ShatteredKingEntity.SLAM_IMPACT + 3, () -> {
                helper.assertTrue(soldier.getHealth() < health, "Der Faustschlag trifft nicht");
                king.forceAttack(level, "volley", soldier, true);
                helper.runAfterDelay(22, () -> {
                    helper.assertFalse(level.getEntitiesOfClass(WardenShardEntity.class, king.getBoundingBox().inflate(30)).isEmpty(),
                            "Die Krone schleudert keine Splitter");
                    king.forceAttack(level, "summon", soldier, true);
                    helper.runAfterDelay(24, () -> {
                        helper.assertFalse(level.getEntitiesOfClass(ShardWardenEntity.class, king.getBoundingBox().inflate(30)).isEmpty(),
                                "Der König ruft keine Wächter");
                        riftAndQuake(helper, level, king, soldier, () -> {
                            king.forceAttack(level, "beam", soldier, true);
                            helper.runAfterDelay(40, () -> {
                                helper.assertTrue(king.isBeaming(), "Kein Sternenstrahl");
                                king.setHealth(1.0f);
                                king.hurtServer(level, level.damageSources().generic(), 50.0f);
                                helper.assertTrue(king.isDeadOrDying(), "Der König fällt nicht");
                                helper.runAfterDelay(io.github.sirschrolli.kingdomsatwar.entity.voidrealm.ShatteredKingEntity.DEATH_TICKS + 6, () -> {
                                    helper.assertTrue(seal.phase() == io.github.sirschrolli.kingdomsatwar.block.voidrealm.ThroneSealBlockEntity.Phase.DEFEATED,
                                            "Der Thron merkt den Sieg nicht");
                                    helper.assertTrue(level.getEntitiesOfClass(ShardWardenEntity.class, new net.minecraft.world.phys.AABB(
                                            helper.absolutePos(BlockPos.ZERO)).inflate(40), net.minecraft.world.entity.Entity::isAlive).isEmpty(),
                                            "Die gerufenen Wächter zerfallen nicht mit dem König");
                                    helper.assertFalse(level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                                            new net.minecraft.world.phys.AABB(helper.absolutePos(BlockPos.ZERO)).inflate(40),
                                            e -> e.getItem().is(VoidBlocks.SHATTERED_CROWN.get())).isEmpty(), "Der König lässt seine Krone nicht fallen");
                                    helper.succeed();
                                });
                            });
                        });
                    });
                });
            });
        });
    }

    /**
     * Risse der Leere (der Boden reißt auf, die Leere bricht unter dem Ritter hervor) und Leerenbeben (ein Ring rollt bis zu
     * ihm, fünf Blöcke von den Fäusten entfernt).
     */
    private static void riftAndQuake(GameTestHelper helper, ServerLevel level, io.github.sirschrolli.kingdomsatwar.entity.voidrealm.ShatteredKingEntity king,
            net.minecraft.world.entity.LivingEntity soldier, Runnable then) {
        soldier.setHealth(soldier.getMaxHealth());
        king.forceAttack(level, "rift", soldier, true);
        helper.runAfterDelay(io.github.sirschrolli.kingdomsatwar.entity.voidrealm.ShatteredKingEntity.RIFT_TEAR + 2, () -> {
            helper.assertFalse(level.getEntitiesOfClass(SpellFxEntity.class, king.getBoundingBox().inflate(30), e -> e.kind() == SpellFxEntity.Kind.ARC)
                    .isEmpty(), "Der Boden reißt nicht auf");
            float before = soldier.getHealth();
            helper.runAfterDelay(io.github.sirschrolli.kingdomsatwar.entity.voidrealm.ShatteredKingEntity.RIFT_ERUPT
                    - io.github.sirschrolli.kingdomsatwar.entity.voidrealm.ShatteredKingEntity.RIFT_TEAR + 4, () -> {
                helper.assertTrue(soldier.getHealth() < before, "Die Leere bricht nicht unter ihm hervor");
                helper.assertFalse(level.getEntitiesOfClass(SpellFxEntity.class, king.getBoundingBox().inflate(30),
                        e -> e.kind() == SpellFxEntity.Kind.PILLAR).isEmpty(), "Keine Säulen aus Leere");
                helper.runAfterDelay(io.github.sirschrolli.kingdomsatwar.entity.voidrealm.ShatteredKingEntity.RIFT_TICKS
                        - io.github.sirschrolli.kingdomsatwar.entity.voidrealm.ShatteredKingEntity.RIFT_ERUPT, () -> {
                    helper.assertTrue(king.currentAttack().equals("none"), "Die Risse enden nicht: " + king.currentAttack());
                    soldier.setHealth(soldier.getMaxHealth());
                    king.forceAttack(level, "quake", soldier, true);
                    // fünf Blöcke vor der Stelle, wo die Fäuste einschlagen (zum König hin): nur der Ring erreicht ihn
                    net.minecraft.world.phys.Vec3 g = king.slamPoint();
                    net.minecraft.world.phys.Vec3 back = king.position().subtract(g).multiply(1.0, 0.0, 1.0).normalize();
                    soldier.teleportTo(g.x + back.x * 5.0, g.y, g.z + back.z * 5.0);
                    float quakeBefore = soldier.getHealth();
                    helper.runAfterDelay(io.github.sirschrolli.kingdomsatwar.entity.voidrealm.ShatteredKingEntity.QUAKE_SLAM + 2, () -> {
                        helper.assertTrue(soldier.getHealth() >= quakeBefore, "Die Fäuste treffen ihn auf fünf Blöcke");
                        helper.assertFalse(level.getEntitiesOfClass(SpellFxEntity.class, king.getBoundingBox().inflate(30),
                                e -> e.kind() == SpellFxEntity.Kind.SHOCKWAVE).isEmpty(), "Kein Bebenring");
                        helper.runAfterDelay(12, () -> {
                            helper.assertTrue(soldier.getHealth() < quakeBefore, "Der Bebenring trifft nicht");
                            helper.runAfterDelay(io.github.sirschrolli.kingdomsatwar.entity.voidrealm.ShatteredKingEntity.QUAKE_TICKS
                                    - io.github.sirschrolli.kingdomsatwar.entity.voidrealm.ShatteredKingEntity.QUAKE_SLAM - 10, () -> {
                                soldier.setHealth(soldier.getMaxHealth());
                                then.run();
                            });
                        });
                    });
                });
            });
        });
    }

    /**
     * Der Thron der Alten: als Bauwerk der Leere angemeldet (alle Biome der Leere), genau einer je Welt im Ring 450–800
     * Blöcke um die Mitte, sein Siegel unter dem Thron; die Oberwelt hat keinen.
     */
    private static void voidThrone(GameTestHelper helper) {
        var server = helper.getLevel().getServer();
        var registries = server.registryAccess();
        var structure = registries.lookupOrThrow(Registries.STRUCTURE)
                .get(io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures.VOID_THRONE_STRUCTURE);
        helper.assertTrue(structure.isPresent(), "Das Bauwerk Thron der Alten fehlt");
        var set = registries.lookupOrThrow(Registries.STRUCTURE_SET).get(io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures.VOID_THRONE_SET);
        helper.assertTrue(set.isPresent() && set.get().value().placement()
                instanceof io.github.sirschrolli.kingdomsatwar.worldgen.throne.ThronePlacement, "Die Platzierung des Throns fehlt");
        var placement = (io.github.sirschrolli.kingdomsatwar.worldgen.throne.ThronePlacement) set.get().value().placement();
        for (long seed : new long[] {1L, 42L, -77L, 20_261_108L, "kingdoms at war".hashCode()}) {
            var chunk = io.github.sirschrolli.kingdomsatwar.worldgen.throne.ThroneLocator.chunk(seed, placement);
            double distance = Math.sqrt(Math.pow(chunk.getMiddleBlockX(), 2) + Math.pow(chunk.getMiddleBlockZ(), 2));
            helper.assertTrue(distance > 430 && distance < 830, "Der Thron liegt nicht im Ring: " + distance);
            helper.assertTrue(chunk.equals(io.github.sirschrolli.kingdomsatwar.worldgen.throne.ThroneLocator.chunk(seed, placement)),
                    "Der Ort des Throns ist nicht fest");
        }
        var biomes = registries.lookupOrThrow(Registries.BIOME);
        for (ResourceKey<Biome> biome : VoidBiomes.ALL) {
            helper.assertTrue(biomes.getOrThrow(biome).is(io.github.sirschrolli.kingdomsatwar.registry.ModTags.Biomes.HAS_VOID_THRONE),
                    "Der Thron kann nicht über " + biome.identifier() + " schweben");
        }
        ServerLevel voidLevel = server.getLevel(VoidGate.THE_VOID);
        if (voidLevel != null) {
            BlockPos center = io.github.sirschrolli.kingdomsatwar.worldgen.throne.ThroneLocator.center(voidLevel);
            helper.assertTrue(center != null, "Kein Thron der Alten in der Leere");
            helper.assertTrue(center.getY() == io.github.sirschrolli.kingdomsatwar.worldgen.throne.ThronePiece.FLOOR + 1, "Falsche Höhe");
            BlockPos seal = io.github.sirschrolli.kingdomsatwar.worldgen.throne.ThroneLocator.seal(center);
            helper.assertTrue(seal.getZ() < center.getZ() && seal.getY() < center.getY(), "Das Siegel liegt nicht unter dem Thron");
        }
        helper.assertTrue(io.github.sirschrolli.kingdomsatwar.worldgen.throne.ThroneLocator.find(helper.getLevel()) == null,
                "Auch die Oberwelt hat einen Thron der Alten");
        helper.succeed();
    }

    /** Sternenrochen: mit Leerenblüten zu zähmen (jede mit einer Chance), danach trägt er seinen Freund – nur ihn. */
    private static void starRayRides(GameTestHelper helper) {
        floor(helper, 9);
        StarRayEntity ray = helper.spawnWithNoFreeWill(ModEntities.STAR_RAY.get(), new BlockPos(4, 2, 4));
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.snapTo(ray.getX() + 1.5, ray.getY(), ray.getZ(), 90.0f, 0.0f);
        for (int i = 0; i < 60 && !ray.isTamed(); i++) {
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(VoidBlocks.VOID_BLOOM.get(), 1));
            ray.mobInteract(player, InteractionHand.MAIN_HAND);
            helper.assertTrue(player.getMainHandItem().isEmpty(), "Die Leerenblüte wird nicht verfüttert");
        }
        helper.assertTrue(ray.isTamed(), "Der Rochen lässt sich mit Leerenblüten nicht zähmen");
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        ray.mobInteract(player, InteractionHand.MAIN_HAND);
        helper.assertTrue(player.getVehicle() == ray, "Der gezähmte Rochen trägt seinen Freund nicht");
        helper.assertTrue(ray.getControllingPassenger() == player, "Der Reiter lenkt den Rochen nicht");
        player.stopRiding();
        Player stranger = helper.makeMockPlayer(GameType.SURVIVAL);
        stranger.snapTo(ray.getX() - 1.5, ray.getY(), ray.getZ(), -90.0f, 0.0f);
        ray.mobInteract(stranger, InteractionHand.MAIN_HAND);
        helper.assertTrue(stranger.getVehicle() == null, "Ein Fremder kann den Rochen reiten");
        helper.succeed();
    }
}
