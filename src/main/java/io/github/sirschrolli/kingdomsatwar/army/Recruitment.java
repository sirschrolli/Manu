package io.github.sirschrolli.kingdomsatwar.army;

import io.github.sirschrolli.kingdomsatwar.block.RecruitmentTableBlock;
import io.github.sirschrolli.kingdomsatwar.config.ModServerConfig;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;
import org.jspecify.annotations.Nullable;

/**
 * Anwerben am Rekrutierungstisch – alles, was der Server dabei prüft und erledigt.
 */
public final class Recruitment {
    /** Wie nah der Spieler am Tisch stehen muss. */
    public static final double TABLE_RANGE = 8.0;

    /** Ergebnis eines Anwerbe-Versuchs (Reihenfolge = Nummer im Netzwerk). */
    public enum Result {
        RECRUITED, TOO_FAR, NOT_ENOUGH, ARMY_FULL, NO_SPACE;

        public String translationKey() {
            return "message.kingdomsatwar.recruit." + this.name().toLowerCase(java.util.Locale.ROOT);
        }
    }

    private Recruitment() {}

    public static int armyLimit() {
        return ModServerConfig.SPEC.isLoaded() ? ModServerConfig.ARMY_LIMIT.get() : ModServerConfig.ARMY_LIMIT.getDefault();
    }

    /** Wie viele Soldaten der Spieler hat (in allen geladenen Dimensionen). */
    public static int armySize(ServerLevel level, UUID owner) {
        int count = 0;
        for (ServerLevel world : level.getServer().getAllLevels()) {
            count += world.getEntities(EntityTypeTest.forClass(SoldierEntity.class), soldier -> owner.equals(soldier.getOwnerId())).size();
        }
        return count;
    }

    public static boolean canAfford(Player player, SoldierType type) {
        if (player.hasInfiniteMaterials()) {
            return true;
        }
        for (SoldierType.Cost cost : type.cost()) {
            if (player.getInventory().countItem(cost.item().get()) < cost.count()) {
                return false;
            }
        }
        return true;
    }

    /** Wirbt einen Soldaten an: prüft Entfernung, Armeegröße und Preis, zieht den Preis ab und lässt ihn am Tisch erscheinen. */
    public static Result recruit(Player player, BlockPos tablePos, SoldierType type) {
        if (!(player.level() instanceof ServerLevel level)) {
            return Result.TOO_FAR;
        }
        BlockState table = level.getBlockState(tablePos);
        if (!(table.getBlock() instanceof RecruitmentTableBlock) || player.distanceToSqr(Vec3.atCenterOf(tablePos)) > TABLE_RANGE * TABLE_RANGE) {
            return Result.TOO_FAR;
        }
        if (armySize(level, player.getUUID()) >= armyLimit()) {
            return Result.ARMY_FULL;
        }
        if (!canAfford(player, type)) {
            return Result.NOT_ENOUGH;
        }
        EntityType<SoldierEntity> entityType = ModEntities.SOLDIERS.get(type).get();
        Vec3 spot = findSpot(level, tablePos, table.getValue(RecruitmentTableBlock.FACING), entityType);
        if (spot == null) {
            return Result.NO_SPACE;
        }
        SoldierEntity soldier = entityType.create(level, EntitySpawnReason.MOB_SUMMONED);
        if (soldier == null) {
            return Result.NO_SPACE;
        }
        if (!player.hasInfiniteMaterials()) {
            for (SoldierType.Cost cost : type.cost()) {
                player.getInventory().clearOrCountMatchingItems(stack -> stack.is(cost.item().get()), false, cost.count(),
                        player.inventoryMenu.getCraftSlots());
            }
        }
        float yaw = (float) (Mth.atan2(player.getZ() - spot.z, player.getX() - spot.x) * Mth.RAD_TO_DEG) - 90.0f;
        soldier.snapTo(spot.x, spot.y, spot.z, yaw, 0.0f);
        soldier.setYHeadRot(yaw);
        soldier.setYBodyRot(yaw);
        EventHooks.finalizeMobSpawn(soldier, level, level.getCurrentDifficultyAt(tablePos), EntitySpawnReason.MOB_SUMMONED, null);
        level.addFreshEntity(soldier);
        soldier.recruitBy(player);
        level.playSound(null, tablePos, SoundEvents.ARMOR_EQUIP_CHAIN.value(), SoundSource.BLOCKS, 1.0f, 1.0f);
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            io.github.sirschrolli.kingdomsatwar.story.Story.onRecruited(serverPlayer);
        }
        return Result.RECRUITED;
    }

    /** Ein freier Platz vor dem Tisch (oder daneben), auf dem ein Soldat stehen kann. */
    private static @Nullable Vec3 findSpot(ServerLevel level, BlockPos table, Direction front, EntityType<?> type) {
        List<BlockPos> candidates = new ArrayList<>();
        BlockPos before = table.relative(front);
        candidates.add(before);
        candidates.add(before.relative(front.getClockWise()));
        candidates.add(before.relative(front.getCounterClockWise()));
        candidates.add(table.relative(front.getClockWise()));
        candidates.add(table.relative(front.getCounterClockWise()));
        candidates.add(before.relative(front));
        candidates.add(table.relative(front.getOpposite()));
        for (BlockPos base : candidates) {
            for (int dy : new int[] {0, 1, -1}) {
                BlockPos pos = base.above(dy);
                Vec3 feet = Vec3.atBottomCenterOf(pos);
                if (level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)
                        && level.noCollision(type.getSpawnAABB(feet.x, feet.y, feet.z))) {
                    return feet;
                }
            }
        }
        return null;
    }
}
