package io.github.sirschrolli.kingdomsatwar.network;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;

/**
 * Server an Spiel: Zeige diesen Gegenstand groß im Bild, wie Minecraft es beim Totem der Unsterblichkeit tut (seit 0.80 für
 * das Zweite Leben des Apfels der Unsterblichkeit – dort ist kein Totem in der Hand, das das Spiel von selbst fände).
 */
public record ItemActivationPayload(ItemStack stack) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ItemActivationPayload> TYPE = new CustomPacketPayload.Type<>(KingdomsAtWar.id("item_activation"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemActivationPayload> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC, ItemActivationPayload::stack, ItemActivationPayload::new);

    @Override
    public CustomPacketPayload.Type<ItemActivationPayload> type() {
        return TYPE;
    }
}
