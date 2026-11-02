package io.github.sirschrolli.kingdomsatwar.entity.creature.rare;

import io.github.sirschrolli.kingdomsatwar.entity.creature.farm.FarmAnimals;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Monsters;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
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
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Wolkenqualle (statt glücklicher Ghast): eine freundliche Haufenwolke mit großen Augen und Dunstfäden, die aus einem
 * eingeweichten Ghastling-Block als Wölkchen schlüpft und heranwächst. Sie tut, was der glückliche Ghast tut: folgt
 * dem, der Schneebälle hält, trägt mit Geschirr bis zu vier Reiter und schwebt mit ihnen über das Land.
 *
 * <p>Drei Gestalten: Schönwetterwolke (weiß mit goldenem Sonnensaum), Abendwolke (rosig mit fliederfarbenen Schatten,
 * eher in warmen Ländern) und Regenwölkchen (blaugrau, mit Tropfen an den Fäden; eher in feuchten und kalten Ländern).
 */
public class CloudJelly extends HappyGhast implements Monsters.Varied {
    private static final EntityDataAccessor<Integer> DATA_LOOK = SynchedEntityData.defineId(CloudJelly.class, EntityDataSerializers.INT);

    public enum Look implements FarmAnimals.Breed {
        SCHOENWETTER, ABENDROT, REGENWOLKE
    }

    public CloudJelly(EntityType<? extends CloudJelly> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_LOOK, 0);
    }

    public Look getLook() {
        return FarmAnimals.byId(Look.values(), this.entityData.get(DATA_LOOK));
    }

    public void setLook(Look look) {
        this.entityData.set(DATA_LOOK, look.ordinal());
    }

    @Override
    public void chooseLook(ServerLevelAccessor level, BlockPos pos) {
        Holder<Biome> biome = level.getBiome(pos);
        int[] weights = Monsters.dry(biome) ? new int[] {45, 45, 10}
                : Monsters.wet(biome) || FarmAnimals.cold(biome) ? new int[] {40, 15, 45} : new int[] {55, 25, 20};
        this.setLook(Look.values()[FarmAnimals.pick(level.getRandom(), weights)]);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
            @Nullable SpawnGroupData groupData) {
        this.chooseLook(level, this.blockPosition());
        return super.finalizeSpawn(level, difficulty, reason, groupData);
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        CloudJelly child = ModEntities.HAPPY_GHAST.get().create(level, EntitySpawnReason.BREEDING);
        if (child != null) {
            child.setLook(this.getLook());
        }
        return child;
    }

    @Override
    public void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString("Look", this.getLook().id());
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setLook(FarmAnimals.byName(Look.values(), input.getStringOr("Look", "schoenwetter"), Look.SCHOENWETTER));
    }

    @Override
    protected Component getTypeName() {
        return Monsters.typeName(this.getType(), this.getLook());
    }
}
