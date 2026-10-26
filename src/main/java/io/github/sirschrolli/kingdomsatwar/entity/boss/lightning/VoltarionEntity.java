package io.github.sirschrolli.kingdomsatwar.entity.boss.lightning;

import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
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
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Voltarion, der Sturmbringer – der Blitzdrachen-Boss, ein Wyvern. Er schläft nicht im Nest, sondern hoch im Sturm über
 * seinem Gipfel: Beim Erwachen zieht ein Gewitter auf, und er fährt mit einem gewaltigen Blitz herab.
 *
 * <p>Angriffe: Biss, Peitschenschwanz, Sturmböe, Donnerschrei (betäubt, auch beim Phasenwechsel), Blitzodem, Blitzodem
 * im Tiefflug, Kugelblitz-Salve, Kettenblitz, Blitzfeld (ab Phase 2), Sturzflug-Ansturm, Gewitter (ab Phase 2) und
 * Blitzwelle (Phase 3). Fliegt viel und lange, ist schnell, aber weniger zäh.
 */
public class VoltarionEntity extends DragonBossEntity {
    /** In diesem Moment des Erwachens fährt er mit dem Blitz herab. */
    public static final int STRIKE_TICK = 80;

    public VoltarionEntity(EntityType<? extends VoltarionEntity> type, Level level) {
        super(type, level, DragonType.LIGHTNING);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBossAttributes(760.0, 10.0, 0.36);
    }

    @Override
    protected int groundDuration() {
        return 220 + this.random.nextInt(140);
    }

    @Override
    protected int airDuration() {
        return 380 + this.random.nextInt(180);
    }

    @Override
    protected double cruiseHeight() {
        return 20.0;
    }

    @Override
    protected void snortParticles() {
        Vec3 mouth = this.mouthPosition();
        for (int i = 0; i < 6; i++) {
            this.level().addParticle(ParticleTypes.ELECTRIC_SPARK, mouth.x, mouth.y + 0.3, mouth.z,
                    (this.random.nextDouble() - 0.5) * 0.3, 0.05, (this.random.nextDouble() - 0.5) * 0.3);
        }
    }

    /** Blitze (auch seine eigenen) und ihr Feuer prallen an ihm ab. */
    @Override
    protected float modifyIncomingDamage(DamageSource source, float amount) {
        if (source.is(DamageTypeTags.IS_LIGHTNING) || source.is(DamageTypeTags.IS_FIRE) && !(source.getEntity() instanceof Player)) {
            return 0.0f;
        }
        return amount;
    }

    // =====================================================================================
    // Trefferzonen (langer Hals, langer Peitschenschwanz, riesige Flügel)
    // =====================================================================================

    @Override
    protected BossPart[] createParts() {
        return new BossPart[] {
                new BossPart(this, "head", 2.6f, 2.2f, 1.5f),
                new BossPart(this, "neck", 2.0f, 2.2f, 1.15f),
                new BossPart(this, "neck2", 1.8f, 2.0f, 1.15f),
                new BossPart(this, "tail1", 2.0f, 1.8f, 0.7f),
                new BossPart(this, "tail2", 1.7f, 1.5f, 0.7f),
                new BossPart(this, "tail3", 1.4f, 1.3f, 0.7f),
                new BossPart(this, "tail4", 1.2f, 1.1f, 0.7f),
                new BossPart(this, "blade", 1.6f, 0.9f, 0.7f),
                new BossPart(this, "wing_l", 6.5f, 1.2f, 0.85f),
                new BossPart(this, "wing_r", 6.5f, 1.2f, 0.85f),
        };
    }

    private Vec3 neckBase() {
        return this.bodyPoint(0, 5.6, 2.2);
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
        return 1.0;
    }

    @Override
    protected void layoutParts() {
        double reach = this.attackHeadReach();
        Vec3 base = this.neckBase();
        Vec3 dir = this.neckDir();
        double c = this.collapse();
        BossPart[] p = this.parts;
        setAt(p[0], base.add(dir.scale(5.3 + reach)).add(0, 2.9 * c, 0));
        setAt(p[1], base.add(dir.scale(1.4 + reach * 0.3)).add(0, 1.2 * c, 0));
        setAt(p[2], base.add(dir.scale(3.0 + reach * 0.6)).add(0, 2.4 * c, 0));
        this.placePart(p[3], 0, 5.0 * c, -4.0);
        this.placePart(p[4], 0, 4.6 * c, -6.8);
        this.placePart(p[5], 0, 4.2 * c, -9.6);
        this.placePart(p[6], 0, 3.9 * c, -12.3);
        this.placePart(p[7], 0, 3.6 * c, -14.8);
        boolean spread = this.isFlying();
        double wx = spread ? 7.5 : 3.0;
        double wy = spread ? 5.8 : 6.2;
        this.placePart(p[8], wx, wy * c, spread ? 0.8 : -0.5);
        this.placePart(p[9], -wx, wy * c, spread ? 0.8 : -0.5);
    }

