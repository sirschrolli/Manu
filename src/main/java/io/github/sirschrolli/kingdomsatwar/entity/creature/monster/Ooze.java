package io.github.sirschrolli.kingdomsatwar.entity.creature.monster;

import io.github.sirschrolli.kingdomsatwar.entity.creature.farm.FarmAnimals;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.cubemob.Slime;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;
import net.minecraft.world.entity.monster.cubemob.AbstractCubeMob;

/**
 * Schleim (statt Vanilla-Schleim): trübgrüner Sumpfschleim mit Algenflocken oder türkisgrauer Höhlenschleim mit
 * Kristallsplittern. Hüpft, teilt sich beim Sterben (die Teile behalten ihre Art) und lässt Schleimbälle fallen.
 */
public class Ooze extends Slime implements Monsters.Varied {
    private static final EntityDataAccessor<Integer> DATA_LOOK = SynchedEntityData.defineId(Ooze.class, EntityDataSerializers.INT);

    public enum Look implements FarmAnimals.Breed {
        SUMPF, HOEHLE
    }

    public Ooze(EntityType<? extends Ooze> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_LOOK, 0);
    }

    public Look getLook() {
        return FarmAnimals.byId(Look.values(), this.entityData.get(DATA_LOOK));
    }

    public void setLook(Look look) {
        this.entityData.set(DATA_LOOK, look.ordinal());
    }

    @Override
    public void chooseLook(ServerLevelAccessor level, BlockPos pos) {
        this.setLook(Monsters.wet(level.getBiome(pos)) && !Monsters.underground(level, pos) ? Look.SUMPF : Look.HOEHLE);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
            @Nullable SpawnGroupData groupData) {
        this.chooseLook(level, this.blockPosition());
        return super.finalizeSpawn(level, difficulty, reason, groupData);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString("Look", this.getLook().id());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setLook(FarmAnimals.byName(Look.values(), input.getStringOr("Look", "sumpf"), Look.SUMPF));
    }

    @Override
    protected Component getTypeName() {
        return Monsters.typeName(this.getType(), this.getLook());
    }

    /** Beim Teilen behalten die kleinen Schleime die Art des großen. */
    @Override
    protected void setUpSplitCube(AbstractCubeMob cube, int halfSize, float xd, float zd) {
        super.setUpSplitCube(cube, halfSize, xd, zd);
        if (cube instanceof Ooze ooze) {
            ooze.setLook(this.getLook());
        }
    }
}
