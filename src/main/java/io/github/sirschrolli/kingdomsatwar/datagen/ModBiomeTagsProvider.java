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
}
