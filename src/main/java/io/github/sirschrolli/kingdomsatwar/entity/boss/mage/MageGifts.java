package io.github.sirschrolli.kingdomsatwar.entity.boss.mage;

import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossEffects;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossFx;
import io.github.sirschrolli.kingdomsatwar.entity.boss.DragonBossEntity;
import io.github.sirschrolli.kingdomsatwar.magic.fx.SpellFxEntity;
import io.github.sirschrolli.kingdomsatwar.magic.fx.SpellProjectile;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Weitere Fähigkeiten der Erzmagier:
 * <ul>
 *   <li><b>Erneuerung</b> (alle): Unter dem Magier leuchtet ein Heilkreis auf, eine Säule aus Licht umhüllt ihn, und er
 *   sammelt vier Sekunden lang Lebenskraft (bis zu gut 15 % seiner Lebensenergie, höchstens zweimal je Kampf). Wer ihm in
 *   dieser Zeit genug Schaden zufügt, bricht das Ritual – dann taumelt er erschöpft und nimmt mehr Schaden.</li>
 *   <li>Ignatius – <b>Phönixwiedergeburt</b>: Der erste tödliche Schlag verbrennt ihn – und aus der Asche steigt er mit
 *   einem Drittel seiner Kraft wieder auf, zwei Phönixe fliegen aus den Flammen.</li>
 *   <li>Isolde – <b>Eiskokon</b>: Fast besiegt, hüllt sie sich in Eis und heilt darin; wer den Kokon rechtzeitig
 *   zerschlägt, trifft sie ungeschützt.</li>
 *   <li>Fulgor – <b>Blitzableiter</b>: Er ruft einen Blitz auf sich selbst herab, der ihn stärkt und die Ladung in einem
 *   Ring auf alle in der Nähe entlädt.</li>
 *   <li>Sylvana – <b>Dornenhaut</b>: Wer sie im Nahkampf trifft, sticht sich an Dornen und wird vergiftet.</li>
 *   <li>Gorvan – <b>Erdwacht</b>: Wer ihn aus der Ferne beschießt, den packt kurz darauf eine Steinfaust aus dem Boden.</li>
 *   <li>Marena – <b>Gegenwelle</b>: Bedrängt man sie im Nahkampf, stößt eine Wasserwelle alle von ihr fort.</li>
 * </ul>
 */
final class MageGifts {
    /** Erneuerung: so lange dauert das Ritual, so oft je Kampf, so viel heilt jeder Puls, so viel Schaden bricht es. */
    static final int RENEW_TICKS = 80;
    static final int RENEW_COOLDOWN = 700;
    static final int RENEW_MAX = 2;
    private static final float RENEW_PULSE = 0.012f;
    private static final float RENEW_BREAK = 0.05f;
    /** So lange taumelt er nach einem gebrochenen Ritual oder zersprungenen Kokon, und so viel mehr Schaden nimmt er dann. */
    static final int STAGGER_TICKS = 50;
    static final float STAGGER_DAMAGE = 1.25f;
    /** Phönixwiedergeburt: Dauer der Flammen und Kraft danach. */
    static final int REBIRTH_TICKS = 50;
    static final float REBIRTH_HEALTH = 0.3f;
    /** Eiskokon: ab welcher Lebensenergie, wie lange, wie viel er aushält und wie viel er heilt (alle 10 Ticks). */
    static final float COCOON_TRIGGER = 0.25f;
    static final int COCOON_TICKS = 100;
    private static final float COCOON_ARMOR = 0.08f;
    private static final float COCOON_PULSE = 0.015f;
    /** Blitzableiter. */
    static final int SURGE_TICKS = 34;
    static final int SURGE_COOLDOWN = 420;
    private static final int SURGE_STRIKE = 20;
    private static final float SURGE_HEAL = 0.06f;
    /** Erdwacht: ab welcher Entfernung und wie oft. */
    private static final double EARTHWARD_RANGE = 9.0;
    private static final int EARTHWARD_COOLDOWN = 160;
    /** Gegenwelle: nach so vielen Nahkampftreffern in so vielen Ticks, höchstens so oft. */
    private static final int WAVE_HITS = 3;
    private static final int WAVE_WINDOW = 60;
    private static final int WAVE_COOLDOWN = 200;

    private MageGifts() {}

