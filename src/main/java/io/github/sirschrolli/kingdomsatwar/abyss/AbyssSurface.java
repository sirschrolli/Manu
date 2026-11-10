package io.github.sirschrolli.kingdomsatwar.abyss;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.AbyssBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks;
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
import net.minecraft.world.level.levelgen.placement.CaveSurface;

/**
 * Die Oberflächen des Abgrunds: Jedes Bruchstück trägt die Haut der Welt, aus der es gerissen wurde – Gras auf Erde auf
 * Stein in den Bruchlanden, Netherrack mit Karmesinnyzel auf Schwarzstein im Glutsturz, Endstein auf Sternenmarmor in den
 * Sternentrümmern, Schattengras auf Schattenerde auf Schattenstein im Schattenschlund, in der Naht alles durcheinander.
 * Erst tiefer im Inneren kommt das Chaosgestein zum Vorschein; an den Unterseiten glühen da und dort Weltennähte.
 */
public final class AbyssSurface {
    public static final ResourceKey<MaterialRule> SURFACE = ResourceKey.create(Registries.MATERIAL_RULE, KingdomsAtWar.id("the_abyss"));

    private AbyssSurface() {}

    private static MaterialRule state(Block block) {
        return MaterialRules.state(block.defaultBlockState());
    }

    public static void bootstrap(BootstrapContext<MaterialRule> context) {
        HolderGetter<MaterialCondition> conditions = context.lookup(Registries.MATERIAL_CONDITION);
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);

        MaterialRule chaos = state(AbyssBlocks.CHAOS_STONE.base().get());
        MaterialRule seam = state(AbyssBlocks.WORLD_SEAM.get());
        MaterialRule grass = state(Blocks.GRASS_BLOCK);
        MaterialRule dirt = state(Blocks.DIRT);
        MaterialRule coarse = state(Blocks.COARSE_DIRT);
        MaterialRule stone = state(Blocks.STONE);
        MaterialRule andesite = state(Blocks.ANDESITE);
        MaterialRule netherrack = state(Blocks.NETHERRACK);
        MaterialRule nylium = state(Blocks.CRIMSON_NYLIUM);
        MaterialRule blackstone = state(Blocks.BLACKSTONE);
        MaterialRule basalt = state(Blocks.BASALT);
        MaterialRule magma = state(Blocks.MAGMA_BLOCK);
        MaterialRule endStone = state(Blocks.END_STONE);
        MaterialRule marble = state(VoidBlocks.ASTRAL_MARBLE.base().get());
        MaterialRule voidStone = state(VoidBlocks.VOID_STONE.base().get());
        MaterialRule shadowGrass = state(ShadowBlocks.SHADOW_GRASS.get());
        MaterialRule shadowSoil = state(ShadowBlocks.SHADOW_SOIL.get());
        MaterialRule shadowStone = state(ShadowBlocks.SHADOW_STONE.base().get());
        MaterialRule sand = state(Blocks.SAND);

        MaterialCondition onFloor = MaterialRules.getCondition(conditions, VanillaMaterialConditions.ON_FLOOR);
        MaterialCondition underFloor = MaterialRules.getCondition(conditions, VanillaMaterialConditions.UNDER_FLOOR);
        MaterialCondition onCeiling = MaterialRules.getCondition(conditions, VanillaMaterialConditions.ON_CEILING);
        MaterialCondition deep = MaterialRules.stoneDepthCheck(5, true, 0, CaveSurface.FLOOR);
        MaterialCondition steep = MaterialRules.steep();
        MaterialCondition patchA = MaterialRules.noiseCondition2d(AbyssTerrain.SURFACE, 0.35);
        MaterialCondition patchB = MaterialRules.noiseCondition2d(AbyssTerrain.SURFACE, -1.0e9, -0.45);
        MaterialCondition glowing = MaterialRules.noiseCondition3d(AbyssTerrain.SURFACE, 0.62);

        MaterialCondition lands = MaterialRules.isBiome(biomes, AbyssBiomes.FRACTURED_LANDS);
        MaterialCondition ember = MaterialRules.isBiome(biomes, AbyssBiomes.EMBER_FALLS);
        MaterialCondition star = MaterialRules.isBiome(biomes, AbyssBiomes.STAR_WRECK);
        MaterialCondition shadow = MaterialRules.isBiome(biomes, AbyssBiomes.SHADOW_CHASM);

        MaterialRule floor = MaterialRules.sequence(
                MaterialRules.ifTrue(lands, MaterialRules.sequence(MaterialRules.ifTrue(steep, stone), MaterialRules.ifTrue(patchB, coarse), grass)),
                MaterialRules.ifTrue(ember, MaterialRules.sequence(MaterialRules.ifTrue(steep, basalt), MaterialRules.ifTrue(patchA, nylium),
                        netherrack)),
                MaterialRules.ifTrue(star, MaterialRules.sequence(MaterialRules.ifTrue(patchA, marble), endStone)),
                MaterialRules.ifTrue(shadow, MaterialRules.sequence(MaterialRules.ifTrue(steep, shadowStone), shadowGrass)),
                // die Naht: Flecken aller Welten auf dem Chaosgestein, dazwischen glühende Fugen
                MaterialRules.ifTrue(patchA, grass),
                MaterialRules.ifTrue(patchB, netherrack),
                MaterialRules.ifTrue(MaterialRules.noiseCondition2d(AbyssTerrain.SURFACE, 0.12, 0.18), seam),
                MaterialRules.ifTrue(MaterialRules.noiseCondition2d(AbyssTerrain.SURFACE, -0.2, -0.05), endStone),
                chaos);

        MaterialRule under = MaterialRules.sequence(
                MaterialRules.ifTrue(lands, MaterialRules.sequence(MaterialRules.ifTrue(patchB, coarse), dirt)),
                MaterialRules.ifTrue(ember, netherrack),
                MaterialRules.ifTrue(star, endStone),
                MaterialRules.ifTrue(shadow, shadowSoil),
                MaterialRules.ifTrue(patchA, dirt),
                MaterialRules.ifTrue(patchB, netherrack),
                chaos);

        MaterialRule inner = MaterialRules.sequence(
                MaterialRules.ifTrue(lands, MaterialRules.sequence(MaterialRules.ifTrue(patchA, andesite), stone)),
                MaterialRules.ifTrue(ember, blackstone),
                MaterialRules.ifTrue(star, MaterialRules.sequence(MaterialRules.ifTrue(patchB, voidStone), endStone)),
                MaterialRules.ifTrue(shadow, shadowStone),
                MaterialRules.ifTrue(patchB, sand));

        MaterialRule ceiling = MaterialRules.sequence(
                MaterialRules.ifTrue(glowing, seam),
                MaterialRules.ifTrue(ember, MaterialRules.ifTrue(patchA, magma)),
                MaterialRules.ifTrue(star, voidStone),
                chaos);

        context.register(SURFACE, MaterialRules.sequence(
                MaterialRules.ifTrue(onFloor, floor),
                MaterialRules.ifTrue(underFloor, under),
                MaterialRules.ifTrue(deep, inner),
                MaterialRules.ifTrue(onCeiling, ceiling)));
    }
}
