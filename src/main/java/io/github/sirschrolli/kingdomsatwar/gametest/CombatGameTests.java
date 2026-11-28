package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.entity.combat.Combat;
import io.github.sirschrolli.kingdomsatwar.entity.combat.MoveKind;
import io.github.sirschrolli.kingdomsatwar.entity.combat.Style;
import io.github.sirschrolli.kingdomsatwar.entity.combat.Temper;
import io.github.sirschrolli.kingdomsatwar.entity.monster.MonsterShot;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/** Tests der Kampfstile (seit 1.14): jedes Monster kämpft anders, und jeder besondere Angriff tut, was er soll. */
final class CombatGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> STYLES =
            ModGameTests.TEST_FUNCTIONS.register("combat_styles", () -> CombatGameTests::styles);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LUNGE =
            ModGameTests.TEST_FUNCTIONS.register("combat_lunge", () -> CombatGameTests::lunge);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SLAM =
            ModGameTests.TEST_FUNCTIONS.register("combat_slam", () -> CombatGameTests::slam);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SWEEP =
            ModGameTests.TEST_FUNCTIONS.register("combat_sweep", () -> CombatGameTests::sweep);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CHARGE =
            ModGameTests.TEST_FUNCTIONS.register("combat_charge", () -> CombatGameTests::charge);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> GRAB =
            ModGameTests.TEST_FUNCTIONS.register("combat_grab", () -> CombatGameTests::grab);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> GUARD =
            ModGameTests.TEST_FUNCTIONS.register("combat_guard", () -> CombatGameTests::guard);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ARROWS =
            ModGameTests.TEST_FUNCTIONS.register("combat_arrows", () -> CombatGameTests::arrows);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> WEB =
            ModGameTests.TEST_FUNCTIONS.register("combat_web", () -> CombatGameTests::web);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CREEPER =
            ModGameTests.TEST_FUNCTIONS.register("combat_creeper", () -> CombatGameTests::creeper);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CURSE =
            ModGameTests.TEST_FUNCTIONS.register("combat_curse", () -> CombatGameTests::curse);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FANGS =
            ModGameTests.TEST_FUNCTIONS.register("combat_fangs", () -> CombatGameTests::fangs);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FIRE =
            ModGameTests.TEST_FUNCTIONS.register("combat_fire", () -> CombatGameTests::fire);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ROAR =
            ModGameTests.TEST_FUNCTIONS.register("combat_roar_frenzy", () -> CombatGameTests::roarFrenzy);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> BURST =
            ModGameTests.TEST_FUNCTIONS.register("combat_burst", () -> CombatGameTests::burst);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> AMBUSH =
            ModGameTests.TEST_FUNCTIONS.register("combat_ambush", () -> CombatGameTests::ambush);

    private CombatGameTests() {}

    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var test : List.of(STYLES, LUNGE, SLAM, SWEEP, CHARGE, GRAB, GUARD, ARROWS, WEB, CREEPER, CURSE, FANGS, FIRE, ROAR, BURST, AMBUSH)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 300, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
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
        helper.getLevel().getServer().setDifficulty(Difficulty.HARD, true);
    }

    /** Ein Ritter, der viel aushält und still steht. */
    private static LivingEntity knight(GameTestHelper helper, double x, double z) {
        LivingEntity knight = helper.spawnWithNoFreeWill(ModEntities.SOLDIERS.get(SoldierType.KNIGHT).get(), new BlockPos(0, 1, 0));
        knight.getAttribute(Attributes.MAX_HEALTH).setBaseValue(600.0);
        knight.setHealth(600.0f);
        knight.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(0.0);
        Vec3 at = helper.absoluteVec(new Vec3(x, 1.0, z));
        knight.snapTo(at.x, at.y, at.z, 0.0f, 0.0f);
        return knight;
    }

    /** Ein Monster mit eigenem Willen (sonst hätte es keinen Kampf), mit Helm gegen die Sonne. */
    private static <T extends Mob> T monster(GameTestHelper helper, EntityType<T> type, double x, double z, Temper temper, MoveKind... moves) {
        Vec3 at = helper.absoluteVec(new Vec3(x, 1.0, z));
        T mob = type.create(helper.getLevel(), EntitySpawnReason.MOB_SUMMONED);
        helper.assertTrue(mob != null, "kein " + type);
        mob.snapTo(at.x, at.y, at.z, 0.0f, 0.0f);
        mob.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.LEATHER_HELMET));
        mob.setPersistenceRequired();
        helper.getLevel().addFreshEntity(mob);
        helper.assertTrue(Combat.fight(mob) != null, "kein Kampfstil: " + mob.getName().getString());
        Combat.setStyle(mob, new Style(temper, List.of(moves), 1.0f));
        Combat.pause(mob, 100000);
        return mob;
    }

    private static float hurt(LivingEntity knight) {
        return knight.getMaxHealth() - knight.getHealth();
    }

    /** Erzwingt den Angriff und wartet, bis er vorbei ist (oder {@code done} früher eintritt). */
    private static void run(GameTestHelper helper, Mob mob, MoveKind kind, LivingEntity target, int maxTicks,
            java.util.function.BooleanSupplier done, java.util.function.Supplier<String> message, Runnable then) {
        helper.assertTrue(Combat.force(mob, kind, target), "kein Kampf für " + kind);
        helper.runAfterDelay(2, () -> ModGameTests.await(helper, () -> done.getAsBoolean() || Combat.current(mob) == null, maxTicks, message,
                () -> {
                    if (!done.getAsBoolean()) {
                        helper.fail(message.get());
                        return;
                    }
                    then.run();
                }));
    }

    // =====================================================================================

    /** Kaum zwei Wiedergänger kämpfen gleich – und der Kampfstil überlebt Speichern und Laden. */
    private static void styles(GameTestHelper helper) {
        floor(helper);
        ServerLevel level = helper.getLevel();
        Set<Temper> tempers = new HashSet<>();
        Set<List<MoveKind>> sets = new HashSet<>();
        for (int i = 0; i < 40; i++) {
            Mob mob = ModEntities.ZOMBIE.get().create(level, EntitySpawnReason.MOB_SUMMONED);
            Vec3 at = helper.absoluteVec(new Vec3(1.5 + i % 7, 1.0, 1.5 + i / 7));
            mob.snapTo(at.x, at.y, at.z, 0.0f, 0.0f);
            mob.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.LEATHER_HELMET));
            level.addFreshEntity(mob);
            Style style = Combat.style(mob);
            helper.assertTrue(style != null, "kein Kampfstil");
            int family = (int) style.moves().stream().filter(m -> m != MoveKind.CIRCLE && m != MoveKind.GUARD && m != MoveKind.ROAR).count();
            helper.assertTrue(style.moves().size() >= 2 && style.moves().size() <= 4, "Zahl der Angriffe: " + style.moves());
            tempers.add(style.temper());
            sets.add(style.moves().stream().sorted().toList());
            if (family > 3) {
                helper.fail("zu viele Familienangriffe: " + style.moves());
            }
            if (i == 0) {
                TagValueOutput out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
                mob.saveWithoutId(out);
                CompoundTag tag = out.buildResult();
                tag.remove("UUID");
                Mob copy = (Mob) mob.getType().create(level, EntitySpawnReason.LOAD);
                copy.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), tag));
                Vec3 c = helper.absoluteVec(new Vec3(7.5, 1.0, 7.5));
                copy.snapTo(c.x, c.y, c.z, 0.0f, 0.0f);
                level.addFreshEntity(copy);
                helper.assertTrue(style.equals(Combat.style(copy)), "Kampfstil nach dem Laden: " + style + " -> " + Combat.style(copy));
                copy.discard();
            }
            mob.discard();
        }
        helper.assertTrue(tempers.size() >= 4, "zu wenige Temperamente: " + tempers);
        helper.assertTrue(sets.size() >= 6, "zu wenige verschiedene Angriffsauswahlen: " + sets.size());
        helper.succeed();
    }

    /** Ansprung: aus sechs Blöcken Abstand – es fliegt heran und trifft. */
    private static void lunge(GameTestHelper helper) {
        floor(helper);
        LivingEntity knight = knight(helper, 7.5, 4.5);
        Mob mob = monster(helper, ModEntities.ZOMBIE.get(), 1.5, 4.5, Temper.UNGESTUEM, MoveKind.LUNGE);
        double start = mob.getX();
        run(helper, mob, MoveKind.LUNGE, knight, 80, () -> hurt(knight) > 0.0f,
                () -> "Ansprung traf nicht (Weg " + (mob.getX() - start) + ", jetzt " + Combat.current(mob) + ")",
                () -> {
                    helper.assertTrue(mob.getX() - start > 3.0, "kaum gesprungen: " + (mob.getX() - start));
                    helper.succeed();
                });
    }

    /** Hammerschlag: wer im Ring steht, wird getroffen und hochgeworfen. */
    private static void slam(GameTestHelper helper) {
        floor(helper);
        LivingEntity knight = knight(helper, 5.6, 4.5);
        Mob mob = monster(helper, ModEntities.ZOMBIE.get(), 3.5, 4.5, Temper.ZAEH, MoveKind.SLAM);
        run(helper, mob, MoveKind.SLAM, knight, 60, () -> hurt(knight) > 0.0f, () -> "Hammerschlag traf nicht", helper::succeed);
    }

    /** Wirbelhieb: beide Ritter ringsum werden getroffen. */
    private static void sweep(GameTestHelper helper) {
        floor(helper);
        LivingEntity a = knight(helper, 6.3, 4.5);
        LivingEntity b = knight(helper, 2.7, 4.5);
        Mob mob = monster(helper, ModEntities.VINDICATOR.get(), 4.5, 4.5, Temper.UNGESTUEM, MoveKind.SWEEP);
        mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_AXE));
        run(helper, mob, MoveKind.SWEEP, a, 60, () -> hurt(a) > 0.0f && hurt(b) > 0.0f,
                () -> "Wirbelhieb: vorn " + hurt(a) + ", hinten " + hurt(b), helper::succeed);
    }

    /** Sturmangriff: aus sieben Blöcken – der Ritter wird getroffen und fortgeschleudert. */
    private static void charge(GameTestHelper helper) {
        floor(helper);
        LivingEntity knight = knight(helper, 7.5, 4.5);
        Mob mob = monster(helper, ModEntities.VINDICATOR.get(), 0.5, 4.5, Temper.UNGESTUEM, MoveKind.CHARGE);
        run(helper, mob, MoveKind.CHARGE, knight, 90, () -> hurt(knight) > 0.0f,
                () -> "Sturmangriff traf nicht (" + mob.position() + ")", helper::succeed);
    }

    /** Umklammern: der Gepackte ist langsam und kann nichts abbauen. */
    private static void grab(GameTestHelper helper) {
        floor(helper);
        LivingEntity knight = knight(helper, 5.8, 4.5);
        Mob mob = monster(helper, ModEntities.ZOMBIE.get(), 4.3, 4.5, Temper.ZAEH, MoveKind.GRAB);
        run(helper, mob, MoveKind.GRAB, knight, 40,
                () -> knight.hasEffect(MobEffects.MINING_FATIGUE) && knight.getEffect(MobEffects.SLOWNESS) != null
                        && knight.getEffect(MobEffects.SLOWNESS).getAmplifier() >= 3,
                () -> "Umklammern packte nicht", helper::succeed);
    }

    /** Deckung: von vorn kommt kaum etwas durch, und es schlägt zurück; von hinten bricht sie. */
    private static void guard(GameTestHelper helper) {
        floor(helper);
        LivingEntity front = knight(helper, 6.5, 4.5);
        Mob mob = monster(helper, ModEntities.ZOMBIE.get(), 4.5, 4.5, Temper.ZAEH, MoveKind.GUARD);
        mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200.0);
        mob.setHealth(200.0f);
        ServerLevel level = helper.getLevel();
        helper.assertTrue(Combat.force(mob, MoveKind.GUARD, front), "kein Kampf");
        ModGameTests.await(helper, () -> Combat.acting(mob), 30, () -> "Deckung kam nicht", () -> {
            float before = mob.getHealth();
            mob.hurtServer(level, level.damageSources().mobAttack(front), 10.0f);
            float blocked = before - mob.getHealth();
            helper.assertTrue(blocked > 0.0f && blocked <= 3.0f, "von vorn nicht abgefangen: " + blocked);
            ModGameTests.await(helper, () -> hurt(front) > 0.0f, 10, () -> "kein Gegenschlag", () -> {
                // von hinten: neu decken, dann trifft es voll
                LivingEntity back = knight(helper, 2.5, 4.5);
                helper.assertTrue(Combat.force(mob, MoveKind.GUARD, front), "kein Kampf");
                ModGameTests.await(helper, () -> Combat.acting(mob), 30, () -> "zweite Deckung kam nicht", () -> {
                    float b2 = mob.getHealth();
                            mob.hurtServer(level, level.damageSources().mobAttack(back), 10.0f);
                    float full = b2 - mob.getHealth();
                    helper.assertTrue(full >= 7.0f, "von hinten abgefangen: " + full);
                    helper.succeed();
                });
            });
        });
    }

    /** Gerippe: die Salve schießt drei Pfeile, der Zielschuss einen schnellen, harten. */
    private static void arrows(GameTestHelper helper) {
        floor(helper);
        LivingEntity knight = knight(helper, 8.5, 8.5);
        Mob mob = monster(helper, ModEntities.SKELETON.get(), 0.5, 0.5, Temper.LAUERND, MoveKind.VOLLEY, MoveKind.AIMED);
        mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
        ServerLevel level = helper.getLevel();
        java.util.function.IntSupplier count = () -> level.getEntitiesOfClass(AbstractArrow.class, ModGameTests.ownArea(helper, 8.0),
                a -> a.getOwner() == mob).size();
        run(helper, mob, MoveKind.VOLLEY, knight, 60, () -> count.getAsInt() >= 3, () -> "Salve: " + count.getAsInt() + " Pfeile", () -> {
            for (AbstractArrow a : level.getEntitiesOfClass(AbstractArrow.class, ModGameTests.ownArea(helper, 8.0))) {
                a.discard();
            }
            helper.runAfterDelay(2, () -> run(helper, mob, MoveKind.AIMED, knight, 80,
                    () -> level.getEntitiesOfClass(AbstractArrow.class, ModGameTests.ownArea(helper, 8.0),
                            a -> a.getOwner() == mob && a.isCritArrow() && a.getDeltaMovement().length() > 2.0).size() == 1
                            || hurt(knight) > 0.0f,
                    () -> "kein Zielschuss", helper::succeed));
        });
    }

    /** Netzschuss: der Getroffene klebt fest, und unter ihm liegt kurz ein Netz. */
    private static void web(GameTestHelper helper) {
        floor(helper);
        LivingEntity knight = knight(helper, 7.5, 4.5);
        Mob mob = monster(helper, ModEntities.SPIDER.get(), 1.5, 4.5, Temper.UNGESTUEM, MoveKind.SPIT);
        ServerLevel level = helper.getLevel();
        helper.assertTrue(Combat.force(mob, MoveKind.SPIT, knight), "kein Kampf");
        ModGameTests.await(helper, () -> knight.getEffect(MobEffects.SLOWNESS) != null && knight.getEffect(MobEffects.SLOWNESS).getAmplifier() >= 3,
                80, () -> "Netz traf nicht (" + level.getEntitiesOfClass(MonsterShot.class, ModGameTests.ownArea(helper, 8.0)).size() + " Geschosse)",
                () -> {
                    BlockPos feet = knight.blockPosition();
                    helper.assertTrue(level.getBlockState(feet).is(Blocks.COBWEB), "kein Netz unter dem Ritter");
                    helper.runAfterDelay(MonsterShot.WEB_TICKS + 10, () -> {
                        helper.assertFalse(level.getBlockState(feet).is(Blocks.COBWEB), "Netz vergeht nicht");
                        helper.succeed();
                    });
                });
    }

    /** Kriecher: die Finte bläht ihn auf, ohne dass er knallt; die Ranke zieht den Ritter heran. */
    private static void creeper(GameTestHelper helper) {
        floor(helper);
        LivingEntity knight = knight(helper, 7.5, 4.5);
        Creeper mob = monster(helper, ModEntities.CREEPER.get(), 1.5, 4.5, Temper.LAUERND, MoveKind.FEINT, MoveKind.VINE);
        boolean[] swelled = {false};
        helper.assertTrue(Combat.force(mob, MoveKind.FEINT, knight), "kein Kampf");
        ModGameTests.await(helper, () -> {
            swelled[0] |= mob.getSwelling(1.0f) > 0.3f;
            return Combat.current(mob) == null;
        }, 60, () -> "Finte endet nicht", () -> {
            helper.assertTrue(swelled[0], "Finte blähte nicht auf");
            helper.assertTrue(mob.isAlive() && mob.getSwellDir() <= 0, "Finte knallte (oder brennt weiter)");
            double before = knight.distanceTo(mob);
            helper.assertTrue(Combat.force(mob, MoveKind.VINE, knight), "kein Kampf");
            ModGameTests.await(helper, () -> knight.distanceTo(mob) < before - 2.0, 60,
                    () -> "Ranke zog nicht: " + before + " -> " + knight.distanceTo(mob), () -> {
                        mob.setSwellDir(-1);
                        Combat.pause(mob, 100000);
                        mob.discard();
                        helper.succeed();
                    });
        });
    }

    /** Fluchkreis: wer drin steht, wird langsam und schwach. */
    private static void curse(GameTestHelper helper) {
        floor(helper);
        LivingEntity knight = knight(helper, 7.5, 4.5);
        Mob mob = monster(helper, ModEntities.WITCH.get(), 1.5, 4.5, Temper.ZAEH, MoveKind.CURSE);
        run(helper, mob, MoveKind.CURSE, knight, 60, () -> knight.hasEffect(MobEffects.WEAKNESS) && knight.hasEffect(MobEffects.SLOWNESS),
                () -> "Fluchkreis wirkte nicht", helper::succeed);
    }

    /** Fangkreis: um den Ritter brechen Fangzähne hervor. */
    private static void fangs(GameTestHelper helper) {
        floor(helper);
        LivingEntity knight = knight(helper, 7.5, 4.5);
        Mob mob = monster(helper, ModEntities.EVOKER.get(), 1.5, 4.5, Temper.ZAEH, MoveKind.FANGS);
        ServerLevel level = helper.getLevel();
        run(helper, mob, MoveKind.FANGS, knight, 60,
                () -> level.getEntitiesOfClass(EvokerFangs.class, knight.getBoundingBox().inflate(3.0)).size() >= 12,
                () -> "zu wenige Fangzähne: " + level.getEntitiesOfClass(EvokerFangs.class, knight.getBoundingBox().inflate(3.0)).size(),
                helper::succeed);
    }

    /** Glutgeist: der Feuerring setzt den Ritter am Boden in Brand, die Flammensäule trifft ihn. */
    private static void fire(GameTestHelper helper) {
        floor(helper);
        LivingEntity knight = knight(helper, 6.5, 4.5);
        Mob mob = monster(helper, ModEntities.BLAZE.get(), 3.5, 4.5, Temper.ZAEH, MoveKind.FIRE_RING, MoveKind.PILLAR);
        mob.setNoGravity(true);
        run(helper, mob, MoveKind.FIRE_RING, knight, 60, () -> hurt(knight) > 0.0f && knight.isOnFire(), () -> "Feuerring traf nicht", () -> {
            knight.clearFire();
            float before = hurt(knight);
            helper.runAfterDelay(12, () -> run(helper, mob, MoveKind.PILLAR, knight, 60, () -> hurt(knight) > before,
                    () -> "Flammensäule traf nicht", helper::succeed));
        });
    }

    /** Kampfschrei: die Artgenossen werden schneller und stärker; ein Rasender gerät verwundet in Raserei. */
    private static void roarFrenzy(GameTestHelper helper) {
        floor(helper);
        LivingEntity knight = knight(helper, 8.5, 4.5);
        Mob leader = monster(helper, ModEntities.ZOMBIE.get(), 1.5, 4.5, Temper.ANFUEHRER, MoveKind.ROAR);
        Mob ally = monster(helper, ModEntities.ZOMBIE.get(), 1.5, 1.5, Temper.RASEND);
        run(helper, leader, MoveKind.ROAR, knight, 40, () -> ally.hasEffect(MobEffects.SPEED) && ally.hasEffect(MobEffects.STRENGTH),
                () -> "Kampfschrei wirkte nicht", () -> {
                    helper.assertFalse(Combat.frenzied(ally), "Raserei zu früh");
                    double speed = ally.getAttributeValue(Attributes.MOVEMENT_SPEED);
                    ally.setHealth(ally.getMaxHealth() * 0.4f);
                    ModGameTests.await(helper, () -> Combat.frenzied(ally), 20, () -> "keine Raserei", () -> {
                        helper.assertTrue(ally.getAttributeValue(Attributes.MOVEMENT_SPEED) > speed, "Raserei macht nicht schneller");
                        helper.succeed();
                    });
                });
    }

    /** Schmetterlandung: der große Schleim springt und landet mit Wucht. */
    private static void burst(GameTestHelper helper) {
        floor(helper);
        LivingEntity knight = knight(helper, 6.5, 4.5);
        // erst groß, dann in die Welt (die kleinsten Schleime haben keinen Kampfstil)
        io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Ooze mob = ModEntities.SLIME.get().create(helper.getLevel(),
                EntitySpawnReason.MOB_SUMMONED);
        mob.setSize(3, true);
        Vec3 at = helper.absoluteVec(new Vec3(2.5, 1.0, 4.5));
        mob.snapTo(at.x, at.y, at.z, 0.0f, 0.0f);
        mob.setPersistenceRequired();
        helper.getLevel().addFreshEntity(mob);
        helper.assertTrue(Combat.fight(mob) != null, "großer Schleim ohne Kampfstil");
        Combat.setStyle(mob, new Style(Temper.UNGESTUEM, List.of(MoveKind.BURST), 1.0f));
        Combat.pause(mob, 100000);
        run(helper, mob, MoveKind.BURST, knight, 80, () -> hurt(knight) > 0.0f, () -> "Schmetterlandung traf nicht", helper::succeed);
    }

    /** Hinterhalt: der Leerwandler steht danach hinter dem Ritter und trifft ihn. */
    private static void ambush(GameTestHelper helper) {
        floor(helper);
        LivingEntity knight = knight(helper, 4.5, 4.5);
        knight.setYRot(0.0f);
        knight.setYHeadRot(0.0f);
        Mob mob = monster(helper, ModEntities.ENDERMAN.get(), 4.5, 8.0, Temper.ZAEH, MoveKind.AMBUSH);
        run(helper, mob, MoveKind.AMBUSH, knight, 60, () -> hurt(knight) > 0.0f,
                () -> "Hinterhalt traf nicht (" + mob.position() + ", Ritter " + knight.position() + ")", () -> {
                    // „hinter“ dem Ritter: er schaut nach Süden (+z), der Leerwandler steht nördlich von ihm
                    helper.assertTrue(mob.getZ() < knight.getZ(), "nicht hinter dem Ritter: " + mob.position());
                    helper.succeed();
                });
    }
}
