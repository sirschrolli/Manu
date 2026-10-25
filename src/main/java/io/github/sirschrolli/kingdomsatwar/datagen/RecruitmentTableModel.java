package io.github.sirschrolli.kingdomsatwar.datagen;

import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.generators.template.ElementBuilder;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;

/**
 * Modell des Rekrutierungstischs (Blickrichtung Norden): Holztisch mit Beinen, Zargen und Streben,
 * blauer Tischläufer mit Wappenbehang, ein aufgeschlagenes Buch, ein Häufchen Smaragde und ein Tintenfass
 * mit Schreibfeder.
 */
final class RecruitmentTableModel {
    static final TextureSlot WOOD = TextureSlot.create("wood");
    static final TextureSlot LEG = TextureSlot.create("leg");
    static final TextureSlot CLOTH = TextureSlot.create("cloth");
    static final TextureSlot BANNER = TextureSlot.create("banner");
    static final TextureSlot ITEMS = TextureSlot.create("items");

    private RecruitmentTableModel() {}

    static ModelTemplate template() {
        ExtendedModelTemplateBuilder b = ExtendedModelTemplateBuilder.builder()
                .parent(Identifier.withDefaultNamespace("block/block"))
                .requiredTextureSlot(WOOD).requiredTextureSlot(LEG).requiredTextureSlot(CLOTH)
                .requiredTextureSlot(BANNER).requiredTextureSlot(ITEMS).requiredTextureSlot(TextureSlot.PARTICLE);
        // Tischplatte
        b.element(e -> box(e, 0, 13, 0, 16, 15, 16, WOOD));
        // Beine
        for (float[] p : new float[][] {{1, 1}, {13, 1}, {1, 13}, {13, 13}}) {
            b.element(e -> box(e, p[0], 0, p[1], p[0] + 2, 13, p[1] + 2, LEG));
        }
        // Zargen unter der Platte
        b.element(e -> box(e, 3, 11, 1.5f, 13, 13, 2.5f, WOOD));
        b.element(e -> box(e, 3, 11, 13.5f, 13, 13, 14.5f, WOOD));
        b.element(e -> box(e, 1.5f, 11, 3, 2.5f, 13, 13, WOOD));
        b.element(e -> box(e, 13.5f, 11, 3, 14.5f, 13, 13, WOOD));
        // Streben unten
        b.element(e -> box(e, 1.5f, 2, 3, 2.5f, 3, 13, LEG));
        b.element(e -> box(e, 13.5f, 2, 3, 14.5f, 3, 13, LEG));
        b.element(e -> box(e, 2.5f, 2, 7.5f, 13.5f, 3, 8.5f, LEG));
        // Tischläufer und Behang vorn/hinten
        b.element(e -> e.from(4, 15, 0).to(12, 15.25f, 16)
                .face(Direction.UP, f -> f.uvs(4, 0, 12, 16).texture(CLOTH))
                .face(Direction.EAST, f -> f.uvs(0, 0, 16, 0.25f).texture(CLOTH))
                .face(Direction.WEST, f -> f.uvs(0, 0, 16, 0.25f).texture(CLOTH)));
        b.element(e -> e.from(4, 8, -0.25f).to(12, 15.25f, 0)
                .face(Direction.NORTH, f -> f.uvs(4, 0, 12, 8).texture(BANNER))
                .face(Direction.SOUTH, f -> f.uvs(4, 0, 12, 8).texture(BANNER))
                .face(Direction.EAST, f -> f.uvs(4, 0, 4.25f, 8).texture(CLOTH))
                .face(Direction.WEST, f -> f.uvs(4, 0, 4.25f, 8).texture(CLOTH))
                .face(Direction.DOWN, f -> f.uvs(4, 0, 12, 0.25f).texture(CLOTH)));
        b.element(e -> e.from(4, 10, 16).to(12, 15.25f, 16.25f)
                .face(Direction.SOUTH, f -> f.uvs(4, 3, 12, 8).texture(BANNER))
                .face(Direction.NORTH, f -> f.uvs(4, 3, 12, 8).texture(BANNER))
                .face(Direction.EAST, f -> f.uvs(4, 0, 4.25f, 5).texture(CLOTH))
                .face(Direction.WEST, f -> f.uvs(4, 0, 4.25f, 5).texture(CLOTH))
                .face(Direction.DOWN, f -> f.uvs(4, 0, 12, 0.25f).texture(CLOTH)));
        // aufgeschlagenes Buch (leicht gedreht)
        b.element(e -> rotatedY(e.from(2.5f, 15.25f, 5).to(9.5f, 15.6f, 11)
                .face(Direction.UP, f -> f.uvs(0, 0, 9, 7).texture(ITEMS))
                .face(Direction.NORTH, f -> f.uvs(0, 0, 7, 0.5f).texture(ITEMS))
                .face(Direction.SOUTH, f -> f.uvs(0, 6, 7, 6.5f).texture(ITEMS))
                .face(Direction.EAST, f -> f.uvs(0, 1, 6, 1.5f).texture(ITEMS))
                .face(Direction.WEST, f -> f.uvs(0, 1, 6, 1.5f).texture(ITEMS))));
        b.element(e -> rotatedY(e.from(3, 15.6f, 5.5f).to(6, 16.1f, 10.5f)
                .face(Direction.UP, f -> f.uvs(0, 8, 4, 14).texture(ITEMS))
                .face(Direction.NORTH, f -> f.uvs(0, 8, 3, 8.5f).texture(ITEMS))
                .face(Direction.SOUTH, f -> f.uvs(0, 8, 3, 8.5f).texture(ITEMS))
                .face(Direction.WEST, f -> f.uvs(0, 8, 5, 8.5f).texture(ITEMS))));
        b.element(e -> rotatedY(e.from(6, 15.6f, 5.5f).to(9, 16.1f, 10.5f)
                .face(Direction.UP, f -> f.uvs(5, 8, 9, 14).texture(ITEMS))
                .face(Direction.NORTH, f -> f.uvs(5, 8, 8, 8.5f).texture(ITEMS))
                .face(Direction.SOUTH, f -> f.uvs(5, 8, 8, 8.5f).texture(ITEMS))
                .face(Direction.EAST, f -> f.uvs(5, 8, 10, 8.5f).texture(ITEMS))));
        // Smaragde
        b.element(e -> e.from(10.5f, 15.25f, 9).to(12, 16.5f, 10.5f).allFaces((d, f) -> f.uvs(9, 0, 12, 3).texture(ITEMS)));
        b.element(e -> e.from(12, 15.25f, 10.2f).to(13.2f, 16.1f, 11.4f).allFaces((d, f) -> f.uvs(9, 14, 11, 16).texture(ITEMS)));
        b.element(e -> e.from(11.2f, 15.25f, 11.2f).to(12.2f, 15.9f, 12.2f).allFaces((d, f) -> f.uvs(9, 14, 11, 16).texture(ITEMS)));
        // Tintenfass mit Feder
        b.element(e -> e.from(11, 15.25f, 4).to(12.5f, 16.5f, 5.5f).allFaces((d, f) -> f.uvs(9, 5, 12, 8).texture(ITEMS)));
        b.element(e -> e.from(11.75f, 16, 3.25f).to(11.75f, 21, 5.75f)
                .face(Direction.EAST, f -> f.uvs(12, 4, 15, 14).texture(ITEMS))
                .face(Direction.WEST, f -> f.uvs(12, 4, 15, 14).texture(ITEMS))
                .rotation(r -> r.origin(11.75f, 16, 4.75f).singleAxis(Direction.Axis.X, -22.5f)));
        return b.build();
    }

    /** Quader mit derselben Textur auf allen Seiten; die UVs folgen der Lage im Block (wie bei Vollblöcken). */
    private static void box(ElementBuilder e, float x1, float y1, float z1, float x2, float y2, float z2, TextureSlot slot) {
        e.from(x1, y1, z1).to(x2, y2, z2).allFaces((direction, face) -> {
            switch (direction) {
                case UP, DOWN -> face.uvs(x1, z1, x2, z2);
                case NORTH, SOUTH -> face.uvs(x1, 16 - y2, x2, 16 - y1);
                default -> face.uvs(z1, 16 - y2, z2, 16 - y1);
            }
            face.texture(slot);
        });
    }

    private static ElementBuilder rotatedY(ElementBuilder e) {
        return e.rotation(r -> r.origin(6, 15.5f, 8).singleAxis(Direction.Axis.Y, 22.5f));
    }
}
