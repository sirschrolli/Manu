package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.hero.HeroAbilities;
import io.github.sirschrolli.kingdomsatwar.hero.HeroAbility;
import io.github.sirschrolli.kingdomsatwar.hero.HeroLevel;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Tests zum Leveln seit 1.11: die Handwerkspunkte (eigener Topf, die Angelkunst gehört dazu, Neuverteilen und Ausgleich je
 * Topf), was Bergbau, Holzfällen, Ackerbau und Bogenschießen bewirken, die aktiven Fähigkeiten (Stufe, Abklingzeit und jede
 * Wirkung) und die Titel hinter dem Unsterblichen.
 */
final class HeroCraftGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> POT =
            ModGameTests.TEST_FUNCTIONS.register("hero_craft_pot", () -> HeroCraftGameTests::pot);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TOOLS =
            ModGameTests.TEST_FUNCTIONS.register("hero_craft_tools", () -> HeroCraftGameTests::tools);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> HARVEST =
            ModGameTests.TEST_FUNCTIONS.register("hero_craft_harvest", () -> HeroCraftGameTests::harvest);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ABILITIES =
            ModGameTests.TEST_FUNCTIONS.register("hero_abilities", () -> HeroCraftGameTests::abilities);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TITLES =
            ModGameTests.TEST_FUNCTIONS.register("hero_titles", () -> HeroCraftGameTests::titles);

    private HeroCraftGameTests() {}

    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var test : List.of(POT, TOOLS, HARVEST, ABILITIES, TITLES)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 400, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
    }

    // =====================================================================================
    // Hilfen
    // =====================================================================================

    private static ServerPlayer hero(GameTestHelper helper, BlockPos pos, Item tool) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        Vec3 at = helper.absoluteVec(Vec3.atBottomCenterOf(pos));
        player.snapTo(at.x, at.y, at.z, 0.0f, 0.0f);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(tool));
        HeroLevel.set(player, HeroLevel.Hero.NONE);
        HeroAbilities.refresh(player);
        return player;
    }

    private static void leave(GameTestHelper helper, ServerPlayer player) {
        HeroLevel.set(player, HeroLevel.Hero.NONE);
        helper.getLevel().getServer().getPlayerList().remove(player);
    }

    /** Ein Held auf dieser Stufe mit diesen Punkten in diesen Talenten. */
    private static HeroLevel.Hero with(int level, Object... talentsAndPoints) {
        List<Integer> points = new ArrayList<>();
        for (int i = 0; i < HeroLevel.Talent.values().length; i++) {
            points.add(0);
        }
        for (int i = 0; i < talentsAndPoints.length; i += 2) {
            points.set(((HeroLevel.Talent) talentsAndPoints[i]).ordinal(), (Integer) talentsAndPoints[i + 1]);
        }
        return new HeroLevel.Hero(HeroLevel.total(level), points, 0);
    }

    private static AABB area(GameTestHelper helper) {
        return new AABB(Vec3.atLowerCornerOf(helper.absolutePos(new BlockPos(-2, -1, -2))),
                Vec3.atLowerCornerOf(helper.absolutePos(new BlockPos(11, 10, 11))));
    }

    private static int count(GameTestHelper helper, Item item) {
        int n = 0;
        for (ItemEntity drop : helper.getLevel().getEntitiesOfClass(ItemEntity.class, area(helper))) {
            if (drop.getItem().is(item)) {
                n += drop.getItem().getCount();
            }
        }
        return n;
    }

    private static void clearDrops(GameTestHelper helper) {
        for (Entity e : helper.getLevel().getEntitiesOfClass(Entity.class, area(helper), e -> !(e instanceof ServerPlayer))) {
            e.discard();
        }
    }

    private static String fmt(double d) {
        return String.format(Locale.ROOT, "%.3f", d);
    }

    // =====================================================================================
    // Der Topf des Handwerks
    // =====================================================================================

    /**
     * Handwerkspunkte: einer je vier Stufen (Stufe 500: 125 – nicht genug, um alles zu meistern); Handwerk und Kampf zählen
     * getrennt; die Angelkunst gehört seit 1.11 zum Handwerk (wer früher Punkte darin hatte, hat seine Talentpunkte wieder
     * frei); Neuverteilen nimmt nur einen Topf zurück; passt ein Topf nicht zur Stufe, wird nur er zurückgegeben.
     */
    private static void pot(GameTestHelper helper) {
        ServerPlayer player = hero(helper, new BlockPos(4, 1, 4), Items.AIR);
        try {
            helper.assertTrue(HeroLevel.craftPointsFor(1) == 0 && HeroLevel.craftPointsFor(3) == 0 && HeroLevel.craftPointsFor(4) == 1
                    && HeroLevel.craftPointsFor(100) == 25 && HeroLevel.craftPointsFor(500) == 125, "Handwerkspunkte je Stufe");
            helper.assertTrue(HeroLevel.craftPointsFor(HeroLevel.MAX_LEVEL) < HeroLevel.Talent.of(HeroLevel.Pot.CRAFT).size() * HeroLevel.MAX_TALENT,
                    "Mit allen Handwerkspunkten ließe sich alles meistern");
            helper.assertTrue(HeroLevel.Talent.FISHING.pot() == HeroLevel.Pot.CRAFT && HeroLevel.Talent.ARCHERY.pot() == HeroLevel.Pot.COMBAT
                    && HeroLevel.Talent.of(HeroLevel.Pot.COMBAT).size() == 7 && HeroLevel.Talent.of(HeroLevel.Pot.CRAFT).size() == 4,
                    "Die Töpfe der Talente stimmen nicht");
            HeroLevel.award(player, HeroLevel.total(40));
            HeroLevel.Hero hero = HeroLevel.of(player);
            helper.assertTrue(hero.earned(HeroLevel.Pot.CRAFT) == 10 && hero.earned(HeroLevel.Pot.COMBAT) == HeroLevel.pointsFor(40),
                    "Punkte auf Stufe 40: " + hero.earned(HeroLevel.Pot.CRAFT));
            for (int i = 0; i < 10; i++) {
                helper.assertTrue(HeroLevel.spend(player, HeroLevel.Talent.MINING), "Handwerkspunkt " + (i + 1) + " geht nicht");
            }
            helper.assertTrue(!HeroLevel.spend(player, HeroLevel.Talent.WOODCUTTING), "Mehr Handwerkspunkte als die Stufe hergibt");
            helper.assertValueEqual(HeroLevel.of(player).unspent(HeroLevel.Pot.COMBAT), HeroLevel.pointsFor(40), "Das Handwerk nimmt Talentpunkte");
            helper.assertTrue(HeroLevel.spend(player, HeroLevel.Talent.ARCHERY), "Bogenschießen nimmt keinen Talentpunkt");
            helper.assertValueEqual(HeroLevel.of(player).unspent(HeroLevel.Pot.CRAFT), 0, "Ein Kampftalent nimmt Handwerkspunkte");
            // neu verteilen: nur der Topf des Handwerks (eine Silbermünze je Punkt)
            player.getInventory().add(new ItemStack(ModItems.GOLD_COIN.get(), 9));
            helper.assertTrue(HeroLevel.resetCost(HeroLevel.of(player), HeroLevel.Pot.CRAFT) == 10 * HeroLevel.RESET_PER_POINT,
                    "Preis des Neuverteilens im Handwerk");
            helper.assertTrue(HeroLevel.reset(player, HeroLevel.Pot.CRAFT), "Handwerk neu verteilen geht nicht");
            hero = HeroLevel.of(player);
            helper.assertTrue(hero.spent(HeroLevel.Pot.CRAFT) == 0 && hero.points(HeroLevel.Talent.ARCHERY) == 1,
                    "Neu verteilen im Handwerk nimmt auch Kampftalente zurück");
            // ein Held von vor 1.11 mit Punkten in der Angelkunst: seine Talentpunkte sind wieder frei
            HeroLevel.set(player, with(320, HeroLevel.Talent.STRENGTH, 30, HeroLevel.Talent.FISHING, 26));
            hero = HeroLevel.of(player);
            helper.assertTrue(hero.spent(HeroLevel.Pot.COMBAT) == 30 && hero.spent(HeroLevel.Pot.CRAFT) == 26
                    && hero.unspent(HeroLevel.Pot.COMBAT) == HeroLevel.pointsFor(320) - 30, "Die Angelkunst zählt noch als Kampftalent");
            helper.assertTrue(!HeroLevel.settle(player) && HeroLevel.of(player).points(HeroLevel.Talent.FISHING) == 26,
                    "Die Angelkunst passt zur Stufe und wurde trotzdem zurückgegeben");
            // passt das Handwerk nicht zur Stufe, wird nur das Handwerk zurückgegeben
            HeroLevel.set(player, with(20, HeroLevel.Talent.STRENGTH, 3, HeroLevel.Talent.FISHING, 12));
            helper.assertTrue(HeroLevel.settle(player), "Zu viele Handwerkspunkte werden nicht zurückgegeben");
            hero = HeroLevel.of(player);
            helper.assertTrue(hero.points(HeroLevel.Talent.FISHING) == 0 && hero.points(HeroLevel.Talent.STRENGTH) == 3,
                    "Der Ausgleich nimmt auch die Kampftalente zurück");
            helper.succeed();
        } finally {
            leave(helper, player);
        }
    }

    // =====================================================================================
    // Werkzeuge und Bogen
    // =====================================================================================

    /**
     * Bergbau: Die Spitzhacke baut schneller ab (40 Punkte: 60 %), nicht aber Holz. Holzfällen: Die Axt fällt Stämme schneller
     * (40 Punkte: 75 %). Bogenschießen: Ein Pfeil aus dem Bogen trifft härter (40 Punkte: 75 %), ein Pfeil ohne Bogen nicht.
     */
    private static void tools(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = hero(helper, new BlockPos(4, 1, 4), Items.IRON_PICKAXE);
        try {
            BlockPos pos = helper.absolutePos(new BlockPos(4, 1, 6));
            BlockState stone = Blocks.STONE.defaultBlockState();
            BlockState log = Blocks.OAK_LOG.defaultBlockState();
            float stoneBase = player.getDestroySpeed(stone, pos);
            float logPick = player.getDestroySpeed(log, pos);
            HeroLevel.set(player, with(500, HeroLevel.Talent.MINING, 40, HeroLevel.Talent.WOODCUTTING, 40));
            float stoneFast = player.getDestroySpeed(stone, pos);
            helper.assertTrue(Math.abs(stoneFast / stoneBase - 1.6f) < 0.01f, "Bergbau mit 40 Punkten: " + fmt(stoneFast / stoneBase));
            helper.assertTrue(Math.abs(player.getDestroySpeed(log, pos) - logPick) < 1.0e-4f, "Die Spitzhacke fällt Holz schneller");
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_AXE));
            float logFast = player.getDestroySpeed(log, pos);
            HeroLevel.set(player, HeroLevel.Hero.NONE);
            float logBase = player.getDestroySpeed(log, pos);
            helper.assertTrue(Math.abs(logFast / logBase - 1.75f) < 0.01f, "Holzfällen mit 40 Punkten: " + fmt(logFast / logBase));
            // Bogenschießen
            LivingEntity target = ModEntities.ZOMBIE.get().create(level, EntitySpawnReason.EVENT);
            if (target == null) {
                throw new IllegalStateException("Kein Zombie");
            }
            Vec3 at = helper.absoluteVec(new Vec3(4.5, 1.0, 7.5));
            target.snapTo(at.x, at.y, at.z, 180.0f, 0.0f);
            level.addFreshEntity(target);
            target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(500.0);
            target.setHealth(500.0f);
            Arrow arrow = new Arrow(level, player, new ItemStack(Items.ARROW), new ItemStack(Items.BOW));
            float plain = hit(level, target, level.damageSources().arrow(arrow, player));
            HeroLevel.set(player, with(500, HeroLevel.Talent.ARCHERY, 40));
            float strong = hit(level, target, level.damageSources().arrow(arrow, player));
            helper.assertTrue(plain > 0.0f && Math.abs(strong / plain - 1.75f) < 0.02f, "Bogenschießen mit 40 Punkten: " + fmt(strong / plain));
            Arrow thrown = new Arrow(level, player, new ItemStack(Items.ARROW), null);
            float loose = hit(level, target, level.damageSources().arrow(thrown, player));
            helper.assertTrue(Math.abs(loose / plain - 1.0f) < 0.02f, "Ein Pfeil ohne Bogen trifft härter: " + fmt(loose / plain));
            helper.assertTrue(Math.abs(HeroLevel.archerySave(player) - 0.3f) < 1.0e-4f, "Pfeile gespart mit 40 Punkten: " + HeroLevel.archerySave(player));
            target.discard();
            helper.succeed();
        } finally {
            leave(helper, player);
        }
    }

    private static float hit(ServerLevel level, LivingEntity target, net.minecraft.world.damagesource.DamageSource source) {
        target.setInvulnerableTime(0);
        target.damageCooldownTime = 0;
        target.setHealth(target.getMaxHealth());
        float before = target.getHealth();
        target.hurtServer(level, source, 10.0f);
        return before - target.getHealth();
    }

    // =====================================================================================
    // Ernte, Erz und Holz
    // =====================================================================================

    /**
     * Ackerbau mit 40 Punkten: reifer Weizen gibt oft eine Ernte mehr (80 %) und wird oft gleich wieder ausgesät (50 %, wenn ein
     * Korn in der Ernte ist). Bergbau gemeistert: mitunter ein zweites Erz (15 %). Holzfällen gemeistert: mitunter ein Stamm mehr
     * (20 %). (Zufall – geprüft wird nur, was praktisch sicher ist.)
     */
    private static void harvest(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = hero(helper, new BlockPos(4, 1, 0), Items.IRON_HOE);
        try {
            clearDrops(helper);
            HeroLevel.set(player, with(500, HeroLevel.Talent.FARMING, 40, HeroLevel.Talent.MINING, 40, HeroLevel.Talent.WOODCUTTING, 40));
            CropBlock wheat = (CropBlock) Blocks.WHEAT;
            int crops = 0;
            int replanted = 0;
            for (int round = 0; round < 2; round++) {
                for (int x = 1; x <= 7; x++) {
                    for (int z = 2; z <= 8; z++) {
                        helper.setBlock(x, 1, z, Blocks.FARMLAND);
                        helper.setBlock(x, 2, z, wheat.getStateForAge(wheat.getMaxAge()));
                        BlockPos pos = helper.absolutePos(new BlockPos(x, 2, z));
                        player.gameMode.destroyBlock(pos);
                        crops++;
                        BlockState after = level.getBlockState(pos);
                        if (after.is(Blocks.WHEAT) && wheat.getAge(after) == 0) {
                            replanted++;
                        }
                    }
                }
            }
            int grain = count(helper, Items.WHEAT);
            helper.assertTrue(grain >= crops + crops / 2, "Ackerbau gibt zu selten eine Ernte mehr: " + grain + " Weizen aus " + crops + " Feldern");
            helper.assertTrue(replanted >= crops / 6, "Ackerbau sät zu selten wieder aus: " + replanted + " von " + crops);
            // Erz und Holz
            clearDrops(helper);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND_PICKAXE));
            int ores = mine(helper, player, Blocks.IRON_ORE.defaultBlockState(), 3);
            int raw = count(helper, Items.RAW_IRON);
            helper.assertTrue(raw > ores, "Bergbau gibt nie ein zweites Erz: " + raw + " Roheisen aus " + ores + " Erzen");
            clearDrops(helper);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND_AXE));
            int logs = mine(helper, player, Blocks.OAK_LOG.defaultBlockState(), 2);
            int got = count(helper, Items.OAK_LOG);
            helper.assertTrue(got > logs, "Holzfällen gibt nie einen Stamm mehr: " + got + " Stämme aus " + logs);
            clearDrops(helper);
            helper.succeed();
        } finally {
            leave(helper, player);
        }
    }

    /** Setzt Schicht um Schicht Blöcke und baut sie ab (gibt zurück, wie viele). */
    private static int mine(GameTestHelper helper, ServerPlayer player, BlockState state, int layers) {
        int n = 0;
        for (int y = 1; y <= layers; y++) {
            for (int x = 1; x <= 7; x++) {
                for (int z = 2; z <= 8; z++) {
                    helper.setBlock(x, y, z, Blocks.AIR);
                }
            }
        }
        for (int y = 1; y <= layers; y++) {
            for (int x = 1; x <= 7; x++) {
                for (int z = 2; z <= 8; z++) {
                    helper.setBlock(x, y, z, state);
                    player.gameMode.destroyBlock(helper.absolutePos(new BlockPos(x, y, z)));
                    n++;
                }
            }
        }
        return n;
    }

    // =====================================================================================
    // Fähigkeiten
    // =====================================================================================

    /**
     * Die Fähigkeiten: freigeschaltet erst auf ihrer Stufe (auch wählen lässt sich vorher keine), dann ruhen sie nach dem
     * Einsatz; Sturmlauf (Tempo II, Sprungkraft), Eiserne Haut (Resistenz II), Zweiter Atem (drei Zehntel Leben, kein Gift
     * mehr), Kampfschrei (Monster geschwächt, der eigene Wolf stärker), Wirbelschlag (trifft das Monster, nicht den Golem),
     * Adlerblick (das Monster leuchtet), Himmelssturz (hoch hinaus, die Landung trifft) und Letztes Gefecht (ein tödlicher
     * Schlag lässt ein halbes Herz).
     */
    private static void abilities(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = hero(helper, new BlockPos(4, 1, 4), Items.IRON_SWORD);
        List<Entity> spawned = new ArrayList<>();
        try {
            int last = 0;
            for (HeroAbility ability : HeroAbility.values()) {
                helper.assertTrue(ability.level() > last && ability.level() <= HeroLevel.MAX_LEVEL, "Stufen der Fähigkeiten: " + ability);
                last = ability.level();
            }
            // wer die erste Fähigkeit bekommt, erfährt es (und bekommt beim Einloggen keinen Hinweis mehr)
            player.setData(io.github.sirschrolli.kingdomsatwar.registry.ModAttachments.HERO_ABILITIES, HeroAbilities.State.NONE);
            HeroLevel.set(player, with(9));
            HeroLevel.award(player, HeroLevel.total(10) - HeroLevel.total(9));
            helper.assertTrue(HeroLevel.of(player).level() == 10 && HeroAbilities.state(player).introduced(),
                    "Mit der ersten Fähigkeit gilt der Held nicht als eingeführt");
            HeroLevel.set(player, with(9));
            helper.assertTrue(!HeroAbilities.use(player, HeroAbility.SPRINT), "Sturmlauf vor Stufe 10");
            helper.assertTrue(!HeroAbilities.select(player, HeroAbility.SKYFALL), "Himmelssturz lässt sich vor seiner Stufe wählen");
            HeroLevel.set(player, with(10));
            helper.assertTrue(HeroAbilities.use(player, HeroAbility.SPRINT), "Sturmlauf auf Stufe 10 geht nicht");
            MobEffectInstance speed = player.getEffect(MobEffects.SPEED);
            helper.assertTrue(speed != null && speed.getAmplifier() == 1 && player.hasEffect(MobEffects.JUMP_BOOST), "Sturmlauf wirkt nicht: " + speed);
            helper.assertTrue(HeroAbilities.cooldown(player, HeroAbility.SPRINT) > 0L && !HeroAbilities.use(player, HeroAbility.SPRINT),
                    "Sturmlauf ruht nicht");
            helper.assertTrue(!HeroAbilities.use(player, HeroAbility.WAR_CRY), "Kampfschrei vor Stufe 30");
            HeroLevel.set(player, with(500));
            helper.assertTrue(HeroAbilities.select(player, HeroAbility.SKYFALL) && HeroAbilities.state(player).selectedAbility() == HeroAbility.SKYFALL,
                    "Wählen geht nicht");
            // Eiserne Haut, Zweiter Atem
            helper.assertTrue(HeroAbilities.use(player, HeroAbility.IRON_SKIN), "Eiserne Haut geht nicht");
            MobEffectInstance resistance = player.getEffect(MobEffects.RESISTANCE);
            helper.assertTrue(resistance != null && resistance.getAmplifier() == 1, "Eiserne Haut wirkt nicht: " + resistance);
            player.removeAllEffects();
            player.setHealth(player.getMaxHealth() * 0.5f);
            player.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 1));
            float hp = player.getHealth();
            helper.assertTrue(HeroAbilities.use(player, HeroAbility.SECOND_WIND), "Zweiter Atem geht nicht");
            helper.assertTrue(player.getHealth() >= hp + player.getMaxHealth() * 0.29f && !player.hasEffect(MobEffects.POISON),
                    "Zweiter Atem heilt nicht oder nimmt das Gift nicht: " + player.getHealth());
            // ein Monster vor ihm, ein Golem daneben, sein Wolf dahinter
            LivingEntity zombie = creature(helper, ModEntities.ZOMBIE.get().create(level, EntitySpawnReason.EVENT), new Vec3(4.5, 1.0, 6.5), spawned);
            // (Golem und Wolf gleich in der Gestalt der Mod: die Tiere von Minecraft ersetzt sie beim Erscheinen)
            LivingEntity golem = creature(helper, ModEntities.IRON_GOLEM.get().create(level, EntitySpawnReason.EVENT), new Vec3(6.5, 1.0, 4.5),
                    spawned);
            net.minecraft.world.entity.animal.wolf.Wolf wolf = (net.minecraft.world.entity.animal.wolf.Wolf) creature(helper,
                    ModEntities.WOLF.get().create(level, EntitySpawnReason.EVENT), new Vec3(4.5, 1.0, 2.5), spawned);
            wolf.tame(player);
            helper.assertTrue(HeroAbilities.use(player, HeroAbility.WAR_CRY), "Kampfschrei geht nicht");
            helper.assertTrue(zombie.hasEffect(MobEffects.WEAKNESS) && zombie.hasEffect(MobEffects.SLOWNESS), "Der Kampfschrei schwächt das Monster nicht");
            helper.assertTrue(wolf.hasEffect(MobEffects.STRENGTH) && !golem.hasEffect(MobEffects.WEAKNESS),
                    "Der Kampfschrei stärkt den Wolf nicht oder schwächt den Golem");
            helper.assertTrue(HeroAbilities.use(player, HeroAbility.EAGLE_EYE), "Adlerblick geht nicht");
            helper.assertTrue(zombie.hasEffect(MobEffects.GLOWING) && !golem.hasEffect(MobEffects.GLOWING), "Der Adlerblick zeigt nicht das Monster");
            float zombieHp = zombie.getHealth();
            float golemHp = golem.getHealth();
            helper.assertTrue(HeroAbilities.use(player, HeroAbility.WHIRLWIND), "Wirbelschlag geht nicht");
            helper.assertTrue(zombie.getHealth() < zombieHp && golem.getHealth() >= golemHp && wolf.getHealth() >= wolf.getMaxHealth(),
                    "Der Wirbelschlag trifft die Falschen: Zombie " + zombieHp + " → " + zombie.getHealth() + ", Golem " + golem.getHealth());
            // Himmelssturz: hoch hinaus, und die Landung trifft
            zombie.setInvulnerableTime(0);
            zombie.damageCooldownTime = 0;
            zombieHp = zombie.getHealth();
            helper.assertTrue(HeroAbilities.use(player, HeroAbility.SKYFALL), "Himmelssturz geht nicht");
            helper.assertTrue(player.getDeltaMovement().y > 0.5 && HeroAbilities.falling(player), "Himmelssturz: kein Satz nach oben");
            HeroAbilities.land(player);
            helper.assertTrue(!HeroAbilities.falling(player) && zombie.getHealth() < zombieHp, "Die Landung trifft das Monster nicht");
            // Letztes Gefecht: ein tödlicher Schlag lässt ein halbes Herz
            player.setHealth(player.getMaxHealth());
            helper.assertTrue(HeroAbilities.use(player, HeroAbility.LAST_STAND) && HeroAbilities.standing(player), "Letztes Gefecht geht nicht");
            player.setInvulnerableTime(0);
            player.hurtServer(level, level.damageSources().magic(), 500.0f);
            helper.assertTrue(player.isAlive() && player.getHealth() >= 1.0f, "Im Letzten Gefecht fällt der Held: " + player.getHealth());
            // alle ruhen jetzt; aufgefrischt sind sie wieder bereit
            for (HeroAbility ability : HeroAbility.values()) {
                if (ability != HeroAbility.SPRINT) {
                    helper.assertTrue(HeroAbilities.cooldown(player, ability) > 0L, ability + " ruht nicht");
                }
            }
            HeroAbilities.refresh(player);
            helper.assertTrue(HeroAbilities.cooldown(player, HeroAbility.LAST_STAND) == 0L, "Auffrischen geht nicht");
            helper.succeed();
        } finally {
            spawned.forEach(Entity::discard);
            leave(helper, player);
        }
    }

    private static LivingEntity creature(GameTestHelper helper, LivingEntity entity, Vec3 at, List<Entity> spawned) {
        if (entity == null) {
            throw new IllegalStateException("Kein Wesen");
        }
        Vec3 p = helper.absoluteVec(at);
        entity.snapTo(p.x, p.y, p.z, 0.0f, 0.0f);
        if (entity instanceof net.minecraft.world.entity.Mob mob) {
            mob.setNoAi(true);
        }
        helper.getLevel().addFreshEntity(entity);
        spawned.add(entity);
        return entity;
    }

    // =====================================================================================
    // Titel
    // =====================================================================================

    /** Die Titel: alle fünfzig Stufen hinter dem Unsterblichen, der letzte auf Stufe 500; der Unsterbliche bleibt der letzte Rang. */
    private static void titles(GameTestHelper helper) {
        helper.assertTrue(HeroLevel.Title.of(HeroLevel.IMMORTAL_LEVEL) == null && HeroLevel.Title.of(249) == null, "Ein Titel vor Stufe 250");
        helper.assertTrue(HeroLevel.Title.of(250) == HeroLevel.Title.LIGHTBRINGER && HeroLevel.Title.of(499) == HeroLevel.Title.WORLDKEEPER
                && HeroLevel.Title.of(HeroLevel.MAX_LEVEL) == HeroLevel.Title.ETERNAL, "Titel je Stufe");
        int last = HeroLevel.IMMORTAL_LEVEL;
        for (HeroLevel.Title title : HeroLevel.Title.values()) {
            helper.assertTrue(title.minLevel() == last + 50, "Titel nicht alle fünfzig Stufen: " + title);
            last = title.minLevel();
        }
        helper.assertTrue(HeroLevel.Rank.of(HeroLevel.MAX_LEVEL) == HeroLevel.Rank.IMMORTAL, "Der Unsterbliche ist nicht mehr der letzte Rang");
        helper.assertTrue(new HeroLevel.Hero(HeroLevel.total(300), List.of(), 0).title() == HeroLevel.Title.DRAGONSLAYER, "Der Titel des Helden");
        helper.succeed();
    }
}
