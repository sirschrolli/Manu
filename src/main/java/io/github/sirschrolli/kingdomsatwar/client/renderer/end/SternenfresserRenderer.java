package io.github.sirschrolli.kingdomsatwar.client.renderer.end;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.monster.dragon.EnderDragonModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EndCrystalRenderer;
import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EnderDragonRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.level.dimension.end.EnderDragonFight;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Sternenfresser: das eigene Aussehen des Enderdrachen (der Vanilla-Drache bleibt, wie er ist – Kampf, Kristalle, Portal
 * und Ei hängen an ihm; sein Konstruktor legt den Typ fest). Eigenes Modell auf dem Gerüst des Vanilla-Drachen (Hals,
 * Schwanz und Flügel bewegen sich wie gewohnt), eigenes Bild, Augen, Kristalle, Adern, Sterne und der Riss leuchten;
 * beim Sterben zerfällt er nach eigenem Muster in violetten Strahlen. Die Heilstrahlen der Endkristalle bleiben die
 * gewohnten.
 */
public class SternenfresserRenderer extends EntityRenderer<EnderDragon, EnderDragonRenderState> {
    private static final Identifier TEXTURE = EndRenderers.texture("ender_dragon", "sternenfresser");
    private static final Identifier DISSOLVE = EndRenderers.texture("ender_dragon", "sternenfresser_zerfall");
    private static final RenderType DYING = RenderTypes.entityCutoutDissolve(TEXTURE, DISSOLVE);
    private static final RenderType GLOW = RenderTypes.eyes(EndRenderers.texture("ender_dragon", "sternenfresser_glow"));
    private static final float HALF_SQRT_3 = (float) (Math.sqrt(3.0) / 2.0);
    /** Farbe der Todesstrahlen außen: Violett statt Magenta. */
    private static final int RAY_OUTER = 0x00A040FF;
    private final EnderDragonModel model;

