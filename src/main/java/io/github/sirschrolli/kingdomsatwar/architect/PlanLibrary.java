package io.github.sirschrolli.kingdomsatwar.architect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.economy.town.Blueprint;
import io.github.sirschrolli.kingdomsatwar.registry.ModAttachments;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jspecify.annotations.Nullable;

/**
 * Das Verzeichnis der eigenen Baupläne einer Welt (Dragon Architect). Die Kurzfassungen ({@link PlanInfo}) hängen an
 * der Oberwelt und werden mit dem Spielstand gespeichert; die Blöcke jedes Plans liegen in einer eigenen Datei im
 * Weltordner ({@code kingdomsatwar/plans/<id>.nbt}, gepackt). Ein gelöschter Plan verschwindet nur aus der Liste – Häuser
 * und Baustellen, die nach ihm gebaut sind oder werden, finden ihn weiter.
 */
public final class PlanLibrary {
    public static final Codec<PlanLibrary> CODEC = RecordCodecBuilder.create(i -> i.group(
            PlanInfo.CODEC.listOf().optionalFieldOf("plans", List.of()).forGetter(l -> List.copyOf(l.plans)),
            Codec.STRING.listOf().optionalFieldOf("hidden", List.of()).forGetter(l -> List.copyOf(l.hidden))
    ).apply(i, PlanLibrary::new));

    /** Höchstens so viele Pläne zeigt das Stadtfenster je Spieler. */
    public static final int MAX_PER_PLAYER = 64;

    private final List<PlanInfo> plans = new ArrayList<>();
    private final Set<String> hidden = new HashSet<>();

    public PlanLibrary() {
    }

    private PlanLibrary(List<PlanInfo> plans, List<String> hidden) {
        this.plans.addAll(plans);
        this.hidden.addAll(hidden);
    }

    public static PlanLibrary of(MinecraftServer server) {
        return server.overworld().getData(ModAttachments.PLANS);
    }

    /** Merkt die Änderungen zum Speichern vor. */
    public void save(MinecraftServer server) {
        server.overworld().setData(ModAttachments.PLANS, this);
    }

    public @Nullable PlanInfo info(String id) {
        for (PlanInfo p : this.plans) {
            if (p.id().equals(id)) {
                return p;
            }
        }
        return null;
    }

    /** Die (nicht gelöschten) Pläne eines Spielers, die neuesten zuerst. */
    public List<PlanInfo> byAuthor(UUID author) {
        List<PlanInfo> out = new ArrayList<>();
        for (int i = this.plans.size() - 1; i >= 0 && out.size() < MAX_PER_PLAYER; i--) {
            PlanInfo p = this.plans.get(i);
            if (p.author().equals(author) && !this.hidden.contains(p.id())) {
                out.add(p);
            }
        }
        return out;
    }

    public boolean hidden(String id) {
        return this.hidden.contains(id);
    }

    /** Nimmt einen Plan aus der Liste (die Datei bleibt für Häuser, die nach ihm gebaut sind). */
    public void hide(MinecraftServer server, String id) {
        if (this.hidden.add(id)) {
            this.save(server);
        }
    }

    public void rename(MinecraftServer server, String id, String name) {
        for (int i = 0; i < this.plans.size(); i++) {
            if (this.plans.get(i).id().equals(id)) {
                this.plans.set(i, this.plans.get(i).renamed(name));
                this.save(server);
            }
        }
    }

    /** Speichert eine Vermessung als neuen Plan (Datei und Verzeichnis). */
    public PlanInfo add(MinecraftServer server, PlanScan scan, String name, UUID author, String authorName) throws IOException {
        String id = this.newId();
        Path file = file(server, id);
        Files.createDirectories(file.getParent());
        NbtIo.writeCompressed(scan.save(), file);
        PlanInfo info = PlanInfo.of(id, clean(name), author, authorName, System.currentTimeMillis(), scan);
        this.plans.add(info);
        this.save(server);
        CACHE.put(key(server, id), scan.toBlueprint());
        return info;
    }

