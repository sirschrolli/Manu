package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie der Gans.
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class GooseGeometry {
    public static final int TEXTURE_WIDTH = 64;
    public static final int TEXTURE_HEIGHT = 64;

    private GooseGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3F, -3F, -5F, 6F, 6F, 10F),
                PartPose.offset(0F, 17F, 0F));
        PartDefinition p_neck = p_body.addOrReplaceChild("neck", CubeListBuilder.create()
                .texOffs(19, 17).addBox(-1F, -8F, -1.5F, 2F, 8F, 2F),
                PartPose.offsetAndRotation(0F, -2F, -4F, 0.2094F, 0F, 0F));
        PartDefinition p_head = p_neck.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(28, 17).addBox(-1.5F, -2.5F, -2.5F, 3F, 3F, 4F)
                .texOffs(0, 30).addBox(-1F, -1.5F, -5F, 2F, 1.5F, 2.5F)
                .texOffs(43, 30).addBox(1.3F, -2F, -1.8F, 0.5F, 1F, 1F)
                .texOffs(48, 30).addBox(-1.8F, -2F, -1.8F, 0.5F, 1F, 1F),
                PartPose.offsetAndRotation(0F, -7.5F, -0.5F, -0.2094F, 0F, 0F));
        PartDefinition p_left_wing = p_body.addOrReplaceChild("left_wing", CubeListBuilder.create()
                .texOffs(33, 0).addBox(0F, 0F, 0F, 1F, 4F, 8F),
                PartPose.offset(3F, -2F, -3F));
        PartDefinition p_right_wing = p_body.addOrReplaceChild("right_wing", CubeListBuilder.create()
                .texOffs(0, 17).addBox(-1F, 0F, 0F, 1F, 4F, 8F),
                PartPose.offset(-3F, -2F, -3F));
        PartDefinition p_tail = p_body.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(43, 17).addBox(-2F, -1F, 0F, 4F, 2F, 3F),
                PartPose.offsetAndRotation(0F, -2.5F, 5F, -0.4363F, 0F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(11, 30).addBox(-0.5F, 0F, -0.5F, 1F, 4F, 1F)
                .texOffs(21, 30).addBox(-1F, 3.5F, -2.5F, 2F, 0.5F, 3F),
                PartPose.offset(1.5F, 20F, 0.5F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(16, 30).addBox(-0.5F, 0F, -0.5F, 1F, 4F, 1F)
                .texOffs(32, 30).addBox(-1F, 3.5F, -2.5F, 2F, 0.5F, 3F),
                PartPose.offset(-1.5F, 20F, 0.5F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
