package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.entity.monster.AshWraithEntity;
import io.github.sirschrolli.kingdomsatwar.entity.monster.EmberWyrmEntity;
import io.github.sirschrolli.kingdomsatwar.entity.monster.MireShamblerEntity;
import io.github.sirschrolli.kingdomsatwar.entity.monster.MonsterShot;
import io.github.sirschrolli.kingdomsatwar.entity.monster.WerewolfEntity;
import io.github.sirschrolli.kingdomsatwar.entity.villager.VillagerEntity;
import io.github.sirschrolli.kingdomsatwar.item.MoonsilverBladeItem;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModTags;
import java.util.List;
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
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/** Tests der neuen Monster (0.69): Werwolf, Moorschlurfer, Glutwurm und Aschengeist. */
final class BeastGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> BASICS =
            ModGameTests.TEST_FUNCTIONS.register("beast_basics", () -> BeastGameTests::basics);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> WEREWOLF =
            ModGameTests.TEST_FUNCTIONS.register("beast_werewolf", () -> BeastGameTests::werewolf);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SHAMBLER =
            ModGameTests.TEST_FUNCTIONS.register("beast_mire_shambler", () -> BeastGameTests::shambler);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> WYRM =
            ModGameTests.TEST_FUNCTIONS.register("beast_ember_wyrm", () -> BeastGameTests::wyrm);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> WRAITH =
            ModGameTests.TEST_FUNCTIONS.register("beast_ash_wraith", () -> BeastGameTests::wraith);

    private BeastGameTests() {}

    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        event.registerTest(BASICS.getId(), new FunctionGameTestInstance(BASICS.key(),
                new TestData<>(environment, Level.OVERWORLD, ARENA, 100, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        for (var test : List.of(WEREWOLF, SHAMBLER, WYRM, WRAITH)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 500, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
    }

    private static void floor(GameTestHelper helper, Block block) {
        for (int x = 0; x < 9; x++) {
            for (int z = 0; z < 9; z++) {
                helper.setBlock(x, 0, z, block);
            }
        }
    }

    /** Die eigene Arena (die Nachbartests liegen 14 Blöcke weiter – ihr Hab und Gut bleibt unberührt). */
    private static AABB area(GameTestHelper helper) {
        return new AABB(Vec3.atLowerCornerOf(helper.absolutePos(new BlockPos(-2, -1, -2))),
                Vec3.atLowerCornerOf(helper.absolutePos(new BlockPos(11, 10, 11))));
    }

    private static void cleanup(GameTestHelper helper) {
        for (Entity e : helper.getLevel().getEntitiesOfClass(Entity.class, area(helper), e -> !(e instanceof ServerPlayer))) {
            e.discard();
        }
    }

    private static void expect(GameTestHelper helper, HolderLookup.RegistryLookup<Biome> biomes, ResourceKey<Biome> biome, TagKey<Biome> tag,
            boolean in) {
        helper.assertTrue(biomes.getOrThrow(biome).is(tag) == in, biome.identifier().getPath() + (in ? " fehlt in " : " gehört nicht in ")
                + tag.location().getPath());
    }

    // =====================================================================================
    // Grundlagen
    // =====================================================================================

    /**
     * Alle vier sind Monster mit Beute, Münzen und Spawn-Ei; Glutwurm und Aschengeist sind feuerfest, der Aschengeist kennt
     * keinen Fallschaden, der Glutwurm erscheint in Lava; jeder lebt in seinen Biomen (und nur dort); die Mondsilberklinge
     * ist ein Fluch für Werwölfe.
     */
    private static void basics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (EntityType<?> type : List.of(ModEntities.WEREWOLF.get(), ModEntities.MIRE_SHAMBLER.get(), ModEntities.EMBER_WYRM.get(),
                ModEntities.ASH_WRAITH.get())) {
            String name = BuiltInRegistries.ENTITY_TYPE.getKey(type).getPath();
            helper.assertTrue(type.getCategory() == MobCategory.MONSTER, name + ": keine Monstergruppe");
            helper.assertTrue(type.getDefaultLootTable().isPresent(), name + ": keine Beute");
            helper.assertTrue(type.builtInRegistryHolder().is(ModTags.EntityTypes.DROPS_COINS), name + ": keine Münzen");
            helper.assertTrue(BuiltInRegistries.ITEM.containsKey(KingdomsAtWar.id(name + "_spawn_egg")), name + ": kein Spawn-Ei");
        }
        helper.assertTrue(ModEntities.EMBER_WYRM.get().fireImmune() && ModEntities.ASH_WRAITH.get().fireImmune(),
                "Glutwurm oder Aschengeist brennen");
        helper.assertFalse(ModEntities.WEREWOLF.get().fireImmune(), "Der Werwolf ist feuerfest");
        helper.assertTrue(ModEntities.ASH_WRAITH.get().builtInRegistryHolder().is(EntityTypeTags.FALL_DAMAGE_IMMUNE),
                "Der Aschengeist nimmt Fallschaden");
        helper.assertTrue(ModEntities.WEREWOLF.get().builtInRegistryHolder().is(ModTags.EntityTypes.WEREBEASTS), "Der Werwolf ist kein Werwesen");
        helper.assertTrue(SpawnPlacements.getPlacementType(ModEntities.EMBER_WYRM.get()) == SpawnPlacementTypes.IN_LAVA,
                "Der Glutwurm erscheint nicht in Lava");
        var biomes = level.registryAccess().lookupOrThrow(Registries.BIOME);
        expect(helper, biomes, Biomes.FOREST, ModTags.Biomes.HAS_WEREWOLVES, true);
        expect(helper, biomes, Biomes.TAIGA, ModTags.Biomes.HAS_WEREWOLVES, true);
        expect(helper, biomes, Biomes.DESERT, ModTags.Biomes.HAS_WEREWOLVES, false);
        expect(helper, biomes, Biomes.SWAMP, ModTags.Biomes.HAS_MIRE_SHAMBLERS, true);
        expect(helper, biomes, Biomes.MANGROVE_SWAMP, ModTags.Biomes.HAS_MIRE_SHAMBLERS, true);
        expect(helper, biomes, Biomes.PLAINS, ModTags.Biomes.HAS_MIRE_SHAMBLERS, false);
        expect(helper, biomes, Biomes.NETHER_WASTES, ModTags.Biomes.HAS_EMBER_WYRMS, true);
        expect(helper, biomes, Biomes.BASALT_DELTAS, ModTags.Biomes.HAS_EMBER_WYRMS, true);
        expect(helper, biomes, Biomes.WARPED_FOREST, ModTags.Biomes.HAS_EMBER_WYRMS, false);
        expect(helper, biomes, Biomes.SOUL_SAND_VALLEY, ModTags.Biomes.HAS_ASH_WRAITHS, true);
        expect(helper, biomes, Biomes.CRIMSON_FOREST, ModTags.Biomes.HAS_ASH_WRAITHS, false);
        WerewolfEntity wolf = helper.spawnWithNoFreeWill(ModEntities.WEREWOLF.get(), new BlockPos(4, 1, 4));
        helper.assertTrue(MoonsilverBladeItem.isBane(wolf), "Die Mondsilberklinge ist kein Fluch für Werwölfe");
        cleanup(helper);
        helper.succeed();
    }

    // =====================================================================================
    // Werwolf
    // =====================================================================================

    /** Der Werwolf heult (und ist danach schneller und stärker) und springt sein Opfer aus sechs Blöcken an. */
    private static void werewolf(GameTestHelper helper) {
        floor(helper, Blocks.GRASS_BLOCK);
        VillagerEntity villager = helper.spawnWithNoFreeWill(ModEntities.VILLAGER.get(), new BlockPos(7, 1, 4));
        WerewolfEntity wolf = helper.spawn(ModEntities.WEREWOLF.get(), new BlockPos(1, 1, 4));
        wolf.forceHowl();
        helper.assertTrue(wolf.isHowling(), "Er heult nicht");
        helper.runAfterDelay(WerewolfEntity.HOWL_TICKS + 2, () -> {
            helper.assertFalse(wolf.isHowling(), "Das Heulen endet nicht");
            helper.assertTrue(wolf.hasEffect(MobEffects.SPEED) && wolf.hasEffect(MobEffects.STRENGTH), "Das Heulen stärkt ihn nicht");
            villager.setHealth(villager.getMaxHealth());
            Vec3 start = helper.absoluteVec(new Vec3(1.5, 1.0, 4.5));
            wolf.teleportTo(start.x, start.y, start.z);
            wolf.setDeltaMovement(Vec3.ZERO);
            wolf.setTarget(villager);
            wolf.forcePounce(villager);
            float before = villager.getHealth();
            helper.runAfterDelay(3, () -> helper.assertFalse(wolf.onGround(), "Er springt nicht"));
            helper.runAfterDelay(14, () -> {
                helper.assertTrue(villager.getHealth() < before, "Der Sprung trifft nicht");
                cleanup(helper);
                helper.succeed();
            });
        });
    }

    // =====================================================================================
    // Moorschlurfer
    // =====================================================================================

    /**
     * Der Moorschlurfer lauert versunken (und rührt sich nicht), ein Treffer lässt ihn aufsteigen; er schleudert Schlamm (der
     * verlangsamt), packt mit Wurzeln zu (hält fest und schadet), lässt los, wer kräftig zuschlägt, und heilt im Wasser.
     */
    private static void shambler(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper, Blocks.MUD);
        VillagerEntity villager = helper.spawnWithNoFreeWill(ModEntities.VILLAGER.get(), new BlockPos(7, 1, 7));
        // erst ohne eigenen Willen (sonst wittert er womöglich Wesen aus den Nachbartests)
        MireShamblerEntity shambler = helper.spawnWithNoFreeWill(ModEntities.MIRE_SHAMBLER.get(), new BlockPos(2, 1, 2));
        shambler.forceSubmerge();
        helper.assertTrue(shambler.isSubmerged(), "Er versinkt nicht");
        helper.runAfterDelay(10, () -> {
            helper.assertTrue(shambler.isSubmerged(), "Er taucht grundlos auf");
            helper.assertFalse(shambler.isPushable(), "Versunken lässt er sich schieben");
            shambler.hurtServer(level, level.damageSources().generic(), 1.0f);
            helper.assertTrue(!shambler.isSubmerged() && shambler.isRising(), "Getroffen steigt er nicht auf");
            shambler.setNoAi(false);
            helper.runAfterDelay(MireShamblerEntity.RISE_TICKS + 2, () -> {
                helper.assertFalse(shambler.isRising(), "Er steigt endlos auf");
                shambler.forceThrow(villager);
                helper.runAfterDelay(MireShamblerEntity.THROW_RELEASE + 2, () -> {
                    helper.assertFalse(level.getEntitiesOfClass(MonsterShot.class, area(helper), s -> s.kind() == MonsterShot.Kind.MUD).isEmpty(),
                            "Kein Schlammklumpen (Ziel " + shambler.getTarget() + "/" + shambler.getTargetUnchecked() + ", Opfer lebt "
                                    + villager.isAlive() + "/" + villager.isInvulnerable() + ")");
                    helper.runAfterDelay(30, () -> {
                        helper.assertTrue(villager.hasEffect(MobEffects.SLOWNESS), "Der Schlamm trifft nicht");
                        grasp(helper, level, shambler, villager);
                    });
                });
            });
        });
    }

    private static void grasp(GameTestHelper helper, ServerLevel level, MireShamblerEntity shambler, VillagerEntity villager) {
        villager.removeAllEffects();
        villager.setHealth(villager.getMaxHealth());
        Vec3 toward = helper.absoluteVec(new Vec3(4.5, 1.0, 4.5)).subtract(shambler.position()).multiply(1.0, 0.0, 1.0).normalize();
        villager.teleportTo(shambler.getX() + toward.x * 2.0, shambler.getY(), shambler.getZ() + toward.z * 2.0);
        shambler.forceGrasp(villager);
        helper.assertTrue(shambler.isGrasping(), "Er packt nicht zu");
        helper.runAfterDelay(14, () -> {
            helper.assertTrue(villager.getHealth() < villager.getMaxHealth(), "Der Wurzelgriff schadet nicht");
            var slow = villager.getEffect(MobEffects.SLOWNESS);
            helper.assertTrue(slow != null && slow.getAmplifier() >= 3, "Der Wurzelgriff hält nicht fest");
            shambler.hurtServer(level, level.damageSources().generic(), 10.0f);
            helper.assertFalse(shambler.isGrasping(), "Wer kräftig zuschlägt, kommt nicht frei");
            // versunken lauert er – steht sein Opfer neben ihm, steigt er auf
            shambler.forceSubmerge();
            shambler.setTarget(villager);
            helper.runAfterDelay(3, () -> {
                helper.assertTrue(!shambler.isSubmerged(), "Er bleibt versunken, obwohl sein Opfer neben ihm steht");
                helper.runAfterDelay(MireShamblerEntity.RISE_TICKS + 2, () -> heal(helper, shambler, villager));
            });
        });
    }

    /** Im Wasser heilt er (ein Becken, aus dem nichts herausläuft). */
    private static void heal(GameTestHelper helper, MireShamblerEntity shambler, VillagerEntity villager) {
        villager.discard();
        shambler.setTarget(null);
        for (int x = 2; x <= 6; x++) {
            for (int z = 2; z <= 6; z++) {
                boolean rim = x == 2 || x == 6 || z == 2 || z == 6;
                helper.setBlock(x, 1, z, rim ? Blocks.MUD : Blocks.WATER);
            }
        }
        Vec3 pool = helper.absoluteVec(new Vec3(4.5, 1.0, 4.5));
        shambler.teleportTo(pool.x, pool.y, pool.z);
        shambler.setHealth(20.0f);
        helper.runAfterDelay(45, () -> {
            helper.assertTrue(shambler.getHealth() > 20.0f, "Im Wasser heilt er nicht (" + shambler.getHealth() + ", im Wasser: "
                    + shambler.isInWater() + ")");
            for (int x = 2; x <= 6; x++) {
                for (int z = 2; z <= 6; z++) {
                    helper.setBlock(x, 1, z, Blocks.AIR);
                }
            }
            cleanup(helper);
            helper.succeed();
        });
    }

    // =====================================================================================
    // Glutwurm
    // =====================================================================================

    /**
     * Der Glutwurm taucht aus der Lava auf (und richtet sich auf): wer nah ist, den beißt er (und setzt ihn in Brand), nach
     * dem Fernen spuckt er Magma; an Land erkaltet er.
     */
    private static void wyrm(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper, Blocks.NETHERRACK);
        // ein Lavabecken mit Wänden (x 1–4, z 1–7)
        BlockState wall = Blocks.BLACKSTONE.defaultBlockState();
        for (int x = 0; x <= 5; x++) {
            for (int z = 0; z <= 8; z++) {
                for (int y = 1; y <= 3; y++) {
                    boolean rim = x == 0 || x == 5 || z == 0 || z == 8;
                    helper.setBlock(new BlockPos(x, y, z), rim ? wall : y <= 2 ? Blocks.LAVA.defaultBlockState() : Blocks.AIR.defaultBlockState());
                }
            }
        }
        VillagerEntity near = helper.spawnWithNoFreeWill(ModEntities.VILLAGER.get(), new BlockPos(7, 1, 3));
        VillagerEntity far = helper.spawnWithNoFreeWill(ModEntities.VILLAGER.get(), new BlockPos(8, 1, 8));
        EmberWyrmEntity wyrm = helper.spawn(ModEntities.EMBER_WYRM.get(), new BlockPos(2, 1, 2));
        EmberWyrmEntity stranded = helper.spawn(ModEntities.EMBER_WYRM.get(), new BlockPos(7, 1, 0));
        helper.runAfterDelay(4, () -> {
            helper.assertTrue(wyrm.isInLava(), "Der Glutwurm ist nicht in der Lava");
            wyrm.forceSurface(near);
            helper.assertTrue(wyrm.isRearing() && wyrm.getBbHeight() > 3.0f, "Er richtet sich nicht auf");
            float before = near.getHealth();
            helper.runAfterDelay(EmberWyrmEntity.BITE_TICK + 2, () -> {
                helper.assertTrue(near.getHealth() < before, "Der Biss trifft nicht");
                helper.assertTrue(near.isOnFire(), "Der Biss setzt nicht in Brand");
                helper.assertTrue(stranded.getHealth() < stranded.getMaxHealth(), "An Land erkaltet er nicht");
                helper.runAfterDelay(EmberWyrmEntity.SURFACE_TICKS - EmberWyrmEntity.BITE_TICK, () -> {
                    helper.assertFalse(wyrm.isRearing(), "Er taucht nicht wieder ab");
                    stranded.discard();
                    wyrm.forceSurface(far);
                    helper.runAfterDelay(EmberWyrmEntity.SPIT_TICKS[0] + 4, () -> {
                        helper.assertFalse(level.getEntitiesOfClass(MonsterShot.class, area(helper), s -> s.kind() == MonsterShot.Kind.MAGMA)
                                .isEmpty(), "Er spuckt kein Magma (Ziel " + wyrm.getTarget() + "/" + wyrm.getTargetUnchecked() + ", fern lebt " + far.isAlive() + "/"
                                + far.isRemoved() + "/" + far.getHealth() + "/" + far.isInvulnerable() + "/" + far.isSpectator() + ", aufgerichtet " + wyrm.isRearing() + ", bei "
                                + helper.relativeVec(wyrm.position()) + ", Geschosse " + level.getEntitiesOfClass(MonsterShot.class, area(helper)).size()
                                + ", lebt " + wyrm.isAlive() + ")");
                        helper.runAfterDelay(EmberWyrmEntity.SPIT_TICKS[2] - EmberWyrmEntity.SPIT_TICKS[0] + 20, () -> {
                            helper.assertTrue(far.getHealth() < far.getMaxHealth() || far.isOnFire(), "Das Magma trifft nicht");
                            for (int x = 0; x <= 8; x++) {
                                for (int z = 0; z <= 8; z++) {
                                    for (int y = 1; y <= 3; y++) {
                                        helper.setBlock(x, y, z, Blocks.AIR);
                                    }
                                }
                            }
                            cleanup(helper);
                            helper.succeed();
                        });
                    });
                });
            });
        });
    }

    // =====================================================================================
    // Aschengeist
    // =====================================================================================

    /**
     * Der Aschengeist schleudert Seelenfeuer (fliegt gerade, schwächt), lässt eine Aschewolke um sein Opfer aufwallen
     * (blendet) und verweht zu Asche (unsichtbar, unverwundbar), bis er sich wieder zusammensetzt.
     */
    private static void wraith(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper, Blocks.SOUL_SOIL);
        VillagerEntity villager = helper.spawnWithNoFreeWill(ModEntities.VILLAGER.get(), new BlockPos(4, 1, 1));
        AshWraithEntity wraith = helper.spawn(ModEntities.ASH_WRAITH.get(), new BlockPos(4, 3, 7));
        helper.runAfterDelay(2, () -> {
            wraith.forceBolt(villager);
            helper.runAfterDelay(AshWraithEntity.BOLT_RELEASE + 2, () -> {
                var bolts = level.getEntitiesOfClass(MonsterShot.class, area(helper), s -> s.kind() == MonsterShot.Kind.SOUL);
                helper.assertFalse(bolts.isEmpty(), "Kein Seelenfeuer");
                helper.assertTrue(bolts.getFirst().getGravity() == 0.0, "Das Seelenfeuer fällt");
                helper.runAfterDelay(20, () -> {
                    helper.assertTrue(villager.hasEffect(MobEffects.WEAKNESS), "Das Seelenfeuer trifft nicht");
                    villager.removeAllEffects();
                    villager.clearFire();
                    villager.setHealth(villager.getMaxHealth());
                    wraith.forceCloud(villager);
                    helper.runAfterDelay(AshWraithEntity.CLOUD_RELEASE + 2, () -> {
                        helper.assertFalse(level.getEntitiesOfClass(AreaEffectCloud.class, villager.getBoundingBox().inflate(2.0)).isEmpty(),
                                "Keine Aschewolke");
                        helper.runAfterDelay(10, () -> {
                            helper.assertTrue(villager.hasEffect(MobEffects.BLINDNESS), "Die Aschewolke blendet nicht");
                            fade(helper, level, wraith);
                        });
                    });
                });
            });
        });
    }

    private static void fade(GameTestHelper helper, ServerLevel level, AshWraithEntity wraith) {
        wraith.forceFade();
        helper.assertTrue(wraith.isFaded() && wraith.isInvisible(), "Er verweht nicht");
        float hp = wraith.getHealth();
        wraith.hurtServer(level, level.damageSources().generic(), 6.0f);
        helper.assertTrue(wraith.getHealth() >= hp, "Verweht ist er verwundbar");
        helper.runAfterDelay(AshWraithEntity.FADE_TICKS + 2, () -> {
            helper.assertTrue(!wraith.isFaded() && !wraith.isInvisible(), "Er setzt sich nicht wieder zusammen");
            cleanup(helper);
            helper.succeed();
        });
    }
}
