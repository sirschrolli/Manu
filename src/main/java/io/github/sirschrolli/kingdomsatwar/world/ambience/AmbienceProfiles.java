package io.github.sirschrolli.kingdomsatwar.world.ambience;

import static io.github.sirschrolli.kingdomsatwar.world.ambience.AmbienceProfile.Time.ALWAYS;
import static io.github.sirschrolli.kingdomsatwar.world.ambience.AmbienceProfile.Time.DAY;
import static io.github.sirschrolli.kingdomsatwar.world.ambience.AmbienceProfile.Time.DUSK;
import static io.github.sirschrolli.kingdomsatwar.world.ambience.AmbienceProfile.Time.NIGHT;

import io.github.sirschrolli.kingdomsatwar.shadow.ShadowBiomes;
import io.github.sirschrolli.kingdomsatwar.voidrealm.VoidBiomes;
import io.github.sirschrolli.kingdomsatwar.worldgen.ModBiomes;
import io.github.sirschrolli.kingdomsatwar.worldgen.overworld.OverworldBiomes;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import org.jspecify.annotations.Nullable;

/**
 * Die Klangwelten aller eigenen Biome – Oberwelt, Drachennester und Schattenwelt.
 *
 * <p>Jedes Biom hat einen Grundton aus Teppichen (Wind, Laub, Brandung, Glut, Dröhnen …), der sich mit Tageszeit, Regen,
 * Höhe und Dach über dem Kopf ändert, und eine Reihe Einzelklänge, die ab und zu ringsum erklingen: Vögel am Tag, Eulen und
 * Grillen in der Nacht, knackendes Eis, rieselnde Steine, ferne Drachen, Flüstern in der Schattenwelt.
 */
public final class AmbienceProfiles {
    private static final Map<ResourceKey<Biome>, AmbienceProfile> PROFILES = new HashMap<>();

    static {
        overworld();
        nests();
        shadowWorld();
        theVoid();
    }

    private AmbienceProfiles() {}

    /** Die Klangwelt eines Bioms (null: keine eigene, Minecraft bleibt zuständig). */
    public static @Nullable AmbienceProfile of(ResourceKey<Biome> biome) {
        return PROFILES.get(biome);
    }

    public static Map<ResourceKey<Biome>, AmbienceProfile> all() {
        return Map.copyOf(PROFILES);
    }

    private static void put(ResourceKey<Biome> biome, AmbienceProfile.Builder builder) {
        PROFILES.put(biome, builder.build());
    }

    // =====================================================================================
    // Oberwelt
    // =====================================================================================

