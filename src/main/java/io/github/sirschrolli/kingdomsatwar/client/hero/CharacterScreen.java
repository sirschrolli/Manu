package io.github.sirschrolli.kingdomsatwar.client.hero;

import io.github.sirschrolli.kingdomsatwar.economy.Coins;
import io.github.sirschrolli.kingdomsatwar.hero.Crystals;
import io.github.sirschrolli.kingdomsatwar.hero.HeroLevel;
import io.github.sirschrolli.kingdomsatwar.magic.Mana;
import io.github.sirschrolli.kingdomsatwar.network.HeroTalentPayload;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jspecify.annotations.Nullable;

/**
 * Das Charakterfenster (Taste K): links der Held selbst mit seinen Werten (Leben, Mana, Rüstung, Schaden) und was er
 * gefunden hat (Herz- und Manakristalle, Biome), rechts die Heldenstufe mit der Leiste der Heldenerfahrung und die sechs
 * Talente – je Talent zehn Kästchen, daneben was die Punkte bringen und ein Knopf für den nächsten Punkt. Unten:
 * alle Punkte neu verteilen (gegen Silber) und Fertig.
 */
public class CharacterScreen extends Screen {
    private static final int WIDTH = 276;
    private static final int HEIGHT = 222;
    private static final int LEFT_W = 82;
    private static final int RIGHT_X = 92;
    private static final int ROW_Y = 64;
    private static final int ROW_H = 21;
    private static final int LABEL = 0xFF404040;
    private static final int MUTED = 0xFF6B6B6B;
    private static final int GOLD = 0xFF8A6A10;
    private static final Identifier HEART = Identifier.withDefaultNamespace("hud/heart/full");
    private static final Identifier ARMOR = Identifier.withDefaultNamespace("hud/armor_full");

    private int left;
    private int top;
    private final List<Button> plus = new ArrayList<>();
    private @Nullable Button reset;

    public CharacterScreen() {
        super(Component.translatable("gui.kingdomsatwar.character.title"));
    }

