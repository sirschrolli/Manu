package io.github.sirschrolli.kingdomsatwar.entity.boss.water;

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
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Thalassa, die Herrin der Tiefe – der Wasserdrachen-Boss, eine Seeschlange. Sie lebt im dunklen See inmitten ihrer
 * versunkenen Grotte und verlässt das Wasser nie (nur zum Sprung aus der Tiefe): Wer am Ufer steht, den holt sie mit
 * Biss, Wasserstrahl, Flutwelle und Strudel; wer ins Wasser fällt, ist in ihrem Reich.
 *
 * <p>Angriffe: Biss, Schwanzpeitsche, Wasserstrahl, Blasen-Salve, Flutwelle, Brüllen; ab Phase 2 Strudel und
 * Blasenkäfig, dazu der Sprung aus der Tiefe. Fliegt nicht. Ihr Leib folgt der Spur, die sie durchs Wasser zieht
 * (die Trefferzonen des Schwanzes liegen auf dieser Spur).
 */
public class ThalassaEntity extends DragonBossEntity {
    /** In diesem Moment des Erwachens bricht sie durch die Wasseroberfläche. */
    public static final int SURFACE_TICK = 100;
    private static final int TRAIL = 96;

    private final Vec3[] trail = new Vec3[TRAIL];
    private int trailHead;
    private int trailCount;
    private double lakeRadius = -1;
    private int waterTop = Integer.MIN_VALUE;
    private float circleAngle;

    public ThalassaEntity(EntityType<? extends ThalassaEntity> type, Level level) {
        super(type, level, DragonType.WATER);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBossAttributes(880.0, 9.0, 0.3);
    }

    @Override
    public boolean canFly() {
        return false;
    }

    @Override
    protected void snortParticles() {
        Vec3 mouth = this.mouthPosition();
        for (int i = 0; i < 8; i++) {
            this.level().addParticle(ParticleTypes.SPLASH, mouth.x, mouth.y + 0.4, mouth.z,
                    (this.random.nextDouble() - 0.5) * 0.4, 0.2, (this.random.nextDouble() - 0.5) * 0.4);
        }
    }

    @Override
    protected float modifyIncomingDamage(DamageSource source, float amount) {
        return amount;
    }

    // =====================================================================================
    // Der See: Wasserspiegel, Größe, Schwimmen
    // =====================================================================================

    /** Höhe des obersten Wasserblocks im See (vom Nest aus gesucht). */
    public int waterTop() {
        if (this.waterTop == Integer.MIN_VALUE) {
            BlockPos home = this.getHome() != null ? this.getHome() : this.blockPosition();
            int top = home.getY();
            for (int y = home.getY() + 4; y > home.getY() - 40; y--) {
                if (!this.level().getFluidState(new BlockPos(home.getX(), y, home.getZ())).isEmpty()) {
                    top = y;
                    break;
                }
            }
            this.waterTop = top;
        }
        return this.waterTop;
    }

    /** Halbmesser des Sees an der Oberfläche (aus 16 Richtungen gemessen, der kleinste Wert). */
    public double lakeRadius() {
        if (this.lakeRadius < 0) {
            BlockPos home = this.getHome() != null ? this.getHome() : this.blockPosition();
            int y = this.waterTop();
            double min = 40;
            for (int k = 0; k < 16; k++) {
                double a = k * Mth.TWO_PI / 16;
                int r = 1;
                while (r < 40 && !this.level().getFluidState(BlockPos.containing(home.getX() + 0.5 + Math.cos(a) * r, y,
                        home.getZ() + 0.5 + Math.sin(a) * r)).isEmpty()) {
                    r++;
                }
                min = Math.min(min, r - 1);
            }
            this.lakeRadius = Math.max(6, min);
        }
        return this.lakeRadius;
    }

    public Vec3 lakeCenter() {
        BlockPos home = this.getHome() != null ? this.getHome() : this.blockPosition();
        return new Vec3(home.getX() + 0.5, this.waterTop(), home.getZ() + 0.5);
    }

