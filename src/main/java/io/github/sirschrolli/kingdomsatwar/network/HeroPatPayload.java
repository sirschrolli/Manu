package io.github.sirschrolli.kingdomsatwar.network;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.hero.HeroPat;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Spiel an Server: Der Held möchte das Wesen {@code target} streicheln. Der Server prüft Stufe, Reichweite und Pause. */
public record HeroPatPayload(int target) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<HeroPatPayload> TYPE = new CustomPacketPayload.Type<>(KingdomsAtWar.id("hero_pat"));
    public static final StreamCodec<RegistryFriendlyByteBuf, HeroPatPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, HeroPatPayload::target, HeroPatPayload::new);

    @Override
    public CustomPacketPayload.Type<HeroPatPayload> type() {
        return TYPE;
    }

    static void handle(HeroPatPayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }
        Entity target = player.level().getEntity(payload.target());
        if (target != null) {
            HeroPat.pat(player, target);
        }
    }
}
