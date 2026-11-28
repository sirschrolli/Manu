package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.item.armory.ArmoryMining;
import io.github.sirschrolli.kingdomsatwar.item.armory.DaggerItem;
import io.github.sirschrolli.kingdomsatwar.item.armory.EmberPickaxeItem;
import io.github.sirschrolli.kingdomsatwar.item.armory.GreatswordItem;
import io.github.sirschrolli.kingdomsatwar.item.armory.HalberdItem;
import io.github.sirschrolli.kingdomsatwar.item.armory.OreGlowEntity;
import io.github.sirschrolli.kingdomsatwar.item.armory.OreSeekerItem;
import io.github.sirschrolli.kingdomsatwar.item.armory.WarhammerItem;
import io.github.sirschrolli.kingdomsatwar.registry.Armory;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.AttackRange;
import net.minecraft.world.item.component.Weapon;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * Tests der Waffenkammer (0.84): Rezepte und Einordnung, die Fähigkeiten der fünf Waffen (Durchbohren, Erdstoß, Stich von
 * hinten, Spaltschlag, Schilde lahmlegen) und der vier Spitzhacken (drei mal drei, Schmelzen, ganze Adern, Erze aufspüren).
 */
final class ArmoryGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> BASICS =
            ModGameTests.TEST_FUNCTIONS.register("armory_basics", () -> ArmoryGameTests::basics);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> HALBERD =
            ModGameTests.TEST_FUNCTIONS.register("armory_halberd", () -> ArmoryGameTests::halberd);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> WARHAMMER =
            ModGameTests.TEST_FUNCTIONS.register("armory_warhammer", () -> ArmoryGameTests::warhammer);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> DAGGER =
            ModGameTests.TEST_FUNCTIONS.register("armory_dagger", () -> ArmoryGameTests::dagger);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> GREATSWORD =
            ModGameTests.TEST_FUNCTIONS.register("armory_greatsword", () -> ArmoryGameTests::greatsword);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> HAMMER =
            ModGameTests.TEST_FUNCTIONS.register("armory_miner_hammer", () -> ArmoryGameTests::minerHammer);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> VEIN =
            ModGameTests.TEST_FUNCTIONS.register("armory_vein_breaker", () -> ArmoryGameTests::veinBreaker);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> EMBER =
            ModGameTests.TEST_FUNCTIONS.register("armory_ember_pickaxe", () -> ArmoryGameTests::emberPickaxe);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SEEKER =
            ModGameTests.TEST_FUNCTIONS.register("armory_ore_seeker", () -> ArmoryGameTests::oreSeeker);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SHIELDS =
            ModGameTests.TEST_FUNCTIONS.register("armory_shields", () -> ArmoryGameTests::shields);

    private ArmoryGameTests() {}

    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var test : List.of(BASICS, HALBERD, WARHAMMER, DAGGER, GREATSWORD, HAMMER, VEIN, EMBER, SHIELDS)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 200, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
        event.registerTest(SEEKER.getId(), new FunctionGameTestInstance(SEEKER.key(),
                new TestData<>(environment, Level.OVERWORLD, ARENA, OreGlowEntity.LIFETIME + 60, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
    }

    // =====================================================================================
    // Hilfen
    // =====================================================================================

    private static void floor(GameTestHelper helper) {
        for (int x = 0; x < 9; x++) {
            for (int z = 0; z < 9; z++) {
                helper.setBlock(x, 0, z, Blocks.STONE);
                for (int y = 1; y < 5; y++) {
                    helper.setBlock(x, y, z, Blocks.AIR);
                }
            }
        }
    }

    private static AABB area(GameTestHelper helper) {
        return new AABB(Vec3.atLowerCornerOf(helper.absolutePos(new BlockPos(-2, -1, -2))),
                Vec3.atLowerCornerOf(helper.absolutePos(new BlockPos(11, 10, 11))));
    }

    private static void cleanup(GameTestHelper helper) {
        helper.getLevel().getEntitiesOfClass(Entity.class, area(helper), e -> !(e instanceof Player)).forEach(Entity::discard);
    }

    /** Ein Ritter, der in Richtung {@code yaw} schaut (0 = Süden, +z). */
    private static Mob knight(GameTestHelper helper, BlockPos pos, float yaw, Item weapon) {
        Mob knight = helper.spawnWithNoFreeWill(ModEntities.SOLDIERS.get(SoldierType.KNIGHT).get(), pos);
        Vec3 at = helper.absoluteVec(Vec3.atBottomCenterOf(pos));
        knight.snapTo(at.x, at.y, at.z, yaw, 0.0f);
        knight.setYHeadRot(yaw);
        knight.setYBodyRot(yaw);
        knight.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(weapon));
        return knight;
    }

    /** Ein Gegner ohne eigenen Willen (Wüstenzombie: brennt nicht in der Sonne). */
    private static Mob dummy(GameTestHelper helper, BlockPos pos, float yaw) {
        Mob mob = helper.spawnWithNoFreeWill(ModEntities.HUSK.get(), pos);
        Vec3 at = helper.absoluteVec(Vec3.atBottomCenterOf(pos));
        mob.snapTo(at.x, at.y, at.z, yaw, 0.0f);
        mob.setYHeadRot(yaw);
        mob.setYBodyRot(yaw);
        return mob;
    }

    private static boolean hurt(LivingEntity e) {
        return e.getHealth() < e.getMaxHealth();
    }

    /** Ein Spieler im Überlebensmodus an {@code pos} (relativ), Blick nach Süden (+z), waagrecht. */
    private static ServerPlayer miner(GameTestHelper helper, BlockPos pos, Item tool) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        Vec3 at = helper.absoluteVec(Vec3.atBottomCenterOf(pos));
        player.snapTo(at.x, at.y, at.z, 0.0f, 0.0f);
        player.setYHeadRot(0.0f);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(tool));
        return player;
    }

    private static void remove(GameTestHelper helper, ServerPlayer player) {
        helper.getLevel().getServer().getPlayerList().remove(player);
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

    // =====================================================================================
    // Tests
    // =====================================================================================

    /** Jede Waffe und jede Spitzhacke hat ihr Rezept, ist als Schwert/Axt/Spitzhacke verzauberbar und trägt ihre Werte. */
    private static void basics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var manager = level.getServer().getRecipeManager();
        List<DeferredItem<? extends Item>> all = new ArrayList<>(Armory.weapons());
        all.addAll(Armory.pickaxes());
        for (DeferredItem<? extends Item> item : all) {
            String name = item.getId().getPath();
            ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, KingdomsAtWar.id(name));
            helper.assertTrue(manager.byKey(key).isPresent(), "Kein Rezept für " + name);
            helper.assertTrue(new ItemStack(item.get()).has(DataComponents.LORE), "Keine Beschreibung bei " + name);
        }
        for (DeferredItem<? extends Item> sword : List.of(Armory.STEEL_HALBERD, Armory.DRACONIUM_WARHAMMER, Armory.NIGHTSTEEL_DAGGER,
                Armory.ABYSSITE_GREATSWORD)) {
            helper.assertTrue(new ItemStack(sword.get()).is(ItemTags.SWORDS), sword.getId() + " ist kein Schwert");
        }
        helper.assertTrue(new ItemStack(Armory.STEEL_DOUBLE_AXE.get()).is(ItemTags.AXES), "Die Doppelaxt ist keine Axt");
        for (DeferredItem<? extends Item> pick : Armory.pickaxes()) {
            helper.assertTrue(new ItemStack(pick.get()).is(ItemTags.PICKAXES), pick.getId() + " ist keine Spitzhacke");
            helper.assertTrue(new ItemStack(pick.get()).isCorrectToolForDrops(Blocks.DIAMOND_ORE.defaultBlockState()),
                    pick.getId() + " baut kein Diamanterz ab");
        }
        AttackRange range = new ItemStack(Armory.STEEL_HALBERD.get()).get(DataComponents.ATTACK_RANGE);
        helper.assertTrue(range != null && range.maxReach() == HalberdItem.REACH, "Die Hellebarde reicht nicht weiter: " + range);
        Weapon axe = new ItemStack(Armory.STEEL_DOUBLE_AXE.get()).get(DataComponents.WEAPON);
        helper.assertTrue(axe != null && axe.disableBlockingForSeconds() >= 10.0f, "Die Doppelaxt legt Schilde nicht zehn Sekunden lahm: " + axe);
        helper.assertTrue(new ItemStack(Armory.STEEL_DOUBLE_AXE.get()).has(DataComponents.BLOCK_TRANSFORMER), "Die Doppelaxt entrindet nicht");
        helper.assertTrue(new ItemStack(Armory.ABYSSITE_GREATSWORD.get()).has(DataComponents.DAMAGE_RESISTANT), "Das Großschwert verbrennt");
        // ohne seitlichen Schwung (die Fähigkeiten ersetzen ihn)
        for (DeferredItem<? extends Item> item : List.of(Armory.STEEL_HALBERD, Armory.ABYSSITE_GREATSWORD, Armory.DRACONIUM_WARHAMMER)) {
            helper.assertTrue(!new ItemStack(item.get()).canPerformAction(net.neoforged.neoforge.common.ItemAbilities.SWORD_SWEEP),
                    item.getId() + " schwingt wie ein Schwert");
        }
        helper.succeed();
    }

    /** Hellebarde: zwei Gegner hinter dem Ziel werden mitgetroffen, einer daneben nicht. */
    private static void halberd(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper);
        Mob knight = knight(helper, new BlockPos(4, 1, 1), 0.0f, Armory.STEEL_HALBERD.get());
        Mob target = dummy(helper, new BlockPos(4, 1, 3), 0.0f);
        Mob second = dummy(helper, new BlockPos(4, 1, 4), 0.0f);
        Mob third = dummy(helper, new BlockPos(4, 1, 5), 0.0f);
        Mob fourth = dummy(helper, new BlockPos(4, 1, 6), 0.0f);
        Mob aside = dummy(helper, new BlockPos(7, 1, 3), 0.0f);
        List<LivingEntity> hit = HalberdItem.pierce(level, target, knight);
        helper.assertTrue(hit.size() == HalberdItem.EXTRA_TARGETS && hit.contains(second) && hit.contains(third),
                "Die Hellebarde durchbohrt nicht die zwei dahinter: " + hit);
        helper.assertTrue(hurt(second) && hurt(third) && !hurt(fourth) && !hurt(aside) && !hurt(target),
                "Falsche Gegner getroffen");
        cleanup(helper);
        helper.succeed();
    }

    /** Kriegshammer: der Erdstoß trifft, entzündet und schleudert alles im Umkreis – nicht weiter; zu kurz gehalten gibt es keinen. */
    private static void warhammer(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper);
        ServerPlayer player = miner(helper, new BlockPos(4, 1, 4), Armory.DRACONIUM_WARHAMMER.get());
        try {
            Mob near = dummy(helper, new BlockPos(6, 1, 4), 0.0f);
            Mob close = dummy(helper, new BlockPos(4, 1, 2), 0.0f);
            Mob far = dummy(helper, new BlockPos(4, 1, 9), 0.0f);
            ItemStack hammer = player.getMainHandItem();
            WarhammerItem item = (WarhammerItem) hammer.getItem();
            int duration = item.getUseDuration(hammer, player);
            helper.assertTrue(!item.releaseUsing(hammer, level, player, duration - 4), "Ein kurzes Antippen löst schon den Erdstoß aus");
            helper.assertTrue(!hurt(near) && !hurt(close), "Ein kurzes Antippen trifft schon");
            helper.assertTrue(item.releaseUsing(hammer, level, player, duration - WarhammerItem.CHARGE_TICKS - 2), "Kein Erdstoß");
            helper.assertTrue(hurt(near) && hurt(close) && near.isOnFire() && close.isOnFire(), "Der Erdstoß trifft oder entzündet nicht");
            helper.assertTrue(near.getDeltaMovement().y > 0.3 && near.getDeltaMovement().x > 0.2, "Der Erdstoß schleudert nicht fort: "
                    + near.getDeltaMovement());
            helper.assertTrue(!hurt(far) && !far.isOnFire(), "Der Erdstoß reicht zu weit");
            helper.assertTrue(player.getCooldowns().isOnCooldown(hammer), "Der Hammer ruht nicht nach dem Erdstoß");
            helper.assertTrue(hammer.getDamageValue() > 0, "Der Erdstoß kostet keine Haltbarkeit");
        } finally {
            remove(helper, player);
        }
        cleanup(helper);
        helper.succeed();
    }

    /** Dolch: von hinten doppelter Schaden, von vorn und von der Seite nicht. */
    private static void dagger(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper);
        Mob target = dummy(helper, new BlockPos(4, 1, 4), 0.0f);                 // schaut nach Süden (+z)
        Mob behind = knight(helper, new BlockPos(4, 1, 2), 0.0f, Armory.NIGHTSTEEL_DAGGER.get());
        Mob front = knight(helper, new BlockPos(4, 1, 6), 180.0f, Armory.NIGHTSTEEL_DAGGER.get());
        Mob side = knight(helper, new BlockPos(6, 1, 4), 90.0f, Armory.NIGHTSTEEL_DAGGER.get());
        Item dagger = Armory.NIGHTSTEEL_DAGGER.get();
        helper.assertTrue(DaggerItem.isBehind(behind, target) && !DaggerItem.isBehind(front, target) && !DaggerItem.isBehind(side, target),
                "Hinten und vorn werden verwechselt");
        float back = dagger.getAttackDamageBonus(target, 6.0f, level.damageSources().mobAttack(behind));
        float ahead = dagger.getAttackDamageBonus(target, 6.0f, level.damageSources().mobAttack(front));
        helper.assertTrue(back == 6.0f && ahead == 0.0f, "Von hinten kein doppelter Schaden (" + back + "/" + ahead + ")");
        cleanup(helper);
        helper.succeed();
    }

    /** Großschwert: ein voller Schlag trifft die Gegner im Halbkreis vorn, nicht den hinter dem Träger. */
    private static void greatsword(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper);
        Mob knight = knight(helper, new BlockPos(4, 1, 2), 0.0f, Armory.ABYSSITE_GREATSWORD.get());
        Mob target = dummy(helper, new BlockPos(4, 1, 4), 0.0f);
        Mob left = dummy(helper, new BlockPos(2, 1, 3), 0.0f);
        Mob right = dummy(helper, new BlockPos(6, 1, 4), 0.0f);
        Mob back = dummy(helper, new BlockPos(4, 1, 0), 0.0f);
        Mob far = dummy(helper, new BlockPos(4, 1, 8), 0.0f);
        List<LivingEntity> hit = GreatswordItem.cleave(level, target, knight);
        helper.assertTrue(hit.size() == 2 && hit.contains(left) && hit.contains(right), "Der Spaltschlag trifft nicht beide Seiten: " + hit);
        helper.assertTrue(hurt(left) && hurt(right) && !hurt(back) && !hurt(far) && !hurt(target), "Falsche Gegner gespalten");
        cleanup(helper);
        helper.succeed();
    }

    /** Bergmannshammer: drei mal drei Blöcke (Haltbarkeit je Block), Obsidian bleibt; schleichend nur einer. */
    private static void minerHammer(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper);
        for (int x = 3; x <= 5; x++) {
            for (int y = 1; y <= 3; y++) {
                helper.setBlock(x, y, 5, Blocks.STONE);
                helper.setBlock(x, y, 6, Blocks.STONE);
            }
        }
        helper.setBlock(5, 3, 5, Blocks.OBSIDIAN);
        ServerPlayer player = miner(helper, new BlockPos(4, 1, 3), Armory.MINER_HAMMER.get());
        try {
            BlockPos center = helper.absolutePos(new BlockPos(4, 2, 5));
            helper.assertTrue(player.gameMode.destroyBlock(center), "Der Hammer baut nicht ab");
            int air = 0;
            for (int x = 3; x <= 5; x++) {
                for (int y = 1; y <= 3; y++) {
                    if (level.getBlockState(helper.absolutePos(new BlockPos(x, y, 5))).isAir()) {
                        air++;
                    }
                }
            }
            helper.assertTrue(air == 8, "Der Hammer bricht nicht drei mal drei (ohne Obsidian): " + air);
            helper.assertTrue(level.getBlockState(helper.absolutePos(new BlockPos(5, 3, 5))).is(Blocks.OBSIDIAN), "Obsidian wurde mitgenommen");
            helper.assertTrue(level.getBlockState(helper.absolutePos(new BlockPos(4, 2, 6))).is(Blocks.STONE), "Die zweite Reihe wurde mitgenommen");
            helper.assertTrue(player.getMainHandItem().getDamageValue() == 8, "Haltbarkeit nicht je Block: "
                    + player.getMainHandItem().getDamageValue());
            helper.assertTrue(count(helper, Items.COBBLESTONE) == 8, "Nicht jeder Block gibt Bruchstein: " + count(helper, Items.COBBLESTONE));
            // schleichend: nur der eine Block
            player.setShiftKeyDown(true);
            BlockPos next = helper.absolutePos(new BlockPos(4, 2, 6));
            player.snapTo(player.getX(), player.getY(), player.getZ() + 1.0, 0.0f, 0.0f);
            player.gameMode.destroyBlock(next);
            helper.assertTrue(level.getBlockState(next).isAir() && level.getBlockState(helper.absolutePos(new BlockPos(3, 2, 6))).is(Blocks.STONE),
                    "Schleichend baut der Hammer mehr als einen Block ab");
        } finally {
            remove(helper, player);
        }
        cleanup(helper);
        helper.succeed();
    }

    /** Adernbrecher: die ganze Ader (auch über Ecken und mit Tiefenschiefer-Erz), kein fremdes Erz, kein entferntes; schleichend einer. */
    private static void veinBreaker(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper);
        List<BlockPos> vein = List.of(new BlockPos(4, 1, 5), new BlockPos(4, 2, 5), new BlockPos(5, 2, 5), new BlockPos(6, 3, 6),
                new BlockPos(3, 1, 6), new BlockPos(3, 1, 7));
        for (BlockPos pos : vein) {
            helper.setBlock(pos, Blocks.IRON_ORE);
        }
        helper.setBlock(new BlockPos(3, 1, 7), Blocks.DEEPSLATE_IRON_ORE);
        helper.setBlock(new BlockPos(5, 1, 5), Blocks.COAL_ORE);                    // fremdes Erz direkt daneben
        helper.setBlock(new BlockPos(8, 1, 8), Blocks.IRON_ORE);                    // nicht verbunden
        ServerPlayer player = miner(helper, new BlockPos(4, 1, 3), Armory.VEIN_BREAKER.get());
        try {
            player.gameMode.destroyBlock(helper.absolutePos(vein.getFirst()));
            for (BlockPos pos : vein) {
                helper.assertTrue(level.getBlockState(helper.absolutePos(pos)).isAir(), "Die Ader ist nicht ganz gebrochen: " + pos);
            }
            helper.assertTrue(level.getBlockState(helper.absolutePos(new BlockPos(5, 1, 5))).is(Blocks.COAL_ORE), "Fremdes Erz wurde mitgenommen");
            helper.assertTrue(level.getBlockState(helper.absolutePos(new BlockPos(8, 1, 8))).is(Blocks.IRON_ORE), "Ein fernes Erz wurde mitgenommen");
            helper.assertTrue(count(helper, Items.RAW_IRON) >= vein.size(), "Nicht jedes Erz gibt Roheisen: " + count(helper, Items.RAW_IRON));
            helper.assertTrue(ArmoryMining.vein(level, helper.absolutePos(new BlockPos(0, 1, 0)), Blocks.STONE.defaultBlockState(),
                    player.getMainHandItem()).size() <= ArmoryMining.MAX_VEIN, "Die Ader ist nicht begrenzt");
            // schleichend: nur der eine Block
            helper.setBlock(new BlockPos(4, 1, 5), Blocks.IRON_ORE);
            helper.setBlock(new BlockPos(4, 2, 5), Blocks.IRON_ORE);
            player.setShiftKeyDown(true);
            player.gameMode.destroyBlock(helper.absolutePos(new BlockPos(4, 1, 5)));
            helper.assertTrue(level.getBlockState(helper.absolutePos(new BlockPos(4, 2, 5))).is(Blocks.IRON_ORE), "Schleichend bricht die Ader trotzdem");
        } finally {
            remove(helper, player);
        }
        cleanup(helper);
        helper.succeed();
    }

    /** Glutspitzhacke: Roheisen kommt als Barren heraus (auch mehrere), Stein bleibt Bruchstein. */
    private static void emberPickaxe(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper);
        var smelted = EmberPickaxeItem.smelt(level, new ItemStack(Items.RAW_GOLD, 3));
        helper.assertTrue(smelted.isPresent() && smelted.get().is(Items.GOLD_INGOT) && smelted.get().getCount() == 3,
                "Rohgold wird nicht zu drei Barren: " + smelted);
        helper.assertTrue(EmberPickaxeItem.smelt(level, new ItemStack(Items.COBBLESTONE)).isEmpty(), "Bruchstein wird geschmolzen");
        helper.setBlock(new BlockPos(4, 1, 5), Blocks.IRON_ORE);
        helper.setBlock(new BlockPos(5, 1, 5), Blocks.STONE);
        ServerPlayer player = miner(helper, new BlockPos(4, 1, 3), Armory.EMBER_PICKAXE.get());
        try {
            player.gameMode.destroyBlock(helper.absolutePos(new BlockPos(4, 1, 5)));
            player.gameMode.destroyBlock(helper.absolutePos(new BlockPos(5, 1, 5)));
            helper.assertTrue(count(helper, Items.IRON_INGOT) >= 1 && count(helper, Items.RAW_IRON) == 0,
                    "Das Eisenerz kommt nicht als Barren heraus: " + count(helper, Items.IRON_INGOT) + "/" + count(helper, Items.RAW_IRON));
            helper.assertTrue(count(helper, Items.COBBLESTONE) == 1 && count(helper, Items.STONE) == 0, "Stein wurde geschmolzen");
        } finally {
            remove(helper, player);
        }
        cleanup(helper);
        helper.succeed();
    }

    /** Erzspürer: findet die Erze im Umkreis (nicht weiter weg), lässt sie nur für den Benutzer leuchten, das Leuchten verlischt. */
    private static void oreSeeker(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper);
        List<BlockPos> ores = List.of(new BlockPos(2, 0, 2), new BlockPos(6, 0, 6), new BlockPos(8, 0, 1));
        List<Block> kinds = List.of(Blocks.DIAMOND_ORE, Blocks.IRON_ORE, Blocks.DEEPSLATE_GOLD_ORE);
        for (int i = 0; i < ores.size(); i++) {
            helper.setBlock(ores.get(i), kinds.get(i));
        }
        ServerPlayer player = miner(helper, new BlockPos(4, 1, 4), Armory.ORE_SEEKER.get());
        ServerPlayer other = helper.makeMockServerPlayerInLevel();
        BlockPos center = helper.absolutePos(new BlockPos(4, 1, 4));
        List<BlockPos> found = OreSeekerItem.scan(level, center);
        for (BlockPos ore : ores) {
            helper.assertTrue(found.contains(helper.absolutePos(ore)), "Das Erz bei " + ore + " wird nicht gefunden");
        }
        helper.assertTrue(found.stream().allMatch(p -> p.distSqr(center) <= OreSeekerItem.RADIUS * OreSeekerItem.RADIUS),
                "Gefunden außerhalb des Umkreises");
        helper.assertTrue(OreSeekerItem.color(Blocks.DIAMOND_ORE.defaultBlockState()) != OreSeekerItem.color(Blocks.IRON_ORE.defaultBlockState()),
                "Diamant und Eisen leuchten gleich");
        player.getMainHandItem().use(level, player, InteractionHand.MAIN_HAND);
        List<OreGlowEntity> glows = level.getEntitiesOfClass(OreGlowEntity.class, new AABB(center).inflate(OreSeekerItem.RADIUS + 1));
        helper.assertTrue(glows.size() >= ores.size(), "Zu wenig leuchtende Erze: " + glows.size());
        for (OreGlowEntity glow : glows) {
            helper.assertTrue(glow.isCurrentlyGlowing() && player.getUUID().equals(glow.owner()), "Ein Leuchten gehört nicht dem Spieler");
            helper.assertTrue(glow.broadcastToPlayer(player) && !glow.broadcastToPlayer(other), "Andere Spieler sehen das Leuchten");
            helper.assertTrue(!glow.shouldBeSaved(), "Das Leuchten würde gespeichert");
        }
        helper.assertTrue(player.getCooldowns().isOnCooldown(player.getMainHandItem()), "Der Erzspürer ruht nicht");
        remove(helper, other);
        helper.runAfterDelay(OreGlowEntity.LIFETIME + 10, () -> {
            remove(helper, player);
            helper.assertTrue(level.getEntitiesOfClass(OreGlowEntity.class, new AABB(center).inflate(OreSeekerItem.RADIUS + 1)).isEmpty(),
                    "Das Leuchten verlischt nicht");
            cleanup(helper);
            helper.succeed();
        });
    }

    /**
     * Schilde: Werte und Rezepte; der Rundschild pariert gleich nach dem Heben (später nicht mehr), am Drachenschild fängt
     * der Angreifer Feuer, der Turmschild deckt auch schräg von hinten, der Spiegelschild schickt einen Pfeil zurück.
     */
    private static void shields(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper);
        var manager = level.getServer().getRecipeManager();
        for (var shield : Armory.shields()) {
            ItemStack stack = new ItemStack(shield.get());
            String name = shield.getId().getPath();
            helper.assertTrue(stack.has(DataComponents.BLOCKS_ATTACKS) && stack.is(net.neoforged.neoforge.common.Tags.Items.TOOLS_SHIELD),
                    name + " blockt nicht wie ein Schild");
            helper.assertTrue(manager.byKey(ResourceKey.create(Registries.RECIPE, KingdomsAtWar.id(name))).isPresent(), "Kein Rezept für " + name);
        }
        var tower = new ItemStack(Armory.TOWER_SHIELD.get()).get(DataComponents.BLOCKS_ATTACKS);
        helper.assertTrue(tower != null && tower.damageReductions().getFirst().horizontalBlockingAngle() > 90.0f, "Der Turmschild deckt nicht mehr");
        // die Blocker: Wüstenzombies mit dem Schild in der Nebenhand, Blick nach Süden (+z) – Ritter senken ihn von selbst
        Mob buckler = dummy(helper, new BlockPos(1, 1, 2), 0.0f);
        Mob dragon = dummy(helper, new BlockPos(3, 1, 2), 0.0f);
        Mob towerKnight = dummy(helper, new BlockPos(5, 1, 2), 0.0f);
        Mob mirror = dummy(helper, new BlockPos(7, 1, 2), 0.0f);
        buckler.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Armory.STEEL_BUCKLER.get()));
        dragon.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Armory.DRAGON_SHIELD.get()));
        towerKnight.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Armory.TOWER_SHIELD.get()));
        mirror.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Armory.MIRROR_SHIELD.get()));
        for (Mob m : List.of(dragon, towerKnight, mirror)) {
            m.startUsingItem(InteractionHand.OFF_HAND);
        }
        helper.runAfterDelay(6, () -> buckler.startUsingItem(InteractionHand.OFF_HAND));
        helper.runAfterDelay(12, () -> {
            for (Mob m : List.of(buckler, dragon, towerKnight, mirror)) {
                helper.assertTrue(m.isBlocking(), "Der Schild ist nicht oben: " + m.getOffhandItem());
            }
            // Parade
            Mob a1 = dummy(helper, new BlockPos(1, 1, 4), 180.0f);
            buckler.hurtServer(level, level.damageSources().mobAttack(a1), 4.0f);
            helper.assertTrue(!hurt(buckler), "Der Rundschild blockt nicht");
            helper.assertTrue(a1.hasEffect(MobEffects.WEAKNESS), "Keine Parade gleich nach dem Heben");
            // Drachenschild
            Mob a2 = dummy(helper, new BlockPos(3, 1, 4), 180.0f);
            dragon.hurtServer(level, level.damageSources().mobAttack(a2), 4.0f);
            helper.assertTrue(!hurt(dragon) && a2.isOnFire(), "Am Drachenschild fängt der Angreifer kein Feuer");
            // Turmschild: schräg von hinten (gut 115° neben der Blickrichtung)
            Mob a3 = dummy(helper, new BlockPos(7, 1, 1), 0.0f);
            towerKnight.hurtServer(level, level.damageSources().mobAttack(a3), 4.0f);
            helper.assertTrue(!hurt(towerKnight), "Der Turmschild deckt die Flanke nicht");
            // Spiegelschild: ein Pfeil von vorn fliegt zurück und gehört nun dem Ritter
            Mob a4 = dummy(helper, new BlockPos(7, 1, 6), 180.0f);
            Vec3 from = mirror.position().add(0, 1.4, 1.5);
            net.minecraft.world.entity.projectile.arrow.Arrow arrow = new net.minecraft.world.entity.projectile.arrow.Arrow(level, from.x, from.y,
                    from.z, new ItemStack(Items.ARROW), null);
            arrow.setOwner(a4);
            arrow.setDeltaMovement(0, 0, -2.0);
            level.addFreshEntity(arrow);
            helper.assertTrue(io.github.sirschrolli.kingdomsatwar.item.armory.ArmoryShields.reflect(mirror, arrow), "Der Spiegel wirft nicht zurück");
            helper.assertTrue(arrow.getOwner() == mirror && arrow.getDeltaMovement().z > 1.0, "Der Pfeil fliegt nicht zurück: "
                    + arrow.getDeltaMovement() + " / " + arrow.getOwner());
            arrow.discard();
            a1.removeAllEffects();
        });
        helper.runAfterDelay(34, () -> {
            Mob a5 = dummy(helper, new BlockPos(1, 1, 4), 180.0f);
            buckler.hurtServer(level, level.damageSources().mobAttack(a5), 4.0f);
            helper.assertTrue(!hurt(buckler) && !a5.hasEffect(MobEffects.WEAKNESS), "Der Rundschild pariert auch lange nach dem Heben");
            cleanup(helper);
            helper.succeed();
        });
    }
}
