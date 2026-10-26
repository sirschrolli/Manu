package io.github.sirschrolli.kingdomsatwar.entity.boss;

import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.hurtingprojectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Geschosse der Drachen-Bosse: Feuerbälle, Meteore, Eisspeere, Eiszapfen, Dornensaat, Felsbrocken, Blitzkugeln und
 * Wasserblasen. Sie zerplatzen beim Aufprall, ohne Blöcke zu zerstören, und treffen nur Gegner des Bosses.
 */
public class BossProjectile extends AbstractHurtingProjectile {
    /**
     * Art des Geschosses: Größe, Wirkungsradius, Wirkungsdauer in Sekunden (Brand, Frost, Gift, Betäubung …) und ob
     * es wie ein Stein herabfällt.
     */
    public enum Kind {
        FIREBALL(1.0f, 3.0, 4, false),
        METEOR(2.2f, 4.2, 5, true),
        /** Eisspeer (Glaciara): schnell und gerade, lässt erstarren. */
        ICE_SPEAR(1.0f, 2.6, 4, false),
        /** Eiszapfen (Glaciara): stürzt vom Himmel. */
        ICICLE(1.3f, 3.0, 4, true),
        /** Dornensaat (Verdantis): vergiftet. */
        THORN(0.8f, 2.4, 5, false),
        /** Felsbrocken (Terragon): fliegt im Bogen, stößt weit weg. */
        BOULDER(2.2f, 4.0, 0, true),
        /** Blitzkugel (Voltarion): betäubt kurz. */
        SPARK(1.1f, 3.2, 2, false),
        /** Wasserblase (Thalassa): verlangsamt und stößt weg. */
        BUBBLE(1.5f, 3.2, 4, false),
        /** Dornensaat (Verdantis, vom Himmel): vergiftet, wo sie einschlägt, wächst kurz Dornengestrüpp. */
        SEED(1.0f, 2.8, 4, true),
        /** Seelenpfeil (Nyxar): zehrt (Verdorren) und lässt die Sicht verdunkeln. */
        SOUL_BOLT(1.1f, 3.0, 3, false);

        public final float size;
        public final double radius;
        public final int effectSeconds;
        public final boolean falls;

        Kind(float size, double radius, int effectSeconds, boolean falls) {
            this.size = size;
            this.radius = radius;
            this.effectSeconds = effectSeconds;
            this.falls = falls;
        }
    }

    private static final EntityDataAccessor<Byte> DATA_KIND = SynchedEntityData.defineId(BossProjectile.class, EntityDataSerializers.BYTE);
    private float damage = 8.0f;
    private int life;

    public BossProjectile(EntityType<? extends BossProjectile> type, Level level) {
        super(type, level);
        this.accelerationPower = 0.0;
    }

