package io.github.sirschrolli.kingdomsatwar.world.ambience;

import static io.github.sirschrolli.kingdomsatwar.world.ambience.AmbienceProfile.Time.ALWAYS;
import static io.github.sirschrolli.kingdomsatwar.world.ambience.AmbienceProfile.Time.DAY;
import static io.github.sirschrolli.kingdomsatwar.world.ambience.AmbienceProfile.Time.DUSK;
import static io.github.sirschrolli.kingdomsatwar.world.ambience.AmbienceProfile.Time.NIGHT;

import io.github.sirschrolli.kingdomsatwar.abyss.AbyssBiomes;
import io.github.sirschrolli.kingdomsatwar.fae.FaeBiomes;
import io.github.sirschrolli.kingdomsatwar.shadow.ShadowBiomes;
import io.github.sirschrolli.kingdomsatwar.voidrealm.VoidBiomes;
import io.github.sirschrolli.kingdomsatwar.worldgen.ModBiomes;
import io.github.sirschrolli.kingdomsatwar.worldgen.overworld.OverworldBiomes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import org.jspecify.annotations.Nullable;

/**
 * Die Klangwelten der Biome – der eigenen (Oberwelt, Drachennester, Schattenwelt, Leere, Feenreich, Abgrund) und, seit
 * 0.61, auch der Standard-Biome der Oberwelt.
 *
 * <p>Seit 0.61 tragen vor allem <b>Einzelklänge</b> die Stimmung: rauschendes Laub, Wind im Gras, rieselnder Sand, Insekten
 * in der Nacht, Bienen über Blumen, Papageien im Dschungel, Möwen an der Küste – vieles davon echte Naturaufnahmen von
 * Minecraft. Darunter liegen nur noch leise Teppiche (Wind, Wasser, Glut, das Dröhnen fremder Welten), die kommen und
 * gehen statt dauernd zu laufen ({@code client.ambience.BiomeAmbience}). Die künstlichen Teppiche aus Laubrauschen,
 * Grillen, Insektensummen und Vogelchor sind nicht mehr dabei.
 */
public final class AmbienceProfiles {
    private static final Map<ResourceKey<Biome>, AmbienceProfile> PROFILES = new HashMap<>();
    /** Klangwelten nach Merkmalen, für Biome ohne eigene (siehe {@link #of(net.minecraft.core.Holder)}). */
    private static final Map<String, AmbienceProfile> FALLBACKS = new HashMap<>();

    static {
        overworld();
        standardBiomes();
        nests();
        shadowWorld();
        theVoid();
        faeRealm();
        abyss();
        theEnd();
        arenas();
        fallbacks();
    }

    private AmbienceProfiles() {}

    /** Die Klangwelt eines Bioms (null: keine eigene, Minecraft bleibt zuständig). */
    public static @Nullable AmbienceProfile of(ResourceKey<Biome> biome) {
        return PROFILES.get(biome);
    }

    /**
     * Die Klangwelt eines Bioms – und für Biome ohne eigene (fremde Mods, künftige Biome) eine passende nach seinen
     * Merkmalen: Wald, Nadelwald, Meer, Fluss, Strand, Gebirge, Trockenland, End oder Grasland. Nur der Nether bleibt bei
     * Minecraft (er hat dort eigene Klangschleifen).
     */
    public static @Nullable AmbienceProfile of(net.minecraft.core.Holder<Biome> biome) {
        AmbienceProfile own = biome.unwrapKey().map(PROFILES::get).orElse(null);
        if (own != null) {
            return own;
        }
        if (biome.is(net.minecraft.tags.BiomeTags.IS_NETHER)) {
            return null;
        }
        if (biome.is(net.minecraft.tags.BiomeTags.IS_END)) {
            return FALLBACKS.get("end");
        }
        if (biome.is(net.minecraft.tags.BiomeTags.IS_OCEAN) || biome.is(net.minecraft.tags.BiomeTags.IS_DEEP_OCEAN)) {
            return FALLBACKS.get("sea");
        }
        if (biome.is(net.minecraft.tags.BiomeTags.IS_RIVER)) {
            return FALLBACKS.get("river");
        }
        if (biome.is(net.minecraft.tags.BiomeTags.IS_BEACH)) {
            return FALLBACKS.get("beach");
        }
        if (biome.is(net.minecraft.tags.BiomeTags.IS_TAIGA)) {
            return FALLBACKS.get("conifers");
        }
        if (biome.is(net.minecraft.tags.BiomeTags.IS_FOREST) || biome.is(net.minecraft.tags.BiomeTags.IS_JUNGLE)) {
            return FALLBACKS.get("woodland");
        }
        if (biome.is(net.minecraft.tags.BiomeTags.IS_MOUNTAIN) || biome.is(net.minecraft.tags.BiomeTags.IS_HILL)) {
            return FALLBACKS.get("peaks");
        }
        if (biome.is(net.minecraft.tags.BiomeTags.IS_BADLANDS) || biome.is(net.minecraft.tags.BiomeTags.IS_SAVANNA)) {
            return FALLBACKS.get("dry");
        }
        return FALLBACKS.get("grassland");
    }

    public static Map<String, AmbienceProfile> fallbackProfiles() {
        return java.util.Collections.unmodifiableMap(FALLBACKS);
    }

    public static Map<ResourceKey<Biome>, AmbienceProfile> all() {
        return Map.copyOf(PROFILES);
    }

