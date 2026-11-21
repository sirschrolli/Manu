package io.github.sirschrolli.kingdomsatwar.client.renderer.creator;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.entity.creator.SchrolliEntity;
import io.github.sirschrolli.kingdomsatwar.entity.creator.SchrolliEntity.Attack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

/**
 * Schrolli der Schöpfer, doppelt so groß wie sein Modell (in der Wahren Gestalt noch ein Drittel größer): Augen, Krone,
 * Heiligenschein, Sonne auf der Brust, Zepterstern und Federspitzen leuchten; das Gewand der Wahren Gestalt ist ein
 * leuchtendes Weltall. Beim Erscheinen, Aufsteigen, im Lichtsprung, bei der Neuschöpfung und im Vergehen leuchtet seine
 * ganze Gestalt in weißem (oder violettem) Licht auf. Der Allmächtige (seit 0.78) glüht wie eine Sonne: das Gewand aus
 * Glut, hinter ihm ein gewaltiger Lichthof, der atmet.
 *
 * <p>Am Boden zieht seine Klinge eine Spur aus Licht hinter sich her: ein Band, das der Schneide folgt und rasch verblasst
 * (golden-weiß, in der Wahren Gestalt violett und türkis). Die Spur wird aus seiner Bewegung berechnet – aus den Haltungen
 * der letzten Augenblicke, mit seiner Drehung und seinem Weg –, darum ist sie bei jeder Bildrate gleich glatt.
 */
public class SchrolliRenderer extends MobRenderer<SchrolliEntity, SchrolliRenderState, SchrolliModel> {
    public static final float SCALE = 2.0f;
    /** Nur für die Vorführung: eine Kunst und ihr Augenblick, die alle Schöpfer zeigen (-1: aus). */
    public static volatile int debugAttack = -1;
    public static volatile float debugTime;
    public static volatile float debugGround = 1.0f;
    public static volatile float debugWalk;
    private static final Identifier TEXTURE = tex("schrolli");
    private static final Identifier TEXTURE_TRUE = tex("schrolli_true");
    private static final Identifier TEXTURE_ALMIGHTY = tex("schrolli_almighty");
    private static final RenderType GLOW_ALMIGHTY = RenderTypes.eyes(tex("schrolli_almighty_glow"));
    private static final RenderType FLASH_ALMIGHTY = RenderTypes.eyes(tex("schrolli_almighty_flash"));
    private static final RenderType GLOW = RenderTypes.eyes(tex("schrolli_glow"));
    private static final RenderType GLOW_TRUE = RenderTypes.eyes(tex("schrolli_true_glow"));
    private static final RenderType FLASH = RenderTypes.eyes(tex("schrolli_flash"));
    private static final RenderType FLASH_TRUE = RenderTypes.eyes(tex("schrolli_true_flash"));

    private static Identifier tex(String name) {
        return KingdomsAtWar.id("textures/entity/schrolli/" + name + ".png");
    }

    /** Die Spur der Klinge: so viele Zwischenlagen, so weit auseinander (Ticks). */
    private static final int TRAIL_STEPS = 26;
    private static final float TRAIL_STEP = 0.2f;
    /** Was sich jeder Schöpfer über seine letzten Augenblicke merkt: {Zeit, Drehung, x, y, z}. */
    private final Map<Integer, ArrayDeque<double[]>> history = new HashMap<>();
    private final SchrolliRenderState probe = new SchrolliRenderState();

