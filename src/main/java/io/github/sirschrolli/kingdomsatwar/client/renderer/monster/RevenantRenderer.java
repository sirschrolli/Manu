package io.github.sirschrolli.kingdomsatwar.client.renderer.monster;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.sirschrolli.kingdomsatwar.client.model.monster.RevenantModel;
import java.util.function.ToIntFunction;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AbstractZombieRenderer;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.Items;

/**
 * Renderer der Zombie-Familie (Wiedergänger, Mumie, Wasserleiche): eigenes Modell und eigene Bilder je Art, leuchtende
 * Augen; Rüstung, Gegenstände in der Hand, Köpfe, Jungtiere und das Zittern beim Verwandeln wie bei Vanilla.
 */
public class RevenantRenderer<T extends Zombie> extends AbstractZombieRenderer<T, RevenantRenderState, RevenantModel> {
    private final Identifier[] textures;
    private final RenderType[] glows;
    private final ToIntFunction<T> look;
    private final float size;
    private final boolean drowned;

    public RevenantRenderer(EntityRendererProvider.Context context, String[] names, ToIntFunction<T> look, float size, boolean drowned) {
        super(context, new RevenantModel(context.bakeLayer(MonsterRenderers.UNDEAD)),
                new RevenantModel(context.bakeLayer(MonsterRenderers.UNDEAD_BABY)),
                ArmorModelSet.bake(ModelLayers.ZOMBIE_ARMOR, context.getModelSet(), RevenantModel::new),
                ArmorModelSet.bake(MonsterRenderers.HUMANOID_BABY_ARMOR, context.getModelSet(), RevenantModel::new));
        this.textures = MonsterRenderers.textures("undead", names);
        this.glows = MonsterRenderers.glows("undead", names);
        this.look = look;
        this.size = size;
        this.drowned = drowned;
        this.addLayer(new GlowLayer<>(this, state -> this.glows[Mth.clamp(state.look, 0, this.glows.length - 1)]));
    }

    @Override
    public RevenantRenderState createRenderState() {
        return new RevenantRenderState();
    }

    @Override
    public void extractRenderState(T entity, RevenantRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.look = this.look.applyAsInt(entity);
        state.drowned = this.drowned;
    }

    @Override
    public Identifier getTextureLocation(RevenantRenderState state) {
        return this.textures[Mth.clamp(state.look, 0, this.textures.length - 1)];
    }

    @Override
    protected void scale(RevenantRenderState state, PoseStack poseStack) {
        if (this.size != 1.0f) {
            poseStack.scale(this.size, this.size, this.size);
        }
        super.scale(state, poseStack);
    }

    @Override
    protected void setupRotations(RevenantRenderState state, PoseStack poseStack, float bodyRot, float entityScale) {
        super.setupRotations(state, poseStack, bodyRot, entityScale);
        if (this.drowned && state.swimAmount > 0.0f) {
            float rotationX = Mth.lerp(state.swimAmount, 0.0f, -10.0f - state.xRot);
            poseStack.rotateAround(Axis.XP.rotationDegrees(rotationX), 0.0f, state.boundingBoxHeight / 2.0f / entityScale, 0.0f);
        }
    }

    @Override
    protected HumanoidModel.ArmPose getArmPose(T mob, HumanoidArm arm) {
        if (this.drowned && mob.getMainArm() == arm && mob.isAggressive() && mob.getItemHeldByArm(arm).is(Items.TRIDENT)) {
            return HumanoidModel.ArmPose.THROW_TRIDENT;
        }
        return super.getArmPose(mob, arm);
    }
}
