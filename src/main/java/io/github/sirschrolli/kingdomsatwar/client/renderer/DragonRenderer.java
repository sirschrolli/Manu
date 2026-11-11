package io.github.sirschrolli.kingdomsatwar.client.renderer;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.boss.DragonPose;
import io.github.sirschrolli.kingdomsatwar.client.boss.DragonRig;
import io.github.sirschrolli.kingdomsatwar.client.model.DragonEntityMotion;
import io.github.sirschrolli.kingdomsatwar.client.model.DragonModel;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.entity.dragon.DragonEntity;
import java.util.EnumMap;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/** Zeichnet die Drachen: Grundtextur je Art plus leuchtende Teile (Augen, Blitzmuster). */
public class DragonRenderer extends MobRenderer<DragonEntity, DragonRenderState, DragonModel> {
    private final DragonEntityMotion motion = new DragonEntityMotion();
    private final DragonPose pose = new DragonPose(6);
    /** Federn je Drache (jeder schwingt für sich nach). */
    private final Map<DragonEntity, DragonRig> rigs = new WeakHashMap<>();
    private static final Map<DragonType, Identifier> TEXTURES = new EnumMap<>(DragonType.class);
    private static final Map<DragonType, RenderType> GLOW = new EnumMap<>(DragonType.class);

    static {
        for (DragonType type : DragonType.values()) {
            TEXTURES.put(type, KingdomsAtWar.id("textures/entity/dragon/" + type.id() + ".png"));
            GLOW.put(type, RenderTypes.eyes(KingdomsAtWar.id("textures/entity/dragon/" + type.id() + "_glow.png")));
        }
    }

    public DragonRenderer(EntityRendererProvider.Context context) {
        super(context, new DragonModel(context.bakeLayer(ModModelLayers.DRAGON)), 1.1f);
        this.addLayer(new EyesLayer<>(this) {
            private DragonType current = DragonType.FIRE;

            @Override
            public void submit(com.mojang.blaze3d.vertex.PoseStack poseStack, net.minecraft.client.renderer.SubmitNodeCollector collector,
                    int light, DragonRenderState state, float yRot, float xRot) {
                this.current = state.dragonType;
                super.submit(poseStack, collector, light, state, yRot, xRot);
            }

            @Override
            public RenderType renderType() {
                return GLOW.get(this.current);
            }
        });
        this.addLayer(new DragonArmorLayer(this, context.getModelSet()));
    }

    @Override
    public Identifier getTextureLocation(DragonRenderState state) {
        return TEXTURES.get(state.dragonType);
    }

    @Override
    public DragonRenderState createRenderState() {
        return new DragonRenderState();
    }

    @Override
    public void extractRenderState(DragonEntity entity, DragonRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.dragonType = entity.getDragonType();
        state.stage = entity.getStage();
        state.flying = entity.isFlying();
        state.sitting = entity.isInSittingPose();
        state.breathing = entity.isBreathing();
        state.ridden = entity.isVehicle();
        state.armor = entity.getBodyArmorItem();
        state.roarAnimation.copyFrom(entity.roarAnimation);
        state.biteAnimation.copyFrom(entity.biteAnimation);
        state.biteDip = entity.biteDip;
        // Eingaben der Bewegungslehre (weich, mit Bruchteil des Ticks)
        state.flapPhase = Mth.lerp(partialTicks, entity.flapPhaseO, entity.flapPhase);
        state.airBlend = Mth.lerp(partialTicks, entity.airBlendO, entity.airBlend);
        state.bank = Mth.lerp(partialTicks, entity.bankO, entity.bank);
        state.flightPitch = Mth.lerp(partialTicks, entity.flightPitchO, entity.flightPitch);
        state.sit = Mth.lerp(partialTicks, entity.sitBlendO, entity.sitBlend);
        state.stride = Mth.lerp(partialTicks, entity.strideO, entity.stride);
        state.strideSpeed = Mth.lerp(partialTicks, entity.strideSpeedO, entity.strideSpeed);
        state.breathTime = entity.isBreathing() ? (entity.tickCount - entity.breathStart + partialTicks) / 20.0f : -1.0f;
        state.idleEvent = entity.idleEvent;
        state.idleTime = entity.idleEvent == 0 ? 0.0f : (entity.tickCount - entity.idleEventStart + partialTicks) / 20.0f;
        state.idleSide = entity.idleSide;
        // Zielhaltung bestimmen und mit den Federn dieses Drachen glätten
        this.motion.pose(state, this.pose);
        DragonRig rig = this.rigs.computeIfAbsent(entity, e -> new DragonRig(3, 6));
        rig.update(state, this.pose, state.ageInTicks / 20.0f, state.bodyRot, entity.hurtTime);
    }
}
