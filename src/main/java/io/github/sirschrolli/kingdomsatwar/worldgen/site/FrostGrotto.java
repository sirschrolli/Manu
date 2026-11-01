package io.github.sirschrolli.kingdomsatwar.worldgen.site;

import io.github.sirschrolli.kingdomsatwar.loot.ModLootTables;
import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.VillageBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * Die Frostgrotte: Ein Einsiedler hat sich in einen Schneehügel eine Höhle gegraben – außen nur ein weißer Buckel mit
 * einem Eingang aus Eisblöcken und einer Rauchfahne, innen eine warme Stube unter einer Kuppel aus Packeis: Felle auf dem
 * Boden, ein Feuer in der Mitte, Bett, Werkbank, Ofen, Vorräte. Unter einem Fell liegt eine Falltür – darunter der
 * versteckte Keller des Einsiedlers mit seinem Braustand und seinem Schatz.
 *
 * <p>Eigene Koordinaten: Mitte der Stube bei (0, 0, 0), y = 0 ist der Boden, der Eingang im Süden.
 */
final class FrostGrotto {
    private FrostGrotto() {}

    static Optional<Structure.GenerationStub> plan(Structure.GenerationContext context, SiteTerrain terrain, long seed) {
        ChunkPos chunk = context.chunkPos();
        int x = chunk.getMiddleBlockX();
        int z = chunk.getMiddleBlockZ();
        int[] span = terrain.span(x, z, 8, 4);
        if (terrain.water(x, z) || span[1] - span[0] > 4) {
            return Optional.empty();
        }
        int y = terrain.ground(x, z);
        Rotation rotation = Rotation.values()[Math.floorMod((int) (seed >>> 7), 4)];
        BlockPos origin = new BlockPos(x, y, z);
        BoundingBox box = SitePiece.box(origin, rotation, -10, -7, -10, 10, 9, 13);
        return Optional.of(new Structure.GenerationStub(origin,
                builder -> builder.addPiece(new SitePiece(SiteKind.FROST_GROTTO, 0, origin, rotation, seed, new int[0], box))));
    }

    static void build(VillageBuilder b, SitePiece piece) {
        new Grotto(b).build();
    }

    private static final class Grotto {
        private final VillageBuilder b;

        Grotto(VillageBuilder b) {
            this.b = b;
        }

        /** Abstand zum Hügel (≤ 1 innerhalb): unregelmäßig, mit Buckeln und Felsnasen. */
        double mound(int x, int y, int z) {
            double ax = x / 8.5;
            double az = z / 7.5;
            double ay = y / 6.5;
            double bump = Math.sin(x * 0.7 + 1.3) * Math.cos(z * 0.6 + 0.4) * 0.12 + (this.b.noise(x / 2, 0, z / 2) - 0.5) * 0.18;
            return ax * ax + az * az + ay * ay - bump;
        }

        /** Außen am Hügel: Fels, Packeis und Schnee in Flecken, oben Schnee. */
        BlockState crust(int x, int y, int z, double m) {
            float n = this.b.noise(x / 2, y / 2, z / 2);
            if (y >= 4 && m > 0.7) {
                return Blocks.SNOW_BLOCK.defaultBlockState();
            }
            if (n < 0.3f) {
                return Blocks.STONE.defaultBlockState();
            }
            if (n < 0.45f) {
                return Blocks.ANDESITE.defaultBlockState();
            }
            if (n < 0.62f) {
                return Blocks.PACKED_ICE.defaultBlockState();
            }
            if (n < 0.7f) {
                return Blocks.COBBLESTONE.defaultBlockState();
            }
            return Blocks.SNOW_BLOCK.defaultBlockState();
        }

        double room(int x, int y, int z) {
            double ax = x / 5.6;
            double az = z / 4.8;
            double ay = (y - 0.5) / 4.2;
            return ax * ax + az * az + ay * ay;
        }

