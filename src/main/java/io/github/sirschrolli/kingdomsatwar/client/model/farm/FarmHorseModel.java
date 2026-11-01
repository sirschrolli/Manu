package io.github.sirschrolli.kingdomsatwar.client.model.farm;

import io.github.sirschrolli.kingdomsatwar.client.renderer.farm.FarmEquineRenderState;
import io.github.sirschrolli.kingdomsatwar.entity.creature.farm.FarmHorse;
import java.util.List;
import net.minecraft.client.model.animal.equine.AbstractEquineModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Pferd mit dem Skelett des Vanilla-Pferds: die Vanilla-Animationen (Gehen, Steigen, Grasen, Schweifschlagen) laufen
 * unverändert, dazu kommen Ohrenspiel und – je Rasse – lange Seitenmähne, großer Schopf und Fesselbehang.
 */
public class FarmHorseModel extends AbstractEquineModel<FarmEquineRenderState> {
    private final ModelPart leftEar;
    private final ModelPart rightEar;
    private final ModelPart forelock;
    private final ModelPart forelockBig;
    private final ModelPart maneDrape;
    private final List<ModelPart> feathers;

    public FarmHorseModel(ModelPart root) {
        super(root);
        ModelPart head = this.headParts.getChild("head");
        this.leftEar = head.getChild("left_ear");
        this.rightEar = head.getChild("right_ear");
        this.forelock = head.getChild("forelock");
        this.forelockBig = head.getChild("forelock_big");
        this.maneDrape = this.headParts.getChild("mane_drape");
        this.feathers = List.of(root.getChild("left_hind_leg").getChild("feather_left_hind_leg"),
                root.getChild("right_hind_leg").getChild("feather_right_hind_leg"),
                root.getChild("left_front_leg").getChild("feather_left_front_leg"),
                root.getChild("right_front_leg").getChild("feather_right_front_leg"));
    }

    @Override
    public void setupAnim(FarmEquineRenderState state) {
        super.setupAnim(state);
        FarmHorse.Breed breed = FarmHorse.Breed.values()[Mth.clamp(state.breed, 0, FarmHorse.Breed.values().length - 1)];
        boolean big = breed == FarmHorse.Breed.FRIESE || breed == FarmHorse.Breed.KALTBLUT || breed == FarmHorse.Breed.PONY;
        this.forelock.visible = !big;
        this.forelockBig.visible = big;
        this.maneDrape.visible = big || breed == FarmHorse.Breed.SCHIMMEL || breed == FarmHorse.Breed.HAFLINGER;
        for (ModelPart feather : this.feathers) {
            feather.visible = big && !state.isBaby;
        }
        if (!state.armored) {
            // Ohrenspiel (unter dem Rossharnisch ruhig, damit die Ohren nicht herausstechen)
            float t = state.ageInTicks;
            this.leftEar.xRot += Math.max(0.0f, Mth.sin(t * 0.06f)) * 0.35f;
            this.rightEar.xRot += Math.max(0.0f, Mth.sin(t * 0.06f + 2.3f)) * 0.35f;
        }
        if (state.isBaby) {
            this.headParts.xScale = this.headParts.yScale = this.headParts.zScale = 1.2f;
        }
    }
}
