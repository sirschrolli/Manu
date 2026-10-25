package io.github.sirschrolli.kingdomsatwar;

import com.mojang.logging.LogUtils;
import io.github.sirschrolli.kingdomsatwar.gametest.ModGameTests;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModCreativeTabs;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;

/**
 * Hauptklasse der Mod "Kingdoms at War".
 *
 * <p>Die Mod vereint die Systeme aus dem Konzept:
 * <ul>
 *     <li><b>Kingdoms at War</b> – Baublöcke, Landwirtschaft und (später) Armeen und Belagerung</li>
 *     <li><b>Dragon Warfare</b> – Drachenerze, Dracheneier und (später) die Drachen selbst</li>
 *     <li><b>Dragon Architect</b> – (später) Bauvorlagen scannen und von NPCs bauen lassen</li>
 * </ul>
 */
@Mod(KingdomsAtWar.MODID)
public final class KingdomsAtWar {
    public static final String MODID = "kingdomsatwar";
    public static final Logger LOGGER = LogUtils.getLogger();

    public KingdomsAtWar(IEventBus modBus, ModContainer container) {
        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModCreativeTabs.TABS.register(modBus);
        ModGameTests.TEST_FUNCTIONS.register(modBus);

        modBus.addListener(ModGameTests::registerTests);
        modBus.addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Kingdoms at War geladen: {} Blöcke, {} Gegenstände",
                ModBlocks.BLOCKS.getEntries().size(), ModItems.ITEMS.getEntries().size());
    }

    /** Erzeugt eine {@link Identifier} im Namensraum dieser Mod. */
    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }
}
