package io.github.sirschrolli.kingdomsatwar.entity.boss.ice;

import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossAttack;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossEffects;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossFx;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossPart;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossProjectile;
import io.github.sirschrolli.kingdomsatwar.entity.boss.DragonBossEntity;
import io.github.sirschrolli.kingdomsatwar.entity.boss.attack.CommonAttacks;
import io.github.sirschrolli.kingdomsatwar.entity.boss.attack.ElementAttacks;
import io.github.sirschrolli.kingdomsatwar.entity.boss.attack.ElementFx;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * Glaciara, die Frostkönigin – der Eisdrachen-Boss. Schläft zusammengerollt in einem riesigen Eiskristall in der Mitte
 * ihres Gletscherkessels und sprengt ihn beim Erwachen.
 *
 * <p>Angriffe: Biss, Prankenhieb, Schwanzhieb, Flügelschlag, Brüllen, Frostodem (lässt erstarren), Frostodem im
 * Sturzflug, Eisspeer-Salve, Schneesturm, Eisgefängnis, Eisspeere aus dem Boden (ab Phase 2), Eiszapfenregen (ab
 * Phase 2) und Frostwelle (Phase 3). Schneller und wendiger als Pyrrhon, dafür etwas weniger Leben.
 */
public class GlaciaraEntity extends DragonBossEntity {
    /** In diesem Moment des Erwachens zerspringt der Kristall. */
    public static final int SHATTER_TICK = 88;

    public GlaciaraEntity(EntityType<? extends GlaciaraEntity> type, Level level) {
        super(type, level, DragonType.ICE);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBossAttributes(820.0, 11.0, 0.34);
    }

    /** Bis der Kristall zerspringt, ist von ihr nichts zu sehen (kein Kopf, der aus dem Eis ragt). */
    @Override
    public boolean hiddenDuringAwakening(int ticks) {
        return ticks < SHATTER_TICK;
    }

    @Override
    protected String signatureAttack(int phase) {
        return phase >= 3 ? "icicle_rain" : "blizzard";
    }

    /** Die Frostkönigin erstarrt nicht. */
    @Override
    public boolean canFreeze() {
        return false;
    }

    @Override
    protected int airDuration() {
        return 220;   // fliegt gern und lange
    }

    // =====================================================================================
    // Trefferzonen (langer Hals, langer Schwanz)
    // =====================================================================================

    @Override
    protected BossPart[] createParts() {
        return new BossPart[] {
                new BossPart(this, "head", 2.8f, 2.4f, 1.5f),
                new BossPart(this, "neck", 2.2f, 2.4f, 1.15f),
                new BossPart(this, "neck2", 2.0f, 2.2f, 1.15f),
                new BossPart(this, "tail1", 2.2f, 2.0f, 0.7f),
                new BossPart(this, "tail2", 1.9f, 1.7f, 0.7f),
                new BossPart(this, "tail3", 1.6f, 1.4f, 0.7f),
                new BossPart(this, "tail4", 1.3f, 1.2f, 0.7f),
                new BossPart(this, "tail5", 1.1f, 1.0f, 0.7f),
                new BossPart(this, "wing_l", 5.6f, 1.2f, 0.85f),
                new BossPart(this, "wing_r", 5.6f, 1.2f, 0.85f),
        };
    }

