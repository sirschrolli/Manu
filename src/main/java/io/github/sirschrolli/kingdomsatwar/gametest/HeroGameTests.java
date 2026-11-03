package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.CrystalFormationBlock;
import io.github.sirschrolli.kingdomsatwar.hero.Crystals;
import io.github.sirschrolli.kingdomsatwar.loot.ModLootTables;
import io.github.sirschrolli.kingdomsatwar.magic.Mana;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import io.github.sirschrolli.kingdomsatwar.worldgen.CrystalFormationFeature;
import io.github.sirschrolli.kingdomsatwar.worldgen.ModWorldgen;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Tests zum eigenen Helden: Herz- und Manakristalle geben dauerhaft ein Herz bzw. 20 Mana mehr (höchstens zehn jeder
 * Sorte), sie wachsen auf Höhlenböden (nie unter freiem Himmel, im Wasser oder auf Erde), lassen sich abbauen, und
 * Schatztruhen, Drachenbosse und Erzmagier geben sie her.
 */
final class HeroGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CRYSTALS =
            ModGameTests.TEST_FUNCTIONS.register("hero_crystals", () -> HeroGameTests::crystals);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CRYSTAL_CAVES =
            ModGameTests.TEST_FUNCTIONS.register("hero_crystal_caves", () -> HeroGameTests::crystalCaves);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CRYSTAL_LOOT =
            ModGameTests.TEST_FUNCTIONS.register("hero_crystal_loot", () -> HeroGameTests::crystalLoot);

    private HeroGameTests() {}

    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var test : List.of(CRYSTALS, CRYSTAL_CAVES, CRYSTAL_LOOT)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 200, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
    }

    /**
     * Herzkristalle: Rechtsklick nimmt einen auf – ein Herz mehr, gleich gefüllt; nach zehn ist Schluss (der Kristall
     * bleibt in der Hand). Die Herzen kommen nach dem Wiedereinstieg zurück. Manakristalle: 20 Mana mehr, gleich gefüllt,
     * und das Mana erholt sich bis zum neuen Höchstwert.
     */
    private static void crystals(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        try {
            player.setGameMode(GameType.SURVIVAL);
            Crystals.set(player, Crystals.Count.NONE);
            helper.assertTrue(near(player.getMaxHealth(), 20.0f), "Ohne Kristalle sollte man zehn Herzen haben: " + player.getMaxHealth());

            // --- Herzkristalle ---
            player.setHealth(10.0f);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.HEART_CRYSTAL.get(), 12));
            InteractionResult first = player.getMainHandItem().use(level, player, InteractionHand.MAIN_HAND);
            helper.assertTrue(first.consumesAction(), "Herzkristall lässt sich nicht aufnehmen: " + first);
            helper.assertTrue(Crystals.of(player).hearts() == 1 && near(player.getMaxHealth(), 22.0f) && near(player.getHealth(), 12.0f),
                    "Ein Herzkristall sollte ein Herz mehr geben (und es füllen): " + Crystals.of(player) + ", "
                            + player.getHealth() + "/" + player.getMaxHealth());
            helper.assertTrue(player.getMainHandItem().getCount() == 11, "Der Kristall wird nicht verbraucht");
            for (int i = 1; i < Crystals.MAX; i++) {
                player.getMainHandItem().use(level, player, InteractionHand.MAIN_HAND);
            }
            helper.assertTrue(Crystals.of(player).hearts() == Crystals.MAX && near(player.getMaxHealth(), 40.0f),
                    "Zehn Herzkristalle sollten zwanzig Herzen geben: " + player.getMaxHealth());
            InteractionResult full = player.getMainHandItem().use(level, player, InteractionHand.MAIN_HAND);
            helper.assertTrue(!full.consumesAction() && player.getMainHandItem().getCount() == 2 && Crystals.of(player).hearts() == Crystals.MAX,
                    "Über zehn Herzkristalle hinaus sollte nichts gehen: " + full + ", " + player.getMainHandItem().getCount());

            // nach dem Tod oder Wiedereinstieg: die Herzen kommen zurück
            var health = player.getAttribute(Attributes.MAX_HEALTH);
            helper.assertTrue(health != null && health.getModifier(Crystals.HEALTH_ID) != null, "Die Herzen fehlen als Steigerung");
            health.removeModifier(Crystals.HEALTH_ID);
            helper.assertTrue(near(player.getMaxHealth(), 20.0f), "Steigerung lässt sich nicht entfernen");
            Crystals.applyHealth(player);
            helper.assertTrue(near(player.getMaxHealth(), 40.0f), "Die Herzen kommen nicht zurück: " + player.getMaxHealth());

            // --- Manakristalle ---
            Mana.set(player, 0.0f);
            helper.assertTrue(near(Mana.maxFor(player), Mana.BASE_MAX), "Ohne Manakristall sollte man " + Mana.BASE_MAX + " Mana fassen");
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.MANA_CRYSTAL.get(), 3));
            helper.assertTrue(player.getMainHandItem().use(level, player, InteractionHand.MAIN_HAND).consumesAction(),
                    "Manakristall lässt sich nicht aufnehmen");
            helper.assertTrue(Crystals.of(player).mana() == 1 && near(Mana.maxFor(player), Mana.BASE_MAX + Crystals.MANA_PER_CRYSTAL)
                    && near(Mana.get(player), Crystals.MANA_PER_CRYSTAL), "Ein Manakristall sollte 20 Mana mehr geben (und füllen): "
                    + Mana.get(player) + "/" + Mana.maxFor(player));
            Mana.regenerate(player, 20 * 120);
            helper.assertTrue(near(Mana.get(player), Mana.BASE_MAX + Crystals.MANA_PER_CRYSTAL),
                    "Mana erholt sich nicht bis zum neuen Höchstwert: " + Mana.get(player));
            helper.assertTrue(Crystals.of(player).hearts() == Crystals.MAX, "Manakristall verändert die Herzen");

            // zurücksetzen: Lebenspunkte sinken wieder auf zehn Herzen
            Crystals.set(player, Crystals.Count.NONE);
            helper.assertTrue(near(player.getMaxHealth(), 20.0f) && player.getHealth() <= 20.0f, "Zurücksetzen nimmt die Herzen nicht");
            helper.succeed();
        } finally {
            level.getServer().getPlayerList().remove(player);
        }
    }

    /**
     * Höhlenkristalle: Beide Weltgen-Merkmale gibt es und sie setzen ihren Kristall. Er wächst auf Gestein in einer Höhle
     * (Luft darüber), nicht unter freiem Himmel, nicht auf Erde, nicht im Wasser und nicht unter einer zu niedrigen Decke;
     * er schaut in verschiedene Richtungen. Abgebaut gibt er den Kristall – auch wenn man ihm den Boden wegschlägt.
     */
    private static void crystalCaves(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var features = level.registryAccess().lookupOrThrow(Registries.FEATURE);
        var placed = level.registryAccess().lookupOrThrow(Registries.PLACED_FEATURE);
        for (ModWorldgen.CrystalVein vein : ModWorldgen.CRYSTAL_VEINS) {
            Feature feature = features.getOrThrow(vein.featureKey()).value();
            helper.assertTrue(feature instanceof CrystalFormationFeature f && f.crystal().is(vein.crystal().get()),
                    "Weltgen-Merkmal " + vein.name() + " setzt nicht " + vein.crystal().get());
            helper.assertTrue(placed.get(vein.placedKey()).isPresent(), "Platziertes Merkmal " + vein.name() + " fehlt");
        }

        BlockPos column = new BlockPos(4, 0, 4);
        BlockPos floor = column.above();
        BlockPos spot = floor.above();
        BlockPos origin = spot.above();
        LevelChunk chunk = level.getChunkAt(helper.absolutePos(column));
        Heightmap.primeHeightmaps(chunk, EnumSet.of(Heightmap.Types.WORLD_SURFACE_WG));
        try {
            BlockState heart = ModBlocks.HEART_CRYSTAL_FORMATION.get().defaultBlockState();
            CrystalFormationFeature small = new CrystalFormationFeature(heart, 3);

            // unter freiem Himmel: nichts
            clear(helper, column);
            helper.setBlock(floor, Blocks.STONE);
            helper.assertTrue(!place(helper, small, origin, 1), "Kristall wächst unter freiem Himmel");

            // in einer Höhle (die Oberfläche liegt weit darüber): auf Gestein ja ...
            BlockPos abs = helper.absolutePos(column);
            chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG)
                    .update(abs.getX() & 15, abs.getY() + 30, abs.getZ() & 15, Blocks.STONE.defaultBlockState());
            for (Block rock : List.of(Blocks.STONE, Blocks.DEEPSLATE, Blocks.TUFF)) {
                clear(helper, column);
                helper.setBlock(floor, rock);
                helper.assertTrue(place(helper, small, origin, 2), "Kristall wächst nicht auf " + rock);
                helper.assertBlockPresent(ModBlocks.HEART_CRYSTAL_FORMATION.get(), spot);
            }
            // ... auf Erde, im Wasser und unter zu niedriger Decke nicht
            clear(helper, column);
            helper.setBlock(floor, Blocks.DIRT);
            helper.assertTrue(!place(helper, small, origin, 3), "Kristall wächst auf Erde");
            clear(helper, column);
            helper.setBlock(floor, Blocks.STONE);
            helper.setBlock(spot, Blocks.WATER);
            helper.setBlock(origin, Blocks.WATER);
            helper.assertTrue(!place(helper, small, origin, 4), "Kristall wächst im Wasser");
            clear(helper, column);
            helper.setBlock(floor, Blocks.STONE);
            for (int y = 1; y <= 3; y++) {
                helper.setBlock(spot.above(y), Blocks.STONE);
            }
            helper.assertTrue(!place(helper, small, spot, 5), "Kristall wächst unter zu niedriger Decke");

            // verschiedene Richtungen
            Set<Direction> facings = new HashSet<>();
            for (int seed = 0; seed < 12; seed++) {
                clear(helper, column);
                helper.setBlock(floor, Blocks.STONE);
                place(helper, small, origin, 0x9E3779B97F4A7C15L * (seed + 1));
                facings.add(helper.getBlockState(spot).getValue(HorizontalDirectionalBlock.FACING));
            }
            helper.assertTrue(facings.size() >= 3, "Kristalle schauen immer gleich: " + facings);

            // Abbauen gibt den Kristall; ohne Boden zerbricht er und gibt ihn ebenso
            for (Crystals.Kind kind : Crystals.Kind.values()) {
                Block block = kind == Crystals.Kind.HEART ? ModBlocks.HEART_CRYSTAL_FORMATION.get() : ModBlocks.MANA_CRYSTAL_FORMATION.get();
                Item item = kind == Crystals.Kind.HEART ? ModItems.HEART_CRYSTAL.get() : ModItems.MANA_CRYSTAL.get();
                helper.assertTrue(((CrystalFormationBlock) block).kind() == kind, block + " ist kein " + kind);

                clear(helper, column);
                helper.setBlock(floor, Blocks.STONE);
                helper.setBlock(spot, block);
                level.destroyBlock(helper.absolutePos(spot), true);
                helper.assertTrue(takeDrops(helper, spot, item) == 1, block + " gibt beim Abbauen keinen Kristall");

                clear(helper, column);
                helper.setBlock(floor, Blocks.STONE);
                helper.setBlock(spot, block);
                level.destroyBlock(helper.absolutePos(floor), false);
                helper.assertBlockNotPresent(block, spot);
                helper.assertTrue(takeDrops(helper, spot, item) == 1, block + " gibt ohne Boden keinen Kristall");
            }
            clear(helper, column);
            helper.succeed();
        } finally {
            Heightmap.primeHeightmaps(chunk, EnumSet.of(Heightmap.Types.WORLD_SURFACE_WG));
        }
    }

    /**
     * Beute: In der Schatzkammer der Stammburg liegen Herz- und Manakristalle, im Allerheiligsten des Magierturms vor
     * allem Manakristalle; jeder Drachenboss lässt einen Herzkristall fallen (Nyxar zwei und einen Manakristall), jeder
     * Erzmagier einen Manakristall.
     */
    private static void crystalLoot(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var tables = level.getServer().reloadableRegistries();
        LootParams chest = new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(helper.absolutePos(BlockPos.ZERO)))
                .create(LootContextParamSets.CHEST);
        int[] treasury = count(tables.getLootTable(ModLootTables.REALM_TREASURY), chest, 400);
        helper.assertTrue(treasury[0] >= 60 && treasury[1] >= 25,
                "Schatzkammer gibt zu wenig Kristalle: " + treasury[0] + " Herz, " + treasury[1] + " Mana in 400 Truhen");
        int[] sanctum = count(tables.getLootTable(ModLootTables.MAGE_TOWER_SANCTUM), chest, 400);
        helper.assertTrue(sanctum[1] >= 120 && sanctum[1] > sanctum[0] * 2,
                "Allerheiligstes gibt zu wenig Manakristalle: " + sanctum[0] + " Herz, " + sanctum[1] + " Mana in 400 Truhen");

        ServerPlayer victim = helper.makeMockServerPlayerInLevel();
        try {
            LootParams death = new LootParams.Builder(level)
                    .withParameter(LootContextParams.THIS_ENTITY, victim)
                    .withParameter(LootContextParams.ORIGIN, victim.position())
                    .withParameter(LootContextParams.DAMAGE_SOURCE, level.damageSources().generic())
                    .create(LootContextParamSets.ENTITY);
            List<EntityType<?>> bosses = new ArrayList<>(List.of(ModEntities.PYRRHON.get(), ModEntities.GLACIARA.get(), ModEntities.VERDANTIS.get(),
                    ModEntities.TERRAGON.get(), ModEntities.VOLTARION.get(), ModEntities.THALASSA.get()));
            for (EntityType<?> boss : bosses) {
                int[] loot = count(table(level, boss), death, 1);
                helper.assertTrue(loot[0] == 1 && loot[1] == 0, boss + " lässt nicht genau einen Herzkristall fallen: " + loot[0] + "/" + loot[1]);
            }
            int[] nyxar = count(table(level, ModEntities.NYXAR.get()), death, 1);
            helper.assertTrue(nyxar[0] == 2 && nyxar[1] == 1, "Nyxar lässt nicht zwei Herz- und einen Manakristall fallen: " + nyxar[0] + "/" + nyxar[1]);
            for (var mage : ModEntities.MAGES.values()) {
                int[] loot = count(table(level, mage.get()), death, 1);
                helper.assertTrue(loot[0] == 0 && loot[1] == 1, mage.getId() + " lässt nicht genau einen Manakristall fallen: " + loot[0] + "/" + loot[1]);
            }
        } finally {
            level.getServer().getPlayerList().remove(victim);
        }
        helper.succeed();
    }

    // --- Hilfen ---

    private static boolean near(float a, float b) {
        return Math.abs(a - b) < 0.01f;
    }

    /** Leert die Säule über dem Boden der Arena (Boden, Kristallplatz und alles darüber bis unters Dach der Arena). */
    private static void clear(GameTestHelper helper, BlockPos column) {
        for (int y = 1; y <= 5; y++) {
            helper.setBlock(column.above(y), Blocks.AIR);
        }
        helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(column)).inflate(3, 6, 3)).forEach(ItemEntity::discard);
    }

    private static boolean place(GameTestHelper helper, CrystalFormationFeature feature, BlockPos origin, long seed) {
        ServerLevel level = helper.getLevel();
        return feature.place(level, level.getChunkSource().getGenerator(), RandomSource.create(seed), helper.absolutePos(origin));
    }

    /** Wie viele dieser Dinge um die Stelle herum liegen (sie werden dabei weggeräumt). */
    private static int takeDrops(GameTestHelper helper, BlockPos local, Item item) {
        int count = 0;
        for (ItemEntity drop : helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(local)).inflate(2))) {
            if (drop.getItem().is(item)) {
                count += drop.getItem().getCount();
            }
            drop.discard();
        }
        return count;
    }

    private static LootTable table(ServerLevel level, EntityType<?> type) {
        ResourceKey<LootTable> key = type.getDefaultLootTable().orElseThrow();
        return level.getServer().reloadableRegistries().getLootTable(key);
    }

    /** Herz- und Manakristalle in so vielen Würfen. */
    private static int[] count(LootTable table, LootParams params, int rolls) {
        int[] found = new int[2];
        for (int i = 0; i < rolls; i++) {
            for (ItemStack stack : table.getRandomItems(params)) {
                if (stack.is(ModItems.HEART_CRYSTAL.get())) {
                    found[0] += stack.getCount();
                } else if (stack.is(ModItems.MANA_CRYSTAL.get())) {
                    found[1] += stack.getCount();
                }
            }
        }
        return found;
    }
}
