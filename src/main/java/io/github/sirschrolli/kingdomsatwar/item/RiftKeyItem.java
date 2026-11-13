package io.github.sirschrolli.kingdomsatwar.item;

import io.github.sirschrolli.kingdomsatwar.abyss.AbyssGate;
import io.github.sirschrolli.kingdomsatwar.registry.AbyssBlocks;
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
 * Rissschlüssel: aus den Splittern der zerbrochenen Krone geschmiedet. In der Leere auf den Boden gerichtet reißt er die
 * Welt ein zweites Mal auf – vor einem öffnet sich ein Riss in den <b>Abgrund</b>. Auf einen solchen Riss gerichtet schließt
 * er ihn wieder (den Riss hinter dem Thron nicht). Er verbrennt nicht und verschwindet nicht, wenn er liegen bleibt.
 */
public class RiftKeyItem extends Item {
    private static final int COOLDOWN = 120;

    public RiftKeyItem(Item.Properties properties) {
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
        if (level.getBlockState(clicked).is(AbyssBlocks.ABYSS_RIFT.get())) {
            if (level instanceof ServerLevel server && AbyssGate.tryClose(server, player, clicked)) {
                player.getCooldowns().addCooldown(context.getItemInHand(), COOLDOWN);
            }
            return InteractionResult.SUCCESS;
        }
        if (context.getClickedFace() != Direction.UP) {
            return InteractionResult.PASS;
        }
        if (level instanceof ServerLevel server && AbyssGate.tryOpen(server, player, clicked)) {
            player.getCooldowns().addCooldown(context.getItemInHand(), COOLDOWN);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public int getEntityLifespan(ItemStack stack, Level level) {
        return Integer.MAX_VALUE;
    }
}
