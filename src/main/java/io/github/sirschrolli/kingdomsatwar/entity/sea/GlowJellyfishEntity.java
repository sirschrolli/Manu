package io.github.sirschrolli.kingdomsatwar.entity.sea;

import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Bucketable;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.fish.WaterAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Leuchtqualle: eine durchscheinende Qualle, die in Schwärmen durch die warmen und tiefen Meere treibt. Sie pumpt sich mit
 * einem Zusammenziehen der Glocke ein Stück voran und sinkt danach langsam wieder ab; nachts leuchten die Schwärme wie
 * Laternen unter Wasser. Drei Farben: Meerblau, Rosa und Bernstein.
 * <p>
 * Wer ihre Fangfäden berührt, wird genesselt (kurz vergiftet). Mit einem Wassereimer lässt sie sich einfangen – für ein
 * leuchtendes Aquarium.
 */
public class GlowJellyfishEntity extends WaterAnimal implements Bucketable {
    private static final EntityDataAccessor<Integer> DATA_VARIANT = SynchedEntityData.defineId(GlowJellyfishEntity.class,
            EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_FROM_BUCKET = SynchedEntityData.defineId(GlowJellyfishEntity.class,
            EntityDataSerializers.BOOLEAN);
    private static final byte EVENT_PULSE = 71;
    /** Meerblau, Rosa, Bernstein. */
    public static final int VARIANTS = 3;
    private static final int STING_COOLDOWN = 30;

    private int pulseIn;
    private int stingCooldown;
    /** Nur auf dem Client: wann sie zuletzt gepumpt hat. */
    public int pulseTick = -100;

    public GlowJellyfishEntity(EntityType<? extends GlowJellyfishEntity> type, Level level) {
        super(type, level);
        this.pulseIn = 20 + this.random.nextInt(40);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 6.0).add(Attributes.MOVEMENT_SPEED, 0.3);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_VARIANT, 0);
        builder.define(DATA_FROM_BUCKET, false);
    }

    public int getVariant() {
        return Mth.clamp(this.entityData.get(DATA_VARIANT), 0, VARIANTS - 1);
    }

    public void setVariant(int variant) {
        this.entityData.set(DATA_VARIANT, Mth.clamp(variant, 0, VARIANTS - 1));
    }

    // ------------------------------------------------------------------ Treiben

    @Override
    protected void travelInWater(Vec3 input, double baseGravity, boolean isFalling, double oldY) {
        this.move(MoverType.SELF, this.getDeltaMovement());
        // zähes Wasser: der Schwung verebbt, danach sinkt sie ganz langsam
        Vec3 v = this.getDeltaMovement().scale(0.94);
        this.setDeltaMovement(v.x, v.y - 0.0015, v.z);
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (!this.isInWater()) {
            return;
        }
        if (--this.pulseIn <= 0) {
            this.pulse(level);
            this.pulseIn = 40 + this.random.nextInt(50);
        }
        if (this.stingCooldown > 0) {
            this.stingCooldown--;
        } else if (this.tickCount % 5 == 0) {
            this.sting(level);
        }
    }

    /** Ein Pumpstoß: meist schräg nach oben, nahe der Oberfläche und am Grund zur Seite bzw. nach oben. */
    private void pulse(ServerLevel level) {
        BlockPos pos = this.blockPosition();
        boolean nearSurface = !SeaCreatures.isWater(level, pos.above(2));
        boolean nearFloor = !SeaCreatures.isWater(level, pos.below(2));
        double a = this.random.nextDouble() * Math.PI * 2.0;
        double side = 0.35 + this.random.nextDouble() * 0.35;
        double up = nearSurface ? -0.25 : nearFloor ? 1.0 : 0.55 + this.random.nextDouble() * 0.45;
        Vec3 dir = new Vec3(Math.cos(a) * side, up, Math.sin(a) * side).normalize();
        this.setDeltaMovement(this.getDeltaMovement().add(dir.scale(0.11)));
        level.broadcastEntityEvent(this, EVENT_PULSE);
    }

    /** Nesseln: wer die Fangfäden berührt, wird kurz vergiftet (andere Meerestiere nicht). */
    private void sting(ServerLevel level) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            return;
        }
        List<LivingEntity> touching = level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(0.15),
                e -> e != this && e.isAlive() && !(e instanceof GlowJellyfishEntity) && !(e instanceof WaterAnimal)
                        && e.getType().getCategory() != MobCategory.WATER_AMBIENT && !(e instanceof Player p && (p.isCreative() || p.isSpectator())));
        for (LivingEntity target : touching) {
            if (target.hurtServer(level, this.damageSources().mobAttack(this), 1.0f)) {
                target.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0), this);
                this.playSound(ModSounds.GLOW_JELLYFISH_STING.get(), 0.8f, 0.9f + this.random.nextFloat() * 0.3f);
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY(0.5), target.getZ(), 6, 0.3, 0.3, 0.3, 0.05);
                this.stingCooldown = STING_COOLDOWN;
            }
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_PULSE) {
            this.pulseTick = this.tickCount;
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide() && this.isInWater() && this.random.nextInt(12) == 0) {
            this.level().addParticle(ParticleTypes.GLOW, this.getRandomX(0.6), this.getY() + this.random.nextDouble() * 0.6,
                    this.getRandomZ(0.6), 0.0, 0.01, 0.0);
        }
    }

    // ------------------------------------------------------------------ Eimer

    @Override
    public boolean fromBucket() {
        return this.entityData.get(DATA_FROM_BUCKET);
    }

    @Override
    public void setFromBucket(boolean fromBucket) {
        this.entityData.set(DATA_FROM_BUCKET, fromBucket);
    }

    @Override
    public void saveToBucketTag(ItemStack bucket) {
        Bucketable.saveDefaultDataToBucketTag(this, bucket);
        CustomData.update(DataComponents.BUCKET_ENTITY_DATA, bucket, tag -> tag.putInt("Variant", this.getVariant()));
    }

    @Override
    public void loadFromBucketTag(CompoundTag tag) {
        Bucketable.loadDefaultDataFromBucketTag(this, tag);
        tag.getInt("Variant").ifPresent(this::setVariant);
    }

    @Override
    public ItemStack getBucketItemStack() {
        return new ItemStack(SeaBlocks.GLOW_JELLYFISH_BUCKET.get());
    }

    @Override
    public SoundEvent getPickupSound() {
        return SoundEvents.BUCKET_FILL_FISH;
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        return Bucketable.bucketMobPickup(player, hand, this).orElse(super.mobInteract(player, hand));
    }

    @Override
    public boolean requiresCustomPersistence() {
        return super.requiresCustomPersistence() || this.fromBucket();
    }

    @Override
    public boolean removeWhenFarAway(double distanceSqr) {
        return !this.fromBucket() && !this.hasCustomName();
    }

    // ------------------------------------------------------------------ Laichen, Speichern, Geräusche

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
            @Nullable SpawnGroupData groupData) {
        // ein Schwarm hat eine Farbe; warme Meere sind bunter, kalte blau
        if (groupData instanceof Swarm swarm) {
            this.setVariant(swarm.variant);
        } else {
            int variant = pickVariant(level.getBiome(this.blockPosition()), level.getRandom());
            this.setVariant(variant);
            groupData = new Swarm(variant);
        }
        return super.finalizeSpawn(level, difficulty, reason, groupData);
    }

    private static int pickVariant(Holder<Biome> biome, RandomSource random) {
        if (SeaCreatures.warm(biome)) {
            return random.nextInt(10) < 5 ? 1 : random.nextInt(10) < 7 ? 2 : 0;
        }
        if (SeaCreatures.cold(biome)) {
            return random.nextInt(10) < 8 ? 0 : 1;
        }
        return random.nextInt(10) < 6 ? 0 : random.nextInt(2) + 1;
    }

    private record Swarm(int variant) implements SpawnGroupData {}

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("Variant", this.getVariant());
        output.putBoolean("FromBucket", this.fromBucket());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setVariant(input.getIntOr("Variant", 0));
        this.setFromBucket(input.getBooleanOr("FromBucket", false));
    }

    /** Im Wasser zwischen 2 und 28 Blöcken unter dem Meeresspiegel. */
    public static boolean checkJellyfishSpawnRules(EntityType<? extends Mob> type, LevelAccessor level, EntitySpawnReason reason, BlockPos pos,
            RandomSource random) {
        if (EntitySpawnReason.isSpawner(reason)) {
            return true;
        }
        int sea = level.getSeaLevel();
        return pos.getY() <= sea - 2 && pos.getY() >= sea - 28 && SeaCreatures.openWater(level, pos);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.GLOW_JELLYFISH_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.GLOW_JELLYFISH_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.GLOW_JELLYFISH_DEATH.get();
    }

    @Override
    protected float getSoundVolume() {
        return 0.5f;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 240;
    }
}
