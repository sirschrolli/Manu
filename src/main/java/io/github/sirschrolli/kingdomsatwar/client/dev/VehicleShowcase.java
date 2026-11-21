package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import io.github.sirschrolli.kingdomsatwar.siege.SiegeTowerEntity;
import io.github.sirschrolli.kingdomsatwar.siege.SiegeType;
import io.github.sirschrolli.kingdomsatwar.siege.SiegeWagonEntity;
import io.github.sirschrolli.kingdomsatwar.siege.SiegeWeaponEntity;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.CameraType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Vorführung der Fahrzeuge: drei Belagerungstürme in ihren drei Höhen (7, 10 und 13 Blöcke) vor einer Burgmauer, ein Turm
 * mit gefallener Brücke, über die die Krieger auf den Wehrgang stürmen (von der Seite und vom Turm aus), ein Pferd, das den
 * vollen Planwagen zieht, drei Wagen, an denen man sieht, wie voll sie sind, und der Laderaum.
 */
final class VehicleShowcase {
    private static final int X0 = 300;
    private static final int Z0 = -300;
    /** Vorderseite der Mauer (die Mauer liegt bei z = WALL − 4 bis WALL − 1). */
    private static final int WALL = Z0 - 30;
    /** Höhe des Wehrgangs über dem Boden (die Zinnen reichen einen Block höher, so hoch wie die Plattform mit zwei Stockwerken). */
    private static final int WALK = 12;

    private VehicleShowcase() {}

    static void vehicles(Deque<Step> steps) {
        SiegeTowerEntity[] towers = new SiegeTowerEntity[4];
        SiegeWagonEntity[] wagons = new SiegeWagonEntity[4];
        List<SoldierEntity> crew = new ArrayList<>();
        steps.add(Step.hud(false));
        steps.add(Step.command("gamemode spectator"));
        steps.add(Step.server(level -> {
            build(level);
            ServerPlayer player = level.getServer().getPlayerList().getPlayers().getFirst();
            int g = Showcase.ground;
            // drei Türme in ihren Höhen im Feld, der vierte steht an der Mauer
            for (int i = 0; i < 3; i++) {
                towers[i] = tower(level, player, X0 + 44.5 + i * 9, WALL + 15.5, i);
            }
            towers[3] = tower(level, player, X0 + 0.5, WALL + 3.5, 2);
            String[] kinds = {"KNIGHT", "SWORDSMAN", "SWORDSMAN", "ARCHER", "KNIGHT"};
            for (int i = 0; i < kinds.length; i++) {
                SoldierEntity soldier = ModEntities.SOLDIERS.get(SoldierType.valueOf(kinds[i])).get().create(level, EntitySpawnReason.COMMAND);
                if (soldier == null) {
                    continue;
                }
                soldier.snapTo(X0 + 0.5 + (i - 2) * 0.8, g, WALL + 7.5, 180.0f, 0.0f);
                soldier.setPersistenceRequired();
                level.addFreshEntity(soldier);
                soldier.recruitBy(player);
                soldier.startRiding(towers[3], true, false);
                crew.add(soldier);
            }
            // der volle Planwagen am Pferd und drei Wagen mit wenig, halber und voller Ladung
            wagons[0] = wagon(level, player, X0 - 23.5, WALL + 40.5, -90.0f, 54);
            var horse = ModEntities.FARM_HORSE.get().create(level, EntitySpawnReason.COMMAND);
            if (horse != null) {
                horse.snapTo(X0 - 23.5 + 4.6, g, WALL + 40.5, -90.0f, 0.0f);
                horse.setYHeadRot(-90.0f);
                horse.setYBodyRot(-90.0f);
                horse.setTamed(true);
                horse.setItemSlot(EquipmentSlot.SADDLE, new ItemStack(Items.SADDLE));
                horse.setNoAi(true);
                horse.setPersistenceRequired();
                level.addFreshEntity(horse);
                wagons[0].setLeashedTo(horse, true);
                SoldierEntity rider = ModEntities.SOLDIERS.get(SoldierType.KNIGHT).get().create(level, EntitySpawnReason.COMMAND);
                if (rider != null) {
                    rider.snapTo(horse.getX(), g, horse.getZ(), -90.0f, 0.0f);
                    rider.setPersistenceRequired();
                    level.addFreshEntity(rider);
                    rider.recruitBy(player);
                    rider.setNoAi(true);
                    rider.startRiding(horse, true, false);
                }
            }
            int[] fill = {6, 30, 54};
            for (int i = 0; i < 3; i++) {
                wagons[i + 1] = wagon(level, player, X0 + 6.5 + i * 7, WALL + 44.5, 0.0f, fill[i]);
            }
        }));
        steps.add(Step.until(() -> towers[3] != null && wagons[3] != null, 400));
        steps.add(Step.waitTicks(20));
        steps.add(Step.waitForChunks(600));

        // 1. drei Höhen: 7, 10 und 13 Blöcke
        steps.add(look(X0 + 37.0, 5.5, WALL + 31.0, X0 + 53.5, 7.0, WALL + 14.0));
        steps.add(Step.waitTicks(60));
        steps.add(Step.screenshot("vehicle_0_towers"));

        // 2. Sturm: die Brücke fällt auf die Zinnen, die Krieger laufen hinüber auf den Wehrgang
        steps.add(Step.server(level -> {
            ServerPlayer player = level.getServer().getPlayerList().getPlayers().getFirst();
            boolean down = towers[3].lower(level, player);
            KingdomsAtWar.LOGGER.info("[Showcase] Brücke fällt: {}, Krieger auf dem Turm: {}", down, towers[3].getPassengers().size());
        }));
        steps.add(Step.waitTicks(46));
        steps.add(look(X0 + 7.0, 14.6, WALL + 8.0, X0 + 0.5, 12.6, WALL - 1.5));
        steps.add(Step.waitTicks(24));
        steps.add(Step.screenshot("vehicle_1_assault"));
        // vom Turm aus: über die Brücke auf den Wehrgang
        steps.add(Step.once(minecraft -> minecraft.player.connection.sendCommand(String.format(Locale.ROOT, "tp @s %.2f %.2f %.2f 180 22",
                X0 + 0.5, Showcase.ground + 13.0 + 0.1, WALL + 4.6))));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("vehicle_2_bridge"));

