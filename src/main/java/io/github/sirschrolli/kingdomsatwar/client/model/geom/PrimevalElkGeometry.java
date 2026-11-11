package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Urelchs (Urhain; Sattel, Zaumzeug und Truhen als eigene Knochen).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class PrimevalElkGeometry {
    public static final int TEXTURE_WIDTH = 192;
    public static final int TEXTURE_HEIGHT = 128;

    private PrimevalElkGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-7F, -5F, -13F, 14F, 13F, 26F),
                PartPose.offsetAndRotation(0F, 2F, 1F, -0.0524F, 0F, 0F));
        PartDefinition p_hump = p_body.addOrReplaceChild("hump", CubeListBuilder.create()
                .texOffs(81, 0).addBox(-6.5F, -12.75F, -13.8F, 13F, 9F, 12F, new CubeDeformation(-0.3F, -0.25F, 0F))
                .texOffs(0, 88).addBox(-5.5F, -8.8F, -2F, 11F, 4F, 5F, new CubeDeformation(-0.1F, 0F, 0F))
                .texOffs(72, 59).addBox(-7F, -6F, -14.4F, 14F, 10F, 4F, new CubeDeformation(-0.4F, 0F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_neck = p_body.addOrReplaceChild("neck", CubeListBuilder.create()
                .texOffs(132, 0).addBox(-4F, -5F, -10F, 8F, 10F, 11F),
                PartPose.offsetAndRotation(0F, -5.5F, -13F, 0.1396F, 0F, 0F));
        PartDefinition p_head = p_neck.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(50, 40).addBox(-4F, -5F, -6.75F, 8F, 9F, 8F, new CubeDeformation(0F, -0.4F, -0.25F))
                .texOffs(83, 40).addBox(-3.5F, -4.1F, -15F, 7F, 8F, 9F, new CubeDeformation(-0.2F, -0.3F, 0F))
                .texOffs(88, 88).addBox(-3.5F, -4.1F, -17F, 7F, 5F, 3F, new CubeDeformation(0F, -0.1F, -0.4F))
                .texOffs(46, 105).addBox(3.575F, -3.25F, -5.7F, 1F, 2F, 2F, new CubeDeformation(-0.375F, -0.35F, -0.1F))
                .texOffs(53, 105).addBox(-4.575F, -3.25F, -5.7F, 1F, 2F, 2F, new CubeDeformation(-0.375F, -0.35F, -0.1F)),
                PartPose.offsetAndRotation(0F, -0.5F, -9.5F, 0.6981F, 0F, 0F));
        PartDefinition p_jaw = p_head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(43, 75).addBox(-3F, 0F, -9.6F, 6F, 2F, 10F, new CubeDeformation(-0.3F, 0F, 0F)),
                PartPose.offset(0F, 3.4F, -6F));
        PartDefinition p_bell = p_head.addOrReplaceChild("bell", CubeListBuilder.create()
                .texOffs(132, 75).addBox(-1F, -0.25F, -1.6F, 2F, 8F, 3F, new CubeDeformation(0F, -0.25F, -0.2F)),
                PartPose.offsetAndRotation(0F, 3.6F, -3.8F, -0.6981F, 0F, 0F));
        PartDefinition p_ear_l = p_head.addOrReplaceChild("ear_l", CubeListBuilder.create()
                .texOffs(156, 98).addBox(-0.4F, -1.4F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.4F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(3.8F, -4.2F, -1.6F, -0.6981F, 0F, -0.384F));
        PartDefinition p_ear_r = p_head.addOrReplaceChild("ear_r", CubeListBuilder.create()
                .texOffs(173, 98).addBox(-5.6F, -1.4F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.4F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(-3.8F, -4.2F, -1.6F, -0.6981F, 0F, 0.384F));
        PartDefinition p_antler_l = p_head.addOrReplaceChild("antler_l", CubeListBuilder.create()
                .texOffs(157, 88).addBox(0F, -1.5F, -1.5F, 5F, 3F, 3F, new CubeDeformation(0F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(3.2F, -4.4F, -2.4F, -0.8378F, 0F, -0.1745F));
        PartDefinition p_antler_l_palm = p_antler_l.addOrReplaceChild("antler_l_palm", CubeListBuilder.create()
                .texOffs(143, 59).addBox(0F, -1F, -5.4F, 11F, 2F, 10F, new CubeDeformation(0F, -0.3F, 0F))
                .texOffs(79, 98).addBox(9.4F, -4.5F, -5.8F, 2F, 4F, 2F, new CubeDeformation(-0.4F, -0.2F, -0.4F))
                .texOffs(129, 88).addBox(9.4F, -5.4F, -3F, 2F, 5F, 2F, new CubeDeformation(-0.4F, -0.3F, -0.4F))
                .texOffs(88, 98).addBox(9.4F, -4.7F, -0.2F, 2F, 4F, 2F, new CubeDeformation(-0.4F, 0F, -0.4F))
                .texOffs(97, 98).addBox(9.4F, -4.4F, 2.6F, 2F, 4F, 2F, new CubeDeformation(-0.4F, -0.3F, -0.4F))
                .texOffs(0, 105).addBox(5.2F, -3.6F, -6.2F, 2F, 3F, 2F, new CubeDeformation(-0.4F, -0.1F, -0.4F))
                .texOffs(9, 105).addBox(2.2F, -3.3F, -6.2F, 2F, 3F, 2F, new CubeDeformation(-0.4F, -0.4F, -0.4F))
                .texOffs(147, 88).addBox(3.4F, 0.35F, 3.4F, 1F, 6F, 1F, new CubeDeformation(-0.2F, -0.25F, -0.2F))
                .texOffs(109, 88).addBox(6.8F, 0.6F, 3.6F, 1F, 7F, 1F, new CubeDeformation(-0.2F, 0F, -0.2F))
                .texOffs(36, 105).addBox(8.4F, 0.6F, -4.8F, 1F, 4F, 1F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offsetAndRotation(4.6F, -0.4F, 0F, 0F, -0.2793F, -0.3491F));
        PartDefinition p_antler_r = p_head.addOrReplaceChild("antler_r", CubeListBuilder.create()
                .texOffs(174, 88).addBox(-5F, -1.5F, -1.5F, 5F, 3F, 3F, new CubeDeformation(0F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(-3.2F, -4.4F, -2.4F, -0.8378F, 0F, 0.1745F));
        PartDefinition p_antler_r_palm = p_antler_r.addOrReplaceChild("antler_r_palm", CubeListBuilder.create()
                .texOffs(0, 75).addBox(-11F, -1F, -5.4F, 11F, 2F, 10F, new CubeDeformation(0F, -0.3F, 0F))
                .texOffs(106, 98).addBox(-11.4F, -4.5F, -5.8F, 2F, 4F, 2F, new CubeDeformation(-0.4F, -0.2F, -0.4F))
                .texOffs(138, 88).addBox(-11.4F, -5.4F, -3F, 2F, 5F, 2F, new CubeDeformation(-0.4F, -0.3F, -0.4F))
                .texOffs(115, 98).addBox(-11.4F, -4.7F, -0.2F, 2F, 4F, 2F, new CubeDeformation(-0.4F, 0F, -0.4F))
                .texOffs(124, 98).addBox(-11.4F, -4.4F, 2.6F, 2F, 4F, 2F, new CubeDeformation(-0.4F, -0.3F, -0.4F))
                .texOffs(18, 105).addBox(-7.2F, -3.6F, -6.2F, 2F, 3F, 2F, new CubeDeformation(-0.4F, -0.1F, -0.4F))
                .texOffs(27, 105).addBox(-4.2F, -3.3F, -6.2F, 2F, 3F, 2F, new CubeDeformation(-0.4F, -0.4F, -0.4F))
                .texOffs(152, 88).addBox(-4.4F, 0.35F, 3.4F, 1F, 6F, 1F, new CubeDeformation(-0.2F, -0.25F, -0.2F))
                .texOffs(114, 88).addBox(-7.8F, 0.6F, 3.6F, 1F, 7F, 1F, new CubeDeformation(-0.2F, 0F, -0.2F))
                .texOffs(41, 105).addBox(-9.4F, 0.6F, -4.8F, 1F, 4F, 1F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offsetAndRotation(-4.6F, -0.4F, 0F, 0F, 0.2793F, 0.3491F));
        PartDefinition p_bridle = p_head.addOrReplaceChild("bridle", CubeListBuilder.create()
                .texOffs(71, 88).addBox(-3.5F, -4.1F, -12.2F, 7F, 8F, 1F, new CubeDeformation(0.01F, -0.09F, 0.06F))
                .texOffs(143, 75).addBox(-4.5F, -5F, -4.2F, 9F, 9F, 1F, new CubeDeformation(-0.29F, -0.19F, 0.06F))
                .texOffs(33, 88).addBox(-4.1F, -1.2F, -12.2F, 1F, 1F, 8F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(52, 88).addBox(3.1F, -1.2F, -12.2F, 1F, 1F, 8F, new CubeDeformation(-0.2F, -0.2F, -0.2F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_tail = p_body.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(68, 98).addBox(-1.5F, 0F, -0.6F, 3F, 4F, 2F, new CubeDeformation(-0.1F, 0F, 0F)),
                PartPose.offsetAndRotation(0F, -3.8F, 12.6F, 0.1745F, 0F, 0F));
        PartDefinition p_saddle = p_body.addOrReplaceChild("saddle", CubeListBuilder.create()
                .texOffs(25, 59).addBox(-5.5F, -6.7F, -3.5F, 11F, 2F, 12F, new CubeDeformation(-0.3F, -0.1F, -0.3F))
                .texOffs(133, 98).addBox(-4F, -8F, -3.8F, 8F, 2F, 3F, new CubeDeformation(0F, -0.4F, -0.4F))
                .texOffs(60, 105).addBox(-4.5F, -7.4F, 6.2F, 9F, 1F, 2F, new CubeDeformation(-0.1F, 0F, -0.2F))
                .texOffs(109, 59).addBox(-7.5F, -5.2F, -1F, 1F, 6F, 7F, new CubeDeformation(-0.3F, 0F, 0F))
                .texOffs(126, 59).addBox(6.5F, -5.2F, -1F, 1F, 6F, 7F, new CubeDeformation(-0.3F, 0F, 0F))
                .texOffs(119, 88).addBox(-7.9F, 0.3F, 1.8F, 1F, 7F, 1F, new CubeDeformation(-0.3F, -0.3F, -0.2F))
                .texOffs(124, 88).addBox(6.9F, 0.3F, 1.8F, 1F, 7F, 1F, new CubeDeformation(-0.3F, -0.3F, -0.2F))
                .texOffs(83, 105).addBox(-8.4F, 6.85F, 1.3F, 2F, 1F, 2F, new CubeDeformation(-0.2F, -0.15F, -0.1F))
                .texOffs(92, 105).addBox(6.4F, 6.85F, 1.3F, 2F, 1F, 2F, new CubeDeformation(-0.2F, -0.15F, -0.1F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_chest_l = p_body.addOrReplaceChild("chest_l", CubeListBuilder.create()
                .texOffs(162, 40).addBox(6.6F, -3.6F, 4.2F, 4F, 7F, 8F, new CubeDeformation(-0.4F, 0F, -0.2F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_chest_r = p_body.addOrReplaceChild("chest_r", CubeListBuilder.create()
                .texOffs(0, 59).addBox(-10.6F, -3.6F, 4.2F, 4F, 7F, 8F, new CubeDeformation(-0.4F, 0F, -0.2F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_left_front_leg = root.addOrReplaceChild("left_front_leg", CubeListBuilder.create()
                .texOffs(116, 40).addBox(-2.5F, -2.25F, -3F, 5F, 11F, 6F, new CubeDeformation(-0.3F, -0.25F, -0.4F)),
                PartPose.offset(4.2F, 8F, -8.6F));
        PartDefinition p_left_front_shin = p_left_front_leg.addOrReplaceChild("left_front_shin", CubeListBuilder.create()
                .texOffs(76, 75).addBox(-1.5F, -0.4F, -2F, 3F, 8F, 4F, new CubeDeformation(0F, -0.4F, -0.4F))
                .texOffs(0, 98).addBox(-2F, 5.75F, -2.2F, 4F, 2F, 4F, new CubeDeformation(-0.2F, -0.25F, 0F)),
                PartPose.offset(0F, 8.5F, -0.2F));
        PartDefinition p_left_hind_leg = root.addOrReplaceChild("left_hind_leg", CubeListBuilder.create()
                .texOffs(0, 40).addBox(-2.5F, -2F, -3.5F, 5F, 11F, 7F, new CubeDeformation(0F, 0F, -0.3F)),
                PartPose.offset(4.4F, 7.4F, 10F));
        PartDefinition p_left_hind_shin = p_left_hind_leg.addOrReplaceChild("left_hind_shin", CubeListBuilder.create()
                .texOffs(106, 75).addBox(-1.5F, -0.4F, -1.5F, 3F, 8F, 3F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(17, 98).addBox(-2F, 6.25F, -2.2F, 4F, 2F, 4F, new CubeDeformation(-0.2F, -0.25F, 0F)),
                PartPose.offset(0F, 9F, 1.4F));
        PartDefinition p_right_front_leg = root.addOrReplaceChild("right_front_leg", CubeListBuilder.create()
                .texOffs(139, 40).addBox(-2.5F, -2.25F, -3F, 5F, 11F, 6F, new CubeDeformation(-0.3F, -0.25F, -0.4F)),
                PartPose.offset(-4.2F, 8F, -8.6F));
        PartDefinition p_right_front_shin = p_right_front_leg.addOrReplaceChild("right_front_shin", CubeListBuilder.create()
                .texOffs(91, 75).addBox(-1.5F, -0.4F, -2F, 3F, 8F, 4F, new CubeDeformation(0F, -0.4F, -0.4F))
                .texOffs(34, 98).addBox(-2F, 5.75F, -2.2F, 4F, 2F, 4F, new CubeDeformation(-0.2F, -0.25F, 0F)),
                PartPose.offset(0F, 8.5F, -0.2F));
        PartDefinition p_right_hind_leg = root.addOrReplaceChild("right_hind_leg", CubeListBuilder.create()
                .texOffs(25, 40).addBox(-2.5F, -2F, -3.5F, 5F, 11F, 7F, new CubeDeformation(0F, 0F, -0.3F)),
                PartPose.offset(-4.4F, 7.4F, 10F));
        PartDefinition p_right_hind_shin = p_right_hind_leg.addOrReplaceChild("right_hind_shin", CubeListBuilder.create()
                .texOffs(119, 75).addBox(-1.5F, -0.4F, -1.5F, 3F, 8F, 3F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(51, 98).addBox(-2F, 6.25F, -2.2F, 4F, 2F, 4F, new CubeDeformation(-0.2F, -0.25F, 0F)),
                PartPose.offset(0F, 9F, 1.4F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
