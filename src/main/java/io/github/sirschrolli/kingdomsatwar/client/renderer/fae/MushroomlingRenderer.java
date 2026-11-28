package io.github.sirschrolli.kingdomsatwar.client.renderer.fae;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import io.github.sirschrolli.kingdomsatwar.client.renderer.monster.GlowLayer;
import io.github.sirschrolli.kingdomsatwar.entity.fae.MushroomlingEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/** Pilzling in drei Farben, mit leuchtenden Augen und Lamellen. */
public class MushroomlingRenderer extends MobRenderer<MushroomlingEntity, FaeRenderState, MushroomlingModel> {
    private static final String[] LOOKS = {"rose", "azure", "violet"};
    private static final Identifier[] TEXTURES = new Identifier[LOOKS.length];
    private static final RenderType[] GLOW = new RenderType[LOOKS.length];

    static {
        for (int i = 0; i < LOOKS.length; i++) {
            TEXTURES[i] = KingdomsAtWar.id("textures/entity/mushroomling/" + LOOKS[i] + ".png");
            GLOW[i] = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/mushroomling/" + LOOKS[i] + "_glow.png"));
        }
    }

    public MushroomlingRenderer(EntityRendererProvider.Context context) {
        super(context, new MushroomlingModel(context.bakeLayer(ModModelLayers.MUSHROOMLING)), 0.35f);
        this.addLayer(new GlowLayer<>(this, state -> GLOW[Mth.clamp(state.variant, 0, LOOKS.length - 1)]));
    }

    @Override
    public Identifier getTextureLocation(FaeRenderState state) {
        return TEXTURES[Mth.clamp(state.variant, 0, LOOKS.length - 1)];
    }

    @Override
    public FaeRenderState createRenderState() {
        return new FaeRenderState();
    }

    @Override
    public void extractRenderState(MushroomlingEntity entity, FaeRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.variant = entity.getVariant();
        state.hide = Anim.follow(entity, 0, entity.isHiding() ? 1.0f : 0.0f, state.ageInTicks, 6.0f);
    }
}
