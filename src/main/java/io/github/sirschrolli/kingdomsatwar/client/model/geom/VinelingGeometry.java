package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Ranklings (Diener von Verdantis).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class VinelingGeometry {
    public static final int TEXTURE_WIDTH = 64;
    public static final int TEXTURE_HEIGHT = 64;

    private VinelingGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3F, -9F, -2F, 6F, 10F, 4F)
                .texOffs(9, 26).addBox(-3.5F, -9.5F, -2.5F, 7F, 2.5F, 5F)
                .texOffs(0, 43).addBox(-2.5F, 0F, -1.5F, 5F, 1.5F, 3F),
                PartPose.offset(0F, 14F, 0F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(21, 0).addBox(-3F, -6F, -3F, 6F, 6F, 6F)
                .texOffs(39, 43).addBox(-2.2F, -4.2F, -3.3F, 1.4F, 1.2F, 0.5F)
                .texOffs(46, 43).addBox(0.8F, -4.2F, -3.3F, 1.4F, 1.2F, 0.5F)
                .texOffs(34, 26).addBox(-2F, -8.5F, -2F, 4F, 2.6F, 4F),
                PartPose.offset(0F, -9F, 0F));
        PartDefinition p_petal_front = p_head.addOrReplaceChild("petal_front", CubeListBuilder.create()
                .texOffs(34, 36).addBox(-2.5F, -5F, -0.5F, 5F, 5F, 1F),
                PartPose.offsetAndRotation(0F, -6F, -3F, -0.2094F, 0F, 0F));
        PartDefinition p_petal_back = p_head.addOrReplaceChild("petal_back", CubeListBuilder.create()
                .texOffs(47, 36).addBox(-2.5F, -5F, -0.5F, 5F, 5F, 1F),
                PartPose.offsetAndRotation(0F, -6F, 3F, 0.2094F, 0F, 0F));
        PartDefinition p_petal_left = p_head.addOrReplaceChild("petal_left", CubeListBuilder.create()
                .texOffs(46, 0).addBox(-0.5F, -5F, -2.5F, 1F, 5F, 5F),
                PartPose.offsetAndRotation(3F, -6F, 0F, 0F, 0F, 0.2094F));
        PartDefinition p_petal_right = p_head.addOrReplaceChild("petal_right", CubeListBuilder.create()
                .texOffs(0, 15).addBox(-0.5F, -5F, -2.5F, 1F, 5F, 5F),
                PartPose.offsetAndRotation(-3F, -6F, 0F, 0F, 0F, -0.2094F));
        PartDefinition p_left_arm = p_body.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(31, 15).addBox(-1F, -0.5F, -1F, 2F, 7F, 2F)
                .texOffs(7, 49).addBox(0.2F, 2F, -1.3F, 0.8F, 0.8F, 0.6F),
                PartPose.offsetAndRotation(3.2F, -8F, 0F, 0F, 0F, -0.2094F));
        PartDefinition p_left_forearm = p_left_arm.addOrReplaceChild("left_forearm", CubeListBuilder.create()
                .texOffs(40, 15).addBox(-0.75F, -0.3F, -0.75F, 1.5F, 7F, 1.5F)
                .texOffs(12, 49).addBox(-0.3F, 3F, -1.2F, 0.6F, 0.8F, 0.6F)
                .texOffs(17, 43).addBox(-1.8F, 6F, -0.25F, 3.6F, 3F, 0.5F),
                PartPose.offsetAndRotation(0F, 6F, 0F, -0.1745F, 0F, 0.1047F));
        PartDefinition p_right_arm = p_body.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(49, 15).addBox(-1F, -0.5F, -1F, 2F, 7F, 2F)
                .texOffs(17, 49).addBox(-1F, 2F, -1.3F, 0.8F, 0.8F, 0.6F),
                PartPose.offsetAndRotation(-3.2F, -8F, 0F, 0F, 0F, 0.2094F));
        PartDefinition p_right_forearm = p_right_arm.addOrReplaceChild("right_forearm", CubeListBuilder.create()
                .texOffs(0, 26).addBox(-0.75F, -0.3F, -0.75F, 1.5F, 7F, 1.5F)
                .texOffs(22, 49).addBox(-0.3F, 3F, -1.2F, 0.6F, 0.8F, 0.6F)
                .texOffs(28, 43).addBox(-1.8F, 6F, -0.25F, 3.6F, 3F, 0.5F),
                PartPose.offsetAndRotation(0F, 6F, 0F, -0.1745F, 0F, -0.1047F));
        PartDefinition p_left_leg = p_body.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(13, 15).addBox(-1F, 0F, -1F, 2F, 8F, 2F)
                .texOffs(0, 36).addBox(-1.6F, 7.5F, -2.6F, 3.2F, 1.5F, 4F)
                .texOffs(53, 43).addBox(-0.4F, 8F, -4.2F, 0.8F, 1F, 1.6F),
                PartPose.offset(1.9F, 1F, 0F));
        PartDefinition p_right_leg = p_body.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(22, 15).addBox(-1F, 0F, -1F, 2F, 8F, 2F)
                .texOffs(17, 36).addBox(-1.6F, 7.5F, -2.6F, 3.2F, 1.5F, 4F)
                .texOffs(0, 49).addBox(-0.4F, 8F, -4.2F, 0.8F, 1F, 1.6F),
                PartPose.offset(-1.9F, 1F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