    /** Was die Fähigkeiten in einem Kampf brauchen (beim Erwachen und bei der Rückkehr zurückgesetzt). */
    static final class Gifts {
        int renewals;
        float renewDamage;
        @Nullable SpellFxEntity renewCircle;
        @Nullable SpellFxEntity renewPillar;
        int stagger;
        boolean reborn;
        int rebirth;
        boolean cocoonUsed;
        int cocoon;
        float cocoonHealth;
        @Nullable SpellFxEntity cocoonFx;
        int lastEarthWard = -10_000;
        int waveHits;
        int waveTimer;
        int lastWave = -10_000;

        void reset() {
            this.endRenewFx();
            if (this.cocoonFx != null) {
                this.cocoonFx.discard();
                this.cocoonFx = null;
            }
            this.renewals = 0;
            this.renewDamage = 0.0f;
            this.stagger = 0;
            this.reborn = false;
            this.rebirth = 0;
            this.cocoonUsed = false;
            this.cocoon = 0;
            this.cocoonHealth = 0.0f;
            this.lastEarthWard = -10_000;
            this.waveHits = 0;
            this.waveTimer = 0;
            this.lastWave = -10_000;
        }

        void endRenewFx() {
            if (this.renewCircle != null) {
                this.renewCircle.discard();
                this.renewCircle = null;
            }
            if (this.renewPillar != null) {
                this.renewPillar.discard();
                this.renewPillar = null;
            }
        }

        /** Hält ihn gerade etwas anderes als ein Angriff gefangen (Flammen, Eis, Taumeln)? */
        boolean busy() {
            return this.rebirth > 0 || this.cocoon > 0 || this.stagger > 0;
        }
    }

    // =====================================================================================
    // Jeden Kampf-Tick
    // =====================================================================================

