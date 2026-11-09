package io.github.sirschrolli.kingdomsatwar.client.renderer.spell;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.client.model.spell.SpellFxModels;
import io.github.sirschrolli.kingdomsatwar.magic.fx.SpellFxEntity;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

/**
 * Zeichnet die Zauberwirkungen ({@link SpellFxEntity}): leuchtende Runenkreise mit wachsender Füllung, Lichtstrahlen
 * und zuckende Blitzbögen (additiv, sie überstrahlen), Säulen aus aufsteigenden Flammenzungen, eine gekrümmte
 * Flutwelle mit Gischtkamm, schillernde Wasserblasen, gleißende Mondsicheln, Schwarze Löcher (dunkler Kern, Lichtring,
 * wirbelnde Scheibe) und Bebenringe – und die Modelle der Felsspitze, Steinfaust, Dornenranke, Giftblüte und des
 * Eiskerkers mit ihren Leuchtebenen.
 */
public class SpellFxRenderer extends EntityRenderer<SpellFxEntity, SpellFxRenderState> {
    private static final int FULL_BRIGHT = 0xF000F0;
    private static final Identifier RING = KingdomsAtWar.id("textures/entity/spell/ring.png");
    private static final Identifier FILL = KingdomsAtWar.id("textures/entity/spell/fill.png");
    private static final Identifier BEAM = KingdomsAtWar.id("textures/entity/spell/beam.png");
    private static final Identifier PILLAR = KingdomsAtWar.id("textures/entity/spell/pillar.png");
    private static final Identifier WAVE = KingdomsAtWar.id("textures/entity/spell/wave.png");
    private static final Identifier BUBBLE = KingdomsAtWar.id("textures/entity/spell/bubble.png");
    private static final Identifier DISK = KingdomsAtWar.id("textures/entity/spell/disk.png");
    private static final Identifier CORE = KingdomsAtWar.id("textures/entity/spell/core.png");
    private static final Identifier LENS = KingdomsAtWar.id("textures/entity/spell/lens.png");
    private static final Identifier HALO = KingdomsAtWar.id("textures/entity/mage/missile.png");
    private static final Identifier SIGIL = KingdomsAtWar.id("textures/entity/mage/sigil.png");
    private static final Identifier GLYPHS = KingdomsAtWar.id("textures/block/magic_table_glyphs.png");
    private static final RenderType RING_TYPE = RenderTypes.entityTranslucentEmissive(RING);
    private static final RenderType FILL_TYPE = RenderTypes.entityTranslucentEmissive(FILL);
    private static final RenderType SIGIL_TYPE = RenderTypes.entityTranslucentEmissive(SIGIL);
    private static final RenderType GLYPH_TYPE = RenderTypes.entityTranslucentEmissive(GLYPHS);
    private static final RenderType WAVE_TYPE = RenderTypes.entityTranslucent(WAVE);
    private static final RenderType BUBBLE_TYPE = RenderTypes.entityTranslucentEmissive(BUBBLE);
    private static final RenderType CORE_TYPE = RenderTypes.entityTranslucent(CORE);

    private final SpellFxModels.Spike spike;
    private final SpellFxModels.Fist fist;
    private final SpellFxModels.Vine vine;
    private final SpellFxModels.Flower flower;
    private final SpellFxModels.Prison prison;

