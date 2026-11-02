package io.github.sirschrolli.kingdomsatwar.client.army;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.army.Recruitment;
import io.github.sirschrolli.kingdomsatwar.army.Training;
import io.github.sirschrolli.kingdomsatwar.client.renderer.SoldierRenderer;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.network.RecruitPayload;
import io.github.sirschrolli.kingdomsatwar.network.TrainPayload;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
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
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jspecify.annotations.Nullable;

/**
 * Das Fenster des Rekrutierungstischs mit zwei Reitern. <b>Anwerben:</b> links Schwertkämpfer und Bogenschütze, in der
 * Mitte der ausgewählte Soldat zum Anschauen (er salutiert), rechts Werte, Preis und "Anwerben" – Ritter und Hauptleute
 * gibt es in den Dörfern. <b>Ausbilden:</b> links die eigenen Soldaten am Tisch, in der Mitte der ausgewählte, rechts
 * Rang, Werte und die Preise fürs Befördern und für die Ausbildung zur nächsten Art.
 */
public class RecruitScreen extends Screen {
    private static final Identifier BACKGROUND = KingdomsAtWar.id("textures/gui/recruitment_table.png");
    private static final int WIDTH = 306;
    private static final int HEIGHT = 176;
    private static final int LIST_X = 7;
    private static final int LIST_Y = 18;
    private static final int ENTRY_W = 116;
    private static final int ENTRY_H = 36;
    private static final int VISIBLE = 4;
    private static final int PREVIEW_X = 128;
    private static final int PREVIEW_Y = 18;
    private static final int PREVIEW_W = 68;
    private static final int PREVIEW_H = 96;
    private static final int INFO_X = 203;
    /** Kennungen der Vorschau-Soldaten (weit weg von echten Lebewesen). */
    private static final int PREVIEW_ID = -1_800_000_000;
    /** Am Tisch wirbt man nur die einfachen Leute an. */
    private static final SoldierType[] RECRUITABLE = {SoldierType.SWORDSMAN, SoldierType.ARCHER};

    private enum Tab {
        RECRUIT, TRAIN
    }

    private final BlockPos tablePos;
    private final int armyLimit;
    private final Map<SoldierType, SoldierEntity> previews = new EnumMap<>(SoldierType.class);
    private Tab tab = Tab.RECRUIT;
    private SoldierType selected = SoldierType.SWORDSMAN;
    private int selectedSoldier = -1;
    private int scroll;
    private int left;
    private int top;
    private @Nullable Button recruitButton;
    private @Nullable Button rankButton;
    private @Nullable Button promoteButton;
    private @Nullable Button recruitTab;
    private @Nullable Button trainTab;
    private @Nullable Component feedback;
    private int feedbackColor;
    private int feedbackTicks;
    private int ticks;

    public RecruitScreen(BlockPos tablePos, int armyLimit) {
        super(Component.translatable("gui.kingdomsatwar.recruitment.title"));
        this.tablePos = tablePos;
        this.armyLimit = armyLimit;
    }

    @Override
    protected void init() {
        this.left = (this.width - WIDTH) / 2;
        this.top = (this.height - HEIGHT) / 2;
        this.recruitTab = this.addRenderableWidget(Button.builder(Component.translatable("gui.kingdomsatwar.recruitment.tab.recruit"),
                button -> this.setTab(Tab.RECRUIT)).bounds(this.left, this.top - 21, 100, 20).build());
        this.trainTab = this.addRenderableWidget(Button.builder(Component.translatable("gui.kingdomsatwar.recruitment.tab.train"),
                button -> this.setTab(Tab.TRAIN)).bounds(this.left + 102, this.top - 21, 100, 20).build());
        this.recruitButton = this.addRenderableWidget(Button.builder(Component.translatable("gui.kingdomsatwar.recruitment.recruit"),
                button -> this.recruit()).bounds(this.left + INFO_X, this.top + 149, 96, 20).build());
        this.rankButton = this.addRenderableWidget(Button.builder(Component.translatable("gui.kingdomsatwar.recruitment.rank_up"),
                button -> this.train(Training.Action.RANK)).bounds(this.left + PREVIEW_X, this.top + 149, 84, 20).build());
        this.promoteButton = this.addRenderableWidget(Button.builder(Component.translatable("gui.kingdomsatwar.recruitment.promote"),
                button -> this.train(Training.Action.PROMOTE)).bounds(this.left + PREVIEW_X + 88, this.top + 149, 84, 20).build());
        this.updateButtons();
    }

