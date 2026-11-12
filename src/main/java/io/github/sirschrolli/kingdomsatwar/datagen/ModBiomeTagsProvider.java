package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.registry.ModTags;
import io.github.sirschrolli.kingdomsatwar.worldgen.overworld.OverworldBiomes;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.neoforged.neoforge.common.Tags;

/** Biom-Tags der Mod: wo Banditen umherstreifen und wo die wilden Kreaturen leben. */
public class ModBiomeTagsProvider extends TagsProvider<Biome> {
    public ModBiomeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, Registries.BIOME, lookupProvider, KingdomsAtWar.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        this.overworldBiomes();
        this.farmAnimals();
        this.villages();
        this.sites();
        this.tag(ModTags.Biomes.HAS_BANDITS)
                .addTag(Tags.Biomes.IS_PLAINS)
                .addTag(BiomeTags.IS_FOREST)
                .addTag(BiomeTags.IS_TAIGA)
                .addTag(BiomeTags.IS_SAVANNA)
                .add(Biomes.MEADOW);
        this.tag(ModTags.Biomes.HAS_WILD_BOARS)
                .addTag(BiomeTags.IS_FOREST)
                .addTag(BiomeTags.IS_TAIGA);
        this.tag(ModTags.Biomes.HAS_DEER)
                .addTag(BiomeTags.IS_FOREST)
                .addTag(BiomeTags.IS_TAIGA)
                .add(OverworldBiomes.ROYAL_MEADOWS)
                .add(Biomes.MEADOW)
                .add(Biomes.CHERRY_GROVE);
        // Tiere der eigenen Biome (0.85): der Schneeleopard auf den Frostgipfeln und den Hängen darunter
        this.tag(ModTags.Biomes.HAS_SNOW_LEOPARDS)
                .add(OverworldBiomes.FROST_PEAKS)
                .add(Biomes.SNOWY_SLOPES)
                .add(Biomes.JAGGED_PEAKS)
                .add(Biomes.GROVE);
        // der Urelch im Urhain und in den alten Taigawäldern
        this.tag(ModTags.Biomes.HAS_PRIMEVAL_ELKS)
                .add(OverworldBiomes.PRIMEVAL_FOREST)
                .add(Biomes.OLD_GROWTH_PINE_TAIGA)
                .add(Biomes.OLD_GROWTH_SPRUCE_TAIGA);
        // die Sturmmöwe an der Sturmküste, an den steinigen Ufern und an den Stränden
        this.tag(ModTags.Biomes.HAS_STORM_GULLS)
                .add(OverworldBiomes.STORM_COAST)
                .add(Biomes.STONY_SHORE)
                .add(Biomes.BEACH);
        // der Greif in den Felsschluchten und über den zerklüfteten Savannen
        this.tag(ModTags.Biomes.HAS_GRIFFINS)
                .add(OverworldBiomes.ROCK_GORGES)
                .add(Biomes.WINDSWEPT_SAVANNA);
        // 0.86: der Glutsalamander in den Aschefeldern, der Moorschnapper im Nebelmoor und in den Sümpfen, Flamingos an der
        // Korallenküste und in den Mangroven, Schmetterlinge auf den Blumenwiesen
        this.tag(ModTags.Biomes.HAS_EMBER_SALAMANDERS)
                .add(OverworldBiomes.ASH_FIELDS);
        this.tag(ModTags.Biomes.HAS_MIRE_SNAPPERS)
                .add(OverworldBiomes.MIST_MOOR)
                .add(Biomes.SWAMP);
        this.tag(ModTags.Biomes.HAS_FLAMINGOS)
                .add(OverworldBiomes.CORAL_COAST)
                .add(Biomes.MANGROVE_SWAMP);
        this.tag(ModTags.Biomes.HAS_BUTTERFLIES)
                .add(OverworldBiomes.ROYAL_MEADOWS)
                .add(Biomes.FLOWER_FOREST)
                .add(Biomes.MEADOW)
                .add(Biomes.SUNFLOWER_PLAINS)
                .add(Biomes.CHERRY_GROVE);
        this.tag(ModTags.Biomes.HAS_GEESE)
                .addTag(BiomeTags.IS_RIVER)
                .addTag(Tags.Biomes.IS_SWAMP)
                .addTag(Tags.Biomes.IS_PLAINS)
                .add(Biomes.MEADOW);
        this.tag(ModTags.Biomes.HAS_WILL_O_WISPS)
                .addTag(Tags.Biomes.IS_SWAMP)
                .add(Biomes.DARK_FOREST)
                .add(Biomes.PALE_GARDEN);
        // Überall in der Oberwelt außer auf Pilzinseln (dort gibt es keine Monster)
        this.tag(ModTags.Biomes.HAS_CAVE_TROLLS)
                .addTag(BiomeTags.IS_FOREST)
                .addTag(BiomeTags.IS_TAIGA)
                .addTag(BiomeTags.IS_JUNGLE)
                .addTag(BiomeTags.IS_SAVANNA)
                .addTag(BiomeTags.IS_BADLANDS)
                .addTag(BiomeTags.IS_MOUNTAIN)
                .addTag(BiomeTags.IS_HILL)
                .addTag(Tags.Biomes.IS_PLAINS)
                .addTag(Tags.Biomes.IS_DESERT)
                .addTag(Tags.Biomes.IS_SWAMP)
                .add(Biomes.DRIPSTONE_CAVES)
                .add(Biomes.LUSH_CAVES)
                .add(Biomes.MEADOW)
                .add(OverworldBiomes.ASH_FIELDS)
                .add(OverworldBiomes.STORM_COAST)
                .add(OverworldBiomes.CORAL_COAST);
        this.tag(ModTags.Biomes.HAS_LINDWORMS)
                .addTag(BiomeTags.IS_MOUNTAIN)
                .addTag(BiomeTags.IS_BADLANDS)
                .addTag(BiomeTags.IS_HILL)
                .add(OverworldBiomes.ROCK_GORGES);
        // Neue Monster (0.69)
        this.tag(ModTags.Biomes.HAS_WEREWOLVES)
                .addTag(BiomeTags.IS_FOREST)
                .addTag(BiomeTags.IS_TAIGA)
                .add(Biomes.PALE_GARDEN)
                .add(OverworldBiomes.PRIMEVAL_FOREST);
        this.tag(ModTags.Biomes.HAS_MIRE_SHAMBLERS)
                .addTag(Tags.Biomes.IS_SWAMP)
                .add(OverworldBiomes.MIST_MOOR);
        this.tag(ModTags.Biomes.HAS_EMBER_WYRMS)
                .add(Biomes.NETHER_WASTES)
                .add(Biomes.BASALT_DELTAS)
                .add(Biomes.CRIMSON_FOREST);
        this.tag(ModTags.Biomes.HAS_ASH_WRAITHS)
                .add(Biomes.SOUL_SAND_VALLEY)
                .add(Biomes.BASALT_DELTAS);
        // Monster des Ends (0.71): nur auf den äußeren Inseln, nie auf der Hauptinsel des Drachen
        this.tag(ModTags.Biomes.HAS_RIFT_HOUNDS)
                .add(Biomes.END_HIGHLANDS)
                .add(Biomes.END_MIDLANDS)
                .add(Biomes.END_BARRENS)
                .add(Biomes.SMALL_END_ISLANDS);
        this.tag(ModTags.Biomes.HAS_CHORUS_MAWS)
                .add(Biomes.END_HIGHLANDS)
                .add(Biomes.END_MIDLANDS);

