package io.github.sirschrolli.kingdomsatwar.client.renderer.monster;

import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import io.github.sirschrolli.kingdomsatwar.client.model.monster.HagModel;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Hag;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;

/**
 * Hexen: eigenes Menschen-Modell mit Spitzhut, Nase, Haaren, Umhang, Kleid und Trankgürtel; den Trank, den sie trinkt
 * oder wirft, hält sie sichtbar in der Hand. Augen und Fläschchen glimmen.
 */
public class HagRenderer extends HumanoidMobRenderer<Hag, HagRenderState, HagModel> {
    private static final String[] NAMES = {"hexe_sumpf", "hexe_nacht", "hexe_kraut"};
    private static final Identifier[] TEXTURES = MonsterRenderers.textures("witch", NAMES);
    private static final RenderType[] GLOWS = MonsterRenderers.glows("witch", NAMES);

    public HagRenderer(EntityRendererProvider.Context context) {
        super(context, new HagModel(context.bakeLayer(MonsterRenderers.WITCH)), 0.5f);
        this.addLayer(new GlowLayer<>(this, state -> GLOWS[state.look]));
    }

    @Override
    public HagRenderState createRenderState() {
        return new HagRenderState();
    }

    @Override
    public void extractRenderState(Hag hag, HagRenderState state, float partialTicks) {
        super.extractRenderState(hag, state, partialTicks);
        state.look = Mth.clamp(hag.getLook().ordinal(), 0, NAMES.length - 1);
        state.holdingPotion = !hag.getMainHandItem().isEmpty();
        state.drinking = hag.isDrinkingPotion();
        state.drink = Anim.follow(hag, 0, state.drinking ? 1.0f : 0.0f, state.ageInTicks, 7.0f);
    }

    @Override
    protected HumanoidModel.ArmPose getArmPose(Hag hag, HumanoidArm arm) {
        return arm == hag.getMainArm() && !hag.getMainHandItem().isEmpty() ? HumanoidModel.ArmPose.ITEM : HumanoidModel.ArmPose.EMPTY;
    }

    @Override
    public Identifier getTextureLocation(HagRenderState state) {
        return TEXTURES[state.look];
    }
}
