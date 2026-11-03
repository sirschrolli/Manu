package io.github.sirschrolli.kingdomsatwar.client.renderer.nether;

import io.github.sirschrolli.kingdomsatwar.client.renderer.monster.GlowLayer;
import io.github.sirschrolli.kingdomsatwar.entity.creature.nether.Rauchqualle;
import net.minecraft.client.model.monster.ghast.GhastModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.GhastRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Ghast;

/**
 * Rauchqualle: eigenes Modell (Körper und Fangarme wie beim Vanilla-Ghast, damit die Fangarme wehen; im Spiel ×4,5),
 * Bild je Gestalt – ruhig mit geschlossenen Lidern, beim Feuerspeien mit aufgerissenen Augen und offenem Feuermaul.
 * Tränen, Augen, Maul, Glutnester und die Spitzen der Rauchfäden leuchten.
 */
public class RauchqualleRenderer extends MobRenderer<Ghast, GhastRenderState, GhastModel> {
    private static final String[] LOOKS = {"asche", "glut", "seele"};
    private static final Identifier[] CALM = new Identifier[LOOKS.length];
    private static final Identifier[] FIRE = new Identifier[LOOKS.length];
    private static final RenderType[] CALM_GLOW = new RenderType[LOOKS.length];
    private static final RenderType[] FIRE_GLOW = new RenderType[LOOKS.length];

    static {
        for (int i = 0; i < LOOKS.length; i++) {
            CALM[i] = NetherRenderers.texture("ghast", LOOKS[i]);
            FIRE[i] = NetherRenderers.texture("ghast", LOOKS[i] + "_feuer");
            CALM_GLOW[i] = RenderTypes.eyes(NetherRenderers.texture("ghast", LOOKS[i] + "_glow"));
            FIRE_GLOW[i] = RenderTypes.eyes(NetherRenderers.texture("ghast", LOOKS[i] + "_feuer_glow"));
        }
    }

    public static class State extends GhastRenderState {
        public int look;
    }

    public RauchqualleRenderer(EntityRendererProvider.Context context) {
        super(context, new GhastModel(context.bakeLayer(NetherRenderers.RAUCHQUALLE)), 1.5f);
        this.addLayer(new GlowLayer<>(this, state -> state.isCharging ? FIRE_GLOW[look(state)] : CALM_GLOW[look(state)]));
    }

    private static int look(GhastRenderState state) {
        return state instanceof State s ? Mth.clamp(s.look, 0, LOOKS.length - 1) : 0;
    }

    @Override
    public GhastRenderState createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(Ghast entity, GhastRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.isCharging = entity.isCharging();
        if (state instanceof State s) {
            s.look = entity instanceof Rauchqualle qualle ? qualle.getLook().ordinal() : 0;
        }
    }

    @Override
    public Identifier getTextureLocation(GhastRenderState state) {
        return state.isCharging ? FIRE[look(state)] : CALM[look(state)];
    }
}