    public SternenfresserRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5f;
        this.model = new EnderDragonModel(context.bakeLayer(EndRenderers.STERNENFRESSER));
    }

    @Override
    public void submit(EnderDragonRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        float yr = state.getHistoricalPos(7).yRot();
        float rot2 = (float) (state.getHistoricalPos(5).y() - state.getHistoricalPos(10).y());
        poseStack.rotateDegrees(Axis.YP, -yr);
        poseStack.rotateDegrees(Axis.XP, rot2 * 10.0f);
        poseStack.translate(0.0f, 0.0f, 1.0f);
        poseStack.scale(-1.0f, -1.0f, 1.0f);
        poseStack.translate(0.0f, -1.501f, 0.0f);
        int overlay = OverlayTexture.pack(0.0f, state.hasRedOverlay);
        if (state.deathTime > 0.0f) {
            int color = ARGB.white(1.0f - state.deathTime / 200.0f);
            collector.submitModel(this.model, state, poseStack, DYING, state.lightCoords, OverlayTexture.NO_OVERLAY, color, null, state.outlineColor);
        } else {
            collector.submitModel(this.model, state, poseStack, TEXTURE, state.lightCoords, overlay, state.outlineColor);
        }
        collector.submitModel(this.model, state, poseStack, GLOW, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        if (state.deathTime > 0.0f) {
            float deathTime = state.deathTime / 200.0f;
            poseStack.pushPose();
            poseStack.translate(0.0f, -1.0f, -2.0f);
            submitRays(poseStack, deathTime, collector, RenderTypes.dragonRays());
            poseStack.popPose();
        }
        poseStack.popPose();
        if (state.beamOffset != null) {
            EnderDragonRenderer.submitCrystalBeams((float) state.beamOffset.x, (float) state.beamOffset.y, (float) state.beamOffset.z,
                    state.ageInTicks, poseStack, collector, state.lightCoords);
        }
        super.submit(state, poseStack, collector, camera);
    }

    /** Todesstrahlen wie beim Enderdrachen, außen violett. */
    private static void submitRays(PoseStack poseStack, float deathTime, SubmitNodeCollector collector, RenderType renderType) {
        collector.submitCustomGeometry(poseStack, renderType, (pose, buffer) -> {
            float overDrive = Math.min(deathTime > 0.8f ? (deathTime - 0.8f) / 0.2f : 0.0f, 1.0f);
            int inner = ARGB.colorFromFloat(1.0f - overDrive, 1.0f, 1.0f, 1.0f);
            RandomSource random = RandomSource.createThreadLocalInstance(432L);
            Vector3f origin = new Vector3f();
            Vector3f outerLeft = new Vector3f();
            Vector3f outerRight = new Vector3f();
            Vector3f outerBottom = new Vector3f();
            Quaternionf rotation = new Quaternionf();
            int rays = Mth.floor((deathTime + deathTime * deathTime) / 2.0f * 60.0f);
            for (int i = 0; i < rays; i++) {
                rotation.rotationXYZ(random.nextFloat() * Mth.TWO_PI, random.nextFloat() * Mth.TWO_PI, random.nextFloat() * Mth.TWO_PI)
                        .rotateXYZ(random.nextFloat() * Mth.TWO_PI, random.nextFloat() * Mth.TWO_PI,
                                random.nextFloat() * Mth.TWO_PI + deathTime * Mth.HALF_PI);
                pose.rotate(rotation);
                float length = random.nextFloat() * 20.0f + 5.0f + overDrive * 10.0f;
                float width = random.nextFloat() * 2.0f + 1.0f + overDrive * 2.0f;
                outerLeft.set(-HALF_SQRT_3 * width, length, -0.5f * width);
                outerRight.set(HALF_SQRT_3 * width, length, -0.5f * width);
                outerBottom.set(0.0f, length, width);
                buffer.addVertex(pose, origin).setColor(inner);
                buffer.addVertex(pose, outerLeft).setColor(RAY_OUTER);
                buffer.addVertex(pose, outerRight).setColor(RAY_OUTER);
                buffer.addVertex(pose, origin).setColor(inner);
                buffer.addVertex(pose, outerRight).setColor(RAY_OUTER);
                buffer.addVertex(pose, outerBottom).setColor(RAY_OUTER);
                buffer.addVertex(pose, origin).setColor(inner);
                buffer.addVertex(pose, outerBottom).setColor(RAY_OUTER);
                buffer.addVertex(pose, outerLeft).setColor(RAY_OUTER);
            }
        });
    }

    @Override
    public EnderDragonRenderState createRenderState() {
        return new EnderDragonRenderState();
    }

    @Override
    public void extractRenderState(EnderDragon entity, EnderDragonRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.flapTime = Mth.lerp(partialTicks, entity.oFlapTime, entity.flapTime);
        state.deathTime = entity.dragonDeathTime > 0 ? entity.dragonDeathTime + partialTicks : 0.0f;
        state.hasRedOverlay = entity.hurtTime > 0;
        EndCrystal crystal = entity.nearestCrystal;
        if (crystal != null) {
            Vec3 pos = crystal.getPosition(partialTicks).add(0.0, EndCrystalRenderer.getY(crystal.time + partialTicks), 0.0);
            state.beamOffset = pos.subtract(entity.getPosition(partialTicks));
        } else {
            state.beamOffset = null;
        }
        DragonPhaseInstance phase = entity.getPhaseManager().getCurrentPhase();
        state.isLandingOrTakingOff = phase == EnderDragonPhase.LANDING || phase == EnderDragonPhase.TAKEOFF;
        state.isSitting = phase.isSitting();
        BlockPos egg = entity.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                EnderDragonFight.getPodiumLocation(entity.getFightOrigin()));
        state.distanceToEgg = egg.distToCenterSqr(entity.position());
        state.partialTicks = entity.isDeadOrDying() ? 0.0f : partialTicks;
        state.flightHistory.copyFrom(entity.flightHistory);
    }

    @Override
    protected boolean affectedByCulling(EnderDragon entity) {
        return false;
    }
}
