package io.github.sirschrolli.kingdomsatwar.client.model.monster;

import io.github.sirschrolli.kingdomsatwar.client.renderer.monster.BonesRenderState;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.skeleton.SkeletonModel;

/**
 * Gerippe, Frostgebein, Moorgebein und Dörrgebein auf dem Skelett-Knochengerüst (Bogenhaltung und Rüstung wie bei
 * Vanilla). Dem Moorgebein lassen sich die Pilze abscheren.
 */
public class BonesModel extends SkeletonModel<BonesRenderState> {
    private final ModelPart mushrooms;

    public BonesModel(ModelPart root) {
        super(root);
        this.mushrooms = root.getChild("head").getChild("mushrooms");
    }

    @Override
    public void setupAnim(BonesRenderState state) {
        super.setupAnim(state);
        this.mushrooms.visible = !state.sheared;
    }
}
