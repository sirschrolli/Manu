package io.github.sirschrolli.kingdomsatwar.client.intro;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jspecify.annotations.Nullable;

/**
 * Ablauf des Erzähl-Intros aus {@code assets/kingdomsatwar/intro/intro.json}: Musik, Gesamtlänge,
 * Beginn der Titelkarte, Abschnitte und je Sprache die Erzählzeilen mit Startzeit und Dauer.
 * Die Datei erzeugt {@code tools/audio/build_audio.py} passend zu den Tonaufnahmen.
 */
public record IntroScript(Identifier music, double length, double titleCardStart, Map<String, Double> sections,
        Map<String, List<Line>> languages, String fallbackLanguage) {

    public static final Identifier LOCATION = KingdomsAtWar.id("intro/intro.json");

    /** Eine Erzählzeile: Übersetzungsschlüssel des Textes, Ton-Ereignis, Beginn und Dauer in Sekunden. */
    public record Line(String key, Identifier sound, double start, double duration) {
        public double end() {
            return this.start + this.duration;
        }
    }

    /** Zeilen in der Sprache des Spielers (z. B. "de_de"), sonst in der Ersatzsprache. */
    public List<Line> linesFor(String language) {
        String lang = language.toLowerCase(Locale.ROOT);
        if (this.languages.containsKey(lang)) {
            return this.languages.get(lang);
        }
        // "de_at", "de_ch" ... bekommen die deutsche Stimme
        for (Map.Entry<String, List<Line>> entry : this.languages.entrySet()) {
            if (entry.getKey().regionMatches(0, lang, 0, 2)) {
                return entry.getValue();
            }
        }
        return this.languages.getOrDefault(this.fallbackLanguage, List.of());
    }

    public double section(String name, double fallback) {
        return this.sections.getOrDefault(name, fallback);
    }

    public static @Nullable IntroScript load(ResourceManager resources) {
        try (Reader reader = resources.openAsReader(LOCATION)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            Map<String, Double> sections = new HashMap<>();
            for (JsonElement element : root.getAsJsonArray("sections")) {
                JsonObject section = element.getAsJsonObject();
                sections.put(section.get("name").getAsString(), section.get("start").getAsDouble());
            }
            Map<String, List<Line>> languages = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : root.getAsJsonObject("languages").entrySet()) {
                List<Line> lines = new ArrayList<>();
                for (JsonElement element : entry.getValue().getAsJsonObject().getAsJsonArray("lines")) {
                    JsonObject line = element.getAsJsonObject();
                    lines.add(new Line(line.get("key").getAsString(), Identifier.parse(line.get("sound").getAsString()),
                            line.get("start").getAsDouble(), line.get("duration").getAsDouble()));
                }
                languages.put(entry.getKey(), List.copyOf(lines));
            }
            return new IntroScript(Identifier.parse(root.get("music").getAsString()), root.get("length").getAsDouble(),
                    root.get("title_card_start").getAsDouble(), Map.copyOf(sections), Map.copyOf(languages),
                    root.has("fallback_language") ? root.get("fallback_language").getAsString() : "en_us");
        } catch (Exception e) {
            KingdomsAtWar.LOGGER.error("Intro konnte nicht geladen werden ({})", LOCATION, e);
            return null;
        }
    }
}
