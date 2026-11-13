package io.github.sirschrolli.kingdomsatwar.client.renderer.wild;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.entity.creature.farm.FarmAnimals;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;

/**
 * Grundlage der Renderer der eigenen Wildtiere. Den Zustand (Sitzen, Schlafen, Zorn, Flügelschlag …) liest der passende
 * Vanilla-Renderer genau so aus, wie Vanilla es tut; dieser Renderer zeichnet dann das eigene Modell mit dem eigenen
 * Fellbild. Jungtiere sind das halb so große erwachsene Modell (mit größerem Kopf, siehe Modelle) – deshalb rechnen
 * die Vanilla-Animationen hier immer in erwachsenen Maßen ({@code ageScale = 1}).
 */
public abstract class WildRenderer<T extends Mob, S extends LivingEntityRenderState, M extends EntityModel<? super S>>
        extends MobRenderer<T, S, M> {
    private final EntityRenderer<? super T, ? super S> vanilla;

    protected WildRenderer(EntityRendererProvider.Context context, M model, float shadow, EntityRenderer<? super T, ? super S> vanilla) {
        super(context, model, shadow);
        this.vanilla = vanilla;
    }

    @Override
    public final void extractRenderState(T entity, S state, float partialTicks) {
        this.vanilla.extractRenderState(entity, state, partialTicks);
        state.ageScale = 1.0f;
        this.extract(entity, state, partialTicks);
    }

    /** Eigene Werte (Rasse, Fellbild) nach dem Vanilla-Zustand. */
    protected void extract(T entity, S state, float partialTicks) {
    }

    @Override
    protected void scale(S state, PoseStack poseStack) {
        if (state.isBaby) {
            poseStack.scale(0.5f, 0.5f, 0.5f);
        }
    }

    @Override
    protected float getShadowRadius(S state) {
        return super.getShadowRadius(state) * (state.isBaby ? 0.5f : 1.0f);
    }

    /** Fellbilder je Rasse: {@code textures/entity/<ordner>/<präfix><rasse><anhang>.png}. */
    protected static Identifier[] textures(String folder, String prefix, FarmAnimals.Breed[] breeds, String suffix) {
        Identifier[] out = new Identifier[breeds.length];
        for (int i = 0; i < breeds.length; i++) {
            out[i] = KingdomsAtWar.id("textures/entity/" + folder + "/" + prefix + breeds[i].id() + suffix + ".png");
        }
        return out;
    }

    protected static Identifier pick(Identifier[] textures, int breed) {
        return textures[Mth.clamp(breed, 0, textures.length - 1)];
    }
}
