package io.github.sirschrolli.kingdomsatwar.economy.town;

import io.github.sirschrolli.kingdomsatwar.architect.PlanInfo;
import io.github.sirschrolli.kingdomsatwar.architect.PlanScan;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.jspecify.annotations.Nullable;

/**
 * Eine Großbaustelle: ein großer eigener Bauplan (Dragon Architect, ab {@link #FROM} Blöcken Breite – bis zur ganzen Burg),
 * aufgeteilt in Bauabschnitte von je einem Chunk (16 × 16 Säulen). Jeder Baumeister nimmt sich einen freien Abschnitt
 * – den nächsten bei ihm –, so arbeiten viele zugleich. Ein Abschnitt entsteht in zwei Gängen: erst der <i>Rohbau</i>
 * (Platz räumen, Fundament, Keller ausheben, alle festen Blöcke Lage für Lage von unten nach oben), dann die
 * <i>Einzelheiten</i> (Türen, Fackeln, Laternen, Teppiche, Pflanzen, Wasser …) – die erst, wenn ringsum der Rohbau steht,
 * damit alles einen Halt findet.
 *
 * <p>Die Bauschritte eines Abschnitts entstehen erst, wenn ihn jemand anfängt (direkt aus der Vermessung, ohne den ganzen
 * Plan aufzubauen); Lagen, die in der Welt ganz aus Luft bestehen, braucht er gar nicht erst wegzuräumen. Gespeichert
 * wird nur, wie weit jeder Abschnitt ist.
 */
public final class SiteSections {
    /** Zustand eines Abschnitts: noch nichts, Rohbau steht, fertig. */
    public static final int OPEN = 0;
    public static final int FRAME = 1;
    public static final int DONE = 2;
    /** Ab dieser Breite oder Tiefe wird ein eigener Plan in Abschnitten gebaut. */
    public static final int FROM = 40;
    /** So lange (Ticks) hält ein Baumeister seinen Abschnitt, ohne daran zu arbeiten (danach darf ihn ein anderer nehmen). */
    private static final long CLAIM_TIMEOUT = 2400L;
    /** Seitenlänge der Felder, in denen der Rohbau eines Abschnitts hochgezogen wird (so weit reicht ein Baumeister). */
    public static final int TILE = 6;
    /** Wie viel näher (Blöcke) ihm Einzelheiten vorkommen als ein neuer Rohbau (fertige Abschnitte sehen gleich schön aus). */
    private static final double DETAIL_BONUS = 20.0;

    private final Town.Project project;
    private final PlanScan scan;
    private final BoundingBox world;
    private final int cx0;
    private final int cz0;
    private final int w;
    private final int d;
    private final int[] state;
    /** Laufende Arbeiten und wer welchen Abschnitt hat (auch von anderen Fäden lesbar, etwa für Vorführungen). */
    private final Map<Integer, Town.Work> works = new java.util.concurrent.ConcurrentHashMap<>();
    private final Map<UUID, Claim> claims = new java.util.concurrent.ConcurrentHashMap<>();
    private int @Nullable [] frameBlocks;
    private int @Nullable [] detailBlocks;

    private static final class Claim {
        final Town.Work work;
        long seen;

        Claim(Town.Work work, long seen) {
            this.work = work;
            this.seen = seen;
        }
    }

    /** Wird ein Plan dieser Größe in Abschnitten gebaut? */
    public static boolean large(PlanInfo info) {
        return Math.max(info.shape().sizeX(), info.shape().sizeZ()) >= FROM;
    }

    SiteSections(Town.Project project, PlanScan scan, int @Nullable [] saved) {
        this.project = project;
        this.scan = scan;
        this.world = Blueprint.worldBounds(scan.localBounds(), project.origin, project.rotation);
        this.cx0 = this.world.minX() >> 4;
        this.cz0 = this.world.minZ() >> 4;
        this.w = (this.world.maxX() >> 4) - this.cx0 + 1;
        this.d = (this.world.maxZ() >> 4) - this.cz0 + 1;
        this.state = new int[this.w * this.d];
        if (saved != null && saved.length == this.state.length) {
            for (int i = 0; i < saved.length; i++) {
                this.state[i] = Math.max(OPEN, Math.min(DONE, saved[i]));
            }
        }
    }

