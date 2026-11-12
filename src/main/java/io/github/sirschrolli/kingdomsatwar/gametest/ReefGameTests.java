package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.sea.AtollCoreBlockEntity;
import io.github.sirschrolli.kingdomsatwar.block.sea.SeashellBlock;
import io.github.sirschrolli.kingdomsatwar.entity.sea.GiantCrabEntity;
import io.github.sirschrolli.kingdomsatwar.entity.sea.ReefColossusEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.item.sea.TideSpearItem;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModTags;
import io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks;
import io.github.sirschrolli.kingdomsatwar.worldgen.atoll.AtollLocator;
import io.github.sirschrolli.kingdomsatwar.worldgen.atoll.AtollPiece;
import io.github.sirschrolli.kingdomsatwar.worldgen.atoll.AtollPlacement;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures;
import io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature.AnemoneGardenFeature;
import io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature.KelpForestFeature;
import io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature.ShellBedFeature;
import java.util.List;
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
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BubbleColumnBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Tests des Meeresgrunds und des Versunkenen Atolls (0.62): die neuen Blöcke, die Landschaften des Meeresgrunds, das
 * Atoll als Bauwerk, der Kampf gegen Karkinos und der Gezeiten-Speer.
 */
final class ReefGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SEA_FLOOR_BLOCKS =
            ModGameTests.TEST_FUNCTIONS.register("sea_floor_blocks", () -> ReefGameTests::seaFloorBlocks);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SEA_FLOOR_FEATURES =
            ModGameTests.TEST_FUNCTIONS.register("sea_floor_features", () -> ReefGameTests::seaFloorFeatures);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SUNKEN_ATOLL =
            ModGameTests.TEST_FUNCTIONS.register("sunken_atoll", () -> ReefGameTests::sunkenAtoll);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> REEF_COLOSSUS_FIGHT =
            ModGameTests.TEST_FUNCTIONS.register("reef_colossus_fight", () -> ReefGameTests::reefColossusFight);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TIDE_SPEAR =
            ModGameTests.TEST_FUNCTIONS.register("tide_spear", () -> ReefGameTests::tideSpear);

    private ReefGameTests() {}

    /** Lädt die Klasse (meldet die Testfunktionen an). */
    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var test : List.of(SEA_FLOOR_BLOCKS, SEA_FLOOR_FEATURES, SUNKEN_ATOLL, TIDE_SPEAR)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 400, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
        // der Kampf dauert länger (Erwachen, acht Angriffe, Tod, Rückruf)
        event.registerTest(REEF_COLOSSUS_FIGHT.getId(), new FunctionGameTestInstance(REEF_COLOSSUS_FIGHT.key(),
                new TestData<>(environment, Level.OVERWORLD, ARENA, 1000, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
    }

    /** Ein Becken: Boden aus {@code floor}, Glasrand, Wasser bis zur vierten Schicht (darüber Luft). */
    private static void pool(GameTestHelper helper, Block floor) {
        for (int x = 0; x < 9; x++) {
            for (int z = 0; z < 9; z++) {
                helper.setBlock(x, 0, z, floor);
                boolean rim = x == 0 || z == 0 || x == 8 || z == 8;
                for (int y = 1; y <= 4; y++) {
                    helper.setBlock(x, y, z, rim ? Blocks.GLASS : Blocks.WATER);
                }
            }
        }
    }

    private static int count(GameTestHelper helper, java.util.function.Predicate<BlockState> test) {
        int n = 0;
        for (int x = 0; x < 9; x++) {
            for (int z = 0; z < 9; z++) {
                for (int y = 1; y <= 5; y++) {
                    if (test.test(helper.getBlockState(new BlockPos(x, y, z)))) {
                        n++;
                    }
                }
            }
        }
        return n;
    }

    /**
     * Die neuen Blöcke des Meeresgrunds: Anemonen halten Wasser, leuchten und vergehen ohne Grund; Muscheln liegen nass wie
     * trocken und geben so viele Schalen, wie daliegen; eine heiße Quelle treibt eine Blasensäule nach oben; Riesentang
     * wächst zur Oberfläche; Leuchtalgen leuchten; alle haben Beute.
     */
    private static void seaFloorBlocks(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        pool(helper, Blocks.SAND);
        BlockPos anemone = new BlockPos(2, 1, 2);
        helper.setBlock(anemone, SeaBlocks.PURPLE_SEA_ANEMONE.get());
        BlockState a = helper.getBlockState(anemone);
        helper.assertTrue(a.getFluidState().is(FluidTags.WATER), "Die Anemone hält kein Wasser");
        helper.assertTrue(a.getLightEmission() > 0, "Die Anemone leuchtet nicht");
        helper.setBlock(new BlockPos(2, 0, 2), Blocks.WATER);
        helper.assertTrue(helper.getBlockState(anemone).is(Blocks.WATER), "Die Anemone bleibt ohne Grund stehen");

        BlockPos shells = new BlockPos(6, 1, 2);
        helper.setBlock(shells, SeaBlocks.SCALLOP_SHELLS.get().defaultBlockState().setValue(SeashellBlock.SHELLS, 3)
                .setValue(SeashellBlock.WATERLOGGED, true));
        BlockState sh = helper.getBlockState(shells);
        helper.assertTrue(sh.getFluidState().is(FluidTags.WATER), "Die Muscheln liegen nicht im Wasser");
        int dropped = Block.getDrops(sh, level, helper.absolutePos(shells), null).stream()
                .filter(stack -> stack.is(SeaBlocks.SCALLOP_SHELLS.get().asItem())).mapToInt(ItemStack::getCount).sum();
        helper.assertTrue(dropped == 3, "Drei Muscheln geben " + dropped + " Muscheln");
        helper.assertFalse(sh.canSurvive(level, helper.absolutePos(new BlockPos(6, 3, 6))), "Muscheln schweben im Wasser");

        BlockPos glow = new BlockPos(2, 1, 6);
        helper.setBlock(glow, SeaBlocks.GLOW_ALGAE.get());
        helper.assertTrue(helper.getBlockState(glow).getLightEmission() > 0, "Leuchtalgen leuchten nicht");

        for (var block : List.of(SeaBlocks.PURPLE_SEA_ANEMONE, SeaBlocks.RED_SEA_ANEMONE, SeaBlocks.GREEN_SEA_ANEMONE,
                SeaBlocks.ORANGE_SEA_ANEMONE, SeaBlocks.COCKLE_SHELLS, SeaBlocks.SCALLOP_SHELLS, SeaBlocks.WHELK_SHELLS)) {
            var key = block.get().getLootTable().orElseThrow();
            helper.assertTrue(level.getServer().reloadableRegistries().getLootTable(key) != LootTable.EMPTY, "Beute fehlt: " + key.identifier());
        }
        for (Block block : List.of(SeaBlocks.TUBE_WORMS.get(), SeaBlocks.VENT_ROCK.get(), SeaBlocks.SULFUR_CRUST.get(),
                SeaBlocks.HYDROTHERMAL_VENT.get(), SeaBlocks.GLOW_ALGAE.get(), SeaBlocks.GIANT_KELP.get())) {
            var key = block.getLootTable().orElseThrow();
            helper.assertTrue(level.getServer().reloadableRegistries().getLootTable(key) != LootTable.EMPTY, "Beute fehlt: " + key.identifier());
        }

        // heiße Quelle: die Blasen steigen
        helper.setBlock(new BlockPos(6, 0, 6), SeaBlocks.HYDROTHERMAL_VENT.get());
        helper.assertTrue(helper.getBlockState(new BlockPos(6, 0, 6)).getLightEmission() > 0, "Die heiße Quelle glüht nicht");

        // Riesentang wächst bis knapp unter die Oberfläche
        BlockPos kelp = new BlockPos(4, 1, 4);
        helper.setBlock(kelp, SeaBlocks.GIANT_KELP.get());
        RandomSource random = RandomSource.create(5);
        for (int i = 0; i < 400; i++) {
            for (int y = 4; y >= 1; y--) {
                BlockPos at = helper.absolutePos(new BlockPos(4, y, 4));
                BlockState state = level.getBlockState(at);
                if (state.is(SeaBlocks.GIANT_KELP.get())) {
                    state.randomTick(level, at, random);
                    break;
                }
            }
        }
        helper.runAfterDelay(26, () -> {
            BlockState column = helper.getBlockState(new BlockPos(6, 1, 6));
            helper.assertTrue(column.is(Blocks.BUBBLE_COLUMN) && !column.getValue(BubbleColumnBlock.DRAG_DOWN),
                    "Über der heißen Quelle steigt keine Blasensäule auf: " + column);
            helper.assertTrue(helper.getBlockState(kelp).is(SeaBlocks.GIANT_KELP_PLANT.get()), "Der Riesentang wächst nicht");
            helper.assertTrue(helper.getBlockState(new BlockPos(4, 3, 4)).is(SeaBlocks.GIANT_KELP.get())
                            || helper.getBlockState(new BlockPos(4, 3, 4)).is(SeaBlocks.GIANT_KELP_PLANT.get()),
                    "Der Riesentang wird nicht hoch genug");
            helper.succeed();
        });
    }

    /**
     * Die Landschaften des Meeresgrunds: eine Muschelbank liegt im seichten Wasser, nicht in der Tiefe; ein Tangwald setzt
     * Tang (auch Riesentang), ein Anemonengarten Anemonen; in den Meeren der Oberwelt kommt jede Landschaft vor.
     */
    private static void seaFloorFeatures(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        pool(helper, Blocks.SAND);
        var generator = level.getChunkSource().getGenerator();
        BlockPos center = helper.absolutePos(new BlockPos(4, 1, 4));
        int surface = helper.absolutePos(new BlockPos(0, 5, 0)).getY();
        try {
            // tief unter dem Meeresspiegel keine Muscheln, im Seichten schon
            io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature.SeaLevel.override(surface + 16);
            new ShellBedFeature().place(level, generator, RandomSource.create(13), center);
            helper.assertTrue(count(helper, s -> SeaBlocks.shells().stream().anyMatch(b -> s.is(b.get()))) == 0, "Muschelbänke in der Tiefe");
            io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature.SeaLevel.override(surface);
            new ShellBedFeature().place(level, generator, RandomSource.create(13), center);
            helper.assertTrue(count(helper, s -> SeaBlocks.shells().stream().anyMatch(b -> s.is(b.get()))) > 0, "Im Seichten liegen keine Muscheln");
        } finally {
            io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature.SeaLevel.override(null);
        }
        helper.assertTrue(new KelpForestFeature(1.0f).place(level, generator, RandomSource.create(11), center), "Kein Tangwald");
        int kelp = count(helper, s -> s.is(SeaBlocks.GIANT_KELP.get()) || s.is(SeaBlocks.GIANT_KELP_PLANT.get()));
        helper.assertTrue(kelp > 0, "Der Tangwald hat keinen Riesentang");
        new AnemoneGardenFeature().place(level, generator, RandomSource.create(12), center);
        int anemones = count(helper, s -> SeaBlocks.anemones().stream().anyMatch(b -> s.is(b.get())));
        helper.assertTrue(anemones > 0, "Der Anemonengarten hat keine Anemonen");
        // jede Landschaft hat ihr Meer
        var registries = level.getServer().registryAccess();
        var placed = registries.lookupOrThrow(Registries.PLACED_FEATURE);
        var biomes = registries.lookupOrThrow(Registries.BIOME);
        for (String name : List.of("kelp_forests", "anemone_gardens", "shell_beds", "vent_fields", "glow_fields", "sea_stacks")) {
            var key = net.minecraft.resources.ResourceKey.create(Registries.PLACED_FEATURE, KingdomsAtWar.id("overworld/standard/ocean/" + name));
            var feature = placed.get(key);
            helper.assertTrue(feature.isPresent(), "Feature fehlt: " + key.identifier());
            boolean somewhere = List.of(Biomes.OCEAN, Biomes.DEEP_OCEAN, Biomes.LUKEWARM_OCEAN, Biomes.DEEP_LUKEWARM_OCEAN, Biomes.COLD_OCEAN,
                    Biomes.DEEP_COLD_OCEAN, Biomes.WARM_OCEAN).stream()
                    .anyMatch(b -> biomes.getOrThrow(b).value().getGenerationSettings().hasFeature(feature.get().value()));
            helper.assertTrue(somewhere, "Kein Meer hat " + name);
        }
        helper.succeed();
    }

    /**
     * Das Versunkene Atoll: Bauwerk und Platzierung angemeldet, nur in warmen und gemäßigten Meeren, eine Truhe mit
     * Beute; der Hügel trägt den Speer und lässt sich abtragen und wieder aufschütten; das Herz weckt erst, wer nahe kommt.
     */
    private static void sunkenAtoll(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var registries = level.getServer().registryAccess();
        helper.assertTrue(registries.lookupOrThrow(Registries.STRUCTURE).get(ModStructures.SUNKEN_ATOLL_STRUCTURE).isPresent(), "Das Bauwerk fehlt");
        var set = registries.lookupOrThrow(Registries.STRUCTURE_SET).get(ModStructures.SUNKEN_ATOLL_SET);
        helper.assertTrue(set.isPresent() && set.get().value().placement() instanceof AtollPlacement, "Die Platzierung des Atolls fehlt");
        var biomes = registries.lookupOrThrow(Registries.BIOME);
        for (var ok : List.of(Biomes.WARM_OCEAN, Biomes.LUKEWARM_OCEAN, Biomes.OCEAN)) {
            helper.assertTrue(biomes.getOrThrow(ok).is(ModTags.Biomes.HAS_SUNKEN_ATOLL), "Kein Atoll in " + ok.identifier());
        }
        for (var no : List.of(Biomes.FROZEN_OCEAN, Biomes.COLD_OCEAN, Biomes.PLAINS, Biomes.DESERT)) {
            helper.assertFalse(biomes.getOrThrow(no).is(ModTags.Biomes.HAS_SUNKEN_ATOLL), "Ein Atoll in " + no.identifier());
        }
        LootTable chest = level.getServer().reloadableRegistries().getLootTable(AtollPiece.CHEST);
        helper.assertTrue(chest != LootTable.EMPTY, "Die Truhe des Atolls hat keine Beute");
        // Hügel mit Speer
        for (int x = 0; x < 9; x++) {
            for (int z = 0; z < 9; z++) {
                helper.setBlock(x, 0, z, Blocks.PRISMARINE_BRICKS);
            }
        }
        BlockPos center = helper.absolutePos(new BlockPos(4, 1, 4));
        AtollPiece.buildMound(level, center);
        helper.assertTrue(level.getBlockState(center.above(4)).is(SeaBlocks.EMBEDDED_TIDE_SPEAR.get()), "Kein Speer im Hügel");
        helper.assertTrue(level.getBlockState(center.above(3)).is(Blocks.GOLD_BLOCK), "Keine Kuppel im Hügel");
        AtollPiece.clearMound(level, center);
        helper.assertTrue(level.getBlockState(center.above(4)).isAir() && level.getBlockState(center).isAir(), "Der Hügel bleibt liegen");
        helper.assertTrue(AtollCoreBlockEntity.wakes(center, center.getX() + 3.0, center.getY(), center.getZ() + 2.0), "Nah dran weckt nicht");
        helper.assertFalse(AtollCoreBlockEntity.wakes(center, center.getX() + 30.0, center.getY(), center.getZ()), "Von weitem weckt");
        helper.assertTrue(AtollLocator.core(center).getY() < center.getY(), "Das Herz liegt nicht unter dem Platz");
        helper.succeed();
    }

    /**
     * Karkinos: das Herz des Atolls weckt ihn (beim Erwachen unverwundbar, der Hügel verschwindet), dann treffen Scherenschlag,
     * Blasenstrahl, Panzerwirbel (Pfeile prallen ab), Eingraben (vergraben unverwundbar), Ruf der Brut, Gezeitenzorn und
     * Flutlanzen. Besiegt lässt er den Gezeiten-Speer fallen, das Atoll ist befreit – und mit dem Horn ruft man ihn zurück.
     */
    private static void reefColossusFight(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (int x = 0; x < 9; x++) {
            for (int z = 0; z < 9; z++) {
                helper.setBlock(x, 0, z, Blocks.SAND);
            }
        }
        helper.assertTrue(ModEntities.REEF_COLOSSUS.get().getCategory() == MobCategory.MONSTER, "Karkinos: falsche Gruppe");
        helper.assertTrue(level.getServer().reloadableRegistries().getLootTable(ModEntities.REEF_COLOSSUS.get().getDefaultLootTable().orElseThrow())
                != LootTable.EMPTY, "Karkinos hat keine Beute");
        BlockPos center = helper.absolutePos(new BlockPos(4, 1, 4));
        BlockPos corePos = helper.absolutePos(new BlockPos(4, -2, 4));
        level.setBlock(corePos, SeaBlocks.ATOLL_CORE.get().defaultBlockState(), Block.UPDATE_ALL);
        if (!(level.getBlockEntity(corePos) instanceof AtollCoreBlockEntity core)) {
            helper.fail("Das Herz des Atolls hat keine Blockentität");
            return;
        }
        core.setup(center);
        AtollPiece.buildMound(level, center);
        helper.assertTrue(core.phase() == AtollCoreBlockEntity.Phase.DORMANT, "Das Atoll schläft nicht");
        ReefColossusEntity boss = core.awaken(level);
        helper.assertTrue(boss != null, "Das Herz weckt Karkinos nicht");
        helper.assertTrue(core.phase() == AtollCoreBlockEntity.Phase.AWAKE, "Das Herz merkt das Erwachen nicht");
        helper.assertFalse(level.getBlockState(center.above(4)).is(SeaBlocks.EMBEDDED_TIDE_SPEAR.get()), "Der Hügel bleibt beim Erwachen");
        helper.assertTrue(boss.isAwakening(), "Karkinos erwacht nicht");
        float before = boss.getHealth();
        boss.hurtServer(level, level.damageSources().generic(), 30.0f);
        helper.assertTrue(boss.getHealth() >= before, "Beim Erwachen ist Karkinos verwundbar");
        var soldier = helper.spawnWithNoFreeWill(ModEntities.SOLDIERS.get(SoldierType.KNIGHT).get(), new BlockPos(4, 1, 0));
        helper.runAfterDelay(ReefColossusEntity.AWAKEN_TICKS + 4, () -> {
            helper.assertFalse(boss.isAwakening(), "Karkinos erhebt sich nicht ganz");
            soldier.setHealth(soldier.getMaxHealth());
            float health = soldier.getHealth();
            boss.forceAttack(level, "slam", soldier, true);
            helper.runAfterDelay(ReefColossusEntity.SLAM_IMPACT + 2, () -> {
                helper.assertTrue(soldier.getHealth() < health, "Der Scherenschlag trifft nicht");
                helper.assertTrue("slam".equals(boss.currentAttack()) || "stuck".equals(boss.currentAttack()),
                        "Nach dem Schlag steckt die Schere nicht fest: " + boss.currentAttack());
                soldier.setHealth(soldier.getMaxHealth());
                boss.forceAttack(level, "jet", soldier, true);
                helper.runAfterDelay(ReefColossusEntity.JET_START + 3, () -> {
                    helper.assertTrue(boss.isJetting(), "Kein Blasenstrahl");
                    boss.forceAttack(level, "roll", soldier, true);
                    helper.runAfterDelay(ReefColossusEntity.ROLL_START + 3, () -> {
                        helper.assertTrue(boss.isRolling(), "Kein Panzerwirbel");
                        helper.assertFalse(boss.isJetting(), "Der Blasenstrahl läuft im Wirbel weiter");
                        var arrow = EntityTypes.ARROW.create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
                        float hp = boss.getHealth();
                        if (arrow != null) {
                            boss.hurtServer(level, level.damageSources().arrow(arrow, null), 20.0f);
                            helper.assertTrue(hp - boss.getHealth() <= 5.01f, "Pfeile prallen am Wirbel nicht ab: " + (hp - boss.getHealth()));
                        }
                        boss.forceAttack(level, "burrow", soldier, true);
                        helper.runAfterDelay(ReefColossusEntity.BURROW_DOWN + 4, () -> {
                            helper.assertTrue(boss.sink() > 0.9f, "Karkinos gräbt sich nicht ein");
                            helper.assertTrue(boss.isInvulnerableTo(level, level.damageSources().generic()), "Vergraben ist er verwundbar");
                            helper.runAfterDelay(ReefColossusEntity.BURROW_TICKS - ReefColossusEntity.BURROW_DOWN + 2, () -> {
                                helper.assertTrue(boss.sink() < 0.01f, "Karkinos bricht nicht wieder hervor");
                                boss.forceAttack(level, "summon", soldier, true);
                                helper.runAfterDelay(28, () -> {
                                    helper.assertFalse(level.getEntitiesOfClass(GiantCrabEntity.class, boss.getBoundingBox().inflate(16)).isEmpty(),
                                            "Karkinos ruft keine Krabben");
                                    boss.forceAttack(level, "tide", soldier, true);
                                    helper.runAfterDelay(20, () -> {
                                        helper.assertTrue(boss.inTide(), "Kein Gezeitenzorn");
                                        // ein frischer Soldat (der erste hat schon viel einstecken müssen)
                                        var fresh = helper.spawnWithNoFreeWill(ModEntities.SOLDIERS.get(SoldierType.KNIGHT).get(),
                                                new BlockPos(1, 1, 1));
                                        float lanceBefore = fresh.getHealth();
                                        boss.forceAttack(level, "lance", fresh, true);
                                        helper.runAfterDelay(ReefColossusEntity.LANCE_STRIKE + 3, () -> {
                                            helper.assertTrue(fresh.getHealth() < lanceBefore, "Die Flutlanzen treffen nicht");
                                            maelstromAndBubbles(helper, level, boss, fresh, () -> death(helper, level, boss, core, center));
                                        });
                                    });
                                });
                            });
                        });
                    });
                });
            });
        });
    }

    /** Mahlstrom (die Scheren mahlen, die Wassersäule wirft empor) und Blasenfalle (das Ziel schwebt in einer Blase). */
    private static void maelstromAndBubbles(GameTestHelper helper, ServerLevel level, ReefColossusEntity boss, net.minecraft.world.entity.LivingEntity knight, Runnable then) {
        knight.setHealth(knight.getMaxHealth());
        float before = knight.getHealth();
        boss.forceAttack(level, "maelstrom", knight, true);
        helper.runAfterDelay(ReefColossusEntity.MAELSTROM_BURST + 2, () -> {
            helper.assertTrue("maelstrom".equals(boss.currentAttack()), "Kein Mahlstrom: " + boss.currentAttack());
            helper.assertTrue(knight.getHealth() < before, "Der Mahlstrom trifft nicht");
            knight.setHealth(knight.getMaxHealth());
            // der Mahlstrom hat das Ziel emporgeworfen: zurück an seinen Platz, damit die Blasen es sicher erreichen
            BlockPos home = helper.absolutePos(new BlockPos(1, 1, 1));
            knight.teleportTo(home.getX() + 0.5, home.getY(), home.getZ() + 0.5);
            knight.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
            knight.fallDistance = 0.0;
            boss.forceAttack(level, "bubbles", knight, true);
            helper.runAfterDelay(ReefColossusEntity.BUBBLE_BLOW + 12, () -> {
                helper.assertTrue(knight.hasEffect(net.minecraft.world.effect.MobEffects.LEVITATION), "Die Blasenfalle fängt nicht");
                helper.assertFalse(level.getEntitiesOfClass(io.github.sirschrolli.kingdomsatwar.magic.fx.SpellFxEntity.class,
                        knight.getBoundingBox().inflate(2.0)).isEmpty(), "Keine Blase um das Ziel");
                then.run();
            });
        });
    }

    private static void death(GameTestHelper helper, ServerLevel level, ReefColossusEntity boss, AtollCoreBlockEntity core, BlockPos center) {
        boss.setHealth(1.0f);
        // ein erster Sieg: der Herzkristall fällt sicher (der Gezeiten-Speer nur selten)
        BossLootGameTests.firstVictory(helper, boss, 50.0f);
        helper.assertTrue(boss.isDeadOrDying(), "Karkinos fällt nicht");
        helper.runAfterDelay(ReefColossusEntity.DEATH_TICKS + 8, () -> {
            helper.assertTrue(core.phase() == AtollCoreBlockEntity.Phase.DEFEATED, "Das Atoll merkt den Sieg nicht");
            AABB area = new AABB(center).inflate(40);
            helper.assertFalse(level.getEntitiesOfClass(ItemEntity.class, area, e -> e.getItem().is(io.github.sirschrolli.kingdomsatwar.registry.ModItems.HEART_CRYSTAL.get())).isEmpty(),
                    "Karkinos lässt keinen Herzkristall fallen");
            helper.assertTrue(level.getEntitiesOfClass(GiantCrabEntity.class, area, net.minecraft.world.entity.Entity::isAlive).isEmpty(),
                    "Die Brut bleibt nach seinem Tod");
            // das Gezeitenhorn ruft ihn zurück
            helper.assertTrue(core.recall(level), "Der Besiegte lässt sich nicht zurückrufen");
            helper.assertTrue(core.phase() == AtollCoreBlockEntity.Phase.AWAKE, "Nach dem Rückruf ist das Atoll nicht wach");
            helper.assertFalse(core.recall(level), "Ein wacher Koloss lässt sich ein zweites Mal rufen");
            core.reset(level);
            helper.assertTrue(core.phase() == AtollCoreBlockEntity.Phase.DORMANT, "Das Atoll schläft nach dem Zurücksetzen nicht");
            helper.assertTrue(level.getBlockState(center.above(4)).is(SeaBlocks.EMBEDDED_TIDE_SPEAR.get()), "Der Hügel wird nicht neu aufgeschüttet");
            helper.assertTrue(level.getEntitiesOfClass(ReefColossusEntity.class, area, net.minecraft.world.entity.Entity::isAlive).isEmpty(),
                    "Nach dem Zurücksetzen bleibt ein Koloss");
            helper.succeed();
        });
    }

    /**
     * Der Gezeiten-Speer: episch, feuerfest, mit Prismarinkristallen zu reparieren; im Wasser trifft er um ein Drittel
     * härter (trocken nicht), wer ihn im Wasser hält, schwimmt wie ein Delfin; das Gezeitenhorn ist ein Rezept.
     */
    private static void tideSpear(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        pool(helper, Blocks.STONE);
        ItemStack spear = new ItemStack(SeaBlocks.TIDE_SPEAR.get());
        helper.assertTrue(spear.getItem() instanceof TideSpearItem, "Kein Gezeiten-Speer");
        helper.assertTrue(spear.getRarity() == Rarity.EPIC, "Der Speer ist nicht episch");
        helper.assertTrue(spear.has(net.minecraft.core.component.DataComponents.DAMAGE_RESISTANT), "Der Speer verbrennt");
        helper.assertTrue(spear.isValidRepairItem(new ItemStack(Items.PRISMARINE_CRYSTALS)), "Prismarinkristalle reparieren ihn nicht");
        helper.assertTrue(spear.getMaxDamage() > 2000, "Der Speer hält zu wenig aus: " + spear.getMaxDamage());
        var wet = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(3, 1, 4));
        var victim = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(5, 1, 4));
        var dry = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(4, 6, 4));

        helper.setBlock(new BlockPos(4, 5, 4), Blocks.STONE);
        wet.setItemSlot(EquipmentSlot.MAINHAND, spear.copy());
        var recipes = level.getServer().getRecipeManager();
        helper.assertTrue(recipes.byKey(net.minecraft.resources.ResourceKey.create(Registries.RECIPE, KingdomsAtWar.id("tide_horn"))).isPresent(),
                "Kein Rezept für das Gezeitenhorn");
        helper.succeedWhen(() -> {
            // allein auf dem Testserver (ohne Spieler) ticken Wesen nicht immer: selbst anstoßen
            if (wet.tickCount == 0 || !wet.isInWater()) {
                wet.tick();
                dry.tick();
            }
            helper.assertTrue(wet.isInWater(), "Das Schwein steht nicht im Wasser");
            float wetBonus = SeaBlocks.TIDE_SPEAR.get().getAttackDamageBonus(victim, 9.0f, level.damageSources().mobAttack(wet));
            float dryBonus = SeaBlocks.TIDE_SPEAR.get().getAttackDamageBonus(victim, 9.0f, level.damageSources().mobAttack(dry));
            helper.assertTrue(Math.abs(wetBonus - 3.0f) < 0.01f, "Im Wasser trifft der Speer nicht härter: " + wetBonus);
            helper.assertTrue(dryBonus == 0.0f || dry.isInWaterOrRain(), "Trocken trifft der Speer härter: " + dryBonus);
            SeaBlocks.TIDE_SPEAR.get().inventoryTick(wet.getMainHandItem(), level, wet, EquipmentSlot.MAINHAND);
            helper.assertTrue(wet.hasEffect(MobEffects.DOLPHINS_GRACE), "Im Wasser schwimmt der Träger nicht wie ein Delfin");
        });
    }
}
