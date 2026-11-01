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
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Plagegeist (statt Plagegeist der Vanilla-Welt): ein kleiner Kapuzengeist mit Schädelgesicht, Fetzenleib und
 * Flügeln aus zerrissenem Tuch – violett beim Schattenbeschwörer, türkis beim Runenpriester. Fliegt durch Wände und
 * vergeht nach einer Weile wie der Vanilla-Plagegeist.
 */
public class Tormentor extends Vex implements Monsters.Varied {
    private static final EntityDataAccessor<Integer> DATA_LOOK = SynchedEntityData.defineId(Tormentor.class, EntityDataSerializers.INT);

    public enum Look implements FarmAnimals.Breed {
        VIOLETT, TUERKIS
    }

    public Tormentor(EntityType<? extends Tormentor> type, Level level) {
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
        // wie sein Beschwörer: der Runenpriester ruft türkise, der Schattenbeschwörer violette Geister
        if (this.getOwner() instanceof Conjurer conjurer) {
            this.setLook(conjurer.getLook() == Conjurer.Look.RUNEN ? Look.TUERKIS : Look.VIOLETT);
        } else {
            this.setLook(Look.values()[level.getRandom().nextInt(2)]);
        }
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
        this.setLook(FarmAnimals.byName(Look.values(), input.getStringOr("Look", "violett"), Look.VIOLETT));
    }

    @Override
    protected Component getTypeName() {
        return Monsters.typeName(this.getType(), this.getLook());
    }
}
