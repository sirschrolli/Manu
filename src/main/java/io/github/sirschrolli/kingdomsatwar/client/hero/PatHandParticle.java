package io.github.sirschrolli.kingdomsatwar.client.hero;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

/**
 * Die Hand beim Streicheln: fünf Bilder in acht Ticks (erhoben, herab, liegt auf, drückt, hebt sich), immer aufrecht und
 * zur Kamera gedreht, über dem Kopf des Wesens – sie folgt ihm, wenn es sich bewegt. Die Finger liegen über der Mitte des
 * Kopfes, der Ärmel schaut zur Seite heraus. Größe nach der Breite des Wesens.
 */
public class PatHandParticle extends SingleQuadParticle {
    public static final int LIFETIME = 8;
    private static @Nullable SpriteSet sprites;

    private final SpriteSet set;
    private final @Nullable Entity target;

    PatHandParticle(ClientLevel level, double x, double y, double z, SpriteSet set, @Nullable Entity target) {
        super(level, x, y, z, set.first());
        this.set = set;
        this.target = target;
        this.lifetime = LIFETIME;
        this.gravity = 0.0f;
        this.hasPhysics = false;
        this.xd = 0.0;
        this.yd = 0.0;
        this.zd = 0.0;
        float width = target == null ? 0.6f : target.getBbWidth();
        this.quadSize = 0.3f * Mth.clamp(width / 0.6f, 0.75f, 3.2f);
        this.follow();
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.setSpriteFromAge(set);
    }

    /** Für die Streicheln-Nachricht: eine Hand über {@code target}; null, solange die Bilder noch nicht geladen sind. */
    public static @Nullable PatHandParticle over(ClientLevel level, Entity target) {
        SpriteSet set = sprites;
        if (set == null) {
            return null;
        }
        return new PatHandParticle(level, target.getX(), target.getY() + target.getBbHeight(), target.getZ(), set, target);
    }

    private void follow() {
        if (this.target != null) {
            // die Unterkante der Hand (Bildzeile 20 von 32) liegt auf dem Kopf
            this.setPos(this.target.getX(), this.target.getY() + this.target.getBbHeight() + this.quadSize * 0.25, this.target.getZ());
        }
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime || this.target != null && this.target.isRemoved()) {
            this.remove();
            return;
        }
        this.follow();
        this.setSpriteFromAge(this.set);
    }

    @Override
    public SingleQuadParticle.FacingCameraMode getFacingCameraMode() {
        return SingleQuadParticle.FacingCameraMode.LOOKAT_Y;
    }

    /** Die Finger (rechts im Bild) sollen über der Mitte des Kopfes liegen: das Bild ein Stück nach links rücken. */
    @Override
    protected void extractRotatedQuad(QuadParticleRenderState state, Quaternionf rotation, float x, float y, float z, float partial) {
        Vector3f right = rotation.transform(new Vector3f(1.0f, 0.0f, 0.0f));
        float shift = -this.quadSize * 0.5f;
        super.extractRotatedQuad(state, rotation, x + right.x * shift, y + right.y * shift, z + right.z * shift, partial);
    }

    @Override
    protected SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.OPAQUE;
    }

    /** Erzeuger (für den Befehl /particle: eine stehende Hand); merkt sich die Bilder für {@link #over}. */
    public record Provider(SpriteSet set) implements ParticleProvider<SimpleParticleType> {
        public Provider {
            sprites = set;
        }

        @Override
        public Particle createParticle(SimpleParticleType options, ClientLevel level, double x, double y, double z, double xd, double yd, double zd,
                RandomSource random) {
            return new PatHandParticle(level, x, y, z, this.set, null);
        }
    }
}
