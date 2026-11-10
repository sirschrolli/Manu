package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.abyss.AbyssBiomes;
import io.github.sirschrolli.kingdomsatwar.abyss.AbyssDimension;
import io.github.sirschrolli.kingdomsatwar.abyss.AbyssGate;
import io.github.sirschrolli.kingdomsatwar.abyss.AbyssTerrain;
import io.github.sirschrolli.kingdomsatwar.abyss.GravityField;
import io.github.sirschrolli.kingdomsatwar.registry.AbyssBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.AmbientSounds;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.voidrealm.RiftState;
import io.github.sirschrolli.kingdomsatwar.voidrealm.VoidGate;
import io.github.sirschrolli.kingdomsatwar.world.ambience.AmbienceProfiles;
import java.util.List;
import java.util.Optional;
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
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/** Tests des Abgrunds: Dimension, Gelände und Biome, der Weg hinein und zurück, der Rissschlüssel und die verdrehte Schwerkraft. */
final class AbyssGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ABYSS_WORLD =
            ModGameTests.TEST_FUNCTIONS.register("abyss_world", () -> AbyssGameTests::abyssWorld);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ABYSS_RIFT_TRAVEL =
            ModGameTests.TEST_FUNCTIONS.register("abyss_rift_travel", () -> AbyssGameTests::abyssRiftTravel);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ABYSS_GRAVITY =
            ModGameTests.TEST_FUNCTIONS.register("abyss_gravity", () -> AbyssGameTests::abyssGravity);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ABYSS_KEY_AND_THRONE =
            ModGameTests.TEST_FUNCTIONS.register("abyss_key_and_throne", () -> AbyssGameTests::abyssKeyAndThrone);

    private AbyssGameTests() {}

    /** Lädt die Klasse (meldet die Testfunktionen an). */
    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var test : List.of(ABYSS_WORLD, ABYSS_RIFT_TRAVEL, ABYSS_GRAVITY)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 400, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
        // das Öffnen mit dem Schlüssel dauert vier Sekunden
        event.registerTest(ABYSS_KEY_AND_THRONE.getId(), new FunctionGameTestInstance(ABYSS_KEY_AND_THRONE.key(),
                new TestData<>(environment, Level.OVERWORLD, ARENA, 600, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
    }

    private static void floor(GameTestHelper helper, int size) {
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                helper.setBlock(x, 0, z, AbyssBlocks.POLISHED_CHAOS_STONE.base().get());
            }
        }
    }

    /**
     * Der Abgrund: Dimensionstyp und Gelände sind registriert; alle Biome kommen vor; Bruchstücke treiben in allen Höhen
     * zwischen 30 und 220 und lassen dazwischen weite, leere Strecken; in jedem Biom leben die Wesen seiner Welt, und jedes
     * hat seine Klangwelt. Der Zerbrochene König lässt den Rissschlüssel fallen.
     */
    private static void abyssWorld(GameTestHelper helper) {
        ServerLevel overworld = helper.getLevel();
        var server = overworld.getServer();
        var registries = server.registryAccess();
        helper.assertTrue(registries.lookupOrThrow(Registries.DIMENSION_TYPE).get(AbyssDimension.TYPE).isPresent(),
                "Der Dimensionstyp des Abgrunds fehlt");
        Holder<NoiseGeneratorSettings> settings = registries.lookupOrThrow(Registries.NOISE_SETTINGS).getOrThrow(AbyssTerrain.SETTINGS);
        ServerLevel abyss = server.getLevel(AbyssGate.THE_ABYSS);
        ChunkGenerator generator;
        RandomState random;
        LevelHeightAccessor height;
        if (abyss != null) {
            generator = abyss.getChunkSource().getGenerator();
            random = abyss.getChunkSource().randomState();
            height = abyss;
        } else {
            generator = new NoiseBasedChunkGenerator(MultiNoiseBiomeSource.createFromList(
                    AbyssBiomes.parameters(registries.lookupOrThrow(Registries.BIOME))), settings);
            random = RandomState.create(registries.lookupOrThrow(Registries.NOISE), 20_261_110L, settings.value());
            height = LevelHeightAccessor.create(-64, 384);
        }
        var possible = generator.getBiomeSource().possibleBiomes();
        for (ResourceKey<Biome> biome : AbyssBiomes.ALL) {
            helper.assertTrue(possible.stream().anyMatch(holder -> holder.is(biome)), "Biom fehlt im Abgrund: " + biome.identifier());
        }
        // Gelände: 100 Säulen in einem weiten Raster – Bruchstücke und Nichts im Wechsel, nie unter 25 oder über 225
        int land = 0;
        int open = 0;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                int x = (i - 5) * 300 + 23;
                int z = (j - 5) * 300 - 41;
                int top = generator.getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, height, random);
                if (top > -40) {
                    land++;
                    helper.assertTrue(top > AbyssTerrain.BOTTOM - 5 && top < AbyssTerrain.TOP + 5, "Bruchstück außerhalb der erlaubten Höhen: " + top);
                } else {
                    open++;
                }
            }
        }
        helper.assertTrue(land >= 20, "Zu wenig Bruchstücke im Abgrund: " + land + " von 100");
        helper.assertTrue(open >= 10, "Zu wenig Nichts im Abgrund: " + open + " von 100");

        // die Wesen der Welten, aus denen die Bruchstücke stammen
        var biomes = registries.lookupOrThrow(Registries.BIOME);
        assertSpawns(helper, biomes.getOrThrow(AbyssBiomes.FRACTURED_LANDS).value(), ModEntities.ZOMBIE.get());
        assertSpawns(helper, biomes.getOrThrow(AbyssBiomes.EMBER_FALLS).value(), ModEntities.ASH_WRAITH.get());
        assertSpawns(helper, biomes.getOrThrow(AbyssBiomes.STAR_WRECK).value(), ModEntities.RIFT_HOUND.get());
        assertSpawns(helper, biomes.getOrThrow(AbyssBiomes.SHADOW_CHASM).value(), ModEntities.SHADE_STALKER.get());
        assertSpawns(helper, biomes.getOrThrow(AbyssBiomes.THE_SEAM).value(), ModEntities.SHARD_WARDEN.get());

        // Klangwelten: das Chaos dröhnt überall, in der Ferne reißt die Welt
        for (ResourceKey<Biome> biome : AbyssBiomes.ALL) {
            helper.assertTrue(AmbienceProfiles.of(biome) != null, "Keine Klangwelt im Biom " + biome.identifier());
        }
        helper.assertTrue(AmbientSounds.has("ambient.bed.abyss_chaos") && AmbientSounds.has("ambient.spot.world_tear"),
                "Die Klänge des Abgrunds fehlen");

        // der Zerbrochene König lässt den Rissschlüssel fallen (genau einen)
        ServerPlayer victim = helper.makeMockServerPlayerInLevel();
        try {
            LootParams death = new LootParams.Builder(overworld)
                    .withParameter(LootContextParams.THIS_ENTITY, victim)
                    .withParameter(LootContextParams.ORIGIN, victim.position())
                    .withParameter(LootContextParams.DAMAGE_SOURCE, overworld.damageSources().generic())
                    .create(LootContextParamSets.ENTITY);
            ResourceKey<LootTable> key = ModEntities.SHATTERED_KING.get().getDefaultLootTable().orElseThrow();
            int keys = 0;
            for (ItemStack stack : server.reloadableRegistries().getLootTable(key).getRandomItems(death)) {
                if (stack.is(AbyssBlocks.RIFT_KEY.get())) {
                    keys += stack.getCount();
                }
            }
            helper.assertTrue(keys == 1, "Der König lässt nicht genau einen Rissschlüssel fallen: " + keys);
        } finally {
            server.getPlayerList().remove(victim);
        }
        ItemStack key = new ItemStack(AbyssBlocks.RIFT_KEY.get());
        helper.assertTrue(key.getRarity() == Rarity.EPIC, "Der Rissschlüssel ist nicht episch");
        helper.succeed();
    }

    private static void assertSpawns(GameTestHelper helper, Biome biome, EntityType<?> type) {
        MobSpawnSettings spawns = biome.getAttributes().applyModifier(EnvironmentAttributes.NATURAL_MOB_SPAWNS, MobSpawnSettings.EMPTY);
        helper.assertTrue(spawns.getMobsToSpawn(MobCategory.MONSTER).unwrap().stream().anyMatch(entry -> entry.value().type() == type),
                "Biom ohne " + type.getDescriptionId());
    }

    /**
     * Ein Abgrundriss führt (wenn es den Abgrund gibt) auf eine Ankerinsel mit dem Riss zurück – und dieser wieder heim.
     * Außerhalb der Leere öffnet der Schlüssel keinen Riss und schließt auch keinen.
     */
    private static void abyssRiftTravel(GameTestHelper helper) {
        ServerLevel overworld = helper.getLevel();
        ServerLevel abyss = overworld.getServer().getLevel(AbyssGate.THE_ABYSS);
        floor(helper, 9);
        BlockPos bottom = helper.absolutePos(new BlockPos(4, 1, 4));
        AbyssGate.build(overworld, bottom, Direction.Axis.X);
        helper.assertTrue(overworld.getBlockState(bottom).is(AbyssBlocks.ABYSS_RIFT.get()), "Im Abgrundriss wogt nichts");
        RiftState state = RiftState.of(overworld);
        state.add(new RiftState.Rift(overworld.dimension(), bottom, Direction.Axis.X, Optional.empty()));
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        TeleportTransition there = AbyssGate.destination(overworld, player, bottom);
        if (abyss != null) {
            helper.assertTrue(there != null && there.newLevel() == abyss, "Der Riss führt nicht in den Abgrund");
            BlockPos landing = BlockPos.containing(there.position());
            helper.assertTrue(abyss.getBlockState(landing.below()).isSolid(), "Im Abgrund fehlt die Ankerinsel unter dem Ziel");
            helper.assertTrue(landing.getY() > AbyssTerrain.BOTTOM && landing.getY() < AbyssTerrain.TOP + 20, "Die Ankerinsel liegt falsch: " + landing);
            RiftState.Rift back = state.near(AbyssGate.THE_ABYSS, landing, 6);
            helper.assertTrue(back != null && back.partner().isPresent(), "Im Abgrund fehlt der Riss zurück");
            helper.assertTrue(abyss.getBlockState(back.bottom()).is(AbyssBlocks.ABYSS_RIFT.get()), "Der Riss zurück ist nicht gebaut");
            TeleportTransition home = AbyssGate.destination(abyss, player, back.bottom());
            helper.assertTrue(home != null && home.newLevel() == overworld, "Der Riss im Abgrund führt nicht zurück");
            state.remove(back);
        } else {
            helper.assertTrue(there == null, "Ohne Abgrund darf der Riss nirgendwohin führen");
        }
        helper.assertFalse(AbyssGate.tryOpen(overworld, player, helper.absolutePos(new BlockPos(1, 0, 1))),
                "Der Rissschlüssel öffnet auch außerhalb der Leere einen Riss");
        helper.assertFalse(AbyssGate.tryClose(overworld, player, bottom.above(3)), "Ein Abgrundriss außerhalb der Leere lässt sich schließen");
        RiftState.Rift stored = state.at(overworld.dimension(), bottom);
        if (stored != null) {
            state.remove(stored);
        }
        state.save(overworld);
        helper.succeed();
    }

    /**
     * Verdrehte Schwerkraft: Am Schwebekristall wiegt man nur ein Fünftel, über dem Hebekristall steigt man auf. Wer das Feld
     * verlässt (oder wessen Kristall zerbricht), fällt bald wieder ganz normal.
     */
    private static void abyssGravity(GameTestHelper helper) {
        floor(helper, 9);
        helper.setBlock(new BlockPos(1, 1, 1), AbyssBlocks.FLOAT_CRYSTAL.get());
        helper.setBlock(new BlockPos(6, 1, 6), AbyssBlocks.LIFT_CRYSTAL.get());
        // Dorfbewohner der Mod (Vanilla-Tiere würden beim Erscheinen gegen die eigenen Tiere der Mod getauscht)
        Mob floater = helper.spawnWithNoFreeWill(ModEntities.VILLAGER.get(), new BlockPos(1, 1, 3));
        Mob lifted = helper.spawnWithNoFreeWill(ModEntities.VILLAGER.get(), new BlockPos(6, 1, 7));
        Mob outside = helper.spawnWithNoFreeWill(ModEntities.VILLAGER.get(), new BlockPos(4, 1, 8));
        double start = lifted.getY();
        double normal = floater.getAttributeValue(Attributes.GRAVITY);
        helper.runAfterDelay(40, () -> {
            helper.assertTrue(GravityField.affected(floater), "Der Schwebekristall wirkt nicht");
            helper.assertTrue(floater.getAttributeValue(Attributes.GRAVITY) < normal * 0.3, "Am Schwebekristall ist die Schwerkraft nicht gering");
            helper.assertTrue(GravityField.affected(lifted), "Der Hebekristall wirkt nicht");
            helper.assertTrue(lifted.getY() > start + 1.0, "Über dem Hebekristall steigt nichts auf: " + (lifted.getY() - start));
            helper.assertFalse(GravityField.affected(outside), "Die verdrehte Schwerkraft reicht zu weit");
            helper.assertTrue(lifted.fallDistance < 0.5, "Im Hebefeld sammelt sich Fallhöhe an");
            helper.setBlock(new BlockPos(1, 1, 1), Blocks.AIR);
        });
        helper.runAfterDelay(70, () -> {
            helper.assertFalse(GravityField.affected(floater), "Nach dem Kristall bleibt die Schwerkraft verdreht");
            helper.assertTrue(Math.abs(floater.getAttributeValue(Attributes.GRAVITY) - normal) < 1.0e-6, "Die Schwerkraft wird nicht wieder normal");
            helper.succeed();
        });
    }

    /**
     * Der Rissschlüssel in der Leere: Er reißt nach vier Sekunden einen Riss auf, den er wieder schließen kann. Der Riss hinter
     * dem Thron entsteht einmal (auf gemeißeltem Chaosgestein) und lässt sich nicht schließen. Ohne Leere (nicht auf jedem
     * Test-Server) bleibt es bei der Prüfung, dass der Schlüssel dort nichts tut.
     */
    private static void abyssKeyAndThrone(GameTestHelper helper) {
        var server = helper.getLevel().getServer();
        ServerLevel voidLevel = server.getLevel(VoidGate.THE_VOID);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        if (voidLevel == null || server.getLevel(AbyssGate.THE_ABYSS) == null) {
            helper.succeed();
            return;
        }
        // eine kleine Plattform weit draußen in der Leere, frei darüber
        BlockPos ground = new BlockPos(24_016, 130, 24_016);
        voidLevel.setChunkForced(ground.getX() >> 4, ground.getZ() >> 4, true);
        load(voidLevel, ground, 1);
        for (int dx = -6; dx <= 6; dx++) {
            for (int dz = -6; dz <= 6; dz++) {
                voidLevel.setBlock(ground.offset(dx, 0, dz), AbyssBlocks.POLISHED_CHAOS_STONE.base().get().defaultBlockState(), Block.UPDATE_CLIENTS);
                for (int dy = 1; dy <= AbyssGate.RIFT_HEIGHT + 3; dy++) {
                    voidLevel.setBlock(ground.offset(dx, dy, dz), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                }
            }
        }
        player.snapTo(ground.getX() + 0.5, ground.getY() + 1, ground.getZ() - 1.5, 0.0f, 0.0f);
        helper.assertTrue(AbyssGate.tryOpen(voidLevel, player, ground), "Der Rissschlüssel öffnet in der Leere keinen Riss");
        BlockPos opened = ground.above().relative(Direction.SOUTH, 2);

        // der Riss hinter dem Thron
        BlockPos center = new BlockPos(24_200, 120, 24_200);
        BlockPos gate = AbyssGate.throneGateBottom(center);
        load(voidLevel, gate, 1);
        helper.assertTrue(AbyssGate.ensureThroneGate(voidLevel, center, false), "Hinter dem Thron öffnet sich kein Riss");
        helper.assertTrue(voidLevel.getBlockState(gate).is(AbyssBlocks.ABYSS_RIFT.get()), "Der Riss hinter dem Thron ist nicht gebaut");
        helper.assertTrue(voidLevel.getBlockState(gate.below()).is(AbyssBlocks.CHISELED_CHAOS_STONE.get()), "Der Riss steht nicht auf seiner Plattform");
        helper.assertFalse(AbyssGate.ensureThroneGate(voidLevel, center, false), "Der Riss hinter dem Thron entsteht zweimal");
        helper.assertFalse(AbyssGate.tryClose(voidLevel, player, gate.above(4)), "Der Riss hinter dem Thron lässt sich schließen");

        helper.runAfterDelay(100, () -> {
            helper.assertTrue(voidLevel.getBlockState(opened).is(AbyssBlocks.ABYSS_RIFT.get()), "Der Schlüssel hat keinen Riss aufgerissen");
            helper.assertTrue(AbyssGate.tryClose(voidLevel, player, opened.above(3)), "Der Schlüssel schließt seinen Riss nicht");
            helper.assertFalse(voidLevel.getBlockState(opened.above(3)).is(AbyssBlocks.ABYSS_RIFT.get()), "Nach dem Schließen wogt der Riss weiter");
            RiftState state = RiftState.of(voidLevel);
            RiftState.Rift throne = state.at(VoidGate.THE_VOID, gate);
            if (throne != null) {
                state.remove(throne);
                state.save(voidLevel);
            }
            voidLevel.setChunkForced(ground.getX() >> 4, ground.getZ() >> 4, false);
            helper.succeed();
        });
    }

    private static void load(ServerLevel level, BlockPos pos, int radius) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                level.getChunk((pos.getX() >> 4) + dx, (pos.getZ() >> 4) + dz);
            }
        }
    }
}
