package io.github.sirschrolli.kingdomsatwar.client.renderer;

import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

/** Was das Modell von Soldaten und Banditen für ein Bild braucht. */
public class KingdomHumanoidRenderState extends HumanoidRenderState {
    public @Nullable Identifier texture;
    /** Fortschritt der Gesten (0 = keine). */
    public float salute;
    public float cheer;
    /** Zusatzteile am Modell. */
    public boolean cape;
    public boolean plume;
    public boolean quiver;
    /** Wie weit der Umhang nach hinten weht (Grad) und wie er flattert. */
    public float capeLean;
    public float capeFlap;
}
