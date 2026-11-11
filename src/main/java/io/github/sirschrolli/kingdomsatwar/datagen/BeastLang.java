package io.github.sirschrolli.kingdomsatwar.datagen;

/** Texte der Tiere der eigenen Biome (0.85): Namen, Spawn-Eier und Untertitel ihrer Geräusche. */
final class BeastLang {
    private BeastLang() {}

    private static void beast(HeroLang.Text text, String id, String nameEn, String nameDe, String eggDe) {
        text.add("entity.kingdomsatwar." + id, nameEn, nameDe);
        text.add("item.kingdomsatwar." + id + "_spawn_egg", nameEn + " Spawn Egg", eggDe + "-Spawn-Ei");
    }

    private static void subtitle(HeroLang.Text text, String sound, String english, String deutsch) {
        text.add("subtitles.kingdomsatwar." + sound, english, deutsch);
    }

    static void register(HeroLang.Text text) {
        beast(text, "snow_leopard", "Snow Leopard", "Schneeleopard", "Schneeleoparden");
        subtitle(text, "entity.snow_leopard.ambient", "Snow leopard chuffs", "Schneeleopard prustet");
        subtitle(text, "entity.snow_leopard.purr", "Snow leopard purrs", "Schneeleopard schnurrt");
        subtitle(text, "entity.snow_leopard.hiss", "Snow leopard snarls", "Schneeleopard faucht");
        subtitle(text, "entity.snow_leopard.eat", "Snow leopard eats", "Schneeleopard frisst");
        subtitle(text, "entity.snow_leopard.hurt", "Snow leopard hurts", "Schneeleopard wird verletzt");
        subtitle(text, "entity.snow_leopard.death", "Snow leopard dies", "Schneeleopard stirbt");
        subtitle(text, "entity.snow_leopard.step", "Soft paws", "Leise Pfoten");
        beast(text, "primeval_elk", "Primeval Elk", "Urelch", "Urelch");
        subtitle(text, "entity.primeval_elk.ambient", "Elk grunts", "Urelch brummt");
        subtitle(text, "entity.primeval_elk.bugle", "Elk bull bellows", "Elchbulle röhrt");
        subtitle(text, "entity.primeval_elk.angry", "Elk snorts", "Urelch schnaubt");
        subtitle(text, "entity.primeval_elk.impact", "Antlers strike", "Schaufeln stoßen zu");
        subtitle(text, "entity.primeval_elk.eat", "Elk eats", "Urelch frisst");
        subtitle(text, "entity.primeval_elk.hurt", "Elk hurts", "Urelch wird verletzt");
        subtitle(text, "entity.primeval_elk.death", "Elk dies", "Urelch stirbt");
        subtitle(text, "entity.primeval_elk.step", "Heavy hooves", "Schwere Hufe");
        beast(text, "storm_gull", "Storm Gull", "Sturmmöwe", "Sturmmöwen");
        subtitle(text, "entity.storm_gull.call", "Gull laughs", "Möwe lacht");
        subtitle(text, "entity.storm_gull.hurt", "Gull shrieks", "Möwe kreischt");
        subtitle(text, "entity.storm_gull.death", "Gull dies", "Möwe stirbt");
        subtitle(text, "entity.storm_gull.flap", "Wings flap", "Flügel klatschen");
        subtitle(text, "entity.storm_gull.step", "Webbed feet patter", "Schwimmfüße tapsen");
        beast(text, "griffin", "Griffin", "Greif", "Greifen");
        subtitle(text, "entity.griffin.screech", "Griffin screeches", "Greif schreit");
        subtitle(text, "entity.griffin.growl", "Griffin growls", "Greif grollt");
        subtitle(text, "entity.griffin.wings", "Mighty wingbeats", "Mächtige Flügelschläge");
        subtitle(text, "entity.griffin.hurt", "Griffin hurts", "Greif wird verletzt");
        subtitle(text, "entity.griffin.death", "Griffin dies", "Greif stirbt");
        subtitle(text, "entity.griffin.strike", "Talons strike", "Krallen schlagen zu");
        subtitle(text, "entity.griffin.step", "Talons click", "Krallen klicken");
        // 0.86
        beast(text, "ember_salamander", "Ember Salamander", "Glutsalamander", "Glutsalamander");
        subtitle(text, "entity.ember_salamander.ambient", "Ember Salamander squeaks", "Glutsalamander quiekt");
        subtitle(text, "entity.ember_salamander.hurt", "Ember Salamander hurts", "Glutsalamander wird verletzt");
        subtitle(text, "entity.ember_salamander.death", "Ember Salamander dies", "Glutsalamander stirbt");
        subtitle(text, "entity.ember_salamander.step", "Ember Salamander scurries", "Glutsalamander huscht");
        subtitle(text, "entity.ember_salamander.ignite", "Glowing skin burns", "Glühende Haut brennt");
        beast(text, "mire_snapper", "Mire Snapper", "Moorschnapper", "Moorschnapper");
        subtitle(text, "entity.mire_snapper.ambient", "Mire Snapper grunts", "Moorschnapper brummt");
        subtitle(text, "entity.mire_snapper.hiss", "Mire Snapper hisses", "Moorschnapper faucht");
        subtitle(text, "entity.mire_snapper.snap", "Jaws snap shut", "Kiefer schnappen zu");
        subtitle(text, "entity.mire_snapper.hurt", "Mire Snapper hurts", "Moorschnapper wird verletzt");
        subtitle(text, "entity.mire_snapper.death", "Mire Snapper dies", "Moorschnapper stirbt");
        subtitle(text, "entity.mire_snapper.step", "Mire Snapper crawls", "Moorschnapper kriecht");
        beast(text, "flamingo", "Flamingo", "Flamingo", "Flamingo");
        subtitle(text, "entity.flamingo.call", "Flamingo honks", "Flamingo schnattert");
        subtitle(text, "entity.flamingo.hurt", "Flamingo hurts", "Flamingo wird verletzt");
        subtitle(text, "entity.flamingo.death", "Flamingo dies", "Flamingo stirbt");
        subtitle(text, "entity.flamingo.flap", "Wings flutter", "Flügel flattern");
        subtitle(text, "entity.flamingo.step", "Water splashes", "Wasser plätschert");
        beast(text, "butterfly", "Butterfly", "Schmetterling", "Schmetterlings");
    }
}
