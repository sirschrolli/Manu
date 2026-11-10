package io.github.sirschrolli.kingdomsatwar.creator;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.entity.creator.DivineFx;
import io.github.sirschrolli.kingdomsatwar.registry.CreatorBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModParticles;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Was die Ausrüstung des Schöpfers kann (seit 0.78; die Schwingen siehe {@link CreatorWings}):
 * <ul>
 *     <li><b>Heiligenschein</b> (Kopf): Nachtsicht, keine Blindheit und keine Dunkelheit; ein Segen – alle drei Sekunden
 *     ein halbes Herz, solange man verwundet ist; Untote in {@value #HALO_RADIUS} Blöcken fangen Feuer und nehmen Schaden;</li>
 *     <li><b>Räder</b> (Beine): ihre Augen enthüllen Feinde in {@value #EYES_RADIUS} Blöcken (sie leuchten durch Wände), und
 *     jedes {@value #DEFLECT_PERCENT}. Prozent der Geschosse prallt an ihnen ab und fliegt zurück (schneller und
 *     Stufen hinauf tragen sie über ihre Werte).</li>
 * </ul>
 * Trägt man alle drei Teile (Heiligenschein, Schwingen, Räder), ist man das <b>Ebenbild des Schöpfers</b>: Der
 * Schwingenschlag lädt doppelt so schnell, und der Segen heilt doppelt so viel.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class CreatorGear {
    public static final double HALO_RADIUS = 6.0;
    public static final float HALO_BURN = 2.0f;
    public static final double EYES_RADIUS = 20.0;
    public static final int DEFLECT_PERCENT = 30;

    private CreatorGear() {}

    public static boolean halo(LivingEntity entity) {
        return entity.getItemBySlot(EquipmentSlot.HEAD).is(CreatorBlocks.CREATOR_HALO.get());
    }

    public static boolean rings(LivingEntity entity) {
        return entity.getItemBySlot(EquipmentSlot.LEGS).is(CreatorBlocks.CREATOR_RINGS.get());
    }

    /** Alle drei Teile: das Ebenbild des Schöpfers. */
    public static boolean image(LivingEntity entity) {
        return halo(entity) && rings(entity) && CreatorWings.wearing(entity);
    }

    @SubscribeEvent
    static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !player.isAlive() || player.isSpectator()) {
            return;
        }
        ServerLevel level = player.level();
        long t = level.getGameTime();
        if (halo(player)) {
            haloTick(level, player, t);
        }
        if (rings(player) && (t + player.getId()) % 20 == 0) {
            eyes(level, player);
        }
    }

    /** Der Heiligenschein: Nachtsicht ohne Flackern, Segen, Untote versengen. */
    private static void haloTick(ServerLevel level, ServerPlayer player, long t) {
        MobEffectInstance sight = player.getEffect(MobEffects.NIGHT_VISION);
        if (sight == null || sight.getDuration() < 260 && sight.getDuration() >= 0 && !sight.isInfiniteDuration()) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0, true, false, true));
        }
        if (player.hasEffect(MobEffects.BLINDNESS)) {
            player.removeEffect(MobEffects.BLINDNESS);
        }
        if (player.hasEffect(MobEffects.DARKNESS)) {
            player.removeEffect(MobEffects.DARKNESS);
        }
        if ((t + player.getId()) % 60 == 0 && player.getHealth() < player.getMaxHealth()) {
            player.heal(image(player) ? 2.0f : 1.0f);
        }
        if ((t + player.getId()) % 40 == 0) {
            AABB box = player.getBoundingBox().inflate(HALO_RADIUS);
            for (LivingEntity undead : level.getEntitiesOfClass(LivingEntity.class, box,
                    e -> e.isAlive() && e != player && e.is(EntityTypeTags.UNDEAD) && e.distanceToSqr(player) <= HALO_RADIUS * HALO_RADIUS)) {
                undead.igniteForSeconds(3.0f);
                undead.hurtServer(level, level.damageSources().indirectMagic(player, player), HALO_BURN);
                level.sendParticles(ParticleTypes.END_ROD, undead.getX(), undead.getY(0.6), undead.getZ(), 6, 0.3, 0.4, 0.3, 0.03);
            }
        }
        if (t % 6 == 0) {
            level.sendParticles(ModParticles.DIVINE_SPARK.get(), player.getX(), player.getY() + player.getBbHeight() + 0.35, player.getZ(),
                    1, 0.25, 0.05, 0.25, 0.0);
        }
    }

    /** Die Augen der Räder enthüllen Feinde in der Nähe: sie leuchten (auch durch Wände) für einige Sekunden. */
    private static void eyes(ServerLevel level, ServerPlayer player) {
        AABB box = player.getBoundingBox().inflate(EYES_RADIUS);
        for (Mob mob : level.getEntitiesOfClass(Mob.class, box, m -> m instanceof Enemy && m.isAlive()
                && m.distanceToSqr(player) <= EYES_RADIUS * EYES_RADIUS)) {
            MobEffectInstance glow = mob.getEffect(MobEffects.GLOWING);
            if (glow == null || glow.getDuration() < 30) {
                mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, 50, 0, true, false), player);
            }
        }
    }

    /** Wer den Heiligenschein trägt, wird weder blind noch von Dunkelheit umhüllt. */
    @SubscribeEvent
    static void onEffect(MobEffectEvent.Applicable event) {
        var effect = event.getEffectInstance().getEffect();
        if ((effect.is(MobEffects.BLINDNESS) || effect.is(MobEffects.DARKNESS)) && halo(event.getEntity())) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
        }
    }

    /** An den Rädern prallt manches Geschoss ab und fliegt zurück. */
    @SubscribeEvent
    static void onImpact(ProjectileImpactEvent event) {
        Projectile shot = event.getProjectile();
        if (!(event.getRayTraceResult() instanceof EntityHitResult hit) || !(hit.getEntity() instanceof Player player) || !rings(player)
                || !(shot.level() instanceof ServerLevel level) || shot.getOwner() == player) {
            return;
        }
        if (level.getRandom().nextInt(100) >= DEFLECT_PERCENT) {
            return;
        }
        event.setCanceled(true);
        Vec3 v = shot.getDeltaMovement();
        Entity owner = shot.getOwner();
        Vec3 back = owner != null ? owner.getEyePosition().subtract(shot.position()).normalize().scale(Math.max(0.8, v.length()))
                : v.scale(-0.8);
        shot.setDeltaMovement(back);
        shot.setOwner(player);
        shot.needsSync = true;
        DivineFx.sparks(level, shot.position(), 14, 0.3, 0.12);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, shot.getX(), shot.getY(), shot.getZ(), 6, 0.2, 0.2, 0.2, 0.2);
        level.playSound(null, player.getX(), player.getY() + 1.0, player.getZ(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.2f, 1.5f);
    }
}
