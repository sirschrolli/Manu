package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.entity.boss.DragonBossEntity;
import io.github.sirschrolli.kingdomsatwar.entity.boss.sky.FlightBan;
import io.github.sirschrolli.kingdomsatwar.entity.boss.sky.SkyBoltEntity;
import io.github.sirschrolli.kingdomsatwar.entity.boss.sky.SkyTheme;
import io.github.sirschrolli.kingdomsatwar.entity.boss.sky.SkyWard;
import io.github.sirschrolli.kingdomsatwar.registry.AnniBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.CreatorBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModEffects;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.StarBlocks;
import java.util.Deque;
import java.util.Set;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * Die Himmelswacht der Bosse ({@code skyward}): Pyrrhon nimmt einen Flieger ins Visier (Siegel, Ziellinie, Warnanzeige), der
 * Bannpfeil jagt heran und trifft; Glaciaras Pfeil verfolgt einen Flüchtenden (von vorn gesehen); Anni stellt den Flieger
 * auf den Boden zurück; die Siegel aller siebzehn Themen; der Flugbann im Inventar und die neue Zeile an den Schwingen.
 * Die Flugszenen laufen in Zeitlupe, damit jedes Bild den gewünschten Augenblick trifft.
 */
@net.neoforged.fml.common.EventBusSubscriber(modid = io.github.sirschrolli.kingdomsatwar.KingdomsAtWar.MODID, value = net.neoforged.api.distmarker.Dist.CLIENT)
final class SkyWardShowcase {
    private static final org.slf4j.Logger LOGGER = com.mojang.logging.LogUtils.getLogger();

    /**
     * Die freie Kamera: Sie umkreist den Spieler (dritte Person, er bleibt in der Bildmitte) und liefert je Bild die
     * Richtung, aus der sie auf ihn blickt, als Versatz – oder {@code null} für die gewöhnliche Sicht. (Eine eigene
     * Kamera-Entität ginge nicht: Dann meldet der Client seinen Ort nicht mehr, und der Pfeil jagte ins Leere.)
     */
    private static volatile java.util.function.Function<Minecraft, Vec3> rig;

    private SkyWardShowcase() {}

    @net.neoforged.bus.api.SubscribeEvent
    static void onCamera(net.neoforged.neoforge.client.event.ViewportEvent.ComputeCameraAngles event) {
        Minecraft minecraft = Minecraft.getInstance();
        java.util.function.Function<Minecraft, Vec3> current = rig;
        if (current == null || !ShowcaseRunner.enabled() || minecraft.player == null) {
            return;
        }
        // die Kamera steht beim Spieler + Versatz und blickt auf ihn
        Vec3 look = current.apply(minecraft).scale(-1.0);
        event.setYaw((float) Math.toDegrees(Math.atan2(-look.x, look.z)));
        event.setPitch((float) -Math.toDegrees(Math.atan2(look.y, look.horizontalDistance())));
    }

