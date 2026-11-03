package io.github.sirschrolli.kingdomsatwar.client.model.monster;

import io.github.sirschrolli.kingdomsatwar.client.renderer.monster.BonesRenderState;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.skeleton.SkeletonModel;
import net.minecraft.util.Mth;

/**
 * Gerippe, Frostgebein, Moorgebein, Dörrgebein und Brandgebein auf dem Skelett-Knochengerüst (Bogenhaltung und Rüstung
 * wie bei Vanilla). Dem Moorgebein lassen sich die Pilze abscheren; die Flammen auf dem Schädel des Brandgebeins
 * flackern und verlöschen unter einem Helm.
 */
public class BonesModel extends SkeletonModel<BonesRenderState> {
    private final ModelPart mushrooms;
    private final ModelPart flames;

    public BonesModel(ModelPart root) {
        super(root);
        this.mushrooms = root.getChild("head").getChild("mushrooms");
        this.flames = root.getChild("head").getChild("flames");
    }

    @Override
    public void setupAnim(BonesRenderState state) {
        super.setupAnim(state);
        this.mushrooms.visible = !state.sheared;
        this.flames.visible = state.headEquipment.isEmpty();
        float t = state.ageInTicks;
        // unruhiges Flackern: zwei Schwingungen übereinander, die Flammen wachsen und ducken sich vom Schädel aus
        this.flames.yScale = 0.9f + 0.14f * Mth.sin(t * 0.83f) + 0.07f * Mth.sin(t * 2.1f + 1.3f);
        this.flames.xScale = 1.0f + 0.05f * Mth.sin(t * 0.61f + 0.4f);
        this.flames.zScale = this.flames.xScale;
    }
}
