package io.github.sirschrolli.kingdomsatwar.entity.creature.monster;

import io.github.sirschrolli.kingdomsatwar.entity.creature.farm.FarmAnimals;
import io.github.sirschrolli.kingdomsatwar.entity.creature.wild.WildAnimals;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Gemeinsames der eigenen Monster: jedes hat ein Aussehen (eine "Art"), das es sich beim Erscheinen nach dem Ort
 * aussucht – nach Biom, Tiefe oder Zufall –, sichtbar in Textur und Namen.
 */
public final class Monsters {
    private Monsters() {}

    /** Monster mit mehreren Arten: suchen sich ihr Aussehen nach dem Ort aus (auch beim Umwandeln aus Vanilla). */
    public interface Varied {
        void chooseLook(ServerLevelAccessor level, BlockPos pos);
    }

    /** Name je Art: {@code entity.kingdomsatwar.<typ>.<art>}. */
    public static Component typeName(EntityType<?> type, FarmAnimals.Breed look) {
        return FarmAnimals.typeName(type, look);
    }

    /** Unter der Oberfläche (kein freier Himmel über dieser Stelle und tiefer als 12 Blöcke darunter). */
    public static boolean underground(ServerLevelAccessor level, BlockPos pos) {
        return pos.getY() < level.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX(), pos.getZ()) - 12;
    }

    public static boolean snowy(Holder<Biome> biome) {
        return WildAnimals.snowy(biome);
    }

    public static boolean dry(Holder<Biome> biome) {
        return WildAnimals.desert(biome) || WildAnimals.badlands(biome) || WildAnimals.savanna(biome);
    }

    public static boolean jungle(Holder<Biome> biome) {
        return WildAnimals.jungle(biome) || biome.is(Biomes.BAMBOO_JUNGLE);
    }

    public static boolean wet(Holder<Biome> biome) {
        return WildAnimals.wetlands(biome);
    }

    public static boolean dark(Holder<Biome> biome) {
        return biome.is(Biomes.DARK_FOREST) || biome.is(Biomes.PALE_GARDEN);
    }

    public static boolean river(Holder<Biome> biome) {
        return biome.is(Biomes.RIVER) || biome.is(Biomes.FROZEN_RIVER);
    }
}
