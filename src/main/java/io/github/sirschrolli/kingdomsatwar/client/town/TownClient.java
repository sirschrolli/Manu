package io.github.sirschrolli.kingdomsatwar.client.town;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.economy.town.Blueprint;
import io.github.sirschrolli.kingdomsatwar.economy.town.BuildingType;
import io.github.sirschrolli.kingdomsatwar.item.BlueprintItem;
import io.github.sirschrolli.kingdomsatwar.network.OpenTownPayload;
import io.github.sirschrolli.kingdomsatwar.network.OpenTradePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Siedlung im Spiel: Stadt- und Handelsfenster öffnen und die Vorschau des Bauplans (Umriss aus Lichtpunkten). */
@EventBusSubscriber(modid = KingdomsAtWar.MODID, value = Dist.CLIENT)
public final class TownClient {
    private static final DustParticleOptions OUTLINE = new DustParticleOptions(0xF2C23A, 0.9f);
    private static final DustParticleOptions FLOOR = new DustParticleOptions(0xFFF2C8, 0.6f);

    private TownClient() {}

    @SubscribeEvent
    static void registerHandlers(RegisterClientPayloadHandlersEvent event) {
        event.register(OpenTownPayload.TYPE, TownClient::onOpenTown);
        event.register(OpenTradePayload.TYPE, TownClient::onOpenTrade);
    }

    private static void onOpenTown(OpenTownPayload payload, IPayloadContext context) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gui.screen() instanceof TownScreen screen && screen.banner().equals(payload.banner())) {
            screen.update(payload.view(), payload.notice().orElse(null), payload.good());
        } else {
            TownScreen screen = new TownScreen(payload.banner(), payload.view());
            mc.gui.setScreen(screen);
            payload.notice().ifPresent(n -> screen.update(payload.view(), n, payload.good()));
        }
    }

    private static void onOpenTrade(OpenTradePayload payload, IPayloadContext context) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gui.screen() instanceof TradeScreen screen && screen.entityId() == payload.entity()) {
            screen.update(payload);
        } else {
            mc.gui.setScreen(new TradeScreen(payload));
        }
    }

    /** Hält der Spieler einen Bauplan, zeigen Lichtpunkte, wo der Bau stehen würde (Eingang grün). */
    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        ClientLevel level = mc.level;
        if (player == null || level == null || mc.gui.screen() != null || (level.getGameTime() % 4) != 0) {
            return;
        }
        ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (!(stack.getItem() instanceof BlueprintItem)) {
            stack = player.getItemInHand(InteractionHand.OFF_HAND);
        }
        BuildingType type = BlueprintItem.type(stack);
        if (type == null || !(mc.hitResult instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) {
            return;
        }
        BlockPos origin = BlueprintItem.origin(hit.getBlockPos(), hit.getDirection());
        Rotation rotation = BlueprintItem.rotation(player.getDirection(), BlueprintItem.turn(stack));
        Blueprint plan = type.blueprint();
        BoundingBox box = plan.worldBounds(origin, rotation);
        double y = origin.getY() + 0.05;
        double x0 = box.minX();
        double x1 = box.maxX() + 1.0;
        double z0 = box.minZ();
        double z1 = box.maxZ() + 1.0;
        // Grundriss am Boden
        for (double x = x0; x <= x1; x += 0.5) {
            level.addParticle(OUTLINE, x, y, z0, 0, 0, 0);
            level.addParticle(OUTLINE, x, y, z1, 0, 0, 0);
        }
        for (double z = z0 + 0.5; z < z1; z += 0.5) {
            level.addParticle(OUTLINE, x0, y, z, 0, 0, 0);
            level.addParticle(OUTLINE, x1, y, z, 0, 0, 0);
        }
        // Eckpfosten bis zur Firsthöhe
        double top = box.maxY() + 1.0;
        for (double h = y + 0.5; h <= top; h += 0.75) {
            level.addParticle(FLOOR, x0, h, z0, 0, 0, 0);
            level.addParticle(FLOOR, x1, h, z0, 0, 0, 0);
            level.addParticle(FLOOR, x0, h, z1, 0, 0, 0);
            level.addParticle(FLOOR, x1, h, z1, 0, 0, 0);
        }
        // Eingang
        BlockPos door = Blueprint.world(origin, rotation, plan.entrance());
        level.addParticle(ParticleTypes.HAPPY_VILLAGER, door.getX() + 0.5, door.getY() + 0.6, door.getZ() + 0.5, 0, 0, 0);
    }
}