        // Drachennester (je eins pro Welt): jede Art in ihrem eigenen Gebiet – Eis im Eis-Biom, Wasser im tiefen Ozean
        var nests = ModTags.Biomes.HAS_DRAGON_NESTS;
        this.tag(nests.get(DragonType.FIRE)).addTag(BiomeTags.IS_BADLANDS).add(Biomes.DESERT).add(OverworldBiomes.ASH_FIELDS);
        this.tag(nests.get(DragonType.ICE)).add(Biomes.ICE_SPIKES).add(Biomes.SNOWY_PLAINS).add(Biomes.SNOWY_SLOPES).add(Biomes.FROZEN_PEAKS)
                .add(OverworldBiomes.FROST_PEAKS);
        this.tag(nests.get(DragonType.LIGHTNING)).add(Biomes.STONY_PEAKS).add(Biomes.JAGGED_PEAKS).add(Biomes.FROZEN_PEAKS);
        this.tag(nests.get(DragonType.NATURE)).addTag(BiomeTags.IS_JUNGLE).add(Biomes.DARK_FOREST).add(Biomes.OLD_GROWTH_BIRCH_FOREST)
                .add(Biomes.OLD_GROWTH_PINE_TAIGA).add(Biomes.OLD_GROWTH_SPRUCE_TAIGA).add(OverworldBiomes.PRIMEVAL_FOREST);
        this.tag(nests.get(DragonType.EARTH)).add(Biomes.WINDSWEPT_GRAVELLY_HILLS).add(Biomes.WINDSWEPT_HILLS).add(Biomes.SAVANNA_PLATEAU)
                .add(Biomes.WINDSWEPT_SAVANNA).add(OverworldBiomes.ROCK_GORGES);
        this.tag(nests.get(DragonType.WATER)).add(Biomes.DEEP_OCEAN).add(Biomes.DEEP_COLD_OCEAN).add(Biomes.DEEP_LUKEWARM_OCEAN);

        // Siegelkreis: offenes, eher ebenes Land
        this.tag(ModTags.Biomes.HAS_SEAL_CIRCLE)
                .add(Biomes.PLAINS).add(Biomes.SUNFLOWER_PLAINS).add(Biomes.MEADOW).add(Biomes.SAVANNA)
                .add(Biomes.FOREST).add(Biomes.FLOWER_FOREST).add(Biomes.BIRCH_FOREST).add(Biomes.CHERRY_GROVE)
                .add(Biomes.TAIGA).add(Biomes.SNOWY_PLAINS).add(Biomes.SNOWY_TAIGA).add(OverworldBiomes.ROYAL_MEADOWS);

        // Stammburgen der fremden Reiche: ebenes, offenes Land (auch Wüste und Steppe – Dornfels baut in Sandstein)
        this.tag(ModTags.Biomes.HAS_REALM_CASTLE)
                .add(Biomes.PLAINS).add(Biomes.SUNFLOWER_PLAINS).add(Biomes.MEADOW).add(Biomes.SAVANNA).add(Biomes.SAVANNA_PLATEAU)
                .add(Biomes.FOREST).add(Biomes.FLOWER_FOREST).add(Biomes.BIRCH_FOREST).add(Biomes.TAIGA).add(Biomes.SNOWY_PLAINS)
                .add(Biomes.DESERT).add(OverworldBiomes.ROYAL_MEADOWS);

