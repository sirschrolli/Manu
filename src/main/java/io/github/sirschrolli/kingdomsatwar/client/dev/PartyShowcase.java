package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.entity.realm.RealmWarriorEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.war.Realm;
import io.github.sirschrolli.kingdomsatwar.war.WarriorRole;
import java.util.Deque;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * Vorführung der gemeinsamen Bosskämpfe (seit 1.11.6): Ein Freund steht neben dir vor dem Fürsten eines Reichs. Trefft ihr
 * ihn beide, wird er stärker (Meldung über der Schnellleiste). Setzt der Freund den letzten Treffer, liegt seine Beute am
 * Boden – und dein Anteil in deinem Inventar, mit dem Sieg in deiner Statistik und deinen Fortschritten.
 */
final class PartyShowcase {
    private static final int OZ = -40;

    private PartyShowcase() {}

    static void fight(Deque<Step> steps) {
        RealmWarriorEntity[] lord = new RealmWarriorEntity[1];
        ServerPlayer[] friend = new ServerPlayer[1];
        steps.add(Step.once(minecraft -> {
            minecraft.options.guiScale().set(2);
            minecraft.resizeGui();
        }));
        steps.add(Step.server(level -> {
            int ground = Showcase.ground;
            Showcase.fill(level, new BlockPos(-12, ground - 1, OZ - 16), new BlockPos(12, ground - 1, OZ + 8), Blocks.GRASS_BLOCK.defaultBlockState());
            Showcase.fill(level, new BlockPos(-12, ground, OZ - 16), new BlockPos(12, ground + 10, OZ + 8), Blocks.AIR.defaultBlockState());
            Showcase.fill(level, new BlockPos(-2, ground - 1, OZ - 8), new BlockPos(2, ground - 1, OZ + 1), Blocks.COARSE_DIRT.defaultBlockState());
        }));
        steps.add(Step.command("gamemode survival"));
        steps.add(Step.command("clear @s"));
        steps.add(Step.command("effect give @s minecraft:resistance infinite 255 true"));
        steps.add(Step.command("item replace entity @s weapon.mainhand with minecraft:diamond_sword"));
        steps.add(Step.command("time set 1000"));
        steps.add(Step.command("weather clear"));
        steps.add(Step.hud(true));
        steps.add(Showcase.tp(-0.6, 0.0, OZ + 0.5, 180, 6));
        steps.add(Step.waitForChunks(200));
        steps.add(Step.waitTicks(20));
        steps.add(Step.server(level -> {
            int ground = Showcase.ground;
            RealmWarriorEntity warrior = ModEntities.REALM_WARRIORS.get(WarriorRole.LORD).get().create(level, EntitySpawnReason.COMMAND);
            if (warrior == null) {
                return;
            }
            BlockPos at = new BlockPos(0, ground, OZ - 5);
            warrior.snapTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, 0.0f, 0.0f);
            warrior.finalizeSpawn(level, level.getCurrentDifficultyAt(at), EntitySpawnReason.COMMAND, null);
            warrior.setRealm(Realm.values()[0]);
            warrior.setYHeadRot(0.0f);
            warrior.setYBodyRot(0.0f);
            var speed = warrior.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speed != null) {
                speed.setBaseValue(0.0);   // er bleibt im Bild
            }
            level.addFreshEntity(warrior);
            lord[0] = warrior;
            // links vor dir (rechts hältst du dein Schwert), mit Blick auf den König
            friend[0] = ShowcaseGuest.join(level, "Freund", new Vec3(-2.4, ground, OZ - 3.1), 246.0f, new ItemStack(Items.IRON_SWORD),
                    GameType.SURVIVAL);
            friend[0].getAbilities().invulnerable = true;   // er steht nur da
            // ein erfahrener Freund: über seinem Kopf steht seine Heldenstufe (seit 1.12)
            io.github.sirschrolli.kingdomsatwar.hero.HeroLevel.set(friend[0], new io.github.sirschrolli.kingdomsatwar.hero.HeroLevel.Hero(
                    io.github.sirschrolli.kingdomsatwar.hero.HeroLevel.total(64), java.util.List.of(), 0));
        }));
        steps.add(Step.waitTicks(30));
        steps.add(Step.server(level -> {
            ServerPlayer me = first(level);
            if (lord[0] != null) {
                lord[0].setTarget(me);   // so zeigt er seine Leiste
            }
        }));
        steps.add(Step.waitTicks(25));
        // beide treffen ihn – jetzt kämpfen zwei, und er wird stärker
        steps.add(Step.once(minecraft -> minecraft.gui.hud.getChat().clearMessages(false)));
        steps.add(Step.once(minecraft -> minecraft.player.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT, false)));
        steps.add(Step.server(level -> hit(level, lord[0], first(level), 9.0f)));
        steps.add(Step.waitTicks(6));
        steps.add(Step.server(level -> {
            if (friend[0] != null) {
                friend[0].swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT, true);
                hit(level, lord[0], friend[0], 9.0f);
            }
        }));
        steps.add(Step.waitTicks(12));
        steps.add(Step.once(minecraft -> minecraft.gui.toastManager().clear()));
        steps.add(Step.waitTicks(2));
        steps.add(Step.screenshot("party_1_kampf"));
        // der Freund setzt den letzten Treffer: seine Beute fällt, deine landet im Inventar
        steps.add(Step.once(minecraft -> minecraft.gui.hud.getChat().clearMessages(false)));
        steps.add(Step.server(level -> {
            if (friend[0] != null) {
                friend[0].swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT, true);
                hit(level, lord[0], friend[0], 100_000.0f);
            }
        }));
        // die Aufstiegs-Szene (die geteilte Heldenerfahrung reicht für eine Stufe) klingt ab, die Nachrichten bleiben stehen
        steps.add(Step.waitTicks(100));
        steps.add(Step.screenshot("party_2_sieg"));
        steps.add(Step.server(level -> {
            if (friend[0] != null) {
                ShowcaseGuest.leave(level, friend[0]);
            }
        }));
        steps.add(Step.command("effect clear @s"));
    }

    private static void hit(ServerLevel level, RealmWarriorEntity lord, ServerPlayer by, float damage) {
        if (lord != null && lord.isAlive()) {
            lord.damageCooldownTime = 0;
            lord.hurtServer(level, level.damageSources().playerAttack(by), damage);
        }
    }

    private static ServerPlayer first(ServerLevel level) {
        return level.getServer().getPlayerList().getPlayers().getFirst();
    }
}
