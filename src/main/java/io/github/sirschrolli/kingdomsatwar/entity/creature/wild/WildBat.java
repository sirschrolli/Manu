package io.github.sirschrolli.kingdomsatwar.entity.creature.wild;

import io.github.sirschrolli.kingdomsatwar.entity.ReplacesVanilla;
import io.github.sirschrolli.kingdomsatwar.entity.creature.farm.FarmAnimals;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Fledermaus – ersetzt die Vanilla-Fledermaus. Abendsegler und Mausohr in allen Höhlen, die dunkle Vampirfledermaus
 * mit roten Augen in Dschungel und Sümpfen. Hängt kopfüber an der Decke und flattert auf, wenn man ihr zu nahe kommt.
 */
public class WildBat extends Bat implements ReplacesVanilla {
    public enum Breed implements FarmAnimals.Breed { ABENDSEGLER, MAUSOHR, VAMPIR }

    private static final EntityDataAccessor<Integer> DATA_BREED =
            SynchedEntityData.defineId(WildBat.class, EntityDataSerializers.INT);

    public WildBat(EntityType<? extends WildBat> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Bat.createAttributes();
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

    public static Breed breedFor(Holder<Biome> biome, RandomSource random) {
        // Gewichte in der Reihenfolge Abendsegler, Mausohr, Vampir
        if (WildAnimals.jungle(biome) || WildAnimals.wetlands(biome)) {
            return Breed.values()[FarmAnimals.pick(random, 20, 20, 60)];
        }
        return Breed.values()[FarmAnimals.pick(random, 50, 47, 3)];
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
            @Nullable SpawnGroupData groupData) {
        this.setBreed(breedFor(level.getBiome(this.blockPosition()), WildAnimals.herd(this.blockPosition(), 43)));
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
        this.setBreed(FarmAnimals.byName(Breed.values(), input.getStringOr("Breed", "abendsegler"), Breed.ABENDSEGLER));
    }

    @Override
    protected Component getTypeName() {
        return FarmAnimals.typeName(this.getType(), this.getBreed());
    }
}
