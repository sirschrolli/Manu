package io.github.sirschrolli.kingdomsatwar.network;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Server an Spiel: Der Held ist von Heldenstufe {@code from} auf {@code to} aufgestiegen – die Aufstiegs-Szene beginnt. */
public record HeroLevelUpPayload(int from, int to) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<HeroLevelUpPayload> TYPE = new CustomPacketPayload.Type<>(KingdomsAtWar.id("hero_level_up"));
    public static final StreamCodec<RegistryFriendlyByteBuf, HeroLevelUpPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, HeroLevelUpPayload::from, ByteBufCodecs.VAR_INT, HeroLevelUpPayload::to, HeroLevelUpPayload::new);

    @Override
    public CustomPacketPayload.Type<HeroLevelUpPayload> type() {
        return TYPE;
    }
}
