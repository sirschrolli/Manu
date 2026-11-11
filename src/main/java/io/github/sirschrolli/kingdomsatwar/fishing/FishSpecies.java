package io.github.sirschrolli.kingdomsatwar.fishing;

import static io.github.sirschrolli.kingdomsatwar.fishing.FishSpecies.Behavior.CALM;
import static io.github.sirschrolli.kingdomsatwar.fishing.FishSpecies.Behavior.DARTER;
import static io.github.sirschrolli.kingdomsatwar.fishing.FishSpecies.Behavior.FLOATER;
import static io.github.sirschrolli.kingdomsatwar.fishing.FishSpecies.Behavior.JITTER;
import static io.github.sirschrolli.kingdomsatwar.fishing.FishSpecies.Behavior.MIXED;
import static io.github.sirschrolli.kingdomsatwar.fishing.FishSpecies.Behavior.SINKER;
import static io.github.sirschrolli.kingdomsatwar.fishing.FishSpecies.Behavior.STEADY;
import static io.github.sirschrolli.kingdomsatwar.fishing.FishSpecies.Rarity.COMMON;
import static io.github.sirschrolli.kingdomsatwar.fishing.FishSpecies.Rarity.EPIC;
import static io.github.sirschrolli.kingdomsatwar.fishing.FishSpecies.Rarity.LEGENDARY;
import static io.github.sirschrolli.kingdomsatwar.fishing.FishSpecies.Rarity.RARE;
import static io.github.sirschrolli.kingdomsatwar.fishing.FishSpecies.Rarity.UNCOMMON;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

/**
 * Die Fische des Angelns (seit 0.79): 76 Arten – für jedes Gewässer und jede Welt eigene. Jede Art hat eine Seltenheit, einen
 * Lebensraum (wo sie beißt), eine Größe (was im Fischbuch als Rekord zählt), eine Schwierigkeit und ein Verhalten im Drill
 * (wie sie an der Schnur zieht) und eine Wirkung beim Essen – keine zwei wirken gleich. Die vier Fische von Minecraft
 * gehören dazu (als ihre eigenen Gegenstände, mit ihrer gewohnten Wirkung).
 *
 * <p>Wo welcher Fisch beißt, bestimmt {@link Habitat#at}: in Flüssen und Seen der Oberwelt (mit eigenen Arten für Sümpfe,
 * Dschungel, Kälte, Gebirge, Trockenland, Höhlen, Kirschhaine, den Düsterwald und die Pilzinseln), im Meer (warm, kalt,
 * tief), in der <b>Lava</b> des Nethers (je Nether-Biom eine Art), in der <b>Leere</b> des Ends und der Leere, im Feenreich,
 * in der Schattenwelt und im Abgrund. Manche beißen nur nachts oder bei Regen, eine Legende nur im Gewitter.
 */
