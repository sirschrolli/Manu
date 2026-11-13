package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.entity.sea.AnglerfishEntity;
import io.github.sirschrolli.kingdomsatwar.entity.sea.GiantCrabEntity;
import io.github.sirschrolli.kingdomsatwar.entity.sea.GlowJellyfishEntity;
import io.github.sirschrolli.kingdomsatwar.entity.sea.MantaRayEntity;
import io.github.sirschrolli.kingdomsatwar.entity.sea.SharkEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks;
import java.util.List;
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
import net.minecraft.world.Difficulty;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/** Tests des Meeres: die fünf Meereswesen, ihr Verhalten, ihre Beute, Laterne, Eimer und Rezepte. */
final class SeaGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SEA_CREATURES =
            ModGameTests.TEST_FUNCTIONS.register("sea_creatures", () -> SeaGameTests::seaCreatures);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> MANTA_BREACHES =
            ModGameTests.TEST_FUNCTIONS.register("manta_breaches", () -> SeaGameTests::mantaBreaches);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> JELLYFISH_STINGS =
            ModGameTests.TEST_FUNCTIONS.register("jellyfish_stings", () -> SeaGameTests::jellyfishStings);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SHARK_HUNTS =
            ModGameTests.TEST_FUNCTIONS.register("shark_hunts", () -> SeaGameTests::sharkHunts);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ANGLERFISH_SNAPS =
            ModGameTests.TEST_FUNCTIONS.register("anglerfish_snaps", () -> SeaGameTests::anglerfishSnaps);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> GIANT_CRABS =
            ModGameTests.TEST_FUNCTIONS.register("giant_crabs", () -> SeaGameTests::giantCrabs);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SEA_ITEMS =
            ModGameTests.TEST_FUNCTIONS.register("sea_items", () -> SeaGameTests::seaItems);

    private SeaGameTests() {}

    /** Lädt die Klasse (meldet die Testfunktionen an). */
    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var test : List.of(SEA_CREATURES, MANTA_BREACHES, JELLYFISH_STINGS, SHARK_HUNTS, ANGLERFISH_SNAPS, GIANT_CRABS, SEA_ITEMS)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 500, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
    }

    /** Ein Becken: Steinboden, Glasrand, Wasser bis zur vierten Schicht (darüber Luft). */
    private static void pool(GameTestHelper helper) {
        for (int x = 0; x < 9; x++) {
            for (int z = 0; z < 9; z++) {
                helper.setBlock(x, 0, z, Blocks.STONE);
                boolean rim = x == 0 || z == 0 || x == 8 || z == 8;
                for (int y = 1; y <= 4; y++) {
                    helper.setBlock(x, y, z, rim ? Blocks.GLASS : Blocks.WATER);
                }
            }
        }
    }

    private static void assertSpawns(GameTestHelper helper, ResourceKey<Biome> key, MobCategory category, EntityType<?> type) {
        Biome biome = helper.getLevel().registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(key).value();
        MobSpawnSettings spawns = biome.getAttributes().applyModifier(EnvironmentAttributes.NATURAL_MOB_SPAWNS, MobSpawnSettings.EMPTY);
        helper.assertTrue(spawns.getMobsToSpawn(category).unwrap().stream().anyMatch(entry -> entry.value().type() == type),
                key.identifier() + " ohne " + type.getDescriptionId());
    }

    /** Die fünf Wesen: richtige Gruppen, Beute, Spawn-Eier, Spawns in den passenden Meeren; im Wasser überleben sie. */
    private static void seaCreatures(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        pool(helper);
        helper.assertTrue(ModEntities.MANTA_RAY.get().getCategory() == MobCategory.WATER_CREATURE, "Mantarochen: falsche Gruppe");
        helper.assertTrue(ModEntities.GLOW_JELLYFISH.get().getCategory() == MobCategory.WATER_AMBIENT, "Leuchtqualle: falsche Gruppe");
        helper.assertTrue(ModEntities.SHARK.get().getCategory() == MobCategory.WATER_CREATURE, "Hai: falsche Gruppe");
        helper.assertTrue(ModEntities.ANGLERFISH.get().getCategory() == MobCategory.MONSTER, "Anglerfisch: falsche Gruppe");
        helper.assertTrue(ModEntities.GIANT_CRAB.get().getCategory() == MobCategory.CREATURE, "Riesenkrabbe: falsche Gruppe");
        assertSpawns(helper, Biomes.WARM_OCEAN, MobCategory.WATER_CREATURE, ModEntities.MANTA_RAY.get());
        assertSpawns(helper, Biomes.DEEP_OCEAN, MobCategory.WATER_CREATURE, ModEntities.SHARK.get());
        assertSpawns(helper, Biomes.LUKEWARM_OCEAN, MobCategory.WATER_AMBIENT, ModEntities.GLOW_JELLYFISH.get());
        assertSpawns(helper, Biomes.DEEP_OCEAN, MobCategory.MONSTER, ModEntities.ANGLERFISH.get());
        assertSpawns(helper, Biomes.BEACH, MobCategory.CREATURE, ModEntities.GIANT_CRAB.get());
        var loot = level.getServer().reloadableRegistries();
        List<EntityType<? extends Mob>> types = List.of(ModEntities.MANTA_RAY.get(), ModEntities.GLOW_JELLYFISH.get(), ModEntities.SHARK.get(),
                ModEntities.ANGLERFISH.get(), ModEntities.GIANT_CRAB.get());
        List<Mob> mobs = new java.util.ArrayList<>();
        for (int i = 0; i < types.size(); i++) {
            EntityType<? extends Mob> type = types.get(i);
            ResourceKey<LootTable> key = type.getDefaultLootTable().orElseThrow();
            helper.assertTrue(loot.getLootTable(key) != LootTable.EMPTY, "Beute fehlt: " + key.identifier());
            mobs.add(helper.spawnWithNoFreeWill(type, new BlockPos(2 + (i % 3) * 2, 2, 2 + (i / 3) * 4)));
        }
        for (var egg : SeaBlocks.spawnEggs()) {
            helper.assertTrue(new ItemStack(egg.get()).getItem() instanceof net.minecraft.world.item.SpawnEggItem, "Kein Spawn-Ei: " + egg.getId());
        }
        helper.runAfterDelay(40, () -> {
            for (Mob mob : mobs) {
                helper.assertTrue(mob.isAlive(), mob.getType().getDescriptionId() + " lebt nicht mehr");
                helper.assertTrue(mob.getHealth() >= mob.getMaxHealth() - 0.01f, mob.getType().getDescriptionId() + " ertrinkt im Wasser");
            }
            helper.succeed();
        });
    }

    /** Mantarochen: setzt zum Sprung an und schießt über die Wasseroberfläche hinaus. */
    private static void mantaBreaches(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        pool(helper);
        MantaRayEntity manta = helper.spawn(ModEntities.MANTA_RAY.get(), new BlockPos(4, 1, 4));
        double surface = helper.absolutePos(new BlockPos(0, 5, 0)).getY();
        helper.runAfterDelay(5, () -> manta.forceBreach(level));
        helper.succeedWhen(() -> {
            helper.assertTrue(manta.isAlive(), "Der Rochen lebt nicht mehr");
            helper.assertTrue(manta.getY() > surface + 0.2, "Der Rochen springt nicht aus dem Wasser (Höhe " + manta.getY() + ")");
        });
    }

    /** Leuchtqualle: nesselt, wer sie berührt (Gift), und lässt sich samt Farbe im Eimer fangen. */
    private static void jellyfishStings(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        level.getServer().setDifficulty(Difficulty.NORMAL, true);
        pool(helper);
        GlowJellyfishEntity jelly = helper.spawn(ModEntities.GLOW_JELLYFISH.get(), new BlockPos(4, 2, 4));
        jelly.setVariant(2);
        ItemStack bucket = jelly.getBucketItemStack();
        jelly.saveToBucketTag(bucket);
        helper.assertTrue(bucket.is(SeaBlocks.GLOW_JELLYFISH_BUCKET.get()), "Kein Eimer mit Leuchtqualle");
        GlowJellyfishEntity copy = ModEntities.GLOW_JELLYFISH.get().create(level, net.minecraft.world.entity.EntitySpawnReason.BUCKET);
        helper.assertTrue(copy != null, "Leuchtqualle lässt sich nicht erzeugen");
        copy.loadFromBucketTag(bucket.getOrDefault(DataComponents.BUCKET_ENTITY_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag());
        helper.assertValueEqual(copy.getVariant(), 2, "Farbe der Qualle nach dem Eimer");
        var pig = helper.spawnWithNoFreeWill(ModEntities.FARM_PIG.get(), new BlockPos(4, 2, 4));
        pig.setPos(jelly.getX(), jelly.getY(), jelly.getZ());
        helper.succeedWhen(() -> helper.assertTrue(pig.hasEffect(MobEffects.POISON), "Die Qualle nesselt nicht"));
    }

    /** Hai: kreist um seine Beute und beißt dann zu. */
    private static void sharkHunts(GameTestHelper helper) {
        pool(helper);
        SharkEntity shark = helper.spawn(ModEntities.SHARK.get(), new BlockPos(2, 2, 2));
        var cod = helper.spawnWithNoFreeWill(ModEntities.COD.get(), new BlockPos(6, 2, 6));
        float health = cod.getHealth();
        helper.runAfterDelay(2, () -> shark.setTarget(cod));
        helper.succeedWhen(() -> helper.assertTrue(!cod.isAlive() || cod.getHealth() < health, "Der Hai beißt nicht zu"));
    }

    /** Anglerfisch: lauert, bis die Beute nah ist, und schnappt dann blitzschnell zu. */
    private static void anglerfishSnaps(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        level.getServer().setDifficulty(Difficulty.NORMAL, true);
        pool(helper);
        AnglerfishEntity angler = helper.spawn(ModEntities.ANGLERFISH.get(), new BlockPos(2, 2, 4));
        var pig = helper.spawnWithNoFreeWill(ModEntities.FARM_PIG.get(), new BlockPos(5, 2, 4));
        float health = pig.getHealth();
        helper.runAfterDelay(2, () -> angler.setTarget(pig));
        helper.succeedWhen(() -> helper.assertTrue(pig.getHealth() < health, "Der Anglerfisch schnappt nicht zu"));
    }

    /** Riesenkrabbe: frisst Seetang, vermehrt sich und kneift zu, wenn man sie reizt. */
    private static void giantCrabs(GameTestHelper helper) {
        for (int x = 0; x < 9; x++) {
            for (int z = 0; z < 9; z++) {
                helper.setBlock(x, 0, z, Blocks.SAND);
            }
        }
        GiantCrabEntity a = helper.spawn(ModEntities.GIANT_CRAB.get(), new BlockPos(2, 1, 2));
        GiantCrabEntity b = helper.spawn(ModEntities.GIANT_CRAB.get(), new BlockPos(3, 1, 2));
        helper.assertTrue(a.isFood(new ItemStack(Items.KELP)) && a.isFood(new ItemStack(Items.SEAGRASS)), "Krabben fressen keinen Seetang");
        helper.assertFalse(a.isFood(new ItemStack(Items.WHEAT)), "Krabben fressen Weizen");
        a.setInLove(null);
        b.setInLove(null);
        GiantCrabEntity c = helper.spawn(ModEntities.GIANT_CRAB.get(), new BlockPos(6, 1, 6));
        var pig = helper.spawnWithNoFreeWill(ModEntities.FARM_PIG.get(), new BlockPos(7, 1, 6));
        float health = pig.getHealth();
        helper.runAfterDelay(2, () -> c.setTarget(pig));
        helper.succeedWhen(() -> {
            helper.assertTrue(pig.getHealth() < health, "Die Krabbe kneift nicht zu");
            helper.assertFalse(helper.getLevel().getEntitiesOfClass(GiantCrabEntity.class,
                    new net.minecraft.world.phys.AABB(helper.absolutePos(new BlockPos(4, 1, 4))).inflate(8.0), GiantCrabEntity::isBaby).isEmpty(),
                    "Keine junge Krabbe");
        });
    }

    /** Krabbenschere (Reichweite in der Zweithand), Tiefseelaterne (Licht), Rezepte und Trank. */
    private static void seaItems(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ItemStack claw = new ItemStack(SeaBlocks.CRAB_CLAW.get());
        var modifiers = claw.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS,
                net.minecraft.world.item.component.ItemAttributeModifiers.EMPTY);
        helper.assertTrue(modifiers.modifiers().stream().anyMatch(e -> e.attribute().is(Attributes.BLOCK_INTERACTION_RANGE.unwrapKey().orElseThrow())
                && e.slot() == EquipmentSlotGroup.OFFHAND && e.modifier().amount() > 1.0), "Die Krabbenschere verlängert die Reichweite nicht");
        helper.setBlock(4, 1, 4, SeaBlocks.DEEP_SEA_LANTERN.get());
        helper.assertValueEqual(level.getBlockState(helper.absolutePos(new BlockPos(4, 1, 4))).getLightEmission(), 15, "Licht der Tiefseelaterne");
        var recipes = level.getServer().getRecipeManager().recipeMap();
        for (String name : List.of("deep_sea_lantern", "arrows_from_shark_tooth", "cooked_crab_meat", "cooked_crab_meat_from_smoking",
                "cooked_crab_meat_from_campfire_cooking", "night_vision_from_glow_jelly_potion", "night_vision_from_glow_jelly_splash_potion")) {
            ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, KingdomsAtWar.id(name));
            helper.assertTrue(recipes.byKey(key) != null, "Rezept fehlt: " + key.identifier());
        }
        helper.assertTrue(new ItemStack(SeaBlocks.COOKED_CRAB_MEAT.get()).has(DataComponents.FOOD), "Gegartes Krabbenfleisch macht nicht satt");
        helper.succeed();
    }
}
