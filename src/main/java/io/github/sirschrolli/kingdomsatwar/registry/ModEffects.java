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
 *     <li><b>Flugbann</b> (seit 0.82): Wen der Bannpfeil eines Bosses trifft, der kann nicht mehr gleiten – dafür tut der
 *     Sturz weniger weh (siehe {@code entity.boss.sky.FlightBan}).</li>
 * </ul>
 */
public final class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, KingdomsAtWar.MODID);

    public static final DeferredHolder<MobEffect, MobEffect> SECOND_LIFE = EFFECTS.register("second_life",
            () -> new Simple(MobEffectCategory.BENEFICIAL, 0xFFD24A));

    /**
     * Flugbann: Das Merkmal „Gleitflug“ ist abgeschaltet (Elytren und alle Schwingen tragen nicht), Milch hilft nicht; der
     * Sturz tut um 60 % weniger weh.
     */
    public static final DeferredHolder<MobEffect, MobEffect> FLIGHT_BAN = EFFECTS.register("flight_ban",
            () -> new Simple(MobEffectCategory.HARMFUL, 0x8FA0C8).addAttributeModifier(net.neoforged.neoforge.common.NeoForgeMod.GLIDING_FLIGHT,
                    KingdomsAtWar.id("effect.flight_ban"), -1.0, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

    private ModEffects() {}

    /** Ein Zustand ohne eigenes Verhalten (was er bewirkt, tun seine Merkmale oder die Ereignisse der Mod). */
    private static final class Simple extends MobEffect {
        Simple(MobEffectCategory category, int color) {
            super(category, color);
        }
    }
}
