package io.github.sirschrolli.kingdomsatwar.client.renderer.spell;

import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.magic.fx.SpellFxEntity;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** Was der Renderer einer Zauberwirkung braucht (siehe {@link SpellFxEntity}). */
public class SpellFxRenderState extends EntityRenderState {
    public SpellFxEntity.Kind kind = SpellFxEntity.Kind.CIRCLE;
    public @Nullable DragonType element;
    public int color = 0xFFFFFF;
    /** Ticks seit dem Erscheinen (mit Teiltick). */
    public float age;
    public int life = 20;
    /** 0–1 über die Lebensdauer. */
    public float progress;
    public float size = 1.0f;
    public float param;
    /** Endpunkt eines Strahls relativ zum Ursprung der Darstellung. */
    public Vec3 end = Vec3.ZERO;
    /** Verschiebung zum verankerten Wesen (folgt ihm ruckelfrei). */
    public Vec3 offset = Vec3.ZERO;
    /** Höhe des verankerten Wesens (Wasserblase). */
    public float anchorHeight = 1.8f;
    public float yaw;
    /** Fester Zufall je Wirkung (Neigung der Felsspitze, Form des Blitzes). */
    public int seed;
}
