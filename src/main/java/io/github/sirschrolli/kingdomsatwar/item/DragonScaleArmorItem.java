package io.github.sirschrolli.kingdomsatwar.item;

import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

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

    /**
     * Die Wirkung des vollständigen Satzes (jede Sekunde geprüft): still, ohne Symbol; legt man ihn ab, endet sie sofort.
     * {@code element} null: kein voller Satz. (Frost-Unempfindlichkeit über das Tag freeze_immune_wearables.)
     */
    public static void applySetBonus(Player player, @org.jspecify.annotations.Nullable DragonType element) {
        String source = "dragon_scale";
        io.github.sirschrolli.kingdomsatwar.item.GearEffects.set(player, MobEffects.FIRE_RESISTANCE, 0, source, element == DragonType.FIRE);
        io.github.sirschrolli.kingdomsatwar.item.GearEffects.set(player, MobEffects.SPEED, element == DragonType.LIGHTNING ? 1 : 0, source,
                element == DragonType.ICE || element == DragonType.LIGHTNING);
        io.github.sirschrolli.kingdomsatwar.item.GearEffects.set(player, MobEffects.REGENERATION, 0, source, element == DragonType.NATURE);
        io.github.sirschrolli.kingdomsatwar.item.GearEffects.set(player, MobEffects.RESISTANCE, 0, source, element == DragonType.EARTH);
        io.github.sirschrolli.kingdomsatwar.item.GearEffects.set(player, MobEffects.WATER_BREATHING, 0, source, element == DragonType.WATER);
        io.github.sirschrolli.kingdomsatwar.item.GearEffects.set(player, MobEffects.DOLPHINS_GRACE, 0, source, element == DragonType.WATER);
    }
}
