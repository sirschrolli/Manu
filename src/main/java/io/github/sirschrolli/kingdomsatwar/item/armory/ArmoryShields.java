package io.github.sirschrolli.kingdomsatwar.item.armory;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;

/**
 * Was die Schilde der Waffenkammer (0.84) können:
 * <ul>
 *   <li><b>Stahl-Rundschild:</b> hebt sich schneller als ein Holzschild. Wer innerhalb von {@link #PARRY_TICKS} Ticks nach
 *   dem Heben einen Hieb fängt, <b>pariert</b>: Der Angreifer wird zurückgestoßen, geschwächt und kurz verlangsamt.</li>
 *   <li><b>Drachenschild:</b> wer gegen ihn schlägt, brennt {@link #DRAGON_BURN} Sekunden.</li>
 *   <li><b>Turmschild:</b> deckt {@link #TOWER_ANGLE}° nach jeder Seite (ein Holzschild 90°), nutzt sich langsamer ab und
 *   steht fest gegen Rückstoß – in der Hand macht er etwas langsamer (siehe Armory).</li>
 *   <li><b>Spiegelschild:</b> Geschosse, die von vorn auf ihn treffen, prallen zurück – dorthin, wohin sein Träger schaut,
 *   und gehören nun ihm.</li>
 * </ul>
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class ArmoryShields {
    private ArmoryShields() {}

    public static final int PARRY_TICKS = 7;
    public static final double PARRY_PUSH = 1.1;
    public static final int PARRY_WEAKNESS = 60;
    public static final int DRAGON_BURN = 4;
    public static final float TOWER_ANGLE = 135.0f;

    /** Die Blockwerte eines Schilds: Hebezeit, Deckung (Grad nach jeder Seite), Abnutzung je geblocktem Schaden. */
    public static BlocksAttacks blocks(HolderLookup.Provider context, float delaySeconds, float angle, float wear) {
        return new BlocksAttacks(delaySeconds, 1.0f, List.of(new BlocksAttacks.DamageReduction(angle, Optional.empty(), 0.0f, 1.0f)),
                new BlocksAttacks.ItemDamageFunction(3.0f, 1.0f, wear), Optional.of(context.getOrThrow(DamageTypeTags.BYPASSES_SHIELD)),
                Optional.of(SoundEvents.SHIELD_BLOCK), Optional.of(SoundEvents.SHIELD_BREAK));
    }

    /** Der Schild, mit dem gerade geblockt wird – wenn er aus der Waffenkammer stammt. */
    public static ArmoryShieldItem.Kind blockingKind(LivingEntity entity) {
        ItemStack stack = entity.getItemBlockingWith();
        return stack != null && stack.getItem() instanceof ArmoryShieldItem shield ? shield.kind() : null;
    }

    /** Ist der Schild erst so kurz oben, dass ein Treffer pariert wird? */
    public static boolean inParryWindow(LivingEntity blocker) {
        ItemStack stack = blocker.getItemBlockingWith();
        BlocksAttacks blocks = stack == null ? null : stack.get(DataComponents.BLOCKS_ATTACKS);
        return blocks != null && blocker.getTicksUsingItem() <= blocks.blockDelayTicks() + PARRY_TICKS;
    }

    @SubscribeEvent
    static void onBlock(LivingShieldBlockEvent event) {
        if (!event.getBlocked() || event.getBlockedDamage() <= 0.0f || !(event.getEntity().level() instanceof ServerLevel level)) {
            return;
        }
        LivingEntity blocker = event.getEntity();
        ArmoryShieldItem.Kind kind = blockingKind(blocker);
        DamageSource source = event.getDamageSource();
        if (kind == null || !(source.getEntity() instanceof LivingEntity attacker) || source.getDirectEntity() != attacker) {
            return;                                                     // nur Hiebe (keine Geschosse, keine Zauber)
        }
        if (kind == ArmoryShieldItem.Kind.BUCKLER && inParryWindow(blocker)) {
            parry(level, blocker, attacker);
        } else if (kind == ArmoryShieldItem.Kind.DRAGON) {
            scorch(level, blocker, attacker);
        }
    }

    /** Parade: Stoß weg vom Schild, Schwäche und ein Augenblick Lähmung; Funken und ein heller Klang. */
    public static void parry(ServerLevel level, LivingEntity blocker, LivingEntity attacker) {
        Vec3 away = attacker.position().subtract(blocker.position()).multiply(1, 0, 1);
        Vec3 dir = away.lengthSqr() < 1.0e-4 ? blocker.getLookAngle().multiply(1, 0, 1).normalize() : away.normalize();
        attacker.push(dir.x * PARRY_PUSH, 0.35, dir.z * PARRY_PUSH);
        attacker.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, PARRY_WEAKNESS, 0), blocker);
        attacker.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 20, 2), blocker);
        Vec3 at = blocker.getEyePosition().add(dir.scale(0.7)).subtract(0, 0.4, 0);
        level.sendParticles(ParticleTypes.CRIT, at.x, at.y, at.z, 16, 0.2, 0.25, 0.2, 0.35);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, at.x, at.y, at.z, 8, 0.15, 0.2, 0.15, 0.2);
        level.playSound(null, blocker.getX(), blocker.getY(), blocker.getZ(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.35f, 1.9f);
        level.playSound(null, blocker.getX(), blocker.getY(), blocker.getZ(), SoundEvents.SHIELD_BLOCK.value(), SoundSource.PLAYERS, 1.0f, 1.4f);
    }

    /** Drachenschild: der Angreifer fängt Feuer. */
    public static void scorch(ServerLevel level, LivingEntity blocker, LivingEntity attacker) {
        if (!attacker.fireImmune()) {
            attacker.igniteForSeconds(DRAGON_BURN);
        }
        Vec3 c = attacker.getBoundingBox().getCenter();
        level.sendParticles(ParticleTypes.FLAME, c.x, c.y, c.z, 14, 0.3, 0.4, 0.3, 0.03);
        level.sendParticles(ParticleTypes.LAVA, c.x, c.y, c.z, 2, 0.2, 0.2, 0.2, 0.0);
        level.playSound(null, blocker.getX(), blocker.getY(), blocker.getZ(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.6f, 1.2f);
    }

    @SubscribeEvent
    static void onImpact(ProjectileImpactEvent event) {
        Projectile projectile = event.getProjectile();
        if (projectile.level().isClientSide() || !(event.getRayTraceResult() instanceof EntityHitResult hit)
                || !(hit.getEntity() instanceof LivingEntity blocker)) {
            return;
        }
        if (reflect(blocker, projectile)) {
            event.setCanceled(true);
        }
    }

    /** Kommt das Geschoss von vorn (waagrecht betrachtet)? */
    public static boolean fromFront(LivingEntity blocker, Entity projectile) {
        Vec3 to = projectile.position().subtract(blocker.position()).multiply(1, 0, 1);
        if (to.lengthSqr() < 1.0e-4) {
            return false;
        }
        return Vec3.directionFromRotation(0.0f, blocker.getYHeadRot()).dot(to.normalize()) > 0.0;
    }

    /**
     * Spiegelschild: das Geschoss prallt zurück – in Blickrichtung des Trägers, mit seiner Geschwindigkeit (mindestens einem
     * Block je Tick) – und gehört nun dem Träger. Kostet einen Punkt Haltbarkeit.
     */
    public static boolean reflect(LivingEntity blocker, Projectile projectile) {
        if (blockingKind(blocker) != ArmoryShieldItem.Kind.MIRROR || projectile.getOwner() == blocker || !fromFront(blocker, projectile)
                || !(blocker.level() instanceof ServerLevel level)) {
            return false;
        }
        double speed = Math.max(1.0, projectile.getDeltaMovement().length()) * 1.1;
        projectile.deflect(ProjectileDeflection.AIM_DEFLECT, blocker, EntityReference.of(blocker), true, speed);
        ItemStack shield = blocker.getItemBlockingWith();
        if (shield != null) {
            shield.hurtAndBreak(1, blocker, blocker.getUsedItemHand());
        }
        Vec3 at = projectile.position();
        level.sendParticles(ParticleTypes.END_ROD, at.x, at.y, at.z, 10, 0.15, 0.15, 0.15, 0.08);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, at.x, at.y, at.z, 12, 0.2, 0.2, 0.2, 0.05);
        level.playSound(null, blocker.getX(), blocker.getY(), blocker.getZ(), SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.PLAYERS, 1.2f, 1.6f);
        level.playSound(null, blocker.getX(), blocker.getY(), blocker.getZ(), SoundEvents.SHIELD_BLOCK.value(), SoundSource.PLAYERS, 0.7f, 1.8f);
        return true;
    }
}
