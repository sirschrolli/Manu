package io.github.sirschrolli.kingdomsatwar.client.renderer.wild;

import io.github.sirschrolli.kingdomsatwar.entity.creature.wild.WildArmadillo;
import net.minecraft.client.model.animal.armadillo.AdultArmadilloModel;
import net.minecraft.client.model.animal.armadillo.ArmadilloModel;
import net.minecraft.client.renderer.entity.ArmadilloRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

/** Gürteltier: Neunbinden- oder Kugelgürteltier; Einrollen, Spähen und Aufrollen macht das Vanilla-Modell. */
public class WildArmadilloRenderer extends WildRenderer<WildArmadillo, WildArmadilloRenderState, ArmadilloModel> {
    private static final Identifier[] TEXTURES = textures("armadillo", "armadillo_", WildArmadillo.Breed.values(), "");

    public WildArmadilloRenderer(EntityRendererProvider.Context context) {
        super(context, new AdultArmadilloModel(context.bakeLayer(WildRenderers.ARMADILLO)), 0.4f, new ArmadilloRenderer(context));
    }

    @Override
    protected void extract(WildArmadillo armadillo, WildArmadilloRenderState state, float partialTicks) {
        state.breed = armadillo.getBreed().ordinal();
    }

    @Override
    public Identifier getTextureLocation(WildArmadilloRenderState state) {
        return pick(TEXTURES, state.breed);
    }

    @Override
    public WildArmadilloRenderState createRenderState() {
        return new WildArmadilloRenderState();
    }
}