    /** Die Standard-Biome der Oberwelt, die eine Klangwelt bekommen (alle außer dem Tiefen Dunkel). */
    public static final List<ResourceKey<Biome>> STANDARD = List.of(Biomes.PLAINS, Biomes.SUNFLOWER_PLAINS, Biomes.MEADOW, Biomes.FOREST,
            Biomes.FLOWER_FOREST, Biomes.BIRCH_FOREST, Biomes.OLD_GROWTH_BIRCH_FOREST, Biomes.DARK_FOREST, Biomes.PALE_GARDEN,
            Biomes.CHERRY_GROVE, Biomes.TAIGA, Biomes.OLD_GROWTH_PINE_TAIGA, Biomes.OLD_GROWTH_SPRUCE_TAIGA, Biomes.SNOWY_TAIGA,
            Biomes.SNOWY_PLAINS, Biomes.ICE_SPIKES, Biomes.SNOWY_SLOPES, Biomes.GROVE, Biomes.JAGGED_PEAKS, Biomes.FROZEN_PEAKS,
            Biomes.STONY_PEAKS, Biomes.WINDSWEPT_HILLS, Biomes.WINDSWEPT_GRAVELLY_HILLS, Biomes.WINDSWEPT_FOREST, Biomes.DESERT,
            Biomes.SAVANNA, Biomes.SAVANNA_PLATEAU, Biomes.WINDSWEPT_SAVANNA, Biomes.BADLANDS, Biomes.WOODED_BADLANDS,
            Biomes.ERODED_BADLANDS, Biomes.JUNGLE, Biomes.SPARSE_JUNGLE, Biomes.BAMBOO_JUNGLE, Biomes.SWAMP, Biomes.MANGROVE_SWAMP,
            Biomes.RIVER, Biomes.FROZEN_RIVER, Biomes.BEACH, Biomes.SNOWY_BEACH, Biomes.STONY_SHORE, Biomes.WARM_OCEAN,
            Biomes.LUKEWARM_OCEAN, Biomes.DEEP_LUKEWARM_OCEAN, Biomes.OCEAN, Biomes.DEEP_OCEAN, Biomes.COLD_OCEAN,
            Biomes.DEEP_COLD_OCEAN, Biomes.FROZEN_OCEAN, Biomes.DEEP_FROZEN_OCEAN, Biomes.MUSHROOM_FIELDS, Biomes.DRIPSTONE_CAVES,
            Biomes.LUSH_CAVES);

    /**
     * Seit 0.79 auch die Biome, die bei Minecraft still sind: das End und das Tiefe Dunkel – und die Kampfdimensionen
     * der Bosse.
     */
    public static final List<ResourceKey<Biome>> EXTRA = List.of(Biomes.THE_END, Biomes.END_HIGHLANDS, Biomes.END_MIDLANDS,
            Biomes.SMALL_END_ISLANDS, Biomes.END_BARRENS, Biomes.DEEP_DARK);

    private static void put(ResourceKey<Biome> biome, AmbienceProfile.Builder builder) {
        PROFILES.put(biome, builder.build());
    }

    /** Dieselbe Klangwelt für mehrere Biome; {@code extra} ergänzt sie je Biom. */
    private static void putAll(List<ResourceKey<Biome>> biomes, Consumer<AmbienceProfile.Builder> base) {
        for (ResourceKey<Biome> biome : biomes) {
            AmbienceProfile.Builder builder = AmbienceProfile.builder();
            base.accept(builder);
            put(biome, builder);
        }
    }

    // =====================================================================================
    // Bausteine
    // =====================================================================================

    /** Laubwald am Tag: Laub rauscht, Amsel, Meise, Fink, Specht und Kuckuck; nachts Eulen und Insekten. */
    private static void woodland(AmbienceProfile.Builder b, float birds) {
        b.wind("wind_breeze", 0.2f)
                .spot("leaves_rustle", 2.2f, ALWAYS)
                .spot("bird_blackbird", 0.5f * birds, DAY)
                .spot("bird_tit", 0.4f * birds, DAY)
                .spot("bird_finch", 0.35f * birds, DAY)
                .spot("woodpecker", 0.5f, DAY)
                .spot("bird_cuckoo", 0.25f * birds, DAY)
                .spot("wood_creak", 0.5f, ALWAYS)
                .spot("twig_snap", 0.35f, ALWAYS)
                .spot("bird_owl", 0.9f, NIGHT)
                .close("fireflies", 1.1f, NIGHT);
    }

    /** Offenes Grasland: Wind streicht durchs Gras, ein paar Vögel; nachts Insekten. */
    private static void grassland(AmbienceProfile.Builder b) {
        b.wind("wind_breeze", 0.22f)
                .spot("grass_wind", 2.0f, ALWAYS)
                .spot("bird_finch", 0.35f, DAY)
                .spot("bird_tit", 0.25f, DAY)
                .spot("bird_crow", 0.35f, DAY)
                .spot("bird_owl", 0.3f, NIGHT)
                .close("fireflies", 1.1f, NIGHT)
                .spot("wolf_howl", 0.12f, NIGHT);
    }

    /** Nadelwald: Laub, knarrende Stämme, Specht und Krähen; nachts Eulen, Füchse und ferne Wölfe. */
    private static void conifers(AmbienceProfile.Builder b) {
        b.wind("wind_breeze", 0.25f)
                .spot("leaves_rustle", 1.2f, ALWAYS)
                .spot("wood_creak", 0.8f, ALWAYS)
                .spot("woodpecker", 0.5f, DAY)
                .spot("bird_crow", 0.4f, DAY)
                .spot("bird_owl", 1.0f, NIGHT)
                .spot("fox", 0.4f, NIGHT)
                .spot("wolf_howl", 0.4f, NIGHT);
    }

    /** Schnee und Eis: Sturmwind, knackendes Eis, nachts Wölfe. */
    private static void snow(AmbienceProfile.Builder b, float wind, float ice) {
        b.wind("wind_gale", wind)
                .spot("ice_creak", 0.4f * ice, ALWAYS)
                .spot("ice_crack", 0.4f * ice, ALWAYS)
                .spot("wolf_howl", 0.35f, NIGHT);
    }

    /** Offenes Meer: sanftes Plätschern, Seewind, Möwen über dem Wasser. */
    private static void sea(AmbienceProfile.Builder b) {
        b.outside("lagoon", 0.16f)
                .wind("wind_coast", 0.28f)
                .spot("bird_gull", 0.6f, DAY);
    }

    // =====================================================================================
    // Eigene Biome der Oberwelt
    // =====================================================================================

