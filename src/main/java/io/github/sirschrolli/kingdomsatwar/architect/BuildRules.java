package io.github.sirschrolli.kingdomsatwar.architect;

import io.github.sirschrolli.kingdomsatwar.economy.town.Material;
import io.github.sirschrolli.kingdomsatwar.registry.ModTags;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.common.Tags;
import org.jspecify.annotations.Nullable;

/**
 * Die Regeln des Dragon Architect: was beim Vermessen eines Gebäudes in den Bauplan kommt und was der Baumeister dafür
 * braucht.
 *
 * <ul>
 * <li><b>Baustoff</b> – fast alles: eine Einheit Holz, Stein oder Metall je Block (siehe {@link Material}).</li>
 * <li><b>Umsonst</b> – Erde, Pflanzen, Wasser und Ähnliches.</li>
 * <li><b>Kostbar</b> – Erze, Edelstein- und Metallblöcke, Obsidian, Ambosse, Leuchtfeuer, Köpfe …: der Baumeister macht
 * sie nicht aus Einheiten, sondern nimmt genau diesen Block aus dem Lager (sonst ließe sich aus Eisen ein Diamantblock
 * zaubern).</li>
 * <li><b>Nicht übernommen</b> – Spawner, Portale, Grundgestein, Befehlsblöcke, das Stadtbanner, fließendes Wasser und
 * Lava: an ihrer Stelle bleibt Luft.</li>
 * </ul>
 *
 * <p>Außerdem werden Zustände bereinigt, die sich sonst ausnutzen ließen (reife Feldfrüchte, volle Bienenstöcke, geladene
 * Seelenanker, Lava im Kessel …), und Laub wird dauerhaft, damit es im Garten des Nachbaus nicht verwelkt.
 */
public final class BuildRules {
    private BuildRules() {}

    /** Wie ein Block im Bauplan zählt. */
    public enum Kind {
        /** Kostet nichts (Luft, Erde, Pflanzen, Wasser). */
        FREE,
        /** Kostet eine Einheit Baustoff. */
        MATERIAL,
        /** Der Baumeister braucht genau diesen Gegenstand aus dem Lager. */
        PRECIOUS,
        /** Wird nicht übernommen. */
        SKIP
    }

    /** Wie dieser (schon bereinigte) Block im Bauplan zählt. */
    public static Kind kind(BlockState state) {
        if (state.isAir()) {
            return Kind.FREE;
        }
        if (skipped(state)) {
            return Kind.SKIP;
        }
        if (precious(state)) {
            return preciousItem(state) == Items.AIR ? Kind.SKIP : Kind.PRECIOUS;
        }
        return Material.of(state) == null ? Kind.FREE : Kind.MATERIAL;
    }

    /** Was ein Baustoff-Block kostet ({@code null}: nichts oder kein Baustoff). */
    public static @Nullable Material material(BlockState state) {
        return kind(state) == Kind.MATERIAL ? Material.of(state) : null;
    }

    /** Der Gegenstand, den ein kostbarer Block aus dem Lager braucht ({@code null}, wenn er nicht kostbar ist). */
    public static @Nullable Item precious(BlockState state, boolean check) {
        if (check && kind(state) != Kind.PRECIOUS) {
            return null;
        }
        Item item = preciousItem(state);
        return item == Items.AIR ? null : item;
    }

    private static Item preciousItem(BlockState state) {
        if (state.getBlock() instanceof net.minecraft.world.level.block.AbstractCauldronBlock) {
            return Items.CAULDRON; // auch der Wasserkessel ist ein Kessel
        }
        return state.getBlock().asItem();
    }

    private static boolean skipped(BlockState state) {
        if (state.is(ModTags.Blocks.ARCHITECT_UNBUILDABLE)) {
            return true;
        }
        Block block = state.getBlock();
        if (block instanceof LiquidBlock) {
            // nur stehendes Wasser kommt mit; fließendes entsteht von selbst, Lava gar nicht
            return !state.getFluidState().is(FluidTags.WATER) || !state.getFluidState().isSource();
        }
        return block == Blocks.PISTON_HEAD || block == Blocks.MOVING_PISTON || block == Blocks.FIRE || block == Blocks.SOUL_FIRE
                || block == Blocks.BUBBLE_COLUMN || block == Blocks.NETHER_PORTAL || block == Blocks.FROSTED_ICE;
    }

