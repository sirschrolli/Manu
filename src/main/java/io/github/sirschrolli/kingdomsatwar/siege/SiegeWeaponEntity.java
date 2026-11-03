package io.github.sirschrolli.kingdomsatwar.siege;

import io.github.sirschrolli.kingdomsatwar.army.CommandBatonItem;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.entity.LinearInterpolationHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * Eine Belagerungswaffe (Katapult, Tribok, Balliste, Rammbock). Sie fährt, zielt und schießt auf Befehl dessen, der sie
 * bedient – ganz auf dem Server (kein Fahrzeug, das der Spieler selbst lenkt):
 *
 * <ul>
 *   <li><b>W/S</b> fahren vor und zurück, die Waffe dreht sich langsam in die Blickrichtung des Schützen.</li>
 *   <li><b>Zielen:</b> Wurfwaffen werfen dorthin, wo man hinschaut (die Wurfweite stellt sich darauf ein, eine Leuchtspur
 *   zeigt die Flugbahn und den Einschlag); die Balliste schießt in Blickrichtung.</li>
 *   <li><b>Leertaste</b> feuert (der Rammbock stößt zu), <b>Schleichen</b> steigt ab.</li>
 *   <li>Rechtsklick mit Munition lädt nach, mit Holzbrettern wird repariert; schleichend mit leerer Hand zerlegt man sie
 *   wieder in ihren Bausatz.</li>
 * </ul>
 *
 * Arm, Schlinge, Bogen und Widder bewegen sich nach {@link SiegePoses}: Erst holt die Waffe aus, dann verlässt das
 * Geschoss Löffel, Schlinge oder Bahn genau in dem Moment, in dem das Modell es zeigt.
 */
