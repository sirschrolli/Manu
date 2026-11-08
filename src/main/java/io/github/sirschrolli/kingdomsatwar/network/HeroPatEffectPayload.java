package io.github.sirschrolli.kingdomsatwar.network;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Server an Spiel: {@code patter} streichelt gerade {@code target} – Hand über dem Kopf zeigen, das Wesen kurz ducken. */
public record HeroPatEffectPayload(int target, int patter) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<HeroPatEffectPayload> TYPE = new CustomPacketPayload.Type<>(KingdomsAtWar.id("hero_pat_effect"));
    public static final StreamCodec<RegistryFriendlyByteBuf, HeroPatEffectPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, HeroPatEffectPayload::target, ByteBufCodecs.VAR_INT, HeroPatEffectPayload::patter, HeroPatEffectPayload::new);

    @Override
    public CustomPacketPayload.Type<HeroPatEffectPayload> type() {
        return TYPE;
    }
}
