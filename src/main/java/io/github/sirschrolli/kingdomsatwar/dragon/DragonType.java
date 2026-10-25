package io.github.sirschrolli.kingdomsatwar.dragon;

import java.util.function.Supplier;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.MapColor;

/**
 * Die sechs Drachenarten aus dem Konzept "Dragon Warfare".
 *
 * <p>Jede Art hat eine eigene Farbe, eigene Eier, Schuppen, einen eigenen Odem-Angriff
 * und eine eigene Bedingung zum Ausbrüten der Eier.
 *
 * <p>Der {@code nestMaterial} ist der Rohstoff, aus dem laut Konzept die Nester der Art bestehen.
 * Er wird im Rezept für das jeweilige Drachenei verwendet.
 */
public enum DragonType {
    FIRE("fire", MapColor.COLOR_RED, () -> Items.MAGMA_BLOCK, () -> ParticleTypes.FLAME, 0xFF6A1A),
    ICE("ice", MapColor.ICE, () -> Items.BLUE_ICE, () -> ParticleTypes.SNOWFLAKE, 0x9FE8FF),
    LIGHTNING("lightning", MapColor.COLOR_YELLOW, () -> Blocks.LIGHTNING_ROD.weathering().unaffected(),
            () -> ParticleTypes.ELECTRIC_SPARK, 0xFFE94A),
    NATURE("nature", MapColor.PLANT, () -> Items.MOSS_BLOCK, () -> ParticleTypes.SPORE_BLOSSOM_AIR, 0x7FE05A),
    EARTH("earth", MapColor.DIRT, () -> Items.RAW_IRON_BLOCK, () -> ParticleTypes.DUST_PLUME, 0xC8A070),
    WATER("water", MapColor.WATER, () -> Items.PRISMARINE, () -> ParticleTypes.SPLASH, 0x4FD0F0);

    private final String id;
    private final MapColor mapColor;
    private final Supplier<ItemLike> nestMaterial;
    private final Supplier<ParticleOptions> particle;
    private final int color;

    DragonType(String id, MapColor mapColor, Supplier<ItemLike> nestMaterial, Supplier<ParticleOptions> particle, int color) {
        this.id = id;
        this.mapColor = mapColor;
        this.nestMaterial = nestMaterial;
        this.particle = particle;
        this.color = color;
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

    /** Partikel für Odem-Angriff und brütende Eier. */
    public ParticleOptions particle() {
        return particle.get();
    }

    /** Leitfarbe der Art (RGB), z. B. für Partikel und Text. */
    public int color() {
        return color;
    }
}
