package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.registry.CreatorBlocks;
import java.util.Deque;
import java.util.Set;
import net.minecraft.client.CameraType;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.options.SoundOptionsScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

/**
 * Probe der Korrekturen aus 0.78.2 ({@code fix_wings}): die Schwingen des Schöpfers von vorn (auch mit gedrehtem Leib), von
 * hinten und im Laufen; der Regler „Biom-Klänge“ in den Toneinstellungen; die Chronik per Taste.
 */
final class WingFixShowcase {
    private WingFixShowcase() {}

    static void wings(Deque<Step> steps) {
        steps.add(Step.once(minecraft -> ShowcaseRunner.hideHand = true));
        for (String command : new String[] {"gamerule advance_time false", "time set 6000", "weather clear", "gamerule spawn_mobs false",
                "kill @e[type=!minecraft:player]", "clear", "gamemode survival"}) {
            steps.add(Step.command(command));
        }
        steps.add(Step.server(any -> {
            ServerPlayer player = any.getServer().getPlayerList().getPlayers().getFirst();
            ServerLevel level = any.getServer().overworld();
            player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(CreatorBlocks.CREATOR_HALO.get()));
            player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(CreatorBlocks.CREATOR_WINGS.get()));
            player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(CreatorBlocks.CREATOR_RINGS.get()));
            player.getAbilities().invulnerable = true;
            player.onUpdateAbilities();
            BlockPos top = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, new BlockPos(0, 0, 0));
            player.teleportTo(level, 0.5, top.getY(), 0.5, Set.of(), 0.0f, 8.0f, true);
        }));
        steps.add(Step.hud(false));
        steps.add(Step.waitTicks(40));
        steps.add(Step.waitForChunks(400));
        steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.THIRD_PERSON_FRONT)));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("schwingen_vorn"));
        // der Kopf dreht sich, der Leib bleibt zurück (so wie im Bild des Spielers): beide Flügel müssen zu sehen sein
        steps.add(turn(48.0f));
        steps.add(Step.waitTicks(12));
        steps.add(Step.screenshot("schwingen_vorn_gedreht"));
        steps.add(turn(-96.0f));
        steps.add(Step.waitTicks(12));
        steps.add(Step.screenshot("schwingen_vorn_gedreht_2"));
        steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK)));
        steps.add(Step.waitTicks(12));
        steps.add(Step.screenshot("schwingen_hinten"));
        // im Laufen
        steps.add(Step.once(minecraft -> minecraft.options.keyUp.setDown(true)));
        steps.add(Step.waitTicks(16));
        steps.add(Step.screenshot("schwingen_laufen"));
        steps.add(Step.once(minecraft -> minecraft.options.keyUp.setDown(false)));
        steps.add(Step.once(minecraft -> minecraft.options.keyShift.setDown(true)));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screenshot("schwingen_schleichen"));
        steps.add(Step.once(minecraft -> minecraft.options.keyShift.setDown(false)));
        // der Regler in den Toneinstellungen (ans Ende gescrollt)
        steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.FIRST_PERSON)));
        steps.add(Step.screen(minecraft -> new SoundOptionsScreen(null, minecraft.options)));
        steps.add(Step.waitTicks(6));
        steps.add(Step.once(minecraft -> {
            if (minecraft.gui.screen() != null) {
                for (GuiEventListener child : minecraft.gui.screen().children()) {
                    if (child instanceof OptionsList list) {
                        list.setScrollAmount(list.maxScrollAmount());
                    }
                }
            }
        }));
        steps.add(Step.waitTicks(6));
        steps.add(Step.screenshot("regler_biom_klaenge"));
        steps.add(Step.once(minecraft -> minecraft.gui.setScreen(null)));
        // die Chronik per Taste (so, wie die Taste es tut)
        steps.add(Step.once(minecraft -> ClientPacketDistributor.sendToServer(
                io.github.sirschrolli.kingdomsatwar.network.RequestChroniclePayload.INSTANCE)));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("chronik_per_taste"));
        steps.add(Step.once(minecraft -> minecraft.gui.setScreen(null)));
        steps.add(Step.quit());
    }

    /** Dreht nur den Blick des Spielers (der Leib folgt erst ab 50 Grad). */
    private static Step turn(float by) {
        return Step.server(any -> {
            ServerPlayer player = any.getServer().getPlayerList().getPlayers().getFirst();
            player.teleportTo(any.getServer().overworld(), player.getX(), player.getY(), player.getZ(), Set.of(), player.getYRot() + by,
                    8.0f, true);
        });
    }
}
