package io.github.sirschrolli.kingdomsatwar.client.renderer.monster;

import net.minecraft.client.model.monster.illager.IllagerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.IllagerRenderState;

/** Gemeinsames der Gesetzlosen: ein Modell für alle (Menschenkopf auf dem Illager-Skelett). */
final class Outlaws {
    private Outlaws() {}

    /** Das Modell der Gesetzlosen; das Überlager (Haar, Schleier, Maske) ist anders als bei Vanilla immer sichtbar. */
    static <S extends IllagerRenderState> IllagerModel<S> model(EntityRendererProvider.Context context) {
        IllagerModel<S> model = new IllagerModel<>(context.bakeLayer(MonsterRenderers.ILLAGER)) {
            @Override
            public void setupAnim(S state) {
                super.setupAnim(state);
                // seit 1.14: die Haltung bei besonderen Angriffen (Axt hoch, Wirbel, Kampfschrei …)
                io.github.sirschrolli.kingdomsatwar.client.combat.CombatPoses.illager(this.root(), state);
            }
        };
        model.getHat().visible = true;
        return model;
    }
}
