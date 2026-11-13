package io.github.sirschrolli.kingdomsatwar.client.renderer.monster;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.monster.UndeadCitizenModel;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.client.renderer.CitizenRenderState;
import io.github.sirschrolli.kingdomsatwar.client.renderer.VillagerRenderer;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.UndeadVillager;
import io.github.sirschrolli.kingdomsatwar.entity.villager.VillageProfession;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/**
 * Untote Dorfbewohner: Modell und Berufstracht der lebenden Dorfbewohner (gleiche Zusatzteile je Beruf), verweste Haut
 * und glimmende Augen; Rüstung, Gegenstände in der Hand, Jungtiere und das Zittern beim Heilen wie bei Vanilla.
 */
public class UndeadVillagerRenderer extends HumanoidMobRenderer<UndeadVillager, CitizenRenderState, UndeadCitizenModel> {
    private static final Map<String, Identifier> TEXTURES = new HashMap<>();
    private static final Map<String, RenderType> GLOWS = new HashMap<>();

    public UndeadVillagerRenderer(EntityRendererProvider.Context context) {
        super(context, new UndeadCitizenModel(context.bakeLayer(ModModelLayers.CITIZEN)),
                new UndeadCitizenModel(context.bakeLayer(MonsterRenderers.UNDEAD_CITIZEN_BABY)), 0.5f);
        this.addLayer(new HumanoidArmorLayer<>(this, ArmorModelSet.bake(ModelLayers.ZOMBIE_ARMOR, context.getModelSet(), HumanoidModel::new),
                ArmorModelSet.bake(MonsterRenderers.HUMANOID_BABY_ARMOR, context.getModelSet(), HumanoidModel::new),
                context.getEquipmentRenderer()));
        this.addLayer(new GlowLayer<>(this, state -> state instanceof UndeadVillagerRenderState undead ? undead.glow : null));
    }

    private static String key(VillageProfession profession, int variant) {
        return profession.id() + "_" + (variant + 1);
    }

    @Override
    public CitizenRenderState createRenderState() {
        return new UndeadVillagerRenderState();
    }

    @Override
    public void extractRenderState(UndeadVillager entity, CitizenRenderState citizen, float partialTicks) {
        super.extractRenderState(entity, citizen, partialTicks);
        if (!(citizen instanceof UndeadVillagerRenderState state)) {
            return;
        }
        VillageProfession profession = entity.getProfession();
        String key = key(profession, entity.getVariant());
        state.texture = TEXTURES.computeIfAbsent(key, k -> KingdomsAtWar.id("textures/entity/undead_villager/" + k + ".png"));
        state.glow = GLOWS.computeIfAbsent(key, k -> RenderTypes.eyes(KingdomsAtWar.id("textures/entity/undead_villager/" + k + "_glow.png")));
        state.parts = VillagerRenderer.parts(profession);
        state.feminine = entity.isFeminine();
        state.working = false;
        state.hammer = false;
        state.isAggressive = entity.isAggressive();
        state.isConverting = entity.isConverting();
    }

    @Override
    protected boolean isShaking(CitizenRenderState state) {
        return super.isShaking(state) || state instanceof UndeadVillagerRenderState undead && undead.isConverting;
    }

    @Override
    public Identifier getTextureLocation(CitizenRenderState state) {
        return state.texture != null ? state.texture : KingdomsAtWar.id("textures/entity/undead_villager/peasant_1.png");
    }
}
