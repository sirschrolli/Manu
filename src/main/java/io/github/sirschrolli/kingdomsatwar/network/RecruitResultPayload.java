package io.github.sirschrolli.kingdomsatwar.network;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Server an Spiel: Ergebnis des Anwerbens ({@link io.github.sirschrolli.kingdomsatwar.army.Recruitment.Result}). */
public record RecruitResultPayload(int result) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RecruitResultPayload> TYPE = new CustomPacketPayload.Type<>(KingdomsAtWar.id("recruit_result"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RecruitResultPayload> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.VAR_INT, RecruitResultPayload::result, RecruitResultPayload::new);

    @Override
    public CustomPacketPayload.Type<RecruitResultPayload> type() {
        return TYPE;
    }
}
