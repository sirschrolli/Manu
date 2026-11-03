package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.registry.ModItems;

/** Texte zum eigenen Helden: Herz- und Manakristalle. */
final class HeroLang {
    @FunctionalInterface
    interface Text {
        void add(String key, String english, String deutsch);
    }

    private HeroLang() {}

    static void register(Text text) {
        // --- Herz- und Manakristalle ---
        text.add("item.kingdomsatwar.heart_crystal", "Heart Crystal", "Herzkristall");
        text.add(ModItems.loreKey("heart_crystal", 1), "Right-click: one more heart, for good", "Rechtsklick: ein Herz mehr, für immer");
        text.add(ModItems.loreKey("heart_crystal", 2), "Grows rarely in caves – at most 10", "Wächst selten in Höhlen – höchstens 10");
        text.add("item.kingdomsatwar.mana_crystal", "Mana Crystal", "Manakristall");
        text.add(ModItems.loreKey("mana_crystal", 1), "Right-click: 20 more mana, for good", "Rechtsklick: 20 Mana mehr, für immer");
        text.add(ModItems.loreKey("mana_crystal", 2), "Grows rarely deep in caves – at most 10", "Wächst selten tief in Höhlen – höchstens 10");
        text.add("block.kingdomsatwar.heart_crystal_formation", "Heart Crystal", "Herzkristall");
        text.add("block.kingdomsatwar.mana_crystal_formation", "Mana Crystal", "Manakristall");
        text.add("message.kingdomsatwar.crystal.heart.absorbed", "❤ One more heart! Now %1$s hearts (%2$s/%3$s heart crystals)",
                "❤ Ein Herz mehr! Jetzt %1$s Herzen (%2$s/%3$s Herzkristalle)");
        text.add("message.kingdomsatwar.crystal.mana.absorbed", "✦ More mana! Now up to %1$s mana (%2$s/%3$s mana crystals)",
                "✦ Mehr Mana! Jetzt bis zu %1$s Mana (%2$s/%3$s Manakristalle)");
        text.add("message.kingdomsatwar.crystal.heart.full", "You have already absorbed all %s heart crystals",
                "Du hast schon alle %s Herzkristalle aufgenommen");
        text.add("message.kingdomsatwar.crystal.mana.full", "You have already absorbed all %s mana crystals",
                "Du hast schon alle %s Manakristalle aufgenommen");
        text.add("tooltip.kingdomsatwar.crystal.count", "Absorbed: %s/%s", "Aufgenommen: %s/%s");
    }
}
