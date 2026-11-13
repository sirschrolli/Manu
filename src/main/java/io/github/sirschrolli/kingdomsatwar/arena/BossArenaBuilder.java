package io.github.sirschrolli.kingdomsatwar.arena;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.abyss.hoard.HoardPiece;
import io.github.sirschrolli.kingdomsatwar.worldgen.throne.ThronePiece;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Baut das Abbild eines Horts in seiner Kampfdimension:
 * <ul>
 *     <li>den Schlund der Welten und den Thron der Alten neu aus ihren Bauplänen (sie schweben ohnehin frei);</li>
 *     <li>die Zauberbühne, die Aschenesse, die Sternwarte und die Fehlerzone als herausgerissenes Stück Welt
 *     ({@link ArenaIsland}) – genau so, wie der Hort in der Welt gerade aussieht, nur ohne Beute und ohne Wesen.</li>
 * </ul>
 */
public final class BossArenaBuilder {
    /** Fester Samen der neu gebauten Horte (jede Arena sieht gleich aus). */
    private static final long SEED = 0x6B696E67646F6DL;

    private BossArenaBuilder() {}

    /** Wo das Stück Welt herausgerissen wird (Boden der Mitte, Originalkoordinaten) und in welcher Form. */
    record Cut(BlockPos origin, ArenaIsland.Shape shape) {}

    /**
     * Baut die Arena: {@code center} ist die Mitte des Kampfs in der Arena (das Abbild von {@link BossLair#arenaFocus()}).
     * Gibt zurück, wo dort das Abbild des Horts (seine Blockentität) steht – {@code null}, wenn es nicht ging.
     */
    public static @Nullable BlockPos build(ServerLevel from, BossLair lair, BlockPos lairPos, ServerLevel arena, BlockPos center) {
        BossArenaKind kind = lair.arenaKind();
        long started = System.nanoTime();
        BlockPos trigger;
        switch (kind) {
            case WORLD_EATER -> {
                BlockPos piece = center.below();
                // erst den Platz leeren (schnell, Abschnitt für Abschnitt), dann ohne das langsame Freiräumen bauen
                int[] clearing = HoardPiece.clearing();
                ArenaIsland.clear(arena, piece, clearing[0], clearing[1], clearing[2]);
                HoardPiece hoard = new HoardPiece(piece, SEED).withoutClearing();
                BoundingBox box = hoard.getBoundingBox();
                hoard.postProcess(arena, arena.structureManager(), arena.getChunkSource().getGenerator(), RandomSource.create(SEED), box,
                        ChunkPos.containing(piece), piece);
                trigger = HoardPiece.sealPosition(center);
            }
            case KING -> {
                BlockPos piece = new BlockPos(center.getX(), ThronePiece.FLOOR, center.getZ());
                // was eine frühere Arena hinterließ (etwa Trümmer jenseits des Freiraums), verschwindet zuerst
                ArenaIsland.clear(arena, piece, 46, 40, 40);
                ThronePiece throne = new ThronePiece(piece, SEED);
                BoundingBox box = throne.getBoundingBox();
                throne.postProcess(arena, arena.structureManager(), arena.getChunkSource().getGenerator(), RandomSource.create(SEED), box,
                        ChunkPos.containing(piece), piece);
                trigger = new BlockPos(piece.getX(), ThronePiece.FLOOR - 3, piece.getZ() + ThronePiece.THRONE_Z);
            }
            case CREATOR -> {
                // die Schöpfung wird immer gleich gebaut: nur wo noch kein Hof steht, erst den Platz leeren – und steht noch
                // eine andere Bühne vom letzten Kampf (Firmament, Herz der Sonne), verschwindet sie zuerst
                BlockPos altar = new BlockPos(center.getX(), center.getY() + CreatorArena.DAIS_HEIGHT, center.getZ() + CreatorArena.ALTAR_Z);
                if (!arena.getBlockState(altar).is(io.github.sirschrolli.kingdomsatwar.registry.CreatorBlocks.CREATOR_ALTAR.get())) {
                    ArenaIsland.clear(arena, center, kind.reach(), 60, 90);
                } else if (arena.getBlockEntity(altar) instanceof io.github.sirschrolli.kingdomsatwar.block.creator.CreatorAltarBlockEntity be
                        && be.arenaStage() != 0) {
                    CreatorStages.clear(arena, center, altar);
                }
                trigger = CreatorArena.build(arena, center);
                if (arena.getBlockEntity(trigger) instanceof io.github.sirschrolli.kingdomsatwar.block.creator.CreatorAltarBlockEntity be) {
                    be.setArenaStage(0);
                }
            }
            default -> {
                Cut cut = cut(kind, from, lair, lairPos);
                BlockPos focus = lair.arenaFocus();
                BlockPos offset = center.subtract(focus);
                ArenaIsland.copy(from, cut.origin(), arena, cut.origin().offset(offset), cut.shape());
                trigger = lairPos.offset(offset);
            }
        }
        KingdomsAtWar.LOGGER.info("Kampfdimension {}: Abbild des Horts bei {} gebaut ({} ms)", kind.id(), center,
                (System.nanoTime() - started) / 1_000_000L);
        return trigger;
    }