        void build() {
            for (int x = -10; x <= 10; x++) {
                for (int z = -10; z <= 10; z++) {
                    // Fundament unter dem Hügel
                    for (int y = -1; y >= -4; y--) {
                        if (this.mound(x, 0, z) > 1.0) {
                            break;
                        }
                        BlockState s = this.b.get(x, y, z);
                        if (s.isSolidRender() && y < -1) {
                            break;
                        }
                        this.b.set(x, y, z, Blocks.PACKED_ICE.defaultBlockState());
                    }
                    for (int y = 0; y <= 8; y++) {
                        double m = this.mound(x, y, z);
                        if (m > 1.0) {
                            continue;
                        }
                        double r = this.room(x, y, z);
                        BlockState state;
                        if (r <= 1.0 && y >= 1) {
                            state = Blocks.AIR.defaultBlockState();
                        } else if (r <= 1.35 && y >= 1) {
                            state = this.b.chance(x, y, z, 0.15f) ? Blocks.BLUE_ICE.defaultBlockState() : Blocks.PACKED_ICE.defaultBlockState();
                        } else {
                            state = this.crust(x, y, z, m);
                        }
                        this.b.set(x, y, z, state);
                    }
                    // Schneedecke obenauf
                    int top = -1;
                    for (int y = 8; y >= 0; y--) {
                        if (this.mound(x, y, z) <= 1.0) {
                            top = y;
                            break;
                        }
                    }
                    if (top >= 0 && this.b.get(x, top + 1, z).isAir() && this.b.chance(x, top, z, 0.7f)) {
                        this.b.set(x, top + 1, z, Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, 1 + (int) (this.b.noise(x, 9, z) * 3)));
                    }
                }
            }
            this.floor();
            this.entrance();
            this.furnish();
            this.cellar();
        }

