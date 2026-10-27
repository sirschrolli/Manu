package io.github.sirschrolli.kingdomsatwar.worldgen.overworld;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.material.VanillaMaterialConditions;
import net.minecraft.data.worldgen.material.VanillaMaterialRules;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.material.MaterialRules;
import net.minecraft.world.level.levelgen.material.condition.MaterialCondition;
import net.minecraft.world.level.levelgen.material.rule.MaterialRule;

/**
 * Der Boden der eigenen Oberwelt-Biome. Die Regeln stehen in {@code kingdomsatwar:overworld/surface} und werden in der
 * Vanilla-Regel {@code minecraft:overworld} vor die Vanilla-Oberflächen gesetzt; für alle übrigen Biome bleibt alles,
 * wie es war (die Königsauen haben ganz gewöhnlichen Gras- und Erdboden).
 *
 * <ul>
 *     <li><b>Aschefelder</b> – Asche, dazwischen Flecken dunkler Aschenerde und Buckel aus Vulkangestein; darunter
 *     Aschenerde und eine dicke Schicht Vulkangestein.</li>
 *     <li><b>Frostgipfel</b> – Schnee, Felder aus Packeis, Spalten aus Gletschereis, Pulverschneelöcher und Frosterde;
 *     steile Wände aus Raureifstein.</li>
 *     <li><b>Sturmküste</b> – Sturmstein mit Kiesstreifen, auf den höheren Kuppen Gras.</li>
 *     <li><b>Urhain</b> – Gras mit Podsol, Moospolstern, Wildwechseln aus grober Erde und vereinzelt Leuchtmoos.</li>
 *     <li><b>Felsschluchten</b> – Felswände aus gebänderten Gesteinsschichten (Tuff, Schiefer, Stein, Kalzit, Andesit,
 *     Terrakotta, Tropfstein), oben trockenes Gras, grobe Erde und Geröllhalden.</li>
 *     <li><b>Korallenküste</b> – Sand über Sandstein, auch unter Wasser.</li>
 *     <li><b>Nebelmoor</b> – Gras, Torf, Schlamm und Moos, Pfützen auf Höhe des Meeresspiegels.</li>
 * </ul>
 */
public final class OverworldSurface {
    public static final ResourceKey<MaterialRule> SURFACE = key("overworld/surface");

    /** Die Vanilla-Regel der Oberwelt, die überschrieben wird (Standard, Große Biome, Zerklüftet nutzen sie). */
    public static final ResourceKey<MaterialRule> VANILLA_OVERWORLD = vanilla("overworld");
    private static final ResourceKey<MaterialRule> VANILLA_SURFACE = vanilla("overworld/surface");
    private static final ResourceKey<MaterialRule> VANILLA_UNDERGROUND = vanilla("overworld/underground");
    private static final ResourceKey<MaterialRule> VANILLA_COPPER_VEIN = vanilla("overworld/copper_ore_vein");
    private static final ResourceKey<MaterialRule> VANILLA_IRON_VEIN = vanilla("overworld/iron_ore_vein");

    private OverworldSurface() {}

    private static ResourceKey<MaterialRule> key(String name) {
        return ResourceKey.create(Registries.MATERIAL_RULE, KingdomsAtWar.id(name));
    }

    private static ResourceKey<MaterialRule> vanilla(String name) {
        return ResourceKey.create(Registries.MATERIAL_RULE, Identifier.withDefaultNamespace(name));
    }

    private static MaterialRule state(Block block) {
        return MaterialRules.state(block.defaultBlockState());
    }

    /** Die Vanilla-Oberwelt mit den eigenen Oberflächen davor (im Namensraum {@code minecraft}). */
    public static void bootstrapVanillaOverride(BootstrapContext<MaterialRule> context) {
        HolderGetter<MaterialRule> rules = context.lookup(Registries.MATERIAL_RULE);
        context.register(VANILLA_OVERWORLD, MaterialRules.sequence(
                MaterialRules.getRule(rules, VanillaMaterialRules.BEDROCK_FLOOR),
                MaterialRules.getRule(rules, VANILLA_COPPER_VEIN),
                MaterialRules.getRule(rules, VANILLA_IRON_VEIN),
                MaterialRules.ifTrue(MaterialRules.abovePreliminarySurface(), MaterialRules.sequence(
                        MaterialRules.getRule(rules, SURFACE),
                        MaterialRules.getRule(rules, VANILLA_SURFACE))),
                MaterialRules.getRule(rules, VANILLA_UNDERGROUND)));
    }

