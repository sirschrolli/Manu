package io.github.sirschrolli.kingdomsatwar.registry;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.item.monster.BogClubItem;
import io.github.sirschrolli.kingdomsatwar.item.monster.ClawBladeItem;
import io.github.sirschrolli.kingdomsatwar.item.monster.EmberFangItem;
import io.github.sirschrolli.kingdomsatwar.item.monster.SoulScytheItem;
import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * Waffen und Rüstungen aus Teilen der neuen Monster (0.70) – und die Teile selbst:
 * <ul>
 *   <li><b>Werwolf:</b> Werwolfspelz und Werwolfsklaue → Krallenklinge (schnell, nachts schärfer, Blutrausch nach jedem
 *   Sieg) und die Werwolfspelz-Rüstung (Wolfskopf-Haube mit Ohren und Schnauze; voller Satz: nachts schnell und
 *   nachtsichtig).</li>
 *   <li><b>Moorschlurfer:</b> Moorwurzel und Moorherz → Moorkeule (schwer, Wurzelgriff, heilt sich in Regen und Wasser).</li>
 *   <li><b>Glutwurm:</b> Glutwurmschuppe und Glutzahn → Glutzahn-Klinge (entzündet, trifft Brennende härter) und der
 *   Glutwurm-Panzer (Hörnerhelm, Rückenstacheln; voller Satz: feuerfest, wer zuschlägt, fängt Feuer).</li>
 *   <li><b>Aschengeist:</b> Seelenasche und Aschenschleier → Seelensense (Seelenfeuer, schwächt; Verwehen: ein Sprung
 *   nach vorn, kurz unsichtbar und unverwundbar).</li>
 * </ul>
 */
public final class MonsterGear {
    private MonsterGear() {}

    /** Lädt die Klasse, damit sich alles anmeldet, bevor die Register gefüllt werden. */
    public static void init() {}

    // =====================================================================================
    // Teile
    // =====================================================================================