public enum FishSpecies implements StringRepresentable {
    // --- die Fische von Minecraft ---
    COD(b("cod", COMMON, 30, 90, 0.20f, STEADY).in(Habitat.SALT, Habitat.SALT_COLD).vanilla(() -> Items.COD)),
    SALMON(b("salmon", COMMON, 40, 110, 0.26f, DARTER).in(Habitat.FRESH, Habitat.FRESH_COLD, Habitat.SALT_COLD).vanilla(() -> Items.SALMON)),
    TROPICAL_FISH(b("tropical_fish", UNCOMMON, 5, 20, 0.30f, JITTER).in(Habitat.SALT_WARM, Habitat.FRESH_WARM).vanilla(() -> Items.TROPICAL_FISH)),
    PUFFERFISH(b("pufferfish", UNCOMMON, 10, 40, 0.38f, FLOATER).in(Habitat.SALT_WARM, Habitat.SALT).vanilla(() -> Items.PUFFERFISH)),
    // --- Flüsse und Seen ---
    BROOK_TROUT(b("brook_trout", COMMON, 20, 45, 0.20f, STEADY).in(Habitat.FRESH).eat(MobEffects.SPEED, 60, 0)),
    RIVER_PERCH(b("river_perch", COMMON, 15, 40, 0.22f, JITTER).in(Habitat.FRESH).time(Time.DAY).eat(MobEffects.HASTE, 60, 0)),
    CARP(b("carp", COMMON, 30, 80, 0.28f, CALM).in(Habitat.FRESH, Habitat.SWAMP).eat(MobEffects.REGENERATION, 12, 0)),
    PIKE(b("pike", UNCOMMON, 45, 120, 0.50f, DARTER).in(Habitat.FRESH).time(Time.DAY).eat(MobEffects.STRENGTH, 45, 0)),
    CATFISH(b("catfish", UNCOMMON, 60, 200, 0.48f, SINKER).in(Habitat.FRESH, Habitat.SWAMP).time(Time.NIGHT).eat(MobEffects.NIGHT_VISION, 120, 0)),
    EEL(b("eel", UNCOMMON, 40, 110, 0.45f, JITTER).in(Habitat.FRESH).time(Time.NIGHT).weather(Weather.RAIN).eat(MobEffects.DOLPHINS_GRACE, 45, 0)),
    STURGEON(b("sturgeon", RARE, 90, 300, 0.62f, SINKER).in(Habitat.FRESH).eat(MobEffects.ABSORPTION, 120, 1)),
    GOLDFISH(b("goldfish", RARE, 8, 25, 0.40f, FLOATER).in(Habitat.FRESH).time(Time.DAY).eat(MobEffects.LUCK, 300, 0)),
    PIRANHA(b("piranha", UNCOMMON, 15, 40, 0.52f, DARTER).in(Habitat.FRESH_WARM).eat(MobEffects.STRENGTH, 15, 1).eat(MobEffects.HUNGER, 15, 0)),
    ARAPAIMA(b("arapaima", RARE, 120, 300, 0.68f, SINKER).in(Habitat.FRESH_WARM).eat(MobEffects.HEALTH_BOOST, 180, 1)),
    MUDSKIPPER(b("mudskipper", COMMON, 8, 20, 0.18f, JITTER).in(Habitat.SWAMP).eat(MobEffects.JUMP_BOOST, 60, 1)),
    ARCTIC_CHAR(b("arctic_char", UNCOMMON, 25, 70, 0.40f, STEADY).in(Habitat.FRESH_COLD).eat(MobEffects.RESISTANCE, 60, 0)),
    RAINBOW_TROUT(b("rainbow_trout", UNCOMMON, 25, 70, 0.42f, DARTER).in(Habitat.MOUNTAIN, Habitat.FRESH).time(Time.DAY).weather(Weather.RAIN)
            .eat(MobEffects.REGENERATION, 8, 1)),
    DESERT_PUPFISH(b("desert_pupfish", UNCOMMON, 3, 8, 0.30f, JITTER).in(Habitat.DRY).eat(MobEffects.FIRE_RESISTANCE, 45, 0).eat(MobEffects.SPEED, 45, 0)),
    CAVE_TETRA(b("cave_tetra", UNCOMMON, 6, 12, 0.35f, FLOATER).in(Habitat.CAVE).eat(MobEffects.NIGHT_VISION, 240, 0).eat(MobEffects.HASTE, 30, 0)),
    DRIPSTONE_LOACH(b("dripstone_loach", COMMON, 8, 18, 0.30f, SINKER).in(Habitat.CAVE).eat(MobEffects.HASTE, 30, 1)),
    SCULK_EEL(b("sculk_eel", RARE, 60, 150, 0.70f, MIXED).in(Habitat.SCULK).eat(MobEffects.DARKNESS, 5, 0).eat(MobEffects.INVISIBILITY, 60, 0)),
    BLOSSOM_KOI(b("blossom_koi", RARE, 30, 70, 0.50f, FLOATER).in(Habitat.BLOSSOM).eat(MobEffects.REGENERATION, 30, 0).eat(MobEffects.LUCK, 60, 0)),
    GLOOM_PIKE(b("gloom_pike", UNCOMMON, 50, 110, 0.55f, DARTER).in(Habitat.DARK).eat(MobEffects.INVISIBILITY, 45, 0)),
    SHROOM_PERCH(b("shroom_perch", UNCOMMON, 15, 35, 0.40f, JITTER).in(Habitat.MUSHROOM).food(7, 0.9f).eat(MobEffects.NAUSEA, 8, 0)),
    // --- das Meer ---
    HERRING(b("herring", COMMON, 15, 40, 0.18f, STEADY).in(Habitat.SALT).eat(MobEffects.WATER_BREATHING, 90, 0)),
    MACKEREL(b("mackerel", COMMON, 20, 50, 0.26f, DARTER).in(Habitat.SALT).time(Time.DAY).eat(MobEffects.SPEED, 20, 1)),
    REDFISH(b("redfish", COMMON, 25, 60, 0.24f, CALM).in(Habitat.SALT, Habitat.SALT_COLD).eat(MobEffects.ABSORPTION, 60, 0)),
    PLAICE(b("plaice", UNCOMMON, 25, 70, 0.34f, SINKER).in(Habitat.SALT).eat(MobEffects.RESISTANCE, 90, 0)),
    TUNA(b("tuna", UNCOMMON, 80, 250, 0.58f, DARTER).in(Habitat.SALT).time(Time.DAY).eat(MobEffects.STRENGTH, 90, 0).eat(MobEffects.SPEED, 90, 0)),
    SWORDFISH(b("swordfish", RARE, 150, 400, 0.72f, DARTER).in(Habitat.SALT).eat(MobEffects.STRENGTH, 30, 1)),
    SUNFISH(b("sunfish", RARE, 100, 300, 0.50f, FLOATER).in(Habitat.SALT).time(Time.DAY).eat(MobEffects.SLOW_FALLING, 90, 0)),
    SEAHORSE(b("seahorse", UNCOMMON, 5, 25, 0.36f, FLOATER).in(Habitat.SALT_WARM).eat(MobEffects.DOLPHINS_GRACE, 90, 0)),
    PARROTFISH(b("parrotfish", UNCOMMON, 25, 90, 0.38f, STEADY).in(Habitat.SALT_WARM).time(Time.DAY).eat(MobEffects.JUMP_BOOST, 120, 0)
            .eat(MobEffects.SPEED, 120, 0)),
    LIONFISH(b("lionfish", RARE, 20, 45, 0.55f, JITTER).in(Habitat.SALT_WARM).eat(MobEffects.POISON, 6, 1).eat(MobEffects.STRENGTH, 60, 1)),
    OCTOPUS(b("octopus", UNCOMMON, 30, 120, 0.50f, MIXED).in(Habitat.SALT).time(Time.NIGHT).eat(MobEffects.INVISIBILITY, 30, 0)
            .eat(MobEffects.WATER_BREATHING, 30, 0)),
    CATSHARK(b("catshark", RARE, 50, 160, 0.66f, DARTER).in(Habitat.SALT, Habitat.DEEP).time(Time.NIGHT).eat(MobEffects.STRENGTH, 60, 0)
            .eat(MobEffects.NIGHT_VISION, 60, 0)),
    HALIBUT(b("halibut", RARE, 80, 250, 0.60f, SINKER).in(Habitat.SALT_COLD).eat(MobEffects.RESISTANCE, 30, 1)),
    // --- die Tiefsee ---
    LANTERNFISH(b("lanternfish", COMMON, 5, 15, 0.25f, JITTER).in(Habitat.DEEP).eat(MobEffects.GLOWING, 30, 0).eat(MobEffects.NIGHT_VISION, 60, 0)),
    HATCHETFISH(b("hatchetfish", COMMON, 4, 12, 0.28f, FLOATER).in(Habitat.DEEP).eat(MobEffects.SPEED, 30, 0).eat(MobEffects.SLOW_FALLING, 30, 0)),
    ANGLERFISH(b("anglerfish", UNCOMMON, 20, 100, 0.52f, SINKER).in(Habitat.DEEP).time(Time.NIGHT).eat(MobEffects.NIGHT_VISION, 300, 0)),
    VIPERFISH(b("viperfish", UNCOMMON, 20, 60, 0.56f, DARTER).in(Habitat.DEEP).eat(MobEffects.STRENGTH, 20, 1).eat(MobEffects.WITHER, 4, 0)),
    FIREFLY_SQUID(b("firefly_squid", UNCOMMON, 5, 12, 0.40f, MIXED).in(Habitat.DEEP).time(Time.NIGHT).eat(MobEffects.GLOWING, 60, 0)
            .eat(MobEffects.SPEED, 30, 1)),
    GIANT_ISOPOD(b("giant_isopod", UNCOMMON, 15, 50, 0.42f, SINKER).in(Habitat.DEEP).eat(MobEffects.RESISTANCE, 60, 1).eat(MobEffects.SLOWNESS, 60, 0)),
    OARFISH(b("oarfish", RARE, 300, 800, 0.70f, MIXED).in(Habitat.DEEP).eat(MobEffects.CONDUIT_POWER, 60, 0)),
    GHOST_SHARK(b("ghost_shark", RARE, 60, 150, 0.66f, DARTER).in(Habitat.DEEP).eat(MobEffects.INVISIBILITY, 90, 0)),
    COELACANTH(b("coelacanth", EPIC, 100, 200, 0.78f, SINKER).in(Habitat.DEEP).eat(MobEffects.HEALTH_BOOST, 300, 2)),
    VAMPIRE_SQUID(b("vampire_squid", EPIC, 15, 30, 0.80f, MIXED).in(Habitat.DEEP).time(Time.NIGHT).eat(MobEffects.INSTANT_HEALTH, 0, 1)
            .eat(MobEffects.DARKNESS, 6, 0)),
    // --- die Lava des Nethers ---
    MAGMA_CARP(b("magma_carp", COMMON, 30, 70, 0.40f, CALM).in(Habitat.LAVA).eat(MobEffects.FIRE_RESISTANCE, 120, 0)),
    CRIMSON_SNAPPER(b("crimson_snapper", UNCOMMON, 25, 60, 0.50f, DARTER).in(Habitat.LAVA_CRIMSON).eat(MobEffects.STRENGTH, 120, 0)
            .eat(MobEffects.FIRE_RESISTANCE, 30, 0)),
    WARP_FIN(b("warp_fin", UNCOMMON, 20, 50, 0.55f, MIXED).in(Habitat.LAVA_WARPED).special(Special.TELEPORT_NEAR).eat(MobEffects.FIRE_RESISTANCE, 30, 0)),
    SOUL_EEL(b("soul_eel", RARE, 60, 160, 0.65f, JITTER).in(Habitat.LAVA_SOUL).eat(MobEffects.SPEED, 15, 2).eat(MobEffects.WITHER, 3, 0)),
    BASALT_PIKE(b("basalt_pike", UNCOMMON, 50, 120, 0.60f, DARTER).in(Habitat.LAVA_BASALT).eat(MobEffects.RESISTANCE, 120, 0)
            .eat(MobEffects.FIRE_RESISTANCE, 60, 0)),
    EMBER_RAY(b("ember_ray", RARE, 60, 140, 0.70f, FLOATER).in(Habitat.LAVA).eat(MobEffects.FIRE_RESISTANCE, 300, 0).eat(MobEffects.REGENERATION, 20, 0)),
    // --- die Leere des Ends und der Leere ---
    ENDER_SALMON(b("ender_salmon", COMMON, 30, 70, 0.40f, DARTER).in(Habitat.END).special(Special.TELEPORT_FAR)),
    CHORUS_FISH(b("chorus_fish", UNCOMMON, 15, 40, 0.45f, FLOATER).in(Habitat.END).eat(MobEffects.LEVITATION, 4, 0).eat(MobEffects.SLOW_FALLING, 20, 0)),
    VOID_RAY(b("void_ray", RARE, 80, 200, 0.70f, FLOATER).in(Habitat.END, Habitat.VOID).eat(MobEffects.SLOW_FALLING, 180, 0)),
    STARDUST_MINNOW(b("stardust_minnow", COMMON, 5, 12, 0.30f, JITTER).in(Habitat.VOID).eat(MobEffects.LUCK, 60, 1)),
    CRYSTAL_FIN(b("crystal_fin", UNCOMMON, 20, 50, 0.50f, STEADY).in(Habitat.VOID).eat(MobEffects.ABSORPTION, 90, 2)),
    // --- Feenreich, Schattenwelt, Abgrund ---
    GLIMMERFISH(b("glimmerfish", COMMON, 8, 20, 0.24f, FLOATER).in(Habitat.FAE).eat(MobEffects.GLOWING, 20, 0).eat(MobEffects.LUCK, 120, 0)),
    MUSHROOM_SWIMMER(b("mushroom_swimmer", UNCOMMON, 10, 30, 0.38f, JITTER).in(Habitat.FAE).eat(MobEffects.JUMP_BOOST, 30, 2)),
    MOONLILY_KOI(b("moonlily_koi", RARE, 30, 80, 0.56f, STEADY).in(Habitat.FAE).eat(MobEffects.REGENERATION, 15, 1).eat(MobEffects.NIGHT_VISION, 60, 0)),
    THORN_PIKE(b("thorn_pike", RARE, 40, 100, 0.64f, DARTER).in(Habitat.FAE).eat(MobEffects.STRENGTH, 120, 0).eat(MobEffects.POISON, 5, 0)),
    MIST_CARP(b("mist_carp", COMMON, 25, 60, 0.30f, CALM).in(Habitat.SHADOW).eat(MobEffects.SLOW_FALLING, 60, 0).eat(MobEffects.INVISIBILITY, 20, 0)),
    BLIND_PIKE(b("blind_pike", UNCOMMON, 40, 100, 0.52f, DARTER).in(Habitat.SHADOW).eat(MobEffects.BLINDNESS, 5, 0).eat(MobEffects.STRENGTH, 20, 2)),
    SHADOW_EEL(b("shadow_eel", UNCOMMON, 50, 140, 0.52f, JITTER).in(Habitat.SHADOW).eat(MobEffects.INVISIBILITY, 60, 0).eat(MobEffects.SPEED, 60, 0)),
    SOULFISH(b("soulfish", RARE, 20, 50, 0.60f, FLOATER).in(Habitat.SHADOW).special(Special.CLEANSE).eat(MobEffects.REGENERATION, 10, 0)),
    EMBER_CATFISH(b("ember_catfish", UNCOMMON, 50, 130, 0.50f, SINKER).in(Habitat.ABYSS).eat(MobEffects.FIRE_RESISTANCE, 180, 0)
            .eat(MobEffects.STRENGTH, 60, 0)),
    CHAOS_FIN(b("chaos_fin", RARE, 30, 90, 0.66f, MIXED).in(Habitat.ABYSS).special(Special.TELEPORT_FAR).eat(MobEffects.SPEED, 20, 1)),
    RIFT_EEL(b("rift_eel", EPIC, 80, 220, 0.80f, DARTER).in(Habitat.ABYSS).eat(MobEffects.LEVITATION, 6, 0).eat(MobEffects.SLOW_FALLING, 30, 0)
            .eat(MobEffects.STRENGTH, 60, 1)),
    // --- Legenden (je Welt eine; sehr selten, sehr schwer) ---
    OLD_ONE(b("old_one", LEGENDARY, 140, 220, 0.90f, SINKER).in(Habitat.FRESH).time(Time.NIGHT).eat(MobEffects.LUCK, 600, 1)
            .eat(MobEffects.REGENERATION, 60, 0).eat(MobEffects.HEALTH_BOOST, 600, 1)),
    GOLDSCALE(b("goldscale", LEGENDARY, 80, 130, 0.90f, DARTER).in(Habitat.FRESH).time(Time.DAY).weather(Weather.RAIN_ONLY)
            .eat(MobEffects.ABSORPTION, 180, 3).eat(MobEffects.LUCK, 300, 2)),
    STORMSAIL(b("stormsail", LEGENDARY, 250, 450, 0.94f, DARTER).in(Habitat.SALT).weather(Weather.THUNDER_ONLY).eat(MobEffects.SPEED, 120, 2)
            .eat(MobEffects.STRENGTH, 120, 1)),
    DEEP_KING(b("deep_king", LEGENDARY, 120, 200, 0.94f, SINKER).in(Habitat.DEEP).time(Time.NIGHT).eat(MobEffects.NIGHT_VISION, 900, 0)
            .eat(MobEffects.STRENGTH, 120, 2).eat(MobEffects.RESISTANCE, 120, 1)),
    INFERNO_KING(b("inferno_king", LEGENDARY, 200, 350, 0.95f, SINKER).in(Habitat.LAVA).eat(MobEffects.FIRE_RESISTANCE, 600, 0)
            .eat(MobEffects.STRENGTH, 60, 2).eat(MobEffects.ABSORPTION, 120, 3)),
    DRAGON_KOI(b("dragon_koi", LEGENDARY, 90, 160, 0.94f, MIXED).in(Habitat.END).eat(MobEffects.STRENGTH, 90, 2).eat(MobEffects.RESISTANCE, 90, 1)
            .eat(MobEffects.REGENERATION, 30, 1)),
    MOONSHINE_KOI(b("moonshine_koi", LEGENDARY, 60, 110, 0.90f, FLOATER).in(Habitat.FAE).eat(MobEffects.REGENERATION, 30, 2)
            .eat(MobEffects.HEALTH_BOOST, 600, 2));

