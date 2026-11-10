package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Chorusschlunds.
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class ChorusMawGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 96;

    private ChorusMawGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_roots = root.addOrReplaceChild("roots", CubeListBuilder.create()
                .texOffs(113, 27).addBox(-6F, -1.6F, -1F, 4F, 2F, 2F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(98, 27).addBox(1.6F, -1.6F, -0.7F, 5F, 2F, 2F, new CubeDeformation(-0.4F, -0.4F, -0.1F))
                .texOffs(53, 16).addBox(-1F, -1.6F, -6.4F, 2F, 2F, 5F, new CubeDeformation(0F, -0.4F, -0.4F))
                .texOffs(68, 16).addBox(-0.9F, -1.6F, 1.7F, 2F, 2F, 5F, new CubeDeformation(-0.1F, -0.4F, -0.3F)),
                PartPose.offset(0F, 24F, 0F));
        PartDefinition p_stalk1 = root.addOrReplaceChild("stalk1", CubeListBuilder.create()
                .texOffs(38, 0).addBox(-2.5F, -6.6F, -2.5F, 5F, 7F, 5F, new CubeDeformation(0F, -0.4F, 0F)),
                PartPose.offset(0F, 24F, 0F));
        PartDefinition p_stalk2 = p_stalk1.addOrReplaceChild("stalk2", CubeListBuilder.create()
                .texOffs(59, 0).addBox(-2.5F, -5.9F, -2.5F, 5F, 6F, 5F, new CubeDeformation(-0.3F, -0.1F, -0.3F)),
                PartPose.offsetAndRotation(0F, -6F, 0F, 0F, 0.2094F, 0F));
        PartDefinition p_stalk3 = p_stalk2.addOrReplaceChild("stalk3", CubeListBuilder.create()
                .texOffs(19, 16).addBox(-2F, -5F, -2F, 4F, 5F, 4F),
                PartPose.offsetAndRotation(0F, -5.6F, 0F, 0F, -0.1745F, 0F));
        PartDefinition p_head = p_stalk3.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(80, 0).addBox(-3.5F, -2.9F, -3.5F, 7F, 3F, 7F, new CubeDeformation(-0.1F, -0.1F, -0.1F))
                .texOffs(84, 34).addBox(-2.7F, -3.65F, -3.05F, 1F, 1F, 1F, new CubeDeformation(-0.15F, -0.05F, -0.15F))
                .texOffs(89, 34).addBox(-2.7F, -3.65F, 2.05F, 1F, 1F, 1F, new CubeDeformation(-0.15F, -0.05F, -0.15F))
                .texOffs(94, 34).addBox(-0.5F, -3.65F, -3.05F, 1F, 1F, 1F, new CubeDeformation(-0.15F, -0.05F, -0.15F))
                .texOffs(99, 34).addBox(-0.5F, -3.65F, 2.05F, 1F, 1F, 1F, new CubeDeformation(-0.15F, -0.05F, -0.15F))
                .texOffs(104, 34).addBox(1.7F, -3.65F, -3.05F, 1F, 1F, 1F, new CubeDeformation(-0.15F, -0.05F, -0.15F))
                .texOffs(109, 34).addBox(1.7F, -3.65F, 2.05F, 1F, 1F, 1F, new CubeDeformation(-0.15F, -0.05F, -0.15F))
                .texOffs(114, 34).addBox(-3.05F, -3.65F, -1.6F, 1F, 1F, 1F, new CubeDeformation(-0.15F, -0.05F, -0.15F))
                .texOffs(119, 34).addBox(2.05F, -3.65F, -1.6F, 1F, 1F, 1F, new CubeDeformation(-0.15F, -0.05F, -0.15F))
                .texOffs(124, 34).addBox(-3.05F, -3.65F, 0.6F, 1F, 1F, 1F, new CubeDeformation(-0.15F, -0.05F, -0.15F))
                .texOffs(0, 39).addBox(2.05F, -3.65F, 0.6F, 1F, 1F, 1F, new CubeDeformation(-0.15F, -0.05F, -0.15F)),
                PartPose.offset(0F, -4.8F, 0F));
        PartDefinition p_sepal_left = p_head.addOrReplaceChild("sepal_left", CubeListBuilder.create()
                .texOffs(109, 16).addBox(-0.4F, -0.2F, -2.5F, 4F, 1F, 5F, new CubeDeformation(-0.4F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(3.2F, -0.6F, 0F, 0F, 0F, 0.6632F));
        PartDefinition p_sepal_right = p_head.addOrReplaceChild("sepal_right", CubeListBuilder.create()
                .texOffs(0, 27).addBox(-3.6F, -0.2F, -2.5F, 4F, 1F, 5F, new CubeDeformation(-0.4F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(-3.2F, -0.6F, 0F, 0F, 0F, -0.6632F));
        PartDefinition p_sepal_front = p_head.addOrReplaceChild("sepal_front", CubeListBuilder.create()
                .texOffs(60, 27).addBox(-2.5F, -0.2F, -3.6F, 5F, 1F, 4F, new CubeDeformation(-0.3F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(0F, -0.6F, -3.2F, -0.6632F, 0F, 0F));
        PartDefinition p_sepal_back = p_head.addOrReplaceChild("sepal_back", CubeListBuilder.create()
                .texOffs(79, 27).addBox(-2.5F, -0.2F, -0.4F, 5F, 1F, 4F, new CubeDeformation(-0.3F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(0F, -0.6F, 3.2F, 0.6632F, 0F, 0F));
        PartDefinition p_petal_front = p_head.addOrReplaceChild("petal_front", CubeListBuilder.create()
                .texOffs(109, 0).addBox(-3.5F, -8F, -0.4F, 7F, 8F, 2F, new CubeDeformation(-0.1F, 0F, -0.4F))
                .texOffs(5, 39).addBox(-2.2F, -6.4F, 1.1F, 1F, 1F, 1F, new CubeDeformation(-0.15F, 0F, -0.1F))
                .texOffs(10, 39).addBox(-0.5F, -6.4F, 1.1F, 1F, 1F, 1F, new CubeDeformation(-0.15F, 0F, -0.1F))
                .texOffs(15, 39).addBox(1.2F, -6.4F, 1.1F, 1F, 1F, 1F, new CubeDeformation(-0.15F, 0F, -0.1F)),
                PartPose.offsetAndRotation(0F, -2.8F, -3.4F, -0.384F, 0F, 0F));
        PartDefinition p_petal_back = p_head.addOrReplaceChild("petal_back", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-3.5F, -8F, -1.6F, 7F, 8F, 2F, new CubeDeformation(-0.1F, 0F, -0.4F))
                .texOffs(20, 39).addBox(-2.2F, -6.4F, -2.1F, 1F, 1F, 1F, new CubeDeformation(-0.15F, 0F, -0.1F))
                .texOffs(25, 39).addBox(-0.5F, -6.4F, -2.1F, 1F, 1F, 1F, new CubeDeformation(-0.15F, 0F, -0.1F))
                .texOffs(30, 39).addBox(1.2F, -6.4F, -2.1F, 1F, 1F, 1F, new CubeDeformation(-0.15F, 0F, -0.1F)),
                PartPose.offsetAndRotation(0F, -2.8F, 3.4F, 0.384F, 0F, 0F));
        PartDefinition p_petal_left = p_head.addOrReplaceChild("petal_left", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-1.6F, -8F, -3.5F, 2F, 8F, 7F, new CubeDeformation(-0.4F, 0F, -0.1F))
                .texOffs(35, 39).addBox(-2.1F, -6.4F, -2.2F, 1F, 1F, 1F, new CubeDeformation(-0.1F, 0F, -0.15F))
                .texOffs(40, 39).addBox(-2.1F, -6.4F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.1F, 0F, -0.15F))
                .texOffs(45, 39).addBox(-2.1F, -6.4F, 1.2F, 1F, 1F, 1F, new CubeDeformation(-0.1F, 0F, -0.15F)),
                PartPose.offsetAndRotation(3.4F, -2.8F, 0F, 0F, 0F, -0.384F));
        PartDefinition p_petal_right = p_head.addOrReplaceChild("petal_right", CubeListBuilder.create()
                .texOffs(19, 0).addBox(-0.4F, -8F, -3.5F, 2F, 8F, 7F, new CubeDeformation(-0.4F, 0F, -0.1F))
                .texOffs(50, 39).addBox(1.1F, -6.4F, -2.2F, 1F, 1F, 1F, new CubeDeformation(-0.1F, 0F, -0.15F))
                .texOffs(55, 39).addBox(1.1F, -6.4F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.1F, 0F, -0.15F))
                .texOffs(60, 39).addBox(1.1F, -6.4F, 1.2F, 1F, 1F, 1F, new CubeDeformation(-0.1F, 0F, -0.15F)),
                PartPose.offsetAndRotation(-3.4F, -2.8F, 0F, 0F, 0F, 0.384F));
        PartDefinition p_vine_left = p_stalk3.addOrReplaceChild("vine_left", CubeListBuilder.create()
                .texOffs(0, 34).addBox(0F, -1F, -1F, 4F, 2F, 2F, new CubeDeformation(0F, -0.45F, -0.45F)),
                PartPose.offsetAndRotation(1.8F, -2.6F, -0.4F, 0F, 0F, -0.733F));
        PartDefinition p_vine_left_mid = p_vine_left.addOrReplaceChild("vine_left_mid", CubeListBuilder.create()
                .texOffs(44, 34).addBox(-0.3F, -0.5F, -0.5F, 4F, 1F, 1F, new CubeDeformation(-0.3F, -0.05F, -0.05F)),
                PartPose.offsetAndRotation(3.8F, 0F, 0F, 0F, 0F, 0.5236F));
        PartDefinition p_vine_left_tip = p_vine_left_mid.addOrReplaceChild("vine_left_tip", CubeListBuilder.create()
                .texOffs(66, 34).addBox(-0.2F, -0.5F, -0.5F, 3F, 1F, 1F, new CubeDeformation(-0.2F, -0.15F, -0.15F))
                .texOffs(26, 34).addBox(1.9F, -1F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(3.2F, 0F, 0F, 0F, 0F, 0.6632F));
        PartDefinition p_vine_right = p_stalk3.addOrReplaceChild("vine_right", CubeListBuilder.create()
                .texOffs(13, 34).addBox(-4F, -1F, -1F, 4F, 2F, 2F, new CubeDeformation(0F, -0.45F, -0.45F)),
                PartPose.offsetAndRotation(-1.8F, -2.6F, -0.4F, 0F, 0F, 0.733F));
        PartDefinition p_vine_right_mid = p_vine_right.addOrReplaceChild("vine_right_mid", CubeListBuilder.create()
                .texOffs(55, 34).addBox(-3.7F, -0.5F, -0.5F, 4F, 1F, 1F, new CubeDeformation(-0.3F, -0.05F, -0.05F)),
                PartPose.offsetAndRotation(-3.8F, 0F, 0F, 0F, 0F, -0.5236F));
        PartDefinition p_vine_right_tip = p_vine_right_mid.addOrReplaceChild("vine_right_tip", CubeListBuilder.create()
                .texOffs(75, 34).addBox(-2.8F, -0.5F, -0.5F, 3F, 1F, 1F, new CubeDeformation(-0.2F, -0.15F, -0.15F))
                .texOffs(35, 34).addBox(-3.9F, -1F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(-3.2F, 0F, 0F, 0F, 0F, -0.6632F));
        PartDefinition p_branch_right = p_stalk2.addOrReplaceChild("branch_right", CubeListBuilder.create()
                .texOffs(34, 27).addBox(-3F, -1.5F, -1.5F, 3F, 3F, 3F, new CubeDeformation(0F, -0.2F, -0.2F)),
                PartPose.offset(-2.1F, -3.6F, 0.6F));
        PartDefinition p_branch_right_up = p_branch_right.addOrReplaceChild("branch_right_up", CubeListBuilder.create()
                .texOffs(83, 16).addBox(-1.5F, -3.6F, -1.5F, 3F, 4F, 3F, new CubeDeformation(-0.4F, -0.4F, -0.4F))
                .texOffs(47, 27).addBox(-1.5F, -5.6F, -1.5F, 3F, 3F, 3F, new CubeDeformation(0F, -0.4F, 0F)),
                PartPose.offset(-2F, -1.2F, 0F));
        PartDefinition p_branch_left = p_stalk1.addOrReplaceChild("branch_left", CubeListBuilder.create()
                .texOffs(19, 27).addBox(-0.3F, -1.5F, -1.5F, 4F, 3F, 3F, new CubeDeformation(-0.3F, 0F, 0F)),
                PartPose.offset(2.4F, -4.4F, 0F));
        PartDefinition p_branch_left_up = p_branch_left.addOrReplaceChild("branch_left_up", CubeListBuilder.create()
                .texOffs(96, 16).addBox(-1.5F, -4F, -1.5F, 3F, 4F, 3F, new CubeDeformation(-0.2F, 0F, -0.2F))
                .texOffs(36, 16).addBox(-2F, -6.5F, -2F, 4F, 3F, 4F, new CubeDeformation(-0.3F, -0.3F, -0.3F)),
                PartPose.offset(2.2F, -1.4F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
