package io.github.sirschrolli.kingdomsatwar.client.renderer.wild;

import io.github.sirschrolli.kingdomsatwar.entity.creature.wild.WildLlama;
import net.minecraft.client.model.animal.llama.LlamaModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LlamaRenderer;
import net.minecraft.client.renderer.entity.state.LlamaRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.equine.Llama;

/** Lama und Händlerlama: Fellbild je Farbe, Teppich bzw. Händlerdecke und Packtaschen wie bei Vanilla. */
public class WildLlamaRenderer<T extends Llama> extends WildRenderer<T, LlamaRenderState, LlamaModel> {
    private static final Identifier[] TEXTURES = textures("llama", "llama_", WildLlama.Breed.values(), "");

    public WildLlamaRenderer(EntityRendererProvider.Context context) {
        super(context, new LlamaModel(context.bakeLayer(WildRenderers.LLAMA)), 0.7f,
                new LlamaRenderer(context, ModelLayers.LLAMA, ModelLayers.LLAMA_BABY));
        this.addLayer(new WildLlamaDecorLayer(this, context.getModelSet(), context.getEquipmentRenderer()));
    }

    @Override
    public Identifier getTextureLocation(LlamaRenderState state) {
        return pick(TEXTURES, WildLlama.breedOf(state.variant).ordinal());
    }

    @Override
    public LlamaRenderState createRenderState() {
        return new LlamaRenderState();
    }
}
