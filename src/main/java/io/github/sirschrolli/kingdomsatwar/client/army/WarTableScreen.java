package io.github.sirschrolli.kingdomsatwar.client.army;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.army.Provisions;
import io.github.sirschrolli.kingdomsatwar.army.Recruitment;
import io.github.sirschrolli.kingdomsatwar.army.Training;
import io.github.sirschrolli.kingdomsatwar.economy.Coins;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.network.CampaignPayload;
import io.github.sirschrolli.kingdomsatwar.network.HealPayload;
import io.github.sirschrolli.kingdomsatwar.network.ProvisionPayload;
import io.github.sirschrolli.kingdomsatwar.network.TrainPayload;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jspecify.annotations.Nullable;

/**
 * Das Fenster des Feldherrentischs. Oben „Alle heilen“: der Feldscher versorgt alle Verwundeten im Feldlager (24 Blöcke
 * um den Tisch) – der Knopf nennt seinen Lohn. Links die eigenen Soldaten im Feldlager mit Gesundheit und Proviant, in
 * der Mitte der ausgewählte, rechts Rang, Werte und die Preise fürs Befördern und für die Ausbildung zu einer anderen Art
 * (gibt es mehrere, wählt man sie mit den Pfeilen: ein Schwertkämpfer wird Ritter, Speerträger, Schildträger oder
 * Belagerungsknecht, ein Ritter Hauptmann, Lanzenreiter oder Waffenknecht, ein Bogenschütze Armbrustschütze).
 * Unten: Proviant zuteilen (aus dem Stadtlager und dem Gepäck) und mit allen auf den Schlachtzug ziehen bzw. sie heim in
 * die Garnison schicken. Neue Soldaten gibt es hier nicht – die heuert man bei den Rittern und Söldnern der Dörfer an.
 */
public class WarTableScreen extends Screen {
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
    private static final int INFO_W = 96;
    /** Die Pfeile zur Wahl der Ausbildung (Größe). */
    private static final int ARROW = 11;
    /** Wo die gewählte Ausbildung steht (Abstand vom oberen Rand). */
    private static final int PROMOTE_Y = PREVIEW_Y + 92;

    private final BlockPos tablePos;
    private final int armyLimit;
    private final int armySize;
    private final boolean hasTown;
    private int selectedSoldier = -1;
    /** Welche der möglichen Ausbildungen gewählt ist (für den ausgewählten Soldaten). */
    private int promoteChoice;
    private int scroll;
    private int left;
    private int top;
    private @Nullable Button healButton;
    private @Nullable Button rankButton;
    private @Nullable Button promoteButton;
    private @Nullable Button provisionButton;
    private @Nullable Button campaignButton;
    private @Nullable Component feedback;
    private int feedbackColor;
    private int feedbackTicks;

    public WarTableScreen(BlockPos tablePos, int armyLimit, int armySize, boolean hasTown) {
        super(Component.translatable("gui.kingdomsatwar.recruitment.title"));
        this.tablePos = tablePos;
        this.armyLimit = armyLimit;
        this.armySize = armySize;
        this.hasTown = hasTown;
    }

