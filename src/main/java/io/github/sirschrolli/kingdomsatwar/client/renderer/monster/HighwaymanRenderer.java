package io.github.sirschrolli.kingdomsatwar.client.renderer.monster;

import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Highwayman;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.PillagerRenderer;
import net.minecraft.client.renderer.entity.state.IllagerRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.illager.Pillager;

/**
 * Wegelagerer: eigenes Modell und Bild je Gestalt; Armbrust in der Hand, Spannen, Zielen, Reiten und Jubeln erbt er
 * vom Vanilla-Renderer.
 */
public class HighwaymanRenderer extends PillagerRenderer {
    private static final String[] NAMES = {"wegelagerer_wald", "wegelagerer_steppe", "wegelagerer_nord"};
    private static final Identifier[] TEXTURES = MonsterRenderers.textures("illager", NAMES);

    public HighwaymanRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = Outlaws.model(context);
    }

    @Override
    public IllagerRenderState createRenderState() {
        return new LookIllagerRenderState();
    }

    @Override
    public void extractRenderState(Pillager entity, IllagerRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        if (state instanceof LookIllagerRenderState look) {
            look.look = entity instanceof Highwayman h ? Mth.clamp(h.getLook().ordinal(), 0, NAMES.length - 1) : 0;
        }
    }

    @Override
    public Identifier getTextureLocation(IllagerRenderState state) {
        return TEXTURES[state instanceof LookIllagerRenderState look ? look.look : 0];
    }
}
