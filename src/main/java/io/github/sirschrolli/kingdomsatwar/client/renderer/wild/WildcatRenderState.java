package io.github.sirschrolli.kingdomsatwar.client.renderer.wild;

import net.minecraft.client.renderer.entity.state.FelineRenderState;

/** Zustand für den Renderer der Wildkatze: Rasse (der Luchs hat Pinsel, Stummelschwanz und dichtes Fell). */
public class WildcatRenderState extends FelineRenderState implements FelineLook {
    public int breed;
    public boolean lynx;

    @Override
    public boolean lynx() {
        return this.lynx;
    }

    @Override
    public boolean fluffy() {
        return this.lynx;
    }
}
