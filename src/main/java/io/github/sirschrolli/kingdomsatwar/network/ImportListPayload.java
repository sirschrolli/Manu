package io.github.sirschrolli.kingdomsatwar.network;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.architect.io.SchematicFiles;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Server an Spiel: die Schematiken in den Ordnern des Servers, die sich als eigene Baupläne einlesen lassen. */
public record ImportListPayload(List<SchematicFiles.Entry> files, String folder) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ImportListPayload> TYPE = new CustomPacketPayload.Type<>(KingdomsAtWar.id("import_list"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ImportListPayload> STREAM_CODEC = StreamCodec.composite(
            SchematicFiles.Entry.STREAM_CODEC.apply(ByteBufCodecs.list()), ImportListPayload::files,
            ByteBufCodecs.STRING_UTF8, ImportListPayload::folder,
            ImportListPayload::new);

    @Override
    public CustomPacketPayload.Type<ImportListPayload> type() {
        return TYPE;
    }
}
