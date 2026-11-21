package io.github.sirschrolli.kingdomsatwar.client.renderer.biome;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import io.github.sirschrolli.kingdomsatwar.entity.creature.biome.GriffinEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/** Greif: zwei Arten; im Flug in der Kurve geneigt, beim Steigen und Stürzen mit dem ganzen Leib geneigt. */
public class GriffinRenderer extends MobRenderer<GriffinEntity, GriffinRenderState, GriffinModel> {
    private static final Identifier[] TEXTURES = {
            KingdomsAtWar.id("textures/entity/griffin/griffin.png"),
            KingdomsAtWar.id("textures/entity/griffin/griffin_rust.png")};

    public GriffinRenderer(EntityRendererProvider.Context context) {
        super(context, new GriffinModel(context.bakeLayer(ModModelLayers.GRIFFIN)), 1.0f);
    }

    @Override
    public Identifier getTextureLocation(GriffinRenderState state) {
        return TEXTURES[Math.floorMod(state.variant, TEXTURES.length)];
    }

    @Override
    public GriffinRenderState createRenderState() {
        return new GriffinRenderState();
    }

    @Override
    public void extractRenderState(GriffinEntity entity, GriffinRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.variant = entity.getVariant();
        float age = state.ageInTicks;
        boolean flying = entity.isFlying() && !entity.isInWater();
        boolean diving = entity.isDiving();
        state.fly = Anim.follow(entity, 0, flying ? 1.0f : 0.0f, age, 5.0f);
        // schlagen beim Steigen, segeln beim Sinken und im Sturz; sonst nur ab und zu ein paar Schläge
        double vy = entity.getY() - entity.yo;
        float wantFlap = diving ? 0.0f : vy > 0.03 ? 1.0f : vy < -0.05 ? 0.0f : (Mth.sin(age * 0.05f + entity.getId()) > 0.55f ? 0.9f : 0.05f);
        state.flap = Anim.follow(entity, 1, wantFlap, age, 4.0f);
        state.dive = Anim.follow(entity, 2, diving ? 1.0f : 0.0f, age, 6.0f);
        state.rest = Anim.follow(entity, 3, entity.isResting() && !flying ? 1.0f : 0.0f, age, 3.0f);
        float turn = Mth.wrapDegrees(entity.getYRot() - entity.yRotO);
        state.bank = Anim.follow(entity, 4, flying ? Mth.clamp(-turn * 0.07f, -0.6f, 0.6f) : 0.0f, age, 3.0f);
        double flat = Math.hypot(entity.getX() - entity.xo, entity.getZ() - entity.zo);
        float wantPitch = flying ? (float) Mth.clamp(-Math.atan2(vy, Math.max(flat, 0.05)), -0.7, 1.1) : 0.0f;
        if (flying && entity.isNoAi()) {
            wantPitch = entity.getXRot() * Mth.DEG_TO_RAD;                  // ohne KI (Vorführung): Neigung wie gesetzt
        }
        state.pitch = Anim.follow(entity, 5, wantPitch, age, 4.0f);
        state.screech.copyFrom(entity.screechAnimation);
        state.strike.copyFrom(entity.strikeAnimation);
        state.swipe.copyFrom(entity.swipeAnimation);
        state.eat.copyFrom(entity.eatAnimation);
    }

    @Override
    protected void setupRotations(GriffinRenderState state, PoseStack poseStack, float bodyRot, float entityScale) {
        super.setupRotations(state, poseStack, bodyRot, entityScale);
        if (state.bank != 0.0f || state.pitch != 0.0f) {
            // um die Leibesmitte neigen (Schräglage in der Kurve, Nase hinab beim Sinken und im Sturz)
            poseStack.translate(0.0f, 0.95f, 0.0f);
            poseStack.rotate(Axis.ZP, state.bank);
            poseStack.rotate(Axis.XP, -state.pitch * state.fly);
            poseStack.translate(0.0f, -0.95f, 0.0f);
        }
    }
}
