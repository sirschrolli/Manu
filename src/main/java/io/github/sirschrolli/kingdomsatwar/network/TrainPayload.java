package io.github.sirschrolli.kingdomsatwar.network;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.army.Recruitment;
import io.github.sirschrolli.kingdomsatwar.army.Training;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Spiel an Server: Der Spieler möchte am Tisch bei {@code pos} seinen Soldaten {@code entityId} ausbilden. */
public record TrainPayload(BlockPos pos, int entityId, int action) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<TrainPayload> TYPE = new CustomPacketPayload.Type<>(KingdomsAtWar.id("train"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TrainPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, TrainPayload::pos,
            ByteBufCodecs.VAR_INT, TrainPayload::entityId,
            ByteBufCodecs.VAR_INT, TrainPayload::action,
            TrainPayload::new);

    @Override
    public CustomPacketPayload.Type<TrainPayload> type() {
        return TYPE;
    }

    /** Auf dem Server: alles wird dort noch einmal geprüft (Tisch, Besitzer, Entfernung, Preis). */
    static void handle(TrainPayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }
        Training.Action[] actions = Training.Action.values();
        if (payload.action() < 0 || payload.action() >= actions.length) {
            return;
        }
        Recruitment.Result result = Training.train(player, payload.pos(), payload.entityId(), actions[payload.action()]);
        ModNetwork.send(player, new RecruitResultPayload(result.ordinal()));
    }
}
