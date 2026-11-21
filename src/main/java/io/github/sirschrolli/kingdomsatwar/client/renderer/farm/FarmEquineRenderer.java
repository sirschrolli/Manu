package io.github.sirschrolli.kingdomsatwar.client.renderer.farm;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.entity.creature.farm.FarmAnimals;
import java.util.function.ToIntFunction;
import net.minecraft.client.model.animal.equine.AbstractEquineModel;
import net.minecraft.client.model.animal.equine.EquineSaddleModel;
import net.minecraft.client.model.animal.equine.HorseModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AbstractHorseRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.SimpleEquipmentLayer;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.equine.AbstractChestedHorse;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import org.jspecify.annotations.Nullable;

/**
 * Renderer für Pferd, Esel und Maultier: eigene Modelle und Fellbilder, aber Sattel, Rossharnisch und Packtaschen-Sattel
 * von Vanilla (das Skelett ist dasselbe, also sitzt alles wie gewohnt). Fohlen sind halb so groß mit größerem Kopf.
 */
public class FarmEquineRenderer<T extends AbstractHorse>
        extends AbstractHorseRenderer<T, FarmEquineRenderState, AbstractEquineModel<FarmEquineRenderState>> {
    private final Identifier[] textures;
    private final ToIntFunction<T> breed;
    private final boolean mule;

    public FarmEquineRenderer(EntityRendererProvider.Context context, AbstractEquineModel<FarmEquineRenderState> adult,
            AbstractEquineModel<FarmEquineRenderState> baby, String folder, String prefix, FarmAnimals.Breed[] breeds,
            ToIntFunction<T> breed, EquipmentClientInfo.LayerType saddleLayer, ModelLayerLocation saddleModel, boolean armor,
            boolean mule) {
        super(context, adult, baby);
        this.textures = new Identifier[breeds.length];
        for (int i = 0; i < breeds.length; i++) {
            this.textures[i] = KingdomsAtWar.id("textures/entity/" + folder + "/" + prefix + breeds[i].id() + ".png");
        }
        this.breed = breed;
        this.mule = mule;
        if (armor) {
            this.addLayer(new SimpleEquipmentLayer<>(this, context.getEquipmentRenderer(), EquipmentClientInfo.LayerType.HORSE_BODY,
                    state -> state.bodyArmorItem, new HorseModel(context.bakeLayer(ModelLayers.HORSE_ARMOR)), null, 2));
        }
        this.addLayer(new SimpleEquipmentLayer<>(this, context.getEquipmentRenderer(), saddleLayer, state -> state.saddle,
                new EquineSaddleModel(context.bakeLayer(saddleModel)), null, 2));
    }

    @Override
    public Identifier getTextureLocation(FarmEquineRenderState state) {
        return this.textures[Mth.clamp(state.breed, 0, this.textures.length - 1)];
    }

    @Override
    protected void scale(FarmEquineRenderState state, PoseStack poseStack) {
        if (state.isBaby) {
            poseStack.scale(state.ageScale, state.ageScale, state.ageScale);
        }
    }

    @Override
    public FarmEquineRenderState createRenderState() {
        return new FarmEquineRenderState();
    }

    @Override
    public void extractRenderState(T entity, FarmEquineRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.breed = this.breed.applyAsInt(entity);
        state.mule = this.mule;
        state.hasChest = entity instanceof AbstractChestedHorse chested && chested.hasChest();
        state.armored = !state.bodyArmorItem.isEmpty();
    }

    /** Unbenutzt, aber praktisch für Tests: die Textur einer Rasse. */
    public @Nullable Identifier texture(int breed) {
        return breed < 0 || breed >= this.textures.length ? null : this.textures[breed];
    }
}
