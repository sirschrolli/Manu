package io.github.sirschrolli.kingdomsatwar.architect.io;

import io.github.sirschrolli.kingdomsatwar.architect.PlanScan;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * Schreibt einen eigenen Bauplan als Schematik für andere Werkzeuge (Dragon Architect): Litematica ({@code .litematic},
 * eine Region), Sponge-Schematik Version 3 ({@code .schem}, WorldEdit &amp; Co.) und Strukturdatei ({@code .nbt},
 * Strukturblock). Gelände unter der Bodenlinie, das der Plan unberührt lässt, wird zu Gras (oberste Lage) und Erde – so
 * reißt das Einfügen mit Litematica oder WorldEdit keine Löcher in den Boden –, in der Strukturdatei zu Strukturleere.
 * Jede Datei trägt die Bodenlinie mit ({@code KingdomsAtWar.Ground}); beim Wiedereinlesen kommt der Plan genau so zurück.
 */
public final class SchematicWriter {
    private SchematicWriter() {}

    /** Schreibt alle drei Dateien in einen Ordner; gibt die geschriebenen Dateien zurück. */
    public static List<Path> writeAll(PlanScan scan, String name, String author, Path dir) throws IOException {
        Files.createDirectories(dir);
        String base = fileName(name);
        List<Path> out = new ArrayList<>();
        for (SchematicReader.Format format : SchematicReader.Format.values()) {
            Path file = free(dir, base, format.extension);
            CompoundTag tag = switch (format) {
                case LITEMATIC -> litematic(scan, name, author);
                case SCHEM -> sponge(scan, name, author);
                case NBT -> structure(scan, author);
            };
            NbtIo.writeCompressed(tag, file);
            out.add(file);
        }
        return out;
    }

    /** Ein Dateiname aus dem Namen des Plans (Umlaute ausgeschrieben, nur Buchstaben, Ziffern, - und _). */
    static String fileName(String name) {
        String s = name.strip().replace("ä", "ae").replace("ö", "oe").replace("ü", "ue").replace("Ä", "Ae").replace("Ö", "Oe")
                .replace("Ü", "Ue").replace("ß", "ss").replaceAll("[^A-Za-z0-9_-]+", "_").replaceAll("_+", "_");
        s = s.replaceAll("^_|_$", "");
        return s.isEmpty() ? "bauplan" : s.length() > 48 ? s.substring(0, 48) : s;
    }

    private static Path free(Path dir, String base, String extension) {
        Path file = dir.resolve(base + extension);
        for (int i = 2; Files.exists(file) && i < 1000; i++) {
            file = dir.resolve(base + "_" + i + extension);
        }
        return file;
    }

    private static int dataVersion() {
        return SharedConstants.getCurrentVersion().dataVersion().version();
    }

    /** Was an einer Stelle in die Datei kommt: unberührtes Gelände unter der Bodenlinie wird Gras (oben) und Erde. */
    private static BlockState at(PlanScan scan, int x, int y, int z) {
        BlockState s = scan.get(x, y, z);
        if (s != null) {
            return s;
        }
        if (y >= scan.ground) {
            return Blocks.AIR.defaultBlockState();
        }
        return y == scan.ground - 1 ? Blocks.GRASS_BLOCK.defaultBlockState() : Blocks.DIRT.defaultBlockState();
    }

