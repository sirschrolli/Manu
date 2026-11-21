package io.github.sirschrolli.kingdomsatwar.worldgen.site;

import io.github.sirschrolli.kingdomsatwar.loot.ModLootTables;
import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.VillageBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.BaseCoralWallFanBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * Das Muschelfeld: Auf dem Meeresgrund liegen riesige Muscheln, halb geöffnet – außen gerippte Schalen, innen
 * perlmuttfarben, in der Mitte eine leuchtende Perle. In manchen liegt eine Truhe mit dem, was das Meer hineingespült hat
 * (darunter Schatzkarten); ringsum im Sand (in kalten Meeren im Kies) stecken alte Scherben, die man freipinseln kann. In
 * warmen Meeren wachsen Korallen zwischen den Muscheln, in kalten Seetang.
 *
 * <p>Eigene Koordinaten: Mitte des Felds bei (0, 0, 0), y = 0 ist die Höhe des Meeresgrunds dort.
 */
final class ClamField {
    private ClamField() {}

    static Optional<Structure.GenerationStub> plan(Structure.GenerationContext context, SiteTerrain terrain, long seed) {
        ChunkPos chunk = context.chunkPos();
        int x = chunk.getMiddleBlockX();
        int z = chunk.getMiddleBlockZ();
        int[] span = terrain.span(x, z, 14, 7);
        // die aufgeklappten Deckel ragen zehn Blöcke hoch: sie sollen nicht aus dem Wasser schauen
        if (terrain.waterDepth(x, z) < 13 || span[1] - span[0] > 3) {
            return Optional.empty();
        }
        int ground = terrain.ground(x, z);
        var biome = terrain.biome(x, ground, z);
        boolean warm = biome.is(net.minecraft.world.level.biome.Biomes.WARM_OCEAN) || biome.is(net.minecraft.world.level.biome.Biomes.LUKEWARM_OCEAN)
                || biome.is(net.minecraft.world.level.biome.Biomes.DEEP_LUKEWARM_OCEAN) || !biome.is(BiomeTags.IS_OCEAN);
        BlockPos origin = new BlockPos(x, ground, z);
        BoundingBox box = new BoundingBox(x - 17, ground - 4, z - 17, x + 17, ground + 10, z + 17);
        return Optional.of(new Structure.GenerationStub(origin,
                builder -> builder.addPiece(new SitePiece(SiteKind.CLAM_FIELD, 0, origin, Rotation.NONE, seed, new int[] {warm ? 1 : 0}, box))));
    }

    static void build(VillageBuilder b, SitePiece piece) {
        boolean warm = piece.data(0) == 1;
        int count = 3 + (int) (b.noise(0, 0, 1) * 3);
        for (int i = 0; i < count; i++) {
            double a = i * Math.PI * 2 / count + b.noise(i, 1, 0) * 1.2;
            double d = i == 0 ? 0 : 6 + b.noise(i, 2, 0) * 7;
            int cx = (int) Math.round(Math.cos(a) * d);
            int cz = (int) Math.round(Math.sin(a) * d);
            double r = i == 0 ? 4.2 : 2.6 + b.noise(i, 3, 0) * 1.4;
            double facing = b.noise(i, 4, 0) * Math.PI * 2;
            new Clam(b, piece, cx, cz, r, facing, warm, i == 0 || b.chance(i, 5, 0, 0.35f)).build();
        }
        // verdächtiger Sand (oder Kies) mit alten Scherben, verstreut im Feld
        for (int i = 0; i < 7; i++) {
            int x = (int) ((b.noise(i, 6, 0) - 0.5) * 26);
            int z = (int) ((b.noise(i, 7, 0) - 0.5) * 26);
            int y = surface(b, x, z);
            if (y == Integer.MIN_VALUE) {
                continue;
            }
            brushable(b, x, y, z, warm);
        }
    }

    /** Oberster fester Block (eigene Höhe) in einer Säule des aktuellen Chunks, sonst MIN_VALUE. */
    static int surface(VillageBuilder b, int x, int z) {
        BlockPos pos = b.world(x, 0, z);
        if (!b.chunkBox.isInside(pos.getX(), b.chunkBox.minY() + 1, pos.getZ())) {
            return Integer.MIN_VALUE;
        }
        return b.level.getHeight(Heightmap.Types.OCEAN_FLOOR, pos.getX(), pos.getZ()) - 1 - b.origin.getY();
    }

