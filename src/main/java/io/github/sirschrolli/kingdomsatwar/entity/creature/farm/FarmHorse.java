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
import net.minecraft.util.Util;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.Donkey;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.animal.equine.Variant;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Pferd – ersetzt das Vanilla-Pferd. Neun Rassen; eine Herde besteht meist aus einer Rasse. Das schwere Kaltblut ist
 * größer, kräftiger und langsamer, das Pony klein und gemächlich. Zähmen, Sattel, Rossharnisch, Steigen und Springen
 * wie bei Vanilla; mit einem Esel gibt es Maultiere.
 */
public class FarmHorse extends Horse implements ReplacesVanilla {
    public enum Breed implements FarmAnimals.Breed {
        FUCHS(1.0f), BRAUNER(1.0f), FRIESE(1.04f), SCHIMMEL(1.0f), SCHECKE(1.0f), HAFLINGER(0.94f), KALTBLUT(1.12f),
        FALBE(0.98f), PONY(0.8f);

        private final float scale;

        Breed(float scale) {
            this.scale = scale;
        }

        public float scale() {
            return this.scale;
        }
    }

    private static final EntityDataAccessor<Integer> DATA_BREED =
            SynchedEntityData.defineId(FarmHorse.class, EntityDataSerializers.INT);

    public FarmHorse(EntityType<? extends FarmHorse> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractHorse.createBaseHorseAttributes();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_BREED, 0);
    }

    public Breed getBreed() {
        return FarmAnimals.byId(Breed.values(), this.entityData.get(DATA_BREED));
    }

    /** Setzt die Rasse samt Körpergröße (Größe wirkt auf Hitbox, Reiter und Modell). */
    public void setBreed(Breed breed) {
        this.entityData.set(DATA_BREED, breed.ordinal());
        AttributeInstance scale = this.getAttribute(Attributes.SCALE);
        if (scale != null) {
            scale.setBaseValue(breed.scale());
        }
    }

    /** Kaltblüter sind kräftiger, aber langsamer; Ponys schwächer und gemächlicher. */
    public void applyBreedBuild() {
        double health = 0.0;
        double speed = 1.0;
        double jump = 1.0;
        switch (this.getBreed()) {
            case KALTBLUT -> {
                health = 6.0;
                speed = 0.84;
                jump = 0.85;
            }
            case PONY -> {
                health = -3.0;
                speed = 0.9;
                jump = 0.8;
            }
            case FRIESE -> health = 2.0;
            case FALBE -> speed = 1.05;
            default -> { }
        }
        this.scaleBase(Attributes.MAX_HEALTH, 1.0, health);
        this.scaleBase(Attributes.MOVEMENT_SPEED, speed, 0.0);
        this.scaleBase(Attributes.JUMP_STRENGTH, jump, 0.0);
        this.setHealth(this.getMaxHealth());
    }

    private void scaleBase(Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute, double factor, double add) {
        AttributeInstance instance = this.getAttribute(attribute);
        if (instance != null) {
            instance.setBaseValue(Math.max(1.0e-3, instance.getBaseValue() * factor + add));
        }
    }

    public static Breed breedFor(Holder<Biome> biome, RandomSource random) {
        if (FarmAnimals.warm(biome)) {
            return Breed.values()[FarmAnimals.pick(random, 20, 10, 0, 5, 25, 0, 0, 40, 0)];
        }
        if (biome.is(Biomes.MEADOW) || biome.is(Biomes.CHERRY_GROVE) || FarmAnimals.mountains(biome)) {
            return Breed.values()[FarmAnimals.pick(random, 5, 10, 0, 10, 5, 40, 0, 0, 30)];
        }
        if (FarmAnimals.cold(biome)) {
            return Breed.values()[FarmAnimals.pick(random, 5, 15, 15, 15, 0, 0, 25, 0, 25)];
        }
        return Breed.values()[FarmAnimals.pick(random, 20, 22, 6, 14, 12, 8, 12, 2, 4)];
    }

    /** Herde: Pferde, die zusammen erscheinen, sind meist von derselben Rasse. */
    static final class Herd extends Horse.HorseGroupData {
        final Breed breed;

        Herd(Variant variant, Breed breed) {
            super(variant);
            this.breed = breed;
        }
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
            @Nullable SpawnGroupData groupData) {
        RandomSource random = level.getRandom();
        Holder<Biome> biome = level.getBiome(this.blockPosition());
        Breed breed;
        if (groupData instanceof Herd herd) {
            breed = random.nextInt(5) == 0 ? breedFor(biome, random) : herd.breed;
        } else {
            breed = breedFor(biome, random);
            groupData = new Herd(Util.getRandom(Variant.values(), random), breed);
        }
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, groupData);
        this.setBreed(breed);
        this.applyBreedBuild();
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
        this.setBreed(FarmAnimals.byName(Breed.values(), input.getStringOr("Breed", "fuchs"), Breed.FUCHS));
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        if (partner instanceof Donkey) {
            FarmMule mule = ModEntities.FARM_MULE.get().create(level, EntitySpawnReason.BREEDING);
            if (mule != null) {
                this.setOffspringAttributes(partner, mule);
                mule.setBreed(this.getBreed() == Breed.FRIESE || this.random.nextInt(4) == 0
                        ? FarmMule.Breed.RAPPE : FarmMule.Breed.BRAUN);
            }
            return mule;
        }
        FarmHorse foal = ModEntities.FARM_HORSE.get().create(level, EntitySpawnReason.BREEDING);
        if (foal != null) {
            this.setOffspringAttributes(partner, foal);
            Breed other = partner instanceof FarmHorse horse ? horse.getBreed() : this.getBreed();
            foal.setBreed(FarmAnimals.inherit(this.random, this.getBreed(), other,
                    breedFor(level.getBiome(this.blockPosition()), this.random)));
        }
        return foal;
    }

    @Override
    protected Component getTypeName() {
        return FarmAnimals.typeName(this.getType(), this.getBreed());
    }
}
