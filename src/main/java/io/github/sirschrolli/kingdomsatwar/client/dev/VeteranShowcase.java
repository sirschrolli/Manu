package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.army.Veterancy;
import io.github.sirschrolli.kingdomsatwar.client.army.WarTableScreen;
import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import java.util.Deque;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

/**
 * Vorführung der Kampferfahrung: fünf Ritter vom Rekruten bis zur Legende (mit Schärpe, Orden und Lorbeerkranz), das
 * Schild mit Stufe und Erfahrung unter dem Fadenkreuz, ein Aufstieg mit Funken und Nachricht und der Feldherrentisch,
 * an dem ein Elitekämpfer auf Stufe 10 zum Meister befördert werden kann.
 */
final class VeteranShowcase {
    private static final int OZ = -60;
    private static final int TZ = OZ - 40;

    private VeteranShowcase() {}

    static void veterans(Deque<Step> steps) {
        // die Reihe: Rekrut, Veteran, Elite, Meister, Legende – je mit passender Stufe
        steps.add(Step.server(level -> {
            int ground = Showcase.ground;
            Showcase.fill(level, new BlockPos(-14, ground - 1, OZ - 60), new BlockPos(24, ground - 1, OZ + 14), Blocks.GRASS_BLOCK.defaultBlockState());
            Showcase.fill(level, new BlockPos(-14, ground, OZ - 60), new BlockPos(24, ground + 8, OZ + 14), Blocks.AIR.defaultBlockState());
            Showcase.fill(level, new BlockPos(-3, ground - 1, OZ - 1), new BlockPos(11, ground - 1, OZ + 1), Blocks.COARSE_DIRT.defaultBlockState());
            Player owner = level.players().isEmpty() ? null : level.players().getFirst();
            int[] levels = {2, 4, 8, 12, 18};
            for (int rank = 0; rank <= SoldierEntity.MAX_RANK; rank++) {
                spawn(level, SoldierType.KNIGHT, rank, levels[rank], rank * 2, OZ, 0.0f, owner, rank);
            }
            // für das erste Bild stehen alle Namen mit ihren Sternen über den Köpfen
            level.getEntitiesOfClass(SoldierEntity.class, new AABB(new BlockPos(4, ground, OZ)).inflate(12, 4, 3))
                    .forEach(s -> s.setCustomNameVisible(true));
        }));
        steps.add(Step.waitTicks(40));
        steps.add(Step.hud(false));
        steps.add(Showcase.tp(4.5, 1.2, OZ + 4.3, 180, 4));
        steps.add(Step.waitForChunks(200));
        steps.add(Step.waitTicks(40));
        steps.add(Step.screenshot("veterans_0_ranks"));
        steps.add(Step.server(level -> level.getEntitiesOfClass(SoldierEntity.class, new AABB(new BlockPos(4, Showcase.ground, OZ)).inflate(12, 4, 3))
                .forEach(s -> s.setCustomNameVisible(false))));

        // das Schild unter dem Fadenkreuz: der Blick auf die Legende
        steps.add(Step.command("gamemode survival"));
        steps.add(Step.command("clear @s"));
        steps.add(Step.command("item replace entity @s weapon.mainhand with kingdomsatwar:command_baton"));
        steps.add(Step.command("item replace entity @s hotbar.1 with minecraft:iron_sword"));
        steps.add(Showcase.tp(8.5, 0.0, OZ + 3.4, 180, 14));
        steps.add(Step.hud(true));
        steps.add(Step.once(minecraft -> minecraft.options.chatVisibility().set(ChatVisiblity.HIDDEN)));
        steps.add(Step.waitTicks(30));
        steps.add(Step.once(minecraft -> minecraft.gui.toastManager().clear()));
        steps.add(Step.waitTicks(2));
        steps.add(Step.screenshot("veterans_1_hud"));

        // ein Aufstieg: der Elitekämpfer erreicht Stufe 10 – jetzt kann er Meister werden
        steps.add(Showcase.tp(4.5, 0.0, OZ + 3.0, 180, 12));
        steps.add(Step.once(minecraft -> minecraft.options.chatVisibility().set(ChatVisiblity.FULL)));
        steps.add(Step.once(minecraft -> minecraft.gui.hud.getChat().clearMessages(false)));
        steps.add(Step.waitTicks(20));
        steps.add(Step.once(minecraft -> minecraft.gui.toastManager().clear()));
        steps.add(Step.server(level -> {
            SoldierEntity elite = byRank(level, OZ, 2);
            if (elite != null) {
                Veterancy.award(elite, Veterancy.total(10) - elite.getCombatExperience() + 3);
            }
        }));
        steps.add(Step.waitTicks(6));
        steps.add(Step.screenshot("veterans_2_levelup"));

        // der Feldherrentisch: drei eigene Soldaten im Feldlager, der nächste am Tisch ist Elite auf Stufe 10
        steps.add(Step.server(level -> {
            int ground = Showcase.ground;
            Showcase.fill(level, new BlockPos(-2, ground - 1, TZ - 6), new BlockPos(14, ground - 1, TZ + 6), Blocks.COARSE_DIRT.defaultBlockState());
            level.setBlock(new BlockPos(6, ground, TZ), ModBlocks.RECRUITMENT_TABLE.get().defaultBlockState()
                    .setValue(HorizontalDirectionalBlock.FACING, Direction.SOUTH), Block.UPDATE_ALL);
            Player owner = level.players().isEmpty() ? null : level.players().getFirst();
            spawn(level, SoldierType.SWORDSMAN, 2, 10, 5, TZ + 1, 180.0f, owner, 1);
            spawn(level, SoldierType.ARCHER, 1, 4, 8, TZ + 2, 180.0f, owner, 0);
            spawn(level, SoldierType.CAPTAIN, 4, 20, 3, TZ - 2, 90.0f, owner, 0);
        }));
        steps.add(Step.command("gamemode creative"));
        steps.add(Showcase.tp(6.5, 0.0, TZ + 3.5, 180, 30));
        steps.add(Step.once(minecraft -> minecraft.options.chatVisibility().set(ChatVisiblity.HIDDEN)));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screen(minecraft -> new WarTableScreen(new BlockPos(6, Showcase.ground, TZ), 32, 3, true)));
        steps.add(Step.waitTicks(10));
        steps.add(Step.once(minecraft -> hover(minecraft, 128 + 42, 149 + 10)));
        steps.add(Step.once(minecraft -> minecraft.gui.toastManager().clear()));
        steps.add(Step.waitTicks(15));
        steps.add(Step.screenshot("veterans_3_table"));
        steps.add(Step.screen(minecraft -> null));
        steps.add(Step.command("gamemode spectator"));
        steps.add(Step.hud(false));
    }

    /** Mauszeiger über eine Stelle des Feldherrentischs (Koordinaten im Fenster). */
    private static void hover(Minecraft minecraft, int guiX, int guiY) {
        int left = (minecraft.getWindow().getGuiScaledWidth() - 306) / 2;
        int top = (minecraft.getWindow().getGuiScaledHeight() - 176) / 2;
        double scale = minecraft.getWindow().getGuiScale();
        try {
            for (String name : new String[] {"xpos", "ypos"}) {
                java.lang.reflect.Field field = minecraft.mouseHandler.getClass().getDeclaredField(name);
                field.setAccessible(true);
                field.setDouble(minecraft.mouseHandler, name.equals("xpos") ? (left + guiX) * scale : (top + guiY) * scale);
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private static @Nullable SoldierEntity byRank(ServerLevel level, int z, int rank) {
        return level.getEntitiesOfClass(SoldierEntity.class, new AABB(new BlockPos(0, Showcase.ground, z)).inflate(20, 4, 3),
                s -> s.getRank() == rank).stream().findFirst().orElse(null);
    }

    private static void spawn(ServerLevel level, SoldierType type, int rank, int combatLevel, int x, int z, float yaw, @Nullable Player owner,
            int variant) {
        SoldierEntity soldier = ModEntities.SOLDIERS.get(type).get().create(level, EntitySpawnReason.COMMAND);
        if (soldier == null) {
            return;
        }
        soldier.equip();
        soldier.setVariant(Math.min(variant, type.variants() - 1));
        soldier.giveRandomName();
        soldier.setCombatExperience(Veterancy.total(combatLevel) + (Veterancy.total(combatLevel + 1) - Veterancy.total(combatLevel)) / 3);
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
        KingdomsAtWar.LOGGER.info("[Showcase] {} Rang {} Stufe {}", soldier.getPersonalName(), rank, soldier.getCombatLevel());
    }
}
