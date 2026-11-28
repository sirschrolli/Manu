package io.github.sirschrolli.kingdomsatwar.item.nether;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.nether.LavaCrustBlock;
import io.github.sirschrolli.kingdomsatwar.registry.FlameGear;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * Die Rüstung der Glutwandler (seit 1.1.6) aus Feueropal und geschwärztem Stahl:
 * <ul>
 *     <li><b>Glutfest</b> (jeder Teil): Feuer, Lava und glühende Böden schaden um {@link #FIRE_SHIELD} weniger – mit allen
 *     vier Teilen nur noch ein Fünftel;</li>
 *     <li><b>Glutblick</b> (Helm): In der Lava sieht man weit (statt nur eine Armlänge) – siehe
 *     {@link io.github.sirschrolli.kingdomsatwar.client.armor.EmberwalkerSight};</li>
 *     <li><b>Glutwandeln</b> (Stiefel): Wo man Lava betritt, erstarrt sie in einem Kreis von {@link #CRUST_RADIUS} Blöcken
 *     zu einer {@link LavaCrustBlock Glutkruste}, die trägt – so geht man über das Lavameer. Bleibt man stehen, hält die
 *     Kruste; geht man weiter, bricht sie hinter einem wieder auf.</li>
 * </ul>
 * Wie bei allen Rüstungen der Mod zeigen sich die Wirkungen ohne Symbol.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class EmberwalkerArmor {
    /** So viel weniger Feuerschaden je Teil. */
    public static final float FIRE_SHIELD = 0.2f;
    /** So weit (Halbmesser in Blöcken) erstarrt die Lava unter den Stiefeln. */
    public static final int CRUST_RADIUS = 3;
    private static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    private EmberwalkerArmor() {}

    /** Wie viele Teile der Rüstung das Wesen trägt (0–4). */
    public static int pieces(LivingEntity entity) {
        List<DeferredItem<Item>> set = FlameGear.emberwalkerSet();
        int count = 0;
        for (int i = 0; i < SLOTS.length; i++) {
            if (entity.getItemBySlot(SLOTS[i]).is(set.get(i).get())) {
                count++;
            }
        }
        return count;
    }

    /** Trägt das Wesen die Stiefel der Glutwandler? */
    public static boolean hasBoots(Entity entity) {
        return entity instanceof LivingEntity living && living.getItemBySlot(EquipmentSlot.FEET).is(FlameGear.EMBERWALKER_BOOTS.get());
    }

    /** Trägt das Wesen den Helm der Glutwandler? */
    public static boolean hasHelmet(Entity entity) {
        return entity instanceof LivingEntity living && living.getItemBySlot(EquipmentSlot.HEAD).is(FlameGear.EMBERWALKER_HELMET.get());
    }

    /** Wie viel Feuerschaden bei so vielen Teilen noch ankommt (Anteil). */
    public static float fireFactor(int pieces) {
        return Math.max(0.0f, 1.0f - FIRE_SHIELD * pieces);
    }

    @SubscribeEvent
    static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!event.getSource().is(DamageTypeTags.IS_FIRE)) {
            return;
        }
        int pieces = pieces(event.getEntity());
        if (pieces > 0) {
            event.setAmount(event.getAmount() * fireFactor(pieces));
        }
    }

    @SubscribeEvent
    static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player && !player.isSpectator()) {
            walk(player.level(), player);
        }
    }

    /**
     * Glutwandeln: Unter dem Träger erstarrt die Lava (nur stille Lava, über der Platz ist) zu einer Kruste – nur, wenn er
     * steht oder geht, nicht im Sprung und nicht, solange er in der Lava schwimmt; und nur mit den Stiefeln der Glutwandler.
     * Gibt zurück, wie viele Blöcke erstarrten.
     */
    public static int walk(ServerLevel level, LivingEntity entity) {
        if (!entity.onGround() || entity.isInLava() || !hasBoots(entity)) {
            return 0;
        }
        BlockState crust = FlameGear.LAVA_CRUST.get().defaultBlockState();
        BlockPos center = entity.blockPosition().below();
        int made = 0;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-CRUST_RADIUS, 0, -CRUST_RADIUS), center.offset(CRUST_RADIUS, 0, CRUST_RADIUS))) {
            double dx = pos.getX() + 0.5 - entity.getX();
            double dz = pos.getZ() + 0.5 - entity.getZ();
            if (dx * dx + dz * dz > (CRUST_RADIUS + 0.5) * (CRUST_RADIUS + 0.5)) {
                continue;
            }
            BlockState state = level.getBlockState(pos);
            FluidState fluid = state.getFluidState();
            if (!state.is(Blocks.LAVA) || !fluid.isSource() || !fluid.is(FluidTags.LAVA)) {
                continue;
            }
            if (!level.getBlockState(pos.above()).isAir() || !level.isUnobstructed(crust, pos, CollisionContext.empty())) {
                continue;
            }
            level.setBlockAndUpdate(pos.immutable(), crust);
            made++;
            if (level.getRandom().nextInt(4) == 0) {
                level.sendParticles(ParticleTypes.LARGE_SMOKE, pos.getX() + 0.5, pos.getY() + 1.05, pos.getZ() + 0.5, 1, 0.2, 0.0, 0.2, 0.01);
            }
        }
        if (made > 0 && level.getGameTime() % 4 == 0) {
            level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.LAVA_EXTINGUISH, SoundSource.PLAYERS,
                    0.18f, 1.5f + level.getRandom().nextFloat() * 0.3f);
        }
        return made;
    }
}
