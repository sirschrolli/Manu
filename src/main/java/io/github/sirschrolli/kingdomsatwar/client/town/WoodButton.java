package io.github.sirschrolli.kingdomsatwar.client.town;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Ein Holzknopf als echtes Bedienelement (Tooltip, Tastatur, Vorlesen) im Stil der Pergament-Fenster – siehe
 * {@link Parchment#woodButton}. Optional mit einem kleinen Bild vor der Schrift.
 */
public class WoodButton extends Button {
    /** Die Schrift, wie sie gesetzt wurde (das Spiel färbt die Nachricht abgeschalteter Knöpfe sonst grau ein). */
    private Component label;
    private @Nullable ItemStack icon;
    /** Betrag hinter der Schrift (als kleine Münzen), -1: keiner. */
    private int price = -1;

    public WoodButton(int x, int y, int width, int height, Component label, OnPress onPress) {
        super(x, y, width, height, label, onPress, DEFAULT_NARRATION);
        this.label = label;
    }

    /** Ein kleines Bild links vor der Schrift ({@code null}: keins). */
    public WoodButton icon(@Nullable ItemStack icon) {
        this.icon = icon;
        return this;
    }

    /** Ein Betrag hinter der Schrift, als kleine Münzen ({@code -1}: keiner). */
    public WoodButton price(int coins) {
        this.price = coins;
        return this;
    }

    @Override
    public void setMessage(Component message) {
        super.setMessage(message);
        this.label = message;
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
        Minecraft minecraft = Minecraft.getInstance();
        // nach einem Mausklick bleibt der Knopf "fokussiert" – hervorgehoben wird er dann nur unter der Maus
        boolean hover = this.isHovered() || this.isFocused() && minecraft.getLastInputType().isKeyboard();
        Parchment.woodButton(g, minecraft.font, this.getX(), this.getY(), this.getWidth(), this.getHeight(), this.label, this.active, hover,
                this.icon, this.price);
    }
}
