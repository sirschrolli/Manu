package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Aschengeists.
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class AshWraithGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 128;

    private AshWraithGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.5F, -13F, -3F, 9F, 13F, 6F, new CubeDeformation(0F, 0F, -0.2F))
                .texOffs(0, 35).addBox(-6F, -14.4F, -3.5F, 12F, 4F, 7F, new CubeDeformation(-0.2F, -0.2F, -0.1F))
                .texOffs(0, 47).addBox(-3F, -11.5F, -3.35F, 6F, 6F, 1F, new CubeDeformation(-0.4F, 0F, -0.3F)),
                PartPose.offsetAndRotation(0F, 4F, 0F, 0.1745F, 0F, 0F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(31, 0).addBox(-4.5F, -9F, -4.9F, 9F, 9F, 9F, new CubeDeformation(-0.1F, 0F, -0.3F))
                .texOffs(68, 0).addBox(-4F, -8.9F, -2.8F, 8F, 9F, 7F, new CubeDeformation(-0.2F, -0.4F, -0.2F))
                .texOffs(57, 35).addBox(-3F, -7.2F, -4.2F, 6F, 6F, 4F, new CubeDeformation(-0.4F, -0.2F, -0.2F))
                .texOffs(19, 55).addBox(-2.4F, -6F, -4.55F, 2F, 2F, 1F, new CubeDeformation(-0.2F, -0.4F, -0.35F))
                .texOffs(26, 55).addBox(0.4F, -6F, -4.55F, 2F, 2F, 1F, new CubeDeformation(-0.2F, -0.4F, -0.35F))
                .texOffs(10, 55).addBox(-1.5F, -2.8F, -4.5F, 3F, 2F, 1F, new CubeDeformation(0F, -0.3F, -0.25F))
                .texOffs(30, 47).addBox(-1.5F, -10.4F, 0.8F, 3F, 2F, 4F, new CubeDeformation(-0.3F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, -14F, -0.5F, -0.1396F, 0F, 0F));
        PartDefinition p_hood_tip = p_head.addOrReplaceChild("hood_tip", CubeListBuilder.create()
                .texOffs(15, 47).addBox(-1F, -1F, -0.25F, 2F, 2F, 5F, new CubeDeformation(-0.1F, -0.2F, -0.25F)),
                PartPose.offsetAndRotation(0F, -9.5F, 4F, 0.6632F, 0F, 0F));
        PartDefinition p_left_arm = p_body.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(64, 20).addBox(-2.5F, -1.85F, -2.5F, 5F, 8F, 5F, new CubeDeformation(-0.3F, -0.25F, -0.3F))
                .texOffs(39, 35).addBox(-1F, 0F, -1F, 2F, 9F, 2F, new CubeDeformation(-0.1F, 0F, -0.1F)),
                PartPose.offsetAndRotation(5.4F, -12F, 0F, -0.3142F, 0F, -0.1745F));
        PartDefinition p_left_forearm = p_left_arm.addOrReplaceChild("left_forearm", CubeListBuilder.create()
                .texOffs(99, 35).addBox(-1F, -0.25F, -1F, 2F, 8F, 2F, new CubeDeformation(-0.2F, -0.25F, -0.2F))
                .texOffs(75, 47).addBox(-1.5F, 6.6F, -1.5F, 3F, 2F, 3F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(45, 47).addBox(-1.4F, 7.8F, -1.15F, 1F, 5F, 1F, new CubeDeformation(-0.25F, -0.4F, -0.25F))
                .texOffs(50, 47).addBox(-0.5F, 7.8F, -1.15F, 1F, 5F, 1F, new CubeDeformation(-0.25F, -0.4F, -0.25F))
                .texOffs(55, 47).addBox(0.4F, 7.8F, -1.15F, 1F, 5F, 1F, new CubeDeformation(-0.25F, -0.4F, -0.25F))
                .texOffs(0, 55).addBox(0.4F, 7.4F, 0.15F, 1F, 3F, 1F, new CubeDeformation(-0.25F, -0.2F, -0.25F)),
                PartPose.offsetAndRotation(0F, 8.6F, 0F, -0.4887F, 0F, 0F));
        PartDefinition p_right_arm = p_body.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(85, 20).addBox(-2.5F, -1.85F, -2.5F, 5F, 8F, 5F, new CubeDeformation(-0.3F, -0.25F, -0.3F))
                .texOffs(48, 35).addBox(-1F, 0F, -1F, 2F, 9F, 2F, new CubeDeformation(-0.1F, 0F, -0.1F)),
                PartPose.offsetAndRotation(-5.4F, -12F, 0F, -0.3142F, 0F, 0.1745F));
        PartDefinition p_right_forearm = p_right_arm.addOrReplaceChild("right_forearm", CubeListBuilder.create()
                .texOffs(108, 35).addBox(-1F, -0.25F, -1F, 2F, 8F, 2F, new CubeDeformation(-0.2F, -0.25F, -0.2F))
                .texOffs(88, 47).addBox(-1.5F, 6.6F, -1.5F, 3F, 2F, 3F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(60, 47).addBox(-1.4F, 7.8F, -1.15F, 1F, 5F, 1F, new CubeDeformation(-0.25F, -0.4F, -0.25F))
                .texOffs(65, 47).addBox(-0.5F, 7.8F, -1.15F, 1F, 5F, 1F, new CubeDeformation(-0.25F, -0.4F, -0.25F))
                .texOffs(70, 47).addBox(0.4F, 7.8F, -1.15F, 1F, 5F, 1F, new CubeDeformation(-0.25F, -0.4F, -0.25F))
                .texOffs(5, 55).addBox(-1.4F, 7.4F, 0.15F, 1F, 3F, 1F, new CubeDeformation(-0.25F, -0.2F, -0.25F)),
                PartPose.offsetAndRotation(0F, 8.6F, 0F, -0.4887F, 0F, 0F));
        PartDefinition p_skirt = p_body.addOrReplaceChild("skirt", CubeListBuilder.create()
                .texOffs(0, 20).addBox(-5F, -0.25F, -3.5F, 10F, 7F, 7F, new CubeDeformation(0F, -0.25F, -0.3F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_skirt_low = p_skirt.addOrReplaceChild("skirt_low", CubeListBuilder.create()
                .texOffs(35, 20).addBox(-4F, -0.25F, -3F, 8F, 7F, 6F, new CubeDeformation(0F, -0.25F, -0.4F)),
                PartPose.offsetAndRotation(0F, 6F, 0.4F, 0.1396F, 0F, 0F));
        PartDefinition p_skirt_tip = p_skirt_low.addOrReplaceChild("skirt_tip", CubeListBuilder.create()
                .texOffs(78, 35).addBox(-3F, 0F, -2F, 6F, 6F, 4F, new CubeDeformation(-0.4F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, 6F, 0.5F, 0.2094F, 0F, 0F));
        PartDefinition p_orbit = root.addOrReplaceChild("orbit", CubeListBuilder.create()
                .texOffs(101, 47).addBox(8F, -1F, -1F, 2F, 2F, 2F)
                .texOffs(110, 47).addBox(-9.7F, 2.8F, 1.8F, 2F, 2F, 2F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(119, 47).addBox(0.7F, -6.3F, 7.7F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.3F, -0.3F)),
                PartPose.offset(0F, -4F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
