package io.github.sirschrolli.kingdomsatwar.army;

import io.github.sirschrolli.kingdomsatwar.block.RecruitmentTableBlock;
import io.github.sirschrolli.kingdomsatwar.economy.Coins;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Der Feldherrentisch: Eigene Soldaten im Feldlager um den Tisch werden hier geheilt (der Feldscher nimmt ein paar
 * Münzen) und ausgebildet – sie steigen gegen Sold und Ausrüstung im Rang (Rekrut → Veteran → Elite) oder werden zu einer
 * stärkeren Art (Schwertkämpfer → Ritter → Hauptmann).
 */
public final class Training {
    /** So weit reicht das Feldlager um den Tisch: Soldaten darin lassen sich heilen und ausbilden. */
    public static final double CAMP_RANGE = 24.0;
    /** Der Feldscher nimmt eine Bronzemünze je vier fehlende Lebenspunkte (je Soldat aufgerundet). */
    public static final int HEALTH_PER_COIN = 4;

    /** Was am Tisch geschieht (Reihenfolge = Nummer im Netzwerk). */
    public enum Action {
        RANK, PROMOTE
    }

    /** Ein Preis: Münzen (in Bronze) und Ausrüstung. */
    public record Cost(int coins, List<Part> items) {
        public List<ItemStack> stacks() {
            return this.items.stream().map(p -> new ItemStack(p.item().get(), p.count())).toList();
        }
    }

    public record Part(Supplier<Item> item, int count) {}

    private Training() {}

    /** Was der nächste Rang kostet ({@code null}: schon Elite). */
    public static @Nullable Cost rankCost(SoldierType type, int rank) {
        if (rank >= SoldierEntity.MAX_RANK) {
            return null;
        }
        int coins = round(type.coins() * (rank == 0 ? 0.5 : 1.0));
        return rank == 0
                ? new Cost(coins, List.of(new Part(() -> Items.IRON_INGOT, 3)))
                : new Cost(coins, List.of(new Part(() -> ModItems.STEEL_INGOT.get(), 2)));
    }

    /** Zu welcher Art sich ein Soldat ausbilden lässt ({@code null}: zu keiner). */
    public static @Nullable SoldierType promotion(SoldierType type) {
        return switch (type) {
            case SWORDSMAN -> SoldierType.KNIGHT;
            case KNIGHT -> SoldierType.CAPTAIN;
            default -> null;
        };
    }

    /** Was die Ausbildung zur nächsten Art kostet ({@code null}: keine möglich). */
    public static @Nullable Cost promoteCost(SoldierType type) {
        SoldierType next = promotion(type);
        if (next == null) {
            return null;
        }
        return next == SoldierType.KNIGHT
                ? new Cost(81, List.of(new Part(() -> ModItems.STEEL_INGOT.get(), 2)))
                : new Cost(162, List.of(new Part(() -> ModItems.GEMSTONE.get(), 1)));
    }

    private static int round(double coins) {
        return (int) Math.ceil(coins / 9.0) * 9;
    }

    public static boolean canAfford(Player player, Cost cost) {
        if (player.hasInfiniteMaterials()) {
            return true;
        }
        if (!Coins.canAfford(player, cost.coins())) {
            return false;
        }
        for (Part part : cost.items()) {
            if (player.getInventory().countItem(part.item().get()) < part.count()) {
                return false;
            }
        }
        return true;
    }

    private static void pay(Player player, Cost cost) {
        if (player.hasInfiniteMaterials()) {
            return;
        }
        Coins.pay(player, cost.coins());
        for (Part part : cost.items()) {
            player.getInventory().clearOrCountMatchingItems(stack -> stack.is(part.item().get()), false, part.count(),
                    player.inventoryMenu.getCraftSlots());
        }
    }

    /** Steht dieser Soldat des Spielers im Feldlager um den Tisch (darf er hier geheilt und ausgebildet werden)? */
    public static boolean atTable(Player player, BlockPos table, SoldierEntity soldier) {
        return soldier.isAlive() && soldier.isOwnedBy(player) && soldier.distanceToSqr(Vec3.atCenterOf(table)) <= CAMP_RANGE * CAMP_RANGE;
    }

