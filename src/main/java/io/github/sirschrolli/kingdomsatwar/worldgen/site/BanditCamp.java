package io.github.sirschrolli.kingdomsatwar.worldgen.site;

import io.github.sirschrolli.kingdomsatwar.entity.villager.VillageProfession;
import io.github.sirschrolli.kingdomsatwar.entity.villager.VillagerEntity;
import io.github.sirschrolli.kingdomsatwar.loot.ModLootTables;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.VillageBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * Das Räuberlager: Wegelagerer haben sich hinter einer Palisade aus angespitzten Stämmen verschanzt – ein Tor mit roten
 * Wimpeln, ein Wachgerüst mit Leiter, zwei Zelte, ein Feuer mit Sitzstämmen, Zielscheiben an Heuballen, ihre Beute in
 * Kisten und Fässern. In einem Käfig hält die Bande einen Gefangenen fest: Wer das Lager aushebt, kann ihn befreien.
 *
 * <p>Eigene Koordinaten: Mitte des Lagers bei (0, 0, 0), y = 0 ist der Boden, das Tor im Süden.
 */
final class BanditCamp {
    static final double RADIUS = 10.5;

    private BanditCamp() {}

    static Optional<Structure.GenerationStub> plan(Structure.GenerationContext context, SiteTerrain terrain, long seed) {
        ChunkPos chunk = context.chunkPos();
        int x = chunk.getMiddleBlockX();
        int z = chunk.getMiddleBlockZ();
        int[] span = terrain.span(x, z, 10, 5);
        if (terrain.water(x, z) || span[1] - span[0] > 4) {
            return Optional.empty();
        }
        int y = terrain.ground(x, z) + 1;
        var biome = terrain.biome(x, y, z);
        int cover = biome.is(net.minecraft.tags.BiomeTags.IS_BADLANDS) ? 3
                : biome.is(net.minecraft.world.level.biome.Biomes.DESERT) ? 1
                : biome.is(net.minecraft.world.level.biome.Biomes.SNOWY_PLAINS) || biome.is(net.minecraft.world.level.biome.Biomes.GROVE) ? 2 : 0;
        Rotation rotation = Rotation.values()[Math.floorMod((int) (seed >>> 7), 4)];
        BlockPos origin = new BlockPos(x, y, z);
        BoundingBox box = new BoundingBox(x - 13, y - 5, z - 13, x + 13, y + 10, z + 13);
        return Optional.of(new Structure.GenerationStub(origin,
                builder -> builder.addPiece(new SitePiece(SiteKind.BANDIT_CAMP, 0, origin, rotation, seed, new int[] {cover}, box))));
    }

    static void build(VillageBuilder b, SitePiece piece) {
        new Camp(b, piece).build();
    }

    private static final class Camp {
        private final VillageBuilder b;
        private final SitePiece piece;

        Camp(VillageBuilder b, SitePiece piece) {
            this.b = b;
            this.piece = piece;
        }

        void build() {
            this.ground();
            this.palisade();
            this.tower();
            this.tents();
            this.fire();
            this.cage();
            this.loot();
        }

        /** Die Decke des Landes (nach dem Biom beim Planen): Gras, Sand, Schnee oder roter Sand. */
        BlockState surface() {
            return switch (this.piece.data(0)) {
                case 1 -> Blocks.SAND.defaultBlockState();
                case 2 -> Blocks.SNOW_BLOCK.defaultBlockState();
                case 3 -> Blocks.RED_SAND.defaultBlockState();
                default -> Blocks.GRASS_BLOCK.defaultBlockState();
            };
        }

