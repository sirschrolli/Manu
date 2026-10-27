package io.github.sirschrolli.kingdomsatwar.registry;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

/**
 * Werkstoffe für Werkzeuge, Waffen und Rüstungen – jedes Erz der Mod hat seine Bestimmung:
 * <ul>
 *     <li><b>Stahl</b>: Werkzeuge und Rüstung zwischen Eisen und Diamant</li>
 *     <li><b>Konterit</b>: leichte Rüstung, die schneller macht und Stürze abfedert</li>
 *     <li><b>Draconium</b>: Waffen, die ihr Ziel entflammen, und Drachenpanzer</li>
 *     <li><b>Drachenschuppen</b>: Rüstungen mit der Kraft ihres Elements</li>
 *     <li><b>Wyvernit</b>: der Gleiter</li>
 *     <li><b>Edelstein</b>: die Königskrone</li>
 *     <li><b>Nachtstahl</b> (Schattenwelt): Werkzeuge und Rüstung wie Netherit; der Helm lässt im Zwielicht sehen</li>
 *     <li><b>Abyssit</b> (Schattenwelt): die stärkste Ausrüstung, aus Nachtstahl aufgewertet; feuerfest</li>
 *     <li><b>Mondsilber</b> (Schattenwelt): die Mondsilberklinge gegen Schattenwesen und Untote</li>
 * </ul>
 */
public final class ModEquipment {
    public static final ResourceKey<EquipmentAsset> STEEL_ASSET = asset("steel");
    public static final ResourceKey<EquipmentAsset> KONTERIT_ASSET = asset("konterit");
    public static final ResourceKey<EquipmentAsset> CROWN_ASSET = asset("gemstone_crown");
    public static final ResourceKey<EquipmentAsset> GLIDER_ASSET = asset("wyvernit_glider");
    public static final ResourceKey<EquipmentAsset> STEEL_DRAGON_ARMOR_ASSET = asset("steel_dragon_armor");
    public static final ResourceKey<EquipmentAsset> DRACONIUM_DRAGON_ARMOR_ASSET = asset("draconium_dragon_armor");
    public static final Map<DragonType, ResourceKey<EquipmentAsset>> SCALE_ASSETS = new EnumMap<>(DragonType.class);
    public static final ResourceKey<EquipmentAsset> NIGHTSTEEL_ASSET = asset("nightsteel");
    public static final ResourceKey<EquipmentAsset> ABYSSITE_ASSET = asset("abyssite");

    public static final ToolMaterial STEEL_TOOLS = new ToolMaterial(BlockTags.INCORRECT_FOR_IRON_TOOL, 640, 7.0f, 2.5f, 12,
            ModTags.Items.STEEL_TOOL_MATERIALS);
    public static final ToolMaterial DRACONIUM_TOOLS = new ToolMaterial(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 1900, 8.5f, 4.0f, 18,
            ModTags.Items.DRACONIUM_TOOL_MATERIALS);

    public static final ToolMaterial NIGHTSTEEL_TOOLS = new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 2200, 9.5f, 4.0f, 15,
            ModTags.Items.NIGHTSTEEL_TOOL_MATERIALS);
    public static final ToolMaterial ABYSSITE_TOOLS = new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 3000, 10.5f, 5.0f, 16,
            ModTags.Items.ABYSSITE_TOOL_MATERIALS);
    public static final ToolMaterial MOONSILVER_TOOLS = new ToolMaterial(BlockTags.INCORRECT_FOR_IRON_TOOL, 1100, 8.0f, 3.0f, 22,
            ModTags.Items.MOONSILVER_TOOL_MATERIALS);

    public static final ArmorMaterial STEEL = armor(24, 2, 6, 7, 2, 9, 11, SoundEvents.ARMOR_EQUIP_IRON, 1.0f, 0.0f,
            ModTags.Items.REPAIRS_STEEL_ARMOR, STEEL_ASSET);
    public static final ArmorMaterial KONTERIT = armor(28, 2, 5, 6, 2, 8, 16, SoundEvents.ARMOR_EQUIP_DIAMOND, 1.5f, 0.0f,
            ModTags.Items.REPAIRS_KONTERIT_ARMOR, KONTERIT_ASSET);
    public static final ArmorMaterial CROWN = armor(18, 0, 0, 0, 2, 0, 25, SoundEvents.ARMOR_EQUIP_GOLD, 0.0f, 0.0f,
            ModTags.Items.REPAIRS_CROWN, CROWN_ASSET);
    /** Drachenpanzer (nur der Körper-Wert zählt). */
    public static final ArmorMaterial STEEL_DRAGON_ARMOR = armor(20, 0, 0, 0, 0, 10, 8, SoundEvents.ARMOR_EQUIP_IRON, 1.0f, 0.0f,
            ModTags.Items.REPAIRS_STEEL_ARMOR, STEEL_DRAGON_ARMOR_ASSET);
    public static final ArmorMaterial DRACONIUM_DRAGON_ARMOR = armor(40, 0, 0, 0, 0, 18, 15, SoundEvents.ARMOR_EQUIP_NETHERITE, 3.0f, 0.1f,
            ModTags.Items.REPAIRS_DRACONIUM, DRACONIUM_DRAGON_ARMOR_ASSET);
    public static final Map<DragonType, ArmorMaterial> SCALE_ARMOR = new EnumMap<>(DragonType.class);
    public static final ArmorMaterial NIGHTSTEEL = armor(36, 3, 6, 8, 3, 11, 15, SoundEvents.ARMOR_EQUIP_NETHERITE, 3.0f, 0.1f,
            ModTags.Items.REPAIRS_NIGHTSTEEL_ARMOR, NIGHTSTEEL_ASSET);
    public static final ArmorMaterial ABYSSITE = armor(44, 3, 6, 8, 3, 12, 16, SoundEvents.ARMOR_EQUIP_NETHERITE, 3.5f, 0.15f,
            ModTags.Items.REPAIRS_ABYSSITE_ARMOR, ABYSSITE_ASSET);

    static {
        for (DragonType type : DragonType.values()) {
            ResourceKey<EquipmentAsset> asset = asset(type.id() + "_dragon_scale");
            SCALE_ASSETS.put(type, asset);
            SCALE_ARMOR.put(type, armor(30, 3, 6, 8, 3, 11, 14, SoundEvents.ARMOR_EQUIP_TURTLE, 2.0f, 0.05f,
                    ModTags.Items.scaleArmorRepair(type), asset));
        }
    }

    private ModEquipment() {}

    private static ResourceKey<EquipmentAsset> asset(String name) {
        return ResourceKey.create(EquipmentAssets.ROOT_ID, KingdomsAtWar.id(name));
    }

    private static ArmorMaterial armor(int durability, int boots, int legs, int chest, int helmet, int body, int enchantment,
            Holder<SoundEvent> sound, float toughness, float knockback, TagKey<Item> repair, ResourceKey<EquipmentAsset> asset) {
        Map<ArmorType, Integer> defense = new EnumMap<>(ArmorType.class);
        defense.put(ArmorType.BOOTS, boots);
        defense.put(ArmorType.LEGGINGS, legs);
        defense.put(ArmorType.CHESTPLATE, chest);
        defense.put(ArmorType.HELMET, helmet);
        defense.put(ArmorType.BODY, body);
        return new ArmorMaterial(durability, defense, enchantment, sound, toughness, knockback, repair, asset);
    }
}
