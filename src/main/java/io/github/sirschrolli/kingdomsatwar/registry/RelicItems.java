package io.github.sirschrolli.kingdomsatwar.registry;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.relic.PowerApple;
import io.github.sirschrolli.kingdomsatwar.relic.PowerAppleItem;
import io.github.sirschrolli.kingdomsatwar.relic.TotemItem;
import io.github.sirschrolli.kingdomsatwar.relic.TotemKind;
import io.github.sirschrolli.kingdomsatwar.relic.TotemReviveEffect;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.component.DeathProtection;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.ClearAllStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Totems und Äpfel der Macht (seit 0.80). */
public final class RelicItems {
    public static final DeferredRegister<ConsumeEffect.Type<?>> CONSUME_EFFECTS = DeferredRegister.create(Registries.CONSUME_EFFECT_TYPE,
            KingdomsAtWar.MODID);
    /** Was ein Totem der Mod beim Tod tut (als Wirkung am Gegenstand, siehe {@link TotemReviveEffect}). */
    public static final DeferredHolder<ConsumeEffect.Type<?>, ConsumeEffect.Type<TotemReviveEffect>> TOTEM_REVIVE = CONSUME_EFFECTS.register(
            "totem_revive", () -> new ConsumeEffect.Type<>(TotemReviveEffect.CODEC, TotemReviveEffect.STREAM_CODEC));

    /** Reihenfolge im Kreativ-Reiter. */
    static final List<Supplier<? extends ItemLike>> TAB = new ArrayList<>();
    private static final EnumMap<TotemKind, DeferredItem<TotemItem>> TOTEMS = new EnumMap<>(TotemKind.class);
    private static final EnumMap<PowerApple, DeferredItem<PowerAppleItem>> APPLES = new EnumMap<>(PowerApple.class);

    static {
        for (TotemKind kind : TotemKind.values()) {
            DeferredItem<TotemItem> item = ModItems.ITEMS.registerItem(kind.itemName(), p -> new TotemItem(kind, p), p -> totem(kind, p));
            TOTEMS.put(kind, item);
            TAB.add(item);
        }
        for (PowerApple apple : PowerApple.values()) {
            DeferredItem<PowerAppleItem> item = ModItems.ITEMS.registerItem(apple.id(), p -> new PowerAppleItem(apple, p), p -> apple(apple, p));
            APPLES.put(apple, item);
            TAB.add(item);
        }
    }

    private RelicItems() {}

    public static void init() {}

    public static Item totem(TotemKind kind) {
        return TOTEMS.get(kind).get();
    }

    public static Item apple(PowerApple apple) {
        return APPLES.get(apple).get();
    }

    public static List<DeferredItem<TotemItem>> totems() {
        return List.copyOf(TOTEMS.values());
    }

    public static List<DeferredItem<PowerAppleItem>> apples() {
        return List.copyOf(APPLES.values());
    }

    private static Item.Properties totem(TotemKind kind, Item.Properties properties) {
        // wie das Totem der Unsterblichkeit: erst fallen alle Zustände ab, dann wirkt das Totem
        properties.stacksTo(1).rarity(kind == TotemKind.CREATOR ? Rarity.EPIC : Rarity.RARE).fireResistant()
                .component(DataComponents.DEATH_PROTECTION, new DeathProtection(List.of(new ClearAllStatusEffectsConsumeEffect(), new TotemReviveEffect(kind))))
                .attributes(kind.attributes());
        if (kind.charges() > 1) {
            properties.durability(kind.charges());
        }
        return properties;
    }

    private static Item.Properties apple(PowerApple apple, Item.Properties properties) {
        FoodProperties food = new FoodProperties.Builder().nutrition(4).saturationModifier(1.2f).alwaysEdible().build();
        properties.food(food, Consumables.defaultFood().onConsume(new ApplyStatusEffectsConsumeEffect(apple.effects())).build())
                .rarity(Rarity.EPIC).fireResistant();
        if (apple == PowerApple.IMMORTAL) {
            // schimmert wie der verzauberte goldene Apfel
            properties.component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        }
        return properties;
    }
}
