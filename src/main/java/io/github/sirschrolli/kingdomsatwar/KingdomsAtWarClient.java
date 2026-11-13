package io.github.sirschrolli.kingdomsatwar;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/** Nur im Spiel (nicht auf dem Server): Einstellungs-Knopf in der Mod-Liste. */
@Mod(value = KingdomsAtWar.MODID, dist = Dist.CLIENT)
public final class KingdomsAtWarClient {
    public KingdomsAtWarClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}
