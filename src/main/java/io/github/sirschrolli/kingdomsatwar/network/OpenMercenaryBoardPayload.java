package io.github.sirschrolli.kingdomsatwar.network;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Server an Spiel: die Söldnertafel bei {@code pos} (im Dorf {@code village}) öffnen. */
public record OpenMercenaryBoardPayload(BlockPos pos, int armyLimit, String village) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenMercenaryBoardPayload> TYPE =
            new CustomPacketPayload.Type<>(KingdomsAtWar.id("open_mercenary_board"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenMercenaryBoardPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, OpenMercenaryBoardPayload::pos,
            ByteBufCodecs.VAR_INT, OpenMercenaryBoardPayload::armyLimit,
            ByteBufCodecs.stringUtf8(64), OpenMercenaryBoardPayload::village,
            OpenMercenaryBoardPayload::new);

    @Override
    public CustomPacketPayload.Type<OpenMercenaryBoardPayload> type() {
        return TYPE;
    }
}
