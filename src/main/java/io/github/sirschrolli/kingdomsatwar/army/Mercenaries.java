package io.github.sirschrolli.kingdomsatwar.army;

import io.github.sirschrolli.kingdomsatwar.block.MercenaryBoardBlock;
import io.github.sirschrolli.kingdomsatwar.economy.Coins;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Söldner in den Dörfern: Im Söldnerhaus jedes Dorfs warten jeden Morgen bis zu drei Kämpfer – Schwertkämpfer,
 * Bogenschützen, Ritter, Speerträger, Armbrustschützen, seltener auch Schildträger, Belagerungsknechte, Waffenknechte,
 * Hauptleute und Lanzenreiter (deren Ross steht im Stall, bis man sie anheuert), manche schon erfahren (Veteran, Elite).
 * An der Söldnertafel sieht man, wer da ist und was er kostet; angeheuert wird mit Münzen (wer seine Ausrüstung
 * selbst mitbringt, will dafür mehr Sold). Geheilt, ausgebildet und befördert wird später am eigenen Feldherrentisch.
 */
public final class Mercenaries {
    /** So viele Söldner warten höchstens an einer Tafel. */
    public static final int MAX_AT_HALL = 3;
    /** So nah muss der Spieler an der Tafel stehen. */
    public static final double BOARD_RANGE = 8.0;
    /** So weit um die Tafel herum gehören die Söldner zum Haus. */
    public static final double HALL_RADIUS = 16.0;

    private Mercenaries() {}

    /** Die Söldner, die an der Tafel bei {@code board} warten (die nächsten zuerst). */
    public static List<SoldierEntity> at(Level level, BlockPos board) {
        Vec3 center = Vec3.atCenterOf(board);
        List<SoldierEntity> list = level.getEntitiesOfClass(SoldierEntity.class, new AABB(board).inflate(HALL_RADIUS),
                s -> s.isAlive() && s.isMercenary() && board.equals(s.getHall()));
        list.sort(Comparator.comparingDouble(s -> s.distanceToSqr(center)));
        return list;
    }

    /** Heuert den Söldner {@code entityId} an der Tafel bei {@code board} an – alles wird hier noch einmal geprüft. */
    public static Recruitment.Result hire(ServerPlayer player, BlockPos board, int entityId) {
        ServerLevel level = player.level();
        if (!(level.getBlockState(board).getBlock() instanceof MercenaryBoardBlock)
                || player.distanceToSqr(Vec3.atCenterOf(board)) > BOARD_RANGE * BOARD_RANGE) {
            return Recruitment.Result.TOO_FAR;
        }
        Entity entity = level.getEntity(entityId);
        if (!(entity instanceof SoldierEntity soldier) || !soldier.isAlive() || !soldier.isMercenary() || !board.equals(soldier.getHall())) {
            return Recruitment.Result.NOT_HERE;
        }
        if (Recruitment.armySize(level, player.getUUID()) >= Recruitment.armyLimit(player)) {
            return Recruitment.Result.ARMY_FULL;
        }
        int price = soldier.hirePrice();
        if (!player.hasInfiniteMaterials()) {
            if (!Coins.canAfford(player, price)) {
                return Recruitment.Result.NOT_ENOUGH;
            }
            Coins.pay(player, price);
        }
        soldier.hireBy(player);
        io.github.sirschrolli.kingdomsatwar.story.Story.onRecruited(player);
        return Recruitment.Result.HIRED;
    }

    /**
     * Welche Art als Nächstes kommt: am häufigsten Ritter, Schwertkämpfer und Bogenschützen, öfter auch Speerträger und
     * Armbrustschützen, seltener Schildträger, Belagerungsknechte und Waffenknechte, Hauptleute und Lanzenreiter am seltensten.
     */
    public static SoldierType randomType(RandomSource random) {
        int roll = random.nextInt(100);
        SoldierType[] order = {SoldierType.KNIGHT, SoldierType.SWORDSMAN, SoldierType.ARCHER, SoldierType.SPEARMAN, SoldierType.CROSSBOWMAN,
                SoldierType.SHIELDBEARER, SoldierType.ENGINEER, SoldierType.MAN_AT_ARMS, SoldierType.CAPTAIN, SoldierType.LANCER};
        int[] weights = {24, 18, 15, 11, 10, 7, 5, 4, 3, 3};
        for (int i = 0; i < order.length; i++) {
            roll -= weights[i];
            if (roll < 0) {
                return order[i];
            }
        }
        return SoldierType.SWORDSMAN;
    }

    /** Wie erfahren er ist: meist Rekrut, oft Veteran, selten Elite. */
    public static int randomRank(RandomSource random) {
        int roll = random.nextInt(100);
        return roll < 55 ? 0 : roll < 88 ? 1 : 2;
    }

    /**
     * Ein neuer Söldner der Art {@code type} mit dem Rang {@code rank}, der an {@code spot} auf einen Herrn wartet und
     * dabei zur Tafel schaut.
     */
    public static @Nullable SoldierEntity spawn(ServerLevel level, BlockPos board, BlockPos spot, SoldierType type, int rank, boolean arrival) {
        SoldierEntity soldier = ModEntities.SOLDIERS.get(type).get().create(level, EntitySpawnReason.EVENT);
        if (soldier == null) {
            return null;
        }
        double dx = board.getX() + 0.5 - (spot.getX() + 0.5);
        double dz = board.getZ() + 0.5 - (spot.getZ() + 0.5);
        float yaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0f;
        soldier.snapTo(spot.getX() + 0.5, spot.getY(), spot.getZ() + 0.5, yaw, 0.0f);
        soldier.setYHeadRot(yaw);
        soldier.setYBodyRot(yaw);
        soldier.finalizeSpawn(level, level.getCurrentDifficultyAt(spot), EntitySpawnReason.EVENT, null);
        soldier.setRank(rank);
        // so erfahren, wie sein Rang verlangt, dazu ein Stück des Wegs zur nächsten Stufe
        int floor = Veterancy.total(Veterancy.minLevel(rank));
        soldier.setCombatExperience(floor + level.getRandom().nextInt(Math.max(1, Veterancy.total(Veterancy.minLevel(rank) + 2) - floor)));
        soldier.setHealth(soldier.getMaxHealth());
        soldier.waitForHire(board, spot);
        soldier.setPersistenceRequired();
        level.addFreshEntity(soldier);
        if (arrival) {
            level.sendParticles(ParticleTypes.POOF, soldier.getX(), soldier.getY() + 0.8, soldier.getZ(), 8, 0.3, 0.5, 0.3, 0.02);
        }
        return soldier;
    }
}