    /** Die Oberflächen der eigenen Biome. */
    public static void bootstrap(BootstrapContext<MaterialRule> context) {
        new Builder(context).register();
    }

    private static final class Builder {
        private final BootstrapContext<MaterialRule> context;
        private final HolderGetter<Biome> biomes;
        private final MaterialCondition onFloor;
        private final MaterialCondition underFloor;
        private final MaterialCondition deepUnderFloor;
        private final MaterialCondition veryDeepUnderFloor;
        private final MaterialCondition onCeiling;
        private final MaterialCondition notUnderwater;
        private final MaterialCondition notUnderDeepWater;
        private final MaterialCondition steep = MaterialRules.steep();

        Builder(BootstrapContext<MaterialRule> context) {
            this.context = context;
            this.biomes = context.lookup(Registries.BIOME);
            HolderGetter<MaterialCondition> conditions = context.lookup(Registries.MATERIAL_CONDITION);
            this.onFloor = MaterialRules.getCondition(conditions, VanillaMaterialConditions.ON_FLOOR);
            this.underFloor = MaterialRules.getCondition(conditions, VanillaMaterialConditions.UNDER_FLOOR);
            this.deepUnderFloor = MaterialRules.getCondition(conditions, VanillaMaterialConditions.DEEP_UNDER_FLOOR);
            this.veryDeepUnderFloor = MaterialRules.getCondition(conditions, VanillaMaterialConditions.VERY_DEEP_UNDER_FLOOR);
            this.onCeiling = MaterialRules.getCondition(conditions, VanillaMaterialConditions.ON_CEILING);
            this.notUnderwater = MaterialRules.getCondition(conditions, VanillaMaterialConditions.NOT_UNDERWATER);
            this.notUnderDeepWater = MaterialRules.getCondition(conditions, VanillaMaterialConditions.NOT_UNDER_DEEP_WATER);
        }

        private MaterialRule biome(String name, ResourceKey<Biome> biome, MaterialRule rule) {
            return MaterialRules.ifTrue(MaterialRules.isBiome(this.biomes, biome),
                    MaterialRules.registerAndWrap(this.context, key("overworld/surface/" + name), rule));
        }

        /** Rieselndes Material, unter dem Luft ist (Überhang), wird durch {@code solid} ersetzt. */
        private MaterialRule falling(MaterialRule loose, MaterialRule solid) {
            return MaterialRules.sequence(MaterialRules.ifTrue(this.onCeiling, solid), loose);
        }

        void register() {
            this.context.register(SURFACE, MaterialRules.sequence(
                    this.biome("ash_fields", OverworldBiomes.ASH_FIELDS, this.ashFields()),
                    this.biome("frost_peaks", OverworldBiomes.FROST_PEAKS, this.frostPeaks()),
                    this.biome("storm_coast", OverworldBiomes.STORM_COAST, this.stormCoast()),
                    this.biome("primeval_forest", OverworldBiomes.PRIMEVAL_FOREST, this.primevalForest()),
                    this.biome("rock_gorges", OverworldBiomes.ROCK_GORGES, this.rockGorges()),
                    this.biome("coral_coast", OverworldBiomes.CORAL_COAST, this.coralCoast()),
                    this.biome("mist_moor", OverworldBiomes.MIST_MOOR, this.mistMoor())));
        }

