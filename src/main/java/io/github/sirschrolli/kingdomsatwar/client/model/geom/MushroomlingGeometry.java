package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Pilzlings.
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class MushroomlingGeometry {
    public static final int TEXTURE_WIDTH = 64;
    public static final int TEXTURE_HEIGHT = 64;

    private MushroomlingGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(41, 0).addBox(-2.5F, -6F, -2F, 5F, 6F, 4F),
                PartPose.offset(0F, 22F, 0F));
        PartDefinition p_left_leg = p_body.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(46, 14).addBox(-1F, 0F, -1.5F, 2F, 2F, 3F, new CubeDeformation(0F, 0F, -0.3F)),
                PartPose.offset(1.3F, 0F, 0F));
        PartDefinition p_left_arm = p_body.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(11, 24).addBox(0F, -0.2F, -1F, 1F, 3F, 2F, new CubeDeformation(0F, -0.2F, -0.25F)),
                PartPose.offset(2.5F, -4.5F, 0F));
        PartDefinition p_right_leg = p_body.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(0, 24).addBox(-1F, 0F, -1.5F, 2F, 2F, 3F, new CubeDeformation(0F, 0F, -0.3F)),
                PartPose.offset(-1.3F, 0F, 0F));
        PartDefinition p_right_arm = p_body.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(18, 24).addBox(-1F, -0.2F, -1F, 1F, 3F, 2F, new CubeDeformation(0F, -0.2F, -0.25F)),
                PartPose.offset(-2.5F, -4.5F, 0F));
        PartDefinition p_cap = p_body.addOrReplaceChild("cap", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-5F, -3F, -5F, 10F, 3F, 10F)
                .texOffs(0, 14).addBox(-3.5F, -5F, -3.5F, 7F, 2F, 7F)
                .texOffs(29, 14).addBox(-2F, -6F, -2F, 4F, 1F, 4F),
                PartPose.offset(0F, -6F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
