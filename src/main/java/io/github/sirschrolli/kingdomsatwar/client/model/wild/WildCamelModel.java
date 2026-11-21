package io.github.sirschrolli.kingdomsatwar.client.model.wild;

import io.github.sirschrolli.kingdomsatwar.client.renderer.wild.WildCamelRenderState;
import io.github.sirschrolli.kingdomsatwar.entity.creature.wild.WildCamel;
import net.minecraft.client.model.animal.camel.AdultCamelModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.CamelRenderState;

/** Kamel auf dem Vanilla-Skelett (Sitzen, Aufstehen, Sprint, Sattel): ein Höcker beim Dromedar, zwei beim Trampeltier. */
public class WildCamelModel extends AdultCamelModel {
    private final ModelPart hump;
    private final ModelPart humps;

    public WildCamelModel(ModelPart root) {
        super(root);
        ModelPart body = root.getChild("body");
        this.hump = body.getChild("hump");
        this.humps = body.getChild("hump2");
    }

    @Override
    public void setupAnim(CamelRenderState state) {
        super.setupAnim(state);
        boolean bactrian = state instanceof WildCamelRenderState camel && camel.breed == WildCamel.Breed.TRAMPELTIER.ordinal();
        this.hump.visible = !bactrian;
        this.humps.visible = bactrian;
    }
}
