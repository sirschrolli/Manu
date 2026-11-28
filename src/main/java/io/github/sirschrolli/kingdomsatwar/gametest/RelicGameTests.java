package io.github.sirschrolli.kingdomsatwar.gametest;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.ModEffects;
import io.github.sirschrolli.kingdomsatwar.registry.RelicItems;
import io.github.sirschrolli.kingdomsatwar.relic.PowerApple;
import io.github.sirschrolli.kingdomsatwar.relic.TotemKind;
import io.github.sirschrolli.kingdomsatwar.relic.Totems;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Prüft die Totems und die Äpfel der Macht (seit 0.80): Jedes Totem hält den Tod auf und lässt mit mehr Herzen wieder
 * aufstehen, entfesselt seine Kraft und wirkt in der Zweithand; das Totem des Schöpfers hält dreimal, das der Leere rettet
 * aus dem Nichts. Die Äpfel geben, was an ihnen steht, und das Zweite Leben hält genau einmal.
 */
final class RelicGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TOTEMS =
            ModGameTests.TEST_FUNCTIONS.register("relic_totems", () -> RelicGameTests::totems);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> APPLES =
            ModGameTests.TEST_FUNCTIONS.register("relic_apples", () -> RelicGameTests::apples);

    private RelicGameTests() {}

    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        event.registerTest(TOTEMS.getId(), new FunctionGameTestInstance(TOTEMS.key(),
                new TestData<>(environment, Level.OVERWORLD, ARENA, 200, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        event.registerTest(APPLES.getId(), new FunctionGameTestInstance(APPLES.key(),
                new TestData<>(environment, Level.OVERWORLD, ARENA, 200, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
    }

    private static JsonObject lang(GameTestHelper helper, String name) {
        try (InputStream in = RelicGameTests.class.getResourceAsStream("/assets/" + KingdomsAtWar.MODID + "/lang/" + name + ".json")) {
            helper.assertTrue(in != null, "Sprachdatei fehlt: " + name);
            return JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (java.io.IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Bild, plastisches Modell und Gegenstandsbeschreibung liegen bei. */
    private static void assets(GameTestHelper helper, String name) {
        for (String file : List.of("textures/item/" + name + ".png", "models/item/relief/" + name + ".json", "models/item/" + name + ".json",
                "items/" + name + ".json")) {
            helper.assertTrue(RelicGameTests.class.getResource("/assets/" + KingdomsAtWar.MODID + "/" + file) != null, "Datei fehlt: " + file);
        }
    }

    private static void recipe(GameTestHelper helper, String name) {
        var key = ResourceKey.create(Registries.RECIPE, KingdomsAtWar.id(name));
        helper.assertTrue(helper.getLevel().getServer().getRecipeManager().recipeMap().byKey(key) != null, "Rezept fehlt: " + name);
    }

    private static ServerPlayer mortal(GameTestHelper helper, double x, double z) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        // im Spiel angekommen, nicht unverwundbar, keine Kreativkräfte – und das Licht der Unsterblichkeit ruht nicht dazwischen
        player.connection.markClientLoaded();
        player.getAbilities().invulnerable = false;
        player.getAbilities().instabuild = false;
        Vec3 stand = helper.absoluteVec(new Vec3(x, 2.0, z));
        player.snapTo(stand.x, stand.y, stand.z, 0.0f, 0.0f);
        return player;
    }

    /**
     * Ein Schlag, den niemand überlebt – außer mit Hilfe. Kurz nach einem Treffer zählt nur, was den letzten übertrifft:
     * Darum ist der {@code n}-te Schlag auf denselben Spieler jeweils dreimal so hart.
     */
    private static void kill(ServerLevel level, ServerPlayer player, int n) {
        player.damageCooldownTime = 0;
        player.hurtServer(level, level.damageSources().generic(), (float) (10_000.0 * Math.pow(3.0, n)));
    }

    /** Ein Feind, der viel aushält (der Zombie der Mod – die von Minecraft werden beim Betreten der Welt durch ihn ersetzt). */
    private static Mob foe(GameTestHelper helper, int x, int z) {
        Mob zombie = helper.spawnWithNoFreeWill(io.github.sirschrolli.kingdomsatwar.registry.ModEntities.ZOMBIE.get(), new BlockPos(x, 2, z));
        zombie.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100.0);
        zombie.setHealth(100.0f);
        return zombie;
    }

    private static void totems(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        JsonObject german = lang(helper, "de_de");
        JsonObject english = lang(helper, "en_us");
        // ein fester Boden unter allem
        for (int x = 0; x <= 8; x++) {
            for (int z = 0; z <= 8; z++) {
                helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
            }
        }
        for (TotemKind kind : TotemKind.values()) {
            String name = kind.itemName();
            ItemStack stack = new ItemStack(RelicItems.totem(kind));
            helper.assertTrue(stack.has(DataComponents.DEATH_PROTECTION) && stack.getMaxStackSize() == 1, "Kein Totem: " + name);
            helper.assertTrue(stack.isDamageableItem() == (kind.charges() > 1) && (kind.charges() == 1 || stack.getMaxDamage() == kind.charges()),
                    "Ladungen falsch: " + name);
            assets(helper, name);
            recipe(helper, name);
            for (JsonObject lang : List.of(german, english)) {
                helper.assertTrue(lang.has("item.kingdomsatwar." + name), "Name fehlt: " + name);
                for (int i = 1; i <= kind.passiveLines(); i++) {
                    helper.assertTrue(lang.has(kind.key("passive." + i)), "Zeile fehlt: " + kind.key("passive." + i));
                }
                for (int i = 1; i <= kind.reviveLines(); i++) {
                    helper.assertTrue(lang.has(kind.key("revive." + i)), "Zeile fehlt: " + kind.key("revive." + i));
                }
            }
            helper.assertTrue(kind.passiveLines() > 0 || !kind.attributes().modifiers().isEmpty(), "Totem ohne Wirkung in der Zweithand: " + name);

            // der Tod wird aufgehalten: mehr Herzen, eigene Zustände, die Kraft trifft den Feind daneben
            ServerPlayer player = mortal(helper, 4.5, 4.5);
            Mob zombie = foe(helper, 6, 4);
            try {
                player.setItemInHand(InteractionHand.OFF_HAND, stack);
                player.addEffect(new MobEffectInstance(MobEffects.POISON, 400, 0));
                kill(level, player, 0);
                float expected = kind.reviveHealth() < 0.0f ? player.getMaxHealth() : Math.min(player.getMaxHealth(), kind.reviveHealth());
                helper.assertTrue(player.isAlive() && Math.abs(player.getHealth() - expected) < 0.01f,
                        name + ": steht nicht mit " + expected + " Lebenspunkten auf, sondern mit " + player.getHealth());
                helper.assertTrue(!player.hasEffect(MobEffects.POISON) && player.hasEffect(MobEffects.REGENERATION), name + ": Zustände nach dem Aufstehen");
                for (MobEffectInstance effect : kind.reviveEffects()) {
                    if (effect.getEffect() != MobEffects.SATURATION) {
                        helper.assertTrue(player.hasEffect(effect.getEffect()), name + ": Zustand fehlt: " + effect.getEffect().getRegisteredName());
                    }
                }
                ItemStack left = player.getOffhandItem();
                if (kind.charges() > 1) {
                    helper.assertTrue(left.is(RelicItems.totem(kind)) && left.getDamageValue() == 1, name + ": kehrt nicht mit einer Ladung weniger zurück");
                } else {
                    helper.assertTrue(left.isEmpty(), name + ": wurde nicht verbraucht");
                }
                switch (kind) {
                    case EMBER -> helper.assertTrue(zombie.getHealth() < 95.0f && zombie.getRemainingFireTicks() > 0, "Feuernova trifft nicht: Leben "
                            + zombie.getHealth() + ", Feuer " + zombie.getRemainingFireTicks());
                    case FROST -> helper.assertTrue(zombie.getHealth() < 97.0f && zombie.hasEffect(MobEffects.SLOWNESS), "Frostnova trifft nicht");
                    case STORM -> helper.assertTrue(zombie.getHealth() < 95.0f, "Blitze treffen nicht");
                    case STONE -> helper.assertTrue(zombie.getHealth() < 97.0f && zombie.getDeltaMovement().y > 0.3, "Erdstoß trifft nicht");
                    case TIDE -> helper.assertTrue(zombie.getHealth() < 99.0f && zombie.getDeltaMovement().horizontalDistance() > 0.5, "Flutwelle trifft nicht");
                    case SHADOW -> helper.assertTrue(zombie.hasEffect(MobEffects.WEAKNESS) && zombie.getTarget() == null, "Schattenschleier wirkt nicht");
                    case CREATOR -> helper.assertTrue(zombie.getHealth() < 90.0f, "Lichtnova trifft nicht");
                    default -> { }
                }
                if (kind == TotemKind.CREATOR) {
                    // dreimal hält es, dann ist es fort – und beim vierten Mal stirbt der Träger
                    kill(level, player, 1);
                    helper.assertTrue(player.isAlive() && player.getOffhandItem().getDamageValue() == 2, "Totem des Schöpfers: zweite Ladung");
                    kill(level, player, 2);
                    helper.assertTrue(player.isAlive() && player.getOffhandItem().isEmpty(), "Totem des Schöpfers: zerfällt nach der dritten Ladung nicht");
                }
            } finally {
                zombie.discard();
                level.getServer().getPlayerList().remove(player);
            }
        }

        // in der Zweithand: Werte, dauernde Zustände, Vergeltung – in der Haupthand nichts davon
        ServerPlayer player = mortal(helper, 4.5, 4.5);
        try {
            double armor = player.getAttributeValue(Attributes.ARMOR);
            player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(RelicItems.totem(TotemKind.STONE)));
            player.doTick();
            helper.assertTrue(Math.abs(player.getAttributeValue(Attributes.ARMOR) - armor - 3.0) < 0.01, "Totem des Felsens gibt keine Rüstung");
            player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(RelicItems.totem(TotemKind.STONE)));
            player.doTick();
            helper.assertTrue(Math.abs(player.getAttributeValue(Attributes.ARMOR) - armor) < 0.01, "Das Totem wirkt auch in der Haupthand");
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(RelicItems.totem(TotemKind.CREATOR)));
            player.doTick();
            helper.assertTrue(Math.abs(player.getMaxHealth() - 30.0f) < 0.01f, "Totem des Schöpfers gibt keine fünf Herzen: " + player.getMaxHealth());
            player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(RelicItems.totem(TotemKind.EMBER)));
            for (int i = 0; i < 21; i++) {
                player.doTick();
            }
            helper.assertTrue(player.hasEffect(MobEffects.FIRE_RESISTANCE), "Totem der Glut schützt nicht vor Feuer");
            Mob attacker = foe(helper, 5, 4);
            try {
                player.damageCooldownTime = 0;
                player.hurtServer(level, level.damageSources().mobAttack(attacker), 2.0f);
                helper.assertTrue(attacker.getRemainingFireTicks() > 0, "Wer den Träger der Glut schlägt, brennt nicht");
            } finally {
                attacker.discard();
            }
            helper.assertTrue(Totems.offhand(player) == TotemKind.EMBER && Totems.kindOf(new ItemStack(Items.TOTEM_OF_UNDYING)) == null, "Totem erkennen");

            // das Totem der Leere: zurück auf den festen Boden, auf dem der Träger zuletzt stand
            player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
            Vec3 ground = helper.absoluteVec(new Vec3(2.5, 2.0, 2.5));
            player.snapTo(ground.x, ground.y, ground.z, 0.0f, 0.0f);
            player.setDeltaMovement(Vec3.ZERO);
            for (int i = 0; i < 25; i++) {
                player.doTick();
            }
            Vec3 safe = Totems.safeSpot(player);
            helper.assertTrue(safe != null && safe.distanceTo(ground) < 1.5, "Der feste Boden wurde nicht gemerkt: " + safe + " statt " + ground);
            player.snapTo(ground.x + 3.0, ground.y + 40.0, ground.z, 0.0f, 0.0f);
            helper.assertTrue(!Totems.voidRescue(player), "Rettung aus dem Nichts ohne Totem");
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(RelicItems.totem(TotemKind.VOID)));
            player.setHealth(3.0f);
            helper.assertTrue(Totems.voidRescue(player) && player.getMainHandItem().isEmpty(), "Das Totem der Leere rettet nicht");
            helper.assertTrue(player.position().distanceTo(ground) < 1.5 && Math.abs(player.getHealth() - 16.0f) < 0.01f
                    && player.hasEffect(MobEffects.SLOW_FALLING), "Nach der Rettung: " + player.position() + ", " + player.getHealth());
        } finally {
            level.getServer().getPlayerList().remove(player);
        }
        helper.succeed();
    }

    private static void apples(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        JsonObject german = lang(helper, "de_de");
        JsonObject english = lang(helper, "en_us");
        for (int x = 0; x <= 8; x++) {
            for (int z = 0; z <= 8; z++) {
                helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
            }
        }
        for (JsonObject lang : List.of(german, english)) {
            helper.assertTrue(lang.has("effect.kingdomsatwar.second_life") && lang.has("apple.kingdomsatwar.eaten")
                    && lang.has("itemGroup.kingdomsatwar.relics"), "Texte der Äpfel fehlen");
        }
        helper.assertTrue(RelicGameTests.class.getResource("/assets/kingdomsatwar/textures/mob_effect/second_life.png") != null, "Bild des Zweiten Lebens fehlt");
        ServerPlayer player = mortal(helper, 4.5, 4.5);
        try {
            for (PowerApple apple : PowerApple.values()) {
                ItemStack stack = new ItemStack(RelicItems.apple(apple));
                helper.assertTrue(stack.has(DataComponents.FOOD) && stack.has(DataComponents.CONSUMABLE), "Kein Apfel: " + apple.id());
                assets(helper, apple.id());
                recipe(helper, apple.id());
                for (JsonObject lang : List.of(german, english)) {
                    helper.assertTrue(lang.has("item.kingdomsatwar." + apple.id()), "Name fehlt: " + apple.id());
                }
                player.removeAllEffects();
                player.setItemInHand(InteractionHand.MAIN_HAND, stack);
                stack.finishUsingItem(level, player);
                for (MobEffectInstance effect : apple.effects()) {
                    MobEffectInstance active = player.getEffect(effect.getEffect());
                    helper.assertTrue(active != null && active.getAmplifier() == effect.getAmplifier(),
                            apple.id() + ": Zustand fehlt oder ist zu schwach: " + effect.getEffect().getRegisteredName());
                }
            }
            // das Zweite Leben: einmal volle Herzen statt des Todes, dann ist es verbraucht
            helper.assertTrue(player.hasEffect(ModEffects.SECOND_LIFE), "Der Apfel der Unsterblichkeit gibt kein Zweites Leben");
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            player.addEffect(new MobEffectInstance(MobEffects.WITHER, 400, 1));
            kill(level, player, 0);
            helper.assertTrue(player.isAlive() && Math.abs(player.getHealth() - player.getMaxHealth()) < 0.01f && !player.hasEffect(ModEffects.SECOND_LIFE)
                    && !player.hasEffect(MobEffects.WITHER) && player.hasEffect(MobEffects.STRENGTH), "Das Zweite Leben hält den Tod nicht auf");
            // ein Totem geht vor – und nimmt zwar alle anderen Zustände, das Zweite Leben aber nicht
            player.addEffect(new MobEffectInstance(ModEffects.SECOND_LIFE, 400, 0));
            player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.TOTEM_OF_UNDYING));
            kill(level, player, 1);
            helper.assertTrue(player.isAlive() && player.getOffhandItem().isEmpty() && player.hasEffect(ModEffects.SECOND_LIFE)
                    && !player.hasEffect(MobEffects.STRENGTH), "Das Totem wurde nicht zuerst verbraucht, oder es nahm das Zweite Leben mit");
            kill(level, player, 2);
            helper.assertTrue(player.isAlive() && !player.hasEffect(ModEffects.SECOND_LIFE), "Das Zweite Leben nach dem Totem");
            kill(level, player, 3);
            helper.assertTrue(player.isDeadOrDying(), "Ohne Zweites Leben stirbt der Spieler nicht");
        } finally {
            level.getServer().getPlayerList().remove(player);
        }
        helper.succeed();
    }
}
