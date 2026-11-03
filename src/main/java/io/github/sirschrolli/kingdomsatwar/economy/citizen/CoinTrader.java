package io.github.sirschrolli.kingdomsatwar.economy.citizen;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

/**
 * Wer gegen Münzen handelt: die Bürger der eigenen Stadt und die Dorfbewohner. Das Handelsfenster, die Preise mit
 * Rabatt durch Ansehen und die Handelsgrenze in Echtzeit sind für beide gleich (siehe {@link Trading}).
 */
public interface CoinTrader {
    /** Handelt dieser Bewohner überhaupt (nicht jeder hat einen Laden)? */
    boolean canTrade();

    /** Die Angebote vor Rabatt (Preise in Bronze); die Reihenfolge bleibt zwischen Anzeigen und Handeln gleich. */
    List<Trading.Offer> baseOffers();

    /** Wie oft seit der letzten Pause gehandelt wurde. */
    int tradesUsed();

    /** Bis wann (Echtzeit, ms) der Laden geschlossen ist; 0: offen. */
    long closedUntil();

    /** Ist der Laden gerade geschlossen? (Öffnet ihn wieder, wenn die Pause vorbei ist.) */
    boolean isClosed();

    void recordTrade(int limit, long pauseMillis);

    @Nullable Player getCustomer();

    void setCustomer(@Nullable Player customer);

    /** Bekommt dieser Spieler alles umsonst (der gekrönte Herrscher bei seinen eigenen Bürgern)? */
    default boolean givesFreely(Player player) {
        return false;
    }

    /** Hauttextur (für das Gesicht im Handelsfenster), z. B. {@code kingdomsatwar:textures/entity/villager/baker_1.png}. */
    String skinTexture();

    /** Übersetzungsschlüssel des Berufs. */
    String professionKey();

    /** Name im Handelsfenster. */
    Component traderName();
}
