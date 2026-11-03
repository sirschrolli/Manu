package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.client.renderer.siege.SiegeWeaponRenderer;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import io.github.sirschrolli.kingdomsatwar.siege.SiegeProjectile;
import io.github.sirschrolli.kingdomsatwar.siege.SiegeType;
import io.github.sirschrolli.kingdomsatwar.siege.SiegeWeaponEntity;
import java.util.Deque;
import java.util.Locale;
import net.minecraft.client.CameraType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;

/**
 * Vorführung der Belagerungswaffen: Katapult, Tribok, Balliste und Rammbock in Ruhe und mitten im Schuss, eine Bresche in
 * der Burgmauer, der Rammbock am Tor, der Blick des Schützen (Zielspur, Anzeige) und eine Bedienmannschaft.
 */
final class SiegeShowcase {
    private static final int X0 = 0;
    private static final int Z0 = -300;
    /** Vorderseite der Burgmauer (die Mauer liegt bei z = WALL − 2 und WALL − 1). */
    private static final int WALL = Z0 - 34;
    private static final double CAT_X = X0 + 0.5;
    private static final double CAT_Z = Z0 - 6.0;
    private static final double TREB_X = X0 + 15.5;
    private static final double TREB_Z = Z0 + 10.0;
    private static final double BAL_X = X0 + 25.5;
    private static final double BAL_Z = Z0 - 8.0;
    /** Das Tor (zwei Türen bei x = GATE und GATE + 1), davor der Rammbock. */
    private static final int GATE = X0 + 30;
    private static final double RAM_X = GATE + 1.0;
    private static final double RAM_Z = WALL + 2.35;

    private SiegeShowcase() {}

    static void siege(Deque<Step> steps) {
        SiegeWeaponEntity[] w = new SiegeWeaponEntity[4];
        steps.add(Step.hud(false));
        steps.add(Step.server(level -> {
            build(level);
            w[0] = spawn(level, SiegeType.CATAPULT, CAT_X, CAT_Z, 180.0f);
            w[1] = spawn(level, SiegeType.TREBUCHET, TREB_X, TREB_Z, 180.0f);
            w[2] = spawn(level, SiegeType.BALLISTA, BAL_X, BAL_Z, 180.0f);
            w[3] = spawn(level, SiegeType.RAM, RAM_X, RAM_Z, 180.0f);
        }));
        steps.add(Step.until(() -> w[3] != null, 400));
        steps.add(look(CAT_X - 2.5, 1.9, CAT_Z - 2.7, CAT_X, 1.0, CAT_Z));
        steps.add(Step.waitTicks(100));
        steps.add(Step.waitForChunks(600));
        steps.add(Step.waitTicks(40));
        steps.add(Step.screenshot("siege_catapult"));

        // Katapult: der Arm schnellt hoch, die Kugel noch im Löffel
        pose(steps, w, 0, 3.3f, look(CAT_X - 4.1, 1.6, CAT_Z + 0.2, CAT_X, 1.5, CAT_Z - 0.3), "siege_catapult_throw");

        // Tribok: gespannt, dann mitten im Schwung
        steps.add(look(TREB_X - 5.3, 2.6, TREB_Z - 4.6, TREB_X, 2.5, TREB_Z));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("siege_trebuchet"));
        pose(steps, w, 1, 7.0f, look(TREB_X - 8.0, 3.0, TREB_Z + 0.3, TREB_X, 3.7, TREB_Z - 1.5), "siege_trebuchet_swing");

