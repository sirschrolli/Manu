package io.github.sirschrolli.kingdomsatwar.client.army;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.army.Provisions;
import io.github.sirschrolli.kingdomsatwar.army.Recruitment;
import io.github.sirschrolli.kingdomsatwar.army.Training;
import io.github.sirschrolli.kingdomsatwar.army.Veterancy;
import io.github.sirschrolli.kingdomsatwar.client.town.Parchment;
import io.github.sirschrolli.kingdomsatwar.client.town.WoodButton;
import io.github.sirschrolli.kingdomsatwar.economy.Coins;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.network.CampaignPayload;
import io.github.sirschrolli.kingdomsatwar.network.HealPayload;
import io.github.sirschrolli.kingdomsatwar.network.ProvisionPayload;
import io.github.sirschrolli.kingdomsatwar.network.TrainPayload;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jspecify.annotations.Nullable;
import io.github.sirschrolli.kingdomsatwar.client.gui.UiTheme;
import io.github.sirschrolli.kingdomsatwar.client.gui.UiMotion;
import net.minecraft.util.Util;

/**
 * Das Fenster des Feldherrentischs (Pergament wie das Stadtfenster). Links die eigenen Soldaten im Feldlager (24 Blöcke um
 * den Tisch) mit Gesundheit und Proviant, in der Mitte der ausgewählte im Bilderrahmen, rechts Rang, Werte und die Preise
 * fürs Befördern und für die Ausbildung zu einer anderen Art (gibt es mehrere, wählt man sie mit den Pfeilen: ein
 * Schwertkämpfer wird Ritter, Speerträger, Schildträger oder Belagerungsknecht, ein Ritter Hauptmann, Lanzenreiter oder
 * Waffenknecht, ein Bogenschütze Armbrustschütze). Unten für alle im Feldlager: heilen (der Knopf nennt den Lohn des
 * Feldschers), Proviant zuteilen (aus dem Stadtlager und dem Gepäck) und mit allen auf den Schlachtzug ziehen bzw. sie heim
 * in die Garnison schicken. Neue Soldaten gibt es hier nicht – die heuert man bei den Rittern und Söldnern der Dörfer an.
 */
public class WarTableScreen extends Screen implements UiTheme.ParchmentStyle {
    /** Wann das Fenster aufging (für das sanfte Aufgehen, siehe {@link UiMotion}). */
    private long openedAt;
    private static final int WIDTH = ArmyWidgets.WIDTH;
    private static final int HEIGHT = ArmyWidgets.HEIGHT;
    private static final int LIST_X = 12;
    private static final int LIST_Y = 34;
    private static final int ENTRY_W = 122;
    private static final int ENTRY_H = 37;
    private static final int VISIBLE = 4;
    /** Breite des Rollbalkens rechts in der Liste (erscheint erst, wenn mehr Soldaten da sind als Platz). */
    private static final int SCROLLBAR = 4;
    /** Die senkrechte Linie zwischen Liste und Bild. */
    private static final int DIVIDER_X = 136;
    private static final int PREVIEW_X = 140;
    private static final int PREVIEW_Y = 34;
    private static final int PREVIEW_W = 68;
    private static final int PREVIEW_H = 96;
    private static final int INFO_X = 213;
    private static final int INFO_W = 95;
    /** Die Pfeile zur Wahl der Ausbildung (Größe). */
    private static final int ARROW = 11;
    /** "Befördern:" und darunter sein Preis (Abstand vom oberen Rand). */
    private static final int RANK_Y = PREVIEW_Y + 66;
    /** Die gewählte Ausbildung (Abstand vom oberen Rand), darunter ihr Preis. */
    private static final int PROMOTE_Y = RANK_Y + 29;
    /** Befördern und Ausbilden. */
    private static final int TRAIN_Y = 162;
    /** Die Leiste unten (heilen, Proviant, Schlachtzug) und die Linie darüber. */
    private static final int BAR_Y = 188;
    private static final int BUTTON_H = 18;

