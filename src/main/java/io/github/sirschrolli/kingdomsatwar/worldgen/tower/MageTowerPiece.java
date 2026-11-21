package io.github.sirschrolli.kingdomsatwar.worldgen.tower;

import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.entity.boss.mage.MageBossEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures;
import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.VillageBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

/**
 * Ein Magierturm als ein Bauteil. Gespeichert werden nur Element, Ursprung (Mitte, Höhe der Terrasse), Drehung und
 * Startwert; gebaut wird Chunk für Chunk von {@link MageTowerBuilder}. Im Chunk mit der Mitte des Sanktums setzt der Bau
 * auch den Erzmagier an seinen Platz, wo er meditiert, bis jemand den Saal betritt.
 */
public class MageTowerPiece extends StructurePiece {
    private final DragonType element;
    private final BlockPos origin;
    private final Rotation rotation;
    private final long seed;

    public MageTowerPiece(DragonType element, BlockPos origin, Rotation rotation, long seed) {
        super(ModStructures.MAGE_TOWER_PIECE.get(), 0, makeBox(origin));
        this.element = element;
        this.origin = origin;
        this.rotation = rotation;
        this.seed = seed;
    }

    public MageTowerPiece(CompoundTag tag) {
        super(ModStructures.MAGE_TOWER_PIECE.get(), tag);
        this.element = DragonType.byId(tag.getStringOr("Element", "fire"));
        this.origin = new BlockPos(tag.getIntOr("OX", 0), tag.getIntOr("OY", 64), tag.getIntOr("OZ", 0));
        this.rotation = Rotation.values()[Mth.clamp(tag.getIntOr("Rot", 0), 0, 3)];
        this.seed = tag.getLongOr("Seed", 0L);
    }

    static BoundingBox makeBox(BlockPos o) {
        int r = MageTowerBuilder.RADIUS;
        return new BoundingBox(o.getX() - r, o.getY() - MageTowerBuilder.BELOW, o.getZ() - r,
                o.getX() + r, o.getY() + MageTowerBuilder.ABOVE, o.getZ() + r);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putString("Element", this.element.id());
        tag.putInt("OX", this.origin.getX());
        tag.putInt("OY", this.origin.getY());
        tag.putInt("OZ", this.origin.getZ());
        tag.putInt("Rot", this.rotation.ordinal());
        tag.putLong("Seed", this.seed);
    }

    public DragonType element() {
        return this.element;
    }

    public BlockPos origin() {
        return this.origin;
    }

    public Rotation rotation() {
        return this.rotation;
    }

    /** Wo der Erzmagier meditiert (Weltkoordinaten). */
    public BlockPos home() {
        return this.origin.offset(new BlockPos(0, MageTowerBuilder.SANCTUM + 1, 0).rotate(this.rotation));
    }

    /** Eine Stelle in Turmkoordinaten als Weltposition. */
    public BlockPos world(int x, int y, int z) {
        return this.origin.offset(new BlockPos(x, y, z).rotate(this.rotation));
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random,
            BoundingBox chunkBB, ChunkPos chunkPos, BlockPos referencePos) {
        VillageBuilder b = new VillageBuilder(level, chunkBB, this.origin, this.rotation, this.seed);
        RandomState randomState = level.getLevel().getChunkSource().randomState();
        new MageTowerBuilder(b, this.element, (x, z) -> {
            BlockPos at = b.world(x, 0, z);
            return generator.getBaseHeight(at.getX(), at.getZ(), Heightmap.Types.OCEAN_FLOOR_WG, level, randomState) - 1 - this.origin.getY();
        }).build();
        b.finish();
        BlockPos home = MageTowerBuilder.home(b);
        if (b.inside(home)) {
            this.spawnMage(level, b, home);
        }
    }

    /** Der Erzmagier meditiert in der Mitte des Sanktums und blickt zur Treppe. */
    private void spawnMage(WorldGenLevel level, VillageBuilder b, BlockPos home) {
        MageBossEntity mage = ModEntities.MAGES.get(this.element).get().create(level.getLevel(), EntitySpawnReason.STRUCTURE);
        if (mage == null) {
            return;
        }
        BlockPos arrival = MageTowerBuilder.arrival(b);
        float yaw = (float) (Mth.atan2(arrival.getZ() - home.getZ(), arrival.getX() - home.getX()) * Mth.RAD_TO_DEG) - 90.0f;
        mage.snapTo(home.getX() + 0.5, home.getY() + 0.35, home.getZ() + 0.5, yaw, 0.0f);
        mage.setYHeadRot(yaw);
        mage.setYBodyRot(yaw);
        mage.setHome(home);
        mage.setPersistenceRequired();
        level.addFreshEntityWithPassengers(mage);
    }
}
