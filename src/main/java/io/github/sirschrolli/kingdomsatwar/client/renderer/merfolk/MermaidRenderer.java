package io.github.sirschrolli.kingdomsatwar.client.renderer.merfolk;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import io.github.sirschrolli.kingdomsatwar.client.model.merfolk.MermaidModel;
import io.github.sirschrolli.kingdomsatwar.entity.merfolk.MerfolkLike;
import io.github.sirschrolli.kingdomsatwar.entity.merfolk.MermaidEntity;
import io.github.sirschrolli.kingdomsatwar.entity.merfolk.MermaidLook;
import io.github.sirschrolli.kingdomsatwar.entity.merfolk.TritonEntity;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.PathfinderMob;

/** Meerjungfrauen und Tritonwachen (seit 0.93): eine Textur je Gestalt; die Tiefsee-Gestalt leuchtet. */
public class MermaidRenderer<T extends PathfinderMob & MerfolkLike> extends MobRenderer<T, MermaidRenderState, MermaidModel> {
    private static final Map<MermaidLook, Identifier> TEXTURES = new EnumMap<>(MermaidLook.class);
    private static final Map<MermaidLook, RenderType> GLOW = new EnumMap<>(MermaidLook.class);

    static {
        for (MermaidLook look : MermaidLook.values()) {
            TEXTURES.put(look, KingdomsAtWar.id("textures/entity/mermaid/" + look.id() + ".png"));
            if (look.glows()) {
                GLOW.put(look, RenderTypes.eyes(KingdomsAtWar.id("textures/entity/mermaid/" + look.id() + "_glow.png")));
            }
        }
    }

    public MermaidRenderer(EntityRendererProvider.Context context) {
        super(context, new MermaidModel(context.bakeLayer(ModModelLayers.MERMAID)), 0.4f);
        this.addLayer(new RenderLayer<MermaidRenderState, MermaidModel>(this) {
            @Override
            public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, MermaidRenderState state, float yRot,
                    float xRot) {
                RenderType glow = GLOW.get(state.look);
                if (glow != null) {
                    collector.order(1).submitModel(this.getParentModel(), state, poseStack, glow, lightCoords, OverlayTexture.NO_OVERLAY,
                            state.outlineColor);
                }
            }
        });
    }

    @Override
    public Identifier getTextureLocation(MermaidRenderState state) {
        return TEXTURES.get(state.look);
    }

    @Override
    public MermaidRenderState createRenderState() {
        return new MermaidRenderState();
    }

    @Override
    public void extractRenderState(T entity, MermaidRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.profession = entity.profession();
        state.look = entity.look();
        state.swimPose = entity.swimPose(partialTicks);
        state.swimPhase = entity.swimPhase(partialTicks);
        state.inWater = entity.isInWater();
        boolean trading = false;
        boolean unhappy = false;
        if (entity instanceof MermaidEntity mermaid) {
            trading = mermaid.isShowingTrade();
            unhappy = mermaid.getUnhappyCounter() > 0;
            state.stance = 0.0f;
        } else if (entity instanceof TritonEntity triton) {
            state.stance = Mth.lerp(partialTicks, triton.stanceO, triton.stance);
            state.thrust.copyFrom(triton.thrustAnimation);
            state.toss.copyFrom(triton.throwAnimation);
        }
        // Ware zeigen und Kopfschütteln weich hinein und heraus
        state.show = Anim.follow(entity, 0, trading ? 1.0f : 0.0f, state.ageInTicks, 6.0f);
        state.sulk = Anim.follow(entity, 1, unhappy ? 1.0f : 0.0f, state.ageInTicks, 8.0f);
    }
}
