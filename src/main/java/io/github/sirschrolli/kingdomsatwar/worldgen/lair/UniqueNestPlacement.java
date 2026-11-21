package io.github.sirschrolli.kingdomsatwar.worldgen.lair;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;

/**
 * Platzierung „genau einmal pro Welt“: Das Nest eines Drachen liegt in einem passenden Biom in einem Ring um den
 * Weltursprung ({@code min_distance} bis {@code max_distance} Blöcke). Wo genau, berechnet {@link NestLocator}
 * aus dem Startwert der Welt – für alle sechs Nester gemeinsam, damit sie einander nicht zu nahe kommen.
 * {@code inland}: nur im Landesinneren (weg von Küsten und tief liegendem, überflutetem Land).
 */
public record UniqueNestPlacement(DragonType element, int minDistance, int maxDistance, HolderSet<Biome> biomes, boolean inland, int salt)
        implements StructurePlacement {
    public static final MapCodec<UniqueNestPlacement> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            DragonType.CODEC.fieldOf("element").forGetter(UniqueNestPlacement::element),
            Codec.intRange(0, 100000).fieldOf("min_distance").forGetter(UniqueNestPlacement::minDistance),
            Codec.intRange(0, 100000).fieldOf("max_distance").forGetter(UniqueNestPlacement::maxDistance),
            RegistryCodecs.holderSet(Registries.BIOME).fieldOf("biomes").forGetter(UniqueNestPlacement::biomes),
            Codec.BOOL.optionalFieldOf("inland", false).forGetter(UniqueNestPlacement::inland),
            Codec.INT.fieldOf("salt").forGetter(UniqueNestPlacement::salt)
    ).apply(instance, UniqueNestPlacement::new));

    @Override
    public boolean isStructureChunk(ChunkGeneratorStructureState state, int chunkX, int chunkZ) {
        ChunkPos nest = NestLocator.find(state, this.element);
        return nest != null && nest.x() == chunkX && nest.z() == chunkZ;
    }

    @Override
    public MapCodec<UniqueNestPlacement> codec() {
        return CODEC;
    }
}
