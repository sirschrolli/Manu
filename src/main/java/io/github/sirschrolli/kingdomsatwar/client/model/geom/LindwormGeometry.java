package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Lindwurms.
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class LindwormGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 128;

    private LindwormGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.5F, -4F, -6F, 9F, 8F, 12F)
                .texOffs(19, 61).addBox(-0.75F, -6.5F, -4F, 1.5F, 2.5F, 2.5F)
                .texOffs(89, 61).addBox(-0.75F, -6F, 1F, 1.5F, 2F, 2.5F),
                PartPose.offset(0F, 16F, 0F));
        PartDefinition p_neck = p_body.addOrReplaceChild("neck", CubeListBuilder.create()
                .texOffs(64, 21).addBox(-3F, -3F, -7F, 6F, 6F, 8F)
                .texOffs(100, 61).addBox(-0.6F, -5F, -5F, 1.2F, 2F, 2.5F),
                PartPose.offsetAndRotation(0F, -2F, -6F, -0.5236F, 0F, 0F));
        PartDefinition p_head = p_neck.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(93, 21).addBox(-3.5F, -3.5F, -7F, 7F, 6F, 7F)
                .texOffs(79, 37).addBox(-2.5F, -2F, -12F, 5F, 3.5F, 5.5F)
                .texOffs(30, 61).addBox(-3.5F, -4.2F, -6F, 7F, 1F, 4F)
                .texOffs(0, 61).addBox(-2F, 1.5F, -11.5F, 4F, 1F, 4.5F)
                .texOffs(55, 68).addBox(3.2F, -2.2F, -5.5F, 0.5F, 1.2F, 1.8F)
                .texOffs(62, 68).addBox(-3.7F, -2.2F, -5.5F, 0.5F, 1.2F, 1.8F),
                PartPose.offsetAndRotation(0F, 0F, -6.5F, 0.5236F, 0F, 0F));
        PartDefinition p_jaw = p_head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(0, 51).addBox(-2.5F, 0F, -6F, 5F, 1.5F, 6.5F)
                .texOffs(72, 61).addBox(-2F, -0.8F, -5.5F, 4F, 1F, 4F),
                PartPose.offset(0F, 2.5F, -6F));
        PartDefinition p_left_horn = p_head.addOrReplaceChild("left_horn", CubeListBuilder.create()
                .texOffs(25, 51).addBox(-1F, -1F, 0F, 2F, 2F, 7F),
                PartPose.offsetAndRotation(2.4F, -3.6F, -1.5F, 0.4363F, 0.3142F, 0F));
        PartDefinition p_right_horn = p_head.addOrReplaceChild("right_horn", CubeListBuilder.create()
                .texOffs(44, 51).addBox(-1F, -1F, 0F, 2F, 2F, 7F),
                PartPose.offsetAndRotation(-2.4F, -3.6F, -1.5F, 0.4363F, -0.3142F, 0F));
        PartDefinition p_left_leg = p_body.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(63, 51).addBox(-1.5F, -1.5F, -1.5F, 3F, 6F, 3F)
                .texOffs(89, 51).addBox(-2F, 4.5F, -3.5F, 4F, 1.5F, 5F)
                .texOffs(69, 68).addBox(-1.75F, 5F, -4.5F, 0.5F, 1F, 1.5F)
                .texOffs(76, 68).addBox(-0.25F, 5F, -4.5F, 0.5F, 1F, 1.5F)
                .texOffs(83, 68).addBox(1.25F, 5F, -4.5F, 0.5F, 1F, 1.5F),
                PartPose.offset(4.8F, 2F, -3F));
        PartDefinition p_right_leg = p_body.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(76, 51).addBox(-1.5F, -1.5F, -1.5F, 3F, 6F, 3F)
                .texOffs(108, 51).addBox(-2F, 4.5F, -3.5F, 4F, 1.5F, 5F)
                .texOffs(90, 68).addBox(-1.75F, 5F, -4.5F, 0.5F, 1F, 1.5F)
                .texOffs(97, 68).addBox(-0.25F, 5F, -4.5F, 0.5F, 1F, 1.5F)
                .texOffs(104, 68).addBox(1.25F, 5F, -4.5F, 0.5F, 1F, 1.5F),
                PartPose.offset(-4.8F, 2F, -3F));
        PartDefinition p_segment1 = p_body.addOrReplaceChild("segment1", CubeListBuilder.create()
                .texOffs(43, 0).addBox(-4F, -3.5F, 0F, 8F, 7F, 10F)
                .texOffs(111, 61).addBox(-0.6F, -5.5F, 4F, 1.2F, 2F, 2.5F),
                PartPose.offsetAndRotation(0F, 0.5F, 5.5F, -0.2443F, 0F, 0F));
        PartDefinition p_segment2 = p_segment1.addOrReplaceChild("segment2", CubeListBuilder.create()
                .texOffs(80, 0).addBox(-3.75F, -3.25F, 0F, 7.5F, 6.5F, 10F)
                .texOffs(0, 68).addBox(-0.6F, -5.25F, 4F, 1.2F, 2F, 2.5F),
                PartPose.offsetAndRotation(0F, 0F, 9.2F, -0.1396F, 0F, 0F));
        PartDefinition p_segment3 = p_segment2.addOrReplaceChild("segment3", CubeListBuilder.create()
                .texOffs(0, 21).addBox(-3.5F, -3F, 0F, 7F, 6F, 9F)
                .texOffs(11, 68).addBox(-0.6F, -5F, 3.6F, 1.2F, 2F, 2.5F),
                PartPose.offsetAndRotation(0F, 0F, 9.2F, 0.1047F, 0F, 0F));
        PartDefinition p_segment4 = p_segment3.addOrReplaceChild("segment4", CubeListBuilder.create()
                .texOffs(33, 21).addBox(-3F, -2.75F, 0F, 6F, 5.5F, 9F)
                .texOffs(22, 68).addBox(-0.6F, -4.75F, 3.6F, 1.2F, 2F, 2.5F),
                PartPose.offsetAndRotation(0F, 0F, 8.2F, 0.1047F, 0F, 0F));
        PartDefinition p_segment5 = p_segment4.addOrReplaceChild("segment5", CubeListBuilder.create()
                .texOffs(0, 37).addBox(-2.75F, -2.5F, 0F, 5.5F, 5F, 8F)
                .texOffs(33, 68).addBox(-0.6F, -4.5F, 3.2F, 1.2F, 2F, 2.5F),
                PartPose.offsetAndRotation(0F, 0F, 8.2F, 0.0873F, 0F, 0F));
        PartDefinition p_segment6 = p_segment5.addOrReplaceChild("segment6", CubeListBuilder.create()
                .texOffs(29, 37).addBox(-2.25F, -2F, 0F, 4.5F, 4F, 8F)
                .texOffs(44, 68).addBox(-0.6F, -4F, 3.2F, 1.2F, 2F, 2.5F),
                PartPose.offsetAndRotation(0F, 0F, 7.2F, 0.0698F, 0F, 0F));
        PartDefinition p_segment7 = p_segment6.addOrReplaceChild("segment7", CubeListBuilder.create()
                .texOffs(56, 37).addBox(-1.75F, -1.75F, 0F, 3.5F, 3.5F, 7F),
                PartPose.offsetAndRotation(0F, 0F, 7.2F, 0.0524F, 0F, 0F));
        PartDefinition p_segment8 = p_segment7.addOrReplaceChild("segment8", CubeListBuilder.create()
                .texOffs(102, 37).addBox(-1.25F, -1.25F, 0F, 2.5F, 2.5F, 7F)
                .texOffs(53, 61).addBox(-2.5F, -0.25F, 6F, 5F, 0.5F, 4F),
                PartPose.offsetAndRotation(0F, 0F, 6.2F, 0.0349F, 0F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
