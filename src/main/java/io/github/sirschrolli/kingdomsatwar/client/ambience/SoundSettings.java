package io.github.sirschrolli.kingdomsatwar.client.ambience;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.config.ModClientConfig;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.options.SoundOptionsScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

/**
 * Der eigene Regler der Biom-Klänge (seit 0.79): In „Optionen → Musik &amp; Geräusche“ steht unter den Reglern von Minecraft
 * eine Zeile „Kingdoms at War“ mit dem Regler <b>Biom-Klänge</b> (0 % = aus). Er schreibt denselben Wert wie die Einstellung
 * der Mod ({@link ModClientConfig#BIOME_AMBIENCE_VOLUME}) und wirkt sofort – die Klangwelten hängen damit nicht mehr am
 * Regler „Umgebung“.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID, value = Dist.CLIENT)
public final class SoundSettings {
    private static boolean loading;
    private static final OptionInstance<Double> BIOME_VOLUME = new OptionInstance<>("kingdomsatwar.options.biomeAmbience",
            OptionInstance.cachedConstantTooltip(Component.translatable("kingdomsatwar.options.biomeAmbience.tooltip")),
            SoundSettings::label, OptionInstance.UnitDouble.INSTANCE, 0.7, value -> {
                if (!loading) {
                    store(value);
                }
            });

    private SoundSettings() {}

    @SubscribeEvent
    static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof SoundOptionsScreen screen)) {
            return;
        }
        for (GuiEventListener child : screen.children()) {
            if (child instanceof OptionsList list) {
                loading = true;
                try {
                    BIOME_VOLUME.set(current());
                } finally {
                    loading = false;
                }
                list.addHeader(Component.translatable("kingdomsatwar.options.sounds"));
                list.addBig(BIOME_VOLUME);
                return;
            }
        }
    }

    private static Component label(Component caption, double value) {
        int percent = (int) Math.round(value * 100.0);
        return percent == 0 ? Options.genericValueLabel(caption, CommonComponents.OPTION_OFF)
                : Component.translatable("options.percent_value", caption, percent);
    }

    /** Der Wert aus den Einstellungen der Mod (0 – 1); sind sie noch nicht geladen, der Standard. */
    private static double current() {
        try {
            return ModClientConfig.BIOME_AMBIENCE.get() ? ModClientConfig.BIOME_AMBIENCE_VOLUME.get() / 100.0 : 0.0;
        } catch (IllegalStateException e) {
            return 0.7;
        }
    }

    private static void store(double value) {
        try {
            int percent = (int) Math.round(value * 100.0);
            ModClientConfig.BIOME_AMBIENCE_VOLUME.set(percent);
            ModClientConfig.BIOME_AMBIENCE_VOLUME.save();
            if (percent > 0 && !ModClientConfig.BIOME_AMBIENCE.get()) {
                ModClientConfig.BIOME_AMBIENCE.set(true);
                ModClientConfig.BIOME_AMBIENCE.save();
            }
        } catch (IllegalStateException e) {
            KingdomsAtWar.LOGGER.debug("Einstellungen noch nicht geladen – Lautstärke der Biom-Klänge nicht gespeichert");
        }
    }
}
