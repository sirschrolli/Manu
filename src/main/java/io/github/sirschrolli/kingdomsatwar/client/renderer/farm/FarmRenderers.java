package io.github.sirschrolli.kingdomsatwar.client.renderer.farm;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.client.model.farm.CattleModel;
import io.github.sirschrolli.kingdomsatwar.client.model.farm.FarmChickenModel;
import io.github.sirschrolli.kingdomsatwar.client.model.farm.FarmDonkeyModel;
import io.github.sirschrolli.kingdomsatwar.client.model.farm.FarmGoatModel;
import io.github.sirschrolli.kingdomsatwar.client.model.farm.FarmHorseModel;
import io.github.sirschrolli.kingdomsatwar.client.model.farm.FarmPigModel;
import io.github.sirschrolli.kingdomsatwar.client.model.farm.FarmSheepModel;
import io.github.sirschrolli.kingdomsatwar.client.model.farm.HareModel;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.CattleGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.FarmChickenGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.FarmDonkeyGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.FarmGoatGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.FarmHorseGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.FarmPigGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.FarmSheepGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.HareGeometry;
import io.github.sirschrolli.kingdomsatwar.entity.creature.farm.Cattle;
import io.github.sirschrolli.kingdomsatwar.entity.creature.farm.FarmChicken;
import io.github.sirschrolli.kingdomsatwar.entity.creature.farm.FarmDonkey;
import io.github.sirschrolli.kingdomsatwar.entity.creature.farm.FarmGoat;
import io.github.sirschrolli.kingdomsatwar.entity.creature.farm.FarmHorse;
import io.github.sirschrolli.kingdomsatwar.entity.creature.farm.FarmMule;
import io.github.sirschrolli.kingdomsatwar.entity.creature.farm.FarmPig;
import io.github.sirschrolli.kingdomsatwar.entity.creature.farm.FarmSheep;
import io.github.sirschrolli.kingdomsatwar.entity.creature.farm.Hare;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Modell-Ebenen und Renderer der eigenen Hof- und Weidetiere. */
public final class FarmRenderers {
    /** Pferd wie Vanilla etwas größer gebaut; Esel und Maultier kleiner (gleiche Faktoren wie Vanilla). */
    private static final float HORSE_SCALE = 1.1f;
    private static final float DONKEY_SCALE = 0.87f;
    private static final float MULE_SCALE = 0.92f;
    private static final RenderType KILLER_EYES = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/hare/hare_mordshase_glow.png"));

