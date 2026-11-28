package io.github.sirschrolli.kingdomsatwar.worldgen.site;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/**
 * Ein Platz für ein Musterstück der Bauwerke von 2.0 (für Spieltests und die Werkstatt der Vorführung): der Boden wie in
 * seiner Dimension, Wasser, Lava, eine Steilküste oder eine Felsdecke – je nachdem, worauf das Bauwerk steht. Danach wird
 * das Musterstück dort gebaut.
 */
public final class WonderGround {
    /** So weit reicht der vorbereitete Platz mindestens um den Ursprung. */
    public static final int REACH = 30;

    private WonderGround() {}

    /** Baut das Musterstück {@code wonder} bei {@code origin} auf vorbereitetem Grund; gibt das gebaute Bauteil zurück. */
    public static SitePiece build(ServerLevel level, Wonders.Wonder wonder, BlockPos origin) {
        SitePiece piece = wonder.sampler().sample(origin);
        BoundingBox box = piece.getBoundingBox();
        BoundingBox area = new BoundingBox(Math.min(box.minX(), origin.getX() - REACH) - 2, Math.min(box.minY(), origin.getY() - 34),
                Math.min(box.minZ(), origin.getZ() - REACH) - 2, Math.max(box.maxX(), origin.getX() + REACH) + 2,
                Math.max(box.maxY(), origin.getY() + 44), Math.max(box.maxZ(), origin.getZ() + REACH) + 2);
        for (int cx = (area.minX() >> 4) - 1; cx <= (area.maxX() >> 4) + 1; cx++) {
            for (int cz = (area.minZ() >> 4) - 1; cz <= (area.maxZ() >> 4) + 1; cz++) {
                level.getChunk(cx, cz, ChunkStatus.FULL, true);
            }
        }
        prepare(level, wonder, origin, area);
        piece.postProcess(level, level.structureManager(), level.getChunkSource().getGenerator(), level.getRandom(), area, ChunkPos.containing(origin),
                origin);
        return piece;
    }

    private static BlockState mod(String name, Block fallback) {
        return BuiltInRegistries.BLOCK.getOptional(KingdomsAtWar.id(name)).orElse(fallback).defaultBlockState();
    }

    /** Bereitet den Platz vor: Boden, Luft, Meeresgrund, Wasser, Lava, Felsdecke oder Steilküste. */
    static void prepare(ServerLevel world, Wonders.Wonder wonder, BlockPos o, BoundingBox area) {
        SiteKind.World dim = wonder.kind().world();
        BlockState grass = switch (dim) {
            case NETHER -> Blocks.NETHERRACK.defaultBlockState();
            case END -> Blocks.END_STONE.defaultBlockState();
            case SHADOW -> mod("shadow_grass", Blocks.GRASS_BLOCK);
            case FAE -> mod("fae_grass", Blocks.GRASS_BLOCK);
            case VOID, ABYSS -> Blocks.STONE.defaultBlockState();
            case URMEER -> Blocks.SAND.defaultBlockState();
            default -> Blocks.GRASS_BLOCK.defaultBlockState();
        };
        BlockState soil = switch (dim) {
            case NETHER -> Blocks.NETHERRACK.defaultBlockState();
            case END -> Blocks.END_STONE.defaultBlockState();
            case SHADOW -> mod("shadow_soil", Blocks.DIRT);
            case FAE -> mod("fae_soil", Blocks.DIRT);
            case URMEER -> Blocks.SANDSTONE.defaultBlockState();
            default -> Blocks.DIRT.defaultBlockState();
        };
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = area.minX(); x <= area.maxX(); x++) {
            for (int z = area.minZ(); z <= area.maxZ(); z++) {
                boolean rim = x == area.minX() || x == area.maxX() || z == area.minZ() || z == area.maxZ();
                int dz = z - o.getZ();
                for (int y = area.minY(); y <= area.maxY(); y++) {
                    int dy = y - o.getY();
                    BlockState state = Blocks.AIR.defaultBlockState();
                    switch (wonder.ground()) {
                        case LAND -> state = dy == -1 ? grass : dy < -1 && dy >= -6 ? soil : state;
                        case SEABED -> state = rim ? Blocks.GLASS.defaultBlockState() : dy <= -1 && dy >= -4 ? Blocks.SAND.defaultBlockState()
                                : dy >= 0 && dy <= 38 ? Blocks.WATER.defaultBlockState() : state;
                        case WATER -> state = rim && dy < 0 && dy >= -14 ? Blocks.GLASS.defaultBlockState() : dy < 0 && dy >= -13
                                ? Blocks.WATER.defaultBlockState() : dy == -14 ? Blocks.SAND.defaultBlockState() : state;
                        case LAVA -> state = dy < 0 && dy >= -6 ? (dz < -12 ? Blocks.NETHERRACK.defaultBlockState() : Blocks.LAVA.defaultBlockState())
                                : dy == -7 ? Blocks.NETHERRACK.defaultBlockState() : state;
                        case CEILING -> state = dy >= 26 && dy <= 32 ? Blocks.NETHERRACK.defaultBlockState()
                                : dy <= -30 && dy >= -32 ? Blocks.LAVA.defaultBlockState() : state;
                        case COAST -> {
                            if (dz >= 7) {
                                state = dy < 0 && dy >= -6 ? Blocks.WATER.defaultBlockState() : dy == -7 ? Blocks.SAND.defaultBlockState() : state;
                            } else if (dz >= 0) {
                                state = dy == -1 ? Blocks.SAND.defaultBlockState() : dy < -1 && dy >= -7 ? Blocks.SANDSTONE.defaultBlockState() : state;
                            } else {
                                state = dy == 10 ? grass : dy == 9 ? soil : dy < 9 && dy >= -7 ? Blocks.STONE.defaultBlockState() : state;
                            }
                        }
                        case AIR -> {
                        }
                    }
                    pos.set(x, y, z);
                    world.setBlock(pos, state, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
                }
            }
        }
    }
}
