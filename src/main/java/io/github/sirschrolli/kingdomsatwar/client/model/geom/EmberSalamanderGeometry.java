package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Glutsalamanders.
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class EmberSalamanderGeometry {
    public static final int TEXTURE_WIDTH = 64;
    public static final int TEXTURE_HEIGHT = 32;

    private EmberSalamanderGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-2F, -1.5F, -2.6F, 4F, 3F, 5F, new CubeDeformation(-0.3F, -0.35F, 0F)),
                PartPose.offset(0F, 21.6F, -2F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(38, 0).addBox(-2F, -1.05F, -3.7F, 4F, 2F, 4F, new CubeDeformation(-0.2F, -0.05F, -0.1F))
                .texOffs(57, 22).addBox(-1.5F, -1.7F, -2.75F, 1F, 1F, 1F, new CubeDeformation(-0.05F, -0.15F, -0.05F))
                .texOffs(0, 26).addBox(0.5F, -1.7F, -2.75F, 1F, 1F, 1F, new CubeDeformation(-0.05F, -0.15F, -0.05F)),
                PartPose.offset(0F, -0.1F, -2.6F));
        PartDefinition p_jaw = p_head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(28, 9).addBox(-2F, -0.25F, -3.5F, 4F, 1F, 4F, new CubeDeformation(-0.3F, -0.25F, -0.4F)),
                PartPose.offset(0F, 0.45F, -0.4F));
        PartDefinition p_body2 = p_body.addOrReplaceChild("body2", CubeListBuilder.create()
                .texOffs(19, 0).addBox(-2F, -1.55F, -0.3F, 4F, 3F, 5F, new CubeDeformation(-0.4F, -0.45F, -0.3F)),
                PartPose.offset(0F, 0.05F, 2.3F));
        PartDefinition p_tail1 = p_body2.addOrReplaceChild("tail1", CubeListBuilder.create()
                .texOffs(0, 9).addBox(-1.5F, -1F, -0.2F, 3F, 2F, 4F, new CubeDeformation(-0.3F, -0.15F, -0.2F))
                .texOffs(0, 16).addBox(-0.5F, -1.65F, -0.1F, 1F, 1F, 4F, new CubeDeformation(-0.35F, -0.2F, -0.3F)),
                PartPose.offset(0F, -0.15F, 4.3F));
        PartDefinition p_tail2 = p_tail1.addOrReplaceChild("tail2", CubeListBuilder.create()
                .texOffs(15, 9).addBox(-1F, -1F, -0.2F, 2F, 2F, 4F, new CubeDeformation(-0.15F, -0.35F, -0.2F))
                .texOffs(11, 16).addBox(-0.5F, -1.4F, -0.2F, 1F, 1F, 4F, new CubeDeformation(-0.37F, -0.25F, -0.2F)),
                PartPose.offset(0F, 0.05F, 3.5F));
        PartDefinition p_tail3 = p_tail2.addOrReplaceChild("tail3", CubeListBuilder.create()
                .texOffs(45, 9).addBox(-1F, -0.5F, -0.3F, 2F, 1F, 4F, new CubeDeformation(-0.45F, -0.05F, -0.3F))
                .texOffs(22, 16).addBox(-0.5F, -1.15F, 0F, 1F, 1F, 3F, new CubeDeformation(-0.4F, -0.3F, 0F)),
                PartPose.offset(0F, 0.05F, 3.5F));
        PartDefinition p_left_hind_leg = p_body2.addOrReplaceChild("left_hind_leg", CubeListBuilder.create()
                .texOffs(29, 22).addBox(-0.05F, -0.5F, -0.5F, 2F, 1F, 1F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offset(1.5F, 0.3F, 2.6F));
        PartDefinition p_left_hind_foot = p_left_hind_leg.addOrReplaceChild("left_hind_foot", CubeListBuilder.create()
                .texOffs(9, 22).addBox(-0.5F, -0.35F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.05F, -0.05F, -0.05F))
                .texOffs(31, 16).addBox(-1F, 1.075F, -1.45F, 2F, 1F, 2F, new CubeDeformation(-0.05F, -0.375F, -0.2F)),
                PartPose.offset(1.8F, 0F, 0F));
        PartDefinition p_right_hind_leg = p_body2.addOrReplaceChild("right_hind_leg", CubeListBuilder.create()
                .texOffs(36, 22).addBox(-1.95F, -0.5F, -0.5F, 2F, 1F, 1F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offset(-1.5F, 0.3F, 2.6F));
        PartDefinition p_right_hind_foot = p_right_hind_leg.addOrReplaceChild("right_hind_foot", CubeListBuilder.create()
                .texOffs(14, 22).addBox(-0.5F, -0.35F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.05F, -0.05F, -0.05F))
                .texOffs(40, 16).addBox(-1F, 1.075F, -1.45F, 2F, 1F, 2F, new CubeDeformation(-0.05F, -0.375F, -0.2F)),
                PartPose.offset(-1.8F, 0F, 0F));
        PartDefinition p_left_front_leg = p_body.addOrReplaceChild("left_front_leg", CubeListBuilder.create()
                .texOffs(43, 22).addBox(-0.05F, -0.5F, -0.5F, 2F, 1F, 1F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offset(1.5F, 0.3F, -1F));
        PartDefinition p_left_front_foot = p_left_front_leg.addOrReplaceChild("left_front_foot", CubeListBuilder.create()
                .texOffs(19, 22).addBox(-0.5F, -0.35F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.05F, -0.05F, -0.05F))
                .texOffs(49, 16).addBox(-1F, 1.075F, -1.45F, 2F, 1F, 2F, new CubeDeformation(-0.05F, -0.375F, -0.2F)),
                PartPose.offset(1.8F, 0F, 0F));
        PartDefinition p_right_front_leg = p_body.addOrReplaceChild("right_front_leg", CubeListBuilder.create()
                .texOffs(50, 22).addBox(-1.95F, -0.5F, -0.5F, 2F, 1F, 1F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offset(-1.5F, 0.3F, -1F));
        PartDefinition p_right_front_foot = p_right_front_leg.addOrReplaceChild("right_front_foot", CubeListBuilder.create()
                .texOffs(24, 22).addBox(-0.5F, -0.35F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.05F, -0.05F, -0.05F))
                .texOffs(0, 22).addBox(-1F, 1.075F, -1.45F, 2F, 1F, 2F, new CubeDeformation(-0.05F, -0.375F, -0.2F)),
                PartPose.offset(-1.8F, 0F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
