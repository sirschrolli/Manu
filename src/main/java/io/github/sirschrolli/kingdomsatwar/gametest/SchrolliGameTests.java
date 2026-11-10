package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.creator.CreatorAltarBlock;
import io.github.sirschrolli.kingdomsatwar.block.creator.CreatorAltarBlockEntity;
import io.github.sirschrolli.kingdomsatwar.creator.Godslayer;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossHealth;
import io.github.sirschrolli.kingdomsatwar.entity.creator.DivineShotEntity;
import io.github.sirschrolli.kingdomsatwar.entity.creator.SchrolliEntity;
import io.github.sirschrolli.kingdomsatwar.entity.creator.SchrolliEntity.Attack;
import io.github.sirschrolli.kingdomsatwar.entity.creator.SeraphlingEntity;
import io.github.sirschrolli.kingdomsatwar.entity.creator.TimeFreeze;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.magic.fx.SpellFxEntity;
import io.github.sirschrolli.kingdomsatwar.registry.CreatorBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Schrolli der Schöpfer (0.75): Lebenskraft und Erscheinen, der Aufstieg in die Wahre Gestalt statt des Todes, seine
 * Künste (jede wird erzwungen und muss ihre Wirkung zeigen), die Künste der Wahren Gestalt, der Altar mit dem Funken und die
 * Beute samt Titel.
 */
