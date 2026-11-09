package io.github.sirschrolli.kingdomsatwar.client.glitch;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.RandomSource;

/**
 * Ein Fehlerpixel: ein kleines, leuchtendes Quadrat in Rot, Grün, Blau, Magenta, Cyan, Gelb oder Weiß. Es fliegt mit dem
 * Schwung, mit dem es entsteht, wird schnell gebremst, schwebt ein wenig auf und flackert – manchmal ist es für einen Tick
 * fort, manchmal springt es in eine andere Farbe. Immer hell.
 */
public class GlitchPixelParticle extends SingleQuadParticle {
    private static final int[] COLORS = {0xFF2A3A, 0x3AFF6A, 0x3A7AFF, 0xF828F0, 0x28E8F8, 0xFFE02A, 0xFFFFFF};

    protected GlitchPixelParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, SpriteSet sprites) {
        super(level, x, y, z, 0, 0, 0, sprites.get(level.getRandom()));
        this.xd = xd;
        this.yd = yd;
        this.zd = zd;
        this.friction = 0.86f;
        this.gravity = -0.004f;
        this.hasPhysics = false;
        this.lifetime = 16 + this.random.nextInt(22);
        this.quadSize = 0.05f + this.random.nextFloat() * 0.06f;
        this.pickColor();
    }

    private void pickColor() {
        int color = COLORS[this.random.nextInt(COLORS.length)];
        this.setColor(((color >> 16) & 0xFF) / 255.0f, ((color >> 8) & 0xFF) / 255.0f, (color & 0xFF) / 255.0f);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.removed) {
            return;
        }
        // flackern: für einen Tick fort, oder in einer anderen Farbe
        float roll = this.random.nextFloat();
        this.alpha = roll < 0.12f ? 0.0f : 1.0f;
        if (roll > 0.94f) {
            this.pickColor();
        }
        float left = this.lifetime - this.age;
        if (left < 6 && this.alpha > 0.0f) {
            this.alpha = left / 6.0f;
        }
    }

    @Override
    public int getLightCoords(float partial) {
        return LightCoordsUtil.FULL_BRIGHT;
    }

    @Override
    protected SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }

    public record Provider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType options, ClientLevel level, double x, double y, double z, double xd, double yd,
                double zd, RandomSource random) {
            return new GlitchPixelParticle(level, x, y, z, xd, yd, zd, this.sprites);
        }
    }
}
