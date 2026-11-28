package io.github.sirschrolli.kingdomsatwar.datagen;

import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.generators.template.ElementBuilder;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;

/**
 * Modell des Wegsteins (seit 1.12): Steinsockel mit Stufe, ein Schaft mit eingemeißelter Rune, die von innen leuchtet, ein
 * Bronzeband, Kappe und Spitze mit einer glimmenden Kristallfassung und vier Bronzekrallen. Darüber schwebt der Kristall
 * (den zeichnet {@code WaystoneRenderer}). Die Texturen liegen wie im Block: Jede Fläche nimmt sich die Pixel, die an ihrer
 * Stelle im Block liegen.
 */
final class WaystoneModel {
    static final TextureSlot BASE_SIDE = TextureSlot.create("base_side");
    static final TextureSlot BASE_TOP = TextureSlot.create("base_top");
    static final TextureSlot SHAFT = TextureSlot.create("shaft");
    static final TextureSlot SHAFT_GLOW = TextureSlot.create("shaft_glow");
    static final TextureSlot BRONZE = TextureSlot.create("bronze");
    static final TextureSlot CAP_SIDE = TextureSlot.create("cap_side");
    static final TextureSlot CAP_TOP = TextureSlot.create("cap_top");
    static final TextureSlot CAP_GLOW = TextureSlot.create("cap_glow");

    private WaystoneModel() {}

    static ModelTemplate template() {
        ExtendedModelTemplateBuilder b = ExtendedModelTemplateBuilder.builder()
                .parent(Identifier.withDefaultNamespace("block/block"))
                .requiredTextureSlot(BASE_SIDE).requiredTextureSlot(BASE_TOP).requiredTextureSlot(SHAFT).requiredTextureSlot(SHAFT_GLOW)
                .requiredTextureSlot(BRONZE).requiredTextureSlot(CAP_SIDE).requiredTextureSlot(CAP_TOP).requiredTextureSlot(CAP_GLOW)
                .requiredTextureSlot(TextureSlot.PARTICLE);
        // Sockel und Stufe
        b.element(e -> box(e, 1, 0, 1, 15, 2, 15, BASE_SIDE, BASE_TOP, BASE_TOP));
        b.element(e -> box(e, 2, 2, 2, 14, 3, 14, BASE_SIDE, BASE_TOP, BASE_TOP));
        // Schaft mit leuchtender Rune
        b.element(e -> box(e, 3, 3, 3, 13, 12, 13, SHAFT, CAP_TOP, BASE_TOP));
        b.element(e -> sides(e, 3, 3, 3, 13, 12, 13, SHAFT_GLOW).lightEmission(12));
        // Bronzeband, Kappe, Spitze
        b.element(e -> box(e, 2.5f, 12, 2.5f, 13.5f, 13, 13.5f, BRONZE, BRONZE, BRONZE));
        b.element(e -> box(e, 3, 13, 3, 13, 14, 13, CAP_SIDE, CAP_TOP, CAP_TOP));
        b.element(e -> box(e, 4, 14, 4, 12, 15, 12, CAP_SIDE, CAP_TOP, CAP_TOP));
        b.element(e -> e.from(4, 15, 4).to(12, 15, 12)
                .face(Direction.UP, f -> f.uvs(4, 4, 12, 12).texture(CAP_GLOW)).lightEmission(10));
        // vier Bronzekrallen der Kristallfassung
        for (float[] c : new float[][] {{4.5f, 4.5f}, {10.5f, 4.5f}, {4.5f, 10.5f}, {10.5f, 10.5f}}) {
            b.element(e -> box(e, c[0], 15, c[1], c[0] + 1, 16, c[1] + 1, BRONZE, BRONZE, BRONZE));
        }
        return b.build();
    }

    /** Quader: Seiten {@code side}, oben {@code top}, unten {@code bottom}; die UVs folgen der Lage im Block. */
    private static ElementBuilder box(ElementBuilder e, float x1, float y1, float z1, float x2, float y2, float z2,
            TextureSlot side, TextureSlot top, TextureSlot bottom) {
        return e.from(x1, y1, z1).to(x2, y2, z2).allFaces((direction, face) -> {
            switch (direction) {
                case UP -> face.uvs(x1, z1, x2, z2).texture(top);
                case DOWN -> face.uvs(x1, z1, x2, z2).texture(bottom);
                case NORTH, SOUTH -> face.uvs(x1, 16 - y2, x2, 16 - y1).texture(side);
                default -> face.uvs(z1, 16 - y2, z2, 16 - y1).texture(side);
            }
        });
    }

    /** Nur die vier Seiten (für leuchtende Auflagen). */
    private static ElementBuilder sides(ElementBuilder e, float x1, float y1, float z1, float x2, float y2, float z2, TextureSlot slot) {
        e.from(x1, y1, z1).to(x2, y2, z2);
        for (Direction d : Direction.Plane.HORIZONTAL) {
            e.face(d, f -> {
                if (d.getAxis() == Direction.Axis.X) {
                    f.uvs(z1, 16 - y2, z2, 16 - y1);
                } else {
                    f.uvs(x1, 16 - y2, x2, 16 - y1);
                }
                f.texture(slot);
            });
        }
        return e;
    }
}