    public SpellFxRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0f;
        this.spike = new SpellFxModels.Spike(context.bakeLayer(ModModelLayers.STONE_SPIKE));
        this.fist = new SpellFxModels.Fist(context.bakeLayer(ModModelLayers.STONE_FIST));
        this.vine = new SpellFxModels.Vine(context.bakeLayer(ModModelLayers.THORN_VINE));
        this.flower = new SpellFxModels.Flower(context.bakeLayer(ModModelLayers.POISON_FLOWER));
        this.prison = new SpellFxModels.Prison(context.bakeLayer(ModModelLayers.ICE_PRISON));
    }

    private static Identifier texture(String id) {
        return KingdomsAtWar.id("textures/entity/spell/" + id + "/" + id + ".png");
    }

    private static Identifier glow(String id) {
        return KingdomsAtWar.id("textures/entity/spell/" + id + "/" + id + "_glow.png");
    }

    @Override
    public SpellFxRenderState createRenderState() {
        return new SpellFxRenderState();
    }

    @Override
    public void extractRenderState(SpellFxEntity entity, SpellFxRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.kind = entity.kind();
        state.element = entity.element();
        state.color = entity.color();
        state.life = entity.life();
        state.age = entity.tickCount + partialTicks;
        state.progress = Mth.clamp(state.age / state.life, 0.0f, 1.0f);
        state.size = entity.size();
        state.param = entity.param();
        state.end = entity.endOffset();
        state.yaw = entity.getYRot();
        state.seed = entity.getId();
        Entity anchor = entity.anchor();
        if (anchor != null) {
            state.offset = anchor.getPosition(partialTicks).subtract(entity.getPosition(partialTicks));
            state.anchorHeight = anchor.getBbHeight();
        } else {
            state.offset = Vec3.ZERO;
        }
    }

    @Override
    protected AABB getBoundingBoxForCulling(SpellFxEntity entity, float partialTicks) {
        return entity.cullingBox();
    }

    @Override
    protected int getBlockLightLevel(SpellFxEntity entity, BlockPos pos) {
        return Math.max(super.getBlockLightLevel(entity, pos), 7);
    }

    @Override
    public void submit(SpellFxRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(state.offset.x, state.offset.y, state.offset.z);
        switch (state.kind) {
            case CIRCLE -> this.circle(state, poseStack, collector);
            case CAST_CIRCLE -> this.castCircle(state, poseStack, collector);
            case BEAM -> this.beam(state, poseStack, collector, camera);
            case ARC -> this.arc(state, poseStack, collector);
            case PILLAR -> this.pillar(state, poseStack, collector);
            case WAVE -> this.wave(state, poseStack, collector);
            case CRESCENT -> this.crescent(state, poseStack, collector);
            case VORTEX -> this.vortex(state, poseStack, collector, camera);
            case SHOCKWAVE -> this.shockwave(state, poseStack, collector);
            case BUBBLE -> this.bubble(state, poseStack, collector, camera);
            case SPIKE -> this.model(this.spike, "stone_spike", false, state, poseStack, collector, 1.0f);
            case FIST -> this.model(this.fist, "stone_fist", false, state, poseStack, collector, 1.0f);
            case VINE -> this.model(this.vine, "thorn_vine", false, state, poseStack, collector, 1.0f);
            case FLOWER -> this.model(this.flower, "poison_flower", false, state, poseStack, collector, 1.0f);
            case PRISON -> this.model(this.prison, "ice_prison", true, state, poseStack, collector,
                    Mth.clamp((state.life - state.age) / 3.0f, 0.0f, 1.0f));
        }
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }

    // =====================================================================================
    // Runenkreise
    // =====================================================================================

    /** Warnkreis: der Ring dreht sich, innen wächst die Füllung, bis es einschlägt. */
    private void circle(SpellFxRenderState s, PoseStack poseStack, SubmitNodeCollector collector) {
        float appear = ease(s.age / 4.0f);
        float radius = s.size * (0.65f + 0.35f * appear);
        int rgb = ARGB.srgbLerp(0.2f, s.color, 0xFFFFFF);
        float flash = s.life - s.age < 2.5f ? 1.0f : 0.0f;
        float pulse = 0.85f + 0.15f * Mth.sin(s.age * (0.4f + s.progress * 0.8f));
        int ringColor = ARGB.color(Math.round(255 * appear * Math.max(pulse, flash)), rgb);
        int fillColor = ARGB.color(Math.round(255 * appear * (0.35f + 0.45f * s.progress + 0.2f * flash)), s.color);
        float fill = Math.max(0.05f, s.progress);
        poseStack.pushPose();
        poseStack.translate(0.0f, 0.03f, 0.0f);
        poseStack.scale(2 * radius * fill, 1.0f, 2 * radius * fill);
        collector.submitCustomGeometry(poseStack, FILL_TYPE, (pose, buffer) -> flatQuad(pose, buffer, fillColor, FULL_BRIGHT));
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0.0f, 0.04f, 0.0f);
        poseStack.rotate(Axis.YP.rotationDegrees(s.age * 2.5f + s.seed * 37));
        poseStack.scale(2 * radius, 1.0f, 2 * radius);
        collector.submitCustomGeometry(poseStack, RING_TYPE, (pose, buffer) -> flatQuad(pose, buffer, ringColor, FULL_BRIGHT));
        poseStack.popPose();
    }

    /** Zauberkreis unter dem Zaubernden: wächst und dreht sich schneller, je stärker die Ladung. */
    private void castCircle(SpellFxRenderState s, PoseStack poseStack, SubmitNodeCollector collector) {
        float charge = Mth.clamp(s.param, 0.0f, 1.0f);
        float appear = ease(s.age / 5.0f);
        float radius = s.size * (0.5f + 0.5f * charge) * appear;
        int rgb = ARGB.srgbLerp(0.15f, s.color, 0xFFFFFF);
        int sigil = ARGB.color(Math.round(255 * appear * (0.45f + 0.45f * charge)), rgb);
        poseStack.pushPose();
        poseStack.translate(0.0f, 0.05f, 0.0f);
        poseStack.pushPose();
        poseStack.rotate(Axis.YP.rotationDegrees(s.age * (1.5f + 9.0f * charge)));
        poseStack.scale(2 * radius, 1.0f, 2 * radius);
        collector.submitCustomGeometry(poseStack, SIGIL_TYPE, (pose, buffer) -> flatQuad(pose, buffer, sigil, FULL_BRIGHT));
        poseStack.popPose();
        // Runenkranz, gegenläufig
        int glyphs = ARGB.color(Math.round(255 * appear * (0.25f + 0.7f * charge)), rgb);
        for (int i = 0; i < 10; i++) {
            int glyph = (i * 3 + s.seed) & 7;
            poseStack.pushPose();
            poseStack.rotate(Axis.YP.rotationDegrees(-s.age * (1.0f + 5.0f * charge) + i * 36.0f));
            poseStack.translate(0.0f, 0.01f, radius * 1.18f);
            poseStack.scale(radius * 0.3f, 1.0f, radius * 0.3f);
            collector.submitCustomGeometry(poseStack, GLYPH_TYPE, (pose, buffer) -> flatGlyph(pose, buffer, glyph, glyphs));
            poseStack.popPose();
        }
        if (charge >= 0.999f) {
            float beat = 1.0f + 0.08f * Mth.sin(s.age * 0.9f);
            int ring = ARGB.color(220, rgb);
            poseStack.rotate(Axis.YP.rotationDegrees(s.age * 4.0f));
            poseStack.scale(2 * radius * 1.32f * beat, 1.0f, 2 * radius * 1.32f * beat);
            collector.submitCustomGeometry(poseStack, RING_TYPE, (pose, buffer) -> flatQuad(pose, buffer, ring, FULL_BRIGHT));
        }
        poseStack.popPose();
    }

    // =====================================================================================
    // Strahl und Blitzbogen
    // =====================================================================================

    private void beam(SpellFxRenderState s, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        double length = s.end.length();
        if (length < 0.05) {
            return;
        }
        float intensity = Math.min(1.0f, s.age / 1.5f) * Mth.clamp((s.life - s.age) / (s.life * 0.55f), 0.0f, 1.0f);
        if (intensity <= 0.01f) {
            return;
        }
        float len = (float) length;
        float width = s.size;
        RenderType type = RenderTypes.energySwirl(BEAM, 0.0f, -s.age * 0.18f);
        int glow = scale(s.color, 0.8f * intensity);
        int core = scale(ARGB.srgbLerp(0.65f, s.color, 0xFFFFFF), intensity);
        float repeat = len / Math.max(0.5f, width * 5.0f);
        poseStack.pushPose();
        Vec3 d = s.end.scale(1.0 / length);
        poseStack.rotate(new Quaternionf().rotationTo(0.0f, 1.0f, 0.0f, (float) d.x, (float) d.y, (float) d.z));
        collector.submitCustomGeometry(poseStack, type, (pose, buffer) -> cross(pose, buffer, len, width * 2.4f, repeat, glow));
        collector.submitCustomGeometry(poseStack, type, (pose, buffer) -> cross(pose, buffer, len, width * 0.8f, repeat, core));
        poseStack.popPose();
        // Schein an Stab und Ziel
        RenderType halo = RenderTypes.energySwirl(HALO, 0.0f, 0.0f);
        for (int end = 0; end < 2; end++) {
            Vec3 at = end == 0 ? Vec3.ZERO : s.end;
            float size = width * (end == 0 ? 3.0f : 4.5f) * (1.0f + 0.15f * Mth.sin(s.age * 1.3f + end));
            int col = scale(ARGB.srgbLerp(0.4f, s.color, 0xFFFFFF), intensity);
            poseStack.pushPose();
            poseStack.translate(at.x, at.y, at.z);
            poseStack.rotate(camera.orientation);
            poseStack.scale(size, size, size);
            collector.submitCustomGeometry(poseStack, halo, (pose, buffer) -> billboard(pose, buffer, col));
            poseStack.popPose();
        }
    }

    /** Zuckender Blitzbogen: alle zwei Ticks eine neue Form, ein heller Kern in einem weiten Schein, dazu Seitenäste. */
    private void arc(SpellFxRenderState s, PoseStack poseStack, SubmitNodeCollector collector) {
        double length = s.end.length();
        if (length < 0.05) {
            return;
        }
        float intensity = Math.min(1.0f, s.age / 1.0f) * Mth.clamp((s.life - s.age) / 3.0f, 0.0f, 1.0f);
        if (intensity <= 0.01f) {
            return;
        }
        RandomSource random = RandomSource.create(s.seed * 31L + (long) (s.age / 2.0f));
        int segments = Math.max(4, (int) (length / 0.6));
        Vec3[] points = new Vec3[segments + 1];
        Vec3 dir = s.end.scale(1.0 / length);
        Vec3 a = Math.abs(dir.y) < 0.9 ? dir.cross(new Vec3(0, 1, 0)).normalize() : dir.cross(new Vec3(1, 0, 0)).normalize();
        Vec3 b = dir.cross(a).normalize();
        double amp = Math.min(0.7, length * 0.09);
        for (int i = 0; i <= segments; i++) {
            Vec3 base = s.end.scale(i / (double) segments);
            if (i > 0 && i < segments) {
                double taper = Math.sin(Math.PI * i / segments);
                base = base.add(a.scale((random.nextDouble() - 0.5) * 2 * amp * taper)).add(b.scale((random.nextDouble() - 0.5) * 2 * amp * taper));
            }
            points[i] = base;
        }
        int glow = ARGB.color(Math.round(110 * intensity), s.color);
        int core = ARGB.color(Math.round(240 * intensity), ARGB.srgbLerp(0.75f, s.color, 0xFFFFFF));
        float width = s.size;
        RenderType type = RenderTypes.lightning();
        collector.submitCustomGeometry(poseStack, type, (pose, buffer) -> {
            polyline(pose, buffer, points, width * 3.2f, glow);
            polyline(pose, buffer, points, width, core);
        });
        // zwei Seitenäste
        for (int k = 0; k < 2; k++) {
            int from = 1 + random.nextInt(Math.max(1, segments - 2));
            Vec3[] branch = new Vec3[4];
            branch[0] = points[from];
            Vec3 bdir = dir.add(a.scale((random.nextDouble() - 0.5) * 1.6)).add(b.scale((random.nextDouble() - 0.5) * 1.6)).normalize();
            double step = Math.min(0.9, length * 0.08);
            for (int i = 1; i < branch.length; i++) {
                branch[i] = branch[i - 1].add(bdir.scale(step)).add(a.scale((random.nextDouble() - 0.5) * step)).add(b.scale((random.nextDouble() - 0.5) * step));
            }
            int bc = ARGB.color(Math.round(160 * intensity), ARGB.srgbLerp(0.5f, s.color, 0xFFFFFF));
            collector.submitCustomGeometry(poseStack, type, (pose, buffer) -> polyline(pose, buffer, branch, width * 0.6f, bc));
        }
    }

    // =====================================================================================
    // Säule, Welle, Blase
    // =====================================================================================

    /** Säule aus aufsteigenden Flammenzungen (Feuer orange, Wasser blau, Aether violett). */
    private void pillar(SpellFxRenderState s, PoseStack poseStack, SubmitNodeCollector collector) {
        float rise = ease(s.age / 4.0f);
        float fade = Mth.clamp((s.life - s.age) / 7.0f, 0.0f, 1.0f);
        float intensity = rise * fade;
        if (intensity <= 0.01f) {
            return;
        }
        float height = s.param * rise * (0.7f + 0.3f * fade);
        float radius = s.size * (0.9f + 0.1f * Mth.sin(s.age * 0.9f)) * (0.6f + 0.4f * fade);
        RenderType type = RenderTypes.energySwirl(PILLAR, 0.0f, s.age * 0.07f);
        int outer = scale(s.color, 0.85f * intensity);
        int inner = scale(ARGB.srgbLerp(0.55f, s.color, 0xFFFFFF), intensity);
        collector.submitCustomGeometry(poseStack, type, (pose, buffer) -> {
            tube(pose, buffer, radius, height, outer);
            tube(pose, buffer, radius * 0.55f, height * 1.05f, inner);
        });
        // Schein am Fuß
        RenderType halo = RenderTypes.energySwirl(HALO, 0.0f, 0.0f);
        int base = scale(s.color, 0.7f * intensity);
        poseStack.pushPose();
        poseStack.translate(0.0f, 0.05f, 0.0f);
        poseStack.scale(radius * 4.0f, 1.0f, radius * 4.0f);
        collector.submitCustomGeometry(poseStack, halo, (pose, buffer) -> flatQuad(pose, buffer, base, FULL_BRIGHT));
        poseStack.popPose();
    }

    /** Flutwelle: eine nach vorn gekrümmte Wasserwand mit Gischtkamm, die Ränder bleiben zurück. */
    private void wave(SpellFxRenderState s, PoseStack poseStack, SubmitNodeCollector collector) {
        float grow = ease(s.age / 5.0f);
        float fade = Mth.clamp((s.life - s.age) / 8.0f, 0.0f, 1.0f);
        float height = 2.7f * grow * (0.4f + 0.6f * fade);
        if (height <= 0.05f) {
            return;
        }
        float yawRad = s.yaw * Mth.DEG_TO_RAD;
        Vec3 fwd = new Vec3(-Mth.sin(yawRad), 0.0, Mth.cos(yawRad));
        Vec3 side = new Vec3(-fwd.z, 0.0, fwd.x);
        float half = s.size * 0.5f;
        int alpha = Math.round(255 * Math.min(1.0f, fade * 1.4f));
        int color = ARGB.color(alpha, 0xFFFFFF);
        float scroll = s.age * 0.05f;
        int cols = 10;
        int rows = 4;
        collector.submitCustomGeometry(poseStack, WAVE_TYPE, (pose, buffer) -> {
            for (int c = 0; c < cols; c++) {
                for (int r = 0; r < rows; r++) {
                    Vec3 p00 = wavePoint(fwd, side, half, height, c / (float) cols, r / (float) rows, s.age);
                    Vec3 p10 = wavePoint(fwd, side, half, height, (c + 1) / (float) cols, r / (float) rows, s.age);
                    Vec3 p11 = wavePoint(fwd, side, half, height, (c + 1) / (float) cols, (r + 1) / (float) rows, s.age);
                    Vec3 p01 = wavePoint(fwd, side, half, height, c / (float) cols, (r + 1) / (float) rows, s.age);
                    float u0 = c / (float) cols * 3 + scroll;
                    float u1 = (c + 1) / (float) cols * 3 + scroll;
                    float v0 = 1.0f - r / (float) rows;
                    float v1 = 1.0f - (r + 1) / (float) rows;
                    quadTwoSided(pose, buffer, p00, p10, p11, p01, u0, u1, v0, v1, color, s.lightCoords);
                }
            }
        });
    }

    /** Punkt auf der Welle: quer 0–1, hoch 0–1; die Ränder hängen zurück, oben bricht sie nach vorn. */
    private static Vec3 wavePoint(Vec3 fwd, Vec3 side, float half, float height, float across, float up, float age) {
        float x = (across - 0.5f) * 2.0f;
        double along = -0.9 * x * x + 0.7 * up * up + 0.12 * Mth.sin(age * 0.5f + x * 3.0f) * up;
        double h = height * up * (1.0 - 0.25 * x * x);
        return side.scale(x * half).add(fwd.scale(along)).add(0.0, h, 0.0);
    }

    /** Schillernde Blase um das verankerte Wesen; zum Schluss platzt sie. */
    private void bubble(SpellFxRenderState s, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        float appear = ease(s.age / 3.0f);
        float pop = Mth.clamp((s.age - (s.life - 3)) / 3.0f, 0.0f, 1.0f);
        float size = s.size * appear * (1.0f + 0.04f * Mth.sin(s.age * 0.5f)) * (1.0f + pop * 0.2f);
        int color = ARGB.color(Math.round(255 * (1.0f - pop)), ARGB.srgbLerp(0.5f, s.color, 0xFFFFFF));
        poseStack.pushPose();
        poseStack.translate(0.0f, s.anchorHeight * 0.5f, 0.0f);
        poseStack.rotate(camera.orientation);
        poseStack.rotate(Axis.ZP.rotationDegrees(Mth.sin(s.age * 0.13f) * 8.0f));
        poseStack.scale(size, size * (1.0f + 0.05f * Mth.sin(s.age * 0.37f)), size);
        collector.submitCustomGeometry(poseStack, BUBBLE_TYPE, (pose, buffer) -> billboardTwoSided(pose, buffer, color));
        poseStack.popPose();
    }

    // =====================================================================================
    // Mondsichel, Schwarzes Loch, Bebenring
    // =====================================================================================

    /**
     * Mondsichel: eine nach vorn gewölbte Klinge aus Licht – in der Mitte hoch, zu den Spitzen hin schmal –, stehend und
     * liegend (von der Seite wie von oben zu sehen), mit hellem Kern, weitem Schein und einem verblassenden Nachbild.
     */
    private void crescent(SpellFxRenderState s, PoseStack poseStack, SubmitNodeCollector collector) {
        float grow = ease(s.age / 3.0f);
        float fade = Mth.clamp((s.life - s.age) / 6.0f, 0.0f, 1.0f);
        float intensity = grow * fade;
        if (intensity <= 0.01f) {
            return;
        }
        float yawRad = s.yaw * Mth.DEG_TO_RAD;
        Vec3 fwd = new Vec3(-Mth.sin(yawRad), 0.0, Mth.cos(yawRad));
        Vec3 side = new Vec3(-fwd.z, 0.0, fwd.x);
        float half = s.size * 0.5f * (0.75f + 0.25f * grow);
        float bulge = SpellFxEntity.CRESCENT_BULGE;
        RenderType type = RenderTypes.energySwirl(BEAM, 0.0f, -s.age * 0.3f);
        int glow = scale(s.color, 0.7f * intensity);
        int core = scale(ARGB.srgbLerp(0.75f, s.color, 0xFFFFFF), intensity);
        int ghost = scale(s.color, 0.35f * intensity);
        // das Nachbild liegt ein Stück zurück (in Flugrichtung gesehen)
        Vec3 behind = fwd.scale(-Math.min(1.4, s.param * 1.8));
        collector.submitCustomGeometry(poseStack, type, (pose, buffer) -> {
            crescentBlade(pose, buffer, fwd, side, Vec3.ZERO, half, bulge, 0.95f, 0.85f, glow);
            crescentBlade(pose, buffer, fwd, side, Vec3.ZERO, half, bulge, 0.38f, 0.85f, core);
            crescentFlat(pose, buffer, fwd, side, half, bulge, 0.75f, 0.85f, glow);
            crescentFlat(pose, buffer, fwd, side, half, bulge, 0.3f, 0.86f, core);
            crescentBlade(pose, buffer, fwd, side, behind, half * 0.92f, bulge * 0.9f, 0.6f, 0.85f, ghost);
        });
    }

    /** Stehende Klinge entlang des Sichelbogens: Höhe {@code height} in der Mitte, zu den Spitzen hin null. */
    private static void crescentBlade(PoseStack.Pose pose, VertexConsumer buffer, Vec3 fwd, Vec3 side, Vec3 shift, float half, float bulge,
            float height, float center, int color) {
        int n = 16;
        for (int i = 0; i < n; i++) {
            float x0 = -1.0f + 2.0f * i / n;
            float x1 = -1.0f + 2.0f * (i + 1) / n;
            Vec3 a = crescentPoint(fwd, side, half, bulge, x0).add(shift);
            Vec3 b = crescentPoint(fwd, side, half, bulge, x1).add(shift);
            float h0 = height * (float) Math.pow(1.0f - x0 * x0, 0.6);
            float h1 = height * (float) Math.pow(1.0f - x1 * x1, 0.6);
            quadTwoSided(pose, buffer, a.add(0.0, center - h0, 0.0), a.add(0.0, center + h0, 0.0), b.add(0.0, center + h1, 0.0),
                    b.add(0.0, center - h1, 0.0), 0.0f, 1.0f, i / (float) n * 2.0f, (i + 1) / (float) n * 2.0f, color, FULL_BRIGHT);
        }
    }

    /** Liegende Sichel in Höhe {@code y}: in der Mitte {@code width} breit, zu den Spitzen hin spitz. */
    private static void crescentFlat(PoseStack.Pose pose, VertexConsumer buffer, Vec3 fwd, Vec3 side, float half, float bulge, float width,
            float y, int color) {
        int n = 16;
        for (int i = 0; i < n; i++) {
            float x0 = -1.0f + 2.0f * i / n;
            float x1 = -1.0f + 2.0f * (i + 1) / n;
            Vec3 a = crescentPoint(fwd, side, half, bulge, x0).add(0.0, y, 0.0);
            Vec3 b = crescentPoint(fwd, side, half, bulge, x1).add(0.0, y, 0.0);
            Vec3 na = crescentNormal(fwd, side, half, bulge, x0).scale(width * Math.pow(1.0f - x0 * x0, 0.8));
            Vec3 nb = crescentNormal(fwd, side, half, bulge, x1).scale(width * Math.pow(1.0f - x1 * x1, 0.8));
            // die Sichel ist vorn rund und hinten hohl: der Vorderrand reicht weiter als der Hinterrand
            quadTwoSided(pose, buffer, a.subtract(na.scale(0.8)), a.add(na.scale(0.45)), b.add(nb.scale(0.45)), b.subtract(nb.scale(0.8)),
                    0.0f, 1.0f, i / (float) n * 2.0f, (i + 1) / (float) n * 2.0f, color, FULL_BRIGHT);
        }
    }

    /** Punkt auf dem Sichelbogen ({@code x} von −1 bis 1 quer): die Mitte eilt um {@code bulge} voraus. */
    private static Vec3 crescentPoint(Vec3 fwd, Vec3 side, float half, float bulge, float x) {
        return side.scale(x * half).add(fwd.scale(bulge * (1.0f - x * x)));
    }

    /** Waagerechte Normale des Sichelbogens (zeigt nach vorn außen). */
    private static Vec3 crescentNormal(Vec3 fwd, Vec3 side, float half, float bulge, float x) {
        // Ableitung des Bogens nach x: side·half − fwd·2·bulge·x; die Normale steht senkrecht dazu
        Vec3 tangent = side.scale(half).subtract(fwd.scale(2.0f * bulge * x));
        Vec3 n = new Vec3(-tangent.z, 0.0, tangent.x).normalize();
        return n.dot(fwd) < 0.0 ? n.scale(-1.0) : n;
    }

    /**
     * Schwarzes Loch: eine wirbelnde Scheibe aus Sternenstaub (außen langsam, innen schnell, leicht gekippt), ein Schein
     * ringsum, darin der schwarze Kern mit seinem gleißenden Lichtring. Zum Schluss stürzt alles in sich zusammen.
     */
    private void vortex(SpellFxRenderState s, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        float grow = ease(s.age / 10.0f);
        float collapse = Mth.clamp((s.life - s.age) / 6.0f, 0.0f, 1.0f);
        float k = grow * collapse * collapse;
        if (k <= 0.01f) {
            return;
        }
        float radius = s.size * k;
        float pulse = 1.0f + 0.05f * Mth.sin(s.age * 0.7f);
        float coreSize = s.size * 0.24f * grow * (0.35f + 0.65f * collapse) * pulse;
        float tilt = 14.0f + 6.0f * Mth.sin(s.seed * 0.7f);
        // weiter Schein
        RenderType halo = RenderTypes.energySwirl(HALO, 0.0f, 0.0f);
        int haloColor = scale(s.color, 0.32f * k);
        poseStack.pushPose();
        poseStack.rotate(camera.orientation);
        poseStack.scale(radius * 2.6f, radius * 2.6f, radius * 2.6f);
        collector.submitCustomGeometry(poseStack, halo, (pose, buffer) -> billboard(pose, buffer, haloColor));
        poseStack.popPose();
        // die Wirbelscheibe: zwei Lagen, die innere schneller und heller
        RenderType disk = RenderTypes.energySwirl(DISK, 0.0f, 0.0f);
        int outer = scale(s.color, 0.85f * k);
        int inner = scale(ARGB.srgbLerp(0.45f, s.color, 0xFFFFFF), 0.9f * k);
        for (int layer = 0; layer < 2; layer++) {
            float r = layer == 0 ? radius : radius * 0.62f;
            float spin = s.age * (layer == 0 ? 5.0f : 11.0f) + s.seed * 23.0f;
            int col = layer == 0 ? outer : inner;
            poseStack.pushPose();
            poseStack.rotate(Axis.XP.rotationDegrees(tilt));
            poseStack.rotate(Axis.YP.rotationDegrees(-spin));
            poseStack.scale(2 * r, 1.0f, 2 * r);
            collector.submitCustomGeometry(poseStack, disk, (pose, buffer) -> flatQuad(pose, buffer, col, FULL_BRIGHT));
            poseStack.popPose();
        }
        // der schwarze Kern (zur Kamera gedreht) und sein Lichtring
        int dark = ARGB.color(Math.round(250 * Math.min(1.0f, grow * 1.3f)), 0x05020C);
        poseStack.pushPose();
        poseStack.rotate(camera.orientation);
        poseStack.pushPose();
        poseStack.scale(2 * coreSize, 2 * coreSize, 2 * coreSize);
        collector.submitCustomGeometry(poseStack, CORE_TYPE, (pose, buffer) -> billboardTwoSided(pose, buffer, dark));
        poseStack.popPose();
        RenderType lens = RenderTypes.energySwirl(LENS, 0.0f, 0.0f);
        int ring = scale(ARGB.srgbLerp(0.6f, s.color, 0xFFFFFF), k * (0.85f + 0.15f * Mth.sin(s.age * 1.1f)));
        float lensSize = coreSize * 2.0f * 1.32f;
        poseStack.scale(lensSize, lensSize, lensSize);
        collector.submitCustomGeometry(poseStack, lens, (pose, buffer) -> billboard(pose, buffer, ring));
        poseStack.popPose();
    }

    /** Bebenring: eine niedrige, gleißende Wand, die sich am Boden ausbreitet, dazu ein Schein auf dem Boden. */
    private void shockwave(SpellFxRenderState s, PoseStack poseStack, SubmitNodeCollector collector) {
        float radius = s.size + s.param * s.age;
        float appear = ease(s.age / 2.0f);
        float fade = Mth.clamp((s.life - s.age) / 6.0f, 0.0f, 1.0f);
        float intensity = appear * fade;
        if (intensity <= 0.01f || radius <= 0.05f) {
            return;
        }
        RenderType type = RenderTypes.energySwirl(BEAM, 0.0f, s.age * 0.12f);
        int glow = scale(s.color, 0.75f * intensity);
        int core = scale(ARGB.srgbLerp(0.65f, s.color, 0xFFFFFF), intensity);
        float h = SpellFxEntity.SHOCKWAVE_HEIGHT;
        collector.submitCustomGeometry(poseStack, type, (pose, buffer) -> {
            ringWall(pose, buffer, radius, 0.0f, h * 1.35f, glow);
            ringWall(pose, buffer, radius, h * 0.15f, h * 0.85f, core);
            ringFloor(pose, buffer, radius - 0.9f, radius + 0.35f, 0.04f, glow);
        });
    }

    /** Senkrechter Ring (Halbmesser {@code radius}) von {@code y0} bis {@code y1}; die Textur liegt quer (heller Kern in der Mitte). */
    private static void ringWall(PoseStack.Pose pose, VertexConsumer buffer, float radius, float y0, float y1, int color) {
        int sides = Math.max(24, Math.min(96, (int) (radius * 6.0f)));
        for (int i = 0; i < sides; i++) {
            float a0 = i * Mth.TWO_PI / sides;
            float a1 = (i + 1) * Mth.TWO_PI / sides;
            Vec3 p0 = new Vec3(Mth.cos(a0) * radius, 0.0, Mth.sin(a0) * radius);
            Vec3 p1 = new Vec3(Mth.cos(a1) * radius, 0.0, Mth.sin(a1) * radius);
            float v0 = i / (float) sides * radius * 0.6f;
            float v1 = (i + 1) / (float) sides * radius * 0.6f;
            quadTwoSided(pose, buffer, p0.add(0.0, y0, 0.0), p0.add(0.0, y1, 0.0), p1.add(0.0, y1, 0.0), p1.add(0.0, y0, 0.0), 0.0f, 1.0f,
                    v0, v1, color, FULL_BRIGHT);
        }
    }

    /** Flacher Ring am Boden (von {@code inner} bis {@code outer}). */
    private static void ringFloor(PoseStack.Pose pose, VertexConsumer buffer, float inner, float outer, float y, int color) {
        float in = Math.max(0.0f, inner);
        int sides = Math.max(24, Math.min(96, (int) (outer * 6.0f)));
        for (int i = 0; i < sides; i++) {
            float a0 = i * Mth.TWO_PI / sides;
            float a1 = (i + 1) * Mth.TWO_PI / sides;
            Vec3 i0 = new Vec3(Mth.cos(a0) * in, y, Mth.sin(a0) * in);
            Vec3 o0 = new Vec3(Mth.cos(a0) * outer, y, Mth.sin(a0) * outer);
            Vec3 o1 = new Vec3(Mth.cos(a1) * outer, y, Mth.sin(a1) * outer);
            Vec3 i1 = new Vec3(Mth.cos(a1) * in, y, Mth.sin(a1) * in);
            float v0 = i / (float) sides * outer * 0.6f;
            float v1 = (i + 1) / (float) sides * outer * 0.6f;
            quadTwoSided(pose, buffer, i0, o0, o1, i1, 0.0f, 1.0f, v0, v1, color, FULL_BRIGHT);
        }
    }

    // =====================================================================================
    // Modelle
    // =====================================================================================

    private void model(Model<SpellFxRenderState> model, String id, boolean translucent, SpellFxRenderState s, PoseStack poseStack,
            SubmitNodeCollector collector, float alpha) {
        if (alpha <= 0.01f) {
            return;
        }
        poseStack.pushPose();
        poseStack.rotate(Axis.YP.rotationDegrees(180.0f - s.yaw));
        poseStack.scale(-s.size, -s.size, s.size);
        poseStack.translate(0.0f, -1.501f, 0.0f);
        RenderType base = translucent ? RenderTypes.entityTranslucent(texture(id)) : RenderTypes.entityCutout(texture(id));
        int tint = ARGB.color(Math.round(255 * alpha), 0xFFFFFF);
        collector.submitModel(model, s, poseStack, base, s.lightCoords, OverlayTexture.NO_OVERLAY, tint, null, s.outlineColor);
        collector.order(1).submitModel(model, s, poseStack, RenderTypes.eyes(glow(id)), FULL_BRIGHT, OverlayTexture.NO_OVERLAY, tint, null,
                s.outlineColor);
        poseStack.popPose();
    }

    // =====================================================================================
    // Geometrie
    // =====================================================================================

    private static float ease(float t) {
        t = Mth.clamp(t, 0.0f, 1.0f);
        return 1.0f - (1.0f - t) * (1.0f - t);
    }

    /** Farbe in der Helligkeit {@code f} (für additive Arten: Schwarz = unsichtbar). */
    private static int scale(int rgb, float f) {
        f = Mth.clamp(f, 0.0f, 1.0f);
        return ARGB.color(255, Math.round(ARGB.red(rgb) * f), Math.round(ARGB.green(rgb) * f), Math.round(ARGB.blue(rgb) * f));
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float z, float u, float v, int color, int light) {
        buffer.addVertex(pose, x, y, z).setColor(color).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
                .setNormal(pose, 0.0f, 1.0f, 0.0f);
    }

    /** Waagerechtes Viereck (Größe 1, Mitte im Ursprung), von beiden Seiten sichtbar. */
    private static void flatQuad(PoseStack.Pose pose, VertexConsumer buffer, int color, int light) {
        vertex(buffer, pose, -0.5f, 0.0f, -0.5f, 0.0f, 0.0f, color, light);
        vertex(buffer, pose, -0.5f, 0.0f, 0.5f, 0.0f, 1.0f, color, light);
        vertex(buffer, pose, 0.5f, 0.0f, 0.5f, 1.0f, 1.0f, color, light);
        vertex(buffer, pose, 0.5f, 0.0f, -0.5f, 1.0f, 0.0f, color, light);
        vertex(buffer, pose, 0.5f, 0.0f, -0.5f, 1.0f, 0.0f, color, light);
        vertex(buffer, pose, 0.5f, 0.0f, 0.5f, 1.0f, 1.0f, color, light);
        vertex(buffer, pose, -0.5f, 0.0f, 0.5f, 0.0f, 1.0f, color, light);
        vertex(buffer, pose, -0.5f, 0.0f, -0.5f, 0.0f, 0.0f, color, light);
    }

    /** Eine Rune aus dem Runenstreifen (8 Zeichen nebeneinander), waagerecht. */
    private static void flatGlyph(PoseStack.Pose pose, VertexConsumer buffer, int glyph, int color) {
        float u0 = glyph / 8.0f;
        float u1 = (glyph + 1) / 8.0f;
        vertex(buffer, pose, -0.5f, 0.0f, -0.5f, u0, 0.0f, color, FULL_BRIGHT);
        vertex(buffer, pose, -0.5f, 0.0f, 0.5f, u0, 1.0f, color, FULL_BRIGHT);
        vertex(buffer, pose, 0.5f, 0.0f, 0.5f, u1, 1.0f, color, FULL_BRIGHT);
        vertex(buffer, pose, 0.5f, 0.0f, -0.5f, u1, 0.0f, color, FULL_BRIGHT);
        vertex(buffer, pose, 0.5f, 0.0f, -0.5f, u1, 0.0f, color, FULL_BRIGHT);
        vertex(buffer, pose, 0.5f, 0.0f, 0.5f, u1, 1.0f, color, FULL_BRIGHT);
        vertex(buffer, pose, -0.5f, 0.0f, 0.5f, u0, 1.0f, color, FULL_BRIGHT);
        vertex(buffer, pose, -0.5f, 0.0f, -0.5f, u0, 0.0f, color, FULL_BRIGHT);
    }

    /** Zur Kamera gedrehtes Viereck (nur Vorderseite – additive Arten zeichnen ohnehin beide). */
    private static void billboard(PoseStack.Pose pose, VertexConsumer buffer, int color) {
        vertex(buffer, pose, -0.5f, -0.5f, 0.0f, 0.0f, 1.0f, color, FULL_BRIGHT);
        vertex(buffer, pose, 0.5f, -0.5f, 0.0f, 1.0f, 1.0f, color, FULL_BRIGHT);
        vertex(buffer, pose, 0.5f, 0.5f, 0.0f, 1.0f, 0.0f, color, FULL_BRIGHT);
        vertex(buffer, pose, -0.5f, 0.5f, 0.0f, 0.0f, 0.0f, color, FULL_BRIGHT);
    }

    private static void billboardTwoSided(PoseStack.Pose pose, VertexConsumer buffer, int color) {
        billboard(pose, buffer, color);
        vertex(buffer, pose, -0.5f, 0.5f, 0.0f, 0.0f, 0.0f, color, FULL_BRIGHT);
        vertex(buffer, pose, 0.5f, 0.5f, 0.0f, 1.0f, 0.0f, color, FULL_BRIGHT);
        vertex(buffer, pose, 0.5f, -0.5f, 0.0f, 1.0f, 1.0f, color, FULL_BRIGHT);
        vertex(buffer, pose, -0.5f, -0.5f, 0.0f, 0.0f, 1.0f, color, FULL_BRIGHT);
    }

    /** Zwei gekreuzte Bänder entlang +Y (Strahl). */
    private static void cross(PoseStack.Pose pose, VertexConsumer buffer, float length, float width, float repeat, int color) {
        float w = width * 0.5f;
        vertex(buffer, pose, -w, 0.0f, 0.0f, 0.0f, repeat, color, FULL_BRIGHT);
        vertex(buffer, pose, w, 0.0f, 0.0f, 1.0f, repeat, color, FULL_BRIGHT);
        vertex(buffer, pose, w, length, 0.0f, 1.0f, 0.0f, color, FULL_BRIGHT);
        vertex(buffer, pose, -w, length, 0.0f, 0.0f, 0.0f, color, FULL_BRIGHT);
        vertex(buffer, pose, 0.0f, 0.0f, -w, 0.0f, repeat, color, FULL_BRIGHT);
        vertex(buffer, pose, 0.0f, 0.0f, w, 1.0f, repeat, color, FULL_BRIGHT);
        vertex(buffer, pose, 0.0f, length, w, 1.0f, 0.0f, color, FULL_BRIGHT);
        vertex(buffer, pose, 0.0f, length, -w, 0.0f, 0.0f, color, FULL_BRIGHT);
    }

    /** Achteckiges Rohr (Säule) vom Boden bis {@code height}. */
    private static void tube(PoseStack.Pose pose, VertexConsumer buffer, float radius, float height, int color) {
        int sides = 10;
        float v = height * 0.4f;
        for (int i = 0; i < sides; i++) {
            float a0 = i * Mth.TWO_PI / sides;
            float a1 = (i + 1) * Mth.TWO_PI / sides;
            float x0 = Mth.cos(a0) * radius;
            float z0 = Mth.sin(a0) * radius;
            float x1 = Mth.cos(a1) * radius;
            float z1 = Mth.sin(a1) * radius;
            float u0 = i / (float) sides * 2.0f;
            float u1 = (i + 1) / (float) sides * 2.0f;
            vertex(buffer, pose, x0, 0.0f, z0, u0, v, color, FULL_BRIGHT);
            vertex(buffer, pose, x1, 0.0f, z1, u1, v, color, FULL_BRIGHT);
            vertex(buffer, pose, x1 * 0.75f, height, z1 * 0.75f, u1, 0.0f, color, FULL_BRIGHT);
            vertex(buffer, pose, x0 * 0.75f, height, z0 * 0.75f, u0, 0.0f, color, FULL_BRIGHT);
        }
    }

    /** Linienzug als gekreuzte Bänder (für den Blitzbogen, Farbe ohne Textur). */
    private static void polyline(PoseStack.Pose pose, VertexConsumer buffer, Vec3[] points, float width, int color) {
        float w = width * 0.5f;
        for (int i = 0; i < points.length - 1; i++) {
            Vec3 p = points[i];
            Vec3 q = points[i + 1];
            Vec3 d = q.subtract(p);
            if (d.lengthSqr() < 1.0e-6) {
                continue;
            }
            Vec3 n = d.normalize();
            Vec3 a = Math.abs(n.y) < 0.9 ? n.cross(new Vec3(0, 1, 0)).normalize() : n.cross(new Vec3(1, 0, 0)).normalize();
            Vec3 b = n.cross(a).normalize();
            for (Vec3 axis : new Vec3[] {a, b}) {
                Vec3 o = axis.scale(w);
                colorVertex(buffer, pose, p.subtract(o), color);
                colorVertex(buffer, pose, p.add(o), color);
                colorVertex(buffer, pose, q.add(o), color);
                colorVertex(buffer, pose, q.subtract(o), color);
                colorVertex(buffer, pose, q.subtract(o), color);
                colorVertex(buffer, pose, q.add(o), color);
                colorVertex(buffer, pose, p.add(o), color);
                colorVertex(buffer, pose, p.subtract(o), color);
            }
        }
    }

    private static void colorVertex(VertexConsumer buffer, PoseStack.Pose pose, Vec3 p, int color) {
        buffer.addVertex(pose, (float) p.x, (float) p.y, (float) p.z).setColor(color);
    }

    private static void quadTwoSided(PoseStack.Pose pose, VertexConsumer buffer, Vec3 p00, Vec3 p10, Vec3 p11, Vec3 p01, float u0, float u1,
            float v0, float v1, int color, int light) {
        vertex(buffer, pose, (float) p00.x, (float) p00.y, (float) p00.z, u0, v0, color, light);
        vertex(buffer, pose, (float) p10.x, (float) p10.y, (float) p10.z, u1, v0, color, light);
        vertex(buffer, pose, (float) p11.x, (float) p11.y, (float) p11.z, u1, v1, color, light);
        vertex(buffer, pose, (float) p01.x, (float) p01.y, (float) p01.z, u0, v1, color, light);
        vertex(buffer, pose, (float) p01.x, (float) p01.y, (float) p01.z, u0, v1, color, light);
        vertex(buffer, pose, (float) p11.x, (float) p11.y, (float) p11.z, u1, v1, color, light);
        vertex(buffer, pose, (float) p10.x, (float) p10.y, (float) p10.z, u1, v0, color, light);
        vertex(buffer, pose, (float) p00.x, (float) p00.y, (float) p00.z, u0, v0, color, light);
    }
}
