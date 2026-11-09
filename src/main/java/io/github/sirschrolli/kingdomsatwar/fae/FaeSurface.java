package io.github.sirschrolli.kingdomsatwar.fae;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.material.condition.MaterialCondition;
import net.minecraft.world.level.levelgen.material.rule.MaterialRule;
import net.minecraft.world.level.levelgen.material.MaterialRules;
import net.minecraft.data.worldgen.material.VanillaMaterialConditions;
import net.minecraft.data.worldgen.material.VanillaMaterialRules;

/**
 * Die Oberflächen des Feenreichs: Feengras auf Feenerde fast überall, samtiges Feenmoos im Pilzhain (und in Flecken im
 * Glimmerwald), offene, dunkle Feenerde zwischen dem Gras der Dornenwildnis, nackter Feenstein an steilen Hängen, heller
 * Sand an den Ufern der Mondseen und Erde, Sand und Ton am Seegrund. Ganz unten das Grundgestein.
 */
public final class FaeSurface {
    public static final ResourceKey<MaterialRule> SURFACE = ResourceKey.create(Registries.MATERIAL_RULE, KingdomsAtWar.id("fae_realm"));

    private FaeSurface() {}

    private static MaterialRule state(Block block) {
        return MaterialRules.state(block.defaultBlockState());
    }

    public static void bootstrap(BootstrapContext<MaterialRule> context) {
        HolderGetter<MaterialRule> rules = context.lookup(Registries.MATERIAL_RULE);
        HolderGetter<MaterialCondition> conditions = context.lookup(Registries.MATERIAL_CONDITION);
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);

        MaterialRule stone = state(FaeBlocks.FAESTONE.base().get());
        MaterialRule soil = state(FaeBlocks.FAE_SOIL.get());
        MaterialRule grass = state(FaeBlocks.FAE_GRASS.get());
        MaterialRule moss = state(FaeBlocks.FAE_MOSS.get());
        MaterialRule sand = state(Blocks.SAND);
        MaterialRule clay = state(Blocks.CLAY);
        MaterialRule calcite = state(Blocks.CALCITE);

        MaterialCondition onFloor = MaterialRules.getCondition(conditions, VanillaMaterialConditions.ON_FLOOR);
        MaterialCondition underFloor = MaterialRules.getCondition(conditions, VanillaMaterialConditions.UNDER_FLOOR);
        MaterialCondition notUnderwater = MaterialRules.getCondition(conditions, VanillaMaterialConditions.NOT_UNDERWATER);
        MaterialCondition steep = MaterialRules.steep();
        // Ufer: höchstens einen Block über dem Wasserspiegel, nicht tiefer als vier darunter
        MaterialCondition aboveShore = MaterialRules.yBlockCheck(VerticalAnchor.absolute(FaeTerrain.SEA_LEVEL + 1), 0);
        MaterialCondition shoreBottom = MaterialRules.yBlockCheck(VerticalAnchor.absolute(FaeTerrain.SEA_LEVEL - 4), 0);
        MaterialCondition shore = MaterialRules.not(aboveShore);

        MaterialRule grassOrSoil = MaterialRules.sequence(MaterialRules.ifTrue(notUnderwater, grass), soil);
        // der Seegrund: Erde, darin Flecken aus Sand und Ton
        MaterialRule lakeBed = MaterialRules.sequence(
                MaterialRules.ifTrue(MaterialRules.noiseCondition2d(FaeTerrain.PATCHES, 0.45), clay),
                MaterialRules.ifTrue(MaterialRules.noiseCondition2d(FaeTerrain.SURFACE, -0.2, 0.25), sand),
                soil);
        MaterialRule beach = MaterialRules.ifTrue(shoreBottom, MaterialRules.ifTrue(shore,
                MaterialRules.sequence(MaterialRules.ifTrue(notUnderwater, sand), lakeBed)));

        MaterialRule floor = MaterialRules.sequence(
                MaterialRules.ifTrue(MaterialRules.not(notUnderwater), lakeBed),
                MaterialRules.ifTrue(steep, MaterialRules.sequence(
                        MaterialRules.ifTrue(MaterialRules.noiseCondition2d(FaeTerrain.PATCHES, 0.5), calcite), stone)),
                MaterialRules.ifTrue(MaterialRules.isBiome(biomes, FaeBiomes.MOONLIT_LAKES), MaterialRules.sequence(
                        MaterialRules.ifTrue(MaterialRules.noiseCondition2d(FaeTerrain.SURFACE, 0.0, 0.6), beach), grassOrSoil)),
                MaterialRules.ifTrue(MaterialRules.isBiome(biomes, FaeBiomes.MUSHROOM_GROVE), MaterialRules.sequence(
                        MaterialRules.ifTrue(MaterialRules.noiseCondition2d(FaeTerrain.SURFACE, -0.35), moss), grassOrSoil)),
                MaterialRules.ifTrue(MaterialRules.isBiome(biomes, FaeBiomes.GLIMMERWOOD), MaterialRules.sequence(
                        MaterialRules.ifTrue(MaterialRules.noiseCondition2d(FaeTerrain.PATCHES, 0.55), moss), grassOrSoil)),
                MaterialRules.ifTrue(MaterialRules.isBiome(biomes, FaeBiomes.THORNWILD), MaterialRules.sequence(
                        MaterialRules.ifTrue(MaterialRules.noiseCondition2d(FaeTerrain.PATCHES, 0.25), soil), grassOrSoil)),
                grassOrSoil);

        MaterialRule under = MaterialRules.sequence(
                MaterialRules.ifTrue(steep, stone),
                soil);

        context.register(SURFACE, MaterialRules.sequence(
                MaterialRules.getRule(rules, VanillaMaterialRules.BEDROCK_FLOOR),
                MaterialRules.ifTrue(onFloor, floor),
                MaterialRules.ifTrue(underFloor, under)));
    }
}
