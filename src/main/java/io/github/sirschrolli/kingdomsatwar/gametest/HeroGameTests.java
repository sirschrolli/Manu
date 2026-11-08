package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.CrystalFormationBlock;
import io.github.sirschrolli.kingdomsatwar.hero.Crystals;
import io.github.sirschrolli.kingdomsatwar.hero.HeroLevel;
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
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LEVELS =
            ModGameTests.TEST_FUNCTIONS.register("hero_levels", () -> HeroGameTests::levels);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TALENTS =
            ModGameTests.TEST_FUNCTIONS.register("hero_talents", () -> HeroGameTests::talents);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SOURCES =
            ModGameTests.TEST_FUNCTIONS.register("hero_sources", () -> HeroGameTests::sources);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SETTLE =
            ModGameTests.TEST_FUNCTIONS.register("hero_settle", () -> HeroGameTests::settle);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> PAT =
            ModGameTests.TEST_FUNCTIONS.register("hero_pat", () -> HeroGameTests::pat);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> COMMAND =
            ModGameTests.TEST_FUNCTIONS.register("hero_command", () -> HeroGameTests::command);

    private HeroGameTests() {}

    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var test : List.of(CRYSTALS, CRYSTAL_CAVES, CRYSTAL_LOOT, LEVELS, TALENTS, SOURCES, SETTLE, PAT, COMMAND)) {
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

    /**
     * Heldenstufen: steigende Schwellen bis Stufe 100 (392.040 Heldenerfahrung), jede Stufe zu genau ihrer Erfahrung, ein
     * Talentpunkt je Stufe und zwei für jeden neuen Rang (Recke 20, Ritter 40, Held 60, Kriegsherr 80, Legende 100); mit
     * allen Punkten lässt sich nicht jedes Talent meistern.
     */
    private static void levels(GameTestHelper helper) {
        helper.assertValueEqual(HeroLevel.total(1), 0, "Stufe 1 ohne Erfahrung");
        for (int level = 2; level <= HeroLevel.MAX_LEVEL; level++) {
            int need = HeroLevel.total(level);
            helper.assertTrue(need > HeroLevel.total(level - 1), "Schwelle zu Heldenstufe " + level + " steigt nicht");
            helper.assertValueEqual(HeroLevel.levelFor(need), level, "Heldenstufe zur Schwelle " + need);
            helper.assertValueEqual(HeroLevel.levelFor(need - 1), level - 1, "Heldenstufe knapp unter der Schwelle " + need);
        }
        helper.assertValueEqual(HeroLevel.levelFor(Integer.MAX_VALUE / 2), HeroLevel.MAX_LEVEL, "höchste Heldenstufe");
        HeroLevel.Hero hero = new HeroLevel.Hero(HeroLevel.total(12), List.of(3, 2), 0);
        helper.assertValueEqual(hero.talents().size(), HeroLevel.Talent.values().length, "Talente werden aufgefüllt");
        helper.assertValueEqual(hero.unspent(), 11 - 5, "freie Talentpunkte auf Stufe 12");
        helper.assertValueEqual(HeroLevel.MAX_LEVEL, 100, "höchste Heldenstufe");
        helper.assertValueEqual(HeroLevel.total(HeroLevel.MAX_LEVEL), 392_040, "Heldenerfahrung für Stufe 100");
        helper.assertTrue(HeroLevel.total(51) - HeroLevel.total(50) > 3_000, "eine Stufe um 50 ist zu schnell erreicht");
        // Ränge und ihre Punkte
        helper.assertValueEqual(HeroLevel.Rank.of(1), HeroLevel.Rank.SQUIRE, "Rang auf Stufe 1");
        helper.assertValueEqual(HeroLevel.Rank.of(19), HeroLevel.Rank.SQUIRE, "Rang auf Stufe 19");
        helper.assertValueEqual(HeroLevel.Rank.of(20), HeroLevel.Rank.WARRIOR, "Rang auf Stufe 20");
        helper.assertValueEqual(HeroLevel.Rank.of(40), HeroLevel.Rank.KNIGHT, "Rang auf Stufe 40");
        helper.assertValueEqual(HeroLevel.Rank.of(79), HeroLevel.Rank.HERO, "Rang auf Stufe 79");
        helper.assertValueEqual(HeroLevel.Rank.of(99), HeroLevel.Rank.WARLORD, "Rang auf Stufe 99");
        helper.assertValueEqual(HeroLevel.Rank.of(100), HeroLevel.Rank.LEGEND, "Rang auf Stufe 100");
        helper.assertValueEqual(HeroLevel.pointsFor(19), 18, "Talentpunkte auf Stufe 19");
        helper.assertValueEqual(HeroLevel.pointsFor(20), 19 + HeroLevel.RANK_BONUS, "Talentpunkte auf Stufe 20 (mit Rangbonus)");
        helper.assertValueEqual(HeroLevel.pointsFor(100), 99 + 5 * HeroLevel.RANK_BONUS, "Talentpunkte auf Stufe 100");
        helper.assertTrue(HeroLevel.pointsFor(HeroLevel.MAX_LEVEL) < HeroLevel.Talent.values().length * HeroLevel.MAX_TALENT,
                "mit allen Punkten ließe sich jedes Talent meistern");
        helper.succeed();
    }

    /**
     * Talente: Punkte gibt es nur, so viele die Stufe hergibt, höchstens zwanzig je Talent; jeder Punkt wirkt sofort (Schaden,
     * Leben, Tempo, Rüstung, Mana, Glück). Neu verteilen kostet eine Silbermünze je Punkt – ohne Geld geht es nicht.
     * Die Erfahrung von Minecraft bleibt dabei unberührt.
     */
    private static void talents(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        try {
            player.setGameMode(GameType.SURVIVAL);
            player.getInventory().clearContent();
            HeroLevel.set(player, HeroLevel.Hero.NONE);
            double damage = player.getAttributeValue(Attributes.ATTACK_DAMAGE);
            double health = player.getAttributeValue(Attributes.MAX_HEALTH);
            double speed = player.getAttributeValue(Attributes.MOVEMENT_SPEED);
            double armor = player.getAttributeValue(Attributes.ARMOR);
            double luck = player.getAttributeValue(Attributes.LUCK);
            float mana = Mana.maxFor(player);
            int vanilla = player.totalExperience;
            helper.assertTrue(!HeroLevel.spend(player, HeroLevel.Talent.STRENGTH), "Punkt ohne Heldenstufe");
            HeroLevel.award(player, HeroLevel.total(7));
            helper.assertValueEqual(HeroLevel.of(player).level(), 7, "Heldenstufe nach der Erfahrung");
            helper.assertValueEqual(player.totalExperience, vanilla, "die Minecraft-Erfahrung darf sich nicht ändern");
            for (HeroLevel.Talent talent : HeroLevel.Talent.values()) {
                helper.assertTrue(HeroLevel.spend(player, talent), "Punkt für " + talent + " geht nicht");
            }
            helper.assertTrue(!HeroLevel.spend(player, HeroLevel.Talent.STRENGTH), "mehr Punkte als die Stufe hergibt");
            helper.assertTrue(Math.abs(player.getAttributeValue(Attributes.ATTACK_DAMAGE) - (damage + 0.3)) < 1.0e-6, "Stärke");
            helper.assertTrue(Math.abs(player.getAttributeValue(Attributes.MAX_HEALTH) - (health + 1.0)) < 1.0e-6, "Lebenskraft");
            helper.assertTrue(Math.abs(player.getAttributeValue(Attributes.MOVEMENT_SPEED) - speed * 1.0125) < 1.0e-6, "Gewandtheit");
            helper.assertTrue(Math.abs(player.getAttributeValue(Attributes.ARMOR) - (armor + 0.4)) < 1.0e-6, "Zähigkeit");
            helper.assertTrue(Math.abs(player.getAttributeValue(Attributes.LUCK) - (luck + 0.2)) < 1.0e-6, "Glück");
            helper.assertTrue(Math.abs(Mana.maxFor(player) - (mana + 8.0f)) < 1.0e-3f, "Zauberkunst");
            // höchstens zwanzig Punkte je Talent
            HeroLevel.award(player, HeroLevel.total(30));
            int spent = 0;
            while (HeroLevel.spend(player, HeroLevel.Talent.STRENGTH)) {
                spent++;
            }
            helper.assertValueEqual(HeroLevel.of(player).points(HeroLevel.Talent.STRENGTH), HeroLevel.MAX_TALENT, "Stärke voll");
            helper.assertValueEqual(spent, HeroLevel.MAX_TALENT - 1, "Punkte bis zum vollen Talent");
            helper.assertTrue(Math.abs(player.getAttributeValue(Attributes.ATTACK_DAMAGE) - (damage + 6.0)) < 1.0e-6, "Stärke gemeistert");
            // die Werte kommen nach dem Wiedereinstieg zurück
            player.getAttribute(Attributes.ATTACK_DAMAGE).removeModifier(KingdomsAtWar.id("hero_strength"));
            HeroLevel.apply(player);
            helper.assertTrue(Math.abs(player.getAttributeValue(Attributes.ATTACK_DAMAGE) - (damage + 6.0)) < 1.0e-6, "Stärke nach dem Wiedereinstieg");
            // neu verteilen: erst ohne, dann mit Geld
            int cost = HeroLevel.resetCost(HeroLevel.of(player));
            helper.assertValueEqual(cost, HeroLevel.of(player).spent() * HeroLevel.RESET_PER_POINT, "Preis des Neuverteilens");
            helper.assertTrue(!HeroLevel.reset(player), "Neu verteilen ohne Geld");
            player.getInventory().add(new ItemStack(ModItems.GOLD_COIN.get(), 9));
            int before = io.github.sirschrolli.kingdomsatwar.economy.Coins.total(player);
            helper.assertTrue(HeroLevel.reset(player), "Neu verteilen mit Geld");
            helper.assertValueEqual(io.github.sirschrolli.kingdomsatwar.economy.Coins.total(player), before - cost, "Münzen nach dem Neuverteilen");
            HeroLevel.Hero hero = HeroLevel.of(player);
            helper.assertTrue(hero.spent() == 0 && hero.unspent() == HeroLevel.pointsFor(hero.level()), "nach dem Neuverteilen sind alle Punkte frei");
            helper.assertTrue(Math.abs(player.getAttributeValue(Attributes.ATTACK_DAMAGE) - damage) < 1.0e-6, "Stärke nach dem Neuverteilen");
            helper.assertTrue(Math.abs(player.getAttributeValue(Attributes.MAX_HEALTH) - health) < 1.0e-6, "Leben nach dem Neuverteilen");
            helper.assertValueEqual(player.totalExperience, vanilla, "die Minecraft-Erfahrung darf sich nicht ändern");
            HeroLevel.set(player, HeroLevel.Hero.NONE);
            helper.succeed();
        } finally {
            level(helper).getServer().getPlayerList().remove(player);
        }
    }

    /**
     * Woher Heldenerfahrung kommt: ein besiegter Zombie (die Hälfte seiner Lebenspunkte), ein Viertel davon, wenn ein
     * eigener Soldat siegt, nichts für Tiere; Erze nach ihrem Wert (Diamant viel, Kohle wenig, Stein gar nicht);
     * Fortschritte nach ihrer Art; Biome werden zu ihrem Namen.
     */
    private static void sources(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        try {
            player.setGameMode(GameType.SURVIVAL);
            // die Fortschritte, die der erste Sieg bringt, hat er schon (die gäben selbst Heldenerfahrung)
            var advancements = level.getServer().getAdvancements();
            for (String id : List.of("adventure/root", "adventure/kill_a_mob")) {
                var holder = advancements.get(net.minecraft.resources.Identifier.withDefaultNamespace(id));
                if (holder != null) {
                    holder.value().criteria().keySet().forEach(criterion -> player.getAdvancements().award(holder, criterion));
                }
            }
            HeroLevel.set(player, HeroLevel.Hero.NONE);
            int vanilla = player.totalExperience;
            var zombie = helper.spawn(ModEntities.ZOMBIE.get(), new BlockPos(4, 1, 6));
            int expected = HeroLevel.killExperience(zombie);
            zombie.hurtServer(level, level.damageSources().playerAttack(player), 1000.0f);
            helper.assertValueEqual(HeroLevel.of(player).experience(), expected, "Heldenerfahrung für den Zombie");
            helper.assertValueEqual(player.totalExperience, vanilla, "die Minecraft-Erfahrung darf sich nicht ändern");
            // ein eigener Soldat siegt: ein Viertel
            var soldier = helper.spawn(ModEntities.SOLDIERS.get(io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType.SWORDSMAN).get(),
                    new BlockPos(2, 1, 2));
            soldier.setOwner(player);
            var second = helper.spawn(ModEntities.ZOMBIE.get(), new BlockPos(6, 1, 6));
            second.hurtServer(level, level.damageSources().mobAttack(soldier), 1000.0f);
            helper.assertValueEqual(HeroLevel.of(player).experience(), expected + Math.round(expected * HeroLevel.FOLLOWER_SHARE),
                    "Anteil am Sieg des Soldaten");
            // Tiere geben nichts
            int before = HeroLevel.of(player).experience();
            var cow = helper.spawn(net.minecraft.world.entity.EntityTypes.COW, new BlockPos(6, 1, 2));
            cow.hurtServer(level, level.damageSources().playerAttack(player), 1000.0f);
            helper.assertValueEqual(HeroLevel.of(player).experience(), before, "Heldenerfahrung für eine Kuh");
            // Erze
            helper.assertTrue(HeroLevel.oreExperience(net.minecraft.world.level.block.Blocks.DIAMOND_ORE.defaultBlockState())
                    > HeroLevel.oreExperience(net.minecraft.world.level.block.Blocks.IRON_ORE.defaultBlockState()), "Diamant lohnt nicht mehr als Eisen");
            helper.assertTrue(HeroLevel.oreExperience(net.minecraft.world.level.block.Blocks.IRON_ORE.defaultBlockState())
                    > HeroLevel.oreExperience(net.minecraft.world.level.block.Blocks.COAL_ORE.defaultBlockState()), "Eisen lohnt nicht mehr als Kohle");
            helper.assertValueEqual(HeroLevel.oreExperience(net.minecraft.world.level.block.Blocks.STONE.defaultBlockState()), 0, "Stein gibt etwas");
            helper.assertTrue(HeroLevel.oreExperience(ModBlocks.GEMSTONE_ORE.get().defaultBlockState()) >= 5, "Edelsteinerz gibt zu wenig");
            // Fortschritte
            var task = advancements.get(net.minecraft.resources.Identifier.withDefaultNamespace("story/mine_stone"));
            var challenge = advancements.get(net.minecraft.resources.Identifier.withDefaultNamespace("adventure/kill_all_mobs"));
            helper.assertTrue(task != null && challenge != null, "Vanilla-Fortschritte fehlen");
            helper.assertTrue(HeroLevel.advancementExperience(challenge.value().display().orElseThrow())
                    > HeroLevel.advancementExperience(task.value().display().orElseThrow()), "Herausforderung lohnt nicht mehr als Aufgabe");
            helper.succeed();
        } finally {
            level.getServer().getPlayerList().remove(player);
        }
    }

    /**
     * Alte Spielstände: Wer nach den neuen Stufen weniger Punkte hat, als er verteilt hat, bekommt alle zurück (kostenlos);
     * wer genug hat, behält seine Verteilung.
     */
    private static void settle(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        try {
            // früher Stufe 13 mit zwölf verteilten Punkten – heute ist das Stufe 10 mit neun Punkten
            HeroLevel.set(player, new HeroLevel.Hero(3_420, List.of(5, 5, 2, 0, 0, 0), 4));
            helper.assertValueEqual(HeroLevel.of(player).level(), 10, "Stufe mit 3.420 Heldenerfahrung");
            helper.assertTrue(HeroLevel.settle(player), "zu viele verteilte Punkte werden nicht zurückgegeben");
            HeroLevel.Hero hero = HeroLevel.of(player);
            helper.assertTrue(hero.spent() == 0 && hero.experience() == 3_420 && hero.discoveries() == 4 && hero.unspent() == 9,
                    "nach dem Zurückgeben: " + hero);
            HeroLevel.set(player, new HeroLevel.Hero(3_420, List.of(5, 4, 0, 0, 0, 0), 4));
            helper.assertTrue(!HeroLevel.settle(player) && HeroLevel.of(player).spent() == 9, "passende Verteilung wurde angetastet");
            HeroLevel.set(player, HeroLevel.Hero.NONE);
            helper.succeed();
        } finally {
            level(helper).getServer().getPlayerList().remove(player);
        }
    }

    /**
     * Streicheln, die Belohnung der Legende: unter Stufe 100 geht es nicht; als Legende streichelt man das Wesen vor sich,
     * nicht zweimal im selben Augenblick und nicht aus der Ferne. Ein eigenes, verletztes Tier bekommt ein halbes Herz
     * zurück – aber nicht bei jedem Streicheln.
     */
    private static void pat(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        BlockPos floor = helper.absolutePos(new BlockPos(1, 1, 1));
        net.minecraft.world.entity.animal.wolf.Wolf[] wolf = {null};
        net.minecraft.world.entity.animal.cow.Cow[] far = {null};
        try {
            player.setGameMode(GameType.SURVIVAL);
            player.snapTo(floor.getX() + 0.5, floor.getY(), floor.getZ() + 0.5, 0.0f, 0.0f);
            wolf[0] = net.minecraft.world.entity.EntityTypes.WOLF.create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
            wolf[0].snapTo(floor.getX() + 0.5, floor.getY(), floor.getZ() + 2.5, 0.0f, 0.0f);
            wolf[0].setNoAi(true);
            level.addFreshEntity(wolf[0]);
            far[0] = net.minecraft.world.entity.EntityTypes.COW.create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
            far[0].snapTo(floor.getX() + 0.5, floor.getY(), floor.getZ() + 12.5, 0.0f, 0.0f);
            far[0].setNoAi(true);
            level.addFreshEntity(far[0]);
            HeroLevel.set(player, new HeroLevel.Hero(HeroLevel.total(99), List.of(), 0));
            HeroPatCheck.locked(helper, player, wolf[0]);
            HeroLevel.set(player, new HeroLevel.Hero(HeroLevel.total(100), List.of(), 0));
            helper.assertValueEqual(HeroLevel.of(player).rank(), HeroLevel.Rank.LEGEND, "Rang auf Stufe 100");
            io.github.sirschrolli.kingdomsatwar.hero.HeroPat.forget(player);
            helper.assertValueEqual(io.github.sirschrolli.kingdomsatwar.hero.HeroPat.pat(player, wolf[0]),
                    io.github.sirschrolli.kingdomsatwar.hero.HeroPat.Result.PATTED, "Streicheln als Legende");
            helper.assertValueEqual(io.github.sirschrolli.kingdomsatwar.hero.HeroPat.pat(player, wolf[0]),
                    io.github.sirschrolli.kingdomsatwar.hero.HeroPat.Result.WAIT, "gleich noch einmal");
            io.github.sirschrolli.kingdomsatwar.hero.HeroPat.forget(player);
            helper.assertValueEqual(io.github.sirschrolli.kingdomsatwar.hero.HeroPat.pat(player, far[0]),
                    io.github.sirschrolli.kingdomsatwar.hero.HeroPat.Result.TOO_FAR, "Streicheln aus zwölf Blöcken");
            // ein eigener, verletzter Wolf: ein halbes Herz zurück, beim nächsten Streicheln gleich danach nicht noch eins
            wolf[0].tame(player);
            wolf[0].setHealth(wolf[0].getMaxHealth() - 6.0f);
            float before = wolf[0].getHealth();
            io.github.sirschrolli.kingdomsatwar.hero.HeroPat.forget(player);
            io.github.sirschrolli.kingdomsatwar.hero.HeroPat.pat(player, wolf[0]);
            helper.assertTrue(Math.abs(wolf[0].getHealth() - (before + io.github.sirschrolli.kingdomsatwar.hero.HeroPat.HEAL)) < 1.0e-4f,
                    "der eigene Wolf bekommt kein halbes Herz: " + before + " → " + wolf[0].getHealth());
            io.github.sirschrolli.kingdomsatwar.hero.HeroPat.forget(player);
            io.github.sirschrolli.kingdomsatwar.hero.HeroPat.pat(player, wolf[0]);
            helper.assertTrue(Math.abs(wolf[0].getHealth() - (before + io.github.sirschrolli.kingdomsatwar.hero.HeroPat.HEAL)) < 1.0e-4f,
                    "Leben bei jedem Streicheln");
            HeroLevel.set(player, HeroLevel.Hero.NONE);
            helper.succeed();
        } finally {
            if (wolf[0] != null) {
                wolf[0].discard();
            }
            if (far[0] != null) {
                far[0].discard();
            }
            level.getServer().getPlayerList().remove(player);
        }
    }

    /**
     * Mit Cheats: {@code /kaw hero level} springt auf eine Heldenstufe, {@code xp add} gibt Heldenerfahrung dazu,
     * {@code xp set} setzt sie, {@code reset} fängt von vorn an; die Erfahrung von Minecraft bleibt dabei, wie sie ist.
     */
    private static void command(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        try {
            HeroLevel.set(player, HeroLevel.Hero.NONE);
            int vanilla = player.totalExperience;
            // Der Testspieler gibt die Befehle selbst (mit Cheat-Rechten) – im großen Testlauf heißen viele Testspieler gleich.
            var source = player.createCommandSourceStack()
                    .withPermission(net.minecraft.server.permissions.LevelBasedPermissionSet.GAMEMASTER)
                    .withSuppressedOutput();
            run(helper, source, "kaw hero level 40");
            helper.assertValueEqual(HeroLevel.of(player).level(), 40, "Heldenstufe nach /kaw hero level 40");
            helper.assertValueEqual(HeroLevel.of(player).rank(), HeroLevel.Rank.KNIGHT, "Rang nach /kaw hero level 40");
            run(helper, source, "kaw hero xp add 1000");
            helper.assertValueEqual(HeroLevel.of(player).experience(), HeroLevel.total(40) + 1000, "Heldenerfahrung nach xp add");
            run(helper, source, "kaw hero xp set " + HeroLevel.total(100));
            helper.assertValueEqual(HeroLevel.of(player).level(), 100, "Heldenstufe nach xp set");
            run(helper, source, "kaw hero reset");
            helper.assertValueEqual(HeroLevel.of(player).experience(), 0, "Heldenerfahrung nach reset");
            helper.assertValueEqual(player.totalExperience, vanilla, "die Minecraft-Erfahrung darf sich nicht ändern");
            helper.succeed();
        } finally {
            level.getServer().getPlayerList().remove(player);
        }
    }

    /**
     * Führt einen Befehl sofort aus und meldet, warum er scheiterte. (Über {@code performPrefixedCommand} liefe er im großen
     * Testlauf womöglich erst später – wenn gerade schon ein Befehl läuft, reiht das Spiel neue nur ein – und Fehler gingen
     * stumm verloren.)
     */
    private static void run(GameTestHelper helper, net.minecraft.commands.CommandSourceStack source, String command) {
        int result;
        try {
            result = source.getServer().getCommands().getDispatcher().execute(command, source);
        } catch (com.mojang.brigadier.exceptions.CommandSyntaxException e) {
            throw helper.assertionException(net.minecraft.network.chat.Component.literal("/" + command + ": " + e.getMessage()));
        } catch (RuntimeException e) {
            throw helper.assertionException(net.minecraft.network.chat.Component.literal("/" + command + ": " + e));
        }
        helper.assertTrue(result > 0, "/" + command + " traf keinen Spieler");
    }

    /** Unter Stufe 100: Streicheln geht nicht. */
    private static final class HeroPatCheck {
        static void locked(GameTestHelper helper, ServerPlayer player, net.minecraft.world.entity.Entity target) {
            io.github.sirschrolli.kingdomsatwar.hero.HeroPat.forget(player);
            helper.assertValueEqual(io.github.sirschrolli.kingdomsatwar.hero.HeroPat.pat(player, target),
                    io.github.sirschrolli.kingdomsatwar.hero.HeroPat.Result.LOCKED, "Streicheln unter Stufe 100");
        }
    }

    private static ServerLevel level(GameTestHelper helper) {
        return helper.getLevel();
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
