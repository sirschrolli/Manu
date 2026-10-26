package io.github.sirschrolli.kingdomsatwar.client.boss;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

/**
 * Große, animierte Odem-Partikel der Drachen-Bosse. Sie wachsen beim Fliegen, werden langsamer, wechseln ihr Bild
 * (z. B. von heller Flamme zu dunklem Rauch) und blenden aus. Leuchtende Arten (Feuer, Funken) sind immer hell.
 */
public class BossParticle extends SingleQuadParticle {
    /** Verhalten einer Partikelart. */
    public record Style(int minLife, int maxLife, float startSize, float endSize, float friction, float gravity, boolean emissive,
            boolean translucent, float spin) {}

    public static final Style FLAME = new Style(16, 26, 0.55f, 2.4f, 0.9f, -0.012f, true, false, 0.12f);
    public static final Style FROST = new Style(20, 32, 0.45f, 1.9f, 0.9f, 0.004f, false, true, 0.05f);
    public static final Style SPARK = new Style(5, 10, 0.35f, 0.7f, 0.86f, 0.0f, true, false, 0.4f);
    public static final Style SPORE = new Style(30, 50, 0.35f, 1.6f, 0.92f, -0.004f, false, true, 0.03f);
    public static final Style DUST = new Style(20, 32, 0.5f, 2.2f, 0.88f, 0.012f, false, true, 0.05f);
    public static final Style WATER = new Style(12, 20, 0.45f, 1.6f, 0.9f, 0.035f, false, true, 0.08f);
    public static final Style SHADOW = new Style(22, 36, 0.55f, 2.6f, 0.9f, -0.006f, false, true, 0.06f);

    private final SpriteSet sprites;
    private final Style style;
    private final float spinSpeed;

    protected BossParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, SpriteSet sprites,
            Style style) {
        super(level, x, y, z, 0, 0, 0, sprites.first());
        this.sprites = sprites;
        this.style = style;
        this.xd = xd;
        this.yd = yd;
        this.zd = zd;
        this.friction = style.friction();
        this.gravity = style.gravity();
        this.hasPhysics = false;
        this.lifetime = style.minLife() + this.random.nextInt(Math.max(1, style.maxLife() - style.minLife() + 1));
        this.quadSize = style.startSize() * (0.8f + this.random.nextFloat() * 0.4f);
        this.roll = this.random.nextFloat() * Mth.TWO_PI;
        this.oRoll = this.roll;
        this.spinSpeed = (this.random.nextFloat() - 0.5f) * style.spin();
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.removed) {
            this.setSpriteFromAge(this.sprites);
            this.oRoll = this.roll;
            this.roll += this.spinSpeed;
            float t = (float) this.age / this.lifetime;
            if (this.style.translucent()) {
                this.alpha = t < 0.15f ? t / 0.15f * 0.85f : 0.85f * (1.0f - Math.max(0.0f, (t - 0.55f) / 0.45f));
            }
        }
    }

    @Override
    public float getQuadSize(float partial) {
        float t = Mth.clamp((this.age + partial) / this.lifetime, 0.0f, 1.0f);
        float grow = 1.0f + (this.style.endSize() / this.style.startSize() - 1.0f) * (1.0f - (1.0f - t) * (1.0f - t));
        return this.quadSize * grow;
    }

    @Override
    public int getLightCoords(float partial) {
        return this.style.emissive() ? LightCoordsUtil.FULL_BRIGHT : super.getLightCoords(partial);
    }

    @Override
    protected SingleQuadParticle.Layer getLayer() {
        return this.style.translucent() ? SingleQuadParticle.Layer.TRANSLUCENT : SingleQuadParticle.Layer.OPAQUE;
    }

    /** Erzeuger für eine Partikelart. */
    public record Provider(SpriteSet sprites, Style style) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType options, ClientLevel level, double x, double y, double z, double xd, double yd,
                double zd, RandomSource random) {
            return new BossParticle(level, x, y, z, xd, yd, zd, this.sprites, this.style);
        }
    }
}