    @Override
    protected void init() {
        this.left = (this.width - WIDTH) / 2;
        this.top = (this.height - HEIGHT) / 2;
        this.healButton = this.addRenderableWidget(Button.builder(Component.translatable("gui.kingdomsatwar.recruitment.heal"),
                button -> this.heal()).bounds(this.left, this.top - 22, WIDTH, 20).build());
        this.rankButton = this.addRenderableWidget(Button.builder(Component.translatable("gui.kingdomsatwar.recruitment.rank_up"),
                button -> this.train(Training.Action.RANK)).bounds(this.left + PREVIEW_X, this.top + 149, 84, 20).build());
        this.promoteButton = this.addRenderableWidget(Button.builder(Component.translatable("gui.kingdomsatwar.recruitment.promote"),
                button -> this.train(Training.Action.PROMOTE)).bounds(this.left + PREVIEW_X + 88, this.top + 149, 84, 20).build());
        this.provisionButton = this.addRenderableWidget(Button.builder(Component.translatable("gui.kingdomsatwar.recruitment.provisions"),
                button -> ClientPacketDistributor.sendToServer(new ProvisionPayload(this.tablePos)))
                .bounds(this.left, this.top + HEIGHT + 2, WIDTH / 2 - 1, 20).build());
        this.campaignButton = this.addRenderableWidget(Button.builder(Component.translatable("gui.kingdomsatwar.recruitment.march"),
                button -> this.campaign()).bounds(this.left + WIDTH / 2 + 1, this.top + HEIGHT + 2, WIDTH / 2 - 1, 20).build());
        this.updateButtons();
    }

    // =====================================================================================
    // Ablauf
    // =====================================================================================

    @Override
    public void tick() {
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
        if (this.healButton == null || this.rankButton == null || this.promoteButton == null || this.provisionButton == null
                || this.campaignButton == null || this.minecraft.player == null) {
            return;
        }
        // Proviant: wie viele Rationen den Soldaten im Feldlager bis zum Vollen fehlen
        List<SoldierEntity> camp = this.soldiers();
        int missing = Provisions.missing(camp);
        this.provisionButton.active = missing > 0;
        this.provisionButton.setMessage(missing > 0 ? Component.translatable("gui.kingdomsatwar.recruitment.provisions.cost", missing)
                : Component.translatable("gui.kingdomsatwar.recruitment.provisions.full"));
        this.provisionButton.setTooltip(Tooltip.create(Component.translatable("gui.kingdomsatwar.recruitment.provisions.hint",
                Provisions.MAX_RATIONS)));
        // Schlachtzug: wer in der Garnison steht, zieht mit; wer mitgezogen ist, kehrt heim
        long home = camp.stream().filter(s -> !s.isOnCampaign()).count();
        long away = camp.size() - home;
        boolean march = home > 0;
        this.campaignButton.active = march || away > 0 && this.hasTown;
        this.campaignButton.setMessage(march ? Component.translatable("gui.kingdomsatwar.recruitment.march.count", home)
                : Component.translatable("gui.kingdomsatwar.recruitment.return.count", away));
        this.campaignButton.setTooltip(Tooltip.create(Component.translatable(march ? "gui.kingdomsatwar.recruitment.march.hint"
                : this.hasTown ? "gui.kingdomsatwar.recruitment.return.hint" : "gui.kingdomsatwar.recruitment.return.no_town")));
        // Heilen: der Lohn des Feldschers für alle Verwundeten im Feldlager
        List<SoldierEntity> wounded = this.soldiers().stream().filter(s -> Training.healCost(s) > 0).toList();
        int coins = Training.healCost(wounded);
        boolean affordable = this.minecraft.player.hasInfiniteMaterials() || Coins.canAfford(this.minecraft.player, coins);
        this.healButton.active = !wounded.isEmpty() && affordable;
        this.healButton.setMessage(wounded.isEmpty() ? Component.translatable("gui.kingdomsatwar.recruitment.heal.none")
                : Component.translatable("gui.kingdomsatwar.recruitment.heal.cost", wounded.size(), Coins.format(coins)));
        this.healButton.setTooltip(Tooltip.create(wounded.isEmpty()
                ? Component.translatable("gui.kingdomsatwar.recruitment.heal.none.hint", (int) Training.CAMP_RANGE)
                : !affordable ? Component.translatable("message.kingdomsatwar.recruit.not_enough")
                : Component.translatable("gui.kingdomsatwar.recruitment.heal.hint", (int) Training.CAMP_RANGE, Training.HEALTH_PER_COIN)));
        // Ausbilden
        SoldierEntity soldier = this.soldier();
        Training.Cost rank = soldier == null ? null : Training.rankCost(soldier.getSoldierType(), soldier.getRank());
        SoldierType chosenTarget = soldier == null ? null : this.target(soldier);
        Training.Cost promote = chosenTarget == null ? null : Training.promoteCost(soldier.getSoldierType(), chosenTarget);
        this.rankButton.active = rank != null && Training.canAfford(this.minecraft.player, rank);
        this.promoteButton.active = promote != null && Training.canAfford(this.minecraft.player, promote);
        this.rankButton.setTooltip(Tooltip.create(soldier == null ? Component.translatable("gui.kingdomsatwar.recruitment.no_soldier")
                : rank == null ? Component.translatable("gui.kingdomsatwar.recruitment.max_rank")
                : Component.translatable("gui.kingdomsatwar.recruitment.rank_up.hint",
                        Component.translatable("soldier.kingdomsatwar.rank." + (soldier.getRank() + 1)))));
        SoldierType next = soldier == null ? null : this.target(soldier);
        this.promoteButton.setMessage(Component.translatable("gui.kingdomsatwar.recruitment.promote"));
        this.promoteButton.setTooltip(Tooltip.create(next == null
                ? Component.translatable("gui.kingdomsatwar.recruitment.promote.none")
                : Component.translatable("gui.kingdomsatwar.recruitment.promote.hint", Component.translatable(next.translationKey()))
                        .append("\n\n").append(Component.translatable("soldier.kingdomsatwar." + next.id() + ".role")
                                .withStyle(ChatFormatting.GOLD))
                        .append("\n").append(Component.translatable(next.descriptionKey()).withStyle(ChatFormatting.GRAY))));
    }

