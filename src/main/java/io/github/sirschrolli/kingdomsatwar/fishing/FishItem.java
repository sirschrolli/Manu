package io.github.sirschrolli.kingdomsatwar.fishing;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/**
 * Ein gefangener Fisch. Roh gegessen wirkt jede Art anders (die Wirkung steht am Gegenstand); gebraten wird aus allen
 * dasselbe sättigende Fischfilet – ohne Wirkung.
 */
public class FishItem extends Item {
    private final FishSpecies species;

    public FishItem(FishSpecies species, Item.Properties properties) {
        super(properties);
        this.species = species;
    }

    public FishSpecies species() {
        return this.species;
    }

    /** Legenden funkeln (seit 0.90). */
    @Override
    public boolean isFoil(ItemStack stack) {
        return this.species.rarity() == FishSpecies.Rarity.LEGENDARY || super.isFoil(stack);
    }

    /** Der Name einer Legende steht in Gold (die Seltenheiten von Minecraft kennen kein Gold). */
    @Override
    public Component getName(ItemStack stack) {
        Component name = super.getName(stack);
        return this.species.rarity() == FishSpecies.Rarity.LEGENDARY ? name.copy().withStyle(ChatFormatting.GOLD) : name;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        if (isDisplay(stack)) {
            // ein Schaustück mit eigenem Namen (im Handelsfenster steht es für „beliebige Fische dieser Seltenheit“)
            return;
        }
        builder.accept(Component.translatable(this.species.rarity().key()).withColor(this.species.rarity().color)
                .append(Component.literal(" · " + this.species.minCm() + "–" + this.species.maxCm() + " cm").withStyle(ChatFormatting.GRAY)));
        String biome = this.species.biome();
        if (biome != null) {
            // die Arten der Biome: wo sie zu Hause sind
            int colon = biome.indexOf(':');
            builder.accept(Component.translatable("item.kingdomsatwar.fish.home",
                    Component.translatable("biome." + biome.substring(0, colon) + "." + biome.substring(colon + 1))).withStyle(ChatFormatting.DARK_AQUA));
        }
        tooltip(this.species, builder);
    }

    /**
     * Ein Schaustück: Es trägt einen anderen Namen als sein Gegenstand (so zeigt das Handelsfenster Sammelangebote wie
     * „beliebige gewöhnliche Fische“ oder die Erweiterung des Fischbeutels).
     */
    public static boolean isDisplay(ItemStack stack) {
        return !java.util.Objects.equals(stack.get(net.minecraft.core.component.DataComponents.ITEM_NAME),
                stack.getItem().components().get(net.minecraft.core.component.DataComponents.ITEM_NAME));
    }

    /** Eine Zeile der Wirkung beim Essen: {@code kind} 0 = nützt, 1 = schadet, 2 = Besonderes. */
    public record Effect(Component text, int kind) {}

    /** Die Wirkung beim Essen, Zeile für Zeile (für den Gegenstand und das Fischbuch). */
    public static List<Effect> effects(FishSpecies species) {
        List<Effect> lines = new ArrayList<>();
        if (species.special() != FishSpecies.Special.NONE) {
            lines.add(new Effect(Component.translatable("fishing.kingdomsatwar.special." + species.special().name().toLowerCase(java.util.Locale.ROOT)), 2));
        }
        for (FishSpecies.Bite bite : species.bites()) {
            MutableComponent line = Component.translatable(bite.effect().value().getDescriptionId());
            if (bite.amplifier() > 0) {
                line.append(" ").append(Component.translatable("potion.potency." + bite.amplifier()));
            }
            if (bite.seconds() > 0) {
                line.append(" (" + MobEffectUtil.formatDuration(new MobEffectInstance(bite.effect(), bite.seconds() * 20), 1.0f, 20.0f).getString() + ")");
            }
            lines.add(new Effect(line, bite.effect().value().isBeneficial() ? 0 : 1));
        }
        return lines;
    }

    /** Die Wirkung beim Essen, wie sie am Gegenstand steht. */
    public static void tooltip(FishSpecies species, Consumer<Component> builder) {
        List<Effect> effects = effects(species);
        if (effects.isEmpty()) {
            return;
        }
        builder.accept(Component.translatable("fishing.kingdomsatwar.eaten").withStyle(ChatFormatting.DARK_PURPLE));
        for (Effect effect : effects) {
            builder.accept(Component.literal(" ").append(effect.text())
                    .withStyle(effect.kind() == 0 ? ChatFormatting.BLUE : effect.kind() == 1 ? ChatFormatting.RED : ChatFormatting.LIGHT_PURPLE));
        }
    }
}
