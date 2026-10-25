package io.github.sirschrolli.kingdomsatwar.client.boss;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;

/**
 * Eigene Bossleisten der Drachen-Bosse: verzierter Rahmen mit Drachenkopf im Stil des Elements, Füllung in der
 * Farbe des Elements, Markierungen der Kampfphasen (60 % und 30 %), Name darüber. Erkannt werden die Leisten am
 * Übersetzungsschlüssel {@code boss.kingdomsatwar.<element>}.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID, value = Dist.CLIENT)
public final class BossBarRenderer {
    public static final int FRAME_W = 232;
    public static final int FRAME_H = 30;
    public static final int BAR_X = 25;
    public static final int BAR_Y = 15;
    public static final int BAR_W = 182;
    public static final int BAR_H = 7;
    private static final Map<DragonType, Identifier> TEXTURES = new EnumMap<>(DragonType.class);
    private static final String PREFIX = "boss.kingdomsatwar.";

    static {
        for (DragonType type : DragonType.values()) {
            TEXTURES.put(type, KingdomsAtWar.id("textures/gui/boss_bar/" + type.id() + ".png"));
        }
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
        int x = g.guiWidth() / 2 - FRAME_W / 2;
        int y = event.getY() + 2;
        Identifier tex = TEXTURES.get(type);
        // leere Leiste, Füllung, Glanz, Rahmen
        g.blit(RenderPipelines.GUI_TEXTURED, tex, x + BAR_X, y + BAR_Y, 0.0f, 40.0f, BAR_W, BAR_H, 256, 64);
        int fill = Mth.lerpDiscrete(boss.getProgress(), 0, BAR_W);
        if (fill > 0) {
            g.blit(RenderPipelines.GUI_TEXTURED, tex, x + BAR_X, y + BAR_Y, 0.0f, 32.0f, fill, BAR_H, 256, 64);
            // wandernder Glanz auf der Füllung
            long time = mc.level != null ? mc.level.getGameTime() : 0;
            int shine = (int) ((time * 3) % (BAR_W + 40)) - 20;
            if (shine > 0 && shine < fill) {
                int w = Math.min(12, fill - shine);
                g.blit(RenderPipelines.GUI_TEXTURED, tex, x + BAR_X + shine, y + BAR_Y, 200.0f, 32.0f, w, BAR_H, 256, 64,
                        ARGB.white(0.55f));
            }
        }
        g.blit(RenderPipelines.GUI_TEXTURED, tex, x, y, 0.0f, 0.0f, FRAME_W, FRAME_H, 256, 64);
        Component name = boss.getName();
        int width = mc.font.width(name);
        g.text(mc.font, name, g.guiWidth() / 2 - width / 2, y - 7, ARGB.opaque(type.color()), true);
        event.setIncrement(FRAME_H + 14);
    }

    /** Element aus dem Namen der Bossleiste ({@code boss.kingdomsatwar.fire} usw.), sonst null. */
    public static DragonType elementOf(Component name) {
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
