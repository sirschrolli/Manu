package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Glimmerfalters.
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class GlimmerMothGeometry {
    public static final int TEXTURE_WIDTH = 64;
    public static final int TEXTURE_HEIGHT = 32;

    private GlimmerMothGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(33, 8).addBox(-1F, -1F, -2F, 2F, 2F, 5F)
                .texOffs(50, 16).addBox(-1.5F, -1.5F, -4F, 3F, 3F, 2F),
                PartPose.offset(0F, 20F, 0F));
        PartDefinition p_antenna_l = p_body.addOrReplaceChild("antenna_l", CubeListBuilder.create()
                .texOffs(0, 22).addBox(-0.5F, 0F, -4F, 1F, 0F, 4F),
                PartPose.offsetAndRotation(0.8F, -1.5F, -3.5F, -0.8727F, 0.3491F, 0F));
        PartDefinition p_left_wing = p_body.addOrReplaceChild("left_wing", CubeListBuilder.create()
                .texOffs(0, 0).addBox(0F, 0F, -4F, 9F, 0F, 7F),
                PartPose.offset(1F, -1F, -1F));
        PartDefinition p_left_hind_wing = p_body.addOrReplaceChild("left_hind_wing", CubeListBuilder.create()
                .texOffs(0, 16).addBox(0F, 0F, 0F, 7F, 0F, 5F),
                PartPose.offset(1F, -1F, 1F));
        PartDefinition p_antenna_r = p_body.addOrReplaceChild("antenna_r", CubeListBuilder.create()
                .texOffs(11, 22).addBox(-0.5F, 0F, -4F, 1F, 0F, 4F),
                PartPose.offsetAndRotation(-0.8F, -1.5F, -3.5F, -0.8727F, -0.3491F, 0F));
        PartDefinition p_right_wing = p_body.addOrReplaceChild("right_wing", CubeListBuilder.create()
                .texOffs(0, 8).addBox(-9F, 0F, -4F, 9F, 0F, 7F),
                PartPose.offset(-1F, -1F, -1F));
        PartDefinition p_right_hind_wing = p_body.addOrReplaceChild("right_hind_wing", CubeListBuilder.create()
                .texOffs(25, 16).addBox(-7F, 0F, 0F, 7F, 0F, 5F),
                PartPose.offset(-1F, -1F, 1F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
