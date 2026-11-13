package io.github.sirschrolli.kingdomsatwar.item.armory;

import io.github.sirschrolli.kingdomsatwar.magic.Spell;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;

/** Gemeinsames der Waffen aus der Waffenkammer: volle Schläge, Gegner erkennen, Nebentreffer austeilen. */
public final class ArmoryCombat {
    private ArmoryCombat() {}

    /**
     * Ein voller Schlag: Spieler müssen ausgeholt haben (wie beim Schwung des Schwerts); Mobs schlagen immer voll zu. Gilt in
     * {@code hurtEnemy}, denn Minecraft setzt das Ausholen erst nach dem Treffer zurück.
     */
    public static boolean fullSwing(LivingEntity attacker) {
        return !(attacker instanceof Player player) || player.getAttackStrengthScale(0.5f) > 0.9f;
    }

    /** Wer getroffen werden darf: lebt, ist kein Verbündeter (eigene Tiere, Drachen, Soldaten, Team) und kein Markierungsständer. */
    public static boolean isEnemy(LivingEntity attacker, Entity entity) {
        return entity instanceof LivingEntity living && living != attacker && living.isAlive() && living.isPickable()
                && !living.isSpectator() && !(living instanceof ArmorStand stand && stand.isMarker()) && !Spell.isAlly(attacker, living);
    }

    /** Der Schaden eines Nebentreffers: ein Anteil des Angriffsschadens (mit der Waffe in der Hand). */
    public static float share(LivingEntity attacker, float factor) {
        return (float) attacker.getAttributeValue(Attributes.ATTACK_DAMAGE) * factor;
    }

    public static DamageSource source(ServerLevel level, LivingEntity attacker) {
        return attacker instanceof Player player ? level.damageSources().playerAttack(player) : level.damageSources().mobAttack(attacker);
    }
}