    @Override
    protected void init() {
        this.left = (this.width - WIDTH) / 2;
        this.top = (this.height - HEIGHT) / 2;
        this.plus.clear();
        for (HeroLevel.Talent talent : HeroLevel.Talent.values()) {
            int y = this.top + ROW_Y + talent.ordinal() * ROW_H + 2;
            this.plus.add(this.addRenderableWidget(Button.builder(Component.literal("+"),
                    button -> ClientPacketDistributor.sendToServer(new HeroTalentPayload(talent.ordinal())))
                    .bounds(this.left + WIDTH - 24, y, 16, 16).build()));
        }
        this.reset = this.addRenderableWidget(Button.builder(Component.translatable("gui.kingdomsatwar.character.reset"),
                button -> ClientPacketDistributor.sendToServer(new HeroTalentPayload(HeroTalentPayload.RESET)))
                .bounds(this.left + RIGHT_X, this.top + HEIGHT - 26, 112, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.kingdomsatwar.character.done"), button -> this.onClose())
                .bounds(this.left + WIDTH - 8 - 60, this.top + HEIGHT - 26, 60, 20).build());
        this.updateButtons();
    }

    @Override
    public void tick() {
        this.updateButtons();
    }

    private void updateButtons() {
        Player player = this.minecraft.player;
        if (player == null || this.reset == null) {
            return;
        }
        HeroLevel.Hero hero = HeroLevel.of(player);
        for (HeroLevel.Talent talent : HeroLevel.Talent.values()) {
            Button button = this.plus.get(talent.ordinal());
            boolean full = hero.points(talent) >= HeroLevel.MAX_TALENT;
            button.active = hero.unspent() > 0 && !full;
            button.setTooltip(Tooltip.create(Component.translatable(talent.translationKey()).withStyle(ChatFormatting.GOLD).append("\n")
                    .append(Component.translatable(talent.translationKey() + ".desc").withStyle(ChatFormatting.GRAY)).append("\n\n")
                    .append(Component.translatable(full ? "gui.kingdomsatwar.character.talent.full" : hero.unspent() > 0
                            ? "gui.kingdomsatwar.character.talent.spend" : "gui.kingdomsatwar.character.talent.no_points"))));
        }
        int cost = HeroLevel.resetCost(hero);
        boolean affordable = player.hasInfiniteMaterials() || Coins.canAfford(player, cost);
        this.reset.active = hero.spent() > 0 && affordable;
        this.reset.setTooltip(Tooltip.create(hero.spent() == 0 ? Component.translatable("gui.kingdomsatwar.character.reset.none")
                : Component.translatable("gui.kingdomsatwar.character.reset.hint", Coins.format(cost))
                        .withStyle(affordable ? ChatFormatting.WHITE : ChatFormatting.RED)));
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (this.minecraft.options.keyInventory.matches(event)
                || io.github.sirschrolli.kingdomsatwar.client.ModKeyMappings.CHARACTER.matches(event)) {
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

    // ------------------------------------------------------------------------------------------------ Zeichnen

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
        super.extractBackground(g, mouseX, mouseY, a);
        panel(g, this.left, this.top, WIDTH, HEIGHT);
        // links: der Held in seinem Rahmen
        inset(g, this.left + 8, this.top + 8, LEFT_W - 6, 92);
        // die Trennlinie zwischen den Spalten und über den Knöpfen
        g.fill(this.left + RIGHT_X - 6, this.top + 8, this.left + RIGHT_X - 5, this.top + HEIGHT - 32, 0xFF8B8B8B);
        g.fill(this.left + RIGHT_X - 5, this.top + 8, this.left + RIGHT_X - 4, this.top + HEIGHT - 32, 0xFFFFFFFF);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
        super.extractRenderState(g, mouseX, mouseY, a);
        Player player = this.minecraft.player;
        if (player == null) {
            return;
        }
        HeroLevel.Hero hero = HeroLevel.of(player);
        int x0 = this.left;
        int y0 = this.top;
        InventoryScreen.extractEntityInInventoryFollowsMouse(g, x0 + 9, y0 + 9, x0 + 8 + LEFT_W - 7, y0 + 99, 38, 0.0625f, mouseX, mouseY, player);
        this.drawStats(g, player, x0 + 8, y0 + 106, mouseX, mouseY);

        // Kopf: Heldenstufe und Heldenerfahrung
        int rx = x0 + RIGHT_X;
        int rw = WIDTH - RIGHT_X - 8;
        int level = hero.level();
        g.text(this.font, Component.translatable("gui.kingdomsatwar.character.level", level).withStyle(ChatFormatting.BOLD), rx, y0 + 9, GOLD,
                false);
        Component name = player.getName();
        g.text(this.font, name, rx + rw - this.font.width(name), y0 + 9, MUTED, false);
        this.drawExperience(g, hero, rx, y0 + 22, rw, mouseX, mouseY);
        int unspent = hero.unspent();
        Component points = unspent > 0 ? Component.translatable("gui.kingdomsatwar.character.points", unspent)
                : Component.translatable("gui.kingdomsatwar.character.points.none");
        g.text(this.font, points, rx, y0 + 48, unspent > 0 ? 0xFF2F7A1F : MUTED, false);
        if (unspent > 0) {
            // ein kleiner Stern, der sanft pulsiert, solange Punkte frei sind
            float pulse = 0.5f + 0.5f * Mth.sin(System.currentTimeMillis() / 220.0f);
            g.text(this.font, "★", rx + this.font.width(points) + 3, y0 + 48, net.minecraft.util.ARGB.color(Math.round(150 + 105 * pulse), 224, 169, 58),
                    false);
        }

        // die sechs Talente
        for (HeroLevel.Talent talent : HeroLevel.Talent.values()) {
            this.drawTalent(g, hero, talent, rx, y0 + ROW_Y + talent.ordinal() * ROW_H, rw - 22, mouseX, mouseY);
        }
    }

    /** Leiste der Heldenerfahrung mit Zahlen darunter; darüber sagt ein Tooltip, wofür es Heldenerfahrung gibt. */
    private void drawExperience(GuiGraphicsExtractor g, HeroLevel.Hero hero, int x, int y, int width, int mouseX, int mouseY) {
        float progress = HeroLevel.progress(hero.experience());
        inset(g, x, y, width, 9);
        int fill = Math.round((width - 2) * progress);
        if (fill > 0) {
            g.fill(x + 1, y + 1, x + 1 + fill, y + 8, 0xFFD89A2B);
            g.fill(x + 1, y + 1, x + 1 + fill, y + 3, 0xFFF6D27A);
            g.fill(x + 1, y + 6, x + 1 + fill, y + 8, 0xFFB0761A);
            // ein heller Schimmer wandert durch die Füllung
            int sx = x + 1 + (int) ((System.currentTimeMillis() / 12) % (width + 20)) - 10;
            int from = Math.max(x + 1, sx);
            int to = Math.min(x + 1 + fill, sx + 6);
            if (to > from) {
                g.fill(from, y + 1, to, y + 8, 0x50FFFFFF);
            }
        }
        boolean max = hero.level() >= HeroLevel.MAX_LEVEL;
        Component numbers = max ? Component.translatable("gui.kingdomsatwar.character.experience.max", hero.experience())
                : Component.translatable("gui.kingdomsatwar.character.experience", hero.experience(), HeroLevel.total(hero.level() + 1));
        g.text(this.font, numbers, x + width - this.font.width(numbers), y + 12, MUTED, false);
        if (mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + 22) {
            g.setTooltipForNextFrame(this.font, this.lines(List.of(Component.translatable("gui.kingdomsatwar.character.sources.title")
                    .withStyle(ChatFormatting.GOLD), Component.translatable("gui.kingdomsatwar.character.sources").withStyle(ChatFormatting.GRAY))),
                    mouseX, mouseY);
        }
    }

    /** Ein Talent: Bild, Name, was die Punkte bringen, darunter zehn Kästchen. */
    private void drawTalent(GuiGraphicsExtractor g, HeroLevel.Hero hero, HeroLevel.Talent talent, int x, int y, int width, int mouseX, int mouseY) {
        int points = hero.points(talent);
        if (talent.ordinal() % 2 == 0) {
            g.fill(x - 2, y, x + width + 2, y + ROW_H - 1, 0x18000000);
        }
        g.item(talent.icon(), x, y + 2);
        int nx = x + 20;
        g.text(this.font, Component.translatable(talent.translationKey()), nx, y + 2, points > 0 ? LABEL : MUTED, false);
        Component effect = effect(talent, points);
        g.text(this.font, effect, x + width - this.font.width(effect), y + 2, points > 0 ? 0xFF2F7A1F : MUTED, false);
        for (int i = 0; i < HeroLevel.MAX_TALENT; i++) {
            int px = nx + i * 7;
            int py = y + 13;
            g.fill(px, py, px + 6, py + 4, 0xFF373737);
            if (i < points) {
                g.fill(px, py, px + 6, py + 4, 0xFFD89A2B);
                g.fill(px, py, px + 6, py + 1, 0xFFF6D27A);
            } else {
                g.fill(px + 1, py + 1, px + 6, py + 4, 0xFF8B8B8B);
            }
        }
        if (mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + ROW_H - 1) {
            g.setTooltipForNextFrame(this.font, this.lines(List.of(Component.translatable(talent.translationKey()).withStyle(ChatFormatting.GOLD)
                    .append(Component.literal("  " + points + "/" + HeroLevel.MAX_TALENT).withStyle(ChatFormatting.YELLOW)),
                    Component.translatable(talent.translationKey() + ".desc").withStyle(ChatFormatting.GRAY))), mouseX, mouseY);
        }
    }

    /** Was die Punkte eines Talents zusammen bringen ("+1,5 Schaden"). */
    static Component effect(HeroLevel.Talent talent, int points) {
        return switch (talent) {
            case STRENGTH -> Component.translatable("hero.kingdomsatwar.talent.strength.effect", number(0.5 * points));
            case VITALITY -> Component.translatable("hero.kingdomsatwar.talent.vitality.effect", number(points));
            case AGILITY -> Component.translatable("hero.kingdomsatwar.talent.agility.effect", number(2 * points));
            case TOUGHNESS -> Component.translatable("hero.kingdomsatwar.talent.toughness.effect", number(0.5 * points));
            case ARCANA -> Component.translatable("hero.kingdomsatwar.talent.arcana.effect", number(10 * points));
            case FORTUNE -> Component.translatable("hero.kingdomsatwar.talent.fortune.effect", number(0.25 * points));
        };
    }

    /** Die Werte des Helden mit kleinen Bildern, darunter die Kristalle und Biome. */
    private void drawStats(GuiGraphicsExtractor g, Player player, int x, int y, int mouseX, int mouseY) {
        int line = 11;
        g.blitSprite(RenderPipelines.GUI_TEXTURED, HEART, x, y, 9, 9);
        g.text(this.font, number(player.getMaxHealth() / 2.0), x + 12, y + 1, LABEL, false);
        g.pose().pushMatrix();
        g.pose().translate(x - 1, y + line - 1);
        g.pose().scale(0.6875f, 0.6875f);
        g.item(new ItemStack(ModItems.MANA_CRYSTAL.get()), 0, 0);
        g.pose().popMatrix();
        g.text(this.font, String.valueOf(Math.round(Mana.maxFor(player))), x + 12, y + line + 1, LABEL, false);
        g.blitSprite(RenderPipelines.GUI_TEXTURED, ARMOR, x + 40, y, 9, 9);
        g.text(this.font, number(player.getAttributeValue(Attributes.ARMOR)), x + 52, y + 1, LABEL, false);
        g.pose().pushMatrix();
        g.pose().translate(x + 39, y + line - 1);
        g.pose().scale(0.6875f, 0.6875f);
        g.item(new ItemStack(Items.IRON_SWORD), 0, 0);
        g.pose().popMatrix();
        g.text(this.font, number(player.getAttributeValue(Attributes.ATTACK_DAMAGE)), x + 52, y + line + 1, LABEL, false);
        if (mouseX >= x && mouseX < x + LEFT_W - 6 && mouseY >= y && mouseY < y + 2 * line) {
            g.setTooltipForNextFrame(this.font, this.lines(List.of(Component.translatable("gui.kingdomsatwar.character.stats")
                    .withStyle(ChatFormatting.GOLD), Component.translatable("gui.kingdomsatwar.character.stats.hint").withStyle(ChatFormatting.GRAY))),
                    mouseX, mouseY);
        }
        Crystals.Count crystals = Crystals.of(player);
        HeroLevel.Hero hero = HeroLevel.of(player);
        int cy = y + 2 * line + 6;
        g.text(this.font, Component.translatable("gui.kingdomsatwar.character.hearts", crystals.hearts(), Crystals.MAX), x, cy, MUTED, false);
        g.text(this.font, Component.translatable("gui.kingdomsatwar.character.mana", crystals.mana(), Crystals.MAX), x, cy + 10, MUTED, false);
        g.text(this.font, Component.translatable("gui.kingdomsatwar.character.biomes", hero.discoveries()), x, cy + 20, MUTED, false);
    }

    private List<FormattedCharSequence> lines(List<Component> components) {
        return components.stream().flatMap(c -> this.font.split(c, 220).stream()).toList();
    }

    /** Eine Zahl mit höchstens einer Nachkommastelle, im Deutschen mit Komma. */
    static String number(double value) {
        String text = Math.abs(value - Math.round(value)) < 0.05 ? String.valueOf(Math.round(value)) : String.format(Locale.ROOT, "%.1f", value);
        String language = net.minecraft.client.Minecraft.getInstance().options.languageCode;
        return language.startsWith("de") ? text.replace('.', ',') : text;
    }

    /** Ein Feld im Stil der Vanilla-Fenster: heller Rand oben links, dunkler unten rechts, abgerundete Ecken. */
    static void panel(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        g.fill(x + 2, y, x + w - 2, y + 1, 0xFF000000);
        g.fill(x + 2, y + h - 1, x + w - 2, y + h, 0xFF000000);
        g.fill(x, y + 2, x + 1, y + h - 2, 0xFF000000);
        g.fill(x + w - 1, y + 2, x + w, y + h - 2, 0xFF000000);
        g.fill(x + 1, y + 1, x + 2, y + 2, 0xFF000000);
        g.fill(x + w - 2, y + 1, x + w - 1, y + 2, 0xFF000000);
        g.fill(x + 1, y + h - 2, x + 2, y + h - 1, 0xFF000000);
        g.fill(x + w - 2, y + h - 2, x + w - 1, y + h - 1, 0xFF000000);
        g.fill(x + 1, y + 2, x + w - 1, y + h - 2, 0xFFC6C6C6);
        g.fill(x + 2, y + 1, x + w - 2, y + h - 1, 0xFFC6C6C6);
        g.fill(x + 2, y + 1, x + w - 3, y + 3, 0xFFFFFFFF);
        g.fill(x + 1, y + 2, x + 3, y + h - 3, 0xFFFFFFFF);
        g.fill(x + 3, y + h - 3, x + w - 2, y + h - 1, 0xFF555555);
        g.fill(x + w - 3, y + 3, x + w - 1, y + h - 2, 0xFF555555);
    }

    /** Ein vertieftes Feld (wie ein Platz im Inventar). */
    static void inset(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + h, 0xFF8B8B8B);
        g.fill(x, y, x + w - 1, y + 1, 0xFF373737);
        g.fill(x, y, x + 1, y + h - 1, 0xFF373737);
        g.fill(x + 1, y + h - 1, x + w, y + h, 0xFFFFFFFF);
        g.fill(x + w - 1, y + 1, x + w, y + h, 0xFFFFFFFF);
    }
}
