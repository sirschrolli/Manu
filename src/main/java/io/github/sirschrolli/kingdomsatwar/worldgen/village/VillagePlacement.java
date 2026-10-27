package io.github.sirschrolli.kingdomsatwar.worldgen.village;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.NestLocator;
import io.github.sirschrolli.kingdomsatwar.worldgen.seal.SealCircleLocator;
import java.util.Optional;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import org.jspecify.annotations.Nullable;

/**
 * Verteilung der Dörfer: ein gewöhnliches Zufallsraster (so finden {@code /locate} und Karten sie), aber nie in der Nähe
 * eines Drachennests – wo ein Drache haust, siedelt niemand – und nicht am Siegelkreis.
 */
public class VillagePlacement extends RandomSpreadStructurePlacement {
    public static final MapCodec<VillagePlacement> CODEC = RecordCodecBuilder.mapCodec(i -> placementCodec(i)
            .and(i.group(
                    Codec.intRange(0, 4096).fieldOf("spacing").forGetter(RandomSpreadStructurePlacement::spacing),
                    Codec.intRange(0, 4096).fieldOf("separation").forGetter(RandomSpreadStructurePlacement::separation),
                    RandomSpreadType.CODEC.optionalFieldOf("spread_type", RandomSpreadType.LINEAR).forGetter(RandomSpreadStructurePlacement::spreadType),
                    Codec.intRange(0, 256).fieldOf("nest_distance").forGetter(VillagePlacement::nestDistance),
                    Codec.intRange(0, 256).fieldOf("seal_distance").forGetter(VillagePlacement::sealDistance)))
            .apply(i, VillagePlacement::new));

    private final int nestDistance;
    private final int sealDistance;

    public VillagePlacement(Vec3i locateOffset, FrequencyReductionMethod frequencyReductionMethod, float frequency, int salt,
            Optional<ExclusionZone> exclusionZone, int spacing, int separation, RandomSpreadType spreadType, int nestDistance, int sealDistance) {
        super(locateOffset, frequencyReductionMethod, frequency, salt, exclusionZone, spacing, separation, spreadType);
        this.nestDistance = nestDistance;
        this.sealDistance = sealDistance;
    }

    public VillagePlacement(int spacing, int separation, int salt, Optional<ExclusionZone> exclusionZone, int nestDistance, int sealDistance) {
        this(Vec3i.ZERO, FrequencyReductionMethod.DEFAULT, 1.0f, salt, exclusionZone, spacing, separation, RandomSpreadType.LINEAR, nestDistance,
                sealDistance);
    }

    /** Mindestabstand (Chunks) zu den Nestern. */
    public int nestDistance() {
        return this.nestDistance;
    }

    /** Mindestabstand (Chunks) zum Siegelkreis. */
    public int sealDistance() {
        return this.sealDistance;
    }

    @Override
    protected boolean isPlacementChunk(ChunkGeneratorStructureState state, int sourceX, int sourceZ) {
        if (!super.isPlacementChunk(state, sourceX, sourceZ)) {
            return false;
        }
        for (DragonType type : DragonType.values()) {
            if (near(NestLocator.find(state, type), sourceX, sourceZ, this.nestDistance)) {
                return false;
            }
        }
        return !near(SealCircleLocator.find(state), sourceX, sourceZ, this.sealDistance);
    }

    private static boolean near(@Nullable ChunkPos pos, int x, int z, int distance) {
        if (pos == null) {
            return false;
        }
        long dx = pos.x() - x;
        long dz = pos.z() - z;
        return dx * dx + dz * dz < (long) distance * distance;
    }

    @Override
    @SuppressWarnings("unchecked")
    public MapCodec<RandomSpreadStructurePlacement> codec() {
        // dieselbe registrierte Codec-Instanz; nur der Typ der Oberklasse verlangt die Umwandlung
        return (MapCodec<RandomSpreadStructurePlacement>) (MapCodec<?>) CODEC;
    }
}
