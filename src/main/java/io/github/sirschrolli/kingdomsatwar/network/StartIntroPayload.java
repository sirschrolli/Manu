package io.github.sirschrolli.kingdomsatwar.network;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Server an Spiel: Erzähl-Intro abspielen. {@code forced} = ausdrücklich gewünscht (Befehl {@code /kaw intro}),
 * sonst beim ersten Betreten der Welt – dann kann der Spieler das Intro in den Einstellungen abschalten.
 */
public record StartIntroPayload(boolean forced) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<StartIntroPayload> TYPE = new CustomPacketPayload.Type<>(KingdomsAtWar.id("start_intro"));
    public static final StreamCodec<RegistryFriendlyByteBuf, StartIntroPayload> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.BOOL, StartIntroPayload::forced, StartIntroPayload::new);

    @Override
    public CustomPacketPayload.Type<StartIntroPayload> type() {
        return TYPE;
    }
}
