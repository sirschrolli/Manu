package io.github.sirschrolli.kingdomsatwar.entity.boss.mage;

import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossEffects;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossFx;
import io.github.sirschrolli.kingdomsatwar.magic.fx.SpellFxEntity;
import io.github.sirschrolli.kingdomsatwar.magic.fx.SpellProjectile;
import io.github.sirschrolli.kingdomsatwar.registry.ModParticles;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Die beiden Meisterzauber jedes Erzmagiers – seine Handschrift, ab der ersten Kampfphase:
 *
 * <ul>
 *   <li>Ignatius (Feuer): Phönixflug – Feuervögel jagen ihre Ziele; Flammensäulen aus glühenden Kreisen.</li>
 *   <li>Isolde (Eis): Frostlanzen im Fächer; Kristallkerker – wer im Kreis bleibt, erstarrt im Eis.</li>
 *   <li>Fulgor (Blitz): Kugelblitze kreisen und schlagen zu; ein Blitzstrahl, der überspringt.</li>
 *   <li>Sylvana (Natur): Dornenranken brechen aus dem Boden; die Giftblüte stößt Sporen aus und nährt Sylvana.</li>
 *   <li>Gorvan (Erde): Felsspitzen in einer Linie; die Steinfaust schleudert in die Luft.</li>
 *   <li>Marena (Wasser): Flutwelle rollt durch den Saal; der Blasenkerker hebt empor und lässt ertrinken.</li>
 * </ul>
 *
 * <p>Jeder Einschlag am Boden kündigt sich mit einem Runenkreis an, der sich füllt – wer ihn verlässt, entgeht dem
 * Zauber.
 */
final class MageSignatures {
    private MageSignatures() {}

    static int duration(DragonType element, boolean trap) {
        if (trap) {
            return switch (element) {
                case FIRE -> 56;
                case ICE -> 24;
                case LIGHTNING -> 58;
                case NATURE -> 22;
                case EARTH -> 34;
                case WATER -> 20;
            };
        }
        return switch (element) {
            case FIRE -> 34;
            case ICE -> 28;
            case LIGHTNING -> 40;
            case NATURE -> 28;
            case EARTH -> 50;
            case WATER -> 36;
        };
    }

    static void strike(ServerLevel level, MageBossEntity mage, LivingEntity target, int t) {
        switch (mage.element()) {
            case FIRE -> phoenix(level, mage, target, t);
            case ICE -> frostLances(level, mage, target, t);
            case LIGHTNING -> ballLightning(level, mage, target, t);
            case NATURE -> thornVines(level, mage, t);
            case EARTH -> stoneSpikes(level, mage, target, t);
            case WATER -> tidalWave(level, mage, target, t);
        }
    }

    static void trap(ServerLevel level, MageBossEntity mage, LivingEntity target, int t) {
        switch (mage.element()) {
            case FIRE -> flamePillars(level, mage, t);
            case ICE -> crystalPrison(level, mage, target, t);
            case LIGHTNING -> arcBeam(level, mage, target, t);
            case NATURE -> poisonBloom(level, mage, target, t);
            case EARTH -> stoneFist(level, mage, target, t);
            case WATER -> bubblePrison(level, mage, target, t);
        }
    }

    // =====================================================================================
    // Hilfen
    // =====================================================================================

    /** Gegner im Saal, das Ziel zuerst, höchstens {@code max}. */
    private static List<LivingEntity> victims(MageBossEntity mage, LivingEntity target, int max) {
        List<LivingEntity> list = new ArrayList<>();
        list.add(target);
        for (LivingEntity e : mage.arenaEnemies()) {
            if (list.size() >= max) {
                break;
            }
            if (e != target) {
                list.add(e);
            }
        }
        return list;
    }

    /** Wo das Wesen gleich steht (ein Stück voraus in Laufrichtung), auf dem Boden. */
    private static Vec3 lead(ServerLevel level, LivingEntity e, double ticks) {
        Vec3 v = e.getDeltaMovement();
        Vec3 at = e.position().add(v.x * ticks, 0.0, v.z * ticks);
        return ground(level, at);
    }

