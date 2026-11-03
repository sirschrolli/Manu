package io.github.sirschrolli.kingdomsatwar.entity.creature.end;

import io.github.sirschrolli.kingdomsatwar.entity.ReplacesVanilla;
import io.github.sirschrolli.kingdomsatwar.entity.creature.farm.FarmAnimals;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Monsters;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Leerwandler (statt Enderman): ein Leib wie aus dem Nachthimmel geschnitten – fast schwarz, voller schwach leuchtender
 * Sterne, über der Brust ein Riss in die leuchtende Leere –, ein langer Kopf mit zwei schmalen, schrägen, gleißenden
 * Augen, über dem drei Splitter aus Purpurkristall kreisen, und Schleier aus Schatten. Er tut, was der Enderman tut:
 * wandert, trägt Blöcke davon, springt fort, scheut Wasser, wird wütend, wenn man ihm in die Augen sieht – dann hebt
 * sich sein Kopf vom Kiefer und darunter klafft ein Schlund voller dünner Zähne –, jagt Leermilben und lässt
 * Enderperlen fallen.
 *
 * <p>Zwei Gestalten: Leerwandler (violett, in Oberwelt und End) und Wirrwandler (türkis, im Nether).
 */
public class Leerwandler extends Enderman implements Monsters.Varied, ReplacesVanilla {
    private static final EntityDataAccessor<Integer> DATA_LOOK = SynchedEntityData.defineId(Leerwandler.class, EntityDataSerializers.INT);

    public enum Look implements FarmAnimals.Breed {
        LEERE, WIRR
    }

    public Leerwandler(EntityType<? extends Enderman> type, Level level) {
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

    /** Im Nether Wirrwandler, sonst Leerwandler. */
    public static Look lookFor(Holder<Biome> biome) {
        return biome.is(BiomeTags.IS_NETHER) ? Look.WIRR : Look.LEERE;
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
        this.setLook(FarmAnimals.byName(Look.values(), input.getStringOr("Look", "leere"), Look.LEERE));
    }

    @Override
    protected Component getTypeName() {
        return Monsters.typeName(this.getType(), this.getLook());
    }
}
