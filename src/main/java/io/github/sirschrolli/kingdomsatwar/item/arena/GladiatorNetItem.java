package io.github.sirschrolli.kingdomsatwar.item.arena;

import io.github.sirschrolli.kingdomsatwar.entity.arena.GladiatorNetEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Das Wurfnetz eines Gladiators (seit 1.9, aus Ventors Beute): Rechtsklick wirft es im Bogen. Trifft es ein Wesen, legt es
 * sich um dessen Füße – es steckt eine Weile fest und ist langsam; geht es daneben, bleibt es als Falle liegen.
 */
public class GladiatorNetItem extends Item {
    public static final int COOLDOWN = 30;

    public GladiatorNetItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.VENTOR_NET.get(), SoundSource.PLAYERS, 0.8f, 1.3f);
        if (level instanceof ServerLevel server) {
            GladiatorNetEntity net = new GladiatorNetEntity(server, player);
            net.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0f, 1.2f, 1.0f);
            server.addFreshEntity(net);
        }
        player.getCooldowns().addCooldown(stack, COOLDOWN);
        stack.consume(1, player);
        return InteractionResult.SUCCESS;
    }
}
