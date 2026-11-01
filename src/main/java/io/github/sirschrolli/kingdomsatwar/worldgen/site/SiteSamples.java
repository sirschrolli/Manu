package io.github.sirschrolli.kingdomsatwar.worldgen.site;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/**
 * Musterstücke der eigenen Bauwerke mit festen Werten (statt aus dem Gelände geplant) – für die Spieltests und die
 * Vorführung, damit sie genau das bauen, was die Welt bauen würde.
 */
public final class SiteSamples {
    private SiteSamples() {}

    /**
     * Ein Sternensturz mit der Mitte bei {@code center} (y = Höhe des Landes). {@code floor}: Boden der Kernkammer
     * (eigene Höhe, in der Welt −30 oder tiefer); {@code angle}: wo der Steg beginnt (Bogenmaß); {@code camp}: Achse des
     * Lagers (0 Norden, 1 Osten, 2 Süden, 3 Westen).
     */
    public static SitePiece fallenStar(BlockPos center, boolean rock, int floor, double angle, int camp, int seaLevel) {
        BoundingBox box = new BoundingBox(center.getX() - FallenStar.REACH, center.getY() + floor - 3, center.getZ() - FallenStar.REACH,
                center.getX() + FallenStar.REACH, center.getY() + 24, center.getZ() + FallenStar.REACH);
        return new SitePiece(SiteKind.FALLEN_STAR, 0, center, Rotation.NONE, 0x57A2L,
                new int[] {rock ? FallenStar.ROCK : FallenStar.CRATER, floor, (int) Math.round(angle * 1000), seaLevel, camp}, box);
    }

    /**
     * Ein Stück Wasserstollen: die Brunnenkammer bei {@code start} (Schacht {@code shaft} Blöcke hoch, mit Brunnenhaus),
     * nach Süden ein Kanal (12 lang, mit Zulauf und Nische), dahinter eine Zisterne, von dort nach Osten eine Treppe hinab
     * und ein Kanal, der an einem Gitter endet.
     */
    public static List<SitePiece> aqueduct(BlockPos start, int shaft) {
        BlockPos tunnel = start.relative(Direction.SOUTH, 7);
        int len = 12;
        BlockPos cistern = tunnel.relative(Direction.SOUTH, len + 7);
        BlockPos stairs = cistern.relative(Direction.EAST, 7);
        BlockPos east = stairs.relative(Direction.EAST, 8).below(4);
        return List.of(
                new SitePiece(SiteKind.AQUEDUCT, Aqueduct.WELL, start, Rotation.NONE, 21L, new int[] {Aqueduct.bit(Direction.SOUTH), shaft},
                        new BoundingBox(start.getX() - 8, start.getY() - 2, start.getZ() - 8, start.getX() + 8, start.getY() + Math.max(8, shaft + 6),
                                start.getZ() + 8)),
                new SitePiece(SiteKind.AQUEDUCT, Aqueduct.TUNNEL, tunnel, Rotation.NONE, 22L, new int[] {len, Aqueduct.INLETS | Aqueduct.ALCOVE | 16},
                        SitePiece.box(tunnel, Rotation.NONE, -5, -2, 0, 5, 6, len - 1)),
                new SitePiece(SiteKind.AQUEDUCT, Aqueduct.CISTERN, cistern, Rotation.NONE, 23L,
                        new int[] {Aqueduct.bit(Direction.NORTH) | Aqueduct.bit(Direction.EAST)},
                        new BoundingBox(cistern.getX() - 8, cistern.getY() - 4, cistern.getZ() - 8, cistern.getX() + 8, cistern.getY() + 9,
                                cistern.getZ() + 8)),
                new SitePiece(SiteKind.AQUEDUCT, Aqueduct.STAIRS, stairs, Rotation.COUNTERCLOCKWISE_90, 24L, new int[] {8, -1},
                        SitePiece.box(stairs, Rotation.COUNTERCLOCKWISE_90, -4, -6, 0, 4, 6, 7)),
                new SitePiece(SiteKind.AQUEDUCT, Aqueduct.TUNNEL, east, Rotation.COUNTERCLOCKWISE_90, 25L, new int[] {12, Aqueduct.END},
                        SitePiece.box(east, Rotation.COUNTERCLOCKWISE_90, -5, -2, 0, 5, 6, 11)));
    }