    /** Schießt ein Geschoss aus {@code from} in Richtung {@code dir}. */
    public static BossProjectile shoot(ServerLevel level, DragonBossEntity boss, Kind kind, Vec3 from, Vec3 dir, double speed, float damage) {
        BossProjectile p = new BossProjectile(ModEntities.BOSS_PROJECTILE.get(), level);
        p.setKind(kind);
        p.damage = damage * boss.damageMultiplier();
        p.setOwner(boss);
        p.setPos(from.x, from.y, from.z);
        p.setDeltaMovement(dir.normalize().scale(speed));
        p.accelerationPower = 0.02;
        level.addFreshEntity(p);
        return p;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_KIND, (byte) 0);
    }

    public Kind getKind() {
        return Kind.values()[Mth.clamp(this.entityData.get(DATA_KIND), 0, Kind.values().length - 1)];
    }

    public void setKind(Kind kind) {
        this.entityData.set(DATA_KIND, (byte) kind.ordinal());
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("Kind", this.getKind().ordinal());
        output.putFloat("Damage", this.damage);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setKind(Kind.values()[Mth.clamp(input.getIntOr("Kind", 0), 0, Kind.values().length - 1)]);
        this.damage = input.getFloatOr("Damage", 8.0f);
    }

    @Override
    public void tick() {
        Kind kind = this.getKind();
        if (kind.falls && !this.level().isClientSide()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0, kind == Kind.BOULDER ? -0.05 : -0.04, 0));
        }
        super.tick();
        if (this.level().isClientSide()) {
            this.trail(kind);
        } else if (++this.life > 200) {
            this.discard();
        }
    }

    /** Spur hinter dem Geschoss (nur auf dem Bildschirm). */
    private void trail(Kind kind) {
        ParticleOptions main;
        ParticleOptions smoke;
        int count;
        switch (kind) {
            case FIREBALL, METEOR -> {
                main = ParticleTypes.FLAME;
                smoke = kind == Kind.METEOR ? ParticleTypes.LARGE_SMOKE : ParticleTypes.SMOKE;
                count = kind == Kind.METEOR ? 4 : 2;
            }
            case ICE_SPEAR, ICICLE -> {
                main = ParticleTypes.SNOWFLAKE;
                smoke = io.github.sirschrolli.kingdomsatwar.registry.ModParticles.BOSS_FROST.get();
                count = 2;
            }
            case THORN, SEED -> {
                main = ParticleTypes.SPORE_BLOSSOM_AIR;
                smoke = ParticleTypes.COMPOSTER;
                count = 1;
            }
            case BOULDER -> {
                main = new net.minecraft.core.particles.BlockParticleOption(ParticleTypes.BLOCK,
                        net.minecraft.world.level.block.Blocks.TUFF.defaultBlockState());
                smoke = io.github.sirschrolli.kingdomsatwar.registry.ModParticles.BOSS_DUST.get();
                count = 2;
            }
            case SPARK -> {
                main = ParticleTypes.ELECTRIC_SPARK;
                smoke = io.github.sirschrolli.kingdomsatwar.registry.ModParticles.BOSS_SPARK.get();
                count = 3;
            }
            case BUBBLE -> {
                main = ParticleTypes.BUBBLE;
                smoke = ParticleTypes.SPLASH;
                count = 2;
            }
            case SOUL_BOLT -> {
                main = ParticleTypes.SCULK_SOUL;
                smoke = io.github.sirschrolli.kingdomsatwar.registry.ModParticles.BOSS_SHADOW.get();
                count = 1;
            }
            default -> {
                main = ParticleTypes.SMOKE;
                smoke = ParticleTypes.SMOKE;
                count = 1;
            }
        }
        for (int i = 0; i < count; i++) {
            this.level().addParticle(main, this.getX() + (this.random.nextDouble() - 0.5) * kind.size,
                    this.getY() + (this.random.nextDouble() - 0.5) * kind.size, this.getZ() + (this.random.nextDouble() - 0.5) * kind.size,
                    0, 0, 0);
        }
        this.level().addParticle(smoke, this.getX(), this.getY(), this.getZ(), 0, 0.02, 0);
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        if (entity instanceof DragonBossEntity || entity instanceof BossPart || entity instanceof BossProjectile) {
            return false;
        }
        return super.canHitEntity(entity);
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (this.level() instanceof ServerLevel level) {
            this.explode(level);
            this.discard();
        }
    }

    private void explode(ServerLevel level) {
        Kind kind = this.getKind();
        Vec3 c = this.position();
        this.impactEffects(level, kind, c);
        DragonBossEntity boss = this.getOwner() instanceof DragonBossEntity b ? b : null;
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(kind.radius))) {
            if (boss != null ? !boss.isBossEnemy(e) : e instanceof DragonBossEntity) {
                continue;
            }
            double d = e.position().add(0, e.getBbHeight() * 0.5, 0).distanceTo(c);
            if (d > kind.radius + 0.5) {
                continue;
            }
            float f = (float) Math.max(0.35, 1.0 - d / (kind.radius + 0.5));
            DamageSource source = boss != null ? this.damageSources().mobProjectile(this, boss) : this.damageSources().explosion(this, null);
            e.hurtServer(level, source, this.damage * f);
            BossEffects.apply(e, kind, kind.effectSeconds);
            double push = kind == Kind.BOULDER ? 1.4 : kind == Kind.BUBBLE ? 1.0 : 0.7;
            DragonBossEntity.pushAway(e, c, push * f, 0.4 * f);
        }
    }

    private void impactEffects(ServerLevel level, Kind kind, Vec3 c) {
        float volume = kind == Kind.METEOR || kind == Kind.BOULDER ? 3.0f : 1.6f;
        float pitch = 0.7f + this.random.nextFloat() * 0.2f;
        switch (kind) {
            case FIREBALL, METEOR -> {
                level.sendParticles(ParticleTypes.EXPLOSION, c.x, c.y, c.z, kind == Kind.METEOR ? 4 : 1, kind.radius * 0.3, 0.3, kind.radius * 0.3, 0);
                level.sendParticles(ParticleTypes.FLAME, c.x, c.y, c.z, 40, kind.radius * 0.4, 0.4, kind.radius * 0.4, 0.08);
                level.sendParticles(ParticleTypes.LAVA, c.x, c.y, c.z, 12, kind.radius * 0.3, 0.2, kind.radius * 0.3, 0.1);
                this.sound(level, c, SoundEvents.GENERIC_EXPLODE.value(), volume, pitch);
            }
            case ICE_SPEAR, ICICLE -> {
                level.sendParticles(new net.minecraft.core.particles.BlockParticleOption(ParticleTypes.BLOCK,
                        net.minecraft.world.level.block.Blocks.PACKED_ICE.defaultBlockState()), c.x, c.y, c.z, 40, kind.radius * 0.3, 0.3,
                        kind.radius * 0.3, 0.15);
                level.sendParticles(ParticleTypes.SNOWFLAKE, c.x, c.y, c.z, 30, kind.radius * 0.4, 0.4, kind.radius * 0.4, 0.05);
                level.sendParticles(io.github.sirschrolli.kingdomsatwar.registry.ModParticles.BOSS_FROST.get(), c.x, c.y, c.z, 12,
                        kind.radius * 0.3, 0.3, kind.radius * 0.3, 0.02);
                this.sound(level, c, SoundEvents.GLASS_BREAK, volume, 0.6f + this.random.nextFloat() * 0.2f);
                this.sound(level, c, SoundEvents.PLAYER_HURT_FREEZE, 1.0f, 0.8f);
            }
            case SEED -> {
                level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, c.x, c.y, c.z, 30, kind.radius * 0.4, 0.4, kind.radius * 0.4, 0.02);
                level.sendParticles(io.github.sirschrolli.kingdomsatwar.registry.ModParticles.BOSS_SPORE.get(), c.x, c.y, c.z, 10,
                        kind.radius * 0.4, 0.3, kind.radius * 0.4, 0.02);
                this.sound(level, c, SoundEvents.SWEET_BERRY_BUSH_PLACE, volume, 0.6f);
                this.sound(level, c, SoundEvents.ROOTED_DIRT_BREAK, 1.2f, 0.7f);
                this.growBrambles(level, c);
            }
            case THORN -> {
                level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, c.x, c.y, c.z, 30, kind.radius * 0.4, 0.4, kind.radius * 0.4, 0.02);
                level.sendParticles(ParticleTypes.COMPOSTER, c.x, c.y, c.z, 20, kind.radius * 0.3, 0.3, kind.radius * 0.3, 0.05);
                level.sendParticles(io.github.sirschrolli.kingdomsatwar.registry.ModParticles.BOSS_SPORE.get(), c.x, c.y, c.z, 14,
                        kind.radius * 0.4, 0.4, kind.radius * 0.4, 0.02);
                this.sound(level, c, SoundEvents.SWEET_BERRY_BUSH_BREAK, volume, pitch);
            }
            case BOULDER -> {
                level.sendParticles(new net.minecraft.core.particles.BlockParticleOption(ParticleTypes.BLOCK,
                        net.minecraft.world.level.block.Blocks.TUFF.defaultBlockState()), c.x, c.y, c.z, 60, kind.radius * 0.35, 0.4,
                        kind.radius * 0.35, 0.2);
                level.sendParticles(io.github.sirschrolli.kingdomsatwar.registry.ModParticles.BOSS_DUST.get(), c.x, c.y, c.z, 24,
                        kind.radius * 0.5, 0.4, kind.radius * 0.5, 0.03);
                this.sound(level, c, SoundEvents.GENERIC_EXPLODE.value(), volume, 0.5f);
                this.sound(level, c, SoundEvents.STONE_BREAK, 2.0f, 0.5f);
            }
            case SPARK -> {
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, c.x, c.y, c.z, 50, kind.radius * 0.4, 0.4, kind.radius * 0.4, 0.4);
                level.sendParticles(net.minecraft.core.particles.ColorParticleOption.create(ParticleTypes.FLASH, 0xFFE8F4FF), c.x, c.y, c.z, 1, 0, 0, 0, 0);
                level.sendParticles(io.github.sirschrolli.kingdomsatwar.registry.ModParticles.BOSS_SPARK.get(), c.x, c.y, c.z, 16,
                        kind.radius * 0.3, 0.3, kind.radius * 0.3, 0.05);
                this.sound(level, c, SoundEvents.LIGHTNING_BOLT_IMPACT, volume, 1.2f);
            }
            case BUBBLE -> {
                level.sendParticles(ParticleTypes.SPLASH, c.x, c.y, c.z, 60, kind.radius * 0.4, 0.4, kind.radius * 0.4, 0.2);
                level.sendParticles(ParticleTypes.BUBBLE_POP, c.x, c.y, c.z, 30, kind.radius * 0.4, 0.4, kind.radius * 0.4, 0.1);
                level.sendParticles(io.github.sirschrolli.kingdomsatwar.registry.ModParticles.BOSS_WATER.get(), c.x, c.y, c.z, 16,
                        kind.radius * 0.3, 0.3, kind.radius * 0.3, 0.05);
                this.sound(level, c, SoundEvents.GENERIC_SPLASH, volume, 0.6f);
                this.sound(level, c, SoundEvents.BUBBLE_COLUMN_UPWARDS_INSIDE, 1.5f, 0.6f);
            }
            case SOUL_BOLT -> {
                level.sendParticles(ParticleTypes.SCULK_SOUL, c.x, c.y, c.z, 30, kind.radius * 0.35, 0.4, kind.radius * 0.35, 0.06);
                level.sendParticles(ParticleTypes.SOUL, c.x, c.y, c.z, 14, kind.radius * 0.3, 0.3, kind.radius * 0.3, 0.04);
                level.sendParticles(io.github.sirschrolli.kingdomsatwar.registry.ModParticles.BOSS_SHADOW.get(), c.x, c.y, c.z, 16,
                        kind.radius * 0.35, 0.3, kind.radius * 0.35, 0.02);
                this.sound(level, c, SoundEvents.SCULK_SHRIEKER_SHRIEK, 1.2f, 1.4f);
                this.sound(level, c, SoundEvents.SOUL_ESCAPE.value(), volume, 0.6f);
            }
        }
    }

    /** Dornengestrüpp schießt rund um den Einschlag aus dem Boden (verschwindet nach zehn Sekunden). */
    private void growBrambles(ServerLevel level, Vec3 c) {
        net.minecraft.core.BlockPos center = net.minecraft.core.BlockPos.containing(c.x, c.y + 0.2, c.z);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (this.random.nextFloat() > 0.6f) {
                    continue;
                }
                for (int dy = 1; dy >= -2; dy--) {
                    net.minecraft.core.BlockPos pos = center.offset(dx, dy, dz);
                    if (level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), net.minecraft.core.Direction.UP)
                            && io.github.sirschrolli.kingdomsatwar.world.TemporaryBlocks.canReplace(level, pos)) {
                        io.github.sirschrolli.kingdomsatwar.world.TemporaryBlocks.place(level, pos,
                                io.github.sirschrolli.kingdomsatwar.registry.ModBlocks.BRAMBLE.get().defaultBlockState(), 200);
                        break;
                    }
                }
            }
        }
    }

    private void sound(ServerLevel level, Vec3 c, net.minecraft.sounds.SoundEvent sound, float volume, float pitch) {
        level.playSound(null, c.x, c.y, c.z, sound, net.minecraft.sounds.SoundSource.HOSTILE, volume, pitch);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        return false;
    }

    @Override
    protected boolean shouldBurn() {
        return false;
    }

    @Override
    protected @Nullable ParticleOptions getTrailParticle() {
        return null;
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    protected float getInertia() {
        return 1.0f;
    }
}
