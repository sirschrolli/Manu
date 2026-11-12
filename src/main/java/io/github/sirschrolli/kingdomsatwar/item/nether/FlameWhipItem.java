package io.github.sirschrolli.kingdomsatwar.item.nether;

import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.enchant.EnchantPower;
import io.github.sirschrolli.kingdomsatwar.magic.fx.SpellFxEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.Optional;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
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
import org.jspecify.annotations.Nullable;

/**
 * Die Flammenpeitsche (seit 1.2): die Waffe Ignaras – ein Griff aus Schwarzgold mit einem Feueropal als Knauf, die Schnur
 * aus glühenden Gliedern. Sie reicht weiter als ein Schwert (zwei Blöcke mehr), und jeder Treffer setzt in Brand.
 * Rechtsklick: der <b>Peitschenschlag</b> – die Schnur schnellt bis zu {@link #LASH_RANGE} Blöcke in Blickrichtung; wen sie
 * zuerst trifft, den versengt sie ({@link #LASH_DAMAGE} Schaden, mit Schärfe mehr) und reißt ihn zu einem heran. Danach ruht
 * sie {@link #COOLDOWN} Ticks.
 */
public class FlameWhipItem extends Item {
    public static final int COOLDOWN = 30;
    public static final double LASH_RANGE = 10.0;
    public static final float LASH_DAMAGE = 9.0f;

    public FlameWhipItem(Properties properties) {
        super(properties);
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.hurtEnemy(stack, target, attacker);
        target.igniteForSeconds(4.0f);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel server && player instanceof ServerPlayer sp) {
            lash(server, sp, LASH_DAMAGE + 1.5f * EnchantPower.level(stack, Enchantments.SHARPNESS));
            stack.hurtAndBreak(1, sp, hand);
            sp.getCooldowns().addCooldown(stack, COOLDOWN);
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * Der Peitschenschlag: Die Schnur schnellt in Blickrichtung (bis zur nächsten Wand); das erste Wesen auf ihrem Weg brennt,
     * nimmt Schaden und wird herangerissen. Gibt das getroffene Wesen zurück (oder null).
     */
    public static @Nullable LivingEntity lash(ServerLevel level, ServerPlayer player, float damage) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0f);
        Vec3 far = eye.add(look.scale(LASH_RANGE));
        BlockHitResult wall = level.clip(new ClipContext(eye, far, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        Vec3 end = wall.getType() == HitResult.Type.MISS ? far : wall.getLocation();
        LivingEntity hit = null;
        double best = Double.MAX_VALUE;
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, new AABB(eye, end).inflate(1.0), e -> canLash(player, e))) {
            Optional<Vec3> through = e.getBoundingBox().inflate(0.4).clip(eye, end);
            if (through.isPresent() && through.get().distanceToSqr(eye) < best) {
                best = through.get().distanceToSqr(eye);
                hit = e;
            }
        }
        Vec3 tip = hit != null ? hit.position().add(0.0, hit.getBbHeight() * 0.55, 0.0) : end;
        Vec3 hand = player.position().add(0.0, player.getBbHeight() * 0.62, 0.0).add(look.multiply(1.0, 0.0, 1.0).normalize().scale(0.4));
        SpellFxEntity.create(level, SpellFxEntity.Kind.BEAM, hand, DragonType.FIRE, 0xFF7A1E, 6, 0.16f).end(tip).spawn();
        double length = tip.distanceTo(hand);
        for (double d = 0.6; d < length; d += 0.5) {
            Vec3 p = hand.add(tip.subtract(hand).scale(d / length));
            level.sendParticles(ParticleTypes.FLAME, p.x, p.y, p.z, 1, 0.03, 0.03, 0.03, 0.0);
        }
        level.sendParticles(ParticleTypes.LAVA, tip.x, tip.y, tip.z, 2, 0.1, 0.1, 0.1, 0.0);
        level.playSound(null, tip.x, tip.y, tip.z, ModSounds.FLAME_WHIP_CRACK.get(), SoundSource.PLAYERS, 1.6f, 0.95f + level.getRandom().nextFloat() * 0.1f);
        if (hit == null) {
            return null;
        }
        if (hit.hurtServer(level, player.damageSources().playerAttack(player), damage)) {
            hit.igniteForSeconds(5.0f);
            Vec3 pull = player.position().subtract(hit.position()).multiply(1.0, 0.0, 1.0);
            double d = pull.length();
            if (d > 2.0) {
                pull = pull.scale(Math.min(1.1, 0.25 + d * 0.07) / d);
                hit.push(pull.x, 0.32, pull.z);
                hit.needsSync = true;
            }
        }
        return hit;
    }

    private static boolean canLash(Player player, Entity e) {
        return e != player && e.isAlive() && !(e instanceof ArmorStand) && !(e instanceof OwnableEntity pet && player.equals(pet.getOwner()))
                && !(e instanceof Player p && (p.isCreative() || p.isSpectator() || !player.canHarmPlayer(p)));
    }
}
