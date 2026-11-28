package io.github.sirschrolli.kingdomsatwar.worldgen.kraken;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;

/**
 * Platzierung des Krakenschlunds (seit 0.91): genau einmal im Urmeer, im offenen, tiefen Meer ({@code biomes}) in einem Ring
 * um die Mitte ({@code min_distance} bis {@code max_distance} Blöcke). Den Ort berechnet {@link KrakenMawLocator}.
 */
public record KrakenMawPlacement(int minDistance, int maxDistance, HolderSet<Biome> biomes, int salt) implements StructurePlacement {
    public static final MapCodec<KrakenMawPlacement> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.intRange(0, 100000).fieldOf("min_distance").forGetter(KrakenMawPlacement::minDistance),
            Codec.intRange(0, 100000).fieldOf("max_distance").forGetter(KrakenMawPlacement::maxDistance),
            RegistryCodecs.holderSet(Registries.BIOME).fieldOf("biomes").forGetter(KrakenMawPlacement::biomes),
            Codec.INT.fieldOf("salt").forGetter(KrakenMawPlacement::salt)
    ).apply(instance, KrakenMawPlacement::new));

    @Override
    public boolean isStructureChunk(ChunkGeneratorStructureState state, int chunkX, int chunkZ) {
        ChunkPos maw = KrakenMawLocator.find(state, this);
        return maw != null && maw.x() == chunkX && maw.z() == chunkZ;
    }

    @Override
    public MapCodec<KrakenMawPlacement> codec() {
        return CODEC;
    }
}
