package io.github.sirschrolli.kingdomsatwar.item.voidrealm;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.abyss.AbyssGate;
import io.github.sirschrolli.kingdomsatwar.registry.VoidGear;
import io.github.sirschrolli.kingdomsatwar.relic.Totems;
import io.github.sirschrolli.kingdomsatwar.voidrealm.VoidGate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import org.jspecify.annotations.Nullable;

/**
 * Die Rüstung der Sternwandler (seit 0.99) aus Himmelseisen überlistet die Schwerkraft: Jeder Teil macht leichter (über das
 * Attribut der Schwerkraft – man springt höher und fällt langsamer), die Stiefel fangen Stürze ab. Mit dem vollen Satz
 * <ul>
 *     <li><b>Sternenschweben:</b> Wer nach einem Absprung länger in der Luft ist und die Sprungtaste hält, sinkt sanft herab
 *     (ein kurzer, unsichtbarer Sinkflug, solange er die Taste hält) – dabei rieselt Sternenstaub;</li>
 *     <li><b>Sternenanker:</b> Wer ins Nichts stürzt (unter den Grund der Welt, in der Leere und im Abgrund schon bevor sie
 *     einen ausspucken), den trägt die Rüstung zurück auf den letzten festen Boden – höchstens alle {@link #ANCHOR_COOLDOWN}
 *     Ticks.</li>
 * </ul>
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class StarwalkerArmor {
    /** Abklingzeit des Sternenankers (Ticks): zwei Minuten. */
    public static final int ANCHOR_COOLDOWN = 20 * 120;
    /** So lange muss man schon in der Luft sein, bevor man schwebt (Ticks) – ein gewöhnlicher Sprung dauert kürzer. */
    public static final int GLIDE_AFTER = 16;
    /** Dauer des Sinkflugs (Ticks) – beim Halten der Taste ständig erneuert. */
    public static final int GLIDE = 8;
    /** In der Leere und im Abgrund greift der Anker schon so tief (sie spucken einen bei -40 wieder aus). */
    public static final double REALM_DEPTH = -32.0;
    private static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private static final String ANCHOR_KEY = "kingdomsatwar_star_anchor";
    private static final String GLIDE_KEY = "kingdomsatwar_star_glide";
    /** Wie lange jeder Spieler schon in der Luft ist (Ticks). */
    private static final Map<UUID, Integer> AIRBORNE = new HashMap<>();
    private static final DustParticleOptions STARDUST = new DustParticleOptions(0xB8D4FF, 0.7f);

    private StarwalkerArmor() {}

    /** Wie viele Teile der Rüstung das Wesen trägt (0–4). */
    public static int pieces(LivingEntity entity) {
        List<DeferredItem<Item>> set = VoidGear.starwalkerSet();
        int count = 0;
        for (int i = 0; i < SLOTS.length; i++) {
            if (entity.getItemBySlot(SLOTS[i]).is(set.get(i).get())) {
                count++;
            }
        }
        return count;
    }

    /** Trägt das Wesen den vollen Satz? */
    public static boolean fullSet(LivingEntity entity) {
        return pieces(entity) == SLOTS.length;
    }

    /** Ab dieser Höhe greift der Sternenanker in dieser Welt. */
    public static double anchorDepth(ServerLevel level) {
        if (level.dimension() == VoidGate.THE_VOID || level.dimension() == AbyssGate.THE_ABYSS) {
            return REALM_DEPTH;
        }
        return level.getMinY() - 4.0;
    }

    // Vor dem Auswurf der Leere und des Abgrunds (normale Priorität), damit der Anker zuerst greift
    @SubscribeEvent(priority = EventPriority.HIGH)
    static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !player.isAlive() || player.isSpectator()) {
            return;
        }
        boolean airborne = !player.onGround() && !player.isInWater() && !player.isInLava() && !player.onClimbable()
                && !player.isPassenger() && !player.isFallFlying() && !player.getAbilities().flying;
        int air = airborne ? AIRBORNE.merge(player.getUUID(), 1, Integer::sum) : 0;
        if (!airborne) {
            AIRBORNE.remove(player.getUUID());
        }
        if (!fullSet(player)) {
            glide(player, false);
            return;
        }
        if (player.getY() < anchorDepth(player.level()) && anchor(player, Totems.safeSpot(player))) {
            AIRBORNE.remove(player.getUUID());
            return;
        }
        glide(player, air >= GLIDE_AFTER && player.getLastClientInput().jump());
    }

    /**
     * Sternenschweben: Will der Spieler schweben, bekommt er einen kurzen, unsichtbaren Sinkflug (immer wieder erneuert);
     * sonst endet der Sinkflug der Rüstung (ein Trank bleibt). Gibt zurück, ob er gerade schwebt.
     */
    public static boolean glide(LivingEntity entity, boolean wanted) {
        boolean given = entity.getPersistentData().getBooleanOr(GLIDE_KEY, false);
        MobEffectInstance current = entity.getEffect(MobEffects.SLOW_FALLING);
        boolean ours = given && current != null && current.isAmbient() && !current.isVisible() && current.getDuration() <= GLIDE;
        if (wanted) {
            if (current == null || (ours && current.getDuration() < GLIDE - 2)) {
                entity.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, GLIDE, 0, true, false, false));
                entity.getPersistentData().putBoolean(GLIDE_KEY, true);
            }
            if (entity.level() instanceof ServerLevel level && entity.tickCount % 3 == 0) {
                level.sendParticles(STARDUST, entity.getX(), entity.getY() - 0.1, entity.getZ(), 3, 0.3, 0.05, 0.3, 0.0);
                if (entity.tickCount % 9 == 0) {
                    level.sendParticles(ParticleTypes.END_ROD, entity.getX(), entity.getY(), entity.getZ(), 1, 0.25, 0.05, 0.25, 0.01);
                }
            }
            return true;
        }
        if (given) {
            if (ours) {
                entity.removeEffect(MobEffects.SLOW_FALLING);
            }
            entity.getPersistentData().remove(GLIDE_KEY);
        }
        return false;
    }

    /** Ist der Sternenanker bereit (nicht in der Abklingzeit)? */
    public static boolean anchorReady(ServerPlayer player) {
        long last = player.getPersistentData().getLongOr(ANCHOR_KEY, Long.MIN_VALUE);
        return last == Long.MIN_VALUE || player.level().getGameTime() - last >= ANCHOR_COOLDOWN || player.level().getGameTime() < last;
    }

    /**
     * Sternenanker: trägt den Spieler mit dem vollen Satz zurück auf festen Boden (safe), wenn der Anker bereit ist. Gibt
     * zurück, ob er ihn getragen hat.
     */
    public static boolean anchor(ServerPlayer player, @Nullable Vec3 safe) {
        if (safe == null || !fullSet(player) || !anchorReady(player)) {
            return false;
        }
        ServerLevel level = player.level();
        level.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0, player.getZ(), 30, 0.4, 0.8, 0.4, 0.05);
        player.teleportTo(level, safe.x, safe.y, safe.z, Set.of(), player.getYRot(), player.getXRot(), true);
        player.setDeltaMovement(Vec3.ZERO);
        player.needsSync = true;
        player.resetFallDistance();
        player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 40, 0, true, false, false));
        player.getPersistentData().putLong(ANCHOR_KEY, level.getGameTime());
        level.sendParticles(STARDUST, safe.x, safe.y + 0.6, safe.z, 40, 0.5, 0.9, 0.5, 0.0);
        level.sendParticles(ParticleTypes.END_ROD, safe.x, safe.y + 1.0, safe.z, 24, 0.4, 0.8, 0.4, 0.04);
        level.playSound(null, safe.x, safe.y, safe.z, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.2f, 0.7f);
        level.playSound(null, safe.x, safe.y, safe.z, SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 0.8f, 1.3f);
        player.sendOverlayMessage(Component.translatable("message.kingdomsatwar.starwalker_anchor").withStyle(ChatFormatting.AQUA));
        return true;
    }

    @SubscribeEvent
    static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        AIRBORNE.remove(event.getEntity().getUUID());
    }
}
