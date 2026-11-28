package io.github.sirschrolli.kingdomsatwar.entity.creature.monster;

import io.github.sirschrolli.kingdomsatwar.entity.ReplacesVanilla;
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
import net.minecraft.world.entity.animal.equine.ZombieHorse;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Zombiepferd: fahl grau-grün mit freiliegenden Rippen oder ein verwesender schwarzer Rappe; lässt sich wie das
 * Vanilla-Zombiepferd satteln, reiten und mit Rossharnisch ausstatten.
 */
public class UndeadHorse extends ZombieHorse implements Monsters.Varied, ReplacesVanilla {
    private static final EntityDataAccessor<Integer> DATA_LOOK = SynchedEntityData.defineId(UndeadHorse.class, EntityDataSerializers.INT);

    public enum Look implements FarmAnimals.Breed {
        GRAU, RAPPE
    }

    public UndeadHorse(EntityType<? extends UndeadHorse> type, Level level) {
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
        this.setLook(Look.values()[FarmAnimals.pick(level.getRandom(), 60, 40)]);
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
        this.setLook(FarmAnimals.byName(Look.values(), input.getStringOr("Look", "grau"), Look.GRAU));
    }

    @Override
    protected Component getTypeName() {
        return Monsters.typeName(this.getType(), this.getLook());
    }
}
