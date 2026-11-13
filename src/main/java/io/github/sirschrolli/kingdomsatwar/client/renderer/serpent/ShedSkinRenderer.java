package io.github.sirschrolli.kingdomsatwar.client.renderer.serpent;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.sirschrolli.kingdomsatwar.entity.serpent.QuetzaraEntity;
import io.github.sirschrolli.kingdomsatwar.entity.serpent.ShedSkinEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Quetzaras abgestreifte Haut: dieselbe Gestalt, blass und halb durchscheinend, der Kopf aufgerissen, die Augen leer. Zerfällt
 * sie, verblasst sie von der Schwanzspitze an.
 */
public class ShedSkinRenderer extends EntityRenderer<ShedSkinEntity, QuetzaraRenderState> {
    private final SerpentDraw draw;
    private final Vec3[] body = new Vec3[QuetzaraEntity.SEGMENTS + 1];

    public ShedSkinRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0f;
        this.draw = new SerpentDraw(context);
    }

    @Override
    public QuetzaraRenderState createRenderState() {
        return new QuetzaraRenderState();
    }

    @Override
    protected AABB getBoundingBoxForCulling(ShedSkinEntity entity, float partialTicks) {
        return super.getBoundingBoxForCulling(entity, partialTicks).inflate(4.0);
    }

    @Override
    public void extractRenderState(ShedSkinEntity entity, QuetzaraRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.ageInTicks = 0.0f;
        state.headYaw = entity.headYaw();
        state.headPitch = entity.headPitch();
        state.jaw = 0.55f;
        state.ruff = entity.ruff() * 0.6f;
        state.tongue = 0.0f;
        state.asleep = false;
        state.molted = false;
        state.shed = true;
        state.hurt = false;
        state.deathTime = 0.0f;
        float crumble = entity.crumble(partialTicks);
        state.alpha = 1.0f - crumble * 0.85f;
        Vec3 origin = new Vec3(state.x, state.y, state.z);
        for (int i = 0; i <= QuetzaraEntity.SEGMENTS; i++) {
            this.body[i] = origin.add(entity.point(i));
        }
        SerpentDraw.lay(state, origin, origin.add(entity.jointOffset()), this.body, 0.0f, 0.0f);
        int gone = (int) (crumble * (QuetzaraEntity.SEGMENTS + 2));
        for (int i = 0; i <= QuetzaraEntity.SEGMENTS; i++) {
            state.segments[i].visible = entity.isPosed() && i < QuetzaraEntity.SEGMENTS + 1 - gone;
            state.segments[i].roll = 0.0f;
        }
    }

    @Override
    public void submit(QuetzaraRenderState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.alpha > 0.02f) {
            this.draw.submit(state, pose, collector, SerpentDraw.SHED, Mth.clamp(state.alpha, 0.0f, 1.0f));
        }
        super.submit(state, pose, collector, camera);
    }
}
