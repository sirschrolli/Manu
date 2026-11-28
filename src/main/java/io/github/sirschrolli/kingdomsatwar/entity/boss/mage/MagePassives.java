package io.github.sirschrolli.kingdomsatwar.entity.boss.mage;

import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossEffects;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossFx;
import io.github.sirschrolli.kingdomsatwar.entity.boss.DragonBossEntity;
import io.github.sirschrolli.kingdomsatwar.magic.fx.SpellFxEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModParticles;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * Die besonderen Fähigkeiten der Erzmagier – jeder kämpft anders:
 * <ul>
 *   <li>Ignatius – Glutaura: wer ihm zu nahe kommt, fängt Feuer.</li>
 *   <li>Isolde – Frostpanzer: ein Kranz aus Eisklingen fängt Schaden ab und wächst nach; wer zuschlägt, erstarrt.</li>
 *   <li>Fulgor – Statische Ladung: wer ihn im Nahkampf trifft, bekommt einen Schlag; er blinzelt doppelt so oft.</li>
 *   <li>Sylvana – Lebenssaft: heilt sich, wenn man ihr eine Weile nichts tut.</li>
 *   <li>Gorvan – Steinhaut: nimmt ein Viertel weniger Schaden.</li>
 *   <li>Marena – Gezeitenschild: Pfeile und Geschosse gleiten oft an einer Wasserhülle ab.</li>
 * </ul>
 * Dazu kommen die Fähigkeiten aus {@link MageGifts} (Erneuerung, Wiedergeburt, Eiskokon, Blitzableiter, Dornenhaut,
 * Erdwacht, Gegenwelle).
 */
final class MagePassives {
    /** Frostpanzer: so viel Schaden fängt er ab (wird mit der Phase stärker). */
    static final float WARD = 26.0f;
    /** So viele Eisklingen zeigt der volle Frostpanzer. */
    static final int WARD_SHARDS = 6;
    /** Nach dem Zerbrechen wächst der Frostpanzer nach so vielen Ticks nach. */
    private static final int WARD_REGROW = 360;

    private MagePassives() {}

    /** Jeden Kampf-Tick. */
    static void tick(ServerLevel level, MageBossEntity mage) {
        switch (mage.element()) {
            case FIRE -> emberAura(level, mage);
            case ICE -> regrowWard(level, mage);
            case NATURE -> lifeSap(level, mage);
            default -> {
            }
        }
    }