    private void setTab(Tab tab) {
        if (this.tab != tab) {
            this.tab = tab;
            this.feedbackTicks = 0;
            this.scroll = 0;
            this.updateButtons();
        }
    }

    // =====================================================================================
    // Ablauf
    // =====================================================================================

    @Override
    public void tick() {
        this.ticks++;
        for (SoldierEntity preview : this.previews.values()) {
            preview.tickCount++;
        }
        if (this.feedbackTicks > 0) {
            this.feedbackTicks--;
        }
        this.updateButtons();
        // zu weit vom Tisch entfernt (oder Tisch abgebaut)? Dann schließen
        if (this.minecraft.player == null || this.minecraft.level == null
                || this.minecraft.player.distanceToSqr(Vec3.atCenterOf(this.tablePos)) > Recruitment.TABLE_RANGE * Recruitment.TABLE_RANGE
                || !(this.minecraft.level.getBlockState(this.tablePos).getBlock() instanceof io.github.sirschrolli.kingdomsatwar.block.RecruitmentTableBlock)) {
            this.onClose();
        }
    }

    private void updateButtons() {
        if (this.recruitButton == null || this.rankButton == null || this.promoteButton == null || this.recruitTab == null
                || this.trainTab == null || this.minecraft.player == null) {
            return;
        }
        boolean recruiting = this.tab == Tab.RECRUIT;
        this.recruitTab.active = !recruiting;
        this.trainTab.active = recruiting;
        this.recruitButton.visible = recruiting;
        this.rankButton.visible = !recruiting;
        this.promoteButton.visible = !recruiting;
        if (recruiting) {
            boolean affordable = Recruitment.canAfford(this.minecraft.player, this.selected);
            boolean room = this.armySize() < this.armyLimit;
            this.recruitButton.active = affordable && room;
            this.recruitButton.setTooltip(!room ? Tooltip.create(Component.translatable("message.kingdomsatwar.recruit.army_full"))
                    : !affordable ? Tooltip.create(Component.translatable("message.kingdomsatwar.recruit.not_enough")) : null);
            return;
        }
        SoldierEntity soldier = this.soldier();
        Training.Cost rank = soldier == null ? null : Training.rankCost(soldier.getSoldierType(), soldier.getRank());
        Training.Cost promote = soldier == null ? null : Training.promoteCost(soldier.getSoldierType());
        this.rankButton.active = rank != null && Training.canAfford(this.minecraft.player, rank);
        this.promoteButton.active = promote != null && Training.canAfford(this.minecraft.player, promote);
        this.rankButton.setTooltip(Tooltip.create(soldier == null ? Component.translatable("gui.kingdomsatwar.recruitment.no_soldier")
                : rank == null ? Component.translatable("gui.kingdomsatwar.recruitment.max_rank")
                : Component.translatable("gui.kingdomsatwar.recruitment.rank_up.hint",
                        Component.translatable("soldier.kingdomsatwar.rank." + (soldier.getRank() + 1)))));
        SoldierType next = soldier == null ? null : Training.promotion(soldier.getSoldierType());
        this.promoteButton.setMessage(next == null ? Component.translatable("gui.kingdomsatwar.recruitment.promote")
                : Component.translatable("gui.kingdomsatwar.recruitment.promote_to", Component.translatable(next.translationKey())));
        this.promoteButton.setTooltip(Tooltip.create(next == null
                ? Component.translatable("gui.kingdomsatwar.recruitment.promote.none")
                : Component.translatable("gui.kingdomsatwar.recruitment.promote.hint", Component.translatable(next.translationKey()))));
    }

    private void recruit() {
        ClientPacketDistributor.sendToServer(new RecruitPayload(this.tablePos, this.selected.ordinal()));
    }

    private void train(Training.Action action) {
        SoldierEntity soldier = this.soldier();
        if (soldier != null) {
            ClientPacketDistributor.sendToServer(new TrainPayload(this.tablePos, soldier.getId(), action.ordinal()));
        }
    }

