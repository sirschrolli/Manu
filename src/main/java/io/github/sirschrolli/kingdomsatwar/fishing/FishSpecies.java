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
import com.mojang.serialization.DataResult;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
 * Die Fische des Angelns: <b>1628 Arten</b> (seit 0.83; 1000 seit 0.81). 84 sind von Hand benannt – die Allerweltsfische der
 * Gewässer, die Fische von Minecraft (als ihre eigenen Gegenstände, mit ihrer gewohnten Wirkung) und die Legenden –, 1544
 * gehören einem Biom: zwanzig in jedem eigenen Biom der Mod, zehn oder zwölf in jedem Biom von Minecraft, auch in den Höhlen,
 * in der Lava des Nethers und in der Leere des Ends ({@code tools/fish_gen.py} berechnet sie in zwei Wellen; diese Klasse
 * liest sie beim Laden aus {@code data/kingdomsatwar/fishing/species.tsv}).
 *
 * <p>Jede Art hat eine Seltenheit, eine Größe (was im Fischbuch als Rekord zählt), eine Schwierigkeit und ein Verhalten im
 * Drill (wie sie an der Schnur zieht) und eine Wirkung beim Essen – keine zwei wirken gleich. Wo eine Art beißt, sagt bei
 * den benannten ihr Lebensraum ({@link Habitat#at}: Flüsse und Seen, Meer, Tiefsee, Lava, Leere …), bei den übrigen ihr Biom
 * und worin der Schwimmer treibt. Manche beißen nur nachts oder bei Regen, einzelne nur im Gewitter.
 */
public final class FishSpecies implements StringRepresentable {
    private static final List<FishSpecies> ALL = new ArrayList<>();
    private static final Map<String, FishSpecies> BY_ID = new HashMap<>();
    /** Die Arten je Biom (Kennung wie {@code minecraft:plains}), in der Reihenfolge des Fischbuchs. */
    private static final Map<String, List<FishSpecies>> BY_BIOME = new LinkedHashMap<>();

    // --- die Fische von Minecraft ---
    public static final FishSpecies COD = add(b("cod", COMMON, 30, 90, 0.20f, STEADY).in(Habitat.SALT, Habitat.SALT_COLD).vanilla(() -> Items.COD));
    public static final FishSpecies SALMON = add(b("salmon", COMMON, 40, 110, 0.26f, DARTER).in(Habitat.FRESH, Habitat.FRESH_COLD, Habitat.SALT_COLD).vanilla(() -> Items.SALMON));
    public static final FishSpecies TROPICAL_FISH = add(b("tropical_fish", UNCOMMON, 5, 20, 0.30f, JITTER).in(Habitat.SALT_WARM, Habitat.FRESH_WARM).vanilla(() -> Items.TROPICAL_FISH));
    public static final FishSpecies PUFFERFISH = add(b("pufferfish", UNCOMMON, 10, 40, 0.38f, FLOATER).in(Habitat.SALT_WARM, Habitat.SALT).vanilla(() -> Items.PUFFERFISH));
    // --- Flüsse und Seen ---
    public static final FishSpecies BROOK_TROUT = add(b("brook_trout", COMMON, 20, 45, 0.20f, STEADY).in(Habitat.FRESH).eat(MobEffects.SPEED, 60, 0));
    public static final FishSpecies RIVER_PERCH = add(b("river_perch", COMMON, 15, 40, 0.22f, JITTER).in(Habitat.FRESH).time(Time.DAY).eat(MobEffects.HASTE, 60, 0));
    public static final FishSpecies CARP = add(b("carp", COMMON, 30, 80, 0.28f, CALM).in(Habitat.FRESH, Habitat.SWAMP).eat(MobEffects.REGENERATION, 12, 0));
    public static final FishSpecies PIKE = add(b("pike", UNCOMMON, 45, 120, 0.50f, DARTER).in(Habitat.FRESH).time(Time.DAY).eat(MobEffects.STRENGTH, 45, 0));
    public static final FishSpecies CATFISH = add(b("catfish", UNCOMMON, 60, 200, 0.48f, SINKER).in(Habitat.FRESH, Habitat.SWAMP).time(Time.NIGHT).eat(MobEffects.NIGHT_VISION, 120, 0));
    public static final FishSpecies EEL = add(b("eel", UNCOMMON, 40, 110, 0.45f, JITTER).in(Habitat.FRESH).time(Time.NIGHT).weather(Weather.RAIN).eat(MobEffects.DOLPHINS_GRACE, 45, 0));
    public static final FishSpecies STURGEON = add(b("sturgeon", RARE, 90, 300, 0.62f, SINKER).in(Habitat.FRESH).eat(MobEffects.ABSORPTION, 120, 1));
    public static final FishSpecies GOLDFISH = add(b("goldfish", RARE, 8, 25, 0.40f, FLOATER).in(Habitat.FRESH).time(Time.DAY).eat(MobEffects.LUCK, 300, 0));
    public static final FishSpecies PIRANHA = add(b("piranha", UNCOMMON, 15, 40, 0.52f, DARTER).in(Habitat.FRESH_WARM).eat(MobEffects.STRENGTH, 15, 1).eat(MobEffects.HUNGER, 15, 0));
    public static final FishSpecies ARAPAIMA = add(b("arapaima", RARE, 120, 300, 0.68f, SINKER).in(Habitat.FRESH_WARM).eat(MobEffects.HEALTH_BOOST, 180, 1));
    public static final FishSpecies MUDSKIPPER = add(b("mudskipper", COMMON, 8, 20, 0.18f, JITTER).in(Habitat.SWAMP).eat(MobEffects.JUMP_BOOST, 60, 1));
    public static final FishSpecies ARCTIC_CHAR = add(b("arctic_char", UNCOMMON, 25, 70, 0.40f, STEADY).in(Habitat.FRESH_COLD).eat(MobEffects.RESISTANCE, 60, 0));
    public static final FishSpecies RAINBOW_TROUT = add(b("rainbow_trout", UNCOMMON, 25, 70, 0.42f, DARTER).in(Habitat.MOUNTAIN, Habitat.FRESH).time(Time.DAY).weather(Weather.RAIN)
            .eat(MobEffects.REGENERATION, 8, 1));
    public static final FishSpecies DESERT_PUPFISH = add(b("desert_pupfish", UNCOMMON, 3, 8, 0.30f, JITTER).in(Habitat.DRY).eat(MobEffects.FIRE_RESISTANCE, 45, 0).eat(MobEffects.SPEED, 45, 0));
    public static final FishSpecies CAVE_TETRA = add(b("cave_tetra", UNCOMMON, 6, 12, 0.35f, FLOATER).in(Habitat.CAVE).eat(MobEffects.NIGHT_VISION, 240, 0).eat(MobEffects.HASTE, 30, 0));
    public static final FishSpecies DRIPSTONE_LOACH = add(b("dripstone_loach", COMMON, 8, 18, 0.30f, SINKER).in(Habitat.CAVE).eat(MobEffects.HASTE, 30, 1));
    public static final FishSpecies SCULK_EEL = add(b("sculk_eel", RARE, 60, 150, 0.70f, MIXED).in(Habitat.SCULK).eat(MobEffects.DARKNESS, 5, 0).eat(MobEffects.INVISIBILITY, 60, 0));
    public static final FishSpecies BLOSSOM_KOI = add(b("blossom_koi", RARE, 30, 70, 0.50f, FLOATER).in(Habitat.BLOSSOM).eat(MobEffects.REGENERATION, 30, 0).eat(MobEffects.LUCK, 60, 0));
    public static final FishSpecies GLOOM_PIKE = add(b("gloom_pike", UNCOMMON, 50, 110, 0.55f, DARTER).in(Habitat.DARK).eat(MobEffects.INVISIBILITY, 45, 0));
    public static final FishSpecies SHROOM_PERCH = add(b("shroom_perch", UNCOMMON, 15, 35, 0.40f, JITTER).in(Habitat.MUSHROOM).food(7, 0.9f).eat(MobEffects.NAUSEA, 8, 0));
    // --- das Meer ---
    public static final FishSpecies HERRING = add(b("herring", COMMON, 15, 40, 0.18f, STEADY).in(Habitat.SALT).eat(MobEffects.WATER_BREATHING, 90, 0));
    public static final FishSpecies MACKEREL = add(b("mackerel", COMMON, 20, 50, 0.26f, DARTER).in(Habitat.SALT).time(Time.DAY).eat(MobEffects.SPEED, 20, 1));
    public static final FishSpecies REDFISH = add(b("redfish", COMMON, 25, 60, 0.24f, CALM).in(Habitat.SALT, Habitat.SALT_COLD).eat(MobEffects.ABSORPTION, 60, 0));
    public static final FishSpecies PLAICE = add(b("plaice", UNCOMMON, 25, 70, 0.34f, SINKER).in(Habitat.SALT).eat(MobEffects.RESISTANCE, 90, 0));
    public static final FishSpecies TUNA = add(b("tuna", UNCOMMON, 80, 250, 0.58f, DARTER).in(Habitat.SALT).time(Time.DAY).eat(MobEffects.STRENGTH, 90, 0).eat(MobEffects.SPEED, 90, 0));
    public static final FishSpecies SWORDFISH = add(b("swordfish", RARE, 150, 400, 0.72f, DARTER).in(Habitat.SALT).eat(MobEffects.STRENGTH, 30, 1));
    public static final FishSpecies SUNFISH = add(b("sunfish", RARE, 100, 300, 0.50f, FLOATER).in(Habitat.SALT).time(Time.DAY).eat(MobEffects.SLOW_FALLING, 90, 0));
    public static final FishSpecies SEAHORSE = add(b("seahorse", UNCOMMON, 5, 25, 0.36f, FLOATER).in(Habitat.SALT_WARM).eat(MobEffects.DOLPHINS_GRACE, 90, 0));
    public static final FishSpecies PARROTFISH = add(b("parrotfish", UNCOMMON, 25, 90, 0.38f, STEADY).in(Habitat.SALT_WARM).time(Time.DAY).eat(MobEffects.JUMP_BOOST, 120, 0)
            .eat(MobEffects.SPEED, 120, 0));
    public static final FishSpecies LIONFISH = add(b("lionfish", RARE, 20, 45, 0.55f, JITTER).in(Habitat.SALT_WARM).eat(MobEffects.POISON, 6, 1).eat(MobEffects.STRENGTH, 60, 1));
    public static final FishSpecies OCTOPUS = add(b("octopus", UNCOMMON, 30, 120, 0.50f, MIXED).in(Habitat.SALT).time(Time.NIGHT).eat(MobEffects.INVISIBILITY, 30, 0)
            .eat(MobEffects.WATER_BREATHING, 30, 0));
    public static final FishSpecies CATSHARK = add(b("catshark", RARE, 50, 160, 0.66f, DARTER).in(Habitat.SALT, Habitat.DEEP).time(Time.NIGHT).eat(MobEffects.STRENGTH, 60, 0)
            .eat(MobEffects.NIGHT_VISION, 60, 0));
    public static final FishSpecies HALIBUT = add(b("halibut", RARE, 80, 250, 0.60f, SINKER).in(Habitat.SALT_COLD).eat(MobEffects.RESISTANCE, 30, 1));
    // --- die Tiefsee ---
    public static final FishSpecies LANTERNFISH = add(b("lanternfish", COMMON, 5, 15, 0.25f, JITTER).in(Habitat.DEEP).eat(MobEffects.GLOWING, 30, 0).eat(MobEffects.NIGHT_VISION, 60, 0));
    public static final FishSpecies HATCHETFISH = add(b("hatchetfish", COMMON, 4, 12, 0.28f, FLOATER).in(Habitat.DEEP).eat(MobEffects.SPEED, 30, 0).eat(MobEffects.SLOW_FALLING, 30, 0));
    public static final FishSpecies ANGLERFISH = add(b("anglerfish", UNCOMMON, 20, 100, 0.52f, SINKER).in(Habitat.DEEP).time(Time.NIGHT).eat(MobEffects.NIGHT_VISION, 300, 0));
    public static final FishSpecies VIPERFISH = add(b("viperfish", UNCOMMON, 20, 60, 0.56f, DARTER).in(Habitat.DEEP).eat(MobEffects.STRENGTH, 20, 1).eat(MobEffects.WITHER, 4, 0));
    public static final FishSpecies FIREFLY_SQUID = add(b("firefly_squid", UNCOMMON, 5, 12, 0.40f, MIXED).in(Habitat.DEEP).time(Time.NIGHT).eat(MobEffects.GLOWING, 60, 0)
            .eat(MobEffects.SPEED, 30, 1));
    public static final FishSpecies GIANT_ISOPOD = add(b("giant_isopod", UNCOMMON, 15, 50, 0.42f, SINKER).in(Habitat.DEEP).eat(MobEffects.RESISTANCE, 60, 1).eat(MobEffects.SLOWNESS, 60, 0));
    public static final FishSpecies OARFISH = add(b("oarfish", RARE, 300, 800, 0.70f, MIXED).in(Habitat.DEEP).eat(MobEffects.CONDUIT_POWER, 60, 0));
    public static final FishSpecies GHOST_SHARK = add(b("ghost_shark", RARE, 60, 150, 0.66f, DARTER).in(Habitat.DEEP).eat(MobEffects.INVISIBILITY, 90, 0));
    public static final FishSpecies COELACANTH = add(b("coelacanth", EPIC, 100, 200, 0.78f, SINKER).in(Habitat.DEEP).eat(MobEffects.HEALTH_BOOST, 300, 2));
    public static final FishSpecies VAMPIRE_SQUID = add(b("vampire_squid", EPIC, 15, 30, 0.80f, MIXED).in(Habitat.DEEP).time(Time.NIGHT).eat(MobEffects.INSTANT_HEALTH, 0, 1)
            .eat(MobEffects.DARKNESS, 6, 0));
    // --- die Lava des Nethers ---
    public static final FishSpecies MAGMA_CARP = add(b("magma_carp", COMMON, 30, 70, 0.40f, CALM).in(Habitat.LAVA).eat(MobEffects.FIRE_RESISTANCE, 120, 0));
    public static final FishSpecies CRIMSON_SNAPPER = add(b("crimson_snapper", UNCOMMON, 25, 60, 0.50f, DARTER).in(Habitat.LAVA_CRIMSON).eat(MobEffects.STRENGTH, 120, 0)
            .eat(MobEffects.FIRE_RESISTANCE, 30, 0));
    public static final FishSpecies WARP_FIN = add(b("warp_fin", UNCOMMON, 20, 50, 0.55f, MIXED).in(Habitat.LAVA_WARPED).special(Special.TELEPORT_NEAR).eat(MobEffects.FIRE_RESISTANCE, 30, 0));
    public static final FishSpecies SOUL_EEL = add(b("soul_eel", RARE, 60, 160, 0.65f, JITTER).in(Habitat.LAVA_SOUL).eat(MobEffects.SPEED, 15, 2).eat(MobEffects.WITHER, 3, 0));
    public static final FishSpecies BASALT_PIKE = add(b("basalt_pike", UNCOMMON, 50, 120, 0.60f, DARTER).in(Habitat.LAVA_BASALT).eat(MobEffects.RESISTANCE, 120, 0)
            .eat(MobEffects.FIRE_RESISTANCE, 60, 0));
    public static final FishSpecies EMBER_RAY = add(b("ember_ray", RARE, 60, 140, 0.70f, FLOATER).in(Habitat.LAVA).eat(MobEffects.FIRE_RESISTANCE, 300, 0).eat(MobEffects.REGENERATION, 20, 0));
    // --- die Leere des Ends und der Leere ---
    public static final FishSpecies ENDER_SALMON = add(b("ender_salmon", COMMON, 30, 70, 0.40f, DARTER).in(Habitat.END).special(Special.TELEPORT_FAR));
    public static final FishSpecies CHORUS_FISH = add(b("chorus_fish", UNCOMMON, 15, 40, 0.45f, FLOATER).in(Habitat.END).eat(MobEffects.LEVITATION, 4, 0).eat(MobEffects.SLOW_FALLING, 20, 0));
    public static final FishSpecies VOID_RAY = add(b("void_ray", RARE, 80, 200, 0.70f, FLOATER).in(Habitat.END, Habitat.VOID).eat(MobEffects.SLOW_FALLING, 180, 0));
    public static final FishSpecies STARDUST_MINNOW = add(b("stardust_minnow", COMMON, 5, 12, 0.30f, JITTER).in(Habitat.VOID).eat(MobEffects.LUCK, 60, 1));
    public static final FishSpecies CRYSTAL_FIN = add(b("crystal_fin", UNCOMMON, 20, 50, 0.50f, STEADY).in(Habitat.VOID).eat(MobEffects.ABSORPTION, 90, 2));
    // --- Feenreich, Schattenwelt, Abgrund ---
    public static final FishSpecies GLIMMERFISH = add(b("glimmerfish", COMMON, 8, 20, 0.24f, FLOATER).in(Habitat.FAE).eat(MobEffects.GLOWING, 20, 0).eat(MobEffects.LUCK, 120, 0));
    public static final FishSpecies MUSHROOM_SWIMMER = add(b("mushroom_swimmer", UNCOMMON, 10, 30, 0.38f, JITTER).in(Habitat.FAE).eat(MobEffects.JUMP_BOOST, 30, 2));
    public static final FishSpecies MOONLILY_KOI = add(b("moonlily_koi", RARE, 30, 80, 0.56f, STEADY).in(Habitat.FAE).eat(MobEffects.REGENERATION, 15, 1).eat(MobEffects.NIGHT_VISION, 60, 0));
    public static final FishSpecies THORN_PIKE = add(b("thorn_pike", RARE, 40, 100, 0.64f, DARTER).in(Habitat.FAE).eat(MobEffects.STRENGTH, 120, 0).eat(MobEffects.POISON, 5, 0));
    public static final FishSpecies MIST_CARP = add(b("mist_carp", COMMON, 25, 60, 0.30f, CALM).in(Habitat.SHADOW).eat(MobEffects.SLOW_FALLING, 60, 0).eat(MobEffects.INVISIBILITY, 20, 0));
    public static final FishSpecies BLIND_PIKE = add(b("blind_pike", UNCOMMON, 40, 100, 0.52f, DARTER).in(Habitat.SHADOW).eat(MobEffects.BLINDNESS, 5, 0).eat(MobEffects.STRENGTH, 20, 2));
    public static final FishSpecies SHADOW_EEL = add(b("shadow_eel", UNCOMMON, 50, 140, 0.52f, JITTER).in(Habitat.SHADOW).eat(MobEffects.INVISIBILITY, 60, 0).eat(MobEffects.SPEED, 60, 0));
    public static final FishSpecies SOULFISH = add(b("soulfish", RARE, 20, 50, 0.60f, FLOATER).in(Habitat.SHADOW).special(Special.CLEANSE).eat(MobEffects.REGENERATION, 10, 0));
    public static final FishSpecies EMBER_CATFISH = add(b("ember_catfish", UNCOMMON, 50, 130, 0.50f, SINKER).in(Habitat.ABYSS).eat(MobEffects.FIRE_RESISTANCE, 180, 0)
            .eat(MobEffects.STRENGTH, 60, 0));
    public static final FishSpecies CHAOS_FIN = add(b("chaos_fin", RARE, 30, 90, 0.66f, MIXED).in(Habitat.ABYSS).special(Special.TELEPORT_FAR).eat(MobEffects.SPEED, 20, 1));
    public static final FishSpecies RIFT_EEL = add(b("rift_eel", EPIC, 80, 220, 0.80f, DARTER).in(Habitat.ABYSS).eat(MobEffects.LEVITATION, 6, 0).eat(MobEffects.SLOW_FALLING, 30, 0)
            .eat(MobEffects.STRENGTH, 60, 1));
    // --- Legenden (je Welt eine; sehr selten, sehr schwer) ---
    public static final FishSpecies OLD_ONE = add(b("old_one", LEGENDARY, 140, 220, 0.90f, SINKER).in(Habitat.FRESH).time(Time.NIGHT).eat(MobEffects.LUCK, 600, 1)
            .eat(MobEffects.REGENERATION, 60, 0).eat(MobEffects.HEALTH_BOOST, 600, 1));
    public static final FishSpecies GOLDSCALE = add(b("goldscale", LEGENDARY, 80, 130, 0.90f, DARTER).in(Habitat.FRESH).time(Time.DAY).weather(Weather.RAIN_ONLY)
            .eat(MobEffects.ABSORPTION, 180, 3).eat(MobEffects.LUCK, 300, 2));
    public static final FishSpecies STORMSAIL = add(b("stormsail", LEGENDARY, 250, 450, 0.94f, DARTER).in(Habitat.SALT).weather(Weather.THUNDER_ONLY).eat(MobEffects.SPEED, 120, 2)
            .eat(MobEffects.STRENGTH, 120, 1));
    public static final FishSpecies DEEP_KING = add(b("deep_king", LEGENDARY, 120, 200, 0.94f, SINKER).in(Habitat.DEEP).time(Time.NIGHT).eat(MobEffects.NIGHT_VISION, 900, 0)
            .eat(MobEffects.STRENGTH, 120, 2).eat(MobEffects.RESISTANCE, 120, 1));
    public static final FishSpecies INFERNO_KING = add(b("inferno_king", LEGENDARY, 200, 350, 0.95f, SINKER).in(Habitat.LAVA).eat(MobEffects.FIRE_RESISTANCE, 600, 0)
            .eat(MobEffects.STRENGTH, 60, 2).eat(MobEffects.ABSORPTION, 120, 3));
    public static final FishSpecies DRAGON_KOI = add(b("dragon_koi", LEGENDARY, 90, 160, 0.94f, MIXED).in(Habitat.END).eat(MobEffects.STRENGTH, 90, 2).eat(MobEffects.RESISTANCE, 90, 1)
            .eat(MobEffects.REGENERATION, 30, 1));
    public static final FishSpecies MOONSHINE_KOI = add(b("moonshine_koi", LEGENDARY, 60, 110, 0.90f, FLOATER).in(Habitat.FAE).eat(MobEffects.REGENERATION, 30, 2)
            .eat(MobEffects.HEALTH_BOOST, 600, 2));

    // --- neue Legenden (seit 0.81) ---
    public static final FishSpecies NIGHT_SOVEREIGN = add(b("night_sovereign", LEGENDARY, 160, 260, 0.94f, SINKER).in(Habitat.SHADOW)
            .eat(MobEffects.INVISIBILITY, 300, 0).eat(MobEffects.STRENGTH, 120, 2).eat(MobEffects.SPEED, 120, 1));
    public static final FishSpecies RIFT_LEVIATHAN = add(b("rift_leviathan", LEGENDARY, 300, 520, 0.96f, MIXED).in(Habitat.ABYSS)
            .eat(MobEffects.STRENGTH, 90, 2).eat(MobEffects.FIRE_RESISTANCE, 600, 0).eat(MobEffects.SLOW_FALLING, 300, 0));
    public static final FishSpecies STAR_WHALE = add(b("star_whale", LEGENDARY, 400, 700, 0.93f, FLOATER).in(Habitat.VOID)
            .eat(MobEffects.SLOW_FALLING, 600, 0).eat(MobEffects.LUCK, 300, 2).eat(MobEffects.ABSORPTION, 180, 3));
    public static final FishSpecies LIGHTLESS_ONE = add(b("lightless_one", LEGENDARY, 90, 160, 0.92f, SINKER).in(Habitat.CAVE)
            .eat(MobEffects.NIGHT_VISION, 1200, 0).eat(MobEffects.HASTE, 180, 2).eat(MobEffects.RESISTANCE, 120, 1));
    public static final FishSpecies FROST_EMPEROR = add(b("frost_emperor", LEGENDARY, 180, 320, 0.93f, SINKER).in(Habitat.FRESH_COLD, Habitat.SALT_COLD)
            .eat(MobEffects.RESISTANCE, 180, 1).eat(MobEffects.ABSORPTION, 240, 2).eat(MobEffects.WATER_BREATHING, 600, 0));
    public static final FishSpecies REEF_QUEEN = add(b("reef_queen", LEGENDARY, 60, 120, 0.92f, JITTER).in(Habitat.SALT_WARM).time(Time.DAY)
            .eat(MobEffects.WATER_BREATHING, 900, 0).eat(MobEffects.DOLPHINS_GRACE, 300, 0).eat(MobEffects.REGENERATION, 30, 1));
    public static final FishSpecies BOG_MOTHER = add(b("bog_mother", LEGENDARY, 150, 260, 0.92f, SINKER).in(Habitat.SWAMP).time(Time.NIGHT)
            .special(Special.CLEANSE).eat(MobEffects.HEALTH_BOOST, 600, 2).eat(MobEffects.RESISTANCE, 180, 0));
    public static final FishSpecies SUN_KING = add(b("sun_king", LEGENDARY, 80, 150, 0.92f, DARTER).in(Habitat.DRY).time(Time.DAY)
            .eat(MobEffects.FIRE_RESISTANCE, 900, 0).eat(MobEffects.SPEED, 180, 1).eat(MobEffects.HASTE, 180, 1));

    /** So viele Arten sind von Hand benannt (sie stehen vorn; danach folgen die Arten der Biome). */
    public static final int NAMED = ALL.size();
    private static final List<FishSpecies> VALUES;
    private static final List<FishSpecies> GENERAL;
    private static final List<FishSpecies> LEGENDS;

    static {
        loadBiomeSpecies();
        VALUES = List.copyOf(ALL);
        GENERAL = List.copyOf(ALL.subList(0, NAMED));
        LEGENDS = VALUES.stream().filter(species -> species.rarity == Rarity.LEGENDARY).toList();
    }

    public static final Codec<FishSpecies> CODEC = Codec.STRING.comapFlatMap(id -> {
        FishSpecies species = BY_ID.get(id);
        return species == null ? DataResult.error(() -> "Unbekannte Fischart: " + id) : DataResult.success(species);
    }, FishSpecies::id);

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
        RIVERS, SEA, DEEP, CAVES, NETHER, END, FAE, SHADOW, ABYSS, LEGENDS;

        public String key() {
            return "fishing.kingdomsatwar.chapter." + this.name().toLowerCase(java.util.Locale.ROOT);
        }
    }

    /** Eine Wirkung beim Essen. */
    public record Bite(Holder<MobEffect> effect, int seconds, int amplifier) {}

    private final int index;
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
    /** Das Biom, dem die Art gehört ({@code null} bei den von Hand benannten: Sie leben in ihren Lebensräumen). */
    private final @Nullable String biome;
    private final Set<Medium> media;
    private final @Nullable Chapter chapter;

    private FishSpecies(int index, Builder b) {
        this.index = index;
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
        this.biome = b.biome;
        this.media = b.media;
        this.chapter = b.chapter;
    }

    private static FishSpecies add(Builder b) {
        FishSpecies species = new FishSpecies(ALL.size(), b);
        if (BY_ID.put(species.id, species) != null) {
            throw new IllegalStateException("Fischart doppelt: " + species.id);
        }
        ALL.add(species);
        if (species.biome != null) {
            BY_BIOME.computeIfAbsent(species.biome, key -> new ArrayList<>()).add(species);
        }
        return species;
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
        @Nullable String biome;
        Set<Medium> media = EnumSet.noneOf(Medium.class);
        @Nullable Chapter chapter;

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

    // =====================================================================================
    // Die Arten der Biome
    // =====================================================================================

    private static final String TABLE = "/data/kingdomsatwar/fishing/species.tsv";

    private static void loadBiomeSpecies() {
        Map<String, Holder<MobEffect>> effects = effectNames();
        try (InputStream in = FishSpecies.class.getResourceAsStream(TABLE)) {
            if (in == null) {
                throw new IllegalStateException("Die Tabelle der Fischarten fehlt: " + TABLE);
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] f = line.split("\t", -1);
                if (f.length < 13) {
                    throw new IllegalStateException("Zeile der Fischarten unvollständig: " + line);
                }
                Builder b = b(f[0], Rarity.valueOf(f[1]), Integer.parseInt(f[2]), Integer.parseInt(f[3]), Float.parseFloat(f[4]), Behavior.valueOf(f[5]));
                b.biome = f[6];
                for (char c : f[7].toCharArray()) {
                    b.media.add(switch (c) {
                        case 'W' -> Medium.WATER;
                        case 'L' -> Medium.LAVA;
                        case 'V' -> Medium.VOID;
                        default -> throw new IllegalStateException("Unbekanntes Medium: " + line);
                    });
                }
                b.chapter = Chapter.valueOf(f[8]);
                b.time(Time.valueOf(f[9])).weather(Weather.valueOf(f[10])).special(Special.valueOf(f[11]));
                for (String part : f[12].split(",")) {
                    String[] e = part.split(":");
                    Holder<MobEffect> effect = effects.get(e[0]);
                    if (effect == null) {
                        throw new IllegalStateException("Unbekannte Wirkung: " + line);
                    }
                    b.eat(effect, Integer.parseInt(e[1]), Integer.parseInt(e[2]));
                }
                add(b);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Map<String, Holder<MobEffect>> effectNames() {
        Map<String, Holder<MobEffect>> map = new HashMap<>();
        map.put("speed", MobEffects.SPEED);
        map.put("haste", MobEffects.HASTE);
        map.put("strength", MobEffects.STRENGTH);
        map.put("jump_boost", MobEffects.JUMP_BOOST);
        map.put("regeneration", MobEffects.REGENERATION);
        map.put("resistance", MobEffects.RESISTANCE);
        map.put("fire_resistance", MobEffects.FIRE_RESISTANCE);
        map.put("water_breathing", MobEffects.WATER_BREATHING);
        map.put("invisibility", MobEffects.INVISIBILITY);
        map.put("night_vision", MobEffects.NIGHT_VISION);
        map.put("health_boost", MobEffects.HEALTH_BOOST);
        map.put("absorption", MobEffects.ABSORPTION);
        map.put("luck", MobEffects.LUCK);
        map.put("slow_falling", MobEffects.SLOW_FALLING);
        map.put("conduit_power", MobEffects.CONDUIT_POWER);
        map.put("dolphins_grace", MobEffects.DOLPHINS_GRACE);
        map.put("instant_health", MobEffects.INSTANT_HEALTH);
        map.put("glowing", MobEffects.GLOWING);
        map.put("levitation", MobEffects.LEVITATION);
        map.put("poison", MobEffects.POISON);
        map.put("wither", MobEffects.WITHER);
        map.put("hunger", MobEffects.HUNGER);
        map.put("nausea", MobEffects.NAUSEA);
        map.put("blindness", MobEffects.BLINDNESS);
        map.put("darkness", MobEffects.DARKNESS);
        map.put("slowness", MobEffects.SLOWNESS);
        map.put("weakness", MobEffects.WEAKNESS);
        map.put("mining_fatigue", MobEffects.MINING_FATIGUE);
        return map;
    }

    // =====================================================================================
    // Alle Arten
    // =====================================================================================

    /** Alle Arten: erst die von Hand benannten, dann die der Biome. */
    public static List<FishSpecies> values() {
        return VALUES;
    }

    public static int count() {
        return VALUES.size();
    }

    public static FishSpecies byIndex(int index) {
        return VALUES.get(Mth.clamp(index, 0, VALUES.size() - 1));
    }

    public static @Nullable FishSpecies byId(String id) {
        return BY_ID.get(id);
    }

    /** Die Biome, die eigene Arten haben (in der Reihenfolge der Tabelle). */
    public static Set<String> biomes() {
        return java.util.Collections.unmodifiableSet(BY_BIOME.keySet());
    }

    /** Die Arten, die nur in diesem Biom leben. */
    public static List<FishSpecies> ofBiome(String biome) {
        List<FishSpecies> list = BY_BIOME.get(biome);
        return list == null ? List.of() : java.util.Collections.unmodifiableList(list);
    }

    /** Die Legenden aller Welten. */
    public static List<FishSpecies> legends() {
        return LEGENDS;
    }

    /** Die Stelle in {@link #values()} (auch im Netz: Server und Spiel haben dieselbe Liste). */
    public int ordinal() {
        return this.index;
    }

    public String id() {
        return this.id;
    }

    @Override
    public String getSerializedName() {
        return this.id;
    }

    @Override
    public String toString() {
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

    /** Das Biom, in dem allein die Art lebt – {@code null} bei den Arten, die nach Lebensräumen verteilt sind. */
    public @Nullable String biome() {
        return this.biome;
    }

    /** Worin die Art eines Bioms beißt (Wasser, Lava, Leere); leer bei den Arten der Lebensräume. */
    public Set<Medium> media() {
        return this.media;
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
        if (this.chapter != null) {
            return this.chapter;
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
        if (this.habitats.contains(Habitat.CAVE) || this.habitats.contains(Habitat.SCULK)) {
            return Chapter.CAVES;
        }
        return this.habitats.contains(Habitat.DEEP) ? Chapter.DEEP : Chapter.RIVERS;
    }

    /** Alle Arten eines Kapitels, in der Reihenfolge des Buchs. */
    public static List<FishSpecies> of(Chapter chapter) {
        List<FishSpecies> list = new ArrayList<>();
        for (FishSpecies species : VALUES) {
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
        return this.media.contains(Medium.LAVA) || this.habitats.stream().anyMatch(Habitat::lava);
    }

    /** Lockt der Köderwurm sie an (die häufigen Fische der Flüsse und Seen)? */
    public boolean likesWorm() {
        boolean fresh = this.biome != null ? this.chapter == Chapter.RIVERS && this.media.contains(Medium.WATER) : this.habitats.contains(Habitat.FRESH);
        return fresh && this.rarity.ordinal() <= Rarity.UNCOMMON.ordinal();
    }

    /** Lockt der Glühköder sie an (was im Dunkeln lebt: Nachtfische, Tiefsee, Höhlen, Lava)? */
    public boolean likesGlow() {
        return this.time == Time.NIGHT || this.ofLava() || this.chapter == Chapter.DEEP || this.chapter == Chapter.CAVES
                || this.habitats.contains(Habitat.DEEP) || this.habitats.contains(Habitat.CAVE) || this.habitats.contains(Habitat.SCULK);
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
     * Ein Angelplatz: die Lebensräume dort ({@link Habitat#at}), das Biom und worin der Schwimmer treibt.
     */
    public record Spot(Set<Habitat> habitats, String biome, Medium medium) {
        /** Nur Lebensräume – ein Platz in keinem bestimmten Biom (für Prüfungen der Allerweltsfische). */
        public static Spot of(Set<Habitat> habitats) {
            return new Spot(habitats, "", Medium.WATER);
        }
    }

    /** Lebt die Art an diesem Platz? 0: nein · 1: ja · 2: ja, und es ist ihr besonderer Ort (dort zeigt sie sich öfter). */
    private int home(Spot spot) {
        if (this.biome != null) {
            return this.biome.equals(spot.biome()) && this.media.contains(spot.medium()) ? 2 : 0;
        }
        int home = 0;
        for (Habitat habitat : this.habitats) {
            if (spot.habitats().contains(habitat)) {
                home = Math.max(home, habitat.special() ? 2 : 1);
            }
        }
        return home;
    }

    /**
     * Wie wahrscheinlich diese Art hier und jetzt beißt, im Verhältnis zu den anderen (0: gar nicht). Legenden werden nicht
     * danach gewählt, sondern nach {@link #legendChance} – für sie sagt das Gewicht nur, ob sie hier und jetzt überhaupt beißen.
     */
    public float weight(Spot spot, boolean day, boolean fixedTime, boolean rain, boolean thunder, int luck, Bait bait) {
        int home = this.home(spot);
        if (home == 0) {
            return 0.0f;
        }
        float w = this.rarity.weight;
        // Arten, die nur an diesem besonderen Ort oder in diesem Biom leben, zeigen sich dort öfter als die Allerweltsfische
        if (home == 2) {
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
     * unter den Arten des Ends).
     */
    public static final float LEGEND_CHANCE = 0.01f;

    /**
     * Die Chance je Biss, dass diese Legende anbeißt (0 für alle anderen Arten und am falschen Ort). Zur falschen Tageszeit
     * fast nie; wer nur bei Regen oder nur im Gewitter beißt, tut es dann dafür öfter (das Wetter hält nicht lange); Glück des
     * Meeres und der passende Köder locken sie.
     */
    public float legendChance(Spot spot, boolean day, boolean fixedTime, boolean rain, boolean thunder, int luck, Bait bait) {
        if (this.rarity != Rarity.LEGENDARY || this.home(spot) == 0) {
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

    /** Die Arten, die an diesem Platz überhaupt leben können: die Allerweltsfische und die des Bioms. */
    public static List<FishSpecies> candidates(Spot spot) {
        List<FishSpecies> local = BY_BIOME.get(spot.biome());
        if (local == null) {
            return GENERAL;
        }
        List<FishSpecies> list = new ArrayList<>(GENERAL.size() + local.size());
        list.addAll(GENERAL);
        list.addAll(local);
        return list;
    }

    /** Wählt die Art, die anbeißt (null: hier lebt nichts). */
    public static @Nullable FishSpecies pick(Spot spot, boolean day, boolean fixedTime, boolean rain, boolean thunder, int luck, Bait bait,
            RandomSource random) {
        List<FishSpecies> candidates = candidates(spot);
        // erst die Legenden: jede mit ihrer eigenen, festen Chance
        for (FishSpecies species : candidates) {
            float chance = species.legendChance(spot, day, fixedTime, rain, thunder, luck, bait);
            if (chance > 0.0f && random.nextFloat() < chance) {
                return species;
            }
        }
        // dann alle anderen nach ihrem Gewicht
        float total = 0.0f;
        float[] weights = new float[candidates.size()];
        for (int i = 0; i < weights.length; i++) {
            FishSpecies species = candidates.get(i);
            if (species.rarity != Rarity.LEGENDARY) {
                weights[i] = species.weight(spot, day, fixedTime, rain, thunder, luck, bait);
                total += weights[i];
            }
        }
        if (total <= 0.0f) {
            return null;
        }
        float roll = random.nextFloat() * total;
        for (int i = 0; i < weights.length; i++) {
            roll -= weights[i];
            if (roll < 0.0f && weights[i] > 0.0f) {
                return candidates.get(i);
            }
        }
        return null;
    }
}
