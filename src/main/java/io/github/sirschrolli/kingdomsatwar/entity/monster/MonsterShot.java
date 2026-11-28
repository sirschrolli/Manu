package io.github.sirschrolli.kingdomsatwar.entity.monster;

import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Geschosse der neuen Monster: der Schlammklumpen des Moorschlurfers (fliegt im Bogen, verlangsamt und spritzt in die
 * Augen), der Magmabatzen des Glutwurms (fliegt im Bogen, setzt in Brand und entzündet, wo er aufschlägt) und das
 * Seelenfeuer des Aschengeists (fliegt gerade, eine Spur aus blauen Flammen, setzt in Brand und lähmt).
 *
 * <p>Seit 1.14 werfen und speien auch andere Monster (siehe {@code entity.combat}): Spinnen <b>Netze</b> (kleben fest –
 * ein Spinnennetz bleibt kurz liegen), Wiedergänger und Trolle <b>Brocken</b>, Mumien <b>Sand</b> (blendet), Schleime
 * <b>Schleimbatzen</b> (verlangsamen), Brandgebeine glühende <b>Schädel</b> (verdorren) und Leerwandler
 * <b>Leerenbrocken</b> (schwindelig).
 */
public class MonsterShot extends ThrowableItemProjectile {
    public enum Kind {
        MUD, MAGMA, SOUL, CHORUS, WEB, ROCK, SAND, SLIME, SKULL, VOID
    }

    private static final EntityDataAccessor<Byte> DATA_KIND = SynchedEntityData.defineId(MonsterShot.class, EntityDataSerializers.BYTE);
    private float damage = 4.0f;
    private int age;

    public MonsterShot(EntityType<? extends MonsterShot> type, Level level) {
        super(type, level);
    }

    /** Wirft ein Geschoss von {@code from} mit der Geschwindigkeit {@code velocity}. */
    public static MonsterShot shoot(ServerLevel level, LivingEntity owner, Kind kind, Vec3 from, Vec3 velocity, float damage) {
        return shoot(level, owner, kind, from, velocity, damage, switch (kind) {
            case MUD -> new ItemStack(Items.MUD);
            case MAGMA -> new ItemStack(Items.FIRE_CHARGE);
            case SOUL, SAND -> ItemStack.EMPTY;
            case CHORUS -> new ItemStack(Items.CHORUS_FRUIT);
            case WEB -> new ItemStack(Items.COBWEB);
            case ROCK -> new ItemStack(Items.COBBLESTONE);
            case SLIME -> new ItemStack(Items.SLIME_BALL);
            case SKULL -> new ItemStack(Items.WITHER_SKELETON_SKULL);
            case VOID -> new ItemStack(Items.END_STONE);
        });
    }

    /** Wie oben, mit eigenem Bild (der Leerwandler wirft den Block, den er trägt). */
    public static MonsterShot shoot(ServerLevel level, LivingEntity owner, Kind kind, Vec3 from, Vec3 velocity, float damage, ItemStack item) {
        MonsterShot shot = new MonsterShot(ModEntities.MONSTER_SHOT.get(), level);
        shot.entityData.set(DATA_KIND, (byte) kind.ordinal());
        shot.setItem(item);
        shot.damage = damage;
        shot.setOwner(owner);
        shot.setPos(from.x, from.y, from.z);
        shot.setDeltaMovement(velocity);
        level.addFreshEntity(shot);
        return shot;
    }

    /**
     * Geschwindigkeit, mit der ein Wurf im Bogen (Schwerkraft {@code gravity}) nach {@code ticks} Ticks bei {@code to} ankommt –
     * mit der Luftreibung der Geschosse (je Tick erst Schwerkraft, dann ×0,99, dann die Bewegung).
     */
    public static Vec3 lob(Vec3 from, Vec3 to, int ticks, double gravity) {
        Vec3 d = to.subtract(from);
        double drag = 0.99;
        double sum = drag * (1.0 - Math.pow(drag, ticks)) / (1.0 - drag);
        double fall = gravity * drag / (1.0 - drag) * (ticks - sum);
        return new Vec3(d.x / sum, (d.y + fall) / sum, d.z / sum);
    }

