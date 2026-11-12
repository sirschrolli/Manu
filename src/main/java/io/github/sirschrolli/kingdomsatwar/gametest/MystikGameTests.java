package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.fae.StageHatBlock;
import io.github.sirschrolli.kingdomsatwar.block.fae.StageHatBlockEntity;
import io.github.sirschrolli.kingdomsatwar.entity.fae.LightDoveEntity;
import io.github.sirschrolli.kingdomsatwar.entity.fae.MagicCardEntity;
import io.github.sirschrolli.kingdomsatwar.entity.fae.MagicHatEntity;
import io.github.sirschrolli.kingdomsatwar.entity.fae.MagicSwordEntity;
import io.github.sirschrolli.kingdomsatwar.entity.fae.MissMystikEntity;
import io.github.sirschrolli.kingdomsatwar.entity.fae.MystikImageEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.fae.Mondhain;
import io.github.sirschrolli.kingdomsatwar.fae.MystikStage;
import io.github.sirschrolli.kingdomsatwar.fae.feature.MystikStageFeature;
import io.github.sirschrolli.kingdomsatwar.item.fae.MysticTopHatEffects;
import io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.MystikBlocks;
import io.github.sirschrolli.kingdomsatwar.worldgen.SpacedPlacement;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
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
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Tests zu MissMystik, der Feen-Zauberkünstlerin (0.66): der große Zylinder (Auftritt, Zugabe, Finale, Sieg, Rückruf mit
 * Feenstaub), alle ihre Tricks, das Kartendeck und der Zylinder der Mystik, die Zauberbühne und wo sie liegt. Alles, wobei
 * sie schwebt, spielt auf eigenen Feldern weit draußen.
 */
