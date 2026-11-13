package io.github.sirschrolli.kingdomsatwar.shadow;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.material.VanillaMaterialConditions;
import net.minecraft.data.worldgen.material.VanillaMaterialRules;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.material.MaterialRules;
import net.minecraft.world.level.levelgen.material.condition.MaterialCondition;
import net.minecraft.world.level.levelgen.material.rule.MaterialRule;

/**
 * Die Oberflächen der Schattenwelt: Schattengras auf Schattenerde, fahler Sand in der Öde, an den Küsten und am
 * Meeresgrund, Aschschnee auf den hohen Gipfeln, nackter Fels an steilen Hängen, Glimmmoos-Flecken auf den Wiesen und
 * Inseln – und tief unten der Tiefschattenstein.
 */
public final class ShadowSurface {
    public static final ResourceKey<MaterialRule> SURFACE = ResourceKey.create(Registries.MATERIAL_RULE, KingdomsAtWar.id("shadow_world"));

    /** Ab dieser Höhe liegt auf den Gipfeln Aschschnee. */
    public static final int ASH_SNOW_LINE = 150;

    private ShadowSurface() {}

    private static MaterialRule state(Block block) {
        return MaterialRules.state(block.defaultBlockState());
    }

    public static void bootstrap(BootstrapContext<MaterialRule> context) {
        HolderGetter<MaterialRule> rules = context.lookup(Registries.MATERIAL_RULE);
        HolderGetter<MaterialCondition> conditions = context.lookup(Registries.MATERIAL_CONDITION);
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);

        MaterialRule stone = state(ShadowBlocks.SHADOW_STONE.base().get());
        MaterialRule deep = state(ShadowBlocks.DEEP_SHADOW_STONE.get());
        MaterialRule soil = state(ShadowBlocks.SHADOW_SOIL.get());
        MaterialRule grass = state(ShadowBlocks.SHADOW_GRASS.get());
        MaterialRule sand = state(ShadowBlocks.SHADOW_SAND.get());
        MaterialRule ashSnow = state(ShadowBlocks.ASH_SNOW_BLOCK.get());
        MaterialRule moss = state(ShadowBlocks.GLIMMER_MOSS.get());

        MaterialCondition onFloor = MaterialRules.getCondition(conditions, VanillaMaterialConditions.ON_FLOOR);
        MaterialCondition underFloor = MaterialRules.getCondition(conditions, VanillaMaterialConditions.UNDER_FLOOR);
        MaterialCondition notUnderwater = MaterialRules.getCondition(conditions, VanillaMaterialConditions.NOT_UNDERWATER);
        MaterialCondition steep = MaterialRules.steep();
        MaterialCondition snowLine = MaterialRules.yBlockCheck(VerticalAnchor.absolute(ASH_SNOW_LINE), 0);
        MaterialCondition aboveBeach = MaterialRules.yBlockCheck(VerticalAnchor.absolute(ShadowTerrain.SEA_LEVEL + 3), 0);
        MaterialCondition beachBottom = MaterialRules.yBlockCheck(VerticalAnchor.absolute(ShadowTerrain.SEA_LEVEL - 5), 0);

        MaterialRule grassOrSoil = MaterialRules.sequence(MaterialRules.ifTrue(notUnderwater, grass), soil);
        // Sandstrände knapp über dem Meeresspiegel
        MaterialRule beach = MaterialRules.ifTrue(beachBottom, MaterialRules.ifTrue(MaterialRules.not(aboveBeach), sand));

        MaterialRule floor = MaterialRules.sequence(
                MaterialRules.ifTrue(MaterialRules.isBiome(biomes, ShadowBiomes.ABYSSAL_SEA), MaterialRules.sequence(
                        MaterialRules.ifTrue(MaterialRules.noiseCondition2d(ShadowTerrain.SURFACE, -0.12, 0.12), deep), sand)),
                MaterialRules.ifTrue(MaterialRules.isBiome(biomes, ShadowBiomes.PALE_WASTES), MaterialRules.sequence(
                        MaterialRules.ifTrue(MaterialRules.noiseCondition2d(ShadowTerrain.PATCHES, 0.55), stone), sand)),
                MaterialRules.ifTrue(MaterialRules.isBiome(biomes, ShadowBiomes.SHADOW_PEAKS), MaterialRules.sequence(
                        MaterialRules.ifTrue(steep, stone),
                        MaterialRules.ifTrue(snowLine, ashSnow),
                        MaterialRules.ifTrue(MaterialRules.noiseCondition2d(ShadowTerrain.SURFACE, 0.25), stone),
                        grassOrSoil)),
                MaterialRules.ifTrue(MaterialRules.isBiome(biomes, ShadowBiomes.CRYSTAL_CHASMS), MaterialRules.sequence(
                        MaterialRules.ifTrue(steep, stone), beach, grassOrSoil)),
                MaterialRules.ifTrue(MaterialRules.isBiome(biomes, ShadowBiomes.SOUL_MARSH), MaterialRules.sequence(
                        MaterialRules.ifTrue(MaterialRules.noiseCondition2d(ShadowTerrain.PATCHES, 0.35), soil), grassOrSoil)),
                MaterialRules.ifTrue(MaterialRules.isBiome(biomes, ShadowBiomes.GLIMMER_MEADOWS), MaterialRules.sequence(
                        beach,
                        MaterialRules.ifTrue(MaterialRules.noiseCondition2d(ShadowTerrain.SURFACE, 0.62), moss), grassOrSoil)),
                MaterialRules.ifTrue(MaterialRules.isBiome(biomes, ShadowBiomes.FLOATING_ISLES), MaterialRules.sequence(
                        MaterialRules.ifTrue(MaterialRules.noiseCondition2d(ShadowTerrain.PATCHES, 0.45), moss), grassOrSoil)),
                MaterialRules.ifTrue(MaterialRules.isBiome(biomes, ShadowBiomes.SHADOW_DEPTHS), stone),
                MaterialRules.ifTrue(steep, stone),
                beach,
                grassOrSoil);

        MaterialRule under = MaterialRules.sequence(
                MaterialRules.ifTrue(MaterialRules.isBiome(biomes, ShadowBiomes.ABYSSAL_SEA, ShadowBiomes.PALE_WASTES), sand),
                MaterialRules.ifTrue(MaterialRules.isBiome(biomes, ShadowBiomes.SHADOW_PEAKS, ShadowBiomes.SHADOW_DEPTHS), stone),
                MaterialRules.ifTrue(steep, stone),
                soil);

        context.register(SURFACE, MaterialRules.sequence(
                MaterialRules.getRule(rules, VanillaMaterialRules.BEDROCK_FLOOR),
                MaterialRules.ifTrue(MaterialRules.abovePreliminarySurface(), MaterialRules.sequence(
                        MaterialRules.ifTrue(onFloor, floor),
                        MaterialRules.ifTrue(underFloor, under))),
                MaterialRules.ifTrue(MaterialRules.verticalGradient("kingdomsatwar:deep_shadow_stone", VerticalAnchor.absolute(0),
                        VerticalAnchor.absolute(8)), deep)));
    }
}
