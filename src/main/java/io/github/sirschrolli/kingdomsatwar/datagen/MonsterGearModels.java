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
 * wie ein Schwert (das Modell steht senkrecht und wird wie das Bild eines Schwerts schräg gelegt). Glühende Teile (Pilze der
 * Moorkeule, Adern des Glutzahns, die Klinge der Seelensense) leuchten mit Lichtstärke 15.
 */
final class MonsterGearModels {
    private MonsterGearModels() {}

    static final TextureSlot MODEL = TextureSlot.create("model");
    static final TextureSlot MODEL_GLOW = TextureSlot.create("model_glow");

    /**
     * Teile je Waffe, senkrecht längs Y um x = z = 8: {von x, y, z, bis x, y, z, Textur u0, v0, u1, v1, Leuchten (0/1)}.
     * Atlas (siehe tools/monster_gear_textures.py): A 0..2 Schaft, B 2..4 Wicklung, C 4..6 Metall/Knochen, D 6..10
     * Klinge/Kopf, E 10..12 Leuchten, F 12..14 Pelz/Moos/Schuppen, G 14..16 Klauen/Schneide.
     */
    private static final float[][] CLAW_BLADE = {
            {7.0f, -3.4f, 7.0f, 9.0f, -2.2f, 9.0f, 4, 0, 6, 1.2f, 0},             // eiserner Knauf
            {7.35f, -2.2f, 7.35f, 8.65f, 3.0f, 8.65f, 2, 0, 3.3f, 5.2f, 0},       // Lederwicklung
            {6.6f, 2.8f, 6.6f, 9.4f, 4.6f, 9.4f, 12, 0, 14, 1.8f, 0},             // Pelzkragen
            {4.8f, 4.4f, 7.0f, 11.2f, 5.6f, 9.0f, 4, 2, 6, 3.2f, 0},              // Faustplatte
            {5.2f, 5.6f, 7.4f, 6.5f, 9.6f, 8.6f, 14, 6, 15.3f, 10, 0},            // linke Klaue
            {4.8f, 9.6f, 7.5f, 6.0f, 13.2f, 8.5f, 14, 2.5f, 15.2f, 6, 0},
            {4.5f, 13.2f, 7.65f, 5.4f, 15.8f, 8.35f, 14, 0, 15, 2.4f, 0},
            {7.35f, 5.6f, 7.4f, 8.65f, 10.4f, 8.6f, 14, 6, 15.3f, 10, 0},         // mittlere Klaue (die längste)
            {7.45f, 10.4f, 7.5f, 8.55f, 14.8f, 8.5f, 14, 2.5f, 15.2f, 6, 0},
            {7.6f, 14.8f, 7.65f, 8.4f, 18.0f, 8.35f, 14, 0, 15, 2.4f, 0},
            {9.5f, 5.6f, 7.4f, 10.8f, 9.6f, 8.6f, 14, 6, 15.3f, 10, 0},           // rechte Klaue
            {10.0f, 9.6f, 7.5f, 11.2f, 13.2f, 8.5f, 14, 2.5f, 15.2f, 6, 0},
            {10.6f, 13.2f, 7.65f, 11.5f, 15.8f, 8.35f, 14, 0, 15, 2.4f, 0},
    };

