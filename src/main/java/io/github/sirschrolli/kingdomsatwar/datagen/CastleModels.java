package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.CastleGear;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;

/**
 * Modelle zur Raubritterburg (seit 1.8): das Rabenbanner (als Block der eiserne Fuß und der Pfahl aus dunkler Eiche mit
 * Eisenbändern; Knauf, Querholz und das wehende Banner zeichnet der Block-Renderer), der Morgenstern (im Inventar das Bild, in
 * der Hand das 3D-Modell) und das Spawn-Ei des Raubritters.
 */
final class CastleModels {
    private CastleModels() {}

    private static final TextureSlot POLE = TextureSlot.create("pole");
    private static final TextureSlot IRON = TextureSlot.create("iron");

    static void register(BlockModelGenerators blocks, ItemModelGenerators items) {
        banner(blocks);
        items.generateFlatItem(CastleGear.RAVEN_BANNER.get().asItem(), ModelTemplates.FLAT_ITEM);
        MonsterGearModels.weapon(items, CastleGear.MORNING_STAR.get(), "morning_star", ArmoryShapes.MORNING_STAR, 0.45f, 3.0f, 2.5f);
        ModModelProvider.flat(items, CastleGear.ROBBER_KNIGHT_SPAWN_EGG.get());
    }

    /** Ein Quader; die Seiten zeigen den Ausschnitt, der ihrer Lage im Block entspricht (über 16 hinaus: verschoben). */
    private static void box(ExtendedModelTemplateBuilder b, float x0, float y0, float z0, float x1, float y1, float z1, TextureSlot tex) {
        b.element(e -> {
            e.from(x0, y0, z0).to(x1, y1, z1);
            float[] u = span(x0, x1);
            float[] w = span(z0, z1);
            float[] v = span(16.0f - y1, 16.0f - y0);
            e.allFaces((direction, face) -> {
                switch (direction) {
                    case UP, DOWN -> face.uvs(u[0], w[0], u[1], w[1]).texture(tex);
                    case NORTH, SOUTH -> face.uvs(u[0], v[0], u[1], v[1]).texture(tex);
                    default -> face.uvs(w[0], v[0], w[1], v[1]).texture(tex);
                }
                if (direction == Direction.DOWN && y0 <= 0.0f) {
                    face.cullface(Direction.DOWN);
                }
            });
        });
    }

    /** Der Ausschnitt der Textur (0..16) für eine Strecke von c0 bis c1: dieselbe Länge, in die Textur geschoben. */
    private static float[] span(float c0, float c1) {
        float len = Math.min(16.0f, c1 - c0);
        float a = c0 % 16.0f;
        if (a < 0.0f) {
            a += 16.0f;
        }
        if (a + len > 16.0f) {
            a = 16.0f - len;
        }
        return new float[] {a, a + len};
    }

    /**
     * Das Modell des Blocks: der eiserne Fuß und der Pfahl aus dunkler Eiche mit Eisenbändern (zwei Blöcke hoch). Was darüber
     * ist – das obere Stück des Pfahls mit dem goldenen Knauf, das Querholz und das wehende Banner (zerrissen: Fetzen) –
     * zeichnet der Block-Renderer ({@code RavenBannerRenderer}).
     */
    private static Identifier bannerModel(BlockModelGenerators blocks, Block banner) {
        ExtendedModelTemplateBuilder b = ExtendedModelTemplateBuilder.builder().parent(Identifier.withDefaultNamespace("block/block"))
                .requiredTextureSlot(POLE).requiredTextureSlot(IRON).requiredTextureSlot(TextureSlot.PARTICLE);
        // der eiserne Fuß und der Pfahl (in zwei Stücken, damit die Maserung nicht gestreckt wird)
        box(b, 5.0f, 0.0f, 5.0f, 11.0f, 1.5f, 11.0f, IRON);
        box(b, 6.25f, 1.5f, 6.25f, 9.75f, 2.5f, 9.75f, IRON);
        box(b, 7.0f, 2.5f, 7.0f, 9.0f, 16.0f, 9.0f, POLE);
        box(b, 7.0f, 16.0f, 7.0f, 9.0f, 32.0f, 9.0f, POLE);
        // Eisenbänder
        box(b, 6.75f, 9.0f, 6.75f, 9.25f, 10.0f, 9.25f, IRON);
        box(b, 6.75f, 22.0f, 6.75f, 9.25f, 23.0f, 9.25f, IRON);
        TextureMapping textures = new TextureMapping()
                .put(POLE, new Material(KingdomsAtWar.id("block/raven_banner_pole")))
                .put(IRON, new Material(KingdomsAtWar.id("block/raven_banner_iron")))
                .put(TextureSlot.PARTICLE, new Material(KingdomsAtWar.id("block/raven_banner_pole")));
        return b.build().create(banner, textures, blocks.modelOutput);
    }

    private static void banner(BlockModelGenerators blocks) {
        Block banner = CastleGear.RAVEN_BANNER.get();
        Identifier pole = bannerModel(blocks, banner);
        blocks.blockStateOutput.accept(MultiVariantGenerator.dispatch(banner, BlockModelGenerators.plainVariant(pole))
                .with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING));
    }
}
