package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import io.github.sirschrolli.kingdomsatwar.registry.ShadowItems;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.CameraType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Rotations;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

/**
 * Vorführung der 3D-Rüstungsteile ({@code ruestungen3d}, seit 0.95): alle Rüstungssätze der Mod auf Rüstungsständern –
 * von vorn, in Gruppen zu dreien und von hinten (Umhang, Schwänze, Rückenstacheln) –, dann der Spieler selbst in drei
 * Rüstungen, von vorn und hinten (dritte Person).
 */
final class ArmorPartsShowcase {
    private static final int OZ = 260;
    private static final double SPACING = 2.6;
    /** So viele Sätze der Mod stehen vorn in der Reihe (danach die von Minecraft). */
    private static final int MOD_SETS = 11;

    private ArmorPartsShowcase() {}

    /** Die Sätze in der Reihenfolge auf der Bühne: Helm, Brust, Beine, Stiefel, Hand. */
    private static List<List<Item>> sets() {
        List<List<Item>> sets = new ArrayList<>();
        sets.add(List.of(ModItems.STEEL_HELMET.get(), ModItems.STEEL_CHESTPLATE.get(), ModItems.STEEL_LEGGINGS.get(),
                ModItems.STEEL_BOOTS.get(), ModItems.STEEL_SWORD.get()));
        sets.add(List.of(ModItems.KONTERIT_HELMET.get(), ModItems.KONTERIT_CHESTPLATE.get(), ModItems.KONTERIT_LEGGINGS.get(),
                ModItems.KONTERIT_BOOTS.get(), ModItems.STEEL_AXE.get()));
        sets.add(List.of(ShadowItems.NIGHTSTEEL_HELMET.get(), ShadowItems.NIGHTSTEEL_CHESTPLATE.get(),
                ShadowItems.NIGHTSTEEL_LEGGINGS.get(), ShadowItems.NIGHTSTEEL_BOOTS.get(), ShadowItems.NIGHTSTEEL_SWORD.get()));
        sets.add(List.of(ShadowItems.ABYSSITE_HELMET.get(), ShadowItems.ABYSSITE_CHESTPLATE.get(), ShadowItems.ABYSSITE_LEGGINGS.get(),
                ShadowItems.ABYSSITE_BOOTS.get(), ShadowItems.ABYSSITE_SWORD.get()));
        for (DragonType type : DragonType.values()) {
            var set = ModItems.DRAGON_SCALE_ARMOR.get(type);
            sets.add(List.of(set.get(0).get(), set.get(1).get(), set.get(2).get(), set.get(3).get(),
                    type == DragonType.FIRE ? ModItems.DRACONIUM_SWORD.get() : type == DragonType.EARTH ? ModItems.DRACONIUM_AXE.get()
                            : Items.AIR));
        }
        sets.add(List.of(ModItems.GEMSTONE_CROWN.get(), ModItems.STEEL_CHESTPLATE.get(), Items.AIR, Items.AIR, Items.AIR));
        // die Rüstungen von Minecraft
        sets.add(List.of(Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS, Items.BOW));
        sets.add(List.of(Items.CHAINMAIL_HELMET, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_BOOTS,
                Items.IRON_SWORD));
        sets.add(List.of(Items.COPPER_HELMET, Items.COPPER_CHESTPLATE, Items.COPPER_LEGGINGS, Items.COPPER_BOOTS, Items.COPPER_SWORD));
        sets.add(List.of(Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS, Items.IRON_AXE));
        sets.add(List.of(Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS, Items.GOLDEN_SWORD));
        sets.add(List.of(Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS, Items.DIAMOND_SWORD));
        sets.add(List.of(Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS,
                Items.NETHERITE_SWORD));
        sets.add(List.of(Items.TURTLE_HELMET, Items.AIR, Items.AIR, Items.AIR, Items.TRIDENT));
        return sets;
    }

