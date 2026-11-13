package io.github.sirschrolli.kingdomsatwar.entity.creature.wild;

import io.github.sirschrolli.kingdomsatwar.entity.ReplacesVanilla;
import io.github.sirschrolli.kingdomsatwar.entity.creature.farm.FarmAnimals;
import io.github.sirschrolli.kingdomsatwar.entity.creature.farm.FarmSheep;
import io.github.sirschrolli.kingdomsatwar.entity.creature.farm.Hare;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
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
import net.minecraft.world.entity.ai.goal.target.NonTameRandomTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.animal.wolf.WolfSoundVariants;
import net.minecraft.world.entity.animal.wolf.WolfVariant;
import net.minecraft.world.entity.animal.wolf.WolfVariants;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Wolf – ersetzt den Vanilla-Wolf. Sechs Arten: Grauwolf in den Wäldern, Polarwolf im Schnee, Schwarz- und Timberwolf
 * in den alten Taigas, Steppenwolf in Savanne und Tafelbergen, Rotwolf im Dschungel. Zähmen, Sitzen, Halsband,
 * Wolfspanzer, Rudel und Jagd wie bei Vanilla – gejagt werden auch die eigenen Schafe, Hasen und Füchse.
 */
public class WildWolf extends Wolf implements ReplacesVanilla {
    public enum Breed implements FarmAnimals.Breed { GRAU, POLAR, SCHWARZ, TIMBER, STEPPE, ROT }

    private static final EntityDataAccessor<Integer> DATA_BREED =
            SynchedEntityData.defineId(WildWolf.class, EntityDataSerializers.INT);

    public WildWolf(EntityType<? extends WildWolf> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Wolf.createAttributes();
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        // Vanilla jagt nur Vanilla-Schafe, -Kaninchen und -Füchse
        this.targetSelector.addGoal(5, new NonTameRandomTargetGoal<>(this, Animal.class, false,
                (target, level) -> target instanceof FarmSheep || target instanceof Hare || target instanceof WildFox));
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
            return FarmAnimals.pick(random, 80, 20) == 0 ? Breed.POLAR : Breed.GRAU;
        }
        if (biome.is(Biomes.OLD_GROWTH_PINE_TAIGA)) {
            return FarmAnimals.pick(random, 60, 40) == 0 ? Breed.SCHWARZ : Breed.TIMBER;
        }
        if (biome.is(Biomes.OLD_GROWTH_SPRUCE_TAIGA)) {
            return FarmAnimals.pick(random, 70, 30) == 0 ? Breed.TIMBER : Breed.SCHWARZ;
        }
        if (WildAnimals.taiga(biome)) {
            return FarmAnimals.pick(random, 60, 40) == 0 ? Breed.GRAU : Breed.TIMBER;
        }
        if (WildAnimals.savanna(biome) || WildAnimals.badlands(biome) || WildAnimals.desert(biome)) {
            return Breed.STEPPE;
        }
        if (WildAnimals.jungle(biome) || WildAnimals.wetlands(biome)) {
            return Breed.ROT;
        }
        return Breed.GRAU;
    }

    /** Rasse eines umgewandelten Vanilla-Wolfs nach seiner Fellvariante. */
    public static Breed fromVanilla(Holder<WolfVariant> variant) {
        if (variant.is(WolfVariants.SNOWY) || variant.is(WolfVariants.ASHEN)) {
            return Breed.POLAR;
        }
        if (variant.is(WolfVariants.BLACK)) {
            return Breed.SCHWARZ;
        }
        if (variant.is(WolfVariants.CHESTNUT)) {
            return Breed.TIMBER;
        }
        if (variant.is(WolfVariants.SPOTTED) || variant.is(WolfVariants.STRIPED)) {
            return Breed.STEPPE;
        }
        if (variant.is(WolfVariants.RUSTY)) {
            return Breed.ROT;
        }
        return Breed.GRAU;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
            @Nullable SpawnGroupData groupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, groupData);
        this.setBreed(breedFor(level.getBiome(this.blockPosition()), WildAnimals.herd(this.blockPosition(), 31)));
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
        this.setBreed(FarmAnimals.byName(Breed.values(), input.getStringOr("Breed", "grau"), Breed.GRAU));
    }

    @Override
    public @Nullable WildWolf getBreedOffspring(ServerLevel level, AgeableMob partner) {
        WildWolf pup = ModEntities.WOLF.get().create(level, EntitySpawnReason.BREEDING);
        if (pup != null && partner instanceof Wolf other) {
            Breed otherBreed = other instanceof WildWolf wolf ? wolf.getBreed() : this.getBreed();
            pup.setBreed(FarmAnimals.inherit(this.random, this.getBreed(), otherBreed,
                    breedFor(level.getBiome(this.blockPosition()), this.random)));
            if (this.isTame()) {
                pup.setOwnerReference(this.getOwnerReference());
                pup.setTame(true, true);
                pup.setComponent(DataComponents.WOLF_COLLAR, DyeColor.getMixedColor(level, this.getCollarColor(), other.getCollarColor()));
            }
            pup.setComponent(DataComponents.WOLF_SOUND_VARIANT, WolfSoundVariants.pickRandomSoundVariant(this.registryAccess(), this.random));
        }
        return pup;
    }

    @Override
    protected Component getTypeName() {
        return FarmAnimals.typeName(this.getType(), this.getBreed());
    }
}
