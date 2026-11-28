package io.github.sirschrolli.kingdomsatwar.entity.combat;

import static io.github.sirschrolli.kingdomsatwar.entity.combat.MoveKind.AIMED;
import static io.github.sirschrolli.kingdomsatwar.entity.combat.MoveKind.AMBUSH;
import static io.github.sirschrolli.kingdomsatwar.entity.combat.MoveKind.BACKSTEP;
import static io.github.sirschrolli.kingdomsatwar.entity.combat.MoveKind.BARRAGE;
import static io.github.sirschrolli.kingdomsatwar.entity.combat.MoveKind.BASH;
import static io.github.sirschrolli.kingdomsatwar.entity.combat.MoveKind.BITE;
import static io.github.sirschrolli.kingdomsatwar.entity.combat.MoveKind.BLINK;
import static io.github.sirschrolli.kingdomsatwar.entity.combat.MoveKind.BURST;
import static io.github.sirschrolli.kingdomsatwar.entity.combat.MoveKind.CHARGE;
import static io.github.sirschrolli.kingdomsatwar.entity.combat.MoveKind.CURSE;
import static io.github.sirschrolli.kingdomsatwar.entity.combat.MoveKind.FANGS;
import static io.github.sirschrolli.kingdomsatwar.entity.combat.MoveKind.FEINT;
import static io.github.sirschrolli.kingdomsatwar.entity.combat.MoveKind.FIRE_RING;
import static io.github.sirschrolli.kingdomsatwar.entity.combat.MoveKind.FUSE_LEAP;
import static io.github.sirschrolli.kingdomsatwar.entity.combat.MoveKind.GRAB;
import static io.github.sirschrolli.kingdomsatwar.entity.combat.MoveKind.LUNGE;
import static io.github.sirschrolli.kingdomsatwar.entity.combat.MoveKind.PILLAR;
import static io.github.sirschrolli.kingdomsatwar.entity.combat.MoveKind.ROAR;
import static io.github.sirschrolli.kingdomsatwar.entity.combat.MoveKind.SCREECH;
import static io.github.sirschrolli.kingdomsatwar.entity.combat.MoveKind.SKITTER;
import static io.github.sirschrolli.kingdomsatwar.entity.combat.MoveKind.SLAM;
import static io.github.sirschrolli.kingdomsatwar.entity.combat.MoveKind.SMOKE;
import static io.github.sirschrolli.kingdomsatwar.entity.combat.MoveKind.SPIT;
import static io.github.sirschrolli.kingdomsatwar.entity.combat.MoveKind.SPORES;
import static io.github.sirschrolli.kingdomsatwar.entity.combat.MoveKind.SWEEP;
import static io.github.sirschrolli.kingdomsatwar.entity.combat.MoveKind.THROW;
import static io.github.sirschrolli.kingdomsatwar.entity.combat.MoveKind.VINE;
import static io.github.sirschrolli.kingdomsatwar.entity.combat.MoveKind.VOLLEY;