    /** Nur im natürlichen Grund (nicht in einer Muschel) und nur mit festem Boden darunter – sonst fiele der Sand. */
    static void brushable(VillageBuilder b, int x, int y, int z, boolean warm) {
        BlockPos pos = b.world(x, y, z);
        if (!b.inside(pos)) {
            return;
        }
        BlockState here = b.get(x, y, z);
        boolean natural = here.is(Blocks.SAND) || here.is(Blocks.GRAVEL) || here.is(Blocks.RED_SAND) || here.is(Blocks.CLAY)
                || here.is(net.minecraft.tags.BlockTags.DIRT);
        if (!natural || !b.get(x, y - 1, z).isSolidRender() || !b.get(x, y + 1, z).is(Blocks.WATER)) {
            return;
        }
        b.set(x, y, z, warm ? Blocks.SUSPICIOUS_SAND.defaultBlockState() : Blocks.SUSPICIOUS_GRAVEL.defaultBlockState());
        ResourceKey<LootTable> loot = warm ? BuiltInLootTables.OCEAN_RUIN_WARM_ARCHAEOLOGY : BuiltInLootTables.OCEAN_RUIN_COLD_ARCHAEOLOGY;
        if (b.level.getBlockEntity(pos) instanceof BrushableBlockEntity brushable) {
            brushable.setLootTable(loot, pos.asLong());
        }
    }

    /** Eine Riesenmuschel: unten die Schale als flache Mulde, oben der Deckel, um ein Scharnier hinten aufgeklappt. */
    private static final class Clam {
        private final VillageBuilder b;
        private final SitePiece piece;
        private final int cx;
        private final int cz;
        private final double r;
        private final double facing;
        private final boolean warm;
        private final boolean chest;

        Clam(VillageBuilder b, SitePiece piece, int cx, int cz, double r, double facing, boolean warm, boolean chest) {
            this.b = b;
            this.piece = piece;
            this.cx = cx;
            this.cz = cz;
            this.r = r;
            this.facing = facing;
            this.warm = warm;
            this.chest = chest;
        }

        /** Rippen der Schale: helle und graue Streifen, die vom Scharnier ausstrahlen. */
        BlockState shell(double u, double v) {
            double angle = Math.atan2(v + this.r, u);
            int rib = (int) Math.floor(angle * 9 / Math.PI);
            return Math.floorMod(rib, 2) == 0 ? Blocks.CALCITE.defaultBlockState()
                    : Blocks.DYED_TERRACOTTA.pick(DyeColor.WHITE).defaultBlockState();
        }

        BlockState inner() {
            return this.warm ? Blocks.DYED_TERRACOTTA.pick(DyeColor.PINK).defaultBlockState()
                    : Blocks.DYED_TERRACOTTA.pick(DyeColor.LIGHT_BLUE).defaultBlockState();
        }

        /** Unter der Schale bis zum Grund Sand (die Muschel liegt nicht in der Luft, wenn der Grund abfällt). */
        void fillBelow(double u, double y, double v) {
            double cos = Math.cos(this.facing);
            double sin = Math.sin(this.facing);
            int x = this.cx + (int) Math.round(u * cos - v * sin);
            int z = this.cz + (int) Math.round(u * sin + v * cos);
            for (int d = (int) Math.round(y); d >= -4; d--) {
                if (this.b.get(x, d, z).isSolidRender()) {
                    return;
                }
                this.b.set(x, d, z, this.warm ? Blocks.SAND.defaultBlockState() : Blocks.GRAVEL.defaultBlockState());
            }
        }

        /** Setzt einen Block der Muschel (gedreht nach der Blickrichtung der Muschel). */
        void put(double u, double y, double v, BlockState state) {
            double cos = Math.cos(this.facing);
            double sin = Math.sin(this.facing);
            int x = this.cx + (int) Math.round(u * cos - v * sin);
            int z = this.cz + (int) Math.round(u * sin + v * cos);
            BlockState s = state.hasProperty(BlockStateProperties.WATERLOGGED) ? state.setValue(BlockStateProperties.WATERLOGGED, true) : state;
            this.b.set(x, (int) Math.round(y), z, s);
        }