    private String newId() {
        String id;
        do {
            id = Long.toString(System.currentTimeMillis(), 36) + Integer.toString(java.util.concurrent.ThreadLocalRandom.current().nextInt(36 * 36), 36);
        } while (this.info(id) != null);
        return id.toLowerCase(Locale.ROOT);
    }

    /** Ein ordentlicher Name: ohne Steuerzeichen, gekürzt, nie leer. */
    public static String clean(String name) {
        String s = name == null ? "" : name.replaceAll("[\\p{Cntrl}§]", "").strip();
        if (s.length() > PlanInfo.MAX_NAME) {
            s = s.substring(0, PlanInfo.MAX_NAME).strip();
        }
        return s.isEmpty() ? "?" : s;
    }

    // =====================================================================================
    // Dateien
    // =====================================================================================

    private static Path dir(MinecraftServer server) {
        return server.getWorldPath(LevelResource.ROOT).resolve(KingdomsAtWar.MODID).resolve("plans");
    }

    private static Path file(MinecraftServer server, String id) {
        if (!id.matches("[a-z0-9]+")) {
            throw new IllegalArgumentException("bad plan id " + id);
        }
        return dir(server).resolve(id + ".nbt");
    }

    /** Lädt die Blöcke eines Plans aus seiner Datei ({@code null}: gibt es nicht). */
    public static @Nullable PlanScan load(MinecraftServer server, String id) {
        try {
            Path file = file(server, id);
            if (!Files.isRegularFile(file)) {
                return null;
            }
            CompoundTag tag = NbtIo.readCompressed(file, NbtAccounter.create(256L * 1024L * 1024L));
            return PlanScan.load(tag);
        } catch (IOException | RuntimeException e) {
            KingdomsAtWar.LOGGER.warn("Bauplan {} lässt sich nicht laden", id, e);
            return null;
        }
    }

    // =====================================================================================
    // Baupläne für den Baumeister (zwischengespeichert)
    // =====================================================================================

    private static final int CACHE_SIZE = 24;
    private static final Map<String, Blueprint> CACHE = new LinkedHashMap<>(32, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Blueprint> eldest) {
            return this.size() > CACHE_SIZE;
        }
    };
    /** Pläne, die sich nicht laden ließen (damit nicht jeder Zugriff die Platte fragt). */
    private static final Set<String> MISSING = new HashSet<>();
    private static final Blueprint EMPTY = Blueprint.custom(Map.of(), Map.of(), List.of(),
            net.minecraft.world.level.block.Blocks.COBBLESTONE.defaultBlockState(), List.of(), new net.minecraft.core.BlockPos(0, 0, -1),
            new net.minecraft.world.level.levelgen.structure.BoundingBox(0, 0, 0, 0, 0, 0));

    private static String key(MinecraftServer server, String id) {
        return System.identityHashCode(server) + ":" + id;
    }

    /**
     * Der Bauplan eines eigenen Plans für den laufenden Server; fehlt die Datei, ein leerer Plan (die Baustelle ist dann
     * sofort fertig, statt hängen zu bleiben).
     */
    public static Blueprint blueprint(String id) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return EMPTY;
        }
        String key = key(server, id);
        synchronized (CACHE) {
            Blueprint cached = CACHE.get(key);
            if (cached != null) {
                return cached;
            }
            if (MISSING.contains(key)) {
                return EMPTY;
            }
            PlanScan scan = load(server, id);
            if (scan == null) {
                MISSING.add(key);
                return EMPTY;
            }
            Blueprint plan = scan.toBlueprint();
            CACHE.put(key, plan);
            return plan;
        }
    }

    /** Die Kurzfassung eines Plans auf dem laufenden Server ({@code null}: unbekannt). */
    public static @Nullable PlanInfo infoOf(String id) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        return server == null ? null : of(server).info(id);
    }

    /** Für Tests: den Zwischenspeicher leeren. */
    public static void clearCache() {
        synchronized (CACHE) {
            CACHE.clear();
            MISSING.clear();
        }
    }
}
