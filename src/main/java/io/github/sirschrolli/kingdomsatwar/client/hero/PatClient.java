package io.github.sirschrolli.kingdomsatwar.client.hero;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.ModKeyMappings;
import io.github.sirschrolli.kingdomsatwar.hero.HeroLevel;
import io.github.sirschrolli.kingdomsatwar.hero.HeroPat;
import io.github.sirschrolli.kingdomsatwar.network.HeroLevelUpPayload;
import io.github.sirschrolli.kingdomsatwar.network.HeroPatEffectPayload;
import io.github.sirschrolli.kingdomsatwar.network.HeroPatPayload;
import com.google.common.reflect.TypeToken;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.client.renderstate.AvatarRenderStateModifier;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Streicheln auf dem Bildschirm, wie in PatPat: Schleichen + Rechtsklick mit leerer Hand (oder die Taste G) streichelt das
 * Wesen im Fadenkreuz – gedrückt halten streichelt weiter. Die Nachricht des Servers zeigt die Hand über dem Kopf; das
 * Wesen wird beim Tätscheln niedriger (um 0,425 Blöcke im tiefsten Punkt, kleine Wesen also stärker), sein Name
 * verschwindet so lange, und wer selbst gestreichelt wird, dem wackelt kurz das Bild. Hier kommt auch die
 * Aufstiegs-Nachricht an.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID, value = Dist.CLIENT)
public final class PatClient {
    /** So lange dauert ein Streicheln (wie in PatPat). */
    public static final long DURATION_MS = 240L;
    /** Um so viele Blöcke wird das Wesen im tiefsten Punkt niedriger (wie in PatPat). */
    public static final float WEIGHT = 0.425f;
    /** Taste G gedrückt halten: alle so vielen Ticks ein neues Streicheln. */
    private static final int REPEAT = 4;
    private static final ContextKey<Float> SQUISH = new ContextKey<>(KingdomsAtWar.id("pat_squish"));
    private static final ContextKey<Boolean> PUSHED = new ContextKey<>(KingdomsAtWar.id("pat_pushed"));
    private static final Map<Integer, Long> PATS = new HashMap<>();
    /** Nur für Vorführungen: hält jedes Streicheln bei diesem Fortschritt an (null: läuft normal). */
    public static @org.jspecify.annotations.Nullable Float devProgress;
    private static int held;

    private PatClient() {}

    @SubscribeEvent
    static void registerHandlers(RegisterClientPayloadHandlersEvent event) {
        event.register(HeroPatEffectPayload.TYPE, PatClient::onPat);
        event.register(HeroLevelUpPayload.TYPE, HeroLevelUpOverlay::onPayload);
        event.register(io.github.sirschrolli.kingdomsatwar.network.HeroImmortalPayload.TYPE, ImmortalOverlay::onPayload);
    }

