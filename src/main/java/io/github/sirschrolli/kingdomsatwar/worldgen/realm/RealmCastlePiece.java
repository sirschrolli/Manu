package io.github.sirschrolli.kingdomsatwar.worldgen.realm;

import io.github.sirschrolli.kingdomsatwar.loot.ModLootTables;
import io.github.sirschrolli.kingdomsatwar.war.Realm;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures;
import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.VillageBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

/**
 * Baut eine Stammburg Chunk für Chunk: zuerst wird der Bauplatz eingeebnet (Fundament bis zum Gelände, darüber alles
 * freigeräumt, ringsum ein sanfter Übergang ins Gelände), dann setzt {@link RealmCastle} die Burg darauf.
 */
public class RealmCastlePiece extends StructurePiece {
    private static final int CLEAR = RealmCastle.HEIGHT + 4;
    private final Realm realm;
    private final BlockPos center;
    private final Rotation rotation;
    private final long seed;

    public RealmCastlePiece(Realm realm, BlockPos center, Rotation rotation, long seed) {
        super(ModStructures.REALM_CASTLE_PIECE.get(), 0, makeBox(center));
        this.realm = realm;
        this.center = center;
        this.rotation = rotation;
        this.seed = seed;
    }

    public RealmCastlePiece(CompoundTag tag) {
        super(ModStructures.REALM_CASTLE_PIECE.get(), tag);
        this.realm = Realm.byId(tag.getStringOr("Realm", Realm.EISENMARK.id()));
        this.center = new BlockPos(tag.getIntOr("CX", 0), tag.getIntOr("CY", 64), tag.getIntOr("CZ", 0));
        this.rotation = Rotation.values()[Math.floorMod(tag.getIntOr("Rot", 0), Rotation.values().length)];
        this.seed = tag.getLongOr("Seed", 0L);
    }

    private static BoundingBox makeBox(BlockPos center) {
        int r = RealmCastle.REACH;
        return new BoundingBox(center.getX() - r, center.getY() - 26, center.getZ() - r, center.getX() + r, center.getY() + CLEAR,
                center.getZ() + r);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putString("Realm", this.realm.id());
        tag.putInt("CX", this.center.getX());
        tag.putInt("CY", this.center.getY());
        tag.putInt("CZ", this.center.getZ());
        tag.putInt("Rot", this.rotation.ordinal());
        tag.putLong("Seed", this.seed);
    }

    public Realm realm() {
        return this.realm;
    }

    /** Mitte des Burghofs (oberste Schicht des Bodens). */
    public BlockPos center() {
        return this.center;
    }

    public Rotation rotation() {
        return this.rotation;
    }

    /** Weltlage eines Punktes in den Koordinaten der Burg. */
    public BlockPos world(BlockPos local) {
        return this.center.offset(local.rotate(this.rotation));
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random,
            BoundingBox chunkBB, ChunkPos chunkPos, BlockPos referencePos) {
        this.prepareGround(level, chunkBB);
        VillageBuilder builder = new VillageBuilder(level, chunkBB, this.center, this.rotation, this.seed);
        RealmCastle.build(builder, this.realm, ModLootTables.REALM_TREASURY, ModLootTables.REALM_ARMORY, ModLootTables.REALM_STORES);
        builder.finish();
    }

    // =====================================================================================
    // Bauplatz
    // =====================================================================================