    private final BlockPos tablePos;
    private final int armyLimit;
    private final int armySize;
    private final boolean hasTown;
    private int selectedSoldier = -1;
    /** Welche der möglichen Ausbildungen gewählt ist (für den ausgewählten Soldaten). */
    private int promoteChoice;
    private int scroll;
    /** Wird gerade der Rollbalken der Liste gezogen? */
    private boolean draggingScroll;
    private int left;
    private int top;
    private @Nullable WoodButton healButton;
    private @Nullable WoodButton rankButton;
    private @Nullable WoodButton promoteButton;
    private @Nullable WoodButton provisionButton;
    private @Nullable WoodButton campaignButton;
    private @Nullable Component feedback;
    private boolean feedbackGood;
    private int feedbackTicks;
    private final ItemStack swordIcon = new ItemStack(Items.IRON_SWORD);
    private final ItemStack bedIcon = new ItemStack(Items.BED.red());

    public WarTableScreen(BlockPos tablePos, int armyLimit, int armySize, boolean hasTown) {
        super(Component.translatable("gui.kingdomsatwar.recruitment.title"));
        this.tablePos = tablePos;
        this.armyLimit = armyLimit;
        this.armySize = armySize;
        this.hasTown = hasTown;
    }

    @Override
    protected void init() {
        if (this.openedAt == 0L) {
            this.openedAt = Util.getMillis();
        }
        this.left = (this.width - WIDTH) / 2;
        this.top = (this.height - HEIGHT) / 2;
        int third = (WIDTH - 2 * LIST_X - 8) / 3;
        this.healButton = this.addRenderableWidget(new WoodButton(this.left + LIST_X, this.top + BAR_Y, third, BUTTON_H,
                Component.translatable("gui.kingdomsatwar.recruitment.heal.short"), button -> this.heal())
                .icon(new ItemStack(Items.GOLDEN_APPLE)));
        this.provisionButton = this.addRenderableWidget(new WoodButton(this.left + LIST_X + third + 4, this.top + BAR_Y, third, BUTTON_H,
                Component.translatable("gui.kingdomsatwar.recruitment.provisions"),
                button -> ClientPacketDistributor.sendToServer(new ProvisionPayload(this.tablePos))).icon(new ItemStack(Items.BREAD)));
        this.campaignButton = this.addRenderableWidget(new WoodButton(this.left + WIDTH - LIST_X - third, this.top + BAR_Y, third, BUTTON_H,
                Component.translatable("gui.kingdomsatwar.recruitment.march"), button -> this.campaign()));
        int half = (WIDTH - LIST_X - PREVIEW_X - 4) / 2;
        this.rankButton = this.addRenderableWidget(new WoodButton(this.left + PREVIEW_X, this.top + TRAIN_Y, half, BUTTON_H,
                Component.translatable("gui.kingdomsatwar.recruitment.rank_up"), button -> this.train(Training.Action.RANK)));
        this.promoteButton = this.addRenderableWidget(new WoodButton(this.left + WIDTH - LIST_X - half, this.top + TRAIN_Y, half, BUTTON_H,
                Component.translatable("gui.kingdomsatwar.recruitment.promote"), button -> this.train(Training.Action.PROMOTE)));
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
        this.provisionButton.setMessage(missing > 0 ? Component.translatable("gui.kingdomsatwar.recruitment.provisions.short", missing)
                : Component.translatable("gui.kingdomsatwar.recruitment.provisions.full.short"));
        this.provisionButton.setTooltip(Tooltip.create(Component.translatable(missing > 0 ? "gui.kingdomsatwar.recruitment.provisions.cost"
                : "gui.kingdomsatwar.recruitment.provisions.full", missing).withStyle(ChatFormatting.GOLD).append("\n")
                .append(Component.translatable("gui.kingdomsatwar.recruitment.provisions.hint", Provisions.MAX_RATIONS)
                        .withStyle(ChatFormatting.GRAY))));
        // Schlachtzug: wer in der Garnison steht, zieht mit; wer mitgezogen ist, kehrt heim
        long home = camp.stream().filter(s -> !s.isOnCampaign()).count();
        long away = camp.size() - home;
        boolean march = home > 0;
        this.campaignButton.active = march || away > 0 && this.hasTown;
        this.campaignButton.setMessage(march ? Component.translatable("gui.kingdomsatwar.recruitment.march.short", home)
                : Component.translatable("gui.kingdomsatwar.recruitment.return.count", away));
        this.campaignButton.icon(march ? this.swordIcon : this.bedIcon);
        this.campaignButton.setTooltip(Tooltip.create(Component.translatable(march ? "gui.kingdomsatwar.recruitment.march.hint"
                : this.hasTown ? "gui.kingdomsatwar.recruitment.return.hint" : "gui.kingdomsatwar.recruitment.return.no_town")));
        // Heilen: der Lohn des Feldschers für alle Verwundeten im Feldlager (steht als Münzen auf dem Knopf)
        List<SoldierEntity> wounded = camp.stream().filter(s -> Training.healCost(s) > 0).toList();
        int coins = Training.healCost(wounded);
        boolean affordable = this.minecraft.player.hasInfiniteMaterials() || Coins.canAfford(this.minecraft.player, coins);
        this.healButton.active = !wounded.isEmpty() && affordable;
        this.healButton.setMessage(Component.translatable(wounded.isEmpty() ? "gui.kingdomsatwar.recruitment.heal.none.short"
                : "gui.kingdomsatwar.recruitment.heal.short"));
        this.healButton.price(wounded.isEmpty() ? -1 : coins);
        this.healButton.setTooltip(Tooltip.create(wounded.isEmpty()
                ? Component.translatable("gui.kingdomsatwar.recruitment.heal.none.hint", (int) Training.CAMP_RANGE)
                : Component.translatable("gui.kingdomsatwar.recruitment.heal.cost", wounded.size(), Coins.format(coins))
                        .withStyle(ChatFormatting.GOLD).append("\n").append(!affordable
                                ? Component.translatable("message.kingdomsatwar.recruit.not_enough").withStyle(ChatFormatting.RED)
                                : Component.translatable("gui.kingdomsatwar.recruitment.heal.hint", (int) Training.CAMP_RANGE,
                                        Training.HEALTH_PER_COIN).withStyle(ChatFormatting.GRAY))));
        // Ausbilden
        SoldierEntity soldier = this.soldier();
        Training.Cost rank = soldier == null ? null : Training.rankCost(soldier.getSoldierType(), soldier.getRank());
        SoldierType chosenTarget = soldier == null ? null : this.target(soldier);
        Training.Cost promote = chosenTarget == null ? null : Training.promoteCost(soldier.getSoldierType(), chosenTarget);
        boolean ready = soldier != null && Training.readyForRank(soldier);
        this.rankButton.visible = soldier != null;
        this.promoteButton.visible = soldier != null;
        this.rankButton.active = rank != null && ready && Training.canAfford(this.minecraft.player, rank);
        this.promoteButton.active = promote != null && Training.canAfford(this.minecraft.player, promote);
        this.rankButton.setTooltip(Tooltip.create(soldier == null ? Component.translatable("gui.kingdomsatwar.recruitment.no_soldier")
                : rank == null ? Component.translatable("gui.kingdomsatwar.recruitment.max_rank")
                : this.rankHint(soldier, ready)));
        SoldierType next = soldier == null ? null : this.target(soldier);
        this.promoteButton.setMessage(Component.translatable("gui.kingdomsatwar.recruitment.promote"));
        this.promoteButton.setTooltip(Tooltip.create(next == null
                ? Component.translatable("gui.kingdomsatwar.recruitment.promote.none")
                : Component.translatable("gui.kingdomsatwar.recruitment.promote.hint", Component.translatable(next.translationKey()))
                        .append("\n\n").append(Component.translatable("soldier.kingdomsatwar." + next.id() + ".role")
                                .withStyle(ChatFormatting.GOLD))
                        .append("\n").append(Component.translatable(next.descriptionKey()).withStyle(ChatFormatting.GRAY))));
    }

