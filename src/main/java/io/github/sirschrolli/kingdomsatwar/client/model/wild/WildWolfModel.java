package io.github.sirschrolli.kingdomsatwar.client.model.wild;

import java.util.List;
import net.minecraft.client.model.animal.wolf.AdultWolfModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.WolfRenderState;

/**
 * Wolf auf dem Vanilla-Skelett: Sitzen, Schütteln, Betteln und Rute bewegt das Vanilla-Modell. Dazu das eigene
 * Halsband (ein eigener Knochen): Im Hauptmodell ist es aus; ein zweites Exemplar zeichnet NUR das Halsband, damit es
 * der Renderer in der Farbe des Halsbands einfärben kann. Welpen bekommen einen größeren Kopf.
 */
public class WildWolfModel extends AdultWolfModel {
    private final ModelPart collar;
    private final List<ModelPart> others;
    private final boolean collarOnly;

    public WildWolfModel(ModelPart root, boolean collarOnly) {
        super(root);
        this.collar = root.getChild("upper_body").getChild("collar");
        this.others = root.getAllParts().stream().filter(part -> part != this.collar).toList();
        this.collarOnly = collarOnly;
    }

    @Override
    public void setupAnim(WolfRenderState state) {
        super.setupAnim(state);
        if (state.isBaby) {
            this.head.xScale = this.head.yScale = this.head.zScale = 1.3f;
        }
        this.collar.visible = this.collarOnly;
        for (ModelPart part : this.others) {
            part.skipDraw = this.collarOnly;
        }
    }
}
