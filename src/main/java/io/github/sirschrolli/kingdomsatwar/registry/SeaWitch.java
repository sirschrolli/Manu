package io.github.sirschrolli.kingdomsatwar.registry;

import io.github.sirschrolli.kingdomsatwar.block.sea.WitchCauldronBlock;
import io.github.sirschrolli.kingdomsatwar.block.sea.WitchCauldronBlockEntity;
import io.github.sirschrolli.kingdomsatwar.item.sea.SeaWitchStaffItem;
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
 * Die Meerhexe und ihre Grotte (seit 0.97): der Hexenkessel, über dem sie schwebt, ihr Hexenstab (ihre Beute) und ihr
 * Spawn-Ei. Die Hexe selbst und ihre Tintengeschosse stehen in {@link ModEntities}.
 */
public final class SeaWitch {
    private SeaWitch() {}

    /** Lädt die Klasse, damit sich alles anmeldet, bevor die Register gefüllt werden. */
    public static void init() {}

    /** Der Hexenkessel: unzerstörbar; er leuchtet, solange die Hexe lebt. */
    public static final DeferredBlock<WitchCauldronBlock> WITCH_CAULDRON = ModBlocks.blockWithItem("witch_cauldron", WitchCauldronBlock::new,
            p -> p.mapColor(MapColor.COLOR_BLACK).strength(-1.0f, 3_600_000.0f).noLootTable().sound(SoundType.METAL).noOcclusion()
                    .lightLevel(s -> s.getValue(WitchCauldronBlock.PHASE).light).pushReaction(PushReaction.IMMOVEABLE)
                    .isValidSpawn((state, level, pos, type) -> false),
            UnaryOperator.identity(), SeaBlocks.TAB);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WitchCauldronBlockEntity>> WITCH_CAULDRON_ENTITY =
            ModBlockEntities.BLOCK_ENTITIES.register("witch_cauldron", () -> new BlockEntityType<>(WitchCauldronBlockEntity::new,
                    WITCH_CAULDRON.get()));

    /** Treibholz mit Korallenzinken: zäh wie Diamant, gut zu verzaubern; repariert mit Aquamarin. */
    public static final ToolMaterial STAFF_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 640, 6.0f, 2.0f, 22,
            ModTags.Items.REPAIRS_SEA_WITCH_STAFF);
    /** Der Hexenstab: die Beute der Meerhexe (Rechtsklick: Tintengeschosse). */
    public static final DeferredItem<SeaWitchStaffItem> WITCH_STAFF = ModItems.ITEMS.registerItem("sea_witch_staff", SeaWitchStaffItem::new,
            () -> new Item.Properties().sword(STAFF_MATERIAL, 4.0f, -2.6f).rarity(Rarity.EPIC)
                    .component(DataComponents.LORE, ModItems.lore("sea_witch_staff", 3)));
    public static final DeferredItem<SpawnEggItem> SEA_WITCH_SPAWN_EGG = ModItems.ITEMS.registerItem("sea_witch_spawn_egg", SpawnEggItem::new,
            p -> p.spawnEgg(ModEntities.SEA_WITCH.get()));

    static {
        ModItems.EQUIPMENT_TAB.add(WITCH_STAFF);
        SeaBlocks.TAB.add(WITCH_STAFF);
        SeaBlocks.TAB.add(SEA_WITCH_SPAWN_EGG);
    }
}
