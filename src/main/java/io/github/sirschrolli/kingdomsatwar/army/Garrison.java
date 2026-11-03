package io.github.sirschrolli.kingdomsatwar.army;

import io.github.sirschrolli.kingdomsatwar.block.RecruitmentTableBlock;
import io.github.sirschrolli.kingdomsatwar.economy.town.BuildingType;
import io.github.sirschrolli.kingdomsatwar.economy.town.Town;
import io.github.sirschrolli.kingdomsatwar.economy.town.Towns;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Garnison und Schlachtzug: Angeworbene Soldaten ziehen in die Siedlung ihres Herrn und bleiben dort – sie gehen um die
 * Kaserne (oder das Stadtbanner) Streife –, bis er sie am Feldherrentisch auf einen Schlachtzug mitnimmt. Danach schickt
 * er sie am Tisch oder mit dem Kommandostab heim. Wer keine Siedlung hat, dessen Soldaten folgen ihm wie bisher.
 */
public final class Garrison {
    /** Weiter als so viele Blöcke von der Garnison entfernt (oder in einer anderen Welt): der Soldat reist hin. */
    public static final double TRAVEL_RANGE = 64.0;

    private Garrison() {}

    /** Die Siedlung des Herrn dieses Soldaten ({@code null}: keine). */
    public static @Nullable Town townOf(SoldierEntity soldier) {
        if (!(soldier.level() instanceof ServerLevel level) || soldier.getOwnerId() == null) {
            return null;
        }
        return Towns.of(level.getServer()).byOwner(soldier.getOwnerId());
    }

