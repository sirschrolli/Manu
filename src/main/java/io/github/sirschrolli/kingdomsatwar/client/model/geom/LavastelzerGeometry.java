package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie der Lavastelzer (Knochen und Rumpf wie beim Vanilla-Schreiter, damit der Sattel passt).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class LavastelzerGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 128;

    private LavastelzerGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-8F, -6F, -8F, 16F, 14F, 16F)
                .texOffs(26, 91).addBox(-7.5F, -4.7F, -9.3F, 15F, 2F, 2F, new CubeDeformation(-0.1F, -0.3F, -0.4F))
                .texOffs(92, 82).addBox(-7.1F, -7.8F, -7.1F, 3F, 2F, 3F, new CubeDeformation(-0.3F, -0.2F, -0.3F))
                .texOffs(105, 82).addBox(4.1F, -7.8F, -7.1F, 3F, 2F, 3F, new CubeDeformation(-0.3F, -0.2F, -0.3F))
                .texOffs(0, 91).addBox(-7.1F, -7.8F, 4.1F, 3F, 2F, 3F, new CubeDeformation(-0.3F, -0.2F, -0.3F))
                .texOffs(13, 91).addBox(4.1F, -7.8F, 4.1F, 3F, 2F, 3F, new CubeDeformation(-0.3F, -0.2F, -0.3F)),
                PartPose.offset(0F, 1F, 0F));
        PartDefinition p_right_bottom_bristle = p_body.addOrReplaceChild("right_bottom_bristle", CubeListBuilder.create()
                .texOffs(0, 31).addBox(-12F, 0F, 0F, 12F, 0F, 16F),
                PartPose.offsetAndRotation(-8F, 4F, -8F, 0F, 0F, -1.2217F));
        PartDefinition p_right_middle_bristle = p_body.addOrReplaceChild("right_middle_bristle", CubeListBuilder.create()
                .texOffs(57, 31).addBox(-12F, 0F, 0F, 12F, 0F, 16F),
                PartPose.offsetAndRotation(-8F, -1F, -8F, 0F, 0F, -1.1345F));
        PartDefinition p_right_top_bristle = p_body.addOrReplaceChild("right_top_bristle", CubeListBuilder.create()
                .texOffs(0, 48).addBox(-12F, 0F, 0F, 12F, 0F, 16F),
                PartPose.offsetAndRotation(-8F, -5F, -8F, 0F, 0F, -0.8727F));
        PartDefinition p_left_top_bristle = p_body.addOrReplaceChild("left_top_bristle", CubeListBuilder.create()
                .texOffs(57, 48).addBox(0F, 0F, 0F, 12F, 0F, 16F),
                PartPose.offsetAndRotation(8F, -6F, -8F, 0F, 0F, 0.8727F));
        PartDefinition p_left_middle_bristle = p_body.addOrReplaceChild("left_middle_bristle", CubeListBuilder.create()
                .texOffs(0, 65).addBox(0F, 0F, 0F, 12F, 0F, 16F),
                PartPose.offsetAndRotation(8F, -2F, -8F, 0F, 0F, 1.1345F));
        PartDefinition p_left_bottom_bristle = p_body.addOrReplaceChild("left_bottom_bristle", CubeListBuilder.create()
                .texOffs(57, 65).addBox(0F, 0F, 0F, 12F, 0F, 16F),
                PartPose.offsetAndRotation(8F, 3F, -8F, 0F, 0F, 1.2217F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(65, 0).addBox(-2F, 0F, -2F, 4F, 16F, 4F)
                .texOffs(50, 82).addBox(-2.5F, 5.3F, -2.5F, 5F, 3F, 5F, new CubeDeformation(0F, -0.3F, 0F))
                .texOffs(0, 82).addBox(-3F, 14.3F, -3F, 6F, 2F, 6F, new CubeDeformation(-0.2F, -0.3F, -0.2F)),
                PartPose.offset(-4F, 8F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(82, 0).addBox(-2F, 0F, -2F, 4F, 16F, 4F)
                .texOffs(71, 82).addBox(-2.5F, 5.3F, -2.5F, 5F, 3F, 5F, new CubeDeformation(0F, -0.3F, 0F))
                .texOffs(25, 82).addBox(-3F, 14.3F, -3F, 6F, 2F, 6F, new CubeDeformation(-0.2F, -0.3F, -0.2F)),
                PartPose.offset(4F, 8F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
