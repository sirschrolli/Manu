package io.github.sirschrolli.kingdomsatwar.registry;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import java.util.List;
import java.util.function.UnaryOperator;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
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
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * Aquamarin aus dem Urmeer (seit 0.97): das Erz in den Felsen des Meeresgrunds, der Edelstein, der Block – und die
 * <b>Taucherrüstung</b> aus Aquamarin, deren Teile unter Wasser helfen (länger atmen, schneller schwimmen, schneller abbauen;
 * der volle Satz schenkt klare Sicht unter Wasser, siehe {@link io.github.sirschrolli.kingdomsatwar.item.sea.AquamarineArmor}).
 */
public final class SeaGear {
    private SeaGear() {}

    /** Lädt die Klasse, damit sich alles anmeldet, bevor die Register gefüllt werden. */
    public static void init() {}

    // =====================================================================================
    // Aquamarin
    // =====================================================================================

    /**
     * Aquamarinerz: lange, meerblaue Kristallsäulen im Gezeitenstein, die ein wenig schimmern (ab einer Eisenspitzhacke;
     * Glück bringt mehr).
     */
    public static final DeferredBlock<DropExperienceBlock> AQUAMARINE_ORE = ModBlocks.blockWithItem("aquamarine_ore",
            p -> new DropExperienceBlock(UniformInt.of(3, 7), p),
            p -> p.mapColor(MapColor.COLOR_CYAN).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops()
                    .strength(3.0f, 3.0f).sound(SoundType.STONE).lightLevel(s -> 3),
            UnaryOperator.identity(), SeaBlocks.TAB);
    /** Aquamarin: ein klarer Stein in der Farbe des Meeres über hellem Sand. */
    public static final DeferredItem<Item> AQUAMARINE = add(ModItems.ITEMS.registerSimpleItem("aquamarine",
            p -> p.component(DataComponents.LORE, ModItems.lore("aquamarine", 1))));
    /** Aquamarinblock: neun geschliffene Aquamarine im Treppenschliff. */
    public static final DeferredBlock<Block> AQUAMARINE_BLOCK = ModBlocks.blockWithItem("aquamarine_block", Block::new,
            p -> p.mapColor(MapColor.COLOR_CYAN).instrument(NoteBlockInstrument.CHIME).requiresCorrectToolForDrops().strength(5.0f, 6.0f)
                    .sound(SoundType.AMETHYST),
            UnaryOperator.identity(), SeaBlocks.TAB);

    // =====================================================================================
    // Die Taucherrüstung
    // =====================================================================================

    public static final ResourceKey<EquipmentAsset> AQUAMARINE_ASSET = ResourceKey.create(EquipmentAssets.ROOT_ID, KingdomsAtWar.id("aquamarine"));
    /** Aquamarin: so fest wie Eisen, etwas zäher, gut zu verzaubern. */
    public static final ArmorMaterial AQUAMARINE_ARMOR = ModEquipment.material(22, 2, 5, 6, 2, 6, 15, SoundEvents.ARMOR_EQUIP_TURTLE, 0.5f,
            0.0f, ModTags.Items.REPAIRS_AQUAMARINE_ARMOR, AQUAMARINE_ASSET);

    /** Der Taucherhelm: Atem (wie Atmung II) und schnelleres Abbauen unter Wasser. */
    public static final DeferredItem<Item> AQUAMARINE_HELMET = armor("aquamarine_helmet", ArmorType.HELMET, EquipmentSlotGroup.HEAD,
            Attributes.OXYGEN_BONUS, 1.5, Attributes.SUBMERGED_MINING_SPEED, 0.4);
    /** Der Harnisch mit den Luftflaschen auf dem Rücken: mehr Atem. */
    public static final DeferredItem<Item> AQUAMARINE_CHESTPLATE = armor("aquamarine_chestplate", ArmorType.CHESTPLATE, EquipmentSlotGroup.CHEST,
            Attributes.OXYGEN_BONUS, 1.0, null, 0.0);
    /** Der Beinschutz: schneller schwimmen. */
    public static final DeferredItem<Item> AQUAMARINE_LEGGINGS = armor("aquamarine_leggings", ArmorType.LEGGINGS, EquipmentSlotGroup.LEGS,
            Attributes.WATER_MOVEMENT_EFFICIENCY, 0.33, null, 0.0);
    /** Die Flossen: noch schneller schwimmen. */
    public static final DeferredItem<Item> AQUAMARINE_BOOTS = armor("aquamarine_boots", ArmorType.BOOTS, EquipmentSlotGroup.FEET,
            Attributes.WATER_MOVEMENT_EFFICIENCY, 0.33, null, 0.0);

    /** Die Taucherrüstung: Helm, Harnisch, Beinschutz, Flossen. */
    public static List<DeferredItem<Item>> aquamarineSet() {
        return List.of(AQUAMARINE_HELMET, AQUAMARINE_CHESTPLATE, AQUAMARINE_LEGGINGS, AQUAMARINE_BOOTS);
    }

    private static DeferredItem<Item> armor(String name, ArmorType type, EquipmentSlotGroup slot, Holder<Attribute> first, double firstValue,
            Holder<Attribute> second, double secondValue) {
        ItemAttributeModifiers modifiers = AQUAMARINE_ARMOR.createAttributes(type)
                .withModifierAdded(first, new AttributeModifier(KingdomsAtWar.id(name + "_water"), firstValue, AttributeModifier.Operation.ADD_VALUE),
                        slot);
        if (second != null) {
            modifiers = modifiers.withModifierAdded(second, new AttributeModifier(KingdomsAtWar.id(name + "_water_2"), secondValue,
                    AttributeModifier.Operation.ADD_VALUE), slot);
        }
        ItemAttributeModifiers all = modifiers;
        DeferredItem<Item> item = ModItems.ITEMS.registerSimpleItem(name, p -> p.humanoidArmor(AQUAMARINE_ARMOR, type).attributes(all)
                .rarity(Rarity.UNCOMMON).component(DataComponents.LORE, ModItems.lore("aquamarine_armor", 2)));
        ModItems.EQUIPMENT_TAB.add(item);
        SeaBlocks.TAB.add(item);
        return item;
    }

    private static DeferredItem<Item> add(DeferredItem<Item> item) {
        SeaBlocks.TAB.add(item);
        return item;
    }
}
