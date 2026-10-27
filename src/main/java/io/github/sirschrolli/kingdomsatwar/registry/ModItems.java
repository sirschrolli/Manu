package io.github.sirschrolli.kingdomsatwar.registry;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.army.CommandBatonItem;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.item.DraconiumWeaponItem;
import io.github.sirschrolli.kingdomsatwar.item.DragonScaleArmorItem;
import io.github.sirschrolli.kingdomsatwar.magic.AetheritStaffItem;
import io.github.sirschrolli.kingdomsatwar.magic.RuneItem;
import io.github.sirschrolli.kingdomsatwar.magic.Spell;
import io.github.sirschrolli.kingdomsatwar.magic.SpellTabletItem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Unit;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.Weapon;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jspecify.annotations.Nullable;

/**
 * Alle Gegenstände der Mod, die keine Blöcke sind. Block-Gegenstände werden in {@link ModBlocks} registriert.
 */
public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(KingdomsAtWar.MODID);

    static final List<Supplier<? extends ItemLike>> RESOURCE_TAB = new ArrayList<>();
    static final List<Supplier<? extends ItemLike>> DRAGON_TAB = new ArrayList<>();
    static final List<Supplier<? extends ItemLike>> FARMING_TAB = new ArrayList<>();
    static final List<Supplier<? extends ItemLike>> KINGDOM_TAB = new ArrayList<>();
    static final List<Supplier<? extends ItemLike>> EQUIPMENT_TAB = new ArrayList<>();

    // =====================================================================================
    // Dragon Warfare – Erze & Rohstoffe
    // =====================================================================================

    /** Kohlenstoff: Brennstoff (wie Kohle) und Grundlage für Stahl. */
    public static final DeferredItem<Item> CARBON = item("carbon",
            p -> p.cookingFuel(ContextIntProviders.COOKING_TIME_COAL)
                    .component(DataComponents.LORE, lore("carbon", 1)), RESOURCE_TAB);
    public static final DeferredItem<Item> GEMSTONE = item("gemstone",
            p -> p.component(DataComponents.LORE, lore("gemstone", 1)), RESOURCE_TAB);
    public static final DeferredItem<Item> KONTERIT = item("konterit",
            p -> p.component(DataComponents.LORE, lore("konterit", 1)), RESOURCE_TAB);
    public static final DeferredItem<Item> AETHERIT = item("aetherit",
            p -> p.rarity(Rarity.UNCOMMON).component(DataComponents.LORE, lore("aetherit", 1)), RESOURCE_TAB);
    public static final DeferredItem<Item> DRACONIUM = item("draconium",
            p -> p.rarity(Rarity.RARE).fireResistant().component(DataComponents.LORE, lore("draconium", 1)), RESOURCE_TAB);
    public static final DeferredItem<Item> WYVERNIT = item("wyvernit",
            p -> p.rarity(Rarity.UNCOMMON).component(DataComponents.LORE, lore("wyvernit", 1)), RESOURCE_TAB);
    public static final DeferredItem<Item> STEEL_BLEND = item("steel_blend", UnaryOperator.identity(), RESOURCE_TAB);
    public static final DeferredItem<Item> STEEL_INGOT = item("steel_ingot",
            p -> p.component(DataComponents.LORE, lore("steel_ingot", 1)), RESOURCE_TAB);

    // =====================================================================================
    // Dragon Warfare – Drachen
    // =====================================================================================

    private static final Map<DragonType, DeferredItem<Item>> DRAGON_SCALES_MUTABLE = new EnumMap<>(DragonType.class);
    /** Eine Schuppensorte je Drachenart – Material für spätere Drachenrüstungen. */
    public static final Map<DragonType, DeferredItem<Item>> DRAGON_SCALES = Collections.unmodifiableMap(DRAGON_SCALES_MUTABLE);

    static {
        for (DragonType type : DragonType.values()) {
            DRAGON_SCALES_MUTABLE.put(type, item(type.id() + "_dragon_scale",
                    p -> p.rarity(Rarity.UNCOMMON).component(DataComponents.LORE, lore("dragon_scale", 1)), DRAGON_TAB));
        }
    }

    /** Drachenfutter – heilt Drachen und lässt sie schneller wachsen. */
    public static final DeferredItem<Item> DRAGON_FOOD = item("dragon_food",
            p -> p.component(DataComponents.LORE, lore("dragon_food", 1)), DRAGON_TAB);

    private static final Map<DragonType, DeferredItem<SpawnEggItem>> DRAGON_SPAWN_EGGS_MUTABLE = new EnumMap<>(DragonType.class);
    /** Spawn-Eier (Kreativmodus) für einen Schlüpfling jeder Drachenart. */
    public static final Map<DragonType, DeferredItem<SpawnEggItem>> DRAGON_SPAWN_EGGS = Collections.unmodifiableMap(DRAGON_SPAWN_EGGS_MUTABLE);

    static {
        for (DragonType type : DragonType.values()) {
            DRAGON_SPAWN_EGGS_MUTABLE.put(type, addTo(DRAGON_TAB, ITEMS.registerItem(type.id() + "_dragon_spawn_egg", SpawnEggItem::new,
                    p -> p.spawnEgg(ModEntities.DRAGONS.get(type).get()))));
        }
    }

    // Wilde Kreaturen: Spawn-Eier
    public static final DeferredItem<SpawnEggItem> WILD_BOAR_SPAWN_EGG = creatureEgg("wild_boar", () -> ModEntities.WILD_BOAR.get());
    public static final DeferredItem<SpawnEggItem> DEER_SPAWN_EGG = creatureEgg("deer", () -> ModEntities.DEER.get());
    public static final DeferredItem<SpawnEggItem> GOOSE_SPAWN_EGG = creatureEgg("goose", () -> ModEntities.GOOSE.get());
    public static final DeferredItem<SpawnEggItem> WILL_O_WISP_SPAWN_EGG = creatureEgg("will_o_wisp", () -> ModEntities.WILL_O_WISP.get());
    public static final DeferredItem<SpawnEggItem> CAVE_TROLL_SPAWN_EGG = creatureEgg("cave_troll", () -> ModEntities.CAVE_TROLL.get());
    public static final DeferredItem<SpawnEggItem> LINDWORM_SPAWN_EGG = creatureEgg("lindworm", () -> ModEntities.LINDWORM.get());

    /** Irrlichtstaub: leuchtender Staub der Irrlichter – eine Zutat für Magie. */
    public static final DeferredItem<Item> WISP_DUST = item("wisp_dust",
            p -> p.rarity(Rarity.UNCOMMON).component(DataComponents.LORE, lore("wisp_dust", 1)), RESOURCE_TAB);

    // =====================================================================================
    // Kingdoms at War – Landwirtschaft & Nahrung
    // =====================================================================================

    // 1. Neue Feldfrüchte und Nutzpflanzen
    public static final DeferredItem<BlockItem> TOMATO_SEEDS = seeds("tomato_seeds", () -> ModBlocks.TOMATOES.get());
    public static final DeferredItem<Item> TOMATO = food("tomato", 3, 0.4f, ContextIntProviders.COMPOSTABLE_MEDIUM);
    public static final DeferredItem<BlockItem> CABBAGE_SEEDS = seeds("cabbage_seeds", () -> ModBlocks.CABBAGES.get());
    public static final DeferredItem<Item> CABBAGE = food("cabbage", 2, 0.4f, ContextIntProviders.COMPOSTABLE_MEDIUM);
    /** Zwiebeln werden direkt gepflanzt, wie Karotten. */
    public static final DeferredItem<BlockItem> ONION = addTo(FARMING_TAB, ITEMS.registerItem("onion",
            p -> new BlockItem(ModBlocks.ONIONS.get(), p),
            () -> new Item.Properties().useItemDescriptionPrefix()
                    .food(new FoodProperties.Builder().nutrition(2).saturationModifier(0.3f).build())
                    .compostable(ContextIntProviders.COMPOSTABLE_MEDIUM)));
    public static final DeferredItem<BlockItem> BARLEY_SEEDS = seeds("barley_seeds", () -> ModBlocks.BARLEY.get());
    public static final DeferredItem<Item> BARLEY = item("barley",
            p -> p.compostable(ContextIntProviders.COMPOSTABLE_MEDIUM), FARMING_TAB);

    // 2. Neue Lebensmittel und Gerichte
    public static final DeferredItem<Item> BREAD_ROLL = food("bread_roll", 4, 0.5f, null);
    public static final DeferredItem<Item> VEGETABLE_STEW = item("vegetable_stew",
            p -> p.food(new FoodProperties.Builder().nutrition(9).saturationModifier(0.7f).build())
                    .usingConvertsTo(Items.BOWL).stacksTo(1), FARMING_TAB);
    public static final DeferredItem<Item> MEAT_PIE = food("meat_pie", 8, 0.8f, null);
    public static final DeferredItem<Item> APPLE_PIE = food("apple_pie", 7, 0.6f, null);
    /** Wildbret vom Hirsch – roh und gebraten. */
    public static final DeferredItem<Item> VENISON = food("venison", 3, 0.3f, null);
    public static final DeferredItem<Item> COOKED_VENISON = food("cooked_venison", 8, 0.8f, null);

    // 3. Hopfen, Weintrauben und Knoblauch (werden direkt gepflanzt), Getränke und Festmahl
    public static final DeferredItem<BlockItem> HOPS = plantable("hops", () -> ModBlocks.HOPS.get(), null);
    public static final DeferredItem<BlockItem> GRAPES = plantable("grapes", () -> ModBlocks.GRAPES.get(),
            new FoodProperties.Builder().nutrition(2).saturationModifier(0.3f).build());
    public static final DeferredItem<BlockItem> GARLIC = plantable("garlic", () -> ModBlocks.GARLIC_CROP.get(),
            new FoodProperties.Builder().nutrition(1).saturationModifier(0.3f).build());
    public static final DeferredItem<Item> GARLIC_BREAD = food("garlic_bread", 7, 0.7f, null);
    /** Ale aus Gerste und Hopfen: macht stark – aber wer zu viel trinkt, dem wird schwindelig. */
    public static final DeferredItem<Item> ALE = drink("ale", 3, 0.3f, 0.3f,
            new MobEffectInstance(MobEffects.STRENGTH, 900, 0));
    /** Rotwein aus Weintrauben: wärmt und heilt langsam. */
    public static final DeferredItem<Item> WINE = drink("wine", 2, 0.4f, 0.2f,
            new MobEffectInstance(MobEffects.REGENERATION, 240, 0));
    /** Honigmet, der Trank der Helden: ein Polster aus Lebenskraft. */
    public static final DeferredItem<Item> MEAD = drink("mead", 4, 0.4f, 0.15f,
            new MobEffectInstance(MobEffects.ABSORPTION, 2400, 0));

    // =====================================================================================
    // Kingdoms at War – Münzen
    // =====================================================================================

    /** Bronzemünze, Silbermünze, Goldmünze (je 10 sind eine der nächstgrößeren). */
    public static final DeferredItem<io.github.sirschrolli.kingdomsatwar.item.CoinItem> BRONZE_COIN = coin("bronze_coin",
            io.github.sirschrolli.kingdomsatwar.economy.Coin.BRONZE, Rarity.COMMON);
    public static final DeferredItem<io.github.sirschrolli.kingdomsatwar.item.CoinItem> SILVER_COIN = coin("silver_coin",
            io.github.sirschrolli.kingdomsatwar.economy.Coin.SILVER, Rarity.COMMON);
    public static final DeferredItem<io.github.sirschrolli.kingdomsatwar.item.CoinItem> GOLD_COIN = coin("gold_coin",
            io.github.sirschrolli.kingdomsatwar.economy.Coin.GOLD, Rarity.UNCOMMON);

    private static DeferredItem<io.github.sirschrolli.kingdomsatwar.item.CoinItem> coin(String name,
            io.github.sirschrolli.kingdomsatwar.economy.Coin coin, Rarity rarity) {
        return addTo(KINGDOM_TAB, ITEMS.registerItem(name, p -> new io.github.sirschrolli.kingdomsatwar.item.CoinItem(coin, p),
                () -> new Item.Properties().rarity(rarity)));
    }

    /** Bauplan aus dem Stadtfenster (welcher Bau, steht im Gegenstand). */
    public static final DeferredItem<io.github.sirschrolli.kingdomsatwar.item.BlueprintItem> BLUEPRINT = ITEMS.registerItem("blueprint",
            io.github.sirschrolli.kingdomsatwar.item.BlueprintItem::new, () -> new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));

    // =====================================================================================
    // Kingdoms at War – Armee
    // =====================================================================================

    /** Kommandostab: Befehle an alle eigenen Soldaten in der Nähe. */
    public static final DeferredItem<CommandBatonItem> COMMAND_BATON = addTo(KINGDOM_TAB, ITEMS.registerItem("command_baton",
            CommandBatonItem::new, () -> new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)
                    .component(DataComponents.LORE, lore("command_baton", 4))));

    private static final Map<SoldierType, DeferredItem<SpawnEggItem>> SOLDIER_SPAWN_EGGS_MUTABLE = new EnumMap<>(SoldierType.class);
    /** Spawn-Eier (Kreativmodus) für jede Soldatenart – der Soldat gehört dem, der das Ei benutzt. */
    public static final Map<SoldierType, DeferredItem<SpawnEggItem>> SOLDIER_SPAWN_EGGS = Collections.unmodifiableMap(SOLDIER_SPAWN_EGGS_MUTABLE);

    static {
        for (SoldierType type : SoldierType.values()) {
            SOLDIER_SPAWN_EGGS_MUTABLE.put(type, addTo(KINGDOM_TAB, ITEMS.registerItem(type.id() + "_spawn_egg", SpawnEggItem::new,
                    p -> p.spawnEgg(ModEntities.SOLDIERS.get(type).get()))));
        }
    }

    public static final DeferredItem<SpawnEggItem> BANDIT_SPAWN_EGG = addTo(KINGDOM_TAB, ITEMS.registerItem("bandit_spawn_egg",
            SpawnEggItem::new, p -> p.spawnEgg(ModEntities.BANDIT.get())));
    public static final DeferredItem<SpawnEggItem> BANDIT_ARCHER_SPAWN_EGG = addTo(KINGDOM_TAB, ITEMS.registerItem("bandit_archer_spawn_egg",
            SpawnEggItem::new, p -> p.spawnEgg(ModEntities.BANDIT_ARCHER.get())));

    // =====================================================================================
    // Waffen, Werkzeuge und Rüstungen – jedes Erz hat seine Bestimmung
    // =====================================================================================

    // Stahl: Werkzeuge und Rüstung zwischen Eisen und Diamant
    public static final DeferredItem<Item> STEEL_SWORD = gear("steel_sword", p -> p.sword(ModEquipment.STEEL_TOOLS, 3.0f, -2.4f));
    public static final DeferredItem<Item> STEEL_PICKAXE = gear("steel_pickaxe", p -> p.pickaxe(ModEquipment.STEEL_TOOLS, 1.0f, -2.8f));
    public static final DeferredItem<Item> STEEL_AXE = gear("steel_axe", p -> p.axe(ModEquipment.STEEL_TOOLS, 6.0f, -3.1f));
    public static final DeferredItem<Item> STEEL_SHOVEL = gear("steel_shovel", p -> p.shovel(ModEquipment.STEEL_TOOLS, 1.5f, -3.0f));
    public static final DeferredItem<Item> STEEL_HOE = gear("steel_hoe", p -> p.hoe(ModEquipment.STEEL_TOOLS, -2.0f, -0.5f));
    public static final DeferredItem<Item> STEEL_HELMET = armor("steel_helmet", ModEquipment.STEEL, ArmorType.HELMET);
    public static final DeferredItem<Item> STEEL_CHESTPLATE = armor("steel_chestplate", ModEquipment.STEEL, ArmorType.CHESTPLATE);
    public static final DeferredItem<Item> STEEL_LEGGINGS = armor("steel_leggings", ModEquipment.STEEL, ArmorType.LEGGINGS);
    public static final DeferredItem<Item> STEEL_BOOTS = armor("steel_boots", ModEquipment.STEEL, ArmorType.BOOTS);

    // Konterit: leichte Rüstung – jedes Teil macht 4 % schneller und federt Stürze ab
    public static final DeferredItem<Item> KONTERIT_HELMET = konterit("konterit_helmet", ArmorType.HELMET);
    public static final DeferredItem<Item> KONTERIT_CHESTPLATE = konterit("konterit_chestplate", ArmorType.CHESTPLATE);
    public static final DeferredItem<Item> KONTERIT_LEGGINGS = konterit("konterit_leggings", ArmorType.LEGGINGS);
    public static final DeferredItem<Item> KONTERIT_BOOTS = konterit("konterit_boots", ArmorType.BOOTS);

    // Draconium: Waffen, die ihr Ziel entflammen
    public static final DeferredItem<DraconiumWeaponItem> DRACONIUM_SWORD = addTo(EQUIPMENT_TAB, ITEMS.registerItem("draconium_sword",
            p -> new DraconiumWeaponItem(p, 4), () -> new Item.Properties().sword(ModEquipment.DRACONIUM_TOOLS, 3.0f, -2.4f)
                    .rarity(Rarity.RARE).fireResistant().component(DataComponents.LORE, lore("draconium_weapon", 1))));
    public static final DeferredItem<DraconiumWeaponItem> DRACONIUM_AXE = addTo(EQUIPMENT_TAB, ITEMS.registerItem("draconium_axe",
            p -> new DraconiumWeaponItem(p, 5), () -> new Item.Properties().axe(ModEquipment.DRACONIUM_TOOLS, 5.5f, -3.0f)
                    .rarity(Rarity.RARE).fireResistant().component(DataComponents.LORE, lore("draconium_weapon", 1))));

    // Drachenschuppen: je Drachenart ein Rüstungssatz mit der Kraft des Elements
    private static final Map<DragonType, List<DeferredItem<DragonScaleArmorItem>>> SCALE_ARMOR_MUTABLE = new EnumMap<>(DragonType.class);
    public static final Map<DragonType, List<DeferredItem<DragonScaleArmorItem>>> DRAGON_SCALE_ARMOR = Collections.unmodifiableMap(SCALE_ARMOR_MUTABLE);

    static {
        for (DragonType type : DragonType.values()) {
            List<DeferredItem<DragonScaleArmorItem>> set = new ArrayList<>();
            for (ArmorType piece : List.of(ArmorType.HELMET, ArmorType.CHESTPLATE, ArmorType.LEGGINGS, ArmorType.BOOTS)) {
                String name = type.id() + "_dragon_scale_" + piece.getName();
                set.add(addTo(EQUIPMENT_TAB, ITEMS.registerItem(name, p -> new DragonScaleArmorItem(p, type),
                        () -> new Item.Properties().humanoidArmor(ModEquipment.SCALE_ARMOR.get(type), piece).rarity(Rarity.UNCOMMON)
                                .component(DataComponents.LORE, lore("dragon_scale_armor_" + type.id(), 1)))));
            }
            SCALE_ARMOR_MUTABLE.put(type, List.copyOf(set));
        }
    }

    // Wyvernit: der Gleiter (wie Elytren, aus dem Erz der Lüfte)
    public static final DeferredItem<Item> WYVERNIT_GLIDER = addTo(EQUIPMENT_TAB, ITEMS.registerSimpleItem("wyvernit_glider",
            p -> p.durability(360).rarity(Rarity.RARE)
                    .component(DataComponents.GLIDER, Unit.INSTANCE)
                    .component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.CHEST)
                            .setEquipSound(SoundEvents.ARMOR_EQUIP_ELYTRA).setAsset(ModEquipment.GLIDER_ASSET).setDamageOnHurt(false).build())
                    .repairable(ModTags.Items.REPAIRS_GLIDER)
                    .component(DataComponents.LORE, lore("wyvernit_glider", 2))));

    // Edelstein: die Königskrone – bringt Glück
    public static final DeferredItem<Item> GEMSTONE_CROWN = addTo(EQUIPMENT_TAB, ITEMS.registerSimpleItem("gemstone_crown",
            p -> p.humanoidArmor(ModEquipment.CROWN, ArmorType.HELMET).rarity(Rarity.EPIC)
                    .attributes(ModEquipment.CROWN.createAttributes(ArmorType.HELMET).withModifierAdded(Attributes.LUCK,
                            new AttributeModifier(KingdomsAtWar.id("crown_luck"), 2.0, AttributeModifier.Operation.ADD_VALUE),
                            EquipmentSlotGroup.HEAD))
                    .component(DataComponents.LORE, lore("gemstone_crown", 1))));

    /** Trollkeule: schwer und langsam, schleudert Gegner weit weg und zerschmettert Schilde (seltene Beute der Höhlentrolle). */
    public static final DeferredItem<Item> TROLL_CLUB = gear("troll_club", p -> p.durability(520).rarity(Rarity.RARE).enchantable(10)
            .repairable(ItemTags.LOGS)
            .component(DataComponents.WEAPON, new Weapon(1, 5.0f))
            .attributes(ItemAttributeModifiers.builder()
                    .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, 10.0,
                            AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                    .add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, -3.4,
                            AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                    .add(Attributes.ATTACK_KNOCKBACK, new AttributeModifier(KingdomsAtWar.id("troll_club_knockback"), 1.5,
                            AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                    .build())
            .component(DataComponents.LORE, lore("troll_club", 1)));

    // Drachenpanzer: Rüstung für den eigenen ausgewachsenen Drachen
    public static final DeferredItem<Item> STEEL_DRAGON_ARMOR = dragonArmor("steel_dragon_armor", ModEquipment.STEEL_DRAGON_ARMOR, false);
    public static final DeferredItem<Item> DRACONIUM_DRAGON_ARMOR = dragonArmor("draconium_dragon_armor", ModEquipment.DRACONIUM_DRAGON_ARMOR, true);

    // =====================================================================================
    // Magie: Aetherit-Stab, Zaubertafeln der Erzmagier (und die alten Runen)
    // =====================================================================================

    /** Wirkt den gewählten Zauber; neue Zauber lernt er am Magietisch aus Zaubertafeln. */
    public static final DeferredItem<AetheritStaffItem> AETHERIT_STAFF = addTo(EQUIPMENT_TAB, ITEMS.registerItem("aetherit_staff",
            AetheritStaffItem::new, p -> p.durability(320).rarity(Rarity.EPIC).enchantable(12)
                    .repairable(ModTags.Items.GEMS_AETHERIT)
                    .component(DataComponents.USE_COOLDOWN, AetheritStaffItem.defaultCooldown())
                    .component(DataComponents.LORE, lore("aetherit_staff", 2))));

    private static final Map<Spell, DeferredItem<SpellTabletItem>> SPELL_TABLETS_MUTABLE = new EnumMap<>(Spell.class);
    /** Eine Zaubertafel je Element – Beute der Erzmagier in ihren Türmen. */
    public static final Map<Spell, DeferredItem<SpellTabletItem>> SPELL_TABLETS = Collections.unmodifiableMap(SPELL_TABLETS_MUTABLE);

    static {
        for (Spell spell : Spell.values()) {
            if (spell.element() != null) {
                SPELL_TABLETS_MUTABLE.put(spell, addTo(EQUIPMENT_TAB, ITEMS.registerItem(spell.element().id() + "_spell_tablet",
                        p -> new SpellTabletItem(spell, p), p -> p.stacksTo(16).rarity(Rarity.EPIC))));
            }
        }
    }

    /** Zeigt zum Nest eines Drachen-Bosses (jedes Nest gibt es nur einmal); Rechtsklick wechselt das Nest. */
    public static final DeferredItem<io.github.sirschrolli.kingdomsatwar.item.DragonCompassItem> DRAGON_COMPASS = addTo(EQUIPMENT_TAB,
            ITEMS.registerItem("dragon_compass", io.github.sirschrolli.kingdomsatwar.item.DragonCompassItem::new,
                    p -> p.stacksTo(1).rarity(Rarity.RARE).component(DataComponents.LORE, lore("dragon_compass", 1))));

    /** Die Chronik des Siegels: Geschichte, Ziele und Hinweise. */
    public static final DeferredItem<io.github.sirschrolli.kingdomsatwar.item.ChronicleItem> CHRONICLE = addTo(EQUIPMENT_TAB,
            ITEMS.registerItem("chronicle", io.github.sirschrolli.kingdomsatwar.item.ChronicleItem::new,
                    p -> p.stacksTo(1).rarity(Rarity.UNCOMMON).component(DataComponents.LORE, lore("chronicle", 1))));

    private static final Map<DragonType, DeferredItem<io.github.sirschrolli.kingdomsatwar.item.SealShardItem>> SEAL_SHARDS_MUTABLE =
            new EnumMap<>(DragonType.class);
    /** Ein Siegelsplitter je Drache – bleibt nach dem Sieg über den Boss zurück. */
    public static final Map<DragonType, DeferredItem<io.github.sirschrolli.kingdomsatwar.item.SealShardItem>> SEAL_SHARDS =
            Collections.unmodifiableMap(SEAL_SHARDS_MUTABLE);

    static {
        for (DragonType type : DragonType.values()) {
            String name = type.id() + "_seal_shard";
            SEAL_SHARDS_MUTABLE.put(type, addTo(DRAGON_TAB, ITEMS.registerItem(name,
                    p -> new io.github.sirschrolli.kingdomsatwar.item.SealShardItem(type, p),
                    p -> p.stacksTo(1).rarity(Rarity.EPIC).fireResistant().component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)
                            .component(DataComponents.LORE, lore("seal_shard", 2)))));
        }
    }

    private static final Map<DragonType, DeferredItem<RuneItem>> RUNES_MUTABLE = new EnumMap<>(DragonType.class);
    /**
     * Die alten Runen je Drachen-Element: werden nicht mehr hergestellt und nicht mehr gefunden (nicht im Kreativ-Menü);
     * vorhandene lassen sich am Magietisch wie eine Zaubertafel einschreiben.
     */
    public static final Map<DragonType, DeferredItem<RuneItem>> RUNES = Collections.unmodifiableMap(RUNES_MUTABLE);

    static {
        for (DragonType type : DragonType.values()) {
            String name = type.id() + "_rune";
            RUNES_MUTABLE.put(type, ITEMS.registerItem(name, p -> new RuneItem(type, p),
                    p -> p.stacksTo(16).rarity(Rarity.RARE).component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)
                            .component(DataComponents.LORE, lore("old_rune", 1))));
        }
    }

    private ModItems() {}

    // =====================================================================================
    // Hilfsmethoden
    // =====================================================================================

    private static <T extends Supplier<? extends ItemLike>> T addTo(List<Supplier<? extends ItemLike>> tab, T entry) {
        tab.add(entry);
        return entry;
    }

    private static DeferredItem<SpawnEggItem> creatureEgg(String name, Supplier<EntityType<? extends net.minecraft.world.entity.Mob>> type) {
        return addTo(DRAGON_TAB, ITEMS.registerItem(name + "_spawn_egg", SpawnEggItem::new, p -> p.spawnEgg(type.get())));
    }

    private static DeferredItem<Item> gear(String name, UnaryOperator<Item.Properties> properties) {
        return addTo(EQUIPMENT_TAB, ITEMS.registerSimpleItem(name, properties));
    }

    private static DeferredItem<Item> armor(String name, ArmorMaterial material, ArmorType type) {
        return gear(name, p -> p.humanoidArmor(material, type));
    }

    /** Konterit-Rüstung: Rüstungswerte plus Schnelligkeit und sichere Fallhöhe je Teil. */
    private static DeferredItem<Item> konterit(String name, ArmorType type) {
        EquipmentSlotGroup slot = EquipmentSlotGroup.bySlot(type.getSlot());
        return gear(name, p -> p.humanoidArmor(ModEquipment.KONTERIT, type).rarity(Rarity.UNCOMMON)
                .attributes(ModEquipment.KONTERIT.createAttributes(type)
                        .withModifierAdded(Attributes.MOVEMENT_SPEED, new AttributeModifier(KingdomsAtWar.id("konterit_speed_" + type.getName()),
                                0.04, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), slot)
                        .withModifierAdded(Attributes.SAFE_FALL_DISTANCE, new AttributeModifier(KingdomsAtWar.id("konterit_fall_" + type.getName()),
                                1.0, AttributeModifier.Operation.ADD_VALUE), slot))
                .component(DataComponents.LORE, lore("konterit_armor", 1)));
    }

    /** Drachenpanzer: passt nur Drachen (Körper-Platz) und fällt beim Tod des Drachen zu Boden. */
    private static DeferredItem<Item> dragonArmor(String name, ArmorMaterial material, boolean fireproof) {
        return gear(name, p -> {
            Item.Properties properties = p.stacksTo(1).rarity(fireproof ? Rarity.EPIC : Rarity.RARE)
                    .durability(ArmorType.BODY.getDurability(material.durability()))
                    .attributes(material.createAttributes(ArmorType.BODY))
                    .repairable(material.repairIngredient())
                    .component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.BODY)
                            .setEquipSound(material.equipSound()).setAsset(material.assetId())
                            .setAllowedEntities(HolderSet.direct(ModEntities.DRAGONS.values().stream()
                                    .map(type -> (Holder<EntityType<?>>) type).toList()))
                            .setCanBeSheared(true).build())
                    .component(DataComponents.LORE, lore(name, 2));
            return fireproof ? properties.fireResistant() : properties;
        });
    }

    private static DeferredItem<Item> item(String name, UnaryOperator<Item.Properties> properties,
            List<Supplier<? extends ItemLike>> tab) {
        return addTo(tab, ITEMS.registerSimpleItem(name, properties));
    }

    private static DeferredItem<Item> food(String name, int nutrition, float saturation,
            @Nullable ResourceKey<ContextIntProvider> compostChance) {
        FoodProperties food = new FoodProperties.Builder().nutrition(nutrition).saturationModifier(saturation).build();
        return item(name, p -> compostChance == null ? p.food(food) : p.food(food).compostable(compostChance), FARMING_TAB);
    }

    /** Frucht, die man direkt pflanzt (wie Karotten); mit {@code food} auch essbar. */
    private static DeferredItem<BlockItem> plantable(String name, Supplier<? extends Block> crop, @Nullable FoodProperties food) {
        return addTo(FARMING_TAB, ITEMS.registerItem(name, p -> new BlockItem(crop.get(), p), () -> {
            Item.Properties properties = new Item.Properties().useItemDescriptionPrefix().compostable(ContextIntProviders.COMPOSTABLE_MEDIUM);
            return food == null ? properties : properties.food(food);
        }));
    }

    /** Getränk in der Flasche: immer trinkbar, gibt seine Wirkung, mit etwas Glück auch Übelkeit; die Flasche bleibt. */
    private static DeferredItem<Item> drink(String name, int nutrition, float saturation, float nauseaChance, MobEffectInstance effect) {
        Consumable consumable = Consumables.defaultDrink()
                .onConsume(new ApplyStatusEffectsConsumeEffect(effect))
                .onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.NAUSEA, 140, 0), nauseaChance))
                .build();
        FoodProperties food = new FoodProperties.Builder().nutrition(nutrition).saturationModifier(saturation).alwaysEdible().build();
        return item(name, p -> p.food(food, consumable).usingConvertsTo(Items.GLASS_BOTTLE).stacksTo(16)
                .component(DataComponents.LORE, lore(name, 1)), FARMING_TAB);
    }

    private static DeferredItem<BlockItem> seeds(String name, Supplier<? extends Block> crop) {
        return addTo(FARMING_TAB, ITEMS.registerItem(name,
                p -> new BlockItem(crop.get(), p),
                () -> new Item.Properties().useItemDescriptionPrefix().compostable(ContextIntProviders.COMPOSTABLE_LOW)));
    }

    /**
     * Erzeugt eine graue Beschreibung (Lore) aus den Übersetzungsschlüsseln
     * {@code lore.kingdomsatwar.<name>.1} bis {@code .<lines>}.
     */
    public static ItemLore lore(String name, int lines) {
        List<Component> components = new ArrayList<>();
        for (int i = 1; i <= lines; i++) {
            components.add(Component.translatable(loreKey(name, i))
                    .withStyle(style -> style.withColor(0xA0A0A0).withItalic(false)));
        }
        return new ItemLore(components);
    }

    public static String loreKey(String name, int line) {
        return "lore." + KingdomsAtWar.MODID + "." + name + "." + line;
    }
}
