package io.github.sirschrolli.kingdomsatwar.entity.creature.monster;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.entity.creature.farm.FarmAnimalConversion;
import io.github.sirschrolli.kingdomsatwar.entity.villager.VillagerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.block.LevelEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingConversionEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

/**
 * Ansteckung und Heilung der Dorfbewohner: Wen ein Wiedergänger (oder ein anderer Untoter der Zombie-Familie) tötet,
 * der steht auf "Normal" zur Hälfte, auf "Schwer" immer als untoter Dorfbewohner wieder auf – wie bei Vanilla. Wird ein
 * untoter Dorfbewohner geheilt, entsteht statt des Vanilla-Dorfbewohners wieder der Bewohner der Mod.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class MonsterEvents {
    private MonsterEvents() {}

    @SubscribeEvent
    static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof VillagerEntity villager) || !(villager.level() instanceof ServerLevel level)
                || !(event.getSource().getEntity() instanceof Zombie)) {
            return;
        }
        Difficulty difficulty = level.getDifficulty();
        if (difficulty != Difficulty.NORMAL && difficulty != Difficulty.HARD
                || difficulty == Difficulty.NORMAL && level.getRandom().nextBoolean()) {
            return;
        }
        UndeadVillager undead = UndeadVillager.zombify(level, villager);
        if (undead == null) {
            return;
        }
        event.setCanceled(true);
        villager.discard();
        level.addFreshEntity(undead);
        if (!villager.isSilent()) {
            level.levelEvent(null, LevelEvent.SOUND_ZOMBIE_INFECTED, villager.blockPosition(), 0);
        }
    }

    @SubscribeEvent
    static void onConvert(LivingConversionEvent.Post event) {
        if (!(event.getEntity() instanceof UndeadVillager undead) || !(event.getOutcome() instanceof Villager vanilla)
                || !(undead.level() instanceof ServerLevel level)) {
            return;
        }
        VillagerEntity cured = undead.createCured(level);
        if (cured != null) {
            vanilla.addTag(FarmAnimalConversion.REPLACED_TAG);       // der Vanilla-Dorfbewohner erscheint gar nicht erst
            level.addFreshEntity(cured);
        }
    }
}
