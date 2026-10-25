package io.github.sirschrolli.kingdomsatwar.dragon;

import java.util.function.Supplier;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.MapColor;

/**
 * Die sechs Drachenarten aus dem Konzept "Dragon Warfare".
 *
 * <p>Jede Art hat eine eigene Farbe, eigene Eier und Schuppen. Die Drachen selbst
 * (Wachstum in fünf Stufen, Zähmen, Reiten, Fliegen) folgen in Phase 4.
 *
 * <p>Der {@code nestMaterial} ist der Rohstoff, aus dem laut Konzept die Nester der Art bestehen.
 * Er wird im Rezept für das jeweilige Drachenei verwendet.
 */
public enum DragonType {
    FIRE("fire", MapColor.COLOR_RED, () -> Items.MAGMA_BLOCK),
    ICE("ice", MapColor.ICE, () -> Items.BLUE_ICE),
    LIGHTNING("lightning", MapColor.COLOR_YELLOW, () -> Blocks.LIGHTNING_ROD.weathering().unaffected()),
    NATURE("nature", MapColor.PLANT, () -> Items.MOSS_BLOCK),
    EARTH("earth", MapColor.DIRT, () -> Items.RAW_IRON_BLOCK),
    WATER("water", MapColor.WATER, () -> Items.PRISMARINE);

    private final String id;
    private final MapColor mapColor;
    private final Supplier<ItemLike> nestMaterial;

    DragonType(String id, MapColor mapColor, Supplier<ItemLike> nestMaterial) {
        this.id = id;
        this.mapColor = mapColor;
        this.nestMaterial = nestMaterial;
    }

    /** Kurzname für Registrierungsnamen, z. B. {@code fire} → {@code fire_dragon_egg}. */
    public String id() {
        return id;
    }

    public MapColor mapColor() {
        return mapColor;
    }

    /** Rohstoff der Nester dieser Art (für das Ei-Rezept). */
    public ItemLike nestMaterial() {
        return nestMaterial.get();
    }
}
