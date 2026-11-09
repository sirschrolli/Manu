package io.github.sirschrolli.kingdomsatwar.client.renderer.fae;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.entity.fae.ThornProjectile;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.resources.Identifier;

/** Ein fliegender Dorn: dunkler, violetter Schaft mit heller Spitze. */
public class ThornProjectileRenderer extends ArrowRenderer<ThornProjectile, ArrowRenderState> {
    private static final Identifier TEXTURE = KingdomsAtWar.id("textures/entity/projectiles/thorn.png");

    public ThornProjectileRenderer(EntityRendererProvider.Context context) {
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
