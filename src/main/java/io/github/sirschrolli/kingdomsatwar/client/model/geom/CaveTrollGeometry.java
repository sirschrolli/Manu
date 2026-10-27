package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Höhlentrolls.
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class CaveTrollGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 128;

    private CaveTrollGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-8F, -18F, -5F, 16F, 18F, 10F)
                .texOffs(27, 67).addBox(-6.5F, -9F, -7F, 13F, 8F, 3F)
                .texOffs(0, 51).addBox(-8.5F, -3F, -5.5F, 17F, 4F, 11F)
                .texOffs(57, 80).addBox(-4F, 1F, -6F, 8F, 5F, 1F),
                PartPose.offsetAndRotation(0F, 10F, 0F, 0.2443F, 0F, 0F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(42, 29).addBox(-4.5F, -8F, -6.5F, 9F, 9F, 9F)
                .texOffs(21, 80).addBox(-1.5F, -4.5F, -9F, 3F, 4F, 3F)
                .texOffs(76, 80).addBox(-4.5F, -8.5F, -7F, 9F, 2F, 2F)
                .texOffs(34, 80).addBox(-3.5F, 0F, -7F, 7F, 2F, 4F)
                .texOffs(121, 80).addBox(-3F, -1.5F, -7.3F, 1F, 2F, 1F)
                .texOffs(0, 88).addBox(2F, -1.5F, -7.3F, 1F, 2F, 1F)
                .texOffs(0, 80).addBox(-2.5F, -9.5F, -3F, 5F, 2F, 5F)
                .texOffs(5, 88).addBox(-3.6F, -5.5F, -6.6F, 2F, 1F, 0.5F)
                .texOffs(12, 88).addBox(1.6F, -5.5F, -6.6F, 2F, 1F, 0.5F),
                PartPose.offsetAndRotation(0F, -17F, -3F, -0.1745F, 0F, 0F));
        PartDefinition p_left_ear = p_head.addOrReplaceChild("left_ear", CubeListBuilder.create()
                .texOffs(99, 80).addBox(0F, -1.5F, -0.5F, 4F, 3F, 1F),
                PartPose.offsetAndRotation(4.5F, -5F, -2F, 0F, -0.5236F, 0F));
        PartDefinition p_right_ear = p_head.addOrReplaceChild("right_ear", CubeListBuilder.create()
                .texOffs(110, 80).addBox(-4F, -1.5F, -0.5F, 4F, 3F, 1F),
                PartPose.offsetAndRotation(-4.5F, -5F, -2F, 0F, 0.5236F, 0F));
        PartDefinition p_left_arm = p_body.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(53, 0).addBox(-3F, -2F, -3F, 5F, 18F, 6F)
                .texOffs(78, 51).addBox(-3.5F, 16F, -3.5F, 6F, 5F, 7F),
                PartPose.offsetAndRotation(10F, -15F, -1F, -0.2443F, 0F, 0F));
        PartDefinition p_right_arm = p_body.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(76, 0).addBox(-2F, -2F, -3F, 5F, 18F, 6F)
                .texOffs(0, 67).addBox(-2.5F, 16F, -3.5F, 6F, 5F, 7F),
                PartPose.offsetAndRotation(-10F, -15F, -1F, -0.2443F, 0F, 0F));
        PartDefinition p_club = p_right_arm.addOrReplaceChild("club", CubeListBuilder.create()
                .texOffs(29, 29).addBox(-1.5F, -2F, -1.5F, 3F, 18F, 3F)
                .texOffs(57, 51).addBox(-2.5F, 13F, -2.5F, 5F, 8F, 5F)
                .texOffs(60, 67).addBox(-3F, 15F, -3F, 6F, 1F, 6F)
                .texOffs(85, 67).addBox(-3F, 18F, -3F, 6F, 1F, 6F),
                PartPose.offsetAndRotation(0.5F, 19F, 0F, 1.309F, 0F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(99, 0).addBox(-3.5F, 0F, -3.5F, 7F, 14F, 7F),
                PartPose.offset(4.5F, 10F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(0, 29).addBox(-3.5F, 0F, -3.5F, 7F, 14F, 7F),
                PartPose.offset(-4.5F, 10F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
