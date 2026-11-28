package io.github.sirschrolli.kingdomsatwar.item.sea;

import io.github.sirschrolli.kingdomsatwar.enchant.EnchantPower;
import io.github.sirschrolli.kingdomsatwar.entity.sea.InkBoltEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Der Hexenstab (seit 0.97): die Beute der Meerhexe – Treibholz, oben drei Zinken aus schwarzer Koralle um eine leuchtende
 * Kugel. Im Nahkampf schlägt er wie ein Schwert. Rechtsklick: drei Kugeln aus Hexentinte, die dem anvisierten Ziel ein wenig
 * folgen ({@link #INK_DAMAGE} Schaden, mit Schärfe mehr, dazu kurz Blindheit); danach ruht er {@link #COOLDOWN} Ticks – unter
 * Wasser, wo die Tinte zu Hause ist, nur {@link #COOLDOWN_WATER}.
 */
public class SeaWitchStaffItem extends Item {
    public static final int COOLDOWN = 50;
    public static final int COOLDOWN_WATER = 30;
    public static final int BOLTS = 3;
    public static final float INK_DAMAGE = 6.0f;
    /** So weit sucht der Stab nach einem Ziel, dem die Tinte folgt. */
    private static final double AIM_RANGE = 32.0;

    public SeaWitchStaffItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel server) {
            Vec3 look = player.getViewVector(1.0f);
            Vec3 from = player.getEyePosition().add(look.scale(0.8)).add(0.0, -0.15, 0.0);
            Entity target = aimed(player);
            float damage = INK_DAMAGE + 0.5f * EnchantPower.level(stack, Enchantments.SHARPNESS);
            for (int i = 0; i < BOLTS; i++) {
                double spread = 0.12;
                Vec3 dir = look.add((player.getRandom().nextDouble() - 0.5) * spread * (i == 0 ? 0.0 : 1.0),
                        (player.getRandom().nextDouble() - 0.5) * spread * 0.5 * (i == 0 ? 0.0 : 1.0),
                        (player.getRandom().nextDouble() - 0.5) * spread * (i == 0 ? 0.0 : 1.0)).normalize();
                InkBoltEntity bolt = new InkBoltEntity(server, player, dir, target, damage, 0.7 + i * 0.04).blindness(30);
                bolt.setPos(from.x, from.y, from.z);
                server.addFreshEntity(bolt);
            }
            server.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.SEA_WITCH_INK.get(), SoundSource.PLAYERS, 1.0f, 1.2f);
            server.sendParticles(ParticleTypes.GLOW_SQUID_INK, from.x, from.y, from.z, 6, 0.2, 0.2, 0.2, 0.03);
            stack.hurtAndBreak(1, player, hand);
            player.getCooldowns().addCooldown(stack, player.isUnderWater() ? COOLDOWN_WATER : COOLDOWN);
        }
        return InteractionResult.SUCCESS;
    }

    /** Das Wesen, auf das der Spieler zielt (die Tinte folgt ihm), oder null. */
    private static @Nullable Entity aimed(Player player) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0f);
        Vec3 end = eye.add(look.scale(AIM_RANGE));
        AABB box = player.getBoundingBox().expandTowards(look.scale(AIM_RANGE)).inflate(1.5);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(player, eye, end, box,
                e -> e instanceof LivingEntity living && living.isAlive() && e != player && !e.isSpectator(), AIM_RANGE * AIM_RANGE);
        return hit != null ? hit.getEntity() : null;
    }
}