    public static final Codec<FishSpecies> CODEC = StringRepresentable.fromEnum(FishSpecies::values);

    /** Seltenheit: Gewicht beim Würfeln und Farbe im Fischbuch. */
    public enum Rarity {
        COMMON(100.0f, 0xFFFFFF), UNCOMMON(42.0f, 0x5ADC5A), RARE(13.0f, 0x58A8FF), EPIC(10.0f, 0xC878FF), LEGENDARY(0.9f, 0xFFC83A);

        public final float weight;
        public final int color;

        Rarity(float weight, int color) {
            this.weight = weight;
            this.color = color;
        }

        public String key() {
            return "fishing.kingdomsatwar.rarity." + this.name().toLowerCase(java.util.Locale.ROOT);
        }
    }

    /** Wie der Fisch an der Schnur zieht (das Minispiel bewegt ihn danach). */
    public enum Behavior { CALM, STEADY, FLOATER, SINKER, JITTER, DARTER, MIXED }

    /** Tageszeit, zu der die Art beißt (zur anderen nur selten). */
    public enum Time { ANY, DAY, NIGHT }

    /** Wetter: bei Regen häufiger, nur bei Regen, nur im Gewitter. */
    public enum Weather { ANY, RAIN, RAIN_ONLY, THUNDER_ONLY }

