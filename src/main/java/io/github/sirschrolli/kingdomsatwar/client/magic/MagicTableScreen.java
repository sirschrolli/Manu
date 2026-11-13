package io.github.sirschrolli.kingdomsatwar.client.magic;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.magic.Inscription;
import io.github.sirschrolli.kingdomsatwar.magic.KnownSpells;
import io.github.sirschrolli.kingdomsatwar.magic.MagicTableMenu;
import io.github.sirschrolli.kingdomsatwar.magic.Spell;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * Fenster des Magietischs in arkanem Stil: links der Siegelkreis mit dem Stab in der Mitte und den sieben Zaubern
 * ringsum (gelernte leuchten in ihrer Farbe, darunter ihre Stufe; der Zauber, der gerade eingeschrieben würde, pulsiert),
 * am Rand kreisen Runen. Rechts die Fächer für Zaubertafel und Aetherit, was das Einschreiben bewirkt, was es kostet,
 * und der Knopf "Einschreiben". Nach dem Einschreiben blitzt der Kreis in der Farbe des Zaubers auf.
 */
public class MagicTableScreen extends AbstractContainerScreen<MagicTableMenu> {
    private static final Identifier BACKGROUND = KingdomsAtWar.id("textures/gui/magic_table.png");
    private static final Identifier PARTS = KingdomsAtWar.id("textures/gui/spell_wheel/parts.png");
    private static final Identifier BUTTON = KingdomsAtWar.id("magic_table/button");
    private static final Identifier BUTTON_HOVER = KingdomsAtWar.id("magic_table/button_highlighted");
    private static final Identifier BUTTON_DISABLED = KingdomsAtWar.id("magic_table/button_disabled");
    private static final Identifier XP_ICON = KingdomsAtWar.id("magic_table/experience");
    private static final Identifier AETHERIT_ICON = KingdomsAtWar.id("magic_table/aetherit");
    private static final int WIDTH = 240;
    private static final int HEIGHT = 222;
    private static final int CIRCLE_X = 63;
    private static final int CIRCLE_Y = 65;
    private static final int GEM_RADIUS = 41;
    private static final int BUTTON_X = 131;
    private static final int BUTTON_Y = 107;
    private static final int BUTTON_W = 94;
    private static final int BUTTON_H = 16;
    private static final int PANEL_X = 124;
    private static final int PANEL_W = 108;

    private static final int TEXT = 0xFFEDE3FF;
    private static final int MUTED = 0xFFA595BF;
    private static final int GOLD = 0xFFF2D28A;
    private static final int BAD = 0xFFFF7A6A;
    private static final int GOOD = 0xFF9CF08A;

    /** Aufblitzen nach dem Einschreiben (Zeitpunkt und Farbe). */
    private long flashAt = -10_000L;
    private int flashColor = Spell.AETHER_COLOR;

