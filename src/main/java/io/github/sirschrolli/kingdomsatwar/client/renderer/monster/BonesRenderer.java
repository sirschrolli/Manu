package io.github.sirschrolli.kingdomsatwar.client.renderer.monster;

import io.github.sirschrolli.kingdomsatwar.client.model.monster.BonesModel;
import java.util.function.ToIntFunction;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AbstractSkeletonRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.skeleton.Bogged;

/**
 * Renderer der Skelett-Familie: eigenes Modell (Rippen, Kiefer, Gewand, Pilze), eigene Bilder je Art und glimmende
 * Augenhöhlen; Bogenhaltung, Rüstung und Gegenstände wie bei Vanilla.
 */
public class BonesRenderer<T extends AbstractSkeleton> extends AbstractSkeletonRenderer<T, BonesRenderState> {
    private final Identifier[] textures;
    private final RenderType[] glows;
    private final ToIntFunction<T> look;

    public BonesRenderer(EntityRendererProvider.Context context, String[] names, ToIntFunction<T> look) {
        super(context, ModelLayers.SKELETON_ARMOR, new BonesModel(context.bakeLayer(MonsterRenderers.BONES)));
        this.textures = MonsterRenderers.textures("skeleton", names);
        this.glows = MonsterRenderers.glows("skeleton", names);
        this.look = look;
        this.addLayer(new GlowLayer<>(this, state -> this.glows[Mth.clamp(state.look, 0, this.glows.length - 1)]));
    }

    @Override
    public BonesRenderState createRenderState() {
        return new BonesRenderState();
    }

    @Override
    public void extractRenderState(T entity, BonesRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.look = this.look.applyAsInt(entity);
        state.sheared = entity instanceof Bogged bogged && bogged.isSheared();
    }

    @Override
    public Identifier getTextureLocation(BonesRenderState state) {
        return this.textures[Mth.clamp(state.look, 0, this.textures.length - 1)];
    }
}
