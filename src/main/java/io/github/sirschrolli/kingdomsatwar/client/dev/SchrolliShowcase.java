package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.arena.BossArenaKind;
import io.github.sirschrolli.kingdomsatwar.arena.BossFights;
import io.github.sirschrolli.kingdomsatwar.arena.BossLair;
import io.github.sirschrolli.kingdomsatwar.block.creator.CreatorAltarBlock;
import io.github.sirschrolli.kingdomsatwar.block.creator.CreatorAltarBlockEntity;
import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.entity.creator.DivineShotEntity;
import io.github.sirschrolli.kingdomsatwar.entity.creator.SchrolliEntity;
import io.github.sirschrolli.kingdomsatwar.entity.creator.SchrolliEntity.Attack;
import io.github.sirschrolli.kingdomsatwar.entity.creator.SeraphlingEntity;
import io.github.sirschrolli.kingdomsatwar.entity.creator.TimeFreeze;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.magic.fx.SpellFxEntity;
import io.github.sirschrolli.kingdomsatwar.registry.CreatorBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import java.util.Deque;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EndRodBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Vorführung ({@code creator_boss}): Schrolli der Schöpfer, ganz wie im Spiel – der Altar in der Oberwelt mit dem
 * Schöpferfunken, die Berührung (die Säule aus Licht), der Sog in die Schöpfung; dort der Hof von weit oben, der Thron, die
 * vier Welten, die Unterseite, der Himmel; sein Erscheinen samt Titel und Leiste, Bildnisse, alle Künste der ersten Gestalt
 * gegen einen (zähen) Ritter, der Aufstieg in die Wahre Gestalt (der Himmel öffnet sich), die Künste der Wahren Gestalt, die
 * Neuschöpfung und ihr Abbruch, Zepter und Schwingen, sein Vergehen, der Titel „Gottbezwinger“, die Beute vor dem Thron und
 * der erloschene Altar nach der Rückkehr.
 */
final class SchrolliShowcase {
    private SchrolliShowcase() {}

    /** Was die Vorführung über den Hof weiß (auf dem Server gesetzt, im Client gelesen). */
    private static final class Court {
        volatile BlockPos altar = BlockPos.ZERO;
        volatile Vec3 c = Vec3.ZERO;
        volatile int boss = -1;
        volatile int knight = -1;
        volatile int stand = -1;
        volatile int state;
        final double[] cam = new double[3];
    }

