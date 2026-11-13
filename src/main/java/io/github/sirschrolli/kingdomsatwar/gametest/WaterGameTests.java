package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.entity.creature.water.Codfish;
import io.github.sirschrolli.kingdomsatwar.entity.creature.water.Newt;
import io.github.sirschrolli.kingdomsatwar.entity.creature.water.ReefFish;
import io.github.sirschrolli.kingdomsatwar.entity.creature.water.Salmonid;
import io.github.sirschrolli.kingdomsatwar.entity.creature.water.SeaDolphin;
import io.github.sirschrolli.kingdomsatwar.entity.creature.water.SeaTurtle;
import io.github.sirschrolli.kingdomsatwar.entity.creature.water.Toad;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import java.util.ArrayList;
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
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.Difficulty;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.fish.Salmon;
import net.minecraft.world.entity.animal.fish.TropicalFish;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.animal.frog.FrogVariants;
import net.minecraft.world.entity.animal.frog.Tadpole;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/** Tests der eigenen Wassertiere (E4c). */
final class WaterGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SPAWN =
            ModGameTests.TEST_FUNCTIONS.register("water_spawn", () -> WaterGameTests::spawn);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SPAWN_LISTS =
            ModGameTests.TEST_FUNCTIONS.register("water_spawn_lists", () -> WaterGameTests::spawnLists);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CONVERSION =
            ModGameTests.TEST_FUNCTIONS.register("water_vanilla_replaced", () -> WaterGameTests::vanillaReplaced);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> VARIANTS =
            ModGameTests.TEST_FUNCTIONS.register("water_variants_kept", () -> WaterGameTests::variantsKept);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TADPOLE =
            ModGameTests.TEST_FUNCTIONS.register("water_tadpole_grows", () -> WaterGameTests::tadpoleGrows);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SAVE_LOAD =
            ModGameTests.TEST_FUNCTIONS.register("water_save_load", () -> WaterGameTests::saveLoad);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FROGLIGHTS =
            ModGameTests.TEST_FUNCTIONS.register("water_froglights", () -> WaterGameTests::froglights);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> COURTSHIP =
            ModGameTests.TEST_FUNCTIONS.register("water_courtship", () -> WaterGameTests::courtship);

    private WaterGameTests() {}

    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var test : List.of(SPAWN, SPAWN_LISTS, CONVERSION, VARIANTS, TADPOLE, SAVE_LOAD, FROGLIGHTS, COURTSHIP)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 300, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
    }

    /** Eigene Wassertiere und die Vanilla-Tiere, die sie ersetzen. */
    private static Map<EntityType<?>, EntityType<? extends Mob>> pairs() {
        Map<EntityType<?>, EntityType<? extends Mob>> map = new LinkedHashMap<>();
        map.put(EntityTypes.COD, ModEntities.COD.get());
        map.put(EntityTypes.SALMON, ModEntities.SALMON.get());
        map.put(EntityTypes.PUFFERFISH, ModEntities.PUFFERFISH.get());
        map.put(EntityTypes.TROPICAL_FISH, ModEntities.TROPICAL_FISH.get());
        map.put(EntityTypes.SQUID, ModEntities.SQUID.get());
        map.put(EntityTypes.GLOW_SQUID, ModEntities.GLOW_SQUID.get());
        map.put(EntityTypes.DOLPHIN, ModEntities.DOLPHIN.get());
        map.put(EntityTypes.TURTLE, ModEntities.TURTLE.get());
        map.put(EntityTypes.AXOLOTL, ModEntities.AXOLOTL.get());
        map.put(EntityTypes.FROG, ModEntities.FROG.get());
        map.put(EntityTypes.TADPOLE, ModEntities.TADPOLE.get());
        map.put(EntityTypes.NAUTILUS, ModEntities.NAUTILUS.get());
        map.put(EntityTypes.ZOMBIE_NAUTILUS, ModEntities.ZOMBIE_NAUTILUS.get());
        return map;
    }

    /**
     * Das Vanilla-Gehirn sucht Partner nur unter dem Vanilla-Typ – die eigene Werbung bringt trotzdem Nachwuchs: zwei
     * verliebte Molche bekommen ein Junges, zwei Nautilusse auch, und von zwei Fröschen trägt einer danach Laich.
     */
    private static void courtship(GameTestHelper helper) {
        pool(helper);
        Newt[] newts = {helper.spawn(ModEntities.AXOLOTL.get(), new BlockPos(1, 1, 1)), helper.spawn(ModEntities.AXOLOTL.get(), new BlockPos(3, 1, 1))};
        var nautili = new net.minecraft.world.entity.animal.Animal[] {helper.spawn(ModEntities.NAUTILUS.get(), new BlockPos(5, 2, 5)),
                helper.spawn(ModEntities.NAUTILUS.get(), new BlockPos(7, 2, 5))};
        Toad[] toads = {helper.spawn(ModEntities.FROG.get(), new BlockPos(1, 1, 6)), helper.spawn(ModEntities.FROG.get(), new BlockPos(3, 1, 6))};
        for (var animal : List.of(newts[0], newts[1], nautili[0], nautili[1], toads[0], toads[1])) {
            animal.setInLove(null);
        }
        helper.succeedWhen(() -> {
            helper.assertTrue(!helper.getLevel().getEntitiesOfClass(Newt.class, arena(helper), Newt::isBaby).isEmpty(), "noch kein junger Molch");
            helper.assertTrue(!helper.getLevel().getEntitiesOfClass(nautili[0].getClass(), arena(helper),
                    net.minecraft.world.entity.AgeableMob::isBaby).isEmpty(), "noch kein junger Nautilus");
            helper.assertTrue(java.util.Arrays.stream(toads).anyMatch(t -> t.getBrain().hasMemoryValue(
                    net.minecraft.world.entity.ai.memory.MemoryModuleType.IS_PREGNANT)), "kein Frosch trägt Laich");
        });
    }

    /** Ein Becken: Steinboden, Glasrand und Wasser bis zur dritten Schicht. */
    private static void pool(GameTestHelper helper) {
        for (int x = 0; x < 9; x++) {
            for (int z = 0; z < 9; z++) {
                helper.setBlock(x, 0, z, Blocks.STONE);
                boolean rim = x == 0 || z == 0 || x == 8 || z == 8;
                for (int y = 1; y <= 3; y++) {
                    helper.setBlock(x, y, z, rim ? Blocks.GLASS : Blocks.WATER);
                }
            }
        }
    }

    private static AABB arena(GameTestHelper helper) {
        return new AABB(helper.absolutePos(new BlockPos(4, 1, 4))).inflate(6.0);
    }

    private static BlockPos slot(int i) {
        return new BlockPos(1 + (i % 4) * 2, 1, 1 + (i / 4) * 2);
    }

    /** Ein Wert, den Vanilla nur aus dem Spielstand liest. */
    private static void withTag(Mob mob, Consumer<CompoundTag> edit) {
        TagValueOutput out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, mob.registryAccess());
        mob.saveWithoutId(out);
        CompoundTag tag = out.buildResult();
        edit.accept(tag);
        mob.load(TagValueInput.create(ProblemReporter.DISCARDING, mob.registryAccess(), tag));
    }

    // =====================================================================================

    /** Alle 13 erscheinen und leben; Beute, Vanilla-Tags und Kategorie wie beim Gegenstück; Namen je Art. */
    private static void spawn(GameTestHelper helper) {
        pool(helper);
        ServerLevel level = helper.getLevel();
        level.getServer().setDifficulty(Difficulty.HARD, true);
        var registries = level.getServer().reloadableRegistries();
        List<Mob> mobs = new ArrayList<>();
        int i = 0;
        for (var pair : pairs().entrySet()) {
            EntityType<? extends Mob> ours = pair.getValue();
            ResourceKey<LootTable> key = ours.getDefaultLootTable().orElseThrow();
            helper.assertTrue(registries.getLootTable(key) != LootTable.EMPTY || pair.getKey() == EntityTypes.TADPOLE
                    || pair.getKey() == EntityTypes.FROG || pair.getKey() == EntityTypes.AXOLOTL, "Beute fehlt: " + key.identifier());
            pair.getKey().builtInRegistryHolder().tags().forEach(tag -> helper.assertTrue(ours.builtInRegistryHolder().is(tag),
                    ours.getDescriptionId() + " fehlt im Tag " + tag.location()));
            helper.assertTrue(ours.getCategory() == pair.getKey().getCategory(), ours.getDescriptionId() + " in falscher Kategorie");
            Mob mob = helper.spawnWithNoFreeWill(ours, slot(i));
            mob.setPersistenceRequired();
            mobs.add(mob);
            i++;
        }
        Codfish cod = (Codfish) mobs.getFirst();
        cod.setLook(Codfish.Look.SEELACHS);
        helper.assertTrue(cod.getName().getString().equals(Component.translatable("entity.kingdomsatwar.cod.seelachs").getString()),
                "Name des Seelachses: " + cod.getName().getString());
        helper.runAfterDelay(10, () -> {
            for (Mob mob : mobs) {
                helper.assertTrue(mob.isAlive() && !mob.isRemoved(), mob.getType().getDescriptionId() + " lebt nicht mehr");
            }
            helper.succeed();
        });
    }

    private static List<EntityType<?>> spawnsOf(Biome biome, MobCategory category) {
        MobSpawnSettings spawns = biome.getModifiedEnvironmentAttributes().applyModifier(EnvironmentAttributes.NATURAL_MOB_SPAWNS,
                EnvironmentAttributes.NATURAL_MOB_SPAWNS.defaultValue());
        List<EntityType<?>> found = new ArrayList<>();
        spawns.getMobsToSpawn(category).unwrap().forEach(w -> found.add(w.value().type()));
        return found;
    }

    /** In keinem Oberwelt-Biom erscheint noch ein Vanilla-Wassertier; Meere, Flüsse, Strände und Sümpfe haben die eigenen. */
    private static void spawnLists(GameTestHelper helper) {
        var biomes = helper.getLevel().registryAccess().lookupOrThrow(Registries.BIOME);
        biomes.listElements().filter(biome -> biome.is(net.minecraft.tags.BiomeTags.IS_OVERWORLD)).forEach(biome -> {
            for (MobCategory category : MobCategory.values()) {
                for (EntityType<?> type : spawnsOf(biome.value(), category)) {
                    helper.assertTrue(!pairs().containsKey(type), biome.key().identifier() + " lässt noch " + type.getDescriptionId() + " erscheinen");
                }
            }
        });
        Map<ResourceKey<Biome>, List<EntityType<?>>> expected = new HashMap<>();
        expected.put(net.minecraft.world.level.biome.Biomes.OCEAN, List.of(ModEntities.COD.get(), ModEntities.SQUID.get(), ModEntities.DOLPHIN.get()));
        expected.put(net.minecraft.world.level.biome.Biomes.RIVER, List.of(ModEntities.SALMON.get(), ModEntities.SQUID.get()));
        expected.put(net.minecraft.world.level.biome.Biomes.WARM_OCEAN, List.of(ModEntities.TROPICAL_FISH.get(), ModEntities.PUFFERFISH.get()));
        expected.put(net.minecraft.world.level.biome.Biomes.BEACH, List.of(ModEntities.TURTLE.get()));
        expected.put(net.minecraft.world.level.biome.Biomes.SWAMP, List.of(ModEntities.FROG.get()));
        expected.put(net.minecraft.world.level.biome.Biomes.LUSH_CAVES, List.of(ModEntities.AXOLOTL.get()));
        for (var entry : expected.entrySet()) {
            Biome biome = biomes.getOrThrow(entry.getKey()).value();
            List<EntityType<?>> all = new ArrayList<>();
            for (MobCategory category : MobCategory.values()) {
                all.addAll(spawnsOf(biome, category));
            }
            for (EntityType<?> type : entry.getValue()) {
                helper.assertTrue(all.contains(type), entry.getKey().identifier() + " ohne " + type.getDescriptionId() + ": " + all);
            }
        }
        helper.succeed();
    }

    /** Erscheint ein Vanilla-Wassertier, steht an seiner Stelle das eigene – mit derselben UUID und demselben Namen. */
    private static void vanillaReplaced(GameTestHelper helper) {
        pool(helper);
        ServerLevel level = helper.getLevel();
        level.getServer().setDifficulty(Difficulty.HARD, true);
        Map<UUID, EntityType<?>> expected = new HashMap<>();
        int i = 0;
        for (var pair : pairs().entrySet()) {
            Entity entity = pair.getKey().create(level, EntitySpawnReason.SPAWN_ITEM_USE);
            helper.assertTrue(entity instanceof Mob, "kein " + pair.getKey().getDescriptionId());
            Mob mob = (Mob) entity;
            mob.setCustomName(Component.literal("Probe " + i));
            mob.setNoAi(true);
            mob.setPersistenceRequired();
            BlockPos pos = helper.absolutePos(slot(i));
            mob.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0f, 0.0f);
            expected.put(mob.getUUID(), pair.getValue());
            level.addFreshEntity(mob);
            i++;
        }
        helper.runAfterDelay(2, () -> {
            for (var entry : expected.entrySet()) {
                Entity found = level.getEntity(entry.getKey());
                helper.assertTrue(found != null && found.getType() == entry.getValue(),
                        "statt " + entry.getValue().getDescriptionId() + " steht da " + (found == null ? "nichts" : found.getType().getDescriptionId()));
                helper.assertTrue(found.hasCustomName() && found.getCustomName().getString().startsWith("Probe"), "Name verloren");
            }
            for (Mob mob : level.getEntitiesOfClass(Mob.class, arena(helper))) {
                helper.assertTrue(!pairs().containsKey(mob.getType()), "noch ein Vanilla-Wassertier: " + mob.getType().getDescriptionId());
            }
            helper.succeed();
        });
    }

    /** Was Vanilla als Spielart speichert, bleibt: Axolotl-Farbe, Tropenfisch-Muster und -Farben, Lachsgröße, Froschart. */
    private static void variantsKept(GameTestHelper helper) {
        pool(helper);
        ServerLevel level = helper.getLevel();
        Axolotl axolotl = EntityTypes.AXOLOTL.create(level, EntitySpawnReason.SPAWN_ITEM_USE);
        TropicalFish fish = EntityTypes.TROPICAL_FISH.create(level, EntitySpawnReason.SPAWN_ITEM_USE);
        Salmon salmon = EntityTypes.SALMON.create(level, EntitySpawnReason.SPAWN_ITEM_USE);
        Frog frog = EntityTypes.FROG.create(level, EntitySpawnReason.SPAWN_ITEM_USE);
        helper.assertTrue(axolotl != null && fish != null && salmon != null && frog != null, "Vanilla-Tiere fehlen");
        int packed = new TropicalFish.Variant(TropicalFish.Pattern.BETTY, DyeColor.RED, DyeColor.WHITE).getPackedId();
        withTag(axolotl, tag -> tag.putInt("Variant", Axolotl.Variant.BLUE.getId()));
        withTag(fish, tag -> tag.putInt("Variant", packed));
        withTag(salmon, tag -> tag.putString("type", "large"));
        withTag(frog, tag -> tag.putString("variant", FrogVariants.COLD.identifier().toString()));
        int i = 0;
        List<UUID> ids = new ArrayList<>();
        for (Mob mob : List.<Mob>of(axolotl, fish, salmon, frog)) {
            mob.setNoAi(true);
            mob.setPersistenceRequired();
            BlockPos pos = helper.absolutePos(slot(i++));
            mob.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0f, 0.0f);
            ids.add(mob.getUUID());
            level.addFreshEntity(mob);
        }
        helper.runAfterDelay(2, () -> {
            helper.assertTrue(level.getEntity(ids.get(0)) instanceof Newt newt && newt.getVariant() == Axolotl.Variant.BLUE,
                    "Blaumolch verloren: " + level.getEntity(ids.get(0)));
            helper.assertTrue(level.getEntity(ids.get(1)) instanceof ReefFish reef && reef.getPattern() == TropicalFish.Pattern.BETTY
                    && reef.getBaseColor() == DyeColor.RED && reef.getPatternColor() == DyeColor.WHITE, "Rifffisch-Muster verloren");
            helper.assertTrue(level.getEntity(ids.get(2)) instanceof Salmonid s && s.getVariant() == Salmon.Variant.LARGE, "Lachsgröße verloren");
            helper.assertTrue(level.getEntity(ids.get(3)) instanceof Toad t && t.getVariant().is(FrogVariants.COLD), "Froschart verloren");
            helper.succeed();
        });
    }

    /** Eine Kaulquappe wird zum eigenen Frosch der Gegend – kein Vanilla-Frosch bleibt übrig. */
    private static void tadpoleGrows(GameTestHelper helper) {
        pool(helper);
        ServerLevel level = helper.getLevel();
        Tadpole tadpole = helper.spawn(ModEntities.TADPOLE.get(), new BlockPos(4, 2, 4));
        tadpole.setPersistenceRequired();
        withTag(tadpole, tag -> tag.putInt("Age", Tadpole.ticksToBeFrog - 5));
        helper.succeedWhen(() -> {
            var frogs = level.getEntitiesOfClass(Frog.class, arena(helper));
            helper.assertTrue(!frogs.isEmpty(), "noch kein Frosch");
            helper.assertTrue(frogs.stream().allMatch(f -> f instanceof Toad), "Vanilla-Frosch: " + frogs);
            helper.assertTrue(level.getEntitiesOfClass(Tadpole.class, arena(helper)).isEmpty(), "Kaulquappe noch da");
        });
    }

    /** Frisst ein eigener Frosch einen kleinen Magmawürfel, bleibt das Froschlicht seiner Art – wie beim Vanilla-Frosch. */
    private static void froglights(GameTestHelper helper) {
        pool(helper);
        ServerLevel level = helper.getLevel();
        level.getServer().setDifficulty(Difficulty.HARD, true);
        var variants = level.registryAccess().lookupOrThrow(Registries.FROG_VARIANT);
        Map<ResourceKey<net.minecraft.world.entity.animal.frog.FrogVariant>, net.minecraft.world.item.Item> expected = new LinkedHashMap<>();
        expected.put(FrogVariants.TEMPERATE, net.minecraft.world.item.Items.OCHRE_FROGLIGHT);
        expected.put(FrogVariants.WARM, net.minecraft.world.item.Items.PEARLESCENT_FROGLIGHT);
        expected.put(FrogVariants.COLD, net.minecraft.world.item.Items.VERDANT_FROGLIGHT);
        int i = 0;
        for (var entry : expected.entrySet()) {
            Toad frog = helper.spawnWithNoFreeWill(ModEntities.FROG.get(), slot(i * 2));
            frog.setComponent(net.minecraft.core.component.DataComponents.FROG_VARIANT, variants.getOrThrow(entry.getKey()));
            var cube = helper.spawnWithNoFreeWill(EntityTypes.MAGMA_CUBE, slot(i * 2 + 1).above(3));
            cube.setSize(1, true);
            cube.hurtServer(level, level.damageSources().mobAttack(frog), 100.0f);
            i++;
        }
        helper.runAfterDelay(5, () -> {
            var items = level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, arena(helper));
            for (var item : expected.values()) {
                helper.assertTrue(items.stream().anyMatch(e -> e.getItem().is(item)), "kein " + item + " gefallen: " + items);
            }
            helper.succeed();
        });
    }

    /** Arten und Spielarten überstehen Speichern und Laden. */
    private static void saveLoad(GameTestHelper helper) {
        pool(helper);
        ServerLevel level = helper.getLevel();
        Codfish cod = helper.spawnWithNoFreeWill(ModEntities.COD.get(), new BlockPos(1, 1, 1));
        cod.setLook(Codfish.Look.SEELACHS);
        Salmonid salmon = helper.spawnWithNoFreeWill(ModEntities.SALMON.get(), new BlockPos(3, 1, 1));
        salmon.setLook(Salmonid.Look.ROTLACHS);
        SeaDolphin dolphin = helper.spawnWithNoFreeWill(ModEntities.DOLPHIN.get(), new BlockPos(5, 1, 1));
        dolphin.setLook(SeaDolphin.Look.SCHWERTWAL);
        SeaTurtle turtle = helper.spawnWithNoFreeWill(ModEntities.TURTLE.get(), new BlockPos(1, 1, 5));
        turtle.setLook(SeaTurtle.Look.KARETT);
        for (Mob mob : List.<Mob>of(cod, salmon, dolphin, turtle)) {
            TagValueOutput out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
            mob.saveWithoutId(out);
            CompoundTag tag = out.buildResult();
            tag.remove("UUID");
            Mob copy = (Mob) mob.getType().create(level, EntitySpawnReason.LOAD);
            helper.assertTrue(copy != null, "keine Kopie");
            copy.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), tag));
            String original = mob.getName().getString();
            helper.assertTrue(copy.getName().getString().equals(original), "Art verloren: " + original + " -> " + copy.getName().getString());
        }
        helper.succeed();
    }
}
