package io.github.sirschrolli.kingdomsatwar.client.renderer.wild;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.sirschrolli.kingdomsatwar.client.model.wild.FelineModel;
import io.github.sirschrolli.kingdomsatwar.entity.creature.wild.HouseCat;
import net.minecraft.client.renderer.entity.CatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/** Hauskatze: Fellbild je Rasse, eingefärbtes Halsband; liegt wie bei Vanilla auf der Seite und beim Spieler im Bett. */
public class HouseCatRenderer extends WildRenderer<HouseCat, HouseCatRenderState, FelineModel<HouseCatRenderState>> {
    private static final Identifier[] TEXTURES = textures("feline", "cat_", HouseCat.Breed.values(), "");

    public HouseCatRenderer(EntityRendererProvider.Context context) {
        super(context, new FelineModel<>(context.bakeLayer(WildRenderers.CAT), false), 0.4f, new CatRenderer(context));
        this.addLayer(new TintedCollarLayer<>(this, new FelineModel<HouseCatRenderState>(context.bakeLayer(WildRenderers.CAT), true),
                state -> state.collarColor, this::getTextureLocation));
    }

    @Override
    protected void extract(HouseCat cat, HouseCatRenderState state, float partialTicks) {
        state.breed = cat.getBreed().ordinal();
        state.fluffy = cat.getBreed() == HouseCat.Breed.PERSER;
    }

    @Override
    protected void setupRotations(HouseCatRenderState state, PoseStack poseStack, float bodyRot, float entityScale) {
        super.setupRotations(state, poseStack, bodyRot, entityScale);
        float lie = state.lieDownAmount;
        if (lie > 0.0f) {
            poseStack.translate(0.4f * lie, 0.15f * lie, 0.1f * lie);
            poseStack.rotateDegrees(Axis.ZP, Mth.rotLerp(lie, 0.0f, 90.0f));
            if (state.isLyingOnTopOfSleepingPlayer) {
                poseStack.translate(0.15f * lie, 0.0f, 0.0f);
            }
        }
    }

    @Override
    public Identifier getTextureLocation(HouseCatRenderState state) {
        return pick(TEXTURES, state.breed);
    }

    @Override
    public HouseCatRenderState createRenderState() {
        return new HouseCatRenderState();
    }
}
