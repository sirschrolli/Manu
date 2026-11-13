package io.github.sirschrolli.kingdomsatwar.network;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.entity.dragon.DragonEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Der Reiter drückt die Odem-Taste: Der gerittene Drache speit in Blickrichtung. */
public record DragonBreathPayload() implements CustomPacketPayload {
    public static final DragonBreathPayload INSTANCE = new DragonBreathPayload();
    public static final CustomPacketPayload.Type<DragonBreathPayload> TYPE = new CustomPacketPayload.Type<>(KingdomsAtWar.id("dragon_breath"));
    public static final StreamCodec<RegistryFriendlyByteBuf, DragonBreathPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public CustomPacketPayload.Type<DragonBreathPayload> type() {
        return TYPE;
    }

    /** Auf dem Server: nur der Besitzer, der den Drachen gerade reitet, darf den Odem auslösen. */
    static void handle(DragonBreathPayload payload, IPayloadContext context) {
        Player player = context.player();
        if (player.getVehicle() instanceof DragonEntity dragon && dragon.getControllingPassenger() == player) {
            dragon.startBreath(player.getLookAngle());
        }
    }
}