    private static final float[][] BOG_CLUB = {
            {7.3f, -3.0f, 7.3f, 8.7f, -2.0f, 8.7f, 0, 0, 1.4f, 1, 0},             // Wurzelknoten unten
            {7.45f, -2.0f, 7.45f, 8.55f, 4.0f, 8.55f, 0, 1, 1.1f, 7, 0},          // dünner Wurzelgriff
            {7.3f, 0.0f, 7.3f, 8.7f, 2.4f, 8.7f, 2, 0, 3.4f, 2.4f, 0},            // Moosband
            {7.15f, 4.0f, 7.15f, 8.85f, 8.0f, 8.85f, 0, 7, 1.7f, 11, 0},          // der Schaft wird dicker
            {6.4f, 8.0f, 6.4f, 9.6f, 15.0f, 9.6f, 6, 0, 9.2f, 7, 0},              // knorriger Kopf
            {5.6f, 9.6f, 6.9f, 6.4f, 12.6f, 9.1f, 6, 7, 6.8f, 10, 0},             // Knorren
            {9.6f, 10.6f, 6.8f, 10.5f, 13.4f, 9.0f, 7, 7, 8, 10, 0},
            {6.6f, 12.0f, 9.6f, 9.0f, 14.0f, 10.4f, 6, 10, 8.4f, 12, 0},
            {6.8f, 15.0f, 6.8f, 9.2f, 16.2f, 9.2f, 12, 0, 14, 1.2f, 0},           // Moospolster
            {9.6f, 13.0f, 7.4f, 11.4f, 13.6f, 8.8f, 10, 0, 11.8f, 0.6f, 1},       // leuchtende Pilze
            {10.2f, 12.2f, 7.8f, 10.8f, 13.0f, 8.4f, 10, 1, 10.6f, 1.8f, 1},
            {4.8f, 11.2f, 7.6f, 6.4f, 11.8f, 8.8f, 10, 2, 11.6f, 2.6f, 1},
            {5.3f, 10.4f, 7.9f, 5.9f, 11.2f, 8.5f, 10, 3, 10.6f, 3.8f, 1},
            {7.6f, 16.2f, 7.6f, 8.8f, 16.7f, 8.8f, 10, 4, 11.2f, 4.5f, 1},
            {6.6f, 6.0f, 8.0f, 6.9f, 8.6f, 8.3f, 1, 8, 1.3f, 10.6f, 0},           // hängende Wurzelfasern
            {9.2f, 5.6f, 7.7f, 9.5f, 8.2f, 8.0f, 1, 8, 1.3f, 10.6f, 0},
    };

    private static final float[][] EMBER_FANG = {
            {7.1f, -3.4f, 7.1f, 8.9f, -2.0f, 8.9f, 4, 0, 6, 1.4f, 0},             // Basaltknauf
            {7.6f, -3.0f, 6.9f, 8.4f, -2.4f, 7.1f, 14, 0, 14.8f, 0.6f, 1},        // Glutsteine im Knauf
            {7.6f, -3.0f, 8.9f, 8.4f, -2.4f, 9.1f, 14, 0, 14.8f, 0.6f, 1},
            {7.4f, -2.0f, 7.4f, 8.6f, 3.6f, 8.6f, 2, 0, 3.2f, 5.6f, 0},           // Griff
            {4.9f, 3.6f, 7.1f, 11.1f, 4.8f, 8.9f, 12, 0, 14, 2, 0},               // Parierstange aus Schuppen
            {4.4f, 4.8f, 7.3f, 5.6f, 6.2f, 8.7f, 12, 2, 13.2f, 3.4f, 0},
            {10.4f, 4.8f, 7.3f, 11.6f, 6.2f, 8.7f, 12, 2, 13.2f, 3.4f, 0},
            {6.9f, 4.8f, 7.55f, 9.3f, 8.6f, 8.45f, 6, 11, 10, 15, 0},             // die gebogene Fangklinge
            {7.2f, 8.6f, 7.6f, 9.6f, 12.0f, 8.4f, 6, 7.5f, 10, 11, 0},
            {7.7f, 12.0f, 7.65f, 9.8f, 15.0f, 8.35f, 6.5f, 4.5f, 9.5f, 7.5f, 0},
            {8.3f, 15.0f, 7.7f, 9.9f, 17.6f, 8.3f, 7, 2, 9, 4.5f, 0},
            {8.9f, 17.6f, 7.75f, 9.7f, 19.6f, 8.25f, 7.5f, 0, 8.5f, 2, 0},
            {7.95f, 5.2f, 7.5f, 8.35f, 9.0f, 8.5f, 10, 0, 11, 4, 1},              // die glühende Ader
            {8.3f, 9.0f, 7.55f, 8.7f, 12.4f, 8.45f, 10, 4, 11, 8, 1},
            {8.65f, 12.4f, 7.6f, 9.05f, 15.2f, 8.4f, 10, 8, 11, 12, 1},
            {9.0f, 15.2f, 7.65f, 9.35f, 17.2f, 8.35f, 10, 12, 11, 15, 1},
    };

