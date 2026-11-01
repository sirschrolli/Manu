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
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Gürteltier – ersetzt das Vanilla-Gürteltier. Neunbinden-Gürteltier und gelbbraunes Kugelgürteltier in Savanne und
 * Tafelbergen. Rollt sich bei Gefahr ein, wirft Hornplatten ab (Bürste!) und mag Spinnenaugen – wie bei Vanilla.
 */
public class WildArmadillo extends Armadillo {
    public enum Breed implements FarmAnimals.Breed { NEUNBINDEN, KUGEL }

    private static final EntityDataAccessor<Integer> DATA_BREED =
            SynchedEntityData.defineId(WildArmadillo.class, EntityDataSerializers.INT);

    private final Courtship<WildArmadillo> courtship = new Courtship<>(WildArmadillo.class, 70);

    public WildArmadillo(EntityType<? extends WildArmadillo> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Armadillo.createAttributes();
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
        if (WildAnimals.savanna(biome)) {
            return FarmAnimals.pick(random, 40, 60) == 0 ? Breed.NEUNBINDEN : Breed.KUGEL;
        }
        return FarmAnimals.pick(random, 75, 25) == 0 ? Breed.NEUNBINDEN : Breed.KUGEL;
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
            @Nullable SpawnGroupData groupData) {
        this.setBreed(breedFor(level.getBiome(this.blockPosition()), WildAnimals.herd(this.blockPosition(), 47)));
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
        this.setBreed(FarmAnimals.byName(Breed.values(), input.getStringOr("Breed", "neunbinden"), Breed.NEUNBINDEN));
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        WildArmadillo pup = ModEntities.ARMADILLO.get().create(level, EntitySpawnReason.BREEDING);
        if (pup != null) {
            Breed other = partner instanceof WildArmadillo armadillo ? armadillo.getBreed() : this.getBreed();
            pup.setBreed(this.random.nextBoolean() ? this.getBreed() : other);
        }
        return pup;
    }

    /** Das Vanilla-Gehirn sucht Partner nur unter Vanilla-Gürteltieren – die Werbung übernimmt {@link Courtship}. */
    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        this.courtship.tick(level, this, armadillo -> !armadillo.shouldHideInShell() && !armadillo.isPanicking());
    }

    @Override
    protected Component getTypeName() {
        return FarmAnimals.typeName(this.getType(), this.getBreed());
    }
}
