package io.github.sirschrolli.kingdomsatwar.architect.io;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.architect.PlanInfo;
import io.github.sirschrolli.kingdomsatwar.architect.PlanLibrary;
import io.github.sirschrolli.kingdomsatwar.architect.PlanScan;
import io.github.sirschrolli.kingdomsatwar.architect.castle.CastleGenerator;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import org.jspecify.annotations.Nullable;

/**
 * Die Schematik-Ordner des Dragon Architect: Dateien zum Einlesen finden (im Ordner {@code schematics} des Spiels wie bei
 * Litematica, bei WorldEdit unter {@code config/worldedit/schematics} und die Strukturen des Strukturblocks im Weltordner)
 * und eigene Pläne als Schematik ablegen ({@code schematics/kingdomsatwar}). Nichts außerhalb dieser Ordner wird gelesen.
 */
public final class SchematicFiles {
    /** Höchstens so viele Dateien listet das Stadtfenster. */
    private static final int MAX_FILES = 200;
    /** So tief sucht die Liste in Unterordnern. */
    private static final int MAX_DEPTH = 4;
    /** Größte Datei, die eingelesen wird (gepackt). */
    private static final long MAX_FILE = 32L * 1024L * 1024L;

    private SchematicFiles() {}

    /** Eine Datei zum Einlesen: Kennung (Ordner-Nummer und Weg darin), Anzeigename, Art und Größe in Bytes. */
    public record Entry(String id, String name, String folder, String format, long bytes) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Entry> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, Entry::id,
                ByteBufCodecs.STRING_UTF8, Entry::name,
                ByteBufCodecs.STRING_UTF8, Entry::folder,
                ByteBufCodecs.STRING_UTF8, Entry::format,
                ByteBufCodecs.VAR_LONG, Entry::bytes,
                Entry::new);
    }

    /** Die Ordner, aus denen gelesen wird (in dieser Reihenfolge; die Nummer steht in der Kennung). */
    static List<Path> roots(MinecraftServer server) {
        Path game = server.getServerDirectory();
        return List.of(game.resolve("schematics"), game.resolve("config").resolve("worldedit").resolve("schematics"),
                server.getWorldPath(LevelResource.GENERATED_DIR));
    }

    /** Wohin eigene Pläne geschrieben werden. */
    public static Path exportDir(MinecraftServer server) {
        return server.getServerDirectory().resolve("schematics").resolve(KingdomsAtWar.MODID);
    }

    /** Wie die Ordner im Fenster heißen. */
    private static final String[] FOLDERS = {"schematics", "worldedit", "structures"};

    /** Kennung der eingebauten Königsburg in der Liste (keine Datei: der Architekt entwirft sie beim Einlesen). */
    public static final String ROYAL_CASTLE = "builtin:royal_castle";
    /** Art der eingebauten Pläne in der Liste. */
    public static final String BUILTIN = "builtin";

    /**
     * Alle lesbaren Dateien (nach Namen sortiert; auch die eigenen, die der Architekt abgelegt hat), davor die eingebauten
     * Pläne des Dragon Architect.
     */
    public static List<Entry> list(MinecraftServer server) {
        List<Entry> out = new ArrayList<>();
        out.add(new Entry(ROYAL_CASTLE, "Königsburg", "Dragon Architect", BUILTIN, 0L));
        List<Path> roots = roots(server);
        for (int r = 0; r < roots.size(); r++) {
            Path root = roots.get(r).normalize();
            if (!Files.isDirectory(root)) {
                continue;
            }
            try (Stream<Path> walk = Files.walk(root, MAX_DEPTH)) {
                for (Path file : (Iterable<Path>) walk::iterator) {
                    if (out.size() >= MAX_FILES) {
                        break;
                    }
                    SchematicReader.Format format = SchematicReader.Format.of(file.getFileName().toString());
                    if (format == null || !Files.isRegularFile(file)) {
                        continue;
                    }
                    if (r == 2 && format != SchematicReader.Format.NBT) {
                        continue;
                    }
                    String relative = root.relativize(file.normalize()).toString().replace('\\', '/');
                    String name = file.getFileName().toString();
                    name = name.substring(0, name.length() - format.extension.length());
                    out.add(new Entry(r + ":" + relative, name, FOLDERS[r], format.name().toLowerCase(Locale.ROOT), Files.size(file)));
                }
            } catch (IOException | RuntimeException e) {
                KingdomsAtWar.LOGGER.warn("Schematik-Ordner {} lässt sich nicht lesen", root, e);
            }
        }
        out.subList(1, out.size()).sort((a, b) -> a.name().compareToIgnoreCase(b.name()));
        return out;
    }

    /** Die Datei zu einer Kennung – nur innerhalb der erlaubten Ordner ({@code null}: gibt es nicht oder nicht erlaubt). */
    static @Nullable Path resolve(MinecraftServer server, String id) {
        int colon = id.indexOf(':');
        if (colon <= 0) {
            return null;
        }
        int r;
        try {
            r = Integer.parseInt(id.substring(0, colon));
        } catch (NumberFormatException e) {
            return null;
        }
        List<Path> roots = roots(server);
        if (r < 0 || r >= roots.size()) {
            return null;
        }
        String relative = id.substring(colon + 1);
        if (relative.isEmpty() || relative.contains("..") || relative.startsWith("/") || relative.contains(":") || relative.contains("\\")) {
            return null;
        }
        Path root = roots.get(r).normalize();
        Path file = root.resolve(relative).normalize();
        if (!file.startsWith(root) || !Files.isRegularFile(file) || SchematicReader.Format.of(file.getFileName().toString()) == null) {
            return null;
        }
        return file;
    }

    /** Was beim Einlesen herauskam: der neue Plan (oder {@code null}) und warum nicht. */
    public record Imported(@Nullable PlanInfo plan, String error, int unknown) {}

    /** Liest eine Datei als eigenen Plan des Spielers ein. */
    public static Imported importFile(ServerPlayer player, String id) {
        MinecraftServer server = player.level().getServer();
        if (id.equals(ROYAL_CASTLE)) {
            return royalCastle(player);
        }
        Path file = resolve(server, id);
        if (file == null) {
            return new Imported(null, "missing", 0);
        }
        PlanLibrary library = PlanLibrary.of(server);
        if (library.byAuthor(player.getUUID()).size() >= PlanLibrary.MAX_PER_PLAYER) {
            return new Imported(null, "too_many", 0);
        }
        try {
            if (Files.size(file) > MAX_FILE) {
                return new Imported(null, "too_large", 0);
            }
            SchematicReader.Result result = SchematicReader.read(file, player.level().registryAccess());
            Volume v = result.volume();
            if (Math.max(v.sizeX, v.sizeZ) > PlanScan.MAX_WIDTH || v.sizeY > PlanScan.MAX_HEIGHT) {
                return new Imported(null, "too_large", result.unknown());
            }
            PlanScan scan = toScan(v, result.ground(), player.level().registryAccess());
            String name = result.name().isBlank() ? file.getFileName().toString().replaceFirst("\\.[^.]+$", "") : result.name();
            PlanInfo info = library.add(server, scan, name, player.getUUID(), player.getName().getString());
            return new Imported(info, "", result.unknown());
        } catch (IllegalArgumentException e) {
            return new Imported(null, "empty", 0);
        } catch (IOException | RuntimeException e) {
            KingdomsAtWar.LOGGER.warn("Schematik {} lässt sich nicht einlesen", file, e);
            return new Imported(null, String.valueOf(e.getMessage()).startsWith("too large") ? "too_large" : "broken", 0);
        }
    }

    /**
     * Die eingebaute Königsburg als eigener Plan des Spielers: der Architekt entwirft sie (300 × 300 Blöcke) und vermisst
     * sie wie eine eingelesene Schematik; der Name richtet sich nach der Sprache des Spielers.
     */
    private static Imported royalCastle(ServerPlayer player) {
        MinecraftServer server = player.level().getServer();
        PlanLibrary library = PlanLibrary.of(server);
        if (library.byAuthor(player.getUUID()).size() >= PlanLibrary.MAX_PER_PLAYER) {
            return new Imported(null, "too_many", 0);
        }
        try {
            CastleGenerator.Castle castle = CastleGenerator.royalCastle();
            PlanScan scan = toScan(castle.volume(), castle.ground(), player.level().registryAccess());
            String name = player.clientInformation().language().startsWith("de") ? "Königsburg" : "Royal Castle";
            PlanInfo info = library.add(server, scan, name, player.getUUID(), player.getName().getString());
            return new Imported(info, "", 0);
        } catch (IOException | RuntimeException e) {
            KingdomsAtWar.LOGGER.warn("Die Königsburg lässt sich nicht anlegen", e);
            return new Imported(null, "broken", 0);
        }
    }

    /** Vermisst ein eingelesenes Volumen wie ein Gebäude in der Welt (Bodenlinie und Vorderseite von selbst). */
    public static PlanScan toScan(Volume v, net.minecraft.core.HolderLookup.Provider registries) {
        return toScan(v, -1, registries);
    }

    /**
     * Vermisst ein eingelesenes Volumen. Kennt die Datei ihre Bodenlinie (vom Dragon Architect geschrieben), gilt sie und
     * vorn ist Norden; sonst sucht der Architekt beides selbst – leere Lagen ganz unten (nur Luft) zählen dann nicht mit.
     */
    public static PlanScan toScan(Volume v, int ground, net.minecraft.core.HolderLookup.Provider registries) {
        BlockPos b = new BlockPos(v.sizeX - 1, v.sizeY - 1, v.sizeZ - 1);
        if (ground >= 0) {
            return PlanScan.scan(v, BlockPos.ZERO, b, ground, Direction.NORTH, registries);
        }
        int lowest = v.lowestLayer();
        if (lowest < 0) {
            throw new IllegalArgumentException("empty");
        }
        BlockPos a = new BlockPos(0, lowest, 0);
        int found = PlanScan.autoGround(v, a, b);
        Direction door = PlanScan.doorSide(v, a, b, found, null);
        return PlanScan.scan(v, a, b, found, door != null ? door : Direction.NORTH, registries);
    }

    /** Legt einen eigenen Plan als .litematic, .schem und .nbt ab; gibt die Dateien zurück (leer: ging nicht). */
    public static List<Path> export(ServerPlayer player, PlanInfo info) {
        MinecraftServer server = player.level().getServer();
        PlanScan scan = PlanLibrary.load(server, info.id());
        if (scan == null) {
            return List.of();
        }
        try {
            return SchematicWriter.writeAll(scan, info.name(), info.authorName(), exportDir(server));
        } catch (IOException | RuntimeException e) {
            KingdomsAtWar.LOGGER.warn("Bauplan {} lässt sich nicht als Schematik ablegen", info.id(), e);
            return List.of();
        }
    }
}
