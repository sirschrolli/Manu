package io.github.sirschrolli.kingdomsatwar.network;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.item.glitch.GlitchWings;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Spiel an Server: Der Spieler will mit den Fehlerflügeln blinzeln (der Server prüft alles und springt). */
public record GlitchBlinkPayload() implements CustomPacketPayload {
    public static final GlitchBlinkPayload INSTANCE = new GlitchBlinkPayload();
    public static final CustomPacketPayload.Type<GlitchBlinkPayload> TYPE = new CustomPacketPayload.Type<>(KingdomsAtWar.id("glitch_blink"));
    public static final StreamCodec<RegistryFriendlyByteBuf, GlitchBlinkPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public CustomPacketPayload.Type<GlitchBlinkPayload> type() {
        return TYPE;
    }

    static void handle(GlitchBlinkPayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            GlitchWings.blink(player);
        }
    }
}
