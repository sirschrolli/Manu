package io.github.sirschrolli.kingdomsatwar.client.renderer.wild;

/** Was das gemeinsame Katzenmodell je Rasse wissen muss (Hauskatzen und Wildkatzen). */
public interface FelineLook {
    /** Ohrpinsel und Stummelschwanz. */
    boolean lynx();

    /** Backenbart und dichtes Fell (Perser, Luchs). */
    boolean fluffy();
}