    /** Die eigenen Soldaten im Feldlager um den Tisch. */
    public static List<SoldierEntity> camp(Player player, BlockPos table) {
        return player.level().getEntitiesOfClass(SoldierEntity.class, new AABB(table).inflate(CAMP_RANGE),
                soldier -> atTable(player, table, soldier));
    }

    /** Was der Feldscher für diesen Soldaten nimmt (in Bronze; 0, wenn er unverletzt ist). */
    public static int healCost(SoldierEntity soldier) {
        float missing = soldier.getMaxHealth() - soldier.getHealth();
        if (missing > 0.01f) {
            return Math.max(1, Mth.ceil(missing / HEALTH_PER_COIN));
        }
        return hurting(soldier) ? 1 : 0;                                      // nur Gift, Schwäche … loswerden
    }

    /** Was das Heilen aller dieser Soldaten kostet. */
    public static int healCost(List<SoldierEntity> soldiers) {
        int coins = 0;
        for (SoldierEntity soldier : soldiers) {
            coins += healCost(soldier);
        }
        return coins;
    }

    private static boolean hurting(SoldierEntity soldier) {
        return soldier.getActiveEffects().stream().anyMatch(effect -> effect.getEffect().value().getCategory() == MobEffectCategory.HARMFUL)
                || soldier.isOnFire();
    }

    /**
     * Heilt alle verwundeten eigenen Soldaten im Feldlager: volle Gesundheit, Gift, Schwäche und Feuer sind fort. Der
     * Feldscher will dafür seinen Lohn (siehe {@link #healCost(SoldierEntity)}).
     */
    public static Recruitment.Result healAll(Player player, BlockPos table) {
        if (!(player.level() instanceof ServerLevel level) || !(level.getBlockState(table).getBlock() instanceof RecruitmentTableBlock)
                || player.distanceToSqr(Vec3.atCenterOf(table)) > Recruitment.TABLE_RANGE * Recruitment.TABLE_RANGE) {
            return Recruitment.Result.TOO_FAR;
        }
        List<SoldierEntity> wounded = camp(player, table).stream().filter(soldier -> healCost(soldier) > 0).toList();
        if (wounded.isEmpty()) {
            return Recruitment.Result.NOTHING_TO_HEAL;
        }
        int coins = healCost(wounded);
        if (!player.hasInfiniteMaterials()) {
            if (!Coins.canAfford(player, coins)) {
                return Recruitment.Result.NOT_ENOUGH;
            }
            Coins.pay(player, coins);
        }
        for (SoldierEntity soldier : wounded) {
            soldier.setHealth(soldier.getMaxHealth());
            for (MobEffectInstance effect : List.copyOf(soldier.getActiveEffects())) {
                if (effect.getEffect().value().getCategory() == MobEffectCategory.HARMFUL) {
                    soldier.removeEffect(effect.getEffect());
                }
            }
            soldier.clearFire();
            level.sendParticles(ParticleTypes.HEART, soldier.getX(), soldier.getY() + 1.4, soldier.getZ(), 6, 0.4, 0.3, 0.4, 0.0);
            level.playSound(null, soldier.blockPosition(), SoundEvents.ARMOR_EQUIP_LEATHER.value(), SoundSource.NEUTRAL, 0.8f, 1.1f);
            soldier.salute();
        }
        level.playSound(null, table, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 0.4f, 1.6f);
        return Recruitment.Result.HEALED;
    }

