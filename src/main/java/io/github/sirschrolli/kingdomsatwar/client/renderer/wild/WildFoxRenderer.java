package io.github.sirschrolli.kingdomsatwar.client.renderer.wild;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.sirschrolli.kingdomsatwar.client.model.wild.WildFoxModel;
import io.github.sirschrolli.kingdomsatwar.entity.creature.wild.WildFox;
import net.minecraft.client.model.animal.fox.FoxModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.FoxRenderer;
import net.minecraft.resources.Identifier;

/** Fuchs: Fellbild je Art, schlafend mit geschlossenen Augen; trägt Dinge im Maul. */
public class WildFoxRenderer extends WildRenderer<WildFox, WildFoxRenderState, FoxModel> {
    private static final Identifier[] AWAKE = textures("fox", "fox_", WildFox.Breed.values(), "");
    private static final Identifier[] ASLEEP = textures("fox", "fox_", WildFox.Breed.values(), "_sleep");

    public WildFoxRenderer(EntityRendererProvider.Context context) {
        super(context, new WildFoxModel(context.bakeLayer(WildRenderers.FOX)), 0.4f, new FoxRenderer(context));
        this.addLayer(new WildFoxHeldItemLayer(this));
    }

    @Override
    protected void extract(WildFox fox, WildFoxRenderState state, float partialTicks) {
        state.breed = fox.getBreed().ordinal();
    }

    /** Beim Sprung auf Beute und beim Kopfüber-im-Schnee kippt der ganze Fuchs mit dem Blick (wie bei Vanilla). */
    @Override
    protected void setupRotations(WildFoxRenderState state, PoseStack poseStack, float bodyRot, float entityScale) {
        super.setupRotations(state, poseStack, bodyRot, entityScale);
        if (state.isPouncing || state.isFaceplanted) {
            poseStack.rotateDegrees(Axis.XP, -state.xRot);
        }
    }

    @Override
    public Identifier getTextureLocation(WildFoxRenderState state) {
        return pick(state.isSleeping ? ASLEEP : AWAKE, state.breed);
    }

    @Override
    public WildFoxRenderState createRenderState() {
        return new WildFoxRenderState();
    }
}
