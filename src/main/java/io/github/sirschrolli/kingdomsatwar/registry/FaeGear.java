package io.github.sirschrolli.kingdomsatwar.registry;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * Ausrüstung aus dem Feenreich (seit 0.96): die <b>Mondsteinrüstung</b> der Zauberer – kaum fester als Eisen, doch jeder Teil
 * speichert Mondlicht (mehr Mana, schnellere Erholung; siehe {@link io.github.sirschrolli.kingdomsatwar.item.fae.MoonstoneArmor}) –
 * und der <b>Hüterhammer</b>, die Waffe des Steinhüters.
 */
public final class FaeGear {
    private FaeGear() {}

    /** Lädt die Klasse, damit sich alles anmeldet, bevor die Register gefüllt werden. */
    public static void init() {}

    // =====================================================================================
    // Die Mondsteinrüstung
    // =====================================================================================

    public static final ResourceKey<EquipmentAsset> MOONSTONE_ASSET = ResourceKey.create(EquipmentAssets.ROOT_ID, KingdomsAtWar.id("moonstone"));
    /** Mondstein: so fest wie Konterit, nicht ganz so hart – und leichter zu verzaubern als jede andere Rüstung. */
    public static final ArmorMaterial MOONSTONE_ARMOR = ModEquipment.material(26, 2, 5, 6, 2, 6, 24, SoundEvents.ARMOR_EQUIP_DIAMOND, 1.0f,
            0.0f, ModTags.Items.REPAIRS_MOONSTONE_ARMOR, MOONSTONE_ASSET);

    public static final DeferredItem<Item> MOONSTONE_HELMET = armor("moonstone_helmet", ArmorType.HELMET);
    public static final DeferredItem<Item> MOONSTONE_CHESTPLATE = armor("moonstone_chestplate", ArmorType.CHESTPLATE);
    public static final DeferredItem<Item> MOONSTONE_LEGGINGS = armor("moonstone_leggings", ArmorType.LEGGINGS);
    public static final DeferredItem<Item> MOONSTONE_BOOTS = armor("moonstone_boots", ArmorType.BOOTS);

    /** Die Mondsteinrüstung: Helm (Stirnreif), Brustpanzer, Beinschutz, Stiefel. */
    public static List<DeferredItem<Item>> moonstoneSet() {
        return List.of(MOONSTONE_HELMET, MOONSTONE_CHESTPLATE, MOONSTONE_LEGGINGS, MOONSTONE_BOOTS);
    }

    // =====================================================================================
    // Der Hüterhammer
    // =====================================================================================

    /** Feenstein, mit Mondstein gebunden: so zäh wie Diamant. */
    public static final ToolMaterial KEEPER_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 1600, 7.0f, 3.0f, 18,
            ModTags.Items.REPAIRS_KEEPER_MAUL);
    /** Der Hüterhammer: die Beute des Steinhüters – schwer, langsam, mit der Kristallspur (Rechtsklick). */
    public static final DeferredItem<io.github.sirschrolli.kingdomsatwar.item.fae.KeeperMaulItem> KEEPER_MAUL = ModItems.ITEMS.registerItem(
            "keeper_maul", io.github.sirschrolli.kingdomsatwar.item.fae.KeeperMaulItem::new,
            () -> new Item.Properties().sword(KEEPER_MATERIAL, 6.0f, -3.2f).rarity(Rarity.EPIC)
                    .component(DataComponents.LORE, ModItems.lore("keeper_maul", 3)));

    static {
        ModItems.EQUIPMENT_TAB.add(KEEPER_MAUL);
        FaeBlocks.TAB.add(KEEPER_MAUL);
    }

    private static DeferredItem<Item> armor(String name, ArmorType type) {
        DeferredItem<Item> item = ModItems.ITEMS.registerSimpleItem(name, p -> p.humanoidArmor(MOONSTONE_ARMOR, type).rarity(Rarity.UNCOMMON)
                .component(DataComponents.LORE, ModItems.lore("moonstone_armor", 2)));
        ModItems.EQUIPMENT_TAB.add(item);
        FaeBlocks.TAB.add(item);
        return item;
    }
}
