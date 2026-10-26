package io.github.sirschrolli.kingdomsatwar.worldgen.umbrer;

import io.github.sirschrolli.kingdomsatwar.entity.umbrer.UmbrerEntity;
import io.github.sirschrolli.kingdomsatwar.entity.umbrer.UmbrerGuardEntity;
import io.github.sirschrolli.kingdomsatwar.entity.umbrer.UmbrerProfession;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

/**
 * Ein Bauteil eines Umbrer-Dorfs: der Platz, ein Weg oder ein Gebäude. Gespeichert werden nur Art, Ursprung,
 * Drehung, Startwert (und beim Weg die Höhen) – alles andere ergibt sich daraus, in jedem Chunk gleich.
 */
public class UmbrerVillagePiece extends StructurePiece {
    /** Die Bauteile mit ihrer Grundfläche (Breite × Tiefe, vorn ist die Tür) und Bauhöhe. */
    public enum Kind {
        PLAZA(15, 15, 10),
        ROAD(3, 3, 8),
        HOUSE(7, 7, 11),
        LONGHOUSE(11, 7, 11),
        SMITHY(9, 9, 12),
        HERB_HUT(7, 11, 11),
        CRYSTAL_WORKSHOP(7, 9, 11),
        SEER_TOWER(9, 9, 29),
        WATCHTOWER(5, 5, 17),
        FARM(9, 9, 5),
        CRYSTAL_GARDEN(7, 7, 6);

        final int width;
        final int depth;
        final int height;

        Kind(int width, int depth, int height) {
            this.width = width;
            this.depth = depth;
            this.height = height;
        }

        public int width() {
            return this.width;
        }

        public int depth() {
            return this.depth;
        }

        public int height() {
            return this.height;
        }
    }

    private final Kind kind;
    private final BlockPos origin;
    private final Rotation rotation;
    private final long seed;
    /** Mitte des Dorfplatzes – an sie sind die Bewohner gebunden. */
    private final BlockPos center;
    /** Nur bei Gebäuden: um wie viel der Fußboden über dem Weg vor der Tür liegt (−1, 0 oder 1). */
    private final int step;
    /** Nur beim Weg: Richtung, Höhe (oberster Block) und Brücke ja/nein (1/0) je Schritt ab der Platzmitte. */
    private final Direction roadDirection;
    private final int[] roadHeights;
    private final int[] roadBridges;

    private UmbrerVillagePiece(Kind kind, BlockPos origin, Rotation rotation, long seed, BlockPos center, int step, Direction roadDirection,
            int[] roadHeights, int[] roadBridges, BoundingBox box) {
        super(ModStructures.UMBRER_VILLAGE_PIECE.get(), 0, box);
        this.kind = kind;
        this.origin = origin;
        this.rotation = rotation;
        this.seed = seed;
        this.center = center;
        this.step = step;
        this.roadDirection = roadDirection;
        this.roadHeights = roadHeights;
        this.roadBridges = roadBridges;
    }

    public UmbrerVillagePiece(CompoundTag tag) {
        super(ModStructures.UMBRER_VILLAGE_PIECE.get(), tag);
        this.kind = Kind.valueOf(tag.getStringOr("Kind", "HOUSE"));
        this.origin = new BlockPos(tag.getIntOr("OX", 0), tag.getIntOr("OY", 64), tag.getIntOr("OZ", 0));
        this.rotation = Rotation.values()[Mth.clamp(tag.getIntOr("Rot", 0), 0, 3)];
        this.seed = tag.getLongOr("Seed", 0L);
        this.center = new BlockPos(tag.getIntOr("CX", this.origin.getX()), tag.getIntOr("CY", this.origin.getY()),
                tag.getIntOr("CZ", this.origin.getZ()));
        this.step = tag.getIntOr("Step", 0);
        this.roadDirection = Direction.from3DDataValue(tag.getIntOr("Dir", 2));
        this.roadHeights = tag.getIntArray("Road").orElse(new int[0]);
        this.roadBridges = tag.getIntArray("Bridge").orElse(new int[this.roadHeights.length]);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putString("Kind", this.kind.name());
        tag.putInt("OX", this.origin.getX());
        tag.putInt("OY", this.origin.getY());
        tag.putInt("OZ", this.origin.getZ());
        tag.putInt("Rot", this.rotation.ordinal());
        tag.putLong("Seed", this.seed);
        tag.putInt("CX", this.center.getX());
        tag.putInt("CY", this.center.getY());
        tag.putInt("CZ", this.center.getZ());
        tag.putInt("Step", this.step);
        tag.putInt("Dir", this.roadDirection.get3DDataValue());
        tag.putIntArray("Road", this.roadHeights);
        tag.putIntArray("Bridge", this.roadBridges);
    }

