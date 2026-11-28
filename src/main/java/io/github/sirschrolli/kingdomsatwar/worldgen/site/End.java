package io.github.sirschrolli.kingdomsatwar.worldgen.site;

/** Die sechs Bauwerke von 2.0 im End. */
final class End {
    private End() {}

    static void register() {
        AirshipWreck.register();
        ChorusGarden.register();
        PurpurWatchtower.register();
        VoidAngler.register();
        EndQuarry.register();
        DragonGrave.register();
    }
}
