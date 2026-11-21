package io.github.sirschrolli.kingdomsatwar.client.renderer.serpent;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.sirschrolli.kingdomsatwar.entity.serpent.QuetzaraEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Quetzara, 1,5-fach gezeichnet: der Kopf am Nacken, dahinter die Glieder und der Schwanz dort, wo die Spur sie hinlegt. Die
 * Augen glühen golden (im Schlaf decken Lider sie); nach der Häutung trägt sie ein neues, leuchtendes Kleid, und getarnt ist
 * sie kaum zu sehen – nur ein Flimmern. Beim Häuten zittert der Leib; im Tod zerfällt sie von der Schwanzspitze an.
 */
public class QuetzaraRenderer extends EntityRenderer<QuetzaraEntity, QuetzaraRenderState> {
    private final SerpentDraw draw;
    private final Vec3[] body = new Vec3[QuetzaraEntity.SEGMENTS + 1];

    public QuetzaraRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0f;
        this.draw = new SerpentDraw(context);
    }

    @Override
    public QuetzaraRenderState createRenderState() {
        return new QuetzaraRenderState();
    }

    /** Der Leib reicht weit über den Nacken hinaus. */
    @Override
    protected AABB getBoundingBoxForCulling(QuetzaraEntity entity, float partialTicks) {
        return super.getBoundingBoxForCulling(entity, partialTicks).inflate(26.0, 8.0, 26.0);
    }

    @Override
    public void extractRenderState(QuetzaraEntity entity, QuetzaraRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.headYaw = Mth.rotLerp(partialTicks, entity.yHeadRotO, entity.getYHeadRot());
        state.headPitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        state.jaw = entity.jaw(partialTicks);
        state.ruff = entity.ruff(partialTicks);
        state.asleep = entity.isAsleep();
        state.molted = entity.isMolted();
        state.shed = false;
        state.hurt = entity.hurtTime > 0 && entity.deathTime <= 0;
        state.deathTime = entity.deathTime > 0 ? entity.deathTime + partialTicks : 0.0f;
        // die Zunge züngelt (nicht im Schlaf, nicht im Tod)
        float cycle = (state.ageInTicks + entity.getId() * 13) % 46.0f;
        state.tongue = state.asleep || state.deathTime > 0.0f ? 0.0f : cycle < 9.0f ? Mth.sin(cycle / 9.0f * Mth.PI) : 0.0f;
        // getarnt: kaum zu sehen, nur ein Flimmern
        state.alpha = entity.isCamouflaged() ? 0.13f + 0.05f * Mth.sin(state.ageInTicks * 0.35f) : 1.0f;
        float shudder = 0.0f;
        if (entity.currentAttack() == QuetzaraEntity.Attack.MOLT) {
            float t = entity.attackTime(partialTicks);
            shudder = t < QuetzaraEntity.MOLT_SPLIT ? Mth.clamp(t / 12.0f, 0.0f, 1.0f) : 0.0f;
        }
        Vec3 origin = new Vec3(state.x, state.y, state.z);
        for (int i = 0; i <= QuetzaraEntity.SEGMENTS; i++) {
            this.body[i] = entity.bodyPoint(i, partialTicks);
        }
        SerpentDraw.lay(state, origin, entity.joint(partialTicks), this.body, state.ageInTicks, shudder);
        // im Tod zerfällt sie von der Schwanzspitze an
        if (state.deathTime > 40.0f) {
            int gone = (int) ((state.deathTime - 40.0f) / 3.0f);
            for (int i = 0; i <= QuetzaraEntity.SEGMENTS; i++) {
                state.segments[i].visible = i < QuetzaraEntity.SEGMENTS + 1 - gone;
            }
            if (gone > QuetzaraEntity.SEGMENTS) {
                state.alpha = Mth.clamp(1.0f - (state.deathTime - 40.0f - 3.0f * (QuetzaraEntity.SEGMENTS + 1)) / 10.0f, 0.0f, 1.0f);
            }
        }
    }

    @Override
    public void submit(QuetzaraRenderState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        float glow = state.deathTime > 0.0f ? Mth.clamp(1.0f - state.deathTime / 40.0f, 0.0f, 1.0f)
                : 0.85f + Mth.sin(state.ageInTicks * 0.09f) * 0.15f;
        if (state.alpha > 0.01f) {
            this.draw.submit(state, pose, collector, state.molted ? SerpentDraw.MOLTED : SerpentDraw.NORMAL, glow);
        }
        super.submit(state, pose, collector, camera);
    }
}
