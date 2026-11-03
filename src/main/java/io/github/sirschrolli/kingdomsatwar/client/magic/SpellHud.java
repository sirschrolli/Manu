package io.github.sirschrolli.kingdomsatwar.client.magic;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.magic.AetheritStaffItem;
import io.github.sirschrolli.kingdomsatwar.magic.KnownSpells;
import io.github.sirschrolli.kingdomsatwar.magic.Spell;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Anzeige neben der Schnellleiste, solange man einen Aetherit-Stab hält: der gewählte Zauber in einer goldenen
 * Fassung mit Stufe und Abklingzeit. Beim Wechsel des Zaubers erscheint kurz sein Name über der Schnellleiste.
 */
public final class SpellHud {
    public static final Identifier LAYER = KingdomsAtWar.id("spell_hud");
    private static final Identifier PARTS = KingdomsAtWar.id("textures/gui/spell_wheel/parts.png");
    private static final int TEX = 256;
    private static final int NAME_TICKS = 50;

    private static @Nullable Spell shown;
    private static int shownLevel;
    private static int nameTicks;
    private static @Nullable Spell lastSelected;

    private SpellHud() {}

    /** Zeigt den Namen eines eben gewählten Zaubers über der Schnellleiste. */
    public static void announce(Spell spell, int level) {
        shown = spell;
        shownLevel = level;
        nameTicks = NAME_TICKS;
        lastSelected = spell;
    }

    static void tick() {
        Player player = Minecraft.getInstance().player;
        if (nameTicks > 0) {
            nameTicks--;
        }
        if (player == null) {
            return;
        }
        ItemStack staff = AetheritStaffItem.heldStaff(player);
        if (staff.isEmpty()) {
            lastSelected = null;
            return;
        }
        KnownSpells spells = KnownSpells.of(staff);
        if (lastSelected != null && lastSelected != spells.selected()) {
            announce(spells.selected(), spells.selectedLevel());
        }
        lastSelected = spells.selected();
    }

    public static void render(GuiGraphicsExtractor g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.gui.hud.isHidden() || player.isSpectator()) {
            return;
        }
        ItemStack staff = AetheritStaffItem.heldStaff(player);
        if (staff.isEmpty()) {
            return;
        }
        KnownSpells spells = KnownSpells.of(staff);
        Spell spell = spells.selected();
        int center = g.guiWidth() / 2;
        boolean indicator = mc.options.attackIndicator().get() == AttackIndicatorStatus.HOTBAR;
        boolean offhandLeft = player.getMainArm().getOpposite() == HumanoidArm.LEFT;
        int x = offhandLeft ? center + 91 + 4 + (indicator ? 22 : 0) : center - 91 - 4 - 28 - (indicator ? 22 : 0);
        int y = g.guiHeight() - 28;

        g.blit(RenderPipelines.GUI_TEXTURED, PARTS, x - 6, y - 6, 80.0f, 0.0f, 40, 40, 52, 52, TEX, TEX, ARGB.color(70, spell.color()));
        g.blit(RenderPipelines.GUI_TEXTURED, PARTS, x, y, 0.0f, 72.0f, 28, 28, TEX, TEX);
        g.blitSprite(RenderPipelines.GUI_TEXTURED, spell.icon(), x + 2, y + 2, 24, 24);
        float cooldown = player.getCooldowns().getCooldownPercent(staff, delta.getGameTimeDeltaPartialTick(true));
        if (cooldown > 0.0f) {
            int h = Mth.ceil(24 * cooldown);
            g.fill(x + 2, y + 26 - h, x + 26, y + 26, ARGB.color(140, 12, 8, 20));
        }
        String numeral = Spell.numeral(spells.selectedLevel());
        g.text(mc.font, numeral, x + 27 - mc.font.width(numeral), y + 19, 0xFFF6DC96, true);

        if (nameTicks > 0 && shown != null) {
            float t = nameTicks - delta.getGameTimeDeltaPartialTick(true);
            int alpha = Math.round(255 * Mth.clamp(t / 10.0f, 0.0f, 1.0f));
            if (alpha > 8) {
                Component name = shown.displayName().append(" " + Spell.numeral(shownLevel));
                int w = mc.font.width(name);
                int ny = g.guiHeight() - 72;
                g.fill(center - w / 2 - 4, ny - 2, center + w / 2 + 4, ny + 10, ARGB.color(alpha / 3, 0, 0, 0));
                g.text(mc.font, name, center - w / 2, ny, ARGB.color(alpha, shown.color()), true);
            }
        }
    }
}
