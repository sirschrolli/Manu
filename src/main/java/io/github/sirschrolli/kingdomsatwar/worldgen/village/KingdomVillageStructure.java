package io.github.sirschrolli.kingdomsatwar.worldgen.village;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

/**
 * Ein Dorf in einer der vier Bauweisen (Fachwerk, nordisch, Lehm, Pfahlbau). Geplant wird es ganz am Anfang aus den
 * Höhen des Geländegenerators (siehe {@link VillagePlanner}); taugt der Ort nicht (zu steil, zu nass, zu wenig Platz),
 * entsteht hier kein Dorf.
 */
public class KingdomVillageStructure extends Structure {
    public static final MapCodec<KingdomVillageStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            settingsCodec(instance),
            VillageStyle.CODEC.fieldOf("style").forGetter(KingdomVillageStructure::style)
    ).apply(instance, KingdomVillageStructure::new));

    private final VillageStyle style;

    public KingdomVillageStructure(Structure.StructureSettings settings, VillageStyle style) {
        super(settings);
        this.style = style;
    }

    /** Nur für die Entwicklung: Bericht über jede geplante Siedlung (welche Bauplätze warum abgelehnt wurden). */
    public static void debug(java.util.function.@org.jspecify.annotations.Nullable Consumer<String> consumer) {
        VillagePlanner.debug = consumer;
    }

    public VillageStyle style() {
        return this.style;
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.VILLAGE.get();
    }

    @Override
    protected Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext context) {
        // schneller Vorab-Test: ohne passendes Biom in der Chunkmitte muss das Gelände gar nicht erst geplant werden
        if (!context.couldValidBiomeExistOnTopOfChunkCenter()) {
            return Optional.empty();
        }
        ChunkPos chunk = context.chunkPos();
        VillageTerrain terrain = new VillageTerrain(context);
        long seed = context.random().nextLong();
        List<KingdomVillagePiece> pieces = VillagePlanner.plan(this.style, chunk.getMiddleBlockX(), chunk.getMiddleBlockZ(), seed, terrain);
        if (pieces.isEmpty()) {
            return Optional.empty();
        }
        KingdomVillagePiece square = pieces.get(0);
        return Optional.of(new Structure.GenerationStub(square.origin(), builder -> pieces.forEach(builder::addPiece)));
    }
}
