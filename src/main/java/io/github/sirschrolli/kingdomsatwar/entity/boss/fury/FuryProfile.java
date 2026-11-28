package io.github.sirschrolli.kingdomsatwar.entity.boss.fury;

import static io.github.sirschrolli.kingdomsatwar.entity.boss.fury.FuryAttack.BEAMS;
import static io.github.sirschrolli.kingdomsatwar.entity.boss.fury.FuryAttack.LINES;
import static io.github.sirschrolli.kingdomsatwar.entity.boss.fury.FuryAttack.PULL;
import static io.github.sirschrolli.kingdomsatwar.entity.boss.fury.FuryAttack.RAIN;
import static io.github.sirschrolli.kingdomsatwar.entity.boss.fury.FuryAttack.SECTORS;
import static io.github.sirschrolli.kingdomsatwar.entity.boss.fury.FuryAttack.WAVES;

import io.github.sirschrolli.kingdomsatwar.entity.abyss.HourWardenEntity;
import io.github.sirschrolli.kingdomsatwar.entity.abyss.WorldEaterEntity;
import io.github.sirschrolli.kingdomsatwar.entity.arena.VentorEntity;
import io.github.sirschrolli.kingdomsatwar.entity.boss.earth.TerragonEntity;
import io.github.sirschrolli.kingdomsatwar.entity.boss.fire.PyrrhonEntity;
import io.github.sirschrolli.kingdomsatwar.entity.boss.ice.GlaciaraEntity;
import io.github.sirschrolli.kingdomsatwar.entity.boss.lightning.VoltarionEntity;
import io.github.sirschrolli.kingdomsatwar.entity.boss.mage.MageBossEntity;
import io.github.sirschrolli.kingdomsatwar.entity.boss.nature.VerdantisEntity;
import io.github.sirschrolli.kingdomsatwar.entity.boss.shadow.NyxarEntity;
import io.github.sirschrolli.kingdomsatwar.entity.boss.water.ThalassaEntity;
import io.github.sirschrolli.kingdomsatwar.entity.castle.RobberKnightEntity;
import io.github.sirschrolli.kingdomsatwar.entity.creator.SchrolliEntity;
import io.github.sirschrolli.kingdomsatwar.entity.deep.SordinEntity;
import io.github.sirschrolli.kingdomsatwar.entity.end.AsteriaEntity;
import io.github.sirschrolli.kingdomsatwar.entity.end.PhasmorEntity;
import io.github.sirschrolli.kingdomsatwar.entity.fae.MissMystikEntity;
import io.github.sirschrolli.kingdomsatwar.entity.fae.MothQueenEntity;
import io.github.sirschrolli.kingdomsatwar.entity.fae.StoneKeeperEntity;
import io.github.sirschrolli.kingdomsatwar.entity.glitch.AnniTrollEntity;
import io.github.sirschrolli.kingdomsatwar.entity.nether.GlutschmiedEntity;
import io.github.sirschrolli.kingdomsatwar.entity.nether.IgnaraEntity;
import io.github.sirschrolli.kingdomsatwar.entity.realm.RealmWarriorEntity;
import io.github.sirschrolli.kingdomsatwar.entity.sea.KrakenEntity;
import io.github.sirschrolli.kingdomsatwar.entity.sea.ReefColossusEntity;
import io.github.sirschrolli.kingdomsatwar.entity.sea.SeaWitchEntity;
import io.github.sirschrolli.kingdomsatwar.entity.serpent.QuetzaraEntity;
import io.github.sirschrolli.kingdomsatwar.entity.shadow.SoulShepherdEntity;
import io.github.sirschrolli.kingdomsatwar.entity.voidrealm.ShatteredKingEntity;
import io.github.sirschrolli.kingdomsatwar.entity.voidrealm.StarWeaverEntity;
import java.util.List;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

/**
 * Welche Zornangriffe ein Boss hat: sein Element und zwei Angriffe, die zu ihm passen (und keinem seiner eigenen
 * gleichen). In der Zornphase kommt bei allen die {@link FuryAttack#NOVA} dazu.
 */