    /**
     * Der Posten in der Garnison: vor dem Tor einer Kaserne der Siedlung (zwischen Tor und Übungsplatz) – oder, solange
     * es keine gibt, am Stadtbanner. Jeder Soldat bekommt einen etwas anderen Platz (vor der Kaserne entlang der Front),
     * damit sie nicht alle auf einem Fleck stehen; gibt es mehrere Kasernen, verteilen sie sich darauf.
     */
    public static BlockPos post(ServerLevel level, Town town, SoldierEntity soldier) {
        long hash = soldier.getUUID().getLeastSignificantBits();
        List<Town.Building> barracks = town.buildings().stream().filter(b -> b.type() == BuildingType.BARRACKS).toList();
        BlockPos center;
        BlockPos spot;
        if (barracks.isEmpty()) {
            center = town.banner();
            int dx = (int) Math.floorMod(hash, 7L) - 3;
            int dz = (int) Math.floorMod(hash >> 8, 7L) - 3;
            if (Math.abs(dx) <= 1 && Math.abs(dz) <= 1) {
                dx = dx < 0 ? -2 : 2;   // nicht aufs Banner
            }
            spot = center.offset(dx, 0, dz);
        } else {
            Town.Building building = barracks.get((int) Math.floorMod(hash >> 16, (long) barracks.size()));
            center = building.world(building.plan().entrance());
            BoundingBox box = building.bounds();
            // die Front läuft quer zur Richtung vom Haus zum Tor
            boolean frontAlongX = Math.abs(center.getX() - box.getCenter().getX()) < Math.abs(center.getZ() - box.getCenter().getZ());
            int side = (int) Math.floorMod(hash, 5L) - 2;
            spot = frontAlongX ? center.offset(side, 0, 0) : center.offset(0, 0, side);
        }
        if (!level.isLoaded(spot)) {
            return spot.immutable();
        }
        BlockPos found = standable(level, spot);
        if (found == null) {
            found = standable(level, center);
        }
        return (found != null ? found : level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spot)).immutable();
    }

    /** Der nächste Platz zum Stehen über oder unter {@code pos}: fester Boden, zwei Blöcke frei, kein Wasser. */
    private static @Nullable BlockPos standable(ServerLevel level, BlockPos pos) {
        for (int dy = 0; dy <= 3; dy++) {
            if (canStand(level, pos.above(dy))) {
                return pos.above(dy);
            }
            if (dy > 0 && canStand(level, pos.below(dy))) {
                return pos.below(dy);
            }
        }
        return null;
    }

    private static boolean canStand(ServerLevel level, BlockPos pos) {
        BlockPos below = pos.below();
        return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)
                && level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()
                && level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty()
                && level.getFluidState(pos).isEmpty();
    }

    /**
     * Schickt den Soldaten in die Garnison seiner Siedlung: Er geht dort um seinen Posten Streife. Ist er weit fort (oder
     * in einer anderen Welt), reist er sofort hin. Gibt zurück, ob er eine Garnison hat.
     */
    public static boolean station(SoldierEntity soldier) {
        Town town = townOf(soldier);
        if (town == null || !(soldier.level() instanceof ServerLevel level)) {
            return false;
        }
        ServerLevel home = level.getServer().getLevel(town.dimension());
        if (home == null) {
            return false;
        }
        BlockPos post = post(home, town, soldier);
        if (!home.isLoaded(post)) {
            home.getChunkAt(post);   // laden, damit er auf festem Boden ankommt
            post = post(home, town, soldier);
        }
        soldier.setCampaign(false);
        soldier.setMode(SoldierEntity.MODE_PATROL, post);
        soldier.setTarget(null);
        if (home != level || soldier.distanceToSqr(Vec3.atBottomCenterOf(post)) > TRAVEL_RANGE * TRAVEL_RANGE) {
            soldier.teleport(new TeleportTransition(home, Vec3.atBottomCenterOf(post), Vec3.ZERO, soldier.getYRot(), 0.0f,
                    TeleportTransition.PLACE_PORTAL_TICKET));
        }
        return true;
    }

    /** Alle eigenen Soldaten im Feldlager, die in der Garnison stehen, ziehen mit dem Spieler: Sie folgen ihm. */
    public static Recruitment.Result march(Player player, BlockPos table) {
        if (!atTable(player, table)) {
            return Recruitment.Result.TOO_FAR;
        }
        List<SoldierEntity> camp = Training.camp(player, table).stream().filter(s -> !s.isOnCampaign()).toList();
        if (camp.isEmpty()) {
            return Recruitment.Result.NO_SOLDIERS;
        }
        for (SoldierEntity soldier : camp) {
            soldier.setCampaign(true);
            soldier.setMode(SoldierEntity.MODE_FOLLOW, null);
            soldier.salute();
        }
        camp.getFirst().speak("follow");
        return Recruitment.Result.MARCHED;
    }

    /** Alle eigenen Soldaten im Feldlager, die mitgezogen sind, kehren in die Garnison zurück. */
    public static Recruitment.Result returnHome(Player player, BlockPos table) {
        if (!atTable(player, table)) {
            return Recruitment.Result.TOO_FAR;
        }
        if (!(player.level() instanceof ServerLevel level) || Towns.of(level.getServer()).byOwner(player.getUUID()) == null) {
            return Recruitment.Result.NO_TOWN;
        }
        List<SoldierEntity> camp = Training.camp(player, table).stream().filter(SoldierEntity::isOnCampaign).toList();
        if (camp.isEmpty()) {
            return Recruitment.Result.NO_SOLDIERS;
        }
        for (SoldierEntity soldier : camp) {
            station(soldier);
        }
        return Recruitment.Result.RETURNED;
    }

    /** Kommandostab (Schleichen und in den Himmel zeigen): alle eigenen Soldaten in Rufweite kehren heim. */
    public static int sendHome(ServerPlayer player) {
        Town town = Towns.of(player.level().getServer()).byOwner(player.getUUID());
        if (town == null) {
            player.sendOverlayMessage(Component.translatable("message.kingdomsatwar.garrison.no_town").withStyle(ChatFormatting.RED));
            return 0;
        }
        int sent = 0;
        for (SoldierEntity soldier : SoldierEntity.soldiersOf(player, SoldierEntity.COMMAND_RANGE)) {
            // wer eine Belagerungswaffe bedient, bleibt an ihr (abziehen geht mit dem Stab an der Waffe)
            if (!io.github.sirschrolli.kingdomsatwar.siege.SiegeCrew.isCrew(soldier) && station(soldier)) {
                sent++;
            }
        }
        player.sendOverlayMessage(Component.translatable("message.kingdomsatwar.garrison.sent_home", sent, town.name())
                .withStyle(ChatFormatting.GOLD));
        return sent;
    }

    private static boolean atTable(Player player, BlockPos table) {
        return player.level().getBlockState(table).getBlock() instanceof RecruitmentTableBlock
                && player.distanceToSqr(Vec3.atCenterOf(table)) <= Recruitment.TABLE_RANGE * Recruitment.TABLE_RANGE;
    }
}