    /** Bildet einen Soldaten aus: prüft Tisch, Besitzer, Entfernung und Preis, zieht den Preis ab und befördert ihn. */
    public static Recruitment.Result train(Player player, BlockPos table, int entityId, Action action) {
        if (!(player.level() instanceof ServerLevel level) || !(level.getBlockState(table).getBlock() instanceof RecruitmentTableBlock)
                || player.distanceToSqr(Vec3.atCenterOf(table)) > Recruitment.TABLE_RANGE * Recruitment.TABLE_RANGE) {
            return Recruitment.Result.TOO_FAR;
        }
        Entity entity = level.getEntity(entityId);
        if (!(entity instanceof SoldierEntity soldier) || !atTable(player, table, soldier)) {
            return Recruitment.Result.NOT_HERE;
        }
        if (action == Action.RANK) {
            Cost cost = rankCost(soldier.getSoldierType(), soldier.getRank());
            if (cost == null) {
                return Recruitment.Result.MAX_RANK;
            }
            if (!canAfford(player, cost)) {
                return Recruitment.Result.NOT_ENOUGH;
            }
            pay(player, cost);
            soldier.setRank(soldier.getRank() + 1);
            soldier.setHealth(soldier.getMaxHealth());
            celebrate(level, soldier);
            return Recruitment.Result.TRAINED;
        }
        SoldierType next = promotion(soldier.getSoldierType());
        Cost cost = promoteCost(soldier.getSoldierType());
        if (next == null || cost == null) {
            return Recruitment.Result.MAX_RANK;
        }
        if (!canAfford(player, cost)) {
            return Recruitment.Result.NOT_ENOUGH;
        }
        SoldierEntity fresh = promote(level, soldier, next, player);
        if (fresh == null) {
            return Recruitment.Result.NO_SPACE;
        }
        pay(player, cost);
        celebrate(level, fresh);
        return Recruitment.Result.PROMOTED;
    }

    /**
     * Ersetzt den Soldaten durch einen der neuen Art an derselben Stelle – mit Herrn, Name, Gesicht, Rang, Befehl und
     * anteiligem Leben. Waffen, die ihm der Spieler gegeben hatte, bekommt der Spieler zurück.
     */
    public static @Nullable SoldierEntity promote(ServerLevel level, SoldierEntity old, SoldierType next, @Nullable Player player) {
        SoldierEntity fresh = ModEntities.SOLDIERS.get(next).get().create(level, EntitySpawnReason.MOB_SUMMONED);
        if (fresh == null) {
            return null;
        }
        fresh.snapTo(old.getX(), old.getY(), old.getZ(), old.getYRot(), old.getXRot());
        fresh.setYHeadRot(old.getYHeadRot());
        fresh.setYBodyRot(old.yBodyRot);
        fresh.equip();
        fresh.copyPersonFrom(old);
        for (EquipmentSlot slot : new EquipmentSlot[] {EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND}) {
            ItemStack gear = old.getItemBySlot(slot);
            if (!gear.isEmpty() && old.getDropChances().byEquipment(slot) > 1.0f) {
                if (player != null) {
                    player.getInventory().placeItemBackInInventory(gear.copy(), net.minecraft.util.Prediction.SERVER_ONLY);
                } else {
                    old.spawnAtLocation(level, gear.copy());
                }
            }
        }
        old.discard();
        level.addFreshEntity(fresh);
        return fresh;
    }

    private static void celebrate(ServerLevel level, SoldierEntity soldier) {
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, soldier.getX(), soldier.getY() + 1.0, soldier.getZ(), 16, 0.4, 0.7, 0.4, 0.0);
        level.sendParticles(ParticleTypes.ENCHANT, soldier.getX(), soldier.getY() + 1.6, soldier.getZ(), 24, 0.4, 0.4, 0.4, 0.4);
        level.playSound(null, soldier.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.NEUTRAL, 0.7f, 1.2f);
        level.playSound(null, soldier.blockPosition(), SoundEvents.ARMOR_EQUIP_NETHERITE.value(), SoundSource.NEUTRAL, 1.0f, 1.0f);
        soldier.salute();
        soldier.speak("recruit");
        if (soldier.getOwner() instanceof ServerPlayer owner) {
            owner.sendOverlayMessage(net.minecraft.network.chat.Component.translatable("message.kingdomsatwar.soldier_trained",
                    soldier.getDisplayName()).withStyle(net.minecraft.ChatFormatting.GOLD));
        }
    }
}
