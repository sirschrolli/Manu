package io.github.sirschrolli.kingdomsatwar.army;

import io.github.sirschrolli.kingdomsatwar.config.ModServerConfig;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

/**
 * Die Armee eines Spielers: wie groß sie sein darf, wie groß sie ist, und die Antworten auf das Anheuern in den Dörfern
 * (Söldnertafel) und auf Heilen und Ausbilden am Feldherrentisch. Am Tisch selbst wirbt man niemanden mehr an –
 * Soldaten holt man sich bei den Rittern und Söldnern der Dörfer.
 */
public final class Recruitment {
    /** Wie nah der Spieler am Feldherrentisch stehen muss. */
    public static final double TABLE_RANGE = 8.0;

    /** Ergebnis von Anheuern, Heilen und Ausbilden (Reihenfolge = Nummer im Netzwerk). */
    public enum Result {
        TOO_FAR, NOT_ENOUGH, ARMY_FULL, NO_SPACE,
        /** Am Feldherrentisch: alle Verwundeten im Feldlager geheilt – bzw. niemand war verwundet. */
        HEALED, NOTHING_TO_HEAL,
        /** Ausbildung am Tisch: befördert (ein Rang höher) bzw. zu einer neuen Art ausgebildet. */
        TRAINED, PROMOTED,
        /** Der Soldat hat den höchsten Rang schon / lässt sich nicht weiter ausbilden. */
        MAX_RANK,
        /** Der Soldat gehört nicht dem Spieler oder steht nicht am Tisch (bzw. der Söldner ist schon fort). */
        NOT_HERE,
        /** Ein Söldner im Dorf wurde angeheuert. */
        HIRED,
        /** Proviant: allen im Feldlager zugeteilt – bzw. nicht genug Essen da, bzw. alle haben schon genug. */
        SUPPLIED, NO_FOOD, NOTHING_NEEDED,
        /** Schlachtzug: die Soldaten des Feldlagers ziehen mit – bzw. kehren in die Garnison zurück. */
        MARCHED, RETURNED,
        /** Ohne eigene Siedlung gibt es keine Garnison; im Feldlager steht niemand, der mitziehen könnte. */
        NO_TOWN, NO_SOLDIERS,
        /** Für den nächsten Rang fehlt dem Soldaten noch Kampferfahrung. */
        NEEDS_LEVEL;

        public String translationKey() {
            return "message.kingdomsatwar.recruit." + this.name().toLowerCase(java.util.Locale.ROOT);
        }
    }

    private Recruitment() {}

    /** Soldaten mehr je Kaserne in der eigenen Siedlung. */
    public static final int SOLDIERS_PER_BARRACKS = 8;

    public static int armyLimit() {
        return ModServerConfig.SPEC.isLoaded() ? ModServerConfig.ARMY_LIMIT.get() : ModServerConfig.ARMY_LIMIT.getDefault();
    }

    /** Grenze der Armee eines Spielers: die Einstellung des Servers und je Kaserne seiner Siedlung acht Soldaten mehr. */
    public static int armyLimit(Player player) {
        int limit = armyLimit();
        if (player.level() instanceof ServerLevel level) {
            io.github.sirschrolli.kingdomsatwar.economy.town.Town town =
                    io.github.sirschrolli.kingdomsatwar.economy.town.Towns.of(level.getServer()).byOwner(player.getUUID());
            if (town != null) {
                limit += SOLDIERS_PER_BARRACKS * town.count(io.github.sirschrolli.kingdomsatwar.economy.town.BuildingType.BARRACKS);
            }
        }
        return limit;
    }

    /**
     * Wie viele Soldaten der Spieler hat – nach der Stammrolle, also auch die in der Garnison, deren Siedlung gerade nicht
     * geladen ist.
     */
    public static int armySize(ServerLevel level, UUID owner) {
        return ArmyRoster.of(level.getServer()).count(owner);
    }
}
