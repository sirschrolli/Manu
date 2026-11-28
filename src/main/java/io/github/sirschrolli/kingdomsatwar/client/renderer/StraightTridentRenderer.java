package io.github.sirschrolli.kingdomsatwar.client.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownTridentRenderer;
import net.minecraft.client.renderer.entity.state.ThrownTridentRenderState;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;

/**
 * Der geworfene Dreizack fliegt schnurgerade mit den Zinken voran: Seine Ausrichtung kommt aus der Flugbahn (siehe
 * {@link FlightAim}), nicht aus der nachhinkenden Drehung des Geschosses – auch beim Wurf aus dem Laufen und im Bogen.
 * Der heimkehrende Dreizack (Treue) fliegt wie bei Minecraft mit dem Griff voran, aber genau auf seiner Bahn.
 */
public class StraightTridentRenderer extends ThrownTridentRenderer {
    private final float[] aim = new float[2];

    public StraightTridentRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void extractRenderState(ThrownTrident entity, ThrownTridentRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        FlightAim.aim(entity, partialTicks, entity.isNoPhysics(), false, this.aim);
        state.yRot = this.aim[0];
        state.xRot = this.aim[1];
    }
}
