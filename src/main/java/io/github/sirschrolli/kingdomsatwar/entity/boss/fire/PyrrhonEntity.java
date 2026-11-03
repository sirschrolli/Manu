package io.github.sirschrolli.kingdomsatwar.entity.boss.fire;

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
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * Pyrrhon, der Aschenfürst – der Feuerdrachen-Boss. Steigt beim Erwachen aus dem Lavasee seines Kraters.
 *
 * <p>Angriffe: Biss, Prankenhieb, Schwanzhieb, Flügelschlag, Brüllen, Stampfen (ab Phase 2), Flammenodem,
 * Feuerodem im Sturzflug, Feuerball-Salve, Meteorregen (ab Phase 2), Lava-Geysire (ab Phase 2) und Flammenring
 * (Phase 3).
 */
public class PyrrhonEntity extends DragonBossEntity {
    private static final double SCALE = 2.1 / 16.0;   // Modell-Einheiten -> Blöcke

    public PyrrhonEntity(EntityType<? extends PyrrhonEntity> type, Level level) {
        super(type, level, DragonType.FIRE);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBossAttributes(900.0, 14.0, 0.3);
    }

    // =====================================================================================
    // Trefferzonen
    // =====================================================================================

    @Override
    protected BossPart[] createParts() {
        return new BossPart[] {
                new BossPart(this, "head", 3.4f, 3.0f, 1.5f),
                new BossPart(this, "neck", 2.8f, 2.8f, 1.15f),
                new BossPart(this, "tail1", 2.6f, 2.4f, 0.7f),
                new BossPart(this, "tail2", 2.2f, 2.0f, 0.7f),
                new BossPart(this, "tail3", 1.8f, 1.6f, 0.7f),
                new BossPart(this, "tail4", 1.4f, 1.4f, 0.7f),
                new BossPart(this, "wing_l", 5.5f, 1.4f, 0.85f),
                new BossPart(this, "wing_r", 5.5f, 1.4f, 0.85f),
        };
    }

    /** Halsansatz relativ zum Körper (Blöcke). */
    private Vec3 neckBase() {
        return this.bodyPoint(0, 5.1 * this.collapse(), 2.6);
    }

    /** Blickrichtung des Halses: zwischen Körper- und Kopfrichtung. */
    private Vec3 neckDir() {
        Vec3 body = Vec3.directionFromRotation(0, this.yBodyRot);
        Vec3 head = Vec3.directionFromRotation(0, this.yHeadRot);
        Vec3 d = body.scale(0.4).add(head.scale(0.6));
        return d.lengthSqr() < 1.0e-4 ? body : d.normalize();
    }

    /** Beim Sterben sackt der Körper zusammen. */
    private double collapse() {
        if (this.getBossState() != State.DYING) {
            return 1.0;
        }
        return 1.0 - 0.55 * Mth.clamp((this.deathTime - 30) / 50.0, 0.0, 1.0);
    }

    @Override
    protected void layoutParts() {
        double reach = this.attackHeadReach();
        Vec3 base = this.neckBase();
        Vec3 dir = this.neckDir();
        double c = this.collapse();
        BossPart[] p = this.parts;
        Vec3 head = base.add(dir.scale(5.8 + reach)).add(0, 2.5 * c, 0);
        Vec3 neck = base.add(dir.scale(2.4 + reach * 0.4)).add(0, 1.4 * c, 0);
        setAt(p[0], head);
        setAt(p[1], neck);
        this.placePart(p[2], 0, 4.0 * c, -4.4);
        this.placePart(p[3], 0, 3.2 * c, -7.0);
        this.placePart(p[4], 0, 2.4 * c, -9.6);
        this.placePart(p[5], 0, 1.7 * c, -12.2);
        boolean spread = this.isFlying();
        double wx = spread ? 6.4 : 3.2;
        double wy = spread ? 6.4 : 6.0;
        this.placePart(p[6], wx, wy * c, spread ? 0.4 : -0.5);
        this.placePart(p[7], -wx, wy * c, spread ? 0.4 : -0.5);
    }

