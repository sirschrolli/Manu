package io.github.sirschrolli.kingdomsatwar.client.fishing;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.fishing.FishSpecies;
import io.github.sirschrolli.kingdomsatwar.fishing.HookAccess;
import io.github.sirschrolli.kingdomsatwar.fishing.Medium;
import io.github.sirschrolli.kingdomsatwar.network.FishingCatchPayload;
import io.github.sirschrolli.kingdomsatwar.network.FishingEndPayload;
import io.github.sirschrolli.kingdomsatwar.network.FishingStartPayload;
import io.github.sirschrolli.kingdomsatwar.network.HookVoidPayload;
import io.github.sirschrolli.kingdomsatwar.network.OpenFishBookPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Das Angeln auf dem eigenen Bildschirm: der Drill, das Fischbuch, der Haken in der Leere. */
@EventBusSubscriber(modid = KingdomsAtWar.MODID, value = Dist.CLIENT)
public final class FishingClient {
    private FishingClient() {}

    @SubscribeEvent
    static void registerHandlers(RegisterClientPayloadHandlersEvent event) {
        event.register(FishingStartPayload.TYPE, FishingClient::onStart);
        event.register(FishingCatchPayload.TYPE, FishingClient::onCatch);
        event.register(FishingEndPayload.TYPE, FishingClient::onEnd);
        event.register(OpenFishBookPayload.TYPE, FishingClient::onBook);
        event.register(io.github.sirschrolli.kingdomsatwar.network.OpenFishBagPayload.TYPE, (payload, context) -> Minecraft.getInstance().gui.setScreen(
                new FishBagScreen(payload.mainHand() ? net.minecraft.world.InteractionHand.MAIN_HAND : net.minecraft.world.InteractionHand.OFF_HAND)));
        event.register(HookVoidPayload.TYPE, FishingClient::onVoid);
    }

    /** Das Bild einer Art (16 × 16): die eigenen aus der Mod, die vier von Minecraft aus dem Spiel selbst. */
    public static Identifier sprite(FishSpecies species) {
        return species.isVanilla() ? Identifier.withDefaultNamespace("textures/item/" + species.id() + ".png")
                : KingdomsAtWar.id("textures/item/fish/" + species.id() + ".png");
    }

    /**
     * Die Farbe, mit der das Bild einer noch nie gefangenen Art zum Schattenriss wird: fast schwarz, in dieser Deckkraft
     * (0–255). Helle Stellen des Bilds bleiben eine Spur heller – man ahnt die Zeichnung, erkennt sie aber nicht.
     */
    public static int shadow(int alpha) {
        return (alpha << 24) | 0x222A3A;
    }

    public static Component name(FishSpecies species) {
        return new ItemStack(species.item()).getHoverName();
    }

    private static void onStart(FishingStartPayload payload, IPayloadContext context) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.gui.screen() == null) {
            minecraft.gui.setScreen(new FishingScreen(payload.session(), FishSpecies.byIndex(payload.species()), Medium.byIndex(payload.medium()),
                    payload.known(), payload.chest(), payload.zoneBonus()));
        } else {
            // ein anderes Fenster ist offen: Der Fisch ist weg, ehe man hinsieht
            net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(
                    new io.github.sirschrolli.kingdomsatwar.network.FishingResultPayload(payload.session(), false, false, false));
        }
    }

    private static void onCatch(FishingCatchPayload payload, IPayloadContext context) {
        if (Minecraft.getInstance().gui.screen() instanceof FishingScreen screen) {
            screen.caught(payload.session(), payload.cm(), payload.news(), payload.chest(), payload.bagged());
        }
    }

    private static void onEnd(FishingEndPayload payload, IPayloadContext context) {
        if (Minecraft.getInstance().gui.screen() instanceof FishingScreen screen) {
            screen.aborted(payload.session());
        }
    }

    private static void onBook(OpenFishBookPayload payload, IPayloadContext context) {
        Minecraft.getInstance().gui.setScreen(new FishBookScreen(payload.log()));
    }

    private static void onVoid(HookVoidPayload payload, IPayloadContext context) {
        Minecraft minecraft = Minecraft.getInstance();
        Entity entity = minecraft.level == null ? null : minecraft.level.getEntity(payload.entity());
        if (entity instanceof HookAccess hook) {
            if (payload.depth()) {
                hook.kingdomsatwar$hold(payload.level());
            } else {
                hook.kingdomsatwar$anchor(payload.level());
            }
        }
    }
}
