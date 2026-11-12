package io.github.sirschrolli.kingdomsatwar.gametest;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.fishing.Bait;
import io.github.sirschrolli.kingdomsatwar.fishing.FishBag;
import io.github.sirschrolli.kingdomsatwar.fishing.FishFight;
import io.github.sirschrolli.kingdomsatwar.fishing.FishItem;
import io.github.sirschrolli.kingdomsatwar.fishing.FishLog;
import io.github.sirschrolli.kingdomsatwar.fishing.FishSpecies;
import io.github.sirschrolli.kingdomsatwar.fishing.Fishing;
import io.github.sirschrolli.kingdomsatwar.fishing.Habitat;
import io.github.sirschrolli.kingdomsatwar.fishing.HookAccess;
import io.github.sirschrolli.kingdomsatwar.fishing.Medium;
import io.github.sirschrolli.kingdomsatwar.registry.FishingItems;
import io.github.sirschrolli.kingdomsatwar.registry.ModTags;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Prüft das Angeln (seit 0.79): die Arten (jede hat Lebensraum, Bild, Namen, Notiz und eine Wirkung, die keine andere hat),
 * den Drill (wer nichts tut, verliert; wer mithält, fängt die leichten sicher und die Legenden manchmal), das Fischbuch –
 * und den Ablauf in der Welt: Biss, Anziehen, Drill, Fang im Wasser und in der Lava.
 */
