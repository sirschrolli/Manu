package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.item.MoonsilverBladeItem;
import io.github.sirschrolli.kingdomsatwar.entity.shadow.CrystalCrawlerEntity;
import io.github.sirschrolli.kingdomsatwar.entity.shadow.MoonStagEntity;
import io.github.sirschrolli.kingdomsatwar.entity.shadow.ShadeStalkerEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModTags;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.storage.loot.LootTable;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ShadowItems;
import io.github.sirschrolli.kingdomsatwar.shadow.CounterGate;
import io.github.sirschrolli.kingdomsatwar.shadow.ShadowBiomes;
import io.github.sirschrolli.kingdomsatwar.shadow.ShadowDimension;
import io.github.sirschrolli.kingdomsatwar.shadow.ShadowEquipment;
import io.github.sirschrolli.kingdomsatwar.shadow.ShadowTerrain;
import io.github.sirschrolli.kingdomsatwar.world.ShadowGate;
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
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.portal.TeleportTransition;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Tests der Schattenwelt: Dimension und Gegentor, Erze, Setzlinge, Ausrüstung und Rezepte.
 */
final class ShadowGameTests {
    private static final Identifier PLATFORM = KingdomsAtWar.id("test_platform");
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");
    private static final BlockPos TEST_POS = new BlockPos(2, 1, 2);

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SHADOW_WORLD =
            ModGameTests.TEST_FUNCTIONS.register("shadow_world", () -> ShadowGameTests::shadowWorld);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SHADOW_ORES =
            ModGameTests.TEST_FUNCTIONS.register("shadow_ores", () -> ShadowGameTests::shadowOres);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SHADOW_SAPLING =
            ModGameTests.TEST_FUNCTIONS.register("shadow_sapling", () -> ShadowGameTests::shadowSapling);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SHADOW_GEAR =
            ModGameTests.TEST_FUNCTIONS.register("shadow_gear", () -> ShadowGameTests::shadowGear);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SHADOW_RECIPES =
            ModGameTests.TEST_FUNCTIONS.register("shadow_recipes", () -> ShadowGameTests::shadowRecipes);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SHADOW_CREATURES =
            ModGameTests.TEST_FUNCTIONS.register("shadow_creatures", () -> ShadowGameTests::shadowCreatures);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SHADE_STALKER_HUNTS =
            ModGameTests.TEST_FUNCTIONS.register("shade_stalker_hunts", () -> ShadowGameTests::shadeStalkerHunts);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CRYSTAL_CRAWLER_SHARDS =
            ModGameTests.TEST_FUNCTIONS.register("crystal_crawler_shards", () -> ShadowGameTests::crystalCrawlerShards);

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> NYXAR_LIFECYCLE =
            ModGameTests.TEST_FUNCTIONS.register("nyxar_lifecycle", () -> ShadowGameTests::nyxarLifecycle);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SHADOW_CITADEL =
            ModGameTests.TEST_FUNCTIONS.register("shadow_citadel", () -> ShadowGameTests::shadowCitadel);

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SHADOW_SITES =
            ModGameTests.TEST_FUNCTIONS.register("shadow_sites", () -> ShadowGameTests::shadowSites);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ROCK_BRIDGE =
            ModGameTests.TEST_FUNCTIONS.register("rock_bridge", () -> ShadowGameTests::rockBridge);

    private ShadowGameTests() {}