    private void campaign() {
        boolean march = this.soldiers().stream().anyMatch(s -> !s.isOnCampaign());
        ClientPacketDistributor.sendToServer(new CampaignPayload(this.tablePos, march));
    }

    private void heal() {
        ClientPacketDistributor.sendToServer(new HealPayload(this.tablePos));
    }

    private void train(Training.Action action) {
        SoldierEntity soldier = this.soldier();
        if (soldier != null) {
            SoldierType target = action == Training.Action.PROMOTE ? this.target(soldier) : null;
            ClientPacketDistributor.sendToServer(new TrainPayload(this.tablePos, soldier.getId(), action.ordinal(),
                    target == null ? -1 : target.ordinal()));
        }
    }

    /** Die gewählte Ausbildung für diesen Soldaten ({@code null}: keine möglich). */
    private @Nullable SoldierType target(SoldierEntity soldier) {
        List<SoldierType> options = Training.promotions(soldier.getSoldierType());
        return options.isEmpty() ? null : options.get(Math.floorMod(this.promoteChoice, options.size()));
    }

    /** Für Vorführungen: diese Ausbildung wählen. */
    public void choose(SoldierType target) {
        SoldierEntity soldier = this.soldier();
        if (soldier != null) {
            int i = Training.promotions(soldier.getSoldierType()).indexOf(target);
            if (i >= 0) {
                this.promoteChoice = i;
                this.updateButtons();
            }
        }
    }

    /** Antwort des Servers auf Heilen oder Ausbilden. */
    public void onResult(Recruitment.Result result) {
        this.feedback = Component.translatable("gui.kingdomsatwar.recruitment.result." + result.name().toLowerCase(java.util.Locale.ROOT));
        boolean good = result == Recruitment.Result.HEALED || result == Recruitment.Result.TRAINED || result == Recruitment.Result.PROMOTED
                || result == Recruitment.Result.SUPPLIED || result == Recruitment.Result.MARCHED || result == Recruitment.Result.RETURNED;
        this.feedbackColor = good ? ArmyWidgets.GOOD : ArmyWidgets.BAD;
        this.feedbackTicks = 60;
        if (result == Recruitment.Result.PROMOTED) {
            // der beförderte Soldat ist ein neues Wesen: den nächsten am selben Platz auswählen
            this.selectedSoldier = -1;
        }
    }

