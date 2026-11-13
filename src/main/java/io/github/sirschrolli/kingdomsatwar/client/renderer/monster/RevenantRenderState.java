package io.github.sirschrolli.kingdomsatwar.client.renderer.monster;

import net.minecraft.client.renderer.entity.state.ZombieRenderState;

/** Zeichenzustand der Zombie-Familie: Art und ob sie wie eine Wasserleiche schwimmt. */
public class RevenantRenderState extends ZombieRenderState {
    public int look;
    public boolean drowned;
}