    public static final DeferredItem<Item> WEREWOLF_PELT = part("werewolf_pelt", p -> p);
    public static final DeferredItem<Item> WEREWOLF_CLAW = part("werewolf_claw", p -> p);
    public static final DeferredItem<Item> BOG_ROOT = part("bog_root", p -> p);
    public static final DeferredItem<Item> BOG_HEART = part("bog_heart", p -> p.rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> EMBER_SCALE = part("ember_scale", p -> p.fireResistant());
    public static final DeferredItem<Item> EMBER_FANG = part("ember_fang", p -> p.fireResistant().rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> SOUL_ASH = part("soul_ash", p -> p);
    public static final DeferredItem<Item> ASH_VEIL = part("ash_veil", p -> p.rarity(Rarity.UNCOMMON));

    public static List<DeferredItem<Item>> parts() {
        return List.of(WEREWOLF_PELT, WEREWOLF_CLAW, BOG_ROOT, BOG_HEART, EMBER_SCALE, EMBER_FANG, SOUL_ASH, ASH_VEIL);
    }

    // =====================================================================================
    // Werkstoffe
    // =====================================================================================

    public static final ToolMaterial CLAW_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_IRON_TOOL, 900, 7.0f, 2.5f, 16,
            ModTags.Items.REPAIRS_CLAW_BLADE);
    public static final ToolMaterial BOG_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_IRON_TOOL, 1100, 5.0f, 2.0f, 12,
            ModTags.Items.REPAIRS_BOG_CLUB);
    public static final ToolMaterial EMBER_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 1600, 8.0f, 3.5f, 14,
            ModTags.Items.REPAIRS_EMBER_FANG);
    public static final ToolMaterial SOUL_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 1400, 8.0f, 4.0f, 18,
            ModTags.Items.REPAIRS_SOUL_SCYTHE);

    public static final ResourceKey<EquipmentAsset> PELT_ASSET = asset("werewolf_pelt");
    public static final ResourceKey<EquipmentAsset> EMBER_ASSET = asset("ember_scale");
    /** Werwolfspelz: so fest wie Eisen, leicht und warm. */
    public static final ArmorMaterial PELT_ARMOR = ModEquipment.material(22, 2, 5, 6, 2, 7, 15, SoundEvents.ARMOR_EQUIP_LEATHER, 0.5f, 0.0f,
            ModTags.Items.REPAIRS_PELT_ARMOR, PELT_ASSET);
    /** Glutwurmschuppen: fester als Diamant, doch nicht so zäh wie Netherit. */
    public static final ArmorMaterial EMBER_ARMOR = ModEquipment.material(32, 3, 6, 8, 3, 11, 13, SoundEvents.ARMOR_EQUIP_NETHERITE, 2.0f,
            0.05f, ModTags.Items.REPAIRS_EMBER_ARMOR, EMBER_ASSET);

    // =====================================================================================
    // Waffen
    // =====================================================================================

    /** Krallenklinge: drei Werwolfsklauen an einem Faustgriff – schnell; nachts schärfer, nach jedem Sieg ein Blutrausch. */
    public static final DeferredItem<ClawBladeItem> CLAW_BLADE = weapon("claw_blade", ClawBladeItem::new,
            p -> p.sword(CLAW_MATERIAL, 3.0f, -1.9f).rarity(Rarity.RARE));
    /** Moorkeule: ein knorriger Wurzelknüppel mit leuchtenden Pilzen – schwer; Wurzelgriff, heilt sich in Regen und Wasser. */
    public static final DeferredItem<BogClubItem> BOG_CLUB = weapon("bog_club", BogClubItem::new,
            p -> p.sword(BOG_MATERIAL, 5.5f, -3.1f).rarity(Rarity.RARE));
    /** Glutzahn: eine gebogene Klinge aus dem Fangzahn des Glutwurms – entzündet und trifft Brennende härter. */
    public static final DeferredItem<EmberFangItem> EMBER_FANG_BLADE = weapon("ember_fang_blade", EmberFangItem::new,
            p -> p.sword(EMBER_MATERIAL, 3.0f, -2.4f).rarity(Rarity.EPIC).fireResistant());
    /** Seelensense: schwächt und entzündet mit Seelenfeuer; Verwehen trägt ihren Träger ein Stück nach vorn. */
    public static final DeferredItem<SoulScytheItem> SOUL_SCYTHE = weapon("soul_scythe", SoulScytheItem::new,
            p -> p.sword(SOUL_MATERIAL, 4.0f, -2.9f).rarity(Rarity.EPIC));

    public static List<DeferredItem<? extends Item>> weapons() {
        return List.of(CLAW_BLADE, BOG_CLUB, EMBER_FANG_BLADE, SOUL_SCYTHE);
    }

    // =====================================================================================
    // Rüstungen
    // =====================================================================================

    public static final DeferredItem<Item> PELT_HELMET = armor("werewolf_pelt_helmet", PELT_ARMOR, ArmorType.HELMET, false);
    public static final DeferredItem<Item> PELT_CHESTPLATE = armor("werewolf_pelt_chestplate", PELT_ARMOR, ArmorType.CHESTPLATE, false);
    public static final DeferredItem<Item> PELT_LEGGINGS = armor("werewolf_pelt_leggings", PELT_ARMOR, ArmorType.LEGGINGS, false);
    public static final DeferredItem<Item> PELT_BOOTS = armor("werewolf_pelt_boots", PELT_ARMOR, ArmorType.BOOTS, false);
    public static final DeferredItem<Item> EMBER_HELMET = armor("ember_scale_helmet", EMBER_ARMOR, ArmorType.HELMET, true);
    public static final DeferredItem<Item> EMBER_CHESTPLATE = armor("ember_scale_chestplate", EMBER_ARMOR, ArmorType.CHESTPLATE, true);
    public static final DeferredItem<Item> EMBER_LEGGINGS = armor("ember_scale_leggings", EMBER_ARMOR, ArmorType.LEGGINGS, true);
    public static final DeferredItem<Item> EMBER_BOOTS = armor("ember_scale_boots", EMBER_ARMOR, ArmorType.BOOTS, true);

    public static List<DeferredItem<Item>> peltSet() {
        return List.of(PELT_HELMET, PELT_CHESTPLATE, PELT_LEGGINGS, PELT_BOOTS);
    }

    public static List<DeferredItem<Item>> emberSet() {
        return List.of(EMBER_HELMET, EMBER_CHESTPLATE, EMBER_LEGGINGS, EMBER_BOOTS);
    }

    // =====================================================================================
    // Hilfen
    // =====================================================================================

    private static DeferredItem<Item> part(String name, UnaryOperator<Item.Properties> properties) {
        DeferredItem<Item> item = ModItems.ITEMS.registerSimpleItem(name,
                p -> properties.apply(p).component(DataComponents.LORE, ModItems.lore(name, 1)));
        ModItems.RESOURCE_TAB.add(item);
        return item;
    }

    private static <T extends Item> DeferredItem<T> weapon(String name, Function<Item.Properties, T> factory,
            UnaryOperator<Item.Properties> properties) {
        DeferredItem<T> item = ModItems.ITEMS.registerItem(name, factory,
                () -> properties.apply(new Item.Properties()).component(DataComponents.LORE, ModItems.lore(name, 2)));
        ModItems.EQUIPMENT_TAB.add(item);
        return item;
    }

    private static DeferredItem<Item> armor(String name, ArmorMaterial material, ArmorType type, boolean ember) {
        String set = ember ? "ember_scale_armor" : "werewolf_pelt_armor";
        DeferredItem<Item> item = ModItems.ITEMS.registerSimpleItem(name, p -> {
            Item.Properties props = p.humanoidArmor(material, type).rarity(ember ? Rarity.EPIC : Rarity.RARE)
                    .component(DataComponents.LORE, ModItems.lore(set, 2));
            if (!ember) {
                // Pelz ist leicht: jedes Teil macht ein wenig schneller
                EquipmentSlotGroup slot = EquipmentSlotGroup.bySlot(type.getSlot());
                props.attributes(material.createAttributes(type).withModifierAdded(Attributes.MOVEMENT_SPEED,
                        new AttributeModifier(KingdomsAtWar.id("werewolf_pelt_speed_" + type.getName()), 0.02,
                                AttributeModifier.Operation.ADD_MULTIPLIED_BASE), slot));
            }
            return ember ? props.fireResistant() : props;
        });
        ModItems.EQUIPMENT_TAB.add(item);
        return item;
    }

    private static ResourceKey<EquipmentAsset> asset(String name) {
        return ResourceKey.create(EquipmentAssets.ROOT_ID, KingdomsAtWar.id(name));
    }
}
