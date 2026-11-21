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
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Brandgebein (statt Witherskelett): die Wache der Netherfestungen – verkohlte Knochen, in deren Rissen, Schädelnähten
 * und Gelenken noch Glut glimmt, Flammen auf dem Schädel, ein angesengter Wappenrock in den Farben des Aschenkönigs mit
 * seiner Dreizackkrone auf der Brust und ein Kragen aus geschwärzten Kettengliedern. Es tut, was das Witherskelett tut:
 * trägt ein Steinschwert, verdorrt, wen es trifft, jagt Borstlinge, ist feuerfest, und aus seinen Schädeln und Seelensand
 * erwacht der Aschenkönig.
 *
 * <p>Zwei Gestalten: Brandgebein (Glut) und Seelengebein (türkises Seelenfeuer, in den Festungen des Seelensandtals).
 */
public class Brandgebein extends WitherSkeleton implements Monsters.Varied, ReplacesVanilla {
    private static final EntityDataAccessor<Integer> DATA_LOOK = SynchedEntityData.defineId(Brandgebein.class, EntityDataSerializers.INT);

    public enum Look implements FarmAnimals.Breed {
        BRAND, SEELE
    }

    public Brandgebein(EntityType<? extends WitherSkeleton> type, Level level) {
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

    /** Im Seelensandtal Seelengebeine, sonst Brandgebeine. */
    public static Look lookFor(Holder<Biome> biome) {
        return biome.is(Biomes.SOUL_SAND_VALLEY) ? Look.SEELE : Look.BRAND;
    }

    @Override
    public void chooseLook(ServerLevelAccessor level, BlockPos pos) {
        this.setLook(lookFor(level.getBiome(pos)));
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
            @Nullable SpawnGroupData groupData) {
        this.chooseLook(level, this.blockPosition());
        return super.finalizeSpawn(level, difficulty, reason, groupData);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString("Look", this.getLook().id());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setLook(FarmAnimals.byName(Look.values(), input.getStringOr("Look", "brand"), Look.BRAND));
    }

    @Override
    protected Component getTypeName() {
        return Monsters.typeName(this.getType(), this.getLook());
    }
}
