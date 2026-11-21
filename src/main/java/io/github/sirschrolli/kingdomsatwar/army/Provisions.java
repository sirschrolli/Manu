package io.github.sirschrolli.kingdomsatwar.army;

import io.github.sirschrolli.kingdomsatwar.block.RecruitmentTableBlock;
import io.github.sirschrolli.kingdomsatwar.economy.town.Town;
import io.github.sirschrolli.kingdomsatwar.economy.town.TownFood;
import io.github.sirschrolli.kingdomsatwar.economy.town.Towns;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Proviant der Soldaten: Jeder Soldat trägt Rationen für einige Tage bei sich und isst jeden Morgen eine davon. Am
 * Feldherrentisch teilt man allen im Feldlager Proviant zu – aus dem Lager der eigenen Siedlung (steht der Tisch darin)
 * und aus dem eigenen Gepäck. Wer nichts mehr hat, hungert: Er heilt nicht mehr von selbst und schlägt schwächer zu.
 */
public final class Provisions {
    /** So viele Tagesrationen trägt ein Soldat höchstens. */
    public static final int MAX_RATIONS = 7;
    /** Mit so vielen kommt ein angeworbener Soldat. */
    public static final int START_RATIONS = 3;
    /** Ein hungriger Soldat trifft nur so stark. */
    public static final float HUNGRY_DAMAGE = 0.7f;

    private Provisions() {}

    /** Der wievielte Tag seit Weltbeginn ist? */
    public static long day(ServerLevel level) {
        return Math.floorDiv(level.getOverworldClockTime(), 24000L);
    }

    /** Wie viele Rationen den Soldaten im Feldlager bis zum Vollen fehlen. */
    public static int missing(List<SoldierEntity> soldiers) {
        int missing = 0;
        for (SoldierEntity soldier : soldiers) {
            missing += Math.max(0, MAX_RATIONS - soldier.getRations()) + (soldier.isHungry() ? 1 : 0);
        }
        return missing;
    }

    /** Wie viel Nährwert der Spieler bei sich trägt. */
    public static int carried(Player player) {
        int total = 0;
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            total += TownFood.nutrition(stack) * stack.getCount();
        }
        return total;
    }

    /** Die Siedlung des Spielers, wenn der Tisch in ihr steht (dann zählt ihr Lager mit). */
    public static @Nullable Town townAt(ServerLevel level, Player player, BlockPos table) {
        Town town = Towns.of(level.getServer()).byOwner(player.getUUID());
        return town != null && town.contains(level.dimension(), table) ? town : null;
    }

    /**
     * Teilt allen eigenen Soldaten im Feldlager Proviant zu, bis jeder {@link #MAX_RATIONS} Rationen hat (wer gerade
     * hungert, isst sofort eine). Das Essen kommt erst aus dem Lager der Siedlung, dann aus dem Gepäck des Spielers; die
     * Hungrigsten bekommen zuerst. Reicht es nicht für alle, bekommt jeder, so weit es reicht.
     */
    public static Recruitment.Result supply(Player player, BlockPos table) {
        if (!(player.level() instanceof ServerLevel level) || !(level.getBlockState(table).getBlock() instanceof RecruitmentTableBlock)
                || player.distanceToSqr(Vec3.atCenterOf(table)) > Recruitment.TABLE_RANGE * Recruitment.TABLE_RANGE) {
            return Recruitment.Result.TOO_FAR;
        }
        List<SoldierEntity> camp = Training.camp(player, table).stream()
                .sorted(Comparator.comparingInt(SoldierEntity::getRations)).toList();
        int need = missing(camp);
        if (need <= 0) {
            return Recruitment.Result.NOTHING_NEEDED;
        }
        Town town = townAt(level, player, table);
        int points = 0;
        int wanted = need * TownFood.MEAL;
        if (player.hasInfiniteMaterials()) {
            points = wanted;
        } else {
            if (town != null) {
                points += TownFood.take(level, town, wanted);
            }
            if (points < wanted) {
                points += takeCarried(player, wanted - points);
            }
        }
        int meals = points / TownFood.MEAL;
        if (meals <= 0) {
            return Recruitment.Result.NO_FOOD;
        }
        long today = day(level);
        // reihum: jeder bekommt eine, solange es reicht – zuerst stillen die Hungrigen ihren Hunger
        boolean given = true;
        while (meals > 0 && given) {
            given = false;
            for (SoldierEntity soldier : camp) {
                if (meals <= 0) {
                    break;
                }
                if (soldier.isHungry()) {
                    soldier.eatRation(today, false);
                    meals--;
                    given = true;
                } else if (soldier.getRations() < MAX_RATIONS) {
                    soldier.setRations(soldier.getRations() + 1);
                    meals--;
                    given = true;
                }
            }
        }
        for (SoldierEntity soldier : camp) {
            level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, Items.BREAD), soldier.getX(), soldier.getY() + 1.2,
                    soldier.getZ(), 5, 0.25, 0.25, 0.25, 0.04);
        }
        level.playSound(null, table, SoundEvents.BUNDLE_INSERT, SoundSource.BLOCKS, 1.0f, 0.9f);
        return Recruitment.Result.SUPPLIED;
    }

    /** Nimmt Essen für (mindestens) {@code points} Nährwert aus dem Gepäck – das Einfache zuerst. */
    private static int takeCarried(Player player, int points) {
        int got = 0;
        List<ItemStack> items = player.getInventory().getNonEquipmentItems();
        for (int max = 2; max <= 20 && got < points; max += 2) {
            for (ItemStack stack : items) {
                int each = TownFood.nutrition(stack);
                if (each <= 0 || each > max || each <= max - 2) {
                    continue;
                }
                int n = Math.min(stack.getCount(), (points - got + each - 1) / each);
                stack.shrink(n);
                got += n * each;
                if (got >= points) {
                    break;
                }
            }
        }
        player.getInventory().setChanged();
        return got;
    }
}