    static void armor(Deque<Step> steps) {
        for (String command : new String[] {"difficulty peaceful", "gamerule advance_time false", "time set 6000", "weather clear",
                "gamemode spectator"}) {
            steps.add(Step.command(command));
        }
        List<List<Item>> sets = sets();
        int count = sets.size();
        steps.add(Step.server(level -> {
            int g = Showcase.ground;
            int x1 = (int) Math.ceil(count * SPACING) + 6;
            Showcase.fill(level, new BlockPos(-8, g - 1, OZ - 12), new BlockPos(x1, g - 1, OZ + 16), Blocks.POLISHED_ANDESITE.defaultBlockState());
            Showcase.fill(level, new BlockPos(-8, g - 1, OZ - 1), new BlockPos(x1, g - 1, OZ + 1), Blocks.DARK_OAK_PLANKS.defaultBlockState());
            Showcase.fill(level, new BlockPos(-8, g, OZ - 12), new BlockPos(x1, g + 12, OZ + 16), Blocks.AIR.defaultBlockState());
            EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.MAINHAND};
            for (int i = 0; i < count; i++) {
                ArmorStand stand = EntityTypes.ARMOR_STAND.create(level, EntitySpawnReason.COMMAND);
                if (stand == null) {
                    continue;
                }
                stand.setShowArms(true);
                stand.setNoBasePlate(true);
                stand.setRightArmPose(new Rotations(-20, 0, 10));
                stand.setLeftArmPose(new Rotations(-10, 0, -8));
                for (int j = 0; j < slots.length; j++) {
                    stand.setItemSlot(slots[j], new ItemStack(sets.get(i).get(j)));
                }
                stand.snapTo(i * SPACING + 0.5, g, OZ + 0.5, 0, 0);
                stand.setYBodyRot(0);
                stand.setYHeadRot(0);
                level.addFreshEntity(stand);
            }
        }));
        steps.add(Step.waitTicks(40));
        steps.add(Step.hud(false));
        // in Gruppen zu dreien, schräg von vorn
        for (int k = 0; k * 3 < count; k++) {
            double cx = (k * 3 + 1) * SPACING + 0.5;
            shot(steps, "ruestungen_vorn_" + k, cx + 1.2, 1.9, OZ + 4.0, cx, 1.2, OZ + 0.5);
        }
        // von hinten
        for (int k = 0; k * 3 < count; k++) {
            double cx = (k * 3 + 1) * SPACING + 0.5;
            shot(steps, "ruestungen_hinten_" + k, cx - 1.3, 2.0, OZ - 3.4, cx, 1.1, OZ + 0.5);
        }
        // schräg an der Reihe entlang: erst die Rüstungen der Mod, dann die von Minecraft
        shot(steps, "ruestungen_alle_mod", -2.6, 2.4, OZ + 4.6, (MOD_SETS - 1) * SPACING * 0.55, 1.0, OZ + 0.5);
        double vanillaStart = MOD_SETS * SPACING + 0.5;
        shot(steps, "ruestungen_alle_minecraft", vanillaStart - 3.0, 2.3, OZ + 4.4, vanillaStart + (count - MOD_SETS - 1) * SPACING * 0.55,
                1.0, OZ + 0.5);
        // von der Seite (Schwänze, Helmbusch, Flügel)
        shot(steps, "ruestungen_seite", -4.5, 1.8, OZ - 1.5, 4.0, 1.1, OZ + 0.5);

        // der Spieler selbst (dritte Person), vor einer Wand aus Steinziegeln
        steps.add(Step.command("gamemode creative"));
        steps.add(Step.server(level -> {
            int g = Showcase.ground;
            Showcase.fill(level, new BlockPos(-8, g, OZ - 12), new BlockPos(-8, g + 6, OZ + 16), Blocks.STONE_BRICKS.defaultBlockState());
        }));
        int[] wear = {4, 0, 3};
        String[] names = {"feuer", "stahl", "abyssit"};
        for (int k = 0; k < wear.length; k++) {
            List<Item> set = sets.get(wear[k]);
            steps.add(Step.server(level -> {
                ServerPlayer player = level.getServer().getPlayerList().getPlayers().getFirst();
                EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.MAINHAND};
                for (int j = 0; j < slots.length; j++) {
                    player.setItemSlot(slots[j], new ItemStack(set.get(j)));
                }
            }));
            steps.add(Step.once(minecraft -> minecraft.player.connection.sendCommand(String.format(Locale.ROOT,
                    "tp @s %.2f %d %.2f -90 4", -3.5, Showcase.ground, OZ - 7.5))));
            steps.add(Step.once(minecraft -> {
                minecraft.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
                minecraft.options.fov().set(55);
            }));
            steps.add(Step.waitTicks(30));
            steps.add(Step.screenshot("ruestungen_spieler_" + names[k] + "_vorn"));
            steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK)));
            steps.add(Step.waitTicks(8));
            steps.add(Step.screenshot("ruestungen_spieler_" + names[k] + "_hinten"));
        }
        steps.add(Step.once(minecraft -> {
            minecraft.options.setCameraType(CameraType.FIRST_PERSON);
            minecraft.options.fov().set(70);
        }));
        steps.add(Step.hud(true));
        steps.add(Step.quit());
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
