package io.github.sirschrolli.kingdomsatwar.entity.creature.farm;

import io.github.sirschrolli.kingdomsatwar.entity.ReplacesVanilla;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.worldgen.overworld.OverworldBiomes;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Ziege – ersetzt die Bergziege. Steinbock und Gämse in den Felsgipfeln, die zottelige Schneeziege an den Schneehängen,
 * die Hausziege auf Bergwiesen. Rammen, weite Sprünge, Hörner (die abbrechen können), Ziegenhörner und Milch wie bei
 * Vanilla.
 */
public class FarmGoat extends Goat implements ReplacesVanilla {
    public enum Breed implements FarmAnimals.Breed { STEINBOCK, GAEMSE, SCHNEE, HAUS }

    private static final EntityDataAccessor<Integer> DATA_BREED =
            SynchedEntityData.defineId(FarmGoat.class, EntityDataSerializers.INT);
    private static final UniformInt TIME_BETWEEN_LONG_JUMPS = UniformInt.of(600, 1200);
    private static final UniformInt TIME_BETWEEN_RAMS = UniformInt.of(600, 6000);

    public FarmGoat(EntityType<? extends FarmGoat> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Goat.createAttributes();
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
        if (biome.is(Biomes.FROZEN_PEAKS) || biome.is(Biomes.SNOWY_SLOPES) || biome.is(Biomes.GROVE)
                || biome.is(OverworldBiomes.FROST_PEAKS)) {
            return FarmAnimals.pick(random, 70, 30) == 0 ? Breed.SCHNEE : Breed.STEINBOCK;
        }
        if (biome.is(Biomes.JAGGED_PEAKS) || biome.is(Biomes.STONY_PEAKS) || biome.is(OverworldBiomes.ROCK_GORGES)) {
            return FarmAnimals.pick(random, 55, 45) == 0 ? Breed.STEINBOCK : Breed.GAEMSE;
        }
        if (FarmAnimals.pasture(biome)) {
            return Breed.HAUS;
        }
        return FarmAnimals.pick(random, 60, 40) == 0 ? Breed.GAEMSE : Breed.HAUS;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
            @Nullable SpawnGroupData groupData) {
        this.setBreed(breedFor(level.getBiome(this.blockPosition()), level.getRandom()));
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
        this.setBreed(FarmAnimals.byName(Breed.values(), input.getStringOr("Breed", "steinbock"), Breed.STEINBOCK));
    }

    @Override
    public @Nullable FarmGoat getBreedOffspring(ServerLevel level, AgeableMob partner) {
        FarmGoat kid = ModEntities.FARM_GOAT.get().create(level, EntitySpawnReason.BREEDING);
        if (kid != null) {
            RandomSource random = level.getRandom();
            kid.getBrain().setMemory(MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, TIME_BETWEEN_LONG_JUMPS.sample(random));
            kid.getBrain().setMemory(MemoryModuleType.RAM_COOLDOWN_TICKS, TIME_BETWEEN_RAMS.sample(random));
            boolean screaming = (random.nextBoolean() ? this : partner) instanceof Goat goat && goat.isScreamingGoat()
                    || random.nextDouble() < 0.02;
            kid.setScreamingGoat(screaming);
            Breed other = partner instanceof FarmGoat goat ? goat.getBreed() : this.getBreed();
            kid.setBreed(FarmAnimals.inherit(this.random, this.getBreed(), other,
                    breedFor(level.getBiome(this.blockPosition()), this.random)));
        }
        return kid;
    }

    @Override
    protected Component getTypeName() {
        return FarmAnimals.typeName(this.getType(), this.getBreed());
    }
}
