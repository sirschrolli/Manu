package io.github.sirschrolli.kingdomsatwar.datagen;

import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.neoforge.client.model.generators.template.ElementBuilder;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;

/**
 * Modelle der Einrichtung: Stuhl, Bank, Tisch, Thron, Kronleuchter und Zinne. Sitzmöbel schauen mit der
 * Vorderseite nach Norden (Lehne im Süden), gedreht wird über den Blockzustand. Die Umrisse dazu stehen in
 * {@code block/FurnitureShapes}.
 */
final class DecorModels {
    /** Bretter (Sitzfläche, Tischplatte, Lehne). */
    static final TextureSlot WOOD = TextureSlot.create("wood");
    /** Rahmenholz (Beine, Pfosten). */
    static final TextureSlot FRAME = TextureSlot.create("frame");
    static final TextureSlot CLOTH = TextureSlot.create("cloth");
    static final TextureSlot GOLD = TextureSlot.create("gold");

    private DecorModels() {}

    private static ExtendedModelTemplateBuilder builder(TextureSlot... slots) {
        ExtendedModelTemplateBuilder b = ExtendedModelTemplateBuilder.builder()
                .parent(Identifier.withDefaultNamespace("block/block"))
                .requiredTextureSlot(TextureSlot.PARTICLE);
        for (TextureSlot slot : slots) {
            b.requiredTextureSlot(slot);
        }
        return b;
    }

    // ------------------------------------------------------------------ Stuhl, Bank, Tisch

    static ModelTemplate chair() {
        ExtendedModelTemplateBuilder b = builder(WOOD, FRAME);
        for (float[] p : new float[][] {{3, 3}, {11, 3}, {3, 11}, {11, 11}}) {
            b.element(e -> box(e, p[0], 0, p[1], p[0] + 2, 7, p[1] + 2, FRAME));      // Beine
        }
        b.element(e -> box(e, 3.5f, 2, 5, 4.5f, 3, 11, FRAME));                       // Querstreben
        b.element(e -> box(e, 11.5f, 2, 5, 12.5f, 3, 11, FRAME));
        b.element(e -> box(e, 2, 7, 2, 14, 9, 14, WOOD));                             // Sitzfläche
        b.element(e -> box(e, 3, 9, 11, 5, 16, 13, FRAME));                           // Lehnenpfosten
        b.element(e -> box(e, 11, 9, 11, 13, 16, 13, FRAME));
        b.element(e -> box(e, 5, 13, 11.5f, 11, 15, 12.5f, WOOD));                    // Lehne: oberes Brett
        b.element(e -> box(e, 6.5f, 9, 11.75f, 7.5f, 13, 12.25f, WOOD));              // zwei Sprossen
        b.element(e -> box(e, 8.5f, 9, 11.75f, 9.5f, 13, 12.25f, WOOD));
        return b.build();
    }

    static ModelTemplate bench() {
        ExtendedModelTemplateBuilder b = builder(WOOD, FRAME);
        b.element(e -> box(e, 1, 0, 4, 3, 7, 12, FRAME));                             // Wangen
        b.element(e -> box(e, 13, 0, 4, 15, 7, 12, FRAME));
        b.element(e -> box(e, 0, 7, 3, 16, 9, 13, WOOD));                             // Sitzbrett
        b.element(e -> box(e, 1, 9, 11, 3, 16, 13, FRAME));                           // Pfosten
        b.element(e -> box(e, 13, 9, 11, 15, 16, 13, FRAME));
        b.element(e -> box(e, 0, 13, 11.5f, 16, 15, 12.5f, WOOD));                    // Lehnenbretter
        b.element(e -> box(e, 0, 10, 11.5f, 16, 11.5f, 12.5f, WOOD));
        return b.build();
    }

    static ModelTemplate tableTop() {
        return builder(WOOD).element(e -> box(e, 0, 14, 0, 16, 16, 16, WOOD)).build();
    }

    /** Tischbein an der Nordwest-Ecke; die anderen Ecken entstehen durch Drehen. */
    static ModelTemplate tableLeg() {
        return builder(FRAME).element(e -> box(e, 1, 0, 1, 3, 14, 3, FRAME)).build();
    }

    /** Ganzer Tisch für das Inventar. */
    static ModelTemplate tableItem() {
        ExtendedModelTemplateBuilder b = builder(WOOD, FRAME);
        b.element(e -> box(e, 0, 14, 0, 16, 16, 16, WOOD));
        for (float[] p : new float[][] {{1, 1}, {13, 1}, {1, 13}, {13, 13}}) {
            b.element(e -> box(e, p[0], 0, p[1], p[0] + 2, 14, p[1] + 2, FRAME));
        }
        return b.build();
    }

