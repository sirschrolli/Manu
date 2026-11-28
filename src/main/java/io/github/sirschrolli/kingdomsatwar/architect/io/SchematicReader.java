package io.github.sirschrolli.kingdomsatwar.architect.io;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Dynamic;
import io.github.sirschrolli.kingdomsatwar.architect.BuildRules;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;
import net.minecraft.SharedConstants;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * Liest Schematiken anderer Werkzeuge (Dragon Architect): Litematica ({@code .litematic}, alle Regionen), Sponge-Schematik
 * von WorldEdit &amp; Co. ({@code .schem}, Version 1–3) und Strukturdateien des Strukturblocks ({@code .nbt}). Ältere
 * Dateien werden mit dem Datenwandler des Spiels auf den heutigen Stand gebracht (umbenannte Blöcke, neue Zustände,
 * Schildtexte). Unbekannte Blöcke (etwa aus anderen Mods) werden zu Luft und gezählt.
 */
public final class SchematicReader {
    /** So viele Bytes (entpackt) liest der Import höchstens – keine Datei soll den Server sprengen. */
    private static final long MAX_BYTES = 128L * 1024L * 1024L;
    /** Längste Seite, die überhaupt gelesen wird. */
    public static final int MAX_SIDE = 1024;
    /** Größter Rauminhalt, der überhaupt gelesen wird. */
    private static final long MAX_VOLUME = 64L * 1024L * 1024L;

    private SchematicReader() {}

    /** Die drei Dateiarten. */
    public enum Format {
        LITEMATIC(".litematic"), SCHEM(".schem"), NBT(".nbt");

        public final String extension;

        Format(String extension) {
            this.extension = extension;
        }

        public static @Nullable Format of(String fileName) {
            String name = fileName.toLowerCase(Locale.ROOT);
            for (Format f : values()) {
                if (name.endsWith(f.extension)) {
                    return f;
                }
            }
            return null;
        }
    }

    /**
     * Was in der Datei stand: das Volumen, sein Name und Zeichner (falls die Datei sie kennt), unbekannte Blöcke und die
     * Bodenlinie, wenn der Dragon Architect die Datei selbst geschrieben hat ({@code -1}: unbekannt).
     */
    public record Result(Volume volume, String name, String author, int unknown, Format format, int ground) {}

    /** Die Bodenlinie aus der Angabe des Dragon Architect ({@code -1}: keine oder unbrauchbar). */
    private static int ground(CompoundTag holder, Volume volume) {
        CompoundTag tag = holder.getCompoundOrEmpty(SchematicWriter.KEY);
        int ground = tag.getIntOr("Ground", -1);
        return ground >= 0 && ground < volume.sizeY && ground <= io.github.sirschrolli.kingdomsatwar.architect.PlanScan.MAX_GROUND ? ground : -1;
    }

    public static Result read(Path file, HolderLookup.Provider registries) throws IOException {
        Format format = Format.of(file.getFileName().toString());
        if (format == null) {
            throw new IOException("unknown format");
        }
        CompoundTag root;
        try {
            root = NbtIo.readCompressed(file, NbtAccounter.create(MAX_BYTES));
        } catch (IOException | RuntimeException e) {
            throw new IOException("not a gzip nbt file", e);
        }
        return read(root, format, registries);
    }

    public static Result read(CompoundTag root, Format format, HolderLookup.Provider registries) throws IOException {
        int[] unknown = {0};
        Result partial = switch (format) {
            case LITEMATIC -> litematic(root, registries, unknown);
            case SCHEM -> sponge(root, registries, unknown);
            case NBT -> structure(root, registries, unknown);
        };
        return new Result(partial.volume(), partial.name(), partial.author(), unknown[0], format, partial.ground());
    }

    // =====================================================================================
    // Litematica
    // =====================================================================================

