package io.github.sirschrolli.kingdomsatwar.client.renderer.farm;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.entity.creature.farm.FarmAnimals;
import java.util.function.ToIntFunction;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;

/**
 * Renderer der eigenen Hof- und Weidetiere: Textur je Rasse ({@code textures/entity/<ordner>/<präfix><rasse>.png}),
 * Jungtiere halb so groß (den großen Kopf macht das Modell), dazu je Tier ein paar Zusatzwerte für die Animation.
 */
public class FarmAnimalRenderer<T extends Mob> extends MobRenderer<T, FarmRenderState, EntityModel<FarmRenderState>> {
    private final Identifier[] textures;
    private final ToIntFunction<T> breed;
    private final Extra<T> extra;

    /** Zusatzwerte eines Tiers für die Animation (Flügelschlag, Grasen, Sprung …). */
    @FunctionalInterface
    public interface Extra<T> {
        void accept(T entity, FarmRenderState state, float partialTicks);
    }

    public FarmAnimalRenderer(EntityRendererProvider.Context context, EntityModel<FarmRenderState> model, float shadow, String folder,
            String prefix, FarmAnimals.Breed[] breeds, ToIntFunction<T> breed, Extra<T> extra) {
        super(context, model, shadow);
        this.textures = new Identifier[breeds.length];
        for (int i = 0; i < breeds.length; i++) {
            this.textures[i] = KingdomsAtWar.id("textures/entity/" + folder + "/" + prefix + breeds[i].id() + ".png");
        }
        this.breed = breed;
        this.extra = extra;
    }

    @Override
    public Identifier getTextureLocation(FarmRenderState state) {
        return this.textures[Mth.clamp(state.breed, 0, this.textures.length - 1)];
    }

    @Override
    protected void scale(FarmRenderState state, PoseStack poseStack) {
        if (state.isBaby) {
            poseStack.scale(state.ageScale, state.ageScale, state.ageScale);
        }
    }

    @Override
    public FarmRenderState createRenderState() {
        return new FarmRenderState();
    }

    @Override
    public void extractRenderState(T entity, FarmRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.breed = this.breed.applyAsInt(entity);
        this.extra.accept(entity, state, partialTicks);
    }

    /** Zusätzliche Ebene (Wolle, leuchtende Augen). */
    public void layer(net.minecraft.client.renderer.entity.layers.RenderLayer<FarmRenderState, EntityModel<FarmRenderState>> layer) {
        this.addLayer(layer);
    }

    /** Für Unterklassen und Ebenen: die Textur einer Rasse. */
    public Identifier texture(int breed) {
        return this.textures[Mth.clamp(breed, 0, this.textures.length - 1)];
    }
}
