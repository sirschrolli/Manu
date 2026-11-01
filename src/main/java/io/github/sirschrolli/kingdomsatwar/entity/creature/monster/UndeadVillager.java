package io.github.sirschrolli.kingdomsatwar.entity.creature.monster;

import io.github.sirschrolli.kingdomsatwar.economy.town.TownNames;
import io.github.sirschrolli.kingdomsatwar.entity.villager.VillageProfession;
import io.github.sirschrolli.kingdomsatwar.entity.villager.VillagerEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.monster.zombie.ZombieVillager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Untoter Dorfbewohner (statt Zombie-Dorfbewohner): ein Bewohner der eigenen Dörfer in der Tracht seines Berufs, aber
 * verwest. Er lässt sich heilen wie der Vanilla-Zombie-Dorfbewohner (Trank der Schwäche, dann ein goldener Apfel) und
 * wird wieder genau der Dorfbewohner, der er war – mit Namen, Beruf, Gesicht, Dorf, Bett und Arbeitsplatz, wenn er
 * einmal einer war (Wiedergänger machen Dorfbewohner auf "Normal" zur Hälfte, auf "Schwer" immer zu Untoten).
 */
public class UndeadVillager extends ZombieVillager implements Monsters.Varied {
    private static final EntityDataAccessor<Byte> DATA_PROFESSION = SynchedEntityData.defineId(UndeadVillager.class,
            EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> DATA_VARIANT = SynchedEntityData.defineId(UndeadVillager.class, EntityDataSerializers.BYTE);
    /** Der Dorfbewohner, der er einmal war (zum Heilen); fehlt bei Untoten, die schon als solche erschienen sind. */
    private @Nullable CompoundTag formerSelf;

    public UndeadVillager(EntityType<? extends UndeadVillager> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_PROFESSION, (byte) 0);
        builder.define(DATA_VARIANT, (byte) 0);
    }

    public VillageProfession getProfession() {
        VillageProfession[] all = VillageProfession.values();
        return all[Mth.clamp(this.entityData.get(DATA_PROFESSION), 0, all.length - 1)];
    }

    public void setProfession(VillageProfession profession) {
        this.entityData.set(DATA_PROFESSION, (byte) profession.ordinal());
    }

    public int getVariant() {
        return Mth.clamp(this.entityData.get(DATA_VARIANT), 0, this.getProfession().variants() - 1);
    }

    public void setVariant(int variant) {
        this.entityData.set(DATA_VARIANT, (byte) Mth.clamp(variant, 0, 7));
    }

    /** Das letzte Gesicht jedes Berufs ist eine Frau (wie bei den Lebenden). */
    public boolean isFeminine() {
        return this.getVariant() == this.getProfession().variants() - 1;
    }

    public @Nullable CompoundTag formerSelf() {
        return this.formerSelf;
    }

    @Override
    public void chooseLook(ServerLevelAccessor level, BlockPos pos) {
        VillageProfession[] all = VillageProfession.values();
        VillageProfession profession = all[level.getRandom().nextInt(all.length)];
        this.setProfession(profession);
        this.setVariant(level.getRandom().nextInt(profession.variants()));
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
        output.putString("Profession", this.getProfession().id());
        output.putInt("Variant", this.getVariant());
        if (this.formerSelf != null) {
            output.store("FormerSelf", CompoundTag.CODEC, this.formerSelf);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setProfession(VillageProfession.byId(input.getStringOr("Profession", "peasant")));
        this.setVariant(input.getIntOr("Variant", 0));
        this.formerSelf = input.read("FormerSelf", CompoundTag.CODEC).orElse(null);
    }

    @Override
    protected Component getTypeName() {
        return Component.translatable(this.getType().getDescriptionId() + (this.isFeminine() ? ".female" : ""));
    }

    // =====================================================================================
    // Ansteckung und Heilung
    // =====================================================================================

    /** Macht aus einem Dorfbewohner einen Untoten (ohne ihn der Welt hinzuzufügen); merkt sich, wer er war. */
    public static @Nullable UndeadVillager zombify(ServerLevel level, VillagerEntity villager) {
        UndeadVillager undead = ModEntities.ZOMBIE_VILLAGER.get().create(level, EntitySpawnReason.CONVERSION);
        if (undead == null) {
            return null;
        }
        undead.snapTo(villager.getX(), villager.getY(), villager.getZ(), villager.getYRot(), villager.getXRot());
        undead.finalizeSpawn(level, level.getCurrentDifficultyAt(undead.blockPosition()), EntitySpawnReason.CONVERSION,
                new Zombie.ZombieGroupData(false, true));
        TagValueOutput out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
        villager.saveWithoutId(out);
        undead.formerSelf = out.buildResult();
        undead.setProfession(villager.getProfession());
        undead.setVariant(villager.getVariant());
        if (villager.hasCustomName()) {
            undead.setCustomName(villager.getCustomName());
        }
        undead.yBodyRot = villager.yBodyRot;
        undead.yHeadRot = villager.yHeadRot;
        undead.setPersistenceRequired();
        return undead;
    }

    /** Der geheilte Dorfbewohner (ohne ihn der Welt hinzuzufügen). */
    public @Nullable VillagerEntity createCured(ServerLevel level) {
        VillagerEntity villager = ModEntities.VILLAGER.get().create(level, EntitySpawnReason.CONVERSION);
        if (villager == null) {
            return null;
        }
        if (this.formerSelf != null) {
            CompoundTag tag = this.formerSelf.copy();
            for (String key : new String[] {"UUID", "Health", "DeathTime", "HurtTime", "HurtByTimestamp", "active_effects", "Fire", "Motion",
                    "fall_distance", "Brain"}) {
                tag.remove(key);
            }
            villager.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), tag));
        } else {
            boolean feminine = this.isFeminine();
            String name = this.hasCustomName() && this.getCustomName() != null ? this.getCustomName().getString()
                    : TownNames.citizen(this.random, feminine);
            villager.setup(this.getProfession(), this.getVariant(), name, this.blockPosition(), "", null, null, this.random.nextLong());
        }
        villager.snapTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
        villager.yBodyRot = this.yBodyRot;
        villager.yHeadRot = this.yHeadRot;
        villager.setHealth(villager.getMaxHealth());
        villager.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 200, 0));
        if (this.isPersistenceRequired()) {
            villager.setPersistenceRequired();
        }
        return villager;
    }
}
