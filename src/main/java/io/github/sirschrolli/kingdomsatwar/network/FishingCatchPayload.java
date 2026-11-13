package io.github.sirschrolli.kingdomsatwar.network;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Server an Spiel: gefangen! Art, Größe in Zentimetern, {@code news} (2 = neue Art im Fischbuch, 1 = neuer Rekord, 0 = sonst), ob eine
 * Truhe dabei war und ob der Fisch gleich in den Fischbeutel gewandert ist – der Drill zeigt es zum Abschluss. */
public record FishingCatchPayload(int session, int species, int cm, int news, boolean chest, boolean bagged) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<FishingCatchPayload> TYPE = new CustomPacketPayload.Type<>(KingdomsAtWar.id("fishing_catch"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FishingCatchPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, FishingCatchPayload::session, ByteBufCodecs.VAR_INT, FishingCatchPayload::species, ByteBufCodecs.VAR_INT, FishingCatchPayload::cm, ByteBufCodecs.VAR_INT, FishingCatchPayload::news, ByteBufCodecs.BOOL, FishingCatchPayload::chest, ByteBufCodecs.BOOL, FishingCatchPayload::bagged,
            FishingCatchPayload::new);

    @Override
    public CustomPacketPayload.Type<FishingCatchPayload> type() {
        return TYPE;
    }
}
