package io.github.sirschrolli.kingdomsatwar.worldgen.site;

/** Die sechs Bauwerke von 2.0 in der Oberwelt. */
final class Overworld {
    private Overworld() {}

    static void register() {
        SennHut.register();
        SmugglerCove.register();
        Apiary.register();
        Barrow.register();
        AlchemistTower.register();
        HuntingLodge.register();
    }
}
