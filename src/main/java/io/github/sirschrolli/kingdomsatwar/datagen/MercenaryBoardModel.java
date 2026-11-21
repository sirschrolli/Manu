package io.github.sirschrolli.kingdomsatwar.datagen;

import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.generators.template.ElementBuilder;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;

/**
 * Modell der Söldnertafel (Blick nach Norden): zwei Pfosten mit Fußstreben, dazwischen ein Brett voller Anschläge
 * (vorn) bzw. Bretter (hinten), oben ein Abdeckbrett gegen den Regen.
 */
final class MercenaryBoardModel {
    static final TextureSlot POST = TextureSlot.create("post");
    static final TextureSlot POST_END = TextureSlot.create("post_end");
    static final TextureSlot BOARD = TextureSlot.create("board");
    static final TextureSlot BACK = TextureSlot.create("back");
    static final TextureSlot ROOF = TextureSlot.create("roof");

    private MercenaryBoardModel() {}

    static ModelTemplate template() {
        ExtendedModelTemplateBuilder b = ExtendedModelTemplateBuilder.builder()
                .parent(Identifier.withDefaultNamespace("block/block"))
                .requiredTextureSlot(POST).requiredTextureSlot(POST_END).requiredTextureSlot(BOARD).requiredTextureSlot(BACK)
                .requiredTextureSlot(ROOF).requiredTextureSlot(TextureSlot.PARTICLE);
        // Pfosten
        b.element(e -> post(e, 0, 2));
        b.element(e -> post(e, 14, 16));
        // Fußstreben vorn und hinten an jedem Pfosten
        for (float x : new float[] {0.25f, 14.25f}) {
            b.element(e -> box(e, x, 0, 5, x + 1.5f, 1.5f, 7, POST));
            b.element(e -> box(e, x, 0, 9, x + 1.5f, 1.5f, 11, POST));
        }
        // das Brett: vorn die Anschläge, hinten Bretter
        b.element(e -> e.from(2, 3, 7.5f).to(14, 15, 8.5f)
                .face(Direction.NORTH, f -> f.uvs(2, 2, 14, 14).texture(BOARD))
                .face(Direction.SOUTH, f -> f.uvs(2, 2, 14, 14).texture(BACK))
                .face(Direction.DOWN, f -> f.uvs(2, 7, 14, 8).texture(BACK)));
        // Querleiste unten
        b.element(e -> box(e, 2, 1.5f, 7.75f, 14, 2.5f, 8.25f, POST));
        // Abdeckbrett gegen den Regen
        b.element(e -> e.from(-0.5f, 15, 6.5f).to(16.5f, 16.25f, 9.5f)
                .face(Direction.UP, f -> f.uvs(0, 6.5f, 16, 9.5f).texture(ROOF))
                .face(Direction.DOWN, f -> f.uvs(0, 6.5f, 16, 9.5f).texture(ROOF))
                .face(Direction.NORTH, f -> f.uvs(0, 4, 16, 5.25f).texture(ROOF))
                .face(Direction.SOUTH, f -> f.uvs(0, 4, 16, 5.25f).texture(ROOF))
                .face(Direction.EAST, f -> f.uvs(6.5f, 4, 9.5f, 5.25f).texture(ROOF))
                .face(Direction.WEST, f -> f.uvs(6.5f, 4, 9.5f, 5.25f).texture(ROOF)));
        return b.build();
    }

    private static void post(ElementBuilder e, float x1, float x2) {
        e.from(x1, 0, 7).to(x2, 15, 9).allFaces((direction, face) -> {
            switch (direction) {
                case UP, DOWN -> face.uvs(7, 7, 9, 9).texture(POST_END);
                case NORTH, SOUTH -> face.uvs(x1, 1, x2, 16).texture(POST);
                default -> face.uvs(7, 1, 9, 16).texture(POST);
            }
        });
    }

    /** Quader mit derselben Textur auf allen Seiten; die UVs folgen der Lage im Block. */
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
}
