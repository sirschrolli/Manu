package io.github.sirschrolli.kingdomsatwar.network;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.fishing.FishLog;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Server an Spiel: das Fischbuch aufschlagen (mit allen Fängen des Spielers). */
public record OpenFishBookPayload(FishLog log) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenFishBookPayload> TYPE = new CustomPacketPayload.Type<>(KingdomsAtWar.id("open_fish_book"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenFishBookPayload> STREAM_CODEC =
            FishLog.STREAM_CODEC.<RegistryFriendlyByteBuf>cast().map(OpenFishBookPayload::new, OpenFishBookPayload::log);

    @Override
    public CustomPacketPayload.Type<OpenFishBookPayload> type() {
        return TYPE;
    }
}