    /** Antwort des Servers auf das Anwerben oder Ausbilden. */
    public void onResult(Recruitment.Result result) {
        this.feedback = Component.translatable("gui.kingdomsatwar.recruitment.result." + result.name().toLowerCase(java.util.Locale.ROOT));
        boolean good = result == Recruitment.Result.RECRUITED || result == Recruitment.Result.TRAINED || result == Recruitment.Result.PROMOTED;
        this.feedbackColor = good ? ArmyWidgets.GOOD : 0xFFB3261E;
        this.feedbackTicks = 60;
        if (result == Recruitment.Result.RECRUITED) {
            SoldierEntity preview = this.preview(this.selected);
            if (preview != null) {
                preview.handleEntityEvent(SoldierEntity.EVENT_CHEER);
            }
        }
        if (result == Recruitment.Result.PROMOTED) {
            // der beförderte Soldat ist ein neues Wesen: den nächsten am selben Platz auswählen
            this.selectedSoldier = -1;
        }
    }

    /** Wie viele Soldaten der Spieler hat (soweit das Spiel sie gerade kennt). */
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

    /** Die eigenen Soldaten am Tisch, die nächsten zuerst. */
    private List<SoldierEntity> soldiers() {
        List<SoldierEntity> list = new ArrayList<>();
        if (this.minecraft.level == null || this.minecraft.player == null) {
            return list;
        }
        Vec3 table = Vec3.atCenterOf(this.tablePos);
        for (var entity : this.minecraft.level.entitiesForRendering()) {
            if (entity instanceof SoldierEntity soldier && Training.atTable(this.minecraft.player, this.tablePos, soldier)) {
                list.add(soldier);
            }
        }
        list.sort(Comparator.comparingDouble(s -> s.distanceToSqr(table)));
        return list;
    }

    /** Der ausgewählte eigene Soldat (oder der erste am Tisch). */
    private @Nullable SoldierEntity soldier() {
        List<SoldierEntity> list = this.soldiers();
        for (SoldierEntity s : list) {
            if (s.getId() == this.selectedSoldier) {
                return s;
            }
        }
        if (list.isEmpty()) {
            return null;
        }
        this.selectedSoldier = list.getFirst().getId();
        return list.getFirst();
    }

    private @Nullable SoldierEntity preview(SoldierType type) {
        if (this.minecraft.level == null) {
            return null;
        }
        return this.previews.computeIfAbsent(type, t -> {
            SoldierEntity soldier = ModEntities.SOLDIERS.get(t).get().create(this.minecraft.level, EntitySpawnReason.LOAD);
            if (soldier != null) {
                soldier.setId(PREVIEW_ID - t.ordinal()); // nur zum Zeichnen, lebt nicht in der Welt
                soldier.equip();
                soldier.setVariant(0);
            }
            return soldier;
        });
    }

