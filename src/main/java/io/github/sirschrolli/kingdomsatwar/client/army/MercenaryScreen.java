package io.github.sirschrolli.kingdomsatwar.client.army;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.army.Mercenaries;
import io.github.sirschrolli.kingdomsatwar.army.Recruitment;
import io.github.sirschrolli.kingdomsatwar.block.MercenaryBoardBlock;
import io.github.sirschrolli.kingdomsatwar.economy.Coins;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.network.HirePayload;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jspecify.annotations.Nullable;

/**
 * Die Söldnertafel im Söldnerhaus eines Dorfs: links die Söldner, die heute warten (Gesicht, Name, Art, Rang), in der
 * Mitte der ausgewählte zum Anschauen, rechts seine Werte und sein Sold, darunter "Anheuern". Ausbilden und befördern
 * lassen sie sich später am eigenen Feldherrentisch.
 */
public class MercenaryScreen extends Screen {
    private static final Identifier BACKGROUND = KingdomsAtWar.id("textures/gui/recruitment_table.png");
    private static final int WIDTH = 306;
    private static final int HEIGHT = 176;
    private static final int LIST_X = 7;
    private static final int LIST_Y = 18;
    private static final int ENTRY_W = 116;
    private static final int ENTRY_H = 36;
    private static final int PREVIEW_X = 128;
    private static final int PREVIEW_Y = 18;
    private static final int PREVIEW_W = 68;
    private static final int PREVIEW_H = 96;
    private static final int INFO_X = 203;

    private final BlockPos board;
    private final int armyLimit;
    private final String village;
    private int selected = -1;
    private int left;
    private int top;
    private @Nullable Button hireButton;
    private @Nullable Component feedback;
    private int feedbackColor;
    private int feedbackTicks;

    public MercenaryScreen(BlockPos board, int armyLimit, String village) {
        super(Component.translatable("gui.kingdomsatwar.mercenaries.title"));
        this.board = board;
        this.armyLimit = armyLimit;
        this.village = village;
    }