public class SiegeWeaponEntity extends Entity {
    private static final EntityDataAccessor<Float> DATA_HEALTH = SynchedEntityData.defineId(SiegeWeaponEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_AMMO = SynchedEntityData.defineId(SiegeWeaponEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> DATA_FIRED_AT = SynchedEntityData.defineId(SiegeWeaponEntity.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Float> DATA_AIM = SynchedEntityData.defineId(SiegeWeaponEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_HURT = SynchedEntityData.defineId(SiegeWeaponEntity.class, EntityDataSerializers.INT);

    /** So weit reicht der Blick beim Zielen (Blöcke). */
    private static final double AIM_RANGE = 160.0;
    /** Nach so vielen Ticks ohne weiteren Stoß „verheilen“ angeschlagene Tore wieder. */
    private static final int RAM_MEMORY = 600;

    private final SiegeType siege;
    private @Nullable UUID owner;
    private boolean lastJump;
    private int overlayTimer;
    /** Der Schuss ist ausgelöst und fliegt in diesem Spieltick los (−1: nichts unterwegs). */
    private long releaseAt = -1L;
    private @Nullable LivingEntity releaseShooter;
    /** Rammbock: wie weit die Blöcke vor dem Widder schon angeschlagen sind (0–10). */
    private final Map<BlockPos, Float> ramDamage = new HashMap<>();
    private long lastBlow;
    /** Auf dem Bildschirm: wie weit sich die Räder gedreht haben. */
    public float wheelRot;
    public float wheelRotO;
    /** Befehle für die Bedienmannschaft (siehe {@link SiegeCrew}). */
    final SiegeCrew.Orders orders = new SiegeCrew.Orders();

    public SiegeWeaponEntity(EntityType<? extends SiegeWeaponEntity> type, Level level, SiegeType siege) {
        super(type, level);
        this.siege = siege;
    }

    public SiegeType siege() {
        return this.siege;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_HEALTH, 1.0f);
        builder.define(DATA_AMMO, 0);
        builder.define(DATA_FIRED_AT, -100_000L);
        builder.define(DATA_AIM, 0.0f);
        builder.define(DATA_HURT, 0);
    }

    // =====================================================================================
    // Werte
    // =====================================================================================

    public float health() {
        return this.entityData.get(DATA_HEALTH) * this.siege.health();
    }

    /** Lebensenergie 0–1. */
    public float healthFraction() {
        return this.entityData.get(DATA_HEALTH);
    }

    public void setHealthFraction(float f) {
        this.entityData.set(DATA_HEALTH, Mth.clamp(f, 0.0f, 1.0f));
    }

    public int ammo() {
        return this.entityData.get(DATA_AMMO);
    }

    public void setAmmo(int ammo) {
        this.entityData.set(DATA_AMMO, Mth.clamp(ammo, 0, this.capacity()));
    }

    /** So viel Munition fasst der Vorrat. */
    public int capacity() {
        return switch (this.siege) {
            case CATAPULT, TREBUCHET -> 16;
            case BALLISTA -> 24;
            case RAM -> 0;
        };
    }

    /** So viel Munition kostet ein Schuss (der Tribok wirft einen Brocken aus zwei Steinkugeln). */
    public int ammoPerShot() {
        return this.siege == SiegeType.TREBUCHET ? 2 : this.siege == SiegeType.RAM ? 0 : 1;
    }

    /** Wurfgeschwindigkeit (Wurfwaffen) bzw. Neigung in Grad (Balliste, positiv = nach oben). */
    public float aim() {
        return this.entityData.get(DATA_AIM);
    }

    /** Ticks seit dem letzten Schuss (für Arm, Sehne, Widder); nie geschossen: sehr groß. */
    public float sinceFired(float partialTicks) {
        return this.level().getGameTime() - this.entityData.get(DATA_FIRED_AT) + partialTicks;
    }

    /** Wie weit nachgeladen ist (0 gerade geschossen, 1 bereit). */
    public float reloadProgress(float partialTicks) {
        return Mth.clamp(this.sinceFired(partialTicks) / this.siege.reload(), 0.0f, 1.0f);
    }

    public boolean ready() {
        return this.sinceFired(0.0f) >= this.siege.reload();
    }

    /** Liegt ein Geschoss bereit (sichtbar im Löffel, in der Schlinge, auf der Bahn)? */
    public boolean loaded() {
        return this.siege.ammo() != SiegeType.Ammo.NONE && this.ammo() >= this.ammoPerShot() && this.ready();
    }

    /** Ist das Geschoss des letzten Schusses noch in der Waffe (Arm holt gerade aus)? */
    public boolean releasing(float partialTicks) {
        float t = this.sinceFired(partialTicks);
        return t >= 0.0f && t < SiegePoses.releaseTick(this.siege);
    }

    public int hurtTime() {
        return this.entityData.get(DATA_HURT);
    }

    public @Nullable UUID owner() {
        return this.owner;
    }

    public void setOwner(@Nullable UUID owner) {
        this.owner = owner;
    }

    public boolean isOwnedBy(Entity entity) {
        return this.owner != null && this.owner.equals(entity.getUUID());
    }

    // =====================================================================================
    // Ablauf
    // =====================================================================================

    @Override
    protected InterpolationHandler createInterpolationHandler() {
        return LinearInterpolationHandler.create(this, 3);
    }

    @Override
    public float maxUpStep() {
        return 1.0f;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level() instanceof ServerLevel level) {
            int hurt = this.entityData.get(DATA_HURT);
            if (hurt > 0) {
                this.entityData.set(DATA_HURT, hurt - 1);
            }
            this.serverTick(level);
        } else {
            this.clientTick();
        }
    }

    private void clientTick() {
        this.wheelRotO = this.wheelRot;
        Vec3 moved = this.position().subtract(this.xo, this.yo, this.zo);
        Vec3 fwd = Vec3.directionFromRotation(0.0f, this.getYRot());
        this.wheelRot += (float) (moved.x * fwd.x + moved.z * fwd.z) / 0.375f;
        if (this.healthFraction() < 0.35f && this.random.nextInt(4) == 0) {
            this.level().addParticle(ParticleTypes.SMOKE, this.getX() + (this.random.nextDouble() - 0.5) * this.getBbWidth(),
                    this.getY() + this.getBbHeight() * 0.5, this.getZ() + (this.random.nextDouble() - 0.5) * this.getBbWidth(), 0, 0.03, 0);
        }
    }

    private void serverTick(ServerLevel level) {
        double drive = 0.0;
        Entity rider = this.getFirstPassenger();
        if (rider instanceof ServerPlayer player) {
            Input input = player.getLastClientInput();
            drive = input.forward() ? 1.0 : input.backward() ? -0.6 : 0.0;
            this.turnToward(player.getYRot(), this.siege.turnRate() * (drive != 0.0 ? 0.6f : 1.0f));
            this.aimFor(level, player);
            if (input.jump() && !this.lastJump) {
                this.fire(level, player);
            }
            this.lastJump = input.jump();
            if (this.siege.ammo() != SiegeType.Ammo.NONE && this.tickCount % 3 == 0) {
                SiegeAim.preview(level, this, player);
            }
            if (--this.overlayTimer <= 0) {
                this.overlayTimer = 10;
                player.sendOverlayMessage(this.status());
            }
        } else if (rider instanceof SoldierEntity soldier) {
            drive = SiegeCrew.tick(level, this, soldier);
            this.lastJump = false;
            if (soldier.getSoldierType() == io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType.ENGINEER) {
                this.engineerWork(level, soldier);
            }
        } else {
            this.lastJump = false;
        }
        SiegeCrew.tickAssignment(level, this);
        if (this.releaseAt >= 0L && level.getGameTime() >= this.releaseAt) {
            this.release(level);
        }
        this.reloadSounds(level);
        this.physics(level, drive);
        if (this.isOnFire() && this.tickCount % 20 == 0) {
            this.damage(level, 2.0f, level.damageSources().onFire());
        }
        if (!this.ramDamage.isEmpty() && level.getGameTime() - this.lastBlow > RAM_MEMORY) {
            this.forgetRamDamage(level);
        }
    }

    /** Fahren: auf ebenem Boden zieht die Waffe an, ohne Antrieb bremst sie; Stufen von einem Block nimmt sie. */
    private void physics(ServerLevel level, double drive) {
        Vec3 v = this.getDeltaMovement();
        Vec3 fwd = Vec3.directionFromRotation(0.0f, this.getYRot());
        double along = v.x * fwd.x + v.z * fwd.z;
        double want = drive * this.siege.speed() * (0.5 + 0.5 * this.healthFraction());
        along += (want - along) * (this.onGround() ? 0.25 : 0.05);
        if (along > 0.0 && this.headBlocked(level, along)) {
            along = 0.0;   // der Widderkopf liegt am Tor an
        }
        double vy = this.onGround() ? Math.max(v.y, 0.0) - 0.08 : v.y - 0.08;
        this.setDeltaMovement(fwd.x * along, vy, fwd.z * along);
        this.move(MoverType.SELF, this.getDeltaMovement());
        Vec3 after = this.getDeltaMovement();
        double friction = this.onGround() ? (drive == 0.0 ? 0.5 : 0.92) : 0.98;
        this.setDeltaMovement(after.x * friction, after.y * 0.98, after.z * friction);
        if (this.onGround() && Math.abs(along) > 0.01 && this.tickCount % 9 == 0) {
            level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.WOOD_STEP, SoundSource.NEUTRAL, 0.7f, 0.6f);
        }
    }

    /** Rammbock: stößt der Widderkopf (er ragt vorn über das Gestell hinaus) beim Vorrücken gegen einen Block? */
    private boolean headBlocked(ServerLevel level, double along) {
        if (this.siege != SiegeType.RAM) {
            return false;
        }
        Vec3 fwd = Vec3.directionFromRotation(0.0f, this.getYRot());
        for (double h : new double[] {0.7, 1.15, 1.6}) {
            Vec3 tip = this.position().add(fwd.scale(2.4 + along)).add(0.0, h, 0.0);
            BlockPos pos = BlockPos.containing(tip);
            BlockState state = level.getBlockState(pos);
            VoxelShape shape = state.getCollisionShape(level, pos);
            if (!shape.isEmpty() && shape.bounds().move(pos).inflate(0.05).contains(tip)) {
                return true;
            }
        }
        return false;
    }

    public void turnToward(float yaw, float rate) {
        float before = this.getYRot();
        float after = Mth.approachDegrees(before, yaw, rate);
        if (after != before) {
            this.setYRot(after);
            this.setYHeadRot(after);
        }
    }

    /** Wie weit die Waffe noch von der Richtung {@code yaw} abweicht (Grad). */
    public float yawError(float yaw) {
        return Math.abs(Mth.wrapDegrees(yaw - this.getYRot()));
    }

    // =====================================================================================
    // Zielen und Schießen
    // =====================================================================================

    /** Wo das Geschoss die Waffe verlässt (Löffel, Schlinge, Bahn) bzw. wo der Widder trifft – in der Welt. */
    public Vec3 launchPoint() {
        Vec3 fwd = Vec3.directionFromRotation(0.0f, this.getYRot());
        return switch (this.siege) {
            // Stein im Löffel bei senkrechtem Arm
            case CATAPULT -> this.position().add(fwd.scale(-0.09)).add(0.0, 2.06, 0.0);
            // Brocken in der Schlinge im Moment des Abwurfs
            case TREBUCHET -> {
                Vec3 stone = SiegePoses.trebuchetStone(SiegePoses.TREBUCHET_RELEASE, this.siege.reload());
                yield this.position().add(fwd.scale(stone.z)).add(0.0, stone.y, 0.0);
            }
            // Spitze des Bolzens auf der Bahn, der Bogen ist um die Zielhöhe geneigt
            case BALLISTA -> {
                float pitch = this.aim() * Mth.DEG_TO_RAD;
                Vec3 along = fwd.scale(Mth.cos(pitch)).add(0.0, Mth.sin(pitch), 0.0);
                Vec3 up = fwd.scale(-Mth.sin(pitch)).add(0.0, Mth.cos(pitch), 0.0);
                yield this.position().add(0.0, 0.875, 0.0).add(along.scale(1.0)).add(up.scale(0.125));
            }
            // Widderkopf im Stoß
            case RAM -> this.position().add(fwd.scale(2.55)).add(0.0, 1.63, 0.0);
        };
    }

    /** Wurfwaffen: Wurfweite auf das anvisierte Ziel; Balliste: Neigung nach dem Blick. */
    private void aimFor(ServerLevel level, Player player) {
        if (this.siege == SiegeType.BALLISTA) {
            this.setAim(Mth.clamp(-player.getXRot(), -16.0f, 20.0f));
            return;
        }
        if (!this.siege.lobs()) {
            return;
        }
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getLookAngle().scale(AIM_RANGE));
        BlockHitResult hit = level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        Vec3 target = hit.getType() == HitResult.Type.MISS ? end : hit.getLocation();
        this.setAim((float) SiegeAim.solve(this.siege, this.launchPoint(), target));
    }

