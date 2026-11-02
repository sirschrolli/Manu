package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.block.entity.MercenaryBoardBlockEntity;
import io.github.sirschrolli.kingdomsatwar.client.army.MercenaryScreen;
import io.github.sirschrolli.kingdomsatwar.client.army.WarTableScreen;
import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import java.util.Deque;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;

/**
 * Vorführung der Armee: Soldaten in allen Rängen (vorn und hinten), Heilen und Ausbilden am Feldherrentisch und die
 * Söldnertafel mit ihren wartenden Söldnern.
 */
final class ArmyShowcase {
    private ArmyShowcase() {}

    static void ranks(Deque<Step> steps) {
        int oz = -140;
        steps.add(Step.server(level -> {
            int ground = Showcase.ground;
            Showcase.fill(level, new BlockPos(-12, ground - 1, oz - 30), new BlockPos(40, ground - 1, oz + 14), Blocks.GRASS_BLOCK.defaultBlockState());
            Showcase.fill(level, new BlockPos(-12, ground, oz - 30), new BlockPos(40, ground + 8, oz + 14), Blocks.AIR.defaultBlockState());
            // Reihe: jede Art als Rekrut, Veteran und Elite
            int x = 0;
            for (SoldierType type : List.of(SoldierType.SWORDSMAN, SoldierType.KNIGHT, SoldierType.ARCHER, SoldierType.CAPTAIN)) {
                for (int rank = 0; rank <= SoldierEntity.MAX_RANK; rank++) {
                    spawn(level, type, rank, x, oz, 0.0f, null);
                    x += 2;
                }
                x += 1;
            }
        }));
        steps.add(Step.waitTicks(40));
        steps.add(Step.hud(false));
        steps.add(Showcase.tp(11.5, 1.8, oz + 9.0, 180, 4));
        steps.add(Step.waitForChunks(200));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("ranks_front"));
        steps.add(Showcase.tp(3.0, 1.7, oz + 3.2, 180, 8));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("ranks_close"));
        steps.add(Showcase.tp(11.5, 1.8, oz - 7.0, 0, 4));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("ranks_back"));

        // Ausbilden: eigene Soldaten um den Tisch
        int tz = oz - 20;
        steps.add(Step.server(level -> {
            int ground = Showcase.ground;
            level.setBlock(new BlockPos(30, ground, tz), ModBlocks.RECRUITMENT_TABLE.get().defaultBlockState()
                    .setValue(HorizontalDirectionalBlock.FACING, Direction.SOUTH), Block.UPDATE_ALL);
            Player owner = level.players().isEmpty() ? null : level.players().getFirst();
            spawn(level, SoldierType.KNIGHT, 1, 28, tz + 2, 180.0f, owner);
            spawn(level, SoldierType.SWORDSMAN, 2, 32, tz + 2, 180.0f, owner);
            spawn(level, SoldierType.ARCHER, 0, 27, tz - 1, 90.0f, owner);
            spawn(level, SoldierType.CAPTAIN, 2, 33, tz - 1, -90.0f, owner);
        }));
        steps.add(Step.waitTicks(20));
        steps.add(Showcase.tp(30.5, 1.4, tz + 2.6, 180, 32));
        steps.add(Step.waitTicks(25));
        steps.add(Step.command("gamemode creative"));
        steps.add(Step.waitTicks(10));
        steps.add(Step.hud(true));
        steps.add(Step.server(level -> {
            // zwei Verwundete im Feldlager: der Feldscher nennt seinen Lohn
            for (SoldierEntity soldier : level.getEntitiesOfClass(SoldierEntity.class, new net.minecraft.world.phys.AABB(new BlockPos(30, Showcase.ground, tz)).inflate(8.0))) {
                if (soldier.getSoldierType() == SoldierType.KNIGHT || soldier.getSoldierType() == SoldierType.ARCHER) {
                    soldier.setHealth(soldier.getMaxHealth() * 0.4f);
                }
            }
        }));
        steps.add(Step.waitTicks(5));
        steps.add(Step.screen(minecraft -> new WarTableScreen(new BlockPos(30, Showcase.ground, tz), 32)));
        steps.add(Step.waitTicks(10));
        steps.add(Step.once(Showcase::parkMouse));
        steps.add(Step.waitTicks(15));
        steps.add(Step.screenshot("war_table_screen"));
        steps.add(Step.screen(minecraft -> null));
        steps.add(Step.command("gamemode spectator"));
        steps.add(Step.hud(false));

        // Söldnertafel mit drei wartenden Söldnern
        int bz = oz - 34;
        steps.add(Step.server(level -> {
            int ground = Showcase.ground;
            Showcase.fill(level, new BlockPos(-12, ground - 1, bz - 10), new BlockPos(20, ground - 1, bz + 10), Blocks.COARSE_DIRT.defaultBlockState());
            Showcase.fill(level, new BlockPos(-12, ground, bz - 10), new BlockPos(20, ground + 8, bz + 10), Blocks.AIR.defaultBlockState());
            BlockPos board = new BlockPos(4, ground, bz);
            level.setBlock(board, ModBlocks.MERCENARY_BOARD.get().defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.SOUTH),
                    Block.UPDATE_ALL);
            if (level.getBlockEntity(board) instanceof MercenaryBoardBlockEntity entity) {
                entity.setup(List.of(board.offset(-2, 0, 2), board.offset(2, 0, 2), board.offset(3, 0, 1), board.offset(-1, 0, 3)),
                        "Lindenau");
                entity.refill(level, false);
            }
        }));
        steps.add(Step.waitTicks(30));
        steps.add(Showcase.tp(4.5, 2.2, bz + 7.5, 180, 12));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("mercenary_board"));
        steps.add(Showcase.tp(4.5, 0.0, bz + 1.8, 180, 32));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("mercenary_board_close"));
        steps.add(Step.command("gamemode creative"));
        steps.add(Step.waitTicks(10));
        steps.add(Step.hud(true));
        steps.add(Step.screen(minecraft -> new MercenaryScreen(new BlockPos(4, Showcase.ground, bz), 32, "Lindenau")));
        steps.add(Step.waitTicks(10));
        steps.add(Step.once(Showcase::parkMouse));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screenshot("mercenary_screen_1"));
        steps.add(Step.once(minecraft -> {
            if (minecraft.gui.screen() instanceof MercenaryScreen screen) {
                screen.select(1);
            }
        }));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screenshot("mercenary_screen_2"));
        steps.add(Step.screen(minecraft -> null));
        steps.add(Step.command("gamemode spectator"));
        steps.add(Step.waitTicks(5));
    }

    private static void spawn(ServerLevel level, SoldierType type, int rank, int x, int z, float yaw, Player owner) {
        SoldierEntity soldier = ModEntities.SOLDIERS.get(type).get().create(level, EntitySpawnReason.COMMAND);
        if (soldier == null) {
            return;
        }
        soldier.equip();
        soldier.setVariant(Math.min(rank, type.variants() - 1));
        soldier.giveRandomName();
        soldier.setRank(rank);
        soldier.setHealth(soldier.getMaxHealth());
        soldier.setNoAi(true);
        soldier.snapTo(x + 0.5, Showcase.ground, z + 0.5, yaw, 0);
        soldier.setYBodyRot(yaw);
        soldier.setYHeadRot(yaw);
        if (owner != null) {
            soldier.setOwner(owner);
            soldier.setMode(SoldierEntity.MODE_HOLD, soldier.blockPosition());
        }
        level.addFreshEntity(soldier);
    }
}