    /**
     * Ein Treffer kommt an: Steinhaut, Frostpanzer und Gezeitenschild ändern den Schaden, die statische Ladung schlägt
     * zurück. @return der verbleibende Schaden (0: ganz abgefangen)
     */
    static float onHurt(ServerLevel level, MageBossEntity mage, DamageSource source, float damage) {
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return damage;
        }
        Entity attacker = source.getEntity();
        boolean melee = attacker instanceof LivingEntity && source.getDirectEntity() == attacker && attacker.distanceToSqr(mage) < 5.0 * 5.0;
        switch (mage.element()) {
            case EARTH -> {
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.TUFF.defaultBlockState()), mage.getX(), mage.getY(1.0),
                        mage.getZ(), 10, 0.4, 0.6, 0.4, 0.1);
                MageGifts.earthward(level, mage, source);
                return damage * 0.75f;
            }
            case WATER -> {
                if (source.getDirectEntity() instanceof Projectile && mage.getRandom().nextFloat() < 0.4f) {
                    SpellFxEntity.create(level, SpellFxEntity.Kind.BUBBLE, mage.position(), DragonType.WATER, 0x7FE0FF, 8, 3.4f).anchor(mage).spawn();
                    BossFx.sound(level, mage.position(), SoundEvents.GENERIC_SPLASH, 1.0f, 1.6f);
                    return 0.0f;
                }
                if (melee) {
                    MageGifts.counterWave(level, mage);
                }
                return damage;
            }
            case NATURE -> {
                if (melee) {
                    MageGifts.thorns(level, mage, (LivingEntity) attacker, damage);
                }
                return damage;
            }
            case ICE -> {
                if (melee) {
                    BossEffects.freeze((LivingEntity) attacker, 2);
                    ((LivingEntity) attacker).addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 1, false, false, true), mage);
                }
                float ward = mage.ward();
                if (ward <= 0.0f) {
                    return damage;
                }
                float left = ward - damage;
                mage.setWard(Math.max(0.0f, left));
                BossFx.sound(level, mage.position(), SoundEvents.GLASS_HIT, 1.2f, 1.4f + mage.getRandom().nextFloat() * 0.3f);
                if (left <= 0.0f) {
                    mage.wardRegrow = WARD_REGROW;
                    BossFx.sound(level, mage.position(), SoundEvents.GLASS_BREAK, 1.8f, 0.8f);
                    level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.PACKED_ICE.defaultBlockState()), mage.getX(),
                            mage.getY(1.0), mage.getZ(), 40, 0.8, 0.8, 0.8, 0.15);
                    return -left;
                }
                return 0.0f;
            }
            case LIGHTNING -> {
                if (melee && mage.tickCount - mage.lastShock > 10) {
                    mage.lastShock = mage.tickCount;
                    LivingEntity victim = (LivingEntity) attacker;
                    Vec3 from = mage.position().add(0, 1.6, 0);
                    SpellFxEntity.create(level, SpellFxEntity.Kind.ARC, from, DragonType.LIGHTNING, 0xFFF06A, 6, 0.2f)
                            .end(victim.getBoundingBox().getCenter()).spawn();
                    mage.hurtTarget(level, victim, 3.0f);
                    BossEffects.stun(victim, 1);
                    DragonBossEntity.pushAway(victim, mage.position(), 0.8, 0.3);
                    BossFx.sound(level, victim.position(), SoundEvents.LIGHTNING_BOLT_IMPACT, 0.8f, 1.9f);
                }
                return damage;
            }
            default -> {
                return damage;
            }
        }
    }

    // ------------------------------------------------------------------ Feuer

    private static void emberAura(ServerLevel level, MageBossEntity mage) {
        if (mage.tickCount % 10 == 0) {
            BossFx.ring(level, ModParticles.BOSS_FLAME.get(), mage.position().add(0, 0.15, 0), 3.2, 16, 0.0);
        }
        if (mage.tickCount % 20 != 0) {
            return;
        }
        for (LivingEntity e : mage.enemiesAround(mage.position(), 3.2, 3.0)) {
            if (mage.hurtTarget(level, e, 1.5f)) {
                BossEffects.burn(e, 2);
            }
        }
    }

    // ------------------------------------------------------------------ Eis

    private static void regrowWard(ServerLevel level, MageBossEntity mage) {
        if (mage.ward() > 0.0f) {
            return;
        }
        if (mage.wardRegrow > 0) {
            mage.wardRegrow--;
            return;
        }
        mage.setWard(maxWard(mage));
        BossFx.sound(level, mage.position(), SoundEvents.AMETHYST_BLOCK_RESONATE, 1.6f, 1.6f);
        BossFx.sound(level, mage.position(), SoundEvents.GLASS_PLACE, 1.4f, 1.2f);
        level.sendParticles(ParticleTypes.SNOWFLAKE, mage.getX(), mage.getY(1.0), mage.getZ(), 30, 1.0, 0.8, 1.0, 0.05);
    }

    static float maxWard(MageBossEntity mage) {
        return WARD * mage.damageMultiplier();
    }

    // ------------------------------------------------------------------ Natur

    private static void lifeSap(ServerLevel level, MageBossEntity mage) {
        if (mage.tickCount % 20 != 0 || mage.tickCount - mage.getLastHurtByMobTimestamp() < 80 || mage.getHealth() >= mage.getMaxHealth()) {
            return;
        }
        mage.heal(mage.getMaxHealth() * 0.007f);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, mage.getX(), mage.getY(0.6), mage.getZ(), 4, 0.5, 0.7, 0.5, 0.0);
        if (mage.getRandom().nextInt(3) == 0) {
            level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, mage.getX(), mage.getY(1.0), mage.getZ(), 6, 0.8, 0.8, 0.8, 0.0);
        }
    }

    /** Wie viele Eisklingen der Frostpanzer gerade zeigt. */
    static int shards(MageBossEntity mage) {
        float max = maxWard(mage);
        return max <= 0.0f ? 0 : Mth.ceil(mage.ward() / max * WARD_SHARDS);
    }
}
