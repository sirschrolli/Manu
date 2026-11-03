package io.github.sirschrolli.kingdomsatwar.worldgen.site;

import io.github.sirschrolli.kingdomsatwar.loot.ModLootTables;
import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.VillageBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * Die Köhlerei: Auf einer Lichtung im Wald haben Köhler ihren Meiler errichtet – ein rauchender Hügel aus Erde über
 * geschichtetem Holz –, daneben Stapel von Scheiten, ein Unterstand mit Pultdach, ein Karren, Fässer und Werkzeug. Die
 * Köhler sind fort; in ihrer Truhe liegen noch Vorräte und Karten. Wer unter dem Unterstand gräbt, findet ihr Versteck.
 *
 * <p>Eigene Koordinaten: Mitte der Lichtung bei (0, 0, 0), y = 0 ist die Höhe direkt über dem Boden.
 */
final class CharcoalCamp {
    /** Holzarten: Stamm, Bretter, Treppe, Zaun, Falltür. */
    private static final Block[][] WOODS = {
            {Blocks.OAK_LOG, Blocks.OAK_PLANKS, Blocks.OAK_STAIRS, Blocks.OAK_FENCE, Blocks.OAK_TRAPDOOR},
            {Blocks.SPRUCE_LOG, Blocks.SPRUCE_PLANKS, Blocks.SPRUCE_STAIRS, Blocks.SPRUCE_FENCE, Blocks.SPRUCE_TRAPDOOR},
            {Blocks.BIRCH_LOG, Blocks.BIRCH_PLANKS, Blocks.BIRCH_STAIRS, Blocks.BIRCH_FENCE, Blocks.BIRCH_TRAPDOOR},
            {Blocks.DARK_OAK_LOG, Blocks.DARK_OAK_PLANKS, Blocks.DARK_OAK_STAIRS, Blocks.DARK_OAK_FENCE, Blocks.DARK_OAK_TRAPDOOR},
            {Blocks.JUNGLE_LOG, Blocks.JUNGLE_PLANKS, Blocks.JUNGLE_STAIRS, Blocks.JUNGLE_FENCE, Blocks.JUNGLE_TRAPDOOR},
            {Blocks.ACACIA_LOG, Blocks.ACACIA_PLANKS, Blocks.ACACIA_STAIRS, Blocks.ACACIA_FENCE, Blocks.ACACIA_TRAPDOOR},
            {Blocks.CHERRY_LOG, Blocks.CHERRY_PLANKS, Blocks.CHERRY_STAIRS, Blocks.CHERRY_FENCE, Blocks.CHERRY_TRAPDOOR},
            {Blocks.MANGROVE_LOG, Blocks.MANGROVE_PLANKS, Blocks.MANGROVE_STAIRS, Blocks.MANGROVE_FENCE, Blocks.MANGROVE_TRAPDOOR}};

    private CharcoalCamp() {}

    static Optional<Structure.GenerationStub> plan(Structure.GenerationContext context, SiteTerrain terrain, long seed) {
        ChunkPos chunk = context.chunkPos();
        int x = chunk.getMiddleBlockX();
        int z = chunk.getMiddleBlockZ();
        int[] span = terrain.span(x, z, 8, 4);
        if (terrain.water(x, z) || span[1] - span[0] > 3) {
            return Optional.empty();
        }
        int ground = terrain.ground(x, z);
        var biome = terrain.biome(x, ground, z);
        int wood = biome.is(BiomeTags.IS_TAIGA) || biome.is(Biomes.OLD_GROWTH_PINE_TAIGA) || biome.is(Biomes.WINDSWEPT_FOREST) ? 1
                : biome.is(Biomes.BIRCH_FOREST) || biome.is(Biomes.OLD_GROWTH_BIRCH_FOREST) ? 2
                : biome.is(Biomes.DARK_FOREST) || biome.is(Biomes.PALE_GARDEN) ? 3
                : biome.is(BiomeTags.IS_JUNGLE) ? 4
                : biome.is(BiomeTags.IS_SAVANNA) || biome.is(BiomeTags.IS_BADLANDS) ? 5
                : biome.is(Biomes.CHERRY_GROVE) ? 6
                : biome.is(Biomes.SWAMP) || biome.is(Biomes.MANGROVE_SWAMP) ? 7 : 0;
        Rotation rotation = Rotation.values()[Math.floorMod((int) (seed >>> 7), 4)];
        BlockPos origin = new BlockPos(x, ground + 1, z);
        BoundingBox box = new BoundingBox(x - 11, ground - 4, z - 11, x + 11, ground + 14, z + 11);
        return Optional.of(new Structure.GenerationStub(origin,
                builder -> builder.addPiece(new SitePiece(SiteKind.CHARCOAL_CAMP, 0, origin, rotation, seed, new int[] {wood}, box))));
    }

