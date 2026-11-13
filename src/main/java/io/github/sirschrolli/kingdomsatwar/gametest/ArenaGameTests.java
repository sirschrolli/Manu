package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.arena.ArcaneSphere;
import io.github.sirschrolli.kingdomsatwar.arena.ArenaFights;
import io.github.sirschrolli.kingdomsatwar.arena.MageArena;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.entity.boss.mage.MageBossEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.worldgen.tower.MageArenaBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Tests zur Arkanen Sphäre: Die Arena steht (fester Boden unter jedem Startplatz, Brüstung, Feuerschalen, Inseln); in der
 * Sphäre lässt sich nichts bauen; wer dort stirbt, verliert alles; ein Erzmagier zieht Spieler und ihre Soldaten hinein,
 * nach dem Sieg kehren alle zurück, nach der Niederlage kehrt der Magier geheilt in seinen Turm zurück.
 */
final class ArenaGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> BUILDER =
            ModGameTests.TEST_FUNCTIONS.register("arena_builder", () -> ArenaGameTests::builder);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> RULES =
            ModGameTests.TEST_FUNCTIONS.register("arena_rules", () -> ArenaGameTests::rules);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FIGHT =
            ModGameTests.TEST_FUNCTIONS.register("arena_fight", () -> ArenaGameTests::fight);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> HOMECOMING =
            ModGameTests.TEST_FUNCTIONS.register("arena_homecoming", () -> ArenaGameTests::homecoming);

    private ArenaGameTests() {}

    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var test : List.of(BUILDER, RULES, FIGHT, HOMECOMING)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 400, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
    }

    /** Die Arena: Boden mit Lichtstein in der Mitte, fester Boden und Luft an jedem Startplatz, Brüstung, Feuerschalen, Inseln. */
    private static void builder(GameTestHelper helper) {
        // der Testserver lädt nur die Grunddimensionen: gebaut wird hoch über der Oberwelt, weit draußen
        ServerLevel arena = helper.getLevel();
        BlockPos base = helper.absolutePos(BlockPos.ZERO);
        BlockPos center = new BlockPos(base.getX() - 5000, 200, base.getZ() + 3000);
        for (long[] chunk : MageArenaBuilder.chunks(center)) {
            arena.getChunk((int) chunk[0], (int) chunk[1]);
        }
        MageArenaBuilder.build(arena, center, DragonType.ICE);
        List<String> problems = new ArrayList<>();
        if (arena.getBlockState(center).getLightEmission() < 10) {
            problems.add("kein Lichtstein in der Mitte: " + arena.getBlockState(center));
        }
        for (Vec3 spot : MageArenaBuilder.spawnPoints(center, 20)) {
            BlockPos feet = BlockPos.containing(spot);
            if (!arena.getBlockState(feet.below()).isSolid() || !arena.getBlockState(feet).isAir() || !arena.getBlockState(feet.above()).isAir()) {
                problems.add("Startplatz " + feet.subtract(center) + " ohne Boden oder verbaut");
            }
            if (spot.subtract(Vec3.atBottomCenterOf(center)).horizontalDistance() > MageArenaBuilder.RADIUS - 2) {
                problems.add("Startplatz zu nah am Rand: " + feet.subtract(center));
            }
        }
        int rim = 0;
        int glow = 0;
        int island = 0;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-MageArenaBuilder.REACH, -34, -MageArenaBuilder.REACH),
                center.offset(MageArenaBuilder.REACH, 24, MageArenaBuilder.REACH))) {
            var state = arena.getBlockState(pos);
            if (state.isAir()) {
                continue;
            }
            double d = Math.sqrt(Math.pow(pos.getX() - center.getX(), 2) + Math.pow(pos.getZ() - center.getZ(), 2));
            if (pos.getY() == center.getY() + 1 && d > MageArenaBuilder.RADIUS - 1.5) {
                rim++;
            }
            if (state.getLightEmission() >= 10) {
                glow++;
            }
            if (d > 28) {
                island++;
            }
        }
        if (rim < 90) {
            problems.add("Brüstung lückenhaft: " + rim);
        }
        if (glow < 30) {
            problems.add("zu wenig Licht: " + glow);
        }
        if (island < 150) {
            problems.add("keine Inseln: " + island);
        }
        helper.assertTrue(problems.isEmpty(), "Arena: " + problems);
        helper.succeed();
    }

    /** In der Sphäre lässt sich nichts bauen (außer im Kreativmodus); wer dort stirbt, verliert alles. */
    private static void rules(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = MageGameTests.survivalPlayer(helper);
        ServerPlayer builder = helper.makeMockServerPlayerInLevel();
        try {
            boolean here = MageArena.forbidden(player, level);
            boolean there = MageArena.forbidden(player, ArcaneSphere.LEVEL);
            boolean creative = MageArena.forbidden(builder, ArcaneSphere.LEVEL);
            player.getInventory().add(new ItemStack(Items.DIAMOND, 12));
            player.getInventory().add(new ItemStack(Items.IRON_SWORD));
            player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
            player.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD));
            MageArena.loseEverything(player);
            boolean empty = player.getInventory().isEmpty() && player.getItemBySlot(EquipmentSlot.HEAD).isEmpty()
                    && player.getItemBySlot(EquipmentSlot.OFFHAND).isEmpty();
            helper.assertFalse(here, "Bauverbot auch außerhalb der Sphäre");
            helper.assertTrue(there, "kein Bauverbot in der Sphäre");
            helper.assertFalse(creative, "Bauverbot auch im Kreativmodus");
            helper.assertTrue(empty, "nach dem Tod in der Sphäre bleiben Gegenstände");
        } finally {
            level.getServer().getPlayerList().remove(player);
            level.getServer().getPlayerList().remove(builder);
        }
        helper.succeed();
    }

    /**
     * Der ganze Kampf: Der Erzmagier zieht den Spieler und seinen Soldaten in die Sphäre und erwacht dort; nach dem Sieg
     * kehren beide an ihre Plätze zurück. Ein zweiter Kampf ohne Gegner endet mit der Rückkehr des Magiers in den Turm.
     */
    private static void fight(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        // der Testserver lädt nur die Grunddimensionen: die Arena entsteht hoch über der Oberwelt, weit draußen
        BlockPos base = helper.absolutePos(BlockPos.ZERO);
        BlockPos arenaCenter = new BlockPos(base.getX() + 6000, 200, base.getZ() - 4000);
        BlockPos arenaCenter2 = new BlockPos(base.getX() + 6000, 200, base.getZ() - 6000);
        // erst die Chunks beider Arenen samt ihrer Wesen laden (unter Last dauert das ein paar Ticks)
        helper.startSequence()
                .thenExecute(() -> {
                    for (BlockPos c : List.of(arenaCenter, arenaCenter2)) {
                        for (long[] chunk : MageArenaBuilder.chunks(c)) {
                            level.getChunk((int) chunk[0], (int) chunk[1]);
                            level.setChunkForced((int) chunk[0], (int) chunk[1], true);
                        }
                    }
                })
                .thenWaitUntil(() -> helper.assertTrue(MageArena.entitiesLoaded(level, arenaCenter) && MageArena.entitiesLoaded(level, arenaCenter2),
                        "Arena-Chunks laden nicht"))
                .thenExecute(() -> fightBody(helper, arenaCenter, arenaCenter2))
                .thenSucceed();
    }

    private static void fightBody(GameTestHelper helper, BlockPos arenaCenter, BlockPos arenaCenter2) {
        ServerLevel level = helper.getLevel();
        ServerLevel arena = level;
        for (int x = 0; x < 9; x++) {
            for (int z = 0; z < 9; z++) {
                helper.setBlock(x, 0, z, Blocks.STONE);
            }
        }
        BlockPos home = helper.absolutePos(new BlockPos(4, 1, 4));
        MageBossEntity mage = ModEntities.MAGES.get(DragonType.EARTH).get().create(level, EntitySpawnReason.MOB_SUMMONED);
        helper.assertTrue(mage != null, "kein Erzmagier");
        mage.snapTo(home.getX() + 0.5, home.getY(), home.getZ() + 0.5, 0.0f, 0.0f);
        mage.setHome(home);
        level.addFreshEntity(mage);
        ServerPlayer player = MageGameTests.survivalPlayer(helper);
        Vec3 start = Vec3.atBottomCenterOf(home.offset(0, 0, 3));
        player.teleportTo(level, start.x, start.y, start.z, java.util.Set.of(), 0.0f, 0.0f, true);
        SoldierEntity soldier = ModEntities.SOLDIERS.get(SoldierType.SWORDSMAN).get().create(level, EntitySpawnReason.MOB_SUMMONED);
        helper.assertTrue(soldier != null, "kein Soldat");
        soldier.snapTo(home.getX() + 2.5, home.getY(), home.getZ() + 3.5, 0.0f, 0.0f);
        soldier.setOwner(player);
        level.addFreshEntity(soldier);
        List<String> problems = new ArrayList<>();
        ArenaFights fights = ArenaFights.of(level.getServer());
        try {
            ArenaFights.Fight fight = MageArena.pull(level, mage, arena, 900, arenaCenter);
            helper.assertTrue(fight != null, "der Magier zieht niemanden in die Sphäre");
            if (!MageArena.inArena(player, arena, fight)) {
                problems.add("Spieler nicht in der Arena: " + player.position());
            }
            var soldierThere = arena.getEntity(soldier.getUUID());
            if (!(soldierThere instanceof SoldierEntity) || !MageArena.inArena(soldierThere, arena, fight)) {
                problems.add("Soldat nicht in der Arena");
            }
            if (!(arena.getEntity(fight.mage()) instanceof MageBossEntity arcane) || arcane.state() == MageBossEntity.State.DORMANT
                    || !fight.center().above().equals(arcane.home())) {
                problems.add("Magier nicht erwacht in der Arena");
            } else {
                // Sieg: der Magier fällt, alle kehren zurück
                arcane.devFight(arena);
                arcane.setInvulnerableTime(0);
                arcane.hurtServer(arena, arena.damageSources().playerAttack(player), 100000.0f);
                if (!fight.won()) {
                    problems.add("Sieg nicht vermerkt");
                }
                MageArena.finish(level.getServer(), arena, fights, fight, true);
                if (player.position().distanceTo(start) > 1.0) {
                    problems.add("Spieler nicht zurück an seinem Platz: " + player.position());
                }
                var soldierBack = level.getEntity(soldier.getUUID());
                if (!(soldierBack instanceof SoldierEntity) || soldierBack.position().distanceTo(new Vec3(home.getX() + 2.5, home.getY(),
                        home.getZ() + 3.5)) > 1.0) {
                    problems.add("Soldat nicht zurück");
                }
                if (fights.byMage(fight.mage()) != null) {
                    problems.add("Kampf nicht beendet");
                }
            }
            // Niederlage: ein zweiter Magier, kein Gegner bleibt in der Arena – er kehrt geheilt heim
            MageBossEntity second = ModEntities.MAGES.get(DragonType.WATER).get().create(level, EntitySpawnReason.MOB_SUMMONED);
            second.snapTo(home.getX() + 0.5, home.getY(), home.getZ() + 0.5, 0.0f, 0.0f);
            second.setHome(home);
            level.addFreshEntity(second);
            player.teleportTo(level, start.x, start.y, start.z, java.util.Set.of(), 0.0f, 0.0f, true);
            ArenaFights.Fight lost = MageArena.pull(level, second, arena, 901, arenaCenter2);
            helper.assertTrue(lost != null, "zweiter Kampf beginnt nicht");
            player.teleportTo(level, start.x, start.y, start.z, java.util.Set.of(), 0.0f, 0.0f, true);
            MageArena.tick(level.getServer(), arena, fights, lost);
            if (!(level.getEntity(lost.mage()) instanceof MageBossEntity back) || back.state() != MageBossEntity.State.DORMANT
                    || back.getHealth() < back.getMaxHealth() || !home.equals(back.home())) {
                problems.add("Magier kehrt nach der Niederlage nicht geheilt in den Turm zurück");
            } else {
                back.discard();
            }
            if (fights.byMage(lost.mage()) != null) {
                problems.add("verlorener Kampf nicht beendet");
            }
        } finally {
            for (ArenaFights.Fight left : fights.all()) {
                fights.remove(left);
            }
            fights.save(level.getServer());
            if (level.getEntity(soldier.getUUID()) instanceof SoldierEntity s) {
                s.discard();
            }
            if (arena.getEntity(soldier.getUUID()) instanceof SoldierEntity s) {
                s.discard();
            }
            level.getServer().getPlayerList().remove(player);
        }
        helper.assertTrue(problems.isEmpty(), "Kampf in der Sphäre: " + problems);
        for (BlockPos c : List.of(arenaCenter, arenaCenter2)) {
            for (long[] chunk : MageArenaBuilder.chunks(c)) {
                level.setChunkForced((int) chunk[0], (int) chunk[1], false);
            }
        }
    }

    /**
     * Der Magier findet immer heim: Sind die Wesen der Arena (etwa nach einem Neustart) noch nicht geladen, gilt der Kampf
     * nicht als gewonnen; der Magier merkt sich seinen Turm (auch über das Speichern hinweg) und kehrt geheilt und
     * meditierend an seinen Platz zurück.
     */
    private static void homecoming(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        List<String> problems = new ArrayList<>();
        ArenaFights fights = ArenaFights.of(level.getServer());
        BlockPos home = helper.absolutePos(new BlockPos(4, 1, 4));
        for (int x = 0; x < 9; x++) {
            for (int z = 0; z < 9; z++) {
                helper.setBlock(x, 0, z, Blocks.STONE);
            }
        }
        // Kampf in einer Arena, deren Chunks (noch) nicht geladen sind: kein Sieg, nur weil der Magier nicht zu finden ist
        BlockPos far = new BlockPos(home.getX() + 40_000, 200, home.getZ() - 40_000);
        ArenaFights.Fight waiting = new ArenaFights.Fight(905, far, DragonType.FIRE, java.util.UUID.randomUUID(), Level.OVERWORLD, home,
                List.of(), level.getGameTime(), false, 0L);
        fights.add(waiting);
        MageBossEntity mage = ModEntities.MAGES.get(DragonType.FIRE).get().create(level, EntitySpawnReason.MOB_SUMMONED);
        MageBossEntity copy = ModEntities.MAGES.get(DragonType.FIRE).get().create(level, EntitySpawnReason.MOB_SUMMONED);
        helper.assertTrue(mage != null && copy != null, "kein Erzmagier");
        try {
            MageArena.tick(level.getServer(), level, fights, waiting);
            if (waiting.won()) {
                problems.add("Kampf gilt als gewonnen, obwohl die Arena noch nicht geladen ist");
            }
            if (fights.byMage(waiting.mage()) == null) {
                problems.add("Kampf verschwunden, obwohl die Arena noch nicht geladen ist");
            }
            // der Turm wird gemerkt und übersteht das Speichern
            mage.setTower(Level.OVERWORLD, home);
            copy.restoreFrom(mage);
            if (!home.equals(copy.towerHome())) {
                problems.add("Turm nach dem Laden vergessen: " + copy.towerHome());
            }
            // Heimkehr: geheilt, meditierend, an seinem Platz
            mage.snapTo(home.getX() + 3.5, home.getY(), home.getZ() + 2.5, 90.0f, 0.0f);
            mage.setHome(home.offset(3, 0, 2));
            level.addFreshEntity(mage);
            mage.devFight(level);
            mage.setHealth(mage.getMaxHealth() * 0.3f);
            MageBossEntity back = MageArena.returnHome(mage, level, home);
            if (back == null) {
                problems.add("Heimkehr gescheitert");
            } else {
                if (back.position().distanceTo(Vec3.atBottomCenterOf(home)) > 1.0) {
                    problems.add("nicht an seinem Platz: " + back.position());
                }
                if (back.state() != MageBossEntity.State.DORMANT || back.getHealth() < back.getMaxHealth() || !home.equals(back.home())) {
                    problems.add("nicht geheilt und meditierend daheim: " + back.state() + " " + back.getHealth());
                }
                if (back.towerHome() != null) {
                    problems.add("Turm nach der Heimkehr noch gemerkt");
                }
                back.discard();
            }
        } finally {
            fights.remove(waiting);
            fights.save(level.getServer());
            mage.discard();
        }
        helper.assertTrue(problems.isEmpty(), "Heimkehr des Magiers: " + problems);
        helper.succeed();
    }
}
