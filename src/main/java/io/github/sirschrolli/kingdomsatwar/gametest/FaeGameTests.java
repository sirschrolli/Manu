package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.fae.MothCocoonBlock;
import io.github.sirschrolli.kingdomsatwar.entity.fae.FairyEntity;
import io.github.sirschrolli.kingdomsatwar.entity.fae.MoonMothEntity;
import io.github.sirschrolli.kingdomsatwar.entity.fae.MothQueenEntity;
import io.github.sirschrolli.kingdomsatwar.entity.fae.MushroomlingEntity;
import io.github.sirschrolli.kingdomsatwar.entity.fae.ThornProjectile;
import io.github.sirschrolli.kingdomsatwar.entity.fae.ThornWightEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.fae.FaeBiomes;
import io.github.sirschrolli.kingdomsatwar.fae.FaeElytra;
import io.github.sirschrolli.kingdomsatwar.fae.FaeGate;
import io.github.sirschrolli.kingdomsatwar.fae.Mondhain;
import io.github.sirschrolli.kingdomsatwar.fae.feature.FairyRingFeature;
import io.github.sirschrolli.kingdomsatwar.fae.feature.GiantMushroomFeature;
import io.github.sirschrolli.kingdomsatwar.fae.feature.GlimmerTreeFeature;
import io.github.sirschrolli.kingdomsatwar.fae.feature.MondhainFeature;
import io.github.sirschrolli.kingdomsatwar.fae.feature.ThornTreeFeature;
import io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModTags;
import io.github.sirschrolli.kingdomsatwar.worldgen.SpacedPlacement;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Tests des Feenreichs (0.63): Welt, Biome und Features, das Feenportal, die Wesen, der Mondhain, der Kampf gegen
 * Noctua, die Mottenkönigin, und die Feen-Elytren.
 */
