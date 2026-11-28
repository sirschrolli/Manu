package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.registry.ModEffects;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.RelicItems;
import io.github.sirschrolli.kingdomsatwar.relic.PowerApple;
import io.github.sirschrolli.kingdomsatwar.relic.TotemKind;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Set;
import net.minecraft.client.CameraType;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Totems und Äpfel der Macht ({@code relics}): im Inventar mit ihrer Beschreibung, plastisch in der Hand (eigene Sicht und
 * von vorn), im Rahmen und am Boden – und wie ein Totem den Tod aufhält (Glut, Schöpfer) und das Zweite Leben greift.
 */
final class RelicShowcase {
    private RelicShowcase() {}

    private static ServerPlayer player(ServerLevel any) {
        return any.getServer().getPlayerList().getPlayers().getFirst();
    }

    private static List<Item> all() {
        List<Item> items = new ArrayList<>();
        for (TotemKind kind : TotemKind.values()) {
            items.add(RelicItems.totem(kind));
        }
        for (PowerApple apple : PowerApple.values()) {
            items.add(RelicItems.apple(apple));
        }
        return items;
    }

    /** Mauszeiger über ein Feld des Inventars (Koordinaten im Fenster, 176 x 166). */
    private static void hover(net.minecraft.client.Minecraft minecraft, int guiX, int guiY) {
        int left = (minecraft.getWindow().getGuiScaledWidth() - 176) / 2;
        int top = (minecraft.getWindow().getGuiScaledHeight() - 166) / 2;
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

    static void relics(Deque<Step> steps) {
        steps.add(Step.once(minecraft -> ShowcaseRunner.hideHand = false));
        for (String command : new String[] {"gamerule advance_time false", "time set 6000", "weather clear", "gamerule spawn_mobs false",
                "kill @e[type=!minecraft:player]", "clear", "gamemode survival"}) {
            steps.add(Step.command(command));
        }
        int[] ground = new int[1];
        steps.add(Step.server(any -> {
            ServerPlayer player = player(any);
            ServerLevel level = any.getServer().overworld();
            player.getAbilities().invulnerable = true;
            player.onUpdateAbilities();
            player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.CAMERA_DISTANCE).setBaseValue(2.3);
            BlockPos top = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, new BlockPos(0, 0, 0));
            ground[0] = top.getY();
            // eine Bühne: ebener Boden, dahinter eine Wand für die Rahmen
            for (int x = -6; x <= 6; x++) {
                for (int z = -3; z <= 7; z++) {
                    level.setBlockAndUpdate(new BlockPos(x, top.getY() - 1, z), Blocks.SMOOTH_STONE.defaultBlockState());
                    for (int y = 0; y <= 5; y++) {
                        level.setBlockAndUpdate(new BlockPos(x, top.getY() + y, z), Blocks.AIR.defaultBlockState());
                    }
                }
                for (int y = 0; y <= 3; y++) {
                    level.setBlockAndUpdate(new BlockPos(x, top.getY() + y, 6), Blocks.DARK_OAK_PLANKS.defaultBlockState());
                }
            }
            player.teleportTo(level, 0.5, top.getY(), 0.5, Set.of(), 0.0f, 4.0f, true);
        }));
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> {
            if (minecraft.gui.hud.isHidden()) {
                minecraft.gui.hud.toggle();
            }
        }));
        steps.add(Step.waitTicks(40));
        steps.add(Step.waitForChunks(400));

        // --- im Inventar: alle dreizehn, die Beschreibung von sechs ---
        if (!Boolean.getBoolean("kingdomsatwar.relicCompareOnly")) {
            inventory(steps);
        }
        hands(steps, ground);
    }

    private static void inventory(Deque<Step> steps) {
        steps.add(Step.server(any -> {
            ServerPlayer player = player(any);
            List<Item> items = all();
            for (int i = 0; i < 9; i++) {
                player.getInventory().setItem(i, new ItemStack(items.get(i)));
            }
            for (int i = 9; i < items.size(); i++) {
                player.getInventory().setItem(27 + (i - 9), new ItemStack(items.get(i)));
            }
        }));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screen(minecraft -> new InventoryScreen(minecraft.player)));
        steps.add(Step.waitTicks(6));
        // Schnellleiste: Feld i bei (8 + 18 i, 142); die unterste Reihe des Gepäcks bei (8 + 18 i, 120)
        Object[][] hovers = {{0, 142, "glut"}, {4, 142, "hain"}, {7, 142, "leere"}, {8, 142, "schoepfer"}, {0, 120, "drachenapfel"},
                {3, 120, "apfel_unsterblichkeit"}};
        for (Object[] h : hovers) {
            steps.add(Step.once(minecraft -> hover(minecraft, 16 + 18 * (int) h[0], (int) h[1] + 8)));
            steps.add(Step.waitTicks(4));
            steps.add(Step.screenshot("relikt_inventar_" + h[2]));
        }
        steps.add(Step.once(minecraft -> minecraft.gui.setScreen(null)));
        steps.add(Step.command("clear"));
        steps.add(Step.waitTicks(6));
    }

    /** Drei Reihen Rüstungsständer mit den neun Totems: von vorn, von der Seite, von hinten – so zeigt sich die Haltung. */
    static void pose(Deque<Step> steps) {
        int[] ground = new int[1];
        for (String command : new String[] {"gamerule advance_time false", "time set 6000", "weather clear", "gamerule spawn_mobs false",
                "kill @e[type=!minecraft:player]", "clear", "gamemode creative"}) {
            steps.add(Step.command(command));
        }
        steps.add(Step.server(any -> {
            ServerPlayer player = player(any);
            ServerLevel level = any.getServer().overworld();
            BlockPos top = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, new BlockPos(0, 0, 0));
            int y = top.getY();
            for (int x = -8; x <= 8; x++) {
                for (int z = -3; z <= 32; z++) {
                    level.setBlockAndUpdate(new BlockPos(x, y - 1, z), Blocks.SMOOTH_STONE.defaultBlockState());
                    for (int dy = 0; dy <= 5; dy++) {
                        level.setBlockAndUpdate(new BlockPos(x, y + dy, z), Blocks.AIR.defaultBlockState());
                    }
                }
            }
            ground[0] = y;
            List<Item> items = all();
            float[] yaws = {180.0f, 90.0f, 0.0f};
            for (int row = 0; row < 3; row++) {
                for (int i = 0; i < 9; i++) {
                    net.minecraft.world.entity.decoration.ArmorStand stand = new net.minecraft.world.entity.decoration.ArmorStand(level,
                            -6.0 + i * 1.5 + 0.5, y, 3.5 + row * 12.0);
                    stand.setYRot(yaws[row]);
                    stand.yBodyRot = yaws[row];
                    stand.setShowArms(true);
                    stand.setNoBasePlate(true);
                    stand.setNoGravity(true);
                    stand.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, new ItemStack(items.get(i)));
                    stand.setItemSlot(net.minecraft.world.entity.EquipmentSlot.OFFHAND, new ItemStack(items.get(9 + i % 4)));
                    level.addFreshEntity(stand);
                }
            }
            player.getAbilities().flying = true;
            player.onUpdateAbilities();
            player.teleportTo(level, 0.5, y + 2.6, -1.5, Set.of(), 0.0f, 22.0f, true);
        }));
        steps.add(Step.hud(false));
        steps.add(Step.waitTicks(40));
        steps.add(Step.waitForChunks(400));
        steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.FIRST_PERSON)));
        String[] views = {"vorn", "seite", "hinten"};
        for (int row = 0; row < 3; row++) {
            for (int group = 0; group < 3; group++) {
                int r = row;
                int g = group;
                steps.add(Step.server(any -> {
                    ServerPlayer player = player(any);
                    player.teleportTo(player.level(), -6.0 + (g * 3 + 1) * 1.5 + 0.5, ground[0] + 0.1, 3.5 + r * 12.0 - 2.9, Set.of(), 0.0f, 12.0f, true);
                }));
                steps.add(Step.waitTicks(8));
                steps.add(Step.screenshot("relikt_haltung_" + views[row] + "_" + (group + 1)));
            }
        }
    }

    private static void hands(Deque<Step> steps, int[] ground) {
        // --- zum Vergleich: das Totem von Minecraft in der Haupthand, das Totem der Glut in der Zweithand ---
        steps.add(Step.server(any -> {
            ServerPlayer player = player(any);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(net.minecraft.world.item.Items.TOTEM_OF_UNDYING));
            player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(RelicItems.totem(TotemKind.EMBER)));
        }));
        steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.FIRST_PERSON)));
        steps.add(Step.waitTicks(26));
        steps.add(Step.screenshot("relikt_vergleich_1ich"));
        steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.THIRD_PERSON_FRONT)));
        steps.add(Step.waitTicks(4));
        steps.add(Step.screenshot("relikt_vergleich_2vorn"));
        steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK)));
        steps.add(Step.waitTicks(4));
        steps.add(Step.screenshot("relikt_vergleich_3hinten"));
        if (Boolean.getBoolean("kingdomsatwar.relicCompareOnly")) {
            return;
        }

        // --- in der Hand: je zwei zugleich (Haupt- und Zweithand), aus eigener Sicht und von vorn ---
        List<Item> items = all();
        int[][] pairs = {{0, 1}, {2, 3}, {4, 5}, {6, 7}, {8, 12}, {9, 10}, {11, 12}};
        String[] names = {"glut_frost", "sturm_fels", "hain_gezeiten", "schatten_leere", "schoepfer_unsterblich", "drache_stern", "schatten_unsterblich"};
        for (int i = 0; i < pairs.length; i++) {
            int[] pair = pairs[i];
            String name = names[i];
            steps.add(Step.server(any -> {
                ServerPlayer player = player(any);
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(items.get(pair[0])));
                player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(items.get(pair[1])));
            }));
            steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.FIRST_PERSON)));
            steps.add(Step.waitTicks(26));
            steps.add(Step.screenshot("relikt_hand_" + name + "_1ich"));
            steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.THIRD_PERSON_FRONT)));
            steps.add(Step.waitTicks(4));
            steps.add(Step.screenshot("relikt_hand_" + name + "_2vorn"));
        }
        steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.FIRST_PERSON)));
        steps.add(Step.server(any -> {
            ServerPlayer player = player(any);
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        }));

        // --- im Rahmen und am Boden: auch dort plastisch ---
        steps.add(Step.server(any -> {
            ServerPlayer player = player(any);
            ServerLevel level = player.level();
            int y = ground[0];
            for (int i = 0; i < items.size(); i++) {
                int column = i % 7;
                int row = i / 7;
                ItemFrame frame = new ItemFrame(level, new BlockPos(-3 + column, y + 2 - row, 5), Direction.NORTH);
                frame.setItem(new ItemStack(items.get(i)), false);
                level.addFreshEntity(frame);
                ItemEntity drop = new ItemEntity(level, -3.0 + i * 0.5 + 0.5, y + 0.2, 3.2, new ItemStack(items.get(i)));
                drop.setDeltaMovement(0.0, 0.0, 0.0);
                drop.setNeverPickUp();
                drop.setUnlimitedLifetime();
                level.addFreshEntity(drop);
            }
            player.teleportTo(level, 0.5, y, 1.2, Set.of(), 0.0f, 2.0f, true);
        }));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("relikt_rahmen"));
        steps.add(Step.server(any -> {
            ServerPlayer player = player(any);
            player.teleportTo(player.level(), 0.5, ground[0], 0.9, Set.of(), 0.0f, 38.0f, true);
        }));
        steps.add(Step.waitTicks(12));
        steps.add(Step.screenshot("relikt_boden"));
        steps.add(Step.command("kill @e[type=!minecraft:player]"));

        // --- das Totem der Glut hält den Tod auf: Feuernova ---
        revive(steps, ground, TotemKind.EMBER, CameraType.FIRST_PERSON, "relikt_tod_glut_ich", 9);
        revive(steps, ground, TotemKind.EMBER, CameraType.THIRD_PERSON_BACK, "relikt_tod_glut_hinten", 5);
        revive(steps, ground, TotemKind.FROST, CameraType.THIRD_PERSON_BACK, "relikt_tod_frost_hinten", 5);
        revive(steps, ground, TotemKind.STORM, CameraType.THIRD_PERSON_BACK, "relikt_tod_sturm_hinten", 4);
        revive(steps, ground, TotemKind.CREATOR, CameraType.FIRST_PERSON, "relikt_tod_schoepfer_ich", 9);
        revive(steps, ground, TotemKind.CREATOR, CameraType.THIRD_PERSON_BACK, "relikt_tod_schoepfer_hinten", 5);

        // --- das Zweite Leben ---
        steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.FIRST_PERSON)));
        steps.add(Step.server(any -> {
            ServerPlayer player = player(any);
            player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(RelicItems.apple(PowerApple.IMMORTAL)));
            for (MobEffectInstance effect : PowerApple.IMMORTAL.effects()) {
                player.addEffect(new MobEffectInstance(effect));
            }
        }));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("relikt_apfel_wirkung"));
        steps.add(Step.server(any -> {
            ServerPlayer player = player(any);
            player.getAbilities().invulnerable = false;
            player.damageCooldownTime = 0;
            player.hurtServer(player.level(), player.level().damageSources().generic(), 10_000.0f);
            player.getAbilities().invulnerable = true;
            helperCheck(player.isAlive() && !player.hasEffect(ModEffects.SECOND_LIFE), "Zweites Leben");
        }));
        steps.add(Step.waitTicks(11));
        steps.add(Step.screenshot("relikt_zweites_leben"));
        steps.add(Step.waitTicks(40));
        steps.add(Step.command("kill @e[type=!minecraft:player]"));
        steps.add(Step.command("effect clear @a"));
    }

    private static void helperCheck(boolean ok, String what) {
        io.github.sirschrolli.kingdomsatwar.KingdomsAtWar.LOGGER.info("[Showcase] {}: {}", what, ok ? "in Ordnung" : "FEHLER");
    }

    /** Der Spieler hält das Totem in der Zweithand, Feinde ringsum – und ein tödlicher Schlag. */
    private static void revive(Deque<Step> steps, int[] ground, TotemKind kind, CameraType camera, String shot, int after) {
        steps.add(Step.command("kill @e[type=!minecraft:player]"));
        steps.add(Step.once(minecraft -> minecraft.options.setCameraType(camera)));
        steps.add(Step.server(any -> {
            ServerPlayer player = player(any);
            ServerLevel level = player.level();
            player.teleportTo(level, 0.5, ground[0], 0.5, Set.of(), 0.0f, camera == CameraType.FIRST_PERSON ? 6.0f : 14.0f, true);
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(RelicItems.totem(kind)));
            player.removeAllEffects();
            double[][] spots = {{0.5, 3.6}, {-2.2, 2.8}, {3.2, 2.8}, {-3.4, 0.6}, {4.4, 0.6}};
            for (double[] spot : spots) {
                Mob zombie = ModEntities.ZOMBIE.get().create(level, EntitySpawnReason.TRIGGERED);
                if (zombie != null) {
                    zombie.snapTo(spot[0], ground[0], spot[1], 180.0f, 0.0f);
                    zombie.setNoAi(true);
                    zombie.setPersistenceRequired();
                    zombie.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD, new ItemStack(net.minecraft.world.item.Items.LEATHER_HELMET));
                    level.addFreshEntity(zombie);
                }
            }
        }));
        steps.add(Step.waitTicks(16));
        steps.add(Step.server(any -> {
            ServerPlayer player = player(any);
            player.getAbilities().invulnerable = false;
            player.damageCooldownTime = 0;
            player.hurtServer(player.level(), player.level().damageSources().generic(), 10_000.0f);
            player.getAbilities().invulnerable = true;
            helperCheck(player.isAlive(), "Totem " + kind.id());
        }));
        steps.add(Step.waitTicks(after));
        steps.add(Step.screenshot(shot));
        steps.add(Step.waitTicks(44));
    }
}
