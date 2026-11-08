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
 * Streicheln auf dem Bildschirm: Die Taste (G) streichelt das Wesen im Fadenkreuz (gedrückt halten streichelt weiter), die
 * Nachricht des Servers zeigt die Hand über dem Kopf, und das gestreichelte Wesen duckt sich beim Zeichnen kurz – es wird
 * niedriger und ein wenig breiter, als drücke die Hand es sanft zusammen. Hier kommt auch die Aufstiegs-Nachricht an.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID, value = Dist.CLIENT)
public final class PatClient {
    /** So lange dauert ein Streicheln (wie die Hand: acht Ticks). */
    private static final long PAT_MILLIS = PatHandParticle.LIFETIME * 50L;
    /** Gedrückt halten: alle so vielen Ticks ein neues Streicheln. */
    private static final int REPEAT = PatHandParticle.LIFETIME;
    private static final ContextKey<Float> SQUISH = new ContextKey<>(KingdomsAtWar.id("pat_squish"));
    private static final ContextKey<Boolean> PUSHED = new ContextKey<>(KingdomsAtWar.id("pat_pushed"));
    private static final Map<Integer, Long> PATS = new HashMap<>();
    private static int held;

    private PatClient() {}

    @SubscribeEvent
    static void registerHandlers(RegisterClientPayloadHandlersEvent event) {
        event.register(HeroPatEffectPayload.TYPE, PatClient::onPat);
        event.register(HeroLevelUpPayload.TYPE, HeroLevelUpOverlay::onPayload);
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
        PATS.values().removeIf(at -> now - at > PAT_MILLIS + 100L);
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

    /** Wie stark das Wesen gerade gedrückt wird (0 bis etwa 0,16): am stärksten, wenn die Hand aufliegt. */
    static float squish(long at, long now) {
        float t = (now - at) / (float) PAT_MILLIS;
        if (t <= 0.3f || t >= 0.92f) {
            return 0.0f;
        }
        return 0.16f * Mth.sin(Mth.PI * (t - 0.3f) / 0.62f);
    }

    @SubscribeEvent
    static void registerModifiers(RegisterRenderStateModifiersEvent event) {
        event.registerEntityModifier(new TypeToken<LivingEntityRenderer<? extends LivingEntity, LivingEntityRenderState, ?>>() {},
                (entity, state) -> mark(entity.getId(), state));
        event.registerAvatarEntityModifier(new AvatarRenderStateModifier() {
            @Override
            public <T extends Avatar & ClientAvatarEntity> void accept(T avatar, AvatarRenderState state) {
                mark(avatar.getId(), state);
            }
        });
    }

    private static void mark(int id, LivingEntityRenderState state) {
        Long at = PATS.get(id);
        float squish = at == null ? 0.0f : squish(at, Util.getMillis());
        state.setRenderData(SQUISH, squish > 0.0f ? squish : null);
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
        pose.scale(1.0f + squish * 0.55f, 1.0f - squish, 1.0f + squish * 0.55f);
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
