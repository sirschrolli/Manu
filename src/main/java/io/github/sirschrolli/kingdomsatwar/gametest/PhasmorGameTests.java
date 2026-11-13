package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.end.VoidThroneBlock;
import io.github.sirschrolli.kingdomsatwar.block.end.VoidThroneBlockEntity;
import io.github.sirschrolli.kingdomsatwar.entity.end.PhaseBoulderEntity;
import io.github.sirschrolli.kingdomsatwar.entity.end.PhasmorEntity;
import io.github.sirschrolli.kingdomsatwar.entity.end.PhasmorIllusion;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.item.end.PhaseBladeItem;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.PhaseGear;
import io.github.sirschrolli.kingdomsatwar.registry.PhaseSanctuary;
import io.github.sirschrolli.kingdomsatwar.worldgen.site.SiteSamples;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Tests zu Phasmor, dem Leerwandlerkönig (1.5): der Leerenthron (der kniende König, Erwachen, Krönung, Wasser, Sieg und
 * Rückruf mit Phasit), seine Angriffe, die Trugbilder, die Phasenklinge und das Phasenheiligtum.
 */
final class PhasmorGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> THRONE =
            ModGameTests.TEST_FUNCTIONS.register("phasmor_throne", () -> PhasmorGameTests::throne);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ATTACKS =
            ModGameTests.TEST_FUNCTIONS.register("phasmor_attacks", () -> PhasmorGameTests::attacks);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ILLUSIONS =
            ModGameTests.TEST_FUNCTIONS.register("phasmor_illusions", () -> PhasmorGameTests::illusions);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> BLADE =
            ModGameTests.TEST_FUNCTIONS.register("phasmor_blade", () -> PhasmorGameTests::blade);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SANCTUARY =
            ModGameTests.TEST_FUNCTIONS.register("phasmor_sanctuary", () -> PhasmorGameTests::sanctuary);

    private PhasmorGameTests() {}

    /** Lädt die Klasse (meldet die Testfunktionen an). */
    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var test : List.of(BLADE, SANCTUARY)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 200, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
        for (var test : List.of(THRONE, ATTACKS, ILLUSIONS)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 1200, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
    }

    private static void floor(GameTestHelper helper) {
        for (int x = 0; x < 9; x++) {
            for (int z = 0; z < 9; z++) {
                helper.setBlock(x, 0, z, Blocks.END_STONE_BRICKS);
            }
        }
    }

    private static void cleanup(ServerLevel level, AABB area) {
        for (Entity e : level.getEntitiesOfClass(Entity.class, area, e -> !(e instanceof ServerPlayer))) {
            e.discard();
        }
    }

    /** Versetzt den Spieler und meldet ihn an der neuen Stelle an (wie es sonst seine Verbindung jeden Tick tut). */
    private static void move(ServerPlayer player, double x, double y, double z) {
        player.teleportTo(player.level(), x, y, z, java.util.Set.of(), 180.0f, 0.0f, true);
        player.level().getChunkSource().move(player);
    }

    // =====================================================================================
    // Der Leerenthron
    // =====================================================================================

    /**
     * Der Leerenthron: unzerstörbar; kommt jemand in die Nähe, kniet der König schon fünf Blöcke vor dem Thron und hält Wache
     * (unverwundbar); tritt jemand näher heran, erwacht er. Wach ist er verwundbar, unter halber Kraft setzt er die Krone auf
     * (während sie auflodert, unverwundbar), Wasser lässt ihn sich krümmen und schadet ihm. Besiegt erlischt der Thron; er
     * lässt Phasit fallen. Ein Phasit entzündet den Thron neu und ruft ihn zurück.
     */
    private static void throne(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper);
        helper.assertTrue(ModEntities.PHASMOR.get().getCategory() == MobCategory.MONSTER, "Phasmor: falsche Gruppe");
        BlockPos pos = helper.absolutePos(new BlockPos(4, 1, 1));
        level.setBlock(pos, PhaseSanctuary.VOID_THRONE.get().defaultBlockState().setValue(VoidThroneBlock.FACING, Direction.SOUTH), Block.UPDATE_ALL);
        helper.assertTrue(level.getBlockState(pos).getDestroySpeed(level, pos) < 0, "Der Leerenthron lässt sich abbauen");
        if (!(level.getBlockEntity(pos) instanceof VoidThroneBlockEntity throne)) {
            helper.fail("Der Leerenthron hat keine Blockentität");
            return;
        }
        helper.assertTrue(throne.phase() == VoidThroneBlock.Phase.GUARDED, "Der Thron wartet nicht auf seinen König");
        AABB area = new AABB(pos).inflate(7, 6, 9);
        ServerPlayer player = MageGameTests.survivalPlayer(helper, "test-void-pilgrim", new io.netty.channel.embedded.EmbeddedChannel[1], 0);
        move(player, pos.getX() + 0.5, pos.getY(), pos.getZ() + 20.0);
        helper.runAfterDelay(45, () -> {
            var found = level.getEntitiesOfClass(PhasmorEntity.class, area);
            helper.assertTrue(found.size() == 1, "Kein (oder mehr als ein) König vor dem Thron: " + found.size() + " (Thron " + throne.phase() + ")");
            PhasmorEntity king = found.getFirst();
            king.confine(9.0);
            helper.assertTrue(king.isGuarding(), "Er hält keine Wache: " + king.currentAttack());
            helper.assertTrue(throne.owns(king), "Der Thron kennt seinen König nicht");
            helper.assertTrue(king.blockPosition().equals(pos.relative(Direction.SOUTH, 5)), "Er kniet nicht fünf Blöcke vor dem Thron: "
                    + helper.relativePos(king.blockPosition()));
            float hp = king.getHealth();
            king.hurtServer(level, level.damageSources().generic(), 50.0f);
            helper.assertTrue(king.getHealth() >= hp && king.isGuarding(), "Wachend ist er verwundbar");
            // jemand tritt heran: er erwacht
            move(player, pos.getX() + 0.5, pos.getY(), pos.getZ() + 9.0);
            helper.runAfterDelay(14, () -> {
                helper.assertTrue(king.currentAttack() == PhasmorEntity.Attack.AWAKEN, "Er erwacht nicht: " + king.currentAttack());
                helper.assertTrue(throne.phase() == VoidThroneBlock.Phase.AWAKE, "Der Thron merkt das Erwachen nicht");
                helper.assertTrue(level.getBlockState(pos).getValue(VoidThroneBlock.PHASE) == VoidThroneBlock.Phase.AWAKE,
                        "Der Phasit des Throns leuchtet nicht auf");
                float before = king.getHealth();
                king.setInvulnerableTime(0);
                king.hurtServer(level, level.damageSources().generic(), 50.0f);
                helper.assertTrue(king.getHealth() >= before, "Beim Erwachen ist er verwundbar");
                level.getServer().getPlayerList().remove(player);
                helper.runAfterDelay(PhasmorEntity.AWAKEN_TICKS + 4, () -> fight(helper, level, pos, throne, king, area));
            });
        });
    }

    private static void fight(GameTestHelper helper, ServerLevel level, BlockPos pos, VoidThroneBlockEntity throne, PhasmorEntity king, AABB area) {
        helper.assertTrue(king.currentAttack() != PhasmorEntity.Attack.AWAKEN && !king.isGuarding(), "Er erwacht nicht ganz: " + king.currentAttack());
        king.setInvulnerableTime(0);
        float hp = king.getHealth();
        king.hurtServer(level, level.damageSources().generic(), 30.0f);
        helper.assertTrue(king.getHealth() < hp, "Wach ist er unverwundbar");
        king.setHealth(king.getMaxHealth() * 0.45f);
        helper.runAfterDelay(4, () -> {
            helper.assertTrue(king.currentAttack() == PhasmorEntity.Attack.CROWN, "Unter halber Kraft setzt er die Krone nicht auf: "
                    + king.currentAttack());
            float before = king.getHealth();
            king.setInvulnerableTime(0);
            king.hurtServer(level, level.damageSources().generic(), 30.0f);
            helper.assertTrue(king.getHealth() >= before, "Während die Krone auflodert, ist er verwundbar");
            helper.runAfterDelay(PhasmorEntity.Attack.CROWN.duration, () -> {
                helper.assertTrue(king.isCrowned(), "Die Krone lodert nicht auf");
                helper.assertTrue(king.currentAttack() != PhasmorEntity.Attack.CROWN, "Die Krönung endet nicht");
                // Wasser: er krümmt sich und nimmt Schaden
                king.setInvulnerableTime(0);
                float dry = king.getHealth();
                king.hurtServer(level, level.damageSources().drown(), 1.0f);
                helper.assertTrue(king.currentAttack() == PhasmorEntity.Attack.STAGGER, "Wasser lässt ihn sich nicht krümmen: " + king.currentAttack());
                helper.assertTrue(king.getHealth() < dry, "Wasser schadet ihm nicht");
                king.setInvulnerableTime(0);
                king.setHealth(1.0f);
                BossLootGameTests.firstVictory(helper, king, 50.0f);
                helper.assertTrue(king.isDeadOrDying(), "Phasmor fällt nicht");
                helper.assertTrue(throne.phase() == VoidThroneBlock.Phase.DEFEATED, "Der Thron merkt den Sieg nicht");
                helper.assertTrue(level.getBlockState(pos).getValue(VoidThroneBlock.PHASE) == VoidThroneBlock.Phase.DEFEATED,
                        "Der Phasit des Throns erlischt nicht");
                helper.runAfterDelay(PhasmorEntity.DEATH_TICKS + 5, () -> {
                    helper.assertTrue(king.isRemoved(), "Er zerfällt nicht");
                    int phasit = level.getEntitiesOfClass(ItemEntity.class, area, e -> e.getItem().is(PhaseGear.PHASIT.get())).stream()
                            .mapToInt(e -> e.getItem().getCount()).sum();
                    helper.assertTrue(phasit >= 2 && phasit <= 4, "Er lässt nicht zwei bis vier Phasit fallen: " + phasit);
                    // ein Phasit entzündet den Thron neu
                    helper.assertTrue(throne.rekindle(level, null), "Ein Phasit entzündet den Thron nicht");
                    var again = level.getEntitiesOfClass(PhasmorEntity.class, area, LivingEntity::isAlive);
                    helper.assertTrue(again.size() == 1 && again.getFirst().currentAttack() == PhasmorEntity.Attack.AWAKEN,
                            "Der König kehrt nicht zurück");
                    helper.assertTrue(level.getBlockState(pos).getValue(VoidThroneBlock.PHASE) == VoidThroneBlock.Phase.AWAKE,
                            "Der Thron leuchtet nicht wieder");
                    helper.assertFalse(throne.rekindle(level, null), "Der Thron lässt sich zweimal entzünden");
                    // aufräumen (der Thron riefe sonst neue Könige, sobald ein Spieler eines anderen Tests vorbeikommt)
                    cleanup(level, area);
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                    helper.succeed();
                });
            });
        });
    }

    // =====================================================================================
    // Die Angriffe
    // =====================================================================================

    /**
     * Seine Angriffe gegen einen Ritter: Klingenhieb, Phasenstoß, Blockwurf, Leerenrisse (sie verschlucken den Ritter und
     * spucken ihn hoch darüber wieder aus) und – gekrönt – der Phasensturm. (Seine Angriffe treffen im Test nur, wer nah bei
     * ihm ist.)
     */
    private static void attacks(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper);
        PhasmorEntity king = helper.spawn(ModEntities.PHASMOR.get(), new BlockPos(4, 1, 1));
        king.confine(9.0);
        LivingEntity knight = helper.spawnWithNoFreeWill(ModEntities.SOLDIERS.get(SoldierType.KNIGHT).get(), new BlockPos(4, 1, 5));
        Vec3 spot = knight.position();
        AABB area = new AABB(helper.absolutePos(new BlockPos(4, 1, 4))).inflate(8, 12, 8);
        float[] hp = {knight.getHealth()};
        step(helper, king, knight, spot, hp, PhasmorEntity.Attack.SLASH, PhasmorEntity.SLASH_HIT + 3, "Der Klingenhieb", () ->
                step(helper, king, knight, spot, hp, PhasmorEntity.Attack.BLINK, PhasmorEntity.BLINK_HIT + 3, "Der Phasenstoß", () ->
                        hurl(helper, level, king, knight, spot, hp, area, () ->
                                rifts(helper, king, knight, spot, hp, () -> storm(helper, level, king, knight, spot, hp, area)))));
    }

    /** Ein Angriff: erzwingen, warten, prüfen, ob der Ritter getroffen wurde, dann den nächsten. */
    private static void step(GameTestHelper helper, PhasmorEntity king, LivingEntity knight, Vec3 spot, float[] hp, PhasmorEntity.Attack attack,
            int ticks, String name, Runnable then) {
        reset(knight, spot, hp);
        king.setTarget(knight);
        king.forceAttack(attack);
        helper.runAfterDelay(ticks, () -> {
            helper.assertTrue(knight.getHealth() < hp[0], name + " trifft nicht (" + king.currentAttack() + ", Abstand "
                    + String.format(java.util.Locale.ROOT, "%.1f", king.distanceTo(knight)) + ")");
            then.run();
        });
    }

    /** Blockwurf: drei Brocken steigen um ihn auf und fliegen auf den Ritter. */
    private static void hurl(GameTestHelper helper, ServerLevel level, PhasmorEntity king, LivingEntity knight, Vec3 spot, float[] hp, AABB area,
            Runnable then) {
        reset(knight, spot, hp);
        king.setTarget(knight);
        king.forceAttack(PhasmorEntity.Attack.HURL);
        helper.runAfterDelay(PhasmorEntity.HURL_LIFT + 3, () -> {
            int boulders = level.getEntitiesOfClass(PhaseBoulderEntity.class, area).size();
            helper.assertTrue(boulders == PhasmorEntity.HURL_THROWS.length, "Es steigen keine drei Brocken auf: " + boulders);
        });
        helper.runAfterDelay(PhasmorEntity.HURL_THROWS[PhasmorEntity.HURL_THROWS.length - 1] + 20, () -> {
            helper.assertTrue(knight.getHealth() < hp[0], "Die Brocken treffen nicht (" + king.currentAttack() + ")");
            then.run();
        });
    }

    /** Leerenrisse: unter dem Ritter öffnet sich ein Riss; bricht er auf, wird der Ritter verschluckt und hoch darüber ausgespuckt. */
    private static void rifts(GameTestHelper helper, PhasmorEntity king, LivingEntity knight, Vec3 spot, float[] hp, Runnable then) {
        reset(knight, spot, hp);
        king.setTarget(knight);
        king.forceAttack(PhasmorEntity.Attack.RIFTS);
        helper.runAfterDelay(PhasmorEntity.RIFT_OPEN + 2, () -> {
            helper.assertTrue(king.riftsOpen(), "Es öffnen sich keine Leerenrisse");
            helper.assertTrue(king.rifts().stream().anyMatch(p -> p.distanceToSqr(knight.position()) < 0.5),
                    "Unter dem Ritter öffnet sich kein Riss: " + king.rifts());
        });
        helper.runAfterDelay(PhasmorEntity.RIFT_ERUPT + 2, () -> {
            helper.assertTrue(knight.getHealth() < hp[0], "Der Leerenriss trifft nicht");
            helper.assertTrue(knight.getY() > spot.y + 1.5, "Der Riss verschluckt den Ritter nicht (er wird nicht emporgespuckt): "
                    + String.format(java.util.Locale.ROOT, "%.1f", knight.getY() - spot.y));
            helper.assertFalse(king.riftsOpen(), "Die Risse schließen sich nicht");
            then.run();
        });
    }

    /** Gekrönt: der Phasensturm – Sprung um Sprung, nach jedem ein Hieb rundum. */
    private static void storm(GameTestHelper helper, ServerLevel level, PhasmorEntity king, LivingEntity knight, Vec3 spot, float[] hp, AABB area) {
        king.forceCrowned();
        helper.assertTrue(king.isCrowned(), "Er lässt sich nicht krönen");
        reset(knight, spot, hp);
        king.setTarget(knight);
        king.forceAttack(PhasmorEntity.Attack.STORM);
        helper.runAfterDelay(PhasmorEntity.STORM_BLINKS[0] + 4, () -> {
            helper.assertTrue(knight.getHealth() < hp[0], "Der Phasensturm trifft nicht");
            helper.assertTrue(king.distanceTo(knight) < 4.5, "Er springt nicht zum Ritter: "
                    + String.format(java.util.Locale.ROOT, "%.1f", king.distanceTo(knight)));
            cleanup(level, area);
            helper.succeed();
        });
    }

    private static void reset(LivingEntity knight, Vec3 spot, float[] hp) {
        knight.teleportTo(spot.x, spot.y, spot.z);
        knight.setHealth(knight.getMaxHealth());
        knight.setInvulnerableTime(0);
        knight.setDeltaMovement(Vec3.ZERO);
        hp[0] = knight.getHealth();
    }

    // =====================================================================================
    // Die Trugbilder
    // =====================================================================================

    /**
     * Trugbilder: Er ruft drei Abbilder auf einen Kreis um den Ritter und mischt sich unter sie. Jeder Treffer lässt ein
     * Trugbild zerfallen; ist der König fort, zerfallen alle. (Hoch über dem Test auf einer eigenen Plattform – im kleinen
     * Testfeld fänden die Trugbilder keinen Boden.)
     */
    private static void illusions(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        int y = 96;
        for (int x = -2; x <= 10; x++) {
            for (int z = -2; z <= 10; z++) {
                helper.setBlock(x, y, z, Blocks.END_STONE_BRICKS);
            }
        }
        PhasmorEntity king = helper.spawn(ModEntities.PHASMOR.get(), new BlockPos(4, y + 1, 0));
        king.confine(9.0);
        LivingEntity knight = helper.spawnWithNoFreeWill(ModEntities.SOLDIERS.get(SoldierType.KNIGHT).get(), new BlockPos(4, y + 1, 4));
        AABB area = new AABB(helper.absolutePos(new BlockPos(4, y + 1, 4))).inflate(10, 6, 10);
        king.setTarget(knight);
        king.forceAttack(PhasmorEntity.Attack.ILLUSIONS);
        helper.runAfterDelay(PhasmorEntity.ILLUSION_SPAWN + 2, () -> {
            List<PhasmorIllusion> shades = level.getEntitiesOfClass(PhasmorIllusion.class, area);
            helper.assertTrue(shades.size() == 3, "Er ruft keine drei Trugbilder: " + shades.size());
            helper.assertTrue(king.illusionCount() == shades.size(), "Er kennt seine Trugbilder nicht: " + king.illusionCount());
            for (PhasmorIllusion shade : shades) {
                helper.assertTrue(shade.king() == king && shade.isIllusion(), "Ein Trugbild gehört nicht zu ihm");
                helper.assertFalse(shade.shouldBeSaved(), "Trugbilder werden mit der Welt gespeichert");
            }
            double d = king.distanceTo(knight);
            helper.assertTrue(d > 3.0 && d < 7.0, "Er mischt sich nicht unter seine Trugbilder: "
                    + String.format(java.util.Locale.ROOT, "%.1f", d));
            PhasmorIllusion first = shades.getFirst();
            first.hurtServer(level, level.damageSources().generic(), 1.0f);
            helper.assertTrue(first.isRemoved(), "Ein Treffer lässt das Trugbild nicht zerfallen");
            king.discard();
            helper.runAfterDelay(3, () -> {
                helper.assertTrue(level.getEntitiesOfClass(PhasmorIllusion.class, area).isEmpty(), "Ohne König bleiben die Trugbilder");
                cleanup(level, area);
                for (int x = -2; x <= 10; x++) {
                    for (int z = -2; z <= 10; z++) {
                        helper.setBlock(x, y, z, Blocks.AIR);
                    }
                }
                helper.succeed();
            });
        });
    }

    // =====================================================================================
    // Die Phasenklinge
    // =====================================================================================

    /**
     * Die Phasenklinge: verzauberbar wie ein Schwert, mit Phasit zu reparieren. Der Phasenhieb trägt den Spieler in
     * Blickrichtung bis zu sieben Blöcke weit, und wen er durchquert, der nimmt Schaden – durch Wände geht er nicht.
     */
    private static void blade(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper);
        ItemStack stack = new ItemStack(PhaseSanctuary.PHASE_BLADE.get());
        helper.assertTrue(stack.is(ItemTags.SWORDS), "Die Phasenklinge ist nicht wie ein Schwert verzauberbar");
        helper.assertTrue(stack.isValidRepairItem(new ItemStack(PhaseGear.PHASIT.get())), "Phasit repariert sie nicht");
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        BlockPos start = helper.absolutePos(new BlockPos(1, 1, 4));
        player.snapTo(start.getX() + 0.5, start.getY(), start.getZ() + 0.5, -90.0f, 0.0f);
        player.setYHeadRot(-90.0f);
        LivingEntity target = helper.spawnWithNoFreeWill(ModEntities.SOLDIERS.get(SoldierType.SWORDSMAN).get(), new BlockPos(4, 1, 4));
        float hp = target.getHealth();
        double length = PhaseBladeItem.dash(level, player, PhaseBladeItem.DASH_DAMAGE);
        helper.assertTrue(length > 4.0, "Der Phasenhieb trägt nicht weit: " + String.format(java.util.Locale.ROOT, "%.1f", length));
        helper.assertTrue(player.getX() > start.getX() + 4.0, "Der Spieler phast nicht vorwärts");
        helper.assertTrue(target.getHealth() < hp, "Wen er durchquert, nimmt keinen Schaden");
        // vor einer Wand endet der Hieb
        BlockPos wallStart = helper.absolutePos(new BlockPos(1, 1, 1));
        helper.setBlock(3, 1, 1, Blocks.END_STONE_BRICKS);
        helper.setBlock(3, 2, 1, Blocks.END_STONE_BRICKS);
        player.snapTo(wallStart.getX() + 0.5, wallStart.getY(), wallStart.getZ() + 0.5, -90.0f, 0.0f);
        double blocked = PhaseBladeItem.dash(level, player, PhaseBladeItem.DASH_DAMAGE);
        helper.assertTrue(blocked < 1.6 && player.getX() < wallStart.getX() + 2.0, "Der Phasenhieb geht durch Wände: "
                + String.format(java.util.Locale.ROOT, "%.1f", blocked));
        level.getServer().getPlayerList().remove(player);
        target.discard();
        helper.succeed();
    }

    // =====================================================================================
    // Das Phasenheiligtum
    // =====================================================================================

    /**
     * Das Phasenheiligtum: auf dem Absatz der Stufen steht der Leerenthron (wachend, mit Blick nach vorn), unten vor den
     * Stufen ist Platz für den knienden König; neben dem Thron zwei Truhen mit Beute, dahinter die Wand mit dem Auge aus
     * Phasit. Gebaut wird nur ein Ausschnitt um Thron und Stufen, hoch über dem Test (so bleiben die Nachbartests unberührt).
     */
    private static void sanctuary(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos origin = helper.absolutePos(new BlockPos(4, 110, 14));
        var sanctum = SiteSamples.phaseSanctum(origin);
        BlockPos thronePos = SiteSamples.phaseSanctumThrone(sanctum);
        var box = BoundingBox.fromCorners(SiteSamples.phaseSanctumAt(sanctum, -6, -2, -15), SiteSamples.phaseSanctumAt(sanctum, 6, 12, -4));
        sanctum.postProcess(level, level.structureManager(), level.getChunkSource().getGenerator(), net.minecraft.util.RandomSource.create(5), box,
                net.minecraft.world.level.ChunkPos.containing(origin), origin);
        var state = level.getBlockState(thronePos);
        helper.assertTrue(state.is(PhaseSanctuary.VOID_THRONE.get()), "Kein Leerenthron auf den Stufen: " + state);
        helper.assertTrue(state.getValue(VoidThroneBlock.PHASE) == VoidThroneBlock.Phase.GUARDED, "Der Thron wartet nicht auf seinen König");
        helper.assertTrue(state.getValue(VoidThroneBlock.FACING) == Direction.SOUTH, "Der Thron blickt nicht nach vorn: " + state);
        if (!(level.getBlockEntity(thronePos) instanceof VoidThroneBlockEntity throne)) {
            helper.fail("Der Thron hat keine Blockentität");
            return;
        }
        BlockPos home = throne.home();
        helper.assertTrue(home.equals(SiteSamples.phaseSanctumAt(sanctum, 0, 0, -6)), "Der Platz des Königs liegt nicht vor den Stufen: "
                + home.subtract(origin));
        for (BlockPos p : BlockPos.betweenClosed(home, home.above(5))) {
            helper.assertTrue(level.getBlockState(p).isAir(), "Der Platz des Königs ist verbaut: " + level.getBlockState(p));
        }
        helper.assertTrue(level.getBlockState(home.below()).isSolidRender(), "Unter dem König ist kein Boden");
        helper.assertTrue(level.getBlockState(SiteSamples.phaseSanctumAt(sanctum, 0, 0, -8)).is(Blocks.END_STONE_BRICK_STAIRS),
                "Keine erste Stufe vor dem Thron");
        helper.assertTrue(level.getBlockState(SiteSamples.phaseSanctumAt(sanctum, 0, 1, -9)).is(Blocks.PURPUR_STAIRS), "Keine zweite Stufe");
        for (int x : new int[] {-3, 3}) {
            BlockPos chest = SiteSamples.phaseSanctumAt(sanctum, x, 3, -12);
            helper.assertTrue(level.getBlockEntity(chest) instanceof ChestBlockEntity c && c.getLootTable() != null,
                    "Keine Truhe mit Beute neben dem Thron bei " + x);
        }
        helper.assertTrue(level.getBlockState(SiteSamples.phaseSanctumAt(sanctum, 2, 7, -14)).is(PhaseGear.PHASIT_BLOCK.get()),
                "Kein Auge aus Phasit in der Wand hinter dem Thron");
        helper.assertFalse(level.getBlockState(SiteSamples.phaseSanctumAt(sanctum, 0, 7, -14)).is(PhaseGear.PHASIT_BLOCK.get()),
                "Das Auge hat keine Pupille");
        // (der Thron riefe sonst einen König, sobald ein Spieler eines anderen Tests in die Nähe käme)
        for (BlockPos p : BlockPos.betweenClosed(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ())) {
            level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        // gedreht blickt der Thron mit – der König kniet immer zwischen Thron und Mitte
        var turned = SiteSamples.phaseSanctum(helper.absolutePos(new BlockPos(-7, 126, 4)), Rotation.CLOCKWISE_90);
        BlockPos turnedThrone = SiteSamples.phaseSanctumThrone(turned);
        var small = BoundingBox.fromCorners(turnedThrone.offset(-1, -1, -1), turnedThrone.offset(1, 1, 1));
        turned.postProcess(level, level.structureManager(), level.getChunkSource().getGenerator(), net.minecraft.util.RandomSource.create(5), small,
                net.minecraft.world.level.ChunkPos.containing(turnedThrone), turnedThrone);
        var turnedState = level.getBlockState(turnedThrone);
        helper.assertTrue(turnedState.is(PhaseSanctuary.VOID_THRONE.get()) && turnedState.getValue(VoidThroneBlock.FACING) == Direction.WEST,
                "Im gedrehten Heiligtum blickt der Thron nicht zur Mitte: " + turnedState);
        for (BlockPos p : BlockPos.betweenClosed(small.minX(), small.minY(), small.minZ(), small.maxX(), small.maxY(), small.maxZ())) {
            level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        helper.succeed();
    }
}
