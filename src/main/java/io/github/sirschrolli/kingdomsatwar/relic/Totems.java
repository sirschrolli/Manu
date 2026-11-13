package io.github.sirschrolli.kingdomsatwar.relic;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.consume_effects.TeleportRandomlyConsumeEffect;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingUseTotemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.jspecify.annotations.Nullable;

/**
 * Was die Totems der Mod ({@link TotemKind}) tun.
 * <ul>
 *     <li><b>In der Zweithand:</b> Werte (am Gegenstand selbst), ein dauernder Zustand, und je nach Totem mehr – Angreifer
 *     brennen oder erstarren, Wunden schließen sich, Feinde bemerken den Träger später.</li>
 *     <li><b>Beim Tod:</b> Minecraft hält ihn auf wie beim Totem der Unsterblichkeit (in jeder Hand; Fortschritt, Bild und
 *     Klang inklusive) und ruft dann {@link #revive}: mehr Herzen, eigene Zustände, die Kraft des Elements. Das Totem des
 *     Schöpfers kehrt danach in die Hand zurück, bis es dreimal gehalten hat.</li>
 *     <li><b>Das Totem der Leere</b> rettet zusätzlich aus dem Nichts unter der Welt (davor schützt sonst kein Totem): Es
 *     trägt seinen Träger auf den festen Boden zurück, auf dem er zuletzt stand.</li>
 * </ul>
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class Totems {
    /** Der feste Boden, auf dem ein Spieler zuletzt stand. */
    private record Safe(ResourceKey<Level> dimension, Vec3 pos) {}

    private static final Map<UUID, InteractionHand> LAST_HAND = new HashMap<>();
    private static final Map<UUID, Safe> SAFE = new HashMap<>();

    private Totems() {}

    public static @Nullable TotemKind kindOf(ItemStack stack) {
        return stack.getItem() instanceof TotemItem totem ? totem.kind() : null;
    }

    /** Das Totem in der Zweithand (nur dort wirkt es dauernd). */
    public static @Nullable TotemKind offhand(LivingEntity entity) {
        return kindOf(entity.getOffhandItem());
    }

    // =====================================================================================
    // Beim Tod
    // =====================================================================================

    /** Merkt sich, aus welcher Hand das Totem kam (dorthin kehrt das Totem des Schöpfers zurück). */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    static void onUseTotem(LivingUseTotemEvent event) {
        if (kindOf(event.getTotem()) != null) {
            LAST_HAND.put(event.getEntity().getUUID(), event.getHandHolding());
        }
    }

    /**
     * Das Totem hat den Tod aufgehalten: mehr Herzen, seine Zustände, seine Kraft. {@code used} ist das verbrauchte Totem
     * (die Hand ist schon leer).
     */
    static boolean revive(Level level, ItemStack used, LivingEntity entity, TotemKind kind) {
        if (!(level instanceof ServerLevel server)) {
            return false;
        }
        float max = entity.getMaxHealth();
        entity.setHealth(kind.reviveHealth() < 0.0f ? max : Math.min(max, kind.reviveHealth()));
        for (MobEffectInstance effect : kind.reviveEffects()) {
            entity.addEffect(new MobEffectInstance(effect));
        }
        power(server, used, entity, kind);
        recharge(server, used, entity, kind);
        return true;
    }

    /** Das Totem des Schöpfers hält mehrmals: Mit einer Ladung weniger kehrt es in die Hand zurück. */
    private static void recharge(ServerLevel level, ItemStack used, LivingEntity entity, TotemKind kind) {
        InteractionHand hand = LAST_HAND.remove(entity.getUUID());
        if (kind.charges() <= 1 || !used.isDamageableItem()) {
            return;
        }
        int damage = used.getDamageValue() + 1;
        if (damage >= used.getMaxDamage()) {
            level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ITEM_BREAK.value(), entity.getSoundSource(), 0.9f, 0.8f);
            return;
        }
        ItemStack back = used.copyWithCount(1);
        back.setDamageValue(damage);
        if (hand != null && entity.getItemInHand(hand).isEmpty()) {
            entity.setItemInHand(hand, back);
        } else if (entity instanceof Player player) {
            player.getInventory().placeItemBackInInventory(back, net.minecraft.util.Prediction.SERVER_ONLY);
        } else {
            entity.spawnAtLocation(level, back);
        }
    }

    /** Die Kraft des Elements, die das Totem beim Aufstehen entfesselt. */
    private static void power(ServerLevel level, ItemStack used, LivingEntity user, TotemKind kind) {
        double x = user.getX();
        double y = user.getY();
        double z = user.getZ();
        DamageSource magic = level.damageSources().indirectMagic(user, user);
        switch (kind) {
            case EMBER -> {
                for (LivingEntity foe : foes(level, user, 6.0)) {
                    foe.hurtServer(level, magic, 10.0f);
                    foe.igniteForSeconds(8.0f);
                    push(user, foe, 0.8, 0.35);
                }
                ring(level, ParticleTypes.FLAME, x, y + 0.3, z, 48, 0.42);
                ring(level, ParticleTypes.SMALL_FLAME, x, y + 0.9, z, 32, 0.3);
                level.sendParticles(ParticleTypes.FLAME, x, y + 1.0, z, 60, 1.8, 0.7, 1.8, 0.04);
                level.sendParticles(ParticleTypes.SMOKE, x, y + 1.4, z, 24, 1.6, 0.6, 1.6, 0.02);
                sound(level, user, SoundEvents.FIRECHARGE_USE, 1.2f, 0.7f);
                sound(level, user, SoundEvents.BLAZE_SHOOT, 0.9f, 0.6f);
            }
            case FROST -> {
                for (LivingEntity foe : foes(level, user, 6.0)) {
                    foe.hurtServer(level, magic, 6.0f);
                    foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 120, 4));
                    foe.setTicksFrozen(Math.max(foe.getTicksFrozen(), 300));
                }
                ring(level, ParticleTypes.SNOWFLAKE, x, y + 0.3, z, 56, 0.4);
                level.sendParticles(ParticleTypes.SNOWFLAKE, x, y + 1.0, z, 60, 2.2, 0.8, 2.2, 0.02);
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.ICE.defaultBlockState()), x, y + 0.6, z, 40, 2.0, 0.5, 2.0, 0.0);
                sound(level, user, SoundEvents.GLASS_BREAK, 1.0f, 0.6f);
                sound(level, user, SoundEvents.POWDER_SNOW_BREAK, 1.2f, 0.7f);
            }
            case STORM -> {
                List<LivingEntity> foes = foes(level, user, 8.0);
                for (int i = 0; i < Math.min(6, foes.size()); i++) {
                    LivingEntity foe = foes.get(i);
                    LightningBolt bolt = net.minecraft.world.entity.EntityTypes.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
                    if (bolt != null) {
                        bolt.snapTo(foe.getX(), foe.getY(), foe.getZ());
                        bolt.setVisualOnly(true);
                        level.addFreshEntity(bolt);
                    }
                    foe.hurtServer(level, magic, 12.0f);
                }
                ring(level, ParticleTypes.ELECTRIC_SPARK, x, y + 0.8, z, 48, 0.5);
                sound(level, user, SoundEvents.TRIDENT_THUNDER.value(), 0.9f, 1.2f);
            }
            case STONE -> {
                for (LivingEntity foe : foes(level, user, 6.0)) {
                    foe.hurtServer(level, magic, 6.0f);
                    double resist = 1.0 - foe.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
                    foe.setDeltaMovement(foe.getDeltaMovement().add(0.0, 0.9 * resist, 0.0));
                    foe.needsSync = true;
                }
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()), x, y + 0.2, z, 90, 2.6, 0.2, 2.6, 0.0);
                ring(level, ParticleTypes.CAMPFIRE_COSY_SMOKE, x, y + 0.1, z, 24, 0.12);
                sound(level, user, SoundEvents.GENERIC_EXPLODE.value(), 0.7f, 0.6f);
                sound(level, user, SoundEvents.STONE_BREAK, 1.2f, 0.5f);
            }
            case GROVE -> {
                for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, user.getBoundingBox().inflate(10.0, 5.0, 10.0),
                        e -> e != user && e.isAlive() && e instanceof OwnableEntity pet && pet.getOwner() == user)) {
                    ally.heal(20.0f);
                    level.sendParticles(ParticleTypes.HEART, ally.getX(), ally.getY(1.0) + 0.3, ally.getZ(), 4, 0.3, 0.2, 0.3, 0.0);
                }
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER, x, y + 1.0, z, 50, 2.4, 1.0, 2.4, 0.0);
                level.sendParticles(ParticleTypes.CHERRY_LEAVES, x, y + 2.2, z, 40, 2.0, 0.6, 2.0, 0.0);
                sound(level, user, SoundEvents.BONE_MEAL_USE, 1.2f, 0.8f);
                sound(level, user, SoundEvents.AMETHYST_BLOCK_CHIME, 1.0f, 1.2f);
            }
            case TIDE -> {
                user.clearFire();
                for (LivingEntity foe : foes(level, user, 7.0)) {
                    foe.hurtServer(level, magic, 4.0f);
                    foe.clearFire();
                    push(user, foe, 1.6, 0.45);
                }
                ring(level, ParticleTypes.SPLASH, x, y + 0.4, z, 64, 0.6);
                level.sendParticles(ParticleTypes.SPLASH, x, y + 0.8, z, 120, 2.6, 0.6, 2.6, 0.2);
                level.sendParticles(ParticleTypes.BUBBLE_POP, x, y + 1.0, z, 40, 2.0, 0.8, 2.0, 0.02);
                sound(level, user, SoundEvents.PLAYER_SPLASH_HIGH_SPEED, 1.2f, 0.8f);
                sound(level, user, SoundEvents.TRIDENT_RIPTIDE_3.value(), 0.8f, 0.9f);
            }
            case SHADOW -> {
                for (LivingEntity foe : foes(level, user, 10.0)) {
                    if (foe instanceof Mob mob) {
                        mob.setTarget(null);
                        mob.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
                        mob.getNavigation().stop();
                    }
                    foe.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 160, 1));
                    foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 160, 1));
                }
                level.sendParticles(ParticleTypes.LARGE_SMOKE, x, y + 1.0, z, 60, 1.2, 0.9, 1.2, 0.02);
                level.sendParticles(ParticleTypes.SQUID_INK, x, y + 1.0, z, 40, 0.8, 0.8, 0.8, 0.05);
                level.sendParticles(ParticleTypes.WITCH, x, y + 1.0, z, 30, 1.4, 0.9, 1.4, 0.0);
                sound(level, user, SoundEvents.ILLUSIONER_MIRROR_MOVE, 1.0f, 0.7f);
                sound(level, user, SoundEvents.ENDERMAN_TELEPORT, 0.6f, 0.5f);
            }
            case VOID -> {
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, x, y + 1.0, z, 70, 0.5, 0.9, 0.5, 0.05);
                // fort von dem, was ihn getötet hätte – wie nach einer Chorusfrucht, nur auf festen Boden
                new TeleportRandomlyConsumeEffect(16.0f, true).apply(level, used, user);
                level.sendParticles(ParticleTypes.END_ROD, user.getX(), user.getY() + 1.0, user.getZ(), 24, 0.4, 0.8, 0.4, 0.03);
            }
            case CREATOR -> {
                for (LivingEntity foe : foes(level, user, 8.0)) {
                    foe.hurtServer(level, magic, 16.0f);
                    push(user, foe, 1.0, 0.4);
                }
                ring(level, ParticleTypes.END_ROD, x, y + 0.3, z, 64, 0.5);
                ring(level, ParticleTypes.END_ROD, x, y + 1.2, z, 40, 0.3);
                level.sendParticles(new DustColorTransitionOptions(0xFFE58A, 0xFFFFFF, 1.5f), x, y + 1.1, z, 70, 0.8, 1.1, 0.8, 0.0);
                sound(level, user, SoundEvents.BEACON_ACTIVATE, 1.0f, 1.4f);
                sound(level, user, SoundEvents.AMETHYST_BLOCK_RESONATE, 1.0f, 0.8f);
            }
        }
    }

    /** Wer dem Träger feind ist: Monster – und alles, was ihn gerade angreift. Eigene Tiere und Truppen nie. */
    public static List<LivingEntity> foes(ServerLevel level, LivingEntity user, double radius) {
        return level.getEntitiesOfClass(LivingEntity.class, user.getBoundingBox().inflate(radius, radius * 0.6, radius),
                e -> e != user && e.isAlive() && !e.isSpectator() && e.distanceToSqr(user) <= radius * radius && hostile(user, e));
    }

    private static boolean hostile(LivingEntity user, LivingEntity other) {
        if (other instanceof OwnableEntity pet && pet.getOwner() == user) {
            return false;
        }
        if (other instanceof Mob mob && mob.getTarget() == user) {
            return true;
        }
        return other instanceof Enemy;
    }

    private static void push(LivingEntity from, LivingEntity target, double strength, double lift) {
        Vec3 away = target.position().subtract(from.position()).multiply(1.0, 0.0, 1.0);
        away = away.lengthSqr() < 1.0e-4 ? new Vec3(1.0, 0.0, 0.0) : away.normalize();
        double resist = 1.0 - target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
        target.setDeltaMovement(target.getDeltaMovement().add(away.x * strength * resist, lift * resist, away.z * strength * resist));
        target.needsSync = true;
    }

    /** Ein Ring aus Funken, der vom Träger aus über den Boden läuft. */
    private static void ring(ServerLevel level, ParticleOptions particle, double x, double y, double z, int count, double speed) {
        for (int i = 0; i < count; i++) {
            double a = i * Math.PI * 2.0 / count;
            level.sendParticles(particle, x + Math.cos(a) * 0.7, y, z + Math.sin(a) * 0.7, 0, Math.cos(a), 0.02, Math.sin(a), speed);
        }
    }

    private static void sound(ServerLevel level, LivingEntity at, SoundEvent sound, float volume, float pitch) {
        level.playSound(null, at.getX(), at.getY(), at.getZ(), sound, SoundSource.PLAYERS, volume, pitch);
    }

    // =====================================================================================
    // In der Zweithand
    // =====================================================================================

    @SubscribeEvent
    static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !player.isAlive()) {
            return;
        }
        int tick = player.tickCount;
        if (tick % 10 == 0) {
            remember(player);
        }
        if (tick % 20 != 0) {
            return;
        }
        TotemKind kind = offhand(player);
        if (kind == null) {
            return;
        }
        Holder<MobEffect> aura = kind.aura();
        if (aura != null) {
            // wie ein Leuchtfeuer: ohne Funken, mit Bild – und so lang, dass Nachtsicht nie flackert
            MobEffectInstance current = player.getEffect(aura);
            if (current == null || current.getDuration() < 260) {
                player.addEffect(new MobEffectInstance(aura, 320, 0, true, false, true));
            }
        }
        switch (kind) {
            case FROST -> player.setTicksFrozen(0);
            case GROVE -> {
                player.removeEffect(MobEffects.POISON);
                if (tick % 100 == 0 && player.getHealth() < player.getMaxHealth()) {
                    player.heal(1.0f);
                }
            }
            default -> { }
        }
    }

    /** Wer den Träger im Nahkampf trifft, bekommt die Glut oder den Frost seines Totems zu spüren. */
    @SubscribeEvent
    static void onHurt(LivingDamageEvent.Post event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide() || event.getInflictedDamage() <= 0.0f) {
            return;
        }
        TotemKind kind = offhand(victim);
        Entity direct = event.getSource().getDirectEntity();
        if (kind == null || !(direct instanceof LivingEntity attacker) || attacker == victim) {
            return;
        }
        switch (kind) {
            case EMBER -> attacker.igniteForSeconds(4.0f);
            case FROST -> {
                attacker.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1));
                attacker.setTicksFrozen(Math.min(attacker.getTicksRequiredToFreeze(), attacker.getTicksFrozen() + 100));
            }
            default -> { }
        }
    }

    /** Totem der Schatten: Feinde bemerken den Träger erst aus halber Entfernung. */
    @SubscribeEvent
    static void onVisibility(LivingEvent.LivingVisibilityEvent event) {
        if (offhand(event.getEntity()) == TotemKind.SHADOW) {
            event.modifyVisibility(0.5);
        }
    }

    // =====================================================================================
    // Das Totem der Leere und das Nichts
    // =====================================================================================

    private static void remember(ServerPlayer player) {
        if (!player.onGround() || player.isSpectator() || player.isInLava()) {
            return;
        }
        Level level = player.level();
        BlockPos below = player.getOnPos();
        if (level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)) {
            SAFE.put(player.getUUID(), new Safe(level.dimension(), new Vec3(below.getX() + 0.5, below.getY() + 1.0, below.getZ() + 0.5)));
        }
    }

    /** Der feste Boden, auf den das Totem der Leere diesen Spieler zurücktrüge (null: keiner bekannt). */
    public static @Nullable Vec3 safeSpot(ServerPlayer player) {
        ServerLevel level = player.level();
        Safe safe = SAFE.get(player.getUUID());
        if (safe != null && safe.dimension == level.dimension()) {
            return safe.pos;
        }
        int x = player.getBlockX();
        int z = player.getBlockZ();
        int top = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        return top > level.getMinY() ? new Vec3(x + 0.5, top, z + 0.5) : null;
    }

    /** Der Sturz ins Nichts: Hält der Spieler ein Totem der Leere, trägt es ihn zurück. */
    @SubscribeEvent(priority = EventPriority.HIGH)
    static void onVoidDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getSource().is(DamageTypes.FELL_OUT_OF_WORLD)
                && player.getY() < player.level().getMinY() && voidRescue(player)) {
            event.setCanceled(true);
        }
    }

    /** Rettet den Spieler mit einem Totem der Leere aus dem Nichts. @return ob es eines gab (und festen Boden) */
    public static boolean voidRescue(ServerPlayer player) {
        InteractionHand hand = null;
        for (InteractionHand candidate : InteractionHand.values()) {
            if (kindOf(player.getItemInHand(candidate)) == TotemKind.VOID) {
                hand = candidate;
                break;
            }
        }
        Vec3 safe = hand == null ? null : safeSpot(player);
        if (hand == null || safe == null) {
            return false;
        }
        ServerLevel level = player.level();
        ItemStack stack = player.getItemInHand(hand);
        ItemStack used = stack.copyWithCount(1);
        // erst das Bild (das Spiel sucht das Totem in der Hand), dann ist es verbraucht
        level.broadcastEntityEvent(player, (byte) 35);
        stack.shrink(1);
        player.awardStat(Stats.ITEM_USED.get(used.getItem()));
        CriteriaTriggers.USED_TOTEM.trigger(player, used);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, player.getX(), player.getY() + 1.0, player.getZ(), 60, 0.5, 0.9, 0.5, 0.05);
        player.teleportTo(level, safe.x, safe.y, safe.z, Set.of(), player.getYRot(), player.getXRot(), true);
        player.setDeltaMovement(Vec3.ZERO);
        player.needsSync = true;
        player.resetFallDistance();
        player.removeAllEffects();
        player.setHealth(Math.min(player.getMaxHealth(), TotemKind.VOID.reviveHealth()));
        for (MobEffectInstance effect : TotemKind.VOID.reviveEffects()) {
            player.addEffect(new MobEffectInstance(effect));
        }
        level.sendParticles(ParticleTypes.END_ROD, safe.x, safe.y + 1.0, safe.z, 30, 0.4, 0.8, 0.4, 0.03);
        level.playSound(null, safe.x, safe.y, safe.z, SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1.0f, 0.8f);
        return true;
    }

    @SubscribeEvent
    static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID id = event.getEntity().getUUID();
        LAST_HAND.remove(id);
        SAFE.remove(id);
    }
}
