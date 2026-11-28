package io.github.sirschrolli.kingdomsatwar.entity.boss.fury;

import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.magic.fx.SpellFxEntity;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Das Element eines Bosses für seine Zornangriffe (seit 1.14): Farbe, Teilchen, wie ein Einschlag aussieht, was ein Treffer
 * anrichtet und wie die Angriffe heißen (siehe {@link FuryAttack#title}).
 */
public enum FuryElement {
    FIRE(0xFF6A1A, DragonType.FIRE),
    ICE(0x9FE8FF, DragonType.ICE),
    LIGHTNING(0xFFE45A, DragonType.LIGHTNING),
    NATURE(0x7FE05A, DragonType.NATURE),
    EARTH(0xC8A070, DragonType.EARTH),
    WATER(0x4FD0F0, DragonType.WATER),
    SHADOW(0x7A3AC8, null),
    LIGHT(0xFFE9A0, null),
    SOUND(0x40E0D0, null),
    VOID(0xC060FF, null),
    POISON(0x8FD04F, null),
    STEEL(0xD0D8E4, null),
    WIND(0xD8F4FF, null);

    private final int color;
    private final @Nullable DragonType dragon;

    FuryElement(int color, @Nullable DragonType dragon) {
        this.color = color;
        this.dragon = dragon;
    }

    public int color() {
        return this.color;
    }

    /** Das Element der Zauberwirkungen (Feuer-, Wasser-, Eissäulen …) oder null (Licht in der eigenen Farbe). */
    public @Nullable DragonType dragon() {
        return this.dragon;
    }

    public static FuryElement of(@Nullable DragonType type) {
        if (type == null) {
            return SHADOW;
        }
        return switch (type) {
            case FIRE -> FIRE;
            case ICE -> ICE;
            case LIGHTNING -> LIGHTNING;
            case NATURE -> NATURE;
            case EARTH -> EARTH;
            case WATER -> WATER;
        };
    }

    /** Die Teilchen des Elements. */
    public ParticleOptions particle() {
        return switch (this) {
            case FIRE -> ParticleTypes.FLAME;
            case ICE -> ParticleTypes.SNOWFLAKE;
            case LIGHTNING -> ParticleTypes.ELECTRIC_SPARK;
            case NATURE -> ParticleTypes.COMPOSTER;
            case EARTH -> ParticleTypes.DUST_PLUME;
            case WATER -> ParticleTypes.SPLASH;
            case SHADOW -> ParticleTypes.SQUID_INK;
            case LIGHT -> ParticleTypes.END_ROD;
            case SOUND -> ParticleTypes.SCULK_CHARGE_POP;
            case VOID -> ParticleTypes.REVERSE_PORTAL;
            case POISON -> new DustParticleOptions(this.color, 1.2f);
            case STEEL -> ParticleTypes.CRIT;
            case WIND -> ParticleTypes.CLOUD;
        };
    }

    public ParticleOptions dust(float size) {
        return new DustParticleOptions(this.color, size);
    }

    /** Der Klang eines Einschlags. */
    public SoundEvent impactSound() {
        return switch (this) {
            case FIRE -> SoundEvents.FIRECHARGE_USE;
            case ICE -> SoundEvents.GLASS_BREAK;
            case LIGHTNING -> SoundEvents.LIGHTNING_BOLT_IMPACT;
            case NATURE -> SoundEvents.CAVE_VINES_PLACE;
            case EARTH -> SoundEvents.MACE_SMASH_GROUND;
            case WATER -> SoundEvents.GENERIC_SPLASH;
            case SHADOW, VOID -> SoundEvents.ENDERMAN_TELEPORT;
            case LIGHT -> SoundEvents.AMETHYST_BLOCK_RESONATE;
            case SOUND -> SoundEvents.WARDEN_SONIC_BOOM;
            case POISON -> SoundEvents.PUFFER_FISH_BLOW_OUT;
            case STEEL -> SoundEvents.ANVIL_LAND;
            case WIND -> SoundEvents.BREEZE_WIND_CHARGE_BURST.value();
        };
    }

    /**
     * Ein Einschlag des Elements an einem Punkt am Boden (sichtbar, ohne Schaden): Feuer- und Wassersäulen, Eiskristalle,
     * Blitzbögen vom Himmel, Dornenranken, Felsspitzen, Lichtsäulen, Klangringe, kleine schwarze Löcher.
     */
    public void impact(ServerLevel level, Vec3 at, float size) {
        switch (this) {
            case FIRE, WATER -> SpellFxEntity.create(level, SpellFxEntity.Kind.PILLAR, at, this.dragon, this.color, 14, size).param(5.0f).spawn();
            case ICE -> SpellFxEntity.create(level, SpellFxEntity.Kind.CRYSTAL, at, this.dragon, this.color, 26, size * 0.9f).spawn();
            case LIGHTNING -> SpellFxEntity.create(level, SpellFxEntity.Kind.ARC, at.add(0.0, 14.0, 0.0), this.dragon, this.color, 6, 0.5f).end(at).spawn();
            case NATURE -> SpellFxEntity.create(level, SpellFxEntity.Kind.VINE, at, this.dragon, this.color, 16, size).param(0.35f).spawn();
            case EARTH, STEEL -> SpellFxEntity.create(level, SpellFxEntity.Kind.SPIKE, at, this.dragon, this.color, 18, size).spawn();
            case SOUND -> SpellFxEntity.create(level, SpellFxEntity.Kind.ECHO, at.add(0.0, 0.05, 0.0), null, this.color, 14, 0.4f).param(0.25f).spawn();
            case VOID -> SpellFxEntity.create(level, SpellFxEntity.Kind.VORTEX, at.add(0.0, 0.6, 0.0), null, this.color, 14, size * 0.6f).spawn();
            case WIND -> SpellFxEntity.create(level, SpellFxEntity.Kind.SHOCKWAVE, at.add(0.0, 0.05, 0.0), null, this.color, 8, 0.4f).param(0.22f).spawn();
            default -> SpellFxEntity.create(level, SpellFxEntity.Kind.PILLAR, at, null, this.color, 14, size).param(5.0f).spawn();
        }
        level.sendParticles(this.particle(), at.x, at.y + 0.4, at.z, 16, size * 0.4, 0.4, size * 0.4, 0.06);
    }

    /** Was ein Treffer zusätzlich anrichtet. */
    public void afflict(LivingEntity victim, LivingEntity boss) {
        switch (this) {
            case FIRE -> victim.igniteForSeconds(4.0f);
            case ICE -> {
                victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1), boss);
                victim.setTicksFrozen(Math.min(victim.getTicksRequiredToFreeze() + 80, victim.getTicksFrozen() + 100));
            }
            case LIGHTNING -> victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 20, 3), boss);
            case NATURE, POISON -> victim.addEffect(new MobEffectInstance(MobEffects.POISON, 80, this == POISON ? 1 : 0), boss);
            case EARTH -> victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 0), boss);
            case WATER -> victim.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 60, 0), boss);
            case SHADOW -> {
                victim.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 70, 0), boss);
                victim.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0), boss);
            }
            case LIGHT -> {
                victim.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0), boss);
                victim.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 20, 0), boss);
            }
            case SOUND -> victim.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 80, 0), boss);
            case VOID -> victim.addEffect(new MobEffectInstance(MobEffects.WITHER, 40, 0), boss);
            case STEEL -> victim.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0), boss);
            case WIND -> {
            }
        }
    }
}