    private static void overworld() {
        // Aschefelder: trockener Wind, leise Glut, Glutspalten fauchen, Fels bröckelt, aus der Tiefe grollt es
        put(OverworldBiomes.ASH_FIELDS, AmbienceProfile.builder()
                .wind("wind_dry", 0.35f)
                .outside("embers", 0.12f)
                .everywhere("lava", 0.1f, 0.3f)
                .close("steam_vent", 1.0f, ALWAYS)
                .far("rumble", 0.35f, ALWAYS)
                .spot("basalt", 0.9f, ALWAYS)
                .spot("bird_crow", 0.3f, DAY));
        // Frostgipfel: heulender Sturm, Gletschereis arbeitet, nachts heulen Wölfe
        put(OverworldBiomes.FROST_PEAKS, AmbienceProfile.builder()
                .wind("wind_gale", 0.4f)
                .outside("ice", 0.12f)
                .spot("ice_crack", 0.8f, ALWAYS)
                .spot("ice_creak", 0.6f, ALWAYS)
                .far("rumble", 0.2f, ALWAYS)
                .spot("wolf_howl", 0.3f, NIGHT));
        // Sturmküste: Seewind, schwere Brandung, Möwen, fernes Gewitter
        put(OverworldBiomes.STORM_COAST, AmbienceProfile.builder()
                .wind("wind_coast", 0.38f)
                .everywhere("surf_storm", 0.3f, 0.2f)
                .spot("bird_gull", 1.5f, DAY)
                .far("thunder", 0.5f, ALWAYS)
                .far("thunder_far", 0.4f, ALWAYS));
        // Urhain: rauschendes Laub, Spechte und Kuckuck, ein paar Singvögel; nachts Eulen, Glühwürmchen und ferne Wölfe
        AmbienceProfile.Builder primeval = AmbienceProfile.builder();
        woodland(primeval, 1.2f);
        put(OverworldBiomes.PRIMEVAL_FOREST, primeval.spot("wolf_howl", 0.25f, NIGHT));
        // Felsschluchten: trockener Wind im Gras, Gestrüpp raschelt, ein Bussard kreist, Steine rieseln
        put(OverworldBiomes.ROCK_GORGES, AmbienceProfile.builder()
                .wind("wind_dry", 0.3f)
                .spot("grass_wind", 1.4f, ALWAYS)
                .close("desert_brush", 0.7f, ALWAYS)
                .sky("bird_buzzard", 0.8f, DAY)
                .spot("pebbles", 1.0f, ALWAYS)
                .spot("bird_crow", 0.35f, DAY)
                .spot("wolf_howl", 0.25f, NIGHT)
                .close("fireflies", 0.6f, NIGHT));
        // Korallenküste: sanfte Wellen und Lagune, Möwen, Schildkröten, Papageien in den Palmen, manchmal ein Delfin
        put(OverworldBiomes.CORAL_COAST, AmbienceProfile.builder()
                .everywhere("surf_gentle", 0.22f, 0.15f)
                .outside("lagoon", 0.2f)
                .wind("wind_breeze", 0.16f)
                .spot("bird_gull", 1.2f, DAY)
                .spot("dolphin", 0.5f, ALWAYS)
                .close("turtle", 0.4f, DAY)
                .spot("parrot", 0.4f, DAY)
                .close("fireflies", 0.8f, NIGHT));
        // Königsauen: Wind im Gras, Bienen, Finken und Meisen; nachts die Nachtigall und Glühwürmchen
        put(OverworldBiomes.ROYAL_MEADOWS, AmbienceProfile.builder()
                .wind("wind_breeze", 0.22f)
                .spot("grass_wind", 1.8f, ALWAYS)
                .close("bees", 1.2f, DAY)
                .spot("bird_finch", 0.5f, DAY)
                .spot("bird_tit", 0.4f, DAY)
                .spot("bird_blackbird", 0.35f, DAY)
                .spot("bird_cuckoo", 0.25f, DAY)
                .spot("bird_nightingale", 0.5f, NIGHT)
                .spot("bird_owl", 0.35f, NIGHT)
                .close("fireflies", 1.3f, NIGHT));
        // Nebelmoor: hohler Wind, blubbernder Torf, Brachvogel und Rohrdommel; nachts Frösche
        put(OverworldBiomes.MIST_MOOR, AmbienceProfile.builder()
                .wind("wind_moor", 0.3f)
                .outside("bog", 0.16f)
                .spot("frog", 1.2f, NIGHT)
                .spot("bird_curlew", 0.6f, DAY)
                .spot("bird_bittern", 0.4f, DUSK)
                .spot("bird_crow", 0.5f, DAY)
                .close("bog_burp", 1.0f, ALWAYS)
                .spot("leaves_rustle", 0.6f, ALWAYS)
                .spot("bird_owl", 0.4f, NIGHT)
                .spot("wolf_howl", 0.2f, NIGHT)
                .close("sparkle", 0.3f, NIGHT));
    }

    // =====================================================================================
    // Standard-Biome der Oberwelt (seit 0.61)
    // =====================================================================================

