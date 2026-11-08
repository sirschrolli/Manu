package io.github.sirschrolli.kingdomsatwar.client.renderer.siege;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.client.model.siege.SiegeModels;
import io.github.sirschrolli.kingdomsatwar.siege.SiegeType;
import io.github.sirschrolli.kingdomsatwar.siege.SiegeWeaponEntity;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

/**
 * Katapult, Tribok, Balliste, Rammbock, Belagerungsturm und Planwagen. Getroffen wackelt die Waffe kurz, stark beschädigt
 * dunkelt das Holz nach (und sie qualmt, siehe {@link SiegeWeaponEntity}). Die Fallbrücke des Turms folgt dem Winkel, den
 * der Server schickt; die Ladung des Wagens ist zu sehen, sobald etwas geladen ist.
 */
public class SiegeWeaponRenderer extends EntityRenderer<SiegeWeaponEntity, SiegeModels.State> {
    /** Nur für Vorführungen: feste Stellung (Ticks nach dem Schuss) je Wesen, unabhängig von der Spielzeit. */
    public static final java.util.Map<java.util.UUID, Float> DEV_POSE = new java.util.concurrent.ConcurrentHashMap<>();

    private final Model<SiegeModels.State> model;
    private final Identifier texture;
    private final SiegeType siege;

    public SiegeWeaponRenderer(EntityRendererProvider.Context context, SiegeType siege) {
        super(context);
        this.siege = siege;
        this.model = switch (siege) {
            case CATAPULT -> new SiegeModels.Catapult(context.bakeLayer(ModModelLayers.CATAPULT));
            case TREBUCHET -> new SiegeModels.Trebuchet(context.bakeLayer(ModModelLayers.TREBUCHET));
            case BALLISTA -> new SiegeModels.Ballista(context.bakeLayer(ModModelLayers.BALLISTA));
            case RAM -> new SiegeModels.BatteringRam(context.bakeLayer(ModModelLayers.BATTERING_RAM));
            case SIEGE_TOWER -> new SiegeModels.SiegeTower(context.bakeLayer(ModModelLayers.SIEGE_TOWER));
            case WAGON -> new SiegeModels.Wagon(context.bakeLayer(ModModelLayers.WAGON));
        };
        String id = textureId(siege);
        this.texture = KingdomsAtWar.id("textures/entity/siege/" + id + "/" + id + ".png");
        this.shadowRadius = siege.width() * 0.5f;
        this.shadowStrength = 0.9f;
    }

    static String textureId(SiegeType siege) {
        return switch (siege) {
            case CATAPULT -> "catapult";
            case TREBUCHET -> "trebuchet";
            case BALLISTA -> "ballista";
            case RAM -> "battering_ram";
            case SIEGE_TOWER -> "siege_tower";
            case WAGON -> "wagon";
        };
    }

    @Override
    public SiegeModels.State createRenderState() {
        return new SiegeModels.State();
    }

    @Override
    public void extractRenderState(SiegeWeaponEntity entity, SiegeModels.State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.siege = entity.siege();
        state.yRot = entity.getYRot(partialTicks);
        Float pose = DEV_POSE.isEmpty() ? null : DEV_POSE.get(entity.getUUID());
        if (pose != null) {
            state.since = pose;
            state.loaded = pose < io.github.sirschrolli.kingdomsatwar.siege.SiegePoses.releaseTick(entity.siege());
        } else {
            state.since = entity.sinceFired(partialTicks);
            state.loaded = entity.loaded() || entity.releasing(partialTicks);
        }
        state.aim = entity.aim();
        state.wheel = Mth.lerp(partialTicks, entity.wheelRotO, entity.wheelRot);
        state.hurt = Math.max(0.0f, entity.hurtTime() - partialTicks);
        state.health = entity.healthFraction();
        state.bridge = entity instanceof io.github.sirschrolli.kingdomsatwar.siege.SiegeTowerEntity tower ? tower.bridge(partialTicks) : 0.0f;
        state.storeys = entity instanceof io.github.sirschrolli.kingdomsatwar.siege.SiegeTowerEntity tower ? tower.storeys() : 1;
        state.cargo = entity instanceof io.github.sirschrolli.kingdomsatwar.siege.SiegeWagonEntity wagon && wagon.cargoLevel() > 0;
    }

    @Override
    protected AABB getBoundingBoxForCulling(SiegeWeaponEntity entity, float partialTicks) {
        // Wurfarm, Schlinge, Widderkopf und Bolzen ragen weit über den Rahmen hinaus, ebenso die Brücke des Turms und die Deichsel
        return entity.getBoundingBox().inflate(switch (this.siege) {
            case TREBUCHET -> 3.5;
            case SIEGE_TOWER -> 4.6;
            case WAGON -> 2.3;
            default -> 1.75;
        });
    }

    @Override
    public void submit(SiegeModels.State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.rotate(Axis.YP.rotationDegrees(180.0f - state.yRot));
        if (state.hurt > 0.0f) {
            poseStack.rotate(Axis.ZP.rotationDegrees(Mth.sin(state.hurt * 1.7f) * state.hurt * 0.3f));
        }
        poseStack.scale(-1.0f, -1.0f, 1.0f);
        poseStack.translate(0.0f, -1.501f, 0.0f);
        collector.submitModel(this.model, state, poseStack, this.model.renderType(this.texture), state.lightCoords, OverlayTexture.NO_OVERLAY,
                tint(state.health), null, state.outlineColor);
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }

    /** Unversehrt unverändert; unter 60 % dunkelt das Holz zunehmend nach (verrußt, angesengt). */
    private static int tint(float health) {
        if (health >= 0.6f) {
            return -1;
        }
        float f = 0.6f + 0.4f * Mth.clamp(health / 0.6f, 0.0f, 1.0f);
        return ARGB.color(255, Math.round(255 * f), Math.round(255 * f * 0.94f), Math.round(255 * f * 0.88f));
    }
}
