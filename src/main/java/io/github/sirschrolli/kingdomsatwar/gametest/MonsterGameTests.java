package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.entity.creature.farm.FarmChicken;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.BogBones;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.BoneHorse;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Boneman;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.DrownedCorpse;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.DryBones;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.DryCamel;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Hag;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Monsters;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Mummy;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Ooze;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.PlantCreeper;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Revenant;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.UndeadHorse;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.UndeadVillager;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.WildSpider;
import io.github.sirschrolli.kingdomsatwar.entity.villager.VillageProfession;
import io.github.sirschrolli.kingdomsatwar.entity.villager.VillagerEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import net.minecraft.tags.TagKey;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.Difficulty;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.animal.camel.CamelHusk;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.equine.SkeletonTrapGoal;
import net.minecraft.world.entity.monster.skeleton.Parched;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/** Tests der eigenen Monster, die die Vanilla-Monster ersetzen (E4a). */
final class MonsterGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SPAWN =
            ModGameTests.TEST_FUNCTIONS.register("monster_spawn", () -> MonsterGameTests::monstersSpawn);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SPAWN_LISTS =
            ModGameTests.TEST_FUNCTIONS.register("monster_spawn_lists", () -> MonsterGameTests::spawnLists);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LOOKS =
            ModGameTests.TEST_FUNCTIONS.register("monster_looks_by_biome", () -> MonsterGameTests::looksByBiome);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CONVERSION =
            ModGameTests.TEST_FUNCTIONS.register("monster_vanilla_replaced", () -> MonsterGameTests::vanillaReplaced);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> JOCKEYS =
            ModGameTests.TEST_FUNCTIONS.register("monster_jockeys", () -> MonsterGameTests::jockeys);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> INFECTION =
            ModGameTests.TEST_FUNCTIONS.register("monster_villager_infection", () -> MonsterGameTests::villagerInfection);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CURE =
            ModGameTests.TEST_FUNCTIONS.register("monster_villager_cure", () -> MonsterGameTests::villagerCure);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SPLIT =
            ModGameTests.TEST_FUNCTIONS.register("monster_ooze_split", () -> MonsterGameTests::oozeSplit);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SAVE_LOAD =
            ModGameTests.TEST_FUNCTIONS.register("monster_save_load", () -> MonsterGameTests::saveLoad);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SHEARING =
            ModGameTests.TEST_FUNCTIONS.register("monster_bog_bones_shearing", () -> MonsterGameTests::bogBonesShearing);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SKELETON_TRAP =
            ModGameTests.TEST_FUNCTIONS.register("monster_skeleton_trap", () -> MonsterGameTests::skeletonTrap);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> MOUNTS =
            ModGameTests.TEST_FUNCTIONS.register("monster_natural_riders", () -> MonsterGameTests::naturalRiders);

    private MonsterGameTests() {}

    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var test : List.of(SPAWN, SPAWN_LISTS, LOOKS, CONVERSION, JOCKEYS, INFECTION, CURE, SPLIT, SAVE_LOAD, SHEARING,
                SKELETON_TRAP, MOUNTS)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 300, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
    }

    /** Eigene Monster und die Vanilla-Monster, die sie ersetzen (gleiche Reihenfolge). */
    private static Map<EntityType<?>, EntityType<? extends Mob>> pairs() {
        Map<EntityType<?>, EntityType<? extends Mob>> map = new java.util.LinkedHashMap<>();
        map.put(EntityTypes.ZOMBIE, ModEntities.ZOMBIE.get());
        map.put(EntityTypes.HUSK, ModEntities.HUSK.get());
        map.put(EntityTypes.DROWNED, ModEntities.DROWNED.get());
        map.put(EntityTypes.ZOMBIE_VILLAGER, ModEntities.ZOMBIE_VILLAGER.get());
        map.put(EntityTypes.SKELETON, ModEntities.SKELETON.get());
        map.put(EntityTypes.STRAY, ModEntities.STRAY.get());
        map.put(EntityTypes.BOGGED, ModEntities.BOGGED.get());
        map.put(EntityTypes.PARCHED, ModEntities.PARCHED.get());
        map.put(EntityTypes.CREEPER, ModEntities.CREEPER.get());
        map.put(EntityTypes.SPIDER, ModEntities.SPIDER.get());
        map.put(EntityTypes.CAVE_SPIDER, ModEntities.CAVE_SPIDER.get());
        map.put(EntityTypes.SLIME, ModEntities.SLIME.get());
        map.put(EntityTypes.SULFUR_CUBE, ModEntities.SULFUR_CUBE.get());
        map.put(EntityTypes.WITCH, ModEntities.WITCH.get());
        map.put(EntityTypes.PHANTOM, ModEntities.PHANTOM.get());
        map.put(EntityTypes.SILVERFISH, ModEntities.SILVERFISH.get());
        map.put(EntityTypes.ZOMBIE_HORSE, ModEntities.ZOMBIE_HORSE.get());
        map.put(EntityTypes.SKELETON_HORSE, ModEntities.SKELETON_HORSE.get());
        map.put(EntityTypes.CAMEL_HUSK, ModEntities.CAMEL_HUSK.get());
        return map;
    }

    private static void floor(GameTestHelper helper) {
        for (int x = 0; x < 9; x++) {
            for (int z = 0; z < 9; z++) {
                helper.setBlock(x, 0, z, Blocks.STONE);
            }
        }
    }

    private static AABB arena(GameTestHelper helper) {
        return new AABB(helper.absolutePos(new BlockPos(4, 1, 4))).inflate(6.0);
    }

    // =====================================================================================

    /**
     * Alle neunzehn Monster erscheinen und leben (in friedlicher Welt würden sie verschwinden – hier auf "Schwer"); ihre
     * Beute verweist auf die der Vanilla-Gegenstücke; in jedem Vanilla-Tag ihres Gegenstücks stehen auch sie.
     */
    private static void monstersSpawn(GameTestHelper helper) {
        floor(helper);
        ServerLevel level = helper.getLevel();
        level.getServer().setDifficulty(Difficulty.HARD, true);
        var registries = level.getServer().reloadableRegistries();
        List<Mob> mobs = new ArrayList<>();
        int i = 0;
        for (var pair : pairs().entrySet()) {
            EntityType<? extends Mob> ours = pair.getValue();
            Mob mob = helper.spawnWithNoFreeWill(ours, new BlockPos(1 + (i % 4) * 2, 1, 1 + (i / 4) * 2));
            mob.setPersistenceRequired();
            mobs.add(mob);
            ResourceKey<LootTable> key = ours.getDefaultLootTable().orElseThrow();
            helper.assertTrue(registries.getLootTable(key) != LootTable.EMPTY || pair.getKey() == EntityTypes.SULFUR_CUBE,
                    "Beute fehlt: " + key.identifier());
            // jedes Vanilla-Tag des Gegenstücks gilt auch für das eigene Monster
            pair.getKey().builtInRegistryHolder().tags().forEach(tag -> helper.assertTrue(ours.builtInRegistryHolder().is(tag),
                    ours.getDescriptionId() + " fehlt im Tag " + tag.location()));
            helper.assertTrue(ours.getCategory() == pair.getKey().getCategory(), ours.getDescriptionId() + " in falscher Kategorie");
            i++;
        }
        helper.assertTrue(ModEntities.ZOMBIE.get().builtInRegistryHolder().is(EntityTypeTags.UNDEAD), "Wiedergänger ist nicht untot");
        helper.assertTrue(ModEntities.SPIDER.get().builtInRegistryHolder().is(EntityTypeTags.ARTHROPOD), "Spinne ist kein Gliederfüßer");
        helper.assertTrue(ModEntities.WITCH.get().builtInRegistryHolder().is(EntityTypeTags.RAIDERS), "Hexe zieht nicht in Überfälle");
        // Namen je Art
        Revenant revenant = (Revenant) mobs.getFirst();
        revenant.setLook(Revenant.Look.SOELDNER);
        helper.assertTrue(revenant.getName().getString().equals(Component.translatable("entity.kingdomsatwar.zombie.soeldner").getString()),
                "Name des Söldners: " + revenant.getName().getString());
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

    private static int weightOf(Biome biome, EntityType<?> type) {
        MobSpawnSettings spawns = biome.getModifiedEnvironmentAttributes().applyModifier(EnvironmentAttributes.NATURAL_MOB_SPAWNS,
                EnvironmentAttributes.NATURAL_MOB_SPAWNS.defaultValue());
        return spawns.getMobsToSpawn(type.getCategory()).unwrap().stream().filter(w -> w.value().type() == type).mapToInt(w -> w.weight())
                .sum();
    }

    /** Kein Vanilla-Monster steht mehr in einer Spawn-Liste der Oberwelt; die eigenen stehen dort mit Vanilla-Gewicht. */
    private static void spawnLists(GameTestHelper helper) {
        var biomes = helper.getLevel().registryAccess().lookupOrThrow(Registries.BIOME);
        Set<EntityType<?>> vanilla = pairs().keySet();
        biomes.listElements().filter(biome -> biome.is(net.minecraft.tags.BiomeTags.IS_OVERWORLD)).forEach(biome -> {
            for (MobCategory category : MobCategory.values()) {
                for (EntityType<?> type : spawnsOf(biome.value(), category)) {
                    helper.assertTrue(!vanilla.contains(type), "In " + biome.key().identifier() + " erscheint noch " + type.getDescriptionId());
                }
            }
        });
        record Expect(ResourceKey<Biome> biome, List<EntityType<?>> types) {}
        for (Expect e : List.of(
                new Expect(Biomes.PLAINS, List.of(ModEntities.ZOMBIE.get(), ModEntities.ZOMBIE_VILLAGER.get(), ModEntities.SKELETON.get(),
                        ModEntities.CREEPER.get(), ModEntities.SPIDER.get(), ModEntities.SLIME.get(), ModEntities.WITCH.get())),
                new Expect(Biomes.DESERT, List.of(ModEntities.HUSK.get(), ModEntities.PARCHED.get())),
                new Expect(Biomes.SNOWY_PLAINS, List.of(ModEntities.STRAY.get())),
                new Expect(Biomes.SWAMP, List.of(ModEntities.BOGGED.get(), ModEntities.SLIME.get())),
                new Expect(Biomes.RIVER, List.of(ModEntities.DROWNED.get())))) {
            List<EntityType<?>> found = spawnsOf(biomes.getOrThrow(e.biome()).value(), MobCategory.MONSTER);
            for (EntityType<?> type : e.types()) {
                helper.assertTrue(found.contains(type), "In " + e.biome().identifier() + " fehlt " + type.getDescriptionId() + " " + found);
            }
        }
        // Vanilla-Gewichte bleiben (Ebene: Zombie 90, Zombiepferd 5, Skelett 100, Creeper 100, Spinne 100)
        Biome plains = biomes.getOrThrow(Biomes.PLAINS).value();
        helper.assertTrue(weightOf(plains, ModEntities.ZOMBIE.get()) == 90, "Wiedergänger-Gewicht " + weightOf(plains, ModEntities.ZOMBIE.get()));
        helper.assertTrue(weightOf(plains, ModEntities.ZOMBIE_HORSE.get()) == 5, "Zombiepferd-Gewicht");
        helper.assertTrue(weightOf(plains, ModEntities.SKELETON.get()) == 100, "Gerippe-Gewicht");
        helper.assertTrue(weightOf(plains, ModEntities.CREEPER.get()) == 100, "Kriecher-Gewicht");
        helper.assertTrue(weightOf(plains, ModEntities.SPIDER.get()) == 100, "Spinnen-Gewicht");
        helper.succeed();
    }

    /** Kriecher und Spinnen richten sich nach dem Land; unter Tage wird der Kriecher zum Höhlenkriecher. */
    private static void looksByBiome(GameTestHelper helper) {
        var biomes = helper.getLevel().registryAccess().lookupOrThrow(Registries.BIOME);
        helper.assertTrue(PlantCreeper.lookFor(biomes.getOrThrow(Biomes.DESERT), false) == PlantCreeper.Look.WUESTE, "Wüstenkriecher");
        helper.assertTrue(PlantCreeper.lookFor(biomes.getOrThrow(Biomes.SNOWY_PLAINS), false) == PlantCreeper.Look.SCHNEE, "Schneekriecher");
        helper.assertTrue(PlantCreeper.lookFor(biomes.getOrThrow(Biomes.JUNGLE), false) == PlantCreeper.Look.DSCHUNGEL, "Dschungelkriecher");
        helper.assertTrue(PlantCreeper.lookFor(biomes.getOrThrow(Biomes.FOREST), false) == PlantCreeper.Look.WALD, "Waldkriecher");
        helper.assertTrue(PlantCreeper.lookFor(biomes.getOrThrow(Biomes.FOREST), true) == PlantCreeper.Look.HOEHLE, "Höhlenkriecher");
        helper.assertTrue(WildSpider.lookFor(biomes.getOrThrow(Biomes.BADLANDS)) == WildSpider.Look.WUESTE, "Wüstenspinne");
        helper.assertTrue(WildSpider.lookFor(biomes.getOrThrow(Biomes.SNOWY_TAIGA)) == WildSpider.Look.SCHNEE, "Schneespinne");
        helper.assertTrue(WildSpider.lookFor(biomes.getOrThrow(Biomes.SWAMP)) == WildSpider.Look.DSCHUNGEL, "Sumpfspinne");
        helper.assertTrue(WildSpider.lookFor(biomes.getOrThrow(Biomes.PLAINS)) == WildSpider.Look.WALD, "Waldspinne");
        helper.assertTrue(Monsters.wet(biomes.getOrThrow(Biomes.MANGROVE_SWAMP)), "Mangrovensumpf ist nass");
        helper.succeed();
    }

    /**
     * Neu erscheinende Vanilla-Monster (Spawn-Eier, Spawner, Befehle, Verwandlungen) werden zu den eigenen – mit ihrer
     * UUID (Prüfkammer-Spawner und Überfälle zählen ihre Wesen daran), Gesundheit, Ausrüstung und Namen.
     */
    private static void vanillaReplaced(GameTestHelper helper) {
        floor(helper);
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
            BlockPos pos = helper.absolutePos(new BlockPos(1 + (i % 4) * 2, 1, 1 + (i / 4) * 2));
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
                helper.assertTrue(!pairs().containsKey(mob.getType()), "noch ein Vanilla-Monster: " + mob.getType().getDescriptionId());
            }
            helper.succeed();
        });
    }

    /** Spinnenreiter und Hühnerreiter: Reittier und Reiter werden beide ersetzt und sitzen wieder auf. */
    private static void jockeys(GameTestHelper helper) {
        floor(helper);
        ServerLevel level = helper.getLevel();
        level.getServer().setDifficulty(Difficulty.HARD, true);
        Spider spider = EntityTypes.SPIDER.create(level, EntitySpawnReason.JOCKEY);
        Skeleton skeleton = EntityTypes.SKELETON.create(level, EntitySpawnReason.JOCKEY);
        Chicken chicken = EntityTypes.CHICKEN.create(level, EntitySpawnReason.JOCKEY);
        Zombie zombie = EntityTypes.ZOMBIE.create(level, EntitySpawnReason.JOCKEY);
        helper.assertTrue(spider != null && skeleton != null && chicken != null && zombie != null, "Vanilla-Wesen fehlen");
        BlockPos a = helper.absolutePos(new BlockPos(2, 1, 4));
        BlockPos b = helper.absolutePos(new BlockPos(6, 1, 4));
        for (Mob mob : List.of(spider, skeleton, chicken, zombie)) {
            mob.setNoAi(true);
            mob.setPersistenceRequired();
        }
        spider.snapTo(a.getX() + 0.5, a.getY(), a.getZ() + 0.5, 0.0f, 0.0f);
        skeleton.snapTo(a.getX() + 0.5, a.getY(), a.getZ() + 0.5, 0.0f, 0.0f);
        skeleton.startRiding(spider, true, false);
        chicken.snapTo(b.getX() + 0.5, b.getY(), b.getZ() + 0.5, 0.0f, 0.0f);
        chicken.setChickenJockey(true);
        zombie.setBaby(true);
        zombie.snapTo(b.getX() + 0.5, b.getY(), b.getZ() + 0.5, 0.0f, 0.0f);
        zombie.startRiding(chicken, true, false);
        level.addFreshEntityWithPassengers(spider);
        level.addFreshEntityWithPassengers(chicken);
        helper.runAfterDelay(3, () -> {
            var spiders = level.getEntitiesOfClass(WildSpider.class, arena(helper));
            helper.assertTrue(spiders.size() == 1, "Spinnen: " + spiders);
            helper.assertTrue(spiders.getFirst().getFirstPassenger() instanceof Boneman, "kein Gerippe auf der Spinne: "
                    + spiders.getFirst().getPassengers());
            var chickens = level.getEntitiesOfClass(FarmChicken.class, arena(helper));
            helper.assertTrue(chickens.size() == 1 && chickens.getFirst().isChickenJockey(), "kein Reithuhn: " + chickens);
            helper.assertTrue(chickens.getFirst().getFirstPassenger() instanceof Revenant revenant && revenant.isBaby(),
                    "kein kleiner Wiedergänger auf dem Huhn: " + chickens.getFirst().getPassengers());
            helper.assertTrue(level.getEntitiesOfClass(Mob.class, arena(helper), m -> pairs().containsKey(m.getType())
                    || m.getType() == EntityTypes.CHICKEN).isEmpty(), "Vanilla-Reiter übrig");
            helper.succeed();
        });
    }

    /**
     * Skelettfalle: Kommt ein Spieler an ein Fallen-Skelettpferd heran, schlägt ein Blitz ein und vier Gerippe mit Helm
     * sitzen auf vier Skelettpferden – alles eigene Wesen (die Falle selbst ruft Vanilla-Skelette und -Pferde).
     */
    private static void skeletonTrap(GameTestHelper helper) {
        floor(helper);
        ServerLevel level = helper.getLevel();
        level.getServer().setDifficulty(Difficulty.HARD, true);
        BoneHorse trap = helper.spawn(ModEntities.SKELETON_HORSE.get(), new BlockPos(4, 1, 4));
        trap.setTrap(true);
        new SkeletonTrapGoal(trap).tick();       // wie wenn ein Spieler herankommt (die Falle prüft nur auf Spielernähe)
        helper.runAfterDelay(6, () -> {
            var horses = level.getEntitiesOfClass(BoneHorse.class, arena(helper));
            helper.assertTrue(horses.size() == 4, "Skelettpferde nach der Falle: " + horses);
            for (BoneHorse horse : horses) {
                helper.assertTrue(horse.isTamed() && !horse.isTrap(), "Falle nicht ausgelöst: " + horse);
                helper.assertTrue(horse.getFirstPassenger() instanceof Boneman rider && !rider.getItemBySlot(EquipmentSlot.HEAD).isEmpty(),
                        "kein Gerippe mit Helm auf " + horse + ": " + horse.getPassengers());
            }
            helper.assertTrue(level.getEntitiesOfClass(Mob.class, arena(helper), m -> pairs().containsKey(m.getType())).isEmpty(),
                    "Vanilla-Wesen übrig");
            helper.succeed();
        });
    }

    /**
     * Reiter, die Vanilla beim natürlichen Erscheinen dazugibt: Mumie und Dörrgebein auf dem Dörrkamel (so wie es
     * Husk.finalizeSpawn in die Welt setzt) und ein Wiedergänger mit Speer auf dem Zombiepferd.
     */
    private static void naturalRiders(GameTestHelper helper) {
        floor(helper);
        ServerLevel level = helper.getLevel();
        level.getServer().setDifficulty(Difficulty.HARD, true);
        BlockPos a = helper.absolutePos(new BlockPos(2, 1, 4));
        Mummy mummy = ModEntities.HUSK.get().create(level, EntitySpawnReason.NATURAL);
        CamelHusk camel = EntityTypes.CAMEL_HUSK.create(level, EntitySpawnReason.NATURAL);
        Parched parched = EntityTypes.PARCHED.create(level, EntitySpawnReason.NATURAL);
        helper.assertTrue(mummy != null && camel != null && parched != null, "Wesen fehlen");
        for (Mob mob : List.<Mob>of(mummy, camel, parched)) {
            mob.snapTo(a.getX() + 0.5, a.getY(), a.getZ() + 0.5, 0.0f, 0.0f);
            mob.setPersistenceRequired();
        }
        mummy.startRiding(camel, true, true);
        level.addFreshEntity(camel);
        parched.startRiding(camel, false, false);
        level.addFreshEntityWithPassengers(parched);
        level.addFreshEntityWithPassengers(mummy);
        UndeadHorse horse = ModEntities.ZOMBIE_HORSE.get().spawn(level, helper.absolutePos(new BlockPos(6, 1, 4)), EntitySpawnReason.NATURAL);
        helper.assertTrue(horse != null, "kein Zombiepferd");
        horse.setPersistenceRequired();
        helper.runAfterDelay(3, () -> {
            var camels = level.getEntitiesOfClass(DryCamel.class, arena(helper));
            helper.assertTrue(camels.size() == 1, "Dörrkamele: " + camels);
            var riders = camels.getFirst().getPassengers();
            helper.assertTrue(riders.size() == 2 && riders.get(0) instanceof Mummy && riders.get(1) instanceof DryBones,
                    "Mumie vorn und Dörrgebein hinten erwartet: " + riders);
            helper.assertTrue(horse.isAlive() && horse.getFirstPassenger() instanceof Revenant revenant
                    && revenant.getMainHandItem().is(Items.IRON_SPEAR), "kein Wiedergänger mit Speer auf dem Zombiepferd: "
                    + horse.getPassengers());
            helper.assertTrue(level.getEntitiesOfClass(Mob.class, arena(helper), m -> pairs().containsKey(m.getType())).isEmpty(),
                    "Vanilla-Wesen übrig");
            helper.succeed();
        });
    }

    /** Wen ein Wiedergänger auf "Schwer" tötet, der steht als untoter Dorfbewohner mit Beruf, Gesicht und Namen wieder auf. */
    private static void villagerInfection(GameTestHelper helper) {
        floor(helper);
        ServerLevel level = helper.getLevel();
        level.getServer().setDifficulty(Difficulty.HARD, true);
        VillagerEntity villager = helper.spawnWithNoFreeWill(ModEntities.VILLAGER.get(), new BlockPos(4, 1, 4));
        villager.setup(VillageProfession.BAKER, 1, "Grete", villager.blockPosition(), "Probedorf", null, null, 7L);
        Revenant revenant = helper.spawnWithNoFreeWill(ModEntities.ZOMBIE.get(), new BlockPos(2, 1, 4));
        villager.hurtServer(level, level.damageSources().mobAttack(revenant), 1000.0f);
        helper.runAfterDelay(2, () -> {
            var undead = level.getEntitiesOfClass(UndeadVillager.class, arena(helper));
            helper.assertTrue(undead.size() == 1, "kein untoter Dorfbewohner: " + undead);
            UndeadVillager u = undead.getFirst();
            helper.assertTrue(u.getProfession() == VillageProfession.BAKER && u.getVariant() == 1, "falscher Beruf: " + u.getProfession());
            helper.assertTrue(u.hasCustomName() && "Grete".equals(u.getCustomName().getString()), "Name verloren");
            helper.assertTrue(u.formerSelf() != null && u.isPersistenceRequired(), "vergisst, wer er war");
            helper.assertTrue(level.getEntitiesOfClass(VillagerEntity.class, arena(helper)).isEmpty(), "der Dorfbewohner lebt noch");
            helper.succeed();
        });
    }

    /** Geheilt wird ein untoter Dorfbewohner wieder zum Dorfbewohner der Mod (nie zum Vanilla-Dorfbewohner) – derselbe wie vorher. */
    private static void villagerCure(GameTestHelper helper) {
        floor(helper);
        ServerLevel level = helper.getLevel();
        level.getServer().setDifficulty(Difficulty.HARD, true);
        VillagerEntity villager = helper.spawnWithNoFreeWill(ModEntities.VILLAGER.get(), new BlockPos(4, 1, 4));
        villager.setup(VillageProfession.FISHER, 2, "Hanne", villager.blockPosition(), "Probedorf", null, null, 9L);
        UndeadVillager undead = UndeadVillager.zombify(level, villager);
        helper.assertTrue(undead != null, "kein Untoter");
        villager.discard();
        // Heilung beginnen und bis kurz vor Schluss vorspulen (wie Trank der Schwäche und goldener Apfel)
        TagValueOutput out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
        undead.saveWithoutId(out);
        CompoundTag tag = out.buildResult();
        tag.putInt("ConversionTime", 2);
        undead.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), tag));
        undead.setNoAi(true);
        level.addFreshEntity(undead);
        helper.assertTrue(undead.isConverting(), "Heilung beginnt nicht");
        helper.runAfterDelay(10, () -> {
            helper.assertTrue(level.getEntitiesOfClass(UndeadVillager.class, arena(helper)).isEmpty(), "noch untot");
            helper.assertTrue(level.getEntitiesOfClass(Villager.class, arena(helper)).isEmpty(), "Vanilla-Dorfbewohner entstanden");
            var cured = level.getEntitiesOfClass(VillagerEntity.class, arena(helper));
            helper.assertTrue(cured.size() == 1, "kein geheilter Dorfbewohner: " + cured);
            VillagerEntity v = cured.getFirst();
            helper.assertTrue(v.getProfession() == VillageProfession.FISHER && v.getVariant() == 2, "falscher Beruf: " + v.getProfession());
            helper.assertTrue(v.hasCustomName() && "Hanne".equals(v.getCustomName().getString()), "Name verloren");
            helper.assertTrue(v.getHealth() == v.getMaxHealth(), "nicht gesund");
            helper.succeed();
        });
    }

    /** Ein großer Höhlenschleim zerfällt in kleine Höhlenschleime. */
    private static void oozeSplit(GameTestHelper helper) {
        floor(helper);
        ServerLevel level = helper.getLevel();
        level.getServer().setDifficulty(Difficulty.HARD, true);
        Ooze ooze = helper.spawnWithNoFreeWill(ModEntities.SLIME.get(), new BlockPos(4, 1, 4));
        ooze.setSize(4, true);
        ooze.setLook(Ooze.Look.HOEHLE);
        ooze.hurtServer(level, level.damageSources().genericKill(), 1000.0f);
        helper.runAfterDelay(25, () -> {
            var children = level.getEntitiesOfClass(Ooze.class, arena(helper), Entity::isAlive);
            helper.assertTrue(children.size() >= 2, "kein Zerfall: " + children);
            for (Ooze child : children) {
                helper.assertTrue(child.getSize() == 2 && child.getLook() == Ooze.Look.HOEHLE, "falscher kleiner Schleim: " + child.getLook());
            }
            helper.succeed();
        });
    }

    /** Art und Eigenheiten bleiben beim Speichern und Laden erhalten. */
    private static void saveLoad(GameTestHelper helper) {
        floor(helper);
        ServerLevel level = helper.getLevel();
        level.getServer().setDifficulty(Difficulty.HARD, true);
        Revenant revenant = helper.spawnWithNoFreeWill(ModEntities.ZOMBIE.get(), new BlockPos(1, 1, 1));
        revenant.setLook(Revenant.Look.MOENCH);
        Mummy mummy = helper.spawnWithNoFreeWill(ModEntities.HUSK.get(), new BlockPos(3, 1, 1));
        mummy.setLook(Mummy.Look.WUESTLING);
        DrownedCorpse corpse = helper.spawnWithNoFreeWill(ModEntities.DROWNED.get(), new BlockPos(5, 1, 1));
        corpse.setLook(DrownedCorpse.Look.FISCHER);
        Boneman bones = helper.spawnWithNoFreeWill(ModEntities.SKELETON.get(), new BlockPos(7, 1, 1));
        bones.setLook(Boneman.Look.RITTER);
        PlantCreeper creeper = helper.spawnWithNoFreeWill(ModEntities.CREEPER.get(), new BlockPos(1, 1, 3));
        creeper.setLook(PlantCreeper.Look.HOEHLE);
        Hag hag = helper.spawnWithNoFreeWill(ModEntities.WITCH.get(), new BlockPos(3, 1, 3));
        hag.setLook(Hag.Look.NACHT);
        UndeadVillager undead = helper.spawnWithNoFreeWill(ModEntities.ZOMBIE_VILLAGER.get(), new BlockPos(5, 1, 3));
        undead.setProfession(VillageProfession.MASON);
        undead.setVariant(2);
        for (Mob mob : List.<Mob>of(revenant, mummy, corpse, bones, creeper, hag, undead)) {
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

    /** Dem Moorgebein lassen sich die Pilze abscheren (wie beim Vanilla-Sumpfskelett). */
    private static void bogBonesShearing(GameTestHelper helper) {
        floor(helper);
        ServerLevel level = helper.getLevel();
        level.getServer().setDifficulty(Difficulty.HARD, true);
        BogBones bones = helper.spawnWithNoFreeWill(ModEntities.BOGGED.get(), new BlockPos(4, 1, 4));
        helper.assertTrue(bones.readyForShearing(), "nicht scherbar");
        bones.shear(level, net.minecraft.sounds.SoundSource.PLAYERS, net.minecraft.world.item.ItemStack.EMPTY);
        helper.assertTrue(bones.isSheared() && !bones.readyForShearing(), "Pilze noch da");
        helper.runAfterDelay(2, () -> {
            var drops = level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, arena(helper));
            helper.assertTrue(drops.stream().anyMatch(item -> item.getItem().is(Blocks.RED_MUSHROOM.asItem())
                    || item.getItem().is(Blocks.BROWN_MUSHROOM.asItem())), "keine Pilze: " + drops);
            helper.succeed();
        });
    }

    @SuppressWarnings("unused")
    private static boolean in(EntityType<?> type, TagKey<EntityType<?>> tag) {
        return type.builtInRegistryHolder().is(tag);
    }
}
