package io.github.sirschrolli.kingdomsatwar.economy.town;

import com.mojang.serialization.Codec;
import io.github.sirschrolli.kingdomsatwar.block.FeastBlock;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import java.util.Locale;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.Tags;
import org.jspecify.annotations.Nullable;

/**
 * Die Baustoffe der Siedlung. Jeder Block eines Bauplans kostet eine Einheit von genau einem Baustoff; Erde, Pflanzen,
 * Feldfrüchte, Wasser und Speisen kosten nichts. Die Einheiten kommen aus Gegenständen im Lager: ein Brett ist eine
 * Einheit Holz, ein Stamm vier; ein Bruchstein (oder Stein, Sand, Kies, Ton, Ziegel) eine Einheit Stein; ein Eisen- oder
 * Kupferbarren (auch roh) eine Einheit Metall, ein Stahlbarren zwei.
 */
public enum Material implements StringRepresentable {
    WOOD,
    STONE,
    METAL;

    public static final Codec<Material> CODEC = StringRepresentable.fromEnum(Material::values);

    public String id() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    @Override
    public String getSerializedName() {
        return this.id();
    }

    public String translationKey() {
        return "material.kingdomsatwar." + this.id();
    }

    /** Wofür der Baustoff im Stadtfenster steht. */
    public ItemStack icon() {
        return new ItemStack(switch (this) {
            case WOOD -> Items.OAK_LOG;
            case STONE -> Items.COBBLESTONE;
            case METAL -> Items.IRON_INGOT;
        });
    }

    /** So viele Einheiten dieses Baustoffs ist ein Stück des Gegenstands wert (0 = gar keine). */
    public int units(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        return switch (this) {
            case WOOD -> stack.is(ItemTags.LOGS) ? 4 : stack.is(ItemTags.PLANKS) ? 1 : 0;
            case STONE -> stack.is(Tags.Items.COBBLESTONES) || stack.is(Tags.Items.STONES) || stack.is(ItemTags.STONE_CRAFTING_MATERIALS)
                    || stack.is(ItemTags.SAND) || stack.is(Items.GRAVEL) || stack.is(Items.CLAY_BALL) || stack.is(Items.BRICK) ? 1 : 0;
            case METAL -> stack.is(ModItems.STEEL_INGOT.get()) ? 2
                    : stack.is(Items.IRON_INGOT) || stack.is(Items.RAW_IRON) || stack.is(Items.COPPER_INGOT) || stack.is(Items.RAW_COPPER) ? 1 : 0;
        };
    }

    /** Welcher Baustoff in einem Gegenstand steckt (oder {@code null}). */
    public static @Nullable Material of(ItemStack stack) {
        for (Material m : values()) {
            if (m.units(stack) > 0) {
                return m;
            }
        }
        return null;
    }

    /** Was ein Block des Bauplans kostet: {@code null}, wenn er nichts kostet (Luft, Erde, Pflanzen, Wasser, Speisen). */
    public static @Nullable Material of(BlockState state) {
        if (state.isAir()) {
            return null;
        }
        Block block = state.getBlock();
        if (block instanceof LiquidBlock || block instanceof VegetationBlock || block instanceof FarmlandBlock || block instanceof FeastBlock
                || state.is(BlockTags.DIRT) || state.is(BlockTags.LEAVES) || block == ModBlocks.BUILDER_SCAFFOLD.get()) {
            return null;
        }
        SoundType sound = state.getSoundType();
        if (sound == SoundType.METAL || sound == SoundType.LANTERN || sound == SoundType.CHAIN || sound == SoundType.ANVIL
                || sound == SoundType.IRON || sound == SoundType.COPPER || sound == SoundType.COPPER_BULB || sound == SoundType.COPPER_GRATE
                || sound == SoundType.NETHERITE_BLOCK || sound == SoundType.HEAVY_CORE) {
            return METAL;
        }
        if (sound == SoundType.WOOD || sound == SoundType.NETHER_WOOD || sound == SoundType.BAMBOO_WOOD || sound == SoundType.CHERRY_WOOD
                || sound == SoundType.LADDER || sound == SoundType.SCAFFOLDING || sound == SoundType.WOOL || sound == SoundType.BAMBOO
                || sound == SoundType.CHISELED_BOOKSHELF || sound == SoundType.SHELF || sound == SoundType.CANDLE
                || sound == SoundType.HANGING_SIGN || sound == SoundType.NETHER_WOOD_HANGING_SIGN || sound == SoundType.BAMBOO_WOOD_HANGING_SIGN
                || sound == SoundType.CHERRY_WOOD_HANGING_SIGN || block instanceof BedBlock || block instanceof BannerBlock) {
            return WOOD;
        }
        if (sound == SoundType.GRASS || sound == SoundType.CROP || sound == SoundType.HARD_CROP || sound == SoundType.SNOW
                || sound == SoundType.MOSS || sound == SoundType.MOSS_CARPET) {
            return null;
        }
        return STONE;
    }
}