    private static Result litematic(CompoundTag root, HolderLookup.Provider registries, int[] unknown) throws IOException {
        int dataVersion = root.getIntOr("MinecraftDataVersion", 0);
        CompoundTag regions = root.getCompoundOrEmpty("Regions");
        if (regions.isEmpty()) {
            throw new IOException("no regions");
        }
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;
        for (String key : regions.keySet()) {
            CompoundTag r = regions.getCompoundOrEmpty(key);
            int[] pos = xyz(r.getCompoundOrEmpty("Position"));
            int[] size = xyz(r.getCompoundOrEmpty("Size"));
            int[] lo = new int[3];
            for (int i = 0; i < 3; i++) {
                lo[i] = pos[i] + (size[i] < 0 ? size[i] + 1 : 0);
            }
            minX = Math.min(minX, lo[0]);
            minY = Math.min(minY, lo[1]);
            minZ = Math.min(minZ, lo[2]);
            maxX = Math.max(maxX, lo[0] + Math.abs(size[0]) - 1);
            maxY = Math.max(maxY, lo[1] + Math.abs(size[1]) - 1);
            maxZ = Math.max(maxZ, lo[2] + Math.abs(size[2]) - 1);
        }
        Volume volume = volume(maxX - minX + 1, maxY - minY + 1, maxZ - minZ + 1);
        for (String key : regions.keySet()) {
            CompoundTag r = regions.getCompoundOrEmpty(key);
            int[] pos = xyz(r.getCompoundOrEmpty("Position"));
            int[] size = xyz(r.getCompoundOrEmpty("Size"));
            int sx = Math.abs(size[0]);
            int sy = Math.abs(size[1]);
            int sz = Math.abs(size[2]);
            int ox = pos[0] + (size[0] < 0 ? size[0] + 1 : 0) - minX;
            int oy = pos[1] + (size[1] < 0 ? size[1] + 1 : 0) - minY;
            int oz = pos[2] + (size[2] < 0 ? size[2] + 1 : 0) - minZ;
            ListTag paletteTag = r.getListOrEmpty("BlockStatePalette");
            BlockState[] palette = new BlockState[Math.max(1, paletteTag.size())];
            for (int i = 0; i < paletteTag.size(); i++) {
                palette[i] = fromTag(paletteTag.getCompoundOrEmpty(i), dataVersion, unknown);
            }
            long[] bits = r.getLongArray("BlockStates").orElse(new long[0]);
            int perEntry = Math.max(2, 32 - Integer.numberOfLeadingZeros(Math.max(1, palette.length - 1)));
            long volumeSize = (long) sx * sy * sz;
            if ((long) bits.length * 64L < volumeSize * perEntry) {
                throw new IOException("region " + key + " too short");
            }
            long mask = (1L << perEntry) - 1L;
            for (int y = 0; y < sy; y++) {
                for (int z = 0; z < sz; z++) {
                    for (int x = 0; x < sx; x++) {
                        long index = ((long) y * sz + z) * sx + x;
                        long start = index * perEntry;
                        int startArr = (int) (start >>> 6);
                        int endArr = (int) ((start + perEntry - 1) >>> 6);
                        int startBit = (int) (start & 63);
                        long value = startArr == endArr ? (bits[startArr] >>> startBit) & mask
                                : ((bits[startArr] >>> startBit) | (bits[endArr] << (64 - startBit))) & mask;
                        BlockState state = value < palette.length && palette[(int) value] != null ? palette[(int) value]
                                : Blocks.AIR.defaultBlockState();
                        volume.set(ox + x, oy + y, oz + z, state);
                    }
                }
            }
            for (Tag t : r.getListOrEmpty("TileEntities")) {
                if (t instanceof CompoundTag be) {
                    BlockPos at = new BlockPos(ox + be.getIntOr("x", 0), oy + be.getIntOr("y", 0), oz + be.getIntOr("z", 0));
                    CompoundTag data = be.copy();
                    data.remove("x");
                    data.remove("y");
                    data.remove("z");
                    blockEntity(volume, at, data, data.getStringOr("id", ""), dataVersion, registries);
                }
            }
        }
        CompoundTag meta = root.getCompoundOrEmpty("Metadata");
        int ground = minY == 0 ? ground(meta, volume) : -1;
        return new Result(volume, meta.getStringOr("Name", ""), meta.getStringOr("Author", ""), 0, Format.LITEMATIC, ground);
    }

    private static int[] xyz(CompoundTag tag) {
        return new int[] {tag.getIntOr("x", 0), tag.getIntOr("y", 0), tag.getIntOr("z", 0)};
    }

    // =====================================================================================
    // Sponge-Schematik (WorldEdit, FAWE, Amulet …)
    // =====================================================================================

