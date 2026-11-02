package io.github.sirschrolli.kingdomsatwar.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

/**
 * Karge Truhen: In allen Truhen (Beute-Tabellen {@code …:chests/…} – die der eigenen Bauwerke, Dörfer und Türme wie die
 * der Vanilla-Bauwerke, die es noch gibt) bleibt nur ein Teil dessen liegen, was gezogen wurde. Jeder Stapel fällt mit der Wahrscheinlichkeit {@code drop} ganz
 * weg, Wertvolles (Diamanten, Smaragde, Gold, Netherit, Edelsteine, verzauberte und seltene Dinge, Ausrüstung) noch öfter
 * ({@code drop_valuable}); die übrigen Stapel schrumpfen auf {@code keep} ihrer Größe. Ganz leer bleibt keine Truhe, und
 * was man für den Fortgang braucht, wird nie genommen: das Herz des Meeres, die Netherit- und die Abyssit-Vorlage, der
 * Drachenkompass, die Zaubertafeln und die Prüfungsschlüssel.
 */
public class LeanLootModifier extends LootModifier {
    public static final MapCodec<LeanLootModifier> CODEC = RecordCodecBuilder.mapCodec(i -> codecStart(i).and(i.group(
            Codec.floatRange(0.0f, 1.0f).fieldOf("drop").forGetter(m -> m.drop),
            Codec.floatRange(0.0f, 1.0f).fieldOf("drop_valuable").forGetter(m -> m.dropValuable),
            Codec.floatRange(0.0f, 1.0f).fieldOf("keep").forGetter(m -> m.keep),
            Codec.STRING.fieldOf("prefix").forGetter(m -> m.prefix)
    )).apply(i, LeanLootModifier::new));

    private final float drop;
    private final float dropValuable;
    private final float keep;
    private final String prefix;

    public LeanLootModifier(Optional<Holder<LootItemCondition>> condition, int priority, float drop, float dropValuable, float keep, String prefix) {
        super(condition, priority);
        this.drop = drop;
        this.dropValuable = dropValuable;
        this.keep = keep;
        this.prefix = prefix;
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
        Identifier id = context.getQueriedLootTableId();
        if (id == null || !id.getPath().startsWith(this.prefix) || loot.isEmpty()) {
            return loot;
        }
        RandomSource random = context.getRandom();
        ObjectArrayList<ItemStack> kept = new ObjectArrayList<>();
        for (ItemStack stack : loot) {
            if (needed(stack)) {
                kept.add(stack);
                continue;
            }
            if (random.nextFloat() < (valuable(stack) ? this.dropValuable : this.drop)) {
                continue;
            }
            if (stack.getCount() > 1) {
                stack.setCount(Math.max(1, Math.round(stack.getCount() * this.keep)));
            }
            kept.add(stack);
        }
        if (kept.isEmpty()) {
            // nie ganz leer: das Bescheidenste bleibt, und zwar nur eines
            ItemStack least = loot.stream().filter(s -> !valuable(s)).findFirst().orElse(loot.getFirst()).copy();
            least.setCount(1);
            kept.add(least);
        }
        return kept;
    }

    /** Was man für den Fortgang braucht und anders kaum bekommt. */
    static boolean needed(ItemStack stack) {
        if (stack.is(Items.HEART_OF_THE_SEA) || stack.is(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE) || stack.is(Items.TRIAL_KEY)
                || stack.is(Items.OMINOUS_TRIAL_KEY) || stack.is(ModItems.DRAGON_COMPASS.get())
                || stack.is(io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.ABYSSITE_UPGRADE_SMITHING_TEMPLATE.get())) {
            return true;
        }
        for (var tablet : ModItems.SPELL_TABLETS.values()) {
            if (stack.is(tablet.get())) {
                return true;
            }
        }
        return false;
    }

    /** Wertvoll: Edelsteine und Edelmetall, Verzaubertes, Seltenes und Ausrüstung. */
    static boolean valuable(ItemStack stack) {
        return stack.is(Items.DIAMOND) || stack.is(Items.EMERALD) || stack.is(Items.GOLD_INGOT) || stack.is(Items.GOLD_BLOCK)
                || stack.is(Items.NETHERITE_SCRAP) || stack.is(Items.NETHERITE_INGOT) || stack.is(Items.ENCHANTED_BOOK)
                || stack.is(Items.GOLDEN_APPLE) || stack.is(Items.ENCHANTED_GOLDEN_APPLE) || stack.is(Items.EXPERIENCE_BOTTLE)
                || stack.is(Items.DIAMOND_BLOCK) || stack.is(Items.EMERALD_BLOCK) || stack.is(ModItems.GEMSTONE.get())
                || stack.isEnchanted() || stack.isDamageableItem() || stack.getRarity() != Rarity.COMMON
                || stack.has(DataComponents.STORED_ENCHANTMENTS);
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return ModLootModifiers.LEAN.get();
    }
}
