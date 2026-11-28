package io.github.sirschrolli.kingdomsatwar.item;

import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

/**
 * Was Rüstungen an Zuständen geben (seit 0.99): Sie wirken still – ohne Symbol und ohne Teilchen. Ein Zustand wird erneuert,
 * solange seine Bedingung gilt (immer mit mehr als zehn Sekunden Rest, so beginnt Nachtsicht nie zu flackern), und endet
 * sofort, wenn sie nicht mehr gilt – er läuft nicht flackernd aus. Mehrere Quellen desselben Zustands stören sich nicht (er
 * endet erst, wenn keine ihn mehr gibt); ein Zustand aus einem Trank (mit Teilchen oder länger) bleibt unberührt.
 */
public final class GearEffects {
    /** So lange (Ticks) gibt die Ausrüstung einen Zustand … */
    public static final int HOLD = 300;
    /** … und erneuert ihn, sobald weniger übrig ist (über 200: Nachtsicht flackert erst darunter). */
    public static final int RENEW = 240;
    private static final String PREFIX = "kingdomsatwar_gear.";

    private GearEffects() {}

    /**
     * Gibt (oder erneuert) den Zustand still – die Quelle {@code source} hält ihn, bis {@link #drop} sie freigibt. Ein längerer
     * oder stärkerer Trank bleibt, wie er ist; läuft ein Trank aus, übernimmt die Ausrüstung still.
     */
    public static void keep(LivingEntity entity, Holder<MobEffect> effect, int amplifier, String source) {
        CompoundTag data = entity.getPersistentData();
        String key = key(effect, source);
        if (!data.getBooleanOr(key, false)) {
            data.putBoolean(key, true);
        }
        MobEffectInstance current = entity.getEffect(effect);
        // schwächer: die Ausrüstung legt sich darüber (ein längerer Trank wartet darunter); gleich stark: erneuern, bevor er
        // ausläuft – auch einen auslaufenden Trank, so flackert er nicht in seinen letzten Sekunden
        if (current == null || current.getAmplifier() < amplifier
                || current.getAmplifier() == amplifier && !current.isInfiniteDuration() && current.getDuration() < RENEW) {
            entity.addEffect(new MobEffectInstance(effect, HOLD, amplifier, true, false, false));
        }
    }

    /** Die Quelle gibt den Zustand nicht mehr: Hält ihn keine andere, endet er sofort (ein Trank bleibt). */
    public static void drop(LivingEntity entity, Holder<MobEffect> effect, String source) {
        CompoundTag data = entity.getPersistentData();
        String key = key(effect, source);
        if (!data.contains(key)) {
            return;
        }
        data.remove(key);
        String suffix = "|" + name(effect);
        for (String other : data.keySet()) {
            if (other.startsWith(PREFIX) && other.endsWith(suffix)) {
                return;
            }
        }
        MobEffectInstance current = entity.getEffect(effect);
        if (current != null && fromGear(current)) {
            MobEffectInstance waiting = hidden(current);
            entity.removeEffect(effect);
            if (waiting != null) {
                entity.addEffect(waiting); // der Trank, der unter dem Zustand der Ausrüstung wartete, wirkt weiter
            }
        }
    }

    /** Der Zustand, der unter diesem wartet (etwa ein schwächerer, längerer Trank), oder null. */
    private static @Nullable MobEffectInstance hidden(MobEffectInstance instance) {
        Tag tag = MobEffectInstance.CODEC.encodeStart(NbtOps.INSTANCE, instance).result().orElse(null);
        if (!(tag instanceof CompoundTag outer) || !(outer.get("hidden_effect") instanceof CompoundTag inner) || outer.get("id") == null) {
            return null;
        }
        CompoundTag copy = inner.copy();
        copy.put("id", outer.get("id").copy());
        return MobEffectInstance.CODEC.parse(NbtOps.INSTANCE, copy).result().orElse(null);
    }

    /** Gibt oder nimmt den Zustand je nach {@code active}. */
    public static void set(LivingEntity entity, Holder<MobEffect> effect, int amplifier, String source, boolean active) {
        if (active) {
            keep(entity, effect, amplifier, source);
        } else {
            drop(entity, effect, source);
        }
    }

    /** Sieht der Zustand aus wie einer von der Ausrüstung (still und nicht länger, als sie ihn gibt)? */
    public static boolean fromGear(MobEffectInstance instance) {
        return instance.isAmbient() && !instance.isVisible() && !instance.isInfiniteDuration() && instance.getDuration() <= HOLD;
    }

    private static String key(Holder<MobEffect> effect, String source) {
        return PREFIX + source + "|" + name(effect);
    }

    private static String name(Holder<MobEffect> effect) {
        return effect.unwrapKey().map(k -> k.identifier().toString()).orElse("unknown");
    }
}
