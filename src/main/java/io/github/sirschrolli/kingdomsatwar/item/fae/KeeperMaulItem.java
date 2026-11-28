package io.github.sirschrolli.kingdomsatwar.item.fae;

import io.github.sirschrolli.kingdomsatwar.enchant.EnchantPower;
import io.github.sirschrolli.kingdomsatwar.entity.fae.CrystalLine;
import io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Der Hüterhammer (seit 0.96): die Waffe des Steinhüters – ein Kopf aus Feenstein, mit Mondstein gebunden, an einem Stiel aus
 * versteinertem Glimmerholz. Schwer und langsam, doch jeder Schlag sitzt. Rechtsklick (auf festem Boden): die
 * <b>Kristallspur</b> – sieben Mondsteinkristalle brechen nacheinander vor dir aus dem Boden, schleudern Gegner hoch und
 * verlangsamen sie ({@link #CRYSTAL_DAMAGE} Schaden, mit Schärfe mehr); danach ruht er {@link #COOLDOWN} Ticks.
 */
public class KeeperMaulItem extends Item {
    public static final int COOLDOWN = 120;
    public static final int CRYSTALS = 7;
    public static final float CRYSTAL_DAMAGE = 7.0f;

    public KeeperMaulItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.onGround()) {
            return InteractionResult.PASS;
        }
        if (level instanceof ServerLevel server) {
            Vec3 dir = player.getLookAngle().multiply(1.0, 0.0, 1.0);
            dir = dir.lengthSqr() < 1.0e-4 ? Vec3.directionFromRotation(0.0f, player.getYRot()) : dir.normalize();
            Vec3 start = player.position().add(dir.scale(1.7));
            float damage = CRYSTAL_DAMAGE + 0.75f * EnchantPower.level(stack, Enchantments.SHARPNESS);
            CrystalLine.start(server, new Vec3(start.x, player.getY(), start.z), dir, CRYSTALS, 1.5, 2, damage, 0.62f, player,
                    victim -> canHit(player, victim));
            server.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.STONE_KEEPER_SMASH.get(), SoundSource.PLAYERS, 1.2f, 1.3f);
            server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, FaeBlocks.MOONSTONE_BLOCK.get().defaultBlockState()), start.x,
                    player.getY() + 0.2, start.z, 20, 0.5, 0.1, 0.5, 0.15);
            stack.hurtAndBreak(1, player, hand);
            player.getCooldowns().addCooldown(stack, COOLDOWN);
        }
        return InteractionResult.SUCCESS;
    }

    /** Die Kristalle verschonen den Träger, seine Tiere und Rüstungsständer. */
    private static boolean canHit(Player owner, LivingEntity victim) {
        if (victim == owner || victim instanceof ArmorStand) {
            return false;
        }
        return !(victim instanceof OwnableEntity pet && owner.equals(pet.getOwner()));
    }
}