    /** Hält einen Punkt im See (waagerecht innerhalb des Ufers, senkrecht zwischen Grund und Oberfläche). */
    public Vec3 clampToLake(Vec3 p, double margin) {
        Vec3 c = this.lakeCenter();
        Vec3 d = new Vec3(p.x - c.x, 0, p.z - c.z);
        double max = Math.max(1.0, this.lakeRadius() - margin);
        if (d.length() > max) {
            d = d.normalize().scale(max);
        }
        double y = Mth.clamp(p.y, c.y - 11.0, c.y - 1.2);
        return new Vec3(c.x + d.x, y, c.z + d.z);
    }

    /** Schwimmt weich zu einem Punkt (ohne Schwerkraft, der Körper dreht in Schwimmrichtung). */
    public void swimTowards(Vec3 destination, double speed, double accel) {
        Vec3 to = destination.subtract(this.position());
        double dist = to.length();
        if (dist < 0.01) {
            return;
        }
        Vec3 desired = to.scale(Math.min(speed, dist * 0.25) / dist);
        Vec3 vel = this.getDeltaMovement();
        vel = vel.add(desired.subtract(vel).scale(accel));
        this.setDeltaMovement(vel);
        if (vel.horizontalDistanceSqr() > 0.004) {
            this.turnTowards(this.position().add(vel.x, 0, vel.z), 4.0f);
        }
    }

    @Override
    public void moveInFight(ServerLevel level, LivingEntity target) {
        Vec3 c = this.lakeCenter();
        double r = this.lakeRadius();
        Vec3 toTarget = new Vec3(target.getX() - c.x, 0, target.getZ() - c.z);
        double reachable = r + 9.0;
        if (target.isInWater() || toTarget.length() < reachable) {
            // auf das Ziel zu – bis an den Rand des Sees, dicht unter der Oberfläche, Kopf zum Ziel
            Vec3 goal = this.clampToLake(new Vec3(target.getX(), c.y - 1.6, target.getZ()), 3.5);
            if (target.isInWater()) {
                goal = this.clampToLake(target.position().add(0, -1.0, 0), 2.0);
            }
            double dist = goal.subtract(this.position()).horizontalDistance();
            if (dist > 2.5) {
                this.swimTowards(goal, 0.42, 0.06);
            } else {
                this.setDeltaMovement(this.getDeltaMovement().scale(0.85));
                this.turnTowards(target.position(), 5.0f);
            }
            return;
        }
        this.circle(0.55, 0.3);
    }

    /** Zieht Kreise im See (Bruchteil des Halbmessers, Geschwindigkeit). */
    private void circle(double fraction, double speed) {
        Vec3 c = this.lakeCenter();
        double radius = Math.max(4.0, this.lakeRadius() * fraction);
        this.circleAngle += (float) (speed / radius) * (this.getPhase() == 3 ? 1.3f : 1.0f);
        Vec3 goal = new Vec3(c.x + Math.cos(this.circleAngle) * radius, c.y - 2.5 + Math.sin(this.circleAngle * 0.7) * 1.5,
                c.z + Math.sin(this.circleAngle) * radius);
        this.swimTowards(goal, speed, 0.06);
    }

    @Override
    protected void hoverOrIdle(ServerLevel level) {
        if (this.getMoveMode() == MoveMode.WATER) {
            this.circle(0.45, 0.2);
        } else {
            super.hoverOrIdle(level);
        }
    }

    @Override
    protected boolean returnHome(ServerLevel level, Vec3 home, double dist) {
        Vec3 c = this.lakeCenter();
        Vec3 goal = new Vec3(c.x, c.y - 9.0, c.z);
        if (this.position().distanceTo(goal) > 3.0) {
            this.swimTowards(this.clampToLake(goal, 2.0), 0.35, 0.06);
        } else {
            this.setDeltaMovement(this.getDeltaMovement().scale(0.8));
            if (this.getHealth() >= this.getMaxHealth() * 0.99f) {
                this.fallAsleep();
            }
        }
        return true;
    }

