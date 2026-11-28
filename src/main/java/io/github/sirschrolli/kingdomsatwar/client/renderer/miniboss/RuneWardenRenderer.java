package io.github.sirschrolli.kingdomsatwar.client.renderer.miniboss;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.renderer.golem.GolemRenderers;
import io.github.sirschrolli.kingdomsatwar.entity.miniboss.RuneWardenEntity;
import java.util.Map;
import net.minecraft.client.model.animal.golem.IronGolemModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.LivingEntityEmissiveLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.IronGolemRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Crackiness;

/**
 * Der Runenwächter: das Golemmodell der Mod in seinem eigenen Bild (dunkler Stein und Eisen, Messingbänder, eisblaue Runen),
 * 1,2-mal so groß. Solange er schläft, sind seine Runen erloschen; wach glimmen sie, im Runenwall gleißen sie. Eigene
 * Haltungen: beide Fäuste über dem Kopf (Runenbeben), die Fäuste im Boden, der starre Blick mit zurückgenommenen Armen
 * (Runenblick), der zeichnende Arm (Runenfallen) und die gekreuzten Arme (Runenwall). Je mehr er abbekommt, desto mehr
 * Risse zeigt er.
 */
public class RuneWardenRenderer extends MobRenderer<RuneWardenEntity, IronGolemRenderState, RuneWardenRenderer.Model> {
    private static final Identifier TEXTURE = KingdomsAtWar.id("textures/entity/iron_golem/runenwaechter.png");
    private static final Identifier GLOW = KingdomsAtWar.id("textures/entity/iron_golem/runenwaechter_glow.png");
    private static final Map<Crackiness.Level, Identifier> CRACKS = Map.of(
            Crackiness.Level.LOW, KingdomsAtWar.id("textures/entity/iron_golem/risse_low.png"),
            Crackiness.Level.MEDIUM, KingdomsAtWar.id("textures/entity/iron_golem/risse_medium.png"),
            Crackiness.Level.HIGH, KingdomsAtWar.id("textures/entity/iron_golem/risse_high.png"));

    public static class State extends IronGolemRenderState {
        public byte pose;
        public float amount;
        public boolean awake;
        public boolean enraged;
    }

    /** Das Golemmodell mit den Haltungen des Runenwächters (über Vanillas Gehen und Schlag gelegt). */
    public static final class Model extends IronGolemModel {
        private final ModelPart head;
        private final ModelPart rightArm;
        private final ModelPart leftArm;

        public Model(ModelPart root) {
            super(root);
            this.head = root.getChild("head");
            this.rightArm = root.getChild("right_arm");
            this.leftArm = root.getChild("left_arm");
        }

        @Override
        public void setupAnim(IronGolemRenderState state) {
            super.setupAnim(state);
            if (!(state instanceof State s)) {
                return;
            }
            if (!s.awake) {
                // eine Statue: die Arme hängen gerade, der Kopf blickt geradeaus
                this.head.xRot = 0.08f;
                this.head.yRot = 0.0f;
                this.rightArm.xRot = 0.0f;
                this.leftArm.xRot = 0.0f;
                return;
            }
            float a = s.amount;
            if (a <= 0.001f) {
                return;
            }
            switch (s.pose) {
                case RuneWardenEntity.POSE_RAISE -> {
                    this.rightArm.xRot = Mth.lerp(a, this.rightArm.xRot, -2.95f);
                    this.leftArm.xRot = Mth.lerp(a, this.leftArm.xRot, -2.95f);
                    this.head.xRot = Mth.lerp(a, this.head.xRot, -0.3f);
                }
                case RuneWardenEntity.POSE_SLAM -> {
                    this.rightArm.xRot = Mth.lerp(a, this.rightArm.xRot, -0.75f);
                    this.leftArm.xRot = Mth.lerp(a, this.leftArm.xRot, -0.75f);
                    this.head.xRot = Mth.lerp(a, this.head.xRot, 0.45f);
                }
                case RuneWardenEntity.POSE_GAZE -> {
                    this.rightArm.xRot = Mth.lerp(a, this.rightArm.xRot, 0.35f);
                    this.leftArm.xRot = Mth.lerp(a, this.leftArm.xRot, 0.35f);
                    this.rightArm.zRot = Mth.lerp(a, this.rightArm.zRot, 0.22f);
                    this.leftArm.zRot = Mth.lerp(a, this.leftArm.zRot, -0.22f);
                }
                case RuneWardenEntity.POSE_INSCRIBE -> {
                    this.rightArm.xRot = Mth.lerp(a, this.rightArm.xRot, -1.75f);
                    this.leftArm.xRot = Mth.lerp(a, this.leftArm.xRot, -0.35f);
                }
                case RuneWardenEntity.POSE_WARD -> {
                    this.rightArm.xRot = Mth.lerp(a, this.rightArm.xRot, -1.3f);
                    this.leftArm.xRot = Mth.lerp(a, this.leftArm.xRot, -1.3f);
                    this.rightArm.yRot = Mth.lerp(a, this.rightArm.yRot, -0.6f);
                    this.leftArm.yRot = Mth.lerp(a, this.leftArm.yRot, 0.6f);
                    this.head.xRot = Mth.lerp(a, this.head.xRot, 0.2f);
                }
                default -> { }
            }
        }
    }

