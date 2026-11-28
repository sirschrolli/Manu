package io.github.sirschrolli.kingdomsatwar.client.architect;

import io.github.sirschrolli.kingdomsatwar.architect.PlanInfo;
import io.github.sirschrolli.kingdomsatwar.item.BlueprintItem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;

/** Im Tooltip eines eigenen Bauplans: Ansicht von vorn und Grundriss, nebeneinander auf Blaupausen-Papier. */
public final class ClientPlanTooltip implements ClientTooltipComponent {
    private static final int PANEL_W = 72;
    private static final int PANEL_H = 52;
    private static final int GAP = 4;
    private final PlanInfo plan;

    public ClientPlanTooltip(BlueprintItem.PlanTooltip tooltip) {
        this.plan = tooltip.plan();
    }

    @Override
    public int getHeight(Font font) {
        return PANEL_H + 13;
    }

    @Override
    public int getWidth(Font font) {
        return PANEL_W * 2 + GAP;
    }

    @Override
    public void extractImage(Font font, int x, int y, int w, int h, GuiGraphicsExtractor g) {
        int top = y + 2;
        PlanTextures.drawPanel(g, this.plan.id() + "/front", this.plan.pictures().frontPicture(), x, top, PANEL_W, PANEL_H, 6);
        PlanTextures.drawPanel(g, this.plan.id() + "/top", this.plan.pictures().topPicture(), x + PANEL_W + GAP, top, PANEL_W, PANEL_H, 6);
        Component front = Component.translatable("gui.kingdomsatwar.plan.front");
        Component plan = Component.translatable("gui.kingdomsatwar.plan.top");
        g.text(font, front, x + (PANEL_W - font.width(front)) / 2, top + PANEL_H + 2, 0xFF9DBBE4, false);
        g.text(font, plan, x + PANEL_W + GAP + (PANEL_W - font.width(plan)) / 2, top + PANEL_H + 2, 0xFF9DBBE4, false);
    }
}
