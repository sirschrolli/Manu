package io.github.sirschrolli.kingdomsatwar.relic;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.network.ItemActivationPayload;
import io.github.sirschrolli.kingdomsatwar.network.ModNetwork;
import io.github.sirschrolli.kingdomsatwar.registry.ModEffects;
import io.github.sirschrolli.kingdomsatwar.registry.RelicItems;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

/**
 * Das Zweite Leben (seit 0.80): Wer einen Apfel der Unsterblichkeit gegessen hat, den hält für zehn Minuten einmal nichts
 * um – kein Schlag, kein Sturz, kein Feuer. Er steht mit allen Herzen wieder auf, was ihm schadete, fällt von ihm ab, und
 * das Zweite Leben ist verbraucht. Es wirkt erst, wenn kein Totem mehr hilft (ein Totem nimmt zwar alle anderen Zustände,
 * das Zweite Leben aber nicht), und vor dem Licht der Unsterblichkeit (das lange ruht, während der Apfel ohnehin vergeht).
 * Gegen /kill und das Nichts unter der Welt hilft es nicht.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class PowerApples {
    /** Wem in diesem Tick ein Totem den Tod aufhält: Ihm nimmt das Totem das Zweite Leben nicht (sonst fielen alle Zustände ab). */
    private static final java.util.Set<java.util.UUID> SHIELDED = new java.util.HashSet<>();
    private static boolean consuming;

    private PowerApples() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    static void onUseTotem(net.neoforged.neoforge.event.entity.living.LivingUseTotemEvent event) {
        if (event.getEntity().hasEffect(ModEffects.SECOND_LIFE)) {
            SHIELDED.add(event.getEntity().getUUID());
        }
    }

    @SubscribeEvent
    static void onEffectRemoved(net.neoforged.neoforge.event.entity.living.MobEffectEvent.Remove event) {
        if (!consuming && !SHIELDED.isEmpty() && event.getEffect().is(ModEffects.SECOND_LIFE) && SHIELDED.contains(event.getEntity().getUUID())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    static void onServerTick(net.neoforged.neoforge.event.tick.ServerTickEvent.Post event) {
        SHIELDED.clear();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    static void onDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity.level() instanceof ServerLevel level) || !entity.hasEffect(ModEffects.SECOND_LIFE)
                || event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return;
        }
        event.setCanceled(true);
        secondLife(level, entity);
    }

    /** Das Zweite Leben greift: volle Herzen, alles Schädliche fällt ab, Licht und Klang. */
    public static void secondLife(ServerLevel level, LivingEntity entity) {
        consuming = true;
        try {
            entity.removeEffect(ModEffects.SECOND_LIFE);
        } finally {
            consuming = false;
        }
        for (MobEffectInstance effect : List.copyOf(entity.getActiveEffects())) {
            if (effect.getEffect().value().getCategory() == MobEffectCategory.HARMFUL) {
                entity.removeEffect(effect.getEffect());
            }
        }
        entity.clearFire();
        entity.setHealth(entity.getMaxHealth());
        entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));
        entity.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 100, 1));
        double x = entity.getX();
        double y = entity.getY();
        double z = entity.getZ();
        for (int i = 0; i < 40; i++) {
            double a = i * Math.PI * 2.0 / 40.0;
            level.sendParticles(ParticleTypes.END_ROD, x + Math.cos(a) * 0.6, y + 0.2, z + Math.sin(a) * 0.6, 0, Math.cos(a), 0.02, Math.sin(a), 0.3);
        }
        level.sendParticles(new DustColorTransitionOptions(0xFFE9A0, 0xFFFFFF, 1.4f), x, y + 1.0, z, 60, 0.6, 1.0, 0.6, 0.0);
        level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, x, y + 1.0, z, 40, 0.4, 0.8, 0.4, 0.4);
        level.playSound(null, x, y, z, SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 0.8f, 1.2f);
        level.playSound(null, x, y, z, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.0f, 1.0f);
        if (entity instanceof ServerPlayer player) {
            ModNetwork.send(player, new ItemActivationPayload(new ItemStack(RelicItems.apple(PowerApple.IMMORTAL))));
            player.sendSystemMessage(Component.translatable("message.kingdomsatwar.second_life").withStyle(ChatFormatting.GOLD));
        }
    }
}