    // =====================================================================================
    // Zustand
    // =====================================================================================

    public int count() {
        return this.state.length;
    }

    public int state(int section) {
        return this.state[section];
    }

    /** Wie viele Abschnitte ganz fertig sind. */
    public int done() {
        int n = 0;
        for (int s : this.state) {
            if (s == DONE) {
                n++;
            }
        }
        return n;
    }

    public boolean allDone() {
        for (int s : this.state) {
            if (s != DONE) {
                return false;
            }
        }
        return true;
    }

    /** Ist schon etwas getan? */
    public boolean started() {
        for (int s : this.state) {
            if (s != OPEN) {
                return true;
            }
        }
        for (Town.Work work : this.works.values()) {
            if (work.progress() > 0) {
                return true;
            }
        }
        return false;
    }

    List<Integer> states() {
        List<Integer> list = new ArrayList<>(this.state.length);
        for (int s : this.state) {
            list.add(s);
        }
        return list;
    }

    /** Wie viele Baumeister gerade einen Abschnitt haben. */
    public int builders(long now) {
        int n = 0;
        for (Claim c : this.claims.values()) {
            if (now - c.seen <= CLAIM_TIMEOUT) {
                n++;
            }
        }
        return n;
    }

    /** Breite des Rasters (Abschnitte von West nach Ost). */
    public int width() {
        return this.w;
    }

    /**
     * Die Karte der Abschnitte für das Stadtfenster: je Abschnitt sein Zustand ({@link #OPEN}, {@link #FRAME},
     * {@link #DONE}) oder 3, wenn gerade ein Baumeister daran arbeitet; Zeile für Zeile von Norden nach Süden.
     */
    public byte[] map(long now) {
        byte[] map = new byte[this.state.length];
        for (int i = 0; i < map.length; i++) {
            map[i] = (byte) this.state[i];
        }
        for (Claim c : this.claims.values()) {
            if (now - c.seen <= CLAIM_TIMEOUT && this.state[c.work.section] != DONE) {
                map[c.work.section] = 3;
            }
        }
        return map;
    }

    /** Der Abschnitt in der Welt (seine Säulen; von der untersten Lage des Plans bis eine Lage über ihn). */
    public BoundingBox bounds(int section) {
        int scx = this.cx0 + section % this.w;
        int scz = this.cz0 + section / this.w;
        return new BoundingBox(Math.max(this.world.minX(), scx << 4), this.world.minY(), Math.max(this.world.minZ(), scz << 4),
                Math.min(this.world.maxX(), (scx << 4) + 15), this.world.maxY() + 1, Math.min(this.world.maxZ(), (scz << 4) + 15));
    }

    /** Wie weit der Bau ist (gesetzte Blöcke: fertige Abschnitte, stehende Rohbauten und was gerade entsteht). */
    public float fraction() {
        this.counts();
        int[] frame = java.util.Objects.requireNonNull(this.frameBlocks);
        int[] detail = java.util.Objects.requireNonNull(this.detailBlocks);
        long total = 0;
        long done = 0;
        for (int i = 0; i < this.state.length; i++) {
            total += frame[i] + detail[i];
            if (this.state[i] == DONE) {
                done += frame[i] + detail[i];
            } else if (this.state[i] == FRAME) {
                done += frame[i];
            }
        }
        for (Town.Work work : this.works.values()) {
            List<Blueprint.Step> steps = work.steps();
            for (int k = 0; k < Math.min(work.progress(), steps.size()); k++) {
                if (!steps.get(k).state().isAir()) {
                    done++;
                }
            }
        }
        return total == 0 ? 1.0f : Math.min(1.0f, done / (float) total);
    }

