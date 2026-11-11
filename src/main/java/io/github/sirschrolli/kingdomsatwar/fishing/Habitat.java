package io.github.sirschrolli.kingdomsatwar.fishing;

import io.github.sirschrolli.kingdomsatwar.abyss.AbyssGate;
import io.github.sirschrolli.kingdomsatwar.fae.FaeGate;
import io.github.sirschrolli.kingdomsatwar.voidrealm.VoidGate;
import io.github.sirschrolli.kingdomsatwar.world.ShadowGate;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.neoforged.neoforge.common.Tags;

/**
 * Lebensräume der Fische. Jeder Ort hat mindestens einen der allgemeinen (Süßwasser, Meer, Lava, Leere …) – darum beißt
 * überall etwas –, viele dazu einen besonderen ({@link #special}), dessen Arten es nur dort gibt.
 */
public enum Habitat {
    FRESH(false), FRESH_COLD(true), FRESH_WARM(true), SWAMP(true), MOUNTAIN(true), DRY(true), CAVE(true), SCULK(true), BLOSSOM(true), DARK(true),
    MUSHROOM(true),
    SALT(false), SALT_WARM(true), SALT_COLD(true), DEEP(true),
    LAVA(false), LAVA_CRIMSON(true), LAVA_WARPED(true), LAVA_SOUL(true), LAVA_BASALT(true),
    END(false), VOID(false), FAE(false), SHADOW(false), ABYSS(false);

    private final boolean special;

    Habitat(boolean special) {
        this.special = special;
    }

    /** Ein besonderer Ort: Seine Arten zeigen sich dort öfter als die Allerweltsfische. */
    public boolean special() {
        return this.special;
    }

    public boolean lava() {
        return this == LAVA || this == LAVA_CRIMSON || this == LAVA_WARPED || this == LAVA_SOUL || this == LAVA_BASALT;
    }

    public String key() {
        return "fishing.kingdomsatwar.habitat." + this.name().toLowerCase(java.util.Locale.ROOT);
    }

    /** Die Lebensräume an dieser Stelle – je nachdem, worin der Schwimmer treibt. */
    public static EnumSet<Habitat> at(Level level, BlockPos pos, Medium medium) {
        ResourceKey<Level> dim = level.dimension();
        Holder<Biome> biome = level.getBiome(pos);
        EnumSet<Habitat> set = EnumSet.noneOf(Habitat.class);
        if (medium == Medium.LAVA) {
            set.add(LAVA);
            if (biome.is(Biomes.CRIMSON_FOREST)) {
                set.add(LAVA_CRIMSON);
            } else if (biome.is(Biomes.WARPED_FOREST)) {
                set.add(LAVA_WARPED);
            } else if (biome.is(Biomes.SOUL_SAND_VALLEY)) {
                set.add(LAVA_SOUL);
            } else if (biome.is(Biomes.BASALT_DELTAS)) {
                set.add(LAVA_BASALT);
            }
            if (dim == AbyssGate.THE_ABYSS) {
                set.add(ABYSS);
            }
            return set;
        }
        if (medium == Medium.VOID) {
            if (dim == Level.END) {
                set.add(END);
            } else if (dim == AbyssGate.THE_ABYSS) {
                set.add(ABYSS);
                set.add(VOID);
            } else {
                set.add(VOID);
            }
            return set;
        }
        if (dim == FaeGate.FAE_REALM) {
            return EnumSet.of(FAE);
        }
        if (dim == ShadowGate.SHADOW_WORLD) {
            return EnumSet.of(SHADOW);
        }
        if (dim == AbyssGate.THE_ABYSS) {
            return EnumSet.of(ABYSS);
        }
        if (dim == VoidGate.THE_VOID) {
            return EnumSet.of(VOID);
        }
        if (dim == Level.END) {
            return EnumSet.of(END);
        }
        boolean cold = biome.is(Tags.Biomes.IS_COLD_OVERWORLD) || biome.is(Tags.Biomes.IS_SNOWY);
        if (biome.is(BiomeTags.IS_OCEAN) || biome.is(BiomeTags.IS_DEEP_OCEAN) || biome.is(BiomeTags.IS_BEACH)) {
            set.add(SALT);
            if (biome.is(BiomeTags.IS_DEEP_OCEAN)) {
                set.add(DEEP);
            }
            if (biome.is(Biomes.WARM_OCEAN) || biome.is(Biomes.LUKEWARM_OCEAN) || biome.is(Biomes.DEEP_LUKEWARM_OCEAN)
                    || biome.is(Tags.Biomes.IS_HOT_OVERWORLD)) {
                set.add(SALT_WARM);
            } else if (cold) {
                set.add(SALT_COLD);
            }
            return set;
        }
        set.add(FRESH);
        boolean deepDark = biome.is(Biomes.DEEP_DARK);
        if (deepDark) {
            set.add(SCULK);
        }
        if (deepDark || biome.is(Biomes.LUSH_CAVES) || biome.is(Biomes.DRIPSTONE_CAVES) || biome.is(Biomes.SULFUR_CAVES)
                || (pos.getY() < level.getSeaLevel() - 12 && !level.canSeeSky(pos.above()))) {
            set.add(CAVE);
        }
        if (biome.is(Tags.Biomes.IS_SWAMP)) {
            set.add(SWAMP);
        }
        if (biome.is(BiomeTags.IS_JUNGLE)) {
            set.add(FRESH_WARM);
        }
        if (cold) {
            set.add(FRESH_COLD);
        }
        if (biome.is(BiomeTags.IS_MOUNTAIN) || biome.is(BiomeTags.IS_HILL) || pos.getY() > 100) {
            set.add(MOUNTAIN);
        }
        if (biome.is(BiomeTags.IS_BADLANDS) || biome.is(BiomeTags.IS_SAVANNA) || biome.is(Biomes.DESERT)) {
            set.add(DRY);
        }
        if (biome.is(Biomes.CHERRY_GROVE) || biome.is(Biomes.MEADOW) || biome.is(Biomes.FLOWER_FOREST) || biome.is(Biomes.SUNFLOWER_PLAINS)) {
            set.add(BLOSSOM);
        }
        if (biome.is(Biomes.DARK_FOREST) || biome.is(Biomes.PALE_GARDEN)) {
            set.add(DARK);
        }
        if (biome.is(Biomes.MUSHROOM_FIELDS)) {
            set.add(MUSHROOM);
        }
        return set;
    }
}
