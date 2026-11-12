package io.github.sirschrolli.kingdomsatwar.urmeer;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import org.jspecify.annotations.Nullable;

/**
 * Im Urmeer gibt es keine Lava (seit 0.99): Wer dort einen Eimer Lava ausgießen will – mit der Hand oder aus einem Werfer –,
 * dem erlischt sie zischend in einer Dampfwolke, und der Eimer bleibt voll (wie Wasser im Nether, nur ohne Verlust). Auch
 * ein Kessel lässt sich dort nicht mit Lava füllen. Das Ausgießen fängt {@code mixin.BucketItemMixin} ab.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class UrmeerLava {
    private UrmeerLava() {}

    /** Ist Lava in dieser Welt verboten? */
    public static boolean forbidden(Level level) {
        return forbidden(level.dimension());
    }

    public static boolean forbidden(net.minecraft.resources.ResourceKey<Level> dimension) {
        return dimension == UrmeerDimension.LEVEL;
    }

    /** Enthält dieser Gegenstand Lava (Eimer von Minecraft oder einer anderen Mod)? */
    public static boolean holdsLava(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        var fluid = FluidUtil.getFirstStackContained(stack);
        return !fluid.isEmpty() && fluid.getFluid().is(FluidTags.LAVA);
    }

    /** Die Lava erlischt: Zischen, Dampf und Bläschen, und ein Hinweis an den, der es versucht hat. */
    public static void quench(@Nullable LivingEntity user, Level level, BlockPos pos) {
        RandomSource random = level.getRandom();
        level.playSound(user, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.7f, 2.0f + (random.nextFloat() - random.nextFloat()) * 0.6f);
        level.playSound(user, pos, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.6f, 0.8f + random.nextFloat() * 0.3f);
        if (level instanceof ServerLevel server) {
            double x = pos.getX() + 0.5;
            double y = pos.getY() + 0.5;
            double z = pos.getZ() + 0.5;
            server.sendParticles(ParticleTypes.LARGE_SMOKE, x, y, z, 10, 0.4, 0.4, 0.4, 0.02);
            server.sendParticles(ParticleTypes.CLOUD, x, y + 0.3, z, 6, 0.3, 0.3, 0.3, 0.02);
            server.sendParticles(ParticleTypes.BUBBLE_COLUMN_UP, x, y, z, 12, 0.35, 0.35, 0.35, 0.05);
            if (user instanceof ServerPlayer player) {
                player.sendOverlayMessage(Component.translatable("message.kingdomsatwar.urmeer_no_lava").withStyle(ChatFormatting.AQUA));
            }
        }
    }

    /** Kein Lavakessel im Urmeer. */
    @SubscribeEvent
    static void onUseOnBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (!forbidden(level) || !holdsLava(event.getItemStack()) || !level.getBlockState(event.getPos()).is(BlockTags.CAULDRONS)) {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.FAIL);
        quench(event.getEntity(), level, event.getPos());
    }
}
