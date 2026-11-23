package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.serpent.SerpentAltarBlock;
import io.github.sirschrolli.kingdomsatwar.block.serpent.SerpentAltarBlockEntity;
import io.github.sirschrolli.kingdomsatwar.entity.serpent.QuetzaraEntity;
import io.github.sirschrolli.kingdomsatwar.entity.serpent.QuetzaraFeatherEntity;
import io.github.sirschrolli.kingdomsatwar.entity.serpent.QuetzaraPart;
import io.github.sirschrolli.kingdomsatwar.entity.serpent.ShedSkinEntity;
import io.github.sirschrolli.kingdomsatwar.entity.serpent.VenomGlobEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.item.serpent.SerpentFangItem;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.SerpentGear;
import io.netty.channel.embedded.EmbeddedChannel;
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
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Tests zu Quetzara, der Gefiederten Schlange (1.10): der Schlangenaltar (sie liegt eingerollt um ihn und schläft, wer in die
 * Grube steigt, weckt sie; findet sie niemanden mehr, kriecht sie zurück, rollt sich ein und heilt; besiegt zerspringt das
 * Schlangenbild, ein Goldblock ruft sie zurück), ihre Angriffe (Giftbiss, Giftspucke, Federsturm, Schwanzpeitsche), der
 * Würgegriff mit dem Befreiungsschlag, die Häutung mit der leeren Haut und dem Lauerbiss – und der Schlangenzahn.
 */