final class SchrolliGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CORE =
            ModGameTests.TEST_FUNCTIONS.register("schrolli_core", () -> SchrolliGameTests::core);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ARTS =
            ModGameTests.TEST_FUNCTIONS.register("schrolli_arts", () -> SchrolliGameTests::arts);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TRUE_ARTS =
            ModGameTests.TEST_FUNCTIONS.register("schrolli_true_arts", () -> SchrolliGameTests::trueArts);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ALTAR =
            ModGameTests.TEST_FUNCTIONS.register("schrolli_altar", () -> SchrolliGameTests::altar);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> WINGS =
            ModGameTests.TEST_FUNCTIONS.register("schrolli_wings", () -> SchrolliGameTests::wings);

    private SchrolliGameTests() {}

    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var test : List.of(CORE, ARTS, TRUE_ARTS, ALTAR, WINGS)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 2400, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
    }

    // =====================================================================================
    // Hilfen
    // =====================================================================================

    /** Ein eigenes Feld hoch über der Welt: eine breite Plattform mit der Mitte bei {@code origin}. */
    private record Field(ServerLevel level, BlockPos origin, int half, List<long[]> chunks) {
        AABB area() {
            return new AABB(this.origin).inflate(80, 64, 80);
        }

        void close() {
            for (Entity e : this.level.getEntitiesOfClass(Entity.class, this.area(), e -> !(e instanceof net.minecraft.world.entity.player.Player))) {
                if (e instanceof LivingEntity living) {
                    TimeFreeze.unfreeze(living);
                }
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
        BlockPos origin = new BlockPos(base.getX() - 24_000 + slot * 500, 230, base.getZ() + 18_000);
        List<long[]> chunks = new ArrayList<>();
        int reach = 64;
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

    /** Ein zäher Zuschauer (ein Ritter ohne eigenen Willen, 3000 Leben), auf den der Schöpfer losgeht. */
    private static LivingEntity dummy(ServerLevel level, BlockPos at) {
        LivingEntity knight = ModEntities.SOLDIERS.get(SoldierType.KNIGHT).get().create(level, EntitySpawnReason.EVENT);
        if (knight == null) {
            throw new IllegalStateException("kein Ritter");
        }
        knight.snapTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, 180.0f, 0.0f);
        ((Mob) knight).setNoAi(true);
        knight.getAttribute(Attributes.MAX_HEALTH).setBaseValue(3000.0);
        knight.setHealth(3000.0f);
        level.addFreshEntity(knight);
        return knight;
    }

    private static SchrolliEntity schrolli(ServerLevel level, BlockPos at) {
        SchrolliEntity boss = ModEntities.SCHROLLI.get().create(level, EntitySpawnReason.EVENT);
        if (boss == null) {
            throw new IllegalStateException("kein Schrolli");
        }
        boss.snapTo(at.getX() + 0.5, at.getY() + 8.0, at.getZ() + 0.5, 0.0f, 0.0f);
        boss.bindTo(null, Vec3.atBottomCenterOf(at));
        level.addFreshEntity(boss);
        return boss;
    }

    private static int shots(ServerLevel level, AABB area, DivineShotEntity.Kind kind) {
        return level.getEntitiesOfClass(DivineShotEntity.class, area, s -> s.kind() == kind && !s.isRemoved()).size();
    }

    private static int fx(ServerLevel level, AABB area, SpellFxEntity.Kind kind) {
        return level.getEntitiesOfClass(SpellFxEntity.class, area, s -> s.kind() == kind).size();
    }

    // =====================================================================================
    // Lebenskraft, Erscheinen, Aufstieg, Tod
    // =====================================================================================

    /**
     * Mehr Lebenskraft als jeder andere Gegner (3.200 je Gestalt, mit jedem weiteren Spieler ein Viertel mehr); beim Erscheinen
     * unverwundbar; der erste tödliche Treffer tötet ihn nicht – er steigt in die Wahre Gestalt auf (unverwundbar, größer,
     * neue volle Leiste); der zweite besiegt ihn. /kill beendet ihn sofort.
     */
    private static void core(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Field field = field(helper, 0, 12);
        BlockPos pos = field.origin();
        SchrolliEntity killed = schrolli(level, pos.offset(-20, 0, 0));
        killed.kill(level);
        helper.assertTrue(killed.isDeadOrDying() && !killed.isTrueForm(), "/kill lässt ihn aufsteigen");
        SchrolliEntity boss = schrolli(level, pos);
        double designed = BossHealth.designed(boss);
        helper.assertTrue(Math.abs(designed - SchrolliEntity.DESIGNED_HEALTH) < 1.0, "Lebenskraft: " + designed);
        helper.assertTrue(designed > 2600.0, "Nicht stärker als der Weltenfresser");
        boss.scaleForChallengers(3);
        helper.assertTrue(Math.abs(BossHealth.designed(boss) - SchrolliEntity.DESIGNED_HEALTH * 1.5) < 1.0, "Drei Spieler: " + BossHealth.designed(boss));
        boss.scaleForChallengers(1);
        boss.awaken();
        float before = boss.getHealth();
        boss.hurtServer(level, level.damageSources().generic(), 50.0f);
        helper.assertTrue(boss.getHealth() >= before, "Beim Erscheinen verwundbar");
        helper.runAfterDelay(SchrolliEntity.AWAKEN_TICKS + 4, () -> {
            helper.assertTrue(boss.currentAttack() != Attack.AWAKEN, "Er erscheint nicht zu Ende");
            // kein Treffer zählt mehr als ein Zwanzigstel seiner Kraft
            float full = boss.getHealth();
            boss.damageCooldownTime = 0;
            boss.hurtServer(level, level.damageSources().generic(), 1.0e6f);
            helper.assertTrue(full - boss.getHealth() < boss.getMaxHealth() * 0.06f && !boss.isDeadOrDying(),
                    "Ein Schlag fällt ihn: " + full + " -> " + boss.getHealth());
            boss.setHealth(5.0f);
            boss.damageCooldownTime = 0;
            boss.hurtServer(level, level.damageSources().generic(), 1.0e6f);
            helper.assertFalse(boss.isDeadOrDying(), "Der tödliche Treffer tötet ihn");
            helper.assertTrue(boss.currentAttack() == Attack.ASCEND, "Kein Aufstieg: " + boss.currentAttack());
            helper.runAfterDelay(SchrolliEntity.ASCEND_SHATTER + 4, () -> {
                float hp = boss.getHealth();
                boss.damageCooldownTime = 0;
                boss.hurtServer(level, level.damageSources().generic(), 1.0e6f);
                helper.assertTrue(boss.getHealth() >= hp && !boss.isDeadOrDying(), "Im Aufstieg verwundbar");
                helper.runAfterDelay(SchrolliEntity.ASCEND_TICKS - SchrolliEntity.ASCEND_SHATTER, () -> {
                    helper.assertTrue(boss.isTrueForm(), "Keine Wahre Gestalt");
                    helper.assertTrue(boss.getScale() > 1.2f, "Die Wahre Gestalt ist nicht größer: " + boss.getScale());
                    helper.assertTrue(boss.getHealth() >= boss.getMaxHealth() - 0.5f, "Die neue Leiste ist nicht voll");
                    boss.setHealth(5.0f);
                    boss.damageCooldownTime = 0;
                    boss.hurtServer(level, level.damageSources().generic(), 1.0e6f);
                    helper.assertTrue(boss.isDeadOrDying(), "Ein zweites Mal steigt er auf");
                    field.close();
                    helper.succeed();
                });
            });
        });
    }

    // =====================================================================================
    // Die Künste der ersten Gestalt
    // =====================================================================================

    /** Jede Kunst wird erzwungen und zeigt ihre Wirkung (Geschosse, Warnkreise, Strahlen, Starre, Schaden). */
    private static void arts(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Field field = field(helper, 1, 24);
        BlockPos pos = field.origin();
        AABB area = field.area();
        SchrolliEntity boss = schrolli(level, pos);
        LivingEntity knight = dummy(level, pos.offset(0, 0, 8));
        List<String> problems = new ArrayList<>();
        boss.setTarget(knight);
        boss.forceAttack(Attack.LANCES);
        helper.runAfterDelay(12, () -> {
            if (shots(level, area, DivineShotEntity.Kind.SPEAR) < 7) {
                problems.add("Lichtspeere: " + shots(level, area, DivineShotEntity.Kind.SPEAR));
            }
            boss.setTarget(knight);
            boss.forceAttack(Attack.PILLARS);
            float hp = knight.getHealth();
            helper.runAfterDelay(8, () -> {
                if (fx(level, area, SpellFxEntity.Kind.CIRCLE) < 3) {
                    problems.add("Säulen: keine Warnkreise");
                }
                helper.runAfterDelay(30, () -> {
                    if (knight.getHealth() >= hp) {
                        problems.add("Säulen: der Ritter blieb unversehrt");
                    }
                    boss.setTarget(knight);
                    boss.forceAttack(Attack.HALO);
                    helper.runAfterDelay(18, () -> {
                        if (!boss.hasFlag(SchrolliEntity.FLAG_HALO) || shots(level, area, DivineShotEntity.Kind.HALO) < 1) {
                            problems.add("Heiligenschein: nicht geworfen");
                        }
                        boss.setTarget(knight);
                        boss.forceAttack(Attack.SERAPHS);
                        helper.runAfterDelay(32, () -> {
                            int seraphs = level.getEntitiesOfClass(SeraphlingEntity.class, area).size();
                            if (seraphs < 3) {
                                problems.add("Seraphim: " + seraphs);
                            }
                            boss.setTarget(knight);
                            boss.forceAttack(Attack.TIMESTOP);
                            helper.runAfterDelay(SchrolliEntity.TIME_FREEZE + 3, () -> {
                                if (!TimeFreeze.isFrozen(knight)) {
                                    problems.add("Die Zeit steht nicht still");
                                }
                                helper.runAfterDelay(SchrolliEntity.TIME_RESUME - SchrolliEntity.TIME_FREEZE + 2, () -> {
                                    if (TimeFreeze.isFrozen(knight)) {
                                        problems.add("Die Zeit läuft nicht weiter");
                                    }
                                    boss.setTarget(knight);
                                    boss.forceAttack(Attack.SUNWHEEL);
                                    helper.runAfterDelay(34, () -> {
                                        if (fx(level, area, SpellFxEntity.Kind.BEAM) < 6) {
                                            problems.add("Sonnenrad: " + fx(level, area, SpellFxEntity.Kind.BEAM) + " Strahlen");
                                        }
                                        boss.setTarget(knight);
                                        boss.forceAttack(Attack.JUDGMENT);
                                        float before = knight.getHealth();
                                        helper.runAfterDelay(72, () -> {
                                            if (knight.getHealth() >= before) {
                                                problems.add("Urteil: kein Schaden");
                                            }
                                            boss.setTarget(knight);
                                            boss.forceAttack(Attack.ORB);
                                            helper.runAfterDelay(20, () -> {
                                                if (!boss.hasFlag(SchrolliEntity.FLAG_ORB) || shots(level, area, DivineShotEntity.Kind.ORB) < 1) {
                                                    problems.add("Weltkugel: nicht geworfen");
                                                }
                                                boss.setTarget(knight);
                                                boss.forceAttack(Attack.FEATHERS);
                                                helper.runAfterDelay(20, () -> {
                                                    if (shots(level, area, DivineShotEntity.Kind.FEATHER) < 10) {
                                                        problems.add("Federsturm: " + shots(level, area, DivineShotEntity.Kind.FEATHER));
                                                    }
                                                    field.close();
                                                    helper.assertTrue(problems.isEmpty(), "Künste: " + problems);
                                                    helper.succeed();
                                                });
                                            });
                                        });
                                    });
                                });
                            });
                        });
                    });
                });
            });
        });
    }

    // =====================================================================================
    // Die Künste der Wahren Gestalt
    // =====================================================================================

    /** Weltenwurf, Sternbild, Supernova (mit und ohne Deckung), Weltenriss und die Neuschöpfung (heilt – bis man sie unterbricht). */
    private static void trueArts(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Field field = field(helper, 2, 30);
        BlockPos pos = field.origin();
        AABB area = field.area();
        SchrolliEntity boss = schrolli(level, pos);
        boss.forceTrueForm();
        LivingEntity knight = dummy(level, pos.offset(6, 0, 6));
        LivingEntity hidden = dummy(level, pos.offset(26, 0, 0));
        // eine Mauer zwischen der Sonne und dem versteckten Ritter
        for (int y = 0; y < 30; y++) {
            for (int z = -3; z <= 3; z++) {
                level.setBlock(pos.offset(24, y, z), Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
        }
        List<String> problems = new ArrayList<>();
        helper.assertTrue(boss.isTrueForm(), "Keine Wahre Gestalt");
        boss.setTarget(knight);
        boss.forceAttack(Attack.PLANETS);
        helper.runAfterDelay(34, () -> {
            if (shots(level, area, DivineShotEntity.Kind.PLANET) < 3) {
                problems.add("Weltenwurf: " + shots(level, area, DivineShotEntity.Kind.PLANET) + " Welten");
            }
            boss.setTarget(knight);
            boss.forceAttack(Attack.CONSTELLATION);
            helper.runAfterDelay(12, () -> {
                if (shots(level, area, DivineShotEntity.Kind.FIXED_STAR) < 9) {
                    problems.add("Sternbild: " + shots(level, area, DivineShotEntity.Kind.FIXED_STAR) + " Sterne");
                }
                boss.setTarget(knight);
                float open = knight.getHealth();
                float covered = hidden.getHealth();
                boss.forceAttack(Attack.SUPERNOVA);
                helper.runAfterDelay(SchrolliEntity.NOVA_BLAST + 3, () -> {
                    if (knight.getHealth() >= open) {
                        problems.add("Supernova: der offene Ritter blieb unversehrt");
                    }
                    if (hidden.getHealth() < covered) {
                        problems.add("Supernova: die Mauer schützt nicht");
                    }
                    boss.setTarget(knight);
                    boss.forceAttack(Attack.WORLD_SPLIT);
                    helper.runAfterDelay(26, () -> {
                        if (fx(level, area, SpellFxEntity.Kind.FISSURE) < 6) {
                            problems.add("Weltenriss: " + fx(level, area, SpellFxEntity.Kind.FISSURE) + " Risse");
                        }
                        // Neuschöpfung: er heilt sich …
                        boss.setHealth(boss.getMaxHealth() * 0.2f);
                        float low = boss.getHealth();
                        boss.forceAttack(Attack.RECREATION);
                        helper.runAfterDelay(60, () -> {
                            if (boss.getHealth() <= low) {
                                problems.add("Neuschöpfung: er heilt nicht");
                            }
                            // … bis genug Schaden ihn unterbricht
                            for (int k = 0; k < 3; k++) {
                                boss.damageCooldownTime = 0;
                                boss.hurtServer(level, level.damageSources().generic(), 60.0f);
                            }
                            helper.runAfterDelay(2, () -> {
                                if (boss.currentAttack() != Attack.STUNNED) {
                                    problems.add("Neuschöpfung: nicht unterbrochen (" + boss.currentAttack() + ")");
                                }
                                field.close();
                                for (int y = 0; y < 30; y++) {
                                    for (int z = -3; z <= 3; z++) {
                                        level.setBlock(pos.offset(24, y, z), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                                    }
                                }
                                helper.assertTrue(problems.isEmpty(), "Künste der Wahren Gestalt: " + problems);
                                helper.succeed();
                            });
                        });
                    });
                });
            });
        });
    }

    // =====================================================================================
    // Altar, Funke, Beute, Titel
    // =====================================================================================

    /**
     * Der Altar: ohne Funken ruft er nicht; mit Funken ruft eine Berührung den Schöpfer (hier ohne Kampfdimension: am Altar).
     * Fällt er (zweimal), ist der Funke verglüht, die Beute liegt vor dem Altar (Zepter und Schwingen), und wer dabei war,
     * ist Gottbezwinger.
     */
    private static void altar(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Field field = field(helper, 3, 20);
        BlockPos pos = field.origin();
        BlockPos altarPos = pos.offset(0, 0, -6);
        level.setBlock(altarPos, CreatorBlocks.CREATOR_ALTAR.get().defaultBlockState().setValue(CreatorAltarBlock.FACING, Direction.SOUTH),
                Block.UPDATE_ALL);
        ServerPlayer player = MageGameTests.survivalPlayer(helper, "test-godslayer", new io.netty.channel.embedded.EmbeddedChannel[1], 0);
        player.teleportTo(level, pos.getX() + 3.5, pos.getY(), pos.getZ() + 0.5, java.util.Set.of(), 0.0f, 0.0f, true);
        level.getChunkSource().move(player);
        if (!(level.getBlockEntity(altarPos) instanceof CreatorAltarBlockEntity altar)) {
            field.close();
            level.getServer().getPlayerList().remove(player);
            throw new IllegalStateException("Kein Altar");
        }
        altar.touch(level, player);
        helper.assertTrue(altar.state() == CreatorAltarBlockEntity.State.EMPTY, "Ohne Funken ruft der Altar");
        helper.assertTrue(altar.insertSpark(level, player), "Der Funke passt nicht in den Altar");
        helper.assertTrue(level.getBlockState(altarPos).getValue(CreatorAltarBlock.PHASE) == CreatorAltarBlock.Phase.ARMED, "Der Altar leuchtet nicht");
        altar.touch(level, player);
        helper.assertTrue(altar.state() == CreatorAltarBlockEntity.State.CHARGING, "Die Berührung ruft nicht: " + altar.state());
        helper.runAfterDelay(CreatorAltarBlockEntity.CHARGE_TICKS + 4, () -> {
            try {
                helper.assertTrue(altar.state() == CreatorAltarBlockEntity.State.AWAKE, "Der Schöpfer kommt nicht: " + altar.state());
                SchrolliEntity boss = level.getEntitiesOfClass(SchrolliEntity.class, field.area()).stream().findFirst().orElse(null);
                helper.assertTrue(boss != null && altar.owns(boss), "Kein Schöpfer am Altar");
                // zweimal gefällt: Aufstieg, dann Sieg
                helper.assertTrue(boss.currentAttack() == Attack.AWAKEN, "Er erscheint nicht: " + boss.currentAttack());
                boss.forceAttack(Attack.NONE);
                boss.forceTrueForm();
                boss.setHealth(5.0f);
                boss.damageCooldownTime = 0;
                boss.hurtServer(level, player.damageSources().playerAttack(player), 1.0e6f);
                helper.assertTrue(boss.isDeadOrDying(), "Er fällt nicht");
                helper.assertTrue(altar.state() == CreatorAltarBlockEntity.State.EMPTY, "Der Funke verglüht nicht: " + altar.state());
                helper.runAfterDelay(SchrolliEntity.DEATH_TICKS + 4, () -> {
                    try {
                        List<ItemStack> drops = level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, field.area()).stream()
                                .map(net.minecraft.world.entity.item.ItemEntity::getItem).toList();
                        helper.assertTrue(drops.stream().anyMatch(s -> s.is(CreatorBlocks.CREATOR_SCEPTER.get())), "Kein Zepter in der Beute");
                        helper.assertTrue(drops.stream().anyMatch(s -> s.is(CreatorBlocks.CREATOR_WINGS.get())), "Keine Schwingen in der Beute");
                        helper.assertTrue(Godslayer.is(player), "Wer dabei war, wird nicht Gottbezwinger");
                        helper.assertTrue(player.getDisplayName().toString().contains("title.kingdomsatwar.godslayer.prefix"),
                                "Kein Titel vor dem Namen: " + player.getDisplayName());
                        helper.succeed();
                    } finally {
                        field.close();
                        level.setBlock(altarPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                        level.getServer().getPlayerList().remove(player);
                    }
                });
            } catch (RuntimeException e) {
                field.close();
                level.setBlock(altarPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                level.getServer().getPlayerList().remove(player);
                throw e;
            }
        });
    }

    // =====================================================================================
    // Die Schwingen des Schöpfers
    // =====================================================================================

    /** Die Schwingen fangen Fall und Aufprall ab; der Schwingenschlag zündet nur im Gleitflug und hat eine Abklingzeit. */
    private static void wings(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = MageGameTests.survivalPlayer(helper, "test-creator-wings", new io.netty.channel.embedded.EmbeddedChannel[1], 0);
        try {
            player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST, new ItemStack(CreatorBlocks.CREATOR_WINGS.get()));
            helper.assertTrue(io.github.sirschrolli.kingdomsatwar.creator.CreatorWings.wearing(player), "Die Schwingen sitzen nicht");
            float hp = player.getHealth();
            player.hurtServer(level, level.damageSources().fall(), 8.0f);
            player.damageCooldownTime = 0;
            player.hurtServer(level, level.damageSources().flyIntoWall(), 8.0f);
            helper.assertTrue(player.getHealth() >= hp, "Fall oder Aufprall verletzen trotz der Schwingen");
            helper.assertFalse(io.github.sirschrolli.kingdomsatwar.creator.CreatorWings.boost(player), "Schwingenschlag ohne Gleitflug");
            player.startFallFlying();
            helper.assertTrue(io.github.sirschrolli.kingdomsatwar.creator.CreatorWings.boost(player), "Kein Schwingenschlag im Gleitflug");
            helper.assertTrue(player.getCooldowns().isOnCooldown(player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST)),
                    "Keine Abklingzeit nach dem Schwingenschlag");
            helper.assertFalse(io.github.sirschrolli.kingdomsatwar.creator.CreatorWings.boost(player), "Zwei Schläge ohne Pause");
            player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST, ItemStack.EMPTY);
            player.damageCooldownTime = 0;
            player.hurtServer(level, level.damageSources().fall(), 4.0f);
            helper.assertTrue(player.getHealth() < hp, "Ohne Schwingen schützt nichts vor dem Fall");
            helper.succeed();
        } finally {
            level.getServer().getPlayerList().remove(player);
        }
    }
}
