package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.TownBannerBlock;
import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.economy.town.BuildingType;
import io.github.sirschrolli.kingdomsatwar.economy.town.Profession;
import io.github.sirschrolli.kingdomsatwar.economy.town.Town;
import io.github.sirschrolli.kingdomsatwar.economy.town.TownManager;
import io.github.sirschrolli.kingdomsatwar.economy.town.TownStorage;
import io.github.sirschrolli.kingdomsatwar.economy.town.Towns;
import io.github.sirschrolli.kingdomsatwar.entity.realm.RealmWarriorEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.war.Realm;
import io.github.sirschrolli.kingdomsatwar.war.RealmRaids;
import io.github.sirschrolli.kingdomsatwar.war.RealmState;
import java.util.Deque;
import java.util.Locale;
import net.minecraft.client.CameraType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

/**
 * Vorführung der Heerhaufen: eine kleine Siedlung mit Soldaten am Banner; ein Heerhaufen von Rabenstein sammelt sich,
 * marschiert heran und kämpft am Banner; zuletzt ist er geschlagen (Kriegskasse im Lager).
 */
final class RaidShowcase {
    private static final int OX = 420;
    private static final int OZ = -420;

    private RaidShowcase() {}

    static void raid(Deque<Step> steps) {
        int[] g = {Integer.MIN_VALUE};
        RealmRaids.Raid[] raid = {null};
        steps.add(Step.hud(false));
        steps.add(Step.command("time set 1000"));
        steps.add(Step.command("tp @s " + OX + " 140 " + (OZ + 30) + " 180 30"));
        steps.add(Step.waitForChunks(600));
        steps.add(Step.server(level -> {
            for (int cx = (OX - 72) >> 4; cx <= (OX + 72) >> 4; cx++) {
                for (int cz = (OZ - 72) >> 4; cz <= (OZ + 72) >> 4; cz++) {
                    level.getChunk(cx, cz);
                    level.setChunkForced(cx, cz, true);
                }
            }
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, OX, OZ);
            BlockState grass = Blocks.GRASS_BLOCK.defaultBlockState();
            BlockState dirt = Blocks.DIRT.defaultBlockState();
            BlockState air = Blocks.AIR.defaultBlockState();
            for (int x = OX - 64; x <= OX + 64; x++) {
                for (int z = OZ - 64; z <= OZ + 64; z++) {
                    for (int dy = -3; dy <= 24; dy++) {
                        level.setBlock(new BlockPos(x, y + dy, z), dy < -1 ? dirt : dy == -1 ? grass : air, Block.UPDATE_CLIENTS);
                    }
                }
            }
            g[0] = y;
        }));
        steps.add(Step.until(() -> g[0] != Integer.MIN_VALUE, 600));
        steps.add(Step.server(level -> {
            ServerPlayer player = level.getServer().getPlayerList().getPlayers().getFirst();
            BlockPos banner = new BlockPos(OX, g[0], OZ);
            BlockState lower = ModBlocks.TOWN_BANNER.get().defaultBlockState().setValue(TownBannerBlock.FACING, Direction.NORTH);
            level.setBlock(banner, lower, Block.UPDATE_ALL);
            level.setBlock(banner.above(), lower.setValue(TownBannerBlock.HALF, DoubleBlockHalf.UPPER), Block.UPDATE_ALL);
            TownManager.placeBanner(player, level, banner);
            Town town = Towns.of(level.getServer()).byOwner(player.getUUID());
            if (town == null) {
                KingdomsAtWar.LOGGER.error("[Showcase] Keine Siedlung");
                return;
            }
            TownManager.buildInstantly(level, town, BuildingType.HOUSE, new BlockPos(OX - 13, g[0], OZ + 6), Rotation.CLOCKWISE_90);
            TownManager.buildInstantly(level, town, BuildingType.SMITHY, new BlockPos(OX + 12, g[0], OZ + 8), Rotation.COUNTERCLOCKWISE_90);
            TownManager.buildInstantly(level, town, BuildingType.TOWNHOUSE, new BlockPos(OX - 14, g[0], OZ - 10), Rotation.CLOCKWISE_90);
            TownManager.buildInstantly(level, town, BuildingType.MARKET, new BlockPos(OX + 12, g[0], OZ - 12), Rotation.COUNTERCLOCKWISE_90);
            TownStorage.supply(level, banner.offset(2, 0, 1), BuildingType.HOUSE.costs());
            for (Profession p : new Profession[] {Profession.FARMER, Profession.SMITH}) {
                TownManager.spawnCitizen(level, town, p);
            }
            // Verteidiger am Banner
            SoldierType[] types = {SoldierType.KNIGHT, SoldierType.KNIGHT, SoldierType.SWORDSMAN, SoldierType.SWORDSMAN, SoldierType.ARCHER,
                    SoldierType.ARCHER, SoldierType.CAPTAIN};
            for (int i = 0; i < types.length; i++) {
                SoldierEntity soldier = ModEntities.SOLDIERS.get(types[i]).get().create(level, EntitySpawnReason.COMMAND);
                if (soldier == null) {
                    continue;
                }
                double a = i * Math.PI * 2 / types.length;
                BlockPos post = banner.offset((int) Math.round(Math.cos(a) * 4), 0, (int) Math.round(Math.sin(a) * 4));
                soldier.snapTo(post.getX() + 0.5, post.getY(), post.getZ() + 0.5, 0.0f, 0.0f);
                level.addFreshEntity(soldier);
                soldier.recruitBy(player);
                soldier.setMode(SoldierEntity.MODE_HOLD, post);
                soldier.setPersistenceRequired();
            }
            RealmState.of(level).declareWar(Realm.RABENSTEIN, player.getUUID());
            RealmState.of(level).save(level);
        }));
        steps.add(Step.waitTicks(40));
        steps.add(Step.server(level -> {
            ServerPlayer player = level.getServer().getPlayerList().getPlayers().getFirst();
            Town town = Towns.of(level.getServer()).byOwner(player.getUUID());
            if (town != null) {
                raid[0] = RealmRaids.start(level, Realm.RABENSTEIN, player, town);
            }
        }));
        steps.add(Step.until(() -> raid[0] != null, 400));
        // 1: der Heerhaufen am Sammelplatz, hinten die Siedlung (mit Titel und Leiste)
        steps.add(Step.hud(true));
        steps.add(camera(raid, -9.0, 3.0, 0.0, 1.5));
        steps.add(Step.waitTicks(25));
        steps.add(Step.screenshot("raid_muster"));
        steps.add(Step.hud(false));
        // 2: auf dem Marsch, seitlich
        steps.add(Step.waitTicks(80));
        steps.add(side(raid, 0.55, 14.0, 4.0));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("raid_march"));
        // 3: Kampf am Banner
        steps.add(Step.until(() -> raid[0] != null && near(raid[0], 9.0), 1200));
        steps.add(Step.waitTicks(30));
        steps.add(Step.hud(true));
        steps.add(bannerView(raid));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("raid_battle"));
        // 4: geschlagen – die Meldung mit der Kriegskasse
        steps.add(Step.server(level -> {
            if (raid[0] != null) {
                for (RealmWarriorEntity w : RealmRaids.devRaiders(level, raid[0])) {
                    w.hurtServer(level, level.damageSources().generic(), 10_000.0f);
                }
            }
        }));
        steps.add(Step.waitTicks(60));
        steps.add(Step.screenshot("raid_victory"));
        steps.add(Step.hud(false));
    }

    /** Sind Krieger des Heerhaufens so nah am Banner? */
    private static boolean near(RealmRaids.Raid raid, double radius) {
        var server = net.minecraft.client.Minecraft.getInstance().getSingleplayerServer();
        if (server == null) {
            return false;
        }
        ServerLevel level = server.overworld();
        Vec3 banner = Vec3.atCenterOf(raid.banner());
        return RealmRaids.devRaiders(level, raid).stream().anyMatch(w -> w.distanceToSqr(banner) < radius * radius);
    }

    /** Hinter dem Sammelplatz (Abstand {@code back}, vom Banner weg), Blick zum Banner. */
    private static Step camera(RealmRaids.Raid[] raid, double back, double height, double side, double lookHeight) {
        return Step.once(minecraft -> {
            BlockPos m = raid[0].muster();
            BlockPos b = raid[0].banner();
            Vec3 dir = new Vec3(b.getX() - m.getX(), 0, b.getZ() - m.getZ()).normalize();
            Vec3 eye = Vec3.atBottomCenterOf(m).add(dir.scale(back)).add(-dir.z * side, height, dir.x * side);
            look(minecraft, eye, Vec3.atBottomCenterOf(b).add(0, lookHeight, 0));
        });
    }

    /** Seitlich auf halbem Weg ({@code at}: Anteil vom Sammelplatz zum Banner), Abstand {@code distance}. */
    private static Step side(RealmRaids.Raid[] raid, double at, double distance, double height) {
        return Step.once(minecraft -> {
            Vec3 m = Vec3.atBottomCenterOf(raid[0].muster());
            Vec3 b = Vec3.atBottomCenterOf(raid[0].banner());
            Vec3 dir = b.subtract(m).normalize();
            Vec3 mid = m.add(b.subtract(m).scale(at));
            Vec3 eye = mid.add(-dir.z * distance, height, dir.x * distance);
            look(minecraft, eye, mid.add(0, 1.2, 0));
        });
    }

    /** Vom Banner aus nach außen dem Heerhaufen entgegen, etwas erhöht. */
    private static Step bannerView(RealmRaids.Raid[] raid) {
        return Step.once(minecraft -> {
            Vec3 m = Vec3.atBottomCenterOf(raid[0].muster());
            Vec3 b = Vec3.atBottomCenterOf(raid[0].banner());
            Vec3 dir = m.subtract(b).normalize();
            Vec3 eye = b.add(dir.scale(-7.0)).add(-dir.z * 3.0, 4.0, dir.x * 3.0);
            look(minecraft, eye, b.add(dir.scale(5.0)).add(0, 1.0, 0));
        });
    }

    private static void look(net.minecraft.client.Minecraft minecraft, Vec3 eye, Vec3 target) {
        double lx = target.x - eye.x;
        double ly = target.y - eye.y;
        double lz = target.z - eye.z;
        float yaw = (float) (-Math.atan2(lx, lz) * 180.0 / Math.PI);
        float pitch = (float) (-Math.atan2(ly, Math.sqrt(lx * lx + lz * lz)) * 180.0 / Math.PI);
        minecraft.options.setCameraType(CameraType.FIRST_PERSON);
        minecraft.player.connection.sendCommand(String.format(Locale.ROOT, "tp @s %.2f %.2f %.2f %.1f %.1f", eye.x, eye.y - 1.62, eye.z, yaw, pitch));
    }
}