        // Balliste: gespannt mit Bolzen, dann der Schuss – die Arme schnellen vor, der Bolzen verlässt die Bahn
        steps.add(look(BAL_X - 1.8, 1.5, BAL_Z - 2.1, BAL_X, 0.9, BAL_Z));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("siege_ballista"));
        SiegeProjectile[] bolt = new SiegeProjectile[1];
        steps.add(Step.server(level -> {
            SiegeWeaponEntity ballista = w[2];
            Vec3 at = ballista.launchPoint().add(0.0, -0.3, -2.2);
            SiegeProjectile p = SiegeProjectile.devCreate(level, SiegeType.BALLISTA, ballista, at);
            p.setYRot(180.0f);
            p.setXRot(0.0f);
            p.devStick();
            level.addFreshEntity(p);
            bolt[0] = p;
        }));
        pose(steps, w, 2, 1.2f, look(BAL_X - 3.4, 1.4, BAL_Z - 1.8, BAL_X, 1.0, BAL_Z - 1.8), "siege_ballista_fire");
        steps.add(Step.server(level -> {
            if (bolt[0] != null) {
                bolt[0].discard();
            }
        }));

        // Rammbock am Tor: ein Stoß hat die Türen schon angeknackst, der nächste kommt
        steps.add(Step.server(level -> w[3].fire(level, null)));
        steps.add(Step.waitTicks(25));
        pose(steps, w, 3, 4.6f, look(RAM_X - 4.6, 1.5, WALL + 1.25, RAM_X, 1.4, WALL + 0.4), "siege_ram");

        // Bresche: das Katapult wirft auf die Mauer; ein Bild mit der Kugel im Flug, eins von der zerschlagenen Mauer
        Vec3[] targets = {
                new Vec3(X0 + 3.5, 0, WALL + 0.1), new Vec3(X0 + 5.5, 0, WALL + 0.1), new Vec3(X0 + 1.5, 0, WALL + 0.1),
                new Vec3(X0 + 4.5, 0, WALL + 0.1), new Vec3(X0 + 7.0, 0, WALL + 0.1)};
        double[] heights = {3.5, 2.5, 2.0, 5.0, 4.0};
        steps.add(look(CAT_X - 9.0, 3.2, CAT_Z - 5.0, CAT_X + 1.0, 3.6, CAT_Z - 5.5));
        steps.add(Step.waitTicks(30));
        for (int i = 0; i < targets.length; i++) {
            int k = i;
            steps.add(Step.server(level -> {
                SiegeWeaponEntity catapult = w[0];
                Vec3 target = targets[k].add(0.0, Showcase.ground + heights[k], 0.0);
                Vec3 d = target.subtract(catapult.position());
                float yaw = (float) (-Mth.atan2(d.x, d.z) * Mth.RAD_TO_DEG);
                catapult.setYRot(yaw);
                catapult.setYHeadRot(yaw);
                catapult.devPose(1000);
                catapult.setAmmo(16);
                catapult.aimAt(target);
                catapult.fire(level, null);
            }));
            if (i == 0) {
                // die Kugel hat eben den Löffel verlassen
                steps.add(Step.waitTicks(6));
                steps.add(Step.screenshot("siege_stone_flight"));
            }
            steps.add(Step.waitTicks(50));
        }
        steps.add(look(X0 + 11.5, 3.2, WALL + 8.5, X0 + 4.0, 3.0, WALL - 0.5));
        steps.add(Step.waitTicks(40));
        steps.add(Step.screenshot("siege_breach"));

        // Blick des Schützen: neben dem Katapult an der Winde, mit Zielspur und Anzeige
        steps.add(Step.server(level -> {
            ServerPlayer player = level.getServer().getPlayerList().getPlayers().getFirst();
            player.setGameMode(GameType.SURVIVAL);
            SiegeWeaponEntity catapult = w[0];
            catapult.setYRot(180.0f);
            catapult.devPose(1000);
            catapult.setAmmo(16);
            var inventory = player.getInventory();
            inventory.clearContent();
            inventory.setItem(0, new ItemStack(ModItems.SIEGE_STONE.get(), 16));
            inventory.setItem(1, new ItemStack(ModItems.BALLISTA_BOLT.get(), 16));
            inventory.setItem(2, new ItemStack(ModItems.CATAPULT_KIT.get()));
            inventory.setItem(3, new ItemStack(ModItems.TREBUCHET_KIT.get()));
            inventory.setItem(4, new ItemStack(ModItems.BALLISTA_KIT.get()));
            inventory.setItem(5, new ItemStack(ModItems.BATTERING_RAM_KIT.get()));
            inventory.setItem(6, new ItemStack(ModItems.COMMAND_BATON.get()));
            inventory.setSelectedSlot(0);
            player.teleportTo(level, catapult.getX() - 1.6, catapult.getY(), catapult.getZ(), java.util.Set.of(), 180.0f, 0.0f, false);
            player.startRiding(catapult, true, true);
        }));
        steps.add(Step.waitTicks(10));
        steps.add(Step.once(minecraft -> {
            minecraft.options.setCameraType(CameraType.FIRST_PERSON);
            minecraft.player.setYRot(180.0f);
            minecraft.player.setXRot(-4.0f);
        }));
        steps.add(Step.hud(true));
        steps.add(Step.waitTicks(60));
        steps.add(Step.screenshot("siege_aim"));
        steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK)));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("siege_aim_third"));
        steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.FIRST_PERSON)));
        steps.add(Step.hud(false));
        steps.add(Step.server(level -> {
            ServerPlayer player = level.getServer().getPlayerList().getPlayers().getFirst();
            player.stopRiding();
            player.getInventory().clearContent();
            player.setGameMode(GameType.SPECTATOR);
        }));
        steps.add(Step.waitTicks(10));

        // Bedienmannschaft: Soldaten an Katapult, Tribok und Balliste
        steps.add(Step.server(level -> {
            ServerPlayer player = level.getServer().getPlayerList().getPlayers().getFirst();
            w[0].setYRot(180.0f);
            SoldierType[] types = {SoldierType.SWORDSMAN, SoldierType.KNIGHT, SoldierType.ARCHER};
            for (int i = 0; i < 3; i++) {
                SoldierEntity soldier = ModEntities.SOLDIERS.get(types[i]).get().create(level, EntitySpawnReason.COMMAND);
                if (soldier == null) {
                    continue;
                }
                soldier.snapTo(w[i].getX(), w[i].getY(), w[i].getZ() + 1.5, 180.0f, 0.0f);
                soldier.setOwner(player);
                soldier.setNoAi(true);
                soldier.setPersistenceRequired();
                level.addFreshEntity(soldier);
                soldier.startRiding(w[i], true, true);
            }
        }));
        steps.add(look(X0 - 6.5, 3.4, Z0 + 4.0, X0 + 12.0, 1.8, Z0 - 6.0));
        steps.add(Step.waitTicks(40));
        steps.add(Step.screenshot("siege_crew"));
    }

    /** Waffe {@code i} zeigt im Bild die Stellung {@code since} Ticks nach dem Schuss (nur auf dem Bildschirm). */
    private static void pose(Deque<Step> steps, SiegeWeaponEntity[] w, int i, float since, Step camera, String name) {
        steps.add(camera);
        steps.add(Step.once(minecraft -> SiegeWeaponRenderer.DEV_POSE.put(w[i].getUUID(), since)));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot(name));
        steps.add(Step.once(minecraft -> SiegeWeaponRenderer.DEV_POSE.remove(w[i].getUUID())));
        steps.add(Step.waitTicks(5));
    }

    private static SiegeWeaponEntity spawn(ServerLevel level, SiegeType type, double x, double z, float yaw) {
        SiegeWeaponEntity weapon = ModEntities.siegeWeapon(type).create(level, EntitySpawnReason.COMMAND);
        weapon.snapTo(x, Showcase.ground, z, yaw, 0.0f);
        weapon.setAmmo(weapon.capacity());
        level.addFreshEntity(weapon);
        return weapon;
    }

    /** Wiese mit Fahrspuren, davor die Burgmauer mit Zinnen und einem Tor aus zwei Fichtentüren unter einem Fallgitter. */
    private static void build(ServerLevel level) {
        int g = Showcase.ground;
        Showcase.fill(level, new BlockPos(X0 - 24, g - 2, WALL - 8), new BlockPos(X0 + 48, g - 2, Z0 + 24), Blocks.DIRT.defaultBlockState());
        Showcase.fill(level, new BlockPos(X0 - 24, g - 1, WALL - 8), new BlockPos(X0 + 48, g - 1, Z0 + 24), Blocks.GRASS_BLOCK.defaultBlockState());
        Showcase.fill(level, new BlockPos(X0 - 24, g, WALL - 8), new BlockPos(X0 + 48, g + 18, Z0 + 24), Blocks.AIR.defaultBlockState());
        RandomSource random = RandomSource.create(42);
        // Fahrspuren und zertrampelte Stellen
        for (int i = 0; i < 70; i++) {
            int x = X0 - 16 + random.nextInt(60);
            int z = WALL + 2 + random.nextInt(Z0 + 20 - WALL);
            BlockState patch = random.nextInt(3) == 0 ? Blocks.COARSE_DIRT.defaultBlockState()
                    : random.nextBoolean() ? Blocks.DIRT_PATH.defaultBlockState() : Blocks.PODZOL.defaultBlockState();
            level.setBlock(new BlockPos(x, g - 1, z), patch, Block.UPDATE_CLIENTS);
        }
        for (int i = 0; i < 40; i++) {
            int x = X0 - 16 + random.nextInt(60);
            int z = WALL + 3 + random.nextInt(Z0 + 20 - WALL);
            if (level.getBlockState(new BlockPos(x, g - 1, z)).is(Blocks.GRASS_BLOCK)) {
                level.setBlock(new BlockPos(x, g, z), (random.nextBoolean() ? Blocks.SHORT_GRASS : Blocks.FERN).defaultBlockState(), Block.UPDATE_CLIENTS);
            }
        }
        // Burgmauer: zwei Blöcke dick, sieben hoch, mit Zinnen; etwas Moos und Risse
        for (int x = X0 - 14; x <= X0 + 46; x++) {
            for (int y = g; y <= g + 6; y++) {
                for (int z = WALL - 2; z <= WALL - 1; z++) {
                    int r = random.nextInt(14);
                    BlockState stone = r == 0 ? Blocks.MOSSY_STONE_BRICKS.defaultBlockState()
                            : r == 1 ? Blocks.CRACKED_STONE_BRICKS.defaultBlockState() : Blocks.STONE_BRICKS.defaultBlockState();
                    level.setBlock(new BlockPos(x, y, z), stone, Block.UPDATE_CLIENTS);
                }
            }
            if ((x & 1) == 0) {
                level.setBlock(new BlockPos(x, g + 7, WALL - 1), Blocks.STONE_BRICK_WALL.defaultBlockState(), Block.UPDATE_ALL);
            }
        }
        // Torhaus: Öffnung zwei breit, drei hoch; Fichtentüren, darüber das Fallgitter
        for (int x = GATE; x <= GATE + 1; x++) {
            for (int y = g; y <= g + 2; y++) {
                for (int z = WALL - 2; z <= WALL - 1; z++) {
                    level.setBlock(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                }
            }
            BlockState door = Blocks.SPRUCE_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.NORTH)
                    .setValue(DoorBlock.HINGE, x == GATE ? DoorHingeSide.RIGHT : DoorHingeSide.LEFT);
            level.setBlock(new BlockPos(x, g, WALL - 1), door.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER), Block.UPDATE_CLIENTS);
            level.setBlock(new BlockPos(x, g + 1, WALL - 1), door.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), Block.UPDATE_CLIENTS);
            level.setBlock(new BlockPos(x, g + 2, WALL - 1), Blocks.IRON_BARS.defaultBlockState(), Block.UPDATE_ALL);
        }
        // die Chunks des Platzes ticken (sonst schösse niemand – ein Zuschauer hält sie nicht wach)
        for (int cx = (X0 - 24) >> 4; cx <= (X0 + 48) >> 4; cx++) {
            for (int cz = (WALL - 8) >> 4; cz <= (Z0 + 24) >> 4; cz++) {
                level.setChunkForced(cx, cz, true);
            }
        }
    }

    /** Kamera: Auge (Höhe über dem Boden) mit Blick auf einen Punkt (Höhe über dem Boden). */
    private static Step look(double ex, double ey, double ez, double tx, double ty, double tz) {
        return Step.once(minecraft -> {
            int g = Showcase.ground;
            double lx = tx - ex;
            double ly = ty - ey;
            double lz = tz - ez;
            float yaw = (float) (-Math.atan2(lx, lz) * 180.0 / Math.PI);
            float pitch = (float) (-Math.atan2(ly, Math.sqrt(lx * lx + lz * lz)) * 180.0 / Math.PI);
            minecraft.options.setCameraType(CameraType.FIRST_PERSON);
            minecraft.player.connection.sendCommand(String.format(Locale.ROOT, "tp @s %.2f %.2f %.2f %.1f %.1f", ex, g + ey - 1.62, ez, yaw,
                    pitch));
        });
    }
}