import io.github.sirschrolli.kingdomsatwar.entity.creature.CaveTrollEntity;
import io.github.sirschrolli.kingdomsatwar.entity.creature.LindwormEntity;
import io.github.sirschrolli.kingdomsatwar.entity.creature.end.Leerwandler;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.BogBones;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Boneman;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Butcher;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Conjurer;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Devastator;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.DrownedCorpse;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.DryBones;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.FrostBones;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Hag;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Highwayman;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Mummy;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Nightmare;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Ooze;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.PlantCreeper;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Revenant;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Trickster;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.UndeadVillager;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.WildCaveSpider;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.WildSpider;
import io.github.sirschrolli.kingdomsatwar.entity.creature.nether.Borstling;
import io.github.sirschrolli.kingdomsatwar.entity.creature.nether.Brandgebein;
import io.github.sirschrolli.kingdomsatwar.entity.creature.nether.BristleGiant;
import io.github.sirschrolli.kingdomsatwar.entity.creature.nether.Glutgeist;
import io.github.sirschrolli.kingdomsatwar.entity.creature.nether.Glutkeiler;
import io.github.sirschrolli.kingdomsatwar.entity.creature.nether.Glutschleim;
import io.github.sirschrolli.kingdomsatwar.entity.creature.nether.Moderkeiler;
import io.github.sirschrolli.kingdomsatwar.entity.creature.nether.Rauchqualle;
import io.github.sirschrolli.kingdomsatwar.entity.creature.nether.RottenBristling;
import io.github.sirschrolli.kingdomsatwar.entity.fae.ThornWightEntity;
import io.github.sirschrolli.kingdomsatwar.entity.monster.AshWraithEntity;
import io.github.sirschrolli.kingdomsatwar.entity.monster.MireShamblerEntity;
import io.github.sirschrolli.kingdomsatwar.entity.monster.NightRamEntity;
import io.github.sirschrolli.kingdomsatwar.entity.monster.RiftHoundEntity;
import io.github.sirschrolli.kingdomsatwar.entity.monster.SoulHeronEntity;
import io.github.sirschrolli.kingdomsatwar.entity.monster.WerewolfEntity;
import io.github.sirschrolli.kingdomsatwar.entity.shadow.CrystalCrawlerEntity;
import io.github.sirschrolli.kingdomsatwar.entity.shadow.ShadeStalkerEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.BanditEntity;
import io.github.sirschrolli.kingdomsatwar.entity.voidrealm.DistortlingEntity;
import io.github.sirschrolli.kingdomsatwar.entity.voidrealm.ShardWardenEntity;
import java.util.List;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.cubemob.AbstractCubeMob;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import org.jspecify.annotations.Nullable;

/**
 * Die Monsterfamilien und ihre besonderen Angriffe. Jedes einzelne Monster beherrscht nur einen Teil davon (zwischen
 * {@link #min()} und {@link #max()}), dazu was sein Temperament mitbringt – so kämpft kaum eines wie das andere.
 */
public enum Family {
    /** Wiedergänger, Mumien, Wasserleichen, untote Dorfbewohner, Moderborstlinge. */
    UNTOTE(0x7A9A4A, Kind.MELEE, 2, 3, List.of(LUNGE, GRAB, SLAM, THROW, ROAR)),
    /** Gerippe, Frost-, Moor- und Dörrgebeine – Bogenschützen. */
    GEBEINE(0xE8E2D0, Kind.RANGED, 2, 3, List.of(VOLLEY, AIMED, BACKSTEP, BASH)),
    /** Brandgebeine – Schwertkämpfer der Festungen. */
    BRANDGEBEIN(0xFF7A1A, Kind.MELEE, 2, 3, List.of(SWEEP, LUNGE, SLAM, THROW)),
    SPINNE(0xB02020, Kind.MELEE, 2, 3, List.of(SPIT, LUNGE, BITE, SKITTER)),
    KRIECHER(0x6FBF3F, Kind.OTHER, 2, 3, List.of(FEINT, FUSE_LEAP, SPORES, VINE)),
    HEXE(0x9B4FC8, Kind.RANGED, 2, 3, List.of(BARRAGE, CURSE, BLINK)),
    PLUENDERER(0x8B5A2B, Kind.RANGED, 2, 3, List.of(VOLLEY, AIMED, BACKSTEP)),
    DIENER(0x5B6B6B, Kind.MELEE, 2, 3, List.of(SWEEP, CHARGE, SLAM, ROAR)),
    BESCHWOERER(0x6A5ACD, Kind.OVERLAY, 1, 2, List.of(FANGS, BLINK)),
    GAUKLER(0x3A7BD5, Kind.RANGED, 1, 2, List.of(VOLLEY, BLINK)),
    VERWUESTER(0x4A4A4A, Kind.MELEE, 2, 3, List.of(CHARGE, SLAM, ROAR)),
    SCHLEIM(0x7ACC5A, Kind.OTHER, 1, 2, List.of(BURST, SPIT)),
    LEERWANDLER(0xA050F0, Kind.MELEE, 1, 2, List.of(AMBUSH, THROW)),
    /** Borstlinge: mit Armbrust oder Schwert – je nachdem, was sie tragen. */
    BORSTLING(0xC9A227, Kind.BRAIN, 2, 3, List.of(SWEEP, LUNGE, ROAR)),
    HUENE(0xB08D57, Kind.BRAIN, 2, 3, List.of(CHARGE, SLAM, SWEEP, ROAR)),
    KEILER(0xA0522D, Kind.BRAIN, 2, 3, List.of(CHARGE, SLAM, ROAR)),
    GLUTGEIST(0xFFB000, Kind.OVERLAY, 2, 3, List.of(FIRE_RING, PILLAR, VOLLEY)),
    QUALLE(0x8A8A8A, Kind.OVERLAY, 1, 2, List.of(VOLLEY, SMOKE)),
    NACHTMAHR(0x4B3F8C, Kind.OVERLAY, 1, 1, List.of(SCREECH)),
    // ---- eigene Monster (sie haben schon ihre eigenen Angriffe; dazu kommt je ein wenig)
    WERWOLF(0x6B5B4B, Kind.MELEE, 1, 2, List.of(SWEEP, GRAB)),
    MOORSCHLURFER(0x4B5B2B, Kind.MELEE, 1, 1, List.of(SLAM)),
    RISSJAEGER(0x9A40E0, Kind.MELEE, 1, 1, List.of(LUNGE)),
    HOEHLENTROLL(0x7A7A6A, Kind.MELEE, 2, 3, List.of(SLAM, SWEEP, THROW)),
    LINDWURM(0x4A8A3A, Kind.MELEE, 1, 2, List.of(LUNGE, BITE, SWEEP)),
    DORNENWICHT(0x5A7A2A, Kind.MELEE, 1, 2, List.of(SWEEP, GRAB)),
    SCHATTENPIRSCHER(0x3A2A5A, Kind.MELEE, 1, 2, List.of(LUNGE, BLINK)),
    KRISTALLKRABBLER(0x9AD0FF, Kind.MELEE, 1, 2, List.of(SKITTER, BITE)),
    SPLITTERWAECHTER(0xB070FF, Kind.MELEE, 1, 2, List.of(SLAM, SWEEP)),
    ZERRLING(0xC040C0, Kind.MELEE, 1, 2, List.of(BLINK, LUNGE)),
    BANDIT(0x8B3A2B, Kind.MELEE, 2, 2, List.of(SWEEP, LUNGE, ROAR)),
    NACHTWIDDER(0x3A3A6A, Kind.MELEE, 0, 0, List.of()),
    SEELENREIHER(0x40C0C0, Kind.OVERLAY, 0, 0, List.of()),
    ASCHENGEIST(0x40B0C0, Kind.OVERLAY, 0, 0, List.of());

