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
                .texOffs(103, 26).addBox(-6F, -1.6F, -1F, 4F, 2F, 2F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(88, 26).addBox(1.6F, -1.6F, -0.7F, 5F, 2F, 2F, new CubeDeformation(-0.4F, -0.4F, -0.1F))
                .texOffs(89, 14).addBox(-1F, -1.6F, -6.4F, 2F, 2F, 5F, new CubeDeformation(0F, -0.4F, -0.4F))
                .texOffs(104, 14).addBox(-0.9F, -1.6F, 1.7F, 2F, 2F, 5F, new CubeDeformation(-0.1F, -0.4F, -0.3F)),
                PartPose.offset(0F, 24F, 0F));
        PartDefinition p_stalk1 = root.addOrReplaceChild("stalk1", CubeListBuilder.create()
                .texOffs(67, 0).addBox(-2.5F, -6.6F, -2.5F, 5F, 7F, 5F, new CubeDeformation(0F, -0.4F, 0F)),
                PartPose.offset(0F, 24F, 0F));
        PartDefinition p_stalk2 = p_stalk1.addOrReplaceChild("stalk2", CubeListBuilder.create()
                .texOffs(0, 14).addBox(-2.5F, -5.9F, -2.5F, 5F, 6F, 5F, new CubeDeformation(-0.3F, -0.1F, -0.3F)),
                PartPose.offsetAndRotation(0F, -6F, 0F, 0F, 0.2094F, 0F));
        PartDefinition p_stalk3 = p_stalk2.addOrReplaceChild("stalk3", CubeListBuilder.create()
                .texOffs(21, 14).addBox(-2F, -5F, -2F, 4F, 5F, 4F),
                PartPose.offsetAndRotation(0F, -5.6F, 0F, 0F, -0.1745F, 0F));
        PartDefinition p_head = p_stalk3.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(88, 0).addBox(-3.5F, -3.7F, -3.5F, 7F, 4F, 7F, new CubeDeformation(0F, -0.3F, 0F))
                .texOffs(106, 34).addBox(-3F, -4.3F, -3.35F, 1F, 1F, 1F, new CubeDeformation(-0.15F, 0F, -0.15F))
                .texOffs(111, 34).addBox(-1.3F, -4.3F, -3.35F, 1F, 1F, 1F, new CubeDeformation(-0.15F, 0F, -0.15F))
                .texOffs(116, 34).addBox(0.4F, -4.3F, -3.35F, 1F, 1F, 1F, new CubeDeformation(-0.15F, 0F, -0.15F))
                .texOffs(121, 34).addBox(2.1F, -4.3F, -3.35F, 1F, 1F, 1F, new CubeDeformation(-0.15F, 0F, -0.15F))
                .texOffs(0, 39).addBox(-3.35F, -4.3F, -1.9F, 1F, 1F, 1F, new CubeDeformation(-0.15F, 0F, -0.15F))
                .texOffs(5, 39).addBox(2.35F, -4.3F, -1.9F, 1F, 1F, 1F, new CubeDeformation(-0.15F, 0F, -0.15F))
                .texOffs(10, 39).addBox(-3.35F, -4.3F, 0.1F, 1F, 1F, 1F, new CubeDeformation(-0.15F, 0F, -0.15F))
                .texOffs(15, 39).addBox(2.35F, -4.3F, 0.1F, 1F, 1F, 1F, new CubeDeformation(-0.15F, 0F, -0.15F)),
                PartPose.offset(0F, -4.8F, 0F));
        PartDefinition p_lid = p_head.addOrReplaceChild("lid", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4F, -4.7F, -7.5F, 8F, 5F, 8F, new CubeDeformation(-0.3F, -0.3F, -0.3F))
                .texOffs(26, 26).addBox(-2.5F, -5.4F, -6F, 5F, 1F, 5F, new CubeDeformation(-0.2F, 0F, -0.2F))
                .texOffs(31, 34).addBox(-2.8F, -0.65F, -6.95F, 1F, 2F, 1F, new CubeDeformation(-0.15F, -0.45F, -0.15F))
                .texOffs(36, 34).addBox(-1F, -0.65F, -6.95F, 1F, 2F, 1F, new CubeDeformation(-0.15F, -0.45F, -0.15F))
                .texOffs(41, 34).addBox(0.8F, -0.65F, -6.95F, 1F, 2F, 1F, new CubeDeformation(-0.15F, -0.45F, -0.15F))
                .texOffs(46, 34).addBox(-3.55F, -0.65F, -5.7F, 1F, 2F, 1F, new CubeDeformation(-0.15F, -0.45F, -0.15F))
                .texOffs(51, 34).addBox(2.55F, -0.65F, -5.7F, 1F, 2F, 1F, new CubeDeformation(-0.15F, -0.45F, -0.15F))
                .texOffs(56, 34).addBox(-3.55F, -0.65F, -3.5F, 1F, 2F, 1F, new CubeDeformation(-0.15F, -0.45F, -0.15F))
                .texOffs(61, 34).addBox(2.55F, -0.65F, -3.5F, 1F, 2F, 1F, new CubeDeformation(-0.15F, -0.45F, -0.15F)),
                PartPose.offset(0F, -3.4F, 3.5F));
        PartDefinition p_petal_left = p_head.addOrReplaceChild("petal_left", CubeListBuilder.create()
                .texOffs(33, 0).addBox(-0.05F, -5.6F, -3.5F, 1F, 6F, 7F, new CubeDeformation(-0.05F, 0F, -0.3F)),
                PartPose.offsetAndRotation(3.4F, -0.6F, 0F, 0F, 0F, -0.4189F));
        PartDefinition p_petal_right = p_head.addOrReplaceChild("petal_right", CubeListBuilder.create()
                .texOffs(50, 0).addBox(-0.95F, -5.6F, -3.5F, 1F, 6F, 7F, new CubeDeformation(-0.05F, 0F, -0.3F)),
                PartPose.offsetAndRotation(-3.4F, -0.6F, 0F, 0F, 0F, 0.4189F));
        PartDefinition p_petal_front = p_head.addOrReplaceChild("petal_front", CubeListBuilder.create()
                .texOffs(38, 14).addBox(-3.5F, -5.6F, -0.95F, 7F, 6F, 1F, new CubeDeformation(-0.3F, 0F, -0.05F)),
                PartPose.offsetAndRotation(0F, -0.6F, -3.4F, 0.4189F, 0F, 0F));
        PartDefinition p_petal_back = p_head.addOrReplaceChild("petal_back", CubeListBuilder.create()
                .texOffs(55, 14).addBox(-3.5F, -5.6F, -0.05F, 7F, 6F, 1F, new CubeDeformation(-0.3F, 0F, -0.05F)),
                PartPose.offsetAndRotation(0F, -0.6F, 3.4F, -0.4189F, 0F, 0F));
        PartDefinition p_vine_left = p_stalk3.addOrReplaceChild("vine_left", CubeListBuilder.create()
                .texOffs(116, 26).addBox(0F, -1F, -1F, 4F, 2F, 2F, new CubeDeformation(0F, -0.45F, -0.45F)),
                PartPose.offsetAndRotation(1.8F, -2.6F, -0.4F, 0F, 0F, -0.733F));
        PartDefinition p_vine_left_mid = p_vine_left.addOrReplaceChild("vine_left_mid", CubeListBuilder.create()
                .texOffs(66, 34).addBox(-0.3F, -0.5F, -0.5F, 4F, 1F, 1F, new CubeDeformation(-0.3F, -0.05F, -0.05F)),
                PartPose.offsetAndRotation(3.8F, 0F, 0F, 0F, 0F, 0.5236F));
        PartDefinition p_vine_left_tip = p_vine_left_mid.addOrReplaceChild("vine_left_tip", CubeListBuilder.create()
                .texOffs(88, 34).addBox(-0.2F, -0.5F, -0.5F, 3F, 1F, 1F, new CubeDeformation(-0.2F, -0.15F, -0.15F))
                .texOffs(13, 34).addBox(1.9F, -1F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(3.2F, 0F, 0F, 0F, 0F, 0.6632F));
        PartDefinition p_vine_right = p_stalk3.addOrReplaceChild("vine_right", CubeListBuilder.create()
                .texOffs(0, 34).addBox(-4F, -1F, -1F, 4F, 2F, 2F, new CubeDeformation(0F, -0.45F, -0.45F)),
                PartPose.offsetAndRotation(-1.8F, -2.6F, -0.4F, 0F, 0F, 0.733F));
        PartDefinition p_vine_right_mid = p_vine_right.addOrReplaceChild("vine_right_mid", CubeListBuilder.create()
                .texOffs(77, 34).addBox(-3.7F, -0.5F, -0.5F, 4F, 1F, 1F, new CubeDeformation(-0.3F, -0.05F, -0.05F)),
                PartPose.offsetAndRotation(-3.8F, 0F, 0F, 0F, 0F, -0.5236F));
        PartDefinition p_vine_right_tip = p_vine_right_mid.addOrReplaceChild("vine_right_tip", CubeListBuilder.create()
                .texOffs(97, 34).addBox(-2.8F, -0.5F, -0.5F, 3F, 1F, 1F, new CubeDeformation(-0.2F, -0.15F, -0.15F))
                .texOffs(22, 34).addBox(-3.9F, -1F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(-3.2F, 0F, 0F, 0F, 0F, -0.6632F));
        PartDefinition p_branch_right = p_stalk2.addOrReplaceChild("branch_right", CubeListBuilder.create()
                .texOffs(62, 26).addBox(-3F, -1.5F, -1.5F, 3F, 3F, 3F, new CubeDeformation(0F, -0.2F, -0.2F)),
                PartPose.offset(-2.1F, -3.6F, 0.6F));
        PartDefinition p_branch_right_up = p_branch_right.addOrReplaceChild("branch_right_up", CubeListBuilder.create()
                .texOffs(0, 26).addBox(-1.5F, -3.6F, -1.5F, 3F, 4F, 3F, new CubeDeformation(-0.4F, -0.4F, -0.4F))
                .texOffs(75, 26).addBox(-1.5F, -5.6F, -1.5F, 3F, 3F, 3F, new CubeDeformation(0F, -0.4F, 0F)),
                PartPose.offset(-2F, -1.2F, 0F));
        PartDefinition p_branch_left = p_stalk1.addOrReplaceChild("branch_left", CubeListBuilder.create()
                .texOffs(47, 26).addBox(-0.3F, -1.5F, -1.5F, 4F, 3F, 3F, new CubeDeformation(-0.3F, 0F, 0F)),
                PartPose.offset(2.4F, -4.4F, 0F));
        PartDefinition p_branch_left_up = p_branch_left.addOrReplaceChild("branch_left_up", CubeListBuilder.create()
                .texOffs(13, 26).addBox(-1.5F, -4F, -1.5F, 3F, 4F, 3F, new CubeDeformation(-0.2F, 0F, -0.2F))
                .texOffs(72, 14).addBox(-2F, -6.5F, -2F, 4F, 3F, 4F, new CubeDeformation(-0.3F, -0.3F, -0.3F)),
                PartPose.offset(2.2F, -1.4F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
