package io.github.sirschrolli.kingdomsatwar.client.model.spell;

import io.github.sirschrolli.kingdomsatwar.client.renderer.spell.SpellProjectileRenderer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Mth;

/** Modelle der Zaubergeschosse: der Phönix mit schlagenden Schwingen und die starre Frostlanze. */
public final class SpellProjectileModels {
    private SpellProjectileModels() {}

    /** Phönix: Schwingen schlagen kräftig (die Spitzen schwingen nach), der Schweif wogt, der Kopf nickt. */
    public static final class Phoenix extends Model<SpellProjectileRenderer.State> {
        private final ModelPart body;
        private final ModelPart head;
        private final ModelPart leftWing;
        private final ModelPart leftTip;
        private final ModelPart rightWing;
        private final ModelPart rightTip;
        private final ModelPart tail;

        public Phoenix(ModelPart root) {
            super(root, RenderTypes::entityTranslucentEmissive);
            this.body = root.getChild("body");
            this.head = this.body.getChild("head");
            this.leftWing = this.body.getChild("left_wing");
            this.leftTip = this.leftWing.getChild("left_wing_tip");
            this.rightWing = this.body.getChild("right_wing");
            this.rightTip = this.rightWing.getChild("right_wing_tip");
            this.tail = this.body.getChild("tail");
        }

        @Override
        public void setupAnim(SpellProjectileRenderer.State s) {
            super.setupAnim(s);
            float t = s.age;
            float flap = Mth.sin(t * 0.85f) * 0.75f;
            float tip = Mth.sin(t * 0.85f - 0.7f) * 0.55f;
            this.leftWing.zRot += flap;
            this.rightWing.zRot -= flap;
            this.leftTip.zRot += tip;
            this.rightTip.zRot -= tip;
            this.body.y += Mth.cos(t * 0.85f) * 0.8f;
            this.tail.xRot += Mth.sin(t * 0.45f) * 0.18f;
            this.tail.yRot += Mth.sin(t * 0.3f) * 0.12f;
            this.head.xRot += Mth.sin(t * 0.85f + 1.2f) * 0.08f;
        }
    }

    /** Frostlanze (auch die Eisklingen von Isoldes Frostpanzer): ohne eigene Bewegung. */
    public static final class Lance extends Model<Object> {
        public Lance(ModelPart root) {
            super(root, RenderTypes::entityTranslucent);
        }
    }
}