        void build() {
            int floor = 0;
            double step = 0.5;
            // die untere Schale: eine Mulde, außen Rippen, innen Perlmutt
            for (double u = -this.r; u <= this.r; u += step) {
                for (double v = -this.r; v <= this.r; v += step) {
                    double d = Math.sqrt(u * u + v * v);
                    if (d > this.r) {
                        continue;
                    }
                    double y = floor + 1 + (d / this.r) * (d / this.r) * 1.6;
                    this.put(u, y, v, d > this.r - 0.9 ? this.shell(u, v) : this.inner());
                    this.put(u, y - 1, v, this.shell(u, v));
                    this.fillBelow(u, y - 2, v);
                }
            }
            // der Deckel: eine flache Kuppel, um das Scharnier (hinten, v = −r) um 55° aufgeklappt
            double open = Math.toRadians(55);
            for (double u = -this.r; u <= this.r; u += step) {
                for (double v = -this.r; v <= this.r; v += step) {
                    double d = Math.sqrt(u * u + v * v);
                    if (d > this.r) {
                        continue;
                    }
                    double h = 1.3 * (1 - (d / this.r) * (d / this.r));
                    double along = v + this.r;
                    double ly = floor + 2.6 + along * Math.sin(open) + h * Math.cos(open);
                    double lv = -this.r + along * Math.cos(open) - h * Math.sin(open);
                    this.put(u, ly, lv, d > this.r - 0.9 ? this.shell(u, v) : this.inner());
                }
            }
            // die Perle und vielleicht eine Truhe
            this.put(0, floor + 2, 0.5, Blocks.SEA_LANTERN.defaultBlockState());
            if (this.chest) {
                double cos = Math.cos(this.facing);
                double sin = Math.sin(this.facing);
                int x = this.cx + (int) Math.round(1.6 * cos);
                int z = this.cz + (int) Math.round(1.6 * sin);
                this.b.chest(x, floor + 2, z, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.NORTH)
                        .setValue(BlockStateProperties.WATERLOGGED, true), this.r > 4 ? ModLootTables.SITE_CLAM_BIG : ModLootTables.SITE_CLAM_SMALL);
            }
            // ringsum Korallen (warm) oder Seegurken
            for (int i = 0; i < 6; i++) {
                double a = i * Math.PI / 3 + this.facing;
                int x = this.cx + (int) Math.round(Math.cos(a) * (this.r + 1.5));
                int z = this.cz + (int) Math.round(Math.sin(a) * (this.r + 1.5));
                int y = surface(this.b, x, z);
                if (y == Integer.MIN_VALUE || !this.b.get(x, y + 1, z).is(Blocks.WATER)) {
                    continue;
                }
                if (this.warm) {
                    Block[] corals = {Blocks.TUBE_CORAL_BLOCK, Blocks.BRAIN_CORAL_BLOCK, Blocks.BUBBLE_CORAL_BLOCK, Blocks.FIRE_CORAL_BLOCK,
                            Blocks.HORN_CORAL_BLOCK};
                    Block[] fans = {Blocks.TUBE_CORAL_WALL_FAN, Blocks.BRAIN_CORAL_WALL_FAN, Blocks.BUBBLE_CORAL_WALL_FAN, Blocks.FIRE_CORAL_WALL_FAN,
                            Blocks.HORN_CORAL_WALL_FAN};
                    int c = Math.floorMod(i + (int) (this.piece.seed() & 7), corals.length);
                    this.b.set(x, y + 1, z, corals[c].defaultBlockState());
                    for (Direction dir : Direction.Plane.HORIZONTAL) {
                        if (this.b.chance(x + dir.getStepX(), y + 1, z + dir.getStepZ(), 0.4f)
                                && this.b.get(x + dir.getStepX(), y + 1, z + dir.getStepZ()).is(Blocks.WATER)) {
                            this.b.set(x + dir.getStepX(), y + 1, z + dir.getStepZ(), fans[c].defaultBlockState()
                                    .setValue(BaseCoralWallFanBlock.FACING, dir).setValue(BlockStateProperties.WATERLOGGED, true));
                        }
                    }
                } else if (i % 3 == 0) {
                    this.b.set(x, y + 1, z, Blocks.SEA_PICKLE.defaultBlockState().setValue(SeaPickleBlock.PICKLES, 2 + (i % 3))
                            .setValue(BlockStateProperties.WATERLOGGED, true));
                } else {
                    // Seetang, zwei bis sechs Blöcke hoch
                    int height = 2 + (int) (this.b.noise(x, 8, z) * 5);
                    for (int h = 1; h <= height; h++) {
                        if (!this.b.get(x, y + h + 1, z).is(Blocks.WATER) || h == height) {
                            this.b.set(x, y + h, z, Blocks.KELP.defaultBlockState());
                            break;
                        }
                        this.b.set(x, y + h, z, Blocks.KELP_PLANT.defaultBlockState());
                    }
                }
            }
        }
    }
}
