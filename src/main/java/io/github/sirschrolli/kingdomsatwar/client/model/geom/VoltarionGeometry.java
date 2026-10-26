package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie von Voltarion, dem Blitzdrachen-Boss (Grundgröße, im Spiel ×2,1).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class VoltarionGeometry {
    public static final int TEXTURE_WIDTH = 512;
    public static final int TEXTURE_HEIGHT = 384;

    private VoltarionGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(91, 0).addBox(-9.5F, -6F, -18F, 19F, 11F, 34F)
                .texOffs(0, 0).addBox(-5.5F, -10F, -17.92F, 11F, 19F, 33.84F)
                .texOffs(424, 125).addBox(-10.5F, -6.5F, -19F, 21F, 12F, 14F)
                .texOffs(169, 54).addBox(-6F, -11F, -18.92F, 12F, 21F, 13.84F)
                .texOffs(405, 154).addBox(-10F, -6F, 3F, 20F, 10F, 12F)
                .texOffs(389, 92).addBox(-6F, -10F, 3.08F, 12F, 18F, 11.84F)
                .texOffs(438, 92).addBox(-4.5F, -11.7F, -15F, 9F, 0.9F, 27F)
                .texOffs(250, 92).addBox(-3.2F, -13F, -14.92F, 6.4F, 3.5F, 26.84F)
                .texOffs(422, 200).addBox(8F, -9F, -17F, 5F, 5F, 10F)
                .texOffs(279, 179).addBox(10F, -11F, -16.92F, 1F, 9F, 9.84F)
                .texOffs(453, 200).addBox(-13F, -9F, -17F, 5F, 5F, 10F)
                .texOffs(302, 179).addBox(-11F, -11F, -16.92F, 1F, 9F, 9.84F)
                .texOffs(0, 179).addBox(-8F, 8.5F, -18F, 16F, 2.2F, 17F)
                .texOffs(453, 179).addBox(-7F, 7.2F, -1F, 14F, 2F, 15F)
                .texOffs(360, 234).addBox(-0.4F, -18.6F, -13F, 0.8F, 6.5F, 4.5F)
                .texOffs(46, 234).addBox(-0.4F, -20.1F, -7F, 0.8F, 8F, 4.5F)
                .texOffs(319, 218).addBox(-0.4F, -20.6F, -1F, 0.8F, 8.5F, 4.5F)
                .texOffs(373, 234).addBox(-0.4F, -19.1F, 5F, 0.8F, 7F, 4.5F)
                .texOffs(241, 248).addBox(-0.4F, -17.6F, 11F, 0.8F, 5.5F, 4.5F),
                PartPose.offset(0F, -10F, 0F));
        PartDefinition p_neck1 = p_body.addOrReplaceChild("neck1", CubeListBuilder.create()
                .texOffs(97, 218).addBox(-5.5F, -2.2F, -7.6F, 11F, 4.4F, 8.2F)
                .texOffs(174, 179).addBox(-2.2F, -5.5F, -7.52F, 4.4F, 11F, 8.04F)
                .texOffs(135, 260).addBox(-2.2F, 4.5F, -7F, 4.4F, 1.6F, 7F)
                .texOffs(181, 248).addBox(-0.35F, -9.6F, -6.5F, 0.7F, 5F, 6F),
                PartPose.offsetAndRotation(0F, -6F, -17F, -0.5236F, 0F, 0F));
        PartDefinition p_neck2 = p_neck1.addOrReplaceChild("neck2", CubeListBuilder.create()
                .texOffs(59, 234).addBox(-5F, -2F, -7.4F, 10F, 4F, 8F)
                .texOffs(403, 179).addBox(-2F, -5F, -7.32F, 4F, 10F, 7.84F)
                .texOffs(185, 260).addBox(-2F, 4F, -6.8F, 4F, 1.6F, 6.8F)
                .texOffs(196, 248).addBox(-0.35F, -8.65F, -6.3F, 0.7F, 4.55F, 5.8F),
                PartPose.offsetAndRotation(0F, 0F, -7F, -0.2443F, 0F, 0F));
        PartDefinition p_neck3 = p_neck2.addOrReplaceChild("neck3", CubeListBuilder.create()
                .texOffs(96, 234).addBox(-4.6F, -1.84F, -7.1F, 9.2F, 3.68F, 7.7F)
                .texOffs(428, 179).addBox(-1.84F, -4.6F, -7.02F, 3.68F, 9.2F, 7.54F)
                .texOffs(208, 260).addBox(-1.84F, 3.6F, -6.5F, 3.68F, 1.6F, 6.5F)
                .texOffs(211, 248).addBox(-0.35F, -7.8F, -6F, 0.7F, 4.1F, 5.5F),
                PartPose.offsetAndRotation(0F, 0F, -6.8F, -0.0698F, 0F, 0F));
        PartDefinition p_neck4 = p_neck3.addOrReplaceChild("neck4", CubeListBuilder.create()
                .texOffs(133, 234).addBox(-4.3F, -1.72F, -6.8F, 8.6F, 3.44F, 7.4F)
                .texOffs(76, 200).addBox(-1.72F, -4.3F, -6.72F, 3.44F, 8.6F, 7.24F)
                .texOffs(231, 260).addBox(-1.72F, 3.3F, -6.2F, 3.44F, 1.6F, 6.2F)
                .texOffs(396, 248).addBox(-0.35F, -7.05F, -5.7F, 0.7F, 3.65F, 5.2F),
                PartPose.offsetAndRotation(0F, 0F, -6.5F, 0.0698F, 0F, 0F));
        PartDefinition p_neck5 = p_neck4.addOrReplaceChild("neck5", CubeListBuilder.create()
                .texOffs(238, 234).addBox(-4F, -1.6F, -6.6F, 8F, 3.2F, 7.2F)
                .texOffs(200, 200).addBox(-1.6F, -4F, -6.52F, 3.2F, 8F, 7.04F)
                .texOffs(395, 260).addBox(-1.6F, 3F, -6F, 3.2F, 1.6F, 6F)
                .texOffs(357, 260).addBox(-0.35F, -6.3F, -5.5F, 0.7F, 3.2F, 5F),
                PartPose.offsetAndRotation(0F, 0F, -6.2F, 0.1745F, 0F, 0F));
        PartDefinition p_neck6 = p_neck5.addOrReplaceChild("neck6", CubeListBuilder.create()
                .texOffs(254, 248).addBox(-3.7F, -1.48F, -6.4F, 7.4F, 2.96F, 7F)
                .texOffs(27, 218).addBox(-1.48F, -3.7F, -6.32F, 2.96F, 7.4F, 6.84F)
                .texOffs(458, 260).addBox(-1.48F, 2.7F, -5.8F, 2.96F, 1.6F, 5.8F)
                .texOffs(17, 270).addBox(-0.35F, -5.55F, -5.3F, 0.7F, 2.75F, 4.8F),
                PartPose.offsetAndRotation(0F, 0F, -6F, 0.1745F, 0F, 0F));
        PartDefinition p_head = p_neck6.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(325, 179).addBox(-5.5F, -2.8F, -12.5F, 11F, 5F, 13F)
                .texOffs(322, 154).addBox(-3.3F, -5F, -12.42F, 6.6F, 9.4F, 12.84F)
                .texOffs(403, 279).addBox(-4.4F, -3.8F, -15F, 8.8F, 0.6F, 5F)
                .texOffs(370, 260).addBox(-3.4F, -4.8F, -14.92F, 6.8F, 2.6F, 4.84F)
                .texOffs(168, 234).addBox(-4.2F, -1.9F, -19.5F, 8.4F, 3.8F, 8F)
                .texOffs(146, 200).addBox(-2.5F, -3.6F, -19.42F, 5F, 7.2F, 7.84F)
                .texOffs(271, 234).addBox(-3.6F, -1.7F, -26.5F, 7.2F, 3.9F, 7.5F)
                .texOffs(484, 200).addBox(-2.2F, -3.1F, -26.42F, 4.4F, 6.7F, 7.34F)
                .texOffs(387, 200).addBox(-2F, -4.4F, -25F, 4F, 1.6F, 12.5F)
                .texOffs(255, 286).addBox(-2.8F, -2.6F, -27.3F, 5.6F, 3.6F, 1F)
                .texOffs(189, 298).addBox(1F, -2.3F, -27.5F, 1.2F, 0.8F, 0.5F)
                .texOffs(432, 279).addBox(3.9F, -3.1F, -12.9F, 1.7F, 1.7F, 3.6F)
                .texOffs(24, 298).addBox(2.9F, 3.6F, -25.5429F, 0.8F, 1.755F, 0.8F)
                .texOffs(29, 298).addBox(2.9F, 3.6F, -23.8286F, 0.8F, 1.3F, 0.8F)
                .texOffs(34, 298).addBox(2.9F, 3.6F, -22.1143F, 0.8F, 1.3F, 0.8F)
                .texOffs(39, 298).addBox(2.9F, 3.6F, -20.4F, 0.8F, 1.3F, 0.8F)
                .texOffs(44, 298).addBox(2.9F, 3.6F, -18.6857F, 0.8F, 1.3F, 0.8F)
                .texOffs(49, 298).addBox(2.9F, 3.6F, -16.9714F, 0.8F, 1.3F, 0.8F)
                .texOffs(54, 298).addBox(2.9F, 3.6F, -15.2571F, 0.8F, 1.755F, 0.8F)
                .texOffs(0, 298).addBox(2.5F, 3.6F, -24.6F, 1F, 2.5F, 1F)
                .texOffs(196, 298).addBox(-2.2F, -2.3F, -27.5F, 1.2F, 0.8F, 0.5F)
                .texOffs(445, 279).addBox(-5.6F, -3.1F, -12.9F, 1.7F, 1.7F, 3.6F)
                .texOffs(59, 298).addBox(-3.7F, 3.6F, -25.5429F, 0.8F, 1.755F, 0.8F)
                .texOffs(64, 298).addBox(-3.7F, 3.6F, -23.8286F, 0.8F, 1.3F, 0.8F)
                .texOffs(69, 298).addBox(-3.7F, 3.6F, -22.1143F, 0.8F, 1.3F, 0.8F)
                .texOffs(74, 298).addBox(-3.7F, 3.6F, -20.4F, 0.8F, 1.3F, 0.8F)
                .texOffs(79, 298).addBox(-3.7F, 3.6F, -18.6857F, 0.8F, 1.3F, 0.8F)
                .texOffs(84, 298).addBox(-3.7F, 3.6F, -16.9714F, 0.8F, 1.3F, 0.8F)
                .texOffs(89, 298).addBox(-3.7F, 3.6F, -15.2571F, 0.8F, 1.755F, 0.8F)
                .texOffs(5, 298).addBox(-3.5F, 3.6F, -24.6F, 1F, 2.5F, 1F)
                .texOffs(203, 298).addBox(-1.95F, 3.6F, -26.9F, 0.7F, 1F, 0.7F)
                .texOffs(208, 298).addBox(-0.9F, 3.6F, -26.9F, 0.7F, 1F, 0.7F)
                .texOffs(213, 298).addBox(0.2F, 3.6F, -26.9F, 0.7F, 1F, 0.7F)
                .texOffs(218, 298).addBox(1.25F, 3.6F, -26.9F, 0.7F, 1F, 0.7F)
                .texOffs(332, 218).addBox(-3F, 3.62F, -25.8F, 6F, 0.3F, 12F),
                PartPose.offsetAndRotation(0F, 0F, -5.8F, 0.5236F, 0F, 0F));
        PartDefinition p_brow_l = p_head.addOrReplaceChild("brow_l", CubeListBuilder.create()
                .texOffs(0, 248).addBox(-2.1F, -1.2F, -0.6F, 4.2F, 2.2F, 8F),
                PartPose.offsetAndRotation(3.8F, -4.4F, -12.6F, 0.1396F, 0F, -0.2793F));
        PartDefinition p_horn_l01 = p_head.addOrReplaceChild("horn_l01", CubeListBuilder.create()
                .texOffs(98, 270).addBox(-1.35F, -1.35F, -0.4F, 2.7F, 2.7F, 3.975F)
                .texOffs(458, 279).addBox(-0.837F, -0.837F, 3.25F, 1.674F, 1.674F, 3.5F),
                PartPose.offsetAndRotation(2.8F, -4.8F, -6F, 0.4189F, 0.2094F, 0F));
        PartDefinition p_horn_l02 = p_horn_l01.addOrReplaceChild("horn_l02", CubeListBuilder.create()
                .texOffs(113, 270).addBox(-1.05F, -1.05F, -0.4F, 2.1F, 2.1F, 3.7F)
                .texOffs(471, 279).addBox(-0.651F, -0.651F, 3F, 1.302F, 1.302F, 3.25F),
                PartPose.offsetAndRotation(0F, 0F, 6.5F, -0.1396F, 0.0698F, 0F));
        PartDefinition p_horn_l03 = p_horn_l02.addOrReplaceChild("horn_l03", CubeListBuilder.create()
                .texOffs(484, 279).addBox(-0.75F, -0.75F, -0.4F, 1.5F, 1.5F, 3.15F)
                .texOffs(405, 293).addBox(-0.465F, -0.465F, 2.5F, 0.93F, 0.93F, 2.75F),
                PartPose.offsetAndRotation(0F, 0F, 6F, -0.1745F, 0.0349F, 0F));
        PartDefinition p_horn_l04 = p_horn_l03.addOrReplaceChild("horn_l04", CubeListBuilder.create()
                .texOffs(414, 293).addBox(-0.5F, -0.5F, -0.4F, 1F, 1F, 2.6F)
                .texOffs(423, 293).addBox(-0.31F, -0.31F, 2F, 0.62F, 0.62F, 2.25F),
                PartPose.offsetAndRotation(0F, 0F, 5F, -0.1047F, 0F, 0F));
        PartDefinition p_horn_l11 = p_head.addOrReplaceChild("horn_l11", CubeListBuilder.create()
                .texOffs(128, 270).addBox(-1.05F, -1.05F, -0.4F, 2.1F, 2.1F, 3.15F)
                .texOffs(270, 286).addBox(-0.651F, -0.651F, 2.5F, 1.302F, 1.302F, 2.75F),
                PartPose.offsetAndRotation(4.6F, -3.2F, -4F, 0.1396F, 0.5585F, 0F));
        PartDefinition p_horn_l12 = p_horn_l11.addOrReplaceChild("horn_l12", CubeListBuilder.create()
                .texOffs(281, 286).addBox(-0.8F, -0.8F, -0.4F, 1.6F, 1.6F, 2.875F)
                .texOffs(432, 293).addBox(-0.496F, -0.496F, 2.25F, 0.992F, 0.992F, 2.5F),
                PartPose.offsetAndRotation(0F, 0F, 5F, -0.1396F, 0.1047F, 0F));
        PartDefinition p_horn_l13 = p_horn_l12.addOrReplaceChild("horn_l13", CubeListBuilder.create()
                .texOffs(292, 286).addBox(-0.55F, -0.55F, -0.4F, 1.1F, 1.1F, 2.325F)
                .texOffs(10, 298).addBox(-0.341F, -0.341F, 1.75F, 0.682F, 0.682F, 2F),
                PartPose.offsetAndRotation(0F, 0F, 4.5F, -0.1396F, 0.0349F, 0F));
        PartDefinition p_brow_r = p_head.addOrReplaceChild("brow_r", CubeListBuilder.create()
                .texOffs(27, 248).addBox(-2.1F, -1.2F, -0.6F, 4.2F, 2.2F, 8F),
                PartPose.offsetAndRotation(-3.8F, -4.4F, -12.6F, 0.1396F, 0F, 0.2793F));
        PartDefinition p_horn_r01 = p_head.addOrReplaceChild("horn_r01", CubeListBuilder.create()
                .texOffs(143, 270).addBox(-1.35F, -1.35F, -0.4F, 2.7F, 2.7F, 3.975F)
                .texOffs(497, 279).addBox(-0.837F, -0.837F, 3.25F, 1.674F, 1.674F, 3.5F),
                PartPose.offsetAndRotation(-2.8F, -4.8F, -6F, 0.4189F, -0.2094F, 0F));
        PartDefinition p_horn_r02 = p_horn_r01.addOrReplaceChild("horn_r02", CubeListBuilder.create()
                .texOffs(158, 270).addBox(-1.05F, -1.05F, -0.4F, 2.1F, 2.1F, 3.7F)
                .texOffs(0, 286).addBox(-0.651F, -0.651F, 3F, 1.302F, 1.302F, 3.25F),
                PartPose.offsetAndRotation(0F, 0F, 6.5F, -0.1396F, -0.0698F, 0F));
        PartDefinition p_horn_r03 = p_horn_r02.addOrReplaceChild("horn_r03", CubeListBuilder.create()
                .texOffs(13, 286).addBox(-0.75F, -0.75F, -0.4F, 1.5F, 1.5F, 3.15F)
                .texOffs(441, 293).addBox(-0.465F, -0.465F, 2.5F, 0.93F, 0.93F, 2.75F),
                PartPose.offsetAndRotation(0F, 0F, 6F, -0.1745F, -0.0349F, 0F));
        PartDefinition p_horn_r04 = p_horn_r03.addOrReplaceChild("horn_r04", CubeListBuilder.create()
                .texOffs(450, 293).addBox(-0.5F, -0.5F, -0.4F, 1F, 1F, 2.6F)
                .texOffs(459, 293).addBox(-0.31F, -0.31F, 2F, 0.62F, 0.62F, 2.25F),
                PartPose.offsetAndRotation(0F, 0F, 5F, -0.1047F, 0F, 0F));
        PartDefinition p_horn_r11 = p_head.addOrReplaceChild("horn_r11", CubeListBuilder.create()
                .texOffs(173, 270).addBox(-1.05F, -1.05F, -0.4F, 2.1F, 2.1F, 3.15F)
                .texOffs(303, 286).addBox(-0.651F, -0.651F, 2.5F, 1.302F, 1.302F, 2.75F),
                PartPose.offsetAndRotation(-4.6F, -3.2F, -4F, 0.1396F, -0.5585F, 0F));
        PartDefinition p_horn_r12 = p_horn_r11.addOrReplaceChild("horn_r12", CubeListBuilder.create()
                .texOffs(314, 286).addBox(-0.8F, -0.8F, -0.4F, 1.6F, 1.6F, 2.875F)
                .texOffs(468, 293).addBox(-0.496F, -0.496F, 2.25F, 0.992F, 0.992F, 2.5F),
                PartPose.offsetAndRotation(0F, 0F, 5F, -0.1396F, -0.1047F, 0F));
        PartDefinition p_horn_r13 = p_horn_r12.addOrReplaceChild("horn_r13", CubeListBuilder.create()
                .texOffs(325, 286).addBox(-0.55F, -0.55F, -0.4F, 1.1F, 1.1F, 2.325F)
                .texOffs(17, 298).addBox(-0.341F, -0.341F, 1.75F, 0.682F, 0.682F, 2F),
                PartPose.offsetAndRotation(0F, 0F, 4.5F, -0.1396F, -0.0349F, 0F));
        PartDefinition p_jaw = p_head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(0, 200).addBox(-3.5F, 1F, -15.6F, 7F, 1F, 16F)
                .texOffs(203, 179).addBox(-2.5F, 0F, -15.52F, 5F, 3F, 15.84F)
                .texOffs(69, 270).addBox(-4.4F, 0.8F, -3F, 8.8F, 1.4F, 4.5F)
                .texOffs(160, 260).addBox(-3.2F, -0.4F, -2.92F, 6.4F, 3.8F, 4.34F)
                .texOffs(94, 298).addBox(2.625F, -1.385F, -14.7393F, 0.75F, 1.485F, 0.75F)
                .texOffs(99, 298).addBox(2.625F, -1F, -13.0679F, 0.75F, 1.1F, 0.75F)
                .texOffs(104, 298).addBox(2.625F, -1F, -11.3964F, 0.75F, 1.1F, 0.75F)
                .texOffs(109, 298).addBox(2.625F, -1F, -9.725F, 0.75F, 1.1F, 0.75F)
                .texOffs(114, 298).addBox(2.625F, -1F, -8.0536F, 0.75F, 1.1F, 0.75F)
                .texOffs(119, 298).addBox(2.625F, -1F, -6.3821F, 0.75F, 1.1F, 0.75F)
                .texOffs(124, 298).addBox(2.625F, -1.385F, -4.7107F, 0.75F, 1.485F, 0.75F)
                .texOffs(129, 298).addBox(-3.375F, -1.385F, -14.7393F, 0.75F, 1.485F, 0.75F)
                .texOffs(134, 298).addBox(-3.375F, -1F, -13.0679F, 0.75F, 1.1F, 0.75F)
                .texOffs(139, 298).addBox(-3.375F, -1F, -11.3964F, 0.75F, 1.1F, 0.75F)
                .texOffs(144, 298).addBox(-3.375F, -1F, -9.725F, 0.75F, 1.1F, 0.75F)
                .texOffs(149, 298).addBox(-3.375F, -1F, -8.0536F, 0.75F, 1.1F, 0.75F)
                .texOffs(154, 298).addBox(-3.375F, -1F, -6.3821F, 0.75F, 1.1F, 0.75F)
                .texOffs(159, 298).addBox(-3.375F, -1.385F, -4.7107F, 0.75F, 1.485F, 0.75F)
                .texOffs(101, 200).addBox(-3.1F, -0.15F, -15.4F, 6.2F, 0.3F, 15F)
                .texOffs(406, 218).addBox(-2.1F, -0.45F, -14F, 4.2F, 0.5F, 11.5F)
                .texOffs(203, 234).addBox(-2.6F, 2.8F, -11F, 5.2F, 0.7F, 11F),
                PartPose.offsetAndRotation(0F, 3.6F, -11F, 0.0524F, 0F, 0F));
        PartDefinition p_beard1 = p_jaw.addOrReplaceChild("beard1", CubeListBuilder.create()
                .texOffs(477, 293).addBox(-0.7F, -0.91F, -0.875F, 1.4F, 1.51F, 1.75F)
                .texOffs(504, 293).addBox(-0.455F, -1.78F, -0.5687F, 0.91F, 1.12F, 1.1375F)
                .texOffs(164, 298).addBox(-0.25F, -2.65F, -0.2625F, 0.5F, 1.12F, 0.525F),
                PartPose.offsetAndRotation(0F, 2.9F, -7F, -2.618F, 0F, 0F));
        PartDefinition p_beard_l = p_jaw.addOrReplaceChild("beard_l", CubeListBuilder.create()
                .texOffs(486, 293).addBox(-0.6F, -0.742F, -0.75F, 1.2F, 1.342F, 1.5F)
                .texOffs(169, 298).addBox(-0.39F, -1.496F, -0.4875F, 0.78F, 1.004F, 0.975F)
                .texOffs(174, 298).addBox(-0.25F, -2.25F, -0.25F, 0.5F, 1.004F, 0.5F),
                PartPose.offsetAndRotation(2.3F, 2.8F, -4F, -2.618F, -0.1745F, 0F));
        PartDefinition p_beard_r = p_jaw.addOrReplaceChild("beard_r", CubeListBuilder.create()
                .texOffs(495, 293).addBox(-0.6F, -0.742F, -0.75F, 1.2F, 1.342F, 1.5F)
                .texOffs(179, 298).addBox(-0.39F, -1.496F, -0.4875F, 0.78F, 1.004F, 0.975F)
                .texOffs(184, 298).addBox(-0.25F, -2.25F, -0.25F, 0.5F, 1.004F, 0.5F),
                PartPose.offsetAndRotation(-2.3F, 2.8F, -4F, -2.618F, 0.1745F, 0F));
        PartDefinition p_tail1 = p_body.addOrReplaceChild("tail1", CubeListBuilder.create()
                .texOffs(225, 200).addBox(-5F, -1.8F, -0.6F, 10F, 3.6F, 10.2F)
                .texOffs(141, 179).addBox(-2.3F, -4.5F, -0.52F, 4.6F, 9F, 10.04F)
                .texOffs(386, 234).addBox(-2.3F, 3.5F, 0F, 4.6F, 1.6F, 9F)
                .texOffs(226, 248).addBox(-0.3F, -7.7F, 1.5F, 0.6F, 4.1F, 6F),
                PartPose.offsetAndRotation(0F, -3F, 15F, -0.1047F, 0F, 0F));
        PartDefinition p_tail2 = p_tail1.addOrReplaceChild("tail2", CubeListBuilder.create()
                .texOffs(268, 200).addBox(-4.5F, -1.6F, -0.6F, 9F, 3.2F, 10.2F)
                .texOffs(246, 179).addBox(-2.1F, -4F, -0.52F, 4.2F, 8F, 10.04F)
                .texOffs(415, 234).addBox(-2.1F, 3F, 0F, 4.2F, 1.6F, 9F)
                .texOffs(411, 248).addBox(-0.3F, -6.85F, 1.5F, 0.6F, 3.75F, 6F),
                PartPose.offsetAndRotation(0F, 0F, 9F, -0.0698F, 0F, 0F));
        PartDefinition p_tail3 = p_tail2.addOrReplaceChild("tail3", CubeListBuilder.create()
                .texOffs(369, 218).addBox(-4F, -1.44F, -0.6F, 8F, 2.88F, 9.7F)
                .texOffs(374, 179).addBox(-1.84F, -3.6F, -0.52F, 3.68F, 7.2F, 9.54F)
                .texOffs(54, 248).addBox(-1.84F, 2.6F, 0F, 3.68F, 1.6F, 8.5F)
                .texOffs(426, 248).addBox(-0.3F, -6.1F, 1.5F, 0.6F, 3.4F, 5.5F),
                PartPose.offsetAndRotation(0F, 0F, 9F, -0.0349F, 0F, 0F));
        PartDefinition p_tail4 = p_tail3.addOrReplaceChild("tail4", CubeListBuilder.create()
                .texOffs(441, 218).addBox(-3.5F, -1.28F, -0.6F, 7F, 2.56F, 9.7F)
                .texOffs(47, 200).addBox(-1.58F, -3.2F, -0.52F, 3.16F, 6.4F, 9.54F)
                .texOffs(81, 248).addBox(-1.58F, 2.2F, 0F, 3.16F, 1.6F, 8.5F)
                .texOffs(441, 248).addBox(-0.3F, -5.35F, 1.5F, 0.6F, 3.05F, 5.5F),
                PartPose.offset(0F, 0F, 8.5F));
        PartDefinition p_tail5 = p_tail4.addOrReplaceChild("tail5", CubeListBuilder.create()
                .texOffs(476, 218).addBox(-3.1F, -1.12F, -0.6F, 6.2F, 2.24F, 9.2F)
                .texOffs(173, 200).addBox(-1.42F, -2.8F, -0.52F, 2.84F, 5.6F, 9.04F)
                .texOffs(285, 248).addBox(-1.42F, 1.8F, 0F, 2.84F, 1.6F, 8F)
                .texOffs(30, 270).addBox(-0.3F, -4.6F, 1.5F, 0.6F, 2.7F, 5F),
                PartPose.offsetAndRotation(0F, 0F, 8.5F, 0.0175F, 0F, 0F));
        PartDefinition p_tail6 = p_tail5.addOrReplaceChild("tail6", CubeListBuilder.create()
                .texOffs(304, 234).addBox(-2.7F, -1F, -0.6F, 5.4F, 2F, 9.2F)
                .texOffs(0, 218).addBox(-1.2F, -2.5F, -0.52F, 2.4F, 5F, 9.04F)
                .texOffs(308, 248).addBox(-1.2F, 1.5F, 0F, 2.4F, 1.6F, 8F)
                .texOffs(43, 270).addBox(-0.3F, -3.95F, 1.5F, 0.6F, 2.35F, 5F),
                PartPose.offsetAndRotation(0F, 0F, 8F, 0.0349F, 0F, 0F));
        PartDefinition p_tail7 = p_tail6.addOrReplaceChild("tail7", CubeListBuilder.create()
                .texOffs(444, 234).addBox(-2.4F, -0.88F, -0.6F, 4.8F, 1.76F, 8.7F)
                .texOffs(294, 218).addBox(-1.08F, -2.2F, -0.52F, 2.16F, 4.4F, 8.54F)
                .texOffs(331, 248).addBox(-1.08F, 1.2F, 0F, 2.16F, 1.6F, 7.5F)
                .texOffs(56, 270).addBox(-0.3F, -3.3F, 1.5F, 0.6F, 2F, 4.5F),
                PartPose.offsetAndRotation(0F, 0F, 8F, 0.0349F, 0F, 0F));
        PartDefinition p_tail8 = p_tail7.addOrReplaceChild("tail8", CubeListBuilder.create()
                .texOffs(473, 234).addBox(-2.1F, -0.76F, -0.6F, 4.2F, 1.52F, 8.7F)
                .texOffs(0, 234).addBox(-0.96F, -1.9F, -0.52F, 1.92F, 3.8F, 8.54F)
                .texOffs(354, 248).addBox(-0.96F, 0.9F, 0F, 1.92F, 1.6F, 7.5F)
                .texOffs(278, 270).addBox(-0.3F, -2.65F, 1.5F, 0.6F, 1.65F, 4.5F),
                PartPose.offsetAndRotation(0F, 0F, 7.5F, 0.0349F, 0F, 0F));
        PartDefinition p_tail9 = p_tail8.addOrReplaceChild("tail9", CubeListBuilder.create()
                .texOffs(108, 248).addBox(-1.8F, -0.64F, -0.6F, 3.6F, 1.28F, 8.2F)
                .texOffs(23, 234).addBox(-0.84F, -1.6F, -0.52F, 1.68F, 3.2F, 8.04F)
                .texOffs(300, 260).addBox(-0.84F, 0.6F, 0F, 1.68F, 1.6F, 7F)
                .texOffs(52, 286).addBox(-0.3F, -2F, 1.5F, 0.6F, 1.3F, 4F),
                PartPose.offsetAndRotation(0F, 0F, 7.5F, 0.0349F, 0F, 0F));
        PartDefinition p_tail10 = p_tail9.addOrReplaceChild("tail10", CubeListBuilder.create()
                .texOffs(135, 248).addBox(-1.5F, -0.56F, -0.6F, 3F, 1.12F, 8.2F)
                .texOffs(337, 234).addBox(-0.66F, -1.4F, -0.52F, 1.32F, 2.8F, 8.04F)
                .texOffs(319, 260).addBox(-0.66F, 0.4F, 0F, 1.32F, 1.6F, 7F),
                PartPose.offsetAndRotation(0F, 0F, 7F, 0.0349F, 0F, 0F));
        PartDefinition p_tail11 = p_tail10.addOrReplaceChild("tail11", CubeListBuilder.create()
                .texOffs(254, 260).addBox(-1.3F, -0.48F, -0.6F, 2.6F, 0.96F, 7.7F)
                .texOffs(160, 248).addBox(-0.58F, -1.2F, -0.52F, 1.16F, 2.4F, 7.54F)
                .texOffs(338, 260).addBox(-0.58F, 0.2F, 0F, 1.16F, 1.6F, 6.5F),
                PartPose.offsetAndRotation(0F, 0F, 7F, 0.0175F, 0F, 0F));
        PartDefinition p_tail12 = p_tail11.addOrReplaceChild("tail12", CubeListBuilder.create()
                .texOffs(277, 260).addBox(-1.1F, -0.4F, -0.6F, 2.2F, 0.8F, 7.2F)
                .texOffs(375, 248).addBox(-0.5F, -1F, -0.52F, 1F, 2F, 7.04F)
                .texOffs(477, 260).addBox(-0.5F, 0F, 0F, 1F, 1.6F, 6F)
                .texOffs(48, 218).addBox(-5.5F, -0.5F, 4.5F, 11F, 1F, 13F),
                PartPose.offsetAndRotation(0F, 0F, 6.5F, 0.0175F, 0F, 0F));
        PartDefinition p_hind_leg_l = p_body.addOrReplaceChild("hind_leg_l", CubeListBuilder.create()
                .texOffs(0, 125).addBox(-4.5F, -3.6F, -3.52F, 9F, 19.6F, 7.04F)
                .texOffs(319, 92).addBox(-2.52F, -3.52F, -5.5F, 5.04F, 19.44F, 11F)
                .texOffs(220, 218).addBox(-5.1F, -4.95F, -3.4F, 10.2F, 6.75F, 6.8F)
                .texOffs(67, 179).addBox(-2.4F, -4.87F, -6.1F, 4.8F, 6.59F, 12.2F),
                PartPose.offsetAndRotation(9.5F, 3F, 8F, -0.3142F, 0F, 0F));
        PartDefinition p_hind_shin_l = p_hind_leg_l.addOrReplaceChild("hind_shin_l", CubeListBuilder.create()
                .texOffs(470, 154).addBox(-3F, -1.4F, -1.68F, 6F, 16.6F, 3.36F)
                .texOffs(363, 154).addBox(-1.68F, -1.32F, -3F, 3.36F, 16.44F, 6F),
                PartPose.offsetAndRotation(0F, 15.4F, 0F, 0.733F, 0F, 0F));
        PartDefinition p_hind_foot_l = p_hind_shin_l.addOrReplaceChild("hind_foot_l", CubeListBuilder.create()
                .texOffs(138, 218).addBox(-4F, 0.9F, -8.4F, 8F, 1.2F, 12F)
                .texOffs(309, 200).addBox(-3.1F, 0F, -8.32F, 6.2F, 3F, 11.84F)
                .texOffs(188, 270).addBox(-3.8167F, 1.1F, -12F, 1.9F, 1.9F, 4.7F)
                .texOffs(336, 286).addBox(-3.5317F, 1.575F, -14.6F, 1.33F, 1.71F, 2.9F)
                .texOffs(203, 270).addBox(-0.95F, 1.1F, -12F, 1.9F, 1.9F, 4.7F)
                .texOffs(347, 286).addBox(-0.665F, 1.575F, -14.6F, 1.33F, 1.71F, 2.9F)
                .texOffs(218, 270).addBox(1.9167F, 1.1F, -12F, 1.9F, 1.9F, 4.7F)
                .texOffs(358, 286).addBox(2.2017F, 1.575F, -14.6F, 1.33F, 1.71F, 2.9F),
                PartPose.offsetAndRotation(0F, 14.4F, 0F, -0.4189F, 0F, 0F));
        PartDefinition p_hind_leg_r = p_body.addOrReplaceChild("hind_leg_r", CubeListBuilder.create()
                .texOffs(35, 125).addBox(-4.5F, -3.6F, -3.52F, 9F, 19.6F, 7.04F)
                .texOffs(354, 92).addBox(-2.52F, -3.52F, -5.5F, 5.04F, 19.44F, 11F)
                .texOffs(257, 218).addBox(-5.1F, -4.95F, -3.4F, 10.2F, 6.75F, 6.8F)
                .texOffs(104, 179).addBox(-2.4F, -4.87F, -6.1F, 4.8F, 6.59F, 12.2F),
                PartPose.offsetAndRotation(-9.5F, 3F, 8F, -0.3142F, 0F, 0F));
        PartDefinition p_hind_shin_r = p_hind_leg_r.addOrReplaceChild("hind_shin_r", CubeListBuilder.create()
                .texOffs(491, 154).addBox(-3F, -1.4F, -1.68F, 6F, 16.6F, 3.36F)
                .texOffs(384, 154).addBox(-1.68F, -1.32F, -3F, 3.36F, 16.44F, 6F),
                PartPose.offsetAndRotation(0F, 15.4F, 0F, 0.733F, 0F, 0F));
        PartDefinition p_hind_foot_r = p_hind_shin_r.addOrReplaceChild("hind_foot_r", CubeListBuilder.create()
                .texOffs(179, 218).addBox(-4F, 0.9F, -8.4F, 8F, 1.2F, 12F)
                .texOffs(348, 200).addBox(-3.1F, 0F, -8.32F, 6.2F, 3F, 11.84F)
                .texOffs(233, 270).addBox(-3.8167F, 1.1F, -12F, 1.9F, 1.9F, 4.7F)
                .texOffs(369, 286).addBox(-3.5317F, 1.575F, -14.6F, 1.33F, 1.71F, 2.9F)
                .texOffs(248, 270).addBox(-0.95F, 1.1F, -12F, 1.9F, 1.9F, 4.7F)
                .texOffs(380, 286).addBox(-0.665F, 1.575F, -14.6F, 1.33F, 1.71F, 2.9F)
                .texOffs(263, 270).addBox(1.9167F, 1.1F, -12F, 1.9F, 1.9F, 4.7F)
                .texOffs(391, 286).addBox(2.2017F, 1.575F, -14.6F, 1.33F, 1.71F, 2.9F),
                PartPose.offsetAndRotation(0F, 14.4F, 0F, -0.4189F, 0F, 0F));
        PartDefinition p_wing_l = p_body.addOrReplaceChild("wing_l", CubeListBuilder.create()
                .texOffs(49, 260).addBox(-1F, -2.8F, -1.232F, 17.5F, 5.6F, 2.464F)
                .texOffs(456, 248).addBox(-0.92F, -1.232F, -2.8F, 17.34F, 2.464F, 5.6F)
                .texOffs(329, 279).addBox(15F, -1.84F, -0.8096F, 15.8F, 3.68F, 1.6192F)
                .texOffs(247, 279).addBox(15.08F, -0.8096F, -1.84F, 15.64F, 1.6192F, 3.68F),
                PartPose.offset(10.5F, -7F, -12F));
        PartDefinition p_wing_l_web = p_wing_l.addOrReplaceChild("wing_l_web", CubeListBuilder.create()
                .texOffs(0, 92).addBox(0F, 0F, 1.34F, 30F, 0F, 32F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_wing_l_fore = p_wing_l.addOrReplaceChild("wing_l_fore", CubeListBuilder.create()
                .texOffs(161, 279).addBox(-0.6F, -1.794F, -0.7894F, 18.6F, 3.588F, 1.5787F)
                .texOffs(67, 279).addBox(-0.52F, -0.7894F, -1.794F, 18.44F, 1.5787F, 3.588F)
                .texOffs(161, 286).addBox(16.2F, -1.265F, -0.5566F, 20.4F, 2.53F, 1.1132F)
                .texOffs(63, 286).addBox(16.28F, -0.5566F, -1.265F, 20.24F, 1.1132F, 2.53F),
                PartPose.offset(30F, 0F, 0F));
        PartDefinition p_wing_l_fore_web = p_wing_l_fore.addOrReplaceChild("wing_l_fore_web", CubeListBuilder.create()
                .texOffs(222, 54).addBox(0F, 0F, 0.765F, 36F, 0F, 34F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_wing_l_hand = p_wing_l_fore.addOrReplaceChild("wing_l_hand", CubeListBuilder.create()
                .texOffs(494, 260).addBox(-2.185F, -2.185F, -1.0925F, 4.37F, 4.37F, 2.185F)
                .texOffs(416, 260).addBox(-2.105F, -1.0925F, -2.185F, 4.21F, 2.185F, 4.37F)
                .texOffs(26, 286).addBox(-0.9F, -0.9F, -5.385F, 1.8F, 1.8F, 3.4F)
                .texOffs(402, 286).addBox(-0.6F, -0.5F, -7.585F, 1.2F, 1.5F, 2.4F),
                PartPose.offset(36F, 0F, 0F));
        PartDefinition p_wing_l_f1 = p_wing_l_hand.addOrReplaceChild("wing_l_f1", CubeListBuilder.create()
                .texOffs(291, 270).addBox(0F, -1.2F, -1.2F, 32.24F, 2.4F, 2.4F)
                .texOffs(424, 286).addBox(31F, -0.72F, -0.72F, 31F, 1.44F, 1.44F)
                .texOffs(70, 125).addBox(0F, 0F, 0.42F, 62F, 0F, 26F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, -0.1396F, 0F));
        PartDefinition p_wing_l_f2 = p_wing_l_hand.addOrReplaceChild("wing_l_f2", CubeListBuilder.create()
                .texOffs(437, 270).addBox(0F, -1.05F, -1.05F, 29.12F, 2.1F, 2.1F)
                .texOffs(67, 293).addBox(28F, -0.63F, -0.63F, 28F, 1.26F, 1.26F)
                .texOffs(0, 154).addBox(0F, 0F, 0.33F, 56F, 0F, 24F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, -0.5934F, 0F));
        PartDefinition p_wing_l_f3 = p_wing_l_hand.addOrReplaceChild("wing_l_f3", CubeListBuilder.create()
                .texOffs(189, 293).addBox(0F, -0.9F, -0.9F, 24.44F, 1.8F, 1.8F)
                .texOffs(299, 293).addBox(23.5F, -0.54F, -0.54F, 23.5F, 1.08F, 1.08F)
                .texOffs(198, 0).addBox(0F, 0F, 0.24F, 47F, 0F, 37F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, -1.0821F, 0F));
        PartDefinition p_wing_r = p_body.addOrReplaceChild("wing_r", CubeListBuilder.create()
                .texOffs(92, 260).addBox(-16.5F, -2.8F, -1.232F, 17.5F, 5.6F, 2.464F)
                .texOffs(0, 260).addBox(-16.42F, -1.232F, -2.8F, 17.34F, 2.464F, 5.6F)
                .texOffs(366, 279).addBox(-30.8F, -1.84F, -0.8096F, 15.8F, 3.68F, 1.6192F)
                .texOffs(288, 279).addBox(-30.72F, -0.8096F, -1.84F, 15.64F, 1.6192F, 3.68F),
                PartPose.offset(-10.5F, -7F, -12F));
        PartDefinition p_wing_r_web = p_wing_r.addOrReplaceChild("wing_r_web", CubeListBuilder.create()
                .texOffs(125, 92).addBox(-30F, 0F, 1.34F, 30F, 0F, 32F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_wing_r_fore = p_wing_r.addOrReplaceChild("wing_r_fore", CubeListBuilder.create()
                .texOffs(204, 279).addBox(-18F, -1.794F, -0.7894F, 18.6F, 3.588F, 1.5787F)
                .texOffs(114, 279).addBox(-17.92F, -0.7894F, -1.794F, 18.44F, 1.5787F, 3.588F)
                .texOffs(208, 286).addBox(-36.6F, -1.265F, -0.5566F, 20.4F, 2.53F, 1.1132F)
                .texOffs(112, 286).addBox(-36.52F, -0.5566F, -1.265F, 20.24F, 1.1132F, 2.53F),
                PartPose.offset(-30F, 0F, 0F));
        PartDefinition p_wing_r_fore_web = p_wing_r_fore.addOrReplaceChild("wing_r_fore_web", CubeListBuilder.create()
                .texOffs(363, 54).addBox(-36F, 0F, 0.765F, 36F, 0F, 34F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_wing_r_hand = p_wing_r_fore.addOrReplaceChild("wing_r_hand", CubeListBuilder.create()
                .texOffs(0, 270).addBox(-2.185F, -2.185F, -1.0925F, 4.37F, 4.37F, 2.185F)
                .texOffs(437, 260).addBox(-2.105F, -1.0925F, -2.185F, 4.21F, 2.185F, 4.37F)
                .texOffs(39, 286).addBox(-0.9F, -0.9F, -5.385F, 1.8F, 1.8F, 3.4F)
                .texOffs(413, 286).addBox(-0.6F, -0.5F, -7.585F, 1.2F, 1.5F, 2.4F),
                PartPose.offset(-36F, 0F, 0F));
        PartDefinition p_wing_r_f1 = p_wing_r_hand.addOrReplaceChild("wing_r_f1", CubeListBuilder.create()
                .texOffs(364, 270).addBox(-32.24F, -1.2F, -1.2F, 32.24F, 2.4F, 2.4F)
                .texOffs(0, 293).addBox(-62F, -0.72F, -0.72F, 31F, 1.44F, 1.44F)
                .texOffs(247, 125).addBox(-62F, 0F, 0.42F, 62F, 0F, 26F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0.1396F, 0F));
        PartDefinition p_wing_r_f2 = p_wing_r_hand.addOrReplaceChild("wing_r_f2", CubeListBuilder.create()
                .texOffs(0, 279).addBox(-29.12F, -1.05F, -1.05F, 29.12F, 2.1F, 2.1F)
                .texOffs(128, 293).addBox(-56F, -0.63F, -0.63F, 28F, 1.26F, 1.26F)
                .texOffs(161, 154).addBox(-56F, 0F, 0.33F, 56F, 0F, 24F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0.5934F, 0F));
        PartDefinition p_wing_r_f3 = p_wing_r_hand.addOrReplaceChild("wing_r_f3", CubeListBuilder.create()
                .texOffs(244, 293).addBox(-24.44F, -0.9F, -0.9F, 24.44F, 1.8F, 1.8F)
                .texOffs(352, 293).addBox(-47F, -0.54F, -0.54F, 23.5F, 1.08F, 1.08F)
                .texOffs(0, 54).addBox(-47F, 0F, 0.24F, 47F, 0F, 37F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 1.0821F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
