package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.sea.WitchCauldronBlock;
import io.github.sirschrolli.kingdomsatwar.block.sea.WitchCauldronBlockEntity;
import io.github.sirschrolli.kingdomsatwar.entity.fae.CrystalLine;
import io.github.sirschrolli.kingdomsatwar.entity.sea.InkBoltEntity;
import io.github.sirschrolli.kingdomsatwar.entity.sea.SeaWitchEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.item.sea.SeaWitchStaffItem;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.SeaWitch;
import io.github.sirschrolli.kingdomsatwar.worldgen.witch.WitchGrottoPiece;
import java.util.ArrayList;
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
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Tests zur Meerhexe und zur Hexengrotte (0.97): wie eine Grotte gebaut ist, der Kessel und die Trance der Hexe, ihr Erwachen,
 * alle Angriffe, ihr Zorn, Tod, erkalteter Kessel und Rückruf mit einer Perle; dazu der Hexenstab.
 */
final class SeaWitchGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");

    static final DeferredHolder<java.util.function.Consumer<GameTestHelper>, java.util.function.Consumer<GameTestHelper>> GROTTO =
            ModGameTests.TEST_FUNCTIONS.register("sea_witch_grotto", () -> SeaWitchGameTests::grotto);
    static final DeferredHolder<java.util.function.Consumer<GameTestHelper>, java.util.function.Consumer<GameTestHelper>> FIGHT =
            ModGameTests.TEST_FUNCTIONS.register("sea_witch_fight", () -> SeaWitchGameTests::fight);
    static final DeferredHolder<java.util.function.Consumer<GameTestHelper>, java.util.function.Consumer<GameTestHelper>> STAFF =
            ModGameTests.TEST_FUNCTIONS.register("sea_witch_staff", () -> SeaWitchGameTests::staff);

    private SeaWitchGameTests() {}

    /** Lädt die Klasse (meldet die Testfunktionen an). */
    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var test : List.of(GROTTO, STAFF)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 300, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
        event.registerTest(FIGHT.getId(), new FunctionGameTestInstance(FIGHT.key(),
                new TestData<>(environment, Level.OVERWORLD, ARENA, 1800, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
    }

    // =====================================================================================
    // Ein eigenes Feld weit draußen
    // =====================================================================================

    private record Field(ServerLevel level, BlockPos origin, List<long[]> chunks) {
        void close() {
            AABB area = new AABB(this.origin).inflate(48, 30, 48);
            for (Entity e : this.level.getEntitiesOfClass(Entity.class, area, e -> !(e instanceof Player))) {
                e.discard();
            }
            BoundingBox box = WitchGrottoPiece.makeBox(this.origin);
            for (BlockPos p : BlockPos.betweenClosed(box.minX() - 1, box.minY() - 1, box.minZ() - 1, box.maxX() + 1, box.maxY() + 1, box.maxZ() + 1)) {
                if (!this.level.getBlockState(p).isAir()) {
                    this.level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                }
            }
            for (long[] c : this.chunks) {
                this.level.setChunkForced((int) c[0], (int) c[1], false);
            }
        }
    }

    private static Field field(GameTestHelper helper, int dx, int dz) {
        ServerLevel level = helper.getLevel();
        BlockPos base = helper.absolutePos(BlockPos.ZERO);
        BlockPos origin = new BlockPos(base.getX() + dx, 140, base.getZ() + dz);
        List<long[]> chunks = new ArrayList<>();
        int reach = 40;
        for (int cx = (origin.getX() - reach) >> 4; cx <= (origin.getX() + reach) >> 4; cx++) {
            for (int cz = (origin.getZ() - reach) >> 4; cz <= (origin.getZ() + reach) >> 4; cz++) {
                level.getChunk(cx, cz);
                level.setChunkForced(cx, cz, true);
                chunks.add(new long[] {cx, cz});
            }
        }
        return new Field(level, origin, chunks);
    }

    private static void move(ServerPlayer player, double x, double y, double z) {
        player.teleportTo(player.level(), x, y, z, java.util.Set.of(), 180.0f, 0.0f, true);
        player.level().getChunkSource().move(player);
    }

    // =====================================================================================
    // Die Grotte
    // =====================================================================================

    /**
     * Eine Grotte am Meeresgrund: eine Kuppel aus Gezeitenstein, innen voll Wasser (auch über dem Kessel, wo die Hexe
     * schwebt), der Eingang offen, ein Knochenbogen mit Schädeln, in der Mitte das Podest mit dem Hexenkessel (er leuchtet und
     * ist unzerstörbar), ringsum Säulen mit Laternen, Laternen an Ketten, zwei Truhen mit Beute, ein Wrack.
     */
    private static void grotto(GameTestHelper helper) {
        Field field = field(helper, 25_000, -21_000);
        ServerLevel level = field.level();
        BlockPos o = field.origin();
        long seed = 4242L;
        int sea = o.getY() + WitchGrottoPiece.HEIGHT + 10;
        try {
            WitchGrottoPiece.build(level, WitchGrottoPiece.makeBox(o), o, seed, sea, RandomSource.create(7), null);
            BlockPos cauldron = WitchGrottoPiece.cauldron(o);
            BlockState c = level.getBlockState(cauldron);
            helper.assertTrue(c.is(SeaWitch.WITCH_CAULDRON.get()), "Kein Hexenkessel in der Mitte: " + c);
            helper.assertTrue(c.getDestroySpeed(level, cauldron) < 0, "Der Hexenkessel lässt sich abbauen");
            helper.assertTrue(c.getLightEmission(level, cauldron) > 0, "Der Hexenkessel leuchtet nicht");
            helper.assertTrue(level.getBlockEntity(cauldron) instanceof WitchCauldronBlockEntity be && be.home().equals(cauldron.above(3)),
                    "Der Kessel kennt den Platz der Hexe nicht");
            for (int dy = 1; dy <= 4; dy++) {
                helper.assertTrue(level.getBlockState(cauldron.above(dy)).is(Blocks.WATER), "Über dem Kessel steht kein Wasser (+" + dy + ")");
            }
            int shell = 0;
            int bones = 0;
            int lanterns = 0;
            int chests = 0;
            int skulls = 0;
            BoundingBox box = WitchGrottoPiece.makeBox(o);
            for (BlockPos p : BlockPos.betweenClosed(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ())) {
                BlockState s = level.getBlockState(p);
                if (s.is(SeaBlocks.TIDE_STONE.base().get()) || s.is(SeaBlocks.TIDE_STONE_BRICKS.base().get())) {
                    shell++;
                } else if (s.is(Blocks.BONE_BLOCK)) {
                    bones++;
                } else if (s.is(Blocks.SOUL_LANTERN)) {
                    lanterns++;
                } else if (s.is(Blocks.CHEST)) {
                    chests++;
                } else if (s.is(Blocks.SKELETON_SKULL)) {
                    skulls++;
                }
            }
            helper.assertTrue(shell > 600, "Zu wenig Gezeitenstein für die Kuppel: " + shell);
            helper.assertTrue(bones >= 12, "Zu wenig Knochen (Bogen, Säulen): " + bones);
            helper.assertTrue(lanterns >= 6, "Zu wenig Laternen: " + lanterns);
            helper.assertTrue(chests == 2, "Nicht zwei Truhen: " + chests);
            helper.assertTrue(skulls >= 3, "Kein Schädel über dem Eingang: " + skulls);
            // der Eingang: Wasser im Gang durch die Schale
            Direction door = WitchGrottoPiece.entrance(seed);
            BlockPos gate = o.relative(door, WitchGrottoPiece.RADIUS - 1).above(2);
            helper.assertTrue(level.getBlockState(gate).is(Blocks.WATER), "Der Eingang ist verschlossen: " + level.getBlockState(gate));
            // oben ist die Kuppel geschlossen
            helper.assertFalse(level.getBlockState(o.above(WitchGrottoPiece.HEIGHT)).is(Blocks.WATER), "Die Kuppel ist oben offen");
            helper.assertTrue(level.getBlockState(o.above(WitchGrottoPiece.innerHeight() - 1)).is(Blocks.WATER)
                    || level.getBlockState(o.above(WitchGrottoPiece.innerHeight() - 1)).is(Blocks.IRON_CHAIN)
                    || level.getBlockState(o.above(WitchGrottoPiece.innerHeight() - 1)).is(Blocks.GLOW_LICHEN),
                    "Unter der Decke ist kein Wasser");
        } finally {
            field.close();
        }
        helper.succeed();
    }

    // =====================================================================================
    // Der Kampf
    // =====================================================================================

    private record Step(int delay, Runnable start, Runnable check) {}

    private static void run(GameTestHelper helper, List<Step> steps, int index, Runnable done) {
        if (index >= steps.size()) {
            done.run();
            return;
        }
        Step step = steps.get(index);
        step.start().run();
        helper.runAfterDelay(step.delay(), () -> {
            step.check().run();
            run(helper, steps, index + 1, done);
        });
    }

    /**
     * In einer (trockenen) Grotte: Kommt ein Spieler in die Nähe, schwebt die Hexe in Trance über ihrem Kessel (unverwundbar, ein
     * Schlag weckt sie); sie erwacht (auch dabei unverwundbar). Dann alle Angriffe gegen einen Ritter und einen Spieler:
     * Tintengeschosse, Strudel, Blasengefängnis, Haie, Korallenspieße, Schwanzhieb, Sirenengesang (wer sie ansieht, wird zu
     * ihr gezogen), unter halber Kraft ihr Zorn, im Zorn der Fluch. Besiegt lässt sie den Hexenstab fallen, ihre Haie
     * verschwinden, der Kessel erkaltet; eine Perle ruft sie zurück; zurückgesetzt schwebt beim nächsten Besuch wieder eine.
     */
    private static void fight(GameTestHelper helper) {
        helper.assertTrue(ModEntities.SEA_WITCH.get().getCategory() == MobCategory.MONSTER, "Meerhexe: falsche Gruppe");
        Field field = field(helper, -23_000, -19_000);
        ServerLevel level = field.level();
        BlockPos o = field.origin();
        // trocken gebaut (der Meeresspiegel liegt unter der Kuppel), damit kein Wasser in die Testwelt läuft
        WitchGrottoPiece.build(level, WitchGrottoPiece.makeBox(o), o, 99L, o.getY() + 1, RandomSource.create(3), null);
        BlockPos cauldronPos = WitchGrottoPiece.cauldron(o);
        if (!(level.getBlockEntity(cauldronPos) instanceof WitchCauldronBlockEntity cauldron)) {
            helper.fail("Der Hexenkessel hat keine Blockentität");
            field.close();
            return;
        }
        BlockPos home = cauldron.home();
        AABB area = new AABB(o).inflate(40, 24, 40);
        ServerPlayer player = MageGameTests.survivalPlayer(helper, "test-witch-visitor", new io.netty.channel.embedded.EmbeddedChannel[1], 0);
        Direction door = WitchGrottoPiece.entrance(99L);
        BlockPos outside = o.relative(door, WitchGrottoPiece.REACH + 8).above(2);
        move(player, outside.getX() + 0.5, outside.getY(), outside.getZ() + 0.5);
        helper.runAfterDelay(72, () -> {
            List<SeaWitchEntity> witches = level.getEntitiesOfClass(SeaWitchEntity.class, area, e -> cauldronPos.equals(e.lair()));
            if (witches.size() != 1) {
                field.close();
                helper.fail("Keine Hexe über dem Kessel: " + witches.size());
                return;
            }
            SeaWitchEntity witch = witches.getFirst();
            helper.assertTrue(witch.isDormant(), "Die Hexe ist nicht in Trance: " + witch.currentAttack());
            helper.assertTrue(witch.position().distanceTo(Vec3.atBottomCenterOf(home)) < 2.0, "Die Hexe schwebt nicht über dem Kessel: "
                    + witch.position());
            helper.assertTrue(cauldron.owns(witch), "Der Kessel kennt seine Hexe nicht");
            helper.assertTrue(level.getBlockState(cauldronPos).getValue(WitchCauldronBlock.PHASE) == WitchCauldronBlock.Phase.SLEEPING,
                    "Der Kessel brodelt nicht ruhig");
            float hp = witch.getHealth();
            witch.hurtServer(level, level.damageSources().playerAttack(player), 40.0f);
            helper.assertTrue(witch.getHealth() >= hp, "In Trance ist sie verwundbar");
            helper.assertTrue(witch.currentAttack() == SeaWitchEntity.Attack.AWAKEN, "Ein Schlag weckt sie nicht: " + witch.currentAttack());
            witch.hurtServer(level, level.damageSources().generic(), 40.0f);
            helper.assertTrue(witch.getHealth() >= hp, "Beim Erwachen ist sie verwundbar");
            level.getServer().getPlayerList().remove(player);
            helper.runAfterDelay(SeaWitchEntity.Attack.AWAKEN.duration + 4, () -> {
                helper.assertTrue(witch.currentAttack() != SeaWitchEntity.Attack.AWAKEN, "Das Erwachen endet nicht");
                helper.assertTrue(level.getBlockState(cauldronPos).getValue(WitchCauldronBlock.PHASE) == WitchCauldronBlock.Phase.AWAKE,
                        "Der Kessel merkt das Erwachen nicht");
                attacks(helper, field, cauldron, witch);
            });
        });
    }

    private static void attacks(GameTestHelper helper, Field field, WitchCauldronBlockEntity cauldron, SeaWitchEntity witch) {
        ServerLevel level = field.level();
        BlockPos o = field.origin();
        LivingEntity knight = ModEntities.SOLDIERS.get(SoldierType.KNIGHT).get().create(level, EntitySpawnReason.EVENT);
        if (knight == null) {
            field.close();
            helper.fail("Kein Ritter");
            return;
        }
        ((Mob) knight).setNoAi(true);
        knight.setNoGravity(true);
        knight.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000.0);
        knight.setHealth(1000.0f);
        Vec3 spot = new Vec3(o.getX() + 0.5, o.getY() + 3.0, o.getZ() + 0.5);
        knight.snapTo(spot.x + 5.0, spot.y, spot.z, 0.0f, 0.0f);
        level.addFreshEntity(knight);
        float[] hp = {0.0f};
        // vor jedem Angriff: der Ritter {@code ahead} Blöcke östlich der Hexe, beide geheilt
        Consumer<Double> place = ahead -> {
            knight.removeAllEffects();
            knight.setHealth(knight.getMaxHealth());
            knight.setInvulnerableTime(0);
            knight.damageCooldownTime = 0;
            knight.teleportTo(spot.x + ahead, spot.y, spot.z);
            knight.setDeltaMovement(Vec3.ZERO);
            witch.snapTo(spot.x, spot.y, spot.z, -90.0f, 0.0f);
            witch.setDeltaMovement(Vec3.ZERO);
            witch.setTarget(knight);
            hp[0] = knight.getHealth();
        };
        AABB around = new AABB(o).inflate(40, 24, 40);
        int[] bolts = {0};
        int[] lines = {0};
        List<Step> steps = List.of(
                new Step(30, () -> {
                    place.accept(7.0);
                    witch.forceAttack(SeaWitchEntity.Attack.INK);
                }, () -> {
                    bolts[0] = level.getEntitiesOfClass(InkBoltEntity.class, around).size();
                    helper.assertTrue(bolts[0] > 0 || knight.getHealth() < hp[0], "Keine Tintengeschosse");
                }),
                new Step(30, () -> { }, () -> {
                    helper.assertTrue(knight.getHealth() < hp[0], "Die Tinte trifft den Ritter nicht");
                    helper.assertTrue(knight.hasEffect(MobEffects.BLINDNESS), "Die Tinte blendet nicht");
                }),
                new Step(54, () -> {
                    place.accept(7.0);
                    witch.forceAttack(SeaWitchEntity.Attack.WHIRLPOOL);
                }, () -> helper.assertTrue(knight.getHealth() < hp[0], "Der Strudel trifft den Ritter nicht")),
                new Step(24, () -> {
                    place.accept(6.0);
                    witch.forceAttack(SeaWitchEntity.Attack.BUBBLE);
                }, () -> helper.assertTrue(knight.hasEffect(MobEffects.SLOWNESS), "Die Blase hält den Ritter nicht fest")),
                new Step(SeaWitchEntity.BUBBLE_LIFE + 4, () -> { }, () -> helper.assertTrue(knight.getHealth() < hp[0],
                        "Die Blase platzt nicht")),
                new Step(30, () -> {
                    place.accept(8.0);
                    witch.forceAttack(SeaWitchEntity.Attack.SHARKS);
                }, () -> {
                    helper.assertTrue(witch.sharkCount() == 2, "Sie ruft nicht zwei Haie: " + witch.sharkCount());
                    long tagged = level.getEntitiesOfClass(LivingEntity.class, around, e -> e.entityTags().contains(SeaWitchEntity.SHARK_TAG)).size();
                    helper.assertTrue(tagged == 2, "Die Haie tragen ihr Zeichen nicht: " + tagged);
                }),
                new Step(30, () -> {
                    place.accept(6.0);
                    lines[0] = CrystalLine.active();
                    witch.forceAttack(SeaWitchEntity.Attack.CORAL);
                }, () -> helper.assertTrue(knight.getHealth() < hp[0], "Die Korallenspieße treffen den Ritter nicht")),
                new Step(20, () -> {
                    place.accept(2.5);
                    witch.forceAttack(SeaWitchEntity.Attack.TAIL);
                }, () -> helper.assertTrue(knight.getHealth() < hp[0], "Der Schwanzhieb trifft den Ritter nicht")));
        run(helper, steps, 0, () -> songAndFury(helper, field, cauldron, witch, knight));
    }

    /** Der Sirenengesang (ein Spieler sieht sie an und wird gezogen), der Zorn unter halber Kraft und der Fluch. */
    private static void songAndFury(GameTestHelper helper, Field field, WitchCauldronBlockEntity cauldron, SeaWitchEntity witch, LivingEntity knight) {
        ServerLevel level = field.level();
        BlockPos o = field.origin();
        Vec3 spot = new Vec3(o.getX() + 0.5, o.getY() + 3.0, o.getZ() + 0.5);
        knight.teleportTo(spot.x + 9.0, spot.y, spot.z + 3.0);
        ServerPlayer player = MageGameTests.survivalPlayer(helper, "test-witch-listener", new io.netty.channel.embedded.EmbeddedChannel[1], 0);
        player.setNoGravity(true);
        // der Spieler schwebt acht Blöcke westlich und sieht die Hexe an
        move(player, spot.x - 8.0, spot.y - 1.5, spot.z);
        player.setYRot(-90.0f);
        player.setYHeadRot(-90.0f);
        player.setXRot(0.0f);
        witch.snapTo(spot.x, spot.y, spot.z, 90.0f, 0.0f);
        witch.setTarget(player);
        witch.forceAttack(SeaWitchEntity.Attack.SONG);
        helper.runAfterDelay(26, () -> {
            helper.assertTrue(player.hasEffect(MobEffects.NAUSEA), "Der Gesang betört den Spieler nicht");
            Vec3 v = player.getDeltaMovement();
            helper.assertTrue(v.x > 0.01, "Der Gesang zieht den Spieler nicht zu ihr: " + v);
            player.removeAllEffects();
            // unter halber Kraft: der Zorn
            witch.setHealth(SeaWitchEntity.MAX_HEALTH * 0.45f);
            witch.forceAttack(SeaWitchEntity.Attack.NONE);
            helper.runAfterDelay(4, () -> {
                helper.assertTrue(witch.currentAttack() == SeaWitchEntity.Attack.FURY, "Unter halber Kraft rast sie nicht: " + witch.currentAttack());
                helper.runAfterDelay(SeaWitchEntity.Attack.FURY.duration, () -> {
                    helper.assertTrue(witch.isEnraged(), "Nach dem Schrei ist sie nicht im Zorn");
                    move(player, spot.x - 5.0, spot.y - 1.5, spot.z);
                    witch.setTarget(player);
                    witch.forceAttack(SeaWitchEntity.Attack.CURSE);
                    helper.runAfterDelay(36, () -> {
                        helper.assertTrue(player.hasEffect(MobEffects.WEAKNESS), "Der Fluch der Tiefe trifft den Spieler nicht");
                        level.getServer().getPlayerList().remove(player);
                        death(helper, field, cauldron, witch);
                    });
                });
            });
        });
    }

    /** Tod: der Hexenstab, ihre Haie verschwinden, der Kessel erkaltet; eine Perle ruft sie zurück; zurückgesetzt in Trance. */
    private static void death(GameTestHelper helper, Field field, WitchCauldronBlockEntity cauldron, SeaWitchEntity witch) {
        ServerLevel level = field.level();
        BlockPos o = field.origin();
        BlockPos cauldronPos = WitchGrottoPiece.cauldron(o);
        AABB around = new AABB(o).inflate(40, 24, 40);
        witch.forceAttack(SeaWitchEntity.Attack.NONE);
        BossLootGameTests.firstVictory(helper, witch, Float.MAX_VALUE);
        helper.assertTrue(witch.isDeadOrDying(), "Die Hexe stirbt nicht");
        helper.runAfterDelay(SeaWitchEntity.DEATH_TICKS + 6, () -> {
            helper.assertTrue(witch.isRemoved(), "Die Hexe zerfließt nicht");
            boolean crystal = !level.getEntitiesOfClass(ItemEntity.class, around, e -> e.getItem().is(io.github.sirschrolli.kingdomsatwar.registry.ModItems.HEART_CRYSTAL.get())).isEmpty();
            helper.assertTrue(crystal, "Sie lässt keinen Herzkristall fallen");
            long sharks = level.getEntitiesOfClass(LivingEntity.class, around, e -> e.entityTags().contains(SeaWitchEntity.SHARK_TAG)).size();
            helper.assertTrue(sharks == 0, "Ihre Haie bleiben: " + sharks);
            helper.assertTrue(level.getBlockState(cauldronPos).getValue(WitchCauldronBlock.PHASE) == WitchCauldronBlock.Phase.DEFEATED,
                    "Der Kessel erkaltet nicht");
            helper.assertTrue(cauldron.recall(level, null), "Eine Perle ruft sie nicht zurück");
            SeaWitchEntity next = cauldron.witch(level);
            helper.assertTrue(next != null && next.currentAttack() == SeaWitchEntity.Attack.AWAKEN, "Die gerufene Hexe erwacht nicht");
            cauldron.reset(level);
            helper.assertTrue(cauldron.witch(level) == null, "Zurückgesetzt bleibt die Hexe");
            helper.assertTrue(level.getBlockState(cauldronPos).getValue(WitchCauldronBlock.PHASE) == WitchCauldronBlock.Phase.SLEEPING,
                    "Zurückgesetzt brodelt der Kessel nicht wieder");
            field.close();
            helper.succeed();
        });
    }

    // =====================================================================================
    // Der Hexenstab
    // =====================================================================================

    /**
     * Der Hexenstab schleudert drei Tintenkugeln, die einem anvisierten Ritter folgen, ihn treffen und blenden; danach ruht er –
     * unter Wasser kürzer.
     */
    private static void staff(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (int x = 0; x < 9; x++) {
            for (int z = 0; z < 9; z++) {
                helper.setBlock(x, 0, z, SeaBlocks.TIDE_STONE_BRICKS.base().get());
            }
        }
        LivingEntity knight = ModEntities.SOLDIERS.get(SoldierType.KNIGHT).get().create(level, EntitySpawnReason.EVENT);
        if (knight == null) {
            helper.fail("Kein Ritter");
            return;
        }
        BlockPos kp = helper.absolutePos(new BlockPos(4, 1, 8));
        knight.snapTo(kp.getX() + 0.5, kp.getY(), kp.getZ() + 0.5, 0.0f, 0.0f);
        ((Mob) knight).setNoAi(true);
        knight.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200.0);
        knight.setHealth(200.0f);
        level.addFreshEntity(knight);
        ServerPlayer player = MageGameTests.survivalPlayer(helper, "test-witch-staff", new io.netty.channel.embedded.EmbeddedChannel[1], 0);
        BlockPos stand = helper.absolutePos(new BlockPos(4, 1, 1));
        player.teleportTo(level, stand.getX() + 0.5, stand.getY(), stand.getZ() + 0.5, java.util.Set.of(), 0.0f, 0.0f, true);
        ItemStack staff = new ItemStack(SeaWitch.WITCH_STAFF.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, staff);
        AABB area = new AABB(helper.absolutePos(BlockPos.ZERO)).inflate(16);
        helper.runAfterDelay(2, () -> {
            player.setYRot(0.0f);
            player.setXRot(0.0f);
            int before = level.getEntitiesOfClass(InkBoltEntity.class, area).size();
            staff.use(level, player, InteractionHand.MAIN_HAND);
            int after = level.getEntitiesOfClass(InkBoltEntity.class, area).size();
            helper.assertTrue(after - before == SeaWitchStaffItem.BOLTS, "Der Stab schleudert nicht drei Kugeln: " + (after - before));
            helper.assertTrue(player.getCooldowns().isOnCooldown(staff), "Der Stab ruht danach nicht");
            helper.assertTrue(SeaWitchStaffItem.COOLDOWN_WATER < SeaWitchStaffItem.COOLDOWN, "Unter Wasser ruht er nicht kürzer");
            helper.runAfterDelay(24, () -> {
                helper.assertTrue(knight.getHealth() < 200.0f, "Die Tinte trifft den Ritter nicht");
                helper.assertTrue(knight.hasEffect(MobEffects.BLINDNESS), "Die Tinte blendet den Ritter nicht");
                helper.assertTrue(player.getHealth() >= player.getMaxHealth(), "Die Tinte trifft den Träger");
                knight.discard();
                for (InkBoltEntity bolt : level.getEntitiesOfClass(InkBoltEntity.class, area)) {
                    bolt.discard();
                }
                level.getServer().getPlayerList().remove(player);
                helper.succeed();
            });
        });
    }
}
