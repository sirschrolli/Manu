package io.github.sirschrolli.kingdomsatwar.network;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Server an Spiel: Das Licht der Unsterblichkeit hat den Helden eben vor dem Tod bewahrt und ruht nun {@code minutes}
 * Minuten – auf seinem Bildschirm läuft die Szene des Lichts.
 */
public record HeroImmortalPayload(int minutes) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<HeroImmortalPayload> TYPE = new CustomPacketPayload.Type<>(KingdomsAtWar.id("hero_immortal"));
    public static final StreamCodec<RegistryFriendlyByteBuf, HeroImmortalPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, HeroImmortalPayload::minutes, HeroImmortalPayload::new);

    @Override
    public CustomPacketPayload.Type<HeroImmortalPayload> type() {
        return TYPE;
    }
}
