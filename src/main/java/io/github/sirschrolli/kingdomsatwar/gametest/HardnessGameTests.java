package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.economy.citizen.Trading;
import io.github.sirschrolli.kingdomsatwar.entity.creature.DeerEntity;
import io.github.sirschrolli.kingdomsatwar.entity.realm.RealmWarriorEntity;
import io.github.sirschrolli.kingdomsatwar.loot.LeanDropsLootModifier;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import io.github.sirschrolli.kingdomsatwar.registry.MonsterGear;
import io.github.sirschrolli.kingdomsatwar.rules.HardCombat;
import io.github.sirschrolli.kingdomsatwar.rules.HardWorld;
import io.github.sirschrolli.kingdomsatwar.rules.Hardness;
import io.github.sirschrolli.kingdomsatwar.war.Realm;
import io.github.sirschrolli.kingdomsatwar.war.WarriorRole;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.feature.AbstractOreFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Tests zum Härte-Paket (1.3): heilende Bosse und Monster, giftige Treffer, zersplitternde Pfeile, seltenere Erze,
 * langsamere Tierzucht, kürzere Tränke, teureres Verzaubern, teurere Händler, seltenere Monsterbeute und der Hirsch, der
 * sich wehrt. Auf dem Testserver gilt die Härte nicht von selbst – die Regeln werden hier einzeln aufgerufen.
 */
