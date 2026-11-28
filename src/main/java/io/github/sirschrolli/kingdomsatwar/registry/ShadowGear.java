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
 * Dämmerquarz (seit 0.98): ein rauchiger Quarz der Schattenwelt, in dem die Farben der Dämmerung stehen – oben nachtviolett,
 * unten ein Rest Abendrot. Daraus die Dämmerrüstung der Schleicher: Wer sie trägt, wird von Monstern später bemerkt (siehe
 * {@link io.github.sirschrolli.kingdomsatwar.item.shadow.DuskArmor}).
 */
public final class ShadowGear {
    private ShadowGear() {}

    /** Lädt die Klasse (Registrierung der Blöcke und Gegenstände). */
    public static void init() {}

    // =====================================================================================
    // Dämmerquarz
    // =====================================================================================

    /** Dämmerquarzerz: rauchviolette Quarzspitzen im Schattenstein, an der Wurzel ein Hauch Abendrot (ab einer Eisenspitzhacke). */
    public static final DeferredBlock<DropExperienceBlock> DUSK_QUARTZ_ORE = ore("dusk_quartz_ore", false);
    /** … und im Tiefschattenstein. */
    public static final DeferredBlock<DropExperienceBlock> DEEP_DUSK_QUARTZ_ORE = ore("deep_dusk_quartz_ore", true);
    /** Dämmerquarz: eine rauchige Quarzspitze, oben nachtviolett, unten abendrot. */
    public static final DeferredItem<Item> DUSK_QUARTZ = add(ModItems.ITEMS.registerSimpleItem("dusk_quartz",
            p -> p.component(DataComponents.LORE, ModItems.lore("dusk_quartz", 1))));
    /** Dämmerquarzblock: geschliffene Quarzsäulen, durch die das Licht der Dämmerung zieht. */
    public static final DeferredBlock<Block> DUSK_QUARTZ_BLOCK = ModBlocks.blockWithItem("dusk_quartz_block", Block::new,
            p -> p.mapColor(MapColor.COLOR_PURPLE).instrument(NoteBlockInstrument.CHIME).requiresCorrectToolForDrops().strength(5.0f, 6.0f)
                    .sound(SoundType.AMETHYST).lightLevel(s -> 3),
            UnaryOperator.identity(), ModBlocks.SHADOW_TAB);

    // =====================================================================================
    // Die Dämmerrüstung
    // =====================================================================================

    public static final ResourceKey<EquipmentAsset> DUSK_ASSET = ResourceKey.create(EquipmentAssets.ROOT_ID, KingdomsAtWar.id("dusk"));
    /** Dämmerquarz mit weichem Leder: Schutz wie Eisen, leise, sehr gut zu verzaubern. */
    public static final ArmorMaterial DUSK_ARMOR = ModEquipment.material(18, 2, 5, 6, 2, 6, 18, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0f,
            0.0f, ModTags.Items.REPAIRS_DUSK_ARMOR, DUSK_ASSET);
    /** Wie viel schneller man schleicht (Beinlinge und Stiefel je). */
    public static final double SNEAK_BONUS = 0.15;

    /** Die Kapuze mit Halbmaske: wer schleicht, sieht im Dunkeln. */
    public static final DeferredItem<Item> DUSK_HOOD = armor("dusk_hood", ArmorType.HELMET, EquipmentSlotGroup.HEAD, null, 0.0);
    /** Der Mantel: ein kurzer Umhang über einem Wams aus Schuppen von Dämmerquarz. */
    public static final DeferredItem<Item> DUSK_CLOAK = armor("dusk_cloak", ArmorType.CHESTPLATE, EquipmentSlotGroup.CHEST, null, 0.0);
    /** Die Beinlinge: schneller schleichen. */
    public static final DeferredItem<Item> DUSK_LEGGINGS = armor("dusk_leggings", ArmorType.LEGGINGS, EquipmentSlotGroup.LEGS,
            Attributes.SNEAKING_SPEED, SNEAK_BONUS);
    /** Die Schleichstiefel mit weichen Sohlen: schneller schleichen, sanfter fallen. */
    public static final DeferredItem<Item> DUSK_BOOTS = armor("dusk_boots", ArmorType.BOOTS, EquipmentSlotGroup.FEET,
            Attributes.SNEAKING_SPEED, SNEAK_BONUS);

    /** Die Dämmerrüstung: Kapuze, Mantel, Beinlinge, Stiefel. */
    public static List<DeferredItem<Item>> duskSet() {
        return List.of(DUSK_HOOD, DUSK_CLOAK, DUSK_LEGGINGS, DUSK_BOOTS);
    }

    private static DeferredItem<Item> armor(String name, ArmorType type, EquipmentSlotGroup slot, Holder<Attribute> extra, double value) {
        ItemAttributeModifiers modifiers = DUSK_ARMOR.createAttributes(type);
        if (extra != null) {
            modifiers = modifiers.withModifierAdded(extra, new AttributeModifier(KingdomsAtWar.id(name + "_sneak"), value,
                    AttributeModifier.Operation.ADD_VALUE), slot);
        }
        if (type == ArmorType.BOOTS) {
            modifiers = modifiers.withModifierAdded(Attributes.SAFE_FALL_DISTANCE, new AttributeModifier(KingdomsAtWar.id(name + "_soft"), 2.0,
                    AttributeModifier.Operation.ADD_VALUE), slot);
        }
        ItemAttributeModifiers all = modifiers;
        DeferredItem<Item> item = ModItems.ITEMS.registerSimpleItem(name, p -> p.humanoidArmor(DUSK_ARMOR, type).attributes(all)
                .rarity(Rarity.UNCOMMON).component(DataComponents.LORE, ModItems.lore("dusk_armor", 3)));
        ModItems.EQUIPMENT_TAB.add(item);
        ModBlocks.SHADOW_TAB.add(item);
        return item;
    }

    private static DeferredBlock<DropExperienceBlock> ore(String name, boolean deep) {
        return ModBlocks.blockWithItem(name, p -> new DropExperienceBlock(UniformInt.of(2, 5), p),
                p -> p.mapColor(deep ? MapColor.DEEPSLATE : MapColor.TERRACOTTA_GRAY).instrument(NoteBlockInstrument.BASEDRUM)
                        .requiresCorrectToolForDrops().strength(deep ? 4.5f : 3.0f, 3.0f)
                        .sound(deep ? SoundType.DEEPSLATE : SoundType.STONE).lightLevel(s -> 2),
                UnaryOperator.identity(), ModBlocks.SHADOW_TAB);
    }

    private static DeferredItem<Item> add(DeferredItem<Item> item) {
        ModBlocks.SHADOW_TAB.add(item);
        return item;
    }
}