        private MaterialRule ashFields() {
            MaterialRule volcanic = state(ModBlocks.VOLCANIC_ROCK.base().get());
            MaterialRule cinder = state(ModBlocks.CINDER_SOIL.get());
            MaterialRule ash = this.falling(state(ModBlocks.ASH_BLOCK.get()), volcanic);
            MaterialRule top = MaterialRules.sequence(
                    MaterialRules.ifTrue(this.steep, volcanic),
                    MaterialRules.ifTrue(MaterialRules.noiseCondition2d(Noises.SURFACE, 0.28), volcanic),
                    MaterialRules.ifTrue(MaterialRules.noiseCondition2d(Noises.PATCH, 0.18), cinder),
                    MaterialRules.ifTrue(MaterialRules.noiseCondition2d(Noises.SURFACE, -0.06, 0.02), cinder),
                    ash);
            return MaterialRules.sequence(
                    MaterialRules.ifTrue(this.onFloor, MaterialRules.sequence(MaterialRules.ifTrue(this.notUnderwater, top), cinder)),
                    MaterialRules.ifTrue(this.notUnderDeepWater, MaterialRules.sequence(
                            MaterialRules.ifTrue(this.underFloor, MaterialRules.sequence(MaterialRules.ifTrue(this.steep, volcanic), cinder)),
                            MaterialRules.ifTrue(this.veryDeepUnderFloor, volcanic))),
                    MaterialRules.ifTrue(this.underFloor, volcanic));
        }

        private MaterialRule frostPeaks() {
            MaterialRule rime = state(ModBlocks.RIME_STONE.base().get());
            MaterialRule snow = state(Blocks.SNOW_BLOCK);
            MaterialRule packed = state(Blocks.PACKED_ICE);
            MaterialRule glacier = state(ModBlocks.GLACIER_ICE.get());
            MaterialRule frozenSoil = state(ModBlocks.FROZEN_SOIL.get());
            MaterialRule top = MaterialRules.sequence(
                    MaterialRules.ifTrue(this.steep, MaterialRules.sequence(
                            MaterialRules.ifTrue(MaterialRules.noiseCondition2d(Noises.PACKED_ICE, -0.15, 0.15), packed), rime)),
                    MaterialRules.ifTrue(MaterialRules.noiseCondition2d(Noises.PACKED_ICE, 0.0, 0.18), packed),
                    MaterialRules.ifTrue(MaterialRules.noiseCondition2d(Noises.ICE, 0.0, 0.03), glacier),
                    MaterialRules.ifTrue(MaterialRules.noiseCondition2d(Noises.POWDER_SNOW, 0.42, 0.56), state(Blocks.POWDER_SNOW)),
                    MaterialRules.ifTrue(MaterialRules.noiseCondition2d(Noises.PATCH, 0.3), frozenSoil),
                    snow);
            MaterialRule under = MaterialRules.sequence(
                    MaterialRules.ifTrue(this.steep, rime),
                    MaterialRules.ifTrue(MaterialRules.noiseCondition2d(Noises.PACKED_ICE, -0.5, 0.2), packed),
                    MaterialRules.ifTrue(MaterialRules.noiseCondition2d(Noises.PATCH, 0.3), frozenSoil),
                    snow);
            return MaterialRules.sequence(
                    MaterialRules.ifTrue(this.onFloor, MaterialRules.sequence(MaterialRules.ifTrue(this.notUnderwater, top), rime)),
                    MaterialRules.ifTrue(this.notUnderDeepWater, MaterialRules.sequence(
                            MaterialRules.ifTrue(this.underFloor, under),
                            MaterialRules.ifTrue(this.veryDeepUnderFloor, rime))),
                    MaterialRules.ifTrue(this.underFloor, rime));
        }

