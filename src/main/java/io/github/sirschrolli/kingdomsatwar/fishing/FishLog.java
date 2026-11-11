package io.github.sirschrolli.kingdomsatwar.fishing;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Das Fischbuch eines Spielers: zu jeder Art, die er je gefangen hat, wie viele und der größte (in Zentimetern), dazu die
 * Zahl der gehobenen Schätze. Bleibt über den Tod hinaus erhalten.
 */
public final class FishLog {
    /** Ein Eintrag: wie oft gefangen und das größte Tier (cm). */
    public record Entry(int count, int best) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("count").forGetter(Entry::count),
                Codec.INT.fieldOf("best").forGetter(Entry::best)).apply(instance, Entry::new));
    }

    public static final Codec<FishLog> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING, Entry.CODEC).fieldOf("fish").forGetter(FishLog::raw),
            Codec.INT.optionalFieldOf("treasures", 0).forGetter(log -> log.treasures)).apply(instance, FishLog::new));
    public static final StreamCodec<ByteBuf, FishLog> STREAM_CODEC = StreamCodec.of((buf, log) -> {
        ByteBufCodecs.VAR_INT.encode(buf, log.entries.size());
        for (Map.Entry<FishSpecies, Entry> e : log.entries.entrySet()) {
            ByteBufCodecs.VAR_INT.encode(buf, e.getKey().ordinal());
            ByteBufCodecs.VAR_INT.encode(buf, e.getValue().count());
            ByteBufCodecs.VAR_INT.encode(buf, e.getValue().best());
        }
        ByteBufCodecs.VAR_INT.encode(buf, log.treasures);
    }, buf -> {
        FishLog log = new FishLog();
        int n = ByteBufCodecs.VAR_INT.decode(buf);
        for (int i = 0; i < n; i++) {
            int species = ByteBufCodecs.VAR_INT.decode(buf);
            int count = ByteBufCodecs.VAR_INT.decode(buf);
            int best = ByteBufCodecs.VAR_INT.decode(buf);
            if (species >= 0 && species < FishSpecies.values().length) {
                log.entries.put(FishSpecies.values()[species], new Entry(count, best));
            }
        }
        log.treasures = ByteBufCodecs.VAR_INT.decode(buf);
        return log;
    });

    private final EnumMap<FishSpecies, Entry> entries = new EnumMap<>(FishSpecies.class);
    private int treasures;

    public FishLog() {}

    /** Aus dem Spielstand: unbekannte Arten (aus späteren Versionen entfernt) werden übergangen. */
    private FishLog(Map<String, Entry> raw, int treasures) {
        for (FishSpecies species : FishSpecies.values()) {
            Entry entry = raw.get(species.id());
            if (entry != null) {
                this.entries.put(species, entry);
            }
        }
        this.treasures = treasures;
    }

    private Map<String, Entry> raw() {
        Map<String, Entry> raw = new HashMap<>();
        this.entries.forEach((species, entry) -> raw.put(species.id(), entry));
        return raw;
    }

    /** Eine Kopie (Anhänge werden nie am Original verändert). */
    public FishLog copy() {
        FishLog copy = new FishLog();
        copy.entries.putAll(this.entries);
        copy.treasures = this.treasures;
        return copy;
    }

    public boolean has(FishSpecies species) {
        return this.entries.containsKey(species);
    }

    public int count(FishSpecies species) {
        Entry entry = this.entries.get(species);
        return entry == null ? 0 : entry.count();
    }

    public int best(FishSpecies species) {
        Entry entry = this.entries.get(species);
        return entry == null ? 0 : entry.best();
    }

    /** Wie viele Arten schon im Buch stehen. */
    public int speciesCaught() {
        return this.entries.size();
    }

    public int totalCaught() {
        int total = 0;
        for (Entry entry : this.entries.values()) {
            total += entry.count();
        }
        return total;
    }

    public int treasures() {
        return this.treasures;
    }

    public void addTreasure() {
        this.treasures++;
    }

    /** Trägt einen Fang ein. @return 2 = neue Art, 1 = neuer Rekord, 0 = sonst */
    public int record(FishSpecies species, int cm) {
        Entry old = this.entries.get(species);
        if (old == null) {
            this.entries.put(species, new Entry(1, cm));
            return 2;
        }
        this.entries.put(species, new Entry(old.count() + 1, Math.max(old.best(), cm)));
        return cm > old.best() ? 1 : 0;
    }
}
