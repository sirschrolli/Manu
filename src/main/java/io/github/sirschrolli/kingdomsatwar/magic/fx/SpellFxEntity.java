package io.github.sirschrolli.kingdomsatwar.magic.fx;

import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModParticles;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

/**
 * Sichtbare Zauberwirkungen der Erzmagier und des Aetherit-Stabs: Runenkreise am Boden, Strahlen und Blitzbögen,
 * Säulen aus Feuer und Wasser, Felsspitzen, Steinfäuste, Dornenranken, Giftblüten, Eiskerker, Wasserblasen und
 * Flutwellen. Gezeichnet wird sie von {@code SpellFxRenderer}; den Schaden richtet aus, wer den Zauber wirkt – über
 * {@link #onTick} und {@link #onExpire} genau dann, wenn die Wirkung zu sehen ist (auch wenn der Zaubernde längst etwas
 * anderes tut). Wird nicht gespeichert.
 */
public class SpellFxEntity extends Entity {
    /** Art der Wirkung. Größe ({@link #size()}) und Zusatzwert ({@link #param()}) bedeuten je Art etwas anderes. */
    public enum Kind {
        /** Warnkreis am Boden: füllt sich, bis es einschlägt. Größe = Halbmesser. */
        CIRCLE,
        /** Zauberkreis unter dem Zaubernden (folgt ihm). Größe = Halbmesser, Zusatz = Ladung 0–1. */
        CAST_CIRCLE,
        /** Gerader Lichtstrahl zum Endpunkt. Größe = Breite. */
        BEAM,
        /** Gezackter Blitzbogen zum Endpunkt. Größe = Breite. */
        ARC,
        /** Säule aus Feuer, Wasser oder Licht. Größe = Halbmesser, Zusatz = Höhe. */
        PILLAR,
        /** Felsspitze, die aus dem Boden bricht. Größe = Maßstab. */
        SPIKE,
        /** Steinerne Faust, die aus dem Boden schlägt. Größe = Maßstab. */
        FIST,
        /** Dornenranke, die aus dem Boden peitscht. Größe = Maßstab, Zusatz = Zeitpunkt des Peitschenhiebs (0–1). */
        VINE,
        /** Riesige Giftblüte, die wächst, sich öffnet und Sporen ausstößt. Größe = Maßstab. */
        FLOWER,
        /** Kerker aus Eiskristallen um ein Wesen. Größe = Maßstab. */
        PRISON,
        /** Wasserblase um ein Wesen (folgt ihm). Größe = Durchmesser. */
        BUBBLE,
        /** Flutwelle, die in Blickrichtung rollt. Größe = Breite, Zusatz = Geschwindigkeit je Tick. */
        WAVE;

        public static Kind of(int index) {
            return values()[Mth.clamp(index, 0, values().length - 1)];
        }
    }

    /** Kein Element: der Aether (Zauber ohne Element, Stab). */
    public static final int AETHER = 255;

