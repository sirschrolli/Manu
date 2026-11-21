package io.github.sirschrolli.kingdomsatwar.client.renderer.abyss;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/** Ein Leibglied des Weltenfressers: zeigt das eine Weltstück, das es auf dem Rücken trägt; die Seitenkristalle zittern leicht. */
public class WorldEaterSegmentModel extends EntityModel<WorldEaterRenderState.Segment> {
    private final ModelPart[] worlds = new ModelPart[4];
    private final ModelPart crystalLeft;
    private final ModelPart crystalRight;

    public WorldEaterSegmentModel(ModelPart root) {
        super(root);
        ModelPart body = root.getChild("body");
        String[] names = {"world_overworld", "world_nether", "world_end", "world_frost"};
        for (int i = 0; i < names.length; i++) {
            this.worlds[i] = body.getChild(names[i]);
        }
        this.crystalLeft = body.getChild("side_crystal_l");
        this.crystalRight = body.getChild("side_crystal_r");
    }

    @Override
    public void setupAnim(WorldEaterRenderState.Segment state) {
        super.setupAnim(state);
        for (int i = 0; i < this.worlds.length; i++) {
            this.worlds[i].visible = i == state.world;
        }
        float t = state.ageInTicks + state.index * 7.0f;
        this.crystalLeft.zRot += Mth.sin(t * 0.09f) * 0.05f;
        this.crystalRight.zRot -= Mth.sin(t * 0.09f) * 0.05f;
    }
}
