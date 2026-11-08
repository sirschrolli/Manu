package io.github.sirschrolli.kingdomsatwar.hero;

import io.github.sirschrolli.kingdomsatwar.network.HeroPatEffectPayload;
import io.github.sirschrolli.kingdomsatwar.network.ModNetwork;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Streicheln – die Belohnung der Legende (Heldenstufe {@value HeroLevel#MAX_LEVEL}), so wie in der Mod PatPat: Schleichen
 * und mit leerer Hand rechtsklicken (oder die Streicheln-Taste G) tätschelt dem Wesen den Kopf. Alle in der Nähe sehen die
 * Hand und wie das Wesen beim Tätscheln niedriger wird, und hören den Tätschel-Ton; wer gestreichelt wird, dessen Name
 * verschwindet so lange. Gedrückt halten streichelt immer weiter. Eigene Soldaten, gezähmte Tiere und Drachen bekommen
 * dabei ein halbes Herz zurück – höchstens alle drei Sekunden je Wesen.
 *
 * <p>Hand und Töne stammen aus PatPat (LopyMine, nikita51; LGPL v3.0 – siehe licenses/patpat/NOTICE.md), der Code ist
 * eigenständig.
 */
@EventBusSubscriber(modid = io.github.sirschrolli.kingdomsatwar.KingdomsAtWar.MODID)
public final class HeroPat {
    /** Ab dieser Heldenstufe. */
    public static final int LEVEL = HeroLevel.MAX_LEVEL;
    /** So weit reicht die Hand (Blöcke, von den Augen bis zum Wesen). */
    public static final double RANGE = 5.0;
    /** Ticks zwischen zwei Streicheleinheiten desselben Helden (gedrückt halten wiederholt alle vier Ticks). */
    public static final int COOLDOWN = 4;
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
    public static boolean unlocked(Player player) {
        return HeroLevel.of(player).level() >= LEVEL;
    }

    /** Wie in PatPat: schleichend, mit leerer Hand, auf ein Lebewesen. */
    public static boolean gesture(Player player, InteractionHand hand, Entity target) {
        return hand == InteractionHand.MAIN_HAND && player.isShiftKeyDown() && player.getMainHandItem().isEmpty() && target instanceof LivingEntity
                && !player.isSpectator();
    }

    /**
     * Schleichen + Rechtsklick: Als Legende streichelt das, statt das Wesen zu benutzen (das Spiel schickt dann gar keinen
     * Rechtsklick, sondern die Streichel-Nachricht – hier auf dem Server greift es, falls doch einer ankommt).
     */
    @SubscribeEvent
    static void onInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        // im Spiel selbst kümmert sich PatClient darum (bricht ab und schickt die Streichel-Nachricht)
        if (event.getLevel().isClientSide() || !gesture(player, event.getHand(), event.getTarget()) || !unlocked(player)) {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (player instanceof ServerPlayer server) {
            pat(server, event.getTarget());
        }
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
        player.swing(InteractionHand.MAIN_HAND, net.minecraft.world.item.component.SwingAnimation.DEFAULT, true);
        level.playSound(null, target.getX(), target.getY() + target.getBbHeight(), target.getZ(), ModSounds.HERO_PAT.get(), SoundSource.PLAYERS,
                1.0f, 1.0f);
        if (target instanceof OwnableEntity ownable && ownable.getOwner() == player && living.getHealth() < living.getMaxHealth()) {
            Long healed = LAST_HEAL.get(target);
            if (healed == null || now - healed >= HEAL_COOLDOWN || now < healed) {
                LAST_HEAL.put(target, now);
                living.heal(HEAL);
                level.sendParticles(ParticleTypes.HEART, target.getX(), target.getY() + target.getBbHeight() + 0.3, target.getZ(), 1,
                        0.1, 0.05, 0.1, 0.0);
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
