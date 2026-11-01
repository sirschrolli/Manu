package io.github.sirschrolli.kingdomsatwar.entity.creature.wild;

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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.animal.bee.Bee;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Biene – ersetzt die Vanilla-Biene. Honigbiene und (in kühlen Gegenden) die pelzige Hummel. Bestäuben, Nester und
 * Bienenstöcke, Honig, Zorn und Stich wie bei Vanilla.
 */
public class WildBee extends Bee {
    public enum Breed implements FarmAnimals.Breed { HONIG, HUMMEL }

    private static final EntityDataAccessor<Integer> DATA_BREED =
            SynchedEntityData.defineId(WildBee.class, EntityDataSerializers.INT);

    public WildBee(EntityType<? extends WildBee> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Bee.createAttributes();
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
        if (WildAnimals.snowy(biome) || WildAnimals.taiga(biome) || biome.is(Biomes.MEADOW) || biome.is(Biomes.WINDSWEPT_HILLS)
                || biome.is(Biomes.WINDSWEPT_FOREST)) {
            return FarmAnimals.pick(random, 30, 70) == 0 ? Breed.HONIG : Breed.HUMMEL;
        }
        return FarmAnimals.pick(random, 75, 25) == 0 ? Breed.HONIG : Breed.HUMMEL;
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
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
        this.setBreed(FarmAnimals.byName(Breed.values(), input.getStringOr("Breed", "honig"), Breed.HONIG));
    }

    @Override
    public @Nullable WildBee getBreedOffspring(ServerLevel level, AgeableMob partner) {
        WildBee larva = ModEntities.BEE.get().create(level, EntitySpawnReason.BREEDING);
        if (larva != null) {
            Breed other = partner instanceof WildBee bee ? bee.getBreed() : this.getBreed();
            larva.setBreed(this.random.nextBoolean() ? this.getBreed() : other);
        }
        return larva;
    }

    /** Wie die Vanilla-Biene: Beerensträucher bremsen nicht und stechen nicht. */
    @Override
    public void makeStuckInBlock(BlockState state, Vec3 speedMultiplier) {
        if (!WildAnimals.ignoresBush(state)) {
            super.makeStuckInBlock(state, speedMultiplier);
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        return !WildAnimals.isBushDamage(source) && super.hurtServer(level, source, damage);
    }

    @Override
    protected Component getTypeName() {
        return FarmAnimals.typeName(this.getType(), this.getBreed());
    }
}