    /** Besondere Wirkung beim Essen (über die Statuseffekte hinaus). */
    public enum Special {
        NONE,
        /** versetzt wie eine Chorusfrucht (bis 16 Blöcke) */
        TELEPORT_FAR,
        /** versetzt ein kurzes Stück (bis 6 Blöcke) */
        TELEPORT_NEAR,
        /** nimmt alle schlechten Wirkungen */
        CLEANSE
    }

    /** Die Kapitel des Fischbuchs. */
    public enum Chapter {
        RIVERS, SEA, DEEP, NETHER, END, FAE, SHADOW, ABYSS, LEGENDS;

        public String key() {
            return "fishing.kingdomsatwar.chapter." + this.name().toLowerCase(java.util.Locale.ROOT);
        }
    }

    /** Eine Wirkung beim Essen. */
    public record Bite(Holder<MobEffect> effect, int seconds, int amplifier) {}

    private final String id;
    private final Rarity rarity;
    private final int minCm;
    private final int maxCm;
    private final float difficulty;
    private final Behavior behavior;
    private final Set<Habitat> habitats;
    private final Time time;
    private final Weather weather;
    private final List<Bite> bites;
    private final Special special;
    private final int nutrition;
    private final float saturation;
    private final @Nullable Supplier<Item> vanilla;

