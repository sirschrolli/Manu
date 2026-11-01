package io.github.sirschrolli.kingdomsatwar.entity.creature.farm;

import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.animal.equine.AbstractChestedHorse;
import net.minecraft.world.entity.animal.equine.Donkey;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/** Esel – ersetzt den Vanilla-Esel: Grauesel mit Schulterkreuz oder brauner Esel; trägt Packtaschen (Truhe). */
public class FarmDonkey extends Donkey {
    public enum Breed implements FarmAnimals.Breed { GRAU, BRAUN }

    private static final EntityDataAccessor<Integer> DATA_BREED =
            SynchedEntityData.defineId(FarmDonkey.class, EntityDataSerializers.INT);

    public FarmDonkey(EntityType<? extends FarmDonkey> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractChestedHorse.createBaseChestedHorseAttributes();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_BREED, 0);
    }

    public Breed getBreed() {
        return FarmAnimals.byId(Breed.values(), this.entityData.get(DATA_BREED));
    }

    public void setBreed(Breed breed) {
        this.entityData.set(DATA_BREED, breed.ordinal());
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
            @Nullable SpawnGroupData groupData) {
        this.setBreed(level.getRandom().nextInt(10) < 7 ? Breed.GRAU : Breed.BRAUN);
        return super.finalizeSpawn(level, difficulty, reason, groupData);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString("Breed", this.getBreed().id());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setBreed(FarmAnimals.byName(Breed.values(), input.getStringOr("Breed", "grau"), Breed.GRAU));
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        if (partner instanceof Horse horse) {
            FarmMule mule = ModEntities.FARM_MULE.get().create(level, EntitySpawnReason.BREEDING);
            if (mule != null) {
                this.setOffspringAttributes(partner, mule);
                boolean black = horse instanceof FarmHorse farm && farm.getBreed() == FarmHorse.Breed.FRIESE;
                mule.setBreed(black || this.random.nextInt(4) == 0 ? FarmMule.Breed.RAPPE : FarmMule.Breed.BRAUN);
            }
            return mule;
        }
        FarmDonkey foal = ModEntities.FARM_DONKEY.get().create(level, EntitySpawnReason.BREEDING);
        if (foal != null) {
            this.setOffspringAttributes(partner, foal);
            Breed other = partner instanceof FarmDonkey donkey ? donkey.getBreed() : this.getBreed();
            foal.setBreed(this.random.nextBoolean() ? this.getBreed() : other);
        }
        return foal;
    }

    @Override
    protected Component getTypeName() {
        return FarmAnimals.typeName(this.getType(), this.getBreed());
    }
}
