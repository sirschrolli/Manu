package io.github.sirschrolli.kingdomsatwar.urmeer;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.sea.TideGateBlock;
import io.github.sirschrolli.kingdomsatwar.registry.ModParticles;
import io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.jspecify.annotations.Nullable;

/**
 * Die Gezeitentore (seit 0.89): Durch sie gelangt man ins Urmeer und zurück.
 * <ul>
 *     <li>Im <b>Versunkenen Tempel</b> auf dem Grund der tiefen Ozeane steht ein rundes Tor, dessen Schleier schon wirbelt.</li>
 *     <li>Wer selbst eins bauen will, setzt einen Rahmen aus <b>Gezeitensteinziegeln</b> (wie beim Netherportal: innen 2 bis
 *     21 Blöcke breit und 3 bis 21 hoch, die Ecken dürfen fehlen; auch unter Wasser) und berührt ihn mit einem <b>Herz des
 *     Meeres</b> – das Herz bleibt dabei ganz.</li>
 *     <li>Die Reise führt an dieselbe Stelle der anderen Welt: Steht dort im Umkreis von {@value #LINK_RADIUS} Blöcken schon
 *     ein Tor, kommt man dort an; sonst entsteht ein rundes Tor auf einer Plattform über dem Wasser (oder auf dem Land).</li>
 * </ul>
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class TideGate {
    /** So lange (Ticks) muss ein Spieler im Schleier bleiben. */
    public static final int TRAVEL_TICKS = 30;
    /** Halbmesser des Schleiers eines runden Tors (um die Mitte). */
    public static final int RADIUS = 3;
    /** Äußerster Halbmesser des Rings eines runden Tors. */
    public static final int RING = RADIUS + 2;
    private static final double DISC_SQ = (RADIUS + 0.5) * (RADIUS + 0.5);
    private static final double RING_SQ = (RADIUS + 2.2) * (RADIUS + 2.2);
    private static final int MIN_WIDTH = 2;
    private static final int MIN_HEIGHT = 3;
    private static final int MAX_SIZE = 21;
    public static final int LINK_RADIUS = 64;
    /** Halbmesser der Plattform unter einem neu entstandenen Tor. */
    public static final int PLATFORM = 5;

    private TideGate() {}

    /** Gehört der Block zum Rahmen eines Tors? */
    public static boolean isFrame(BlockState state) {
        return state.is(SeaBlocks.TIDE_STONE_BRICKS.base().get()) || state.is(SeaBlocks.CHISELED_TIDE_STONE.get());
    }

    private static boolean isOpen(BlockState state) {
        return state.isAir() || state.is(Blocks.WATER) || state.is(SeaBlocks.TIDE_GATE.get()) || state.canBeReplaced()
                || state.is(Blocks.KELP) || state.is(Blocks.KELP_PLANT);
    }

    // =====================================================================================
    // Das runde Tor
    // =====================================================================================

    /** Liegt die Stelle ({@code a} entlang, {@code b} aufwärts von der Mitte) im Schleier eines runden Tors? */
    public static boolean inDisc(int a, int b) {
        return a * a + b * b <= DISC_SQ;
    }

    /**
     * Der Block des Rings eines runden Tors an der Stelle ({@code a} entlang, {@code b} aufwärts von der Mitte) – oder
     * {@code null}, wo kein Ring ist. Elf Blöcke hoch: Gezeitensteinziegel, an den vier Schlusssteinen um den Schleier
     * gemeißelter Gezeitenstein, oben die Krone, an den Seiten Meereslaternen.
     */
    public static @Nullable BlockState ringBlock(int a, int b) {
        int d = a * a + b * b;
        if (d <= DISC_SQ || d > RING_SQ) {
            return null;
        }
        int ax = Math.abs(a);
        int ab = Math.abs(b);
        if ((ax == RADIUS + 1 && b == 0) || (a == 0 && ab == RADIUS + 1)) {
            return SeaBlocks.CHISELED_TIDE_STONE.get().defaultBlockState();
        }
        if (a == 0 && b == RING) {
            return SeaBlocks.CHISELED_TIDE_STONE.get().defaultBlockState();
        }
        if (ax == RING && b == 0) {
            return Blocks.SEA_LANTERN.defaultBlockState();
        }
        if (ax == RADIUS && ab == RADIUS) {
            return Blocks.DARK_PRISMARINE.defaultBlockState();
        }
        return SeaBlocks.TIDE_STONE_BRICKS.base().get().defaultBlockState();
    }

    /** Der Schleier an einer Stelle (geflutet, wenn dort Wasser steht). */
    public static BlockState veil(Direction.Axis axis, boolean wet) {
        return SeaBlocks.TIDE_GATE.get().defaultBlockState().setValue(TideGateBlock.AXIS, axis).setValue(TideGateBlock.WATERLOGGED, wet);
    }

    private static Direction along(Direction.Axis axis) {
        return axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;
    }

    /** Die Richtung aus dem Schleier heraus (eine der beiden Seiten). */
    public static Direction out(Direction.Axis axis) {
        return axis == Direction.Axis.X ? Direction.SOUTH : Direction.EAST;
    }

    /** Setzt einen runden Ring samt Schleier um {@code center} in die Welt. */
    public static void placeRound(ServerLevel level, BlockPos center, Direction.Axis axis) {
        Direction along = along(axis);
        for (int a = -RING; a <= RING; a++) {
            for (int b = -RING; b <= RING; b++) {
                BlockState ring = ringBlock(a, b);
                if (ring != null) {
                    level.setBlock(center.relative(along, a).above(b), ring, Block.UPDATE_CLIENTS);
                }
            }
        }
        for (int a = -RADIUS; a <= RADIUS; a++) {
            for (int b = -RADIUS; b <= RADIUS; b++) {
                if (inDisc(a, b)) {
                    BlockPos p = center.relative(along, a).above(b);
                    boolean wet = level.getFluidState(p).is(Fluids.WATER);
                    level.setBlock(p, veil(axis, wet), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
                }
            }
        }
    }

    // =====================================================================================
    // Ein selbstgebauter Rahmen
    // =====================================================================================

    /** Eine Portalfläche: erster (unterster, westlichster bzw. nördlichster) Block, Ausrichtung, Breite und Höhe. */
    public record Shape(BlockPos corner, Direction.Axis axis, int width, int height) {
        List<BlockPos> inside() {
            List<BlockPos> list = new ArrayList<>();
            Direction along = along(this.axis);
            for (int w = 0; w < this.width; w++) {
                for (int h = 0; h < this.height; h++) {
                    list.add(this.corner.relative(along, w).above(h));
                }
            }
            return list;
        }

        BlockPos foot() {
            return this.corner.relative(along(this.axis), this.width / 2);
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
        Direction along = along(axis);
        BlockPos pos = inside;
        int steps = 0;
        while (isOpen(level.getBlockState(pos.below())) && steps++ < MAX_SIZE) {
            pos = pos.below();
        }
        if (!isFrame(level.getBlockState(pos.below()))) {
            return null;
        }
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

    /** Erweckt einen Rahmen (mit einem Herz des Meeres auf {@code clicked} von der Seite {@code face}). */
    public static boolean tryLight(ServerLevel level, BlockPos clicked, Direction face) {
        Shape shape = findShape(level, clicked.relative(face));
        if (shape == null && face.getAxis().isHorizontal()) {
            shape = findShape(level, clicked.above());
        }
        if (shape == null) {
            return false;
        }
        for (BlockPos pos : shape.inside()) {
            boolean wet = level.getFluidState(pos).is(Fluids.WATER);
            level.setBlock(pos, veil(shape.axis(), wet), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        }
        TideGateState state = TideGateState.of(level);
        state.add(new TideGateState.Gate(level.dimension(), shape.foot(), shape.axis()));
        state.save(level);
        BlockPos middle = shape.corner().relative(along(shape.axis()), shape.width() / 2).above(shape.height() / 2);
        level.playSound(null, middle, SoundEvents.CONDUIT_ACTIVATE, SoundSource.BLOCKS, 1.2f, 1.0f);
        level.sendParticles(ModParticles.SEA_GLOW.get(), middle.getX() + 0.5, middle.getY() + 0.5, middle.getZ() + 0.5, 120,
                shape.width() * 0.4, shape.height() * 0.4, shape.width() * 0.4, 0.05);
        return true;
    }

    /** Das Herz des Meeres erweckt einen Rahmen aus Gezeitensteinziegeln. */
    @SubscribeEvent
    public static void onUse(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getItemStack().is(Items.HEART_OF_THE_SEA) || !isFrame(event.getLevel().getBlockState(event.getPos()))) {
            return;
        }
        if (!(event.getLevel() instanceof ServerLevel level)) {
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            return;
        }
        Direction face = event.getFace() == null ? Direction.UP : event.getFace();
        if (tryLight(level, event.getPos(), face)) {
            event.setCancellationResult(InteractionResult.SUCCESS_SERVER);
        } else {
            event.getEntity().sendOverlayMessage(Component.translatable("block.kingdomsatwar.tide_gate.no_frame"));
            event.setCancellationResult(InteractionResult.FAIL);
        }
        event.setCanceled(true);
    }

    // =====================================================================================
    // Reisen
    // =====================================================================================

    /**
     * Das Tor, zu dem ein Block des Schleiers gehört: der mittlere Block seiner untersten Reihe (die zusammenhängenden
     * Blöcke des Schleiers werden abgesucht – das geht bei runden wie bei eckigen Toren).
     */
    public static TideGateState.@Nullable Gate locate(Level level, BlockPos entry) {
        BlockState start = level.getBlockState(entry);
        if (!start.is(SeaBlocks.TIDE_GATE.get())) {
            return null;
        }
        Direction.Axis axis = start.getValue(TideGateBlock.AXIS);
        Direction along = along(axis);
        Set<BlockPos> seen = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(entry.immutable());
        seen.add(entry.immutable());
        while (!queue.isEmpty() && seen.size() < 600) {
            BlockPos pos = queue.poll();
            for (BlockPos next : new BlockPos[] {pos.above(), pos.below(), pos.relative(along), pos.relative(along.getOpposite())}) {
                if (!seen.contains(next) && level.getBlockState(next).is(SeaBlocks.TIDE_GATE.get())) {
                    seen.add(next);
                    queue.add(next);
                }
            }
        }
        int bottom = seen.stream().mapToInt(BlockPos::getY).min().orElse(entry.getY());
        List<BlockPos> row = new ArrayList<>(seen.stream().filter(p -> p.getY() == bottom).toList());
        row.sort(Comparator.comparingInt(p -> along.getAxis().choose(p.getX(), p.getY(), p.getZ())));
        return new TideGateState.Gate(level.dimension(), row.get(row.size() / 2), axis);
    }

    /** Steht das Tor noch? */
    private static boolean alive(ServerLevel level, TideGateState.Gate gate) {
        return level.getBlockState(gate.foot()).is(SeaBlocks.TIDE_GATE.get());
    }

    /** Ziel einer Reise durch ein Gezeitentor. */
    public static @Nullable TeleportTransition destination(ServerLevel level, Entity entity, BlockPos entry) {
        boolean inUrmeer = level.dimension() == UrmeerDimension.LEVEL;
        ServerLevel target = inUrmeer ? level.getServer().overworld() : level.getServer().getLevel(UrmeerDimension.LEVEL);
        if (target == null) {
            return null;
        }
        TideGateState state = TideGateState.of(level);
        // das Tor, durch das man geht, merken (die Tore der Versunkenen Tempel entstehen von selbst)
        TideGateState.Gate here = locate(level, entry);
        Direction.Axis axis = here != null ? here.axis() : Direction.Axis.X;
        BlockPos wanted = here != null ? here.foot() : entry;
        if (here != null && state.nearest(level.dimension(), here.foot(), 3) == null) {
            state.add(here);
        }
        TideGateState.Gate gate = state.nearest(target.dimension(), wanted, LINK_RADIUS);
        while (gate != null && !alive(target, gate)) {
            state.remove(gate);
            gate = state.nearest(target.dimension(), wanted, LINK_RADIUS);
        }
        if (gate == null) {
            gate = build(target, wanted, axis);
            state.add(gate);
        }
        state.save(level);
        TeleportTransition.PostTeleportTransition rush = arrived -> arrived.level().playSound(null, arrived.blockPosition(),
                SoundEvents.CONDUIT_ACTIVATE, SoundSource.PLAYERS, 0.8f, 1.1f);
        return arrive(target, gate, rush.then(TeleportTransition.PLACE_PORTAL_TICKET));
    }

    /** Ankunft vor einem Tor: zwei Blöcke vor der Mitte seiner Unterkante, mit dem Blick vom Tor weg. */
    private static TeleportTransition arrive(ServerLevel level, TideGateState.Gate gate, TeleportTransition.PostTeleportTransition after) {
        Direction out = out(gate.axis());
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
     * Baut ein rundes Tor an der Stelle der anderen Welt: auf einer runden Plattform aus Gezeitensteinziegeln – über dem
     * Meer einen Block über dem Wasser, auf Säulen bis zum Grund; auf dem Land eben in den Boden gelegt. Darüber wird es frei.
     */
    public static TideGateState.Gate build(ServerLevel level, BlockPos wanted, Direction.Axis axis) {
        level.getChunk(wanted.getX() >> 4, wanted.getZ() >> 4);
        int x = wanted.getX();
        int z = wanted.getZ();
        int ground = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        boolean water = !level.getFluidState(new BlockPos(x, ground - 1, z)).isEmpty();
        int floor = water ? ground : ground - 1;
        floor = Math.max(level.getMinY() + 8, Math.min(floor, level.getMaxY() - RING * 2 - 6));
        BlockState bricks = SeaBlocks.TIDE_STONE_BRICKS.base().get().defaultBlockState();
        BlockState stone = SeaBlocks.TIDE_STONE.base().get().defaultBlockState();
        double edge = (PLATFORM + 0.5) * (PLATFORM + 0.5);
        for (int dx = -PLATFORM; dx <= PLATFORM; dx++) {
            for (int dz = -PLATFORM; dz <= PLATFORM; dz++) {
                int d = dx * dx + dz * dz;
                if (d > edge) {
                    continue;
                }
                BlockPos p = new BlockPos(x + dx, floor, z + dz);
                boolean lamp = Math.abs(dx) == 3 && Math.abs(dz) == 3;
                level.setBlockAndUpdate(p, lamp ? Blocks.SEA_LANTERN.defaultBlockState() : d <= 2 ? SeaBlocks.CHISELED_TIDE_STONE.get().defaultBlockState()
                        : d > (PLATFORM - 0.5) * (PLATFORM - 0.5) ? stone : bricks);
                for (int h = 1; h <= RING * 2 + 2; h++) {
                    BlockPos above = p.above(h);
                    if (!level.getBlockState(above).isAir()) {
                        level.setBlock(above, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                    }
                }
                // Säulen unter den Laternen bis zum Grund (über dem Meer)
                if (lamp) {
                    for (int down = 1; down < 96; down++) {
                        BlockPos below = p.below(down);
                        BlockState there = level.getBlockState(below);
                        if (below.getY() <= level.getMinY() || (!there.isAir() && there.getFluidState().isEmpty())) {
                            break;
                        }
                        level.setBlockAndUpdate(below, bricks);
                    }
                }
            }
        }
        BlockPos center = new BlockPos(x, floor + 1 + RADIUS, z);
        placeRound(level, center, axis);
        return new TideGateState.Gate(level.dimension(), center.below(RADIUS), axis);
    }
}
