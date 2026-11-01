package io.github.sirschrolli.kingdomsatwar.entity.creature.farm;

import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.chicken.ChickenVariant;
import net.minecraft.world.entity.animal.chicken.ChickenVariants;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Huhn – ersetzt das Vanilla-Huhn. Weiße Hennen legen weiße, braune und schwarze Hennen braune und Sperberhennen
 * (kalte Länder) blaue Eier; der Hahn mit großem Kamm und Sichelfedern legt keine. Etwa jedes fünfte Küken wird ein
 * Hahn. Hühnerjockeys, Flattern und Futter wie bei Vanilla.
 */
public class FarmChicken extends Chicken {
    public enum Breed implements FarmAnimals.Breed { WHITE, BROWN, BLACK, BARRED, ROOSTER }

    private static final EntityDataAccessor<Integer> DATA_BREED =
            SynchedEntityData.defineId(FarmChicken.class, EntityDataSerializers.INT);

    public FarmChicken(EntityType<? extends FarmChicken> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Chicken.createAttributes();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_BREED, 0);
    }

    public Breed getBreed() {
        return FarmAnimals.byId(Breed.values(), this.entityData.get(DATA_BREED));
    }

    public boolean isRooster() {
        return this.getBreed() == Breed.ROOSTER;
    }

    /** Setzt die Rasse und passend dazu die Vanilla-Variante, nach der sich die Farbe der gelegten Eier richtet. */
    public void setBreed(Breed breed) {
        this.entityData.set(DATA_BREED, breed.ordinal());
        ResourceKey<ChickenVariant> eggs = switch (breed) {
            case BROWN, BLACK -> ChickenVariants.WARM;
            case BARRED -> ChickenVariants.COLD;
            default -> ChickenVariants.TEMPERATE;
        };
        this.registryAccess().lookupOrThrow(Registries.CHICKEN_VARIANT).get(eggs).ifPresent(this::setVariant);
    }

    /** Rasse einer Henne aus der Vanilla-Variante (Temperatur der Gegend). */
    public static Breed henFor(ResourceKey<ChickenVariant> variant, RandomSource random) {
        if (ChickenVariants.COLD.equals(variant)) {
            return Breed.BARRED;
        }
        if (ChickenVariants.WARM.equals(variant)) {
            return random.nextInt(3) == 0 ? Breed.BLACK : Breed.BROWN;
        }
        return random.nextInt(3) == 0 ? Breed.BROWN : Breed.WHITE;
    }

    public static Breed breedFor(Holder<Biome> biome, RandomSource random) {
        if (random.nextInt(5) == 0) {
            return Breed.ROOSTER;
        }
        if (FarmAnimals.cold(biome)) {
            return henFor(ChickenVariants.COLD, random);
        }
        return henFor(FarmAnimals.warm(biome) ? ChickenVariants.WARM : ChickenVariants.TEMPERATE, random);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
            @Nullable SpawnGroupData groupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, groupData);
        this.setBreed(breedFor(level.getBiome(this.blockPosition()), level.getRandom()));
        return data;
    }

    /** Hähne legen keine Eier. */
    @Override
    public void aiStep() {
        if (this.isRooster()) {
            this.eggTime = 6000;
        }
        super.aiStep();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString("Breed", this.getBreed().id());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        // ohne gespeicherte Rasse (umgewandeltes Vanilla-Huhn) entscheidet dessen Variante
        Breed fallback = henFor(this.getVariant().unwrapKey().orElse(ChickenVariants.TEMPERATE), this.random);
        this.setBreed(FarmAnimals.byName(Breed.values(), input.getStringOr("Breed", fallback.id()), fallback));
    }

    @Override
    public @Nullable FarmChicken getBreedOffspring(ServerLevel level, AgeableMob partner) {
        FarmChicken chick = ModEntities.FARM_CHICKEN.get().create(level, EntitySpawnReason.BREEDING);
        if (chick != null) {
            if (this.random.nextInt(5) == 0) {
                chick.setBreed(Breed.ROOSTER);
            } else {
                // die Henne gibt die Rasse weiter (ist kein Elternteil eine Henne, entscheidet die Gegend)
                Breed hen = !this.isRooster() ? this.getBreed()
                        : partner instanceof FarmChicken other && !other.isRooster() ? other.getBreed()
                        : henFor(this.getVariant().unwrapKey().orElse(ChickenVariants.TEMPERATE), this.random);
                if (partner instanceof FarmChicken other && !other.isRooster() && !this.isRooster() && this.random.nextBoolean()) {
                    hen = other.getBreed();
                }
                chick.setBreed(hen);
            }
        }
        return chick;
    }

    @Override
    protected Component getTypeName() {
        return FarmAnimals.typeName(this.getType(), this.getBreed());
    }
}
