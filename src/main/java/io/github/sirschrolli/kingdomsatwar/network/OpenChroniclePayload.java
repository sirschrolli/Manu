package io.github.sirschrolli.kingdomsatwar.network;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.story.ChronicleData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Server an Spiel: die Chronik des Siegels aufschlagen (mit allem, was sie zeigen soll). */
public record OpenChroniclePayload(ChronicleData data) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenChroniclePayload> TYPE = new CustomPacketPayload.Type<>(KingdomsAtWar.id("open_chronicle"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenChroniclePayload> STREAM_CODEC =
            ChronicleData.STREAM_CODEC.map(OpenChroniclePayload::new, OpenChroniclePayload::data);

    @Override
    public CustomPacketPayload.Type<OpenChroniclePayload> type() {
        return TYPE;
    }
}
