package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Glutwurms (Schädel als Ursprung, sechs Glieder).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class EmberWyrmGeometry {
    public static final int TEXTURE_WIDTH = 192;
    public static final int TEXTURE_HEIGHT = 128;

    private EmberWyrmGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.5F, -4F, -9F, 9F, 6F, 9F)
                .texOffs(91, 16).addBox(-4F, -2.7F, -13.4F, 8F, 5F, 5F, new CubeDeformation(-0.4F, -0.3F, -0.4F))
                .texOffs(0, 30).addBox(-5F, -4.8F, -8F, 10F, 2F, 5F, new CubeDeformation(-0.1F, -0.2F, 0F))
                .texOffs(171, 30).addBox(-4.3F, -3.8F, -8.6F, 2F, 2F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.3F))
                .texOffs(178, 30).addBox(2.3F, -3.8F, -8.6F, 2F, 2F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.3F))
                .texOffs(21, 38).addBox(-3.1F, 1.3F, -12.7F, 1F, 2F, 1F, new CubeDeformation(-0.1F, -0.3F, -0.1F))
                .texOffs(26, 38).addBox(2.1F, 1.3F, -12.7F, 1F, 2F, 1F, new CubeDeformation(-0.1F, -0.3F, -0.1F))
                .texOffs(31, 38).addBox(-1.5F, -2.7F, -13.55F, 3F, 1F, 1F, new CubeDeformation(-0.3F, -0.1F, -0.25F)),
                PartPose.offset(0F, 16F, -4F));
        PartDefinition p_left_horn = p_head.addOrReplaceChild("left_horn", CubeListBuilder.create()
                .texOffs(137, 16).addBox(-1.5F, -1.5F, 0F, 3F, 3F, 5F, new CubeDeformation(-0.3F, -0.3F, 0F)),
                PartPose.offsetAndRotation(3.4F, -4F, -3F, 0.6981F, 0.3142F, 0.2094F));
        PartDefinition p_left_horn_tip = p_left_horn.addOrReplaceChild("left_horn_tip", CubeListBuilder.create()
                .texOffs(42, 30).addBox(-1F, -1F, 0F, 2F, 2F, 4F, new CubeDeformation(-0.2F, -0.2F, 0F))
                .texOffs(185, 30).addBox(-0.5F, -0.5F, 4F, 1F, 1F, 2F, new CubeDeformation(-0.1F, -0.1F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 5F, -0.5236F, 0F, 0F));
        PartDefinition p_right_horn = p_head.addOrReplaceChild("right_horn", CubeListBuilder.create()
                .texOffs(154, 16).addBox(-1.5F, -1.5F, 0F, 3F, 3F, 5F, new CubeDeformation(-0.3F, -0.3F, 0F)),
                PartPose.offsetAndRotation(-3.4F, -4F, -3F, 0.6981F, -0.3142F, -0.2094F));
        PartDefinition p_right_horn_tip = p_right_horn.addOrReplaceChild("right_horn_tip", CubeListBuilder.create()
                .texOffs(55, 30).addBox(-1F, -1F, 0F, 2F, 2F, 4F, new CubeDeformation(-0.2F, -0.2F, 0F))
                .texOffs(0, 38).addBox(-0.5F, -0.5F, 4F, 1F, 1F, 2F, new CubeDeformation(-0.1F, -0.1F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 5F, -0.5236F, 0F, 0F));
        PartDefinition p_jaw = p_head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(29, 16).addBox(-4F, -0.3F, -8.8F, 8F, 3F, 9F, new CubeDeformation(-0.2F, -0.3F, -0.2F))
                .texOffs(40, 38).addBox(-3.1F, -1F, -8.3F, 1F, 1F, 1F, new CubeDeformation(-0.1F, 0F, -0.1F))
                .texOffs(45, 38).addBox(2.1F, -1F, -8.3F, 1F, 1F, 1F, new CubeDeformation(-0.1F, 0F, -0.1F))
                .texOffs(50, 38).addBox(-0.5F, -0.95F, -8.5F, 1F, 1F, 1F, new CubeDeformation(-0.1F, -0.05F, -0.1F)),
                PartPose.offsetAndRotation(0F, 2F, -4F, 0.1047F, 0F, 0F));
        PartDefinition p_segment1 = p_head.addOrReplaceChild("segment1", CubeListBuilder.create()
                .texOffs(37, 0).addBox(-4.5F, -3.5F, 0F, 9F, 7F, 8F)
                .texOffs(31, 30).addBox(-1F, -7.1F, 1.3F, 2F, 4F, 3F, new CubeDeformation(-0.4F, -0.4F, -0.2F))
                .texOffs(136, 30).addBox(-0.5F, -5.46F, 4.8F, 1F, 2F, 2F, new CubeDeformation(-0.1F, -0.04F, -0.2F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_segment2 = p_segment1.addOrReplaceChild("segment2", CubeListBuilder.create()
                .texOffs(72, 0).addBox(-4.5F, -3.5F, 0F, 9F, 7F, 8F, new CubeDeformation(-0.3F, -0.2F, 0F))
                .texOffs(81, 30).addBox(-1F, -6.225F, 1.3F, 2F, 3F, 3F, new CubeDeformation(-0.4F, -0.075F, -0.2F))
                .texOffs(143, 30).addBox(-0.5F, -5.155F, 4.8F, 1F, 2F, 2F, new CubeDeformation(-0.1F, -0.145F, -0.2F)),
                PartPose.offset(0F, 0F, 8F));
        PartDefinition p_segment3 = p_segment2.addOrReplaceChild("segment3", CubeListBuilder.create()
                .texOffs(107, 0).addBox(-4F, -3F, 0F, 8F, 6F, 8F, new CubeDeformation(-0.2F, 0F, 0F))
                .texOffs(92, 30).addBox(-1F, -5.75F, 1.3F, 2F, 3F, 3F, new CubeDeformation(-0.4F, -0.25F, -0.2F))
                .texOffs(150, 30).addBox(-0.5F, -4.75F, 4.8F, 1F, 2F, 2F, new CubeDeformation(-0.1F, -0.25F, -0.2F)),
                PartPose.offset(0F, 0F, 8F));
        PartDefinition p_segment4 = p_segment3.addOrReplaceChild("segment4", CubeListBuilder.create()
                .texOffs(140, 0).addBox(-3.5F, -3F, 0F, 7F, 6F, 8F, new CubeDeformation(-0.2F, -0.3F, 0F))
                .texOffs(103, 30).addBox(-1F, -5.275F, 1.3F, 2F, 3F, 3F, new CubeDeformation(-0.4F, -0.425F, -0.2F))
                .texOffs(157, 30).addBox(-0.5F, -4.345F, 4.8F, 1F, 2F, 2F, new CubeDeformation(-0.1F, -0.355F, -0.2F)),
                PartPose.offset(0F, 0F, 8F));
        PartDefinition p_segment5 = p_segment4.addOrReplaceChild("segment5", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-3F, -2.5F, 0F, 6F, 5F, 8F, new CubeDeformation(-0.2F, -0.2F, 0F))
                .texOffs(114, 30).addBox(-1F, -4.2F, 1.3F, 2F, 2F, 3F, new CubeDeformation(-0.4F, -0.1F, -0.2F))
                .texOffs(164, 30).addBox(-0.5F, -3.84F, 4.8F, 1F, 2F, 2F, new CubeDeformation(-0.1F, -0.46F, -0.2F)),
                PartPose.offset(0F, 0F, 8F));
        PartDefinition p_segment6 = p_segment5.addOrReplaceChild("segment6", CubeListBuilder.create()
                .texOffs(64, 16).addBox(-2.5F, -2F, 0F, 5F, 4F, 8F, new CubeDeformation(-0.3F, -0.1F, 0F))
                .texOffs(125, 30).addBox(-1F, -3.625F, 1.3F, 2F, 2F, 3F, new CubeDeformation(-0.4F, -0.275F, -0.2F))
                .texOffs(7, 38).addBox(-0.5F, -2.835F, 4.8F, 1F, 1F, 2F, new CubeDeformation(-0.1F, -0.065F, -0.2F)),
                PartPose.offset(0F, 0F, 8F));
        PartDefinition p_tail = p_segment6.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(118, 16).addBox(-2F, -1.5F, 0F, 4F, 3F, 5F, new CubeDeformation(-0.4F, -0.1F, 0F))
                .texOffs(68, 30).addBox(-1F, -1F, 5F, 2F, 2F, 4F, new CubeDeformation(-0.2F, -0.2F, 0F))
                .texOffs(14, 38).addBox(-0.5F, -0.5F, 9F, 1F, 1F, 2F, new CubeDeformation(-0.2F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 8F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
