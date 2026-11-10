package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.arena.ArenaIsland;
import io.github.sirschrolli.kingdomsatwar.arena.BossArenaBuilder;
import io.github.sirschrolli.kingdomsatwar.arena.BossArenaKind;
import io.github.sirschrolli.kingdomsatwar.arena.CreatorArena;
import io.github.sirschrolli.kingdomsatwar.arena.CreatorStages;
import io.github.sirschrolli.kingdomsatwar.block.creator.CreatorAltarBlockEntity;
import io.github.sirschrolli.kingdomsatwar.creator.CreatorGear;
import io.github.sirschrolli.kingdomsatwar.creator.CreatorWings;
import io.github.sirschrolli.kingdomsatwar.entity.creator.DivineShotEntity;
import io.github.sirschrolli.kingdomsatwar.entity.creator.SchrolliEntity;
import io.github.sirschrolli.kingdomsatwar.entity.creator.SchrolliEntity.Attack;
import io.github.sirschrolli.kingdomsatwar.hero.HeroLevel;
import io.github.sirschrolli.kingdomsatwar.hero.ImmortalKeep;
import io.github.sirschrolli.kingdomsatwar.magic.fx.SpellFxEntity;
import io.github.sirschrolli.kingdomsatwar.registry.CreatorBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModAttachments;
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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Seit 0.78: Schrolli der Allmächtige und alles, was er mitbringt – seine Künste (und Lichtkäfig und Schwarzes Loch der
 * früheren Gestalten), die drei Bühnen der Schöpfung, die Ausrüstung des Schöpfers (Heiligenschein, Schwingen, Räder,
 * Rüstung über 30) und dass ein Unsterblicher (Heldenstufe 200) sein Hab und Gut behält.
 */
