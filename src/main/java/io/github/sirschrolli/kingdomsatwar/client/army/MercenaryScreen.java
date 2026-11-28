package io.github.sirschrolli.kingdomsatwar.client.army;

import io.github.sirschrolli.kingdomsatwar.army.Mercenaries;
import io.github.sirschrolli.kingdomsatwar.army.Recruitment;
import io.github.sirschrolli.kingdomsatwar.block.MercenaryBoardBlock;
import io.github.sirschrolli.kingdomsatwar.client.town.Parchment;
import io.github.sirschrolli.kingdomsatwar.client.town.WoodButton;
import io.github.sirschrolli.kingdomsatwar.economy.Coin;
import io.github.sirschrolli.kingdomsatwar.economy.Coins;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.network.HirePayload;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jspecify.annotations.Nullable;
import io.github.sirschrolli.kingdomsatwar.client.gui.UiTheme;
import io.github.sirschrolli.kingdomsatwar.client.gui.UiMotion;
import net.minecraft.util.Util;

/**
 * Die Söldnertafel im Söldnerhaus eines Dorfs (Pergament wie das Stadtfenster): links die Söldner, die heute warten
 * (Gesicht, Name, Art, Rang), in der Mitte der ausgewählte im Bilderrahmen, rechts seine Werte und sein Sold, darunter was
 * er kann; unten die eigene Börse und "Anheuern". Ausbilden und befördern lassen sie sich später am eigenen
 * Feldherrentisch.
 */
public class MercenaryScreen extends Screen implements UiTheme.ParchmentStyle {
    /** Wann das Fenster aufging (für das sanfte Aufgehen, siehe {@link UiMotion}). */
    private long openedAt;
    private static final int WIDTH = ArmyWidgets.WIDTH;
    private static final int HEIGHT = ArmyWidgets.HEIGHT;
    private static final int LIST_X = 12;
    private static final int LIST_Y = 34;
    private static final int ENTRY_W = 122;
    private static final int ENTRY_H = 37;
    private static final int VISIBLE = 4;
    private static final int DIVIDER_X = 136;
    private static final int PREVIEW_X = 140;
    private static final int PREVIEW_Y = 34;
    private static final int PREVIEW_W = 68;
    private static final int PREVIEW_H = 96;
    private static final int INFO_X = 213;
    private static final int INFO_W = 95;
    /** Linie über der Beschreibung, darunter (bis zur Leiste unten) was der Söldner kann. */
    private static final int TEXT_Y = 137;
    /** Die Leiste unten (Börse und "Anheuern") und die Linie darüber. */
    private static final int BAR_Y = 188;
    private static final int BUTTON_H = 18;

    private final BlockPos board;
    private final int armyLimit;
    private final String village;
    private int selected = -1;
    private int left;
    private int top;
    private @Nullable WoodButton hireButton;
    private @Nullable Component feedback;
    private boolean feedbackGood;
    private int feedbackTicks;

    public MercenaryScreen(BlockPos board, int armyLimit, String village) {
        super(Component.translatable("gui.kingdomsatwar.mercenaries.title"));
        this.board = board;
        this.armyLimit = armyLimit;
        this.village = village;
    }

    @Override
    protected void init() {
        if (this.openedAt == 0L) {
            this.openedAt = Util.getMillis();
        }
        this.left = (this.width - WIDTH) / 2;
        this.top = (this.height - HEIGHT) / 2;
        this.hireButton = this.addRenderableWidget(new WoodButton(this.left + INFO_X, this.top + BAR_Y, INFO_W, BUTTON_H,
                Component.translatable("gui.kingdomsatwar.mercenaries.hire"), button -> this.hire()));
        this.updateButton();
    }

    // =====================================================================================
    // Ablauf
    // =====================================================================================

    @Override
    public void tick() {
        if (this.feedbackTicks > 0) {
            this.feedbackTicks--;
        }
        this.updateButton();
        if (this.minecraft.player == null || this.minecraft.level == null
                || this.minecraft.player.distanceToSqr(Vec3.atCenterOf(this.board)) > Mercenaries.BOARD_RANGE * Mercenaries.BOARD_RANGE
                || !(this.minecraft.level.getBlockState(this.board).getBlock() instanceof MercenaryBoardBlock)) {
            this.onClose();
        }
    }

