package io.github.sirschrolli.kingdomsatwar.client.boss;

import net.minecraft.client.model.geom.ModelPart;

/** Nyxars Modell: langer Hals (sieben Glieder), zehn Schwanzglieder, vier Beine, riesige Flügel mit vier Fingern. */
public class NyxarModel extends DragonBossModel {
    public NyxarModel(ModelPart root) {
        super(root, 7, 10, true, 4, 32.0f);
    }

    @Override
    public DragonMotion createMotion() {
        return new NyxarMotion(this.neck.length, this.tail.length, true, this.bodyHeight);
    }
}
