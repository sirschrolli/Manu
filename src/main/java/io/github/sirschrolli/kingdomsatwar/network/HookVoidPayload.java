package io.github.sirschrolli.kingdomsatwar.network;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Server an Spiel: Dieser Angelhaken treibt in der Leere – auf dieser Höhe (wie auf einer Wasseroberfläche). */
public record HookVoidPayload(int entity, double level) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<HookVoidPayload> TYPE = new CustomPacketPayload.Type<>(KingdomsAtWar.id("hook_void"));
    public static final StreamCodec<RegistryFriendlyByteBuf, HookVoidPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, HookVoidPayload::entity, ByteBufCodecs.DOUBLE, HookVoidPayload::level, HookVoidPayload::new);

    @Override
    public CustomPacketPayload.Type<HookVoidPayload> type() {
        return TYPE;
    }
}