    /** Die Gebeine eines Seedrachen (Ursprung: Höhe direkt über dem Grund, am Ansatz des Halses); unter {@code sea} Wasser. */
    public static SitePiece seaDragon(BlockPos origin, Rotation rotation, boolean sunken, int sea) {
        return new SitePiece(SiteKind.SEA_DRAGON, 0, origin, rotation, 0x5EAL, new int[] {sunken ? 1 : 0, sea},
                SitePiece.box(origin, rotation, -16, -5, -13, 16, 13, SeaDragon.LENGTH + 2));
    }

    /** Eine Karawanserei mit der Mitte des Hofs bei {@code center} (y = Boden), das Tor im Süden. */
    public static SitePiece caravanserai(BlockPos center) {
        int h = Caravanserai.HALF;
        return new SitePiece(SiteKind.CARAVANSERAI, 0, center, Rotation.NONE, 0xCA2AL, new int[0],
                new BoundingBox(center.getX() - h - 1, center.getY() - 9, center.getZ() - h - 1, center.getX() + h + 1, center.getY() + 12,
                        center.getZ() + h + 1));
    }

    /** Ein Schlangentempel: die Schnauze bei {@code origin} (y = Boden), der Kopf liegt nach Süden. */
    public static SitePiece serpentTemple(BlockPos origin) {
        return new SitePiece(SiteKind.SERPENT_TEMPLE, 0, origin, Rotation.NONE, 0x5E2BL, new int[0],
                SitePiece.box(origin, Rotation.NONE, -17, -10, -13, 17, 32, 31));
    }

    /** Ein Hexenbaum mit der Mitte des Stamms bei {@code origin} (y = Boden der Hütte), die Tür im Süden. */
    public static SitePiece witchTree(BlockPos origin) {
        return new SitePiece(SiteKind.WITCH_TREE, 0, origin, Rotation.NONE, 0xEC5L, new int[0],
                new BoundingBox(origin.getX() - 12, origin.getY() - 4, origin.getZ() - 12, origin.getX() + 12, origin.getY() + 22, origin.getZ() + 12));
    }

    /** Eine Frostgrotte mit der Mitte der Stube bei {@code origin} (y = Boden), der Eingang im Süden. */
    public static SitePiece frostGrotto(BlockPos origin) {
        return new SitePiece(SiteKind.FROST_GROTTO, 0, origin, Rotation.NONE, 0xF20L, new int[0],
                SitePiece.box(origin, Rotation.NONE, -10, -7, -10, 10, 9, 13));
    }

    /** Ein Räuberlager mit der Mitte bei {@code origin} (y = über dem Boden), das Tor im Süden. */
    public static SitePiece banditCamp(BlockPos origin) {
        return new SitePiece(SiteKind.BANDIT_CAMP, 0, origin, Rotation.NONE, 0xBA4DL, new int[] {0},
                new BoundingBox(origin.getX() - 13, origin.getY() - 5, origin.getZ() - 13, origin.getX() + 13, origin.getY() + 10, origin.getZ() + 13));
    }

    /** Eine Köhlerei mit der Mitte der Lichtung bei {@code origin} (y = über dem Boden); {@code wood}: Holzart 0–7. */
    public static SitePiece charcoalCamp(BlockPos origin, int wood) {
        return new SitePiece(SiteKind.CHARCOAL_CAMP, 0, origin, Rotation.NONE, 0xC0A1L, new int[] {wood},
                new BoundingBox(origin.getX() - 11, origin.getY() - 5, origin.getZ() - 11, origin.getX() + 11, origin.getY() + 13, origin.getZ() + 11));
    }