    private static void setAt(BossPart part, Vec3 pos) {
        part.setOldPosAndRot();
        part.setPos(pos.x, pos.y - part.getBbHeight() / 2.0, pos.z);
    }

    @Override
    public Vec3 mouthPosition() {
        return this.neckBase().add(this.neckDir().scale(7.9 + this.attackHeadReach())).add(0, 2.0 * this.collapse(), 0);
    }

    // =====================================================================================
    // Angriffe
    // =====================================================================================

    @Override
    protected String signatureAttack(int phase) {
        return phase >= 3 ? "meteor_rain" : "eruption";
    }

    @Override
    protected void registerAttacks(List<BossAttack> list) {
        ElementFx fire = new ElementFx(ModParticles.BOSS_FLAME.get(), ParticleTypes.LARGE_SMOKE,
                ModSounds.DRAGON_BREATH.get(DragonType.FIRE).get(), (boss, e) -> e.igniteForSeconds(5));
        ElementFx lava = new ElementFx(ParticleTypes.LAVA, ParticleTypes.SMOKE,
                ModSounds.DRAGON_BREATH.get(DragonType.FIRE).get(), (boss, e) -> e.igniteForSeconds(6));
        list.add(new CommonAttacks.Bite(20, 11.5));
        list.add(new CommonAttacks.Claw(14));
        list.add(new CommonAttacks.TailSweep(12));
        list.add(new CommonAttacks.WingBuffet(6));
        list.add(new CommonAttacks.Roar(24));
        list.add(new CommonAttacks.Stomp(10));
        list.add(new CommonAttacks.TakeOff());
        list.add(new CommonAttacks.Land());
        list.add(new ElementAttacks.Breath(fire, 6, 20));
        list.add(new ElementAttacks.AirBreath(fire, 7));
        list.add(new ElementAttacks.Volley(BossProjectile.Kind.FIREBALL, 4, 9));
        list.add(new MeteorRain(14));
        list.add(new ElementAttacks.Eruption(lava, 12, 2));
        list.add(new ElementAttacks.NovaRing(fire, 12, 20));
    }

    // =====================================================================================
    // Erwachen: aus dem Lavasee
    // =====================================================================================

    @Override
    public int awakeningTicks() {
        return 170;
    }

    @Override
    protected void tickAwakening(ServerLevel level, int tick) {
        BlockPos home = this.getHome() != null ? this.getHome() : this.blockPosition();
        Vec3 c = Vec3.atBottomCenterOf(home);
        double depth = 11.0;
        double rise = Mth.clamp((tick - 50) / 70.0, 0.0, 1.0);
        double eased = 1.0 - Math.pow(1.0 - rise, 3.0);
        this.setPos(c.x, c.y - depth + depth * eased, c.z);
        this.setDeltaMovement(Vec3.ZERO);
        if (tick < 50) {
            // Der Lavasee brodelt
            if (tick % 2 == 0) {
                BossFx.warnDisc(level, ParticleTypes.LAVA, c.add(0, 0.2, 0), 5.0, 4);
                BossFx.burst(level, ParticleTypes.LARGE_SMOKE, c.add(0, 1, 0), 3, 3.0, 0.03);
            }
            if (tick % 8 == 0) {
                BossFx.sound(level, c, SoundEvents.LAVA_POP, 3.0f, 0.5f);
                BossFx.sound(level, c, SoundEvents.LAVA_AMBIENT, 3.0f, 0.6f);
            }
            if (tick == 30) {
                BossFx.sound(level, c, ModSounds.DRAGON_AMBIENT.get(), 5.0f, 0.4f);
            }
        } else if (tick < 125) {
            if (tick == 50) {
                BossFx.sound(level, c, SoundEvents.GENERIC_EXPLODE.value(), 5.0f, 0.5f);
                BossFx.burst(level, ParticleTypes.EXPLOSION, c.add(0, 1, 0), 6, 3.0, 0.0);
                for (int i = 0; i < 6; i++) {
                    BossFx.burst(level, ParticleTypes.LAVA, c.add(0, 1 + i, 0), 20, 2.5, 0.2);
                }
            }
            BossFx.burst(level, ParticleTypes.FLAME, this.position().add(0, 2, 0), 10, 3.5, 0.06);
            BossFx.burst(level, ParticleTypes.LAVA, c.add(0, 0.5, 0), 3, 4.0, 0.1);
            if (tick % 6 == 0) {
                BossFx.sound(level, c, SoundEvents.LAVA_POP, 3.0f, 0.4f);
            }
        } else {
            if (tick == 132) {
                BossFx.sound(level, this.position(), ModSounds.DRAGON_ROAR.get(), 8.0f, 0.42f);
                BossFx.sound(level, this.position(), SoundEvents.ENDER_DRAGON_GROWL, 5.0f, 0.5f);
                this.shake(level, 90, 0.9f, 35);
            }
            if (tick > 132) {
                // Ascheregen über dem Krater
                for (int i = 0; i < 6; i++) {
                    double a = this.random.nextDouble() * Mth.TWO_PI;
                    double r = this.random.nextDouble() * 30;
                    level.sendParticles(ParticleTypes.WHITE_ASH, c.x + Math.cos(a) * r, c.y + 18 + this.random.nextDouble() * 8,
                            c.z + Math.sin(a) * r, 2, 1, 1, 1, 0.0);
                }
            }
        }
    }

