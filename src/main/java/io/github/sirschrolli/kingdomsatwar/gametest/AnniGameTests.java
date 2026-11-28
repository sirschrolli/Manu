package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.glitch.ErrorCoreBlock;
import io.github.sirschrolli.kingdomsatwar.block.glitch.ErrorCoreBlockEntity;
import io.github.sirschrolli.kingdomsatwar.entity.glitch.AfterimageEntity;
import io.github.sirschrolli.kingdomsatwar.entity.glitch.AnniTrollEntity;
import io.github.sirschrolli.kingdomsatwar.entity.glitch.GlitchCloneEntity;
import io.github.sirschrolli.kingdomsatwar.entity.glitch.MissingZoneEntity;
import io.github.sirschrolli.kingdomsatwar.entity.glitch.PixelCubeEntity;
import io.github.sirschrolli.kingdomsatwar.entity.glitch.ScanlineEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.item.glitch.GlitchWings;
import io.github.sirschrolli.kingdomsatwar.registry.AnniBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Tests zu Anni dem Troll (0.67): der Fehlerkern (Laden, Absturz unter halber Kraft, Wiederherstellung als Anni 2.0, Sieg,
 * Neustart mit Redstone), seine Angriffe (Klone, fehlende Textur, Pixelregen, Zeilenfehler, Rückruck, der Speedhack von
 * Anni 2.0), /kill ohne Wiederherstellung, Bannhammer und Fehlerflügel. Alles, was Platz braucht, spielt auf eigenen Feldern
 * weit draußen.
 */
