package io.github.sirschrolli.kingdomsatwar.entity.creature.nether;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

/** Gemeinsames des Nether-Volks und seiner Tiere. */
public final class NetherFolk {
    private NetherFolk() {}

    /** Im Karmesinwald? */
    public static boolean crimson(Holder<Biome> biome) {
        return biome.is(Biomes.CRIMSON_FOREST);
    }
}
