package io.github.sirschrolli.kingdomsatwar.registry;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.item.MoonsilverBladeItem;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * Die Schätze der Schattenwelt:
 * <ul>
 *     <li><b>Mondsilber</b> – fahles Silber; Währung der Umbrer, Stoff der Mondsilberklinge und der Mondlaternen;</li>
 *     <li><b>Nachtstahl</b> – Werkzeuge und Rüstung so stark wie Netherit; der Helm lässt in der Schattenwelt sehen;</li>
 *     <li><b>Seelenkristall</b> – leuchtender Kristall aus der Tiefe (für Zauber und den Weg zu Nyxar);</li>
 *     <li><b>Abyssit</b> – aus dem tiefsten Abgrund: mit der Abyssit-Schmiedevorlage wird Nachtstahl zur stärksten
 *     Ausrüstung der Welt, feuerfest; die Stiefel fangen jeden Sturz ab.</li>
 * </ul>
 */
public final class ShadowItems {
    private ShadowItems() {}

    /** Lädt die Klasse, damit sich alle Gegenstände anmelden, bevor die Register gefüllt werden. */
    public static void init() {}

    private static final List<Supplier<? extends ItemLike>> TAB = ModBlocks.SHADOW_TAB;

    // =====================================================================================
    // Rohstoffe
    // =====================================================================================

    public static final DeferredItem<Item> RAW_MOONSILVER = item("raw_moonsilver", UnaryOperator.identity());
    public static final DeferredItem<Item> MOONSILVER_INGOT = item("moonsilver_ingot", UnaryOperator.identity());
    public static final DeferredItem<Item> MOONSILVER_NUGGET = item("moonsilver_nugget", UnaryOperator.identity());
    public static final DeferredItem<Item> RAW_NIGHTSTEEL = item("raw_nightsteel", UnaryOperator.identity());
    public static final DeferredItem<Item> NIGHTSTEEL_INGOT = item("nightsteel_ingot", p -> p.rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> SOUL_CRYSTAL = item("soul_crystal", p -> p.rarity(Rarity.UNCOMMON)
            .component(DataComponents.LORE, ModItems.lore("soul_crystal", 1)));
    public static final DeferredItem<Item> ABYSSITE_SHARD = item("abyssite_shard", p -> p.rarity(Rarity.RARE).fireResistant());
    public static final DeferredItem<Item> ABYSSITE_INGOT = item("abyssite_ingot", p -> p.rarity(Rarity.RARE).fireResistant());
    public static final DeferredItem<Item> NIGHT_CRYSTAL_SHARD = item("night_crystal_shard", UnaryOperator.identity());
    /** Seelenbeeren: leuchtende Beeren, essbar (kurz Nachtsicht) und zum Pflanzen des Strauchs. */
    public static final DeferredItem<net.minecraft.world.item.BlockItem> SOUL_BERRIES = register("soul_berries",
            p -> new net.minecraft.world.item.BlockItem(ShadowBlocks.SOUL_BERRY_BUSH.get(), p),
            p -> p.useItemDescriptionPrefix()
                    .food(new net.minecraft.world.food.FoodProperties.Builder().nutrition(2).saturationModifier(0.2f).build(),
                            net.minecraft.world.item.component.Consumables.defaultFood()
                                    .onConsume(new net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect(
                                            new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.NIGHT_VISION, 400, 0)))
                                    .build())
                    .compostable(net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders.COMPOSTABLE_LOW)
                    .component(DataComponents.LORE, ModItems.lore("soul_berries", 1)));
    /** Dämmerkorn: Saat, Ähren und das Brot der Umbrer. */
    public static final DeferredItem<net.minecraft.world.item.BlockItem> DUSKGRAIN_SEEDS = register("duskgrain_seeds",
            p -> new net.minecraft.world.item.BlockItem(ShadowBlocks.DUSKGRAIN.get(), p),
            p -> p.useItemDescriptionPrefix()
                    .compostable(net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders.COMPOSTABLE_LOW));
    public static final DeferredItem<Item> DUSKGRAIN = item("duskgrain",
            p -> p.compostable(net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders.COMPOSTABLE_MEDIUM));
    public static final DeferredItem<Item> DUSK_BREAD = item("dusk_bread",
            p -> p.food(new net.minecraft.world.food.FoodProperties.Builder().nutrition(6).saturationModifier(0.7f).build())
                    .compostable(net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders.COMPOSTABLE_MEDIUM_HIGH)
                    .component(DataComponents.LORE, ModItems.lore("dusk_bread", 1)));
    public static final DeferredItem<SmithingTemplateItem> ABYSSITE_UPGRADE_SMITHING_TEMPLATE = register("abyssite_upgrade_smithing_template",
            p -> new SmithingTemplateItem(
                    description("abyssite_upgrade.applies_to"),
                    description("abyssite_upgrade.ingredients"),
                    Component.translatable(key("abyssite_upgrade.base_slot_description")),
                    Component.translatable(key("abyssite_upgrade.additions_slot_description")),
                    List.of(slot("helmet"), slot("sword"), slot("chestplate"), slot("pickaxe"), slot("leggings"), slot("axe"),
                            slot("boots"), slot("hoe"), slot("shovel")),
                    List.of(slot("ingot")), p),
            p -> p.rarity(Rarity.RARE).fireResistant());

