package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.entity.creator.DivineShotEntity;
import io.github.sirschrolli.kingdomsatwar.registry.AbyssBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.AnniBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.CreatorBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ForgeBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import io.github.sirschrolli.kingdomsatwar.registry.MonsterGear;
import io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ShadowItems;
import io.github.sirschrolli.kingdomsatwar.registry.StarBlocks;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.client.CameraType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

/**
 * Waffen in der Hand ({@code weapons_hand}): jede Waffe aus eigener Sicht, von vorn und von hinten (F5) – so zeigt sich, ob
 * sie richtig liegt –, dazu der geworfene Lichtspeer und ein geworfener Dreizack im Flug, von der Seite gesehen.
 */
final class WeaponShowcase {
    private WeaponShowcase() {}

    private static Map<String, Supplier<? extends Item>> weapons() {
        Map<String, Supplier<? extends Item>> map = new LinkedHashMap<>();
        map.put("schoepferspeer", CreatorBlocks.CREATOR_SCEPTER);
        map.put("gezeitenspeer", SeaBlocks.TIDE_SPEAR);
        map.put("glutspeer", ForgeBlocks.GLUT_SPEAR);
        map.put("sternenlanze", StarBlocks.STAR_LANCE);
        map.put("weltenspeer", AbyssBlocks.WORLD_SPEAR);
        map.put("eisenspeer", () -> Items.IRON_SPEAR);
        map.put("dreizack", () -> Items.TRIDENT);
        map.put("bannhammer", AnniBlocks.BANNHAMMER);
        map.put("seelensense", MonsterGear.SOUL_SCYTHE);
        map.put("moorkeule", MonsterGear.BOG_CLUB);
        map.put("klauenklinge", MonsterGear.CLAW_BLADE);
        map.put("trollkeule", ModItems.TROLL_CLUB);
        map.put("draconiumschwert", ModItems.DRACONIUM_SWORD);
        map.put("mondsilberklinge", ShadowItems.MOONSILVER_BLADE);
        map.put("abyssitschwert", ShadowItems.ABYSSITE_SWORD);
        map.put("stahlschwert", ModItems.STEEL_SWORD);
        map.put("stahlspitzhacke", ModItems.STEEL_PICKAXE);
        map.put("diamantschwert", () -> Items.DIAMOND_SWORD);
        map.put("flammenpeitsche", io.github.sirschrolli.kingdomsatwar.registry.FlameSanctuary.FLAME_WHIP);
        return map;
    }

    private static ServerPlayer player(ServerLevel any) {
        return any.getServer().getPlayerList().getPlayers().getFirst();
    }

    static void hand(Deque<Step> steps) {
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
            // näher heran: die Kamera hinter und vor dem Spieler steht nur gut zwei Blöcke weg
            player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.CAMERA_DISTANCE).setBaseValue(2.3);
            BlockPos top = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, new BlockPos(0, 0, 0));
            player.teleportTo(level, 0.5, top.getY(), 0.5, Set.of(), 0.0f, 4.0f, true);
        }));
        steps.add(Step.hud(false));
        // die Anzeige wieder an (sonst fehlt die Hand in der eigenen Sicht) – der Chat bleibt aus
        steps.add(Step.once(minecraft -> {
            if (minecraft.gui.hud.isHidden()) {
                minecraft.gui.hud.toggle();
            }
        }));
        steps.add(Step.waitTicks(40));
        steps.add(Step.waitForChunks(400));
        String only = System.getProperty("kingdomsatwar.weapons", "");
        for (Map.Entry<String, Supplier<? extends Item>> weapon : weapons().entrySet()) {
            if (!only.isBlank() && !Set.of(only.split(",")).contains(weapon.getKey())) {
                continue;
            }
            steps.add(Step.server(any -> player(any).setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(weapon.getValue().get()))));
            steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.FIRST_PERSON)));
            steps.add(Step.waitTicks(8));
            steps.add(Step.screenshot("hand_" + weapon.getKey() + "_1ich"));
            steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.THIRD_PERSON_FRONT)));
            steps.add(Step.waitTicks(4));
            steps.add(Step.screenshot("hand_" + weapon.getKey() + "_2vorn"));
            steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK)));
            steps.add(Step.waitTicks(4));
            steps.add(Step.screenshot("hand_" + weapon.getKey() + "_3hinten"));
            if (weapon.getKey().contains("speer") || weapon.getKey().contains("lanze") || weapon.getKey().equals("dreizack")) {
                // die Taste zum Benutzen gehalten: Ansturm (Speere) oder Ausholen (Dreizack)
                steps.add(Step.once(minecraft -> minecraft.options.keyUse.setDown(true)));
                steps.add(Step.waitTicks(14));
                steps.add(Step.screenshot("hand_" + weapon.getKey() + "_4halten_hinten"));
                steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.THIRD_PERSON_FRONT)));
                steps.add(Step.waitTicks(3));
                steps.add(Step.screenshot("hand_" + weapon.getKey() + "_5halten_vorn"));
                steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.FIRST_PERSON)));
                steps.add(Step.waitTicks(3));
                steps.add(Step.screenshot("hand_" + weapon.getKey() + "_6halten_ich"));
                steps.add(Step.once(minecraft -> minecraft.options.keyUse.setDown(false)));
                steps.add(Step.waitTicks(12));
                steps.add(Step.command("kill @e[type=!minecraft:player]"));
            }
        }
        // --- Würfe, von der Seite gesehen: langsam quer durchs Bild ---
        steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.FIRST_PERSON)));
        steps.add(Step.server(any -> {
            ServerPlayer player = player(any);
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            ServerLevel level = player.level();
            Vec3 from = player.position().add(-3.0, 1.9, 4.0);
            DivineShotEntity.thrownSpear(level, player, from, new Vec3(1.0, 0.0, 0.0), 0.12, 0.0f);
            ThrownTrident trident = new ThrownTrident(level, player, new ItemStack(Items.TRIDENT));
            trident.snapTo(from.x, from.y - 1.0, from.z, 0.0f, 0.0f);
            trident.setNoGravity(true);
            trident.shoot(1.0, 0.0, 0.0, 0.12f, 0.0f);
            level.addFreshEntity(trident);
        }));
        steps.add(Step.waitTicks(22));
        steps.add(Step.screenshot("wurf_seite"));
        // --- der Wurf des Lichtspeers aus der Hand: eigene Sicht und von hinten, kurz nach dem Abwurf ---
        for (CameraType camera : new CameraType[] {CameraType.FIRST_PERSON, CameraType.THIRD_PERSON_BACK}) {
            steps.add(Step.command("kill @e[type=!minecraft:player]"));
            steps.add(Step.once(minecraft -> minecraft.options.setCameraType(camera)));
            steps.add(Step.server(any -> player(any).setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(CreatorBlocks.CREATOR_SCEPTER.get()))));
            steps.add(Step.waitTicks(25));
            steps.add(Step.server(any -> {
                ServerPlayer player = player(any);
                player.getMainHandItem().use(player.level(), player, InteractionHand.MAIN_HAND);
            }));
            steps.add(Step.waitTicks(1));
            steps.add(Step.screenshot("wurf_lichtspeer_" + (camera == CameraType.FIRST_PERSON ? "ich" : "hinten")));
        }
        steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.FIRST_PERSON)));
        steps.add(Step.quit());
    }
}
