package io.github.sirschrolli.kingdomsatwar.client.model.end;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.enderman.EndermanModel;
import net.minecraft.client.renderer.entity.state.EndermanRenderState;
import net.minecraft.util.Mth;

/**
 * Leerwandler auf dem Enderman-Gerüst (Gehen, Greifen, getragener Block und aufgerissenes Maul wie bei Vanilla); dazu
 * kreisen die drei Kristallsplitter über dem Kopf, schweben dabei auf und ab und drehen sich um sich selbst – im Zorn
 * rasen sie.
 */
public class LeerwandlerModel extends EndermanModel<EndermanRenderState> {
    private final ModelPart halo;
    private final ModelPart[] shards = new ModelPart[3];

    public LeerwandlerModel(ModelPart root) {
        super(root);
        this.halo = root.getChild("head").getChild("halo");
        for (int i = 0; i < this.shards.length; i++) {
            this.shards[i] = this.halo.getChild("shard_" + i);
        }
    }

    @Override
    public void setupAnim(EndermanRenderState state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        float speed = state.isCreepy ? 0.22f : 0.05f;
        this.halo.yRot = t * speed;
        for (int i = 0; i < this.shards.length; i++) {
            this.shards[i].y += Mth.sin(t * 0.09f + i * 2.1f) * 0.8f;
            this.shards[i].yRot += t * 0.11f * (i % 2 == 0 ? 1.0f : -1.0f);
        }
    }
}
