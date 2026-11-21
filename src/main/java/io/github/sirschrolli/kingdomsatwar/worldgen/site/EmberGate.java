package io.github.sirschrolli.kingdomsatwar.worldgen.site;

import io.github.sirschrolli.kingdomsatwar.loot.ModLootTables;
import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.VillageBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * Die Glutpforte: Feuerdiener, die einst den Weg in die Unterwelt suchten, haben zwischen zwei Säulen aus Basalt ein Tor
 * aus Obsidian errichtet. Es ist zerbrochen – einige Steine fehlen, wer sie ersetzt, kann es entzünden. Ringsum hat die
 * Glut des Tors den Boden verwandelt: Netherrack, Magma, Schwarzstein, ein kleiner Lavatümpel. Zu beiden Seiten stehen
 * Feuerschalen, unter Schutt liegt die Truhe mit dem Gold der Feuerdiener.
 *
 * <p>Eigene Koordinaten: Mitte vor dem Tor bei (0, 0, 0), y = 0 ist die Höhe direkt über dem Boden, das Tor steht quer
 * zur z-Achse.
 */
final class EmberGate {
    private EmberGate() {}

    static Optional<Structure.GenerationStub> plan(Structure.GenerationContext context, SiteTerrain terrain, long seed) {
        ChunkPos chunk = context.chunkPos();
        int x = chunk.getMiddleBlockX();
        int z = chunk.getMiddleBlockZ();
        int[] span = terrain.span(x, z, 6, 3);
        if (terrain.water(x, z) || span[1] - span[0] > 4) {
            return Optional.empty();
        }
        int y = terrain.ground(x, z) + 1;
        Rotation rotation = Rotation.values()[Math.floorMod((int) (seed >>> 7), 4)];
        BlockPos origin = new BlockPos(x, y, z);
        BoundingBox box = new BoundingBox(x - 9, y - 4, z - 9, x + 9, y + 10, z + 9);
        return Optional.of(new Structure.GenerationStub(origin,
                builder -> builder.addPiece(new SitePiece(SiteKind.EMBER_GATE, 0, origin, rotation, seed, new int[0], box))));
    }

    static void build(VillageBuilder b, SitePiece piece) {
        // der verwandelte Boden
        for (int x = -8; x <= 8; x++) {
            for (int z = -8; z <= 8; z++) {
                double r = Math.sqrt(x * x + z * z) + b.noise(x, 0, z) * 2.0;
                if (r > 8) {
                    continue;
                }
                for (int y = -1; y >= -3; y--) {
                    if (b.get(x, y, z).isSolidRender() && y < -1) {
                        break;
                    }
                    b.set(x, y, z, Blocks.NETHERRACK.defaultBlockState());
                }
                float n = b.noise(x, 1, z);
                BlockState ground = r < 3.5 ? (n < 0.12f ? Blocks.MAGMA_BLOCK.defaultBlockState() : n < 0.55f ? Blocks.NETHERRACK.defaultBlockState()
                        : Blocks.BLACKSTONE.defaultBlockState())
                        : n < (8 - r) / 8.0f ? (n < 0.08f ? Blocks.MAGMA_BLOCK.defaultBlockState() : Blocks.NETHERRACK.defaultBlockState())
                        : b.get(x, -1, z);
                b.set(x, -1, z, ground);
                for (int y = 0; y <= 9; y++) {
                    if (!b.get(x, y, z).isAir()) {
                        b.set(x, y, z, Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }
        // ein Tümpel aus Lava, eingefasst
        for (int[] c : new int[][] {{4, 3}, {5, 3}, {4, 4}}) {
            b.set(c[0], -1, c[1], Blocks.LAVA.defaultBlockState());
            b.set(c[0], -2, c[1], Blocks.BLACKSTONE.defaultBlockState());
        }
        for (int[] c : new int[][] {{3, 3}, {6, 3}, {3, 4}, {5, 4}, {4, 5}, {4, 2}, {5, 2}}) {
            b.set(c[0], -1, c[1], Blocks.BLACKSTONE.defaultBlockState());
        }
        // das Tor: Rahmen aus Obsidian (4 × 5), einige Steine fehlen; die Ecken aus Basalt
        int[][] frame = {{0, 0}, {1, 0}, {-1, 1}, {-1, 2}, {-1, 3}, {2, 1}, {2, 2}, {2, 3}, {0, 4}, {1, 4}};
        int missing = 1 + (int) (b.noise(0, 30, 0) * 3);
        for (int i = 0; i < frame.length; i++) {
            int[] f = frame[i];
            boolean gone = Math.floorMod(i * 7 + (int) (piece.seed() & 0xFF), frame.length) < missing;
            b.set(f[0], f[1], -2, gone ? (b.chance(f[0], f[1], 0, 0.5f) ? Blocks.CRYING_OBSIDIAN.defaultBlockState()
                    : Blocks.AIR.defaultBlockState()) : Blocks.OBSIDIAN.defaultBlockState());
        }
        for (int[] c : new int[][] {{-1, 0}, {2, 0}, {-1, 4}, {2, 4}}) {
            b.set(c[0], c[1], -2, Blocks.POLISHED_BASALT.defaultBlockState());
        }
        // die Säulen aus Basalt links und rechts, oben ein Sturz aus Schwarzstein mit Ketten
        for (int x : new int[] {-2, 3}) {
            for (int y = 0; y <= 6; y++) {
                b.set(x, y, -2, y == 6 ? Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState()
                        : Blocks.BASALT.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y));
            }
            b.set(x, 0, -1, VillageBuilder.stairs(Blocks.POLISHED_BLACKSTONE_STAIRS, Direction.NORTH, Half.BOTTOM));
            b.set(x, 0, -3, VillageBuilder.stairs(Blocks.POLISHED_BLACKSTONE_STAIRS, Direction.SOUTH, Half.BOTTOM));
        }
        for (int x = -2; x <= 3; x++) {
            b.set(x, 7, -2, x == -2 || x == 3 ? Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
                    : VillageBuilder.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, x < 1 ? Direction.EAST : Direction.WEST, Half.BOTTOM));
        }
        b.set(0, 5, -2, Blocks.IRON_CHAIN);
        b.set(1, 5, -2, Blocks.IRON_CHAIN);
        b.set(0, 6, -2, Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
        b.set(1, 6, -2, Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
        // Feuerschalen
        for (int x : new int[] {-4, 5}) {
            b.set(x, 0, 0, Blocks.POLISHED_BLACKSTONE_WALL.defaultBlockState());
            b.set(x, 1, 0, Blocks.CAMPFIRE);
        }
        // Schutt und die Truhe der Feuerdiener
        for (int[] c : new int[][] {{-4, -4}, {-3, -5}, {4, -4}, {-5, 2}}) {
            b.set(c[0], 0, c[1], b.chance(c[0], 0, c[1], 0.5f) ? Blocks.OBSIDIAN.defaultBlockState() : Blocks.BLACKSTONE.defaultBlockState());
        }
        b.chest(-3, 0, -4, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH), ModLootTables.SITE_EMBER_GATE);
        if (b.chance(9, 9, 9, 0.4f)) {
            b.set(3, 0, 2, Blocks.GOLD_BLOCK);
        }
        b.set(-1, 0, 2, Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, false));
    }
}
