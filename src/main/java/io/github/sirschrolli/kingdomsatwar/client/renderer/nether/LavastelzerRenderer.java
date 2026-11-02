package io.github.sirschrolli.kingdomsatwar.client.renderer.nether;

import io.github.sirschrolli.kingdomsatwar.client.renderer.monster.GlowLayer;
import io.github.sirschrolli.kingdomsatwar.entity.creature.nether.Lavastelzer;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.strider.AdultStriderModel;
import net.minecraft.client.model.monster.strider.BabyStriderModel;
import net.minecraft.client.model.monster.strider.StriderModel;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.SimpleEquipmentLayer;
import net.minecraft.client.renderer.entity.state.StriderRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;

/**
 * Lavastelzer: eigenes Modell (Knochen und Rumpf wie beim Vanilla-Schreiter, damit Stelzen, Haarwehen und der
 * Vanilla-Sattel passen), Bild je Gestalt; Augen, Haarspitzen, Poren und die heißen Füße leuchten. Außerhalb der Lava
 * friert er: fahles Bild ohne Glut, und er zittert.
 */
public class LavastelzerRenderer extends AgeableMobRenderer<Lavastelzer, StriderRenderState, StriderModel> {
    private static final String[] LOOKS = {"glut", "seele"};
    private static final Identifier[] ADULT = new Identifier[LOOKS.length];
    private static final Identifier[] BABY = new Identifier[LOOKS.length];
    private static final Identifier[] ADULT_COLD = new Identifier[LOOKS.length];
    private static final Identifier[] BABY_COLD = new Identifier[LOOKS.length];
    private static final RenderType[] GLOW = new RenderType[LOOKS.length];
    private static final RenderType[] BABY_GLOW = new RenderType[LOOKS.length];

    static {
        for (int i = 0; i < LOOKS.length; i++) {
            ADULT[i] = NetherRenderers.texture("strider", LOOKS[i]);
            BABY[i] = NetherRenderers.texture("strider/baby", LOOKS[i]);
            ADULT_COLD[i] = NetherRenderers.texture("strider", LOOKS[i] + "_kalt");
            BABY_COLD[i] = NetherRenderers.texture("strider/baby", LOOKS[i] + "_kalt");
            GLOW[i] = RenderTypes.eyes(NetherRenderers.texture("strider", LOOKS[i] + "_glow"));
            BABY_GLOW[i] = RenderTypes.eyes(NetherRenderers.texture("strider/baby", LOOKS[i] + "_glow"));
        }
    }

    public static class State extends StriderRenderState {
        public int look;
    }

    public LavastelzerRenderer(EntityRendererProvider.Context context) {
        super(context, new AdultStriderModel(context.bakeLayer(NetherRenderers.LAVASTELZER)),
                new BabyStriderModel(context.bakeLayer(NetherRenderers.LAVASTELZER_BABY)), 0.5f);
        this.addLayer(new GlowLayer<>(this, state -> state.isSuffocating ? null : state.isBaby ? BABY_GLOW[look(state)] : GLOW[look(state)]));
        // der Vanilla-Sattel: sein Modell hat den Rumpf des Vanilla-Schreiters, und genau den hat auch der Lavastelzer
        this.addLayer(new SimpleEquipmentLayer<>(this, context.getEquipmentRenderer(), EquipmentClientInfo.LayerType.STRIDER_SADDLE,
                state -> state.saddle, new AdultStriderModel(context.bakeLayer(ModelLayers.STRIDER_SADDLE)), null));
    }

    private static int look(StriderRenderState state) {
        return state instanceof State s ? Mth.clamp(s.look, 0, LOOKS.length - 1) : 0;
    }

    @Override
    public StriderRenderState createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(Lavastelzer entity, StriderRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.saddle = entity.getItemBySlot(EquipmentSlot.SADDLE).copy();
        state.isSuffocating = entity.isSuffocating();
        state.isRidden = entity.isVehicle();
        if (state instanceof State s) {
            s.look = entity.getLook().ordinal();
        }
    }

    @Override
    public Identifier getTextureLocation(StriderRenderState state) {
        int look = look(state);
        if (state.isSuffocating) {
            return state.isBaby ? BABY_COLD[look] : ADULT_COLD[look];
        }
        return state.isBaby ? BABY[look] : ADULT[look];
    }

    @Override
    protected float getShadowRadius(StriderRenderState state) {
        float radius = super.getShadowRadius(state);
        return state.isBaby ? radius * 0.5f : radius;
    }

    @Override
    protected boolean isShaking(StriderRenderState state) {
        return super.isShaking(state) || state.isSuffocating;
    }
}
