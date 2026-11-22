package io.github.sirschrolli.kingdomsatwar.network;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Server an Spiel: Das Fenster des Wegkompasses öffnet sich – für den Kompass in der Haupthand oder in der anderen. */
public record OpenWaystoneCompassPayload(boolean mainHand) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenWaystoneCompassPayload> TYPE =
            new CustomPacketPayload.Type<>(KingdomsAtWar.id("open_waystone_compass"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenWaystoneCompassPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, OpenWaystoneCompassPayload::mainHand, OpenWaystoneCompassPayload::new);

    @Override
    public CustomPacketPayload.Type<OpenWaystoneCompassPayload> type() {
        return TYPE;
    }
}