    private void updateButton() {
        if (this.hireButton == null || this.minecraft.player == null) {
            return;
        }
        SoldierEntity soldier = this.soldier();
        boolean room = this.armySize() < this.armyLimit;
        boolean affordable = soldier != null && (this.minecraft.player.hasInfiniteMaterials()
                || Coins.total(this.minecraft.player) >= soldier.hirePrice());
        this.hireButton.active = soldier != null && room && affordable;
        this.hireButton.setTooltip(soldier == null ? null
                : !room ? Tooltip.create(Component.translatable("message.kingdomsatwar.recruit.army_full"))
                : !affordable ? Tooltip.create(Component.translatable("message.kingdomsatwar.recruit.not_enough"))
                : Tooltip.create(Component.translatable("gui.kingdomsatwar.mercenaries.hire.hint", soldier.getDisplayName(),
                        Coins.format(soldier.hirePrice())).append("\n").append(Component.translatable("gui.kingdomsatwar.mercenaries.hint")
                        .withStyle(ChatFormatting.GRAY))));
    }

    private void hire() {
        SoldierEntity soldier = this.soldier();
        if (soldier != null) {
            ClientPacketDistributor.sendToServer(new HirePayload(this.board, soldier.getId()));
        }
    }

    /** Antwort des Servers. */
    public void onResult(Recruitment.Result result) {
        this.feedback = Component.translatable("gui.kingdomsatwar.recruitment.result." + result.name().toLowerCase(Locale.ROOT));
        this.feedbackGood = result == Recruitment.Result.HIRED;
        this.feedbackTicks = 70;
        if (result == Recruitment.Result.HIRED) {
            this.selected = -1;
        }
    }

    /** Die Söldner an dieser Tafel, die nächsten zuerst (wie auf dem Server). */
    private List<SoldierEntity> mercenaries() {
        return this.minecraft.level == null ? List.of() : Mercenaries.at(this.minecraft.level, this.board);
    }

    private @Nullable SoldierEntity soldier() {
        List<SoldierEntity> list = this.mercenaries();
        for (SoldierEntity s : list) {
            if (s.getId() == this.selected) {
                return s;
            }
        }
        if (list.isEmpty()) {
            return null;
        }
        this.selected = list.getFirst().getId();
        return list.getFirst();
    }

    private int armySize() {
        if (this.minecraft.level == null || this.minecraft.player == null) {
            return 0;
        }
        int count = 0;
        for (var entity : this.minecraft.level.entitiesForRendering()) {
            if (entity instanceof SoldierEntity soldier && soldier.isOwnedBy(this.minecraft.player) && soldier.isAlive()) {
                count++;
            }
        }
        return count;
    }