    /** Grundart: Nahkämpfer, Schütze, Gehirn-KI (Borstlinge, Keiler), nur nebenher (Flieger, Zauberer) oder Eigenes. */
    public enum Kind {
        MELEE, RANGED, BRAIN, OVERLAY, OTHER
    }

    private final int color;
    private final Kind kind;
    private final int min;
    private final int max;
    private final List<MoveKind> pool;

    Family(int color, Kind kind, int min, int max, List<MoveKind> pool) {
        this.color = color;
        this.kind = kind;
        this.min = min;
        this.max = max;
        this.pool = pool;
    }

    public int color() {
        return this.color;
    }

    public Kind kind() {
        return this.kind;
    }

    public int min() {
        return this.min;
    }

    public int max() {
        return this.max;
    }

    /** Kämpft die Familie aus der Nähe (dann kann sie umkreisen und sich decken)? */
    public boolean melee() {
        return this.kind == Kind.MELEE || this.kind == Kind.BRAIN;
    }

    /** Steuert ein Gehirn (statt Zielen) das Monster? */
    public boolean brain() {
        return this.kind == Kind.BRAIN;
    }

    /** Läuft alles neben dem gewohnten Verhalten her (Flieger, Zauberer)? */
    public boolean overlay() {
        return this.kind == Kind.OVERLAY;
    }

    public boolean flying() {
        return this == GLUTGEIST || this == QUALLE || this == NACHTMAHR || this == SEELENREIHER || this == ASCHENGEIST;
    }

    /** Die Angriffe, aus denen gewählt wird (Borstlinge und Banditen mit Fernwaffe schießen statt zu schlagen). */
    public List<MoveKind> pool(boolean ranged) {
        if (ranged && (this == BORSTLING || this == BANDIT)) {
            return List.of(VOLLEY, AIMED, this == BORSTLING ? ROAR : BACKSTEP);
        }
        return this.pool;
    }

