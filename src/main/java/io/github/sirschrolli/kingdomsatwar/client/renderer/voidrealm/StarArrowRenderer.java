package io.github.sirschrolli.kingdomsatwar.client.renderer.voidrealm;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.entity.voidrealm.StarArrowEntity;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.resources.Identifier;

/** Ein Sternenpfeil: ein Schaft aus Himmelseisen mit Kristallspitze und Federn aus Sternenseide. */
public class StarArrowRenderer extends ArrowRenderer<StarArrowEntity, ArrowRenderState> {
    private static final Identifier TEXTURE = KingdomsAtWar.id("textures/entity/projectiles/star_arrow.png");

    public StarArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected Identifier getTextureLocation(ArrowRenderState state) {
        return TEXTURE;
    }

    @Override
    public ArrowRenderState createRenderState() {
        return new ArrowRenderState();
    }
}