    // =====================================================================================
    // Eingaben
    // =====================================================================================

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        List<SoldierEntity> list = this.mercenaries();
        for (int i = 0; i < Math.min(VISIBLE, list.size()); i++) {
            int x = this.left + LIST_X;
            int y = this.top + LIST_Y + i * ENTRY_H;
            if (event.x() >= x && event.x() < x + ENTRY_W && event.y() >= y && event.y() < y + ENTRY_H - 2) {
                SoldierEntity soldier = list.get(i);
                if (soldier.getId() != this.selected) {
                    this.selected = soldier.getId();
                    this.feedbackTicks = 0;
                    this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
                    this.updateButton();
                }
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    /** Für Vorführungen: den Söldner an Stelle {@code index} auswählen. */
    public void select(int index) {
        List<SoldierEntity> list = this.mercenaries();
        if (index >= 0 && index < list.size()) {
            this.selected = list.get(index).getId();
            this.updateButton();
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (this.minecraft.options.keyInventory.matches(event)) {
            this.onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean isInGameUi() {
        return true;
    }

    // =====================================================================================
    // Zeichnen
    // =====================================================================================

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
        super.extractBackground(g, mouseX, mouseY, a);
        UiMotion.begin(g, this.openedAt, this.width / 2.0f, this.height / 2.0f);
        ArmyWidgets.window(g, this.left, this.top);
        UiMotion.end(g);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
        UiMotion.begin(g, this.openedAt, this.width / 2.0f, this.height / 2.0f);
        try {
            this.extractWindow(g, mouseX, mouseY, a);
        } finally {
            UiMotion.end(g);
        }
    }

    private void extractWindow(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
        super.extractRenderState(g, mouseX, mouseY, a);
        int x0 = this.left;
        int y0 = this.top;
        Component title = (this.village.isEmpty() ? Component.translatable("gui.kingdomsatwar.mercenaries.title")
                : Component.translatable("gui.kingdomsatwar.mercenaries.title.village", this.village)).withStyle(ChatFormatting.BOLD);
        Component army = Component.translatable("gui.kingdomsatwar.recruitment.army", this.armySize(), this.armyLimit);
        ArmyWidgets.header(g, this.font, x0, y0, new ItemStack(Coin.GOLD.item()), title, army,
                this.feedback != null && this.feedbackTicks > 0 ? this.feedback : null, this.feedbackGood);
        List<SoldierEntity> list = this.mercenaries();
        SoldierEntity chosen = this.soldier();
        int shown = Math.min(VISIBLE, list.size());
        for (int i = 0; i < shown; i++) {
            this.drawEntry(g, list.get(i), chosen, x0 + LIST_X, y0 + LIST_Y + i * ENTRY_H, mouseX, mouseY);
        }
        // darunter (oder statt der Liste) ein Hinweis – nur, wenn er ganz hinpasst (sonst steht er im Tooltip von "Anheuern")
        Component hint = list.isEmpty() ? Component.translatable("gui.kingdomsatwar.mercenaries.none")
                : Component.translatable("gui.kingdomsatwar.mercenaries.hint");
        int hy = y0 + LIST_Y + shown * ENTRY_H + 2;
        int room = y0 + LIST_Y + VISIBLE * ENTRY_H - hy;
        List<FormattedCharSequence> lines = this.font.split(hint, ENTRY_W - 4);
        int spacing = lines.size() * 10 - 2 <= room ? 10 : 9;
        if (lines.size() * spacing - spacing + 8 <= room) {
            for (int i = 0; i < lines.size(); i++) {
                g.text(this.font, lines.get(i), x0 + LIST_X + 2, hy + i * spacing, ArmyWidgets.MUTED, false);
            }
        }
        Parchment.vLine(g, x0 + DIVIDER_X, y0 + LIST_Y, VISIBLE * ENTRY_H - 2);
        // unten: die eigene Börse (links neben "Anheuern")
        Parchment.hLine(g, x0 + LIST_X, y0 + BAR_Y - 5, WIDTH - 2 * LIST_X);
        Component purse = Component.translatable("gui.kingdomsatwar.town.purse");
        g.text(this.font, purse, x0 + LIST_X + 2, y0 + BAR_Y + 5, ArmyWidgets.MUTED, false);
        if (this.minecraft.player != null) {
            Parchment.price(g, this.font, x0 + LIST_X + 6 + this.font.width(purse), y0 + BAR_Y + 3, Coins.total(this.minecraft.player),
                    ArmyWidgets.LABEL);
        }
        ArmyWidgets.portrait(g, x0 + PREVIEW_X, y0 + PREVIEW_Y);
        if (chosen == null) {
            // niemand da: im Rahmen ein blasses Schild (wie ein leerer Aushang)
            Parchment.item(g, new ItemStack(net.minecraft.world.item.Items.OAK_SIGN), x0 + PREVIEW_X + PREVIEW_W / 2 - 16,
                    y0 + PREVIEW_Y + PREVIEW_H / 2 - 18, 2.0f);
            g.fill(x0 + PREVIEW_X + 4, y0 + PREVIEW_Y + 4, x0 + PREVIEW_X + PREVIEW_W - 4, y0 + PREVIEW_Y + PREVIEW_H - 4, 0x88140C06);
            // rechts daneben, wozu Söldner gut sind
            g.text(this.font, Component.translatable("gui.kingdomsatwar.mercenaries.title").withStyle(ChatFormatting.BOLD), x0 + INFO_X,
                    y0 + PREVIEW_Y + 2, ArmyWidgets.LABEL, false);
            List<FormattedCharSequence> about = this.font.split(Component.translatable("gui.kingdomsatwar.mercenaries.hint"), INFO_W);
            for (int i = 0; i < Math.min(8, about.size()); i++) {
                g.text(this.font, about.get(i), x0 + INFO_X, y0 + PREVIEW_Y + 16 + i * 10, ArmyWidgets.MUTED, false);
            }
            return;
        }
        InventoryScreen.extractEntityInInventoryFollowsMouse(g, x0 + PREVIEW_X + 4, y0 + PREVIEW_Y + 4, x0 + PREVIEW_X + PREVIEW_W - 4,
                y0 + PREVIEW_Y + PREVIEW_H - 4, 33, 0.0625f, mouseX, mouseY, chosen);
        this.drawInfo(g, chosen, x0 + INFO_X, y0 + PREVIEW_Y, mouseX, mouseY);
        // was er kann – unter Bild und Werten, über die ganze Breite
        Parchment.hLine(g, x0 + PREVIEW_X, y0 + TEXT_Y, WIDTH - LIST_X - PREVIEW_X);
        List<FormattedCharSequence> text = this.font.split(Component.translatable(chosen.getSoldierType().descriptionKey()),
                WIDTH - LIST_X - PREVIEW_X);
        int rows = Math.min(4, text.size());
        int width = WIDTH - LIST_X - PREVIEW_X;
        for (int i = 0; i < rows; i++) {
            FormattedCharSequence line = text.get(i);
            int ly = y0 + TEXT_Y + 4 + i * 10;
            if (i == rows - 1 && text.size() > rows) {
                // mehr als vier Zeilen: die letzte endet mit "…"
                StringBuilder sb = new StringBuilder();
                line.accept((index, style, codePoint) -> {
                    sb.appendCodePoint(codePoint);
                    return true;
                });
                String last = sb.toString().stripTrailing();
                g.text(this.font, this.font.width(last + " …") <= width ? last + " …" : ArmyWidgets.shorten(this.font, last, width),
                        x0 + PREVIEW_X, ly, ArmyWidgets.LABEL, false);
            } else {
                g.text(this.font, line, x0 + PREVIEW_X, ly, ArmyWidgets.LABEL, false);
            }
        }
    }

    private void drawEntry(GuiGraphicsExtractor g, SoldierEntity soldier, @Nullable SoldierEntity chosen, int x, int y, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX < x + ENTRY_W && mouseY >= y && mouseY < y + ENTRY_H - 2;
        boolean isSelected = chosen != null && chosen.getId() == soldier.getId();
        String name = soldier.getPersonalName().isEmpty() ? soldier.getDisplayName().getString() : soldier.getPersonalName();
        ArmyWidgets.entry(g, this.font, x, y, ENTRY_W, ENTRY_H - 2, hovered, isSelected, soldier.getSoldierType(), soldier.getVariant(), name,
                Component.translatable(soldier.getSoldierType().translationKey()), soldier.getRank());
        ArmyWidgets.levelBadge(g, this.font, x + 4, y + 5, soldier.getCombatLevel());
        // dritte Zeile: der Sold als kleine Münzen
        Parchment.price(g, this.font, x + ArmyWidgets.TEXT_X - 1, y + ArmyWidgets.ROW3 - 2, soldier.hirePrice(), ArmyWidgets.MUTED);
    }

    private void drawInfo(GuiGraphicsExtractor g, SoldierEntity soldier, int x, int y, int mouseX, int mouseY) {
        SoldierType type = soldier.getSoldierType();
        int rank = soldier.getRank();
        ArmyWidgets.fitText(g, this.font, Component.translatable(type.translationKey()).withStyle(ChatFormatting.BOLD), x, y + 2, INFO_W,
                ArmyWidgets.LABEL, false);
        g.text(this.font, ArmyWidgets.rank(rank, SoldierEntity.MAX_RANK), x, y + 13, rank == 0 ? ArmyWidgets.MUTED : ArmyWidgets.GOLD, false);
        int level = soldier.getCombatLevel();
        ArmyWidgets.levelLine(g, this.font, x, y + 23, INFO_W, level, soldier.getCombatExperience(), mouseX, mouseY);
        int sy = y + 34;
        g.blitSprite(RenderPipelines.GUI_TEXTURED, ArmyWidgets.HEART, x, sy, 9, 9);
        ArmyWidgets.stat(g, this.font, x + 12, sy + 1, String.valueOf(Mth.ceil(soldier.getAttributeValue(Attributes.MAX_HEALTH))), null);
        g.blitSprite(RenderPipelines.GUI_TEXTURED, ArmyWidgets.ARMOR, x, sy + 11, 9, 9);
        ArmyWidgets.stat(g, this.font, x + 12, sy + 12, String.valueOf((int) Math.round(type.armor() + 2.0 * rank
                + io.github.sirschrolli.kingdomsatwar.army.Veterancy.ARMOR_PER_LEVEL * (level - 1))), null);
        ArmyWidgets.weaponIcon(g, type, x - 1, sy + 21);
        ArmyWidgets.fitText(g, this.font, Component.translatable(type.ranged() ? "gui.kingdomsatwar.recruitment.ranged"
                : "gui.kingdomsatwar.recruitment.melee").append(" " + (int) Math.round(type.attack() + rank
                + io.github.sirschrolli.kingdomsatwar.army.Veterancy.DAMAGE_PER_LEVEL * (level - 1))), x + 12, sy + 23, INFO_W - 12,
                ArmyWidgets.LABEL, false);
        int py = y + 70;
        g.text(this.font, Component.translatable("gui.kingdomsatwar.mercenaries.wage"), x, py, ArmyWidgets.LABEL, false);
        ArmyWidgets.costRow(g, this.font, this.minecraft.player, x, py + 10, soldier.hirePrice(), List.of(), List.of(), mouseX, mouseY);
    }
}