    /** Zählt einmal die Blöcke je Abschnitt (Rohbau mit Fundament, Einzelheiten). */
    private void counts() {
        if (this.frameBlocks != null) {
            return;
        }
        int[] frame = new int[this.state.length];
        int[] detail = new int[this.state.length];
        int ox = (this.scan.sizeX - 1) / 2;
        int oz = (this.scan.sizeZ - 1) / 2;
        for (int gz = 0; gz < this.scan.sizeZ; gz++) {
            for (int gx = 0; gx < this.scan.sizeX; gx++) {
                BlockPos wpos = Blueprint.world(this.project.origin, this.project.rotation, new BlockPos(gx - ox, 0, gz - oz));
                int section = ((wpos.getZ() >> 4) - this.cz0) * this.w + ((wpos.getX() >> 4) - this.cx0);
                if (section < 0 || section >= this.state.length) {
                    continue;
                }
                if (this.scan.footprintColumn(gx, gz)) {
                    frame[section]++;
                }
                for (int gy = 0; gy < this.scan.sizeY; gy++) {
                    BlockState s = this.scan.get(gx, gy, gz);
                    if (s == null || s.isAir()) {
                        continue;
                    }
                    if (Blueprint.attached(s)) {
                        detail[section]++;
                    } else {
                        frame[section]++;
                    }
                }
            }
        }
        this.frameBlocks = frame;
        this.detailBlocks = detail;
    }

    // =====================================================================================
    // Abschnitte vergeben
    // =====================================================================================

    /**
     * Die Arbeit eines Baumeisters: sein Abschnitt, oder – wenn er keinen hat – der nächste freie (Einzelheiten dort, wo
     * ringsum der Rohbau steht, sonst ein neuer Rohbau). {@code null}: gerade nichts frei.
     */
    public Town.@Nullable Work claim(UUID builder, ServerLevel level, BlockPos feet) {
        long now = level.getGameTime();
        Claim mine = this.claims.get(builder);
        if (mine != null) {
            mine.seen = now;
            return mine.work;
        }
        this.claims.values().removeIf(c -> now - c.seen > CLAIM_TIMEOUT);
        Set<Integer> taken = new HashSet<>();
        for (Claim c : this.claims.values()) {
            taken.add(key(c.work.section, c.work.phase));
        }
        for (int guard = 0; guard < this.state.length * 2 + 2; guard++) {
            int best = -1;
            int bestPhase = 0;
            double bestScore = Double.MAX_VALUE;
            for (int i = 0; i < this.state.length; i++) {
                int phase;
                if (this.state[i] == OPEN) {
                    phase = 0;
                } else if (this.state[i] == FRAME && this.neighboursFramed(i)) {
                    phase = 1;
                } else {
                    continue;
                }
                if (taken.contains(key(i, phase))) {
                    continue;
                }
                BoundingBox b = this.bounds(i);
                double dx = (b.minX() + b.maxX() + 1) / 2.0 - feet.getX();
                double dz = (b.minZ() + b.maxZ() + 1) / 2.0 - feet.getZ();
                double score = Math.sqrt(dx * dx + dz * dz) - (phase == 1 ? DETAIL_BONUS : 0.0) + i * 1.0e-4;
                if (score < bestScore) {
                    bestScore = score;
                    best = i;
                    bestPhase = phase;
                }
            }
            if (best < 0) {
                return null;
            }
            int section = best;
            int phase = bestPhase;
            Town.Work work = this.works.computeIfAbsent(key(section, phase),
                    k -> new Town.Work(this.project, section, phase, this.generate(level, section, phase), this.bounds(section)));
            if (work.steps().isEmpty()) {
                // nichts zu tun (Luft, oder keine Einzelheiten): gleich weiter
                this.works.remove(key(section, phase));
                this.advance(section, phase);
                continue;
            }
            this.claims.put(builder, new Claim(work, now));
            return work;
        }
        return null;
    }

    /** Ein Baumeister lässt seinen Abschnitt los (er arbeitet woanders). */
    public void release(UUID builder) {
        this.claims.remove(builder);
    }

    /** Der Baumeister ist mit seiner Arbeit fertig: der Abschnitt rückt einen Gang weiter. */
    public void complete(UUID builder, Town.Work work) {
        this.claims.remove(builder);
        this.works.remove(key(work.section, work.phase));
        this.advance(work.section, work.phase);
    }

    private void advance(int section, int phase) {
        this.counts();
        if (phase == 0) {
            this.state[section] = java.util.Objects.requireNonNull(this.detailBlocks)[section] == 0 ? DONE : FRAME;
        } else {
            this.state[section] = DONE;
        }
    }

