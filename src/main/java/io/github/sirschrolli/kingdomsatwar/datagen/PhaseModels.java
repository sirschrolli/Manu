package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.end.VoidThroneBlock;
import io.github.sirschrolli.kingdomsatwar.registry.PhaseGear;
import io.github.sirschrolli.kingdomsatwar.registry.PhaseSanctuary;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.world.level.block.Blocks;

/**
 * Modelle zum Phasit (seit 1.4): das Erz (Kristallbüschel über Endstein, die hellsten Pixel leuchten), der Block (das Licht
 * springt in der Leuchtebene von Stein zu Stein), der Kristall (im Inventar das Bild, in der Hand plastisch) und die Rüstung
 * der Leerwandler.
 */
final class PhaseModels {
    private PhaseModels() {}

    private static final TextureSlot OBSIDIAN = TextureSlot.create("obsidian");
    private static final TextureSlot PURPUR = TextureSlot.create("purpur");
    private static final TextureSlot PILLAR = TextureSlot.create("pillar");
    private static final TextureSlot CUSHION = TextureSlot.create("cushion");
    private static final TextureSlot GEM = TextureSlot.create("gem");

    static void register(BlockModelGenerators blocks, ItemModelGenerators items) {
        ModModelProvider.ore(blocks, PhaseGear.PHASIT_ORE, Blocks.END_STONE, "phasit");
        ModModelProvider.glowingCube(blocks, PhaseGear.PHASIT_BLOCK.get());
        ModModelProvider.flat(items, PhaseGear.PHASIT.get());
        ModModelProvider.armor(items, PhaseGear.VOIDWALKER_HELMET.get(), PhaseGear.VOIDWALKER_CHESTPLATE.get(),
                PhaseGear.VOIDWALKER_LEGGINGS.get(), PhaseGear.VOIDWALKER_BOOTS.get());
        // seit 1.5: der Leerenthron, die Phasenklinge (im Inventar das Bild, in der Hand das 3D-Modell) und Phasmors Spawn-Ei
        throne(blocks);
        MonsterGearModels.weapon(items, PhaseSanctuary.PHASE_BLADE.get(), "phase_blade", ArmoryShapes.PHASE_BLADE, 0.55f, 1.6f);
        ModModelProvider.flat(items, PhaseSanctuary.PHASMOR_SPAWN_EGG.get());
    }

    // =====================================================================================
    // Der Leerenthron
    // =====================================================================================

    /** Ein V-Ausschnitt der Textur passend zur Höhe (hohe Teile werden gestreckt, damit nichts über den Rand ragt). */
    private static float[] v(float y0, float y1) {
        float h = y1 - y0;
        if (h >= 16.0f) {
            return new float[] {0.0f, 16.0f};
        }
        float top = ((16.0f - y1) % 16.0f + 16.0f) % 16.0f;
        if (top + h > 16.0f) {
            top = 16.0f - h;
        }
        return new float[] {top, top + h};
    }

    /** Ein Quader; jede Seite zeigt den Ausschnitt der Textur, der ihrer Lage im Block entspricht. */
    private static void box(ExtendedModelTemplateBuilder b, float x0, float y0, float z0, float x1, float y1, float z1, TextureSlot side,
            TextureSlot top, int light) {
        float[] vv = v(y0, y1);
        b.element(e -> {
            e.from(x0, y0, z0).to(x1, y1, z1);
            e.allFaces((direction, face) -> {
                switch (direction.getAxis()) {
                    case Y -> face.uvs(x0, z0, x1, z1).texture(top);
                    case Z -> face.uvs(x0, vv[0], x1, vv[1]).texture(side);
                    default -> face.uvs(z0, vv[0], z1, vv[1]).texture(side);
                }
                if (direction == Direction.DOWN && y0 <= 0.0f) {
                    face.cullface(Direction.DOWN);
                }
            });
            if (light > 0) {
                e.lightEmission(light);
            }
        });
    }

