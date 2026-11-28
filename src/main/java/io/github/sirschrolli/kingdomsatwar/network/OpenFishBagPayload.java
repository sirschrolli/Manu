package io.github.sirschrolli.kingdomsatwar.network;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Server an Spiel: Das Fenster des Fischbeutels öffnet sich – für den Beutel in der Haupthand oder in der anderen. */
public record OpenFishBagPayload(boolean mainHand) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenFishBagPayload> TYPE = new CustomPacketPayload.Type<>(KingdomsAtWar.id("open_fish_bag"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenFishBagPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, OpenFishBagPayload::mainHand, OpenFishBagPayload::new);

    @Override
    public CustomPacketPayload.Type<OpenFishBagPayload> type() {
        return TYPE;
    }
}
