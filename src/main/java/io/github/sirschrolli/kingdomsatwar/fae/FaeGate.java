package io.github.sirschrolli.kingdomsatwar.fae;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.fae.FaePortalBlock;
import io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModParticles;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Das Tor ins Feenreich: Wie das Netherportal ein Rahmen – hier aus <b>Mondsteinziegeln</b>, innen 2 bis 21 Blöcke breit
 * und 3 bis 21 Blöcke hoch (die Ecken dürfen fehlen). Mit <b>Feenstaub</b> entzündet, füllt ihn ein schimmernder Schleier.
 * Die Reise führt an dieselbe Stelle der anderen Welt (eins zu eins): Steht dort im Umkreis von 48 Blöcken schon ein
 * Feenportal, kommt man dort an; sonst entsteht ein neues auf festem Grund.
 */
public final class FaeGate {
    public static final ResourceKey<Level> FAE_REALM = ResourceKey.create(Registries.DIMENSION, KingdomsAtWar.id("fae_realm"));
    private static final int MIN_WIDTH = 2;
    private static final int MIN_HEIGHT = 3;
    private static final int MAX_SIZE = 21;
    private static final int LINK_RADIUS = 48;

    private FaeGate() {}

    /** Gehört der Block zum Rahmen? */
    public static boolean isFrame(BlockState state) {
        return state.is(FaeBlocks.MOONSTONE_BRICKS.base().get()) || state.is(FaeBlocks.CHISELED_MOONSTONE.get());
    }

    private static boolean isOpen(BlockState state) {
        return state.isAir() || state.is(BlockTags.REPLACEABLE) || state.is(FaeBlocks.FAE_PORTAL.get());
    }

    /** Eine Portalfläche: erster (unterster, westlichster bzw. nördlichster) Block, Ausrichtung, Breite und Höhe. */
    public record Shape(BlockPos corner, Direction.Axis axis, int width, int height) {
        Direction along() {
            return this.axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;
        }

        Iterable<BlockPos> inside() {
            java.util.List<BlockPos> list = new java.util.ArrayList<>();
            for (int w = 0; w < this.width; w++) {
                for (int h = 0; h < this.height; h++) {
                    list.add(this.corner.relative(this.along(), w).above(h));
                }
            }
            return list;
        }
    }

    /** Sucht einen vollständigen Rahmen um die Stelle {@code inside} (beide Ausrichtungen). */
    public static @Nullable Shape findShape(Level level, BlockPos inside) {
        for (Direction.Axis axis : new Direction.Axis[] {Direction.Axis.X, Direction.Axis.Z}) {
            Shape shape = findShape(level, inside, axis);
            if (shape != null) {
                return shape;
            }
        }
        return null;
    }

    private static @Nullable Shape findShape(Level level, BlockPos inside, Direction.Axis axis) {
        if (!isOpen(level.getBlockState(inside))) {
            return null;
        }
        Direction along = axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;
        // hinab bis zum Rahmen
        BlockPos pos = inside;
        int steps = 0;
        while (isOpen(level.getBlockState(pos.below())) && steps++ < MAX_SIZE) {
            pos = pos.below();
        }
        if (!isFrame(level.getBlockState(pos.below()))) {
            return null;
        }
        // zum Anfang der Reihe
        steps = 0;
        while (isOpen(level.getBlockState(pos.relative(along.getOpposite()))) && isFrame(level.getBlockState(pos.relative(along.getOpposite()).below()))
                && steps++ < MAX_SIZE) {
            pos = pos.relative(along.getOpposite());
        }
        if (!isFrame(level.getBlockState(pos.relative(along.getOpposite())))) {
            return null;
        }
        BlockPos corner = pos;
        int width = 0;
        while (width < MAX_SIZE && isOpen(level.getBlockState(corner.relative(along, width)))
                && isFrame(level.getBlockState(corner.relative(along, width).below()))) {
            width++;
        }
        if (width < MIN_WIDTH || width > MAX_SIZE || !isFrame(level.getBlockState(corner.relative(along, width)))) {
            return null;
        }
        int height = 0;
        outer:
        while (height < MAX_SIZE) {
            for (int w = 0; w < width; w++) {
                if (!isOpen(level.getBlockState(corner.relative(along, w).above(height)))) {
                    break outer;
                }
            }
            if (!isFrame(level.getBlockState(corner.relative(along.getOpposite()).above(height)))
                    || !isFrame(level.getBlockState(corner.relative(along, width).above(height)))) {
                return null;
            }
            height++;
        }
        if (height < MIN_HEIGHT || height > MAX_SIZE) {
            return null;
        }
        for (int w = 0; w < width; w++) {
            if (!isFrame(level.getBlockState(corner.relative(along, w).above(height)))) {
                return null;
            }
        }
        return new Shape(corner, axis, width, height);
    }

