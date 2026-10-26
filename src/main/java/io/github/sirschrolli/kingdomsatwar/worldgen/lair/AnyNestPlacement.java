package io.github.sirschrolli.kingdomsatwar.worldgen.lair;

import com.mojang.serialization.MapCodec;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;

/**
 * Trifft auf die Chunks aller sechs Drachennester zu. Wird nur als Bezug für „Sperrzonen“ anderer Bauwerke genutzt
 * (Strukturgruppe {@code kingdomsatwar:dragon_lairs} ohne eigene Bauwerke): So halten z. B. Dörfer über ihre
 * {@code exclusion_zone} Abstand zu den Nestern.
 */
public record AnyNestPlacement() implements StructurePlacement {
    public static final MapCodec<AnyNestPlacement> CODEC = MapCodec.unit(AnyNestPlacement::new);

    @Override
    public boolean isStructureChunk(ChunkGeneratorStructureState state, int chunkX, int chunkZ) {
        for (DragonType type : DragonType.values()) {
            ChunkPos nest = NestLocator.find(state, type);
            if (nest != null && nest.x() == chunkX && nest.z() == chunkZ) {
                return true;
            }
        }
        return false;
    }

    @Override
    public MapCodec<AnyNestPlacement> codec() {
        return CODEC;
    }
}