    private static void standardBiomes() {
        // Ebenen und Wiesen
        putAll(List.of(Biomes.PLAINS), b -> {
            grassland(b);
            b.close("bees", 0.5f, DAY).spot("bird_cuckoo", 0.2f, DAY);
        });
        putAll(List.of(Biomes.SUNFLOWER_PLAINS), b -> {
            grassland(b);
            b.close("bees", 1.2f, DAY);
        });
        putAll(List.of(Biomes.MEADOW), b -> b.wind("wind_breeze", 0.25f)
                .spot("grass_wind", 1.6f, ALWAYS)
                .close("bees", 1.6f, DAY)
                .spot("bird_finch", 0.4f, DAY)
                .spot("goat", 0.3f, ALWAYS)
                .spot("bird_nightingale", 0.3f, NIGHT)
                .close("fireflies", 1.0f, NIGHT));
        // Wälder
        putAll(List.of(Biomes.FOREST, Biomes.BIRCH_FOREST, Biomes.OLD_GROWTH_BIRCH_FOREST), b -> woodland(b, 1.0f));
        putAll(List.of(Biomes.FLOWER_FOREST), b -> {
            woodland(b, 1.0f);
            b.close("bees", 1.4f, DAY);
        });
        putAll(List.of(Biomes.DARK_FOREST), b -> b.wind("wind_breeze", 0.15f)
                .spot("leaves_rustle", 1.6f, ALWAYS)
                .spot("wood_creak", 1.0f, ALWAYS)
                .spot("twig_snap", 0.6f, ALWAYS)
                .spot("bird_crow", 0.6f, DAY)
                .spot("bird_owl", 1.4f, NIGHT)
                .spot("wolf_howl", 0.3f, NIGHT)
                .close("fireflies", 1.0f, NIGHT));
        putAll(List.of(Biomes.PALE_GARDEN), b -> b.wind("wind_breeze", 0.12f)
                .spot("pale_moss", 1.6f, ALWAYS)
                .close("eyeblossom", 1.2f, NIGHT)
                .spot("wood_creak", 0.8f, ALWAYS));
        putAll(List.of(Biomes.CHERRY_GROVE), b -> b.wind("wind_breeze", 0.22f)
                .spot("leaves_rustle", 1.6f, ALWAYS)
                .close("bees", 1.4f, DAY)
                .spot("bird_finch", 0.4f, DAY)
                .spot("bird_tit", 0.3f, DAY)
                .spot("bird_cuckoo", 0.3f, DAY)
                .spot("bird_nightingale", 0.3f, NIGHT)
                .close("fireflies", 1.0f, NIGHT));
        putAll(List.of(Biomes.TAIGA, Biomes.OLD_GROWTH_PINE_TAIGA, Biomes.OLD_GROWTH_SPRUCE_TAIGA), AmbienceProfiles::conifers);
        // Schnee, Eis und Gipfel
        putAll(List.of(Biomes.SNOWY_TAIGA), b -> {
            snow(b, 0.3f, 0.6f);
            b.spot("wood_creak", 0.5f, ALWAYS).spot("fox", 0.3f, NIGHT);
        });
        putAll(List.of(Biomes.SNOWY_PLAINS), b -> snow(b, 0.32f, 0.6f));
        putAll(List.of(Biomes.ICE_SPIKES), b -> snow(b, 0.32f, 2.0f));
        putAll(List.of(Biomes.SNOWY_SLOPES, Biomes.GROVE), b -> {
            snow(b, 0.4f, 0.8f);
            b.spot("goat", 0.3f, ALWAYS);
        });
        putAll(List.of(Biomes.JAGGED_PEAKS, Biomes.FROZEN_PEAKS), b -> {
            snow(b, 0.45f, 1.0f);
            b.spot("pebbles", 0.6f, ALWAYS).spot("goat", 0.3f, ALWAYS);
        });
        putAll(List.of(Biomes.STONY_PEAKS), b -> b.wind("wind_gale", 0.42f)
                .spot("pebbles", 0.8f, ALWAYS)
                .spot("goat", 0.4f, ALWAYS)
                .sky("bird_buzzard", 0.4f, DAY));
        putAll(List.of(Biomes.WINDSWEPT_HILLS, Biomes.WINDSWEPT_GRAVELLY_HILLS), b -> b.wind("wind_gale", 0.35f)
                .spot("grass_wind", 1.0f, ALWAYS)
                .spot("pebbles", 0.6f, ALWAYS)
                .spot("goat", 0.3f, ALWAYS)
                .sky("bird_buzzard", 0.5f, DAY)
                .spot("bird_crow", 0.3f, DAY)
                .spot("wolf_howl", 0.3f, NIGHT));
        putAll(List.of(Biomes.WINDSWEPT_FOREST), b -> {
            conifers(b);
            b.spot("pebbles", 0.4f, ALWAYS);
        });
        // Trockene Lande
        putAll(List.of(Biomes.DESERT), b -> b.wind("wind_dry", 0.3f)
                .spot("sand_wind", 2.0f, ALWAYS)
                .close("desert_brush", 1.0f, ALWAYS)
                .sky("bird_buzzard", 0.3f, DAY));
        putAll(List.of(Biomes.SAVANNA, Biomes.SAVANNA_PLATEAU, Biomes.WINDSWEPT_SAVANNA), b -> b.wind("wind_dry", 0.25f)
                .spot("grass_wind", 2.0f, ALWAYS)
                .close("desert_brush", 0.6f, ALWAYS)
                .sky("bird_buzzard", 0.6f, DAY)
                .spot("bird_crow", 0.3f, DAY)
                .close("fireflies", 0.8f, NIGHT));
        putAll(List.of(Biomes.BADLANDS, Biomes.WOODED_BADLANDS, Biomes.ERODED_BADLANDS), b -> b.wind("wind_dry", 0.3f)
                .spot("sand_wind", 1.2f, ALWAYS)
                .spot("pebbles", 1.0f, ALWAYS)
                .sky("bird_buzzard", 0.5f, DAY)
                .close("desert_brush", 0.6f, ALWAYS));
        // Dschungel und Sumpf
        putAll(List.of(Biomes.JUNGLE, Biomes.SPARSE_JUNGLE, Biomes.BAMBOO_JUNGLE), b -> b.wind("wind_breeze", 0.15f)
                .spot("leaves_rustle", 2.0f, ALWAYS)
                .spot("parrot", 2.0f, DAY)
                .spot("woodpecker", 0.3f, DAY)
                .spot("frog", 0.6f, NIGHT)
                .spot("bird_owl", 0.6f, NIGHT)
                .close("fireflies", 1.4f, NIGHT));
        putAll(List.of(Biomes.SWAMP, Biomes.MANGROVE_SWAMP), b -> b.wind("wind_moor", 0.2f)
                .outside("bog", 0.15f)
                .spot("frog", 1.6f, NIGHT)
                .spot("frog", 0.4f, DAY)
                .close("bog_burp", 0.8f, ALWAYS)
                .spot("bird_crow", 0.4f, DAY)
                .spot("bird_bittern", 0.3f, DUSK)
                .spot("bird_owl", 0.5f, NIGHT)
                .close("fireflies", 1.4f, NIGHT));
        // Wasser: Flüsse, Strände und Meere
        putAll(List.of(Biomes.RIVER), b -> b.wind("wind_breeze", 0.14f)
                .close("water_flow", 1.4f, ALWAYS)
                .spot("leaves_rustle", 0.5f, ALWAYS)
                .spot("frog", 0.6f, NIGHT)
                .spot("bird_curlew", 0.3f, DAY)
                .close("fireflies", 0.8f, NIGHT));
        putAll(List.of(Biomes.FROZEN_RIVER), b -> b.wind("wind_gale", 0.25f)
                .spot("ice_creak", 0.8f, ALWAYS)
                .spot("ice_crack", 0.5f, ALWAYS));
        putAll(List.of(Biomes.BEACH), b -> b.everywhere("surf_gentle", 0.28f, 0.15f)
                .wind("wind_coast", 0.25f)
                .spot("bird_gull", 1.6f, DAY)
                .close("turtle", 0.4f, DAY)
                .spot("dolphin", 0.2f, ALWAYS));
        putAll(List.of(Biomes.SNOWY_BEACH), b -> b.everywhere("surf_gentle", 0.24f, 0.15f)
                .wind("wind_coast", 0.3f)
                .spot("bird_gull", 0.6f, DAY)
                .spot("ice_creak", 0.3f, ALWAYS));
        putAll(List.of(Biomes.STONY_SHORE), b -> b.everywhere("surf_storm", 0.24f, 0.15f)
                .wind("wind_coast", 0.3f)
                .spot("bird_gull", 1.2f, DAY)
                .spot("pebbles", 0.5f, ALWAYS));
        putAll(List.of(Biomes.WARM_OCEAN, Biomes.LUKEWARM_OCEAN), b -> {
            sea(b);
            b.spot("dolphin", 0.8f, ALWAYS).spot("turtle", 0.2f, DAY);
        });
        putAll(List.of(Biomes.DEEP_LUKEWARM_OCEAN), b -> {
            sea(b);
            b.spot("dolphin", 0.7f, ALWAYS).far("whale", 0.3f, ALWAYS);
        });
        putAll(List.of(Biomes.OCEAN), b -> {
            sea(b);
            b.spot("dolphin", 0.4f, ALWAYS);
        });
        putAll(List.of(Biomes.DEEP_OCEAN), b -> {
            sea(b);
            b.spot("dolphin", 0.3f, ALWAYS).far("whale", 0.4f, ALWAYS).far("whale_far", 0.3f, ALWAYS);
        });
        putAll(List.of(Biomes.COLD_OCEAN, Biomes.DEEP_COLD_OCEAN), b -> b.outside("lagoon", 0.14f)
                .wind("wind_gale", 0.3f)
                .spot("bird_gull", 0.3f, DAY)
                .far("whale_far", 0.25f, ALWAYS));
        putAll(List.of(Biomes.FROZEN_OCEAN, Biomes.DEEP_FROZEN_OCEAN), b -> b.wind("wind_gale", 0.35f)
                .spot("ice_creak", 0.6f, ALWAYS)
                .spot("ice_crack", 0.4f, ALWAYS)
                .far("whale_far", 0.2f, ALWAYS));
        putAll(List.of(Biomes.MUSHROOM_FIELDS), b -> b.outside("lagoon", 0.14f)
                .wind("wind_breeze", 0.18f)
                .spot("bird_gull", 0.6f, DAY)
                .close("bog_burp", 0.4f, ALWAYS));
        // Höhlen: Tropfen und Wasser, auch tief unter der Erde
        putAll(List.of(Biomes.DRIPSTONE_CAVES), b -> b.everywhere("drips", 0.2f, 1.0f).close("drip", 1.2f, ALWAYS));
        putAll(List.of(Biomes.LUSH_CAVES), b -> b.everywhere("drips", 0.14f, 1.0f).close("drip", 0.8f, ALWAYS)
                .close("bubbles", 0.4f, ALWAYS));
    }

