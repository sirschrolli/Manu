package io.github.sirschrolli.kingdomsatwar.registry;

import io.github.sirschrolli.kingdomsatwar.block.deep.OrganDeskBlock;
import io.github.sirschrolli.kingdomsatwar.block.deep.OrganDeskBlockEntity;
import io.github.sirschrolli.kingdomsatwar.item.deep.TuningForkItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Weapon;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * Die Stille Kathedrale und ihr Kantor (seit 1.7): das Orgelpult, an dem Sordin, der Kantor der Stille, steht und spielt, die
 * Stimmgabel (seine Beute) und sein Spawn-Ei – im Kreativ-Tab „Legenden“. Der Kantor selbst steht in {@link ModEntities}.
 */
public final class CathedralGear {
    private CathedralGear() {}

    /** Lädt die Klasse, damit sich alles anmeldet, bevor die Register gefüllt werden. */
    public static void init() {}

    /** Das Orgelpult: unzerstörbar; die Noten auf dem Blatt leuchten, solange der Kantor lebt. */
    public static final DeferredBlock<OrganDeskBlock> ORGAN_DESK = ModBlocks.blockWithItem("organ_desk", OrganDeskBlock::new,
            p -> p.mapColor(MapColor.COLOR_BROWN).strength(-1.0f, 3_600_000.0f).noLootTable().sound(SoundType.WOOD)
                    .lightLevel(s -> s.getValue(OrganDeskBlock.PHASE).light).noOcclusion().pushReaction(PushReaction.IMMOVEABLE)
                    .isValidSpawn((state, level, pos, type) -> false),
            ip -> ip.rarity(Rarity.EPIC).component(DataComponents.LORE, ModItems.lore("organ_desk", 2)), ForgeBlocks.TAB);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<OrganDeskBlockEntity>> ORGAN_DESK_ENTITY =
            ModBlockEntities.BLOCK_ENTITIES.register("organ_desk", () -> new BlockEntityType<>(OrganDeskBlockEntity::new, ORGAN_DESK.get()));

    /** Die Stimmgabel: schwer wie ein Streitkolben; Rechtsklick: die Resonanz (ein Schallschlag durch Stein und Rüstung). */
    public static final DeferredItem<TuningForkItem> TUNING_FORK = ModItems.ITEMS.registerItem("tuning_fork", TuningForkItem::new,
            () -> new Item.Properties().stacksTo(1).durability(2000).rarity(Rarity.EPIC).enchantable(15)
                    .repairable(ModTags.Items.REPAIRS_TUNING_FORK)
                    .component(DataComponents.WEAPON, new Weapon(1, 0.0f))
                    .attributes(ItemAttributeModifiers.builder()
                            .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, 8.0,
                                    AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                            .add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, -2.9,
                                    AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                            .build())
                    .component(DataComponents.LORE, ModItems.lore("tuning_fork", 3)));
    public static final DeferredItem<SpawnEggItem> SORDIN_SPAWN_EGG = ModItems.ITEMS.registerItem("sordin_spawn_egg",
            SpawnEggItem::new, p -> p.spawnEgg(ModEntities.SORDIN.get()));

    static {
        ModItems.EQUIPMENT_TAB.add(TUNING_FORK);
        ForgeBlocks.TAB.add(TUNING_FORK);
        ForgeBlocks.TAB.add(SORDIN_SPAWN_EGG);
    }
}
