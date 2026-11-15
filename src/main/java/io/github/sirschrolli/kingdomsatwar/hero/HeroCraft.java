package io.github.sirschrolli.kingdomsatwar.hero;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import org.jspecify.annotations.Nullable;

/**
 * Was die Talente des Handwerks und das Bogenschießen in der Welt bewirken (seit 1.11):
 *
 * <ul>
 *   <li><b>Bergbau:</b> Die Spitzhacke baut schneller ab; in der Meisterschaft gibt ein Erz mitunter ein zweites.</li>
 *   <li><b>Holzfällen:</b> Die Axt fällt Stämme schneller; in der Meisterschaft fällt mitunter ein Stamm mehr.</li>
 *   <li><b>Ackerbau:</b> Reife Feldfrüchte (auch Netherwarzen und Kakao) geben mitunter eine Ernte mehr; in der
 *       Meisterschaft sät der Held mitunter gleich wieder aus (mit einem Samen aus der Ernte).</li>
 *   <li><b>Bogenschießen:</b> Pfeile aus Bogen und Armbrust treffen härter; in der Meisterschaft wird mitunter ein Pfeil
 *       nicht verbraucht.</li>
 * </ul>
 * Mit Behutsamkeit (oder ohne das richtige Werkzeug) gibt es kein zweites Erz – sonst ließe sich dasselbe Erz immer wieder
 * setzen und abbauen.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class HeroCraft {
    private HeroCraft() {}

    @SubscribeEvent
    static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        BlockState state = event.getState();
        ItemStack tool = player.getMainHandItem();
        float bonus = 0.0f;
        if (state.is(BlockTags.LOGS) && tool.is(ItemTags.AXES)) {
            bonus = HeroLevel.woodSpeed(player);
        } else if (state.is(BlockTags.MINEABLE_WITH_PICKAXE) && tool.is(ItemTags.PICKAXES)) {
            bonus = HeroLevel.miningSpeed(player);
        }
        if (bonus > 0.0f) {
            event.setNewSpeed(event.getNewSpeed() * (1.0f + bonus));
        }
    }

    @SubscribeEvent
    static void onDrops(BlockDropsEvent event) {
        if (!(event.getBreaker() instanceof ServerPlayer player) || player.isSpectator() || event.getDrops().isEmpty()) {
            return;
        }
        ServerLevel level = event.getLevel();
        BlockState state = event.getState();
        BlockPos pos = event.getPos();
        List<ItemEntity> drops = event.getDrops();
        RandomSource random = level.getRandom();
        if (state.is(Tags.Blocks.ORES)) {
            if (!silkTouch(level, event.getTool()) && random.nextFloat() < HeroLevel.miningDouble(player)) {
                extra(level, pos, drops, drops.getFirst().getItem().getItem());
            }
        } else if (state.is(BlockTags.LOGS)) {
            if (random.nextFloat() < HeroLevel.woodExtra(player)) {
                extra(level, pos, drops, drops.getFirst().getItem().getItem());
            }
        } else if (ripe(state)) {
            Item seed = state.getBlock().asItem();
            if (random.nextFloat() < HeroLevel.farmingExtra(player)) {
                extra(level, pos, drops, produce(drops, seed));
            }
            if (random.nextFloat() < HeroLevel.farmingReplant(player)) {
                replant(level, pos, state, drops, seed);
            }
        }
    }

    /**
     * Bogenschießen: Ein Pfeil aus Bogen oder Armbrust eines Helden trifft härter (der Schaden steht fest, wenn der Pfeil
     * trifft – so wirkt der Bonus auch auf kritische Treffer und Durchschuss).
     */
    @SubscribeEvent
    static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getSource().getDirectEntity() instanceof AbstractArrow arrow && fromBow(arrow) && arrow.getOwner() instanceof Player player) {
            float bonus = HeroLevel.archeryBonus(player);
            if (bonus > 0.0f) {
                event.setAmount(event.getAmount() * (1.0f + bonus));
            }
        }
    }

    /** Bogenschießen in der Meisterschaft: Mitunter bekommt der Schütze den Pfeil gleich zurück (aufheben kann man ihn dann nicht). */
    @SubscribeEvent
    static void onArrow(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || event.loadedFromDisk() || !(event.getEntity() instanceof AbstractArrow arrow) || !fromBow(arrow)
                || !(arrow.getOwner() instanceof ServerPlayer player) || player.hasInfiniteMaterials()
                || arrow.pickup != AbstractArrow.Pickup.ALLOWED) {
            return;
        }
        float save = HeroLevel.archerySave(player);
        if (save > 0.0f && player.getRandom().nextFloat() < save) {
            ItemStack back = arrow.getPickupItemStackOrigin().copyWithCount(1);
            if (!back.isEmpty() && player.getInventory().add(back)) {
                arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            }
        }
    }

    /** Ein Pfeil (kein Dreizack, kein Netz) aus Bogen oder Armbrust? */
    static boolean fromBow(AbstractArrow arrow) {
        ItemStack weapon = arrow.getWeaponItem();
        return weapon != null && weapon.getItem() instanceof ProjectileWeaponItem;
    }

    private static boolean silkTouch(ServerLevel level, ItemStack tool) {
        Holder<Enchantment> silk = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH);
        return EnchantmentHelper.getItemEnchantmentLevel(silk, tool) > 0;
    }

    /** Eine reife Feldfrucht? */
    static boolean ripe(BlockState state) {
        Block block = state.getBlock();
        if (block instanceof CropBlock crop) {
            return crop.isMaxAge(state);
        }
        if (block instanceof NetherWartBlock) {
            return state.getValue(NetherWartBlock.AGE) >= NetherWartBlock.MAX_AGE;
        }
        if (block instanceof CocoaBlock) {
            return state.getValue(CocoaBlock.AGE) >= CocoaBlock.MAX_AGE;
        }
        return false;
    }

    /** Die eigentliche Ernte: was kein Same ist (Weizen statt Weizenkörner) – bei Karotten und Kartoffeln die Frucht selbst. */
    private static Item produce(List<ItemEntity> drops, Item seed) {
        for (ItemEntity drop : drops) {
            if (!drop.getItem().is(seed)) {
                return drop.getItem().getItem();
            }
        }
        return seed;
    }

    /** Ein Stück mehr von {@code item}, wo die anderen Beutestücke liegen. */
    private static void extra(ServerLevel level, BlockPos pos, List<ItemEntity> drops, Item item) {
        ItemEntity first = drops.getFirst();
        ItemEntity more = new ItemEntity(level, first.getX(), first.getY(), first.getZ(), new ItemStack(item));
        more.setDefaultPickUpDelay();
        drops.add(more);
    }

    /** Sät gleich wieder aus (die junge Pflanze), wenn ein Same in der Ernte ist und sie dort wachsen kann. */
    private static void replant(ServerLevel level, BlockPos pos, BlockState ripe, List<ItemEntity> drops, Item seed) {
        BlockState young = young(ripe);
        if (young == null || !level.getBlockState(pos).isAir() || !young.canSurvive(level, pos)) {
            return;
        }
        for (ItemEntity drop : drops) {
            if (drop.getItem().is(seed)) {
                drop.getItem().shrink(1);
                if (drop.getItem().isEmpty()) {
                    drops.remove(drop);
                }
                level.setBlock(pos, young, Block.UPDATE_ALL);
                return;
            }
        }
    }

    private static @Nullable BlockState young(BlockState ripe) {
        Block block = ripe.getBlock();
        if (block instanceof CropBlock crop) {
            return crop.getStateForAge(0);
        }
        if (block instanceof NetherWartBlock) {
            return ripe.setValue(NetherWartBlock.AGE, 0);
        }
        if (block instanceof CocoaBlock) {
            return ripe.setValue(CocoaBlock.AGE, 0);
        }
        return null;
    }
}
