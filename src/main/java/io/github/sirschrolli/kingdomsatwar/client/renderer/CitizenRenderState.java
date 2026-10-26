package io.github.sirschrolli.kingdomsatwar.client.renderer;

import io.github.sirschrolli.kingdomsatwar.economy.town.Profession;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

/** Was das Modell eines Bürgers für ein Bild braucht. */
public class CitizenRenderState extends HumanoidRenderState {
    public @Nullable Identifier texture;
    public Profession profession = Profession.BUILDER;
    public boolean feminine;
    public boolean working;
}
