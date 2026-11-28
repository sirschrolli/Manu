package io.github.sirschrolli.kingdomsatwar.item.end;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.PhaseGear;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.EndermanAngerEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import org.jspecify.annotations.Nullable;

/**
 * Die Rüstung der Leerwandler (seit 1.4) aus Phasit und schwarzem Leerenstahl:
 * <ul>
 *     <li><b>Phasenschritt</b> (jeder Teil): Trifft dich ein Wesen im Nahkampf, weichst du mit {@value #PHASE_PERCENT} %
 *     je Teil dem Hieb ganz aus – du springst wie ein Leerwandler ein paar Blöcke weit fort (höchstens alle
 *     {@value #COOLDOWN_SECONDS} Sekunden);</li>
 *     <li><b>Schwebende Sohlen</b> (Stiefel): Enderperlen tun nicht mehr weh;</li>
 *     <li><b>Einer von ihnen</b> (alle vier Teile): Die Leerwandler werden nicht wütend, wenn du sie ansiehst.</li>
 * </ul>
 * Wie bei allen Rüstungen der Mod zeigen sich die Wirkungen ohne Symbol.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class VoidwalkerArmor {
    /** So wahrscheinlich weicht man je Teil einem Nahkampfhieb aus. */
    public static final float PHASE_CHANCE = 0.06f;
    static final int PHASE_PERCENT = 6;
    /** So lange (Ticks) nach einem Phasenschritt geht kein weiterer. */
    public static final int COOLDOWN = 60;
    static final int COOLDOWN_SECONDS = 3;
    /** So weit (Blöcke, waagrecht) springt man mindestens und höchstens. */
    public static final double MIN_JUMP = 4.0;
    public static final double MAX_JUMP = 8.0;
    /** So weit darf der Boden am Ziel höher oder tiefer liegen (sonst führte ein Schritt etwa von einer Brücke in die Tiefe). */
    public static final double MAX_DROP = 3.5;
    /** Die Farbe des Nachbilds: Phasit-Türkis. */
    public static final int AFTERIMAGE = 0x3FE6CC;
    private static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    /** Wann jemand zuletzt ausgewichen ist (Spielzeit; nur im Server-Thread). */
    private static final Map<Entity, Long> LAST_PHASE = new WeakHashMap<>();

    private VoidwalkerArmor() {}

    /** Wie viele Teile der Rüstung das Wesen trägt (0–4). */
    public static int pieces(LivingEntity entity) {
        List<DeferredItem<Item>> set = PhaseGear.voidwalkerSet();
        int count = 0;
        for (int i = 0; i < SLOTS.length; i++) {
            if (entity.getItemBySlot(SLOTS[i]).is(set.get(i).get())) {
                count++;
            }
        }
        return count;
    }

    /** Trägt das Wesen die Stiefel der Leerwandler? */
    public static boolean hasBoots(LivingEntity entity) {
        return entity.getItemBySlot(EquipmentSlot.FEET).is(PhaseGear.VOIDWALKER_BOOTS.get());
    }

    /** Ein Nahkampfhieb eines Wesens (kein Pfeil, kein Zauber, keine Explosion)? */
    public static boolean melee(DamageSource source) {
        return source.getEntity() instanceof LivingEntity && source.getDirectEntity() == source.getEntity()
                && !source.is(net.minecraft.tags.DamageTypeTags.IS_EXPLOSION) && !source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_ARMOR);
    }

    /** Darf das Wesen jetzt ausweichen (die Wartezeit ist um)? */
    public static boolean ready(LivingEntity entity) {
        Long last = LAST_PHASE.get(entity);
        long now = entity.level().getGameTime();
        return last == null || now - last >= COOLDOWN || now < last;
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity victim = event.getEntity();
        if (!(victim.level() instanceof ServerLevel level)) {
            return;
        }
        DamageSource source = event.getSource();
        if (source.is(DamageTypes.ENDER_PEARL) && hasBoots(victim)) {
            event.setCanceled(true);                     // die schwebenden Sohlen
            return;
        }
        int pieces = pieces(victim);
        if (pieces == 0 || !melee(source) || !ready(victim) || level.getRandom().nextFloat() >= PHASE_CHANCE * pieces) {
            return;
        }
        if (phase(level, victim, source.getEntity())) {
            event.setCanceled(true);
        }
    }

    /**
     * Der Phasenschritt: Das Wesen springt vier bis acht Blöcke fort – weg vom Angreifer, auf festen Boden, nicht ins Wasser.
     * Gibt zurück, ob es gelang.
     */
    public static boolean phase(ServerLevel level, LivingEntity entity, @Nullable Entity attacker) {
        RandomSource random = entity.getRandom();
        Vec3 from = entity.position();
        double away = attacker == null ? random.nextDouble() * Math.PI * 2.0
                : Math.atan2(entity.getZ() - attacker.getZ(), entity.getX() - attacker.getX());
        for (int attempt = 0; attempt < 16; attempt++) {
            double angle = away + (random.nextDouble() - 0.5) * Math.PI * (attempt < 8 ? 0.9 : 2.0);
            double distance = MIN_JUMP + random.nextDouble() * (MAX_JUMP - MIN_JUMP);
            double x = from.x + Math.cos(angle) * distance;
            double z = from.z + Math.sin(angle) * distance;
            double y = from.y + random.nextInt(5) - 2;
            if (entity.isPassenger()) {
                entity.stopRiding();
            }
            if (entity.randomTeleport(x, y, z, false, state -> state.is(BlockTags.CONSUMABLE_DOES_NOT_TELEPORT_TO)) && !entity.isInWater()) {
                if (Math.abs(entity.getY() - from.y) > MAX_DROP) {
                    // (randomTeleport sucht den Boden beliebig weit unten – ein Schritt soll nicht von der Brücke in die Tiefe führen)
                    entity.teleportTo(from.x, from.y, from.z);
                    continue;
                }
                LAST_PHASE.put(entity, level.getGameTime());
                entity.resetFallDistance();
                level.gameEvent(GameEvent.TELEPORT, from, GameEvent.Context.of(entity));
                phaseEffects(level, entity, from);
                return true;
            }
        }
        return false;
    }

    /**
     * Am alten Ort ein türkises Nachbild (Staub in der Gestalt des Wesens, dazu Funken und der Wirbel der Leerwandler, der sich
     * zusammensaugt), am neuen ein Wirbel, der hervorquillt, und das Geräusch der Leerwandler.
     */
    public static void phaseEffects(ServerLevel level, LivingEntity entity, Vec3 from) {
        double h = entity.getBbHeight();
        double w = entity.getBbWidth();
        level.sendParticles(new DustParticleOptions(AFTERIMAGE, 1.3f), from.x, from.y + h * 0.5, from.z, 46, w * 0.3, h * 0.3, w * 0.3, 0.0);
        level.sendParticles(ParticleTypes.GLOW, from.x, from.y + h * 0.5, from.z, 14, w * 0.4, h * 0.35, w * 0.4, 0.02);
        level.sendParticles(ParticleTypes.PORTAL, from.x, from.y + h * 0.5, from.z, 40, 0.3, h * 0.4, 0.3, 0.6);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, entity.getX(), entity.getY() + h * 0.5, entity.getZ(), 30, w * 0.4, h * 0.35,
                w * 0.4, 0.05);
        level.sendParticles(ParticleTypes.GLOW, entity.getX(), entity.getY() + h * 0.5, entity.getZ(), 8, w * 0.4, h * 0.35, w * 0.4, 0.02);
        level.playSound(null, from.x, from.y, from.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.8f, 1.2f);
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.6f, 1.45f);
    }

    /** Einer von ihnen: Wer die ganze Rüstung trägt, den sehen die Leerwandler nicht als Herausforderer. */
    @SubscribeEvent
    static void onEndermanAnger(EndermanAngerEvent event) {
        if (pieces(event.getPlayer()) == 4) {
            event.setCanceled(true);
        }
    }

    /** Für Prüfungen: die Wartezeit vergessen. */
    public static void forget(LivingEntity entity) {
        LAST_PHASE.remove(entity);
    }
}
