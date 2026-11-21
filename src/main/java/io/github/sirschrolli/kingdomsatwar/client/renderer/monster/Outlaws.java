package io.github.sirschrolli.kingdomsatwar.client.renderer.monster;

import net.minecraft.client.model.monster.illager.IllagerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.IllagerRenderState;

/** Gemeinsames der Gesetzlosen: ein Modell für alle (Menschenkopf auf dem Illager-Skelett). */
final class Outlaws {
    private Outlaws() {}

    /** Das Modell der Gesetzlosen; das Überlager (Haar, Schleier, Maske) ist anders als bei Vanilla immer sichtbar. */
    static <S extends IllagerRenderState> IllagerModel<S> model(EntityRendererProvider.Context context) {
        IllagerModel<S> model = new IllagerModel<>(context.bakeLayer(MonsterRenderers.ILLAGER));
        model.getHat().visible = true;
        return model;
    }
}
