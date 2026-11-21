package io.github.sirschrolli.kingdomsatwar.client.model.wild;

import io.github.sirschrolli.kingdomsatwar.client.renderer.wild.FelineLook;
import java.util.List;
import net.minecraft.client.model.animal.feline.AdultFelineModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.FelineRenderState;

/**
 * Hauskatzen und Wildkatzen auf dem Vanilla-Katzenskelett (Sitzen, Schleichen, Liegen, Schnurren). Je Rasse: Ohrpinsel
 * und Stummelschwanz des Luchses statt des langen Schwanzendes, Backenbart und dichtes Fell bei Perser und Luchs.
 * Das Halsband ist ein eigener Knochen (bei dichtem Fell ein weiteres) und wird – wie beim Wolf – von einem zweiten
 * Exemplar allein gezeichnet.
 */
public class FelineModel<T extends FelineRenderState> extends AdultFelineModel<T> {
    private final ModelPart tuftLeft;
    private final ModelPart tuftRight;
    private final ModelPart tailStub;
    private final ModelPart cheeks;
    private final ModelPart fluff;
    private final ModelPart collar;
    private final ModelPart fluffyCollar;
    private final List<ModelPart> others;
    private final boolean collarOnly;

    public FelineModel(ModelPart root, boolean collarOnly) {
        super(root);
        ModelPart headPart = root.getChild("head");
        this.tuftLeft = headPart.getChild("ear_l").getChild("tuft_l");
        this.tuftRight = headPart.getChild("ear_r").getChild("tuft_r");
        this.cheeks = headPart.getChild("cheeks");
        this.tailStub = root.getChild("tail1").getChild("tail_stub");
        ModelPart bodyPart = root.getChild("body");
        this.fluff = bodyPart.getChild("fluff_body");
        this.collar = bodyPart.getChild("collar");
        this.fluffyCollar = bodyPart.getChild("collar_fluffy");
        this.others = root.getAllParts().stream().filter(part -> part != this.collar && part != this.fluffyCollar).toList();
        this.collarOnly = collarOnly;
    }

    @Override
    public void setupAnim(T state) {
        super.setupAnim(state);
        boolean lynx = state instanceof FelineLook look && look.lynx();
        boolean fluffy = state instanceof FelineLook look && look.fluffy();
        this.tuftLeft.visible = this.tuftRight.visible = lynx;
        this.tailStub.visible = lynx;
        this.tail2.visible = !lynx;
        this.cheeks.visible = this.fluff.visible = fluffy;
        if (state.isBaby) {
            this.head.xScale = this.head.yScale = this.head.zScale = 1.3f;
        }
        this.collar.visible = this.collarOnly && !fluffy;
        this.fluffyCollar.visible = this.collarOnly && fluffy;
        for (ModelPart part : this.others) {
            part.skipDraw = this.collarOnly;
        }
    }
}