    /** Entzündet einen Rahmen (mit Feenstaub auf {@code clicked} von der Seite {@code face}). */
    public static boolean tryLight(ServerLevel level, BlockPos clicked, Direction face) {
        BlockPos inside = clicked.relative(face);
        Shape shape = findShape(level, inside);
        if (shape == null && face.getAxis().isHorizontal()) {
            // auch von oben auf die untere Kante geklickt
            shape = findShape(level, clicked.above());
        }
        if (shape == null) {
            return false;
        }
        fill(level, shape);
        FaeGateState state = FaeGateState.of(level);
        state.add(new FaeGateState.Gate(level.dimension(), shape.corner(), shape.axis(), shape.width()));
        state.save(level);
        BlockPos middle = shape.corner().relative(shape.along(), shape.width() / 2).above(shape.height() / 2);
        level.playSound(null, middle, ModSounds.FAE_PORTAL_LIGHT.get(), SoundSource.BLOCKS, 1.2f, 1.0f);
        level.sendParticles(ModParticles.FAE_SPARK.get(), middle.getX() + 0.5, middle.getY() + 0.5, middle.getZ() + 0.5, 120,
                shape.width() * 0.4, shape.height() * 0.4, shape.width() * 0.4, 0.05);
        return true;
    }

    private static void fill(ServerLevel level, Shape shape) {
        BlockState portal = FaeBlocks.FAE_PORTAL.get().defaultBlockState().setValue(FaePortalBlock.AXIS, shape.axis());
        for (BlockPos pos : shape.inside()) {
            level.setBlock(pos, portal, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        }
    }

    // =====================================================================================
    // Reisen
    // =====================================================================================

    /** Ziel einer Reise durch ein Feenportal. */
    public static @Nullable TeleportTransition destination(ServerLevel level, Entity entity, BlockPos entry) {
        ServerLevel target = level.dimension() == FAE_REALM ? level.getServer().overworld() : level.getServer().getLevel(FAE_REALM);
        if (target == null) {
            return null;
        }
        Direction.Axis axis = level.getBlockState(entry).hasProperty(FaePortalBlock.AXIS)
                ? level.getBlockState(entry).getValue(FaePortalBlock.AXIS) : Direction.Axis.X;
        BlockPos wanted = new BlockPos(entry.getX(), entry.getY(), entry.getZ());
        FaeGateState state = FaeGateState.of(level);
        FaeGateState.Gate gate = state.nearest(target.dimension(), wanted, LINK_RADIUS);
        while (gate != null && !target.getBlockState(gate.corner()).is(FaeBlocks.FAE_PORTAL.get())) {
            // verfallen (Rahmen abgebaut): vergessen und weitersuchen
            target.getChunk(gate.corner().getX() >> 4, gate.corner().getZ() >> 4);
            if (target.getBlockState(gate.corner()).is(FaeBlocks.FAE_PORTAL.get())) {
                break;
            }
            state.remove(gate);
            gate = state.nearest(target.dimension(), wanted, LINK_RADIUS);
        }
        if (gate == null) {
            gate = build(target, wanted, axis);
            state.add(gate);
        }
        state.save(level);
        TeleportTransition.PostTeleportTransition chime = arrived -> arrived.level().playSound(null, arrived.blockPosition(),
                ModSounds.FAE_PORTAL_TRAVEL.get(), net.minecraft.sounds.SoundSource.PLAYERS, 0.7f, 1.15f);
        return arrive(target, gate, chime.then(TeleportTransition.PLACE_PORTAL_TICKET));
    }

    /** Ankunft vor einem Portal: zwei Blöcke vor seiner Mitte, mit dem Blick vom Portal weg. */
    private static TeleportTransition arrive(ServerLevel level, FaeGateState.Gate gate, TeleportTransition.PostTeleportTransition after) {
        Direction out = gate.axis() == Direction.Axis.X ? Direction.SOUTH : Direction.EAST;
        BlockPos foot = gate.foot();
        BlockPos spot = foot.relative(out, 2);
        if (!free(level, spot)) {
            spot = foot.relative(out.getOpposite(), 2);
            out = out.getOpposite();
            if (!free(level, spot)) {
                spot = foot;
            }
        }
        return new TeleportTransition(level, Vec3.atBottomCenterOf(spot), Vec3.ZERO, out.toYRot(), 0.0f, after);
    }

    private static boolean free(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()
                && level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty()
                && !level.getBlockState(pos.below()).getCollisionShape(level, pos.below()).isEmpty();
    }

    /**
     * Baut ein Portal (innen 2 × 3) an der Stelle der anderen Welt: auf dem Boden, nie im Wasser (dort auf einer kleinen
     * Plattform aus Mondsteinziegeln), mit freiem Raum davor und dahinter.
     */
    public static FaeGateState.Gate build(ServerLevel level, BlockPos wanted, Direction.Axis axis) {
        level.getChunk(wanted.getX() >> 4, wanted.getZ() >> 4);
        int x = wanted.getX();
        int z = wanted.getZ();
        int ground = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        int top = level.getMaxY() - 8;
        int y = Math.max(level.getMinY() + 8, Math.min(ground, top));
        boolean water = !level.getFluidState(new BlockPos(x, ground - 1, z)).isEmpty();
        Direction along = axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;
        Direction out = axis == Direction.Axis.X ? Direction.SOUTH : Direction.EAST;
        BlockPos corner = new BlockPos(x, y, z);
        BlockState frame = FaeBlocks.MOONSTONE_BRICKS.base().get().defaultBlockState();
        BlockState floor = level.dimension() == FAE_REALM ? FaeBlocks.FAESTONE_BRICKS.base().get().defaultBlockState()
                : Blocks.STONE_BRICKS.defaultBlockState();
        // Boden und Freiraum: 4 Blöcke breit, 5 tief (zwei vor und zwei hinter dem Portal)
        for (int w = -1; w <= 2; w++) {
            for (int d = -2; d <= 2; d++) {
                BlockPos base = corner.relative(along, w).relative(out, d);
                BlockPos below = base.below();
                if (water || level.getBlockState(below).getCollisionShape(level, below).isEmpty()) {
                    level.setBlockAndUpdate(below, water ? frame : floor);
                }
                for (int h = 0; h < 5; h++) {
                    BlockPos p = base.above(h);
                    if (d == 0 && w >= -1 && w <= 2) {
                        continue;
                    }
                    if (!level.getBlockState(p).isAir()) {
                        level.setBlockAndUpdate(p, Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }
        // der Rahmen
        for (int w = -1; w <= 2; w++) {
            for (int h = -1; h <= 3; h++) {
                BlockPos p = corner.relative(along, w).above(h);
                boolean edge = w == -1 || w == 2 || h == -1 || h == 3;
                if (edge) {
                    boolean cornerBlock = (w == -1 || w == 2) && (h == -1 || h == 3);
                    level.setBlockAndUpdate(p, cornerBlock ? FaeBlocks.CHISELED_MOONSTONE.get().defaultBlockState() : frame);
                } else {
                    level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                }
            }
        }
        Shape shape = new Shape(corner, axis, 2, 3);
        fill(level, shape);
        return new FaeGateState.Gate(level.dimension(), corner, axis, 2);
    }
}
