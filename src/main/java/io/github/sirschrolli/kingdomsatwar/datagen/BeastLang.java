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
    }
}