    private static Result sponge(CompoundTag root, HolderLookup.Provider registries, int[] unknown) throws IOException {
        CompoundTag s = root.getCompound("Schematic").orElse(root);
        int version = s.getIntOr("Version", 1);
        int dataVersion = s.getIntOr("DataVersion", 1343); // Version 1 kannte noch keine Datenversion (1.13.2)
        int w = s.getShortOr("Width", (short) 0) & 0xFFFF;
        int h = s.getShortOr("Height", (short) 0) & 0xFFFF;
        int l = s.getShortOr("Length", (short) 0) & 0xFFFF;
        Volume volume = volume(w, h, l);
        CompoundTag paletteTag;
        byte[] data;
        ListTag entities;
        if (version >= 3) {
            CompoundTag blocks = s.getCompoundOrEmpty("Blocks");
            paletteTag = blocks.getCompoundOrEmpty("Palette");
            data = blocks.getByteArray("Data").orElse(new byte[0]);
            entities = blocks.getListOrEmpty("BlockEntities");
        } else {
            paletteTag = s.getCompoundOrEmpty("Palette");
            data = s.getByteArray("BlockData").orElse(new byte[0]);
            entities = s.contains("BlockEntities") ? s.getListOrEmpty("BlockEntities") : s.getListOrEmpty("TileEntities");
        }
        if (paletteTag.isEmpty()) {
            throw new IOException("no palette (old .schematic files are not supported)");
        }
        int max = 0;
        for (String key : paletteTag.keySet()) {
            max = Math.max(max, paletteTag.getIntOr(key, 0));
        }
        if (max > 1 << 20) {
            throw new IOException("palette too large");
        }
        BlockState[] palette = new BlockState[max + 1];
        for (String key : paletteTag.keySet()) {
            int index = paletteTag.getIntOr(key, -1);
            if (index >= 0) {
                palette[index] = fromFlat(key, dataVersion, unknown);
            }
        }
        long total = (long) w * h * l;
        int i = 0;
        long pos = 0;
        while (i < data.length && pos < total) {
            int value = 0;
            int shift = 0;
            byte b;
            do {
                b = data[i++];
                value |= (b & 0x7F) << shift;
                shift += 7;
                if (shift > 35) {
                    throw new IOException("bad block data");
                }
            } while ((b & 0x80) != 0 && i < data.length);
            int x = (int) (pos % w);
            int z = (int) ((pos / w) % l);
            int y = (int) (pos / ((long) w * l));
            BlockState state = value >= 0 && value < palette.length && palette[value] != null ? palette[value] : Blocks.AIR.defaultBlockState();
            volume.set(x, y, z, state);
            pos++;
        }
        for (Tag t : entities) {
            if (t instanceof CompoundTag be) {
                int[] p = be.getIntArray("Pos").orElse(new int[0]);
                if (p.length < 3) {
                    continue;
                }
                String id = be.getStringOr("Id", be.getStringOr("id", ""));
                CompoundTag beData = version >= 3 ? be.getCompoundOrEmpty("Data").copy() : be.copy();
                beData.remove("Pos");
                beData.remove("Id");
                blockEntity(volume, new BlockPos(p[0], p[1], p[2]), beData, id, dataVersion, registries);
            }
        }
        CompoundTag meta = s.getCompoundOrEmpty("Metadata");
        return new Result(volume, meta.getStringOr("Name", ""), meta.getStringOr("Author", ""), 0, Format.SCHEM, ground(meta, volume));
    }

    // =====================================================================================
    // Strukturdatei (Strukturblock)
    // =====================================================================================

    private static Result structure(CompoundTag root, HolderLookup.Provider registries, int[] unknown) throws IOException {
        int dataVersion = root.getIntOr("DataVersion", 500);
        CompoundTag tag = dataVersion < current() ? DataFixTypes.STRUCTURE.updateToCurrentVersion(fixer(), root, dataVersion) : root;
        ListTag size = tag.getListOrEmpty("size");
        Volume volume = volume(size.getIntOr(0, 0), size.getIntOr(1, 0), size.getIntOr(2, 0));
        ListTag paletteTag = tag.contains("palettes") ? tag.getListOrEmpty("palettes").getListOrEmpty(0) : tag.getListOrEmpty("palette");
        BlockState[] palette = new BlockState[Math.max(1, paletteTag.size())];
        for (int i = 0; i < paletteTag.size(); i++) {
            palette[i] = fromTag(paletteTag.getCompoundOrEmpty(i), current(), unknown);
        }
        for (Tag t : tag.getListOrEmpty("blocks")) {
            if (!(t instanceof CompoundTag block)) {
                continue;
            }
            ListTag p = block.getListOrEmpty("pos");
            int state = block.getIntOr("state", -1);
            if (p.size() < 3 || state < 0 || state >= palette.length) {
                continue;
            }
            BlockPos at = new BlockPos(p.getIntOr(0, 0), p.getIntOr(1, 0), p.getIntOr(2, 0));
            volume.set(at.getX(), at.getY(), at.getZ(), palette[state]);
            CompoundTag nbt = block.getCompoundOrEmpty("nbt");
            if (!nbt.isEmpty()) {
                blockEntity(volume, at, nbt.copy(), nbt.getStringOr("id", ""), current(), registries);
            }
        }
        return new Result(volume, "", root.getStringOr("author", ""), 0, Format.NBT, ground(root, volume));
    }