    public RuneWardenRenderer(EntityRendererProvider.Context context) {
        super(context, new Model(context.bakeLayer(GolemRenderers.IRON_GOLEM)), 0.9f);
        this.addLayer(new Cracks(this));
        this.addLayer(new LivingEntityEmissiveLayer<>(this, state -> GLOW, (state, ageInTicks) -> {
            if (!(state instanceof State s) || !s.awake) {
                return 0.0f;
            }
            if (s.pose == RuneWardenEntity.POSE_WARD && s.amount > 0.5f) {
                return 1.0f;
            }
            float base = s.enraged ? 0.86f : 0.72f;
            return Mth.clamp(base + 0.14f * Mth.sin(ageInTicks * 0.21f) + 0.06f * Mth.sin(ageInTicks * 1.3f + 0.7f), 0.0f, 1.0f);
        }, new Model(context.bakeLayer(GolemRenderers.IRON_GOLEM)), RenderTypes::entityTranslucentEmissive, false));
    }

    @Override
    public IronGolemRenderState createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(RuneWardenEntity entity, IronGolemRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.attackTicksRemaining = entity.getAttackAnimationTick() > 0 ? entity.getAttackAnimationTick() - partialTicks : 0.0f;
        state.offerFlowerTick = 0;
        state.flowerBlock.clear();
        state.crackiness = Crackiness.GOLEM.byFraction(entity.getHealth() / entity.getMaxHealth());
        if (state instanceof State s) {
            s.pose = entity.shownPose();
            s.amount = entity.poseAmount(partialTicks);
            s.awake = entity.isAwake();
            s.enraged = entity.isEnraged();
            if (!s.awake) {
                state.walkAnimationSpeed = 0.0f;
            }
        }
    }

    @Override
    public Identifier getTextureLocation(IronGolemRenderState state) {
        return TEXTURE;
    }

    @Override
    protected void setupRotations(IronGolemRenderState state, PoseStack poseStack, float bodyRot, float entityScale) {
        super.setupRotations(state, poseStack, bodyRot, entityScale);
        if (!(state.walkAnimationSpeed < 0.01f)) {
            // das schwere Wanken des Golems beim Gehen
            float wp = state.walkAnimationPos + 6.0f;
            float triangleWave = (Math.abs(wp % 13.0f - 6.5f) - 3.25f) / 3.25f;
            poseStack.rotate(Axis.ZP.rotationDegrees(6.5f * triangleWave));
        }
    }

    /** Risse je nach Schaden – mit den Rissbildern des Golemmodells der Mod. */
    private static final class Cracks extends RenderLayer<IronGolemRenderState, Model> {
        Cracks(RenderLayerParent<IronGolemRenderState, Model> parent) {
            super(parent);
        }

        @Override
        public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, IronGolemRenderState state, float yRot, float xRot) {
            if (!state.isInvisible && state.crackiness != Crackiness.Level.NONE) {
                renderColoredCutoutModel(this.getParentModel(), CRACKS.get(state.crackiness), poseStack, collector, lightCoords, state, -1, 1);
            }
        }
    }
}
