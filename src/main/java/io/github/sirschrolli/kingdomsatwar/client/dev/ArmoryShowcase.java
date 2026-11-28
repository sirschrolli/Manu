package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.item.armory.OreSeekerItem;
import io.github.sirschrolli.kingdomsatwar.registry.Armory;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.client.CameraType;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Rotations;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Die Waffenkammer ({@code armory}, 0.84): jede Waffe, jede Spitzhacke und jeder Schild aus eigener Sicht und mit F5 von vorn
 * und hinten (die Schilde auch beim Blocken); der Kriegshammer beim Ausholen und im Erdstoß, der Erzspürer vor einem Felsblock
 * voller Erze, der Bergmannshammer in einem Stollen, die Waffenkammer auf Rüstungsständern und im Gepäck.
 */
final class ArmoryShowcase {
    private ArmoryShowcase() {}

    private static Map<String, Supplier<? extends Item>> tools() {
        Map<String, Supplier<? extends Item>> map = new LinkedHashMap<>();
        map.put("hellebarde", Armory.STEEL_HALBERD);
        map.put("kriegshammer", Armory.DRACONIUM_WARHAMMER);
        map.put("dolch", Armory.NIGHTSTEEL_DAGGER);
        map.put("grossschwert", Armory.ABYSSITE_GREATSWORD);
        map.put("doppelaxt", Armory.STEEL_DOUBLE_AXE);
        map.put("bergmannshammer", Armory.MINER_HAMMER);
        map.put("glutspitzhacke", Armory.EMBER_PICKAXE);
        map.put("adernbrecher", Armory.VEIN_BREAKER);
        map.put("erzspuerer", Armory.ORE_SEEKER);
        return map;
    }

    private static Map<String, Supplier<? extends Item>> shields() {
        Map<String, Supplier<? extends Item>> map = new LinkedHashMap<>();
        map.put("rundschild", Armory.STEEL_BUCKLER);
        map.put("drachenschild", Armory.DRAGON_SHIELD);
        map.put("turmschild", Armory.TOWER_SHIELD);
        map.put("spiegelschild", Armory.MIRROR_SHIELD);
        return map;
    }

    private static ServerPlayer player(ServerLevel any) {
        return any.getServer().getPlayerList().getPlayers().getFirst();
    }

