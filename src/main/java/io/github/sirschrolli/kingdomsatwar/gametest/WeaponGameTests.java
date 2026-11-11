package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.entity.creator.DivineShotEntity;
import io.github.sirschrolli.kingdomsatwar.item.creator.CreatorScepterItem;
import io.github.sirschrolli.kingdomsatwar.registry.CreatorBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import io.github.sirschrolli.kingdomsatwar.rules.WeaponPower;
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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Prüft die Verzauberungen auf den Waffen der Mod (seit 0.79): Schärfe gibt auf einem Diamantschwert und auf schwächeren
 * Waffen drei Punkte dazu wie immer, auf stärkeren Waffen der Mod entsprechend mehr – und verstärkt die Fähigkeiten
 * (Lichtspeer, Lichtwelle, Lichtsäule) um denselben Anteil.
 */
final class WeaponGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ENCHANTED =
            ModGameTests.TEST_FUNCTIONS.register("weapon_enchantments", () -> WeaponGameTests::enchanted);

    private WeaponGameTests() {}

    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        event.registerTest(ENCHANTED.getId(), new FunctionGameTestInstance(ENCHANTED.key(),
                new TestData<>(environment, Level.OVERWORLD, ARENA, 200, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
    }

    private static ItemStack enchanted(ServerLevel level, Item item, ResourceKey<Enchantment> enchantment, int grade) {
        ItemStack stack = new ItemStack(item);
        stack.enchant(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(enchantment), grade);
        return stack;
    }

    /** Ein Ziel, das viel aushält und keine Rüstung trägt. */
    private static LivingEntity dummy(GameTestHelper helper) {
        var pig = helper.spawnWithNoFreeWill(ModEntities.FARM_PIG.get(), new BlockPos(4, 1, 4));
        pig.getAttribute(Attributes.MAX_HEALTH).setBaseValue(400.0);
        pig.setHealth(400.0f);
        return pig;
    }

    /** Ein voll ausgeholter Schlag mit dieser Waffe auf ein frisches Ziel: wie viel Leben er kostet. */
    private static float strike(GameTestHelper helper, ServerPlayer player, ItemStack weapon) {
        LivingEntity target = dummy(helper);
        try {
            return strike(player, target, weapon);
        } finally {
            target.discard();
        }
    }

    private static float strike(ServerPlayer player, LivingEntity target, ItemStack weapon) {
        player.setItemInHand(InteractionHand.MAIN_HAND, weapon);
        for (int i = 0; i < 40; i++) {
            player.doTick();
        }
        float before = target.getHealth();
        player.attack(target);
        return before - target.getHealth();
    }

    private static void enchanted(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (int x = 0; x < 9; x++) {
            for (int z = 0; z < 9; z++) {
                helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
            }
        }
        LivingEntity target = dummy(helper);
        LivingEntity[] mark = new LivingEntity[1];
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.getAbilities().instabuild = false;
        player.snapTo(target.getX() - 1.6, target.getY(), target.getZ(), -90.0f, 0.0f);
        Item spear = CreatorBlocks.CREATOR_SCEPTER.get();
        try {
            // das Maß: auf dem Diamantschwert bleibt alles, wie es ist – Schärfe V gibt drei Punkte
            float diamond = strike(helper, player, new ItemStack(Items.DIAMOND_SWORD));
            float diamondSharp = strike(helper, player, enchanted(level, Items.DIAMOND_SWORD, Enchantments.SHARPNESS, 5));
            helper.assertTrue(Math.abs(diamond - WeaponPower.REFERENCE) < 0.05f && Math.abs(diamondSharp - diamond - 3.0f) < 0.05f,
                    "Diamantschwert: " + diamond + " und mit Schärfe V " + diamondSharp);
            // eine schwächere Waffe der Mod: ebenfalls drei Punkte
            float steel = strike(helper, player, new ItemStack(ModItems.STEEL_SWORD.get()));
            float steelSharp = strike(helper, player, enchanted(level, ModItems.STEEL_SWORD.get(), Enchantments.SHARPNESS, 5));
            helper.assertTrue(WeaponPower.scale(new ItemStack(ModItems.STEEL_SWORD.get())) == 1.0f && Math.abs(steelSharp - steel - 3.0f) < 0.05f,
                    "Stahlschwert: " + steel + " und mit Schärfe V " + steelSharp);
            // der Speer des Schöpfers: so viel mehr, wie er stärker ist als ein Diamantschwert
            float plain = strike(helper, player, new ItemStack(spear));
            float sharp = strike(helper, player, enchanted(level, spear, Enchantments.SHARPNESS, 5));
            float scale = WeaponPower.scale(new ItemStack(spear));
            helper.assertTrue(Math.abs(plain - (float) CreatorScepterItem.MELEE_DAMAGE) < 0.05f && scale > 2.5f,
                    "Speer des Schöpfers: Schlag " + plain + ", Maß " + scale);
            helper.assertTrue(Math.abs(sharp - plain - 3.0f * scale) < 0.1f, "Schärfe V auf dem Speer des Schöpfers: " + plain + " -> " + sharp
                    + " (erwartet +" + 3.0f * scale + ")");
            // Bann wirkt nur gegen Untote – gegen ein Schwein bringt er nichts
            float smite = strike(helper, player, enchanted(level, spear, Enchantments.SMITE, 5));
            helper.assertTrue(Math.abs(smite - plain) < 0.05f, "Bann trifft ein Schwein härter: " + smite);

            // Fähigkeiten: mit Schärfe V um drei Siebtel stärker (wie der Schlag eines Diamantschwerts); ohne Verzauberung unverändert
            DamageSource magic = level.damageSources().indirectMagic(player, player);
            ItemStack sharpSpear = enchanted(level, spear, Enchantments.SHARPNESS, 5);
            helper.assertTrue(WeaponPower.ability(level, new ItemStack(spear), target, magic, 18.0f) == 18.0f
                    && Math.abs(WeaponPower.ability(level, sharpSpear, target, magic, 14.0f) - 20.0f) < 0.01f
                    && WeaponPower.ability(level, ItemStack.EMPTY, target, magic, 18.0f) == 18.0f, "Fähigkeit mit Schärfe V: "
                    + WeaponPower.ability(level, sharpSpear, target, magic, 18.0f));
            var zombie = helper.spawnWithNoFreeWill(ModEntities.ZOMBIE.get(), new BlockPos(7, 1, 7));
            ItemStack smiteSpear = enchanted(level, spear, Enchantments.SMITE, 5);
            helper.assertTrue(WeaponPower.ability(level, smiteSpear, target, magic, 16.0f) == 16.0f
                    && Math.abs(WeaponPower.ability(level, smiteSpear, zombie, magic, 14.0f) - 39.0f) < 0.01f, "Fähigkeit mit Bann V gegen Untote: "
                    + WeaponPower.ability(level, smiteSpear, zombie, magic, 14.0f));
            zombie.discard();

            // der geworfene Lichtspeer trägt die Verzauberung mit (auf ein frisches Ziel: Das erste hat die Lichtwelle des
            // dritten Speertreffers abbekommen und ist fortgeschleudert)
            helper.assertTrue(target.getHealth() < target.getMaxHealth(), "Die Lichtwelle des dritten Treffers blieb aus");
            target.discard();
            mark[0] = dummy(helper);
            Vec3 from = mark[0].position().add(-3.0, 0.5, 0.0);
            DivineShotEntity.thrownSpear(level, player, from, new Vec3(1.0, 0.0, 0.0), 3.0, CreatorScepterItem.SPEAR_DAMAGE).weapon(sharpSpear);
        } catch (RuntimeException e) {
            level.getServer().getPlayerList().remove(player);
            throw e;
        }
        helper.runAfterDelay(12, () -> {
            try {
                float lost = mark[0].getMaxHealth() - mark[0].getHealth();
                float expected = CreatorScepterItem.SPEAR_DAMAGE * (1.0f + 3.0f / WeaponPower.REFERENCE);
                helper.assertTrue(Math.abs(lost - expected) < 0.1f, "Der Lichtspeer mit Schärfe V kostet " + lost + " statt " + expected);
            } finally {
                level.getServer().getPlayerList().remove(player);
            }
            helper.succeed();
        });
    }
}
