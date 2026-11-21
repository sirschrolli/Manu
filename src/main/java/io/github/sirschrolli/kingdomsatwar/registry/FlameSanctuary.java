package io.github.sirschrolli.kingdomsatwar.registry;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.nether.FlameAltarBlock;
import io.github.sirschrolli.kingdomsatwar.block.nether.FlameAltarBlockEntity;
import io.github.sirschrolli.kingdomsatwar.item.nether.FlameWhipItem;
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
 * Das Flammenheiligtum und seine Priesterin (seit 1.2): der Flammenaltar in der Mitte des Heiligtums, vor dem Ignara schwebt
 * und betet, die Flammenpeitsche (ihre Beute) und ihr Spawn-Ei – im Kreativ-Tab „Legenden“. Die Priesterin selbst und ihre
 * Opalsplitter stehen in {@link ModEntities}.
 */
public final class FlameSanctuary {
    private FlameSanctuary() {}

    /** Lädt die Klasse, damit sich alles anmeldet, bevor die Register gefüllt werden. */
    public static void init() {}

    /** Der Flammenaltar: unzerstörbar; das ewige Feuer brennt, solange die Priesterin lebt. */
    public static final DeferredBlock<FlameAltarBlock> FLAME_ALTAR = ModBlocks.blockWithItem("flame_altar", FlameAltarBlock::new,
            p -> p.mapColor(MapColor.COLOR_BLACK).strength(-1.0f, 3_600_000.0f).noLootTable().sound(SoundType.GILDED_BLACKSTONE)
                    .lightLevel(s -> s.getValue(FlameAltarBlock.PHASE).light).noOcclusion().pushReaction(PushReaction.IMMOVEABLE)
                    .isValidSpawn((state, level, pos, type) -> false),
            ip -> ip.rarity(Rarity.EPIC).component(DataComponents.LORE, ModItems.lore("flame_altar", 2)), ForgeBlocks.TAB);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FlameAltarBlockEntity>> FLAME_ALTAR_ENTITY =
            ModBlockEntities.BLOCK_ENTITIES.register("flame_altar", () -> new BlockEntityType<>(FlameAltarBlockEntity::new, FLAME_ALTAR.get()));

    /** Die Flammenpeitsche: reicht weiter als ein Schwert, setzt in Brand; Rechtsklick: der Peitschenschlag. */
    public static final DeferredItem<FlameWhipItem> FLAME_WHIP = ModItems.ITEMS.registerItem("flame_whip", FlameWhipItem::new,
            () -> new Item.Properties().stacksTo(1).durability(1800).rarity(Rarity.EPIC).fireResistant().enchantable(16)
                    .repairable(ModTags.Items.REPAIRS_FLAME_WHIP)
                    .component(DataComponents.WEAPON, new Weapon(1, 0.0f))
                    .attributes(ItemAttributeModifiers.builder()
                            .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, 7.0,
                                    AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                            .add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, -2.3,
                                    AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                            .add(Attributes.ENTITY_INTERACTION_RANGE, new AttributeModifier(KingdomsAtWar.id("flame_whip_reach"), 2.0,
                                    AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                            .build())
                    .component(DataComponents.LORE, ModItems.lore("flame_whip", 3)));
    public static final DeferredItem<SpawnEggItem> IGNARA_SPAWN_EGG = ModItems.ITEMS.registerItem("ignara_spawn_egg",
            SpawnEggItem::new, p -> p.spawnEgg(ModEntities.IGNARA.get()));

    static {
        ModItems.EQUIPMENT_TAB.add(FLAME_WHIP);
        ForgeBlocks.TAB.add(FLAME_WHIP);
        ForgeBlocks.TAB.add(IGNARA_SPAWN_EGG);
    }
}