    private void prepareGround(WorldGenLevel level, BoundingBox chunkBB) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int floor = this.center.getY();
        for (int x = chunkBB.minX(); x <= chunkBB.maxX(); x++) {
            for (int z = chunkBB.minZ(); z <= chunkBB.maxZ(); z++) {
                int lx = Math.abs(x - this.center.getX());
                int lz = Math.abs(z - this.center.getZ());
                double qx = Math.max(0, lx - RealmCastle.PLATFORM);
                double qz = Math.max(0, lz - RealmCastle.PLATFORM);
                double outside = Math.sqrt(qx * qx + qz * qz);
                if (outside > RealmCastle.BLEND) {
                    continue;
                }
                int ground = groundY(level, pos, x, z, floor);
                BlockState top = level.getBlockState(pos.set(x, ground, z));
                BlockState surface = surfaceFor(top);
                if (outside <= 0.0) {
                    this.platformColumn(level, pos, x, z, ground, surface);
                } else {
                    double t = smooth(Mth.clamp(outside / RealmCastle.BLEND, 0.0, 1.0));
                    int target = (int) Math.round(Mth.lerp(t, floor, ground));
                    this.blendColumn(level, pos, x, z, ground, target, surface);
                }
            }
        }
    }

    /** Bauplatz: bis zur Hofhöhe auffüllen, darüber freiräumen. */
    private void platformColumn(WorldGenLevel level, BlockPos.MutableBlockPos pos, int x, int z, int ground, BlockState surface) {
        int floor = this.center.getY();
        for (int y = floor + 1; y <= floor + CLEAR; y++) {
            if (!level.getBlockState(pos.set(x, y, z)).isAir()) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
        }
        int bottom = Math.max(level.getMinY() + 1, Math.min(ground, floor) - 2);
        for (int y = floor - 1; y >= bottom; y--) {
            BlockState old = level.getBlockState(pos.set(x, y, z));
            if (y < ground && VillageBuilder.isSolidGround(old)) {
                break;
            }
            level.setBlock(pos, floor - y <= 3 ? belowFor(surface) : Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        level.setBlock(pos.set(x, floor, z), surface, Block.UPDATE_CLIENTS);
    }

    /** Übergang: vom Bauplatz sanft zur ursprünglichen Geländehöhe. */
    private void blendColumn(WorldGenLevel level, BlockPos.MutableBlockPos pos, int x, int z, int ground, int target, BlockState surface) {
        if (target < ground) {
            for (int y = target + 1; y <= ground + 24; y++) {
                if (!level.getBlockState(pos.set(x, y, z)).isAir()) {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                }
            }
            level.setBlock(pos.set(x, target, z), surface, Block.UPDATE_CLIENTS);
        } else if (target > ground) {
            for (int y = ground + 1; y < target; y++) {
                level.setBlock(pos.set(x, y, z), belowFor(surface), Block.UPDATE_CLIENTS);
            }
            // Pflanzen über dem alten Boden fort, damit nichts im neuen Hang steckt
            for (int y = target + 1; y <= target + 3; y++) {
                BlockState old = level.getBlockState(pos.set(x, y, z));
                if (!old.isAir() && !old.is(BlockTags.LOGS) && !old.is(BlockTags.LEAVES)) {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                }
            }
            level.setBlock(pos.set(x, target, z), surface, Block.UPDATE_CLIENTS);
        }
    }

    /** Oberste feste Bodenschicht (ohne Bäume, Pflanzen und Wasser). */
    private static int groundY(WorldGenLevel level, BlockPos.MutableBlockPos pos, int x, int z, int floor) {
        int top = Math.min(level.getMaxY() - 1, floor + 48);
        int bottom = Math.max(level.getMinY() + 1, floor - 40);
        for (int y = top; y > bottom; y--) {
            BlockState state = level.getBlockState(pos.set(x, y, z));
            if (VillageBuilder.isSolidGround(state)) {
                return y;
            }
        }
        return floor - 1;
    }

    private static BlockState surfaceFor(BlockState top) {
        if (top.is(Blocks.SAND) || top.is(Blocks.RED_SAND) || top.is(Blocks.PODZOL) || top.is(Blocks.COARSE_DIRT) || top.is(Blocks.SNOW_BLOCK)
                || top.is(Blocks.MUD) || top.is(Blocks.MOSS_BLOCK)) {
            return top;
        }
        return Blocks.GRASS_BLOCK.defaultBlockState();
    }

    private static BlockState belowFor(BlockState surface) {
        if (surface.is(Blocks.SAND) || surface.is(Blocks.RED_SAND)) {
            return Blocks.SANDSTONE.defaultBlockState();
        }
        return Blocks.DIRT.defaultBlockState();
    }

    private static double smooth(double t) {
        return t * t * (3.0 - 2.0 * t);
    }
}