        // 3. der volle Planwagen am Pferd, ein Ritter reitet vorn
        steps.add(look(X0 - 15.5, 2.6, WALL + 45.0, X0 - 21.0, 1.4, WALL + 40.5));
        steps.add(Step.waitTicks(40));
        steps.add(Step.screenshot("vehicle_3_wagon"));

        // 4. drei Wagen: wenig, halb, voll
        steps.add(look(X0 + 13.5, 4.6, WALL + 38.0, X0 + 13.5, 1.0, WALL + 44.5));
        steps.add(Step.waitTicks(40));
        steps.add(Step.screenshot("vehicle_4_cargo"));

        // 5. der Laderaum: sechs Reihen
        steps.add(Step.command("gamemode survival"));
        steps.add(Step.once(minecraft -> minecraft.player.connection.sendCommand(String.format(Locale.ROOT, "tp @s %.2f %.2f %.2f 0 30",
                X0 + 20.5, (double) Showcase.ground, WALL + 41.5))));
        steps.add(Step.waitTicks(10));
        steps.add(Step.hud(true));
        steps.add(Step.server(level -> {
            ServerPlayer player = level.getServer().getPlayerList().getPlayers().getFirst();
            player.getInventory().clearContent();
            player.getInventory().setItem(0, new ItemStack(Items.IRON_SWORD));
            player.getInventory().setItem(1, new ItemStack(ModItems.COMMAND_BATON.get()));
            player.getInventory().setItem(2, new ItemStack(Items.BREAD, 16));
            wagons[3].openCargo(player);
        }));
        steps.add(Step.waitTicks(10));
        steps.add(Step.once(Showcase::parkMouse));
        steps.add(Step.once(minecraft -> minecraft.gui.toastManager().clear()));
        steps.add(Step.waitTicks(6));
        steps.add(Step.screenshot("vehicle_5_hold"));
        steps.add(Step.screen(minecraft -> null));
        steps.add(Step.hud(false));
        steps.add(Step.command("gamemode spectator"));
    }

    private static SiegeTowerEntity tower(ServerLevel level, ServerPlayer owner, double x, double z, int storeys) {
        SiegeTowerEntity tower = (SiegeTowerEntity) ModEntities.siegeWeapon(SiegeType.SIEGE_TOWER).create(level, EntitySpawnReason.COMMAND);
        tower.snapTo(x, Showcase.ground, z, 180.0f, 0.0f);
        tower.setYHeadRot(180.0f);
        tower.setOwner(owner.getUUID());
        tower.setStoreys(storeys);
        level.addFreshEntity(tower);
        return tower;
    }

    /** Ein Planwagen mit {@code slots} belegten Fächern (Baustoffe, Vorräte, Waffen). */
    private static SiegeWagonEntity wagon(ServerLevel level, ServerPlayer owner, double x, double z, float yaw, int slots) {
        SiegeWagonEntity wagon = (SiegeWagonEntity) ModEntities.siegeWeapon(SiegeType.WAGON).create(level, EntitySpawnReason.COMMAND);
        wagon.snapTo(x, Showcase.ground, z, yaw, 0.0f);
        wagon.setYHeadRot(yaw);
        wagon.setOwner(owner.getUUID());
        level.addFreshEntity(wagon);
        ItemStack[] goods = {new ItemStack(Blocks.STONE_BRICKS, 64), new ItemStack(Blocks.OAK_PLANKS, 64), new ItemStack(Items.BREAD, 32),
                new ItemStack(ModItems.SIEGE_STONE.get(), 16), new ItemStack(Items.ARROW, 64), new ItemStack(Blocks.OAK_LOG, 64),
                new ItemStack(Items.IRON_INGOT, 32), new ItemStack(Items.COOKED_BEEF, 24), new ItemStack(ModItems.BALLISTA_BOLT.get(), 16),
                new ItemStack(Blocks.COBBLESTONE, 64), new ItemStack(Items.LADDER, 32), new ItemStack(Items.TORCH, 64), new ItemStack(Items.APPLE, 32),
                new ItemStack(Blocks.GLASS, 32), new ItemStack(Items.LEATHER, 24), new ItemStack(Items.STRING, 32), new ItemStack(Items.WHEAT, 48),
                new ItemStack(Items.COAL, 64)};
        for (int i = 0; i < slots && i < wagon.cargo().getContainerSize(); i++) {
            wagon.cargo().setItem(i, goods[(i * 7) % goods.length].copy());
        }
        return wagon;
    }

    /** Wiese mit Fahrweg vor einer Burgmauer: zwölf Blöcke hoch, Zinnen an der Vorderkante, zwei Mauertürme. */
    private static void build(ServerLevel level) {
        int g = Showcase.ground;
        Showcase.fill(level, new BlockPos(X0 - 50, g - 2, WALL - 12), new BlockPos(X0 + 90, g - 2, WALL + 64), Blocks.DIRT.defaultBlockState());
        Showcase.fill(level, new BlockPos(X0 - 50, g - 1, WALL - 12), new BlockPos(X0 + 90, g - 1, WALL + 64), Blocks.GRASS_BLOCK.defaultBlockState());
        Showcase.fill(level, new BlockPos(X0 - 50, g, WALL - 12), new BlockPos(X0 + 90, g + 26, WALL + 64), Blocks.AIR.defaultBlockState());
        RandomSource random = RandomSource.create(7);
        // Fahrweg zu den Wagen und vor der Mauer zertrampelte Erde
        for (int x = X0 - 40; x <= X0 + 40; x++) {
            for (int dz = -1; dz <= 1; dz++) {
                int z = WALL + 41 + dz + (int) Math.round(Math.sin(x * 0.08) * 1.5);
                level.setBlock(new BlockPos(x, g - 1, z), (random.nextInt(4) == 0 ? Blocks.COARSE_DIRT : Blocks.DIRT_PATH).defaultBlockState(),
                        Block.UPDATE_CLIENTS);
            }
        }
        for (int i = 0; i < 160; i++) {
            int x = X0 - 30 + random.nextInt(100);
            int z = WALL + 1 + random.nextInt(24);
            level.setBlock(new BlockPos(x, g - 1, z), (random.nextBoolean() ? Blocks.COARSE_DIRT : Blocks.PODZOL).defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        for (int i = 0; i < 90; i++) {
            int x = X0 - 45 + random.nextInt(130);
            int z = WALL + 26 + random.nextInt(36);
            BlockPos pos = new BlockPos(x, g, z);
            if (level.getBlockState(pos.below()).is(Blocks.GRASS_BLOCK)) {
                level.setBlock(pos, (random.nextInt(3) == 0 ? Blocks.FERN : random.nextInt(5) == 0 ? Blocks.POPPY : Blocks.SHORT_GRASS)
                        .defaultBlockState(), Block.UPDATE_CLIENTS);
            }
        }
        // die Mauer: vier Blöcke dick, Wehrgang auf WALK, Zinnen vorn
        for (int x = X0 - 40; x <= X0 + 80; x++) {
            for (int z = WALL - 4; z <= WALL - 1; z++) {
                for (int y = g; y < g + WALK; y++) {
                    level.setBlock(new BlockPos(x, y, z), stone(random), Block.UPDATE_CLIENTS);
                }
            }
            if ((x & 1) == 0) {
                level.setBlock(new BlockPos(x, g + WALK, WALL - 1), stone(random), Block.UPDATE_CLIENTS);
            }
            level.setBlock(new BlockPos(x, g + WALK, WALL - 4), Blocks.STONE_BRICK_WALL.defaultBlockState(), Block.UPDATE_ALL);
        }
        // zwei Mauertürme mit Fackeln
        for (int tx : new int[] {X0 - 14, X0 + 22}) {
            for (int x = tx - 2; x <= tx + 2; x++) {
                for (int z = WALL - 6; z <= WALL; z++) {
                    for (int y = g; y < g + WALK + 6; y++) {
                        level.setBlock(new BlockPos(x, y, z), stone(random), Block.UPDATE_CLIENTS);
                    }
                    if ((x + z) % 2 == 0) {
                        level.setBlock(new BlockPos(x, g + WALK + 6, z), stone(random), Block.UPDATE_CLIENTS);
                    }
                }
            }
            level.setBlock(new BlockPos(tx, g + WALK + 2, WALL + 1), Blocks.WALL_TORCH.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.WallTorchBlock.FACING, net.minecraft.core.Direction.SOUTH), Block.UPDATE_ALL);
        }
        for (int cx = (X0 - 50) >> 4; cx <= (X0 + 90) >> 4; cx++) {
            for (int cz = (WALL - 12) >> 4; cz <= (WALL + 64) >> 4; cz++) {
                level.setChunkForced(cx, cz, true);
            }
        }
    }

    private static BlockState stone(RandomSource random) {
        int r = random.nextInt(12);
        return r == 0 ? Blocks.MOSSY_STONE_BRICKS.defaultBlockState() : r == 1 ? Blocks.CRACKED_STONE_BRICKS.defaultBlockState()
                : Blocks.STONE_BRICKS.defaultBlockState();
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
            minecraft.player.connection.sendCommand(String.format(Locale.ROOT, "tp @s %.2f %.2f %.2f %.1f %.1f", ex, g + ey - 1.62, ez, yaw, pitch));
        });
    }
}
