package io.github.sirschrolli.kingdomsatwar.worldgen.umbrer;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * Baut ein Gebäude in eigenen Koordinaten: x nach rechts (Osten), y nach oben (0 = Fußboden), z nach hinten (Süden);
 * die Vorderseite mit der Tür liegt bei z = 0 und zeigt (ungedreht) nach Norden. Richtungen werden in diesem Rahmen
 * angegeben und beim Setzen mitgedreht. Gesetzt wird nur, was im aktuellen Chunk liegt – so kann jedes Bauteil Chunk
 * für Chunk gebaut werden und alle Teile passen zusammen. Zufall, der über mehrere Blöcke entscheidet, wird vorab aus
 * {@link #random} gezogen; Zufall je Block kommt aus {@link #chance}, damit jeder Chunk dasselbe Ergebnis erhält.
 */
public final class VillageBuilder {
    public final WorldGenLevel level;
    public final BoundingBox chunkBox;
    /** Ursprung (links vorne, Höhe des Fußbodens) in Weltkoordinaten. */
    public final BlockPos origin;
    public final Rotation rotation;
    public final RandomSource random;
    private final long seed;
    private final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
    /** Zäune, Scheiben, Mauern …: ihre Verbindungen werden am Ende aus den Nachbarn berechnet. */
    private final List<BlockPos> shaped = new ArrayList<>();

    public VillageBuilder(WorldGenLevel level, BoundingBox chunkBox, BlockPos origin, Rotation rotation, long seed) {
        this.level = level;
        this.chunkBox = chunkBox;
        this.origin = origin;
        this.rotation = rotation;
        this.seed = seed;
        this.random = RandomSource.create(seed);
    }

    /** Weltposition eines Punktes in Gebäude-Koordinaten. */
    public BlockPos world(int x, int y, int z) {
        BlockPos local = new BlockPos(x, y, z).rotate(this.rotation);
        return this.origin.offset(local);
    }

    /** Eine Richtung im Gebäude-Rahmen als Weltrichtung. */
    public Direction worldDirection(Direction local) {
        return this.rotation.rotate(local);
    }

    public boolean inside(BlockPos pos) {
        return this.chunkBox.isInside(pos) && pos.getY() > this.level.getMinY() && pos.getY() < this.level.getMaxY();
    }

    /** Liegt der Punkt (in Gebäude-Koordinaten) im aktuellen Chunk? */
    public boolean here(int x, int y, int z) {
        return this.inside(this.world(x, y, z));
    }

    // =====================================================================================
    // Setzen
    // =====================================================================================

    /** Setzt einen Block (die Blickrichtung wird mitgedreht). */
    public void set(int x, int y, int z, BlockState state) {
        this.setWorld(this.world(x, y, z), state.rotate(this.rotation));
    }

    public void set(int x, int y, int z, Block block) {
        this.set(x, y, z, block.defaultBlockState());
    }

    /** Setzt einen Block in Weltkoordinaten (ohne Drehung), wenn er im aktuellen Chunk liegt. */
    public void setWorld(BlockPos pos, BlockState state) {
        if (this.inside(pos)) {
            this.level.setBlock(pos, state, Block.UPDATE_CLIENTS);
            if (needsShape(state)) {
                this.shaped.add(pos.immutable());
            }
        }
    }

    public BlockState get(int x, int y, int z) {
        BlockPos pos = this.world(x, y, z);
        return this.inside(pos) ? this.level.getBlockState(pos) : Blocks.AIR.defaultBlockState();
    }

    public void fill(int x0, int y0, int z0, int x1, int y1, int z1, BlockState state) {
        for (int x = Math.min(x0, x1); x <= Math.max(x0, x1); x++) {
            for (int y = Math.min(y0, y1); y <= Math.max(y0, y1); y++) {
                for (int z = Math.min(z0, z1); z <= Math.max(z0, z1); z++) {
                    this.set(x, y, z, state);
                }
            }
        }
    }

    public void fill(int x0, int y0, int z0, int x1, int y1, int z1, Block block) {
        this.fill(x0, y0, z0, x1, y1, z1, block.defaultBlockState());
    }

    public void air(int x0, int y0, int z0, int x1, int y1, int z1) {
        this.fill(x0, y0, z0, x1, y1, z1, Blocks.AIR.defaultBlockState());
    }

    /** Rahmen eines Rechtecks (nur der Rand) in einer Höhe. */
    public void ring(int x0, int z0, int x1, int z1, int y, BlockState state) {
        for (int x = x0; x <= x1; x++) {
            this.set(x, y, z0, state);
            this.set(x, y, z1, state);
        }
        for (int z = z0 + 1; z < z1; z++) {
            this.set(x0, y, z, state);
            this.set(x1, y, z, state);
        }
    }

    /** Fester Zufall je Block (unabhängig davon, in welchem Chunk gerade gebaut wird). */
    public boolean chance(int x, int y, int z, float probability) {
        return this.noise(x, y, z) < probability;
    }

    /** Ein fester Zufallswert 0..1 je Block. */
    public float noise(int x, int y, int z) {
        long h = this.seed ^ (x * 0x9E3779B97F4A7C15L) ^ (y * 0xC2B2AE3D27D4EB4FL) ^ (z * 0x165667B19E3779F9L);
        h = (h ^ (h >>> 30)) * 0xBF58476D1CE4E5B9L;
        h = (h ^ (h >>> 27)) * 0x94D049BB133111EBL;
        h ^= h >>> 31;
        return (h >>> 40) / (float) (1L << 24);
    }

    // =====================================================================================
    // Bauteile
    // =====================================================================================

    public static BlockState stairs(Block block, Direction facing, Half half) {
        return block.defaultBlockState().setValue(StairBlock.FACING, facing).setValue(StairBlock.HALF, half);
    }

    public static BlockState stairs(Block block, Direction facing, Half half, StairsShape shape) {
        return stairs(block, facing, half).setValue(StairBlock.SHAPE, shape);
    }

    public static BlockState slab(Block block, SlabType type) {
        return block.defaultBlockState().setValue(SlabBlock.TYPE, type);
    }

    /** Tür (unten bei y, oben bei y + 1); {@code facing} ist die Richtung, in die man beim Hineingehen blickt. */
    public void door(int x, int y, int z, Block door, Direction facing) {
        BlockState state = door.defaultBlockState().setValue(DoorBlock.FACING, facing);
        this.set(x, y, z, state.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER));
        this.set(x, y + 1, z, state.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
    }

    /** Bett: Fußende bei (x, y, z), Kopfende ein Block weiter in Richtung {@code facing}. */
    public void bed(int x, int y, int z, Block bed, Direction facing) {
        BlockState state = bed.defaultBlockState().setValue(BedBlock.FACING, facing);
        this.set(x, y, z, state.setValue(BedBlock.PART, BedPart.FOOT));
        this.set(x + facing.getStepX(), y, z + facing.getStepZ(), state.setValue(BedBlock.PART, BedPart.HEAD));
    }

    /** Leiter von y0 bis y1; {@code facing} zeigt von der Wand weg in den Raum. */
    public void ladder(int x, int y0, int y1, int z, Direction facing) {
        BlockState state = Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, facing);
        for (int y = y0; y <= y1; y++) {
            this.set(x, y, z, state);
        }
    }

    public void hangingLantern(int x, int y, int z, Block lantern) {
        this.set(x, y, z, lantern.defaultBlockState().setValue(LanternBlock.HANGING, true));
    }

    /**
     * Satteldach mit First entlang z (Giebel vorne bei z0 und hinten bei z1), Wände bei x0 und x1, Traufe auf Höhe y.
     * Das Dach steht vorne und hinten einen Block über; die Giebeldreiecke werden mit {@code gable} gefüllt, der First
     * aus {@code ridge} mit einer Stufe {@code cap} darauf.
     */
    public void gableAlongZ(int x0, int x1, int z0, int z1, int y, Block stairs, BlockState ridge, BlockState cap, BlockState gable) {
        for (int i = 0; ; i++) {
            int left = x0 - 1 + i;
            int right = x1 + 1 - i;
            int h = y + i;
            if (left > right) {
                break;
            }
            if (left == right) {
                for (int z = z0 - 1; z <= z1 + 1; z++) {
                    this.set(left, h - 1, z, ridge);
                    this.set(left, h, z, cap);
                }
                break;
            }
            for (int z = z0 - 1; z <= z1 + 1; z++) {
                this.set(left, h, z, stairs(stairs, Direction.EAST, Half.BOTTOM));
                this.set(right, h, z, stairs(stairs, Direction.WEST, Half.BOTTOM));
            }
            if (i >= 1) {
                for (int x = left + 1; x < right; x++) {
                    this.set(x, h, z0, gable);
                    this.set(x, h, z1, gable);
                }
            }
        }
    }

    /** Satteldach mit First entlang x (Traufen vorne und hinten, Giebel links bei x0 und rechts bei x1). */
    public void gableAlongX(int x0, int x1, int z0, int z1, int y, Block stairs, BlockState ridge, BlockState cap, BlockState gable) {
        for (int i = 0; ; i++) {
            int front = z0 - 1 + i;
            int back = z1 + 1 - i;
            int h = y + i;
            if (front > back) {
                break;
            }
            if (front == back) {
                for (int x = x0 - 1; x <= x1 + 1; x++) {
                    this.set(x, h - 1, front, ridge);
                    this.set(x, h, front, cap);
                }
                break;
            }
            for (int x = x0 - 1; x <= x1 + 1; x++) {
                this.set(x, h, front, stairs(stairs, Direction.SOUTH, Half.BOTTOM));
                this.set(x, h, back, stairs(stairs, Direction.NORTH, Half.BOTTOM));
            }
            if (i >= 1) {
                for (int z = front + 1; z < back; z++) {
                    this.set(x0, h, z, gable);
                    this.set(x1, h, z, gable);
                }
            }
        }
    }

    /**
     * Walm- bzw. Zeltdach über dem Rechteck (x0..x1, z0..z1) ab Höhe y: Ring für Ring nach innen, die Ecken als
     * Außenecken. Bleibt eine Linie oder ein Punkt übrig, kommt dort {@code top} hin. Gibt die Höhe der Spitze zurück.
     */
    public int hipRoof(int x0, int z0, int x1, int z1, int y, Block stairs, BlockState top) {
        for (int k = 0; ; k++) {
            int xa = x0 + k;
            int xb = x1 - k;
            int za = z0 + k;
            int zb = z1 - k;
            int h = y + k;
            if (xa > xb || za > zb) {
                return h - 1;
            }
            if (xa == xb || za == zb) {
                this.fill(xa, h, za, xb, h, zb, top);
                return h;
            }
            for (int x = xa + 1; x < xb; x++) {
                this.set(x, h, za, stairs(stairs, Direction.SOUTH, Half.BOTTOM));
                this.set(x, h, zb, stairs(stairs, Direction.NORTH, Half.BOTTOM));
            }
            for (int z = za + 1; z < zb; z++) {
                this.set(xa, h, z, stairs(stairs, Direction.EAST, Half.BOTTOM));
                this.set(xb, h, z, stairs(stairs, Direction.WEST, Half.BOTTOM));
            }
            this.set(xa, h, za, stairs(stairs, Direction.SOUTH, Half.BOTTOM, StairsShape.OUTER_LEFT));
            this.set(xb, h, za, stairs(stairs, Direction.SOUTH, Half.BOTTOM, StairsShape.OUTER_RIGHT));
            this.set(xa, h, zb, stairs(stairs, Direction.NORTH, Half.BOTTOM, StairsShape.OUTER_RIGHT));
            this.set(xb, h, zb, stairs(stairs, Direction.NORTH, Half.BOTTOM, StairsShape.OUTER_LEFT));
        }
    }

    // =====================================================================================
    // Gelände
    // =====================================================================================

    /**
     * Fundament: unter der Grundfläche bis zum Gelände auffüllen (Rand aus {@code edge}, innen {@code inner}); darüber bis
     * {@code clear} Blöcke hoch alles freiräumen (auch Bäume und Pflanzen).
     */
    public void foundation(int x0, int z0, int x1, int z1, BlockState edge, BlockState inner, int clear) {
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                boolean rim = x == x0 || x == x1 || z == z0 || z == z1;
                this.column(x, z, rim ? edge : inner, clear);
            }
        }
    }

    /** Eine Säule des Fundaments: darunter auffüllen, darüber freiräumen. */
    public void column(int x, int z, BlockState fill, int clear) {
        BlockPos top = this.world(x, 0, z);
        if (!this.chunkBox.isInside(top.getX(), this.chunkBox.minY(), top.getZ())) {
            return;
        }
        for (int y = -1; y > -24; y--) {
            this.cursor.set(top.getX(), top.getY() + y, top.getZ());
            BlockState old = this.level.getBlockState(this.cursor);
            if (y < -1 && isSolidGround(old)) {
                break;
            }
            this.level.setBlock(this.cursor, fill, Block.UPDATE_CLIENTS);
        }
        for (int y = 0; y < clear; y++) {
            this.cursor.set(top.getX(), top.getY() + y, top.getZ());
            if (!this.level.getBlockState(this.cursor).isAir()) {
                this.level.setBlock(this.cursor, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
        }
    }

    public static boolean isSolidGround(BlockState state) {
        return state.isSolidRender() && !state.is(BlockTags.LOGS) && !state.is(BlockTags.LEAVES);
    }

    /** Truhe (oder Fass) mit Beute aus der Tabelle (eigener Zufall je Truhe, damit alle Chunks gleich bauen). */
    public void chest(int x, int y, int z, BlockState state, ResourceKey<LootTable> loot) {
        BlockPos pos = this.world(x, y, z);
        if (this.inside(pos)) {
            this.level.setBlock(pos, state.rotate(this.rotation), Block.UPDATE_CLIENTS);
            RandomizableContainer.setBlockEntityLootTable(this.level, RandomSource.create(pos.asLong() ^ this.seed), pos, loot);
        }
    }

    // =====================================================================================
    // Abschluss
    // =====================================================================================

    private static boolean needsShape(BlockState state) {
        Block block = state.getBlock();
        return block instanceof CrossCollisionBlock || block instanceof WallBlock || block instanceof FenceGateBlock;
    }

    /**
     * Verbindet Zäune, Scheiben und Mauern mit ihren Nachbarn – auch mit denen jenseits der Chunkgrenze, die schon
     * stehen (die Nachbarn dort werden mit angepasst; später gebaute Chunks passen sich selbst an).
     */
    public void finish() {
        BlockPos.MutableBlockPos neighbour = new BlockPos.MutableBlockPos();
        for (BlockPos pos : this.shaped) {
            this.reshape(pos);
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                neighbour.setWithOffset(pos, dir);
                if (!this.chunkBox.isInside(neighbour) && needsShape(this.level.getBlockState(neighbour))) {
                    this.reshape(neighbour.immutable());
                }
            }
        }
        this.shaped.clear();
    }

    private void reshape(BlockPos pos) {
        BlockState state = this.level.getBlockState(pos);
        BlockState shaped = Block.updateFromNeighbourShapes(state, this.level, pos);
        if (shaped != state) {
            this.level.setBlock(pos, shaped, Block.UPDATE_CLIENTS);
        }
    }
}
