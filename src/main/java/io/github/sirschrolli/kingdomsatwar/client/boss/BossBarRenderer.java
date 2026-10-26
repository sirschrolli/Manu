package io.github.sirschrolli.kingdomsatwar.client.boss;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import org.jspecify.annotations.Nullable;

/**
 * Eigene Bossleisten der Drachen-Bosse: je Element ein verzierter Rahmen (Flammen, Eiskristalle, Blitze, Ranken,
 * Felsen, Wellen), links ein Medaillon mit dem Bildnis des Bosses, darüber zwei Edelsteine für die Kampfphasen
 * (60 % und 30 % – sie zerspringen, sobald die Phase erreicht ist). Die Füllung strömt in der Farbe des Elements,
 * erlittener Schaden bleibt als helle Spur kurz stehen und schmilzt dann nach. In der letzten Phase glüht der ganze
 * Rahmen im Takt, das Bildnis färbt sich vor Wut rot. Der Name steht auf einem dunklen Band darüber.
 *
 * <p>Erkannt werden die Leisten am Übersetzungsschlüssel {@code boss.kingdomsatwar.<element>}; der Aufbau der Texturen
 * steht in tools/bossbar_textures.py.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID, value = Dist.CLIENT)
public final class BossBarRenderer {
    public static final int FRAME_W = 240;
    public static final int FRAME_H = 44;
    public static final int BAR_X = 48;
    public static final int BAR_Y = 18;
    public static final int BAR_W = 182;
    public static final int BAR_H = 9;
    private static final int EMB_X = 21;
    private static final int EMB_Y = 22;
    private static final int TEX = 256;
    /** Abstand vom oberen Rand der Leiste bis zum Rahmen (Platz für den Namen). */
    private static final int NAME_SPACE = 11;
    private static final float[] GEMS = {0.6f, 0.3f};
    private static final Map<DragonType, Identifier> TEXTURES = new EnumMap<>(DragonType.class);
    private static final Map<UUID, Trail> TRAILS = new HashMap<>();
    private static final String PREFIX = "boss.kingdomsatwar.";

    static {
        for (DragonType type : DragonType.values()) {
            TEXTURES.put(type, KingdomsAtWar.id("textures/gui/boss_bar/" + type.id() + ".png"));
        }
    }

    /** Spur des erlittenen Schadens: bleibt nach einem Treffer kurz stehen und schmilzt dann zur echten Füllung. */
    private static final class Trail {
        float value;
        float last;
        long holdUntil;
        long time;
    }

    private BossBarRenderer() {}

    @SubscribeEvent
    static void onBossBar(CustomizeGuiOverlayEvent.BossEventProgress event) {
        LerpingBossEvent boss = event.getBossEvent();
        DragonType type = elementOf(boss.getName());
        if (type == null) {
            return;
        }
        event.setCanceled(true);
        GuiGraphicsExtractor g = event.getGuiGraphics();
        Minecraft mc = Minecraft.getInstance();
        Identifier tex = TEXTURES.get(type);
        long now = Util.getMillis();
        float progress = Mth.clamp(boss.getProgress(), 0.0f, 1.0f);
        float trail = trail(boss.getId(), progress, now);
        int x = g.guiWidth() / 2 - BAR_W / 2 - BAR_X;
        int top = event.getY();
        int y = top + NAME_SPACE;
        int bx = x + BAR_X;
        int by = y + BAR_Y;

        // Name auf einem dunklen Band
        Component name = boss.getName();
        int nameW = mc.font.width(name);
        int nx = g.guiWidth() / 2 - nameW / 2;
        g.fill(nx - 9, top - 1, nx + nameW + 9, top + 9, ARGB.color(90, 0, 0, 0));
        g.fill(nx - 6, top - 2, nx + nameW + 6, top + 10, ARGB.color(110, 0, 0, 0));
        g.text(mc.font, name, nx, top, ARGB.opaque(type.color()), true);

        // Leiste: leer, Schadensspur, strömende Füllung, Glanz
        g.blit(RenderPipelines.GUI_TEXTURED, tex, bx, by, 0.0f, 58.0f, BAR_W, BAR_H, TEX, TEX);
        int fill = Mth.lerpDiscrete(progress, 0, BAR_W);
        int trailFill = Mth.lerpDiscrete(trail, 0, BAR_W);
        if (trailFill > fill) {
            g.blit(RenderPipelines.GUI_TEXTURED, tex, bx + fill, by, fill, 68.0f, trailFill - fill, BAR_H, TEX, TEX);
        }
        if (fill > 0) {
            int offset = (int) ((now / 45L) % TEX);
            int first = Math.min(fill, TEX - offset);
            g.blit(RenderPipelines.GUI_TEXTURED, tex, bx, by, offset, 48.0f, first, BAR_H, TEX, TEX);
            if (first < fill) {
                g.blit(RenderPipelines.GUI_TEXTURED, tex, bx + first, by, 0.0f, 48.0f, fill - first, BAR_H, TEX, TEX);
            }
            int shine = (int) ((now / 12L) % (BAR_W + 60)) - 30;
            if (shine > -16 && shine < fill) {
                int from = Math.max(0, shine);
                int to = Math.min(fill, shine + 16);
                if (to > from) {
                    g.blit(RenderPipelines.GUI_TEXTURED, tex, bx + from, by, 184.0f + (from - shine), 58.0f, to - from, BAR_H,
                            TEX, TEX, ARGB.white(0.6f));
                }
            }
        }

        // Aura der letzten Phase (pulsierend), in der mittleren Phase nur schwach
        float pulse = 0.5f + 0.5f * Mth.sin(now / 1000.0f * 5.5f);
        float aura = progress < GEMS[1] ? 0.55f + 0.45f * pulse : progress < GEMS[0] ? 0.12f + 0.1f * pulse : 0.0f;
        if (aura > 0.02f) {
            g.blit(RenderPipelines.GUI_TEXTURED, tex, x, y, 0.0f, 80.0f, FRAME_W, FRAME_H, TEX, TEX, ARGB.white(aura));
        }

        // Rahmen, Bildnis (doppelte Auflösung), Edelsteine
        g.blit(RenderPipelines.GUI_TEXTURED, tex, x, y, 0.0f, 0.0f, FRAME_W, FRAME_H, TEX, TEX);
        int tint = -1;
        if (progress < GEMS[1]) {
            int gb = Math.round(255 - 110 * pulse);
            tint = ARGB.color(255, 255, gb, gb);
        }
        g.blit(RenderPipelines.GUI_TEXTURED, tex, x + EMB_X - 16, y + EMB_Y - 16, 0.0f, 128.0f, 32, 32, 64, 64, TEX, TEX, tint);
        for (float f : GEMS) {
            int gx = bx + Math.round(BAR_W * f) - 4;
            int gy = by - 3 - 7;
            float u = progress > f ? 204.0f : 214.0f;
            g.blit(RenderPipelines.GUI_TEXTURED, tex, gx, gy, u, 58.0f, 9, 9, TEX, TEX);
        }
        event.setIncrement(NAME_SPACE + FRAME_H + 4);
    }

    /** Spur des Schadens fortschreiben und ihren aktuellen Stand liefern. */
    private static float trail(UUID id, float progress, long now) {
        if (TRAILS.size() > 16) {
            TRAILS.clear();
        }
        Trail t = TRAILS.computeIfAbsent(id, k -> {
            Trail n = new Trail();
            n.value = progress;
            n.last = progress;
            n.time = now;
            return n;
        });
        float dt = Math.min(0.25f, (now - t.time) / 1000.0f);
        t.time = now;
        if (progress < t.last - 0.0005f && now > t.holdUntil + 300L) {
            t.holdUntil = now + 550L;               // frischer Treffer: Spur kurz stehen lassen (nicht endlos verlängern)
        }
        t.last = progress;
        if (progress >= t.value) {
            t.value = progress;                     // Heilung: sofort mitgehen
        } else if (now > t.holdUntil) {
            t.value = Math.max(progress, t.value - dt * (0.35f + (t.value - progress) * 2.0f));
        }
        return t.value;
    }

    /** Element aus dem Namen der Bossleiste ({@code boss.kingdomsatwar.fire} usw.), sonst null. */
    public static @Nullable DragonType elementOf(Component name) {
        if (name.getContents() instanceof TranslatableContents contents && contents.getKey().startsWith(PREFIX)) {
            String id = contents.getKey().substring(PREFIX.length());
            for (DragonType type : DragonType.values()) {
                if (type.id().equals(id)) {
                    return type;
                }
            }
        }
        return null;
    }
}
