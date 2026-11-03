package io.github.sirschrolli.kingdomsatwar.client.model.shadow;

import static io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim.DEG;

import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import io.github.sirschrolli.kingdomsatwar.client.renderer.shadow.ShadowCreatureRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/** Kristallkriecher: Krabbeln mit sechs Beinen im Wechsel (drei und drei), zuckende Zangen, Biss. */
public class CrystalCrawlerModel extends EntityModel<ShadowCreatureRenderState> {
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart abdomen;
    private final ModelPart mandibleL;
    private final ModelPart mandibleR;
    private final ModelPart[] legs = new ModelPart[6];
    private final ModelPart crystals;

    public CrystalCrawlerModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.head = this.body.getChild("head");
        this.abdomen = this.body.getChild("abdomen");
        this.mandibleL = this.head.getChild("mandible_l");
        this.mandibleR = this.head.getChild("mandible_r");
        this.crystals = this.body.getChild("crystals");
        for (int i = 0; i < 3; i++) {
            this.legs[i] = this.body.getChild("leg_l" + (i + 1));
            this.legs[i + 3] = this.body.getChild("leg_r" + (i + 1));
        }
    }

    @Override
    public void setupAnim(ShadowCreatureRenderState state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        float pos = state.walkAnimationPos;
        float speed = Math.min(state.walkAnimationSpeed * 1.5f, 1.0f);
        float stride = pos * 1.2f;
        for (int i = 0; i < 6; i++) {
            boolean left = i < 3;
            int k = i % 3;
            // Dreifuß-Gang: vorne+hinten links mit Mitte rechts im Takt
            float phase = ((k == 1) == left ? Mth.PI : 0.0f);
            float swing = Mth.cos(stride + phase) * 0.45f * speed;
            float lift = Math.max(0.0f, Mth.sin(stride + phase)) * 0.35f * speed;
            ModelPart leg = this.legs[i];
            leg.yRot += swing * (left ? 1.0f : -1.0f);
            leg.zRot += lift * (left ? -1.0f : 1.0f);
        }
        this.body.y += Math.abs(Mth.sin(stride)) * 0.3f * speed;
        this.abdomen.xRot += Mth.sin(t * 0.08f) * 0.04f;
        this.crystals.yRot += Mth.sin(t * 0.03f) * 0.02f;
        float twitch = Mth.sin(t * 0.4f) * 0.06f;
        this.mandibleL.yRot += twitch;
        this.mandibleR.yRot -= twitch;
        this.head.yRot += Mth.clamp(state.yRot, -30.0f, 30.0f) * DEG * 0.6f;
        this.head.xRot += Mth.clamp(state.xRot, -20.0f, 20.0f) * DEG * 0.5f;

        float s = Anim.seconds(state.attack, t);
        if (s >= 0.0f && s < 0.4f) {
            float open = Anim.bump(s, 0.1f, 0.14f, 0.25f);
            this.mandibleL.yRot -= 0.6f * open;
            this.mandibleR.yRot += 0.6f * open;
            this.head.xRot -= 0.25f * open;
            this.body.xRot -= 0.1f * open;
        }
        this.body.xRot -= 1.25f * state.climb;                           // an der Wand aufgerichtet
    }
}
