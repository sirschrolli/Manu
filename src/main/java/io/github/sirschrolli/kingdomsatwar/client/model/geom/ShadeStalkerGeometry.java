package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Schattenpirschers.
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class ShadeStalkerGeometry {
    public static final int TEXTURE_WIDTH = 64;
    public static final int TEXTURE_HEIGHT = 64;

    private ShadeStalkerGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-1F, 0F, -1F, 2F, 19F, 2F)
                .texOffs(34, 35).addBox(-1.5F, 18F, -3F, 3F, 2F, 4F)
                .texOffs(34, 51).addBox(-1.5F, 19.5F, -4F, 3F, 0.5F, 1F),
                PartPose.offset(2.3F, 4F, 0.5F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(9, 0).addBox(-1F, 0F, -1F, 2F, 19F, 2F)
                .texOffs(49, 35).addBox(-1.5F, 18F, -3F, 3F, 2F, 4F)
                .texOffs(43, 51).addBox(-1.5F, 19.5F, -4F, 3F, 0.5F, 1F),
                PartPose.offset(-2.3F, 4F, 0.5F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(37, 0).addBox(-3.5F, -14F, -2F, 7F, 14F, 4F)
                .texOffs(45, 44).addBox(-2.5F, -6F, -2.6F, 5F, 4F, 1F)
                .texOffs(13, 51).addBox(-0.5F, -12.5F, 1.6F, 1F, 2F, 1.2F)
                .texOffs(20, 51).addBox(-0.5F, -8.5F, 1.6F, 1F, 2F, 1.2F)
                .texOffs(27, 51).addBox(-0.5F, -4.5F, 1.6F, 1F, 2F, 1.2F),
                PartPose.offsetAndRotation(0F, 4F, 0.5F, 0.2793F, 0F, 0F));
        PartDefinition p_cloak = p_body.addOrReplaceChild("cloak", CubeListBuilder.create()
                .texOffs(18, 0).addBox(-4F, 0F, 0F, 8F, 18F, 1F),
                PartPose.offsetAndRotation(0F, -13.5F, 2.1F, -0.2094F, 0F, 0F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(36, 22).addBox(-2.5F, -6F, -3.5F, 5F, 6F, 5F)
                .texOffs(30, 44).addBox(-2F, -2F, -6F, 4F, 2F, 3F)
                .texOffs(0, 51).addBox(-1.5F, 0F, -5.5F, 3F, 1F, 3F)
                .texOffs(52, 51).addBox(-1.8F, -4.2F, -3.8F, 1.2F, 1F, 0.5F)
                .texOffs(0, 56).addBox(0.6F, -4.2F, -3.8F, 1.2F, 1F, 0.5F),
                PartPose.offsetAndRotation(0F, -14F, -0.5F, -0.2443F, 0F, 0F));
        PartDefinition p_wisp_l = p_head.addOrReplaceChild("wisp_l", CubeListBuilder.create()
                .texOffs(0, 35).addBox(-0.5F, 0F, 0F, 1F, 1F, 7F),
                PartPose.offsetAndRotation(1.5F, -5F, 1.5F, -0.6109F, 0.2443F, 0F));
        PartDefinition p_wisp_r = p_head.addOrReplaceChild("wisp_r", CubeListBuilder.create()
                .texOffs(17, 35).addBox(-0.5F, 0F, 0F, 1F, 1F, 7F),
                PartPose.offsetAndRotation(-1.5F, -5F, 1.5F, -0.6109F, -0.2443F, 0F));
        PartDefinition p_left_arm = p_body.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(0, 22).addBox(-1F, -1F, -1F, 2F, 10F, 2F),
                PartPose.offsetAndRotation(4.5F, -13F, 0F, -0.1745F, 0F, -0.1047F));
        PartDefinition p_left_forearm = p_left_arm.addOrReplaceChild("left_forearm", CubeListBuilder.create()
                .texOffs(9, 22).addBox(-1F, 0F, -1F, 2F, 10F, 2F),
                PartPose.offsetAndRotation(0F, 9F, 0F, -0.3142F, 0F, 0F));
        PartDefinition p_left_forearm_claw1 = p_left_forearm.addOrReplaceChild("left_forearm_claw1", CubeListBuilder.create()
                .texOffs(0, 44).addBox(-0.25F, 0F, -0.25F, 0.5F, 5F, 0.5F),
                PartPose.offsetAndRotation(-0.8F, 10F, -0.5F, -0.2094F, 0F, 0.2443F));
        PartDefinition p_left_forearm_claw2 = p_left_forearm.addOrReplaceChild("left_forearm_claw2", CubeListBuilder.create()
                .texOffs(5, 44).addBox(-0.25F, 0F, -0.25F, 0.5F, 5F, 0.5F),
                PartPose.offsetAndRotation(0F, 10F, -0.5F, -0.2094F, 0F, 0F));
        PartDefinition p_left_forearm_claw3 = p_left_forearm.addOrReplaceChild("left_forearm_claw3", CubeListBuilder.create()
                .texOffs(10, 44).addBox(-0.25F, 0F, -0.25F, 0.5F, 5F, 0.5F),
                PartPose.offsetAndRotation(0.8F, 10F, -0.5F, -0.2094F, 0F, -0.2443F));
        PartDefinition p_right_arm = p_body.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(18, 22).addBox(-1F, -1F, -1F, 2F, 10F, 2F),
                PartPose.offsetAndRotation(-4.5F, -13F, 0F, -0.1745F, 0F, 0.1047F));
        PartDefinition p_right_forearm = p_right_arm.addOrReplaceChild("right_forearm", CubeListBuilder.create()
                .texOffs(27, 22).addBox(-1F, 0F, -1F, 2F, 10F, 2F),
                PartPose.offsetAndRotation(0F, 9F, 0F, -0.3142F, 0F, 0F));
        PartDefinition p_right_forearm_claw1 = p_right_forearm.addOrReplaceChild("right_forearm_claw1", CubeListBuilder.create()
                .texOffs(15, 44).addBox(-0.25F, 0F, -0.25F, 0.5F, 5F, 0.5F),
                PartPose.offsetAndRotation(-0.8F, 10F, -0.5F, -0.2094F, 0F, 0.2443F));
        PartDefinition p_right_forearm_claw2 = p_right_forearm.addOrReplaceChild("right_forearm_claw2", CubeListBuilder.create()
                .texOffs(20, 44).addBox(-0.25F, 0F, -0.25F, 0.5F, 5F, 0.5F),
                PartPose.offsetAndRotation(0F, 10F, -0.5F, -0.2094F, 0F, 0F));
        PartDefinition p_right_forearm_claw3 = p_right_forearm.addOrReplaceChild("right_forearm_claw3", CubeListBuilder.create()
                .texOffs(25, 44).addBox(-0.25F, 0F, -0.25F, 0.5F, 5F, 0.5F),
                PartPose.offsetAndRotation(0.8F, 10F, -0.5F, -0.2094F, 0F, -0.2443F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
