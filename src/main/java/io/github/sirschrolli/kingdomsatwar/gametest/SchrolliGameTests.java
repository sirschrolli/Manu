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
 * Beute samt Titel. Seit 0.76 der Nahkampf am Boden: Landung, die Künste der Klinge, die Schwingenwehr samt Gegenschlag
 * und dass er von selbst landet und sich wieder emporschwingt.
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
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> MELEE =
            ModGameTests.TEST_FUNCTIONS.register("schrolli_melee", () -> SchrolliGameTests::melee);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> GUARD =
            ModGameTests.TEST_FUNCTIONS.register("schrolli_guard", () -> SchrolliGameTests::guard);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> STANCE =
            ModGameTests.TEST_FUNCTIONS.register("schrolli_stance", () -> SchrolliGameTests::stance);

    private SchrolliGameTests() {}

    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var test : List.of(CORE, ARTS, TRUE_ARTS, ALTAR, WINGS, MELEE, GUARD, STANCE)) {
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
        player.connection.markClientLoaded();   // sonst ist er unverwundbar wie ein Spieler, der gerade erst die Welt lädt
        player.getAbilities().invulnerable = false;   // die Testwelt läuft im Kreativmodus
        player.getAbilities().instabuild = false;
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

    // =====================================================================================
    // Am Boden: die Klinge aus Licht (seit 0.76)
    // =====================================================================================

    /** Stellt den Ritter {@code distance} Blöcke vor den Schöpfer (mit voller Lebenskraft) und gibt seine Lebenskraft zurück. */
    private static float before(SchrolliEntity boss, LivingEntity knight, double distance) {
        Vec3 at = boss.position().add(boss.getForward().multiply(1.0, 0.0, 1.0).normalize().scale(distance));
        knight.teleportTo(at.x, boss.getY(), at.z);
        knight.setDeltaMovement(Vec3.ZERO);
        knight.setHealth(knight.getMaxHealth());
        knight.damageCooldownTime = 0;
        boss.setTarget(knight);
        return knight.getHealth();
    }

    /**
     * Landung (er steht danach am Boden, Beine und Klinge geformt), Klingenhieb (trifft, zuletzt der Spaltschlag mit seinem
     * Riss), Lichtsicheln (gleiten und treffen), Klingenwirbel, Sturmlauf (er stürmt weit), Himmelssturz (er schlägt am Boden
     * ein) und in der Wahren Gestalt die Weltenklinge.
     */
    private static void melee(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Field field = field(helper, 5, 34);
        BlockPos pos = field.origin();
        AABB area = field.area();
        SchrolliEntity boss = schrolli(level, pos);
        LivingEntity knight = dummy(level, pos.offset(0, 0, 12));
        List<String> problems = new ArrayList<>();
        boss.setTarget(knight);
        boss.forceAttack(Attack.DESCEND);
        helper.assertFalse(boss.isGrounded(), "Vor der Landung schon am Boden");
        helper.runAfterDelay(SchrolliEntity.DESCEND_LAND + 3, () -> {
            if (!boss.isGrounded() || !boss.hasFlag(SchrolliEntity.FLAG_GROUND)) {
                problems.add("Landung: nicht am Boden");
            }
            if (Math.abs(boss.getY() - pos.getY()) > 0.6) {
                problems.add("Landung: " + String.format("%.1f", boss.getY() - pos.getY()) + " über dem Boden");
            }
            float hp = before(boss, knight, 5.0);
            boss.forceAttack(Attack.STRIKE);
            helper.runAfterDelay(64, () -> {
                if (knight.getHealth() >= hp) {
                    problems.add("Klingenhieb: der Ritter blieb unversehrt");
                }
                if (fx(level, area, SpellFxEntity.Kind.FISSURE) < 1) {
                    problems.add("Klingenhieb: kein Spaltschlag");
                }
                if (!boss.isGrounded()) {
                    problems.add("Klingenhieb: nicht mehr am Boden");
                }
                float hp2 = before(boss, knight, 13.0);
                boss.forceAttack(Attack.SLASHES);
                helper.runAfterDelay(22, () -> {
                    if (fx(level, area, SpellFxEntity.Kind.CRESCENT) < 1) {
                        problems.add("Lichtsicheln: keine Sichel");
                    }
                    helper.runAfterDelay(30, () -> {
                        if (knight.getHealth() >= hp2) {
                            problems.add("Lichtsicheln: kein Treffer");
                        }
                        float hp3 = before(boss, knight, 5.0);
                        boss.forceAttack(Attack.WHIRL);
                        helper.runAfterDelay(50, () -> {
                            if (knight.getHealth() >= hp3) {
                                problems.add("Klingenwirbel: kein Treffer");
                            }
                            float hp4 = before(boss, knight, 12.0);
                            Vec3 start = boss.position();
                            boss.forceAttack(Attack.DASH);
                            helper.runAfterDelay(34, () -> {
                                double run = DivineFxProbe.flat(start, boss.position());
                                if (run < 10.0) {
                                    problems.add("Sturmlauf: nur " + String.format("%.1f", run) + " Blöcke");
                                }
                                if (knight.getHealth() >= hp4) {
                                    problems.add("Sturmlauf: kein Treffer");
                                }
                                float hp5 = before(boss, knight, 8.0);
                                boss.forceAttack(Attack.PLUNGE);
                                helper.runAfterDelay(SchrolliEntity.PLUNGE_IMPACT + 2, () -> {
                                    if (Math.abs(boss.getY() - pos.getY()) > 0.8) {
                                        problems.add("Himmelssturz: nicht am Boden eingeschlagen (" + String.format("%.1f", boss.getY() - pos.getY()) + ")");
                                    }
                                    if (knight.getHealth() >= hp5) {
                                        problems.add("Himmelssturz: kein Treffer");
                                    }
                                    boss.forceTrueForm();
                                    float hp6 = before(boss, knight, 15.0);
                                    boss.forceAttack(Attack.WORLD_BLADE);
                                    helper.runAfterDelay(64, () -> {
                                        if (knight.getHealth() >= hp6) {
                                            problems.add("Weltenklinge: der Rundschlag trifft nicht");
                                        }
                                        boss.forceAttack(Attack.RISE);
                                        helper.runAfterDelay(Attack.RISE.duration + 2, () -> {
                                            if (boss.isGrounded() || boss.hasFlag(SchrolliEntity.FLAG_GROUND)) {
                                                problems.add("Aufschwung: noch am Boden");
                                            }
                                            if (boss.getY() - pos.getY() < 3.0) {
                                                problems.add("Aufschwung: nur " + String.format("%.1f", boss.getY() - pos.getY()) + " hoch");
                                            }
                                            field.close();
                                            helper.assertTrue(problems.isEmpty(), "Nahkampf: " + problems);
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
    }

    /**
     * Schwingenwehr: ein Hieb von vorn wird pariert (kein Schaden) und mit dem Gegenschlag beantwortet, der den Angreifer
     * trifft; von hinten trifft man ihn.
     */
    private static void guard(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Field field = field(helper, 6, 16);
        BlockPos pos = field.origin();
        SchrolliEntity boss = schrolli(level, pos);
        ServerPlayer player = MageGameTests.survivalPlayer(helper, "test-creator-guard", new io.netty.channel.embedded.EmbeddedChannel[1], 0);
        player.connection.markClientLoaded();
        player.getAbilities().invulnerable = false;
        player.getAbilities().instabuild = false;
        Runnable done = () -> {
            level.getServer().getPlayerList().remove(player);
            field.close();
        };
        try {
            boss.forceAttack(Attack.GUARD);
            boss.setYRot(0.0f);
            boss.setYBodyRot(0.0f);
            boss.setYHeadRot(0.0f);
            player.teleportTo(level, boss.getX(), boss.getY(), boss.getZ() + 4.5, java.util.Set.of(), 180.0f, 0.0f, true);
            boss.setTarget(player);
        } catch (Throwable e) {
            done.run();
            throw e;
        }
        helper.runAfterDelay(10, () -> {
            float php;
            try {
                float hp = boss.getHealth();
                boss.damageCooldownTime = 0;
                boolean hurt = boss.hurtServer(level, player.damageSources().playerAttack(player), 20.0f);
                helper.assertFalse(hurt, "Schwingenwehr: der Hieb von vorn trifft");
                helper.assertTrue(boss.getHealth() >= hp, "Schwingenwehr: er verliert Lebenskraft");
                helper.assertTrue(boss.currentAttack() == Attack.COUNTER, "Kein Gegenschlag: " + boss.currentAttack());
                php = player.getHealth();
            } catch (Throwable e) {
                done.run();
                throw e;
            }
            helper.runAfterDelay(14, () -> {
                try {
                    helper.assertTrue(player.getHealth() < php, "Der Gegenschlag trifft nicht (" + php + " -> " + player.getHealth() + ")");
                    player.setHealth(player.getMaxHealth());
                    boss.forceAttack(Attack.GUARD);
                    boss.setYRot(0.0f);
                    boss.setYBodyRot(0.0f);
                    boss.setYHeadRot(0.0f);
                    player.teleportTo(level, boss.getX(), boss.getY(), boss.getZ() - 4.5, java.util.Set.of(), 0.0f, 0.0f, true);
                } catch (Throwable e) {
                    done.run();
                    throw e;
                }
                helper.runAfterDelay(6, () -> {
                    try {
                        float hp2 = boss.getHealth();
                        boss.damageCooldownTime = 0;
                        boolean hurt = boss.hurtServer(level, player.damageSources().playerAttack(player), 20.0f);
                        helper.assertTrue(hurt && boss.getHealth() < hp2, "Von hinten trifft man ihn nicht");
                        helper.assertTrue(boss.currentAttack() == Attack.GUARD, "Von hinten getroffen pariert er doch: " + boss.currentAttack());
                    } finally {
                        done.run();
                    }
                    helper.succeed();
                });
            });
        });
    }

    /** Er landet von selbst (nach einigen Künsten in der Luft), ficht am Boden – und schwingt sich wieder empor. */
    private static void stance(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Field field = field(helper, 7, 36);
        BlockPos pos = field.origin();
        SchrolliEntity boss = schrolli(level, pos);
        LivingEntity knight = dummy(level, pos.offset(3, 0, 10));
        boss.setTarget(knight);
        boolean[] seen = {false, false};
        int[] meleeArts = {0};
        Attack[] last = {Attack.NONE};
        helper.onEachTick(() -> {
            knight.setHealth(knight.getMaxHealth());
            if (boss.isGrounded()) {
                seen[0] = true;
            } else if (seen[0]) {
                seen[1] = true;
            }
            Attack now = boss.currentAttack();
            if (now != last[0] && now.melee()) {
                meleeArts[0]++;
            }
            last[0] = now;
        });
        helper.succeedWhen(() -> {
            helper.assertTrue(seen[0], "Er landet nicht von selbst");
            helper.assertTrue(meleeArts[0] >= 2, "Am Boden ficht er nicht: " + meleeArts[0]);
            helper.assertTrue(seen[1], "Er schwingt sich nicht wieder empor");
            field.close();
        });
    }

    /** Waagrechter Abstand (für die Tests). */
    private static final class DivineFxProbe {
        static double flat(Vec3 a, Vec3 b) {
            return Math.sqrt((a.x - b.x) * (a.x - b.x) + (a.z - b.z) * (a.z - b.z));
        }
    }
}
