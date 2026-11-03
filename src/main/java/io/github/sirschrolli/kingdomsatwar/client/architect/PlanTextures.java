package io.github.sirschrolli.kingdomsatwar.client.architect;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.architect.PlanScan;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

/**
 * Die kleinen Bilder der Baupläne (Grundriss und Ansicht) als Texturen im Grafikspeicher – einmal angelegt und dann
 * wiederverwendet (die ältesten werden freigegeben, wenn es zu viele werden).
 */
public final class PlanTextures {
    private static final int MAX = 48;
    private static final Map<String, Entry> TEXTURES = new LinkedHashMap<>(64, 0.75f, true);
    private static int counter;

    private record Entry(Identifier id, int width, int height) {}

    private PlanTextures() {}

    /** Die Textur zu einem Bild ({@code key} muss das Bild eindeutig benennen). */
    private static Entry texture(String key, PlanScan.Picture picture) {
        Entry entry = TEXTURES.get(key);
        if (entry != null) {
            return entry;
        }
        NativeImage image = new NativeImage(picture.width(), picture.height(), true);
        for (int y = 0; y < picture.height(); y++) {
            for (int x = 0; x < picture.width(); x++) {
                image.setPixel(x, y, picture.argb(x, y));
            }
        }
        Identifier id = KingdomsAtWar.id("dynamic/plan_" + (counter++));
        Minecraft.getInstance().getTextureManager().register(id, new DynamicTexture(() -> "kingdomsatwar plan " + key, image));
        entry = new Entry(id, picture.width(), picture.height());
        TEXTURES.put(key, entry);
        if (TEXTURES.size() > MAX) {
            var it = TEXTURES.entrySet().iterator();
            Map.Entry<String, Entry> eldest = it.next();
            it.remove();
            Minecraft.getInstance().getTextureManager().release(eldest.getValue().id());
        }
        return entry;
    }

    /** Vergisst eine Textur (z. B. wenn sich die Vorschau geändert hat). */
    public static void forget(String key) {
        Entry entry = TEXTURES.remove(key);
        if (entry != null) {
            Minecraft.getInstance().getTextureManager().release(entry.id());
        }
    }

    /**
     * Zeichnet ein Bild auf Blaupausen-Papier: blauer Grund mit feinem Raster, das Bild darauf so groß wie möglich (in
     * ganzen Vielfachen, damit die Blöcke scharf bleiben) und mittig. Gibt zurück, wo das Bild liegt (x, y, Breite, Höhe).
     */
    public static int[] drawPanel(GuiGraphicsExtractor g, String key, PlanScan.Picture picture, int x, int y, int w, int h, int grid) {
        g.fill(x, y, x + w, y + h, 0xFF1D3A66);
        for (int gx = x + grid; gx < x + w; gx += grid) {
            g.fill(gx, y + 1, gx + 1, y + h - 1, 0x2EA9C8F0);
        }
        for (int gy = y + grid; gy < y + h; gy += grid) {
            g.fill(x + 1, gy, x + w - 1, gy + 1, 0x2EA9C8F0);
        }
        g.outline(x, y, w, h, 0xFF9DBBE4);
        if (picture.width() <= 1 && picture.height() <= 1) {
            return new int[] {x + w / 2, y + h / 2, 0, 0};
        }
        int scale = Math.max(1, Math.min((w - 6) / picture.width(), (h - 6) / picture.height()));
        int dw = picture.width() * scale;
        int dh = picture.height() * scale;
        float shrink = 1.0f;
        if (dw > w - 4 || dh > h - 4) {
            // zu groß selbst bei einfacher Größe: verkleinert zeichnen
            shrink = Math.min((w - 4) / (float) picture.width(), (h - 4) / (float) picture.height());
            dw = Math.round(picture.width() * shrink);
            dh = Math.round(picture.height() * shrink);
        }
        int px = x + (w - dw) / 2;
        int py = y + (h - dh) / 2;
        Entry entry = texture(key, picture);
        g.blit(RenderPipelines.GUI_TEXTURED, entry.id(), px, py, 0.0f, 0.0f, dw, dh, entry.width(), entry.height(), entry.width(), entry.height());
        return new int[] {px, py, dw, dh};
    }
}
