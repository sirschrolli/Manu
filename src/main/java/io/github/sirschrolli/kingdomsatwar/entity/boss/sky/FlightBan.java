package io.github.sirschrolli.kingdomsatwar.entity.boss.sky;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.creator.CreatorWings;
import io.github.sirschrolli.kingdomsatwar.end.StarWings;
import io.github.sirschrolli.kingdomsatwar.fae.FaeElytra;
import io.github.sirschrolli.kingdomsatwar.item.glitch.GlitchWings;
import io.github.sirschrolli.kingdomsatwar.registry.ModEffects;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import org.jspecify.annotations.Nullable;

/**
 * Der Flugbann (seit 0.82): Wen der Bannpfeil eines Bosses trifft, der kann eine Weile nicht gleiten – Elytren und alle
 * Schwingen tragen nicht (das Merkmal „Gleitflug“ ist abgeschaltet, siehe {@code ModEffects#FLIGHT_BAN}). Damit der Sturz
 * nicht gleich das Ende ist, tut er um 60 % weniger weh. Milch und Totems heben den Bann nicht auf; gute Schwingen
 * verkürzen ihn (Feen-Elytren um 15 %, Sternenschwingen um 30 %, Fehlerflügel um 40 %, die Schwingen des Schöpfers
 * um 60 %).
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class FlightBan {
    /** Um so viel tut der Sturz im Flugbann weniger weh. */
    public static final float FALL_FACTOR = 0.4f;

    private static boolean lifting;

    private FlightBan() {}

    /** Wie viel vom Flugbann bei diesen Schwingen übrig bleibt (1 = alles). */
    public static float factor(LivingEntity entity) {
        if (CreatorWings.wearing(entity)) {
            return 0.4f;
        }
        if (GlitchWings.wearing(entity)) {
            return 0.6f;
        }
        if (StarWings.wearing(entity)) {
            return 0.7f;
        }
        if (FaeElytra.wearing(entity)) {
            return 0.85f;
        }
        return 1.0f;
    }

    /**
     * Verhängt den Flugbann: {@code ticks} lang, bei guten Schwingen kürzer. Das Gleiten endet sofort.
     *
     * @return wie viele Ticks der Bann tatsächlich hält
     */
    public static int apply(LivingEntity victim, int ticks, @Nullable Entity source) {
        int actual = Math.max(20, Math.round(ticks * factor(victim)));
        victim.stopFallFlying();
        victim.addEffect(new MobEffectInstance(ModEffects.FLIGHT_BAN, actual, 0, false, false, true), source);
        return actual;
    }

    /** Hebt den Flugbann vorzeitig auf (Milch und Totems können das nicht). */
    public static void lift(LivingEntity entity) {
        lifting = true;
        try {
            entity.removeEffect(ModEffects.FLIGHT_BAN);
        } finally {
            lifting = false;
        }
    }

    public static boolean banned(LivingEntity entity) {
        return entity.hasEffect(ModEffects.FLIGHT_BAN);
    }

    @SubscribeEvent
    static void onFall(LivingFallEvent event) {
        if (banned(event.getEntity())) {
            event.setDamageMultiplier(event.getDamageMultiplier() * FALL_FACTOR);
        }
    }

    /** Milch, Totems und dergleichen heben den Bann nicht auf – er läuft ab (oder wird mit {@link #lift} aufgehoben). */
    @SubscribeEvent
    static void onRemove(MobEffectEvent.Remove event) {
        if (!lifting && event.getEffect().is(ModEffects.FLIGHT_BAN)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    static void onExpired(MobEffectEvent.Expired event) {
        MobEffectInstance instance = event.getEffectInstance();
        if (instance != null && instance.getEffect().is(ModEffects.FLIGHT_BAN) && event.getEntity() instanceof ServerPlayer player) {
            player.sendOverlayMessage(Component.translatable("message.kingdomsatwar.sky_ward.released").withColor(0xBFD8FF));
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.SKY_WARD_RELEASE.get(), SoundSource.PLAYERS, 0.8f, 1.0f);
        }
    }
}