    /** Was der Architekt beim Wiedereinlesen wissen muss: die Bodenlinie (Lagen darunter) und dass vorn Norden ist. */
    static CompoundTag architect(PlanScan scan) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Version", 1);
        tag.putInt("Ground", scan.ground);
        tag.putString("Front", "north");
        return tag;
    }

    /** Unter diesem Namen steht die Angabe in den Metadaten (bei der Strukturdatei ganz oben). */
    static final String KEY = "KingdomsAtWar";

    /** Die Daten eines Blockobjekts mit seiner Kennung ({@code id}), oder {@code null}. */
    private static @Nullable CompoundTag blockEntity(PlanScan scan, int x, int y, int z) {
        CompoundTag data = scan.blockEntity(x, y, z);
        BlockState state = scan.get(x, y, z);
        if (data == null || state == null || !(state.getBlock() instanceof EntityBlock entityBlock)) {
            return null;
        }
        BlockEntity be = entityBlock.newBlockEntity(BlockPos.ZERO, state);
        Identifier id = be == null ? null : BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(be.getType());
        if (id == null) {
            return null;
        }
        CompoundTag out = data.copy();
        out.putString("id", id.toString());
        return out;
    }

    // =====================================================================================
    // Litematica
    // =====================================================================================

    public static CompoundTag litematic(PlanScan scan, String name, String author) {
        int sx = scan.sizeX;
        int sy = scan.sizeY;
        int sz = scan.sizeZ;
        Map<BlockState, Integer> ids = new IdentityHashMap<>();
        List<BlockState> palette = new ArrayList<>();
        palette.add(Blocks.AIR.defaultBlockState());
        ids.put(Blocks.AIR.defaultBlockState(), 0);
        int[] values = new int[sx * sy * sz];
        int blocks = 0;
        ListTag tiles = new ListTag();
        for (int y = 0; y < sy; y++) {
            for (int z = 0; z < sz; z++) {
                for (int x = 0; x < sx; x++) {
                    BlockState s = at(scan, x, y, z);
                    Integer id = ids.get(s);
                    if (id == null) {
                        id = palette.size();
                        ids.put(s, id);
                        palette.add(s);
                    }
                    values[(y * sz + z) * sx + x] = id;
                    if (!s.isAir()) {
                        blocks++;
                    }
                    CompoundTag be = blockEntity(scan, x, y, z);
                    if (be != null) {
                        be.putInt("x", x);
                        be.putInt("y", y);
                        be.putInt("z", z);
                        tiles.add(be);
                    }
                }
            }
        }
        int bits = Math.max(2, 32 - Integer.numberOfLeadingZeros(Math.max(1, palette.size() - 1)));
        long[] packed = new long[(int) (((long) values.length * bits + 63) / 64)];
        for (int i = 0; i < values.length; i++) {
            long start = (long) i * bits;
            int startArr = (int) (start >>> 6);
            int endArr = (int) ((start + bits - 1) >>> 6);
            int startBit = (int) (start & 63);
            long v = values[i] & ((1L << bits) - 1L);
            packed[startArr] |= v << startBit;
            if (startArr != endArr) {
                packed[endArr] |= v >>> (64 - startBit);
            }
        }
        ListTag paletteTag = new ListTag();
        for (BlockState s : palette) {
            paletteTag.add(NbtUtils.writeBlockState(s));
        }
        CompoundTag region = new CompoundTag();
        region.put("Position", xyz(0, 0, 0));
        region.put("Size", xyz(sx, sy, sz));
        region.put("BlockStatePalette", paletteTag);
        region.put("BlockStates", new LongArrayTag(packed));
        region.put("TileEntities", tiles);
        region.put("Entities", new ListTag());
        region.put("PendingBlockTicks", new ListTag());
        region.put("PendingFluidTicks", new ListTag());
        CompoundTag regions = new CompoundTag();
        regions.put(name.isBlank() ? "Bauplan" : name, region);
        long now = System.currentTimeMillis();
        CompoundTag meta = new CompoundTag();
        meta.putString("Name", name);
        meta.putString("Author", author);
        meta.putString("Description", "Kingdoms at War – Dragon Architect");
        meta.putInt("RegionCount", 1);
        meta.putInt("TotalVolume", sx * sy * sz);
        meta.putInt("TotalBlocks", blocks);
        meta.putLong("TimeCreated", now);
        meta.putLong("TimeModified", now);
        meta.put("EnclosingSize", xyz(sx, sy, sz));
        meta.put(KEY, architect(scan));
        CompoundTag root = new CompoundTag();
        root.putInt("Version", 6);
        root.putInt("SubVersion", 1);
        root.putInt("MinecraftDataVersion", dataVersion());
        root.put("Metadata", meta);
        root.put("Regions", regions);
        return root;
    }

    private static CompoundTag xyz(int x, int y, int z) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("x", x);
        tag.putInt("y", y);
        tag.putInt("z", z);
        return tag;
    }

    // =====================================================================================
    // Sponge-Schematik, Version 3
    // =====================================================================================

    public static CompoundTag sponge(PlanScan scan, String name, String author) {
        int sx = scan.sizeX;
        int sy = scan.sizeY;
        int sz = scan.sizeZ;
        Map<BlockState, Integer> ids = new IdentityHashMap<>();
        CompoundTag palette = new CompoundTag();
        java.io.ByteArrayOutputStream data = new java.io.ByteArrayOutputStream();
        ListTag entities = new ListTag();
        for (int y = 0; y < sy; y++) {
            for (int z = 0; z < sz; z++) {
                for (int x = 0; x < sx; x++) {
                    BlockState s = at(scan, x, y, z);
                    Integer id = ids.get(s);
                    if (id == null) {
                        id = ids.size();
                        ids.put(s, id);
                        palette.putInt(BlockStateParserText.of(s), id);
                    }
                    int v = id;
                    while ((v & ~0x7F) != 0) {
                        data.write((v & 0x7F) | 0x80);
                        v >>>= 7;
                    }
                    data.write(v);
                    CompoundTag be = blockEntity(scan, x, y, z);
                    if (be != null) {
                        CompoundTag entry = new CompoundTag();
                        entry.put("Pos", new IntArrayTag(new int[] {x, y, z}));
                        entry.putString("Id", be.getStringOr("id", ""));
                        be.remove("id");
                        entry.put("Data", be);
                        entities.add(entry);
                    }
                }
            }
        }
        CompoundTag blocks = new CompoundTag();
        blocks.put("Palette", palette);
        blocks.putByteArray("Data", data.toByteArray());
        blocks.put("BlockEntities", entities);
        CompoundTag meta = new CompoundTag();
        meta.putString("Name", name);
        meta.putString("Author", author);
        meta.putLong("Date", System.currentTimeMillis());
        meta.put(KEY, architect(scan));
        CompoundTag schematic = new CompoundTag();
        schematic.putInt("Version", 3);
        schematic.putInt("DataVersion", dataVersion());
        schematic.putShort("Width", (short) sx);
        schematic.putShort("Height", (short) sy);
        schematic.putShort("Length", (short) sz);
        schematic.put("Offset", new IntArrayTag(new int[] {0, 0, 0}));
        schematic.put("Metadata", meta);
        schematic.put("Blocks", blocks);
        CompoundTag root = new CompoundTag();
        root.put("Schematic", schematic);
        return root;
    }

    // =====================================================================================
    // Strukturdatei
    // =====================================================================================

    public static CompoundTag structure(PlanScan scan, String author) {
        Map<BlockState, Integer> ids = new IdentityHashMap<>();
        ListTag palette = new ListTag();
        ListTag blocks = new ListTag();
        for (int y = 0; y < scan.sizeY; y++) {
            for (int z = 0; z < scan.sizeZ; z++) {
                for (int x = 0; x < scan.sizeX; x++) {
                    BlockState s = scan.get(x, y, z);
                    if (s == null) {
                        continue; // Strukturleere: das Gelände bleibt
                    }
                    Integer id = ids.get(s);
                    if (id == null) {
                        id = palette.size();
                        ids.put(s, id);
                        palette.add(NbtUtils.writeBlockState(s));
                    }
                    CompoundTag block = new CompoundTag();
                    ListTag pos = new ListTag();
                    pos.add(IntTag.valueOf(x));
                    pos.add(IntTag.valueOf(y));
                    pos.add(IntTag.valueOf(z));
                    block.put("pos", pos);
                    block.putInt("state", id);
                    CompoundTag be = blockEntity(scan, x, y, z);
                    if (be != null) {
                        block.put("nbt", be);
                    }
                    blocks.add(block);
                }
            }
        }
        ListTag size = new ListTag();
        size.add(IntTag.valueOf(scan.sizeX));
        size.add(IntTag.valueOf(scan.sizeY));
        size.add(IntTag.valueOf(scan.sizeZ));
        CompoundTag root = new CompoundTag();
        root.putInt("DataVersion", dataVersion());
        root.put("size", size);
        root.put("palette", palette);
        root.put("blocks", blocks);
        root.put("entities", new ListTag());
        root.putString("author", author);
        root.put(KEY, architect(scan));
        return root;
    }

    /** Ein Block als Zeichenkette wie im Befehl {@code /setblock} ({@code minecraft:oak_stairs[facing=east,half=bottom]}). */
    static final class BlockStateParserText {
        private BlockStateParserText() {}

        static String of(BlockState state) {
            StringBuilder out = new StringBuilder(BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());
            if (!state.getProperties().isEmpty()) {
                out.append('[');
                boolean first = true;
                for (var property : state.getProperties()) {
                    if (!first) {
                        out.append(',');
                    }
                    first = false;
                    out.append(property.getName()).append('=').append(name(state, property));
                }
                out.append(']');
            }
            return out.toString().toLowerCase(Locale.ROOT);
        }

        private static <T extends Comparable<T>> String name(BlockState state, net.minecraft.world.level.block.state.properties.Property<T> property) {
            return property.getName(state.getValue(property));
        }
    }
}
