package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Butcher;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Conjurer;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.DeepWarden;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Devastator;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Gnarl;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Highwayman;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Tormentor;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.monster.creaking.Creaking;
import net.minecraft.world.entity.monster.illager.Pillager;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.entity.raid.Raids;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.CreakingHeartBlockEntity;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/** Tests der übrigen eigenen Monster (E4b): Gesetzlose, Verwüster, Plagegeister, Riffwächter, Windgeist, Knorz, Tiefenhüter. */
final class OutlawGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SPAWN =
            ModGameTests.TEST_FUNCTIONS.register("outlaw_spawn", () -> OutlawGameTests::spawn);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CONVERSION =
            ModGameTests.TEST_FUNCTIONS.register("outlaw_vanilla_replaced", () -> OutlawGameTests::vanillaReplaced);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> RAID =
            ModGameTests.TEST_FUNCTIONS.register("outlaw_raid", () -> OutlawGameTests::raid);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> VEX_OWNER =
            ModGameTests.TEST_FUNCTIONS.register("outlaw_vex_owner", () -> OutlawGameTests::vexOwner);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CREAKING_HEART =
            ModGameTests.TEST_FUNCTIONS.register("outlaw_creaking_heart", () -> OutlawGameTests::creakingHeart);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> WARDEN_EMERGES =
            ModGameTests.TEST_FUNCTIONS.register("outlaw_warden_emerges", () -> OutlawGameTests::wardenEmerges);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SAVE_LOAD =
            ModGameTests.TEST_FUNCTIONS.register("outlaw_save_load", () -> OutlawGameTests::saveLoad);

    private OutlawGameTests() {}

    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var test : List.of(SPAWN, CONVERSION, RAID, VEX_OWNER, CREAKING_HEART, WARDEN_EMERGES, SAVE_LOAD)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 300, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
    }

    /** Eigene Monster und die Vanilla-Monster, die sie ersetzen. */
    private static Map<EntityType<?>, EntityType<? extends Mob>> pairs() {
        Map<EntityType<?>, EntityType<? extends Mob>> map = new LinkedHashMap<>();
        map.put(EntityTypes.PILLAGER, ModEntities.PILLAGER.get());
        map.put(EntityTypes.VINDICATOR, ModEntities.VINDICATOR.get());
        map.put(EntityTypes.EVOKER, ModEntities.EVOKER.get());
        map.put(EntityTypes.ILLUSIONER, ModEntities.ILLUSIONER.get());
        map.put(EntityTypes.VEX, ModEntities.VEX.get());
        map.put(EntityTypes.RAVAGER, ModEntities.RAVAGER.get());
        map.put(EntityTypes.GUARDIAN, ModEntities.GUARDIAN.get());
        map.put(EntityTypes.ELDER_GUARDIAN, ModEntities.ELDER_GUARDIAN.get());
        map.put(EntityTypes.BREEZE, ModEntities.BREEZE.get());
        map.put(EntityTypes.CREAKING, ModEntities.CREAKING.get());
        map.put(EntityTypes.WARDEN, ModEntities.WARDEN.get());
        return map;
    }

    private static void floor(GameTestHelper helper) {
        for (int x = 0; x < 9; x++) {
            for (int z = 0; z < 9; z++) {
                helper.setBlock(x, 0, z, Blocks.STONE);
            }
        }
    }

    private static AABB arena(GameTestHelper helper) {
        return new AABB(helper.absolutePos(new BlockPos(4, 1, 4))).inflate(6.0);
    }

    private static BlockPos slot(int i) {
        return new BlockPos(1 + (i % 4) * 2, 1, 1 + (i / 4) * 3);
    }

    // =====================================================================================

    /**
     * Alle elf erscheinen und leben; jedes hat Beute, steht in denselben Vanilla-Tags und derselben Kategorie wie sein
     * Gegenstück und trägt den Namen seiner Gestalt.
     */
    private static void spawn(GameTestHelper helper) {
        floor(helper);
        ServerLevel level = helper.getLevel();
        level.getServer().setDifficulty(Difficulty.HARD, true);
        var registries = level.getServer().reloadableRegistries();
        List<Mob> mobs = new ArrayList<>();
        int i = 0;
        for (var pair : pairs().entrySet()) {
            EntityType<? extends Mob> ours = pair.getValue();
            ResourceKey<LootTable> key = ours.getDefaultLootTable().orElseThrow();
            helper.assertTrue(registries.getLootTable(key) != LootTable.EMPTY || pair.getKey() == EntityTypes.VEX
                    || pair.getKey() == EntityTypes.ILLUSIONER || pair.getKey() == EntityTypes.CREAKING, "Beute fehlt: " + key.identifier());
            pair.getKey().builtInRegistryHolder().tags().forEach(tag -> helper.assertTrue(ours.builtInRegistryHolder().is(tag),
                    ours.getDescriptionId() + " fehlt im Tag " + tag.location()));
            helper.assertTrue(ours.getCategory() == pair.getKey().getCategory(), ours.getDescriptionId() + " in falscher Kategorie");
            Mob mob = helper.spawnWithNoFreeWill(ours, slot(i));
            mob.setPersistenceRequired();
            mobs.add(mob);
            i++;
        }
        helper.assertTrue(mobs.get(0) instanceof Raider raider && raider.canBeLeader(), "Wegelagerer kann kein Hauptmann sein");
        Butcher butcher = (Butcher) mobs.get(1);
        butcher.setLook(Butcher.Look.BERSERKER);
        helper.assertTrue(butcher.getName().getString().equals(Component.translatable("entity.kingdomsatwar.vindicator.berserker").getString()),
                "Name des Berserkers: " + butcher.getName().getString());
        helper.runAfterDelay(10, () -> {
            for (Mob mob : mobs) {
                helper.assertTrue(mob.isAlive() && !mob.isRemoved(), mob.getType().getDescriptionId() + " lebt nicht mehr");
            }
            helper.succeed();
        });
    }

    /** Erscheint ein Vanilla-Monster, steht an seiner Stelle das eigene – mit derselben UUID und demselben Namen. */
    private static void vanillaReplaced(GameTestHelper helper) {
        floor(helper);
        ServerLevel level = helper.getLevel();
        level.getServer().setDifficulty(Difficulty.HARD, true);
        Map<UUID, EntityType<?>> expected = new HashMap<>();
        int i = 0;
        for (var pair : pairs().entrySet()) {
            Entity entity = pair.getKey().create(level, EntitySpawnReason.SPAWN_ITEM_USE);
            helper.assertTrue(entity instanceof Mob, "kein " + pair.getKey().getDescriptionId());
            Mob mob = (Mob) entity;
            mob.setCustomName(Component.literal("Probe " + i));
            mob.setNoAi(true);
            mob.setPersistenceRequired();
            BlockPos pos = helper.absolutePos(slot(i));
            mob.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0f, 0.0f);
            expected.put(mob.getUUID(), pair.getValue());
            level.addFreshEntity(mob);
            i++;
        }
        helper.runAfterDelay(2, () -> {
            for (var entry : expected.entrySet()) {
                Entity found = level.getEntity(entry.getKey());
                helper.assertTrue(found != null && found.getType() == entry.getValue(),
                        "statt " + entry.getValue().getDescriptionId() + " steht da " + (found == null ? "nichts" : found.getType().getDescriptionId()));
                helper.assertTrue(found.hasCustomName() && found.getCustomName().getString().startsWith("Probe"), "Name verloren");
            }
            for (Mob mob : level.getEntitiesOfClass(Mob.class, arena(helper))) {
                helper.assertTrue(!pairs().containsKey(mob.getType()), "noch ein Vanilla-Monster: " + mob.getType().getDescriptionId());
            }
            helper.succeed();
        });
    }

    /**
     * Überfall: Wie Raid.spawnGroup es tut, erscheinen ein Wegelagerer (Hauptmann) und ein Verwüster, auf den danach ein
     * Wegelagerer aufsitzt. Alle werden zu eigenen Wesen, bleiben Teil des Überfalls (auch der Hauptmann) und der Reiter
     * sitzt am Ende auf dem eigenen Verwüster.
     */
    private static void raid(GameTestHelper helper) {
        floor(helper);
        ServerLevel level = helper.getLevel();
        level.getServer().setDifficulty(Difficulty.HARD, true);
        BlockPos center = helper.absolutePos(new BlockPos(4, 1, 4));
        Raid raid = new Raid(center, Difficulty.HARD);
        var raidMap = ObfuscationReflectionHelper.<it.unimi.dsi.fastutil.ints.Int2ObjectMap<Raid>, Raids>getPrivateValue(Raids.class,
                level.getRaids(), "raidMap");
        int raidId = 900_000 + level.getRandom().nextInt(10_000);
        raidMap.put(raidId, raid);
        try {
            BlockPos at = helper.absolutePos(new BlockPos(2, 0, 4));
            Pillager captain = EntityTypes.PILLAGER.create(level, EntitySpawnReason.EVENT);
            helper.assertTrue(captain != null, "kein Plünderer");
            captain.setPatrolLeader(true);
            raid.setLeader(1, captain);
            raid.joinRaid(level, 1, captain, at, false);
            Ravager ravager = EntityTypes.RAVAGER.create(level, EntitySpawnReason.EVENT);
            Pillager rider = EntityTypes.PILLAGER.create(level, EntitySpawnReason.EVENT);
            helper.assertTrue(ravager != null && rider != null, "kein Verwüster");
            BlockPos at2 = helper.absolutePos(new BlockPos(6, 0, 4));
            raid.joinRaid(level, 1, ravager, at2, false);
            raid.joinRaid(level, 1, rider, at2, false);
            rider.snapTo(at2, 0.0f, 0.0f);
            rider.startRiding(ravager, false, false);
            // sofort: im Überfall stehen schon die eigenen (dieselben UUIDs), nicht mehr die Vanilla-Originale
            var raiders = raid.getAllRaiders();
            helper.assertTrue(raiders.size() == 3, "Überfall zählt " + raiders.size() + " Mitglieder: " + raiders);
            helper.assertTrue(raiders.stream().allMatch(r -> r instanceof Highwayman || r instanceof Devastator),
                    "Vanilla-Wesen im Überfall: " + raiders);
            helper.assertTrue(raid.getLeader(1) instanceof Highwayman leader && leader.isPatrolLeader()
                    && leader.getUUID().equals(captain.getUUID()), "Hauptmann nicht übernommen: " + raid.getLeader(1));
            helper.assertTrue(raiders.stream().allMatch(r -> r.getCurrentRaid() == raid), "Mitglied ohne Überfall");
            UUID ravagerId = ravager.getUUID();
            UUID riderId = rider.getUUID();
            helper.runAfterDelay(2, () -> {
                Entity mount = level.getEntity(ravagerId);
                Entity seated = level.getEntity(riderId);
                helper.assertTrue(mount instanceof Devastator && seated instanceof Highwayman, "nicht umgewandelt: " + mount + ", " + seated);
                helper.assertTrue(seated.getVehicle() == mount, "Reiter sitzt nicht auf dem Verwüster: " + seated.getVehicle());
                helper.assertTrue(level.getEntitiesOfClass(Mob.class, arena(helper), m -> pairs().containsKey(m.getType())).isEmpty(),
                        "Vanilla-Wesen übrig");
                raidMap.remove(raidId);
                raid.stop();
                helper.succeed();
            });
        } catch (RuntimeException e) {
            raidMap.remove(raidId);
            throw e;
        }
    }

    /** Ein Plagegeist behält Besitzer, Heimat und Lebensdauer und nimmt die Farbe seines Beschwörers an. */
    private static void vexOwner(GameTestHelper helper) {
        floor(helper);
        ServerLevel level = helper.getLevel();
        level.getServer().setDifficulty(Difficulty.HARD, true);
        Conjurer conjurer = helper.spawnWithNoFreeWill(ModEntities.EVOKER.get(), new BlockPos(2, 1, 4));
        conjurer.setLook(Conjurer.Look.RUNEN);
        Vex vex = EntityTypes.VEX.create(level, EntitySpawnReason.MOB_SUMMONED);
        helper.assertTrue(vex != null, "kein Plagegeist");
        BlockPos origin = helper.absolutePos(new BlockPos(5, 2, 4));
        vex.snapTo(origin, 0.0f, 0.0f);
        vex.setOwner(conjurer);
        vex.setBoundOrigin(origin);
        vex.setLimitedLife(1200);
        vex.setNoAi(true);
        UUID id = vex.getUUID();
        level.addFreshEntity(vex);
        // jeden Tick neu prüfen, bis der Plagegeist ersetzt ist (bei voller Testlast kann es einen Tick länger dauern)
        helper.succeedWhen(() -> {
            Tormentor tormentor = level.getEntitiesOfClass(Tormentor.class, new net.minecraft.world.phys.AABB(origin).inflate(6.0)).stream()
                    .findFirst().orElse(null);
            helper.assertTrue(tormentor != null, "kein eigener Plagegeist: " + level.getEntity(id));
            helper.assertTrue(tormentor.getOwner() == conjurer, "Besitzer verloren: " + tormentor.getOwner());
            helper.assertTrue(origin.equals(tormentor.getBoundOrigin()), "Heimat verloren: " + tormentor.getBoundOrigin());
            helper.assertTrue(tormentor.getLook() == Tormentor.Look.TUERKIS, "Farbe nicht die des Runenpriesters: " + tormentor.getLook());
        });
    }

    /**
     * Knarzherz: Wie das Herz es tut, erscheint der Knarz und wird erst danach an das Herz gebunden. Am Ende des Ticks
     * gehören Heimat und Herz dem Knorz – er ist geschützt (unverwundbar) wie der Vanilla-Knarz.
     */
    private static void creakingHeart(GameTestHelper helper) {
        floor(helper);
        ServerLevel level = helper.getLevel();
        level.getServer().setDifficulty(Difficulty.HARD, true);
        BlockPos heartPos = helper.absolutePos(new BlockPos(1, 1, 1));
        helper.setBlock(new BlockPos(1, 1, 1), Blocks.CREAKING_HEART);
        helper.assertTrue(level.getBlockEntity(heartPos) instanceof CreakingHeartBlockEntity, "kein Knarzherz");
        CreakingHeartBlockEntity heart = (CreakingHeartBlockEntity) level.getBlockEntity(heartPos);
        Creaking creaking = EntityTypes.CREAKING.create(level, EntitySpawnReason.SPAWNER);
        helper.assertTrue(creaking != null, "kein Knarz");
        BlockPos at = helper.absolutePos(new BlockPos(5, 1, 5));
        creaking.snapTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, 0.0f, 0.0f);
        creaking.setNoAi(true);
        creaking.setPersistenceRequired();                    // sonst schickt das Herz ihn bei Tag gleich wieder fort
        UUID id = creaking.getUUID();
        level.addFreshEntity(creaking);
        creaking.setTransient(heartPos);
        heart.setCreakingInfo(creaking);
        helper.runAfterDelay(2, () -> {
            Entity found = level.getEntity(id);
            helper.assertTrue(found instanceof Gnarl, "kein Knorz: " + found);
            Gnarl gnarl = (Gnarl) found;
            helper.assertTrue(heartPos.equals(gnarl.getHomePos()), "Heimat verloren: " + gnarl.getHomePos());
            helper.assertTrue(heart.isProtector(gnarl), "Herz kennt den Knorz nicht");
            helper.assertTrue(level.getEntitiesOfClass(Creaking.class, arena(helper), c -> !(c instanceof Gnarl)).isEmpty(), "Vanilla-Knarz übrig");
            helper.succeed();
        });
    }

    /** Ein vom Kreischer gerufener Wärter taucht als Tiefenhüter aus dem Boden auf (Auftauchen bleibt erhalten). */
    private static void wardenEmerges(GameTestHelper helper) {
        floor(helper);
        ServerLevel level = helper.getLevel();
        level.getServer().setDifficulty(Difficulty.HARD, true);
        var warden = EntityTypes.WARDEN.spawn(level, helper.absolutePos(new BlockPos(4, 1, 4)), EntitySpawnReason.TRIGGERED);
        helper.assertTrue(warden != null, "kein Wärter");
        UUID id = warden.getUUID();
        helper.runAfterDelay(3, () -> {
            Entity found = level.getEntity(id);
            helper.assertTrue(found instanceof DeepWarden, "kein Tiefenhüter: " + found);
            DeepWarden deep = (DeepWarden) found;
            helper.assertTrue(deep.getPose() == Pose.EMERGING, "taucht nicht auf: " + deep.getPose());
            helper.assertTrue(deep.getBrain().hasMemoryValue(net.minecraft.world.entity.ai.memory.MemoryModuleType.IS_EMERGING),
                    "Auftauchen vergessen");
            deep.discard();
            helper.succeed();
        });
    }

    /** Gestalt, Hauptmann-Würde und Besitzer überstehen Speichern und Laden. */
    private static void saveLoad(GameTestHelper helper) {
        floor(helper);
        ServerLevel level = helper.getLevel();
        level.getServer().setDifficulty(Difficulty.HARD, true);
        Highwayman highwayman = helper.spawnWithNoFreeWill(ModEntities.PILLAGER.get(), new BlockPos(1, 1, 1));
        highwayman.setLook(Highwayman.Look.NORD);
        highwayman.setPatrolLeader(true);
        Butcher butcher = helper.spawnWithNoFreeWill(ModEntities.VINDICATOR.get(), new BlockPos(3, 1, 1));
        butcher.setLook(Butcher.Look.BERSERKER);
        Conjurer conjurer = helper.spawnWithNoFreeWill(ModEntities.EVOKER.get(), new BlockPos(5, 1, 1));
        conjurer.setLook(Conjurer.Look.RUNEN);
        Devastator devastator = helper.spawnWithNoFreeWill(ModEntities.RAVAGER.get(), new BlockPos(4, 1, 5));
        devastator.setLook(Devastator.Look.STEPPE);
        Tormentor tormentor = helper.spawnWithNoFreeWill(ModEntities.VEX.get(), new BlockPos(7, 2, 1));
        tormentor.setLook(Tormentor.Look.TUERKIS);
        for (Mob mob : List.<Mob>of(highwayman, butcher, conjurer, devastator, tormentor)) {
            TagValueOutput out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
            mob.saveWithoutId(out);
            CompoundTag tag = out.buildResult();
            tag.remove("UUID");
            Mob copy = (Mob) mob.getType().create(level, EntitySpawnReason.LOAD);
            helper.assertTrue(copy != null, "keine Kopie");
            copy.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), tag));
            String original = mob.getName().getString();
            helper.assertTrue(copy.getName().getString().equals(original), "Gestalt verloren: " + original + " -> " + copy.getName().getString());
            if (copy instanceof Highwayman h) {
                helper.assertTrue(h.isPatrolLeader(), "Hauptmann vergessen");
            }
        }
        helper.succeed();
    }
}