    /** Die Oberfläche unter (oder knapp über) einer Stelle. */
    static Vec3 ground(ServerLevel level, Vec3 at) {
        BlockPos.MutableBlockPos pos = BlockPos.containing(at.x, at.y + 1.5, at.z).mutable();
        for (int i = 0; i < 8; i++) {
            if (!level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) {
                return new Vec3(at.x, pos.getY() + level.getBlockState(pos).getCollisionShape(level, pos).max(net.minecraft.core.Direction.Axis.Y),
                        at.z);
            }
            pos.move(0, -1, 0);
        }
        return at;
    }

    private static void sound(ServerLevel level, Vec3 at, net.minecraft.sounds.SoundEvent sound, float volume, float pitch) {
        BossFx.sound(level, at, sound, volume, pitch);
    }

    private static void sound(ServerLevel level, Vec3 at, net.minecraft.core.Holder<net.minecraft.sounds.SoundEvent> sound, float volume,
            float pitch) {
        BossFx.sound(level, at, sound.value(), volume, pitch);
    }

    /** Kraft sammeln: Funken der eigenen Farbe wirbeln um den erhobenen Stab. */
    private static void gather(ServerLevel level, MageBossEntity mage, int t) {
        Vec3 tip = mage.staffTip();
        for (int k = 0; k < 2; k++) {
            double a = t * 0.6 + k * Mth.PI;
            level.sendParticles(new DustParticleOptions(mage.element().color(), 1.2f), tip.x + Math.cos(a) * 0.6, tip.y + Math.sin(t * 0.3) * 0.2,
                    tip.z + Math.sin(a) * 0.6, 1, 0, 0, 0, 0);
        }
    }

    /** Wer im Umkreis des Einschlags steht (Gegner des Magiers). */
    private static List<LivingEntity> hitAt(MageBossEntity mage, Vec3 at, double radius, double height) {
        List<LivingEntity> list = new ArrayList<>();
        for (LivingEntity e : mage.enemiesAround(at, radius, height + 1.0)) {
            if (e.getY() > at.y - 1.0 && e.getY() < at.y + height) {
                list.add(e);
            }
        }
        return list;
    }

    // =====================================================================================
    // Feuer – Ignatius
    // =====================================================================================

    /** Phönixflug: ein Feuervogel je Phase, jeder auf ein eigenes Ziel. */
    private static void phoenix(ServerLevel level, MageBossEntity mage, LivingEntity target, int t) {
        if (t == 2) {
            sound(level, mage.position(), SoundEvents.EVOKER_PREPARE_ATTACK, 1.8f, 0.7f);
            sound(level, mage.position(), SoundEvents.BLAZE_AMBIENT, 1.6f, 0.6f);
        }
        if (t < 16) {
            gather(level, mage, t);
            level.sendParticles(ParticleTypes.SMALL_FLAME, mage.staffTip().x, mage.staffTip().y, mage.staffTip().z, 2, 0.15, 0.15, 0.15, 0.01);
        }
        if (t == 16) {
            List<LivingEntity> targets = victims(mage, target, mage.phase());
            Vec3 tip = mage.staffTip();
            for (int i = 0; i < mage.phase(); i++) {
                LivingEntity goal = targets.get(i % targets.size());
                Vec3 flat = goal.position().subtract(mage.position()).multiply(1, 0, 1);
                if (flat.lengthSqr() < 1.0e-3) {
                    flat = Vec3.directionFromRotation(0.0f, mage.getYRot());
                }
                float side = mage.phase() == 1 ? 0.0f : (i - (mage.phase() - 1) / 2.0f) * 50.0f;
                Vec3 dir = flat.normalize().yRot(side * Mth.DEG_TO_RAD).add(0, 0.4, 0);
                SpellProjectile.shoot(level, mage, SpellProjectile.Kind.PHOENIX, tip, dir, 0.5, 9.0f * mage.damageMultiplier(), goal, 1);
            }
            sound(level, tip, SoundEvents.BLAZE_SHOOT, 2.0f, 0.55f);
            sound(level, tip, SoundEvents.PHANTOM_FLAP, 2.0f, 0.6f);
            BossFx.impact(level, ParticleTypes.FLAME, tip, 24, 0.4, 0.4, 0.4, 0.08);
        }
    }