        // Dörfer der Umbrer: offene Wiesen, lichte Wälder, die fahle Ödnis und die Ränder der Seelenmarsch
        this.tag(ModTags.Biomes.HAS_MOON_TEMPLE)
                .add(io.github.sirschrolli.kingdomsatwar.shadow.ShadowBiomes.SHADOW_FOREST)
                .add(io.github.sirschrolli.kingdomsatwar.shadow.ShadowBiomes.PALE_WASTES)
                .add(io.github.sirschrolli.kingdomsatwar.shadow.ShadowBiomes.GLIMMER_MEADOWS);
        this.tag(ModTags.Biomes.HAS_CRYSTAL_MINE)
                .add(io.github.sirschrolli.kingdomsatwar.shadow.ShadowBiomes.CRYSTAL_CHASMS)
                .add(io.github.sirschrolli.kingdomsatwar.shadow.ShadowBiomes.SHADOW_PEAKS)
                .add(io.github.sirschrolli.kingdomsatwar.shadow.ShadowBiomes.PALE_WASTES);
        this.tag(ModTags.Biomes.HAS_LIGHTHOUSE)
                .add(io.github.sirschrolli.kingdomsatwar.shadow.ShadowBiomes.SHADOW_FOREST)
                .add(io.github.sirschrolli.kingdomsatwar.shadow.ShadowBiomes.PALE_WASTES)
                .add(io.github.sirschrolli.kingdomsatwar.shadow.ShadowBiomes.GLIMMER_MEADOWS)
                .add(io.github.sirschrolli.kingdomsatwar.shadow.ShadowBiomes.SOUL_MARSH)
                .add(io.github.sirschrolli.kingdomsatwar.shadow.ShadowBiomes.CRYSTAL_CHASMS);
        this.tag(ModTags.Biomes.HAS_OLD_WORLD_RUIN)
                .add(io.github.sirschrolli.kingdomsatwar.shadow.ShadowBiomes.GLIMMER_MEADOWS)
                .add(io.github.sirschrolli.kingdomsatwar.shadow.ShadowBiomes.PALE_WASTES)
                .add(io.github.sirschrolli.kingdomsatwar.shadow.ShadowBiomes.SHADOW_FOREST)
                .add(io.github.sirschrolli.kingdomsatwar.shadow.ShadowBiomes.SOUL_MARSH);
        // (seit 0.92 auch in den Landschaften, die aus den Weiten wurden: so liegt der Schlund dort, wo er in 0.91 lag)
        this.tag(ModTags.Biomes.HAS_SEA_VILLAGE).add(io.github.sirschrolli.kingdomsatwar.urmeer.UrmeerBiomes.KELP_FOREST)
                .add(io.github.sirschrolli.kingdomsatwar.urmeer.UrmeerBiomes.CORAL_GARDEN)
                .add(io.github.sirschrolli.kingdomsatwar.urmeer.UrmeerBiomes.PEARL_GROUNDS)
                .add(io.github.sirschrolli.kingdomsatwar.urmeer.UrmeerBiomes.DEEP);
        // die Seelenpferche (0.98): im Seelensumpf der Schattenwelt
        this.tag(ModTags.Biomes.HAS_SOUL_PEN).add(io.github.sirschrolli.kingdomsatwar.shadow.ShadowBiomes.SOUL_MARSH);
        this.tag(ModTags.Biomes.HAS_STAR_WEB).add(io.github.sirschrolli.kingdomsatwar.voidrealm.VoidBiomes.EXPANSE);
        // die Uhrtürme (1.01): in jedem Biom des Abgrunds
        io.github.sirschrolli.kingdomsatwar.abyss.AbyssBiomes.ALL.forEach(key -> this.tag(ModTags.Biomes.HAS_CLOCK_TOWER).add(key));
        // die Hexengrotten (0.97): in den tiefen, dunklen Gegenden des Urmeers
        this.tag(ModTags.Biomes.HAS_WITCH_GROTTO).add(io.github.sirschrolli.kingdomsatwar.urmeer.UrmeerBiomes.DEEP)
                .add(io.github.sirschrolli.kingdomsatwar.urmeer.UrmeerBiomes.KELP_FOREST)
                .add(io.github.sirschrolli.kingdomsatwar.urmeer.UrmeerBiomes.GLOW_DEPTHS)
                .add(io.github.sirschrolli.kingdomsatwar.urmeer.UrmeerBiomes.PEARL_GROUNDS);
        this.tag(ModTags.Biomes.HAS_KRAKEN_MAW).add(io.github.sirschrolli.kingdomsatwar.urmeer.UrmeerBiomes.DEEP)
                .add(io.github.sirschrolli.kingdomsatwar.urmeer.UrmeerBiomes.KELP_FOREST)
                .add(io.github.sirschrolli.kingdomsatwar.urmeer.UrmeerBiomes.PEARL_GROUNDS)
                .add(io.github.sirschrolli.kingdomsatwar.urmeer.UrmeerBiomes.CORAL_GARDEN);
        this.tag(ModTags.Biomes.HAS_SUNKEN_ATOLL)
                .add(net.minecraft.world.level.biome.Biomes.WARM_OCEAN)
                .add(net.minecraft.world.level.biome.Biomes.LUKEWARM_OCEAN)
                .add(net.minecraft.world.level.biome.Biomes.DEEP_LUKEWARM_OCEAN)
                .add(net.minecraft.world.level.biome.Biomes.OCEAN)
                .add(net.minecraft.world.level.biome.Biomes.DEEP_OCEAN);
        var throne = this.tag(ModTags.Biomes.HAS_VOID_THRONE);
        io.github.sirschrolli.kingdomsatwar.voidrealm.VoidBiomes.ALL.forEach(throne::add);
        var hoard = this.tag(ModTags.Biomes.HAS_WORLD_EATER_HOARD);
        io.github.sirschrolli.kingdomsatwar.abyss.AbyssBiomes.ALL.forEach(hoard::add);
        this.tag(ModTags.Biomes.HAS_SHADOW_CITADEL)
                .add(io.github.sirschrolli.kingdomsatwar.shadow.ShadowBiomes.PALE_WASTES)
                .add(io.github.sirschrolli.kingdomsatwar.shadow.ShadowBiomes.SHADOW_FOREST)
                .add(io.github.sirschrolli.kingdomsatwar.shadow.ShadowBiomes.GLIMMER_MEADOWS);
        this.tag(ModTags.Biomes.HAS_UMBRER_VILLAGE)
                .add(io.github.sirschrolli.kingdomsatwar.shadow.ShadowBiomes.GLIMMER_MEADOWS)
                .add(io.github.sirschrolli.kingdomsatwar.shadow.ShadowBiomes.SHADOW_FOREST)
                .add(io.github.sirschrolli.kingdomsatwar.shadow.ShadowBiomes.PALE_WASTES)
                .add(io.github.sirschrolli.kingdomsatwar.shadow.ShadowBiomes.SOUL_MARSH);

