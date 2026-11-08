package io.github.sirschrolli.kingdomsatwar.registry;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * Was das Meer schenkt: die Beute der Meereswesen, die Tiefseelaterne, der Eimer mit Leuchtqualle und die Spawn-Eier –
 * alles im Kreativ-Tab „Das Meer“.
 */
public final class SeaBlocks {
    private SeaBlocks() {}

    /** Inhalt des Kreativ-Tabs „Das Meer“ (in Reihenfolge). */
    static final List<Supplier<? extends ItemLike>> TAB = new ArrayList<>();

    /** Lädt die Klasse, damit sich alles anmeldet, bevor die Register gefüllt werden. */
    public static void init() {}

    public static List<Supplier<? extends ItemLike>> tab() {
        return java.util.Collections.unmodifiableList(TAB);
    }

    // =====================================================================================
    // Blöcke
    // =====================================================================================

    /** Tiefseelaterne: ein Gehäuse aus Prismarin und Eisen, in dem der Leuchtköder eines Anglerfischs weiterleuchtet. */
    public static final DeferredBlock<LanternBlock> DEEP_SEA_LANTERN = ModBlocks.blockWithItem("deep_sea_lantern", LanternBlock::new,
            p -> p.mapColor(MapColor.COLOR_CYAN).forceSolidOn().strength(3.5f).sound(SoundType.LANTERN).lightLevel(s -> 15).noOcclusion()
                    .pushReaction(PushReaction.POPPED),
            UnaryOperator.identity(), TAB);

    // =====================================================================================
    // Gegenstände
    // =====================================================================================

    /** Leuchtgelee: von der Leuchtqualle – mit einem seltsamen Trank wird daraus ein Trank der Nachtsicht. */
    public static final DeferredItem<Item> GLOW_JELLY = item("glow_jelly", p -> p.component(DataComponents.LORE, ModItems.lore("glow_jelly", 1)));
    /** Leuchtköder: der Köder eines Anglerfischs, der auch nach seinem Tod weiterleuchtet. */
    public static final DeferredItem<Item> ANGLER_LURE = item("angler_lure", p -> p.rarity(Rarity.UNCOMMON)
            .component(DataComponents.LORE, ModItems.lore("angler_lure", 1)));
    /** Haizahn: dreieckig und scharf – mit Stock und Feder ergibt er sechs Pfeile. */
    public static final DeferredItem<Item> SHARK_TOOTH = item("shark_tooth", p -> p.component(DataComponents.LORE, ModItems.lore("shark_tooth", 1)));
    /** Krabbenschere: in der Zweithand greift man anderthalb Blöcke weiter (und einen Block weiter nach Wesen). */
    public static final DeferredItem<Item> CRAB_CLAW = item("crab_claw", p -> p.stacksTo(1).rarity(Rarity.UNCOMMON)
            .component(DataComponents.LORE, ModItems.lore("crab_claw", 1))
            .attributes(ItemAttributeModifiers.builder()
                    .add(Attributes.BLOCK_INTERACTION_RANGE, new AttributeModifier(KingdomsAtWar.id("crab_claw_block_reach"), 1.5,
                            AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.OFFHAND)
                    .add(Attributes.ENTITY_INTERACTION_RANGE, new AttributeModifier(KingdomsAtWar.id("crab_claw_entity_reach"), 1.0,
                            AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.OFFHAND)
                    .build()));
    public static final DeferredItem<Item> CRAB_MEAT = food("crab_meat", 2, 0.3f);
    public static final DeferredItem<Item> COOKED_CRAB_MEAT = food("cooked_crab_meat", 6, 0.8f);
    /** Eimer mit Leuchtqualle: setzt sie wieder ins Wasser (behält ihre Farbe). */
    public static final DeferredItem<MobBucketItem> GLOW_JELLYFISH_BUCKET = add(ModItems.ITEMS.registerItem("glow_jellyfish_bucket",
            p -> new MobBucketItem(ModEntities.GLOW_JELLYFISH.get(), Fluids.WATER, SoundEvents.BUCKET_EMPTY_FISH, p),
            () -> new Item.Properties().stacksTo(1).component(DataComponents.BUCKET_ENTITY_DATA, CustomData.EMPTY)));

    // ---------------------------------------------------------------- Spawn-Eier der Meereswesen
    public static final DeferredItem<SpawnEggItem> MANTA_RAY_SPAWN_EGG = egg("manta_ray", () -> ModEntities.MANTA_RAY.get());
    public static final DeferredItem<SpawnEggItem> GLOW_JELLYFISH_SPAWN_EGG = egg("glow_jellyfish", () -> ModEntities.GLOW_JELLYFISH.get());
    public static final DeferredItem<SpawnEggItem> SHARK_SPAWN_EGG = egg("shark", () -> ModEntities.SHARK.get());
    public static final DeferredItem<SpawnEggItem> ANGLERFISH_SPAWN_EGG = egg("anglerfish", () -> ModEntities.ANGLERFISH.get());
    public static final DeferredItem<SpawnEggItem> GIANT_CRAB_SPAWN_EGG = egg("giant_crab", () -> ModEntities.GIANT_CRAB.get());

    public static List<DeferredItem<SpawnEggItem>> spawnEggs() {
        return List.of(MANTA_RAY_SPAWN_EGG, GLOW_JELLYFISH_SPAWN_EGG, SHARK_SPAWN_EGG, ANGLERFISH_SPAWN_EGG, GIANT_CRAB_SPAWN_EGG);
    }

    /** Alle schlichten Gegenstände (flaches Modell). */
    public static List<DeferredItem<? extends Item>> flatItems() {
        return List.of(GLOW_JELLY, ANGLER_LURE, SHARK_TOOTH, CRAB_CLAW, CRAB_MEAT, COOKED_CRAB_MEAT, GLOW_JELLYFISH_BUCKET);
    }

    private static DeferredItem<SpawnEggItem> egg(String name, Supplier<EntityType<? extends Mob>> type) {
        return add(ModItems.ITEMS.registerItem(name + "_spawn_egg", SpawnEggItem::new, p -> p.spawnEgg(type.get())));
    }

    private static DeferredItem<Item> item(String name, UnaryOperator<Item.Properties> properties) {
        return add(ModItems.ITEMS.registerSimpleItem(name, properties));
    }

    private static DeferredItem<Item> food(String name, int nutrition, float saturation) {
        FoodProperties food = new FoodProperties.Builder().nutrition(nutrition).saturationModifier(saturation).build();
        return item(name, p -> p.food(food));
    }

    private static <T extends Item> DeferredItem<T> add(DeferredItem<T> item) {
        TAB.add(item);
        return item;
    }
}