final class QuetzaraGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");
    private static final Identifier TOWN = KingdomsAtWar.id("test_town");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ALTAR =
            ModGameTests.TEST_FUNCTIONS.register("quetzara_altar", () -> QuetzaraGameTests::altar);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ATTACKS =
            ModGameTests.TEST_FUNCTIONS.register("quetzara_attacks", () -> QuetzaraGameTests::attacks);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> COIL =
            ModGameTests.TEST_FUNCTIONS.register("quetzara_coil", () -> QuetzaraGameTests::coil);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> MOLT =
            ModGameTests.TEST_FUNCTIONS.register("quetzara_molt", () -> QuetzaraGameTests::molt);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FANG =
            ModGameTests.TEST_FUNCTIONS.register("quetzara_serpent_fang", () -> QuetzaraGameTests::serpentFang);

    private QuetzaraGameTests() {}

    /** Lädt die Klasse (meldet die Testfunktionen an). */
    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        event.registerTest(FANG.getId(), new FunctionGameTestInstance(FANG.key(),
                new TestData<>(environment, Level.OVERWORLD, ARENA, 300, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        for (var test : List.of(ALTAR, ATTACKS, COIL, MOLT)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, TOWN, 1400, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
    }

    // =====================================================================================
    // Hilfen
    // =====================================================================================

    /** Der Boden aus Stein (wie bei den Tests Ventors). */
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

    /** Führt {@code body} aus; scheitert es, wird vorher aufgeräumt (eine liegen gebliebene Schlange stürzte sich sonst auf Nachbarn). */
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

    /** Vergiftet, mindestens in dieser Stärke (0: Gift I)? */
    private static boolean poisoned(LivingEntity entity, int amplifier) {
        MobEffectInstance poison = entity.getEffect(MobEffects.POISON);
        return poison != null && poison.getAmplifier() >= amplifier;
    }

    private static void fresh(LivingEntity entity) {
        entity.setInvulnerableTime(0);
        entity.damageCooldownTime = 0;
    }

    private static int dropped(ServerLevel level, AABB area, net.minecraft.world.item.Item item) {
        return level.getEntitiesOfClass(ItemEntity.class, area, e -> e.getItem().is(item)).stream().mapToInt(e -> e.getItem().getCount()).sum();
    }

    // =====================================================================================
    // Der Schlangenaltar
    // =====================================================================================

    /**
     * Der Schlangenaltar: unzerstörbar. Nähert sich jemand, liegt Quetzara eingerollt um ihn (jedes Glied in der Spirale) und
     * schläft – unverwundbar, und Magie weckt sie nicht. Steigt jemand in die Grube hinab, erwacht sie (dabei unverwundbar);
     * danach trifft man sie. Findet sie niemanden mehr, kriecht sie zurück, rollt sich ein und heilt im Schlaf. Besiegt lässt
     * sie Federn und Smaragde fallen, das Schlangenbild zerspringt, und sie kommt nicht wieder – bis ein Goldblock auf dem
     * Altar sie zurückruft (sofort wach).
     */
    private static void altar(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper);
        helper.assertTrue(ModEntities.QUETZARA.get().getCategory() == MobCategory.MONSTER, "Quetzara: falsche Gruppe");
        BlockPos pos = helper.absolutePos(new BlockPos(24, 1, 24));
        level.setBlock(pos, SerpentGear.SERPENT_ALTAR.get().defaultBlockState().setValue(SerpentAltarBlock.FACING, Direction.NORTH), Block.UPDATE_ALL);
        if (!(level.getBlockEntity(pos) instanceof SerpentAltarBlockEntity altar)) {
            throw new IllegalStateException("Der Schlangenaltar hat keine Blockentität");
        }
        helper.assertTrue(level.getBlockState(pos).getDestroySpeed(level, pos) < 0, "Der Schlangenaltar lässt sich abbauen");
        AABB area = new AABB(pos).inflate(30, 12, 30);
        ServerPlayer player = MageGameTests.survivalPlayer(helper, "test-quetzara-altar", new EmbeddedChannel[1], 0);
        Runnable tidy = () -> {
            level.getServer().getPlayerList().remove(player);
            cleanup(level, area);
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        };
        // jemand nähert sich (nicht nah genug, um sie zu wecken): sie liegt eingerollt um den Altar und schläft
        move(player, pos.getX() + 0.5, pos.getY(), pos.getZ() + 20.5, 180.0f);
        helper.runAfterDelay(45, () -> tidyOnFailure(tidy, () -> {
            QuetzaraEntity serpent = altar.serpent(level);
            helper.assertTrue(serpent != null, "Sie liegt nicht um den Altar, als sich jemand nähert");
            helper.assertTrue(serpent.isAsleep() && altar.phase() == SerpentAltarBlock.Phase.SLEEPING
                    && level.getBlockState(pos).getValue(SerpentAltarBlock.PHASE) == SerpentAltarBlock.Phase.SLEEPING,
                    "Sie schläft nicht: " + serpent.currentAttack() + ", " + altar.phase());
            helper.assertTrue(pos.equals(serpent.altar()), "Sie kennt ihren Altar nicht: " + serpent.altar());
            Vec3 c = Vec3.atBottomCenterOf(pos);
            for (int i = 0; i <= QuetzaraEntity.SEGMENTS; i++) {
                Vec3 p = serpent.bodyPoint(i, 1.0f);
                double flat = Math.hypot(p.x - c.x, p.z - c.z);
                helper.assertTrue(flat < QuetzaraEntity.COIL_OUTER + 0.6 && flat > QuetzaraEntity.COIL_INNER - 0.6,
                        "Glied " + i + " liegt nicht in der Spirale um den Altar: " + fmt(flat));
            }
            float hp = serpent.getHealth();
            serpent.hurtServer(level, level.damageSources().magic(), 50.0f);
            helper.assertTrue(serpent.getHealth() >= hp && serpent.isAsleep(), "Schlafend ist sie verwundbar, oder Magie weckt sie");
            // jemand steigt in die Grube hinab
            move(player, pos.getX() + 0.5, pos.getY(), pos.getZ() + 9.5, 180.0f);
            helper.runAfterDelay(14, () -> tidyOnFailure(tidy, () -> awake(helper, level, pos, altar, serpent, player, area, tidy)));
        }));
    }

    private static void awake(GameTestHelper helper, ServerLevel level, BlockPos pos, SerpentAltarBlockEntity altar, QuetzaraEntity serpent,
            ServerPlayer player, AABB area, Runnable tidy) {
        helper.assertTrue(serpent.currentAttack() == QuetzaraEntity.Attack.AWAKEN, "Sie erwacht nicht, als jemand in die Grube steigt: "
                + serpent.currentAttack());
        helper.assertTrue(altar.phase() == SerpentAltarBlock.Phase.AWAKE
                && level.getBlockState(pos).getValue(SerpentAltarBlock.PHASE) == SerpentAltarBlock.Phase.AWAKE, "Der Altar merkt das Erwachen nicht");
        float hp = serpent.getHealth();
        fresh(serpent);
        serpent.hurtServer(level, level.damageSources().magic(), 50.0f);
        helper.assertTrue(serpent.getHealth() >= hp, "Beim Erwachen ist sie verwundbar");
        helper.runAfterDelay(QuetzaraEntity.AWAKEN_TICKS + 4, () -> tidyOnFailure(tidy, () -> {
            helper.assertTrue(serpent.currentAttack() != QuetzaraEntity.Attack.AWAKEN, "Sie erwacht nicht ganz");
            fresh(serpent);
            float before = serpent.getHealth();
            serpent.hurtServer(level, level.damageSources().magic(), 20.0f);
            helper.assertTrue(before - serpent.getHealth() > 19.0f, "Wach ist sie unverwundbar: " + fmt(before - serpent.getHealth()));
            // niemand mehr da: sie kriecht zurück, rollt sich ein und schläft (und heilt)
            level.getServer().getPlayerList().remove(player);
            serpent.setHealth(serpent.getMaxHealth() - 60.0f);
            serpent.forceAttack(QuetzaraEntity.Attack.RETURN);
            helper.runAfterDelay(160, () -> tidyOnFailure(tidy, () -> {
                helper.assertTrue(serpent.isAsleep() && altar.phase() == SerpentAltarBlock.Phase.SLEEPING,
                        "Sie rollt sich nicht wieder um den Altar: " + serpent.currentAttack() + ", " + altar.phase());
                helper.assertTrue(serpent.getHealth() > serpent.getMaxHealth() - 60.0f, "Im Schlaf heilt sie nicht");
                defeat(helper, level, pos, altar, serpent, area, tidy);
            }));
        }));
    }

    private static void defeat(GameTestHelper helper, ServerLevel level, BlockPos pos, SerpentAltarBlockEntity altar, QuetzaraEntity serpent,
            AABB area, Runnable tidy) {
        QuetzaraEntity woken = altar.awaken(level);
        helper.assertTrue(woken == serpent && serpent.currentAttack() == QuetzaraEntity.Attack.AWAKEN, "Der Altar weckt sie nicht");
        helper.runAfterDelay(QuetzaraEntity.AWAKEN_TICKS + 4, () -> tidyOnFailure(tidy, () -> {
            fresh(serpent);
            serpent.setHealth(1.0f);
            BossLootGameTests.firstVictory(helper, serpent, 50.0f);
            helper.assertTrue(serpent.isDeadOrDying(), "Quetzara fällt nicht");
            helper.assertTrue(altar.phase() == SerpentAltarBlock.Phase.FALLEN
                    && level.getBlockState(pos).getValue(SerpentAltarBlock.PHASE) == SerpentAltarBlock.Phase.FALLEN, "Das Schlangenbild zerspringt nicht");
            helper.runAfterDelay(QuetzaraEntity.DEATH_TICKS + 8, () -> tidyOnFailure(tidy, () -> {
                helper.assertTrue(serpent.isRemoved(), "Sie zerfällt nicht");
                int feathers = dropped(level, area, SerpentGear.QUETZAL_FEATHER.get());
                int emeralds = dropped(level, area, Items.EMERALD);
                helper.assertTrue(feathers >= 1 && feathers <= 3 && emeralds >= 1 && emeralds <= 3, "Ihre Beute stimmt nicht: " + feathers
                        + " Federn, " + emeralds + " Smaragde");
                helper.assertTrue(altar.serpent(level) == null, "Mit zerbrochenem Schlangenbild liegt wieder eine Schlange am Altar");
                // ein Goldblock ruft sie zurück (sofort wach)
                ServerPlayer giver = MageGameTests.survivalPlayer(helper, "test-quetzara-gold", new EmbeddedChannel[1], 0);
                Runnable tidyAll = () -> {
                    level.getServer().getPlayerList().remove(giver);
                    tidy.run();
                };
                tidyOnFailure(tidyAll, () -> {
                    giver.connection.markClientLoaded();
                    giver.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
                    move(giver, pos.getX() + 0.5, pos.getY(), pos.getZ() - 2.5, 0.0f);
                    ItemStack gold = new ItemStack(Items.GOLD_BLOCK, 2);
                    giver.setItemInHand(InteractionHand.MAIN_HAND, gold);
                    InteractionResult result = level.getBlockState(pos).useItemOn(gold, level, giver, InteractionHand.MAIN_HAND,
                            new BlockHitResult(Vec3.atCenterOf(pos).add(0.0, 0.5, 0.0), Direction.UP, pos, false));
                    helper.assertTrue(result.consumesAction(), "Der Goldblock wird nicht angenommen: " + result);
                    helper.assertTrue(gold.getCount() == 1, "Der Goldblock wird nicht verbraucht: " + gold.getCount());
                    QuetzaraEntity again = altar.serpent(level);
                    helper.assertTrue(again != null && again != serpent && again.currentAttack() == QuetzaraEntity.Attack.AWAKEN
                            && altar.phase() == SerpentAltarBlock.Phase.AWAKE, "Sie kehrt nicht zurück: " + altar.phase());
                    tidyAll.run();
                    helper.succeed();
                });
            }));
        }));
    }

    // =====================================================================================
    // Die Angriffe
    // =====================================================================================

    /** Ein Ritter, der viel aushält und sich nicht von selbst bewegt. */
    private static LivingEntity knight(GameTestHelper helper, BlockPos at) {
        LivingEntity knight = helper.spawnWithNoFreeWill(ModEntities.SOLDIERS.get(SoldierType.KNIGHT).get(), at);
        knight.getAttribute(Attributes.MAX_HEALTH).setBaseValue(600.0);
        knight.setHealth(600.0f);
        return knight;
    }

    /**
     * Legt sie bei {@code spot} hin (der Kopf nach Osten, der Leib gerade nach Westen), stellt den Ritter {@code distance}
     * östlich davon und beginnt den Angriff. Was vom letzten Angriff noch fliegt oder liegt, ist fort.
     */
    private static void place(QuetzaraEntity serpent, LivingEntity knight, Vec3 spot, double distance, float[] hp, QuetzaraEntity.Attack attack) {
        serpent.forceAttack(QuetzaraEntity.Attack.NONE);
        for (Entity e : serpent.level().getEntitiesOfClass(Entity.class, serpent.getBoundingBox().inflate(32.0),
                e -> e instanceof QuetzaraFeatherEntity || e instanceof VenomGlobEntity || e instanceof AreaEffectCloud || e instanceof ShedSkinEntity)) {
            e.discard();
        }
        serpent.layOut(spot, -90.0f, 0.0f);
        knight.teleportTo(spot.x + distance, spot.y, spot.z);
        knight.setHealth(knight.getMaxHealth());
        fresh(knight);
        knight.setDeltaMovement(Vec3.ZERO);
        knight.resetFallDistance();
        knight.removeAllEffects();
        hp[0] = knight.getHealth();
        serpent.setTarget(knight);
        serpent.forceAttack(attack);
    }

    /** Ein Angriff: an die Plätze, erzwingen, warten, prüfen, ob der Ritter getroffen wurde (und wie schwer), dann der nächste. */
    private static void step(GameTestHelper helper, QuetzaraEntity serpent, LivingEntity knight, Vec3 spot, double distance, float[] hp,
            QuetzaraEntity.Attack attack, int ticks, String name, Runnable tidy, Consumer<Float> then) {
        place(serpent, knight, spot, distance, hp, attack);
        helper.runAfterDelay(ticks, () -> tidyOnFailure(tidy, () -> {
            helper.assertTrue(knight.getHealth() < hp[0], name + " trifft nicht (" + serpent.currentAttack() + ", Abstand "
                    + fmt(serpent.distanceTo(knight)) + ")");
            then.accept(hp[0] - knight.getHealth());
        }));
    }

    /**
     * Ihre Angriffe gegen einen (zähen) Ritter: der Giftbiss (schweres Gift), die Giftspucke (eine Giftwolke beim Ritter), der
     * Federsturm (vergiftete Federn) und die Schwanzpeitsche (wer am Schwanz steht, wird getroffen).
     */
    private static void attacks(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper);
        QuetzaraEntity serpent = helper.spawn(ModEntities.QUETZARA.get(), new BlockPos(20, 1, 24));
        serpent.confine(16.0);
        LivingEntity knight = knight(helper, new BlockPos(26, 1, 24));
        AABB area = new AABB(helper.absolutePos(new BlockPos(24, 1, 24))).inflate(30, 16, 30);
        Runnable tidy = () -> cleanup(level, area);
        Vec3 spot = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(20, 1, 24)));
        float[] hp = {0.0f};
        step(helper, serpent, knight, spot, 4.0, hp, QuetzaraEntity.Attack.BITE, QuetzaraEntity.BITE_LUNGE + 9, "Der Giftbiss", tidy, bite -> {
            helper.assertTrue(poisoned(knight, 1), "Der Giftbiss vergiftet nicht schwer: " + knight.getEffect(MobEffects.POISON));
            // die Giftspucke: ein Klumpen fliegt, beim Ritter steht eine Giftwolke
            place(serpent, knight, spot, 8.0, hp, QuetzaraEntity.Attack.SPIT);
            helper.runAfterDelay(QuetzaraEntity.SPIT_RELEASE + 2, () -> tidyOnFailure(tidy, () ->
                    helper.assertTrue(!level.getEntitiesOfClass(VenomGlobEntity.class, area).isEmpty()
                            || !level.getEntitiesOfClass(AreaEffectCloud.class, area).isEmpty(), "Sie spuckt kein Gift")));
            helper.runAfterDelay(QuetzaraEntity.SPIT_RELEASE + 40, () -> tidyOnFailure(tidy, () -> {
                List<AreaEffectCloud> clouds = level.getEntitiesOfClass(AreaEffectCloud.class, knight.getBoundingBox().inflate(5.0, 4.0, 5.0));
                helper.assertTrue(!clouds.isEmpty() || knight.getHealth() < hp[0], "Die Giftspucke landet nicht beim Ritter");
                // der Federsturm
                step(helper, serpent, knight, spot, 9.0, hp, QuetzaraEntity.Attack.FEATHERS, QuetzaraEntity.FEATHER_VOLLEYS[2] + 16,
                        "Der Federsturm", tidy, feathers -> {
                            helper.assertTrue(poisoned(knight, 0), "Die Federn vergiften nicht");
                            rain(helper, level, serpent, knight, spot, hp, area, tidy, () -> whip(helper, level, serpent, knight, spot, hp, tidy));
                        });
            }));
        });
    }

    /**
     * Der Giftregen: Ein Warnkreis liegt genau unter dem Ritter, die übrigen ringsum; alle Tropfen steigen auf und fallen jeder
     * in seinen Kreis (dort steht danach eine kleine Giftwolke). Der Ritter, der stehen bleibt, wird getroffen und vergiftet.
     */
    private static void rain(GameTestHelper helper, ServerLevel level, QuetzaraEntity serpent, LivingEntity knight, Vec3 spot, float[] hp,
            AABB area, Runnable tidy, Runnable then) {
        place(serpent, knight, spot, 10.0, hp, QuetzaraEntity.Attack.RAIN);
        int last = QuetzaraEntity.RAIN_BURSTS[QuetzaraEntity.RAIN_BURSTS.length - 1];
        helper.runAfterDelay(QuetzaraEntity.RAIN_BURSTS[0] + 1, () -> tidyOnFailure(tidy, () -> {
            List<Vec3> spots = serpent.rainSpots();
            helper.assertTrue(spots.size() == QuetzaraEntity.RAIN_DROPS, "Der Giftregen plant nicht " + QuetzaraEntity.RAIN_DROPS
                    + " Tropfen: " + spots.size());
            Vec3 first = spots.getFirst();
            helper.assertTrue(Math.hypot(first.x - knight.getX(), first.z - knight.getZ()) < 0.6 && Math.abs(first.y - knight.getY()) < 0.6,
                    "Kein Warnkreis unter dem Ritter: " + first + " / " + knight.position());
        }));
        helper.runAfterDelay(last + 1, () -> tidyOnFailure(tidy, () -> {
            int drops = level.getEntitiesOfClass(VenomGlobEntity.class, area).size();
            helper.assertTrue(drops == QuetzaraEntity.RAIN_DROPS, "Es fliegen nicht " + QuetzaraEntity.RAIN_DROPS + " Tropfen: " + drops);
        }));
        helper.runAfterDelay(last + 58, () -> tidyOnFailure(tidy, () -> {
            helper.assertTrue(level.getEntitiesOfClass(VenomGlobEntity.class, area).isEmpty(), "Es fliegen noch Tropfen");
            List<AreaEffectCloud> clouds = level.getEntitiesOfClass(AreaEffectCloud.class, area);
            for (Vec3 p : serpent.rainSpots()) {
                double best = clouds.stream().mapToDouble(c -> Math.hypot(c.getX() - p.x, c.getZ() - p.z)).min().orElse(99.0);
                helper.assertTrue(best < 1.2, "Ein Tropfen fällt nicht in seinen Kreis (" + fmt(best) + " daneben)");
            }
            helper.assertTrue(knight.getHealth() < hp[0], "Der Giftregen trifft den Ritter nicht");
            helper.assertTrue(poisoned(knight, 0), "Der Giftregen vergiftet nicht");
            then.run();
        }));
    }

    /** Die Schwanzpeitsche: Der Ritter steht neben dem Schwanz – der Hieb fegt ihn fort. */
    private static void whip(GameTestHelper helper, ServerLevel level, QuetzaraEntity serpent, LivingEntity knight, Vec3 spot, float[] hp,
            Runnable tidy) {
        place(serpent, knight, spot, 6.0, hp, QuetzaraEntity.Attack.NONE);
        helper.runAfterDelay(2, () -> tidyOnFailure(tidy, () -> {
            Vec3 tail = serpent.bodyPoint(QuetzaraEntity.SEGMENTS - 2, 1.0f);
            knight.teleportTo(tail.x, spot.y, tail.z + 1.8);
            knight.setDeltaMovement(Vec3.ZERO);
            hp[0] = knight.getHealth();
            serpent.setTarget(knight);
            serpent.forceAttack(QuetzaraEntity.Attack.WHIP);
            Vec3 before = knight.position();
            helper.runAfterDelay(QuetzaraEntity.WHIP_STRIKE + 10, () -> tidyOnFailure(tidy, () -> {
                helper.assertTrue(knight.getHealth() < hp[0], "Die Schwanzpeitsche trifft nicht (Seite " + serpent.whipSide() + ")");
                helper.assertTrue(knight.position().distanceTo(before) > 0.5, "Die Schwanzpeitsche fegt den Ritter nicht fort");
                tidy.run();
                helper.succeed();
            }));
        }));
    }

    // =====================================================================================
    // Der Würgegriff
    // =====================================================================================

    /**
     * Der Würgegriff: Sie umkreist den Ritter, schließt den Ring (er kann sich nicht mehr rühren) und drückt zu. Wer ihr in der
     * Umschlingung genug Schaden zufügt, kommt frei: Sie lässt los und ist benommen – dann trifft man ihren Kopf härter.
     */
    private static void coil(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper);
        QuetzaraEntity serpent = helper.spawn(ModEntities.QUETZARA.get(), new BlockPos(20, 1, 24));
        serpent.confine(16.0);
        LivingEntity knight = knight(helper, new BlockPos(24, 1, 24));
        AABB area = new AABB(helper.absolutePos(new BlockPos(24, 1, 24))).inflate(30, 16, 30);
        Runnable tidy = () -> cleanup(level, area);
        Vec3 spot = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(20, 1, 24)));
        float[] hp = {0.0f};
        place(serpent, knight, spot, 3.0, hp, QuetzaraEntity.Attack.CONSTRICT);
        helper.runAfterDelay(32, () -> tidyOnFailure(tidy, () -> {
            helper.assertTrue(knight.getUUID().equals(serpent.coilVictim()), "Sie umschlingt den Ritter nicht");
            helper.assertTrue(knight.getAttributeValue(Attributes.MOVEMENT_SPEED) < 1.0e-3, "In der Umschlingung kann er sich rühren");
            int around = 0;
            for (int i = 0; i <= QuetzaraEntity.SEGMENTS; i++) {
                Vec3 p = serpent.bodyPoint(i, 1.0f);
                if (Math.hypot(p.x - knight.getX(), p.z - knight.getZ()) < 3.2) {
                    around++;
                }
            }
            helper.assertTrue(around >= 6, "Ihr Leib liegt nicht um den Ritter: " + around + " Glieder");
        }));
        helper.runAfterDelay(32 + 24, () -> tidyOnFailure(tidy, () -> {
            helper.assertTrue(knight.getHealth() < hp[0], "Sie drückt nicht zu");
            helper.assertTrue(serpent.currentAttack() == QuetzaraEntity.Attack.CONSTRICT, "Sie lässt von selbst los: " + serpent.currentAttack());
            // der Befreiungsschlag
            fresh(serpent);
            serpent.hurtServer(level, level.damageSources().magic(), QuetzaraEntity.ESCAPE_DAMAGE + 1.0f);
            helper.runAfterDelay(3, () -> tidyOnFailure(tidy, () -> {
                helper.assertTrue(serpent.currentAttack() == QuetzaraEntity.Attack.STUN, "Nach dem Befreiungsschlag ist sie nicht benommen: "
                        + serpent.currentAttack());
                helper.assertTrue(serpent.coilVictim() == null && knight.getAttributeValue(Attributes.MOVEMENT_SPEED) > 0.05,
                        "Der Ritter bleibt gefesselt");
                QuetzaraPart[] parts = serpent.serpentParts();
                fresh(serpent);
                float before = serpent.getHealth();
                serpent.hurtFromPart(level, parts[0], level.damageSources().magic(), 10.0f);
                float head = before - serpent.getHealth();
                fresh(serpent);
                before = serpent.getHealth();
                serpent.hurtFromPart(level, parts[6], level.damageSources().magic(), 10.0f);
                float body = before - serpent.getHealth();
                helper.assertTrue(body > 0.0f && head > body * 2.5f, "Benommen trifft man den Kopf nicht härter: Kopf " + fmt(head) + ", Leib "
                        + fmt(body));
                tidy.run();
                helper.succeed();
            }));
        }));
    }

    // =====================================================================================
    // Die Häutung
    // =====================================================================================

    /**
     * Die Häutung: Unter halber Kraft häutet sie sich von selbst (solange die Haut reißt, unverwundbar). Die leere Haut bleibt
     * liegen – wer sie trifft, dem platzt sie als Giftwolke entgegen, dann zerfällt sie. Sie selbst ist gehäutet und getarnt;
     * ihr erster Biss aus dem Hinterhalt trifft härter (und danach ist die Tarnung fort), ihr Gift ist stärker.
     */
    private static void molt(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper);
        QuetzaraEntity serpent = helper.spawn(ModEntities.QUETZARA.get(), new BlockPos(26, 1, 24));
        serpent.confine(16.0);
        LivingEntity knight = knight(helper, new BlockPos(33, 1, 24));
        AABB area = new AABB(helper.absolutePos(new BlockPos(24, 1, 24))).inflate(30, 16, 30);
        Runnable tidy = () -> cleanup(level, area);
        Vec3 spot = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(26, 1, 24)));
        float[] hp = {0.0f};
        place(serpent, knight, spot, 7.0, hp, QuetzaraEntity.Attack.NONE);
        serpent.setHealth(QuetzaraEntity.MAX_HEALTH * 0.45f);
        helper.runAfterDelay(4, () -> tidyOnFailure(tidy, () -> {
            helper.assertTrue(serpent.currentAttack() == QuetzaraEntity.Attack.MOLT, "Unter halber Kraft häutet sie sich nicht: "
                    + serpent.currentAttack());
            fresh(serpent);
            float before = serpent.getHealth();
            serpent.hurtServer(level, level.damageSources().magic(), 30.0f);
            helper.assertTrue(serpent.getHealth() >= before, "Während die Haut reißt, ist sie verwundbar");
        }));
        helper.runAfterDelay(QuetzaraEntity.MOLT_SPLIT + 6, () -> tidyOnFailure(tidy, () -> {
            List<ShedSkinEntity> skins = level.getEntitiesOfClass(ShedSkinEntity.class, area);
            helper.assertTrue(skins.size() == 1 && serpent.skins().size() == 1, "Keine leere Haut: " + skins.size());
            helper.assertTrue(serpent.isMolted() && serpent.isCamouflaged(), "Sie ist nicht gehäutet oder nicht getarnt");
            ShedSkinEntity skin = skins.getFirst();
            helper.assertTrue(skin.isPickable() && skin.isAttackable(), "Die leere Haut lässt sich nicht treffen");
            skin.hurtServer(level, level.damageSources().magic(), 1.0f);
            helper.assertTrue(!level.getEntitiesOfClass(AreaEffectCloud.class, skin.getBoundingBox().inflate(12.0)).isEmpty(),
                    "Die Haut platzt nicht als Giftwolke");
            helper.runAfterDelay(30, () -> tidyOnFailure(tidy, () -> helper.assertTrue(skin.isRemoved(), "Die geplatzte Haut zerfällt nicht")));
        }));
        helper.runAfterDelay(QuetzaraEntity.Attack.MOLT.duration + 6, () -> tidyOnFailure(tidy, () -> {
            helper.assertTrue(serpent.currentAttack() != QuetzaraEntity.Attack.MOLT, "Die Häutung endet nicht");
            helper.assertTrue(serpent.isCamouflaged(), "Die Tarnung ist zu früh fort");
            // der Lauerbiss
            step(helper, serpent, knight, spot, 4.0, hp, QuetzaraEntity.Attack.BITE, QuetzaraEntity.BITE_LUNGE + 9, "Der Lauerbiss", tidy, ambush -> {
                helper.assertTrue(poisoned(knight, 2), "Gehäutet ist ihr Gift nicht stärker: " + knight.getEffect(MobEffects.POISON));
                helper.assertTrue(!serpent.isCamouflaged(), "Nach dem Lauerbiss bleibt sie getarnt");
                // ein gewöhnlicher Biss danach
                step(helper, serpent, knight, spot, 4.0, hp, QuetzaraEntity.Attack.BITE, QuetzaraEntity.BITE_LUNGE + 9, "Der Biss", tidy, normal -> {
                    helper.assertTrue(ambush > normal * 1.2f, "Der Lauerbiss trifft nicht härter: " + fmt(ambush) + " statt " + fmt(normal));
                    tidy.run();
                    helper.succeed();
                });
            });
        }));
    }

    // =====================================================================================
    // Der Schlangenzahn
    // =====================================================================================

    /**
     * Der Schlangenzahn: ein schneller, scharfer Dolch (kein Schwungangriff), haltbar, mit Quetzalfedern auszubessern,
     * verzauberbar, episch. Jeder Treffer vergiftet; wer schon vergiftet ist, den trifft er härter. Der Schlangenstoß: ein Satz
     * nach vorn, der Biss trifft und vergiftet schwer; danach ruht der Zahn.
     */
    private static void serpentFang(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ItemStack fang = new ItemStack(SerpentGear.SERPENT_FANG.get());
        double[] mods = new double[2];
        fang.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY).forEach(EquipmentSlot.MAINHAND, (attribute, modifier) -> {
            if (attribute.is(Attributes.ATTACK_DAMAGE)) {
                mods[0] += modifier.amount();
            }
            if (attribute.is(Attributes.ATTACK_SPEED)) {
                mods[1] += modifier.amount();
            }
        });
        helper.assertTrue(Math.abs(mods[0] + 1.0 - SerpentFangItem.DAMAGE) < 1.0e-6, "Falscher Schaden: " + (mods[0] + 1.0));
        helper.assertTrue(Math.abs(mods[1] + 4.0 - SerpentFangItem.SPEED) < 1.0e-6, "Falsches Tempo: " + (mods[1] + 4.0));
        helper.assertTrue(fang.getMaxDamage() >= 1000, "Der Schlangenzahn hält zu wenig aus: " + fang.getMaxDamage());
        helper.assertTrue(fang.isValidRepairItem(new ItemStack(SerpentGear.QUETZAL_FEATHER.get())), "Nicht mit Quetzalfedern auszubessern");
        helper.assertTrue(fang.is(ItemTags.MELEE_WEAPON_ENCHANTABLE) && fang.is(ItemTags.DURABILITY_ENCHANTABLE), "Nicht verzauberbar");
        helper.assertTrue(fang.getRarity() == Rarity.EPIC, "Der Schlangenzahn ist nicht episch");
        helper.assertFalse(fang.canPerformAction(ItemAbilities.SWORD_SWEEP), "Der Dolch hat einen Schwungangriff");

        BlockPos base = helper.absolutePos(new BlockPos(4, 200, 4));
        for (BlockPos p : BlockPos.betweenClosed(base.offset(-4, -1, -3), base.offset(4, -1, 10))) {
            level.setBlock(p, Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        ServerPlayer player = MageGameTests.survivalPlayer(helper, "test-serpent-fang", new EmbeddedChannel[1], 0);
        // (ein Ritter: Tiere ersetzt die Mod beim Erscheinen durch ihre eigenen)
        LivingEntity pig = ModEntities.SOLDIERS.get(SoldierType.KNIGHT).get().create(level, EntitySpawnReason.EVENT);
        if (pig == null) {
            throw new IllegalStateException("Kein Ritter");
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
        tidyOnFailure(tidy, () -> {
            player.connection.markClientLoaded();
            player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
            move(player, base.getX() + 0.5, base.getY(), base.getZ() + 0.5, 0.0f);       // Blick nach Süden
            if (pig instanceof net.minecraft.world.entity.Mob mob) {
                mob.setNoAi(true);
            }
            pig.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200.0);
            pig.setHealth(200.0f);
            pig.snapTo(base.getX() + 0.5, base.getY(), base.getZ() + 4.0, 180.0f, 0.0f);
            level.addFreshEntity(pig);
            player.setItemInHand(InteractionHand.MAIN_HAND, fang);
            // jeder Treffer vergiftet; wer schon vergiftet ist, den trifft er härter
            float plain = fang.getItem().getAttackDamageBonus(pig, 10.0f, player.damageSources().playerAttack(player));
            helper.assertTrue(plain == 0.0f, "Ohne Gift trifft er schon härter: " + plain);
            fang.hurtEnemy(pig, player);
            helper.assertTrue(poisoned(pig, 1), "Ein Treffer vergiftet nicht: " + pig.getEffect(MobEffects.POISON));
            float bonus = fang.getItem().getAttackDamageBonus(pig, 10.0f, player.damageSources().playerAttack(player));
            helper.assertTrue(Math.abs(bonus - 10.0f * SerpentFangItem.POISONED_BONUS) < 1.0e-3, "Wer vergiftet ist, den trifft er nicht härter: "
                    + bonus);
            pig.removeAllEffects();
            // der Schlangenstoß
            float hp = pig.getHealth();
            int worn = fang.getDamageValue();
            LivingEntity aim = SerpentFangItem.lungeTarget(level, player, new Vec3(0.0, 0.0, 1.0));
            helper.assertTrue(aim == pig, "Der Schlangenstoß findet den Ritter nicht: " + aim + " (Spieler " + player.position() + ", Ritter "
                    + pig.position() + ", Sicht " + player.hasLineOfSight(pig) + ", lebt " + pig.isAlive() + ")");
            fang.use(level, player, InteractionHand.MAIN_HAND);
            helper.assertTrue(pig.getHealth() < hp, "Der Schlangenstoß trifft nicht: " + pig.getHealth() + " (Angriff "
                    + player.getAttributeValue(Attributes.ATTACK_DAMAGE) + ")");
            helper.assertTrue(poisoned(pig, 2), "Der Schlangenstoß vergiftet nicht schwer: " + pig.getEffect(MobEffects.POISON));
            helper.assertTrue(player.getDeltaMovement().z > 0.5, "Kein Satz nach vorn: " + fmt(player.getDeltaMovement().z));
            helper.assertTrue(player.getCooldowns().isOnCooldown(fang), "Der Schlangenstoß lässt sich ohne Pause wiederholen");
            helper.assertTrue(fang.getDamageValue() == worn + 2, "Der Schlangenstoß nutzt den Zahn nicht ab: " + fang.getDamageValue());
        });
        tidy.run();
        helper.succeed();
    }
}
