package io.github.sirschrolli.kingdomsatwar.voidrealm;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.material.VanillaMaterialConditions;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.material.MaterialRules;
import net.minecraft.world.level.levelgen.material.condition.MaterialCondition;
import net.minecraft.world.level.levelgen.material.rule.MaterialRule;

/**
 * Die Oberflächen der Leere: Leerenmoos auf den meisten Bruchstücken, Sternenstaub auf den Sternenstaubfeldern, nackter
 * Leerenstein mit Moosflecken auf den Splitterklippen – und auf den Weltenresten die Böden fremder Welten: Wiese auf Erde,
 * Wüstensand auf Sandstein oder Schnee auf Packeis, je nachdem, welche Welt das Bruchstück einmal war.
 */
public final class VoidSurface {
    public static final ResourceKey<MaterialRule> SURFACE = ResourceKey.create(Registries.MATERIAL_RULE, KingdomsAtWar.id("the_void"));

    private VoidSurface() {}

    private static MaterialRule state(Block block) {
        return MaterialRules.state(block.defaultBlockState());
    }

    public static void bootstrap(BootstrapContext<MaterialRule> context) {
        HolderGetter<MaterialCondition> conditions = context.lookup(Registries.MATERIAL_CONDITION);
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);

        MaterialRule stone = state(VoidBlocks.VOID_STONE.base().get());
        MaterialRule moss = state(VoidBlocks.VOID_MOSS.get());
        MaterialRule stardust = state(VoidBlocks.STARDUST.get());
        MaterialRule grass = state(Blocks.GRASS_BLOCK);
        MaterialRule dirt = state(Blocks.DIRT);
        MaterialRule sand = state(Blocks.SAND);
        MaterialRule sandstone = state(Blocks.SANDSTONE);
        MaterialRule snow = state(Blocks.SNOW_BLOCK);
        MaterialRule packedIce = state(Blocks.PACKED_ICE);

        MaterialCondition onFloor = MaterialRules.getCondition(conditions, VanillaMaterialConditions.ON_FLOOR);
        MaterialCondition underFloor = MaterialRules.getCondition(conditions, VanillaMaterialConditions.UNDER_FLOOR);
        MaterialCondition steep = MaterialRules.steep();
        MaterialCondition meadow = MaterialRules.noiseCondition2d(VoidTerrain.REMNANT, -1.0e9, -0.18);
        MaterialCondition frozen = MaterialRules.noiseCondition2d(VoidTerrain.REMNANT, 0.22);

        MaterialRule floor = MaterialRules.sequence(
                MaterialRules.ifTrue(MaterialRules.isBiome(biomes, VoidBiomes.STARDUST_FIELDS), MaterialRules.sequence(
                        MaterialRules.ifTrue(MaterialRules.noiseCondition2d(VoidTerrain.SURFACE, 0.62), moss), stardust)),
                MaterialRules.ifTrue(MaterialRules.isBiome(biomes, VoidBiomes.WORLD_REMNANTS), MaterialRules.sequence(
                        MaterialRules.ifTrue(meadow, grass), MaterialRules.ifTrue(frozen, snow), sand)),
                MaterialRules.ifTrue(MaterialRules.isBiome(biomes, VoidBiomes.SHARD_CLIFFS), MaterialRules.sequence(
                        MaterialRules.ifTrue(steep, stone),
                        MaterialRules.ifTrue(MaterialRules.noiseCondition2d(VoidTerrain.SURFACE, 0.15), moss), stone)),
                MaterialRules.ifTrue(MaterialRules.isBiome(biomes, VoidBiomes.EXPANSE), MaterialRules.sequence(
                        MaterialRules.ifTrue(MaterialRules.noiseCondition2d(VoidTerrain.SURFACE, -0.1), moss), stone)),
                MaterialRules.ifTrue(steep, stone),
                moss);

        MaterialRule under = MaterialRules.sequence(
                MaterialRules.ifTrue(MaterialRules.isBiome(biomes, VoidBiomes.STARDUST_FIELDS), stardust),
                MaterialRules.ifTrue(MaterialRules.isBiome(biomes, VoidBiomes.WORLD_REMNANTS), MaterialRules.sequence(
                        MaterialRules.ifTrue(meadow, dirt), MaterialRules.ifTrue(frozen, packedIce), sandstone)));

        context.register(SURFACE, MaterialRules.sequence(
                MaterialRules.ifTrue(onFloor, floor),
                MaterialRules.ifTrue(underFloor, under)));
    }
}