    /** Zielt auf einen Punkt (Bedienmannschaft): Wurfweite bzw. Neigung. */
    public void aimAt(Vec3 target) {
        if (this.siege == SiegeType.BALLISTA) {
            Vec3 d = target.subtract(this.launchPoint());
            this.setAim(Mth.clamp((float) (Mth.atan2(d.y, d.horizontalDistance()) * Mth.RAD_TO_DEG), -16.0f, 20.0f));
        } else if (this.siege.lobs()) {
            this.setAim((float) SiegeAim.solve(this.siege, this.launchPoint(), target));
        }
    }

    private void setAim(float aim) {
        if (Math.abs(aim - this.aim()) > 0.005f) {
            this.entityData.set(DATA_AIM, aim);
        }
    }

    /** Abschussgeschwindigkeit als Vektor. */
    public Vec3 launchVelocity() {
        Vec3 fwd = Vec3.directionFromRotation(0.0f, this.getYRot());
        if (this.siege == SiegeType.BALLISTA) {
            float pitch = this.aim() * Mth.DEG_TO_RAD;
            return fwd.scale(Mth.cos(pitch) * this.siege.maxVelocity()).add(0.0, Mth.sin(pitch) * this.siege.maxVelocity(), 0.0);
        }
        double v = Mth.clamp(this.aim(), this.siege.minVelocity(), this.siege.maxVelocity());
        double angle = SiegeAim.launchAngle(this.siege);
        return fwd.scale(Math.cos(angle) * v).add(0.0, Math.sin(angle) * v, 0.0);
    }

