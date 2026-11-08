package io.github.sirschrolli.kingdomsatwar.registry;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Klangereignisse der Umgebungsklänge (erzeugt von tools/audio/ambience.py – nicht von Hand ändern): Teppiche
 * ({@code ambient.bed.*}, nahtlose Schleifen) und Einzelklänge ({@code ambient.spot.*}, an zufälligen Orten um den
 * Spieler). Welche wo erklingen, steht in {@code client.ambience.AmbienceProfiles}.
 */
public final class AmbientSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, KingdomsAtWar.MODID);
    public static final List<String> BEDS = List.of(
            "wind_breeze", "wind_gale", "wind_coast", "wind_dry", "wind_moor", "leaves",
            "reeds", "birds_forest", "birds_meadow", "crickets", "crickets_dry", "insects_day",
            "frogs", "surf_gentle", "surf_storm", "lagoon", "bog", "embers",
            "lava", "ice", "storm_charge", "drips", "deep_water", "magic_grove",
            "earth_rumble", "shadow_forest", "shadow_wastes", "shadow_crystal", "shadow_marsh", "shadow_peaks",
            "shadow_glimmer", "shadow_sea", "shadow_isles", "shadow_depths", "whispers", "void_hum",
            "void_wind", "void_crystal", "void_stardust", "void_ruins", "void_remnants");
    public static final List<String> SPOTS = List.of(
            "bird_blackbird", "bird_tit", "bird_finch", "bird_cuckoo", "woodpecker", "bird_owl",
            "bird_nightingale", "bird_crow", "bird_gull", "bird_curlew", "bird_bittern", "bird_buzzard",
            "wolf_howl", "wood_creak", "twig_snap", "ice_crack", "ice_creak", "pebbles",
            "steam_vent", "rumble", "thunder_far", "zap", "bog_burp", "drip",
            "whale_far", "sparkle", "whisper", "shadow_call", "soul_wail", "crystal_chime",
            "deep_moan", "wing_flutter", "shadow_creak", "star_song", "rock_grind", "glitch_far",
            "void_choir", "dragon_far", "dragon_wings", "fireflies", "amethyst", "soul_voices",
            "basalt", "thunder", "dolphin", "frog", "whale", "pale_moss",
            "warped_creak");
    /** Untertitel der Einzelklänge: Englisch, Deutsch. */
    public static final Map<String, List<String>> SUBTITLES = Map.ofEntries(
            Map.entry("bird_blackbird", List.of("Blackbird sings", "Amsel singt")),
            Map.entry("bird_tit", List.of("Tit calls", "Meise ruft")),
            Map.entry("bird_finch", List.of("Finch sings", "Fink schmettert")),
            Map.entry("bird_cuckoo", List.of("Cuckoo calls", "Kuckuck ruft")),
            Map.entry("woodpecker", List.of("Woodpecker drums", "Specht trommelt")),
            Map.entry("bird_owl", List.of("Owl hoots", "Eule ruft")),
            Map.entry("bird_nightingale", List.of("Nightingale sings", "Nachtigall singt")),
            Map.entry("bird_crow", List.of("Crow caws", "Krähe krächzt")),
            Map.entry("bird_gull", List.of("Gull cries", "Möwe kreischt")),
            Map.entry("bird_curlew", List.of("Curlew calls", "Brachvogel ruft")),
            Map.entry("bird_bittern", List.of("Bittern booms", "Rohrdommel dröhnt")),
            Map.entry("bird_buzzard", List.of("Buzzard cries", "Bussard schreit")),
            Map.entry("wolf_howl", List.of("Distant wolf howls", "Wolf heult in der Ferne")),
            Map.entry("wood_creak", List.of("Tree creaks", "Baum knarrt")),
            Map.entry("twig_snap", List.of("Twig snaps", "Ast knackt")),
            Map.entry("ice_crack", List.of("Ice cracks", "Eis knackt")),
            Map.entry("ice_creak", List.of("Ice groans", "Eis ächzt")),
            Map.entry("pebbles", List.of("Pebbles fall", "Steine rieseln")),
            Map.entry("steam_vent", List.of("Vent hisses", "Glutspalte faucht")),
            Map.entry("rumble", List.of("Deep rumble", "Grollen aus der Tiefe")),
            Map.entry("thunder_far", List.of("Distant thunder", "Donner in der Ferne")),
            Map.entry("zap", List.of("Sparks crackle", "Funken knistern")),
            Map.entry("bog_burp", List.of("Bog bubbles", "Moor blubbert")),
            Map.entry("drip", List.of("Water drips", "Wasser tropft")),
            Map.entry("whale_far", List.of("Call from the deep", "Rufen aus der Tiefe")),
            Map.entry("sparkle", List.of("Chimes sparkle", "Glöckchen funkeln")),
            Map.entry("whisper", List.of("Whispers", "Flüstern")),
            Map.entry("shadow_call", List.of("Shadow bird wails", "Schattenvogel klagt")),
            Map.entry("soul_wail", List.of("Souls wail", "Seelen klagen")),
            Map.entry("crystal_chime", List.of("Crystal chimes", "Kristall singt")),
            Map.entry("deep_moan", List.of("Something groans below", "Etwas dröhnt in der Tiefe")),
            Map.entry("wing_flutter", List.of("Wings flutter", "Flügel schlagen")),
            Map.entry("shadow_creak", List.of("Shadow wood groans", "Schattenholz ächzt")),
            Map.entry("star_song", List.of("Distant star ray song", "Sternenrochen singt in der Ferne")),
            Map.entry("rock_grind", List.of("Floating rocks grind", "Treibende Felsen schaben")),
            Map.entry("glitch_far", List.of("Something glitches", "Etwas flackert")),
            Map.entry("void_choir", List.of("A distant choir", "Ein ferner Chor")),
            Map.entry("dragon_far", List.of("Distant dragon roar", "Drache brüllt in der Ferne")),
            Map.entry("dragon_wings", List.of("Great wings beat", "Schwingen rauschen")),
            Map.entry("fireflies", List.of("Fireflies buzz", "Glühwürmchen summen")),
            Map.entry("amethyst", List.of("Crystal resonates", "Kristall schwingt")),
            Map.entry("soul_voices", List.of("Voices", "Stimmen")),
            Map.entry("basalt", List.of("Rock crumbles", "Fels bröckelt")),
            Map.entry("thunder", List.of("Thunder rolls", "Donnergrollen")),
            Map.entry("dolphin", List.of("Dolphin whistles", "Delfin pfeift")),
            Map.entry("frog", List.of("Frog croaks", "Frosch quakt")),
            Map.entry("whale", List.of("Whale song", "Walgesang")),
            Map.entry("pale_moss", List.of("Something rustles", "Etwas raschelt")),
            Map.entry("warped_creak", List.of("Shadow wood creaks", "Schattenholz knarrt"))
    );
    private static final Map<String, DeferredHolder<SoundEvent, SoundEvent>> EVENTS = new LinkedHashMap<>();

    static {
        for (String bed : BEDS) {
            register("ambient.bed." + bed);
        }
        for (String spot : SPOTS) {
            register("ambient.spot." + spot);
        }
    }

    private AmbientSounds() {}

    private static void register(String id) {
        EVENTS.put(id, SOUNDS.register(id, () -> SoundEvent.createVariableRangeEvent(KingdomsAtWar.id(id))));
    }

    /** Ein Teppich (Schleife). */
    public static SoundEvent bed(String name) {
        return EVENTS.get("ambient.bed." + name).get();
    }

    /** Ein Satz Einzelklänge. */
    public static SoundEvent spot(String name) {
        return EVENTS.get("ambient.spot." + name).get();
    }

    /** Gibt es dieses Ereignis? (für Tests) */
    public static boolean has(String id) {
        return EVENTS.containsKey(id);
    }
}
