package io.github.sirschrolli.kingdomsatwar.entity.nether;

import io.github.sirschrolli.kingdomsatwar.entity.creature.nether.Brandgebein;
import io.github.sirschrolli.kingdomsatwar.registry.ForgeBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Glutklumpen: ein Klumpen geschmolzenen Metalls, den Malleus aus seiner Esse schöpft und im hohen Bogen schleudert. Er
 * zieht Flammen und Rauch hinter sich her und zerplatzt beim Aufschlag zu einer Glutpfütze: Wer im Umkreis von gut zwei
 * Blöcken steht, verbrennt. Blöcke beschädigt er nicht.
 */
public class GlutklumpenEntity extends ThrowableItemProjectile {
    public static final double RADIUS = 2.3;
    private static final double GRAVITY = 0.05;
    private float damage = 6.0f;

    public GlutklumpenEntity(EntityType<? extends GlutklumpenEntity> type, Level level) {
        super(type, level);
    }

    public GlutklumpenEntity(Level level, LivingEntity owner) {
        super(ModEntities.GLUTKLUMPEN.get(), owner, level, new ItemStack(ForgeBlocks.MOLTEN_GLOB.get()));
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    @Override
    protected Item getDefaultItem() {
        return ForgeBlocks.MOLTEN_GLOB.get();
    }

    @Override
    protected double getDefaultGravity() {
        return GRAVITY;
    }

    /**
     * Wirft im Bogen von {@code from} nach {@code to}: der flachere der beiden Wurfwinkel, der bei {@code speed} Blöcken
     * je Tick (und der Schwerkraft des Klumpens) dort ankommt; reicht die Kraft nicht, im Winkel der größten Weite.
     */
    public void lob(Vec3 from, Vec3 to, double speed) {
        this.setPos(from.x, from.y, from.z);
        double dx = to.x - from.x;
        double dz = to.z - from.z;
        double dy = to.y - from.y;
        double flat = Math.sqrt(dx * dx + dz * dz);
        if (flat < 1.0e-3) {
            this.setDeltaMovement(0.0, speed, 0.0);
            return;
        }
        // ein wenig weiter zielen: der Luftwiderstand bremst den Klumpen unterwegs
        double d = flat * 1.06;
        double v2 = speed * speed;
        double disc = v2 * v2 - GRAVITY * (GRAVITY * d * d + 2.0 * dy * v2);
        double angle = disc < 0.0 ? Math.PI / 4.0 : Math.atan((v2 - Math.sqrt(disc)) / (GRAVITY * d));
        double horizontal = Math.cos(angle) * speed;
        this.setDeltaMovement(dx / flat * horizontal, Math.sin(angle) * speed, dz / flat * horizontal);
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return super.canHitEntity(entity) && !(entity instanceof GlutschmiedEntity) && !(entity instanceof Brandgebein);
    }

    @Override
    public void tick() {
        super.tick();
        Level level = this.level();
        if (level.isClientSide()) {
            Vec3 v = this.getDeltaMovement();
            for (int i = 0; i < 2; i++) {
                double f = i * 0.5;
                level.addParticle(ParticleTypes.FLAME, this.getX() - v.x * f + (this.random.nextDouble() - 0.5) * 0.25,
                        this.getY() + 0.2 - v.y * f + (this.random.nextDouble() - 0.5) * 0.25,
                        this.getZ() - v.z * f + (this.random.nextDouble() - 0.5) * 0.25, 0.0, 0.01, 0.0);
            }
            if (this.random.nextInt(2) == 0) {
                level.addParticle(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 0.2, this.getZ(), 0.0, 0.02, 0.0);
            }
            if (this.random.nextInt(4) == 0) {
                level.addParticle(ParticleTypes.DRIPPING_LAVA, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
            }
        } else if (this.tickCount > 200) {
            this.discard();
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (this.level() instanceof ServerLevel level && !this.isRemoved()) {
            this.burst(level);
            this.discard();
        }
    }

    /** Zerplatzt: Glut spritzt nach allen Seiten, wer nahe steht, verbrennt. */
    private void burst(ServerLevel level) {
        double x = this.getX();
        double y = this.getY();
        double z = this.getZ();
        level.playSound(null, x, y, z, ModSounds.GLUTSCHMIED_GLOB.get(), SoundSource.HOSTILE, 1.6f, 0.85f + this.random.nextFloat() * 0.3f);
        level.sendParticles(ParticleTypes.LAVA, x, y + 0.2, z, 12, 0.5, 0.2, 0.5, 0.0);
        level.sendParticles(ParticleTypes.FLAME, x, y + 0.3, z, 36, 0.9, 0.3, 0.9, 0.06);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, x, y + 0.4, z, 10, 0.6, 0.3, 0.6, 0.02);
        level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0xFFFF9A3A), x, y + 0.3, z, 1, 0.0, 0.0, 0.0, 0.0);
        Entity owner = this.getOwner();
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(RADIUS),
                e -> e.isAlive() && e != owner && !(e instanceof GlutschmiedEntity) && !(e instanceof Brandgebein) && !(e instanceof ArmorStand)
                        && !(e instanceof Player p && (p.isCreative() || p.isSpectator())))) {
            if (victim.distanceToSqr(x, y, z) > (RADIUS + 0.6) * (RADIUS + 0.6)) {
                continue;
            }
            if (victim.hurtServer(level, this.damageSources().thrown(this, owner), this.damage)) {
                victim.igniteForSeconds(4.0f);
            }
        }
    }
}