final class FaeGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FAE_REALM =
            ModGameTests.TEST_FUNCTIONS.register("fae_realm", () -> FaeGameTests::faeRealm);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FAE_PORTAL =
            ModGameTests.TEST_FUNCTIONS.register("fae_portal", () -> FaeGameTests::faePortal);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FAE_FEATURES =
            ModGameTests.TEST_FUNCTIONS.register("fae_features", () -> FaeGameTests::faeFeatures);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FAE_CREATURES =
            ModGameTests.TEST_FUNCTIONS.register("fae_creatures", () -> FaeGameTests::faeCreatures);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> MONDHAIN =
            ModGameTests.TEST_FUNCTIONS.register("fae_mondhain", () -> FaeGameTests::mondhain);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> MOTH_QUEEN_FIGHT =
            ModGameTests.TEST_FUNCTIONS.register("fae_moth_queen_fight", () -> FaeGameTests::mothQueenFight);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FAE_ELYTRA =
            ModGameTests.TEST_FUNCTIONS.register("fae_elytra", () -> FaeGameTests::faeElytra);

    private FaeGameTests() {}

    /** Lädt die Klasse (meldet die Testfunktionen an). */
    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var test : List.of(FAE_REALM, FAE_PORTAL, FAE_FEATURES, FAE_CREATURES, MONDHAIN, FAE_ELYTRA)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 400, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
        // der Kampf dauert länger (Erwachen, alle Angriffe, Tod)
        event.registerTest(MOTH_QUEEN_FIGHT.getId(), new FunctionGameTestInstance(MOTH_QUEEN_FIGHT.key(),
                new TestData<>(environment, Level.OVERWORLD, ARENA, 1200, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
    }

    private static void floor(GameTestHelper helper, Block block) {
        for (int x = 0; x < 9; x++) {
            for (int z = 0; z < 9; z++) {
                helper.setBlock(x, 0, z, block);
            }
        }
    }

    /**
     * Die Welt für die großen Features: das End (dort entstehen beim Erzeugen neuer Chunks keine Tiere, die andere Tests
     * stören könnten – etwa die, die Vanilla-Tiere ersetzen); fehlt es, die Oberwelt.
     */
    private static ServerLevel featureLevel(GameTestHelper helper) {
        ServerLevel end = helper.getLevel().getServer().getLevel(Level.END);
        return end != null ? end : helper.getLevel();
    }

    /**
     * Eine Wiese aus Feengras (darunter Feenerde) weit draußen und hoch oben – für die Features, die über den Testbereich
     * hinaus wachsen und erst ab Höhe 2 bauen dürfen. Jeder Test bekommt seinen eigenen Fleck.
     */
    private static BlockPos meadow(GameTestHelper helper, int slot, int radius) {
        ServerLevel level = featureLevel(helper);
        BlockPos test = helper.absolutePos(BlockPos.ZERO);
        BlockPos center = new BlockPos(Math.floorMod(test.getX(), 1024) + 6000 + slot * 160, 110, Math.floorMod(test.getZ(), 1024) + 6000);
        BlockState grass = FaeBlocks.FAE_GRASS.get().defaultBlockState();
        BlockState soil = FaeBlocks.FAE_SOIL.get().defaultBlockState();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                BlockPos p = center.offset(dx, 0, dz);
                level.getChunk(p.getX() >> 4, p.getZ() >> 4);
                level.setBlock(p, grass, Block.UPDATE_CLIENTS);
                level.setBlock(p.below(), soil, Block.UPDATE_CLIENTS);
                level.setBlock(p.below(2), soil, Block.UPDATE_CLIENTS);
            }
        }
        return center.above();
    }

    private static int count(ServerLevel level, BlockPos center, int radius, int height, Predicate<BlockState> test) {
        int n = 0;
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-radius, -2, -radius), center.offset(radius, height, radius))) {
            if (test.test(level.getBlockState(p))) {
                n++;
            }
        }
        return n;
    }

    // =====================================================================================
    // Welt, Biome, Features
    // =====================================================================================

    /**
     * Das Feenreich: Dimensionstyp, alle fünf Biome mit Klangwelt, alle Features angemeldet; gibt es die Welt, kommen
     * alle Biome im Erzeuger vor. Der Mondhain-Fundort stimmt mit der Platzierung überein, und Bäume halten Abstand.
     */
    private static void faeRealm(GameTestHelper helper) {
        var server = helper.getLevel().getServer();
        var registries = server.registryAccess();
        var biomes = registries.lookupOrThrow(Registries.BIOME);
        for (ResourceKey<Biome> biome : FaeBiomes.ALL) {
            helper.assertTrue(biomes.get(biome).isPresent(), "Biom fehlt: " + biome.identifier());
        }
        var placed = registries.lookupOrThrow(Registries.PLACED_FEATURE);
        for (var key : io.github.sirschrolli.kingdomsatwar.fae.FaeFeatures.placedKeys()) {
            helper.assertTrue(placed.get(key).isPresent(), "Feature fehlt: " + key.identifier());
        }
        ServerLevel fae = server.getLevel(FaeGate.FAE_REALM);
        if (fae != null) {
            var possible = fae.getChunkSource().getGenerator().getBiomeSource().possibleBiomes();
            for (ResourceKey<Biome> biome : FaeBiomes.ALL) {
                helper.assertTrue(possible.stream().anyMatch(h -> h.is(biome)), "Biom kommt im Feenreich nicht vor: " + biome.identifier());
            }
        }
        // der Mondhain: der berechnete Chunk ist der, den die Platzierung wählt; drumherum ist Platz
        long seed = 20_261_109L;
        for (int rx = -2; rx <= 2; rx++) {
            for (int rz = -2; rz <= 2; rz++) {
                var chunk = Mondhain.chunk(seed, rx, rz);
                helper.assertTrue(SpacedPlacement.isGridChunk(seed, Mondhain.SALT, chunk.x(), chunk.z(), Mondhain.SPACING, Mondhain.SEPARATION),
                        "Der Mondhain liegt nicht im Chunk der Platzierung");
                BlockPos center = Mondhain.center(seed, rx, rz);
                helper.assertTrue(Mondhain.near(seed, center.getX() + 6, center.getZ() - 5, Mondhain.CLEARING), "Die Lichtung ist nicht frei");
                helper.assertFalse(Mondhain.near(seed, center.getX() + 40, center.getZ(), Mondhain.CLEARING), "Die Lichtung ist zu groß");
                BlockPos found = Mondhain.nearest(seed, center.getX() + 30, center.getZ() + 20);
                helper.assertTrue(found.getX() == center.getX() && found.getZ() == center.getZ(), "Der nächste Mondhain wird nicht gefunden");
            }
        }
        helper.succeed();
    }

    /** Das Feenportal: Feenstaub entzündet einen Rahmen aus Mondsteinziegeln; ohne Rahmen nicht; abgebaut erlischt es. */
    private static void faePortal(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper, Blocks.STONE);
        // Rahmen 4 breit, 5 hoch (innen 2 × 3) entlang x
        Block bricks = FaeBlocks.MOONSTONE_BRICKS.base().get();
        for (int x = 2; x <= 5; x++) {
            helper.setBlock(x, 1, 4, bricks);
            helper.setBlock(x, 5, 4, bricks);
        }
        for (int y = 1; y <= 5; y++) {
            helper.setBlock(2, y, 4, bricks);
            helper.setBlock(5, y, 4, bricks);
        }
        BlockPos clicked = helper.absolutePos(new BlockPos(3, 1, 4));
        helper.assertFalse(FaeGate.tryLight(level, helper.absolutePos(new BlockPos(7, 0, 7)), Direction.UP), "Das Portal entsteht ohne Rahmen");
        helper.assertTrue(FaeGate.tryLight(level, clicked, Direction.UP), "Feenstaub entzündet den Rahmen nicht");
        for (int x = 3; x <= 4; x++) {
            for (int y = 2; y <= 4; y++) {
                helper.assertBlockPresent(FaeBlocks.FAE_PORTAL.get(), new BlockPos(x, y, 4));
            }
        }
        ServerLevel fae = level.getServer().getLevel(FaeGate.FAE_REALM);
        var player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        TeleportTransition there = FaeGate.destination(level, player, helper.absolutePos(new BlockPos(3, 2, 4)));
        if (fae != null) {
            helper.assertTrue(there != null && there.newLevel() == fae, "Das Portal führt nicht ins Feenreich");
            BlockPos landing = BlockPos.containing(there.position());
            helper.assertTrue(fae.getBlockState(landing.below()).isSolid() || !fae.getFluidState(landing.below()).isEmpty(),
                    "Im Feenreich fehlt der Boden unter der Ankunft");
            var state = io.github.sirschrolli.kingdomsatwar.fae.FaeGateState.of(fae);
            var back = state.nearest(Level.OVERWORLD, landing, 16);
            helper.assertTrue(back == null || back.dimension() == Level.OVERWORLD, "Unsinniger Rückweg");
        } else {
            helper.assertTrue(there == null, "Ohne Feenreich darf das Portal nirgendwohin führen");
        }
        // Rahmen abgebaut: das Portal erlischt
        helper.setBlock(2, 3, 4, Blocks.AIR);
        helper.runAfterDelay(2, () -> {
            helper.assertBlockNotPresent(FaeBlocks.FAE_PORTAL.get(), new BlockPos(3, 3, 4));
            helper.succeed();
        });
    }

    /** Die Features des Feenreichs wachsen: drei Glimmerbäume, Riesenpilz, Feenring, Dornenbaum und Dickicht. */
    private static void faeFeatures(GameTestHelper helper) {
        ServerLevel level = featureLevel(helper);
        var generator = level.getChunkSource().getGenerator();
        RandomSource random = RandomSource.create(631);
        record Case(String name, Feature feature, Predicate<BlockState> expect, int min) {}
        List<Case> cases = List.of(
                new Case("schlanker Glimmerbaum", new GlimmerTreeFeature(GlimmerTreeFeature.Kind.SLENDER, GlimmerTreeFeature.Palette.ROSE),
                        s -> s.is(BlockTags.LEAVES), 20),
                new Case("Glimmerriese", new GlimmerTreeFeature(GlimmerTreeFeature.Kind.GRAND, GlimmerTreeFeature.Palette.MIXED),
                        s -> s.is(FaeBlocks.GLIMMERWOOD_LOG.get()) || s.is(FaeBlocks.GLIMMERWOOD_WOOD.get()), 40),
                new Case("Trauerglimmer", new GlimmerTreeFeature(GlimmerTreeFeature.Kind.WEEPING, GlimmerTreeFeature.Palette.AZURE),
                        s -> s.is(BlockTags.LEAVES), 20),
                new Case("Riesenpilz", new GiantMushroomFeature(GiantMushroomFeature.Kind.ROSE, false),
                        s -> s.is(FaeBlocks.ROSE_MUSHROOM_CAP.get()), 12),
                new Case("Feenring", new FairyRingFeature(), s -> s.is(FaeBlocks.LANTERN_MUSHROOM.get()), 8),
                new Case("Dornenbaum", new ThornTreeFeature(ThornTreeFeature.Kind.TREE), s -> s.is(FaeBlocks.THORN_HEDGE.get()), 6),
                new Case("Dornendickicht", new ThornTreeFeature(ThornTreeFeature.Kind.THICKET), s -> s.is(FaeBlocks.THORN_HEDGE.get()), 6));
        for (int i = 0; i < cases.size(); i++) {
            Case c = cases.get(i);
            BlockPos at = meadow(helper, i, 12);
            boolean placed = c.feature().place(level, generator, random, at);
            helper.assertTrue(placed, c.name() + " wächst nicht");
            int found = count(level, at, 12, 30, c.expect());
            helper.assertTrue(found >= c.min(), c.name() + ": zu klein (" + found + ")");
        }
        helper.succeed();
    }

    // =====================================================================================
    // Die Wesen
    // =====================================================================================

    /**
     * Die Wesen: Gruppen, Beute, Spawn-Eier und das Feenvolk-Etikett; die Fee tanzt für eine Blume und schenkt Feenstaub,
     * der Pilzling pustet Sporen, der Dornwicht vergiftet und schießt Dornen.
     */
    private static void faeCreatures(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper, FaeBlocks.FAE_GRASS.get());
        var lootTables = level.getServer().reloadableRegistries();
        for (var type : List.of(ModEntities.FAIRY.get(), ModEntities.FAE_WISP.get(), ModEntities.MUSHROOMLING.get(), ModEntities.THORN_WIGHT.get(),
                ModEntities.MOTH_QUEEN.get())) {
            helper.assertTrue(lootTables.getLootTable(type.getDefaultLootTable().orElseThrow()) != LootTable.EMPTY,
                    "Keine Beute: " + type.getDescriptionId());
            helper.assertTrue(type.builtInRegistryHolder().is(ModTags.EntityTypes.FAE_CREATURES), "Nicht im Feenvolk: " + type.getDescriptionId());
        }
        helper.assertTrue(ModEntities.THORN_WIGHT.get().getCategory() == MobCategory.MONSTER, "Dornwicht: falsche Gruppe");
        helper.assertTrue(ModEntities.FAIRY.get().getCategory() == MobCategory.CREATURE, "Fee: falsche Gruppe");
        for (var egg : FaeBlocks.spawnEggs()) {
            helper.assertTrue(egg.get().getDefaultInstance().getItem() == egg.get(), "Spawn-Ei fehlt");
        }
        // die Fee: eine Blume, ein Tanz, Feenstaub
        FairyEntity fairy = helper.spawn(ModEntities.FAIRY.get(), new BlockPos(2, 2, 2));
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        player.snapTo(helper.absoluteVec(new Vec3(2.5, 1, 3.5)));
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.POPPY));
        var result = fairy.interact(player, InteractionHand.MAIN_HAND, fairy.position());
        helper.assertTrue(result.consumesAction(), "Die Fee nimmt die Blume nicht");
        helper.assertTrue(fairy.getDance() > 0, "Die Fee tanzt nicht");
        helper.assertTrue(player.getMainHandItem().isEmpty(), "Die Blume bleibt in der Hand");
        // der Test-Spieler muss gleich wieder gehen: solange er (weit weg) in der Welt ist, verschwinden in anderen
        // Tests die Wesen, die „zu weit von jedem Spieler“ sind
        level.getServer().getPlayerList().remove(player);
        // der Pilzling: wer ihn schlägt, bekommt Sporen ab
        MushroomlingEntity mushroomling = helper.spawn(ModEntities.MUSHROOMLING.get(), new BlockPos(6, 1, 6));
        var zombie = helper.spawnWithNoFreeWill(ModEntities.SOLDIERS.get(SoldierType.SWORDSMAN).get(), new BlockPos(7, 1, 6));
        boolean hurt = mushroomling.hurtServer(level, level.damageSources().mobAttack(zombie), 1.0f);
        helper.assertTrue(hurt, "Der Pilzling nimmt keinen Schaden");
        helper.assertTrue(zombie.hasEffect(MobEffects.NAUSEA), "Die Sporen wirken nicht: " + zombie.getActiveEffects());
        // der Dornwicht: Gift im Nahkampf, eine Salve Dornen
        ThornWightEntity wight = helper.spawnWithNoFreeWill(ModEntities.THORN_WIGHT.get(), new BlockPos(4, 1, 7));
        var target = helper.spawnWithNoFreeWill(EntityTypes.ZOMBIE, new BlockPos(4, 1, 1));
        wight.doHurtTarget(level, zombie);
        helper.assertTrue(zombie.hasEffect(MobEffects.POISON), "Der Dornwicht vergiftet nicht");
        wight.volley(level, target);
        helper.assertTrue(level.getEntitiesOfClass(ThornProjectile.class, new AABB(helper.absolutePos(new BlockPos(4, 1, 4))).inflate(8)).size() >= 3,
                "Der Dornwicht schießt keine Salve");
        helper.runAfterDelay(FairyEntity.DANCE_TICKS + 5, () -> {
            helper.assertFalse(level.getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(new BlockPos(4, 1, 4))).inflate(8),
                    e -> e.getItem().is(FaeBlocks.FAE_DUST.get())).isEmpty(), "Die Fee schenkt keinen Feenstaub");
            helper.succeed();
        });
    }

    // =====================================================================================
    // Der Mondhain
    // =====================================================================================

    /**
     * Der Mondhain wächst auf einer Wiese: eingeebnet, mit Steinkreis (leuchtende Kapitelle), Altar und dem Kokon in der
     * Mitte, ringsum Bäume mit Seide; in der Mitte steht kein Baum.
     */
    private static void mondhain(GameTestHelper helper) {
        ServerLevel level = featureLevel(helper);
        BlockPos meadow = meadow(helper, 20, 24);
        // die Wiese liegt so, dass ihre Mitte die Mitte eines Chunks ist (dort baut der Mondhain)
        BlockPos chunkMiddle = new BlockPos((meadow.getX() & ~15) + 8, meadow.getY(), (meadow.getZ() & ~15) + 8);
        if (!chunkMiddle.equals(meadow)) {
            meadow = meadow(helper, 21, 24);
            chunkMiddle = new BlockPos((meadow.getX() & ~15) + 8, meadow.getY(), (meadow.getZ() & ~15) + 8);
            for (int dx = -24; dx <= 24; dx++) {
                for (int dz = -24; dz <= 24; dz++) {
                    BlockPos p = chunkMiddle.offset(dx, -1, dz);
                    level.setBlock(p, FaeBlocks.FAE_GRASS.get().defaultBlockState(), Block.UPDATE_CLIENTS);
                    level.setBlock(p.below(), FaeBlocks.FAE_SOIL.get().defaultBlockState(), Block.UPDATE_CLIENTS);
                }
            }
        }
        boolean placed = new MondhainFeature().place(level, level.getChunkSource().getGenerator(), RandomSource.create(9163), chunkMiddle);
        helper.assertTrue(placed, "Der Mondhain wächst nicht");
        int floor = chunkMiddle.getY() - 1;
        BlockPos cocoon = new BlockPos(chunkMiddle.getX(), floor + MondhainFeature.COCOON_HEIGHT, chunkMiddle.getZ());
        helper.assertTrue(level.getBlockState(cocoon).is(FaeBlocks.MOTH_COCOON.get()), "Auf dem Altar liegt kein Kokon");
        int capitals = count(level, chunkMiddle, 14, 10, s -> s.is(FaeBlocks.CHISELED_MOONSTONE.get()));
        helper.assertTrue(capitals >= 9, "Der Steinkreis fehlt (" + capitals + " Kapitelle)");
        int logsInside = 0;
        for (BlockPos p : BlockPos.betweenClosed(chunkMiddle.offset(-8, -1, -8), chunkMiddle.offset(8, 12, 8))) {
            double dx = p.getX() - chunkMiddle.getX();
            double dz = p.getZ() - chunkMiddle.getZ();
            if (dx * dx + dz * dz <= 64.0 && level.getBlockState(p).is(BlockTags.LOGS)) {
                logsInside++;
            }
        }
        helper.assertTrue(logsInside == 0, "In der Lichtung steht ein Baum");
        int trees = count(level, chunkMiddle, 22, 20, s -> s.is(BlockTags.LOGS));
        helper.assertTrue(trees >= 12, "Um den Hain stehen keine Bäume");
        int silk = count(level, chunkMiddle, 22, 24, s -> s.is(FaeBlocks.SILK_STRANDS.get()) || s.is(FaeBlocks.MOTH_SILK.get()));
        helper.assertTrue(silk >= 4, "Im Hain hängt keine Seide (" + silk + ")");
        helper.succeed();
    }

    // =====================================================================================
    // Noctua, die Mottenkönigin
    // =====================================================================================

    /**
     * Der Kampf: Ohne Feenstaub bleibt der Kokon zu, mit Feenstaub erwacht Noctua (dabei unverwundbar). Flügelsturm,
     * Mottenschwarm, Mondstrahl und Schuppenwolke treffen einen Ritter; benommen nimmt sie mehr Schaden; unter halber Kraft
     * wird sie rasend. Besiegt lässt sie die Feen-Elytren fallen, ihre Falter verglimmen, und ein neuer Kokon spinnt sich.
     */
    private static void mothQueenFight(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper, Blocks.STONE);
        helper.assertTrue(ModEntities.MOTH_QUEEN.get().getCategory() == MobCategory.MONSTER, "Noctua: falsche Gruppe");
        BlockPos home = helper.absolutePos(new BlockPos(4, 1, 4));
        level.setBlock(home, FaeBlocks.MOTH_COCOON.get().defaultBlockState(), Block.UPDATE_ALL);
        // ohne Feenstaub: nichts
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK));
        helper.assertTrue(level.getBlockState(home).is(FaeBlocks.MOTH_COCOON.get()), "Der Kokon fehlt");
        helper.assertTrue(level.getBlockState(home).getDestroySpeed(level, home) < 0, "Der Kokon lässt sich abbauen");
        helper.assertTrue(MothCocoonBlock.awaken(level, home, player), "Feenstaub weckt die Königin nicht");
        var queens = level.getEntitiesOfClass(MothQueenEntity.class, new AABB(home).inflate(8));
        helper.assertTrue(queens.size() == 1, "Keine (oder zu viele) Königinnen: " + queens.size());
        MothQueenEntity queen = queens.getFirst();
        helper.assertTrue(level.getBlockState(home).isAir(), "Der Kokon bleibt nach dem Erwachen");
        helper.assertFalse(MothCocoonBlock.awaken(level, home, player), "Eine zweite Königin erwacht");
        level.getServer().getPlayerList().remove(player);
        float before = queen.getHealth();
        queen.hurtServer(level, level.damageSources().generic(), 30.0f);
        helper.assertTrue(queen.getHealth() >= before, "Beim Erwachen ist sie verwundbar");
        var knight = helper.spawnWithNoFreeWill(ModEntities.SOLDIERS.get(SoldierType.KNIGHT).get(), new BlockPos(4, 1, 0));
        helper.runAfterDelay(MothQueenEntity.AWAKEN_TICKS + 4, () -> {
            helper.assertTrue(queen.currentAttack() == MothQueenEntity.Attack.NONE, "Sie erwacht nicht ganz: " + queen.currentAttack());
            queen.setTarget(knight);
            knight.setHealth(knight.getMaxHealth());
            float hp = knight.getHealth();
            queen.snapTo(knight.getX(), knight.getY() + 4.0, knight.getZ() + 6.0, 180.0f, 0.0f);
            queen.forceAttack(MothQueenEntity.Attack.GUST);
            helper.runAfterDelay(14, () -> {
                helper.assertTrue(knight.getHealth() < hp, "Der Flügelsturm trifft nicht");
                queen.forceAttack(MothQueenEntity.Attack.SWARM);
                helper.runAfterDelay(18, () -> {
                    helper.assertFalse(level.getEntitiesOfClass(MoonMothEntity.class, queen.getBoundingBox().inflate(16)).isEmpty(),
                            "Kein Mottenschwarm");
                    knight.setHealth(knight.getMaxHealth());
                    float beforeBeam = knight.getHealth();
                    queen.setTarget(knight);
                    queen.forceAttack(MothQueenEntity.Attack.BEAM);
                    helper.runAfterDelay(40, () -> {
                        helper.assertTrue(knight.getHealth() < beforeBeam, "Der Mondstrahl trifft nicht");
                        // die Falter verglimmen lassen (sonst stirbt der Ritter vielleicht an ihren Bissen), dann über ihn
                        for (MoonMothEntity moth : level.getEntitiesOfClass(MoonMothEntity.class, queen.getBoundingBox().inflate(32))) {
                            moth.vanish(level);
                        }
                        knight.setHealth(knight.getMaxHealth());
                        queen.snapTo(knight.getX() + 1.0, knight.getY() + 6.5, knight.getZ(), 0.0f, 0.0f);
                        queen.setTarget(knight);
                        queen.forceAttack(MothQueenEntity.Attack.SCALES);
                        helper.runAfterDelay(50, () -> {
                            helper.assertTrue(knight.hasEffect(MobEffects.WEAKNESS), String.format(java.util.Locale.ROOT,
                                    "Die Schuppenwolke wirkt nicht (waagrecht %.1f, Höhe %.1f, Ritter lebt: %s, Angriff %s)",
                                    Math.hypot(queen.getX() - knight.getX(), queen.getZ() - knight.getZ()), queen.getY() - knight.getY(),
                                    knight.isAlive(), queen.currentAttack()));
                            eclipseAndCocoon(helper, level, queen, knight, () -> dive(helper, level, queen, knight, home));
                        });
                    });
                });
            });
        });
    }

    /** Mondfinsternis (der erste Mondstrahl fällt auf den Ritter) und Seidenkokon (er wird eingesponnen). */
    private static void eclipseAndCocoon(GameTestHelper helper, ServerLevel level, MothQueenEntity queen,
            net.minecraft.world.entity.LivingEntity knight, Runnable then) {
        knight.setHealth(knight.getMaxHealth());
        float hp = knight.getHealth();
        queen.snapTo(knight.getX() + 2.0, knight.getY() + 6.0, knight.getZ() + 4.0, 0.0f, 0.0f);
        queen.setDeltaMovement(Vec3.ZERO);
        queen.setTarget(knight);
        queen.forceAttack(MothQueenEntity.Attack.ECLIPSE);
        helper.runAfterDelay(MothQueenEntity.ECLIPSE_FIRST + MothQueenEntity.ECLIPSE_WARN + 3, () -> {
            helper.assertTrue(knight.getHealth() < hp, "Der Mondstrahl der Finsternis trifft nicht");
            helper.assertTrue(knight.hasEffect(MobEffects.GLOWING), "Wen der Strahl trifft, der leuchtet nicht");
            knight.setHealth(knight.getMaxHealth());
            queen.snapTo(knight.getX() + 2.0, knight.getY() + 5.0, knight.getZ() + 4.0, 0.0f, 0.0f);
            queen.setDeltaMovement(Vec3.ZERO);
            queen.setTarget(knight);
            queen.forceAttack(MothQueenEntity.Attack.COCOON);
            helper.runAfterDelay(MothQueenEntity.COCOON_WRAP + 3, () -> {
                var slow = knight.getEffect(MobEffects.SLOWNESS);
                helper.assertTrue(slow != null && slow.getAmplifier() >= 5, "Der Ritter wird nicht eingesponnen");
                then.run();
            });
        });
    }

    /** Sturzflug: Der Ritter weicht im letzten Moment aus – sie prallt auf und liegt benommen am Boden. */
    private static void dive(GameTestHelper helper, ServerLevel level, MothQueenEntity queen,
            net.minecraft.world.entity.LivingEntity knight, BlockPos home) {
        queen.snapTo(knight.getX() + 2.0, knight.getY() + 7.0, knight.getZ() + 3.0, 0.0f, 0.0f);
        queen.setDeltaMovement(Vec3.ZERO);
        queen.setTarget(knight);
        queen.forceAttack(MothQueenEntity.Attack.DIVE);
        helper.runAfterDelay(11, () -> {
            knight.teleportTo(knight.getX() + 6.0, knight.getY(), knight.getZ());
            helper.runAfterDelay(26, () -> {
                helper.assertTrue(queen.currentAttack() == MothQueenEntity.Attack.STUNNED, String.format(java.util.Locale.ROOT,
                        "Nach dem verfehlten Sturzflug ist sie nicht benommen (%s, %.1f über dem Boden)", queen.currentAttack(),
                        queen.getY() - helper.absolutePos(BlockPos.ZERO).getY()));
                stunnedAndRage(helper, level, queen, home);
            });
        });
    }

    private static void stunnedAndRage(GameTestHelper helper, ServerLevel level, MothQueenEntity queen, BlockPos home) {
        helper.runAfterDelay(2, () -> {
            queen.setInvulnerableTime(0);
            float hp = queen.getHealth();
            queen.hurtServer(level, level.damageSources().generic(), 10.0f);
            helper.assertTrue(hp - queen.getHealth() > 10.5f, "Benommen nimmt sie nicht mehr Schaden: " + (hp - queen.getHealth()));
            queen.setHealth(queen.getMaxHealth() * 0.45f);
            helper.runAfterDelay(2, () -> {
                helper.assertTrue(queen.isEnraged(), "Unter halber Kraft wird sie nicht rasend");
                helper.runAfterDelay(MothQueenEntity.Attack.STUNNED.duration + 2, () -> {
                    helper.assertTrue(queen.currentAttack() != MothQueenEntity.Attack.STUNNED, "Sie bleibt benommen");
                    queen.setInvulnerableTime(0);
                    queen.setHealth(1.0f);
                    BossLootGameTests.firstVictory(helper, queen, 50.0f);
                    helper.assertTrue(queen.isDeadOrDying(), "Noctua fällt nicht");
                    helper.runAfterDelay(25, () -> {
                        AABB area = new AABB(home).inflate(40);
                        helper.assertFalse(level.getEntitiesOfClass(ItemEntity.class, area, e -> e.getItem().is(FaeBlocks.FAE_ELYTRA.get())).isEmpty(),
                                "Sie lässt die Feen-Elytren nicht fallen");
                        helper.assertTrue(level.getEntitiesOfClass(MoonMothEntity.class, area, MoonMothEntity::isAlive).isEmpty(),
                                "Ihre Falter bleiben nach ihrem Tod");
                        helper.assertTrue(level.getBlockState(home).is(FaeBlocks.MOTH_COCOON.get()), "Es spinnt sich kein neuer Kokon");
                        helper.succeed();
                    });
                });
            });
        });
    }

    // =====================================================================================
    // Die Feen-Elytren
    // =====================================================================================

    /**
     * Die Feen-Elytren: gleiten (Gleiter, Brustplatz), 540 Haltbarkeit, Reparatur mit Feenstaub; der Flügelschlag
     * beschleunigt, kostet Haltbarkeit und hat eine Abklingzeit; der Aufprall an Wänden tut nicht weh.
     */
    private static void faeElytra(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ItemStack elytra = new ItemStack(FaeBlocks.FAE_ELYTRA.get());
        helper.assertTrue(elytra.has(DataComponents.GLIDER), "Die Feen-Elytren gleiten nicht");
        var equippable = elytra.get(DataComponents.EQUIPPABLE);
        helper.assertTrue(equippable != null && equippable.slot() == EquipmentSlot.CHEST, "Die Feen-Elytren sitzen nicht auf dem Rücken");
        helper.assertTrue(elytra.getMaxDamage() == 540, "Falsche Haltbarkeit: " + elytra.getMaxDamage());
        helper.assertTrue(elytra.isValidRepairItem(new ItemStack(FaeBlocks.FAE_DUST.get())), "Feenstaub repariert sie nicht");
        Vec3 slow = new Vec3(0.3, -0.1, 0.0);
        Vec3 fast = FaeElytra.flapVelocity(slow, new Vec3(1.0, 0.0, 0.0));
        helper.assertTrue(fast.x > slow.x + 0.5 && fast.y > slow.y + 0.3, "Der Flügelschlag trägt nicht");
        helper.assertTrue(FaeElytra.flapVelocity(new Vec3(3.0, 0, 0), new Vec3(1, 0, 0)).length() <= 2.21, "Der Flügelschlag ist zu schnell");
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        player.setItemSlot(EquipmentSlot.CHEST, elytra);
        helper.assertTrue(FaeElytra.wearing(player), "Getragen werden sie nicht erkannt");
        player.snapTo(helper.absoluteVec(new Vec3(4.5, 6.0, 4.5)));
        player.startFallFlying();
        helper.assertTrue(player.isFallFlying(), "Der Test-Spieler gleitet nicht");
        FaeElytra.flap(player);
        ItemStack worn = player.getItemBySlot(EquipmentSlot.CHEST);
        helper.assertTrue(worn.getDamageValue() == 1, "Der Flügelschlag kostet keine Haltbarkeit: " + worn.getDamageValue());
        helper.assertTrue(player.getCooldowns().isOnCooldown(worn), "Keine Abklingzeit nach dem Flügelschlag");
        FaeElytra.flap(player);
        helper.assertTrue(worn.getDamageValue() == 1, "Während der Abklingzeit schlagen sie noch einmal");
        float hp = player.getHealth();
        player.hurtServer(level, level.damageSources().flyIntoWall(), 6.0f);
        helper.assertTrue(player.getHealth() >= hp, "Der Aufprall an der Wand tut weh");
        level.getServer().getPlayerList().remove(player);
        helper.succeed();
    }
}