    // =====================================================================================
    // Drachennester
    // =====================================================================================

    private static void nests() {
        put(ModBiomes.ASHLANDS, AmbienceProfile.builder()
                .everywhere("lava", 0.25f, 0.4f)
                .outside("embers", 0.15f)
                .wind("wind_dry", 0.22f)
                .close("steam_vent", 1.2f, ALWAYS)
                .far("rumble", 0.5f, ALWAYS)
                .spot("basalt", 1.2f, ALWAYS)
                .far("dragon_far", 0.35f, ALWAYS)
                .far("dragon_wings", 0.2f, ALWAYS));
        put(ModBiomes.FROSTBASIN, AmbienceProfile.builder()
                .wind("wind_gale", 0.38f)
                .outside("ice", 0.15f)
                .spot("ice_crack", 1.0f, ALWAYS)
                .spot("ice_creak", 0.8f, ALWAYS)
                .far("rumble", 0.25f, ALWAYS)
                .far("dragon_far", 0.3f, ALWAYS)
                .spot("wolf_howl", 0.25f, NIGHT));
        put(ModBiomes.RUGGED_WILDS, AmbienceProfile.builder()
                .wind("wind_dry", 0.3f)
                .everywhere("earth_rumble", 0.2f, 0.5f)
                .spot("grass_wind", 1.0f, ALWAYS)
                .spot("pebbles", 1.2f, ALWAYS)
                .far("rumble", 0.4f, ALWAYS)
                .sky("bird_buzzard", 0.5f, DAY)
                .far("dragon_far", 0.3f, ALWAYS));
        put(ModBiomes.ANCIENT_GROVE, AmbienceProfile.builder()
                .outside("magic_grove", 0.3f)
                .spot("leaves_rustle", 1.8f, ALWAYS)
                .close("sparkle", 1.0f, ALWAYS)
                .spot("bird_blackbird", 0.5f, DAY)
                .spot("woodpecker", 0.4f, DAY)
                .spot("wood_creak", 0.7f, ALWAYS)
                .spot("bird_owl", 0.8f, NIGHT)
                .close("fireflies", 1.6f, NIGHT)
                .far("dragon_far", 0.25f, ALWAYS));
        put(ModBiomes.STORM_CLIFFS, AmbienceProfile.builder()
                .wind("wind_gale", 0.4f)
                .outside("storm_charge", 0.2f)
                .far("thunder", 1.0f, ALWAYS)
                .far("thunder_far", 0.6f, ALWAYS)
                .close("zap", 0.6f, ALWAYS)
                .far("dragon_far", 0.3f, ALWAYS));
        put(ModBiomes.ABYSSAL_GROTTO, AmbienceProfile.builder()
                .everywhere("drips", 0.3f, 1.0f)
                .everywhere("deep_water", 0.25f, 1.0f)
                .close("drip", 1.4f, ALWAYS)
                .far("whale", 0.3f, ALWAYS)
                .far("whale_far", 0.2f, ALWAYS)
                .close("bog_burp", 0.3f, ALWAYS)
                .far("dragon_far", 0.25f, ALWAYS));
    }

    // =====================================================================================
    // Schattenwelt
    // =====================================================================================

