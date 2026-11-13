package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.castle.RavenBannerBlock;
import io.github.sirschrolli.kingdomsatwar.registry.CastleGear;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;

/**
 * Modelle zur Raubritterburg (seit 1.8): das Rabenbanner (ein eiserner Fuß, der hohe Pfahl aus dunkler Eiche mit Eisenbändern
 * und goldenem Knauf, das Querholz mit goldenen Spitzen und daran das Banner – zerrissen nur noch Fetzen), der Morgenstern
 * (im Inventar das Bild, in der Hand das 3D-Modell) und das Spawn-Ei des Raubritters.
 */
final class CastleModels {
    private CastleModels() {}

    private static final TextureSlot POLE = TextureSlot.create("pole");
    private static final TextureSlot IRON = TextureSlot.create("iron");
    private static final TextureSlot GOLD = TextureSlot.create("gold");
    private static final TextureSlot CLOTH = TextureSlot.create("cloth");

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
     * Das Modell (Blick nach Norden: dort hängt das Banner, dorthin blickt der Ritter): Fuß, Pfahl, Bänder, Knauf, Querholz und
     * das Tuch (vorn das Bild, hinten gespiegelt). {@code torn}: nur noch Fetzen.
     */
    private static Identifier bannerModel(BlockModelGenerators blocks, Block banner, String suffix, boolean torn) {
        ExtendedModelTemplateBuilder b = ExtendedModelTemplateBuilder.builder().parent(Identifier.withDefaultNamespace("block/block"))
                .requiredTextureSlot(POLE).requiredTextureSlot(IRON).requiredTextureSlot(GOLD).requiredTextureSlot(CLOTH)
                .requiredTextureSlot(TextureSlot.PARTICLE);
        // der eiserne Fuß und der Pfahl (in zwei Stücken, damit die Maserung nicht gestreckt wird)
        box(b, 5.0f, 0.0f, 5.0f, 11.0f, 1.5f, 11.0f, IRON);
        box(b, 7.0f, 1.5f, 7.0f, 9.0f, 16.0f, 9.0f, POLE);
        box(b, 7.0f, 16.0f, 7.0f, 9.0f, 30.0f, 9.0f, POLE);
        // Eisenbänder, der goldene Knauf
        box(b, 6.75f, 5.0f, 6.75f, 9.25f, 6.0f, 9.25f, IRON);
        box(b, 6.75f, 18.0f, 6.75f, 9.25f, 19.0f, 9.25f, IRON);
        box(b, 6.5f, 30.0f, 6.5f, 9.5f, 31.5f, 9.5f, GOLD);
        box(b, 7.25f, 31.5f, 7.25f, 8.75f, 32.0f, 8.75f, GOLD);
        // das Querholz mit goldenen Spitzen und eisernem Beschlag am Pfahl
        box(b, -0.5f, 27.6f, 6.0f, 16.5f, 29.0f, 7.4f, POLE);
        box(b, -1.5f, 27.4f, 5.8f, -0.5f, 29.2f, 7.6f, GOLD);
        box(b, 16.5f, 27.4f, 5.8f, 17.5f, 29.2f, 7.6f, GOLD);
        box(b, 6.6f, 27.2f, 5.8f, 9.4f, 29.4f, 7.6f, IRON);
        // das Tuch: vorn das Bild, hinten gespiegelt, die Kanten schmal
        b.element(e -> {
            e.from(0.0f, 5.0f, 5.35f).to(16.0f, 29.0f, 5.75f);
            e.allFaces((direction, face) -> {
                switch (direction) {
                    case NORTH -> face.uvs(0.0f, 0.0f, 16.0f, 16.0f).texture(CLOTH);
                    case SOUTH -> face.uvs(16.0f, 0.0f, 0.0f, 16.0f).texture(CLOTH);
                    case UP, DOWN -> face.uvs(0.0f, 0.0f, 16.0f, 0.3f).texture(CLOTH);
                    default -> face.uvs(0.0f, 0.0f, 0.3f, 16.0f).texture(CLOTH);
                }
            });
        });
        TextureMapping textures = new TextureMapping()
                .put(POLE, new Material(KingdomsAtWar.id("block/raven_banner_pole")))
                .put(IRON, new Material(KingdomsAtWar.id("block/raven_banner_iron")))
                .put(GOLD, new Material(KingdomsAtWar.id("block/raven_banner_gold")))
                .put(CLOTH, new Material(KingdomsAtWar.id(torn ? "block/raven_banner_torn" : "block/raven_banner_cloth")))
                .put(TextureSlot.PARTICLE, new Material(KingdomsAtWar.id("block/raven_banner_pole")));
        return b.build().createWithSuffix(banner, suffix, textures, blocks.modelOutput);
    }

    private static void banner(BlockModelGenerators blocks) {
        Block banner = CastleGear.RAVEN_BANNER.get();
        Identifier flying = bannerModel(blocks, banner, "", false);
        Identifier torn = bannerModel(blocks, banner, "_torn", true);
        blocks.blockStateOutput.accept(MultiVariantGenerator.dispatch(banner)
                .with(PropertyDispatch.initial(RavenBannerBlock.PHASE).generate(phase -> BlockModelGenerators.plainVariant(switch (phase) {
                    case GUARDED, AWAKE -> flying;
                    case FALLEN -> torn;
                })))
                .with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING));
    }
}