    private static BlockPos origin(ServerLevel level) {
        return level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, new BlockPos(0, 0, 0));
    }

    private static void fill(ServerLevel level, BlockPos a, BlockPos b, BlockState state) {
        Showcase.fill(level, a, b, state);
    }

    static void armory(Deque<Step> steps) {
        steps.add(Step.once(minecraft -> ShowcaseRunner.hideHand = false));
        for (String command : new String[] {"gamerule advance_time false", "time set 6000", "weather clear", "gamerule spawn_mobs false",
                "kill @e[type=!minecraft:player]", "clear", "gamemode survival"}) {
            steps.add(Step.command(command));
        }
        steps.add(Step.server(any -> {
            ServerPlayer player = player(any);
            ServerLevel level = any.getServer().overworld();
            player.getAbilities().invulnerable = true;
            player.onUpdateAbilities();
            player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.CAMERA_DISTANCE).setBaseValue(2.3);
            BlockPos top = origin(level);
            // ein ebener Platz aus Steinziegeln, damit nichts im Bild stört
            fill(level, top.offset(-14, -1, -14), top.offset(14, -1, 14), Blocks.STONE_BRICKS.defaultBlockState());
            fill(level, top.offset(-14, 0, -14), top.offset(14, 8, 14), Blocks.AIR.defaultBlockState());
            player.teleportTo(level, top.getX() + 0.5, top.getY(), top.getZ() + 0.5, Set.of(), 0.0f, 4.0f, true);
        }));
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> {
            if (minecraft.gui.hud.isHidden()) {
                minecraft.gui.hud.toggle();
            }
        }));
        steps.add(Step.waitTicks(40));
        steps.add(Step.waitForChunks(400));

        // --- jede Waffe und Spitzhacke in der Hand: eigene Sicht, F5 vorn, F5 hinten ---
        for (Map.Entry<String, Supplier<? extends Item>> tool : tools().entrySet()) {
            steps.add(Step.server(any -> {
                ServerPlayer player = player(any);
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(tool.getValue().get()));
                player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
            }));
            views(steps, "hand_" + tool.getKey());
        }
        // --- die Schilde: in der Nebenhand, dann blockend ---
        for (Map.Entry<String, Supplier<? extends Item>> shield : shields().entrySet()) {
            steps.add(Step.server(any -> {
                ServerPlayer player = player(any);
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Armory.STEEL_HALBERD.get()));
                player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(shield.getValue().get()));
            }));
            views(steps, "schild_" + shield.getKey());
            steps.add(Step.server(any -> player(any).setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY)));
            steps.add(Step.once(minecraft -> minecraft.options.keyUse.setDown(true)));
            steps.add(Step.waitTicks(10));
            views(steps, "schild_" + shield.getKey() + "_block");
            steps.add(Step.once(minecraft -> minecraft.options.keyUse.setDown(false)));
            steps.add(Step.waitTicks(6));
        }

        // --- der Kriegshammer: Ausholen (Glut am Kopf), dann der Erdstoß zwischen Wüstenzombies ---
        steps.add(Step.server(any -> {
            ServerPlayer player = player(any);
            ServerLevel level = player.level();
            player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Armory.DRACONIUM_WARHAMMER.get()));
            BlockPos top = origin(level);
            int[][] ring = {{3, 0}, {-3, 0}, {0, 3}, {2, -2}, {-2, -2}, {2, 2}, {-2, 2}};
            for (int[] r : ring) {
                Mob mob = ModEntities.HUSK.get().create(level, EntitySpawnReason.COMMAND);
                if (mob != null) {
                    mob.snapTo(top.getX() + 0.5 + r[0], top.getY(), top.getZ() + 0.5 + r[1], (float) Math.toDegrees(Math.atan2(r[0], -r[1])), 0.0f);
                    mob.setNoAi(true);
                    level.addFreshEntity(mob);
                }
            }
        }));
        steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.THIRD_PERSON_FRONT)));
        steps.add(Step.once(minecraft -> minecraft.options.keyUse.setDown(true)));
        steps.add(Step.hud(false));
        steps.add(Step.waitTicks(26));
        steps.add(Step.screenshot("hammer_ausholen_vorn"));
        steps.add(Step.hud(true));
        steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.FIRST_PERSON)));
        steps.add(Step.waitTicks(3));
        steps.add(Step.screenshot("hammer_ausholen_ich"));
        steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK)));
        steps.add(Step.waitTicks(3));
        steps.add(Step.screenshot("hammer_ausholen_hinten"));
        steps.add(Step.server(any -> {
            ServerPlayer player = player(any);
            for (Mob mob : player.level().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(6.0))) {
                mob.setNoAi(false);
                mob.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 200, 6, false, false));
            }
        }));
        steps.add(Step.once(minecraft -> minecraft.options.keyUse.setDown(false)));
        steps.add(Step.waitTicks(4));
        steps.add(Step.screenshot("hammer_erdstoss_1"));
        steps.add(Step.waitTicks(5));
        steps.add(Step.screenshot("hammer_erdstoss_2"));
        steps.add(Step.waitTicks(30));
        steps.add(Step.command("kill @e[type=!minecraft:player]"));
        steps.add(Step.waitTicks(20));
        steps.add(Step.command("kill @e[type=!minecraft:player]"));
        steps.add(Step.server(any -> {
            ServerPlayer player = player(any);
            BlockPos top = origin(player.level());
            fill(player.level(), top.offset(-14, 0, -14), top.offset(14, 8, 14), Blocks.AIR.defaultBlockState());
            player.clearFire();
        }));

        // --- der Erzspürer: ein Felsblock voller Erze, die durch den Stein leuchten ---
        steps.add(Step.server(any -> {
            ServerPlayer player = player(any);
            ServerLevel level = player.level();
            BlockPos top = origin(level);
            BlockPos a = top.offset(-6, 0, 4);
            fill(level, a, a.offset(12, 6, 8), Blocks.STONE.defaultBlockState());
            List<Block> ores = List.of(Blocks.DIAMOND_ORE, Blocks.IRON_ORE, Blocks.GOLD_ORE, Blocks.EMERALD_ORE, Blocks.REDSTONE_ORE,
                    Blocks.LAPIS_ORE, Blocks.COPPER_ORE, Blocks.COAL_ORE);
            net.minecraft.util.RandomSource random = net.minecraft.util.RandomSource.create(84L);
            for (int i = 0; i < 46; i++) {
                BlockPos at = a.offset(1 + random.nextInt(11), 1 + random.nextInt(5), 1 + random.nextInt(7));
                Block ore = ores.get(random.nextInt(ores.size()));
                level.setBlock(at, ore.defaultBlockState(), Block.UPDATE_ALL);
                if (random.nextInt(3) == 0) {
                    level.setBlock(at.above(), ore.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Armory.ORE_SEEKER.get()));
            player.teleportTo(level, top.getX() + 0.5, top.getY(), top.getZ() + 0.5, Set.of(), 0.0f, 12.0f, true);
        }));
        steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.FIRST_PERSON)));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screenshot("erzspuerer_vorher"));
        steps.add(Step.server(any -> {
            ServerPlayer player = player(any);
            OreSeekerItem.reveal(player.level(), player, OreSeekerItem.scan(player.level(), player.blockPosition()));
        }));
        steps.add(Step.waitTicks(12));
        steps.add(Step.screenshot("erzspuerer_leuchten"));
        steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK)));
        steps.add(Step.waitTicks(4));
        steps.add(Step.screenshot("erzspuerer_leuchten_hinten"));
        steps.add(Step.waitTicks(180));

        // --- der Bergmannshammer: drei Schläge in eine Felswand – ein Stollen, drei mal drei ---
        steps.add(Step.server(any -> {
            ServerPlayer player = player(any);
            ServerLevel level = player.level();
            BlockPos top = origin(level);
            fill(level, top.offset(-6, 0, 2), top.offset(6, 6, 12), Blocks.STONE.defaultBlockState());
            for (int i = 0; i < 18; i++) {
                fill(level, top.offset(-6 + (i * 5) % 13, (i * 3) % 7, 2 + (i * 7) % 11), top.offset(-6 + (i * 5) % 13, (i * 3) % 7, 2 + (i * 7) % 11),
                        (i % 3 == 0 ? Blocks.ANDESITE : i % 3 == 1 ? Blocks.TUFF : Blocks.COAL_ORE).defaultBlockState());
            }
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Armory.MINER_HAMMER.get()));
            player.teleportTo(level, top.getX() + 0.5, top.getY(), top.getZ() + 0.5, Set.of(), 0.0f, 10.0f, true);
        }));
        steps.add(Step.waitTicks(6));
        for (int depth = 0; depth < 3; depth++) {
            int d = depth;
            steps.add(Step.server(any -> {
                ServerPlayer player = player(any);
                ServerLevel level = player.level();
                BlockPos top = origin(level);
                BlockPos center = top.offset(0, 1, 2 + d);
                player.teleportTo(level, top.getX() + 0.5, top.getY(), top.getZ() + 0.5 + d, Set.of(), 0.0f, 10.0f, true);
                BlockState state = level.getBlockState(center);
                if (!state.isAir()) {
                    player.gameMode.destroyBlock(center);
                }
            }));
            steps.add(Step.waitTicks(6));
        }
        steps.add(Step.command("kill @e[type=minecraft:item]"));
        steps.add(Step.server(any -> {
            ServerPlayer player = player(any);
            ServerLevel level = player.level();
            BlockPos top = origin(level);
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0, false, false));
            player.teleportTo(level, top.getX() + 0.5, top.getY(), top.getZ() - 1.5, Set.of(), 0.0f, 8.0f, true);
        }));
        steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.FIRST_PERSON)));
        steps.add(Step.waitTicks(8));
        steps.add(Step.screenshot("bergmannshammer_stollen"));

        // --- die Waffenkammer auf Rüstungsständern ---
        steps.add(Step.server(any -> {
            ServerPlayer player = player(any);
            ServerLevel level = player.level();
            BlockPos top = origin(level);
            fill(level, top.offset(-14, 0, -14), top.offset(14, 8, 14), Blocks.AIR.defaultBlockState());
            fill(level, top.offset(-7, 0, 6), top.offset(7, 5, 6), Blocks.DARK_OAK_PLANKS.defaultBlockState());
            List<Supplier<? extends Item>> mains = List.of(Armory.STEEL_HALBERD, Armory.DRACONIUM_WARHAMMER, Armory.NIGHTSTEEL_DAGGER,
                    Armory.ABYSSITE_GREATSWORD, Armory.STEEL_DOUBLE_AXE, Armory.MINER_HAMMER, Armory.EMBER_PICKAXE);
            List<Supplier<? extends Item>> offs = List.of(Armory.STEEL_BUCKLER, Armory.DRAGON_SHIELD, Armory.MIRROR_SHIELD, Armory.TOWER_SHIELD,
                    Armory.STEEL_BUCKLER, Armory.VEIN_BREAKER, Armory.ORE_SEEKER);
            for (int i = 0; i < mains.size(); i++) {
                ArmorStand stand = new ArmorStand(level, top.getX() - 5.5 + i * 2.0, top.getY(), top.getZ() + 4.5);
                stand.setYRot(180.0f);
                stand.setYBodyRot(180.0f);
                stand.setYHeadRot(180.0f);
                stand.setShowArms(true);
                stand.setNoBasePlate(true);
                stand.setRightArmPose(new Rotations(-25.0f, 0.0f, 8.0f));
                stand.setLeftArmPose(new Rotations(-30.0f, 0.0f, -8.0f));
                stand.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(mains.get(i).get()));
                stand.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(offs.get(i).get()));
                level.addFreshEntity(stand);
            }
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            player.teleportTo(level, top.getX() + 0.5, top.getY(), top.getZ() - 4.5, Set.of(), 0.0f, 8.0f, true);
        }));
        steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.FIRST_PERSON)));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("staender"));
        steps.add(Step.server(any -> {
            ServerPlayer player = player(any);
            BlockPos top = origin(player.level());
            player.teleportTo(player.level(), top.getX() - 3.0, top.getY() + 0.5, top.getZ() + 1.5, Set.of(), -20.0f, 12.0f, true);
        }));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screenshot("staender_nah"));

        // --- im Gepäck: alles beisammen, dazu die Beschreibung ---
        steps.add(Step.server(any -> {
            ServerPlayer player = player(any);
            player.removeAllEffects();                                  // sonst rückt die Wirkungsliste das Gepäck zur Seite
            player.getInventory().clearContent();
            int slot = 9;
            for (Supplier<? extends Item> item : tools().values()) {
                player.getInventory().setItem(slot++, new ItemStack(item.get()));
            }
            for (Supplier<? extends Item> item : shields().values()) {
                player.getInventory().setItem(slot++, new ItemStack(item.get()));
            }
            player.getInventory().setItem(0, new ItemStack(Armory.STEEL_HALBERD.get()));
            player.getInventory().setItem(40, new ItemStack(Armory.DRAGON_SHIELD.get()));
        }));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screen(minecraft -> new InventoryScreen(minecraft.player)));
        steps.add(Step.once(minecraft -> hover(minecraft, -88 + 8 + 18 * 1 + 8, -83 + 84 + 8)));
        steps.add(Step.waitTicks(8));
        steps.add(Step.screenshot("gepaeck_kriegshammer"));
        steps.add(Step.once(minecraft -> hover(minecraft, -88 + 8 + 18 * 1 + 8, -83 + 84 + 18 + 8)));
        steps.add(Step.waitTicks(8));
        steps.add(Step.screenshot("gepaeck_drachenschild_a"));
        steps.add(Step.once(minecraft -> hover(minecraft, -88 + 8 + 18 * 0 + 8, -83 + 84 + 18 + 8)));
        steps.add(Step.waitTicks(8));
        steps.add(Step.screenshot("gepaeck_b"));
        steps.add(Step.once(minecraft -> hover(minecraft, -88 + 8 + 18 * 2 + 8, -83 + 84 + 8)));
        steps.add(Step.waitTicks(8));
        steps.add(Step.screenshot("gepaeck_drachenschild"));
        steps.add(Step.once(minecraft -> minecraft.gui.setScreen(null)));
        steps.add(Step.quit());
    }

    /** Nur die Schilde aus eigener Sicht, mit dem Holzschild von Minecraft zum Vergleich (Szene {@code armory_shields}). */
    static void shieldsFirstPerson(Deque<Step> steps) {
        steps.add(Step.once(minecraft -> ShowcaseRunner.hideHand = false));
        for (String command : new String[] {"gamerule advance_time false", "time set 6000", "weather clear", "gamerule spawn_mobs false",
                "kill @e[type=!minecraft:player]", "clear", "gamemode survival"}) {
            steps.add(Step.command(command));
        }
        steps.add(Step.server(any -> {
            ServerPlayer player = player(any);
            ServerLevel level = any.getServer().overworld();
            player.getAbilities().invulnerable = true;
            player.onUpdateAbilities();
            BlockPos top = origin(level);
            fill(level, top.offset(-14, -1, -14), top.offset(14, -1, 14), Blocks.STONE_BRICKS.defaultBlockState());
            fill(level, top.offset(-14, 0, -14), top.offset(14, 8, 14), Blocks.AIR.defaultBlockState());
            player.teleportTo(level, top.getX() + 0.5, top.getY(), top.getZ() + 0.5, Set.of(), 0.0f, 4.0f, true);
        }));
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> {
            if (minecraft.gui.hud.isHidden()) {
                minecraft.gui.hud.toggle();
            }
        }));
        steps.add(Step.waitTicks(40));
        steps.add(Step.waitForChunks(400));
        Map<String, Supplier<? extends Item>> all = new LinkedHashMap<>();
        all.put("holzschild", () -> net.minecraft.world.item.Items.SHIELD);
        all.putAll(shields());
        for (Map.Entry<String, Supplier<? extends Item>> shield : all.entrySet()) {
            for (boolean sword : new boolean[] {false, true}) {
                steps.add(Step.server(any -> {
                    ServerPlayer player = player(any);
                    player.setItemInHand(InteractionHand.MAIN_HAND, sword ? new ItemStack(net.minecraft.world.item.Items.DIAMOND_SWORD) : ItemStack.EMPTY);
                    player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(shield.getValue().get()));
                }));
                steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.FIRST_PERSON)));
                steps.add(Step.waitTicks(30));
                String tag = "vergleich_" + shield.getKey() + (sword ? "_schwert" : "");
                steps.add(Step.screenshot(tag + "_1ruhe"));
                steps.add(Step.once(minecraft -> minecraft.options.keyUse.setDown(true)));
                steps.add(Step.waitTicks(12));
                steps.add(Step.screenshot(tag + "_2block"));
                steps.add(Step.once(minecraft -> minecraft.options.keyUse.setDown(false)));
                steps.add(Step.waitTicks(6));
            }
        }
        steps.add(Step.quit());
    }

    /** Eigene Sicht (mit Anzeige), F5 von vorn und von hinten (ohne – sie verdeckte die Füße und den Schild). */
    /** Die 21 Werkzeuge und Waffen, die seit 0.88 in 3D sind (Name im Bild -> Gegenstand). */
    private static Map<String, Supplier<? extends Item>> toolsets() {
        Map<String, Supplier<? extends Item>> map = new LinkedHashMap<>();
        map.put("stahlschwert", io.github.sirschrolli.kingdomsatwar.registry.ModItems.STEEL_SWORD);
        map.put("stahlaxt", io.github.sirschrolli.kingdomsatwar.registry.ModItems.STEEL_AXE);
        map.put("stahlspitzhacke", io.github.sirschrolli.kingdomsatwar.registry.ModItems.STEEL_PICKAXE);
        map.put("stahlschaufel", io.github.sirschrolli.kingdomsatwar.registry.ModItems.STEEL_SHOVEL);
        map.put("stahlhacke", io.github.sirschrolli.kingdomsatwar.registry.ModItems.STEEL_HOE);
        map.put("nachtstahlschwert", io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.NIGHTSTEEL_SWORD);
        map.put("nachtstahlaxt", io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.NIGHTSTEEL_AXE);
        map.put("nachtstahlspitzhacke", io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.NIGHTSTEEL_PICKAXE);
        map.put("nachtstahlschaufel", io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.NIGHTSTEEL_SHOVEL);
        map.put("nachtstahlhacke", io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.NIGHTSTEEL_HOE);
        map.put("abyssitschwert", io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.ABYSSITE_SWORD);
        map.put("abyssitaxt", io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.ABYSSITE_AXE);
        map.put("abyssitspitzhacke", io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.ABYSSITE_PICKAXE);
        map.put("abyssitschaufel", io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.ABYSSITE_SHOVEL);
        map.put("abyssithacke", io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.ABYSSITE_HOE);
        map.put("draconiumschwert", io.github.sirschrolli.kingdomsatwar.registry.ModItems.DRACONIUM_SWORD);
        map.put("draconiumaxt", io.github.sirschrolli.kingdomsatwar.registry.ModItems.DRACONIUM_AXE);
        map.put("mondsilberklinge", io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.MOONSILVER_BLADE);
        map.put("trollkeule", io.github.sirschrolli.kingdomsatwar.registry.ModItems.TROLL_CLUB);
        map.put("kommandostab", io.github.sirschrolli.kingdomsatwar.registry.ModItems.COMMAND_BATON);
        map.put("vermessungsstab", io.github.sirschrolli.kingdomsatwar.registry.ModItems.SURVEYORS_ROD);
        return map;
    }

    /**
     * Werkzeuge und Waffen in 3D ({@code werkzeuge_3d}, 0.88): jedes Stück aus eigener Sicht und mit F5 von vorn und hinten, dann
     * alle auf Rüstungsständern (drei Reihen) und im Gepäck.
     */
    static void toolsets(Deque<Step> steps) {
        steps.add(Step.once(minecraft -> ShowcaseRunner.hideHand = false));
        for (String command : new String[] {"gamerule advance_time false", "time set 6000", "weather clear", "gamerule spawn_mobs false",
                "kill @e[type=!minecraft:player]", "clear", "gamemode survival"}) {
            steps.add(Step.command(command));
        }
        steps.add(Step.server(any -> {
            ServerPlayer player = player(any);
            ServerLevel level = any.getServer().overworld();
            player.getAbilities().invulnerable = true;
            player.onUpdateAbilities();
            player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.CAMERA_DISTANCE).setBaseValue(2.3);
            BlockPos top = origin(level);
            fill(level, top.offset(-14, -1, -14), top.offset(14, -1, 14), Blocks.STONE_BRICKS.defaultBlockState());
            fill(level, top.offset(-14, 0, -14), top.offset(14, 8, 14), Blocks.AIR.defaultBlockState());
            player.teleportTo(level, top.getX() + 0.5, top.getY(), top.getZ() + 0.5, Set.of(), 0.0f, 4.0f, true);
        }));
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> {
            if (minecraft.gui.hud.isHidden()) {
                minecraft.gui.hud.toggle();
            }
        }));
        steps.add(Step.waitTicks(40));
        steps.add(Step.waitForChunks(400));
        for (Map.Entry<String, Supplier<? extends Item>> tool : toolsets().entrySet()) {
            steps.add(Step.server(any -> {
                ServerPlayer player = player(any);
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(tool.getValue().get()));
                player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
            }));
            views(steps, "w3d_" + tool.getKey());
        }
        // alle an einer Wand: drei Reihen zu sieben, als 3D-Modelle (Darstellung „Kopf“: das Modell, wie es gebaut ist)
        List<Supplier<? extends Item>> all = List.copyOf(toolsets().values());
        steps.add(Step.server(any -> {
            ServerPlayer player = player(any);
            ServerLevel level = player.level();
            BlockPos top = origin(level);
            fill(level, top.offset(-14, 0, -14), top.offset(14, 8, 14), Blocks.AIR.defaultBlockState());
            fill(level, top.offset(-9, 0, 7), top.offset(9, 7, 7), Blocks.POLISHED_ANDESITE.defaultBlockState());
            fill(level, top.offset(-9, 7, 7), top.offset(9, 7, 7), Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
            for (int i = 0; i < all.size(); i++) {
                int row = i / 7;
                int col = i % 7;
                // Blick nach Süden: Osten liegt links
                double x = top.getX() + 0.5 - (col - 3) * 2.35;
                double y = top.getY() + 5.75 - row * 2.3;
                String id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(all.get(i).get()).toString();
                level.getServer().getCommands().performPrefixedCommand(level.getServer().createCommandSourceStack().withSuppressedOutput(),
                        String.format(java.util.Locale.ROOT, "summon minecraft:item_display %.2f %.2f %.2f {item:{id:\"%s\",count:1},"
                                + "item_display:\"head\",billboard:\"fixed\",Rotation:[0f,0f],brightness:{sky:15,block:15},"
                                + "transformation:{left_rotation:[0f,0f,0f,1f],right_rotation:[0f,0f,0f,1f],translation:[0f,0f,0f],"
                                + "scale:[1.8f,1.8f,1.8f]}}", x, y, top.getZ() + 6.4, id));
            }
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            player.teleportTo(level, top.getX() + 0.5, top.getY() + 1.2, top.getZ() - 2.2, Set.of(), 0.0f, -5.0f, true);
        }));
        steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.FIRST_PERSON)));
        steps.add(Step.hud(false));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("w3d_wand"));
        steps.add(Step.server(any -> {
            ServerPlayer player = player(any);
            BlockPos top = origin(player.level());
            player.teleportTo(player.level(), top.getX() + 3.0, top.getY() + 1.6, top.getZ() + 2.2, Set.of(), 24.0f, -8.0f, true);
        }));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screenshot("w3d_wand_schraeg"));
        steps.add(Step.hud(true));
        steps.add(Step.command("kill @e[type=minecraft:item_display]"));
        // im Gepäck: alles beisammen
        steps.add(Step.server(any -> {
            ServerPlayer player = player(any);
            player.removeAllEffects();
            player.getInventory().clearContent();
            int slot = 9;
            for (Supplier<? extends Item> item : all) {
                player.getInventory().setItem(slot++, new ItemStack(item.get()));
            }
            player.getInventory().setItem(0, new ItemStack(all.getFirst().get()));
        }));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screen(minecraft -> new InventoryScreen(minecraft.player)));
        steps.add(Step.waitTicks(8));
        steps.add(Step.screenshot("w3d_gepaeck"));
        steps.add(Step.once(minecraft -> minecraft.gui.setScreen(null)));
        steps.add(Step.quit());
    }

    /**
     * Die plastischen Gegenstände ({@code gegenstaende_relief}, 0.88): eine Wand voller Barren, Steine, Schuppen, Runen, Münzen und
     * Speisen (Darstellung wie im Rahmen, leicht gedreht, damit man die Dicke sieht), dazu einige in der Hand und am Boden.
     */
    static void reliefs(Deque<Step> steps) {
        String[] wall = {"steel_ingot", "nightsteel_ingot", "moonsilver_ingot", "abyssite_ingot", "draconium", "konterit", "wyvernit",
                "gemstone", "star_shard", "void_shard", "soul_crystal", "mana_crystal", "heart_crystal", "glitch_shard",
                "fire_dragon_scale", "ice_dragon_scale", "water_dragon_scale", "nature_dragon_scale", "ember_scale", "ember_fang", "werewolf_claw",
                "fire_rune", "water_rune", "fire_seal_shard", "bronze_coin", "silver_coin", "gold_coin", "dragon_horn",
                "meat_pie", "apple_pie", "bread_roll", "cooked_venison", "cabbage", "tomato", "ale"};
        steps.add(Step.once(minecraft -> ShowcaseRunner.hideHand = false));
        for (String command : new String[] {"gamerule advance_time false", "time set 6000", "weather clear", "gamerule spawn_mobs false",
                "kill @e[type=!minecraft:player]", "clear", "gamemode survival"}) {
            steps.add(Step.command(command));
        }
        steps.add(Step.server(any -> {
            ServerPlayer player = player(any);
            ServerLevel level = any.getServer().overworld();
            player.getAbilities().invulnerable = true;
            player.onUpdateAbilities();
            BlockPos top = origin(level);
            fill(level, top.offset(-14, -1, -14), top.offset(14, -1, 14), Blocks.STONE_BRICKS.defaultBlockState());
            fill(level, top.offset(-14, 0, -14), top.offset(14, 8, 14), Blocks.AIR.defaultBlockState());
            fill(level, top.offset(-9, 0, 7), top.offset(9, 6, 7), Blocks.SPRUCE_PLANKS.defaultBlockState());
            fill(level, top.offset(-9, 6, 7), top.offset(9, 6, 7), Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
            for (int i = 0; i < wall.length; i++) {
                int row = i / 7;
                int col = i % 7;
                double x = top.getX() + 0.5 - (col - 3) * 1.55;
                double y = top.getY() + 5.0 - row * 1.1;
                level.getServer().getCommands().performPrefixedCommand(level.getServer().createCommandSourceStack().withSuppressedOutput(),
                        String.format(java.util.Locale.ROOT, "summon minecraft:item_display %.2f %.2f %.2f {item:{id:\"kingdomsatwar:%s\",count:1},"
                                + "item_display:\"fixed\",billboard:\"fixed\",Rotation:[%df,0f],brightness:{sky:15,block:15},"
                                + "transformation:{left_rotation:[0f,0f,0f,1f],right_rotation:[0f,0f,0f,1f],translation:[0f,0f,0f],"
                                + "scale:[1.05f,1.05f,1.05f]}}", x, y, top.getZ() + 6.45, wall[i], 160 + (col - 3) * 6));
            }
            player.teleportTo(level, top.getX() + 0.5, top.getY() + 1.0, top.getZ() + 0.6, Set.of(), 0.0f, -14.0f, true);
        }));
        steps.add(Step.hud(false));
        steps.add(Step.waitTicks(40));
        steps.add(Step.waitForChunks(400));
        steps.add(Step.screenshot("relief_wand"));
        steps.add(Step.server(any -> {
            ServerPlayer player = player(any);
            BlockPos top = origin(player.level());
            player.teleportTo(player.level(), top.getX() + 4.0, top.getY() + 1.2, top.getZ() + 3.0, Set.of(), 35.0f, -6.0f, true);
        }));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screenshot("relief_wand_schraeg"));
        steps.add(Step.command("kill @e[type=minecraft:item_display]"));
        // am Boden liegend
        steps.add(Step.server(any -> {
            ServerPlayer player = player(any);
            ServerLevel level = player.level();
            BlockPos top = origin(level);
            String[] ground = {"steel_ingot", "gemstone", "gold_coin", "meat_pie", "fire_dragon_scale", "star_shard", "tomato", "ember_fang"};
            for (int i = 0; i < ground.length; i++) {
                var item = net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(io.github.sirschrolli.kingdomsatwar.KingdomsAtWar.id(ground[i]));
                var entity = new net.minecraft.world.entity.item.ItemEntity(level, top.getX() + 0.5 + (i % 4 - 1.5) * 0.9,
                        top.getY() + 0.1, top.getZ() + 2.6 + (i / 4) * 0.9, new ItemStack(item));
                entity.setDeltaMovement(0, 0, 0);
                entity.setNeverPickUp();
                entity.setUnlimitedLifetime();
                level.addFreshEntity(entity);
            }
            player.teleportTo(level, top.getX() + 0.5, top.getY(), top.getZ() + 0.3, Set.of(), 0.0f, 48.0f, true);
        }));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("relief_boden"));
        steps.add(Step.command("kill @e[type=minecraft:item]"));
        // in der Hand (eigene Sicht und F5 von vorn)
        steps.add(Step.once(minecraft -> {
            if (minecraft.gui.hud.isHidden()) {
                minecraft.gui.hud.toggle();
            }
        }));
        for (String held : new String[] {"steel_ingot", "gemstone", "meat_pie", "dragon_horn"}) {
            steps.add(Step.server(any -> {
                ServerPlayer player = player(any);
                BlockPos top = origin(player.level());
                player.teleportTo(player.level(), top.getX() + 0.5, top.getY(), top.getZ() + 0.5, Set.of(), 180.0f, 6.0f, true);
                var item = net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(io.github.sirschrolli.kingdomsatwar.KingdomsAtWar.id(held));
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item));
            }));
            views(steps, "relief_" + held);
        }
        steps.add(Step.quit());
    }

    private static void views(Deque<Step> steps, String name) {
        steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.FIRST_PERSON)));
        // nach dem Wechsel hebt sich die Waffe erst, wenn sie wieder ausgeholt hat (langsame Waffen gut eine Sekunde)
        steps.add(Step.waitTicks(36));
        steps.add(Step.screenshot(name + "_1ich"));
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.THIRD_PERSON_FRONT)));
        steps.add(Step.waitTicks(4));
        steps.add(Step.screenshot(name + "_2vorn"));
        steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK)));
        steps.add(Step.waitTicks(4));
        steps.add(Step.screenshot(name + "_3hinten"));
        steps.add(Step.hud(true));
        steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.FIRST_PERSON)));
    }

    /** Mauszeiger auf eine Stelle des Bildschirms (in Oberflächen-Koordinaten, von der Mitte aus gerechnet). */
    private static void hover(net.minecraft.client.Minecraft minecraft, int fromCenterX, int fromCenterY) {
        double scale = minecraft.getWindow().getGuiScale();
        double x = (minecraft.getWindow().getGuiScaledWidth() / 2.0 + fromCenterX) * scale;
        double y = (minecraft.getWindow().getGuiScaledHeight() / 2.0 + fromCenterY) * scale;
        try {
            for (String field : new String[] {"xpos", "ypos"}) {
                java.lang.reflect.Field f = minecraft.mouseHandler.getClass().getDeclaredField(field);
                f.setAccessible(true);
                f.setDouble(minecraft.mouseHandler, field.equals("xpos") ? x : y);
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