    // ------------------------------------------------------------------ Thron

    static ModelTemplate throne() {
        ExtendedModelTemplateBuilder b = builder(WOOD, CLOTH, GOLD)
                // Der Thron ist fast zwei Blöcke hoch: im Inventar kleiner zeigen
                .transform(ItemDisplayContext.GUI, t -> t.rotation(30, 225, 0).translation(0, -2.75f, 0).scale(0.42f))
                .transform(ItemDisplayContext.FIXED, t -> t.translation(0, -3, 0).scale(0.34f))
                .transform(ItemDisplayContext.GROUND, t -> t.translation(0, 2, 0).scale(0.18f))
                .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, t -> t.rotation(75, 45, 0).translation(0, 1.5f, 0).scale(0.26f))
                .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, t -> t.rotation(0, 45, 0).translation(0, -1, 0).scale(0.28f))
                .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND, t -> t.rotation(0, 225, 0).translation(0, -1, 0).scale(0.28f));
        b.element(e -> box(e, 0, 0, 0, 16, 2, 16, WOOD));                             // Podest
        b.element(e -> box(e, 0.5f, 2, 0.5f, 15.5f, 2.5f, 15.5f, GOLD));              // Goldkante
        b.element(e -> box(e, 1, 2.5f, 1, 15, 7, 15, WOOD));                          // Sitzkasten
        b.element(e -> box(e, 2, 7, 1.5f, 14, 8.5f, 13, CLOTH));                      // Sitzkissen
        b.element(e -> box(e, 0, 7, 1, 2, 12, 13, WOOD));                             // Armlehnen
        b.element(e -> box(e, 14, 7, 1, 16, 12, 13, WOOD));
        b.element(e -> box(e, 0, 12, 0.5f, 2, 13, 13, GOLD));
        b.element(e -> box(e, 14, 12, 0.5f, 16, 13, 13, GOLD));
        // Hohe Lehne (unten und oben getrennt, damit die Texturen nicht verzerrt werden)
        b.element(e -> box(e, 1, 7, 13, 15, 16, 16, WOOD));
        b.element(e -> box(e, 1, 16, 13, 15, 26, 16, WOOD));
        b.element(e -> box(e, 3, 9, 12.5f, 13, 16, 13, CLOTH));
        b.element(e -> box(e, 3, 16, 12.5f, 13, 24, 13, CLOTH));
        b.element(e -> box(e, 0, 7, 13, 1, 16, 16, GOLD));                            // goldener Rahmen
        b.element(e -> box(e, 15, 7, 13, 16, 16, 16, GOLD));
        b.element(e -> box(e, 0, 16, 13, 1, 26, 16, GOLD));
        b.element(e -> box(e, 15, 16, 13, 16, 26, 16, GOLD));
        b.element(e -> box(e, 0, 26, 13, 16, 27, 16, GOLD));
        // Krone obenauf
        b.element(e -> box(e, 1, 27, 13.5f, 3, 29, 15.5f, GOLD));
        b.element(e -> box(e, 4.5f, 27, 13.75f, 5.5f, 28, 15.25f, GOLD));
        b.element(e -> box(e, 7, 27, 13.5f, 9, 31, 15.5f, GOLD));
        b.element(e -> box(e, 10.5f, 27, 13.75f, 11.5f, 28, 15.25f, GOLD));
        b.element(e -> box(e, 13, 27, 13.5f, 15, 29, 15.5f, GOLD));
        return b.build();
    }

    // ------------------------------------------------------------------ Kronleuchter

    /**
     * Kronleuchter aus einer einzigen Textur ({@code block/chandelier}): Eisen (u0-8, v0-8), Wachs (u8-12, v0-8),
     * Flamme (u12-16, v0-4), Gold (u12-16, v4-8).
     */
    static ModelTemplate chandelier() {
        TextureSlot tex = TextureSlot.ALL;
        ExtendedModelTemplateBuilder b = builder(tex).ambientOcclusion(false);
        b.element(e -> atlas(e, 7.5f, 9, 7.5f, 8.5f, 16, 8.5f, tex, 0, 0));          // Aufhängung
        b.element(e -> atlas(e, 6.5f, 7, 6.5f, 9.5f, 10, 9.5f, tex, 12, 4));         // Nabe
        b.element(e -> atlas(e, 7, 5, 7, 9, 7, 9, tex, 12, 4));                       // Zapfen unten
        b.element(e -> atlas(e, 7.5f, 4, 7.5f, 8.5f, 5, 8.5f, tex, 13, 5));
        b.element(e -> atlas(e, 9.5f, 7.5f, 7.5f, 14, 8.5f, 8.5f, tex, 1, 2));       // vier Arme
        b.element(e -> atlas(e, 2, 7.5f, 7.5f, 6.5f, 8.5f, 8.5f, tex, 1, 2));
        b.element(e -> atlas(e, 7.5f, 7.5f, 9.5f, 8.5f, 8.5f, 14, tex, 1, 2));
        b.element(e -> atlas(e, 7.5f, 7.5f, 2, 8.5f, 8.5f, 6.5f, tex, 1, 2));
        // Eisenring, der die Arme verbindet
        b.element(e -> atlas(e, 4, 7.75f, 3.5f, 12, 8.25f, 4, tex, 0, 4));
        b.element(e -> atlas(e, 4, 7.75f, 12, 12, 8.25f, 12.5f, tex, 0, 4));
        b.element(e -> atlas(e, 3.5f, 7.75f, 4, 4, 8.25f, 12, tex, 0, 4));
        b.element(e -> atlas(e, 12, 7.75f, 4, 12.5f, 8.25f, 12, tex, 0, 4));
        for (float[] c : CANDLES) {
            float x = c[0], z = c[1], y = c[2];
            b.element(e -> atlas(e, x - 1, y, z - 1, x + 1, y + 1, z + 1, tex, 12, 5));       // Tülle
            b.element(e -> atlas(e, x - 0.5f, y + 1, z - 0.5f, x + 0.5f, y + 1 + c[3], z + 0.5f, tex, 9, 1)); // Kerze
            float top = y + 1 + c[3];
            // Flamme: zwei gekreuzte, selbstleuchtende Flächen
            b.element(e -> e.from(x - 0.5f, top, z).to(x + 0.5f, top + 2, z).lightEmission(15)
                    .face(Direction.NORTH, f -> f.uvs(12, 0, 16, 4).texture(tex))
                    .face(Direction.SOUTH, f -> f.uvs(12, 0, 16, 4).texture(tex)));
            b.element(e -> e.from(x, top, z - 0.5f).to(x, top + 2, z + 0.5f).lightEmission(15)
                    .face(Direction.EAST, f -> f.uvs(12, 0, 16, 4).texture(tex))
                    .face(Direction.WEST, f -> f.uvs(12, 0, 16, 4).texture(tex)));
        }
        return b.build();
    }

    /**
     * Kerzen des Kronleuchters: x, z, Höhe der Tülle, Kerzenlänge. Vier große an den Armenden, vier kleinere auf den
     * Ecken des Rings. Die Dochtspitzen stehen auch in {@code block/ChandelierBlock} (Flammen-Partikel).
     */
    static final float[][] CANDLES = {
            {2.5f, 8, 8.5f, 3}, {13.5f, 8, 8.5f, 3}, {8, 2.5f, 8.5f, 3}, {8, 13.5f, 8.5f, 3},
            {4, 4, 8.25f, 2}, {12, 4, 8.25f, 2}, {4, 12, 8.25f, 2}, {12, 12, 8.25f, 2}};

    // ------------------------------------------------------------------ Festmahl

    /** Holzbrett. */
    static final TextureSlot BOARD = TextureSlot.create("board");
    /**
     * Speisen aus einer Textur ({@code block/feast}): Kruste (u0-8, v0-8), Anschnitt (u8-12, v0-4), Knochen
     * (u12-16, v0-2), Möhre (u8-12, v4-6), Kartoffel (u12-16, v4-6), Kräuter (u8-10, v6-8), Apfel (u10-12, v6-8),
     * Brotlaib (u12-16, v6-10).
     */
    static final TextureSlot FOOD = TextureSlot.create("food");

    /**
     * Festmahl mit {@code eaten} gegessenen Portionen (0-3): Der Braten wird von Westen her angeschnitten, die
     * Beilagen bleiben liegen. Blick nach Norden.
     */
    static ModelTemplate feast(int eaten) {
        ExtendedModelTemplateBuilder b = builder(BOARD, FOOD);
        b.element(e -> box(e, 1, 0, 1, 15, 1, 15, BOARD));                                    // Brett
        // Beilagen
        b.element(e -> atlas(e, 2, 1, 2.5f, 5, 2, 3.5f, FOOD, 8, 4));                        // Möhren
        b.element(e -> atlas(e, 11, 1, 12.5f, 14, 2, 13.5f, FOOD, 8, 4));
        b.element(e -> atlas(e, 2, 1, 12, 4, 2.5f, 14, FOOD, 12, 4));                        // Kartoffeln
        b.element(e -> atlas(e, 12, 1, 2, 14, 2.5f, 4, FOOD, 12, 4));
        b.element(e -> atlas(e, 5.5f, 1, 12.5f, 7, 1.5f, 14, FOOD, 8, 6));                   // Kräuter
        b.element(e -> atlas(e, 1.5f, 1, 6.5f, 3, 2.5f, 8, FOOD, 10, 6));                    // Apfel
        b.element(e -> atlas(e, 5, 1, 1.5f, 8.5f, 3, 3.5f, FOOD, 12, 6));                    // Brotlaib
        // Braten mit Knochen
        float west = 4 + eaten * 2;
        b.element(e -> roast(e, west, 1, 5, 11, 5, 11, eaten > 0));
        if (5 + eaten * 2 < 10) {
            b.element(e -> roast(e, 5 + eaten * 2, 5, 6, 10, 6, 10, eaten > 0));
        }
        b.element(e -> atlas(e, 11, 2.5f, 7.5f, 13.5f, 3.5f, 8.5f, FOOD, 12, 0));             // Knochen
        b.element(e -> atlas(e, 13.5f, 2, 7, 14.5f, 4, 9, FOOD, 12, 0));
        return b.build();
    }

    /** Bratenstück: Kruste rundherum, nach dem Anschneiden zeigt die Westseite das rosige Fleisch. */
    private static void roast(ElementBuilder e, float x1, float y1, float z1, float x2, float y2, float z2, boolean cut) {
        atlas(e, x1, y1, z1, x2, y2, z2, FOOD, 0, 0);
        if (cut) {
            e.face(Direction.WEST, f -> f.uvs(8, 0, 12, 4).texture(FOOD));
        }
    }

    // ------------------------------------------------------------------ Zinne

    /** Halbhohe Brüstung über die volle Breite, Mauerzahn mittig (Reihe in Ost-West-Richtung). */
    static ModelTemplate battlement() {
        return builder(TextureSlot.ALL)
                .element(e -> box(e, 0, 0, 0, 16, 8, 16, TextureSlot.ALL))
                .element(e -> box(e, 4, 8, 0, 12, 16, 16, TextureSlot.ALL))
                .build();
    }

    // ------------------------------------------------------------------ Hilfen

    /**
     * Quader mit derselben Textur auf allen Seiten; die UVs folgen der Lage im Block genau wie bei Vanilla-Stufen
     * und -Treppen, damit die Muster nahtlos an Vollblöcke anschließen. Teile oberhalb des Blocks (y 16-32)
     * nehmen dieselben Texturzeilen wie unten. Seiten am Blockrand werden weggelassen, wenn der Nachbarblock
     * sie verdeckt.
     */
    static void box(ElementBuilder e, float x1, float y1, float z1, float x2, float y2, float z2, TextureSlot slot) {
        float shift = y1 >= 16 ? 16 : 0;
        float v1 = 16 - (y2 - shift), v2 = 16 - (y1 - shift);
        e.from(x1, y1, z1).to(x2, y2, z2).allFaces((direction, face) -> {
            switch (direction) {
                case DOWN -> face.uvs(x1, 16 - z2, x2, 16 - z1);
                case UP -> face.uvs(x1, z1, x2, z2);
                case NORTH -> face.uvs(16 - x2, v1, 16 - x1, v2);
                case SOUTH -> face.uvs(x1, v1, x2, v2);
                case WEST -> face.uvs(z1, v1, z2, v2);
                case EAST -> face.uvs(16 - z2, v1, 16 - z1, v2);
            }
            face.texture(slot);
            boolean onEdge = switch (direction) {
                case DOWN -> y1 == 0;
                case UP -> y2 == 16;
                case NORTH -> z1 == 0;
                case SOUTH -> z2 == 16;
                case WEST -> x1 == 0;
                case EAST -> x2 == 16;
            };
            if (onEdge) {
                face.cullface(direction);
            }
        });
    }

    /** Quader, dessen Seiten einen Ausschnitt ab ({@code u}, {@code v}) der gemeinsamen Textur zeigen. */
    private static void atlas(ElementBuilder e, float x1, float y1, float z1, float x2, float y2, float z2,
            TextureSlot slot, float u, float v) {
        float w = x2 - x1, h = y2 - y1, d = z2 - z1;
        e.from(x1, y1, z1).to(x2, y2, z2).allFaces((direction, face) -> {
            switch (direction) {
                case UP, DOWN -> face.uvs(u, v, u + w, v + d);
                case NORTH, SOUTH -> face.uvs(u, v, u + w, v + h);
                default -> face.uvs(u, v, u + d, v + h);
            }
            face.texture(slot);
        });
    }
}