    private static void shadowWorld() {
        put(ShadowBiomes.SHADOW_FOREST, AmbienceProfile.builder()
                .everywhere("shadow_forest", 0.36f, 0.45f)
                .everywhere("whispers", 0.08f, 0.3f)
                .spot("leaves_rustle", 0.9f, ALWAYS)
                .spot("shadow_call", 0.8f, ALWAYS)
                .spot("shadow_creak", 0.9f, ALWAYS)
                .spot("pale_moss", 1.1f, ALWAYS)
                .spot("warped_creak", 0.6f, ALWAYS)
                .close("whisper", 0.35f, ALWAYS));
        put(ShadowBiomes.PALE_WASTES, AmbienceProfile.builder()
                .everywhere("shadow_wastes", 0.4f, 0.35f)
                .spot("wolf_howl", 0.4f, ALWAYS)
                .spot("shadow_call", 0.4f, ALWAYS)
                .far("rumble", 0.3f, ALWAYS)
                .spot("soul_voices", 0.35f, ALWAYS));
        put(ShadowBiomes.CRYSTAL_CHASMS, AmbienceProfile.builder()
                .everywhere("shadow_crystal", 0.36f, 0.6f)
                .spot("crystal_chime", 1.4f, ALWAYS)
                .spot("amethyst", 1.2f, ALWAYS)
                .spot("pebbles", 0.6f, ALWAYS)
                .far("deep_moan", 0.3f, ALWAYS));
        put(ShadowBiomes.SOUL_MARSH, AmbienceProfile.builder()
                .everywhere("shadow_marsh", 0.36f, 0.4f)
                .outside("bog", 0.18f)
                .spot("soul_wail", 0.8f, ALWAYS)
                .spot("soul_voices", 0.8f, ALWAYS)
                .close("bog_burp", 1.2f, ALWAYS)
                .close("whisper", 0.35f, ALWAYS));
        put(ShadowBiomes.SHADOW_PEAKS, AmbienceProfile.builder()
                .bed(new AmbienceProfile.Bed("shadow_peaks", 0.4f, ALWAYS, 0.1f, 1.0f, 0.8f))
                .spot("wolf_howl", 0.5f, ALWAYS)
                .spot("shadow_call", 0.5f, ALWAYS)
                .spot("pebbles", 0.8f, ALWAYS)
                .far("rumble", 0.3f, ALWAYS));
        put(ShadowBiomes.GLIMMER_MEADOWS, AmbienceProfile.builder()
                .everywhere("shadow_glimmer", 0.36f, 0.4f)
                .close("sparkle", 1.4f, ALWAYS)
                .spot("wing_flutter", 0.8f, ALWAYS)
                .spot("shadow_call", 0.5f, ALWAYS)
                .close("fireflies", 1.2f, ALWAYS));
        put(ShadowBiomes.ABYSSAL_SEA, AmbienceProfile.builder()
                .everywhere("shadow_sea", 0.4f, 0.4f)
                .outside("surf_gentle", 0.16f)
                .far("whale", 0.5f, ALWAYS)
                .far("whale_far", 0.35f, ALWAYS)
                .far("deep_moan", 0.3f, ALWAYS));
        put(ShadowBiomes.FLOATING_ISLES, AmbienceProfile.builder()
                .everywhere("shadow_isles", 0.36f, 0.35f)
                .wind("wind_gale", 0.18f)
                .spot("wing_flutter", 1.0f, ALWAYS)
                .far("dragon_wings", 0.3f, ALWAYS)
                .spot("crystal_chime", 0.6f, ALWAYS)
                .spot("shadow_call", 0.5f, ALWAYS));
        put(ShadowBiomes.SHADOW_DEPTHS, AmbienceProfile.builder()
                .everywhere("shadow_depths", 0.42f, 1.0f)
                .everywhere("drips", 0.2f, 1.0f)
                .everywhere("whispers", 0.12f, 1.0f)
                .far("deep_moan", 0.5f, ALWAYS)
                .close("drip", 1.2f, ALWAYS)
                .close("whisper", 0.5f, ALWAYS)
                .spot("soul_voices", 0.4f, ALWAYS, 8, 24, -6, 6, false, 0.8f, 1.0f));
    }

    // =====================================================================================
    // Die Leere
    // =====================================================================================

    /**
     * Die Leere: das tiefe, schwebende Dröhnen des Nichts mit feinem Sternensirren; dazu je Biom gläserne Akkorde und
     * singende Kristalle, rieselnder Sternenstaub, Chöre der Alten zwischen schabenden Steinen oder dumpfe Erinnerungen an
     * verlorene Welten. Aus der Ferne singen Sternenrochen.
     */
    private static void theVoid() {
        put(VoidBiomes.EXPANSE, AmbienceProfile.builder()
                .everywhere("void_hum", 0.42f, 0.6f)
                .wind("void_wind", 0.24f)
                .far("star_song", 0.6f, ALWAYS)
                .far("deep_moan", 0.2f, ALWAYS)
                .spot("rock_grind", 0.4f, ALWAYS)
                .spot("sparkle", 0.5f, ALWAYS));
        put(VoidBiomes.CRYSTAL_FOREST, AmbienceProfile.builder()
                .everywhere("void_hum", 0.28f, 0.4f)
                .everywhere("void_crystal", 0.32f, 0.5f)
                .spot("crystal_chime", 1.2f, ALWAYS)
                .spot("amethyst", 0.9f, ALWAYS)
                .spot("glitch_far", 0.3f, ALWAYS)
                .far("star_song", 0.3f, ALWAYS));
        put(VoidBiomes.WEIGHTLESS_RUINS, AmbienceProfile.builder()
                .everywhere("void_hum", 0.28f, 0.4f)
                .everywhere("void_ruins", 0.36f, 0.6f)
                .wind("void_wind", 0.14f)
                .far("void_choir", 0.5f, ALWAYS)
                .spot("rock_grind", 0.7f, ALWAYS)
                .close("whisper", 0.35f, ALWAYS)
                .spot("pebbles", 0.4f, ALWAYS));
        put(VoidBiomes.STARDUST_FIELDS, AmbienceProfile.builder()
                .everywhere("void_hum", 0.3f, 0.4f)
                .everywhere("void_stardust", 0.32f, 0.4f)
                .spot("sparkle", 1.0f, ALWAYS)
                .far("star_song", 0.5f, ALWAYS));
        put(VoidBiomes.SHARD_CLIFFS, AmbienceProfile.builder()
                .everywhere("void_hum", 0.36f, 0.5f)
                .wind("void_wind", 0.3f)
                .everywhere("void_crystal", 0.18f, 0.3f)
                .spot("crystal_chime", 0.7f, ALWAYS)
                .spot("rock_grind", 0.6f, ALWAYS)
                .spot("pebbles", 0.5f, ALWAYS)
                .far("deep_moan", 0.3f, ALWAYS));
        put(VoidBiomes.WORLD_REMNANTS, AmbienceProfile.builder()
                .everywhere("void_hum", 0.28f, 0.4f)
                .everywhere("void_remnants", 0.36f, 0.5f)
                .spot("glitch_far", 0.5f, ALWAYS)
                .far("void_choir", 0.2f, ALWAYS)
                .far("star_song", 0.2f, ALWAYS));
    }
    // =====================================================================================
    // Das Feenreich
    // =====================================================================================