    static void build(VillageBuilder b, SitePiece piece) {
        new Camp(b, WOODS[Math.clamp(piece.data(0), 0, WOODS.length - 1)]).build();
    }

    private static final class Camp {
        private final VillageBuilder b;
        private final Block[] wood;

        Camp(VillageBuilder b, Block[] wood) {
            this.b = b;
            this.wood = wood;
        }

        void build() {
            this.clearing();
            this.kiln(-3, -2);
            this.woodpile();
            this.shelter();
            this.cart();
        }

        /** Die Lichtung: Bäume und Büsche fort, der Boden festgetreten (Gras, Erde, Asche). */
        void clearing() {
            for (int x = -10; x <= 10; x++) {
                for (int z = -10; z <= 10; z++) {
                    double r = Math.sqrt(x * x + z * z);
                    if (r > 9.5) {
                        continue;
                    }
                    for (int y = 0; y <= 13; y++) {
                        BlockState s = this.b.get(x, y, z);
                        if (!s.isAir() && !s.is(net.minecraft.tags.BlockTags.SNOW)) {
                            this.b.set(x, y, z, Blocks.AIR.defaultBlockState());
                        }
                    }
                    for (int y = -1; y >= -4; y--) {
                        if (this.b.get(x, y, z).isSolidRender() && y < -1) {
                            break;
                        }
                        this.b.set(x, y, z, Blocks.DIRT.defaultBlockState());
                    }
                    float n = this.b.noise(x, -1, z);
                    this.b.set(x, -1, z, r < 5 && n < 0.4f ? Blocks.COARSE_DIRT.defaultBlockState() : n < 0.55f ? Blocks.PODZOL.defaultBlockState()
                            : Blocks.GRASS_BLOCK.defaultBlockState());
                }
            }
        }

        /** Der Meiler: ein Hügel aus Erde und Rasen über Scheiten, oben ein rauchendes Feuer. */
        void kiln(int cx, int cz) {
            for (int x = -4; x <= 4; x++) {
                for (int z = -4; z <= 4; z++) {
                    for (int y = 0; y <= 3; y++) {
                        double d = Math.sqrt(x * x + z * z + (y * 1.4) * (y * 1.4));
                        if (d > 3.8) {
                            continue;
                        }
                        float n = this.b.noise(x, y, z);
                        BlockState s = d > 2.8 ? (n < 0.5f ? Blocks.COARSE_DIRT.defaultBlockState() : n < 0.75f ? Blocks.PODZOL.defaultBlockState()
                                : Blocks.DIRT.defaultBlockState())
                                : this.wood[0].defaultBlockState();
                        this.b.set(cx + x, y, cz + z, s);
                    }
                }
            }
            // Luftlöcher mit sichtbaren Scheiten am Fuß, oben das Feuer
            for (Direction d : Direction.Plane.HORIZONTAL) {
                this.b.set(cx + d.getStepX() * 3, 0, cz + d.getStepZ() * 3, this.wood[0].defaultBlockState()
                        .setValue(RotatedPillarBlock.AXIS, d.getAxis()));
            }
            this.b.set(cx, 3, cz, Blocks.CAMPFIRE.defaultBlockState().setValue(net.minecraft.world.level.block.CampfireBlock.SIGNAL_FIRE, true));
            this.b.set(cx, 2, cz, Blocks.HAY_BLOCK);
        }