    /**
     * Das Modell des Throns (Blick nach Norden; die Lehne steht hinten): ein Sockel aus Obsidian, der Sitz aus Purpur mit
     * einem violetten Polster, Armlehnen mit Phasitspitzen, die hohe Lehne aus Obsidian zwischen zwei Purpursäulen, darin ein
     * Phasit in einer Fassung aus Purpur, oben ein Kamm mit drei Phasitzacken. {@code lit}: Der Phasit leuchtet.
     */
    private static Identifier throneModel(BlockModelGenerators blocks, Block throne, String suffix, boolean lit) {
        ExtendedModelTemplateBuilder b = ExtendedModelTemplateBuilder.builder().parent(Identifier.withDefaultNamespace("block/block"))
                .requiredTextureSlot(OBSIDIAN).requiredTextureSlot(PURPUR).requiredTextureSlot(PILLAR).requiredTextureSlot(CUSHION)
                .requiredTextureSlot(GEM).requiredTextureSlot(TextureSlot.PARTICLE);
        int glow = lit ? 15 : 0;
        // Sockel und Sitz
        box(b, 0, 0, 0, 16, 2, 16, OBSIDIAN, OBSIDIAN, 0);
        box(b, 1, 2, 1, 15, 8, 15, PURPUR, PURPUR, 0);
        box(b, 2, 8, 1.5f, 14, 9, 11, CUSHION, CUSHION, 0);
        // die Armlehnen und ihre Phasitspitzen
        box(b, 0.5f, 8, 1, 3.5f, 12, 11, PILLAR, PURPUR, 0);
        box(b, 12.5f, 8, 1, 15.5f, 12, 11, PILLAR, PURPUR, 0);
        box(b, 1, 12, 1.5f, 3, 14.5f, 3.5f, GEM, GEM, glow);
        box(b, 13, 12, 1.5f, 15, 14.5f, 3.5f, GEM, GEM, glow);
        // die Lehne zwischen zwei Säulen
        box(b, 2, 8, 11, 14, 26, 15, OBSIDIAN, OBSIDIAN, 0);
        box(b, 0.5f, 8, 10.5f, 2.5f, 28, 15.5f, PILLAR, PURPUR, 0);
        box(b, 13.5f, 8, 10.5f, 15.5f, 28, 15.5f, PILLAR, PURPUR, 0);
        // der Phasit in der Lehne (Fassung aus Purpur)
        box(b, 5, 15.5f, 10.4f, 11, 22.5f, 11, PURPUR, PURPUR, 0);
        box(b, 6, 16.5f, 10.0f, 10, 21.5f, 10.5f, GEM, GEM, glow);
        // der Kamm mit drei Zacken
        box(b, 2.5f, 26, 11.5f, 13.5f, 28, 14.5f, PURPUR, PURPUR, 0);
        box(b, 4, 28, 12.25f, 6, 30.5f, 13.75f, GEM, GEM, glow);
        box(b, 10, 28, 12.25f, 12, 30.5f, 13.75f, GEM, GEM, glow);
        box(b, 7, 28, 12, 9, 32, 14, GEM, GEM, glow);
        TextureMapping textures = new TextureMapping()
                .put(OBSIDIAN, new Material(Identifier.withDefaultNamespace("block/obsidian")))
                .put(PURPUR, new Material(Identifier.withDefaultNamespace("block/purpur_block")))
                .put(PILLAR, new Material(Identifier.withDefaultNamespace("block/purpur_pillar")))
                .put(CUSHION, new Material(Identifier.withDefaultNamespace("block/purple_wool")))
                .put(GEM, lit ? new Material(KingdomsAtWar.id("block/phasit_block")) : new Material(Identifier.withDefaultNamespace("block/crying_obsidian")))
                .put(TextureSlot.PARTICLE, new Material(Identifier.withDefaultNamespace("block/obsidian")));
        return b.build().createWithSuffix(throne, suffix, textures, blocks.modelOutput);
    }

    private static void throne(BlockModelGenerators blocks) {
        Block throne = PhaseSanctuary.VOID_THRONE.get();
        Identifier guarded = throneModel(blocks, throne, "", true);
        Identifier defeated = throneModel(blocks, throne, "_defeated", false);
        blocks.blockStateOutput.accept(MultiVariantGenerator.dispatch(throne)
                .with(PropertyDispatch.initial(VoidThroneBlock.PHASE).generate(phase -> BlockModelGenerators.plainVariant(switch (phase) {
                    case GUARDED, AWAKE -> guarded;
                    case DEFEATED -> defeated;
                })))
                .with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING));
        blocks.registerSimpleItemModel(throne, guarded);
    }
}
