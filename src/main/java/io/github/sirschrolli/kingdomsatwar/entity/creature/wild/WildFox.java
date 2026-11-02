package io.github.sirschrolli.kingdomsatwar.entity.creature.wild;

import io.github.sirschrolli.kingdomsatwar.entity.ReplacesVanilla;
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
import net.minecraft.world.entity.animal.fox.Fox;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Fuchs – ersetzt den Vanilla-Fuchs. Rotfuchs in Taiga und Wäldern (selten als Silberfuchs), Polarfuchs im Schnee,
 * Wüstenfuchs (Fennek) mit riesigen Ohren in Wüste und Tafelbergen. Schläft tagsüber, lauert, springt auf Beute,
 * trägt Dinge im Maul, pflückt Beeren und vertraut seinen Züchtern – alles wie bei Vanilla.
 */
public class WildFox extends Fox implements ReplacesVanilla {
    public enum Breed implements FarmAnimals.Breed { ROT, POLAR, WUESTE, SILBER }

    private static final EntityDataAccessor<Integer> DATA_BREED =
            SynchedEntityData.defineId(WildFox.class, EntityDataSerializers.INT);

    public WildFox(EntityType<? extends WildFox> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Fox.createAttributes();
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
        if (WildAnimals.snowy(biome)) {
            return FarmAnimals.pick(random, 85, 15) == 0 ? Breed.POLAR : Breed.ROT;
        }
        if (WildAnimals.desert(biome) || WildAnimals.badlands(biome)) {
            return Breed.WUESTE;
        }
        if (WildAnimals.taiga(biome)) {
            return FarmAnimals.pick(random, 90, 10) == 0 ? Breed.ROT : Breed.SILBER;
        }
        return FarmAnimals.pick(random, 96, 4) == 0 ? Breed.ROT : Breed.SILBER;
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
            @Nullable SpawnGroupData groupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, groupData);
        this.setBreed(breedFor(level.getBiome(this.blockPosition()), WildAnimals.herd(this.blockPosition(), 37)));
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
        this.setBreed(FarmAnimals.byName(Breed.values(), input.getStringOr("Breed", "rot"), Breed.ROT));
    }

    @Override
    public @Nullable WildFox getBreedOffspring(ServerLevel level, AgeableMob partner) {
        WildFox kit = ModEntities.FOX.get().create(level, EntitySpawnReason.BREEDING);
        if (kit != null) {
            Breed other = partner instanceof WildFox fox ? fox.getBreed() : this.getBreed();
            kit.setBreed(FarmAnimals.inherit(this.random, this.getBreed(), other, breedFor(level.getBiome(this.blockPosition()), this.random)));
        }
        return kit;
    }

    /** Wie der Vanilla-Fuchs: Beerensträucher bremsen nicht … */
    @Override
    public void makeStuckInBlock(BlockState state, Vec3 speedMultiplier) {
        if (!WildAnimals.ignoresBush(state)) {
            super.makeStuckInBlock(state, speedMultiplier);
        }
    }

    /** … und stechen nicht. */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        return !WildAnimals.isBushDamage(source) && super.hurtServer(level, source, damage);
    }

    @Override
    protected Component getTypeName() {
        return FarmAnimals.typeName(this.getType(), this.getBreed());
    }
}
