package io.github.sirschrolli.kingdomsatwar.entity.shadow;

import io.github.sirschrolli.kingdomsatwar.entity.creature.DeerEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ShadowItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * Mondhirsch: ein scheuer, silbriger Hirsch der Schattenwelt mit einem Geweih aus Nachtkristall, das sanft leuchtet.
 * Er frisst Seelenbeeren und lässt sich mit ihnen anlocken und züchten. Wo er läuft, glimmen kleine Funken.
 */
public class MoonStagEntity extends DeerEntity {
    private static final net.minecraft.core.particles.DustParticleOptions ANTLER_SPARK =
            new net.minecraft.core.particles.DustParticleOptions(0xBFE6FF, 0.7f);
    public MoonStagEntity(EntityType<? extends MoonStagEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(ShadowItems.SOUL_BERRIES.get());
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        MoonStagEntity fawn = ModEntities.MOON_STAG.get().create(level, EntitySpawnReason.BREEDING);
        if (fawn != null) {
            fawn.setStag(this.random.nextBoolean());
        }
        return fawn;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide() && this.isStag() && !this.isBaby() && this.random.nextInt(8) == 0) {
            // Funken am Kristallgeweih
            this.level().addParticle(ANTLER_SPARK, this.getRandomX(0.5), this.getY(1.2) + this.random.nextFloat() * 0.4, this.getRandomZ(0.5),
                    0.0, 0.015, 0.0);
        }
    }
}