    /**
     * Das Feenreich: ein leiser, magischer Hain-Teppich, der kommt und geht; darüber Abendvögel, das Funkeln und Klingen
     * der Feen, rauschendes Laub und summende Bienen – im Pilzhain tropft und flirrt es, an den Mondseen quaken Frösche
     * und plätschert Wasser, in der Dornenwildnis knackt und raschelt es, und manchmal flüstert etwas.
     */
    private static void faeRealm() {
        put(FaeBiomes.FAE_MEADOWS, AmbienceProfile.builder()
                .outside("magic_grove", 0.18f)
                .wind("wind_breeze", 0.12f)
                .spot("grass_wind", 1.0f, ALWAYS)
                .spot("bees", 0.9f, ALWAYS)
                .spot("bird_nightingale", 0.7f, ALWAYS)
                .spot("bird_finch", 0.5f, ALWAYS)
                .close("sparkle", 1.2f, ALWAYS)
                .spot("wing_flutter", 0.4f, ALWAYS));
        put(FaeBiomes.GLIMMERWOOD, AmbienceProfile.builder()
                .outside("magic_grove", 0.26f)
                .spot("leaves_rustle", 1.6f, ALWAYS)
                .spot("bird_nightingale", 0.9f, ALWAYS)
                .spot("bird_tit", 0.5f, ALWAYS)
                .close("sparkle", 1.4f, ALWAYS)
                .close("fireflies", 1.0f, ALWAYS)
                .spot("crystal_chime", 0.5f, ALWAYS)
                .spot("wood_creak", 0.4f, ALWAYS));
        put(FaeBiomes.MUSHROOM_GROVE, AmbienceProfile.builder()
                .outside("magic_grove", 0.2f)
                .everywhere("shadow_glimmer", 0.1f, 0.2f)
                .spot("drip", 0.8f, ALWAYS)
                .close("sparkle", 1.6f, ALWAYS)
                .spot("eyeblossom", 0.8f, ALWAYS)
                .spot("amethyst", 0.6f, ALWAYS)
                .close("fireflies", 1.2f, ALWAYS));
        put(FaeBiomes.MOONLIT_LAKES, AmbienceProfile.builder()
                .outside("magic_grove", 0.14f)
                .wind("wind_breeze", 0.1f)
                .spot("frog", 1.4f, ALWAYS)
                .spot("water_flow", 0.8f, ALWAYS)
                .spot("bird_nightingale", 0.5f, ALWAYS)
                .close("sparkle", 1.0f, ALWAYS)
                .spot("crystal_chime", 0.4f, ALWAYS));
        put(FaeBiomes.THORNWILD, AmbienceProfile.builder()
                .everywhere("whispers", 0.08f, 0.25f)
                .wind("wind_moor", 0.14f)
                .spot("twig_snap", 1.0f, ALWAYS)
                .spot("leaves_rustle", 1.0f, ALWAYS)
                .spot("bird_crow", 0.6f, ALWAYS)
                .spot("wood_creak", 0.8f, ALWAYS)
                .close("whisper", 0.3f, ALWAYS)
                .spot("bird_owl", 0.4f, ALWAYS));
    }
    // =====================================================================================
    // Der Abgrund
    // =====================================================================================

    /**
     * Der Abgrund: unter allem liegt das schiefe Dröhnen des Chaos, durch das ab und zu ein fernes Reißen geht; in der Ferne
     * reißt die Welt weiter auf. Dazu klingt in jedem Biom die Welt nach, aus der seine Bruchstücke stammen – Blätter und
     * Krähen in den Bruchlanden, Glut und Lava im Glutsturz, singende Kristalle in den Sternentrümmern, Klagen und Flüstern
     * im Schattenschlund. In der Naht knistern die Weltennähte.
     */
    private static void abyss() {
        put(AbyssBiomes.FRACTURED_LANDS, AmbienceProfile.builder()
                .everywhere("abyss_chaos", 0.34f, 0.5f)
                .wind("void_wind", 0.16f)
                .far("world_tear", 0.5f, ALWAYS)
                .spot("leaves_rustle", 0.6f, ALWAYS)
                .spot("bird_crow", 0.3f, ALWAYS)
                .spot("rock_grind", 0.6f, ALWAYS)
                .spot("pebbles", 0.5f, ALWAYS));
        put(AbyssBiomes.EMBER_FALLS, AmbienceProfile.builder()
                .everywhere("abyss_chaos", 0.28f, 0.4f)
                .everywhere("lava", 0.2f, 0.3f)
                .outside("embers", 0.12f)
                .far("world_tear", 0.5f, ALWAYS)
                .spot("steam_vent", 0.6f, ALWAYS)
                .spot("basalt", 0.7f, ALWAYS)
                .far("rumble", 0.4f, ALWAYS));
        put(AbyssBiomes.STAR_WRECK, AmbienceProfile.builder()
                .everywhere("abyss_chaos", 0.26f, 0.4f)
                .everywhere("void_crystal", 0.16f, 0.3f)
                .far("world_tear", 0.4f, ALWAYS)
                .spot("crystal_chime", 0.8f, ALWAYS)
                .spot("amethyst", 0.6f, ALWAYS)
                .spot("glitch_far", 0.4f, ALWAYS)
                .far("star_song", 0.25f, ALWAYS));
        put(AbyssBiomes.SHADOW_CHASM, AmbienceProfile.builder()
                .everywhere("abyss_chaos", 0.28f, 0.4f)
                .everywhere("whispers", 0.08f, 0.3f)
                .far("world_tear", 0.4f, ALWAYS)
                .spot("shadow_call", 0.5f, ALWAYS)
                .spot("soul_voices", 0.5f, ALWAYS)
                .far("deep_moan", 0.35f, ALWAYS)
                .close("whisper", 0.3f, ALWAYS));
        put(AbyssBiomes.THE_SEAM, AmbienceProfile.builder()
                .everywhere("abyss_chaos", 0.42f, 0.6f)
                .far("world_tear", 0.8f, ALWAYS)
                .spot("zap", 0.8f, ALWAYS)
                .spot("glitch_far", 0.6f, ALWAYS)
                .far("rumble", 0.4f, ALWAYS)
                .far("void_choir", 0.25f, ALWAYS)
                .close("whisper", 0.3f, ALWAYS));
    }
    // =====================================================================================
    // Das End, das Tiefe Dunkel, die Kampfdimensionen und die Klangwelten nach Merkmalen (seit 0.79)
    // =====================================================================================

