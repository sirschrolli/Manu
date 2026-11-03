package io.github.sirschrolli.kingdomsatwar.entity.creature.nether;

import io.github.sirschrolli.kingdomsatwar.entity.ReplacesVanilla;
import io.github.sirschrolli.kingdomsatwar.entity.creature.farm.FarmAnimals;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Monsters;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Moderkeiler (statt Zoglin): ein verwester Glutkeiler mit fleckiger, grünlicher Haut, offenen Stellen, freiliegenden
 * Rippen und Stirnknochen, räudiger Mähne, einem abgebrochenen Hauer und einem glimmenden Auge. Er tut, was der Zoglin
 * tut: greift blind alles an, was lebt – nur andere Moderkeiler und Kriecher nicht –, und fürchtet nichts.
 *
 * <p>Zwei Gestalten: karmesinrot verwest (aus einem Karmesinkeiler) oder aschgrau verwittert.
 */
public class Moderkeiler extends Zoglin implements Monsters.Varied, ReplacesVanilla {
    private static final EntityDataAccessor<Integer> DATA_LOOK = SynchedEntityData.defineId(Moderkeiler.class, EntityDataSerializers.INT);

    public enum Look implements FarmAnimals.Breed {
        MODER_KARMESIN, MODER_ASCHEN
    }

    public Moderkeiler(EntityType<? extends Zoglin> type, Level level) {
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
        this.setLook(NetherFolk.crimson(level.getBiome(pos)) ? Look.MODER_KARMESIN : Look.MODER_ASCHEN);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
            @Nullable SpawnGroupData groupData) {
        if (reason != EntitySpawnReason.CONVERSION) {
            this.chooseLook(level, this.blockPosition());
        }
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
        this.setLook(FarmAnimals.byName(Look.values(), input.getStringOr("Look", "moder_aschen"), Look.MODER_ASCHEN));
    }

    @Override
    protected Component getTypeName() {
        return Monsters.typeName(this.getType(), this.getLook());
    }
}
