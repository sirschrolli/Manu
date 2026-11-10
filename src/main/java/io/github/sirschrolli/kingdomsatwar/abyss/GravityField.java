package io.github.sirschrolli.kingdomsatwar.abyss;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.abyss.GravityCrystalBlock;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Die verdrehte Schwerkraft des Abgrunds. Die Kristalle verändern die Schwerkraft (das Attribut {@code gravity}) der
 * Lebewesen in ihrer Nähe – solange sie dort sind, und noch einen Augenblick danach:
 * <ul>
 *     <li><b>Schwebekristall</b>: in einer Kugel von sieben Blöcken Halbmesser nur noch ein Fünftel der Schwerkraft – man
 *     springt fünf Blöcke hoch, sinkt sanft und nimmt keinen Fallschaden;</li>
 *     <li><b>Hebekristall</b>: in einer Säule über ihm (zweieinhalb Blöcke Halbmesser, zwanzig hoch) kehrt sie sich um – man
 *     wird langsam nach oben getragen (höchstens etwa vier Blöcke in der Sekunde); wer schleicht, sinkt darin.</li>
 * </ul>
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class GravityField {
    public static final double FLOAT_RADIUS = 7.0;
    public static final double LIFT_RADIUS = 2.5;
    public static final double LIFT_HEIGHT = 20.0;
    /** Anteil der Schwerkraft, der wegfällt (Schweben) bzw. so viel, dass sie sich sanft umkehrt (Heben). */
    public static final double FLOAT_FACTOR = -0.8;
    public static final double LIFT_FACTOR = -1.05;
    /** Im Hebefeld beim Schleichen: nur ein Zehntel der Schwerkraft (man sinkt sachte). */
    public static final double SINK_FACTOR = -0.9;
    private static final Identifier MODIFIER = KingdomsAtWar.id("gravity_field");
    /** So viele Ticks hält die Wirkung, nachdem man das Feld verlassen hat. */
    private static final int LINGER = 6;

    private static final Map<LivingEntity, Integer> TOUCHED = new WeakHashMap<>();

    private GravityField() {}

    /** Ein Kristall tickt (alle drei Ticks wirkt er auf die Lebewesen in seinem Feld). */
    public static void tickCrystal(ServerLevel level, BlockPos pos, GravityCrystalBlock.Kind kind) {
        if ((level.getGameTime() + pos.asLong()) % 3 != 0) {
            return;
        }
        Vec3 c = Vec3.atCenterOf(pos);
        AABB box = kind == GravityCrystalBlock.Kind.FLOAT
                ? new AABB(c, c).inflate(FLOAT_RADIUS)
                : new AABB(c.x - LIFT_RADIUS, c.y - 1.5, c.z - LIFT_RADIUS, c.x + LIFT_RADIUS, c.y + LIFT_HEIGHT, c.z + LIFT_RADIUS);
        int now = level.getServer().getTickCount();
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && !e.isSpectator())) {
            if (kind == GravityCrystalBlock.Kind.FLOAT) {
                if (e.position().add(0.0, e.getBbHeight() * 0.5, 0.0).distanceToSqr(c) > FLOAT_RADIUS * FLOAT_RADIUS) {
                    continue;
                }
                apply(e, FLOAT_FACTOR, now);
            } else {
                double dx = e.getX() - c.x;
                double dz = e.getZ() - c.z;
                if (dx * dx + dz * dz > LIFT_RADIUS * LIFT_RADIUS) {
                    continue;
                }
                apply(e, e.isShiftKeyDown() ? SINK_FACTOR : LIFT_FACTOR, now);
            }
        }
    }

    /** Gibt dem Lebewesen die veränderte Schwerkraft (oder lässt sie ihm) und verhindert den Fallschaden. */
    public static void apply(LivingEntity entity, double factor, int now) {
        AttributeInstance gravity = entity.getAttribute(Attributes.GRAVITY);
        if (gravity == null) {
            return;
        }
        AttributeModifier current = gravity.getModifier(MODIFIER);
        if (current == null || current.amount() != factor) {
            gravity.removeModifier(MODIFIER);
            gravity.addTransientModifier(new AttributeModifier(MODIFIER, factor, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
        entity.resetFallDistance();
        TOUCHED.put(entity, now + LINGER);
    }

    /** Ist das Lebewesen gerade in einem Feld der verdrehten Schwerkraft? */
    public static boolean affected(LivingEntity entity) {
        AttributeInstance gravity = entity.getAttribute(Attributes.GRAVITY);
        return gravity != null && gravity.getModifier(MODIFIER) != null;
    }

    @SubscribeEvent
    static void onServerTick(ServerTickEvent.Post event) {
        if (TOUCHED.isEmpty()) {
            return;
        }
        int now = event.getServer().getTickCount();
        Iterator<Map.Entry<LivingEntity, Integer>> it = TOUCHED.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<LivingEntity, Integer> entry = it.next();
            LivingEntity entity = entry.getKey();
            if (entity == null || entity.isRemoved() || now > entry.getValue()) {
                if (entity != null) {
                    AttributeInstance gravity = entity.getAttribute(Attributes.GRAVITY);
                    if (gravity != null) {
                        gravity.removeModifier(MODIFIER);
                    }
                    if (!(entity instanceof Player)) {
                        entity.resetFallDistance();
                    }
                }
                it.remove();
            }
        }
    }
}
