package io.github.sirschrolli.kingdomsatwar.client.magic;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.magic.AetheritStaffItem;
import io.github.sirschrolli.kingdomsatwar.magic.KnownSpells;
import io.github.sirschrolli.kingdomsatwar.magic.Mana;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Die Mana-Leiste über der Hungerleiste, solange man einen Aetherit-Stab hält: links der Mana-Kristall, daneben die
 * Füllung mit einem wandernden Schimmer, davor die Zahl. Der Teil, den der gewählte Zauber kosten würde, leuchtet am
 * Ende der Füllung auf; reicht das Mana nicht, glüht die Leiste rot.
 */
public final class ManaHud {
    public static final Identifier LAYER = KingdomsAtWar.id("mana_bar");
    private static final Identifier BACKGROUND = KingdomsAtWar.id("mana/bar_background");
    private static final Identifier FILL = KingdomsAtWar.id("mana/bar_fill");
    private static final int W = 82;
    private static final int H = 9;
    private static final int INNER_X = 12;
    private static final int INNER_W = 68;
    private static final int INNER_Y = 2;
    private static final int INNER_H = 5;

    /** Was gerade gezeigt wird (gleitet der echten Zahl nach). */
    private static float shown = -1.0f;
    private static long lastFrame;

    private ManaHud() {}

    public static void render(GuiGraphicsExtractor g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.gui.hud.isHidden() || player.isSpectator()) {
            return;
        }
        ItemStack staff = AetheritStaffItem.heldStaff(player);
        if (staff.isEmpty()) {
            shown = -1.0f;
            return;
        }
        float max = Mana.maxFor(player);
        float mana = Math.min(Mana.get(player), max);
        long now = Util.getMillis();
        float step = Mth.clamp((now - lastFrame) / 120.0f, 0.0f, 1.0f);
        lastFrame = now;
        shown = shown < 0.0f ? mana : shown + (mana - shown) * step;

        int x = g.guiWidth() / 2 + 91 - W;
        int y = g.guiHeight() - mc.gui.hud.rightHeight;
        mc.gui.hud.rightHeight += 10;

        g.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND, x, y, W, H);
        int filled = Math.round(INNER_W * Mth.clamp(shown / max, 0.0f, 1.0f));
        if (filled > 0) {
            g.blitSprite(RenderPipelines.GUI_TEXTURED, FILL, W, H, INNER_X, 0, x + INNER_X, y, filled, H);
            // ein heller Schimmer wandert durch die Füllung
            float t = (now % 2600L) / 2600.0f;
            int sx = x + INNER_X + Math.round(t * (INNER_W + 12)) - 6;
            int from = Math.max(x + INNER_X, sx);
            int to = Math.min(x + INNER_X + filled, sx + 5);
            if (to > from) {
                g.fill(from, y + INNER_Y, to, y + INNER_Y + INNER_H, ARGB.color(70, 255, 255, 255));
            }
        }
        KnownSpells spells = KnownSpells.of(staff);
        int cost = Mana.cost(spells.selected(), spells.selectedLevel(), 0.0f);
        float pulse = 0.5f + 0.5f * Mth.sin(now / 160.0f);
        if (player.hasInfiniteMaterials() || mana + 1.0e-3f >= cost) {
            // was der gewählte Zauber kosten würde
            int costW = Math.min(filled, Math.max(1, Math.round(INNER_W * cost / max)));
            g.fill(x + INNER_X + filled - costW, y + INNER_Y, x + INNER_X + filled, y + INNER_Y + INNER_H,
                    ARGB.color(Math.round(40 + 50 * pulse), 220, 245, 255));
        } else {
            g.fill(x + INNER_X, y + INNER_Y, x + INNER_X + INNER_W, y + INNER_Y + INNER_H, ARGB.color(Math.round(50 + 70 * pulse), 255, 50, 70));
        }
        String text = String.valueOf(Math.round(shown));
        g.text(mc.font, text, x - mc.font.width(text) - 2, y + 1, 0xFF9FD6FF, true);
    }
}
