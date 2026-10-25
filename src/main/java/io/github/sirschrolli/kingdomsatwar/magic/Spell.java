package io.github.sirschrolli.kingdomsatwar.magic;

import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Die Zauber des Aetherit-Stabs. Ohne Rune schießt der Stab einen Aether-Blitz; mit einer Rune in der anderen Hand
 * wirkt er den Zauber ihres Elements – derselben sechs Elemente, aus denen die Drachen geboren sind.
 *
 * <p>Jeder Zauber kostet Haltbarkeit des Stabs und braucht danach eine Weile, bis er wieder bereit ist.
 */
public enum Spell {
    /** Aether-Blitz: trifft das anvisierte Wesen mit reiner Magie. */
    AETHER_BOLT(null, 1, 15),
    /** Feuerball, wie ihn die Lohe schleudert. */
    FIRE(DragonType.FIRE, 1, 20),
    /** Frosthauch: verlangsamt, lässt erstarren und friert Wasser zu Eis. */
    ICE(DragonType.ICE, 1, 30),
    /** Ruft einen Blitz an die anvisierte Stelle. */
    LIGHTNING(DragonType.LIGHTNING, 3, 80),
    /** Heilt einen selbst und seine Verbündeten und lässt Pflanzen sprießen. */
    NATURE(DragonType.NATURE, 2, 160),
    /** Erdstoß: schleudert Feinde in der Nähe von sich. */
    EARTH(DragonType.EARTH, 2, 80),
    /** Woge: drängt alles zurück, löscht Feuer und lässt einen unter Wasser atmen. */
    WATER(DragonType.WATER, 1, 60);

    private final @Nullable DragonType element;
    private final int cost;
    private final int cooldown;

    Spell(@Nullable DragonType element, int cost, int cooldown) {
        this.element = element;
        this.cost = cost;
        this.cooldown = cooldown;
    }

    /** Zauber zur Rune (oder der Aether-Blitz ohne Rune). */
    public static Spell of(@Nullable DragonType element) {
        for (Spell spell : values()) {
            if (spell.element == element) {
                return spell;
            }
        }
        return AETHER_BOLT;
    }

    /** Haltbarkeit, die der Zauber den Stab kostet. */
    public int cost() {
        return this.cost;
    }

    /** Abklingzeit in Ticks. */
    public int cooldown() {
        return this.cooldown;
    }

    /** Wirkt den Zauber. @return ob er etwas bewirkt hat (sonst keine Kosten) */
    public boolean cast(ServerLevel level, Player caster) {
        return switch (this) {
            case AETHER_BOLT -> aetherBolt(level, caster);
            case FIRE -> fireball(level, caster);
            case ICE -> frost(level, caster);
            case LIGHTNING -> lightning(level, caster);
            case NATURE -> nature(level, caster);
            case EARTH -> earthquake(level, caster);
            case WATER -> wave(level, caster);
        };
    }

    // =====================================================================================
    // Die Zauber
    // =====================================================================================

    private static boolean aetherBolt(ServerLevel level, Player caster) {
        HitResult hit = ProjectileUtil.getHitResultOnViewVector(caster, target -> canHit(caster, target), 24.0);
        trail(level, handPosition(caster), hit.getLocation(), ParticleTypes.END_ROD, 3);
        sound(level, caster, SoundEvents.AMETHYST_BLOCK_RESONATE, 1.2f, 1.4f);
        sound(level, caster, SoundEvents.EVOKER_CAST_SPELL, 0.6f, 1.6f);
        if (hit instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof LivingEntity target) {
            target.hurtServer(level, level.damageSources().indirectMagic(caster, caster), 6.0f);
            level.sendParticles(ParticleTypes.GLOW, target.getX(), target.getY(0.5), target.getZ(), 15, 0.3, 0.4, 0.3, 0.05);
        }
        return true;
    }

    private static boolean fireball(ServerLevel level, Player caster) {
        Vec3 look = caster.getLookAngle();
        // Der Feuerball verbrennt Wesen, setzt aber keine Blöcke in Brand (keine abgebrannten Burgen aus Versehen)
        SmallFireball fireball = new SmallFireball(level, caster, look.scale(1.6)) {
            @Override
            protected void onHitBlock(BlockHitResult hit) {
                if (this.level() instanceof ServerLevel server) {
                    Vec3 at = hit.getLocation();
                    server.sendParticles(ParticleTypes.SMOKE, at.x, at.y, at.z, 8, 0.15, 0.15, 0.15, 0.02);
                }
            }
        };
        Vec3 start = handPosition(caster);
        fireball.setPos(start.x, start.y, start.z);
        level.addFreshEntity(fireball);
        sound(level, caster, SoundEvents.BLAZE_SHOOT, 1.0f, 1.0f);
        level.sendParticles(ParticleTypes.SMALL_FLAME, start.x, start.y, start.z, 6, 0.08, 0.08, 0.08, 0.02);
        return true;
    }

