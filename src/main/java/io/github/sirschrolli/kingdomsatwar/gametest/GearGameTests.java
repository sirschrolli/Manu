package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.item.monster.BogClubItem;
import io.github.sirschrolli.kingdomsatwar.item.monster.EmberFangItem;
import io.github.sirschrolli.kingdomsatwar.item.monster.MonsterGearEvents;
import io.github.sirschrolli.kingdomsatwar.item.monster.SoulScytheItem;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.MonsterGear;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

/** Tests der Waffen und Rüstungen aus Monsterteilen (0.70). */
final class GearGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> RECIPES =
            ModGameTests.TEST_FUNCTIONS.register("gear_recipes", () -> GearGameTests::recipes);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> WEAPONS =
            ModGameTests.TEST_FUNCTIONS.register("gear_weapons", () -> GearGameTests::weapons);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ARMOR =
            ModGameTests.TEST_FUNCTIONS.register("gear_armor", () -> GearGameTests::armor);

    private GearGameTests() {}

    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var test : List.of(RECIPES, WEAPONS, ARMOR)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 200, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
    }

    private static void floor(GameTestHelper helper) {
        for (int x = 0; x < 9; x++) {
            for (int z = 0; z < 9; z++) {
                helper.setBlock(x, 0, z, Blocks.STONE);
            }
        }
    }

    private static AABB area(GameTestHelper helper) {
        return new AABB(Vec3.atLowerCornerOf(helper.absolutePos(new BlockPos(-2, -1, -2))),
                Vec3.atLowerCornerOf(helper.absolutePos(new BlockPos(11, 10, 11))));
    }

    private static void cleanup(GameTestHelper helper) {
        helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.Entity.class, area(helper), e -> !(e instanceof Player))
                .forEach(net.minecraft.world.entity.Entity::discard);
    }

    /** Jede Waffe und jedes Rüstungsteil hat ein Rezept, das genau sie ergibt. */
    private static void recipes(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var manager = level.getServer().getRecipeManager();
        List<DeferredItem<?>> all = new java.util.ArrayList<>(MonsterGear.weapons());
        all.addAll(MonsterGear.peltSet());
        all.addAll(MonsterGear.emberSet());
        for (DeferredItem<?> item : all) {
            String name = item.getId().getPath();
            ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, KingdomsAtWar.id(name));
            var recipe = manager.byKey(key);
            helper.assertTrue(recipe.isPresent(), "Kein Rezept für " + name);
        }
        helper.succeed();
    }

    /**
     * Krallenklinge: wer mit ihr siegt, gerät in einen Blutrausch. Moorkeule: Wurzeln packen zu. Glutzahn: entzündet und
     * trifft Brennende härter. Seelensense: schwächt und entzündet; Verwehen trägt ein Stück nach vorn, unsichtbar.
     */
    private static void weapons(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper);
        Mob knight = helper.spawnWithNoFreeWill(ModEntities.SOLDIERS.get(SoldierType.KNIGHT).get(), new BlockPos(1, 1, 4));
        Mob target = helper.spawnWithNoFreeWill(ModEntities.VILLAGER.get(), new BlockPos(3, 1, 4));
        // Blutrausch
        knight.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(MonsterGear.CLAW_BLADE.get()));
        Mob victim = helper.spawnWithNoFreeWill(ModEntities.VILLAGER.get(), new BlockPos(2, 1, 6));
        victim.hurtServer(level, level.damageSources().mobAttack(knight), 1000.0f);
        helper.assertTrue(knight.hasEffect(MobEffects.SPEED) && knight.hasEffect(MobEffects.STRENGTH), "Kein Blutrausch nach dem Sieg");
        // Wurzelgriff (je Treffer etwa jedes dritte Mal)
        ItemStack club = new ItemStack(MonsterGear.BOG_CLUB.get());
        for (int i = 0; i < 30 && !target.hasEffect(MobEffects.SLOWNESS); i++) {
            club.getItem().hurtEnemy(club, target, knight);
        }
        var slow = target.getEffect(MobEffects.SLOWNESS);
        helper.assertTrue(slow != null && slow.getAmplifier() >= 3, "Die Moorkeule packt nicht zu");
        // Glutzahn
        ItemStack fang = new ItemStack(MonsterGear.EMBER_FANG_BLADE.get());
        target.clearFire();
        float cold = fang.getItem().getAttackDamageBonus(target, 5.0f, level.damageSources().mobAttack(knight));
        fang.getItem().hurtEnemy(fang, target, knight);
        helper.assertTrue(target.isOnFire(), "Der Glutzahn entzündet nicht");
        float hot = fang.getItem().getAttackDamageBonus(target, 5.0f, level.damageSources().mobAttack(knight));
        helper.assertTrue(cold == 0.0f && hot == EmberFangItem.BURNING_BONUS, "Der Glutzahn trifft Brennende nicht härter (" + cold + "/" + hot + ")");
        // Seelensense
        target.clearFire();
        target.removeAllEffects();
        ItemStack scythe = new ItemStack(MonsterGear.SOUL_SCYTHE.get());
        scythe.getItem().hurtEnemy(scythe, target, knight);
        helper.assertTrue(target.hasEffect(MobEffects.WEAKNESS) && target.isOnFire(), "Die Seelensense schwächt oder brennt nicht");
        // Verwehen: der Ritter blickt nach Süden über die freie Arena
        knight.snapTo(knight.getX(), knight.getY(), helper.absoluteVec(new Vec3(1.5, 1, 0.5)).z, 0.0f, 0.0f);
        knight.setYHeadRot(0.0f);
        Vec3 from = knight.position();
        Vec3 to = SoulScytheItem.dashTarget(level, knight);
        helper.assertTrue(to != null && to.distanceTo(from) >= 2.0, "Kein Ziel zum Verwehen: " + to);
        SoulScytheItem.fade(level, knight, to);
        helper.assertTrue(knight.position().distanceTo(from) >= 2.0 && knight.hasEffect(MobEffects.INVISIBILITY), "Er verweht nicht");
        cleanup(helper);
        helper.succeed();
    }

    /** Volle Sätze werden erkannt; der Glutwurm-Panzer macht feuerfest und lässt Angreifer brennen. */
    private static void armor(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper);
        Mob wearer = helper.spawnWithNoFreeWill(ModEntities.SOLDIERS.get(SoldierType.KNIGHT).get(), new BlockPos(2, 1, 4));
        // ein Feind (Verbündete der Soldaten richten keinen Schaden an – dann gäbe es auch keine Vergeltung)
        Mob attacker = helper.spawnWithNoFreeWill(net.minecraft.world.entity.EntityTypes.VINDICATOR, new BlockPos(4, 1, 4));
        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        List<DeferredItem<net.minecraft.world.item.Item>> pelt = MonsterGear.peltSet();
        List<DeferredItem<net.minecraft.world.item.Item>> ember = MonsterGear.emberSet();
        for (int i = 0; i < 4; i++) {
            wearer.setItemSlot(slots[i], new ItemStack(pelt.get(i).get()));
        }
        helper.assertTrue(MonsterGearEvents.fullSet(wearer) == MonsterGearEvents.ArmorSet.PELT, "Der Pelzsatz wird nicht erkannt");
        wearer.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ember.get(0).get()));
        helper.assertTrue(MonsterGearEvents.fullSet(wearer) == null, "Ein gemischter Satz zählt als voller Satz");
        for (int i = 0; i < 4; i++) {
            wearer.setItemSlot(slots[i], new ItemStack(ember.get(i).get()));
        }
        helper.assertTrue(MonsterGearEvents.fullSet(wearer) == MonsterGearEvents.ArmorSet.EMBER, "Der Glutwurmsatz wird nicht erkannt");
        MonsterGearEvents.applySetBonus(wearer, MonsterGearEvents.ArmorSet.EMBER);
        helper.assertTrue(wearer.hasEffect(MobEffects.FIRE_RESISTANCE), "Der Glutwurm-Panzer macht nicht feuerfest");
        attacker.clearFire();
        helper.assertTrue(!attacker.isOnFire(), "Der Angreifer brennt schon vorher");
        wearer.hurtServer(level, level.damageSources().mobAttack(attacker), 2.0f);
        helper.assertTrue(attacker.isOnFire(), "Wer den Glutwurm-Panzer trifft, fängt kein Feuer");
        helper.assertTrue(new ItemStack(MonsterGear.EMBER_CHESTPLATE.get()).has(net.minecraft.core.component.DataComponents.DAMAGE_RESISTANT),
                "Der Glutwurm-Panzer verbrennt in Lava");
        cleanup(helper);
        helper.succeed();
    }
}
