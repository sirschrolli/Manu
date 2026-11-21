package io.github.sirschrolli.kingdomsatwar.entity.creature.nether;

import io.github.sirschrolli.kingdomsatwar.entity.ReplacesVanilla;
import io.github.sirschrolli.kingdomsatwar.entity.creature.farm.FarmAnimals;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Monsters;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.cubemob.AbstractCubeMob;
import net.minecraft.world.entity.monster.cubemob.MagmaCube;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Glutschleim (statt Magmawürfel): ein Würfel aus Lava unter einer Kruste erkalteter Schollen mit glühenden Fugen, mit
 * einem Gesicht aus Glut und einem weißglühenden Herzen; springt er, leuchten die Schichten zwischen den Schollen auf.
 * Er tut, was der Magmawürfel tut: hüpft weit, fürchtet weder Feuer noch Lava, teilt sich beim Sterben in kleinere
 * Schleime gleicher Art und lässt Magmacreme fallen.
 *
 * <p>Zwei Gestalten: Glutschleim (schwarze Kruste, orange Glut) und Basaltschleim (graue Basaltkruste, tiefrote Glut, in
 * den Basaltdeltas).
 */
public class Glutschleim extends MagmaCube implements Monsters.Varied, ReplacesVanilla {
    private static final EntityDataAccessor<Integer> DATA_LOOK = SynchedEntityData.defineId(Glutschleim.class, EntityDataSerializers.INT);

    public enum Look implements FarmAnimals.Breed {
        GLUT, BASALT
    }

    public Glutschleim(EntityType<? extends MagmaCube> type, Level level) {
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

    /** In den Basaltdeltas Basaltschleime, sonst Glutschleime. */
    public static Look lookFor(Holder<Biome> biome, RandomSource random) {
        return biome.is(Biomes.BASALT_DELTAS) ? Look.BASALT : Look.GLUT;
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

    /** Beim Teilen behalten die kleinen Schleime die Art des großen. */
    @Override
    protected void setUpSplitCube(AbstractCubeMob cube, int halfSize, float xd, float zd) {
        super.setUpSplitCube(cube, halfSize, xd, zd);
        if (cube instanceof Glutschleim slime) {
            slime.setLook(this.getLook());
        }
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
