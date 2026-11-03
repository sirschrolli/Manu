package io.github.sirschrolli.kingdomsatwar.entity.creature.nether;

import io.github.sirschrolli.kingdomsatwar.entity.ReplacesVanilla;
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
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Glutkeiler (statt Hoglin): ein schwerer Keiler des Nethers mit Schulterbuckel, Borstenmähne und langen, geschwungenen
 * Hauern. Er tut, was der Hoglin tut: greift Spieler an und schleudert sie mit dem Kopf fort, flieht vor Wirrpilzen,
 * Portalen und Seelenankern, lässt sich mit Karmesinpilzen paaren, wird von Borstlingen gejagt (und flieht, wenn sie in
 * der Überzahl sind) und verwest in der Oberwelt zum Moderkeiler.
 *
 * <p>Drei Gestalten: Karmesinkeiler (im Karmesinwald, rostrot, mit Karmesinpilzen auf dem Rücken), Aschenkeiler (aschgrau,
 * die Borstenspitzen glimmen) und – selten – der Schlackenkeiler mit glühender Magmakruste auf den Schultern.
 */
public class Glutkeiler extends Hoglin implements Monsters.Varied, ReplacesVanilla {
    private static final EntityDataAccessor<Integer> DATA_LOOK = SynchedEntityData.defineId(Glutkeiler.class, EntityDataSerializers.INT);

    public enum Look implements FarmAnimals.Breed {
        KARMESIN, ASCHEN, SCHLACKE
    }

    public Glutkeiler(EntityType<? extends Hoglin> type, Level level) {
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

    /** Gestalt nach dem Land: im Karmesinwald meist Karmesinkeiler, sonst Aschenkeiler; jeder zwölfte ein Schlackenkeiler. */
    public static Look lookFor(Holder<Biome> biome, net.minecraft.util.RandomSource random) {
        if (random.nextInt(12) == 0) {
            return Look.SCHLACKE;
        }
        return NetherFolk.crimson(biome) ? Look.KARMESIN : Look.ASCHEN;
    }

    @Override
    public void chooseLook(ServerLevelAccessor level, BlockPos pos) {
        this.setLook(lookFor(level.getBiome(pos), level.getRandom()));
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
            @Nullable SpawnGroupData groupData) {
        this.chooseLook(level, this.blockPosition());
        return super.finalizeSpawn(level, difficulty, reason, groupData);
    }

    /** Frischlinge der eigenen Art; sie haben die Gestalt der Mutter oder des Vaters (selten die des Landes). */
    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        Glutkeiler piglet = ModEntities.HOGLIN.get().create(level, EntitySpawnReason.BREEDING);
        if (piglet != null) {
            piglet.setPersistenceRequired();
            Look other = partner instanceof Glutkeiler keiler ? keiler.getLook() : this.getLook();
            piglet.setLook(FarmAnimals.inherit(this.random, this.getLook(), other, lookFor(level.getBiome(this.blockPosition()), this.random)));
        }
        return piglet;
    }

    /** In der Oberwelt wird aus ihm ein Moderkeiler – karmesinrot oder aschgrau verwest, wie er war. */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends Mob> @Nullable T convertTo(EntityType<T> type, ConversionParams params, EntitySpawnReason reason,
            ConversionParams.AfterConversion<T> after) {
        if (type != EntityTypes.ZOGLIN) {
            return super.convertTo(type, params, reason, after);
        }
        Moderkeiler.Look look = this.getLook() == Look.KARMESIN ? Moderkeiler.Look.MODER_KARMESIN : Moderkeiler.Look.MODER_ASCHEN;
        return (T) super.convertTo(ModEntities.ZOGLIN.get(), params, reason, zoglin -> {
            zoglin.setLook(look);
            after.finalizeConversion((T) zoglin);
        });
    }

    @Override
    public void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString("Look", this.getLook().id());
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setLook(FarmAnimals.byName(Look.values(), input.getStringOr("Look", "aschen"), Look.ASCHEN));
    }

    @Override
    protected Component getTypeName() {
        return Monsters.typeName(this.getType(), this.getLook());
    }
}
