package io.github.sirschrolli.kingdomsatwar.war;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.sirschrolli.kingdomsatwar.registry.ModAttachments;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import org.jspecify.annotations.Nullable;

/**
 * Der Zustand der vier Reiche (Anhang an der Oberwelt): Lage und Drehung der Stammburg (sobald sie erzeugt wurde), ob
 * der Herrscher gefallen ist, wer die Burg erobert hat und seit wann, wer mit dem Reich im Krieg liegt, wie viele
 * Krieger gefallen sind und wann zuletzt Tribut gezahlt wurde.
 */
public final class RealmState {
    /** Ein Reich. */
    public static final class Entry {
        static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BlockPos.CODEC.optionalFieldOf("castle").forGetter(e -> Optional.ofNullable(e.castle)),
                Codec.INT.optionalFieldOf("rotation", 0).forGetter(e -> e.rotation),
                Codec.BOOL.optionalFieldOf("lord_fallen", false).forGetter(e -> e.lordFallen),
                UUIDUtil.CODEC.optionalFieldOf("conqueror").forGetter(e -> Optional.ofNullable(e.conqueror)),
                Codec.LONG.optionalFieldOf("conquered_at", 0L).forGetter(e -> e.conqueredAt),
                UUIDUtil.CODEC.listOf().optionalFieldOf("enemies", List.of()).forGetter(e -> List.copyOf(e.enemies)),
                Codec.INT.optionalFieldOf("fallen", 0).forGetter(e -> e.fallen),
                Codec.LONG.optionalFieldOf("last_tribute", 0L).forGetter(e -> e.lastTribute),
                Codec.INT.optionalFieldOf("garrison", 0).forGetter(e -> e.garrison),
                Codec.LONG.optionalFieldOf("last_reinforcement", 0L).forGetter(e -> e.lastReinforcement)
        ).apply(instance, Entry::new));

        private @Nullable BlockPos castle;
        private int rotation;
        private boolean lordFallen;
        private @Nullable UUID conqueror;
        private long conqueredAt;
        private final Set<UUID> enemies = new HashSet<>();
        private int fallen;
        private long lastTribute;
        private int garrison;
        private long lastReinforcement;
        /** Spielzeit des letzten Alarms (nicht gespeichert). */
        private long lastAlarm = -100000L;

        Entry() {}

        private Entry(Optional<BlockPos> castle, int rotation, boolean lordFallen, Optional<UUID> conqueror, long conqueredAt,
                List<UUID> enemies, int fallen, long lastTribute, int garrison, long lastReinforcement) {
            this.castle = castle.orElse(null);
            this.rotation = rotation;
            this.lordFallen = lordFallen;
            this.conqueror = conqueror.orElse(null);
            this.conqueredAt = conqueredAt;
            this.enemies.addAll(enemies);
            this.fallen = fallen;
            this.lastTribute = lastTribute;
            this.garrison = garrison;
            this.lastReinforcement = lastReinforcement;
        }
    }

    public static final Codec<RealmState> CODEC = Codec.unboundedMap(Realm.CODEC, Entry.CODEC)
            .xmap(RealmState::new, state -> new EnumMap<>(state.entries));

    private final Map<Realm, Entry> entries = new EnumMap<>(Realm.class);

    public RealmState() {
        this(Map.of());
    }

    private RealmState(Map<Realm, Entry> entries) {
        for (Realm realm : Realm.values()) {
            this.entries.put(realm, entries.getOrDefault(realm, new Entry()));
        }
    }

    public static RealmState of(ServerLevel level) {
        return level.getServer().overworld().getData(ModAttachments.REALMS);
    }

    /** Merkt die Änderungen zum Speichern vor. */
    public void save(ServerLevel level) {
        level.getServer().overworld().setData(ModAttachments.REALMS, this);
    }

    private Entry entry(Realm realm) {
        return this.entries.get(realm);
    }

    // =====================================================================================
    // Burg
    // =====================================================================================

    /** Mitte des Burghofs (sobald die Burg erzeugt wurde). */
    public @Nullable BlockPos castle(Realm realm) {
        return this.entry(realm).castle;
    }

    public int rotation(Realm realm) {
        return this.entry(realm).rotation;
    }

    public void setCastle(Realm realm, BlockPos center, int rotation) {
        Entry entry = this.entry(realm);
        entry.castle = center.immutable();
        entry.rotation = rotation;
    }

    // =====================================================================================
    // Herrscher, Eroberung, Krieg
    // =====================================================================================

    public boolean isLordFallen(Realm realm) {
        return this.entry(realm).lordFallen;
    }

    public void lordFallen(Realm realm) {
        this.entry(realm).lordFallen = true;
    }

    public boolean isConquered(Realm realm) {
        return this.entry(realm).conqueror != null;
    }

    public @Nullable UUID conqueror(Realm realm) {
        return this.entry(realm).conqueror;
    }

    public long conqueredAt(Realm realm) {
        return this.entry(realm).conqueredAt;
    }

    public void conquer(Realm realm, UUID player, long gameTime) {
        Entry entry = this.entry(realm);
        entry.conqueror = player;
        entry.conqueredAt = gameTime;
        entry.lastTribute = gameTime;
        entry.enemies.remove(player);
    }

    /** Liegt der Spieler mit dem Reich im Krieg (hat er einen seiner Krieger erschlagen)? */
    public boolean atWar(Realm realm, UUID player) {
        return this.entry(realm).enemies.contains(player);
    }

    public void declareWar(Realm realm, UUID player) {
        Entry entry = this.entry(realm);
        if (!player.equals(entry.conqueror)) {
            entry.enemies.add(player);
        }
    }

    /** Alle Reiche, mit denen der Spieler im Krieg liegt. */
    public List<Realm> wars(UUID player) {
        List<Realm> out = new ArrayList<>();
        for (Realm realm : Realm.values()) {
            if (this.atWar(realm, player)) {
                out.add(realm);
            }
        }
        return out;
    }

    // =====================================================================================
    // Besatzung, Tribut, Alarm
    // =====================================================================================

    public int fallen(Realm realm) {
        return this.entry(realm).fallen;
    }

    public void warriorFallen(Realm realm) {
        this.entry(realm).fallen++;
    }

    /** Ein gefallener Krieger wurde ersetzt. */
    public void replaced(Realm realm) {
        Entry entry = this.entry(realm);
        entry.fallen = Math.max(0, entry.fallen - 1);
    }

    /** Bis wann der Nachschub schon nachgerückt ist (Spielzeit). */
    public long lastReinforcement(Realm realm) {
        return this.entry(realm).lastReinforcement;
    }

    public void setLastReinforcement(Realm realm, long gameTime) {
        this.entry(realm).lastReinforcement = gameTime;
    }

    public long lastTribute(Realm realm) {
        return this.entry(realm).lastTribute;
    }

    public void setLastTribute(Realm realm, long gameTime) {
        this.entry(realm).lastTribute = gameTime;
    }

    /** Wie viele eigene Soldaten die eroberte Burg bewachen. */
    public int garrison(Realm realm) {
        return this.entry(realm).garrison;
    }

    public void setGarrison(Realm realm, int count) {
        this.entry(realm).garrison = Math.max(0, count);
    }

    /** Alarm in der Burg: höchstens einmal je Minute läuten die Glocken. Gibt {@code true} zurück, wenn jetzt Alarm ist. */
    public boolean alarm(Realm realm, long gameTime) {
        Entry entry = this.entry(realm);
        if (gameTime - entry.lastAlarm < 1200L) {
            return false;
        }
        entry.lastAlarm = gameTime;
        return true;
    }

    /** Alles zurück auf Anfang (nur für Befehle und Tests). */
    public void reset(Realm realm) {
        BlockPos castle = this.entry(realm).castle;
        int rotation = this.entry(realm).rotation;
        Entry fresh = new Entry();
        fresh.castle = castle;
        fresh.rotation = rotation;
        this.entries.put(realm, fresh);
    }
}
