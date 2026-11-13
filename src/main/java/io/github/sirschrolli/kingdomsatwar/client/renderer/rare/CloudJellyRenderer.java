package io.github.sirschrolli.kingdomsatwar.client.renderer.rare;

import io.github.sirschrolli.kingdomsatwar.entity.creature.rare.CloudJelly;
import net.minecraft.client.model.animal.ghast.HappyGhastHarnessModel;
import net.minecraft.client.model.animal.ghast.HappyGhastModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.RopesLayer;
import net.minecraft.client.renderer.entity.layers.SimpleEquipmentLayer;
import net.minecraft.client.renderer.entity.state.HappyGhastRenderState;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.phys.AABB;

/**
 * Wolkenqualle: eigenes Modell (Körperwürfel und Fangarme an denselben Stellen wie beim glücklichen Ghast, damit das
 * Wehen der Fangarme greift und Geschirr, Schutzbrille und Seile passen), Wolkenbild je Gestalt; das Wölkchen hat
 * größere Augen.
 */
public class CloudJellyRenderer extends AgeableMobRenderer<HappyGhast, HappyGhastRenderState, HappyGhastModel> {
    private static final String[] LOOKS = {"schoenwetter", "abendrot", "regenwolke"};
    private static final Identifier[] ADULT = new Identifier[LOOKS.length];
    private static final Identifier[] BABY = new Identifier[LOOKS.length];
    private static final Identifier ROPES = Identifier.withDefaultNamespace("textures/entity/ghast/happy_ghast_ropes.png");

    static {
        for (int i = 0; i < LOOKS.length; i++) {
            ADULT[i] = RareRenderers.texture("happy_ghast", LOOKS[i]);
            BABY[i] = RareRenderers.texture("happy_ghast", LOOKS[i] + "_baby");
        }
    }

    /** Bildzustand mit der Gestalt (die Ebenen für Geschirr und Seile erwarten den Vanilla-Zustand). */
    public static class State extends HappyGhastRenderState {
        public int look;
    }

    public CloudJellyRenderer(EntityRendererProvider.Context context) {
        super(context, new HappyGhastModel(context.bakeLayer(RareRenderers.HAPPY_GHAST)),
                new HappyGhastModel(context.bakeLayer(RareRenderers.HAPPY_GHAST_BABY)), 2.0f);
        this.addLayer(new SimpleEquipmentLayer<>(this, context.getEquipmentRenderer(), EquipmentClientInfo.LayerType.HAPPY_GHAST_BODY,
                state -> state.bodyItem, new HappyGhastHarnessModel(context.bakeLayer(ModelLayers.HAPPY_GHAST_HARNESS)),
                new HappyGhastHarnessModel(context.bakeLayer(ModelLayers.HAPPY_GHAST_BABY_HARNESS))));
        this.addLayer(new RopesLayer<>(this, context.getModelSet(), ROPES));
    }

    @Override
    public HappyGhastRenderState createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(HappyGhast entity, HappyGhastRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.bodyItem = entity.getItemBySlot(EquipmentSlot.BODY).copy();
        state.isRidden = entity.isVehicle();
        state.isLeashHolder = entity.isLeashHolder();
        if (state instanceof State s) {
            s.look = entity instanceof CloudJelly jelly ? jelly.getLook().ordinal() : 0;
        }
    }

    @Override
    protected AABB getBoundingBoxForCulling(HappyGhast entity, float partialTicks) {
        AABB box = super.getBoundingBoxForCulling(entity, partialTicks);
        return box.setMinY(box.minY - entity.getBbHeight() / 2.0f);
    }

    @Override
    public Identifier getTextureLocation(HappyGhastRenderState state) {
        int look = state instanceof State s ? Mth.clamp(s.look, 0, LOOKS.length - 1) : 0;
        return state.isBaby ? BABY[look] : ADULT[look];
    }
}
