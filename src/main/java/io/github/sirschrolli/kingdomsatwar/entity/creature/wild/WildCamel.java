package io.github.sirschrolli.kingdomsatwar.entity.creature.wild;

import io.github.sirschrolli.kingdomsatwar.entity.creature.farm.FarmAnimals;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Kamel – ersetzt das Vanilla-Kamel. Das Dromedar mit einem Höcker in der Wüste, das zottelige Trampeltier mit zwei
 * Höckern in Tafelbergen und auf den Savannen-Hochebenen. Sattel, zwei Reiter, Sprint, Sitzen und Kakteen wie bei
 * Vanilla.
 */
public class WildCamel extends Camel {
    public enum Breed implements FarmAnimals.Breed { DROMEDAR, TRAMPELTIER }

    private static final EntityDataAccessor<Integer> DATA_BREED =
            SynchedEntityData.defineId(WildCamel.class, EntityDataSerializers.INT);

    private final Courtship<WildCamel> courtship = new Courtship<>(WildCamel.class, 70);

    public WildCamel(EntityType<? extends WildCamel> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Camel.createAttributes();
    }

    /** Auf Sand wie bei Vanilla, das Trampeltier auch auf dem Gras der Hochebenen. */
    public static boolean checkWildCamelSpawnRules(EntityType<? extends Camel> type, LevelAccessor level, EntitySpawnReason reason,
            BlockPos pos, RandomSource random) {
        if (level.getBlockState(pos.below()).is(BlockTags.CAMELS_SPAWNABLE_ON) && level.getRawBrightness(pos, 0) > 8) {
            return true;
        }
        return Animal.checkAnimalSpawnRules(type, level, reason, pos, random);
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

    public static Breed breedFor(Holder<Biome> biome) {
        return WildAnimals.badlands(biome) || WildAnimals.savanna(biome) || WildAnimals.snowy(biome) ? Breed.TRAMPELTIER : Breed.DROMEDAR;
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
            @Nullable SpawnGroupData groupData) {
        this.setBreed(breedFor(level.getBiome(this.blockPosition())));
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
        this.setBreed(FarmAnimals.byName(Breed.values(), input.getStringOr("Breed", "dromedar"), Breed.DROMEDAR));
    }

    @Override
    public @Nullable WildCamel getBreedOffspring(ServerLevel level, AgeableMob partner) {
        WildCamel calf = ModEntities.CAMEL.get().create(level, EntitySpawnReason.BREEDING);
        if (calf != null) {
            Breed other = partner instanceof WildCamel camel ? camel.getBreed() : this.getBreed();
            calf.setBreed(this.random.nextBoolean() ? this.getBreed() : other);
        }
        return calf;
    }

    /**
     * Das Vanilla-Gehirn sucht Partner nur unter Vanilla-Kamelen – die Werbung übernimmt {@link Courtship}. Ein
     * verliebtes Kamel, das gerade sitzt, steht dafür auf.
     */
    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (this.isInLove() && this.isCamelSitting() && !this.isVehicle()) {
            this.standUp();
        }
        this.courtship.tick(level, this, camel -> !camel.isCamelSitting() && !camel.isPanicking() && !camel.isVehicle());
    }

    @Override
    protected Component getTypeName() {
        return FarmAnimals.typeName(this.getType(), this.getBreed());
    }
}
