package io.github.sirschrolli.kingdomsatwar.urmeer;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.material.VanillaMaterialConditions;
import net.minecraft.data.worldgen.material.VanillaMaterialRules;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.material.MaterialRules;
import net.minecraft.world.level.levelgen.material.condition.MaterialCondition;
import net.minecraft.world.level.levelgen.material.rule.MaterialRule;

/**
 * Die Oberflächen des Urmeers: Sand mit Flecken aus Kies und Ton auf dem weiten Grund, heller Sand und Kalkstein auf den
 * Korallenbänken, Abgrundsand und Abgrundstein in den Gräben; auf den Inseln Strände aus Sand, darüber Gras, an steilen
 * Hängen nackter Fels.
 */
public final class UrmeerSurface {
    public static final ResourceKey<MaterialRule> SURFACE = ResourceKey.create(Registries.MATERIAL_RULE, KingdomsAtWar.id("urmeer"));

    private UrmeerSurface() {}

    private static MaterialRule state(Block block) {
        return MaterialRules.state(block.defaultBlockState());
    }

    public static void bootstrap(BootstrapContext<MaterialRule> context) {
        HolderGetter<MaterialRule> rules = context.lookup(Registries.MATERIAL_RULE);
        HolderGetter<MaterialCondition> conditions = context.lookup(Registries.MATERIAL_CONDITION);
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);

        MaterialRule stone = state(io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.TIDE_STONE.base().get());
        MaterialRule sand = state(Blocks.SAND);
        MaterialRule sandstone = state(Blocks.SANDSTONE);
        MaterialRule gravel = state(Blocks.GRAVEL);
        MaterialRule clay = state(Blocks.CLAY);
        MaterialRule calcite = state(Blocks.CALCITE);
        MaterialRule grass = state(Blocks.GRASS_BLOCK);
        MaterialRule dirt = state(Blocks.DIRT);
        MaterialRule abyssSand = state(ModBlocks.ABYSSAL_SAND.get());
        MaterialRule abyssStone = state(ModBlocks.ABYSSAL_STONE.base().get());

        MaterialCondition onFloor = MaterialRules.getCondition(conditions, VanillaMaterialConditions.ON_FLOOR);
        MaterialCondition underFloor = MaterialRules.getCondition(conditions, VanillaMaterialConditions.UNDER_FLOOR);
        MaterialCondition notUnderwater = MaterialRules.getCondition(conditions, VanillaMaterialConditions.NOT_UNDERWATER);
        MaterialCondition steep = MaterialRules.steep();
        // Strand: bis drei Blöcke über dem Meeresspiegel
        MaterialCondition beachHigh = MaterialRules.not(MaterialRules.yBlockCheck(VerticalAnchor.absolute(UrmeerTerrain.SEA_LEVEL + 3), 0));
        MaterialCondition deepDown = MaterialRules.not(MaterialRules.yBlockCheck(VerticalAnchor.absolute(UrmeerTerrain.FLOOR - 18), 0));

        MaterialRule seabed = MaterialRules.sequence(
                MaterialRules.ifTrue(MaterialRules.noiseCondition2d(UrmeerTerrain.PATCHES, 0.5), clay),
                MaterialRules.ifTrue(MaterialRules.noiseCondition2d(UrmeerTerrain.SURFACE, -0.3, 0.12), gravel),
                sand);
        MaterialRule reefBed = MaterialRules.sequence(
                MaterialRules.ifTrue(MaterialRules.noiseCondition2d(UrmeerTerrain.PATCHES, 0.45), calcite),
                sand);
        MaterialRule trenchBed = MaterialRules.sequence(
                MaterialRules.ifTrue(steep, abyssStone),
                MaterialRules.ifTrue(MaterialRules.noiseCondition2d(UrmeerTerrain.PATCHES, 0.35), abyssStone),
                abyssSand);
        MaterialRule island = MaterialRules.sequence(
                MaterialRules.ifTrue(steep, stone),
                MaterialRules.ifTrue(beachHigh, sand),
                MaterialRules.ifTrue(notUnderwater, grass),
                sand);

        MaterialRule floor = MaterialRules.sequence(
                MaterialRules.ifTrue(MaterialRules.isBiome(biomes, UrmeerBiomes.TRENCH), trenchBed),
                MaterialRules.ifTrue(deepDown, trenchBed),
                MaterialRules.ifTrue(notUnderwater, island),
                MaterialRules.ifTrue(steep, stone),
                MaterialRules.ifTrue(MaterialRules.isBiome(biomes, UrmeerBiomes.REEF), reefBed),
                seabed);
        MaterialRule under = MaterialRules.sequence(
                MaterialRules.ifTrue(steep, stone),
                MaterialRules.ifTrue(MaterialRules.isBiome(biomes, UrmeerBiomes.TRENCH), abyssStone),
                MaterialRules.ifTrue(deepDown, abyssStone),
                MaterialRules.ifTrue(MaterialRules.isBiome(biomes, UrmeerBiomes.ISLES), MaterialRules.sequence(
                        MaterialRules.ifTrue(beachHigh, sandstone), dirt)),
                sandstone);

        context.register(SURFACE, MaterialRules.sequence(
                MaterialRules.getRule(rules, VanillaMaterialRules.BEDROCK_FLOOR),
                MaterialRules.ifTrue(onFloor, floor),
                MaterialRules.ifTrue(underFloor, under)));
    }
}
