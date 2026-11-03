package io.github.sirschrolli.kingdomsatwar.client.renderer.monster;

import net.minecraft.client.renderer.entity.state.SkeletonRenderState;

/** Zeichenzustand der Skelett-Familie: Art und ob die Pilze abgeschoren sind. */
public class BonesRenderState extends SkeletonRenderState {
    public int look;
    public boolean sheared;
}
