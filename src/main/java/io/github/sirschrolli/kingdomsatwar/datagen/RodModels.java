package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.fishing.RodType;
import io.github.sirschrolli.kingdomsatwar.registry.FishingItems;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.properties.conditional.FishingRodCast;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;

/**
 * Die Angeln in 3D (0.90): im Inventar, am Boden und im Rahmen das bisherige Bild, in der Hand das 3D-Modell aus
 * {@link RodShapes} (erzeugt von tools/rod_models.py) – ruhend mit Schnur, Schwimmer oder Köder und Haken, ausgeworfen ohne
 * Schnur (die zeichnet das Spiel bis zum Schwimmer) und mit stärker gebogener Spitze. Gehalten wie die Angel von Minecraft (die
 * Werte ihres Modells "handheld_rod"), damit die Schnur an der Spitze ansetzt.
 *
 * <p>Auch die Angel von Minecraft bekommt ihr 3D-Modell: {@code assets/minecraft/items/fishing_rod.json} (von Hand) verweist auf
 * die hier erzeugten Modelle {@code kingdomsatwar:item/fishing_rod_in_hand} und {@code …_cast_in_hand}.
 */
final class RodModels {
    private RodModels() {}

    static void register(ItemModelGenerators items) {
        rod(items, FishingItems.rod(RodType.IRON), RodType.IRON.id(), RodShapes.IRON_FISHING_ROD, RodShapes.IRON_FISHING_ROD_CAST);
        rod(items, FishingItems.rod(RodType.GOLD), RodType.GOLD.id(), RodShapes.GOLD_FISHING_ROD, RodShapes.GOLD_FISHING_ROD_CAST);
        rod(items, FishingItems.rod(RodType.DRAGON), RodType.DRAGON.id(), RodShapes.DRAGON_FISHING_ROD, RodShapes.DRAGON_FISHING_ROD_CAST);
        rod(items, FishingItems.rod(RodType.SHADOW), RodType.SHADOW.id(), RodShapes.SHADOW_FISHING_ROD, RodShapes.SHADOW_FISHING_ROD_CAST);
        rod(items, FishingItems.rod(RodType.GLUT), RodType.GLUT.id(), RodShapes.GLUT_FISHING_ROD, RodShapes.GLUT_FISHING_ROD_CAST);
        rod(items, FishingItems.rod(RodType.STAR), RodType.STAR.id(), RodShapes.STAR_FISHING_ROD, RodShapes.STAR_FISHING_ROD_CAST);
        // die Angel von Minecraft: nur die Modelle (ihre Gegenstandsbeschreibung liegt von Hand unter assets/minecraft/items)
        inHand(items, "fishing_rod", "", RodShapes.FISHING_ROD, Identifier.withDefaultNamespace("item/fishing_rod"));
        inHand(items, "fishing_rod", "_cast", RodShapes.FISHING_ROD_CAST, Identifier.withDefaultNamespace("item/fishing_rod"));
    }

    private static void rod(ItemModelGenerators items, Item item, String name, float[][] rest, float[][] cast) {
        Identifier icon = KingdomsAtWar.id("item/" + name);
        ItemModel.Unbaked flat = ItemModelUtils.plainModel(items.createFlatItemModel(item, ModelTemplates.FLAT_HANDHELD_ROD_ITEM));
        ItemModel.Unbaked flatCast = ItemModelUtils.plainModel(items.createFlatItemModel(item, "_cast", ModelTemplates.FLAT_HANDHELD_ROD_ITEM));
        ItemModel.Unbaked hand = ItemModelUtils.plainModel(inHand(items, name, "", rest, icon));
        ItemModel.Unbaked handCast = ItemModelUtils.plainModel(inHand(items, name, "_cast", cast, icon));
        items.itemModelOutput.accept(item, ItemModelUtils.conditional(new FishingRodCast(),
                ItemModelGenerators.createFlatModelDispatch(flatCast, handCast), ItemModelGenerators.createFlatModelDispatch(flat, hand)));
    }

    private static Identifier inHand(ItemModelGenerators items, String name, String suffix, float[][] parts, Identifier particle) {
        TextureMapping textures = new TextureMapping()
                .put(MonsterGearModels.MODEL, new Material(KingdomsAtWar.id("item/" + name + "_model")))
                .put(MonsterGearModels.MODEL_GLOW, new Material(KingdomsAtWar.id("item/" + name + "_model_glow")))
                .put(TextureSlot.PARTICLE, new Material(particle));
        // gehalten wie die Angel von Minecraft ("handheld_rod")
        ExtendedModelTemplateBuilder hand = ExtendedModelTemplateBuilder.builder()
                .requiredTextureSlot(MonsterGearModels.MODEL).requiredTextureSlot(MonsterGearModels.MODEL_GLOW).requiredTextureSlot(TextureSlot.PARTICLE)
                .guiLight(net.minecraft.client.resources.model.UnbakedModel.GuiLight.FRONT)
                .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, t -> t.rotation(0, 90, 55).translation(0, 4.0f, 2.5f).scale(0.85f, 0.85f, 0.85f))
                .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND, t -> t.rotation(0, -90, -55).translation(0, 4.0f, 2.5f).scale(0.85f, 0.85f, 0.85f))
                .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, t -> t.rotation(0, 90, 25).translation(0, 1.6f, 0.8f).scale(0.68f, 0.68f, 0.68f))
                .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND, t -> t.rotation(0, -90, -25).translation(0, 1.6f, 0.8f).scale(0.68f, 0.68f, 0.68f));
        MonsterGearModels.addParts(hand, parts);
        return hand.build().create(KingdomsAtWar.id("item/" + name + suffix + "_in_hand"), textures, items.modelOutput);
    }
}
