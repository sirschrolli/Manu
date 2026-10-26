package io.github.sirschrolli.kingdomsatwar.client.boss;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;

/**
 * Verdantis' Modell: kurzer, dicker Hals (vier Glieder), sieben Schwanzglieder mit Samenkolben, vier Beine, Blattflügel
 * mit drei Fingern. Dazu wiegen sich die Bäumchen auf dem Rücken, die Farnwedel am Schwanz und die Blattohren.
 */
public class VerdantisModel extends DragonBossModel {
    private final List<ModelPart> trees = new ArrayList<>();
    private final List<ModelPart> fronds = new ArrayList<>();
    private final @Nullable ModelPart earL;
    private final @Nullable ModelPart earR;

    public VerdantisModel(ModelPart root) {
        super(root, 4, 7, true, 3, 33.0f);
        Function<String, @Nullable ModelPart> part = root.createPartLookup();
        for (String name : new String[] {"tree_0", "tree_1", "deadwood"}) {
            ModelPart p = part.apply(name);
            if (p != null) {
                this.trees.add(p);
            }
        }
        for (int i = 0; i < 5; i++) {
            ModelPart p = part.apply("tailfrond_" + i);
            if (p != null) {
                this.fronds.add(p);
            }
        }
        this.earL = part.apply("ear_l");
        this.earR = part.apply("ear_r");
    }

    @Override
    public DragonMotion createMotion() {
        return new VerdantisMotion(this.neck.length, this.tail.length, true, this.bodyHeight);
    }

    @Override
    public void setupAnim(BossRenderState s) {
        super.setupAnim(s);
        float t = s.ageInTicks / 20.0f;
        float walk = Math.min(s.walkAnimationSpeed * 1.4f, 1.0f);
        float wind = 0.04f + 0.05f * walk + 0.12f * s.airBlend;
        for (int i = 0; i < this.trees.size(); i++) {
            ModelPart tree = this.trees.get(i);
            tree.zRot += Mth.sin(t * 1.3f + i * 1.7f) * wind - s.bodyRoll * 0.8f;
            tree.xRot += Mth.sin(t * 1.1f + i * 2.3f) * wind * 0.7f - s.bodyPitch * 0.6f;
        }
        for (int i = 0; i < this.fronds.size(); i++) {
            this.fronds.get(i).xRot += Mth.sin(t * 3.1f + i * 0.9f) * (0.06f + 0.1f * walk) + 0.05f * s.airBlend;
        }
        float flick = Math.max(0.0f, Mth.sin(t * 0.7f) - 0.85f) * 3.0f;
        if (this.earL != null) {
            this.earL.yRot += Mth.sin(t * 9.0f) * 0.25f * flick + 0.15f * Mth.sin(t * 0.9f);
        }
        if (this.earR != null) {
            this.earR.yRot -= Mth.sin(t * 9.0f + 1.0f) * 0.25f * flick + 0.15f * Mth.sin(t * 0.9f + 0.5f);
        }
    }
}