    // =====================================================================================
    // Tod
    // =====================================================================================

    @Override
    protected void tickDeathEffects(ServerLevel level, int tick) {
        Vec3 body = this.position().add(0, 3.0 * this.collapse(), 0);
        if (tick == 1) {
            BossFx.sound(level, body, ModSounds.DRAGON_DEATH.get(), 8.0f, 0.4f);
            BossFx.sound(level, body, SoundEvents.ENDER_DRAGON_GROWL, 5.0f, 0.4f);
        }
        if (tick < 60) {
            BossFx.burst(level, ParticleTypes.FLAME, body, 6, 3.0, 0.05);
            BossFx.burst(level, ParticleTypes.LARGE_SMOKE, body.add(0, 2, 0), 3, 2.5, 0.03);
            if (tick % 10 == 0) {
                Vec3 p = body.add((this.random.nextDouble() - 0.5) * 8, this.random.nextDouble() * 3, (this.random.nextDouble() - 0.5) * 8);
                BossFx.burst(level, ParticleTypes.EXPLOSION, p, 1, 0, 0);
                BossFx.sound(level, p, SoundEvents.GENERIC_EXPLODE.value(), 2.0f, 0.9f);
            }
            if (tick == 30) {
                BossFx.sound(level, body, ModSounds.DRAGON_HURT.get(), 6.0f, 0.4f);
            }
        } else if (tick < 130) {
            BossFx.burst(level, ParticleTypes.LARGE_SMOKE, body.add(0, 1.5, 0), 4, 3.0, 0.04);
            BossFx.burst(level, ParticleTypes.WHITE_ASH, body, 6, 4.0, 0.02);
            if (tick % 4 == 0) {
                BossFx.burst(level, ParticleTypes.LAVA, body, 2, 3.0, 0.1);
            }
        } else {
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.MAGMA_BLOCK.defaultBlockState()),
                    body.x, body.y, body.z, 14, 4.0, 1.5, 4.0, 0.1);
            BossFx.burst(level, ParticleTypes.FLAME, body, 8, 4.0, 0.03);
            if (tick == 160) {
                BossFx.burst(level, ParticleTypes.EXPLOSION_EMITTER, body, 1, 0, 0);
                BossFx.burst(level, ParticleTypes.END_ROD, body, 80, 3.0, 0.3);
                BossFx.sound(level, body, SoundEvents.GENERIC_EXPLODE.value(), 6.0f, 0.6f);
                BossFx.sound(level, body, SoundEvents.TOTEM_USE, 3.0f, 0.6f);
            }
        }
    }
}
