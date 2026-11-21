package io.github.sirschrolli.kingdomsatwar.item.deep;

import io.github.sirschrolli.kingdomsatwar.enchant.EnchantPower;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Die Stimmgabel (seit 1.7): die Waffe Sordins, des Kantors der Stille – ein Griff aus Kupfer, in seegrünes Leder gewickelt,
 * und zwei Zinken aus Hallkristall. Schwer wie ein Streitkolben (etwa so viel Schaden wie ein Netheritschwert, langsamer).
 * Rechtsklick: die <b>Resonanz</b> – du schlägst die Gabel an, und ein Schallschlag fährt in Blickrichtung bis zu
 * {@link #RANGE} Blöcke weit, durch Stein und Rüstung (wie der des Tiefenhüters); jeden, den er durchquert, trifft er
 * ({@link #DAMAGE} Schaden, mit Schärfe mehr). Nur Wolle hält ihn auf. Danach ruht die Gabel {@link #COOLDOWN} Ticks.
 */
public class TuningForkItem extends Item {
    public static final int COOLDOWN = 120;
    public static final double RANGE = 16.0;
    public static final float DAMAGE = 8.0f;

    public TuningForkItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel server && player instanceof ServerPlayer sp) {
            resonate(server, sp, DAMAGE + 1.5f * EnchantPower.level(stack, Enchantments.SHARPNESS));
            stack.hurtAndBreak(2, sp, hand);
            sp.getCooldowns().addCooldown(stack, COOLDOWN);
        }
        return InteractionResult.SUCCESS;
    }

    /** Die Resonanz: ein Schallschlag aus den Augen des Spielers in Blickrichtung. Gibt zurück, wie viele er traf. */
    public static int resonate(ServerLevel level, ServerPlayer player, float damage) {
        Vec3 from = player.getEyePosition();
        Vec3 dir = player.getViewVector(1.0f).normalize();
        double reach = RANGE;
        for (double d = 0.5; d < RANGE; d += 0.5) {
            BlockPos p = BlockPos.containing(from.add(dir.scale(d)));
            BlockState state = level.getBlockState(p);
            if (state.is(BlockTags.WOOL) || state.is(BlockTags.OCCLUDES_VIBRATION_SIGNALS)) {
                reach = d;
                break;
            }
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.TUNING_FORK_RESONATE.get(), SoundSource.PLAYERS, 1.6f,
                0.95f + level.getRandom().nextFloat() * 0.1f);
        for (double d = 1.5; d < reach; d += 1.5) {
            Vec3 p = from.add(dir.scale(d));
            level.sendParticles(ParticleTypes.SONIC_BOOM, p.x, p.y, p.z, 1, 0.0, 0.0, 0.0, 0.0);
        }
        Vec3 hand = from.add(dir.scale(0.8)).add(0.0, -0.3, 0.0);
        level.sendParticles(new DustParticleOptions(0x9CD8FF, 1.1f), hand.x, hand.y, hand.z, 12, 0.15, 0.15, 0.15, 0.0);
        Set<UUID> hit = new HashSet<>();
        Vec3 end = from.add(dir.scale(reach));
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, new AABB(from, end).inflate(1.5), e -> canHit(player, e))) {
            Vec3 c = e.getBoundingBox().getCenter();
            double along = c.subtract(from).dot(dir);
            if (along < 0.0 || along > reach + 0.5) {
                continue;
            }
            double off = c.subtract(from.add(dir.scale(along))).length();
            if (off > 0.9 + e.getBbWidth() * 0.5 + e.getBbHeight() * 0.25 || !hit.add(e.getUUID())) {
                continue;
            }
            if (e.hurtServer(level, player.damageSources().sonicBoom(player), damage)) {
                e.push(dir.x * 0.8, 0.25, dir.z * 0.8);
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
