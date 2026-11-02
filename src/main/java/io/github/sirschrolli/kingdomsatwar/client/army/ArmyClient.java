package io.github.sirschrolli.kingdomsatwar.client.army;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.army.Recruitment;
import io.github.sirschrolli.kingdomsatwar.network.OpenMercenaryBoardPayload;
import io.github.sirschrolli.kingdomsatwar.network.OpenRecruitScreenPayload;
import io.github.sirschrolli.kingdomsatwar.network.RecruitResultPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Nachrichten vom Server rund um die Armee: Feldherrentisch und Söldnertafel öffnen und Ergebnisse anzeigen. */
@EventBusSubscriber(modid = KingdomsAtWar.MODID, value = Dist.CLIENT)
public final class ArmyClient {
    private ArmyClient() {}

    @SubscribeEvent
    static void registerHandlers(RegisterClientPayloadHandlersEvent event) {
        event.register(OpenRecruitScreenPayload.TYPE, ArmyClient::onOpen);
        event.register(OpenMercenaryBoardPayload.TYPE, ArmyClient::onOpenBoard);
        event.register(RecruitResultPayload.TYPE, ArmyClient::onResult);
    }

    private static void onOpen(OpenRecruitScreenPayload payload, IPayloadContext context) {
        Minecraft.getInstance().gui.setScreen(new WarTableScreen(payload.pos(), payload.armyLimit()));
    }

    private static void onOpenBoard(OpenMercenaryBoardPayload payload, IPayloadContext context) {
        Minecraft.getInstance().gui.setScreen(new MercenaryScreen(payload.pos(), payload.armyLimit(), payload.village()));
    }

    private static void onResult(RecruitResultPayload payload, IPayloadContext context) {
        Recruitment.Result[] results = Recruitment.Result.values();
        if (payload.result() < 0 || payload.result() >= results.length) {
            return;
        }
        if (Minecraft.getInstance().gui.screen() instanceof WarTableScreen screen) {
            screen.onResult(results[payload.result()]);
        } else if (Minecraft.getInstance().gui.screen() instanceof MercenaryScreen screen) {
            screen.onResult(results[payload.result()]);
        }
    }
}