final class AlmightyGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ARTS =
            ModGameTests.TEST_FUNCTIONS.register("schrolli_almighty_arts", () -> AlmightyGameTests::arts);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> STAGES =
            ModGameTests.TEST_FUNCTIONS.register("creator_stages", () -> AlmightyGameTests::stages);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> GEAR =
            ModGameTests.TEST_FUNCTIONS.register("creator_gear", () -> AlmightyGameTests::gear);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> KEEP =
            ModGameTests.TEST_FUNCTIONS.register("hero_keep", () -> AlmightyGameTests::keep);

    private AlmightyGameTests() {}

    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var test : List.of(ARTS, STAGES, GEAR, KEEP)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 3000, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
    }

    // =====================================================================================
    // Die Künste des Allmächtigen
    // =====================================================================================

    /** Ein Schritt der Folge: eine Kunst erzwingen, nach einer Weile prüfen. */
    private record Step(Attack attack, int after, Runnable before, Runnable check) {}

    /**
     * Jede neue Kunst wird erzwungen und zeigt ihre Wirkung: Lichtkäfig (Stäbe, Schaden), Schwarzes Loch (Wirbel, Schaden),
     * Sonnensturz (stürzende Sonnen), Prismenkäfig (sechs Prismen, Strahlen), Klingenkranz (Speere im Kreis), Sonneneruption
     * (Warnkreise), Gericht der Sonne (außen trifft es nur, wer draußen steht, innen nur, wer drinnen steht) und Sonnenspalter.
     */
    private static void arts(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        SchrolliGameTests.Field field = SchrolliGameTests.field(helper, 9, 32);
        BlockPos pos = field.origin();
        AABB area = field.area();
        SchrolliEntity boss = SchrolliGameTests.schrolli(level, pos);
        boss.forceAlmighty();
        LivingEntity inner = SchrolliGameTests.dummy(level, pos.offset(0, 0, 8));
        LivingEntity outer = SchrolliGameTests.dummy(level, pos.offset(20, 0, 0));
        List<String> problems = new ArrayList<>();
        helper.assertTrue(boss.isAlmighty() && boss.phase() == 3, "Kein Allmächtiger");
        float[] hp = new float[2];
        Runnable mark = () -> {
            hp[0] = inner.getHealth();
            hp[1] = outer.getHealth();
        };
        List<Step> steps = List.of(
                new Step(Attack.LIGHT_CAGE, 20, mark, () -> {
                    if (SchrolliGameTests.fx(level, area, SpellFxEntity.Kind.BEAM) < 8) {
                        problems.add("Lichtkäfig: " + SchrolliGameTests.fx(level, area, SpellFxEntity.Kind.BEAM) + " Stäbe");
                    }
                }),
                new Step(Attack.NONE, 40, () -> {
                }, () -> {
                    if (inner.getHealth() >= hp[0]) {
                        problems.add("Lichtkäfig: kein Schaden");
                    }
                }),
                new Step(Attack.BLACK_HOLE, 30, mark, () -> {
                    if (SchrolliGameTests.fx(level, area, SpellFxEntity.Kind.VORTEX) < 1) {
                        problems.add("Schwarzes Loch: kein Wirbel");
                    }
                }),
                new Step(Attack.NONE, 100, () -> {
                }, () -> {
                    if (inner.getHealth() >= hp[0]) {
                        problems.add("Schwarzes Loch: kein Schaden");
                    }
                }),
                new Step(Attack.SUNFALL, 24, mark, () -> {
                    long suns = level.getEntitiesOfClass(DivineShotEntity.class, area, DivineShotEntity::isFallingSun).size();
                    if (suns < 1) {
                        problems.add("Sonnensturz: keine Sonnen");
                    }
                }),
                new Step(Attack.PRISM, 36, mark, () -> {
                    if (SchrolliGameTests.shots(level, area, DivineShotEntity.Kind.FIXED_STAR) != 6) {
                        problems.add("Prismenkäfig: " + SchrolliGameTests.shots(level, area, DivineShotEntity.Kind.FIXED_STAR) + " Prismen");
                    }
                    if (SchrolliGameTests.fx(level, area, SpellFxEntity.Kind.BEAM) < 6) {
                        problems.add("Prismenkäfig: " + SchrolliGameTests.fx(level, area, SpellFxEntity.Kind.BEAM) + " Strahlen");
                    }
                }),
                new Step(Attack.BLADES, 12, mark, () -> {
                    if (SchrolliGameTests.shots(level, area, DivineShotEntity.Kind.SPEAR) < 14) {
                        problems.add("Klingenkranz: " + SchrolliGameTests.shots(level, area, DivineShotEntity.Kind.SPEAR) + " Speere");
                    }
                }),
                new Step(Attack.FLARE, 20, mark, () -> {
                    if (SchrolliGameTests.fx(level, area, SpellFxEntity.Kind.CIRCLE) < 5) {
                        problems.add("Sonneneruption: " + SchrolliGameTests.fx(level, area, SpellFxEntity.Kind.CIRCLE) + " Warnkreise");
                    }
                }),
                new Step(Attack.SOLAR_JUDGMENT, 66, () -> {
                    // was die vorigen Künste noch in der Luft haben (Warnkreise, schwebende Speere), vergeht
                    level.getEntitiesOfClass(SpellFxEntity.class, area).forEach(SpellFxEntity::discard);
                    level.getEntitiesOfClass(DivineShotEntity.class, area).forEach(DivineShotEntity::discard);
                    mark.run();
                }, () -> {
                    if (outer.getHealth() >= hp[1]) {
                        problems.add("Gericht der Sonne: außen kein Schaden");
                    }
                    if (inner.getHealth() < hp[0]) {
                        problems.add("Gericht der Sonne: außen traf auch innen");
                    }
                    mark.run();
                }),
                new Step(Attack.NONE, 36, () -> {
                }, () -> {
                    if (inner.getHealth() >= hp[0]) {
                        problems.add("Gericht der Sonne: innen kein Schaden");
                    }
                }),
                new Step(Attack.SUN_CLEAVE, 38, mark, () -> {
                    if (inner.getHealth() >= hp[0] || SchrolliGameTests.fx(level, area, SpellFxEntity.Kind.FISSURE) < 3) {
                        problems.add("Sonnenspalter: Schaden " + (hp[0] - inner.getHealth()) + ", Risse "
                                + SchrolliGameTests.fx(level, area, SpellFxEntity.Kind.FISSURE));
                    }
                }));
        run(helper, boss, inner, steps, 0, () -> {
            field.close();
            helper.assertTrue(problems.isEmpty(), "Künste des Allmächtigen: " + problems);
            helper.succeed();
        });
    }

    /** Führt die Schritte nacheinander aus (Attack.NONE: die laufende Kunst läuft weiter). */
    private static void run(GameTestHelper helper, SchrolliEntity boss, LivingEntity target, List<Step> steps, int index, Runnable done) {
        if (index >= steps.size()) {
            done.run();
            return;
        }
        Step step = steps.get(index);
        step.before().run();
        if (step.attack() != Attack.NONE) {
            boss.setTarget(target);
            boss.forceAttack(step.attack());
        }
        helper.runAfterDelay(step.after(), () -> {
            step.check().run();
            run(helper, boss, target, steps, index + 1, done);
        });
    }

    // =====================================================================================
    // Die drei Bühnen der Schöpfung
    // =====================================================================================

    private static boolean solid(ServerLevel level, BlockPos p) {
        return !level.getBlockState(p).isAir();
    }

    /**
     * Der Hof verwandelt sich ins Firmament (Polarstern in der Mitte, Obelisken, keine Brücken) und ins Herz der Sonne (weißer
     * Kern, Pylonen); der Altar bleibt jedes Mal stehen und merkt sich die Bühne; auf jeder Bühne finden Herausforderer ihre
     * Plätze am Rand. Beim nächsten Kampf entsteht wieder der Hof (mit Säulen und Brücken).
     */
    private static void stages(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos base = helper.absolutePos(BlockPos.ZERO);
        BlockPos center = new BlockPos(base.getX() + 40_000, CreatorArena.FLOOR_Y + 1, base.getZ() + 40_000);
        BlockPos floor = center.below();
        List<long[]> chunks = new ArrayList<>();
        for (int cx = (center.getX() - 72) >> 4; cx <= (center.getX() + 72) >> 4; cx++) {
            for (int cz = (center.getZ() - 72) >> 4; cz <= (center.getZ() + 72) >> 4; cz++) {
                level.getChunk(cx, cz);
                level.setChunkForced(cx, cz, true);
                chunks.add(new long[] {cx, cz});
            }
        }
        List<String> problems = new ArrayList<>();
        try {
            BlockPos altar = CreatorArena.build(level, center);
            helper.assertTrue(level.getBlockEntity(altar) instanceof CreatorAltarBlockEntity, "Kein Altar im Hof");
            double pillarAngle = Math.toRadians(15.0);
            BlockPos pillar = floor.offset((int) Math.round(Math.cos(pillarAngle) * 35), 6, (int) Math.round(Math.sin(pillarAngle) * 35));
            double bridgeAngle = Math.toRadians(45.0);
            BlockPos bridge = floor.offset((int) Math.round(Math.cos(bridgeAngle) * 55), 0, (int) Math.round(Math.sin(bridgeAngle) * 55));
            helper.assertTrue(solid(level, bridge), "Im Hof fehlt die Brücke");
            // das Firmament
            CreatorStages.transform(level, center, CreatorStages.Stage.FIRMAMENT, altar);
            if (!level.getBlockState(floor).is(Blocks.SEA_LANTERN)) {
                problems.add("Firmament: kein Polarstern, sondern " + level.getBlockState(floor));
            }
            BlockState obelisk = level.getBlockState(pillar);
            if (!obelisk.is(Blocks.OBSIDIAN) && !obelisk.is(Blocks.CRYING_OBSIDIAN) && !obelisk.is(Blocks.AMETHYST_BLOCK)) {
                problems.add("Firmament: kein Obelisk, sondern " + obelisk);
            }
            if (solid(level, bridge)) {
                problems.add("Firmament: die Brücke steht noch");
            }
            checkAltar(level, altar, 1, "Firmament", problems);
            checkSpots(level, center, "Firmament", problems);
            // das Herz der Sonne
            CreatorStages.transform(level, center, CreatorStages.Stage.SUN_HEART, altar);
            if (!level.getBlockState(floor).is(Blocks.PEARLESCENT_FROGLIGHT)) {
                problems.add("Herz der Sonne: kein weißer Kern, sondern " + level.getBlockState(floor));
            }
            double pylonAngle = Math.toRadians(22.5);
            BlockPos pylon = floor.offset((int) Math.round(Math.cos(pylonAngle) * 34), 6, (int) Math.round(Math.sin(pylonAngle) * 34));
            if (!solid(level, pylon)) {
                problems.add("Herz der Sonne: kein Pylon");
            }
            if (solid(level, pillar) && level.getBlockState(pillar).is(Blocks.OBSIDIAN)) {
                problems.add("Herz der Sonne: der Obelisk steht noch");
            }
            checkAltar(level, altar, 2, "Herz der Sonne", problems);
            checkSpots(level, center, "Herz der Sonne", problems);
            // der nächste Kampf: wieder der Hof (wie im Bauplan der Kampfdimension)
            CreatorStages.clear(level, center, altar);
            BlockPos again = CreatorArena.build(level, center);
            if (level.getBlockEntity(again) instanceof CreatorAltarBlockEntity be) {
                be.setArenaStage(0);
            }
            if (!level.getBlockState(pillar).is(Blocks.QUARTZ_PILLAR) && !level.getBlockState(pillar).is(Blocks.GOLD_BLOCK)) {
                problems.add("Hof: keine Säule, sondern " + level.getBlockState(pillar));
            }
            if (!solid(level, bridge)) {
                problems.add("Hof: die Brücke fehlt");
            }
            checkAltar(level, again, 0, "Hof", problems);
        } finally {
            ArenaIsland.clear(level, center, BossArenaKind.CREATOR.reach(), 60, 90);
            for (long[] c : chunks) {
                level.setChunkForced((int) c[0], (int) c[1], false);
            }
        }
        helper.assertTrue(problems.isEmpty(), "Bühnen der Schöpfung: " + problems);
        helper.succeed();
    }

    private static void checkAltar(ServerLevel level, BlockPos altar, int stage, String name, List<String> problems) {
        if (!level.getBlockState(altar).is(CreatorBlocks.CREATOR_ALTAR.get())) {
            problems.add(name + ": der Altar ist fort");
        } else if (!(level.getBlockEntity(altar) instanceof CreatorAltarBlockEntity be) || be.arenaStage() != stage) {
            problems.add(name + ": der Altar kennt seine Bühne nicht");
        }
    }

    private static void checkSpots(ServerLevel level, BlockPos center, String name, List<String> problems) {
        List<Vec3> spots = BossArenaBuilder.spawnPoints(level, center, BossArenaKind.CREATOR, 6);
        for (Vec3 spot : spots) {
            double d = Math.sqrt((spot.x - center.getX() - 0.5) * (spot.x - center.getX() - 0.5) + (spot.z - center.getZ() - 0.5) * (spot.z - center.getZ() - 0.5));
            if (d < 12.0 || d > 34.0 || Math.abs(spot.y - center.getY()) > 4.0) {
                problems.add(name + ": schlechter Platz " + spot);
                return;
            }
        }
    }

    // =====================================================================================
    // Die Ausrüstung des Schöpfers
    // =====================================================================================

    /**
     * Heiligenschein (Nachtsicht, keine Blindheit, Untote brennen), Räder (Feinde leuchten), Schwingen (schneller Schwingenschlag
     * als Ebenbild), Rüstung bis 60 (Minecraft deckelt sonst bei 30) und der Speer (jeder dritte Treffer eine Lichtwelle).
     */
    private static void gear(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        SchrolliGameTests.Field field = SchrolliGameTests.field(helper, 10, 8);
        BlockPos pos = field.origin();
        ServerPlayer player = MageGameTests.survivalPlayer(helper, "test-creator-gear", new io.netty.channel.embedded.EmbeddedChannel[1], 0);
        player.teleportTo(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, java.util.Set.of(), 0.0f, 0.0f, true);
        level.getChunkSource().move(player);
        player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(CreatorBlocks.CREATOR_HALO.get()));
        player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(CreatorBlocks.CREATOR_WINGS.get()));
        player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(CreatorBlocks.CREATOR_RINGS.get()));
        Zombie zombie = io.github.sirschrolli.kingdomsatwar.registry.ModEntities.ZOMBIE.get().create(level, EntitySpawnReason.EVENT);
        Zombie far = io.github.sirschrolli.kingdomsatwar.registry.ModEntities.ZOMBIE.get().create(level, EntitySpawnReason.EVENT);
        if (zombie == null || far == null) {
            throw new IllegalStateException("kein Zombie");
        }
        zombie.snapTo(pos.getX() + 3.5, pos.getY(), pos.getZ() + 0.5, 0.0f, 0.0f);
        zombie.setNoAi(true);
        far.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 14.5, 0.0f, 0.0f);
        far.setNoAi(true);
        far.setPersistenceRequired();
        zombie.setPersistenceRequired();
        level.addFreshEntity(zombie);
        level.addFreshEntity(far);
        List<String> problems = new ArrayList<>();
        // der Testspieler hat keine Verbindung, die ihn tickt: hier tickt er jeden Tick selbst
        boolean[] gone = {false};
        helper.onEachTick(() -> {
            if (!gone[0] && !player.isRemoved()) {
                player.doTick();
            }
        });
        helper.runAfterDelay(2, () -> {
            try {
                if (!CreatorGear.image(player)) {
                    problems.add("Kein Ebenbild mit allen drei Teilen");
                }
                if (CreatorWings.cooldown(player) != CreatorWings.BOOST_COOLDOWN / 2) {
                    problems.add("Der Schwingenschlag lädt nicht schneller");
                }
                if (player.getArmorValue() < 22) {
                    problems.add("Rüstung der Ausrüstung: " + player.getArmorValue());
                }
                player.getAttribute(Attributes.ARMOR).setBaseValue(50.0);
                if (player.getArmorValue() != 60) {
                    problems.add("Heldenrüstung bis 60: " + player.getArmorValue());
                }
                player.getAttribute(Attributes.ARMOR).setBaseValue(0.0);
                player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200));
                if (player.hasEffect(MobEffects.BLINDNESS)) {
                    problems.add("Der Heiligenschein lässt Blindheit zu");
                }
                // der Speer: jeder dritte Treffer eine Lichtwelle (auch um das Ziel herum)
                ItemStack spear = new ItemStack(CreatorBlocks.CREATOR_SCEPTER.get());
                float before = zombie.getHealth();
                for (int k = 0; k < 3; k++) {
                    zombie.damageCooldownTime = 0;
                    spear.getItem().postHurtEnemy(spear, zombie, player);
                }
                if (zombie.getHealth() >= before) {
                    problems.add("Der dritte Treffer entfesselt keine Lichtwelle");
                }
            } catch (RuntimeException e) {
                problems.add(e.toString());
            }
            helper.runAfterDelay(44, () -> {
                try {
                    if (!player.hasEffect(MobEffects.NIGHT_VISION)) {
                        problems.add("Keine Nachtsicht mit dem Heiligenschein");
                    }
                    if (!zombie.isOnFire() && zombie.getRemainingFireTicks() <= 0) {
                        problems.add("Der Untote brennt nicht");
                    }
                    if (!far.hasEffect(MobEffects.GLOWING)) {
                        problems.add("Die Räder enthüllen den Feind nicht");
                    }
                } finally {
                    gone[0] = true;
                    zombie.discard();
                    far.discard();
                    field.close();
                    level.getServer().getPlayerList().remove(player);
                }
                helper.assertTrue(problems.isEmpty(), "Ausrüstung des Schöpfers: " + problems);
                helper.succeed();
            });
        });
    }

    // =====================================================================================
    // Der Unsterbliche behält sein Hab und Gut
    // =====================================================================================

    /** Auf Stufe 199 nicht, auf Stufe 200 schon: das Inventar wird verwahrt (dann ist es leer) und kehrt an seine Plätze zurück. */
    private static void keep(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = MageGameTests.survivalPlayer(helper, "test-hero-keep", new io.netty.channel.embedded.EmbeddedChannel[1], 0);
        try {
            HeroLevel.set(player, new HeroLevel.Hero(HeroLevel.total(199), List.of(), 0));
            helper.assertFalse(ImmortalKeep.keeps(player), "Stufe 199 behält das Inventar");
            HeroLevel.set(player, new HeroLevel.Hero(HeroLevel.total(200), List.of(), 0));
            helper.assertTrue(ImmortalKeep.keeps(player), "Stufe 200 behält das Inventar nicht");
            Inventory inventory = player.getInventory();
            inventory.setItem(0, new ItemStack(Items.DIAMOND_SWORD));
            inventory.setItem(7, new ItemStack(Items.BREAD, 32));
            inventory.setItem(Inventory.SLOT_OFFHAND, new ItemStack(Items.SHIELD));
            player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(CreatorBlocks.CREATOR_HALO.get()));
            ImmortalKeep.keep(player);
            helper.assertTrue(inventory.isEmpty(), "Nach dem Verwahren liegt noch etwas im Inventar");
            ImmortalKeep.Kept kept = player.getData(ModAttachments.KEPT_INVENTORY);
            helper.assertValueEqual(kept.items().size(), 4, "Verwahrte Gegenstände");
            ImmortalKeep.restore(player, kept);
            player.setData(ModAttachments.KEPT_INVENTORY, ImmortalKeep.Kept.NONE);
            helper.assertTrue(inventory.getItem(0).is(Items.DIAMOND_SWORD), "Das Schwert ist nicht an seinem Platz");
            helper.assertTrue(inventory.getItem(7).is(Items.BREAD) && inventory.getItem(7).getCount() == 32, "Das Brot ist nicht an seinem Platz");
            helper.assertTrue(player.getOffhandItem().is(Items.SHIELD), "Der Schild ist nicht in der Zweithand");
            helper.assertTrue(player.getItemBySlot(EquipmentSlot.HEAD).is(CreatorBlocks.CREATOR_HALO.get()), "Der Heiligenschein ist nicht auf dem Kopf");
        } finally {
            level.getServer().getPlayerList().remove(player);
        }
        helper.succeed();
    }
}