    /** Flammensäulen: glühende Kreise unter allen Gegnern – in der letzten Phase eine zweite Welle. */
    private static void flamePillars(ServerLevel level, MageBossEntity mage, int t) {
        if (t == 0) {
            sound(level, mage.position(), SoundEvents.EVOKER_PREPARE_SUMMON, 1.8f, 0.8f);
        }
        if (t == 4 || (t == 30 && mage.phase() >= 3)) {
            List<Vec3> spots = new ArrayList<>();
            for (LivingEntity e : mage.arenaEnemies()) {
                if (spots.size() >= 5) {
                    break;
                }
                spots.add(lead(level, e, 10));
                if (mage.phase() >= 2) {
                    double a = mage.getRandom().nextDouble() * Mth.TWO_PI;
                    spots.add(ground(level, e.position().add(Math.cos(a) * 3.0, 0, Math.sin(a) * 3.0)));
                }
            }
            for (Vec3 spot : spots) {
                flameCircle(level, mage, spot);
            }
            sound(level, mage.position(), SoundEvents.FIRECHARGE_USE, 1.6f, 0.6f);
        }
    }

    private static void flameCircle(ServerLevel level, MageBossEntity mage, Vec3 spot) {
        float damage = 10.0f * mage.damageMultiplier();
        SpellFxEntity.create(level, SpellFxEntity.Kind.CIRCLE, spot.add(0, 0.02, 0), DragonType.FIRE, 26, 1.8f).onExpire(fx -> {
            SpellFxEntity.create(level, SpellFxEntity.Kind.PILLAR, spot, DragonType.FIRE, 0xFF7A22, 26, 1.35f).param(6.5f).spawn();
            sound(level, spot, SoundEvents.BLAZE_SHOOT, 1.6f, 0.5f + mage.getRandom().nextFloat() * 0.2f);
            sound(level, spot, SoundEvents.FIRECHARGE_USE, 1.2f, 0.7f);
            for (LivingEntity e : hitAt(mage, spot, 1.9, 5.0)) {
                if (mage.hurtTarget(level, e, damage)) {
                    BossEffects.burn(e, 5);
                    e.push(0.0, 0.55, 0.0);
                    e.needsSync = true;
                }
            }
        }).spawn();
    }

    // =====================================================================================
    // Eis – Isolde
    // =====================================================================================

    /** Frostlanzen: ein Fächer aus Eisspeeren auf das Ziel. */
    private static void frostLances(ServerLevel level, MageBossEntity mage, LivingEntity target, int t) {
        if (t == 2) {
            sound(level, mage.position(), SoundEvents.AMETHYST_BLOCK_RESONATE, 1.8f, 1.4f);
        }
        if (t < 14) {
            gather(level, mage, t);
            Vec3 tip = mage.staffTip();
            level.sendParticles(ParticleTypes.SNOWFLAKE, tip.x, tip.y, tip.z, 3, 0.3, 0.3, 0.3, 0.02);
        }
        if (t == 14) {
            Vec3 tip = mage.staffTip();
            Vec3 aim = target.getBoundingBox().getCenter().add(target.getDeltaMovement().scale(6)).subtract(tip);
            int count = mage.phase() >= 3 ? 7 : 5;
            float spread = mage.phase() >= 3 ? 48.0f : 36.0f;
            for (int i = 0; i < count; i++) {
                float yaw = (i / (float) (count - 1) - 0.5f) * spread;
                Vec3 dir = aim.normalize().yRot(yaw * Mth.DEG_TO_RAD);
                SpellProjectile.shoot(level, mage, SpellProjectile.Kind.ICE_LANCE, tip, dir, 1.45, 6.0f * mage.damageMultiplier(), null, 1);
            }
            sound(level, tip, SoundEvents.TRIDENT_THROW.value(), 2.0f, 1.3f);
            sound(level, tip, SoundEvents.GLASS_BREAK, 1.4f, 1.8f);
        }
    }