    // =====================================================================================
    // Erzeugen
    // =====================================================================================

    static UmbrerVillagePiece plaza(BlockPos center, int radius, long seed) {
        BoundingBox box = new BoundingBox(center.getX() - radius - 1, center.getY() - 12, center.getZ() - radius - 1,
                center.getX() + radius + 1, center.getY() + 10, center.getZ() + radius + 1);
        return new UmbrerVillagePiece(Kind.PLAZA, center, Rotation.NONE, seed, center, 0, Direction.NORTH, new int[0], new int[0], box);
    }

    static UmbrerVillagePiece road(BlockPos center, Direction direction, int[] heights, int[] bridges, long seed) {
        int length = heights.length - 1;
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (int h : heights) {
            minY = Math.min(minY, h);
            maxY = Math.max(maxY, h);
        }
        int x1 = center.getX() + direction.getStepX() * length;
        int z1 = center.getZ() + direction.getStepZ() * length;
        BoundingBox box = new BoundingBox(Math.min(center.getX(), x1) - 3, minY - 12, Math.min(center.getZ(), z1) - 3,
                Math.max(center.getX(), x1) + 3, maxY + 8, Math.max(center.getZ(), z1) + 3);
        return new UmbrerVillagePiece(Kind.ROAD, center, Rotation.NONE, seed, center, 0, direction, heights, bridges, box);
    }

    static UmbrerVillagePiece building(Kind kind, BlockPos origin, Rotation rotation, long seed, BlockPos center, int step) {
        BoundingBox box = footprint(origin, rotation, kind.width, kind.depth, origin.getY() - 24, origin.getY() + kind.height)
                .inflatedBy(2, 0, 2);
        return new UmbrerVillagePiece(kind, origin, rotation, seed, center, step, Direction.NORTH, new int[0], new int[0], box);
    }

    /** Für Tests: ein einzelnes Gebäude ohne Dorf (Fußboden auf Höhe des Ursprungs, Weg vor der Tür auf gleicher Höhe). */
    public static UmbrerVillagePiece single(Kind kind, BlockPos origin, Rotation rotation, long seed) {
        return building(kind, origin, rotation, seed, origin, 0);
    }

    /** Grundfläche eines gedrehten Rechtecks (Ursprung = vorne links). */
    static BoundingBox footprint(BlockPos origin, Rotation rotation, int width, int depth, int minY, int maxY) {
        BlockPos a = origin.offset(new BlockPos(0, 0, 0).rotate(rotation));
        BlockPos b = origin.offset(new BlockPos(width - 1, 0, depth - 1).rotate(rotation));
        return new BoundingBox(Math.min(a.getX(), b.getX()), minY, Math.min(a.getZ(), b.getZ()),
                Math.max(a.getX(), b.getX()), maxY, Math.max(a.getZ(), b.getZ()));
    }

    public BlockPos origin() {
        return this.origin;
    }

    public Kind kind() {
        return this.kind;
    }

    public Rotation rotation() {
        return this.rotation;
    }

