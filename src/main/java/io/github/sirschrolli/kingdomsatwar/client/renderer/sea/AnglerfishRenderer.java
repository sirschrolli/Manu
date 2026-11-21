package io.github.sirschrolli.kingdomsatwar.client.renderer.sea;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import io.github.sirschrolli.kingdomsatwar.client.model.sea.AnglerfishModel;
import io.github.sirschrolli.kingdomsatwar.client.renderer.monster.GlowLayer;
import io.github.sirschrolli.kingdomsatwar.entity.sea.AnglerfishEntity;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

/**
 * Anglerfisch: Köder, Leuchtpunkte und Augen leuchten (eigene Leuchttextur); um den Köder liegt ein weicher, pulsierender
 * Schein, der beim Zuschnappen grell aufflammt – in der dunklen Tiefe sieht man oft nur ihn.
 */
public class AnglerfishRenderer extends MobRenderer<AnglerfishEntity, SeaRenderState, AnglerfishModel> {
    private static final Identifier TEXTURE = KingdomsAtWar.id("textures/entity/anglerfish/anglerfish.png");
    private static final RenderType GLOW = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/anglerfish/anglerfish_glow.png"));
    private static final int LURE = 0x7AD8FF;

    public AnglerfishRenderer(EntityRendererProvider.Context context) {
        super(context, new AnglerfishModel(context.bakeLayer(ModModelLayers.ANGLERFISH)), 0.5f);
        this.addLayer(new GlowLayer<>(this, state -> GLOW));
    }

    @Override
    public Identifier getTextureLocation(SeaRenderState state) {
        return TEXTURE;
    }

    @Override
    public SeaRenderState createRenderState() {
        return new SeaRenderState();
    }

    @Override
    public void extractRenderState(AnglerfishEntity entity, SeaRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.swim = Mth.lerp(partialTicks, entity.swimO, entity.swim);
        state.inWater = entity.isInWater();
        state.attack.copyFrom(entity.snapAnimation);
    }

    @Override
    public void submit(SeaRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(state, poseStack, collector, camera);
        if (state.isInvisible || state.deathTime > 0.0f) {
            return;
        }
        // wo der Köder gerade hängt: das Modell für diesen Zustand stellen und der Knochenkette folgen
        // (in einem eigenen, leeren Stapel – so liegt der Punkt relativ zum Wesen, nicht zur Kamera)
        AnglerfishModel model = this.getModel();
        model.setupAnim(state);
        PoseStack local = new PoseStack();
        local.scale(state.scale, state.scale, state.scale);
        this.setupRotations(state, local, state.bodyRot, state.scale);
        local.scale(-1.0f, -1.0f, 1.0f);
        this.scale(state, local);
        local.translate(0.0f, -1.501f, 0.0f);
        for (ModelPart part : model.lureChain()) {
            part.translateAndRotate(local);
        }
        Vector3f lure = local.last().pose().transformPosition(new Vector3f());
        float t = state.ageInTicks;
        float strength = 0.55f + 0.12f * Mth.sin(t * 0.13f) + 0.06f * Mth.sin(t * 0.37f);
        float snap = Anim.seconds(state.attack, t);
        if (snap >= 0.0f && snap < 0.8f) {
            strength += Anim.bump(snap, 0.05f, 0.15f, 0.7f) * 0.6f;
        }
        SeaGlow.halo(poseStack, collector, camera.orientation, lure, 1.1f + strength * 0.4f, LURE, strength);
    }

    @Override
    protected float getShadowStrength(SeaRenderState state) {
        return 0.3f;
    }
}
