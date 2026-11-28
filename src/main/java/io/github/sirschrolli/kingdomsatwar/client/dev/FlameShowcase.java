package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.block.nether.LavaCrustBlock;
import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.registry.FlameGear;
import java.util.Deque;
import java.util.Locale;
import net.minecraft.client.CameraType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Rotations;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Vorführung des Feueropals und der Rüstung der Glutwandler ({@code flame_gear}, seit 1.1.6): eine Wand aus Netherrack und
 * Schwarzstein mit Feueropal-Knollen, ein Sockel aus Feueropalblöcken, Steine am Boden; drei Rüstungsständer in der Rüstung
 * (vorn, hinten, von der Seite); der Spieler auf einer Glutkruste mitten im Lavabecken (dritte Person); und der Blick in der
 * Lava mit und ohne den Helm.
 */
final class FlameShowcase {
    private static final int OZ = 420;
    private static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    private FlameShowcase() {}

    static void gear(Deque<Step> steps) {
        for (String command : new String[] {"difficulty peaceful", "gamerule advance_time false", "time set 13500", "weather clear",
                "gamemode spectator"}) {
            steps.add(Step.command(command));
        }
        steps.add(Step.server(level -> {
            int g = Showcase.ground;
            // Boden aus Schwarzstein, dahinter eine Wand aus Netherrack (links) und Schwarzstein (rechts) mit Knollen
            Showcase.fill(level, new BlockPos(-14, g - 4, OZ - 14), new BlockPos(16, g - 1, OZ + 14), Blocks.BLACKSTONE.defaultBlockState());
            Showcase.fill(level, new BlockPos(-14, g, OZ - 14), new BlockPos(16, g + 14, OZ + 14), Blocks.AIR.defaultBlockState());
            Showcase.fill(level, new BlockPos(-14, g, OZ + 6), new BlockPos(16, g + 9, OZ + 8), Blocks.NETHERRACK.defaultBlockState());
            Showcase.fill(level, new BlockPos(1, g, OZ + 6), new BlockPos(16, g + 9, OZ + 8), Blocks.BLACKSTONE.defaultBlockState());
            BlockState ore = FlameGear.FIRE_OPAL_ORE.get().defaultBlockState();
            BlockState blackOre = FlameGear.BLACKSTONE_FIRE_OPAL_ORE.get().defaultBlockState();
            int[][] veins = {{-6, 1}, {-5, 1}, {-6, 2}, {-3, 4}, {-2, 4}, {-2, 5}, {-9, 3}, {-8, 6}, {-4, 7}};
            for (int[] v : veins) {
                level.setBlock(new BlockPos(v[0], g + v[1], OZ + 6), ore, 3);
            }
            for (int[] v : veins) {
                level.setBlock(new BlockPos(7 - v[0], g + v[1], OZ + 6), blackOre, 3);
            }
            // ein Sockel aus Feueropalblöcken mit Steinen darauf
            Showcase.fill(level, new BlockPos(-1, g, OZ + 3), new BlockPos(1, g, OZ + 4), FlameGear.FIRE_OPAL_BLOCK.get().defaultBlockState());
            level.setBlock(new BlockPos(0, g + 1, OZ + 4), FlameGear.FIRE_OPAL_BLOCK.get().defaultBlockState(), 3);
            for (int i = 0; i < 3; i++) {
                ItemEntity gem = new ItemEntity(level, -0.6 + i * 0.6 + 0.5, g + 1.05, OZ + 3.3, new ItemStack(FlameGear.FIRE_OPAL.get()));
                gem.setDeltaMovement(0, 0, 0);
                gem.setNeverPickUp();
                gem.setUnlimitedLifetime();
                level.addFreshEntity(gem);
            }
            // drei Rüstungsständer in der Rüstung der Glutwandler
            for (int i = 0; i < 3; i++) {
                ArmorStand stand = EntityTypes.ARMOR_STAND.create(level, EntitySpawnReason.COMMAND);
                if (stand == null) {
                    continue;
                }
                stand.setShowArms(true);
                stand.setNoBasePlate(true);
                stand.setRightArmPose(new Rotations(-20, 0, 10));
                stand.setLeftArmPose(new Rotations(-10, 0, -8));
                var set = FlameGear.emberwalkerSet();
                for (int j = 0; j < SLOTS.length; j++) {
                    stand.setItemSlot(SLOTS[j], new ItemStack(set.get(j).get()));
                }
                stand.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(i == 1 ? Items.NETHERITE_SWORD : Items.AIR));
                float yaw = i == 0 ? 155.0f : i == 1 ? 180.0f : 0.0f;
                stand.snapTo(-6.5 + i * 3.0, g, OZ + 0.5, yaw, 0);
                stand.setYBodyRot(yaw);
                stand.setYHeadRot(yaw);
                level.addFreshEntity(stand);
            }
            // ein Lavabecken rechts (der Steg aus Glutkruste kommt erst vor seinem Bild – sie schmilzt ja wieder)
            Showcase.fill(level, new BlockPos(6, g - 1, OZ - 8), new BlockPos(14, g - 1, OZ + 2), Blocks.LAVA.defaultBlockState());
            // ein tiefes Becken für den Blick in die Lava, mit Feueropal-Erz am Grund
            Showcase.fill(level, new BlockPos(-12, g - 4, OZ - 12), new BlockPos(-4, g - 1, OZ - 6), Blocks.LAVA.defaultBlockState());
            Showcase.fill(level, new BlockPos(-12, g - 5, OZ - 12), new BlockPos(-4, g - 5, OZ - 6), Blocks.NETHERRACK.defaultBlockState());
            for (int[] v : new int[][] {{-10, -9}, {-6, -8}, {-8, -11}, {-5, -11}, {-11, -7}}) {
                level.setBlock(new BlockPos(v[0], g - 5, OZ + v[1]), ore, 3);
            }
        }));
        steps.add(Step.waitTicks(40));
        steps.add(Step.hud(false));
        shot(steps, "feueropal_wand", 0.5, 3.0, OZ - 2.5, 0.5, 3.5, OZ + 6.5);
        steps.add(Step.command("time set 6000"));
        shot(steps, "feueropal_wand_tag", 0.5, 3.0, OZ - 2.5, 0.5, 3.5, OZ + 6.5);
        shot(steps, "feueropal_sockel", 0.5, 2.1, OZ + 0.6, 0.5, 0.6, OZ + 4.0);
        shot(steps, "glutwandler_staender", -3.5, 2.0, OZ - 4.2, -3.5, 1.1, OZ + 0.5);
        shot(steps, "glutwandler_nah", -3.5, 1.7, OZ - 2.0, -3.5, 1.2, OZ + 0.5);
        shot(steps, "glutwandler_seite", -10.5, 1.8, OZ - 1.0, -3.0, 1.1, OZ + 0.5);
        steps.add(Step.server(FlameShowcase::crustPath));
        shot(steps, "glutkruste", 10.0, 3.5, OZ - 9.5, 10.0, 0.0, OZ - 2.5);

