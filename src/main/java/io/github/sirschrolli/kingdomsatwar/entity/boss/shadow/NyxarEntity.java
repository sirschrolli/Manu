package io.github.sirschrolli.kingdomsatwar.entity.boss.shadow;

import io.github.sirschrolli.kingdomsatwar.entity.boss.BossAttack;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossFx;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossPart;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossProjectile;
import io.github.sirschrolli.kingdomsatwar.entity.boss.DragonBossEntity;
import io.github.sirschrolli.kingdomsatwar.entity.boss.attack.CommonAttacks;
import io.github.sirschrolli.kingdomsatwar.entity.boss.attack.ElementAttacks;
import io.github.sirschrolli.kingdomsatwar.entity.boss.attack.ElementFx;
import io.github.sirschrolli.kingdomsatwar.registry.ModParticles;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Nyxar, der Schattendrache – der Endgegner der Schattenwelt. Das Wesen, vor dem das Siegel die Welt schützte. Er
 * schläft als Schatten auf dem Thron seiner Zitadelle und nimmt beim Erwachen aus dem Dunkel Gestalt an.
 *
 * <p>Angriffe: Biss, Prankenhieb, Schwanzhieb, Flügelschlag, Schattenschrei (Blindheit), Schattenodem (zehrt, blendet),
 * Schattenodem im Flug, Seelenpfeil-Salve, Schattensprung (verschwindet und erscheint hinter dem Ziel), Schattenklauen
 * aus dem Boden, Finsternis (ab Phase 2: die Arena wird dunkel, Irrlichter jagen die Spieler), Seelenraub (ab Phase 2:
 * heilt sich an getroffenen Gegnern), Schattenregen (Phase 3) und Abgrundwelle. Der stärkste Boss der Mod.
 */
public class NyxarEntity extends DragonBossEntity {
    /** In diesem Moment des Erwachens nimmt er Gestalt an. */
    public static final int FORM_TICK = 90;

    public NyxarEntity(EntityType<? extends NyxarEntity> type, Level level) {
        super(type, level, null);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBossAttributes(1400.0, 14.0, 0.35);
    }

    @Override
    public int bossColor() {
        return 0xB46CFF;
    }

    @Override
    public boolean hiddenDuringAwakening(int ticks) {
        return ticks < 20;
    }

    @Override
    protected int groundDuration() {
        return 260 + this.random.nextInt(160);
    }

    @Override
    protected int airDuration() {
        return 240 + this.random.nextInt(120);
    }

    @Override
    protected double cruiseHeight() {
        return 16.0;
    }

    @Override
    protected void snortParticles() {
        Vec3 mouth = this.mouthPosition();
        for (int i = 0; i < 5; i++) {
            this.level().addParticle(ParticleTypes.SCULK_SOUL, mouth.x, mouth.y + 0.3, mouth.z,
                    (this.random.nextDouble() - 0.5) * 0.2, 0.06, (this.random.nextDouble() - 0.5) * 0.2);
        }
    }

    /** Verdorren, Gift und Magie der Schatten tun ihm nichts; Feuer und Licht treffen ihn härter. */
    @Override
    protected float modifyIncomingDamage(DamageSource source, float amount) {
        if (source.is(DamageTypeTags.WITCH_RESISTANT_TO) && !(source.getEntity() instanceof Player)) {
            return 0.0f;
        }
        if (source.is(DamageTypeTags.IS_FIRE)) {
            return amount * 1.25f;
        }
        return amount;
    }

    @Override
    public boolean canFreeze() {
        return false;
    }

    // =====================================================================================
    // Trefferzonen (langer Hals, langer Schwanz, zwei Flügelpaare)
    // =====================================================================================

    @Override
    protected BossPart[] createParts() {
        return new BossPart[] {
                new BossPart(this, "head", 3.0f, 2.6f, 1.5f),
                new BossPart(this, "neck", 2.4f, 2.6f, 1.15f),
                new BossPart(this, "neck2", 2.2f, 2.4f, 1.15f),
                new BossPart(this, "tail1", 2.4f, 2.2f, 0.7f),
                new BossPart(this, "tail2", 2.0f, 1.8f, 0.7f),
                new BossPart(this, "tail3", 1.7f, 1.5f, 0.7f),
                new BossPart(this, "tail4", 1.4f, 1.3f, 0.7f),
                new BossPart(this, "tail5", 1.2f, 1.1f, 0.7f),
                new BossPart(this, "wing_l", 6.0f, 1.3f, 0.85f),
                new BossPart(this, "wing_r", 6.0f, 1.3f, 0.85f),
        };
    }

    private Vec3 neckBase() {
        return this.bodyPoint(0, 5.4 * this.collapse(), 2.8);
    }