        /** Gestapelte Scheite (liegende Stämme) und ein Hauklotz. */
        void woodpile() {
            for (int x = 3; x <= 6; x++) {
                for (int y = 0; y <= 1; y++) {
                    for (int z = -6; z <= -5; z++) {
                        if (y == 1 && x == 6) {
                            continue;
                        }
                        this.b.set(x, y, z, this.wood[0].defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z));
                    }
                }
            }
            this.b.set(1, 0, -6, this.wood[0].defaultBlockState());
            this.b.set(1, 1, -6, Blocks.STONE_BUTTON.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.ButtonBlock.FACE, net.minecraft.world.level.block.state.properties.AttachFace.FLOOR));
        }

        /** Ein Unterstand mit Pultdach: Pritsche, Fässer, Truhe; darunter vergraben das Versteck der Köhler. */
        void shelter() {
            int x0 = 2;
            int z0 = 2;
            for (int[] c : new int[][] {{0, 0}, {4, 0}, {0, 3}, {4, 3}}) {
                int h = c[1] == 0 ? 3 : 2;
                for (int y = 0; y < h; y++) {
                    this.b.set(x0 + c[0], y, z0 + c[1], this.wood[3].defaultBlockState());
                }
            }
            for (int x = x0 - 1; x <= x0 + 5; x++) {
                this.b.set(x, 3, z0 - 1, VillageBuilder.stairs(this.wood[2], Direction.SOUTH, Half.BOTTOM));
                this.b.set(x, 3, z0, this.wood[1].defaultBlockState());
                this.b.set(x, 3, z0 + 1, VillageBuilder.stairs(this.wood[2], Direction.SOUTH, Half.TOP));
                this.b.set(x, 2, z0 + 2, VillageBuilder.stairs(this.wood[2], Direction.SOUTH, Half.BOTTOM));
                this.b.set(x, 2, z0 + 3, VillageBuilder.stairs(this.wood[2], Direction.SOUTH, Half.TOP));
                this.b.set(x, 2, z0 + 4, VillageBuilder.stairs(this.wood[2], Direction.SOUTH, Half.BOTTOM));
            }
            this.b.set(x0 + 1, 0, z0 + 1, Blocks.HAY_BLOCK);
            this.b.set(x0 + 2, 0, z0 + 1, Blocks.HAY_BLOCK);
            this.b.set(x0 + 3, 0, z0, Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, Direction.UP));
            this.b.set(x0 + 3, 1, z0, Blocks.LANTERN.defaultBlockState());
            this.b.chest(x0 + 1, 0, z0 + 2, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.NORTH),
                    ModLootTables.SITE_CHARCOAL_CAMP);
            // das Versteck: eine Truhe zwei Blöcke unter der Pritsche
            this.b.chest(x0 + 2, -3, z0 + 1, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.NORTH),
                    ModLootTables.SITE_CHARCOAL_CAMP_SECRET);
            this.b.set(x0 + 2, -2, z0 + 1, Blocks.COARSE_DIRT.defaultBlockState());
        }

        /** Ein Karren mit Rädern aus Falltüren, beladen mit Kohle (Fass). */
        void cart() {
            int x0 = -6;
            int z0 = 4;
            for (int x = x0; x <= x0 + 2; x++) {
                this.b.set(x, 1, z0, this.wood[1].defaultBlockState());
                this.b.set(x, 1, z0 + 1, this.wood[1].defaultBlockState());
            }
            for (int x : new int[] {x0, x0 + 2}) {
                this.b.set(x, 0, z0 - 1, this.wood[4].defaultBlockState().setValue(TrapDoorBlock.FACING, Direction.NORTH)
                        .setValue(TrapDoorBlock.OPEN, true));
                this.b.set(x, 0, z0 + 2, this.wood[4].defaultBlockState().setValue(TrapDoorBlock.FACING, Direction.SOUTH)
                        .setValue(TrapDoorBlock.OPEN, true));
            }
            this.b.set(x0 + 1, 2, z0, Blocks.COAL_BLOCK);
            this.b.set(x0 + 1, 2, z0 + 1, Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, Direction.UP));
            this.b.set(x0 + 3, 1, z0, this.wood[3].defaultBlockState());
            this.b.set(x0 + 4, 1, z0, this.wood[3].defaultBlockState());
        }
    }
}
