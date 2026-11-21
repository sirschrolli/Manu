package io.github.sirschrolli.kingdomsatwar.client.renderer.creator;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import io.github.sirschrolli.kingdomsatwar.entity.creator.SeraphlingEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/** Ein Seraphling: goldenes Antlitz mit leuchtenden Augen, ein kreisender Heiligenschein, zwei Paar schwirrende Schwingen. */
public class SeraphlingRenderer extends MobRenderer<SeraphlingEntity, SeraphlingRenderer.State, SeraphlingRenderer.Model> {
    private static final Identifier TEXTURE = KingdomsAtWar.id("textures/entity/schrolli/seraphling.png");
    private static final RenderType GLOW = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/schrolli/seraphling_glow.png"));

    public static class State extends LivingEntityRenderState {
        public float flap;
    }

    public static class Model extends EntityModel<State> {
        private final ModelPart body;
        private final ModelPart head;
        private final ModelPart halo;
        private final ModelPart robeLow;
        private final ModelPart armL;
        private final ModelPart armR;
        private final ModelPart[][] wingsL = new ModelPart[2][2];
        private final ModelPart[][] wingsR = new ModelPart[2][2];

        public Model(ModelPart root) {
            super(root);
            this.body = root.getChild("body");
            this.head = this.body.getChild("head");
            this.halo = this.head.getChild("halo");
            this.robeLow = this.body.getChild("robe_low");
            this.armL = this.body.getChild("left_arm");
            this.armR = this.body.getChild("right_arm");
            String[] pos = {"upper", "lower"};
            for (int w = 0; w < 2; w++) {
                for (String side : new String[] {"left", "right"}) {
                    ModelPart a = this.body.getChild("wing_" + pos[w] + "_" + side);
                    ModelPart b = a.getChild("wing_" + pos[w] + "_" + side + "_tip");
                    ModelPart[] target = side.equals("left") ? this.wingsL[w] : this.wingsR[w];
                    target[0] = a;
                    target[1] = b;
                }
            }
        }

        @Override
        public void setupAnim(State state) {
            super.setupAnim(state);
            float t = state.ageInTicks;
            float f = state.flap;
            this.body.y += Mth.sin(t * 0.15f) * 0.8f;
            this.halo.yRot += t * 0.08f;
            this.halo.y += Mth.sin(t * 0.1f) * 0.3f;
            this.robeLow.xRot += Mth.sin(t * 0.2f) * 0.08f;
            this.armL.xRot += Mth.sin(t * 0.12f) * 0.12f;
            this.armR.xRot += Mth.sin(t * 0.12f + 1.0f) * 0.12f;
            this.head.yRot += Mth.clamp(state.yRot, -40.0f, 40.0f) * Anim.DEG * 0.7f;
            this.head.xRot += Mth.clamp(state.xRot, -30.0f, 30.0f) * Anim.DEG * 0.5f;
            for (int w = 0; w < 2; w++) {
                float s = Mth.sin(f - w * 0.6f) * (w == 0 ? 28.0f : 20.0f) * Anim.DEG;
                this.wingsL[w][0].zRot += s;
                this.wingsR[w][0].zRot -= s;
                float s2 = Mth.sin(f - w * 0.6f - 0.5f) * 14.0f * Anim.DEG;
                this.wingsL[w][1].zRot += s2;
                this.wingsR[w][1].zRot -= s2;
            }
        }
    }

    public SeraphlingRenderer(EntityRendererProvider.Context context) {
        super(context, new Model(context.bakeLayer(CreatorRenderers.SERAPHLING)), 0.35f);
        this.addLayer(new RenderLayer<>(this) {
            @Override
            public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, State state, float yRot, float xRot) {
                if (!state.isInvisible) {
                    collector.order(1).submitModel(this.getParentModel(), state, poseStack, GLOW, light, OverlayTexture.NO_OVERLAY, -1, null,
                            state.outlineColor);
                }
            }
        });
    }

    @Override
    public Identifier getTextureLocation(State state) {
        return TEXTURE;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(SeraphlingEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.flap = Mth.lerp(partialTicks, entity.flapO, entity.flap);
    }

    @Override
    protected int getBlockLightLevel(SeraphlingEntity entity, BlockPos pos) {
        return 15;
    }
}
