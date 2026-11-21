package io.github.sirschrolli.kingdomsatwar.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

/**
 * Seltenere Monsterbeute (seit 1.3): Was Monster fallen lassen (Knochen, Fäden, Schwarzpulver, verrottetes Fleisch,
 * Schattenessenz, Nachtkristallsplitter …), fällt mit der Wahrscheinlichkeit {@code drop} ganz weg. Unberührt bleibt, was
 * man für den Fortgang braucht (Lohenruten, Enderperlen, Ghasttränen, Shulkerschalen, Witherskelett-Schädel, Totems,
 * Windruten …) und alles Seltene; die Münzen und die Teile der eigenen Monster (Werwolfspelz, Moorwurzel …) sind schon in
 * ihren Beute-Tabellen seltener.
 */
public class LeanDropsLootModifier extends LootModifier {
    public static final MapCodec<LeanDropsLootModifier> CODEC = RecordCodecBuilder.mapCodec(i -> codecStart(i).and(
            Codec.floatRange(0.0f, 1.0f).fieldOf("drop").forGetter(m -> m.drop)
    ).apply(i, LeanDropsLootModifier::new));

    private final float drop;

    public LeanDropsLootModifier(Optional<Holder<LootItemCondition>> condition, int priority, float drop) {
        super(condition, priority);
        this.drop = drop;
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
        if (loot.isEmpty()) {
            return loot;
        }
        RandomSource random = context.getRandom();
        ObjectArrayList<ItemStack> kept = new ObjectArrayList<>();
        for (ItemStack stack : loot) {
            if (spared(stack) || random.nextFloat() >= this.drop) {
                kept.add(stack);
            }
        }
        return kept;
    }

    /** Bleibt immer: was man für den Fortgang braucht, Seltenes, Verzaubertes, Münzen und die Teile der eigenen Monster. */
    static boolean spared(ItemStack stack) {
        if (LeanLootModifier.needed(stack) || stack.is(Items.BLAZE_ROD) || stack.is(Items.ENDER_PEARL) || stack.is(Items.GHAST_TEAR)
                || stack.is(Items.SHULKER_SHELL) || stack.is(Items.WITHER_SKELETON_SKULL) || stack.is(Items.NETHER_STAR)
                || stack.is(Items.TOTEM_OF_UNDYING) || stack.is(Items.BREEZE_ROD) || stack.is(Items.ECHO_SHARD)
                || stack.getRarity() != Rarity.COMMON || stack.isEnchanted() || stack.getItem() instanceof io.github.sirschrolli.kingdomsatwar.item.CoinItem) {
            return true;
        }
        for (var part : io.github.sirschrolli.kingdomsatwar.registry.MonsterGear.parts()) {
            if (stack.is(part.get())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return ModLootModifiers.LEAN_DROPS.get();
    }
}