    // =====================================================================================
    // Eingaben
    // =====================================================================================

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (this.tab == Tab.RECRUIT) {
            for (int i = 0; i < RECRUITABLE.length; i++) {
                SoldierType type = RECRUITABLE[i];
                if (this.inEntry(event.x(), event.y(), i)) {
                    if (this.selected != type) {
                        this.select(type);
                        this.click();
                    }
                    return true;
                }
            }
        } else {
            List<SoldierEntity> list = this.soldiers();
            for (int i = 0; i < VISIBLE && this.scroll + i < list.size(); i++) {
                if (this.inEntry(event.x(), event.y(), i)) {
                    SoldierEntity soldier = list.get(this.scroll + i);
                    if (soldier.getId() != this.selectedSoldier) {
                        this.selectedSoldier = soldier.getId();
                        this.feedbackTicks = 0;
                        this.click();
                    }
                    return true;
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        if (this.tab == Tab.TRAIN) {
            int max = Math.max(0, this.soldiers().size() - VISIBLE);
            this.scroll = Mth.clamp(this.scroll - (int) Math.signum(scrollY), 0, max);
            return true;
        }
        return super.mouseScrolled(x, y, scrollX, scrollY);
    }

    private boolean inEntry(double mx, double my, int row) {
        int x = this.left + LIST_X;
        int y = this.top + LIST_Y + row * ENTRY_H;
        return mx >= x && mx < x + ENTRY_W && my >= y && my < y + ENTRY_H - 2;
    }

    private void click() {
        this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
    }

    /** Soldatenart auswählen; der Soldat im Bild salutiert. */
    public void select(SoldierType type) {
        this.selected = type;
        this.feedbackTicks = 0;
        SoldierEntity preview = this.preview(type);
        if (preview != null) {
            preview.handleEntityEvent(SoldierEntity.EVENT_SALUTE);
        }
        this.updateButtons();
    }

    /** Für Vorführungen: zum Reiter "Ausbilden" wechseln. */
    public void showTraining() {
        this.setTab(Tab.TRAIN);
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
        Component title = Component.translatable(this.tab == Tab.RECRUIT ? "gui.kingdomsatwar.recruitment.title"
                : "gui.kingdomsatwar.recruitment.title.train");
        g.text(this.font, title, x0 + 8, y0 + 6, ArmyWidgets.LABEL, false);
        Component army = Component.translatable("gui.kingdomsatwar.recruitment.army", this.armySize(), this.armyLimit);
        g.text(this.font, army, x0 + WIDTH - 8 - this.font.width(army), y0 + 6, ArmyWidgets.LABEL, false);
        if (this.tab == Tab.RECRUIT) {
            this.renderRecruit(g, mouseX, mouseY);
        } else {
            this.renderTrain(g, mouseX, mouseY);
        }
        if (this.feedback != null && this.feedbackTicks > 0) {
            int fy = this.tab == Tab.RECRUIT ? y0 + 155 : y0 + 139;
            g.text(this.font, this.feedback, x0 + PREVIEW_X, fy, this.feedbackColor, false);
        }
    }

    // ---------------------------------------------------------------- Anwerben

    private void renderRecruit(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        int x0 = this.left;
        int y0 = this.top;
        for (int i = 0; i < RECRUITABLE.length; i++) {
            this.drawTypeEntry(g, RECRUITABLE[i], x0 + LIST_X, y0 + LIST_Y + i * ENTRY_H, mouseX, mouseY);
        }
        // Ritter und Hauptleute: in den Dörfern
        int hy = y0 + LIST_Y + RECRUITABLE.length * ENTRY_H + 4;
        List<FormattedCharSequence> hint = this.font.split(Component.translatable("gui.kingdomsatwar.recruitment.villages_hint"), ENTRY_W - 4);
        for (int i = 0; i < Math.min(6, hint.size()); i++) {
            g.text(this.font, hint.get(i), x0 + LIST_X + 2, hy + i * 10, ArmyWidgets.MUTED, false);
        }
        SoldierEntity preview = this.preview(this.selected);
        if (preview != null) {
            InventoryScreen.extractEntityInInventoryFollowsMouse(g, x0 + PREVIEW_X + 1, y0 + PREVIEW_Y + 1, x0 + PREVIEW_X + PREVIEW_W - 1,
                    y0 + PREVIEW_Y + PREVIEW_H - 1, 34, 0.0625f, mouseX, mouseY, preview);
        }
        this.drawTypeInfo(g, x0 + INFO_X, y0 + PREVIEW_Y, mouseX, mouseY);
        ArmyWidgets.description(g, this.font, Component.translatable(this.selected.descriptionKey()), x0 + PREVIEW_X, y0 + 117,
                WIDTH - PREVIEW_X - 8, INFO_X - PREVIEW_X - 4, ArmyWidgets.LABEL);
    }

    private void drawTypeEntry(GuiGraphicsExtractor g, SoldierType type, int x, int y, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX < x + ENTRY_W && mouseY >= y && mouseY < y + ENTRY_H - 2;
        ArmyWidgets.entry(g, this.font, x, y, ENTRY_W, ENTRY_H - 2, hovered, type == this.selected, type, 0,
                Component.translatable(type.translationKey()).getString(), Component.translatable("soldier.kingdomsatwar." + type.id() + ".role"), 0);
    }

    private void drawTypeInfo(GuiGraphicsExtractor g, int x, int y, int mouseX, int mouseY) {
        SoldierType type = this.selected;
        g.text(this.font, Component.translatable(type.translationKey()).withStyle(ChatFormatting.BOLD), x, y + 2, ArmyWidgets.LABEL, false);
        g.text(this.font, Component.translatable("soldier.kingdomsatwar." + type.id() + ".role"), x, y + 13, ArmyWidgets.MUTED, false);
        int sy = y + 28;
        g.blitSprite(RenderPipelines.GUI_TEXTURED, ArmyWidgets.HEART, x, sy, 9, 9);
        g.text(this.font, Component.translatable("gui.kingdomsatwar.recruitment.health", (int) type.health()), x + 12, sy + 1, ArmyWidgets.LABEL, false);
        g.blitSprite(RenderPipelines.GUI_TEXTURED, ArmyWidgets.ARMOR, x, sy + 12, 9, 9);
        g.text(this.font, Component.translatable("gui.kingdomsatwar.recruitment.armor", (int) type.armor()), x + 12, sy + 13, ArmyWidgets.LABEL, false);
        ArmyWidgets.weaponIcon(g, type, x - 1, sy + 23);
        g.text(this.font, Component.translatable(type.ranged() ? "gui.kingdomsatwar.recruitment.ranged" : "gui.kingdomsatwar.recruitment.melee"),
                x + 12, sy + 25, ArmyWidgets.LABEL, false);
        int py = y + 68;
        g.text(this.font, Component.translatable("gui.kingdomsatwar.recruitment.cost"), x, py, ArmyWidgets.LABEL, false);
        List<ItemStack> items = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();
        for (SoldierType.Cost cost : type.cost()) {
            items.add(cost.stack());
            counts.add(cost.count());
        }
        ArmyWidgets.costRow(g, this.font, this.minecraft.player, x, py + 10, type.coins(), items, counts, mouseX, mouseY);
    }

    // ---------------------------------------------------------------- Ausbilden

    private void renderTrain(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        int x0 = this.left;
        int y0 = this.top;
        List<SoldierEntity> list = this.soldiers();
        SoldierEntity chosen = this.soldier();
        if (list.isEmpty()) {
            List<FormattedCharSequence> hint = this.font.split(Component.translatable("gui.kingdomsatwar.recruitment.no_soldiers"), ENTRY_W - 4);
            for (int i = 0; i < Math.min(12, hint.size()); i++) {
                g.text(this.font, hint.get(i), x0 + LIST_X + 2, y0 + LIST_Y + 4 + i * 10, ArmyWidgets.MUTED, false);
            }
        }
        this.scroll = Mth.clamp(this.scroll, 0, Math.max(0, list.size() - VISIBLE));
        for (int i = 0; i < VISIBLE && this.scroll + i < list.size(); i++) {
            this.drawSoldierEntry(g, list.get(this.scroll + i), chosen, x0 + LIST_X, y0 + LIST_Y + i * ENTRY_H, mouseX, mouseY);
        }
        if (list.size() > VISIBLE) {
            String more = (this.scroll + 1) + "–" + Math.min(list.size(), this.scroll + VISIBLE) + " / " + list.size();
            g.text(this.font, more, x0 + LIST_X + ENTRY_W - this.font.width(more), y0 + 6 + 160, ArmyWidgets.MUTED, false);
        }
        if (chosen == null) {
            return;
        }
        InventoryScreen.extractEntityInInventoryFollowsMouse(g, x0 + PREVIEW_X + 1, y0 + PREVIEW_Y + 1, x0 + PREVIEW_X + PREVIEW_W - 1,
                y0 + PREVIEW_Y + PREVIEW_H - 1, 34, 0.0625f, mouseX, mouseY, chosen);
        int x = x0 + INFO_X;
        int y = y0 + PREVIEW_Y;
        SoldierType type = chosen.getSoldierType();
        int rank = chosen.getRank();
        g.text(this.font, Component.translatable(type.translationKey()).withStyle(ChatFormatting.BOLD), x, y + 2, ArmyWidgets.LABEL, false);
        g.text(this.font, ArmyWidgets.rank(rank, SoldierEntity.MAX_RANK), x, y + 13, rank == 0 ? ArmyWidgets.MUTED : ArmyWidgets.GOLD, false);
        // Werte jetzt und nach dem Befördern
        Training.Cost rankCost = Training.rankCost(type, rank);
        int sy = y + 26;
        double health = chosen.getAttributeValue(Attributes.MAX_HEALTH);
        // Rüstung und Schaden aus Art und Rang (der Schaden wird nicht an das Spiel übertragen)
        double armor = type.armor() + 2.0 * rank;
        double attack = type.attack() + rank;
        g.blitSprite(RenderPipelines.GUI_TEXTURED, ArmyWidgets.HEART, x, sy, 9, 9);
        ArmyWidgets.stat(g, this.font, x + 12, sy + 1, Mth.ceil(chosen.getHealth()) + "/" + (int) Math.round(health),
                rankCost == null ? null : String.valueOf((int) Math.round(type.health() * (1.0 + 0.25 * (rank + 1)))));
        g.blitSprite(RenderPipelines.GUI_TEXTURED, ArmyWidgets.ARMOR, x, sy + 11, 9, 9);
        ArmyWidgets.stat(g, this.font, x + 12, sy + 12, String.valueOf((int) Math.round(armor)), rankCost == null ? null : String.valueOf((int) Math.round(armor + 2)));
        ArmyWidgets.weaponIcon(g, type, x - 1, sy + 21);
        ArmyWidgets.stat(g, this.font, x + 12, sy + 23, String.valueOf((int) Math.round(attack)), rankCost == null ? null : String.valueOf((int) Math.round(attack + 1)));
        // Preise
        int py = y + 62;
        g.text(this.font, Component.translatable("gui.kingdomsatwar.recruitment.rank_cost"), x, py, ArmyWidgets.LABEL, false);
        if (rankCost != null) {
            this.trainCostRow(g, x, py + 9, rankCost, mouseX, mouseY);
        } else {
            g.text(this.font, Component.translatable("gui.kingdomsatwar.recruitment.max_rank.short"), x, py + 14, ArmyWidgets.MUTED, false);
        }
        SoldierType next = Training.promotion(type);
        Training.Cost promote = Training.promoteCost(type);
        int qy = py + 30;
        if (next != null && promote != null) {
            g.text(this.font, Component.translatable("gui.kingdomsatwar.recruitment.promote_cost"), x, qy, ArmyWidgets.LABEL, false);
            this.trainCostRow(g, x, qy + 9, promote, mouseX, mouseY);
        } else {
            List<FormattedCharSequence> none = this.font.split(Component.translatable("gui.kingdomsatwar.recruitment.promote.none.short"), 96);
            for (int i = 0; i < Math.min(2, none.size()); i++) {
                g.text(this.font, none.get(i), x, qy + i * 10, ArmyWidgets.MUTED, false);
            }
        }
        // unter dem Bild: wer es ist
        String name = chosen.getPersonalName().isEmpty() ? chosen.getDisplayName().getString() : chosen.getPersonalName();
        String shown = this.font.plainSubstrByWidth(name, PREVIEW_W);
        g.text(this.font, shown, x0 + PREVIEW_X + (PREVIEW_W - this.font.width(shown)) / 2, y0 + 119, ArmyWidgets.LABEL, false);
    }

    private void trainCostRow(GuiGraphicsExtractor g, int x, int y, Training.Cost cost, int mouseX, int mouseY) {
        List<ItemStack> items = cost.stacks();
        List<Integer> counts = cost.items().stream().map(Training.Part::count).toList();
        ArmyWidgets.costRow(g, this.font, this.minecraft.player, x, y, cost.coins(), items, counts, mouseX, mouseY);
    }

    private void drawSoldierEntry(GuiGraphicsExtractor g, SoldierEntity soldier, @Nullable SoldierEntity chosen, int x, int y, int mouseX,
            int mouseY) {
        boolean hovered = mouseX >= x && mouseX < x + ENTRY_W && mouseY >= y && mouseY < y + ENTRY_H - 2;
        boolean isSelected = chosen != null && soldier.getId() == chosen.getId();
        String name = soldier.getPersonalName().isEmpty() ? soldier.getDisplayName().getString() : soldier.getPersonalName();
        ArmyWidgets.entry(g, this.font, x, y, ENTRY_W, ENTRY_H - 2, hovered, isSelected, soldier.getSoldierType(), soldier.getVariant(), name,
                Component.translatable(soldier.getSoldierType().translationKey()), soldier.getRank());
    }

    @Override
    public void removed() {
        this.previews.clear();
        super.removed();
    }
}
