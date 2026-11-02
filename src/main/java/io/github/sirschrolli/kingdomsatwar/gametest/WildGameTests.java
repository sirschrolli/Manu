package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.entity.creature.wild.Bear;
import io.github.sirschrolli.kingdomsatwar.entity.creature.wild.HouseCat;
import io.github.sirschrolli.kingdomsatwar.entity.creature.wild.WildArmadillo;
import io.github.sirschrolli.kingdomsatwar.entity.creature.wild.WildBat;
import io.github.sirschrolli.kingdomsatwar.entity.creature.wild.WildBee;
import io.github.sirschrolli.kingdomsatwar.entity.creature.wild.WildCamel;
import io.github.sirschrolli.kingdomsatwar.entity.creature.wild.WildFox;
import io.github.sirschrolli.kingdomsatwar.entity.creature.wild.WildLlama;
import io.github.sirschrolli.kingdomsatwar.entity.creature.wild.WildPanda;
import io.github.sirschrolli.kingdomsatwar.entity.creature.wild.WildParrot;
import io.github.sirschrolli.kingdomsatwar.entity.creature.wild.WildTraderLlama;
import io.github.sirschrolli.kingdomsatwar.entity.creature.wild.WildWolf;
import io.github.sirschrolli.kingdomsatwar.entity.creature.wild.Wildcat;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.worldgen.overworld.OverworldBiomes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.animal.feline.CatVariants;
import net.minecraft.world.entity.animal.fox.Fox;
import net.minecraft.world.entity.animal.panda.Panda;
import net.minecraft.world.entity.animal.parrot.Parrot;
import net.minecraft.world.entity.animal.equine.Llama;
import net.minecraft.world.entity.animal.wolf.WolfVariants;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/** Tests der eigenen Wildtiere, die die Vanilla-Wildtiere ersetzen (E3b). */
final class WildGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SPAWN =
            ModGameTests.TEST_FUNCTIONS.register("wild_animals_spawn", () -> WildGameTests::animalsSpawn);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SPAWN_LISTS =
            ModGameTests.TEST_FUNCTIONS.register("wild_spawn_lists", () -> WildGameTests::spawnLists);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> BREEDS =
            ModGameTests.TEST_FUNCTIONS.register("wild_breeds_by_biome", () -> WildGameTests::breedsByBiome);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CONVERSION =
            ModGameTests.TEST_FUNCTIONS.register("wild_vanilla_replaced", () -> WildGameTests::vanillaReplaced);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> OFFSPRING =
            ModGameTests.TEST_FUNCTIONS.register("wild_offspring", () -> WildGameTests::offspring);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CAMEL_COURTSHIP =
            ModGameTests.TEST_FUNCTIONS.register("wild_camel_courtship", () -> helper -> courtship(helper, ModEntities.CAMEL.get(),
                    WildCamel.class));
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ARMADILLO_COURTSHIP =
            ModGameTests.TEST_FUNCTIONS.register("wild_armadillo_courtship", () -> helper -> courtship(helper, ModEntities.ARMADILLO.get(),
                    WildArmadillo.class));
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SAVE_LOAD =
            ModGameTests.TEST_FUNCTIONS.register("wild_save_load", () -> WildGameTests::saveLoad);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> EQUIPMENT =
            ModGameTests.TEST_FUNCTIONS.register("wild_equipment", () -> WildGameTests::equipment);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> BERRY_BUSH =
            ModGameTests.TEST_FUNCTIONS.register("wild_berry_bush", () -> WildGameTests::berryBush);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> BEEHIVE =
            ModGameTests.TEST_FUNCTIONS.register("wild_beehive", () -> WildGameTests::beehive);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SHOULDER =
            ModGameTests.TEST_FUNCTIONS.register("wild_parrot_shoulder", () -> WildGameTests::parrotShoulder);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TRADER_LLAMA =
            ModGameTests.TEST_FUNCTIONS.register("wild_trader_llama_leash", () -> WildGameTests::traderLlamaLeash);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> WOLF_PREY =
            ModGameTests.TEST_FUNCTIONS.register("wild_wolf_hunts_fox", () -> WildGameTests::wolfHuntsFox);

    private WildGameTests() {}

    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var test : List.of(SPAWN, SPAWN_LISTS, BREEDS, CONVERSION, OFFSPRING, CAMEL_COURTSHIP, ARMADILLO_COURTSHIP, SAVE_LOAD,
                EQUIPMENT, BERRY_BUSH, BEEHIVE, SHOULDER, TRADER_LLAMA, WOLF_PREY)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 300, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
    }

    private static List<EntityType<? extends Mob>> types() {
        return List.of(ModEntities.WOLF.get(), ModEntities.FOX.get(), ModEntities.BEAR.get(), ModEntities.PANDA.get(), ModEntities.CAT.get(),
                ModEntities.WILDCAT.get(), ModEntities.PARROT.get(), ModEntities.BEE.get(), ModEntities.BAT.get(), ModEntities.ARMADILLO.get(),
                ModEntities.CAMEL.get(), ModEntities.LLAMA.get(), ModEntities.TRADER_LLAMA.get());
    }

    private static Set<EntityType<?>> vanillaTypes() {
        return Set.of(EntityTypes.WOLF, EntityTypes.FOX, EntityTypes.POLAR_BEAR, EntityTypes.PANDA, EntityTypes.CAT, EntityTypes.OCELOT,
                EntityTypes.PARROT, EntityTypes.BEE, EntityTypes.BAT, EntityTypes.ARMADILLO, EntityTypes.CAMEL, EntityTypes.LLAMA,
                EntityTypes.TRADER_LLAMA);
    }

    private static void floor(GameTestHelper helper) {
        for (int x = 0; x < 9; x++) {
            for (int z = 0; z < 9; z++) {
                helper.setBlock(x, 0, z, Blocks.GRASS_BLOCK);
            }
        }
    }

    private static AABB arena(GameTestHelper helper) {
        return new AABB(helper.absolutePos(new BlockPos(4, 1, 4))).inflate(12.0);
    }

    // =====================================================================================

    /** Alle dreizehn Wildtiere erscheinen und leben; Beute wie bei Vanilla; die Vanilla-Tags gelten auch für sie. */
    private static void animalsSpawn(GameTestHelper helper) {
        floor(helper);
        var registries = helper.getLevel().getServer().reloadableRegistries();
        List<Mob> mobs = new ArrayList<>();
        int i = 0;
        for (EntityType<? extends Mob> type : types()) {
            mobs.add(helper.spawnWithNoFreeWill(type, new BlockPos(1 + (i % 4) * 2, 1, 1 + (i / 4) * 2)));
            ResourceKey<LootTable> key = type.getDefaultLootTable().orElseThrow();
            helper.assertTrue(registries.getLootTable(key) != LootTable.EMPTY, "Beute fehlt: " + key.identifier());
            i++;
        }
        for (var type : List.of(ModEntities.WOLF.get(), ModEntities.FOX.get(), ModEntities.BEAR.get(), ModEntities.PANDA.get(),
                ModEntities.CAT.get(), ModEntities.WILDCAT.get(), ModEntities.PARROT.get(), ModEntities.BEE.get(), ModEntities.ARMADILLO.get(),
                ModEntities.CAMEL.get(), ModEntities.LLAMA.get())) {
            helper.assertTrue(type.builtInRegistryHolder().is(EntityTypeTags.FOLLOWABLE_FRIENDLY_MOBS), type.getDescriptionId() + " fehlt im Tag der Freunde");
        }
        helper.assertTrue(ModEntities.BEE.get().builtInRegistryHolder().is(EntityTypeTags.BEEHIVE_INHABITORS), "Biene darf nicht in den Stock");
        helper.assertTrue(ModEntities.BEE.get().builtInRegistryHolder().is(EntityTypeTags.ARTHROPOD), "Biene ist kein Gliederfüßer");
        helper.assertTrue(ModEntities.CAMEL.get().builtInRegistryHolder().is(EntityTypeTags.CAN_EQUIP_SADDLE), "Kamel kann keinen Sattel tragen");
        helper.assertTrue(ModEntities.BAT.get().builtInRegistryHolder().is(EntityTypeTags.FALL_DAMAGE_IMMUNE), "Fledermaus nimmt Fallschaden");
        helper.runAfterDelay(10, () -> {
            for (Mob mob : mobs) {
                helper.assertTrue(mob.isAlive(), mob.getType().getDescriptionId() + " lebt nicht mehr");
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

    private static int weightOf(Biome biome, MobCategory category, EntityType<?> type) {
        MobSpawnSettings spawns = biome.getModifiedEnvironmentAttributes().applyModifier(EnvironmentAttributes.NATURAL_MOB_SPAWNS,
                EnvironmentAttributes.NATURAL_MOB_SPAWNS.defaultValue());
        return spawns.getMobsToSpawn(category).unwrap().stream().filter(w -> w.value().type() == type).mapToInt(w -> w.weight()).sum();
    }

    /** Kein Vanilla-Wildtier steht mehr in einer Spawn-Liste der Oberwelt; die eigenen stehen dort mit Vanilla-Gewicht. */
    private static void spawnLists(GameTestHelper helper) {
        var biomes = helper.getLevel().registryAccess().lookupOrThrow(Registries.BIOME);
        Set<EntityType<?>> vanilla = vanillaTypes();
        biomes.listElements().filter(biome -> biome.is(net.minecraft.tags.BiomeTags.IS_OVERWORLD)).forEach(biome -> {
            for (MobCategory category : MobCategory.values()) {
                for (EntityType<?> type : spawnsOf(biome.value(), category)) {
                    helper.assertTrue(!vanilla.contains(type), "In " + biome.key().identifier() + " erscheint noch " + type.getDescriptionId());
                }
            }
        });
        record Expect(ResourceKey<Biome> biome, MobCategory category, List<EntityType<?>> types) {}
        for (Expect e : List.of(
                new Expect(Biomes.TAIGA, MobCategory.CREATURE, List.of(ModEntities.WOLF.get(), ModEntities.FOX.get(), ModEntities.BEAR.get())),
                new Expect(Biomes.TAIGA, MobCategory.MONSTER, List.of(ModEntities.WILDCAT.get())),
                new Expect(Biomes.JUNGLE, MobCategory.CREATURE, List.of(ModEntities.PARROT.get(), ModEntities.PANDA.get())),
                new Expect(Biomes.JUNGLE, MobCategory.MONSTER, List.of(ModEntities.WILDCAT.get())),
                new Expect(Biomes.SNOWY_PLAINS, MobCategory.CREATURE, List.of(ModEntities.BEAR.get())),
                new Expect(Biomes.SAVANNA, MobCategory.CREATURE, List.of(ModEntities.ARMADILLO.get())),
                new Expect(Biomes.DESERT, MobCategory.CREATURE, List.of(ModEntities.CAMEL.get(), ModEntities.FOX.get())),
                new Expect(Biomes.SAVANNA_PLATEAU, MobCategory.CREATURE, List.of(ModEntities.LLAMA.get(), ModEntities.WOLF.get(),
                        ModEntities.ARMADILLO.get(), ModEntities.CAMEL.get())),
                new Expect(Biomes.WINDSWEPT_HILLS, MobCategory.CREATURE, List.of(ModEntities.LLAMA.get())),
                new Expect(Biomes.DRIPSTONE_CAVES, MobCategory.AMBIENT, List.of(ModEntities.BAT.get())),
                new Expect(Biomes.FOREST, MobCategory.MONSTER, List.of(ModEntities.WILDCAT.get())),
                new Expect(OverworldBiomes.ROCK_GORGES, MobCategory.CREATURE, List.of(ModEntities.LLAMA.get(), ModEntities.ARMADILLO.get(),
                        ModEntities.CAMEL.get())),
                new Expect(OverworldBiomes.PRIMEVAL_FOREST, MobCategory.CREATURE, List.of(ModEntities.WOLF.get(), ModEntities.BEAR.get())))) {
            List<EntityType<?>> found = spawnsOf(biomes.getOrThrow(e.biome()).value(), e.category());
            for (EntityType<?> type : e.types()) {
                helper.assertTrue(found.contains(type), "In " + e.biome().identifier() + " fehlt " + type.getDescriptionId() + " " + found);
            }
        }
        // Vanilla-Gewichte bleiben: Pandas im Bambusdschungel 80, Papageien im Dschungel 40, Fledermäuse 10
        helper.assertTrue(weightOf(biomes.getOrThrow(Biomes.BAMBOO_JUNGLE).value(), MobCategory.CREATURE, ModEntities.PANDA.get()) == 80,
                "Panda-Gewicht im Bambusdschungel");
        helper.assertTrue(weightOf(biomes.getOrThrow(Biomes.JUNGLE).value(), MobCategory.CREATURE, ModEntities.PARROT.get()) == 40,
                "Papagei-Gewicht im Dschungel");
        helper.assertTrue(weightOf(biomes.getOrThrow(Biomes.PLAINS).value(), MobCategory.AMBIENT, ModEntities.BAT.get()) == 10,
                "Fledermaus-Gewicht");
        helper.succeed();
    }

    /** Die Art richtet sich nach dem Land. */
    private static void breedsByBiome(GameTestHelper helper) {
        var biomes = helper.getLevel().registryAccess().lookupOrThrow(Registries.BIOME);
        var random = helper.getLevel().getRandom();
        for (int i = 0; i < 20; i++) {
            var snowWolf = WildWolf.breedFor(biomes.getOrThrow(Biomes.SNOWY_TAIGA), random);
            helper.assertTrue(snowWolf == WildWolf.Breed.POLAR || snowWolf == WildWolf.Breed.GRAU, "Wolf im Schnee: " + snowWolf);
            helper.assertTrue(WildWolf.breedFor(biomes.getOrThrow(Biomes.SAVANNA_PLATEAU), random) == WildWolf.Breed.STEPPE, "kein Steppenwolf");
            helper.assertTrue(WildWolf.breedFor(biomes.getOrThrow(Biomes.SPARSE_JUNGLE), random) == WildWolf.Breed.ROT, "kein Rotwolf");
            helper.assertTrue(WildFox.breedFor(biomes.getOrThrow(Biomes.DESERT), random) == WildFox.Breed.WUESTE, "kein Wüstenfuchs");
            var snowFox = WildFox.breedFor(biomes.getOrThrow(Biomes.SNOWY_PLAINS), random);
            helper.assertTrue(snowFox == WildFox.Breed.POLAR || snowFox == WildFox.Breed.ROT, "Fuchs im Schnee: " + snowFox);
            helper.assertTrue(Bear.breedFor(biomes.getOrThrow(Biomes.ICE_SPIKES), random) == Bear.Breed.EIS, "kein Eisbär");
            helper.assertTrue(Bear.breedFor(biomes.getOrThrow(Biomes.DEEP_FROZEN_OCEAN), random) == Bear.Breed.EIS, "kein Eisbär auf dem Meer");
            helper.assertTrue(Bear.breedFor(biomes.getOrThrow(Biomes.OLD_GROWTH_SPRUCE_TAIGA), random) != Bear.Breed.EIS, "Eisbär im Wald");
            helper.assertTrue(Wildcat.breedFor(biomes.getOrThrow(Biomes.JUNGLE)) == Wildcat.Breed.OZELOT, "kein Ozelot");
            helper.assertTrue(Wildcat.breedFor(biomes.getOrThrow(Biomes.SNOWY_TAIGA)) == Wildcat.Breed.LUCHS, "kein Luchs");
            helper.assertTrue(Wildcat.breedFor(biomes.getOrThrow(Biomes.DARK_FOREST)) == Wildcat.Breed.WALDKATZE, "keine Waldkatze");
            helper.assertTrue(WildCamel.breedFor(biomes.getOrThrow(Biomes.DESERT)) == WildCamel.Breed.DROMEDAR, "kein Dromedar");
            helper.assertTrue(WildCamel.breedFor(biomes.getOrThrow(Biomes.BADLANDS)) == WildCamel.Breed.TRAMPELTIER, "kein Trampeltier");
            var bee = WildBee.breedFor(biomes.getOrThrow(Biomes.MEADOW), random);
            helper.assertTrue(bee == WildBee.Breed.HUMMEL || bee == WildBee.Breed.HONIG, "Biene: " + bee);
        }
        var cats = helper.getLevel().registryAccess().lookupOrThrow(Registries.CAT_VARIANT);
        helper.assertTrue(HouseCat.fromVanilla(cats.getOrThrow(CatVariants.ALL_BLACK)) == HouseCat.Breed.HEXE, "keine Hexenkatze");
        helper.assertTrue(HouseCat.fromVanilla(cats.getOrThrow(CatVariants.SIAMESE)) == HouseCat.Breed.SIAM, "keine Siamkatze");
        var wolves = helper.getLevel().registryAccess().lookupOrThrow(Registries.WOLF_VARIANT);
        helper.assertTrue(WildWolf.fromVanilla(wolves.getOrThrow(WolfVariants.BLACK)) == WildWolf.Breed.SCHWARZ, "kein Schwarzwolf");
        helper.assertTrue(WildParrot.breedOf(Parrot.Variant.GRAY) == WildParrot.Breed.GRAU, "kein Graupapagei");
        helper.assertTrue(WildLlama.breedOf(Llama.Variant.BROWN) == WildLlama.Breed.BRAUN, "kein braunes Lama");
        helper.succeed();
    }

    /** Neu erscheinende Vanilla-Wildtiere werden zu den eigenen – mit Zähmung, Halsband, Farbe und Genen. */
    private static void vanillaReplaced(GameTestHelper helper) {
        floor(helper);
        ServerLevel level = helper.getLevel();
        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        owner.snapTo(helper.absoluteVec(new net.minecraft.world.phys.Vec3(4.5, 1, 4.5)), 0.0f, 0.0f);   // sonst springt der zahme Wolf zum Besitzer
        List<Mob> originals = new ArrayList<>();
        for (EntityType<?> type : vanillaTypes()) {
            if (type == EntityTypes.TRADER_LLAMA) {
                continue;                                         // eigener Test (Leine)
            }
            Entity entity = type.create(level, EntitySpawnReason.SPAWN_ITEM_USE);
            helper.assertTrue(entity instanceof Mob, "kein " + type.getDescriptionId());
            originals.add((Mob) entity);
        }
        for (Mob mob : originals) {
            switch (mob) {
                case net.minecraft.world.entity.animal.wolf.Wolf wolf -> {
                    wolf.setComponent(DataComponents.WOLF_VARIANT,
                            level.registryAccess().lookupOrThrow(Registries.WOLF_VARIANT).getOrThrow(WolfVariants.BLACK));
                    wolf.tame(owner);
                    wolf.setComponent(DataComponents.WOLF_COLLAR, DyeColor.LIME);
                }
                case Fox fox -> fox.setComponent(DataComponents.FOX_VARIANT, Fox.Variant.SNOW);
                case Panda panda -> {
                    panda.setMainGene(Panda.Gene.PLAYFUL);
                    panda.setHiddenGene(Panda.Gene.PLAYFUL);
                }
                case net.minecraft.world.entity.animal.feline.Cat cat -> cat.setComponent(DataComponents.CAT_VARIANT,
                        level.registryAccess().lookupOrThrow(Registries.CAT_VARIANT).getOrThrow(CatVariants.CALICO));
                case Parrot parrot -> parrot.setComponent(DataComponents.PARROT_VARIANT, Parrot.Variant.BLUE);
                case Llama llama -> llama.setComponent(DataComponents.LLAMA_VARIANT, Llama.Variant.BROWN);
                default -> { }
            }
        }
        int i = 0;
        for (Mob mob : originals) {
            BlockPos pos = helper.absolutePos(new BlockPos(1 + (i % 4) * 2, 1, 1 + (i / 4) * 2));
            mob.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0f, 0.0f);
            level.addFreshEntity(mob);
            i++;
        }
        // jeden Tick neu prüfen, bis alles ersetzt ist (bei voller Testlast kann es einen Tick länger dauern)
        helper.succeedWhen(() -> {
            AABB box = arena(helper);
            Set<EntityType<?>> vanilla = vanillaTypes();
            helper.assertTrue(level.getEntitiesOfClass(Mob.class, box, m -> vanilla.contains(m.getType())).isEmpty(), "Vanilla-Wildtiere sind noch da");
            var wolves = level.getEntitiesOfClass(WildWolf.class, box);
            helper.assertTrue(wolves.size() == 1, "kein Wolf");
            WildWolf wolf = wolves.getFirst();
            helper.assertTrue(wolf.getBreed() == WildWolf.Breed.SCHWARZ, "Wolf ist " + wolf.getBreed());
            helper.assertTrue(wolf.isTame() && wolf.getOwnerReference() != null && wolf.getOwnerReference().getUUID().equals(owner.getUUID()),
                    "Zähmung verloren");
            helper.assertTrue(wolf.getCollarColor() == DyeColor.LIME, "Halsband verloren: " + wolf.getCollarColor());
            var foxes = level.getEntitiesOfClass(WildFox.class, box);
            helper.assertTrue(foxes.size() == 1 && foxes.getFirst().getBreed() == WildFox.Breed.POLAR, "Polarfuchs nicht übernommen");
            var bears = level.getEntitiesOfClass(Bear.class, box);
            helper.assertTrue(bears.size() == 1 && bears.getFirst().getBreed() == Bear.Breed.EIS, "Eisbär nicht übernommen");
            var pandas = level.getEntitiesOfClass(WildPanda.class, box);
            helper.assertTrue(pandas.size() == 1 && pandas.getFirst().getVariant() == Panda.Gene.PLAYFUL, "Gene des Pandas verloren");
            var cats = level.getEntitiesOfClass(HouseCat.class, box);
            helper.assertTrue(cats.size() == 1 && cats.getFirst().getBreed() == HouseCat.Breed.GLUECK, "Glückskatze nicht übernommen");
            helper.assertTrue(level.getEntitiesOfClass(Wildcat.class, box, cat -> cat.getBreed() == Wildcat.Breed.OZELOT).size() == 1, "kein Ozelot");
            var parrots = level.getEntitiesOfClass(WildParrot.class, box);
            helper.assertTrue(parrots.size() == 1 && parrots.getFirst().getBreed() == WildParrot.Breed.HYAZINTH, "Hyazinthara nicht übernommen");
            var llamas = level.getEntitiesOfClass(WildLlama.class, box);
            helper.assertTrue(llamas.size() == 1 && llamas.getFirst().getVariant() == Llama.Variant.BROWN, "Farbe des Lamas verloren");
            for (Class<? extends Mob> type : List.of(WildBee.class, WildBat.class, WildArmadillo.class, WildCamel.class)) {
                // Fledermaus und Biene fliegen gleich los: etwas weiter suchen
                helper.assertTrue(level.getEntitiesOfClass(type, box.inflate(8.0)).size() == 1, "kein " + type.getSimpleName());
            }
        });
    }

    /** Nachwuchs: jedes Tier bekommt Junge seiner eigenen Art (Wolf und Katze zahm wie die Eltern). */
    private static void offspring(GameTestHelper helper) {
        floor(helper);
        ServerLevel level = helper.getLevel();
        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        record Pair(EntityType<? extends Mob> type, Class<?> child) {}
        int i = 0;
        for (Pair pair : List.of(new Pair(ModEntities.WOLF.get(), WildWolf.class), new Pair(ModEntities.FOX.get(), WildFox.class),
                new Pair(ModEntities.PANDA.get(), WildPanda.class), new Pair(ModEntities.CAT.get(), HouseCat.class),
                new Pair(ModEntities.WILDCAT.get(), Wildcat.class), new Pair(ModEntities.BEE.get(), WildBee.class),
                new Pair(ModEntities.ARMADILLO.get(), WildArmadillo.class), new Pair(ModEntities.CAMEL.get(), WildCamel.class),
                new Pair(ModEntities.LLAMA.get(), WildLlama.class), new Pair(ModEntities.TRADER_LLAMA.get(), WildTraderLlama.class),
                new Pair(ModEntities.BEAR.get(), Bear.class))) {
            var a = (AgeableMob) helper.spawnWithNoFreeWill(pair.type(), new BlockPos(1 + (i % 4) * 2, 1, 1 + (i / 4) * 3));
            var b = (AgeableMob) helper.spawnWithNoFreeWill(pair.type(), new BlockPos(1 + (i % 4) * 2, 1, 2 + (i / 4) * 3));
            for (AgeableMob parent : List.of(a, b)) {
                if (parent instanceof Llama) {                    // Stärke setzt Vanilla erst beim Erscheinen
                    parent.finalizeSpawn(level, level.getCurrentDifficultyAt(parent.blockPosition()), EntitySpawnReason.SPAWN_ITEM_USE, null);
                }
            }
            if (a instanceof net.minecraft.world.entity.TamableAnimal ta && b instanceof net.minecraft.world.entity.TamableAnimal tb) {
                ta.tame(owner);
                tb.tame(owner);
            }
            var child = a.getBreedOffspring(level, b);
            helper.assertTrue(pair.child().isInstance(child), pair.type().getDescriptionId() + " bekommt " + child);
            if (child instanceof net.minecraft.world.entity.TamableAnimal tame) {
                helper.assertTrue(tame.isTame() && tame.isOwnedBy(owner), pair.type().getDescriptionId() + ": Junges nicht zahm");
            }
            i++;
        }
        helper.succeed();
    }

    /** Kamel und Gürteltier: Das Vanilla-Gehirn sucht nur Vanilla-Partner – die eigene Werbung bringt trotzdem Junge. */
    private static <T extends AgeableMob> void courtship(GameTestHelper helper, EntityType<T> type, Class<T> kind) {
        floor(helper);
        T a = helper.spawn(type, new BlockPos(2, 1, 4));
        T b = helper.spawn(type, new BlockPos(6, 1, 4));
        ((net.minecraft.world.entity.animal.Animal) a).setInLove(null);
        ((net.minecraft.world.entity.animal.Animal) b).setInLove(null);
        helper.succeedWhen(() -> helper.assertTrue(!helper.getLevel().getEntitiesOfClass(kind, arena(helper), AgeableMob::isBaby).isEmpty(),
                "noch kein Junges"));
    }

    /** Art und Merkmale überstehen Speichern und Laden. */
    private static void saveLoad(GameTestHelper helper) {
        floor(helper);
        ServerLevel level = helper.getLevel();
        WildWolf wolf = helper.spawnWithNoFreeWill(ModEntities.WOLF.get(), new BlockPos(1, 1, 1));
        wolf.setBreed(WildWolf.Breed.TIMBER);
        WildFox fox = helper.spawnWithNoFreeWill(ModEntities.FOX.get(), new BlockPos(3, 1, 1));
        fox.setBreed(WildFox.Breed.SILBER);
        HouseCat cat = helper.spawnWithNoFreeWill(ModEntities.CAT.get(), new BlockPos(5, 1, 1));
        cat.setBreed(HouseCat.Breed.KARTAEUSER);
        WildCamel camel = helper.spawnWithNoFreeWill(ModEntities.CAMEL.get(), new BlockPos(3, 1, 5));
        camel.setBreed(WildCamel.Breed.TRAMPELTIER);
        WildBat bat = helper.spawnWithNoFreeWill(ModEntities.BAT.get(), new BlockPos(7, 1, 1));
        bat.setBreed(WildBat.Breed.VAMPIR);
        Wildcat lynx = helper.spawnWithNoFreeWill(ModEntities.WILDCAT.get(), new BlockPos(7, 1, 5));
        lynx.setBreed(Wildcat.Breed.LUCHS);
        helper.assertTrue(reload(level, wolf, ModEntities.WOLF.get()).getBreed() == WildWolf.Breed.TIMBER, "Wolf verändert");
        helper.assertTrue(reload(level, fox, ModEntities.FOX.get()).getBreed() == WildFox.Breed.SILBER, "Fuchs verändert");
        helper.assertTrue(reload(level, cat, ModEntities.CAT.get()).getBreed() == HouseCat.Breed.KARTAEUSER, "Katze verändert");
        helper.assertTrue(reload(level, camel, ModEntities.CAMEL.get()).getBreed() == WildCamel.Breed.TRAMPELTIER, "Kamel verändert");
        helper.assertTrue(reload(level, bat, ModEntities.BAT.get()).getBreed() == WildBat.Breed.VAMPIR, "Fledermaus verändert");
        helper.assertTrue(reload(level, lynx, ModEntities.WILDCAT.get()).getBreed() == Wildcat.Breed.LUCHS, "Luchs verändert");
        helper.succeed();
    }

    /** Wolfspanzer passt dem eigenen Wolf, Teppiche den eigenen Lamas, der Sattel dem eigenen Kamel. */
    private static void equipment(GameTestHelper helper) {
        Equippable armor = Items.WOLF_ARMOR.components().get(DataComponents.EQUIPPABLE);
        helper.assertTrue(armor != null && armor.canBeEquippedBy(BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(ModEntities.WOLF.get())),
                "Wolfspanzer passt dem Wolf nicht");
        Equippable carpet = Items.CARPET.pick(DyeColor.RED).components().get(DataComponents.EQUIPPABLE);
        helper.assertTrue(carpet != null && carpet.canBeEquippedBy(BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(ModEntities.LLAMA.get()))
                        && carpet.canBeEquippedBy(BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(ModEntities.TRADER_LLAMA.get())),
                "Teppich passt dem Lama nicht");
        helper.assertTrue(carpet.canBeEquippedBy(BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(EntityTypes.LLAMA)), "Vanilla-Lama verliert den Teppich");
        Equippable saddle = Items.SADDLE.components().get(DataComponents.EQUIPPABLE);
        helper.assertTrue(saddle != null && saddle.canBeEquippedBy(BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(ModEntities.CAMEL.get())),
                "Sattel passt dem Kamel nicht");
        floor(helper);
        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        WildWolf wolf = helper.spawnWithNoFreeWill(ModEntities.WOLF.get(), new BlockPos(4, 1, 4));
        wolf.tame(owner);
        owner.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new net.minecraft.world.item.ItemStack(Items.WOLF_ARMOR));
        wolf.mobInteract(owner, net.minecraft.world.InteractionHand.MAIN_HAND);
        helper.assertTrue(wolf.getBodyArmorItem().is(Items.WOLF_ARMOR), "Wolf trägt den Panzer nicht");
        helper.succeed();
    }

    /** Fuchs und Biene stechen sich nicht an Beerensträuchern, der Wolf schon. */
    private static void berryBush(GameTestHelper helper) {
        floor(helper);
        ServerLevel level = helper.getLevel();
        WildFox fox = helper.spawnWithNoFreeWill(ModEntities.FOX.get(), new BlockPos(2, 1, 2));
        WildBee bee = helper.spawnWithNoFreeWill(ModEntities.BEE.get(), new BlockPos(4, 1, 2));
        WildWolf wolf = helper.spawnWithNoFreeWill(ModEntities.WOLF.get(), new BlockPos(6, 1, 2));
        var bush = level.damageSources().sweetBerryBush();
        helper.assertTrue(!fox.hurtServer(level, bush, 1.0f) && fox.getHealth() == fox.getMaxHealth(), "Fuchs sticht sich");
        helper.assertTrue(!bee.hurtServer(level, bush, 1.0f) && bee.getHealth() == bee.getMaxHealth(), "Biene sticht sich");
        helper.assertTrue(wolf.hurtServer(level, bush, 1.0f), "Wolf ist gegen Dornen gefeit");
        helper.succeed();
    }

    /** Die eigene Biene fliegt in den Bienenstock und kommt als eigene Biene mit ihrer Art wieder heraus. */
    private static void beehive(GameTestHelper helper) {
        floor(helper);
        ServerLevel level = helper.getLevel();
        BlockPos hivePos = new BlockPos(4, 1, 4);
        helper.setBlock(hivePos, Blocks.BEEHIVE);
        WildBee bee = helper.spawnWithNoFreeWill(ModEntities.BEE.get(), new BlockPos(4, 2, 2));
        bee.setBreed(WildBee.Breed.HUMMEL);
        BeehiveBlockEntity hive = (BeehiveBlockEntity) level.getBlockEntity(helper.absolutePos(hivePos));
        helper.assertTrue(hive != null, "kein Bienenstock");
        hive.addOccupant(bee);
        helper.assertTrue(hive.getOccupantCount() == 1 && bee.isRemoved(), "Biene nicht im Stock");
        hive.emptyAllLivingFromHive(null, level.getBlockState(helper.absolutePos(hivePos)), BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY);
        // gleich im selben Tick prüfen (und nur dicht am Stock): Nachbartests dürfen in der Zwischenzeit nichts verändern
        helper.assertTrue(hive.getOccupantCount() == 0, "Biene bleibt im Stock");
        var bees = level.getEntitiesOfClass(WildBee.class, new AABB(helper.absolutePos(hivePos)).inflate(2.0));
        helper.assertTrue(bees.size() == 1 && bees.getFirst().getBreed() == WildBee.Breed.HUMMEL, "Hummel kommt nicht heraus: " + bees);
        helper.succeed();
    }

    /**
     * Auf der Schulter sitzt der Papagei als Vanilla-Papagei mit seiner Farbe (nur so zeigt ihn der Spieler); steigt er
     * ab, wird er wieder zum eigenen Papagei.
     */
    private static void parrotShoulder(GameTestHelper helper) {
        floor(helper);
        ServerLevel level = helper.getLevel();
        WildParrot parrot = helper.spawnWithNoFreeWill(ModEntities.PARROT.get(), new BlockPos(4, 1, 4));
        parrot.setComponent(DataComponents.PARROT_VARIANT, Parrot.Variant.YELLOW_BLUE);
        CompoundTag tag = parrot.shoulderTag();
        helper.assertTrue("minecraft:parrot".equals(tag.getStringOr("id", "")), "Schulter-Papagei ist kein Vanilla-Papagei: " + tag);
        helper.assertTrue(tag.read("Variant", Parrot.Variant.LEGACY_CODEC).orElse(null) == Parrot.Variant.YELLOW_BLUE, "Farbe verloren");
        Entity respawned = EntityType.create(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), tag), level,
                new net.minecraft.world.entity.EntitySpawnRequest(EntitySpawnReason.LOAD, false)).orElseThrow();
        BlockPos pos = helper.absolutePos(new BlockPos(2, 1, 2));
        respawned.setUUID(java.util.UUID.randomUUID());
        respawned.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        parrot.discard();
        level.addWithUUID(respawned);
        helper.runAfterDelay(2, () -> {
            var parrots = level.getEntitiesOfClass(WildParrot.class, arena(helper));
            helper.assertTrue(parrots.size() == 1 && parrots.getFirst().getBreed() == WildParrot.Breed.GELBBRUST,
                    "kein Gelbbrustara nach dem Absteigen: " + parrots);
            helper.assertTrue(level.getEntitiesOfClass(Parrot.class, arena(helper), p -> p.getType() == EntityTypes.PARROT).isEmpty(),
                    "Vanilla-Papagei bleibt");
            helper.succeed();
        });
    }

    /** Das Lama des fahrenden Händlers wird zum eigenen Lama, der Händler zum eigenen Händler – und das Lama bleibt an seiner Leine. */
    private static void traderLlamaLeash(GameTestHelper helper) {
        floor(helper);
        ServerLevel level = helper.getLevel();
        WanderingTrader trader = helper.spawnWithNoFreeWill(EntityTypes.WANDERING_TRADER, new BlockPos(2, 1, 4));
        trader.setDespawnDelay(48000);                            // wie beim fahrenden Händler (sonst geht das Lama gleich)
        var llama = EntityTypes.TRADER_LLAMA.spawn(level, helper.absolutePos(new BlockPos(5, 1, 4)), EntitySpawnReason.EVENT);
        helper.assertTrue(llama != null, "kein Händlerlama");
        llama.setLeashedTo(trader, true);
        java.util.UUID traderId = trader.getUUID();
        helper.succeedWhen(() -> {
            var llamas = level.getEntitiesOfClass(WildTraderLlama.class, arena(helper));
            helper.assertTrue(llamas.size() == 1, "kein eigenes Händlerlama");
            // auch der Händler selbst ist jetzt der eigene (mit derselben UUID) – das Lama hängt an seiner Leine
            helper.assertTrue(llamas.getFirst().getLeashHolder() instanceof io.github.sirschrolli.kingdomsatwar.entity.villager.TravelingMerchant merchant
                    && merchant.getUUID().equals(traderId), "Händlerlama nicht mehr an der Leine des Händlers: " + llamas.getFirst().getLeashHolder());
            helper.assertTrue(level.getEntitiesOfClass(Llama.class, arena(helper), l -> l.getType() == EntityTypes.TRADER_LLAMA).isEmpty(),
                    "Vanilla-Händlerlama bleibt");
            helper.assertTrue(level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, arena(helper),
                    item -> item.getItem().is(Items.LEAD)).isEmpty(), "Leine fallen gelassen");
        });
    }

    /** Wölfe jagen auch die eigenen Füchse. */
    private static void wolfHuntsFox(GameTestHelper helper) {
        floor(helper);
        var wolf = helper.spawn(ModEntities.WOLF.get(), new BlockPos(2, 1, 4));
        helper.spawnWithNoFreeWill(ModEntities.FOX.get(), new BlockPos(6, 1, 4));
        helper.succeedWhen(() -> helper.assertTrue(wolf.getTarget() instanceof WildFox, "der Wolf jagt den Fuchs nicht"));
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
    private static Set<String> names(List<? extends Entity> list) {
        Set<String> out = new HashSet<>();
        list.forEach(e -> out.add(e.getType().getDescriptionId()));
        return out;
    }
}
