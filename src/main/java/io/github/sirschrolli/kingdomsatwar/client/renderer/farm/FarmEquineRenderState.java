package io.github.sirschrolli.kingdomsatwar.client.renderer.farm;

import net.minecraft.client.renderer.entity.state.EquineRenderState;

/** Zeichenzustand von Pferd, Esel und Maultier (Vanilla-Zustand plus Rasse und Packtaschen). */
public class FarmEquineRenderState extends EquineRenderState {
    public int breed;
    public boolean hasChest;
    public boolean mule;
    public boolean armored;
}
