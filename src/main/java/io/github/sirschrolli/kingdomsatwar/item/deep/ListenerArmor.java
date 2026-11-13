package io.github.sirschrolli.kingdomsatwar.item.deep;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.magic.fx.SpellFxEntity;
import io.github.sirschrolli.kingdomsatwar.registry.EchoGear;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.VanillaGameEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * Die Rüstung der Lauscher (seit 1.6) aus Hallkristall auf Platten aus Tiefenschiefer:
 * <ul>
 *     <li><b>Schallschutz</b> (jeder Teil): {@value #SONIC_PERCENT} % weniger Schaden je Teil durch den Schallschlag des
 *     Tiefenhüters;</li>
 *     <li><b>Klare Sicht</b> (Helm): Die Dunkelheit, die der Tiefenhüter und die Kreischer verbreiten, legt sich nicht mehr
 *     über die Augen;</li>
 *     <li><b>Wollsohlen</b> (Stiefel): Schritte, Sprünge, Landungen und Schwimmzüge machen keine Erschütterung mehr –
 *     Sculk-Sensoren, Kreischer und der Tiefenhüter hören sie nicht;</li>
 *     <li><b>Echoortung</b> (alle vier Teile): Wer schleicht, ruft alle {@value #ECHO_SECONDS} Sekunden leise in die
 *     Dunkelheit – ein Ring breitet sich aus, und jedes Wesen in {@value #ECHO_BLOCKS} Blöcken leuchtet kurz auf, auch durch
 *     Wände hindurch.</li>
 * </ul>
 * Wie bei allen Rüstungen der Mod zeigen sich die Wirkungen ohne Symbol.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class ListenerArmor {
    /** So viel weniger Schaden durch den Schallschlag je Teil. */
    public static final float SONIC_SHIELD = 0.2f;
    static final int SONIC_PERCENT = 20;
    /** Echoortung: so oft (Ticks) beim Schleichen. */
    public static final int ECHO_INTERVAL = 40;
    static final int ECHO_SECONDS = 2;
    /** So weit reicht der Ruf (Blöcke). */
    public static final double ECHO_RADIUS = 16.0;
    static final int ECHO_BLOCKS = 16;
    /** So lange leuchten die Wesen danach (Ticks). */
    public static final int ECHO_GLOW = 50;
    /** Die Farbe des Rufs: das blasse Blau des Hallkristalls. */
    public static final int ECHO_COLOR = 0x9CD8FF;
    private static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    /** Wann jemand zuletzt gerufen hat (Spielzeit; nur im Server-Thread). */
    private static final Map<Entity, Long> LAST_ECHO = new WeakHashMap<>();

    private ListenerArmor() {}

    /** Wie viele Teile der Rüstung das Wesen trägt (0–4). */
    public static int pieces(LivingEntity entity) {
        List<DeferredItem<Item>> set = EchoGear.listenerSet();
        int count = 0;
        for (int i = 0; i < SLOTS.length; i++) {
            if (entity.getItemBySlot(SLOTS[i]).is(set.get(i).get())) {
                count++;
            }
        }
        return count;
    }

    /** Trägt das Wesen den Helm der Lauscher? */
    public static boolean hasHelmet(LivingEntity entity) {
        return entity.getItemBySlot(EquipmentSlot.HEAD).is(EchoGear.LISTENER_HELMET.get());
    }

    /** Trägt das Wesen die Stiefel der Lauscher (mit den Wollsohlen)? */
    public static boolean hasBoots(LivingEntity entity) {
        return entity.getItemBySlot(EquipmentSlot.FEET).is(EchoGear.LISTENER_BOOTS.get());
    }

    /** Die Erschütterungen, die die Wollsohlen verschlucken: Schritte, Landungen, Platschen, Schwimmen, Gleiten. */
    public static boolean muffled(Holder<GameEvent> event) {
        return event.is(GameEvent.STEP) || event.is(GameEvent.HIT_GROUND) || event.is(GameEvent.SPLASH) || event.is(GameEvent.SWIM)
                || event.is(GameEvent.ELYTRA_GLIDE);
    }

    /** Die Wollsohlen: Was die Stiefel verschlucken, erreicht keinen Sculk und keinen Tiefenhüter. */
    @SubscribeEvent
    static void onGameEvent(VanillaGameEvent event) {
        if (event.getCause() instanceof LivingEntity living && muffled(event.getVanillaEvent()) && hasBoots(living)) {
            event.setCanceled(true);
        }
    }

    /** Klare Sicht: Mit dem Helm legt sich keine Dunkelheit über die Augen. */
    @SubscribeEvent
    static void onEffect(MobEffectEvent.Applicable event) {
        if (event.getEffectInstance().is(MobEffects.DARKNESS) && hasHelmet(event.getEntity())) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
        }
    }

    /** Schallschutz: Jeder Teil fängt einen Teil des Schallschlags ab. */
    @SubscribeEvent
    static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!event.getSource().is(DamageTypes.SONIC_BOOM)) {
            return;
        }
        int pieces = pieces(event.getEntity());
        if (pieces > 0) {
            event.setAmount(event.getAmount() * Math.max(0.0f, 1.0f - SONIC_SHIELD * pieces));
        }
    }

    /** Echoortung: Wer in der ganzen Rüstung schleicht, ruft alle zwei Sekunden in die Dunkelheit. */
    @SubscribeEvent
    static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!(player.level() instanceof ServerLevel level) || !player.isCrouching() || player.isSpectator() || pieces(player) < 4) {
            return;
        }
        if (ready(player)) {
            echo(level, player);
        }
    }

    /** Darf das Wesen wieder rufen (die Wartezeit ist um)? */
    public static boolean ready(LivingEntity entity) {
        Long last = LAST_ECHO.get(entity);
        long now = entity.level().getGameTime();
        return last == null || now - last >= ECHO_INTERVAL || now < last;
    }

    /**
     * Der Ruf: ein leiser, gläserner Ton, ein Ring aus blassem Licht breitet sich aus, und jedes Wesen in
     * {@link #ECHO_RADIUS} Blöcken leuchtet kurz auf (auch hinter Wänden). Gibt zurück, wie viele es fand.
     */
    public static int echo(ServerLevel level, LivingEntity source) {
        LAST_ECHO.put(source, level.getGameTime());
        Vec3 at = source.position();
        level.playSound(null, at.x, at.y, at.z, ModSounds.LISTENER_ECHO.get(), SoundSource.PLAYERS, 0.7f, 0.95f + level.getRandom().nextFloat() * 0.1f);
        SpellFxEntity.create(level, SpellFxEntity.Kind.SHOCKWAVE, at.add(0.0, 0.1, 0.0), null, ECHO_COLOR, 20, 0.6f)
                .param((float) (ECHO_RADIUS / 20.0)).spawn();
        int found = 0;
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, new AABB(at, at).inflate(ECHO_RADIUS),
                e -> e != source && e.isAlive() && !(e instanceof ArmorStand) && !e.isSpectator()
                        && e.distanceToSqr(at) <= ECHO_RADIUS * ECHO_RADIUS)) {
            e.addEffect(new MobEffectInstance(MobEffects.GLOWING, ECHO_GLOW, 0, false, false), source);
            level.sendParticles(ParticleTypes.SCULK_CHARGE_POP, e.getX(), e.getY(0.6), e.getZ(), 4, 0.25, 0.4, 0.25, 0.01);
            found++;
        }
        return found;
    }

    /** Für Tests: die Wartezeit vergessen. */
    public static void forget(Entity entity) {
        LAST_ECHO.remove(entity);
    }
}