    /** Wie viele Soldaten der Spieler hat (laut Stammrolle beim Öffnen; auch die gerade nicht geladenen). */
    private int armySize() {
        return this.armySize;
    }

    /** Die eigenen Soldaten im Feldlager, die nächsten zuerst. */
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

    /** Der ausgewählte eigene Soldat (oder der erste im Feldlager). */
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

    // =====================================================================================
    // Eingaben
    // =====================================================================================

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        List<SoldierEntity> list = this.soldiers();
        for (int i = 0; i < VISIBLE && this.scroll + i < list.size(); i++) {
            if (this.inEntry(event.x(), event.y(), i)) {
                SoldierEntity soldier = list.get(this.scroll + i);
                if (soldier.getId() != this.selectedSoldier) {
                    this.selectedSoldier = soldier.getId();
                    this.promoteChoice = 0;
                    this.feedbackTicks = 0;
                    this.click();
                }
                return true;
            }
        }
        // die Pfeile neben der gewählten Ausbildung
        SoldierEntity chosen = this.soldier();
        int options = chosen == null ? 0 : Training.promotions(chosen.getSoldierType()).size();
        if (options > 1) {
            int ax = this.left + INFO_X;
            int ay = this.top + PROMOTE_Y;
            if (this.inArrow(event.x(), event.y(), ax, ay)) {
                this.promoteChoice = Math.floorMod(this.promoteChoice - 1, options);
                this.click();
                this.updateButtons();
                return true;
            }
            if (this.inArrow(event.x(), event.y(), ax + INFO_W - ARROW, ay)) {
                this.promoteChoice = Math.floorMod(this.promoteChoice + 1, options);
                this.click();
                this.updateButtons();
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        int max = Math.max(0, this.soldiers().size() - VISIBLE);
        this.scroll = Mth.clamp(this.scroll - (int) Math.signum(scrollY), 0, max);
        return true;
    }

    private boolean inArrow(double mx, double my, int x, int y) {
        return mx >= x && mx < x + ARROW && my >= y - 1 && my < y + ARROW - 1;
    }

    private boolean inEntry(double mx, double my, int row) {
        int x = this.left + LIST_X;
        int y = this.top + LIST_Y + row * ENTRY_H;
        return mx >= x && mx < x + ENTRY_W && my >= y && my < y + ENTRY_H - 2;
    }

    private void click() {
        this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
    }

    /** Für Vorführungen: diesen Soldaten auswählen. */
    public void select(SoldierEntity soldier) {
        if (soldier.getId() != this.selectedSoldier) {
            this.promoteChoice = 0;
        }
        this.selectedSoldier = soldier.getId();
        this.updateButtons();
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
        g.text(this.font, Component.translatable("gui.kingdomsatwar.recruitment.title"), x0 + 8, y0 + 6, ArmyWidgets.LABEL, false);
        Component army = Component.translatable("gui.kingdomsatwar.recruitment.army", this.armySize(), this.armyLimit);
        g.text(this.font, army, x0 + WIDTH - 8 - this.font.width(army), y0 + 6, ArmyWidgets.LABEL, false);
        this.renderCamp(g, mouseX, mouseY);
        if (this.feedback != null && this.feedbackTicks > 0) {
            g.text(this.font, this.feedback, x0 + PREVIEW_X, y0 + 139, this.feedbackColor, false);
        }
    }

    private void renderCamp(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        int x0 = this.left;
        int y0 = this.top;
        List<SoldierEntity> list = this.soldiers();
        SoldierEntity chosen = this.soldier();
        if (list.isEmpty()) {
            List<FormattedCharSequence> hint = this.font.split(Component.translatable("gui.kingdomsatwar.recruitment.no_soldiers",
                    (int) Training.CAMP_RANGE), ENTRY_W - 4);
            for (int i = 0; i < Math.min(14, hint.size()); i++) {
                g.text(this.font, hint.get(i), x0 + LIST_X + 2, y0 + LIST_Y + 4 + i * 10, ArmyWidgets.MUTED, false);
            }
            List<FormattedCharSequence> villages = this.font.split(Component.translatable("gui.kingdomsatwar.recruitment.villages_hint"),
                    WIDTH - PREVIEW_X - 8);
            for (int i = 0; i < Math.min(5, villages.size()); i++) {
                g.text(this.font, villages.get(i), x0 + PREVIEW_X, y0 + PREVIEW_Y + 4 + i * 10, ArmyWidgets.MUTED, false);
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
        List<SoldierType> options = Training.promotions(type);
        SoldierType next = this.target(chosen);
        Training.Cost promote = next == null ? null : Training.promoteCost(type, next);
        int qy = py + 30;
        if (next != null && promote != null) {
            this.drawPromotion(g, x, qy, next, options.size(), mouseX, mouseY);
            this.trainCostRow(g, x, qy + 10, promote, mouseX, mouseY);
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

    /**
     * Die gewählte Ausbildung: "→ Ritter" – gibt es mehrere, mit Pfeilen links und rechts zum Wählen. Über dem Namen zeigt
     * ein Tooltip, was die neue Art kann.
     */
    private void drawPromotion(GuiGraphicsExtractor g, int x, int y, SoldierType next, int options, int mouseX, int mouseY) {
        Component name = Component.translatable(next.translationKey());
        int nameX = x;
        int nameW = INFO_W;
        // die Trennlinie des Hintergrunds (unter dem Namen im Bild) läuft hier durch – in dieser Spalte überdecken
        g.fill(x - 1, y - 2, x + INFO_W + 1, y + ARROW, 0xFFC6C6C6);
        if (options > 1) {
            this.arrow(g, x, y - 1, "‹", mouseX, mouseY);
            this.arrow(g, x + INFO_W - ARROW, y - 1, "›", mouseX, mouseY);
            nameX = x + ARROW + 1;
            nameW = INFO_W - 2 * ARROW - 2;
            int shown = Math.min(nameW, this.font.width(name));
            ArmyWidgets.fitText(g, this.font, name, nameX + (nameW - shown) / 2, y + 1, nameW, ArmyWidgets.GOLD, false);
        } else {
            ArmyWidgets.fitText(g, this.font, Component.literal("→ ").append(name), nameX, y + 1, nameW, ArmyWidgets.GOLD, false);
        }
        if (mouseX >= nameX && mouseX < nameX + nameW && mouseY >= y && mouseY < y + 10) {
            g.setTooltipForNextFrame(this.font, List.of(name.copy().withStyle(ChatFormatting.GOLD),
                    Component.translatable("soldier.kingdomsatwar." + next.id() + ".role").withStyle(ChatFormatting.YELLOW),
                    Component.translatable(next.descriptionKey()).withStyle(ChatFormatting.GRAY)).stream()
                    .flatMap(c -> this.font.split(c, 200).stream()).toList(), mouseX, mouseY);
        }
    }

    private void arrow(GuiGraphicsExtractor g, int x, int y, String glyph, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX < x + ARROW && mouseY >= y && mouseY < y + ARROW;
        g.blitSprite(RenderPipelines.GUI_TEXTURED, hovered ? ArmyWidgets.BUTTON_HIGHLIGHTED : ArmyWidgets.BUTTON, x, y, ARROW, ARROW);
        g.text(this.font, glyph, x + (ARROW - this.font.width(glyph) + 1) / 2, y + 2, hovered ? 0xFFFFE08A : 0xFFFFFFFF, true);
    }

    private void trainCostRow(GuiGraphicsExtractor g, int x, int y, Training.Cost cost, int mouseX, int mouseY) {
        List<ItemStack> items = cost.stacks();
        List<Integer> counts = cost.items().stream().map(Training.Part::count).toList();
        ArmyWidgets.costRow(g, this.font, this.minecraft.player, x, y, cost.coins(), items, counts, mouseX, mouseY);
    }

    /** Ein Soldat der Liste, darunter seine Gesundheit als Balken (grün, gelb, rot bei schweren Wunden). */
    private void drawSoldierEntry(GuiGraphicsExtractor g, SoldierEntity soldier, @Nullable SoldierEntity chosen, int x, int y, int mouseX,
            int mouseY) {
        boolean hovered = mouseX >= x && mouseX < x + ENTRY_W && mouseY >= y && mouseY < y + ENTRY_H - 2;
        boolean isSelected = chosen != null && soldier.getId() == chosen.getId();
        String name = soldier.getPersonalName().isEmpty() ? soldier.getDisplayName().getString() : soldier.getPersonalName();
        // rechts in der zweiten Zeile stehen Proviant und Schlachtzug – die Art des Soldaten muss davor enden
        String days = soldier.isHungry() ? "!" : String.valueOf(soldier.getRations());
        int dx = x + ENTRY_W - 6 - this.font.width(days);
        int iconsLeft = dx - (soldier.isOnCampaign() ? 22 : 11);
        ArmyWidgets.entry(g, this.font, x, y, ENTRY_W, ENTRY_H - 2, hovered, isSelected, soldier.getSoldierType(), soldier.getVariant(), name,
                Component.translatable(soldier.getSoldierType().translationKey()), soldier.getRank(), iconsLeft - (x + 33) - 2);
        float part = Mth.clamp(soldier.getHealth() / Math.max(1.0f, soldier.getMaxHealth()), 0.0f, 1.0f);
        int bx = x + 33;
        int by = y + 29;
        int bw = ENTRY_W - 38;
        g.fill(bx - 1, by - 1, bx + bw + 1, by + 3, 0xFF1E1E1E);
        g.fill(bx, by, bx + bw, by + 2, 0xFF5A1414);
        int color = part > 0.66f ? 0xFF4CAF50 : part > 0.33f ? 0xFFE0B030 : 0xFFE04030;
        g.fill(bx, by, bx + Math.round(bw * part), by + 2, color);
        // rechts in der zweiten Zeile: Proviant (Brot und Tage, rot bei Hunger) und ob er auf dem Schlachtzug ist
        g.text(this.font, days, dx, y + 19, soldier.isHungry() ? 0xFFFF6B5E : soldier.getRations() <= 1 ? 0xFFF2C14E : 0xFFE8E0D0, true);
        g.pose().pushMatrix();
        g.pose().translate(dx - 10, y + 18);
        g.pose().scale(0.5f, 0.5f);
        g.item(new ItemStack(net.minecraft.world.item.Items.BREAD), 0, 0);
        g.pose().popMatrix();
        if (soldier.isOnCampaign()) {
            g.pose().pushMatrix();
            g.pose().translate(dx - 21, y + 18);
            g.pose().scale(0.5f, 0.5f);
            g.item(new ItemStack(net.minecraft.world.item.Items.IRON_SWORD), 0, 0);
            g.pose().popMatrix();
        }
        if (mouseX >= dx - 22 && mouseX < x + ENTRY_W - 4 && mouseY >= y + 17 && mouseY < y + 27) {
            g.setTooltipForNextFrame(this.font, Component.translatable(soldier.isHungry() ? "gui.kingdomsatwar.recruitment.rations.hungry"
                    : "gui.kingdomsatwar.recruitment.rations", soldier.getRations()).append(soldier.isOnCampaign()
                    ? Component.translatable("gui.kingdomsatwar.recruitment.on_campaign") : Component.translatable(this.hasTown
                    ? "gui.kingdomsatwar.recruitment.in_garrison" : "gui.kingdomsatwar.recruitment.no_garrison")), mouseX, mouseY);
        }
    }
}
