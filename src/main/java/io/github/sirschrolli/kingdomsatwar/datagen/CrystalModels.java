package io.github.sirschrolli.kingdomsatwar.datagen;

import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;

/**
 * Modelle der Herz- und Manakristalle, wie sie in Höhlen wachsen: auf einem Brocken Fels ein geschliffenes Herz aus
 * rotem Kristall (mit erhabenem Kern und zwei kleinen Splittern daneben) bzw. ein Grüppchen blauer Kristallspitzen, die
 * schräg aus dem Fels wachsen. Der Kristall leuchtet (Lichtstärke der Flächen 12). Die Vorderseite schaut nach Norden,
 * gedreht wird über den Blockzustand.
 */
final class CrystalModels {
    static final TextureSlot CRYSTAL = TextureSlot.create("crystal");
    static final TextureSlot ROCK = TextureSlot.create("rock");
    private static final int GLOW = 12;

    private CrystalModels() {}

    private static ExtendedModelTemplateBuilder builder() {
        return ExtendedModelTemplateBuilder.builder()
                .parent(Identifier.withDefaultNamespace("block/block"))
                .requiredTextureSlot(TextureSlot.PARTICLE)
                .requiredTextureSlot(CRYSTAL)
                .requiredTextureSlot(ROCK);
    }

    /** Der Felsbrocken, auf dem der Kristall wächst. */
    private static void rock(ExtendedModelTemplateBuilder b) {
        cube(b, 3, 0, 4, 12, 2, 12, ROCK, 0, 0, 0, 0);
        cube(b, 5, 0, 3, 13, 1.5f, 11, ROCK, 0, 0, 0, 0);
        cube(b, 5, 2, 5, 10.5f, 3, 10, ROCK, 0, 0, 0, 0);
        cube(b, 9, 1.5f, 8, 12.5f, 2.5f, 11.5f, ROCK, 0, 0, 0, 0);
    }

    /**
     * Ein Quader (Texturkoordinaten nach seiner Lage im Block) – Kristall leuchtet; gedreht um seinen Fuß
     * (Mitte der Unterseite), wenn ein Winkel angegeben ist.
     */
    private static void cube(ExtendedModelTemplateBuilder b, float x1, float y1, float z1, float x2, float y2, float z2, TextureSlot slot,
            int light, float rx, float ry, float rz) {
        b.element(e -> {
            e.from(x1, y1, z1).to(x2, y2, z2).allFaces((direction, face) -> {
                float v1 = 16 - y2;
                float v2 = 16 - y1;
                switch (direction) {
                    case DOWN -> face.uvs(x1, 16 - z2, x2, 16 - z1);
                    case UP -> face.uvs(x1, z1, x2, z2);
                    case NORTH -> face.uvs(16 - x2, v1, 16 - x1, v2);
                    case SOUTH -> face.uvs(x1, v1, x2, v2);
                    case WEST -> face.uvs(z1, v1, z2, v2);
                    case EAST -> face.uvs(16 - z2, v1, 16 - z1, v2);
                }
                face.texture(slot);
            });
            if (light > 0) {
                e.lightEmission(light);
            }
            if (rx != 0 || ry != 0 || rz != 0) {
                e.rotation(r -> r.origin((x1 + x2) / 2, y1, (z1 + z2) / 2).eulerXYZ(rx, ry, rz));
            }
        });
    }

    private static void gem(ExtendedModelTemplateBuilder b, float x1, float y1, float z1, float x2, float y2, float z2, float rx, float ry,
            float rz) {
        cube(b, x1, y1, z1, x2, y2, z2, CRYSTAL, GLOW, rx, ry, rz);
    }

    /** Das Herz aus Kristall: zwei Bögen oben, nach unten spitz zulaufend, in der Mitte erhaben; zwei Splitter im Fels. */
    static ModelTemplate heart() {
        ExtendedModelTemplateBuilder b = builder();
        rock(b);
        float z1 = 6.5f;
        float z2 = 9.5f;
        gem(b, 4, 12, z1, 7, 13, z2, 0, 0, 0);
        gem(b, 9, 12, z1, 12, 13, z2, 0, 0, 0);
        gem(b, 3, 9, z1, 13, 12, z2, 0, 0, 0);
        gem(b, 4, 8, z1, 12, 9, z2, 0, 0, 0);
        gem(b, 5, 7, z1, 11, 8, z2, 0, 0, 0);
        gem(b, 6, 6, z1, 10, 7, z2, 0, 0, 0);
        gem(b, 7, 5, z1, 9, 6, z2, 0, 0, 0);
        gem(b, 7.5f, 3, 7.5f, 8.5f, 5, 8.5f, 0, 0, 0);
        // erhabener Kern: die Mitte des Herzens steht vorn und hinten ein wenig vor
        gem(b, 4.5f, 9.5f, 6, 6.5f, 11.5f, 10, 0, 0, 0);
        gem(b, 9.5f, 9.5f, 6, 11.5f, 11.5f, 10, 0, 0, 0);
        gem(b, 6, 7.5f, 6, 10, 10.5f, 10, 0, 0, 0);
        // kleine Splitter, schräg aus dem Fels
        gem(b, 3.5f, 2, 8.5f, 5, 6, 10, 0, 0, 20);
        gem(b, 11, 1.5f, 4.5f, 12.5f, 5, 6, -20, 0, -15);
        return b.build();
    }

    /** Die Manakristalle: eine hohe Spitze in der Mitte, um sie herum vier kleinere, schräg aus dem Fels gewachsen. */
    static ModelTemplate mana() {
        ExtendedModelTemplateBuilder b = builder();
        rock(b);
        gem(b, 6.5f, 2, 6.5f, 9.5f, 12, 9.5f, 0, 45, 0);
        gem(b, 7, 12, 7, 9, 14, 9, 0, 45, 0);
        gem(b, 7.5f, 14, 7.5f, 8.5f, 15.5f, 8.5f, 0, 45, 0);
        gem(b, 3.5f, 2, 7, 5.5f, 9, 9, 0, 0, 25);
        gem(b, 4, 9, 7.5f, 5, 10.5f, 8.5f, 0, 0, 25);
        gem(b, 10.5f, 2, 7, 12.5f, 8, 9, 0, 0, -25);
        gem(b, 7, 2, 10.5f, 9, 7.5f, 12.5f, 22.5f, 0, 0);
        gem(b, 7.5f, 2, 3.5f, 9, 5.5f, 5, -25, 20, 0);
        return b.build();
    }
}
