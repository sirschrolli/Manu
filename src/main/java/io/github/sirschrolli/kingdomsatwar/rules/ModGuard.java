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
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.ModLoadingIssue;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforgespi.language.IModInfo;
import org.jspecify.annotations.Nullable;

/**
 * Kingdoms at War wird allein gespielt: Ist neben Minecraft und NeoForge noch eine andere Mod installiert (oder liegt
 * sonst eine fremde Datei im Mod-Ordner), bricht das Spiel den Start mit einer Liste der fremden Mods ab. Gelöscht wird
 * nichts – wer spielen will, entfernt die Mods selbst aus dem Mod-Ordner.
 */
public final class ModGuard {
    /** Was neben dieser Mod geladen sein darf: das Spiel selbst und der Mod-Lader. */
    public static final Set<String> ALLOWED = Set.of("minecraft", "neoforge", KingdomsAtWar.MODID);

    /**
     * Die Meldung im Ladefehler-Bildschirm (zweisprachig: der Mod-Lader kennt nur seine eigenen Übersetzungen und zeigt
     * unbekannte Schlüssel wörtlich an).
     */
    static final String MESSAGE = "§6Kingdoms at War§r läuft nur ohne andere Mods. Bitte entferne aus dem Mod-Ordner: §e{0}§r\n"
            + "§7Kingdoms at War can only be played without other mods. Please remove from the mods folder: {0}";

    private ModGuard() {}

    /** Beim Laden der Mod: fremde Mods melden (der Start bricht dann mit dieser Meldung ab). */
    public static void check(ModContainer self) {
        List<String> foreign = new ArrayList<>();
        for (IModInfo mod : ModList.get().getMods()) {
            if (!ALLOWED.contains(mod.getModId())) {
                foreign.add(mod.getDisplayName() + " (" + mod.getModId() + ")");
            }
        }
        if (foreign.isEmpty()) {
            // keine fremden Mods geladen – aber vielleicht fremde Bibliotheken oder Kernmods im Mod-Ordner
            foreign.addAll(foreignFiles(FMLPaths.MODSDIR.get(), self.getModInfo().getOwningFile().getFile().getFilePath()));
        }
        if (!foreign.isEmpty()) {
            String list = String.join(", ", foreign);
            KingdomsAtWar.LOGGER.error("Kingdoms at War läuft nur ohne andere Mods – gefunden: {}", list);
            ModLoader.addLoadingIssue(ModLoadingIssue.error(MESSAGE, list));
        }
    }

    /** Die fremden Kennungen in dieser Liste (alles außer Minecraft, NeoForge und dieser Mod), sortiert. */
    public static List<String> foreignIds(Collection<String> modIds) {
        return modIds.stream().filter(id -> !ALLOWED.contains(id)).sorted().toList();
    }

    /** Fremde Dateien im Mod-Ordner: jede Mod-Datei außer der eigenen (ausgeschaltete Dateien zählen nicht). */
    public static List<String> foreignFiles(@Nullable Path modsDir, @Nullable Path own) {
        if (modsDir == null || !Files.isDirectory(modsDir)) {
            return List.of();
        }
        Path self = own == null ? null : own.toAbsolutePath().normalize();
        try (Stream<Path> files = Files.list(modsDir)) {
            return files.filter(Files::isRegularFile)
                    .filter(path -> {
                        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
                        return name.endsWith(".jar") || name.endsWith(".zip");
                    })
                    .filter(path -> self == null || !(path.toAbsolutePath().normalize().equals(self)
                            || self.getFileName() != null && path.getFileName().toString().equals(self.getFileName().toString())))
                    .map(path -> path.getFileName().toString())
                    .sorted()
                    .toList();
        } catch (IOException e) {
            KingdomsAtWar.LOGGER.warn("Mod-Ordner nicht lesbar: {}", modsDir, e);
            return List.of();
        }
    }
}
