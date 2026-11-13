package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.item.end.VoidwalkerArmor;
import io.github.sirschrolli.kingdomsatwar.registry.PhaseGear;
import java.util.Deque;
import java.util.List;
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
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChorusFlowerBlock;
import net.minecraft.world.level.block.EndRodBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Vorführung des Phasits und der Rüstung der Leerwandler ({@code phase_gear}, seit 1.4) – im End, auf einer schwebenden Insel:
 * eine Klippe aus Endstein mit Phasiterz, ein Sockel aus Phasitblöcken mit Steinen darauf, drei Rüstungsständer in der Rüstung
 * (vorn, schräg, von hinten), der Phasenschritt (ein Ständer springt einem Zombie davon), der Spieler in der Rüstung neben den
 * Leerwandlern (dritte Person), Phasit in der Hand und das Gepäck.
 */
final class PhaseShowcase {
    private static final String END = "minecraft:the_end";
    private static final String STAGE_TAG = "kaw_phase_stage";
    private static final int EX = 2400;
    private static final int EY = 110;
    private static final int EZ = -400;
    private static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    private PhaseShowcase() {}

    static void gear(Deque<Step> steps) {
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> {
            minecraft.options.renderDistance().set(10);
            minecraft.options.gamma().set(1.0);
            ShowcaseRunner.hideHand = true;
        }));
        for (String command : new String[] {"gamerule spawn_monsters false", "difficulty normal", "weather clear", "gamemode spectator"}) {
            steps.add(Step.command(command));
        }
        boolean[] ready = new boolean[1];
        steps.add(Step.server(any -> {
            stage(end(any));
            ready[0] = true;
        }));
        steps.add(Step.until(() -> ready[0], 6000));
        look(steps, EX + 0.5, 3.0, EZ - 6.5, EX + 0.5, 3.4, EZ + 6.5);
        steps.add(Step.waitTicks(60));
        steps.add(Step.waitForChunks(900));
        steps.add(Step.waitTicks(40));
        steps.add(Step.server(any -> stageEntities(end(any))));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("phasit_klippe"));
        shot(steps, "phasit_erz_nah", EX - 6.0, 2.6, EZ + 2.6, EX - 6.0, 2.4, EZ + 6.0);
        shot(steps, "phasit_sockel", EX + 2.5, 2.1, EZ + 0.6, EX + 2.5, 0.7, EZ + 4.0);
        shot(steps, "leerwandler_staender", EX - 4.5, 2.0, EZ - 4.4, EX - 4.5, 1.1, EZ + 0.5);
        shot(steps, "leerwandler_nah", EX - 4.5, 1.75, EZ - 2.0, EX - 4.5, 1.25, EZ + 0.5);
        shot(steps, "leerwandler_seite", EX - 12.0, 1.8, EZ - 1.2, EX - 3.5, 1.1, EZ + 0.5);

        // der Phasenschritt: ein Zombie holt aus, der Ständer ist schon fort – ein Wirbel, wo er stand, einer, wo er ankommt
        look(steps, EX + 8.8, 2.2, EZ - 12.2, EX + 8.8, 1.0, EZ - 5.8);
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("phasenschritt_vorher"));
        steps.add(Step.server(any -> {
            // derselbe Wirbel wie im Spiel, nur mit festem Ziel, damit Start und Ziel im Bild sind
            ServerLevel level = end(any);
            if (tagged(level, "kaw_phase_dummy") instanceof ArmorStand stand) {
                Vec3 from = stand.position();
                stand.teleportTo(EX + 12.0, EY, EZ - 6.5);
                stand.setYRot(110.0f);
                stand.setYBodyRot(110.0f);
                VoidwalkerArmor.phaseEffects(level, stand, from);
            }
        }));
        steps.add(Step.waitTicks(3));
        steps.add(Step.screenshot("phasenschritt"));
        steps.add(Step.waitTicks(40));
        steps.add(Step.server(any -> {
            ServerLevel level = end(any);
            for (String tag : new String[] {"kaw_phase_dummy", "kaw_phase_zombie"}) {
                Entity e = tagged(level, tag);
                if (e != null) {
                    e.discard();
                }
            }
        }));

        // der Spieler in der Rüstung (dritte Person), dann neben den Leerwandlern
        steps.add(Step.command("gamemode creative"));
        steps.add(Step.server(any -> {
            ServerPlayer player = any.getServer().getPlayerList().getPlayers().getFirst();
            var set = PhaseGear.voidwalkerSet();
            for (int j = 0; j < SLOTS.length; j++) {
                player.setItemSlot(SLOTS[j], new ItemStack(set.get(j).get()));
            }
            player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.NETHERITE_SWORD));
        }));
        steps.add(Step.once(minecraft -> minecraft.player.connection.sendCommand(String.format(Locale.ROOT,
                "execute in %s run tp @s %.2f %d %.2f 165 6", END, EX + 4.5, EY, EZ - 1.5))));
        steps.add(Step.once(minecraft -> {
            minecraft.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
            minecraft.options.fov().set(60);
        }));
        steps.add(Step.waitTicks(40));
        steps.add(Step.screenshot("leerwandler_spieler_vorn"));
        steps.add(Step.once(minecraft -> minecraft.player.connection.sendCommand(String.format(Locale.ROOT,
                "execute in %s run tp @s %.2f %d %.2f 30 4", END, EX + 4.5, EY, EZ - 1.5))));
        steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK)));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("leerwandler_spieler_hinten"));
        // einer von ihnen: die beiden Leerwandler stehen ruhig neben dem Spieler
        steps.add(Step.server(any -> {
            List<Enderman> men = end(any).getEntitiesOfClass(Enderman.class, new AABB(EX - 30, EY - 5, EZ - 30, EX + 30, EY + 10, EZ + 30));
            if (men.size() >= 2) {
                place(men.get(0), EX + 8.9, EZ + 1.0, 196.0f);
                place(men.get(1), EX + 5.1, EZ + 1.3, 166.0f);
            }
        }));
        steps.add(Step.once(minecraft -> minecraft.player.connection.sendCommand(String.format(Locale.ROOT,
                "execute in %s run tp @s %.2f %d %.2f 180 -2", END, EX + 7.0, EY, EZ - 0.2))));
        steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.THIRD_PERSON_FRONT)));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("leerwandler_einer_von_ihnen"));

        // Phasit in der Hand (erste Person) und im Gepäck
        steps.add(Step.once(minecraft -> {
            minecraft.options.setCameraType(CameraType.FIRST_PERSON);
            minecraft.options.fov().set(70);
            ShowcaseRunner.hideHand = false;
        }));
        steps.add(Step.command("gamemode survival"));
        steps.add(Step.server(any -> {
            ServerPlayer player = any.getServer().getPlayerList().getPlayers().getFirst();
            player.getAbilities().invulnerable = true;
            player.getAbilities().mayfly = true;
            player.getAbilities().flying = true;
            player.onUpdateAbilities();
            player.getInventory().clearContent();
            var set = PhaseGear.voidwalkerSet();
            for (int j = 0; j < SLOTS.length; j++) {
                player.setItemSlot(SLOTS[j], new ItemStack(set.get(j).get()));
            }
            player.getInventory().setItem(0, new ItemStack(PhaseGear.PHASIT.get(), 12));
            player.getInventory().setItem(1, new ItemStack(PhaseGear.PHASIT_ORE.get(), 5));
            player.getInventory().setItem(2, new ItemStack(PhaseGear.PHASIT_BLOCK.get(), 2));
            player.getInventory().setItem(3, new ItemStack(Items.ENDER_PEARL, 16));
            player.getInventory().setItem(4, new ItemStack(Items.POPPED_CHORUS_FRUIT, 8));
            for (int j = 0; j < set.size(); j++) {
                player.getInventory().setItem(9 + j, new ItemStack(set.get(j).get()));
            }
            player.getInventory().setSelectedSlot(0);
        }));
        steps.add(Step.once(minecraft -> minecraft.player.connection.sendCommand(String.format(Locale.ROOT,
                "execute in %s run tp @s %.2f %.2f %.2f 0 18", END, EX - 6.0, EY + 0.6, EZ + 2.2))));
        steps.add(Step.waitTicks(30));
        steps.add(Step.hud(true));
        steps.add(Step.once(minecraft -> minecraft.gui.toastManager().clear()));
        steps.add(Step.waitTicks(5));
        steps.add(Step.screenshot("phasit_hand"));
        steps.add(Step.screen(minecraft -> new InventoryScreen(minecraft.player)));
        steps.add(Step.once(minecraft -> minecraft.gui.toastManager().clear()));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screenshot("phasit_gepaeck"));
        steps.add(Step.once(minecraft -> minecraft.gui.setScreen(null)));
        steps.add(Step.quit());
    }

    // =====================================================================================
    // Die Bühne
    // =====================================================================================

    /** Eine schwebende Endsteininsel mit einer Klippe voller Phasiterz, Chorus und Endstäben als Licht. */
    private static void stage(ServerLevel level) {
        RandomSource random = RandomSource.create(1404);
        BlockState endStone = Blocks.END_STONE.defaultBlockState();
        Showcase.fill(level, new BlockPos(EX - 26, EY - 14, EZ - 24), new BlockPos(EX + 26, EY + 18, EZ + 26), Blocks.AIR.defaultBlockState());
        // die Insel: ein flacher Teller, nach unten spitz zulaufend
        for (int x = EX - 22; x <= EX + 22; x++) {
            for (int z = EZ - 18; z <= EZ + 22; z++) {
                double dx = (x - EX) / 21.0;
                double dz = (z - EZ - 2) / 18.5;
                double d = Math.sqrt(dx * dx + dz * dz) + random.nextDouble() * 0.05;
                if (d > 1.0) {
                    continue;
                }
                int depth = (int) ((1.0 - d) * 11) + 1;
                for (int dy = 1; dy <= depth; dy++) {
                    set(level, x, EY - dy, z, endStone);
                }
            }
        }
        // die Klippe: unregelmäßig hoch, die Front zur Bühne hin glatt
        for (int x = EX - 15; x <= EX + 15; x++) {
            int top = 6 + (int) Math.round(2.2 * Math.sin(x * 0.55) + 1.4 * Math.sin(x * 1.3 + 1.0)) + (Math.abs(x - EX) > 11 ? -2 : 0);
            for (int z = EZ + 6; z <= EZ + 10; z++) {
                int h = top + (z > EZ + 8 ? 1 : 0);
                for (int y = EY; y < EY + h; y++) {
                    set(level, x, y, z, endStone);
                }
            }
        }
        // Phasiterz: Büschel in der Front der Klippe und eins am Boden
        BlockState ore = PhaseGear.PHASIT_ORE.get().defaultBlockState();
        int[][] veins = {{-9, 1}, {-8, 1}, {-9, 2}, {-6, 3}, {-5, 3}, {-5, 4}, {-11, 4}, {-3, 1}, {-2, 5}, {-7, 6},
            {3, 2}, {4, 2}, {4, 3}, {7, 1}, {8, 4}, {9, 4}, {11, 2}, {6, 6}};
        for (int[] v : veins) {
            set(level, EX + v[0], EY + v[1], EZ + 6, ore);
        }
        // die Front nicht ganz glatt: am Fuß hier und da ein Vorsprung (nie vor einem Büschel), oben ein paar Zacken
        for (int x = EX - 14; x <= EX + 14; x++) {
            for (int y = EY; y <= EY + 1; y++) {
                if (random.nextFloat() < 0.3f && level.getBlockState(new BlockPos(x, y, EZ + 6)).is(Blocks.END_STONE)
                        && !level.getBlockState(new BlockPos(x, EY - 1, EZ + 5)).is(PhaseGear.PHASIT_ORE.get())
                        && (y == EY || level.getBlockState(new BlockPos(x, y - 1, EZ + 5)).is(Blocks.END_STONE))) {
                    set(level, x, y, EZ + 5, endStone);
                }
            }
        }
        set(level, EX - 9, EY - 1, EZ + 5, ore);
        set(level, EX - 10, EY - 1, EZ + 5, ore);
        // der Sockel aus Phasitblöcken
        BlockState block = PhaseGear.PHASIT_BLOCK.get().defaultBlockState();
        Showcase.fill(level, new BlockPos(EX + 1, EY, EZ + 3), new BlockPos(EX + 3, EY, EZ + 4), block);
        set(level, EX + 2, EY + 1, EZ + 4, block);
        // Endstäbe als Licht (das End ist sonst zu dunkel für die Rüstung), ein Purpurbogen und Chorus am Rand
        BlockState rod = Blocks.END_ROD.defaultBlockState().setValue(EndRodBlock.FACING, Direction.UP);
        for (int[] r : new int[][] {{-12, -2}, {14, 2}, {-15, 4}}) {
            set(level, EX + r[0], EY, EZ + r[1], rod);
        }
        for (int x = EX - 14; x <= EX + 14; x += 7) {
            int y = EY;
            while (level.getBlockState(new BlockPos(x, y, EZ + 7)).is(Blocks.END_STONE)) {
                y++;
            }
            set(level, x, y, EZ + 7, rod);
        }
        BlockState purpur = Blocks.PURPUR_PILLAR.defaultBlockState();
        for (int y = EY; y < EY + 5; y++) {
            set(level, EX + 13, y, EZ - 4, purpur);
            set(level, EX + 17, y, EZ - 4, purpur);
        }
        Showcase.fill(level, new BlockPos(EX + 13, EY + 5, EZ - 4), new BlockPos(EX + 17, EY + 5, EZ - 4), Blocks.PURPUR_BLOCK.defaultBlockState());
        set(level, EX + 15, EY + 6, EZ - 4, Blocks.PURPUR_SLAB.defaultBlockState());
        for (int[] c : new int[][] {{-17, 8}, {-15, 13}, {-19, 3}, {17, 5}, {12, 14}, {-6, 15}}) {
            ChorusFlowerBlock.generatePlant(level, new BlockPos(EX + c[0], EY, EZ + c[1]), random, 6);
        }
    }

    /** Die Gegenstände und Wesen auf der Bühne (erst, wenn die Kamera da ist, damit nichts in entladenen Abschnitten verschwindet). */
    private static void stageEntities(ServerLevel level) {
        for (Entity e : level.getEntitiesOfClass(Entity.class, new AABB(EX - 30, EY - 20, EZ - 30, EX + 30, EY + 30, EZ + 30))) {
            if (!(e instanceof ServerPlayer)) {
                e.discard();
            }
        }
        for (int i = 0; i < 3; i++) {
            ItemEntity gem = new ItemEntity(level, EX + 2.5 + (i - 1) * 0.62, EY + 1.05, EZ + 3.35, new ItemStack(PhaseGear.PHASIT.get()));
            gem.setDeltaMovement(0, 0, 0);
            gem.setNeverPickUp();
            gem.setUnlimitedLifetime();
            level.addFreshEntity(gem);
        }
        // drei Rüstungsständer: schräg, vorn (mit Schwert), von hinten
        for (int i = 0; i < 3; i++) {
            float yaw = i == 0 ? 155.0f : i == 1 ? 180.0f : 0.0f;
            ArmorStand stand = stand(level, EX - 7.5 + i * 3.0, EY, EZ + 0.5, yaw);
            if (stand != null && i == 1) {
                stand.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.NETHERITE_SWORD));
            }
        }
        // zwei Leerwandler neben der Klippe, ruhig
        creature(level, EntityTypes.ENDERMAN, EX + 8.5, EY, EZ + 3.0, 120.0f);
        creature(level, EntityTypes.ENDERMAN, EX + 11.5, EY, EZ + 4.2, 200.0f);
        // für den Phasenschritt: ein Ständer und ein Zombie, der nach ihm schlägt
        ArmorStand dummy = stand(level, EX + 6.5, EY, EZ - 5.5, 90.0f);
        if (dummy != null) {
            dummy.addTag("kaw_phase_dummy");
        }
        Mob zombie = creature(level, EntityTypes.ZOMBIE, EX + 4.6, EY, EZ - 5.5, 270.0f);
        if (zombie != null) {
            zombie.addTag("kaw_phase_zombie");
            zombie.setAggressive(true);
            zombie.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
        }
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
        var set = PhaseGear.voidwalkerSet();
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

    private static Mob creature(ServerLevel level, net.minecraft.world.entity.EntityType<? extends Mob> type, double x, double y, double z,
            float yaw) {
        Mob mob = type.create(level, EntitySpawnReason.COMMAND);
        if (mob == null) {
            return null;
        }
        mob.setNoAi(true);
        mob.setPersistenceRequired();
        mob.addTag(STAGE_TAG);
        mob.snapTo(x, y, z, yaw, 0);
        mob.setYBodyRot(yaw);
        mob.setYHeadRot(yaw);
        level.addFreshEntity(mob);
        return mob;
    }

    private static void place(Mob mob, double x, double z, float yaw) {
        mob.snapTo(x, EY, z, yaw, 0);
        mob.setYBodyRot(yaw);
        mob.setYHeadRot(yaw);
    }

    private static Entity tagged(ServerLevel level, String tag) {
        for (Entity e : level.getEntitiesOfClass(Entity.class, new AABB(EX - 30, EY - 20, EZ - 30, EX + 30, EY + 30, EZ + 30))) {
            if (e.entityTags().contains(tag)) {
                return e;
            }
        }
        return null;
    }

    // =====================================================================================
    // Hilfen
    // =====================================================================================

    private static ServerLevel end(ServerLevel any) {
        return any.getServer().getLevel(Level.END);
    }

    private static void set(ServerLevel level, int x, int y, int z, BlockState state) {
        level.setBlock(new BlockPos(x, y, z), state, Block.UPDATE_CLIENTS);
    }

    /** Kamera (Auge auf EY + dy) bei (x, z) im End, Blick auf (tx, EY + tdy, tz). */
    private static void look(Deque<Step> steps, double x, double dy, double z, double tx, double tdy, double tz) {
        steps.add(Step.once(minecraft -> {
            double lx = tx - x;
            double ly = tdy - dy;
            double lz = tz - z;
            float yaw = (float) (-Math.atan2(lx, lz) * 180.0 / Math.PI);
            float pitch = (float) (-Math.atan2(ly, Math.sqrt(lx * lx + lz * lz)) * 180.0 / Math.PI);
            minecraft.player.connection.sendCommand(String.format(Locale.ROOT, "execute in %s run tp @s %.2f %.2f %.2f %.1f %.1f", END, x,
                    EY + dy - 1.62, z, yaw, pitch));
        }));
    }

    private static void shot(Deque<Step> steps, String name, double x, double dy, double z, double tx, double tdy, double tz) {
        look(steps, x, dy, z, tx, tdy, tz);
        steps.add(Step.waitTicks(25));
        steps.add(Step.waitForChunks(300));
        steps.add(Step.screenshot(name));
    }
}
