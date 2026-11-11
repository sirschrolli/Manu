package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.MonsterGear;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;

/**
 * Modelle der Waffen und Rüstungen aus Monsterteilen (0.70): im Inventar das Bild, in der Hand ein 3D-Modell, gehalten
 * wie ein Schwert (das Modell steht senkrecht und wird wie das Bild eines Schwerts schräg gelegt). Die Teile – Quader und
 * gebogene Stränge aus gedrehten Gliedern – kommen aus {@link MonsterGearShapes} (erzeugt von tools/monster_gear_models.py).
 * Glühende Teile (Moorherz und Pilze, die Ader des Glutzahns, Schneide und Augen der Seelensense) leuchten mit Lichtstärke 15.
 */
final class MonsterGearModels {
    private MonsterGearModels() {}

    static final TextureSlot MODEL = TextureSlot.create("model");
    static final TextureSlot MODEL_GLOW = TextureSlot.create("model_glow");

    static void register(ItemModelGenerators items) {
        weapon(items, MonsterGear.CLAW_BLADE.get(), "claw_blade", MonsterGearShapes.CLAW_BLADE);
        weapon(items, MonsterGear.BOG_CLUB.get(), "bog_club", MonsterGearShapes.BOG_CLUB);
        weapon(items, MonsterGear.EMBER_FANG_BLADE.get(), "ember_fang_blade", MonsterGearShapes.EMBER_FANG);
        weapon(items, MonsterGear.SOUL_SCYTHE.get(), "soul_scythe", MonsterGearShapes.SOUL_SCYTHE);
        for (var part : MonsterGear.parts()) {
            ModModelProvider.flat(items, part.get());
        }
        ModModelProvider.armor(items, MonsterGear.PELT_HELMET.get(), MonsterGear.PELT_CHESTPLATE.get(), MonsterGear.PELT_LEGGINGS.get(),
                MonsterGear.PELT_BOOTS.get());
        ModModelProvider.armor(items, MonsterGear.EMBER_HELMET.get(), MonsterGear.EMBER_CHESTPLATE.get(), MonsterGear.EMBER_LEGGINGS.get(),
                MonsterGear.EMBER_BOOTS.get());
    }

    private static Material tex(String path) {
        return new Material(KingdomsAtWar.id(path));
    }

    private static void weapon(ItemModelGenerators items, Item item, String name, float[][] parts) {
        weapon(items, item, name, parts, 0.68f, 0.0f);
    }

    /**
     * Auch für die Waffenkammer (0.84): Bild im Inventar, das schräg gelegte 3D-Modell in der Hand. Lange Waffen dürfen aus
     * eigener Sicht kleiner erscheinen ({@code firstPersonScale}, Minecraft: 0.68) und ein Stück zur Bildmitte rücken
     * ({@code firstPersonShift}, in Sechzehntel Blöcken), sonst ragen ihre Köpfe aus dem Bild.
     */
    static void weapon(ItemModelGenerators items, Item item, String name, float[][] parts, float firstPersonScale, float firstPersonShift) {
        float handX = 1.13f - firstPersonShift;
        TextureMapping textures = new TextureMapping()
                .put(MODEL, tex("item/" + name + "_model"))
                .put(MODEL_GLOW, tex("item/" + name + "_model_glow"))
                .put(TextureSlot.PARTICLE, tex("item/" + name));
        // wie ein Schwert gehalten (die Werte des Vanilla-Modells "handheld")
        ExtendedModelTemplateBuilder hand = ExtendedModelTemplateBuilder.builder()
                .requiredTextureSlot(MODEL).requiredTextureSlot(MODEL_GLOW).requiredTextureSlot(TextureSlot.PARTICLE)
                .guiLight(net.minecraft.client.resources.model.UnbakedModel.GuiLight.FRONT)
                .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, t -> t.rotation(0, -90, 55).translation(0, 4.0f, 0.5f).scale(0.85f, 0.85f, 0.85f))
                .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND, t -> t.rotation(0, 90, -55).translation(0, 4.0f, 0.5f).scale(0.85f, 0.85f, 0.85f))
                .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND,
                        t -> t.rotation(0, -90, 25).translation(handX, 3.2f, 1.13f).scale(firstPersonScale, firstPersonScale, firstPersonScale))
                .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND,
                        t -> t.rotation(0, 90, -25).translation(handX, 3.2f, 1.13f).scale(firstPersonScale, firstPersonScale, firstPersonScale));
        // je Teil: die eigene Drehung um seinen Drehpunkt und das Schräglegen (-45° um Z durch die Mitte) als EINE Drehung –
        // beide drehen zuerst um X/Y, zuletzt um Z, also addieren sich die Z-Winkel; der Drehpunkt wandert mit
        double tilt = Math.toRadians(-45.0);
        for (float[] p : parts) {
            boolean glow = p[18] > 0;
            float px = p[22];
            float py = p[23];
            float pz = p[24];
            double dx = px - 8.0;
            double dy = py - 8.0;
            float tx = (float) (8.0 + dx * Math.cos(tilt) - dy * Math.sin(tilt) - px);
            float ty = (float) (8.0 + dx * Math.sin(tilt) + dy * Math.cos(tilt) - py);
            hand.element(e -> {
                e.from(p[0] + tx, p[1] + ty, p[2]).to(p[3] + tx, p[4] + ty, p[5]);
                e.allFaces((direction, face) -> {
                    face.texture(glow ? MODEL_GLOW : MODEL);
                    int i = switch (direction.getAxis()) {
                        case Y -> 14;
                        case X -> 10;
                        case Z -> 6;
                    };
                    // vorn und hinten zeigt die Textur gleich herum (links im Bild = -x)
                    if (direction == Direction.NORTH) {
                        face.uvs(p[i + 2], p[i + 1], p[i], p[i + 3]);
                    } else {
                        face.uvs(p[i], p[i + 1], p[i + 2], p[i + 3]);
                    }
                });
                if (glow) {
                    e.lightEmission(15);
                }
                e.rotation(r -> r.origin(px + tx, py + ty, pz).eulerXYZ(p[19], p[20], p[21] - 45.0f));
            });
        }
        Identifier inHand = hand.build().create(ModelLocationUtils.getModelLocation(item, "_in_hand"), textures, items.modelOutput);
        var flat = ItemModelUtils.plainModel(items.createFlatItemModel(item, ModelTemplates.FLAT_HANDHELD_ITEM));
        items.itemModelOutput.accept(item, ItemModelGenerators.createFlatModelDispatch(flat, ItemModelUtils.plainModel(inHand)),
                new ClientItem.Properties(true, false, 1.0f));
    }
}
