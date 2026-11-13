package io.github.sirschrolli.kingdomsatwar.registry;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import java.util.List;
import java.util.function.UnaryOperator;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * Himmelseisen (seit 0.99): Eisen aus gefallenen Sternen. In der Leere steckt es in Leerenstein, am reichsten im Kern der
 * gefallenen Sterne in den Sternenkratern der Sternenstaubfelder. Roh ist es ein Klumpen mit schwarzer Schmelzkruste;
 * geschmolzen und geschliffen zeigt es das Muster der Sterne (feine, sich kreuzende Bänder). Daraus die Rüstung der
 * Sternwandler, die die Schwerkraft überlistet (siehe {@link io.github.sirschrolli.kingdomsatwar.item.voidrealm.StarwalkerArmor}).
 */
public final class VoidGear {
    private VoidGear() {}

    /** Lädt die Klasse (Registrierung der Blöcke und Gegenstände). */
    public static void init() {}

    // =====================================================================================
    // Himmelseisen
    // =====================================================================================

    /** Himmelseisenerz: Leerenstein mit Brocken aus Sterneneisen, die im Dunkeln schimmern (ab einer Diamantspitzhacke). */
    public static final DeferredBlock<Block> SKY_IRON_ORE = ModBlocks.blockWithItem("sky_iron_ore", Block::new,
            p -> p.mapColor(MapColor.COLOR_BLACK).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops()
                    .strength(4.0f, 6.0f).sound(SoundType.DEEPSLATE).lightLevel(s -> 3),
            UnaryOperator.identity(), VoidBlocks.TAB);
    /** Rohes Himmelseisen: ein Klumpen mit schwarzer Schmelzkruste, aus dem blankes Eisen schaut. */
    public static final DeferredItem<Item> RAW_SKY_IRON = add(ModItems.ITEMS.registerSimpleItem("raw_sky_iron",
            p -> p.component(DataComponents.LORE, ModItems.lore("raw_sky_iron", 1))));
    /** Himmelseisenbarren: geschliffen zeigt er das Muster der Sterne. */
    public static final DeferredItem<Item> SKY_IRON_INGOT = add(ModItems.ITEMS.registerSimpleItem("sky_iron_ingot",
            p -> p.rarity(Rarity.UNCOMMON).component(DataComponents.LORE, ModItems.lore("sky_iron_ingot", 1))));
    /** Himmelseisenblock: eine geschliffene Platte mit dem Muster der Sterne, durch das ein kühler Schimmer zieht. */
    public static final DeferredBlock<Block> SKY_IRON_BLOCK = ModBlocks.blockWithItem("sky_iron_block", Block::new,
            p -> p.mapColor(MapColor.COLOR_LIGHT_BLUE).instrument(NoteBlockInstrument.IRON_XYLOPHONE).requiresCorrectToolForDrops()
                    .strength(5.5f, 8.0f).sound(SoundType.NETHERITE_BLOCK).lightLevel(s -> 2),
            UnaryOperator.identity(), VoidBlocks.TAB);

    // =====================================================================================
    // Die Rüstung der Sternwandler
    // =====================================================================================

    public static final ResourceKey<EquipmentAsset> STARWALKER_ASSET = ResourceKey.create(EquipmentAssets.ROOT_ID,
            KingdomsAtWar.id("starwalker"));
    /** Himmelseisen: Schutz wie Diamant, etwas zäh, gut zu verzaubern. */
    public static final ArmorMaterial STARWALKER_ARMOR = ModEquipment.material(33, 3, 6, 8, 3, 11, 14, SoundEvents.ARMOR_EQUIP_NETHERITE,
            2.0f, 0.0f, ModTags.Items.REPAIRS_STARWALKER_ARMOR, STARWALKER_ASSET);
    /** So viel weniger zieht die Schwerkraft je Teil (vier Teile: knapp ein Fünftel – man springt höher und fällt langsamer). */
    public static final double PIECE_GRAVITY = -0.05;
    /** So viel weiter fällt man mit den Stiefeln, ohne sich zu verletzen (Blöcke). */
    public static final double SOFT_FALL = 3.0;

    /** Der Helm mit dem Sternenkamm. */
    public static final DeferredItem<Item> STARWALKER_HELMET = armor("starwalker_helmet", ArmorType.HELMET, EquipmentSlotGroup.HEAD);
    /** Der Harnisch mit dem Sternbild auf der Brust und dem kurzen Kometenmantel. */
    public static final DeferredItem<Item> STARWALKER_CHESTPLATE = armor("starwalker_chestplate", ArmorType.CHESTPLATE, EquipmentSlotGroup.CHEST);
    /** Die Beinschienen. */
    public static final DeferredItem<Item> STARWALKER_LEGGINGS = armor("starwalker_leggings", ArmorType.LEGGINGS, EquipmentSlotGroup.LEGS);
    /** Die Stiefel mit den Kometenflossen: weiche Landung. */
    public static final DeferredItem<Item> STARWALKER_BOOTS = armor("starwalker_boots", ArmorType.BOOTS, EquipmentSlotGroup.FEET);

    /** Die Rüstung der Sternwandler: Helm, Harnisch, Beinschienen, Stiefel. */
    public static List<DeferredItem<Item>> starwalkerSet() {
        return List.of(STARWALKER_HELMET, STARWALKER_CHESTPLATE, STARWALKER_LEGGINGS, STARWALKER_BOOTS);
    }

    private static DeferredItem<Item> armor(String name, ArmorType type, EquipmentSlotGroup slot) {
        ItemAttributeModifiers modifiers = STARWALKER_ARMOR.createAttributes(type).withModifierAdded(Attributes.GRAVITY,
                new AttributeModifier(KingdomsAtWar.id(name + "_light"), PIECE_GRAVITY, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL), slot);
        if (type == ArmorType.BOOTS) {
            modifiers = modifiers.withModifierAdded(Attributes.SAFE_FALL_DISTANCE, new AttributeModifier(KingdomsAtWar.id(name + "_soft"),
                    SOFT_FALL, AttributeModifier.Operation.ADD_VALUE), slot);
        }
        ItemAttributeModifiers all = modifiers;
        DeferredItem<Item> item = ModItems.ITEMS.registerSimpleItem(name, p -> p.humanoidArmor(STARWALKER_ARMOR, type).attributes(all)
                .rarity(Rarity.RARE).component(DataComponents.LORE, ModItems.lore("starwalker_armor", 3)));
        ModItems.EQUIPMENT_TAB.add(item);
        VoidBlocks.TAB.add(item);
        return item;
    }

    private static DeferredItem<Item> add(DeferredItem<Item> item) {
        VoidBlocks.TAB.add(item);
        return item;
    }
}
