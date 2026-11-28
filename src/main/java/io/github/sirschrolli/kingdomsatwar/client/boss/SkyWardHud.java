package io.github.sirschrolli.kingdomsatwar.client.boss;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.entity.boss.sky.SkyBoltEntity;
import io.github.sirschrolli.kingdomsatwar.entity.boss.sky.SkyTheme;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import org.jspecify.annotations.Nullable;

/**
 * Die Warnanzeige der Himmelswacht. Nimmt ein Boss den Spieler ins Visier, ziehen sich vier Winkel um das Fadenkreuz
 * zusammen (so lange lädt das Siegel), darunter stehen das Zeichen des Flugbanns und „IM VISIER – Jetzt landen!“, und
 * vom Bildrand her glimmt die Farbe des Bosses. Jagt der Bannpfeil heran, zeigt ein Pfeil um das Fadenkreuz, aus welcher
 * Richtung er kommt, darunter steht die Entfernung, und ein Ton schlägt umso schneller an, je näher er ist.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID, value = Dist.CLIENT)
public final class SkyWardHud {
    public static final Identifier LAYER = KingdomsAtWar.id("sky_ward");
    private static final Identifier ICON = KingdomsAtWar.id("textures/mob_effect/flight_ban.png");
    private static final Identifier ARROW = KingdomsAtWar.id("textures/gui/sky_ward/arrow.png");

    private static @Nullable SkyBoltEntity threat;
    private static int pingTimer;
    private static float shown;
    private static float lastShown;
    private static SkyTheme theme = SkyTheme.FIRE;
    private static boolean homing;

    private SkyWardHud() {}

    @SubscribeEvent
    static void registerLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.CROSSHAIR, LAYER, SkyWardHud::render);
    }

    /** Der Bannpfeil, der es gerade auf den Spieler abgesehen hat (für Vorführung und Tests). */
    public static @Nullable SkyBoltEntity threat() {
        return threat;
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        lastShown = shown;
        if (player == null || minecraft.level == null) {
            threat = null;
            shown = 0.0f;
            return;
        }
        if (minecraft.isPaused()) {
            return;
        }
        SkyBoltEntity nearest = null;
        double best = Double.MAX_VALUE;
        for (Entity entity : minecraft.level.entitiesForRendering()) {
            if (entity instanceof SkyBoltEntity bolt && bolt.state() != SkyBoltEntity.FREE && bolt.targetEntity() == player) {
                // ein jagender Pfeil geht dem ladenden Siegel vor
                double d = bolt.distanceToSqr(player) - (bolt.state() == SkyBoltEntity.HOMING ? 1.0e8 : 0.0);
                if (d < best) {
                    best = d;
                    nearest = bolt;
                }
            }
        }
        threat = nearest;
        if (nearest == null) {
            shown = Math.max(0.0f, shown - 0.2f);
            pingTimer = 0;
            return;
        }
        theme = nearest.theme();
        homing = nearest.state() == SkyBoltEntity.HOMING;
        shown = Math.min(1.0f, shown + 0.25f);
        if (homing) {
            float distance = nearest.distanceTo(player);
            if (--pingTimer <= 0) {
                float near = 1.0f - Mth.clamp(distance / 48.0f, 0.0f, 1.0f);
                pingTimer = Mth.clamp((int) (distance / 3.0f), 3, 16);
                minecraft.getSoundManager().play(SimpleSoundInstance.forUI(ModSounds.SKY_WARD_PING.get(), 1.0f + near * 0.6f, 0.35f + near * 0.25f));
            }
        } else {
            pingTimer = 0;
        }
    }

    static void render(GuiGraphicsExtractor g, DeltaTracker delta) {
        Minecraft minecraft = Minecraft.getInstance();
        float partial = delta.getGameTimeDeltaPartialTick(false);
        float alpha = Mth.lerp(partial, lastShown, shown);
        LocalPlayer player = minecraft.player;
        if (alpha <= 0.01f || player == null || minecraft.gui.hud.isHidden()) {
            return;
        }
        int width = g.guiWidth();
        int height = g.guiHeight();
        int cx = width / 2;
        int cy = height / 2;
        float time = player.tickCount + partial;
        SkyBoltEntity bolt = threat;
        int rgb = theme.color;
        // wie weit das Siegel geladen ist (im Flug: voll)
        float charge = bolt != null && !homing ? Mth.clamp((bolt.clientStateTicks() - 1 + partial) / bolt.chargeTicks(), 0.0f, 1.0f) : 1.0f;
        float blink = homing ? 0.6f + 0.4f * Mth.sin(time * 1.3f) : 0.8f + 0.2f * Mth.sin(time * 0.6f);
        // Glimmen vom Bildrand
        int edge = ARGB.color((int) (alpha * (homing ? 70 + 40 * blink : 30 + 50 * charge)), rgb);
        int clear = ARGB.color(0, rgb);
        int band = height / 5;
        g.fillGradient(0, 0, width, band, edge, clear);
        g.fillGradient(0, height - band, width, height, clear, edge);
        // vier Winkel ziehen sich um das Fadenkreuz zusammen
        int reach = Math.round(Mth.lerp(charge * charge, 40.0f, 13.0f));
        int bright = ARGB.color((int) (alpha * 255 * blink), rgb);
        int dark = ARGB.color((int) (alpha * 170 * blink), 0x10121C);
        for (int sx = -1; sx <= 1; sx += 2) {
            for (int sy = -1; sy <= 1; sy += 2) {
                corner(g, cx + sx * reach, cy + sy * reach, -sx, -sy, bright, dark);
            }
        }
        Font font = minecraft.font;
        if (!homing) {
            // unter den Winkeln: das Zeichen des Flugbanns und die Warnung (die Bildmitte bleibt frei für den Boss)
            Component locked = Component.translatable("hud.kingdomsatwar.sky_ward.locked");
            Component land = Component.translatable("hud.kingdomsatwar.sky_ward.land");
            int textWidth = Math.max(font.width(locked), font.width(land));
            int left = cx - (22 + textWidth) / 2;
            int top = cy + reach + 9;
            int icon = ARGB.color((int) (alpha * 255 * (0.75f + 0.25f * blink)), 0xFFFFFF);
            g.blit(RenderPipelines.GUI_TEXTURED, ICON, left, top, 0.0f, 0.0f, 18, 18, 18, 18, icon);
            g.text(font, locked, left + 22, top, ARGB.color((int) (alpha * 255), rgb), true);
            g.text(font, land, left + 22, top + 10, ARGB.color((int) (alpha * 255), 0xFFFFFF), true);
            return;
        }
        if (bolt == null) {
            return;
        }
        // Richtung des Pfeils, von der Kamera aus gesehen
        Camera camera = minecraft.gameRenderer.mainCamera();
        Vec3 rel = bolt.getPosition(partial).add(0.0, 0.3, 0.0).subtract(camera.position());
        float yaw = camera.yRot() * Mth.DEG_TO_RAD;
        float pitch = camera.xRot() * Mth.DEG_TO_RAD;
        // in den Blick der Kamera gedreht: x nach rechts, y nach oben, z nach vorn
        double x1 = -(rel.x * Mth.cos(yaw) + rel.z * Mth.sin(yaw));
        double z1 = -rel.x * Mth.sin(yaw) + rel.z * Mth.cos(yaw);
        double y2 = rel.y * Mth.cos(pitch) + z1 * Mth.sin(pitch);
        double z2 = -rel.y * Mth.sin(pitch) + z1 * Mth.cos(pitch);
        // voraus: dorthin, wo man ihn sieht (oben = über dem Fadenkreuz); hinter einem: nach unten, links oder rechts herum
        float angle = (float) (z2 >= 0.0 ? Mth.atan2(x1, y2) : Mth.atan2(x1, -Math.hypot(y2, z2)));
        if (z2 > 0.0 && x1 * x1 + y2 * y2 < z2 * z2 * 0.004) {
            // genau voraus: der Pfeil zeigt nach oben
            angle = 0.0f;
        }
        float ring = 34.0f;
        g.pose().pushMatrix();
        g.pose().translate(cx + Mth.sin(angle) * ring, cy - Mth.cos(angle) * ring);
        g.pose().rotate(angle);
        g.pose().scale(1.5f, 1.5f);
        g.blit(RenderPipelines.GUI_TEXTURED, ARROW, -8, -8, 0.0f, 0.0f, 16, 16, 16, 16, ARGB.color((int) (alpha * 255), rgb));
        g.pose().popMatrix();
        Component incoming = Component.translatable("hud.kingdomsatwar.sky_ward.incoming");
        g.text(font, incoming, cx - font.width(incoming) / 2, cy + 54, ARGB.color((int) (alpha * 255 * blink), rgb), true);
        Component distance = Component.translatable("hud.kingdomsatwar.sky_ward.distance", Math.round(rel.length()));
        g.text(font, distance, cx - font.width(distance) / 2, cy + 65, ARGB.color((int) (alpha * 255), 0xFFFFFF), true);
    }

    /** Ein Winkel aus zwei Balken (7 lang, 2 dick) mit dunklem Saum; die Schenkel zeigen in Richtung (dx, dy). */
    private static void corner(GuiGraphicsExtractor g, int x, int y, int dx, int dy, int color, int shadow) {
        int x0 = Math.min(x, x + dx * 7);
        int x1 = Math.max(x, x + dx * 7);
        int y0 = Math.min(y, y + dy * 7);
        int y1 = Math.max(y, y + dy * 7);
        // waagerechter Schenkel
        int hy0 = dy > 0 ? y : y - 2;
        g.fill(x0 - 1, hy0 - 1, x1 + 1, hy0 + 3, shadow);
        // senkrechter Schenkel
        int vx0 = dx > 0 ? x : x - 2;
        g.fill(vx0 - 1, y0 - 1, vx0 + 3, y1 + 1, shadow);
        g.fill(x0, hy0, x1, hy0 + 2, color);
        g.fill(vx0, y0, vx0 + 2, y1, color);
    }
}
