package io.github.sirschrolli.kingdomsatwar.block.entity;

import io.github.sirschrolli.kingdomsatwar.army.Mercenaries;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlockEntities;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

/**
 * Die Söldnertafel merkt sich die Plätze im Söldnerhaus, an denen die Söldner warten, und den Tag, an dem zuletzt neue
 * kamen. Jeden Morgen füllt sie auf, bis wieder {@link Mercenaries#MAX_AT_HALL} Söldner da sind – aber nur, wenn die
 * Gegend samt ihren Bewohnern geladen ist (sonst kämen zu den schon wartenden noch einmal so viele hinzu).
 */
public class MercenaryBoardBlockEntity extends BlockEntity {
    private final List<BlockPos> spots = new ArrayList<>();
    private String village = "";
    private long lastDay = -1L;

    public MercenaryBoardBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MERCENARY_BOARD.get(), pos, state);
    }

    /** Beim Bau des Dorfs: die Warteplätze (Weltkoordinaten) und der Name des Dorfs. */
    public void setup(List<BlockPos> spots, String village) {
        this.spots.clear();
        for (BlockPos spot : spots) {
            this.spots.add(spot.immutable());
        }
        this.village = village;
        this.setChanged();
    }

    public String village() {
        return this.village;
    }

    public List<BlockPos> spots() {
        return List.copyOf(this.spots);
    }

    public void serverTick(ServerLevel level) {
        if (Math.floorMod(level.getGameTime() + this.worldPosition.asLong(), 100L) != 0L) {
            return;
        }
        this.refreshIfDue(level);
    }

    /** Ist ein neuer Tag angebrochen (oder kam noch nie jemand), füllt die Tafel auf. */
    public void refreshIfDue(ServerLevel level) {
        long day = Math.floorDiv(level.getOverworldClockTime(), 24000L);
        if (day == this.lastDay || !this.loaded(level)) {
            return;
        }
        boolean first = this.lastDay < 0L;
        this.lastDay = day;
        this.setChanged();
        // beim ersten Mal sind sie einfach schon da, später kommen sie morgens an
        this.refill(level, !first);
    }

    /** Sind die Tafel, ihre Plätze und alle Wesen dort geladen? */
    private boolean loaded(ServerLevel level) {
        if (!level.areEntitiesActuallyLoadedAndTicking(ChunkPos.containing(this.worldPosition))) {
            return false;
        }
        for (BlockPos spot : this.spots) {
            if (!level.isLoaded(spot) || !level.areEntitiesActuallyLoadedAndTicking(ChunkPos.containing(spot))) {
                return false;
            }
        }
        return true;
    }

    /** Neue Söldner, bis wieder drei warten; gibt zurück, wie viele kamen. */
    public int refill(ServerLevel level, boolean arrival) {
        List<SoldierEntity> present = Mercenaries.at(level, this.worldPosition);
        int missing = Mercenaries.MAX_AT_HALL - present.size();
        if (missing <= 0) {
            return 0;
        }
        List<BlockPos> free = new ArrayList<>();
        for (BlockPos spot : this.spots.isEmpty() ? this.nearbySpots(level) : this.spots) {
            if (this.standable(level, spot) && present.stream().noneMatch(s -> s.blockPosition().distSqr(spot) < 2.0)) {
                free.add(spot);
            }
        }
        RandomSource random = level.getRandom();
        net.minecraft.util.Util.shuffle(free, random);
        int came = 0;
        boolean knight = present.stream().anyMatch(s -> s.getSoldierType() == SoldierType.KNIGHT);
        for (int i = 0; i < missing && !free.isEmpty(); i++) {
            // es wartet fast immer ein Ritter: dafür kommt man ins Söldnerhaus
            SoldierType type = !knight && random.nextFloat() < 0.8f ? SoldierType.KNIGHT : Mercenaries.randomType(random);
            knight |= type == SoldierType.KNIGHT;
            if (Mercenaries.spawn(level, this.worldPosition, free.removeFirst(), type, Mercenaries.randomRank(random), arrival) != null) {
                came++;
            }
        }
        return came;
    }

    /** Kann ein Söldner hier stehen (fester Boden, zwei Blöcke Luft, kein anderer im Weg)? */
    private boolean standable(ServerLevel level, BlockPos spot) {
        if (!level.isLoaded(spot)) {
            return false;
        }
        BlockState floor = level.getBlockState(spot.below());
        return floor.isFaceSturdy(level, spot.below(), Direction.UP)
                && level.getBlockState(spot).getCollisionShape(level, spot).isEmpty()
                && level.getBlockState(spot.above()).getCollisionShape(level, spot.above()).isEmpty()
                && level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, new AABB(spot)).isEmpty();
    }

    /** Ohne gespeicherte Plätze (z. B. von Hand gesetzt): freie Stellen vor der Tafel. */
    private List<BlockPos> nearbySpots(ServerLevel level) {
        Direction front = this.getBlockState().hasProperty(HorizontalDirectionalBlock.FACING)
                ? this.getBlockState().getValue(HorizontalDirectionalBlock.FACING) : Direction.NORTH;
        List<BlockPos> list = new ArrayList<>();
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                for (int dy = -1; dy <= 1; dy++) {
                    BlockPos pos = this.worldPosition.offset(dx, dy, dz);
                    if ((dx != 0 || dz != 0) && Math.abs(dx) + Math.abs(dz) >= 2 && this.standable(level, pos)) {
                        list.add(pos);
                    }
                }
            }
        }
        BlockPos ahead = this.worldPosition.relative(front, 2);
        list.sort(Comparator.comparingDouble(p -> p.distSqr(ahead)));
        return list.subList(0, Math.min(12, list.size()));                  // genug Plätze, auch wenn drei schon besetzt sind
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("Spots", BlockPos.CODEC.listOf(), this.spots);
        output.putString("Village", this.village);
        output.putLong("LastDay", this.lastDay);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.spots.clear();
        input.read("Spots", BlockPos.CODEC.listOf()).ifPresent(this.spots::addAll);
        this.village = input.getStringOr("Village", "");
        this.lastDay = input.getLongOr("LastDay", -1L);
    }
}
