package io.github.sirschrolli.kingdomsatwar.item;

import io.github.sirschrolli.kingdomsatwar.story.Story;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/**
 * Die Chronik des Siegels: erzählt die Geschichte Kapitel für Kapitel, nennt die Ziele und gibt Hinweise, wo der
 * Siegelkreis und die sechs Drachennester liegen. Rechtsklick schlägt sie auf.
 */
public class ChronicleItem extends Item {
    public ChronicleItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer serverPlayer) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0f, 0.9f);
            Story.openChronicle(serverPlayer);
        }
        return InteractionResult.SUCCESS;
    }
}