    private Vec3 neckDir() {
        Vec3 body = Vec3.directionFromRotation(0, this.yBodyRot);
        Vec3 head = Vec3.directionFromRotation(0, this.yHeadRot);
        Vec3 d = body.scale(0.35).add(head.scale(0.65));
        return d.lengthSqr() < 1.0e-4 ? body : d.normalize();
    }

    private double collapse() {
        if (this.getBossState() == State.DYING) {
            return 1.0 - 0.55 * Mth.clamp((this.deathTime - 30) / 50.0, 0.0, 1.0);
        }
        if (this.getBossState() == State.AWAKENING && this.getStateTicks() < FORM_TICK) {
            return 0.6;
        }
        return 1.0;
    }

    @Override
    protected void layoutParts() {
        double reach = this.attackHeadReach();
        Vec3 base = this.neckBase();
        Vec3 dir = this.neckDir();
        double c = this.collapse();
        BossPart[] p = this.parts;
        setAt(p[0], base.add(dir.scale(7.0 + reach)).add(0, 3.2 * c, 0));
        setAt(p[1], base.add(dir.scale(2.6 + reach * 0.35)).add(0, 1.5 * c, 0));
        setAt(p[2], base.add(dir.scale(4.9 + reach * 0.7)).add(0, 2.6 * c, 0));
        this.placePart(p[3], 0, 4.2 * c, -4.8);
        this.placePart(p[4], 0, 3.4 * c, -7.8);
        this.placePart(p[5], 0, 2.6 * c, -10.8);
        this.placePart(p[6], 0, 1.9 * c, -13.8);
        this.placePart(p[7], 0, 1.3 * c, -16.8);
        boolean spread = this.isFlying();
        double wx = spread ? 7.2 : 3.4;
        double wy = spread ? 6.8 : 6.4;
        this.placePart(p[8], wx, wy * c, spread ? 0.4 : -0.5);
        this.placePart(p[9], -wx, wy * c, spread ? 0.4 : -0.5);
    }

    private static void setAt(BossPart part, Vec3 pos) {
        part.setOldPosAndRot();
        part.setPos(pos.x, pos.y - part.getBbHeight() / 2.0, pos.z);
    }

    @Override
    public Vec3 mouthPosition() {
        return this.neckBase().add(this.neckDir().scale(9.6 + this.attackHeadReach())).add(0, 2.6 * this.collapse(), 0);
    }

    // =====================================================================================
    // Angriffe
    // =====================================================================================

    @Override
    protected String signatureAttack(int phase) {
        return phase >= 3 ? "shadow_rain" : "eclipse";
    }

    @Override
    protected void registerAttacks(List<BossAttack> list) {
        ElementFx shadow = new ElementFx(ModParticles.BOSS_SHADOW.get(), ParticleTypes.SCULK_SOUL,
                ModSounds.NYXAR_BREATH.get(), ShadowAttacks::wither);
        list.add(new CommonAttacks.Bite(20, 12.0));
        list.add(new CommonAttacks.Claw(14));
        list.add(new CommonAttacks.TailSweep(13));
        list.add(new CommonAttacks.WingBuffet(8));
        list.add(new ShadowAttacks.ShadowShriek(18));
        list.add(new CommonAttacks.TakeOff());
        list.add(new CommonAttacks.Land());
        list.add(new ElementAttacks.Breath(shadow, 6, 23));
        list.add(new ElementAttacks.AirBreath(shadow, 7));
        list.add(new ElementAttacks.Volley(BossProjectile.Kind.SOUL_BOLT, 6, 8));
        list.add(new ShadowAttacks.ShadowStep(16));
        list.add(new ShadowAttacks.ShadowClaws(12, 2));
        list.add(new ShadowAttacks.Eclipse(2));
        list.add(new ShadowAttacks.SoulDrain(14, 2));
        list.add(new ShadowAttacks.ShadowRain(12, 3));
        list.add(new ElementAttacks.NovaRing(shadow, 13, 24));
    }

    // =====================================================================================
    // Erwachen: Aus dem Schatten des Throns nimmt er Gestalt an
    // =====================================================================================

    @Override
    public int awakeningTicks() {
        return 200;
    }