    FishSpecies(Builder b) {
        this.id = b.id;
        this.rarity = b.rarity;
        this.minCm = b.minCm;
        this.maxCm = b.maxCm;
        this.difficulty = b.difficulty;
        this.behavior = b.behavior;
        this.habitats = b.habitats;
        this.time = b.time;
        this.weather = b.weather;
        this.bites = List.copyOf(b.bites);
        this.special = b.special;
        this.nutrition = b.nutrition > 0 ? b.nutrition : 2 + b.rarity.ordinal();
        this.saturation = b.saturation;
        this.vanilla = b.vanilla;
    }

    private static Builder b(String id, Rarity rarity, int minCm, int maxCm, float difficulty, Behavior behavior) {
        return new Builder(id, rarity, minCm, maxCm, difficulty, behavior);
    }

    private static final class Builder {
        final String id;
        final Rarity rarity;
        final int minCm;
        final int maxCm;
        final float difficulty;
        final Behavior behavior;
        Set<Habitat> habitats = EnumSet.noneOf(Habitat.class);
        Time time = Time.ANY;
        Weather weather = Weather.ANY;
        final List<Bite> bites = new ArrayList<>();
        Special special = Special.NONE;
        int nutrition;
        float saturation = 0.3f;
        @Nullable Supplier<Item> vanilla;

