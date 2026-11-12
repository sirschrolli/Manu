package io.github.sirschrolli.kingdomsatwar.item.monster;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.MonsterGear;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * Die Kräfte der Monsterausrüstung (0.70):
 * <ul>
 *   <li>voller Satz <b>Werwolfspelz</b>: nachts Schnelligkeit und Nachtsicht;</li>
 *   <li>voller Satz <b>Glutwurm-Panzer</b>: Feuerresistenz, und wer den Träger im Nahkampf trifft, fängt Feuer;</li>
 *   <li><b>Krallenklinge</b>: Blutrausch nach jedem Sieg.</li>
 * </ul>
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class MonsterGearEvents {
    /** So lange brennt, wer den Glutwurm-Panzer trifft (Sekunden). */
    public static final float EMBER_RETALIATION_SECONDS = 4.0f;

    private MonsterGearEvents() {}

    public enum ArmorSet {
        PELT, EMBER
    }

    /** Welchen vollständigen Satz trägt das Wesen (oder {@code null})? */
    public static ArmorSet fullSet(LivingEntity entity) {
        if (wears(entity, MonsterGear.peltSet())) {
            return ArmorSet.PELT;
        }
        if (wears(entity, MonsterGear.emberSet())) {
            return ArmorSet.EMBER;
        }
        return null;
    }

    private static boolean wears(LivingEntity entity, List<DeferredItem<Item>> set) {
        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        for (int i = 0; i < 4; i++) {
            if (!entity.getItemBySlot(slots[i]).is(set.get(i).get())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Die Kraft des vollen Satzes (jede Sekunde geprüft): still, ohne Symbol; legt man den Satz ab (oder wird es beim Pelz
     * Tag), endet sie sofort. {@code set} null: kein voller Satz.
     */
    public static void applySetBonus(LivingEntity entity, @org.jspecify.annotations.Nullable ArmorSet set) {
        boolean night = set == ArmorSet.PELT && entity.level().isDarkOutside();
        io.github.sirschrolli.kingdomsatwar.item.GearEffects.set(entity, MobEffects.SPEED, 0, "werewolf_pelt", night);
        io.github.sirschrolli.kingdomsatwar.item.GearEffects.set(entity, MobEffects.NIGHT_VISION, 0, "werewolf_pelt", night);
        io.github.sirschrolli.kingdomsatwar.item.GearEffects.set(entity, MobEffects.FIRE_RESISTANCE, 0, "ember_scale", set == ArmorSet.EMBER);
    }

    @SubscribeEvent
    static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || player.tickCount % 20 != 0 || !player.isAlive()) {
            return;
        }
        applySetBonus(player, fullSet(player));
    }

    /** Krallenklinge: wer mit ihr ein Wesen besiegt, gerät in einen Blutrausch. */
    @SubscribeEvent
    static void onDeath(LivingDeathEvent event) {
        if (event.getSource().getDirectEntity() instanceof LivingEntity killer && killer != event.getEntity()
                && killer.getMainHandItem().getItem() instanceof ClawBladeItem && !killer.level().isClientSide()) {
            ClawBladeItem.frenzy(killer);
        }
    }

    /** Glutwurm-Panzer: wer den Träger im Nahkampf trifft, fängt Feuer. */
    @SubscribeEvent
    static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity victim = event.getEntity();
        if (!(victim.level() instanceof ServerLevel level) || !(event.getSource().getDirectEntity() instanceof LivingEntity attacker)
                || attacker == victim || attacker.fireImmune() || attacker.distanceToSqr(victim) > 25.0) {
            return;
        }
        if (fullSet(victim) == ArmorSet.EMBER) {
            attacker.igniteForSeconds(EMBER_RETALIATION_SECONDS);
            level.sendParticles(ParticleTypes.FLAME, attacker.getX(), attacker.getY(0.6), attacker.getZ(), 8, 0.25, 0.35, 0.25, 0.02);
        }
    }
}