    /** Die Familie eines Monsters oder null (Bosse, Begleiter der Bosse, Reittiere und was zu eigen ist, bleiben, wie sie sind). */
    public static @Nullable Family of(Mob mob) {
        if (mob instanceof Revenant || mob instanceof Mummy || mob instanceof DrownedCorpse || mob instanceof UndeadVillager
                || mob instanceof RottenBristling) {
            return UNTOTE;
        }
        if (mob instanceof Boneman || mob instanceof FrostBones || mob instanceof BogBones || mob instanceof DryBones) {
            return GEBEINE;
        }
        if (mob instanceof Brandgebein) {
            return BRANDGEBEIN;
        }
        if (mob instanceof WildSpider || mob instanceof WildCaveSpider) {
            return SPINNE;
        }
        if (mob instanceof PlantCreeper) {
            return KRIECHER;
        }
        if (mob instanceof Hag) {
            return HEXE;
        }
        if (mob instanceof Highwayman) {
            return PLUENDERER;
        }
        if (mob instanceof Butcher) {
            return DIENER;
        }
        if (mob instanceof Conjurer) {
            return BESCHWOERER;
        }
        if (mob instanceof Trickster) {
            return GAUKLER;
        }
        if (mob instanceof Devastator) {
            return VERWUESTER;
        }
        if (mob instanceof Ooze || mob instanceof Glutschleim) {
            return SCHLEIM;
        }
        if (mob instanceof Leerwandler) {
            return LEERWANDLER;
        }
        if (mob instanceof Borstling) {
            return BORSTLING;
        }
        if (mob instanceof BristleGiant) {
            return HUENE;
        }
        if (mob instanceof Glutkeiler || mob instanceof Moderkeiler) {
            return KEILER;
        }
        if (mob instanceof Glutgeist) {
            return GLUTGEIST;
        }
        if (mob instanceof Rauchqualle) {
            return QUALLE;
        }
        if (mob instanceof Nightmare) {
            return NACHTMAHR;
        }
        if (mob instanceof WerewolfEntity) {
            return WERWOLF;
        }
        if (mob instanceof MireShamblerEntity) {
            return MOORSCHLURFER;
        }
        if (mob instanceof RiftHoundEntity) {
            return RISSJAEGER;
        }
        if (mob instanceof CaveTrollEntity) {
            return HOEHLENTROLL;
        }
        if (mob instanceof LindwormEntity) {
            return LINDWURM;
        }
        if (mob instanceof ThornWightEntity) {
            return DORNENWICHT;
        }
        if (mob instanceof ShadeStalkerEntity) {
            return SCHATTENPIRSCHER;
        }
        if (mob instanceof CrystalCrawlerEntity) {
            return KRISTALLKRABBLER;
        }
        if (mob instanceof ShardWardenEntity) {
            return SPLITTERWAECHTER;
        }
        if (mob instanceof DistortlingEntity) {
            return ZERRLING;
        }
        if (mob instanceof BanditEntity) {
            return BANDIT;
        }
        if (mob instanceof NightRamEntity) {
            return NACHTWIDDER;
        }
        if (mob instanceof SoulHeronEntity) {
            return SEELENREIHER;
        }
        if (mob instanceof AshWraithEntity) {
            return ASCHENGEIST;
        }
        return null;
    }

    /** Kämpft dieses Monster aus der Ferne (Bogen, Armbrust)? */
    public static boolean ranged(Mob mob, Family family) {
        return switch (family) {
            case GEBEINE, PLUENDERER, GAUKLER, HEXE -> true;
            case BORSTLING, BANDIT -> mob.getMainHandItem().getItem() instanceof CrossbowItem
                    || mob.getMainHandItem().getItem() instanceof BowItem;
            default -> false;
        };
    }

    /** Schleime nur ab mittlerer Größe (die winzigen haben nichts Besonderes). */
    /**
     * Hat das Monster ein eigenes Tempo (der Schattenpirscher schleicht nur, wenn man ihn ansieht, und hetzt sonst)? Dann
     * ändern weder Temperament noch Raserei sein Tempo – sonst ginge sein Kniff verloren.
     */
    public static boolean ownPace(Mob mob) {
        return mob instanceof ShadeStalkerEntity;
    }

    public static boolean big(Mob mob) {
        return !(mob instanceof AbstractCubeMob cube) || cube.getSize() >= 2;
    }
}
