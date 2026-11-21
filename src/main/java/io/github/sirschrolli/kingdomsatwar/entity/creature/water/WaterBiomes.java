package io.github.sirschrolli.kingdomsatwar.entity.creature.water;

import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

/** Meere nach Wassertemperatur – danach richten sich die Arten der Wassertiere. */
public final class WaterBiomes {
    private WaterBiomes() {}

    public static boolean cold(Holder<Biome> biome) {
        return biome.is(Biomes.COLD_OCEAN) || biome.is(Biomes.DEEP_COLD_OCEAN) || biome.is(Biomes.FROZEN_OCEAN)
                || biome.is(Biomes.DEEP_FROZEN_OCEAN) || biome.is(Biomes.FROZEN_RIVER) || biome.is(Biomes.SNOWY_BEACH);
    }

    public static boolean warm(Holder<Biome> biome) {
        return biome.is(Biomes.WARM_OCEAN) || biome.is(Biomes.LUKEWARM_OCEAN) || biome.is(Biomes.DEEP_LUKEWARM_OCEAN)
                || biome.is(BiomeTags.IS_BEACH) && !biome.is(Biomes.SNOWY_BEACH)
                || biome.is(io.github.sirschrolli.kingdomsatwar.worldgen.overworld.OverworldBiomes.CORAL_COAST);
    }
}