    /** Frosthauch in einem Kegel von 8 Blöcken vor dem Zaubernden. */
    private static boolean frost(ServerLevel level, Player caster) {
        Vec3 eye = caster.getEyePosition();
        Vec3 look = caster.getLookAngle();
        for (int i = 2; i <= 8; i++) {
            Vec3 point = eye.add(look.scale(i));
            double spread = 0.15 * i;
            level.sendParticles(ParticleTypes.SNOWFLAKE, point.x, point.y, point.z, 6, spread, spread, spread, 0.02);
        }
        for (LivingEntity target : inCone(level, caster, 8.0, 0.8)) {
            target.hurtServer(level, level.damageSources().freeze(), 4.0f);
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 2), caster);
            target.setTicksFrozen(Math.max(target.getTicksFrozen(), target.getTicksRequiredToFreeze() + 120));
        }
        // Wasser vor einem friert zu brüchigem Eis (wie mit Frostläufer)
        BlockPos center = caster.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-6, -2, -6), center.offset(6, 1, 6))) {
            Vec3 toBlock = Vec3.atCenterOf(pos).subtract(eye);
            if (toBlock.length() > 7.5 || toBlock.normalize().dot(look) < 0.7) {
                continue;
            }
            BlockState state = level.getBlockState(pos);
            if (state.getFluidState().is(Fluids.WATER) && state.getFluidState().isSource() && level.getBlockState(pos.above()).isAir()) {
                level.setBlockAndUpdate(pos, Blocks.FROSTED_ICE.defaultBlockState());
                level.scheduleTick(pos, Blocks.FROSTED_ICE, Mth.nextInt(caster.getRandom(), 60, 120));
            }
        }
        sound(level, caster, SoundEvents.POWDER_SNOW_BREAK, 1.2f, 0.7f);
        sound(level, caster, SoundEvents.PLAYER_HURT_FREEZE, 0.6f, 1.4f);
        return true;
    }

    private static boolean lightning(ServerLevel level, Player caster) {
        HitResult hit = ProjectileUtil.getHitResultOnViewVector(caster, target -> canHit(caster, target), 48.0);
        if (hit.getType() == HitResult.Type.MISS) {
            sound(level, caster, SoundEvents.BEACON_DEACTIVATE, 0.6f, 1.6f);
            return false;
        }
        LightningBolt bolt = EntityTypes.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
        if (bolt == null) {
            return false;
        }
        bolt.snapTo(hit.getLocation());
        if (caster instanceof ServerPlayer player) {
            bolt.setCause(player);
        }
        level.addFreshEntity(bolt);
        trail(level, handPosition(caster), hit.getLocation(), ParticleTypes.ELECTRIC_SPARK, 2);
        return true;
    }

    private static boolean nature(ServerLevel level, Player caster) {
        List<LivingEntity> allies = new java.util.ArrayList<>(List.of(caster));
        allies.addAll(level.getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(8.0),
                entity -> entity != caster && entity.isAlive() && isAlly(caster, entity)));
        for (LivingEntity ally : allies) {
            ally.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 120, 1), caster);
            if (ally != caster) {     // beim Zaubernden selbst nicht – direkt vor der Kamera wären die Funken riesig
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER, ally.getX(), ally.getY(0.6), ally.getZ(), 10, 0.4, 0.5, 0.4, 0.0);
            }
        }
        BlockPos center = caster.blockPosition();
        ItemStack boneMeal = new ItemStack(Items.BONE_MEAL, 64);
        for (int i = 0; i < 10; i++) {
            BlockPos pos = center.offset(caster.getRandom().nextInt(9) - 4, caster.getRandom().nextInt(3) - 1, caster.getRandom().nextInt(9) - 4);
            if (BoneMealItem.applyBonemeal(boneMeal, level, pos, caster)) {
                level.levelEvent(1505, pos, 15);    // Knochenmehl-Partikel
            }
        }
        level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, caster.getX(), caster.getY(1.0), caster.getZ(), 40, 3.0, 1.0, 3.0, 0.0);
        sound(level, caster, SoundEvents.AMETHYST_BLOCK_CHIME, 1.4f, 0.8f);
        sound(level, caster, SoundEvents.BONE_MEAL_USE, 1.0f, 1.0f);
        return true;
    }

    private static boolean earthquake(ServerLevel level, Player caster) {
        BlockState ground = level.getBlockState(caster.blockPosition().below());
        ParticleOptions dust = ground.isAir() ? ParticleTypes.CLOUD : new BlockParticleOption(ParticleTypes.BLOCK, ground);
        for (int ring = 1; ring <= 5; ring++) {
            for (int a = 0; a < ring * 8; a++) {
                double angle = Math.PI * 2 * a / (ring * 8);
                level.sendParticles(dust, caster.getX() + Math.cos(angle) * ring, caster.getY() + 0.1, caster.getZ() + Math.sin(angle) * ring,
                        2, 0.1, 0.1, 0.1, 0.1);
            }
        }
        DamageSource source = level.damageSources().indirectMagic(caster, caster);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(5.0, 2.0, 5.0),
                entity -> entity.isAlive() && canHit(caster, entity))) {
            if (target.hurtServer(level, source, 6.0f)) {
                Vec3 away = target.position().subtract(caster.position()).multiply(1.0, 0.0, 1.0).normalize();
                target.knockback(1.4, -away.x, -away.z, source, 6.0f);
                target.push(0.0, 0.5, 0.0);
            }
        }
        sound(level, caster, SoundEvents.GENERIC_EXPLODE.value(), 0.7f, 0.55f);
        sound(level, caster, SoundEvents.RAVAGER_STEP, 1.2f, 0.6f);
        return true;
    }

    private static boolean wave(ServerLevel level, Player caster) {
        Vec3 eye = caster.getEyePosition();
        Vec3 look = caster.getLookAngle();
        for (int i = 2; i <= 7; i++) {
            Vec3 point = eye.add(look.scale(i)).add(0, -0.8, 0);
            double spread = 0.2 * i;
            level.sendParticles(ParticleTypes.SPLASH, point.x, point.y, point.z, 20, spread, 0.3, spread, 0.1);
            level.sendParticles(ParticleTypes.BUBBLE_POP, point.x, point.y, point.z, 6, spread, 0.3, spread, 0.05);
        }
        DamageSource source = level.damageSources().indirectMagic(caster, caster);
        for (LivingEntity target : inCone(level, caster, 7.0, 0.6)) {
            target.clearFire();
            target.knockback(1.8, -look.x, -look.z, source, 0.0f);
            target.push(0.0, 0.25, 0.0);
        }
        // Feuer vor einem löschen
        BlockPos center = caster.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-6, -1, -6), center.offset(6, 2, 6))) {
            Vec3 toBlock = Vec3.atCenterOf(pos).subtract(eye);
            if (toBlock.length() <= 7.0 && toBlock.normalize().dot(look) > 0.5 && level.getBlockState(pos).is(Blocks.FIRE)) {
                level.removeBlock(pos, false);
                level.levelEvent(1009, pos, 0);   // Zischen
            }
        }
        caster.clearFire();
        caster.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 600, 0), caster);
        caster.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 200, 0), caster);
        sound(level, caster, SoundEvents.GENERIC_SPLASH, 1.2f, 0.8f);
        return true;
    }

    // =====================================================================================
    // Hilfen
    // =====================================================================================

    /** Verbündete: man selbst, eigene Tiere, Drachen und Soldaten sowie Mitglieder des eigenen Teams. */
    public static boolean isAlly(Player caster, Entity entity) {
        if (entity == caster || caster.isAlliedTo(entity)) {
            return true;
        }
        return entity instanceof OwnableEntity ownable && ownable.getOwnerReference() != null
                && caster.getUUID().equals(ownable.getOwnerReference().getUUID());
    }

    private static boolean canHit(Player caster, Entity entity) {
        return entity instanceof LivingEntity && entity.isPickable() && !entity.isSpectator() && !isAlly(caster, entity);
    }

    /** Lebewesen im Kegel vor dem Zaubernden (Reichweite, Mindest-Kosinus zur Blickrichtung). */
    private static List<LivingEntity> inCone(ServerLevel level, Player caster, double range, double minDot) {
        Vec3 eye = caster.getEyePosition();
        Vec3 look = caster.getLookAngle();
        return level.getEntitiesOfClass(LivingEntity.class, new AABB(eye, eye).inflate(range), entity -> {
            if (!entity.isAlive() || !canHit(caster, entity)) {
                return false;
            }
            Vec3 to = entity.getBoundingBox().getCenter().subtract(eye);
            return to.length() <= range && to.normalize().dot(look) >= minDot;
        });
    }

    /** Spitze des Stabs: etwas vor und unterhalb der Augen (Partikel direkt vor der Kamera verdecken sonst die Sicht). */
    private static Vec3 handPosition(Player caster) {
        Vec3 look = caster.getLookAngle();
        return caster.getEyePosition().add(look.scale(1.2)).add(0, -0.25, 0);
    }

    private static void trail(ServerLevel level, Vec3 from, Vec3 to, ParticleOptions particle, int perBlock) {
        Vec3 delta = to.subtract(from);
        int steps = Math.max(1, (int) (delta.length() * perBlock));
        for (int i = 1; i <= steps; i++) {
            Vec3 point = from.add(delta.scale((double) i / steps));
            level.sendParticles(particle, point.x, point.y, point.z, 1, 0.02, 0.02, 0.02, 0.0);
        }
    }

    private static void sound(ServerLevel level, Player caster, SoundEvent sound, float volume, float pitch) {
        level.playSound(null, caster.getX(), caster.getY(), caster.getZ(), sound, SoundSource.PLAYERS, volume, pitch);
    }
}
