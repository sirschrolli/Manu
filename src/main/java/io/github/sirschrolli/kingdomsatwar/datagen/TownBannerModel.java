package io.github.sirschrolli.kingdomsatwar.datagen;

import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.generators.template.ElementBuilder;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;

/**
 * Modell des Stadtbanners (Tuch nach Norden): unten ein zweistufiger Steinsockel mit Goldring und dem Mast, oben die
 * Querstange mit Goldkappen, der Goldknauf und das Tuch, das über beide Blöcke vor dem Mast herabhängt.
 */
final class TownBannerModel {
    static final TextureSlot STONE = TextureSlot.create("stone");
    static final TextureSlot SLAB = TextureSlot.create("slab");
    static final TextureSlot POLE = TextureSlot.create("pole");
    static final TextureSlot GOLD = TextureSlot.create("gold");
    static final TextureSlot CLOTH = TextureSlot.create("cloth");

    private TownBannerModel() {}

    /** Untere Hälfte: Sockel, Goldring, Mast und das untere Stück Tuch (mit dem Schwalbenschwanz). */
    static ModelTemplate lower() {
        ExtendedModelTemplateBuilder b = ExtendedModelTemplateBuilder.builder()
                .parent(Identifier.withDefaultNamespace("block/block"))
                .requiredTextureSlot(STONE).requiredTextureSlot(SLAB).requiredTextureSlot(POLE).requiredTextureSlot(GOLD)
                .requiredTextureSlot(CLOTH).requiredTextureSlot(TextureSlot.PARTICLE);
        b.element(e -> box(e, 3, 0, 3, 13, 2, 13, STONE));
        b.element(e -> box(e, 4, 2, 4, 12, 4, 12, SLAB));
        b.element(e -> box(e, 6.5f, 4, 6.5f, 9.5f, 5, 9.5f, GOLD));
        b.element(e -> box(e, 7, 5, 7, 9, 16, 9, POLE));
        b.element(e -> cloth(e, 6, 16, 0, 10));
        return b.build();
    }

    /** Obere Hälfte: Mast, Knauf, Querstange mit Kappen und das obere Stück Tuch. */
    static ModelTemplate upper() {
        ExtendedModelTemplateBuilder b = ExtendedModelTemplateBuilder.builder()
                .parent(Identifier.withDefaultNamespace("block/block"))
                .requiredTextureSlot(POLE).requiredTextureSlot(GOLD).requiredTextureSlot(CLOTH).requiredTextureSlot(TextureSlot.PARTICLE);
        b.element(e -> box(e, 7, 0, 7, 9, 14, 9, POLE));
        b.element(e -> box(e, 6.5f, 14, 6.5f, 9.5f, 14.75f, 9.5f, GOLD));
        b.element(e -> box(e, 7.25f, 14.75f, 7.25f, 8.75f, 16.25f, 8.75f, GOLD));
        b.element(e -> box(e, 1.5f, 12, 5.75f, 14.5f, 13.25f, 7, POLE));
        b.element(e -> box(e, 0.5f, 11.75f, 5.5f, 1.5f, 13.5f, 7.25f, GOLD));
        b.element(e -> box(e, 14.5f, 11.75f, 5.5f, 15.5f, 13.5f, 7.25f, GOLD));
        b.element(e -> cloth(e, 0, 12, 4, 16));
        return b.build();
    }

    /**
     * Ein Stück Tuch (u 2..14) von {@code y1} bis {@code y2}, dünn vor dem Mast; die Rückseite zeigt das Bild gespiegelt.
     * {@code v1}..{@code v2} ist der Bereich in der Textur.
     */
    private static ElementBuilder cloth(ElementBuilder e, float y1, float y2, float v1, float v2) {
        return e.from(2, y1, 6.25f).to(14, y2, 6.5f)
                .face(Direction.NORTH, f -> f.uvs(2, v1, 14, v2).texture(CLOTH))
                .face(Direction.SOUTH, f -> f.uvs(14, v1, 2, v2).texture(CLOTH))
                .face(Direction.WEST, f -> f.uvs(2, v1, 2.25f, v2).texture(CLOTH))
                .face(Direction.EAST, f -> f.uvs(13.75f, v1, 14, v2).texture(CLOTH));
    }

    /** Quader mit derselben Textur auf allen Seiten; die UVs folgen der Lage im Block. */
    private static void box(ElementBuilder e, float x1, float y1, float z1, float x2, float y2, float z2, TextureSlot slot) {
        e.from(x1, y1, z1).to(x2, y2, z2).allFaces((direction, face) -> {
            switch (direction) {
                case UP, DOWN -> face.uvs(x1, z1, x2, z2);
                case NORTH, SOUTH -> face.uvs(x1, 16 - Math.min(16, y2), x2, 16 - y1);
                default -> face.uvs(z1, 16 - Math.min(16, y2), z2, 16 - y1);
            }
            face.texture(slot);
        });
    }
}