    // =====================================================================================
    // Trefferzonen: Kopf und Hals vorn, der Schlangenleib auf der Spur durchs Wasser
    // =====================================================================================

    @Override
    protected BossPart[] createParts() {
        return new BossPart[] {
                new BossPart(this, "head", 2.6f, 2.2f, 1.5f),
                new BossPart(this, "neck", 2.2f, 2.2f, 1.1f),
                new BossPart(this, "neck2", 2.0f, 2.0f, 1.1f),
                new BossPart(this, "tail1", 2.6f, 2.4f, 0.8f),
                new BossPart(this, "tail2", 2.4f, 2.2f, 0.8f),
                new BossPart(this, "tail3", 2.1f, 2.0f, 0.75f),
                new BossPart(this, "tail4", 1.8f, 1.7f, 0.7f),
                new BossPart(this, "tail5", 1.4f, 1.4f, 0.7f),
                new BossPart(this, "fluke", 1.6f, 2.6f, 0.7f),
        };
    }

    private Vec3 neckBase() {
        return this.bodyPoint(0, 1.6, 3.2);
    }

    private Vec3 neckDir() {
        Vec3 body = Vec3.directionFromRotation(0, this.yBodyRot);
        Vec3 head = Vec3.directionFromRotation(0, this.yHeadRot);
        Vec3 d = body.scale(0.3).add(head.scale(0.7));
        return d.lengthSqr() < 1.0e-4 ? body : d.normalize();
    }

    /** Wie hoch der Hals gerade aufgerichtet ist (Aufbäumen bei Flutwelle und Brüllen). */
    private double neckRise() {
        int anim = this.getAttackAnim();
        return anim == io.github.sirschrolli.kingdomsatwar.entity.boss.BossAnims.RING
                || anim == io.github.sirschrolli.kingdomsatwar.entity.boss.BossAnims.ROAR ? 3.0 : 0.0;
    }

    @Override
    protected void layoutParts() {
        double reach = this.attackHeadReach();
        Vec3 base = this.neckBase();
        Vec3 dir = this.neckDir();
        double up = this.neckRise();
        BossPart[] p = this.parts;
        setAt(p[0], base.add(dir.scale(6.6 + reach)).add(0, 2.8 + up, 0));
        setAt(p[1], base.add(dir.scale(1.6 + reach * 0.3)).add(0, 1.0 + up * 0.3, 0));
        setAt(p[2], base.add(dir.scale(3.8 + reach * 0.6)).add(0, 2.2 + up * 0.7, 0));
        // der Leib liegt auf der Spur: Abstände in Blöcken hinter der Körpermitte
        double[] along = {3.2, 6.6, 10.0, 13.2, 16.2, 19.0};
        for (int i = 0; i < 6; i++) {
            Vec3 at = this.trailPoint(along[i]);
            setAt(p[3 + i], at.add(0, 1.2 - i * 0.12, 0));
        }
    }

    private static void setAt(BossPart part, Vec3 pos) {
        part.setOldPosAndRot();
        part.setPos(pos.x, pos.y - part.getBbHeight() / 2.0, pos.z);
    }

    /** Punkt auf der Spur, {@code distance} Blöcke hinter der Körpermitte (fehlt die Spur, gerade nach hinten). */
    private Vec3 trailPoint(double distance) {
        Vec3 prev = this.position();
        double left = distance;
        for (int k = 0; k < this.trailCount; k++) {
            Vec3 q = this.trail[Math.floorMod(this.trailHead - 1 - k, TRAIL)];
            double seg = q.distanceTo(prev);
            if (seg >= left) {
                return prev.add(q.subtract(prev).scale(left / Math.max(seg, 1.0e-4)));
            }
            left -= seg;
            prev = q;
        }
        Vec3 back = Vec3.directionFromRotation(0, this.yBodyRot).scale(-1);
        return prev.add(back.scale(left));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        // Spur aufzeichnen (alle 0,4 Blöcke ein Punkt)
        Vec3 pos = this.position();
        Vec3 last = this.trailCount > 0 ? this.trail[Math.floorMod(this.trailHead - 1, TRAIL)] : null;
        if (last == null || last.distanceToSqr(pos) > 0.16) {
            this.trail[this.trailHead] = pos;
            this.trailHead = (this.trailHead + 1) % TRAIL;
            this.trailCount = Math.min(TRAIL, this.trailCount + 1);
        }
    }

