package io.github.sirschrolli.kingdomsatwar.network;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Server an Spiel: Bildschirm beben lassen (Boss erwacht, stampft, stürzt …). Stärke 0..1, Dauer in Ticks. */
public record ScreenShakePayload(float strength, int ticks) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ScreenShakePayload> TYPE = new CustomPacketPayload.Type<>(KingdomsAtWar.id("screen_shake"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ScreenShakePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, ScreenShakePayload::strength, ByteBufCodecs.VAR_INT, ScreenShakePayload::ticks, ScreenShakePayload::new);

    @Override
    public CustomPacketPayload.Type<ScreenShakePayload> type() {
        return TYPE;
    }
}
