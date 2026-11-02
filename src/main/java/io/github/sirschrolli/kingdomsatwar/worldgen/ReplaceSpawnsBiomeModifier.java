package io.github.sirschrolli.kingdomsatwar.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.MobSpawnSettingsBuilder;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;

/**
 * Tauscht in den Spawn-Listen der Biome Lebewesen gegen andere aus – mit genau dem Gewicht, der Gruppengröße und in
 * der Spawn-Kategorie des Originals (und mit seinen Spawn-Kosten). So erscheinen die eigenen Wildtiere überall dort, wo Vanilla seine Wildtiere
 * vorsieht, auch in Biomen anderer Mods, ohne dass jedes Biom einzeln aufgezählt werden muss.
 *
 * <p>JSON: {@code {"type": "kingdomsatwar:replace_spawns", "biomes": "#minecraft:is_overworld",
 * "replacements": {"minecraft:wolf": "kingdomsatwar:wolf", …}}}
 */
public record ReplaceSpawnsBiomeModifier(HolderSet<Biome> biomes, Map<EntityType<?>, EntityType<?>> replacements)
        implements BiomeModifier {
    public static final MapCodec<ReplaceSpawnsBiomeModifier> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Biome.LIST_CODEC.fieldOf("biomes").forGetter(ReplaceSpawnsBiomeModifier::biomes),
            Codec.unboundedMap(BuiltInRegistries.ENTITY_TYPE.byNameCodec(), BuiltInRegistries.ENTITY_TYPE.byNameCodec())
                    .fieldOf("replacements").forGetter(ReplaceSpawnsBiomeModifier::replacements)
    ).apply(i, ReplaceSpawnsBiomeModifier::new));

    @Override
    public void modify(RegistryAccess registries, Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase != Phase.MODIFY || !this.biomes.contains(biome)) {
            return;
        }
        MobSpawnSettingsBuilder spawns = builder.getMobSpawnSettings();
        for (MobCategory category : List.copyOf(spawns.getSpawnerTypes())) {
            WeightedList.Builder<MobSpawnSettings.SpawnerData> list = spawns.getSpawner(category);
            if (list == null) {
                continue;
            }
            List<Weighted<MobSpawnSettings.SpawnerData>> found = new ArrayList<>();
            list.removeIf(entry -> {
                if (this.replacements.containsKey(entry.value().type())) {
                    found.add(entry);
                    return true;
                }
                return false;
            });
            for (Weighted<MobSpawnSettings.SpawnerData> entry : found) {
                EntityType<?> type = this.replacements.get(entry.value().type());
                list.add(new MobSpawnSettings.SpawnerData(type, entry.value().count()), entry.weight());
            }
        }
        // auch die Spawn-Kosten (im Seelensandtal etwa für Schreiter, Skelett, Ghast und Enderman) gehen auf den Ersatz über
        for (Map.Entry<EntityType<?>, EntityType<?>> swap : this.replacements.entrySet()) {
            MobSpawnSettings.MobSpawnCost cost = spawns.getCost(swap.getKey());
            if (cost != null) {
                spawns.removeSpawnCost(swap.getKey());
                spawns.addMobSpawnCost(swap.getValue(), cost.charge(), cost.energyBudget());
            }
        }
    }

    @Override
    public MapCodec<? extends BiomeModifier> codec() {
        return ModFeatureTypes.REPLACE_SPAWNS.get();
    }
}
