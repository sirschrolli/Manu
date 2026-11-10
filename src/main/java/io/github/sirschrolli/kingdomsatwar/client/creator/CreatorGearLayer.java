package io.github.sirschrolli.kingdomsatwar.client.creator;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.CreatorBlocks;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.context.ContextKey;
import org.joml.Quaternionf;

/**
 * Die Ausrüstung des Schöpfers am Träger (seit 0.78), über der gewöhnlichen Rüstung (Spieler und Rüstungsständer):
 * <ul>
 *   <li><b>Schwingen:</b> zwei Flügel aus Federn – je ein Oberarm, Unterarm und eine Hand mit Arm- und Handschwingen und
 *   darüber Deckfedern, weiß mit goldenen Spitzen. Im Stehen liegen sie angelegt auf dem Rücken und atmen sacht, beim
 *   Schleichen enger; im Gleitflug breiten sie sich aus (gut drei Blöcke Spannweite), schlagen in langsamen Wellen und beim
 *   Schwingenschlag einmal kräftig.</li>
 *   <li><b>Heiligenschein:</b> ein Ring aus goldenem Licht über dem Kopf, leicht nach hinten geneigt, mit vier Steinen und
 *   einem inneren Lichtring; er dreht sich langsam und schwebt sacht auf und ab.</li>
 *   <li><b>Räder:</b> zwei goldene Ringe voller Augen um die Hüften, schräg zueinander, die sich gegenläufig drehen.</li>
 * </ul>
 * Alles Gold und die Augen leuchten (eigene Leuchtebene). Jedes Teil wird mit seiner eigenen Lage eingereicht (die Teile
 * selbst bleiben unbewegt), darum sieht jeder Träger anders aus, auch wenn alle dasselbe Modell teilen.
 */
