package io.github.sirschrolli.kingdomsatwar.entity.creature.golem;

import io.github.sirschrolli.kingdomsatwar.entity.ReplacesVanilla;
import io.github.sirschrolli.kingdomsatwar.entity.creature.farm.FarmAnimals;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Monsters;
import io.github.sirschrolli.kingdomsatwar.entity.creature.wild.WildAnimals;
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
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Wichtel (statt Hilfsgeist): ein kleiner Hausgeist mit großen leuchtenden Augen, spitzen Ohren und Libellenflügeln.
 * Er sammelt wie der Vanilla-Hilfsgeist Gegenstände der Art, die man ihm gibt, und bringt sie dem Spieler oder zum
 * Notenblock; zur Musik einer Jukebox tanzt er, und mit einem Amethystsplitter wird aus einem Wichtel zwei (das neue
 * trägt dieselbe Mütze).
 *
 * <p>Vier Gestalten: Mooswichtel (Moosmütze mit Blüten), Pilzwichtel (Fliegenpilz-Hut), Laubwichtel (Herbstlaub) und
 * Sternwichtel (nachtblau mit goldenen Sternen).
 */
public class HouseSprite extends Allay implements Monsters.Varied, ReplacesVanilla {
    private static final EntityDataAccessor<Integer> DATA_LOOK = SynchedEntityData.defineId(HouseSprite.class, EntityDataSerializers.INT);

    public enum Look implements FarmAnimals.Breed {
        MOOSWICHTEL, PILZWICHTEL, LAUBWICHTEL, STERNWICHTEL
    }

    public HouseSprite(EntityType<? extends HouseSprite> type, Level level) {
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

    /** Im dunklen Wald eher Pilzwichtel, in Wäldern Moos- und Laubwichtel, sonst alle vier. */
    @Override
    public void chooseLook(ServerLevelAccessor level, BlockPos pos) {
        Holder<Biome> biome = level.getBiome(pos);
        int[] weights = Monsters.dark(biome) ? new int[] {20, 50, 15, 15}
                : WildAnimals.forest(biome) ? new int[] {35, 20, 35, 10} : new int[] {30, 25, 25, 20};
        this.setLook(Look.values()[FarmAnimals.pick(level.getRandom(), weights)]);
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
        this.setLook(FarmAnimals.byName(Look.values(), input.getStringOr("Look", "mooswichtel"), Look.MOOSWICHTEL));
    }

    @Override
    protected Component getTypeName() {
        return Monsters.typeName(this.getType(), this.getLook());
    }
}
