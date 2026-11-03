package io.github.sirschrolli.kingdomsatwar.client.renderer.umbrer;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.client.model.umbrer.UmbrerModel;
import io.github.sirschrolli.kingdomsatwar.entity.umbrer.UmbrerEntity;
import io.github.sirschrolli.kingdomsatwar.entity.umbrer.UmbrerGuardEntity;
import io.github.sirschrolli.kingdomsatwar.entity.umbrer.UmbrerLike;
import io.github.sirschrolli.kingdomsatwar.entity.umbrer.UmbrerProfession;
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

/** Umbrer und Wächter: eine Textur je Beruf, dazu leuchtende Augen, Sterne, Kristalle und Linsen. */
public class UmbrerRenderer<T extends PathfinderMob & UmbrerLike> extends MobRenderer<T, UmbrerRenderState, UmbrerModel> {
    private static final Map<UmbrerProfession, Identifier> TEXTURES = new EnumMap<>(UmbrerProfession.class);
    private static final Map<UmbrerProfession, RenderType> GLOW = new EnumMap<>(UmbrerProfession.class);

    static {
        for (UmbrerProfession profession : UmbrerProfession.values()) {
            TEXTURES.put(profession, KingdomsAtWar.id("textures/entity/umbrer/" + profession.id() + ".png"));
            GLOW.put(profession, RenderTypes.eyes(KingdomsAtWar.id("textures/entity/umbrer/" + profession.id() + "_glow.png")));
        }
    }

    public UmbrerRenderer(EntityRendererProvider.Context context) {
        super(context, new UmbrerModel(context.bakeLayer(ModModelLayers.UMBRER)), 0.5f);
        this.addLayer(new RenderLayer<UmbrerRenderState, UmbrerModel>(this) {
            @Override
            public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, UmbrerRenderState state, float yRot,
                    float xRot) {
                collector.order(1).submitModel(this.getParentModel(), state, poseStack, GLOW.get(state.profession), lightCoords,
                        OverlayTexture.NO_OVERLAY, state.outlineColor);
            }
        });
    }

    @Override
    public Identifier getTextureLocation(UmbrerRenderState state) {
        return TEXTURES.get(state.profession);
    }

    @Override
    public UmbrerRenderState createRenderState() {
        return new UmbrerRenderState();
    }

    @Override
    public void extractRenderState(T entity, UmbrerRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.profession = entity.profession();
        if (entity instanceof UmbrerEntity umbrer) {
            state.trading = umbrer.isShowingTrade();
            state.unhappy = umbrer.getUnhappyCounter() > 0;
            state.stance = 0.0f;
        } else if (entity instanceof UmbrerGuardEntity guard) {
            state.trading = false;
            state.unhappy = false;
            state.stance = Mth.lerp(partialTicks, guard.stanceO, guard.stance);
            state.thrust.copyFrom(guard.thrustAnimation);
        }
    }
}