    private static void theEnd() {
        putAll(List.of(Biomes.THE_END, Biomes.END_HIGHLANDS, Biomes.END_MIDLANDS, Biomes.SMALL_END_ISLANDS, Biomes.END_BARRENS), b -> b
                .everywhere("void_hum", 0.3f, 0.5f)
                .wind("void_wind", 0.22f)
                .far("star_song", 0.4f, ALWAYS)
                .spot("rock_grind", 0.4f, ALWAYS)
                .spot("sparkle", 0.3f, ALWAYS)
                .far("deep_moan", 0.2f, ALWAYS));
        put(Biomes.DEEP_DARK, AmbienceProfile.builder()
                .everywhere("drips", 0.16f, 1.0f)
                .everywhere("earth_rumble", 0.12f, 1.0f)
                .close("drip", 0.9f, ALWAYS)
                .far("deep_moan", 0.5f, ALWAYS)
                .close("whisper", 0.3f, ALWAYS)
                .far("rumble", 0.3f, ALWAYS));
    }

    private static void arenas() {
        put(io.github.sirschrolli.kingdomsatwar.arena.ArcaneSphere.BIOME, AmbienceProfile.builder()
                .everywhere("magic_grove", 0.22f, 1.0f)
                .everywhere("void_hum", 0.14f, 1.0f)
                .close("sparkle", 0.8f, ALWAYS)
                .spot("crystal_chime", 0.7f, ALWAYS, 8, 24, -2, 8, false, 0.9f, 1.0f)
                .spot("amethyst", 0.5f, ALWAYS, 8, 24, -2, 8, false, 0.9f, 1.0f));
        put(io.github.sirschrolli.kingdomsatwar.arena.BossArenaKind.MYSTIK.biome(), AmbienceProfile.builder()
                .everywhere("magic_grove", 0.22f, 1.0f)
                .wind("wind_breeze", 0.14f)
                .close("sparkle", 0.9f, ALWAYS)
                .close("fireflies", 0.8f, ALWAYS)
                .spot("bird_nightingale", 0.5f, ALWAYS));
        put(io.github.sirschrolli.kingdomsatwar.arena.BossArenaKind.MALLEUS.biome(), AmbienceProfile.builder()
                .everywhere("lava", 0.24f, 1.0f)
                .everywhere("embers", 0.16f, 1.0f)
                .spot("steam_vent", 0.8f, ALWAYS, 8, 26, -2, 6, false, 1.0f, 1.0f)
                .spot("basalt", 0.6f, ALWAYS, 8, 26, -2, 6, false, 1.0f, 1.0f)
                .far("rumble", 0.5f, ALWAYS));
        put(io.github.sirschrolli.kingdomsatwar.arena.BossArenaKind.ASTERIA.biome(), AmbienceProfile.builder()
                .everywhere("void_stardust", 0.24f, 1.0f)
                .everywhere("void_hum", 0.16f, 1.0f)
                .far("star_song", 0.7f, ALWAYS)
                .close("sparkle", 0.7f, ALWAYS)
                .spot("crystal_chime", 0.5f, ALWAYS, 8, 26, -2, 10, false, 0.9f, 1.0f));
        put(io.github.sirschrolli.kingdomsatwar.arena.BossArenaKind.ANNI.biome(), AmbienceProfile.builder()
                .everywhere("void_hum", 0.2f, 1.0f)
                .spot("glitch_far", 1.4f, ALWAYS, 8, 26, -2, 8, false, 0.9f, 1.0f)
                .spot("zap", 0.8f, ALWAYS, 8, 26, -2, 8, false, 0.8f, 1.0f)
                .far("world_tear", 0.3f, ALWAYS));
        put(io.github.sirschrolli.kingdomsatwar.arena.BossArenaKind.KING.biome(), AmbienceProfile.builder()
                .everywhere("void_ruins", 0.26f, 1.0f)
                .everywhere("void_hum", 0.18f, 1.0f)
                .wind("void_wind", 0.14f)
                .far("void_choir", 0.6f, ALWAYS)
                .spot("rock_grind", 0.6f, ALWAYS, 10, 30, -2, 8, false, 1.0f, 1.0f)
                .close("whisper", 0.3f, ALWAYS));
        put(io.github.sirschrolli.kingdomsatwar.arena.BossArenaKind.WORLD_EATER.biome(), AmbienceProfile.builder()
                .everywhere("abyss_chaos", 0.36f, 1.0f)
                .wind("void_wind", 0.14f)
                .far("world_tear", 0.8f, ALWAYS)
                .far("rumble", 0.5f, ALWAYS)
                .spot("rock_grind", 0.6f, ALWAYS, 10, 30, -2, 8, false, 1.0f, 1.0f)
                .spot("pebbles", 0.5f, ALWAYS, 8, 24, -2, 6, false, 1.0f, 1.0f));
        put(io.github.sirschrolli.kingdomsatwar.arena.BossArenaKind.CREATOR.biome(), AmbienceProfile.builder()
                .everywhere("magic_grove", 0.2f, 1.0f)
                .wind("wind_breeze", 0.16f)
                .far("void_choir", 0.6f, ALWAYS)
                .far("star_song", 0.4f, ALWAYS)
                .close("sparkle", 0.8f, ALWAYS)
                .spot("crystal_chime", 0.4f, ALWAYS, 8, 26, -2, 10, false, 0.9f, 1.0f));
    }

    private static void fallbacks() {
        FALLBACKS.put("woodland", build(b -> woodland(b, 1.0f)));
        FALLBACKS.put("grassland", build(AmbienceProfiles::grassland));
        FALLBACKS.put("conifers", build(AmbienceProfiles::conifers));
        FALLBACKS.put("sea", build(AmbienceProfiles::sea));
        FALLBACKS.put("river", PROFILES.get(Biomes.RIVER));
        FALLBACKS.put("beach", PROFILES.get(Biomes.BEACH));
        FALLBACKS.put("peaks", PROFILES.get(Biomes.STONY_PEAKS));
        FALLBACKS.put("dry", PROFILES.get(Biomes.SAVANNA));
        FALLBACKS.put("end", PROFILES.get(Biomes.THE_END));
    }

    private static AmbienceProfile build(Consumer<AmbienceProfile.Builder> base) {
        AmbienceProfile.Builder builder = AmbienceProfile.builder();
        base.accept(builder);
        return builder.build();
    }
}