        private MaterialRule stormCoast() {
            MaterialRule storm = state(ModBlocks.STORMSTONE.base().get());
            MaterialRule gravel = this.falling(state(Blocks.GRAVEL), storm);
            MaterialCondition streak = MaterialRules.noiseCondition2d(Noises.GRAVEL, -0.06, 0.06);
            // auf den Kuppen über dem Wellenschlag wächst Gras
            MaterialCondition grassy = MaterialRules.yStartCheck(VerticalAnchor.absolute(70), 0);
            MaterialCondition meadow = MaterialRules.noiseCondition2d(Noises.SURFACE, 0.12);
            MaterialRule top = MaterialRules.sequence(
                    MaterialRules.ifTrue(streak, gravel),
                    MaterialRules.ifTrue(this.steep, storm),
                    MaterialRules.ifTrue(grassy, MaterialRules.ifTrue(meadow, state(Blocks.GRASS_BLOCK))),
                    storm);
            MaterialRule under = MaterialRules.sequence(
                    MaterialRules.ifTrue(this.steep, storm),
                    MaterialRules.ifTrue(grassy, MaterialRules.ifTrue(meadow, state(Blocks.DIRT))),
                    storm);
            return MaterialRules.sequence(
                    MaterialRules.ifTrue(this.onFloor, MaterialRules.sequence(
                            MaterialRules.ifTrue(this.notUnderwater, top),
                            MaterialRules.ifTrue(streak, gravel),
                            storm)),
                    MaterialRules.ifTrue(this.notUnderDeepWater, MaterialRules.sequence(
                            MaterialRules.ifTrue(this.underFloor, under),
                            MaterialRules.ifTrue(this.veryDeepUnderFloor, storm))),
                    MaterialRules.ifTrue(this.underFloor, storm));
        }

        private MaterialRule primevalForest() {
            MaterialRule top = MaterialRules.sequence(
                    MaterialRules.ifTrue(MaterialRules.noiseCondition2d(Noises.SURFACE, 0.22), state(Blocks.PODZOL)),
                    MaterialRules.ifTrue(MaterialRules.noiseCondition2d(Noises.PATCH, 0.3), state(Blocks.MOSS_BLOCK)),
                    // schmale, gewundene Bänder: Wildwechsel aus grober Erde
                    MaterialRules.ifTrue(MaterialRules.noiseCondition2d(Noises.SURFACE, -0.31, -0.27), state(Blocks.COARSE_DIRT)),
                    MaterialRules.ifTrue(MaterialRules.noiseCondition2d(Noises.SMALL_PATCH, 2.3), state(ModBlocks.GLOWING_MOSS.get())),
                    state(Blocks.GRASS_BLOCK));
            MaterialRule pond = MaterialRules.sequence(
                    MaterialRules.ifTrue(MaterialRules.noiseCondition2d(Noises.PATCH, 0.0), state(Blocks.MUD)),
                    this.falling(state(Blocks.GRAVEL), state(Blocks.STONE)));
            return MaterialRules.sequence(
                    MaterialRules.ifTrue(this.onFloor, MaterialRules.sequence(MaterialRules.ifTrue(this.notUnderwater, top), pond)),
                    MaterialRules.ifTrue(this.notUnderDeepWater, MaterialRules.ifTrue(this.underFloor, MaterialRules.sequence(
                            MaterialRules.ifTrue(MaterialRules.noiseCondition2d(Noises.SURFACE, 0.22), state(Blocks.ROOTED_DIRT)),
                            state(Blocks.DIRT)))));
        }

        /**
         * Gesteinsschichten der Felsschluchten: von oben nach unten wiederkehrende Bänder unterschiedlicher Dicke. Die
         * Grenzen verschieben sich mit der Oberflächentiefe ein wenig, so liegen die Schichten leicht gewellt.
         */
        private MaterialRule strata() {
            Block[] bands = {Blocks.TUFF, ModBlocks.SHALE.base().get(), Blocks.STONE, Blocks.CALCITE, Blocks.ANDESITE,
                    ModBlocks.SHALE.base().get(), Blocks.TERRACOTTA, Blocks.TUFF, Blocks.DRIPSTONE_BLOCK, Blocks.STONE,
                    ModBlocks.SHALE.base().get(), Blocks.SMOOTH_BASALT};
            int[] thickness = {3, 2, 4, 1, 2, 3, 2, 3, 1, 3, 2, 1};
            List<MaterialRule> layers = new ArrayList<>();
            int y = 300;
            int i = 0;
            while (y > 30) {
                int band = i % bands.length;
                y -= thickness[band];
                layers.add(MaterialRules.ifTrue(MaterialRules.yBlockCheck(VerticalAnchor.absolute(y), 1), state(bands[band])));
                i++;
            }
            layers.add(state(Blocks.STONE));
            return MaterialRules.sequence(layers);
        }

