package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Moorschlurfers.
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class MireShamblerGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 128;

    private MireShamblerGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-7F, -14F, -5F, 14F, 14F, 10F)
                .texOffs(0, 59).addBox(-6F, -17.5F, -3.5F, 12F, 4F, 9F)
                .texOffs(0, 25).addBox(-8F, -15F, -4.7F, 16F, 6F, 10F, new CubeDeformation(-0.4F, 0F, -0.2F))
                .texOffs(25, 42).addBox(-5F, -2F, -5.8F, 10F, 6F, 9F)
                .texOffs(30, 85).addBox(-3F, -19.75F, 0F, 6F, 3F, 4F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(25, 95).addBox(-6.5F, -18.1F, -2F, 4F, 1F, 4F, new CubeDeformation(0F, -0.3F, 0F))
                .texOffs(42, 95).addBox(2.75F, -16.3F, 2.25F, 4F, 1F, 4F, new CubeDeformation(-0.25F, -0.3F, -0.25F))
                .texOffs(84, 95).addBox(-8.5F, -12.8F, -2.6F, 2F, 1F, 3F, new CubeDeformation(-0.1F, -0.2F, -0.2F))
                .texOffs(105, 95).addBox(-8.4F, -10.65F, 0.2F, 2F, 1F, 2F, new CubeDeformation(-0.3F, -0.25F, 0F))
                .texOffs(114, 95).addBox(-8.25F, -8.9F, -1.85F, 2F, 1F, 2F, new CubeDeformation(-0.45F, -0.3F, -0.25F))
                .texOffs(0, 102).addBox(5.6F, -16.65F, 3.2F, 2F, 1F, 2F, new CubeDeformation(0F, -0.25F, 0F)),
                PartPose.offsetAndRotation(0F, 12F, 1F, 0.384F, 0F, 0F));
        PartDefinition p_branch = p_body.addOrReplaceChild("branch", CubeListBuilder.create()
                .texOffs(62, 73).addBox(-1F, -9F, -1F, 2F, 9F, 2F, new CubeDeformation(-0.4F, 0F, -0.4F))
                .texOffs(38, 102).addBox(-0.4F, -6.1F, -0.5F, 3F, 1F, 1F, new CubeDeformation(0F, -0.1F, -0.1F))
                .texOffs(47, 102).addBox(-2.8F, -8.15F, -0.55F, 3F, 1F, 1F, new CubeDeformation(-0.2F, -0.15F, -0.15F)),
                PartPose.offsetAndRotation(2.5F, -17F, 2F, -0.4363F, 0.3491F, 0.3142F));
        PartDefinition p_reeds = p_body.addOrReplaceChild("reeds", CubeListBuilder.create()
                .texOffs(71, 73).addBox(-0.5F, -10F, -0.5F, 1F, 10F, 1F, new CubeDeformation(-0.2F, 0F, -0.2F))
                .texOffs(97, 85).addBox(-1F, -9.5F, -1F, 2F, 4F, 2F, new CubeDeformation(-0.4F, -0.4F, -0.4F))
                .texOffs(23, 102).addBox(-0.5F, -11.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.35F, -0.2F, -0.35F))
                .texOffs(76, 73).addBox(1.2F, -8.6F, 0.6F, 1F, 9F, 1F, new CubeDeformation(-0.2F, -0.4F, -0.2F))
                .texOffs(106, 85).addBox(0.7F, -7.7F, 0.1F, 2F, 4F, 2F, new CubeDeformation(-0.4F, -0.4F, -0.4F))
                .texOffs(28, 102).addBox(1.2F, -9.8F, 0.6F, 1F, 2F, 1F, new CubeDeformation(-0.35F, -0.2F, -0.35F))
                .texOffs(25, 85).addBox(-2F, -7F, 0.4F, 1F, 7F, 1F, new CubeDeformation(-0.2F, 0F, -0.2F))
                .texOffs(115, 85).addBox(-2.5F, -6.5F, -0.1F, 2F, 4F, 2F, new CubeDeformation(-0.4F, -0.4F, -0.4F))
                .texOffs(33, 102).addBox(-2F, -8.6F, 0.4F, 1F, 2F, 1F, new CubeDeformation(-0.35F, -0.2F, -0.35F)),
                PartPose.offsetAndRotation(-3.6F, -16F, 2.4F, -0.2793F, -0.2094F, -0.2094F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(101, 59).addBox(-3.5F, -4F, -5.25F, 7F, 6F, 6F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(72, 85).addBox(-4F, -4.6F, -4F, 8F, 2F, 4F, new CubeDeformation(-0.1F, 0F, 0F))
                .texOffs(9, 102).addBox(-2.9F, -2.8F, -5.6F, 2F, 2F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.3F))
                .texOffs(16, 102).addBox(0.9F, -2.8F, -5.6F, 2F, 2F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.3F)),
                PartPose.offsetAndRotation(0F, -10F, -5F, -0.3142F, 0F, 0F));
        PartDefinition p_jaw = p_head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(51, 85).addBox(-3F, -0.2F, -3.2F, 6F, 3F, 4F, new CubeDeformation(0F, -0.2F, 0F))
                .texOffs(95, 95).addBox(-2.3F, 2.6F, -3.1F, 1F, 3F, 1F, new CubeDeformation(-0.1F, 0F, -0.1F))
                .texOffs(59, 95).addBox(0.3F, 2.6F, -2.7F, 1F, 4F, 1F, new CubeDeformation(-0.1F, 0F, -0.1F))
                .texOffs(100, 95).addBox(1.45F, 2.2F, -3.25F, 1F, 3F, 1F, new CubeDeformation(-0.15F, -0.4F, -0.15F)),
                PartPose.offsetAndRotation(0F, 1F, -2F, 0.2094F, 0F, 0F));
        PartDefinition p_left_arm = p_body.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(49, 0).addBox(-3F, -2F, -3F, 6F, 11F, 6F, new CubeDeformation(-0.2F, 0F, 0F))
                .texOffs(43, 59).addBox(-3.5F, -2.9F, -3.5F, 7F, 5F, 7F, new CubeDeformation(-0.3F, -0.3F, -0.2F)),
                PartPose.offsetAndRotation(7.5F, -12F, -1F, -0.4538F, 0F, -0.1047F));
        PartDefinition p_left_forearm = p_left_arm.addOrReplaceChild("left_forearm", CubeListBuilder.create()
                .texOffs(53, 25).addBox(-3F, 0F, -3F, 6F, 10F, 6F, new CubeDeformation(-0.4F, 0F, -0.4F))
                .texOffs(81, 73).addBox(-3F, 8.5F, -3F, 6F, 3F, 6F)
                .texOffs(64, 42).addBox(-3.5F, 0.75F, -3.5F, 7F, 8F, 7F, new CubeDeformation(-0.4F, -0.25F, -0.4F))
                .texOffs(124, 85).addBox(-2.3F, 10.6F, -2.3F, 1F, 5F, 1F, new CubeDeformation(-0.05F, -0.4F, -0.05F))
                .texOffs(0, 95).addBox(-0.5F, 11F, -2.7F, 1F, 5F, 1F, new CubeDeformation(-0.05F, 0F, -0.05F))
                .texOffs(5, 95).addBox(1.3F, 10.7F, -2.3F, 1F, 5F, 1F, new CubeDeformation(-0.05F, -0.3F, -0.05F))
                .texOffs(64, 95).addBox(-2.1F, 10.7F, 0.9F, 1F, 4F, 1F, new CubeDeformation(-0.05F, -0.3F, -0.05F))
                .texOffs(69, 95).addBox(1.1F, 10.8F, 0.9F, 1F, 4F, 1F, new CubeDeformation(-0.05F, -0.2F, -0.05F)),
                PartPose.offsetAndRotation(0F, 8.6F, 0F, -0.2094F, 0F, 0F));
        PartDefinition p_right_arm = p_body.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(74, 0).addBox(-3F, -2F, -3F, 6F, 11F, 6F, new CubeDeformation(-0.2F, 0F, 0F))
                .texOffs(72, 59).addBox(-3.5F, -2.9F, -3.5F, 7F, 5F, 7F, new CubeDeformation(-0.3F, -0.3F, -0.2F)),
                PartPose.offsetAndRotation(-7.5F, -12F, -1F, -0.4538F, 0F, 0.1047F));
        PartDefinition p_right_forearm = p_right_arm.addOrReplaceChild("right_forearm", CubeListBuilder.create()
                .texOffs(78, 25).addBox(-3F, 0F, -3F, 6F, 10F, 6F, new CubeDeformation(-0.4F, 0F, -0.4F))
                .texOffs(0, 85).addBox(-3F, 8.5F, -3F, 6F, 3F, 6F)
                .texOffs(93, 42).addBox(-3.5F, 0.75F, -3.5F, 7F, 8F, 7F, new CubeDeformation(-0.4F, -0.25F, -0.4F))
                .texOffs(10, 95).addBox(-2.3F, 10.6F, -2.3F, 1F, 5F, 1F, new CubeDeformation(-0.05F, -0.4F, -0.05F))
                .texOffs(15, 95).addBox(-0.5F, 11F, -2.7F, 1F, 5F, 1F, new CubeDeformation(-0.05F, 0F, -0.05F))
                .texOffs(20, 95).addBox(1.3F, 10.7F, -2.3F, 1F, 5F, 1F, new CubeDeformation(-0.05F, -0.3F, -0.05F))
                .texOffs(74, 95).addBox(-2.1F, 10.7F, 0.9F, 1F, 4F, 1F, new CubeDeformation(-0.05F, -0.3F, -0.05F))
                .texOffs(79, 95).addBox(1.1F, 10.8F, 0.9F, 1F, 4F, 1F, new CubeDeformation(-0.05F, -0.2F, -0.05F)),
                PartPose.offsetAndRotation(0F, 8.6F, 0F, -0.2094F, 0F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(103, 25).addBox(-3F, -0.25F, -3F, 6F, 10F, 6F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(0, 73).addBox(-3.5F, 8.5F, -4.2F, 7F, 3F, 8F, new CubeDeformation(-0.1F, 0F, -0.4F)),
                PartPose.offset(3.8F, 12.5F, 1F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(0, 42).addBox(-3F, -0.25F, -3F, 6F, 10F, 6F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(31, 73).addBox(-3.5F, 8.5F, -4.2F, 7F, 3F, 8F, new CubeDeformation(-0.1F, 0F, -0.4F)),
                PartPose.offset(-3.8F, 12.5F, 1F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
