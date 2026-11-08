package io.github.sirschrolli.kingdomsatwar.hero;

import io.github.sirschrolli.kingdomsatwar.network.HeroPatEffectPayload;
import io.github.sirschrolli.kingdomsatwar.network.ModNetwork;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;

/**
 * Streicheln – die Belohnung der Legende (Heldenstufe {@value HeroLevel#MAX_LEVEL}). Mit der Streicheln-Taste (G) tätschelt
 * der Held dem Wesen vor sich den Kopf: Alle in der Nähe sehen die Hand und wie es sich kurz duckt, Herzen steigen auf, es
 * gibt ein leises Tätscheln und oft den Laut des Wesens. Eigene Soldaten, gezähmte Tiere und Drachen bekommen dabei ein
 * halbes Herz zurück – höchstens alle drei Sekunden je Wesen.
 *
 * <p>Die Idee stammt aus der Mod PatPat; hier ist sie eigenständig umgesetzt (eigener Code, eigene Hand), damit keine
 * zweite Mod nötig ist.
 */
public final class HeroPat {
    /** Ab dieser Heldenstufe. */
    public static final int LEVEL = HeroLevel.MAX_LEVEL;
    /** So weit reicht die Hand (Blöcke, von den Augen bis zum Wesen). */
    public static final double RANGE = 5.0;
    /** Ticks zwischen zwei Streicheleinheiten desselben Helden. */
    public static final int COOLDOWN = 5;
    /** Ticks, bis dasselbe Wesen wieder Leben bekommt. */
    public static final int HEAL_COOLDOWN = 60;
    /** Wie viel Leben eigene Gefährten bekommen (ein halbes Herz). */
    public static final float HEAL = 1.0f;

    /** Was beim Streicheln herauskam. */
    public enum Result { PATTED, LOCKED, TOO_FAR, WAIT, INVALID }

    private static final Map<UUID, Long> LAST_PAT = new ConcurrentHashMap<>();
    private static final Map<Entity, Long> LAST_HEAL = new WeakHashMap<>();

    private HeroPat() {}

    /** Darf dieser Held streicheln? */
    public static boolean unlocked(net.minecraft.world.entity.player.Player player) {
        return HeroLevel.of(player).level() >= LEVEL;
    }

    /** Der Held streichelt {@code target} – der Server prüft Stufe, Reichweite und Pause. */
    public static Result pat(ServerPlayer player, Entity target) {
        if (!(target instanceof LivingEntity living) || !living.isAlive() || target == player || player.isSpectator()) {
            return Result.INVALID;
        }
        if (!unlocked(player)) {
            player.sendOverlayMessage(Component.translatable("hero.kingdomsatwar.pat.locked", LEVEL).withStyle(ChatFormatting.GRAY));
            return Result.LOCKED;
        }
        if (target.level() != player.level() || player.getEyePosition().distanceTo(target.getBoundingBox().getCenter()) > RANGE + target.getBbWidth()) {
            return Result.TOO_FAR;
        }
        ServerLevel level = player.level();
        long now = level.getGameTime();
        Long last = LAST_PAT.get(player.getUUID());
        if (last != null && now - last < COOLDOWN && now >= last) {
            return Result.WAIT;
        }
        LAST_PAT.put(player.getUUID(), now);
        double top = target.getY() + target.getBbHeight();
        level.sendParticles(ParticleTypes.HEART, target.getX(), top + 0.35, target.getZ(), 2 + level.getRandom().nextInt(2),
                target.getBbWidth() * 0.3, 0.12, target.getBbWidth() * 0.3, 0.02);
        level.playSound(null, target.getX(), top, target.getZ(), SoundEvents.WOOL_HIT, SoundSource.PLAYERS, 0.7f,
                1.35f + level.getRandom().nextFloat() * 0.3f);
        if (target instanceof Mob mob && level.getRandom().nextFloat() < 0.4f) {
            mob.playAmbientSound();
        }
        if (target instanceof OwnableEntity ownable && ownable.getOwner() == player && living.getHealth() < living.getMaxHealth()) {
            Long healed = LAST_HEAL.get(target);
            if (healed == null || now - healed >= HEAL_COOLDOWN || now < healed) {
                LAST_HEAL.put(target, now);
                living.heal(HEAL);
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER, target.getX(), top - target.getBbHeight() * 0.4, target.getZ(), 5,
                        target.getBbWidth() * 0.4, target.getBbHeight() * 0.3, target.getBbWidth() * 0.4, 0.0);
            }
        }
        HeroPatEffectPayload payload = new HeroPatEffectPayload(target.getId(), player.getId());
        for (ServerPlayer viewer : level.players()) {
            if (viewer.distanceToSqr(target) < 64.0 * 64.0) {
                ModNetwork.send(viewer, payload);
            }
        }
        return Result.PATTED;
    }

    /** Für Tests: Pausen vergessen. */
    public static void forget(ServerPlayer player) {
        LAST_PAT.remove(player.getUUID());
    }
}
