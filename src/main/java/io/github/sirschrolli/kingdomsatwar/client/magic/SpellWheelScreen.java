package io.github.sirschrolli.kingdomsatwar.client.magic;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.ModKeyMappings;
import io.github.sirschrolli.kingdomsatwar.magic.AetheritStaffItem;
import io.github.sirschrolli.kingdomsatwar.magic.KnownSpells;
import io.github.sirschrolli.kingdomsatwar.magic.Spell;
import io.github.sirschrolli.kingdomsatwar.network.SelectSpellPayload;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.UseCooldown;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

/**
 * Der Zauberkreis: eine dunkle Scheibe mit Goldringen und Siebenstern, auf der die sieben Zauber in Fassungen sitzen
 * (oben der Aether-Blitz, im Uhrzeigersinn Feuer, Eis, Blitz, Natur, Erde, Wasser – immer an derselben Stelle, damit die
 * Hand sich die Richtung merkt). Am Rand kreisen Runen, in der Mitte stehen Name, Stufe, Beschreibung und Abklingzeit
 * des Zaubers unter der Maus.
 *
 * <p>Bedienung: Taste halten, Maus in Richtung des Zaubers bewegen, loslassen. Kurz getippt bleibt der Kreis offen, dann
 * wählt ein Klick. Ziffern 1–7 wählen direkt, Rechtsklick oder Esc bricht ab. Noch nicht gelernte Zauber sind
 * gesperrt (grau mit Schloss).
 */
public class SpellWheelScreen extends Screen {
    private static final Identifier BASE = KingdomsAtWar.id("textures/gui/spell_wheel/base.png");
    private static final Identifier PARTS = KingdomsAtWar.id("textures/gui/spell_wheel/parts.png");
    private static final int TEX = 256;
    private static final int SIZE = 200;
    /** Abstand der Fassungen von der Mitte. */
    private static final int RADIUS = 74;
    /** Innerhalb dieses Abstands zur Mitte ist nichts unter der Maus. */
    private static final int DEAD_ZONE = 22;
    private static final int RUNES = 21;
    private static final Spell[] ORDER = Spell.values();
    private static final int TEXT = 0xFFF2E8FF;
    private static final int MUTED = 0xFFA898C0;
    private static final int GOLD = 0xFFF6DC96;

    private final long openedAt = Util.getMillis();
    /** Wird die Taste noch gehalten? (kurz getippt bleibt der Kreis offen, bis geklickt wird) */
    private boolean holding = true;
    private int hovered = -1;
    private int lastHovered = -1;
    private final float[] hover = new float[ORDER.length];
    /** Je Zauber ein Stab mit dessen Abklingzeit-Gruppe – zum Abfragen der Abklingzeit. */
    private final ItemStack[] probes = new ItemStack[ORDER.length];
    private long lastFrame = Util.getMillis();
    /** Nur für Vorführungen: dieser Zauber gilt als unter der Maus (-1 = echte Maus). */
    private int forcedHover = -1;

