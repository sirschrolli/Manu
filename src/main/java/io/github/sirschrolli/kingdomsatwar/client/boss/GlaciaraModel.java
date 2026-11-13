package io.github.sirschrolli.kingdomsatwar.client.boss;

import net.minecraft.client.model.geom.ModelPart;

/** Glaciaras Modell: langer Schwanenhals (acht Glieder), zehn Schwanzglieder, vier Beine, Flügel mit vier Fingern. */
public class GlaciaraModel extends DragonBossModel {
    public GlaciaraModel(ModelPart root) {
        super(root, 8, 10, true, 4, 30.0f);
    }

    @Override
    public DragonMotion createMotion() {
        return new GlaciaraMotion(this.neck.length, this.tail.length, true, this.bodyHeight);
    }
}
