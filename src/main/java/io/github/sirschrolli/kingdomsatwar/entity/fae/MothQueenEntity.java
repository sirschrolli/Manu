package io.github.sirschrolli.kingdomsatwar.entity.fae;

import io.github.sirschrolli.kingdomsatwar.magic.fx.SpellFxEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModParticles;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Noctua, die Mottenkönigin: ein riesiger Mondfalter, der im Mondhain des Feenreichs in einem Kokon schläft, bis jemand
 * ihn mit Feenstaub weckt. Sie umkreist ihre Gegner hoch in der Luft und greift abwechselnd an:
 * <ul>
 *     <li><b>Flügelsturm</b> – mächtige Flügelschläge, die alle vor ihr zurückwerfen;</li>
 *     <li><b>Schuppenwolke</b> – über dem Gegner rieselt glitzernder Schuppenstaub (Schwäche, Langsamkeit, Schaden);</li>
 *     <li><b>Mottenschwarm</b> – ein schriller Ruf, und Mondfalter stürzen sich auf die Gegner;</li>
 *     <li><b>Mondstrahl</b> – leuchtende Kreise am Boden, dann fallen Strahlen aus Mondlicht hinein;</li>
 *     <li><b>Sturzflug</b> – sie stößt herab und rammt ihr Ziel;</li>
 *     <li><b>Schlaflied</b> – ein betörendes Lied: wer sie dabei ansieht, wird schwer und schläfrig;</li>
 *     <li><b>Mondfinsternis</b> – der Mond verfinstert sich, Dunkelheit legt sich über alle; nur leuchtende Kreise warnen,
 *     wo gleich Mondlicht herabfällt;</li>
 *     <li><b>Seidenkokon</b> – ein Faden aus Seide schnellt auf ihr Ziel, und es wird in einen Kokon gesponnen (kaum zu
 *     bewegen, schwach, die Seide schnürt zu).</li>
 * </ul>
 * Verfehlt sie im Sturzflug ihr Ziel und prallt auf den Boden, liegt sie drei Sekunden benommen da – die Gelegenheit für
 * Schwertkämpfer. Unter halber Kraft wird sie rasend: schneller, mehr Falter, mehr Strahlen. Besiegt hinterlässt sie die
 * Feen-Elytren, und im Mondhain spinnt sich ein neuer Kokon.
 */
public class MothQueenEntity extends Monster implements io.github.sirschrolli.kingdomsatwar.entity.boss.sky.SkyWarden {
    public static final float MAX_HEALTH = 360.0f;
    public static final int AWAKEN_TICKS = 80;
    /** Mondfinsternis: Dauer, erster Mondstrahl, Abstand der Strahlen, Vorwarnzeit eines Kreises. */
    public static final int ECLIPSE_TICKS = 92;
    public static final int ECLIPSE_FIRST = 14;
    public static final int ECLIPSE_STEP = 12;
    public static final int ECLIPSE_WARN = 16;
    /** Seidenkokon: Dauer, der Faden schnellt, der Kokon schließt sich, wie lange er hält. */
    public static final int COCOON_TICKS = 48;
    public static final int COCOON_THREAD = 10;
    public static final int COCOON_WRAP = 18;
    public static final int COCOON_LIFE = 70;

    public enum Attack {
        NONE(0), AWAKEN(AWAKEN_TICKS), GUST(44), SCALES(56), SWARM(34), BEAM(64), DIVE(50), LULLABY(70), STUNNED(60), ECLIPSE(ECLIPSE_TICKS),
        COCOON(COCOON_TICKS);

        public final int duration;

        Attack(int duration) {
            this.duration = duration;
        }
    }

