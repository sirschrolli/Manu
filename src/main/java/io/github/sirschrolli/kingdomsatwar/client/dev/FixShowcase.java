package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.client.army.WarTableScreen;
import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.worldgen.village.KingdomVillagePiece;
import io.github.sirschrolli.kingdomsatwar.worldgen.village.VillageBuilding;
import io.github.sirschrolli.kingdomsatwar.worldgen.village.VillageStyle;
import java.util.Deque;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Rotation;

/**
 * Vorführung der Verbesserungen: Betten im Obergeschoss stehen jetzt im Raum statt in der Wand, die Kräuter hängen an
 * ihrer Stange, Kronleuchter und Laternen an Ketten vom Dach, und der Feldherrentisch mit vielen Soldaten (feste
 * Reihenfolge, Rollbalken).
 */
final class FixShowcase {
    /** Hier stehen die Gebäude nebeneinander (je 48 Blöcke Abstand). */
    private static final int X0 = 7000;
    private static final int Z0 = 7000;

    private FixShowcase() {}

    static void fixes(Deque<Step> steps) {
        steps.add(Step.hud(false));
        steps.add(Step.command("gamemode spectator"));
        steps.add(Step.command("time set 6000"));
        steps.add(Step.command("gamerule advance_time false"));
        steps.add(Step.command("weather clear"));
        steps.add(Showcase.tp(X0 + 0.5, 20, Z0 - 20.5, 0, 30));
        steps.add(Step.waitTicks(60));
        steps.add(Step.waitForChunks(600));

        // 1) Wohnhaus: oben die Schlafkammer
        BlockPos[] house = new BlockPos[1];
        building(steps, VillageStyle.TIMBER, VillageBuilding.HOUSE, X0, house);
        // im Obergeschoss vorn in der Mitte, Blick nach hinten auf die Betten (Füße eine Stufe über dem Obergeschossboden)
        steps.add(Step.once(minecraft -> minecraft.player.connection.sendCommand(String.format(java.util.Locale.ROOT,
                "tp @s %.2f %.2f %.2f 0 24", house[0].getX() + 5.0, house[0].getY() + 6.0, house[0].getZ() + 1.35))));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("fix_0_house_beds"));

        // 2) Kräuterhaus: getrocknete Kräuter an der Stange, die Laterne an einer Kette
        BlockPos[] herbs = new BlockPos[1];
        building(steps, VillageStyle.TIMBER, VillageBuilding.HERB_HOUSE, X0 + 48, herbs);
        steps.add(Step.once(minecraft -> minecraft.player.connection.sendCommand(String.format(java.util.Locale.ROOT,
                "tp @s %.2f %.2f %.2f -40 -14", herbs[0].getX() + 1.6, herbs[0].getY() + 1.0, herbs[0].getZ() + 4.3))));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("fix_1_herb_house"));

        // 3) Kapelle im Lehmdorf: die Kronleuchter hängen an Ketten unter dem Flachdach
        BlockPos[] chapel = new BlockPos[1];
        building(steps, VillageStyle.CLAY, VillageBuilding.CHAPEL, X0 + 96, chapel);
        steps.add(Step.once(minecraft -> minecraft.player.connection.sendCommand(String.format(java.util.Locale.ROOT,
                "tp @s %.2f %.2f %.2f 0 -6", chapel[0].getX() + 4.5, chapel[0].getY() + 1.0, chapel[0].getZ() + 5.4))));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("fix_2_chapel"));

        // 4) Feldherrentisch mit 28 Soldaten
        int tz = Z0 - 60;
        int tx = X0;
        steps.add(Step.server(level -> {
            int g = Showcase.ground;
            Showcase.fill(level, new BlockPos(tx - 12, g - 1, tz - 12), new BlockPos(tx + 12, g - 1, tz + 12), Blocks.GRASS_BLOCK.defaultBlockState());
            Showcase.fill(level, new BlockPos(tx - 12, g, tz - 12), new BlockPos(tx + 12, g + 8, tz + 12), Blocks.AIR.defaultBlockState());
            level.setBlock(new BlockPos(tx, g, tz), ModBlocks.RECRUITMENT_TABLE.get().defaultBlockState()
                    .setValue(HorizontalDirectionalBlock.FACING, Direction.SOUTH), Block.UPDATE_ALL);
            Player owner = level.players().isEmpty() ? null : level.players().getFirst();
            SoldierType[] types = {SoldierType.SWORDSMAN, SoldierType.KNIGHT, SoldierType.ARCHER, SoldierType.CAPTAIN, SoldierType.SPEARMAN,
                    SoldierType.CROSSBOWMAN, SoldierType.SHIELDBEARER};
            int n = 0;
            for (int ring = 3; ring <= 7; ring += 2) {
                int count = ring * 3;
                for (int k = 0; k < count && n < 28; k++, n++) {
                    double a = k * Math.PI * 2.0 / count;
                    int x = tx + (int) Math.round(Math.cos(a) * ring);
                    int z = tz + (int) Math.round(Math.sin(a) * ring);
                    float yaw = (float) Math.toDegrees(Math.atan2(-(tx - x), tz - z));
                    ArmyShowcase.spawn(level, types[n % types.length], n % 5, x, z, yaw, owner);
                }
            }
        }));
        steps.add(Step.waitTicks(30));
        steps.add(Showcase.tp(tx + 0.5, 1.4, tz + 2.6, 180, 32));
        steps.add(Step.waitTicks(25));
        steps.add(Step.command("gamemode creative"));
        steps.add(Step.waitTicks(10));
        steps.add(Step.hud(true));
        steps.add(Step.screen(minecraft -> new WarTableScreen(new BlockPos(tx, Showcase.ground, tz), 40, 28, true)));
        steps.add(Step.waitTicks(10));
        steps.add(Step.once(Showcase::parkMouse));
        steps.add(Step.waitTicks(15));
        steps.add(Step.screenshot("fix_3_war_table"));
        // weiter unten in der Liste (zehn Schritte mit dem Mausrad)
        steps.add(Step.once(minecraft -> {
            if (minecraft.gui.screen() instanceof WarTableScreen screen) {
                for (int i = 0; i < 10; i++) {
                    screen.mouseScrolled(0, 0, 0, -1);
                }
            }
        }));
        steps.add(Step.waitTicks(40));
        steps.add(Step.screenshot("fix_4_war_table_scrolled"));
        steps.add(Step.screen(minecraft -> null));
        steps.add(Step.command("gamemode spectator"));
        steps.add(Step.hud(false));
        steps.add(Step.waitTicks(5));
    }

    /** Baut ein Dorfgebäude auf vorbereitetem Boden bei x (Ursprung vorne links in {@code at[0]}). */
    private static void building(Deque<Step> steps, VillageStyle style, VillageBuilding kind, int x, BlockPos[] at) {
        steps.add(Showcase.tp(x + 0.5, 20, Z0 - 20.5, 0, 30));
        steps.add(Step.waitTicks(10));
        steps.add(Step.waitForChunks(400));
        steps.add(Step.server(level -> {
            BlockPos origin = new BlockPos(x - kind.width / 2, Showcase.ground - 1, Z0);
            VillageShowcase.prepare(level, style, origin, kind);
            KingdomVillagePiece piece = KingdomVillagePiece.single(style, 0, kind, origin, Rotation.NONE, 31L * kind.ordinal() + 7);
            VillageShowcase.build(level, piece);
            at[0] = origin;
        }));
        steps.add(Step.waitTicks(30));
    }
}
