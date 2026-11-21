package io.github.sirschrolli.kingdomsatwar.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.registry.ModAttachments;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.jspecify.annotations.Nullable;

/**
 * Was die Welt über die sechs Drachennester weiß: Lage (sobald das Nest erzeugt wurde) und Zustand
 * (schlafend, wach, besiegt). Gespeichert als Anhang an der Oberwelt.
 */
public final class LairRecords {
    public enum Phase { DORMANT, AWAKE, DEFEATED }

    public record Entry(BlockPos center, Phase phase) {
        static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BlockPos.CODEC.fieldOf("center").forGetter(Entry::center),
                Codec.STRING.xmap(s -> Phase.valueOf(s.toUpperCase()), p -> p.name().toLowerCase()).fieldOf("phase").forGetter(Entry::phase)
        ).apply(instance, Entry::new));
    }

    public static final Codec<LairRecords> CODEC = Codec.unboundedMap(Codec.STRING, Entry.CODEC).xmap(LairRecords::fromMap, LairRecords::toMap);

    private final Map<DragonType, Entry> entries = new EnumMap<>(DragonType.class);

    public LairRecords() {
    }

    private static LairRecords fromMap(Map<String, Entry> map) {
        LairRecords records = new LairRecords();
        map.forEach((key, entry) -> records.entries.put(DragonType.byId(key), entry));
        return records;
    }

    private Map<String, Entry> toMap() {
        Map<String, Entry> map = new HashMap<>();
        this.entries.forEach((type, entry) -> map.put(type.id(), entry));
        return map;
    }

    public static LairRecords of(ServerLevel level) {
        return level.getServer().overworld().getData(ModAttachments.LAIRS);
    }

    public @Nullable Entry get(DragonType type) {
        return this.entries.get(type);
    }

    public Phase phase(DragonType type) {
        Entry entry = this.entries.get(type);
        return entry == null ? Phase.DORMANT : entry.phase();
    }

    public boolean isDefeated(DragonType type) {
        return this.phase(type) == Phase.DEFEATED;
    }

    public int defeatedCount() {
        return (int) this.entries.values().stream().filter(e -> e.phase() == Phase.DEFEATED).count();
    }

    /** Setzt ein Nest wieder auf „schlafend“ (auch wenn es besiegt war) – nur für Befehle und Tests. */
    public static void reset(ServerLevel level, DragonType type, BlockPos center) {
        ServerLevel overworld = level.getServer().overworld();
        LairRecords records = overworld.getData(ModAttachments.LAIRS);
        records.entries.put(type, new Entry(center, Phase.DORMANT));
        overworld.setData(ModAttachments.LAIRS, records);
    }

    /** Trägt Lage und Zustand ein (und markiert die Daten zum Speichern). */
    public static void update(ServerLevel level, DragonType type, BlockPos center, Phase phase) {
        ServerLevel overworld = level.getServer().overworld();
        LairRecords records = overworld.getData(ModAttachments.LAIRS);
        Entry old = records.entries.get(type);
        if (old == null || !old.center().equals(center) || old.phase() != phase) {
            // ein besiegtes Nest bleibt besiegt
            Phase keep = old != null && old.phase() == Phase.DEFEATED ? Phase.DEFEATED : phase;
            records.entries.put(type, new Entry(center, keep));
            overworld.setData(ModAttachments.LAIRS, records);
        }
    }
}