    private static void overworld() {
        // Aschefelder: trockener, staubiger Wind, knisternde Glut, aus der Tiefe grollt es, Glutspalten fauchen
        put(OverworldBiomes.ASH_FIELDS, AmbienceProfile.builder()
                .wind("wind_dry", 0.5f)
                .outside("embers", 0.22f)
                .everywhere("lava", 0.16f, 0.3f)
                .close("steam_vent", 1.6f, ALWAYS)
                .far("rumble", 0.5f, ALWAYS)
                .spot("basalt", 1.4f, ALWAYS)
                .spot("bird_crow", 0.5f, DAY));
        // Frostgipfel: heulender Sturm, Gletschereis arbeitet, nachts heulen Wölfe
        put(OverworldBiomes.FROST_PEAKS, AmbienceProfile.builder()
                .wind("wind_gale", 0.55f)
                .outside("ice", 0.3f)
                .spot("ice_crack", 1.2f, ALWAYS)
                .spot("ice_creak", 1.0f, ALWAYS)
                .far("rumble", 0.3f, ALWAYS)
                .spot("wolf_howl", 0.4f, NIGHT));
        // Sturmküste: Seewind, schwere Brandung an den Klippen, Möwen, fernes Gewitter
        put(OverworldBiomes.STORM_COAST, AmbienceProfile.builder()
                .wind("wind_coast", 0.5f)
                .everywhere("surf_storm", 0.45f, 0.2f)
                .spot("bird_gull", 2.5f, DAY)
                .far("thunder", 0.7f, ALWAYS)
                .far("thunder_far", 0.5f, ALWAYS));
        // Urhain: rauschendes Laub, ein Vogelchor, Spechte und Kuckuck; nachts Eulen, Grillen, Glühwürmchen und ferne Wölfe
        put(OverworldBiomes.PRIMEVAL_FOREST, AmbienceProfile.builder()
                .wind("wind_breeze", 0.3f)
                .outside("leaves", 0.3f)
                .day("birds_forest", 0.45f)
                .night("crickets", 0.38f)
                .spot("bird_blackbird", 2.4f, DAY)
                .spot("bird_tit", 1.8f, DAY)
                .spot("bird_finch", 1.8f, DAY)
                .spot("bird_cuckoo", 0.5f, DAY)
                .spot("woodpecker", 1.0f, DAY)
                .spot("wood_creak", 1.0f, ALWAYS)
                .spot("twig_snap", 0.7f, ALWAYS)
                .spot("bird_owl", 1.4f, NIGHT)
                .spot("wolf_howl", 0.3f, NIGHT)
                .close("fireflies", 1.6f, NIGHT));
        // Felsschluchten: trockener Wind, Grashüpfer, ein Bussard kreist, Steine rieseln; nachts zirpt es spärlich
        put(OverworldBiomes.ROCK_GORGES, AmbienceProfile.builder()
                .wind("wind_dry", 0.4f)
                .day("insects_day", 0.22f)
                .night("crickets_dry", 0.35f)
                .sky("bird_buzzard", 1.0f, DAY)
                .spot("pebbles", 1.2f, ALWAYS)
                .spot("bird_crow", 0.5f, DAY)
                .spot("wolf_howl", 0.3f, NIGHT));
        // Korallenküste: plätschernde Lagune, sanfte Wellen, Möwen und manchmal ein Delfin; tropische Nächte
        put(OverworldBiomes.CORAL_COAST, AmbienceProfile.builder()
                .outside("lagoon", 0.4f)
                .everywhere("surf_gentle", 0.32f, 0.15f)
                .wind("wind_breeze", 0.25f)
                .day("insects_day", 0.12f)
                .night("crickets", 0.32f)
                .spot("bird_gull", 1.4f, DAY)
                .spot("dolphin", 0.5f, ALWAYS));
        // Königsauen: Lerchen über den Wiesen, Grashüpfer und Hummeln; nachts Grillen und die Nachtigall
        put(OverworldBiomes.ROYAL_MEADOWS, AmbienceProfile.builder()
                .wind("wind_breeze", 0.3f)
                .day("birds_meadow", 0.42f)
                .day("insects_day", 0.3f)
                .night("crickets", 0.45f)
                .spot("bird_finch", 1.4f, DAY)
                .spot("bird_tit", 1.1f, DAY)
                .spot("bird_blackbird", 1.0f, DAY)
                .spot("bird_cuckoo", 0.4f, DAY)
                .spot("bird_nightingale", 1.2f, NIGHT)
                .spot("bird_owl", 0.5f, NIGHT)
                .close("fireflies", 0.8f, NIGHT));
        // Nebelmoor: hohler Wind, raschelndes Schilf, blubbernder Torf, Brachvogel und Rohrdommel; nachts Frösche und Unken
        put(OverworldBiomes.MIST_MOOR, AmbienceProfile.builder()
                .wind("wind_moor", 0.42f)
                .outside("reeds", 0.26f)
                .outside("bog", 0.25f)
                .night("frogs", 0.45f)
                .spot("bird_curlew", 0.9f, DAY)
                .spot("bird_bittern", 0.6f, DUSK)
                .spot("bird_crow", 0.7f, DAY)
                .close("bog_burp", 1.4f, ALWAYS)
                .spot("frog", 0.9f, NIGHT)
                .spot("bird_owl", 0.5f, NIGHT)
                .spot("wolf_howl", 0.25f, NIGHT)
                .close("sparkle", 0.4f, NIGHT));
    }

    // =====================================================================================
    // Drachennester
    // =====================================================================================

