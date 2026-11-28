package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Kupferknechts (Knochen wie beim Vanilla-Kupfergolem, dazu der Aufziehschlüssel).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class CopperServantGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 64;

    private CopperServantGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(82, 0).addBox(-4F, -6F, -3F, 8F, 6F, 6F)
                .texOffs(38, 16).addBox(-4.5F, -5.4F, -3.5F, 9F, 1F, 7F, new CubeDeformation(-0.2F, 0F, -0.2F))
                .texOffs(71, 16).addBox(-4.5F, -1.6F, -3.5F, 9F, 1F, 7F, new CubeDeformation(-0.2F, 0F, -0.2F))
                .texOffs(50, 36).addBox(-2F, -4.5F, -3.8F, 4F, 3F, 1F, new CubeDeformation(-0.4F, -0.2F, -0.2F)),
                PartPose.offset(0F, 19F, 0F));
        PartDefinition p_key = p_body.addOrReplaceChild("key", CubeListBuilder.create()
                .texOffs(79, 36).addBox(-0.5F, -0.5F, -0.2F, 1F, 1F, 2F, new CubeDeformation(0F, 0F, -0.2F))
                .texOffs(28, 36).addBox(-2.5F, -2F, 1.4F, 5F, 4F, 1F, new CubeDeformation(-0.3F, -0.4F, -0.2F)),
                PartPose.offset(0F, -3.2F, 3F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4F, -5F, -5F, 8F, 5F, 10F)
                .texOffs(37, 0).addBox(-5F, -1.9F, -6F, 10F, 2F, 12F, new CubeDeformation(-0.4F, -0.3F, -0.4F))
                .texOffs(61, 36).addBox(-3.6F, -4.3F, -5.7F, 3F, 3F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(70, 36).addBox(0.6F, -4.3F, -5.7F, 3F, 3F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(41, 36).addBox(-1F, -2F, -6F, 2F, 3F, 2F)
                .texOffs(19, 36).addBox(-1F, -9F, -1F, 2F, 4F, 2F)
                .texOffs(19, 27).addBox(-2F, -13F, -2F, 4F, 4F, 4F)
                .texOffs(86, 36).addBox(-0.5F, -14.8F, -0.5F, 1F, 2F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offset(0F, -6F, 0F));
        PartDefinition p_right_arm = p_body.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(104, 16).addBox(-3.5F, -1.6F, -2.5F, 4F, 3F, 5F, new CubeDeformation(-0.2F, 0F, -0.3F))
                .texOffs(66, 27).addBox(-3F, 0.8F, -1.5F, 3F, 5F, 3F, new CubeDeformation(-0.2F, -0.4F, 0F))
                .texOffs(0, 16).addBox(-3.5F, 4.8F, -2.5F, 4F, 5F, 5F, new CubeDeformation(-0.3F, -0.4F, -0.4F)),
                PartPose.offset(-4F, -6F, 0F));
        PartDefinition p_left_arm = p_body.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(0, 27).addBox(-0.5F, -1.6F, -2.5F, 4F, 3F, 5F, new CubeDeformation(-0.2F, 0F, -0.3F))
                .texOffs(79, 27).addBox(0F, 0.8F, -1.5F, 3F, 5F, 3F, new CubeDeformation(-0.2F, -0.4F, 0F))
                .texOffs(19, 16).addBox(-0.5F, 4.8F, -2.5F, 4F, 5F, 5F, new CubeDeformation(-0.3F, -0.4F, -0.4F)),
                PartPose.offset(4F, -6F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(36, 27).addBox(-3.5F, -0.3F, -2F, 3F, 4F, 4F, new CubeDeformation(0F, -0.3F, -0.4F))
                .texOffs(92, 27).addBox(-4F, 3.1F, -2.9F, 4F, 2F, 5F, new CubeDeformation(-0.1F, -0.1F, -0.3F)),
                PartPose.offset(0F, 19F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(51, 27).addBox(0.5F, -0.3F, -2F, 3F, 4F, 4F, new CubeDeformation(0F, -0.3F, -0.4F))
                .texOffs(0, 36).addBox(0F, 3.1F, -2.9F, 4F, 2F, 5F, new CubeDeformation(-0.1F, -0.1F, -0.3F)),
                PartPose.offset(0F, 19F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
