package io.github.sirschrolli.kingdomsatwar.entity.creature.rare;

import io.github.sirschrolli.kingdomsatwar.entity.ReplacesVanilla;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Urwühler (statt Schnüffler): ein uraltes, zotteliges Grabtier wie ein Moschusochse, mit langem Fellbehang, einer
 * breiten Rüsselplatte und einer Moosmatte auf dem Rücken, auf der Farne und Blumen aus alter Zeit wachsen. Er tut,
 * was der Vanilla-Schnüffler tut: schnuppert, gräbt alte Samen (Fackellilie, Kannenpflanze) aus, legt Eier, aus denen
 * Junge schlüpfen, und folgt dem, der Fackellilien-Samen in der Hand hält.
 *
 * <p>Drei Gestalten nach dem Land, in dem er schlüpft: Zottelwühler (dunkles Kastanienfell), Steppenwühler (in warmen,
 * trockenen Ländern; ockerfarben mit dunklen Rückenstreifen) und Frostwühler (in kalten Ländern; langes graues Fell).
 */
public class PrimevalDigger extends Sniffer implements Monsters.Varied, ReplacesVanilla {
    private static final EntityDataAccessor<Integer> DATA_LOOK = SynchedEntityData.defineId(PrimevalDigger.class, EntityDataSerializers.INT);

    public enum Look implements FarmAnimals.Breed {
        ZOTTELWUEHLER, STEPPENWUEHLER, FROSTWUEHLER
    }

    public PrimevalDigger(EntityType<? extends PrimevalDigger> type, Level level) {
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

    /** Gestalt nach dem Land: kalt → Frostwühler, warm und trocken → Steppenwühler, sonst Zottelwühler. */
    public static Look lookFor(Holder<Biome> biome) {
        if (FarmAnimals.cold(biome)) {
            return Look.FROSTWUEHLER;
        }
        return Monsters.dry(biome) ? Look.STEPPENWUEHLER : Look.ZOTTELWUEHLER;
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

    /** Ein Junges (aus dem Spawn-Ei) hat die Gestalt des Elterntiers. */
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        PrimevalDigger child = ModEntities.SNIFFER.get().create(level, EntitySpawnReason.BREEDING);
        if (child != null) {
            child.setLook(this.getLook());
        }
        return child;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString("Look", this.getLook().id());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setLook(FarmAnimals.byName(Look.values(), input.getStringOr("Look", "zottelwuehler"), Look.ZOTTELWUEHLER));
    }

    @Override
    protected Component getTypeName() {
        return Monsters.typeName(this.getType(), this.getLook());
    }
}