    public Kind kind() {
        return Kind.values()[Mth.clamp(this.entityData.get(DATA_KIND), 0, Kind.values().length - 1)];
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_KIND, (byte) 0);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.MUD;
    }

    @Override
    protected double getDefaultGravity() {
        return this.kind() == Kind.SOUL ? 0.0 : 0.04;
    }

    @Override
    public void tick() {
        super.tick();
        Level level = this.level();
        if (level.isClientSide()) {
            Vec3 v = this.getDeltaMovement();
            switch (this.kind()) {
                case MUD -> {
                    if (this.random.nextInt(2) == 0) {
                        level.addParticle(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.MUD.defaultBlockState()), this.getX(), this.getY(),
                                this.getZ(), 0.0, 0.0, 0.0);
                    }
                }
                case MAGMA -> {
                    level.addParticle(ParticleTypes.FLAME, this.getX() - v.x * 0.5, this.getY() - v.y * 0.5, this.getZ() - v.z * 0.5, 0.0, 0.01, 0.0);
                    if (this.random.nextInt(3) == 0) {
                        level.addParticle(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY(), this.getZ(), 0.0, 0.02, 0.0);
                    }
                    if (this.random.nextInt(4) == 0) {
                        level.addParticle(ParticleTypes.DRIPPING_LAVA, this.getX(), this.getY() - 0.1, this.getZ(), 0.0, 0.0, 0.0);
                    }
                }
                case CHORUS -> {
                    if (this.random.nextInt(2) == 0) {
                        level.addParticle(ParticleTypes.PORTAL, this.getX(), this.getY(), this.getZ(), -v.x * 0.2, -v.y * 0.2, -v.z * 0.2);
                    }
                }
                case WEB -> {
                    if (this.random.nextInt(2) == 0) {
                        level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.COBWEB.defaultBlockState()), this.getX(), this.getY(),
                                this.getZ(), 0.0, 0.0, 0.0);
                    }
                }
                case ROCK -> {
                    if (this.random.nextInt(3) == 0) {
                        level.addParticle(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.COBBLESTONE.defaultBlockState()), this.getX(),
                                this.getY(), this.getZ(), 0.0, 0.0, 0.0);
                    }
                }
                case SAND -> {
                    // eine Wolke aus Wüstensand, die nachrieselt
                    for (int i = 0; i < 3; i++) {
                        level.addParticle(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState()),
                                this.getX() + (this.random.nextDouble() - 0.5) * 0.4, this.getY() + (this.random.nextDouble() - 0.5) * 0.4,
                                this.getZ() + (this.random.nextDouble() - 0.5) * 0.4, 0.0, 0.0, 0.0);
                    }
                    level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SAND.defaultBlockState()), this.getX(), this.getY(),
                            this.getZ(), -v.x * 0.1, -v.y * 0.1, -v.z * 0.1);
                }
                case SLIME -> {
                    if (this.random.nextInt(2) == 0) {
                        level.addParticle(ParticleTypes.ITEM_SLIME, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
                    }
                }
                case SKULL -> {
                    level.addParticle(ParticleTypes.SMOKE, this.getX() - v.x * 0.5, this.getY() - v.y * 0.5, this.getZ() - v.z * 0.5, 0.0, 0.01, 0.0);
                    if (this.random.nextInt(2) == 0) {
                        level.addParticle(ParticleTypes.FLAME, this.getX(), this.getY() + 0.1, this.getZ(), 0.0, 0.01, 0.0);
                    }
                }
                case VOID -> {
                    level.addParticle(ParticleTypes.REVERSE_PORTAL, this.getX(), this.getY(), this.getZ(), -v.x * 0.1, -v.y * 0.1, -v.z * 0.1);
                    if (this.random.nextInt(2) == 0) {
                        level.addParticle(ParticleTypes.PORTAL, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
                    }
                }
                case SOUL -> {
                    // ein Ball aus blauen Flammen mit einem Schweif
                    for (int i = 0; i < 3; i++) {
                        level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, this.getX() + (this.random.nextDouble() - 0.5) * 0.25,
                                this.getY() + (this.random.nextDouble() - 0.5) * 0.25, this.getZ() + (this.random.nextDouble() - 0.5) * 0.25,
                                -v.x * 0.1, -v.y * 0.1, -v.z * 0.1);
                    }
                    if (this.random.nextInt(2) == 0) {
                        level.addParticle(ParticleTypes.SOUL, this.getX(), this.getY(), this.getZ(), -v.x * 0.05, 0.02, -v.z * 0.05);
                    }
                }
            }
        } else if (++this.age > 200) {
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        super.onHitEntity(hit);
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        Entity target = hit.getEntity();
        Entity owner = this.getOwner();
        if (target == owner) {
            return;
        }
        if (!target.hurtServer(level, this.damageSources().mobProjectile(this, owner instanceof LivingEntity l ? l : null), this.damage)) {
            return;
        }
        if (target instanceof LivingEntity living) {
            switch (this.kind()) {
                case MUD -> {
                    // Schlamm in den Augen und an den Beinen
                    living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 70, 1), owner);
                    living.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 24, 0), owner);
                }
                case MAGMA -> living.igniteForSeconds(5.0f);
                case CHORUS -> {
                    // wie nach einer Chorusfrucht: weg, an eine zufällige Stelle in der Nähe – und ihm wird übel
                    ChorusMawEntity.chorusTeleport(level, living, 10.0);
                    ChorusMawEntity.dizzy(living, owner);
                }
                case SOUL -> {
                    living.igniteForSeconds(3.0f);
                    living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 0), owner);
                    living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 0), owner);
                }
                case WEB -> {
                    // klebt: kaum ein Schritt möglich, und unter den Füßen bleibt ein Netz kurz liegen
                    living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 3), owner);
                    web(level, living.blockPosition());
                }
                case ROCK -> living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 20, 1), owner);
                case SAND -> {
                    living.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 45, 0), owner);
                    living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 0), owner);
                }
                case SLIME -> living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 70, 1), owner);
                case SKULL -> {
                    living.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 0), owner);
                    living.igniteForSeconds(2.0f);
                }
                case VOID -> {
                    living.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 100, 0), owner);
                    living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 0), owner);
                }
            }
        }
    }

    /** Ein Spinnennetz, das nach drei Sekunden wieder vergeht (nur wo Platz ist). */
    private static void web(ServerLevel level, BlockPos pos) {
        if (io.github.sirschrolli.kingdomsatwar.world.TemporaryBlocks.canReplace(level, pos) && level.getBlockState(pos).isAir()) {
            io.github.sirschrolli.kingdomsatwar.world.TemporaryBlocks.place(level, pos, Blocks.COBWEB.defaultBlockState(), WEB_TICKS);
        }
    }

    /** So lange (Ticks) bleibt das Netz einer Spinne liegen. */
    public static final int WEB_TICKS = 60;

    @Override
    protected void onHitBlock(BlockHitResult hit) {
        super.onHitBlock(hit);
        if (this.kind() == Kind.WEB && this.level() instanceof ServerLevel level) {
            web(level, hit.getBlockPos().relative(hit.getDirection()));
        }
        if (this.kind() == Kind.MAGMA && this.level() instanceof ServerLevel level) {
            // der Batzen entzündet, wo er aufschlägt
            BlockPos at = hit.getBlockPos().relative(hit.getDirection());
            if (level.isEmptyBlock(at) && BaseFireBlock.canBePlacedAt(level, at, hit.getDirection())) {
                level.setBlockAndUpdate(at, BaseFireBlock.getState(level, at));
            }
        }
    }

    @Override
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        Vec3 p = this.position();
        switch (this.kind()) {
            case MUD -> {
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.MUD.defaultBlockState()), p.x, p.y, p.z, 24, 0.3, 0.3, 0.3, 0.15);
                level.playSound(null, p.x, p.y, p.z, ModSounds.MIRE_SHAMBLER_SPLAT.get(), SoundSource.HOSTILE, 1.0f, 0.9f + this.random.nextFloat() * 0.2f);
            }
            case MAGMA -> {
                level.sendParticles(ParticleTypes.LAVA, p.x, p.y, p.z, 8, 0.3, 0.2, 0.3, 0.0);
                level.sendParticles(ParticleTypes.FLAME, p.x, p.y, p.z, 16, 0.3, 0.3, 0.3, 0.06);
                level.playSound(null, p.x, p.y, p.z, SoundEvents.FIRECHARGE_USE, SoundSource.HOSTILE, 1.0f, 0.8f + this.random.nextFloat() * 0.2f);
            }
            case CHORUS -> {
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, p.x, p.y, p.z, 30, 0.3, 0.3, 0.3, 0.2);
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.CHORUS_FLOWER.defaultBlockState()), p.x, p.y, p.z, 12, 0.2,
                        0.2, 0.2, 0.1);
                level.playSound(null, p.x, p.y, p.z, ModSounds.CHORUS_MAW_TELEPORT.get(), SoundSource.HOSTILE, 1.0f, 1.2f + this.random.nextFloat() * 0.2f);
            }
            case SOUL -> {
                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, p.x, p.y, p.z, 24, 0.25, 0.25, 0.25, 0.08);
                level.sendParticles(ParticleTypes.SOUL, p.x, p.y, p.z, 6, 0.2, 0.2, 0.2, 0.03);
                level.playSound(null, p.x, p.y, p.z, SoundEvents.SOUL_ESCAPE.value(), SoundSource.HOSTILE, 1.2f, 0.9f + this.random.nextFloat() * 0.2f);
            }
            case WEB -> {
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.COBWEB.defaultBlockState()), p.x, p.y, p.z, 20, 0.35, 0.35, 0.35, 0.1);
                level.playSound(null, p.x, p.y, p.z, SoundEvents.WOOL_PLACE, SoundSource.HOSTILE, 1.0f, 0.7f);
                level.playSound(null, p.x, p.y, p.z, SoundEvents.SLIME_SQUISH_SMALL, SoundSource.HOSTILE, 0.8f, 1.3f);
            }
            case ROCK -> {
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.COBBLESTONE.defaultBlockState()), p.x, p.y, p.z, 26, 0.3, 0.3,
                        0.3, 0.15);
                level.playSound(null, p.x, p.y, p.z, SoundEvents.STONE_BREAK, SoundSource.HOSTILE, 1.2f, 0.7f + this.random.nextFloat() * 0.2f);
            }
            case SAND -> {
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SAND.defaultBlockState()), p.x, p.y, p.z, 40, 0.6, 0.5, 0.6, 0.1);
                level.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState()), p.x, p.y + 0.5, p.z, 30, 0.8,
                        0.6, 0.8, 0.0);
                level.playSound(null, p.x, p.y, p.z, SoundEvents.SAND_BREAK, SoundSource.HOSTILE, 1.4f, 0.8f);
            }
            case SLIME -> {
                level.sendParticles(ParticleTypes.ITEM_SLIME, p.x, p.y, p.z, 22, 0.3, 0.3, 0.3, 0.15);
                level.playSound(null, p.x, p.y, p.z, SoundEvents.SLIME_SQUISH, SoundSource.HOSTILE, 1.0f, 0.9f);
            }
            case SKULL -> {
                level.sendParticles(ParticleTypes.SMOKE, p.x, p.y, p.z, 20, 0.3, 0.3, 0.3, 0.05);
                level.sendParticles(ParticleTypes.FLAME, p.x, p.y, p.z, 10, 0.25, 0.25, 0.25, 0.05);
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.BONE_BLOCK.defaultBlockState()), p.x, p.y, p.z, 12, 0.2, 0.2,
                        0.2, 0.1);
                level.playSound(null, p.x, p.y, p.z, SoundEvents.SKELETON_HURT, SoundSource.HOSTILE, 1.0f, 0.6f);
            }
            case VOID -> {
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, p.x, p.y, p.z, 40, 0.4, 0.4, 0.4, 0.2);
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, this.getItem().getItem() instanceof net.minecraft.world.item.BlockItem block
                        ? block.getBlock().defaultBlockState() : Blocks.END_STONE.defaultBlockState()), p.x, p.y, p.z, 20, 0.3, 0.3, 0.3, 0.15);
                level.playSound(null, p.x, p.y, p.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 0.8f, 0.6f);
            }
        }
        this.discard();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("Kind", this.kind().ordinal());
        output.putFloat("Damage", this.damage);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.entityData.set(DATA_KIND, (byte) Mth.clamp(input.getIntOr("Kind", 0), 0, Kind.values().length - 1));
        this.damage = input.getFloatOr("Damage", 4.0f);
    }
}
