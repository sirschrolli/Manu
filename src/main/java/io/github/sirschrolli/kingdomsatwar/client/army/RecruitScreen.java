package io.github.sirschrolli.kingdomsatwar.client.army;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.army.Recruitment;
import io.github.sirschrolli.kingdomsatwar.client.renderer.SoldierRenderer;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.network.RecruitPayload;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jspecify.annotations.Nullable;

/**
 * Das Anwerbe-Fenster des Rekrutierungstischs: links die vier Soldatenarten, in der Mitte der ausgewählte
 * Soldat zum Anschauen (er salutiert, wenn man ihn auswählt), rechts Werte, Preis und der Knopf "Anwerben".
 */
public class RecruitScreen extends Screen {
    private static final Identifier BACKGROUND = KingdomsAtWar.id("textures/gui/recruitment_table.png");
    private static final Identifier BUTTON = Identifier.withDefaultNamespace("widget/button");
    private static final Identifier BUTTON_HIGHLIGHTED = Identifier.withDefaultNamespace("widget/button_highlighted");
    private static final Identifier SLOT = Identifier.withDefaultNamespace("container/slot");
    private static final Identifier HEART = Identifier.withDefaultNamespace("hud/heart/full");
    private static final Identifier ARMOR = Identifier.withDefaultNamespace("hud/armor_full");
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
    private static final int LABEL = 0xFF404040;
    private static final int MUTED = 0xFF6B6B6B;
    /** Kennungen der Vorschau-Soldaten (weit weg von echten Lebewesen). */
    private static final int PREVIEW_ID = -1_800_000_000;

