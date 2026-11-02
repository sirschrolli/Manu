package io.github.sirschrolli.kingdomsatwar.entity.creature.nether;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

/** Gemeinsames des Nether-Volks (Borstlinge, Borstenhünen, Moderborstlinge). */
public final class NetherFolk {
    private NetherFolk() {}

    /** Im Karmesinwald? */
    public static boolean crimson(Holder<Biome> biome) {
        return biome.is(Biomes.CRIMSON_FOREST);
    }

    /** Untot – vor solchen weichen Borstlinge zurück (das Vanilla-Gehirn kennt nur die Vanilla-Untoten). */
    public static boolean undead(Entity entity) {
        return entity instanceof RottenBristling;
    }

    /**
     * Das Vanilla-Gehirn eines Borstlings merkt sich nur Vanilla-Zombie-Piglins und -Zoglins als „untot in der Nähe“.
     * Steht stattdessen ein eigener Untoter in Sichtweite, wird er hier eingetragen – dann weicht der Borstling wie
     * gewohnt zurück.
     */
    public static void noticeUndead(AbstractPiglin piglin) {
        var brain = piglin.getBrain();
        if (piglin.tickCount % 10 != 0 || brain.hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED)) {
            return;
        }
        brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
                .flatMap(visible -> visible.findClosest(NetherFolk::undead))
                .ifPresent(undead -> brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED, undead));
    }
}
