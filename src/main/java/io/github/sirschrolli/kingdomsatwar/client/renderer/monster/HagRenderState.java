package io.github.sirschrolli.kingdomsatwar.client.renderer.monster;

import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

/** Zeichenzustand der Hexe: Art, ob sie einen Trank hält und ob sie trinkt. */
public class HagRenderState extends HumanoidRenderState {
    public int look;
    public boolean holdingPotion;
    public boolean drinking;
    /** Wie weit die Flasche gerade zum Mund geführt ist (0..1, weich). */
    public float drink;
}