        // Türme der Erzmagier: jedes Element in seinem Land
        var towers = ModTags.Biomes.HAS_MAGE_TOWERS;
        this.tag(towers.get(DragonType.FIRE)).addTag(BiomeTags.IS_BADLANDS).add(Biomes.DESERT).add(OverworldBiomes.ASH_FIELDS);
        this.tag(towers.get(DragonType.ICE)).add(Biomes.SNOWY_PLAINS).add(Biomes.ICE_SPIKES).add(Biomes.SNOWY_TAIGA).add(Biomes.GROVE);
        this.tag(towers.get(DragonType.LIGHTNING)).add(Biomes.MEADOW).add(Biomes.WINDSWEPT_HILLS).add(Biomes.WINDSWEPT_FOREST)
                .add(Biomes.WINDSWEPT_GRAVELLY_HILLS).add(Biomes.STONY_SHORE).add(OverworldBiomes.STORM_COAST);
        this.tag(towers.get(DragonType.NATURE)).add(Biomes.FOREST).add(Biomes.FLOWER_FOREST).add(Biomes.BIRCH_FOREST)
                .add(Biomes.OLD_GROWTH_BIRCH_FOREST).add(Biomes.DARK_FOREST).add(Biomes.CHERRY_GROVE).addTag(BiomeTags.IS_JUNGLE)
                .add(OverworldBiomes.PRIMEVAL_FOREST);
        this.tag(towers.get(DragonType.EARTH)).addTag(BiomeTags.IS_SAVANNA).add(Biomes.PLAINS).add(Biomes.SUNFLOWER_PLAINS)
                .add(Biomes.TAIGA).add(Biomes.OLD_GROWTH_PINE_TAIGA).add(Biomes.OLD_GROWTH_SPRUCE_TAIGA);
        this.tag(towers.get(DragonType.WATER)).addTag(BiomeTags.IS_BEACH).add(Biomes.SWAMP).add(Biomes.MANGROVE_SWAMP)
                .add(OverworldBiomes.MIST_MOOR);