    private static void nests() {
        put(ModBiomes.ASHLANDS, AmbienceProfile.builder()
                .everywhere("lava", 0.4f, 0.4f)
                .outside("embers", 0.32f)
                .wind("wind_dry", 0.28f)
                .close("steam_vent", 1.8f, ALWAYS)
                .far("rumble", 0.7f, ALWAYS)
                .spot("basalt", 1.8f, ALWAYS)
                .far("dragon_far", 0.45f, ALWAYS)
                .far("dragon_wings", 0.25f, ALWAYS));
        put(ModBiomes.FROSTBASIN, AmbienceProfile.builder()
                .wind("wind_gale", 0.5f)
                .outside("ice", 0.4f)
                .spot("ice_crack", 1.5f, ALWAYS)
                .spot("ice_creak", 1.2f, ALWAYS)
                .far("rumble", 0.35f, ALWAYS)
                .far("dragon_far", 0.4f, ALWAYS)
                .spot("wolf_howl", 0.3f, NIGHT));
        put(ModBiomes.RUGGED_WILDS, AmbienceProfile.builder()
                .wind("wind_dry", 0.38f)
                .everywhere("earth_rumble", 0.32f, 0.5f)
                .spot("pebbles", 1.8f, ALWAYS)
                .far("rumble", 0.55f, ALWAYS)
                .sky("bird_buzzard", 0.7f, DAY)
                .far("dragon_far", 0.4f, ALWAYS));
        put(ModBiomes.ANCIENT_GROVE, AmbienceProfile.builder()
                .outside("magic_grove", 0.42f)
                .outside("leaves", 0.22f)
                .day("birds_forest", 0.32f)
                .night("crickets", 0.32f)
                .close("sparkle", 1.4f, ALWAYS)
                .spot("bird_blackbird", 1.4f, DAY)
                .spot("woodpecker", 0.6f, DAY)
                .spot("wood_creak", 0.9f, ALWAYS)
                .spot("bird_owl", 1.0f, NIGHT)
                .close("fireflies", 1.8f, NIGHT)
                .far("dragon_far", 0.3f, ALWAYS));
        put(ModBiomes.STORM_CLIFFS, AmbienceProfile.builder()
                .wind("wind_gale", 0.52f)
                .outside("storm_charge", 0.3f)
                .far("thunder", 1.4f, ALWAYS)
                .far("thunder_far", 0.9f, ALWAYS)
                .close("zap", 0.9f, ALWAYS)
                .far("dragon_far", 0.4f, ALWAYS));
        put(ModBiomes.ABYSSAL_GROTTO, AmbienceProfile.builder()
                .everywhere("drips", 0.42f, 1.0f)
                .everywhere("deep_water", 0.38f, 1.0f)
                .close("drip", 1.8f, ALWAYS)
                .far("whale", 0.4f, ALWAYS)
                .far("whale_far", 0.3f, ALWAYS)
                .close("bog_burp", 0.4f, ALWAYS)
                .far("dragon_far", 0.3f, ALWAYS));
    }

    // =====================================================================================
    // Schattenwelt
    // =====================================================================================

    private static void shadowWorld() {
        put(ShadowBiomes.SHADOW_FOREST, AmbienceProfile.builder()
                .everywhere("shadow_forest", 0.5f, 0.45f)
                .outside("leaves", 0.14f)
                .everywhere("whispers", 0.12f, 0.3f)
                .spot("shadow_call", 1.0f, ALWAYS)
                .spot("shadow_creak", 1.1f, ALWAYS)
                .spot("pale_moss", 1.3f, ALWAYS)
                .spot("warped_creak", 0.7f, ALWAYS)
                .close("whisper", 0.45f, ALWAYS));
        put(ShadowBiomes.PALE_WASTES, AmbienceProfile.builder()
                .everywhere("shadow_wastes", 0.55f, 0.35f)
                .spot("wolf_howl", 0.4f, ALWAYS)
                .spot("shadow_call", 0.4f, ALWAYS)
                .far("rumble", 0.3f, ALWAYS)
                .spot("soul_voices", 0.4f, ALWAYS));
        put(ShadowBiomes.CRYSTAL_CHASMS, AmbienceProfile.builder()
                .everywhere("shadow_crystal", 0.5f, 0.6f)
                .spot("crystal_chime", 1.8f, ALWAYS)
                .spot("amethyst", 1.4f, ALWAYS)
                .spot("pebbles", 0.7f, ALWAYS)
                .far("deep_moan", 0.3f, ALWAYS));
        put(ShadowBiomes.SOUL_MARSH, AmbienceProfile.builder()
                .everywhere("shadow_marsh", 0.5f, 0.4f)
                .outside("bog", 0.28f)
                .outside("reeds", 0.2f)
                .spot("soul_wail", 0.9f, ALWAYS)
                .spot("soul_voices", 0.9f, ALWAYS)
                .close("bog_burp", 1.4f, ALWAYS)
                .close("whisper", 0.4f, ALWAYS));
        put(ShadowBiomes.SHADOW_PEAKS, AmbienceProfile.builder()
                .bed(new AmbienceProfile.Bed("shadow_peaks", 0.55f, ALWAYS, 0.1f, 1.0f, 0.8f))
                .spot("wolf_howl", 0.5f, ALWAYS)
                .spot("shadow_call", 0.5f, ALWAYS)
                .spot("pebbles", 0.9f, ALWAYS)
                .far("rumble", 0.3f, ALWAYS));
        put(ShadowBiomes.GLIMMER_MEADOWS, AmbienceProfile.builder()
                .everywhere("shadow_glimmer", 0.5f, 0.4f)
                .outside("crickets", 0.18f)
                .close("sparkle", 1.8f, ALWAYS)
                .spot("wing_flutter", 0.9f, ALWAYS)
                .spot("shadow_call", 0.5f, ALWAYS)
                .close("fireflies", 1.0f, ALWAYS));
        put(ShadowBiomes.ABYSSAL_SEA, AmbienceProfile.builder()
                .everywhere("shadow_sea", 0.55f, 0.4f)
                .outside("surf_gentle", 0.22f)
                .far("whale", 0.6f, ALWAYS)
                .far("whale_far", 0.4f, ALWAYS)
                .far("deep_moan", 0.3f, ALWAYS));
        put(ShadowBiomes.FLOATING_ISLES, AmbienceProfile.builder()
                .everywhere("shadow_isles", 0.5f, 0.35f)
                .wind("wind_gale", 0.22f)
                .spot("wing_flutter", 1.1f, ALWAYS)
                .far("dragon_wings", 0.35f, ALWAYS)
                .spot("crystal_chime", 0.6f, ALWAYS)
                .spot("shadow_call", 0.5f, ALWAYS));
        put(ShadowBiomes.SHADOW_DEPTHS, AmbienceProfile.builder()
                .everywhere("shadow_depths", 0.6f, 1.0f)
                .everywhere("drips", 0.28f, 1.0f)
                .everywhere("whispers", 0.18f, 1.0f)
                .far("deep_moan", 0.6f, ALWAYS)
                .close("drip", 1.4f, ALWAYS)
                .close("whisper", 0.6f, ALWAYS)
                .spot("soul_voices", 0.4f, ALWAYS, 8, 24, -6, 6, false, 0.8f, 1.0f));
    }

