package io.github.sirschrolli.kingdomsatwar.entity.creature.golem;

import io.github.sirschrolli.kingdomsatwar.entity.creature.farm.FarmAnimals;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Monsters;
import io.github.sirschrolli.kingdomsatwar.worldgen.overworld.OverworldBiomes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Eisenwächter (statt Eisengolem): ein geschmiedeter Wächter in Plattenrüstung, den ein Schmiedefeuer in der Brust
 * beseelt – hinter dem Gitter glüht es, aus dem Abzug auf dem Rücken steigt Rauch, im Sehschlitz des Topfhelms glimmen
 * die Augen. Er wird gebaut wie der Vanilla-Golem (vier Eisenblöcke und ein geschnitzter Kürbis), lässt Eisen und Mohn
 * fallen, wird mit Eisenbarren geflickt, schenkt Dorfbewohnern Mohnblumen und Kupferknechten eine Blume für den
 * Blitzableiter. Er beschützt die Bewohner der Dörfer: wer einen angreift, bekommt es mit ihm zu tun (gebaute Wächter
 * greifen keine Spieler an). In jedem Dorf wacht einer am Dorfplatz.
 *
 * <p>Vier Gestalten nach dem Land, in dem er entsteht: Schmiedeeisen, Mooswächter (Wälder, Dschungel, Sümpfe),
 * Frostwächter (Schnee und Eis) und Rostwächter (Küsten, Strände, Flüsse).
 */
public class IronSentinel extends IronGolem implements Monsters.Varied {
    private static final EntityDataAccessor<Integer> DATA_LOOK = SynchedEntityData.defineId(IronSentinel.class, EntityDataSerializers.INT);

    public enum Look implements FarmAnimals.Breed {
        SCHMIEDEEISEN, MOOSWAECHTER, FROSTWAECHTER, ROSTWAECHTER
    }

    public IronSentinel(EntityType<? extends IronSentinel> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(3, new MoveTowardsRestrictionGoal(this, 0.6));
        this.targetSelector.addGoal(1, new GolemGoals.DefendVillagers(this));
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

    /** Die Gestalt nach dem Land: Schnee macht Frost, Feuchte und dichter Wald Moos, Salzluft Rost. */
    public static Look lookFor(Holder<Biome> biome) {
        if (Monsters.snowy(biome) || biome.is(BiomeTags.IS_TAIGA) && biome.value().getBaseTemperature() < 0.15f) {
            return Look.FROSTWAECHTER;
        }
        if (Monsters.jungle(biome) || Monsters.wet(biome) || Monsters.dark(biome) || biome.is(Biomes.LUSH_CAVES)
                || biome.is(Biomes.OLD_GROWTH_SPRUCE_TAIGA) || biome.is(Biomes.OLD_GROWTH_PINE_TAIGA) || biome.is(OverworldBiomes.PRIMEVAL_FOREST)) {
            return Look.MOOSWAECHTER;
        }
        if (biome.is(BiomeTags.IS_BEACH) || biome.is(BiomeTags.IS_OCEAN) || biome.is(BiomeTags.IS_RIVER) || biome.is(Biomes.STONY_SHORE)
                || biome.is(OverworldBiomes.STORM_COAST) || biome.is(OverworldBiomes.CORAL_COAST)) {
            return Look.ROSTWAECHTER;
        }
        return Look.SCHMIEDEEISEN;
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
        this.setLook(FarmAnimals.byName(Look.values(), input.getStringOr("Look", "schmiedeeisen"), Look.SCHMIEDEEISEN));
    }

    @Override
    protected Component getTypeName() {
        return Monsters.typeName(this.getType(), this.getLook());
    }

    @Override
    public void aiStep() {
        super.aiStep();
        // Rauch aus dem Abzug auf dem Rücken (2,4 Blöcke hoch, ein Drittel Block hinter der Mitte)
        if (this.level().isClientSide() && this.isAlive() && this.random.nextInt(this.isAggressive() ? 3 : 9) == 0) {
            float yaw = this.yBodyRot * Mth.DEG_TO_RAD;
            double x = this.getX() + Mth.sin(yaw) * 0.34;
            double z = this.getZ() - Mth.cos(yaw) * 0.34;
            this.level().addParticle(ParticleTypes.SMOKE, x, this.getY() + 2.42, z, 0.0, 0.03 + this.random.nextDouble() * 0.02, 0.0);
        }
    }
}
