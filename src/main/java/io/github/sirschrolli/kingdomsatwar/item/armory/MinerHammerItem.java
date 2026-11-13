package io.github.sirschrolli.kingdomsatwar.item.armory;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Bergmannshammer: ein schwerer Stahlblock mit Messingbändern. Er baut <b>drei mal drei</b> Blöcke ab – die Fläche quer zur
 * Seite, auf die man schlägt (Tunnel, Böden, Decken). Schleichend nur den einen Block. Mitgenommen wird nur, was die
 * Spitzhacke abbauen darf und nicht viel härter ist als der Block in der Mitte (also kein Obsidian neben Stein).
 */
public class MinerHammerItem extends Item {
    public MinerHammerItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity owner) {
        boolean result = super.mineBlock(stack, level, state, pos, owner);
        if (level instanceof ServerLevel server && owner instanceof ServerPlayer player && !player.isShiftKeyDown()
                && !ArmoryMining.isBreaking(player) && stack.isCorrectToolForDrops(state)) {
            breakPlane(server, player, stack, pos, state);
        }
        return result;
    }

    /** Die Fläche um {@code pos} brechen (ohne die Mitte, die bricht Minecraft selbst). Gibt die Zahl der Blöcke zurück. */
    public static int breakPlane(ServerLevel level, ServerPlayer player, ItemStack stack, BlockPos pos, BlockState state) {
        Direction face = ArmoryMining.face(player, pos);
        float hardness = state.getDestroySpeed(level, pos);
        List<BlockPos> plane = ArmoryMining.plane(pos, face).stream()
                .filter(p -> ArmoryMining.fitsHammer(level, stack, p, hardness)).toList();
        return ArmoryMining.breakAs(player, plane);
    }
}
