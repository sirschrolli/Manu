package io.github.sirschrolli.kingdomsatwar.registry;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.neoforged.neoforge.registries.DeferredBlock;
import org.jspecify.annotations.Nullable;

/**
 * Ein Baustoff aus dem "modularen Blocksystem" (Bild "Kingdoms at War – Neue Blöcke", Abschnitt 12):
 * Vollblock, Treppe, Stufe und optional Mauer.
 */
public record BuildingSet(
        String name,
        DeferredBlock<Block> base,
        DeferredBlock<StairBlock> stairs,
        DeferredBlock<SlabBlock> slab,
        @Nullable DeferredBlock<WallBlock> wall,
        Tool tool) {

    /** Mit welchem Werkzeug der Baustoff abgebaut wird. */
    public enum Tool {
        PICKAXE,
        AXE
    }

    /** Alle Blöcke dieses Sets (Vollblock, Treppe, Stufe, Mauer). */
    public List<DeferredBlock<? extends Block>> all() {
        List<DeferredBlock<? extends Block>> list = new ArrayList<>(List.of(base, stairs, slab));
        if (wall != null) {
            list.add(wall);
        }
        return list;
    }
}
