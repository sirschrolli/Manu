package io.github.sirschrolli.kingdomsatwar.network;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.architect.PlanInfo;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Server an Spiel: die eigenen Baupläne des Spielers (für das Stadtfenster). */
public record PlanListPayload(List<PlanInfo> plans) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PlanListPayload> TYPE = new CustomPacketPayload.Type<>(KingdomsAtWar.id("plan_list"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PlanListPayload> STREAM_CODEC = StreamCodec.composite(
            PlanInfo.STREAM_CODEC.apply(ByteBufCodecs.list()), PlanListPayload::plans,
            PlanListPayload::new);

    @Override
    public CustomPacketPayload.Type<PlanListPayload> type() {
        return TYPE;
    }
}