    private static boolean precious(BlockState state) {
        if (state.is(Tags.Blocks.STORAGE_BLOCKS_WHEAT) || state.is(Tags.Blocks.STORAGE_BLOCKS_DRIED_KELP)
                || state.is(Tags.Blocks.STORAGE_BLOCKS_BONE_MEAL)) {
            return false; // Heuballen, Kelp- und Knochenblöcke sind billig
        }
        if (state.is(ModTags.Blocks.ARCHITECT_PRECIOUS) || state.is(Tags.Blocks.ORES) || state.is(Tags.Blocks.STORAGE_BLOCKS)) {
            return true;
        }
        Item item = state.getBlock().asItem();
        return item != Items.AIR && item.components().getOrDefault(DataComponents.RARITY, Rarity.COMMON) != Rarity.COMMON;
    }

    /**
     * Bereinigt einen Block für den Bauplan: Kolben eingefahren, Feldfrüchte und Beerensträucher frisch gesetzt, Bienenstöcke
     * und Komposter leer, Seelenanker ungeladen, Kessel ohne Lava oder Pulverschnee, Kreischer ohne Ruf, Öfen kalt, Laub
     * dauerhaft.
     */
    public static BlockState normalize(BlockState state) {
        Block block = state.getBlock();
        if (block == Blocks.LAVA_CAULDRON || block == Blocks.POWDER_SNOW_CAULDRON) {
            return Blocks.CAULDRON.defaultBlockState();
        }
        BlockState out = state;
        for (Property<?> property : state.getProperties()) {
            String name = property.getName();
            boolean reset = name.equals("age") || name.equals("honey_level") || name.equals("charges")
                    || name.equals("level") && block == Blocks.COMPOSTER;
            if (reset && property instanceof IntegerProperty ints) {
                out = out.setValue(ints, ints.getPossibleValues().getFirst());
            }
        }
        out = setIfPresent(out, BlockStateProperties.EXTENDED, false);
        out = setIfPresent(out, BlockStateProperties.BERRIES, false);
        out = setIfPresent(out, BlockStateProperties.CAN_SUMMON, false);
        out = setIfPresent(out, BlockStateProperties.POWERED, false);
        out = setIfPresent(out, BlockStateProperties.TRIGGERED, false);
        if (block instanceof net.minecraft.world.level.block.AbstractFurnaceBlock) {
            out = setIfPresent(out, BlockStateProperties.LIT, false);
        }
        if (block instanceof net.minecraft.world.level.block.LeavesBlock) {
            out = setIfPresent(out, BlockStateProperties.PERSISTENT, true);
        }
        return out;
    }

    private static <T extends Comparable<T>> BlockState setIfPresent(BlockState state, Property<T> property, T value) {
        return state.hasProperty(property) ? state.setValue(property, value) : state;
    }

    /** Werden die Daten dieses Blockobjekts mit in den Bauplan übernommen? (Schilder und Banner: ja; Behälter nie.) */
    public static boolean keepsData(BlockEntity blockEntity) {
        BlockState state = blockEntity.getBlockState();
        return blockEntity instanceof SignBlockEntity || blockEntity instanceof BannerBlockEntity
                || state.getBlock() instanceof SignBlock || state.getBlock() instanceof BannerBlock || state.getBlock() instanceof WallBannerBlock;
    }

    /**
     * Entfernt aus gespeicherten Schild- und Bannerdaten alles, was beim Anklicken etwas auslöst (Befehle, Links): ein
     * Nachbau soll nur Text und Muster tragen, nie die Befehlsschilder eines Admins vervielfältigen.
     */
    public static net.minecraft.nbt.CompoundTag harmless(net.minecraft.nbt.CompoundTag tag) {
        strip(tag);
        return tag;
    }

    private static void strip(net.minecraft.nbt.Tag tag) {
        if (tag instanceof net.minecraft.nbt.CompoundTag compound) {
            compound.remove("click_event");
            compound.remove("clickEvent");
            for (String key : java.util.List.copyOf(compound.keySet())) {
                net.minecraft.nbt.Tag child = compound.get(key);
                if (child != null) {
                    strip(child);
                }
            }
        } else if (tag instanceof net.minecraft.nbt.ListTag list) {
            for (net.minecraft.nbt.Tag child : list) {
                strip(child);
            }
        }
    }

    /** Gewachsener Boden (für die Bodenlinie): Erde, Gras, Fels, Sand, Kies, Schnee, Wasser … */
    public static boolean terrain(BlockState state) {
        return state.is(ModTags.Blocks.ARCHITECT_TERRAIN) || state.getBlock() instanceof LiquidBlock;
    }
}