        Builder(String id, Rarity rarity, int minCm, int maxCm, float difficulty, Behavior behavior) {
            this.id = id;
            this.rarity = rarity;
            this.minCm = minCm;
            this.maxCm = maxCm;
            this.difficulty = difficulty;
            this.behavior = behavior;
        }

        Builder in(Habitat first, Habitat... more) {
            this.habitats = EnumSet.of(first, more);
            return this;
        }

        Builder time(Time time) {
            this.time = time;
            return this;
        }

        Builder weather(Weather weather) {
            this.weather = weather;
            return this;
        }

        Builder eat(Holder<MobEffect> effect, int seconds, int amplifier) {
            this.bites.add(new Bite(effect, seconds, amplifier));
            return this;
        }

        Builder special(Special special) {
            this.special = special;
            return this;
        }

        Builder food(int nutrition, float saturation) {
            this.nutrition = nutrition;
            this.saturation = saturation;
            return this;
        }

        Builder vanilla(Supplier<Item> item) {
            this.vanilla = item;
            return this;
        }
    }

    public String id() {
        return this.id;
    }

    @Override
    public String getSerializedName() {
        return this.id;
    }

    public Rarity rarity() {
        return this.rarity;
    }

    public int minCm() {
        return this.minCm;
    }

    public int maxCm() {
        return this.maxCm;
    }

