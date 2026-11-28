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
 * Kingdoms at War wird nur mit freigegebenen Mods gespielt: Ist neben Minecraft und NeoForge noch eine andere Mod
 * installiert (oder liegt sonst eine fremde Datei im Mod-Ordner), bricht das Spiel den Start mit einer Liste der fremden
 * Mods ab. Gelöscht wird nichts – wer spielen will, entfernt die Mods selbst aus dem Mod-Ordner.
 *
 * <p>Freigegeben sind {@link #APPROVED}: <b>Essential</b> (essential.gg – damit lädt man Freunde in die eigene Welt ein)
 * und seit 1.11.7 Mods für Anzeige, Bedienung und Leistung, die das Spiel nicht leichter machen – samt den Bibliotheken,
 * die sie brauchen (Searchables für Controlling, Prickle für Enchantment Descriptions, libIPN und Kotlin für Inventory
 * Profiles Next, die Config Lib von SuperMartijn642 für Durability Tooltip). Seit 1.14 kommen Mouse Tweaks, uku's Armor HUD
 * (mit ukulib), Roughly Enough Items, SpruceUI, Zoomify (mit YetAnotherConfigLib), Swinging Lanterns, ShulkerBoxTooltip
 * und LambDynamicLights dazu. Erkannt wird eine Mod an ihrer Kennung oder
 * an ihrem Namen (ohne Groß- und Kleinschreibung, Leer- und Sonderzeichen); was in der Datei einer freigegebenen Mod
 * steckt, ist mit ihr freigegeben.
 *
 * <p>Essential liegt als kleiner Lader im Mod-Ordner und holt sich die eigentliche Mod in den Ordner {@code essential}
 * des Spiels; erlaubt sind der Lader, Essential selbst und was in dessen Datei steckt. Mods, die andere über den Lader von
 * Essential nachladen lassen, bleiben fremde Mods.
 */
public final class ModGuard {
    /** Eine freigegebene Mod: ihr Name und die Kennungen, unter denen sie (und die Bibliotheken, die sie braucht) lädt. */
    public record Approved(String name, List<String> ids) {
        public Approved(String name, String... ids) {
            this(name, List.of(ids));
        }
    }

    /** Die freigegebenen Mods (seit 1.11.7; bis dahin nur Essential; seit 1.14 noch acht weitere). */
    public static final List<Approved> APPROVED = List.of(
            new Approved("Essential", "essential", "essentialmod"),
            new Approved("3D Skin Layers", "skinlayers3d"),
            new Approved("AppleSkin", "appleskin"),
            new Approved("Architectury API", "architectury"),
            new Approved("Armor Durability HUD", "armorhud", "armor_hud", "armordurabilityhud", "armordurabilityhuddisplay"),
            new Approved("Cloth Config API", "cloth_config"),
            new Approved("Clumps", "clumps"),
            new Approved("Controlling", "controlling", "searchables"),
            new Approved("Distant Horizons", "distanthorizons"),
            new Approved("Durability Tooltip", "durabilitytooltip", "supermartijn642configlib", "supermartijn642corelib"),
            new Approved("Effect Timer Plus", "effecttimerplus"),
            new Approved("Enchantment Descriptions", "enchdesc", "prickle", "bookshelf"),
            new Approved("Entity Culling", "entityculling"),
            new Approved("FerriteCore", "ferritecore"),
            new Approved("Inventory Profiles Next", "inventoryprofilesnext", "libipn"),
            new Approved("InventoryHUD+", "inventoryhud", "inventoryhudplus"),
            new Approved("InvMove", "invmove", "invmove_compat"),
            new Approved("Jade", "jade"),
            new Approved("KotlinLangForge", "klf", "kotlinforforge"),
            // seit 1.14
            new Approved("LambDynamicLights", "lambdynlights", "lambdynlights_api", "lambdynamiclights", "yumi_mc_core", "yumi_mc_foundation",
                    "pridelib"),
            new Approved("Mouse Tweaks", "mousetweaks"),
            new Approved("Roughly Enough Items", "roughlyenoughitems", "Roughly Enough Items (REI)", "rei_plugin_compatibilities"),
            new Approved("ShulkerBoxTooltip", "shulkerboxtooltip"),
            new Approved("SpruceUI", "spruceui"),
            new Approved("Swinging Lanterns", "swinginglanterns", "swinging_lanterns"),
            new Approved("uku's Armor HUD", "ukus_armor_hud", "ukulib"),
            new Approved("Zoomify", "zoomify", "yet_another_config_lib_v3", "yacl", "YetAnotherConfigLib"));

    /** Was immer geladen sein darf: das Spiel selbst, der Mod-Lader, diese Mod – und MixinExtras, das in vielen Mods steckt. */
    static final Set<String> BASE = Set.of("minecraft", "neoforge", KingdomsAtWar.MODID, "mixinextras");

    /** Woran eine freigegebene Mod zu erkennen ist: ihre Kennungen und ihr Name, als {@link #key}. */
    private static final Set<String> KEYS = APPROVED.stream()
            .flatMap(mod -> Stream.concat(Stream.of(mod.name()), mod.ids().stream()))
            .map(ModGuard::key)
            .collect(java.util.stream.Collectors.toUnmodifiableSet());

    /** Kennung von Essential (essential.gg) – auch der Name seines Ordners im Spielordner. */
    static final String ESSENTIAL = "essential";
    /** Woran der Lader von Essential im Mod-Ordner zu erkennen ist (an seinem Inhalt, nicht am Dateinamen). */
    static final String ESSENTIAL_LOADER = "gg/essential/loader/stage0/";

    /**
     * Die Meldung im Ladefehler-Bildschirm (zweisprachig: der Mod-Lader kennt nur seine eigenen Übersetzungen und zeigt
     * unbekannte Schlüssel wörtlich an).
     */
    static final String MESSAGE = "§6Kingdoms at War§r läuft nur mit freigegebenen Mods ({1}). "
            + "Bitte entferne aus dem Mod-Ordner: §e{0}§r\n"
            + "§7Kingdoms at War can only be played with approved mods ({1}). "
            + "Please remove from the mods folder: {0}";

    private ModGuard() {}

    /** Beim Laden der Mod: fremde Mods melden (der Start bricht dann mit dieser Meldung ab). */
    public static void check(ModContainer self) {
        List<String> foreign = new ArrayList<>();
        List<Path> allowedFiles = new ArrayList<>();
        allowedFiles.add(self.getModInfo().getOwningFile().getFile().getFilePath());
        for (IModInfo mod : ModList.get().getMods()) {
            IModFile file = mod.getOwningFile() == null ? null : mod.getOwningFile().getFile();
            if (approved(mod.getModId(), mod.getDisplayName()) || partOfApproved(file)) {
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
            KingdomsAtWar.LOGGER.error("Kingdoms at War läuft nur mit freigegebenen Mods – fremd: {}", list);
            ModLoader.addLoadingIssue(ModLoadingIssue.error(MESSAGE, list, approvedNames()));
        }
    }

    /** Die Namen der freigegebenen Mods, für die Meldung. */
    public static String approvedNames() {
        return String.join(", ", APPROVED.stream().map(Approved::name).toList());
    }

    /** Ist diese Mod freigegeben? (An ihrer Kennung oder an ihrem Namen erkannt; das Spiel und diese Mod sind es immer.) */
    public static boolean approved(String modId, @Nullable String displayName) {
        return BASE.contains(modId) || KEYS.contains(key(modId)) || displayName != null && KEYS.contains(key(displayName));
    }

    /** Kennung oder Name ohne Groß- und Kleinschreibung, Leer- und Sonderzeichen („InventoryHUD+“ → „inventoryhud“). */
    static String key(String text) {
        return text.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    /** Die fremden Kennungen in dieser Liste (alles, was nicht freigegeben ist), sortiert. */
    public static List<String> foreignIds(Collection<String> modIds) {
        return modIds.stream().filter(id -> !approved(id, null)).sorted().toList();
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
     * Steckt diese Mod-Datei in einer freigegebenen Mod? Ja, wenn sie selbst oder die Datei, in der sie steckt, eine
     * freigegebene Mod enthält (so kommen deren eingebaute Bibliotheken mit) oder die eigentliche Mod Essential aus dem
     * Ordner {@code essential} des Spiels ist ({@code Essential (neoforge_…).jar}).
     */
    public static boolean partOfApproved(@Nullable IModFile file) {
        for (IModFile f = file; f != null; f = f.getDiscoveryAttributes() == null ? null : f.getDiscoveryAttributes().parent()) {
            if (essentialJar(f.getFilePath()) || f.getModInfos().stream().anyMatch(info -> !BASE.contains(info.getModId())
                    && approved(info.getModId(), info.getDisplayName()))) {
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
