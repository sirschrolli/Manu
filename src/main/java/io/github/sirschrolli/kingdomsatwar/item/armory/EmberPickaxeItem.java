package io.github.sirschrolli.kingdomsatwar.item.armory;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

/**
 * Glutspitzhacke: eine Draconium-Picke mit glühender Ader und einem Glutstein in der goldenen Fassung. Was sie aus dem Fels
 * bricht, kommt <b>geschmolzen</b> heraus: Erze und Rohstoffe (Roheisen, Rohgold, Erzblöcke …) werden gleich zu Barren und
 * Steinen – mit Glück (Fortune) entsprechend mehr. Andere Blöcke (Stein, Erde) bleiben, wie sie sind.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public class EmberPickaxeItem extends Item {
    public EmberPickaxeItem(Properties properties) {
        super(properties);
    }

    /** Soll dieser Fund geschmolzen werden? Nur Erze und Rohstoffe. */
    public static boolean smeltable(ItemStack stack) {
        return stack.is(Tags.Items.RAW_MATERIALS) || stack.is(Tags.Items.ORES);
    }

    /** Das geschmolzene Ergebnis (mit der Anzahl des Fundes) – oder nichts, wenn es kein Rezept gibt. */
    public static Optional<ItemStack> smelt(ServerLevel level, ItemStack stack) {
        if (stack.isEmpty() || !smeltable(stack)) {
            return Optional.empty();
        }
        SingleRecipeInput input = new SingleRecipeInput(stack.copyWithCount(1));
        return level.getServer().getRecipeManager().getRecipeFor(RecipeType.SMELTING, input, level)
                .map(holder -> holder.value().assemble(input))
                .filter(result -> !result.isEmpty())
                .map(result -> result.copyWithCount(Math.min(result.getMaxStackSize(), result.getCount() * stack.getCount())));
    }

    @SubscribeEvent
    static void onDrops(BlockDropsEvent event) {
        if (!(event.getTool().getItem() instanceof EmberPickaxeItem)) {
            return;
        }
        ServerLevel level = event.getLevel();
        boolean any = false;
        for (ItemEntity drop : event.getDrops()) {
            Optional<ItemStack> smelted = smelt(level, drop.getItem());
            if (smelted.isPresent()) {
                drop.setItem(smelted.get());
                any = true;
            }
        }
        if (any) {
            BlockPos pos = event.getPos();
            level.sendParticles(ParticleTypes.FLAME, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 10, 0.3, 0.3, 0.3, 0.02);
            level.sendParticles(ParticleTypes.LAVA, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 2, 0.2, 0.2, 0.2, 0.0);
            level.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 0.35f, 1.4f);
        }
    }
}