    /** Steht ringsum (auch über Eck) schon der Rohbau? */
    private boolean neighboursFramed(int section) {
        int sx = section % this.w;
        int sz = section / this.w;
        for (int dz = -1; dz <= 1; dz++) {
            for (int dx = -1; dx <= 1; dx++) {
                int x = sx + dx;
                int z = sz + dz;
                if (x < 0 || z < 0 || x >= this.w || z >= this.d) {
                    continue;
                }
                if (this.state[z * this.w + x] == OPEN) {
                    return false;
                }
            }
        }
        return true;
    }

    private static int key(int section, int phase) {
        return section * 2 + phase;
    }

    // =====================================================================================
    // Bauschritte eines Abschnitts
    // =====================================================================================

    /** Eine Stelle des Plans in der Welt (mit gedrehtem Block und den Daten von Schild oder Banner). */
    private record Cell(BlockPos pos, BlockState state, @Nullable CompoundTag data) {}

    private static Rotation inverse(Rotation rotation) {
        return switch (rotation) {
            case CLOCKWISE_90 -> Rotation.COUNTERCLOCKWISE_90;
            case COUNTERCLOCKWISE_90 -> Rotation.CLOCKWISE_90;
            default -> rotation;
        };
    }

    /**
     * Die Bauschritte eines Abschnitts. Rohbau: räumen (von oben nach unten, Reihe für Reihe in Schlangenlinien; Lagen,
     * die in der Welt nur Luft sind, entfallen), Fundamentsäulen, Keller ausheben (von oben nach unten), dann alle festen
     * Blöcke Lage für Lage von unten nach oben – innerhalb der Lage immer zum nächsten Block. Einzelheiten: alles Angehängte
     * in derselben Reihenfolge.
     */
    public List<Blueprint.Step> generate(ServerLevel level, int section, int phase) {
        int scx = this.cx0 + section % this.w;
        int scz = this.cz0 + section / this.w;
        int x0 = Math.max(this.world.minX(), scx << 4);
        int x1 = Math.min(this.world.maxX(), (scx << 4) + 15);
        int z0 = Math.max(this.world.minZ(), scz << 4);
        int z1 = Math.min(this.world.maxZ(), (scz << 4) + 15);
        BlockPos origin = this.project.origin;
        Rotation rotation = this.project.rotation;
        Rotation inverse = inverse(rotation);
        int ox = (this.scan.sizeX - 1) / 2;
        int oz = (this.scan.sizeZ - 1) / 2;
        int cw = x1 - x0 + 1;
        int cd = z1 - z0 + 1;
        int[] gxs = new int[cw * cd];
        int[] gzs = new int[cw * cd];
        for (int z = z0; z <= z1; z++) {
            for (int x = x0; x <= x1; x++) {
                BlockPos local = new BlockPos(x - origin.getX(), 0, z - origin.getZ()).rotate(inverse);
                int c = (z - z0) * cw + (x - x0);
                gxs[c] = local.getX() + ox;
                gzs[c] = local.getZ() + oz;
            }
        }
        List<Blueprint.Step> out = new ArrayList<>();
        BlockPos corner = new BlockPos(x0, 0, z0);
        if (phase == 0) {
            BlockState air = Blocks.AIR.defaultBlockState();
            LevelChunk chunk = level.getChunk(scx, scz);
            int top = this.scan.sizeY - this.scan.ground; // eine Lage über dem Plan
            for (int ly = top; ly >= 0; ly--) {
                int wy = origin.getY() + ly;
                int si = chunk.getSectionIndex(wy);
                if (si < 0 || si >= chunk.getSectionsCount() || chunk.getSection(si).hasOnlyAir()) {
                    continue;
                }
                int gy = ly + this.scan.ground;
                for (int z = z0; z <= z1; z++) {
                    boolean even = ((z - z0) & 1) == 0;
                    for (int k = 0; k < cw; k++) {
                        int x = even ? x0 + k : x1 - k;
                        int c = (z - z0) * cw + (x - x0);
                        if (gy < this.scan.sizeY && this.inside(gxs[c], gzs[c]) && this.scan.meaningful(gxs[c], gy, gzs[c])) {
                            continue;
                        }
                        out.add(new Blueprint.Step(new BlockPos(x, wy, z), air, false));
                    }
                }
            }
            // Fundamentsäulen unter der Grundfläche
            BlockState foundation = this.scan.foundation();
            for (int z = z0; z <= z1; z++) {
                boolean even = ((z - z0) & 1) == 0;
                for (int k = 0; k < cw; k++) {
                    int x = even ? x0 + k : x1 - k;
                    int c = (z - z0) * cw + (x - x0);
                    if (this.inside(gxs[c], gzs[c]) && this.scan.footprintColumn(gxs[c], gzs[c])) {
                        out.add(new Blueprint.Step(new BlockPos(x, origin.getY() - 1, z), foundation, true));
                    }
                }
            }
        }
        // die Stellen des Plans in diesem Abschnitt
        List<Cell> dig = new ArrayList<>();
        List<Cell> solid = new ArrayList<>();
        List<Cell> attached = new ArrayList<>();
        for (int z = z0; z <= z1; z++) {
            for (int x = x0; x <= x1; x++) {
                int c = (z - z0) * cw + (x - x0);
                int gx = gxs[c];
                int gz = gzs[c];
                if (!this.inside(gx, gz)) {
                    continue;
                }
                for (int gy = 0; gy < this.scan.sizeY; gy++) {
                    BlockState s = this.scan.get(gx, gy, gz);
                    if (s == null || s.isAir() && gy >= this.scan.ground) {
                        continue;
                    }
                    Cell cell = new Cell(new BlockPos(x, origin.getY() + gy - this.scan.ground, z), Blueprint.world(s, rotation),
                            this.scan.blockEntity(gx, gy, gz));
                    if (s.isAir()) {
                        dig.add(cell);
                    } else if (Blueprint.attached(s)) {
                        attached.add(cell);
                    } else {
                        solid.add(cell);
                    }
                }
            }
        }
        if (phase == 0) {
            BlockPos last = corner;
            if (!dig.isEmpty()) {
                List<Cell> sorted = Blueprint.sweep(dig, Cell::pos, corner);
                TreeMap<Integer, List<Cell>> byLayer = new TreeMap<>(Comparator.reverseOrder());
                for (Cell cell : sorted) {
                    byLayer.computeIfAbsent(cell.pos().getY(), y -> new ArrayList<>()).add(cell);
                }
                for (List<Cell> layer : byLayer.values()) {
                    for (Cell cell : layer) {
                        out.add(new Blueprint.Step(cell.pos(), cell.state(), false, cell.data()));
                        last = cell.pos();
                    }
                }
            }
            // Rohbau in Feldern von TILE × TILE Säulen: Feld für Feld in Schlangenlinien, in jedem Feld Lage für Lage von
            // unten nach oben – so steht der Baumeister lange an einer Stelle und zieht die Mauern dort ganz hoch
            int tilesX = (cw + TILE - 1) / TILE;
            int tilesZ = (cd + TILE - 1) / TILE;
            List<List<Cell>> tiles = new ArrayList<>();
            for (int t = 0; t < tilesX * tilesZ; t++) {
                tiles.add(new ArrayList<>());
            }
            for (Cell cell : solid) {
                int tx = (cell.pos().getX() - x0) / TILE;
                int tz = (cell.pos().getZ() - z0) / TILE;
                tiles.get(tz * tilesX + tx).add(cell);
            }
            for (int tz = 0; tz < tilesZ; tz++) {
                for (int k = 0; k < tilesX; k++) {
                    int tx = (tz & 1) == 0 ? k : tilesX - 1 - k;
                    List<Cell> tile = tiles.get(tz * tilesX + tx);
                    if (tile.isEmpty()) {
                        continue;
                    }
                    for (Cell cell : Blueprint.sweep(tile, Cell::pos, last)) {
                        out.add(new Blueprint.Step(cell.pos(), cell.state(), false, cell.data()));
                        last = cell.pos();
                    }
                }
            }
        } else {
            for (Cell cell : Blueprint.sweep(attached, Cell::pos, corner)) {
                out.add(new Blueprint.Step(cell.pos(), cell.state(), false, cell.data()));
            }
        }
        return out;
    }

    private boolean inside(int gx, int gz) {
        return gx >= 0 && gz >= 0 && gx < this.scan.sizeX && gz < this.scan.sizeZ;
    }
}