    static void boss(Deque<Step> steps) {
        Court s = new Court();
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> {
            minecraft.options.renderDistance().set(16);
            minecraft.options.gamma().set(1.0);
        }));
        for (String command : new String[] {"gamerule spawn_monsters false", "gamerule spawn_mobs false", "gamerule advance_time false",
                "time set 6000", "weather clear", "difficulty normal", "gamemode spectator"}) {
            steps.add(Step.command(command));
        }
        // ------------------------------------------------------------------ der Altar in der Oberwelt
        steps.add(Step.server(any -> shrine(any.getServer().overworld(), s)));
        steps.add(Step.until(() -> s.state != 0, 600));
        steps.add(overworldCam(s, 3.6, 2.3, 4.6, 0.0, 1.25, 0.0));
        steps.add(Step.waitTicks(40));
        steps.add(Step.waitForChunks(800));
        steps.add(overworldCam(s, 3.6, 2.3, 4.6, 0.0, 1.25, 0.0));
        steps.add(Step.waitTicks(10));
        steps.add(shot("schoepfer_altar"));
        // die Berührung: der Altar ruft, und wer im Kreis steht, wird in die Schöpfung gezogen
        steps.add(Step.command("gamemode survival"));
        steps.add(Step.server(any -> {
            ServerLevel level = any.getServer().overworld();
            ServerPlayer player = cam(any.getServer());
            hover(player);
            Vec3 a = Vec3.atBottomCenterOf(s.altar);
            look(player, level, a.add(7.0, 7.5, 10.0), a.add(0.0, 4.0, 0.0));
            if (level.getBlockEntity(s.altar) instanceof CreatorAltarBlockEntity altar) {
                altar.touch(level, player);
                KingdomsAtWar.LOGGER.info("[Showcase] Schöpfer: der Altar ruft ({})", altar.state());
            }
        }));
        steps.add(Step.waitTicks(72));
        steps.add(shot("schoepfer_altar_ruf"));
        steps.add(Step.until(() -> inCourt(Minecraft.getInstance()), 1200));
        steps.add(Step.command("gamemode creative"));
        steps.add(Step.server(any -> findCourt(any.getServer(), s)));
        steps.add(Step.until(() -> s.state != 1, 200));
        steps.add(Step.waitTicks(40));
        steps.add(Step.waitForChunks(1600));
        // ------------------------------------------------------------------ der Hof
        steps.add(courtCam(s, 64.0, 46.0, 80.0, 0.0, -2.0, -4.0));
        steps.add(Step.waitTicks(40));
        steps.add(Step.waitForChunks(900));
        steps.add(courtCam(s, 64.0, 46.0, 80.0, 0.0, -2.0, -4.0));
        steps.add(Step.waitTicks(6));
        steps.add(shot("schoepfer_hof"));
        steps.add(courtCam(s, -38.0, 30.0, 52.0, 0.0, 4.0, -18.0));
        steps.add(Step.waitTicks(20));
        steps.add(shot("schoepfer_hof_west"));
        steps.add(courtCam(s, 0.0, 5.0, 24.0, 0.0, 9.0, -36.0));
        steps.add(Step.waitTicks(20));
        steps.add(shot("schoepfer_hof_thron"));
        steps.add(courtCam(s, 9.0, 6.5, -18.0, 0.0, 6.0, -36.0));
        steps.add(Step.waitTicks(20));
        steps.add(shot("schoepfer_thron"));
        // die vier Welten an ihren Brücken (Garten, Glut, Eis, End)
        String[] worlds = {"garten", "glut", "eis", "end"};
        for (int k = 0; k < 4; k++) {
            double a = Math.toRadians(45.0 + k * 90.0);
            double side = Math.toRadians(45.0 + k * 90.0 + 90.0);
            Vec3 island = new Vec3(Math.cos(a) * 80.0, 3.0, Math.sin(a) * 80.0);
            Vec3 eye = new Vec3(Math.cos(a) * 46.0 + Math.cos(side) * 20.0, 16.0, Math.sin(a) * 46.0 + Math.sin(side) * 20.0);
            steps.add(courtCam(s, eye.x, eye.y, eye.z, island.x, island.y, island.z));
            steps.add(Step.waitTicks(16));
            steps.add(Step.waitForChunks(400));
            steps.add(courtCam(s, eye.x, eye.y, eye.z, island.x, island.y, island.z));
            steps.add(Step.waitTicks(4));
            steps.add(shot("schoepfer_welt_" + worlds[k]));
        }
        steps.add(courtCam(s, 34.0, -44.0, 40.0, 0.0, -22.0, 0.0));
        steps.add(Step.waitTicks(20));
        steps.add(shot("schoepfer_unterseite"));
        // der Himmel: das Sonnenrad im Süden, darunter der Schöpfer über seinem Hof
        steps.add(courtCam(s, 2.0, 2.0, -12.0, -12.0, 40.0, 30.0));
        steps.add(Step.waitTicks(16));
        steps.add(shot("schoepfer_himmel"));
        // ------------------------------------------------------------------ sein Erscheinen
        steps.add(Step.server(any -> {
            ServerLevel court = court(any.getServer());
            clearEffects(court, s);
            if (boss(court, s) instanceof SchrolliEntity boss) {
                boss.setNoAi(false);
                boss.awaken();
            }
        }));
        steps.add(attackAt(s, Attack.AWAKEN, 36));
        steps.add(courtLook(s, 7.0, 1.4, 21.0, true, 2.0));
        steps.add(Step.waitTicks(1));
        steps.add(shot("schoepfer_erscheinen"));
        steps.add(attackAt(s, Attack.AWAKEN, SchrolliEntity.AWAKEN_LAND));
        steps.add(courtLook(s, 12.0, 3.0, 22.0, true, 3.0));
        steps.add(Step.waitTicks(1));
        steps.add(shot("schoepfer_ankunft"));
        steps.add(Step.hud(true));
        steps.add(attackAt(s, Attack.AWAKEN, SchrolliEntity.AWAKEN_LAND + 14));
        steps.add(courtLook(s, 0.0, 3.4, 22.0, true, 3.2));
        steps.add(Step.waitTicks(2));
        steps.add(shot("schoepfer_titel"));
        steps.add(Step.hud(false));
        steps.add(Step.waitTicks(60));
        // Bildnisse (er hält still, der Kamera zugewandt)
        steps.add(pose(s, 3.0));
        steps.add(faceCam(s, 12.5, 2.6, 3.0, 14.0));
        steps.add(Step.waitTicks(12));
        steps.add(shot("schoepfer_bild"));
        steps.add(faceCam(s, 5.2, 5.4, 4.9, 10.0));
        steps.add(Step.waitTicks(8));
        steps.add(shot("schoepfer_gesicht"));
        steps.add(faceCam(s, 10.5, 3.2, 3.0, 75.0));
        steps.add(Step.waitTicks(8));
        steps.add(shot("schoepfer_seite"));
        steps.add(faceCam(s, 11.0, 4.6, 3.6, 158.0));
        steps.add(Step.waitTicks(8));
        steps.add(shot("schoepfer_ruecken"));
        steps.add(release(s));
        steps.add(Step.hud(true));
        steps.add(courtLook(s, 4.0, 3.0, 24.0, true, 3.0));
        steps.add(Step.waitTicks(6));
        steps.add(shot("schoepfer_leiste"));
        steps.add(Step.hud(false));
        // ------------------------------------------------------------------ die Künste der ersten Gestalt
        steps.add(Step.server(any -> knight(court(any.getServer()), s)));
        arts(steps, s, new String[][] {
                {"LANCES", "13", "schoepfer_lichtspeere", "-30", "17", "2.5", "0.15"},
                {"LANCES", "30", "schoepfer_lichtspeere_flug", "-70", "14", "3", "0.55"},
                {"HALO", "24", "schoepfer_heiligenschein", "-60", "16", "3", "0.55"},
                {"PILLARS", "35", "schoepfer_saeulen", "-35", "22", "9", "0.75"},
                {"STARFALL", "54", "schoepfer_sternenregen", "-25", "28", "13", "0.55"},
                {"SMITE", "36", "schoepfer_zepterschlag", "-50", "15", "4.5", "0.25"},
                {"SUNBEAM", "64", "schoepfer_strahl", "-60", "21", "6", "0.45"},
                {"ORB", "32", "schoepfer_weltkugel", "-60", "17", "4", "0.6"},
                {"FEATHERS", "24", "schoepfer_federsturm", "-40", "16", "3", "0.4"},
                {"JUDGMENT", "44", "schoepfer_urteil_kreis", "-40", "15", "7", "0.85"},
                {"JUDGMENT", "71", "schoepfer_urteil", "-40", "19", "7", "0.75"},
                {"SERAPHS", "38", "schoepfer_seraphim", "-30", "16", "3", "0.2"},
                {"BLINK", "21", "schoepfer_lichtsprung", "-60", "12", "3", "0.5"},
                {"GALE", "32", "schoepfer_schwingensturm", "-45", "17", "4", "0.2"},
                {"SUNWHEEL", "58", "schoepfer_sonnenrad", "-20", "30", "17", "0.0"},
                {"CHOIR", "45", "schoepfer_engelschor", "-45", "22", "6", "0.3"},
                {"WORD", "46", "schoepfer_es_werde_licht", "-20", "19", "2", "0.0"},
                {"TIMESTOP", "62", "schoepfer_zeitstillstand", "-50", "16", "4", "0.6"},
        });
        steps.add(Step.waitTicks(30));
        // ------------------------------------------------------------------ der Aufstieg in die Wahre Gestalt
        steps.add(Step.server(any -> {
            ServerLevel court = court(any.getServer());
            clearEffects(court, s);
            place(court, s, 12.0, 8.0);
            if (boss(court, s) instanceof SchrolliEntity boss) {
                boss.forceAttack(Attack.NONE);
                boss.setHealth(5.0f);
                boss.damageCooldownTime = 0;
                boss.hurtServer(court, court.damageSources().generic(), 1.0e6f);
            }
        }));
        steps.add(attackAt(s, Attack.ASCEND, 44));
        steps.add(follow(s, -30.0, 19.0, 2.0, 0.0));
        steps.add(Step.waitTicks(1));
        steps.add(shot("schoepfer_aufstieg"));
        steps.add(attackAt(s, Attack.ASCEND, SchrolliEntity.ASCEND_SHATTER + 2));
        steps.add(follow(s, -30.0, 20.0, 2.0, 0.0));
        steps.add(Step.waitTicks(1));
        steps.add(shot("schoepfer_zerbrochen"));
        steps.add(attackAt(s, Attack.ASCEND, SchrolliEntity.ASCEND_REVEAL - 8));
        steps.add(follow(s, -30.0, 22.0, 2.0, 0.0));
        steps.add(Step.waitTicks(1));
        steps.add(shot("schoepfer_sonne"));
        steps.add(Step.hud(true));
        steps.add(attackAt(s, Attack.ASCEND, SchrolliEntity.ASCEND_REVEAL + 10));
        steps.add(follow(s, -25.0, 17.0, 1.5, 0.0));
        steps.add(Step.waitTicks(2));
        steps.add(shot("schoepfer_wahre_gestalt"));
        steps.add(Step.hud(false));
        steps.add(attackAt(s, Attack.ASCEND, SchrolliEntity.ASCEND_BLAST + 3));
        steps.add(follow(s, -30.0, 26.0, 11.0, 0.0));
        steps.add(Step.waitTicks(1));
        steps.add(shot("schoepfer_welle"));
        steps.add(attackAt(s, Attack.ASCEND, Attack.ASCEND.ordinal() == 0 ? 0 : SchrolliEntity.ASCEND_TICKS + 2));
        steps.add(Step.waitTicks(20));
        // der Himmel hat sich geöffnet: das Auge aus Sternen, Risse aus Licht
        steps.add(pose(s, 13.0));
        steps.add(courtCam(s, 0.0, 2.0, 13.0, 0.0, 40.0, -1.0));
        steps.add(Step.waitTicks(16));
        steps.add(shot("schoepfer_auge"));
        steps.add(courtCam(s, 64.0, 46.0, 80.0, 0.0, -2.0, -4.0));
        steps.add(Step.waitTicks(20));
        steps.add(Step.waitForChunks(400));
        steps.add(courtCam(s, 64.0, 46.0, 80.0, 0.0, -2.0, -4.0));
        steps.add(Step.waitTicks(4));
        steps.add(shot("schoepfer_hof_wahr"));
        steps.add(courtCam(s, 2.0, 2.0, -12.0, -12.0, 40.0, 30.0));
        steps.add(Step.waitTicks(16));
        steps.add(shot("schoepfer_himmel_wahr"));
        // die Wahre Gestalt aus der Nähe
        steps.add(pose(s, 3.0));
        steps.add(faceCam(s, 15.0, 3.0, 3.8, -14.0));
        steps.add(Step.waitTicks(12));
        steps.add(shot("schoepfer_wahr_bild"));
        steps.add(faceCam(s, 6.6, 7.0, 6.3, -10.0));
        steps.add(Step.waitTicks(8));
        steps.add(shot("schoepfer_wahr_gesicht"));
        steps.add(faceCam(s, 14.0, 6.0, 4.6, 160.0));
        steps.add(Step.waitTicks(8));
        steps.add(shot("schoepfer_wahr_ruecken"));
        steps.add(release(s));
        steps.add(Step.hud(true));
        steps.add(courtLook(s, 5.0, 3.0, 28.0, true, 4.0));
        steps.add(Step.waitTicks(6));
        steps.add(shot("schoepfer_wahr_leiste"));
        steps.add(Step.hud(false));
        // ------------------------------------------------------------------ die Künste der Wahren Gestalt
        arts(steps, s, new String[][] {
                {"PLANETS", "40", "schoepfer_weltenwurf", "-35", "24", "6", "0.25"},
                {"PLANETS", "72", "schoepfer_weltenwurf_flug", "-70", "22", "6", "0.6"},
                {"SUPERNOVA", "124", "schoepfer_supernova", "@", "30", "8", "44"},
                {"SUPERNOVA", "142", "schoepfer_supernova_blitz", "@", "30", "8", "44"},
                {"GENESIS", "86", "schoepfer_urknall_sog", "@", "20", "12", "26"},
                {"GENESIS", "112", "schoepfer_urknall", "@", "24", "14", "30"},
                {"CONSTELLATION", "46", "schoepfer_sternbild_linien", "-40", "19", "8", "0.8"},
                {"CONSTELLATION", "62", "schoepfer_sternbild", "-40", "19", "8", "0.8"},
                {"TWIN_HALOS", "46", "schoepfer_doppelklinge", "-30", "24", "11", "0.5"},
                {"APOCALYPSE", "92", "schoepfer_juengstes_gericht", "@", "42", "30", "62"},
                {"EYE_BEAMS", "66", "schoepfer_augenstrahlen", "-45", "21", "5", "0.4"},
                {"COMET", "58", "schoepfer_komet", "-60", "22", "5", "0.75"},
                {"COMET", "72", "schoepfer_komet_einschlag", "-50", "19", "6", "0.5"},
                {"WORLD_SPLIT", "30", "schoepfer_weltenriss", "-30", "23", "12", "0.2"},
                {"WORLD_SPLIT", "40", "schoepfer_weltenriss_licht", "-30", "23", "12", "0.2"},
                {"TIMESTOP", "66", "schoepfer_wahr_zeitstillstand", "-50", "18", "5", "0.6"},
        });
        // die Neuschöpfung – und wie man sie abbricht
        steps.add(Step.server(any -> {
            ServerLevel court = court(any.getServer());
            clearEffects(court, s);
            place(court, s, 14.0, 8.0);
            if (boss(court, s) instanceof SchrolliEntity boss) {
                boss.setHealth(boss.getMaxHealth() * 0.24f);
                boss.forceAttack(Attack.RECREATION);
            }
        }));
        steps.add(Step.hud(true));
        steps.add(attackAt(s, Attack.RECREATION, 72));
        steps.add(follow(s, -30.0, 20.0, 5.0, 0.0));
        steps.add(Step.waitTicks(2));
        steps.add(shot("schoepfer_neuschoepfung"));
        steps.add(Step.hud(false));
        steps.add(Step.server(any -> {
            ServerLevel court = court(any.getServer());
            if (boss(court, s) instanceof SchrolliEntity boss) {
                for (int k = 0; k < 3; k++) {
                    boss.damageCooldownTime = 0;
                    boss.hurtServer(court, court.damageSources().generic(), 60.0f);
                }
            }
        }));
        steps.add(attackAt(s, Attack.STUNNED, 24));
        steps.add(follow(s, -40.0, 13.0, 3.0, 0.0));
        steps.add(Step.waitTicks(1));
        steps.add(shot("schoepfer_taumelnd"));
        steps.add(Step.waitTicks(70));
        // ------------------------------------------------------------------ Zepter und Schwingen
        steps.add(Step.server(any -> gear(court(any.getServer()), s)));
        steps.add(standCam(s, 4.4, 1.9, 0.0, 1.25));
        steps.add(Step.waitTicks(20));
        steps.add(shot("schoepfer_beute"));
        steps.add(standCam(s, 4.0, 2.2, 175.0, 1.35));
        steps.add(Step.waitTicks(8));
        steps.add(shot("schoepfer_beute_ruecken"));
        steps.add(Step.server(any -> {
            ServerPlayer player = cam(any.getServer());
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(CreatorBlocks.CREATOR_SCEPTER.get()));
        }));
        steps.add(courtLook(s, 10.0, 2.0, 30.0, true, 3.0));
        steps.add(Step.hud(true));
        steps.add(Step.waitTicks(10));
        steps.add(shot("schoepfer_zepter_hand"));
        steps.add(Step.server(any -> {
            ServerPlayer player = cam(any.getServer());
            ServerLevel court = court(any.getServer());
            player.getCooldowns().removeCooldown(player.getCooldowns().getCooldownGroup(player.getMainHandItem()));
            player.getMainHandItem().use(court, player, InteractionHand.MAIN_HAND);
        }));
        steps.add(Step.waitTicks(3));
        steps.add(shot("schoepfer_zepter_speer"));
        steps.add(Step.server(any -> cam(any.getServer()).setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY)));
        steps.add(Step.hud(false));
        // ------------------------------------------------------------------ sein Vergehen
        steps.add(Step.server(any -> {
            ServerLevel court = court(any.getServer());
            clearEffects(court, s);
            if (court.getEntity(s.stand) instanceof Entity stand) {
                stand.discard();
            }
            place(court, s, 12.0, 6.0);
            if (court.getEntity(s.knight) instanceof Entity knight) {
                knight.discard();
            }
            if (boss(court, s) instanceof SchrolliEntity boss) {
                ServerPlayer player = cam(any.getServer());
                boss.forceAttack(Attack.NONE);
                boss.setHealth(5.0f);
                boss.damageCooldownTime = 0;
                boss.setLastHurtByPlayer(player, 100);
                boss.hurtServer(court, player.damageSources().playerAttack(player), 1.0e6f);
            }
        }));
        steps.add(dyingAt(s, 64));
        steps.add(courtLook(s, 6.0, 2.0, 20.0, true, 3.0));
        steps.add(Step.waitTicks(1));
        steps.add(shot("schoepfer_vergehen"));
        steps.add(dyingAt(s, 206));
        steps.add(courtLook(s, 14.0, 4.0, 30.0, true, 8.0));
        steps.add(Step.waitTicks(1));
        steps.add(shot("schoepfer_lichtsaeule"));
        steps.add(Step.hud(true));
        steps.add(dyingAt(s, SchrolliEntity.DEATH_TICKS - 8));
        steps.add(courtLook(s, 10.0, 3.0, 26.0, true, 4.0));
        steps.add(Step.waitTicks(2));
        steps.add(shot("schoepfer_gottbezwinger"));
        steps.add(Step.hud(false));
        // die Beute vor dem Thron
        steps.add(Step.waitTicks(14));
        steps.add(courtCam(s, 5.5, 6.6, -18.0, 0.0, 3.5, -27.0));
        steps.add(Step.waitTicks(14));
        steps.add(shot("schoepfer_beute_thron"));
        // zurück am Altar: der Funke ist verglüht
        steps.add(Step.until(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            return minecraft.level != null && minecraft.level.dimension() == Level.OVERWORLD;
        }, 1200));
        steps.add(Step.command("gamemode spectator"));
        steps.add(Step.waitTicks(20));
        steps.add(overworldCam(s, 3.6, 2.3, 4.6, 0.0, 1.25, 0.0));
        steps.add(Step.waitTicks(30));
        steps.add(shot("schoepfer_altar_erloschen"));
        steps.add(Step.server(any -> {
            ServerLevel level = any.getServer().overworld();
            KingdomsAtWar.LOGGER.info("[Showcase] Schöpfer: zurück am Altar ({})",
                    level.getBlockEntity(s.altar) instanceof CreatorAltarBlockEntity altar ? altar.state() : "-");
        }));
        steps.add(Step.command("gamerule advance_time true"));
    }

    // =====================================================================================
    // Die Künste
    // =====================================================================================

    /**
     * Eine Reihe von Künsten: {Kunst, Tick des Bildes, Name, Blickwinkel zur Linie Schöpfer → Ritter (Grad), Abstand, Höhe,
     * Blick zum Ritter 0..1}. Steht statt des Winkels ein {@code @}, steht die Kamera fest über dem Hof: (x, Höhe, z) relativ
     * zur Mitte, der Blick auf ihn.
     */
    private static void arts(Deque<Step> steps, Court s, String[][] shots) {
        java.util.Set<String> started = new java.util.HashSet<>();
        for (String[] shot : shots) {
            Attack attack = Attack.valueOf(shot[0]);
            int tick = Integer.parseInt(shot[1]);
            if (started.add(shot[0])) {
                double distance = switch (attack) {
                    case SMITE, BLINK, GALE, COMET -> 13.0;
                    case SUNWHEEL, SUPERNOVA, GENESIS, APOCALYPSE -> 10.0;
                    default -> 15.0;
                };
                steps.add(reset(s, attack, distance, 8.0));
            }
            steps.add(attackAt(s, attack, tick - 4));
            if (shot[3].equals("@")) {
                steps.add(courtLook(s, Double.parseDouble(shot[4]), Double.parseDouble(shot[5]), Double.parseDouble(shot[6]), true, 3.0));
            } else {
                steps.add(follow(s, Double.parseDouble(shot[3]), Double.parseDouble(shot[4]), Double.parseDouble(shot[5]), Double.parseDouble(shot[6])));
            }
            steps.add(Step.waitTicks(1));
            steps.add(shot(shot[2]));
        }
    }

    /** Vor einer Kunst: aufräumen, Plätze einnehmen, dann die Kunst beginnen. */
    private static Step reset(Court s, Attack attack, double distance, double height) {
        return Step.server(any -> {
            ServerLevel court = court(any.getServer());
            clearEffects(court, s);
            place(court, s, distance, height);
            if (boss(court, s) instanceof SchrolliEntity boss) {
                boss.forceAttack(attack);
            }
        });
    }

    /** Räumt auf, was Künste zurückließen: Geschosse, Lichter, Seraphlinge (und die Starre). */
    private static void clearEffects(ServerLevel court, Court s) {
        if (court == null) {
            return;
        }
        AABB area = new AABB(s.c, s.c).inflate(90.0, 80.0, 90.0);
        for (Entity e : court.getEntitiesOfClass(Entity.class, area, e -> e instanceof SpellFxEntity || e instanceof DivineShotEntity
                || e instanceof SeraphlingEntity)) {
            e.discard();
        }
        for (LivingEntity e : court.getEntitiesOfClass(LivingEntity.class, area, TimeFreeze::isFrozen)) {
            TimeFreeze.unfreeze(e);
        }
    }

    /** Ritter auf seinen Platz (zehn Blöcke südlich der Mitte), der Schöpfer {@code distance} nördlich von ihm, schwebend. */
    private static void place(ServerLevel court, Court s, double distance, double height) {
        if (court == null) {
            return;
        }
        Vec3 k = s.c.add(0.0, 0.0, 10.0);
        if (court.getEntity(s.knight) instanceof Mob knight) {
            TimeFreeze.unfreeze(knight);
            knight.teleportTo(k.x, k.y, k.z);
            knight.setDeltaMovement(Vec3.ZERO);
            knight.setYRot(180.0f);
            knight.setYBodyRot(180.0f);
            knight.setYHeadRot(180.0f);
            knight.removeAllEffects();
            knight.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 1_000_000, 4, false, false));
            knight.setHealth(knight.getMaxHealth());
            knight.clearFire();
        }
        if (boss(court, s) instanceof SchrolliEntity boss) {
            boss.setNoAi(false);
            Vec3 b = k.add(0.0, height, -distance);
            boss.teleportTo(b.x, b.y, b.z);
            boss.setDeltaMovement(Vec3.ZERO);
            boss.setYRot(0.0f);
            boss.setYBodyRot(0.0f);
            boss.setYHeadRot(0.0f);
            if (court.getEntity(s.knight) instanceof LivingEntity knight) {
                boss.setTarget(knight);
            }
        }
    }

    /** Ein zäher Ritter (ohne eigenen Willen, unverwundbar durch Widerstand) als Gegner. */
    private static void knight(ServerLevel court, Court s) {
        if (court == null) {
            return;
        }
        Mob knight = ModEntities.SOLDIERS.get(SoldierType.KNIGHT).get().create(court, EntitySpawnReason.COMMAND);
        if (knight == null) {
            return;
        }
        Vec3 k = s.c.add(0.0, 0.0, 10.0);
        knight.snapTo(k.x, k.y, k.z, 180.0f, 0.0f);
        knight.setNoAi(true);
        knight.setPersistenceRequired();
        knight.setSilent(true);
        var health = knight.getAttribute(Attributes.MAX_HEALTH);
        if (health != null) {
            health.setBaseValue(500.0);
        }
        knight.setHealth(knight.getMaxHealth());
        knight.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 1_000_000, 4, false, false));
        court.addFreshEntity(knight);
        s.knight = knight.getId();
    }

    /** Ein Rüstungsständer mit Zepter und Schwingen auf dem Hof (der Thron im Hintergrund). */
    private static void gear(ServerLevel court, Court s) {
        if (court == null) {
            return;
        }
        clearEffects(court, s);
        if (boss(court, s) instanceof SchrolliEntity boss) {
            boss.forceAttack(Attack.NONE);
            boss.setNoAi(true);
            Vec3 b = s.c.add(0.0, 16.0, -24.0);
            boss.teleportTo(b.x, b.y, b.z);
            boss.setDeltaMovement(Vec3.ZERO);
        }
        if (court.getEntity(s.knight) instanceof Mob knight) {
            knight.teleportTo(s.c.x + 20.0, s.c.y, s.c.z + 20.0);
        }
        Vec3 p = s.c.add(-6.0, 0.0, -9.0);
        ArmorStand stand = new ArmorStand(court, p.x, p.y, p.z);
        stand.setShowArms(true);
        stand.setYRot(160.0f);
        stand.setYBodyRot(160.0f);
        stand.setYHeadRot(160.0f);
        stand.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(CreatorBlocks.CREATOR_SCEPTER.get()));
        stand.setItemSlot(EquipmentSlot.CHEST, new ItemStack(CreatorBlocks.CREATOR_WINGS.get()));
        court.addFreshEntity(stand);
        s.stand = stand.getId();
    }

    // =====================================================================================
    // Der Altar in der Oberwelt
    // =====================================================================================

    /** Ein kleines Heiligtum aus Quarz und Gold in der flachen Welt; darauf der Altar mit dem Schöpferfunken. */
    private static void shrine(ServerLevel level, Court s) {
        for (int cx = -3; cx <= 3; cx++) {
            for (int cz = -3; cz <= 3; cz++) {
                level.setChunkForced(cx, cz, true);
                level.getChunk(cx, cz);
            }
        }
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, 0, 0);
        BlockPos base = new BlockPos(0, y, 0);
        BlockState smooth = Blocks.SMOOTH_QUARTZ.defaultBlockState();
        BlockState gold = Blocks.GOLD_BLOCK.defaultBlockState();
        BlockState chiseled = Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState();
        BlockState bricks = Blocks.QUARTZ_BRICKS.defaultBlockState();
        for (int dx = -7; dx <= 7; dx++) {
            for (int dz = -7; dz <= 7; dz++) {
                double r = Math.sqrt(dx * dx + dz * dz);
                if (r > 7.3) {
                    continue;
                }
                BlockState top = r < 1.5 ? gold : r < 4.6 ? smooth : r < 5.6 ? gold : r < 6.5 ? smooth : bricks;
                if (r >= 2.5 && r < 4.6 && (dx == 0 || dz == 0)) {
                    top = chiseled;
                }
                level.setBlock(base.offset(dx, -1, dz), top, 3);
            }
        }
        for (int k = 0; k < 4; k++) {
            double a = Math.toRadians(45.0 + k * 90.0);
            BlockPos p = base.offset((int) Math.round(Math.cos(a) * 6.0), 0, (int) Math.round(Math.sin(a) * 6.0));
            for (int h = 0; h < 4; h++) {
                level.setBlock(p.above(h), h == 3 ? chiseled : Blocks.QUARTZ_PILLAR.defaultBlockState(), 3);
            }
            level.setBlock(p.above(4), gold, 3);
            level.setBlock(p.above(5), Blocks.END_ROD.defaultBlockState().setValue(EndRodBlock.FACING, Direction.UP), 3);
        }
        s.altar = base;
        level.setBlock(base, CreatorBlocks.CREATOR_ALTAR.get().defaultBlockState().setValue(CreatorAltarBlock.FACING, Direction.SOUTH), 3);
        if (level.getBlockEntity(base) instanceof CreatorAltarBlockEntity altar) {
            altar.insertSpark(level, cam(level.getServer()));
        }
        KingdomsAtWar.LOGGER.info("[Showcase] Schöpfer: Altar bei {} ({})", base, level.getBlockState(base));
        s.state = 1;
    }

    /** Findet den Kampf des Spielers: die Mitte des Hofs und den Schöpfer. */
    private static void findCourt(MinecraftServer server, Court s) {
        ServerPlayer player = cam(server);
        ServerLevel court = court(server);
        BossFights.Fight fight = BossFights.of(server).byFighter(player.getUUID());
        if (court == null || fight == null) {
            KingdomsAtWar.LOGGER.info("[Showcase] Schöpfer: kein Kampf gefunden");
            s.state = -1;
            return;
        }
        s.c = Vec3.atBottomCenterOf(fight.center());
        LivingEntity boss = court.getBlockEntity(fight.trigger()) instanceof BossLair replica ? replica.arenaBoss(court) : null;
        s.boss = boss != null ? boss.getId() : -1;
        KingdomsAtWar.LOGGER.info("[Showcase] Schöpfer: Hof bei {}, Abbild des Altars bei {}, Schöpfer {}", s.c, fight.trigger(), boss);
        s.state = 2;
    }

    // =====================================================================================
    // Hilfen
    // =====================================================================================

    private static ServerPlayer cam(MinecraftServer server) {
        return server.getPlayerList().getPlayers().getFirst();
    }

    private static ServerLevel court(MinecraftServer server) {
        return server.getLevel(BossArenaKind.CREATOR.level());
    }

    private static SchrolliEntity boss(ServerLevel court, Court s) {
        return court != null && court.getEntity(s.boss) instanceof SchrolliEntity boss ? boss : null;
    }

    private static boolean inCourt(Minecraft minecraft) {
        return minecraft.level != null && minecraft.level.dimension() == BossArenaKind.CREATOR.level();
    }

    private static Step shot(String name) {
        return Step.wanted(name) ? Step.screenshot(name) : Step.once(minecraft -> { });
    }

    /** Wartet, bis der Schöpfer (im Client) die Kunst {@code attack} seit mindestens {@code tick} Ticks zeigt – oder sie beendet hat. */
    private static Step attackAt(Court s, Attack attack, int tick) {
        boolean[] seen = {false};
        return Step.until(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level == null || !(minecraft.level.getEntity(s.boss) instanceof SchrolliEntity boss)) {
                return false;
            }
            if (boss.currentAttack() == attack) {
                seen[0] = true;
                return boss.attackTime(0.0f) >= tick;
            }
            return seen[0];
        }, 700);
    }

    /** Wartet, bis er (im Client) seit {@code ticks} Ticks vergeht. */
    private static Step dyingAt(Court s, int ticks) {
        return Step.until(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level == null || !(minecraft.level.getEntity(s.boss) instanceof SchrolliEntity boss)) {
                return true;
            }
            return boss.deathTime >= ticks;
        }, 600);
    }

    /** Er hält still, {@code height} über der Mitte, nach Süden gewandt. */
    private static Step pose(Court s, double height) {
        return Step.server(any -> {
            ServerLevel court = court(any.getServer());
            clearEffects(court, s);
            if (boss(court, s) instanceof SchrolliEntity boss) {
                boss.forceAttack(Attack.NONE);
                boss.setNoAi(true);
                Vec3 b = s.c.add(0.0, height, 0.0);
                boss.snapTo(b.x, b.y, b.z, 0.0f, 0.0f);
                boss.setYBodyRot(0.0f);
                boss.setYHeadRot(0.0f);
                boss.setDeltaMovement(Vec3.ZERO);
            }
        });
    }

    private static Step release(Court s) {
        return Step.server(any -> {
            if (boss(court(any.getServer()), s) instanceof SchrolliEntity boss) {
                boss.setNoAi(false);
            }
        });
    }

    /**
     * Kamera beim Schöpfer: im Abstand {@code distance}, um {@code angle} Grad aus der Linie Schöpfer → Ritter gedreht,
     * {@code height} über seiner Brust; Blick auf ihn ({@code toward} 0) oder auf den Ritter ({@code toward} 1).
     */
    private static Step follow(Court s, double angle, double distance, double height, double toward) {
        return Step.server(any -> {
            ServerLevel court = court(any.getServer());
            SchrolliEntity boss = boss(court, s);
            if (boss == null) {
                return;
            }
            Vec3 c = boss.position().add(boss.getDeltaMovement().scale(3.0)).add(0.0, boss.getBbHeight() * 0.5, 0.0);
            Vec3 k = court.getEntity(s.knight) instanceof Entity knight ? knight.position().add(0.0, 1.0, 0.0) : s.c.add(0.0, 1.0, 10.0);
            Vec3 line = k.subtract(c).multiply(1.0, 0.0, 1.0);
            if (line.lengthSqr() < 1.0e-3) {
                line = new Vec3(0.0, 0.0, 1.0);
            }
            Vec3 focus = c.add(k.subtract(c).scale(toward));
            Vec3 dir = line.normalize().yRot((float) Math.toRadians(angle));
            Vec3 eye = focus.add(dir.scale(distance)).add(0.0, height, 0.0);
            look(cam(any.getServer()), court, free(court, eye, focus), focus);
        });
    }

    /**
     * Kamera vor ihm (aus seiner Blickrichtung um {@code angle} Grad gedreht) im Abstand {@code distance}, {@code height}
     * über seinen Füßen; Blick auf ihn in der Höhe {@code look} (die Wahre Gestalt ist größer: alles wächst mit).
     */
    private static Step faceCam(Court s, double distance, double height, double look, double angle) {
        return Step.server(any -> {
            ServerLevel court = court(any.getServer());
            SchrolliEntity boss = boss(court, s);
            if (boss == null) {
                return;
            }
            double yaw = Math.toRadians(boss.getYRot() + angle);
            Vec3 forward = new Vec3(-Math.sin(yaw), 0.0, Math.cos(yaw));
            Vec3 eye = boss.position().add(forward.scale(distance)).add(0.0, height, 0.0);
            Vec3 focus = boss.position().add(0.0, look, 0.0);
            look(cam(any.getServer()), court, free(court, eye, focus), focus);
        });
    }

    /** Kamera am Rüstungsständer: im Abstand {@code distance}, aus seiner Blickrichtung um {@code angle} Grad gedreht. */
    private static Step standCam(Court s, double distance, double height, double angle, double look) {
        return Step.server(any -> {
            ServerLevel court = court(any.getServer());
            if (court == null || !(court.getEntity(s.stand) instanceof Entity stand)) {
                return;
            }
            double yaw = Math.toRadians(stand.getYRot() + angle);
            Vec3 forward = new Vec3(-Math.sin(yaw), 0.0, Math.cos(yaw));
            Vec3 eye = stand.position().add(forward.scale(distance)).add(0.0, height, 0.0);
            look(cam(any.getServer()), court, eye, stand.position().add(0.0, look, 0.0));
        });
    }

    /** Kamera an Mitte + (x, y, z) im Hof; Blick auf ihn (seine Brust + {@code up}) oder, ohne ihn, auf die Mitte. */
    private static Step courtLook(Court s, double x, double y, double z, boolean atBoss, double up) {
        return Step.server(any -> {
            ServerLevel court = court(any.getServer());
            if (court == null) {
                return;
            }
            SchrolliEntity boss = boss(court, s);
            Vec3 focus = atBoss && boss != null ? boss.position().add(0.0, up, 0.0) : s.c.add(0.0, up, 0.0);
            Vec3 eye = s.c.add(x, y, z);
            look(cam(any.getServer()), court, free(court, eye, focus), focus);
        });
    }

    /** Kamera an Mitte + (dx, dy, dz) im Hof, Blick auf Mitte + (tx, ty, tz). */
    private static Step courtCam(Court s, double dx, double dy, double dz, double tx, double ty, double tz) {
        return Step.server(any -> {
            ServerLevel court = court(any.getServer());
            if (court == null || s.state != 2) {
                return;
            }
            Vec3 focus = s.c.add(tx, ty, tz);
            look(cam(any.getServer()), court, free(court, s.c.add(dx, dy, dz), focus), focus);
        });
    }

    /** Kamera am Altar in der Oberwelt: Altar + (dx, dy, dz) (Augenhöhe), Blick auf Altar + (tx, ty, tz). */
    private static Step overworldCam(Court s, double dx, double dy, double dz, double tx, double ty, double tz) {
        return Step.server(any -> {
            ServerLevel level = any.getServer().overworld();
            Vec3 a = Vec3.atBottomCenterOf(s.altar);
            ServerPlayer player = cam(any.getServer());
            s.cam[0] = a.x + dx;
            s.cam[1] = a.y + dy;
            s.cam[2] = a.z + dz;
            look(player, level, a.add(dx, dy, dz), a.add(tx, ty, tz));
        });
    }

    /** Versetzt die Kamera (Augen bei {@code eye}) mit Blick auf {@code target}; sie schwebt dort. */
    private static void look(ServerPlayer player, ServerLevel level, Vec3 eye, Vec3 target) {
        Vec3 d = target.subtract(eye);
        float yaw = (float) (-Math.atan2(d.x, d.z) * 180.0 / Math.PI);
        float pitch = (float) (-Math.atan2(d.y, Math.sqrt(d.x * d.x + d.z * d.z)) * 180.0 / Math.PI);
        if (!player.isSpectator()) {
            player.getAbilities().mayfly = true;
            player.getAbilities().flying = true;
            player.onUpdateAbilities();
        }
        player.setDeltaMovement(Vec3.ZERO);
        player.resetFallDistance();
        player.teleportTo(level, eye.x, eye.y - 1.62, eye.z, Set.of(), yaw, pitch, true);
    }

    /** Unverwundbar und schwebend (die Kamera im Überlebensmodus). */
    private static void hover(ServerPlayer player) {
        player.getAbilities().invulnerable = true;
        player.getAbilities().mayfly = true;
        player.getAbilities().flying = true;
        player.onUpdateAbilities();
        player.resetFallDistance();
        player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 2400, 4, false, false));
    }

    /** Nicht in einer Säule, nicht im Fels: so weit zum Ziel hin, bis die Kamera frei steht. */
    private static Vec3 free(ServerLevel level, Vec3 eye, Vec3 target) {
        Vec3 out = eye;
        for (int i = 0; i < 48 && !clear(level, out); i++) {
            out = out.add(target.subtract(out).normalize().scale(0.75));
        }
        return out;
    }

    private static boolean clear(ServerLevel level, Vec3 eye) {
        BlockPos p = BlockPos.containing(eye);
        for (BlockPos q : new BlockPos[] {p, p.above(), p.below(), p.north(), p.south(), p.east(), p.west()}) {
            BlockState state = level.getBlockState(q);
            if (!state.isAir() && !state.getCollisionShape(level, q).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /** Für die Beute: liegt etwas vor dem Thron? (nur zur Meldung) */
    static int lootNear(ServerLevel court, Court s) {
        return court.getEntitiesOfClass(ItemEntity.class, new AABB(s.c, s.c).inflate(48.0, 20.0, 48.0)).size();
    }
}