    private static void onPat(HeroPatEffectPayload payload, IPayloadContext context) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        Entity target = mc.level.getEntity(payload.target());
        if (target != null) {
            start(target);
        }
    }

    /** Zeigt ein Streicheln an {@code target} (auch für Vorführungen). */
    public static void start(Entity target) {
        Minecraft mc = Minecraft.getInstance();
        PATS.put(target.getId(), Util.getMillis());
        if (target == mc.player) {
            io.github.sirschrolli.kingdomsatwar.client.boss.ScreenShake.add(0.08f, 5);
        }
        if (mc.level != null) {
            PatHandParticle hand = PatHandParticle.over(mc.level, target);
            if (hand != null) {
                mc.particleEngine.add(hand);
            }
        }
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        boolean clicked = false;
        while (ModKeyMappings.PAT.consumeClick()) {
            clicked = true;
        }
        if (mc.player == null || mc.gui.screen() != null) {
            held = 0;
            return;
        }
        held = ModKeyMappings.PAT.isDown() ? held + 1 : 0;
        if (clicked || held > 0 && held % REPEAT == 0) {
            tryPat(mc, clicked);
        }
        long now = Util.getMillis();
        if (devProgress == null) {
            PATS.values().removeIf(at -> now - at > DURATION_MS + 100L);
        }
    }

    /**
     * Schleichen + Rechtsklick mit leerer Hand: Als Legende streichelt das, statt das Wesen zu benutzen – das Spiel schickt
     * dann die Streichel-Nachricht statt des Rechtsklicks. Wer noch keine Legende ist, benutzt das Wesen wie immer.
     */
    @SubscribeEvent
    static void onInteract(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.EntityInteract event) {
        if (!event.getLevel().isClientSide() || !HeroPat.gesture(event.getEntity(), event.getHand(), event.getTarget())
                || !HeroPat.unlocked(event.getEntity())) {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
        ClientPacketDistributor.sendToServer(new HeroPatPayload(event.getTarget().getId()));
    }

    private static void tryPat(Minecraft mc, boolean clicked) {
        if (!(mc.hitResult instanceof EntityHitResult hit) || !(hit.getEntity() instanceof LivingEntity target)) {
            return;
        }
        if (HeroLevel.of(mc.player).level() < HeroPat.LEVEL) {
            if (clicked) {
                mc.gui.hud.setOverlayMessage(Component.translatable("hero.kingdomsatwar.pat.locked", HeroPat.LEVEL).withStyle(ChatFormatting.GRAY), false);
            }
            return;
        }
        ClientPacketDistributor.sendToServer(new HeroPatPayload(target.getId()));
    }

    /** Fortschritt eines Streichelns (0 bis 1), anfangs schnell, dann langsamer – danach richten sich Bild und Höhe. */
    public static float progress(long elapsed) {
        if (devProgress != null) {
            return devProgress;
        }
        float t = Mth.clamp(elapsed / (float) DURATION_MS, 0.0f, 1.0f);
        return 1.0f - (1.0f - t) * (1.0f - t);
    }

    /** Wie hoch das Wesen beim Fortschritt {@code progress} gezeichnet wird (1 = wie immer). */
    public static float scaleY(LivingEntity entity, float progress) {
        float weight = WEIGHT / Math.max(0.1f, entity.getBbHeight());
        return Math.max(0.15f, 1.0f - weight * Mth.sin(Mth.PI * progress));
    }

    @SubscribeEvent
    static void registerModifiers(RegisterRenderStateModifiersEvent event) {
        event.registerEntityModifier(new TypeToken<LivingEntityRenderer<? extends LivingEntity, LivingEntityRenderState, ?>>() {},
                (entity, state) -> mark(entity, state));
        event.registerAvatarEntityModifier(new AvatarRenderStateModifier() {
            @Override
            public <T extends Avatar & ClientAvatarEntity> void accept(T avatar, AvatarRenderState state) {
                mark(avatar, state);
            }
        });
    }

    /** Wird das Wesen gerade gestreichelt: wie niedrig es gezeichnet wird, und sein Name verschwindet so lange. */
    private static void mark(LivingEntity entity, LivingEntityRenderState state) {
        Long at = PATS.get(entity.getId());
        long elapsed = at == null ? Long.MAX_VALUE : devProgress != null ? 0L : Util.getMillis() - at;
        if (elapsed > DURATION_MS) {
            state.setRenderData(SQUISH, null);
            return;
        }
        state.setRenderData(SQUISH, scaleY(entity, progress(elapsed)));
        state.nameTag = null;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    static void onRenderPre(RenderLivingEvent.Pre<?, ?, ?> event) {
        LivingEntityRenderState state = event.getRenderState();
        Float squish = state.getRenderData(SQUISH);
        if (event.isCanceled() || squish == null) {
            state.setRenderData(PUSHED, null);
            return;
        }
        PoseStack pose = event.getPoseStack();
        pose.pushPose();
        pose.scale(1.0f, squish, 1.0f);
        state.setRenderData(PUSHED, Boolean.TRUE);
    }

    @SubscribeEvent
    static void onRenderPost(RenderLivingEvent.Post<?, ?, ?> event) {
        LivingEntityRenderState state = event.getRenderState();
        if (Boolean.TRUE.equals(state.getRenderData(PUSHED))) {
            state.setRenderData(PUSHED, null);
            event.getPoseStack().popPose();
        }
    }
}
