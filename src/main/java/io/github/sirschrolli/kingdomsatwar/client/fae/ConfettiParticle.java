package io.github.sirschrolli.kingdomsatwar.client.fae;

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
 * Konfetti: bunte Schnipsel (Streifen, Quadrate, Sterne, Kreise) in Magenta, Gold, Türkis, Violett, Rosa und Weiß. Sie
 * fliegen aus dem Knall heraus, werden von der Luft gebremst und schweben dann taumelnd zu Boden – dabei drehen sie sich
 * und blitzen auf, wenn sie das Licht fangen. Immer hell (sie glitzern auch in der Nacht).
 */
public class ConfettiParticle extends SingleQuadParticle {
    private static final int[] COLORS = {0xFF4AB8, 0xFFD24A, 0x4AE0E8, 0xA86AFF, 0xFF9AD8, 0xFFFFFF, 0x7AF0A0, 0xFF7A4A};

    private final float spinSpeed;
    private final float flutter;
    private final float phase;
    private final float baseR;
    private final float baseG;
    private final float baseB;

    protected ConfettiParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, SpriteSet sprites) {
        super(level, x, y, z, 0, 0, 0, sprites.get(level.getRandom()));
        this.xd = xd;
        this.yd = yd + 0.04;
        this.zd = zd;
        this.friction = 0.9f;
        this.gravity = 0.045f;
        this.hasPhysics = true;
        this.lifetime = 70 + this.random.nextInt(50);
        this.quadSize = 0.045f + this.random.nextFloat() * 0.035f;
        this.roll = this.random.nextFloat() * Mth.TWO_PI;
        this.oRoll = this.roll;
        this.spinSpeed = (this.random.nextFloat() - 0.5f) * 0.6f;
        this.flutter = 0.02f + this.random.nextFloat() * 0.03f;
        this.phase = this.random.nextFloat() * Mth.TWO_PI;
        int color = COLORS[this.random.nextInt(COLORS.length)];
        this.baseR = ((color >> 16) & 0xFF) / 255.0f;
        this.baseG = ((color >> 8) & 0xFF) / 255.0f;
        this.baseB = (color & 0xFF) / 255.0f;
        this.setColor(this.baseR, this.baseG, this.baseB);
    }

    @Override
    public void tick() {
        // nach dem Knall: erst schnell, dann schwebend – die Luft bremst stark, sobald es fällt
        if (this.age > 6) {
            this.friction = 0.82f;
            this.gravity = 0.06f;
        }
        super.tick();
        if (this.removed) {
            return;
        }
        this.oRoll = this.roll;
        this.roll += this.spinSpeed;
        // taumeln: seitlich hin und her
        if (this.age > 6 && !this.onGround) {
            float w = this.age * 0.35f + this.phase;
            this.xd += Mth.sin(w) * this.flutter * 0.25f;
            this.zd += Mth.cos(w * 0.8f) * this.flutter * 0.25f;
        }
        // im Licht aufblitzen (die Schnipsel drehen sich)
        float glint = 0.75f + 0.25f * Mth.sin(this.age * 0.6f + this.phase);
        this.setColor(this.baseR * glint, this.baseG * glint, this.baseB * glint);
        // am Boden liegen bleiben und verblassen
        if (this.onGround) {
            this.xd *= 0.5;
            this.zd *= 0.5;
        }
        float left = this.lifetime - this.age;
        if (left < 15) {
            this.alpha = left / 15.0f;
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

    /** Erzeuger: jedes Teilchen bekommt eine zufällige Form und Farbe. */
    public record Provider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType options, ClientLevel level, double x, double y, double z, double xd, double yd,
                double zd, RandomSource random) {
            return new ConfettiParticle(level, x, y, z, xd, yd, zd, this.sprites);
        }
    }
}
