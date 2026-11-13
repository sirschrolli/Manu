package io.github.sirschrolli.kingdomsatwar.item.armory;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Adernbrecher: eine lange Nachtstahl-Picke mit violetter Ader und einem Erzkristall in der Fassung. Trifft sie ein Erz, bricht
 * die <b>ganze Ader</b> – alle zusammenhängenden Blöcke derselben Erzsorte (auch über Ecken, bis {@link ArmoryMining#MAX_VEIN}
 * weitere). Schleichend nur den einen Block.
 */
public class VeinBreakerItem extends Item {
    public VeinBreakerItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity owner) {
        boolean result = super.mineBlock(stack, level, state, pos, owner);
        if (level instanceof ServerLevel server && owner instanceof ServerPlayer player && !player.isShiftKeyDown()
                && !ArmoryMining.isBreaking(player) && ArmoryMining.isOre(state) && stack.isCorrectToolForDrops(state)) {
            breakVein(server, player, stack, pos, state);
        }
        return result;
    }

    /** Die Ader um {@code pos} brechen (ohne den Block selbst). Gibt die Zahl der Blöcke zurück. */
    public static int breakVein(ServerLevel level, ServerPlayer player, ItemStack stack, BlockPos pos, BlockState state) {
        List<BlockPos> vein = ArmoryMining.vein(level, pos, state, stack);
        if (vein.isEmpty()) {
            return 0;
        }
        for (BlockPos p : vein) {
            level.sendParticles(ParticleTypes.WITCH, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, 3, 0.3, 0.3, 0.3, 0.01);
        }
        int broken = ArmoryMining.breakAs(player, vein);
        level.playSound(null, pos, SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.PLAYERS, 0.8f, 0.7f + Math.min(broken, 20) * 0.03f);
        return broken;
    }
}