    @Override
    protected void tickAwakening(ServerLevel level, int tick) {
        BlockPos home = this.getHome() != null ? this.getHome() : this.blockPosition();
        Vec3 c = Vec3.atBottomCenterOf(home);
        this.setPos(c.x, c.y, c.z);
        this.setDeltaMovement(Vec3.ZERO);
        if (tick == 1) {
            BossFx.sound(level, c, SoundEvents.WARDEN_EMERGE, 6.0f, 0.5f);
            BossFx.sound(level, c, SoundEvents.SCULK_SHRIEKER_SHRIEK, 4.0f, 0.4f);
        }
        if (tick < FORM_TICK) {
            // Schatten sammeln sich spiralförmig über dem Thron
            double k = tick / (double) FORM_TICK;
            for (int i = 0; i < 4; i++) {
                double a = tick * 0.35 + i * Math.PI / 2;
                double r = 14.0 * (1.0 - k) + 1.5;
                level.sendParticles(ParticleTypes.SCULK_SOUL, c.x + Math.cos(a) * r, c.y + 0.5 + k * 6.0, c.z + Math.sin(a) * r,
                        2, 0.2, 0.2, 0.2, 0.02);
            }
            if (tick % 4 == 0) {
                BossFx.ring(level, ModParticles.BOSS_SHADOW.get(), c.add(0, 0.2, 0), 2.0 + k * 12.0, 18, 0.0);
            }
            if (tick % 20 == 10) {
                BossFx.sound(level, c, SoundEvents.WARDEN_HEARTBEAT, 5.0f, 0.6f);
            }
            return;
        }
        if (tick == FORM_TICK) {
            BossFx.sound(level, c, SoundEvents.WARDEN_ROAR, 8.0f, 0.55f);
            BossFx.sound(level, c, ModSounds.DRAGON_ROAR.get(), 8.0f, 0.5f);
            BossFx.burst(level, ParticleTypes.SCULK_SOUL, c.add(0, 4, 0), 160, 6.0, 0.4);
            BossFx.burst(level, ModParticles.BOSS_SHADOW.get(), c.add(0, 3, 0), 60, 5.0, 0.15);
            this.shake(level, 90, 1.1f, 30);
            for (LivingEntity e : this.enemiesAround(c, 12, 6)) {
                DragonBossEntity.pushAway(e, c, 1.4, 0.6);
                e.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DARKNESS, 80, 0), this);
            }
        } else if (tick < 160) {
            if (tick % 3 == 0) {
                BossFx.burst(level, ParticleTypes.SCULK_SOUL, this.position().add(0, 4, 0), 4, 3.5, 0.08);
            }
        } else if (tick == 168) {
            BossFx.sound(level, this.position(), ModSounds.DRAGON_ROAR.get(), 8.0f, 0.45f);
            this.shake(level, 90, 0.8f, 30);
            BossFx.ring(level, ModParticles.BOSS_SHADOW.get(), c.add(0, 0.3, 0), 10.0, 40, 0.1);
        }
    }

    /** Statt sich hinzulegen, zerfließt er zu Schatten, die zum Thron zurückkriechen. */
    @Override
    protected void tickSlumber(ServerLevel level, int tick) {
        if (tick % 2 == 0) {
            BossFx.burst(level, ParticleTypes.SCULK_SOUL, this.position().add(0, 3, 0), 5, 4.0, 0.05);
        }
        if (tick == 12) {
            BossFx.sound(level, this.position(), SoundEvents.WARDEN_DIG, 5.0f, 0.6f);
        }
    }

    // =====================================================================================
    // Tod: Die Schatten weichen von ihm, Licht bricht hervor
    // =====================================================================================

    @Override
    protected void tickDeathEffects(ServerLevel level, int tick) {
        Vec3 body = this.position().add(0, 3.0 * this.collapse(), 0);
        if (tick == 1) {
            BossFx.sound(level, body, ModSounds.DRAGON_DEATH.get(), 8.0f, 0.55f);
            BossFx.sound(level, body, SoundEvents.WARDEN_DEATH, 6.0f, 0.6f);
        }
        if (tick < 130) {
            if (tick % 2 == 0) {
                BossFx.burst(level, ParticleTypes.SCULK_SOUL, body, 6, 4.0, 0.12);
            }
            if (tick % 10 == 0) {
                BossFx.burst(level, ParticleTypes.END_ROD, body.add((this.random.nextDouble() - 0.5) * 8, this.random.nextDouble() * 3,
                        (this.random.nextDouble() - 0.5) * 8), 12, 1.0, 0.08);
            }
            if (tick == 30 || tick == 90) {
                BossFx.sound(level, body, ModSounds.DRAGON_HURT.get(), 6.0f, 0.6f);
            }
        } else if (tick == 160) {
            level.sendParticles(net.minecraft.core.particles.ColorParticleOption.create(ParticleTypes.FLASH, 0xFFF0E0FF),
                    body.x, body.y, body.z, 3, 1, 1, 1, 0);
            BossFx.burst(level, ParticleTypes.END_ROD, body, 200, 6.0, 0.4);
            BossFx.burst(level, ParticleTypes.SCULK_SOUL, body, 120, 7.0, 0.3);
            BossFx.sound(level, body, SoundEvents.TOTEM_USE, 4.0f, 0.7f);
            BossFx.sound(level, body, SoundEvents.BEACON_POWER_SELECT, 4.0f, 0.5f);
        }
    }

    /** Kein Drachenei: Nyxar hinterlässt das Schattenherz und beendet die Geschichte. */
    @Override
    protected void onDefeated(ServerLevel level, BlockPos home) {
        ShadowThrone.onNyxarDefeated(level, this, home);
    }
}