final class AnniGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CORE =
            ModGameTests.TEST_FUNCTIONS.register("anni_core", () -> AnniGameTests::core);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ATTACKS =
            ModGameTests.TEST_FUNCTIONS.register("anni_attacks", () -> AnniGameTests::attacks);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SPEEDHACK =
            ModGameTests.TEST_FUNCTIONS.register("anni_speedhack", () -> AnniGameTests::speedhack);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> REVIVE =
            ModGameTests.TEST_FUNCTIONS.register("anni_revive", () -> AnniGameTests::revive);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> GEAR =
            ModGameTests.TEST_FUNCTIONS.register("anni_gear", () -> AnniGameTests::gear);

    private AnniGameTests() {}

    /** Lädt die Klasse (meldet die Testfunktionen an). */
    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        event.registerTest(GEAR.getId(), new FunctionGameTestInstance(GEAR.key(),
                new TestData<>(environment, Level.OVERWORLD, ARENA, 200, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        for (var test : List.of(CORE, ATTACKS, SPEEDHACK, REVIVE)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 1600, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
    }

    // =====================================================================================
    // Hilfen
    // =====================================================================================

    private static void floor(GameTestHelper helper) {
        for (int x = 0; x < 9; x++) {
            for (int z = 0; z < 9; z++) {
                helper.setBlock(x, 0, z, Blocks.STONE);
            }
        }
    }

    /** Ein eigenes Feld weit draußen (die Chunks ringsum bleiben geladen): eine breite Plattform mit der Mitte bei {@code origin}. */
    private record Field(ServerLevel level, BlockPos origin, int half, List<long[]> chunks) {
        AABB area() {
            return new AABB(this.origin).inflate(48, 32, 48);
        }

        void close() {
            for (Entity e : this.level.getEntitiesOfClass(Entity.class, this.area(), e -> !(e instanceof net.minecraft.world.entity.player.Player))) {
                e.discard();
            }
            for (BlockPos p : BlockPos.betweenClosed(this.origin.offset(-this.half, -1, -this.half), this.origin.offset(this.half, 2, this.half))) {
                this.level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
            for (long[] c : this.chunks) {
                this.level.setChunkForced((int) c[0], (int) c[1], false);
            }
        }
    }

    private static Field field(GameTestHelper helper, int slot, int half) {
        ServerLevel level = helper.getLevel();
        BlockPos base = helper.absolutePos(BlockPos.ZERO);
        BlockPos origin = new BlockPos(base.getX() + 15000 + slot * 400, 150, base.getZ() - 16000);
        List<long[]> chunks = new java.util.ArrayList<>();
        int reach = 48;
        for (int cx = (origin.getX() - reach) >> 4; cx <= (origin.getX() + reach) >> 4; cx++) {
            for (int cz = (origin.getZ() - reach) >> 4; cz <= (origin.getZ() + reach) >> 4; cz++) {
                level.getChunk(cx, cz);
                level.setChunkForced(cx, cz, true);
                chunks.add(new long[] {cx, cz});
            }
        }
        for (int x = -half; x <= half; x++) {
            for (int z = -half; z <= half; z++) {
                level.setBlock(origin.offset(x, -1, z), Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
        }
        return new Field(level, origin, half, chunks);
    }

    private static void move(ServerPlayer player, double x, double y, double z) {
        player.teleportTo(player.level(), x, y, z, java.util.Set.of(), 180.0f, 0.0f, true);
        player.level().getChunkSource().move(player);
    }

    /** Ein zäher Zuschauer (ein Ritter ohne eigenen Willen, 1000 Leben), auf den Anni losgeht. */
    private static LivingEntity dummy(ServerLevel level, BlockPos at) {
        LivingEntity knight = ModEntities.SOLDIERS.get(SoldierType.KNIGHT).get().create(level, EntitySpawnReason.EVENT);
        if (knight == null) {
            throw new IllegalStateException("kein Ritter");
        }
        knight.snapTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, 180.0f, 0.0f);
        ((Mob) knight).setNoAi(true);
        knight.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000.0);
        knight.setHealth(1000.0f);
        level.addFreshEntity(knight);
        return knight;
    }

    private static AnniTrollEntity anni(ServerLevel level, BlockPos at) {
        AnniTrollEntity anni = ModEntities.ANNI_TROLL.get().create(level, EntitySpawnReason.EVENT);
        if (anni == null) {
            throw new IllegalStateException("kein Anni");
        }
        anni.snapTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, 0.0f, 0.0f);
        level.addFreshEntity(anni);
        return anni;
    }

    // =====================================================================================
    // Fehlerkern: Laden, Absturz, Wiederherstellung, Sieg, Neustart
    // =====================================================================================

    /**
     * Der Fehlerkern: unzerstörbar und leuchtend; tritt ein Spieler heran, wird Anni geladen (erst unsichtbar und
     * unverwundbar). Unter halber Kraft stürzt er ab (unverwundbar bis zum Neustart) und kämpft im Fehlermodus weiter. Der
     * erste tödliche Treffer tötet ihn nicht: Er lädt seine Sicherung und kehrt als Anni 2.0 zurück (volle, aber kürzere
     * Leiste). Der zweite besiegt ihn – der Kern ist abgestürzt, und ein Neustart lädt Anni wieder.
     */
    private static void core(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper);
        Field field = field(helper, 0, 9);
        BlockPos pos = field.origin();
        level.setBlock(pos, AnniBlocks.ERROR_CORE.get().defaultBlockState(), Block.UPDATE_ALL);
        helper.assertTrue(level.getBlockState(pos).getDestroySpeed(level, pos) < 0, "Der Fehlerkern lässt sich abbauen");
        helper.assertTrue(level.getBlockState(pos).getLightEmission(level, pos) > 0, "Der Fehlerkern leuchtet nicht");
        if (!(level.getBlockEntity(pos) instanceof ErrorCoreBlockEntity core)) {
            helper.fail("Der Fehlerkern hat keine Blockentität");
            return;
        }
        helper.assertTrue(core.phase() == ErrorCoreBlockEntity.Phase.WAITING, "Der Kern wartet nicht");
        AABB area = field.area();
        ServerPlayer player = MageGameTests.survivalPlayer(helper, "test-anni-visitor", new io.netty.channel.embedded.EmbeddedChannel[1], 0);
        move(player, pos.getX() + 0.5, pos.getY(), pos.getZ() + 8.5);
        helper.runAfterDelay(15, () -> {
            var trolls = level.getEntitiesOfClass(AnniTrollEntity.class, area, e -> pos.equals(e.home()));
            helper.assertTrue(trolls.size() == 1, "Anni wird nicht geladen: " + trolls.size() + " (Kern " + core.phase() + ")");
            AnniTrollEntity anni = trolls.getFirst();
            level.getServer().getPlayerList().remove(player);
            helper.assertTrue(anni.currentAttack() == AnniTrollEntity.Attack.SPAWN, "Kein Laden: " + anni.currentAttack());
            helper.assertTrue(anni.isHidden(), "Vor dem Zusammensetzen ist er schon zu sehen");
            helper.assertTrue(core.owns(anni), "Der Kern kennt seinen Troll nicht");
            helper.assertTrue(level.getBlockState(pos).getValue(ErrorCoreBlock.PHASE) == ErrorCoreBlock.Phase.RUNNING, "Der Kern merkt es nicht");
            float hp = anni.getHealth();
            anni.hurtServer(level, level.damageSources().generic(), 50.0f);
            helper.assertTrue(anni.getHealth() >= hp, "Beim Laden ist er verwundbar");
            double designed = anni.designedMaxHealth();
            helper.assertTrue(Math.abs(designed - AnniTrollEntity.MAX_HEALTH) < 1.0, "Gedachte Lebenskraft: " + designed);
            helper.assertTrue(anni.healthScale() < 0.7f, "Über der Obergrenze zählt jeder Treffer voll: " + anni.healthScale());
            LivingEntity knight = dummy(level, pos.offset(0, 0, 7));
            helper.runAfterDelay(AnniTrollEntity.SPAWN_TICKS, () -> {
                helper.assertTrue(anni.currentAttack() != AnniTrollEntity.Attack.SPAWN, "Das Laden endet nicht");
                helper.assertFalse(anni.isHidden(), "Nach dem Laden bleibt er unsichtbar");
                anni.setTarget(knight);
                anni.forceAttack(AnniTrollEntity.Attack.NONE);
                anni.setInvulnerableTime(0);
                anni.hurtServer(level, level.damageSources().generic(), 20.0f);
                helper.assertTrue(anni.getHealth() < anni.getMaxHealth(), "Nach dem Laden ist er unverwundbar");
                // unter halber Kraft: Absturz
                anni.setHealth(anni.getMaxHealth() * 0.45f);
                helper.runAfterDelay(3, () -> {
                    helper.assertTrue(anni.currentAttack() == AnniTrollEntity.Attack.CRASH, "Unter halber Kraft stürzt er nicht ab: "
                            + anni.currentAttack());
                    float crashed = anni.getHealth();
                    anni.setInvulnerableTime(0);
                    anni.hurtServer(level, level.damageSources().generic(), 40.0f);
                    helper.assertTrue(anni.getHealth() >= crashed, "Im Absturz ist er verwundbar");
                    helper.runAfterDelay(AnniTrollEntity.Attack.CRASH.duration + 2, () -> {
                        helper.assertTrue(anni.isGlitchMode(), "Nach dem Neustart kein Fehlermodus");
                        helper.assertTrue(anni.currentAttack() != AnniTrollEntity.Attack.CRASH, "Der Absturz endet nicht");
                        // der erste tödliche Treffer: Wiederherstellung
                        anni.forceAttack(AnniTrollEntity.Attack.NONE);
                        anni.setInvulnerableTime(0);
                        anni.hurtServer(level, level.damageSources().generic(), 1.0e6f);
                        helper.assertFalse(anni.isDeadOrDying(), "Der erste tödliche Treffer tötet ihn");
                        helper.assertTrue(anni.isRevived() && anni.currentAttack() == AnniTrollEntity.Attack.REVIVE,
                                "Keine Wiederherstellung: " + anni.currentAttack());
                        helper.assertTrue(core.owns(anni), "Während der Wiederherstellung gehört er nicht mehr zum Kern");
                        helper.runAfterDelay(AnniTrollEntity.Attack.REVIVE.duration + 2, () -> {
                            helper.assertTrue(anni.isVersion2(), "Er kehrt nicht als Anni 2.0 zurück");
                            helper.assertFalse(anni.isHidden(), "Anni 2.0 bleibt unsichtbar");
                            double expected = designed * AnniTrollEntity.BACKUP_HEALTH;
                            helper.assertTrue(Math.abs(anni.designedMaxHealth() - expected) < 1.0, "Lebenskraft von Anni 2.0: "
                                    + anni.designedMaxHealth() + " statt " + expected);
                            helper.assertTrue(anni.getHealth() >= anni.getMaxHealth() - 0.5f, "Die Leiste ist nicht voll: " + anni.getHealth());
                            // der zweite: endgültig
                            anni.forceAttack(AnniTrollEntity.Attack.NONE);
                            anni.setInvulnerableTime(0);
                            anni.hurtServer(level, level.damageSources().generic(), 1.0e6f);
                            helper.assertTrue(anni.isDeadOrDying(), "Anni 2.0 stirbt nicht");
                            helper.runAfterDelay(4, () -> {
                                helper.assertTrue(core.phase() == ErrorCoreBlockEntity.Phase.CRASHED, "Der Kern stürzt nicht ab: " + core.phase());
                                helper.assertTrue(core.restart(level, null), "Der Kern startet nicht neu");
                                helper.assertTrue(core.phase() == ErrorCoreBlockEntity.Phase.RUNNING, "Nach dem Neustart läuft nichts");
                                core.reset(level);
                                helper.assertTrue(core.phase() == ErrorCoreBlockEntity.Phase.WAITING, "Der Kern wartet nicht wieder");
                                field.close();
                                helper.succeed();
                            });
                        });
                    });
                });
            });
        });
    }

    // =====================================================================================
    // Angriffe
    // =====================================================================================

    /**
     * Klone, fehlende Textur, Pixelregen, Zeilenfehler (als Anni 2.0 mit einer zweiten roten Zeile) und Rückruck: Jeder
     * hinterlässt, was er soll; beim Rückruck reißt es das Ziel an seine gemerkte Stelle zurück.
     */
    private static void attacks(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper);
        Field field = field(helper, 1, 16);
        BlockPos pos = field.origin();
        AABB area = field.area();
        AnniTrollEntity anni = anni(level, pos);
        LivingEntity knight = dummy(level, pos.offset(0, 0, 6));
        anni.setTarget(knight);
        anni.forceAttack(AnniTrollEntity.Attack.CLONES);
        helper.runAfterDelay(AnniTrollEntity.CLONES_SPLIT + 2, () -> {
            int clones = level.getEntitiesOfClass(GlitchCloneEntity.class, area, c -> c.isCloneOf(anni)).size();
            helper.assertTrue(clones == 3, "Klone: " + clones);
            anni.setTarget(knight);
            anni.forceAttack(AnniTrollEntity.Attack.MISSING);
            helper.runAfterDelay(AnniTrollEntity.MISSING_SLAM + 2, () -> {
                int zones = level.getEntitiesOfClass(MissingZoneEntity.class, area).size();
                helper.assertTrue(zones >= 1, "Kein Feld fehlender Textur");
                anni.setTarget(knight);
                anni.forceAttack(AnniTrollEntity.Attack.PIXEL_RAIN);
                helper.runAfterDelay(AnniTrollEntity.RAIN_START + 8, () -> {
                    int pixels = level.getEntitiesOfClass(PixelCubeEntity.class, area).size();
                    helper.assertTrue(pixels >= 2, "Kein Pixelregen: " + pixels);
                    anni.setTarget(knight);
                    anni.forceAttack(AnniTrollEntity.Attack.SCANLINE);
                    helper.runAfterDelay(AnniTrollEntity.SCAN_LOW + 2, () -> {
                        helper.assertTrue(level.getEntitiesOfClass(ScanlineEntity.class, area).size() >= 1, "Keine Zeile");
                        // Anni 2.0: mit der blauen Zeile kreist zugleich eine zweite rote
                        anni.forceVersion2();
                        anni.setTarget(knight);
                        anni.forceAttack(AnniTrollEntity.Attack.SCANLINE);
                        helper.runAfterDelay(AnniTrollEntity.SCAN_HIGH + 2, () -> {
                            int fresh = level.getEntitiesOfClass(ScanlineEntity.class, area, l -> l.tickCount <= 4).size();
                            helper.assertTrue(fresh == 2, "Anni 2.0: frische Zeilen " + fresh + " statt 2");
                            anni.setTarget(knight);
                            anni.forceAttack(AnniTrollEntity.Attack.ROLLBACK);
                            helper.runAfterDelay(AnniTrollEntity.ROLLBACK_MARK + 2, () -> {
                                Vec3 marked = knight.position();
                                int marks = level.getEntitiesOfClass(AfterimageEntity.class, area).size();
                                helper.assertTrue(marks >= 1, "Kein Abbild beim Merken");
                                knight.teleportTo(knight.getX() + 4.0, knight.getY(), knight.getZ());
                                helper.runAfterDelay(AnniTrollEntity.ROLLBACK_SNAP - AnniTrollEntity.ROLLBACK_MARK, () -> {
                                    double off = knight.position().distanceTo(marked);
                                    helper.assertTrue(off < 1.0, "Rückruck: " + String.format("%.1f", off) + " Blöcke daneben");
                                    field.close();
                                    helper.succeed();
                                });
                            });
                        });
                    });
                });
            });
        });
    }

    /** Speedhack (nur Anni 2.0): Er zielt, rast los, trifft, wen er erwischt, und hinterlässt fehlende Textur. */
    private static void speedhack(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper);
        Field field = field(helper, 2, 20);
        BlockPos pos = field.origin();
        AABB area = field.area();
        AnniTrollEntity anni = anni(level, pos.offset(0, 0, -8));
        LivingEntity knight = dummy(level, pos.offset(0, 0, 4));
        anni.forceVersion2();
        anni.setTarget(knight);
        anni.forceAttack(AnniTrollEntity.Attack.SPEEDHACK);
        helper.runAfterDelay(AnniTrollEntity.HACK_FIRST + 2, () -> {
            helper.assertTrue(anni.isLagging(), "Beim Zielen hängt er nicht");
            Vec3 start = anni.position();
            helper.runAfterDelay(AnniTrollEntity.HACK_AIM + AnniTrollEntity.HACK_DASH, () -> {
                double moved = anni.position().distanceTo(start);
                helper.assertTrue(moved > 6.0, "Er rast nicht: " + String.format("%.1f", moved));
                int trail = level.getEntitiesOfClass(MissingZoneEntity.class, area).size();
                helper.assertTrue(trail >= 1, "Keine Spur fehlender Textur");
                helper.assertTrue(knight.getHealth() < knight.getMaxHealth(), "Er trifft niemanden");
                field.close();
                helper.succeed();
            });
        });
    }

    /**
     * Wiederherstellung: Auch ohne Kern lädt er beim ersten tödlichen Treffer seine Sicherung (zerfallen ist er unsichtbar
     * und unverwundbar); /kill dagegen beendet ihn sofort.
     */
    private static void revive(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper);
        Field field = field(helper, 3, 9);
        BlockPos pos = field.origin();
        AnniTrollEntity killed = anni(level, pos.offset(-5, 0, 0));
        killed.kill(level);
        helper.assertTrue(killed.isDeadOrDying() && !killed.isRevived(), "/kill lässt ihn eine Sicherung laden");
        AnniTrollEntity anni = anni(level, pos.offset(4, 0, 0));
        double designed = anni.designedMaxHealth();
        anni.hurtServer(level, level.damageSources().generic(), 1.0e6f);
        helper.assertFalse(anni.isDeadOrDying(), "Der tödliche Treffer tötet ihn");
        helper.assertTrue(anni.currentAttack() == AnniTrollEntity.Attack.REVIVE, "Keine Wiederherstellung: " + anni.currentAttack());
        helper.runAfterDelay(AnniTrollEntity.REVIVE_SHATTER + 4, () -> {
            helper.assertTrue(anni.isHidden(), "Zerfallen ist er noch zu sehen");
            float hp = anni.getHealth();
            anni.setInvulnerableTime(0);
            anni.hurtServer(level, level.damageSources().generic(), 1.0e6f);
            helper.assertTrue(anni.getHealth() >= hp && !anni.isDeadOrDying(), "Während der Wiederherstellung verwundbar");
            helper.runAfterDelay(AnniTrollEntity.REVIVE_FILL + 10 - AnniTrollEntity.REVIVE_SHATTER - 4, () -> {
                helper.assertTrue(anni.getHealth() > 1.0f, "Die Leiste füllt sich nicht");
                helper.runAfterDelay(AnniTrollEntity.Attack.REVIVE.duration - AnniTrollEntity.REVIVE_FILL - 10 + 2, () -> {
                    helper.assertTrue(anni.isVersion2() && anni.isGlitchMode(), "Kein Anni 2.0");
                    double expected = designed * AnniTrollEntity.BACKUP_HEALTH;
                    helper.assertTrue(Math.abs(anni.designedMaxHealth() - expected) < 1.0, "Lebenskraft: " + anni.designedMaxHealth()
                            + " statt " + expected);
                    helper.assertTrue(anni.getHealth() >= anni.getMaxHealth() - 0.5f, "Die Leiste ist nicht voll");
                    anni.setInvulnerableTime(0);
                    anni.hurtServer(level, level.damageSources().generic(), 1.0e6f);
                    helper.assertTrue(anni.isDeadOrDying(), "Ein zweites Mal lädt er eine Sicherung");
                    field.close();
                    helper.succeed();
                });
            });
        });
    }

    // =====================================================================================
    // Beute
    // =====================================================================================

    /**
     * Bannhammer (schwere, epische Waffe; jeder dritte Treffer ein Ruckler, jeder fünfte ein Bann, der auch Umstehende trifft)
     * und Fehlerflügel (gleiten, Brustplatz).
     */
    private static void gear(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper);
        ItemStack hammer = new ItemStack(AnniBlocks.BANNHAMMER.get());
        helper.assertTrue(hammer.has(DataComponents.WEAPON), "Der Bannhammer ist keine Waffe");
        helper.assertTrue(hammer.getMaxDamage() == 2600, "Haltbarkeit des Bannhammers: " + hammer.getMaxDamage());
        helper.assertTrue(hammer.get(DataComponents.RARITY) == Rarity.EPIC, "Der Bannhammer ist nicht episch");
        helper.assertTrue(hammer.is(ItemTags.SHARP_WEAPON_ENCHANTABLE) && hammer.is(ItemTags.DURABILITY_ENCHANTABLE), "Bannhammer: keine Verzauberungen");
        ItemStack wings = new ItemStack(AnniBlocks.GLITCH_WINGS.get());
        helper.assertTrue(wings.has(DataComponents.GLIDER), "Die Fehlerflügel gleiten nicht");
        var equippable = wings.get(DataComponents.EQUIPPABLE);
        helper.assertTrue(equippable != null && equippable.slot() == EquipmentSlot.CHEST, "Die Fehlerflügel sitzen nicht auf der Brust");
        helper.assertTrue(new ItemStack(AnniBlocks.GLITCH_SHARD.get()).is(io.github.sirschrolli.kingdomsatwar.registry.ModTags.Items.REPAIRS_GLITCH_GEAR),
                "Fehlersplitter reparieren nichts");

        ServerPlayer player = MageGameTests.survivalPlayer(helper, "test-anni-gear", new io.netty.channel.embedded.EmbeddedChannel[1], 0);
        BlockPos at = helper.absolutePos(new BlockPos(4, 1, 4));
        move(player, at.getX() + 0.5, at.getY(), at.getZ() - 1.5);
        player.setItemInHand(InteractionHand.MAIN_HAND, hammer);
        player.setItemSlot(EquipmentSlot.CHEST, wings);
        helper.assertTrue(GlitchWings.wearing(player), "Die Fehlerflügel zählen nicht als angelegt");
        LivingEntity target = dummy(level, at);
        LivingEntity bystander = dummy(level, at.offset(2, 0, 0));
        for (int i = 0; i < 3; i++) {
            hammer.getItem().postHurtEnemy(hammer, target, player);
        }
        helper.assertTrue(target.hasEffect(MobEffects.SLOWNESS), "Der dritte Treffer ruckelt nicht");
        float before = bystander.getHealth();
        hammer.getItem().postHurtEnemy(hammer, target, player);
        hammer.getItem().postHurtEnemy(hammer, target, player);
        helper.assertTrue(bystander.getHealth() < before, "Der fünfte Treffer bannt die Umstehenden nicht");
        level.getServer().getPlayerList().remove(player);
        target.discard();
        bystander.discard();
        helper.succeed();
    }
}
