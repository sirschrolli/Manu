package io.github.sirschrolli.kingdomsatwar.network;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Server an Spiel: das Fenster „Bauplan zeichnen“ für den vermessenen Bereich öffnen (Dragon Architect). */
public record OpenSavePlanPayload(BlockPos first, BlockPos second) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenSavePlanPayload> TYPE = new CustomPacketPayload.Type<>(KingdomsAtWar.id("open_save_plan"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenSavePlanPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, OpenSavePlanPayload::first,
            BlockPos.STREAM_CODEC, OpenSavePlanPayload::second,
            OpenSavePlanPayload::new);

    @Override
    public CustomPacketPayload.Type<OpenSavePlanPayload> type() {
        return TYPE;
    }
}
