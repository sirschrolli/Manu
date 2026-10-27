package io.github.sirschrolli.kingdomsatwar.entity.boss;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

/**
 * Wirkungen der Elemente auf getroffene Gegner: Feuer setzt in Brand, Eis lässt erstarren (Frostschauer wie im
 * Pulverschnee, dazu langsam), Natur vergiftet, Blitz betäubt kurz, Wasser macht schwerfällig.
 */
public final class BossEffects {
    private BossEffects() {}

    public static void apply(LivingEntity target, BossProjectile.Kind kind, int seconds) {
        if (seconds <= 0) {
            return;
        }
        switch (kind) {
            case FIREBALL, METEOR -> burn(target, seconds);
            case ICE_SPEAR, ICICLE -> freeze(target, seconds);
            case THORN, SEED -> poison(target, seconds);
            case SPARK -> stun(target, seconds);
            case BUBBLE -> soak(target, seconds);
            case SOUL_BOLT -> wither(target, seconds);
            case BOULDER -> {
            }
        }
    }

    /**
     * Eine Schwächung durch einen Boss: mit Zeichen in der Anzeige, aber ohne Wirbel-Teilchen – die stiegen dem
     * Getroffenen sonst mitten im Kampf vor die Augen.
     */
    public static MobEffectInstance debuff(Holder<MobEffect> effect, int duration, int amplifier) {
        return new MobEffectInstance(effect, duration, amplifier, false, false, true);
    }

    public static void burn(LivingEntity target, int seconds) {
        target.igniteForSeconds(seconds);
    }

    /** Erstarren: Frostschauer (wie im Pulverschnee, nimmt langsam wieder ab) und verlangsamt. */
    public static void freeze(LivingEntity target, int seconds) {
        int frozen = target.getTicksRequiredToFreeze() + seconds * 40;
        if (target.getTicksFrozen() < frozen) {
            target.setTicksFrozen(frozen);
        }
        target.addEffect(debuff(MobEffects.SLOWNESS, seconds * 20, 2));
    }

    public static void poison(LivingEntity target, int seconds) {
        target.addEffect(debuff(MobEffects.POISON, seconds * 20, 1));
        target.addEffect(debuff(MobEffects.NAUSEA, Math.min(seconds, 4) * 20, 0));
    }

    /** Betäubt: kaum noch Bewegung und schwache Schläge. */
    public static void stun(LivingEntity target, int seconds) {
        target.addEffect(debuff(MobEffects.SLOWNESS, seconds * 20, 5));
        target.addEffect(debuff(MobEffects.MINING_FATIGUE, seconds * 20, 2));
        target.addEffect(debuff(MobEffects.WEAKNESS, seconds * 20, 1));
    }

    /** Schatten zehren: Verdorren und kurz verdunkelte Sicht. */
    public static void wither(LivingEntity target, int seconds) {
        target.addEffect(debuff(MobEffects.WITHER, seconds * 20, 0));
        target.addEffect(debuff(MobEffects.DARKNESS, seconds * 20 + 40, 0));
    }

    public static void soak(LivingEntity target, int seconds) {
        target.addEffect(debuff(MobEffects.SLOWNESS, seconds * 20, 1));
    }
}
