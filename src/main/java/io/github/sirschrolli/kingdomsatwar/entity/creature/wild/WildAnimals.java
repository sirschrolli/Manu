package io.github.sirschrolli.kingdomsatwar.entity.creature.wild;

import io.github.sirschrolli.kingdomsatwar.worldgen.overworld.OverworldBiomes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Gemeinsames der eigenen Wildtiere (E3b): Biom-Gruppen für die Wahl der Art, ein Zufall, der für ein ganzes Rudel
 * gleich ausfällt, und die Unempfindlichkeit gegen Beerensträucher (Füchse und Bienen).
 *
 * <p>Die Rassen-Hilfen (Speichern als Name, Vererbung, Name mit Rasse) teilen sich die Wildtiere mit den Hoftieren,
 * siehe {@link io.github.sirschrolli.kingdomsatwar.entity.creature.farm.FarmAnimals}.
 */
public final class WildAnimals {
    private WildAnimals() {}

    /**
     * Zufall aus der Lage des Chunks: Alle Tiere, die zusammen in einem Chunk erscheinen (ein Rudel, eine Herde), ziehen
     * dieselbe Zahl und bekommen so dieselbe Art.
     */
    public static RandomSource herd(BlockPos pos, int salt) {
        return RandomSource.create(Mth.getSeed(pos.getX() >> 4, salt, pos.getZ() >> 4));
    }

    // ------------------------------------------------------------------ Biome

    public static boolean snowy(Holder<Biome> biome) {
        return biome.is(BiomeTags.SPAWNS_SNOW_FOXES) || biome.is(BiomeTags.POLAR_BEARS_SPAWN_ON_ALTERNATE_BLOCKS)
                || biome.is(Biomes.SNOWY_PLAINS) || biome.is(Biomes.ICE_SPIKES) || biome.is(Biomes.SNOWY_SLOPES)
                || biome.is(Biomes.GROVE) || biome.is(Biomes.FROZEN_PEAKS) || biome.is(Biomes.JAGGED_PEAKS)
                || biome.is(Biomes.SNOWY_BEACH) || biome.is(Biomes.FROZEN_RIVER) || biome.is(OverworldBiomes.FROST_PEAKS);
    }

    public static boolean taiga(Holder<Biome> biome) {
        return biome.is(BiomeTags.IS_TAIGA);
    }

    public static boolean desert(Holder<Biome> biome) {
        return biome.is(Biomes.DESERT);
    }

    public static boolean badlands(Holder<Biome> biome) {
        return biome.is(BiomeTags.IS_BADLANDS) || biome.is(OverworldBiomes.ROCK_GORGES);
    }

    public static boolean savanna(Holder<Biome> biome) {
        return biome.is(BiomeTags.IS_SAVANNA);
    }

    public static boolean jungle(Holder<Biome> biome) {
        return biome.is(BiomeTags.IS_JUNGLE);
    }

    public static boolean wetlands(Holder<Biome> biome) {
        return biome.is(Biomes.SWAMP) || biome.is(Biomes.MANGROVE_SWAMP) || biome.is(OverworldBiomes.MIST_MOOR);
    }

    public static boolean forest(Holder<Biome> biome) {
        return biome.is(BiomeTags.IS_FOREST) || biome.is(OverworldBiomes.PRIMEVAL_FOREST);
    }

    // ------------------------------------------------------------------ Beerensträucher

    /** Füchse und Bienen bleiben in Beerensträuchern nicht hängen (Vanilla prüft nur die Vanilla-Typen). */
    public static boolean ignoresBush(BlockState state) {
        return state.is(Blocks.SWEET_BERRY_BUSH);
    }

    /** … und stechen sich nicht an ihnen. */
    public static boolean isBushDamage(DamageSource source) {
        return source.is(DamageTypes.SWEET_BERRY_BUSH);
    }

    // ------------------------------------------------------------------ Spawn-Regeln

    /** Wie die Vanilla-Tiere auf Gras – oder, wo nur Sand ist (Wüste, Tafelberge), auf Sand. */
    public static boolean checkGrassOrSandSpawnRules(EntityType<? extends Animal> type, LevelAccessor level, EntitySpawnReason reason,
            BlockPos pos, RandomSource random) {
        BlockState below = level.getBlockState(pos.below());
        if ((below.is(BlockTags.SAND) || below.is(BlockTags.FOXES_SPAWNABLE_ON) || below.is(Blocks.TERRACOTTA))
                && level.getRawBrightness(pos, 0) > 8) {
            return true;
        }
        return Animal.checkAnimalSpawnRules(type, level, reason, pos, random);
    }
}
