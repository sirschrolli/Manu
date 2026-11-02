package io.github.sirschrolli.kingdomsatwar.network;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.army.Provisions;
import io.github.sirschrolli.kingdomsatwar.army.Recruitment;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Spiel an Server: Der Spieler möchte am Feldherrentisch bei {@code pos} allen im Feldlager Proviant zuteilen. */
public record ProvisionPayload(BlockPos pos) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ProvisionPayload> TYPE = new CustomPacketPayload.Type<>(KingdomsAtWar.id("provisions"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ProvisionPayload> STREAM_CODEC =
            StreamCodec.composite(BlockPos.STREAM_CODEC, ProvisionPayload::pos, ProvisionPayload::new);

    @Override
    public CustomPacketPayload.Type<ProvisionPayload> type() {
        return TYPE;
    }

    /** Auf dem Server: alles wird dort noch einmal geprüft (Tisch, Entfernung, Essen). */
    static void handle(ProvisionPayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }
        Recruitment.Result result = Provisions.supply(player, payload.pos());
        ModNetwork.send(player, new RecruitResultPayload(result.ordinal()));
    }
}