        private MaterialRule rockGorges() {
            MaterialRule strata = MaterialRules.registerAndWrap(this.context, key("overworld/rock_strata"), this.strata());
            MaterialRule rubble = this.falling(state(ModBlocks.RUBBLE.get()), state(Blocks.TUFF));
            MaterialCondition scree = MaterialRules.noiseCondition2d(Noises.SURFACE, 0.3);
            MaterialRule top = MaterialRules.sequence(
                    MaterialRules.ifTrue(this.steep, strata),
                    MaterialRules.ifTrue(scree, rubble),
                    MaterialRules.ifTrue(MaterialRules.noiseCondition2d(Noises.SURFACE, 0.12), state(Blocks.COARSE_DIRT)),
                    MaterialRules.ifTrue(MaterialRules.noiseCondition2d(Noises.PATCH, 0.25), state(Blocks.COARSE_DIRT)),
                    state(Blocks.GRASS_BLOCK));
            MaterialRule under = MaterialRules.sequence(
                    MaterialRules.ifTrue(this.steep, strata),
                    MaterialRules.ifTrue(scree, rubble),
                    state(Blocks.DIRT));
            return MaterialRules.sequence(
                    MaterialRules.ifTrue(this.onFloor, MaterialRules.sequence(
                            MaterialRules.ifTrue(this.notUnderwater, top),
                            this.falling(state(Blocks.GRAVEL), state(Blocks.TUFF)))),
                    MaterialRules.ifTrue(this.notUnderDeepWater, MaterialRules.ifTrue(this.underFloor, under)),
                    // alles übrige über der Oberfläche: die gebänderten Felswände
                    strata);
        }

        private MaterialRule coralCoast() {
            MaterialRule sandstone = state(Blocks.SANDSTONE);
            MaterialRule sand = this.falling(state(Blocks.SAND), sandstone);
            return MaterialRules.sequence(
                    MaterialRules.ifTrue(this.onFloor, sand),
                    MaterialRules.ifTrue(this.underFloor, sand),
                    MaterialRules.ifTrue(this.deepUnderFloor, sandstone));
        }

        private MaterialRule mistMoor() {
            MaterialRule peat = state(ModBlocks.PEAT.get());
            MaterialRule mud = state(Blocks.MUD);
            MaterialCondition seaLevel = MaterialRules.yBlockCheck(VerticalAnchor.absolute(62), 0);
            MaterialCondition aboveSeaLevel = MaterialRules.yBlockCheck(VerticalAnchor.absolute(63), 0);
            MaterialRule puddles = MaterialRules.ifTrue(seaLevel, MaterialRules.ifTrue(MaterialRules.not(aboveSeaLevel),
                    MaterialRules.ifTrue(MaterialRules.noiseCondition2d(Noises.SWAMP, 0.0), state(Blocks.WATER))));
            MaterialRule top = MaterialRules.sequence(
                    MaterialRules.ifTrue(MaterialRules.noiseCondition2d(Noises.PATCH, 0.2), peat),
                    MaterialRules.ifTrue(MaterialRules.noiseCondition2d(Noises.SURFACE, 0.3), mud),
                    MaterialRules.ifTrue(MaterialRules.noiseCondition2d(Noises.SMALL_PATCH, 1.8), state(Blocks.MOSS_BLOCK)),
                    state(Blocks.GRASS_BLOCK));
            return MaterialRules.sequence(
                    MaterialRules.ifTrue(this.onFloor, MaterialRules.sequence(
                            puddles,
                            MaterialRules.ifTrue(this.notUnderwater, top),
                            MaterialRules.ifTrue(MaterialRules.noiseCondition2d(Noises.PATCH, -0.1), peat),
                            mud)),
                    MaterialRules.ifTrue(this.notUnderDeepWater, MaterialRules.ifTrue(this.underFloor, MaterialRules.sequence(
                            MaterialRules.ifTrue(MaterialRules.noiseCondition2d(Noises.PATCH, 0.0), peat),
                            state(Blocks.DIRT)))));
        }
    }
}