    /**
     * Wiedergeburt, Kokon und Taumeln laufen statt der Angriffe. @return ob der Magier in diesem Tick damit beschäftigt ist
     * (dann greift er nicht an und bewegt sich nicht).
     */
    static boolean tick(ServerLevel level, MageBossEntity mage) {
        Gifts g = mage.gifts;
        if (g.waveTimer > 0 && --g.waveTimer == 0) {
            g.waveHits = 0;
        }
        if (g.rebirth > 0) {
            tickRebirth(level, mage);
            return true;
        }
        if (g.cocoon > 0) {
            tickCocoon(level, mage);
            return true;
        }
        if (mage.element() == DragonType.ICE && !g.cocoonUsed && mage.getHealth() < mage.getMaxHealth() * COCOON_TRIGGER) {
            beginCocoon(level, mage);
            return true;
        }
        if (g.stagger > 0) {
            g.stagger--;
            mage.setDeltaMovement(mage.getDeltaMovement().multiply(0.4, 1.0, 0.4));
            if (g.stagger % 6 == 0) {
                Vec3 head = mage.position().add(0, mage.getBbHeight() + 0.2, 0);
                for (int k = 0; k < 3; k++) {
                    double a = (mage.tickCount * 0.3) + k * Mth.TWO_PI / 3;
                    level.sendParticles(ParticleTypes.ENCHANTED_HIT, head.x + Math.cos(a) * 0.45, head.y, head.z + Math.sin(a) * 0.45, 1, 0, 0, 0, 0);
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Ein Treffer kommt an: im Kokon fängt das Eis ihn ab, in den Flammen der Wiedergeburt verbrennt er, beim Taumeln
     * trifft er härter. @return der Schaden, der durchkommt (0: abgefangen)
     */
    static float onHurt(ServerLevel level, MageBossEntity mage, DamageSource source, float damage) {
        Gifts g = mage.gifts;
        if (g.rebirth > 0) {
            level.sendParticles(ParticleTypes.FLAME, mage.getX(), mage.getY(0.6), mage.getZ(), 8, 0.4, 0.6, 0.4, 0.05);
            return 0.0f;
        }
        if (g.cocoon > 0) {
            g.cocoonHealth -= damage;
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.PACKED_ICE.defaultBlockState()), mage.getX(), mage.getY(0.6),
                    mage.getZ(), 14, 0.6, 0.8, 0.6, 0.1);
            sound(level, mage.position(), SoundEvents.GLASS_HIT, 1.4f, 0.8f + mage.getRandom().nextFloat() * 0.3f);
            if (g.cocoonHealth <= 0.0f) {
                shatterCocoon(level, mage);
            }
            return 0.0f;
        }
        return g.stagger > 0 ? damage * STAGGER_DAMAGE : damage;
    }

    /** Nach dem Treffer (endgültiger Schaden): bricht das Heilritual? */
    static void afterHurt(ServerLevel level, MageBossEntity mage, float damage) {
        Gifts g = mage.gifts;
        if (mage.renewing()) {
            g.renewDamage += damage;
            if (g.renewDamage >= mage.getMaxHealth() * RENEW_BREAK) {
                breakRenewal(level, mage);
            } else {
                sound(level, mage.position(), SoundEvents.AMETHYST_BLOCK_HIT, 1.2f, 0.6f + g.renewDamage / (mage.getMaxHealth() * RENEW_BREAK));
            }
        }
    }

    // =====================================================================================
    // Erneuerung (alle)
    // =====================================================================================

    static boolean canRenew(MageBossEntity mage) {
        Gifts g = mage.gifts;
        return g.renewals < RENEW_MAX && mage.getHealth() < mage.getMaxHealth() * 0.7f && !g.busy();
    }

    static float renewWeight(MageBossEntity mage) {
        return mage.getHealth() < mage.getMaxHealth() * 0.45f ? 6.0f : 2.5f;
    }

    /** Das Heilritual: Kreis, Lichtsäule, einströmende Kraft – alle fünf Ticks ein Puls Lebensenergie. */
    static void renew(ServerLevel level, MageBossEntity mage, int t) {
        Gifts g = mage.gifts;
        DragonType element = mage.element();
        Vec3 ground = MageSignatures.ground(level, mage.position());
        if (t == 0) {
            g.renewals++;
            g.renewDamage = 0.0f;
            g.endRenewFx();
            g.renewCircle = SpellFxEntity.create(level, SpellFxEntity.Kind.CAST_CIRCLE, ground.add(0, 0.03, 0), element, RENEW_TICKS + 4, 3.0f)
                    .onTick(fx -> fx.setParam(Math.min(1.0f, fx.tickCount / (float) RENEW_TICKS))).spawn();
            g.renewPillar = SpellFxEntity.create(level, SpellFxEntity.Kind.PILLAR, ground, element, RENEW_TICKS + 4, 0.95f).param(5.5f).spawn();
            sound(level, mage.position(), SoundEvents.BEACON_ACTIVATE, 2.0f, 1.3f);
            sound(level, mage.position(), SoundEvents.EVOKER_PREPARE_SUMMON, 1.6f, 1.2f);
            Component name = Component.translatable(mage.getType().getDescriptionId()).withColor(element.color());
            tell(level, mage, Component.translatable("boss.kingdomsatwar.mage.renew", name).withStyle(ChatFormatting.LIGHT_PURPLE));
        }
        // Kraft strömt aus der Umgebung herein
        if (t % 2 == 0) {
            ParticleOptions particle = renewParticle(element);
            for (int k = 0; k < 3; k++) {
                double a = mage.getRandom().nextDouble() * Mth.TWO_PI;
                double r = 2.6 + mage.getRandom().nextDouble() * 1.2;
                Vec3 from = mage.position().add(Math.cos(a) * r, 0.2 + mage.getRandom().nextDouble() * 2.2, Math.sin(a) * r);
                Vec3 to = mage.position().add(0, 1.2, 0).subtract(from);
                level.sendParticles(particle, from.x, from.y, from.z, 0, to.x, to.y, to.z, 0.09);
            }
        }
        if (t >= 10 && t % 5 == 0 && t < RENEW_TICKS) {
            mage.heal(mage.getMaxHealth() * RENEW_PULSE);
            level.sendParticles(new DustParticleOptions(element.color(), 1.3f), mage.getX(), mage.getY(0.5), mage.getZ(), 6, 0.4, 0.7, 0.4, 0.0);
            level.sendParticles(ParticleTypes.END_ROD, mage.getX(), mage.getY(0.3), mage.getZ(), 2, 0.3, 0.3, 0.3, 0.03);
            if (t % 10 == 0) {
                sound(level, mage.position(), SoundEvents.AMETHYST_BLOCK_CHIME, 2.0f, 0.7f + t / (float) RENEW_TICKS * 0.8f);
            }
        }
        if (t == RENEW_TICKS - 1) {
            g.endRenewFx();
            sound(level, mage.position(), SoundEvents.BEACON_POWER_SELECT, 1.8f, 1.4f);
            BossFx.impact(level, new DustParticleOptions(element.color(), 1.8f), mage.position().add(0, 1.0, 0), 30, 0.6, 0.9, 0.6, 0.05);
        }
    }

    /** Genug Schaden: das Ritual zerbricht, der Magier taumelt erschöpft. */
    private static void breakRenewal(ServerLevel level, MageBossEntity mage) {
        Gifts g = mage.gifts;
        g.endRenewFx();
        mage.interruptAttack();
        g.stagger = STAGGER_TICKS;
        sound(level, mage.position(), SoundEvents.GLASS_BREAK, 2.0f, 0.6f);
        sound(level, mage.position(), SoundEvents.EVOKER_HURT, 1.6f, 0.6f);
        BossFx.impact(level, new DustParticleOptions(mage.element().color(), 1.6f), mage.position().add(0, 1.0, 0), 40, 1.0, 1.0, 1.0, 0.1);
        BossFx.impact(level, ParticleTypes.ENCHANTED_HIT, mage.position().add(0, 1.0, 0), 30, 0.8, 0.8, 0.8, 0.3);
        Component name = Component.translatable(mage.getType().getDescriptionId()).withColor(mage.element().color());
        tell(level, mage, Component.translatable("boss.kingdomsatwar.mage.renew_broken", name).withStyle(ChatFormatting.GOLD));
    }

    private static ParticleOptions renewParticle(DragonType element) {
        return switch (element) {
            case FIRE -> ParticleTypes.FLAME;
            case ICE -> ParticleTypes.SNOWFLAKE;
            case LIGHTNING -> ParticleTypes.ELECTRIC_SPARK;
            case NATURE -> ParticleTypes.HAPPY_VILLAGER;
            case EARTH -> new BlockParticleOption(ParticleTypes.BLOCK, Blocks.TUFF.defaultBlockState());
            case WATER -> ParticleTypes.BUBBLE_POP;
        };
    }

    // =====================================================================================
    // Feuer – Phönixwiedergeburt
    // =====================================================================================

    /** Der tödliche Schlag – Ignatius verbrennt und steht wieder auf. @return ob er stattdessen wiedergeboren wird */
    static boolean rebirth(ServerLevel level, MageBossEntity mage) {
        Gifts g = mage.gifts;
        if (mage.element() != DragonType.FIRE || g.reborn || g.rebirth > 0) {
            return false;
        }
        g.reborn = true;
        g.rebirth = REBIRTH_TICKS;
        g.stagger = 0;
        g.endRenewFx();
        mage.setHealth(1.0f);
        mage.interruptAttack();
        mage.gesture(MageBossEntity.ANIM_TRANSITION);
        Vec3 ground = MageSignatures.ground(level, mage.position());
        SpellFxEntity.create(level, SpellFxEntity.Kind.PILLAR, ground, DragonType.FIRE, 0xFF7A22, REBIRTH_TICKS, 1.5f).param(8.0f).spawn();
        SpellFxEntity.create(level, SpellFxEntity.Kind.CIRCLE, ground.add(0, 0.03, 0), DragonType.FIRE, REBIRTH_TICKS, 3.6f).spawn();
        sound(level, mage.position(), SoundEvents.BLAZE_DEATH, 2.0f, 0.5f);
        sound(level, mage.position(), SoundEvents.FIRECHARGE_USE, 2.0f, 0.6f);
        BossFx.impact(level, ParticleTypes.LAVA, mage.position().add(0, 0.8, 0), 20, 0.6, 0.6, 0.6, 0.1);
        tell(level, mage, Component.translatable("boss.kingdomsatwar.mage.rebirth_burn").withColor(DragonType.FIRE.color()));
        return true;
    }

    private static void tickRebirth(ServerLevel level, MageBossEntity mage) {
        Gifts g = mage.gifts;
        int t = REBIRTH_TICKS - g.rebirth;
        mage.setDeltaMovement(0, mage.getDeltaMovement().y, 0);
        if (t % 2 == 0) {
            level.sendParticles(ParticleTypes.FLAME, mage.getX(), mage.getY(0.3), mage.getZ(), 10, 0.5, 1.0, 0.5, 0.06);
            level.sendParticles(ParticleTypes.LARGE_SMOKE, mage.getX(), mage.getY(0.8), mage.getZ(), 2, 0.4, 0.6, 0.4, 0.02);
        }
        if (t % 8 == 0) {
            BossFx.ring(level, ParticleTypes.FLAME, mage.position().add(0, 0.15, 0), 1.0 + t / 12.0, 24, 0.0);
            sound(level, mage.position(), SoundEvents.BLAZE_BURN, 1.6f, 0.6f);
        }
        if (--g.rebirth > 0) {
            return;
        }
        // aus der Asche
        mage.setHealth(mage.getMaxHealth() * REBIRTH_HEALTH);
        mage.gesture(MageBossEntity.ANIM_NONE);
        Vec3 c = mage.position().add(0, 1.2, 0);
        BossFx.impact(level, ColorParticleOption.create(ParticleTypes.FLASH, 0xFFFF8A30), c, 1, 0, 0, 0, 0);
        BossFx.impact(level, ParticleTypes.FLAME, c, 120, 2.2, 1.2, 2.2, 0.2);
        BossFx.ring(level, ParticleTypes.FLAME, mage.position().add(0, 0.2, 0), 6.0, 60, 0.0);
        sound(level, mage.position(), SoundEvents.ENDER_DRAGON_GROWL, 1.4f, 1.6f);
        sound(level, mage.position(), SoundEvents.BLAZE_SHOOT, 2.0f, 0.5f);
        mage.shake(level, 26.0, 0.7f, 16);
        float damage = 8.0f * mage.damageMultiplier();
        for (LivingEntity e : mage.enemiesAround(mage.position(), 6.0, 4.0)) {
            if (mage.hurtTarget(level, e, damage)) {
                BossEffects.burn(e, 4);
                DragonBossEntity.pushAway(e, mage.position(), 1.3, 0.45);
            }
        }
        // zwei Phönixe steigen aus den Flammen
        List<LivingEntity> enemies = mage.arenaEnemies();
        for (int i = 0; i < 2; i++) {
            LivingEntity goal = enemies.isEmpty() ? null : enemies.get(i % enemies.size());
            Vec3 flat = goal != null ? goal.position().subtract(mage.position()).multiply(1, 0, 1) : Vec3.directionFromRotation(0.0f, mage.getYRot());
            if (flat.lengthSqr() < 1.0e-3) {
                flat = Vec3.directionFromRotation(0.0f, mage.getYRot());
            }
            Vec3 dir = flat.normalize().yRot((i == 0 ? -40.0f : 40.0f) * Mth.DEG_TO_RAD).add(0, 0.6, 0);
            SpellProjectile.shoot(level, mage, SpellProjectile.Kind.PHOENIX, c, dir, 0.45, 9.0f * mage.damageMultiplier(), goal, 1);
        }
        tell(level, mage, Component.translatable("boss.kingdomsatwar.mage.rebirth").withColor(DragonType.FIRE.color()));
    }

    // =====================================================================================
    // Eis – Eiskokon
    // =====================================================================================

    private static void beginCocoon(ServerLevel level, MageBossEntity mage) {
        Gifts g = mage.gifts;
        g.cocoonUsed = true;
        g.cocoon = COCOON_TICKS;
        g.cocoonHealth = mage.getMaxHealth() * COCOON_ARMOR;
        g.stagger = 0;
        g.endRenewFx();
        mage.interruptAttack();
        mage.gesture(MageBossEntity.ANIM_SHIELD);
        Vec3 ground = MageSignatures.ground(level, mage.position());
        g.cocoonFx = SpellFxEntity.create(level, SpellFxEntity.Kind.PRISON, ground, DragonType.ICE, COCOON_TICKS + 2, 1.55f).spawn();
        sound(level, mage.position(), SoundEvents.GLASS_PLACE, 2.0f, 0.6f);
        sound(level, mage.position(), SoundEvents.AMETHYST_BLOCK_RESONATE, 2.0f, 0.7f);
        BossFx.impact(level, ParticleTypes.SNOWFLAKE, mage.position().add(0, 1.0, 0), 50, 1.0, 1.2, 1.0, 0.05);
        Component name = Component.translatable(mage.getType().getDescriptionId()).withColor(DragonType.ICE.color());
        tell(level, mage, Component.translatable("boss.kingdomsatwar.mage.cocoon", name).withStyle(ChatFormatting.AQUA));
    }

    private static void tickCocoon(ServerLevel level, MageBossEntity mage) {
        Gifts g = mage.gifts;
        int t = COCOON_TICKS - g.cocoon;
        mage.setDeltaMovement(0, mage.getDeltaMovement().y, 0);
        if (t > 0 && t % 10 == 0) {
            mage.heal(mage.getMaxHealth() * COCOON_PULSE);
            level.sendParticles(ParticleTypes.SNOWFLAKE, mage.getX(), mage.getY(0.5), mage.getZ(), 10, 0.7, 0.9, 0.7, 0.02);
            sound(level, mage.position(), SoundEvents.AMETHYST_BLOCK_CHIME, 1.4f, 1.2f + t / (float) COCOON_TICKS * 0.6f);
        }
        // die Kälte des Kokons kriecht über den Boden
        if (t % 20 == 0) {
            for (LivingEntity e : mage.enemiesAround(mage.position(), 4.0, 3.0)) {
                e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 30, 1, false, false, true), mage);
                e.setTicksFrozen(Math.min(e.getTicksRequiredToFreeze() + 40, e.getTicksFrozen() + 30));
            }
        }
        if (--g.cocoon > 0) {
            return;
        }
        // sie bricht aus dem Eis – mit einer Frostwelle
        if (g.cocoonFx != null) {
            g.cocoonFx.discard();
            g.cocoonFx = null;
        }
        mage.gesture(MageBossEntity.ANIM_NONE);
        burst(level, mage, 6.0f, 6.0, 2);
        tell(level, mage, Component.translatable("boss.kingdomsatwar.mage.cocoon_free").withColor(DragonType.ICE.color()));
    }

