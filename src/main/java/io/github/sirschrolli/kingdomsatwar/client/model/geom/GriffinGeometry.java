package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Greifen (Ruhehaltung: Schwingen ausgebreitet).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class GriffinGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 128;

    private GriffinGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.5F, -4.5F, -9F, 9F, 9F, 18F)
                .texOffs(55, 0).addBox(-5F, -5.8F, -10.5F, 10F, 11F, 9F, new CubeDeformation(0F, -0.2F, -0.3F)),
                PartPose.offset(0F, 10F, 2F));
        PartDefinition p_neck = p_body.addOrReplaceChild("neck", CubeListBuilder.create()
                .texOffs(94, 0).addBox(-3F, -9.9F, -3.6F, 6F, 11F, 7F, new CubeDeformation(0F, -0.3F, -0.4F))
                .texOffs(75, 28).addBox(-4F, -3.6F, -4.2F, 8F, 5F, 8F, new CubeDeformation(-0.4F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(0F, -3.4F, -8.4F, 0.384F, 0F, 0F));
        PartDefinition p_head = p_neck.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(46, 28).addBox(-3F, -5.5F, -5.5F, 6F, 6F, 8F, new CubeDeformation(0F, 0F, -0.1F))
                .texOffs(56, 79).addBox(-3.5F, -5.75F, -6.05F, 7F, 2F, 4F, new CubeDeformation(-0.25F, -0.35F, -0.35F))
                .texOffs(74, 88).addBox(-2F, -5.1F, -6.25F, 4F, 3F, 1F, new CubeDeformation(-0.35F, -0.2F, -0.05F))
                .texOffs(43, 79).addBox(-1.5F, -5.225F, -8.4F, 3F, 4F, 3F, new CubeDeformation(-0.05F, -0.475F, 0F))
                .texOffs(54, 88).addBox(-1.5F, -4.7F, -10.15F, 3F, 3F, 2F, new CubeDeformation(-0.3F, -0.25F, -0.15F))
                .texOffs(65, 88).addBox(-1F, -2.475F, -10.8F, 2F, 3F, 2F, new CubeDeformation(-0.05F, -0.125F, -0.3F))
                .texOffs(121, 88).addBox(2.53F, -4.25F, -4.65F, 1F, 2F, 2F, new CubeDeformation(-0.42F, -0.35F, -0.25F))
                .texOffs(0, 95).addBox(-3.53F, -4.25F, -4.65F, 1F, 2F, 2F, new CubeDeformation(-0.42F, -0.35F, -0.25F)),
                PartPose.offsetAndRotation(0F, -9F, -0.6F, -0.384F, 0F, 0F));
        PartDefinition p_jaw = p_head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(13, 88).addBox(-1.5F, 0F, -3.75F, 3F, 1F, 4F, new CubeDeformation(-0.3F, 0F, -0.25F)),
                PartPose.offset(0F, -1.7F, -5.6F));
        PartDefinition p_tuft_l = p_head.addOrReplaceChild("tuft_l", CubeListBuilder.create()
                .texOffs(113, 79).addBox(-1F, -1F, -0.1F, 2F, 2F, 4F, new CubeDeformation(-0.2F, -0.3F, -0.1F)),
                PartPose.offsetAndRotation(1.9F, -5F, 1.6F, -0.4538F, 0.2793F, 0F));
        PartDefinition p_tuft_r = p_head.addOrReplaceChild("tuft_r", CubeListBuilder.create()
                .texOffs(0, 88).addBox(-1F, -1F, -0.1F, 2F, 2F, 4F, new CubeDeformation(-0.2F, -0.3F, -0.1F)),
                PartPose.offsetAndRotation(-1.9F, -5F, 1.6F, -0.4538F, -0.2793F, 0F));
        PartDefinition p_wing_l = p_body.addOrReplaceChild("wing_l", CubeListBuilder.create()
                .texOffs(0, 44).addBox(-0.25F, -0.5F, -1.4F, 10F, 1F, 10F, new CubeDeformation(-0.25F, 0F, -0.2F)),
                PartPose.offset(4.2F, -4F, -5.5F));
        PartDefinition p_wing_l_mid = p_wing_l.addOrReplaceChild("wing_l_mid", CubeListBuilder.create()
                .texOffs(41, 44).addBox(-0.25F, -0.5F, -1.3F, 10F, 1F, 10F, new CubeDeformation(-0.25F, -0.1F, -0.3F)),
                PartPose.offset(9.5F, 0F, 0F));
        PartDefinition p_wing_l_tip = p_wing_l_mid.addOrReplaceChild("wing_l_tip", CubeListBuilder.create()
                .texOffs(75, 56).addBox(0F, -0.5F, -0.9F, 14F, 1F, 9F, new CubeDeformation(0F, -0.1F, -0.1F)),
                PartPose.offset(9.5F, 0F, 0F));
        PartDefinition p_wing_r = p_body.addOrReplaceChild("wing_r", CubeListBuilder.create()
                .texOffs(82, 44).addBox(-9.75F, -0.5F, -1.4F, 10F, 1F, 10F, new CubeDeformation(-0.25F, 0F, -0.2F)),
                PartPose.offset(-4.2F, -4F, -5.5F));
        PartDefinition p_wing_r_mid = p_wing_r.addOrReplaceChild("wing_r_mid", CubeListBuilder.create()
                .texOffs(0, 56).addBox(-9.75F, -0.5F, -1.3F, 10F, 1F, 10F, new CubeDeformation(-0.25F, -0.1F, -0.3F)),
                PartPose.offset(-9.5F, 0F, 0F));
        PartDefinition p_wing_r_tip = p_wing_r_mid.addOrReplaceChild("wing_r_tip", CubeListBuilder.create()
                .texOffs(0, 68).addBox(-14F, -0.5F, -0.9F, 14F, 1F, 9F, new CubeDeformation(0F, -0.1F, -0.1F)),
                PartPose.offset(-9.5F, 0F, 0F));
        PartDefinition p_tail = p_body.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(47, 68).addBox(-1F, -1F, -0.25F, 2F, 2F, 8F, new CubeDeformation(-0.2F, -0.2F, -0.25F)),
                PartPose.offsetAndRotation(0F, -3.2F, 8.6F, -0.8727F, 0F, 0F));
        PartDefinition p_tail2 = p_tail.addOrReplaceChild("tail2", CubeListBuilder.create()
                .texOffs(68, 68).addBox(-1F, -1F, -0.25F, 2F, 2F, 7F, new CubeDeformation(-0.3F, -0.3F, -0.25F)),
                PartPose.offsetAndRotation(0F, 0F, 7.4F, 0.6632F, 0F, 0F));
        PartDefinition p_tail_tuft = p_tail2.addOrReplaceChild("tail_tuft", CubeListBuilder.create()
                .texOffs(0, 79).addBox(-1.5F, -1.5F, -1F, 3F, 3F, 5F, new CubeDeformation(-0.2F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(0F, 0F, 6.4F, 0.1745F, 0F, 0F));
        PartDefinition p_left_front_leg = root.addOrReplaceChild("left_front_leg", CubeListBuilder.create()
                .texOffs(41, 56).addBox(-2F, -1.2F, -2F, 4F, 7F, 4F, new CubeDeformation(-0.1F, -0.2F, 0F)),
                PartPose.offset(3F, 13F, -4.6F));
        PartDefinition p_left_front_shin = p_left_front_leg.addOrReplaceChild("left_front_shin", CubeListBuilder.create()
                .texOffs(17, 79).addBox(-1.5F, -0.45F, -1.5F, 3F, 5F, 3F, new CubeDeformation(-0.3F, -0.45F, -0.3F)),
                PartPose.offset(0F, 5.5F, -0.2F));
        PartDefinition p_left_front_foot = p_left_front_shin.addOrReplaceChild("left_front_foot", CubeListBuilder.create()
                .texOffs(28, 88).addBox(-1F, 0.525F, -3.6F, 2F, 1F, 4F, new CubeDeformation(-0.4F, -0.025F, -0.1F))
                .texOffs(85, 88).addBox(-1.75F, 0.575F, -2.75F, 1F, 1F, 3F, new CubeDeformation(0F, -0.075F, -0.05F))
                .texOffs(94, 88).addBox(0.75F, 0.575F, -2.75F, 1F, 1F, 3F, new CubeDeformation(0F, -0.075F, -0.05F))
                .texOffs(21, 95).addBox(-0.5F, 0.575F, 0.1F, 1F, 1F, 2F, new CubeDeformation(0F, -0.075F, -0.1F))
                .texOffs(7, 95).addBox(-0.5F, 0F, -5.05F, 1F, 2F, 2F, new CubeDeformation(-0.08F, -0.4F, -0.45F))
                .texOffs(35, 95).addBox(-1.75F, 0.5F, -3.7F, 1F, 1F, 1F, new CubeDeformation(-0.15F, 0F, 0F))
                .texOffs(40, 95).addBox(0.75F, 0.5F, -3.7F, 1F, 1F, 1F, new CubeDeformation(-0.15F, 0F, 0F))
                .texOffs(45, 95).addBox(-0.5F, 0.5F, 1.95F, 1F, 1F, 1F, new CubeDeformation(-0.12F, 0F, -0.05F)),
                PartPose.offset(0F, 4F, 0F));
        PartDefinition p_right_front_leg = root.addOrReplaceChild("right_front_leg", CubeListBuilder.create()
                .texOffs(58, 56).addBox(-2F, -1.2F, -2F, 4F, 7F, 4F, new CubeDeformation(-0.1F, -0.2F, 0F)),
                PartPose.offset(-3F, 13F, -4.6F));
        PartDefinition p_right_front_shin = p_right_front_leg.addOrReplaceChild("right_front_shin", CubeListBuilder.create()
                .texOffs(30, 79).addBox(-1.5F, -0.45F, -1.5F, 3F, 5F, 3F, new CubeDeformation(-0.3F, -0.45F, -0.3F)),
                PartPose.offset(0F, 5.5F, -0.2F));
        PartDefinition p_right_front_foot = p_right_front_shin.addOrReplaceChild("right_front_foot", CubeListBuilder.create()
                .texOffs(41, 88).addBox(-1F, 0.525F, -3.6F, 2F, 1F, 4F, new CubeDeformation(-0.4F, -0.025F, -0.1F))
                .texOffs(103, 88).addBox(-1.75F, 0.575F, -2.75F, 1F, 1F, 3F, new CubeDeformation(0F, -0.075F, -0.05F))
                .texOffs(112, 88).addBox(0.75F, 0.575F, -2.75F, 1F, 1F, 3F, new CubeDeformation(0F, -0.075F, -0.05F))
                .texOffs(28, 95).addBox(-0.5F, 0.575F, 0.1F, 1F, 1F, 2F, new CubeDeformation(0F, -0.075F, -0.1F))
                .texOffs(14, 95).addBox(-0.5F, 0F, -5.05F, 1F, 2F, 2F, new CubeDeformation(-0.08F, -0.4F, -0.45F))
                .texOffs(50, 95).addBox(-1.75F, 0.5F, -3.7F, 1F, 1F, 1F, new CubeDeformation(-0.15F, 0F, 0F))
                .texOffs(55, 95).addBox(0.75F, 0.5F, -3.7F, 1F, 1F, 1F, new CubeDeformation(-0.15F, 0F, 0F))
                .texOffs(60, 95).addBox(-0.5F, 0.5F, 1.95F, 1F, 1F, 1F, new CubeDeformation(-0.12F, 0F, -0.05F)),
                PartPose.offset(0F, 4F, 0F));
        PartDefinition p_left_hind_leg = root.addOrReplaceChild("left_hind_leg", CubeListBuilder.create()
                .texOffs(0, 28).addBox(-2.5F, -1.75F, -3F, 5F, 9F, 6F, new CubeDeformation(-0.4F, -0.25F, -0.4F)),
                PartPose.offset(3.2F, 10.5F, 8F));
        PartDefinition p_left_hind_shin = p_left_hind_leg.addOrReplaceChild("left_hind_shin", CubeListBuilder.create()
                .texOffs(87, 68).addBox(-1.5F, -0.25F, -1.5F, 3F, 6F, 3F, new CubeDeformation(-0.2F, -0.25F, -0.2F)),
                PartPose.offset(0F, 6.5F, 1.6F));
        PartDefinition p_left_hind_paw = p_left_hind_shin.addOrReplaceChild("left_hind_paw", CubeListBuilder.create()
                .texOffs(79, 79).addBox(-2F, -0.25F, -2.8F, 4F, 2F, 4F, new CubeDeformation(-0.3F, -0.25F, -0.2F)),
                PartPose.offset(0F, 5.5F, 0F));
        PartDefinition p_right_hind_leg = root.addOrReplaceChild("right_hind_leg", CubeListBuilder.create()
                .texOffs(23, 28).addBox(-2.5F, -1.75F, -3F, 5F, 9F, 6F, new CubeDeformation(-0.4F, -0.25F, -0.4F)),
                PartPose.offset(-3.2F, 10.5F, 8F));
        PartDefinition p_right_hind_shin = p_right_hind_leg.addOrReplaceChild("right_hind_shin", CubeListBuilder.create()
                .texOffs(100, 68).addBox(-1.5F, -0.25F, -1.5F, 3F, 6F, 3F, new CubeDeformation(-0.2F, -0.25F, -0.2F)),
                PartPose.offset(0F, 6.5F, 1.6F));
        PartDefinition p_right_hind_paw = p_right_hind_shin.addOrReplaceChild("right_hind_paw", CubeListBuilder.create()
                .texOffs(96, 79).addBox(-2F, -0.25F, -2.8F, 4F, 2F, 4F, new CubeDeformation(-0.3F, -0.25F, -0.2F)),
                PartPose.offset(0F, 5.5F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