        /** Festgetretener Boden im Lager, außen der Übergang ins Gelände. */
        void ground() {
            for (int x = -12; x <= 12; x++) {
                for (int z = -12; z <= 12; z++) {
                    double r = Math.sqrt(x * x + z * z);
                    if (r > RADIUS + 1.5) {
                        continue;
                    }
                    for (int y = -1; y >= -4; y--) {
                        if (this.b.get(x, y, z).isSolidRender() && y < -1) {
                            break;
                        }
                        this.b.set(x, y, z, Blocks.DIRT.defaultBlockState());
                    }
                    float n = this.b.noise(x, 0, z);
                    // die Decke des Landes (Gras, Sand, Schnee …) bleibt, im Lager festgetreten
                    BlockState cover = this.surface();
                    boolean sandy = cover.is(net.minecraft.tags.BlockTags.SAND);
                    this.b.set(x, -1, z, r > RADIUS ? cover : n < 0.3f ? (sandy ? Blocks.SMOOTH_SANDSTONE.defaultBlockState()
                            : Blocks.COARSE_DIRT.defaultBlockState())
                            : n < 0.55f ? (sandy ? Blocks.SANDSTONE.defaultBlockState() : Blocks.DIRT_PATH.defaultBlockState())
                            : n < 0.7f ? Blocks.GRAVEL.defaultBlockState() : cover);
                    for (int y = 0; y <= 9; y++) {
                        this.b.set(x, y, z, Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }

        /** Palisade aus senkrechten Stämmen mit angespitzten Enden (Zäune), das Tor im Süden mit Wimpeln. */
        void palisade() {
            for (int x = -12; x <= 12; x++) {
                for (int z = -12; z <= 12; z++) {
                    double r = Math.sqrt(x * x + z * z);
                    if (r < RADIUS - 0.5 || r >= RADIUS + 0.5) {
                        continue;
                    }
                    if (z > 0 && Math.abs(x) <= 1) {
                        continue;   // Tor
                    }
                    int h = 3 + (int) (this.b.noise(x, 5, z) * 2);
                    for (int y = 0; y < h; y++) {
                        this.b.set(x, y, z, Blocks.SPRUCE_LOG.defaultBlockState());
                    }
                    this.b.set(x, h, z, Blocks.SPRUCE_FENCE);
                }
            }
            int gz = (int) Math.round(RADIUS);
            for (int x = -2; x <= 2; x++) {
                this.b.set(x, 4, gz, Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X));
            }
            for (int x : new int[] {-2, 2}) {
                for (int y = 0; y <= 3; y++) {
                    this.b.set(x, y, gz, Blocks.SPRUCE_LOG.defaultBlockState());
                }
                this.b.set(x, 5, gz, Blocks.SPRUCE_FENCE);
                this.b.set(x, 6, gz, Blocks.SPRUCE_FENCE);
                this.b.set(x, 6, gz + 1, Blocks.WOOL.pick(DyeColor.RED));
                this.b.set(x, 5, gz + 1, Blocks.WOOL.pick(DyeColor.RED));
            }
        }

        /** Das Wachgerüst im Nordwesten: vier Pfosten, eine Plattform mit Brüstung, eine Leiter. */
        void tower() {
            int cx = -6;
            int cz = -5;
            for (int[] c : new int[][] {{-1, -1}, {1, -1}, {-1, 1}, {1, 1}}) {
                for (int y = 0; y <= 5; y++) {
                    this.b.set(cx + c[0], y, cz + c[1], Blocks.SPRUCE_LOG.defaultBlockState());
                }
                this.b.set(cx + c[0], 6, cz + c[1], Blocks.SPRUCE_FENCE);
                this.b.set(cx + c[0], 7, cz + c[1], Blocks.SPRUCE_FENCE);
            }
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    this.b.set(cx + x, 5, cz + z, Blocks.SPRUCE_PLANKS.defaultBlockState());
                    if (Math.abs(x) == 1 || Math.abs(z) == 1) {
                        if (!(Math.abs(x) == 1 && Math.abs(z) == 1)) {
                            this.b.set(cx + x, 6, cz + z, Blocks.SPRUCE_FENCE);
                        }
                    }
                    this.b.set(cx + x, 8, cz + z, Blocks.SPRUCE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM));
                }
            }
            this.b.set(cx, 6, cz + 1, Blocks.AIR.defaultBlockState());
            this.b.set(cx, 5, cz + 1, Blocks.AIR.defaultBlockState());
            for (int y = 0; y <= 5; y++) {
                this.b.set(cx, y, cz + 2, Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.SOUTH));
            }
            this.b.set(cx, 0, cz + 1, Blocks.SPRUCE_PLANKS.defaultBlockState());
            for (int y = 1; y <= 4; y++) {
                this.b.set(cx, y, cz + 1, Blocks.SPRUCE_PLANKS.defaultBlockState());
            }
            this.b.set(cx, 7, cz, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
        }

        /** Zwei Zelte aus Wolle (dunkel und braun) im Osten. */
        void tents() {
            this.tent(5, -5, DyeColor.BROWN, DyeColor.GRAY);
            this.tent(6, 2, DyeColor.GREEN, DyeColor.BROWN);
        }

