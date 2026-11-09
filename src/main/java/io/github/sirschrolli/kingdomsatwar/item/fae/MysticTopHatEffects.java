package io.github.sirschrolli.kingdomsatwar.item.fae;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.ModParticles;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import io.github.sirschrolli.kingdomsatwar.registry.MystikBlocks;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

/**
 * Der Zylinder der Mystik kennt MissMystiks Verschwindetrick: Sinkt das Leben seines Trägers unter ein knappes Drittel,
 * verschwindet er in einer Rauchwolke voller Konfetti – vier Sekunden unsichtbar und schnell, und wer ihn gerade jagt,
 * verliert ihn aus den Augen. Danach braucht der Hut anderthalb Minuten, bis er den Trick wieder kann.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class MysticTopHatEffects {
    /** Wann (Spielzeit) der Trick wieder geht. */
    public static final String READY = KingdomsAtWar.MODID + ":mystic_hat_ready";
    public static final int COOLDOWN = 1800;
    private static final DustParticleOptions SMOKE = new DustParticleOptions(0x6A4A9A, 2.4f);

    private MysticTopHatEffects() {}

    @SubscribeEvent
    static void onDamage(LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof Player player) || !(player.level() instanceof ServerLevel level) || !player.isAlive()) {
            return;
        }
        if (!player.getItemBySlot(EquipmentSlot.HEAD).is(MystikBlocks.MYSTIC_TOP_HAT.get())
                || player.getHealth() > player.getMaxHealth() * 0.3f) {
            return;
        }
        vanish(level, player);
    }

    /** Der Verschwindetrick (wenn der Hut bereit ist); true, wenn er geschah. */
    public static boolean vanish(ServerLevel level, Player player) {
        CompoundTag data = player.getPersistentData();
        long now = level.getGameTime();
        if (data.getLongOr(READY, 0L) > now) {
            return false;
        }
        data.putLong(READY, now + COOLDOWN);
        player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 80, 0, false, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.SPEED, 80, 1, false, true, true));
        level.sendParticles(SMOKE, player.getX(), player.getY(1.0), player.getZ(), 40, 0.5, 0.8, 0.5, 0.02);
        level.sendParticles(ParticleTypes.POOF, player.getX(), player.getY(0.6), player.getZ(), 16, 0.4, 0.6, 0.4, 0.04);
        level.sendParticles(ModParticles.CONFETTI.get(), player.getX(), player.getY(1.0), player.getZ(), 30, 0.4, 0.3, 0.4, 0.2);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.MYSTIC_HAT_VANISH.get(), SoundSource.PLAYERS, 1.2f, 1.0f);
        for (Mob mob : level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(24.0), m -> m.getTarget() == player)) {
            mob.setTarget(null);
        }
        player.sendOverlayMessage(Component.translatable("item.kingdomsatwar.mystic_top_hat.vanish").withColor(0xE8A8FF));
        player.getItemBySlot(EquipmentSlot.HEAD).hurtAndBreak(4, player, EquipmentSlot.HEAD);
        return true;
    }
}
