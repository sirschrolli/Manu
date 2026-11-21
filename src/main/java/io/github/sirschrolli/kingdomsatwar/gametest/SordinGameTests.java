package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.deep.OrganDeskBlock;
import io.github.sirschrolli.kingdomsatwar.block.deep.OrganDeskBlockEntity;
import io.github.sirschrolli.kingdomsatwar.entity.deep.SordinEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.item.deep.TuningForkItem;
import io.github.sirschrolli.kingdomsatwar.registry.CathedralGear;
import io.github.sirschrolli.kingdomsatwar.registry.EchoGear;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
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
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Tests zu Sordin, dem Kantor der Stille (1.7): das Orgelpult (der spielende Kantor, Erwachen, Fortissimo, Taumeln, Sieg und
 * Rückruf mit Hallkristall), sein Gehör (Schritte, Schleichen, Wollsohlen, Orgel), seine Angriffe und die Stimmgabel.
 */
final class SordinGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> DESK =
            ModGameTests.TEST_FUNCTIONS.register("sordin_desk", () -> SordinGameTests::desk);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> HEARING =
            ModGameTests.TEST_FUNCTIONS.register("sordin_hearing", () -> SordinGameTests::hearing);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ATTACKS =
            ModGameTests.TEST_FUNCTIONS.register("sordin_attacks", () -> SordinGameTests::attacks);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FORK =
            ModGameTests.TEST_FUNCTIONS.register("sordin_fork", () -> SordinGameTests::fork);

    private SordinGameTests() {}

    /** Lädt die Klasse (meldet die Testfunktionen an). */
    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var test : List.of(FORK, HEARING)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 300, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
        for (var test : List.of(DESK, ATTACKS)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 1200, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
    }

    private static void floor(GameTestHelper helper) {
        for (int x = 0; x < 9; x++) {
            for (int z = 0; z < 9; z++) {
                helper.setBlock(x, 0, z, Blocks.DEEPSLATE_TILES);
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

    private static OrganDeskBlockEntity placeDesk(GameTestHelper helper, ServerLevel level, BlockPos pos) {
        level.setBlock(pos, CathedralGear.ORGAN_DESK.get().defaultBlockState().setValue(OrganDeskBlock.FACING, Direction.SOUTH), Block.UPDATE_ALL);
        if (!(level.getBlockEntity(pos) instanceof OrganDeskBlockEntity desk)) {
            throw new IllegalStateException("Das Orgelpult hat keine Blockentität");
        }
        return desk;
    }

    // =====================================================================================
    // Das Orgelpult
    // =====================================================================================

    /**
     * Das Orgelpult: unzerstörbar; kommt jemand in die Nähe, steht der Kantor schon vor dem Pult und spielt (unverwundbar);
     * tritt jemand näher heran, erwacht er. Wach ist er verwundbar; spielt jemand auf der Orgel, taumelt er (und nimmt mehr
     * Schaden); unter halber Kraft das Fortissimo (zuerst unverwundbar). Besiegt verstummt das Pult; er lässt Hallkristall
     * fallen. Ein Hallkristall weckt das Pult und ruft ihn zurück.
     */
    private static void desk(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper);
        helper.assertTrue(ModEntities.SORDIN.get().getCategory() == MobCategory.MONSTER, "Sordin: falsche Gruppe");
        BlockPos pos = helper.absolutePos(new BlockPos(4, 1, 1));
        OrganDeskBlockEntity desk = placeDesk(helper, level, pos);
        helper.assertTrue(level.getBlockState(pos).getDestroySpeed(level, pos) < 0, "Das Orgelpult lässt sich abbauen");
        helper.assertTrue(desk.phase() == OrganDeskBlock.Phase.GUARDED, "Das Pult wartet nicht auf seinen Kantor");
        AABB area = new AABB(pos).inflate(7, 6, 9);
        ServerPlayer player = MageGameTests.survivalPlayer(helper, "test-cathedral-pilgrim", new io.netty.channel.embedded.EmbeddedChannel[1], 0);
        player.setShiftKeyDown(true);                      // er schleicht: seine Schritte wecken den Kantor nicht
        move(player, pos.getX() + 0.5, pos.getY(), pos.getZ() + 20.0);
        helper.runAfterDelay(45, () -> {
            var found = level.getEntitiesOfClass(SordinEntity.class, area);
            helper.assertTrue(found.size() == 1, "Kein (oder mehr als ein) Kantor vor dem Pult: " + found.size() + " (Pult " + desk.phase() + ")");
            SordinEntity cantor = found.getFirst();
            cantor.confine(9.0);
            helper.assertTrue(cantor.isPlaying(), "Er spielt nicht: " + cantor.currentAttack());
            helper.assertTrue(desk.owns(cantor), "Das Pult kennt seinen Kantor nicht");
            helper.assertTrue(cantor.blockPosition().equals(pos.relative(Direction.SOUTH, 1)), "Er steht nicht vor dem Pult: "
                    + helper.relativePos(cantor.blockPosition()));
            helper.assertTrue(Math.abs(Math.floorMod((int) cantor.getYRot(), 360) - 180) < 5, "Er blickt nicht zur Orgel: " + cantor.getYRot());
            float hp = cantor.getHealth();
            cantor.hurtServer(level, level.damageSources().magic(), 50.0f);
            helper.assertTrue(cantor.getHealth() >= hp && cantor.isPlaying(), "Spielend ist er verwundbar");
            // jemand tritt heran: er erwacht
            move(player, pos.getX() + 0.5, pos.getY(), pos.getZ() + 8.0);
            helper.runAfterDelay(14, () -> {
                helper.assertTrue(cantor.currentAttack() == SordinEntity.Attack.AWAKEN, "Er erwacht nicht: " + cantor.currentAttack());
                helper.assertTrue(desk.phase() == OrganDeskBlock.Phase.AWAKE, "Das Pult merkt das Erwachen nicht");
                helper.assertTrue(level.getBlockState(pos).getValue(OrganDeskBlock.PHASE) == OrganDeskBlock.Phase.AWAKE,
                        "Die Noten des Pults leuchten nicht auf");
                float before = cantor.getHealth();
                cantor.setInvulnerableTime(0);
                cantor.hurtServer(level, level.damageSources().magic(), 50.0f);
                helper.assertTrue(cantor.getHealth() >= before, "Beim Erwachen ist er verwundbar");
                level.getServer().getPlayerList().remove(player);
                helper.runAfterDelay(SordinEntity.AWAKEN_TICKS + 4, () -> fight(helper, level, pos, desk, cantor, area));
            });
        });
    }

    private static void fight(GameTestHelper helper, ServerLevel level, BlockPos pos, OrganDeskBlockEntity desk, SordinEntity cantor, AABB area) {
        helper.assertTrue(cantor.currentAttack() != SordinEntity.Attack.AWAKEN && !cantor.isPlaying(), "Er erwacht nicht ganz: "
                + cantor.currentAttack());
        // wach ist er verwundbar (Zauber gehen durch seine Rüstung: so lässt sich der Schaden genau messen)
        cantor.setInvulnerableTime(0);
        cantor.damageCooldownTime = 0;
        float hp = cantor.getHealth();
        cantor.hurtServer(level, level.damageSources().magic(), 20.0f);
        float normal = hp - cantor.getHealth();
        helper.assertTrue(normal > 19.0f, "Wach ist er unverwundbar: " + normal);
        // jemand spielt auf der Orgel: er hält sich die Ohren zu und nimmt mehr Schaden; gleich darauf lässt ihn die Orgel kalt
        helper.assertTrue(cantor.hearOrgan(level), "Die Orgel lässt ihn nicht taumeln");
        helper.assertTrue(cantor.currentAttack() == SordinEntity.Attack.STAGGER, "Er taumelt nicht: " + cantor.currentAttack());
        cantor.setInvulnerableTime(0);
        cantor.damageCooldownTime = 0;
        hp = cantor.getHealth();
        cantor.hurtServer(level, level.damageSources().magic(), 20.0f);
        float staggered = hp - cantor.getHealth();
        helper.assertTrue(staggered > normal * 1.2f, "Taumelnd nimmt er nicht mehr Schaden: " + normal + " / " + staggered);
        helper.assertFalse(cantor.hearOrgan(level), "Die Orgel lässt ihn gleich wieder taumeln");
        // der Schallschlag (auch sein eigener) kümmert ihn nicht
        cantor.setInvulnerableTime(0);
        cantor.damageCooldownTime = 0;
        hp = cantor.getHealth();
        cantor.hurtServer(level, level.damageSources().sonicBoom(cantor), 20.0f);
        helper.assertTrue(cantor.getHealth() >= hp, "Ein Schallschlag schadet ihm");
        helper.runAfterDelay(SordinEntity.STAGGER_TICKS + 2, () -> {
            helper.assertTrue(cantor.currentAttack() != SordinEntity.Attack.STAGGER, "Das Taumeln endet nicht");
            cantor.setHealth(cantor.getMaxHealth() * 0.45f);
            helper.runAfterDelay(4, () -> {
                helper.assertTrue(cantor.currentAttack() == SordinEntity.Attack.FORTISSIMO, "Unter halber Kraft kein Fortissimo: "
                        + cantor.currentAttack());
                float before = cantor.getHealth();
                cantor.setInvulnerableTime(0);
                cantor.damageCooldownTime = 0;
                cantor.hurtServer(level, level.damageSources().magic(), 30.0f);
                helper.assertTrue(cantor.getHealth() >= before, "Während er Luft holt, ist er verwundbar");
                helper.runAfterDelay(SordinEntity.Attack.FORTISSIMO.duration, () -> {
                    helper.assertTrue(cantor.isFortissimo(), "Das Fortissimo bleibt nicht");
                    helper.assertTrue(cantor.currentAttack() != SordinEntity.Attack.FORTISSIMO, "Das Fortissimo endet nicht");
                    cantor.setInvulnerableTime(0);
                    cantor.damageCooldownTime = 0;
                    cantor.setHealth(1.0f);
                    BossLootGameTests.firstVictory(helper, cantor, 50.0f);
                    helper.assertTrue(cantor.isDeadOrDying(), "Sordin fällt nicht");
                    helper.assertTrue(desk.phase() == OrganDeskBlock.Phase.SILENCED, "Das Pult merkt den Sieg nicht");
                    helper.assertTrue(level.getBlockState(pos).getValue(OrganDeskBlock.PHASE) == OrganDeskBlock.Phase.SILENCED,
                            "Das Pult verstummt nicht");
                    helper.runAfterDelay(SordinEntity.DEATH_TICKS + 5, () -> {
                        helper.assertTrue(cantor.isRemoved(), "Er zerfällt nicht");
                        int crystals = level.getEntitiesOfClass(ItemEntity.class, area, e -> e.getItem().is(EchoGear.ECHO_CRYSTAL.get())).stream()
                                .mapToInt(e -> e.getItem().getCount()).sum();
                        helper.assertTrue(crystals >= 2 && crystals <= 4, "Er lässt nicht zwei bis vier Hallkristalle fallen: " + crystals);
                        // ein Hallkristall weckt das Pult
                        helper.assertTrue(desk.rekindle(level, null), "Ein Hallkristall weckt das Pult nicht");
                        var again = level.getEntitiesOfClass(SordinEntity.class, area, LivingEntity::isAlive);
                        helper.assertTrue(again.size() == 1 && again.getFirst().currentAttack() == SordinEntity.Attack.AWAKEN,
                                "Der Kantor kehrt nicht zurück");
                        helper.assertTrue(level.getBlockState(pos).getValue(OrganDeskBlock.PHASE) == OrganDeskBlock.Phase.AWAKE,
                                "Die Noten leuchten nicht wieder");
                        helper.assertFalse(desk.rekindle(level, null), "Das Pult lässt sich zweimal wecken");
                        // aufräumen (das Pult riefe sonst neue Kantoren, sobald ein Spieler eines anderen Tests vorbeikommt)
                        cleanup(level, area);
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                        helper.succeed();
                    });
                });
            });
        });
    }

    // =====================================================================================
    // Das Gehör
    // =====================================================================================

    /**
     * Sein Gehör: Ein Schritt in Hörweite (aber weiter fort, als dass man ihn sähe) weckt ihn – nicht aber, wer schleicht, und
     * nicht, wer die Wollsohlen der Lauscher trägt. Ein Notenblock, der in seiner Nähe erklingt, weckt ihn auch.
     */
    private static void hearing(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper);
        BlockPos pos = helper.absolutePos(new BlockPos(4, 1, 1));
        BlockPos note = helper.absolutePos(new BlockPos(1, 1, 6));
        OrganDeskBlockEntity desk = placeDesk(helper, level, pos);
        SordinEntity cantor = desk.spawn(level, false);
        helper.assertTrue(cantor != null && cantor.isPlaying(), "Der Kantor spielt nicht");
        AABB area = new AABB(pos).inflate(7, 6, 9);
        ServerPlayer player = MageGameTests.survivalPlayer(helper, "test-cathedral-listener", new io.netty.channel.embedded.EmbeddedChannel[1], 0);
        Runnable tidy = () -> {
            level.getServer().getPlayerList().remove(player);
            cleanup(level, area);
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            level.setBlock(note, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        };
        player.connection.markClientLoaded();
        move(player, pos.getX() + 0.5, pos.getY(), pos.getZ() + 18.0);
        // das Pult meldet sich erst im nächsten Tick bei der Welt an (wie jede neue Blockentität)
        helper.runAfterDelay(2, () -> tidyOnFailure(tidy, () -> {
            Vec3 at = player.position();
            helper.assertTrue(at.distanceTo(Vec3.atCenterOf(pos)) > SordinEntity.WAKE_RADIUS
                    && at.distanceTo(Vec3.atCenterOf(pos)) < SordinEntity.HEAR_RADIUS, "Probe falsch aufgebaut");
            // wer schleicht, den hört er nicht
            player.setShiftKeyDown(true);
            level.gameEvent(player, GameEvent.STEP, at);
            helper.assertTrue(cantor.isPlaying(), "Er hört die Schritte eines Schleichenden");
            // wer die Wollsohlen trägt, den hört er nicht
            player.setShiftKeyDown(false);
            player.setItemSlot(EquipmentSlot.FEET, new ItemStack(EchoGear.LISTENER_BOOTS.get()));
            level.gameEvent(player, GameEvent.STEP, at);
            level.gameEvent(player, GameEvent.HIT_GROUND, at);
            helper.assertTrue(cantor.isPlaying(), "Er hört die Schritte trotz Wollsohlen");
            // sonst schon
            player.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);
            level.gameEvent(player, GameEvent.STEP, at);
            helper.assertTrue(cantor.currentAttack() == SordinEntity.Attack.AWAKEN, "Er hört die Schritte nicht: " + cantor.currentAttack());
            // ein Notenblock in der Nähe weckt einen spielenden Kantor
            level.getServer().getPlayerList().remove(player);
            desk.reset(level);
            SordinEntity second = desk.spawn(level, false);
            helper.assertTrue(second != null && second.isPlaying(), "Der zweite Kantor spielt nicht");
            level.setBlock(note, Blocks.NOTE_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
            level.blockEvent(note, Blocks.NOTE_BLOCK, 0, 0);
            helper.runAfterDelay(3, () -> tidyOnFailure(tidy, () -> {
                helper.assertTrue(second.currentAttack() == SordinEntity.Attack.AWAKEN, "Die Orgel weckt ihn nicht: " + second.currentAttack());
                tidy.run();
                helper.succeed();
            }));
        }));
    }

    /** Führt {@code body} aus; scheitert es, wird vorher aufgeräumt (ein liegen gebliebenes Pult riefe sonst Kantoren herbei). */
    private static void tidyOnFailure(Runnable tidy, Runnable body) {
        try {
            body.run();
        } catch (RuntimeException e) {
            tidy.run();
            throw e;
        }
    }

    // =====================================================================================
    // Die Angriffe
    // =====================================================================================

    /**
     * Seine Angriffe gegen einen Ritter: der Gabelhieb, die Schallwelle (Wolle hält sie auf), der Akkord (die Ringe treffen,
     * wer am Boden steht), der Notenschwarm und die Stille (er verschwindet und trifft von hinten). (Seine Angriffe treffen im
     * Test nur, wer nah bei ihm ist.)
     */
    private static void attacks(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper);
        SordinEntity cantor = helper.spawn(ModEntities.SORDIN.get(), new BlockPos(4, 1, 1));
        cantor.confine(10.0);                              // (auch die Orgel eines Nachbartests hört er so nicht)
        LivingEntity knight = helper.spawnWithNoFreeWill(ModEntities.SOLDIERS.get(SoldierType.KNIGHT).get(), new BlockPos(4, 1, 4));
        Vec3 near = knight.position();
        Vec3 far = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(4, 1, 8)));
        AABB area = new AABB(helper.absolutePos(new BlockPos(4, 1, 4))).inflate(10, 12, 10);
        float[] hp = {knight.getHealth()};
        step(helper, cantor, knight, near, hp, SordinEntity.Attack.SWING, SordinEntity.SWING_HIT + 3, "Der Gabelhieb", () ->
                step(helper, cantor, knight, far, hp, SordinEntity.Attack.SONIC, SordinEntity.SONIC_FIRE + 3, "Die Schallwelle", () ->
                        wool(helper, level, cantor, knight, far, hp, () ->
                                step(helper, cantor, knight, near, hp, SordinEntity.Attack.CHORD, SordinEntity.CHORD_BEATS[0] + 16, "Der Akkord", () ->
                                        step(helper, cantor, knight, far, hp, SordinEntity.Attack.NOTES, 70, "Der Notenschwarm", () ->
                                                silence(helper, cantor, knight, near, hp, area))))));
    }

    /** Ein Angriff: erzwingen, warten, prüfen, ob der Ritter getroffen wurde, dann den nächsten. */
    private static void step(GameTestHelper helper, SordinEntity cantor, LivingEntity knight, Vec3 spot, float[] hp, SordinEntity.Attack attack,
            int ticks, String name, Runnable then) {
        reset(knight, spot, hp);
        cantor.setTarget(knight);
        cantor.forceAttack(attack);
        helper.runAfterDelay(ticks, () -> {
            helper.assertTrue(knight.getHealth() < hp[0], name + " trifft nicht (" + cantor.currentAttack() + ", Abstand "
                    + String.format(java.util.Locale.ROOT, "%.1f", cantor.distanceTo(knight)) + ")");
            then.run();
        });
    }

    /** Die Schallwelle gegen eine Wand aus Wolle: sie bleibt darin stecken, der Ritter dahinter bleibt heil. */
    private static void wool(GameTestHelper helper, ServerLevel level, SordinEntity cantor, LivingEntity knight, Vec3 spot, float[] hp,
            Runnable then) {
        BlockPos a = helper.absolutePos(new BlockPos(1, 1, 6));
        BlockPos b = helper.absolutePos(new BlockPos(7, 6, 6));
        for (BlockPos p : BlockPos.betweenClosed(a, b)) {
            level.setBlock(p, Blocks.WOOL.pick(net.minecraft.world.item.DyeColor.GRAY).defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        reset(knight, spot, hp);
        cantor.setTarget(knight);
        cantor.forceAttack(SordinEntity.Attack.SONIC);
        helper.runAfterDelay(SordinEntity.SONIC_FIRE + 3, () -> {
            helper.assertTrue(knight.getHealth() >= hp[0], "Die Schallwelle dringt durch die Wolle");
            for (BlockPos p : BlockPos.betweenClosed(a, b)) {
                level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
            then.run();
        });
    }

    /** Die Stille: er wird unsichtbar, taucht hinter dem Ritter auf und trifft. */
    private static void silence(GameTestHelper helper, SordinEntity cantor, LivingEntity knight, Vec3 spot, float[] hp, AABB area) {
        reset(knight, spot, hp);
        cantor.setTarget(knight);
        cantor.forceAttack(SordinEntity.Attack.SILENCE);
        helper.runAfterDelay(SordinEntity.SILENCE_VANISH + 6, () -> helper.assertTrue(cantor.isInvisible(), "In der Stille bleibt er sichtbar"));
        helper.runAfterDelay(SordinEntity.SILENCE_HIT + 4, () -> {
            helper.assertFalse(cantor.isInvisible(), "Nach der Stille bleibt er unsichtbar");
            helper.assertTrue(knight.getHealth() < hp[0], "Aus der Stille trifft er nicht (Abstand "
                    + String.format(java.util.Locale.ROOT, "%.1f", cantor.distanceTo(knight)) + ")");
            cleanup(helper.getLevel(), area);
            helper.succeed();
        });
    }

    private static void reset(LivingEntity knight, Vec3 spot, float[] hp) {
        knight.teleportTo(spot.x, spot.y, spot.z);
        knight.setHealth(knight.getMaxHealth());
        knight.setInvulnerableTime(0);
        knight.setDeltaMovement(Vec3.ZERO);
        knight.resetFallDistance();                       // vom letzten Treffer hochgeschleudert: sonst fiele er sich wund
        hp[0] = knight.getHealth();
    }

    // =====================================================================================
    // Die Stimmgabel
    // =====================================================================================

    /**
     * Die Stimmgabel: schwer wie ein Streitkolben, verzauberbar (Schärfe, Haltbarkeit), mit Hallkristall auszubessern; die
     * Resonanz trifft einen Zombie durch eine Steinwand hindurch – durch eine Wand aus Wolle nicht.
     */
    private static void fork(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ItemStack fork = new ItemStack(CathedralGear.TUNING_FORK.get());
        double[] damage = new double[1];
        fork.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY).forEach(EquipmentSlot.MAINHAND, (attribute, modifier) -> {
            if (attribute.is(Attributes.ATTACK_DAMAGE)) {
                damage[0] += modifier.amount();
            }
        });
        helper.assertTrue(damage[0] >= 8.0, "Die Stimmgabel trifft zu schwach: " + damage[0]);
        helper.assertTrue(fork.isValidRepairItem(new ItemStack(EchoGear.ECHO_CRYSTAL.get())), "Nicht mit Hallkristall auszubessern");
        helper.assertTrue(fork.is(ItemTags.SHARP_WEAPON_ENCHANTABLE) && fork.is(ItemTags.DURABILITY_ENCHANTABLE), "Nicht verzauberbar");

        BlockPos base = helper.absolutePos(new BlockPos(4, 200, 4));
        for (BlockPos p : BlockPos.betweenClosed(base.offset(-2, -1, -2), base.offset(2, -1, 12))) {
            level.setBlock(p, Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        ServerPlayer player = MageGameTests.survivalPlayer(helper, "test-tuning-fork", new io.netty.channel.embedded.EmbeddedChannel[1], 0);
        Zombie zombie = ModEntities.ZOMBIE.get().create(level, EntitySpawnReason.EVENT);
        if (zombie == null) {
            throw new IllegalStateException("Kein Zombie");
        }
        try {
            player.connection.markClientLoaded();
            player.teleportTo(level, base.getX() + 0.5, base.getY(), base.getZ() + 0.5, java.util.Set.of(), 0.0f, 0.0f, true);
            level.getChunkSource().move(player);
            zombie.setNoAi(true);
            zombie.snapTo(base.getX() + 0.5, base.getY(), base.getZ() + 8.5, 180.0f, 0.0f);
            level.addFreshEntity(zombie);
            // eine Steinwand dazwischen: der Schall geht hindurch
            for (BlockPos p : BlockPos.betweenClosed(base.offset(-2, 0, 4), base.offset(2, 3, 4))) {
                level.setBlock(p, Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
            float before = zombie.getHealth();
            int hit = TuningForkItem.resonate(level, player, TuningForkItem.DAMAGE);
            helper.assertTrue(hit == 1 && zombie.getHealth() < before, "Die Resonanz trifft nicht durch Stein: " + hit);
            // eine Wand aus Wolle: der Schall bleibt stecken
            for (BlockPos p : BlockPos.betweenClosed(base.offset(-2, 0, 4), base.offset(2, 3, 4))) {
                level.setBlock(p, Blocks.WOOL.pick(net.minecraft.world.item.DyeColor.WHITE).defaultBlockState(), Block.UPDATE_CLIENTS);
            }
            zombie.setHealth(zombie.getMaxHealth());
            zombie.setInvulnerableTime(0);
            zombie.damageCooldownTime = 0;
            before = zombie.getHealth();
            hit = TuningForkItem.resonate(level, player, TuningForkItem.DAMAGE);
            helper.assertTrue(hit == 0 && zombie.getHealth() >= before, "Die Resonanz dringt durch Wolle: " + hit);
            helper.assertFalse(player.hasEffect(MobEffects.DARKNESS), "Die Resonanz verdunkelt den Spieler");
            helper.succeed();
        } finally {
            zombie.discard();
            level.getServer().getPlayerList().remove(player);
            for (BlockPos p : BlockPos.betweenClosed(base.offset(-2, -1, -2), base.offset(2, 3, 12))) {
                level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
        }
    }
}
