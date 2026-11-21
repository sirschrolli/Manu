package io.github.sirschrolli.kingdomsatwar.client.renderer.monster;

import io.github.sirschrolli.kingdomsatwar.client.renderer.CitizenRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.jspecify.annotations.Nullable;

/** Zeichenzustand der untoten Dorfbewohner: wie bei den Lebenden, dazu Angriffslust, Heilung und Leucht-Textur. */
public class UndeadVillagerRenderState extends CitizenRenderState {
    public boolean isAggressive;
    public boolean isConverting;
    public @Nullable RenderType glow;
}
