package io.github.sirschrolli.kingdomsatwar.client.renderer.end;

import io.github.sirschrolli.kingdomsatwar.client.model.end.LeerwandlerModel;
import io.github.sirschrolli.kingdomsatwar.client.renderer.monster.GlowLayer;
import io.github.sirschrolli.kingdomsatwar.entity.creature.end.Leerwandler;
import net.minecraft.client.model.monster.enderman.EndermanModel;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.entity.EndermanRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.CarriedBlockLayer;
import net.minecraft.client.renderer.entity.state.EndermanRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Leerwandler: eigenes Modell auf dem Enderman-Gerüst, Bild je Gestalt; Augen, Sterne, Riss, Kristalle und Schlund
 * leuchten, er zittert im Zorn und trägt Blöcke wie der Enderman.
 */
public class LeerwandlerRenderer extends MobRenderer<Enderman, EndermanRenderState, EndermanModel<EndermanRenderState>> {
    private static final String[] LOOKS = {"leerwandler", "wirrwandler"};
    private static final Identifier[] TEXTURES = new Identifier[LOOKS.length];
    private static final RenderType[] GLOWS = new RenderType[LOOKS.length];

    static {
        for (int i = 0; i < LOOKS.length; i++) {
            TEXTURES[i] = EndRenderers.texture("enderman", LOOKS[i]);
            GLOWS[i] = RenderTypes.eyes(EndRenderers.texture("enderman", LOOKS[i] + "_glow"));
        }
    }

    public static class State extends EndermanRenderState {
        public int look;
    }

    private final RandomSource random = RandomSource.create();
    private final BlockModelResolver blockModelResolver;

    public LeerwandlerRenderer(EntityRendererProvider.Context context) {
        super(context, new LeerwandlerModel(context.bakeLayer(EndRenderers.LEERWANDLER)), 0.5f);
        this.blockModelResolver = context.getBlockModelResolver();
        this.addLayer(new GlowLayer<>(this, state -> GLOWS[look(state)]));
        this.addLayer(new CarriedBlockLayer(this));
    }

    private static int look(EndermanRenderState state) {
        return state instanceof State s ? Mth.clamp(s.look, 0, LOOKS.length - 1) : 0;
    }

    @Override
    public Vec3 getRenderOffset(EndermanRenderState state) {
        Vec3 offset = super.getRenderOffset(state);
        if (state.isCreepy) {
            double d = 0.02 * state.scale;
            return offset.add(this.random.nextGaussian() * d, 0.0, this.random.nextGaussian() * d);
        }
        return offset;
    }

    @Override
    public Identifier getTextureLocation(EndermanRenderState state) {
        return TEXTURES[look(state)];
    }

    @Override
    public EndermanRenderState createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(Enderman entity, EndermanRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        HumanoidMobRenderer.extractHumanoidRenderState(entity, state, partialTicks, this.itemModelResolver);
        state.isCreepy = entity.isCreepy();
        BlockState carried = entity.getCarriedBlock();
        if (carried != null) {
            this.blockModelResolver.update(state.carriedBlock, carried, EndermanRenderer.BLOCK_DISPLAY_CONTEXT);
        } else {
            state.carriedBlock.clear();
        }
        if (state instanceof State s) {
            s.look = entity instanceof Leerwandler wandler ? wandler.getLook().ordinal() : 0;
        }
    }
}