public class CreatorGearLayer<S extends HumanoidRenderState, M extends HumanoidModel<S>> extends RenderLayer<S, M> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(KingdomsAtWar.id("creator_gear"), "main");
    private static final Identifier TEXTURE = KingdomsAtWar.id("textures/entity/equipment_parts/creator_gear.png");
    private static final Identifier GLOW = KingdomsAtWar.id("textures/entity/equipment_parts/creator_gear_glow.png");
    /** Wie weit der letzte Schwingenschlag ist (0 – 1; nur am eigenen Spieler, sonst fehlt der Wert). */
    public static final ContextKey<Float> FLAP = new ContextKey<>(KingdomsAtWar.id("creator_wing_flap"));

    private static final float D = Mth.DEG_TO_RAD;
    /** Die Schwingen sind größer als ihr Modell gezeichnet (mächtig, wie echte Flügel). */
    private static final float WING_SCALE = 1.3f;
    /**
     * Haltungen des linken Flügels in Grad {Oberarm, Unterarm, Hand} × {x, y, z}; der rechte gespiegelt (y und z umgekehrt).
     * Im Gleitflug hängen die Federn ein wenig unter die Knochen (so sieht man die Schwingen auch von hinten), die Hand hebt
     * sich wie beim Adler.
     */
    private static final float[][] REST = {{8, -60, -32}, {0, 142, 24}, {0, -148, 6}};
    private static final float[][] TUCK = {{14, -80, -28}, {0, 162, 20}, {0, -166, 6}};
    private static final float[][] SPREAD = {{-24, -12, 8}, {-6, -6, 2}, {-4, 0, 14}};
    private static final float INNER = 7.0f;
    private static final float MID = 6.0f;
    private static final float HALO_RADIUS = 4.6f;
    private static final float WHEEL_RADIUS = 9.5f;

    private final ModelPart[] wingL = new ModelPart[3];
    private final ModelPart[] wingR = new ModelPart[3];
    private final ModelPart halo;
    private final ModelPart wheelA;
    private final ModelPart wheelB;

    public CreatorGearLayer(RenderLayerParent<S, M> parent, ModelPart root) {
        super(parent);
        String[] names = {"inner", "mid", "tip"};
        for (int i = 0; i < 3; i++) {
            this.wingL[i] = root.getChild("wing_left_" + names[i]);
            this.wingR[i] = root.getChild("wing_right_" + names[i]);
        }
        this.halo = root.getChild("halo");
        this.wheelA = root.getChild("wheel_a");
        this.wheelB = root.getChild("wheel_b");
    }

    /** Texturlage 128×64 (gemalt 512×256), siehe tools/creator_gear.py. */
    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        for (int side = 0; side < 2; side++) {
            boolean left = side == 0;
            String name = left ? "wing_left_" : "wing_right_";
            root.addOrReplaceChild(name + "inner", wingPart(left, INNER, 1.5f, 1.0f, 36, 0, 0, 0, 7, 10, 20, 0, 7, 5), PartPose.ZERO);
            root.addOrReplaceChild(name + "mid", wingPart(left, MID, 1.5f, 1.0f, 36, 3, 0, 11, 6, 11, 20, 6, 6, 6), PartPose.ZERO);
            root.addOrReplaceChild(name + "tip", wingPart(left, 4.0f, 1.0f, 1.0f, 36, 6, 0, 22, 10, 17, 20, 13, 6, 7), PartPose.ZERO);
        }
        // der Heiligenschein: sechzehn Glieder im Kreis, vier Steine, ein innerer Lichtring
        PartDefinition halo = root.addOrReplaceChild("halo", CubeListBuilder.create(), PartPose.ZERO);
        for (int k = 0; k < 16; k++) {
            float a = k * Mth.TWO_PI / 16.0f;
            halo.addOrReplaceChild("seg" + k, CubeListBuilder.create().texOffs(64, 0).addBox(-1.0f, -0.5f, -0.5f, 2.0f, 1.0f, 1.0f),
                    PartPose.offsetAndRotation(Mth.cos(a) * HALO_RADIUS, 0.0f, Mth.sin(a) * HALO_RADIUS, 0.0f, -(a + Mth.HALF_PI), 0.0f));
            float r = HALO_RADIUS - 1.05f;
            halo.addOrReplaceChild("light" + k, CubeListBuilder.create().texOffs(78, 0).addBox(-0.75f, -0.25f, -0.25f, 1.5f, 0.5f, 0.5f),
                    PartPose.offsetAndRotation(Mth.cos(a) * r, 0.0f, Mth.sin(a) * r, 0.0f, -(a + Mth.HALF_PI), 0.0f));
            if (k % 4 == 0) {
                halo.addOrReplaceChild("gem" + k, CubeListBuilder.create().texOffs(72, 0).addBox(-0.5f, -1.15f, -0.5f, 1.0f, 1.0f, 1.0f),
                        PartPose.offsetAndRotation(Mth.cos(a) * HALO_RADIUS, 0.0f, Mth.sin(a) * HALO_RADIUS, 0.0f, -(a + Mth.HALF_PI), 0.0f));
            }
        }
        wheel(root, "wheel_a");
        wheel(root, "wheel_b");
        return LayerDefinition.create(mesh, 128, 64);
    }

    /**
     * Ein Glied eines Flügels (in seiner eigenen Lage, entlang +x bzw. beim rechten -x): der Knochen als Vorderkante, darunter
     * die Federfläche, auf der Oberseite (hinten, +z) die Deckfedern.
     */
    private static CubeListBuilder wingPart(boolean left, float length, float thick, float depth, int boneU, int boneV,
            int planeU, int planeV, float planeW, float planeH, int covU, int covV, float covW, float covH) {
        CubeListBuilder b = CubeListBuilder.create();
        float bx = left ? 0.0f : -length;
        b.texOffs(boneU, boneV).mirror(!left).addBox(bx, -thick * 0.5f, -depth * 0.5f, length, thick, depth);
        float px = left ? 0.0f : -planeW;
        b.texOffs(planeU, planeV).mirror(!left).addBox(px, 0.0f, 0.0f, planeW, planeH, 0.0f);
        float cx = left ? 0.0f : -covW;
        b.texOffs(covU, covV).mirror(!left).addBox(cx, -0.4f, 0.32f, covW, covH, 0.0f);
        return b;
    }

    /** Ein Rad: vierundzwanzig Glieder im Kreis, abwechselnd mit Auge, mit Rune und schlicht. */
    private static void wheel(PartDefinition root, String name) {
        PartDefinition wheel = root.addOrReplaceChild(name, CubeListBuilder.create(), PartPose.ZERO);
        for (int k = 0; k < 24; k++) {
            float a = k * Mth.TWO_PI / 24.0f;
            int u = k % 3 == 0 ? 72 : k % 3 == 1 ? 64 : 80;
            wheel.addOrReplaceChild("seg" + k, CubeListBuilder.create().texOffs(u, 4).addBox(-1.25f, -0.75f, -0.75f, 2.5f, 1.5f, 1.5f),
                    PartPose.offsetAndRotation(Mth.cos(a) * WHEEL_RADIUS, 0.0f, Mth.sin(a) * WHEEL_RADIUS, 0.0f, -(a + Mth.HALF_PI), 0.0f));
        }
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, S state, float yRot, float xRot) {
        M model = this.getParentModel();
        float t = state.ageInTicks;
        if (state.chestEquipment.is(CreatorBlocks.CREATOR_WINGS.get())) {
            float spread = Mth.clamp((-state.elytraRotZ - 0.2618f) / (Mth.HALF_PI - 0.2618f), 0.0f, 1.0f);
            if (!state.isFallFlying) {
                spread = 0.0f;
            }
            spread = spread * spread * (3.0f - 2.0f * spread);
            float tuck = state.isCrouching && !state.isFallFlying ? 1.0f : 0.0f;
            Float flap = state.getRenderData(FLAP);
            this.wing(poseStack, collector, light, model, this.wingL, 1.0f, spread, tuck, t, flap);
            this.wing(poseStack, collector, light, model, this.wingR, -1.0f, spread, tuck, t, flap);
        }
        if (state.headEquipment.is(CreatorBlocks.CREATOR_HALO.get())) {
            poseStack.pushPose();
            model.root().translateAndRotate(poseStack);
            model.head.translateAndRotate(poseStack);
            poseStack.translate(0.0f, (-11.2f + Mth.sin(t * 0.08f) * 0.35f) / 16.0f, 0.9f / 16.0f);
            poseStack.rotate(new Quaternionf().rotationZYX(0.0f, 0.0f, -0.32f));
            poseStack.rotate(new Quaternionf().rotationY(t * 0.02f));
            this.submit(collector, poseStack, this.halo, light);
            poseStack.popPose();
        }
        if (state.legsEquipment.is(CreatorBlocks.CREATOR_RINGS.get())) {
            for (int i = 0; i < 2; i++) {
                poseStack.pushPose();
                model.root().translateAndRotate(poseStack);
                model.body.translateAndRotate(poseStack);
                poseStack.translate(0.0f, (10.6f + Mth.sin(t * 0.06f + i * 2.1f) * 0.4f) / 16.0f, 0.0f);
                float tilt = (i == 0 ? 0.42f : -0.42f) + Mth.sin(t * 0.03f + i) * 0.06f;
                poseStack.rotate(new Quaternionf().rotationZYX(i == 0 ? tilt * 0.35f : tilt, (i == 0 ? 0.0f : 1.3f), i == 0 ? tilt : tilt * 0.35f));
                poseStack.rotate(new Quaternionf().rotationY((i == 0 ? 1.0f : -1.3f) * t * 0.045f));
                this.submit(collector, poseStack, i == 0 ? this.wheelA : this.wheelB, light);
                poseStack.popPose();
            }
        }
    }

    /** Ein Flügel: Lage aus Ruhe, Schleichen und Ausbreiten gemischt, dazu Atmen, Schlagen im Flug und der Schwingenschlag. */
    private void wing(PoseStack poseStack, SubmitNodeCollector collector, int light, M model, ModelPart[] parts, float sign, float spread,
            float tuck, float t, Float flap) {
        float[][] pose = new float[3][3];
        for (int b = 0; b < 3; b++) {
            for (int a = 0; a < 3; a++) {
                float rest = Mth.lerp(tuck, REST[b][a], TUCK[b][a]);
                pose[b][a] = Mth.lerp(spread, rest, SPREAD[b][a]);
            }
        }
        // Atmen in Ruhe, langsame Wellen im Flug
        float breathe = Mth.sin(t * 0.07f) * (1.0f - spread);
        pose[0][1] += breathe * 1.5f;
        pose[1][2] += breathe * 2.0f;
        float wave = Mth.sin(t * 0.32f) * spread;
        pose[0][1] += wave * 14.0f;
        pose[1][1] += Mth.sin(t * 0.32f - 0.7f) * spread * 8.0f;
        pose[2][1] += Mth.sin(t * 0.32f - 1.3f) * spread * 7.0f;
        // der Schwingenschlag: einmal weit hinauf und kräftig hinab
        if (flap != null && flap < 1.0f) {
            float k = Mth.sin(flap * Mth.TWO_PI) * (1.0f - flap);
            pose[0][1] += -k * 38.0f * Math.max(spread, 0.4f);
            pose[1][1] += -k * 18.0f;
            pose[2][1] += -k * 14.0f;
        }
        poseStack.pushPose();
        model.root().translateAndRotate(poseStack);
        model.body.translateAndRotate(poseStack);
        poseStack.translate(sign * 1.6f / 16.0f, 0.6f / 16.0f, 2.3f / 16.0f);
        // mächtige Schwingen: größer als das Modell (die Federn reichen in Ruhe bis über die Hüften)
        poseStack.scale(WING_SCALE, WING_SCALE, WING_SCALE);
        float[] lengths = {INNER, MID};
        for (int b = 0; b < 3; b++) {
            if (b > 0) {
                poseStack.translate(sign * lengths[b - 1] / 16.0f, 0.0f, 0.0f);
            }
            poseStack.rotate(new Quaternionf().rotationZYX(sign * pose[b][2] * D, sign * pose[b][1] * D, pose[b][0] * D));
            this.submit(collector, poseStack, parts[b], light);
        }
        poseStack.popPose();
    }

    private void submit(SubmitNodeCollector collector, PoseStack poseStack, ModelPart part, int light) {
        RenderType cutout = RenderTypes.entityCutout(TEXTURE);
        collector.submitModelPart(part, poseStack, cutout, light, OverlayTexture.NO_OVERLAY, null);
        collector.submitModelPart(part, poseStack, RenderTypes.eyes(GLOW), light, OverlayTexture.NO_OVERLAY, null);
    }
}
