package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Wie Gegenstände gehalten werden ({@code halten}, 1.1.3): die Speere (mit dem Eisenspeer von Minecraft zum Vergleich) aus der
 * eigenen Sicht ruhig, beim Ansturm und beim Stoß, mit F5 von hinten und von vorn; dann jeder Gegenstand mit eigener Haltung
 * aus der eigenen Sicht und mit F5; zuletzt Puppen in einer Reihe – so sehen andere Spieler die Speere von der Seite.
 * Mit {@code -Dkingdomsatwar.hold=…} (Namen mit Komma) nur diese Gegenstände.
 */
final class HoldShowcase {
    private HoldShowcase() {}

    private static ServerPlayer player(ServerLevel any) {
        return any.getServer().getPlayerList().getPlayers().getFirst();
    }

    private static BlockPos origin(ServerLevel level) {
        return level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, new BlockPos(0, 0, 0));
    }

    static final List<String> BOWS = List.of("minecraft:bow", "kingdomsatwar:star_bow");

    static final List<String> SPEARS = List.of("minecraft:iron_spear", "kingdomsatwar:glut_spear", "kingdomsatwar:tide_spear",
            "kingdomsatwar:world_spear", "kingdomsatwar:star_lance");

    private static Set<String> only() {
        String filter = System.getProperty("kingdomsatwar.hold", "");
        return filter.isBlank() ? Set.of() : Set.of(filter.split(","));
    }

    private static boolean wanted(String id) {
        Set<String> only = only();
        return only.isEmpty() || only.contains(id) || only.contains(id.substring(id.indexOf(':') + 1));
    }

    private static String name(String id) {
        return id.substring(id.indexOf(':') + 1).toLowerCase(Locale.ROOT);
    }

    private static ItemStack stack(String id) {
        return new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(id)));
    }

    static void hold(Deque<Step> steps) {
        steps.add(Step.once(minecraft -> ShowcaseRunner.hideHand = false));
        for (String command : new String[] {"gamerule advance_time false", "time set 6000", "weather clear", "gamerule spawn_mobs false",
                "kill @e[type=!minecraft:player]", "clear", "effect clear @s", "gamemode survival"}) {
            steps.add(Step.command(command));
        }
        steps.add(Step.server(any -> {
            ServerPlayer player = player(any);
            ServerLevel level = any.getServer().overworld();
            player.getAbilities().invulnerable = true;
            player.onUpdateAbilities();
            player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.CAMERA_DISTANCE).setBaseValue(2.6);
            BlockPos top = origin(level);
            Showcase.fill(level, top.offset(-16, -1, -16), top.offset(16, -1, 16), Blocks.SMOOTH_STONE.defaultBlockState());
            Showcase.fill(level, top.offset(-16, 0, -16), top.offset(16, 10, 16), Blocks.AIR.defaultBlockState());
            // eine helle Wand im Norden als ruhiger Hintergrund
            Showcase.fill(level, top.offset(-16, 0, -12), top.offset(16, 8, -12), Blocks.CONCRETE.pick(net.minecraft.world.item.DyeColor.WHITE).defaultBlockState());
            player.teleportTo(level, top.getX() + 0.5, top.getY(), top.getZ() + 0.5, Set.of(), 180.0f, 8.0f, true);
        }));
        steps.add(Step.hud(true));
        steps.add(Step.waitTicks(40));
        steps.add(Step.waitForChunks(400));

        // --- die Speere: ruhig, Ansturm (Rechtsklick halten), Stoß (Linksklick); eigene Sicht und F5 ---
        for (String spear : SPEARS) {
            if (!wanted(spear)) {
                continue;
            }
            String n = "speer_" + name(spear);
            steps.add(Step.server(any -> {
                ServerPlayer player = player(any);
                player.setItemInHand(InteractionHand.MAIN_HAND, stack(spear));
                player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
            }));
            steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.FIRST_PERSON)));
            steps.add(Step.waitTicks(40));
            steps.add(Step.screenshot(n + "_1ich"));
            steps.add(Step.once(minecraft -> minecraft.options.keyUse.setDown(true)));
            steps.add(Step.waitTicks(14));
            steps.add(Step.screenshot(n + "_2ich_ansturm"));
            steps.add(Step.waitTicks(30));
            steps.add(Step.screenshot(n + "_3ich_ansturm_lang"));
            steps.add(Step.hud(false));
            steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK)));
            steps.add(Step.waitTicks(2));
            steps.add(Step.screenshot(n + "_4hinten_ansturm"));
            steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.THIRD_PERSON_FRONT)));
            steps.add(Step.waitTicks(2));
            steps.add(Step.screenshot(n + "_5vorn_ansturm"));
            steps.add(Step.once(minecraft -> minecraft.options.keyUse.setDown(false)));
            steps.add(Step.waitTicks(30));
            steps.add(Step.screenshot(n + "_6vorn"));
            steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK)));
            steps.add(Step.waitTicks(4));
            steps.add(Step.screenshot(n + "_7hinten"));
            steps.add(Step.hud(true));
            steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.FIRST_PERSON)));
            steps.add(Step.waitTicks(30));
            steps.add(Step.once(minecraft -> KeyMapping.click(minecraft.options.keyAttack.getKey())));
            steps.add(Step.waitTicks(2));
            steps.add(Step.screenshot(n + "_8ich_stoss"));
            steps.add(Step.waitTicks(20));
        }

        // --- die Bögen (mit dem Bogen von Minecraft zum Vergleich): ruhig, beim Spannen in drei Stufen, gespannt mit F5 ---
        for (String bow : BOWS) {
            if (!wanted(bow)) {
                continue;
            }
            String n = "bogen_" + name(bow);
            steps.add(Step.server(any -> {
                ServerPlayer player = player(any);
                player.setItemInHand(InteractionHand.MAIN_HAND, stack(bow));
                player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
                player.getInventory().add(new ItemStack(net.minecraft.world.item.Items.ARROW, 64));
            }));
            steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.FIRST_PERSON)));
            steps.add(Step.waitTicks(36));
            steps.add(Step.screenshot(n + "_1ich"));
            steps.add(Step.once(minecraft -> minecraft.options.keyUse.setDown(true)));
            steps.add(Step.waitTicks(5));
            steps.add(Step.screenshot(n + "_2ich_spannen"));
            steps.add(Step.waitTicks(10));
            steps.add(Step.screenshot(n + "_3ich_halb"));
            steps.add(Step.waitTicks(12));
            steps.add(Step.screenshot(n + "_4ich_gespannt"));
            steps.add(Step.hud(false));
            steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK)));
            steps.add(Step.waitTicks(2));
            steps.add(Step.screenshot(n + "_5hinten_gespannt"));
            steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.THIRD_PERSON_FRONT)));
            steps.add(Step.waitTicks(2));
            steps.add(Step.screenshot(n + "_6vorn_gespannt"));
            steps.add(Step.once(minecraft -> minecraft.options.keyUse.setDown(false)));
            steps.add(Step.waitTicks(30));
            steps.add(Step.screenshot(n + "_7vorn"));
            steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK)));
            steps.add(Step.waitTicks(4));
            steps.add(Step.screenshot(n + "_8hinten"));
            steps.add(Step.hud(true));
            steps.add(Step.command("kill @e[type=minecraft:arrow]"));
        }
        steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.FIRST_PERSON)));

        // --- alle Gegenstände mit eigener Haltung: eigene Sicht, F5 vorn, F5 hinten ---
        for (String id : heldIds()) {
            if (!wanted(id)) {
                continue;
            }
            steps.add(Step.server(any -> {
                ServerPlayer player = player(any);
                boolean shield = id.endsWith("_shield") || id.endsWith("buckler");
                player.setItemInHand(InteractionHand.MAIN_HAND, shield ? ItemStack.EMPTY : stack(id));
                player.setItemInHand(InteractionHand.OFF_HAND, shield ? stack(id) : ItemStack.EMPTY);
            }));
            steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.FIRST_PERSON)));
            steps.add(Step.waitTicks(36));
            steps.add(Step.screenshot("hand_" + name(id) + "_1ich"));
            steps.add(Step.hud(false));
            steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.THIRD_PERSON_FRONT)));
            steps.add(Step.waitTicks(3));
            steps.add(Step.screenshot("hand_" + name(id) + "_2vorn"));
            steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK)));
            steps.add(Step.waitTicks(3));
            steps.add(Step.screenshot("hand_" + name(id) + "_3hinten"));
            steps.add(Step.hud(true));
        }
        steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.FIRST_PERSON)));

        // --- Puppen in einer Reihe: so sehen andere Spieler die Speere (von der Seite und schräg von vorn) ---
        List<String> lineup = new ArrayList<>(SPEARS);
        lineup.add("kingdomsatwar:steel_halberd");
        steps.add(Step.server(any -> {
            ServerPlayer player = player(any);
            ServerLevel level = player.level();
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            BlockPos top = origin(level);
            for (int i = 0; i < lineup.size(); i++) {
                double x = top.getX() + 0.5 + (i - (lineup.size() - 1) / 2.0) * 2.4;
                level.getServer().getCommands().performPrefixedCommand(level.getServer().createCommandSourceStack().withSuppressedOutput(),
                        String.format(Locale.ROOT, "summon minecraft:mannequin %.2f %d %.2f {Rotation:[-90f,0f],immovable:1b,"
                                + "hide_description:1b,equipment:{mainhand:{id:\"%s\",count:1}}}", x, top.getY(), top.getZ() - 6.5,
                                lineup.get(i)));
            }
            player.teleportTo(level, top.getX() + 0.5, top.getY() + 1.0, top.getZ() + 1.0, Set.of(), 180.0f, 6.0f, true);
        }));
        steps.add(Step.hud(false));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("puppen_seite"));
        steps.add(Step.command("execute as @e[type=minecraft:mannequin] at @s run tp @s ~ ~ ~ -20 0"));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("puppen_vorn"));
        steps.add(Step.command("execute as @e[type=minecraft:mannequin] at @s run tp @s ~ ~ ~ 160 0"));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("puppen_hinten"));
        steps.add(Step.hud(true));
        steps.add(Step.command("kill @e[type=minecraft:mannequin]"));

        // --- Bögen in fremder Hand: Puppen (ruhig) und Schützen der Armee (ruhig und beim Zielen) ---
        if (BOWS.stream().anyMatch(HoldShowcase::wanted)) {
            steps.add(Step.server(any -> {
                ServerPlayer player = player(any);
                ServerLevel level = player.level();
                BlockPos top = origin(level);
                String[] held = {"minecraft:bow", "kingdomsatwar:star_bow"};
                for (int i = 0; i < held.length; i++) {
                    double x = top.getX() + 0.5 + (i - 2.5) * 2.2;
                    level.getServer().getCommands().performPrefixedCommand(level.getServer().createCommandSourceStack().withSuppressedOutput(),
                            String.format(Locale.ROOT, "summon minecraft:mannequin %.2f %d %.2f {Rotation:[-90f,0f],immovable:1b,"
                                    + "hide_description:1b,equipment:{mainhand:{id:\"%s\",count:1}}}", x, top.getY(), top.getZ() - 6.5, held[i]));
                }
                // zwei Bogenschützen und ein Armbrustschütze der Armee: einer ruhig, zwei zielend
                io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType[] types = {
                        io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType.ARCHER,
                        io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType.ARCHER,
                        io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType.CROSSBOWMAN};
                for (int i = 0; i < types.length; i++) {
                    var soldier = io.github.sirschrolli.kingdomsatwar.registry.ModEntities.SOLDIERS.get(types[i]).get().create(level,
                            net.minecraft.world.entity.EntitySpawnReason.COMMAND);
                    if (soldier == null) {
                        continue;
                    }
                    soldier.equip();
                    soldier.setNoAi(true);
                    soldier.snapTo(top.getX() + 0.5 + (i - 0.5) * 2.2 + 2.2, top.getY(), top.getZ() - 6.5, -90.0f, 0.0f);
                    soldier.setYBodyRot(-90.0f);
                    soldier.setYHeadRot(-90.0f);
                    soldier.setAggressive(i > 0);
                    if (i == 1) {
                        soldier.startUsingItem(InteractionHand.MAIN_HAND);
                    }
                    soldier.addTag("kaw_hold");
                    level.addFreshEntity(soldier);
                }
                player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                player.teleportTo(level, top.getX() + 0.5, top.getY() + 1.0, top.getZ() + 1.0, Set.of(), 180.0f, 6.0f, true);
            }));
            steps.add(Step.hud(false));
            // (die Puppen der Speere davor vergehen mit einer Staubwolke – erst verfliegen lassen)
            steps.add(Step.waitTicks(70));
            steps.add(Step.screenshot("boegen_seite"));
            steps.add(Step.command("execute as @e[type=minecraft:mannequin] at @s run tp @s ~ ~ ~ -20 0"));
            steps.add(Step.server(any -> player(any).level().getEntitiesOfClass(io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity.class,
                    player(any).getBoundingBox().inflate(30), e -> e.entityTags().contains("kaw_hold")).forEach(e -> {
                        e.setYRot(-20.0f);
                        e.setYBodyRot(-20.0f);
                        e.setYHeadRot(-20.0f);
                    })));
            steps.add(Step.waitTicks(20));
            steps.add(Step.screenshot("boegen_vorn"));
            steps.add(Step.hud(true));
            steps.add(Step.command("kill @e[type=minecraft:mannequin]"));
            steps.add(Step.command("kill @e[tag=kaw_hold]"));
        }
        steps.add(Step.quit());
    }

    /** Alle Gegenstände der Mod mit eigener Haltung in der Hand (Modell „…_in_hand“, ohne die Speere), dazu Schilde und Bogen. */
    private static List<String> heldIds() {
        return List.of("abyssite_axe", "abyssite_greatsword", "abyssite_hoe", "abyssite_pickaxe", "abyssite_shovel", "abyssite_sword",
                "aetherit_staff", "bannhammer", "bog_club", "claw_blade", "command_baton", "creator_scepter", "draconium_axe",
                "draconium_sword", "draconium_warhammer", "dragon_fishing_rod", "dragon_shield", "ember_fang_blade", "ember_pickaxe",
                "glut_fishing_rod", "gold_fishing_rod", "iron_fishing_rod", "keeper_maul", "kraken_fishing_rod", "miner_hammer",
                "minute_hand", "mirror_shield", "moonsilver_blade", "mystic_deck", "nightsteel_axe", "nightsteel_dagger", "nightsteel_hoe",
                "nightsteel_pickaxe", "nightsteel_shovel", "nightsteel_sword", "ore_seeker", "sea_witch_staff", "shadow_fishing_rod",
                "shepherd_crook", "soul_scythe", "star_bow", "star_fishing_rod", "steel_axe", "steel_buckler", "steel_double_axe",
                "steel_halberd", "steel_hoe", "steel_pickaxe", "steel_shovel", "steel_sword", "surveyors_rod", "tower_shield",
                "troll_club", "vein_breaker")
                .stream().map(n -> "kingdomsatwar:" + n).toList();
    }
}
