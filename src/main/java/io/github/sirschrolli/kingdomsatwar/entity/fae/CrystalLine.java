package io.github.sirschrolli.kingdomsatwar.entity.fae;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.magic.fx.SpellFxEntity;
import io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jspecify.annotations.Nullable;

/**
 * Eine Kristallspur (seit 0.96): Mondsteinkristalle brechen nacheinander aus dem Boden, Schritt für Schritt in eine Richtung
 * – die Kristallspur des Steinhüters und die des Hüterhammers (seit 0.97 auch als Korallenspieße der Meerhexe). Die Spur folgt dem Gelände (Stufen hinauf und hinab, bis drei
 * Blöcke) und endet an Wänden und Abgründen. Wen ein Kristall erwischt, der wird hochgeschleudert, nimmt Schaden und ist kurz
 * verlangsamt (jedes Wesen nur einmal je Spur).
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class CrystalLine {
    /** Wie die Spur aussieht: Mondsteinkristalle (Steinhüter, Hüterhammer) oder Korallenspieße (Meerhexe, seit 0.97). */
    public enum Style {
        MOONSTONE, CORAL
    }

    private static final List<CrystalLine> ACTIVE = new ArrayList<>();
    /** So weit (waagerecht) um einen Kristall wird getroffen. */
    public static final double HIT_RADIUS = 1.3;

    private final ServerLevel level;
    private final Vec3 dir;
    private final int steps;
    private final double spacing;
    private final int interval;
    private final float damage;
    private final float size;
    private final @Nullable LivingEntity owner;
    private final Predicate<LivingEntity> canHit;
    private final Style style;
    private final Set<UUID> hit = new HashSet<>();
    private Vec3 next;
    private double floorY;
    private int step;
    private int timer;

    private CrystalLine(ServerLevel level, Vec3 start, Vec3 dir, int steps, double spacing, int interval, float damage, float size,
            @Nullable LivingEntity owner, Predicate<LivingEntity> canHit, Style style) {
        this.level = level;
        this.style = style;
        this.dir = new Vec3(dir.x, 0.0, dir.z).normalize();
        this.steps = steps;
        this.spacing = spacing;
        this.interval = Math.max(1, interval);
        this.damage = damage;
        this.size = size;
        this.owner = owner;
        this.canHit = canHit;
        this.next = start;
        this.floorY = start.y;
    }

    /**
     * Beginnt eine Spur bei {@code start} (Bodenhöhe) in Richtung {@code dir}: {@code steps} Kristalle im Abstand
     * {@code spacing}, alle {@code interval} Ticks einer (der erste sofort).
     */
    public static CrystalLine start(ServerLevel level, Vec3 start, Vec3 dir, int steps, double spacing, int interval, float damage, float size,
            @Nullable LivingEntity owner, Predicate<LivingEntity> canHit) {
        return start(level, start, dir, steps, spacing, interval, damage, size, owner, canHit, Style.MOONSTONE);
    }

    /** Wie oben, in einem anderen Aussehen ({@link Style}). */
    public static CrystalLine start(ServerLevel level, Vec3 start, Vec3 dir, int steps, double spacing, int interval, float damage, float size,
            @Nullable LivingEntity owner, Predicate<LivingEntity> canHit, Style style) {
        CrystalLine line = new CrystalLine(level, start, dir, steps, spacing, interval, damage, size, owner, canHit, style);
        ACTIVE.add(line);
        line.erupt();
        return line;
    }

    /** Laufende Spuren (für Tests). */
    public static int active() {
        return ACTIVE.size();
    }

    @SubscribeEvent
    static void onServerTick(ServerTickEvent.Post event) {
        if (ACTIVE.isEmpty()) {
            return;
        }
        List<CrystalLine> copy = new ArrayList<>(ACTIVE);
        for (CrystalLine line : copy) {
            if (line.level.getServer() != event.getServer() || line.done()) {
                ACTIVE.remove(line);
                continue;
            }
            if (--line.timer <= 0) {
                line.erupt();
                if (line.done()) {
                    ACTIVE.remove(line);
                }
            }
        }
    }

    private boolean done() {
        return this.step >= this.steps;
    }

    /** Der nächste Kristall: Boden suchen (bis drei Blöcke höher oder tiefer), aufbrechen, treffen. */
    private void erupt() {
        this.timer = this.interval;
        Double ground = this.groundAt(this.next.x, this.floorY, this.next.z);
        if (ground == null) {
            this.step = this.steps;
            return;
        }
        this.floorY = ground;
        Vec3 at = new Vec3(this.next.x, ground, this.next.z);
        boolean coral = this.style == Style.CORAL;
        SpellFxEntity.create(this.level, coral ? SpellFxEntity.Kind.CORAL : SpellFxEntity.Kind.CRYSTAL, at, null, coral ? 0xFF7A8A : 0x8EC8FF, 24,
                this.size * (0.85f + 0.3f * this.level.getRandom().nextFloat())).spawn();
        BlockPos below = BlockPos.containing(at.x, at.y - 0.5, at.z);
        BlockState floor = this.level.getBlockState(below);
        if (!floor.isAir()) {
            this.level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, floor), at.x, at.y + 0.2, at.z, 14, 0.5, 0.15, 0.5, 0.12);
        }
        BlockState chips = coral ? net.minecraft.world.level.block.Blocks.FIRE_CORAL_BLOCK.defaultBlockState()
                : FaeBlocks.MOONSTONE_BLOCK.get().defaultBlockState();
        this.level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, chips), at.x, at.y + 0.8, at.z, 6, 0.3, 0.5, 0.3, 0.1);
        if (coral) {
            this.level.sendParticles(ParticleTypes.BUBBLE, at.x, at.y + 0.6, at.z, 10, 0.4, 0.6, 0.4, 0.08);
        }
        if (this.step % 2 == 0) {
            this.level.playSound(null, at.x, at.y, at.z, coral ? ModSounds.SEA_WITCH_CORAL.get() : ModSounds.STONE_KEEPER_CRYSTAL.get(),
                    SoundSource.HOSTILE, 1.4f, 0.85f + this.level.getRandom().nextFloat() * 0.3f);
        }
        double r = HIT_RADIUS * Math.max(0.8, this.size / 0.8);
        AABB box = new AABB(at.x - r, at.y - 0.5, at.z - r, at.x + r, at.y + 2.6, at.z + r);
        for (LivingEntity victim : this.level.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != this.owner && this.canHit.test(e))) {
            if (Math.hypot(victim.getX() - at.x, victim.getZ() - at.z) > r + victim.getBbWidth() * 0.5 || !this.hit.add(victim.getUUID())) {
                continue;
            }
            if (victim.hurtServer(this.level, this.damageSource(), this.damage)) {
                victim.setDeltaMovement(victim.getDeltaMovement().multiply(0.5, 0.0, 0.5).add(this.dir.x * 0.25, 0.62, this.dir.z * 0.25));
                victim.needsSync = true;
                victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 1), this.owner);
            }
        }
        this.step++;
        this.next = this.next.add(this.dir.scale(this.spacing));
    }

    private DamageSource damageSource() {
        if (this.owner instanceof Player player) {
            return this.level.damageSources().playerAttack(player);
        }
        if (this.owner != null) {
            return this.level.damageSources().mobAttack(this.owner);
        }
        return this.level.damageSources().magic();
    }

    /** Oberkante festen Bodens bei (x, z) nahe {@code y}: erst bis drei Blöcke hinauf, dann bis drei hinab; sonst null. */
    private @Nullable Double groundAt(double x, double y, double z) {
        BlockPos base = BlockPos.containing(x, y - 0.5, z);
        for (int dy = 0; dy <= 3; dy++) {
            BlockPos pos = base.above(dy);
            if (this.solid(pos) && !this.solid(pos.above()) && !this.solid(pos.above(2))) {
                return (double) pos.getY() + 1.0;
            }
        }
        for (int dy = 1; dy <= 3; dy++) {
            BlockPos pos = base.below(dy);
            if (this.solid(pos) && !this.solid(pos.above())) {
                return (double) pos.getY() + 1.0;
            }
        }
        return null;
    }

    private boolean solid(BlockPos pos) {
        return this.level.getBlockState(pos).isFaceSturdy(this.level, pos, Direction.UP);
    }
}
