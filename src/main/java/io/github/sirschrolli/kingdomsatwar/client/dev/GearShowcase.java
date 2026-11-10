package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import java.util.Deque;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;

/**
 * Vorführung der Waffen und Rüstungen aus Monsterteilen (0.70): vier Schaufiguren (Mannequins) auf einer Waldlichtung –
 * im Werwolfspelz mit Krallenklinge, im Glutwurm-Panzer mit Glutzahn, mit Moorkeule und mit Seelensense; von vorn, aus der
 * Nähe, von der Seite und von hinten, dazu bei Nacht (die glühenden Teile leuchten).
 */
final class GearShowcase {
    private static final int OZ = 1800;

    private GearShowcase() {}

    static void gear(Deque<Step> steps) {
        steps.add(Step.command("difficulty peaceful"));
        steps.add(Step.server(level -> {
            int g = Showcase.ground;
            Showcase.fill(level, new BlockPos(-30, g - 1, OZ - 20), new BlockPos(30, g - 1, OZ + 30), Blocks.GRASS_BLOCK.defaultBlockState());
            Showcase.fill(level, new BlockPos(-30, g, OZ - 20), new BlockPos(30, g + 12, OZ + 30), Blocks.AIR.defaultBlockState());
            Showcase.fill(level, new BlockPos(-8, g - 1, OZ - 2), new BlockPos(8, g - 1, OZ + 3), Blocks.POLISHED_ANDESITE.defaultBlockState());
            Showcase.fill(level, new BlockPos(-8, g - 1, OZ + 4), new BlockPos(8, g - 1, OZ + 4), Blocks.MOSSY_STONE_BRICKS.defaultBlockState());
            // eine Hecke als Hintergrund
            Showcase.fill(level, new BlockPos(-14, g, OZ + 7), new BlockPos(14, g + 3, OZ + 8),
                    Blocks.SPRUCE_LEAVES.defaultBlockState().setValue(net.minecraft.world.level.block.LeavesBlock.PERSISTENT, true));
        }));
        steps.add(Step.waitTicks(10));
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> minecraft.options.fov().set(50)));
        steps.add(Step.command("time set 6000"));
        steps.add(look(0.0, 0.6, OZ - 6.5, 0.0, 1.3, OZ + 0.5));
        steps.add(Step.waitTicks(60));
        steps.add(Step.waitForChunks(600));
        // erst wenn die Kamera da ist (sonst sind die Chunks nicht geladen)
        mannequin(steps, -4.5, "kingdomsatwar:werewolf_pelt", "kingdomsatwar:claw_blade");
        mannequin(steps, -1.5, "kingdomsatwar:ember_scale", "kingdomsatwar:ember_fang_blade");
        mannequin(steps, 1.5, null, "kingdomsatwar:bog_club");
        mannequin(steps, 4.5, null, "kingdomsatwar:soul_scythe");
        steps.add(Step.waitTicks(40));
        steps.add(Step.screenshot("gear_overview"));
        steps.add(look(-3.6, 0.9, OZ - 1.6, -4.5, 1.5, OZ + 0.5));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("gear_pelt_close"));
        steps.add(look(-6.2, 1.0, OZ - 1.0, -4.5, 1.7, OZ + 0.5));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("gear_pelt_side"));
        steps.add(look(-0.6, 0.9, OZ - 1.6, -1.5, 1.5, OZ + 0.5));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("gear_ember_close"));
        steps.add(look(-0.2, 1.0, OZ + 3.0, -1.5, 1.5, OZ + 0.5));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("gear_ember_back"));
        steps.add(look(-5.9, 1.0, OZ + 3.1, -4.5, 1.3, OZ + 0.5));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("gear_pelt_back"));
        // bei Nacht: die Bernsteinaugen des Wolfskopfs und die glühenden Spitzen des Glutwurm-Panzers
        steps.add(Step.command("time set 18000"));
        steps.add(look(-3.0, 0.8, OZ - 2.6, -3.0, 1.6, OZ + 0.5));
        steps.add(Step.waitTicks(40));
        steps.add(Step.screenshot("gear_armor_night"));
        steps.add(Step.command("time set 6000"));
        // die vier Waffen groß als Schaustücke (das 3D-Modell aus der Hand), vor der Hecke
        String[] weapons = {"claw_blade", "ember_fang_blade", "bog_club", "soul_scythe"};
        double[] xs = {-4.5, -1.5, 1.5, 4.5};
        for (int i = 0; i < 4; i++) {
            String id = weapons[i];
            double x = xs[i];
            steps.add(Step.once(minecraft -> minecraft.player.connection.sendCommand(String.format(Locale.ROOT,
                    "summon item_display %.2f %.2f %.2f {item:{id:\"kingdomsatwar:%s\"},item_display:\"head\",brightness:{sky:15,block:15},"
                            + "transformation:{left_rotation:[0f,0f,0f,1f],right_rotation:[0f,0f,0f,1f],translation:[0f,0f,0f],scale:[2.2f,2.2f,2.2f]}}",
                    x, Showcase.ground + 2.2, OZ + 5.4, id))));
        }
        steps.add(Step.command("kill @e[type=mannequin]"));
        steps.add(look(0.0, 0.9, OZ - 1.5, 0.0, 2.3, OZ + 5.4));
        steps.add(Step.waitTicks(40));
        steps.add(Step.screenshot("gear_weapons"));
        steps.add(Step.command("time set 18000"));
        steps.add(Step.command("execute as @e[type=item_display] run data merge entity @s {brightness:{sky:0,block:0}}"));
        steps.add(Step.waitTicks(40));
        steps.add(Step.screenshot("gear_weapons_night"));
        steps.add(Step.command("time set 6000"));
    }

    /** Eine Schaufigur bei x (mit Rüstungssatz {@code set} – oder ohne – und einer Waffe in der Hand), Blick zur Kamera. */
    private static void mannequin(Deque<Step> steps, double x, String set, String weapon) {
        StringBuilder equipment = new StringBuilder("mainhand:{id:\"" + weapon + "\"}");
        if (set != null) {
            String[][] pieces = {{"head", "helmet"}, {"chest", "chestplate"}, {"legs", "leggings"}, {"feet", "boots"}};
            for (String[] p : pieces) {
                equipment.append(",").append(p[0]).append(":{id:\"").append(set).append("_").append(p[1]).append("\"}");
            }
        }
        steps.add(Step.once(minecraft -> minecraft.player.connection.sendCommand(String.format(Locale.ROOT,
                "summon mannequin %.2f %d %.2f {immovable:1b,hide_description:1b,Rotation:[180f,0f],equipment:{%s}}", x,
                Showcase.ground, OZ + 0.5, equipment))));
    }

    private static Step look(double x, double dy, double z, double tx, double tdy, double tz) {
        return Step.once(minecraft -> {
            int g = Showcase.ground;
            double lx = tx - x;
            double ly = tdy - (dy + 1.62);
            double lz = tz - z;
            float yaw = (float) (-Math.atan2(lx, lz) * 180.0 / Math.PI);
            float pitch = (float) (-Math.atan2(ly, Math.sqrt(lx * lx + lz * lz)) * 180.0 / Math.PI);
            minecraft.player.connection.sendCommand(String.format(Locale.ROOT, "tp @s %.2f %.2f %.2f %.1f %.1f", x, g + dy, z, yaw, pitch));
        });
    }
}
