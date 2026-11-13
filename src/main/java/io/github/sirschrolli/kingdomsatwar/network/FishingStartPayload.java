package io.github.sirschrolli.kingdomsatwar.network;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Server an Spiel: Ein Fisch hängt am Haken – der Drill beginnt ({@code client.fishing.FishingScreen}). {@code known}: steht die Art schon im
 * Fischbuch; {@code chest}: in der Säule treibt eine Truhe; {@code zoneBonus}: um so viel ist die Fangzone größer (Glück des Meeres). */
public record FishingStartPayload(int session, int species, int medium, boolean known, boolean chest, float zoneBonus) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<FishingStartPayload> TYPE = new CustomPacketPayload.Type<>(KingdomsAtWar.id("fishing_start"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FishingStartPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, FishingStartPayload::session, ByteBufCodecs.VAR_INT, FishingStartPayload::species, ByteBufCodecs.VAR_INT, FishingStartPayload::medium, ByteBufCodecs.BOOL, FishingStartPayload::known, ByteBufCodecs.BOOL, FishingStartPayload::chest, ByteBufCodecs.FLOAT, FishingStartPayload::zoneBonus, FishingStartPayload::new);

    @Override
    public CustomPacketPayload.Type<FishingStartPayload> type() {
        return TYPE;
    }
}
