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
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.animal.polarbear.PolarBear;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Bär – ersetzt den Eisbären. Eisbär auf Schnee und Eis, Braunbär mit hohem Schulterbuckel in den alten Wäldern und
 * der Taiga, Schwarzbär in den übrigen Wäldern. Alle verhalten sich wie der Vanilla-Eisbär: friedlich, bis man ihren
 * Jungen zu nahe kommt – dann richten sie sich auf und schlagen zu.
 */
public class Bear extends PolarBear implements ReplacesVanilla {
    public enum Breed implements FarmAnimals.Breed { EIS, BRAUN, SCHWARZ }

    private static final EntityDataAccessor<Integer> DATA_BREED =
            SynchedEntityData.defineId(Bear.class, EntityDataSerializers.INT);

    public Bear(EntityType<? extends Bear> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PolarBear.createAttributes();
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
        if (WildAnimals.snowy(biome) || biome.is(Biomes.FROZEN_OCEAN) || biome.is(Biomes.DEEP_FROZEN_OCEAN)) {
            return Breed.EIS;
        }
        if (WildAnimals.taiga(biome) || biome.is(Biomes.OLD_GROWTH_BIRCH_FOREST) || biome.is(Biomes.WINDSWEPT_FOREST)) {
            return FarmAnimals.pick(random, 75, 25) == 0 ? Breed.BRAUN : Breed.SCHWARZ;
        }
        return FarmAnimals.pick(random, 35, 65) == 0 ? Breed.BRAUN : Breed.SCHWARZ;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
            @Nullable SpawnGroupData groupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, groupData);
        this.setBreed(breedFor(level.getBiome(this.blockPosition()), WildAnimals.herd(this.blockPosition(), 41)));
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
        this.setBreed(FarmAnimals.byName(Breed.values(), input.getStringOr("Breed", "eis"), Breed.EIS));
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        Bear cub = ModEntities.BEAR.get().create(level, EntitySpawnReason.BREEDING);
        if (cub != null) {
            cub.setBreed(this.getBreed());
        }
        return cub;
    }

    @Override
    protected Component getTypeName() {
        return FarmAnimals.typeName(this.getType(), this.getBreed());
    }
}
