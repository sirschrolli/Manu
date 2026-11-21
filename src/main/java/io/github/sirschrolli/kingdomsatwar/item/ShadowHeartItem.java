package io.github.sirschrolli.kingdomsatwar.item;

import io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks;
import io.github.sirschrolli.kingdomsatwar.voidrealm.VoidGate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * Herz der Schatten: was von Nyxar bleibt – ein Kristallherz, in dem noch eine Seele glimmt. Auf den Boden gerichtet
 * reißt es die Welt auf: Vor einem öffnet sich ein Riss in die <b>Leere</b>. Auf einen Riss gerichtet schließt es ihn
 * wieder. Das Herz selbst bleibt erhalten; es verbrennt nicht und verschwindet nicht, wenn es liegen bleibt.
 */
public class ShadowHeartItem extends Item {
    private static final int COOLDOWN = 100;

    public ShadowHeartItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        BlockPos clicked = context.getClickedPos();
        if (level.getBlockState(clicked).is(VoidBlocks.VOID_RIFT.get())) {
            if (level instanceof ServerLevel server && VoidGate.tryClose(server, player, clicked)) {
                player.getCooldowns().addCooldown(context.getItemInHand(), COOLDOWN);
            }
            return InteractionResult.SUCCESS;
        }
        if (context.getClickedFace() != Direction.UP) {
            return InteractionResult.PASS;
        }
        if (level instanceof ServerLevel server && VoidGate.tryOpen(server, player, clicked)) {
            player.getCooldowns().addCooldown(context.getItemInHand(), COOLDOWN);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public int getEntityLifespan(ItemStack stack, Level level) {
        return Integer.MAX_VALUE;
    }
}