    // =====================================================================================
    // Nachtstahl
    // =====================================================================================

    public static final DeferredItem<Item> NIGHTSTEEL_SWORD = item("nightsteel_sword", p -> p.sword(ModEquipment.NIGHTSTEEL_TOOLS, 3.0f, -2.4f));
    public static final DeferredItem<Item> NIGHTSTEEL_PICKAXE = item("nightsteel_pickaxe", p -> p.pickaxe(ModEquipment.NIGHTSTEEL_TOOLS, 1.0f, -2.8f));
    public static final DeferredItem<Item> NIGHTSTEEL_AXE = item("nightsteel_axe", p -> p.axe(ModEquipment.NIGHTSTEEL_TOOLS, 5.0f, -3.0f));
    public static final DeferredItem<Item> NIGHTSTEEL_SHOVEL = item("nightsteel_shovel", p -> p.shovel(ModEquipment.NIGHTSTEEL_TOOLS, 1.5f, -3.0f));
    public static final DeferredItem<Item> NIGHTSTEEL_HOE = item("nightsteel_hoe", p -> p.hoe(ModEquipment.NIGHTSTEEL_TOOLS, -4.0f, 0.0f));
    /** Der Helm schenkt in der Schattenwelt Nachtsicht (siehe ShadowEquipment). */
    public static final DeferredItem<Item> NIGHTSTEEL_HELMET = armor("nightsteel_helmet", ModEquipment.NIGHTSTEEL, ArmorType.HELMET,
            p -> p.component(DataComponents.LORE, ModItems.lore("nightsteel_helmet", 1)));
    public static final DeferredItem<Item> NIGHTSTEEL_CHESTPLATE = armor("nightsteel_chestplate", ModEquipment.NIGHTSTEEL, ArmorType.CHESTPLATE,
            UnaryOperator.identity());
    public static final DeferredItem<Item> NIGHTSTEEL_LEGGINGS = armor("nightsteel_leggings", ModEquipment.NIGHTSTEEL, ArmorType.LEGGINGS,
            UnaryOperator.identity());
    public static final DeferredItem<Item> NIGHTSTEEL_BOOTS = armor("nightsteel_boots", ModEquipment.NIGHTSTEEL, ArmorType.BOOTS,
            UnaryOperator.identity());

    // =====================================================================================
    // Abyssit (aus Nachtstahl aufgewertet)
    // =====================================================================================

    public static final DeferredItem<Item> ABYSSITE_SWORD = abyss("abyssite_sword", p -> p.sword(ModEquipment.ABYSSITE_TOOLS, 3.0f, -2.4f));
    public static final DeferredItem<Item> ABYSSITE_PICKAXE = abyss("abyssite_pickaxe", p -> p.pickaxe(ModEquipment.ABYSSITE_TOOLS, 1.0f, -2.8f));
    public static final DeferredItem<Item> ABYSSITE_AXE = abyss("abyssite_axe", p -> p.axe(ModEquipment.ABYSSITE_TOOLS, 5.0f, -3.0f));
    public static final DeferredItem<Item> ABYSSITE_SHOVEL = abyss("abyssite_shovel", p -> p.shovel(ModEquipment.ABYSSITE_TOOLS, 1.5f, -3.0f));
    public static final DeferredItem<Item> ABYSSITE_HOE = abyss("abyssite_hoe", p -> p.hoe(ModEquipment.ABYSSITE_TOOLS, -5.0f, 0.0f));
    public static final DeferredItem<Item> ABYSSITE_HELMET = armor("abyssite_helmet", ModEquipment.ABYSSITE, ArmorType.HELMET,
            p -> p.fireResistant().rarity(Rarity.EPIC).component(DataComponents.LORE, ModItems.lore("nightsteel_helmet", 1)));
    public static final DeferredItem<Item> ABYSSITE_CHESTPLATE = armor("abyssite_chestplate", ModEquipment.ABYSSITE, ArmorType.CHESTPLATE,
            p -> p.fireResistant().rarity(Rarity.EPIC));
    public static final DeferredItem<Item> ABYSSITE_LEGGINGS = armor("abyssite_leggings", ModEquipment.ABYSSITE, ArmorType.LEGGINGS,
            p -> p.fireResistant().rarity(Rarity.EPIC));
    /** Die Stiefel fangen jeden Sturz ab. */
    public static final DeferredItem<Item> ABYSSITE_BOOTS = armor("abyssite_boots", ModEquipment.ABYSSITE, ArmorType.BOOTS,
            p -> p.fireResistant().rarity(Rarity.EPIC)
                    .attributes(ModEquipment.ABYSSITE.createAttributes(ArmorType.BOOTS)
                            .withModifierAdded(Attributes.FALL_DAMAGE_MULTIPLIER, new AttributeModifier(KingdomsAtWar.id("abyssite_boots_fall"),
                                    -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL), EquipmentSlotGroup.FEET))
                    .component(DataComponents.LORE, ModItems.lore("abyssite_boots", 1)));

