package io.github.sirschrolli.kingdomsatwar.client.renderer.monster;

import io.github.sirschrolli.kingdomsatwar.client.renderer.wild.WildRenderer;
import java.util.function.ToIntFunction;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.monster.spider.SpiderModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SpiderRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.spider.Spider;

/**
 * Spinnen und Höhlenspinne: eigenes Modell mit geknickten Beinen, Kieferklauen und Spinnwarzen, eigene Bilder je Art
 * und leuchtende Augen; Gehen und Klettern wie bei Vanilla, auf dem Rücken liegend im Tod.
 */
public class WildSpiderRenderer<T extends Spider> extends WildRenderer<T, LookRenderState, SpiderModel> {
    private final Identifier[] textures;
    private final RenderType[] glows;
    private final ToIntFunction<T> look;

    public WildSpiderRenderer(EntityRendererProvider.Context context, ModelLayerLocation layer, float shadow, String[] names,
            ToIntFunction<T> look) {
        super(context, new SpiderModel(context.bakeLayer(layer)), shadow, new SpiderRenderer<T>(context));
        this.textures = MonsterRenderers.textures("spider", names);
        this.glows = MonsterRenderers.glows("spider", names);
        this.look = look;
        this.addLayer(new GlowLayer<>(this, state -> this.glows[Mth.clamp(state.look, 0, this.glows.length - 1)]));
    }

    @Override
    protected void extract(T spider, LookRenderState state, float partialTicks) {
        state.look = this.look.applyAsInt(spider);
    }

    @Override
    protected float getFlipDegrees() {
        return 180.0f;
    }

    @Override
    public Identifier getTextureLocation(LookRenderState state) {
        return this.textures[Mth.clamp(state.look, 0, this.textures.length - 1)];
    }

    @Override
    public LookRenderState createRenderState() {
        return new LookRenderState();
    }
}
