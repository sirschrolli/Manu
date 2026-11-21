package io.github.sirschrolli.kingdomsatwar.client.renderer.monster;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import io.github.sirschrolli.kingdomsatwar.entity.monster.NightRamEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/** Nachtwidder mit glimmenden Adern in den Kristallhörnern und blassblauen Augen. */
public class NightRamRenderer extends MobRenderer<NightRamEntity, NightRamRenderState, NightRamModel> {
    private static final Identifier TEXTURE = KingdomsAtWar.id("textures/entity/night_ram/night_ram.png");
    private static final RenderType GLOW = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/night_ram/night_ram_glow.png"));

    public NightRamRenderer(EntityRendererProvider.Context context) {
        super(context, new NightRamModel(context.bakeLayer(ModModelLayers.NIGHT_RAM)), 0.8f);
        this.addLayer(new GlowLayer<>(this, state -> GLOW));
    }

    @Override
    public Identifier getTextureLocation(NightRamRenderState state) {
        return TEXTURE;
    }

    @Override
    public NightRamRenderState createRenderState() {
        return new NightRamRenderState();
    }

    @Override
    public void extractRenderState(NightRamEntity entity, NightRamRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.butt.copyFrom(entity.buttAnimation);
        state.prepare.copyFrom(entity.prepareAnimation);
        state.stomp.copyFrom(entity.stompAnimation);
        state.stun.copyFrom(entity.stunAnimation);
        state.ramState = entity.state();
        boolean low = state.ramState == NightRamEntity.PREPARE || state.ramState == NightRamEntity.CHARGE;
        state.lowered = Anim.follow(entity, 0, low ? 1.0f : 0.0f, state.ageInTicks, 8.0f);
    }
}