    private static final EntityDataAccessor<Byte> DATA_KIND = SynchedEntityData.defineId(SpellFxEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Integer> DATA_STYLE = SynchedEntityData.defineId(SpellFxEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_COLOR = SynchedEntityData.defineId(SpellFxEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_LIFE = SynchedEntityData.defineId(SpellFxEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_SIZE = SynchedEntityData.defineId(SpellFxEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_PARAM = SynchedEntityData.defineId(SpellFxEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Vector3fc> DATA_END = SynchedEntityData.defineId(SpellFxEntity.class, EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<Integer> DATA_ANCHOR = SynchedEntityData.defineId(SpellFxEntity.class, EntityDataSerializers.INT);

    /** Nur auf dem Server: geschieht jeden Tick (z. B. Schaden, solange die Welle rollt). */
    private @Nullable Consumer<SpellFxEntity> onTick;
    /** Nur auf dem Server: geschieht am Ende (z. B. der Eiskerker zerspringt). */
    private @Nullable Consumer<SpellFxEntity> onExpire;

    public SpellFxEntity(EntityType<? extends SpellFxEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    /**
     * Eine neue Wirkung (noch nicht in der Welt – erst {@link #spawn()}).
     *
     * @param element Element (Partikel und Stil) oder null für den Aether
     * @param color Leitfarbe (RGB)
     * @param life Dauer in Ticks
     */
    public static SpellFxEntity create(ServerLevel level, Kind kind, Vec3 pos, @Nullable DragonType element, int color, int life, float size) {
        SpellFxEntity fx = new SpellFxEntity(ModEntities.SPELL_FX.get(), level);
        fx.setPos(pos.x, pos.y, pos.z);
        fx.entityData.set(DATA_KIND, (byte) kind.ordinal());
        fx.entityData.set(DATA_STYLE, element == null ? AETHER : element.ordinal());
        fx.entityData.set(DATA_COLOR, color & 0xFFFFFF);
        fx.entityData.set(DATA_LIFE, Math.max(1, life));
        fx.entityData.set(DATA_SIZE, size);
        fx.setYRot(level.getRandom().nextFloat() * 360.0f);
        return fx;
    }

    /** Kurzform: Wirkung in der Farbe eines Elements. */
    public static SpellFxEntity create(ServerLevel level, Kind kind, Vec3 pos, DragonType element, int life, float size) {
        return create(level, kind, pos, element, element.color(), life, size);
    }

    public SpellFxEntity spawn() {
        this.level().addFreshEntity(this);
        return this;
    }

    public SpellFxEntity param(float param) {
        this.entityData.set(DATA_PARAM, param);
        return this;
    }

    /** Endpunkt eines Strahls oder Blitzbogens (Weltkoordinaten). */
    public SpellFxEntity end(Vec3 end) {
        Vec3 rel = end.subtract(this.position());
        this.entityData.set(DATA_END, new Vector3f((float) rel.x, (float) rel.y, (float) rel.z));
        return this;
    }

    /** Folgt diesem Wesen (Zauberkreis unter dem Zaubernden, Blase um den Gefangenen, Strahl aus dem Stab). */
    public SpellFxEntity anchor(Entity entity) {
        this.entityData.set(DATA_ANCHOR, entity.getId());
        this.setPos(entity.getX(), entity.getY(), entity.getZ());
        return this;
    }

    /** Blickrichtung (Flutwelle, Faust, Strahlursprung). */
    public SpellFxEntity facing(float yaw) {
        this.setYRot(yaw);
        this.yRotO = yaw;
        return this;
    }

    public SpellFxEntity onTick(Consumer<SpellFxEntity> action) {
        this.onTick = action;
        return this;
    }

    public SpellFxEntity onExpire(Consumer<SpellFxEntity> action) {
        this.onExpire = action;
        return this;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_KIND, (byte) 0);
        builder.define(DATA_STYLE, AETHER);
        builder.define(DATA_COLOR, 0xFFFFFF);
        builder.define(DATA_LIFE, 20);
        builder.define(DATA_SIZE, 1.0f);
        builder.define(DATA_PARAM, 0.0f);
        builder.define(DATA_END, new Vector3f());
        builder.define(DATA_ANCHOR, -1);
    }

    // ------------------------------------------------------------------ Werte

    public Kind kind() {
        return Kind.of(this.entityData.get(DATA_KIND));
    }

    /** Element der Wirkung (null: Aether). */
    public @Nullable DragonType element() {
        int style = this.entityData.get(DATA_STYLE);
        return style >= 0 && style < DragonType.values().length ? DragonType.values()[style] : null;
    }

    public int color() {
        return this.entityData.get(DATA_COLOR);
    }

    public int life() {
        return this.entityData.get(DATA_LIFE);
    }

    public float size() {
        return this.entityData.get(DATA_SIZE);
    }

    public float param() {
        return this.entityData.get(DATA_PARAM);
    }

    public void setParam(float param) {
        if (Math.abs(param - this.param()) > 1.0e-3f) {
            this.entityData.set(DATA_PARAM, param);
        }
    }

    public void setSize(float size) {
        if (Math.abs(size - this.size()) > 1.0e-3f) {
            this.entityData.set(DATA_SIZE, size);
        }
    }

    /** Endpunkt relativ zur Wirkung. */
    public Vec3 endOffset() {
        Vector3fc v = this.entityData.get(DATA_END);
        return new Vec3(v.x(), v.y(), v.z());
    }

    /** Endpunkt in Weltkoordinaten. */
    public Vec3 endPoint() {
        return this.position().add(this.endOffset());
    }

    /** Verschiebt den Endpunkt (Strahl folgt seinem Ziel), ohne bei winzigen Änderungen neu zu senden. */
    public void moveEnd(Vec3 end) {
        Vec3 rel = end.subtract(this.position());
        Vector3fc old = this.entityData.get(DATA_END);
        if (Math.abs(old.x() - rel.x) + Math.abs(old.y() - rel.y) + Math.abs(old.z() - rel.z) > 0.02) {
            this.entityData.set(DATA_END, new Vector3f((float) rel.x, (float) rel.y, (float) rel.z));
        }
    }

    public @Nullable Entity anchor() {
        int id = this.entityData.get(DATA_ANCHOR);
        return id < 0 ? null : this.level().getEntity(id);
    }

    /** Fortschritt 0–1 über die Lebensdauer. */
    public float progress(float partialTicks) {
        return Mth.clamp((this.tickCount + partialTicks) / this.life(), 0.0f, 1.0f);
    }

    /** Richtung der Flutwelle (waagerecht). */
    public Vec3 forward() {
        return Vec3.directionFromRotation(0.0f, this.getYRot());
    }

    // ------------------------------------------------------------------ Ablauf

    @Override
    public void tick() {
        super.tick();
        Entity anchor = this.anchor();
        if (this.entityData.get(DATA_ANCHOR) >= 0) {
            if (anchor == null || anchor.isRemoved()) {
                if (!this.level().isClientSide()) {
                    this.discard();
                    return;
                }
            } else {
                this.setPos(anchor.getX(), anchor.getY(), anchor.getZ());
            }
        }
        if (this.kind() == Kind.WAVE) {
            this.setPos(this.position().add(this.forward().scale(this.param())));
        }
        if (this.level() instanceof ServerLevel) {
            if (this.onTick != null) {
                this.onTick.accept(this);
            }
            if (this.tickCount >= this.life() && !this.isRemoved()) {
                if (this.onExpire != null) {
                    this.onExpire.accept(this);
                }
                this.discard();
            }
        } else {
            this.clientEffects();
        }
    }

    /** Partikel der Wirkungen (nur auf dem Bildschirm). */
    private void clientEffects() {
        Level level = this.level();
        int t = this.tickCount;
        int life = this.life();
        float size = this.size();
        DragonType element = this.element();
        switch (this.kind()) {
            case CIRCLE -> {
                if (this.progress(0.0f) > 0.55f && this.random.nextInt(2) == 0) {
                    double a = this.random.nextDouble() * Mth.TWO_PI;
                    level.addParticle(new DustParticleOptions(this.color(), 0.9f), this.getX() + Math.cos(a) * size, this.getY() + 0.1,
                            this.getZ() + Math.sin(a) * size, 0.0, 0.04, 0.0);
                }
            }
            case CAST_CIRCLE -> {
                float charge = this.param();
                if (this.random.nextFloat() < 0.25f + charge * 0.6f) {
                    double a = this.random.nextDouble() * Mth.TWO_PI;
                    double r = size * (0.45 + 0.55 * charge);
                    level.addParticle(new DustParticleOptions(this.color(), 0.7f + charge * 0.5f), this.getX() + Math.cos(a) * r,
                            this.getY() + 0.08, this.getZ() + Math.sin(a) * r, 0.0, 0.03 + charge * 0.05, 0.0);
                }
            }
            case BEAM, ARC -> {
                if (t < 3) {
                    Vec3 end = this.endPoint();
                    level.addParticle(this.kind() == Kind.ARC ? ParticleTypes.ELECTRIC_SPARK : ParticleTypes.END_ROD, end.x, end.y, end.z,
                            (this.random.nextDouble() - 0.5) * 0.2, this.random.nextDouble() * 0.15, (this.random.nextDouble() - 0.5) * 0.2);
                }
            }
            case PILLAR -> {
                ParticleOptions particle = element == DragonType.WATER ? ParticleTypes.SPLASH
                        : element == DragonType.FIRE ? ModParticles.BOSS_FLAME.get() : ParticleTypes.END_ROD;
                if (t < life - 4) {
                    for (int i = 0; i < 2; i++) {
                        double a = this.random.nextDouble() * Mth.TWO_PI;
                        double r = this.random.nextDouble() * size;
                        level.addParticle(particle, this.getX() + Math.cos(a) * r, this.getY() + this.random.nextDouble() * this.param(),
                                this.getZ() + Math.sin(a) * r, 0.0, 0.12, 0.0);
                    }
                }
            }
            case SPIKE, FIST, VINE -> {
                if (t == 1) {
                    this.groundBurst(size * (this.kind() == Kind.FIST ? 1.4 : 0.9), 18);
                }
                if (t == life - 6) {
                    this.groundBurst(size * 0.8, 8);
                }
            }
            case FLOWER -> {
                if (t == 1) {
                    this.groundBurst(size * 0.8, 12);
                }
                if (t > 20 && t < life - 12 && this.random.nextInt(2) == 0) {
                    double a = this.random.nextDouble() * Mth.TWO_PI;
                    double r = this.random.nextDouble() * 1.2 * size;
                    level.addParticle(ModParticles.BOSS_SPORE.get(), this.getX() + Math.cos(a) * r, this.getY() + 1.9 * size,
                            this.getZ() + Math.sin(a) * r, Math.cos(a) * 0.08, 0.05, Math.sin(a) * 0.08);
                }
            }
            case PRISON -> {
                if (this.random.nextInt(3) == 0) {
                    level.addParticle(ParticleTypes.SNOWFLAKE, this.getX() + (this.random.nextDouble() - 0.5) * 1.6 * size,
                            this.getY() + this.random.nextDouble() * 2.4 * size, this.getZ() + (this.random.nextDouble() - 0.5) * 1.6 * size,
                            0.0, -0.02, 0.0);
                }
                if (t == life - 1) {
                    BlockParticleOption ice = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.PACKED_ICE.defaultBlockState());
                    for (int i = 0; i < 40; i++) {
                        level.addParticle(ice, this.getX() + (this.random.nextDouble() - 0.5) * 1.4 * size, this.getY() + this.random.nextDouble() * 2.2 * size,
                                this.getZ() + (this.random.nextDouble() - 0.5) * 1.4 * size, 0.0, 0.1, 0.0);
                    }
                }
            }
            case BUBBLE -> {
                Entity anchor = this.anchor();
                double cy = anchor != null ? anchor.getY(0.5) : this.getY() + size * 0.5;
                if (this.random.nextInt(2) == 0) {
                    level.addParticle(ParticleTypes.BUBBLE, this.getX() + (this.random.nextDouble() - 0.5) * size * 0.8,
                            cy + (this.random.nextDouble() - 0.5) * size * 0.8, this.getZ() + (this.random.nextDouble() - 0.5) * size * 0.8,
                            0.0, 0.05, 0.0);
                }
                if (t == life - 1) {
                    for (int i = 0; i < 30; i++) {
                        level.addParticle(i % 2 == 0 ? ParticleTypes.SPLASH : ParticleTypes.BUBBLE_POP, this.getX() + (this.random.nextDouble() - 0.5) * size,
                                cy + (this.random.nextDouble() - 0.5) * size, this.getZ() + (this.random.nextDouble() - 0.5) * size, 0.0, 0.1, 0.0);
                    }
                }
            }
            case WAVE -> {
                Vec3 side = new Vec3(-this.forward().z, 0.0, this.forward().x);
                for (int i = 0; i < 3; i++) {
                    double s = (this.random.nextDouble() - 0.5) * size;
                    level.addParticle(ParticleTypes.SPLASH, this.getX() + side.x * s, this.getY() + 2.2 + this.random.nextDouble() * 0.4,
                            this.getZ() + side.z * s, 0.0, 0.1, 0.0);
                }
                if (this.random.nextInt(2) == 0) {
                    double s = (this.random.nextDouble() - 0.5) * size;
                    level.addParticle(ParticleTypes.BUBBLE_POP, this.getX() + side.x * s, this.getY() + 0.3, this.getZ() + side.z * s, 0.0, 0.05, 0.0);
                }
            }
        }
    }

    /** Erde und Steinchen spritzen aus dem Boden. */
    private void groundBurst(double radius, int count) {
        BlockPos below = BlockPos.containing(this.getX(), this.getY() - 0.2, this.getZ());
        BlockState ground = this.level().getBlockState(below);
        if (ground.isAir()) {
            ground = Blocks.DIRT.defaultBlockState();
        }
        BlockParticleOption particle = new BlockParticleOption(ParticleTypes.BLOCK, ground);
        for (int i = 0; i < count; i++) {
            double a = this.random.nextDouble() * Mth.TWO_PI;
            double r = this.random.nextDouble() * radius;
            this.level().addParticle(particle, this.getX() + Math.cos(a) * r, this.getY() + 0.1, this.getZ() + Math.sin(a) * r,
                    Math.cos(a) * 0.15, 0.25 + this.random.nextDouble() * 0.2, Math.sin(a) * 0.15);
        }
    }

    // ------------------------------------------------------------------ Hilfen für den Schaden

    /** Lebewesen in einem waagerechten Kreis um die Wirkung (Höhe: von knapp unter bis {@code height} über ihr). */
    public java.util.List<LivingEntity> livingInside(double radius, double height) {
        AABB box = new AABB(this.getX() - radius, this.getY() - 1.0, this.getZ() - radius, this.getX() + radius, this.getY() + height,
                this.getZ() + radius);
        return this.level().getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive()
                && Mth.square(e.getX() - this.getX()) + Mth.square(e.getZ() - this.getZ()) <= radius * radius);
    }

    // ------------------------------------------------------------------ Entity

    /** Was die Wirkung auf dem Bildschirm einnimmt (der Strahl reicht bis zum Endpunkt, die Säule in die Höhe). */
    public AABB cullingBox() {
        AABB box = this.getBoundingBox();
        float size = this.size();
        return switch (this.kind()) {
            case BEAM, ARC -> box.expandTowards(this.endOffset()).inflate(size + 1.0);
            case PILLAR -> box.inflate(size + 0.5).expandTowards(0.0, this.param(), 0.0);
            case WAVE -> box.inflate(size * 0.6 + 1.0, 3.0, size * 0.6 + 1.0);
            case CIRCLE, CAST_CIRCLE -> box.inflate(size + 0.5, 1.0, size + 0.5);
            default -> box.inflate(Math.max(2.0, size * 2.5), 3.5 * Math.max(1.0, size), Math.max(2.0, size * 2.5));
        };
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 160.0 * 160.0;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public boolean ignoreExplosion(Explosion explosion) {
        return true;
    }

    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
    }
}
