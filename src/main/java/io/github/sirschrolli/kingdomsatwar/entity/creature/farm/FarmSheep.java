package io.github.sirschrolli.kingdomsatwar.entity.creature.farm;

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
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Schaf – ersetzt das Vanilla-Schaf. Rassen: Merino mit Wollschopf, Heidschnucke mit Schneckenhörnern und grauer Wolle
 * (Heiden, Nadelwälder, Berge), Schwarzkopfschaf. Scheren, Färben, Grasen und Nachwachsen der Wolle wie bei Vanilla.
 */
public class FarmSheep extends Sheep {
    public enum Breed implements FarmAnimals.Breed { MERINO, HEIDSCHNUCKE, SCHWARZKOPF }

    private static final EntityDataAccessor<Integer> DATA_BREED =
            SynchedEntityData.defineId(FarmSheep.class, EntityDataSerializers.INT);

    public FarmSheep(EntityType<? extends FarmSheep> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Sheep.createAttributes();
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
        if (FarmAnimals.cold(biome) || FarmAnimals.mountains(biome)) {
            return FarmAnimals.pick(random, 65, 35) == 0 ? Breed.HEIDSCHNUCKE : Breed.SCHWARZKOPF;
        }
        if (FarmAnimals.warm(biome) || biome.is(Biomes.MEADOW) || biome.is(Biomes.CHERRY_GROVE)) {
            return FarmAnimals.pick(random, 70, 30) == 0 ? Breed.MERINO : Breed.SCHWARZKOPF;
        }
        return Breed.values()[FarmAnimals.pick(random, 45, 15, 40)];
    }

    /** Heidschnucken tragen von Natur aus silbergraue, seltener dunkelgraue, braune oder schwarze Wolle. */
    private static DyeColor naturalColor(Breed breed, DyeColor rolled, RandomSource random) {
        if (breed != Breed.HEIDSCHNUCKE || rolled != DyeColor.WHITE) {
            return rolled;
        }
        return switch (FarmAnimals.pick(random, 55, 25, 12, 8)) {
            case 0 -> DyeColor.LIGHT_GRAY;
            case 1 -> DyeColor.GRAY;
            case 2 -> DyeColor.BROWN;
            default -> DyeColor.BLACK;
        };
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
            @Nullable SpawnGroupData groupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, groupData);
        this.setBreed(breedFor(level.getBiome(this.blockPosition()), level.getRandom()));
        this.setColor(naturalColor(this.getBreed(), this.getColor(), level.getRandom()));
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
        this.setBreed(FarmAnimals.byName(Breed.values(), input.getStringOr("Breed", "merino"), Breed.MERINO));
    }

    @Override
    public @Nullable FarmSheep getBreedOffspring(ServerLevel level, AgeableMob partner) {
        FarmSheep lamb = ModEntities.FARM_SHEEP.get().create(level, EntitySpawnReason.BREEDING);
        if (lamb != null) {
            Sheep other = (Sheep) partner;
            Breed otherBreed = partner instanceof FarmSheep sheep ? sheep.getBreed() : this.getBreed();
            lamb.setBreed(FarmAnimals.inherit(this.random, this.getBreed(), otherBreed,
                    breedFor(level.getBiome(this.blockPosition()), this.random)));
            lamb.setColor(DyeColor.getMixedColor(level, this.getColor(), other.getColor()));
        }
        return lamb;
    }

    @Override
    protected Component getTypeName() {
        return FarmAnimals.typeName(this.getType(), this.getBreed());
    }
}
