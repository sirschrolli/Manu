package io.github.sirschrolli.kingdomsatwar.item;

import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.effect.MobEffectInstance;

/**
 * Rüstung aus Drachenschuppen. Wer alle vier Teile derselben Drachenart trägt, erhält die Kraft ihres Elements:
 * Feuer – Feuerresistenz, Eis – Schnelligkeit (und jedes Teil schützt vor Pulverschnee-Frost), Blitz – große Schnelligkeit,
 * Natur – Regeneration, Erde – Widerstand, Wasser – Wasseratmung und Delfinsgunst.
 */
public class DragonScaleArmorItem extends Item {
    private final DragonType element;

    public DragonScaleArmorItem(Properties properties, DragonType element) {
        super(properties);
        this.element = element;
    }

    public DragonType element() {
        return this.element;
    }

    /** Trägt der Spieler den vollständigen Satz einer Drachenart? Dann welche, sonst {@code null}. */
    public static DragonType fullSet(Player player) {
        DragonType found = null;
        for (EquipmentSlot slot : new EquipmentSlot[] {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            if (!(player.getItemBySlot(slot).getItem() instanceof DragonScaleArmorItem armor)) {
                return null;
            }
            if (found != null && found != armor.element) {
                return null;
            }
            found = armor.element;
        }
        return found;
    }

    /** Die Wirkung des vollständigen Satzes (wird jede Sekunde für 3 Sekunden erneuert). */
    public static void applySetBonus(Player player, DragonType element) {
        switch (element) {
            case FIRE -> effect(player, MobEffects.FIRE_RESISTANCE, 0);
            case ICE -> effect(player, MobEffects.SPEED, 0);       // Frost-Unempfindlichkeit über das Tag freeze_immune_wearables
            case LIGHTNING -> effect(player, MobEffects.SPEED, 1);
            case NATURE -> effect(player, MobEffects.REGENERATION, 0);
            case EARTH -> effect(player, MobEffects.RESISTANCE, 0);
            case WATER -> {
                effect(player, MobEffects.WATER_BREATHING, 0);
                effect(player, MobEffects.DOLPHINS_GRACE, 0);
            }
        }
    }

    private static void effect(Player player, Holder<MobEffect> effect, int amplifier) {
        MobEffectInstance current = player.getEffect(effect);
        if (current == null || current.getDuration() < 40 || current.getAmplifier() < amplifier) {
            player.addEffect(new MobEffectInstance(effect, 60 + (effect == MobEffects.REGENERATION ? 40 : 0), amplifier, true, false, true));
        }
    }
}
