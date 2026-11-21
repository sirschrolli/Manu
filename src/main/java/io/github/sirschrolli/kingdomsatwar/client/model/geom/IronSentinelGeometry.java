package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Eisenwächters (Knochen wie beim Vanilla-Eisengolem; Polster und Eiszapfen nur je Gestalt bemalt).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class IronSentinelGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 128;

    private IronSentinelGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(59, 0).addBox(-4F, -12F, -5.5F, 8F, 10F, 8F)
                .texOffs(50, 95).addBox(-1F, -13.9F, -5.4F, 2F, 2F, 8F, new CubeDeformation(-0.4F, -0.1F, -0.2F))
                .texOffs(0, 115).addBox(-4.5F, -11.6F, -6.1F, 9F, 2F, 1F, new CubeDeformation(-0.1F, -0.4F, 0F))
                .texOffs(17, 107).addBox(-4.5F, -5.6F, -6.6F, 9F, 4F, 2F, new CubeDeformation(0F, -0.4F, -0.2F))
                .texOffs(71, 95).addBox(-4.5F, -12.6F, -5.4F, 9F, 1F, 8F, new CubeDeformation(-0.2F, -0.1F, -0.4F))
                .texOffs(21, 115).addBox(-4.4F, -10.3F, -6.3F, 1F, 2F, 1F, new CubeDeformation(-0.1F, -0.3F, -0.1F))
                .texOffs(97, 107).addBox(-0.5F, -10.4F, -6.3F, 1F, 3F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.1F))
                .texOffs(26, 115).addBox(3.4F, -10.3F, -6.3F, 1F, 2F, 1F, new CubeDeformation(-0.1F, -0.3F, -0.1F)),
                PartPose.offset(0F, -7F, -2F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-9F, -2F, -6F, 18F, 12F, 11F)
                .texOffs(0, 107).addBox(-3.5F, 0.25F, -6.85F, 7F, 6F, 1F, new CubeDeformation(0F, -0.25F, -0.15F))
                .texOffs(50, 57).addBox(-5.5F, -3.5F, -5.25F, 11F, 2F, 10F, new CubeDeformation(0F, -0.3F, -0.25F))
                .texOffs(106, 95).addBox(-2F, -6.4F, 4F, 4F, 5F, 3F, new CubeDeformation(-0.4F, 0F, -0.2F))
                .texOffs(40, 107).addBox(-2.5F, -7.8F, 3.5F, 5F, 2F, 4F, new CubeDeformation(-0.4F, -0.4F, -0.2F))
                .texOffs(74, 71).addBox(-4.5F, 10F, -3F, 9F, 5F, 6F, new CubeDeformation(0.5F)),
                PartPose.offset(0F, -7F, 0F));
        PartDefinition p_right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(25, 24).addBox(-15F, -4.6F, -4.5F, 7F, 6F, 9F, new CubeDeformation(-0.2F, -0.4F, -0.3F))
                .texOffs(62, 83).addBox(-14.5F, 0.4F, -4F, 6F, 3F, 8F, new CubeDeformation(-0.1F, -0.2F, -0.2F))
                .texOffs(83, 41).addBox(-13.2F, 2.3F, -2.5F, 4F, 9F, 5F, new CubeDeformation(-0.4F, -0.3F, -0.1F))
                .texOffs(0, 95).addBox(-13.7F, 10.1F, -3.5F, 5F, 4F, 7F, new CubeDeformation(-0.3F, -0.3F, -0.3F))
                .texOffs(92, 0).addBox(-13.7F, 13.1F, -3.5F, 5F, 9F, 7F, new CubeDeformation(-0.1F, -0.3F, -0.1F))
                .texOffs(91, 24).addBox(-14.2F, 21.1F, -4F, 6F, 7F, 8F, new CubeDeformation(-0.3F, -0.3F, -0.3F))
                .texOffs(0, 71).addBox(-15.7F, -4.7F, -5F, 8F, 1F, 10F, new CubeDeformation(-0.3F, -0.1F, -0.4F))
                .texOffs(31, 115).addBox(-14.3F, 3.1F, -3.5F, 1F, 2F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.1F))
                .texOffs(102, 107).addBox(-14.3F, 3F, -0.7F, 1F, 3F, 1F, new CubeDeformation(-0.1F, -0.2F, -0.1F))
                .texOffs(36, 115).addBox(-14.3F, 3F, 2.3F, 1F, 2F, 1F, new CubeDeformation(-0.1F, -0.2F, -0.1F)),
                PartPose.offset(0F, -7F, 0F));
        PartDefinition p_left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(58, 24).addBox(8F, -4.6F, -4.5F, 7F, 6F, 9F, new CubeDeformation(-0.2F, -0.4F, -0.3F))
                .texOffs(91, 83).addBox(8.5F, 0.4F, -4F, 6F, 3F, 8F, new CubeDeformation(-0.1F, -0.2F, -0.2F))
                .texOffs(102, 41).addBox(9.2F, 2.3F, -2.5F, 4F, 9F, 5F, new CubeDeformation(-0.4F, -0.3F, -0.1F))
                .texOffs(25, 95).addBox(8.7F, 10.1F, -3.5F, 5F, 4F, 7F, new CubeDeformation(-0.3F, -0.3F, -0.3F))
                .texOffs(0, 24).addBox(8.7F, 13.1F, -3.5F, 5F, 9F, 7F, new CubeDeformation(-0.1F, -0.3F, -0.1F))
                .texOffs(0, 41).addBox(8.2F, 21.1F, -4F, 6F, 7F, 8F, new CubeDeformation(-0.3F, -0.3F, -0.3F))
                .texOffs(37, 71).addBox(7.7F, -4.7F, -5F, 8F, 1F, 10F, new CubeDeformation(-0.3F, -0.1F, -0.4F))
                .texOffs(41, 115).addBox(13.3F, 3.1F, -3.5F, 1F, 2F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.1F))
                .texOffs(107, 107).addBox(13.3F, 3F, -0.7F, 1F, 3F, 1F, new CubeDeformation(-0.1F, -0.2F, -0.1F))
                .texOffs(46, 115).addBox(13.3F, 3F, 2.3F, 1F, 2F, 1F, new CubeDeformation(-0.1F, -0.2F, -0.1F)),
                PartPose.offset(0F, -7F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(29, 41).addBox(-4F, -3.6F, -3.5F, 7F, 8F, 6F, new CubeDeformation(-0.3F, -0.4F, -0.1F))
                .texOffs(0, 57).addBox(-3.5F, 3.8F, -3.4F, 6F, 7F, 6F, new CubeDeformation(-0.1F, -0.2F, -0.4F))
                .texOffs(59, 107).addBox(-4F, 3F, -4.2F, 7F, 3F, 2F, new CubeDeformation(-0.1F, 0F, -0.3F))
                .texOffs(0, 83).addBox(-4F, 10.2F, -5.2F, 7F, 3F, 8F, new CubeDeformation(0F, -0.2F, -0.3F)),
                PartPose.offset(-4F, 11F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(56, 41).addBox(-4F, -3.6F, -3.5F, 7F, 8F, 6F, new CubeDeformation(-0.3F, -0.4F, -0.1F))
                .texOffs(25, 57).addBox(-3.5F, 3.8F, -3.4F, 6F, 7F, 6F, new CubeDeformation(-0.1F, -0.2F, -0.4F))
                .texOffs(78, 107).addBox(-4F, 3F, -4.2F, 7F, 3F, 2F, new CubeDeformation(-0.1F, 0F, -0.3F))
                .texOffs(31, 83).addBox(-4F, 10.2F, -5.2F, 7F, 3F, 8F, new CubeDeformation(0F, -0.2F, -0.3F)),
                PartPose.offset(5F, 11F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
