package io.github.sirschrolli.kingdomsatwar.gametest;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.AmbientSounds;
import io.github.sirschrolli.kingdomsatwar.shadow.ShadowBiomes;
import io.github.sirschrolli.kingdomsatwar.world.ambience.AmbienceProfile;
import io.github.sirschrolli.kingdomsatwar.world.ambience.AmbienceProfiles;
import io.github.sirschrolli.kingdomsatwar.worldgen.ModBiomes;
import io.github.sirschrolli.kingdomsatwar.worldgen.overworld.OverworldBiomes;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/** Prüft die Klangwelten der Biome: jedes eigene Biom hat eine, und jeder Klang darin gibt es wirklich. */
final class AmbienceGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> BIOME_AMBIENCE =
            ModGameTests.TEST_FUNCTIONS.register("biome_ambience", () -> AmbienceGameTests::biomeAmbience);

    private AmbienceGameTests() {}

    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        event.registerTest(BIOME_AMBIENCE.getId(), new FunctionGameTestInstance(BIOME_AMBIENCE.key(),
                new TestData<>(environment, Level.OVERWORLD, ARENA, 100, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
    }

    private static void biomeAmbience(GameTestHelper helper) {
        List<ResourceKey<Biome>> biomes = new ArrayList<>(OverworldBiomes.ALL);
        biomes.addAll(ModBiomes.NESTS);
        biomes.addAll(ShadowBiomes.ALL);
        Set<String> usedBeds = new HashSet<>();
        for (ResourceKey<Biome> biome : biomes) {
            AmbienceProfile profile = AmbienceProfiles.of(biome);
            helper.assertTrue(profile != null, "Keine Klangwelt für " + biome.identifier());
            helper.assertTrue(!profile.beds().isEmpty() && !profile.spots().isEmpty(), "Klangwelt ohne Teppich oder Einzelklänge: " + biome.identifier());
            for (AmbienceProfile.Bed bed : profile.beds()) {
                helper.assertTrue(AmbientSounds.BEDS.contains(bed.sound()), "Unbekannter Teppich " + bed.sound() + " in " + biome.identifier());
                helper.assertTrue(bed.volume() > 0.0f && bed.volume() <= 1.0f, "Lautstärke außerhalb 0–1: " + bed.sound());
                usedBeds.add(bed.sound());
            }
            for (AmbienceProfile.Spot spot : profile.spots()) {
                helper.assertTrue(AmbientSounds.SPOTS.contains(spot.sound()), "Unbekannter Einzelklang " + spot.sound() + " in " + biome.identifier());
                helper.assertTrue(spot.near() < spot.far() && spot.perMinute() > 0.0f, "Einzelklang falsch eingestellt: " + spot.sound());
            }
        }
        for (String bed : AmbientSounds.BEDS) {
            helper.assertTrue(usedBeds.contains(bed), "Teppich wird nirgends gespielt: " + bed);
        }
        // jedes Klangereignis ist angemeldet und steht mit seinen Dateien in sounds.json
        JsonObject sounds = readSoundsJson(helper);
        List<String> events = new ArrayList<>();
        AmbientSounds.BEDS.forEach(b -> events.add("ambient.bed." + b));
        AmbientSounds.SPOTS.forEach(s -> events.add("ambient.spot." + s));
        for (String event : events) {
            helper.assertTrue(BuiltInRegistries.SOUND_EVENT.containsKey(KingdomsAtWar.id(event)), "Klangereignis nicht angemeldet: " + event);
            helper.assertTrue(sounds.has(event), "Klangereignis fehlt in sounds.json: " + event);
            for (JsonElement entry : sounds.getAsJsonObject(event).getAsJsonArray("sounds")) {
                String name = entry.isJsonObject() ? entry.getAsJsonObject().get("name").getAsString() : entry.getAsString();
                if (name.startsWith(KingdomsAtWar.MODID + ":")) {
                    String file = "/assets/" + KingdomsAtWar.MODID + "/sounds/" + name.substring(KingdomsAtWar.MODID.length() + 1) + ".ogg";
                    helper.assertTrue(AmbienceGameTests.class.getResource(file) != null, "Klangdatei fehlt: " + file);
                }
            }
            if (event.startsWith("ambient.spot.")) {
                helper.assertTrue(AmbientSounds.SUBTITLES.containsKey(event.substring("ambient.spot.".length())), "Untertitel fehlt: " + event);
            }
        }
        helper.succeed();
    }

    private static JsonObject readSoundsJson(GameTestHelper helper) {
        try (InputStream in = AmbienceGameTests.class.getResourceAsStream("/assets/" + KingdomsAtWar.MODID + "/sounds.json")) {
            helper.assertTrue(in != null, "sounds.json fehlt");
            return JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (java.io.IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