    private FarmRenderers() {}

    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ModModelLayers.CATTLE, CattleGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.FARM_PIG, FarmPigGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.FARM_SHEEP, FarmSheepGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.FARM_CHICKEN, FarmChickenGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.FARM_GOAT, FarmGoatGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.HARE, HareGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.FARM_HORSE, () -> LayerDefinition.create(FarmHorseGeometry.createMesh(),
                FarmHorseGeometry.TEXTURE_WIDTH, FarmHorseGeometry.TEXTURE_HEIGHT).apply(MeshTransformer.scaling(HORSE_SCALE)));
        event.registerLayerDefinition(ModModelLayers.FARM_DONKEY, () -> LayerDefinition.create(FarmDonkeyGeometry.createMesh(),
                FarmDonkeyGeometry.TEXTURE_WIDTH, FarmDonkeyGeometry.TEXTURE_HEIGHT).apply(MeshTransformer.scaling(DONKEY_SCALE)));
        event.registerLayerDefinition(ModModelLayers.FARM_MULE, () -> LayerDefinition.create(FarmDonkeyGeometry.createMesh(),
                FarmDonkeyGeometry.TEXTURE_WIDTH, FarmDonkeyGeometry.TEXTURE_HEIGHT).apply(MeshTransformer.scaling(MULE_SCALE)));
    }

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.CATTLE.get(), context -> new FarmAnimalRenderer<Cattle>(context,
                new CattleModel(context.bakeLayer(ModModelLayers.CATTLE)), 0.7f, "cattle", "", Cattle.Breed.values(),
                cattle -> cattle.getBreed().ordinal(), (cattle, state, partialTicks) -> { }));

        event.registerEntityRenderer(ModEntities.FARM_PIG.get(), context -> new FarmAnimalRenderer<FarmPig>(context,
                new FarmPigModel(context.bakeLayer(ModModelLayers.FARM_PIG)), 0.7f, "pig", "", FarmPig.Breed.values(),
                pig -> pig.getBreed().ordinal(),
                (pig, state, partialTicks) -> state.saddled = !pig.getItemBySlot(EquipmentSlot.SADDLE).isEmpty()));

        event.registerEntityRenderer(ModEntities.FARM_SHEEP.get(), context -> {
            FarmAnimalRenderer<FarmSheep> renderer = new FarmAnimalRenderer<>(context,
                    new FarmSheepModel(context.bakeLayer(ModModelLayers.FARM_SHEEP), false), 0.7f, "sheep", "", FarmSheep.Breed.values(),
                    sheep -> sheep.getBreed().ordinal(), (sheep, state, partialTicks) -> {
                        state.sheared = sheep.isSheared();
                        state.woolColor = sheep.getColor();
                        state.jeb = sheep.getCustomName() != null && "jeb_".equals(sheep.getCustomName().getString());
                        state.headEatPosition = sheep.getHeadEatPositionScale(partialTicks);
                        state.headEatAngle = sheep.getHeadEatAngleScale(partialTicks);
                    });
            renderer.layer(new FarmSheepWoolLayer(renderer, new FarmSheepModel(context.bakeLayer(ModModelLayers.FARM_SHEEP), true)));
            return renderer;
        });

        event.registerEntityRenderer(ModEntities.FARM_CHICKEN.get(), context -> new FarmAnimalRenderer<FarmChicken>(context,
                new FarmChickenModel(context.bakeLayer(ModModelLayers.FARM_CHICKEN)), 0.3f, "chicken", "chicken_",
                FarmChicken.Breed.values(), chicken -> chicken.getBreed().ordinal(), (chicken, state, partialTicks) -> {
                    state.flap = Mth.lerp(partialTicks, chicken.oFlap, chicken.flap);
                    state.flapSpeed = Mth.lerp(partialTicks, chicken.oFlapSpeed, chicken.flapSpeed);
                }));

        event.registerEntityRenderer(ModEntities.FARM_GOAT.get(), context -> new FarmAnimalRenderer<FarmGoat>(context,
                new FarmGoatModel(context.bakeLayer(ModModelLayers.FARM_GOAT)), 0.7f, "goat", "goat_", FarmGoat.Breed.values(),
                goat -> goat.getBreed().ordinal(), (goat, state, partialTicks) -> {
                    state.leftHorn = goat.hasLeftHorn();
                    state.rightHorn = goat.hasRightHorn();
                    state.rammingXHeadRot = goat.getRammingXHeadRot();
                }));

        event.registerEntityRenderer(ModEntities.HARE.get(), context -> {
            FarmAnimalRenderer<Hare> renderer = new FarmAnimalRenderer<>(context, new HareModel(context.bakeLayer(ModModelLayers.HARE)),
                    0.3f, "hare", "hare_", Hare.Breed.values(), hare -> hare.getBreed().ordinal(),
                    (hare, state, partialTicks) -> state.jumpCompletion = hare.getJumpCompletion(partialTicks));
            renderer.layer(new RenderLayer<>(renderer) {
                @Override
                public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, FarmRenderState state, float yRot,
                        float xRot) {
                    if (state.breed == Hare.Breed.MORDSHASE.ordinal() && !state.isInvisible) {
                        collector.order(1).submitModel(this.getParentModel(), state, poseStack, KILLER_EYES, light,
                                OverlayTexture.NO_OVERLAY, state.outlineColor);
                    }
                }
            });
            return renderer;
        });

        event.registerEntityRenderer(ModEntities.FARM_HORSE.get(), context -> new FarmEquineRenderer<FarmHorse>(context,
                new FarmHorseModel(context.bakeLayer(ModModelLayers.FARM_HORSE)), new FarmHorseModel(context.bakeLayer(ModModelLayers.FARM_HORSE)),
                "horse", "horse_", FarmHorse.Breed.values(), horse -> horse.getBreed().ordinal(),
                EquipmentClientInfo.LayerType.HORSE_SADDLE, ModelLayers.HORSE_SADDLE, true, false));

        event.registerEntityRenderer(ModEntities.FARM_DONKEY.get(), context -> new FarmEquineRenderer<FarmDonkey>(context,
                new FarmDonkeyModel(context.bakeLayer(ModModelLayers.FARM_DONKEY)),
                new FarmDonkeyModel(context.bakeLayer(ModModelLayers.FARM_DONKEY)), "donkey", "donkey_", FarmDonkey.Breed.values(),
                donkey -> donkey.getBreed().ordinal(), EquipmentClientInfo.LayerType.DONKEY_SADDLE, ModelLayers.DONKEY_SADDLE, false,
                false));

        event.registerEntityRenderer(ModEntities.FARM_MULE.get(), context -> new FarmEquineRenderer<FarmMule>(context,
                new FarmDonkeyModel(context.bakeLayer(ModModelLayers.FARM_MULE)),
                new FarmDonkeyModel(context.bakeLayer(ModModelLayers.FARM_MULE)), "donkey", "mule_", FarmMule.Breed.values(),
                mule -> mule.getBreed().ordinal(), EquipmentClientInfo.LayerType.MULE_SADDLE, ModelLayers.MULE_SADDLE, false, true));
    }
}
