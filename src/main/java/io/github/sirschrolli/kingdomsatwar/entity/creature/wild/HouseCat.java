package io.github.sirschrolli.kingdomsatwar.entity.creature.wild;

import io.github.sirschrolli.kingdomsatwar.entity.creature.farm.FarmAnimals;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
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
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.feline.CatVariant;
import net.minecraft.world.entity.animal.feline.CatVariants;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Hauskatze – ersetzt die Vanilla-Katze. Acht Rassen: getigert, roter Kater, Siam, Kartäuser, Glückskatze, Perser,
 * Smoking und die schwarze Hexenkatze (beim Hexenbaum). Zähmen mit Fisch, Halsband, Sitzen, Schlafen beim Spieler und
 * Geschenke am Morgen wie bei Vanilla; Creeper und Phantome fürchten sie genauso.
 */
public class HouseCat extends Cat {
    public enum Breed implements FarmAnimals.Breed { GETIGERT, ROT, SIAM, KARTAEUSER, GLUECK, PERSER, SMOKING, HEXE }

    private static final EntityDataAccessor<Integer> DATA_BREED =
            SynchedEntityData.defineId(HouseCat.class, EntityDataSerializers.INT);

    public HouseCat(EntityType<? extends HouseCat> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Cat.createAttributes();
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

    /** Rasse nach der Vanilla-Fellvariante (die Vanilla beim Erscheinen wählt – im Hexenhaus etwa die schwarze). */
    public static Breed fromVanilla(Holder<CatVariant> variant) {
        if (variant.is(CatVariants.ALL_BLACK)) {
            return Breed.HEXE;
        }
        if (variant.is(CatVariants.BLACK)) {
            return Breed.SMOKING;
        }
        if (variant.is(CatVariants.RED)) {
            return Breed.ROT;
        }
        if (variant.is(CatVariants.SIAMESE) || variant.is(CatVariants.RAGDOLL)) {
            return Breed.SIAM;
        }
        if (variant.is(CatVariants.BRITISH_SHORTHAIR)) {
            return Breed.KARTAEUSER;
        }
        if (variant.is(CatVariants.CALICO)) {
            return Breed.GLUECK;
        }
        if (variant.is(CatVariants.PERSIAN) || variant.is(CatVariants.WHITE)) {
            return Breed.PERSER;
        }
        return Breed.GETIGERT;
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
            @Nullable SpawnGroupData groupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, groupData);
        // von selbst erscheinen Katzen nur in Dörfern und am Hexenbaum im Sumpf – dort ist es die schwarze Hexenkatze
        boolean witch = reason == EntitySpawnReason.NATURAL && WildAnimals.wetlands(level.getBiome(this.blockPosition()));
        this.setBreed(witch ? Breed.HEXE : fromVanilla(this.getVariant()));
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
        this.setBreed(FarmAnimals.byName(Breed.values(), input.getStringOr("Breed", "getigert"), Breed.GETIGERT));
    }

    @Override
    public @Nullable HouseCat getBreedOffspring(ServerLevel level, AgeableMob partner) {
        HouseCat kitten = ModEntities.CAT.get().create(level, EntitySpawnReason.BREEDING);
        if (kitten != null && partner instanceof Cat other) {
            Breed otherBreed = other instanceof HouseCat cat ? cat.getBreed() : this.getBreed();
            kitten.setBreed(this.random.nextBoolean() ? this.getBreed() : otherBreed);
            if (this.isTame()) {
                kitten.setOwnerReference(this.getOwnerReference());
                kitten.setTame(true, true);
                kitten.setComponent(DataComponents.CAT_COLLAR, DyeColor.getMixedColor(level, this.getCollarColor(), other.getCollarColor()));
            }
        }
        return kitten;
    }

    @Override
    protected Component getTypeName() {
        return FarmAnimals.typeName(this.getType(), this.getBreed());
    }
}
