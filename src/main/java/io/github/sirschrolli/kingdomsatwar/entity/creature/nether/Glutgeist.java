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
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Glutgeist (statt Lohe): ein Feuergeist mit einer Maske aus geschwärztem Erz, einer Krone aus Flammen und einem
 * glühenden Kern, um den zwölf Glutklingen mit leuchtenden Runen kreisen. Er tut, was die Lohe tut: bewacht die
 * Netherfestungen, entzündet sich und schießt Feuerkugeln in Dreiergruppen, wird von Schneebällen und Wasser verletzt
 * und lässt Lohenruten fallen.
 *
 * <p>Zwei Gestalten: Glutgeist (goldenes Feuer) und Seelengeist (türkises Seelenfeuer, in den Festungen des
 * Seelensandtals).
 */
public class Glutgeist extends Blaze implements Monsters.Varied, ReplacesVanilla {
    private static final EntityDataAccessor<Integer> DATA_LOOK = SynchedEntityData.defineId(Glutgeist.class, EntityDataSerializers.INT);

    public enum Look implements FarmAnimals.Breed {
        GLUT, SEELE
    }

    public Glutgeist(EntityType<? extends Blaze> type, Level level) {
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

    /** Im Seelensandtal Seelengeister, sonst Glutgeister. */
    public static Look lookFor(Holder<Biome> biome, RandomSource random) {
        return biome.is(Biomes.SOUL_SAND_VALLEY) ? Look.SEELE : Look.GLUT;
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