    /** 0 (ein Kinderspiel) bis 1 (kaum zu halten). */
    public float difficulty() {
        return this.difficulty;
    }

    public Behavior behavior() {
        return this.behavior;
    }

    public Set<Habitat> habitats() {
        return this.habitats;
    }

    public Time time() {
        return this.time;
    }

    public Weather weather() {
        return this.weather;
    }

    public List<Bite> bites() {
        return this.bites;
    }

    public Special special() {
        return this.special;
    }

    public int nutrition() {
        return this.nutrition;
    }

    public float saturation() {
        return this.saturation;
    }

    /** Ein Fisch von Minecraft (sein Gegenstand kommt aus dem Spiel selbst)? */
    public boolean isVanilla() {
        return this.vanilla != null;
    }

    /** Der Gegenstand dieses Fischs. */
    public Item item() {
        return this.vanilla != null ? this.vanilla.get() : io.github.sirschrolli.kingdomsatwar.registry.FishingItems.fish(this);
    }

    /** In welchem Kapitel des Fischbuchs die Art steht. */
    public Chapter chapter() {
        if (this.rarity == Rarity.LEGENDARY) {
            return Chapter.LEGENDS;
        }
        if (this.habitats.contains(Habitat.FAE)) {
            return Chapter.FAE;
        }
        if (this.habitats.contains(Habitat.SHADOW)) {
            return Chapter.SHADOW;
        }
        if (this.habitats.contains(Habitat.ABYSS)) {
            return Chapter.ABYSS;
        }
        if (this.ofLava()) {
            return Chapter.NETHER;
        }
        if (this.habitats.contains(Habitat.END) || this.habitats.contains(Habitat.VOID)) {
            return Chapter.END;
        }
        if (this.habitats.contains(Habitat.FRESH)) {
            return Chapter.RIVERS;
        }
        if (this.habitats.contains(Habitat.SALT) || this.habitats.contains(Habitat.SALT_WARM) || this.habitats.contains(Habitat.SALT_COLD)) {
            return Chapter.SEA;
        }
        return this.habitats.contains(Habitat.DEEP) ? Chapter.DEEP : Chapter.RIVERS;
    }

    /** Alle Arten eines Kapitels, in der Reihenfolge des Buchs. */
    public static List<FishSpecies> of(Chapter chapter) {
        List<FishSpecies> list = new ArrayList<>();
        for (FishSpecies species : values()) {
            if (species.chapter() == chapter) {
                list.add(species);
            }
        }
        return list;
    }

    /** Übersetzungsschlüssel der Notiz im Fischbuch. */
    public String noteKey() {
        return "fishing.kingdomsatwar.note." + this.id;
    }

    /** Beißt in Lava (ihr Gegenstand verbrennt nicht). */
    public boolean ofLava() {
        return this.habitats.stream().anyMatch(Habitat::lava);
    }

    /**
     * Würfelt eine Größe: kleine Fische sind häufig, kapitale selten – wer im Drill nie die Fangzone verliert
     * ({@code perfect}), fängt ein größeres Tier.
     */
    public int rollSize(RandomSource random, boolean perfect) {
        double k = Math.pow(random.nextDouble(), 1.7);
        if (perfect) {
            k = Math.min(1.0, k + 0.12 + random.nextDouble() * 0.1);
        }
        return Mth.clamp((int) Math.round(this.minCm + (this.maxCm - this.minCm) * k), this.minCm, this.maxCm);
    }