    private static final float[][] SOUL_SCYTHE = {
            {7.2f, -4.0f, 7.2f, 8.8f, -2.8f, 8.8f, 4, 0, 6, 1.2f, 0},             // Knochenkappe
            {7.5f, -2.8f, 7.5f, 8.5f, 17.0f, 8.5f, 0, 0, 1, 16, 0},               // Schaft aus Aschenholz
            {7.3f, 1.0f, 7.3f, 8.7f, 3.4f, 8.7f, 2, 0, 3.4f, 2.4f, 0},            // Wicklungen aus Aschenschleier
            {7.3f, 9.0f, 7.3f, 8.7f, 10.6f, 8.7f, 2, 3, 3.4f, 4.6f, 0},
            {6.9f, 16.6f, 6.9f, 9.1f, 19.2f, 9.1f, 4, 2, 6, 4.6f, 0},             // Knochenfassung
            {7.3f, 19.2f, 7.3f, 8.7f, 20.2f, 8.7f, 4, 5, 5.4f, 6, 0},
            {1.4f, 17.4f, 7.7f, 6.9f, 19.4f, 8.3f, 6, 0, 10, 2, 1},               // Klinge aus Seelenfeuer
            {-0.4f, 16.0f, 7.75f, 2.4f, 18.6f, 8.25f, 6, 2.5f, 8.8f, 5, 1},
            {-1.2f, 14.2f, 7.8f, 0.6f, 16.4f, 8.2f, 6, 5.5f, 7.8f, 7.7f, 1},
            {-1.4f, 13.0f, 7.85f, -0.4f, 14.4f, 8.15f, 6, 8, 7, 9.4f, 1},
            {1.0f, 19.2f, 7.8f, 6.9f, 19.9f, 8.2f, 14, 0, 16, 0.7f, 0},           // dunkler Klingenrücken
            {9.1f, 17.8f, 7.8f, 10.8f, 18.8f, 8.2f, 4, 7, 6, 8, 0},               // Gegenstachel
            {7.6f, 19.0f, 6.7f, 8.4f, 19.8f, 6.9f, 10, 0, 10.8f, 0.8f, 1},        // Seelenflamme an der Fassung
    };

    static void register(ItemModelGenerators items) {
        weapon(items, MonsterGear.CLAW_BLADE.get(), "claw_blade", CLAW_BLADE);
        weapon(items, MonsterGear.BOG_CLUB.get(), "bog_club", BOG_CLUB);
        weapon(items, MonsterGear.EMBER_FANG_BLADE.get(), "ember_fang_blade", EMBER_FANG);
        weapon(items, MonsterGear.SOUL_SCYTHE.get(), "soul_scythe", SOUL_SCYTHE);
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
                        t -> t.rotation(0, -90, 25).translation(1.13f, 3.2f, 1.13f).scale(0.68f, 0.68f, 0.68f))
                .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND,
                        t -> t.rotation(0, 90, -25).translation(1.13f, 3.2f, 1.13f).scale(0.68f, 0.68f, 0.68f));
        for (float[] p : parts) {
            boolean glow = p[10] > 0;
            hand.element(e -> {
                e.from(p[0], p[1], p[2]).to(p[3], p[4], p[5]);
                e.allFaces((direction, face) -> {
                    face.texture(glow ? MODEL_GLOW : MODEL);
                    if (direction.getAxis() == Direction.Axis.Y) {
                        face.uvs(p[6], p[7], Math.min(p[8], p[6] + 1.5f), Math.min(p[9], p[7] + 1.5f));
                    } else {
                        face.uvs(p[6], p[7], p[8], p[9]);
                    }
                });
                if (glow) {
                    e.lightEmission(15);
                }
                // senkrecht gebaut, wie das Bild eines Schwerts schräg gelegt
                e.rotation(r -> r.origin(8, 8, 8).eulerXYZ(0, 0, -45));
            });
        }
        Identifier inHand = hand.build().create(ModelLocationUtils.getModelLocation(item, "_in_hand"), textures, items.modelOutput);
        var flat = ItemModelUtils.plainModel(items.createFlatItemModel(item, ModelTemplates.FLAT_HANDHELD_ITEM));
        items.itemModelOutput.accept(item, ItemModelGenerators.createFlatModelDispatch(flat, ItemModelUtils.plainModel(inHand)),
                new ClientItem.Properties(true, false, 1.0f));
    }
}