public record FuryProfile(FuryElement element, List<FuryAttack> attacks) {
    static FuryProfile of(FuryElement element, FuryAttack a, FuryAttack b) {
        return new FuryProfile(element, List.of(a, b));
    }

    /** Das Profil eines Bosses (null: kein Boss mit Zornangriffen). */
    public static @Nullable FuryProfile of(LivingEntity boss) {
        return switch (boss) {
            // die Drachen
            case PyrrhonEntity _ -> of(FuryElement.FIRE, SECTORS, LINES);
            case GlaciaraEntity _ -> of(FuryElement.ICE, BEAMS, SECTORS);
            case VoltarionEntity _ -> of(FuryElement.LIGHTNING, BEAMS, RAIN);
            case VerdantisEntity _ -> of(FuryElement.NATURE, LINES, PULL);
            case TerragonEntity _ -> of(FuryElement.EARTH, WAVES, LINES);
            case ThalassaEntity _ -> of(FuryElement.WATER, PULL, WAVES);
            case NyxarEntity _ -> of(FuryElement.SHADOW, BEAMS, RAIN);
            // die Erzmagier, je nach Element
            case MageBossEntity mage -> switch (FuryElement.of(mage.element())) {
                case FIRE -> of(FuryElement.FIRE, RAIN, WAVES);
                case ICE -> of(FuryElement.ICE, SECTORS, BEAMS);
                case LIGHTNING -> of(FuryElement.LIGHTNING, BEAMS, RAIN);
                case NATURE -> of(FuryElement.NATURE, LINES, PULL);
                case EARTH -> of(FuryElement.EARTH, WAVES, LINES);
                default -> of(FuryElement.WATER, PULL, SECTORS);
            };
            // die übrigen Bosse
            case PhasmorEntity _ -> of(FuryElement.VOID, PULL, SECTORS);
            case QuetzaraEntity _ -> of(FuryElement.POISON, SECTORS, BEAMS);
            case StoneKeeperEntity _ -> of(FuryElement.EARTH, RAIN, LINES);
            case SordinEntity _ -> of(FuryElement.SOUND, WAVES, BEAMS);
            case GlutschmiedEntity _ -> of(FuryElement.FIRE, RAIN, WAVES);
            case IgnaraEntity _ -> of(FuryElement.FIRE, BEAMS, PULL);
            case MothQueenEntity _ -> of(FuryElement.LIGHT, RAIN, SECTORS);
            case ShatteredKingEntity _ -> of(FuryElement.VOID, LINES, RAIN);
            case ReefColossusEntity _ -> of(FuryElement.WATER, LINES, SECTORS);
            case AnniTrollEntity _ -> of(FuryElement.EARTH, RAIN, WAVES);
            case AsteriaEntity _ -> of(FuryElement.LIGHT, BEAMS, PULL);
            case MissMystikEntity _ -> of(FuryElement.LIGHT, SECTORS, BEAMS);
            case SoulShepherdEntity _ -> of(FuryElement.SHADOW, LINES, PULL);
            case StarWeaverEntity _ -> of(FuryElement.LIGHT, LINES, RAIN);
            case WorldEaterEntity _ -> of(FuryElement.VOID, PULL, WAVES);
            // die Zeiger der großen Uhr kreisen, die Felder des Zifferblatts brechen auf
            case HourWardenEntity _ -> of(FuryElement.STEEL, BEAMS, SECTORS);
            case KrakenEntity _ -> of(FuryElement.WATER, BEAMS, RAIN);
            case SeaWitchEntity _ -> of(FuryElement.WATER, RAIN, PULL);
            case RobberKnightEntity _ -> of(FuryElement.STEEL, RAIN, LINES);
            case VentorEntity _ -> of(FuryElement.WIND, BEAMS, PULL);
            case RealmWarriorEntity b -> b.isPartyBoss() ? of(FuryElement.STEEL, WAVES, LINES) : null;
            case SchrolliEntity _ -> of(FuryElement.LIGHT, RAIN, SECTORS);
            default -> null;
        };
    }
}
