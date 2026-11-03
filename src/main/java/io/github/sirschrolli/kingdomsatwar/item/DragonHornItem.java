package io.github.sirschrolli.kingdomsatwar.item;

import io.github.sirschrolli.kingdomsatwar.entity.dragon.DragonWar;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/**
 * Das Drachenhorn: ruft die eigenen Drachen in den Krieg (siehe {@link DragonWar}) – auf einen Gegner zeigen = Angriff,
 * auf einen Ort zeigen = Luftangriff, in den Himmel zeigen oder schleichend blasen = Rückkehr.
 */
public class DragonHornItem extends Item {
    /** Kurze Pause zwischen zwei Hornstößen. */
    private static final int COOLDOWN = 30;

    public DragonHornItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            DragonWar.horn(serverLevel, serverPlayer);
            player.getCooldowns().addCooldown(player.getItemInHand(hand), COOLDOWN);
        }
        return InteractionResult.SUCCESS;
    }
}
