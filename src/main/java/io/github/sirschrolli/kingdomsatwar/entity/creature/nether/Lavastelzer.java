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
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Lavastelzer (statt Schreiter): ein warziges Lavawesen auf langen Stelzbeinen mit sechs Lappen aus feurigem Haar, einem
 * breiten Maul und großen, glühenden Augen. Er tut, was der Schreiter tut: watet über Lavaseen, lässt sich mit Sattel
 * und Wirrpilzrute reiten und antreiben, mit Wirrpilzen locken und paaren – und friert, sobald er die Lava verlässt
 * (dann wird er fahl, nichts glimmt mehr, er zittert und wird langsam).
 *
 * <p>Zwei Gestalten: Glutstelzer (feuerrot) und Seelenstelzer (im Seelensandtal, türkis wie Seelenfeuer).
 */
public class Lavastelzer extends Strider implements Monsters.Varied, ReplacesVanilla {
    private static final EntityDataAccessor<Integer> DATA_LOOK = SynchedEntityData.defineId(Lavastelzer.class, EntityDataSerializers.INT);

    public enum Look implements FarmAnimals.Breed {
        GLUT, SEELE
    }

    public Lavastelzer(EntityType<? extends Strider> type, Level level) {
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

    /** Im Seelensandtal Seelenstelzer, sonst Glutstelzer (und selten ein verirrter Seelenstelzer). */
    public static Look lookFor(Holder<Biome> biome, RandomSource random) {
        return biome.is(Biomes.SOUL_SAND_VALLEY) || random.nextInt(20) == 0 ? Look.SEELE : Look.GLUT;
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

    /** Junge der eigenen Art in der Gestalt der Mutter oder des Vaters (selten in der des Landes). */
    @Override
    public @Nullable Strider getBreedOffspring(ServerLevel level, AgeableMob partner) {
        Lavastelzer young = ModEntities.STRIDER.get().create(level, EntitySpawnReason.BREEDING);
        if (young != null) {
            Look other = partner instanceof Lavastelzer stelzer ? stelzer.getLook() : this.getLook();
            young.setLook(FarmAnimals.inherit(this.random, this.getLook(), other, lookFor(level.getBiome(this.blockPosition()), this.random)));
        }
        return young;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString("Look", this.getLook().id());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setLook(FarmAnimals.byName(Look.values(), input.getStringOr("Look", "glut"), Look.GLUT));
    }

    @Override
    protected Component getTypeName() {
        return Monsters.typeName(this.getType(), this.getLook());
    }
}