    private Vec3 neckBase() {
        return this.bodyPoint(0, 5.3 * this.collapse(), 2.3);
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
        if (this.getBossState() == State.AWAKENING && this.getStateTicks() < SHATTER_TICK + 10) {
            return 0.6;   // zusammengerollt im Kristall
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
        setAt(p[0], base.add(dir.scale(4.6 + reach)).add(0, 6.0 * c, 0));
        setAt(p[1], base.add(dir.scale(1.2 + reach * 0.3)).add(0, 2.2 * c, 0));
        setAt(p[2], base.add(dir.scale(2.8 + reach * 0.6)).add(0, 4.4 * c, 0));
        this.placePart(p[3], 0, 3.8 * c, -3.9);
        this.placePart(p[4], 0, 3.2 * c, -6.6);
        this.placePart(p[5], 0, 2.6 * c, -9.4);
        this.placePart(p[6], 0, 2.1 * c, -12.2);
        this.placePart(p[7], 0, 1.7 * c, -15.0);
        boolean spread = this.isFlying();
        double wx = spread ? 6.2 : 3.0;
        double wy = spread ? 6.6 : 6.4;
        this.placePart(p[8], wx, wy * c, spread ? 0.6 : -0.3);
        this.placePart(p[9], -wx, wy * c, spread ? 0.6 : -0.3);
    }

    private static void setAt(BossPart part, Vec3 pos) {
        part.setOldPosAndRot();
        part.setPos(pos.x, pos.y - part.getBbHeight() / 2.0, pos.z);
    }

    @Override
    public Vec3 mouthPosition() {
        return this.neckBase().add(this.neckDir().scale(7.2 + this.attackHeadReach())).add(0, 5.4 * this.collapse(), 0);
    }

    // =====================================================================================
    // Angriffe
    // =====================================================================================

    @Override
    protected void registerAttacks(List<BossAttack> list) {
        ElementFx frost = new ElementFx(ModParticles.BOSS_FROST.get(), ParticleTypes.SNOWFLAKE,
                ModSounds.DRAGON_BREATH.get(DragonType.ICE).get(), (boss, e) -> BossEffects.freeze(e, 3));
        list.add(new CommonAttacks.Bite(17, 11.0));
        list.add(new CommonAttacks.Claw(12));
        list.add(new CommonAttacks.TailSweep(11));
        list.add(new CommonAttacks.WingBuffet(6));
        list.add(new CommonAttacks.Roar(24));
        list.add(new CommonAttacks.TakeOff());
        list.add(new CommonAttacks.Land());
        list.add(new ElementAttacks.Breath(frost, 5, 21));
        list.add(new ElementAttacks.AirBreath(frost, 6));
        list.add(new ElementAttacks.Volley(BossProjectile.Kind.ICE_SPEAR, 5, 8));
        list.add(new IceAttacks.Blizzard(20, 3));
        list.add(new IceAttacks.IcePrison());
        list.add(new IceAttacks.IceSpikes(11, 2));
        list.add(new IceAttacks.IcicleRain(12));
        list.add(new ElementAttacks.NovaRing(frost, 11, 20));
    }

    // =====================================================================================
    // Erwachen: Der Eiskristall zerspringt
    // =====================================================================================

    @Override
    public int awakeningTicks() {
        return 175;
    }

    @Override
    protected void tickAwakening(ServerLevel level, int tick) {
        BlockPos home = this.getHome() != null ? this.getHome() : this.blockPosition();
        Vec3 c = Vec3.atBottomCenterOf(home);
        this.setPos(c.x, c.y, c.z);
        this.setDeltaMovement(Vec3.ZERO);
        if (tick < SHATTER_TICK) {
            // Der Kristall glüht auf und bekommt Risse, Frost breitet sich aus
            if (tick % 3 == 0) {
                BossFx.burst(level, ParticleTypes.END_ROD, c.add(0, 4, 0), 3, 2.5, 0.02);
                BossFx.ring(level, ModParticles.BOSS_FROST.get(), c.add(0, 0.3, 0), 3.0 + tick * 0.12, 18, 0.1);
            }
            if (tick % 12 == 0) {
                BossFx.sound(level, c, SoundEvents.AMETHYST_BLOCK_CHIME, 3.0f, 0.4f + tick * 0.006f);
            }
            if (tick % 20 == 10) {
                BossFx.sound(level, c, SoundEvents.GLASS_HIT, 3.0f, 0.5f);
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ModBlocks.FROST_CRYSTAL.get().defaultBlockState()),
                        c.x, c.y + 4, c.z, 20, 2.0, 3.0, 2.0, 0.1);
            }
            if (tick == 40) {
                BossFx.sound(level, c, ModSounds.DRAGON_AMBIENT.get(), 5.0f, 0.55f);
            }
        } else if (tick == SHATTER_TICK) {
            this.shatterCrystal(level, home);
        } else if (tick < 130) {
            BossFx.burst(level, ParticleTypes.SNOWFLAKE, this.position().add(0, 3, 0), 8, 4.0, 0.08);
        } else {
            if (tick == 134) {
                BossFx.sound(level, this.position(), ModSounds.DRAGON_ROAR.get(), 8.0f, 0.55f);
                BossFx.sound(level, this.position(), SoundEvents.ENDER_DRAGON_GROWL, 5.0f, 0.65f);
                this.shake(level, 90, 0.8f, 30);
            }
            if (tick > 134) {
                // Schneetreiben über dem Kessel
                for (int i = 0; i < 6; i++) {
                    double a = this.random.nextDouble() * Mth.TWO_PI;
                    double r = this.random.nextDouble() * 30;
                    level.sendParticles(ParticleTypes.SNOWFLAKE, c.x + Math.cos(a) * r, c.y + 14 + this.random.nextDouble() * 10,
                            c.z + Math.sin(a) * r, 3, 1, 1, 1, 0.02);
                }
            }
        }
    }

    /** Der Eiskristall zerspringt: Splitter fliegen, Frostwelle, Beben. */
    private void shatterCrystal(ServerLevel level, BlockPos home) {
        Vec3 c = Vec3.atBottomCenterOf(home);
        BlockParticleOption shard = new BlockParticleOption(ParticleTypes.BLOCK, ModBlocks.FROST_CRYSTAL.get().defaultBlockState());
        for (BlockPos pos : BlockPos.betweenClosed(home.offset(-10, -1, -10), home.offset(10, 21, 10))) {
            if (level.getBlockState(pos).is(ModBlocks.FROST_CRYSTAL.get())) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                if (this.random.nextInt(3) == 0) {
                    level.sendParticles(shard, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 8, 0.4, 0.4, 0.4, 0.3);
                }
            }
        }
        BossFx.sound(level, c, SoundEvents.GLASS_BREAK, 6.0f, 0.4f);
        BossFx.sound(level, c, SoundEvents.AMETHYST_CLUSTER_BREAK, 5.0f, 0.5f);
        BossFx.sound(level, c, SoundEvents.GENERIC_EXPLODE.value(), 4.0f, 0.7f);
        BossFx.burst(level, ParticleTypes.SNOWFLAKE, c.add(0, 4, 0), 160, 6.0, 0.4);
        BossFx.burst(level, ModParticles.BOSS_FROST.get(), c.add(0, 3, 0), 60, 5.0, 0.1);
        BossFx.ring(level, ParticleTypes.SNOWFLAKE, c.add(0, 0.5, 0), 8.0, 60, 0.2);
        this.shake(level, 90, 1.0f, 25);
        for (var e : this.enemiesAround(c, 9, 6)) {
            DragonBossEntity.pushAway(e, c, 1.6, 0.6);
            BossEffects.freeze(e, 3);
        }
    }

    // =====================================================================================
    // Tod: Sie erstarrt zu Eis und zerspringt
    // =====================================================================================

    @Override
    protected void tickDeathEffects(ServerLevel level, int tick) {
        Vec3 body = this.position().add(0, 3.0 * this.collapse(), 0);
        if (tick == 1) {
            BossFx.sound(level, body, ModSounds.DRAGON_DEATH.get(), 8.0f, 0.55f);
            BossFx.sound(level, body, SoundEvents.ENDER_DRAGON_GROWL, 5.0f, 0.6f);
        }
        if (tick < 60) {
            BossFx.burst(level, ParticleTypes.SNOWFLAKE, body, 8, 3.0, 0.06);
            BossFx.burst(level, ModParticles.BOSS_FROST.get(), body.add(0, 2, 0), 3, 2.5, 0.03);
            if (tick % 10 == 0) {
                Vec3 p = body.add((this.random.nextDouble() - 0.5) * 8, this.random.nextDouble() * 3, (this.random.nextDouble() - 0.5) * 8);
                BossFx.burst(level, ParticleTypes.EXPLOSION, p, 1, 0, 0);
                BossFx.sound(level, p, SoundEvents.GLASS_BREAK, 2.0f, 0.7f);
            }
            if (tick == 30) {
                BossFx.sound(level, body, ModSounds.DRAGON_HURT.get(), 6.0f, 0.55f);
            }
        } else if (tick < 130) {
            BossFx.burst(level, ParticleTypes.SNOWFLAKE, body.add(0, 1.5, 0), 6, 3.5, 0.03);
            BossFx.burst(level, ParticleTypes.WHITE_ASH, body, 8, 4.0, 0.02);
            if (tick % 6 == 0) {
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.PACKED_ICE.defaultBlockState()),
                        body.x, body.y, body.z, 10, 3.0, 1.5, 3.0, 0.1);
                BossFx.sound(level, body, SoundEvents.GLASS_HIT, 1.5f, 0.6f);
            }
        } else {
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ModBlocks.FROST_CRYSTAL.get().defaultBlockState()),
                    body.x, body.y, body.z, 14, 4.0, 1.5, 4.0, 0.15);
            if (tick == 160) {
                BossFx.burst(level, ParticleTypes.EXPLOSION_EMITTER, body, 1, 0, 0);
                BossFx.burst(level, ParticleTypes.END_ROD, body, 80, 3.0, 0.3);
                BossFx.burst(level, ParticleTypes.SNOWFLAKE, body, 200, 6.0, 0.5);
                BossFx.sound(level, body, SoundEvents.GLASS_BREAK, 6.0f, 0.5f);
                BossFx.sound(level, body, SoundEvents.TOTEM_USE, 3.0f, 0.7f);
            }
        }
    }
}
