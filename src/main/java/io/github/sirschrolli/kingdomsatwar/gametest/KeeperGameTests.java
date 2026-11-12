package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.fae.KeeperStoneBlock;
import io.github.sirschrolli.kingdomsatwar.block.fae.KeeperStoneBlockEntity;
import io.github.sirschrolli.kingdomsatwar.entity.fae.CrystalLine;
import io.github.sirschrolli.kingdomsatwar.entity.fae.KeeperBoulderEntity;
import io.github.sirschrolli.kingdomsatwar.entity.fae.StoneKeeperEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.fae.Mondhain;
import io.github.sirschrolli.kingdomsatwar.fae.Mondsteinbruch;
import io.github.sirschrolli.kingdomsatwar.fae.MystikStage;
import io.github.sirschrolli.kingdomsatwar.fae.feature.MondsteinbruchFeature;
import io.github.sirschrolli.kingdomsatwar.item.fae.KeeperMaulItem;
import io.github.sirschrolli.kingdomsatwar.item.fae.MoonstoneArmor;
import io.github.sirschrolli.kingdomsatwar.loot.ModLootTables;
import io.github.sirschrolli.kingdomsatwar.magic.Mana;
import io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.FaeGear;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.worldgen.SpacedPlacement;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Tests zum Steinhüter und zum alten Mondsteinbruch (0.96): Mondsteinerz und Mondsteinrüstung, der Hüterhammer, der Hüterstein
 * (der Hüter kniet schlafend, erwacht, wenn man an den Grund tritt), alle Angriffe, Kristallpanzer und Benommenheit, Tod,
 * erloschener Stein und Rückruf mit Mondstein; dazu wo die Brüche liegen und wie einer gebaut wird.
 */