    @Override
    protected void init() {
        this.left = (this.width - WIDTH) / 2;
        this.top = (this.height - HEIGHT) / 2;
        this.hireButton = this.addRenderableWidget(Button.builder(Component.translatable("gui.kingdomsatwar.mercenaries.hire"),
                button -> this.hire()).bounds(this.left + INFO_X, this.top + 149, 96, 20).build());
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
                        Coins.format(soldier.hirePrice()))));
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
        this.feedbackColor = result == Recruitment.Result.HIRED ? ArmyWidgets.GOOD : ArmyWidgets.BAD;
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
        for (int i = 0; i < Math.min(Mercenaries.MAX_AT_HALL + 1, list.size()); i++) {
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
        g.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, this.left, this.top, 0.0f, 0.0f, WIDTH, HEIGHT, WIDTH, HEIGHT);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
        super.extractRenderState(g, mouseX, mouseY, a);
        int x0 = this.left;
        int y0 = this.top;
        Component title = this.village.isEmpty() ? Component.translatable("gui.kingdomsatwar.mercenaries.title")
                : Component.translatable("gui.kingdomsatwar.mercenaries.title.village", this.village);
        Component army = Component.translatable("gui.kingdomsatwar.recruitment.army", this.armySize(), this.armyLimit);
        g.text(this.font, this.font.plainSubstrByWidth(title.getString(), WIDTH - 24 - this.font.width(army)), x0 + 8, y0 + 6,
                ArmyWidgets.LABEL, false);
        g.text(this.font, army, x0 + WIDTH - 8 - this.font.width(army), y0 + 6, ArmyWidgets.LABEL, false);
        List<SoldierEntity> list = this.mercenaries();
        SoldierEntity chosen = this.soldier();
        int shown = Math.min(4, list.size());
        for (int i = 0; i < shown; i++) {
            this.drawEntry(g, list.get(i), chosen, x0 + LIST_X, y0 + LIST_Y + i * ENTRY_H, mouseX, mouseY);
        }
        // darunter (oder statt der Liste) ein Hinweis
        Component hint = list.isEmpty() ? Component.translatable("gui.kingdomsatwar.mercenaries.none")
                : Component.translatable("gui.kingdomsatwar.mercenaries.hint");
        int hy = y0 + LIST_Y + shown * ENTRY_H + 4;
        List<FormattedCharSequence> lines = this.font.split(hint, ENTRY_W - 4);
        for (int i = 0; i < lines.size() && hy + i * 10 < y0 + HEIGHT - 10; i++) {
            g.text(this.font, lines.get(i), x0 + LIST_X + 2, hy + i * 10, ArmyWidgets.MUTED, false);
        }
        if (chosen != null) {
            InventoryScreen.extractEntityInInventoryFollowsMouse(g, x0 + PREVIEW_X + 1, y0 + PREVIEW_Y + 1, x0 + PREVIEW_X + PREVIEW_W - 1,
                    y0 + PREVIEW_Y + PREVIEW_H - 1, 34, 0.0625f, mouseX, mouseY, chosen);
            this.drawInfo(g, chosen, x0 + INFO_X, y0 + PREVIEW_Y, mouseX, mouseY);
            ArmyWidgets.description(g, this.font, Component.translatable(chosen.getSoldierType().descriptionKey()), x0 + PREVIEW_X, y0 + 117,
                    WIDTH - PREVIEW_X - 8, INFO_X - PREVIEW_X - 4, ArmyWidgets.LABEL);
        }
        if (this.feedback != null && this.feedbackTicks > 0) {
            g.text(this.font, this.feedback, x0 + PREVIEW_X, y0 + 155, this.feedbackColor, false);
        }
    }

    private void drawEntry(GuiGraphicsExtractor g, SoldierEntity soldier, @Nullable SoldierEntity chosen, int x, int y, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX < x + ENTRY_W && mouseY >= y && mouseY < y + ENTRY_H - 2;
        boolean isSelected = chosen != null && chosen.getId() == soldier.getId();
        String name = soldier.getPersonalName().isEmpty() ? soldier.getDisplayName().getString() : soldier.getPersonalName();
        ArmyWidgets.entry(g, this.font, x, y, ENTRY_W, ENTRY_H - 2, hovered, isSelected, soldier.getSoldierType(), soldier.getVariant(), name,
                Component.translatable(soldier.getSoldierType().translationKey()), soldier.getRank());
    }

    private void drawInfo(GuiGraphicsExtractor g, SoldierEntity soldier, int x, int y, int mouseX, int mouseY) {
        SoldierType type = soldier.getSoldierType();
        int rank = soldier.getRank();
        g.text(this.font, Component.translatable(type.translationKey()).withStyle(ChatFormatting.BOLD), x, y + 2, ArmyWidgets.LABEL, false);
        g.text(this.font, ArmyWidgets.rank(rank, SoldierEntity.MAX_RANK), x, y + 13, rank == 0 ? ArmyWidgets.MUTED : ArmyWidgets.GOLD, false);
        int sy = y + 28;
        g.blitSprite(RenderPipelines.GUI_TEXTURED, ArmyWidgets.HEART, x, sy, 9, 9);
        ArmyWidgets.stat(g, this.font, x + 12, sy + 1, String.valueOf(Mth.ceil(soldier.getAttributeValue(Attributes.MAX_HEALTH))), null);
        g.blitSprite(RenderPipelines.GUI_TEXTURED, ArmyWidgets.ARMOR, x, sy + 12, 9, 9);
        ArmyWidgets.stat(g, this.font, x + 12, sy + 13, String.valueOf((int) Math.round(type.armor() + 2.0 * rank)), null);
        ArmyWidgets.weaponIcon(g, type, x - 1, sy + 23);
        g.text(this.font, Component.translatable(type.ranged() ? "gui.kingdomsatwar.recruitment.ranged" : "gui.kingdomsatwar.recruitment.melee")
                .append(" " + (int) Math.round(type.attack() + rank)), x + 12, sy + 25, ArmyWidgets.LABEL, false);
        int py = y + 68;
        g.text(this.font, Component.translatable("gui.kingdomsatwar.mercenaries.wage"), x, py, ArmyWidgets.LABEL, false);
        ArmyWidgets.costRow(g, this.font, this.minecraft.player, x, py + 10, soldier.hirePrice(), List.of(), List.of(), mouseX, mouseY);
    }
}
