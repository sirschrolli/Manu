package io.github.sirschrolli.kingdomsatwar.network;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.army.Recruitment;
import io.github.sirschrolli.kingdomsatwar.army.Training;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Spiel an Server: Der Spieler möchte am Feldherrentisch bei {@code pos} alle Verwundeten im Feldlager heilen lassen. */
public record HealPayload(BlockPos pos) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<HealPayload> TYPE = new CustomPacketPayload.Type<>(KingdomsAtWar.id("heal_camp"));
    public static final StreamCodec<RegistryFriendlyByteBuf, HealPayload> STREAM_CODEC =
            StreamCodec.composite(BlockPos.STREAM_CODEC, HealPayload::pos, HealPayload::new);

    @Override
    public CustomPacketPayload.Type<HealPayload> type() {
        return TYPE;
    }

    /** Auf dem Server: alles wird dort noch einmal geprüft (Tisch, Entfernung, Preis). */
    static void handle(HealPayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }
        Recruitment.Result result = Training.healAll(player, payload.pos());
        ModNetwork.send(player, new RecruitResultPayload(result.ordinal()));
    }
}
