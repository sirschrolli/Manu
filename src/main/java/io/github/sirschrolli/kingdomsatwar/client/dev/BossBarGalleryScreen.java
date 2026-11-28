package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.client.boss.BossBarRenderer;
import java.util.UUID;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Nur für Vorführungen: zeigt eigene Bossleisten nebeneinander und untereinander (ohne dass ein Boss im Spiel ist), mit
 * Ständen, die sich von außen ändern lassen – so lassen sich alle Leisten auf einem Bild zeigen und Treffer, Heilung und
 * Phasenwechsel vorführen. Die Welt bleibt dahinter sichtbar.
 */
final class BossBarGalleryScreen extends Screen {
    final String[] keys;
    final float[] progress;
    private final UUID[] ids;
    private final int columns;
    private final int firstTop;

    BossBarGalleryScreen(String[] keys, float[] progress, int columns, int firstTop) {
        super(Component.literal("Bossleisten"));
        this.keys = keys;
        this.progress = progress;
        this.columns = columns;
        this.firstTop = firstTop;
        this.ids = new UUID[keys.length];
        for (int i = 0; i < keys.length; i++) {
            this.ids[i] = UUID.randomUUID();
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
        // kein Schleier: die Welt bleibt dahinter sichtbar wie bei den echten Leisten
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
        int colW = this.width / this.columns;
        for (int i = 0; i < this.keys.length; i++) {
            int col = i % this.columns;
            int row = i / this.columns;
            // der Rahmen reicht links weiter als rechts (Medaillon): so steht er mittig in seiner Spalte
            int cx = colW * col + colW / 2 + 19;
            int top = this.firstTop + row * BossBarRenderer.HEIGHT;
            BossBarRenderer.draw(g, this.ids[i], Component.translatable("boss.kingdomsatwar." + this.keys[i]), this.progress[i], cx, top);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
