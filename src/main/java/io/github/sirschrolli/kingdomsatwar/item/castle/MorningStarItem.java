package io.github.sirschrolli.kingdomsatwar.item.castle;

import io.github.sirschrolli.kingdomsatwar.enchant.EnchantPower;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Der Morgenstern (seit 1.8): die Waffe Kunos von Rabenfels, des Raubritters – ein Stiel aus Eschenholz mit Eisenbändern,
 * ein Kopf aus geschwärztem Eisen voller Stacheln. Sehr schwer (mehr Schaden als ein Netheritschwert, langsam) und, wie eine
 * Axt, ein <b>Schildbrecher</b>: Wer einen erhobenen Schild trifft, macht ihn für eine Weile unbrauchbar.
 * Rechtsklick: der <b>Schmetterschlag</b> – du schlägst den Morgenstern vor dir auf den Boden: Der Boden reißt in einem Fächer
 * bis {@link #RANGE} Blöcke weit auf, wer darin steht, nimmt {@link #DAMAGE} Schaden (mit Schärfe mehr), wird fortgeworfen
 * und ist kurz verlangsamt. Danach ruht der Morgenstern {@link #COOLDOWN} Ticks.
 */
public class MorningStarItem extends Item {
    public static final int COOLDOWN = 160;
    public static final double RANGE = 5.0;
    public static final float DAMAGE = 9.0f;
    /** Der Fächer: so weit nach jeder Seite (Grad). */
    public static final double ARC = 45.0;

    public MorningStarItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel server && player instanceof ServerPlayer sp) {
            smash(server, sp, DAMAGE + 1.5f * EnchantPower.level(stack, Enchantments.SHARPNESS));
            stack.hurtAndBreak(2, sp, hand);
            sp.getCooldowns().addCooldown(stack, COOLDOWN);
        }
        return InteractionResult.SUCCESS;
    }

    /** Der Schmetterschlag: ein Fächer vor dem Spieler. Gibt zurück, wie viele er traf. */
    public static int smash(ServerLevel level, ServerPlayer player, float damage) {
        Vec3 dir = player.getViewVector(1.0f).multiply(1.0, 0.0, 1.0);
        dir = dir.lengthSqr() < 1.0e-4 ? Vec3.directionFromRotation(0.0f, player.getYRot()) : dir.normalize();
        level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.MORNING_STAR_SMASH.get(), SoundSource.PLAYERS, 1.6f,
                0.9f + level.getRandom().nextFloat() * 0.15f);
        // der Boden reißt auf: Brocken und Staub in einem Fächer
        for (int i = 1; i <= 5; i++) {
            for (int k = -i; k <= i; k += Math.max(1, i)) {
                Vec3 at = player.position().add(dir.yRot((float) Math.toRadians(k * ARC / Math.max(1, i))).scale(i * RANGE / 5.0));
                BlockPos below = BlockPos.containing(at.x, at.y - 0.5, at.z);
                BlockState ground = level.getBlockState(below);
                if (!ground.isAir()) {
                    level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground), at.x, at.y + 0.1, at.z, 8, 0.3, 0.05, 0.3, 0.15);
                }
            }
        }
        level.sendParticles(ParticleTypes.CRIT, player.getX() + dir.x * 1.2, player.getY() + 0.2, player.getZ() + dir.z * 1.2, 12, 0.3, 0.1, 0.3, 0.2);
        Set<UUID> hit = new HashSet<>();
        double cos = Math.cos(Math.toRadians(ARC));
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(RANGE + 1.0, 2.0, RANGE + 1.0),
                e -> canHit(player, e))) {
            Vec3 to = e.position().subtract(player.position()).multiply(1.0, 0.0, 1.0);
            double d = to.length();
            if (d > RANGE + e.getBbWidth() * 0.5 || d > 0.8 && to.normalize().dot(dir) < cos || Math.abs(e.getY() - player.getY()) > 2.5
                    || !hit.add(e.getUUID())) {
                continue;
            }
            if (e.hurtServer(level, player.damageSources().playerAttack(player), damage)) {
                Vec3 out = d < 1.0e-3 ? dir : to.normalize();
                e.push(out.x * 0.9, 0.45, out.z * 0.9);
                e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 1), player);
                e.needsSync = true;
            }
        }
        return hit.size();
    }

    private static boolean canHit(Player player, Entity e) {
        return e != player && e.isAlive() && !(e instanceof ArmorStand) && !(e instanceof OwnableEntity pet && player.equals(pet.getOwner()))
                && !(e instanceof Player p && (p.isCreative() || p.isSpectator() || !player.canHarmPlayer(p)));
    }
}
