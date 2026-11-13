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
 * Münzen) und ausgebildet – sie steigen gegen Sold und Ausrüstung im Rang (Rekrut → Veteran → Elite → Meister →
 * Legende, jeder Rang erst ab einer Stufe der Kampferfahrung) oder werden zu einer anderen Art:
 * <pre>
 *   Schwertkämpfer ─┬─ Ritter ─┬─ Hauptmann
 *                   │          ├─ Lanzenreiter
 *                   │          └─ Waffenknecht
 *                   ├─ Speerträger
 *                   ├─ Schildträger
 *                   └─ Belagerungsknecht
 *   Bogenschütze ──── Armbrustschütze
 * </pre>
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

    /**
     * Was der nächste Rang kostet ({@code null}: schon Legende): Veteran Eisen, Elite Stahl, Meister Stahl und ein
     * Edelstein, Legende Edelsteine und Draconium – und Sold, der mit jedem Rang steigt. Befördern lässt sich nur, wer die
     * Stufe dafür erreicht hat ({@link Veterancy#minLevel(int)}).
     */
    public static @Nullable Cost rankCost(SoldierType type, int rank) {
        if (rank >= SoldierEntity.MAX_RANK) {
            return null;
        }
        return switch (rank) {
            case 0 -> new Cost(round(type.coins() * 0.5), List.of(new Part(() -> Items.IRON_INGOT, 3)));
            case 1 -> new Cost(round(type.coins()), List.of(new Part(() -> ModItems.STEEL_INGOT.get(), 2)));
            case 2 -> new Cost(round(type.coins() * 1.5), List.of(new Part(() -> ModItems.STEEL_INGOT.get(), 3),
                    new Part(() -> ModItems.GEMSTONE.get(), 1)));
            default -> new Cost(round(type.coins() * 2.0), List.of(new Part(() -> ModItems.GEMSTONE.get(), 2),
                    new Part(() -> ModItems.DRACONIUM.get(), 1)));
        };
    }

    /** Hat der Soldat die Stufe für seinen nächsten Rang? */
    public static boolean readyForRank(SoldierEntity soldier) {
        return soldier.getRank() < SoldierEntity.MAX_RANK && soldier.getCombatLevel() >= Veterancy.minLevel(soldier.getRank() + 1);
    }

    /** Zu welchen Arten sich ein Soldat ausbilden lässt (die übliche zuerst; leer: zu keiner). */
    public static List<SoldierType> promotions(SoldierType type) {
        return switch (type) {
            case SWORDSMAN -> List.of(SoldierType.KNIGHT, SoldierType.SPEARMAN, SoldierType.SHIELDBEARER, SoldierType.ENGINEER);
            case KNIGHT -> List.of(SoldierType.CAPTAIN, SoldierType.LANCER, SoldierType.MAN_AT_ARMS);
            case ARCHER -> List.of(SoldierType.CROSSBOWMAN);
            default -> List.of();
        };
    }

    /** Die übliche Ausbildung ({@code null}: keine möglich). */
    public static @Nullable SoldierType promotion(SoldierType type) {
        List<SoldierType> options = promotions(type);
        return options.isEmpty() ? null : options.getFirst();
    }

    /** Was die Ausbildung von {@code from} zu {@code to} kostet ({@code null}: so nicht möglich). */
    public static @Nullable Cost promoteCost(SoldierType from, SoldierType to) {
        if (!promotions(from).contains(to)) {
            return null;
        }
        return switch (to) {
            case KNIGHT -> new Cost(81, List.of(new Part(() -> ModItems.STEEL_INGOT.get(), 2)));
            case CAPTAIN -> new Cost(162, List.of(new Part(() -> ModItems.GEMSTONE.get(), 1)));
            case SPEARMAN -> new Cost(27, List.of(new Part(() -> Items.IRON_SPEAR, 1)));
            case SHIELDBEARER -> new Cost(45, List.of(new Part(() -> Items.SHIELD, 1), new Part(() -> Items.IRON_INGOT, 2)));
            case ENGINEER -> new Cost(36, List.of(new Part(() -> Items.IRON_INGOT, 3), new Part(() -> Items.CRAFTING_TABLE, 1)));
            case CROSSBOWMAN -> new Cost(45, List.of(new Part(() -> Items.CROSSBOW, 1)));
            case LANCER -> new Cost(162, List.of(new Part(() -> Items.SADDLE, 1), new Part(() -> ModItems.STEEL_INGOT.get(), 2)));
            case MAN_AT_ARMS -> new Cost(108, List.of(new Part(() -> ModItems.STEEL_INGOT.get(), 4)));
            default -> null;
        };
    }

    /** Was die übliche Ausbildung kostet ({@code null}: keine möglich). */
    public static @Nullable Cost promoteCost(SoldierType type) {
        SoldierType next = promotion(type);
        return next == null ? null : promoteCost(type, next);
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

    /** Ein neues Schlachtross für einen Lanzenreiter, der seines verloren hat (in Bronze). */
    public static final int STEED_COST = 27;

    /**
     * Was der Feldscher für diesen Soldaten nimmt (in Bronze; 0, wenn er unverletzt ist) – für einen Lanzenreiter ohne Ross
     * kommt ein neues Schlachtross aus den Ställen dazu.
     */
    public static int healCost(SoldierEntity soldier) {
        int steed = soldier.needsSteed() ? STEED_COST : 0;
        float missing = soldier.getMaxHealth() - soldier.getHealth();
        if (missing > 0.01f) {
            return steed + Math.max(1, Mth.ceil(missing / HEALTH_PER_COIN));
        }
        return steed + (hurting(soldier) ? 1 : 0);                            // nur Gift, Schwäche … loswerden
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
            if (soldier.needsSteed()) {
                soldier.provideSteed();
            }
            level.sendParticles(ParticleTypes.HEART, soldier.getX(), soldier.getY() + 1.4, soldier.getZ(), 6, 0.4, 0.3, 0.4, 0.0);
            level.playSound(null, soldier.blockPosition(), SoundEvents.ARMOR_EQUIP_LEATHER.value(), SoundSource.NEUTRAL, 0.8f, 1.1f);
            soldier.salute();
        }
        level.playSound(null, table, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 0.4f, 1.6f);
        return Recruitment.Result.HEALED;
    }

    /** Wie {@link #train(Player, BlockPos, int, Action, SoldierType)} – die Ausbildung zur üblichen Art. */
    public static Recruitment.Result train(Player player, BlockPos table, int entityId, Action action) {
        return train(player, table, entityId, action, null);
    }

    /**
     * Bildet einen Soldaten aus: prüft Tisch, Besitzer, Entfernung und Preis, zieht den Preis ab und befördert ihn – im Rang
     * oder zur Art {@code target} ({@code null}: zur üblichen).
     */
    public static Recruitment.Result train(Player player, BlockPos table, int entityId, Action action, @Nullable SoldierType target) {
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
            if (!readyForRank(soldier)) {
                return Recruitment.Result.NEEDS_LEVEL;
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
        SoldierType next = target != null ? target : promotion(soldier.getSoldierType());
        Cost cost = next == null ? null : promoteCost(soldier.getSoldierType(), next);
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
