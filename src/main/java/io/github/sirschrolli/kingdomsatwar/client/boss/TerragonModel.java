package io.github.sirschrolli.kingdomsatwar.client.boss;

import net.minecraft.client.model.geom.ModelPart;

/** Terragons Modell: kurzer, dicker Hals (drei Glieder), sechs Schwanzglieder mit Felskeule, vier Säulenbeine, keine Flügel. */
public class TerragonModel extends DragonBossModel {
    public TerragonModel(ModelPart root) {
        super(root, 3, 6, true, 0, 31.0f);
    }

    @Override
    public DragonMotion createMotion() {
        return new TerragonMotion(this.neck.length, this.tail.length, true, this.bodyHeight);
    }
}