        void tent(int cx, int cz, DyeColor a, DyeColor c) {
            for (int z = cz - 2; z <= cz + 1; z++) {
                BlockState cloth = Blocks.WOOL.pick(Math.floorMod(z, 2) == 0 ? a : c).defaultBlockState();
                this.b.set(cx - 2, 0, z, cloth);
                this.b.set(cx - 1, 1, z, cloth);
                this.b.set(cx, 2, z, cloth);
                this.b.set(cx + 1, 1, z, cloth);
                this.b.set(cx + 2, 0, z, cloth);
            }
            for (int x = cx - 1; x <= cx + 1; x++) {
                this.b.set(x, 0, cz - 2, Blocks.WOOL.pick(a));
            }
            this.b.set(cx, 1, cz - 2, Blocks.WOOL.pick(a));
            this.b.set(cx, 0, cz - 1, Blocks.CARPET.pick(DyeColor.RED));
            this.b.set(cx, 0, cz, Blocks.CARPET.pick(DyeColor.RED));
        }

        /** Das Feuer in der Mitte mit Sitzstämmen; Zielscheiben an Heuballen. */
        void fire() {
            this.b.set(0, 0, 0, Blocks.CAMPFIRE);
            this.b.set(0, 0, 2, Blocks.SPRUCE_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X));
            this.b.set(-1, 0, 2, Blocks.SPRUCE_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X));
            this.b.set(2, 0, -1, Blocks.SPRUCE_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z));
            this.b.set(-2, 0, -1, Blocks.SPRUCE_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z));
            for (int x : new int[] {-8, -7}) {
                this.b.set(x, 0, 3, Blocks.HAY_BLOCK);
                this.b.set(x, 1, 3, Blocks.TARGET);
            }
            this.b.set(-3, 0, 6, Blocks.GRINDSTONE.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.GrindstoneBlock.FACE, net.minecraft.world.level.block.state.properties.AttachFace.FLOOR));
            this.b.set(3, 0, 6, Blocks.SPRUCE_FENCE);
            this.b.set(3, 1, 6, Blocks.LANTERN);
        }

        /** Ein Käfig aus Gitterstäben mit einem gefangenen Bewohner. */
        void cage() {
            int cx = -2;
            int cz = -8;
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    boolean edge = Math.abs(x) == 1 || Math.abs(z) == 1;
                    for (int y = 0; y <= 2; y++) {
                        if (edge) {
                            this.b.set(cx + x, y, cz + z, Blocks.IRON_BARS);
                        }
                    }
                    this.b.set(cx + x, 3, cz + z, Blocks.SPRUCE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM));
                }
            }
            this.b.set(cx, 0, cz, Blocks.CARPET.pick(DyeColor.YELLOW));
            BlockPos pos = this.b.world(cx, 0, cz);
            if (this.b.inside(pos)) {
                VillagerEntity captive = ModEntities.VILLAGER.get().create(this.b.level.getLevel(), EntitySpawnReason.STRUCTURE);
                if (captive != null) {
                    long seed = this.piece.seed();
                    VillageProfession[] professions = VillageProfession.values();
                    captive.setup(professions[Math.floorMod((int) (seed >>> 3), professions.length)], Math.floorMod((int) (seed >>> 9), 4),
                            io.github.sirschrolli.kingdomsatwar.economy.town.TownNames.citizen(net.minecraft.util.RandomSource.create(seed),
                                    (seed & 1) == 0), null, "", null, null, seed);
                    captive.setPersistenceRequired();
                    captive.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0f, 0.0f);
                    this.b.level.addFreshEntity(captive);
                }
            }
        }

        /** Die Beute der Bande: eine Kiste und Fässer neben dem Wachgerüst. */
        void loot() {
            this.b.chest(-8, 0, -2, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.EAST), ModLootTables.SITE_BANDIT_CAMP);
            this.b.set(-8, 0, -1, Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, Direction.UP));
            this.b.set(-7, 0, -1, Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, Direction.UP));
            this.b.set(-8, 1, -1, Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, Direction.UP));
            this.b.set(4, 0, -8, VillageBuilder.stairs(Blocks.SPRUCE_STAIRS, Direction.SOUTH, Half.BOTTOM));
        }
    }
}
