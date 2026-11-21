package io.github.sirschrolli.kingdomsatwar.network;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Server an Spiel: Der Drill ist vorbei, ohne Fang (der Haken ist fort, die Schnur gerissen, die Zeit um). */
public record FishingEndPayload(int session) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<FishingEndPayload> TYPE = new CustomPacketPayload.Type<>(KingdomsAtWar.id("fishing_end"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FishingEndPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, FishingEndPayload::session, FishingEndPayload::new);

    @Override
    public CustomPacketPayload.Type<FishingEndPayload> type() {
        return TYPE;
    }
}
