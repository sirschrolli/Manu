package io.github.sirschrolli.kingdomsatwar.worldgen.site;

/** Die sechs Bauwerke von 2.0 in der Unterwelt. */
final class Nether {
    private Nether() {}

    static void register() {
        EmberDungeon.register();
        StriderRanch.register();
        WartBrewery.register();
        SoulChapel.register();
        GoldMine.register();
        BrokenBridge.register();
    }
}
