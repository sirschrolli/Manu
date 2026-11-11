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
 *   <li><b>Schwingen:</b> zwei Flügel aus einzelnen Federn (seit 0.78.2; Aufbau in {@code tools/creator_wings.py}): an
 *   Oberarm, Unterarm und Hand sitzen Schirm- und Armschwingen, darüber große und kleine Deckfedern, darunter
 *   Unterflügeldecken, an der Hand sieben Handschwingen mit goldenen Spitzen, die sich je nach Haltung auffächern. Im Stehen
 *   sind sie halb geöffnet wie bei einem Engel (die Bögen über den Schultern, die Schwungfedern bis zur Hüfte) – so sieht
 *   man von vorn beide, auch wenn der Leib gedreht ist –, sie atmen sacht, streichen im Laufen nach hinten und wippen mit
 *   den Schritten; beim Schleichen falten sie sich hinter dem Rücken zusammen; im Gleitflug breiten sie sich aus (über
 *   vier Blöcke Spannweite), schlagen in langsamen Wellen und beim Schwingenschlag einmal kräftig. Jede Feder zeigt von
 *   vorn ihre Unterseite und von hinten ihre Oberseite (gezeichnet wird immer nur die zugewandte – nichts flimmert).</li>
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
    private static final float[][] REST = {{2, -16, -40}, {0, 6, 52}, {0, 4, 36}};
    private static final float[][] TUCK = {{4, -44, -40}, {0, 120, 30}, {0, -128, 8}};
    private static final float[][] SPREAD = {{-24, -12, 8}, {-6, -6, 2}, {-4, 0, 14}};
    /** Wie weit die Handschwingen aufgefächert sind (Anteil ihres Winkels im Gleitflug): im Stehen und zusammengefaltet. */
    private static final float FAN_REST = 0.62f;
    private static final float FAN_TUCK = 0.15f;
    /** Neigung jeder Feder um ihre Längsachse (Grad): Nachbarn überlappen wie Dachschindeln und durchdringen sich nie. */
    private static final float TILT = -8.0f;
    private static final float[] BONE_LENGTH = {7.0f, 6.0f, 4.0f};
    private static final String[] BONES = {"inner", "mid", "tip"};
    /** Die Arme der Glieder: Länge, Höhe, Tiefe, u, v, y von, z von (weich befiedert, von Deckfedern fast verdeckt). */
    private static final float[][] ARMS = {{7, 2, 2, 0, 20, -1.0f, 0.1f}, {6, 2, 2, 0, 24, -1.0f, 0.1f}, {4, 1, 1, 0, 28, -0.5f, 0.6f}};
    // --- aus tools/creator_wings.py (dort ändern und neu ausgeben) ---
    /** Arten der Federn: Breite, Länge, u, v. */
    private static final float[][] KINDS = {
            {3, 16, 0, 0}, {3, 14, 6, 0}, {3, 12, 12, 0}, {3, 12, 18, 0}, {3, 10, 24, 0}, {3, 7, 30, 0}, {2, 4, 36, 0}
    };
    /** Feste Federn: Glied, x entlang des Knochens, y, z, Art, Drehung z, y, x (Grad, linker Flügel). */
    private static final float[][] FIXED = {
            {0, 0.3f, 0.2f, 1.3f, 4, 46f, -8f, 0f}, {0, 0.9f, 0.2f, 1.25f, 4, 41f, -8f, 0f},
            {0, 1.6f, 0.2f, 1.2f, 4, 36f, -8f, 0f}, {0, 2.8f, 0.2f, 1.15f, 4, 32f, -8f, 0f}, {0, 4f, 0.2f, 1.1f, 3, 28f, -8f, 0f},
            {0, 5.2f, 0.2f, 1.05f, 3, 24f, -8f, 0f}, {0, 6.4f, 0.2f, 1f, 3, 20f, -8f, 0f}, {1, 0.4f, 0.2f, 0.95f, 3, 4f, -8f, 0f},
            {1, 1.4f, 0.2f, 0.9f, 3, 1.6f, -8f, 0f}, {1, 2.4f, 0.2f, 0.85f, 3, -0.8f, -8f, 0f},
            {1, 3.4f, 0.2f, 0.8f, 3, -3.2f, -8f, 0f}, {1, 4.4f, 0.2f, 0.75f, 3, -5.6f, -8f, 0f},
            {1, 5.4f, 0.2f, 0.7f, 3, -8f, -8f, 0f}, {0, 0.9f, -0.3f, 2.42f, 5, 30f, -8f, -9f},
            {0, 2.2f, -0.3f, 2.38f, 5, 26f, -8f, -9f}, {0, 3.5f, -0.3f, 2.34f, 5, 22f, -8f, -9f},
            {0, 4.8f, -0.3f, 2.3f, 5, 18f, -8f, -9f}, {0, 6.1f, -0.3f, 2.26f, 5, 14f, -8f, -9f},
            {1, 0.4f, -0.3f, 2.42f, 5, 3f, -8f, -9f}, {1, 1.4f, -0.3f, 2.38f, 5, 1f, -8f, -9f},
            {1, 2.4f, -0.3f, 2.34f, 5, -1f, -8f, -9f}, {1, 3.4f, -0.3f, 2.3f, 5, -3f, -8f, -9f},
            {1, 4.4f, -0.3f, 2.26f, 5, -5f, -8f, -9f}, {1, 5.4f, -0.3f, 2.22f, 5, -7f, -8f, -9f},
            {2, 0.3f, -0.2f, 1.9f, 5, -8f, -8f, -8f}, {2, 1.4f, -0.2f, 1.86f, 5, -18f, -8f, -8f},
            {2, 2.5f, -0.2f, 1.82f, 5, -28f, -8f, -8f}, {2, 3.6f, -0.2f, 1.78f, 5, -38f, -8f, -8f},
            {0, 0.7f, -1f, 2.62f, 6, 26f, -6f, -7f}, {0, 2.1f, -1f, 2.59f, 6, 22f, -6f, -7f},
            {0, 3.5f, -1f, 2.56f, 6, 18f, -6f, -7f}, {0, 4.9f, -1f, 2.53f, 6, 14f, -6f, -7f},
            {0, 6.3f, -1f, 2.5f, 6, 10f, -6f, -7f}, {1, 0.5f, -1f, 2.62f, 6, 2f, -6f, -7f},
            {1, 1.75f, -1f, 2.59f, 6, 0f, -6f, -7f}, {1, 3f, -1f, 2.56f, 6, -2f, -6f, -7f},
            {1, 4.25f, -1f, 2.53f, 6, -4f, -6f, -7f}, {1, 5.5f, -1f, 2.5f, 6, -6f, -6f, -7f},
            {2, 0.5f, -0.6f, 2.06f, 6, -10f, -6f, -6f}, {2, 1.9f, -0.6f, 2.03f, 6, -20f, -6f, -6f},
            {2, 3.3f, -0.6f, 2f, 6, -30f, -6f, -6f}, {0, 1.2f, -0.2f, -0.02f, 5, 28f, -8f, 9f},
            {0, 2.7f, -0.2f, -0.06f, 5, 23f, -8f, 9f}, {0, 4.2f, -0.2f, -0.1f, 5, 18f, -8f, 9f},
            {0, 5.7f, -0.2f, -0.14f, 5, 13f, -8f, 9f}, {1, 0.6f, -0.2f, -0.02f, 5, 2f, -8f, 9f},
            {1, 1.8f, -0.2f, -0.06f, 5, 0f, -8f, 9f}, {1, 3f, -0.2f, -0.1f, 5, -2f, -8f, 9f},
            {1, 4.2f, -0.2f, -0.14f, 5, -4f, -8f, 9f}, {1, 5.4f, -0.2f, -0.18f, 5, -6f, -8f, 9f},
            {2, 0.4f, -0.1f, 0.48f, 5, -8f, -8f, 8f}, {2, 1.8f, -0.1f, 0.44f, 5, -20f, -8f, 8f},
            {2, 3.2f, -0.1f, 0.4f, 5, -32f, -8f, 8f}, {2, 0.2f, -0.4f, 1.72f, 5, -72f, -8f, 0f}
    };
    /** Handschwingen: x entlang der Hand, z, Art, Winkel im Gleitflug (Grad; negativ = zur Flügelspitze hin). */
    private static final float[][] PRIMARIES = {
            {0.1f, 1.05f, 1, -10f}, {0.75f, 1f, 0, -21f}, {1.4f, 0.95f, 0, -32f}, {2.05f, 0.9f, 0, -44f}, {2.7f, 0.85f, 0, -56f}, {3.35f, 0.8f, 1, -68f}, {3.9f, 0.75f, 2, -80f}
    };
    private static final float HALO_RADIUS = 4.6f;
    private static final float WHEEL_RADIUS = 9.5f;

    private final ModelPart[] wingL = new ModelPart[3];
    private final ModelPart[] wingR = new ModelPart[3];
    private final ModelPart[] primariesL = new ModelPart[PRIMARIES.length];
    private final ModelPart[] primariesR = new ModelPart[PRIMARIES.length];
    private final ModelPart halo;
    private final ModelPart wheelA;
    private final ModelPart wheelB;

    public CreatorGearLayer(RenderLayerParent<S, M> parent, ModelPart root) {
        super(parent);
        for (int i = 0; i < 3; i++) {
            this.wingL[i] = root.getChild("wing_left_" + BONES[i]);
            this.wingR[i] = root.getChild("wing_right_" + BONES[i]);
        }
        for (int i = 0; i < PRIMARIES.length; i++) {
            this.primariesL[i] = root.getChild("wing_left_p" + i);
            this.primariesR[i] = root.getChild("wing_right_p" + i);
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
            float sign = left ? 1.0f : -1.0f;
            String name = left ? "wing_left_" : "wing_right_";
            for (int b = 0; b < 3; b++) {
                float[] arm = ARMS[b];
                PartDefinition bone = root.addOrReplaceChild(name + BONES[b], CubeListBuilder.create().texOffs((int) arm[3], (int) arm[4])
                        .mirror(!left).addBox(left ? 0.0f : -arm[0], arm[5], arm[6], arm[0], arm[1], arm[2],
                                new net.minecraft.client.model.geom.builders.CubeDeformation(b == 2 ? -0.15f : -0.32f)), PartPose.ZERO);
                int n = 0;
                for (float[] f : FIXED) {
                    if ((int) f[0] == b) {
                        bone.addOrReplaceChild("f" + n++, feather(left, (int) f[4]),
                                PartPose.offsetAndRotation(sign * f[1], f[2], f[3], f[7] * D, sign * f[6] * D, sign * f[5] * D));
                    }
                }
            }
            for (int i = 0; i < PRIMARIES.length; i++) {
                root.addOrReplaceChild(name + "p" + i, feather(left, (int) PRIMARIES[i][2]), PartPose.ZERO);
            }
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
     * Eine einzelne Feder: eine Fläche ohne Tiefe, die Wurzel am Ursprung, die Spitze bei +y. Ihre Vorderseite (nach -z, die
     * Unterseite des Flügels) kommt aus (u, v) der Textur, ihre Rückseite (die Oberseite) aus dem Feld gleich daneben.
     */
    private static CubeListBuilder feather(boolean left, int kind) {
        float[] k = KINDS[kind];
        return CubeListBuilder.create().texOffs((int) k[2], (int) k[3]).mirror(!left).addBox(-k[0] * 0.5f, 0.0f, 0.0f, k[0], k[1], 0.0f);
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
            float walk = Math.min(1.0f, state.walkAnimationSpeed) * (1.0f - spread) * (1.0f - tuck);
            float step = Mth.cos(state.walkAnimationPos * 0.6662f) * walk;
            this.wing(poseStack, collector, light, model, this.wingL, this.primariesL, 1.0f, spread, tuck, t, flap, walk, step);
            this.wing(poseStack, collector, light, model, this.wingR, this.primariesR, -1.0f, spread, tuck, t, flap, walk, -step);
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

    /**
     * Ein Flügel: Lage aus Ruhe, Schleichen und Ausbreiten gemischt, dazu Atmen, Laufen ({@code walk}: wie schnell, {@code step}:
     * der Takt der Schritte), Schlagen im Flug und der Schwingenschlag.
     */
    private void wing(PoseStack poseStack, SubmitNodeCollector collector, int light, M model, ModelPart[] parts, ModelPart[] primaries,
            float sign, float spread, float tuck, float t, Float flap, float walk, float step) {
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
        // im Laufen streichen sie nach hinten und wippen mit den Schritten
        pose[0][1] -= walk * 9.0f;
        pose[0][2] += walk * 4.0f + step * 2.5f;
        pose[1][2] += walk * 5.0f + step * 3.5f;
        pose[2][2] += step * 3.0f;
        float wave = Mth.sin(t * 0.32f) * spread;
        pose[0][1] += wave * 14.0f;
        pose[1][1] += Mth.sin(t * 0.32f - 0.7f) * spread * 8.0f;
        pose[2][1] += Mth.sin(t * 0.32f - 1.3f) * spread * 7.0f;
        // die Handschwingen fächern sich auf: zusammengefaltet kaum, im Stehen halb, im Gleitflug ganz – und sie atmen mit
        float fan = Mth.lerp(spread, Mth.lerp(tuck, FAN_REST, FAN_TUCK), 1.0f) + breathe * 0.03f + Math.abs(step) * 0.05f;
        // der Schwingenschlag: einmal weit hinauf und kräftig hinab (im Abschlag spreizen sich die Handschwingen)
        if (flap != null && flap < 1.0f) {
            float k = Mth.sin(flap * Mth.TWO_PI) * (1.0f - flap);
            pose[0][1] += -k * 38.0f * Math.max(spread, 0.4f);
            pose[1][1] += -k * 18.0f;
            pose[2][1] += -k * 14.0f;
            fan += Math.max(0.0f, -k) * 0.12f;
        }
        poseStack.pushPose();
        model.root().translateAndRotate(poseStack);
        model.body.translateAndRotate(poseStack);
        poseStack.translate(sign * 1.6f / 16.0f, 0.6f / 16.0f, 2.3f / 16.0f);
        // mächtige Schwingen: größer als das Modell (die Federn reichen in Ruhe bis über die Hüften)
        poseStack.scale(WING_SCALE, WING_SCALE, WING_SCALE);
        for (int b = 0; b < 3; b++) {
            if (b > 0) {
                poseStack.translate(sign * BONE_LENGTH[b - 1] / 16.0f, 0.0f, 0.0f);
            }
            poseStack.rotate(new Quaternionf().rotationZYX(sign * pose[b][2] * D, sign * pose[b][1] * D, pose[b][0] * D));
            this.submitFeathers(collector, poseStack, parts[b], light);
        }
        // die Handschwingen an der Hand, jede mit ihrem Fächerwinkel
        for (int i = 0; i < primaries.length; i++) {
            float[] p = PRIMARIES[i];
            poseStack.pushPose();
            poseStack.translate(sign * p[0] / 16.0f, 0.0f, p[1] / 16.0f);
            poseStack.rotate(new Quaternionf().rotationZYX(sign * p[3] * fan * D, sign * TILT * D, 0.0f));
            this.submitFeathers(collector, poseStack, primaries[i], light);
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    /** Heiligenschein und Räder: von allen Seiten gleich (ohne Rückseiten-Auslese). */
    private void submit(SubmitNodeCollector collector, PoseStack poseStack, ModelPart part, int light) {
        RenderType cutout = RenderTypes.entityCutout(TEXTURE);
        collector.submitModelPart(part, poseStack, cutout, light, OverlayTexture.NO_OVERLAY, null);
        collector.submitModelPart(part, poseStack, RenderTypes.eyes(GLOW), light, OverlayTexture.NO_OVERLAY, null);
    }

    /**
     * Federn: Von jeder Fläche wird nur die Seite gezeichnet, die zum Betrachter zeigt – vorn die Unterseite, hinten die
     * Oberseite. (Ohne diese Auslese lägen beide Seiten deckungsgleich übereinander und flimmerten.)
     */
    private void submitFeathers(SubmitNodeCollector collector, PoseStack poseStack, ModelPart part, int light) {
        collector.submitModelPart(part, poseStack, RenderTypes.entityCutoutCull(TEXTURE), light, OverlayTexture.NO_OVERLAY, null);
        collector.submitModelPart(part, poseStack, RenderTypes.eyes(GLOW), light, OverlayTexture.NO_OVERLAY, null);
    }
}