final class MystikGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> HAT =
            ModGameTests.TEST_FUNCTIONS.register("mystik_hat", () -> MystikGameTests::hat);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TRICKS =
            ModGameTests.TEST_FUNCTIONS.register("mystik_tricks", () -> MystikGameTests::tricks);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> GEAR =
            ModGameTests.TEST_FUNCTIONS.register("mystik_gear", () -> MystikGameTests::gear);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> STAGE =
            ModGameTests.TEST_FUNCTIONS.register("mystik_stage", () -> MystikGameTests::stage);

    private MystikGameTests() {}

    /** Lädt die Klasse (meldet die Testfunktionen an). */
    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var test : List.of(GEAR, STAGE)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 300, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
        for (var test : List.of(HAT, TRICKS)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 1600, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
    }

    private static void floor(GameTestHelper helper) {
        for (int x = 0; x < 9; x++) {
            for (int z = 0; z < 9; z++) {
                helper.setBlock(x, 0, z, FaeBlocks.FAESTONE_BRICKS.base().get());
            }
        }
    }

    /** Ein eigenes Feld weit draußen (die Chunks ringsum bleiben geladen): eine Plattform mit der Mitte bei {@code origin}. */
    private record Field(ServerLevel level, BlockPos origin, List<long[]> chunks) {
        void close() {
            AABB area = new AABB(this.origin).inflate(48, 48, 48);
            for (Entity e : this.level.getEntitiesOfClass(Entity.class, area, e -> !(e instanceof net.minecraft.world.entity.player.Player))) {
                e.discard();
            }
            for (BlockPos p : BlockPos.betweenClosed(this.origin.offset(-6, -1, -6), this.origin.offset(6, 2, 6))) {
                this.level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
            for (long[] c : this.chunks) {
                this.level.setChunkForced((int) c[0], (int) c[1], false);
            }
        }
    }

    private static Field field(GameTestHelper helper, int slot) {
        ServerLevel level = helper.getLevel();
        BlockPos base = helper.absolutePos(BlockPos.ZERO);
        BlockPos origin = new BlockPos(base.getX() - 15000 - slot * 400, 150, base.getZ() + 16000);
        List<long[]> chunks = new java.util.ArrayList<>();
        int reach = 44;
        for (int cx = (origin.getX() - reach) >> 4; cx <= (origin.getX() + reach) >> 4; cx++) {
            for (int cz = (origin.getZ() - reach) >> 4; cz <= (origin.getZ() + reach) >> 4; cz++) {
                level.getChunk(cx, cz);
                level.setChunkForced(cx, cz, true);
                chunks.add(new long[] {cx, cz});
            }
        }
        for (int x = -5; x <= 5; x++) {
            for (int z = -5; z <= 5; z++) {
                level.setBlock(origin.offset(x, -1, z), FaeBlocks.FAESTONE_BRICKS.base().get().defaultBlockState(), Block.UPDATE_CLIENTS);
            }
        }
        return new Field(level, origin, chunks);
    }

    private static void move(ServerPlayer player, double x, double y, double z) {
        player.teleportTo(player.level(), x, y, z, java.util.Set.of(), 180.0f, 0.0f, true);
        player.level().getChunkSource().move(player);
    }

    // =====================================================================================
    // Der große Zylinder
    // =====================================================================================

    /**
     * Der Zylinder: unzerstörbar; tritt ein Spieler heran, beginnt die Vorstellung – MissMystik steckt erst unsichtbar und
     * unverwundbar im Hut, springt dann heraus und verbeugt sich. Danach ist sie verwundbar; unter 60 % gibt sie eine
     * Zugabe (Rampenlicht), unter 30 % das große Finale. Besiegt lässt sie Kartendeck und Zylinder fallen, der Hut ist leer;
     * Feenstaub ruft sie zurück.
     */
    private static void hat(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper);
        Field field = field(helper, 0);
        helper.assertTrue(ModEntities.MISS_MYSTIK.get().getCategory() == MobCategory.MONSTER, "MissMystik: falsche Gruppe");
        BlockPos pos = field.origin();
        level.setBlock(pos, MystikBlocks.STAGE_HAT.get().defaultBlockState(), Block.UPDATE_ALL);
        helper.assertTrue(level.getBlockState(pos).getDestroySpeed(level, pos) < 0, "Der Zylinder lässt sich abbauen");
        helper.assertTrue(level.getBlockState(pos).getLightEmission(level, pos) > 0, "Der Zylinder leuchtet nicht");
        if (!(level.getBlockEntity(pos) instanceof StageHatBlockEntity hat)) {
            helper.fail("Der Zylinder hat keine Blockentität");
            return;
        }
        helper.assertTrue(hat.phase() == StageHatBlockEntity.Phase.WAITING, "Der Zylinder wartet nicht");
        AABB area = new AABB(pos).inflate(24, 24, 24);
        ServerPlayer player = MageGameTests.survivalPlayer(helper, "test-mystik-visitor", new io.netty.channel.embedded.EmbeddedChannel[1], 0);
        move(player, pos.getX() + 0.5, pos.getY(), pos.getZ() + 5.5);
        helper.runAfterDelay(15, () -> {
            var performers = level.getEntitiesOfClass(MissMystikEntity.class, area, e -> pos.equals(e.home()));
            helper.assertTrue(performers.size() == 1, "MissMystik tritt nicht auf: " + performers.size() + " (Hut " + hat.phase() + ")");
            MissMystikEntity mystik = performers.getFirst();
            level.getServer().getPlayerList().remove(player);
            helper.assertTrue(mystik.currentAttack() == MissMystikEntity.Attack.APPEAR, "Kein Auftritt: " + mystik.currentAttack());
            helper.assertTrue(mystik.isHidden(), "Vor dem Knall ist sie schon zu sehen");
            helper.assertTrue(hat.owns(mystik), "Der Zylinder kennt seine Künstlerin nicht");
            helper.assertTrue(level.getBlockState(pos).getValue(StageHatBlock.PHASE) == StageHatBlock.Phase.SHOW, "Der Hut merkt es nicht");
            float hp = mystik.getHealth();
            mystik.hurtServer(level, level.damageSources().generic(), 50.0f);
            helper.assertTrue(mystik.getHealth() >= hp, "Im Hut ist sie verwundbar");
            // ein Zuschauer (der Spieler ist fort): ein Ritter auf dem Feld
            LivingEntity knight = ModEntities.SOLDIERS.get(SoldierType.KNIGHT).get().create(level, net.minecraft.world.entity.EntitySpawnReason.EVENT);
            if (knight != null) {
                knight.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 4.5, 0.0f, 0.0f);
                ((net.minecraft.world.entity.Mob) knight).setNoAi(true);
                knight.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000.0);
                knight.setHealth(1000.0f);
                level.addFreshEntity(knight);
            }
            helper.runAfterDelay(MissMystikEntity.APPEAR_TICKS, () -> {
                helper.assertTrue(mystik.currentAttack() != MissMystikEntity.Attack.APPEAR, "Der Auftritt endet nicht");
                helper.assertFalse(mystik.isHidden(), "Nach dem Auftritt bleibt sie unsichtbar");
                helper.assertTrue(mystik.getY() > pos.getY() + 1.0, "Sie springt nicht aus dem Hut: " + (mystik.getY() - pos.getY()));
                mystik.setInvulnerableTime(0);
                mystik.hurtServer(level, level.damageSources().generic(), 20.0f);
                helper.assertTrue(mystik.getHealth() < mystik.getMaxHealth(), "Nach dem Auftritt ist sie unverwundbar");
                // Zugabe unter 60 %
                if (knight != null) {
                    mystik.setTarget(knight);
                }
                mystik.forceAttack(MissMystikEntity.Attack.NONE);
                mystik.setHealth(mystik.getMaxHealth() * 0.55f);
                helper.runAfterDelay(3, () -> {
                    helper.assertTrue(mystik.isEncore(), "Unter 60 % gibt sie keine Zugabe");
                    helper.assertTrue(mystik.currentAttack() == MissMystikEntity.Attack.SPOTLIGHT, "Die Zugabe beginnt nicht mit dem Rampenlicht: "
                            + mystik.currentAttack());
                    mystik.setHealth(mystik.getMaxHealth() * 0.25f);
                    helper.runAfterDelay(3, () -> {
                        helper.assertTrue(mystik.currentAttack() == MissMystikEntity.Attack.FINALE, "Unter 30 % kein Finale: " + mystik.currentAttack());
                        mystik.setInvulnerableTime(0);
                        mystik.setHealth(1.0f);
                        BossLootGameTests.firstVictory(helper, mystik, 50.0f);
                        helper.assertTrue(mystik.isDeadOrDying(), "MissMystik fällt nicht");
                        helper.assertTrue(hat.phase() == StageHatBlockEntity.Phase.EMPTY, "Der Zylinder merkt den Sieg nicht");
                        helper.assertTrue(level.getBlockState(pos).getValue(StageHatBlock.PHASE) == StageHatBlock.Phase.EMPTY, "Der Hut ist nicht leer");
                        helper.runAfterDelay(MissMystikEntity.DEATH_TICKS + 5, () -> {
                            helper.assertTrue(mystik.isRemoved(), "Sie vergeht nicht");
                            var drops = level.getEntitiesOfClass(ItemEntity.class, area, e -> true);
                            helper.assertTrue(drops.stream().anyMatch(e -> e.getItem().is(io.github.sirschrolli.kingdomsatwar.registry.ModItems.HEART_CRYSTAL.get())),
                                    "Sie lässt keinen Herzkristall fallen");
                            drops.forEach(Entity::discard);
                            helper.assertTrue(hat.refill(level, null), "Feenstaub ruft sie nicht zurück");
                            var again = level.getEntitiesOfClass(MissMystikEntity.class, area, e -> e.isAlive() && pos.equals(e.home()));
                            helper.assertTrue(again.size() == 1 && again.getFirst().currentAttack() == MissMystikEntity.Attack.APPEAR,
                                    "Sie kehrt nicht zurück");
                            helper.assertFalse(hat.refill(level, null), "Der Hut lässt sich zweimal füllen");
                            hat.reset(level);
                            helper.assertTrue(level.getEntitiesOfClass(MissMystikEntity.class, area, e -> e.isAlive() && pos.equals(e.home())).isEmpty(),
                                    "Nach dem Zurücksetzen bleibt sie");
                            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                            field.close();
                            helper.succeed();
                        });
                    });
                });
            });
        });
    }

    // =====================================================================================
    // Die Tricks
    // =====================================================================================

    private static void reset(LivingEntity knight, Vec3 spot, float[] hp) {
        knight.removeAllEffects();
        knight.setHealth(knight.getMaxHealth());
        knight.setInvulnerableTime(0);
        knight.teleportTo(spot.x, spot.y, spot.z);
        knight.setDeltaMovement(Vec3.ZERO);
        hp[0] = knight.getHealth();
    }

    /** Schritt für Schritt: {@code step} läuft, danach nach {@code delay} Ticks die Prüfung und der nächste Schritt. */
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
     * Alle Tricks gegen einen schwebenden Ritter: Kartenfächer, Lichttauben, Verschwindetrick (unsichtbar, taucht neben ihm
     * auf, sticht), Spiegelkabinett (Spiegelbilder, die zerspringen, wenn sie selbst getroffen wird), Hut-Trick (Hut fliegt,
     * Knall, Hut kehrt zurück), Schwerterkiste, „Zieh eine Karte!“ (Pik trifft, Herz heilt), Rampenlicht und Finale.
     */
    private static void tricks(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper);
        Field field = field(helper, 1);
        BlockPos o = field.origin();
        LivingEntity knight = ModEntities.SOLDIERS.get(SoldierType.KNIGHT).get().create(level, net.minecraft.world.entity.EntitySpawnReason.EVENT);
        MissMystikEntity mystik = ModEntities.MISS_MYSTIK.get().create(level, net.minecraft.world.entity.EntitySpawnReason.EVENT);
        if (knight == null || mystik == null) {
            helper.fail("Ritter oder MissMystik lassen sich nicht erschaffen");
            return;
        }
        knight.snapTo(o.getX() + 0.5, o.getY(), o.getZ() + 0.5, 0.0f, 0.0f);
        ((net.minecraft.world.entity.Mob) knight).setNoAi(true);
        knight.setNoGravity(true);
        knight.getAttribute(Attributes.MAX_HEALTH).setBaseValue(400.0);
        knight.setHealth(400.0f);
        level.addFreshEntity(knight);
        mystik.snapTo(o.getX() + 0.5, o.getY() + 2.5, o.getZ() - 6.5, 0.0f, 0.0f);
        level.addFreshEntity(mystik);
        Vec3 spot = knight.position();
        AABB area = new AABB(o).inflate(40, 24, 40);
        float[] hp = {knight.getHealth()};
        float[] own = {0.0f};
        Runnable aim = () -> {
            reset(knight, spot, hp);
            mystik.setTarget(knight);
        };
        List<Step> steps = List.of(
                new Step(MissMystikEntity.CARDS_THROW + 1, () -> {
                    aim.run();
                    mystik.forceAttack(MissMystikEntity.Attack.CARDS);
                }, () -> helper.assertFalse(level.getEntitiesOfClass(MagicCardEntity.class, area, e -> e.getOwner() == mystik).isEmpty(),
                        "Sie wirft keine Karten")),
                new Step(22, () -> { }, () -> helper.assertTrue(knight.getHealth() < hp[0], "Der Kartenfächer trifft nicht")),
                new Step(MissMystikEntity.DOVES_SNAP + 2, () -> {
                    aim.run();
                    mystik.forceAttack(MissMystikEntity.Attack.DOVES);
                }, () -> helper.assertTrue(level.getEntitiesOfClass(LightDoveEntity.class, area, e -> e.getOwner() == mystik).size() >= 6,
                        "Keine Lichttauben")),
                new Step(60, () -> { }, () -> {
                    helper.assertTrue(knight.getHealth() < hp[0], "Die Lichttauben treffen nicht");
                    helper.assertTrue(knight.hasEffect(MobEffects.GLOWING), "Die Lichttauben lassen ihn nicht leuchten");
                }),
                new Step(MissMystikEntity.VANISH_POOF + 2, () -> {
                    aim.run();
                    mystik.forceAttack(MissMystikEntity.Attack.VANISH);
                }, () -> {
                    helper.assertTrue(mystik.isHidden(), "Sie verschwindet nicht");
                    float before = mystik.getHealth();
                    mystik.setInvulnerableTime(0);
                    mystik.hurtServer(level, level.damageSources().generic(), 10.0f);
                    helper.assertTrue(mystik.getHealth() >= before, "Verschwunden ist sie verwundbar");
                }),
                new Step(MissMystikEntity.VANISH_STRIKE - MissMystikEntity.VANISH_POOF + 1, () -> { }, () -> {
                    helper.assertFalse(mystik.isHidden(), "Sie taucht nicht wieder auf");
                    helper.assertTrue(mystik.distanceTo(knight) < 4.5, "Sie taucht nicht neben ihm auf: " + mystik.distanceTo(knight));
                    helper.assertTrue(knight.getHealth() < hp[0], "Der Stich nach dem Verschwinden trifft nicht");
                }),
                new Step(MissMystikEntity.MIRRORS_SPLIT + 2, () -> {
                    aim.run();
                    mystik.forceAttack(MissMystikEntity.Attack.MIRRORS);
                }, () -> {
                    int images = level.getEntitiesOfClass(MystikImageEntity.class, area, e -> e.isImageOf(mystik)).size();
                    helper.assertTrue(images == 3, "Kein Spiegelkabinett: " + images + " Spiegelbilder");
                    mystik.setInvulnerableTime(0);
                    mystik.hurtServer(level, level.damageSources().generic(), 5.0f);
                    helper.assertTrue(level.getEntitiesOfClass(MystikImageEntity.class, area, e -> e.isImageOf(mystik)).isEmpty(),
                            "Die Spiegelbilder zerspringen nicht, als sie getroffen wird");
                }),
                new Step(MissMystikEntity.HAT_THROW + 2, () -> {
                    aim.run();
                    mystik.forceAttack(MissMystikEntity.Attack.HAT);
                }, () -> {
                    helper.assertFalse(level.getEntitiesOfClass(MagicHatEntity.class, area).isEmpty(), "Der Zylinder fliegt nicht");
                    helper.assertFalse(mystik.wearsHat(), "Sie trägt den Hut noch");
                }),
                new Step(MagicHatEntity.FLY_TICKS + MagicHatEntity.BURST + 4, () -> { }, () ->
                        helper.assertTrue(knight.getHealth() < hp[0], "Der Knall des Hut-Tricks trifft nicht")),
                new Step(40, () -> knight.teleportTo(spot.x, spot.y, spot.z), () -> {
                    helper.assertTrue(mystik.wearsHat(), "Der Hut kehrt nicht zurück");
                    helper.assertTrue(level.getEntitiesOfClass(MagicHatEntity.class, area).isEmpty(), "Der fliegende Hut bleibt");
                }),
                new Step(8, () -> {
                    aim.run();
                    mystik.forceAttack(MissMystikEntity.Attack.SWORDS);
                }, () -> helper.assertTrue(level.getEntitiesOfClass(MagicSwordEntity.class, area).size() == 8, "Keine Schwerterkiste")),
                new Step(MissMystikEntity.SWORDS_STAB - 8 + 2, () -> { }, () ->
                        helper.assertTrue(knight.getHealth() < hp[0], "Die Schwerter treffen nicht")),
                new Step(MissMystikEntity.TRICK_FLIP + 20, () -> {
                    aim.run();
                    mystik.forceSuit(MissMystikEntity.Suit.SPADE);
                    mystik.forceAttack(MissMystikEntity.Attack.CARD_TRICK);
                }, () -> helper.assertTrue(knight.getHealth() < hp[0], "Pik trifft nicht")),
                new Step(MissMystikEntity.TRICK_FLIP + 22, () -> {
                    aim.run();
                    mystik.setHealth(mystik.getMaxHealth() * 0.8f);
                    own[0] = mystik.getHealth();
                    mystik.forceSuit(MissMystikEntity.Suit.HEART);
                    mystik.forceAttack(MissMystikEntity.Attack.CARD_TRICK);
                }, () -> helper.assertTrue(mystik.getHealth() > own[0], "Herz heilt sie nicht")),
                new Step(80, () -> {
                    aim.run();
                    mystik.forceSuit(null);
                    mystik.forceEncore();
                    mystik.forceAttack(MissMystikEntity.Attack.SPOTLIGHT);
                }, () -> helper.assertTrue(knight.getHealth() < hp[0], "Das Rampenlicht trifft nicht")),
                new Step(MissMystikEntity.FINALE_BLAST + 2, () -> {
                    aim.run();
                    mystik.forceAttack(MissMystikEntity.Attack.FINALE);
                }, () -> helper.assertTrue(knight.getHealth() < hp[0], "Das Finale trifft nicht")));
        run(helper, steps, 0, () -> {
            field.close();
            helper.succeed();
        });
    }

    // =====================================================================================
    // Kartendeck und Zylinder
    // =====================================================================================

    /**
     * Das Kartendeck: wirft drei Karten (Abklingzeit, kostet Haltbarkeit), jeder fünfte Wurf fünf; mit Feenstaub zu reparieren.
     * Der Zylinder der Mystik: auf dem Kopf zu tragen, schützt und bringt Glück; unter einem Drittel Leben verschwindet sein
     * Träger (unsichtbar und schnell) – aber nur alle anderthalb Minuten.
     */
    private static void gear(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper);
        ItemStack deck = new ItemStack(MystikBlocks.MYSTIC_DECK.get());
        helper.assertTrue(deck.isValidRepairItem(new ItemStack(FaeBlocks.FAE_DUST.get())), "Feenstaub repariert das Kartendeck nicht");
        helper.assertTrue(deck.is(ItemTags.DURABILITY_ENCHANTABLE), "Das Kartendeck ist nicht verzauberbar");
        ServerPlayer player = MageGameTests.survivalPlayer(helper, "test-mystik-gear", new io.netty.channel.embedded.EmbeddedChannel[1], 0);
        BlockPos at = helper.absolutePos(new BlockPos(4, 1, 1));
        move(player, at.getX() + 0.5, at.getY(), at.getZ() + 0.5);
        player.setGameMode(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, deck);
        AABB area = new AABB(at).inflate(12);
        MystikBlocks.MYSTIC_DECK.get().use(level, player, InteractionHand.MAIN_HAND);
        var cards = level.getEntitiesOfClass(MagicCardEntity.class, area, e -> e.getOwner() == player);
        helper.assertTrue(cards.size() == 3, "Das Kartendeck wirft nicht drei Karten: " + cards.size());
        ItemStack held = player.getItemInHand(InteractionHand.MAIN_HAND);
        helper.assertTrue(player.getCooldowns().isOnCooldown(held), "Das Kartendeck hat keine Abklingzeit");
        helper.assertTrue(held.getDamageValue() == 1, "Ein Wurf kostet keine Haltbarkeit");
        cards.forEach(Entity::discard);
        for (int i = 0; i < 4; i++) {
            player.getCooldowns().removeCooldown(player.getCooldowns().getCooldownGroup(held));
            MystikBlocks.MYSTIC_DECK.get().use(level, player, InteractionHand.MAIN_HAND);
            if (i < 3) {
                level.getEntitiesOfClass(MagicCardEntity.class, area, e -> e.getOwner() == player).forEach(Entity::discard);
            }
        }
        int ace = level.getEntitiesOfClass(MagicCardEntity.class, area, e -> e.getOwner() == player).size();
        helper.assertTrue(ace == 5, "Der fünfte Wurf ist kein Ass im Ärmel: " + ace);
        level.getEntitiesOfClass(MagicCardEntity.class, area, e -> e.getOwner() == player).forEach(Entity::discard);
        // der Zylinder
        ItemStack topHat = new ItemStack(MystikBlocks.MYSTIC_TOP_HAT.get());
        var equippable = topHat.get(DataComponents.EQUIPPABLE);
        helper.assertTrue(equippable != null && equippable.slot() == EquipmentSlot.HEAD, "Der Zylinder lässt sich nicht aufsetzen");
        helper.assertTrue(topHat.isValidRepairItem(new ItemStack(FaeBlocks.FAE_DUST.get())), "Feenstaub repariert den Zylinder nicht");
        player.setItemSlot(EquipmentSlot.HEAD, topHat);
        var modifiers = topHat.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, net.minecraft.world.item.component.ItemAttributeModifiers.EMPTY);
        double[] armor = {0.0};
        double[] luck = {0.0};
        modifiers.forEach(EquipmentSlot.HEAD, (attribute, modifier) -> {
            if (attribute.is(Attributes.ARMOR)) {
                armor[0] += modifier.amount();
            } else if (attribute.is(Attributes.LUCK)) {
                luck[0] += modifier.amount();
            }
        });
        helper.runAfterDelay(2, () -> {
            helper.assertTrue(armor[0] >= 3.0, "Der Zylinder schützt nicht");
            helper.assertTrue(luck[0] >= 1.0, "Der Zylinder bringt kein Glück");
            player.setHealth(player.getMaxHealth());
            player.connection.markClientLoaded();   // sonst ist er unverwundbar wie ein Spieler, der gerade erst die Welt lädt
            player.hurtServer(level, level.damageSources().generic(), player.getMaxHealth() * 0.8f);
            helper.assertTrue(player.hasEffect(MobEffects.INVISIBILITY), "Kein Verschwindetrick unter einem Drittel Leben (Leben "
                    + player.getHealth() + "/" + player.getMaxHealth() + ", Kopf " + player.getItemBySlot(EquipmentSlot.HEAD) + ")");
            helper.assertTrue(player.hasEffect(MobEffects.SPEED), "Der Verschwindetrick macht nicht schnell");
            player.removeAllEffects();
            helper.assertFalse(MysticTopHatEffects.vanish(level, player), "Der Verschwindetrick geht ohne Abklingzeit");
            level.getServer().getPlayerList().remove(player);
            helper.succeed();
        });
    }

    // =====================================================================================
    // Die Zauberbühne
    // =====================================================================================

    /**
     * Die Zauberbühne: wo sie liegt (einmal je Feld, nie nahe einem Mondhain; der Fundort stimmt mit der Platzierung
     * überein) – und gebaut auf einer Wiese: der Zylinder wartet mitten auf der Bühne, ringsum Teppich, Vorhänge im Portal,
     * Rampenlichter, Bänke für die Zuschauer, Stufen hinauf. Danach verschwindet der Zylinder wieder (er riefe sonst
     * MissMystik, sobald ein Spieler in die Nähe käme).
     */
    private static void stage(GameTestHelper helper) {
        long seed = 20_261_109L;
        int stages = 0;
        for (int rx = -3; rx <= 3; rx++) {
            for (int rz = -3; rz <= 3; rz++) {
                var chunk = MystikStage.chunk(seed, rx, rz);
                helper.assertTrue(SpacedPlacement.isGridChunk(seed, MystikStage.SALT, chunk.x(), chunk.z(), MystikStage.SPACING,
                        MystikStage.SEPARATION), "Die Bühne liegt nicht im Chunk der Platzierung");
                if (!MystikStage.exists(seed, rx, rz)) {
                    continue;
                }
                stages++;
                BlockPos center = MystikStage.center(seed, rx, rz);
                helper.assertTrue(MystikStage.isStageChunk(seed, chunk.x(), chunk.z()), "Der Bühnen-Chunk wird nicht erkannt");
                helper.assertFalse(Mondhain.near(seed, center.getX(), center.getZ(), MystikStage.MIN_DISTANCE_TO_GROVE),
                        "Die Bühne liegt zu nahe an einem Mondhain");
                helper.assertTrue(MystikStage.near(seed, center.getX() + 7, center.getZ() - 9, MystikStage.CLEARING), "Die Lichtung ist nicht frei");
                BlockPos found = MystikStage.nearest(seed, center.getX() + 20, center.getZ() + 25);
                helper.assertTrue(found != null && found.getX() == center.getX() && found.getZ() == center.getZ(),
                        "Die nächste Bühne wird nicht gefunden");
            }
        }
        helper.assertTrue(stages >= 20, "Zu wenige Zauberbühnen: " + stages + " von 49 Feldern");
        // gebaut auf einer Wiese (im End, weit draußen und hoch oben)
        ServerLevel level = helper.getLevel().getServer().getLevel(Level.END);
        if (level == null) {
            level = helper.getLevel();
        }
        BlockPos test = helper.absolutePos(BlockPos.ZERO);
        int cx = ((Math.floorMod(test.getX(), 1024) + 9000) & ~15) + 8;
        int cz = ((Math.floorMod(test.getZ(), 1024) + 9000) & ~15) + 8;
        int y = 110;
        BlockState grass = FaeBlocks.FAE_GRASS.get().defaultBlockState();
        BlockState soil = FaeBlocks.FAE_SOIL.get().defaultBlockState();
        for (int dx = -26; dx <= 26; dx++) {
            for (int dz = -26; dz <= 26; dz++) {
                BlockPos p = new BlockPos(cx + dx, y, cz + dz);
                level.getChunk(p.getX() >> 4, p.getZ() >> 4);
                level.setBlock(p, grass, Block.UPDATE_CLIENTS);
                level.setBlock(p.below(), soil, Block.UPDATE_CLIENTS);
                level.setBlock(p.below(2), soil, Block.UPDATE_CLIENTS);
            }
        }
        boolean built = MystikStageFeature.build(level, level.getChunkSource().getGenerator(), RandomSource.create(4242), cx, cz);
        helper.assertTrue(built, "Die Bühne wird nicht gebaut");
        BlockPos hatPos = new BlockPos(cx, y + MystikStageFeature.HAT_HEIGHT, cz + MystikStageFeature.HAT_DZ);
        helper.assertTrue(level.getBlockState(hatPos).is(MystikBlocks.STAGE_HAT.get()), "Kein Zylinder auf der Bühne: " + level.getBlockState(hatPos));
        helper.assertTrue(level.getBlockState(hatPos).getValue(StageHatBlock.PHASE) == StageHatBlock.Phase.WAITING, "Der Zylinder wartet nicht");
        helper.assertTrue(level.getBlockState(hatPos.below()).is(FaeBlocks.GLIMMERWOOD_PLANKS.base().get()), "Der Zylinder steht nicht auf der Bühne");
        helper.assertTrue(level.getBlockState(hatPos.offset(2, 0, 0)).is(Blocks.CARPET.pick(net.minecraft.world.item.DyeColor.MAGENTA)),
                "Kein Teppich um den Zylinder");
        int curtains = 0;
        int footlights = 0;
        int benches = 0;
        for (BlockPos p : BlockPos.betweenClosed(cx - 16, y, cz - 13, cx + 16, y + 14, cz + 14)) {
            BlockState s = level.getBlockState(p);
            if (s.is(MystikBlocks.VELVET_CURTAIN.get()) || s.is(MystikBlocks.VELVET_CURTAIN_FRINGE.get())) {
                curtains++;
            } else if (s.is(FaeBlocks.CHISELED_MOONSTONE.get()) && p.getY() == y + 2) {
                footlights++;
            } else if (s.is(FaeBlocks.GLIMMERWOOD_PLANKS.stairs().get()) && p.getZ() > cz + 6) {
                benches++;
            }
        }
        helper.assertTrue(curtains >= 150, "Zu wenige Vorhänge: " + curtains);
        helper.assertTrue(footlights >= 8, "Keine Rampenlichter: " + footlights);
        helper.assertTrue(benches >= 30, "Zu wenige Bänke: " + benches);
        helper.assertTrue(level.getBlockState(new BlockPos(cx, y + 1, cz + 6)).is(FaeBlocks.GLIMMERWOOD_PLANKS.stairs().get()),
                "Keine Stufen hinauf");
        helper.assertTrue(level.getBlockState(hatPos.above()).isAir() && level.getBlockState(hatPos.above(3)).isAir(), "Über dem Zylinder ist es verbaut");
        level.setBlock(hatPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        helper.succeed();
    }
}