    public SchrolliRenderer(EntityRendererProvider.Context context) {
        super(context, new SchrolliModel(context.bakeLayer(CreatorRenderers.SCHROLLI)), 2.6f);
        this.addLayer(new RenderLayer<>(this) {
            @Override
            public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, SchrolliRenderState state, float yRot, float xRot) {
                if (state.isInvisible) {
                    return;
                }
                float fade = fade(state);
                float t = state.ageInTicks;
                float glow = (0.88f + Mth.sin(t * 0.09f) * 0.08f + Mth.sin(t * 0.41f) * 0.04f) * fade;
                if ((state.flags & SchrolliEntity.FLAG_GLARE) != 0) {
                    glow = Math.min(1.0f, glow + 0.3f);
                }
                collector.order(1).submitModel(this.getParentModel(), state, poseStack, state.almighty ? GLOW_ALMIGHTY : state.trueForm ? GLOW_TRUE
                        : GLOW, light, OverlayTexture.NO_OVERLAY, ARGB.color(Math.round(Mth.clamp(glow, 0.0f, 1.0f) * 255.0f), 0xFFFFFF), null,
                        state.outlineColor);
                if (state.almighty && state.deathTime <= 0.0f) {
                    // die Glut seines Gewands lodert ein zweites Mal, warm und rasch pulsierend
                    float shine = 0.28f + Mth.sin(t * 0.13f) * 0.1f + Mth.sin(t * 0.47f) * 0.05f;
                    collector.order(2).submitModel(this.getParentModel(), state, poseStack, GLOW_ALMIGHTY, light, OverlayTexture.NO_OVERLAY,
                            ARGB.color(Math.round(shine * 255.0f), 0xFF9A40), null, state.outlineColor);
                } else if (state.trueForm && state.deathTime <= 0.0f) {
                    // das Weltall seines Gewands schimmert ein zweites Mal, violett und langsam pulsierend
                    float shine = 0.35f + Mth.sin(t * 0.07f) * 0.15f;
                    collector.order(2).submitModel(this.getParentModel(), state, poseStack, GLOW_TRUE, light, OverlayTexture.NO_OVERLAY,
                            ARGB.color(Math.round(shine * 255.0f), 0xD8B0FF), null, state.outlineColor);
                }
                if (state.flash > 0.01f) {
                    int tint = (state.flags & SchrolliEntity.FLAG_CHANNEL) != 0 ? 0xC890FF : 0xFFF6E0;
                    collector.order(3).submitModel(this.getParentModel(), state, poseStack, state.almighty ? FLASH_ALMIGHTY
                            : state.trueForm ? FLASH_TRUE : FLASH, light,
                            OverlayTexture.NO_OVERLAY, ARGB.color(Math.round(Mth.clamp(state.flash, 0.0f, 1.0f) * 255.0f), tint), null,
                            state.outlineColor);
                }
            }
        });
    }

    /** Im Vergehen wird er durchsichtig (das letzte Fünftel). */
    private static float fade(SchrolliRenderState state) {
        if (state.deathTime <= 0.0f) {
            return 1.0f;
        }
        float d = Mth.clamp(state.deathTime / SchrolliEntity.DEATH_TICKS, 0.0f, 1.0f);
        return d < 0.82f ? 1.0f : Math.max(0.0f, 1.0f - (d - 0.82f) / 0.12f);
    }

    /** Wie hell seine ganze Gestalt gerade aufleuchtet. */
    private static float flash(SchrolliEntity entity, Attack attack, float a, float age) {
        if (entity.deathTime > 0) {
            float d = entity.deathTime / (float) SchrolliEntity.DEATH_TICKS;
            return Mth.clamp(0.15f + (d - 0.5f) * 1.8f, 0.0f, 1.0f) + Mth.sin(age * 0.8f) * 0.08f * d;
        }
        return switch (attack) {
            case AWAKEN -> Math.max(Mth.clamp(1.0f - a / 40.0f, 0.0f, 1.0f), Mth.clamp(1.0f - Math.abs(a - SchrolliEntity.AWAKEN_LAND) / 10.0f, 0.0f, 1.0f));
            case ASCEND -> a < SchrolliEntity.ASCEND_SHATTER ? Mth.clamp(a / SchrolliEntity.ASCEND_SHATTER, 0.0f, 1.0f)
                    : Mth.clamp(1.0f - (a - SchrolliEntity.ASCEND_REVEAL) / 40.0f, 0.0f, 1.0f);
            case TRANSCEND -> a < SchrolliEntity.TRANSCEND_COLLAPSE ? Mth.clamp(a / SchrolliEntity.TRANSCEND_COLLAPSE, 0.0f, 0.8f)
                    : Mth.clamp(1.0f - (a - SchrolliEntity.TRANSCEND_REVEAL) / 50.0f, 0.0f, 1.0f);
            case SOLAR_JUDGMENT -> Mth.clamp(1.0f - Math.abs(a - SchrolliEntity.SOLAR_FINAL) / 12.0f, 0.0f, 0.8f);
            case BLINK -> Math.max(Mth.clamp(1.0f - Math.abs(a - 4.0f) / 4.0f, 0.0f, 1.0f), Mth.clamp(1.0f - Math.abs(a - 16.0f) / 5.0f, 0.0f, 1.0f));
            case RECREATION -> 0.3f + Mth.sin(age * 0.25f) * 0.18f;
            case WORD -> Mth.clamp((a - 20.0f) / 36.0f, 0.0f, 1.0f) * (a < 58.0f ? 0.8f : 0.0f);
            case SUPERNOVA -> a > 120.0f && a < 145.0f ? Mth.clamp((a - 120.0f) / 20.0f, 0.0f, 0.7f) : 0.0f;
            // die Landung: Beine und Klinge formen sich in einem Aufleuchten
            case DESCEND -> Mth.clamp(1.0f - Math.abs(a - SchrolliEntity.DESCEND_LAND - 2.0f) / 9.0f, 0.0f, 0.75f);
            case RISE -> Mth.clamp(1.0f - Math.abs(a - SchrolliEntity.RISE_LIFT) / 8.0f, 0.0f, 0.6f);
            default -> 0.0f;
        };
    }

    @Override
    public Identifier getTextureLocation(SchrolliRenderState state) {
        return state.almighty ? TEXTURE_ALMIGHTY : state.trueForm ? TEXTURE_TRUE : TEXTURE;
    }

    @Override
    protected @Nullable RenderType getRenderType(SchrolliRenderState state, boolean isBodyVisible, boolean forceTransparent, boolean appearGlowing) {
        Identifier texture = this.getTextureLocation(state);
        if (!isBodyVisible && !forceTransparent && !appearGlowing) {
            return null;
        }
        return appearGlowing && !isBodyVisible ? RenderTypes.outline(texture) : RenderTypes.entityTranslucent(texture);
    }

    @Override
    protected int getModelTint(SchrolliRenderState state) {
        return ARGB.color(Math.round(fade(state) * 255.0f), 0xFFFFFF);
    }

    @Override
    public SchrolliRenderState createRenderState() {
        return new SchrolliRenderState();
    }

    @Override
    public void extractRenderState(SchrolliEntity entity, SchrolliRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        Attack attack = entity.currentAttack();
        state.attack = attack.ordinal();
        state.attackTime = entity.attackTime(partialTicks);
        state.trueForm = entity.isTrueForm();
        state.almighty = entity.isAlmighty();
        state.flags = (entity.hasFlag(SchrolliEntity.FLAG_HALO) ? SchrolliEntity.FLAG_HALO : 0)
                | (entity.hasFlag(SchrolliEntity.FLAG_ORB) ? SchrolliEntity.FLAG_ORB : 0)
                | (entity.hasFlag(SchrolliEntity.FLAG_HIDDEN) ? SchrolliEntity.FLAG_HIDDEN : 0)
                | (entity.hasFlag(SchrolliEntity.FLAG_CHANNEL) ? SchrolliEntity.FLAG_CHANNEL : 0)
                | (entity.hasFlag(SchrolliEntity.FLAG_STUN) ? SchrolliEntity.FLAG_STUN : 0)
                | (entity.hasFlag(SchrolliEntity.FLAG_GLARE) ? SchrolliEntity.FLAG_GLARE : 0);
        state.wing = Mth.lerp(partialTicks, entity.wingO, entity.wing);
        state.ground = Mth.lerp(partialTicks, entity.groundO, entity.ground);
        if (debugAttack >= 0) {
            // Vorführung: eine Haltung festhalten
            state.attack = debugAttack;
            state.attackTime = debugTime;
            state.ground = debugGround;
            if (debugWalk > 0.0f) {
                state.walkAnimationPos = debugTime;
                state.walkAnimationSpeed = debugWalk;
            }
        }
        state.ascension = entity.ascension(partialTicks);
        state.flash = flash(entity, attack, state.attackTime, state.ageInTicks);
        state.id = entity.getId();
        if ((state.flags & SchrolliEntity.FLAG_HIDDEN) != 0) {
            state.isInvisible = true;
        }
        // im Vergehen nicht rot getönt (er vergeht in Licht)
        if (entity.deathTime > 0) {
            state.hasRedOverlay = false;
        }
    }

    @Override
    public void submit(SchrolliRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(state, poseStack, collector, camera);
        if (state.almighty && !state.isInvisible) {
            // der Lichthof der Sonne hinter ihm (atmet; im Vergehen erlischt er)
            float fade = fade(state) * (state.deathTime > 0.0f ? Math.max(0.0f, 1.0f - state.deathTime / SchrolliEntity.DEATH_TICKS) : 1.0f);
            float t = state.ageInTicks;
            float size = (9.0f + Mth.sin(t * 0.07f) * 0.7f) * state.scale;
            poseStack.pushPose();
            // hinter seinem Rücken, auf Höhe des Heiligenscheins
            float yaw = state.bodyRot * Mth.DEG_TO_RAD;
            poseStack.translate(Mth.sin(yaw) * 1.6f * state.scale, 5.9f * state.scale, -Mth.cos(yaw) * 1.6f * state.scale);
            DivineShotRenderer.billboard(poseStack, collector, camera, DivineShotRenderer.GLOW_DISC, size,
                    ARGB.color(Math.round(120 * fade), 0xFFA040), 0.0f);
            DivineShotRenderer.billboard(poseStack, collector, camera, DivineShotRenderer.GLOW_DISC, size * 0.55f,
                    ARGB.color(Math.round(110 * fade), 0xFFF0C0), 0.0f);
            DivineShotRenderer.billboard(poseStack, collector, camera, DivineShotRenderer.STAR4, size * 0.9f,
                    ARGB.color(Math.round(90 * fade), 0xFFE0A0), t * 0.6f);
            poseStack.popPose();
        }
        if (state.ground > 0.9f && !state.isInvisible && state.deathTime <= 0.0f) {
            this.submitTrail(state, poseStack, collector);
        } else {
            this.history.remove(state.id);
        }
    }

    /** Merkt sich, wo er gerade ist und wohin er sich dreht (für die Spur seiner Klinge). */
    private ArrayDeque<double[]> remember(SchrolliRenderState state) {
        ArrayDeque<double[]> past = this.history.computeIfAbsent(state.id, id -> new ArrayDeque<>());
        double now = state.ageInTicks;
        if (!past.isEmpty() && past.peekLast()[0] > now) {
            past.clear();
        }
        if (past.isEmpty() || now - past.peekLast()[0] > 1.0e-3) {
            past.addLast(new double[] {now, state.bodyRot, state.x, state.y, state.z});
        }
        while (past.size() > 2 && now - past.peekFirst()[0] > TRAIL_STEPS * TRAIL_STEP + 1.0) {
            past.removeFirst();
        }
        // vergessene Schöpfer (nicht mehr zu sehen) räumen
        if (this.history.size() > 6) {
            this.history.values().removeIf(d -> d.isEmpty() || now - d.peekLast()[0] > 200.0 || d.peekLast()[0] - now > 200.0);
        }
        return past;
    }

    /** Wo er zur Zeit {@code time} war: {Drehung, x, y, z} (zwischen den gemerkten Augenblicken gemittelt). */
    private static double[] at(ArrayDeque<double[]> past, double time) {
        double[] before = past.peekFirst();
        double[] after = before;
        for (Iterator<double[]> it = past.iterator(); it.hasNext(); ) {
            double[] e = it.next();
            if (e[0] <= time) {
                before = e;
                after = e;
            } else {
                after = e;
                break;
            }
        }
        double span = after[0] - before[0];
        double k = span > 1.0e-6 ? Mth.clamp((time - before[0]) / span, 0.0, 1.0) : 0.0;
        double rot = before[1] + Mth.wrapDegrees(after[1] - before[1]) * k;
        return new double[] {rot, Mth.lerp(k, before[2], after[2]), Mth.lerp(k, before[3], after[3]), Mth.lerp(k, before[4], after[4])};
    }

    /**
     * Die Spur der Klinge: für die letzten Augenblicke wird seine Haltung noch einmal berechnet (mit der Drehung und dem
     * Ort von damals) und die Schneide zu einem Band verbunden – je schneller sie fuhr, desto heller; je älter, desto blasser.
     */
    private void submitTrail(SchrolliRenderState state, PoseStack poseStack, SubmitNodeCollector collector) {
        ArrayDeque<double[]> past = this.remember(state);
        SchrolliModel model = this.getModel();
        SchrolliRenderState p = this.probe;
        p.attack = state.attack;
        p.trueForm = state.trueForm;
        p.flags = state.flags;
        p.ascension = state.ascension;
        p.ground = state.ground;
        p.flash = state.flash;
        p.deathTime = 0.0f;
        p.yRot = state.yRot;
        p.xRot = state.xRot;
        p.walkAnimationPos = state.walkAnimationPos;
        p.walkAnimationSpeed = state.walkAnimationSpeed;
        p.scale = state.scale;
        double now = state.ageInTicks;
        Vector3f[] inner = new Vector3f[TRAIL_STEPS + 1];
        Vector3f[] outer = new Vector3f[TRAIL_STEPS + 1];
        Vector3f[] edge = new Vector3f[TRAIL_STEPS + 1];
        int count = 0;
        for (int i = 0; i <= TRAIL_STEPS; i++) {
            float back = i * TRAIL_STEP;
            if (state.attackTime - back < 0.0f && state.attack != 0) {
                break;
            }
            double[] then = at(past, now - back);
            p.attackTime = state.attackTime - back;
            p.ageInTicks = state.ageInTicks - back;
            p.wing = state.wing - back * 0.06f;
            model.setupAnim(p);
            if (!model.bladeVisible()) {
                break;
            }
            PoseStack local = new PoseStack();
            local.translate((float) (then[1] - state.x), (float) (then[2] - state.y), (float) (then[3] - state.z));
            local.scale(state.scale, state.scale, state.scale);
            local.rotateDegrees(Axis.YP, 180.0f - (float) then[0]);
            local.scale(-1.0f, -1.0f, 1.0f);
            local.scale(SCALE, SCALE, SCALE);
            local.translate(0.0f, -1.501f, 0.0f);
            model.toBlade(local);
            Matrix4f m = local.last().pose();
            inner[count] = m.transformPosition(new Vector3f(0.0f, -11.0f / 16.0f, 0.0f));
            outer[count] = m.transformPosition(new Vector3f(0.0f, -31.0f / 16.0f, 0.0f));
            edge[count] = m.transformPosition(new Vector3f(0.0f, -24.0f / 16.0f, 0.0f));
            count++;
        }
        model.setupAnim(state);
        if (count < 2) {
            return;
        }
        // wie schnell die Spitze fuhr (Blöcke je Tick) – nur rasche Hiebe ziehen eine Spur
        float[] glow = new float[count];
        boolean any = false;
        for (int i = 0; i < count - 1; i++) {
            float speed = outer[i].distance(outer[i + 1]) / TRAIL_STEP;
            float age = 1.0f - i / (float) (count - 1);
            glow[i] = Mth.clamp((speed - 0.6f) / 1.8f, 0.0f, 1.0f) * (float) Math.pow(age, 1.2);
            any |= glow[i] > 0.02f;
        }
        if (!any) {
            return;
        }
        boolean truly = state.trueForm;
        int soft = truly ? 0xA050FF : 0xFFA820;
        int core = truly ? 0xD8F8FF : 0xFFF4D0;
        final int n = count;
        collector.order(4).submitCustomGeometry(poseStack, RenderTypes.lightning(), (pose, buffer) -> {
            for (int i = 0; i < n - 1; i++) {
                float a = glow[i];
                float b = glow[Math.min(n - 2, i + 1)] * (i + 1 < n - 1 ? 1.0f : 0.0f);
                if (a <= 0.02f && b <= 0.02f) {
                    continue;
                }
                // ein weiches, breites Band (innen durchsichtig, zur Spitze hin leuchtend) …
                band(pose, buffer, inner[i], outer[i], outer[i + 1], inner[i + 1], ARGB.color(0, soft), ARGB.color(Math.round(190 * a), soft),
                        ARGB.color(Math.round(190 * b), soft), ARGB.color(0, soft));
                // … und eine gleißende Schneide
                band(pose, buffer, edge[i], outer[i], outer[i + 1], edge[i + 1], ARGB.color(Math.round(40 * a), core),
                        ARGB.color(Math.round(255 * a), core), ARGB.color(Math.round(255 * b), core), ARGB.color(Math.round(40 * b), core));
            }
        });
    }

    /** Ein Viereck mit Farben je Ecke, von beiden Seiten sichtbar. */
    private static void band(PoseStack.Pose pose, VertexConsumer buffer, Vector3f p0, Vector3f p1, Vector3f p2, Vector3f p3, int c0, int c1, int c2,
            int c3) {
        buffer.addVertex(pose, p0.x, p0.y, p0.z).setColor(c0);
        buffer.addVertex(pose, p1.x, p1.y, p1.z).setColor(c1);
        buffer.addVertex(pose, p2.x, p2.y, p2.z).setColor(c2);
        buffer.addVertex(pose, p3.x, p3.y, p3.z).setColor(c3);
        buffer.addVertex(pose, p3.x, p3.y, p3.z).setColor(c3);
        buffer.addVertex(pose, p2.x, p2.y, p2.z).setColor(c2);
        buffer.addVertex(pose, p1.x, p1.y, p1.z).setColor(c1);
        buffer.addVertex(pose, p0.x, p0.y, p0.z).setColor(c0);
    }

    @Override
    protected void scale(SchrolliRenderState state, PoseStack poseStack) {
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    /** Er kippt im Tod nicht zur Seite: er vergeht in Licht. */
    @Override
    protected float getFlipDegrees() {
        return 0.0f;
    }

    @Override
    protected int getBlockLightLevel(SchrolliEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    protected float getShadowStrength(SchrolliRenderState state) {
        return 0.4f;
    }
}
