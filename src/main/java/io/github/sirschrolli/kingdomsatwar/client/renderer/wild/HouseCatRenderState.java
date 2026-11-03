package io.github.sirschrolli.kingdomsatwar.client.renderer.wild;

import net.minecraft.client.renderer.entity.state.CatRenderState;

/** Zustand für den Renderer der Hauskatze: Rasse und Fell. */
public class HouseCatRenderState extends CatRenderState implements FelineLook {
    public int breed;
    public boolean fluffy;

    @Override
    public boolean lynx() {
        return false;
    }

    @Override
    public boolean fluffy() {
        return this.fluffy;
    }
}