    public SpellWheelScreen() {
        super(Component.translatable("gui.kingdomsatwar.spell_wheel"));
        for (int i = 0; i < ORDER.length; i++) {
            ItemStack probe = new ItemStack(ModItems.AETHERIT_STAFF.get());
            probe.set(DataComponents.USE_COOLDOWN, new UseCooldown(1.0f, Optional.of(ORDER[i].cooldownGroup())));
            this.probes[i] = probe;
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    /** Nur für Vorführungen: einen Zauber hervorheben, als läge die Maus darauf. */
    public void devHover(int index) {
        this.forcedHover = index;
    }

    @Override
    public void tick() {
        if (this.minecraft.player == null || AetheritStaffItem.heldStaff(this.minecraft.player).isEmpty()) {
            this.onClose();
        }
    }

    private KnownSpells spells() {
        return this.minecraft.player == null ? KnownSpells.DEFAULT
                : KnownSpells.of(AetheritStaffItem.heldStaff(this.minecraft.player));
    }

    /** Öffnen: in 140 ms eingeblendet und auf volle Größe (weich abgebremst). */
    private float openProgress() {
        float t = Mth.clamp((Util.getMillis() - this.openedAt) / 140.0f, 0.0f, 1.0f);
        return 1.0f - (1.0f - t) * (1.0f - t);
    }

    /** Welcher Zauber liegt in Richtung der Maus? (-1 = Mitte) */
    private static int pick(double dx, double dy) {
        if (dx * dx + dy * dy < DEAD_ZONE * DEAD_ZONE) {
            return -1;
        }
        double angle = Math.atan2(dx, -dy);
        if (angle < 0) {
            angle += Math.PI * 2;
        }
        return (int) Math.round(angle / (Math.PI * 2 / ORDER.length)) % ORDER.length;
    }

    // =====================================================================================
    // Zeichnen
    // =====================================================================================

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
        float open = this.openProgress();
        g.fillGradient(0, 0, this.width, this.height, ARGB.color(Math.round(open * 70), 10, 4, 22),
                ARGB.color(Math.round(open * 120), 10, 4, 22));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
        long now = Util.getMillis();
        float dt = Math.min(0.1f, (now - this.lastFrame) / 1000.0f);
        this.lastFrame = now;
        KnownSpells spells = this.spells();
        int cx = this.width / 2;
        int cy = this.height / 2;
        if (this.forcedHover >= 0) {
            float angle = this.forcedHover * Mth.TWO_PI / ORDER.length;
            mouseX = cx + Math.round(Mth.sin(angle) * RADIUS);
            mouseY = cy - Math.round(Mth.cos(angle) * RADIUS);
        }
        this.hovered = pick(mouseX - cx, mouseY - cy);
        if (this.hovered != this.lastHovered) {
            if (this.hovered >= 0) {
                this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.AMETHYST_BLOCK_HIT,
                        1.2f + this.hovered * 0.08f, 0.35f));
            }
            this.lastHovered = this.hovered;
        }
        float open = this.openProgress();
        float scale = 0.86f + 0.14f * open;

        g.pose().pushMatrix();
        g.pose().translate(cx, cy);
        g.pose().scale(scale, scale);
        g.blit(RenderPipelines.GUI_TEXTURED, BASE, -SIZE / 2, -SIZE / 2, 0.0f, 0.0f, SIZE, SIZE, TEX, TEX, ARGB.white(open));
        this.drawRunes(g, now, open);
        this.drawPointer(g, mouseX - cx, mouseY - cy, open, spells);
        for (int i = 0; i < ORDER.length; i++) {
            this.drawSocket(g, i, spells, open, dt, a);
        }
        this.drawCenter(g, spells, open, a);
        g.pose().popMatrix();

