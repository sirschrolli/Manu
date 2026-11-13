package io.github.sirschrolli.kingdomsatwar.entity.creature.farm;

import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.cow.AbstractCow;
import net.minecraft.world.entity.animal.cow.CowSoundVariant;
import net.minecraft.world.entity.animal.cow.CowSoundVariants;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Hausrind – ersetzt Kuh und Pilzkuh. Fünf Rassen: Fleckvieh und Braunvieh auf den Weiden, zotteliges Hochlandrind im
 * Norden, Steppenrind mit Leierhörnern im Süden und das Pilzrind auf den Pilzinseln. Gibt Milch; das Pilzrind füllt
 * außerdem Schüsseln mit Pilzsuppe.
 */
public class Cattle extends AbstractCow {
    public enum Breed implements FarmAnimals.Breed { FLECKVIEH, BRAUNVIEH, HOCHLAND, STEPPE, PILZ }

    private static final EntityDataAccessor<Integer> DATA_BREED =
            SynchedEntityData.defineId(Cattle.class, EntityDataSerializers.INT);

    public Cattle(EntityType<? extends Cattle> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractCow.createAttributes();
    }

    /** Wie Vanilla-Tiere auf Gras – das Pilzrind auch auf Myzel. */
    public static boolean checkCattleSpawnRules(EntityType<? extends Animal> type, LevelAccessor level, EntitySpawnReason reason,
            BlockPos pos, RandomSource random) {
        if (level.getBlockState(pos.below()).is(BlockTags.MOOSHROOMS_SPAWNABLE_ON) && level.getRawBrightness(pos, 0) > 8) {
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

    public static Breed breedFor(Holder<Biome> biome, RandomSource random) {
        if (biome.is(Biomes.MUSHROOM_FIELDS)) {
            return Breed.PILZ;
        }
        if (FarmAnimals.cold(biome)) {
            return FarmAnimals.pick(random, 75, 25) == 0 ? Breed.HOCHLAND : Breed.BRAUNVIEH;
        }
        if (FarmAnimals.warm(biome)) {
            return FarmAnimals.pick(random, 75, 25) == 0 ? Breed.STEPPE : Breed.FLECKVIEH;
        }
        if (FarmAnimals.mountains(biome) || biome.is(Biomes.MEADOW) || biome.is(Biomes.CHERRY_GROVE)) {
            return FarmAnimals.pick(random, 65, 35) == 0 ? Breed.BRAUNVIEH : Breed.FLECKVIEH;
        }
        return FarmAnimals.pick(random, 65, 35) == 0 ? Breed.FLECKVIEH : Breed.BRAUNVIEH;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
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
        this.setBreed(FarmAnimals.byName(Breed.values(), input.getStringOr("Breed", "fleckvieh"), Breed.FLECKVIEH));
    }

    @Override
    public @Nullable Cattle getBreedOffspring(ServerLevel level, AgeableMob partner) {
        Cattle calf = ModEntities.CATTLE.get().create(level, EntitySpawnReason.BREEDING);
        if (calf != null) {
            Breed other = partner instanceof Cattle cattle ? cattle.getBreed() : this.getBreed();
            calf.setBreed(FarmAnimals.inherit(this.random, this.getBreed(), other,
                    breedFor(level.getBiome(this.blockPosition()), this.random)));
        }
        return calf;
    }

    /** Pilzrinder füllen Schüsseln mit Pilzsuppe (wie die Pilzkuh). */
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (this.getBreed() == Breed.PILZ && stack.is(Items.BOWL) && !this.isBaby()) {
            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(Items.MUSHROOM_STEW), false));
            this.playSound(SoundEvents.MOOSHROOM_MILK, 1.0f, 1.0f);
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    /** Hochland- und Steppenrinder brummen tiefer. */
    @Override
    protected CowSoundVariant getSoundSet() {
        Breed breed = this.getBreed();
        return SoundEvents.COW_SOUNDS.get(breed == Breed.HOCHLAND || breed == Breed.STEPPE
                ? CowSoundVariants.SoundSet.MOODY : CowSoundVariants.SoundSet.CLASSIC);
    }

    @Override
    protected Component getTypeName() {
        return FarmAnimals.typeName(this.getType(), this.getBreed());
    }
}