    private static final EntityDataAccessor<Integer> DATA_ATTACK = SynchedEntityData.defineId(MothQueenEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_ENRAGED = SynchedEntityData.defineId(MothQueenEntity.class,
            EntityDataSerializers.BOOLEAN);

    private final ServerBossEvent bossBar = new ServerBossEvent(UUID.randomUUID(), Component.translatable("boss.kingdomsatwar.moth"),
            BossEvent.BossBarColor.PINK, BossEvent.BossBarOverlay.NOTCHED_10);
    private Attack attack = Attack.NONE;
    private Attack lastAttack = Attack.NONE;
    private int attackTick;
    private int cooldown = 40;
    private float orbit;
    private @Nullable BlockPos home;
    private final List<Vec3> beams = new ArrayList<>();
    private Vec3 diveDir = Vec3.ZERO;
    private boolean diveHit;
    private boolean lullabyWarned;
    /** Seidenkokon: wen die Fäden treffen. */
    private final List<LivingEntity> cocoonVictims = new ArrayList<>();
    private int idleTicks;
    /** Nur im Client: seit wann der laufende Angriff läuft (für die Animationen). */
    private int clientAttackStart;

    public MothQueenEntity(EntityType<? extends MothQueenEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.xpReward = 300;
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.ARMOR, 6.0)
                .add(Attributes.ATTACK_DAMAGE, 8.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FLYING_SPEED, 0.6)
                .add(Attributes.FOLLOW_RANGE, 48.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.9);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACK, 0);
        builder.define(DATA_ENRAGED, false);
    }

    public Attack currentAttack() {
        int i = this.entityData.get(DATA_ATTACK);
        return i >= 0 && i < Attack.values().length ? Attack.values()[i] : Attack.NONE;
    }

    public boolean isEnraged() {
        return this.entityData.get(DATA_ENRAGED);
    }

    /** Wie lange der laufende Angriff schon dauert (im Client, für die Animationen). */
    public float attackTime(float partialTicks) {
        return this.tickCount - this.clientAttackStart + partialTicks;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (DATA_ATTACK.equals(key)) {
            this.clientAttackStart = this.tickCount;
        }
    }

    public @Nullable BlockPos home() {
        return this.home;
    }

    /** Beginnt das Erwachen (aus dem Kokon im Mondhain). */
    public void awaken(BlockPos home) {
        this.home = home;
        this.begin(Attack.AWAKEN);
    }

    /** Beginnt sofort einen bestimmten Angriff (für Tests und die Vorführung). */
    public void forceAttack(Attack next) {
        this.begin(next);
    }

    private void begin(Attack next) {
        this.attack = next;
        this.attackTick = 0;
        this.entityData.set(DATA_ATTACK, next.ordinal());
        if (next != Attack.NONE && next != Attack.AWAKEN) {
            this.lastAttack = next;
        }
    }

    // ------------------------------------------------------------------ Grundverhalten

    @Override
    public void travel(Vec3 input) {
        this.move(net.minecraft.world.entity.MoverType.SELF, this.getDeltaMovement());
        this.setDeltaMovement(this.getDeltaMovement().scale(0.9));
    }

    @Override
    protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {
    }

    @Override
    public boolean causeFallDamage(double distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    /** Während sie erwacht, ist sie unverwundbar; benommen nimmt sie mehr Schaden. */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (this.attack == Attack.AWAKEN && !source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return false;
        }
        if (source.is(net.minecraft.world.damagesource.DamageTypes.IN_WALL)) {
            return false;
        }
        if (this.attack == Attack.STUNNED) {
            amount *= 1.5f;
        }
        boolean hurt = super.hurtServer(level, source, amount);
        // wer sie angreift (auch Wölfe oder Golems), wird ihr Ziel
        if (hurt && source.getEntity() instanceof LivingEntity attacker && attacker != this
                && !(attacker instanceof Player player && (player.isCreative() || player.isSpectator()))
                && !(this.getTarget() instanceof Player)) {
            this.setTarget(attacker);
        }
        return hurt;
    }

    @Override
    public boolean removeWhenFarAway(double distanceSqr) {
        return false;
    }

    @Override
    public boolean canBeLeashed() {
        return false;
    }

    @Override
    protected boolean canRide(net.minecraft.world.entity.Entity vehicle) {
        return false;
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossBar.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossBar.removePlayer(player);
    }

    @Override
    public void tick() {
        // sie gleitet durch Laub und Äste; nur benommen fällt sie wie ein Körper zu Boden
        this.noPhysics = this.currentAttack() != Attack.STUNNED;
        super.tick();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide()) {
            this.clientEffects();
        }
    }

    private void clientEffects() {
        Level level = this.level();
        int sparks = this.isEnraged() ? 3 : 2;
        for (int i = 0; i < sparks; i++) {
            double a = this.random.nextDouble() * Math.PI * 2.0;
            double r = 1.5 + this.random.nextDouble() * 4.0;
            level.addParticle(ModParticles.FAE_SPARK.get(), this.getX() + Math.cos(a) * r, this.getY(0.4) + (this.random.nextDouble() - 0.5),
                    this.getZ() + Math.sin(a) * r, 0.0, -0.02, 0.0);
        }
        if (this.currentAttack() == Attack.LULLABY && this.random.nextInt(3) == 0) {
            level.addParticle(ParticleTypes.NOTE, this.getRandomX(2.0), this.getY(1.0) + 0.5, this.getRandomZ(2.0),
                    this.random.nextDouble(), 0.0, 0.0);
        }
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        this.bossBar.setProgress(this.getHealth() / this.getMaxHealth());
        if (!this.isEnraged() && this.getHealth() < this.getMaxHealth() * 0.5f) {
            this.entityData.set(DATA_ENRAGED, true);
            level.playSound(null, this.blockPosition(), ModSounds.MOTH_QUEEN_SCREECH.get(), SoundSource.HOSTILE, 4.0f, 0.8f);
            level.sendParticles(ModParticles.FAE_SPARK.get(), this.getX(), this.getY(0.5), this.getZ(), 80, 3.0, 1.5, 3.0, 0.1);
        }
        if (this.attack == Attack.AWAKEN) {
            this.tickAwakening(level);
            return;
        }
        if (this.attack == Attack.STUNNED) {
            this.tickStunned(level);
            return;
        }
        LivingEntity target = this.findTarget(level);
        this.setTarget(target);
        if (target == null) {
            this.idle(level);
            return;
        }
        this.idleTicks = 0;
        this.getLookControl().setLookAt(target, 30.0f, 30.0f);
        this.lookAt(target, 20.0f, 20.0f);
        if (this.attack == Attack.NONE) {
            this.circle(target, 1.0);
            if (--this.cooldown <= 0) {
                this.begin(this.chooseAttack(target));
            }
            return;
        }
        this.attackTick++;
        switch (this.attack) {
            case GUST -> this.tickGust(level, target);
            case SCALES -> this.tickScales(level, target);
            case SWARM -> this.tickSwarm(level, target);
            case BEAM -> this.tickBeam(level, target);
            case DIVE -> this.tickDive(level, target);
            case LULLABY -> this.tickLullaby(level, target);
            case ECLIPSE -> this.tickEclipse(level, target);
            case COCOON -> this.tickCocoon(level, target);
            default -> { }
        }
        if (this.attackTick >= this.attack.duration) {
            this.begin(Attack.NONE);
            this.cooldown = (int) ((this.isEnraged() ? 22 : 34) + this.random.nextInt(20));
        }
    }

    /** Der nächste Spieler in Reichweite – aber sie entfernt sich nie weiter als 48 Blöcke von ihrem Mondhain. */
    private @Nullable LivingEntity findTarget(ServerLevel level) {
        if (this.home != null && this.distanceToSqr(Vec3.atCenterOf(this.home)) > 48.0 * 48.0) {
            return null;
        }
        LivingEntity current = this.getTarget();
        if (current != null && current.isAlive() && current.distanceToSqr(this) < 56.0 * 56.0
                && !(current instanceof Player player && (player.isCreative() || player.isSpectator()))) {
            return current;
        }
        return level.getNearestPlayer(this.getX(), this.getY(), this.getZ(), 40.0,
                e -> e instanceof Player p && !p.isCreative() && !p.isSpectator() && p.isAlive());
    }

    /** Ohne Gegner: über dem Mondhain kreisen; nach einer Weile ohne Kampf heilt sie sich. */
    private void idle(ServerLevel level) {
        if (this.attack != Attack.NONE) {
            this.begin(Attack.NONE);
        }
        BlockPos center = this.home != null ? this.home : this.blockPosition();
        this.orbit += 0.015f;
        Vec3 want = Vec3.atCenterOf(center).add(Math.cos(this.orbit) * 8.0, 9.0, Math.sin(this.orbit) * 8.0);
        this.steer(want, 0.05, 0.35);
        if (++this.idleTicks > 600 && this.tickCount % 20 == 0) {
            this.heal(8.0f);
        }
    }

    /** Kreist in sechs bis neun Blöcken Höhe um das Ziel. */
    private void circle(LivingEntity target, double speedScale) {
        this.orbit += (this.isEnraged() ? 0.028f : 0.02f) * (float) speedScale;
        double radius = 9.0 + Math.sin(this.tickCount * 0.02) * 2.0;
        double height = 6.5 + Math.sin(this.tickCount * 0.05) * 1.5;
        Vec3 want = target.position().add(Math.cos(this.orbit) * radius, height, Math.sin(this.orbit) * radius);
        this.steer(want, 0.06, this.isEnraged() ? 0.75 : 0.6);
    }

    private void steer(Vec3 want, double accel, double max) {
        Vec3 to = want.subtract(this.position());
        Vec3 v = this.getDeltaMovement().add(to.normalize().scale(Math.min(to.length(), 4.0) * accel * 0.25));
        if (v.length() > max) {
            v = v.normalize().scale(max);
        }
        this.setDeltaMovement(v);
    }

    private Attack chooseAttack(LivingEntity target) {
        List<Attack> options = new ArrayList<>(List.of(Attack.GUST, Attack.SCALES, Attack.SWARM, Attack.BEAM, Attack.DIVE, Attack.LULLABY,
                Attack.ECLIPSE, Attack.COCOON));
        options.remove(this.lastAttack);
        if (this.distanceTo(target) > 18.0) {
            options.remove(Attack.GUST);
        }
        return options.get(this.random.nextInt(options.size()));
    }

    // ------------------------------------------------------------------ Erwachen

    private void tickAwakening(ServerLevel level) {
        int t = ++this.attackTick;
        // langsam aus dem Kokon aufsteigen, die Flügel entfalten
        this.setDeltaMovement(0.0, t < 50 ? 0.06 : 0.0, 0.0);
        if (t % 4 == 0) {
            level.sendParticles(ModParticles.FAE_SPARK.get(), this.getX(), this.getY(0.5), this.getZ(), 14, 2.5, 1.0, 2.5, 0.05);
        }
        if (t == 2) {
            level.playSound(null, this.blockPosition(), ModSounds.MOTH_QUEEN_AWAKEN.get(), SoundSource.HOSTILE, 3.0f, 1.0f);
        }
        if (t == AWAKEN_TICKS - 20) {
            level.playSound(null, this.blockPosition(), ModSounds.MOTH_QUEEN_SCREECH.get(), SoundSource.HOSTILE, 4.0f, 1.0f);
            Component title = Component.translatable("boss.kingdomsatwar.moth").withColor(0xFFB8E8);
            Component subtitle = Component.translatable("boss.kingdomsatwar.moth.title").withStyle(ChatFormatting.GRAY);
            for (ServerPlayer player : level.players()) {
                if (player.distanceToSqr(this) < 80.0 * 80.0) {
                    player.connection.send(new ClientboundSetTitlesAnimationPacket(20, 70, 30));
                    player.connection.send(new ClientboundSetTitleTextPacket(title));
                    player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
                }
            }
        }
        if (t >= AWAKEN_TICKS) {
            this.begin(Attack.NONE);
            this.cooldown = 30;
        }
    }

    // ------------------------------------------------------------------ Angriffe

    /**
     * Wen ihre Angriffe treffen: alle Lebewesen außer ihr selbst, den Wesen des Feenreichs (Feen, Pilzlinge, ihre Falter),
     * Rüstungsständern und Spielern im Kreativ- oder Zuschauermodus – also Spieler, ihre Soldaten, Tiere und Begleiter.
     */
    private boolean canStrike(LivingEntity entity) {
        return entity != this && entity.isAlive() && !entity.is(io.github.sirschrolli.kingdomsatwar.registry.ModTags.EntityTypes.FAE_CREATURES)
                && !(entity instanceof net.minecraft.world.entity.decoration.ArmorStand)
                && !(entity instanceof Player player && (player.isCreative() || player.isSpectator()));
    }

    private List<LivingEntity> victims(ServerLevel level, AABB area) {
        return level.getEntitiesOfClass(LivingEntity.class, area, this::canStrike);
    }

    private static void push(LivingEntity entity, Vec3 push) {
        entity.push(push.x, push.y, push.z);
        entity.needsSync = true;
    }

    /** Flügelsturm: anhalten, dreimal gewaltig schlagen – alles vor ihr wird zurückgeworfen. */
    private void tickGust(ServerLevel level, LivingEntity target) {
        this.setDeltaMovement(this.getDeltaMovement().scale(0.6));
        int t = this.attackTick;
        if (t == 12 || t == 24 || t == 36) {
            level.playSound(null, this.blockPosition(), ModSounds.MOTH_QUEEN_GUST.get(), SoundSource.HOSTILE, 3.0f, 0.9f + t * 0.005f);
            Vec3 forward = target.position().subtract(this.position()).normalize();
            for (LivingEntity victim : this.victims(level, this.getBoundingBox().inflate(16.0))) {
                Vec3 to = victim.position().subtract(this.position());
                if (to.normalize().dot(forward) < 0.25) {
                    continue;
                }
                double resist = 1.0 - victim.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE) * 0.6;
                push(victim, to.normalize().scale(1.3 * resist).add(0.0, 0.45 * resist, 0.0));
                victim.hurtServer(level, this.damageSources().mobAttack(this), 2.0f);
            }
            for (int i = 0; i < 30; i++) {
                double s = this.random.nextDouble() * 12.0;
                Vec3 p = this.position().add(forward.scale(s)).add((this.random.nextDouble() - 0.5) * s * 0.6, this.random.nextDouble() * 2.0 - 1.0,
                        (this.random.nextDouble() - 0.5) * s * 0.6);
                level.sendParticles(ParticleTypes.CLOUD, p.x, p.y, p.z, 1, 0.0, 0.0, 0.0, 0.0);
                level.sendParticles(ModParticles.FAE_SPARK.get(), p.x, p.y, p.z, 1, 0.1, 0.1, 0.1, 0.05);
            }
        }
    }

    /** Schuppenwolke: über das Ziel fliegen und glitzernden Staub rieseln lassen. */
    private void tickScales(ServerLevel level, LivingEntity target) {
        Vec3 above = target.position().add(0.0, 7.0, 0.0);
        this.steer(above, 0.08, 0.7);
        int t = this.attackTick;
        if (t >= 14 && t % 4 == 0) {
            if (t % 12 == 0) {
                level.playSound(null, this.blockPosition(), ModSounds.MOTH_QUEEN_SCALES.get(), SoundSource.HOSTILE, 2.0f, 1.0f);
            }
            Vec3 center = new Vec3(this.getX(), target.getY(), this.getZ());
            double radius = this.isEnraged() ? 6.0 : 5.0;
            for (int i = 0; i < 40; i++) {
                double a = this.random.nextDouble() * Math.PI * 2.0;
                double r = Math.sqrt(this.random.nextDouble()) * radius;
                double y = center.y + this.random.nextDouble() * 6.0;
                // silbriger Schuppenstaub mit rosa Glitzer
                level.sendParticles(i % 3 == 0 ? ModParticles.FAE_SPARK.get() : ParticleTypes.WHITE_ASH, center.x + Math.cos(a) * r, y,
                        center.z + Math.sin(a) * r, 1, 0.0, -0.05, 0.0, 0.01);
            }
            for (LivingEntity victim : this.victims(level, new AABB(center, center).inflate(radius, 7.0, radius))) {
                victim.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 0), this);
                victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1), this);
                victim.hurtServer(level, this.damageSources().indirectMagic(this, this), 1.5f);
            }
        }
    }

    /** Mottenschwarm: ein schriller Ruf, dann stürzen sich Mondfalter auf die Gegner. */
    private void tickSwarm(ServerLevel level, LivingEntity target) {
        this.setDeltaMovement(this.getDeltaMovement().scale(0.7));
        if (this.attackTick == 6) {
            level.playSound(null, this.blockPosition(), ModSounds.MOTH_QUEEN_SCREECH.get(), SoundSource.HOSTILE, 3.0f, 1.2f);
        }
        if (this.attackTick == 14) {
            int count = this.isEnraged() ? 5 : 3;
            long alive = level.getEntitiesOfClass(MoonMothEntity.class, this.getBoundingBox().inflate(40.0)).size();
            for (int i = 0; i < count && alive + i < 10; i++) {
                MoonMothEntity moth = ModEntities.MOON_MOTH.get().create(level, EntitySpawnReason.MOB_SUMMONED);
                if (moth == null) {
                    continue;
                }
                double a = i * Math.PI * 2.0 / count;
                moth.snapTo(this.getX() + Math.cos(a) * 2.0, this.getY(0.5), this.getZ() + Math.sin(a) * 2.0, this.getYRot(), 0.0f);
                moth.setTarget(target);
                level.addFreshEntity(moth);
                level.sendParticles(ModParticles.FAE_SPARK.get(), moth.getX(), moth.getY(), moth.getZ(), 10, 0.3, 0.3, 0.3, 0.05);
            }
        }
    }

    /** Mondstrahl: leuchtende Kreise am Boden (dort, wo die Gegner stehen), dann fallen Strahlen aus Mondlicht hinein. */
    private void tickBeam(ServerLevel level, LivingEntity target) {
        this.circle(target, 0.5);
        int t = this.attackTick;
        if (t == 4) {
            this.beams.clear();
            level.playSound(null, this.blockPosition(), ModSounds.MOTH_QUEEN_BEAM_CHARGE.get(), SoundSource.HOSTILE, 3.0f, 1.0f);
            int count = this.isEnraged() ? 4 : 3;
            // ein Strahl auf jeden Spieler (und das Ziel), die übrigen streuen um das Ziel
            List<LivingEntity> marked = new ArrayList<>(level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(32.0),
                    this::canStrike));
            if (!marked.contains(target)) {
                marked.addFirst(target);
            }
            for (int i = 0; i < count; i++) {
                LivingEntity at = marked.get(i % marked.size());
                double spread = i < marked.size() ? 0.0 : 4.0;
                double x = at.getX() + (this.random.nextDouble() - 0.5) * 2.0 * spread;
                double z = at.getZ() + (this.random.nextDouble() - 0.5) * 2.0 * spread;
                int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mth.floor(x), Mth.floor(z));
                this.beams.add(new Vec3(x, y, z));
            }
        }
        if (t >= 4 && t < 34 && t % 2 == 0) {
            for (Vec3 b : this.beams) {
                for (int i = 0; i < 16; i++) {
                    double a = i * Math.PI * 2.0 / 16 + t * 0.05;
                    level.sendParticles(ParticleTypes.END_ROD, b.x + Math.cos(a) * 2.2, b.y + 0.15, b.z + Math.sin(a) * 2.2, 1, 0.0, 0.0, 0.0, 0.0);
                }
            }
        }
        if (t == 36) {
            for (Vec3 b : this.beams) {
                level.playSound(null, BlockPos.containing(b), ModSounds.MOTH_QUEEN_BEAM.get(), SoundSource.HOSTILE, 2.5f, 1.0f);
                for (int h = 0; h < 24; h++) {
                    level.sendParticles(ParticleTypes.END_ROD, b.x, b.y + h * 0.8, b.z, 3, 0.4, 0.3, 0.4, 0.0);
                    level.sendParticles(ModParticles.FAE_SPARK.get(), b.x, b.y + h * 0.8, b.z, 2, 0.6, 0.3, 0.6, 0.02);
                }
                level.sendParticles(net.minecraft.core.particles.ColorParticleOption.create(ParticleTypes.FLASH, 0xFFFFD8F4), b.x, b.y + 0.5, b.z, 1, 0.0, 0.0, 0.0, 0.0);
                for (LivingEntity hit : this.victims(level, new AABB(b, b).inflate(2.4, 3.0, 2.4))) {
                    hit.hurtServer(level, this.damageSources().indirectMagic(this, this), this.isEnraged() ? 11.0f : 9.0f);
                }
            }
        }
    }

    /**
     * Sturzflug: kurz steigen, dann pfeilschnell auf die Stelle herab, an der das Ziel stand. Trifft sie, fliegt das Ziel
     * davon, und sie fängt sich wieder; weicht es aus, prallt sie auf den Boden und liegt benommen da.
     */
    private void tickDive(ServerLevel level, LivingEntity target) {
        int t = this.attackTick;
        if (t < 12) {
            this.setDeltaMovement(this.getDeltaMovement().scale(0.8).add(0.0, 0.06, 0.0));
            if (t == 10) {
                this.diveDir = target.position().add(0.0, 0.4, 0.0).subtract(this.position()).normalize();
                this.diveHit = false;
                level.playSound(null, this.blockPosition(), ModSounds.MOTH_QUEEN_DIVE.get(), SoundSource.HOSTILE, 3.0f, 1.0f);
            }
            return;
        }
        if (t < 36 && !this.diveHit) {
            double speed = this.isEnraged() ? 1.5 : 1.25;
            this.setDeltaMovement(this.diveDir.scale(speed));
            if (t % 2 == 0) {
                level.sendParticles(ModParticles.FAE_SPARK.get(), this.getX(), this.getY(0.5), this.getZ(), 6, 1.5, 0.5, 1.5, 0.02);
            }
            for (LivingEntity victim : this.victims(level, this.getBoundingBox().inflate(0.6))) {
                if (victim.hurtServer(level, this.damageSources().mobAttack(this), this.isEnraged() ? 10.0f : 8.0f)) {
                    push(victim, this.diveDir.scale(1.4).add(0.0, 0.5, 0.0));
                    // nur wenn sie ihr Ziel (oder einen Spieler) erwischt, fängt sie sich – ein Hase im Weg zählt nicht
                    if (victim == target || victim instanceof Player) {
                        this.diveHit = true;
                    }
                }
            }
            if (!this.diveHit && this.groundAhead(level)) {
                this.crash(level);
            }
            return;
        }
        // abfangen und wieder steigen
        this.setDeltaMovement(this.getDeltaMovement().scale(0.85).add(0.0, 0.12, 0.0));
    }

    /** Liegt in Flugrichtung (oder unter ihr) fester Boden? Laub zählt nicht. */
    private boolean groundAhead(ServerLevel level) {
        for (Vec3 probe : new Vec3[] {this.position().add(this.diveDir.scale(1.6)), this.position().add(0.0, -0.4, 0.0)}) {
            BlockPos pos = BlockPos.containing(probe);
            BlockState state = level.getBlockState(pos);
            if (!state.is(net.minecraft.tags.BlockTags.LEAVES) && !state.getCollisionShape(level, pos).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /** Prallt auf: Staub, ein dumpfer Schlag, Funken; dann liegt sie benommen da. */
    private void crash(ServerLevel level) {
        level.playSound(null, this.blockPosition(), net.minecraft.sounds.SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.HOSTILE, 2.0f, 0.6f);
        level.sendParticles(ParticleTypes.CLOUD, this.getX(), this.getY() + 0.3, this.getZ(), 30, 2.0, 0.3, 2.0, 0.05);
        level.sendParticles(ModParticles.FAE_SPARK.get(), this.getX(), this.getY() + 0.5, this.getZ(), 40, 2.0, 0.5, 2.0, 0.1);
        for (LivingEntity victim : this.victims(level, this.getBoundingBox().inflate(2.5))) {
            victim.hurtServer(level, this.damageSources().mobAttack(this), 4.0f);
        }
        this.setDeltaMovement(Vec3.ZERO);
        this.begin(Attack.STUNNED);
    }

    /** Benommen: sinkt zu Boden und bleibt drei Sekunden liegen (taumelnde Funken über dem Kopf). */
    private void tickStunned(ServerLevel level) {
        int t = ++this.attackTick;
        this.setDeltaMovement(this.getDeltaMovement().multiply(0.5, 1.0, 0.5).add(0.0, this.onGround() ? 0.0 : -0.06, 0.0));
        if (t % 5 == 0) {
            double a = t * 0.4;
            level.sendParticles(ModParticles.FAE_SPARK.get(), this.getX() + Math.cos(a) * 1.2, this.getY() + 2.2, this.getZ() + Math.sin(a) * 1.2,
                    1, 0.0, 0.0, 0.0, 0.0);
            level.sendParticles(ParticleTypes.CRIT, this.getX() + Math.cos(a + Math.PI) * 1.2, this.getY() + 2.2,
                    this.getZ() + Math.sin(a + Math.PI) * 1.2, 1, 0.0, 0.0, 0.0, 0.0);
        }
        if (t >= Attack.STUNNED.duration) {
            level.playSound(null, this.blockPosition(), ModSounds.MOTH_QUEEN_GUST.get(), SoundSource.HOSTILE, 2.5f, 1.2f);
            this.setDeltaMovement(0.0, 0.5, 0.0);
            this.begin(Attack.NONE);
            this.cooldown = 20;
        }
    }

    /** Schlaflied: sie singt – wer sie dabei ansieht, wird schwer und schläfrig. */
    private void tickLullaby(ServerLevel level, LivingEntity target) {
        this.setDeltaMovement(this.getDeltaMovement().scale(0.7));
        int t = this.attackTick;
        if (t == 4) {
            level.playSound(null, this.blockPosition(), ModSounds.MOTH_QUEEN_LULLABY.get(), SoundSource.HOSTILE, 3.5f, 1.0f);
            if (!this.lullabyWarned) {
                this.lullabyWarned = true;
                for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, this.getBoundingBox().inflate(24.0))) {
                    player.connection.send(new ClientboundSetActionBarTextPacket(
                            Component.translatable("message.kingdomsatwar.moth_queen.lullaby").withStyle(ChatFormatting.LIGHT_PURPLE)));
                }
            }
        }
        if (t >= 22 && t % 10 == 0) {
            for (LivingEntity victim : this.victims(level, this.getBoundingBox().inflate(20.0))) {
                Vec3 look = victim.getViewVector(1.0f);
                Vec3 to = this.getEyePosition().subtract(victim.getEyePosition()).normalize();
                if (look.dot(to) > 0.55) {
                    victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 70, 2), this);
                    victim.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 70, 1), this);
                    if (victim instanceof Player) {
                        victim.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 70, 0), this);
                        victim.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 100, 0), this);
                    }
                }
            }
        }
    }

    /**
     * Mondfinsternis: Sie steigt höher, und der Mond verfinstert sich – Dunkelheit legt sich über alle in vierzig Blöcken.
     * Dann fallen nacheinander Strahlen aus Mondlicht auf ihre Gegner; ein leuchtender Kreis am Boden warnt jeweils kurz
     * vorher. Wen ein Strahl trifft, der leuchtet und ist im Dunkeln gut zu sehen.
     */
    private void tickEclipse(ServerLevel level, LivingEntity target) {
        this.circle(target, 0.35);
        this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.012, 0.0));
        int t = this.attackTick;
        if (t == 4) {
            level.playSound(null, this.blockPosition(), ModSounds.MOTH_QUEEN_LULLABY.get(), SoundSource.HOSTILE, 3.5f, 0.6f);
            for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, this.getBoundingBox().inflate(40.0), this::canStrike)) {
                player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, ECLIPSE_TICKS - 4, 0, false, false), this);
                player.connection.send(new ClientboundSetActionBarTextPacket(
                        Component.translatable("message.kingdomsatwar.moth_queen.eclipse").withStyle(ChatFormatting.LIGHT_PURPLE)));
            }
            level.sendParticles(ModParticles.FAE_SPARK.get(), this.getX(), this.getY(0.5), this.getZ(), 60, 2.5, 1.5, 2.5, 0.05);
        }
        int step = this.isEnraged() ? 8 : ECLIPSE_STEP;
        if (t >= ECLIPSE_FIRST && t <= ECLIPSE_TICKS - ECLIPSE_WARN - 4 && (t - ECLIPSE_FIRST) % step == 0) {
            List<LivingEntity> marked = new ArrayList<>(level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(32.0), this::canStrike));
            if (!marked.contains(target)) {
                marked.add(target);
            }
            LivingEntity at = marked.get(((t - ECLIPSE_FIRST) / step) % marked.size());
            // dorthin, wo es gleich sein wird
            Vec3 lead = at.position().add(at.getDeltaMovement().multiply(8.0, 0.0, 8.0));
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mth.floor(lead.x), Mth.floor(lead.z));
            Vec3 spot = new Vec3(lead.x, Math.abs(y - at.getY()) < 6 ? y : at.getY(), lead.z);
            float damage = this.isEnraged() ? 11.0f : 9.0f;
            SpellFxEntity.create(level, SpellFxEntity.Kind.CIRCLE, spot.add(0.0, 0.05, 0.0), null, 0xCFDCFF, ECLIPSE_WARN, 2.2f)
                    .onExpire(fx -> this.moonbeam(level, fx.position(), damage)).spawn();
        }
        if (t == ECLIPSE_TICKS - 4) {
            level.sendParticles(net.minecraft.core.particles.ColorParticleOption.create(ParticleTypes.FLASH, 0xFFE8F0FF), this.getX(), this.getY(0.5),
                    this.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    /** Ein Strahl aus Mondlicht fällt an dieser Stelle herab. */
    private void moonbeam(ServerLevel level, Vec3 at, float damage) {
        level.playSound(null, BlockPos.containing(at), ModSounds.MOTH_QUEEN_BEAM.get(), SoundSource.HOSTILE, 2.5f, 0.9f);
        SpellFxEntity.create(level, SpellFxEntity.Kind.PILLAR, at, null, 0xE4ECFF, 16, 1.6f).param(16.0f).spawn();
        level.sendParticles(ModParticles.FAE_SPARK.get(), at.x, at.y + 1.0, at.z, 30, 1.0, 1.5, 1.0, 0.05);
        for (LivingEntity hit : this.victims(level, new AABB(at, at).inflate(2.2, 3.0, 2.2))) {
            if (hit.distanceToSqr(at.x, hit.getY(), at.z) < 2.2 * 2.2 && hit.hurtServer(level, this.damageSources().indirectMagic(this, this), damage)) {
                hit.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0), this);
            }
        }
    }

    /**
     * Seidenkokon: Sie hält inne, ein Faden aus Seide schnellt auf ihr Ziel – und wenn es ihn nicht abschüttelt (aus dem
     * Blick geht oder zu weit fort ist), wird es in einen Kokon gesponnen: kaum zu bewegen, geschwächt, und die Seide
     * schnürt alle Sekunde zu. In Rage spinnt sie auch einen zweiten Gegner ein.
     */
    private void tickCocoon(ServerLevel level, LivingEntity target) {
        this.setDeltaMovement(this.getDeltaMovement().scale(0.75));
        int t = this.attackTick;
        if (t == 4) {
            level.playSound(null, this.blockPosition(), ModSounds.MOTH_QUEEN_SCREECH.get(), SoundSource.HOSTILE, 2.5f, 1.5f);
        }
        if (t == COCOON_THREAD) {
            this.cocoonVictims.clear();
            this.cocoonVictims.add(target);
            if (this.isEnraged()) {
                level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(28.0), p -> p != target && this.canStrike(p)).stream()
                        .findFirst().ifPresent(this.cocoonVictims::add);
            }
            for (LivingEntity victim : this.cocoonVictims) {
                SpellFxEntity.create(level, SpellFxEntity.Kind.BEAM, this.position().add(0.0, this.getBbHeight() * 0.5, 0.0), null, 0xF6F0E2,
                        COCOON_WRAP - COCOON_THREAD + 4, 0.14f).end(victim.position().add(0.0, victim.getBbHeight() * 0.6, 0.0)).spawn();
            }
        }
        if (t == COCOON_WRAP) {
            for (LivingEntity victim : this.cocoonVictims) {
                if (victim.isAlive() && victim.distanceTo(this) < 34.0 && this.hasLineOfSight(victim)) {
                    this.spin(level, victim);
                }
            }
        }
    }

    /** Spinnt ein Wesen in einen Kokon. */
    private void spin(ServerLevel level, LivingEntity victim) {
        level.playSound(null, victim.getX(), victim.getY(), victim.getZ(), ModSounds.MOTH_QUEEN_BEAM_CHARGE.get(), SoundSource.HOSTILE, 1.6f, 1.6f);
        if (victim instanceof ServerPlayer player) {
            player.connection.send(new ClientboundSetActionBarTextPacket(
                    Component.translatable("message.kingdomsatwar.moth_queen.cocoon").withStyle(ChatFormatting.LIGHT_PURPLE)));
        }
        float damage = this.isEnraged() ? 3.0f : 2.0f;
        SpellFxEntity.create(level, SpellFxEntity.Kind.BUBBLE, victim.position(), null, 0xEFE6D2, COCOON_LIFE,
                Math.max(2.4f, victim.getBbHeight() * 1.45f)).anchor(victim).onTick(fx -> {
                    if (!victim.isAlive()) {
                        fx.discard();
                        return;
                    }
                    victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 6, 5, false, false), this);
                    victim.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 6, 1, false, false), this);
                    victim.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 6, 2, false, false), this);
                    if (fx.tickCount % 20 == 10) {
                        victim.hurtServer(level, this.damageSources().indirectMagic(this, this), damage);
                    }
                    if (fx.tickCount % 3 == 0) {
                        level.sendParticles(ParticleTypes.WHITE_ASH, victim.getX(), victim.getY(0.5), victim.getZ(), 6, 0.55, 0.75, 0.55, 0.0);
                        level.sendParticles(ParticleTypes.END_ROD, victim.getX(), victim.getY(0.5), victim.getZ(), 1, 0.6, 0.8, 0.6, 0.0);
                    }
                }).onExpire(fx -> {
                    level.playSound(null, victim.getX(), victim.getY(), victim.getZ(), ModSounds.MOTH_QUEEN_GUST.get(), SoundSource.HOSTILE, 1.0f, 1.8f);
                    level.sendParticles(ParticleTypes.CLOUD, victim.getX(), victim.getY(0.5), victim.getZ(), 24, 0.6, 0.8, 0.6, 0.05);
                }).spawn();
    }

    // ------------------------------------------------------------------ Tod, Speichern, Geräusche

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (this.level() instanceof ServerLevel level) {
            level.sendParticles(ModParticles.FAE_SPARK.get(), this.getX(), this.getY(0.5), this.getZ(), 200, 3.0, 2.0, 3.0, 0.15);
            for (MoonMothEntity moth : level.getEntitiesOfClass(MoonMothEntity.class, this.getBoundingBox().inflate(48.0))) {
                moth.vanish(level);
            }
            // im Mondhain spinnt sich ein neuer Kokon (für den nächsten, der die Königin herausfordern will)
            if (this.home != null && level.getBlockState(this.home).canBeReplaced()) {
                level.setBlockAndUpdate(this.home, io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks.MOTH_COCOON.get().defaultBlockState());
            }
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        this.bossBar.removeAllPlayers();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (this.home != null) {
            output.putLong("Home", this.home.asLong());
        }
        output.putBoolean("Enraged", this.isEnraged());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        input.getLong("Home").ifPresent(h -> this.home = BlockPos.of(h));
        this.entityData.set(DATA_ENRAGED, input.getBooleanOr("Enraged", false));
        if (this.hasCustomName()) {
            this.bossBar.setName(this.getDisplayName());
        }
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return ModSounds.MOTH_QUEEN_AMBIENT.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 160;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.MOTH_QUEEN_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.MOTH_QUEEN_DEATH.get();
    }

    @Override
    protected float getSoundVolume() {
        return 3.0f;
    }

    // ------------------------------------------------------------------ Himmelswacht (0.82)

    @Override
    public io.github.sirschrolli.kingdomsatwar.entity.boss.sky.SkyTheme skyTheme() {
        return io.github.sirschrolli.kingdomsatwar.entity.boss.sky.SkyTheme.MOTH;
    }

    @Override
    public boolean guardsSky() {
        return this.attack != Attack.AWAKEN && this.getTarget() != null && this.getTarget().isAlive();
    }
}
