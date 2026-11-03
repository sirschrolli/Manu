package io.github.sirschrolli.kingdomsatwar.network;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.army.Mercenaries;
import io.github.sirschrolli.kingdomsatwar.army.Recruitment;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Spiel an Server: Der Spieler heuert an der Söldnertafel bei {@code board} den Söldner {@code entityId} an. */
public record HirePayload(BlockPos board, int entityId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<HirePayload> TYPE = new CustomPacketPayload.Type<>(KingdomsAtWar.id("hire"));
    public static final StreamCodec<RegistryFriendlyByteBuf, HirePayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, HirePayload::board,
            ByteBufCodecs.VAR_INT, HirePayload::entityId,
            HirePayload::new);

    @Override
    public CustomPacketPayload.Type<HirePayload> type() {
        return TYPE;
    }

    /** Auf dem Server: Tafel, Entfernung, Söldner, Armeegröße und Sold werden dort geprüft. */
    static void handle(HirePayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }
        Recruitment.Result result = Mercenaries.hire(player, payload.board(), payload.entityId());
        ModNetwork.send(player, new RecruitResultPayload(result.ordinal()));
    }
}