    /**
     * Was das Befördern bringt (beim Meister und bei der Legende dazu ihre Gabe) – oder ab welcher Stufe es geht und wie
     * viel Kampferfahrung noch fehlt.
     */
    private Component rankHint(SoldierEntity soldier, boolean ready) {
        int next = soldier.getRank() + 1;
        Component rankName = Component.translatable("soldier.kingdomsatwar.rank." + next);
        if (!ready) {
            int needed = Veterancy.minLevel(next);
            return Component.translatable("gui.kingdomsatwar.recruitment.rank_up.needs", rankName, needed,
                    Math.max(0, Veterancy.total(needed) - soldier.getCombatExperience())).withStyle(ChatFormatting.RED)
                    .append("\n\n").append(Component.translatable("gui.kingdomsatwar.soldier.experience.hint").withStyle(ChatFormatting.GRAY));
        }
        Component hint = Component.translatable("gui.kingdomsatwar.recruitment.rank_up.hint", rankName);
        return next >= 3 ? hint.copy().append("\n").append(Component.translatable("gui.kingdomsatwar.recruitment.rank_up.perk." + next)
                .withStyle(ChatFormatting.GOLD)) : hint;
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
        this.feedbackGood = good;
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

    /**
     * Feste Reihenfolge der Liste: nach Art, dann die höchsten Ränge und Stufen zuerst, dann nach Namen. (Früher standen
     * die nächsten oben – im vollen Feldlager laufen die Soldaten aber ständig umher, und die Liste sprang dann von Bild zu
     * Bild hin und her, sodass man niemanden mehr anklicken konnte.)
     */
    private static final Comparator<SoldierEntity> ORDER = Comparator.<SoldierEntity>comparingInt(s -> s.getSoldierType().ordinal())
            .thenComparing(Comparator.comparingInt(SoldierEntity::getRank).reversed())
            .thenComparing(Comparator.comparingInt(SoldierEntity::getCombatLevel).reversed())
            .thenComparing(s -> s.getPersonalName().isEmpty() ? "\uffff" : s.getPersonalName())
            .thenComparing(s -> s.getUUID());

    /** Die eigenen Soldaten im Feldlager, in fester Reihenfolge (siehe {@link #ORDER}). */
    private List<SoldierEntity> soldiers() {
        List<SoldierEntity> list = new ArrayList<>();
        if (this.minecraft.level == null || this.minecraft.player == null) {
            return list;
        }
        for (var entity : this.minecraft.level.entitiesForRendering()) {
            if (entity instanceof SoldierEntity soldier && Training.atTable(this.minecraft.player, this.tablePos, soldier)) {
                list.add(soldier);
            }
        }
        list.sort(ORDER);
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
        if (list.size() > VISIBLE && this.inScrollbar(event.x(), event.y())) {
            this.draggingScroll = true;
            this.scrollTo(event.y(), list.size());
            return true;
        }
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
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (this.draggingScroll) {
            this.scrollTo(event.y(), this.soldiers().size());
            return true;
        }
        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        this.draggingScroll = false;
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        int max = Math.max(0, this.soldiers().size() - VISIBLE);
        this.scroll = Mth.clamp(this.scroll - (int) Math.signum(scrollY), 0, max);
        return true;
    }

    /** Die Liste so verschieben, dass der Griff des Rollbalkens bei {@code mouseY} liegt. */
    private void scrollTo(double mouseY, int size) {
        int max = Math.max(0, size - VISIBLE);
        int trackY = this.top + LIST_Y;
        int trackH = VISIBLE * ENTRY_H - 2;
        int thumb = this.thumbHeight(size);
        double f = (mouseY - trackY - thumb / 2.0) / Math.max(1, trackH - thumb);
        this.scroll = Mth.clamp((int) Math.round(f * max), 0, max);
    }

    private int thumbHeight(int size) {
        int trackH = VISIBLE * ENTRY_H - 2;
        return Mth.clamp(trackH * VISIBLE / Math.max(1, size), 14, trackH);
    }

    private boolean inScrollbar(double mx, double my) {
        int x = this.left + LIST_X + ENTRY_W - SCROLLBAR;
        int y = this.top + LIST_Y;
        return mx >= x && mx < x + SCROLLBAR && my >= y && my < y + VISIBLE * ENTRY_H - 2;
    }

    /** Pfeiltasten: den vorigen bzw. nächsten Soldaten wählen (die Liste rollt mit); Bild auf/ab blättert. */
    private boolean moveSelection(int step) {
        List<SoldierEntity> list = this.soldiers();
        if (list.isEmpty()) {
            return false;
        }
        int index = 0;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId() == this.selectedSoldier) {
                index = i;
                break;
            }
        }
        int next = Mth.clamp(index + step, 0, list.size() - 1);
        if (next != index || list.get(next).getId() != this.selectedSoldier) {
            this.selectedSoldier = list.get(next).getId();
            this.promoteChoice = 0;
            this.feedbackTicks = 0;
            this.click();
        }
        if (next < this.scroll) {
            this.scroll = next;
        } else if (next >= this.scroll + VISIBLE) {
            this.scroll = next - VISIBLE + 1;
        }
        this.updateButtons();
        return true;
    }

    private boolean inArrow(double mx, double my, int x, int y) {
        return mx >= x && mx < x + ARROW && my >= y - 1 && my < y + ARROW - 1;
    }

    private boolean inEntry(double mx, double my, int row) {
        int x = this.left + LIST_X;
        int y = this.top + LIST_Y + row * ENTRY_H;
        return mx >= x && mx < x + this.entryWidth() && my >= y && my < y + ENTRY_H - 2;
    }

    /** Breite eines Eintrags: Mit Rollbalken (mehr Soldaten als Platz) wird die Liste etwas schmaler. */
    private int entryWidth() {
        return this.soldiers().size() > VISIBLE ? ENTRY_W - SCROLLBAR - 2 : ENTRY_W;
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
        switch (event.key()) {
            case com.mojang.blaze3d.platform.InputConstants.KEY_UP -> {
                return this.moveSelection(-1);
            }
            case com.mojang.blaze3d.platform.InputConstants.KEY_DOWN -> {
                return this.moveSelection(1);
            }
            case com.mojang.blaze3d.platform.InputConstants.KEY_PAGEUP -> {
                return this.moveSelection(-VISIBLE);
            }
            case com.mojang.blaze3d.platform.InputConstants.KEY_PAGEDOWN -> {
                return this.moveSelection(VISIBLE);
            }
            default -> {
                return super.keyPressed(event);
            }
        }
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
        Component army = Component.translatable("gui.kingdomsatwar.recruitment.army", this.armySize(), this.armyLimit);
        ArmyWidgets.header(g, this.font, x0, y0, new ItemStack(ModBlocks.RECRUITMENT_TABLE.get()),
                Component.translatable("gui.kingdomsatwar.recruitment.title").withStyle(ChatFormatting.BOLD), army,
                this.feedback != null && this.feedbackTicks > 0 ? this.feedback : null, this.feedbackGood);
        // Linie über der Leiste unten (gilt für alle im Feldlager)
        Parchment.hLine(g, x0 + LIST_X, y0 + BAR_Y - 5, WIDTH - 2 * LIST_X);
        this.renderCamp(g, mouseX, mouseY);
    }

    private void renderCamp(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        int x0 = this.left;
        int y0 = this.top;
        List<SoldierEntity> list = this.soldiers();
        SoldierEntity chosen = this.soldier();
        if (list.isEmpty()) {
            // niemand im Feldlager: links der Hinweis, rechts (statt Bild und Werten) woher neue Soldaten kommen
            this.wrapped(g, Component.translatable("gui.kingdomsatwar.recruitment.no_soldiers", (int) Training.CAMP_RANGE),
                    x0 + LIST_X + 2, y0 + LIST_Y + 3, ENTRY_W - 4, 14, ArmyWidgets.MUTED);
            int cx = x0 + PREVIEW_X;
            int cy = y0 + PREVIEW_Y;
            int cw = WIDTH - LIST_X - PREVIEW_X;
            Parchment.card(g, cx, cy, cw, TRAIN_Y + BUTTON_H - PREVIEW_Y, false, false);
            Parchment.item(g, new ItemStack(Items.IRON_HELMET), cx + 6, cy + 6, 1.0f);
            g.text(this.font, Component.translatable("gui.kingdomsatwar.recruitment.villages_title").withStyle(ChatFormatting.BOLD), cx + 26, cy + 10,
                    ArmyWidgets.LABEL, false);
            this.wrapped(g, Component.translatable("gui.kingdomsatwar.recruitment.villages_hint"), cx + 6, cy + 28, cw - 12, 12,
                    ArmyWidgets.MUTED);
        } else {
            Parchment.vLine(g, x0 + DIVIDER_X, y0 + LIST_Y, VISIBLE * ENTRY_H - 2);
        }
        this.scroll = Mth.clamp(this.scroll, 0, Math.max(0, list.size() - VISIBLE));
        int entryW = this.entryWidth();
        for (int i = 0; i < VISIBLE && this.scroll + i < list.size(); i++) {
            this.drawSoldierEntry(g, list.get(this.scroll + i), chosen, x0 + LIST_X, y0 + LIST_Y + i * ENTRY_H, entryW, mouseX, mouseY);
        }
        if (list.size() > VISIBLE) {
            this.drawScrollbar(g, list.size(), mouseX, mouseY);
        }
        if (chosen == null) {
            return;
        }
        ArmyWidgets.portrait(g, x0 + PREVIEW_X, y0 + PREVIEW_Y);
        InventoryScreen.extractEntityInInventoryFollowsMouse(g, x0 + PREVIEW_X + 4, y0 + PREVIEW_Y + 4, x0 + PREVIEW_X + PREVIEW_W - 4,
                y0 + PREVIEW_Y + PREVIEW_H - 4, 33, 0.0625f, mouseX, mouseY, chosen);
        // unter dem Bild: wer es ist
        String name = chosen.getPersonalName().isEmpty() ? chosen.getDisplayName().getString() : chosen.getPersonalName();
        String shown = ArmyWidgets.shorten(this.font, name, PREVIEW_W + 4);
        g.text(this.font, shown, x0 + PREVIEW_X + (PREVIEW_W - this.font.width(shown)) / 2, y0 + PREVIEW_Y + PREVIEW_H + 4, ArmyWidgets.LABEL,
                false);
        int x = x0 + INFO_X;
        int y = y0 + PREVIEW_Y;
        SoldierType type = chosen.getSoldierType();
        int rank = chosen.getRank();
        ArmyWidgets.fitText(g, this.font, Component.translatable(type.translationKey()).withStyle(ChatFormatting.BOLD), x, y + 2, INFO_W,
                ArmyWidgets.LABEL, false);
        g.text(this.font, ArmyWidgets.rank(rank, SoldierEntity.MAX_RANK), x, y + 13, rank == 0 ? ArmyWidgets.MUTED : ArmyWidgets.GOLD, false);
        // Stufe und der Weg zur nächsten
        int level = chosen.getCombatLevel();
        ArmyWidgets.levelLine(g, this.font, x, y + 23, INFO_W, level, chosen.getCombatExperience(), mouseX, mouseY);
        // Werte jetzt und nach dem Befördern
        Training.Cost rankCost = Training.rankCost(type, rank);
        int sy = y + 34;
        double health = chosen.getAttributeValue(Attributes.MAX_HEALTH);
        // Rüstung und Schaden aus Art, Rang und Stufe (der Schaden wird nicht an das Spiel übertragen)
        double armor = type.armor() + 2.0 * rank + Veterancy.ARMOR_PER_LEVEL * (level - 1);
        double attack = type.attack() + rank + Veterancy.DAMAGE_PER_LEVEL * (level - 1);
        g.blitSprite(RenderPipelines.GUI_TEXTURED, ArmyWidgets.HEART, x, sy, 9, 9);
        ArmyWidgets.stat(g, this.font, x + 12, sy + 1, Mth.ceil(chosen.getHealth()) + "/" + Mth.ceil(health),
                rankCost == null ? null : String.valueOf(Mth.ceil(health + type.health() * 0.25)));
        g.blitSprite(RenderPipelines.GUI_TEXTURED, ArmyWidgets.ARMOR, x, sy + 11, 9, 9);
        ArmyWidgets.stat(g, this.font, x + 12, sy + 12, String.valueOf((int) Math.round(armor)), rankCost == null ? null : String.valueOf((int) Math.round(armor + 2)));
        ArmyWidgets.weaponIcon(g, type, x - 1, sy + 21);
        ArmyWidgets.stat(g, this.font, x + 12, sy + 23, String.valueOf((int) Math.round(attack)), rankCost == null ? null : String.valueOf((int) Math.round(attack + 1)));
        // Preise – und ab welcher Stufe befördert werden kann
        int py = y0 + RANK_Y;
        if (rankCost != null && !Training.readyForRank(chosen)) {
            // noch zu wenig Erfahrung: statt "Befördern:" steht da, ab welcher Stufe es geht
            g.text(this.font, Component.translatable("gui.kingdomsatwar.recruitment.rank_requirement", Veterancy.minLevel(rank + 1)), x, py,
                    ArmyWidgets.BAD, false);
        } else {
            g.text(this.font, Component.translatable("gui.kingdomsatwar.recruitment.rank_cost"), x, py, ArmyWidgets.LABEL, false);
        }
        if (rankCost != null) {
            this.trainCostRow(g, x, py + 9, rankCost, mouseX, mouseY);
        } else {
            g.text(this.font, Component.translatable("gui.kingdomsatwar.recruitment.max_rank.short"), x, py + 13, ArmyWidgets.MUTED, false);
        }
        List<SoldierType> options = Training.promotions(type);
        SoldierType next = this.target(chosen);
        Training.Cost promote = next == null ? null : Training.promoteCost(type, next);
        int qy = y0 + PROMOTE_Y;
        if (next != null && promote != null) {
            this.drawPromotion(g, x, qy, next, options.size(), mouseX, mouseY);
            this.trainCostRow(g, x, qy + 10, promote, mouseX, mouseY);
        } else {
            this.wrapped(g, Component.translatable("gui.kingdomsatwar.recruitment.promote.none.short"), x, qy, INFO_W, 2, ArmyWidgets.MUTED);
        }
    }

    /** Text mit Zeilenumbruch, höchstens {@code maxLines} Zeilen. */
    private void wrapped(GuiGraphicsExtractor g, Component text, int x, int y, int width, int maxLines, int color) {
        List<FormattedCharSequence> lines = this.font.split(text, width);
        for (int i = 0; i < Math.min(maxLines, lines.size()); i++) {
            g.text(this.font, lines.get(i), x, y + i * 10, color, false);
        }
    }

    /**
     * Die gewählte Ausbildung: "→ Ritter" – gibt es mehrere, mit Pfeilen links und rechts zum Wählen. Über dem Namen zeigt
     * ein Tooltip, was die neue Art kann.
     */
    private void drawPromotion(GuiGraphicsExtractor g, int x, int y, SoldierType next, int options, int mouseX, int mouseY) {
        Component name = Component.translatable(next.translationKey());
        int nameX = x;
        int nameW = INFO_W;
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
        Parchment.woodButton(g, this.font, x, y, ARROW, ARROW, Component.literal(glyph), true, hovered, null);
    }

    private void trainCostRow(GuiGraphicsExtractor g, int x, int y, Training.Cost cost, int mouseX, int mouseY) {
        List<ItemStack> items = cost.stacks();
        List<Integer> counts = cost.items().stream().map(Training.Part::count).toList();
        ArmyWidgets.costRow(g, this.font, this.minecraft.player, x, y, cost.coins(), items, counts, mouseX, mouseY);
    }

    /** Der Rollbalken rechts neben der Liste: helle Rinne, Griff aus Holz. */
    private void drawScrollbar(GuiGraphicsExtractor g, int size, int mouseX, int mouseY) {
        int x = this.left + LIST_X + ENTRY_W - SCROLLBAR;
        int y = this.top + LIST_Y;
        int h = VISIBLE * ENTRY_H - 2;
        int thumb = this.thumbHeight(size);
        int max = Math.max(1, size - VISIBLE);
        int ty = y + Math.round((h - thumb) * (this.scroll / (float) max));
        ArmyWidgets.scrollbar(g, x, y, SCROLLBAR, h, ty, thumb, this.draggingScroll || this.inScrollbar(mouseX, mouseY));
    }

    /** Ein Soldat der Liste, darunter seine Gesundheit als Balken (grün, gelb, rot bei schweren Wunden). */
    private void drawSoldierEntry(GuiGraphicsExtractor g, SoldierEntity soldier, @Nullable SoldierEntity chosen, int x, int y, int entryW,
            int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX < x + entryW && mouseY >= y && mouseY < y + ENTRY_H - 2;
        boolean isSelected = chosen != null && soldier.getId() == chosen.getId();
        String name = soldier.getPersonalName().isEmpty() ? soldier.getDisplayName().getString() : soldier.getPersonalName();
        ArmyWidgets.entry(g, this.font, x, y, entryW, ENTRY_H - 2, hovered, isSelected, soldier.getSoldierType(), soldier.getVariant(), name,
                Component.translatable(soldier.getSoldierType().translationKey()), soldier.getRank());
        ArmyWidgets.levelBadge(g, this.font, x + 4, y + 5, soldier.getCombatLevel());
        // dritte Zeile: die Gesundheit als Balken, rechts daneben ob er auf dem Schlachtzug ist und sein Proviant (Brot und
        // Tage, rot bei Hunger)
        String days = soldier.isHungry() ? "!" : String.valueOf(soldier.getRations());
        int ry = y + ArmyWidgets.ROW3;
        int dx = x + entryW - 4 - this.font.width(days);
        int iconsLeft = dx - (soldier.isOnCampaign() ? 22 : 11);
        float part = Mth.clamp(soldier.getHealth() / Math.max(1.0f, soldier.getMaxHealth()), 0.0f, 1.0f);
        int color = part > 0.66f ? 0xFF4E9A38 : part > 0.33f ? 0xFFD0A02C : 0xFFC8402C;
        int bx = x + ArmyWidgets.TEXT_X;
        Parchment.bar(g, bx, ry + 2, iconsLeft - 3 - bx, 4, part, color);
        g.text(this.font, days, dx, ry, soldier.isHungry() ? ArmyWidgets.BAD : soldier.getRations() <= 1 ? ArmyWidgets.GOLD
                : ArmyWidgets.LABEL, false);
        Parchment.item(g, new ItemStack(Items.BREAD), dx - 10, ry, 0.5f);
        if (soldier.isOnCampaign()) {
            Parchment.item(g, new ItemStack(Items.IRON_SWORD), dx - 21, ry, 0.5f);
        }
        if (mouseX >= dx - 22 && mouseX < x + entryW - 3 && mouseY >= ry - 1 && mouseY < ry + 9) {
            g.setTooltipForNextFrame(this.font, Component.translatable(soldier.isHungry() ? "gui.kingdomsatwar.recruitment.rations.hungry"
                    : "gui.kingdomsatwar.recruitment.rations", soldier.getRations()).append(soldier.isOnCampaign()
                    ? Component.translatable("gui.kingdomsatwar.recruitment.on_campaign") : Component.translatable(this.hasTown
                    ? "gui.kingdomsatwar.recruitment.in_garrison" : "gui.kingdomsatwar.recruitment.no_garrison")), mouseX, mouseY);
        }
    }
}
