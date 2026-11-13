package io.github.sirschrolli.kingdomsatwar.worldgen.realm;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.sirschrolli.kingdomsatwar.war.RealmLocator;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;

/**
 * Platzierung der Stammburgen: genau eine je Reich, in einem Ring um den Weltursprung ({@code min_distance} bis
 * {@code max_distance} Blöcke) auf ebenem, offenem Land. Die Orte berechnet {@link RealmLocator}; welches Reich in
 * einem Chunk sitzt, liest das Bauwerk dort nach.
 */
public record RealmCastlePlacement(int minDistance, int maxDistance, HolderSet<Biome> biomes, int salt) implements StructurePlacement {
    public static final MapCodec<RealmCastlePlacement> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.intRange(0, 100000).fieldOf("min_distance").forGetter(RealmCastlePlacement::minDistance),
            Codec.intRange(0, 100000).fieldOf("max_distance").forGetter(RealmCastlePlacement::maxDistance),
            RegistryCodecs.holderSet(Registries.BIOME).fieldOf("biomes").forGetter(RealmCastlePlacement::biomes),
            Codec.INT.fieldOf("salt").forGetter(RealmCastlePlacement::salt)
    ).apply(instance, RealmCastlePlacement::new));

    @Override
    public boolean isStructureChunk(ChunkGeneratorStructureState state, int chunkX, int chunkZ) {
        return RealmLocator.at(state, chunkX, chunkZ) != null;
    }

    @Override
    public MapCodec<RealmCastlePlacement> codec() {
        return CODEC;
    }
}
