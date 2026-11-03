package io.github.sirschrolli.kingdomsatwar.client.renderer.monster;

import io.github.sirschrolli.kingdomsatwar.client.model.farm.FarmHorseModel;
import io.github.sirschrolli.kingdomsatwar.client.renderer.farm.FarmEquineRenderer;
import io.github.sirschrolli.kingdomsatwar.entity.creature.farm.FarmAnimals;
import java.util.function.ToIntFunction;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.equine.AbstractHorse;

/**
 * Zombie- und Skelettpferde auf dem Pferde-Modell der Mod: eigene Bilder je Art, leuchtende Augen; Sattel (und beim
 * Zombiepferd Rossharnisch) wie bei Vanilla.
 */
public class UndeadEquineRenderer<T extends AbstractHorse> extends FarmEquineRenderer<T> {
    private final RenderType[] glows;

    public UndeadEquineRenderer(EntityRendererProvider.Context context, String prefix, FarmAnimals.Breed[] looks, ToIntFunction<T> look,
            EquipmentClientInfo.LayerType saddle, boolean armor) {
        super(context, new FarmHorseModel(context.bakeLayer(MonsterRenderers.UNDEAD_HORSE)),
                new FarmHorseModel(context.bakeLayer(MonsterRenderers.UNDEAD_HORSE)), "undead_horse", prefix, looks, look, saddle,
                ModelLayers.HORSE_SADDLE, armor, false);
        String[] names = new String[looks.length];
        for (int i = 0; i < looks.length; i++) {
            names[i] = prefix + looks[i].id();
        }
        this.glows = MonsterRenderers.glows("undead_horse", names);
        this.addLayer(new GlowLayer<>(this, state -> this.glows[Mth.clamp(state.breed, 0, this.glows.length - 1)]));
    }
}