    @Override
    public Vec3 mouthPosition() {
        return this.neckBase().add(this.neckDir().scale(8.4 + this.attackHeadReach())).add(0, 2.6 + this.neckRise(), 0);
    }

    // =====================================================================================
    // Angriffe
    // =====================================================================================

    @Override
    protected String signatureAttack(int phase) {
        return phase >= 3 ? "tidal_wave" : "whirlpool";
    }

    @Override
    protected void registerAttacks(List<BossAttack> list) {
        ElementFx water = new ElementFx(ModParticles.BOSS_WATER.get(), ParticleTypes.BUBBLE,
                ModSounds.DRAGON_BREATH.get(DragonType.WATER).get(), WaterAttacks::drench);
        list.add(new CommonAttacks.Bite(20, 13.0));
        list.add(new CommonAttacks.TailSweep(15));
        list.add(new CommonAttacks.Roar(18));
        list.add(new ElementAttacks.Breath(water, 6, 24));
        list.add(new ElementAttacks.Volley(BossProjectile.Kind.BUBBLE, 6, 7));
        list.add(new WaterAttacks.TidalWave(12, 22));
        list.add(new WaterAttacks.Whirlpool(4));
        list.add(new WaterAttacks.BubblePrison(10));
        list.add(new WaterAttacks.DepthLeap(18));
    }

    // =====================================================================================
    // Erwachen: Ein Strudel bildet sich, sie steigt aus der Tiefe
    // =====================================================================================

    @Override
    public int awakeningTicks() {
        return 180;
    }

    @Override
    protected void tickAwakening(ServerLevel level, int tick) {
        Vec3 c = this.lakeCenter();
        double bottom = c.y - 11.0;
        double surface = c.y - 1.4;
        double u = Mth.clamp((tick - 20) / (double) (SURFACE_TICK - 20), 0.0, 1.0);
        double y = Mth.lerp(u * u * (3 - 2 * u), bottom, surface);
        this.setPos(c.x, y, c.z);
        this.setDeltaMovement(Vec3.ZERO);
        this.setYRot(this.getYRot() + (tick < SURFACE_TICK ? 3.0f : 0.0f));
        this.yBodyRot = this.getYRot();
        if (tick < SURFACE_TICK) {
            // der Strudel: Blasen steigen spiralförmig auf, die Oberfläche kräuselt sich
            double r = 2.0 + tick * 0.08;
            for (int i = 0; i < 6; i++) {
                double a = tick * 0.35 + i * Mth.TWO_PI / 6;
                level.sendParticles(ParticleTypes.BUBBLE_COLUMN_UP, c.x + Math.cos(a) * r, c.y - 4 + level.getRandom().nextDouble() * 3,
                        c.z + Math.sin(a) * r, 2, 0.2, 0.4, 0.2, 0.05);
                level.sendParticles(ParticleTypes.SPLASH, c.x + Math.cos(a) * r, c.y + 1.0, c.z + Math.sin(a) * r, 3, 0.3, 0.05, 0.3, 0.1);
            }
            if (tick % 3 == 0) {
                BossFx.ring(level, ParticleTypes.BUBBLE_POP, c.add(0, 1.0, 0), r, 24, 0.0);
            }
            if (tick % 20 == 0) {
                BossFx.sound(level, c, SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_AMBIENT, 4.0f, 0.5f);
                BossFx.sound(level, c, SoundEvents.CONDUIT_AMBIENT, 3.0f, 0.5f);
            }
            if (tick == 40) {
                BossFx.sound(level, c, ModSounds.DRAGON_AMBIENT.get(), 5.0f, 0.5f);
            }
            return;
        }
        if (tick == SURFACE_TICK) {
            BossFx.sound(level, c, SoundEvents.GENERIC_SPLASH, 6.0f, 0.4f);
            BossFx.sound(level, c, SoundEvents.PLAYER_SPLASH_HIGH_SPEED, 6.0f, 0.5f);
            for (int i = 0; i < 4; i++) {
                level.sendParticles(ParticleTypes.SPLASH, c.x, c.y + 2, c.z, 120, 3.0, 1.5, 3.0, 0.6);
            }
            BossFx.burst(level, ModParticles.BOSS_WATER.get(), c.add(0, 3, 0), 80, 4.0, 0.3);
            this.shake(level, 80, 1.0f, 25);
            for (var e : this.enemiesAround(c, 14, 8)) {
                DragonBossEntity.pushAway(e, c, 1.3, 0.6);
            }
        } else if (tick == 132) {
            BossFx.sound(level, this.position(), ModSounds.DRAGON_ROAR.get(), 8.0f, 0.55f);
            BossFx.sound(level, this.position(), SoundEvents.ELDER_GUARDIAN_CURSE, 3.0f, 0.6f);
            this.shake(level, 90, 0.9f, 30);
            Vec3 mouth = this.mouthPosition();
            level.sendParticles(ParticleTypes.SPLASH, mouth.x, mouth.y, mouth.z, 80, 1.0, 1.0, 1.0, 0.5);
        } else if (tick % 4 == 0) {
            level.sendParticles(ParticleTypes.FALLING_WATER, this.getX(), this.getY() + 4, this.getZ(), 16, 2.5, 1.5, 2.5, 0.0);
        }
        if (tick == this.awakeningTicks() - 1) {
            this.setMoveMode(MoveMode.WATER);
        }
    }