    private final BlockPos tablePos;
    private final int armyLimit;
    private final Map<SoldierType, SoldierEntity> previews = new EnumMap<>(SoldierType.class);
    private SoldierType selected = SoldierType.SWORDSMAN;
    private int left;
    private int top;
    private @Nullable Button recruitButton;
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
        this.recruitButton = this.addRenderableWidget(Button.builder(Component.translatable("gui.kingdomsatwar.recruitment.recruit"),
                button -> this.recruit()).bounds(this.left + INFO_X, this.top + 149, 96, 20).build());
        this.updateButton();
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
        this.updateButton();
        // zu weit vom Tisch entfernt (oder Tisch abgebaut)? Dann schließen
        if (this.minecraft.player == null || this.minecraft.level == null
                || this.minecraft.player.distanceToSqr(Vec3.atCenterOf(this.tablePos)) > Recruitment.TABLE_RANGE * Recruitment.TABLE_RANGE
                || !(this.minecraft.level.getBlockState(this.tablePos).getBlock() instanceof io.github.sirschrolli.kingdomsatwar.block.RecruitmentTableBlock)) {
            this.onClose();
        }
    }

    private void updateButton() {
        if (this.recruitButton == null || this.minecraft.player == null) {
            return;
        }
        boolean affordable = Recruitment.canAfford(this.minecraft.player, this.selected);
        boolean room = this.armySize() < this.armyLimit;
        this.recruitButton.active = affordable && room;
        if (!room) {
            this.recruitButton.setTooltip(net.minecraft.client.gui.components.Tooltip.create(
                    Component.translatable("message.kingdomsatwar.recruit.army_full")));
        } else if (!affordable) {
            this.recruitButton.setTooltip(net.minecraft.client.gui.components.Tooltip.create(
                    Component.translatable("message.kingdomsatwar.recruit.not_enough")));
        } else {
            this.recruitButton.setTooltip(null);
        }
    }

    private void recruit() {
        ClientPacketDistributor.sendToServer(new RecruitPayload(this.tablePos, this.selected.ordinal()));
    }

    /** Antwort des Servers auf das Anwerben. */
    public void onResult(Recruitment.Result result) {
        this.feedback = Component.translatable("gui.kingdomsatwar.recruitment.result." + result.name().toLowerCase(java.util.Locale.ROOT));
        this.feedbackColor = result == Recruitment.Result.RECRUITED ? 0xFF2E7D32 : 0xFFB3261E;
        this.feedbackTicks = 60;
        if (result == Recruitment.Result.RECRUITED) {
            SoldierEntity preview = this.preview(this.selected);
            if (preview != null) {
                preview.handleEntityEvent(SoldierEntity.EVENT_CHEER);
            }
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
        for (SoldierType type : SoldierType.values()) {
            int x = this.left + LIST_X;
            int y = this.top + LIST_Y + type.ordinal() * ENTRY_H;
            if (event.x() >= x && event.x() < x + ENTRY_W && event.y() >= y && event.y() < y + ENTRY_H - 2) {
                if (this.selected != type) {
                    this.select(type);
                    this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
                }
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    /** Soldatenart auswählen; der Soldat im Bild salutiert. */
    public void select(SoldierType type) {
        this.selected = type;
        this.feedbackTicks = 0;
        SoldierEntity preview = this.preview(type);
        if (preview != null) {
            preview.handleEntityEvent(SoldierEntity.EVENT_SALUTE);
        }
        this.updateButton();
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
        // Kopfzeile
        g.text(this.font, this.title, x0 + 8, y0 + 6, LABEL, false);
        Component army = Component.translatable("gui.kingdomsatwar.recruitment.army", this.armySize(), this.armyLimit);
        g.text(this.font, army, x0 + WIDTH - 8 - this.font.width(army), y0 + 6, LABEL, false);

        for (SoldierType type : SoldierType.values()) {
            this.drawEntry(g, type, x0 + LIST_X, y0 + LIST_Y + type.ordinal() * ENTRY_H, mouseX, mouseY);
        }

        // ausgewählter Soldat
        SoldierEntity preview = this.preview(this.selected);
        if (preview != null) {
            InventoryScreen.extractEntityInInventoryFollowsMouse(g, x0 + PREVIEW_X + 1, y0 + PREVIEW_Y + 1, x0 + PREVIEW_X + PREVIEW_W - 1,
                    y0 + PREVIEW_Y + PREVIEW_H - 1, 34, 0.0625f, mouseX, mouseY, preview);
        }
        this.drawInfo(g, x0 + INFO_X, y0 + PREVIEW_Y, mouseX, mouseY);

        // Beschreibung unter dem Bild
        List<FormattedCharSequence> lines = this.font.split(Component.translatable(this.selected.descriptionKey()), WIDTH - PREVIEW_X - 10);
        for (int i = 0; i < Math.min(3, lines.size()); i++) {
            g.text(this.font, lines.get(i), x0 + PREVIEW_X, y0 + 119 + i * 10, LABEL, false);
        }
        if (this.feedback != null && this.feedbackTicks > 0) {
            g.text(this.font, this.feedback, x0 + PREVIEW_X, y0 + 155, this.feedbackColor, false);
        }
    }

    private void drawEntry(GuiGraphicsExtractor g, SoldierType type, int x, int y, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX < x + ENTRY_W && mouseY >= y && mouseY < y + ENTRY_H - 2;
        boolean isSelected = type == this.selected;
        g.blitSprite(RenderPipelines.GUI_TEXTURED, hovered || isSelected ? BUTTON_HIGHLIGHTED : BUTTON, x, y, ENTRY_W, ENTRY_H - 2);
        // Gesicht aus der Textur (Kopf + Helm)
        Identifier skin = SoldierRenderer.texture(type, 0);
        int fx = x + 5;
        int fy = y + 5;
        g.fill(fx - 1, fy - 1, fx + 25, fy + 25, 0xFF1E1E1E);
        g.blit(RenderPipelines.GUI_TEXTURED, skin, fx, fy, 8.0f, 8.0f, 24, 24, 8, 8, 128, 64);
        g.blit(RenderPipelines.GUI_TEXTURED, skin, fx, fy, 40.0f, 8.0f, 24, 24, 8, 8, 128, 64);
        int color = isSelected ? 0xFFFFE08A : 0xFFFFFFFF;
        g.text(this.font, Component.translatable(type.translationKey()), x + 34, y + 7, color, true);
        g.text(this.font, Component.translatable("soldier.kingdomsatwar." + type.id() + ".role"), x + 34, y + 19, 0xFFC8C8C8, true);
        if (isSelected) {
            g.text(this.font, "▶", x + ENTRY_W - 11, y + 13, 0xFFFFE08A, true);
        }
    }

    private void drawInfo(GuiGraphicsExtractor g, int x, int y, int mouseX, int mouseY) {
        SoldierType type = this.selected;
        g.text(this.font, Component.translatable(type.translationKey()).withStyle(ChatFormatting.BOLD), x, y + 2, LABEL, false);
        g.text(this.font, Component.translatable("soldier.kingdomsatwar." + type.id() + ".role"), x, y + 13, MUTED, false);

        // Werte
        int sy = y + 28;
        g.blitSprite(RenderPipelines.GUI_TEXTURED, HEART, x, sy, 9, 9);
        g.text(this.font, Component.translatable("gui.kingdomsatwar.recruitment.health", (int) type.health()), x + 12, sy + 1, LABEL, false);
        g.blitSprite(RenderPipelines.GUI_TEXTURED, ARMOR, x, sy + 12, 9, 9);
        g.text(this.font, Component.translatable("gui.kingdomsatwar.recruitment.armor", (int) type.armor()), x + 12, sy + 13, LABEL, false);
        g.pose().pushMatrix();
        g.pose().translate(x - 1, sy + 23);
        g.pose().scale(0.6875f, 0.6875f);
        g.item(new ItemStack(type.ranged() ? Items.BOW : Items.IRON_SWORD), 0, 0);
        g.pose().popMatrix();
        g.text(this.font, Component.translatable(type.ranged() ? "gui.kingdomsatwar.recruitment.ranged" : "gui.kingdomsatwar.recruitment.melee"),
                x + 12, sy + 25, LABEL, false);

        // Preis: der Sold in Münzen, dazu die nötige Ausrüstung
        int py = y + 68;
        g.text(this.font, Component.translatable("gui.kingdomsatwar.recruitment.cost"), x, py, LABEL, false);
        List<ItemStack> shown = new ArrayList<>(io.github.sirschrolli.kingdomsatwar.economy.Coins.stacks(type.coins()));
        int coinStacks = shown.size();
        for (SoldierType.Cost cost : type.cost()) {
            shown.add(cost.stack());
        }
        var player = this.minecraft.player;
        boolean creative = player != null && player.hasInfiniteMaterials();
        int purse = player == null ? 0 : io.github.sirschrolli.kingdomsatwar.economy.Coins.total(player);
        for (int i = 0; i < shown.size(); i++) {
            ItemStack stack = shown.get(i);
            int sx = x + i * 20;
            int slotY = py + 10;
            g.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT, sx, slotY, 18, 18);
            g.item(stack, sx + 1, slotY + 1);
            g.itemDecorations(this.font, stack, sx + 1, slotY + 1);
            boolean coin = i < coinStacks;
            int have = coin ? purse : player == null ? 0 : player.getInventory().countItem(stack.getItem());
            int need = coin ? type.coins() : type.cost().get(i - coinStacks).count();
            boolean enough = creative || have >= need;
            if (!enough) {
                g.fill(sx + 1, slotY + 1, sx + 17, slotY + 17, 0x55FF3030);
            }
            if (mouseX >= sx && mouseX < sx + 18 && mouseY >= slotY && mouseY < slotY + 18) {
                List<Component> tooltip = new ArrayList<>();
                if (coin) {
                    tooltip.add(Component.translatable("gui.kingdomsatwar.recruitment.pay",
                            io.github.sirschrolli.kingdomsatwar.economy.Coins.format(need)));
                    tooltip.add(Component.translatable("gui.kingdomsatwar.recruitment.purse",
                            io.github.sirschrolli.kingdomsatwar.economy.Coins.format(have)).withStyle(enough ? ChatFormatting.GREEN : ChatFormatting.RED));
                } else {
                    tooltip.add(Component.literal(need + "× ").append(stack.getHoverName()));
                    tooltip.add(Component.translatable("gui.kingdomsatwar.recruitment.have", have)
                            .withStyle(enough ? ChatFormatting.GREEN : ChatFormatting.RED));
                }
                g.setTooltipForNextFrame(this.font, tooltip, java.util.Optional.empty(), stack, mouseX, mouseY);
            }
        }
    }

    @Override
    public void removed() {
        this.previews.clear();
        super.removed();
    }
}
