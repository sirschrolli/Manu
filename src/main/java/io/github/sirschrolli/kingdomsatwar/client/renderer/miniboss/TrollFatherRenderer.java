package io.github.sirschrolli.kingdomsatwar.client.renderer.miniboss;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.client.model.creature.CaveTrollModel;
import io.github.sirschrolli.kingdomsatwar.client.renderer.creature.CreatureRenderState;
import io.github.sirschrolli.kingdomsatwar.entity.miniboss.TrollFatherEntity;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * Grumbold, der Trollvater: das Trollmodell der Mod in seinem eigenen Bild (blaugrau wie alter Fels, Flechten, weißes Haar,
 * goldbeschlagene Keule), 1,45-mal so groß, mit glühenden Augen. Solange er schläft, sinkt er im Stehen in sich zusammen
 * (Kopf auf der Brust). Eigene Haltungen: der Brocken über dem Kopf, der Wurf, das Gebrüll (Kopf hoch, Arme weit) und der
 * Keulenwirbel (er dreht sich mit ausgestreckten Armen um sich selbst). Im Sonnenlicht wird er grau wie Stein.
 */
public class TrollFatherRenderer extends MobRenderer<TrollFatherEntity, CreatureRenderState, TrollFatherRenderer.Model> {
    private static final Identifier TEXTURE = KingdomsAtWar.id("textures/entity/cave_troll/trollvater.png");
    private static final RenderType GLOW = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/cave_troll/trollvater_glow.png"));

    public static class State extends CreatureRenderState {
        public byte pose;
        public float amount;
        public float spin;
        public boolean awake;
    }

    /** Das Trollmodell mit den Haltungen des Trollvaters (über Gang, Atmen und Keulenhieb gelegt). */
    public static final class Model extends CaveTrollModel {
        private final ModelPart body;
        private final ModelPart head;
        private final ModelPart leftArm;
        private final ModelPart rightArm;

        public Model(ModelPart root) {
            super(root);
            this.body = root.getChild("body");
            this.head = this.body.getChild("head");
            this.leftArm = this.body.getChild("left_arm");
            this.rightArm = this.body.getChild("right_arm");
        }

        @Override
        public void setupAnim(CreatureRenderState state) {
            super.setupAnim(state);
            if (!(state instanceof State s)) {
                return;
            }
            if (!s.awake) {
                // er döst im Stehen: in sich zusammengesunken, der Kopf auf der Brust
                this.body.xRot += 0.28f;
                this.head.xRot += 0.5f;
                this.leftArm.xRot -= 0.18f;
                this.rightArm.xRot -= 0.18f;
                return;
            }
            float a = s.amount;
            if (a <= 0.001f) {
                return;
            }
            switch (s.pose) {
                case TrollFatherEntity.POSE_LIFT -> {
                    this.rightArm.xRot = Mth.lerp(a, this.rightArm.xRot, -2.9f);
                    this.leftArm.xRot = Mth.lerp(a, this.leftArm.xRot, -2.9f);
                    this.rightArm.zRot = Mth.lerp(a, this.rightArm.zRot, -0.22f);
                    this.leftArm.zRot = Mth.lerp(a, this.leftArm.zRot, 0.22f);
                    this.body.xRot -= 0.12f * a;
                    this.head.xRot -= 0.25f * a;
                }
                case TrollFatherEntity.POSE_THROW -> {
                    this.rightArm.xRot = Mth.lerp(a, this.rightArm.xRot, -1.1f);
                    this.leftArm.xRot = Mth.lerp(a, this.leftArm.xRot, -1.1f);
                    this.body.xRot += 0.28f * a;
                }
                case TrollFatherEntity.POSE_ROAR -> {
                    this.head.xRot -= 0.7f * a;
                    this.rightArm.zRot += 0.95f * a;
                    this.leftArm.zRot -= 0.95f * a;
                    this.rightArm.xRot -= 0.35f * a;
                    this.leftArm.xRot -= 0.35f * a;
                    this.body.xRot -= 0.16f * a;
                }
                case TrollFatherEntity.POSE_SPIN -> {
                    this.rightArm.zRot = Mth.lerp(a, this.rightArm.zRot, 1.35f);
                    this.leftArm.zRot = Mth.lerp(a, this.leftArm.zRot, -1.35f);
                    this.rightArm.xRot = Mth.lerp(a, this.rightArm.xRot, -0.2f);
                    this.leftArm.xRot = Mth.lerp(a, this.leftArm.xRot, -0.2f);
                }
                default -> { }
            }
        }
    }

    public TrollFatherRenderer(EntityRendererProvider.Context context) {
        super(context, new Model(context.bakeLayer(ModModelLayers.CAVE_TROLL)), 1.0f);
        this.addLayer(new EyesLayer<>(this) {
            @Override
            public RenderType renderType() {
                return GLOW;
            }
        });
    }

    @Override
    public Identifier getTextureLocation(CreatureRenderState state) {
        return TEXTURE;
    }

    @Override
    protected int getModelTint(CreatureRenderState state) {
        return state.petrifying ? 0xFFB8BCC0 : -1;
    }

    @Override
    public CreatureRenderState createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(TrollFatherEntity entity, CreatureRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.petrifying = entity.isPetrifying();
        state.attack.copyFrom(entity.smashAnimation);
        if (state instanceof State s) {
            s.pose = entity.shownPose();
            s.amount = entity.poseAmount(partialTicks);
            s.spin = entity.spin(partialTicks);
            s.awake = entity.isAwake();
        }
    }

    @Override
    protected void setupRotations(CreatureRenderState state, PoseStack poseStack, float bodyRot, float scale) {
        super.setupRotations(state, poseStack, bodyRot, scale);
        if (state instanceof State s && s.spin != 0.0f) {
            poseStack.rotate(Axis.YP.rotationDegrees(-s.spin));
        }
    }
}
