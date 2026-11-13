package io.github.sirschrolli.kingdomsatwar.network;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.army.Garrison;
import io.github.sirschrolli.kingdomsatwar.army.Recruitment;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Spiel an Server: am Feldherrentisch bei {@code pos} auf den Schlachtzug ziehen ({@code march}) oder heimkehren. */
public record CampaignPayload(BlockPos pos, boolean march) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<CampaignPayload> TYPE = new CustomPacketPayload.Type<>(KingdomsAtWar.id("campaign"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CampaignPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, CampaignPayload::pos,
            ByteBufCodecs.BOOL, CampaignPayload::march,
            CampaignPayload::new);

    @Override
    public CustomPacketPayload.Type<CampaignPayload> type() {
        return TYPE;
    }

    /** Auf dem Server: alles wird dort noch einmal geprüft (Tisch, Entfernung, Siedlung). */
    static void handle(CampaignPayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }
        Recruitment.Result result = payload.march() ? Garrison.march(player, payload.pos()) : Garrison.returnHome(player, payload.pos());
        ModNetwork.send(player, new RecruitResultPayload(result.ordinal()));
    }
}
