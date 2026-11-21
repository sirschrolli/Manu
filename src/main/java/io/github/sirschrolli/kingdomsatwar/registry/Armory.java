package io.github.sirschrolli.kingdomsatwar.registry;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.item.armory.ArmoryShieldItem;
import io.github.sirschrolli.kingdomsatwar.item.armory.ArmoryShields;
import io.github.sirschrolli.kingdomsatwar.item.armory.DaggerItem;
import io.github.sirschrolli.kingdomsatwar.item.armory.EmberPickaxeItem;
import io.github.sirschrolli.kingdomsatwar.item.armory.GreatswordItem;
import io.github.sirschrolli.kingdomsatwar.item.armory.HalberdItem;
import io.github.sirschrolli.kingdomsatwar.item.armory.MinerHammerItem;
import io.github.sirschrolli.kingdomsatwar.item.armory.OreGlowEntity;
import io.github.sirschrolli.kingdomsatwar.item.armory.OreSeekerItem;
import io.github.sirschrolli.kingdomsatwar.item.armory.VeinBreakerItem;
import io.github.sirschrolli.kingdomsatwar.item.armory.WarhammerItem;
import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.Weapon;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * Die Waffenkammer (0.84): fünf Waffen und vier Spitzhacken mit eigenen Fähigkeiten, alle in 3D gehalten.
 * <ul>
 *   <li><b>Stahl-Hellebarde:</b> reicht 4,5 statt 3 Blöcke; ein voller Schlag durchbohrt bis zu zwei Gegner dahinter.</li>
 *   <li><b>Draconium-Kriegshammer:</b> rechte Taste halten und loslassen: Erdstoß – Gegner ringsum werden fortgeschleudert und
 *   fangen Feuer.</li>
 *   <li><b>Nachtstahl-Dolch:</b> sehr schnell; von hinten doppelter Schaden.</li>
 *   <li><b>Abyssit-Großschwert:</b> langsam und schwer; ein voller Schlag trifft alles im Halbkreis vor dem Träger.</li>
 *   <li><b>Stahl-Doppelaxt:</b> legt Schilde zehn Sekunden lahm (eine Axt nur fünf).</li>
 *   <li><b>Bergmannshammer:</b> baut drei mal drei Blöcke ab (schleichend nur einen).</li>
 *   <li><b>Glutspitzhacke:</b> schmilzt Erze und Rohstoffe gleich beim Abbauen.</li>
 *   <li><b>Adernbrecher:</b> bricht eine ganze Erzader auf einmal (schleichend nur einen Block).</li>
 *   <li><b>Erzspürer:</b> rechte Taste: Erze im Umkreis leuchten acht Sekunden durch die Wände.</li>
 *   <li><b>Schilde</b> (siehe {@link ArmoryShields}): Stahl-Rundschild (Parade), Drachenschild (Angreifer brennen),
 *   Turmschild (deckt die Seiten, steht fest), Spiegelschild (Geschosse prallen zurück).</li>
 * </ul>
 */
public final class Armory {
    private Armory() {}

    /** Lädt die Klasse, damit sich alles anmeldet, bevor die Register gefüllt werden. */
    public static void init() {}

