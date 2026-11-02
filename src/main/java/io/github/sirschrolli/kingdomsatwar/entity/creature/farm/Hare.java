package io.github.sirschrolli.kingdomsatwar.entity.creature.farm;

import io.github.sirschrolli.kingdomsatwar.entity.ReplacesVanilla;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Hase und Kaninchen – ersetzen das Vanilla-Kaninchen. Feldhase auf Wiesen und in Wäldern, Schneehase im Schnee,
 * Wüstenhase mit Riesenlöffeln in Wüste und Savanne, Wildkaninchen in Wäldern, Scheckenkaninchen auf Weiden – und der
 * berüchtigte Mordshase (nur per Befehl, wie der Killerhase). Hoppeln, Karottenklau und Zucht wie bei Vanilla.
 */
public class Hare extends Rabbit implements ReplacesVanilla {
    public enum Breed implements FarmAnimals.Breed {
        FELDHASE(Rabbit.Variant.BROWN), SCHNEEHASE(Rabbit.Variant.WHITE), WUESTENHASE(Rabbit.Variant.GOLD),
        KANINCHEN(Rabbit.Variant.SALT), SCHECKE(Rabbit.Variant.WHITE_SPLOTCHED), MORDSHASE(Rabbit.Variant.EVIL);

        private final Rabbit.Variant variant;

        Breed(Rabbit.Variant variant) {
            this.variant = variant;
        }

        public Rabbit.Variant variant() {
            return this.variant;
        }
    }

    private static final EntityDataAccessor<Integer> DATA_BREED =
            SynchedEntityData.defineId(Hare.class, EntityDataSerializers.INT);

    public Hare(EntityType<? extends Hare> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Rabbit.createAttributes();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_BREED, 0);
    }

    public Breed getBreed() {
        return FarmAnimals.byId(Breed.values(), this.entityData.get(DATA_BREED));
    }

    /** Setzt die Rasse und die passende Vanilla-Variante (der Mordshase greift an wie der Killerhase). */
    public void setBreed(Breed breed) {
        this.entityData.set(DATA_BREED, breed.ordinal());
        if (this.getVariant() != breed.variant()) {
            this.applyImplicitComponent(DataComponents.RABBIT_VARIANT, breed.variant());
        }
    }

    /** Rasse aus der Vanilla-Variante, die das Kaninchen nach seinem Biom bekommen hat. */
    public static Breed fromVariant(Rabbit.Variant variant, Holder<Biome> biome, RandomSource random) {
        return switch (variant) {
            case WHITE -> Breed.SCHNEEHASE;
            case WHITE_SPLOTCHED -> biome.is(BiomeTags.SPAWNS_WHITE_RABBITS) ? Breed.SCHNEEHASE : Breed.SCHECKE;
            case GOLD -> Breed.WUESTENHASE;
            case SALT -> FarmAnimals.pasture(biome) ? Breed.FELDHASE : Breed.KANINCHEN;
            case BLACK -> FarmAnimals.pasture(biome) ? Breed.SCHECKE : Breed.KANINCHEN;
            case EVIL -> Breed.MORDSHASE;
            default -> FarmAnimals.pasture(biome) && random.nextInt(6) == 0 ? Breed.SCHECKE
                    : FarmAnimals.forest(biome) && random.nextInt(3) == 0 ? Breed.KANINCHEN : Breed.FELDHASE;
        };
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
            @Nullable SpawnGroupData groupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, groupData);
        if (FarmAnimals.warm(level.getBiome(this.blockPosition())) && this.getVariant() != Rabbit.Variant.EVIL) {
            this.setBreed(Breed.WUESTENHASE);
        } else {
            this.setBreed(fromVariant(this.getVariant(), level.getBiome(this.blockPosition()), level.getRandom()));
        }
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
        // ohne gespeicherte Rasse (umgewandeltes Vanilla-Kaninchen) entscheidet dessen Variante
        Breed fallback = switch (this.getVariant()) {
            case WHITE, WHITE_SPLOTCHED -> Breed.SCHNEEHASE;
            case GOLD -> Breed.WUESTENHASE;
            case SALT, BLACK -> Breed.KANINCHEN;
            case EVIL -> Breed.MORDSHASE;
            default -> Breed.FELDHASE;
        };
        this.setBreed(FarmAnimals.byName(Breed.values(), input.getStringOr("Breed", fallback.id()), fallback));
    }

    @Override
    public @Nullable Hare getBreedOffspring(ServerLevel level, AgeableMob partner) {
        Hare leveret = ModEntities.HARE.get().create(level, EntitySpawnReason.BREEDING);
        if (leveret != null) {
            Breed other = partner instanceof Hare hare ? hare.getBreed() : this.getBreed();
            Breed local = fromVariant(Rabbit.Variant.BROWN, level.getBiome(this.blockPosition()), this.random);
            Breed breed = FarmAnimals.inherit(this.random, this.getBreed(), other, local);
            leveret.setBreed(breed == Breed.MORDSHASE && this.random.nextInt(4) != 0 ? Breed.SCHNEEHASE : breed);
        }
        return leveret;
    }

    @Override
    protected Component getTypeName() {
        return FarmAnimals.typeName(this.getType(), this.getBreed());
    }
}
