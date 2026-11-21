package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Sternenfressers (Knochen, Drehpunkte und Abstände wie beim Vanilla-Enderdrachen, dessen Animation Hals, Schwanz und Flügel bewegt; dazu Hörner, Kamm, Kristalldornen, Krallen und Schwanzklinge).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class SternenfresserGeometry {
    public static final int TEXTURE_WIDTH = 512;
    public static final int TEXTURE_HEIGHT = 512;

    private SternenfresserGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(33, 203).addBox(-7.5F, -8F, -10F, 15F, 15F, 16F)
                .texOffs(396, 203).addBox(-5.5F, -2F, -25F, 11F, 6F, 15F)
                .texOffs(118, 298).addBox(-4.5F, -3.45F, -23.5F, 2F, 2F, 3F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(129, 298).addBox(2.5F, -3.45F, -23.5F, 2F, 2F, 3F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(250, 276).addBox(-7.8F, -9.2F, -11F, 5F, 2F, 7F)
                .texOffs(275, 276).addBox(2.8F, -9.2F, -11F, 5F, 2F, 7F)
                .texOffs(242, 289).addBox(-9.2F, -1F, -6F, 2F, 2F, 6F)
                .texOffs(140, 298).addBox(-5.7F, 3.8F, -23F, 1F, 2F, 1F, new CubeDeformation(0F, -0.2F, 0F))
                .texOffs(145, 298).addBox(-5.7F, 3.8F, -19F, 1F, 2F, 1F, new CubeDeformation(0F, -0.2F, 0F))
                .texOffs(150, 298).addBox(-5.7F, 3.8F, -15F, 1F, 2F, 1F, new CubeDeformation(0F, -0.2F, 0F))
                .texOffs(259, 289).addBox(7.2F, -1F, -6F, 2F, 2F, 6F)
                .texOffs(155, 298).addBox(4.7F, 3.8F, -23F, 1F, 2F, 1F, new CubeDeformation(0F, -0.2F, 0F))
                .texOffs(160, 298).addBox(4.7F, 3.8F, -19F, 1F, 2F, 1F, new CubeDeformation(0F, -0.2F, 0F))
                .texOffs(165, 298).addBox(4.7F, 3.8F, -15F, 1F, 2F, 1F, new CubeDeformation(0F, -0.2F, 0F))
                .texOffs(78, 298).addBox(-1F, -11.6F, -6.3F, 2F, 4F, 3F, new CubeDeformation(-0.25F, -0.4F, -0.3F))
                .texOffs(487, 289).addBox(-1F, -12.6F, -2.3F, 2F, 5F, 3F, new CubeDeformation(-0.25F, -0.4F, -0.3F))
                .texOffs(89, 298).addBox(-1F, -11.7F, 1.7F, 2F, 4F, 3F, new CubeDeformation(-0.25F, -0.3F, -0.3F)),
                PartPose.offset(0F, 20F, -62F));
        PartDefinition p_right_horn = p_head.addOrReplaceChild("right_horn", CubeListBuilder.create()
                .texOffs(74, 257).addBox(-2F, -2F, 0F, 4F, 4F, 13F, new CubeDeformation(-0.4F, -0.4F, 0F)),
                PartPose.offsetAndRotation(-5F, -7.5F, -1F, 0.5585F, -0.2443F, 0F));
        PartDefinition p_right_horn_tip = p_right_horn.addOrReplaceChild("right_horn_tip", CubeListBuilder.create()
                .texOffs(395, 257).addBox(-1.5F, -1.5F, 0F, 3F, 3F, 10F, new CubeDeformation(-0.4F, -0.4F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 13F, 0.4189F, -0.1047F, 0F));
        PartDefinition p_left_horn = p_head.addOrReplaceChild("left_horn", CubeListBuilder.create()
                .texOffs(109, 257).addBox(-2F, -2F, 0F, 4F, 4F, 13F, new CubeDeformation(-0.4F, -0.4F, 0F)),
                PartPose.offsetAndRotation(5F, -7.5F, -1F, 0.5585F, 0.2443F, 0F));
        PartDefinition p_left_horn_tip = p_left_horn.addOrReplaceChild("left_horn_tip", CubeListBuilder.create()
                .texOffs(422, 257).addBox(-1.5F, -1.5F, 0F, 3F, 3F, 10F, new CubeDeformation(-0.4F, -0.4F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 13F, 0.4189F, 0.1047F, 0F));
        PartDefinition p_jaw = p_head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(339, 203).addBox(-5.5F, 0F, -17F, 11F, 4F, 17F)
                .texOffs(170, 298).addBox(-4.9F, -1.75F, -16F, 1F, 2F, 1F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(175, 298).addBox(-4.9F, -1.75F, -12F, 1F, 2F, 1F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(180, 298).addBox(-4.9F, -1.75F, -8F, 1F, 2F, 1F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(185, 298).addBox(3.9F, -1.75F, -16F, 1F, 2F, 1F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(190, 298).addBox(3.9F, -1.75F, -12F, 1F, 2F, 1F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(195, 298).addBox(3.9F, -1.75F, -8F, 1F, 2F, 1F, new CubeDeformation(0F, -0.25F, 0F)),
                PartPose.offset(0F, 4F, -8F));
        PartDefinition p_neck0 = root.addOrReplaceChild("neck0", CubeListBuilder.create()
                .texOffs(172, 236).addBox(-4.5F, -4.5F, -5F, 9F, 9F, 10F)
                .texOffs(300, 276).addBox(-1F, -8.5F, -2.5F, 2F, 4F, 5F, new CubeDeformation(-0.25F, 0F, 0F)),
                PartPose.offset(0F, 20F, -12F));
        PartDefinition p_neck1 = root.addOrReplaceChild("neck1", CubeListBuilder.create()
                .texOffs(211, 236).addBox(-4.5F, -4.5F, -5F, 9F, 9F, 10F)
                .texOffs(315, 276).addBox(-1F, -8.4F, -2.5F, 2F, 4F, 5F, new CubeDeformation(-0.25F, 0F, 0F)),
                PartPose.offset(0F, 20F, -22F));
        PartDefinition p_neck2 = root.addOrReplaceChild("neck2", CubeListBuilder.create()
                .texOffs(250, 236).addBox(-4.5F, -4.5F, -5F, 9F, 9F, 10F)
                .texOffs(330, 276).addBox(-1F, -8.3F, -2.5F, 2F, 4F, 5F, new CubeDeformation(-0.25F, 0F, 0F)),
                PartPose.offset(0F, 20F, -32F));
        PartDefinition p_neck3 = root.addOrReplaceChild("neck3", CubeListBuilder.create()
                .texOffs(289, 236).addBox(-4.5F, -4.5F, -5F, 9F, 9F, 10F)
                .texOffs(345, 276).addBox(-1F, -8.2F, -2.5F, 2F, 4F, 5F, new CubeDeformation(-0.25F, 0F, 0F)),
                PartPose.offset(0F, 20F, -42F));
        PartDefinition p_neck4 = root.addOrReplaceChild("neck4", CubeListBuilder.create()
                .texOffs(328, 236).addBox(-4.5F, -4.5F, -5F, 9F, 9F, 10F)
                .texOffs(360, 276).addBox(-1F, -8.1F, -2.5F, 2F, 4F, 5F, new CubeDeformation(-0.25F, 0F, 0F)),
                PartPose.offset(0F, 20F, -52F));
        PartDefinition p_tail0 = root.addOrReplaceChild("tail0", CubeListBuilder.create()
                .texOffs(49, 236).addBox(-5F, -5F, -5F, 10F, 10F, 10F)
                .texOffs(375, 276).addBox(-1F, -9F, -2.5F, 2F, 4F, 5F, new CubeDeformation(-0.3F, 0F, 0F)),
                PartPose.offset(0F, 10F, 60F));
        PartDefinition p_tail1 = root.addOrReplaceChild("tail1", CubeListBuilder.create()
                .texOffs(90, 236).addBox(-5F, -5F, -5F, 10F, 10F, 10F, new CubeDeformation(-0.21F, -0.21F, 0F))
                .texOffs(390, 276).addBox(-1F, -8.68F, -2.5F, 2F, 4F, 5F, new CubeDeformation(-0.3F, -0.11F, 0F)),
                PartPose.offset(0F, 10F, 70F));
        PartDefinition p_tail2 = root.addOrReplaceChild("tail2", CubeListBuilder.create()
                .texOffs(131, 236).addBox(-5F, -5F, -5F, 10F, 10F, 10F, new CubeDeformation(-0.42F, -0.42F, 0F))
                .texOffs(405, 276).addBox(-1F, -8.36F, -2.5F, 2F, 4F, 5F, new CubeDeformation(-0.3F, -0.22F, 0F)),
                PartPose.offset(0F, 10F, 80F));
        PartDefinition p_tail3 = root.addOrReplaceChild("tail3", CubeListBuilder.create()
                .texOffs(367, 236).addBox(-4.5F, -4.5F, -5F, 9F, 9F, 10F, new CubeDeformation(-0.13F, -0.13F, 0F))
                .texOffs(420, 276).addBox(-1F, -8.04F, -2.5F, 2F, 4F, 5F, new CubeDeformation(-0.3F, -0.33F, 0F)),
                PartPose.offset(0F, 10F, 90F));
        PartDefinition p_tail4 = root.addOrReplaceChild("tail4", CubeListBuilder.create()
                .texOffs(406, 236).addBox(-4.5F, -4.5F, -5F, 9F, 9F, 10F, new CubeDeformation(-0.34F, -0.34F, 0F))
                .texOffs(435, 276).addBox(-1F, -7.72F, -2.5F, 2F, 4F, 5F, new CubeDeformation(-0.3F, -0.44F, 0F)),
                PartPose.offset(0F, 10F, 100F));
        PartDefinition p_tail5 = root.addOrReplaceChild("tail5", CubeListBuilder.create()
                .texOffs(445, 236).addBox(-4F, -4F, -5F, 8F, 8F, 10F, new CubeDeformation(-0.05F, -0.05F, 0F))
                .texOffs(412, 289).addBox(-1F, -6.9F, -2.5F, 2F, 3F, 5F, new CubeDeformation(-0.3F, -0.05F, 0F)),
                PartPose.offset(0F, 10F, 110F));
        PartDefinition p_tail6 = root.addOrReplaceChild("tail6", CubeListBuilder.create()
                .texOffs(0, 257).addBox(-4F, -4F, -5F, 8F, 8F, 10F, new CubeDeformation(-0.26F, -0.26F, 0F))
                .texOffs(427, 289).addBox(-1F, -6.58F, -2.5F, 2F, 3F, 5F, new CubeDeformation(-0.3F, -0.16F, 0F)),
                PartPose.offset(0F, 10F, 120F));
        PartDefinition p_tail7 = root.addOrReplaceChild("tail7", CubeListBuilder.create()
                .texOffs(37, 257).addBox(-4F, -4F, -5F, 8F, 8F, 10F, new CubeDeformation(-0.47F, -0.47F, 0F))
                .texOffs(442, 289).addBox(-1F, -6.26F, -2.5F, 2F, 3F, 5F, new CubeDeformation(-0.3F, -0.27F, 0F)),
                PartPose.offset(0F, 10F, 130F));
        PartDefinition p_tail8 = root.addOrReplaceChild("tail8", CubeListBuilder.create()
                .texOffs(144, 257).addBox(-3.5F, -3.5F, -5F, 7F, 7F, 10F, new CubeDeformation(-0.18F, -0.18F, 0F))
                .texOffs(457, 289).addBox(-1F, -5.94F, -2.5F, 2F, 3F, 5F, new CubeDeformation(-0.3F, -0.38F, 0F)),
                PartPose.offset(0F, 10F, 140F));
        PartDefinition p_tail9 = root.addOrReplaceChild("tail9", CubeListBuilder.create()
                .texOffs(179, 257).addBox(-3.5F, -3.5F, -5F, 7F, 7F, 10F, new CubeDeformation(-0.39F, -0.39F, 0F))
                .texOffs(472, 289).addBox(-1F, -5.62F, -2.5F, 2F, 3F, 5F, new CubeDeformation(-0.3F, -0.49F, 0F)),
                PartPose.offset(0F, 10F, 150F));
        PartDefinition p_tail10 = root.addOrReplaceChild("tail10", CubeListBuilder.create()
                .texOffs(237, 257).addBox(-3F, -3F, -5F, 6F, 6F, 10F, new CubeDeformation(-0.1F, -0.1F, 0F))
                .texOffs(498, 289).addBox(-1F, -4.8F, -2.5F, 2F, 2F, 5F, new CubeDeformation(-0.3F, -0.1F, 0F)),
                PartPose.offset(0F, 10F, 160F));
        PartDefinition p_tail11 = root.addOrReplaceChild("tail11", CubeListBuilder.create()
                .texOffs(270, 257).addBox(-3F, -3F, -5F, 6F, 6F, 10F, new CubeDeformation(-0.31F, -0.31F, 0F))
                .texOffs(316, 203).addBox(-1F, -9F, -4.5F, 2F, 18F, 9F, new CubeDeformation(-0.4F, 0F, 0F)),
                PartPose.offset(0F, 10F, 170F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-12F, -16F, -16F, 24F, 24F, 64F)
                .texOffs(349, 257).addBox(-1.5F, -22F, -12F, 3F, 6F, 8F, new CubeDeformation(-0.3F, 0F, 0F))
                .texOffs(303, 257).addBox(-1.5F, -24F, 0F, 3F, 8F, 8F, new CubeDeformation(-0.3F, 0F, 0F))
                .texOffs(214, 257).addBox(-1.5F, -25F, 12F, 3F, 9F, 8F, new CubeDeformation(-0.3F, 0F, 0F))
                .texOffs(326, 257).addBox(-1.5F, -24F, 24F, 3F, 8F, 8F, new CubeDeformation(-0.3F, 0F, 0F))
                .texOffs(372, 257).addBox(-1.5F, -22F, 36F, 3F, 6F, 8F, new CubeDeformation(-0.3F, 0F, 0F)),
                PartPose.offset(0F, 20F, 8F));
        PartDefinition p_left_wing = p_body.addOrReplaceChild("left_wing", CubeListBuilder.create()
                .texOffs(0, 276).addBox(0F, -3F, -3F, 56F, 6F, 6F)
                .texOffs(177, 0).addBox(0F, 0F, 2F, 56F, 0F, 56F)
                .texOffs(100, 298).addBox(54F, -7F, -2.5F, 2F, 4F, 2F),
                PartPose.offset(12F, -15F, -6F));
        PartDefinition p_left_wing_tip = p_left_wing.addOrReplaceChild("left_wing_tip", CubeListBuilder.create()
                .texOffs(0, 289).addBox(0F, -2F, -2F, 56F, 4F, 4F)
                .texOffs(0, 89).addBox(0F, 0F, 2F, 56F, 0F, 56F),
                PartPose.offset(56F, 0F, 0F));
        PartDefinition p_right_wing = p_body.addOrReplaceChild("right_wing", CubeListBuilder.create()
                .texOffs(125, 276).addBox(-56F, -3F, -3F, 56F, 6F, 6F)
                .texOffs(225, 89).addBox(-56F, 0F, 2F, 56F, 0F, 56F)
                .texOffs(109, 298).addBox(-56F, -7F, -2.5F, 2F, 4F, 2F),
                PartPose.offset(-12F, -15F, -6F));
        PartDefinition p_right_wing_tip = p_right_wing.addOrReplaceChild("right_wing_tip", CubeListBuilder.create()
                .texOffs(121, 289).addBox(-56F, -2F, -2F, 56F, 4F, 4F)
                .texOffs(0, 146).addBox(-56F, 0F, 2F, 56F, 0F, 56F),
                PartPose.offset(-56F, 0F, 0F));
        PartDefinition p_left_front_leg = p_body.addOrReplaceChild("left_front_leg", CubeListBuilder.create()
                .texOffs(453, 146).addBox(-4F, -4F, -4F, 8F, 24F, 8F),
                PartPose.offsetAndRotation(12F, 0F, -6F, 1.3F, 0F, 0F));
        PartDefinition p_left_front_leg_tip = p_left_front_leg.addOrReplaceChild("left_front_leg_tip", CubeListBuilder.create()
                .texOffs(266, 203).addBox(-3F, -1F, -3F, 6F, 24F, 6F),
                PartPose.offsetAndRotation(0F, 20F, -1F, -0.5F, 0F, 0F));
        PartDefinition p_left_front_foot = p_left_front_leg_tip.addOrReplaceChild("left_front_foot", CubeListBuilder.create()
                .texOffs(449, 203).addBox(-4F, 0F, -12F, 8F, 4F, 16F)
                .texOffs(0, 298).addBox(-3.75F, 1.25F, -15.25F, 2F, 3F, 4F, new CubeDeformation(-0.25F, -0.25F, -0.25F))
                .texOffs(13, 298).addBox(-1F, 1.25F, -15.25F, 2F, 3F, 4F, new CubeDeformation(-0.25F, -0.25F, -0.25F))
                .texOffs(26, 298).addBox(1.75F, 1.25F, -15.25F, 2F, 3F, 4F, new CubeDeformation(-0.25F, -0.25F, -0.25F)),
                PartPose.offsetAndRotation(0F, 23F, 0F, 0.75F, 0F, 0F));
        PartDefinition p_left_hind_leg = p_body.addOrReplaceChild("left_hind_leg", CubeListBuilder.create()
                .texOffs(225, 146).addBox(-8F, -4F, -8F, 16F, 32F, 16F),
                PartPose.offsetAndRotation(16F, -4F, 34F, 1F, 0F, 0F));
        PartDefinition p_left_hind_leg_tip = p_left_hind_leg.addOrReplaceChild("left_hind_leg_tip", CubeListBuilder.create()
                .texOffs(355, 146).addBox(-6F, -2F, 0F, 12F, 32F, 12F),
                PartPose.offsetAndRotation(0F, 32F, -4F, 0.5F, 0F, 0F));
        PartDefinition p_left_hind_foot = p_left_hind_leg_tip.addOrReplaceChild("left_hind_foot", CubeListBuilder.create()
                .texOffs(96, 203).addBox(-9F, 0F, -20F, 18F, 6F, 24F)
                .texOffs(276, 289).addBox(-8.25F, 3F, -24.25F, 3F, 3F, 5F, new CubeDeformation(-0.25F, 0F, -0.25F))
                .texOffs(293, 289).addBox(-3.5F, 3F, -24.25F, 3F, 3F, 5F, new CubeDeformation(-0.25F, 0F, -0.25F))
                .texOffs(310, 289).addBox(1.25F, 3F, -24.25F, 3F, 3F, 5F, new CubeDeformation(-0.25F, 0F, -0.25F))
                .texOffs(327, 289).addBox(5.75F, 3F, -24.25F, 3F, 3F, 5F, new CubeDeformation(-0.25F, 0F, -0.25F)),
                PartPose.offsetAndRotation(0F, 31F, 4F, 0.75F, 0F, 0F));
        PartDefinition p_right_front_leg = p_body.addOrReplaceChild("right_front_leg", CubeListBuilder.create()
                .texOffs(0, 203).addBox(-4F, -4F, -4F, 8F, 24F, 8F),
                PartPose.offsetAndRotation(-12F, 0F, -6F, 1.3F, 0F, 0F));
        PartDefinition p_right_front_leg_tip = p_right_front_leg.addOrReplaceChild("right_front_leg_tip", CubeListBuilder.create()
                .texOffs(291, 203).addBox(-3F, -1F, -3F, 6F, 24F, 6F),
                PartPose.offsetAndRotation(0F, 20F, -1F, -0.5F, 0F, 0F));
        PartDefinition p_right_front_foot = p_right_front_leg_tip.addOrReplaceChild("right_front_foot", CubeListBuilder.create()
                .texOffs(0, 236).addBox(-4F, 0F, -12F, 8F, 4F, 16F)
                .texOffs(39, 298).addBox(-3.75F, 1.25F, -15.25F, 2F, 3F, 4F, new CubeDeformation(-0.25F, -0.25F, -0.25F))
                .texOffs(52, 298).addBox(-1F, 1.25F, -15.25F, 2F, 3F, 4F, new CubeDeformation(-0.25F, -0.25F, -0.25F))
                .texOffs(65, 298).addBox(1.75F, 1.25F, -15.25F, 2F, 3F, 4F, new CubeDeformation(-0.25F, -0.25F, -0.25F)),
                PartPose.offsetAndRotation(0F, 23F, 0F, 0.75F, 0F, 0F));
        PartDefinition p_right_hind_leg = p_body.addOrReplaceChild("right_hind_leg", CubeListBuilder.create()
                .texOffs(290, 146).addBox(-8F, -4F, -8F, 16F, 32F, 16F),
                PartPose.offsetAndRotation(-16F, -4F, 34F, 1F, 0F, 0F));
        PartDefinition p_right_hind_leg_tip = p_right_hind_leg.addOrReplaceChild("right_hind_leg_tip", CubeListBuilder.create()
                .texOffs(404, 146).addBox(-6F, -2F, 0F, 12F, 32F, 12F),
                PartPose.offsetAndRotation(0F, 32F, -4F, 0.5F, 0F, 0F));
        PartDefinition p_right_hind_foot = p_right_hind_leg_tip.addOrReplaceChild("right_hind_foot", CubeListBuilder.create()
                .texOffs(181, 203).addBox(-9F, 0F, -20F, 18F, 6F, 24F)
                .texOffs(344, 289).addBox(-8.25F, 3F, -24.25F, 3F, 3F, 5F, new CubeDeformation(-0.25F, 0F, -0.25F))
                .texOffs(361, 289).addBox(-3.5F, 3F, -24.25F, 3F, 3F, 5F, new CubeDeformation(-0.25F, 0F, -0.25F))
                .texOffs(378, 289).addBox(1.25F, 3F, -24.25F, 3F, 3F, 5F, new CubeDeformation(-0.25F, 0F, -0.25F))
                .texOffs(395, 289).addBox(5.75F, 3F, -24.25F, 3F, 3F, 5F, new CubeDeformation(-0.25F, 0F, -0.25F)),
                PartPose.offsetAndRotation(0F, 31F, 4F, 0.75F, 0F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
