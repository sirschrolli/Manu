package io.github.sirschrolli.kingdomsatwar.entity.creature.farm;

import io.github.sirschrolli.kingdomsatwar.entity.ReplacesVanilla;
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
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Hausschwein – ersetzt das Schwein. Rassen: rosiges Landschwein, Sattelschwein mit weißem Gürtel, buntes Schwein mit
 * schwarzen Platten (Wälder, warme Länder) und das krause Wollschwein (kalte Länder). Mit Sattel und Karottenrute
 * reitbar wie bei Vanilla.
 */
public class FarmPig extends Pig implements ReplacesVanilla {
    public enum Breed implements FarmAnimals.Breed { LANDSCHWEIN, SATTELSCHWEIN, BUNT, WOLLSCHWEIN }

    private static final EntityDataAccessor<Integer> DATA_BREED =
            SynchedEntityData.defineId(FarmPig.class, EntityDataSerializers.INT);

    public FarmPig(EntityType<? extends FarmPig> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Pig.createAttributes();
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
        if (FarmAnimals.cold(biome)) {
            return FarmAnimals.pick(random, 70, 30) == 0 ? Breed.WOLLSCHWEIN : Breed.SATTELSCHWEIN;
        }
        if (FarmAnimals.warm(biome)) {
            return FarmAnimals.pick(random, 60, 40) == 0 ? Breed.BUNT : Breed.LANDSCHWEIN;
        }
        if (FarmAnimals.forest(biome)) {
            return Breed.values()[FarmAnimals.pick(random, 25, 25, 50, 0)];
        }
        return Breed.values()[FarmAnimals.pick(random, 50, 35, 15, 0)];
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
        this.setBreed(FarmAnimals.byName(Breed.values(), input.getStringOr("Breed", "landschwein"), Breed.LANDSCHWEIN));
    }

    @Override
    public @Nullable FarmPig getBreedOffspring(ServerLevel level, AgeableMob partner) {
        FarmPig piglet = ModEntities.FARM_PIG.get().create(level, EntitySpawnReason.BREEDING);
        if (piglet != null) {
            Breed other = partner instanceof FarmPig pig ? pig.getBreed() : this.getBreed();
            piglet.setBreed(FarmAnimals.inherit(this.random, this.getBreed(), other,
                    breedFor(level.getBiome(this.blockPosition()), this.random)));
        }
        return piglet;
    }

    @Override
    protected Component getTypeName() {
        return FarmAnimals.typeName(this.getType(), this.getBreed());
    }
}
