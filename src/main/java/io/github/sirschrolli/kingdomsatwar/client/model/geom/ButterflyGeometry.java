package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie der Schmetterlinge (Flügelform in der Textur).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class ButterflyGeometry {
    public static final int TEXTURE_WIDTH = 32;
    public static final int TEXTURE_HEIGHT = 32;

    private ButterflyGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(13, 9).addBox(-0.5F, -0.5F, -1.3F, 1F, 1F, 2F, new CubeDeformation(-0.2F, -0.2F, -0.3F))
                .texOffs(0, 0).addBox(-0.5F, -0.5F, 0F, 1F, 1F, 3F, new CubeDeformation(-0.28F, -0.28F, -0.4F)),
                PartPose.offset(0F, 22F, 0F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(7, 13).addBox(-0.5F, -0.52F, -0.775F, 1F, 1F, 1F, new CubeDeformation(-0.18F, -0.2F, -0.225F)),
                PartPose.offset(0F, 0F, -1F));
        PartDefinition p_antenna_l = p_head.addOrReplaceChild("antenna_l", CubeListBuilder.create()
                .texOffs(20, 9).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.45F, -0.45F, 0F))
                .texOffs(12, 13).addBox(-0.5F, -0.5F, -2.59F, 1F, 1F, 1F, new CubeDeformation(-0.39F, -0.39F, -0.34F)),
                PartPose.offsetAndRotation(0.15F, -0.25F, -0.4F, -0.4887F, 0.384F, 0F));
        PartDefinition p_antenna_r = p_head.addOrReplaceChild("antenna_r", CubeListBuilder.create()
                .texOffs(0, 13).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.45F, -0.45F, 0F))
                .texOffs(17, 13).addBox(-0.5F, -0.5F, -2.59F, 1F, 1F, 1F, new CubeDeformation(-0.39F, -0.39F, -0.34F)),
                PartPose.offsetAndRotation(-0.15F, -0.25F, -0.4F, -0.4887F, -0.384F, 0F));
        PartDefinition p_wing_l = p_body.addOrReplaceChild("wing_l", CubeListBuilder.create()
                .texOffs(9, 0).addBox(0F, 0F, -2.2F, 4F, 0F, 3F, new CubeDeformation(0F, 0F, -0.3F)),
                PartPose.offsetAndRotation(0.25F, -0.2F, -0.5F, 0F, 0.2793F, 0F));
        PartDefinition p_hind_l = p_body.addOrReplaceChild("hind_l", CubeListBuilder.create()
                .texOffs(15, 5).addBox(0F, 0F, -0.3F, 3F, 0F, 3F, new CubeDeformation(0F, 0F, -0.1F)),
                PartPose.offsetAndRotation(0.22F, -0.18F, 0.2F, 0F, -0.1396F, 0F));
        PartDefinition p_wing_r = p_body.addOrReplaceChild("wing_r", CubeListBuilder.create()
                .texOffs(0, 5).addBox(-4F, 0F, -2.2F, 4F, 0F, 3F, new CubeDeformation(0F, 0F, -0.3F)),
                PartPose.offsetAndRotation(-0.25F, -0.2F, -0.5F, 0F, -0.2793F, 0F));
        PartDefinition p_hind_r = p_body.addOrReplaceChild("hind_r", CubeListBuilder.create()
                .texOffs(0, 9).addBox(-3F, 0F, -0.3F, 3F, 0F, 3F, new CubeDeformation(0F, 0F, -0.1F)),
                PartPose.offsetAndRotation(-0.22F, -0.18F, 0.2F, 0F, 0.1396F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
