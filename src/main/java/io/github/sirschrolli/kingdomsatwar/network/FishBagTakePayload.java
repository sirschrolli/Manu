package io.github.sirschrolli.kingdomsatwar.network;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.fishing.FishBag;
import io.github.sirschrolli.kingdomsatwar.fishing.FishSpecies;
import io.github.sirschrolli.kingdomsatwar.fishing.Fishing;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Spiel an Server: Der Spieler nimmt Fische aus dem Fischbeutel in seiner Hand. {@code species}: 0 = alles, sonst die Art (ihre
 * Stelle in der Liste aller Arten plus eins); {@code mode}: ein Stapel, alle dieser Art oder ein einzelner
 * ({@link FishBag#TAKE_STACK} …), 3 = stattdessen das Fischbuch aufschlagen. Der Server prüft alles selbst.
 */
public record FishBagTakePayload(boolean mainHand, int species, int mode) implements CustomPacketPayload {
    public static final int OPEN_BOOK = 3;
    public static final CustomPacketPayload.Type<FishBagTakePayload> TYPE = new CustomPacketPayload.Type<>(KingdomsAtWar.id("fish_bag_take"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FishBagTakePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, FishBagTakePayload::mainHand, ByteBufCodecs.VAR_INT, FishBagTakePayload::species, ByteBufCodecs.VAR_INT,
            FishBagTakePayload::mode, FishBagTakePayload::new);

    @Override
    public CustomPacketPayload.Type<FishBagTakePayload> type() {
        return TYPE;
    }

    static void handle(FishBagTakePayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }
        if (payload.mode() == OPEN_BOOK) {
            Fishing.openBook(player);
            return;
        }
        int index = payload.species() - 1;
        FishSpecies species = index >= 0 && index < FishSpecies.count() ? FishSpecies.byIndex(index) : null;
        if (payload.species() == 0 || species != null) {
            FishBag.take(player, payload.mainHand() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND, species,
                    Math.max(0, Math.min(FishBag.TAKE_ONE, payload.mode())));
        }
    }
}
