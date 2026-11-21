package io.github.sirschrolli.kingdomsatwar.worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;

/**
 * Fügt Spawns in einer bestimmten Spawn-Kategorie hinzu – wie NeoForges {@code add_spawns}, nur dass die Kategorie
 * frei wählbar ist. Gebraucht für Luchs und Waldkatze: Sie erscheinen wie der Vanilla-Ozelot im Durchgang der Monster
 * (laufend statt nur beim Erzeugen der Welt), zählen aber als friedliche Tiere.
 */
public record AddCategorySpawnsBiomeModifier(HolderSet<Biome> biomes, MobCategory category,
        WeightedList<MobSpawnSettings.SpawnerData> spawners) implements BiomeModifier {
    public static final MapCodec<AddCategorySpawnsBiomeModifier> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Biome.LIST_CODEC.fieldOf("biomes").forGetter(AddCategorySpawnsBiomeModifier::biomes),
            MobCategory.CODEC.fieldOf("category").forGetter(AddCategorySpawnsBiomeModifier::category),
            WeightedList.codec(MobSpawnSettings.SpawnerData.CODEC).fieldOf("spawners").forGetter(AddCategorySpawnsBiomeModifier::spawners)
    ).apply(i, AddCategorySpawnsBiomeModifier::new));

    @Override
    public void modify(RegistryAccess registries, Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase == Phase.ADD && this.biomes.contains(biome)) {
            for (Weighted<MobSpawnSettings.SpawnerData> spawner : this.spawners.unwrap()) {
                builder.getMobSpawnSettings().addSpawn(spawner.value().type(), this.category, spawner.weight(), spawner.value().count());
            }
        }
    }

    @Override
    public MapCodec<? extends BiomeModifier> codec() {
        return ModFeatureTypes.ADD_CATEGORY_SPAWNS.get();
    }
}
