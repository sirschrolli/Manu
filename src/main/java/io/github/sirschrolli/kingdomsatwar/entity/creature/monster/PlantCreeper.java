package io.github.sirschrolli.kingdomsatwar.entity.creature.monster;

import io.github.sirschrolli.kingdomsatwar.entity.ReplacesVanilla;
import io.github.sirschrolli.kingdomsatwar.entity.creature.farm.FarmAnimals;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Kriecher (statt Creeper): ein pflanzenartiges Wesen, je nach Gegend ein moosgrüner Waldkriecher, ein stacheliger
 * Wüstenkriecher, ein bereifter Schneekriecher, ein Höhlenkriecher mit Flechten und leuchtenden Pilzflecken oder ein
 * blühender Dschungelkriecher. Zischt, explodiert und lässt sich vom Blitz aufladen wie der Vanilla-Creeper.
 */
public class PlantCreeper extends Creeper implements Monsters.Varied, ReplacesVanilla {
    private static final EntityDataAccessor<Integer> DATA_LOOK = SynchedEntityData.defineId(PlantCreeper.class, EntityDataSerializers.INT);

    public enum Look implements FarmAnimals.Breed {
        WALD, WUESTE, SCHNEE, HOEHLE, DSCHUNGEL
    }

    public PlantCreeper(EntityType<? extends PlantCreeper> type, Level level) {
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
        this.setLook(lookFor(level.getBiome(pos), Monsters.underground(level, pos)));
    }

    /** Unter Tage Höhlenkriecher, sonst nach dem Biom. */
    public static Look lookFor(net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome> biome, boolean underground) {
        return underground ? Look.HOEHLE
                : Monsters.snowy(biome) ? Look.SCHNEE
                : Monsters.dry(biome) ? Look.WUESTE
                : Monsters.jungle(biome) || Monsters.wet(biome) ? Look.DSCHUNGEL
                : Look.WALD;
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
        this.setLook(FarmAnimals.byName(Look.values(), input.getStringOr("Look", "wald"), Look.WALD));
    }

    @Override
    protected Component getTypeName() {
        return Monsters.typeName(this.getType(), this.getLook());
    }
}
