package io.github.sirschrolli.kingdomsatwar.registry;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.creator.CreatorAltarBlock;
import io.github.sirschrolli.kingdomsatwar.block.creator.CreatorAltarBlockEntity;
import io.github.sirschrolli.kingdomsatwar.item.creator.CreatorScepterItem;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Weapon;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * Schrolli der Schöpfer (0.75): der Altar des Schöpfers, der Schöpferfunke, das Zepter und die Schwingen des Schöpfers und
 * die Spawn-Eier – im eigenen Kreativ-Tab „Die Schöpfung“.
 */
public final class CreatorBlocks {
    static final List<Supplier<? extends ItemLike>> TAB = new ArrayList<>();

    private CreatorBlocks() {}

    /** Lädt die Klasse, damit sich alles anmeldet, bevor die Register gefüllt werden. */
    public static void init() {}

    /** Der Altar des Schöpfers: verwahrt den Schöpferfunken und ruft Schrolli den Schöpfer. Sehr widerstandsfähig. */
    public static final DeferredBlock<CreatorAltarBlock> CREATOR_ALTAR = ModBlocks.blockWithItem("creator_altar", CreatorAltarBlock::new,
            p -> p.mapColor(MapColor.GOLD).strength(50.0f, 1200.0f).requiresCorrectToolForDrops().sound(SoundType.AMETHYST)
                    .lightLevel(s -> s.getValue(CreatorAltarBlock.PHASE).light).noOcclusion().pushReaction(PushReaction.IMMOVEABLE)
                    .isValidSpawn((state, level, pos, type) -> false),
            ip -> ip.rarity(Rarity.EPIC).fireResistant().component(DataComponents.LORE, ModItems.lore("creator_altar", 3)), TAB);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CreatorAltarBlockEntity>> CREATOR_ALTAR_ENTITY =
            ModBlockEntities.BLOCK_ENTITIES.register("creator_altar", () -> new BlockEntityType<>(CreatorAltarBlockEntity::new, CREATOR_ALTAR.get()));

    /** Der Schöpferfunke: ein Splitter des ersten Lichts – Beute des Weltenfressers oder aus den Trophäen aller Bosse gefertigt. */
    public static final DeferredItem<Item> CREATOR_SPARK = add(ModItems.ITEMS.registerSimpleItem("creator_spark",
            p -> p.rarity(Rarity.EPIC).fireResistant().stacksTo(16).component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)
                    .component(DataComponents.LORE, ModItems.lore("creator_spark", 3))));

    /**
     * Das Zepter des Schöpfers: eine Waffe aus Gold und Licht, unzerbrechlich. Schlägt mächtig zu; Rechtsklick wirft einen
     * Lichtspeer, schleichend ruft es eine Säule aus Licht herab.
     */
    public static final DeferredItem<CreatorScepterItem> CREATOR_SCEPTER = add(ModItems.ITEMS.registerItem("creator_scepter", CreatorScepterItem::new,
            () -> new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant().enchantable(22)
                    .component(DataComponents.UNBREAKABLE, Unit.INSTANCE)
                    .component(DataComponents.WEAPON, new Weapon(1, 0.0f))
                    .attributes(ItemAttributeModifiers.builder()
                            .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, 15.0,
                                    AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                            .add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, -2.6,
                                    AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                            .add(Attributes.ENTITY_INTERACTION_RANGE, new AttributeModifier(KingdomsAtWar.id("creator_scepter_reach"), 1.0,
                                    AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                            .build())
                    .component(DataComponents.LORE, ModItems.lore("creator_scepter", 4))));

    /** Die Schwingen des Schöpfers: weiße Federn mit goldenen Spitzen – gleiten wie Elytren und zerbrechen nie. */
    public static final DeferredItem<Item> CREATOR_WINGS = add(ModItems.ITEMS.registerSimpleItem("creator_wings",
            p -> p.rarity(Rarity.EPIC).fireResistant().stacksTo(1)
                    .component(DataComponents.GLIDER, Unit.INSTANCE)
                    .component(DataComponents.UNBREAKABLE, Unit.INSTANCE)
                    .component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.CHEST)
                            .setEquipSound(SoundEvents.ARMOR_EQUIP_ELYTRA).setAsset(ModEquipment.CREATOR_WINGS_ASSET).setDamageOnHurt(false).build())
                    .component(DataComponents.LORE, ModItems.lore("creator_wings", 2))));

    // ---------------------------------------------------------------- Spawn-Eier
    public static final DeferredItem<SpawnEggItem> SCHROLLI_SPAWN_EGG = egg("schrolli", () -> ModEntities.SCHROLLI.get());
    public static final DeferredItem<SpawnEggItem> SERAPHLING_SPAWN_EGG = egg("seraphling", () -> ModEntities.SERAPHLING.get());

    public static List<DeferredItem<SpawnEggItem>> spawnEggs() {
        return List.of(SCHROLLI_SPAWN_EGG, SERAPHLING_SPAWN_EGG);
    }

    /** Die Gegenstände mit flachem Bild (das Zepter ist in der Hand ein 3D-Modell). */
    public static List<DeferredItem<Item>> flatItems() {
        return List.of(CREATOR_SPARK, CREATOR_WINGS);
    }

    private static DeferredItem<SpawnEggItem> egg(String name, Supplier<EntityType<? extends Mob>> type) {
        return add(ModItems.ITEMS.registerItem(name + "_spawn_egg", SpawnEggItem::new, p -> p.spawnEgg(type.get())));
    }

    private static <T extends Item> DeferredItem<T> add(DeferredItem<T> item) {
        TAB.add(item);
        return item;
    }
}
