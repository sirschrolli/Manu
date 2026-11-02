package io.github.sirschrolli.kingdomsatwar.client.model.nether;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.WitherRenderState;
import net.minecraft.util.Mth;

/**
 * Modell des Aschenkönigs (Geometrie aus {@code AschenkoenigGeometry}). Die Köpfe drehen sich wie beim Vanilla-Wither zu
 * ihren Zielen; dazu atmet der Brustkorb, der Schwanz schlängelt, die Kiefer mahlen, die Arme pendeln und greifen, der
 * Mantel weht, das Seelenherz schlägt und das Feuer in der Krone flackert. Ohne Rückseiten-Auslassung gezeichnet, damit
 * man in die Krone und durch die Rippen sieht.
 */
public class AschenkoenigModel extends EntityModel<WitherRenderState> {
    private static final float DEG = Mth.DEG_TO_RAD;
    private final ModelPart centerHead;
    private final ModelPart rightHead;
    private final ModelPart leftHead;
    private final ModelPart[] jaws;
    private final ModelPart crownFire;
    private final ModelPart ribcage;
    private final ModelPart heart;
    private final ModelPart cape;
    private final ModelPart[] tail;
    private final ModelPart[] arms;
    private final ModelPart[] forearms;
    private final ModelPart[] hands;

    public AschenkoenigModel(ModelPart root) {
        super(root);                                       // entityCutout: ohne Rückseiten-Auslassung
        this.centerHead = root.getChild("center_head");
        this.rightHead = root.getChild("right_head");
        this.leftHead = root.getChild("left_head");
        this.jaws = new ModelPart[] {this.centerHead.getChild("center_jaw"), this.rightHead.getChild("right_jaw"),
                this.leftHead.getChild("left_jaw")};
        this.crownFire = this.centerHead.getChild("crown").getChild("crown_fire");
        this.ribcage = root.getChild("ribcage");
        this.heart = this.ribcage.getChild("heart");
        this.cape = this.ribcage.getChild("cape");
        ModelPart tail1 = this.ribcage.getChild("tail");
        ModelPart tail2 = tail1.getChild("tail2");
        this.tail = new ModelPart[] {tail1, tail2, tail2.getChild("tail3")};
        this.arms = new ModelPart[] {root.getChild("right_arm"), root.getChild("left_arm")};
        this.forearms = new ModelPart[] {this.arms[0].getChild("right_forearm"), this.arms[1].getChild("left_forearm")};
        this.hands = new ModelPart[] {this.forearms[0].getChild("right_hand"), this.forearms[1].getChild("left_hand")};
    }

    /** Nur die Hülle für den Seelenschild: ohne die Flammen in der Krone (Flächen bekämen sonst einen Schild). */
    public AschenkoenigModel shell() {
        this.crownFire.skipDraw = true;
        return this;
    }

    @Override
    public void setupAnim(WitherRenderState state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        // Köpfe wie beim Vanilla-Wither: der mittlere folgt dem Blick, die seitlichen ihren eigenen Zielen
        this.centerHead.yRot = state.yRot * DEG;
        this.centerHead.xRot = state.xRot * DEG;
        this.rightHead.yRot = (state.yHeadRots[0] - state.bodyRot) * DEG;
        this.rightHead.xRot = state.xHeadRots[0] * DEG;
        this.leftHead.yRot = (state.yHeadRots[1] - state.bodyRot) * DEG;
        this.leftHead.xRot = state.xHeadRots[1] * DEG;
        // der Brustkorb schwankt wie der des Withers, Schwanz und Mantel ziehen nach
        float breath = Mth.cos(t * 0.1f);
        this.ribcage.xRot = (0.065f + 0.05f * breath) * Mth.PI;
        for (int i = 0; i < this.tail.length; i++) {
            this.tail[i].xRot += 0.1f * breath + 0.14f * Mth.sin(t * 0.13f - i * 0.9f);
            this.tail[i].zRot += 0.09f * Mth.sin(t * 0.08f - i * 1.1f);
        }
        this.cape.xRot += 0.06f + 0.05f * Mth.sin(t * 0.11f) + 0.025f * Mth.sin(t * 0.37f + 1.0f);
        // die Kiefer mahlen, jeder in seinem Takt
        for (int i = 0; i < this.jaws.length; i++) {
            this.jaws[i].xRot += Math.max(0.0f, 0.12f * Mth.sin(t * 0.07f + i * 2.1f) + 0.05f * Mth.sin(t * 0.23f + i));
        }
        // die Arme pendeln langsam, die Krallen greifen
        for (int i = 0; i < 2; i++) {
            float side = i == 0 ? 1.0f : -1.0f;
            float sway = Mth.sin(t * 0.06f + i * Mth.PI);
            this.arms[i].xRot += 0.12f * sway;
            this.arms[i].zRot += side * 0.05f * Mth.sin(t * 0.05f + i);
            this.forearms[i].xRot += -0.1f - 0.12f * Mth.sin(t * 0.06f + i * Mth.PI + 0.6f);
            this.hands[i].xRot += -0.15f * Math.max(0.0f, Mth.sin(t * 0.09f + i * 1.7f));
        }
        // das Seelenherz schlägt, das Feuer in der Krone flackert
        float beat = Math.max(0.0f, Mth.sin(t * 0.35f)) * Math.max(0.0f, Mth.sin(t * 0.35f));
        float pulse = 1.0f + 0.12f * beat;
        this.heart.xScale = pulse;
        this.heart.yScale = pulse;
        this.heart.zScale = pulse;
        this.crownFire.yScale = 0.92f + 0.12f * Mth.sin(t * 0.7f) + 0.06f * Mth.sin(t * 1.9f + 0.7f);
        this.crownFire.xScale = 1.0f + 0.05f * Mth.sin(t * 0.5f + 0.3f);
        this.crownFire.zScale = this.crownFire.xScale;
    }
}