    // =====================================================================================
    // Mondsilber
    // =====================================================================================

    /** Mondsilberklinge: trifft Schattenwesen und Untote besonders hart und lässt sie aufleuchten. */
    public static final DeferredItem<MoonsilverBladeItem> MOONSILVER_BLADE = register("moonsilver_blade", MoonsilverBladeItem::new,
            p -> p.sword(ModEquipment.MOONSILVER_TOOLS, 3.0f, -2.2f).rarity(Rarity.RARE)
                    .component(DataComponents.LORE, ModItems.lore("moonsilver_blade", 2)));

    // =====================================================================================
    // Kreaturen
    // =====================================================================================

    /** Schattenessenz: was von einem Schattenpirscher bleibt – Zutat für Schattenzauber, begehrt bei den Umbrern. */
    public static final DeferredItem<Item> SHADOW_ESSENCE = item("shadow_essence", p -> p.rarity(Rarity.UNCOMMON)
            .component(DataComponents.LORE, ModItems.lore("shadow_essence", 1)));
    public static final DeferredItem<SpawnEggItem> SHADE_STALKER_SPAWN_EGG = egg("shade_stalker", () -> ModEntities.SHADE_STALKER.get());
    public static final DeferredItem<SpawnEggItem> CRYSTAL_CRAWLER_SPAWN_EGG = egg("crystal_crawler", () -> ModEntities.CRYSTAL_CRAWLER.get());
    public static final DeferredItem<SpawnEggItem> NIGHT_WING_SPAWN_EGG = egg("night_wing", () -> ModEntities.NIGHT_WING.get());
    public static final DeferredItem<SpawnEggItem> MOON_STAG_SPAWN_EGG = egg("moon_stag", () -> ModEntities.MOON_STAG.get());
    public static final DeferredItem<SpawnEggItem> GLIMMER_MOTH_SPAWN_EGG = egg("glimmer_moth", () -> ModEntities.GLIMMER_MOTH.get());
    public static final DeferredItem<SpawnEggItem> UMBRER_SPAWN_EGG = egg("umbrer", () -> ModEntities.UMBRER.get());
    public static final DeferredItem<SpawnEggItem> UMBRER_GUARD_SPAWN_EGG = egg("umbrer_guard", () -> ModEntities.UMBRER_GUARD.get());

    /** Alle Spawn-Eier der Schattenwelt (für Modelle und Übersetzungen). */
    public static List<DeferredItem<SpawnEggItem>> spawnEggs() {
        return List.of(SHADE_STALKER_SPAWN_EGG, CRYSTAL_CRAWLER_SPAWN_EGG, NIGHT_WING_SPAWN_EGG, MOON_STAG_SPAWN_EGG, GLIMMER_MOTH_SPAWN_EGG,
                UMBRER_SPAWN_EGG, UMBRER_GUARD_SPAWN_EGG);
    }

    // =====================================================================================
    // Hilfsmethoden
    // =====================================================================================

    private static DeferredItem<SpawnEggItem> egg(String name, Supplier<EntityType<? extends Mob>> type) {
        return register(name + "_spawn_egg", SpawnEggItem::new, p -> p.spawnEgg(type.get()));
    }

    private static DeferredItem<Item> item(String name, UnaryOperator<Item.Properties> properties) {
        DeferredItem<Item> item = ModItems.ITEMS.registerSimpleItem(name, properties);
        TAB.add(item);
        return item;
    }

    private static <I extends Item> DeferredItem<I> register(String name, Function<Item.Properties, I> factory,
            UnaryOperator<Item.Properties> properties) {
        DeferredItem<I> item = ModItems.ITEMS.registerItem(name, factory, properties);
        TAB.add(item);
        return item;
    }

    private static DeferredItem<Item> abyss(String name, UnaryOperator<Item.Properties> properties) {
        return item(name, p -> properties.apply(p).fireResistant().rarity(Rarity.EPIC));
    }

    private static DeferredItem<Item> armor(String name, ArmorMaterial material, ArmorType type, UnaryOperator<Item.Properties> extra) {
        return item(name, p -> extra.apply(p.humanoidArmor(material, type)));
    }

    private static String key(String path) {
        return "item." + KingdomsAtWar.MODID + ".smithing_template." + path;
    }

    private static Component description(String path) {
        return Component.translatable(key(path)).withStyle(net.minecraft.ChatFormatting.BLUE);
    }

    private static Identifier slot(String name) {
        return Identifier.withDefaultNamespace("container/slot/" + name);
    }
}
