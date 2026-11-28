package io.github.sirschrolli.kingdomsatwar.network;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.magic.AetheritStaffItem;
import io.github.sirschrolli.kingdomsatwar.magic.Spell;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Spiel an Server: im Zauberkreis wurde ein Zauber gewählt (für den Stab in der Hand). */
public record SelectSpellPayload(Spell spell) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SelectSpellPayload> TYPE = new CustomPacketPayload.Type<>(KingdomsAtWar.id("select_spell"));
    public static final StreamCodec<ByteBuf, SelectSpellPayload> STREAM_CODEC =
            Spell.STREAM_CODEC.map(SelectSpellPayload::new, SelectSpellPayload::spell);

    @Override
    public CustomPacketPayload.Type<SelectSpellPayload> type() {
        return TYPE;
    }

    static void handle(SelectSpellPayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            ItemStack staff = AetheritStaffItem.heldStaff(player);
            if (!staff.isEmpty()) {
                AetheritStaffItem.select(staff, payload.spell());
            }
        }
    }
}