    /**
     * Löst den Schuss aus (Rammbock: holt zum Stoß aus), wenn die Waffe bereit und geladen ist; holt sich sonst Munition
     * aus dem Gepäck des Schützen. Das Geschoss fliegt erst los, wenn der Arm (die Sehne, der Widder) so weit ist.
     */
    public boolean fire(ServerLevel level, @Nullable LivingEntity shooter) {
        if (!this.ready() || this.releaseAt >= 0L) {
            return false;
        }
        if (this.siege != SiegeType.RAM) {
            if (this.ammo() < this.ammoPerShot() && shooter instanceof Player player) {
                this.takeAmmo(player);
            }
            if (this.ammo() < this.ammoPerShot()) {
                level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.DISPENSER_FAIL, SoundSource.NEUTRAL, 0.8f, 0.6f);
                if (shooter instanceof Player player) {
                    player.sendOverlayMessage(Component.translatable("siege.kingdomsatwar.no_ammo", this.ammoName()).withStyle(ChatFormatting.RED));
                }
                return false;
            }
            this.setAmmo(this.ammo() - this.ammoPerShot());
        }
        long now = level.getGameTime();
        this.entityData.set(DATA_FIRED_AT, now);
        this.releaseAt = now + SiegePoses.releaseTick(this.siege);
        this.releaseShooter = shooter;
        Vec3 at = this.position().add(0.0, 1.0, 0.0);
        switch (this.siege) {
            case CATAPULT -> {
                SiegeImpacts.sound(level, at, SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR, 1.2f, 0.55f);
                SiegeImpacts.sound(level, at, SoundEvents.CROSSBOW_SHOOT, 1.0f, 0.35f);
            }
            case TREBUCHET -> {
                SiegeImpacts.sound(level, at, SoundEvents.CHAIN_BREAK, 1.4f, 0.5f);
                SiegeImpacts.sound(level, at, SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR, 1.6f, 0.4f);
            }
            case BALLISTA -> SiegeImpacts.sound(level, at, SoundEvents.CROSSBOW_QUICK_CHARGE_3.value(), 1.0f, 0.5f);
            case RAM -> SiegeImpacts.sound(level, at, SoundEvents.CHAIN_STEP, 1.2f, 0.6f);
        }
        return true;
    }

    /** Der Arm ist oben, die Sehne vorn, der Widder vorn: jetzt fliegt das Geschoss bzw. trifft der Stoß. */
    private void release(ServerLevel level) {
        LivingEntity shooter = this.releaseShooter;
        this.releaseAt = -1L;
        this.releaseShooter = null;
        if (shooter != null && (shooter.isRemoved() || shooter.getVehicle() != this)) {
            shooter = null;
        }
        if (this.siege == SiegeType.RAM) {
            SiegeImpacts.ramSwing(level, this, shooter);
        } else {
            SiegeProjectile.launch(level, this, shooter);
        }
        this.orders.shotFired();
    }

    /**
     * Ein Belagerungsknecht an der Waffe: Er lädt um die Hälfte schneller nach (die Winde läuft schneller – die Zeit seit
     * dem Schuss rückt jeden zweiten Tick um einen Tick weiter; den Abschuss selbst beschleunigt er nicht) und bessert die
     * Waffe nebenbei aus.
     */
    private void engineerWork(ServerLevel level, SoldierEntity engineer) {
        long now = level.getGameTime();
        float since = this.sinceFired(0.0f);
        if (this.siege != SiegeType.RAM && now % 2 == 0 && since > SiegePoses.releaseTick(this.siege) + 4 && since < this.siege.reload() - 1) {
            this.entityData.set(DATA_FIRED_AT, this.entityData.get(DATA_FIRED_AT) - 1L);
        }
        if (this.healthFraction() < 1.0f && (now + this.getId()) % 40 == 0) {
            this.setHealthFraction(this.healthFraction() + 0.015f);
            engineer.swing(net.minecraft.world.InteractionHand.MAIN_HAND, net.minecraft.world.item.component.SwingAnimation.DEFAULT);
            level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.WOOD_HIT, SoundSource.NEUTRAL, 0.7f, 1.0f);
        }
    }

    /** Knarren der Winde beim Nachladen, ein Klacken, wenn die Waffe wieder bereit ist. */
    private void reloadSounds(ServerLevel level) {
        if (this.siege == SiegeType.RAM) {
            return;
        }
        float t = this.sinceFired(0.0f);
        int reload = this.siege.reload();
        if (t > 12 && t < reload && (int) t % 12 == 0) {
            level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.CROSSBOW_LOADING_MIDDLE.value(), SoundSource.NEUTRAL, 0.5f,
                    0.45f + this.random.nextFloat() * 0.1f);
        } else if ((int) t == reload) {
            level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.CROSSBOW_LOADING_END.value(), SoundSource.NEUTRAL, 0.8f, 0.5f);
        }
    }

    /** Munition aus dem Inventar des Schützen in den Vorrat. */
    private void takeAmmo(Player player) {
        if (player.hasInfiniteMaterials()) {
            this.setAmmo(this.capacity());
            return;
        }
        var inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize() && this.ammo() < this.capacity(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (this.isAmmo(stack)) {
                int take = Math.min(stack.getCount(), this.capacity() - this.ammo());
                stack.shrink(take);
                this.setAmmo(this.ammo() + take);
            }
        }
    }

    public boolean isAmmo(ItemStack stack) {
        return switch (this.siege.ammo()) {
            case STONE -> stack.is(ModItems.SIEGE_STONE.get());
            case BOLT -> stack.is(ModItems.BALLISTA_BOLT.get());
            case NONE -> false;
        };
    }

    public Component ammoName() {
        return Component.translatable(this.siege.ammo() == SiegeType.Ammo.BOLT ? "item.kingdomsatwar.ballista_bolt" : "item.kingdomsatwar.siege_stone");
    }

    /** Zeile über der Schnellleiste für den Schützen: Vorrat, Bereitschaft, Wurfweite. */
    private Component status() {
        Component name = this.getDisplayName().copy().withStyle(ChatFormatting.GOLD);
        Component state = this.ammo() < this.ammoPerShot() && this.siege != SiegeType.RAM
                ? Component.translatable("siege.kingdomsatwar.empty").withStyle(ChatFormatting.RED)
                : this.ready() ? Component.translatable("siege.kingdomsatwar.ready").withStyle(ChatFormatting.GREEN)
                : Component.translatable("siege.kingdomsatwar.reloading", Math.round(this.reloadProgress(0.0f) * 100)).withStyle(ChatFormatting.GRAY);
        Component health = Component.translatable("siege.kingdomsatwar.health", Math.round(this.healthFraction() * 100))
                .withStyle(this.healthFraction() < 0.35f ? ChatFormatting.RED : ChatFormatting.GRAY);
        return switch (this.siege) {
            case RAM -> Component.translatable("siege.kingdomsatwar.status.ram", name, state, health);
            case BALLISTA -> Component.translatable("siege.kingdomsatwar.status.ballista", name, this.ammo(), this.ammoName(), state,
                    Math.round(this.aim()), health);
            default -> Component.translatable("siege.kingdomsatwar.status", name, this.ammo(), this.ammoName(), state,
                    (int) Math.round(SiegeAim.range(this.siege, this.launchVelocity())), health);
        };
    }

    // =====================================================================================
    // Rammbock
    // =====================================================================================

    /** Ein Stoß gegen den Block: Risse wachsen; @return ob er jetzt bricht. */
    boolean ramBlow(ServerLevel level, BlockPos pos, float amount) {
        this.lastBlow = level.getGameTime();
        BlockPos key = pos.immutable();
        float damage = this.ramDamage.getOrDefault(key, 0.0f) + amount;
        if (damage >= 9.99f) {
            this.ramDamage.remove(key);
            level.destroyBlockProgress(this.breakerId(key), key, -1);
            return true;
        }
        this.ramDamage.put(key, damage);
        level.destroyBlockProgress(this.breakerId(key), key, Mth.clamp((int) damage, 0, 9));
        return false;
    }

    private void forgetRamDamage(ServerLevel level) {
        for (Iterator<BlockPos> it = this.ramDamage.keySet().iterator(); it.hasNext(); ) {
            BlockPos pos = it.next();
            level.destroyBlockProgress(this.breakerId(pos), pos, -1);
            it.remove();
        }
    }

    /** Eigene Kennung je angeschlagenem Block (jeder Kennung zeigt das Spiel nur einen Riss). */
    private int breakerId(BlockPos pos) {
        return 0x40000000 | (this.getId() * 31 + (int) (pos.asLong() ^ pos.asLong() >>> 32)) & 0x3FFFFFFF;
    }

    // =====================================================================================
    // Bedienen
    // =====================================================================================

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return this.getPassengers().isEmpty() && (passenger instanceof Player || passenger instanceof SoldierEntity);
    }

    @Override
    public boolean shouldRiderSit() {
        return this.siege.seated();
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions, float scale) {
        Vec3 seat = this.siege.seat();
        // Füße des Schützen: im Stehen auf dem Boden, im Sitzen 0,6 unter der Sitzfläche (wie bei jedem Sitzenden)
        double y = seat.y + passenger.getVehicleAttachmentPoint(this).y - (this.siege.seated() ? 0.6 : 0.0);
        return new Vec3(seat.x, y, seat.z).yRot(-this.getYRot() * Mth.DEG_TO_RAD);
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        Vec3 fwd = Vec3.directionFromRotation(0.0f, this.getYRot());
        Vec3 side = new Vec3(-fwd.z, 0.0, fwd.x);
        double out = this.getBbWidth() * 0.5 + passenger.getBbWidth() * 0.5 + 0.3;
        for (Vec3 dir : new Vec3[] {fwd.scale(-1.0), side.scale(-1.0), side, fwd}) {
            Vec3 at = this.position().add(dir.scale(out));
            for (double dy = 0.0; dy <= 1.0; dy += 0.5) {
                Vec3 spot = at.add(0.0, dy, 0.0);
                if (this.level().noCollision(passenger, passenger.getType().getSpawnAABB(spot.x, spot.y, spot.z))
                        && !this.level().getBlockState(BlockPos.containing(spot).below()).getCollisionShape(this.level(),
                        BlockPos.containing(spot).below()).isEmpty()) {
                    return spot;
                }
            }
        }
        return super.getDismountLocationForPassenger(passenger);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand, Vec3 location) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof CommandBatonItem || stack.getItem() instanceof SiegeKitItem) {
            return InteractionResult.PASS;   // Befehle gibt der Kommandostab selbst
        }
        if (this.isAmmo(stack)) {
            if (this.ammo() >= this.capacity()) {
                if (!this.level().isClientSide()) {
                    player.sendOverlayMessage(Component.translatable("siege.kingdomsatwar.full", this.ammoName()).withStyle(ChatFormatting.GRAY));
                }
                return InteractionResult.FAIL;
            }
            if (this.level() instanceof ServerLevel level) {
                int take = Math.min(stack.getCount(), this.capacity() - this.ammo());
                this.setAmmo(this.ammo() + take);
                stack.consume(take, player);
                level.playSound(null, this.getX(), this.getY(), this.getZ(), this.siege.ammo() == SiegeType.Ammo.BOLT ? SoundEvents.CROSSBOW_LOADING_END.value()
                        : SoundEvents.STONE_PLACE, SoundSource.NEUTRAL, 0.9f, 0.8f);
                player.sendOverlayMessage(Component.translatable("siege.kingdomsatwar.loaded", this.ammo(), this.capacity(), this.ammoName())
                        .withStyle(ChatFormatting.GOLD));
            }
            return InteractionResult.SUCCESS;
        }
        if (stack.is(ItemTags.PLANKS) && this.healthFraction() < 1.0f) {
            if (this.level() instanceof ServerLevel level) {
                this.setHealthFraction(this.healthFraction() + 0.1f);
                stack.consume(1, player);
                level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.WOOD_PLACE, SoundSource.NEUTRAL, 1.0f, 0.9f);
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.OAK_PLANKS.defaultBlockState()), this.getX(),
                        this.getY(0.5), this.getZ(), 12, this.getBbWidth() * 0.3, 0.4, this.getBbWidth() * 0.3, 0.05);
                player.sendOverlayMessage(Component.translatable("siege.kingdomsatwar.repaired", Math.round(this.healthFraction() * 100))
                        .withStyle(ChatFormatting.GREEN));
            }
            return InteractionResult.SUCCESS;
        }
        if (player.isSecondaryUseActive() && stack.isEmpty()) {
            if (this.level() instanceof ServerLevel level) {
                if (this.owner != null && !this.isOwnedBy(player) && !player.hasInfiniteMaterials()) {
                    player.sendOverlayMessage(Component.translatable("siege.kingdomsatwar.not_yours").withStyle(ChatFormatting.RED));
                    return InteractionResult.FAIL;
                }
                this.dismantle(level, player);
            }
            return InteractionResult.SUCCESS;
        }
        if (!player.isSecondaryUseActive() && !this.isVehicle()) {
            if (!this.level().isClientSide() && player.startRiding(this)) {
                if (this.owner == null) {
                    this.owner = player.getUUID();
                }
                player.sendOverlayMessage(Component.translatable("siege.kingdomsatwar.controls." + this.siege.id()).withStyle(ChatFormatting.GRAY));
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    /** Zerlegen: der Bausatz (mit dem Zustand der Waffe) und die übrige Munition. */
    private void dismantle(ServerLevel level, Player player) {
        this.ejectPassengers();
        this.forgetRamDamage(level);
        ItemStack kit = SiegeKitItem.kit(this.siege, this.healthFraction());
        if (!player.getInventory().add(kit)) {
            this.spawnAtLocation(level, kit);
        }
        int ammo = this.ammo();
        while (ammo > 0) {
            ItemStack rest = this.siege.ammo() == SiegeType.Ammo.BOLT ? new ItemStack(ModItems.BALLISTA_BOLT.get(), Math.min(16, ammo))
                    : new ItemStack(ModItems.SIEGE_STONE.get(), Math.min(16, ammo));
            ammo -= rest.getCount();
            if (!player.getInventory().add(rest)) {
                this.spawnAtLocation(level, rest);
            }
        }
        level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.WOOD_BREAK, SoundSource.NEUTRAL, 1.0f, 0.8f);
        level.sendParticles(ParticleTypes.POOF, this.getX(), this.getY(0.4), this.getZ(), 16, this.getBbWidth() * 0.4, 0.4, this.getBbWidth() * 0.4, 0.02);
        this.discard();
    }

    // =====================================================================================
    // Schaden
    // =====================================================================================

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    @Override
    public boolean canBeCollidedWith(@Nullable Entity other) {
        return true;
    }

    @Override
    public boolean canCollideWith(Entity entity) {
        return (entity.canBeCollidedWith(this) || entity.isPushable()) && !this.isPassengerOfSameVehicle(entity);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean hurtClient(DamageSource source) {
        return true;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (this.isRemoved() || this.isInvulnerableToBase(source)) {
            return false;
        }
        Entity attacker = source.getEntity();
        if (attacker != null && this.hasPassenger(attacker)) {
            return false;
        }
        if (attacker instanceof Player player && player.getAbilities().instabuild && source.getDirectEntity() == player) {
            this.forgetRamDamage(level);
            this.discard();
            return true;
        }
        float damage = amount;
        if (source.getDirectEntity() instanceof AbstractArrow) {
            damage *= 0.25f;   // Pfeile bleiben im Holz stecken
        } else if (source.is(DamageTypeTags.IS_EXPLOSION)) {
            damage *= 1.5f;
        } else if (attacker instanceof LivingEntity living && source.getDirectEntity() == attacker
                && living.getMainHandItem().is(ItemTags.AXES)) {
            damage *= 2.0f;    // mit der Axt zerhackt man sie schneller
        }
        this.damage(level, damage, source);
        return true;
    }

    /** Schaden in Lebenspunkten; bei 0 bricht die Waffe auseinander. */
    public void damage(ServerLevel level, float damage, @Nullable DamageSource source) {
        if (damage <= 0.0f || this.isRemoved()) {
            return;
        }
        this.entityData.set(DATA_HURT, 10);
        this.setHealthFraction(this.healthFraction() - damage / this.siege.health());
        level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR, SoundSource.NEUTRAL, 0.6f,
                1.2f + this.random.nextFloat() * 0.3f);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.OAK_PLANKS.defaultBlockState()), this.getX(), this.getY(0.5),
                this.getZ(), 8, this.getBbWidth() * 0.3, this.getBbHeight() * 0.3, this.getBbWidth() * 0.3, 0.1);
        if (this.healthFraction() <= 0.0f) {
            this.breakApart(level);
        }
    }

    private void breakApart(ServerLevel level) {
        this.ejectPassengers();
        this.forgetRamDamage(level);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.OAK_PLANKS.defaultBlockState()), this.getX(), this.getY(0.5),
                this.getZ(), 80, this.getBbWidth() * 0.4, this.getBbHeight() * 0.4, this.getBbWidth() * 0.4, 0.15);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY(0.5), this.getZ(), 20, 0.6, 0.6, 0.6, 0.05);
        level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, SoundSource.NEUTRAL, 1.4f, 0.7f);
        if (level.getGameRules().get(GameRules.ENTITY_DROPS)) {
            for (ItemStack stack : SiegeKitItem.salvage(this.siege, this.random)) {
                this.spawnAtLocation(level, stack);
            }
        }
        this.discard();
    }

    @Override
    public @Nullable ItemStack getPickResult() {
        return SiegeKitItem.kit(this.siege, 1.0f);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        double size = Math.max(4.0, this.getBoundingBox().getSize() * 8.0) * getViewScale();
        return distance < size * size * 4.0;
    }

    // =====================================================================================
    // Speichern
    // =====================================================================================

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.putFloat("SiegeHealth", this.healthFraction());
        output.putInt("SiegeAmmo", this.ammo());
        output.putInt("SiegeSinceFired", (int) Math.min(this.siege.reload() + 100, this.sinceFired(0.0f)));
        if (this.owner != null) {
            output.store("SiegeOwner", UUIDUtil.CODEC, this.owner);
        }
        this.orders.save(output);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        this.setHealthFraction(input.getFloatOr("SiegeHealth", 1.0f));
        this.setAmmo(input.getIntOr("SiegeAmmo", 0));
        int since = input.getIntOr("SiegeSinceFired", this.siege.reload() + 100);
        this.entityData.set(DATA_FIRED_AT, this.level().getGameTime() - Math.max(since, SiegePoses.releaseTick(this.siege)));
        this.owner = input.read("SiegeOwner", UUIDUtil.CODEC).orElse(null);
        this.orders.load(input);
    }

    /** Nur für Tests und Vorführungen: die Waffe zeigt die Stellung {@code since} Ticks nach einem Schuss (ohne zu schießen). */
    public void devPose(int since) {
        this.entityData.set(DATA_FIRED_AT, this.level().getGameTime() - since);
    }

    /** Für die Leuchtspur der Flugbahn: Farbe je Waffe. */
    static DustParticleOptions previewDust(SiegeType siege) {
        return new DustParticleOptions(siege == SiegeType.BALLISTA ? 0xFFD27A : 0xFFB05A, 0.9f);
    }
}
