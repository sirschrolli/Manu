package io.github.sirschrolli.kingdomsatwar.item.abyss;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.abyss.AbyssGate;
import io.github.sirschrolli.kingdomsatwar.entity.abyss.WorldChunkEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import io.github.sirschrolli.kingdomsatwar.voidrealm.VoidGate;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Der Weltenspeer: aus dem Fangzahn des Weltenfressers geschmiedet, der Schaft aus Chaosgestein, durch das eine
 * Weltennaht glüht, am Kreuz vier Bruchstücke der Welten, die er verschlang, am Knauf ein Splitter der Königskrone.
 * Ein Speer wie die Speere von Minecraft (Stoß und Ansturm), dazu:
 * <ul>
 *     <li><b>Weltenkraft:</b> im Abgrund und in der Leere trifft er um ein Drittel härter;</li>
 *     <li><b>Weltenmal:</b> jeder Treffer trägt eine der vier verschlungenen Welten in sich, reihum – Glut setzt in Brand,
 *     Frost lähmt, End hebt empor, Schatten blendet und schwächt;</li>
 *     <li><b>Weltenbrocken:</b> jeder vierte Treffer reißt einen Brocken dieser Welt vom Himmel, der auf das Ziel stürzt und
 *     ringsum zerschellt (er verschont den Träger und die Seinen).</li>
 * </ul>
 */
public class WorldSpearItem extends Item {
    private static final String HITS = KingdomsAtWar.MODID + ":world_spear_hits";
    /** Farben der vier Welten (Glut, Frost, End, Schatten). */
    public static final int[] COLORS = {0xFF6A1A, 0x9ADCFF, 0xE8E0A0, 0x6A5AE0};

    public WorldSpearItem(Properties properties) {
        super(properties);
    }

    /** Im Abgrund oder in der Leere (oder in ihren Kampfdimensionen, dem Weltenschlund und der Thronwelt)? */
    public static boolean worldly(Entity entity) {
        Level level = entity.level();
        return io.github.sirschrolli.kingdomsatwar.arena.BossArenaKind.counts(level, AbyssGate.THE_ABYSS)
                || io.github.sirschrolli.kingdomsatwar.arena.BossArenaKind.counts(level, VoidGate.THE_VOID);
    }

    @Override
    public float getAttackDamageBonus(Entity victim, float damage, DamageSource source) {
        if (source.getEntity() instanceof LivingEntity attacker && worldly(attacker)) {
            return damage / 3.0f;
        }
        return 0.0f;
    }

    @Override
    public void postHurtEnemy(ItemStack stack, LivingEntity mob, LivingEntity attacker) {
        super.postHurtEnemy(stack, mob, attacker);
        if (!(attacker.level() instanceof ServerLevel level)) {
            return;
        }
        CompoundTag data = attacker.getPersistentData();
        int hits = data.getIntOr(HITS, 0);
        WorldChunkEntity.Kind kind = WorldChunkEntity.Kind.values()[Math.floorMod(hits, 4)];
        mark(level, mob, attacker, kind);
        hits = (hits + 1) % 4;
        data.putInt(HITS, hits);
        if (hits == 0) {
            fallingChunk(level, mob, attacker, kind);
        }
    }

    /** Weltenmal: die Wirkung der Welt {@code kind} auf das Ziel, mit Funken in ihrer Farbe. */
    public static void mark(ServerLevel level, LivingEntity mob, LivingEntity attacker, WorldChunkEntity.Kind kind) {
        ParticleOptions sparks = switch (kind) {
            case GLUT -> ParticleTypes.FLAME;
            case FROST -> ParticleTypes.SNOWFLAKE;
            case END -> ParticleTypes.END_ROD;
            case SHADOW -> ParticleTypes.SOUL_FIRE_FLAME;
        };
        double y = mob.getY(0.6);
        level.sendParticles(sparks, mob.getX(), y, mob.getZ(), 12, 0.35, 0.45, 0.35, 0.04);
        level.sendParticles(new DustParticleOptions(COLORS[kind.ordinal()], 1.3f), mob.getX(), y, mob.getZ(), 10, 0.4, 0.5, 0.4, 0.0);
        switch (kind) {
            case GLUT -> mob.igniteForSeconds(4.0f);
            case FROST -> {
                mob.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1), attacker);
                mob.setTicksFrozen(Math.max(mob.getTicksFrozen(), 140));
            }
            case END -> mob.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 20, 1), attacker);
            case SHADOW -> {
                mob.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0), attacker);
                mob.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 0), attacker);
            }
        }
    }

    /** Weltenbrocken: ein Brocken der Welt {@code kind} stürzt vom Himmel auf das Ziel (unter Decken entsprechend tiefer). */
    public static void fallingChunk(ServerLevel level, LivingEntity mob, LivingEntity attacker, WorldChunkEntity.Kind kind) {
        Vec3 target = mob.position().add(0.0, mob.getBbHeight() * 0.5, 0.0);
        Vec3 drift = new Vec3((level.getRandom().nextDouble() - 0.5) * 3.0, 0.0, (level.getRandom().nextDouble() - 0.5) * 3.0);
        Vec3 top = target.add(drift).add(0.0, 13.0, 0.0);
        HitResult roof = level.clip(new ClipContext(target, top, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mob));
        double room = roof.getType() == HitResult.Type.MISS ? 13.0 : roof.getLocation().distanceTo(target) - 1.2;
        Vec3 from = target.add(top.subtract(target).normalize().scale(Math.max(1.5, room)));
        Vec3 velocity = target.subtract(from).normalize().scale(1.25);
        WorldChunkEntity.spit(level, attacker, kind, from, velocity, 10.0f);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, from.x, from.y, from.z, 30, 0.6, 0.6, 0.6, 0.08);
        level.playSound(null, from.x, from.y, from.z, ModSounds.WORLD_EATER_RIFT.get(), SoundSource.PLAYERS, 1.0f, 1.4f);
    }
}