    // =====================================================================================
    // Hilfen
    // =====================================================================================

    private static Volume volume(int x, int y, int z) throws IOException {
        if (x <= 0 || y <= 0 || z <= 0) {
            throw new IOException("empty");
        }
        if (Math.max(x, Math.max(y, z)) > MAX_SIDE || (long) x * y * z > MAX_VOLUME) {
            throw new IOException("too large: " + x + "x" + y + "x" + z);
        }
        return new Volume(x, y, z);
    }

    private static DataFixer fixer() {
        return DataFixers.getDataFixer();
    }

    private static int current() {
        return SharedConstants.getCurrentVersion().dataVersion().version();
    }

    /** Ein Block als Zeichenkette ({@code minecraft:oak_stairs[facing=east]}), auf den heutigen Stand gebracht. */
    static BlockState fromFlat(String flat, int dataVersion, int[] unknown) {
        String fixed = flat;
        if (dataVersion > 0 && dataVersion < current()) {
            try {
                fixed = fixer().update(References.FLAT_BLOCK_STATE, new Dynamic<>(NbtOps.INSTANCE, StringTag.valueOf(flat)), dataVersion, current())
                        .asString(flat);
            } catch (RuntimeException e) {
                fixed = flat;
            }
        }
        try {
            return BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK, fixed, false).blockState();
        } catch (CommandSyntaxException | RuntimeException e) {
            unknown[0]++;
            return Blocks.AIR.defaultBlockState();
        }
    }

    /** Ein Block als {@code {Name, Properties}}, auf den heutigen Stand gebracht. */
    static BlockState fromTag(CompoundTag tag, int dataVersion, int[] unknown) {
        CompoundTag fixed = tag;
        if (dataVersion > 0 && dataVersion < current()) {
            try {
                if (fixer().update(References.BLOCK_STATE, new Dynamic<>(NbtOps.INSTANCE, (Tag) tag), dataVersion, current()).getValue()
                        instanceof CompoundTag c) {
                    fixed = c;
                }
            } catch (RuntimeException e) {
                fixed = tag;
            }
        }
        Identifier id = Identifier.tryParse(fixed.getStringOr("Name", "minecraft:air"));
        if (id == null || !BuiltInRegistries.BLOCK.containsKey(id)) {
            unknown[0]++;
            return Blocks.AIR.defaultBlockState();
        }
        return NbtUtils.readBlockState(BuiltInRegistries.BLOCK, fixed);
    }

    /** Legt ein Blockobjekt an, wenn der Dragon Architect seine Daten übernimmt (Schilder, Banner). */
    private static void blockEntity(Volume volume, BlockPos pos, CompoundTag data, String id, int dataVersion, HolderLookup.Provider registries) {
        BlockState state = volume.getBlockState(pos);
        if (!state.hasBlockEntity() || id.isEmpty()) {
            return;
        }
        CompoundTag tag = data.copy();
        tag.putString("id", id);
        tag.putInt("x", pos.getX());
        tag.putInt("y", pos.getY());
        tag.putInt("z", pos.getZ());
        if (dataVersion > 0 && dataVersion < current()) {
            try {
                if (fixer().update(References.BLOCK_ENTITY, new Dynamic<>(NbtOps.INSTANCE, (Tag) tag), dataVersion, current()).getValue()
                        instanceof CompoundTag c) {
                    tag = c;
                }
            } catch (RuntimeException e) {
                return;
            }
        }
        try {
            BlockEntity be = BlockEntity.loadStatic(pos, state, tag, registries);
            if (be != null && BuildRules.keepsData(be)) {
                volume.setBlockEntity(pos, be);
            }
        } catch (RuntimeException e) {
            // kaputte Daten: das Schild bleibt leer
        }
    }
}