    /**
     * Wie wahrscheinlich diese Art hier und jetzt beißt, im Verhältnis zu den anderen (0: gar nicht). Legenden werden nicht
     * danach gewählt, sondern nach {@link #legendChance} – für sie sagt das Gewicht nur, ob sie hier und jetzt überhaupt beißen.
     */
    public float weight(Set<Habitat> here, boolean day, boolean fixedTime, boolean rain, boolean thunder, int luck, Bait bait) {
        boolean home = false;
        boolean special = false;
        for (Habitat habitat : this.habitats) {
            if (here.contains(habitat)) {
                home = true;
                special |= habitat.special();
            }
        }
        if (!home) {
            return 0.0f;
        }
        float w = this.rarity.weight;
        // Arten, die nur an diesem besonderen Ort leben, zeigen sich dort öfter als die Allerweltsfische
        if (special) {
            w *= 1.8f;
        }
        if (!fixedTime && this.time != Time.ANY && (this.time == Time.DAY) != day) {
            w *= 0.12f;
        }
        switch (this.weather) {
            case RAIN -> w *= rain ? 2.4f : 0.5f;
            case RAIN_ONLY -> w *= rain ? 6.0f : 0.0f;
            case THUNDER_ONLY -> w *= thunder ? 10.0f : 0.0f;
            default -> { }
        }
        if (this.rarity.ordinal() >= Rarity.RARE.ordinal()) {
            w *= 1.0f + 0.35f * Math.max(0, luck);
        }
        return w * bait.factor(this);
    }

    /**
     * So wahrscheinlich hängt bei einem Biss eine Legende am Haken, wenn Ort, Zeit und Wetter stimmen: eine von hundert –
     * gleich, wie viele andere Arten dort leben (im Gewimmel der Tiefsee ist der König der Tiefe so selten wie der Drachenkoi
     * unter den drei Arten des Ends).
     */
    public static final float LEGEND_CHANCE = 0.01f;

    /**
     * Die Chance je Biss, dass diese Legende anbeißt (0 für alle anderen Arten und am falschen Ort). Zur falschen Tageszeit
     * fast nie; wer nur bei Regen oder nur im Gewitter beißt, tut es dann dafür öfter (das Wetter hält nicht lange); Glück des
     * Meeres und der passende Köder locken sie.
     */
    public float legendChance(Set<Habitat> here, boolean day, boolean fixedTime, boolean rain, boolean thunder, int luck, Bait bait) {
        if (this.rarity != Rarity.LEGENDARY) {
            return 0.0f;
        }
        boolean home = false;
        for (Habitat habitat : this.habitats) {
            home |= here.contains(habitat);
        }
        if (!home) {
            return 0.0f;
        }
        float chance = LEGEND_CHANCE;
        if (!fixedTime && this.time != Time.ANY && (this.time == Time.DAY) != day) {
            chance *= 0.12f;
        }
        switch (this.weather) {
            case RAIN -> chance *= rain ? 1.5f : 0.75f;
            case RAIN_ONLY -> chance *= rain ? 2.5f : 0.0f;
            case THUNDER_ONLY -> chance *= thunder ? 4.0f : 0.0f;
            default -> { }
        }
        return Math.min(0.25f, chance * (1.0f + 0.35f * Math.max(0, luck)) * bait.factor(this));
    }

    /** Wählt die Art, die anbeißt (null: hier lebt nichts). */
    public static @Nullable FishSpecies pick(Set<Habitat> here, boolean day, boolean fixedTime, boolean rain, boolean thunder, int luck, Bait bait,
            RandomSource random) {
        // erst die Legenden: jede mit ihrer eigenen, festen Chance
        for (FishSpecies species : values()) {
            float chance = species.legendChance(here, day, fixedTime, rain, thunder, luck, bait);
            if (chance > 0.0f && random.nextFloat() < chance) {
                return species;
            }
        }
        // dann alle anderen nach ihrem Gewicht
        float total = 0.0f;
        float[] weights = new float[values().length];
        for (FishSpecies species : values()) {
            if (species.rarity == Rarity.LEGENDARY) {
                continue;
            }
            weights[species.ordinal()] = species.weight(here, day, fixedTime, rain, thunder, luck, bait);
            total += weights[species.ordinal()];
        }
        if (total <= 0.0f) {
            return null;
        }
        float roll = random.nextFloat() * total;
        for (FishSpecies species : values()) {
            roll -= weights[species.ordinal()];
            if (roll < 0.0f) {
                return species;
            }
        }
        return null;
    }

    public static FishSpecies byIndex(int index) {
        return values()[Mth.clamp(index, 0, values().length - 1)];
    }
}
