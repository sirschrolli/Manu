package io.github.sirschrolli.kingdomsatwar.worldgen.village;

/**
 * Das Gelände, auf dem ein Dorf geplant wird: im Spiel das des Geländegenerators ({@link VillageTerrain}), in Tests und
 * Vorführungen ebenes Land ({@link #flat}).
 */
interface VillageGround {
    /** Oberster fester Block (y). */
    int ground(int x, int z);

    /** Erste freie Höhe (über Wasser die Wasseroberfläche + 1). */
    int surface(int x, int z);

    boolean water(int x, int z);

    /** Wassertiefe (0 = trocken). */
    int waterDepth(int x, int z);

    int seaLevel();

    /** Darf hier ein Weg verlaufen (Land des Dorfs oder ein Fluss, über den eine Brücke führt)? */
    boolean allowed(int x, int z);

    /** Darf hier ein Gebäude stehen? */
    boolean buildable(int x, int z);

    /**
     * Ebenes, trockenes Land mit dem obersten Block bei {@code y}; bei {@code wet} steht überall flaches Wasser (für
     * Pfahldörfer): der Grund liegt dann zwei Blöcke unter der Wasseroberfläche.
     */
    static VillageGround flat(int y, int seaLevel, boolean wet) {
        return new VillageGround() {
            @Override
            public int ground(int x, int z) {
                return wet ? y - 2 : y;
            }

            @Override
            public int surface(int x, int z) {
                return y + 1;
            }

            @Override
            public boolean water(int x, int z) {
                return wet;
            }

            @Override
            public int waterDepth(int x, int z) {
                return wet ? 2 : 0;
            }

            @Override
            public int seaLevel() {
                return seaLevel;
            }

            @Override
            public boolean allowed(int x, int z) {
                return true;
            }

            @Override
            public boolean buildable(int x, int z) {
                return true;
            }
        };
    }
}
