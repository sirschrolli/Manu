package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.entity.monster.ChorusMawEntity;
import io.github.sirschrolli.kingdomsatwar.entity.monster.MonsterShot;
import io.github.sirschrolli.kingdomsatwar.entity.monster.NightRamEntity;
import io.github.sirschrolli.kingdomsatwar.entity.monster.RiftHoundEntity;
import io.github.sirschrolli.kingdomsatwar.entity.monster.SoulHeronEntity;
import io.github.sirschrolli.kingdomsatwar.entity.villager.VillagerEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModTags;
import io.github.sirschrolli.kingdomsatwar.registry.ShadowItems;
import io.github.sirschrolli.kingdomsatwar.shadow.ShadowBiomes;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/** Tests der Monster der Schattenwelt und des Ends (0.71): Seelenreiher, Nachtwidder, Rissjäger und Chorusschlund. */
final class RealmGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> BASICS =
            ModGameTests.TEST_FUNCTIONS.register("realm_basics", () -> RealmGameTests::basics);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> HERON =
            ModGameTests.TEST_FUNCTIONS.register("realm_soul_heron", () -> RealmGameTests::heron);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> RAM =
            ModGameTests.TEST_FUNCTIONS.register("realm_night_ram", () -> RealmGameTests::ram);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> HOUND =
            ModGameTests.TEST_FUNCTIONS.register("realm_rift_hound", () -> RealmGameTests::hound);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> MAW =
            ModGameTests.TEST_FUNCTIONS.register("realm_chorus_maw", () -> RealmGameTests::maw);

    private RealmGameTests() {}

    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        event.registerTest(BASICS.getId(), new FunctionGameTestInstance(BASICS.key(),
                new TestData<>(environment, Level.OVERWORLD, ARENA, 100, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        for (var test : List.of(HERON, RAM, HOUND, MAW)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 500, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
    }

    /** Boden von −2 bis 11 – alles, wohin ein Wesen springen oder versetzt werden kann, liegt in der eigenen Arena. */
    private static void floor(GameTestHelper helper, Block block) {
        for (int x = -2; x <= 11; x++) {
            for (int z = -2; z <= 11; z++) {
                helper.setBlock(x, 0, z, block);
            }
        }
    }

    /** Eine Wand quer durch die Arena (x 1–7, drei Blöcke hoch) bei {@code z}. */
    private static void wall(GameTestHelper helper, int z, Block block) {
        for (int x = 1; x <= 7; x++) {
            for (int y = 1; y <= 3; y++) {
                helper.setBlock(new BlockPos(x, y, z), block);
            }
        }
    }

    /** Die eigene Arena (die Nachbartests liegen 14 Blöcke weiter – ihr Hab und Gut bleibt unberührt). */
    private static AABB area(GameTestHelper helper) {
        return new AABB(Vec3.atLowerCornerOf(helper.absolutePos(new BlockPos(-2, -1, -2))),
                Vec3.atLowerCornerOf(helper.absolutePos(new BlockPos(12, 12, 12))));
    }

    private static void cleanup(GameTestHelper helper) {
        for (Entity e : helper.getLevel().getEntitiesOfClass(Entity.class, area(helper), e -> !(e instanceof ServerPlayer))) {
            e.discard();
        }
    }

    /** Stellt ein Wesen an einen Punkt der Arena und lässt es dorthin blicken. */
    private static void place(GameTestHelper helper, Mob mob, double x, double z, double lookX, double lookZ) {
        Vec3 at = helper.absoluteVec(new Vec3(x, 1.0, z));
        float yaw = (float) (Mth.atan2(lookZ - z, lookX - x) * Mth.RAD_TO_DEG) - 90.0f;
        mob.snapTo(at.x, at.y, at.z, yaw, 0.0f);
        mob.setYBodyRot(yaw);
        mob.setYHeadRot(yaw);
        mob.setDeltaMovement(Vec3.ZERO);
    }

    /** Ein zähes Opfer: hält auch die Hiebe aus, die das Wesen zwischen den Prüfschritten von sich aus austeilt. */
    private static VillagerEntity victim(GameTestHelper helper, BlockPos pos) {
        VillagerEntity villager = helper.spawnWithNoFreeWill(ModEntities.VILLAGER.get(), pos);
        villager.getAttribute(Attributes.MAX_HEALTH).setBaseValue(400.0);
        villager.setHealth(400.0f);
        return villager;
    }

    /** Das Opfer wieder heil und ohne Wirkungen (und gleich wieder verwundbar). */
    private static void refresh(VillagerEntity villager) {
        villager.removeAllEffects();
        villager.setHealth(villager.getMaxHealth());
        villager.setInvulnerableTime(0);
        villager.hurtTime = 0;
    }

    private static void expect(GameTestHelper helper, HolderLookup.RegistryLookup<Biome> biomes, ResourceKey<Biome> biome, TagKey<Biome> tag,
            boolean in) {
        helper.assertTrue(biomes.getOrThrow(biome).is(tag) == in, biome.identifier().getPath() + (in ? " fehlt in " : " gehört nicht in ")
                + tag.location().getPath());
    }

    private static void assertSpawns(GameTestHelper helper, Biome biome, EntityType<?> type, String where) {
        MobSpawnSettings spawns = biome.getAttributes().applyModifier(EnvironmentAttributes.NATURAL_MOB_SPAWNS, MobSpawnSettings.EMPTY);
        helper.assertTrue(spawns.getMobsToSpawn(MobCategory.MONSTER).unwrap().stream().anyMatch(entry -> entry.value().type() == type),
                where + " ohne " + type.getDescriptionId());
    }

    /** Würfelt die Beute eines Wesens dreißigmal (ohne Spieler als Täter) und prüft, dass die Grundbeute dabei ist. */
    private static void assertLoot(GameTestHelper helper, Mob mob, Item... items) {
        ServerLevel level = helper.getLevel();
        LootTable table = level.getServer().reloadableRegistries().getLootTable(mob.getType().getDefaultLootTable().orElseThrow());
        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.THIS_ENTITY, mob)
                .withParameter(LootContextParams.ORIGIN, mob.position())
                .withParameter(LootContextParams.DAMAGE_SOURCE, level.damageSources().generic())
                .create(LootContextParamSets.ENTITY);
        boolean[] found = new boolean[items.length];
        for (int i = 0; i < 30; i++) {
            for (ItemStack stack : table.getRandomItems(params)) {
                for (int k = 0; k < items.length; k++) {
                    found[k] |= stack.is(items[k]);
                }
            }
        }
        for (int k = 0; k < items.length; k++) {
            helper.assertTrue(found[k], mob.getType().getDescriptionId() + " lässt kein " + BuiltInRegistries.ITEM.getKey(items[k]) + " fallen");
        }
    }

    // =====================================================================================
    // Grundlagen
    // =====================================================================================

    /**
     * Alle vier sind Monster mit Beute, Münzen und Spawn-Ei; der Reiher kennt keinen Fallschaden, der Schlund wurzelt (lässt
     * sich weder schieben noch zurückstoßen). Rissjäger und Schlund leben auf den äußeren End-Inseln (nicht auf der
     * Hauptinsel), Reiher im Seelensumpf, Widder auf den Schattengipfeln und in der Bleichen Öde; der Reiher erscheint nur nahe
     * am Wasser, der Schlund nur auf Endstein.
     */
    private static void basics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        List<EntityType<? extends Mob>> types = List.of(ModEntities.SOUL_HERON.get(), ModEntities.NIGHT_RAM.get(), ModEntities.RIFT_HOUND.get(),
                ModEntities.CHORUS_MAW.get());
        for (EntityType<?> type : types) {
            String name = BuiltInRegistries.ENTITY_TYPE.getKey(type).getPath();
            helper.assertTrue(type.getCategory() == MobCategory.MONSTER, name + ": keine Monstergruppe");
            helper.assertTrue(type.getDefaultLootTable().isPresent(), name + ": keine Beute");
            helper.assertTrue(type.builtInRegistryHolder().is(ModTags.EntityTypes.DROPS_COINS), name + ": keine Münzen");
            helper.assertTrue(BuiltInRegistries.ITEM.containsKey(KingdomsAtWar.id(name + "_spawn_egg")), name + ": kein Spawn-Ei");
        }
        helper.assertTrue(ModEntities.SOUL_HERON.get().builtInRegistryHolder().is(EntityTypeTags.FALL_DAMAGE_IMMUNE),
                "Der Seelenreiher nimmt Fallschaden");
        var biomes = level.registryAccess().lookupOrThrow(Registries.BIOME);
        expect(helper, biomes, Biomes.END_HIGHLANDS, ModTags.Biomes.HAS_RIFT_HOUNDS, true);
        expect(helper, biomes, Biomes.END_MIDLANDS, ModTags.Biomes.HAS_RIFT_HOUNDS, true);
        expect(helper, biomes, Biomes.END_BARRENS, ModTags.Biomes.HAS_RIFT_HOUNDS, true);
        expect(helper, biomes, Biomes.SMALL_END_ISLANDS, ModTags.Biomes.HAS_RIFT_HOUNDS, true);
        expect(helper, biomes, Biomes.THE_END, ModTags.Biomes.HAS_RIFT_HOUNDS, false);
        expect(helper, biomes, Biomes.END_HIGHLANDS, ModTags.Biomes.HAS_CHORUS_MAWS, true);
        expect(helper, biomes, Biomes.END_MIDLANDS, ModTags.Biomes.HAS_CHORUS_MAWS, true);
        expect(helper, biomes, Biomes.END_BARRENS, ModTags.Biomes.HAS_CHORUS_MAWS, false);
        expect(helper, biomes, Biomes.THE_END, ModTags.Biomes.HAS_CHORUS_MAWS, false);
        assertSpawns(helper, biomes.getOrThrow(ShadowBiomes.SOUL_MARSH).value(), ModEntities.SOUL_HERON.get(), "Seelensumpf");
        assertSpawns(helper, biomes.getOrThrow(ShadowBiomes.SHADOW_PEAKS).value(), ModEntities.NIGHT_RAM.get(), "Schattengipfel");
        assertSpawns(helper, biomes.getOrThrow(ShadowBiomes.PALE_WASTES).value(), ModEntities.NIGHT_RAM.get(), "Bleiche Öde");

        // die Beute (ohne Spieler als Täter nur die Grundbeute)
        floor(helper, Blocks.GRASS_BLOCK);
        SoulHeronEntity heron = helper.spawnWithNoFreeWill(ModEntities.SOUL_HERON.get(), new BlockPos(1, 1, 1));
        NightRamEntity ram = helper.spawnWithNoFreeWill(ModEntities.NIGHT_RAM.get(), new BlockPos(7, 1, 1));
        RiftHoundEntity hound = helper.spawnWithNoFreeWill(ModEntities.RIFT_HOUND.get(), new BlockPos(1, 1, 7));
        ChorusMawEntity maw = helper.spawnWithNoFreeWill(ModEntities.CHORUS_MAW.get(), new BlockPos(7, 1, 7));
        assertLoot(helper, heron, Items.FEATHER);
        assertLoot(helper, ram, Items.MUTTON, ShadowItems.NIGHT_CRYSTAL_SHARD.get());
        assertLoot(helper, hound, Items.AMETHYST_SHARD);
        assertLoot(helper, maw, Items.CHORUS_FRUIT);

        // der Schlund wurzelt
        helper.assertFalse(maw.isPushable(), "Der Schlund lässt sich schieben");
        helper.assertTrue(maw.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE) >= 1.0, "Der Schlund lässt sich zurückstoßen");

        // Spawnregeln: der Reiher braucht Wasser in der Nähe, der Schlund Endstein (nicht, wenn schon zu viele da sind)
        heron.discard();
        maw.discard();
        BlockPos spot = helper.absolutePos(new BlockPos(4, 1, 4));
        if (level.getDifficulty() != Difficulty.PEACEFUL) {
            helper.assertFalse(SoulHeronEntity.checkHeronSpawnRules(ModEntities.SOUL_HERON.get(), level, EntitySpawnReason.NATURAL, spot,
                    level.getRandom()), "Der Reiher erscheint fern vom Wasser");
            helper.assertFalse(ChorusMawEntity.checkMawSpawnRules(ModEntities.CHORUS_MAW.get(), level, EntitySpawnReason.NATURAL, spot,
                    level.getRandom()), "Der Schlund erscheint auf Gras");
            helper.setBlock(new BlockPos(6, -1, 4), Blocks.STONE);
            helper.setBlock(new BlockPos(6, 0, 4), Blocks.WATER);
            helper.setBlock(new BlockPos(4, 0, 4), Blocks.END_STONE);
            if (level.getEntitiesOfClass(SoulHeronEntity.class, new AABB(spot).inflate(20.0)).isEmpty()) {
                helper.assertTrue(SoulHeronEntity.checkHeronSpawnRules(ModEntities.SOUL_HERON.get(), level, EntitySpawnReason.NATURAL, spot,
                        level.getRandom()), "Der Reiher erscheint nicht am Wasser");
            }
            if (level.getEntitiesOfClass(ChorusMawEntity.class, new AABB(spot).inflate(30.0)).size() < 3) {
                helper.assertTrue(ChorusMawEntity.checkMawSpawnRules(ModEntities.CHORUS_MAW.get(), level, EntitySpawnReason.NATURAL, spot,
                        level.getRandom()), "Der Schlund erscheint nicht auf Endstein");
            }
        }
        ChorusMawEntity rootedMaw = helper.spawnWithNoFreeWill(ModEntities.CHORUS_MAW.get(), new BlockPos(7, 1, 7));
        Vec3 start = rootedMaw.position();
        rootedMaw.push(1.0, 0.3, 1.0);
        rootedMaw.knockback(2.0, 1.0, 1.0, level.damageSources().generic(), 1.0f, false);
        helper.runAfterDelay(5, () -> {
            helper.assertTrue(rootedMaw.position().distanceTo(start) < 0.05, "Der Schlund rutscht weg (" + start + " → "
                    + rootedMaw.position() + ")");
            helper.setBlock(new BlockPos(6, 0, 4), Blocks.GRASS_BLOCK);
            cleanup(helper);
            helper.succeed();
        });
    }

    // =====================================================================================
    // Seelenreiher
    // =====================================================================================

    /**
     * Der Seelenreiher stößt mit dem Speerschnabel zu (Schaden, verlangsamt), schlägt mit den Schwingen (treibt zurück,
     * blendet) und greift aus der Luft an: er steigt steil auf, fliegt über sein Opfer und stürzt sich herab – und landet
     * ohne Fallschaden.
     */
    private static void heron(GameTestHelper helper) {
        floor(helper, Blocks.MUD);
        VillagerEntity villager = victim(helper, new BlockPos(4, 1, 6));
        SoulHeronEntity heron = helper.spawnWithNoFreeWill(ModEntities.SOUL_HERON.get(), new BlockPos(4, 1, 3));
        place(helper, heron, 4.5, 3.5, 4.5, 6.5);
        heron.setTarget(villager);
        heron.forceStab();
        helper.assertTrue(heron.isStabbing(), "Er holt nicht zum Stoß aus");
        helper.runAfterDelay(SoulHeronEntity.STAB_WINDUP + 2, () -> {
            helper.assertTrue(villager.getHealth() < villager.getMaxHealth(), "Der Schnabelstoß trifft nicht");
            helper.assertTrue(villager.hasEffect(MobEffects.SLOWNESS), "Der Schnabelstoß verlangsamt nicht");
            helper.runAfterDelay(SoulHeronEntity.STAB_TICKS - SoulHeronEntity.STAB_WINDUP, () -> {
                helper.assertFalse(heron.isStabbing(), "Der Stoß endet nicht");
                // Schwingenschlag: wer dicht vor ihm steht, fliegt zurück und sieht nichts mehr
                refresh(villager);
                Vec3 v = helper.absoluteVec(new Vec3(4.5, 1.0, 5.0));
                villager.teleportTo(v.x, v.y, v.z);
                villager.setDeltaMovement(Vec3.ZERO);
                place(helper, heron, 4.5, 3.5, 4.5, 6.5);
                double before = villager.distanceTo(heron);
                heron.forceBuffet();
                helper.runAfterDelay(SoulHeronEntity.BUFFET_HIT + 6, () -> {
                    helper.assertTrue(villager.getHealth() < villager.getMaxHealth(), "Der Schwingenschlag schadet nicht");
                    helper.assertTrue(villager.hasEffect(MobEffects.BLINDNESS), "Der Schwingenschlag blendet nicht");
                    helper.assertTrue(villager.distanceTo(heron) > before + 0.5, "Der Schwingenschlag treibt nicht zurück ("
                            + before + " → " + villager.distanceTo(heron) + ")");
                    helper.runAfterDelay(SoulHeronEntity.BUFFET_TICKS, () -> flight(helper, heron, villager));
                });
            });
        });
    }

    private static void flight(GameTestHelper helper, SoulHeronEntity heron, VillagerEntity villager) {
        refresh(villager);
        Vec3 v = helper.absoluteVec(new Vec3(4.5, 1.0, 9.5));
        villager.teleportTo(v.x, v.y, v.z);
        villager.setDeltaMovement(Vec3.ZERO);
        place(helper, heron, 4.5, 1.5, 4.5, 9.5);
        heron.setHealth(heron.getMaxHealth());
        double ground = heron.getY();
        double[] top = {ground};
        helper.onEachTick(() -> top[0] = Math.max(top[0], heron.getY()));
        heron.forceTakeOff();
        helper.runAfterDelay(8, () -> helper.assertTrue(heron.isFlying() && heron.getY() > ground + 1.2, "Er hebt nicht ab (fliegt "
                + heron.isFlying() + ", " + (heron.getY() - ground) + " hoch, Ziel " + heron.getTarget() + ")"));
        int flightEnd = SoulHeronEntity.RISE_TICKS + SoulHeronEntity.APPROACH_TICKS + SoulHeronEntity.DIVE_TICKS + 12;
        helper.runAfterDelay(flightEnd, () -> {
            helper.assertTrue(top[0] > ground + 4.0, "Er steigt nicht hoch genug auf (" + (top[0] - ground) + ")");
            helper.assertFalse(heron.isFlying(), "Er landet nicht wieder");
            helper.assertTrue(villager.getHealth() < villager.getMaxHealth(), "Der Sturzflug trifft nicht");
            helper.assertTrue(heron.getHealth() >= heron.getMaxHealth(), "Er nimmt Schaden beim Landen");
            heron.discard();
            villager.discard();
            cleanup(helper);
            helper.succeed();
        });
    }

    // =====================================================================================
    // Nachtwidder
    // =====================================================================================

    /**
     * Der Nachtwidder scharrt und stürmt dann geradeaus auf sein Opfer los (Schaden, schleudert davon); rennt er gegen eine
     * Wand, ist er benommen (und verwundbarer); aufgebäumt stampft er (Schaden und Verlangsamung ringsum).
     */
    private static void ram(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper, Blocks.STONE);
        wall(helper, 10, Blocks.GLASS);          // fängt das davongeschleuderte Opfer auf
        VillagerEntity villager = victim(helper, new BlockPos(4, 1, 7));
        NightRamEntity ram = helper.spawnWithNoFreeWill(ModEntities.NIGHT_RAM.get(), new BlockPos(4, 1, 1));
        place(helper, ram, 4.5, 1.5, 4.5, 7.5);
        // erst die Wucht eines Treffers, solange er bei Sinnen ist
        ram.setInvulnerableTime(0);
        float full = ram.getHealth();
        ram.hurtServer(level, level.damageSources().generic(), 4.0f);
        float normalLoss = full - ram.getHealth();
        ram.setHealth(ram.getMaxHealth());
        ram.setTarget(villager);
        ram.forceCharge();
        helper.assertTrue(ram.state() == NightRamEntity.PREPARE, "Er scharrt nicht");
        helper.runAfterDelay(NightRamEntity.PREPARE_TICKS + 2, () -> {
            helper.assertTrue(ram.state() == NightRamEntity.CHARGE, "Er stürmt nicht los (" + ram.state() + ")");
            helper.runAfterDelay(16, () -> {
                helper.assertTrue(villager.getHealth() < villager.getMaxHealth(), "Der Ansturm trifft nicht");
                helper.assertTrue(ram.state() != NightRamEntity.CHARGE, "Nach dem Treffer stürmt er weiter");
                stun(helper, level, ram, villager, normalLoss);
            });
        });
    }

    /** Gegen die Wand: Krachen, benommen, verwundbarer – und nach einer Weile wieder bei Sinnen. */
    private static void stun(GameTestHelper helper, ServerLevel level, NightRamEntity ram, VillagerEntity villager, float normalLoss) {
        wall(helper, 7, Blocks.STONE);
        refresh(villager);
        Vec3 v = helper.absoluteVec(new Vec3(4.5, 1.0, 9.5));
        villager.teleportTo(v.x, v.y, v.z);
        villager.setDeltaMovement(Vec3.ZERO);
        place(helper, ram, 4.5, 1.5, 4.5, 9.5);
        ram.setTarget(villager);
        ram.forceCharge();
        helper.runAfterDelay(NightRamEntity.PREPARE_TICKS + 16, () -> {
            helper.assertTrue(ram.state() == NightRamEntity.STUNNED, "Gegen die Wand ist er nicht benommen (" + ram.state() + ")");
            helper.assertTrue(villager.getHealth() >= villager.getMaxHealth(), "Er trifft durch die Wand");
            ram.setInvulnerableTime(0);
            float before = ram.getHealth();
            ram.hurtServer(level, level.damageSources().generic(), 4.0f);
            float stunnedLoss = before - ram.getHealth();
            helper.assertTrue(stunnedLoss > normalLoss * 1.3f, "Benommen ist er nicht verwundbarer (" + normalLoss + " → " + stunnedLoss + ")");
            ram.setTarget(null);                 // sonst stampft er gleich wieder nach dem Opfer hinter der Wand
            helper.runAfterDelay(NightRamEntity.STUN_TICKS + 2, () -> {
                helper.assertTrue(ram.state() == NightRamEntity.IDLE, "Er bleibt benommen (Zustand " + ram.state() + ")");
                wall(helper, 7, Blocks.AIR);
                wall(helper, 10, Blocks.AIR);
                stomp(helper, ram, villager);
            });
        });
    }

    private static void stomp(GameTestHelper helper, NightRamEntity ram, VillagerEntity villager) {
        refresh(villager);
        Vec3 v = helper.absoluteVec(new Vec3(4.5, 1.0, 6.5));
        villager.teleportTo(v.x, v.y, v.z);
        villager.setDeltaMovement(Vec3.ZERO);
        place(helper, ram, 4.5, 4.5, 4.5, 6.5);
        ram.forceStomp();
        helper.assertTrue(ram.state() == NightRamEntity.STOMP, "Er bäumt sich nicht auf");
        helper.runAfterDelay(NightRamEntity.STOMP_HIT + 2, () -> {
            helper.assertTrue(villager.getHealth() < villager.getMaxHealth(), "Das Stampfen schadet nicht");
            helper.assertTrue(villager.hasEffect(MobEffects.SLOWNESS), "Das Stampfen verlangsamt nicht");
            helper.runAfterDelay(NightRamEntity.STOMP_TICKS - NightRamEntity.STOMP_HIT, () -> {
                helper.assertTrue(ram.state() == NightRamEntity.IDLE, "Er stampft endlos");
                ram.discard();
                villager.discard();
                cleanup(helper);
                helper.succeed();
            });
        });
    }

    // =====================================================================================
    // Rissjäger
    // =====================================================================================

    /**
     * Der Rissjäger springt durch die Leere (dabei trifft ihn nichts), tritt hinter seinem Opfer heraus und beißt sofort zu
     * (verlangsamt); sein Heulen macht das ganze Rudel schneller.
     */
    private static void hound(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper, Blocks.END_STONE);
        VillagerEntity villager = victim(helper, new BlockPos(4, 1, 8));
        RiftHoundEntity hound = helper.spawnWithNoFreeWill(ModEntities.RIFT_HOUND.get(), new BlockPos(4, 1, 1));
        RiftHoundEntity mate = helper.spawnWithNoFreeWill(ModEntities.RIFT_HOUND.get(), new BlockPos(1, 1, 2));
        place(helper, hound, 4.5, 1.5, 4.5, 8.5);
        hound.setTarget(villager);
        hound.forcePhase();
        helper.assertTrue(hound.isPhasing(), "Er springt nicht in die Leere");
        float hp = hound.getHealth();
        hound.hurtServer(level, level.damageSources().generic(), 5.0f);
        helper.assertTrue(hound.getHealth() >= hp, "Zwischen den Welten ist er verwundbar");
        helper.runAfterDelay(RiftHoundEntity.PHASE_TICKS + 2, () -> {
            helper.assertFalse(hound.isPhasing(), "Er kehrt nicht aus der Leere zurück");
            helper.assertTrue(hound.distanceTo(villager) < 3.4, "Er taucht nicht bei seinem Opfer auf (" + hound.distanceTo(villager) + ")");
            helper.assertTrue(villager.getHealth() < villager.getMaxHealth(), "Er beißt nach dem Sprung nicht zu");
            helper.assertTrue(villager.hasEffect(MobEffects.SLOWNESS), "Der Biss verlangsamt nicht");
            hound.setTarget(null);
            hound.forceHowl();
            helper.runAfterDelay(RiftHoundEntity.HOWL_TICKS - 6, () -> {
                helper.assertTrue(hound.hasEffect(MobEffects.SPEED) && mate.hasEffect(MobEffects.SPEED), "Das Heulen treibt das Rudel nicht an");
                hound.discard();
                mate.discard();
                villager.discard();
                cleanup(helper);
                helper.succeed();
            });
        });
    }

    // =====================================================================================
    // Chorusschlund
    // =====================================================================================

    /**
     * Der Chorusschlund peitscht mit seinen Ranken (Schaden, zieht heran), schnappt zu, wer zu nah kommt, und spuckt nach
     * dem Fernen Chorussamen, die ihr Opfer wie eine Chorusfrucht versetzen (und ihm übel machen); bedrängt verpflanzt er
     * sich ein Stück weiter.
     */
    private static void maw(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper, Blocks.END_STONE);
        VillagerEntity villager = victim(helper, new BlockPos(4, 1, 5));
        ChorusMawEntity maw = helper.spawnWithNoFreeWill(ModEntities.CHORUS_MAW.get(), new BlockPos(4, 1, 1));
        place(helper, maw, 4.5, 1.5, 4.5, 5.5);
        maw.setTarget(villager);
        double before = villager.distanceTo(maw);
        maw.forceLash();
        helper.runAfterDelay(ChorusMawEntity.LASH_HIT + 2, () -> {
            helper.assertTrue(villager.getHealth() < villager.getMaxHealth(), "Der Rankenhieb trifft nicht");
            helper.runAfterDelay(6, () -> {
                helper.assertTrue(villager.distanceTo(maw) < before - 0.2, "Die Ranke zieht nicht heran (" + before + " → "
                        + villager.distanceTo(maw) + ")");
                bite(helper, level, maw, villager);
            });
        });
    }

    private static void bite(GameTestHelper helper, ServerLevel level, ChorusMawEntity maw, VillagerEntity villager) {
        refresh(villager);
        Vec3 v = helper.absoluteVec(new Vec3(4.5, 1.0, 3.3));
        villager.teleportTo(v.x, v.y, v.z);
        villager.setDeltaMovement(Vec3.ZERO);
        helper.runAfterDelay(ChorusMawEntity.LASH_TICKS, () -> {
            maw.forceBite();
            helper.runAfterDelay(ChorusMawEntity.BITE_HIT + 2, () -> {
                helper.assertTrue(villager.getHealth() < villager.getMaxHealth(), "Der Biss trifft nicht");
                helper.runAfterDelay(ChorusMawEntity.BITE_TICKS, () -> spit(helper, level, maw, villager));
            });
        });
    }

    private static void spit(GameTestHelper helper, ServerLevel level, ChorusMawEntity maw, VillagerEntity villager) {
        // erst Ruhe (ohne Ziel beginnt er nichts Neues), dann gilt der Same dem Opfer in der Ferne
        maw.setTarget(null);
        refresh(villager);
        Vec3 v = helper.absoluteVec(new Vec3(4.5, 1.0, 6.5));
        villager.teleportTo(v.x, v.y, v.z);
        villager.setDeltaMovement(Vec3.ZERO);
        helper.runAfterDelay(ChorusMawEntity.LASH_TICKS + 2, () -> {
            place(helper, maw, 4.5, 1.5, 4.5, 6.5);
            maw.setTarget(villager);
            maw.forceSpit();
            seed(helper, level, maw, villager);
        });
    }

    private static void seed(GameTestHelper helper, ServerLevel level, ChorusMawEntity maw, VillagerEntity villager) {
        helper.runAfterDelay(ChorusMawEntity.SPIT_RELEASE + 2, () -> {
            helper.assertFalse(level.getEntitiesOfClass(MonsterShot.class, area(helper), s -> s.kind() == MonsterShot.Kind.CHORUS).isEmpty(),
                    "Kein Chorussame (Geschosse ringsum " + level.getEntitiesOfClass(MonsterShot.class, area(helper).inflate(30.0)).stream()
                            .map(s -> s.kind() + "@" + helper.relativeVec(s.position())).toList() + ", Ziel " + maw.getTarget() + ", Opfer "
                            + villager.isAlive() + " bei " + helper.relativeVec(villager.position()) + ", Schlund bei " + helper.relativeVec(maw.position())
                            + ", offen " + maw.isOpen() + ")");
            helper.runAfterDelay(30, () -> {
                helper.assertTrue(villager.hasEffect(MobEffects.NAUSEA), "Der Chorussame trifft nicht (oder macht nicht übel)");
                // bedrängt verpflanzt er sich: auf festen Boden, ein Stück weiter
                place(helper, maw, 4.5, 4.5, 4.5, 8.5);
                Vec3 from = maw.position();
                boolean moved = false;
                for (int i = 0; i < 4 && !moved; i++) {
                    moved = maw.forceReplant();
                }
                helper.assertTrue(moved && maw.position().distanceTo(from) > 0.5, "Er verpflanzt sich nicht");
                helper.assertTrue(maw.position().distanceTo(from) <= 9.0, "Er verpflanzt sich zu weit (" + maw.position().distanceTo(from) + ")");
                helper.assertTrue(maw.onGround() || !level.getBlockState(maw.blockPosition().below()).isAir(), "Er wurzelt nicht auf festem Boden");
                maw.discard();
                villager.discard();
                cleanup(helper);
                helper.succeed();
            });
        });
    }
}
