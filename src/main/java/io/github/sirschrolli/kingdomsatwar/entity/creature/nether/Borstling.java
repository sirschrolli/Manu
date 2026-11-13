package io.github.sirschrolli.kingdomsatwar.entity.creature.nether;

import io.github.sirschrolli.kingdomsatwar.entity.ReplacesVanilla;
import io.github.sirschrolli.kingdomsatwar.entity.creature.farm.FarmAnimals;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Monsters;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Borstling (statt Piglin): ein stolzes Keilervolk des Nethers mit Hauern, Nasenring, Borstenkamm und Zopf, im
 * Waffenrock mit Fellumhang. Er tut, was der Piglin tut: liebt Gold, tauscht Goldbarren gegen allerlei Schätze, jagt
 * Glutkeiler, greift an, wer kein Gold trägt, und wird in der Oberwelt zum Moderborstling.
 *
 * <p>Drei Gestalten: Karmesinborstling (im Karmesinwald, rote Tracht, schwarze Kriegsbemalung), Aschenborstling (in
 * Ödland, Seelensandtal und Basaltdeltas, aschfarbene Tracht, weiße Bemalung) und – selten – der reich geschmückte
 * Goldborstling.
 */
public class Borstling extends Piglin implements Monsters.Varied, ReplacesVanilla {
    private static final EntityDataAccessor<Integer> DATA_LOOK = SynchedEntityData.defineId(Borstling.class, EntityDataSerializers.INT);

    public enum Look implements FarmAnimals.Breed {
        KARMESIN, ASCHEN, GOLD
    }

    public Borstling(EntityType<? extends AbstractPiglin> type, Level level) {
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
        Holder<Biome> biome = level.getBiome(pos);
        if (level.getRandom().nextInt(10) == 0) {
            this.setLook(Look.GOLD);
        } else {
            this.setLook(NetherFolk.crimson(biome) ? Look.KARMESIN : Look.ASCHEN);
        }
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
            @Nullable SpawnGroupData groupData) {
        this.chooseLook(level, this.blockPosition());
        return super.finalizeSpawn(level, difficulty, reason, groupData);
    }

    /** In der Oberwelt wird aus ihm ein Moderborstling – karmesinrot oder aschgrau verwest, wie er war. */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends Mob> @Nullable T convertTo(EntityType<T> type, ConversionParams params, EntitySpawnReason reason,
            ConversionParams.AfterConversion<T> after) {
        if (type != EntityTypes.ZOMBIFIED_PIGLIN) {
            return super.convertTo(type, params, reason, after);
        }
        RottenBristling.Look look = this.getLook() == Look.KARMESIN ? RottenBristling.Look.MODER_KARMESIN : RottenBristling.Look.MODER_ASCHEN;
        return (T) super.convertTo(ModEntities.ZOMBIFIED_PIGLIN.get(), params, reason, rotten -> {
            rotten.setLook(look);
            after.finalizeConversion((T) rotten);
        });
    }

    @Override
    public void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString("Look", this.getLook().id());
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setLook(FarmAnimals.byName(Look.values(), input.getStringOr("Look", "aschen"), Look.ASCHEN));
    }

    @Override
    protected Component getTypeName() {
        return Monsters.typeName(this.getType(), this.getLook());
    }
}