    /** Kristallkerker: Wer im Kreis bleibt, erstarrt im Eis und wird beim Zerspringen getroffen. */
    private static void crystalPrison(ServerLevel level, MageBossEntity mage, LivingEntity target, int t) {
        if (t != 2) {
            return;
        }
        sound(level, mage.position(), SoundEvents.EVOKER_PREPARE_ATTACK, 1.6f, 1.3f);
        for (LivingEntity victim : victims(mage, target, mage.phase() >= 3 ? 2 : 1)) {
            Vec3 spot = ground(level, victim.position());
            SpellFxEntity.create(level, SpellFxEntity.Kind.CIRCLE, spot.add(0, 0.02, 0), DragonType.ICE, 18, 1.5f).onExpire(fx -> {
                if (!victim.isAlive() || victim.position().subtract(spot).horizontalDistanceSqr() > 1.7 * 1.7) {
                    level.sendParticles(ParticleTypes.SNOWFLAKE, spot.x, spot.y + 0.5, spot.z, 30, 0.8, 0.4, 0.8, 0.05);
                    sound(level, spot, SoundEvents.POWDER_SNOW_BREAK, 1.2f, 1.2f);
                    return;
                }
                encase(level, mage, victim);
            }).spawn();
        }
    }

    private static void encase(ServerLevel level, MageBossEntity mage, LivingEntity victim) {
        Vec3 at = victim.position();
        sound(level, at, SoundEvents.GLASS_PLACE, 1.6f, 0.6f);
        sound(level, at, SoundEvents.PLAYER_HURT_FREEZE, 1.2f, 0.8f);
        float damage = 7.0f * mage.damageMultiplier();
        SpellFxEntity.create(level, SpellFxEntity.Kind.PRISON, at, DragonType.ICE, 64, Math.max(1.0f, victim.getBbHeight() / 1.9f)).onTick(fx -> {
            if (!victim.isAlive()) {
                return;
            }
            // festgefroren: keine Bewegung, kein Sprung aus dem Eis
            victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 6, 7, false, false, true));
            victim.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 6, 2, false, false, true));
            if (victim.position().distanceToSqr(at) > 0.04) {
                victim.teleportTo(at.x, Math.min(victim.getY(), at.y + 0.2), at.z);
            }
            victim.setTicksFrozen(Math.max(victim.getTicksFrozen(), victim.getTicksRequiredToFreeze() + 40));
        }).onExpire(fx -> {
            sound(level, at, SoundEvents.GLASS_BREAK, 2.0f, 0.7f);
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.PACKED_ICE.defaultBlockState()), at.x, at.y + 1.0, at.z, 40, 0.6,
                    0.9, 0.6, 0.15);
            if (victim.isAlive() && victim.position().distanceToSqr(at) < 4.0) {
                mage.hurtTarget(level, victim, damage);
                BossEffects.freeze(victim, 3);
            }
        }).spawn();
    }

    // =====================================================================================
    // Blitz – Fulgor
    // =====================================================================================

    /** Kugelblitze: kreisen erst um Fulgor, dann jagen sie los. */
    private static void ballLightning(ServerLevel level, MageBossEntity mage, LivingEntity target, int t) {
        if (t == 2) {
            sound(level, mage.position(), SoundEvents.BEACON_ACTIVATE, 1.6f, 1.8f);
        }
        if (t == 8) {
            int count = mage.phase() >= 3 ? 5 : 3;
            List<LivingEntity> targets = victims(mage, target, count);
            for (int i = 0; i < count; i++) {
                LivingEntity goal = targets.get(i % targets.size());
                float angle = i * Mth.TWO_PI / count;
                Vec3 from = mage.position().add(Math.cos(angle) * 2.0, 1.8, Math.sin(angle) * 2.0);
                MagicMissile orb = MagicMissile.shoot(level, mage, goal, from, new Vec3(0, 0.01, 0), 0.62, 5.5f * mage.damageMultiplier());
                orb.orbit(mage, 26 + i * 4, angle);
            }
            sound(level, mage.position(), SoundEvents.LIGHTNING_BOLT_THUNDER, 1.2f, 1.7f);
        }
    }

    /** Blitzstrahl: ein knisternder Bogen aus dem Stab – solange das Ziel in Sicht ist, und er springt über. */
    private static void arcBeam(ServerLevel level, MageBossEntity mage, LivingEntity target, int t) {
        MageBossEntity.SignatureState s = mage.signature;
        if (t == 0) {
            sound(level, mage.position(), SoundEvents.BEACON_POWER_SELECT, 1.8f, 1.6f);
        }
        if (t < 8) {
            gather(level, mage, t);
            return;
        }
        Vec3 tip = mage.staffTip();
        Vec3 aim = target.getBoundingBox().getCenter();
        boolean sees = target.isAlive() && aim.distanceToSqr(tip) < 22.0 * 22.0 && level.clip(new ClipContext(tip, aim, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, mage)).getType() == HitResult.Type.MISS;
        if (!sees) {
            if (s.beam != null) {
                s.beam.discard();
                s.beam = null;
            }
            if (s.beam2 != null) {
                s.beam2.discard();
                s.beam2 = null;
            }
            return;
        }
        if (s.beam == null || s.beam.isRemoved()) {
            s.beam = SpellFxEntity.create(level, SpellFxEntity.Kind.ARC, tip, DragonType.LIGHTNING, 0xFFF06A, 60 - t, 0.35f).end(aim).spawn();
            sound(level, tip, SoundEvents.LIGHTNING_BOLT_IMPACT, 1.4f, 1.8f);
        }
        s.beam.setPos(tip.x, tip.y, tip.z);
        s.beam.moveEnd(aim);
        LivingEntity chained = null;
        if (mage.phase() >= 2) {
            for (LivingEntity e : mage.enemiesAround(target.position(), 7.0, 4.0)) {
                if (e != target) {
                    chained = e;
                    break;
                }
            }
        }
        if (chained != null) {
            Vec3 next = chained.getBoundingBox().getCenter();
            if (s.beam2 == null || s.beam2.isRemoved()) {
                s.beam2 = SpellFxEntity.create(level, SpellFxEntity.Kind.ARC, aim, DragonType.LIGHTNING, 0xFFF06A, 60 - t, 0.25f).end(next).spawn();
            }
            s.beam2.setPos(aim.x, aim.y, aim.z);
            s.beam2.moveEnd(next);
        } else if (s.beam2 != null) {
            s.beam2.discard();
            s.beam2 = null;
        }
        if (t % 4 == 0) {
            mage.hurtTarget(level, target, 2.4f);
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, aim.x, aim.y, aim.z, 8, 0.3, 0.4, 0.3, 0.1);
            if (chained != null) {
                mage.hurtTarget(level, chained, 1.6f);
            }
            if (t % 12 == 0) {
                BossEffects.stun(target, 1);
                sound(level, aim, SoundEvents.LIGHTNING_BOLT_IMPACT, 0.7f, 2.0f);
            }
        }
    }

    // =====================================================================================
    // Natur – Sylvana
    // =====================================================================================

    /** Dornenranken: unter jedem Gegner ein Kreis, dann peitschen Ranken aus dem Boden. */
    private static void thornVines(ServerLevel level, MageBossEntity mage, int t) {
        if (t == 0) {
            sound(level, mage.position(), SoundEvents.EVOKER_PREPARE_ATTACK, 1.6f, 0.9f);
        }
        if (t != 4) {
            return;
        }
        float damage = 7.0f * mage.damageMultiplier();
        List<Vec3> spots = new ArrayList<>();
        for (LivingEntity e : mage.arenaEnemies()) {
            if (spots.size() >= 6) {
                break;
            }
            spots.add(lead(level, e, 8));
            if (mage.phase() >= 3) {
                double a = mage.getRandom().nextDouble() * Mth.TWO_PI;
                spots.add(ground(level, e.position().add(Math.cos(a) * 2.2, 0, Math.sin(a) * 2.2)));
            }
        }
        for (Vec3 spot : spots) {
            SpellFxEntity.create(level, SpellFxEntity.Kind.CIRCLE, spot.add(0, 0.02, 0), DragonType.NATURE, 20, 1.5f).onExpire(fx -> {
                // drei Ranken brechen rings um die Stelle hervor und peitschen nach innen
                float start = mage.getRandom().nextFloat() * 360.0f;
                for (int k = 0; k < 3; k++) {
                    float angle = start + k * 120.0f;
                    Vec3 at = ground(level, spot.add(-Mth.sin(angle * Mth.DEG_TO_RAD) * 1.0, 0, Mth.cos(angle * Mth.DEG_TO_RAD) * 1.0));
                    SpellFxEntity.create(level, SpellFxEntity.Kind.VINE, at, DragonType.NATURE, 44, 1.15f + mage.getRandom().nextFloat() * 0.2f)
                            .param(0.25f + k * 0.06f).facing(angle).spawn();     // der Peitschenhieb geht nach innen
                }
                sound(level, spot, SoundEvents.ROOTED_DIRT_BREAK, 1.6f, 0.6f);
                sound(level, spot, SoundEvents.SWEET_BERRY_BUSH_PLACE, 1.4f, 0.7f);
                for (LivingEntity e : hitAt(mage, spot, 1.7, 3.5)) {
                    if (mage.hurtTarget(level, e, damage)) {
                        BossEffects.poison(e, 3);
                        e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 4, false, false, true), mage);
                        e.push(0.0, 0.35, 0.0);
                        e.needsSync = true;
                    }
                }
            }).spawn();
        }
    }

    /** Giftblüte: wächst beim Ziel, öffnet sich und stößt Sporen aus – und nährt Sylvana, solange sie blüht. */
    private static void poisonBloom(ServerLevel level, MageBossEntity mage, LivingEntity target, int t) {
        if (t == 0) {
            sound(level, mage.position(), SoundEvents.EVOKER_PREPARE_SUMMON, 1.6f, 1.1f);
        }
        if (t != 6) {
            return;
        }
        int count = mage.phase() >= 3 ? 2 : 1;
        List<LivingEntity> targets = victims(mage, target, count);
        for (int i = 0; i < count; i++) {
            LivingEntity goal = targets.get(i % targets.size());
            double a = mage.getRandom().nextDouble() * Mth.TWO_PI;
            Vec3 spot = ground(level, goal.position().add(Math.cos(a) * 2.0, 0, Math.sin(a) * 2.0));
            bloom(level, mage, spot);
        }
    }

    private static void bloom(ServerLevel level, MageBossEntity mage, Vec3 spot) {
        float damage = 1.6f * mage.damageMultiplier();
        sound(level, spot, SoundEvents.ROOTED_DIRT_BREAK, 1.6f, 0.8f);
        sound(level, spot, SoundEvents.AZALEA_LEAVES_PLACE, 1.6f, 0.6f);
        SpellFxEntity.create(level, SpellFxEntity.Kind.FLOWER, spot, DragonType.NATURE, 150, 1.45f).onTick(fx -> {
            int age = fx.tickCount;
            if (age < 24 || age > 140 || age % 10 != 0) {
                return;
            }
            level.sendParticles(ModParticles.BOSS_SPORE.get(), spot.x, spot.y + 2.6, spot.z, 6, 0.9, 0.4, 0.9, 0.02);
            if (age % 30 == 0) {
                sound(level, spot, SoundEvents.PUFFER_FISH_BLOW_OUT, 1.2f, 0.6f);
            }
            for (LivingEntity e : hitAt(mage, spot, 4.2, 3.5)) {
                if (mage.hurtTarget(level, e, damage)) {
                    BossEffects.poison(e, 3);
                }
            }
            if (mage.isAlive() && mage.distanceToSqr(spot) < 14.0 * 14.0 && mage.getHealth() < mage.getMaxHealth()) {
                mage.heal(mage.getMaxHealth() * 0.006f);
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER, mage.getX(), mage.getY(1.0), mage.getZ(), 3, 0.4, 0.6, 0.4, 0.0);
            }
        }).spawn();
    }

    // =====================================================================================
    // Erde – Gorvan
    // =====================================================================================

    /** Felsspitzen: brechen eine nach der anderen in einer Linie zum Ziel aus dem Boden (zuletzt in drei Linien). */
    private static void stoneSpikes(ServerLevel level, MageBossEntity mage, LivingEntity target, int t) {
        MageBossEntity.SignatureState s = mage.signature;
        if (t == 2) {
            sound(level, mage.position(), SoundEvents.RAVAGER_ROAR, 1.2f, 0.6f);
        }
        if (t < 10) {
            gather(level, mage, t);
            return;
        }
        if (t == 10) {
            Vec3 flat = target.position().subtract(mage.position()).multiply(1, 0, 1);
            s.dir = flat.lengthSqr() < 1.0e-3 ? Vec3.directionFromRotation(0.0f, mage.getYRot()) : flat.normalize();
            s.hits.clear();
            mage.shake(level, 24.0, 0.4f, 30);
        }
        int i = (t - 10) / 2;
        if ((t - 10) % 2 != 0 || i >= 16 || s.dir == null) {
            return;
        }
        float damage = 9.0f * mage.damageMultiplier();
        float[] lines = mage.phase() >= 3 ? new float[] {-20.0f, 0.0f, 20.0f} : new float[] {0.0f};
        for (float yaw : lines) {
            Vec3 dir = s.dir.yRot(yaw * Mth.DEG_TO_RAD);
            Vec3 at = mage.position().add(dir.scale(1.8 + i * 1.3)).add((mage.getRandom().nextDouble() - 0.5) * 0.4, 0,
                    (mage.getRandom().nextDouble() - 0.5) * 0.4);
            Vec3 spot = ground(level, at);
            if (Math.abs(spot.y - mage.getY()) > 3.0) {
                continue;
            }
            SpellFxEntity.create(level, SpellFxEntity.Kind.SPIKE, spot, DragonType.EARTH, 30, 0.85f + mage.getRandom().nextFloat() * 0.35f).spawn();
            if (i % 2 == 0) {
                sound(level, spot, SoundEvents.POINTED_DRIPSTONE_LAND, 1.3f, 0.5f + mage.getRandom().nextFloat() * 0.2f);
            }
            for (LivingEntity e : hitAt(mage, spot, 1.35, 3.0)) {
                if (s.hits.add(e.getId()) && mage.hurtTarget(level, e, damage)) {
                    e.push(0.0, 0.75, 0.0);
                    e.needsSync = true;
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 1, false, false, true), mage);
                }
            }
        }
    }

    /** Steinfaust: aus dem Kreis unter dem Ziel schlägt eine Faust aus Fels und schleudert es in die Luft. */
    private static void stoneFist(ServerLevel level, MageBossEntity mage, LivingEntity target, int t) {
        if (t == 0) {
            sound(level, mage.position(), SoundEvents.EVOKER_PREPARE_ATTACK, 1.6f, 0.6f);
        }
        if (t != 4) {
            return;
        }
        float damage = 12.0f * mage.damageMultiplier();
        for (LivingEntity victim : victims(mage, target, mage.phase() >= 2 ? 2 : 1)) {
            Vec3 spot = lead(level, victim, 6);
            SpellFxEntity.create(level, SpellFxEntity.Kind.CIRCLE, spot.add(0, 0.02, 0), DragonType.EARTH, 22, 2.1f).onExpire(fx -> {
                SpellFxEntity.create(level, SpellFxEntity.Kind.FIST, spot, DragonType.EARTH, 32, 1.25f).facing(mage.getYRot() + 180.0f).spawn();
                sound(level, spot, SoundEvents.GENERIC_EXPLODE.value(), 1.6f, 0.5f);
                sound(level, spot, SoundEvents.ANVIL_LAND, 0.8f, 0.5f);
                mage.shake(level, 18.0, 0.6f, 14);
                for (LivingEntity e : hitAt(mage, spot, 2.3, 3.5)) {
                    if (mage.hurtTarget(level, e, damage)) {
                        e.setDeltaMovement(e.getDeltaMovement().x * 0.3, 1.25, e.getDeltaMovement().z * 0.3);
                        e.needsSync = true;
                        e.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 70, 0, false, false, true), mage);
                    }
                }
            }).spawn();
        }
    }

    // =====================================================================================
    // Wasser – Marena
    // =====================================================================================

    /** Flutwelle: rollt durch den Saal und reißt mit, wen sie erfasst (zuletzt drei Wellen im Fächer). */
    private static void tidalWave(ServerLevel level, MageBossEntity mage, LivingEntity target, int t) {
        if (t == 2) {
            sound(level, mage.position(), SoundEvents.BUBBLE_COLUMN_UPWARDS_INSIDE, 2.0f, 0.6f);
        }
        if (t < 12) {
            gather(level, mage, t);
            return;
        }
        if (t != 12) {
            return;
        }
        Vec3 flat = target.position().subtract(mage.position()).multiply(1, 0, 1);
        Vec3 base = flat.lengthSqr() < 1.0e-3 ? Vec3.directionFromRotation(0.0f, mage.getYRot()) : flat.normalize();
        float[] fan = mage.phase() >= 3 ? new float[] {-40.0f, 0.0f, 40.0f} : new float[] {0.0f};
        float damage = 8.0f * mage.damageMultiplier();
        for (float yaw : fan) {
            Vec3 dir = base.yRot(yaw * Mth.DEG_TO_RAD);
            Vec3 start = ground(level, mage.position().add(dir.scale(1.5)));
            float facing = (float) (Mth.atan2(-dir.x, dir.z) * Mth.RAD_TO_DEG);
            Set<Integer> hit = new HashSet<>();
            float width = 8.0f;
            SpellFxEntity.create(level, SpellFxEntity.Kind.WAVE, start, DragonType.WATER, 0x4FD0F0, 44, width).param(0.5f).facing(facing)
                    .onTick(fx -> {
                        Vec3 fwd = fx.forward();
                        Vec3 side = new Vec3(-fwd.z, 0, fwd.x);
                        for (LivingEntity e : mage.enemiesAround(fx.position(), width * 0.6, 3.0)) {
                            Vec3 rel = e.position().subtract(fx.position());
                            if (Math.abs(rel.dot(fwd)) < 1.3 && Math.abs(rel.dot(side)) < width * 0.5 && hit.add(e.getId())
                                    && mage.hurtTarget(level, e, damage)) {
                                double push = 1.0 - e.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE);
                                e.setDeltaMovement(e.getDeltaMovement().add(fwd.scale(1.4 * push)).add(0, 0.45 * push, 0));
                                e.needsSync = true;
                                e.clearFire();
                                BossEffects.soak(e, 3);
                            }
                        }
                    }).spawn();
        }
        sound(level, mage.position(), SoundEvents.GENERIC_SPLASH, 2.4f, 0.5f);
        sound(level, mage.position(), SoundEvents.PLAYER_SPLASH_HIGH_SPEED, 2.0f, 0.6f);
    }

    /** Blasenkerker: wer im Kreis bleibt, wird in einer Wasserblase emporgehoben und ringt nach Luft. */
    private static void bubblePrison(ServerLevel level, MageBossEntity mage, LivingEntity target, int t) {
        if (t != 2) {
            return;
        }
        sound(level, mage.position(), SoundEvents.EVOKER_PREPARE_ATTACK, 1.6f, 1.2f);
        float damage = 1.8f * mage.damageMultiplier();
        for (LivingEntity victim : victims(mage, target, mage.phase() >= 3 ? 2 : 1)) {
            Vec3 spot = ground(level, victim.position());
            SpellFxEntity.create(level, SpellFxEntity.Kind.CIRCLE, spot.add(0, 0.02, 0), DragonType.WATER, 16, 1.5f).onExpire(fx -> {
                if (!victim.isAlive() || victim.position().subtract(spot).horizontalDistanceSqr() > 1.7 * 1.7) {
                    level.sendParticles(ParticleTypes.SPLASH, spot.x, spot.y + 0.3, spot.z, 30, 0.8, 0.2, 0.8, 0.1);
                    sound(level, spot, SoundEvents.GENERIC_SPLASH, 1.0f, 1.4f);
                    return;
                }
                sound(level, spot, SoundEvents.BUBBLE_COLUMN_UPWARDS_INSIDE, 1.6f, 1.0f);
                victim.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 44, 0, false, false, true), mage);
                victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 3, false, false, true), mage);
                SpellFxEntity.create(level, SpellFxEntity.Kind.BUBBLE, victim.position(), DragonType.WATER, 0x7FE0FF, 60,
                        Math.max(1.6f, victim.getBbHeight() * 1.15f)).anchor(victim).onTick(bubble -> {
                    if (bubble.tickCount % 10 == 5 && victim.isAlive()) {
                        mage.hurtTarget(level, victim, damage);
                        level.sendParticles(ParticleTypes.BUBBLE, victim.getX(), victim.getEyeY(), victim.getZ(), 6, 0.2, 0.2, 0.2, 0.05);
                    }
                }).onExpire(bubble -> {
                    victim.removeEffect(MobEffects.LEVITATION);
                    sound(level, victim.position(), SoundEvents.BUBBLE_POP, 2.0f, 0.6f);
                    sound(level, victim.position(), SoundEvents.GENERIC_SPLASH, 1.2f, 1.2f);
                }).spawn();
            }).spawn();
        }
    }
}