    /** Der Kokon ist zerschlagen: Splitter fliegen, sie taumelt ungeschützt. */
    private static void shatterCocoon(ServerLevel level, MageBossEntity mage) {
        Gifts g = mage.gifts;
        g.cocoon = 0;
        if (g.cocoonFx != null) {
            g.cocoonFx.discard();
            g.cocoonFx = null;
        }
        mage.gesture(MageBossEntity.ANIM_NONE);
        burst(level, mage, 4.0f, 4.0, 0);
        g.stagger = STAGGER_TICKS;
        Component name = Component.translatable(mage.getType().getDescriptionId()).withColor(DragonType.ICE.color());
        tell(level, mage, Component.translatable("boss.kingdomsatwar.mage.cocoon_broken", name).withStyle(ChatFormatting.GOLD));
    }

    private static void burst(ServerLevel level, MageBossEntity mage, float baseDamage, double radius, int freeze) {
        Vec3 c = mage.position().add(0, 1.0, 0);
        sound(level, c, SoundEvents.GLASS_BREAK, 2.2f, 0.5f);
        sound(level, c, SoundEvents.AMETHYST_CLUSTER_BREAK, 2.0f, 0.6f);
        BossFx.impact(level, new BlockParticleOption(ParticleTypes.BLOCK, Blocks.PACKED_ICE.defaultBlockState()), c, 80, 1.2, 1.2, 1.2, 0.25);
        BossFx.impact(level, ParticleTypes.SNOWFLAKE, c, 60, 2.0, 1.0, 2.0, 0.15);
        BossFx.ring(level, ParticleTypes.SNOWFLAKE, mage.position().add(0, 0.2, 0), radius, 50, 0.0);
        float damage = baseDamage * mage.damageMultiplier();
        for (LivingEntity e : mage.enemiesAround(mage.position(), radius, 3.5)) {
            if (mage.hurtTarget(level, e, damage)) {
                if (freeze > 0) {
                    BossEffects.freeze(e, freeze);
                }
                e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1, false, false, true), mage);
                DragonBossEntity.pushAway(e, mage.position(), 0.8, 0.3);
            }
        }
    }

    // =====================================================================================
    // Blitz – Blitzableiter
    // =====================================================================================

    static float surgeWeight(MageBossEntity mage) {
        if (mage.element() != DragonType.LIGHTNING || mage.phase() < 2) {
            return 0.0f;
        }
        return mage.getHealth() < mage.getMaxHealth() * 0.9f ? 4.0f : 1.0f;
    }

    /** Er reckt den Stab, die Wolken laden sich auf – dann schlägt der Blitz in ihn ein und entlädt sich ringsum. */
    static void surge(ServerLevel level, MageBossEntity mage, int t) {
        Vec3 tip = mage.staffTip();
        if (t == 0) {
            sound(level, mage.position(), SoundEvents.BEACON_POWER_SELECT, 2.0f, 1.6f);
            sound(level, mage.position(), SoundEvents.LIGHTNING_BOLT_THUNDER, 1.0f, 1.6f);
        }
        if (t < SURGE_STRIKE && t % 4 == 0) {
            Vec3 sky = tip.add((mage.getRandom().nextDouble() - 0.5) * 3.0, 9.0 + mage.getRandom().nextDouble() * 3.0,
                    (mage.getRandom().nextDouble() - 0.5) * 3.0);
            SpellFxEntity.create(level, SpellFxEntity.Kind.ARC, sky, DragonType.LIGHTNING, 0xFFF06A, 4, 0.18f).end(tip).spawn();
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, tip.x, tip.y, tip.z, 8, 0.3, 0.3, 0.3, 0.2);
            sound(level, tip, SoundEvents.LIGHTNING_BOLT_IMPACT, 0.6f, 1.8f + mage.getRandom().nextFloat() * 0.2f);
        }
        if (t != SURGE_STRIKE) {
            return;
        }
        LightningBolt bolt = EntityTypes.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
        if (bolt != null) {
            bolt.setVisualOnly(true);
            bolt.snapTo(mage.getX(), mage.getY(), mage.getZ());
            level.addFreshEntity(bolt);
        }
        mage.heal(mage.getMaxHealth() * SURGE_HEAL);
        sound(level, mage.position(), SoundEvents.LIGHTNING_BOLT_IMPACT, 2.0f, 0.8f);
        BossFx.impact(level, ParticleTypes.ELECTRIC_SPARK, mage.position().add(0, 1.0, 0), 80, 1.6, 1.2, 1.6, 0.4);
        BossFx.ring(level, ParticleTypes.ELECTRIC_SPARK, mage.position().add(0, 0.3, 0), 5.5, 60, 0.0);
        mage.shake(level, 22.0, 0.5f, 10);
        float damage = 4.5f * mage.damageMultiplier();
        for (LivingEntity e : mage.enemiesAround(mage.position(), 6.0, 4.0)) {
            SpellFxEntity.create(level, SpellFxEntity.Kind.ARC, tip, DragonType.LIGHTNING, 0xFFF06A, 6, 0.2f)
                    .end(e.getBoundingBox().getCenter()).spawn();
            if (mage.hurtTarget(level, e, damage)) {
                BossEffects.stun(e, 1);
                DragonBossEntity.pushAway(e, mage.position(), 0.8, 0.3);
            }
        }
    }

    // =====================================================================================
    // Natur – Dornenhaut, Erde – Erdwacht, Wasser – Gegenwelle (aus MagePassives#onHurt)
    // =====================================================================================

    /** Sylvana im Nahkampf getroffen: Dornen stechen zurück. */
    static void thorns(ServerLevel level, MageBossEntity mage, LivingEntity attacker, float damage) {
        float back = Math.max(1.5f, damage * 0.25f);
        attacker.hurtServer(level, mage.damageSources().thorns(mage), back);
        attacker.addEffect(new MobEffectInstance(MobEffects.POISON, 50, 0, false, false, true), mage);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SWEET_BERRY_BUSH.defaultBlockState()), attacker.getX(),
                attacker.getY(0.6), attacker.getZ(), 12, 0.3, 0.4, 0.3, 0.1);
        level.sendParticles(new DustParticleOptions(0x5FA83A, 1.0f), mage.getX(), mage.getY(0.6), mage.getZ(), 10, 0.5, 0.7, 0.5, 0.0);
        sound(level, attacker.position(), SoundEvents.SWEET_BERRY_BUSH_BREAK, 1.2f, 0.7f);
    }

    /** Gorvan wird aus der Ferne beschossen: Unter dem Schützen bricht eine Steinfaust hervor. */
    static void earthward(ServerLevel level, MageBossEntity mage, DamageSource source) {
        Gifts g = mage.gifts;
        if (!(source.getDirectEntity() instanceof Projectile) || !(source.getEntity() instanceof LivingEntity shooter) || !mage.isBossEnemy(shooter)
                || shooter.distanceTo(mage) < EARTHWARD_RANGE || mage.tickCount - g.lastEarthWard < EARTHWARD_COOLDOWN) {
            return;
        }
        g.lastEarthWard = mage.tickCount;
        Vec3 spot = MageSignatures.ground(level, shooter.position());
        float damage = 10.0f * mage.damageMultiplier();
        sound(level, mage.position(), SoundEvents.EVOKER_PREPARE_ATTACK, 1.4f, 0.5f);
        SpellFxEntity.create(level, SpellFxEntity.Kind.CIRCLE, spot.add(0, 0.02, 0), DragonType.EARTH, 16, 1.9f).onExpire(fx -> {
            SpellFxEntity.create(level, SpellFxEntity.Kind.FIST, spot, DragonType.EARTH, 30, 1.15f).facing(mage.getYRot() + 180.0f).spawn();
            sound(level, spot, SoundEvents.GENERIC_EXPLODE.value(), 1.4f, 0.55f);
            sound(level, spot, SoundEvents.ANVIL_LAND, 0.7f, 0.5f);
            for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, new net.minecraft.world.phys.AABB(spot, spot).inflate(2.0, 3.0, 2.0),
                    e -> e.isAlive() && mage.isBossEnemy(e))) {
                if (mage.hurtTarget(level, e, damage)) {
                    e.setDeltaMovement(e.getDeltaMovement().x * 0.3, 1.1, e.getDeltaMovement().z * 0.3);
                    e.needsSync = true;
                }
            }
        }).spawn();
        if (shooter instanceof ServerPlayer player) {
            player.sendOverlayMessage(Component.translatable("boss.kingdomsatwar.mage.earthward").withStyle(ChatFormatting.GOLD));
        }
    }

    /** Marena im Nahkampf bedrängt: nach dem dritten Treffer rollt eine Welle alle von ihr fort. */
    static void counterWave(ServerLevel level, MageBossEntity mage) {
        Gifts g = mage.gifts;
        g.waveHits++;
        g.waveTimer = WAVE_WINDOW;
        if (g.waveHits < WAVE_HITS || mage.tickCount - g.lastWave < WAVE_COOLDOWN) {
            return;
        }
        g.waveHits = 0;
        g.lastWave = mage.tickCount;
        Vec3 c = mage.position();
        SpellFxEntity.create(level, SpellFxEntity.Kind.BUBBLE, c, DragonType.WATER, 0x7FE0FF, 10, 5.0f).spawn();
        SpellFxEntity.create(level, SpellFxEntity.Kind.CIRCLE, MageSignatures.ground(level, c).add(0, 0.03, 0), DragonType.WATER, 14, 5.0f).spawn();
        sound(level, c, SoundEvents.GENERIC_SPLASH, 2.0f, 0.6f);
        sound(level, c, SoundEvents.PLAYER_SPLASH_HIGH_SPEED, 1.6f, 0.8f);
        BossFx.ring(level, ParticleTypes.SPLASH, c.add(0, 0.3, 0), 4.0, 70, 0.0);
        BossFx.impact(level, ParticleTypes.BUBBLE_POP, c.add(0, 1.0, 0), 40, 1.5, 0.8, 1.5, 0.1);
        float damage = 4.0f * mage.damageMultiplier();
        for (LivingEntity e : mage.enemiesAround(c, 5.0, 3.5)) {
            if (mage.hurtTarget(level, e, damage)) {
                DragonBossEntity.pushAway(e, c, 1.6, 0.45);
                e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 1, false, false, true), mage);
            }
        }
    }

    // =====================================================================================

    private static void tell(ServerLevel level, MageBossEntity mage, Component message) {
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(mage) < MageBossEntity.LEASH_RADIUS * MageBossEntity.LEASH_RADIUS) {
                player.sendOverlayMessage(message);
            }
        }
    }

    private static void sound(ServerLevel level, Vec3 at, SoundEvent sound, float volume, float pitch) {
        level.playSound(null, at.x, at.y, at.z, sound, SoundSource.HOSTILE, volume, pitch);
    }

    /** Ist er gerade in den Flammen der Wiedergeburt oder im Eis (für die Darstellung und den Schutz)? */
    static boolean untouchable(MageBossEntity mage) {
        return mage.gifts.rebirth > 0 || mage.gifts.cocoon > 0;
    }
}