    /** Das Stück Welt, das für diesen Hort herausgerissen wird. */
    static Cut cut(BossArenaKind kind, ServerLevel from, BossLair lair, BlockPos lairPos) {
        BlockPos focus = lair.arenaFocus();
        return switch (kind) {
            // die Bühne mit Zuschauerrund und Lichtung (der Zylinder steht 6 Blöcke hinter der Mitte, 3 über dem Boden)
            case MYSTIK -> new Cut(new BlockPos(focus.getX(), focus.getY() - 3, focus.getZ() + 6), new ArenaIsland.Shape(31, 18, 32, false, 11, 0.86));
            // die Esse im Glutdom, oben aufgerissen: ein Krater unter dem brennenden Himmel (der Amboss steht 47 über dem
            // Boden der Schlackenhalle, 6 vor der Mitte des Turms)
            case MALLEUS -> {
                Direction facing = lair instanceof io.github.sirschrolli.kingdomsatwar.block.nether.GlutAnvilBlockEntity anvil
                        ? anvil.facing() : Direction.SOUTH;
                BlockPos tower = lairPos.relative(facing, -6).below(47);
                yield new Cut(tower, new ArenaIsland.Shape(40, 12, 58, true, 23, 0.6));
            }
            // die Sternwarte mit ihrem Hof und den Seitentürmen (der Sternstein steht zwei über dem Pflaster)
            case ASTERIA -> new Cut(focus.below(2), new ArenaIsland.Shape(37, 30, 48, false, 37));
            // die Fehlerzone (der Kern steht auf seinem Sockel, zwei über dem Boden der Arena)
            case ANNI -> new Cut(focus.below(2), new ArenaIsland.Shape(34, 18, 38, false, 41, 0.84));
            default -> new Cut(focus, new ArenaIsland.Shape(30, 16, 30, false, 53));
        };
    }

    // =====================================================================================
    // Wo die Kämpfer erscheinen
    // =====================================================================================

    /**
     * Freie Plätze auf festem Boden um die Mitte (zwischen {@link BossArenaKind#spawnMin()} und {@link BossArenaKind#spawnMax()}
     * Blöcken), weit genug auseinander. Findet sich nichts, steht man über der Mitte.
     */
    public static List<Vec3> spawnPoints(ServerLevel level, BlockPos center, BossArenaKind kind, int count) {
        List<Vec3> found = new ArrayList<>();
        int min = kind.spawnMin();
        int max = kind.spawnMax();
        for (double r = min; r <= max && found.size() < 64; r += 1.5) {
            int steps = Math.max(8, (int) (r * Math.PI));
            for (int i = 0; i < steps && found.size() < 64; i++) {
                // bei jedem Ring etwas versetzt, damit die Plätze sich verteilen
                double a = Math.PI / 2.0 + (i + (r - min) * 0.37) * 2.0 * Math.PI / steps;
                int x = center.getX() + (int) Math.round(Math.cos(a) * r);
                int z = center.getZ() + (int) Math.round(Math.sin(a) * r);
                Vec3 spot = stand(level, x, center.getY(), z);
                if (spot != null && found.stream().noneMatch(p -> p.distanceToSqr(spot) < 2.2)) {
                    found.add(spot);
                }
            }
        }
        List<Vec3> out = new ArrayList<>();
        if (found.isEmpty()) {
            for (int i = 0; i < count; i++) {
                out.add(Vec3.atBottomCenterOf(center.above()));
            }
            return out;
        }
        // gleichmäßig über die gefundenen Plätze verteilt (zuerst die nahen, dann die fernen)
        for (int i = 0; i < count; i++) {
            int k = (int) ((long) i * found.size() / Math.max(1, count)) % found.size();
            out.add(found.get((k + i / found.size()) % found.size()));
        }
        return out;
    }

    /** Ein Platz zum Stehen in dieser Säule nahe der Höhe {@code y} (von oben gesucht), oder {@code null}. */
    static @Nullable Vec3 stand(ServerLevel level, int x, int y, int z) {
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        for (int dy = 6; dy >= -8; dy--) {
            int fy = y + dy;
            BlockState floor = level.getBlockState(p.set(x, fy - 1, z));
            if (!floor.isFaceSturdy(level, p, Direction.UP) || dangerous(floor)) {
                continue;
            }
            BlockState feet = level.getBlockState(p.set(x, fy, z));
            BlockState head = level.getBlockState(p.set(x, fy + 1, z));
            if (passable(level, x, fy, z, feet) && passable(level, x, fy + 1, z, head)) {
                return new Vec3(x + 0.5, fy, z + 0.5);
            }
        }
        return null;
    }

    private static boolean passable(ServerLevel level, int x, int y, int z, BlockState state) {
        return state.getCollisionShape(level, new BlockPos(x, y, z)).isEmpty() && state.getFluidState().isEmpty() && !state.is(BlockTags.FIRE)
                && !state.is(Blocks.SWEET_BERRY_BUSH) && !state.is(Blocks.POWDER_SNOW) && !state.is(Blocks.COBWEB);
    }

    private static boolean dangerous(BlockState state) {
        return state.is(Blocks.MAGMA_BLOCK) || state.is(BlockTags.CAMPFIRES) || state.is(Blocks.CACTUS) || state.is(Blocks.POINTED_DRIPSTONE);
    }
}
