package io.github.sirschrolli.kingdomsatwar.client.hero;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

/**
 * Die Hand beim Streicheln (die Hand aus PatPat): fünf Bilder in 240 Millisekunden, zur Kamera gedreht, 0,85 Blöcke groß,
 * die Fingerspitzen auf dem Kopf – und weil das Wesen beim Tätscheln niedriger wird, geht sie mit ihm hinunter. Bild und
 * Lage werden in jedem Bild neu berechnet, damit die kurze Bewegung flüssig ist.
 */
public class PatHandParticle extends SingleQuadParticle {
    /** So groß ist die Hand (Blöcke). */
    private static final float SIZE = 0.85f;
    /** So weit reicht das Bild unter den Scheitel (die Finger liegen auf dem Kopf). */
    private static final float FINGERS = 0.14f;
    private static @Nullable SpriteSet sprites;

    private final SpriteSet set;
    private final @Nullable Entity target;
    private final long start;

    PatHandParticle(ClientLevel level, double x, double y, double z, SpriteSet set, @Nullable Entity target) {
        super(level, x, y, z, set.first());
        this.set = set;
        this.target = target;
        this.start = Util.getMillis();
        this.lifetime = (int) (PatClient.DURATION_MS / 50L) + 2;
        this.gravity = 0.0f;
        this.hasPhysics = false;
        this.xd = 0.0;
        this.yd = 0.0;
        this.zd = 0.0;
        this.quadSize = SIZE / 2.0f;
    }

    /** Eine Hand über {@code target}; null, solange die Bilder noch nicht geladen sind. */
    public static @Nullable PatHandParticle over(ClientLevel level, Entity target) {
        SpriteSet set = sprites;
        if (set == null) {
            return null;
        }
        return new PatHandParticle(level, target.getX(), target.getY() + target.getBbHeight(), target.getZ(), set, target);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (PatClient.devProgress == null && this.age++ >= this.lifetime || this.target != null && this.target.isRemoved()) {
            this.remove();
            return;
        }
        if (this.target != null) {
            this.setPos(this.target.getX(), this.target.getY() + this.target.getBbHeight(), this.target.getZ());
        }
    }

    @Override
    public void extract(QuadParticleRenderState state, Camera camera, float partial) {
        long elapsed = Util.getMillis() - this.start;
        if (elapsed > PatClient.DURATION_MS && PatClient.devProgress == null) {
            return;
        }
        float progress = PatClient.progress(elapsed);
        this.setSprite(this.set.get(Mth.clamp((int) Math.floor(5 * progress), 0, 4), 4));
        Vec3 anchor;
        if (this.target != null) {
            // die Fingerspitzen liegen auf dem Kopf; der Kopf geht beim Tätscheln mit hinunter
            Vec3 base = this.target.getPosition(partial);
            float squish = this.target instanceof net.minecraft.world.entity.LivingEntity living ? PatClient.scaleY(living, progress) : 1.0f;
            anchor = new Vec3(base.x, base.y + this.target.getBbHeight() * squish - FINGERS + SIZE / 2.0, base.z);
        } else {
            anchor = new Vec3(Mth.lerp(partial, this.xo, this.x), Mth.lerp(partial, this.yo, this.y), Mth.lerp(partial, this.zo, this.z));
        }
        Vec3 eye = camera.position();
        Quaternionf rotation = new Quaternionf();
        this.getFacingCameraMode().setRotation(rotation, camera, partial);
        this.extractRotatedQuad(state, rotation, (float) (anchor.x - eye.x), (float) (anchor.y - eye.y), (float) (anchor.z - eye.z), partial);
    }

    @Override
    protected SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.TRANSLUCENT;
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
