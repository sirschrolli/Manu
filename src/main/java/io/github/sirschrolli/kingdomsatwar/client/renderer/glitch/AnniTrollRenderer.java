package io.github.sirschrolli.kingdomsatwar.client.renderer.glitch;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.entity.glitch.AnniTrollEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;

/**
 * Anni der Troll – mit ihren Bildfehlern: Zwei farbverschobene Abbilder (rot und cyan) laufen neben ihr her, je weiter,
 * je kaputter sie gerade ist (beim Hängen, im Absturz, im Fehlermodus). Für einzelne Ticks springt sie ein Stück zur
 * Seite oder zeigt statt ihrer Haut nur fehlende Textur. Hängt sie, dreht sich über ihrem Kopf ein Ladekreis. Augen,
 * Bildschirmauge, Pixel, Schnalle und die Bruchflächen des Arms leuchten. Im Tod zerfällt sie flackernd.
 */
public class AnniTrollRenderer extends MobRenderer<AnniTrollEntity, AnniTrollRenderState, AnniTrollModel> {
    public static final Identifier TEXTURE = KingdomsAtWar.id("textures/entity/anni_troll/anni_troll.png");
    public static final Identifier MISSING = KingdomsAtWar.id("textures/entity/anni_troll/anni_troll_missing.png");
    public static final Identifier TEXTURE_V2 = KingdomsAtWar.id("textures/entity/anni_troll/anni_troll_v2.png");
    public static final RenderType GLOW = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/anni_troll/anni_troll_glow.png"));
    public static final RenderType GLOW_V2 = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/anni_troll/anni_troll_v2_glow.png"));
    public static final RenderType SPLIT = RenderTypes.entityTranslucentEmissive(TEXTURE);
    private static final RenderType LOADING = RenderTypes.entityTranslucentEmissive(KingdomsAtWar.id("textures/entity/anni_troll/loading.png"));
    public static final float SCALE = 1.2f;
    private static final int FULL_BRIGHT = 0xF000F0;

    public AnniTrollRenderer(EntityRendererProvider.Context context) {
        super(context, new AnniTrollModel(context.bakeLayer(ModModelLayers.ANNI_TROLL)), 1.3f);
        this.addLayer(new GlowLayer(this));
        this.addLayer(new SplitLayer(this));
    }

    /** Zufall, fest für einen Tick (dieselbe Formel wie im Modell). */
    static float hash(int seed, int tick, int salt) {
        int h = seed * 73_856_093 ^ tick * 19_349_663 ^ salt * 83_492_791;
        h ^= h >>> 13;
        h *= 0x5bd1e995;
        h ^= h >>> 15;
        return (h & 0xFFFF) / 65535.0f;
    }

    /** Wie kaputt sie gerade ist (0 ruhig … 1 völlig zerfallen). */
    static float glitchLevel(AnniTrollRenderState state) {
        float g = state.version2 ? 0.55f : state.glitchMode ? 0.45f : 0.2f;
        if (state.lagging) {
            g = Math.max(g, 0.7f);
        }
        if (state.attack == AnniTrollEntity.Attack.CRASH.ordinal() && state.attackTime < AnniTrollEntity.CRASH_RESTART) {
            g = 1.0f;
        }
        if (state.attack == AnniTrollEntity.Attack.SPAWN.ordinal() && state.attackTime < AnniTrollEntity.SPAWN_ASSEMBLE + 12) {
            g = 0.9f;
        }
        if (state.attack == AnniTrollEntity.Attack.REVIVE.ordinal() && state.attackTime < AnniTrollEntity.REVIVE_ASSEMBLE + 10) {
            // erst zerfällt sie (alles flackert), dann setzt sie sich ruckelnd zusammen
            g = state.attackTime < AnniTrollEntity.REVIVE_SHATTER ? 1.0f : 0.9f;
        }
        if (state.deathTime > 0.0f) {
            g = 0.8f + Math.min(0.2f, state.deathTime / 100.0f);
        }
        return g;
    }

    /** Zeigt sie in diesem Tick nur fehlende Textur? */
    static boolean missingFrame(AnniTrollRenderState state) {
        float g = glitchLevel(state);
        float chance = g >= 1.0f ? 0.3f : g >= 0.7f ? 0.1f : g * 0.06f;
        return hash(state.seed, (int) state.ageInTicks, 20) < chance;
    }

