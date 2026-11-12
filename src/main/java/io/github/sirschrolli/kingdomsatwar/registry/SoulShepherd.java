package io.github.sirschrolli.kingdomsatwar.registry;

import io.github.sirschrolli.kingdomsatwar.block.shadow.SoulBellBlock;
import io.github.sirschrolli.kingdomsatwar.block.shadow.SoulBellBlockEntity;
import io.github.sirschrolli.kingdomsatwar.item.shadow.ShepherdCrookItem;
import java.util.function.UnaryOperator;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * Der Seelenhirte und sein Pferch (seit 0.98): die Seelenglocke, unter der er in Andacht steht, sein Hirtenstab (seine
 * Beute) und sein Spawn-Ei. Der Hirte selbst und seine Irrlichter stehen in {@link ModEntities}.
 */
public final class SoulShepherd {
    private SoulShepherd() {}

    /** Lädt die Klasse, damit sich alles anmeldet, bevor die Register gefüllt werden. */
    public static void init() {}

    /** Die Seelenglocke: unzerstörbar; in ihr glimmt Seelenfeuer, solange der Hirte lebt. */
    public static final DeferredBlock<SoulBellBlock> SOUL_BELL = ModBlocks.blockWithItem("soul_bell", SoulBellBlock::new,
            p -> p.mapColor(MapColor.COLOR_CYAN).strength(-1.0f, 3_600_000.0f).noLootTable().sound(SoundType.ANVIL).noOcclusion()
                    .lightLevel(s -> s.getValue(SoulBellBlock.PHASE).light).pushReaction(PushReaction.IMMOVEABLE)
                    .isValidSpawn((state, level, pos, type) -> false),
            UnaryOperator.identity(), ModBlocks.SHADOW_TAB);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SoulBellBlockEntity>> SOUL_BELL_ENTITY =
            ModBlockEntities.BLOCK_ENTITIES.register("soul_bell", () -> new BlockEntityType<>(SoulBellBlockEntity::new, SOUL_BELL.get()));

    /** Sumpfeiche mit eisernem Haken: zäh, gut zu verzaubern; repariert mit Dämmerquarz. */
    public static final ToolMaterial CROOK_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 900, 6.0f, 3.0f, 18,
            ModTags.Items.REPAIRS_SHEPHERD_CROOK);
    /** Der Hirtenstab: die Beute des Seelenhirten (Rechtsklick: Hakenzug). */
    public static final DeferredItem<ShepherdCrookItem> CROOK = ModItems.ITEMS.registerItem("shepherd_crook", ShepherdCrookItem::new,
            () -> new Item.Properties().sword(CROOK_MATERIAL, 4.0f, -2.8f).rarity(Rarity.EPIC)
                    .component(DataComponents.LORE, ModItems.lore("shepherd_crook", 3)));
    public static final DeferredItem<SpawnEggItem> SOUL_SHEPHERD_SPAWN_EGG = ModItems.ITEMS.registerItem("soul_shepherd_spawn_egg",
            SpawnEggItem::new, p -> p.spawnEgg(ModEntities.SOUL_SHEPHERD.get()));

    static {
        ModItems.EQUIPMENT_TAB.add(CROOK);
        ModBlocks.SHADOW_TAB.add(CROOK);
        ModBlocks.SHADOW_TAB.add(SOUL_SHEPHERD_SPAWN_EGG);
    }
}