        void floor() {
            for (int x = -5; x <= 5; x++) {
                for (int z = -4; z <= 4; z++) {
                    if (this.room(x, 1, z) <= 1.0) {
                        this.b.set(x, 0, z, Math.floorMod(x, 2) == 0 ? Blocks.SPRUCE_PLANKS.defaultBlockState() : Blocks.STRIPPED_SPRUCE_LOG
                                .defaultBlockState().setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS, Direction.Axis.Z));
                    }
                }
            }
        }

        /**
         * Der Eingang: ein kurzer Stollen in den Hügel, davor ein Vorbau aus Fichtenholz (Pfosten, Pultdach, Tür), an
         * dem eine Laterne hängt, daneben Brennholz.
         */
        void entrance() {
            for (int z = 4; z <= 9; z++) {
                for (int x = -1; x <= 1; x++) {
                    for (int y = 1; y <= 3; y++) {
                        this.b.set(x, y, z, y == 3 && Math.abs(x) == 1 ? Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState()
                                .setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS, Direction.Axis.Z) : Blocks.AIR.defaultBlockState());
                    }
                    this.b.set(x, 0, z, Blocks.SPRUCE_PLANKS.defaultBlockState());
                }
            }
            // Türrahmen
            for (int y = 1; y <= 3; y++) {
                this.b.set(-1, y, 7, Blocks.SPRUCE_LOG.defaultBlockState());
                this.b.set(1, y, 7, Blocks.SPRUCE_LOG.defaultBlockState());
            }
            this.b.set(0, 3, 7, Blocks.SPRUCE_LOG.defaultBlockState().setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS,
                    Direction.Axis.X));
            this.b.door(0, 1, 7, Blocks.SPRUCE_DOOR, Direction.NORTH);
            // Vorbau: zwei Pfosten, ein Pultdach
            for (int x : new int[] {-2, 2}) {
                for (int y = 1; y <= 3; y++) {
                    this.b.set(x, y, 10, Blocks.SPRUCE_FENCE);
                }
            }
            for (int x = -3; x <= 3; x++) {
                this.b.set(x, 4, 8, Blocks.SPRUCE_PLANKS.defaultBlockState());
                this.b.set(x, 4, 9, VillageBuilder.stairs(Blocks.SPRUCE_STAIRS, Direction.NORTH, Half.BOTTOM));
                this.b.set(x, 4, 10, VillageBuilder.stairs(Blocks.SPRUCE_STAIRS, Direction.NORTH, Half.BOTTOM));
                this.b.set(x, 4, 11, VillageBuilder.stairs(Blocks.SPRUCE_STAIRS, Direction.NORTH, Half.TOP));
                this.b.set(x, 5, 8, Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, 2));
            }
            this.b.set(1, 3, 10, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
            for (int x = 3; x <= 4; x++) {
                for (int y = 1; y <= 2; y++) {
                    this.b.set(x, y, 9, Blocks.SPRUCE_LOG.defaultBlockState().setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS,
                            Direction.Axis.Z));
                }
            }
            this.b.set(-3, 1, 9, Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, Direction.UP));
        }

        void furnish() {
            // Feuer in der Mitte, darüber ein Rauchloch
            this.b.set(0, 0, 0, Blocks.COBBLESTONE);
            this.b.set(0, 1, 0, Blocks.CAMPFIRE);
            for (int y = 2; y <= 8; y++) {
                if (this.room(0, y, 0) > 1.0) {
                    this.b.set(0, y, 0, Blocks.AIR.defaultBlockState());
                }
            }
            // Felle
            for (int[] c : new int[][] {{-2, 1}, {-2, 2}, {2, 1}, {1, 2}, {-1, -2}, {2, -1}, {-3, -2}}) {
                this.b.set(c[0], 1, c[1], Blocks.CARPET.pick(this.b.chance(c[0], 1, c[1], 0.5f) ? DyeColor.WHITE : DyeColor.LIGHT_GRAY));
            }
            this.b.bed(4, 1, -1, Blocks.BED.pick(DyeColor.BROWN), Direction.SOUTH);
            this.b.set(-4, 1, 1, Blocks.CRAFTING_TABLE);
            this.b.set(-4, 1, 0, Blocks.FURNACE.defaultBlockState().setValue(FurnaceBlock.FACING, Direction.EAST));
            this.b.set(-4, 1, -1, Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, Direction.UP));
            this.b.chest(4, 1, 2, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.WEST), ModLootTables.SITE_FROST_GROTTO);
            this.b.set(2, 4, -2, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
            this.b.set(-2, 4, 2, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
            this.b.set(1, 1, -3, Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.CANDLES, 2).setValue(CandleBlock.LIT, true));
        }

        /** Der versteckte Keller unter dem Fell im Westen: Falltür, Leiter, Braustand, Kessel und der Schatz. */
        void cellar() {
            this.b.set(-3, 0, -2, Blocks.SPRUCE_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.HALF, Half.TOP)
                    .setValue(TrapDoorBlock.FACING, Direction.SOUTH));
            for (int y = -1; y >= -4; y--) {
                this.b.set(-3, y, -2, Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.SOUTH));
                this.b.set(-3, y, -3, Blocks.STONE_BRICKS);
            }
            for (int x = -6; x <= 0; x++) {
                for (int z = -2; z <= 3; z++) {
                    for (int y = -6; y <= -2; y++) {
                        boolean shell = x == -6 || x == 0 || z == 3 || y == -6 || y == -2;
                        if (z == -2 && x == -3 && y > -6) {
                            continue;
                        }
                        this.b.set(x, y, z, shell ? (this.b.chance(x, y, z, 0.3f) ? Blocks.PACKED_ICE.defaultBlockState()
                                : Blocks.STONE_BRICKS.defaultBlockState()) : Blocks.AIR.defaultBlockState());
                    }
                }
            }
            for (int x = -5; x <= -1; x++) {
                this.b.set(x, -5, -2, Blocks.STONE_BRICKS);
                for (int y = -4; y <= -3; y++) {
                    if (x != -3) {
                        this.b.set(x, y, -2, Blocks.STONE_BRICKS);
                    }
                }
            }
            this.b.set(-5, -5, 2, Blocks.BREWING_STAND);
            this.b.set(-4, -5, 2, Blocks.CAULDRON);
            this.b.set(-1, -5, 2, Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, Direction.UP));
            this.b.chest(-1, -5, 0, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.WEST), ModLootTables.SITE_FROST_GROTTO_CELLAR);
            this.b.set(-5, -5, 0, Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.CANDLES, 3).setValue(CandleBlock.LIT, true));
            this.b.set(-3, -3, 1, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
        }
    }
}