    @Override
    protected void tickSlumber(ServerLevel level, int tick) {
        // sie taucht ab in die Tiefe des Sees
        Vec3 c = this.lakeCenter();
        this.setPos(this.getX(), Math.max(c.y - 11.0, this.getY() - 0.15), this.getZ());
        level.sendParticles(ParticleTypes.BUBBLE, this.getX(), this.getY() + 2, this.getZ(), 6, 2.0, 1.0, 2.0, 0.05);
    }

    // =====================================================================================
    // Tod: Sie windet sich, erschlafft und sinkt auf den Grund des Sees
    // =====================================================================================

    @Override
    protected void tickDeathEffects(ServerLevel level, int tick) {
        Vec3 body = this.position().add(0, 1.5, 0);
        if (tick == 1) {
            BossFx.sound(level, body, ModSounds.DRAGON_DEATH.get(), 8.0f, 0.6f);
            BossFx.sound(level, body, SoundEvents.ELDER_GUARDIAN_DEATH, 4.0f, 0.6f);
        }
        if (tick < 50) {
            level.sendParticles(ParticleTypes.SPLASH, body.x, body.y + 2, body.z, 12, 2.5, 0.5, 2.5, 0.3);
            if (tick % 8 == 0) {
                BossFx.sound(level, body, SoundEvents.GENERIC_SPLASH, 3.0f, 0.5f);
            }
        } else if (tick < 150) {
            Vec3 c = this.lakeCenter();
            this.setPos(this.getX(), Math.max(c.y - 10.5, this.getY() - 0.08), this.getZ());
            level.sendParticles(ParticleTypes.BUBBLE, body.x, body.y, body.z, 6, 2.5, 1.0, 2.5, 0.05);
        } else if (tick == 155) {
            Vec3 c = this.lakeCenter();
            level.sendParticles(ParticleTypes.BUBBLE_COLUMN_UP, c.x, c.y - 4, c.z, 200, 3.0, 3.0, 3.0, 0.2);
            BossFx.burst(level, ParticleTypes.END_ROD, c.add(0, 1.5, 0), 60, 3.0, 0.2);
            BossFx.sound(level, c, SoundEvents.CONDUIT_ACTIVATE, 4.0f, 0.8f);
            BossFx.sound(level, c, SoundEvents.TOTEM_USE, 3.0f, 0.8f);
        }
    }
}