    /** Die leuchtenden Teile, sanft flimmernd. */
    static final class GlowLayer extends RenderLayer<AnniTrollRenderState, AnniTrollModel> {
        GlowLayer(RenderLayerParent<AnniTrollRenderState, AnniTrollModel> parent) {
            super(parent);
        }

        @Override
        public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, AnniTrollRenderState state, float yRot, float xRot) {
            if (state.isInvisible || missingFrame(state)) {
                return;
            }
            float flicker = hash(state.seed, (int) state.ageInTicks, 21) < 0.1f ? 0.55f : 0.9f + Mth.sin(state.ageInTicks * 0.4f) * 0.08f;
            float alpha = Mth.clamp(flicker * fade(state), 0.0f, 1.0f);
            collector.order(1).submitModel(this.getParentModel(), state, poseStack, state.version2 ? GLOW_V2 : GLOW, light, OverlayTexture.NO_OVERLAY,
                    ARGB.color(Math.round(alpha * 255.0f), 0xFFFFFF), null, state.outlineColor);
        }
    }

    /** Die Farbverschiebung: ein rotes und ein cyanfarbenes Abbild, seitlich (zur Kamera hin) versetzt. */
    static final class SplitLayer extends RenderLayer<AnniTrollRenderState, AnniTrollModel> {
        SplitLayer(RenderLayerParent<AnniTrollRenderState, AnniTrollModel> parent) {
            super(parent);
        }

        @Override
        public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, AnniTrollRenderState state, float yRot, float xRot) {
            if (state.isInvisible || state.splitX == 0.0f && state.splitZ == 0.0f) {
                return;
            }
            float g = glitchLevel(state);
            int alpha = Math.round((0.18f + g * 0.22f) * fade(state) * 255.0f);
            for (int side = -1; side <= 1; side += 2) {
                poseStack.pushPose();
                poseStack.translate(state.splitX * side, 0.0f, state.splitZ * side);
                int color = side < 0 ? ARGB.color(alpha, 255, 40, 80) : ARGB.color(alpha, 40, 230, 255);
                collector.order(2).submitModel(this.getParentModel(), state, poseStack, SPLIT, FULL_BRIGHT, OverlayTexture.NO_OVERLAY, color, null,
                        state.outlineColor);
                poseStack.popPose();
            }
        }
    }

    /** Im Sterben: das letzte Drittel wird sie durchsichtig (sie zerfällt in Pixel). */
    static float fade(AnniTrollRenderState state) {
        if (state.deathTime <= 0.0f) {
            return state.alpha;
        }
        float d = Mth.clamp(state.deathTime / AnniTrollEntity.DEATH_TICKS, 0.0f, 1.0f);
        return state.alpha * (d < 0.6f ? 1.0f : 1.0f - (d - 0.6f) / 0.4f);
    }

    @Override
    public Identifier getTextureLocation(AnniTrollRenderState state) {
        return missingFrame(state) ? MISSING : state.version2 ? TEXTURE_V2 : TEXTURE;
    }

    @Override
    protected @Nullable RenderType getRenderType(AnniTrollRenderState state, boolean isBodyVisible, boolean forceTransparent,
            boolean appearGlowing) {
        if (!isBodyVisible && !forceTransparent && !appearGlowing) {
            return null;
        }
        Identifier texture = this.getTextureLocation(state);
        return appearGlowing && !isBodyVisible ? RenderTypes.outline(texture) : RenderTypes.entityTranslucent(texture);
    }

    @Override
    protected int getModelTint(AnniTrollRenderState state) {
        return ARGB.color(Math.round(fade(state) * 255.0f), 0xFFFFFF);
    }

    @Override
    public AnniTrollRenderState createRenderState() {
        return new AnniTrollRenderState();
    }

    @Override
    public void extractRenderState(AnniTrollEntity entity, AnniTrollRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.attack = entity.currentAttack().ordinal();
        state.attackTime = entity.attackTime(partialTicks);
        state.lagging = entity.isLagging();
        state.lagTime = entity.lagTime(partialTicks);
        state.sinceLag = entity.sinceLag(partialTicks);
        state.glitchMode = entity.isGlitchMode();
        state.version2 = entity.isVersion2();
        state.seed = entity.getId();
        state.clone = false;
        state.afterimage = false;
        state.alpha = 1.0f;
        if (entity.isHidden()) {
            state.isInvisible = true;
        }
        if (entity.deathTime > 0) {
            state.hasRedOverlay = false;
        }
    }

    @Override
    protected void scale(AnniTrollRenderState state, PoseStack poseStack) {
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    /** Sie kippt im Tod nicht um, sie sackt zusammen und zerfällt (siehe {@link AnniTrollModel}). */
    @Override
    protected float getFlipDegrees() {
        return 0.0f;
    }

    @Override
    protected int getBlockLightLevel(AnniTrollEntity entity, BlockPos pos) {
        return Math.max(6, super.getBlockLightLevel(entity, pos));
    }

    @Override
    public void submit(AnniTrollRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        computeSplit(state, camera);
        poseStack.pushPose();
        jitter(state, poseStack);
        super.submit(state, poseStack, collector, camera);
        poseStack.popPose();
        if (state.lagging && !state.isInvisible) {
            submitLoading(state, poseStack, collector, camera, 4.9f);
        }
    }

    /** Wie weit (und in welche Richtung, im Modellraum) die Farbabbilder versetzt sind: quer zur Blickrichtung. */
    static void computeSplit(AnniTrollRenderState state, CameraRenderState camera) {
        float g = glitchLevel(state);
        float amount = 0.03f + g * 0.09f + (hash(state.seed, (int) state.ageInTicks, 30) < 0.15f ? g * 0.12f : 0.0f);
        // rechts der Kamera in der Welt, dann in den Raum des Modells (gedreht um 180 - Körperwinkel, x gespiegelt)
        float cy = camera.yRot * Mth.DEG_TO_RAD;
        float rx = -Mth.cos(cy);
        float rz = -Mth.sin(cy);
        float a = (state.bodyRot - 180.0f) * Mth.DEG_TO_RAD;
        float ux = rx * Mth.cos(a) + rz * Mth.sin(a);
        float uz = -rx * Mth.sin(a) + rz * Mth.cos(a);
        state.splitX = -ux * amount;
        state.splitZ = uz * amount;
    }

    /** Ein Ruck: für einzelne Ticks steht sie ein Stück daneben (im Absturz und im Fehlermodus öfter). */
    static void jitter(AnniTrollRenderState state, PoseStack poseStack) {
        float g = glitchLevel(state);
        int tick = (int) state.ageInTicks;
        if (hash(state.seed, tick, 40) < g * 0.35f) {
            float s = 0.08f + g * 0.25f;
            poseStack.translate((hash(state.seed, tick, 41) - 0.5f) * 2.0f * s, (hash(state.seed, tick, 42) - 0.5f) * s,
                    (hash(state.seed, tick, 43) - 0.5f) * 2.0f * s);
        }
    }

    /** Der Ladekreis: acht Punkte im Kreis, die sich ruckweise drehen (über dem Kopf, der Kamera zugewandt). */
    static void submitLoading(AnniTrollRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera,
            float height) {
        poseStack.pushPose();
        poseStack.translate(0.0f, height, 0.0f);
        poseStack.rotate(Axis.YP.rotationDegrees(180.0f - camera.yRot));
        poseStack.rotate(Axis.ZP.rotationDegrees(-45.0f * ((int) (state.ageInTicks / 2.0f) % 8)));
        float s = 0.55f;
        collector.submitCustomGeometry(poseStack, LOADING, (pose, buffer) -> {
            vertex(buffer, pose, -s, -s, 0.0f, 1.0f);
            vertex(buffer, pose, s, -s, 1.0f, 1.0f);
            vertex(buffer, pose, s, s, 1.0f, 0.0f);
            vertex(buffer, pose, -s, s, 0.0f, 0.0f);
            vertex(buffer, pose, -s, s, 0.0f, 0.0f);
            vertex(buffer, pose, s, s, 1.0f, 0.0f);
            vertex(buffer, pose, s, -s, 1.0f, 1.0f);
            vertex(buffer, pose, -s, -s, 0.0f, 1.0f);
        });
        poseStack.popPose();
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float u, float v) {
        buffer.addVertex(pose, x, y, 0.0f).setColor(-1).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT)
                .setNormal(pose, 0.0f, 0.0f, 1.0f);
    }
}