    /** Um wie viel der Fußboden über dem Weg vor der Tür liegt (−1, 0 oder 1). */
    int step() {
        return this.step;
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource ignored,
            BoundingBox chunkBox, ChunkPos chunkPos, BlockPos referencePos) {
        VillageBuilder b = new VillageBuilder(level, chunkBox, this.origin, this.rotation, this.seed);
        switch (this.kind) {
            case PLAZA -> this.plaza(b);
            case ROAD -> this.road(b);
            case HOUSE -> UmbrerBuildings.house(b, this);
            case LONGHOUSE -> UmbrerBuildings.longhouse(b, this);
            case SMITHY -> UmbrerBuildings.smithy(b, this);
            case HERB_HUT -> UmbrerBuildings.herbHut(b, this);
            case CRYSTAL_WORKSHOP -> UmbrerBuildings.crystalWorkshop(b, this);
            case SEER_TOWER -> UmbrerBuildings.seerTower(b, this);
            case WATCHTOWER -> UmbrerBuildings.watchtower(b, this);
            case FARM -> UmbrerBuildings.farm(b, this);
            case CRYSTAL_GARDEN -> UmbrerBuildings.crystalGarden(b, this);
        }
        b.finish();
    }

    // =====================================================================================
    // Bewohner
    // =====================================================================================

