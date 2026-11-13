package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.arena.ArenaSealBlock;
import io.github.sirschrolli.kingdomsatwar.block.arena.ArenaSealBlockEntity;
import io.github.sirschrolli.kingdomsatwar.block.arena.GladiatorNetBlock;
import io.github.sirschrolli.kingdomsatwar.entity.arena.GladiatorNetEntity;
import io.github.sirschrolli.kingdomsatwar.entity.arena.StormTridentEntity;
import io.github.sirschrolli.kingdomsatwar.entity.arena.VentorEntity;
import io.github.sirschrolli.kingdomsatwar.entity.arena.WindVortexEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.item.arena.GladiatorNetItem;
import io.github.sirschrolli.kingdomsatwar.registry.ArenaGear;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.WindCharge;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.TrialSpawnerBlock;
import net.minecraft.world.level.block.entity.TrialSpawnerBlockEntity;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Tests zu Ventor, dem Gladiator der Winde (1.9): das Arenasiegel (es zählt die bezwungenen Käfige, ruft den Meister, wenn
 * jemand die Arena betritt, nimmt ihn zurück, wenn er niemanden findet, und verlangt nach seinem Fall neue Siege), seine
 * Angriffe, der Sturmdreizack und das Wurfnetz.
 */
final class VentorGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");
    private static final Identifier TOWN = KingdomsAtWar.id("test_town");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SEAL =
            ModGameTests.TEST_FUNCTIONS.register("ventor_seal", () -> VentorGameTests::seal);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ATTACKS =
            ModGameTests.TEST_FUNCTIONS.register("ventor_attacks", () -> VentorGameTests::attacks);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> STORM_TRIDENT =
            ModGameTests.TEST_FUNCTIONS.register("ventor_storm_trident", () -> VentorGameTests::stormTrident);

    private VentorGameTests() {}

    /** Lädt die Klasse (meldet die Testfunktionen an). */
    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        event.registerTest(STORM_TRIDENT.getId(), new FunctionGameTestInstance(STORM_TRIDENT.key(),
                new TestData<>(environment, Level.OVERWORLD, ARENA, 300, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        for (var test : List.of(SEAL, ATTACKS)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, TOWN, 1400, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
    }

    /** Der Boden aus Stein (wie bei den Tests des Raubritters). */
    private static void floor(GameTestHelper helper) {
        for (int x = 0; x < 48; x++) {
            for (int z = 0; z < 48; z++) {
                helper.setBlock(x, 0, z, Blocks.STONE);
            }
        }
    }

    private static void cleanup(ServerLevel level, AABB area) {
        for (Entity e : level.getEntitiesOfClass(Entity.class, area, e -> !(e instanceof ServerPlayer))) {
            e.discard();
        }
    }

    /** Versetzt den Spieler und meldet ihn an der neuen Stelle an (wie es sonst seine Verbindung jeden Tick tut). */
    private static void move(ServerPlayer player, double x, double y, double z, float yaw) {
        player.teleportTo(player.level(), x, y, z, java.util.Set.of(), yaw, 0.0f, true);
        player.level().getChunkSource().move(player);
    }

    /** Führt {@code body} aus; scheitert es, wird vorher aufgeräumt (ein liegen gebliebener Ventor stürzte sich sonst auf Nachbarn). */
    private static void tidyOnFailure(Runnable tidy, Runnable body) {
        try {
            body.run();
        } catch (RuntimeException e) {
            tidy.run();
            throw e;
        }
    }

    private static String fmt(double d) {
        return String.format(Locale.ROOT, "%.1f", d);
    }

    // =====================================================================================
    // Das Arenasiegel
    // =====================================================================================

    /**
     * Ein Käfig ist bezwungen: Sein Prüfungsspawner kühlt ab ({@code COOLDOWN}) oder wirft gerade seine Belohnung aus. Damit der
     * Zustand bleibt (ohne Spieler und Kämpfer ginge er sonst sofort weiter), endet seine Abkühlung erst in einer Stunde.
     */
    private static void won(ServerLevel level, BlockPos cage, TrialSpawnerState state) {
        level.setBlock(cage, level.getBlockState(cage).setValue(TrialSpawnerBlock.STATE, state), Block.UPDATE_ALL);
        if (level.getBlockEntity(cage) instanceof TrialSpawnerBlockEntity spawner) {
            CompoundTag tag = new CompoundTag();
            tag.putLong("cooldown_ends_at", level.getGameTime() + 72_000L);
            spawner.loadCustomOnly(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), tag));
        }
    }

    /**
     * Das Arenasiegel: unzerstörbar; es findet die Käfige ringsum (nicht den Spawner der Böen über sich). Käfige, die schon vor
     * dem ersten Kampf abkühlen, zählen, ebenso einer, der gerade seine Belohnung auswirft; sind alle bezwungen, ist der
     * Meister bereit und steigt auf, sobald jemand die Arena betritt (beim Aufsteigen unverwundbar). Findet er niemanden mehr,
     * sinkt er zurück – das Siegel ist wieder bereit. Besiegt, lässt er Netze, Böenruten und Windkugeln fallen; danach zählt
     * ein abkühlender Käfig nicht mehr, nur einer, der erneut bezwungen wird.
     */
    private static void seal(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper);
        helper.assertTrue(ModEntities.VENTOR.get().getCategory() == MobCategory.MONSTER, "Ventor: falsche Gruppe");
        helper.setBlock(24, 1, 24, Blocks.SMOOTH_SANDSTONE);
        BlockPos pos = helper.absolutePos(new BlockPos(24, 2, 24));
        level.setBlock(pos, ArenaGear.ARENA_SEAL.get().defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(pos.above(), Blocks.TRIAL_SPAWNER.defaultBlockState(), Block.UPDATE_ALL);
        List<BlockPos> cages = List.of(helper.absolutePos(new BlockPos(14, 1, 24)), helper.absolutePos(new BlockPos(34, 1, 24)),
                helper.absolutePos(new BlockPos(24, 1, 34)));
        for (BlockPos cage : cages) {
            level.setBlock(cage, Blocks.TRIAL_SPAWNER.defaultBlockState(), Block.UPDATE_ALL);
        }
        if (!(level.getBlockEntity(pos) instanceof ArenaSealBlockEntity seal)) {
            throw new IllegalStateException("Das Arenasiegel hat keine Blockentität");
        }
        helper.assertTrue(level.getBlockState(pos).getDestroySpeed(level, pos) < 0, "Das Arenasiegel lässt sich abbauen");
        seal.scan(level);
        helper.assertTrue(seal.cages().size() == 3, "Das Siegel findet nicht drei Käfige: " + seal.cages().size());
        helper.assertFalse(seal.cages().contains(pos.above()), "Der Spawner der Böen gilt als Käfig");
        helper.assertTrue(seal.phase() == ArenaSealBlock.Phase.DORMANT && seal.conqueredCount() == 0, "Das Siegel schläft nicht");
        AABB area = new AABB(pos).inflate(30, 12, 30);
        ServerPlayer player = MageGameTests.survivalPlayer(helper, "test-ventor-seal", new io.netty.channel.embedded.EmbeddedChannel[1], 0);
        Runnable tidy = () -> {
            level.getServer().getPlayerList().remove(player);
            cleanup(level, area);
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        };
        // (der Spieler wartet vor der Arena)
        move(player, pos.getX() + 0.5, pos.getY() - 1.0, pos.getZ() + 20.5, 180.0f);
        won(level, cages.get(0), TrialSpawnerState.COOLDOWN);
        won(level, cages.get(1), TrialSpawnerState.COOLDOWN);
        helper.runAfterDelay(12, () -> tidyOnFailure(tidy, () -> {
            helper.assertTrue(seal.conqueredCount() == 2, "Zwei abkühlende Käfige zählen nicht: " + seal.conqueredCount());
            helper.assertTrue(seal.phase() == ArenaSealBlock.Phase.DORMANT, "Mit zwei von drei Käfigen ist der Meister schon bereit");
            won(level, cages.get(2), TrialSpawnerState.WAITING_FOR_REWARD_EJECTION);
            helper.runAfterDelay(12, () -> tidyOnFailure(tidy, () -> {
                helper.assertTrue(seal.allConquered(), "Der dritte Käfig zählt nicht: " + seal.conqueredCount());
                helper.assertTrue(seal.phase() == ArenaSealBlock.Phase.READY
                        && level.getBlockState(pos).getValue(ArenaSealBlock.PHASE) == ArenaSealBlock.Phase.READY, "Der Meister ist nicht bereit");
                helper.assertTrue(seal.ventor(level) == null, "Der Meister steigt auf, ohne dass jemand in der Arena ist");
                // jemand betritt die Arena
                move(player, pos.getX() + 4.5, pos.getY() - 1.0, pos.getZ() + 0.5, 90.0f);
                helper.runAfterDelay(12, () -> tidyOnFailure(tidy, () -> rise(helper, level, pos, seal, cages, player, area, tidy)));
            }));
        }));
    }

    private static void rise(GameTestHelper helper, ServerLevel level, BlockPos pos, ArenaSealBlockEntity seal, List<BlockPos> cages,
            ServerPlayer player, AABB area, Runnable tidy) {
        VentorEntity ventor = seal.ventor(level);
        helper.assertTrue(ventor != null, "Der Meister steigt nicht auf, als jemand die Arena betritt");
        ventor.confine(14.0);
        helper.assertTrue(ventor.currentAttack() == VentorEntity.Attack.AWAKEN, "Er steigt nicht auf: " + ventor.currentAttack());
        helper.assertTrue(seal.phase() == ArenaSealBlock.Phase.AWAKE, "Das Siegel merkt das Aufsteigen nicht");
        helper.assertTrue(pos.above(2).equals(ventor.home()) && pos.equals(ventor.seal()), "Er kennt seinen Platz nicht: " + ventor.home());
        float hp = ventor.getHealth();
        ventor.hurtServer(level, level.damageSources().magic(), 50.0f);
        helper.assertTrue(ventor.getHealth() >= hp, "Beim Aufsteigen ist er verwundbar");
        // niemand mehr da: er sinkt zurück, das Siegel ist wieder bereit
        level.getServer().getPlayerList().remove(player);
        ventor.forceAttack(VentorEntity.Attack.RETREAT);
        helper.runAfterDelay(VentorEntity.RETREAT_TICKS + 8, () -> tidyOnFailure(tidy, () -> {
            helper.assertTrue(ventor.isRemoved(), "Er sinkt nicht zurück");
            helper.assertTrue(seal.phase() == ArenaSealBlock.Phase.READY, "Das Siegel ist danach nicht wieder bereit: " + seal.phase());
            VentorEntity again = seal.summon(level);
            helper.assertTrue(again != null && seal.phase() == ArenaSealBlock.Phase.AWAKE, "Er steigt nicht wieder auf");
            again.confine(14.0);
            helper.runAfterDelay(VentorEntity.AWAKEN_TICKS + 4, () -> tidyOnFailure(tidy, () -> {
                helper.assertTrue(again.currentAttack() != VentorEntity.Attack.AWAKEN, "Er steigt nicht ganz auf");
                again.setInvulnerableTime(0);
                again.damageCooldownTime = 0;
                float before = again.getHealth();
                again.hurtServer(level, level.damageSources().magic(), 20.0f);
                helper.assertTrue(before - again.getHealth() > 19.0f, "Aufgestiegen ist er unverwundbar: " + (before - again.getHealth()));
                again.setInvulnerableTime(0);
                again.damageCooldownTime = 0;
                again.setHealth(1.0f);
                BossLootGameTests.firstVictory(helper, again, 50.0f);
                helper.assertTrue(again.isDeadOrDying(), "Ventor fällt nicht");
                helper.assertTrue(seal.phase() == ArenaSealBlock.Phase.CONQUERED && seal.conqueredCount() == 0,
                        "Das Siegel merkt den Sieg nicht: " + seal.phase() + ", " + seal.conqueredCount());
                helper.runAfterDelay(VentorEntity.DEATH_TICKS + 8, () -> tidyOnFailure(tidy, () -> {
                    helper.assertTrue(again.isRemoved(), "Er verweht nicht");
                    int nets = level.getEntitiesOfClass(ItemEntity.class, area, e -> e.getItem().is(ArenaGear.GLADIATOR_NET.get())).stream()
                            .mapToInt(e -> e.getItem().getCount()).sum();
                    int rods = level.getEntitiesOfClass(ItemEntity.class, area, e -> e.getItem().is(Items.BREEZE_ROD)).stream()
                            .mapToInt(e -> e.getItem().getCount()).sum();
                    helper.assertTrue(nets >= 1 && nets <= 3 && rods >= 1 && rods <= 3, "Seine Beute stimmt nicht: " + nets + " Netze, " + rods
                            + " Böenruten");
                    // nach seinem Fall zählen die alten Siege nicht mehr (auch nicht der Käfig, der noch Belohnung auswirft) – nur
                    // einer, dessen Prüfung neu beginnt und der erneut bezwungen wird
                    helper.runAfterDelay(12, () -> tidyOnFailure(tidy, () -> {
                        helper.assertTrue(seal.conqueredCount() == 0, "Nach dem Kampf zählen die alten Siege noch: " + seal.conqueredCount());
                        level.setBlock(cages.get(1), level.getBlockState(cages.get(1)).setValue(TrialSpawnerBlock.STATE,
                                TrialSpawnerState.WAITING_FOR_PLAYERS), Block.UPDATE_ALL);
                        helper.runAfterDelay(12, () -> tidyOnFailure(tidy, () -> {
                            won(level, cages.get(1), TrialSpawnerState.WAITING_FOR_REWARD_EJECTION);
                            helper.runAfterDelay(12, () -> tidyOnFailure(tidy, () -> {
                                helper.assertTrue(seal.conqueredCount() == 1 && seal.phase() == ArenaSealBlock.Phase.CONQUERED,
                                        "Ein neuer Sieg zählt nicht: " + seal.conqueredCount() + ", " + seal.phase());
                                tidy.run();
                                helper.succeed();
                            }));
                        }));
                    }));
                }));
            }));
        }));
    }

    // =====================================================================================
    // Die Angriffe
    // =====================================================================================

    /**
     * Seine Angriffe gegen einen (zähen) Ritter: Böe (Geschosse prallen ab, solange sie weht), Dreizackstoß, Netzwurf (der Ritter
     * steckt fest, und der Stoß trifft ihn härter), Dreizackwurf (der Dreizack kehrt zurück), Windkugeln, Windhosen, Sturzflug
     * (der Kreis liegt um das Ziel), der entfesselte Sturm unter halber Kraft (dabei unverwundbar) und der Zyklon.
     */
    private static void attacks(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper);
        VentorEntity ventor = helper.spawn(ModEntities.VENTOR.get(), new BlockPos(14, 1, 24));
        ventor.confine(16.0);
        LivingEntity knight = helper.spawnWithNoFreeWill(ModEntities.SOLDIERS.get(SoldierType.KNIGHT).get(), new BlockPos(20, 1, 24));
        knight.getAttribute(Attributes.MAX_HEALTH).setBaseValue(600.0);
        knight.setHealth(600.0f);
        AABB area = new AABB(helper.absolutePos(new BlockPos(24, 1, 24))).inflate(30, 16, 30);
        Runnable tidy = () -> cleanup(level, area);
        Vec3 spot = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(14, 1, 24)));
        float[] hp = {knight.getHealth()};
        float[] normal = {0.0f};
        gust(helper, level, ventor, knight, spot, hp, tidy, () ->
                step(helper, ventor, knight, spot, 3.4, hp, VentorEntity.Attack.THRUST, VentorEntity.THRUST_HIT + 4, "Der Dreizackstoß", tidy, damage -> {
                    normal[0] = damage;
                    net(helper, level, ventor, knight, spot, hp, normal[0], tidy, () ->
                            toss(helper, level, ventor, knight, spot, hp, area, tidy, () ->
                                    step(helper, ventor, knight, spot, 8.0, hp, VentorEntity.Attack.CHARGES, VentorEntity.CHARGE_SHOTS[2] + 10,
                                            "Die Windkugeln", tidy, d -> vortex(helper, level, ventor, knight, spot, hp, area, tidy, () ->
                                                    dive(helper, level, ventor, knight, spot, hp, tidy, () ->
                                                            rage(helper, level, ventor, knight, spot, hp, tidy))))));
                }));
    }

    /** Stellt beide an ihre Plätze (der Ritter {@code distance} östlich von Ventor) und beginnt den Angriff. */
    private static void place(VentorEntity ventor, LivingEntity knight, Vec3 spot, double distance, float[] hp, VentorEntity.Attack attack) {
        ventor.forceAttack(VentorEntity.Attack.NONE);
        // was vom letzten Angriff (oder einem, den er zwischendurch selbst begann) noch umherzieht, ist fort
        for (Entity e : ventor.level().getEntitiesOfClass(Entity.class, ventor.getBoundingBox().inflate(32.0),
                e -> e instanceof WindVortexEntity || e instanceof GladiatorNetEntity || e instanceof StormTridentEntity)) {
            e.discard();
        }
        ventor.setNoGravity(false);
        ventor.teleportTo(spot.x, spot.y, spot.z);
        ventor.setDeltaMovement(Vec3.ZERO);
        ventor.catchTrident();
        knight.teleportTo(spot.x + distance, spot.y, spot.z);
        knight.setHealth(knight.getMaxHealth());
        knight.setInvulnerableTime(0);
        knight.damageCooldownTime = 0;
        knight.setDeltaMovement(Vec3.ZERO);
        knight.resetFallDistance();
        knight.removeAllEffects();
        hp[0] = knight.getHealth();
        ventor.setTarget(knight);
        ventor.lookAt(knight, 360.0f, 30.0f);
        ventor.setYBodyRot(ventor.getYRot());
        ventor.forceAttack(attack);
    }

    /** Ein Angriff: an die Plätze, erzwingen, warten, prüfen, ob der Ritter getroffen wurde (und wie schwer), dann der nächste. */
    private static void step(GameTestHelper helper, VentorEntity ventor, LivingEntity knight, Vec3 spot, double distance, float[] hp,
            VentorEntity.Attack attack, int ticks, String name, Runnable tidy, Consumer<Float> then) {
        place(ventor, knight, spot, distance, hp, attack);
        helper.runAfterDelay(ticks, () -> tidyOnFailure(tidy, () -> {
            helper.assertTrue(knight.getHealth() < hp[0], name + " trifft nicht (" + ventor.currentAttack() + ", Abstand "
                    + fmt(ventor.distanceTo(knight)) + ")");
            then.accept(hp[0] - knight.getHealth());
        }));
    }

    /** Die Böe: Solange sie weht, prallen Pfeile an ihm ab (Windkugeln nicht); dann trifft sie den Ritter. */
    private static void gust(GameTestHelper helper, ServerLevel level, VentorEntity ventor, LivingEntity knight, Vec3 spot, float[] hp, Runnable tidy,
            Runnable then) {
        place(ventor, knight, spot, 4.0, hp, VentorEntity.Attack.GUST);
        helper.assertTrue(ventor.deflection(new Arrow(EntityTypes.ARROW, level)) == ProjectileDeflection.NONE, "Pfeile prallen schon vor der Böe ab");
        helper.runAfterDelay(VentorEntity.GUST_HIT - 3, () -> tidyOnFailure(tidy, () -> {
            helper.assertTrue(ventor.deflection(new Arrow(EntityTypes.ARROW, level)) != ProjectileDeflection.NONE, "Pfeile prallen an der Böe nicht ab");
            helper.assertTrue(ventor.deflection(new WindCharge(EntityTypes.WIND_CHARGE, level)) == ProjectileDeflection.NONE,
                    "Windkugeln prallen an der Böe ab");
        }));
        helper.runAfterDelay(VentorEntity.GUST_HIT + 3, () -> tidyOnFailure(tidy, () -> {
            helper.assertTrue(knight.getHealth() < hp[0], "Die Böe trifft nicht (" + ventor.currentAttack() + ", Abstand " + fmt(ventor.distanceTo(knight))
                    + ")");
            then.run();
        }));
    }

    /** Der Netzwurf: Der Ritter steckt im Netz (Ventor hat keines mehr in der Hand); dann trifft ihn der Stoß härter. */
    private static void net(GameTestHelper helper, ServerLevel level, VentorEntity ventor, LivingEntity knight, Vec3 spot, float[] hp, float normal,
            Runnable tidy, Runnable then) {
        place(ventor, knight, spot, 7.0, hp, VentorEntity.Attack.NET);
        helper.runAfterDelay(VentorEntity.NET_RELEASE + 14, () -> tidyOnFailure(tidy, () -> {
            helper.assertFalse(ventor.hasNet(), "Er hält das Netz noch in der Hand");
            helper.assertTrue(GladiatorNetBlock.entangled(knight), "Der Ritter steckt nicht im Netz (Abstand " + fmt(ventor.distanceTo(knight)) + ")");
            helper.assertTrue(knight.getHealth() < hp[0], "Das Netz trifft nicht");
            // der Stoß auf den Gefangenen (Ventor rückt heran, der Ritter bleibt im Netz)
            ventor.forceAttack(VentorEntity.Attack.NONE);
            Vec3 k = knight.position();
            ventor.teleportTo(k.x - 3.4, k.y, k.z);
            ventor.setDeltaMovement(Vec3.ZERO);
            float before = knight.getHealth();
            knight.setInvulnerableTime(0);
            knight.damageCooldownTime = 0;
            ventor.setTarget(knight);
            ventor.lookAt(knight, 360.0f, 30.0f);
            ventor.forceAttack(VentorEntity.Attack.THRUST);
            helper.runAfterDelay(VentorEntity.THRUST_HIT + 4, () -> tidyOnFailure(tidy, () -> {
                float damage = before - knight.getHealth();
                helper.assertTrue(damage > normal * 1.2f, "Wer im Netz steckt, den trifft der Stoß nicht härter: " + fmt(damage) + " statt " + fmt(normal));
                then.run();
            }));
        }));
    }

    /** Der Dreizackwurf: Der Dreizack fliegt (Ventor hat ihn nicht mehr), trifft und kehrt in seine Hand zurück. */
    private static void toss(GameTestHelper helper, ServerLevel level, VentorEntity ventor, LivingEntity knight, Vec3 spot, float[] hp, AABB area,
            Runnable tidy, Runnable then) {
        place(ventor, knight, spot, 9.0, hp, VentorEntity.Attack.THROW);
        helper.runAfterDelay(VentorEntity.THROW_RELEASE + 2, () -> tidyOnFailure(tidy, () -> {
            helper.assertFalse(ventor.hasTrident(), "Er hält den Dreizack noch in der Hand");
            helper.assertTrue(!level.getEntitiesOfClass(StormTridentEntity.class, area).isEmpty(), "Kein Dreizack fliegt");
        }));
        helper.runAfterDelay(VentorEntity.THROW_RELEASE + 14, () -> tidyOnFailure(tidy, () -> {
            helper.assertTrue(knight.getHealth() < hp[0], "Der Dreizackwurf trifft nicht (Abstand " + fmt(ventor.distanceTo(knight)) + ")");
            ventor.forceAttack(VentorEntity.Attack.NONE);       // (er hält inne, bis der Dreizack zurück ist)
        }));
        helper.runAfterDelay(VentorEntity.THROW_RELEASE + 50, () -> tidyOnFailure(tidy, () -> {
            helper.assertTrue(ventor.hasTrident(), "Der Dreizack kehrt nicht in seine Hand zurück");
            helper.assertTrue(level.getEntitiesOfClass(StormTridentEntity.class, area).isEmpty(), "Der Dreizack fliegt noch");
            then.run();
        }));
    }

    /** Die Windhosen: drei erheben sich rings um den Ritter. */
    private static void vortex(GameTestHelper helper, ServerLevel level, VentorEntity ventor, LivingEntity knight, Vec3 spot, float[] hp, AABB area,
            Runnable tidy, Runnable then) {
        place(ventor, knight, spot, 6.0, hp, VentorEntity.Attack.VORTEX);
        helper.runAfterDelay(VentorEntity.VORTEX_SPAWN + 2, () -> tidyOnFailure(tidy, () -> {
            helper.assertTrue(ventor.vorticesAlive(level) == 3, "Nicht drei Windhosen: " + ventor.vorticesAlive(level));
            List<WindVortexEntity> vortices = level.getEntitiesOfClass(WindVortexEntity.class, area);
            helper.assertTrue(vortices.size() == 3, "Die Windhosen sind nicht in der Welt: " + vortices.size());
            for (WindVortexEntity v : vortices) {
                helper.assertTrue(v.distanceTo(knight) < 8.0, "Eine Windhose erhebt sich fern vom Ritter: " + fmt(v.distanceTo(knight)));
                v.discard();
            }
            then.run();
        }));
    }

    /** Der Sturzflug: hinauf, der Kreis liegt um den Ritter, hinab – der Aufschlag trifft ihn. */
    private static void dive(GameTestHelper helper, ServerLevel level, VentorEntity ventor, LivingEntity knight, Vec3 spot, float[] hp, Runnable tidy,
            Runnable then) {
        place(ventor, knight, spot, 7.0, hp, VentorEntity.Attack.DIVE);
        double ground = ventor.getY();
        helper.runAfterDelay(VentorEntity.DIVE_HOVER + 2, () -> tidyOnFailure(tidy, () ->
                helper.assertTrue(ventor.getY() - ground > 4.0, "Er steigt nicht auf: " + fmt(ventor.getY() - ground))));
        helper.runAfterDelay(VentorEntity.DIVE_LOCK + 1, () -> tidyOnFailure(tidy, () -> {
            Vec3 mark = ventor.diveMark();
            double off = Math.hypot(mark.x - knight.getX(), mark.z - knight.getZ());
            helper.assertTrue(off < 1.0, "Der Kreis liegt nicht um den Ritter: " + fmt(off));
        }));
        helper.runAfterDelay(VentorEntity.DIVE_FALL + 18, () -> tidyOnFailure(tidy, () -> {
            helper.assertTrue(ventor.getY() - ground < 1.0, "Er landet nicht: " + fmt(ventor.getY() - ground));
            helper.assertTrue(knight.getHealth() < hp[0], "Der Sturzflug trifft nicht (Abstand " + fmt(ventor.distanceTo(knight)) + ")");
            then.run();
        }));
    }

    /** Unter halber Kraft entfesselt er den Sturm (dabei ist er unverwundbar), danach der Zyklon (Pfeile prallen ab). */
    private static void rage(GameTestHelper helper, ServerLevel level, VentorEntity ventor, LivingEntity knight, Vec3 spot, float[] hp, Runnable tidy) {
        place(ventor, knight, spot, 9.0, hp, VentorEntity.Attack.NONE);
        ventor.setEnraged(false);
        ventor.setHealth(VentorEntity.MAX_HEALTH * 0.45f);
        helper.runAfterDelay(3, () -> tidyOnFailure(tidy, () -> {
            helper.assertTrue(ventor.currentAttack() == VentorEntity.Attack.RAGE && ventor.isEnraged(), "Unter halber Kraft entfesselt er den Sturm nicht: "
                    + ventor.currentAttack());
            float before = ventor.getHealth();
            ventor.setInvulnerableTime(0);
            ventor.damageCooldownTime = 0;
            ventor.hurtServer(level, level.damageSources().magic(), 30.0f);
            helper.assertTrue(ventor.getHealth() >= before, "Während er den Sturm entfesselt, ist er verwundbar");
            helper.runAfterDelay(VentorEntity.RAGE_TICKS + 2, () -> tidyOnFailure(tidy, () -> {
                helper.assertTrue(ventor.currentAttack() != VentorEntity.Attack.RAGE && ventor.isEnraged(), "Der Sturm endet nicht oder flaut ab");
                place(ventor, knight, spot, 3.8, hp, VentorEntity.Attack.CYCLONE);
                helper.runAfterDelay(VentorEntity.CYCLONE_FROM + 4, () -> tidyOnFailure(tidy, () ->
                        helper.assertTrue(ventor.deflection(new Arrow(EntityTypes.ARROW, level)) != ProjectileDeflection.NONE,
                                "Pfeile prallen am Zyklon nicht ab")));
                helper.runAfterDelay(VentorEntity.CYCLONE_FROM + 24, () -> tidyOnFailure(tidy, () -> {
                    helper.assertTrue(knight.getHealth() < hp[0], "Der Zyklon trifft nicht (Abstand " + fmt(ventor.distanceTo(knight)) + ")");
                    tidy.run();
                    helper.succeed();
                }));
            }));
        }));
    }

    // =====================================================================================
    // Sturmdreizack und Wurfnetz
    // =====================================================================================

    /**
     * Der Sturmdreizack: schwer, haltbar, mit Böenruten auszubessern, verzauberbar; geworfen trifft er, entfesselt eine Böe (die
     * auch den Nachbarn trifft) und kehrt zum Werfer zurück. Das Wurfnetz: geworfen fängt es, wen es trifft.
     */
    private static void stormTrident(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ItemStack trident = new ItemStack(ArenaGear.STORM_TRIDENT.get());
        double[] damage = new double[1];
        trident.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY).forEach(EquipmentSlot.MAINHAND, (attribute, modifier) -> {
            if (attribute.is(Attributes.ATTACK_DAMAGE)) {
                damage[0] += modifier.amount();
            }
        });
        helper.assertTrue(damage[0] >= 9.0, "Der Sturmdreizack trifft zu schwach: " + damage[0]);
        helper.assertTrue(trident.getMaxDamage() >= 1000, "Der Sturmdreizack hält zu wenig aus: " + trident.getMaxDamage());
        helper.assertTrue(trident.isValidRepairItem(new ItemStack(Items.BREEZE_ROD)), "Nicht mit Böenruten auszubessern");
        helper.assertTrue(trident.is(ItemTags.SHARP_WEAPON_ENCHANTABLE) && trident.is(ItemTags.DURABILITY_ENCHANTABLE), "Nicht verzauberbar");
        helper.assertTrue(trident.getRarity() == Rarity.EPIC, "Der Sturmdreizack ist nicht episch");

        BlockPos base = helper.absolutePos(new BlockPos(4, 200, 4));
        for (BlockPos p : BlockPos.betweenClosed(base.offset(-4, -1, -3), base.offset(4, -1, 10))) {
            level.setBlock(p, Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        ServerPlayer player = MageGameTests.survivalPlayer(helper, "test-storm-trident", new io.netty.channel.embedded.EmbeddedChannel[1], 0);
        Zombie front = ModEntities.ZOMBIE.get().create(level, EntitySpawnReason.EVENT);
        Zombie side = ModEntities.ZOMBIE.get().create(level, EntitySpawnReason.EVENT);
        Zombie netted = ModEntities.ZOMBIE.get().create(level, EntitySpawnReason.EVENT);
        if (front == null || side == null || netted == null) {
            throw new IllegalStateException("Kein Zombie");
        }
        AABB area = new AABB(base).inflate(12, 8, 14);
        Runnable tidy = () -> {
            level.getServer().getPlayerList().remove(player);
            for (Entity e : level.getEntitiesOfClass(Entity.class, area, e -> !(e instanceof ServerPlayer))) {
                e.discard();
            }
            for (BlockPos p : BlockPos.betweenClosed(base.offset(-4, -1, -3), base.offset(4, 3, 10))) {
                level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
        };
        float[] hp = new float[3];
        tidyOnFailure(tidy, () -> {
            player.connection.markClientLoaded();
            player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
            move(player, base.getX() + 0.5, base.getY(), base.getZ() + 0.5, 0.0f);       // Blick nach Süden
            for (Zombie z : List.of(front, side, netted)) {
                z.setNoAi(true);
            }
            front.snapTo(base.getX() + 0.5, base.getY(), base.getZ() + 6.5, 180.0f, 0.0f);
            side.snapTo(base.getX() + 2.2, base.getY(), base.getZ() + 6.5, 180.0f, 0.0f);
            level.addFreshEntity(front);
            level.addFreshEntity(side);
            hp[0] = front.getHealth();
            hp[1] = side.getHealth();
            player.setItemInHand(InteractionHand.MAIN_HAND, trident);
            trident.use(level, player, InteractionHand.MAIN_HAND);
            trident.releaseUsing(level, player, trident.getUseDuration(player) - 15);
            helper.assertTrue(player.getMainHandItem().isEmpty(), "Der Sturmdreizack bleibt in der Hand");
            helper.assertTrue(!level.getEntitiesOfClass(StormTridentEntity.class, area).isEmpty(), "Kein Sturmdreizack fliegt");
        });
        helper.runAfterDelay(10, () -> tidyOnFailure(tidy, () -> {
            helper.assertTrue(hp[0] - front.getHealth() >= 6.0f, "Der Wurf trifft nicht hart genug: " + fmt(hp[0] - front.getHealth()));
            helper.assertTrue(side.getHealth() < hp[1], "Die Böe beim Aufschlag trifft den Nachbarn nicht");
            helper.assertTrue(player.getHealth() >= player.getMaxHealth(), "Die Böe trifft den Werfer");
        }));
        helper.runAfterDelay(40, () -> tidyOnFailure(tidy, () -> {
            List<StormTridentEntity> flying = level.getEntitiesOfClass(StormTridentEntity.class, area);
            helper.assertTrue(flying.size() == 1, "Der Sturmdreizack ist fort: " + flying.size());
            StormTridentEntity back = flying.getFirst();
            helper.assertTrue(back.isReturning() && back.distanceTo(player) < 3.0, "Er kehrt nicht zum Werfer zurück (Abstand "
                    + fmt(back.distanceTo(player)) + ")");
            back.playerTouch(player);
            helper.assertTrue(player.getInventory().contains(stack -> stack.is(ArenaGear.STORM_TRIDENT.get())), "Der Werfer fängt ihn nicht");
            // das Wurfnetz
            front.discard();
            side.discard();
            netted.snapTo(base.getX() + 0.5, base.getY(), base.getZ() + 5.5, 180.0f, 0.0f);
            level.addFreshEntity(netted);
            hp[2] = netted.getHealth();
            ItemStack nets = new ItemStack(ArenaGear.GLADIATOR_NET.get(), 3);
            player.setItemInHand(InteractionHand.MAIN_HAND, nets);
            move(player, base.getX() + 0.5, base.getY(), base.getZ() + 0.5, 0.0f);
            player.setXRot(4.0f);
            nets.use(level, player, InteractionHand.MAIN_HAND);
            helper.assertTrue(nets.getCount() == 2, "Das Netz wird nicht verbraucht: " + nets.getCount());
            helper.assertTrue(player.getCooldowns().isOnCooldown(nets) && GladiatorNetItem.COOLDOWN >= 20, "Netze lassen sich ohne Pause werfen");
            helper.assertTrue(!level.getEntitiesOfClass(GladiatorNetEntity.class, area).isEmpty(), "Kein Netz fliegt");
            helper.runAfterDelay(12, () -> tidyOnFailure(tidy, () -> {
                helper.assertTrue(GladiatorNetBlock.entangled(netted), "Das Wurfnetz fängt den Zombie nicht");
                helper.assertTrue(netted.hasEffect(net.minecraft.world.effect.MobEffects.SLOWNESS), "Wen das Netz fängt, der ist nicht langsam");
                tidy.run();
                helper.succeed();
            }));
        }));
    }
}
