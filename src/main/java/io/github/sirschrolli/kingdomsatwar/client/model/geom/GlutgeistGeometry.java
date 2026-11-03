package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Glutgeists (Kopf und zwölf Stäbe wie bei der Vanilla-Lohe, dazu Krone, Bart und Kern).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class GlutgeistGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 64;

    private GlutgeistGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4F, -4F, -4F, 8F, 8F, 8F)
                .texOffs(33, 0).addBox(-4.5F, -5F, -4.5F, 9F, 2F, 9F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(70, 0).addBox(-3.5F, -8.5F, -3.5F, 7F, 4F, 7F)
                .texOffs(99, 17).addBox(-2.5F, 3.75F, -4.25F, 5F, 3F, 3F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(0, 28).addBox(-4.875F, -1.5F, -1.5F, 1F, 3F, 3F, new CubeDeformation(-0.125F, 0F, 0F))
                .texOffs(9, 28).addBox(3.875F, -1.5F, -1.5F, 1F, 3F, 3F, new CubeDeformation(-0.125F, 0F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_core = root.addOrReplaceChild("core", CubeListBuilder.create()
                .texOffs(99, 0).addBox(-2F, 0F, -2F, 4F, 6F, 4F)
                .texOffs(116, 17).addBox(-1.5F, 6F, -1.5F, 3F, 3F, 3F)
                .texOffs(18, 28).addBox(-1F, 9F, -1F, 2F, 4F, 2F),
                PartPose.offset(0F, 5F, 0F));
        PartDefinition p_part0 = root.addOrReplaceChild("part0", CubeListBuilder.create()
                .texOffs(116, 0).addBox(0F, 0F, 0F, 2F, 8F, 2F)
                .texOffs(27, 28).addBox(0.5F, -2.75F, 0.5F, 1F, 3F, 1F, new CubeDeformation(0F, -0.25F, 0F)),
                PartPose.offset(9F, -1F, 0F));
        PartDefinition p_part1 = root.addOrReplaceChild("part1", CubeListBuilder.create()
                .texOffs(0, 17).addBox(0F, 0F, 0F, 2F, 8F, 2F)
                .texOffs(32, 28).addBox(0.5F, -2.75F, 0.5F, 1F, 3F, 1F, new CubeDeformation(0F, -0.25F, 0F)),
                PartPose.offset(0F, -1.1224F, 9F));
        PartDefinition p_part2 = root.addOrReplaceChild("part2", CubeListBuilder.create()
                .texOffs(9, 17).addBox(0F, 0F, 0F, 2F, 8F, 2F)
                .texOffs(37, 28).addBox(0.5F, -2.75F, 0.5F, 1F, 3F, 1F, new CubeDeformation(0F, -0.25F, 0F)),
                PartPose.offset(-9F, -1.4597F, 0F));
        PartDefinition p_part3 = root.addOrReplaceChild("part3", CubeListBuilder.create()
                .texOffs(18, 17).addBox(0F, 0F, 0F, 2F, 8F, 2F)
                .texOffs(42, 28).addBox(0.5F, -2.75F, 0.5F, 1F, 3F, 1F, new CubeDeformation(0F, -0.25F, 0F)),
                PartPose.offset(0F, -1.9293F, -9F));
        PartDefinition p_part4 = root.addOrReplaceChild("part4", CubeListBuilder.create()
                .texOffs(27, 17).addBox(0F, 0F, 0F, 2F, 8F, 2F)
                .texOffs(47, 28).addBox(0.5F, -2.75F, 0.5F, 1F, 3F, 1F, new CubeDeformation(0F, -0.25F, 0F)),
                PartPose.offset(4.9497F, 1.5839F, 4.9497F));
        PartDefinition p_part5 = root.addOrReplaceChild("part5", CubeListBuilder.create()
                .texOffs(36, 17).addBox(0F, 0F, 0F, 2F, 8F, 2F)
                .texOffs(52, 28).addBox(0.5F, -2.75F, 0.5F, 1F, 3F, 1F, new CubeDeformation(0F, -0.25F, 0F)),
                PartPose.offset(-4.9497F, 1.1989F, 4.9497F));
        PartDefinition p_part6 = root.addOrReplaceChild("part6", CubeListBuilder.create()
                .texOffs(45, 17).addBox(0F, 0F, 0F, 2F, 8F, 2F)
                .texOffs(57, 28).addBox(0.5F, -2.75F, 0.5F, 1F, 3F, 1F, new CubeDeformation(0F, -0.25F, 0F)),
                PartPose.offset(-4.9497F, 1.01F, -4.9497F));
        PartDefinition p_part7 = root.addOrReplaceChild("part7", CubeListBuilder.create()
                .texOffs(54, 17).addBox(0F, 0F, 0F, 2F, 8F, 2F)
                .texOffs(62, 28).addBox(0.5F, -2.75F, 0.5F, 1F, 3F, 1F, new CubeDeformation(0F, -0.25F, 0F)),
                PartPose.offset(4.9497F, 1.0635F, -4.9497F));
        PartDefinition p_part8 = root.addOrReplaceChild("part8", CubeListBuilder.create()
                .texOffs(63, 17).addBox(0F, 0F, 0F, 2F, 8F, 2F)
                .texOffs(67, 28).addBox(0.5F, -2.75F, 0.5F, 1F, 3F, 1F, new CubeDeformation(0F, -0.25F, 0F)),
                PartPose.offset(4.455F, 11.9602F, 2.27F));
        PartDefinition p_part9 = root.addOrReplaceChild("part9", CubeListBuilder.create()
                .texOffs(72, 17).addBox(0F, 0F, 0F, 2F, 8F, 2F)
                .texOffs(72, 28).addBox(0.5F, -2.75F, 0.5F, 1F, 3F, 1F, new CubeDeformation(0F, -0.25F, 0F)),
                PartPose.offset(-2.27F, 11.893F, 4.455F));
        PartDefinition p_part10 = root.addOrReplaceChild("part10", CubeListBuilder.create()
                .texOffs(81, 17).addBox(0F, 0F, 0F, 2F, 8F, 2F)
                .texOffs(77, 28).addBox(0.5F, -2.75F, 0.5F, 1F, 3F, 1F, new CubeDeformation(0F, -0.25F, 0F)),
                PartPose.offset(-4.455F, 11.3466F, -2.27F));
        PartDefinition p_part11 = root.addOrReplaceChild("part11", CubeListBuilder.create()
                .texOffs(90, 17).addBox(0F, 0F, 0F, 2F, 8F, 2F)
                .texOffs(82, 28).addBox(0.5F, -2.75F, 0.5F, 1F, 3F, 1F, new CubeDeformation(0F, -0.25F, 0F)),
                PartPose.offset(2.27F, 10.6143F, -4.455F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
