package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.entity.merfolk.MermaidEntity;
import io.github.sirschrolli.kingdomsatwar.entity.merfolk.MermaidProfession;
import io.github.sirschrolli.kingdomsatwar.entity.merfolk.TritonEntity;
import io.github.sirschrolli.kingdomsatwar.entity.sea.SharkEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModTags;
import io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.SeaWoods;
import io.github.sirschrolli.kingdomsatwar.urmeer.UrmeerBiomes;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures;
import io.github.sirschrolli.kingdomsatwar.worldgen.merfolk.SeaVillagePiece;
import java.util.EnumSet;
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
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Tests des Meervolks (0.93): die Tauschangebote der Meerjungfrauen (in Perlen), Schwimmen und Atmen unter Wasser, die
 * Tritonwachen (vertreiben Haie, eilen herbei, wenn eine Meerjungfrau angegriffen wird) und das Meeresdorf als Bauwerk
 * des Urmeers (Platz mit Dreizacksäule, Häuser, Turm, Truhen, Bewohner).
 */
final class MerfolkGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");
    /** Ein Becken um die Mitte des Testplatzes. */
    private static final int REACH = 8;
    private static final int DEPTH = 12;

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> MERFOLK_TRADES =
            ModGameTests.TEST_FUNCTIONS.register("merfolk_trades", () -> MerfolkGameTests::trades);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> MERFOLK_SWIM =
            ModGameTests.TEST_FUNCTIONS.register("merfolk_swim", () -> MerfolkGameTests::swim);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TRITON_DEFENDS =
            ModGameTests.TEST_FUNCTIONS.register("triton_defends", () -> MerfolkGameTests::defends);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SEA_VILLAGE =
            ModGameTests.TEST_FUNCTIONS.register("sea_village", () -> MerfolkGameTests::seaVillage);

    private MerfolkGameTests() {}

    /** Lädt die Klasse (meldet die Testfunktionen an). */
    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var test : List.of(MERFOLK_TRADES, MERFOLK_SWIM, TRITON_DEFENDS)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 400, 0, true, Rotation.NONE, false, 1, 1, true, 12)));
        }
        // das Dorf reicht weit über den Testplatz hinaus
        event.registerTest(SEA_VILLAGE.getId(), new FunctionGameTestInstance(SEA_VILLAGE.key(),
                new TestData<>(environment, Level.OVERWORLD, ARENA, 200, 0, true, Rotation.NONE, false, 1, 1, true, 40)));
    }

    private static void pool(GameTestHelper helper) {
        for (int x = 4 - REACH; x <= 4 + REACH; x++) {
            for (int z = 4 - REACH; z <= 4 + REACH; z++) {
                helper.setBlock(x, 0, z, Blocks.SAND);
                for (int y = 1; y <= DEPTH; y++) {
                    helper.setBlock(x, y, z, Blocks.WATER);
                }
            }
        }
    }

    private static MermaidEntity mermaid(GameTestHelper helper, BlockPos at, MermaidProfession profession) {
        ServerLevel level = helper.getLevel();
        MermaidEntity mermaid = ModEntities.MERMAID.get().create(level, EntitySpawnReason.MOB_SUMMONED);
        BlockPos abs = helper.absolutePos(at);
        mermaid.snapTo(abs.getX() + 0.5, abs.getY(), abs.getZ() + 0.5, 0.0f, 0.0f);
        mermaid.finalizeSpawn(level, level.getCurrentDifficultyAt(abs), EntitySpawnReason.MOB_SUMMONED, null);
        if (profession != null) {
            mermaid.setProfession(profession);
        }
        level.addFreshEntity(mermaid);
        return mermaid;
    }

    private static TritonEntity triton(GameTestHelper helper, BlockPos at) {
        ServerLevel level = helper.getLevel();
        TritonEntity triton = ModEntities.TRITON.get().create(level, EntitySpawnReason.MOB_SUMMONED);
        BlockPos abs = helper.absolutePos(at);
        triton.snapTo(abs.getX() + 0.5, abs.getY(), abs.getZ() + 0.5, 0.0f, 0.0f);
        triton.finalizeSpawn(level, level.getCurrentDifficultyAt(abs), EntitySpawnReason.MOB_SUMMONED, null);
        triton.settle(helper.absolutePos(new BlockPos(4, 2, 4)), 24);
        level.addFreshEntity(triton);
        return triton;
    }

    /**
     * Die Tauschangebote: jeder Beruf hat welche, jedes Angebot kostet Perlen oder bringt welche ein; die Perlentaucherin
     * hat ein Herz des Meeres, die Korallengärtnerin die vier Setzlinge, die Muschelbauerin Meereslaternen, die
     * Gezeitensängerin einen Aquisitor und Bücher. Dazu Perle, Spawn-Eier und Beute.
     */
    private static void trades(GameTestHelper helper) {
        pool(helper);
        Set<MermaidProfession> traders = EnumSet.noneOf(MermaidProfession.class);
        for (MermaidProfession profession : MermaidProfession.TRADERS) {
            traders.add(profession);
            MermaidEntity mermaid = mermaid(helper, new BlockPos(4, 2, 4), profession);
            var offers = mermaid.getOffers();
            helper.assertFalse(offers.isEmpty(), "Keine Angebote: " + profession.id());
            for (MerchantOffer offer : offers) {
                helper.assertTrue(offer.getBaseCostA().is(SeaBlocks.PEARL.get()) || offer.getResult().is(SeaBlocks.PEARL.get()),
                        profession.id() + " tauscht nicht gegen Perlen: " + offer.getResult());
            }
            switch (profession) {
                case PEARL_DIVER -> helper.assertTrue(offers.stream().anyMatch(o -> o.getResult().is(Items.HEART_OF_THE_SEA)),
                        "Die Perlentaucherin hat kein Herz des Meeres");
                case CORAL_GARDENER -> {
                    for (SeaWoods.SeaWood wood : SeaWoods.ALL) {
                        helper.assertTrue(offers.stream().anyMatch(o -> o.getResult().is(wood.sapling.get().asItem())),
                                "Die Korallengärtnerin hat keinen Setzling: " + wood.name);
                    }
                }
                case SHELL_BUILDER -> helper.assertTrue(offers.stream().anyMatch(o -> o.getResult().is(Items.SEA_LANTERN)),
                        "Die Muschelbauerin hat keine Meereslaternen");
                case TIDE_SINGER -> {
                    helper.assertTrue(offers.stream().anyMatch(o -> o.getResult().is(Items.CONDUIT)), "Die Gezeitensängerin hat keinen Aquisitor");
                    helper.assertTrue(offers.stream().anyMatch(o -> o.getResult().is(Items.ENCHANTED_BOOK)), "Die Gezeitensängerin hat keine Bücher");
                }
                default -> {
                }
            }
            mermaid.discard();
        }
        helper.assertTrue(traders.size() == 4, "Nicht vier Berufe zum Tauschen");
        helper.assertFalse(MermaidProfession.GUARD.wears("top") || !MermaidProfession.GUARD.wears("trident"), "Die Wache trägt nicht ihr Zeug");
        helper.assertTrue(MermaidProfession.PEARL_DIVER.wears("diver_band") && MermaidProfession.PEARL_DIVER.wears("hair_back")
                && !MermaidProfession.PEARL_DIVER.wears("helmet"), "Die Perlentaucherin trägt das falsche Zubehör");
        helper.assertFalse(new ItemStack(SeaBlocks.PEARL.get()).isEmpty(), "Keine Perle");
        helper.assertFalse(new ItemStack(SeaBlocks.MERMAID_SPAWN_EGG.get()).isEmpty() || new ItemStack(SeaBlocks.TRITON_SPAWN_EGG.get()).isEmpty(),
                "Kein Spawn-Ei");
        var loot = helper.getLevel().getServer().reloadableRegistries();
        helper.assertTrue(loot.getLootTable(ModEntities.TRITON.get().getDefaultLootTable().orElseThrow()) != LootTable.EMPTY,
                "Die Wache hat keine Beute");
        helper.succeed();
    }

    /** Unter Wasser: Meerjungfrau und Wache bleiben gesund und atmen; die Gestalt richtet sich nach dem Ort, der Beruf ist gewählt. */
    private static void swim(GameTestHelper helper) {
        pool(helper);
        MermaidEntity mermaid = mermaid(helper, new BlockPos(4, 2, 4), null);
        TritonEntity triton = triton(helper, new BlockPos(6, 2, 4));
        helper.assertTrue(mermaid.profession() != MermaidProfession.GUARD, "Die Meerjungfrau hat keinen Beruf");
        helper.assertTrue(mermaid.look() != null && triton.look() != null, "Keine Gestalt");
        helper.assertTrue(mermaid.canBreatheUnderwater() && triton.canBreatheUnderwater(), "Das Meervolk atmet nicht unter Wasser");
        helper.runAfterDelay(120, () -> {
            for (var e : List.of(mermaid, triton)) {
                helper.assertTrue(e.isAlive(), "Gestorben: " + e.getType());
                helper.assertTrue(e.isInWater(), "Nicht mehr im Wasser: " + e.getType());
                helper.assertTrue(e.getHealth() >= e.getMaxHealth(), "Verletzt: " + e.getType() + " " + e.getHealth());
                helper.assertTrue(e.getAirSupply() >= e.getMaxAirSupply(), "Ohne Luft: " + e.getType());
            }
            helper.succeed();
        });
    }

    /**
     * Die Wachen: Wer eine Meerjungfrau angreift, wird sofort zum Ziel der Wache; ein Hai im Dorf wird angegriffen und
     * vertrieben.
     */
    private static void defends(GameTestHelper helper) {
        pool(helper);
        ServerLevel level = helper.getLevel();
        MermaidEntity mermaid = mermaid(helper, new BlockPos(2, 2, 4), MermaidProfession.CORAL_GARDENER);
        TritonEntity triton = triton(helper, new BlockPos(4, 2, 4));
        Zombie zombie = helper.spawn(EntityTypes.ZOMBIE, new BlockPos(2, 2, 7));
        mermaid.hurtServer(level, level.damageSources().mobAttack(zombie), 1.0f);
        helper.assertTrue(triton.getTarget() == zombie, "Die Wache eilt der Meerjungfrau nicht zu Hilfe");
        zombie.discard();
        triton.setTarget(null);
        helper.assertTrue(ModEntities.SHARK.get().builtInRegistryHolder().is(ModTags.EntityTypes.MERFOLK_THREATS), "Haie bedrohen das Meervolk nicht");
        SharkEntity shark = helper.spawn(ModEntities.SHARK.get(), new BlockPos(9, 3, 4));
        helper.succeedWhen(() -> helper.assertTrue(!shark.isAlive() || triton.getTarget() == shark || shark.getLastHurtByMob() == triton,
                "Die Wache greift den Hai nicht an"));
    }

    /**
     * Das Meeresdorf: Bauwerk, Platzierung (fern des Krakenschlunds), Biome, Beute; ein Dorf auf dem Testplatz – die
     * Dreizacksäule mit leuchtenden Spitzen auf dem Platz aus Prismarin und Perlmutt, Häuser aus Korallenholz, der Turm,
     * Truhen, Bewohner aller vier Berufe und zwei Wachen.
     */
    private static void seaVillage(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var registries = level.getServer().registryAccess();
        helper.assertTrue(registries.lookupOrThrow(Registries.STRUCTURE).get(ModStructures.SEA_VILLAGE_STRUCTURE).isPresent(), "Das Bauwerk fehlt");
        var set = registries.lookupOrThrow(Registries.STRUCTURE_SET).get(ModStructures.SEA_VILLAGES);
        helper.assertTrue(set.isPresent() && set.get().value().placement() instanceof RandomSpreadStructurePlacement,
                "Die Platzierung der Meeresdörfer fehlt");
        var biomes = registries.lookupOrThrow(Registries.BIOME);
        for (var key : List.of(UrmeerBiomes.KELP_FOREST, UrmeerBiomes.CORAL_GARDEN, UrmeerBiomes.PEARL_GROUNDS, UrmeerBiomes.DEEP)) {
            helper.assertTrue(biomes.getOrThrow(key).is(ModTags.Biomes.HAS_SEA_VILLAGE), "Kein Meeresdorf in " + key.identifier());
        }
        for (var no : List.of(UrmeerBiomes.ISLES, Biomes.DEEP_OCEAN, Biomes.PLAINS)) {
            helper.assertFalse(biomes.getOrThrow(no).is(ModTags.Biomes.HAS_SEA_VILLAGE), "Ein Meeresdorf in " + no.identifier());
        }
        helper.assertTrue(level.getServer().reloadableRegistries().getLootTable(SeaVillagePiece.CHEST) != LootTable.EMPTY,
                "Die Truhen des Dorfs haben keine Beute");

        BlockPos center = helper.absolutePos(new BlockPos(4, 0, 4));
        int f = center.getY();
        int sea = f + 22;
        BoundingBox box = new BoundingBox(center.getX() - SeaVillagePiece.CORE - 1, f - 6, center.getZ() - SeaVillagePiece.CORE - 1,
                center.getX() + SeaVillagePiece.CORE + 1, f + 20, center.getZ() + SeaVillagePiece.CORE + 1);
        SeaVillagePiece village = new SeaVillagePiece(center, 9_311L, sea, 1);
        village.postProcess(level, level.structureManager(), level.getChunkSource().getGenerator(), RandomSource.create(5), box,
                ChunkPos.containing(center), center);
        int houses = village.houseCount();
        helper.assertTrue(houses >= 7, "Zu wenige Häuser im Plan: " + houses);
        helper.assertTrue(level.getBlockState(center.above(1)).is(Blocks.DARK_PRISMARINE), "Kein Sockel der Dreizacksäule");
        helper.assertTrue(level.getBlockState(center.above(4)).is(Blocks.PRISMARINE_WALL), "Keine Dreizacksäule");
        helper.assertTrue(level.getBlockState(center.above(14)).is(Blocks.SEA_LANTERN), "Die mittlere Zinke leuchtet nicht");
        helper.assertTrue(level.getBlockState(center.offset(2, 13, 0)).is(Blocks.SEA_LANTERN), "Die Seitenzinke leuchtet nicht");
        helper.assertTrue(level.getBlockState(center.east(6)).is(Blocks.DARK_PRISMARINE), "Kein Ring aus dunklem Prismarin im Platz");
        helper.assertTrue(level.getBlockState(center.east(5)).is(ModBlocks.NACRE_BLOCK.get()), "Keine Speichen aus Perlmutt");
        int planks = 0;
        int nacre = 0;
        int chests = 0;
        for (BlockPos pos : BlockPos.betweenClosed(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ())) {
            var state = level.getBlockState(pos);
            if (state.is(SeaWoods.CORAL.planks.base().get())) {
                planks++;
            } else if (state.is(ModBlocks.NACRE_BLOCK.get())) {
                nacre++;
            } else if (state.is(Blocks.CHEST) && level.getBlockEntity(pos) instanceof ChestBlockEntity chest && chest.getLootTable() != null) {
                chests++;
            }
        }
        helper.assertTrue(planks > 200, "Zu wenig Häuser aus Korallenholz: " + planks);
        helper.assertTrue(nacre > 60, "Kein Turm aus Perlmutt: " + nacre);
        helper.assertTrue(chests == houses, "Nicht in jedem Haus eine Truhe: " + chests + " von " + houses);
        AABB area = AABB.of(box);
        List<MermaidEntity> mermaids = level.getEntitiesOfClass(MermaidEntity.class, area);
        List<TritonEntity> guards = level.getEntitiesOfClass(TritonEntity.class, area);
        Set<MermaidProfession> professions = EnumSet.noneOf(MermaidProfession.class);
        for (MermaidEntity m : mermaids) {
            professions.add(m.profession());
            helper.assertTrue(m.hasHome(), "Eine Meerjungfrau gehört zu keinem Dorf");
        }
        helper.assertTrue(mermaids.size() == houses, "Nicht eine Meerjungfrau je Haus: " + mermaids.size() + " von " + houses);
        helper.assertTrue(professions.containsAll(List.of(MermaidProfession.TRADERS)), "Nicht alle Berufe im Dorf: " + professions);
        helper.assertTrue(guards.size() == 2, "Nicht zwei Wachen: " + guards.size());
        // aufräumen: die Bewohner sollen nicht in andere Tests schwimmen
        mermaids.forEach(MermaidEntity::discard);
        guards.forEach(TritonEntity::discard);
        helper.succeed();
    }
}
