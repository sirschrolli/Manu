package io.github.sirschrolli.kingdomsatwar.rules;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;
import java.util.zip.ZipFile;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.ModLoadingIssue;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforgespi.language.IModInfo;
import net.neoforged.neoforgespi.locating.IModFile;
import org.jspecify.annotations.Nullable;

/**
 * Kingdoms at War wird ohne andere Mods gespielt: Ist neben Minecraft und NeoForge noch eine andere Mod installiert (oder
 * liegt sonst eine fremde Datei im Mod-Ordner), bricht das Spiel den Start mit einer Liste der fremden Mods ab. Gelöscht
 * wird nichts – wer spielen will, entfernt die Mods selbst aus dem Mod-Ordner.
 *
 * <p>Die eine Ausnahme ist <b>Essential</b> (essential.gg): Damit lädt man Freunde in die eigene Welt ein. Essential liegt
 * als kleiner Lader im Mod-Ordner und holt sich die eigentliche Mod in den Ordner {@code essential} des Spiels; erlaubt
 * sind der Lader, Essential selbst und was in dessen Datei steckt. Mods, die andere über den Lader von Essential
 * nachladen lassen, bleiben fremde Mods.
 */
public final class ModGuard {
    /** Was neben dieser Mod geladen sein darf: das Spiel selbst, der Mod-Lader und Essential (für Welten mit Freunden). */
    public static final Set<String> ALLOWED = Set.of("minecraft", "neoforge", KingdomsAtWar.MODID, "essential");

    /** Kennung von Essential (essential.gg). */
    static final String ESSENTIAL = "essential";
    /** Woran der Lader von Essential im Mod-Ordner zu erkennen ist (an seinem Inhalt, nicht am Dateinamen). */
    static final String ESSENTIAL_LOADER = "gg/essential/loader/stage0/";

    /**
     * Die Meldung im Ladefehler-Bildschirm (zweisprachig: der Mod-Lader kennt nur seine eigenen Übersetzungen und zeigt
     * unbekannte Schlüssel wörtlich an).
     */
    static final String MESSAGE = "§6Kingdoms at War§r läuft nur ohne andere Mods (erlaubt ist Essential für Welten mit Freunden). "
            + "Bitte entferne aus dem Mod-Ordner: §e{0}§r\n"
            + "§7Kingdoms at War can only be played without other mods (Essential for playing with friends is allowed). "
            + "Please remove from the mods folder: {0}";

    private ModGuard() {}

    /** Beim Laden der Mod: fremde Mods melden (der Start bricht dann mit dieser Meldung ab). */
    public static void check(ModContainer self) {
        List<String> foreign = new ArrayList<>();
        List<Path> allowedFiles = new ArrayList<>();
        allowedFiles.add(self.getModInfo().getOwningFile().getFile().getFilePath());
        for (IModInfo mod : ModList.get().getMods()) {
            IModFile file = mod.getOwningFile() == null ? null : mod.getOwningFile().getFile();
            if (ALLOWED.contains(mod.getModId()) || partOfEssential(file)) {
                if (file != null && file.getFilePath() != null) {
                    allowedFiles.add(file.getFilePath());
                }
            } else {
                foreign.add(mod.getDisplayName() + " (" + mod.getModId() + ")");
            }
        }
        if (foreign.isEmpty()) {
            // keine fremden Mods geladen – aber vielleicht fremde Bibliotheken oder Kernmods im Mod-Ordner
            foreign.addAll(foreignFiles(FMLPaths.MODSDIR.get(), allowedFiles));
        }
        if (!foreign.isEmpty()) {
            String list = String.join(", ", foreign);
            KingdomsAtWar.LOGGER.error("Kingdoms at War läuft nur ohne andere Mods – gefunden: {}", list);
            ModLoader.addLoadingIssue(ModLoadingIssue.error(MESSAGE, list));
        }
    }

    /** Die fremden Kennungen in dieser Liste (alles außer Minecraft, NeoForge, dieser Mod und Essential), sortiert. */
    public static List<String> foreignIds(Collection<String> modIds) {
        return modIds.stream().filter(id -> !ALLOWED.contains(id)).sorted().toList();
    }

    /** Fremde Dateien im Mod-Ordner: jede Mod-Datei außer der eigenen (ausgeschaltete Dateien zählen nicht). */
    public static List<String> foreignFiles(@Nullable Path modsDir, @Nullable Path own) {
        return foreignFiles(modsDir, own == null ? List.of() : List.of(own));
    }

    /**
     * Fremde Dateien im Mod-Ordner: jede Mod-Datei außer den erlaubten (die eigene, die von Essential) und dem Lader von
     * Essential; ausgeschaltete Dateien zählen nicht.
     */
    public static List<String> foreignFiles(@Nullable Path modsDir, Collection<Path> allowed) {
        if (modsDir == null || !Files.isDirectory(modsDir)) {
            return List.of();
        }
        List<Path> known = allowed.stream().filter(java.util.Objects::nonNull).map(path -> path.toAbsolutePath().normalize()).toList();
        try (Stream<Path> files = Files.list(modsDir)) {
            return files.filter(Files::isRegularFile)
                    .filter(path -> {
                        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
                        return name.endsWith(".jar") || name.endsWith(".zip");
                    })
                    .filter(path -> known.stream().noneMatch(self -> path.toAbsolutePath().normalize().equals(self)
                            || self.getFileName() != null && path.getFileName().toString().equals(self.getFileName().toString())))
                    .filter(path -> !essentialLoader(path))
                    .map(path -> path.getFileName().toString())
                    .sorted()
                    .toList();
        } catch (IOException e) {
            KingdomsAtWar.LOGGER.warn("Mod-Ordner nicht lesbar: {}", modsDir, e);
            return List.of();
        }
    }

    /**
     * Gehört diese Mod-Datei zu Essential? Ja, wenn sie selbst oder die Datei, in der sie steckt, Essential enthält oder
     * die eigentliche Mod aus dem Ordner {@code essential} des Spiels ist ({@code Essential (neoforge_…).jar}).
     */
    public static boolean partOfEssential(@Nullable IModFile file) {
        for (IModFile f = file; f != null; f = f.getDiscoveryAttributes() == null ? null : f.getDiscoveryAttributes().parent()) {
            if (f.getModInfos().stream().anyMatch(info -> ESSENTIAL.equals(info.getModId())) || essentialJar(f.getFilePath())) {
                return true;
            }
        }
        return false;
    }

    /** Die eigentliche Mod Essential, wie ihr Lader sie ablegt: {@code <Spielordner>/essential/Essential (…).jar}. */
    public static boolean essentialJar(@Nullable Path path) {
        if (path == null || path.getFileName() == null || path.getParent() == null) {
            return false;
        }
        try {
            Path folder = FMLPaths.GAMEDIR.get().resolve(ESSENTIAL).toAbsolutePath().normalize();
            return path.getFileName().toString().startsWith("Essential (") && path.toAbsolutePath().normalize().getParent().equals(folder);
        } catch (RuntimeException e) {
            return false; // Pfad aus einem anderen Dateisystem (z. B. in einer Jar-Datei)
        }
    }

    /** Ist diese Datei der Lader von Essential? (Erkannt an seinen Klassen – umbenannte fremde Mods fallen weiter auf.) */
    public static boolean essentialLoader(Path jar) {
        try (ZipFile zip = new ZipFile(jar.toFile())) {
            return zip.stream().anyMatch(entry -> entry.getName().startsWith(ESSENTIAL_LOADER));
        } catch (IOException | RuntimeException e) {
            return false;
        }
    }
}
