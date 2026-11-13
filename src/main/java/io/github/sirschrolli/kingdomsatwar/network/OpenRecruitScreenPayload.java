package io.github.sirschrolli.kingdomsatwar.network;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Server an Spiel: Fenster des Feldherrentischs bei {@code pos} öffnen (mit Größe der Armee und ob es eine Garnison gibt). */
public record OpenRecruitScreenPayload(BlockPos pos, int armyLimit, int armySize, boolean hasTown) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenRecruitScreenPayload> TYPE =
            new CustomPacketPayload.Type<>(KingdomsAtWar.id("open_recruit_screen"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenRecruitScreenPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, OpenRecruitScreenPayload::pos,
            ByteBufCodecs.VAR_INT, OpenRecruitScreenPayload::armyLimit,
            ByteBufCodecs.VAR_INT, OpenRecruitScreenPayload::armySize,
            ByteBufCodecs.BOOL, OpenRecruitScreenPayload::hasTown,
            OpenRecruitScreenPayload::new);

    @Override
    public CustomPacketPayload.Type<OpenRecruitScreenPayload> type() {
        return TYPE;
    }
}
