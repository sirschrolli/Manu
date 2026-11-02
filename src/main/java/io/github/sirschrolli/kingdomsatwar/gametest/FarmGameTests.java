package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.entity.creature.farm.Cattle;
import io.github.sirschrolli.kingdomsatwar.entity.creature.farm.FarmChicken;
import io.github.sirschrolli.kingdomsatwar.entity.creature.farm.FarmDonkey;
import io.github.sirschrolli.kingdomsatwar.entity.creature.farm.FarmGoat;
import io.github.sirschrolli.kingdomsatwar.entity.creature.farm.FarmHorse;
import io.github.sirschrolli.kingdomsatwar.entity.creature.farm.FarmMule;
import io.github.sirschrolli.kingdomsatwar.entity.creature.farm.FarmPig;
import io.github.sirschrolli.kingdomsatwar.entity.creature.farm.FarmSheep;
import io.github.sirschrolli.kingdomsatwar.entity.creature.farm.Hare;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModTags;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.chicken.ChickenVariants;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
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

/** Tests der eigenen Hof- und Weidetiere, die die Vanilla-Tiere ersetzen. */
final class FarmGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SPAWN =
            ModGameTests.TEST_FUNCTIONS.register("farm_animals_spawn", () -> FarmGameTests::animalsSpawn);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SPAWN_LISTS =
            ModGameTests.TEST_FUNCTIONS.register("farm_spawn_lists", () -> FarmGameTests::spawnLists);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> BREEDS =
            ModGameTests.TEST_FUNCTIONS.register("farm_breeds_by_biome", () -> FarmGameTests::breedsByBiome);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CONVERSION =
            ModGameTests.TEST_FUNCTIONS.register("farm_vanilla_replaced", () -> FarmGameTests::vanillaReplaced);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> MILK =
            ModGameTests.TEST_FUNCTIONS.register("farm_milk_and_stew", () -> FarmGameTests::milkAndStew);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> EGGS =
            ModGameTests.TEST_FUNCTIONS.register("farm_chicken_eggs", () -> FarmGameTests::chickenEggs);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SHEARING =
            ModGameTests.TEST_FUNCTIONS.register("farm_sheep_shearing", () -> FarmGameTests::sheepShearing);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> HORSES =
            ModGameTests.TEST_FUNCTIONS.register("farm_horses", () -> FarmGameTests::horses);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> OFFSPRING =
            ModGameTests.TEST_FUNCTIONS.register("farm_offspring", () -> FarmGameTests::offspring);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SAVE_LOAD =
            ModGameTests.TEST_FUNCTIONS.register("farm_save_load", () -> FarmGameTests::saveLoad);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> GOAT_COURTSHIP =
            ModGameTests.TEST_FUNCTIONS.register("farm_goat_courtship", () -> FarmGameTests::goatCourtship);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> WOLF_HUNTS =
            ModGameTests.TEST_FUNCTIONS.register("farm_wolf_hunts", () -> FarmGameTests::wolfHunts);

    private FarmGameTests() {}

    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var test : List.of(SPAWN, SPAWN_LISTS, BREEDS, CONVERSION, MILK, EGGS, SHEARING, HORSES, OFFSPRING, SAVE_LOAD,
                GOAT_COURTSHIP, WOLF_HUNTS)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 200, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
    }

    private static List<EntityType<? extends Mob>> types() {
        return List.of(ModEntities.CATTLE.get(), ModEntities.FARM_PIG.get(), ModEntities.FARM_SHEEP.get(), ModEntities.FARM_CHICKEN.get(),
                ModEntities.FARM_HORSE.get(), ModEntities.FARM_DONKEY.get(), ModEntities.FARM_MULE.get(), ModEntities.FARM_GOAT.get(),
                ModEntities.HARE.get());
    }

    private static void floor(GameTestHelper helper) {
        for (int x = 0; x < 9; x++) {
            for (int z = 0; z < 9; z++) {
                helper.setBlock(x, 0, z, Blocks.GRASS_BLOCK);
            }
        }
    }

    // =====================================================================================

    /** Alle neun Tiere erscheinen, haben Beute (die der Vanilla-Tiere) und die nötigen Tags für Sattel und Harnisch. */
    private static void animalsSpawn(GameTestHelper helper) {
        floor(helper);
        var registries = helper.getLevel().getServer().reloadableRegistries();
        List<Mob> mobs = new ArrayList<>();
        int i = 0;
        for (EntityType<? extends Mob> type : types()) {
            mobs.add(helper.spawnWithNoFreeWill(type, new BlockPos(1 + (i % 3) * 3, 1, 1 + (i / 3) * 3)));
            ResourceKey<LootTable> key = type.getDefaultLootTable().orElseThrow();
            helper.assertTrue(registries.getLootTable(key) != LootTable.EMPTY, "Beute fehlt: " + key.identifier());
            helper.assertTrue(type.builtInRegistryHolder().is(EntityTypeTags.FOLLOWABLE_FRIENDLY_MOBS), type.getDescriptionId() + " fehlt im Tag der Freunde");
            i++;
        }
        for (var type : List.of(ModEntities.FARM_PIG.get(), ModEntities.FARM_HORSE.get(), ModEntities.FARM_DONKEY.get(),
                ModEntities.FARM_MULE.get())) {
            helper.assertTrue(type.builtInRegistryHolder().is(EntityTypeTags.CAN_EQUIP_SADDLE), type.getDescriptionId() + " kann keinen Sattel tragen");
        }
        helper.assertTrue(ModEntities.FARM_HORSE.get().builtInRegistryHolder().is(EntityTypeTags.CAN_WEAR_HORSE_ARMOR), "Pferd kann keinen Rossharnisch tragen");
        helper.runAfterDelay(10, () -> {
            for (Mob mob : mobs) {
                helper.assertTrue(mob.isAlive(), mob.getType().getDescriptionId() + " lebt nicht mehr");
            }
            helper.succeed();
        });
    }

    /** In den Spawn-Listen stehen die eigenen Tiere mit Vanilla-Gewichten, die Vanilla-Tiere nicht mehr. */
    private static void spawnLists(GameTestHelper helper) {
        var biomes = helper.getLevel().registryAccess().lookupOrThrow(Registries.BIOME);
        Set<EntityType<?>> vanilla = Set.of(EntityTypes.COW, EntityTypes.MOOSHROOM, EntityTypes.PIG, EntityTypes.SHEEP, EntityTypes.CHICKEN,
                EntityTypes.HORSE, EntityTypes.DONKEY, EntityTypes.MULE, EntityTypes.GOAT, EntityTypes.RABBIT);
        record Expect(ResourceKey<Biome> biome, List<EntityType<?>> types) {}
        for (Expect e : List.of(
                new Expect(Biomes.PLAINS, List.of(ModEntities.CATTLE.get(), ModEntities.FARM_PIG.get(), ModEntities.FARM_SHEEP.get(),
                        ModEntities.FARM_CHICKEN.get(), ModEntities.FARM_HORSE.get(), ModEntities.FARM_DONKEY.get(), ModEntities.HARE.get())),
                new Expect(Biomes.MUSHROOM_FIELDS, List.of(ModEntities.CATTLE.get())),
                new Expect(Biomes.FROZEN_PEAKS, List.of(ModEntities.FARM_GOAT.get())),
                new Expect(Biomes.DESERT, List.of(ModEntities.HARE.get())),
                new Expect(Biomes.MEADOW, List.of(ModEntities.FARM_SHEEP.get(), ModEntities.FARM_DONKEY.get(), ModEntities.FARM_GOAT.get())),
                new Expect(io.github.sirschrolli.kingdomsatwar.worldgen.overworld.OverworldBiomes.ROYAL_MEADOWS,
                        List.of(ModEntities.CATTLE.get(), ModEntities.FARM_HORSE.get())))) {
            Biome biome = biomes.getOrThrow(e.biome()).value();
            MobSpawnSettings spawns = biome.getModifiedEnvironmentAttributes().applyModifier(EnvironmentAttributes.NATURAL_MOB_SPAWNS,
                    EnvironmentAttributes.NATURAL_MOB_SPAWNS.defaultValue());
            List<EntityType<?>> found = new ArrayList<>();
            spawns.getMobsToSpawn(MobCategory.CREATURE).unwrap().forEach(w -> found.add(w.value().type()));
            for (EntityType<?> type : found) {
                helper.assertTrue(!vanilla.contains(type), "In " + e.biome().identifier() + " erscheint noch " + type.getDescriptionId());
            }
            for (EntityType<?> type : e.types()) {
                helper.assertTrue(found.contains(type), "In " + e.biome().identifier() + " fehlt " + type.getDescriptionId() + " " + found);
            }
        }
        helper.assertTrue(biomes.get(ModTags.Biomes.REPLACES_VANILLA_ANIMALS).map(set -> set.size() > 50).orElse(false),
                "zu wenige Biome ohne Vanilla-Tiere");
        helper.succeed();
    }

    /** Die Rassen richten sich nach dem Land: Pilzrind auf Pilzinseln, Schneeziege im Schnee, Wüstenhase in der Wüste … */
    private static void breedsByBiome(GameTestHelper helper) {
        var biomes = helper.getLevel().registryAccess().lookupOrThrow(Registries.BIOME);
        var random = helper.getLevel().getRandom();
        for (int i = 0; i < 20; i++) {
            helper.assertTrue(Cattle.breedFor(biomes.getOrThrow(Biomes.MUSHROOM_FIELDS), random) == Cattle.Breed.PILZ, "kein Pilzrind");
            var cold = Cattle.breedFor(biomes.getOrThrow(Biomes.SNOWY_TAIGA), random);
            helper.assertTrue(cold == Cattle.Breed.HOCHLAND || cold == Cattle.Breed.BRAUNVIEH, "im Schnee: " + cold);
            var warm = Cattle.breedFor(biomes.getOrThrow(Biomes.SAVANNA), random);
            helper.assertTrue(warm == Cattle.Breed.STEPPE || warm == Cattle.Breed.FLECKVIEH, "in der Savanne: " + warm);
            var goat = FarmGoat.breedFor(biomes.getOrThrow(Biomes.FROZEN_PEAKS), random);
            helper.assertTrue(goat == FarmGoat.Breed.SCHNEE || goat == FarmGoat.Breed.STEINBOCK, "Ziege im Eis: " + goat);
            helper.assertTrue(FarmGoat.breedFor(biomes.getOrThrow(Biomes.MEADOW), random) == FarmGoat.Breed.HAUS, "keine Hausziege");
            var pig = FarmPig.breedFor(biomes.getOrThrow(Biomes.TAIGA), random);
            helper.assertTrue(pig == FarmPig.Breed.WOLLSCHWEIN || pig == FarmPig.Breed.SATTELSCHWEIN, "Schwein im Norden: " + pig);
            var horse = FarmHorse.breedFor(biomes.getOrThrow(Biomes.SAVANNA), random);
            helper.assertTrue(horse != FarmHorse.Breed.KALTBLUT && horse != FarmHorse.Breed.FRIESE, "Pferd in der Savanne: " + horse);
        }
        helper.assertTrue(Hare.fromVariant(Rabbit.Variant.GOLD, biomes.getOrThrow(Biomes.DESERT), random) == Hare.Breed.WUESTENHASE,
                "kein Wüstenhase");
        helper.assertTrue(Hare.fromVariant(Rabbit.Variant.WHITE, biomes.getOrThrow(Biomes.SNOWY_PLAINS), random) == Hare.Breed.SCHNEEHASE,
                "kein Schneehase");
        helper.assertTrue(Hare.fromVariant(Rabbit.Variant.EVIL, biomes.getOrThrow(Biomes.PLAINS), random) == Hare.Breed.MORDSHASE,
                "kein Mordshase");
        helper.assertTrue(FarmChicken.henFor(ChickenVariants.COLD, random) == FarmChicken.Breed.BARRED, "keine Sperberhenne im Kalten");
        helper.succeed();
    }

    /** Neu erscheinende Vanilla-Tiere (Spawn-Ei, Befehl) werden zu den eigenen – mit Farbe, Hörnern und Variante. */
    private static void vanillaReplaced(GameTestHelper helper) {
        floor(helper);
        ServerLevel level = helper.getLevel();
        var sheep = EntityTypes.SHEEP.create(level, EntitySpawnReason.SPAWN_ITEM_USE);
        var goat = EntityTypes.GOAT.create(level, EntitySpawnReason.SPAWN_ITEM_USE);
        var rabbit = EntityTypes.RABBIT.create(level, EntitySpawnReason.SPAWN_ITEM_USE);
        var cow = EntityTypes.MOOSHROOM.create(level, EntitySpawnReason.SPAWN_ITEM_USE);
        var chicken = EntityTypes.CHICKEN.create(level, EntitySpawnReason.SPAWN_ITEM_USE);
        helper.assertTrue(sheep != null && goat != null && rabbit != null && cow != null && chicken != null, "Vanilla-Tiere fehlen");
        sheep.setColor(DyeColor.RED);
        sheep.setCustomName(net.minecraft.network.chat.Component.literal("Rosa"));
        goat.dropHorn();
        level.registryAccess().lookupOrThrow(Registries.CHICKEN_VARIANT).get(ChickenVariants.WARM).ifPresent(chicken::setVariant);
        int i = 0;
        for (Mob mob : List.<Mob>of(sheep, goat, rabbit, cow, chicken)) {
            BlockPos pos = helper.absolutePos(new BlockPos(1 + i * 2 % 8, 1, 2 + (i / 4) * 3));
            mob.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0f, 0.0f);
            level.addFreshEntity(mob);
            i++;
        }
        helper.runAfterDelay(2, () -> {
            AABB box = new AABB(helper.absolutePos(BlockPos.ZERO)).inflate(12.0);
            helper.assertTrue(level.getEntitiesOfClass(Mob.class, box, m -> m.getType() == EntityTypes.SHEEP || m.getType() == EntityTypes.GOAT
                    || m.getType() == EntityTypes.RABBIT || m.getType() == EntityTypes.MOOSHROOM || m.getType() == EntityTypes.CHICKEN).isEmpty(),
                    "Vanilla-Tiere sind noch da");
            var sheeps = level.getEntitiesOfClass(FarmSheep.class, box);
            helper.assertTrue(sheeps.size() == 1 && sheeps.getFirst().getColor() == DyeColor.RED, "Schaf nicht übernommen (Farbe)");
            helper.assertTrue("Rosa".equals(sheeps.getFirst().getCustomName().getString()), "Name des Schafs verloren");
            var goats = level.getEntitiesOfClass(FarmGoat.class, box);
            helper.assertTrue(goats.size() == 1 && goats.getFirst().hasLeftHorn() != goats.getFirst().hasRightHorn(),
                    "Ziege nicht übernommen (abgebrochenes Horn)");
            helper.assertTrue(level.getEntitiesOfClass(Hare.class, box).size() == 1, "kein Hase");
            var cattle = level.getEntitiesOfClass(Cattle.class, box);
            helper.assertTrue(cattle.size() == 1 && cattle.getFirst().getBreed() == Cattle.Breed.PILZ, "Pilzkuh nicht zum Pilzrind geworden");
            var hens = level.getEntitiesOfClass(FarmChicken.class, box);
            helper.assertTrue(hens.size() == 1, "kein Huhn");
            var hen = hens.getFirst();
            helper.assertTrue(hen.isRooster() || hen.getBreed() == FarmChicken.Breed.BROWN || hen.getBreed() == FarmChicken.Breed.BLACK,
                    "warmes Huhn ist " + hen.getBreed());
            helper.succeed();
        });
    }

    /** Rinder geben Milch, Pilzrinder dazu Pilzsuppe; Ziegen geben Milch. */
    private static void milkAndStew(GameTestHelper helper) {
        floor(helper);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        Cattle cow = helper.spawnWithNoFreeWill(ModEntities.CATTLE.get(), new BlockPos(2, 1, 2));
        cow.setBreed(Cattle.Breed.FLECKVIEH);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BUCKET));
        cow.mobInteract(player, InteractionHand.MAIN_HAND);
        helper.assertTrue(player.getItemInHand(InteractionHand.MAIN_HAND).is(Items.MILK_BUCKET), "keine Milch vom Rind");
        Cattle mushroom = helper.spawnWithNoFreeWill(ModEntities.CATTLE.get(), new BlockPos(6, 1, 2));
        mushroom.setBreed(Cattle.Breed.PILZ);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BOWL));
        mushroom.mobInteract(player, InteractionHand.MAIN_HAND);
        helper.assertTrue(player.getItemInHand(InteractionHand.MAIN_HAND).is(Items.MUSHROOM_STEW), "keine Pilzsuppe vom Pilzrind");
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BOWL));
        cow.mobInteract(player, InteractionHand.MAIN_HAND);
        helper.assertTrue(player.getItemInHand(InteractionHand.MAIN_HAND).is(Items.BOWL), "Fleckvieh gibt Pilzsuppe");
        FarmGoat goat = helper.spawnWithNoFreeWill(ModEntities.FARM_GOAT.get(), new BlockPos(4, 1, 6));
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BUCKET));
        goat.mobInteract(player, InteractionHand.MAIN_HAND);
        helper.assertTrue(player.getItemInHand(InteractionHand.MAIN_HAND).is(Items.MILK_BUCKET), "keine Milch von der Ziege");
        helper.succeed();
    }

    /** Hennen legen Eier in der Farbe ihrer Rasse (weiß, braun, blau), der Hahn legt keine. */
    private static void chickenEggs(GameTestHelper helper) {
        floor(helper);
        record Hen(FarmChicken.Breed breed, Item egg, BlockPos pos) {}
        List<Hen> hens = List.of(new Hen(FarmChicken.Breed.WHITE, Items.EGG, new BlockPos(1, 1, 1)),
                new Hen(FarmChicken.Breed.BROWN, Items.BROWN_EGG, new BlockPos(7, 1, 1)),
                new Hen(FarmChicken.Breed.BARRED, Items.BLUE_EGG, new BlockPos(1, 1, 7)),
                new Hen(FarmChicken.Breed.ROOSTER, Items.AIR, new BlockPos(7, 1, 7)));
        for (Hen hen : hens) {
            FarmChicken chicken = helper.spawnWithNoFreeWill(ModEntities.FARM_CHICKEN.get(), hen.pos());
            chicken.setBreed(hen.breed());
            chicken.eggTime = 3;
        }
        helper.runAfterDelay(12, () -> {
            for (Hen hen : hens) {
                AABB box = new AABB(helper.absolutePos(hen.pos())).inflate(1.5);
                var items = helper.getLevel().getEntitiesOfClass(ItemEntity.class, box);
                if (hen.egg() == Items.AIR) {
                    helper.assertTrue(items.isEmpty(), "Der Hahn hat ein Ei gelegt");
                } else {
                    helper.assertTrue(items.stream().anyMatch(item -> item.getItem().is(hen.egg())),
                            hen.breed().id() + " legt kein " + hen.egg().getDescriptionId() + ": " + items);
                }
            }
            helper.succeed();
        });
    }

    /** Geschoren fällt Wolle in der Farbe des Schafs; Heidschnucken bekommen natürlich graue Wolle. */
    private static void sheepShearing(GameTestHelper helper) {
        floor(helper);
        FarmSheep sheep = helper.spawnWithNoFreeWill(ModEntities.FARM_SHEEP.get(), new BlockPos(4, 1, 4));
        sheep.setBreed(FarmSheep.Breed.MERINO);
        sheep.setColor(DyeColor.BLUE);
        helper.assertTrue(sheep.readyForShearing(), "Schaf ist nicht schurbereit");
        sheep.shear(helper.getLevel(), SoundSource.PLAYERS, new ItemStack(Items.SHEARS));
        helper.assertTrue(sheep.isSheared(), "Schaf ist nicht geschoren");
        helper.runAfterDelay(2, () -> {
            var items = helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(new BlockPos(4, 1, 4))).inflate(3.0));
            helper.assertTrue(items.stream().anyMatch(item -> item.getItem().is(Items.WOOL.pick(DyeColor.BLUE))), "keine blaue Wolle: " + items);
            helper.succeed();
        });
    }

    /** Pferderassen: das Kaltblut ist größer, das Pony kleiner; Sattel und Rossharnisch passen; Pferd + Esel = Maultier. */
    private static void horses(GameTestHelper helper) {
        floor(helper);
        ServerLevel level = helper.getLevel();
        FarmHorse draft = helper.spawnWithNoFreeWill(ModEntities.FARM_HORSE.get(), new BlockPos(2, 1, 2));
        draft.setBreed(FarmHorse.Breed.KALTBLUT);
        FarmHorse pony = helper.spawnWithNoFreeWill(ModEntities.FARM_HORSE.get(), new BlockPos(6, 1, 2));
        pony.setBreed(FarmHorse.Breed.PONY);
        helper.assertTrue(draft.getAttributeValue(Attributes.SCALE) > 1.1 && pony.getAttributeValue(Attributes.SCALE) < 0.85,
                "Größen stimmen nicht");
        helper.assertTrue(draft.isEquippableInSlot(new ItemStack(Items.IRON_HORSE_ARMOR), EquipmentSlot.BODY), "kein Rossharnisch");
        draft.setTamed(true);
        draft.setItemSlot(EquipmentSlot.SADDLE, new ItemStack(Items.SADDLE));
        helper.assertTrue(draft.isSaddled(), "Sattel sitzt nicht");
        FarmDonkey donkey = helper.spawnWithNoFreeWill(ModEntities.FARM_DONKEY.get(), new BlockPos(2, 1, 6));
        helper.assertTrue(draft.getBreedOffspring(level, donkey) instanceof FarmMule, "Pferd + Esel ist kein Maultier");
        helper.assertTrue(donkey.getBreedOffspring(level, draft) instanceof FarmMule, "Esel + Pferd ist kein Maultier");
        helper.assertTrue(draft.getBreedOffspring(level, pony) instanceof FarmHorse, "Pferd + Pferd ist kein Fohlen");
        donkey.setTamed(true);
        helper.assertTrue(donkey.isEquippableInSlot(new ItemStack(Items.SADDLE), EquipmentSlot.SADDLE), "Esel kann keinen Sattel tragen");
        // die Hitbox folgt der Größe, sobald die Werte im nächsten Tick übernommen sind
        helper.runAfterDelay(2, () -> {
            helper.assertTrue(draft.getBbWidth() > pony.getBbWidth() * 1.3, "Kaltblut nicht größer als das Pony: " + draft.getBbWidth()
                    + " / " + pony.getBbWidth());
            helper.succeed();
        });
    }

    /** Nachwuchs: jedes Tier bekommt Junge seiner eigenen Art; Lämmer erben die gemischte Wollfarbe. */
    private static void offspring(GameTestHelper helper) {
        floor(helper);
        ServerLevel level = helper.getLevel();
        record Pair(EntityType<? extends Mob> type, Class<?> child) {}
        int i = 0;
        for (Pair pair : List.of(new Pair(ModEntities.CATTLE.get(), Cattle.class), new Pair(ModEntities.FARM_PIG.get(), FarmPig.class),
                new Pair(ModEntities.FARM_SHEEP.get(), FarmSheep.class), new Pair(ModEntities.FARM_CHICKEN.get(), FarmChicken.class),
                new Pair(ModEntities.FARM_GOAT.get(), FarmGoat.class), new Pair(ModEntities.HARE.get(), Hare.class),
                new Pair(ModEntities.FARM_DONKEY.get(), FarmDonkey.class))) {
            var a = (net.minecraft.world.entity.AgeableMob) helper.spawnWithNoFreeWill(pair.type(), new BlockPos(1 + (i % 4) * 2, 1, 1 + (i / 4) * 4));
            var b = (net.minecraft.world.entity.AgeableMob) helper.spawnWithNoFreeWill(pair.type(), new BlockPos(1 + (i % 4) * 2, 1, 2 + (i / 4) * 4));
            if (a instanceof Sheep sa && b instanceof Sheep sb) {
                sa.setColor(DyeColor.RED);
                sb.setColor(DyeColor.YELLOW);
            }
            var child = a.getBreedOffspring(level, b);
            helper.assertTrue(pair.child().isInstance(child), pair.type().getDescriptionId() + " bekommt " + child);
            if (child instanceof Sheep lamb) {
                helper.assertTrue(lamb.getColor() == DyeColor.ORANGE, "Lamm hat " + lamb.getColor() + " statt orange");
            }
            i++;
        }
        FarmMule mule = helper.spawnWithNoFreeWill(ModEntities.FARM_MULE.get(), new BlockPos(7, 1, 7));
        helper.assertTrue(mule.getBreedOffspring(level, mule) == null, "Maultier bekommt Junge");
        helper.succeed();
    }

    /** Rasse, Hörner und Farbe überstehen Speichern und Laden. */
    private static void saveLoad(GameTestHelper helper) {
        floor(helper);
        ServerLevel level = helper.getLevel();
        FarmGoat goat = helper.spawnWithNoFreeWill(ModEntities.FARM_GOAT.get(), new BlockPos(2, 1, 2));
        goat.setBreed(FarmGoat.Breed.GAEMSE);
        goat.dropHorn();
        FarmHorse horse = helper.spawnWithNoFreeWill(ModEntities.FARM_HORSE.get(), new BlockPos(6, 1, 2));
        horse.setBreed(FarmHorse.Breed.HAFLINGER);
        Hare hare = helper.spawnWithNoFreeWill(ModEntities.HARE.get(), new BlockPos(2, 1, 6));
        hare.setBreed(Hare.Breed.MORDSHASE);
        helper.assertTrue(hare.getVariant() == Rabbit.Variant.EVIL, "Mordshase ist nicht böse");
        FarmChicken rooster = helper.spawnWithNoFreeWill(ModEntities.FARM_CHICKEN.get(), new BlockPos(6, 1, 6));
        rooster.setBreed(FarmChicken.Breed.ROOSTER);

        FarmGoat goat2 = reload(level, goat, ModEntities.FARM_GOAT.get());
        helper.assertTrue(goat2.getBreed() == FarmGoat.Breed.GAEMSE && goat2.hasLeftHorn() != goat2.hasRightHorn(), "Ziege verändert");
        FarmHorse horse2 = reload(level, horse, ModEntities.FARM_HORSE.get());
        helper.assertTrue(horse2.getBreed() == FarmHorse.Breed.HAFLINGER, "Pferd verändert");
        Hare hare2 = reload(level, hare, ModEntities.HARE.get());
        helper.assertTrue(hare2.getBreed() == Hare.Breed.MORDSHASE && hare2.getVariant() == Rabbit.Variant.EVIL, "Hase verändert");
        FarmChicken rooster2 = reload(level, rooster, ModEntities.FARM_CHICKEN.get());
        helper.assertTrue(rooster2.isRooster(), "Hahn verändert");
        helper.succeed();
    }

    /** Zwei verliebte Ziegen finden zueinander und bekommen ein Zicklein; Ziegen rammen einander nicht. */
    private static void goatCourtship(GameTestHelper helper) {
        floor(helper);
        FarmGoat a = helper.spawn(ModEntities.FARM_GOAT.get(), new BlockPos(2, 1, 4));
        FarmGoat b = helper.spawn(ModEntities.FARM_GOAT.get(), new BlockPos(6, 1, 4));
        // die Ramm-Auswahl der Vanilla-Ziegen schließt Ziegen aus – die eigenen gelten als solche
        helper.assertTrue(b.is(EntityTypes.GOAT), "Ziegen rammen einander");
        a.setInLove(null);
        b.setInLove(null);
        helper.succeedWhen(() -> helper.assertTrue(!helper.getLevel().getEntitiesOfClass(FarmGoat.class,
                        new AABB(helper.absolutePos(new BlockPos(4, 1, 4))).inflate(8.0), FarmGoat::isBaby).isEmpty(),
                "noch kein Zicklein"));
    }

    /** Wölfe jagen auch die eigenen Schafe. */
    private static void wolfHunts(GameTestHelper helper) {
        floor(helper);
        var wolf = helper.spawn(ModEntities.WOLF.get(), new BlockPos(2, 1, 4));
        helper.spawnWithNoFreeWill(ModEntities.FARM_SHEEP.get(), new BlockPos(6, 1, 4));
        helper.succeedWhen(() -> helper.assertTrue(wolf.getTarget() instanceof FarmSheep, "der Wolf jagt das Schaf nicht"));
    }

    private static <T extends Mob> T reload(ServerLevel level, T mob, EntityType<T> type) {
        TagValueOutput out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
        mob.saveWithoutId(out);
        CompoundTag tag = out.buildResult();
        T loaded = type.create(level, EntitySpawnReason.LOAD);
        loaded.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), tag));
        return loaded;
    }

    @SuppressWarnings("unused")
    private static boolean isVanilla(Mob mob) {
        return mob instanceof Chicken && mob.getType() == EntityTypes.CHICKEN || mob instanceof Goat && mob.getType() == EntityTypes.GOAT;
    }
}
