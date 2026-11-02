package io.github.sirschrolli.kingdomsatwar.worldgen.site;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures;
import java.util.Optional;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

/**
 * Ein eigenes Bauwerk der Oberwelt ({@link SiteKind}). Wo es Platz findet und wie es sich zusammensetzt, plant die
 * jeweilige Art aus dem Gelände; gebaut wird es von {@link SitePiece}.
 */
public class SiteStructure extends Structure {
    public static final MapCodec<SiteStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            settingsCodec(instance),
            SiteKind.CODEC.fieldOf("kind").forGetter(s -> s.kind)
    ).apply(instance, SiteStructure::new));

    private final SiteKind kind;

    public SiteStructure(Structure.StructureSettings settings, SiteKind kind) {
        super(settings);
        this.kind = kind;
    }

    public SiteKind kind() {
        return this.kind;
    }

    @Override
    protected Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext context) {
        SiteTerrain terrain = new SiteTerrain(context);
        long seed = context.random().nextLong();
        return switch (this.kind) {
            case FALLEN_STAR -> FallenStar.plan(context, terrain, seed);
            case AQUEDUCT -> Aqueduct.plan(context, terrain, seed);
            case TIDE_SHRINE -> TideShrine.plan(context, terrain, seed);
            case SEA_DRAGON -> SeaDragon.plan(context, terrain, seed);
            case CARAVANSERAI -> Caravanserai.plan(context, terrain, seed);
            case SERPENT_TEMPLE -> SerpentTemple.plan(context, terrain, seed);
            case WITCH_TREE -> WitchTree.plan(context, terrain, seed);
            case FROST_GROTTO -> FrostGrotto.plan(context, terrain, seed);
            case BANDIT_CAMP -> BanditCamp.plan(context, terrain, seed);
            case CHARCOAL_CAMP -> CharcoalCamp.plan(context, terrain, seed);
            case EMBER_GATE -> EmberGate.plan(context, terrain, seed);
            case CLAM_FIELD -> ClamField.plan(context, terrain, seed);
            case CORAL_PALACE -> CoralPalace.plan(context, terrain, seed);
            case BURIED_ABBEY -> BuriedAbbey.plan(context, terrain, seed);
            case SILENT_CATHEDRAL -> SilentCathedral.plan(context, terrain, seed);
            case TRIAL_ARENA -> TrialArena.plan(context, terrain, seed);
            case ROBBER_CASTLE -> RobberCastle.plan(context, terrain, seed);
            case ASH_FORGE -> AshForge.plan(context, terrain, seed);
        };
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.SITE.get();
    }
}
