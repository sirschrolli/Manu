package io.github.sirschrolli.kingdomsatwar.item.abyss;

import io.github.sirschrolli.kingdomsatwar.enchant.EnchantPower;
import io.github.sirschrolli.kingdomsatwar.magic.fx.SpellFxEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.Set;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Der Minutenzeiger (seit 1.01): die Beute des Stundenwächters – der lange Zeiger der großen Uhr als Klinge, aus Uhrbronze
 * mit einem Kern aus Chronit, die Spitze ein durchbrochenes Herz wie an alten Turmuhren. Er schlägt wie ein Schwert, und
 * <ul>
 *     <li>jeder Treffer rückt ihn eine Minute vor; der <b>zwölfte schlägt die Stunde</b>: Ein Glockenschlag rollt um das Ziel
 *     ({@link #HOUR_DAMAGE} Schaden, mit Schärfe mehr), und wer ringsum steht, gerät ins Stocken;</li>
 *     <li>Rechtsklick: der <b>Zeitsprung</b> – man vergeht in Bernsteinlicht und steht bis zu {@link #JUMP_RANGE} Blöcke weiter
 *     in Blickrichtung (nicht durch Wände), danach ruht er {@link #COOLDOWN} Ticks.</li>
 * </ul>
 */
public class MinuteHandItem extends Item {
    public static final int COOLDOWN = 120;
    public static final double JUMP_RANGE = 8.0;
    public static final float HOUR_DAMAGE = 8.0f;
    public static final double HOUR_RADIUS = 4.0;
    /** So viele Treffer, dann schlägt die Stunde. */
    public static final int MINUTES = 12;
    private static final String KEY = "kingdomsatwar_minute_hand";

    public MinuteHandItem(Properties properties) {
        super(properties);
    }

    /** Wie viele Minuten der Zeiger seit der letzten Stunde vorgerückt ist (0–11). */
    public static int minutes(Player player) {
        return player.getPersistentData().getIntOr(KEY, 0);
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.hurtEnemy(stack, target, attacker);
        if (!(attacker instanceof ServerPlayer player) || !(player.level() instanceof ServerLevel level)) {
            return;
        }
        int minutes = minutes(player) + 1;
        if (minutes >= MINUTES) {
            player.getPersistentData().putInt(KEY, 0);
            strikeHour(level, player, target, HOUR_DAMAGE + 1.5f * EnchantPower.level(stack, Enchantments.SHARPNESS));
        } else {
            player.getPersistentData().putInt(KEY, minutes);
            if (minutes == MINUTES - 1) {
                player.sendOverlayMessage(Component.translatable("item.kingdomsatwar.minute_hand.ready").withColor(0xFFC86A));
            }
        }
    }

    /** Die volle Stunde: ein Glockenschlag um das Ziel – Schaden ringsum, und alle (außer dem Spieler) geraten ins Stocken. */
    public static void strikeHour(ServerLevel level, Player player, LivingEntity target, float damage) {
        Vec3 at = target.position();
        level.playSound(null, at.x, at.y, at.z, ModSounds.MINUTE_HAND_HOUR.get(), SoundSource.PLAYERS, 2.0f, 1.0f);
        SpellFxEntity.create(level, SpellFxEntity.Kind.SHOCKWAVE, at.add(0.0, 0.05, 0.0), null, 0xFFB347, 10, 0.6f).param(0.4f).spawn();
        level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0xFFFFB347), at.x, at.y + 1.0, at.z, 1, 0.0, 0.0, 0.0, 0.0);
        level.sendParticles(TimewardenArmor.AMBER, at.x, at.y + 1.0, at.z, 30, 1.2, 0.6, 1.2, 0.0);
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, new AABB(at, at).inflate(HOUR_RADIUS, 2.5, HOUR_RADIUS),
                e -> e.isAlive() && e != player && !(e instanceof ArmorStand) && !(e instanceof OwnableEntity pet && player.equals(pet.getOwner()))
                        && !(e instanceof Player p && (p.isCreative() || p.isSpectator() || !player.canHarmPlayer(p))))) {
            if (victim.distanceToSqr(at) > HOUR_RADIUS * HOUR_RADIUS) {
                continue;
            }
            victim.hurtServer(level, player.damageSources().playerAttack(player), damage);
            TimewardenArmor.stall(level, victim, player);
        }
    }

    /** Zeitsprung: bis zu acht Blöcke in Blickrichtung (waagerecht), vor Wänden hält er an. */
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        Vec3 look = player.getViewVector(1.0f).multiply(1.0, 0.0, 1.0);
        if (look.lengthSqr() < 1.0e-4) {
            return InteractionResult.PASS;
        }
        if (level instanceof ServerLevel server && player instanceof ServerPlayer sp) {
            Vec3 to = jumpTarget(server, sp, look.normalize());
            if (to == null) {
                return InteractionResult.FAIL;
            }
            Vec3 from = sp.position();
            server.playSound(null, from.x, from.y, from.z, ModSounds.MINUTE_HAND_JUMP.get(), SoundSource.PLAYERS, 1.2f, 1.0f);
            server.sendParticles(TimewardenArmor.AMBER, from.x, from.y + 1.0, from.z, 30, 0.3, 0.7, 0.3, 0.0);
            server.sendParticles(ParticleTypes.REVERSE_PORTAL, from.x, from.y + 1.0, from.z, 20, 0.3, 0.7, 0.3, 0.05);
            sp.teleportTo(server, to.x, to.y, to.z, Set.of(), sp.getYRot(), sp.getXRot(), false);
            sp.resetFallDistance();
            sp.setInvulnerableTime(Math.max(sp.getInvulnerableTime(), 10));
            for (int i = 0; i < 12; i++) {
                double a = i * Mth.TWO_PI / 12.0;
                server.sendParticles(TimewardenArmor.AMBER, to.x + Math.cos(a) * 0.9, to.y + 0.1, to.z + Math.sin(a) * 0.9, 2, 0.02, 0.02, 0.02, 0.0);
            }
            stack.hurtAndBreak(1, sp, hand);
            sp.getCooldowns().addCooldown(stack, COOLDOWN);
        }
        return InteractionResult.SUCCESS;
    }

    /** Wohin der Sprung trägt: so weit es frei ist (Platz für den Spieler), oder null, wenn kaum ein Schritt möglich ist. */
    static Vec3 jumpTarget(ServerLevel level, ServerPlayer player, Vec3 dir) {
        Vec3 eye = player.getEyePosition();
        BlockHitResult hit = level.clip(new ClipContext(eye, eye.add(dir.scale(JUMP_RANGE)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        double reach = hit.getType() == HitResult.Type.MISS ? JUMP_RANGE : Math.max(0.0, hit.getLocation().distanceTo(eye) - 0.8);
        for (double d = reach; d >= 1.5; d -= 0.5) {
            Vec3 at = player.position().add(dir.scale(d));
            AABB box = player.getDimensions(player.getPose()).makeBoundingBox(at.x, at.y, at.z);
            if (level.noCollision(player, box)) {
                return at;
            }
        }
        return null;
    }
}