    /** Schaltet die freie Kamera ein ({@code distance} Blöcke vom Spieler) oder mit {@code null} wieder aus. */
    private static void rig(Deque<Step> steps, double distance, java.util.function.Function<Minecraft, Vec3> next) {
        steps.add(Step.server(any -> player(any).getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.CAMERA_DISTANCE)
                .setBaseValue(next == null ? 4.0 : distance)));
        steps.add(Step.once(minecraft -> {
            rig = next;
            if (next != null) {
                minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK);
            }
        }));
    }

    /** Ein Bild ohne die übliche Wartezeit (die Flugszenen laufen in Zeitlupe, jedes Bild zeigt den gerade gezeichneten Augenblick). */
    private static Step quick(String name) {
        return (minecraft, ticks) -> {
            if (ticks < 2) {
                minecraft.gui.toastManager().clear();
                return false;
            }
            LOGGER.info("[Showcase] Screenshot {}", name);
            net.minecraft.client.Screenshot.grab(minecraft.gameDirectory, name + ".png", minecraft.gameRenderer.mainRenderTarget(), 1, message -> { });
            return true;
        };
    }

    private static boolean wanted(String part) {
        String only = System.getProperty("kingdomsatwar.skyScenes", "");
        return only.isBlank() || java.util.Arrays.asList(only.split(",")).contains(part);
    }

    /** Waagerechte Richtung vom Spieler zu einem Punkt. */
    private static Vec3 toward(Vec3 from, Vec3 to) {
        Vec3 d = to.subtract(from).multiply(1.0, 0.0, 1.0);
        return d.lengthSqr() < 1.0e-6 ? new Vec3(0.0, 0.0, 1.0) : d.normalize();
    }

    private static ServerPlayer player(ServerLevel any) {
        return any.getServer().getPlayerList().getPlayers().getFirst();
    }

    private static int ground() {
        return Showcase.ground;
    }

    /** Der Spieler: verwundbar wie im Überlebensmodus (sonst beachtet ihn die Himmelswacht nicht), aber ohne Schaden. */
    private static void survivor(ServerPlayer player, ItemStack wings) {
        player.getAbilities().invulnerable = true;
        player.getAbilities().mayfly = false;
        player.getAbilities().flying = false;
        player.onUpdateAbilities();
        player.setItemSlot(EquipmentSlot.CHEST, wings);
        player.removeAllEffects();
        FlightBan.lift(player);
        player.clearFire();
        player.setTicksFrozen(0);
    }

    private static void startGlide(Deque<Step> steps, Vec3 velocity) {
        steps.add(Step.server(any -> {
            ServerPlayer player = player(any);
            player.startFallFlying();
            player.setDeltaMovement(velocity);
            player.needsSync = true;
        }));
        steps.add(Step.once(minecraft -> {
            if (minecraft.player != null) {
                minecraft.player.setDeltaMovement(velocity);
            }
        }));
    }

    /** Ein Drachenboss ohne eigenen Willen in Kampfhaltung. */
    private static DragonBossEntity dragon(ServerLevel level, DragonType type, double x, double z, float yaw) {
        DragonBossEntity boss = ModEntities.bossType(type).create(level, EntitySpawnReason.COMMAND);
        boss.snapTo(x, ground(), z, yaw, 0.0f);
        boss.yBodyRot = yaw;
        boss.yHeadRot = yaw;
        boss.setNoAi(true);
        boss.setPersistenceRequired();
        level.addFreshEntity(boss);
        boss.devPose(DragonBossEntity.MoveMode.GROUND);
        return boss;
    }

    private static void clearStage(ServerLevel level, int ox, int oz, int half) {
        int g = ground();
        Showcase.fill(level, new BlockPos(ox - half, g - 2, oz - half), new BlockPos(ox + half, g - 1, oz + half), Blocks.GRASS_BLOCK.defaultBlockState());
        Showcase.fill(level, new BlockPos(ox - half, g, oz - half), new BlockPos(ox + half, g + 6, oz + half), Blocks.AIR.defaultBlockState());
    }

    private static boolean homing(SkyBoltEntity[] bolt) {
        return bolt[0] != null && (bolt[0].isRemoved() || bolt[0].state() != SkyBoltEntity.CHARGE);
    }

    static void skyward(Deque<Step> steps) {
        for (String command : new String[] {"gamerule advance_time false", "time set 6000", "weather clear", "gamerule spawn_mobs false",
                "kill @e[type=!minecraft:player]", "clear", "gamemode survival"}) {
            steps.add(Step.command(command));
        }
        steps.add(Step.server(any -> survivor(player(any), ItemStack.EMPTY)));
        for (String command : new String[] {"team add vorfuehrung", "team modify vorfuehrung nametagVisibility never", "team join vorfuehrung @s"}) {
            steps.add(Step.command(command));
        }
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> {
            if (minecraft.gui.hud.isHidden()) {
                minecraft.gui.hud.toggle();
            }
            ShowcaseRunner.hideHand = true;
        }));
        if (wanted("ich")) {
            firstPerson(steps);
        }
        if (wanted("weit")) {
            wide(steps);
        }
        if (wanted("jagd")) {
            chase(steps);
        }
        if (wanted("anni")) {
            anni(steps);
        }
        if (wanted("siegel")) {
            gallery(steps);
        }
        if (wanted("inventar")) {
            inventory(steps);
        }
        steps.add(Step.command("tick rate 20"));
    }

    /** Bereitet eine Flugszene vor: Bühne, Boss, Spieler mit Schwingen am Boden daneben. */
    private static void stage(Deque<Step> steps, int ox, int oz, Mob[] boss, java.util.function.Function<ServerLevel, Mob> spawn, ItemStack wings,
            boolean hud) {
        steps.add(Step.command("tick rate 20"));
        rig(steps, 4.0, null);
        steps.add(Step.once(minecraft -> {
            if (minecraft.gui.hud.isHidden() == hud) {
                minecraft.gui.hud.toggle();
            }
        }));
        steps.add(Showcase.tp(ox + 0.5, 0.0, oz - 30.0, 0.0f, 0.0f));
        steps.add(Step.waitForChunks(300));
        steps.add(Step.server(any -> {
            ServerLevel level = any.getServer().overworld();
            clearStage(level, ox, oz, 26);
            boss[0] = spawn.apply(level);
            survivor(player(any), wings.copy());
        }));
        steps.add(Step.waitTicks(40));
        steps.add(Step.once(minecraft -> minecraft.gui.hud.setOverlayMessage(net.minecraft.network.chat.Component.empty(), false)));
    }

    /** Hebt den Spieler in die Luft und lässt ihn losgleiten (in Zeitlupe). */
    private static void takeOff(Deque<Step> steps, double x, double dy, double z, float yaw, float pitch, Vec3 velocity) {
        steps.add(Step.command("tick rate 5"));
        steps.add(Step.waitTicks(2));
        steps.add(Showcase.tp(x, dy, z, yaw, pitch));
        steps.add(Step.waitTicks(4));
        startGlide(steps, velocity);
        steps.add(Step.waitTicks(3));
        steps.add(Step.server(any -> {
            ServerPlayer player = player(any);
            LOGGER.info("[Showcase] Gleitflug (Server): gleitet={} Boden={} y={} Gleitmerkmal={}", player.isFallFlying(), player.onGround(),
                    String.format(java.util.Locale.ROOT, "%.1f", player.getY()), player.getAttributeValue(net.neoforged.neoforge.common.NeoForgeMod.GLIDING_FLIGHT));
        }));
    }

    private static void endScene(Deque<Step> steps, Mob[] boss) {
        steps.add(Step.command("tick rate 20"));
        rig(steps, 4.0, null);
        steps.add(Step.waitTicks(30));
        steps.add(Step.server(any -> {
            boss[0].discard();
            FlightBan.lift(player(any));
        }));
        steps.add(Step.command("kill @e[type=!minecraft:player]"));
    }

    // ------------------------------------------------------------------ Pyrrhon, mit eigenen Augen: Visier, Pfeil, Treffer

    private static void firstPerson(Deque<Step> steps) {
        int ox = 3000;
        int oz = 0;
        Mob[] boss = new Mob[1];
        SkyBoltEntity[] bolt = new SkyBoltEntity[1];
        stage(steps, ox, oz, boss, level -> dragon(level, DragonType.FIRE, ox + 0.5, oz + 0.5, 180.0f), new ItemStack(Items.ELYTRA), true);
        steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.FIRST_PERSON)));
        takeOff(steps, ox + 0.5, 34.0, oz - 74.0, 0.0f, 12.0f, new Vec3(0.0, -0.05, 1.1));
        steps.add(Step.server(any -> bolt[0] = SkyWard.mark(any.getServer().overworld(), boss[0], player(any), SkyTheme.FIRE, 30)));
        steps.add(Step.waitTicks(13));
        steps.add(quick("himmelswacht_visier"));
        steps.add(Step.waitTicks(6));
        steps.add(quick("himmelswacht_visier_spaet"));
        steps.add(Step.until(() -> homing(bolt), 60));
        steps.add(quick("himmelswacht_pfeil"));
        steps.add(quick("himmelswacht_pfeil_nah"));
        steps.add(Step.until(() -> bolt[0] != null && bolt[0].isRemoved(), 60));
        steps.add(quick("himmelswacht_treffer"));
        steps.add(Step.waitTicks(5));
        steps.add(quick("himmelswacht_flugbann"));
        endScene(steps, boss);
    }

    // ------------------------------------------------------------------ Pyrrhon, von außen: Siegel, Ziellinie, Pfeil

    private static void wide(Deque<Step> steps) {
        int ox = 3200;
        int oz = 0;
        Mob[] boss = new Mob[1];
        SkyBoltEntity[] bolt = new SkyBoltEntity[1];
        stage(steps, ox, oz, boss, level -> dragon(level, DragonType.FIRE, ox + 0.5, oz + 0.5, 180.0f), new ItemStack(Items.ELYTRA), false);
        takeOff(steps, ox + 0.5, 30.0, oz - 62.0, 0.0f, 10.0f, new Vec3(0.0, -0.05, 1.1));
        // die Kamera fliegt schräg hinter dem Flieger mit und blickt an ihm vorbei auf den Boss
        rig(steps, 8.5, minecraft -> {
            Vec3 forward = toward(minecraft.player.position(), boss[0].position());
            Vec3 right = new Vec3(-forward.z, 0.0, forward.x);
            return forward.scale(-1.0).add(right.scale(0.55)).add(0.0, 0.34, 0.0);
        });
        steps.add(Step.server(any -> bolt[0] = SkyWard.mark(any.getServer().overworld(), boss[0], player(any), SkyTheme.FIRE, 30)));
        steps.add(Step.waitTicks(12));
        steps.add(quick("himmelswacht_weit_visier"));
        steps.add(Step.waitTicks(8));
        steps.add(quick("himmelswacht_weit_visier_spaet"));
        steps.add(Step.until(() -> homing(bolt), 60));
        for (int i = 1; i <= 4; i++) {
            steps.add(quick("himmelswacht_weit_pfeil_" + i));
        }
        steps.add(Step.until(() -> bolt[0] != null && bolt[0].isRemoved(), 60));
        steps.add(quick("himmelswacht_weit_treffer"));
        endScene(steps, boss);
    }

    // ------------------------------------------------------------------ Glaciara: die Jagd, von der Seite gesehen

    private static void chase(Deque<Step> steps) {
        int ox = 3400;
        int oz = 0;
        Mob[] boss = new Mob[1];
        SkyBoltEntity[] bolt = new SkyBoltEntity[1];
        stage(steps, ox, oz, boss, level -> dragon(level, DragonType.ICE, ox + 0.5, oz + 0.5, 0.0f), new ItemStack(FaeBlocks.FAE_ELYTRA.get()), false);
        takeOff(steps, ox + 0.5, 30.0, oz + 6.0, 0.0f, -3.0f, new Vec3(0.0, 0.02, 0.8));
        // die Kamera fliegt seitlich voraus und blickt zurück: vorn der Flieger, hinter ihm der Pfeil, dahinter der Boss
        rig(steps, 11.0, minecraft -> {
            Vec3 away = toward(boss[0].position(), minecraft.player.position());
            Vec3 right = new Vec3(-away.z, 0.0, away.x);
            return away.scale(0.55).add(right.scale(1.0)).add(0.0, 0.12, 0.0);
        });
        steps.add(Step.server(any -> bolt[0] = SkyWard.mark(any.getServer().overworld(), boss[0], player(any), SkyTheme.ICE, 24)));
        steps.add(Step.waitTicks(14));
        steps.add(quick("himmelswacht_jagd_visier"));
        steps.add(Step.until(() -> homing(bolt), 60));
        steps.add(Step.server(any -> LOGGER.info("[Showcase] Jagd beginnt: Abstand {}", String.format(java.util.Locale.ROOT, "%.1f",
                bolt[0].distanceTo(player(any))))));
        for (int i = 1; i <= 14; i++) {
            steps.add(Step.waitTicks(1));
            steps.add(quick("himmelswacht_jagd_" + i));
            int shot = i;
            steps.add(Step.server(any -> LOGGER.info("[Showcase] Jagd {}: Abstand {} Zustand {}", shot, String.format(java.util.Locale.ROOT, "%.1f",
                    bolt[0].distanceTo(player(any))), bolt[0].isRemoved() ? "weg" : String.valueOf(bolt[0].state()))));
        }
        steps.add(Step.until(() -> bolt[0] != null && bolt[0].isRemoved(), 80));
        steps.add(quick("himmelswacht_jagd_treffer"));
        steps.add(Step.waitTicks(4));
        steps.add(quick("himmelswacht_jagd_sturz"));
        endScene(steps, boss);
    }

    // ------------------------------------------------------------------ Anni: „Fliegen ist auf diesem Server nicht erlaubt“

    private static void anni(Deque<Step> steps) {
        int ox = 3600;
        int oz = 0;
        Mob[] boss = new Mob[1];
        SkyBoltEntity[] bolt = new SkyBoltEntity[1];
        stage(steps, ox, oz, boss, level -> {
            Mob anni = ModEntities.ANNI_TROLL.get().create(level, EntitySpawnReason.COMMAND);
            anni.snapTo(ox + 0.5, ground(), oz + 0.5, 180.0f, 0.0f);
            anni.yBodyRot = 180.0f;
            anni.yHeadRot = 180.0f;
            anni.setNoAi(true);
            anni.setPersistenceRequired();
            level.addFreshEntity(anni);
            return anni;
        }, new ItemStack(AnniBlocks.GLITCH_WINGS.get()), false);
        takeOff(steps, ox + 0.5, 20.0, oz - 44.0, 0.0f, 10.0f, new Vec3(0.0, -0.05, 0.9));
        rig(steps, 9.0, minecraft -> {
            Vec3 forward = toward(minecraft.player.position(), boss[0].position());
            Vec3 right = new Vec3(-forward.z, 0.0, forward.x);
            return forward.scale(-1.0).add(right.scale(0.5)).add(0.0, 0.3, 0.0);
        });
        steps.add(Step.waitTicks(10));
        steps.add(Step.server(any -> bolt[0] = SkyWard.mark(any.getServer().overworld(), boss[0], player(any), SkyTheme.GLITCH, 20)));
        steps.add(Step.waitTicks(11));
        steps.add(quick("himmelswacht_anni_visier"));
        steps.add(Step.until(() -> bolt[0] != null && bolt[0].isRemoved(), 80));
        rig(steps, 4.0, null);
        steps.add(Step.once(minecraft -> {
            minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK);
            if (minecraft.gui.hud.isHidden()) {
                minecraft.gui.hud.toggle();
            }
        }));
        steps.add(Step.waitTicks(3));
        steps.add(quick("himmelswacht_anni_rueckruck"));
        endScene(steps, boss);
    }

    // ------------------------------------------------------------------ die Siegel aller Themen

    private static void gallery(Deque<Step> steps) {
        int ox = 3900;
        int oz = 0;
        steps.add(Step.command("tick rate 20"));
        rig(steps, 4.0, null);
        steps.add(Step.once(minecraft -> {
            if (minecraft.gui.hud.isHidden()) {
                minecraft.gui.hud.toggle();
            }
        }));
        steps.add(Step.command("gamemode creative"));
        steps.add(Showcase.tp(ox + 0.5, 12.0, oz - 15.0, 0.0f, 36.0f));
        steps.add(Step.waitForChunks(300));
        steps.add(Step.server(any -> {
            ServerLevel level = any.getServer().overworld();
            int g = ground();
            // eine dunkle Fläche, auf der die Siegel leuchten
            Showcase.fill(level, new BlockPos(ox - 26, g - 1, oz - 16), new BlockPos(ox + 26, g - 1, oz + 22), Blocks.POLISHED_BLACKSTONE.defaultBlockState());
            Showcase.fill(level, new BlockPos(ox - 26, g, oz - 16), new BlockPos(ox + 26, g + 8, oz + 22), Blocks.AIR.defaultBlockState());
            ServerPlayer player = player(any);
            player.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
            player.getAbilities().mayfly = true;
            player.getAbilities().flying = true;
            player.onUpdateAbilities();
            SkyTheme[] themes = SkyTheme.values();
            int columns = 6;
            for (int i = 0; i < themes.length; i++) {
                int row = i / columns;
                int inRow = Math.min(columns, themes.length - row * columns);
                double x = ox + 0.5 + (i % columns - (inRow - 1) / 2.0) * 6.4;
                double z = oz + 0.5 + row * 6.6;
                ArmorStand stand = new ArmorStand(level, x, g - 1.6, z);
                stand.setInvisible(true);
                stand.setNoGravity(true);
                level.addFreshEntity(stand);
                SkyBoltEntity.display(level, stand, themes[i]);
            }
        }));
        steps.add(Step.once(minecraft -> {
            minecraft.options.setCameraType(CameraType.FIRST_PERSON);
            minecraft.gui.hud.setOverlayMessage(net.minecraft.network.chat.Component.empty(), false);
        }));
        steps.add(Step.waitTicks(40));
        steps.add(Showcase.tp(ox + 0.5, 12.0, oz - 15.0, 0.0f, 36.0f));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screenshot("himmelswacht_siegel"));
        steps.add(Showcase.tp(ox - 13.0, 8.5, oz - 9.0, -48.0f, 30.0f));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screenshot("himmelswacht_siegel_nah"));
        steps.add(Step.command("kill @e[type=!minecraft:player]"));
    }

    // ------------------------------------------------------------------ Flugbann im Inventar, die Zeile an den Schwingen

    /** Mauszeiger über ein Feld des Inventars (Koordinaten im Fenster, 176 x 166). */
    private static void hover(Minecraft minecraft, int guiX, int guiY) {
        int left = (minecraft.getWindow().getGuiScaledWidth() - 176) / 2;
        int top = (minecraft.getWindow().getGuiScaledHeight() - 166) / 2;
        double scale = minecraft.getWindow().getGuiScale();
        try {
            for (String name : new String[] {"xpos", "ypos"}) {
                java.lang.reflect.Field field = minecraft.mouseHandler.getClass().getDeclaredField(name);
                field.setAccessible(true);
                field.setDouble(minecraft.mouseHandler, name.equals("xpos") ? (left + guiX) * scale : (top + guiY) * scale);
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void inventory(Deque<Step> steps) {
        steps.add(Step.command("gamemode survival"));
        steps.add(Showcase.tp(3900.5, 0.0, -30.0, 0.0f, 10.0f));
        steps.add(Step.server(any -> {
            ServerPlayer player = player(any);
            survivor(player, ItemStack.EMPTY);
            player.getInventory().clearContent();
            player.getInventory().setItem(0, new ItemStack(Items.ELYTRA));
            player.getInventory().setItem(1, new ItemStack(FaeBlocks.FAE_ELYTRA.get()));
            player.getInventory().setItem(2, new ItemStack(StarBlocks.STAR_WINGS.get()));
            player.getInventory().setItem(3, new ItemStack(AnniBlocks.GLITCH_WINGS.get()));
            player.getInventory().setItem(4, new ItemStack(CreatorBlocks.CREATOR_WINGS.get()));
            player.addEffect(new MobEffectInstance(ModEffects.FLIGHT_BAN, 198, 0, false, false, true));
        }));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screen(minecraft -> new InventoryScreen(minecraft.player)));
        steps.add(Step.waitTicks(6));
        Object[][] hovers = {{1, "feen"}, {2, "sternen"}, {3, "fehler"}, {4, "schoepfer"}};
        for (Object[] h : hovers) {
            steps.add(Step.server(any -> {
                // der Bann soll auf jedem Bild noch laufen
                FlightBan.lift(player(any));
                player(any).addEffect(new MobEffectInstance(ModEffects.FLIGHT_BAN, 198, 0, false, false, true));
            }));
            steps.add(Step.once(minecraft -> hover(minecraft, 16 + 18 * (int) h[0], 150)));
            steps.add(Step.waitTicks(4));
            steps.add(Step.screenshot("himmelswacht_schwingen_" + h[1]));
        }
        steps.add(Step.once(minecraft -> minecraft.gui.setScreen(null)));
        steps.add(Step.server(any -> FlightBan.lift(player(any))));
        steps.add(Step.command("clear"));
        steps.add(Step.waitTicks(6));
    }
}
