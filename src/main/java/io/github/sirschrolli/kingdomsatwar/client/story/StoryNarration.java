package io.github.sirschrolli.kingdomsatwar.client.story;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.config.ModClientConfig;
import java.io.Reader;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import org.jspecify.annotations.Nullable;

/**
 * Der Erzähler zu den Kapiteln der Geschichte: die Stimme (dieselbe wie im Intro) und der Text jeder Zeile. Die Zeilen
 * heißen wie in {@code tools/audio/lines.py} (z. B. „kingdom“, „fallen_fire“, „remaining_3“); ihre Sprechdauer steht in
 * {@code assets/kingdomsatwar/story/narration.json}, die Töne heißen {@code kingdomsatwar:story.<zeile>.<de|en>}.
 */
public final class StoryNarration {
    private static final Identifier LOCATION = KingdomsAtWar.id("story/narration.json");

    private StoryNarration() {}

    /** "de" für deutsche Spiele (auch de_at, de_ch …), sonst "en". */
    public static String language() {
        String selected = Minecraft.getInstance().getLanguageManager().getSelected().toLowerCase(Locale.ROOT);
        return selected.startsWith("de") ? "de" : "en";
    }

    /** Der Text einer Zeile in der Sprache des Spiels. */
    public static Component text(String line) {
        return Component.translatable("story.kingdomsatwar.narration." + line);
    }

    /** Sprechdauer einer Zeile in Sekunden – ohne Aufnahme geschätzt aus der Länge des Textes. */
    public static double duration(String line) {
        Map<String, JsonElement> durations = durations();
        if (durations != null && durations.get(line) instanceof JsonObject perLanguage && perLanguage.has(language())) {
            return perLanguage.get(language()).getAsDouble();
        }
        return 1.5 + text(line).getString().length() / 15.0;
    }

    /** Spielt die Stimme einer Zeile (falls eingeschaltet und vorhanden); gibt den Ton zurück, um ihn anhalten zu können. */
    public static @Nullable SoundInstance play(String line) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!ModClientConfig.NARRATOR_VOICE.get()) {
            return null;
        }
        Identifier sound = KingdomsAtWar.id("story." + line + "." + language());
        if (minecraft.getSoundManager().getSoundEvent(sound) == null) {
            return null;
        }
        SoundInstance instance = new SimpleSoundInstance(sound, SoundSource.VOICE, 1.0f, 1.0f, SoundInstance.createUnseededRandom(), false, 0,
                SoundInstance.Attenuation.NONE, 0.0, 0.0, 0.0, true);
        minecraft.getSoundManager().play(instance);
        return instance;
    }

    private static @Nullable Map<String, JsonElement> durations() {
        try (Reader reader = Minecraft.getInstance().getResourceManager().openAsReader(LOCATION)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            return root.getAsJsonObject("lines").asMap();
        } catch (Exception e) {
            return null;
        }
    }
}
