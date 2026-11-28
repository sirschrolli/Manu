package io.github.sirschrolli.kingdomsatwar.client.renderer.monster;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import io.github.sirschrolli.kingdomsatwar.entity.monster.AshWraithEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/** Aschengeist mit seelenblauen Augen, Seelenflamme in der Brust, glimmenden Krallen und Aschebrocken. */
public class AshWraithRenderer extends MobRenderer<AshWraithEntity, AshWraithRenderState, AshWraithModel> {
    private static final Identifier TEXTURE = KingdomsAtWar.id("textures/entity/ash_wraith/ash_wraith.png");
    private static final RenderType GLOW = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/ash_wraith/ash_wraith_glow.png"));

    public AshWraithRenderer(EntityRendererProvider.Context context) {
        super(context, new AshWraithModel(context.bakeLayer(ModModelLayers.ASH_WRAITH)), 0.4f);
        this.addLayer(new GlowLayer<>(this, state -> GLOW));
    }

    @Override
    public Identifier getTextureLocation(AshWraithRenderState state) {
        return TEXTURE;
    }

    @Override
    public AshWraithRenderState createRenderState() {
        return new AshWraithRenderState();
    }

    @Override
    public void extractRenderState(AshWraithEntity entity, AshWraithRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.bolt.copyFrom(entity.boltAnimation);
        state.cloud.copyFrom(entity.cloudAnimation);
        state.slash.copyFrom(entity.slashAnimation);
        float moving = (float) Math.min(1.0, entity.getDeltaMovement().horizontalDistance() * 6.0);
        state.drift = Anim.follow(entity, 0, moving, state.ageInTicks, 4.0f);
    }
}
