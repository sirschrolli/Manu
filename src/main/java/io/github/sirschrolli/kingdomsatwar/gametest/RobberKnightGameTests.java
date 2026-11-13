package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.castle.RavenBannerBlock;
import io.github.sirschrolli.kingdomsatwar.block.castle.RavenBannerBlockEntity;
import io.github.sirschrolli.kingdomsatwar.entity.castle.RobberKnightEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.Warhorse;
import io.github.sirschrolli.kingdomsatwar.item.castle.MorningStarItem;
import io.github.sirschrolli.kingdomsatwar.registry.CastleGear;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import java.util.List;
import java.util.Locale;
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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Tests zu Kuno von Rabenfels, dem Raubritter (1.8): das Rabenbanner (er hält Hof, erwacht, wird abgeworfen, fällt, ein
 * schwarzes Banner ruft ihn zurück), seine Angriffe zu Ross und zu Fuß, die Lanze, die an einem Schild bricht, und der
 * Morgenstern.
 */
final class RobberKnightGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");
    private static final Identifier TOWN = KingdomsAtWar.id("test_town");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> BANNER =
            ModGameTests.TEST_FUNCTIONS.register("robber_knight_banner", () -> RobberKnightGameTests::banner);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ATTACKS =
            ModGameTests.TEST_FUNCTIONS.register("robber_knight_attacks", () -> RobberKnightGameTests::attacks);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SHIELD =
            ModGameTests.TEST_FUNCTIONS.register("robber_knight_shield", () -> RobberKnightGameTests::shield);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> MORNING_STAR =
            ModGameTests.TEST_FUNCTIONS.register("robber_knight_morning_star", () -> RobberKnightGameTests::morningStar);

    private RobberKnightGameTests() {}

    /** Lädt die Klasse (meldet die Testfunktionen an). */
    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        event.registerTest(MORNING_STAR.getId(), new FunctionGameTestInstance(MORNING_STAR.key(),
                new TestData<>(environment, Level.OVERWORLD, ARENA, 300, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        for (var test : List.of(BANNER, ATTACKS, SHIELD)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, TOWN, 1400, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
    }

    /**
     * Der Boden aus Stein (der große Testbereich bringt ihn zwar mit, doch steht er im ersten Tick noch nicht überall; Stein,
     * nicht Pflaster: Ein Nachbartest, dessen Phasenschritt hierher springt, erwartet Stein unter den Füßen).
     */
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

    private static RavenBannerBlockEntity placeBanner(ServerLevel level, BlockPos pos) {
        level.setBlock(pos, CastleGear.RAVEN_BANNER.get().defaultBlockState().setValue(RavenBannerBlock.FACING, Direction.SOUTH), Block.UPDATE_ALL);
        if (!(level.getBlockEntity(pos) instanceof RavenBannerBlockEntity banner)) {
            throw new IllegalStateException("Das Rabenbanner hat keine Blockentität");
        }
        return banner;
    }

    /** Führt {@code body} aus; scheitert es, wird vorher aufgeräumt (ein liegen gebliebenes Banner riefe sonst Ritter herbei). */
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
    // Das Rabenbanner
    // =====================================================================================

    /**
     * Das Rabenbanner: unzerstörbar; kommt jemand in die Nähe der Burg, hält der Ritter schon zu Ross unter dem Banner Hof
     * (unverwundbar, Blick zum Tor); tritt jemand näher heran, erwacht er. Unter halber Kraft wirft ihn das Ross ab (er ist
     * dabei kurz unverwundbar, das Ross flieht gesattelt). Besiegt reißt das Banner; er lässt Stahl fallen. Ein schwarzes
     * Banner am leeren Pfahl ruft ihn zurück.
     */
    private static void banner(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper);
        helper.assertTrue(ModEntities.ROBBER_KNIGHT.get().getCategory() == MobCategory.MONSTER, "Der Raubritter: falsche Gruppe");
        BlockPos pos = helper.absolutePos(new BlockPos(20, 1, 8));
        RavenBannerBlockEntity banner = placeBanner(level, pos);
        helper.assertTrue(level.getBlockState(pos).getDestroySpeed(level, pos) < 0, "Das Rabenbanner lässt sich abbauen");
        helper.assertTrue(banner.phase() == RavenBannerBlock.Phase.GUARDED, "Das Banner wartet nicht auf seinen Herrn");
        BlockPos home = banner.home();
        helper.assertTrue(home.equals(pos.south(2).east(2)), "Sein Platz liegt falsch: " + helper.relativePos(home));
        AABB area = new AABB(pos).inflate(24, 8, 24);
        ServerPlayer player = MageGameTests.survivalPlayer(helper, "test-robber-guest", new io.netty.channel.embedded.EmbeddedChannel[1], 0);
        Runnable tidy = () -> {
            level.getServer().getPlayerList().remove(player);
            cleanup(level, area.inflate(16));
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        };
        move(player, home.getX() + 0.5, home.getY(), home.getZ() + 30.0, 180.0f);
        helper.runAfterDelay(45, () -> tidyOnFailure(tidy, () -> {
            var found = level.getEntitiesOfClass(RobberKnightEntity.class, area);
            helper.assertTrue(found.size() == 1, "Kein (oder mehr als ein) Ritter unter dem Banner: " + found.size() + " (Banner " + banner.phase() + ")");
            RobberKnightEntity knight = found.getFirst();
            knight.confine(12.0);                              // (sonst ritte er gegen die Spieler der Nachbartests)
            helper.assertTrue(knight.isHoldingCourt() && knight.isMounted(), "Er hält nicht zu Ross Hof: " + knight.currentAttack());
            helper.assertTrue(banner.owns(knight), "Das Banner kennt seinen Herrn nicht");
            helper.assertTrue(knight.blockPosition().equals(home), "Er steht nicht an seinem Platz: " + helper.relativePos(knight.blockPosition()));
            helper.assertTrue(Math.abs(net.minecraft.util.Mth.wrapDegrees(knight.getYRot())) < 5, "Er blickt nicht zum Tor: " + knight.getYRot());
            float hp = knight.getHealth();
            knight.hurtServer(level, level.damageSources().magic(), 50.0f);
            helper.assertTrue(knight.getHealth() >= hp && knight.isHoldingCourt(), "Hof haltend ist er verwundbar");
            // jemand tritt heran: er erwacht
            move(player, home.getX() + 0.5, home.getY(), home.getZ() + 8.0, 180.0f);
            helper.runAfterDelay(14, () -> tidyOnFailure(tidy, () -> {
                helper.assertTrue(knight.currentAttack() == RobberKnightEntity.Attack.AWAKEN, "Er erwacht nicht: " + knight.currentAttack());
                helper.assertTrue(banner.phase() == RavenBannerBlock.Phase.AWAKE, "Das Banner merkt das Erwachen nicht");
                helper.assertTrue(level.getBlockState(pos).getValue(RavenBannerBlock.PHASE) == RavenBannerBlock.Phase.AWAKE,
                        "Das Banner zeigt das Erwachen nicht");
                float before = knight.getHealth();
                knight.setInvulnerableTime(0);
                knight.hurtServer(level, level.damageSources().magic(), 50.0f);
                helper.assertTrue(knight.getHealth() >= before, "Beim Erwachen ist er verwundbar");
                level.getServer().getPlayerList().remove(player);
                helper.runAfterDelay(RobberKnightEntity.AWAKEN_TICKS + 4, () -> tidyOnFailure(tidy, () -> fight(helper, level, pos, banner, knight, area, tidy)));
            }));
        }));
    }

    private static void fight(GameTestHelper helper, ServerLevel level, BlockPos pos, RavenBannerBlockEntity banner, RobberKnightEntity knight,
            AABB area, Runnable tidy) {
        helper.assertTrue(knight.currentAttack() != RobberKnightEntity.Attack.AWAKEN && !knight.isHoldingCourt(), "Er erwacht nicht ganz: "
                + knight.currentAttack());
        // wach ist er verwundbar (Zauber gehen durch seine Rüstung: so lässt sich der Schaden genau messen)
        knight.setInvulnerableTime(0);
        knight.damageCooldownTime = 0;
        float hp = knight.getHealth();
        knight.hurtServer(level, level.damageSources().magic(), 20.0f);
        helper.assertTrue(hp - knight.getHealth() > 19.0f, "Wach ist er unverwundbar: " + (hp - knight.getHealth()));
        // unter halber Kraft wirft ihn das Ross ab
        knight.setHealth(knight.getMaxHealth() * 0.45f);
        helper.runAfterDelay(4, () -> tidyOnFailure(tidy, () -> {
            helper.assertTrue(knight.currentAttack() == RobberKnightEntity.Attack.UNHORSE, "Unter halber Kraft wirft ihn das Ross nicht ab: "
                    + knight.currentAttack());
            float before = knight.getHealth();
            knight.setInvulnerableTime(0);
            knight.damageCooldownTime = 0;
            knight.hurtServer(level, level.damageSources().magic(), 30.0f);
            helper.assertTrue(knight.getHealth() >= before, "Während er stürzt, ist er verwundbar");
            helper.runAfterDelay(RobberKnightEntity.UNHORSE_FALL + 2, () -> tidyOnFailure(tidy, () -> {
                helper.assertFalse(knight.isMounted(), "Er sitzt noch im Sattel");
                helper.assertTrue(knight.getBbHeight() < 3.0f, "Zu Fuß ist er so groß wie zu Ross: " + knight.getBbHeight());
                var horses = level.getEntitiesOfClass(Warhorse.class, area.inflate(16), h -> h.entityTags().contains("kaw_robber_horse"));
                helper.assertTrue(horses.size() == 1, "Kein Ross, das flieht: " + horses.size());
                helper.assertTrue(horses.getFirst().getItemBySlot(EquipmentSlot.SADDLE).is(Items.SADDLE), "Das Ross ist nicht gesattelt");
                horses.getFirst().discard();                      // (es liefe sonst in die Nachbartests davon)
                helper.runAfterDelay(RobberKnightEntity.Attack.UNHORSE.duration, () -> tidyOnFailure(tidy, () -> {
                    helper.assertTrue(knight.currentAttack() != RobberKnightEntity.Attack.UNHORSE, "Er rafft sich nicht auf");
                    knight.setInvulnerableTime(0);
                    knight.damageCooldownTime = 0;
                    knight.setHealth(1.0f);
                    BossLootGameTests.firstVictory(helper, knight, 50.0f);
                    helper.assertTrue(knight.isDeadOrDying(), "Der Raubritter fällt nicht");
                    helper.assertTrue(banner.phase() == RavenBannerBlock.Phase.FALLEN, "Das Banner merkt den Sieg nicht");
                    helper.assertTrue(level.getBlockState(pos).getValue(RavenBannerBlock.PHASE) == RavenBannerBlock.Phase.FALLEN,
                            "Das Banner reißt nicht");
                    helper.runAfterDelay(RobberKnightEntity.DEATH_TICKS + 5, () -> tidyOnFailure(tidy, () -> {
                        helper.assertTrue(knight.isRemoved(), "Er zerfällt nicht");
                        int steel = level.getEntitiesOfClass(ItemEntity.class, area, e -> e.getItem().is(ModItems.STEEL_INGOT.get())).stream()
                                .mapToInt(e -> e.getItem().getCount()).sum();
                        helper.assertTrue(steel >= 1 && steel <= 3, "Er lässt nicht ein bis drei Stahlbarren fallen: " + steel);
                        // ein schwarzes Banner am leeren Pfahl ruft ihn zurück
                        helper.assertTrue(banner.raise(level, null), "Das schwarze Banner ruft ihn nicht zurück");
                        var again = level.getEntitiesOfClass(RobberKnightEntity.class, area, LivingEntity::isAlive);
                        helper.assertTrue(again.size() == 1 && again.getFirst().currentAttack() == RobberKnightEntity.Attack.AWAKEN
                                && again.getFirst().isMounted(), "Der Raubritter kehrt nicht (zu Ross) zurück");
                        helper.assertTrue(level.getBlockState(pos).getValue(RavenBannerBlock.PHASE) == RavenBannerBlock.Phase.AWAKE,
                                "Das Banner weht nicht wieder");
                        helper.assertFalse(banner.raise(level, null), "Das Banner lässt sich zweimal hissen");
                        tidy.run();
                        helper.succeed();
                    }));
                }));
            }));
        }));
    }

    // =====================================================================================
    // Die Angriffe
    // =====================================================================================

    /**
     * Seine Angriffe gegen einen Ritter: zu Ross der Lanzenstich, das Steigen, der Bolzenhagel, der Hornruf (seine Leute
     * kommen) und der Lanzengang; zu Fuß der Morgensternschlag, der Wirbel, der Schildstoß und der Schildwall (von vorn trifft
     * ihn nichts, von hinten schon).
     */
    private static void attacks(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper);
        RobberKnightEntity knight = helper.spawn(ModEntities.ROBBER_KNIGHT.get(), new BlockPos(24, 1, 10));
        knight.confine(18.0);
        LivingEntity soldier = helper.spawnWithNoFreeWill(ModEntities.SOLDIERS.get(SoldierType.KNIGHT).get(), new BlockPos(24, 1, 14));
        AABB area = new AABB(helper.absolutePos(new BlockPos(24, 1, 20))).inflate(30, 12, 30);
        Runnable tidy = () -> cleanup(level, area);
        Vec3 near = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(24, 1, 14)));
        Vec3 far = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(24, 1, 26)));
        float[] hp = {soldier.getHealth()};
        step(helper, knight, soldier, near, hp, RobberKnightEntity.Attack.THRUST, RobberKnightEntity.THRUST_HIT + 3, "Der Lanzenstich", tidy, () ->
                step(helper, knight, soldier, near, hp, RobberKnightEntity.Attack.TRAMPLE, RobberKnightEntity.TRAMPLE_HIT + 3, "Das Steigen", tidy, () ->
                        volley(helper, knight, soldier, far, hp, tidy, () ->
                                horn(helper, level, knight, soldier, near, hp, area, tidy, () ->
                                        step(helper, knight, soldier, far, hp, RobberKnightEntity.Attack.CHARGE,
                                                RobberKnightEntity.CHARGE_AIM + 30, "Der Lanzengang", tidy, () ->
                                                onFoot(helper, level, knight, soldier, near, hp, area, tidy))))));
    }

    /** Ein Angriff: zurück an den Platz, erzwingen, warten, prüfen, ob der Ritter getroffen wurde, dann den nächsten. */
    private static void step(GameTestHelper helper, RobberKnightEntity knight, LivingEntity soldier, Vec3 spot, float[] hp,
            RobberKnightEntity.Attack attack, int ticks, String name, Runnable tidy, Runnable then) {
        knight.teleportTo(spot.x, spot.y, spot.z - 4.0);
        knight.setDeltaMovement(Vec3.ZERO);
        reset(soldier, spot, hp);
        knight.setTarget(soldier);
        knight.lookAt(soldier, 360.0f, 30.0f);
        knight.forceAttack(attack);
        helper.runAfterDelay(ticks, () -> tidyOnFailure(tidy, () -> {
            helper.assertTrue(soldier.getHealth() < hp[0], name + " trifft nicht (" + knight.currentAttack() + ", Abstand "
                    + fmt(knight.distanceTo(soldier)) + ")");
            then.run();
        }));
    }

    /** Der Bolzenhagel: fünf Zeichen am Boden (eines unter dem Ziel), dann schlagen dort die Bolzen ein. */
    private static void volley(GameTestHelper helper, RobberKnightEntity knight, LivingEntity soldier, Vec3 spot, float[] hp, Runnable tidy,
            Runnable then) {
        knight.teleportTo(spot.x, spot.y, spot.z - 12.0);
        reset(soldier, spot, hp);
        knight.setTarget(soldier);
        knight.forceAttack(RobberKnightEntity.Attack.VOLLEY);
        helper.runAfterDelay(RobberKnightEntity.VOLLEY_MARK + 3, () -> tidyOnFailure(tidy, () -> {
            helper.assertTrue(knight.volleyMarks().size() == RobberKnightEntity.VOLLEY_SPOTS, "Keine Zeichen am Boden: " + knight.volleyMarks().size());
            helper.assertTrue(soldier.getHealth() >= hp[0], "Der Hagel trifft vor der Zeit");
        }));
        helper.runAfterDelay(RobberKnightEntity.VOLLEY_FIRE + 8, () -> tidyOnFailure(tidy, () -> {
            helper.assertTrue(soldier.getHealth() < hp[0], "Der Bolzenhagel trifft nicht");
            then.run();
        }));
    }

    /** Der Hornruf: zwei seiner Leute kommen (sie gehören zu ihm: seine Angriffe treffen sie nicht). */
    private static void horn(GameTestHelper helper, ServerLevel level, RobberKnightEntity knight, LivingEntity soldier, Vec3 spot, float[] hp,
            AABB area, Runnable tidy, Runnable then) {
        knight.teleportTo(spot.x, spot.y, spot.z - 6.0);
        reset(soldier, spot, hp);
        knight.setTarget(soldier);
        knight.forceAttack(RobberKnightEntity.Attack.HORN);
        helper.runAfterDelay(RobberKnightEntity.HORN_CALL + 2, () -> tidyOnFailure(tidy, () -> {
            helper.assertTrue(knight.menAlive(level) == 2, "Auf den Hornruf kommen nicht zwei Leute: " + knight.menAlive(level));
            var men = level.getEntitiesOfClass(LivingEntity.class, area, e -> e.entityTags().contains("kaw_robber_men"));
            helper.assertTrue(men.size() == 2, "Seine Leute sind nicht da: " + men.size());
            helper.assertFalse(knight.canStrike(men.getFirst()), "Seine Angriffe träfen seine Leute");
            for (LivingEntity man : men) {
                man.discard();
            }
            then.run();
        }));
    }

    /** Zu Fuß: Morgensternschlag, Wirbel, Schildstoß, dann der Schildwall. */
    private static void onFoot(GameTestHelper helper, ServerLevel level, RobberKnightEntity knight, LivingEntity soldier, Vec3 spot, float[] hp,
            AABB area, Runnable tidy) {
        knight.forceOnFoot();
        helper.assertFalse(knight.isMounted(), "Er steigt nicht ab");
        Vec3 close = spot.add(0.0, 0.0, -1.5);
        step(helper, knight, soldier, spot.add(0.0, 0.0, -1.0), hp, RobberKnightEntity.Attack.SMASH, RobberKnightEntity.SMASH_HIT + 3,
                "Der Morgensternschlag", tidy, () ->
                step(helper, knight, soldier, spot.add(0.0, 0.0, -1.0), hp, RobberKnightEntity.Attack.WHIRL, RobberKnightEntity.WHIRL_HITS[0] + 3,
                        "Der Wirbel", tidy, () ->
                        step(helper, knight, soldier, spot.add(0.0, 0.0, -1.8), hp, RobberKnightEntity.Attack.BASH, RobberKnightEntity.BASH_HIT + 6,
                                "Der Schildstoß", tidy, () -> wall(helper, level, knight, soldier, close, area, tidy))));
    }

    /** Der Schildwall: von vorn trifft ihn nichts – von hinten sehr wohl. */
    private static void wall(GameTestHelper helper, ServerLevel level, RobberKnightEntity knight, LivingEntity soldier, Vec3 spot, AABB area,
            Runnable tidy) {
        knight.teleportTo(spot.x, spot.y, spot.z - 3.0);
        knight.setTarget(soldier);
        soldier.teleportTo(spot.x, spot.y, spot.z);
        knight.lookAt(soldier, 360.0f, 30.0f);
        knight.forceAttack(RobberKnightEntity.Attack.WALL);
        helper.runAfterDelay(RobberKnightEntity.WALL_FROM + 4, () -> tidyOnFailure(tidy, () -> {
            helper.assertTrue(knight.currentAttack() == RobberKnightEntity.Attack.WALL, "Kein Schildwall: " + knight.currentAttack());
            Vec3 front = knight.position().add(knight.forward().scale(2.0));
            soldier.teleportTo(front.x, front.y, front.z);
            knight.setInvulnerableTime(0);
            knight.damageCooldownTime = 0;
            float before = knight.getHealth();
            knight.hurtServer(level, level.damageSources().mobAttack(soldier), 12.0f);
            helper.assertTrue(knight.getHealth() >= before, "Von vorn geht der Schlag durch den Schildwall");
            Vec3 back = knight.position().subtract(knight.forward().scale(2.0));
            soldier.teleportTo(back.x, back.y, back.z);
            knight.setInvulnerableTime(0);
            knight.damageCooldownTime = 0;
            knight.hurtServer(level, level.damageSources().mobAttack(soldier), 12.0f);
            helper.assertTrue(knight.getHealth() < before, "Von hinten trifft ihn nichts");
            tidy.run();
            helper.succeed();
        }));
    }

    private static void reset(LivingEntity soldier, Vec3 spot, float[] hp) {
        soldier.teleportTo(spot.x, spot.y, spot.z);
        soldier.setHealth(soldier.getMaxHealth());
        soldier.setInvulnerableTime(0);
        soldier.setDeltaMovement(Vec3.ZERO);
        soldier.resetFallDistance();                        // vom letzten Treffer hochgeschleudert: sonst fiele er sich wund
        soldier.removeAllEffects();
        hp[0] = soldier.getHealth();
    }

    // =====================================================================================
    // Die Lanze am Schild
    // =====================================================================================

    /**
     * Wer den Lanzengang mit einem erhobenen Schild auffängt, an dem bricht die Lanze: der Schild ist kurz unbrauchbar und hat
     * gelitten, der Ritter wankt im Sattel (er nimmt dabei mehr Schaden) – danach reicht man ihm eine neue Lanze.
     */
    private static void shield(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper);
        RobberKnightEntity knight = helper.spawn(ModEntities.ROBBER_KNIGHT.get(), new BlockPos(24, 1, 8));
        knight.confine(20.0);
        AABB area = new AABB(helper.absolutePos(new BlockPos(24, 1, 20))).inflate(30, 12, 30);
        ServerPlayer player = MageGameTests.survivalPlayer(helper, "test-robber-shield", new io.netty.channel.embedded.EmbeddedChannel[1], 0);
        Runnable tidy = () -> {
            level.getServer().getPlayerList().remove(player);
            cleanup(level, area);
        };
        tidyOnFailure(tidy, () -> {
            player.connection.markClientLoaded();
            player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);     // (sonst nutzt sich der Schild nicht ab)
            BlockPos at = helper.absolutePos(new BlockPos(24, 1, 20));
            move(player, at.getX() + 0.5, at.getY(), at.getZ() + 0.5, 180.0f);          // er blickt nach Norden, zum Ritter
            player.setHealth(player.getMaxHealth());
            ItemStack shield = new ItemStack(Items.SHIELD);
            player.setItemInHand(InteractionHand.OFF_HAND, shield);
            player.startUsingItem(InteractionHand.OFF_HAND);
            for (int i = 0; i < 8 && !player.isBlocking(); i++) {
                player.doTick();                                // (die Verbindung des Testspielers tickt ihn sonst nicht)
            }
            move(player, at.getX() + 0.5, at.getY(), at.getZ() + 0.5, 180.0f);
            helper.assertTrue(player.isBlocking(), "Der Spieler hält den Schild nicht");
            knight.setInvulnerableTime(0);
            knight.damageCooldownTime = 0;
            float hp0 = knight.getHealth();
            knight.hurtServer(level, level.damageSources().magic(), 20.0f);
            float normal = hp0 - knight.getHealth();
            knight.setTarget(player);
            knight.lookAt(player, 360.0f, 30.0f);
            knight.forceAttack(RobberKnightEntity.Attack.CHARGE);
        });
        helper.runAfterDelay(RobberKnightEntity.CHARGE_AIM + 26, () -> tidyOnFailure(tidy, () -> {
            helper.assertTrue(knight.currentAttack() == RobberKnightEntity.Attack.STAGGER, "Die Lanze bricht nicht am Schild: "
                    + knight.currentAttack() + ", Abstand " + fmt(knight.distanceTo(player)));
            helper.assertFalse(knight.hasLance(), "Er hält noch die ganze Lanze");
            ItemStack shield = player.getItemInHand(InteractionHand.OFF_HAND);
            helper.assertTrue(player.getCooldowns().isOnCooldown(shield), "Der Schild taugt gleich wieder");
            helper.assertTrue(shield.getDamageValue() > 0, "Der Schild hat nicht gelitten");
            helper.assertTrue(player.getHealth() >= player.getMaxHealth() - 0.01f, "Durch den Schild nimmt der Spieler Schaden");
            // wankend nimmt er mehr Schaden
            knight.setInvulnerableTime(0);
            knight.damageCooldownTime = 0;
            float hp = knight.getHealth();
            knight.hurtServer(level, level.damageSources().magic(), 20.0f);
            float staggered = hp - knight.getHealth();
            helper.assertTrue(staggered > 20.0f * 1.2f, "Wankend nimmt er nicht mehr Schaden: " + staggered);
            level.getServer().getPlayerList().remove(player);
            helper.runAfterDelay(RobberKnightEntity.STAGGER_TICKS + 2, () -> tidyOnFailure(tidy, () -> {
                helper.assertTrue(knight.currentAttack() != RobberKnightEntity.Attack.STAGGER, "Das Wanken endet nicht");
                helper.assertTrue(knight.hasLance(), "Er bekommt keine neue Lanze");
                tidy.run();
                helper.succeed();
            }));
        }));
    }

    // =====================================================================================
    // Der Morgenstern
    // =====================================================================================

    /**
     * Der Morgenstern: schwerer als ein Netheritschwert, ein Schildbrecher, verzauberbar (Schärfe, Haltbarkeit), mit Stahl
     * auszubessern; der Schmetterschlag trifft, wer vor dem Spieler steht – nicht, wer hinter ihm steht – und lässt den
     * Morgenstern ruhen.
     */
    private static void morningStar(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ItemStack star = new ItemStack(CastleGear.MORNING_STAR.get());
        double[] damage = new double[1];
        star.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY).forEach(EquipmentSlot.MAINHAND, (attribute, modifier) -> {
            if (attribute.is(Attributes.ATTACK_DAMAGE)) {
                damage[0] += modifier.amount();
            }
        });
        helper.assertTrue(damage[0] >= 9.0, "Der Morgenstern trifft zu schwach: " + damage[0]);
        var weapon = star.get(DataComponents.WEAPON);
        helper.assertTrue(weapon != null && weapon.disableBlockingForSeconds() > 0.0f, "Der Morgenstern bricht keine Schilde");
        helper.assertTrue(star.isValidRepairItem(new ItemStack(ModItems.STEEL_INGOT.get())), "Nicht mit Stahl auszubessern");
        helper.assertTrue(star.is(ItemTags.SHARP_WEAPON_ENCHANTABLE) && star.is(ItemTags.DURABILITY_ENCHANTABLE), "Nicht verzauberbar");

        BlockPos base = helper.absolutePos(new BlockPos(4, 200, 4));
        for (BlockPos p : BlockPos.betweenClosed(base.offset(-3, -1, -5), base.offset(3, -1, 7))) {
            level.setBlock(p, Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        ServerPlayer player = MageGameTests.survivalPlayer(helper, "test-morning-star", new io.netty.channel.embedded.EmbeddedChannel[1], 0);
        Zombie front = ModEntities.ZOMBIE.get().create(level, EntitySpawnReason.EVENT);
        Zombie back = ModEntities.ZOMBIE.get().create(level, EntitySpawnReason.EVENT);
        if (front == null || back == null) {
            throw new IllegalStateException("Kein Zombie");
        }
        try {
            player.connection.markClientLoaded();
            player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);     // (sonst nutzt sich der Morgenstern nicht ab)
            move(player, base.getX() + 0.5, base.getY(), base.getZ() + 0.5, 0.0f);       // Blick nach Süden
            for (Zombie z : List.of(front, back)) {
                z.setNoAi(true);
            }
            front.snapTo(base.getX() + 0.5, base.getY(), base.getZ() + 3.5, 180.0f, 0.0f);
            back.snapTo(base.getX() + 0.5, base.getY(), base.getZ() - 2.5, 0.0f, 0.0f);
            level.addFreshEntity(front);
            level.addFreshEntity(back);
            player.setItemInHand(InteractionHand.MAIN_HAND, star);
            float frontHp = front.getHealth();
            float backHp = back.getHealth();
            star.use(level, player, InteractionHand.MAIN_HAND);
            helper.assertTrue(front.getHealth() < frontHp, "Der Schmetterschlag trifft nicht, wer vor ihm steht");
            helper.assertTrue(back.getHealth() >= backHp, "Der Schmetterschlag trifft, wer hinter ihm steht");
            helper.assertTrue(front.hasEffect(net.minecraft.world.effect.MobEffects.SLOWNESS), "Wen er trifft, der ist nicht verlangsamt");
            helper.assertTrue(player.getCooldowns().isOnCooldown(star), "Der Morgenstern ruht danach nicht");
            helper.assertTrue(star.getDamageValue() == 2, "Der Schmetterschlag kostet nicht zwei Haltbarkeit: " + star.getDamageValue());
            helper.assertTrue(MorningStarItem.COOLDOWN >= 100, "Der Schmetterschlag ist zu oft möglich");
        } finally {
            level.getServer().getPlayerList().remove(player);
            front.discard();
            back.discard();
            for (BlockPos p : BlockPos.betweenClosed(base.offset(-3, -1, -5), base.offset(3, -1, 7))) {
                level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
        }
        helper.succeed();
    }
}