        Component hint = Component.translatable(this.holding ? "gui.kingdomsatwar.spell_wheel.hint_hold" : "gui.kingdomsatwar.spell_wheel.hint_click");
        g.centeredText(this.font, hint, cx, cy + Math.round(SIZE / 2.0f * scale) + 6, ARGB.color(Math.round(open * 200), 0xC8B8E0));
    }

    /** Runenzeichen, die langsam um den Rand kreisen und nacheinander aufglimmen. */
    private void drawRunes(GuiGraphicsExtractor g, long now, float open) {
        float spin = (now % 90000L) / 90000.0f * Mth.TWO_PI;
        for (int i = 0; i < RUNES; i++) {
            float angle = spin + i * Mth.TWO_PI / RUNES;
            float x = Mth.sin(angle) * 91.5f;
            float y = -Mth.cos(angle) * 91.5f;
            float pulse = 0.35f + 0.5f * (0.5f + 0.5f * Mth.sin(now / 520.0f - i * 0.9f));
            g.pose().pushMatrix();
            g.pose().translate(x - 3.5f, y - 4.5f);
            g.blit(RenderPipelines.GUI_TEXTURED, PARTS, 0, 0, (i % 12) * 8, 56.0f, 7, 9, TEX, TEX,
                    ARGB.color(Math.round(255 * pulse * open), 0xC49AFF));
            g.pose().popMatrix();
        }
    }

    /** Leuchtender Punkt am Rand des Mittelfelds in Richtung der Maus. */
    private void drawPointer(GuiGraphicsExtractor g, int dx, int dy, float open, KnownSpells spells) {
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist < DEAD_ZONE) {
            return;
        }
        float x = (float) (dx / dist * 52.0);
        float y = (float) (dy / dist * 52.0);
        int color = this.hovered >= 0 && spells.knows(ORDER[this.hovered]) ? ORDER[this.hovered].color() : 0xC0B0D8;
        g.pose().pushMatrix();
        g.pose().translate(x - 3.5f, y - 3.5f);
        g.blit(RenderPipelines.GUI_TEXTURED, PARTS, 0, 0, 216.0f, 0.0f, 7, 7, TEX, TEX, ARGB.color(Math.round(255 * open), color));
        g.pose().popMatrix();
    }

    private void drawSocket(GuiGraphicsExtractor g, int i, KnownSpells spells, float open, float dt, float partial) {
        Spell spell = ORDER[i];
        boolean known = spells.knows(spell);
        boolean selected = spell == spells.selected();
        float target = i == this.hovered ? (known ? 1.0f : 0.35f) : 0.0f;
        this.hover[i] += (target - this.hover[i]) * Math.min(1.0f, dt * 16.0f);
        float h = this.hover[i];
        float angle = i * Mth.TWO_PI / ORDER.length;
        float sx = Mth.sin(angle) * RADIUS;
        float sy = -Mth.cos(angle) * RADIUS;
        int alpha = Math.round(255 * open);

        g.pose().pushMatrix();
        g.pose().translate(sx, sy);
        float s = 1.0f + 0.16f * h;
        g.pose().scale(s, s);
        if (known) {
            float glow = Math.max(selected ? 0.45f : 0.0f, h);
            if (glow > 0.01f) {
                g.blit(RenderPipelines.GUI_TEXTURED, PARTS, -26, -26, 80.0f, 0.0f, 52, 52, TEX, TEX,
                        ARGB.color(Math.round(255 * glow * open), spell.color()));
            }
        }
        g.blit(RenderPipelines.GUI_TEXTURED, PARTS, -18, -18, known ? 0.0f : 40.0f, 0.0f, 36, 36, TEX, TEX, ARGB.white(open));
        int iconColor = known ? ARGB.white(open) : ARGB.color(Math.round(150 * open), 70, 66, 84);
        g.blitSprite(RenderPipelines.GUI_TEXTURED, spell.icon(), -12, -12, 24, 24, iconColor);
        if (known) {
            float cooldown = this.minecraft.player.getCooldowns().getCooldownPercent(this.probes[i], partial);
            if (cooldown > 0.0f) {
                int height = Mth.ceil(24 * cooldown);
                g.fill(-12, 12 - height, 12, 12, ARGB.color(Math.round(150 * open), 12, 8, 20));
            }
            int level = spells.level(spell);
            for (int l = 0; l < Spell.MAX_LEVEL; l++) {
                g.blit(RenderPipelines.GUI_TEXTURED, PARTS, -8 + l * 6, 13, l < level ? 204.0f : 210.0f, 0.0f, 5, 5, TEX, TEX, ARGB.white(open));
            }
        } else {
            g.blit(RenderPipelines.GUI_TEXTURED, PARTS, -5, -6, 192.0f, 0.0f, 10, 12, TEX, TEX, ARGB.white(open));
        }
        if (selected) {
            g.blit(RenderPipelines.GUI_TEXTURED, PARTS, -20, -20, 136.0f, 0.0f, 40, 40, TEX, TEX, ARGB.color(Math.round(230 * open), GOLD));
        }
        if (h > 0.02f && known) {
            g.blit(RenderPipelines.GUI_TEXTURED, PARTS, -20, -20, 136.0f, 0.0f, 40, 40, TEX, TEX,
                    ARGB.color(Math.round(255 * h * open), ARGB.srgbLerp(0.35f, spell.color(), 0xFFFFFF)));
        }
        g.pose().popMatrix();
    }

    /** Mittelfeld: Name, Stufe, Beschreibung und Abklingzeit des Zaubers unter der Maus (sonst des gewählten). */
    private void drawCenter(GuiGraphicsExtractor g, KnownSpells spells, float open, float partial) {
        int index = this.hovered >= 0 ? this.hovered : spells.selected().ordinal();
        Spell spell = ORDER[index];
        boolean known = spells.knows(spell);
        int alpha = Math.round(255 * open);
        int y = -34;
        g.centeredText(this.font, spell.displayName(), 0, y, ARGB.color(alpha, known ? spell.color() : 0x9A90A8));
        y += 11;
        if (known) {
            int level = spells.level(spell);
            g.centeredText(this.font, Component.translatable("gui.kingdomsatwar.spell_wheel.level", Spell.numeral(level)), 0, y,
                    ARGB.color(alpha, GOLD));
        } else {
            g.centeredText(this.font, Component.translatable("gui.kingdomsatwar.spell_wheel.locked"), 0, y, ARGB.color(alpha, MUTED));
        }
        y += 13;
        Component text = known ? spell.description() : Component.translatable("gui.kingdomsatwar.spell_wheel.learn");
        List<FormattedCharSequence> lines = this.font.split(text, 120);
        g.pose().pushMatrix();
        g.pose().scale(0.75f, 0.75f);
        int ly = Math.round(y / 0.75f);
        for (FormattedCharSequence line : lines.subList(0, Math.min(4, lines.size()))) {
            g.centeredText(this.font, line, 0, ly, ARGB.color(alpha, known ? TEXT : MUTED));
            ly += 10;
        }
        g.pose().popMatrix();
        if (known) {
            float cooldown = this.minecraft.player.getCooldowns().getCooldownPercent(this.probes[index], partial);
            Component state;
            int color;
            if (cooldown > 0.0f) {
                float seconds = cooldown * spell.cooldown(spells.level(spell)) / 20.0f;
                state = Component.translatable("gui.kingdomsatwar.spell_wheel.cooldown", String.format(java.util.Locale.ROOT, "%.1f", seconds));
                color = 0xFFE0A0;
            } else {
                state = Component.translatable("gui.kingdomsatwar.spell_wheel.ready").withStyle(ChatFormatting.BOLD);
                color = 0x9CF08A;
            }
            g.centeredText(this.font, state, 0, 30, ARGB.color(alpha, color));
        }
    }

    // =====================================================================================
    // Bedienung
    // =====================================================================================

    /** Wählt den Zauber unter der Maus (wenn gelernt) und schließt den Kreis. */
    private void confirm() {
        if (this.hovered >= 0 && this.minecraft.player != null) {
            Spell spell = ORDER[this.hovered];
            ItemStack staff = AetheritStaffItem.heldStaff(this.minecraft.player);
            KnownSpells spells = KnownSpells.of(staff);
            if (spells.knows(spell)) {
                if (spells.selected() != spell) {
                    ClientPacketDistributor.sendToServer(new SelectSpellPayload(spell));
                    AetheritStaffItem.select(staff, spell);     // sofort anzeigen; der Server bestätigt
                    SpellHud.announce(spell, spells.level(spell));
                }
                this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.AMETHYST_BLOCK_CHIME, 1.5f, 0.8f));
            } else {
                this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.CHAIN_PLACE, 0.6f, 0.6f));
                if (!this.holding) {
                    return;             // gesperrten Zauber angeklickt: Kreis bleibt offen
                }
            }
        }
        this.onClose();
    }

    private void release() {
        if (!this.holding) {
            return;
        }
        if (this.hovered < 0 && Util.getMillis() - this.openedAt < 250L) {
            this.holding = false;       // kurz getippt: offen lassen, bis geklickt wird
        } else {
            this.confirm();
        }
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        if (ModKeyMappings.SPELL_WHEEL.matches(event)) {
            this.release();
            return true;
        }
        return super.keyReleased(event);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (ModKeyMappings.SPELL_WHEEL.matchesMouse(event)) {
            this.release();
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (!this.holding && ModKeyMappings.SPELL_WHEEL.matches(event)) {
            this.confirm();
            return true;
        }
        int key = event.key();
        if (key >= InputConstants.KEY_1 && key <= InputConstants.KEY_7) {
            this.hovered = key - InputConstants.KEY_1;
            this.holding = true;
            this.confirm();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == InputConstants.MOUSE_BUTTON_RIGHT) {
            this.onClose();
            return true;
        }
        if (event.button() == InputConstants.MOUSE_BUTTON_LEFT) {
            this.hovered = pick(event.x() - this.width / 2.0, event.y() - this.height / 2.0);
            if (this.hovered < 0) {
                this.onClose();
            } else {
                this.confirm();
            }
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }
}
