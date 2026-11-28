package io.github.sirschrolli.kingdomsatwar.client.combat;

import com.google.common.reflect.TypeToken;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.entity.combat.Act;
import io.github.sirschrolli.kingdomsatwar.entity.combat.MoveKind;
import io.github.sirschrolli.kingdomsatwar.registry.ModAttachments;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import org.jspecify.annotations.Nullable;

/**
 * Die Haltung der Monster bei ihren besonderen Angriffen (seit 1.14): Wer ausholt, duckt sich, bäumt sich auf, bebt,
 * dreht sich zurück oder reckt den Kopf – so sieht man kommen, was gleich geschieht. Das gilt für jedes Monster (der
 * ganze Körper neigt, staucht und dreht sich); Menschengestalten heben dazu die Arme ({@link #humanoid},
 * {@link #illager}).
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID, value = Dist.CLIENT)
public final class CombatPoses {
    /**
     * Was das Monster gerade tut.
     *
     * @param windup Fortschritt der Vorwarnung 0–1
     * @param act    Ticks seit der Ausführung (negativ während der Vorwarnung)
     */
    public record Pose(MoveKind kind, float windup, float act, float body) {
    }

    static final ContextKey<Pose> POSE = new ContextKey<>(KingdomsAtWar.id("combat_pose"));
    private static final ContextKey<Boolean> PUSHED = new ContextKey<>(KingdomsAtWar.id("combat_pushed"));

    private CombatPoses() {}

    @SubscribeEvent
    static void registerModifiers(RegisterRenderStateModifiersEvent event) {
        event.registerEntityModifier(new TypeToken<LivingEntityRenderer<? extends LivingEntity, LivingEntityRenderState, ?>>() {},
                CombatPoses::mark);
    }

    private static void mark(LivingEntity entity, LivingEntityRenderState state) {
        state.setRenderData(POSE, pose(entity, state.partialTick, state.bodyRot));
    }

    /** Die Haltung eines Wesens jetzt (oder null). */
    public static @Nullable Pose pose(LivingEntity entity, float partialTick, float body) {
        if (!(entity instanceof Mob) || !entity.hasData(ModAttachments.COMBAT_ACT.get())) {
            return null;
        }
        Act act = entity.getData(ModAttachments.COMBAT_ACT.get());
        MoveKind kind = act.kind();
        if (kind == null || kind.pose() == MoveKind.Pose.NONE) {
            return null;
        }
        float t = entity.level().getGameTime() - act.start() + partialTick;
        if (t < 0.0f || t > act.windup() + 60.0f) {
            return null;
        }
        float windup = act.windup() <= 0 ? 1.0f : Mth.clamp(t / act.windup(), 0.0f, 1.0f);
        return new Pose(kind, windup, t - act.windup(), body);
    }

    public static @Nullable Pose of(LivingEntityRenderState state) {
        return state.getRenderData(POSE);
    }

    static float ease(float x) {
        x = Mth.clamp(x, 0.0f, 1.0f);
        return x * x * (3.0f - 2.0f * x);
    }

    /** 1 beim Loslegen, dann abklingend über {@code ticks}. */
    static float decay(float act, float ticks) {
        return act < 0.0f ? 0.0f : Mth.clamp(1.0f - act / ticks, 0.0f, 1.0f);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    static void onRenderPre(RenderLivingEvent.Pre<?, ?, ?> event) {
        LivingEntityRenderState state = event.getRenderState();
        Pose pose = state.getRenderData(POSE);
        if (event.isCanceled() || pose == null) {
            state.setRenderData(PUSHED, null);
            return;
        }
        PoseStack stack = event.getPoseStack();
        stack.pushPose();
        body(pose, stack, state);
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

    /** Neigt den ganzen Körper um die Füße (positiv = nach vorn). */
    private static void lean(PoseStack stack, float body, float degrees) {
        if (degrees == 0.0f) {
            return;
        }
        float facing = 180.0f - body;
        stack.rotate(Axis.YP.rotationDegrees(facing));
        stack.rotate(Axis.XP.rotationDegrees(-degrees));
        stack.rotate(Axis.YP.rotationDegrees(-facing));
    }

    private static void squash(PoseStack stack, float amount) {
        if (amount != 0.0f) {
            stack.scale(1.0f + amount * 0.5f, 1.0f - amount, 1.0f + amount * 0.5f);
        }
    }

    /** Die Haltung des ganzen Körpers (für alle Monster). */
    static void body(Pose p, PoseStack stack, LivingEntityRenderState state) {
        float w = ease(p.windup());
        float a = p.act();
        switch (p.kind().pose()) {
            case CROUCH -> {
                if (a < 0.0f) {
                    lean(stack, p.body(), 10.0f * w);
                    squash(stack, 0.17f * w);
                } else {
                    float k = decay(a, 6.0f);
                    lean(stack, p.body(), 18.0f * k);
                    squash(stack, -0.12f * k);
                }
            }
            case RAISE -> {
                if (a < 0.0f) {
                    lean(stack, p.body(), -14.0f * w);
                    squash(stack, -0.06f * w);
                } else {
                    float k = decay(a, 6.0f);
                    lean(stack, p.body(), 22.0f * k);
                    squash(stack, 0.1f * k);
                }
            }
            case COIL -> {
                if (a < 0.0f) {
                    stack.rotate(Axis.YP.rotationDegrees(-40.0f * w));
                    squash(stack, 0.05f * w);
                } else {
                    float k = Mth.clamp(a / 7.0f, 0.0f, 1.0f);
                    float spin = -40.0f + 400.0f * (1.0f - (1.0f - k) * (1.0f - k));
                    stack.rotate(Axis.YP.rotationDegrees(spin));
                }
            }
            case TREMBLE -> {
                if (a < 0.0f) {
                    float shake = 0.035f * w;
                    float t = state.ageInTicks;
                    stack.translate(Mth.sin(t * 2.3f) * shake, 0.0f, Mth.cos(t * 2.9f) * shake);
                    lean(stack, p.body(), 7.0f * w);
                    squash(stack, 0.05f * w);
                } else {
                    lean(stack, p.body(), 16.0f + 2.0f * Mth.sin(a * 1.2f));
                }
            }
            case GRAB -> lean(stack, p.body(), a < 0.0f ? 12.0f * w : 14.0f);
            case GUARD -> {
                squash(stack, 0.06f * w);
                lean(stack, p.body(), 5.0f * w);
            }
            case AIM -> {
                if (a < 0.0f) {
                    lean(stack, p.body(), -4.0f * w);
                    if (p.windup() > 0.75f) {
                        float t = state.ageInTicks;
                        stack.translate(Mth.sin(t * 3.1f) * 0.01f, 0.0f, Mth.cos(t * 2.7f) * 0.01f);
                    }
                } else {
                    lean(stack, p.body(), -6.0f * decay(a, 4.0f));
                }
            }
            case REAR -> {
                if (a < 0.0f) {
                    lean(stack, p.body(), -16.0f * w);
                    squash(stack, -0.04f * w);
                } else {
                    lean(stack, p.body(), 12.0f * decay(a, 6.0f));
                }
            }
            case THROW -> {
                if (a < 0.0f) {
                    stack.rotate(Axis.YP.rotationDegrees(25.0f * w));
                    lean(stack, p.body(), -10.0f * w);
                } else {
                    float k = decay(a, 6.0f);
                    stack.rotate(Axis.YP.rotationDegrees(-15.0f * k));
                    lean(stack, p.body(), 10.0f * k);
                }
            }
            case NONE -> {
            }
        }
    }

    // =====================================================================================
    // Arme der Menschengestalten
    // =====================================================================================

    /** Arme (und Kopf) eines Menschenmodells zur Haltung – am Ende von {@code setupAnim} aufrufen. */
    public static void humanoid(HumanoidModel<?> model, LivingEntityRenderState state) {
        Pose p = state.getRenderData(POSE);
        if (p != null) {
            arms(p, model.rightArm, model.leftArm, model.head);
        }
    }

    /** Wie {@link #humanoid}, für das Illager-Skelett (Diener, Plünderer, Beschwörer, Gaukler). */
    public static void illager(ModelPart root, LivingEntityRenderState state) {
        Pose p = state.getRenderData(POSE);
        if (p == null) {
            return;
        }
        ModelPart right = root.getChild("right_arm");
        ModelPart left = root.getChild("left_arm");
        if (p.kind().pose() != MoveKind.Pose.AIM) {
            // die verschränkten Arme verschwinden, die einzelnen zeigen sich
            root.getChild("arms").visible = false;
            right.visible = true;
            left.visible = true;
        }
        arms(p, right, left, root.getChild("head"));
    }

    private static void set(ModelPart part, float weight, float x, float y, float z) {
        part.xRot = Mth.lerp(weight, part.xRot, x);
        part.yRot = Mth.lerp(weight, part.yRot, y);
        part.zRot = Mth.lerp(weight, part.zRot, z);
    }

    static void arms(Pose p, ModelPart right, ModelPart left, ModelPart head) {
        float w = ease(p.windup());
        float a = p.act();
        switch (p.kind()) {
            case SLAM -> {
                if (a < 0.0f) {
                    set(right, w, -2.9f, -0.2f, 0.2f);
                    set(left, w, -2.9f, 0.2f, -0.2f);
                    head.xRot = Mth.lerp(w, head.xRot, -0.3f);
                } else {
                    float k = decay(a, 8.0f);
                    set(right, 1.0f, Mth.lerp(k, -1.4f, -0.3f), -0.1f, 0.1f);
                    set(left, 1.0f, Mth.lerp(k, -1.4f, -0.3f), 0.1f, -0.1f);
                }
            }
            case BASH -> {
                set(right, a < 0.0f ? w : 1.0f, a < 0.0f ? -2.4f : -0.9f, 0.3f, 0.0f);
            }
            case GRAB -> {
                if (a < 0.0f) {
                    set(right, w, -1.55f, -0.55f, 0.0f);
                    set(left, w, -1.55f, 0.55f, 0.0f);
                } else {
                    set(right, 1.0f, -1.45f, -0.12f, 0.0f);
                    set(left, 1.0f, -1.45f, 0.12f, 0.0f);
                }
            }
            case GUARD -> {
                set(right, w, -1.95f, -0.75f, 0.0f);
                set(left, w, -1.95f, 0.75f, 0.0f);
                head.xRot = Mth.lerp(w, head.xRot, 0.25f);
            }
            case ROAR -> {
                float k = a < 0.0f ? w : decay(a, 10.0f);
                set(right, k, 0.35f, 0.0f, 1.05f);
                set(left, k, 0.35f, 0.0f, -1.05f);
                head.xRot = Mth.lerp(k, head.xRot, -0.6f);
            }
            case THROW, BARRAGE -> {
                if (a < 0.0f) {
                    set(right, w, -2.7f, 0.3f, 0.2f);
                } else {
                    set(right, 1.0f, Mth.lerp(decay(a, 6.0f), -0.5f, -1.7f), 0.0f, 0.0f);
                }
            }
            case SWEEP -> {
                float k = a < 0.0f ? w : 1.0f - Mth.clamp((a - 5.0f) / 5.0f, 0.0f, 1.0f);
                set(right, k, -0.2f, 0.0f, 1.45f);
                set(left, k, -0.2f, 0.0f, -1.45f);
            }
            case LUNGE, BITE -> {
                if (a < 0.0f) {
                    set(right, w, 0.6f, 0.0f, 0.15f);
                    set(left, w, 0.6f, 0.0f, -0.15f);
                } else {
                    float k = decay(a, 10.0f);
                    set(right, k, -1.7f, -0.1f, 0.0f);
                    set(left, k, -1.7f, 0.1f, 0.0f);
                }
            }
            case CHARGE -> {
                float k = a < 0.0f ? w : 1.0f;
                set(right, k, 0.75f, 0.0f, 0.2f);
                set(left, k, 0.75f, 0.0f, -0.2f);
                head.xRot = Mth.lerp(k, head.xRot, 0.35f);
            }
            default -> {
            }
        }
    }
}