    private static void setAt(BossPart part, Vec3 pos) {
        part.setOldPosAndRot();
        part.setPos(pos.x, pos.y - part.getBbHeight() / 2.0, pos.z);
    }

    @Override
    public Vec3 mouthPosition() {
        return this.neckBase().add(this.neckDir().scale(7.1 + this.attackHeadReach())).add(0, 2.6 * this.collapse(), 0);
    }

    // =====================================================================================
    // Angriffe
    // =====================================================================================

    @Override
    protected void registerAttacks(List<BossAttack> list) {
        ElementFx lightning = new ElementFx(ModParticles.BOSS_SPARK.get(), ParticleTypes.ELECTRIC_SPARK,
                ModSounds.DRAGON_BREATH.get(DragonType.LIGHTNING).get(), LightningAttacks::shock);
        list.add(new CommonAttacks.Bite(18, 12.0));
        list.add(new CommonAttacks.TailSweep(14));
        list.add(new CommonAttacks.WingBuffet(8));
        list.add(new LightningAttacks.ThunderRoar(16));
        list.add(new CommonAttacks.TakeOff());
        list.add(new CommonAttacks.Land());
        list.add(new ElementAttacks.Breath(lightning, 5, 22));
        list.add(new ElementAttacks.AirBreath(lightning, 6));
        list.add(new ElementAttacks.Volley(BossProjectile.Kind.SPARK, 6, 8));
        list.add(new LightningAttacks.ChainLightning(12));
        list.add(new LightningAttacks.LightningField(4, 9));
        list.add(new LightningAttacks.DiveCharge(16));
        list.add(new LightningAttacks.Thunderstorm(12));
        list.add(new ElementAttacks.NovaRing(lightning, 12, 22));
    }

    // =====================================================================================
    // Erwachen: Ein Gewitter zieht auf, er fährt mit einem gewaltigen Blitz herab
    // =====================================================================================

    @Override
    public int awakeningTicks() {
        return 180;
    }

