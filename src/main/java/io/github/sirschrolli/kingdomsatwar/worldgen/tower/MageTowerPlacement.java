package io.github.sirschrolli.kingdomsatwar.worldgen.tower;

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

/**
 * Verteilung der Magiertürme: wie ein gewöhnliches Zufallsraster (so finden {@code /locate} und Karten sie), aber nie in
 * der Nähe eines Drachennests oder des Siegelkreises – deren Gelände gestalten Nest und Kreis selbst. Von den Dörfern
 * hält die Sperrzone der Platzierung Abstand.
 */
public class MageTowerPlacement extends RandomSpreadStructurePlacement {
    public static final MapCodec<MageTowerPlacement> CODEC = RecordCodecBuilder.mapCodec(i -> placementCodec(i)
            .and(i.group(
                    Codec.intRange(0, 4096).fieldOf("spacing").forGetter(RandomSpreadStructurePlacement::spacing),
                    Codec.intRange(0, 4096).fieldOf("separation").forGetter(RandomSpreadStructurePlacement::separation),
                    RandomSpreadType.CODEC.optionalFieldOf("spread_type", RandomSpreadType.LINEAR).forGetter(RandomSpreadStructurePlacement::spreadType),
                    Codec.intRange(0, 256).fieldOf("nest_distance").forGetter(MageTowerPlacement::nestDistance)))
            .apply(i, MageTowerPlacement::new));

    private final int nestDistance;

    public MageTowerPlacement(Vec3i locateOffset, FrequencyReductionMethod frequencyReductionMethod, float frequency, int salt,
            Optional<ExclusionZone> exclusionZone, int spacing, int separation, RandomSpreadType spreadType, int nestDistance) {
        super(locateOffset, frequencyReductionMethod, frequency, salt, exclusionZone, spacing, separation, spreadType);
        this.nestDistance = nestDistance;
    }

    public MageTowerPlacement(int spacing, int separation, int salt, Optional<ExclusionZone> exclusionZone, int nestDistance) {
        this(Vec3i.ZERO, FrequencyReductionMethod.DEFAULT, 1.0f, salt, exclusionZone, spacing, separation, RandomSpreadType.LINEAR, nestDistance);
    }

    /** Mindestabstand (Chunks) zu Nestern und Siegelkreis. */
    public int nestDistance() {
        return this.nestDistance;
    }

    @Override
    protected boolean isPlacementChunk(ChunkGeneratorStructureState state, int sourceX, int sourceZ) {
        if (!super.isPlacementChunk(state, sourceX, sourceZ)) {
            return false;
        }
        for (DragonType type : DragonType.values()) {
            if (this.near(NestLocator.find(state, type), sourceX, sourceZ)) {
                return false;
            }
        }
        return !this.near(SealCircleLocator.find(state), sourceX, sourceZ);
    }

    private boolean near(ChunkPos pos, int x, int z) {
        if (pos == null) {
            return false;
        }
        long dx = pos.x() - x;
        long dz = pos.z() - z;
        return dx * dx + dz * dz < (long) this.nestDistance * this.nestDistance;
    }

    @Override
    @SuppressWarnings("unchecked")
    public MapCodec<RandomSpreadStructurePlacement> codec() {
        // dieselbe registrierte Codec-Instanz; nur der Typ der Oberklasse verlangt die Umwandlung
        return (MapCodec<RandomSpreadStructurePlacement>) (MapCodec<?>) CODEC;
    }
}
