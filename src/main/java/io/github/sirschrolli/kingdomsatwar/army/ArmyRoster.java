package io.github.sirschrolli.kingdomsatwar.army;

import com.mojang.serialization.Codec;
import io.github.sirschrolli.kingdomsatwar.registry.ModAttachments;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.MinecraftServer;

/**
 * Die Stammrolle: welche Soldaten welchem Spieler gehören (Anhang an der Oberwelt, wird mit dem Spielstand gespeichert).
 * So zählen auch Soldaten in der Garnison mit, deren Siedlung gerade nicht geladen ist – die Größe der Armee hängt nicht
 * davon ab, wo der Spieler gerade steht.
 */
public final class ArmyRoster {
    public static final Codec<ArmyRoster> CODEC = Codec.unboundedMap(UUIDUtil.STRING_CODEC, UUIDUtil.STRING_CODEC.listOf())
            .xmap(ArmyRoster::new, roster -> {
                Map<UUID, List<UUID>> out = new HashMap<>();
                roster.members.forEach((owner, soldiers) -> out.put(owner, List.copyOf(soldiers)));
                return out;
            });

    private final Map<UUID, Set<UUID>> members = new HashMap<>();

    public ArmyRoster() {
    }

    private ArmyRoster(Map<UUID, List<UUID>> saved) {
        saved.forEach((owner, soldiers) -> this.members.put(owner, new LinkedHashSet<>(soldiers)));
    }

    public static ArmyRoster of(MinecraftServer server) {
        return server.overworld().getData(ModAttachments.ARMY_ROSTER);
    }

    private void save(MinecraftServer server) {
        server.overworld().setData(ModAttachments.ARMY_ROSTER, this);
    }

    /** Wie viele Soldaten dem Spieler gehören. */
    public int count(UUID owner) {
        Set<UUID> soldiers = this.members.get(owner);
        return soldiers == null ? 0 : soldiers.size();
    }

    public boolean contains(UUID owner, UUID soldier) {
        Set<UUID> soldiers = this.members.get(owner);
        return soldiers != null && soldiers.contains(soldier);
    }

    public List<UUID> soldiers(UUID owner) {
        Set<UUID> soldiers = this.members.get(owner);
        return soldiers == null ? List.of() : new ArrayList<>(soldiers);
    }

    /** Der Soldat dient (jetzt) diesem Spieler – bei keinem anderen mehr. */
    public static void enlist(MinecraftServer server, UUID owner, UUID soldier) {
        ArmyRoster roster = of(server);
        boolean changed = false;
        for (Map.Entry<UUID, Set<UUID>> entry : roster.members.entrySet()) {
            if (!entry.getKey().equals(owner)) {
                changed |= entry.getValue().remove(soldier);
            }
        }
        changed |= roster.members.computeIfAbsent(owner, key -> new LinkedHashSet<>()).add(soldier);
        if (changed) {
            roster.save(server);
        }
    }

    /** Der Soldat ist gefallen, entlassen oder verschwunden. */
    public static void discharge(MinecraftServer server, UUID soldier) {
        ArmyRoster roster = of(server);
        boolean changed = false;
        for (Set<UUID> soldiers : roster.members.values()) {
            changed |= soldiers.remove(soldier);
        }
        if (changed) {
            roster.members.values().removeIf(Set::isEmpty);
            roster.save(server);
        }
    }
}
