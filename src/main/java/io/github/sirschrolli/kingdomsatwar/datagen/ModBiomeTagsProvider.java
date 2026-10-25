package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.ModTags;
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
                .add(Biomes.MEADOW);
        this.tag(ModTags.Biomes.HAS_LINDWORMS)
                .addTag(BiomeTags.IS_MOUNTAIN)
                .addTag(BiomeTags.IS_BADLANDS)
                .addTag(BiomeTags.IS_HILL);
    }
}
