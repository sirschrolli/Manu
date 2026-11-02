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
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Rauchqualle (statt Ghast): ein düsterer Ballen aus Qualm, aus dem Rauchfäden hängen, mit schweren Lidern, unter denen
 * glühende Tränen hervorrinnen. Sie tut, was der Ghast tut: schwebt klagend über dem Nether, reißt die Augen auf und
 * speit Feuerbälle, die man mit einem Schlag zurückschmettern kann, und lässt Tränen und Schwarzpulver fallen.
 *
 * <p>Drei Gestalten: Aschenqualle (aschgrau, Glut im Qualm), Glutqualle (kohlschwarz mit Glutnestern, in den
 * Basaltdeltas und zuweilen im Ödland) und Seelenqualle (blaugrau mit Seelenfeuer, im Seelensandtal).
 */
public class Rauchqualle extends Ghast implements Monsters.Varied, ReplacesVanilla {
    private static final EntityDataAccessor<Integer> DATA_LOOK = SynchedEntityData.defineId(Rauchqualle.class, EntityDataSerializers.INT);

    public enum Look implements FarmAnimals.Breed {
        ASCHE, GLUT, SEELE
    }

    public Rauchqualle(EntityType<? extends Ghast> type, Level level) {
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

    /** Im Seelensandtal Seelenquallen, in den Basaltdeltas Glutquallen, sonst meist Aschenquallen. */
    public static Look lookFor(Holder<Biome> biome, RandomSource random) {
        if (biome.is(Biomes.SOUL_SAND_VALLEY)) {
            return Look.SEELE;
        }
        if (biome.is(Biomes.BASALT_DELTAS)) {
            return Look.GLUT;
        }
        return random.nextInt(4) == 0 ? Look.GLUT : Look.ASCHE;
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
        this.setLook(FarmAnimals.byName(Look.values(), input.getStringOr("Look", "asche"), Look.ASCHE));
    }

    @Override
    protected Component getTypeName() {
        return Monsters.typeName(this.getType(), this.getLook());
    }
}