    /** Bergmannshammer: langsamer als eine Stahlspitzhacke (er bricht ja neun Blöcke), dafür viel zäher. */
    public static final ToolMaterial MINER_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_IRON_TOOL, 1500, 5.0f, 2.5f, 12,
            ModTags.Items.STEEL_TOOL_MATERIALS);

    // =====================================================================================
    // Waffen
    // =====================================================================================

    public static final DeferredItem<HalberdItem> STEEL_HALBERD = gear("steel_halberd", HalberdItem::new,
            p -> p.sword(ModEquipment.STEEL_TOOLS, 4.0f, -3.0f).component(DataComponents.ATTACK_RANGE, HalberdItem.RANGE)
                    .rarity(Rarity.UNCOMMON));
    public static final DeferredItem<WarhammerItem> DRACONIUM_WARHAMMER = gear("draconium_warhammer", WarhammerItem::new,
            p -> p.sword(ModEquipment.DRACONIUM_TOOLS, 6.0f, -3.3f).rarity(Rarity.RARE).fireResistant());
    public static final DeferredItem<DaggerItem> NIGHTSTEEL_DAGGER = gear("nightsteel_dagger", DaggerItem::new,
            p -> p.sword(ModEquipment.NIGHTSTEEL_TOOLS, 0.5f, -1.4f).rarity(Rarity.RARE));
    public static final DeferredItem<GreatswordItem> ABYSSITE_GREATSWORD = gear("abyssite_greatsword", GreatswordItem::new,
            p -> p.sword(ModEquipment.ABYSSITE_TOOLS, 6.0f, -3.2f).rarity(Rarity.EPIC).fireResistant());
    public static final DeferredItem<Item> STEEL_DOUBLE_AXE = gear("steel_double_axe", Item::new,
            p -> p.axe(ModEquipment.STEEL_TOOLS, 7.0f, -3.2f).component(DataComponents.WEAPON, new Weapon(2, 10.0f))
                    .rarity(Rarity.UNCOMMON));

    // =====================================================================================
    // Spitzhacken
    // =====================================================================================

    public static final DeferredItem<MinerHammerItem> MINER_HAMMER = gear("miner_hammer", MinerHammerItem::new,
            p -> p.pickaxe(MINER_MATERIAL, 3.0f, -3.2f).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<EmberPickaxeItem> EMBER_PICKAXE = gear("ember_pickaxe", EmberPickaxeItem::new,
            p -> p.pickaxe(ModEquipment.DRACONIUM_TOOLS, 1.0f, -2.8f).rarity(Rarity.RARE).fireResistant());
    public static final DeferredItem<VeinBreakerItem> VEIN_BREAKER = gear("vein_breaker", VeinBreakerItem::new,
            p -> p.pickaxe(ModEquipment.NIGHTSTEEL_TOOLS, 1.0f, -2.8f).rarity(Rarity.RARE));
    public static final DeferredItem<OreSeekerItem> ORE_SEEKER = gear("ore_seeker", OreSeekerItem::new,
            p -> p.pickaxe(ModEquipment.ABYSSITE_TOOLS, 1.0f, -2.8f).rarity(Rarity.EPIC).fireResistant());

    // =====================================================================================
    // Schilde (in der Nebenhand wie der Schild von Minecraft; die Fähigkeiten in ArmoryShields)
    // =====================================================================================

    public static final DeferredItem<ArmoryShieldItem> STEEL_BUCKLER = shield("steel_buckler", ArmoryShieldItem.Kind.BUCKLER,
            p -> p.durability(420).rarity(Rarity.UNCOMMON).repairable(ModTags.Items.STEEL_TOOL_MATERIALS), 0.1f, 90.0f, 1.0f);
    public static final DeferredItem<ArmoryShieldItem> DRAGON_SHIELD = shield("dragon_shield", ArmoryShieldItem.Kind.DRAGON,
            p -> p.durability(900).rarity(Rarity.RARE).fireResistant().repairable(ModTags.Items.DRACONIUM_TOOL_MATERIALS), 0.25f, 90.0f, 1.0f);
    public static final DeferredItem<ArmoryShieldItem> TOWER_SHIELD = shield("tower_shield", ArmoryShieldItem.Kind.TOWER,
            p -> p.durability(1200).rarity(Rarity.RARE).repairable(ModTags.Items.NIGHTSTEEL_TOOL_MATERIALS)
                    .attributes(ItemAttributeModifiers.builder()
                            .add(Attributes.MOVEMENT_SPEED, new AttributeModifier(KingdomsAtWar.id("tower_shield_weight"), -0.1,
                                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL), EquipmentSlotGroup.HAND)
                            .add(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(KingdomsAtWar.id("tower_shield_footing"), 0.5,
                                    AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.HAND)
                            .build()),
            0.4f, ArmoryShields.TOWER_ANGLE, 0.5f);
    public static final DeferredItem<ArmoryShieldItem> MIRROR_SHIELD = shield("mirror_shield", ArmoryShieldItem.Kind.MIRROR,
            p -> p.durability(1400).rarity(Rarity.EPIC).fireResistant().repairable(ModTags.Items.ABYSSITE_TOOL_MATERIALS), 0.25f, 90.0f, 1.0f);

    public static List<DeferredItem<ArmoryShieldItem>> shields() {
        return List.of(STEEL_BUCKLER, DRAGON_SHIELD, TOWER_SHIELD, MIRROR_SHIELD);
    }

    /** Das Leuchten des Erzspürers: ein Blockbild im Erz, das nur sein Besitzer sieht – durch Wände, in der Farbe des Erzes. */
    public static final DeferredHolder<EntityType<?>, EntityType<OreGlowEntity>> ORE_GLOW = ModEntities.ENTITIES.registerEntityType(
            "ore_glow", OreGlowEntity::new, MobCategory.MISC,
            builder -> builder.noLootTable().noSummon().sized(0.0f, 0.0f).clientTrackingRange(4).updateInterval(20));

    public static List<DeferredItem<? extends Item>> weapons() {
        return List.of(STEEL_HALBERD, DRACONIUM_WARHAMMER, NIGHTSTEEL_DAGGER, ABYSSITE_GREATSWORD, STEEL_DOUBLE_AXE);
    }

    public static List<DeferredItem<? extends Item>> pickaxes() {
        return List.of(MINER_HAMMER, EMBER_PICKAXE, VEIN_BREAKER, ORE_SEEKER);
    }

    // =====================================================================================
    // Hilfen
    // =====================================================================================

    private static DeferredItem<ArmoryShieldItem> shield(String name, ArmoryShieldItem.Kind kind, UnaryOperator<Item.Properties> properties,
            float delaySeconds, float angle, float wear) {
        return gear(name, p -> new ArmoryShieldItem(p, kind), p -> properties.apply(p)
                .equippableUnswappable(EquipmentSlot.OFFHAND)
                .delayedComponent(DataComponents.BLOCKS_ATTACKS, context -> ArmoryShields.blocks(context, delaySeconds, angle, wear))
                .component(DataComponents.BREAK_SOUND, SoundEvents.SHIELD_BREAK));
    }

    private static <T extends Item> DeferredItem<T> gear(String name, Function<Item.Properties, T> factory,
            UnaryOperator<Item.Properties> properties) {
        DeferredItem<T> item = ModItems.ITEMS.registerItem(name, factory,
                () -> properties.apply(new Item.Properties()).component(DataComponents.LORE, ModItems.lore(name, 2)));
        ModItems.EQUIPMENT_TAB.add(item);
        return item;
    }
}
