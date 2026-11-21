package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.nether.FlameAltarBlock;
import io.github.sirschrolli.kingdomsatwar.block.nether.FlameAltarBlockEntity;
import io.github.sirschrolli.kingdomsatwar.entity.creature.nether.Glutgeist;
import io.github.sirschrolli.kingdomsatwar.entity.nether.IgnaraEntity;
import io.github.sirschrolli.kingdomsatwar.entity.nether.OpalShardEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.item.nether.FlameWhipItem;
import io.github.sirschrolli.kingdomsatwar.registry.FlameGear;
import io.github.sirschrolli.kingdomsatwar.registry.FlameSanctuary;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.worldgen.site.SiteSamples;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Tests zu Ignara, der Flammenpriesterin (1.2): der Flammenaltar (betende Priesterin, Erwachen, Phönixgestalt, Sieg und
 * Rückruf mit einem Feueropal), ihre Angriffe, die Flammenpeitsche und das Flammenheiligtum.
 */
final class IgnaraGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ALTAR =
            ModGameTests.TEST_FUNCTIONS.register("ignara_altar", () -> IgnaraGameTests::altar);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ATTACKS =
            ModGameTests.TEST_FUNCTIONS.register("ignara_attacks", () -> IgnaraGameTests::attacks);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> WHIP =
            ModGameTests.TEST_FUNCTIONS.register("ignara_whip", () -> IgnaraGameTests::whip);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SANCTUARY =
            ModGameTests.TEST_FUNCTIONS.register("ignara_sanctuary", () -> IgnaraGameTests::sanctuary);

    private IgnaraGameTests() {}

    /** Lädt die Klasse (meldet die Testfunktionen an). */
    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var test : List.of(WHIP, SANCTUARY)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 200, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
        for (var test : List.of(ALTAR, ATTACKS)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 1200, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
    }

    private static void floor(GameTestHelper helper) {
        for (int x = 0; x < 9; x++) {
            for (int z = 0; z < 9; z++) {
                helper.setBlock(x, 0, z, Blocks.POLISHED_BLACKSTONE_BRICKS);
            }
        }
    }

    private static void cleanup(ServerLevel level, AABB area) {
        for (var e : level.getEntitiesOfClass(net.minecraft.world.entity.Entity.class, area, e -> !(e instanceof ServerPlayer))) {
            e.discard();
        }
    }

    /** Versetzt den Spieler und meldet ihn an der neuen Stelle an (wie es sonst seine Verbindung jeden Tick tut). */
    private static void move(ServerPlayer player, double x, double y, double z) {
        player.teleportTo(player.level(), x, y, z, java.util.Set.of(), 180.0f, 0.0f, true);
        player.level().getChunkSource().move(player);
    }

    // =====================================================================================
    // Der Flammenaltar
    // =====================================================================================

    /**
     * Der Flammenaltar: unzerstörbar; kommt jemand in die Nähe, schwebt Ignara schon betend vor dem Altar (unverwundbar);
     * tritt jemand näher heran, erwacht sie. Danach ist sie verwundbar (doch nicht durch Feuer), unter halber Kraft wird sie
     * zum Phönix, und besiegt erlischt das ewige Feuer; sie lässt Feueropale fallen und beim ersten Sieg einen Herzkristall.
     * Ein Feueropal entzündet den Altar neu und ruft sie zurück.
     */
    private static void altar(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper);
        helper.assertTrue(ModEntities.IGNARA.get().getCategory() == MobCategory.MONSTER, "Ignara: falsche Gruppe");
        BlockPos pos = helper.absolutePos(new BlockPos(4, 1, 1));
        level.setBlock(pos, FlameSanctuary.FLAME_ALTAR.get().defaultBlockState().setValue(FlameAltarBlock.FACING, Direction.SOUTH), Block.UPDATE_ALL);
        helper.assertTrue(level.getBlockState(pos).getDestroySpeed(level, pos) < 0, "Der Flammenaltar lässt sich abbauen");
        if (!(level.getBlockEntity(pos) instanceof FlameAltarBlockEntity altar)) {
            helper.fail("Der Flammenaltar hat keine Blockentität");
            return;
        }
        helper.assertTrue(altar.phase() == FlameAltarBlock.Phase.PRAYING, "Der Altar wartet nicht auf die Priesterin");
        AABB area = new AABB(pos).inflate(6, 5, 7);
        ServerPlayer player = MageGameTests.survivalPlayer(helper, "test-flame-pilgrim", new io.netty.channel.embedded.EmbeddedChannel[1], 0);
        move(player, pos.getX() + 0.5, pos.getY(), pos.getZ() + 15.0);
        helper.runAfterDelay(45, () -> {
            var found = level.getEntitiesOfClass(IgnaraEntity.class, area);
            helper.assertTrue(found.size() == 1, "Keine (oder mehr als eine) Priesterin vor dem Altar: " + found.size() + " (Altar " + altar.phase() + ")");
            IgnaraEntity ignara = found.getFirst();
            ignara.confine(9.0);
            helper.assertTrue(ignara.isPraying(), "Sie betet nicht: " + ignara.currentAttack());
            helper.assertTrue(altar.owns(ignara), "Der Altar kennt seine Priesterin nicht");
            helper.assertTrue(ignara.blockPosition().equals(pos.relative(Direction.SOUTH, 2)), "Sie schwebt nicht vor dem Altar: "
                    + helper.relativePos(ignara.blockPosition()));
            float hp = ignara.getHealth();
            ignara.hurtServer(level, level.damageSources().generic(), 50.0f);
            helper.assertTrue(ignara.getHealth() >= hp && ignara.isPraying(), "Im Gebet ist sie verwundbar");
            // jemand tritt heran: sie erwacht
            move(player, pos.getX() + 0.5, pos.getY(), pos.getZ() + 7.0);
            helper.runAfterDelay(14, () -> {
                helper.assertTrue(ignara.currentAttack() == IgnaraEntity.Attack.AWAKEN, "Sie erwacht nicht: " + ignara.currentAttack());
                helper.assertTrue(altar.phase() == FlameAltarBlock.Phase.AWAKE, "Der Altar merkt das Erwachen nicht");
                helper.assertTrue(level.getBlockState(pos).getValue(FlameAltarBlock.PHASE) == FlameAltarBlock.Phase.AWAKE,
                        "Die Flammen des Altars lodern nicht auf");
                level.getServer().getPlayerList().remove(player);
                helper.runAfterDelay(IgnaraEntity.AWAKEN_TICKS + 4, () -> fight(helper, level, pos, altar, ignara, area));
            });
        });
    }

    private static void fight(GameTestHelper helper, ServerLevel level, BlockPos pos, FlameAltarBlockEntity altar, IgnaraEntity ignara, AABB area) {
        helper.assertTrue(ignara.currentAttack() != IgnaraEntity.Attack.AWAKEN && !ignara.isPraying(),
                "Sie erwacht nicht ganz: " + ignara.currentAttack());
        ignara.setInvulnerableTime(0);
        float hp = ignara.getHealth();
        ignara.hurtServer(level, level.damageSources().onFire(), 20.0f);
        ignara.hurtServer(level, level.damageSources().lava(), 20.0f);
        helper.assertTrue(ignara.getHealth() >= hp, "Feuer oder Lava schaden ihr");
        ignara.setInvulnerableTime(0);
        ignara.hurtServer(level, level.damageSources().generic(), 20.0f);
        helper.assertTrue(ignara.getHealth() < hp, "Wach ist sie unverwundbar");
        ignara.setHealth(ignara.getMaxHealth() * 0.45f);
        helper.runAfterDelay(4, () -> {
            helper.assertTrue(ignara.currentAttack() == IgnaraEntity.Attack.PHOENIX, "Unter halber Kraft wird sie nicht zum Phönix: "
                    + ignara.currentAttack());
            float before = ignara.getHealth();
            ignara.setInvulnerableTime(0);
            ignara.hurtServer(level, level.damageSources().generic(), 20.0f);
            helper.assertTrue(ignara.getHealth() >= before, "Während der Verwandlung ist sie verwundbar");
            helper.runAfterDelay(30, () -> {
                helper.assertTrue(ignara.isPhoenix(), "Ihr wachsen keine Schwingen aus Flammen");
                ignara.setInvulnerableTime(0);
                ignara.setHealth(1.0f);
                BossLootGameTests.firstVictory(helper, ignara, 50.0f);
                helper.assertTrue(ignara.isDeadOrDying(), "Ignara fällt nicht");
                helper.assertTrue(altar.phase() == FlameAltarBlock.Phase.DEFEATED, "Der Altar merkt den Sieg nicht");
                helper.assertTrue(level.getBlockState(pos).getValue(FlameAltarBlock.PHASE) == FlameAltarBlock.Phase.DEFEATED,
                        "Das ewige Feuer erlischt nicht");
                helper.runAfterDelay(IgnaraEntity.DEATH_TICKS + 5, () -> {
                    helper.assertTrue(ignara.isRemoved(), "Sie zerfällt nicht");
                    int opals = level.getEntitiesOfClass(ItemEntity.class, area, e -> e.getItem().is(FlameGear.FIRE_OPAL.get())).stream()
                            .mapToInt(e -> e.getItem().getCount()).sum();
                    helper.assertTrue(opals >= 2 && opals <= 4, "Sie lässt nicht zwei bis vier Feueropale fallen: " + opals);
                    // ein Feueropal entzündet den Altar neu
                    helper.assertTrue(altar.rekindle(level, null), "Ein Feueropal entzündet den Altar nicht");
                    var again = level.getEntitiesOfClass(IgnaraEntity.class, area, LivingEntity::isAlive);
                    helper.assertTrue(again.size() == 1 && again.getFirst().currentAttack() == IgnaraEntity.Attack.AWAKEN,
                            "Die Priesterin kehrt nicht zurück");
                    helper.assertTrue(level.getBlockState(pos).getValue(FlameAltarBlock.PHASE) == FlameAltarBlock.Phase.AWAKE,
                            "Der Altar lodert nicht wieder");
                    helper.assertFalse(altar.rekindle(level, null), "Der Altar lässt sich zweimal entzünden");
                    // aufräumen (der Altar riefe sonst neue Priesterinnen, sobald ein Spieler eines anderen Tests vorbeikommt)
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
     * Ihre Angriffe gegen einen Ritter: Peitschenhieb, Lavawoge, Feuersäulen, Flammenschlinge, Opalregen, der Ruf der
     * Glutgeister und – als Phönix – der Sturzflug. (Ihre Angriffe treffen im Test nur, wer nah bei ihr ist.)
     */
    private static void attacks(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper);
        IgnaraEntity ignara = helper.spawn(ModEntities.IGNARA.get(), new BlockPos(4, 1, 1));
        ignara.confine(9.0);
        LivingEntity knight = helper.spawnWithNoFreeWill(ModEntities.SOLDIERS.get(SoldierType.KNIGHT).get(), new BlockPos(4, 1, 5));
        AABB area = new AABB(helper.absolutePos(new BlockPos(4, 1, 4))).inflate(8, 6, 8);
        float[] hp = {knight.getHealth()};
        step(helper, ignara, knight, hp, IgnaraEntity.Attack.LASH, 16, "Der Peitschenhieb", true, () ->
                step(helper, ignara, knight, hp, IgnaraEntity.Attack.WAVE, 34, "Die Lavawoge", true, () ->
                        step(helper, ignara, knight, hp, IgnaraEntity.Attack.PILLARS, IgnaraEntity.PILLAR_ERUPT + 3, "Die Feuersäule", true, () ->
                                snare(helper, ignara, knight, hp, () -> rain(helper, level, ignara, knight, hp, area)))));
    }

    /**
     * Die Flammenschlinge erwischt den Ritter: er brennt und wird gebremst, während sie ihn heranzieht (der Ritter hat keinen
     * eigenen Willen und bleibt stehen – heranziehen lässt er sich so nicht).
     */
    private static void snare(GameTestHelper helper, IgnaraEntity ignara, LivingEntity knight, float[] hp, Runnable then) {
        reset(knight, hp);
        knight.removeAllEffects();
        ignara.setTarget(knight);
        ignara.forceAttack(IgnaraEntity.Attack.SNARE);
        helper.runAfterDelay(IgnaraEntity.SNARE_THROW + 4, () -> {
            helper.assertTrue(knight.isOnFire() && knight.hasEffect(net.minecraft.world.effect.MobEffects.SLOWNESS),
                    "Die Flammenschlinge erwischt den Ritter nicht");
            helper.runAfterDelay(IgnaraEntity.SNARE_STRIKE - IgnaraEntity.SNARE_THROW + 2, then::run);
        });
    }

    /** Ein Angriff: erzwingen, warten, prüfen, ob der Ritter getroffen wurde (und brennt), dann den nächsten. */
    private static void step(GameTestHelper helper, IgnaraEntity ignara, LivingEntity knight, float[] hp, IgnaraEntity.Attack attack, int ticks,
            String name, boolean fire, Runnable then) {
        reset(knight, hp);
        ignara.setTarget(knight);
        ignara.forceAttack(attack);
        helper.runAfterDelay(ticks, () -> {
            helper.assertTrue(knight.getHealth() < hp[0], name + " trifft nicht (" + ignara.currentAttack() + ", Abstand "
                    + String.format(java.util.Locale.ROOT, "%.1f", ignara.distanceTo(knight)) + ")");
            if (fire) {
                helper.assertTrue(knight.isOnFire(), name + " setzt nicht in Brand");
            }
            then.run();
        });
    }

    private static void rain(GameTestHelper helper, ServerLevel level, IgnaraEntity ignara, LivingEntity knight, float[] hp, AABB area) {
        reset(knight, hp);
        ignara.setTarget(knight);
        ignara.forceAttack(IgnaraEntity.Attack.OPAL_RAIN);
        helper.runAfterDelay(26, () -> helper.assertFalse(level.getEntitiesOfClass(OpalShardEntity.class, area.inflate(0, 16, 0)).isEmpty(),
                "Es regnet keine Opalsplitter"));
        helper.runAfterDelay(IgnaraEntity.Attack.OPAL_RAIN.duration + 12, () -> {
            helper.assertTrue(knight.getHealth() < hp[0], "Die Opalsplitter treffen nicht");
            reset(knight, hp);
            ignara.setTarget(knight);
            ignara.forceAttack(IgnaraEntity.Attack.CALL);
            helper.runAfterDelay(20, () -> {
                var spirits = level.getEntitiesOfClass(Glutgeist.class, area);
                helper.assertTrue(spirits.size() == 2, "Der Ruf holt keine zwei Glutgeister: " + spirits.size());
                spirits.forEach(net.minecraft.world.entity.Entity::discard);
                // als Phönix: der Sturzflug
                ignara.forcePhoenix();
                helper.assertTrue(ignara.isPhoenix(), "Sie wird nicht zum Phönix");
                reset(knight, hp);
                ignara.setTarget(knight);
                ignara.forceAttack(IgnaraEntity.Attack.DIVE);
                helper.runAfterDelay(30, () -> {
                    helper.assertTrue(knight.getHealth() < hp[0], "Der Sturzflug trifft nicht");
                    cleanup(level, area);
                    helper.succeed();
                });
            });
        });
    }

    private static void reset(LivingEntity knight, float[] hp) {
        knight.clearFire();
        knight.setHealth(knight.getMaxHealth());
        knight.setInvulnerableTime(0);
        knight.setDeltaMovement(Vec3.ZERO);
        hp[0] = knight.getHealth();
    }

    // =====================================================================================
    // Die Flammenpeitsche
    // =====================================================================================

    /**
     * Die Flammenpeitsche: verzauberbar wie ein Schwert, feuerfest, mit Feueropalen zu reparieren, reicht weiter als ein
     * Schwert; jeder Treffer setzt in Brand. Der Peitschenschlag trifft das erste Wesen in Blickrichtung (bis zehn Blöcke),
     * setzt es in Brand und reißt es heran.
     */
    private static void whip(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper);
        ItemStack stack = new ItemStack(FlameSanctuary.FLAME_WHIP.get());
        helper.assertTrue(stack.is(ItemTags.SWORDS), "Die Flammenpeitsche ist nicht wie ein Schwert verzauberbar");
        helper.assertTrue(stack.has(DataComponents.DAMAGE_RESISTANT), "Die Flammenpeitsche verbrennt");
        helper.assertTrue(stack.isValidRepairItem(new ItemStack(FlameGear.FIRE_OPAL.get())), "Feueropale reparieren sie nicht");
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        BlockPos start = helper.absolutePos(new BlockPos(1, 1, 4));
        player.snapTo(start.getX() + 0.5, start.getY(), start.getZ() + 0.5, -90.0f, 0.0f);
        player.setYHeadRot(-90.0f);
        LivingEntity target = helper.spawnWithNoFreeWill(ModEntities.SOLDIERS.get(SoldierType.SWORDSMAN).get(), new BlockPos(7, 1, 4));
        float hp = target.getHealth();
        LivingEntity hit = FlameWhipItem.lash(level, player, FlameWhipItem.LASH_DAMAGE);
        helper.assertTrue(hit == target, "Der Peitschenschlag trifft nicht das Wesen in Blickrichtung: " + hit);
        helper.assertTrue(target.getHealth() < hp, "Der Peitschenschlag schadet nicht");
        helper.assertTrue(target.isOnFire(), "Der Peitschenschlag setzt nicht in Brand");
        helper.assertTrue(target.getDeltaMovement().x < -0.1, "Der Peitschenschlag reißt nicht heran: " + target.getDeltaMovement());
        target.clearFire();
        FlameSanctuary.FLAME_WHIP.get().hurtEnemy(stack, target, player);
        helper.assertTrue(target.isOnFire(), "Ein Hieb mit der Peitsche setzt nicht in Brand");
        level.getServer().getPlayerList().remove(player);
        target.discard();
        helper.succeed();
    }

    // =====================================================================================
    // Das Flammenheiligtum
    // =====================================================================================

    /**
     * Das Flammenheiligtum: auf dem Podest steht der Flammenaltar (betend, mit Blick zum Steg), davor ist Platz für die
     * Priesterin, darunter das Podest; daneben zwei Truhen mit Beute, dahinter die Wand mit dem Flammenfenster. Gebaut wird
     * nur ein Ausschnitt um das Podest, hoch über dem Test (so bleiben die Nachbartests unberührt).
     */
    private static void sanctuary(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos origin = helper.absolutePos(new BlockPos(4, 110, 9));
        var sanctum = SiteSamples.flameSanctum(origin);
        BlockPos altarPos = SiteSamples.flameSanctumAltar(sanctum);
        var box = net.minecraft.world.level.levelgen.structure.BoundingBox.fromCorners(SiteSamples.flameSanctumAt(sanctum, -5, -2, -9),
                SiteSamples.flameSanctumAt(sanctum, 5, 8, 0));
        sanctum.postProcess(level, level.structureManager(), level.getChunkSource().getGenerator(), net.minecraft.util.RandomSource.create(5), box,
                net.minecraft.world.level.ChunkPos.containing(origin), origin);
        var state = level.getBlockState(altarPos);
        helper.assertTrue(state.is(FlameSanctuary.FLAME_ALTAR.get()), "Kein Flammenaltar auf dem Podest: " + state);
        helper.assertTrue(state.getValue(FlameAltarBlock.PHASE) == FlameAltarBlock.Phase.PRAYING, "Der Altar wartet nicht auf die Priesterin");
        helper.assertTrue(state.getValue(FlameAltarBlock.FACING) == Direction.SOUTH, "Der Altar blickt nicht zum Steg: " + state);
        helper.assertTrue(level.getBlockEntity(altarPos) instanceof FlameAltarBlockEntity, "Der Altar hat keine Blockentität");
        BlockPos place = altarPos.relative(Direction.SOUTH, 2);
        for (BlockPos p : BlockPos.betweenClosed(place, place.above(3))) {
            helper.assertTrue(level.getBlockState(p).isAir(), "Der Platz der Priesterin ist verbaut: " + level.getBlockState(p));
        }
        helper.assertTrue(level.getBlockState(place.below()).isSolidRender(), "Unter der Priesterin ist kein Podest");
        for (int x : new int[] {-2, 2}) {
            BlockPos chest = SiteSamples.flameSanctumAt(sanctum, x, 1, -6);
            helper.assertTrue(level.getBlockEntity(chest) instanceof ChestBlockEntity c && c.getLootTable() != null,
                    "Keine Truhe mit Beute neben dem Altar bei " + x);
        }
        helper.assertTrue(level.getBlockState(SiteSamples.flameSanctumAt(sanctum, 0, 3, -8)).is(Blocks.SHROOMLIGHT), "Kein Flammenfenster hinter dem Altar");
        helper.assertTrue(level.getBlockState(SiteSamples.flameSanctumAt(sanctum, 0, -1, 0)).is(Blocks.GILDED_BLACKSTONE),
                "Keine goldene Sonne in der Mitte des Bodens");
        // (der Altar riefe sonst eine Priesterin, sobald ein Spieler eines anderen Tests in die Nähe käme)
        for (BlockPos p : BlockPos.betweenClosed(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ())) {
            level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        // gedreht blickt der Altar mit – die Priesterin schwebt immer zwischen Altar und Steg
        var turned = SiteSamples.flameSanctum(origin.above(14), Rotation.CLOCKWISE_90);
        BlockPos turnedAltar = SiteSamples.flameSanctumAltar(turned);
        var small = net.minecraft.world.level.levelgen.structure.BoundingBox.fromCorners(turnedAltar.offset(-1, -1, -1), turnedAltar.offset(1, 1, 1));
        turned.postProcess(level, level.structureManager(), level.getChunkSource().getGenerator(), net.minecraft.util.RandomSource.create(5), small,
                net.minecraft.world.level.ChunkPos.containing(turnedAltar), turnedAltar);
        var turnedState = level.getBlockState(turnedAltar);
        helper.assertTrue(turnedState.is(FlameSanctuary.FLAME_ALTAR.get()) && turnedState.getValue(FlameAltarBlock.FACING) == Direction.WEST,
                "Im gedrehten Heiligtum blickt der Altar nicht zum Steg: " + turnedState);
        for (BlockPos p : BlockPos.betweenClosed(small.minX(), small.minY(), small.minZ(), small.maxX(), small.maxY(), small.maxZ())) {
            level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        helper.succeed();
    }
}