    // =====================================================================================
    // Die Leere
    // =====================================================================================

    /**
     * Die Leere: überall das tiefe, schwebende Dröhnen des Nichts mit feinem Sternensirren; dazu je Biom gläserne
     * Akkorde und singende Kristalle, rieselnder Sternenstaub, Chöre der Alten zwischen schabenden Steinen oder dumpfe
     * Erinnerungen an verlorene Welten. Aus der Ferne singen Sternenrochen.
     */
    private static void theVoid() {
        put(VoidBiomes.EXPANSE, AmbienceProfile.builder()
                .everywhere("void_hum", 0.6f, 0.6f)
                .wind("void_wind", 0.32f)
                .far("star_song", 0.6f, ALWAYS)
                .far("deep_moan", 0.2f, ALWAYS)
                .spot("rock_grind", 0.4f, ALWAYS)
                .spot("sparkle", 0.5f, ALWAYS));
        put(VoidBiomes.CRYSTAL_FOREST, AmbienceProfile.builder()
                .everywhere("void_hum", 0.38f, 0.4f)
                .everywhere("void_crystal", 0.45f, 0.5f)
                .spot("crystal_chime", 1.4f, ALWAYS)
                .spot("amethyst", 1.0f, ALWAYS)
                .spot("glitch_far", 0.3f, ALWAYS)
                .far("star_song", 0.3f, ALWAYS));
        put(VoidBiomes.WEIGHTLESS_RUINS, AmbienceProfile.builder()
                .everywhere("void_hum", 0.38f, 0.4f)
                .everywhere("void_ruins", 0.5f, 0.6f)
                .wind("void_wind", 0.18f)
                .far("void_choir", 0.5f, ALWAYS)
                .spot("rock_grind", 0.8f, ALWAYS)
                .close("whisper", 0.4f, ALWAYS)
                .spot("pebbles", 0.4f, ALWAYS));
        put(VoidBiomes.STARDUST_FIELDS, AmbienceProfile.builder()
                .everywhere("void_hum", 0.42f, 0.4f)
                .everywhere("void_stardust", 0.45f, 0.4f)
                .spot("sparkle", 1.2f, ALWAYS)
                .far("star_song", 0.5f, ALWAYS));
        put(VoidBiomes.SHARD_CLIFFS, AmbienceProfile.builder()
                .everywhere("void_hum", 0.5f, 0.5f)
                .wind("void_wind", 0.42f)
                .everywhere("void_crystal", 0.25f, 0.3f)
                .spot("crystal_chime", 0.8f, ALWAYS)
                .spot("rock_grind", 0.7f, ALWAYS)
                .spot("pebbles", 0.6f, ALWAYS)
                .far("deep_moan", 0.3f, ALWAYS));
        put(VoidBiomes.WORLD_REMNANTS, AmbienceProfile.builder()
                .everywhere("void_hum", 0.38f, 0.4f)
                .everywhere("void_remnants", 0.5f, 0.5f)
                .spot("glitch_far", 0.6f, ALWAYS)
                .far("void_choir", 0.2f, ALWAYS)
                .far("star_song", 0.2f, ALWAYS));
    }
}
