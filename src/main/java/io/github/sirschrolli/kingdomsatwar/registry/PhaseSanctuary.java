package io.github.sirschrolli.kingdomsatwar.registry;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.end.VoidThroneBlock;
import io.github.sirschrolli.kingdomsatwar.block.end.VoidThroneBlockEntity;
import io.github.sirschrolli.kingdomsatwar.item.end.PhaseBladeItem;
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
 * Das Phasenheiligtum und sein König (seit 1.5): der Leerenthron, vor dem Phasmor, der Leerwandlerkönig, kniet und Wache hält,
 * die Phasenklinge (seine Beute) und sein Spawn-Ei – im Kreativ-Tab „Legenden“. Der König selbst, seine Trugbilder und seine
 * Phasenbrocken stehen in {@link ModEntities}.
 */
public final class PhaseSanctuary {
    private PhaseSanctuary() {}

    /** Lädt die Klasse, damit sich alles anmeldet, bevor die Register gefüllt werden. */
    public static void init() {}

    /** Der Leerenthron: unzerstörbar; der Phasit in der Lehne leuchtet, solange der König lebt. */
    public static final DeferredBlock<VoidThroneBlock> VOID_THRONE = ModBlocks.blockWithItem("void_throne", VoidThroneBlock::new,
            p -> p.mapColor(MapColor.COLOR_PURPLE).strength(-1.0f, 3_600_000.0f).noLootTable().sound(SoundType.STONE)
                    .lightLevel(s -> s.getValue(VoidThroneBlock.PHASE).light).noOcclusion().pushReaction(PushReaction.IMMOVEABLE)
                    .isValidSpawn((state, level, pos, type) -> false),
            ip -> ip.rarity(Rarity.EPIC).component(DataComponents.LORE, ModItems.lore("void_throne", 2)), ForgeBlocks.TAB);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<VoidThroneBlockEntity>> VOID_THRONE_ENTITY =
            ModBlockEntities.BLOCK_ENTITIES.register("void_throne", () -> new BlockEntityType<>(VoidThroneBlockEntity::new, VOID_THRONE.get()));

    /** Die Phasenklinge: ein Zweihänder aus Phasit, etwas schwerer als Netherit; Rechtsklick: der Phasenhieb. */
    public static final DeferredItem<PhaseBladeItem> PHASE_BLADE = ModItems.ITEMS.registerItem("phase_blade", PhaseBladeItem::new,
            () -> new Item.Properties().stacksTo(1).durability(2200).rarity(Rarity.EPIC).enchantable(15)
                    .repairable(ModTags.Items.REPAIRS_PHASE_BLADE)
                    .component(DataComponents.WEAPON, new Weapon(1, 0.0f))
                    .attributes(ItemAttributeModifiers.builder()
                            .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, 8.0,
                                    AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                            .add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, -2.7,
                                    AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                            .add(Attributes.ENTITY_INTERACTION_RANGE, new AttributeModifier(KingdomsAtWar.id("phase_blade_reach"), 0.5,
                                    AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                            .build())
                    .component(DataComponents.LORE, ModItems.lore("phase_blade", 3)));
    public static final DeferredItem<SpawnEggItem> PHASMOR_SPAWN_EGG = ModItems.ITEMS.registerItem("phasmor_spawn_egg",
            SpawnEggItem::new, p -> p.spawnEgg(ModEntities.PHASMOR.get()));

    static {
        ModItems.EQUIPMENT_TAB.add(PHASE_BLADE);
        ForgeBlocks.TAB.add(PHASE_BLADE);
        ForgeBlocks.TAB.add(PHASMOR_SPAWN_EGG);
    }
}