        this.tag(ModTags.Biomes.HAS_CASTLE_RUINS)
                .addTag(Tags.Biomes.IS_PLAINS)
                .addTag(BiomeTags.IS_FOREST)
                .addTag(BiomeTags.IS_TAIGA)
                .add(Biomes.MEADOW);
    }

    /**
     * Die eigenen Oberwelt-Biome in den allgemeinen Tags (Vanilla und {@code c:}), damit Spiel und andere Mods sie richtig
     * einordnen – und damit die Biom-Modifikatoren der Mod (Erze, Blumen, Kreaturen) sie erreichen.
     */
    /**
     * Die Dörfer: Fachwerk in Ebenen, Wiesen und Laubwäldern, nordische Blockhäuser in Taiga und Schneeland, Lehmbauten in
     * Wüste und Savanne, Pfahlbauten in Sumpf und Moor. Die alten Dörfer (und die Außenposten der Plünderer) gibt es nicht
     * mehr.
     */
    private void villages() {
        var v = ModTags.Biomes.HAS_VILLAGE;
        this.tag(v.get(io.github.sirschrolli.kingdomsatwar.worldgen.village.VillageStyle.TIMBER))
                .add(Biomes.PLAINS).add(Biomes.SUNFLOWER_PLAINS).add(Biomes.MEADOW).add(Biomes.FOREST).add(Biomes.FLOWER_FOREST)
                .add(Biomes.BIRCH_FOREST).add(OverworldBiomes.ROYAL_MEADOWS);
        this.tag(v.get(io.github.sirschrolli.kingdomsatwar.worldgen.village.VillageStyle.NORDIC))
                .add(Biomes.TAIGA).add(Biomes.SNOWY_TAIGA).add(Biomes.SNOWY_PLAINS).add(Biomes.OLD_GROWTH_PINE_TAIGA)
                .add(Biomes.OLD_GROWTH_SPRUCE_TAIGA);
        this.tag(v.get(io.github.sirschrolli.kingdomsatwar.worldgen.village.VillageStyle.CLAY))
                .add(Biomes.DESERT).add(Biomes.SAVANNA);
        this.tag(v.get(io.github.sirschrolli.kingdomsatwar.worldgen.village.VillageStyle.STILT))
                .add(Biomes.SWAMP).add(Biomes.MANGROVE_SWAMP).add(OverworldBiomes.MIST_MOOR);
        var ground = this.tag(ModTags.Biomes.VILLAGE_GROUND);
        for (var style : io.github.sirschrolli.kingdomsatwar.worldgen.village.VillageStyle.values()) {
            ground.addTag(v.get(style));
        }
        // keine Dörfer und Außenposten aus Vanilla mehr
        for (TagKey<Biome> old : java.util.List.of(BiomeTags.HAS_VILLAGE_PLAINS, BiomeTags.HAS_VILLAGE_DESERT, BiomeTags.HAS_VILLAGE_SAVANNA,
                BiomeTags.HAS_VILLAGE_SNOWY, BiomeTags.HAS_VILLAGE_TAIGA, BiomeTags.HAS_PILLAGER_OUTPOST)) {
            this.tag(old).replace();
        }
    }

    /**
     * Die eigenen Bauwerke an Stelle der Vanilla-Bauwerke: das Weltentor und die Stollen überall in der Oberwelt, Schätze
     * an den Stränden, Wracks im Meer und an der Küste. Die Vanilla-Bauwerke, die sie ersetzen, entstehen nicht mehr.
     */
    private void sites() {
        var has = ModTags.Biomes.HAS_SITE;
        this.tag(has.get(io.github.sirschrolli.kingdomsatwar.worldgen.site.SiteKind.FALLEN_STAR)).addTag(BiomeTags.IS_OVERWORLD);
        this.tag(has.get(io.github.sirschrolli.kingdomsatwar.worldgen.site.SiteKind.AQUEDUCT)).addTag(BiomeTags.IS_OVERWORLD);
        this.tag(has.get(io.github.sirschrolli.kingdomsatwar.worldgen.site.SiteKind.TIDE_SHRINE)).addTag(BiomeTags.IS_BEACH)
                .add(OverworldBiomes.CORAL_COAST);
        // die Bauwerke des Meeres auch im Urmeer (0.89): Gebeine von Seedrachen, Muschelfelder, Korallenpaläste
        this.tag(has.get(io.github.sirschrolli.kingdomsatwar.worldgen.site.SiteKind.SEA_DRAGON)).addTag(BiomeTags.IS_OCEAN)
                .addTag(BiomeTags.IS_BEACH).add(OverworldBiomes.CORAL_COAST).add(OverworldBiomes.STORM_COAST)
                .add(io.github.sirschrolli.kingdomsatwar.urmeer.UrmeerBiomes.DEEP).add(io.github.sirschrolli.kingdomsatwar.urmeer.UrmeerBiomes.ISLES)
                .add(io.github.sirschrolli.kingdomsatwar.urmeer.UrmeerBiomes.KELP_FOREST)
                .add(io.github.sirschrolli.kingdomsatwar.urmeer.UrmeerBiomes.PEARL_GROUNDS);
        this.tag(has.get(io.github.sirschrolli.kingdomsatwar.worldgen.site.SiteKind.CARAVANSERAI)).add(net.minecraft.world.level.biome.Biomes.DESERT);
        this.tag(has.get(io.github.sirschrolli.kingdomsatwar.worldgen.site.SiteKind.SERPENT_TEMPLE)).addTag(BiomeTags.IS_JUNGLE);
        this.tag(has.get(io.github.sirschrolli.kingdomsatwar.worldgen.site.SiteKind.WITCH_TREE)).add(net.minecraft.world.level.biome.Biomes.SWAMP)
                .add(OverworldBiomes.MIST_MOOR);
        this.tag(has.get(io.github.sirschrolli.kingdomsatwar.worldgen.site.SiteKind.FROST_GROTTO)).add(net.minecraft.world.level.biome.Biomes.SNOWY_PLAINS)
                .add(net.minecraft.world.level.biome.Biomes.SNOWY_TAIGA).add(net.minecraft.world.level.biome.Biomes.SNOWY_SLOPES)
                .add(OverworldBiomes.FROST_PEAKS);
        this.tag(has.get(io.github.sirschrolli.kingdomsatwar.worldgen.site.SiteKind.BANDIT_CAMP)).add(net.minecraft.world.level.biome.Biomes.PLAINS)
                .add(net.minecraft.world.level.biome.Biomes.SAVANNA).add(net.minecraft.world.level.biome.Biomes.TAIGA)
                .add(net.minecraft.world.level.biome.Biomes.SNOWY_PLAINS).add(net.minecraft.world.level.biome.Biomes.MEADOW)
                .add(net.minecraft.world.level.biome.Biomes.GROVE).add(net.minecraft.world.level.biome.Biomes.DESERT)
                .add(OverworldBiomes.ROYAL_MEADOWS).add(OverworldBiomes.ROCK_GORGES);
        this.tag(has.get(io.github.sirschrolli.kingdomsatwar.worldgen.site.SiteKind.CHARCOAL_CAMP)).addTag(BiomeTags.IS_FOREST).addTag(BiomeTags.IS_TAIGA)
                .add(net.minecraft.world.level.biome.Biomes.MEADOW).add(net.minecraft.world.level.biome.Biomes.SAVANNA)
                .add(net.minecraft.world.level.biome.Biomes.SPARSE_JUNGLE).add(net.minecraft.world.level.biome.Biomes.BAMBOO_JUNGLE)
                .add(net.minecraft.world.level.biome.Biomes.SWAMP).add(net.minecraft.world.level.biome.Biomes.WOODED_BADLANDS)
                .add(net.minecraft.world.level.biome.Biomes.PALE_GARDEN).add(OverworldBiomes.PRIMEVAL_FOREST);
        this.tag(has.get(io.github.sirschrolli.kingdomsatwar.worldgen.site.SiteKind.EMBER_GATE)).addTag(BiomeTags.IS_OVERWORLD);
        this.tag(has.get(io.github.sirschrolli.kingdomsatwar.worldgen.site.SiteKind.CLAM_FIELD)).addTag(BiomeTags.IS_OCEAN)
                .add(io.github.sirschrolli.kingdomsatwar.urmeer.UrmeerBiomes.DEEP).add(io.github.sirschrolli.kingdomsatwar.urmeer.UrmeerBiomes.REEF)
                .add(io.github.sirschrolli.kingdomsatwar.urmeer.UrmeerBiomes.PEARL_GROUNDS)
                .add(io.github.sirschrolli.kingdomsatwar.urmeer.UrmeerBiomes.CORAL_GARDEN);
        this.tag(has.get(io.github.sirschrolli.kingdomsatwar.worldgen.site.SiteKind.CORAL_PALACE)).addTag(BiomeTags.IS_DEEP_OCEAN)
                .add(io.github.sirschrolli.kingdomsatwar.urmeer.UrmeerBiomes.DEEP);
        // der Versunkene Tempel mit dem Gezeitentor (0.89): auf dem Grund der tiefen Ozeane
        this.tag(has.get(io.github.sirschrolli.kingdomsatwar.worldgen.site.SiteKind.SUNKEN_TEMPLE)).addTag(BiomeTags.IS_DEEP_OCEAN);
        this.tag(has.get(io.github.sirschrolli.kingdomsatwar.worldgen.site.SiteKind.BURIED_ABBEY)).add(net.minecraft.world.level.biome.Biomes.TAIGA)
                .add(net.minecraft.world.level.biome.Biomes.SNOWY_TAIGA).add(net.minecraft.world.level.biome.Biomes.OLD_GROWTH_PINE_TAIGA)
                .add(net.minecraft.world.level.biome.Biomes.OLD_GROWTH_SPRUCE_TAIGA).add(net.minecraft.world.level.biome.Biomes.OLD_GROWTH_BIRCH_FOREST)
                .add(net.minecraft.world.level.biome.Biomes.JUNGLE).add(OverworldBiomes.PRIMEVAL_FOREST);
        this.tag(has.get(io.github.sirschrolli.kingdomsatwar.worldgen.site.SiteKind.SILENT_CATHEDRAL)).add(net.minecraft.world.level.biome.Biomes.DEEP_DARK);
        // die Arena liegt tief unter der Erde, fast überall (das Reich des Sculk prüft sie selbst aus)
        this.tag(has.get(io.github.sirschrolli.kingdomsatwar.worldgen.site.SiteKind.TRIAL_ARENA)).addTag(BiomeTags.IS_OVERWORLD);
        this.tag(has.get(io.github.sirschrolli.kingdomsatwar.worldgen.site.SiteKind.ROBBER_CASTLE)).add(net.minecraft.world.level.biome.Biomes.DARK_FOREST)
                .add(net.minecraft.world.level.biome.Biomes.PALE_GARDEN);
        // in der Unterwelt: die Aschenesse überall (wie die Festung, die sie ersetzt)
        this.tag(has.get(io.github.sirschrolli.kingdomsatwar.worldgen.site.SiteKind.ASH_FORGE)).addTag(BiomeTags.IS_NETHER);
        this.tag(BiomeTags.HAS_NETHER_FORTRESS).replace();
        // die Hortfeste, wo früher Bastionen standen (nicht in den Basaltdeltas)
        this.tag(has.get(io.github.sirschrolli.kingdomsatwar.worldgen.site.SiteKind.HORTFESTE)).add(net.minecraft.world.level.biome.Biomes.CRIMSON_FOREST)
                .add(net.minecraft.world.level.biome.Biomes.NETHER_WASTES).add(net.minecraft.world.level.biome.Biomes.SOUL_SAND_VALLEY)
                .add(net.minecraft.world.level.biome.Biomes.WARPED_FOREST);
        this.tag(BiomeTags.HAS_BASTION_REMNANT).replace();
        // die Riesengebeine im Seelensandtal (statt der Netherfossilien), die Verlorene Pforte überall im Nether (statt der Portalruine)
        this.tag(has.get(io.github.sirschrolli.kingdomsatwar.worldgen.site.SiteKind.GIANT_BONES))
                .add(net.minecraft.world.level.biome.Biomes.SOUL_SAND_VALLEY);
        this.tag(BiomeTags.HAS_NETHER_FOSSIL).replace();
        this.tag(has.get(io.github.sirschrolli.kingdomsatwar.worldgen.site.SiteKind.LOST_GATE)).addTag(BiomeTags.IS_NETHER);
        this.tag(BiomeTags.HAS_RUINED_PORTAL_NETHER).replace();
        // die Sternwarte auf den äußeren Inseln des Ends (statt der Endstadt)
        this.tag(has.get(io.github.sirschrolli.kingdomsatwar.worldgen.site.SiteKind.STARWATCH))
                .add(net.minecraft.world.level.biome.Biomes.END_HIGHLANDS).add(net.minecraft.world.level.biome.Biomes.END_MIDLANDS);
        // die Fehlerzone (Anni der Troll): auf festem, eher flachem Land – Wiesen, Wälder, Savannen, Schnee, Wüste und Tafelberge
        this.tag(has.get(io.github.sirschrolli.kingdomsatwar.worldgen.site.SiteKind.ERROR_ZONE)).addTag(BiomeTags.IS_FOREST).addTag(BiomeTags.IS_TAIGA)
                .addTag(BiomeTags.IS_SAVANNA).addTag(BiomeTags.IS_BADLANDS).add(net.minecraft.world.level.biome.Biomes.PLAINS)
                .add(net.minecraft.world.level.biome.Biomes.SUNFLOWER_PLAINS).add(net.minecraft.world.level.biome.Biomes.MEADOW)
                .add(net.minecraft.world.level.biome.Biomes.SNOWY_PLAINS).add(net.minecraft.world.level.biome.Biomes.DESERT);
        this.tag(BiomeTags.HAS_END_CITY).replace();
        for (TagKey<Biome> old : java.util.List.of(BiomeTags.HAS_STRONGHOLD, BiomeTags.HAS_MINESHAFT, BiomeTags.HAS_MINESHAFT_MESA,
                BiomeTags.HAS_BURIED_TREASURE, BiomeTags.HAS_SHIPWRECK, BiomeTags.HAS_SHIPWRECK_BEACHED, BiomeTags.HAS_DESERT_PYRAMID,
                BiomeTags.HAS_JUNGLE_TEMPLE, BiomeTags.HAS_SWAMP_HUT, BiomeTags.HAS_IGLOO, BiomeTags.HAS_RUINED_PORTAL_DESERT,
                BiomeTags.HAS_RUINED_PORTAL_JUNGLE, BiomeTags.HAS_RUINED_PORTAL_OCEAN, BiomeTags.HAS_RUINED_PORTAL_SWAMP,
                BiomeTags.HAS_RUINED_PORTAL_MOUNTAIN, BiomeTags.HAS_RUINED_PORTAL_STANDARD, BiomeTags.HAS_ABANDONED_CAMP_BAMBOO_JUNGLE,
                BiomeTags.HAS_ABANDONED_CAMP_BIRCH_FOREST, BiomeTags.HAS_ABANDONED_CAMP_CHERRY_GROVE, BiomeTags.HAS_ABANDONED_CAMP_DAPPLED_FOREST,
                BiomeTags.HAS_ABANDONED_CAMP_FLOWER_FOREST, BiomeTags.HAS_ABANDONED_CAMP_FOREST, BiomeTags.HAS_ABANDONED_CAMP_MEADOW,
                BiomeTags.HAS_ABANDONED_CAMP_OLD_GROWTH_BIRCH_FOREST, BiomeTags.HAS_ABANDONED_CAMP_OLD_GROWTH_PINE_TAIGA,
                BiomeTags.HAS_ABANDONED_CAMP_OLD_GROWTH_SPRUCE_TAIGA, BiomeTags.HAS_ABANDONED_CAMP_PALE_GARDEN, BiomeTags.HAS_ABANDONED_CAMP_SAVANNA,
                BiomeTags.HAS_ABANDONED_CAMP_SNOWY_TAIGA, BiomeTags.HAS_ABANDONED_CAMP_SPARSE_JUNGLE, BiomeTags.HAS_ABANDONED_CAMP_SWAMP,
                BiomeTags.HAS_ABANDONED_CAMP_TAIGA, BiomeTags.HAS_ABANDONED_CAMP_WINDSWEPT_FOREST, BiomeTags.HAS_ABANDONED_CAMP_WOODED_BADLANDS,
                BiomeTags.HAS_OCEAN_RUIN_COLD, BiomeTags.HAS_OCEAN_RUIN_WARM, BiomeTags.HAS_OCEAN_MONUMENT, BiomeTags.HAS_TRAIL_RUINS,
                BiomeTags.HAS_ANCIENT_CITY, BiomeTags.HAS_TRIAL_CHAMBERS, BiomeTags.HAS_WOODLAND_MANSION)) {
            this.tag(old).replace();
        }
    }

    private void overworldBiomes() {
        var all = OverworldBiomes.ALL;
        all.forEach(biome -> {
            this.tag(BiomeTags.IS_OVERWORLD).add(biome);
            this.tag(Tags.Biomes.IS_OVERWORLD).add(biome);
            this.tag(BiomeTags.STRONGHOLD_BIASED_TO).add(biome);
        });
        this.tag(BiomeTags.IS_MOUNTAIN).add(OverworldBiomes.FROST_PEAKS);
        this.tag(BiomeTags.IS_FOREST).add(OverworldBiomes.PRIMEVAL_FOREST);
        this.tag(BiomeTags.IS_SAVANNA).add(OverworldBiomes.ROCK_GORGES);
        this.tag(BiomeTags.IS_BEACH).add(OverworldBiomes.CORAL_COAST);
        this.tag(BiomeTags.SPAWNS_WHITE_RABBITS).add(OverworldBiomes.FROST_PEAKS);
        this.tag(BiomeTags.SPAWNS_SNOW_FOXES).add(OverworldBiomes.FROST_PEAKS);
        this.tag(BiomeTags.SPAWNS_GOLD_RABBITS).add(OverworldBiomes.ASH_FIELDS);
        this.tag(BiomeTags.ALLOWS_SURFACE_SLIME_SPAWNS).add(OverworldBiomes.MIST_MOOR);
        this.tag(BiomeTags.WATER_ON_MAP_OUTLINES).add(OverworldBiomes.MIST_MOOR);
        this.tag(BiomeTags.PRODUCES_CORALS_FROM_BONEMEAL).add(OverworldBiomes.CORAL_COAST);

        this.tag(Tags.Biomes.IS_PLAINS).add(OverworldBiomes.ROYAL_MEADOWS);
        this.tag(Tags.Biomes.IS_FLORAL).add(OverworldBiomes.ROYAL_MEADOWS);
        this.tag(Tags.Biomes.IS_SWAMP).add(OverworldBiomes.MIST_MOOR);
        this.tag(Tags.Biomes.IS_SPOOKY).add(OverworldBiomes.MIST_MOOR);
        this.tag(Tags.Biomes.IS_SNOWY).add(OverworldBiomes.FROST_PEAKS);
        this.tag(Tags.Biomes.IS_ICY).add(OverworldBiomes.FROST_PEAKS);
        this.tag(Tags.Biomes.IS_MOUNTAIN_PEAK).add(OverworldBiomes.FROST_PEAKS);
        this.tag(Tags.Biomes.IS_STONY_SHORES).add(OverworldBiomes.STORM_COAST);
        this.tag(Tags.Biomes.IS_WINDSWEPT).add(OverworldBiomes.STORM_COAST);
        this.tag(Tags.Biomes.IS_OLD_GROWTH).add(OverworldBiomes.PRIMEVAL_FOREST);
        this.tag(Tags.Biomes.IS_MAGICAL).add(OverworldBiomes.PRIMEVAL_FOREST);
        this.tag(Tags.Biomes.IS_PLATEAU).add(OverworldBiomes.ROCK_GORGES);
        this.tag(Tags.Biomes.IS_WASTELAND).add(OverworldBiomes.ASH_FIELDS);
        this.tag(Tags.Biomes.IS_DEAD).add(OverworldBiomes.ASH_FIELDS);
        this.tag(Tags.Biomes.IS_SANDY).add(OverworldBiomes.CORAL_COAST);
        this.tag(Tags.Biomes.IS_HOT_OVERWORLD).add(OverworldBiomes.ASH_FIELDS).add(OverworldBiomes.ROCK_GORGES).add(OverworldBiomes.CORAL_COAST);
        this.tag(Tags.Biomes.IS_COLD_OVERWORLD).add(OverworldBiomes.FROST_PEAKS);
        this.tag(Tags.Biomes.IS_TEMPERATE_OVERWORLD).add(OverworldBiomes.STORM_COAST).add(OverworldBiomes.PRIMEVAL_FOREST)
                .add(OverworldBiomes.ROYAL_MEADOWS).add(OverworldBiomes.MIST_MOOR);
        this.tag(Tags.Biomes.IS_DRY_OVERWORLD).add(OverworldBiomes.ASH_FIELDS).add(OverworldBiomes.ROCK_GORGES);
        this.tag(Tags.Biomes.IS_WET_OVERWORLD).add(OverworldBiomes.MIST_MOOR).add(OverworldBiomes.PRIMEVAL_FOREST);
        this.tag(Tags.Biomes.IS_SPARSE_VEGETATION_OVERWORLD).add(OverworldBiomes.ASH_FIELDS).add(OverworldBiomes.FROST_PEAKS)
                .add(OverworldBiomes.ROCK_GORGES);
        this.tag(Tags.Biomes.IS_DENSE_VEGETATION_OVERWORLD).add(OverworldBiomes.PRIMEVAL_FOREST);
        this.tag(Tags.Biomes.IS_CONIFEROUS_TREE).add(OverworldBiomes.STORM_COAST);
        this.tag(Tags.Biomes.IS_DECIDUOUS_TREE).add(OverworldBiomes.PRIMEVAL_FOREST).add(OverworldBiomes.ROYAL_MEADOWS);
        this.tag(Tags.Biomes.IS_SAVANNA_TREE).add(OverworldBiomes.ROCK_GORGES);
        this.tag(Tags.Biomes.PRIMARY_WOOD_TYPE_OAK).add(OverworldBiomes.ROYAL_MEADOWS);
        this.tag(Tags.Biomes.PRIMARY_WOOD_TYPE_SPRUCE).add(OverworldBiomes.STORM_COAST);
        this.tag(Tags.Biomes.PRIMARY_WOOD_TYPE_JUNGLE).add(OverworldBiomes.CORAL_COAST);
        this.tag(Tags.Biomes.PRIMARY_WOOD_TYPE_ACACIA).add(OverworldBiomes.ROCK_GORGES);
        this.tag(Tags.Biomes.PRIMARY_WOOD_TYPE_BIRCH).add(OverworldBiomes.MIST_MOOR);
    }

    /** Eigene Hof- und Weidetiere: dieselben Biome wie bei Vanilla, dazu die eigenen Biome der Oberwelt. */
    private void farmAnimals() {
        java.util.List<net.minecraft.resources.ResourceKey<Biome>> farm = java.util.List.of(Biomes.BADLANDS, Biomes.BAMBOO_JUNGLE,
                Biomes.BIRCH_FOREST, Biomes.DAPPLED_FOREST,
                Biomes.DARK_FOREST, Biomes.ERODED_BADLANDS, Biomes.FLOWER_FOREST, Biomes.FOREST, Biomes.JUNGLE,
                Biomes.OLD_GROWTH_BIRCH_FOREST, Biomes.OLD_GROWTH_PINE_TAIGA, Biomes.OLD_GROWTH_SPRUCE_TAIGA, Biomes.PLAINS,
                Biomes.SAVANNA, Biomes.SAVANNA_PLATEAU, Biomes.SNOWY_TAIGA, Biomes.SPARSE_JUNGLE, Biomes.SUNFLOWER_PLAINS,
                Biomes.SWAMP, Biomes.TAIGA, Biomes.WINDSWEPT_FOREST, Biomes.WINDSWEPT_GRAVELLY_HILLS, Biomes.WINDSWEPT_HILLS,
                Biomes.WINDSWEPT_SAVANNA, Biomes.WOODED_BADLANDS, OverworldBiomes.ROYAL_MEADOWS, OverworldBiomes.PRIMEVAL_FOREST,
                OverworldBiomes.ROCK_GORGES);
        for (var tag : java.util.List.of(ModTags.Biomes.HAS_CATTLE, ModTags.Biomes.HAS_PIGS, ModTags.Biomes.HAS_SHEEP,
                ModTags.Biomes.HAS_CHICKENS)) {
            var builder = this.tag(tag);
            farm.forEach(builder::add);
        }
        this.tag(ModTags.Biomes.HAS_MUSHROOM_CATTLE).add(Biomes.MUSHROOM_FIELDS);
        this.tag(ModTags.Biomes.HAS_FEW_SHEEP).add(Biomes.CHERRY_GROVE).add(Biomes.MEADOW);
        this.tag(ModTags.Biomes.HAS_HORSES).add(Biomes.PLAINS).add(Biomes.SUNFLOWER_PLAINS).add(OverworldBiomes.ROYAL_MEADOWS);
        this.tag(ModTags.Biomes.HAS_FEW_HORSES).add(Biomes.SAVANNA).add(Biomes.SAVANNA_PLATEAU).add(Biomes.WINDSWEPT_SAVANNA)
                .add(Biomes.MEADOW).add(OverworldBiomes.ROCK_GORGES);
        this.tag(ModTags.Biomes.HAS_DONKEYS).add(Biomes.MEADOW).add(Biomes.PLAINS).add(Biomes.SUNFLOWER_PLAINS).add(Biomes.SAVANNA)
                .add(Biomes.SAVANNA_PLATEAU).add(Biomes.WINDSWEPT_SAVANNA);
        this.tag(ModTags.Biomes.HAS_GOATS).add(Biomes.FROZEN_PEAKS).add(Biomes.JAGGED_PEAKS).add(Biomes.SNOWY_SLOPES)
                .add(Biomes.STONY_PEAKS).add(OverworldBiomes.FROST_PEAKS);
        this.tag(ModTags.Biomes.HAS_FEW_GOATS).add(Biomes.MEADOW).add(OverworldBiomes.ROYAL_MEADOWS).add(OverworldBiomes.ROCK_GORGES);
        this.tag(ModTags.Biomes.HAS_MANY_HARES).add(Biomes.DESERT).add(Biomes.SNOWY_PLAINS).add(Biomes.ICE_SPIKES).add(Biomes.GROVE);
        this.tag(ModTags.Biomes.HAS_HARES).add(Biomes.TAIGA).add(Biomes.SNOWY_TAIGA).add(Biomes.OLD_GROWTH_PINE_TAIGA)
                .add(Biomes.OLD_GROWTH_SPRUCE_TAIGA).add(Biomes.SNOWY_SLOPES).add(Biomes.FLOWER_FOREST).add(Biomes.PLAINS)
                .add(Biomes.SUNFLOWER_PLAINS).add(OverworldBiomes.ROYAL_MEADOWS).add(OverworldBiomes.PRIMEVAL_FOREST)
                .add(Biomes.DAPPLED_FOREST);
        this.tag(ModTags.Biomes.HAS_FEW_HARES).add(Biomes.CHERRY_GROVE).add(Biomes.MEADOW).add(Biomes.SAVANNA);
        this.tag(ModTags.Biomes.REPLACES_VANILLA_ANIMALS).addTag(BiomeTags.IS_OVERWORLD)
                .add(OverworldBiomes.ROYAL_MEADOWS).add(OverworldBiomes.PRIMEVAL_FOREST).add(OverworldBiomes.ROCK_GORGES)
                .add(OverworldBiomes.FROST_PEAKS).add(OverworldBiomes.ASH_FIELDS).add(OverworldBiomes.STORM_COAST)
                .add(OverworldBiomes.CORAL_COAST).add(OverworldBiomes.MIST_MOOR);
    }
}
