package io.github.sirschrolli.kingdomsatwar.gametest;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.fishing.Bait;
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

    private FishingGameTests() {}

    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        event.registerTest(SPECIES.getId(), new FunctionGameTestInstance(SPECIES.key(),
                new TestData<>(environment, Level.OVERWORLD, ARENA, 100, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        event.registerTest(FIGHT.getId(), new FunctionGameTestInstance(FIGHT.key(),
                new TestData<>(environment, Level.OVERWORLD, ARENA, 100, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        event.registerTest(FLOW.getId(), new FunctionGameTestInstance(FLOW.key(),
                new TestData<>(environment, Level.OVERWORLD, ARENA, 200, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
    }

    // =====================================================================================
    // Die Arten
    // =====================================================================================

    private static void species(GameTestHelper helper) {
        JsonObject german = lang(helper, "de_de");
        JsonObject english = lang(helper, "en_us");
        RandomSource random = RandomSource.create(79);
        Map<String, FishSpecies> effects = new HashMap<>();
        int custom = 0;
        for (FishSpecies species : FishSpecies.values()) {
            String id = species.id();
            helper.assertTrue(!species.habitats().isEmpty(), "Art ohne Lebensraum: " + id);
            helper.assertTrue(species.minCm() > 0 && species.maxCm() > species.minCm(), "Größe falsch: " + id);
            helper.assertTrue(species.difficulty() > 0.1f && species.difficulty() < 1.0f, "Schwierigkeit außerhalb: " + id);
            // irgendwann, irgendwo beißt jede
            float weight = species.weight(EnumSet.copyOf(species.habitats()), species.time() != FishSpecies.Time.NIGHT, false,
                    species.weather() != FishSpecies.Weather.ANY, species.weather() == FishSpecies.Weather.THUNDER_ONLY, 0, Bait.NONE);
            helper.assertTrue(weight > 0.0f, "Art beißt nie: " + id);
            for (int i = 0; i < 20; i++) {
                int cm = species.rollSize(random, i % 2 == 0);
                helper.assertTrue(cm >= species.minCm() && cm <= species.maxCm(), "Größe außerhalb der Spanne: " + id + " " + cm);
            }
            helper.assertTrue(FishingGameTests.class.getResource("/assets/kingdomsatwar/textures/gui/fishing/silhouette/" + id + ".png") != null,
                    "Schattenriss fehlt: " + id);
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
        helper.assertTrue(FishSpecies.values().length == 76 && custom == 72, "Zahl der Arten: " + FishSpecies.values().length + " / " + custom);

        // überall beißt etwas: in jedem allgemeinen Lebensraum bei Tag und Nacht, und jeder besondere hat eigene Arten
        for (Habitat habitat : Habitat.values()) {
            boolean own = false;
            for (FishSpecies species : FishSpecies.values()) {
                own |= species.habitats().contains(habitat);
            }
            helper.assertTrue(own, "Lebensraum ohne Art: " + habitat);
            if (!habitat.special()) {
                for (boolean day : new boolean[] {true, false}) {
                    helper.assertTrue(FishSpecies.pick(EnumSet.of(habitat), day, false, false, false, 0, Bait.NONE, random) != null,
                            "Hier beißt nichts: " + habitat + (day ? " am Tag" : " in der Nacht"));
                }
            }
        }
        // Legenden sind selten – etwa eine je hundert Bisse an ihrem Ort, mit Glück des Meeres III doppelt so oft –, und zwar
        // überall gleich selten: im Gewimmel der Tiefsee nicht seltener als im See
        EnumSet<Habitat> lake = EnumSet.of(Habitat.FRESH);
        EnumSet<Habitat> deepSea = EnumSet.of(Habitat.SALT, Habitat.DEEP);
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
                && FishSpecies.STORMSAIL.legendChance(EnumSet.of(Habitat.SALT), true, false, true, false, 0, Bait.NONE) == 0.0f
                && FishSpecies.STORMSAIL.legendChance(EnumSet.of(Habitat.SALT), true, false, true, true, 0, Bait.NONE) > FishSpecies.LEGEND_CHANCE,
                "Die Chancen der Legenden");
        // Goldschuppe nur bei Regen, Sturmsegler nur im Gewitter
        helper.assertTrue(FishSpecies.GOLDSCALE.weight(lake, true, false, false, false, 0, Bait.NONE) == 0.0f
                && FishSpecies.GOLDSCALE.weight(lake, true, false, true, false, 0, Bait.NONE) > 0.0f, "Goldschuppe und der Regen");
        EnumSet<Habitat> sea = EnumSet.of(Habitat.SALT);
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
            helper.assertTrue(size >= 3 && size <= 25, "Kapitel " + chapter + " hat " + size + " Arten");
            helper.assertTrue(german.has(chapter.key()) && english.has(chapter.key()), "Kapitelname fehlt: " + chapter);
            total += size;
        }
        helper.assertTrue(total == FishSpecies.values().length, "Kapitel decken nicht alle Arten ab");
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
        FishFight fight = new FishFight(species.behavior(), species.difficulty(), 0.0f, false, RandomSource.create(seed));
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

    private static void fight(GameTestHelper helper) {
        StringBuilder table = new StringBuilder();
        for (FishSpecies species : FishSpecies.values()) {
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
            table.append(String.format("%n  %-18s %-9s %.2f  %2d/%d  perfekt %2d", species.id(), species.behavior(), species.difficulty(), wins, runs, perfect));
            // kein Fang geht schneller, als der Server ihn glaubt
            if (wins > 0) {
                helper.assertTrue(fastest / 6 >= FishFight.minTicks(species.difficulty()), "Fang schneller als die Mindestzeit: " + species.id());
            }
            if (species.difficulty() <= 0.5f) {
                helper.assertTrue(wins == runs, "Leichter Fisch entkommt einem geübten Angler: " + species.id() + " " + wins + "/" + runs);
            } else if (species.rarity() == FishSpecies.Rarity.LEGENDARY) {
                helper.assertTrue(wins >= runs / 4, "Legende unfangbar: " + species.id() + " " + wins + "/" + runs);
            } else {
                helper.assertTrue(wins >= runs / 2, "Fisch zu schwer: " + species.id() + " " + wins + "/" + runs);
            }
        }
        KingdomsAtWar.LOGGER.info("Drill – geübter Angler (0,2 s Verzug), Siege von 24:{}", table);
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
        helper.assertTrue(species != null && species.habitats().contains(Habitat.END), "In der Leere des Ends hing kein Endfisch am Haken: " + species);
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