        // der Spieler in der Rüstung auf der Kruste mitten in der Lava (dritte Person)
        steps.add(Step.command("gamemode creative"));
        steps.add(Step.server(level -> {
            ServerPlayer player = level.getServer().getPlayerList().getPlayers().getFirst();
            var set = FlameGear.emberwalkerSet();
            for (int j = 0; j < SLOTS.length; j++) {
                player.setItemSlot(SLOTS[j], new ItemStack(set.get(j).get()));
            }
            player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.NETHERITE_SWORD));
        }));
        steps.add(Step.server(FlameShowcase::crustPath));
        steps.add(Step.once(minecraft -> minecraft.player.connection.sendCommand(String.format(Locale.ROOT,
                "tp @s %.2f %d %.2f 160 8", 10.5, Showcase.ground, OZ - 2.5))));
        steps.add(Step.once(minecraft -> {
            minecraft.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
            minecraft.options.fov().set(60);
        }));
        steps.add(Step.waitTicks(40));
        steps.add(Step.screenshot("glutwandler_spieler_vorn"));
        steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK)));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screenshot("glutwandler_spieler_hinten"));

        // der Blick in der Lava: mit dem Helm weit, ohne nur eine Armlänge
        steps.add(Step.once(minecraft -> {
            minecraft.options.setCameraType(CameraType.FIRST_PERSON);
            minecraft.options.fov().set(70);
        }));
        steps.add(Step.once(minecraft -> minecraft.player.connection.sendCommand(String.format(Locale.ROOT,
                "tp @s %.2f %d %.2f -60 35", -11.5, Showcase.ground - 3, OZ - 11.5))));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("glutblick_mit_helm"));
        steps.add(Step.server(level -> level.getServer().getPlayerList().getPlayers().getFirst().setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY)));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screenshot("glutblick_ohne_helm"));
        steps.add(Step.hud(true));
        steps.add(Step.quit());
    }

    /** Ein Steg aus Glutkruste im Lavabecken, in allen vier Stufen (zur Mitte jung, nach außen aufgebrochen). */
    private static void crustPath(net.minecraft.server.level.ServerLevel level) {
        int g = Showcase.ground;
        for (int x = 7; x <= 13; x++) {
            int age = Math.min(LavaCrustBlock.MAX_AGE, Math.abs(x - 10));
            level.setBlock(new BlockPos(x, g - 1, OZ - 3), FlameGear.LAVA_CRUST.get().defaultBlockState().setValue(LavaCrustBlock.AGE, age), 3);
            level.setBlock(new BlockPos(x, g - 1, OZ - 2), FlameGear.LAVA_CRUST.get().defaultBlockState().setValue(LavaCrustBlock.AGE,
                    Math.min(3, age + 1)), 3);
        }
    }

    private static void shot(Deque<Step> steps, String name, double x, double dy, double z, double tx, double tdy, double tz) {
        steps.add(Step.once(minecraft -> {
            double lx = tx - x;
            double ly = tdy - dy;
            double lz = tz - z;
            float yaw = (float) (-Math.atan2(lx, lz) * 180.0 / Math.PI);
            float pitch = (float) (-Math.atan2(ly, Math.sqrt(lx * lx + lz * lz)) * 180.0 / Math.PI);
            minecraft.player.connection.sendCommand(String.format(Locale.ROOT, "tp @s %.2f %.2f %.2f %.1f %.1f", x,
                    Showcase.ground + dy - 1.62, z, yaw, pitch));
        }));
        steps.add(Step.waitTicks(25));
        steps.add(Step.waitForChunks(300));
        steps.add(Step.screenshot(name));
    }
}
