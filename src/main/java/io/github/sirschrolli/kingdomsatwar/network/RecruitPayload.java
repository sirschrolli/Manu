package io.github.sirschrolli.kingdomsatwar.network;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.army.Recruitment;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Spiel an Server: Der Spieler möchte am Tisch bei {@code pos} einen Soldaten der Art {@code soldierType} anwerben. */
public record RecruitPayload(BlockPos pos, int soldierType) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RecruitPayload> TYPE = new CustomPacketPayload.Type<>(KingdomsAtWar.id("recruit"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RecruitPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, RecruitPayload::pos,
            ByteBufCodecs.VAR_INT, RecruitPayload::soldierType,
            RecruitPayload::new);

    @Override
    public CustomPacketPayload.Type<RecruitPayload> type() {
        return TYPE;
    }

    /** Auf dem Server: alles wird dort noch einmal geprüft (Entfernung, Preis, Armeegröße). */
    static void handle(RecruitPayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }
        SoldierType[] types = SoldierType.values();
        if (payload.soldierType() < 0 || payload.soldierType() >= types.length) {
            return;
        }
        Recruitment.Result result = Recruitment.recruit(player, payload.pos(), types[payload.soldierType()]);
        PacketDistributor.sendToPlayer(player, new RecruitResultPayload(result.ordinal()));
    }
}
