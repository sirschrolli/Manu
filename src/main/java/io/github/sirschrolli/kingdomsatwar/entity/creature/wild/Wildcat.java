package io.github.sirschrolli.kingdomsatwar.entity.creature.wild;

import io.github.sirschrolli.kingdomsatwar.entity.ReplacesVanilla;
import io.github.sirschrolli.kingdomsatwar.entity.creature.farm.FarmAnimals;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import net.minecraft.core.Holder;
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
import net.minecraft.world.entity.animal.feline.Ocelot;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Wildkatze – ersetzt den Ozelot. Ozelot im Dschungel, Luchs mit Ohrpinseln und Stummelschwanz in Taiga und Schnee,
 * die graue Waldkatze in den Wäldern. Scheu wie der Vanilla-Ozelot: Vertrauen gewinnt man nur mit Geduld und Fisch;
 * Hühner jagen sie gern.
 */
public class Wildcat extends Ocelot implements ReplacesVanilla {
    public enum Breed implements FarmAnimals.Breed { OZELOT, LUCHS, WALDKATZE }

    private static final EntityDataAccessor<Integer> DATA_BREED =
            SynchedEntityData.defineId(Wildcat.class, EntityDataSerializers.INT);

    public Wildcat(EntityType<? extends Wildcat> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Ocelot.createAttributes();
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

    public static Breed breedFor(Holder<Biome> biome) {
        if (WildAnimals.jungle(biome)) {
            return Breed.OZELOT;
        }
        if (WildAnimals.snowy(biome) || WildAnimals.taiga(biome)) {
            return Breed.LUCHS;
        }
        return Breed.WALDKATZE;
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
            @Nullable SpawnGroupData groupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, groupData);
        this.setBreed(breedFor(level.getBiome(this.blockPosition())));
        return data;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString("Breed", this.getBreed().id());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setBreed(FarmAnimals.byName(Breed.values(), input.getStringOr("Breed", "ozelot"), Breed.OZELOT));
    }

    @Override
    public @Nullable Wildcat getBreedOffspring(ServerLevel level, AgeableMob partner) {
        Wildcat kitten = ModEntities.WILDCAT.get().create(level, EntitySpawnReason.BREEDING);
        if (kitten != null) {
            Breed other = partner instanceof Wildcat cat ? cat.getBreed() : this.getBreed();
            kitten.setBreed(this.random.nextBoolean() ? this.getBreed() : other);
        }
        return kitten;
    }

    @Override
    protected Component getTypeName() {
        return FarmAnimals.typeName(this.getType(), this.getBreed());
    }
}
