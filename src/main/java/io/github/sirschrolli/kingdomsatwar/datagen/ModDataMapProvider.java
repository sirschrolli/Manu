package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks;
import net.neoforged.neoforge.registries.datamaps.builtin.Transformable;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FireBlock;
import net.neoforged.neoforge.common.data.DataMapProvider;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.datamaps.builtin.Flammable;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;

/**
 * Data Maps von NeoForge. Bisher nur: welche Blöcke Feuer fangen und wie schnell sie abbrennen
 * (Datei {@code data/neoforge/data_maps/block/flammable.json}).
 */
public class ModDataMapProvider extends DataMapProvider {
    /** Wie Holzbretter: fängt schwer Feuer, brennt dann aber recht schnell ab. */
    private static final Flammable LIKE_PLANKS = new Flammable(FireBlock.IGNITE_HARD, FireBlock.BURN_MEDIUM);
    /** Wie Baumstämme: fängt schwer Feuer und brennt nur langsam ab. */
    private static final Flammable LIKE_LOGS = new Flammable(FireBlock.IGNITE_HARD, FireBlock.BURN_HARD);

    public ModDataMapProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider);
    }

    @Override
    protected void gather(HolderLookup.Provider provider) {
        var flammable = builder(NeoForgeDataMaps.FLAMMABLE);
        for (DeferredBlock<? extends Block> block : ModBlocks.woodenBlocks()) {
            flammable.add(block.key(), block == ModBlocks.TIMBER_BEAM ? LIKE_LOGS : LIKE_PLANKS, false);
        }
        // Uralter Hain: Urholz wie Stämme, Laub und Gestrüpp wie Laub
        flammable.add(ModBlocks.ANCIENT_LOG.key(), LIKE_LOGS, false);
        flammable.add(ModBlocks.GNARLED_ROOTS.key(), LIKE_LOGS, false);
        flammable.add(ModBlocks.ANCIENT_LEAVES.key(), new Flammable(FireBlock.IGNITE_EASY, FireBlock.BURN_EASY), false);
        flammable.add(ModBlocks.BRAMBLE.key(), new Flammable(FireBlock.IGNITE_INSTANT, FireBlock.BURN_INSTANT), false);
        // Schattenwelt: Holz und Laub brennen wie gewöhnliches
        for (var wood : java.util.List.of(ShadowBlocks.SHADOW_LOG, ShadowBlocks.SHADOW_WOOD, ShadowBlocks.STRIPPED_SHADOW_LOG,
                ShadowBlocks.STRIPPED_SHADOW_WOOD)) {
            flammable.add(wood.key(), LIKE_LOGS, false);
        }
        // (die Bretter samt Treppe und Stufe stecken schon in den Holz-Sets oben)
        for (var wood : java.util.List.of(ShadowBlocks.SHADOW_FENCE, ShadowBlocks.SHADOW_FENCE_GATE)) {
            flammable.add(wood.key(), LIKE_PLANKS, false);
        }
        flammable.add(ShadowBlocks.SHADOW_LEAVES.key(), new Flammable(FireBlock.IGNITE_EASY, FireBlock.BURN_EASY), false);

        // Werkzeuge: Axt entrindet Schattenholz, Hacke macht Ackerland, Schaufel einen Pfad
        var transform = builder(NeoForgeDataMaps.TRANSFORMABLES);
        transform.add(ShadowBlocks.SHADOW_LOG.key(), Transformable.stripping(ShadowBlocks.SHADOW_LOG.get(),
                ShadowBlocks.STRIPPED_SHADOW_LOG.get()), false);
        transform.add(ShadowBlocks.SHADOW_WOOD.key(), Transformable.stripping(ShadowBlocks.SHADOW_WOOD.get(),
                ShadowBlocks.STRIPPED_SHADOW_WOOD.get()), false);
        for (var soil : java.util.List.of(ShadowBlocks.SHADOW_SOIL, ShadowBlocks.SHADOW_GRASS)) {
            Block block = soil.get();
            transform.add(soil.key(), new Transformable(java.util.Map.of(
                    net.minecraft.world.item.component.BlockTransformers.HOE,
                    Transformable.tilling(block, ShadowBlocks.SHADOW_FARMLAND.get()).transformers()
                            .get(net.minecraft.world.item.component.BlockTransformers.HOE),
                    net.minecraft.world.item.component.BlockTransformers.SHOVEL,
                    Transformable.flattening(block, ShadowBlocks.SHADOW_PATH.get()).transformers()
                            .get(net.minecraft.world.item.component.BlockTransformers.SHOVEL))), false);
        }
    }
}
