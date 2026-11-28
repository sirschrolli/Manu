package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Dornwichts.
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class ThornWightGeometry {
    public static final int TEXTURE_WIDTH = 64;
    public static final int TEXTURE_HEIGHT = 64;

    private ThornWightGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_wight = root.addOrReplaceChild("wight", CubeListBuilder.create(),
                PartPose.offset(0F, 24F, 0F));
        PartDefinition p_left_leg = p_wight.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-1F, 0F, -1F, 2F, 14F, 2F)
                .texOffs(50, 17).addBox(-1.5F, 13F, -2.5F, 3F, 2F, 4F, new CubeDeformation(0F, 0F, -0.3F)),
                PartPose.offset(1.6F, -15F, 0F));
        PartDefinition p_right_leg = p_wight.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(9, 0).addBox(-1F, 0F, -1F, 2F, 14F, 2F)
                .texOffs(0, 29).addBox(-1.5F, 13F, -2.5F, 3F, 2F, 4F, new CubeDeformation(0F, 0F, -0.3F)),
                PartPose.offset(-1.6F, -15F, 0F));
        PartDefinition p_body = p_wight.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(18, 0).addBox(-2.5F, -11F, -1.5F, 5F, 11F, 3F)
                .texOffs(27, 17).addBox(-3.5F, -12F, -2F, 7F, 3F, 4F, new CubeDeformation(-0.3F, 0F, -0.2F)),
                PartPose.offset(0F, -15F, 0F));
        PartDefinition p_spine0 = p_body.addOrReplaceChild("spine0", CubeListBuilder.create()
                .texOffs(15, 29).addBox(-0.5F, -3.6F, -0.5F, 1F, 4F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.1F)),
                PartPose.offsetAndRotation(-1.4F, -9F, 1.5F, -0.9599F, 0F, -0.6109F));
        PartDefinition p_spine1 = p_body.addOrReplaceChild("spine1", CubeListBuilder.create()
                .texOffs(20, 29).addBox(-0.5F, -3.6F, -0.5F, 1F, 4F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.1F)),
                PartPose.offsetAndRotation(1.2F, -6F, 1.5F, -0.9599F, 0F, 0.5236F));
        PartDefinition p_spine2 = p_body.addOrReplaceChild("spine2", CubeListBuilder.create()
                .texOffs(25, 29).addBox(-0.5F, -3.6F, -0.5F, 1F, 4F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.1F)),
                PartPose.offsetAndRotation(-0.6F, -3F, 1.5F, -0.9599F, 0F, -0.3491F));
        PartDefinition p_spine3 = p_body.addOrReplaceChild("spine3", CubeListBuilder.create()
                .texOffs(30, 29).addBox(-0.5F, -3.6F, -0.5F, 1F, 4F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.1F)),
                PartPose.offsetAndRotation(1.6F, -10F, 1.5F, -0.9599F, 0F, 0.6981F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(35, 0).addBox(-2.5F, -6F, -2.6F, 5F, 6F, 5F, new CubeDeformation(-0.1F, 0F, 0F))
                .texOffs(25, 36).addBox(-1.5F, -1.4F, -3.3F, 3F, 2F, 1F, new CubeDeformation(-0.3F, -0.2F, -0.2F)),
                PartPose.offset(0F, -12F, 0F));
        PartDefinition p_horn0 = p_head.addOrReplaceChild("horn0", CubeListBuilder.create()
                .texOffs(15, 36).addBox(-0.5F, -3F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.05F, 0F, -0.05F)),
                PartPose.offsetAndRotation(-2F, -5.6F, -0.5F, -0.1745F, 0F, -0.4363F));
        PartDefinition p_horn1 = p_head.addOrReplaceChild("horn1", CubeListBuilder.create()
                .texOffs(35, 29).addBox(-0.5F, -4F, -0.5F, 1F, 4F, 1F, new CubeDeformation(-0.05F, 0F, -0.05F)),
                PartPose.offsetAndRotation(-0.8F, -5.6F, -0.5F, -0.3142F, 0F, -0.1396F));
        PartDefinition p_horn2 = p_head.addOrReplaceChild("horn2", CubeListBuilder.create()
                .texOffs(40, 29).addBox(-0.5F, -4F, -0.5F, 1F, 4F, 1F, new CubeDeformation(-0.05F, 0F, -0.05F)),
                PartPose.offsetAndRotation(0.8F, -5.6F, -0.5F, -0.3142F, 0F, 0.1396F));
        PartDefinition p_horn3 = p_head.addOrReplaceChild("horn3", CubeListBuilder.create()
                .texOffs(20, 36).addBox(-0.5F, -3F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.05F, 0F, -0.05F)),
                PartPose.offsetAndRotation(2F, -5.6F, -0.5F, -0.1745F, 0F, 0.4363F));
        PartDefinition p_horn4 = p_head.addOrReplaceChild("horn4", CubeListBuilder.create()
                .texOffs(45, 29).addBox(-0.5F, -4F, -0.5F, 1F, 4F, 1F, new CubeDeformation(-0.05F, 0F, -0.05F)),
                PartPose.offsetAndRotation(0F, -5.6F, 1.5F, 0.2618F, 0F, 0F));
        PartDefinition p_left_arm = p_body.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(56, 0).addBox(-1F, 0F, -1F, 2F, 9F, 2F),
                PartPose.offset(3.9F, -11F, 0F));
        PartDefinition p_left_forearm = p_left_arm.addOrReplaceChild("left_forearm", CubeListBuilder.create()
                .texOffs(9, 17).addBox(-1F, 0F, -1F, 2F, 8F, 2F, new CubeDeformation(-0.1F, 0F, -0.1F)),
                PartPose.offset(0F, 9F, 0F));
        PartDefinition p_left_claw0 = p_left_forearm.addOrReplaceChild("left_claw0", CubeListBuilder.create()
                .texOffs(50, 29).addBox(-0.5F, -0.4F, -0.5F, 1F, 4F, 1F, new CubeDeformation(-0.2F, -0.4F, -0.2F)),
                PartPose.offsetAndRotation(-0.7F, 8F, -0.6F, -0.3142F, 0F, -0.2094F));
        PartDefinition p_left_claw1 = p_left_forearm.addOrReplaceChild("left_claw1", CubeListBuilder.create()
                .texOffs(55, 29).addBox(-0.5F, -0.4F, -0.5F, 1F, 4F, 1F, new CubeDeformation(-0.2F, -0.4F, -0.2F)),
                PartPose.offsetAndRotation(0F, 8F, -0.8F, -0.3142F, 0F, 0F));
        PartDefinition p_left_claw2 = p_left_forearm.addOrReplaceChild("left_claw2", CubeListBuilder.create()
                .texOffs(60, 29).addBox(-0.5F, -0.4F, -0.5F, 1F, 4F, 1F, new CubeDeformation(-0.2F, -0.4F, -0.2F)),
                PartPose.offsetAndRotation(0.7F, 8F, -0.6F, -0.3142F, 0F, 0.2094F));
        PartDefinition p_right_arm = p_body.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(0, 17).addBox(-1F, 0F, -1F, 2F, 9F, 2F),
                PartPose.offset(-3.9F, -11F, 0F));
        PartDefinition p_right_forearm = p_right_arm.addOrReplaceChild("right_forearm", CubeListBuilder.create()
                .texOffs(18, 17).addBox(-1F, 0F, -1F, 2F, 8F, 2F, new CubeDeformation(-0.1F, 0F, -0.1F)),
                PartPose.offset(0F, 9F, 0F));
        PartDefinition p_right_claw0 = p_right_forearm.addOrReplaceChild("right_claw0", CubeListBuilder.create()
                .texOffs(0, 36).addBox(-0.5F, -0.4F, -0.5F, 1F, 4F, 1F, new CubeDeformation(-0.2F, -0.4F, -0.2F)),
                PartPose.offsetAndRotation(-0.7F, 8F, -0.6F, -0.3142F, 0F, -0.2094F));
        PartDefinition p_right_claw1 = p_right_forearm.addOrReplaceChild("right_claw1", CubeListBuilder.create()
                .texOffs(5, 36).addBox(-0.5F, -0.4F, -0.5F, 1F, 4F, 1F, new CubeDeformation(-0.2F, -0.4F, -0.2F)),
                PartPose.offsetAndRotation(0F, 8F, -0.8F, -0.3142F, 0F, 0F));
        PartDefinition p_right_claw2 = p_right_forearm.addOrReplaceChild("right_claw2", CubeListBuilder.create()
                .texOffs(10, 36).addBox(-0.5F, -0.4F, -0.5F, 1F, 4F, 1F, new CubeDeformation(-0.2F, -0.4F, -0.2F)),
                PartPose.offsetAndRotation(0.7F, 8F, -0.6F, -0.3142F, 0F, 0.2094F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