    @Override
    protected void tickAwakening(ServerLevel level, int tick) {
        BlockPos home = this.getHome() != null ? this.getHome() : this.blockPosition();
        Vec3 c = Vec3.atBottomCenterOf(home);
        if (tick < STRIKE_TICK) {
            this.setPos(c.x, c.y + 60, c.z);
            this.setDeltaMovement(Vec3.ZERO);
            if (tick == 1) {
                level.getServer().setWeatherParameters(0, 6000, true, false);   // der Sturm zieht auf
            }
            if (tick % 12 == 4) {
                // ferne Blitze rund um den Gipfel
                double a = this.random.nextDouble() * Mth.TWO_PI;
                double r = 30 + this.random.nextDouble() * 25;
                double x = c.x + Math.cos(a) * r;
                double z = c.z + Math.sin(a) * r;
                LightningAttacks.visualBolt(level, new Vec3(x, this.groundY(x, z), z));
            }
            if (tick % 3 == 0) {
                for (int i = 0; i < 3; i++) {
                    double a = this.random.nextDouble() * Mth.TWO_PI;
                    double r = this.random.nextDouble() * 14;
                    level.sendParticles(ParticleTypes.ELECTRIC_SPARK, c.x + Math.cos(a) * r, c.y + 0.3 + this.random.nextDouble() * 2,
                            c.z + Math.sin(a) * r, 2, 0.2, 0.2, 0.2, 0.05);
                }
                BossFx.ring(level, ModParticles.BOSS_SPARK.get(), c.add(0, 0.2, 0), 2.0 + tick * 0.08, 16, 0.1);
            }
            if (tick == 40) {
                BossFx.sound(level, c, SoundEvents.LIGHTNING_BOLT_THUNDER, 6.0f, 0.5f);
            }
            return;
        }
        this.setPos(c.x, c.y, c.z);
        this.setDeltaMovement(Vec3.ZERO);
        if (tick == STRIKE_TICK) {
            for (int i = 0; i < 4; i++) {
                LightningAttacks.visualBolt(level, c.add((this.random.nextDouble() - 0.5) * 3, 0, (this.random.nextDouble() - 0.5) * 3));
            }
            level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0xFFE8F8FF), c.x, c.y + 4, c.z, 3, 1, 2, 1, 0);
            BossFx.sound(level, c, SoundEvents.LIGHTNING_BOLT_THUNDER, 8.0f, 0.6f);
            BossFx.sound(level, c, SoundEvents.GENERIC_EXPLODE.value(), 5.0f, 0.6f);
            BossFx.burst(level, ParticleTypes.ELECTRIC_SPARK, c.add(0, 3, 0), 200, 5.0, 0.6);
            BossFx.burst(level, ModParticles.BOSS_SPARK.get(), c.add(0, 2, 0), 60, 4.0, 0.1);
            this.shake(level, 90, 1.2f, 25);
            for (var e : this.enemiesAround(c, 12, 6)) {
                DragonBossEntity.pushAway(e, c, 1.5, 0.6);
            }
        } else if (tick < 128) {
            if (tick % 2 == 0) {
                BossFx.burst(level, ParticleTypes.ELECTRIC_SPARK, this.position().add(0, 4, 0), 6, 3.0, 0.1);
            }
            if (tick % 9 == 0) {
                LightningAttacks.arc(level, this.position().add(0, 5, 0), this.position().add((this.random.nextDouble() - 0.5) * 12, 0,
                        (this.random.nextDouble() - 0.5) * 12));
                BossFx.sound(level, this.position(), SoundEvents.LIGHTNING_BOLT_IMPACT, 1.5f, 1.5f);
            }
        } else if (tick == 132) {
            BossFx.sound(level, this.position(), ModSounds.DRAGON_ROAR.get(), 8.0f, 0.7f);
            BossFx.sound(level, this.position(), SoundEvents.LIGHTNING_BOLT_THUNDER, 6.0f, 0.8f);
            this.shake(level, 90, 0.9f, 30);
            Vec3 mouth = this.mouthPosition();
            level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0xFFE8F8FF), mouth.x, mouth.y, mouth.z, 1, 0, 0, 0, 0);
            LightningAttacks.visualBolt(level, c.add(8, 0, 0));
            LightningAttacks.visualBolt(level, c.add(-8, 0, 0));
        }
    }

    /** Statt sich hinzulegen, steigt er in den Sturm auf und verschwindet mit einem Blitz. */
    @Override
    protected void tickSlumber(ServerLevel level, int tick) {
        if (tick > 18) {
            this.setPos(this.getX(), this.getY() + 0.25 + (tick - 18) * 0.05, this.getZ());
            this.setDeltaMovement(Vec3.ZERO);
            if (tick % 3 == 0) {
                BossFx.burst(level, ParticleTypes.ELECTRIC_SPARK, this.position().add(0, 3, 0), 6, 3.0, 0.1);
            }
        }
        if (tick == 20) {
            BossFx.sound(level, this.position(), ModSounds.DRAGON_ROAR.get(), 6.0f, 0.8f);
        }
        if (tick == 58) {
            LightningAttacks.visualBolt(level, this.position());
            level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0xFFE8F8FF), this.getX(), this.getY() + 3, this.getZ(), 1, 0, 0, 0, 0);
        }
    }

    // =====================================================================================
    // Tod: Er zerfällt im Sturm, ein letzter Blitz, dann klart es auf
    // =====================================================================================

    @Override
    protected void tickDeathEffects(ServerLevel level, int tick) {
        Vec3 body = this.position().add(0, 3.0 * this.collapse(), 0);
        if (tick == 1) {
            BossFx.sound(level, body, ModSounds.DRAGON_DEATH.get(), 8.0f, 0.7f);
            BossFx.sound(level, body, SoundEvents.LIGHTNING_BOLT_THUNDER, 5.0f, 0.6f);
        }
        if (tick < 130) {
            BossFx.burst(level, ParticleTypes.ELECTRIC_SPARK, body, 8, 3.5, 0.15);
            if (tick % 15 == 0) {
                double a = this.random.nextDouble() * Mth.TWO_PI;
                double r = 4 + this.random.nextDouble() * 12;
                double x = this.getX() + Math.cos(a) * r;
                double z = this.getZ() + Math.sin(a) * r;
                LightningAttacks.visualBolt(level, new Vec3(x, this.groundY(x, z), z));
            }
            if (tick == 30) {
                BossFx.sound(level, body, ModSounds.DRAGON_HURT.get(), 6.0f, 0.7f);
            }
        } else if (tick == 160) {
            for (int i = 0; i < 3; i++) {
                LightningAttacks.visualBolt(level, this.position());
            }
            level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0xFFE8F8FF), body.x, body.y, body.z, 3, 1, 1, 1, 0);
            BossFx.burst(level, ParticleTypes.END_ROD, body, 80, 3.0, 0.3);
            BossFx.sound(level, body, SoundEvents.TOTEM_USE, 3.0f, 0.9f);
            level.getServer().setWeatherParameters(6000, 0, false, false);   // es klart auf
        }
    }
}
