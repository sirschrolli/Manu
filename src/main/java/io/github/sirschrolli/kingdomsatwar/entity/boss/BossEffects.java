package io.github.sirschrolli.kingdomsatwar.entity.boss;

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

    public static void burn(LivingEntity target, int seconds) {
        target.igniteForSeconds(seconds);
    }

    /** Erstarren: Frostschauer (wie im Pulverschnee, nimmt langsam wieder ab) und verlangsamt. */
    public static void freeze(LivingEntity target, int seconds) {
        int frozen = target.getTicksRequiredToFreeze() + seconds * 40;
        if (target.getTicksFrozen() < frozen) {
            target.setTicksFrozen(frozen);
        }
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, seconds * 20, 2));
    }

    public static void poison(LivingEntity target, int seconds) {
        target.addEffect(new MobEffectInstance(MobEffects.POISON, seconds * 20, 1));
        target.addEffect(new MobEffectInstance(MobEffects.NAUSEA, Math.min(seconds, 4) * 20, 0));
    }

    /** Betäubt: kaum noch Bewegung und schwache Schläge. */
    public static void stun(LivingEntity target, int seconds) {
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, seconds * 20, 5));
        target.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, seconds * 20, 2));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, seconds * 20, 1));
    }

    /** Schatten zehren: Verdorren und kurz verdunkelte Sicht. */
    public static void wither(LivingEntity target, int seconds) {
        target.addEffect(new MobEffectInstance(MobEffects.WITHER, seconds * 20, 0));
        target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, seconds * 20 + 40, 0));
    }

    public static void soak(LivingEntity target, int seconds) {
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, seconds * 20, 1));
    }
}
