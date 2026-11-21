package io.github.sirschrolli.kingdomsatwar.client.renderer.monster;

import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Butcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.VindicatorRenderer;
import net.minecraft.client.renderer.entity.state.IllagerRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.illager.Vindicator;

/**
 * Schlächter: eigenes Modell und Bild je Gestalt; verschränkte Arme, die Axt erst im Kampf und der Jubel kommen vom
 * Vanilla-Renderer.
 */
public class ButcherRenderer extends VindicatorRenderer {
    private static final String[] NAMES = {"schlaechter_henker", "schlaechter_berserker"};
    private static final Identifier[] TEXTURES = MonsterRenderers.textures("illager", NAMES);

    public ButcherRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = Outlaws.model(context);
    }

    @Override
    public IllagerRenderState createRenderState() {
        return new LookIllagerRenderState();
    }

    @Override
    public void extractRenderState(Vindicator entity, IllagerRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        if (state instanceof LookIllagerRenderState look) {
            look.look = entity instanceof Butcher b ? Mth.clamp(b.getLook().ordinal(), 0, NAMES.length - 1) : 0;
        }
    }

    @Override
    public Identifier getTextureLocation(IllagerRenderState state) {
        return TEXTURES[state instanceof LookIllagerRenderState look ? look.look : 0];
    }
}
