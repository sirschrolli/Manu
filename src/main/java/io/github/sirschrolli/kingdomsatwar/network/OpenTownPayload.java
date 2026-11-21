package io.github.sirschrolli.kingdomsatwar.network;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.economy.town.TownView;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Server an Spiel: das Stadtfenster öffnen (oder auffrischen). {@code notice} ist die Antwort auf die letzte Handlung
 * (angeheuert, Bauplan genommen, zu wenig Münzen, ...) und wird im Fenster angezeigt.
 */
public record OpenTownPayload(BlockPos banner, TownView view, Optional<Component> notice, boolean good) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenTownPayload> TYPE = new CustomPacketPayload.Type<>(KingdomsAtWar.id("open_town"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenTownPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, OpenTownPayload::banner,
            TownView.STREAM_CODEC, OpenTownPayload::view,
            ComponentSerialization.TRUSTED_OPTIONAL_STREAM_CODEC, OpenTownPayload::notice,
            net.minecraft.network.codec.ByteBufCodecs.BOOL, OpenTownPayload::good,
            OpenTownPayload::new);

    @Override
    public CustomPacketPayload.Type<OpenTownPayload> type() {
        return TYPE;
    }
}