final class FishingGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SPECIES =
            ModGameTests.TEST_FUNCTIONS.register("fishing_species", () -> FishingGameTests::species);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FIGHT =
            ModGameTests.TEST_FUNCTIONS.register("fishing_fight", () -> FishingGameTests::fight);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FLOW =
            ModGameTests.TEST_FUNCTIONS.register("fishing_flow", () -> FishingGameTests::flow);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> BAG =
            ModGameTests.TEST_FUNCTIONS.register("fishing_bag", () -> FishingGameTests::bag);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> RODS =
            ModGameTests.TEST_FUNCTIONS.register("fishing_rods", () -> FishingGameTests::rods);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> UNDERWATER =
            ModGameTests.TEST_FUNCTIONS.register("fishing_underwater", () -> FishingGameTests::underwater);

    private FishingGameTests() {}

    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        event.registerTest(SPECIES.getId(), new FunctionGameTestInstance(SPECIES.key(),
                new TestData<>(environment, Level.OVERWORLD, ARENA, 100, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        event.registerTest(FIGHT.getId(), new FunctionGameTestInstance(FIGHT.key(),
                new TestData<>(environment, Level.OVERWORLD, ARENA, 100, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        event.registerTest(FLOW.getId(), new FunctionGameTestInstance(FLOW.key(),
                new TestData<>(environment, Level.OVERWORLD, ARENA, 200, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        event.registerTest(BAG.getId(), new FunctionGameTestInstance(BAG.key(),
                new TestData<>(environment, Level.OVERWORLD, ARENA, 100, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        event.registerTest(RODS.getId(), new FunctionGameTestInstance(RODS.key(),
                new TestData<>(environment, Level.OVERWORLD, ARENA, 120, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        event.registerTest(UNDERWATER.getId(), new FunctionGameTestInstance(UNDERWATER.key(),
                new TestData<>(environment, Level.OVERWORLD, ARENA, 100, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
    }

    // =====================================================================================
    // Die Arten
    // =====================================================================================

    private static void species(GameTestHelper helper) {
        JsonObject german = lang(helper, "de_de");
        JsonObject english = lang(helper, "en_us");
        RandomSource random = RandomSource.create(79);
        Map<String, FishSpecies> effects = new HashMap<>();
        Map<String, FishSpecies> names = new HashMap<>();
        Map<String, FishSpecies> images = new HashMap<>();
        int custom = 0;
        for (FishSpecies species : FishSpecies.values()) {
            String id = species.id();
            boolean local = species.biome() != null;
            // entweder nach Lebensräumen verteilt (die von Hand benannten) oder an ein Biom und ein Medium gebunden
            helper.assertTrue(local ? species.habitats().isEmpty() && !species.media().isEmpty() : !species.habitats().isEmpty() && species.media().isEmpty(),
                    "Art ohne Lebensraum: " + id);
            helper.assertTrue(local == species.ordinal() >= FishSpecies.NAMED && FishSpecies.byIndex(species.ordinal()) == species
                    && FishSpecies.byId(id) == species, "Reihenfolge der Arten: " + id);
            helper.assertTrue(species.minCm() > 0 && species.maxCm() > species.minCm(), "Größe falsch: " + id);
            helper.assertTrue(species.difficulty() > 0.1f && species.difficulty() < 1.0f, "Schwierigkeit außerhalb: " + id);
            // irgendwann, irgendwo beißt jede
            FishSpecies.Spot home = local ? new FishSpecies.Spot(EnumSet.noneOf(Habitat.class), species.biome(), species.media().iterator().next())
                    : FishSpecies.Spot.of(EnumSet.copyOf(species.habitats()));
            float weight = species.weight(home, species.time() != FishSpecies.Time.NIGHT, false,
                    species.weather() != FishSpecies.Weather.ANY, species.weather() == FishSpecies.Weather.THUNDER_ONLY, 0, Bait.NONE);
            helper.assertTrue(weight > 0.0f, "Art beißt nie: " + id);
            if (local) {
                // … und zwar nur in ihrem Biom und ihrem Medium
                helper.assertTrue(species.weight(new FishSpecies.Spot(EnumSet.allOf(Habitat.class), "minecraft:the_void", Medium.WATER), true, true, true, true,
                        0, Bait.NONE) == 0.0f, "Art beißt außerhalb ihres Bioms: " + id);
                for (Medium medium : Medium.values()) {
                    float there = species.weight(new FishSpecies.Spot(EnumSet.noneOf(Habitat.class), species.biome(), medium), true, true, true, true, 0, Bait.NONE);
                    helper.assertTrue(there > 0.0f == species.media().contains(medium), "Medium passt nicht: " + id + " in " + medium);
                }
                // die Legende eines Bioms steht bei ihrem Biom, nicht bei den fünfzehn Legenden aller Welten
                helper.assertTrue(species.chapter() != FishSpecies.Chapter.LEGENDS, "Biomart im Kapitel der Legenden: " + id);
            } else {
                helper.assertTrue(species.rarity() == FishSpecies.Rarity.LEGENDARY == (species.chapter() == FishSpecies.Chapter.LEGENDS),
                        "Kapitel der Legenden: " + id);
            }
            for (int i = 0; i < 20; i++) {
                int cm = species.rollSize(random, i % 2 == 0);
                helper.assertTrue(cm >= species.minCm() && cm <= species.maxCm(), "Größe außerhalb der Spanne: " + id + " " + cm);
            }
            for (JsonObject lang : List.of(german, english)) {
                helper.assertTrue(lang.has(species.noteKey()) && lang.get(species.noteKey()).getAsString().length() > 20, "Notiz fehlt: " + id);
            }
            ItemStack stack = new ItemStack(species.item());
            helper.assertTrue(stack.is(ItemTags.FISHES), "Nicht als Fisch gemerkt: " + id);
            if (species.isVanilla()) {
                continue;
            }
            custom++;
            helper.assertTrue(species.item() instanceof FishItem item && item.species() == species, "Gegenstand passt nicht: " + id);
            helper.assertTrue(stack.has(DataComponents.FOOD) && stack.has(DataComponents.CONSUMABLE) && stack.is(ModTags.Items.FISH),
                    "Kein essbarer Fisch: " + id);
            helper.assertTrue(species.ofLava() == stack.has(DataComponents.DAMAGE_RESISTANT), "Feuerfestigkeit passt nicht zur Lava: " + id);
            helper.assertTrue(FishingGameTests.class.getResource("/assets/kingdomsatwar/textures/item/fish/" + id + ".png") != null, "Bild fehlt: " + id);
            helper.assertTrue(FishingGameTests.class.getResource("/assets/kingdomsatwar/models/item/fish_" + id + ".json") != null, "Modell fehlt: " + id);
            for (JsonObject lang : List.of(german, english)) {
                helper.assertTrue(lang.has("item.kingdomsatwar.fish_" + id), "Name fehlt: " + id);
                // keine zwei heißen gleich (in keiner der beiden Sprachen)
                String name = (lang == german ? "de:" : "en:") + lang.get("item.kingdomsatwar.fish_" + id).getAsString().toLowerCase(java.util.Locale.ROOT);
                FishSpecies namesake = names.put(name, species);
                helper.assertTrue(namesake == null, "Zwei Fische heißen gleich: " + name + " (" + id + " und " + (namesake == null ? "" : namesake.id()) + ")");
            }
            // keine zwei sehen gleich aus
            try (InputStream in = FishingGameTests.class.getResourceAsStream("/assets/kingdomsatwar/textures/item/fish/" + id + ".png")) {
                String digest = java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("MD5").digest(in.readAllBytes()));
                FishSpecies lookalike = images.put(digest, species);
                helper.assertTrue(lookalike == null, "Zwei Fische sehen gleich aus: " + id + " und " + (lookalike == null ? "" : lookalike.id()));
            } catch (java.io.IOException | java.security.NoSuchAlgorithmException e) {
                throw new IllegalStateException(e);
            }
            // jeder wirkt anders
            List<FishItem.Effect> lines = FishItem.effects(species);
            helper.assertTrue(!lines.isEmpty(), "Fisch ohne Wirkung: " + id);
            StringBuilder signature = new StringBuilder(species.special().name());
            species.bites().stream().map(b -> b.effect().getRegisteredName() + ":" + b.seconds() + ":" + b.amplifier()).sorted()
                    .forEach(s -> signature.append('|').append(s));
            FishSpecies twin = effects.put(signature.toString(), species);
            helper.assertTrue(twin == null, "Zwei Fische wirken gleich: " + id + " und " + (twin == null ? "" : twin.id()));
        }
        helper.assertTrue(FishSpecies.count() == 8369 && FishSpecies.NAMED == 84 && custom == 8365 && FishSpecies.legends().size() == 15,
                "Zahl der Arten: " + FishSpecies.count() + " / " + FishSpecies.NAMED + " / " + custom + " / " + FishSpecies.legends().size());

        // jedes Biom hat eigene Arten: die Biome der Mod je einundfünfzig, die von Minecraft einunddreißig oder dreiunddreißig (seit
        // 0.83 zwei Wellen, seit 0.90 dazu die Legende des Bioms, seit 0.99 dreißig oder zwanzig mehr), die des Urmeers die meisten
        // (518 oder 519) – nur die Kampfarenen und das leere Nichts nicht (dort angelt niemand)
        java.util.Set<String> arenas = java.util.Set.of("arcane_sphere", "creation", "ember_forge", "error_space", "moon_stage", "star_canopy",
                "throne_realm", "world_maw");
        java.util.Set<String> known = new java.util.HashSet<>();
        helper.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.BIOME).listElementIds().forEach(key -> {
            Identifier biome = key.identifier();
            known.add(biome.toString());
            List<FishSpecies> local = FishSpecies.ofBiome(biome.toString());
            if (biome.getNamespace().equals(KingdomsAtWar.MODID)) {
                int expected = arenas.contains(biome.getPath()) ? 0 : biome.getPath().startsWith("urmeer_") ? 518 : 51;
                helper.assertTrue(local.size() == expected || biome.getPath().startsWith("urmeer_") && local.size() == expected + 1,
                        "Arten im Biom " + biome + ": " + local.size());
            } else if (biome.getNamespace().equals("minecraft")) {
                helper.assertTrue(biome.getPath().equals("the_void") ? local.isEmpty() : local.size() == 31 || local.size() == 33,
                        "Arten im Biom " + biome + ": " + local.size());
            }
            // in jedem Biom gibt es jede Seltenheit: Gewöhnliches, mindestens zwei mystische Fänge (einen aus jeder Welle) und die
            // Legende des Bioms (im Urmeer zwei)
            if (!local.isEmpty()) {
                for (FishSpecies.Rarity rarity : FishSpecies.Rarity.values()) {
                    helper.assertTrue(local.stream().anyMatch(s -> s.rarity() == rarity), "Keine Art der Seltenheit " + rarity + " im Biom " + biome);
                }
                helper.assertTrue(local.stream().filter(s -> s.rarity() == FishSpecies.Rarity.COMMON).count() >= 3
                        && local.stream().filter(s -> s.rarity() == FishSpecies.Rarity.EPIC).count() >= 2
                        && local.stream().filter(s -> s.rarity() == FishSpecies.Rarity.LEGENDARY).count() == (biome.getPath().startsWith("urmeer_") ? 2 : 1),
                        "Seltenheiten im Biom " + biome);
                helper.assertTrue(german.has("biome." + biome.getNamespace() + "." + biome.getPath()) || biome.getNamespace().equals("minecraft"),
                        "Name des Bioms fehlt: " + biome);
            }
        });
        for (String biome : FishSpecies.biomes()) {
            helper.assertTrue(known.contains(biome), "Arten für ein Biom, das es nicht gibt: " + biome);
        }
        // in der Ebene: die Allerweltsfische der Flüsse und die dreizehn der Ebene (mit ihrer Legende) – die eigenen stellen gut die
        // Hälfte des Fangs
        FishSpecies.Spot plains = new FishSpecies.Spot(EnumSet.of(Habitat.FRESH), "minecraft:plains", Medium.WATER);
        int localCatch = 0;
        java.util.Set<FishSpecies> seen = new java.util.HashSet<>();
        for (int i = 0; i < 4000; i++) {
            FishSpecies bite = FishSpecies.pick(plains, i % 2 == 0, false, i % 5 == 0, false, 0, Bait.NONE, random);
            helper.assertTrue(bite != null && (bite.biome() == null || bite.biome().equals("minecraft:plains")), "Falscher Fang in der Ebene");
            if (bite.biome() != null) {
                localCatch++;
                seen.add(bite);
            }
        }
        KingdomsAtWar.LOGGER.info("Fang in der Ebene: {} von 4000 aus den {} eigenen Arten", localCatch, seen.size());
        // seit 0.99 leben in der Ebene dreiunddreißig eigene Arten: Sie stellen den größten Teil des Fangs, fast alle zeigen sich
        helper.assertTrue(localCatch > 2400 && localCatch < 3900 && seen.size() >= 26, "Fang in der Ebene: " + localCatch + " eigene, " + seen.size() + " Arten");
        // in der Lava der Aschefelder nur Lavafische, im Wasser dort keine
        for (Medium medium : new Medium[] {Medium.WATER, Medium.LAVA}) {
            FishSpecies.Spot ash = new FishSpecies.Spot(EnumSet.noneOf(Habitat.class), "kingdomsatwar:ash_fields", medium);
            for (int i = 0; i < 300; i++) {
                FishSpecies bite = FishSpecies.pick(ash, true, false, false, false, 0, Bait.NONE, random);
                helper.assertTrue(bite != null && bite.media().contains(medium) && bite.ofLava() == (medium == Medium.LAVA), "Fang in den Aschefeldern: " + medium);
            }
        }

        // überall beißt etwas: in jedem allgemeinen Lebensraum bei Tag und Nacht, und jeder besondere hat eigene Arten
        for (Habitat habitat : Habitat.values()) {
            boolean own = false;
            for (FishSpecies species : FishSpecies.values()) {
                own |= species.habitats().contains(habitat);
            }
            helper.assertTrue(own, "Lebensraum ohne Art: " + habitat);
            if (!habitat.special()) {
                for (boolean day : new boolean[] {true, false}) {
                    helper.assertTrue(FishSpecies.pick(FishSpecies.Spot.of(EnumSet.of(habitat)), day, false, false, false, 0, Bait.NONE, random) != null,
                            "Hier beißt nichts: " + habitat + (day ? " am Tag" : " in der Nacht"));
                }
            }
        }
        // Legenden sind selten – etwa eine je hundert Bisse an ihrem Ort, mit Glück des Meeres III doppelt so oft –, und zwar
        // überall gleich selten: im Gewimmel der Tiefsee nicht seltener als im See
        FishSpecies.Spot lake = FishSpecies.Spot.of(EnumSet.of(Habitat.FRESH));
        FishSpecies.Spot deepSea = FishSpecies.Spot.of(EnumSet.of(Habitat.SALT, Habitat.DEEP));
        int legends = 0;
        int lucky = 0;
        int byDay = 0;
        int kings = 0;
        for (int i = 0; i < 20000; i++) {
            if (FishSpecies.pick(lake, false, false, false, false, 0, Bait.NONE, random) == FishSpecies.OLD_ONE) {
                legends++;
            }
            if (FishSpecies.pick(lake, false, false, false, false, 3, Bait.NONE, random) == FishSpecies.OLD_ONE) {
                lucky++;
            }
            if (FishSpecies.pick(lake, true, false, false, false, 0, Bait.NONE, random) == FishSpecies.OLD_ONE) {
                byDay++;
            }
            if (FishSpecies.pick(deepSea, false, false, false, false, 0, Bait.NONE, random) == FishSpecies.DEEP_KING) {
                kings++;
            }
        }
        helper.assertTrue(legends > 130 && legends < 280 && lucky > 320 && lucky < 520 && byDay < 60 && kings > 130 && kings < 280,
                "Legenden je 20000 Bisse: der Alte vom See " + legends + ", mit Glück " + lucky + ", am Tag " + byDay + ", der König der Tiefe " + kings);
        helper.assertTrue(FishSpecies.PIKE.legendChance(lake, true, false, false, false, 3, Bait.NONE) == 0.0f
                && FishSpecies.OLD_ONE.legendChance(deepSea, false, false, false, false, 3, Bait.NONE) == 0.0f
                && FishSpecies.GOLDSCALE.legendChance(lake, true, false, false, false, 0, Bait.NONE) == 0.0f
                && FishSpecies.GOLDSCALE.legendChance(lake, true, false, true, false, 0, Bait.NONE) > FishSpecies.LEGEND_CHANCE
                && FishSpecies.STORMSAIL.legendChance(FishSpecies.Spot.of(EnumSet.of(Habitat.SALT)), true, false, true, false, 0, Bait.NONE) == 0.0f
                && FishSpecies.STORMSAIL.legendChance(FishSpecies.Spot.of(EnumSet.of(Habitat.SALT)), true, false, true, true, 0, Bait.NONE)
                        > FishSpecies.LEGEND_CHANCE,
                "Die Chancen der Legenden");
        // Goldschuppe nur bei Regen, Sturmsegler nur im Gewitter
        helper.assertTrue(FishSpecies.GOLDSCALE.weight(lake, true, false, false, false, 0, Bait.NONE) == 0.0f
                && FishSpecies.GOLDSCALE.weight(lake, true, false, true, false, 0, Bait.NONE) > 0.0f, "Goldschuppe und der Regen");
        FishSpecies.Spot sea = FishSpecies.Spot.of(EnumSet.of(Habitat.SALT));
        helper.assertTrue(FishSpecies.STORMSAIL.weight(sea, true, false, true, false, 0, Bait.NONE) == 0.0f
                && FishSpecies.STORMSAIL.weight(sea, true, false, true, true, 0, Bait.NONE) > 0.0f, "Sturmsegler und das Gewitter");
        // Köder
        helper.assertTrue(Bait.WORM.factor(FishSpecies.BROOK_TROUT) > 1.0f && Bait.WORM.factor(FishSpecies.STURGEON) == 1.0f
                && Bait.GLOW.factor(FishSpecies.ANGLERFISH) > 1.0f && Bait.GLOW.factor(FishSpecies.MAGMA_CARP) > 1.0f
                && Bait.of(new ItemStack(FishingItems.BAIT_GLOW.get())) == Bait.GLOW && Bait.of(new ItemStack(Items.STICK)) == Bait.NONE, "Köder");

        // Kapitel des Fischbuchs
        int total = 0;
        for (FishSpecies.Chapter chapter : FishSpecies.Chapter.values()) {
            int size = FishSpecies.of(chapter).size();
            helper.assertTrue(size >= 15, "Kapitel " + chapter + " hat " + size + " Arten");
            helper.assertTrue(german.has(chapter.key() + ".tip") && english.has(chapter.key() + ".tip"), "Hinweis zum Kapitel fehlt: " + chapter);
            helper.assertTrue(german.has(chapter.key()) && english.has(chapter.key()), "Kapitelname fehlt: " + chapter);
            total += size;
        }
        helper.assertTrue(total == FishSpecies.count(), "Kapitel decken nicht alle Arten ab");
        for (Habitat habitat : Habitat.values()) {
            helper.assertTrue(german.has(habitat.key()) && english.has(habitat.key()), "Name des Lebensraums fehlt: " + habitat);
        }
        for (FishSpecies.Rarity rarity : FishSpecies.Rarity.values()) {
            helper.assertTrue(german.has(rarity.key()), "Name der Seltenheit fehlt: " + rarity);
        }

        // das Fischbuch: eintragen, speichern, laden, verschicken
        FishLog log = new FishLog();
        helper.assertTrue(log.record(FishSpecies.PIKE, 60) == 2 && log.record(FishSpecies.PIKE, 50) == 0 && log.record(FishSpecies.PIKE, 90) == 1,
                "Eintragen ins Fischbuch");
        log.record(FishSpecies.DRAGON_KOI, 120);
        log.addTreasure();
        FishLog loaded = FishLog.CODEC.parse(JsonOps.INSTANCE, FishLog.CODEC.encodeStart(JsonOps.INSTANCE, log).getOrThrow()).getOrThrow();
        ByteBuf buffer = Unpooled.buffer();
        FishLog.STREAM_CODEC.encode(buffer, log);
        FishLog sent = FishLog.STREAM_CODEC.decode(buffer);
        for (FishLog copy : List.of(loaded, sent, log.copy())) {
            helper.assertTrue(copy.count(FishSpecies.PIKE) == 3 && copy.best(FishSpecies.PIKE) == 90 && copy.has(FishSpecies.DRAGON_KOI)
                    && copy.speciesCaught() == 2 && copy.totalCaught() == 4 && copy.treasures() == 1 && !copy.has(FishSpecies.COD), "Fischbuch nach dem Laden");
        }
        helper.succeed();
    }

    private static JsonObject lang(GameTestHelper helper, String name) {
        try (InputStream in = FishingGameTests.class.getResourceAsStream("/assets/" + KingdomsAtWar.MODID + "/lang/" + name + ".json")) {
            helper.assertTrue(in != null, "Sprachdatei fehlt: " + name);
            return JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (java.io.IOException e) {
            throw new IllegalStateException(e);
        }
    }

    // =====================================================================================
    // Der Drill
    // =====================================================================================

    /**
     * Ein geübter Angler als Rechnung: sieht den Fisch mit {@code delay} Sekunden Verzug, rechnet dessen Schwung ein wenig mit
     * und hält oder lässt los, je nachdem, wo die Zone zum Stehen käme.
     */
    private static int[] play(FishSpecies species, long seed, float delay, boolean idle) {
        FishFight fight = new FishFight(species, 0.0f, false, RandomSource.create(seed));
        ArrayDeque<float[]> seen = new ArrayDeque<>();
        int lag = Math.round(delay / FishFight.STEP);
        boolean held = false;
        int steps = 0;
        while (fight.result() == 0 && steps < 120 * 150) {
            seen.addLast(new float[] {fight.fishPos(), fight.fishVel()});
            if (seen.size() > lag + 1) {
                seen.removeFirst();
            }
            if (!idle && steps % 2 == 0) {
                float[] fish = seen.peekFirst();
                float center = fight.zonePos() + fight.zoneSize() / 2.0f;
                float brake = fight.zoneVel() > 0.0f ? 2.1f : 2.5f;
                float stop = center + fight.zoneVel() * Math.abs(fight.zoneVel()) / (2.0f * brake);
                // am Boden liegen lassen hieße: Die Schnur hängt durch – also antippen
                held = stop < fish[0] + fish[1] * delay * 0.7f || (fight.zonePos() <= 0.004f && fight.inZone());
            }
            fight.step(held);
            steps++;
        }
        return new int[] {fight.result(), fight.perfect() ? 1 : 0, steps};
    }

    /**
     * Ein Angler, der nur antippt (seit 0.83 steuert die Maus, jeder Klick ein Schubs): sieht den Fisch mit {@code delay}
     * Sekunden Verzug und klickt höchstens alle {@code gap} Sekunden – immer dann, wenn die Zone sonst unter dem Fisch zum
     * Stehen käme.
     */
    private static int[] tap(FishSpecies species, long seed, float delay, float gap) {
        FishFight fight = new FishFight(species, 0.0f, false, RandomSource.create(seed));
        ArrayDeque<float[]> seen = new ArrayDeque<>();
        int lag = Math.round(delay / FishFight.STEP);
        int wait = 0;
        int steps = 0;
        while (fight.result() == 0 && steps < 120 * 150) {
            seen.addLast(new float[] {fight.fishPos(), fight.fishVel()});
            if (seen.size() > lag + 1) {
                seen.removeFirst();
            }
            if (--wait <= 0) {
                float[] fish = seen.peekFirst();
                float center = fight.zonePos() + fight.zoneSize() / 2.0f;
                float stop = center + Math.max(0.0f, fight.zoneVel()) * fight.zoneVel() / (2.0f * 2.1f) + Math.min(0.0f, fight.zoneVel()) * 0.1f;
                if (stop < fish[0] + fish[1] * delay * 0.7f || (fight.zonePos() <= 0.004f && fight.inZone())) {
                    fight.kick();
                    wait = Math.round(gap / FishFight.STEP);
                }
            }
            fight.step(false);
            steps++;
        }
        return new int[] {fight.result(), fight.perfect() ? 1 : 0, steps};
    }

    private static void fight(GameTestHelper helper) {
        StringBuilder table = new StringBuilder();
        List<FishSpecies> all = FishSpecies.values();
        // alle von Hand benannten Arten und eine Stichprobe der Biome (gut 260; der Drill hängt nur an Verhalten, Schwierigkeit und
        // Seltenheit)
        int every = Math.max(8, (all.size() - FishSpecies.NAMED) / 260);
        List<String> problems = new java.util.ArrayList<>();
        for (int n = 0; n < all.size(); n++) {
            if (n >= FishSpecies.NAMED && n % every != 0) {
                continue;
            }
            FishSpecies species = all.get(n);
            // wer nichts tut, verliert – immer
            int[] idle = play(species, 7, 0.2f, true);
            helper.assertTrue(idle[0] == -1, "Ohne Zutun gefangen: " + species.id());
            int wins = 0;
            int perfect = 0;
            int fastest = Integer.MAX_VALUE;
            int runs = 24;
            for (int seed = 0; seed < runs; seed++) {
                int[] result = play(species, 1000 + seed, 0.2f, false);
                if (result[0] == 1) {
                    wins++;
                    perfect += result[1];
                    fastest = Math.min(fastest, result[2]);
                }
            }
            table.append(String.format("%n  %-24s %-9s %-9s %.2f  %2d/%d  perfekt %2d", species.id(), species.rarity(), species.behavior(),
                    species.difficulty(), wins, runs, perfect));
            boolean rare = species.rarity().ordinal() >= FishSpecies.Rarity.RARE.ordinal();
            // kein Fang geht schneller, als der Server ihn glaubt
            if (wins > 0 && fastest / 6 < FishFight.minTicks(species.difficulty())) {
                problems.add("Fang schneller als die Mindestzeit: " + species.id());
            }
            // nur mit Klicks (höchstens sechs in der Sekunde) geht es auch
            if (species.difficulty() <= 0.4f) {
                int taps = 0;
                for (int seed = 0; seed < 12; seed++) {
                    taps += tap(species, 2000 + seed, 0.2f, 0.16f)[0] == 1 ? 1 : 0;
                }
                table.append(String.format("  geklickt %2d/12", taps));
                if (taps < (rare ? 8 : 10)) {
                    problems.add("Nur mit Klicks nicht zu fangen: " + species.id() + " " + taps + "/12");
                }
            }
            // seit 0.99 sind seltene Fische unruhiger: Leichte gewöhnliche und ungewöhnliche fängt ein geübter Angler immer, leichte
            // seltene fast immer, mystische mindestens jedes dritte Mal, Legenden jedes vierte
            if (species.difficulty() <= 0.5f) {
                if (rare ? wins < runs * 5 / 6 : wins != runs) {
                    problems.add("Leichter Fisch entkommt einem geübten Angler: " + species.id() + " " + wins + "/" + runs);
                }
            } else if (species.rarity() == FishSpecies.Rarity.LEGENDARY) {
                if (wins < runs / 4) {
                    problems.add("Legende unfangbar: " + species.id() + " " + wins + "/" + runs);
                }
            } else if (species.rarity() == FishSpecies.Rarity.EPIC) {
                if (wins < runs / 3) {
                    problems.add("Mystischer Fisch zu schwer: " + species.id() + " " + wins + "/" + runs);
                }
            } else if (wins < runs / 2) {
                problems.add("Fisch zu schwer: " + species.id() + " " + wins + "/" + runs);
            }
        }
        KingdomsAtWar.LOGGER.info("Drill – geübter Angler (0,2 s Verzug), Siege von 24:{}", table);
        helper.assertTrue(problems.isEmpty(), "Drill: " + problems.size() + " Probleme, etwa " + (problems.isEmpty() ? "" : problems.getFirst()));
        // je seltener, desto unruhiger (seit 0.99): Bei gleicher Art zu schwimmen und gleicher Schwierigkeit legt ein legendärer
        // Fisch in zwanzig Sekunden deutlich mehr Weg zurück als ein gewöhnlicher, und jede Seltenheit mehr als die darunter
        for (FishSpecies.Behavior behavior : FishSpecies.Behavior.values()) {
            double last = -1.0;
            double first = 0.0;
            for (FishSpecies.Rarity rarity : FishSpecies.Rarity.values()) {
                double path = 0.0;
                for (int seed = 0; seed < 6; seed++) {
                    FishFight moving = new FishFight(behavior, 0.5f, rarity, 0.0f, false, RandomSource.create(300 + seed));
                    float before = moving.fishPos();
                    for (int i = 0; i < 120 * 20; i++) {
                        moving.swim();
                        path += Math.abs(moving.fishPos() - before);
                        before = moving.fishPos();
                    }
                }
                if (rarity == FishSpecies.Rarity.COMMON) {
                    first = path;
                }
                helper.assertTrue(path > last * 0.97, "Seltenere Fische bewegen sich nicht mehr: " + behavior + " " + rarity + " " + path + " ≤ " + last);
                last = path;
            }
            // Schießer und Launische sind schon von sich aus unruhig – bei ihnen wirkt die Seltenheit schwächer
            double needed = behavior == FishSpecies.Behavior.DARTER || behavior == FishSpecies.Behavior.MIXED ? 1.1 : 1.3;
            KingdomsAtWar.LOGGER.info("Drill – Weg in 20 s ({}): gewöhnlich {}, legendär {} (×{})", behavior, String.format("%.1f", first),
                    String.format("%.1f", last), String.format("%.2f", last / first));
            helper.assertTrue(last > first * needed, "Legenden sind nicht unruhiger als gewöhnliche Fische: " + behavior + " " + first + " → " + last);
        }
        // die Fangzone schrumpft mit der Schwierigkeit, Glück des Meeres vergrößert sie
        helper.assertTrue(FishFight.zone(0.2f, 0.0f) > FishFight.zone(0.9f, 0.0f) && FishFight.zone(0.9f, 0.15f) > FishFight.zone(0.9f, 0.0f),
                "Größe der Fangzone");
        // die Truhe: Wer die Zone über ihr hält, hebt sie (der Fisch ist derweil woanders – das kostet Fortschritt)
        FishFight chest = new FishFight(FishSpecies.Behavior.CALM, 0.2f, 0.0f, true, RandomSource.create(5));
        chest.pose(0.3f, 0.9f, 0.6f, 0.42f, 0.0f);
        for (int i = 0; i < 120 * 3 && !chest.chestCollected() && chest.result() == 0; i++) {
            chest.step(chest.zonePos() + chest.zoneSize() / 2.0f < 0.42f);
        }
        helper.assertTrue(chest.progress() < 0.6f, "Die Truhe zu heben kostet nichts");
        helper.assertTrue(chest.chestCollected(), "Die Truhe lässt sich nicht heben");
        helper.succeed();
    }

    // =====================================================================================
    // In der Welt
    // =====================================================================================

    private static FishingHook cast(ServerLevel level, ServerPlayer player, Vec3 at) {
        if (player.fishing != null) {
            player.fishing.discard();
        }
        FishingHook hook = new FishingHook(player, level, 0, 0);
        hook.snapTo(at.x, at.y, at.z, 0.0f, 0.0f);
        hook.setDeltaMovement(0.0, -0.05, 0.0);
        level.addFreshEntity(hook);
        return hook;
    }

    /** Wirft aus, lässt beißen und zieht an – bis ein Fisch am Haken hängt (und nicht bloß Kram). */
    private static FishingHook hookFish(GameTestHelper helper, ServerLevel level, ServerPlayer player, Vec3 at) {
        for (int i = 0; i < 12; i++) {
            FishingHook hook = cast(level, player, at);
            ((HookAccess) hook).kingdomsatwar$forceBite();
            int damage = hook.retrieve(player.getMainHandItem());
            if (Fishing.sessionOf(player) != 0) {
                helper.assertTrue(damage == 0 && hook.isAlive() && player.fishing == hook && Fishing.isFighting(hook), "Der Drill beginnt nicht sauber");
                return hook;
            }
            // Kram: gleich herausgehoben, der Haken ist eingeholt
            helper.assertTrue(hook.isRemoved(), "Weder Drill noch Fang");
        }
        throw helper.assertionException("Zwölfmal nur Kram geangelt");
    }

    /**
     * Die Leere: Von einer Plattform im End (weit draußen zwischen den Inseln) über den Rand geworfen, fällt der Schwimmer
     * ein Stück und bleibt dann hängen wie auf einer Wasseroberfläche; über festem Grund fällt er einfach weiter.
     */
    private static void voidFishing(GameTestHelper helper, ServerPlayer player) {
        // in der Oberwelt trägt die Luft unter einer Klippe keinen Schwimmer: Leere ist dort nur, wo bis zum Boden der Welt
        // nichts mehr kommt
        ServerLevel home = helper.getLevel();
        Vec3 high = player.position().add(0.0, 60.0, 0.0);
        player.snapTo(high.x, high.y, high.z, 0.0f, 0.0f);
        FishingHook cliff = cast(home, player, high.add(3.0, 1.0, 0.0));
        for (int i = 0; i < 50 && cliff.isAlive(); i++) {
            cliff.tick();
        }
        helper.assertTrue(cliff.isAlive() && ((HookAccess) cliff).kingdomsatwar$medium() != Medium.VOID
                        && player.getY() - cliff.getY() > Fishing.VOID_DROP + 4.0,
                "Über festem Land treibt der Schwimmer in der Luft: " + (player.getY() - cliff.getY()) + " tiefer, worin: "
                        + ((HookAccess) cliff).kingdomsatwar$medium());
        cliff.discard();
        ServerLevel end = home.getServer().getLevel(Level.END);
        if (end == null) {
            return;
        }
        BlockPos test = helper.absolutePos(BlockPos.ZERO);
        int x0 = 400 + Math.floorMod(test.getX(), 256);
        int z0 = 400 + Math.floorMod(test.getZ(), 256);
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                end.setBlockAndUpdate(new BlockPos(x0 + dx, 90, z0 + dz), Blocks.END_STONE.defaultBlockState());
            }
        }
        player.teleportTo(end, x0 + 0.5, 91.0, z0 + 0.5, java.util.Set.of(), 0.0f, 0.0f, true);
        helper.assertTrue(player.level() == end, "Der Angler ist nicht im End angekommen");
        // über den Rand hinaus
        FishingHook hook = cast(end, player, new Vec3(x0 + 0.5, 92.0, z0 + 6.5));
        for (int i = 0; i < 120 && hook.isAlive(); i++) {
            hook.tick();
        }
        helper.assertTrue(hook.isAlive() && ((HookAccess) hook).kingdomsatwar$medium() == Medium.VOID,
                "Der Schwimmer treibt nicht in der Leere: y=" + hook.getY() + ", worin: " + ((HookAccess) hook).kingdomsatwar$medium());
        double depth = player.getY() - hook.getY();
        helper.assertTrue(depth > Fishing.VOID_DROP - 0.6 && depth < Fishing.VOID_DROP + 1.6 && Math.abs(hook.getDeltaMovement().y) < 0.08,
                "Der Schwimmer hängt nicht ein Stück unter dem Angler: " + depth + " tiefer, Fahrt " + hook.getDeltaMovement().y);
        ((HookAccess) hook).kingdomsatwar$forceBite();
        helper.assertTrue(hook.retrieve(player.getMainHandItem()) == 0 && Fishing.isFighting(hook), "In der Leere beginnt kein Drill");
        FishSpecies species = Fishing.fighting(player);
        // ein Allerweltsfisch des Ends – oder eine Art des Bioms, über dem der Haken hängt (die beißen nur in der Leere)
        helper.assertTrue(species != null && (species.habitats().contains(Habitat.END) || (species.biome() != null && species.media().contains(Medium.VOID)
                && species.chapter() == FishSpecies.Chapter.END)), "In der Leere des Ends hing kein Endfisch am Haken: " + species);
        Fishing.ageSession(player, 600);
        Fishing.onResult(player, Fishing.sessionOf(player), true, false, false);
        helper.assertTrue(hook.isRemoved() && Fishing.log(player).has(species), "Der Fisch aus der Leere wurde nicht gefangen");
        // über festem Grund (der Plattform selbst) gibt es keine Leere
        FishingHook dry = cast(end, player, new Vec3(x0 + 0.5, 96.0, z0 + 1.5));
        for (int i = 0; i < 60 && dry.isAlive(); i++) {
            dry.tick();
        }
        helper.assertTrue(((HookAccess) dry).kingdomsatwar$medium() != Medium.VOID && dry.getY() > 90.5, "Über festem Grund treibt der Schwimmer in der Leere");
        dry.discard();
        end.getEntitiesOfClass(ItemEntity.class, new AABB(player.position(), player.position()).inflate(6.0)).forEach(ItemEntity::discard);
    }

    /**
     * Unter Wasser (seit 0.90): Ein Taucher wirft aus – der Haken gleitet ein paar Blöcke durchs Wasser und bleibt dann in der
     * Tiefe stehen, statt zur Oberfläche zu treiben; dort beißt es, gedrillt wird wie oben, und der Fang landet beim Taucher.
     * Wer mit dem Kopf über Wasser auswirft, angelt wie gewohnt an der Oberfläche.
     */
    private static void underwater(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        // ein Becken, fünf Blöcke tief
        for (int x = 0; x <= 8; x++) {
            for (int z = 0; z <= 8; z++) {
                for (int y = 1; y <= 5; y++) {
                    boolean wall = x == 0 || x == 8 || z == 0 || z == 8;
                    helper.setBlock(new BlockPos(x, y, z), wall ? Blocks.STONE : Blocks.WATER);
                }
            }
        }
        double surface = helper.absoluteVec(new Vec3(0.0, 6.0, 0.0)).y;
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.getAbilities().instabuild = false;
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.FISHING_ROD));
        Vec3 diver = helper.absoluteVec(new Vec3(1.5, 1.2, 4.5));
        try {
            player.snapTo(diver.x, diver.y, diver.z, -90.0f, 0.0f);
            helper.assertTrue(Fishing.submerged(player), "Der Taucher ist nicht unter Wasser");
            FishingHook hook = null;
            for (int attempt = 0; attempt < 12 && Fishing.sessionOf(player) == 0; attempt++) {
                if (player.fishing != null) {
                    player.fishing.discard();
                }
                // ausgeworfen wie mit der Angel: aus den Augen, in Blickrichtung
                hook = new FishingHook(player, level, 0, 0);
                level.addFreshEntity(hook);
                for (int i = 0; i < 40 && hook.isAlive(); i++) {
                    hook.tick();
                }
                helper.assertTrue(hook.isAlive() && Fishing.deep(hook), "Der Haken steht nicht unter Wasser: y=" + hook.getY());
                helper.assertTrue(surface - hook.getY() > 2.0 && Math.abs(hook.getDeltaMovement().y) < 0.08,
                        "Der Haken bleibt nicht in der Tiefe: " + (surface - hook.getY()) + " unter der Oberfläche, Fahrt " + hook.getDeltaMovement().y);
                helper.assertTrue(hook.getX() - player.getX() > 2.0 && hook.getX() - player.getX() < 7.0,
                        "Der Haken gleitet nicht ein Stück durchs Wasser: " + (hook.getX() - player.getX()));
                ((HookAccess) hook).kingdomsatwar$forceBite();
                hook.retrieve(player.getMainHandItem());
            }
            int session = Fishing.sessionOf(player);
            FishSpecies species = Fishing.fighting(player);
            helper.assertTrue(session != 0 && species != null && hook != null && Fishing.isFighting(hook), "Unter Wasser beginnt kein Drill");
            helper.assertTrue(!species.ofLava(), "Unter Wasser hing ein Lavafisch am Haken: " + species.id());
            Fishing.ageSession(player, 600);
            Fishing.onResult(player, session, true, false, false);
            helper.assertTrue(hook.isRemoved() && Fishing.log(player).has(species), "Der Fisch aus der Tiefe wurde nicht gefangen");
            // der Fang landet gleich beim Taucher (er flöge unter Wasser nicht weit)
            List<ItemEntity> drops = level.getEntitiesOfClass(ItemEntity.class, new AABB(player.position(), player.position()).inflate(1.5),
                    e -> e.getItem().is(species.item()));
            helper.assertTrue(!drops.isEmpty(), "Der Fang landet nicht beim Taucher");
            drops.forEach(ItemEntity::discard);

            // mit dem Kopf über Wasser: wie gewohnt an der Oberfläche
            player.snapTo(diver.x, surface - 0.9, diver.z, -90.0f, 35.0f);
            helper.assertTrue(!Fishing.submerged(player), "Mit dem Kopf über Wasser taucht der Angler");
            FishingHook top = new FishingHook(player, level, 0, 0);
            level.addFreshEntity(top);
            for (int i = 0; i < 80 && top.isAlive(); i++) {
                top.tick();
            }
            helper.assertTrue(top.isAlive() && !Fishing.deep(top) && Math.abs(top.getY() - (surface - 0.1)) < 0.4,
                    "An der Oberfläche treibt der Schwimmer nicht oben: " + (top.getY() - surface));
            top.discard();
        } finally {
            if (player.fishing != null) {
                player.fishing.discard();
            }
            level.getEntitiesOfClass(ItemEntity.class, new AABB(diver, diver).inflate(8.0)).forEach(ItemEntity::discard);
            level.getServer().getPlayerList().remove(player);
        }
        helper.succeed();
    }

    // =====================================================================================
    // Der Fischbeutel
    // =====================================================================================

    private static int fishCount(ServerPlayer player, FishSpecies species) {
        int n = player.getInventory().countItem(species.item());
        for (ItemStack bag : FishBag.carried(player)) {
            n += FishBag.of(bag).count(species);
        }
        return n;
    }

    /** Der Fischbeutel: Fassungsvermögen und Preise, einlegen und entnehmen, der Fang wandert hinein, Erweiterung und Verkauf beim Fischer. */
    private static void bag(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        // jede Stufe fasst mehr und kostet mehr – ohne Ende
        helper.assertTrue(FishBag.capacity(0) == 64 && FishBag.capacity(1) == 96 && FishBag.capacity(10) == 384 && FishBag.capacity(1000) == 32064,
                "Fassungsvermögen");
        helper.assertTrue(FishBag.upgradePrice(0) == 12 && FishBag.upgradePrice(10) == 53 && FishBag.upgradePrice(20) == 234, "Preise der Stufen: "
                + FishBag.upgradePrice(0) + ", " + FishBag.upgradePrice(10) + ", " + FishBag.upgradePrice(20));
        for (int l = 0; l < 70; l++) {
            helper.assertTrue(FishBag.upgradePrice(l + 1) > FishBag.upgradePrice(l) && FishBag.capacity(l + 1) == FishBag.capacity(l) + FishBag.STEP,
                    "Stufe " + (l + 1) + " ist nicht teurer oder nicht größer");
        }
        helper.assertTrue(FishBag.upgradePrice(5000) == 1_000_000 && FishBag.capacity(Integer.MAX_VALUE) == Integer.MAX_VALUE, "Sehr hohe Stufen");

        // einlegen, bis er voll ist
        ItemStack bag = new ItemStack(FishingItems.FISH_BAG.get());
        helper.assertTrue(FishBag.of(bag).equals(FishBag.EMPTY) && bag.getMaxStackSize() == 1 && FishBag.isBag(bag) && !bag.isBarVisible(), "Neuer Beutel");
        helper.assertTrue(FishBag.insert(bag, FishSpecies.PIKE, 40) == 40 && FishBag.insert(bag, FishSpecies.COD, 40) == 24
                && FishBag.insert(bag, FishSpecies.CARP, 1) == 0, "Einlegen bis zum Rand");
        FishBag contents = FishBag.of(bag);
        helper.assertTrue(contents.total() == 64 && contents.free() == 0 && contents.count(FishSpecies.PIKE) == 40 && contents.count(FishSpecies.COD) == 24
                && contents.count(FishSpecies.CARP) == 0 && contents.entries().size() == 2 && contents.entries().getFirst().species() == FishSpecies.COD,
                "Inhalt des Beutels");
        helper.assertTrue(bag.isBarVisible() && bag.getBarWidth() == 13, "Leiste am vollen Beutel");
        // speichern, laden, verschicken
        FishBag bigger = contents.upgraded();
        FishBag loaded = FishBag.CODEC.parse(JsonOps.INSTANCE, FishBag.CODEC.encodeStart(JsonOps.INSTANCE, bigger).getOrThrow()).getOrThrow();
        ByteBuf buffer = Unpooled.buffer();
        FishBag.STREAM_CODEC.encode(buffer, bigger);
        FishBag sent = FishBag.STREAM_CODEC.decode(buffer);
        helper.assertTrue(loaded.equals(bigger) && sent.equals(bigger) && loaded.level() == 1 && loaded.capacity() == 96 && loaded.free() == 32
                && !bigger.equals(contents), "Beutel nach dem Laden");
        // was ein Fisch ist
        ItemStack named = new ItemStack(FishSpecies.PIKE.item());
        named.set(DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal("Hugo"));
        FishSpecies local = FishSpecies.ofBiome("kingdomsatwar:coral_coast").getFirst();
        helper.assertTrue(FishBag.speciesOf(new ItemStack(Items.COD)) == FishSpecies.COD && FishBag.speciesOf(new ItemStack(FishSpecies.PIKE.item())) == FishSpecies.PIKE
                && FishBag.speciesOf(new ItemStack(local.item())) == local && FishBag.speciesOf(new ItemStack(Items.STICK)) == null
                && FishBag.speciesOf(new ItemStack(FishingItems.COOKED_FILLET.get())) == null && FishBag.speciesOf(named) == null, "Was in den Beutel gehört");

        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.getAbilities().instabuild = false;
        BlockPos spot = helper.absolutePos(new BlockPos(4, 1, 4));
        player.snapTo(spot.getX() + 0.5, spot.getY(), spot.getZ() + 0.5, 0.0f, 0.0f);
        io.github.sirschrolli.kingdomsatwar.entity.villager.VillagerEntity fisher =
                io.github.sirschrolli.kingdomsatwar.registry.ModEntities.VILLAGER.get().create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
        try {
            player.getInventory().clearContent();
            // zwei Beutel: einer im Inventar, einer in der Hand – der in der Hand füllt sich zuerst
            ItemStack stored = new ItemStack(FishingItems.FISH_BAG.get());
            ItemStack held = new ItemStack(FishingItems.FISH_BAG.get());
            player.getInventory().setItem(9, stored);
            player.setItemInHand(InteractionHand.MAIN_HAND, held);
            helper.assertTrue(FishBag.carried(player).size() == 2 && FishBag.carried(player).getFirst() == held && FishBag.forUpgrade(player) == held,
                    "Beutel am Spieler");
            helper.assertTrue(FishBag.deposit(player, FishSpecies.EEL, 70) == 70 && FishBag.of(held).count(FishSpecies.EEL) == 64
                    && FishBag.of(stored).count(FishSpecies.EEL) == 6, "Verteilen auf zwei Beutel");
            helper.assertTrue(FishBag.deposit(player, local, 100) == 58 && FishBag.of(stored).free() == 0 && FishBag.deposit(player, local, 1) == 0,
                    "Volle Beutel nehmen nichts mehr");
            // entnehmen: einer, ein Stapel, alle – nie mehr, als das Inventar fasst
            helper.assertTrue(FishBag.take(player, InteractionHand.MAIN_HAND, FishSpecies.EEL, FishBag.TAKE_ONE) == 1
                    && player.getInventory().countItem(FishSpecies.EEL.item()) == 1 && FishBag.of(held).count(FishSpecies.EEL) == 63, "Einen Fisch entnehmen");
            held.set(io.github.sirschrolli.kingdomsatwar.registry.ModDataComponents.FISH_BAG.get(), new FishBag(10, Map.of("pike", 200, "cod", 30)));
            helper.assertTrue(FishBag.take(player, InteractionHand.MAIN_HAND, FishSpecies.PIKE, FishBag.TAKE_STACK) == 64
                    && FishBag.take(player, InteractionHand.MAIN_HAND, FishSpecies.PIKE, FishBag.TAKE_ALL) == 136
                    && player.getInventory().countItem(FishSpecies.PIKE.item()) == 200 && FishBag.of(held).count(FishSpecies.PIKE) == 0
                    && FishBag.of(held).count(FishSpecies.COD) == 30, "Stapel und alle einer Art entnehmen");
            helper.assertTrue(FishBag.take(player, InteractionHand.OFF_HAND, FishSpecies.COD, FishBag.TAKE_ALL) == 0, "Entnehmen ohne Beutel in der Hand");
            // das Inventar bis auf einen halben Stapel Kabeljau füllen: Nur was hineinpasst, verlässt den Beutel – nichts geht verloren
            for (int i = 0; i < 36; i++) {
                if (player.getInventory().getItem(i).isEmpty()) {
                    player.getInventory().setItem(i, new ItemStack(i == 30 ? Items.COD : Items.DIRT, i == 30 ? 54 : 64));
                }
            }
            int codBefore = fishCount(player, FishSpecies.COD);
            helper.assertTrue(FishBag.take(player, InteractionHand.MAIN_HAND, null, FishBag.TAKE_ALL) == 10 && FishBag.of(held).count(FishSpecies.COD) == 20
                    && fishCount(player, FishSpecies.COD) == codBefore, "Alles entnehmen bei vollem Inventar");
            helper.assertTrue(FishBag.take(player, InteractionHand.MAIN_HAND, null, FishBag.TAKE_ALL) == 0 && FishBag.of(held).total() == 20, "Volles Inventar");
            for (int i = 0; i < 36; i++) {
                if (player.getInventory().getItem(i).is(Items.DIRT)) {
                    player.getInventory().setItem(i, ItemStack.EMPTY);
                }
            }

            // beim Fischer: Erweiterung (der Preis steigt mit der Stufe) und Verkauf des Fangs
            fisher.setup(io.github.sirschrolli.kingdomsatwar.entity.villager.VillageProfession.FISHER, 0, "Test", spot, "Testdorf", null, null, 81L);
            fisher.snapTo(spot.getX() + 1.5, spot.getY(), spot.getZ() + 0.5, 0.0f, 0.0f);
            fisher.setNoAi(true);
            level.addFreshEntity(fisher);
            List<io.github.sirschrolli.kingdomsatwar.economy.citizen.Trading.Offer> offers =
                    io.github.sirschrolli.kingdomsatwar.economy.citizen.Trading.offers(player, fisher);
            int upgrade = -1;
            int sale = -1;
            int buyBag = -1;
            for (int i = 0; i < offers.size(); i++) {
                if (offers.get(i).special().equals(FishBag.UPGRADE_OFFER)) {
                    upgrade = i;
                } else if (offers.get(i).special().equals(FishBag.SELL_OFFER + "uncommon")) {
                    sale = i;
                } else if (offers.get(i).special().isEmpty() && FishBag.isBag(offers.get(i).get())) {
                    buyBag = i;
                }
            }
            helper.assertTrue(upgrade >= 0 && sale >= 0 && buyBag >= 0, "Der Fischer bietet Beutel, Erweiterung und Aufkauf nicht an");
            int discount = io.github.sirschrolli.kingdomsatwar.economy.citizen.Trading.discount(player, fisher);
            int price = Math.max(1, Math.round(FishBag.upgradePrice(10) * (100 - discount) / 100.0f));
            helper.assertTrue(offers.get(upgrade).giveCoins() == price && FishBag.of(offers.get(upgrade).get()).level() == 11,
                    "Preis der Erweiterung: " + offers.get(upgrade).giveCoins() + " statt " + price);
            helper.assertValueEqual(io.github.sirschrolli.kingdomsatwar.economy.citizen.Trading.trade(player, fisher.getId(), upgrade),
                    io.github.sirschrolli.kingdomsatwar.economy.citizen.Trading.Result.MISSING_COINS, "Erweiterung ohne Geld");
            io.github.sirschrolli.kingdomsatwar.economy.Coins.give(player, price + 3);
            helper.assertValueEqual(io.github.sirschrolli.kingdomsatwar.economy.citizen.Trading.trade(player, fisher.getId(), upgrade),
                    io.github.sirschrolli.kingdomsatwar.economy.citizen.Trading.Result.DONE, "Erweiterung kaufen");
            helper.assertTrue(FishBag.of(held).level() == 11 && FishBag.of(held).count(FishSpecies.COD) == 20 && FishBag.of(stored).level() == 0
                    && io.github.sirschrolli.kingdomsatwar.economy.Coins.total(player) == 3, "Nach der Erweiterung");
            helper.assertTrue(fisher.tradesUsed() == 0, "Die Erweiterung zählt zur Handelsgrenze des Ladens");
            int next = io.github.sirschrolli.kingdomsatwar.economy.citizen.Trading.offers(player, fisher).get(upgrade).giveCoins();
            helper.assertTrue(next > price, "Die nächste Stufe ist nicht teurer: " + next + " nach " + price);
            // Verkauf: fünf ungewöhnliche Fische – erst die losen aus dem Inventar, dann aus den Beuteln
            int uncommon = FishBag.countFish(player, FishSpecies.Rarity.UNCOMMON);
            helper.assertTrue(uncommon == player.getInventory().countItem(FishSpecies.PIKE.item()) + player.getInventory().countItem(FishSpecies.EEL.item())
                    + FishBag.of(stored).entries().stream().filter(e -> e.species().rarity() == FishSpecies.Rarity.UNCOMMON).mapToInt(FishBag.Entry::count).sum(),
                    "Zählen der Fische einer Seltenheit");
            io.github.sirschrolli.kingdomsatwar.economy.citizen.Trading.Offer selling = offers.get(sale);
            helper.assertValueEqual(io.github.sirschrolli.kingdomsatwar.economy.citizen.Trading.trade(player, fisher.getId(), sale),
                    io.github.sirschrolli.kingdomsatwar.economy.citizen.Trading.Result.DONE, "Fang verkaufen");
            helper.assertTrue(FishBag.countFish(player, FishSpecies.Rarity.UNCOMMON) == uncommon - selling.give().getCount()
                    && io.github.sirschrolli.kingdomsatwar.economy.Coins.total(player) == 3 + selling.getCoins() && fisher.tradesUsed() == 1, "Nach dem Verkauf");
            helper.assertTrue(!FishBag.removeFish(player, FishSpecies.Rarity.LEGENDARY, 1) && FishBag.removeFish(player, FishSpecies.Rarity.UNCOMMON, uncommon - 5)
                    && FishBag.countFish(player, FishSpecies.Rarity.UNCOMMON) == 0, "Fische abgeben");
            // ohne Beutel gibt es keine Erweiterung
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            player.getInventory().setItem(9, ItemStack.EMPTY);
            io.github.sirschrolli.kingdomsatwar.economy.Coins.give(player, 500);
            int coins = io.github.sirschrolli.kingdomsatwar.economy.Coins.total(player);
            helper.assertValueEqual(io.github.sirschrolli.kingdomsatwar.economy.citizen.Trading.trade(player, fisher.getId(), upgrade),
                    io.github.sirschrolli.kingdomsatwar.economy.citizen.Trading.Result.NOTHING, "Erweiterung ohne Beutel");
            helper.assertTrue(io.github.sirschrolli.kingdomsatwar.economy.Coins.total(player) == coins, "Ohne Beutel wurde trotzdem bezahlt");
            helper.assertValueEqual(io.github.sirschrolli.kingdomsatwar.economy.citizen.Trading.trade(player, fisher.getId(), buyBag),
                    io.github.sirschrolli.kingdomsatwar.economy.citizen.Trading.Result.DONE, "Beutel kaufen");
            helper.assertTrue(FishBag.carried(player).size() == 1 && FishBag.of(FishBag.carried(player).getFirst()).equals(FishBag.EMPTY), "Gekaufter Beutel");
        } finally {
            fisher.discard();
            level.getServer().getPlayerList().remove(player);
        }
        // Rezept und Texte
        helper.assertTrue(level.getServer().getRecipeManager().recipeMap().byKey(net.minecraft.resources.ResourceKey.create(
                net.minecraft.core.registries.Registries.RECIPE, KingdomsAtWar.id("fish_bag"))) != null, "Rezept des Fischbeutels fehlt");
        JsonObject german = lang(helper, "de_de");
        for (String key : List.of("item.kingdomsatwar.fish_bag", "item.kingdomsatwar.fish_bag.level", "item.kingdomsatwar.fish_bag.fill",
                "item.kingdomsatwar.fish_bag.more", "item.kingdomsatwar.fish_bag.hint.catch", "item.kingdomsatwar.fish_bag.hint.open",
                "item.kingdomsatwar.fish_bag.hint.upgrade", "item.kingdomsatwar.fish_bag.upgrade_hint", "trade.kingdomsatwar.fish_bag_upgrade",
                "trade.kingdomsatwar.fish.common", "trade.kingdomsatwar.fish.uncommon", "trade.kingdomsatwar.fish.rare", "trade.kingdomsatwar.fish.epic",
                "trade.kingdomsatwar.fish.any", "trade.kingdomsatwar.fish.from", "gui.kingdomsatwar.trade.missing_bag",
                "message.kingdomsatwar.fish_bag.inventory_full", "gui.kingdomsatwar.fishing.into_bag", "gui.kingdomsatwar.fish_bag.title",
                "gui.kingdomsatwar.fish_bag.level", "gui.kingdomsatwar.fish_bag.fill", "gui.kingdomsatwar.fish_bag.empty", "gui.kingdomsatwar.fish_bag.take_all",
                "gui.kingdomsatwar.fish_bag.book", "gui.kingdomsatwar.fish_bag.hint", "gui.kingdomsatwar.fish_bag.next", "fishing.kingdomsatwar.book.everywhere",
                "fishing.kingdomsatwar.medium.lava", "fishing.kingdomsatwar.medium.void")) {
            helper.assertTrue(german.has(key), "Text fehlt: " + key);
        }
        for (String file : List.of("textures/item/fish_bag.png", "textures/gui/fishing/bag.png", "models/item/relief/fish_bag.json", "items/fish_bag.json")) {
            helper.assertTrue(FishingGameTests.class.getResource("/assets/kingdomsatwar/" + file) != null, "Datei fehlt: " + file);
        }
        helper.succeed();
    }

    /**
     * Die Angeln der Mod (seit 0.87): jede da, mit Bild, Namen, Rezept, Haltbarkeit und den Verzauberungen der Angel; ihre
     * Vorteile (Biss, Fangzone, Glück) wirken dort, wo sie sollen – und zusammen mit Köder, Drillmeister und „Köder“. Die
     * Glutangel in der Lava: Der Fisch beißt doppelt so schnell.
     */
    private static void rods(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        JsonObject german = lang(helper, "de_de");
        JsonObject english = lang(helper, "en_us");
        for (io.github.sirschrolli.kingdomsatwar.fishing.RodType type : io.github.sirschrolli.kingdomsatwar.fishing.RodType.values()) {
            ItemStack rod = new ItemStack(FishingItems.rod(type));
            helper.assertTrue(rod.getItem() instanceof io.github.sirschrolli.kingdomsatwar.fishing.ModFishingRodItem item && item.type() == type
                    && rod.canPerformAction(net.neoforged.neoforge.common.ItemAbilities.FISHING_ROD_CAST), "Keine Angel: " + type);
            helper.assertTrue(rod.getMaxDamage() == type.durability() && rod.getMaxDamage() > new ItemStack(Items.FISHING_ROD).getMaxDamage() / 2,
                    "Haltbarkeit von " + type + ": " + rod.getMaxDamage());
            helper.assertTrue(rod.has(DataComponents.DAMAGE_RESISTANT) == type.fireproof(), "Feuerfest stimmt nicht: " + type);
            helper.assertTrue(rod.is(ItemTags.FISHING_ENCHANTABLE) && rod.is(ItemTags.DURABILITY_ENCHANTABLE), "Nicht wie eine Angel verzauberbar: " + type);
            helper.assertTrue(rod.isValidRepairItem(new ItemStack(Items.STRING)) == false, "Mit Schnur ausbesserbar: " + type);
            for (JsonObject lang : List.of(german, english)) {
                for (String key : List.of("item.kingdomsatwar." + type.id(), io.github.sirschrolli.kingdomsatwar.registry.ModItems.loreKey(type.id(), 1),
                        io.github.sirschrolli.kingdomsatwar.registry.ModItems.loreKey(type.id(), 2))) {
                    helper.assertTrue(lang.has(key), "Text fehlt: " + key);
                }
            }
            // ein Rezept haben alle außer der Krakenangel (Beute des Kraken, seit 0.91)
            helper.assertTrue((FishingGameTests.class.getResource("/data/kingdomsatwar/recipe/" + type.id() + ".json") != null) == type.craftable(),
                    "Rezept stimmt nicht: " + type);
            for (String file : List.of("assets/kingdomsatwar/textures/item/" + type.id() + ".png", "assets/kingdomsatwar/textures/item/" + type.id() + "_cast.png",
                    "assets/kingdomsatwar/items/" + type.id() + ".json",
                    // seit 0.90 in der Hand in 3D, ruhend und ausgeworfen
                    "assets/kingdomsatwar/models/item/" + type.id() + "_in_hand.json", "assets/kingdomsatwar/models/item/" + type.id() + "_cast_in_hand.json",
                    "assets/kingdomsatwar/textures/item/" + type.id() + "_model.png")) {
                helper.assertTrue(FishingGameTests.class.getResource("/" + file) != null, "Datei fehlt: " + file);
            }
        }
        // auch die Angel von Minecraft ist in der Hand 3D
        for (String file : List.of("assets/minecraft/items/fishing_rod.json", "assets/kingdomsatwar/models/item/fishing_rod_in_hand.json",
                "assets/kingdomsatwar/models/item/fishing_rod_cast_in_hand.json", "assets/kingdomsatwar/textures/item/fishing_rod_model.png")) {
            helper.assertTrue(FishingGameTests.class.getResource("/" + file) != null, "Datei fehlt: " + file);
        }
        helper.assertTrue(new ItemStack(FishingItems.rod(io.github.sirschrolli.kingdomsatwar.fishing.RodType.IRON)).isValidRepairItem(new ItemStack(Items.IRON_INGOT))
                && new ItemStack(FishingItems.rod(io.github.sirschrolli.kingdomsatwar.fishing.RodType.DRAGON))
                        .isValidRepairItem(new ItemStack(io.github.sirschrolli.kingdomsatwar.registry.ModItems.DRACONIUM.get())), "Nicht mit dem Werkstoff ausbesserbar");

        // die Vorteile: wo sie wirken und wo nicht
        var water = new io.github.sirschrolli.kingdomsatwar.fishing.RodType.Place(Medium.WATER, false, false);
        var night = new io.github.sirschrolli.kingdomsatwar.fishing.RodType.Place(Medium.WATER, true, false);
        var lava = new io.github.sirschrolli.kingdomsatwar.fishing.RodType.Place(Medium.LAVA, false, false);
        var voidPlace = io.github.sirschrolli.kingdomsatwar.fishing.RodType.Place.of(level, helper.absolutePos(BlockPos.ZERO), Medium.VOID);
        helper.assertTrue(voidPlace.dark() && voidPlace.end(), "Die Leere ist nicht dunkel oder zählt nicht wie das End");
        var types = io.github.sirschrolli.kingdomsatwar.fishing.RodType.class;
        helper.assertTrue(io.github.sirschrolli.kingdomsatwar.fishing.RodType.GLUT.haste(lava) == 0.5f
                && io.github.sirschrolli.kingdomsatwar.fishing.RodType.GLUT.haste(water) == 0.0f
                && io.github.sirschrolli.kingdomsatwar.fishing.RodType.SHADOW.haste(night) > 0.0f
                && io.github.sirschrolli.kingdomsatwar.fishing.RodType.SHADOW.haste(water) == 0.0f
                && io.github.sirschrolli.kingdomsatwar.fishing.RodType.STAR.zone(voidPlace) > io.github.sirschrolli.kingdomsatwar.fishing.RodType.STAR.zone(water)
                && io.github.sirschrolli.kingdomsatwar.fishing.RodType.GOLD.luck(water) == 2
                && io.github.sirschrolli.kingdomsatwar.fishing.RodType.IRON.zone(water) > 0.0f, "Die Vorteile der Angeln stimmen nicht: " + types);
        helper.assertTrue(Fishing.zoneBonus(0, 0, null, water) == 0.0f && Math.abs(Fishing.zoneBonus(0, 3, null, water) - 0.3f) < 1.0e-4f
                        && Math.abs(Fishing.zoneBonus(2, 1, io.github.sirschrolli.kingdomsatwar.fishing.RodType.IRON, water) - 0.3f) < 1.0e-4f
                        && Fishing.zoneBonus(10, 3, io.github.sirschrolli.kingdomsatwar.fishing.RodType.GLUT, lava) == Fishing.ZONE_CAP,
                "Fangzone: " + Fishing.zoneBonus(2, 1, io.github.sirschrolli.kingdomsatwar.fishing.RodType.IRON, water));
        helper.assertTrue(Math.abs(Fishing.haste(0.25f, 0.5f, 3) - (1.0f - 0.75f * 0.5f * 0.82f)) < 1.0e-4f && Fishing.haste(0.0f, 0.0f, 0) == 0.0f,
                "Köder, Angel und Verzauberung wirken nicht zusammen: " + Fishing.haste(0.25f, 0.5f, 3));

        // die Glutangel über der Lava
        for (int x = 0; x <= 4; x++) {
            for (int z = 0; z <= 4; z++) {
                boolean inner = x >= 1 && x <= 3 && z >= 1 && z <= 3;
                helper.setBlock(new BlockPos(x, 1, z), inner ? Blocks.LAVA : Blocks.STONE);
            }
        }
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.getAbilities().instabuild = false;
        Vec3 stand = helper.absoluteVec(new Vec3(2.5, 2.0, 5.5));
        player.snapTo(stand.x, stand.y, stand.z, 180.0f, 0.0f);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(FishingItems.rod(io.github.sirschrolli.kingdomsatwar.fishing.RodType.GLUT)));
        FishingHook hook = cast(level, player, helper.absoluteVec(new Vec3(2.5, 2.4, 2.5)));
        helper.runAfterDelay(25, () -> {
            try {
                helper.assertTrue(hook.isAlive() && ((HookAccess) hook).kingdomsatwar$medium() == Medium.LAVA, "Der Haken treibt nicht auf der Lava");
                helper.assertTrue(Math.abs(Fishing.haste(hook) - 0.5f) < 1.0e-4f, "Die Glutangel beschleunigt den Biss in der Lava nicht: " + Fishing.haste(hook));
                // mit der Angel von Minecraft: kein Vorteil
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.FISHING_ROD));
                helper.assertTrue(Fishing.haste(hook) == 0.0f, "Die Angel von Minecraft beschleunigt: " + Fishing.haste(hook));
            } finally {
                hook.discard();
                level.getServer().getPlayerList().remove(player);
            }
            helper.succeed();
        });
    }

    private static void flow(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        // zwei Becken, eins mit Wasser, eins mit Lava; dazwischen steht der Angler
        for (int x = 0; x <= 8; x++) {
            for (int z = 0; z <= 8; z++) {
                boolean water = x <= 4 && z <= 4;
                boolean lava = x >= 4 && z >= 4;
                if (!water && !lava) {
                    continue;
                }
                boolean inner = (water && x >= 1 && x <= 3 && z >= 1 && z <= 3) || (lava && x >= 5 && x <= 7 && z >= 5 && z <= 7);
                helper.setBlock(new BlockPos(x, 1, z), inner ? (x <= 3 ? Blocks.WATER : Blocks.LAVA) : Blocks.STONE);
            }
        }
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.getAbilities().instabuild = false;
        Vec3 stand = helper.absoluteVec(new Vec3(4.5, 2.0, 4.5));
        player.snapTo(stand.x, stand.y, stand.z, 0.0f, 0.0f);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.FISHING_ROD));
        player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(FishingItems.BAIT_WORM.get(), 16));
        Vec3 pond = helper.absoluteVec(new Vec3(2.5, 1.95, 2.5));
        Vec3 pit = helper.absoluteVec(new Vec3(6.5, 2.4, 6.5));
        FishingHook[] hook = new FishingHook[1];
        int[] session = new int[1];
        Runnable cleanup = () -> {
            if (player.fishing != null) {
                player.fishing.discard();
            }
            level.getServer().getPlayerList().remove(player);
        };
        try {
            helper.assertTrue(Fishing.log(player).speciesCaught() == 0, "Das Fischbuch ist nicht leer");
            hook[0] = hookFish(helper, level, player, pond);
            session[0] = Fishing.sessionOf(player);
            helper.assertTrue(player.getOffhandItem().getCount() < 16, "Der Köder wurde nicht verbraucht");
            // mitten im Drill wird nicht eingeholt
            helper.assertTrue(hook[0].retrieve(player.getMainHandItem()) == 0 && hook[0].isAlive(), "Im Drill eingeholt");
        } catch (RuntimeException e) {
            cleanup.run();
            throw e;
        }
        helper.runAfterDelay(30, () -> {
            try {
                // der Biss hält an, solange gedrillt wird (sonst wäre er nach spätestens 40 Ticks vorbei)
                helper.assertTrue(hook[0].isAlive() && Fishing.isFighting(hook[0]) && Fishing.sessionOf(player) == session[0], "Der Drill ist abgerissen");
                helper.assertTrue(((HookAccess) hook[0]).kingdomsatwar$medium() == Medium.WATER, "Der Haken treibt nicht im Wasser");
                FishSpecies first = Fishing.fighting(player);
                // ein Fang, schneller als möglich: zählt nicht – der Fisch entkommt, der Haken bleibt draußen
                Fishing.onResult(player, session[0], true, true, false);
                helper.assertTrue(hook[0].isAlive() && Fishing.sessionOf(player) == 0 && Fishing.log(player).speciesCaught() == 0,
                        "Ein unmöglich schneller Fang zählt");
                helper.assertTrue(first != null && !first.ofLava(), "Im Teich hing ein Lavafisch am Haken");
                // nächster Biss, diesmal ehrlich gedrillt
                hook[0] = hookFish(helper, level, player, pond);
                session[0] = Fishing.sessionOf(player);
                FishSpecies species = Fishing.fighting(player);
                Fishing.ageSession(player, 600);
                Fishing.onResult(player, session[0], true, true, false);
                helper.assertTrue(hook[0].isRemoved() && player.fishing == null && Fishing.sessionOf(player) == 0, "Nach dem Fang ist der Haken noch draußen");
                FishLog log = Fishing.log(player);
                helper.assertTrue(species != null && log.count(species) == 1 && log.best(species) >= species.minCm() && log.best(species) <= species.maxCm(),
                        "Der Fang steht nicht im Fischbuch");
                helper.assertTrue(player.getMainHandItem().getDamageValue() == 1, "Die Angel nutzt sich nicht ab: " + player.getMainHandItem().getDamageValue());
                List<ItemEntity> drops = level.getEntitiesOfClass(ItemEntity.class, new AABB(pond, pond).inflate(3.0),
                        e -> e.getItem().is(species.item()));
                helper.assertTrue(!drops.isEmpty(), "Der Fisch fliegt nicht heraus");
                drops.forEach(ItemEntity::discard);
                // ab jetzt trägt der Angler einen Fischbeutel: Der nächste Fang wandert hinein, statt heranzufliegen
                player.getInventory().setItem(10, new ItemStack(FishingItems.FISH_BAG.get()));
                hook[0] = hookFish(helper, level, player, pond);
                FishSpecies bagged = Fishing.fighting(player);
                Fishing.ageSession(player, 600);
                Fishing.onResult(player, Fishing.sessionOf(player), true, false, false);
                helper.assertTrue(bagged != null && FishBag.of(player.getInventory().getItem(10)).count(bagged) == 1
                        && FishBag.of(player.getInventory().getItem(10)).total() == 1, "Der Fang ist nicht im Fischbeutel");
                helper.assertTrue(level.getEntitiesOfClass(ItemEntity.class, new AABB(pond, pond).inflate(4.0), e -> e.getItem().is(bagged.item())).isEmpty(),
                        "Der Fisch fliegt heraus, obwohl er im Beutel liegt");
                helper.assertTrue(Fishing.log(player).count(bagged) >= 1, "Der Fang im Beutel steht nicht im Fischbuch");
                player.getInventory().setItem(10, ItemStack.EMPTY);
                // und nun in die Lava
                hook[0] = cast(level, player, pit);
            } catch (RuntimeException e) {
                cleanup.run();
                throw e;
            }
        });
        helper.runAfterDelay(55, () -> {
            try {
                FishingHook lavaHook = hook[0];
                helper.assertTrue(lavaHook.isAlive() && ((HookAccess) lavaHook).kingdomsatwar$medium() == Medium.LAVA, "Der Haken treibt nicht auf der Lava");
                double surface = helper.absoluteVec(new Vec3(0.0, 1.9, 0.0)).y;
                helper.assertTrue(Math.abs(lavaHook.getY() - surface) < 0.5 && !lavaHook.isOnFire(),
                        "Der Schwimmer treibt nicht an der Oberfläche oder brennt: " + (lavaHook.getY() - surface));
                ((HookAccess) lavaHook).kingdomsatwar$forceBite();
                helper.assertTrue(lavaHook.retrieve(player.getMainHandItem()) == 0 && Fishing.isFighting(lavaHook), "In der Lava beginnt kein Drill");
                FishSpecies species = Fishing.fighting(player);
                helper.assertTrue(species != null && species.ofLava(), "In der Lava hing kein Lavafisch am Haken: " + species);
                Fishing.ageSession(player, 600);
                Fishing.onResult(player, Fishing.sessionOf(player), true, false, true);
                helper.assertTrue(lavaHook.isRemoved() && Fishing.log(player).has(species), "Der Lavafisch wurde nicht gefangen");
                List<ItemEntity> drops = level.getEntitiesOfClass(ItemEntity.class, new AABB(stand, stand).inflate(4.0),
                        e -> e.getItem().is(species.item()));
                helper.assertTrue(!drops.isEmpty() && drops.getFirst().getItem().has(DataComponents.DAMAGE_RESISTANT), "Der Lavafisch kam nicht an");
                drops.forEach(ItemEntity::discard);
                voidFishing(helper, player);
            } finally {
                cleanup.run();
            }
            helper.succeed();
        });
    }
}
