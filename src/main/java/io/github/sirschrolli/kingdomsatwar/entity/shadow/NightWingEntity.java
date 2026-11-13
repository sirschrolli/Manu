package io.github.sirschrolli.kingdomsatwar.entity.shadow;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jspecify.annotations.Nullable;

/**
 * Nachtschwinge: kreist hoch über den Schattengipfeln und den schwebenden Inseln und stößt auf Wanderer herab – wie ein
 * Phantom, aber größer, mit Fledermausflügeln und glühenden Augen. Anders als Phantome kommt sie nicht, weil jemand
 * nicht schläft: Sie lebt hier.
 */
public class NightWingEntity extends Phantom {
    public NightWingEntity(EntityType<? extends NightWingEntity> type, Level level) {
        super(type, level);
        this.xpReward = 8;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 22.0)
                .add(Attributes.ATTACK_DAMAGE, 6.0)
                .add(Attributes.FOLLOW_RANGE, 48.0);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
            @Nullable SpawnGroupData groupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, groupData);
        this.setPhantomSize(1 + this.random.nextInt(2));
        return data;
    }

    /** Unter freiem Himmel, nicht im Frieden. */
    public static boolean checkNightWingSpawnRules(EntityType<? extends Mob> type, LevelAccessor level, EntitySpawnReason reason,
            BlockPos pos, RandomSource random) {
        return level.getDifficulty() != Difficulty.PEACEFUL && (EntitySpawnReason.isSpawner(reason) || level.canSeeSky(pos));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.PHANTOM_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.PHANTOM_HURT;
    }

    @Override
    public float getVoicePitch() {
        return 0.6f + (this.random.nextFloat() - 0.5f) * 0.1f;
    }
}