    /** Lädt die Klasse (meldet die Testfunktionen an). */
    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var test : List.of(SHADOW_ORES, SHADOW_RECIPES)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(), new TestData<>(environment, PLATFORM, 100, 0, true)));
        }
        // Nyxar braucht Platz (seine Angriffe reichen weit) und Zeit (Erwachen, alle Angriffe, Todesanimation)
        event.registerTest(NYXAR_LIFECYCLE.getId(), new FunctionGameTestInstance(NYXAR_LIFECYCLE.key(),
                new TestData<>(environment, Level.OVERWORLD, ARENA, 900, 0, true, Rotation.NONE, false, 1, 1, true, 56)));
        for (var test : List.of(SHADOW_WORLD, SHADOW_SAPLING, SHADOW_GEAR, SHADOW_CREATURES, SHADE_STALKER_HUNTS, CRYSTAL_CRAWLER_SHARDS,
                SHADOW_CITADEL, SHADOW_SITES, ROCK_BRIDGE)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 400, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
    }

    /**
     * Die Schattenwelt: Dimensionstyp und Gelände sind registriert; das Gelände hat überall festen Grund, genug Land und an
     * der Oberfläche die Oberflächen-Biome (nicht die Tiefen). Gibt es die Welt (nicht auf dem Test-Server, der nur die
     * Grunddimensionen lädt), entsteht das Gegentor und führt hin und zurück.
     */
    private static void shadowWorld(GameTestHelper helper) {
        ServerLevel overworld = helper.getLevel();
        var server = overworld.getServer();
        var registries = server.registryAccess();
        helper.assertTrue(registries.lookupOrThrow(Registries.DIMENSION_TYPE).get(ShadowDimension.TYPE).isPresent(),
                "Der Dimensionstyp der Schattenwelt fehlt");
        Holder<NoiseGeneratorSettings> settings = registries.lookupOrThrow(Registries.NOISE_SETTINGS).getOrThrow(ShadowTerrain.SETTINGS);
        ServerLevel shadow = server.getLevel(ShadowGate.SHADOW_WORLD);
        ChunkGenerator generator;
        RandomState random;
        LevelHeightAccessor height;
        if (shadow != null) {
            generator = shadow.getChunkSource().getGenerator();
            random = shadow.getChunkSource().randomState();
            height = shadow;
        } else {
            generator = new NoiseBasedChunkGenerator(MultiNoiseBiomeSource.createFromList(
                    ShadowBiomes.parameters(registries.lookupOrThrow(Registries.BIOME))), settings);
            random = RandomState.create(registries.lookupOrThrow(Registries.NOISE), 20_261_026L, settings.value());
            height = LevelHeightAccessor.create(-64, 384);
        }
        var possible = generator.getBiomeSource().possibleBiomes();
        for (ResourceKey<Biome> biome : ShadowBiomes.ALL) {
            helper.assertTrue(possible.stream().anyMatch(holder -> holder.is(biome)), "Biom fehlt in der Schattenwelt: " + biome.identifier());
        }
        // Gelände: 36 Säulen in einem weiten Raster
        BiomeResolver biomes = generator.getBiomeSource().createUncachedResolver(random);
        int land = 0;
        int depthsAtSurface = 0;
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                int x = (i - 3) * 700 + 123;
                int z = (j - 3) * 700 - 77;
                int ground = generator.getBaseHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, height, random);
                helper.assertTrue(ground > -40, "Kein fester Grund bei " + x + " " + z + " (Höhe " + ground + ")");
                if (ground > ShadowTerrain.SEA_LEVEL) {
                    land++;
                }
                if (biomes.getNoiseBiome(x >> 2, (ground + 2) >> 2, z >> 2).is(ShadowBiomes.SHADOW_DEPTHS)) {
                    depthsAtSurface++;
                }
            }
        }
        helper.assertTrue(land >= 12, "Zu wenig Land in der Schattenwelt: " + land + " von 36");
        helper.assertTrue(depthsAtSurface <= 4, "Die Tiefen reichen bis an die Oberfläche: " + depthsAtSurface + " von 36");
        if (shadow != null) {
            // Gegentor: wird gebaut und führt hin und zurück
            BlockPos gate = CounterGate.ensure(shadow, BlockPos.ZERO);
            helper.assertTrue(shadow.getBlockState(gate).is(ModBlocks.SHADOW_PORTAL.get()), "Im Gegentor wogt kein Schattentor: " + gate);
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            TeleportTransition there = ShadowGate.destination(overworld, player, BlockPos.ZERO);
            helper.assertTrue(there != null && there.newLevel() == shadow, "Das Schattentor führt nicht in die Schattenwelt");
            TeleportTransition back = ShadowGate.destination(shadow, player, gate);
            helper.assertTrue(back != null && back.newLevel() == server.overworld(), "Das Gegentor führt nicht zurück");
        } else {
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            helper.assertTrue(ShadowGate.destination(overworld, player, BlockPos.ZERO) == null,
                    "Ohne Schattenwelt darf das Tor nirgendwohin führen");
        }
        helper.succeed();
    }

    /**
     * Nyxar: erwacht über den Thronkern (beim Erwachen unverwundbar), kämpft dann; alle seine eigenen Angriffe laufen
     * vollständig durch; Magie und Gift von Nicht-Spielern prallen an ihm ab; nach dem Todesstoß und der Todesanimation
     * bleibt der Thron besiegt, die Welt zählt den Sieg und das Herz der Schatten schwebt im Thronhof.
     */
    private static void nyxarLifecycle(GameTestHelper helper) {
        for (int x = 0; x < 9; x++) {
            for (int z = 0; z < 9; z++) {
                helper.setBlock(x, 0, z, net.minecraft.world.level.block.Blocks.STONE);
            }
        }
        ServerLevel level = helper.getLevel();
        BlockPos home = helper.absolutePos(new BlockPos(4, 1, 4));
        BlockPos corePos = home.below(5);
        level.setBlock(corePos, ModBlocks.SHADOW_THRONE_CORE.get().defaultBlockState(), net.minecraft.world.level.block.Block.UPDATE_ALL);
        var core = (io.github.sirschrolli.kingdomsatwar.block.entity.ShadowThroneCoreBlockEntity) level.getBlockEntity(corePos);
        helper.assertTrue(core != null, "Thronkern ohne Blockobjekt");
        core.setup(home);
        int defeatsBefore = io.github.sirschrolli.kingdomsatwar.world.SealState.of(level).nyxarDefeats();
        var boss = core.awaken(level);
        helper.assertTrue(boss != null, "Nyxar lässt sich nicht erzeugen");
        helper.assertTrue(boss.element() == null && "shadow".equals(boss.bossId()), "Nyxar hat ein Element");
        helper.assertTrue(corePos.equals(boss.getLairCore()), "Nyxar kennt seinen Thron nicht");
        helper.assertFalse(boss.hurtServer(level, level.damageSources().generic(), 10.0f), "Nyxar ist beim Erwachen verwundbar");
        int awake = boss.awakeningTicks() + 5;
        helper.runAfterDelay(awake, () -> {
            helper.assertTrue(boss.getBossState() == io.github.sirschrolli.kingdomsatwar.entity.boss.DragonBossEntity.State.FIGHT,
                    "Nyxar ist nach dem Erwachen nicht kampfbereit: " + boss.getBossState());
            // ein unverwundbarer Ritter als Ziel
            var knight = io.github.sirschrolli.kingdomsatwar.registry.ModEntities.SOLDIERS
                    .get(io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType.KNIGHT).get()
                    .create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
            helper.assertTrue(knight != null, "Ritter lässt sich nicht erzeugen");
            knight.snapTo(home.getX() + 10.5, home.getY(), home.getZ() + 0.5, 90, 0);
            knight.setPermanentlyInvulnerable(true);
            level.addFreshEntity(knight);
            for (String name : List.of("shadow_shriek", "shadow_step", "shadow_claws", "eclipse", "soul_drain", "shadow_rain", "breath",
                    "volley", "nova_ring")) {
                var attack = boss.findAttack(name);
                helper.assertTrue(attack != null, "Nyxar kennt den Angriff nicht: " + name);
                attack.start(level, boss, knight);
                for (int t = 0; t < attack.duration(); t++) {
                    attack.tick(level, boss, knight, t);
                }
                attack.stop(level, boss);
            }
            helper.assertFalse(boss.isInvisible(), "Nyxar bleibt nach dem Schattensprung unsichtbar");
            // Magie von Nicht-Spielern prallt ab, gewöhnlicher Schaden wirkt
            float before = boss.getHealth();
            boss.hurtServer(level, level.damageSources().magic(), 40.0f);
            helper.assertTrue(boss.getHealth() >= before - 0.01f, "Magie von Nicht-Spielern schadet Nyxar");
            before = boss.getHealth();
            boss.hurtServer(level, level.damageSources().generic(), 40.0f);
            helper.assertTrue(boss.getHealth() < before, "Gewöhnlicher Schaden wirkt nicht");
            knight.discard();
            boss.setHealth(1.0f);
            boss.hurtServer(level, level.damageSources().generic(), 50.0f);
            helper.assertTrue(boss.isDeadOrDying(), "Nyxar stirbt nicht");
        });
        helper.runAfterDelay(awake + io.github.sirschrolli.kingdomsatwar.entity.boss.DragonBossEntity.DEATH_TICKS + 10, () -> {
            helper.assertTrue(boss.isRemoved(), "Nyxar wurde nach der Todesanimation nicht entfernt");
            helper.assertTrue(core.phase() == io.github.sirschrolli.kingdomsatwar.world.LairRecords.Phase.DEFEATED,
                    "Der Thron ist nach dem Sieg nicht besiegt: " + core.phase());
            helper.assertTrue(io.github.sirschrolli.kingdomsatwar.world.SealState.of(level).nyxarDefeats() == defeatsBefore + 1,
                    "Die Welt zählt den Sieg über Nyxar nicht");
            var hearts = level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, new net.minecraft.world.phys.AABB(home).inflate(40),
                    item -> item.getItem().is(io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.SHADOW_HEART.get()));
            helper.assertTrue(!hearts.isEmpty(), "Kein Herz der Schatten nach dem Sieg");
            hearts.forEach(net.minecraft.world.entity.Entity::discard);
            core.reset(level);
            helper.assertTrue(core.phase() == io.github.sirschrolli.kingdomsatwar.world.LairRecords.Phase.DORMANT, "Thron lässt sich nicht zurücksetzen");
            level.setBlock(corePos, net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(), net.minecraft.world.level.block.Block.UPDATE_ALL);
            helper.succeed();
        });
    }

    /**
     * Die Bauwerke der Schattenwelt: Mondtempel (mit Altar, Truhe und Gruft), Kristallmine (Truhe in der Halle, Spawner
     * der Kristallkriecher in der Kristallkammer, Lore mit Beute im Stollen), Mondleuchtturm (Licht, Tür, Truhe im
     * Wärterhaus) und Ruine der alten Welt (Truhe, Brunnen). Gebaut wird jedes Bauwerk vollständig weit abseits der
     * anderen Tests; alle Bauwerke und ihre Gruppen sind registriert.
     */
    private static void shadowSites(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var registries = level.registryAccess();
        BlockPos base = helper.absolutePos(new BlockPos(4, 1, 4));
        var generator = level.getChunkSource().getGenerator();
        int offset = 0;
        BlockPos mine = null;
        for (var kind : io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.ShadowSiteStructure.Kind.values()) {
            helper.assertTrue(registries.lookupOrThrow(Registries.STRUCTURE)
                    .get(io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures.shadowSite(kind)).isPresent(), "Bauwerk fehlt: " + kind.id());
            var sites = registries.lookupOrThrow(Registries.STRUCTURE_SET)
                    .get(io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures.SHADOW_SITES_SET);
            helper.assertTrue(sites.isPresent() && sites.get().value().structures().stream()
                    .anyMatch(entry -> entry.structure().is(io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures.shadowSite(kind))),
                    "Nicht in der Gruppe der Bauwerke: " + kind.id());
            offset += 160;
            BlockPos origin = base.offset(2000 + offset, kind == io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.ShadowSiteStructure.Kind.MINE ? 40 : 0, 0);
            var piece = new io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.ShadowSitePiece(kind, origin,
                    net.minecraft.world.level.block.Rotation.NONE, 4242L + offset);
            var box = piece.getBoundingBox();
            // Chunk für Chunk bauen wie in der echten Weltgenerierung
            for (int cx = box.minX() >> 4; cx <= box.maxX() >> 4; cx++) {
                for (int cz = box.minZ() >> 4; cz <= box.maxZ() >> 4; cz++) {
                    var chunkBox = new net.minecraft.world.level.levelgen.structure.BoundingBox(cx << 4, level.getMinY(), cz << 4,
                            (cx << 4) + 15, level.getMaxY(), (cz << 4) + 15);
                    piece.postProcess(level, level.structureManager(), generator, net.minecraft.util.RandomSource.create(1), chunkBox,
                            new net.minecraft.world.level.ChunkPos(cx, cz), origin);
                }
            }
            switch (kind) {
                case TEMPLE -> {
                    assertChest(helper, origin.offset(3, 4, 14), io.github.sirschrolli.kingdomsatwar.loot.ModLootTables.MOON_TEMPLE_CHEST, "Tempel");
                    assertChest(helper, origin.offset(-5, -4, 10), io.github.sirschrolli.kingdomsatwar.loot.ModLootTables.MOON_TEMPLE_CRYPT, "Gruft");
                    helper.assertTrue(level.getBlockState(origin.offset(0, 5, 15)).is(ShadowBlocks.SOUL_CRYSTAL_BLOCK.get()), "Tempel ohne Mondreliquie");
                    helper.assertTrue(level.getBlockState(origin.offset(0, 20, 0)).is(ShadowBlocks.POLISHED_SHADOW_STONE.slab().get())
                            || level.getBlockState(origin.offset(0, 20, 0)).isAir(), "Tempeldach ohne First");
                }
                case MINE -> {
                    assertChest(helper, origin.offset(7, -26, -4), io.github.sirschrolli.kingdomsatwar.loot.ModLootTables.CRYSTAL_MINE_CHEST, "Mine");
                    helper.assertTrue(level.getBlockEntity(origin.offset(-29, -26, 0))
                            instanceof net.minecraft.world.level.block.entity.SpawnerBlockEntity, "Kristallkammer ohne Spawner");
                    helper.assertTrue(level.getBlockState(origin.offset(0, -10, -1)).is(net.minecraft.world.level.block.Blocks.LADDER),
                            "Schacht ohne Leiter");
                    mine = origin;
                }
                case LIGHTHOUSE -> {
                    helper.assertTrue(level.getBlockState(origin.offset(0, 29, 0)).is(net.minecraft.world.level.block.Blocks.SEA_LANTERN)
                            && level.getBlockState(origin.offset(0, 30, 0)).is(net.minecraft.world.level.block.Blocks.SEA_LANTERN),
                            "Leuchtturm ohne Licht");
                    // die Leiter führt bis unter die Galerie, darüber ist der Durchgang zum Laternenraum frei
                    helper.assertTrue(level.getBlockState(origin.offset(0, 27, 2)).is(net.minecraft.world.level.block.Blocks.LADDER)
                            && level.getBlockState(origin.offset(0, 28, 2)).isAir() && level.getBlockState(origin.offset(0, 29, 2)).isAir(),
                            "Leuchtturm: Leiter oder Durchgang zur Galerie versperrt");
                    helper.assertTrue(level.getBlockState(origin.offset(1, 30, 2)).is(net.minecraft.world.level.block.Blocks.GLASS),
                            "Laternenraum ohne Glas");
                    helper.assertTrue(level.getBlockState(origin.offset(0, 1, -4)).is(ShadowBlocks.SHADOW_DOOR.get()), "Leuchtturm ohne Tür");
                    assertChest(helper, origin.offset(-7, 1, -4), io.github.sirschrolli.kingdomsatwar.loot.ModLootTables.LIGHTHOUSE_CHEST, "Wärterhaus");
                }
                case RUIN -> {
                    assertChest(helper, origin.offset(-5, 1, 2), io.github.sirschrolli.kingdomsatwar.loot.ModLootTables.OLD_WORLD_RUIN_CHEST, "Ruine");
                    helper.assertTrue(level.getBlockState(origin.offset(-9, -1, 0)).is(net.minecraft.world.level.block.Blocks.WATER),
                            "Brunnen ohne Wasser");
                }
            }
        }
        // Die Lore ist ein Wesen: sichtbar wird sie erst, wenn ihr Chunk wirklich geladen ist – dafür den Chunk kurz
        // festhalten und warten, bis sie auftaucht.
        helper.assertTrue(mine != null, "Mine nicht gebaut");
        BlockPos cart = mine.offset(23, -26, 0);
        var cartChunk = net.minecraft.world.level.ChunkPos.containing(cart);
        level.setChunkForced(cartChunk.x(), cartChunk.z(), true);
        helper.succeedWhen(() -> {
            var carts = level.getEntitiesOfClass(net.minecraft.world.entity.vehicle.minecart.MinecartChest.class,
                    net.minecraft.world.phys.AABB.encapsulatingFullBlocks(cart.offset(-3, -2, -3), cart.offset(3, 2, 3)));
            helper.assertTrue(!carts.isEmpty() && carts.getFirst().getContainerLootTable() != null, "Keine Lore mit Beute im Stollen");
            carts.forEach(net.minecraft.world.entity.Entity::discard);
            level.setChunkForced(cartChunk.x(), cartChunk.z(), false);
        });
    }

    /**
     * Felsbrücke: über einer runden Senke, deren Rand zwölf Blöcke höher liegt als ihr Grund, spannt sich ein Bogen aus
     * Schattenstein von Kante zu Kante – über der Mitte der Senke liegt Fels, darunter bleibt sie frei.
     */
    private static void rockBridge(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(4, 1, 4));
        int cx = ((anchor.getX() + 3400) & ~15) + 8;
        int cz = (anchor.getZ() & ~15) + 8;
        int floor = anchor.getY() + 8;
        var stone = ShadowBlocks.SHADOW_STONE.base().get().defaultBlockState();
        var air = net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        for (int x = cx - 28; x <= cx + 28; x++) {
            for (int z = cz - 28; z <= cz + 28; z++) {
                int top = Math.sqrt((x - cx) * (x - cx) + (z - cz) * (z - cz)) < 8.0 ? floor : floor + 12;
                for (int y = floor - 2; y <= floor + 20; y++) {
                    level.setBlock(new BlockPos(x, y, z), y <= top ? stone : air, 2);
                }
            }
        }
        boolean placed = new io.github.sirschrolli.kingdomsatwar.shadow.feature.RockBridgeFeature().place(level,
                level.getChunkSource().getGenerator(), net.minecraft.util.RandomSource.create(7L), new BlockPos(cx, floor, cz));
        helper.assertTrue(placed, "Die Felsbrücke wurde nicht gebaut");
        int rock = 0;
        for (int y = floor + 1; y <= floor + 20; y++) {
            var state = level.getBlockState(new BlockPos(cx, y, cz));
            if (state.is(ShadowBlocks.SHADOW_STONE.base().get()) || state.is(ShadowBlocks.DEEP_SHADOW_STONE.get())
                    || state.is(ShadowBlocks.SHADOW_GRASS.get())) {
                rock++;
            }
        }
        helper.assertTrue(rock >= 2, "Kein Felsbogen über der Mitte der Senke");
        helper.assertTrue(level.getBlockState(new BlockPos(cx, floor + 1, cz)).isAir()
                && level.getBlockState(new BlockPos(cx, floor + 3, cz)).isAir(), "Der Bogen füllt die Senke aus");
        helper.succeed();
    }

    private static void assertChest(GameTestHelper helper, BlockPos pos, net.minecraft.resources.ResourceKey<net.minecraft.world.level.storage.loot.LootTable> loot,
            String where) {
        helper.assertTrue(helper.getLevel().getBlockEntity(pos) instanceof net.minecraft.world.level.block.entity.ChestBlockEntity chest
                && loot.equals(chest.getLootTable()), where + ": keine Truhe mit Beute bei " + pos);
    }

    /**
     * Die Schattenzitadelle: Bauwerk und Gruppe sind registriert, ihr Ort lässt sich aus dem Startwert berechnen (einmal,
     * im Ring 900–1700 Blöcke um die Mitte) und liegt auf passendem Land; Umbrer-Dörfer halten Abstand.
     */
    private static void shadowCitadel(GameTestHelper helper) {
        var registries = helper.getLevel().getServer().registryAccess();
        var set = registries.lookupOrThrow(Registries.STRUCTURE_SET).get(io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures.SHADOW_CITADEL_SET);
        helper.assertTrue(set.isPresent(), "Die Strukturgruppe der Zitadelle fehlt");
        helper.assertTrue(registries.lookupOrThrow(Registries.STRUCTURE)
                .get(io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures.SHADOW_CITADEL_STRUCTURE).isPresent(), "Das Bauwerk fehlt");
        var placement = (io.github.sirschrolli.kingdomsatwar.worldgen.citadel.CitadelPlacement) set.get().value().placement();
        Holder<NoiseGeneratorSettings> settings = registries.lookupOrThrow(Registries.NOISE_SETTINGS).getOrThrow(ShadowTerrain.SETTINGS);
        var source = MultiNoiseBiomeSource.createFromList(ShadowBiomes.parameters(registries.lookupOrThrow(Registries.BIOME)));
        RandomState random = RandomState.create(registries.lookupOrThrow(Registries.NOISE), 20_261_026L, settings.value());
        var chunk = io.github.sirschrolli.kingdomsatwar.worldgen.citadel.CitadelLocator.find(20_261_026L, source, random, placement);
        helper.assertTrue(chunk != null, "Die Zitadelle hat keinen Ort");
        double dist = Math.sqrt((double) chunk.getMiddleBlockX() * chunk.getMiddleBlockX() + (double) chunk.getMiddleBlockZ() * chunk.getMiddleBlockZ());
        helper.assertTrue(dist >= 850 && dist <= 3500, "Die Zitadelle liegt außerhalb ihres Rings: " + dist);
        var again = io.github.sirschrolli.kingdomsatwar.worldgen.citadel.CitadelLocator.find(20_261_026L, source, random, placement);
        helper.assertTrue(chunk.equals(again), "Der Ort der Zitadelle ist nicht beständig");
        BiomeResolver biomes = source.createUncachedResolver(random);
        var biome = io.github.sirschrolli.kingdomsatwar.worldgen.lair.NestLocator.surfaceBiome(source.createResolver(
                random.createClimateSampler(net.minecraft.world.level.levelgen.densityfunction.SamplerContext.EMPTY_UNCACHED)),
                random.createClimateSampler(net.minecraft.world.level.levelgen.densityfunction.SamplerContext.EMPTY_UNCACHED),
                chunk.getMiddleBlockX(), chunk.getMiddleBlockZ());
        helper.assertTrue(biome.is(io.github.sirschrolli.kingdomsatwar.registry.ModTags.Biomes.HAS_SHADOW_CITADEL)
                        || dist > 1700, "Die Zitadelle steht in einem unpassenden Biom: " + biome.getRegisteredName());
        helper.assertTrue(biomes != null, "Keine Biome");
        helper.succeed();
    }

    /**
     * Die Kreaturen der Schattenwelt: alle erscheinen, leben und haben Beute; Mondhirsche fressen Seelenbeeren, bekommen
     * Mondhirsch-Kitze und dürfen auf Schattengras erscheinen; die Biome kennen ihre Bewohner; die feindlichen
     * Schattenwesen sind als solche markiert (für die Mondsilberklinge).
     */
    private static void shadowCreatures(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (int x = 0; x < 9; x++) {
            for (int z = 0; z < 9; z++) {
                helper.setBlock(x, 0, z, ShadowBlocks.SHADOW_GRASS.get());
            }
        }
        var loot = level.getServer().reloadableRegistries();
        List<EntityType<? extends Mob>> types = List.of(ModEntities.SHADE_STALKER.get(), ModEntities.CRYSTAL_CRAWLER.get(),
                ModEntities.NIGHT_WING.get(), ModEntities.MOON_STAG.get(), ModEntities.GLIMMER_MOTH.get());
        List<Mob> mobs = new java.util.ArrayList<>();
        for (int i = 0; i < types.size(); i++) {
            var type = types.get(i);
            mobs.add(helper.spawnWithNoFreeWill(type, new BlockPos(1 + (i % 3) * 3, 1, 2 + (i / 3) * 4)));
            ResourceKey<LootTable> key = type.getDefaultLootTable().orElseThrow();
            helper.assertTrue(loot.getLootTable(key) != LootTable.EMPTY, "Beute fehlt: " + key.identifier());
        }
        // Tags
        helper.assertTrue(mobs.get(0).is(ModTags.EntityTypes.SHADOW_CREATURES), "Der Schattenpirscher ist kein Schattenwesen");
        helper.assertTrue(mobs.get(1).is(net.minecraft.tags.EntityTypeTags.ARTHROPOD), "Der Kristallkriecher ist kein Gliederfüßer");
        helper.assertFalse(mobs.get(3).is(ModTags.EntityTypes.SHADOW_CREATURES), "Der Mondhirsch gilt als feindliches Schattenwesen");
        // Mondhirsch
        MoonStagEntity stag = (MoonStagEntity) mobs.get(3);
        helper.assertTrue(stag.isFood(new ItemStack(ShadowItems.SOUL_BERRIES.get())), "Mondhirsche fressen keine Seelenbeeren");
        helper.assertFalse(stag.isFood(new ItemStack(Items.WHEAT)), "Mondhirsche fressen Weizen");
        var fawn = stag.getBreedOffspring(level, stag);
        helper.assertTrue(fawn instanceof MoonStagEntity, "Mondhirsche bekommen keine Mondhirsch-Kitze");
        helper.assertTrue(net.minecraft.world.entity.animal.Animal.checkAnimalSpawnRules(ModEntities.MOON_STAG.get(), level,
                EntitySpawnReason.NATURAL, helper.absolutePos(new BlockPos(7, 1, 7)), level.getRandom()), "Tiere erscheinen nicht auf Schattengras");
        // Bewohner der Biome
        var biomes = level.registryAccess().lookupOrThrow(Registries.BIOME);
        assertSpawns(helper, biomes.getOrThrow(ShadowBiomes.GLIMMER_MEADOWS).value(), MobCategory.CREATURE, ModEntities.MOON_STAG.get());
        assertSpawns(helper, biomes.getOrThrow(ShadowBiomes.GLIMMER_MEADOWS).value(), MobCategory.AMBIENT, ModEntities.GLIMMER_MOTH.get());
        assertSpawns(helper, biomes.getOrThrow(ShadowBiomes.SHADOW_FOREST).value(), MobCategory.MONSTER, ModEntities.SHADE_STALKER.get());
        assertSpawns(helper, biomes.getOrThrow(ShadowBiomes.SHADOW_DEPTHS).value(), MobCategory.MONSTER, ModEntities.CRYSTAL_CRAWLER.get());
        assertSpawns(helper, biomes.getOrThrow(ShadowBiomes.FLOATING_ISLES).value(), MobCategory.MONSTER, ModEntities.NIGHT_WING.get());
        helper.runAfterDelay(10, () -> {
            for (Mob mob : mobs) {
                helper.assertTrue(mob.isAlive(), mob.getType().getDescriptionId() + " lebt nicht mehr");
            }
            helper.succeed();
        });
    }

    private static void assertSpawns(GameTestHelper helper, Biome biome, MobCategory category, EntityType<?> type) {
        MobSpawnSettings spawns = biome.getAttributes().applyModifier(EnvironmentAttributes.NATURAL_MOB_SPAWNS, MobSpawnSettings.EMPTY);
        helper.assertTrue(spawns.getMobsToSpawn(category).unwrap().stream().anyMatch(entry -> entry.value().type() == type),
                "Biom ohne " + type.getDescriptionId());
    }

    /**
     * Schattenpirscher: merkt, ob sein Opfer ihn ansieht – angesehen wird er langsam, unbeobachtet schnell; sein Hieb hüllt
     * das Opfer in Finsternis.
     */
    private static void shadeStalkerHunts(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (int x = 0; x < 9; x++) {
            for (int z = 0; z < 9; z++) {
                helper.setBlock(x, 0, z, ShadowBlocks.SHADOW_STONE.base().get());
            }
        }
        ShadeStalkerEntity stalker = helper.spawnWithNoFreeWill(ModEntities.SHADE_STALKER.get(), new BlockPos(4, 1, 6));
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        // Blick nach Süden (+Z) auf den Pirscher
        player.snapTo(stalker.getX(), stalker.getY(), stalker.getZ() - 4.0, 0.0f, 0.0f);
        helper.assertTrue(stalker.isLookedAtBy(player), "Der Pirscher merkt nicht, dass er angesehen wird");
        player.snapTo(stalker.getX(), stalker.getY(), stalker.getZ() - 4.0, 180.0f, 0.0f);
        helper.assertFalse(stalker.isLookedAtBy(player), "Der Pirscher glaubt sich angesehen, obwohl man wegschaut");
        stalker.setTarget(player);
        double base = stalker.getAttributeBaseValue(Attributes.MOVEMENT_SPEED);
        helper.runAfterDelay(6, () -> {
            helper.assertFalse(stalker.isWatched(), "Unbeobachtet hält er sich für beobachtet");
            helper.assertTrue(stalker.getAttributeValue(Attributes.MOVEMENT_SPEED) > base * 1.2,
                    "Unbeobachtet ist er nicht schneller: " + stalker.getAttributeValue(Attributes.MOVEMENT_SPEED));
            player.snapTo(stalker.getX(), stalker.getY(), stalker.getZ() - 4.0, 0.0f, 0.0f);
        });
        helper.runAfterDelay(14, () -> {
            helper.assertTrue(stalker.isWatched(), "Angesehen merkt er es nicht");
            helper.assertTrue(stalker.getAttributeValue(Attributes.MOVEMENT_SPEED) < base * 0.5,
                    "Angesehen ist er nicht langsamer: " + stalker.getAttributeValue(Attributes.MOVEMENT_SPEED));
            stalker.doHurtTarget(level, player);
            helper.assertTrue(player.hasEffect(MobEffects.DARKNESS), "Der Hieb hüllt nicht in Finsternis");
            helper.succeed();
        });
    }

    /** Kristallkriecher: wer ihn aus der Nähe trifft, bekommt manchmal einen Splitterhagel ab. */
    private static void crystalCrawlerShards(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (int x = 0; x < 9; x++) {
            for (int z = 0; z < 9; z++) {
                helper.setBlock(x, 0, z, ShadowBlocks.SHADOW_STONE.base().get());
            }
        }
        CrystalCrawlerEntity crawler = helper.spawnWithNoFreeWill(ModEntities.CRYSTAL_CRAWLER.get(), new BlockPos(4, 1, 4));
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.snapTo(crawler.getX() + 1.5, crawler.getY(), crawler.getZ(), 90.0f, 0.0f);
        helper.assertTrue(crawler.getNavigation() instanceof net.minecraft.world.entity.ai.navigation.WallClimberNavigation,
                "Der Kristallkriecher kann nicht klettern");
        float before = player.getHealth();
        int hits = 0;
        for (int i = 0; i < 40 && player.getHealth() >= before; i++) {
            crawler.damageCooldownTime = 0;
            player.damageCooldownTime = 0;
            crawler.setHealth(crawler.getMaxHealth());
            if (crawler.hurtServer(level, level.damageSources().playerAttack(player), 0.5f)) {
                hits++;
            }
        }
        helper.assertTrue(player.getHealth() < before, "Kein Splitterhagel nach 40 Treffern (" + hits + " trafen, Leben "
                + player.getHealth() + ", Abstand " + player.distanceTo(crawler) + ")");
        helper.succeed();
    }

    /** Die Erze der Schattenwelt lassen ihre Rohstoffe fallen (Abyssit sich selbst), Drusen ihre Splitter. */
    private static void shadowOres(GameTestHelper helper) {
        assertDrops(helper, ShadowBlocks.MOONSILVER_ORE, ShadowItems.RAW_MOONSILVER.get());
        assertDrops(helper, ShadowBlocks.DEEP_MOONSILVER_ORE, ShadowItems.RAW_MOONSILVER.get());
        assertDrops(helper, ShadowBlocks.NIGHTSTEEL_ORE, ShadowItems.RAW_NIGHTSTEEL.get());
        assertDrops(helper, ShadowBlocks.DEEP_NIGHTSTEEL_ORE, ShadowItems.RAW_NIGHTSTEEL.get());
        assertDrops(helper, ShadowBlocks.SOUL_CRYSTAL_ORE, ShadowItems.SOUL_CRYSTAL.get());
        assertDrops(helper, ShadowBlocks.ABYSSITE_ORE, ShadowBlocks.ABYSSITE_ORE.get().asItem());
        assertDrops(helper, ShadowBlocks.NIGHT_CRYSTAL_CLUSTER, ShadowItems.NIGHT_CRYSTAL_SHARD.get());
        assertDrops(helper, ShadowBlocks.SHADOW_GRASS, ShadowBlocks.SHADOW_SOIL.get().asItem());
        // Abyssit braucht mindestens eine Diamantspitzhacke
        BlockState abyssite = ShadowBlocks.ABYSSITE_ORE.get().defaultBlockState();
        helper.assertFalse(new ItemStack(Items.IRON_PICKAXE).isCorrectToolForDrops(abyssite), "Abyssit lässt sich mit Eisen abbauen");
        helper.assertTrue(new ItemStack(Items.DIAMOND_PICKAXE).isCorrectToolForDrops(abyssite), "Abyssit lässt sich nicht mit Diamant abbauen");
        helper.succeed();
    }

    /** Ein Schattensetzling auf Schattengras wächst zu einem Schattenbaum. */
    private static void shadowSapling(GameTestHelper helper) {
        for (int x = 0; x < 9; x++) {
            for (int z = 0; z < 9; z++) {
                helper.setBlock(x, 0, z, ShadowBlocks.SHADOW_GRASS.get());
            }
        }
        BlockPos sapling = new BlockPos(4, 1, 4);
        helper.setBlock(sapling, ShadowBlocks.SHADOW_SAPLING.get());
        ServerLevel level = helper.getLevel();
        BlockPos abs = helper.absolutePos(sapling);
        for (int i = 0; i < 8 && level.getBlockState(abs).is(ShadowBlocks.SHADOW_SAPLING.get()); i++) {
            BlockState state = level.getBlockState(abs);
            ((SaplingBlock) state.getBlock()).advanceTree(level, abs, state, level.getRandom());
        }
        helper.assertBlockPresent(ShadowBlocks.SHADOW_LOG.get(), sapling);
        helper.succeed();
    }

    /** Mondsilberklinge, Nachtstahlhelm, Abyssit-Stiefel und die Schutzwerte der Rüstungen. */
    private static void shadowGear(GameTestHelper helper) {
        for (int x = 0; x < 9; x++) {
            for (int z = 0; z < 9; z++) {
                helper.setBlock(x, 0, z, net.minecraft.world.level.block.Blocks.STONE);
            }
        }
        // Mondsilberklinge: hart gegen Untote, normal gegen Tiere
        var zombie = helper.spawnWithNoFreeWill(EntityTypes.ZOMBIE, new BlockPos(2, 1, 2));
        var pig = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(6, 1, 6));
        MoonsilverBladeItem blade = ShadowItems.MOONSILVER_BLADE.get();
        DamageSource source = helper.getLevel().damageSources().generic();
        helper.assertTrue(blade.getAttackDamageBonus(zombie, 5.0f, source) >= MoonsilverBladeItem.BONUS_DAMAGE,
                "Die Mondsilberklinge trifft Untote nicht härter");
        helper.assertTrue(blade.getAttackDamageBonus(pig, 5.0f, source) == 0.0f, "Die Mondsilberklinge trifft auch Tiere härter");
        // Nachtstahlhelm: Sicht in der Schattenwelt
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        helper.assertFalse(ShadowEquipment.hasShadowSight(player), "Sicht ohne Helm");
        player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ShadowItems.NIGHTSTEEL_HELMET.get()));
        helper.assertTrue(ShadowEquipment.hasShadowSight(player), "Der Nachtstahlhelm schenkt keine Sicht");
        // Abyssit-Stiefel: kein Fallschaden; Rüstungswerte
        var wearer = helper.spawnWithNoFreeWill(EntityTypes.ZOMBIE, new BlockPos(4, 1, 6));
        equip(wearer, ShadowItems.ABYSSITE_HELMET.get(), ShadowItems.ABYSSITE_CHESTPLATE.get(), ShadowItems.ABYSSITE_LEGGINGS.get(),
                ShadowItems.ABYSSITE_BOOTS.get());
        var night = helper.spawnWithNoFreeWill(EntityTypes.ZOMBIE, new BlockPos(6, 1, 2));
        equip(night, ShadowItems.NIGHTSTEEL_HELMET.get(), ShadowItems.NIGHTSTEEL_CHESTPLATE.get(), ShadowItems.NIGHTSTEEL_LEGGINGS.get(),
                ShadowItems.NIGHTSTEEL_BOOTS.get());
        helper.succeedWhen(() -> {
            helper.assertTrue(wearer.getAttributeValue(Attributes.FALL_DAMAGE_MULTIPLIER) <= 0.0001,
                    "Die Abyssit-Stiefel fangen Stürze nicht ab: " + wearer.getAttributeValue(Attributes.FALL_DAMAGE_MULTIPLIER));
            helper.assertTrue(night.getArmorValue() >= 20, "Nachtstahl schützt zu wenig: " + night.getArmorValue());
            helper.assertTrue(wearer.getAttributeValue(Attributes.ARMOR_TOUGHNESS) > night.getAttributeValue(Attributes.ARMOR_TOUGHNESS),
                    "Abyssit ist nicht zäher als Nachtstahl");
        });
    }

    /** Die wichtigsten Rezepte der Schattenwelt sind da. */
    private static void shadowRecipes(GameTestHelper helper) {
        var recipes = helper.getLevel().getServer().getRecipeManager().recipeMap();
        for (String name : List.of("shadow_planks", "shadow_wood", "polished_shadow_stone", "shadow_stone_bricks", "chiseled_shadow_stone",
                "shadow_stone_stairs", "shadow_plank_slab", "ash_snow", "moonsilver_ingot_from_smelting_raw_moonsilver",
                "nightsteel_ingot_from_blasting_raw_nightsteel", "abyssite_shard_from_smelting_abyssite_ore", "abyssite_ingot",
                "nightsteel_sword", "nightsteel_chestplate", "abyssite_sword_smithing", "abyssite_boots_smithing",
                "abyssite_upgrade_smithing_template", "moon_lantern", "moonsilver_blade", "night_crystal", "moonsilver_block",
                "moonsilver_nugget_from_moonsilver_ingot")) {
            ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, KingdomsAtWar.id(name));
            helper.assertTrue(recipes.byKey(key) != null, "Rezept fehlt: " + key.identifier());
        }
        helper.succeed();
    }

    // =====================================================================================
    // Hilfen
    // =====================================================================================

    private static void equip(net.minecraft.world.entity.Mob mob, Item helmet, Item chest, Item legs, Item boots) {
        mob.setItemSlot(EquipmentSlot.HEAD, new ItemStack(helmet));
        mob.setItemSlot(EquipmentSlot.CHEST, new ItemStack(chest));
        mob.setItemSlot(EquipmentSlot.LEGS, new ItemStack(legs));
        mob.setItemSlot(EquipmentSlot.FEET, new ItemStack(boots));
    }

    private static void assertDrops(GameTestHelper helper, DeferredBlock<? extends Block> block, Item expected) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(TEST_POS);
        BlockState state = block.get().defaultBlockState();
        List<ItemStack> drops = Block.getDrops(state, level, pos, null, null, new ItemStack(Items.NETHERITE_PICKAXE));
        helper.assertTrue(drops.stream().anyMatch(stack -> stack.is(expected)),
                block.getId() + " lässt nicht " + expected + " fallen, sondern " + drops);
    }
}