    public MagicTableScreen(MagicTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, WIDTH, HEIGHT);
        this.titleLabelX = PANEL_X + PANEL_W / 2;
        this.titleLabelY = 9;
        this.inventoryLabelX = MagicTableMenu.INVENTORY_X;
        this.inventoryLabelY = MagicTableMenu.INVENTORY_Y - 11;
    }

    @Override
    public void containerTick() {
        super.containerTick();
        this.minecraft.player.experienceDisplayStartTick = this.minecraft.player.tickCount;
    }

    // =====================================================================================
    // Zeichnen
    // =====================================================================================

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
        super.extractBackground(g, mouseX, mouseY, a);
        int x = this.leftPos;
        int y = this.topPos;
        g.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, x, y, 0.0f, 0.0f, WIDTH, HEIGHT, 256, 256);
        long now = Util.getMillis();
        this.drawRunes(g, x + CIRCLE_X, y + CIRCLE_Y, now);
        this.drawFlash(g, x + CIRCLE_X, y + CIRCLE_Y, now);
        this.drawGems(g, x, y, now, mouseX, mouseY);
        this.drawPanel(g, x, y, mouseX, mouseY, now);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int xm, int ym) {
        g.centeredText(this.font, this.title, this.titleLabelX, this.titleLabelY, GOLD);
        g.text(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, MUTED, false);
    }

    /** Runen kreisen langsam um den Siegelkreis. */
    private void drawRunes(GuiGraphicsExtractor g, int cx, int cy, long now) {
        float spin = (now % 120000L) / 120000.0f * Mth.TWO_PI;
        int count = 18;
        for (int i = 0; i < count; i++) {
            float angle = -spin + i * Mth.TWO_PI / count;
            float pulse = 0.3f + 0.45f * (0.5f + 0.5f * Mth.sin(now / 640.0f + i * 1.3f));
            g.pose().pushMatrix();
            g.pose().translate(cx + Mth.sin(angle) * 57.0f - 3.5f, cy - Mth.cos(angle) * 57.0f - 4.5f);
            g.blit(RenderPipelines.GUI_TEXTURED, PARTS, 0, 0, (i % 12) * 8, 56.0f, 7, 9, 256, 256, ARGB.color(Math.round(255 * pulse), 0xB98CFF));
            g.pose().popMatrix();
        }
    }

    /** Nach dem Einschreiben: ein Schein in der Farbe des Zaubers, der von der Mitte aus verglüht. */
    private void drawFlash(GuiGraphicsExtractor g, int cx, int cy, long now) {
        float t = (now - this.flashAt) / 1200.0f;
        if (t < 0.0f || t > 1.0f) {
            return;
        }
        float alpha = (1.0f - t) * (1.0f - t);
        int size = Math.round(60 + 70 * t);
        for (int layer = 0; layer < 2; layer++) {
            int s = layer == 0 ? size : size / 2;
            int color = layer == 0 ? this.flashColor : ARGB.srgbLerp(0.6f, this.flashColor, 0xFFFFFF);
            g.blit(RenderPipelines.GUI_TEXTURED, PARTS, cx - s / 2, cy - s / 2, 80.0f, 0.0f, s, s, 52, 52, 256, 256,
                    ARGB.color(Math.round(255 * alpha), color));
        }
    }

    private void drawGems(GuiGraphicsExtractor g, int x, int y, long now, int mouseX, int mouseY) {
        ItemStack staff = this.menu.staff();
        boolean hasStaff = !staff.isEmpty();
        KnownSpells spells = KnownSpells.of(staff);
        Inscription plan = this.menu.plan();
        Spell[] all = Spell.values();
        for (int i = 0; i < all.length; i++) {
            Spell spell = all[i];
            float angle = i * Mth.TWO_PI / all.length;
            int gx = x + CIRCLE_X + Math.round(Mth.sin(angle) * GEM_RADIUS);
            int gy = y + CIRCLE_Y - Math.round(Mth.cos(angle) * GEM_RADIUS);
            boolean known = hasStaff && spells.knows(spell);
            boolean target = hasStaff && plan.spell() == spell && plan.status() != Inscription.Status.MAX_LEVEL;
            if (target) {
                float pulse = 0.5f + 0.5f * Mth.sin(now / 220.0f);
                g.blit(RenderPipelines.GUI_TEXTURED, PARTS, gx - 22, gy - 22, 80.0f, 0.0f, 44, 44, 52, 52, 256, 256,
                        ARGB.color(Math.round(90 + 150 * pulse), spell.color()));
            } else if (known) {
                g.blit(RenderPipelines.GUI_TEXTURED, PARTS, gx - 18, gy - 18, 80.0f, 0.0f, 36, 36, 52, 52, 256, 256,
                        ARGB.color(70, spell.color()));
            }
            g.blit(RenderPipelines.GUI_TEXTURED, PARTS, gx - 14, gy - 14, 0.0f, 72.0f, 28, 28, 256, 256);
            g.blitSprite(RenderPipelines.GUI_TEXTURED, spell.icon(), gx - 12, gy - 12, 24, 24,
                    known || target ? -1 : ARGB.color(120, 70, 66, 84));
            if (known) {
                int level = spells.level(spell);
                for (int l = 0; l < Spell.MAX_LEVEL; l++) {
                    boolean lit = l < level;
                    boolean next = target && l == level;
                    float u = lit ? 204.0f : 210.0f;
                    int color = next ? ARGB.color(Math.round(160 + 95 * Mth.sin(now / 160.0f)), 0xFFFFFF) : -1;
                    g.blit(RenderPipelines.GUI_TEXTURED, PARTS, gx - 8 + l * 6, gy + 11, next ? 204.0f : u, 0.0f, 5, 5, 256, 256, color);
                }
                if (spell == spells.selected()) {
                    g.blit(RenderPipelines.GUI_TEXTURED, PARTS, gx - 16, gy - 16, 136.0f, 0.0f, 32, 32, 40, 40, 256, 256,
                            ARGB.color(200, 0xF6DC96));
                }
            }
            if (Math.abs(mouseX - gx) <= 12 && Math.abs(mouseY - gy) <= 12) {
                List<FormattedCharSequence> tip = new ArrayList<>();
                tip.add(spell.displayName().withColor(spell.color())
                        .append(known ? Component.literal(" " + Spell.numeral(spells.level(spell))).withStyle(ChatFormatting.GRAY)
                                : Component.empty()).getVisualOrderText());
                tip.addAll(this.font.split(spell.description().withStyle(ChatFormatting.GRAY), 170));
                if (!known) {
                    tip.add(Component.translatable("gui.kingdomsatwar.spell_wheel.locked").withStyle(ChatFormatting.DARK_GRAY)
                            .getVisualOrderText());
                }
                g.setTooltipForNextFrame(this.font, tip, mouseX, mouseY);
            }
        }
    }

    private void drawPanel(GuiGraphicsExtractor g, int x, int y, int mouseX, int mouseY, long now) {
        int left = x + PANEL_X + 8;
        int width = PANEL_W - 16;
        g.text(this.font, Component.translatable("gui.kingdomsatwar.magic_table.tablet"), x + MagicTableMenu.TABLET_X + 21,
                y + MagicTableMenu.TABLET_Y + 4, MUTED, false);
        g.text(this.font, Component.translatable("gui.kingdomsatwar.magic_table.catalyst"), x + MagicTableMenu.CATALYST_X + 21,
                y + MagicTableMenu.CATALYST_Y + 4, MUTED, false);

        Inscription plan = this.menu.plan();
        int ty = y + 71;
        if (plan.status() == Inscription.Status.NO_STAFF) {
            for (FormattedCharSequence line : this.font.split(Component.translatable(plan.status().key()), width)) {
                g.text(this.font, line, left, ty, MUTED, false);
                ty += 10;
            }
        } else if (plan.spell() != null) {
            Spell spell = plan.spell();
            Component name = spell.displayName().withColor(spell.color());
            g.text(this.font, name, left, ty, -1, false);
            String levels = plan.status() == Inscription.Status.MAX_LEVEL ? Spell.numeral(plan.fromLevel())
                    : (plan.fromLevel() == 0 ? "" : Spell.numeral(plan.fromLevel()) + " → ") + Spell.numeral(plan.toLevel());
            g.text(this.font, levels, left + width - this.font.width(levels), ty, GOLD, false);
            ty += 11;
            Component detail = plan.status() == Inscription.Status.MAX_LEVEL
                    ? Component.translatable(plan.status().key())
                    : plan.toLevel() <= 1 ? Component.translatable("gui.kingdomsatwar.magic_table.learn")
                    : spell.levelDescription(plan.toLevel());
            g.pose().pushMatrix();
            g.pose().translate(left, ty);
            g.pose().scale(0.75f, 0.75f);
            int ly = 0;
            List<FormattedCharSequence> lines = this.font.split(detail, Math.round(width / 0.75f));
            for (FormattedCharSequence line : lines.subList(0, Math.min(2, lines.size()))) {
                g.text(this.font, line, 0, ly, TEXT, false);
                ly += 10;
            }
            g.pose().popMatrix();
            if (plan.status() != Inscription.Status.MAX_LEVEL) {
                int cy = y + 97;
                boolean free = this.minecraft.player.hasInfiniteMaterials();
                boolean enoughXp = free || this.minecraft.player.experienceLevel >= plan.experience();
                boolean enoughGem = free || (Inscription.isCatalyst(this.menu.catalyst()) && this.menu.catalyst().getCount() >= plan.aetherit());
                g.blitSprite(RenderPipelines.GUI_TEXTURED, XP_ICON, left, cy - 1, 9, 9);
                String xp = String.valueOf(plan.experience());
                g.text(this.font, xp, left + 11, cy, enoughXp ? GOOD : BAD, false);
                int gemX = left + 11 + this.font.width(xp) + 12;
                g.blitSprite(RenderPipelines.GUI_TEXTURED, AETHERIT_ICON, gemX, cy - 1, 9, 9);
                g.text(this.font, String.valueOf(plan.aetherit()), gemX + 11, cy, enoughGem ? GOOD : BAD, false);
            }
        }

        // Knopf "Einschreiben"
        boolean ready = plan.ready();
        boolean hover = this.overButton(mouseX, mouseY);
        Identifier sprite = !ready ? BUTTON_DISABLED : hover ? BUTTON_HOVER : BUTTON;
        g.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x + BUTTON_X, y + BUTTON_Y, BUTTON_W, BUTTON_H);
        Component label = Component.translatable("gui.kingdomsatwar.magic_table.inscribe");
        int labelColor = !ready ? 0xFF6E6380 : hover ? 0xFFFFF4C8 : GOLD;
        g.centeredText(this.font, label, x + BUTTON_X + BUTTON_W / 2, y + BUTTON_Y + 4, labelColor);
        if (hover && !ready && plan.status() != Inscription.Status.NO_STAFF) {
            g.setTooltipForNextFrame(this.font, this.font.split(Component.translatable(plan.status().key()).withStyle(ChatFormatting.RED), 170),
                    mouseX, mouseY);
        }
    }

    private boolean overButton(double mouseX, double mouseY) {
        double mx = mouseX - this.leftPos - BUTTON_X;
        double my = mouseY - this.topPos - BUTTON_Y;
        return mx >= 0 && my >= 0 && mx < BUTTON_W && my < BUTTON_H;
    }

    // =====================================================================================
    // Bedienung
    // =====================================================================================

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (this.overButton(event.x(), event.y())) {
            this.inscribe();
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    /** "Einschreiben" (auch für Vorführungen ohne Maus). */
    public void inscribe() {
        Inscription plan = this.menu.plan();
        if (this.menu.clickMenuButton(this.minecraft.player, MagicTableMenu.BUTTON_INSCRIBE)) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, MagicTableMenu.BUTTON_INSCRIBE);
            this.flashAt = Util.getMillis();
            this.flashColor = plan.spell() != null ? plan.spell().color() : Spell.AETHER_COLOR;
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.AMETHYST_BLOCK_CHIME, 1.2f, 1.0f));
        } else {
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.CHAIN_PLACE, 0.6f, 0.6f));
        }
    }
}
