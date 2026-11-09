package io.github.sirschrolli.kingdomsatwar.registry;

import io.github.sirschrolli.kingdomsatwar.block.end.StarstoneBlock;
import io.github.sirschrolli.kingdomsatwar.block.end.StarstoneBlockEntity;
import io.github.sirschrolli.kingdomsatwar.item.end.StarLanceItem;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * Asteria, die Sternenreiterin (0.65): der Sternstein auf dem Sternenhof der Sternwarte, die Sternenlanze, die
 * Sternenschwingen und die Sternsplitter – im Kreativ-Tab „Legenden“ (nach Malleus).
 */
public final class StarBlocks {
    private StarBlocks() {}

    /** Lädt die Klasse, damit sich alles anmeldet, bevor die Register gefüllt werden. */
    public static void init() {}

    /** Der Sternstein: ruft Asteria herab, merkt sich ihren Sieg und ruft sie (mit einem Endkristall) zurück. Unzerstörbar. */
    public static final DeferredBlock<StarstoneBlock> STARSTONE = ModBlocks.blockWithItem("starstone", StarstoneBlock::new,
            p -> p.mapColor(MapColor.COLOR_LIGHT_BLUE).strength(-1.0f, 3_600_000.0f).noLootTable().sound(SoundType.AMETHYST)
                    .lightLevel(s -> s.getValue(StarstoneBlock.PHASE).light).noOcclusion().pushReaction(PushReaction.IMMOVEABLE)
                    .isValidSpawn((state, level, pos, type) -> false),
            ip -> ip.rarity(Rarity.EPIC).component(DataComponents.LORE, ModItems.lore("starstone", 2)), ForgeBlocks.TAB);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<StarstoneBlockEntity>> STARSTONE_ENTITY =
            ModBlockEntities.BLOCK_ENTITIES.register("starstone", () -> new BlockEntityType<>(StarstoneBlockEntity::new, STARSTONE.get()));

    /** Werkstoff der Sternenlanze: härter und schärfer als Netherit; mit Sternsplittern zu reparieren. */
    public static final ToolMaterial STAR_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 2800, 9.5f, 5.0f, 18,
            ModTags.Items.REPAIRS_STAR_LANCE);
    /** Die Sternenlanze: Asterias Lanze (Stoß und Ansturm wie ein Speer, stärker im End und unter dem Nachthimmel, Sternenmal). */
    public static final DeferredItem<StarLanceItem> STAR_LANCE = add(ModItems.ITEMS.registerItem("star_lance", StarLanceItem::new,
            () -> new Item.Properties().spear(STAR_MATERIAL, 1.15f, 1.45f, 0.35f, 3.0f, 8.0f, 6.0f, 5.1f, 9.0f, 4.4f).rarity(Rarity.EPIC)
                    .fireResistant().component(DataComponents.LORE, ModItems.lore("star_lance", 3))));
    /**
     * Die Sternenschwingen: die Schwingen des gepanzerten Sternenrochens. Gleiten wie Elytren – dazu im Gleitflug (Springen)
     * der Sternenschub, ein Stoß nach vorn wie von einer Rakete (alle drei Sekunden), und eine Spur aus Sternenlicht.
     */
    public static final DeferredItem<Item> STAR_WINGS = add(ModItems.ITEMS.registerSimpleItem("star_wings",
            p -> p.durability(640).rarity(Rarity.EPIC).fireResistant()
                    .component(DataComponents.GLIDER, Unit.INSTANCE)
                    .component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.CHEST)
                            .setEquipSound(SoundEvents.ARMOR_EQUIP_ELYTRA).setAsset(ModEquipment.STAR_WINGS_ASSET).setDamageOnHurt(false).build())
                    .repairable(ModTags.Items.REPAIRS_STAR_WINGS)
                    .component(DataComponents.LORE, ModItems.lore("star_wings", 4))));
    /** Sternsplitter: ein Splitter gefallenen Sternenlichts – Beute Asterias, repariert Sternenlanze und Sternenschwingen. */
    public static final DeferredItem<Item> STAR_SHARD = add(ModItems.ITEMS.registerSimpleItem("star_shard",
            p -> p.rarity(Rarity.UNCOMMON).component(DataComponents.LORE, ModItems.lore("star_shard", 1))));

    // ---------------------------------------------------------------- Spawn-Eier
    public static final DeferredItem<SpawnEggItem> ASTERIA_SPAWN_EGG = egg("asteria", () -> ModEntities.ASTERIA.get());

    public static List<DeferredItem<SpawnEggItem>> spawnEggs() {
        return List.of(ASTERIA_SPAWN_EGG);
    }

    /** Schlichte Gegenstände (flaches Modell). */
    public static List<DeferredItem<? extends Item>> flatItems() {
        return List.of(STAR_SHARD);
    }

    private static DeferredItem<SpawnEggItem> egg(String name, Supplier<EntityType<? extends Mob>> type) {
        return add(ModItems.ITEMS.registerItem(name + "_spawn_egg", SpawnEggItem::new, p -> p.spawnEgg(type.get())));
    }

    private static <T extends Item> DeferredItem<T> add(DeferredItem<T> item) {
        ForgeBlocks.TAB.add(item);
        return item;
    }
}
