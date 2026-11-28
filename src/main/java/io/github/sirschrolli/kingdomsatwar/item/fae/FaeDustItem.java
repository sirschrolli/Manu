package io.github.sirschrolli.kingdomsatwar.item.fae;

import io.github.sirschrolli.kingdomsatwar.fae.FaeGate;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

/**
 * Feenstaub: schimmernder Staub aus Amethyst, Leuchtbeeren und Blüten. Auf einen Rahmen aus Mondsteinziegeln gestreut,
 * öffnet er das Tor ins Feenreich.
 */
public class FaeDustItem extends Item {
    public FaeDustItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!FaeGate.isFrame(context.getLevel().getBlockState(context.getClickedPos()))) {
            return InteractionResult.PASS;
        }
        if (!(context.getLevel() instanceof ServerLevel level)) {
            return InteractionResult.SUCCESS;
        }
        if (FaeGate.tryLight(level, context.getClickedPos(), context.getClickedFace())) {
            if (context.getPlayer() == null || !context.getPlayer().getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }
            return InteractionResult.SUCCESS_SERVER;
        }
        if (context.getPlayer() != null) {
            context.getPlayer().sendOverlayMessage(Component.translatable("item.kingdomsatwar.fae_dust.no_frame"));
        }
        return InteractionResult.FAIL;
    }
}
