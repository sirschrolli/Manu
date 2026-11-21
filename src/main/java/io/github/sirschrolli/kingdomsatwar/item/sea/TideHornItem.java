package io.github.sirschrolli.kingdomsatwar.item.sea;

import io.github.sirschrolli.kingdomsatwar.block.sea.AtollCoreBlockEntity;
import io.github.sirschrolli.kingdomsatwar.worldgen.atoll.AtollLocator;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Das Gezeitenhorn: eine große Wellhornschnecke mit goldenem Mundstück. Wer hineinbläst, hört das Meer antworten – aus
 * der Richtung des Versunkenen Atolls (Richtung und Entfernung), im Urmeer aus der Richtung des Krakenschlunds (seit 0.91).
 * In der Lagune eines Atolls, dessen Riffkoloss besiegt ist, ruft es ihn zurück, im Felsenring eines besiegten Kraken diesen
 * – dafür gibt man dem Meer ein Herz des Meeres (in der anderen Hand).
 */
public class TideHornItem extends Item {
    public TideHornItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!(level instanceof ServerLevel server)) {
            return InteractionResult.SUCCESS;
        }
        player.getCooldowns().addCooldown(player.getItemInHand(hand), 60);
        server.playSound(null, player.blockPosition(), SoundEvents.GOAT_HORN_SOUND_VARIANTS.get(3).value(), SoundSource.PLAYERS, 3.0f, 0.55f);
        server.playSound(null, player.blockPosition(), SoundEvents.CONDUIT_AMBIENT_SHORT, SoundSource.PLAYERS, 1.5f, 0.6f);
        // im Urmeer antwortet das Meer aus dem Krakenschlund (seit 0.91)
        BlockPos maw = io.github.sirschrolli.kingdomsatwar.worldgen.kraken.KrakenMawLocator.center(server);
        if (maw != null) {
            return this.kraken(server, player, hand, maw);
        }
        BlockPos atoll = AtollLocator.center(server);
        if (atoll == null) {
            player.sendOverlayMessage(Component.translatable("item.kingdomsatwar.tide_horn.none"));
            return InteractionResult.SUCCESS;
        }
        double dx = atoll.getX() + 0.5 - player.getX();
        double dz = atoll.getZ() + 0.5 - player.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        if (dist < 20.0 && server.getBlockEntity(AtollLocator.core(atoll)) instanceof AtollCoreBlockEntity core
                && core.phase() == AtollCoreBlockEntity.Phase.DEFEATED) {
            InteractionHand other = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            ItemStack offering = player.getItemInHand(other);
            if (!offering.is(Items.HEART_OF_THE_SEA)) {
                player.sendOverlayMessage(Component.translatable("item.kingdomsatwar.tide_horn.offering"));
                return InteractionResult.SUCCESS;
            }
            if (core.recall(server)) {
                offering.consume(1, player);
                player.sendOverlayMessage(Component.translatable("item.kingdomsatwar.tide_horn.recalled").withColor(0x7AE8FF));
            }
            return InteractionResult.SUCCESS;
        }
        direction(player, dx, dz, dist);
        return InteractionResult.SUCCESS;
    }

    /** Richtung wie eine Himmelsrichtung, dazu die Entfernung. */
    private static void direction(Player player, double dx, double dz, double dist) {
        float yaw = (float) (Mth.atan2(-dx, dz) * Mth.RAD_TO_DEG);
        String[] dirs = {"south", "southwest", "west", "northwest", "north", "northeast", "east", "southeast"};
        String dir = dirs[Math.floorMod(Math.round(yaw / 45.0f), 8)];
        player.sendOverlayMessage(Component.translatable("item.kingdomsatwar.tide_horn.direction",
                Component.translatable("direction.kingdomsatwar." + dir), (int) dist).withColor(0x7AE8FF));
    }

    /** Im Urmeer: zeigt den Weg zum Krakenschlund; über dem Schlund eines besiegten Kraken ruft es ihn zurück. */
    private InteractionResult kraken(ServerLevel server, Player player, InteractionHand hand, BlockPos maw) {
        double dx = maw.getX() + 0.5 - player.getX();
        double dz = maw.getZ() + 0.5 - player.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        BlockPos corePos = io.github.sirschrolli.kingdomsatwar.worldgen.kraken.KrakenMawLocator.core(maw);
        if (dist < io.github.sirschrolli.kingdomsatwar.block.sea.KrakenCoreBlockEntity.WAKE_RADIUS
                && server.getBlockEntity(corePos) instanceof io.github.sirschrolli.kingdomsatwar.block.sea.KrakenCoreBlockEntity core
                && core.phase() == io.github.sirschrolli.kingdomsatwar.block.sea.KrakenCoreBlockEntity.Phase.DEFEATED) {
            InteractionHand other = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            ItemStack offering = player.getItemInHand(other);
            if (!offering.is(Items.HEART_OF_THE_SEA)) {
                player.sendOverlayMessage(Component.translatable("item.kingdomsatwar.tide_horn.offering"));
                return InteractionResult.SUCCESS;
            }
            if (core.recall(server)) {
                offering.consume(1, player);
                player.sendOverlayMessage(Component.translatable("item.kingdomsatwar.tide_horn.kraken_recalled").withColor(0x5AD8E8));
            }
            return InteractionResult.SUCCESS;
        }
        direction(player, dx, dz, dist);
        return InteractionResult.SUCCESS;
    }
}