final class KeeperGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> MOONSTONE =
            ModGameTests.TEST_FUNCTIONS.register("keeper_moonstone", () -> KeeperGameTests::moonstone);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FIGHT =
            ModGameTests.TEST_FUNCTIONS.register("keeper_fight", () -> KeeperGameTests::fight);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> QUARRY =
            ModGameTests.TEST_FUNCTIONS.register("keeper_quarry", () -> KeeperGameTests::quarry);

    private KeeperGameTests() {}

    /** Lädt die Klasse (meldet die Testfunktionen an). */
    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var test : List.of(MOONSTONE, QUARRY)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 300, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
        event.registerTest(FIGHT.getId(), new FunctionGameTestInstance(FIGHT.key(),
                new TestData<>(environment, Level.OVERWORLD, ARENA, 1600, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
    }

    private static void floor(GameTestHelper helper) {
        for (int x = 0; x < 9; x++) {
            for (int z = 0; z < 9; z++) {
                helper.setBlock(x, 0, z, FaeBlocks.FAESTONE_BRICKS.base().get());
            }
        }
    }

    // =====================================================================================
    // Mondstein, Mondsteinrüstung, Hüterhammer
    // =====================================================================================

    /**
     * Mondsteinerz braucht eine Eisenspitzhacke und gibt Mondstein; die Mondsteinrüstung fasst je Teil mehr Mana (der volle Satz
     * 80) und lässt es schneller zurückkehren; der Hüterhammer lässt eine Kristallspur vor dem Träger aufbrechen, die einen
     * Ritter trifft (den Träger nicht), und ruht danach.
     */
    private static void moonstone(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper);
        BlockState ore = FaeBlocks.MOONSTONE_ORE.get().defaultBlockState();
        helper.assertTrue(ore.requiresCorrectToolForDrops(), "Mondsteinerz fällt auch ohne Werkzeug");
        helper.assertFalse(new ItemStack(Items.STONE_PICKAXE).isCorrectToolForDrops(ore), "Eine Steinspitzhacke reicht für Mondsteinerz");
        helper.assertTrue(new ItemStack(Items.IRON_PICKAXE).isCorrectToolForDrops(ore), "Eine Eisenspitzhacke reicht nicht für Mondsteinerz");
        BlockPos orePos = helper.absolutePos(new BlockPos(1, 1, 1));
        level.setBlock(orePos, ore, Block.UPDATE_ALL);
        List<ItemStack> drops = Block.getDrops(ore, level, orePos, null, null, new ItemStack(Items.IRON_PICKAXE));
        helper.assertTrue(drops.stream().anyMatch(s -> s.is(FaeBlocks.MOONSTONE.get())), "Mondsteinerz gibt keinen Mondstein: " + drops);
        level.setBlock(orePos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);

        ServerPlayer player = MageGameTests.survivalPlayer(helper, "test-moonstone-mage", new io.netty.channel.embedded.EmbeddedChannel[1], 0);
        BlockPos stand = helper.absolutePos(new BlockPos(4, 1, 1));
        player.teleportTo(level, stand.getX() + 0.5, stand.getY(), stand.getZ() + 0.5, java.util.Set.of(), 0.0f, 0.0f, true);
        float plain = Mana.maxFor(player);
        helper.assertTrue(MoonstoneArmor.regenFactor(player) == 1.0f, "Ohne Rüstung erholt sich das Mana schneller");
        player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(FaeGear.MOONSTONE_HELMET.get()));
        helper.assertTrue(MoonstoneArmor.pieces(player) == 1, "Der Stirnreif zählt nicht");
        helper.assertTrue(Math.abs(Mana.maxFor(player) - plain - MoonstoneArmor.MANA_PER_PIECE) < 0.01f, "Ein Teil fasst nicht mehr Mana");
        player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(FaeGear.MOONSTONE_CHESTPLATE.get()));
        player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(FaeGear.MOONSTONE_LEGGINGS.get()));
        player.setItemSlot(EquipmentSlot.FEET, new ItemStack(FaeGear.MOONSTONE_BOOTS.get()));
        helper.assertTrue(MoonstoneArmor.pieces(player) == 4, "Der volle Satz zählt nicht");
        helper.assertTrue(Math.abs(Mana.maxFor(player) - plain - 80.0f) < 0.01f, "Der volle Satz fasst nicht 80 Mana mehr: "
                + (Mana.maxFor(player) - plain));
        helper.assertTrue(Math.abs(MoonstoneArmor.regenFactor(player) - 1.5f) < 0.01f, "Der volle Satz erholt nicht um die Hälfte schneller");
        for (EquipmentSlot slot : new EquipmentSlot[] {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            player.setItemSlot(slot, ItemStack.EMPTY);
        }

        // der Hüterhammer: Kristallspur nach Süden auf einen Ritter
        LivingEntity knight = ModEntities.SOLDIERS.get(SoldierType.KNIGHT).get().create(level, EntitySpawnReason.EVENT);
        if (knight == null) {
            helper.fail("Kein Ritter");
            return;
        }
        BlockPos kp = helper.absolutePos(new BlockPos(4, 1, 6));
        knight.snapTo(kp.getX() + 0.5, kp.getY(), kp.getZ() + 0.5, 0.0f, 0.0f);
        ((Mob) knight).setNoAi(true);
        knight.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200.0);
        knight.setHealth(200.0f);
        level.addFreshEntity(knight);
        ItemStack maul = new ItemStack(FaeGear.KEEPER_MAUL.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, maul);
        player.teleportTo(level, stand.getX() + 0.5, stand.getY(), stand.getZ() + 0.5, java.util.Set.of(), 0.0f, 0.0f, true);
        player.setOnGround(true);
        float ownHealth = player.getHealth();
        helper.runAfterDelay(2, () -> {
            player.setOnGround(true);
            player.setYRot(0.0f);
            player.setXRot(0.0f);
            int before = CrystalLine.active();
            maul.use(level, player, InteractionHand.MAIN_HAND);
            helper.assertTrue(CrystalLine.active() > before, "Der Hüterhammer lässt keine Kristallspur aufbrechen");
            helper.assertTrue(player.getCooldowns().isOnCooldown(maul), "Der Hüterhammer ruht danach nicht");
            helper.runAfterDelay(20, () -> {
                helper.assertTrue(knight.getHealth() < 200.0f, "Die Kristallspur trifft den Ritter nicht");
                helper.assertTrue(player.getHealth() >= ownHealth, "Die Kristallspur trifft den Träger");
                helper.assertTrue(KeeperMaulItem.COOLDOWN > 40, "Der Hüterhammer ruht zu kurz");
                knight.discard();
                level.getServer().getPlayerList().remove(player);
                helper.succeed();
            });
        });
    }

    // =====================================================================================
    // Der Kampf
    // =====================================================================================

    /** Ein eigenes Feld weit draußen (die Chunks ringsum bleiben geladen): eine Plattform mit der Mitte bei {@code origin}. */
    private record Field(ServerLevel level, BlockPos origin, List<long[]> chunks) {
        void close() {
            AABB area = new AABB(this.origin).inflate(48, 48, 48);
            for (Entity e : this.level.getEntitiesOfClass(Entity.class, area, e -> !(e instanceof net.minecraft.world.entity.player.Player))) {
                e.discard();
            }
            for (BlockPos p : BlockPos.betweenClosed(this.origin.offset(-16, -1, -16), this.origin.offset(16, 3, 16))) {
                this.level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
            for (long[] c : this.chunks) {
                this.level.setChunkForced((int) c[0], (int) c[1], false);
            }
        }
    }

    private static Field field(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos base = helper.absolutePos(BlockPos.ZERO);
        BlockPos origin = new BlockPos(base.getX() - 21000, 150, base.getZ() + 16000);
        List<long[]> chunks = new ArrayList<>();
        int reach = 56;
        for (int cx = (origin.getX() - reach) >> 4; cx <= (origin.getX() + reach) >> 4; cx++) {
            for (int cz = (origin.getZ() - reach) >> 4; cz <= (origin.getZ() + reach) >> 4; cz++) {
                level.getChunk(cx, cz);
                level.setChunkForced(cx, cz, true);
                chunks.add(new long[] {cx, cz});
            }
        }
        BlockState floor = FaeBlocks.FAESTONE_BRICKS.base().get().defaultBlockState();
        for (int x = -16; x <= 16; x++) {
            for (int z = -16; z <= 16; z++) {
                level.setBlock(origin.offset(x, -1, z), floor, Block.UPDATE_CLIENTS);
            }
        }
        return new Field(level, origin, chunks);
    }

    private static void move(ServerPlayer player, double x, double y, double z) {
        player.teleportTo(player.level(), x, y, z, java.util.Set.of(), 180.0f, 0.0f, true);
        player.level().getChunkSource().move(player);
    }

    /** Schritt für Schritt: {@code start} läuft, nach {@code delay} Ticks die Prüfung und der nächste Schritt. */
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
     * Der Hüterstein ist unzerstörbar und leuchtet; tritt ein Spieler in die Nähe, kniet der Hüter (schlafend, unverwundbar) an
     * seinem Platz; tritt er an den Grund, erwacht der Hüter (auch dabei unverwundbar). Dann alle Angriffe gegen einen Ritter:
     * Faustschlag, Stampfer, Felswurf, Kristallspur, Mondstrahl, Felssprung; der Kristallpanzer fängt drei Fünftel ab, bis er
     * zerspringt – benommen trifft man ihn härter. Besiegt lässt er den Hüterhammer fallen, der Stein erlischt, ein Mondstein ruft
     * einen neuen Hüter; zurückgesetzt kniet beim nächsten Besuch wieder einer.
     */
    private static void fight(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper);
        helper.assertTrue(ModEntities.STONE_KEEPER.get().getCategory() == MobCategory.MONSTER, "Steinhüter: falsche Gruppe");
        Field field = field(helper);
        BlockPos o = field.origin();
        level.setBlock(o, FaeBlocks.KEEPER_STONE.get().defaultBlockState(), Block.UPDATE_ALL);
        helper.assertTrue(level.getBlockState(o).getDestroySpeed(level, o) < 0, "Der Hüterstein lässt sich abbauen");
        helper.assertTrue(level.getBlockState(o).getLightEmission(level, o) > 0, "Der Hüterstein leuchtet nicht");
        if (!(level.getBlockEntity(o) instanceof KeeperStoneBlockEntity stone)) {
            helper.fail("Der Hüterstein hat keine Blockentität");
            return;
        }
        BlockPos home = o.offset(0, 0, MondsteinbruchFeature.HOME_DZ);
        helper.assertTrue(stone.home().equals(home), "Der Hüter kniet nicht vor dem Stein: " + stone.home());
        AABB area = new AABB(o).inflate(40, 24, 40);
        ServerPlayer player = MageGameTests.survivalPlayer(helper, "test-keeper-visitor", new io.netty.channel.embedded.EmbeddedChannel[1], 0);
        move(player, o.getX() + 0.5, o.getY(), o.getZ() + 26.5);
        helper.runAfterDelay(72, () -> {
            List<StoneKeeperEntity> keepers = level.getEntitiesOfClass(StoneKeeperEntity.class, area, e -> o.equals(e.lair()));
            helper.assertTrue(keepers.size() == 1, "Kein schlafender Hüter am Stein: " + keepers.size());
            StoneKeeperEntity keeper = keepers.getFirst();
            helper.assertTrue(keeper.isDormant(), "Der Hüter schläft nicht: " + keeper.currentAttack());
            helper.assertTrue(keeper.blockPosition().closerThan(home, 1.5), "Der Hüter kniet nicht an seinem Platz: " + keeper.blockPosition());
            helper.assertTrue(stone.owns(keeper), "Der Stein kennt seinen Hüter nicht");
            helper.assertTrue(level.getBlockState(o).getValue(KeeperStoneBlock.PHASE) == KeeperStoneBlock.Phase.SLEEPING, "Der Stein schläft nicht");
            float hp = keeper.getHealth();
            keeper.hurtServer(level, level.damageSources().generic(), 40.0f);
            helper.assertTrue(keeper.getHealth() >= hp, "Im Schlaf ist er verwundbar");
            move(player, home.getX() + 0.5, home.getY(), home.getZ() + 8.5);
            helper.runAfterDelay(14, () -> {
                helper.assertTrue(keeper.currentAttack() == StoneKeeperEntity.Attack.AWAKEN, "Er erwacht nicht: " + keeper.currentAttack());
                keeper.hurtServer(level, level.damageSources().generic(), 40.0f);
                helper.assertTrue(keeper.getHealth() >= hp, "Beim Erwachen ist er verwundbar");
                level.getServer().getPlayerList().remove(player);
                helper.runAfterDelay(StoneKeeperEntity.Attack.AWAKEN.duration + 4, () -> {
                    helper.assertTrue(keeper.currentAttack() != StoneKeeperEntity.Attack.AWAKEN, "Das Erwachen endet nicht");
                    helper.assertTrue(level.getBlockState(o).getValue(KeeperStoneBlock.PHASE) == KeeperStoneBlock.Phase.AWAKE,
                            "Der Stein merkt das Erwachen nicht");
                    attacks(helper, level, field, stone, keeper);
                });
            });
        });
    }

    private static void attacks(GameTestHelper helper, ServerLevel level, Field field, KeeperStoneBlockEntity stone, StoneKeeperEntity keeper) {
        BlockPos o = field.origin();
        LivingEntity knight = ModEntities.SOLDIERS.get(SoldierType.KNIGHT).get().create(level, EntitySpawnReason.EVENT);
        if (knight == null) {
            helper.fail("Kein Ritter");
            return;
        }
        ((Mob) knight).setNoAi(true);
        knight.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000.0);
        knight.setHealth(1000.0f);
        Vec3 spot = new Vec3(o.getX() + 6.5, o.getY(), o.getZ() - 8.5);
        knight.snapTo(spot.x, spot.y, spot.z + 2.6, 0.0f, 0.0f);
        level.addFreshEntity(knight);
        float[] hp = {0.0f};
        // vor jedem Angriff: der Ritter in {@code ahead} Blöcken vor dem Hüter (nach Süden), beide geheilt
        Consumer<Double> place = ahead -> {
            knight.removeAllEffects();
            knight.setHealth(knight.getMaxHealth());
            knight.setInvulnerableTime(0);
            knight.damageCooldownTime = 0;
            knight.teleportTo(spot.x, spot.y, spot.z + ahead);
            knight.setDeltaMovement(Vec3.ZERO);
            keeper.snapTo(spot.x, spot.y, spot.z, 0.0f, 0.0f);
            keeper.setYHeadRot(0.0f);
            keeper.setYBodyRot(0.0f);
            keeper.setDeltaMovement(Vec3.ZERO);
            keeper.setTarget(knight);
            hp[0] = knight.getHealth();
        };
        Predicate<String> hurt = what -> {
            helper.assertTrue(knight.getHealth() < hp[0], what + " trifft den Ritter nicht (Angriff jetzt " + keeper.currentAttack() + ")");
            return true;
        };
        AABB around = new AABB(o).inflate(40, 24, 40);
        List<Step> steps = List.of(
                new Step(StoneKeeperEntity.Attack.SMASH.duration - 4, () -> {
                    place.accept(2.6);
                    keeper.forceAttack(StoneKeeperEntity.Attack.SMASH);
                }, () -> hurt.test("Der Faustschlag")),
                new Step(StoneKeeperEntity.Attack.STOMP.duration - 2, () -> {
                    place.accept(3.2);
                    keeper.forceAttack(StoneKeeperEntity.Attack.STOMP);
                }, () -> hurt.test("Der Stampfer")),
                new Step(38, () -> {
                    place.accept(9.0);
                    keeper.forceAttack(StoneKeeperEntity.Attack.BOULDER);
                }, () -> helper.assertFalse(level.getEntitiesOfClass(KeeperBoulderEntity.class, around, e -> true).isEmpty(),
                        "Er wirft keinen Felsbrocken")),
                new Step(46, () -> { }, () -> hurt.test("Der Felsbrocken")),
                new Step(40, () -> {
                    place.accept(7.0);
                    keeper.forceAttack(StoneKeeperEntity.Attack.CRYSTALS);
                }, () -> hurt.test("Die Kristallspur")),
                new Step(56, () -> {
                    place.accept(8.0);
                    keeper.forceEnraged();
                    keeper.forceAttack(StoneKeeperEntity.Attack.BEAM);
                }, () -> {
                    hurt.test("Der Mondstrahl");
                    helper.assertTrue(knight.hasEffect(MobEffects.GLOWING), "Der Mondstrahl lässt den Ritter nicht leuchten");
                }),
                new Step(StoneKeeperEntity.LEAP_FLIGHT + 30, () -> {
                    place.accept(12.0);
                    keeper.forceAttack(StoneKeeperEntity.Attack.LEAP);
                }, () -> {
                    helper.assertTrue(keeper.distanceTo(knight) < 6.0, "Der Felssprung bringt ihn nicht zum Ritter: " + keeper.distanceTo(knight));
                    hurt.test("Die Landung");
                }),
                new Step(26, () -> {
                    place.accept(6.0);
                    keeper.forceAttack(StoneKeeperEntity.Attack.ARMOR);
                }, () -> helper.assertTrue(keeper.isArmored(), "Kein Kristallpanzer")));
        run(helper, steps, 0, () -> armorAndDeath(helper, level, field, stone, keeper, knight));
    }

    private static void armorAndDeath(GameTestHelper helper, ServerLevel level, Field field, KeeperStoneBlockEntity stone, StoneKeeperEntity keeper,
            LivingEntity knight) {
        BlockPos o = field.origin();
        knight.discard();
        keeper.setTarget(null);
        keeper.forceAttack(StoneKeeperEntity.Attack.NONE);
        keeper.setHealth(keeper.getMaxHealth());
        keeper.setInvulnerableTime(0);
        keeper.damageCooldownTime = 0;
        float before = keeper.getHealth();
        keeper.hurtServer(level, level.damageSources().generic(), 10.0f);
        float taken = before - keeper.getHealth();
        helper.assertTrue(Math.abs(taken - 10.0f * StoneKeeperEntity.ARMOR_FACTOR) < 0.01f, "Der Panzer fängt nicht drei Fünftel ab: " + taken);
        keeper.setInvulnerableTime(0);
        keeper.damageCooldownTime = 0;
        keeper.hurtServer(level, level.damageSources().generic(), StoneKeeperEntity.ARMOR_HEALTH);
        helper.assertFalse(keeper.isArmored(), "Der Panzer zerspringt nicht");
        helper.assertTrue(keeper.currentAttack() == StoneKeeperEntity.Attack.STAGGER, "Er ist nicht benommen: " + keeper.currentAttack());
        keeper.setInvulnerableTime(0);
        keeper.damageCooldownTime = 0;
        float staggered = keeper.getHealth();
        keeper.hurtServer(level, level.damageSources().generic(), 10.0f);
        helper.assertTrue(Math.abs(staggered - keeper.getHealth() - 10.0f * StoneKeeperEntity.STAGGER_FACTOR) < 0.01f,
                "Benommen trifft man ihn nicht härter: " + (staggered - keeper.getHealth()));
        // besiegt
        keeper.setInvulnerableTime(0);
        keeper.damageCooldownTime = 0;
        keeper.setHealth(1.0f);
        BossLootGameTests.firstVictory(helper, keeper, 50.0f);
        helper.assertTrue(keeper.isDeadOrDying(), "Der Hüter fällt nicht");
        helper.assertTrue(stone.phase() == KeeperStoneBlock.Phase.DEFEATED, "Der Stein merkt den Sieg nicht");
        helper.assertTrue(level.getBlockState(o).getValue(KeeperStoneBlock.PHASE) == KeeperStoneBlock.Phase.DEFEATED, "Der Stein erlischt nicht");
        AABB area = new AABB(o).inflate(40, 24, 40);
        helper.runAfterDelay(StoneKeeperEntity.DEATH_TICKS + 5, () -> {
            helper.assertTrue(keeper.isRemoved(), "Er zerfällt nicht");
            List<ItemEntity> drops = level.getEntitiesOfClass(ItemEntity.class, area, e -> true);
            helper.assertTrue(drops.stream().anyMatch(e -> e.getItem().is(FaeGear.KEEPER_MAUL.get())), "Er lässt den Hüterhammer nicht fallen");
            helper.assertTrue(drops.stream().anyMatch(e -> e.getItem().is(FaeBlocks.MOONSTONE.get())), "Er lässt keinen Mondstein fallen");
            drops.forEach(Entity::discard);
            helper.assertTrue(stone.recall(level, null), "Ein Mondstein ruft keinen neuen Hüter");
            List<StoneKeeperEntity> again = level.getEntitiesOfClass(StoneKeeperEntity.class, area, e -> e.isAlive() && o.equals(e.lair()));
            helper.assertTrue(again.size() == 1 && again.getFirst().currentAttack() == StoneKeeperEntity.Attack.AWAKEN,
                    "Der neue Hüter erhebt sich nicht");
            helper.assertTrue(level.getBlockState(o).getValue(KeeperStoneBlock.PHASE) == KeeperStoneBlock.Phase.AWAKE, "Der Stein leuchtet nicht wieder");
            helper.assertFalse(stone.recall(level, null), "Ein zweiter Hüter lässt sich rufen");
            stone.reset(level);
            helper.assertTrue(level.getEntitiesOfClass(StoneKeeperEntity.class, area, e -> e.isAlive() && o.equals(e.lair())).isEmpty(),
                    "Nach dem Zurücksetzen bleibt er");
            helper.assertTrue(stone.phase() == KeeperStoneBlock.Phase.SLEEPING, "Nach dem Zurücksetzen schläft der Stein nicht");
            level.setBlock(o, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            field.close();
            helper.succeed();
        });
    }

    // =====================================================================================
    // Der alte Mondsteinbruch
    // =====================================================================================

    /**
     * Wo die Brüche liegen (höchstens einer pro Feld, nie zu nahe an einem Mondhain oder einer Zauberbühne, der Fundort stimmt mit
     * der Platzierung überein) – und ein Bruch, gebaut auf einer Wiese: Hüterstein am Grund mit dem Platz des Hüters davor, drei
     * Stufen, Rampen, Gerüste mit Leitern, der Kran mit seiner Kette, die Hütte mit der Truhe, Lampen und Erzadern in den Wänden.
     */
    private static void quarry(GameTestHelper helper) {
        long seed = helper.getLevel().getSeed();
        int quarries = 0;
        for (int rx = -3; rx <= 3; rx++) {
            for (int rz = -3; rz <= 3; rz++) {
                var chunk = Mondsteinbruch.chunk(seed, rx, rz);
                helper.assertTrue(SpacedPlacement.isGridChunk(seed, Mondsteinbruch.SALT, chunk.x(), chunk.z(), Mondsteinbruch.SPACING,
                        Mondsteinbruch.SEPARATION), "Der Bruch liegt nicht im Chunk der Platzierung");
                if (!Mondsteinbruch.exists(seed, rx, rz)) {
                    continue;
                }
                quarries++;
                BlockPos center = Mondsteinbruch.center(seed, rx, rz);
                helper.assertTrue(Mondsteinbruch.isQuarryChunk(seed, chunk.x(), chunk.z()), "Der Chunk des Bruchs wird nicht erkannt");
                helper.assertFalse(Mondhain.near(seed, center.getX(), center.getZ(), Mondsteinbruch.MIN_DISTANCE), "Der Bruch liegt zu nahe an einem Mondhain");
                helper.assertFalse(MystikStage.near(seed, center.getX(), center.getZ(), Mondsteinbruch.MIN_DISTANCE),
                        "Der Bruch liegt zu nahe an einer Zauberbühne");
                helper.assertTrue(Mondsteinbruch.near(seed, center.getX() + 12, center.getZ() - 17, Mondsteinbruch.CLEARING), "Der Rand ist nicht frei");
                BlockPos found = Mondsteinbruch.nearest(seed, center.getX() + 30, center.getZ() - 20);
                helper.assertTrue(found != null && found.getX() == center.getX() && found.getZ() == center.getZ(), "Der nächste Bruch wird nicht gefunden");
            }
        }
        helper.assertTrue(quarries >= 15, "Zu wenige Mondsteinbrüche: " + quarries + " von 49 Feldern");

        // gebaut auf einer Wiese (im End, weit draußen und hoch oben)
        ServerLevel level = helper.getLevel().getServer().getLevel(Level.END);
        if (level == null) {
            level = helper.getLevel();
        }
        BlockPos test = helper.absolutePos(BlockPos.ZERO);
        int cx = ((Math.floorMod(test.getX(), 1024) + 12000) & ~15) + 8;
        int cz = ((Math.floorMod(test.getZ(), 1024) + 12000) & ~15) + 8;
        int y = 110;
        BlockState grass = FaeBlocks.FAE_GRASS.get().defaultBlockState();
        BlockState soil = FaeBlocks.FAE_SOIL.get().defaultBlockState();
        for (int dx = -31; dx <= 31; dx++) {
            for (int dz = -31; dz <= 31; dz++) {
                BlockPos p = new BlockPos(cx + dx, y, cz + dz);
                level.getChunk(p.getX() >> 4, p.getZ() >> 4);
                level.setBlock(p, grass, Block.UPDATE_CLIENTS);
                level.setBlock(p.below(), soil, Block.UPDATE_CLIENTS);
                level.setBlock(p.below(2), soil, Block.UPDATE_CLIENTS);
            }
        }
        BlockPos stonePos = MondsteinbruchFeature.build(level, RandomSource.create(4343), cx, cz);
        int bottom = y - MondsteinbruchFeature.DEPTH;
        helper.assertTrue(stonePos.equals(new BlockPos(cx, bottom + 1, cz)), "Der Hüterstein liegt nicht am Grund: " + stonePos);
        helper.assertTrue(level.getBlockState(stonePos).is(FaeBlocks.KEEPER_STONE.get()), "Kein Hüterstein am Grund: " + level.getBlockState(stonePos));
        if (!(level.getBlockEntity(stonePos) instanceof KeeperStoneBlockEntity stone)) {
            helper.fail("Der Hüterstein hat keine Blockentität");
            return;
        }
        helper.assertTrue(stone.home().equals(stonePos.offset(0, 0, MondsteinbruchFeature.HOME_DZ)), "Der Platz des Hüters stimmt nicht: " + stone.home());
        helper.assertTrue(level.getBlockState(stonePos.below()).is(FaeBlocks.MOONSTONE_BRICKS.base().get()), "Kein Sockel aus Mondsteinziegeln");
        for (int up = 0; up <= 3; up++) {
            helper.assertTrue(level.getBlockState(stone.home().above(up)).isAir(), "Der Platz des Hüters ist verbaut (" + up + ")");
        }
        for (int yy = bottom + 2; yy <= y + 6; yy++) {
            helper.assertTrue(level.getBlockState(new BlockPos(cx, yy, cz)).isAir(), "Über dem Hüterstein ist es verbaut (Höhe " + yy + ")");
        }
        // drei Stufen und der Rand: rundum (bis auf Rampen, Gerüste, Stapel und Geröll) auf ihrer Höhe
        double[] radii = {6.0, 11.0, 14.0, 19.0};
        for (int i = 0; i < radii.length; i++) {
            int expected = bottom + i * MondsteinbruchFeature.STEP;
            double share = plateau(level, cx, cz, radii[i], expected, y + 8);
            helper.assertTrue(share >= 0.7, "Stufe " + i + " liegt nicht auf Höhe " + expected + " (nur " + Math.round(share * 100) + " %)");
        }
        BlockState rim = level.getBlockState(new BlockPos(cx, y, cz + 19));
        helper.assertTrue(rim.is(FaeBlocks.FAE_GRASS.get()) || rim.is(FaeBlocks.FAE_MOSS.get()), "Der Südrand ist kein Gras: " + rim);
        int stairs = 0;
        int ladders = 0;
        int chains = 0;
        int lamps = 0;
        int ores = 0;
        int logs = 0;
        int vines = 0;
        int doors = 0;
        boolean chest = false;
        for (BlockPos p : BlockPos.betweenClosed(cx - 27, bottom - 1, cz - 27, cx + 27, y + 14, cz + 27)) {
            BlockState s = level.getBlockState(p);
            if (s.isAir()) {
                continue;
            }
            if (s.is(FaeBlocks.FAESTONE_BRICKS.stairs().get())) {
                stairs++;
            } else if (s.is(Blocks.LADDER)) {
                ladders++;
            } else if (s.is(Blocks.IRON_CHAIN)) {
                chains++;
            } else if (s.is(FaeBlocks.CHISELED_MOONSTONE.get())) {
                lamps++;
            } else if (s.is(FaeBlocks.MOONSTONE_ORE.get())) {
                ores++;
            } else if (s.is(FaeBlocks.GLIMMERWOOD_LOG.get())) {
                logs++;
            } else if (s.is(FaeBlocks.GLOW_VINES.get())) {
                vines++;
            } else if (s.is(FaeBlocks.GLIMMERWOOD_DOOR.get())) {
                doors++;
            } else if (s.is(Blocks.CHEST) && level.getBlockEntity(p) instanceof ChestBlockEntity box
                    && ModLootTables.MOONSTONE_QUARRY_CHEST.equals(box.getLootTable())) {
                chest = true;
            }
        }
        helper.assertTrue(stairs >= 6, "Zu wenige Stufen in den Rampen: " + stairs);
        helper.assertTrue(ladders >= 4, "Keine Leiter am Gerüst: " + ladders);
        helper.assertTrue(chains >= 8, "Der Kran hat keine Kette: " + chains);
        helper.assertTrue(lamps >= 12, "Zu wenige Mondsteinlampen: " + lamps);
        helper.assertTrue(ores >= 12, "Zu wenig Mondsteinerz in den Wänden: " + ores);
        helper.assertTrue(logs >= 20, "Zu wenig Glimmerholz (Gerüste, Kran, Hütte): " + logs);
        helper.assertTrue(vines >= 4, "Zu wenige Leuchtranken: " + vines);
        helper.assertTrue(doors == 2, "Die Hütte hat keine Tür: " + doors);
        helper.assertTrue(chest, "Keine Truhe des Vorarbeiters");
        level.setBlock(stonePos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        helper.succeed();
    }

    /** Anteil der Richtungen (alle 10°), in denen der Boden im Abstand {@code radius} auf Höhe {@code expected} (±1) liegt. */
    private static double plateau(ServerLevel level, int cx, int cz, double radius, int expected, int from) {
        int hits = 0;
        for (int i = 0; i < 36; i++) {
            double a = Math.toRadians(i * 10.0);
            int found = surface(level, cx + (int) Math.round(Math.cos(a) * radius), cz + (int) Math.round(Math.sin(a) * radius), from);
            if (Math.abs(found - expected) <= 1) {
                hits++;
            }
        }
        return hits / 36.0;
    }

    /** Oberster fester Block der Spalte unter {@code from} (Pflanzen, Leuchtranken und Ketten zählen nicht). */
    private static int surface(ServerLevel level, int x, int z, int from) {
        for (int y = from; y > from - 40; y--) {
            BlockState s = level.getBlockState(new BlockPos(x, y, z));
            if (!s.isAir() && s.isSolid()) {
                return y;
            }
        }
        return Integer.MIN_VALUE;
    }
}