    /** Eine Glutpforte mit der Mitte vor dem Tor bei {@code origin} (y = über dem Boden); das Tor steht bei z = −2. */
    public static SitePiece emberGate(BlockPos origin) {
        return new SitePiece(SiteKind.EMBER_GATE, 0, origin, Rotation.NONE, 0xE3BL, new int[0],
                new BoundingBox(origin.getX() - 9, origin.getY() - 4, origin.getZ() - 9, origin.getX() + 9, origin.getY() + 10, origin.getZ() + 9));
    }

    /** Ein Muschelfeld mit der Mitte bei {@code origin} (y = oberster Block des Meeresgrunds); warm: Sand und Korallen. */
    public static SitePiece clamField(BlockPos origin, boolean warm) {
        return new SitePiece(SiteKind.CLAM_FIELD, 0, origin, Rotation.NONE, 0xC1A3L, new int[] {warm ? 1 : 0},
                new BoundingBox(origin.getX() - 17, origin.getY() - 4, origin.getZ() - 17, origin.getX() + 17, origin.getY() + 10, origin.getZ() + 17));
    }

    /** Ein Korallenpalast mit der Mitte bei {@code origin} (y = direkt über dem Meeresgrund). */
    public static SitePiece coralPalace(BlockPos origin) {
        int h = CoralPalace.HALF;
        return new SitePiece(SiteKind.CORAL_PALACE, 0, origin, Rotation.NONE, 0xC02AL, new int[0],
                new BoundingBox(origin.getX() - h - 1, origin.getY() - 10, origin.getZ() - h - 1, origin.getX() + h + 1, origin.getY() + 22,
                        origin.getZ() + h + 1));
    }

    /** Ein verschüttetes Kloster mit der Mitte des Kreuzgangs bei {@code origin} (Boden); die Erde liegt {@code surface} höher. */
    public static SitePiece buriedAbbey(BlockPos origin, int surface) {
        return new SitePiece(SiteKind.BURIED_ABBEY, 0, origin, Rotation.NONE, 0xAB3L, new int[] {surface},
                new BoundingBox(origin.getX() - 13, origin.getY() - 2, origin.getZ() - 13, origin.getX() + 13, origin.getY() + Math.max(surface, 9) + 9,
                        origin.getZ() + 13));
    }

    /** Eine Stille Kathedrale mit der Mitte des Mittelschiffs bei {@code origin} (y = Schicht des Fußbodens). */
    public static SitePiece silentCathedral(BlockPos origin) {
        return new SitePiece(SiteKind.SILENT_CATHEDRAL, 0, origin, Rotation.NONE, 0xCA7L, new int[0], SilentCathedral.box(origin, Rotation.NONE));
    }

    /** Eine Arena der Prüfungen mit der Mitte der Arena bei {@code origin} (y = Schicht des Sandes). */
    public static SitePiece trialArena(BlockPos origin, long seed) {
        return new SitePiece(SiteKind.TRIAL_ARENA, 0, origin, Rotation.NONE, seed, new int[0], TrialArena.box(origin));
    }

    /** Eine Raubritterburg mit der Mitte des Hofs bei {@code origin} (y = oberste Schicht des Hofs). */
    public static SitePiece robberCastle(BlockPos origin) {
        return new SitePiece(SiteKind.ROBBER_CASTLE, 0, origin, Rotation.NONE, 0xB06L, new int[0], RobberCastle.box(origin));
    }

    /** Die Konfiguration des Prüfungsspawners in einem Käfig der Arena. */
    public static String arenaCage(int cage, long seed) {
        return TrialArena.cageConfig(cage, seed);
    }

    /** Ein Gezeitenschrein mit der Mitte des Altars bei {@code center} (y = Oberfläche des Strands). */
    public static SitePiece tideShrine(BlockPos center) {
        return new SitePiece(SiteKind.TIDE_SHRINE, 0, center, Rotation.NONE, 77L, new int[0],
                new BoundingBox(center.getX() - 6, center.getY() - 5, center.getZ() - 6, center.getX() + 6, center.getY() + 10, center.getZ() + 6));
    }
}