    /** Setzt einen Umbrer (oder Wächter), wenn die Stelle im aktuellen Chunk liegt; er bleibt in der Nähe des Dorfs. */
    void spawn(VillageBuilder b, int x, int y, int z, UmbrerProfession profession) {
        BlockPos pos = b.world(x, y, z);
        if (!b.inside(pos)) {
            return;
        }
        WorldGenLevel level = b.level;
        Mob mob;
        if (profession == UmbrerProfession.GUARD) {
            UmbrerGuardEntity guard = ModEntities.UMBRER_GUARD.get().create(level.getLevel(), EntitySpawnReason.STRUCTURE);
            if (guard == null) {
                return;
            }
            guard.settle(this.villageCenter(), 44);
            mob = guard;
        } else {
            UmbrerEntity umbrer = ModEntities.UMBRER.get().create(level.getLevel(), EntitySpawnReason.STRUCTURE);
            if (umbrer == null) {
                return;
            }
            umbrer.setProfession(profession);
            umbrer.settle(this.villageCenter(), 40);
            mob = umbrer;
        }
        mob.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, b.random.nextFloat() * 360.0f, 0.0f);
        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), EntitySpawnReason.STRUCTURE, null);
        mob.setPersistenceRequired();
        level.addFreshEntityWithPassengers(mob);
    }

    /** Die Mitte des Dorfplatzes (für die Bindung der Bewohner). */
    private BlockPos villageCenter() {
        return this.center;
    }

    // =====================================================================================
    // Platz mit Mondbrunnen
    // =====================================================================================

    private void plaza(VillageBuilder b) {
        int r = UmbrerVillageStructure.PLAZA_RADIUS;
        BlockState polished = ShadowBlocks.POLISHED_SHADOW_STONE.base().get().defaultBlockState();
        BlockState bricks = ShadowBlocks.SHADOW_STONE_BRICKS.base().get().defaultBlockState();
        BlockState chiseled = ShadowBlocks.CHISELED_SHADOW_STONE.get().defaultBlockState();
        for (int x = -r; x <= r; x++) {
            for (int z = -r; z <= r; z++) {
                double d = Math.sqrt(x * x + z * z);
                if (d > r + 0.5) {
                    continue;
                }
                b.foundation(x, z, x, z, bricks, bricks, 9);
                BlockState floor = d > r - 1.2 ? bricks : (d > 4.5 && d < 5.5) ? bricks : polished;
                if ((x == 0 || z == 0) && Math.abs(x + z) == 4) {
                    floor = chiseled;
                }
                b.set(x, 0, z, floor);
            }
        }
        // Mondbrunnen: ein Becken mit Wasser, in der Mitte eine Säule, auf der ein Nachtkristall leuchtet wie der Mond
        BlockState rimSlab = VillageBuilder.slab(ShadowBlocks.POLISHED_SHADOW_STONE.slab().get(), SlabType.BOTTOM);
        BlockState wall = ShadowBlocks.SHADOW_STONE_BRICKS.wall() != null
                ? ShadowBlocks.SHADOW_STONE_BRICKS.wall().get().defaultBlockState() : bricks;
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                int ax = Math.abs(x);
                int az = Math.abs(z);
                if (ax == 3 && az == 3) {
                    continue;
                }
                if (ax == 3 || az == 3 || ax == 2 && az == 2) {
                    // Rand: polierter Stein, in den Ecken kleine Laternensäulen
                    b.set(x, 0, z, polished);
                    if (ax == 2 && az == 2) {
                        b.set(x, 1, z, wall);
                        b.set(x, 2, z, ShadowBlocks.MOON_LANTERN.get().defaultBlockState());
                    } else {
                        b.set(x, 1, z, rimSlab);
                    }
                } else {
                    b.set(x, -2, z, bricks);
                    b.set(x, -1, z, Blocks.WATER);
                    b.set(x, 0, z, Blocks.WATER);
                    b.air(x, 1, z, x, 1, z);
                }
            }
        }
        BlockState column = ShadowBlocks.DEEP_SHADOW_STONE.get().defaultBlockState();
        b.set(0, -1, 0, chiseled);
        b.set(0, 0, 0, chiseled);
        b.fill(0, 1, 0, 0, 2, 0, column);
        b.set(0, 3, 0, chiseled);
        b.set(0, 4, 0, ShadowBlocks.NIGHT_CRYSTAL.get().defaultBlockState());
        b.set(0, 5, 0, ShadowBlocks.NIGHT_CRYSTAL_CLUSTER.get().defaultBlockState());
        // vier Wasserspeier: Stufen kopfüber an der Säule
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            b.set(dir.getStepX(), 3, dir.getStepZ(), VillageBuilder.stairs(ShadowBlocks.POLISHED_SHADOW_STONE.stairs().get(), dir.getOpposite(),
                    net.minecraft.world.level.block.state.properties.Half.TOP));
        }
        // Laternenpfähle an den Diagonalen, Bänke dazwischen
        for (int sx = -1; sx <= 1; sx += 2) {
            for (int sz = -1; sz <= 1; sz += 2) {
                lanternPost(b, sx * 5, 0, sz * 5);
            }
        }
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            Direction side = dir.getClockWise();
            int bx = dir.getStepX() * 6 + side.getStepX() * 3;
            int bz = dir.getStepZ() * 6 + side.getStepZ() * 3;
            BlockState bench = ShadowBlocks.SHADOW_PLANKS.stairs().get().defaultBlockState()
                    .setValue(net.minecraft.world.level.block.StairBlock.FACING, dir);
            b.set(bx, 1, bz, bench);
        }
        // zwei Wächter auf dem Platz
        this.spawn(b, 3, 1, -3, UmbrerProfession.GUARD);
        this.spawn(b, -3, 1, 3, UmbrerProfession.GUARD);
    }

    /** Laternenpfahl: Zaunpfosten auf einem Sockel, oben eine Mondlaterne. */
    static void lanternPost(VillageBuilder b, int x, int y, int z) {
        b.set(x, y + 1, z, ShadowBlocks.SHADOW_STONE_BRICKS.base().get());
        b.set(x, y + 2, z, ShadowBlocks.SHADOW_FENCE.get());
        b.set(x, y + 3, z, ShadowBlocks.SHADOW_FENCE.get());
        b.set(x, y + 4, z, ShadowBlocks.MOON_LANTERN.get());
    }

    // =====================================================================================
    // Wege (über Wasser als Laternenbrücken)
    // =====================================================================================

    private void road(VillageBuilder b) {
        WorldGenLevel level = b.level;
        BoundingBox chunkBox = b.chunkBox;
        Direction dir = this.roadDirection;
        Direction side = dir.getClockWise();
        int length = this.roadHeights.length - 1;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        BlockState path = ShadowBlocks.SHADOW_PATH.get().defaultBlockState();
        BlockState soil = ShadowBlocks.SHADOW_SOIL.get().defaultBlockState();
        BlockState bricks = ShadowBlocks.SHADOW_STONE_BRICKS.base().get().defaultBlockState();
        BlockState planks = ShadowBlocks.SHADOW_PLANKS.base().get().defaultBlockState();
        BlockState fence = ShadowBlocks.SHADOW_FENCE.get().defaultBlockState();
        boolean[] bridge = new boolean[length + 1];
        for (int t = UmbrerVillageStructure.PLAZA_RADIUS; t <= length; t++) {
            int y = this.roadHeights[t];
            int cx = this.origin.getX() + dir.getStepX() * t;
            int cz = this.origin.getZ() + dir.getStepZ() * t;
            // Brücke über Wasser und Senken (beim Planen entschieden, damit alle Chunks gleich bauen)
            bridge[t] = t < this.roadBridges.length && this.roadBridges[t] != 0;
            for (int w = -1; w <= 1; w++) {
                int x = cx + side.getStepX() * w;
                int z = cz + side.getStepZ() * w;
                if (!chunkBox.isInside(x, y, z)) {
                    continue;
                }
                if (bridge[t]) {
                    // Laternenbrücke: Bohlen über dem Wasser, Pfeiler aus Schattenholz an den Rändern
                    b.setWorld(pos.set(x, y, z), planks);
                    if (w != 0 && t % 4 == 0) {
                        for (int py = y - 1; py > y - 16; py--) {
                            pos.set(x, py, z);
                            if (VillageBuilder.isSolidGround(level.getBlockState(pos))) {
                                break;
                            }
                            b.setWorld(pos, ShadowBlocks.SHADOW_LOG.get().defaultBlockState());
                        }
                    }
                } else {
                    // Erde darunter auffüllen, Pfad obenauf, Ränder mit Ziegeln
                    for (int py = y - 1; py > y - 6; py--) {
                        pos.set(x, py, z);
                        if (VillageBuilder.isSolidGround(level.getBlockState(pos))) {
                            break;
                        }
                        b.setWorld(pos, soil);
                    }
                    b.setWorld(pos.set(x, y, z), w != 0 && b.noise(x, y, z) < 0.25f ? bricks : path);
                }
                for (int py = y + 1; py <= y + 4; py++) {
                    pos.set(x, py, z);
                    if (!level.getBlockState(pos).isAir()) {
                        b.setWorld(pos, Blocks.AIR.defaultBlockState());
                    }
                }
            }
            // Laternenpfähle abwechselnd links und rechts (nie vor einer Haustür), auf Brücken Geländer
            int lw = t % 16 < 8 ? 2 : -2;
            if (t % 8 == 0) {
                int lx = cx + side.getStepX() * lw;
                int lz = cz + side.getStepZ() * lw;
                if (bridge[t]) {
                    b.setWorld(pos.set(lx, y, lz), planks);
                }
                b.setWorld(pos.set(lx, y + 1, lz), bricks);
                b.setWorld(pos.set(lx, y + 2, lz), fence);
                b.setWorld(pos.set(lx, y + 3, lz), fence);
                b.setWorld(pos.set(lx, y + 4, lz), ShadowBlocks.MOON_LANTERN.get().defaultBlockState());
            }
            if (bridge[t]) {
                for (int w = -2; w <= 2; w += 4) {
                    if (t % 8 == 0 && w == lw) {
                        continue;
                    }
                    int x = cx + side.getStepX() * w;
                    int z = cz + side.getStepZ() * w;
                    b.setWorld(pos.set(x, y, z), planks);
                    b.setWorld(pos.set(x, y + 1, z), fence);
                }
            }
        }
    }

    /** Hilfe für Stufen: untere Stufe. */
    static BlockState bottomSlab(BlockState slab) {
        return slab.hasProperty(SlabBlock.TYPE) ? slab.setValue(SlabBlock.TYPE, SlabType.BOTTOM) : slab;
    }
}
