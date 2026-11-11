package io.github.sirschrolli.kingdomsatwar.registry;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Die eigenen Zustände (seit 0.80).
 * <ul>
 *     <li><b>Zweites Leben:</b> Der Apfel der Unsterblichkeit hält einmal den Tod auf (siehe {@code relic.PowerApples}).</li>
 * </ul>
 */
public final class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, KingdomsAtWar.MODID);

    public static final DeferredHolder<MobEffect, MobEffect> SECOND_LIFE = EFFECTS.register("second_life",
            () -> new Simple(MobEffectCategory.BENEFICIAL, 0xFFD24A));

    private ModEffects() {}

    /** Ein Zustand ohne eigenes Verhalten (was er bewirkt, tun seine Merkmale oder die Ereignisse der Mod). */
    private static final class Simple extends MobEffect {
        Simple(MobEffectCategory category, int color) {
            super(category, color);
        }
    }
}
