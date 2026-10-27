package io.github.sirschrolli.kingdomsatwar.client.renderer;

import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

/** Was das Modell eines Bürgers oder Dorfbewohners für ein Bild braucht. */
public class CitizenRenderState extends HumanoidRenderState {
    public @Nullable Identifier texture;
    /** Getragene Zusatzteile (Bitmaske, siehe {@link io.github.sirschrolli.kingdomsatwar.client.model.CitizenModel}). */
    public int parts;
    public boolean feminine;
    public boolean working;
    /** Hält bei der Arbeit einen Hammer bereit (Schmiede, Steinmetz). */
    public boolean hammer;
}
