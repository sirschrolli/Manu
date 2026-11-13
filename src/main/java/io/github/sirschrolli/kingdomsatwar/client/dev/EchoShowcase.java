package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.registry.EchoGear;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import java.util.Deque;
import java.util.Locale;
import net.minecraft.client.CameraType;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Rotations;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Vorführung des Hallkristalls und der Rüstung der Lauscher ({@code echo_gear}, seit 1.6) – in einer Höhle der Tiefen
 * Dunkelheit: eine Wand aus Tiefenschiefer mit schimmerndem Hallkristallerz, Sculk am Boden, Sensoren und ein Kreischer, ein
 * Sockel aus Hallkristallblöcken mit Kristallen darauf, Seelenlaternen als einziges Licht; drei Rüstungsständer (schräg, vorn,
 * von hinten); der Spieler in der Rüstung (dritte Person); die Echoortung (hinter einer Wand leuchten Wesen auf); der
 * Spieler schleicht am Tiefenhüter vorbei; Hallkristall in der Hand und das Gepäck.
 */
final class EchoShowcase {
    private static final String STAGE_TAG = "kaw_echo_stage";
    private static final int OZ = 360;
    private static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    private EchoShowcase() {}

    static void gear(Deque<Step> steps) {
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> {
            minecraft.options.renderDistance().set(8);
            minecraft.options.gamma().set(1.0);
            ShowcaseRunner.hideHand = true;
        }));
        for (String command : new String[] {"gamerule spawn_monsters false", "difficulty normal", "gamerule advance_time false", "time set 18000",
                "weather clear", "gamemode spectator"}) {
            steps.add(Step.command(command));
        }
        boolean[] ready = new boolean[1];
        steps.add(Step.server(level -> {
            stage(level);
            ready[0] = true;
        }));
        steps.add(Step.until(() -> ready[0], 6000));
        shot(steps, null, 0.5, 3.4, OZ - 9.5, 0.5, 3.6, OZ + 7.0);
        steps.add(Step.waitTicks(40));
        steps.add(Step.server(EchoShowcase::stageEntities));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("hallkristall_hoehle"));
        shot(steps, "hallkristall_erz_nah", -6.0, 2.7, OZ + 3.2, -6.0, 2.6, OZ + 7.0);
        shot(steps, "hallkristall_sockel", 2.5, 2.1, OZ + 0.6, 2.5, 0.7, OZ + 4.0);
        shot(steps, "lauscher_staender", -4.5, 2.0, OZ - 4.4, -4.5, 1.1, OZ + 0.5);
        shot(steps, "lauscher_nah", -4.5, 1.75, OZ - 2.0, -4.5, 1.3, OZ + 0.5);
        shot(steps, "lauscher_seite", -12.0, 1.8, OZ - 1.2, -3.5, 1.1, OZ + 0.5);

        // der Spieler in der Rüstung (dritte Person)
        steps.add(Step.command("gamemode creative"));
        steps.add(Step.server(level -> {
            ServerPlayer player = level.getServer().getPlayerList().getPlayers().getFirst();
            var set = EchoGear.listenerSet();
            for (int j = 0; j < SLOTS.length; j++) {
                player.setItemSlot(SLOTS[j], new ItemStack(set.get(j).get()));
            }
            player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.NETHERITE_SWORD));
        }));
        tp(steps, -1.5, OZ - 3.5, 165.0f, 6.0f);
        steps.add(Step.once(minecraft -> {
            minecraft.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
            minecraft.options.fov().set(60);
        }));
        steps.add(Step.waitTicks(40));
        steps.add(Step.screenshot("lauscher_spieler_vorn"));
        tp(steps, -1.5, OZ - 3.5, 15.0f, 4.0f);
        steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK)));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("lauscher_spieler_hinten"));

        // die Echoortung: Der Spieler schleicht, ein Ring läuft über den Boden, hinter der Wand leuchten die Wesen auf
        tp(steps, 5.5, OZ - 4.5, -90.0f, 12.0f);
        steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK)));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("echoortung_vorher"));
        steps.add(Step.once(minecraft -> minecraft.options.keyShift.setDown(true)));
        steps.add(Step.waitTicks(8));
        steps.add(Step.screenshot("echoortung"));
        steps.add(Step.waitTicks(16));
        steps.add(Step.screenshot("echoortung_nach"));
        steps.add(Step.once(minecraft -> minecraft.options.keyShift.setDown(false)));
        steps.add(Step.waitTicks(60));

        // leise Schritte: Der Spieler geht am Tiefenhüter vorbei, der ihn nicht hört (die Wollsohlen)
        steps.add(Step.server(level -> {
            for (Entity e : level.getEntitiesOfClass(Entity.class, new AABB(-20, Showcase.ground - 4, OZ - 20, 20, Showcase.ground + 12, OZ + 12))) {
                if (e.entityTags().contains("kaw_echo_warden") && e instanceof Mob warden) {
                    // schräg hinter dem Spieler (aus der Sicht der Kamera rechts), das Gesicht halb zur Kamera
                    warden.snapTo(7.35, Showcase.ground, OZ - 4.35, 150.0f, 0.0f);
                    warden.setYBodyRot(150.0f);
                    warden.setYHeadRot(160.0f);
                }
            }
        }));
        tp(steps, 6.0, OZ - 8.0, 120.0f, 4.0f);
        steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.THIRD_PERSON_FRONT)));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("lauscher_tiefenhueter"));

        // Hallkristall in der Hand (erste Person) und im Gepäck
        steps.add(Step.once(minecraft -> {
            minecraft.options.setCameraType(CameraType.FIRST_PERSON);
            minecraft.options.fov().set(70);
            ShowcaseRunner.hideHand = false;
        }));
        steps.add(Step.command("gamemode survival"));
        steps.add(Step.server(level -> {
            ServerPlayer player = level.getServer().getPlayerList().getPlayers().getFirst();
            player.getAbilities().invulnerable = true;
            player.getAbilities().mayfly = true;
            player.getAbilities().flying = true;
            player.onUpdateAbilities();
            player.getInventory().clearContent();
            var set = EchoGear.listenerSet();
            for (int j = 0; j < SLOTS.length; j++) {
                player.setItemSlot(SLOTS[j], new ItemStack(set.get(j).get()));
            }
            player.getInventory().setItem(0, new ItemStack(EchoGear.ECHO_CRYSTAL.get(), 14));
            player.getInventory().setItem(1, new ItemStack(EchoGear.ECHO_CRYSTAL_ORE.get(), 6));
            player.getInventory().setItem(2, new ItemStack(EchoGear.ECHO_CRYSTAL_BLOCK.get(), 2));
            player.getInventory().setItem(3, new ItemStack(Items.SCULK, 12));
            player.getInventory().setItem(4, new ItemStack(Items.WOOL.pick(net.minecraft.world.item.DyeColor.GRAY), 8));
            player.getInventory().setItem(5, new ItemStack(Items.DIAMOND_PICKAXE));
            for (int j = 0; j < set.size(); j++) {
                player.getInventory().setItem(9 + j, new ItemStack(set.get(j).get()));
            }
            player.getInventory().setSelectedSlot(0);
        }));
        // ein helles (unsichtbares) Licht über dem Spieler, damit der Kristall in der Hand nicht im Schatten liegt
        steps.add(Step.server(level -> level.setBlock(new BlockPos(-6, Showcase.ground + 2, OZ),
                Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, 15), Block.UPDATE_CLIENTS)));
        // so weit von der Wand, dass kein Block im Visier ist (sonst stünde sein Umriss im Bild)
        steps.add(Step.once(minecraft -> minecraft.player.connection.sendCommand(String.format(Locale.ROOT,
                "tp @s %.2f %.2f %.2f 0 8", -6.0, Showcase.ground + 0.6, OZ + 0.9))));
        steps.add(Step.waitTicks(30));
        steps.add(Step.once(minecraft -> ShowcaseRunner.bareHud = true));
        steps.add(Step.hud(true));
        steps.add(Step.once(minecraft -> minecraft.gui.toastManager().clear()));
        steps.add(Step.waitTicks(5));
        steps.add(Step.screenshot("hallkristall_hand"));
        steps.add(Step.once(minecraft -> ShowcaseRunner.bareHud = false));
        steps.add(Step.waitTicks(3));
        steps.add(Step.screenshot("hallkristall_hand_hud"));
        steps.add(Step.screen(minecraft -> new InventoryScreen(minecraft.player)));
        steps.add(Step.once(minecraft -> minecraft.gui.toastManager().clear()));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screenshot("hallkristall_gepaeck"));
        steps.add(Step.once(minecraft -> minecraft.gui.setScreen(null)));
        steps.add(Step.quit());
    }

    // =====================================================================================
    // Die Bühne
    // =====================================================================================

    /**
     * Eine Höhle der Tiefen Dunkelheit: Boden aus Tiefenschiefer und Sculk, hinten eine Wand mit Hallkristallerz, eine
     * niedrige Decke, rechts eine Trennwand (dahinter die Wesen für die Echoortung), Seelenlaternen an Ketten.
     */
    private static void stage(ServerLevel level) {
        int g = Showcase.ground;
        RandomSource random = RandomSource.create(1606);
        BlockState deepslate = Blocks.DEEPSLATE.defaultBlockState();
        BlockState sculk = Blocks.SCULK.defaultBlockState();
        Showcase.fill(level, new BlockPos(-20, g - 5, OZ - 20), new BlockPos(20, g + 13, OZ + 12), deepslate);
        Showcase.fill(level, new BlockPos(-17, g, OZ - 15), new BlockPos(17, g + 9, OZ + 6), Blocks.AIR.defaultBlockState());
        // die Decke nicht ganz flach: hier und da hängt sie tiefer, in der Mitte wölbt sie sich auf
        for (int x = -17; x <= 17; x++) {
            for (int z = OZ - 15; z <= OZ + 6; z++) {
                double arch = 1.0 - Math.abs(x) / 17.0;
                int top = g + 7 + (int) Math.round(arch * 2.2 + random.nextDouble() * 0.6);
                for (int y = top + 1; y <= g + 9; y++) {
                    set(level, x, y, z, deepslate);
                }
                if (random.nextFloat() < 0.06f) {
                    set(level, x, top, z, deepslate);
                }
            }
        }
        // der Boden: Tiefenschiefer, darauf Sculk in Flecken (vorn zur Kamera hin dichter)
        for (int x = -17; x <= 17; x++) {
            for (int z = OZ - 15; z <= OZ + 6; z++) {
                double patch = Math.sin(x * 0.41 + 0.7) * Math.cos(z * 0.37) + random.nextDouble() * 0.9;
                set(level, x, g - 1, z, patch > 0.35 ? sculk : deepslate);
            }
        }
        // die Wand hinten: Hallkristallerz in Büscheln, die Front nicht ganz glatt
        BlockState ore = EchoGear.ECHO_CRYSTAL_ORE.get().defaultBlockState();
        int[][] veins = {{-9, 1}, {-8, 1}, {-9, 2}, {-6, 3}, {-5, 3}, {-5, 4}, {-11, 4}, {-3, 1}, {-2, 5}, {-7, 6},
            {3, 2}, {4, 2}, {4, 3}, {7, 1}, {8, 4}, {9, 4}, {11, 2}, {6, 6}, {-13, 2}, {13, 5}};
        for (int[] v : veins) {
            set(level, v[0], g + v[1], OZ + 7, ore);
        }
        for (int x = -16; x <= 16; x++) {
            for (int y = g; y <= g + 1; y++) {
                if (random.nextFloat() < 0.3f && !level.getBlockState(new BlockPos(x, y, OZ + 7)).is(EchoGear.ECHO_CRYSTAL_ORE.get())
                        && (y == g || level.getBlockState(new BlockPos(x, y - 1, OZ + 6)).is(Blocks.DEEPSLATE))) {
                    set(level, x, y, OZ + 6, deepslate);
                }
            }
        }
        set(level, -9, g - 1, OZ + 6, ore);
        set(level, -10, g - 1, OZ + 6, ore);
        // Sculk-Adern an der Wand und an der Decke, Leuchtflechten
        for (int i = 0; i < 70; i++) {
            int x = -16 + random.nextInt(33);
            int y = g + random.nextInt(8);
            BlockPos at = new BlockPos(x, y, OZ + 6);
            if (level.getBlockState(at).isAir() && level.getBlockState(at.south()).is(Blocks.DEEPSLATE)) {
                level.setBlock(at, (random.nextInt(4) == 0 ? Blocks.GLOW_LICHEN : Blocks.SCULK_VEIN).defaultBlockState()
                        .setValue(MultifaceBlock.getFaceProperty(Direction.SOUTH), true), Block.UPDATE_CLIENTS);
            }
        }
        for (int i = 0; i < 90; i++) {
            int x = -16 + random.nextInt(33);
            int z = OZ - 14 + random.nextInt(20);
            BlockPos at = new BlockPos(x, ceiling(level, x, z) - 1, z);
            if (level.getBlockState(at).isAir() && level.getBlockState(at.above()).is(Blocks.DEEPSLATE)) {
                level.setBlock(at, Blocks.SCULK_VEIN.defaultBlockState().setValue(MultifaceBlock.getFaceProperty(Direction.UP), true),
                        Block.UPDATE_CLIENTS);
            }
        }
        // Sculk-Sensoren, ein Kreischer und ein Katalysator auf dem Boden
        for (int[] s : new int[][] {{-12, -6}, {-14, 3}, {12, 2}, {-2, -9}}) {
            set(level, s[0], g, OZ + s[1], Blocks.SCULK_SENSOR.defaultBlockState());
        }
        set(level, 14, g, OZ - 1, Blocks.SCULK_SHRIEKER.defaultBlockState());
        set(level, -15, g, OZ - 2, Blocks.SCULK_CATALYST.defaultBlockState());
        set(level, 6, g, OZ + 4, Blocks.SCULK_CATALYST.defaultBlockState());
        // der Sockel aus Hallkristallblöcken
        BlockState block = EchoGear.ECHO_CRYSTAL_BLOCK.get().defaultBlockState();
        Showcase.fill(level, new BlockPos(1, g, OZ + 3), new BlockPos(3, g, OZ + 4), block);
        set(level, 2, g + 1, OZ + 4, block);
        // rechts eine niedrige Trennwand aus Tiefenschieferziegeln; dahinter die Wesen für die Echoortung
        BlockState bricks = Blocks.DEEPSLATE_BRICKS.defaultBlockState();
        for (int z = OZ - 12; z <= OZ - 1; z++) {
            for (int y = g; y <= g + 3; y++) {
                set(level, 10, y, z, y == g + 3 && random.nextInt(3) == 0 ? Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState() : bricks);
            }
        }
        // Seelenlaternen an Ketten (das einzige Licht neben den Kristallen)
        for (int[] l : new int[][] {{-4, 0}, {3, -2}, {-11, -4}, {7, -7}, {-1, -11}, {13, -9}}) {
            int x = l[0];
            int z = OZ + l[1];
            int bottom = g + 5;
            for (int cy = ceiling(level, x, z) - 1; cy > bottom; cy--) {
                set(level, x, cy, z, Blocks.IRON_CHAIN.defaultBlockState());
            }
            set(level, x, bottom, z, Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
        }
        // unsichtbares Licht, damit man auf den Bildern etwas erkennt (die Tiefe Dunkelheit ist sonst fast schwarz)
        for (int x = -15; x <= 15; x += 5) {
            for (int z = OZ - 13; z <= OZ + 5; z += 6) {
                BlockPos at = new BlockPos(x, g + 3, z);
                if (level.getBlockState(at).isAir()) {
                    level.setBlock(at, Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, z > OZ ? 10 : 11), Block.UPDATE_CLIENTS);
                }
            }
        }
    }

    /** Die Gegenstände und Wesen (erst, wenn die Kamera da ist, damit nichts in entladenen Abschnitten verschwindet). */
    private static void stageEntities(ServerLevel level) {
        int g = Showcase.ground;
        for (Entity e : level.getEntitiesOfClass(Entity.class, new AABB(-22, g - 6, OZ - 22, 22, g + 14, OZ + 14))) {
            if (!(e instanceof ServerPlayer)) {
                e.discard();
            }
        }
        for (int i = 0; i < 3; i++) {
            ItemEntity gem = new ItemEntity(level, 2.5 + (i - 1) * 0.62, g + 1.05, OZ + 3.35, new ItemStack(EchoGear.ECHO_CRYSTAL.get()));
            gem.setDeltaMovement(0, 0, 0);
            gem.setNeverPickUp();
            gem.setUnlimitedLifetime();
            level.addFreshEntity(gem);
        }
        // drei Rüstungsständer: schräg, vorn (mit Schwert), von hinten
        for (int i = 0; i < 3; i++) {
            float yaw = i == 0 ? 155.0f : i == 1 ? 180.0f : 0.0f;
            ArmorStand stand = stand(level, -7.5 + i * 3.0, g, OZ + 0.5, yaw);
            if (stand != null && i == 1) {
                stand.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.NETHERITE_SWORD));
            }
        }
        // der Tiefenhüter (ruhig) – erst hinten links, später neben dem schleichenden Spieler
        Mob warden = creature(level, ModEntities.WARDEN.get(), 13.5, g, OZ + 3.5, 200.0f);
        if (warden != null) {
            warden.addTag("kaw_echo_warden");
        }
        // hinter der Trennwand: Wesen, die die Echoortung zeigt
        creature(level, ModEntities.ZOMBIE.get(), 12.5, g, OZ - 8.5, 90.0f);
        creature(level, ModEntities.SKELETON.get(), 13.5, g, OZ - 5.5, 70.0f);
        creature(level, ModEntities.CREEPER.get(), 12.0, g, OZ - 3.0, 100.0f);
        creature(level, ModEntities.SPIDER.get(), 15.0, g, OZ - 10.5, 120.0f);
    }

    private static ArmorStand stand(ServerLevel level, double x, double y, double z, float yaw) {
        ArmorStand stand = EntityTypes.ARMOR_STAND.create(level, EntitySpawnReason.COMMAND);
        if (stand == null) {
            return null;
        }
        stand.setShowArms(true);
        stand.setNoBasePlate(true);
        stand.setRightArmPose(new Rotations(-20, 0, 10));
        stand.setLeftArmPose(new Rotations(-10, 0, -8));
        var set = EchoGear.listenerSet();
        for (int j = 0; j < SLOTS.length; j++) {
            stand.setItemSlot(SLOTS[j], new ItemStack(set.get(j).get()));
        }
        stand.addTag(STAGE_TAG);
        stand.snapTo(x, y, z, yaw, 0);
        stand.setYBodyRot(yaw);
        stand.setYHeadRot(yaw);
        level.addFreshEntity(stand);
        return stand;
    }

    private static Mob creature(ServerLevel level, EntityType<? extends Mob> type, double x, double y, double z, float yaw) {
        Mob mob = type.create(level, EntitySpawnReason.COMMAND);
        if (mob == null) {
            return null;
        }
        mob.setNoAi(true);
        mob.setPersistenceRequired();
        mob.setSilent(true);
        mob.addTag(STAGE_TAG);
        mob.snapTo(x, y, z, yaw, 0);
        mob.setYBodyRot(yaw);
        mob.setYHeadRot(yaw);
        level.addFreshEntity(mob);
        return mob;
    }

    // =====================================================================================
    // Hilfen
    // =====================================================================================

    /** Die Höhe des untersten Deckenblocks über (x, z). */
    private static int ceiling(ServerLevel level, int x, int z) {
        int y = Showcase.ground + 5;
        while (y < Showcase.ground + 13 && level.getBlockState(new BlockPos(x, y, z)).isAir()) {
            y++;
        }
        return y;
    }

    private static void set(ServerLevel level, int x, int y, int z, BlockState state) {
        level.setBlock(new BlockPos(x, y, z), state, Block.UPDATE_CLIENTS);
    }

    /** Den Spieler (dritte Person) an (x, z) auf den Boden stellen, mit Blickrichtung. */
    private static void tp(Deque<Step> steps, double x, double z, float yaw, float pitch) {
        steps.add(Step.once(minecraft -> minecraft.player.connection.sendCommand(String.format(Locale.ROOT,
                "tp @s %.2f %d %.2f %.1f %.1f", x, Showcase.ground, z, yaw, pitch))));
    }

    /** Kamera (Auge auf Boden + dy) bei (x, z), Blick auf (tx, Boden + tdy, tz); mit Namen ein Bild. */
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
        if (name != null) {
            steps.add(Step.screenshot(name));
        }
    }
}
