package io.github.sirschrolli.kingdomsatwar.rules;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

/**
 * Verzauberungen auf den Waffen der Mod (seit 0.79). Schärfe, Bann und Nemesis geben bei Minecraft einen festen Betrag
 * dazu – Schärfe V drei Punkte, gleich ob die Waffe 7 oder 22 Schaden macht. Auf einem Diamantschwert sind das drei
 * Siebtel mehr, auf dem Speer des Schöpfers kaum zu spüren, und die Fähigkeiten der Waffen (Lichtspeer, Lichtsäule, Sterne,
 * Weltenbrocken, der Bann des Bannhammers) rührte es gar nicht an. Darum:
 * <ul>
 *     <li><b>Im Nahkampf</b> wirkt eine Verzauberung auf Waffen der Mod, die stärker sind als ein Diamantschwert, im selben
 *     Verhältnis wie dort: Der Betrag wächst mit dem Schaden der Waffe ({@link #scale}).</li>
 *     <li><b>Fähigkeiten</b> der Waffen werden um denselben Anteil stärker, den ein Diamantschwert durch die Verzauberung
 *     gewinnt ({@link #ability}) – Schärfe V also um drei Siebtel, Bann V gegen Untote auf fast das Dreifache.</li>
 * </ul>
 * Waffen von Minecraft und anderen Mods bleiben, wie sie sind.
 */
public final class WeaponPower {
    /** Der Schlag eines Diamantschwerts in der Hand: das Maß, an dem die Verzauberungen von Minecraft ausgerichtet sind. */
    public static final float REFERENCE = 7.0f;

    private WeaponPower() {}

    /** Eine Waffe der Mod? */
    public static boolean ours(ItemStack stack) {
        return !stack.isEmpty() && KingdomsAtWar.MODID.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace());
    }

    /** Der Schlag mit dieser Waffe in der Haupthand, ohne alles Weitere (1 für die Faust plus das, was die Waffe dazugibt). */
    public static float baseDamage(ItemStack stack) {
        ItemAttributeModifiers modifiers = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        double damage = 1.0;
        for (ItemAttributeModifiers.Entry entry : modifiers.modifiers()) {
            if (entry.attribute().is(Attributes.ATTACK_DAMAGE) && entry.modifier().operation() == AttributeModifier.Operation.ADD_VALUE
                    && (entry.slot() == EquipmentSlotGroup.MAINHAND || entry.slot() == EquipmentSlotGroup.HAND || entry.slot() == EquipmentSlotGroup.ANY)) {
                damage += entry.modifier().amount();
            }
        }
        return (float) damage;
    }

    /** Um wie viel stärker als auf einem Diamantschwert eine Verzauberung auf dieser Waffe wirkt (mindestens 1). */
    public static float scale(ItemStack stack) {
        return ours(stack) ? Math.max(1.0f, baseDamage(stack) / REFERENCE) : 1.0f;
    }

    /**
     * Nahkampf: {@code enchanted} ist der Schaden, wie Minecraft ihn mit den Verzauberungen errechnet hat. Was sie
     * dazugegeben haben, wächst mit der Stärke der Waffe.
     */
    public static float melee(ItemStack weapon, float base, float enchanted) {
        float bonus = enchanted - base;
        return bonus > 0.0f ? base + bonus * scale(weapon) : enchanted;
    }

    /**
     * Der Schaden einer Fähigkeit der Waffe gegen dieses Ziel: wächst durch Verzauberungen um denselben Anteil wie der
     * Schlag eines Diamantschwerts.
     */
    public static float ability(ServerLevel level, ItemStack weapon, Entity victim, DamageSource source, float base) {
        if (weapon.isEmpty() || !weapon.isEnchanted()) {
            return base;
        }
        float bonus = EnchantmentHelper.modifyDamage(level, weapon, victim, source, REFERENCE) - REFERENCE;
        return bonus > 0.0f ? base * (1.0f + bonus / REFERENCE) : base;
    }
}