final class HardnessGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> COMBAT =
            ModGameTests.TEST_FUNCTIONS.register("hard_combat", () -> HardnessGameTests::combat);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> WORLD =
            ModGameTests.TEST_FUNCTIONS.register("hard_world", () -> HardnessGameTests::world);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LOOT =
            ModGameTests.TEST_FUNCTIONS.register("hard_loot", () -> HardnessGameTests::loot);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> DEER =
            ModGameTests.TEST_FUNCTIONS.register("hard_deer", () -> HardnessGameTests::deer);

    private HardnessGameTests() {}

    /** Lädt die Klasse (meldet die Testfunktionen an). */
    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var test : List.of(COMBAT, WORLD, LOOT, DEER)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 200, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
    }

    /**
     * Monster heilen in Ruhe ein halbes Herz je Sekunde (nicht gleich nach einem Treffer); ein Boss heilt erst, wenn niemand
     * mehr in seiner Nähe ist, dann 2 % je Sekunde. Giftige Treffer: Zombies machen hungrig, Spinnen vergiften,
     * Skelettpfeile lähmen, andere Monster schwächen – Bosse und Tiere nicht. Pfeile von Spielern zersplittern zur Hälfte.
     */
    private static void combat(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        List<String> problems = new ArrayList<>();
        BlockPos high = helper.absolutePos(new BlockPos(4, 120, 4));
        Mob spider = ModEntities.SPIDER.get().create(level, EntitySpawnReason.MOB_SUMMONED);
        Mob zombie = ModEntities.ZOMBIE.get().create(level, EntitySpawnReason.MOB_SUMMONED);
        Mob skeleton = ModEntities.SKELETON.get().create(level, EntitySpawnReason.MOB_SUMMONED);
        Mob werewolf = ModEntities.WEREWOLF.get().create(level, EntitySpawnReason.MOB_SUMMONED);
        Mob deer = ModEntities.DEER.get().create(level, EntitySpawnReason.MOB_SUMMONED);
        RealmWarriorEntity lord = ModEntities.REALM_WARRIORS.get(WarriorRole.LORD).get().create(level, EntitySpawnReason.MOB_SUMMONED);
        ServerPlayer player = MageGameTests.survivalPlayer(helper, "test-hard-fighter", new io.netty.channel.embedded.EmbeddedChannel[1], 0);
        try {
            if (Hardness.active(level.getServer()) || HardWorld.active()) {
                problems.add("Härte auf dem Testserver aktiv");
            }
            // --- Monster heilen sich ---
            spider.snapTo(high.getX() + 0.5, high.getY(), high.getZ() + 0.5, 0.0f, 0.0f);
            spider.setHealth(4.0f);
            float healed = HardCombat.regenerate(level, spider);
            if (Math.abs(healed - HardCombat.MONSTER_REGENERATION) > 1.0e-4f) {
                problems.add("Monster heilt in Ruhe nicht ein halbes Herz: " + healed);
            }
            spider.hurtServer(level, level.damageSources().generic(), 1.0f);
            if (HardCombat.regenerate(level, spider) != 0.0f) {
                problems.add("Monster heilt gleich nach einem Treffer");
            }
            deer.setHealth(4.0f);
            if (HardCombat.regenerate(level, deer) != 0.0f) {
                problems.add("Tiere heilen wie Monster");
            }

            // --- Bosse heilen sich, wenn niemand mehr da ist ---
            lord.setRealm(Realm.SILBERAU);
            lord.snapTo(high.getX() + 0.5, high.getY(), high.getZ() + 0.5, 0.0f, 0.0f);
            lord.setHealth(lord.getMaxHealth() * 0.5f);
            player.snapTo(high.getX() + 3.5, high.getY(), high.getZ() + 0.5, 0.0f, 0.0f);
            if (HardCombat.regenerate(level, lord) != 0.0f) {
                problems.add("Boss heilt, obwohl jemand bei ihm ist");
            }
            player.snapTo(high.getX() + 0.5, high.getY() - 110, high.getZ() + 0.5, 0.0f, 0.0f);
            float bossHealed = HardCombat.regenerate(level, lord);
            if (Math.abs(bossHealed - lord.getMaxHealth() * HardCombat.BOSS_REGENERATION) > 0.01f) {
                problems.add("Boss heilt allein nicht 2 % je Sekunde: " + bossHealed + " von " + lord.getMaxHealth());
            }

            // --- Giftige Treffer ---
            RandomSource random = RandomSource.create(1303L);
            Arrow bone = new Arrow(level, skeleton, new ItemStack(Items.ARROW), null);
            int[] hits = new int[5];
            for (int i = 0; i < 1000; i++) {
                hits[0] += has(HardCombat.venom(zombie, zombie, random), MobEffects.HUNGER);
                hits[1] += has(HardCombat.venom(spider, spider, random), MobEffects.POISON);
                hits[2] += has(HardCombat.venom(skeleton, bone, random), MobEffects.SLOWNESS);
                hits[3] += has(HardCombat.venom(werewolf, werewolf, random), MobEffects.WEAKNESS);
                hits[4] += HardCombat.venom(lord, lord, random) != null || HardCombat.venom(deer, deer, random) != null ? 1 : 0;
            }
            if (hits[0] < 230 || hits[0] > 370 || hits[1] < 230 || hits[1] > 370 || hits[2] < 190 || hits[2] > 310
                    || hits[3] < 100 || hits[3] > 200 || hits[4] != 0) {
                problems.add("giftige Treffer (Hunger, Gift, Langsamkeit, Schwäche, Boss/Tier) in 1000: " + java.util.Arrays.toString(hits));
            }

            // --- Pfeile zerbrechen ---
            Arrow own = new Arrow(level, player, new ItemStack(Items.ARROW), null);
            int broken = 0;
            for (int i = 0; i < 1000; i++) {
                broken += HardCombat.breaks(own, random) ? 1 : 0;
            }
            if (own.pickup != AbstractArrow.Pickup.ALLOWED || broken < 430 || broken > 570) {
                problems.add("Pfeile des Spielers zersplittern nicht etwa jedes zweite Mal: " + broken + " von 1000 (" + own.pickup + ")");
            }
            Arrow infinite = new Arrow(level, player, new ItemStack(Items.ARROW), null);
            infinite.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            for (int i = 0; i < 100; i++) {
                if (HardCombat.breaks(bone, random) || HardCombat.breaks(infinite, random)) {
                    problems.add("Pfeile der Skelette oder unendliche Pfeile zersplittern");
                    break;
                }
            }
            HardCombat.shatter(level, own);
            if (!own.isRemoved()) {
                problems.add("Zersplitterter Pfeil bleibt liegen");
            }
        } finally {
            for (Mob mob : List.of(spider, zombie, skeleton, werewolf, deer, lord)) {
                mob.discard();
            }
            level.getServer().getPlayerList().remove(player);
        }
        helper.assertTrue(problems.isEmpty(), "Härte im Kampf: " + problems);
        helper.succeed();
    }

    private static int has(MobEffectInstance effect, Holder<net.minecraft.world.effect.MobEffect> type) {
        return effect != null && effect.getEffect().equals(type) ? 1 : 0;
    }

    /**
     * Erze: Erzadern (Eisen, Feueropal) zählen, Kies nicht; mit der Härte bleibt etwa jede zweite aus. Jungtiere brauchen
     * doppelt so lange, Eltern warten doppelt so lange. Tränke wirken halb so lange. Der Zaubertisch will doppelt so viel
     * Lapislazuli. Händler verlangen anderthalbmal so viel und zahlen 70 %, Karten noch einmal das Doppelte.
     */
    private static void world(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        List<String> problems = new ArrayList<>();
        var features = level.registryAccess().lookupOrThrow(Registries.FEATURE);
        Feature iron = features.getOrThrow(ResourceKey.create(Registries.FEATURE, Identifier.withDefaultNamespace("ore_iron"))).value();
        Feature gravel = features.getOrThrow(ResourceKey.create(Registries.FEATURE, Identifier.withDefaultNamespace("ore_gravel"))).value();
        Feature opal = features.getOrThrow(ResourceKey.create(Registries.FEATURE, KingdomsAtWar.id("ore_fire_opal"))).value();
        if (!(iron instanceof AbstractOreFeature ironOre) || !HardWorld.isOre(ironOre) || !(gravel instanceof AbstractOreFeature gravelOre)
                || HardWorld.isOre(gravelOre) || !(opal instanceof AbstractOreFeature opalOre) || !HardWorld.isOre(opalOre)) {
            problems.add("Erzadern falsch erkannt: " + iron + ", " + gravel + ", " + opal);
        } else {
            RandomSource random = RandomSource.create(26L);
            int skipped = 0;
            for (int i = 0; i < 1000; i++) {
                skipped += HardWorld.skipOre(ironOre, random, true) ? 1 : 0;
                if (HardWorld.skipOre(gravelOre, random, true) || HardWorld.skipOre(ironOre, random, false)) {
                    problems.add("Kies fällt weg oder Erz ohne Härte");
                    break;
                }
            }
            if (skipped < 430 || skipped > 570) {
                problems.add("Nicht etwa jede zweite Erzader fällt weg: " + skipped + " von 1000");
            }
            if (HardWorld.skipOre(ironOre, RandomSource.create(1L))) {
                problems.add("Seltenere Erze auf dem Testserver");
            }
        }

        // --- langsamere Tierzucht ---
        DeerEntity fawn = ModEntities.DEER.get().create(level, EntitySpawnReason.BREEDING);
        DeerEntity parent = ModEntities.DEER.get().create(level, EntitySpawnReason.MOB_SUMMONED);
        try {
            fawn.setAge(AgeableMob.BABY_START_AGE);
            boolean slowed = HardWorld.slowGrowth(fawn);
            boolean again = HardWorld.slowGrowth(fawn);
            if (!slowed || again || fawn.getAge() != AgeableMob.BABY_START_AGE * 2) {
                problems.add("Jungtier wächst nicht halb so schnell: " + slowed + "/" + again + ", Alter " + fawn.getAge());
            }
            parent.setAge(6000);
            HardWorld.restParent(parent);
            if (parent.getAge() != 12000) {
                problems.add("Eltern warten nicht doppelt so lange: " + parent.getAge());
            }
        } finally {
            fawn.discard();
            parent.discard();
        }

        // --- kürzere Tränke ---
        ItemStack swiftness = PotionContents.createItemStack(Items.POTION, Potions.SWIFTNESS);
        float scale = swiftness.getOrDefault(DataComponents.POTION_DURATION_SCALE, 1.0f);
        float splash = PotionContents.createItemStack(Items.SPLASH_POTION, Potions.SWIFTNESS).getOrDefault(DataComponents.POTION_DURATION_SCALE, 1.0f);
        float lingering = PotionContents.createItemStack(Items.LINGERING_POTION, Potions.SWIFTNESS)
                .getOrDefault(DataComponents.POTION_DURATION_SCALE, 1.0f);
        float tipped = PotionContents.createItemStack(Items.TIPPED_ARROW, Potions.SWIFTNESS).getOrDefault(DataComponents.POTION_DURATION_SCALE, 1.0f);
        if (scale != 0.5f || splash != 0.5f || lingering != 0.125f || tipped != 0.0625f) {
            problems.add("Trankdauer: " + scale + "/" + splash + "/" + lingering + "/" + tipped);
        }
        ServerPlayer drinker = helper.makeMockServerPlayerInLevel();
        try {
            swiftness.get(DataComponents.POTION_CONTENTS).applyToLivingEntity(drinker, scale);
            MobEffectInstance speed = drinker.getEffect(MobEffects.SPEED);
            if (speed == null || speed.getDuration() != 1800) {
                problems.add("Trank der Schnelligkeit wirkt nicht anderthalb Minuten: " + speed);
            }
        } finally {
            level.getServer().getPlayerList().remove(drinker);
        }

        // --- teureres Verzaubern ---
        if (HardWorld.lapisUnits(5, true) != 2 || HardWorld.lapisUnits(6, true) != 3 || HardWorld.lapisUnits(5, false) != 5
                || HardWorld.lapisCost(3, true) != 6 || HardWorld.lapisCost(3, false) != 3) {
            problems.add("Lapislazuli: " + HardWorld.lapisUnits(5, true) + "/" + HardWorld.lapisCost(3, true));
        }
        MutableComponent one = HardWorld.lapisText(Component.translatable("container.enchant.lapis.one"), true);
        MutableComponent three = HardWorld.lapisText(Component.translatable("container.enchant.lapis.many", 3), true);
        if (!(one.getContents() instanceof TranslatableContents a) || !a.getKey().equals("container.enchant.lapis.many") || !a.getArgs()[0].equals(2)
                || !(three.getContents() instanceof TranslatableContents b) || !b.getArgs()[0].equals(6)) {
            problems.add("Zaubertisch nennt den falschen Preis: " + one.getString() + " / " + three.getString());
        }

        // --- teurere Händler ---
        if (Trading.pay(100, 0, Trading.HARD_BUY) != 150 || Trading.pay(100, 20, Trading.HARD_BUY) != 120
                || Trading.earn(10, 0, Trading.HARD_SELL) != 7 || Trading.earn(1, 0, Trading.HARD_SELL) != 1
                || Trading.pay(100, 0, 1.0f) != 100 || Trading.earn(10, 0, 1.0f) != 10) {
            problems.add("Händlerpreise: " + Trading.pay(100, 0, Trading.HARD_BUY) + "/" + Trading.earn(10, 0, Trading.HARD_SELL));
        }
        float map = Trading.buyFactor(Trading.Offer.special("nest_map", ItemStack.EMPTY, 120));
        float bread = Trading.buyFactor(Trading.Offer.buy(Items.BREAD, 1, 5));
        if (Math.abs(map - 3.0f) > 1.0e-4f || Math.abs(bread - 1.5f) > 1.0e-4f) {
            problems.add("Karten nicht doppelt so teuer: " + map + "/" + bread);
        }
        helper.assertTrue(problems.isEmpty(), "Härte der Welt: " + problems);
        helper.succeed();
    }

    /**
     * Seltenere Monsterbeute: Von dem, was Monster fallen lassen, bleibt ein gutes Drittel aus – nicht aber, was man für den
     * Fortgang braucht (Lohenruten), Münzen und die Teile der eigenen Monster (die sind in ihren Tabellen seltener).
     */
    private static void loot(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        LeanDropsLootModifier lean = new LeanDropsLootModifier(Optional.empty(), 0, 0.35f);
        LootParams params = new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN, helper.absoluteVec(net.minecraft.world.phys.Vec3.ZERO))
                .create(LootContextParamSets.CHEST);
        LootContext context = new LootContext.Builder(params).create(Optional.empty());
        ObjectArrayList<ItemStack> drops = new ObjectArrayList<>();
        for (int i = 0; i < 1000; i++) {
            drops.add(new ItemStack(Items.BONE));
        }
        for (int i = 0; i < 100; i++) {
            drops.add(new ItemStack(Items.BLAZE_ROD));
            drops.add(new ItemStack(ModItems.BRONZE_COIN.get()));
            drops.add(new ItemStack(MonsterGear.WEREWOLF_PELT.get()));
        }
        ObjectArrayList<ItemStack> kept = lean.apply(drops, context);
        int bones = 0;
        int spared = 0;
        for (ItemStack stack : kept) {
            if (stack.is(Items.BONE)) {
                bones++;
            } else {
                spared++;
            }
        }
        helper.assertTrue(bones >= 580 && bones <= 720 && spared == 300,
                "Monsterbeute: " + bones + " von 1000 Knochen, " + spared + " von 300 Lohenruten/Münzen/Pelzen");
        helper.succeed();
    }

    /**
     * Der Hirsch wehrt sich: Greift ein Spieler ihn an, geht er mit gesenktem Geweih auf ihn los; die Hirschkuh flieht nur.
     */
    private static void deer(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (int x = 0; x < 9; x++) {
            for (int z = 0; z < 9; z++) {
                helper.setBlock(x, 0, z, Blocks.GRASS_BLOCK);
            }
        }
        DeerEntity stag = helper.spawn(ModEntities.DEER.get(), new BlockPos(2, 1, 2));
        DeerEntity doe = helper.spawn(ModEntities.DEER.get(), new BlockPos(6, 1, 6));
        stag.setStag(true);
        stag.setAge(0);
        doe.setStag(false);
        doe.setAge(0);
        DeerEntity fawn = ModEntities.DEER.get().create(level, EntitySpawnReason.BREEDING);
        fawn.setStag(true);
        fawn.setAge(AgeableMob.BABY_START_AGE);
        helper.assertTrue(stag.fights() && !doe.fights() && !fawn.fights(), "Nur der erwachsene Hirsch kämpft");
        fawn.discard();
        ServerPlayer hunter = MageGameTests.survivalPlayer(helper, "test-hard-hunter", new io.netty.channel.embedded.EmbeddedChannel[1], 0);
        hunter.getAbilities().invulnerable = false;   // der Testserver gibt jedem Spieler erst die Fähigkeiten des Kreativmodus
        hunter.getAbilities().instabuild = false;
        BlockPos stand = helper.absolutePos(new BlockPos(4, 1, 4));
        hunter.teleportTo(level, stand.getX() + 0.5, stand.getY(), stand.getZ() + 0.5, java.util.Set.of(), 0.0f, 0.0f, true);
        level.getChunkSource().move(hunter);
        // erst nach ein paar Ticks: Minecraft merkt sich den Angreifer mit dem Alter des Opfers (bei 0 sähe es nichts Neues)
        helper.runAfterDelay(5, () -> {
            stag.hurtServer(level, level.damageSources().playerAttack(hunter), 1.0f);
            doe.hurtServer(level, level.damageSources().playerAttack(hunter), 1.0f);
        });
        helper.onEachTick(() -> {
            if (stag.getTarget() == hunter && stag.isFighting()) {
                helper.assertTrue(doe.getTarget() == null && !doe.isFighting(), "Die Hirschkuh greift an");
                level.getServer().getPlayerList().remove(hunter);
                helper.succeed();
            }
        });
    }
}
