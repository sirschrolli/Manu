package io.github.sirschrolli.kingdomsatwar.network;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.architect.Architect;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Spiel an Server: den vermessenen Bereich als Bauplan zeichnen (Name, Bodenlinie, Vorderseite). Der Server prüft, ob
 * die Ecken die des Vermessungsstabs in der Hand sind, und vermisst selbst.
 */
public record SavePlanPayload(BlockPos first, BlockPos second, String name, int ground, Direction front) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SavePlanPayload> TYPE = new CustomPacketPayload.Type<>(KingdomsAtWar.id("save_plan"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SavePlanPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SavePlanPayload::first,
            BlockPos.STREAM_CODEC, SavePlanPayload::second,
            ByteBufCodecs.stringUtf8(64), SavePlanPayload::name,
            ByteBufCodecs.VAR_INT, SavePlanPayload::ground,
            Direction.STREAM_CODEC, SavePlanPayload::front,
            SavePlanPayload::new);

    @Override
    public CustomPacketPayload.Type<SavePlanPayload> type() {
        return TYPE;
    }

    static void handle(SavePlanPayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            Architect.save(player, payload.first(), payload.second(), payload.name(), payload.ground(), payload.front());
        }
    }
}
