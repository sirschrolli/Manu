package io.github.sirschrolli.kingdomsatwar.datagen;

import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.generators.template.ElementBuilder;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;

/**
 * Modell des Magietischs: Sockel aus Arkanstein mit Goldband, an den Ecken Aetherit-Kristalle, eine Säule mit einer
 * leuchtenden Runenrille und die Platte mit dem eingelegten Siegelkreis. Die leuchtenden Teile (Runen, Kristalle) liegen
 * als eigene Flächen genau auf dem Stein (wie die Grasdecke von Vanilla) und leuchten aus sich heraus.
 */
final class MagicTableModel {
    static final TextureSlot BASE = TextureSlot.create("base");
    static final TextureSlot BASE_TOP = TextureSlot.create("base_top");
    static final TextureSlot BOTTOM = TextureSlot.create("bottom");
    static final TextureSlot GOLD = TextureSlot.create("gold");
    static final TextureSlot COLUMN = TextureSlot.create("column");
    static final TextureSlot COLUMN_GLOW = TextureSlot.create("column_glow");
    static final TextureSlot TOP = TextureSlot.create("top");
    static final TextureSlot TOP_GLOW = TextureSlot.create("top_glow");
    static final TextureSlot TOP_SIDE = TextureSlot.create("top_side");
    static final TextureSlot TOP_SIDE_GLOW = TextureSlot.create("top_side_glow");
    static final TextureSlot CRYSTAL = TextureSlot.create("crystal");

    private MagicTableModel() {}

    static ModelTemplate template() {
        ExtendedModelTemplateBuilder b = ExtendedModelTemplateBuilder.builder()
                .parent(Identifier.withDefaultNamespace("block/block"))
                .requiredTextureSlot(BASE).requiredTextureSlot(BASE_TOP).requiredTextureSlot(BOTTOM).requiredTextureSlot(GOLD)
                .requiredTextureSlot(COLUMN).requiredTextureSlot(COLUMN_GLOW).requiredTextureSlot(TOP).requiredTextureSlot(TOP_GLOW)
                .requiredTextureSlot(TOP_SIDE).requiredTextureSlot(TOP_SIDE_GLOW).requiredTextureSlot(CRYSTAL)
                .requiredTextureSlot(TextureSlot.PARTICLE);
        // Sockel und Goldband
        b.element(e -> box(e, 1, 0, 1, 15, 3, 15, BASE, BASE_TOP, BOTTOM));
        b.element(e -> box(e, 1.5f, 3, 1.5f, 14.5f, 4, 14.5f, GOLD, GOLD, GOLD));
        // Säule mit leuchtender Runenrille
        b.element(e -> box(e, 4, 4, 4, 12, 10, 12, COLUMN, COLUMN, COLUMN));
        b.element(e -> sides(e, 4, 4, 4, 12, 10, 12, COLUMN_GLOW).lightEmission(13));
        // Aetherit-Kristalle an den vier Ecken des Sockels
        for (float[] c : new float[][] {{2, 2}, {12.5f, 2}, {2, 12.5f}, {12.5f, 12.5f}}) {
            b.element(e -> box(e, c[0], 4, c[1], c[0] + 1.5f, 7.5f, c[1] + 1.5f, CRYSTAL, CRYSTAL, CRYSTAL).lightEmission(10));
            b.element(e -> box(e, c[0] + 0.25f, 7.5f, c[1] + 0.25f, c[0] + 1.25f, 8.75f, c[1] + 1.25f, CRYSTAL, CRYSTAL, CRYSTAL).lightEmission(10));
        }
        // Platte mit Siegelkreis
        b.element(e -> box(e, 0, 10, 0, 16, 13, 16, TOP_SIDE, TOP, BASE_TOP));
        b.element(e -> e.from(0, 13, 0).to(16, 13, 16)
                .face(Direction.UP, f -> f.uvs(0, 0, 16, 16).texture(TOP_GLOW)).lightEmission(15));
        b.element(e -> sides(e, 0, 10, 0, 16, 13, 16, TOP_SIDE_GLOW).lightEmission(12));
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
