package io.github.sirschrolli.kingdomsatwar.datagen;

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
    }
}
