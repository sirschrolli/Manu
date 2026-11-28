package io.github.sirschrolli.kingdomsatwar.registry;

import io.github.sirschrolli.kingdomsatwar.fishing.FishItem;
import io.github.sirschrolli.kingdomsatwar.fishing.FishSpecies;
import io.github.sirschrolli.kingdomsatwar.fishing.ModFishingRodItem;
import io.github.sirschrolli.kingdomsatwar.fishing.RodType;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.RemoveStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.TeleportRandomlyConsumeEffect;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * Das Angeln (seit 0.79): ein Gegenstand je Fischart (die vier Fische von Minecraft bleiben, was sie sind) – seit 0.81 sind es
 * 996 –, das gebratene Fischfilet, die Köder und der Fischbeutel; seit 0.87 sechs eigene Angeln.
 */
public final class FishingItems {
    /** Reihenfolge im Kreativ-Reiter „Angeln“. */
    static final List<Supplier<? extends ItemLike>> TAB = new ArrayList<>();
    private static final Map<FishSpecies, DeferredItem<FishItem>> FISH = new LinkedHashMap<>();
    private static final Map<RodType, DeferredItem<ModFishingRodItem>> RODS = new EnumMap<>(RodType.class);

    // die Angeln (seit 0.87) stehen vorn im Reiter
    static {
        for (RodType type : RodType.values()) {
            RODS.put(type, add(ModItems.ITEMS.registerItem(type.id(), p -> new ModFishingRodItem(type, p), p -> rodProperties(type, p))));
        }
    }

    /** Köderwurm: lockt die häufigen Fische der Flüsse und Seen, der Biss kommt schneller. */
    public static final DeferredItem<Item> BAIT_WORM = add(ModItems.ITEMS.registerSimpleItem("bait_worm",
            p -> p.component(DataComponents.LORE, ModItems.lore("bait_worm", 2))));
    /** Glühköder: lockt, was im Dunkeln lebt – Nachtfische, Tiefsee, Höhlen, Lava. */
    public static final DeferredItem<Item> BAIT_GLOW = add(ModItems.ITEMS.registerSimpleItem("bait_glow",
            p -> p.component(DataComponents.LORE, ModItems.lore("bait_glow", 2))));
    /** Gebratenes Fischfilet: aus jedem der eigenen Fische (Ofen, Räucherofen, Lagerfeuer) – sättigt wie gebratener Lachs. */
    public static final DeferredItem<Item> COOKED_FILLET = add(ModItems.ITEMS.registerSimpleItem("fish_fillet_cooked",
            p -> p.food(new FoodProperties.Builder().nutrition(6).saturationModifier(0.8f).build())));

    /**
     * Fischbeutel (seit 0.81): Der Fang wandert hinein, im Fenster des Beutels nimmt man ihn wieder heraus. Fasst 64 Fische und
     * mit jeder Stufe 32 mehr; die Stufen verkauft der Fischer im Dorf.
     */
    public static final DeferredItem<io.github.sirschrolli.kingdomsatwar.fishing.FishBagItem> FISH_BAG = add(ModItems.ITEMS.registerItem("fish_bag",
            io.github.sirschrolli.kingdomsatwar.fishing.FishBagItem::new,
            p -> p.stacksTo(1).component(ModDataComponents.FISH_BAG.get(), io.github.sirschrolli.kingdomsatwar.fishing.FishBag.EMPTY)));

    static {
        for (FishSpecies species : FishSpecies.values()) {
            if (!species.isVanilla()) {
                FISH.put(species, add(ModItems.ITEMS.registerItem("fish_" + species.id(), p -> new FishItem(species, p), p -> properties(species, p))));
            }
        }
    }

    private FishingItems() {}

    public static void init() {}

    /** Der Gegenstand einer eigenen Fischart. */
    public static Item fish(FishSpecies species) {
        return FISH.get(species).get();
    }

    /** Eine Angel der Mod. */
    public static ModFishingRodItem rod(RodType type) {
        return RODS.get(type).get();
    }

    /** Alle Angeln der Mod (für Modelle, Rezepte und Merkmale). */
    public static List<DeferredItem<ModFishingRodItem>> rods() {
        return List.copyOf(RODS.values());
    }

    /** Alle eigenen Fische (für Modelle, Rezepte und Merkmale). */
    public static List<DeferredItem<FishItem>> fishes() {
        return List.copyOf(FISH.values());
    }

    /** Alle schlichten Gegenstände neben den Fischen (flaches Modell). */
    public static List<DeferredItem<? extends Item>> flatItems() {
        return List.of(BAIT_WORM, BAIT_GLOW, COOKED_FILLET);
    }

    private static Item.Properties rodProperties(RodType type, Item.Properties properties) {
        properties.durability(type.durability()).enchantable(type.enchantability()).repairable(ModTags.Items.rodRepair(type))
                .rarity(type.rarity()).component(DataComponents.LORE, ModItems.lore(type.id(), 2));
        if (type.fireproof()) {
            properties.fireResistant();
        }
        return properties;
    }

    private static Item.Properties properties(FishSpecies species, Item.Properties properties) {
        FoodProperties food = new FoodProperties.Builder().nutrition(species.nutrition()).saturationModifier(species.saturation()).alwaysEdible().build();
        Consumable.Builder eat = Consumables.defaultFood();
        List<MobEffectInstance> effects = new ArrayList<>();
        for (FishSpecies.Bite bite : species.bites()) {
            effects.add(new MobEffectInstance(bite.effect(), Math.max(1, bite.seconds() * 20), bite.amplifier()));
        }
        if (!effects.isEmpty()) {
            eat.onConsume(new ApplyStatusEffectsConsumeEffect(effects));
        }
        switch (species.special()) {
            case TELEPORT_FAR -> eat.onConsume(new TeleportRandomlyConsumeEffect(16.0f, true));
            case TELEPORT_NEAR -> eat.onConsume(new TeleportRandomlyConsumeEffect(6.0f, true));
            case CLEANSE -> eat.onConsume(new RemoveStatusEffectsConsumeEffect(HolderSet.direct(MobEffects.POISON, MobEffects.WITHER,
                    MobEffects.BLINDNESS, MobEffects.DARKNESS, MobEffects.SLOWNESS, MobEffects.WEAKNESS, MobEffects.HUNGER, MobEffects.NAUSEA,
                    MobEffects.MINING_FATIGUE, MobEffects.LEVITATION)));
            default -> { }
        }
        properties.food(food, eat.build()).rarity(switch (species.rarity()) {
            case COMMON -> Rarity.COMMON;
            case UNCOMMON -> Rarity.UNCOMMON;
            case RARE -> Rarity.RARE;
            case EPIC, LEGENDARY -> Rarity.EPIC;
        });
        if (species.ofLava()) {
            properties.fireResistant();
        }
        return properties;
    }

    private static <T extends Item> DeferredItem<T> add(DeferredItem<T> item) {
        TAB.add(item);
        return item;
    }
}
