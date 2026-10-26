package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie von Verdantis, dem Naturdrachen-Boss (Grundgröße, im Spiel ×2,0).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class VerdantisGeometry {
    public static final int TEXTURE_WIDTH = 512;
    public static final int TEXTURE_HEIGHT = 512;

    private VerdantisGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(131, 0).addBox(-14.5F, -7F, -23F, 29F, 12F, 46F)
                .texOffs(0, 0).addBox(-9.5F, -12F, -22.92F, 19F, 22F, 45.84F)
                .texOffs(105, 144).addBox(-16F, -7.5F, -24F, 32F, 13F, 17F)
                .texOffs(395, 0).addBox(-10.5F, -13F, -23.92F, 21F, 24F, 16.84F)
                .texOffs(287, 175).addBox(-15F, -7.5F, 9F, 30F, 11F, 14F)
                .texOffs(407, 69).addBox(-10F, -12.5F, 9.08F, 20F, 21F, 13.84F)
                .texOffs(0, 69).addBox(-11.5F, -14F, -20F, 23F, 1F, 37F)
                .texOffs(282, 0).addBox(-9.5F, -16F, -19.92F, 19F, 5F, 36.84F)
                .texOffs(376, 175).addBox(-8F, -17F, -14F, 16F, 0.6F, 24F)
                .texOffs(37, 175).addBox(-6.8F, -18.2F, -13.92F, 13.6F, 3F, 23.84F)
                .texOffs(134, 231).addBox(-12F, 9.5F, -23F, 24F, 2.6F, 19F)
                .texOffs(294, 144).addBox(-11F, 8.6F, -4F, 22F, 2.5F, 26F)
                .texOffs(220, 378).addBox(-10F, -21.5F, -15.6F, 8F, 5F, 5.2F)
                .texOffs(156, 378).addBox(-9.92F, -20.1F, -17F, 7.84F, 2.2F, 8F)
                .texOffs(411, 365).addBox(2F, -22.5F, -9.32F, 9F, 6F, 5.64F)
                .texOffs(197, 365).addBox(2.08F, -20.82F, -11F, 8.84F, 2.64F, 9F)
                .texOffs(157, 391).addBox(-7F, -21F, -0.74F, 7F, 4.5F, 4.48F)
                .texOffs(0, 402).addBox(-6.92F, -19.74F, -2F, 6.84F, 1.98F, 7F)
                .texOffs(130, 391).addBox(4F, -20.5F, 6.4F, 8F, 5F, 4.2F)
                .texOffs(99, 391).addBox(4.08F, -19.1F, 5F, 7.84F, 2.2F, 7F)
                .texOffs(0, 412).addBox(-4F, -19.5F, 13.12F, 7F, 4F, 3.76F)
                .texOffs(344, 402).addBox(-3.92F, -18.38F, 12F, 6.84F, 1.76F, 6F)
                .texOffs(445, 444).addBox(-7F, -22.6F, -15F, 1.4F, 1.4F, 1.4F)
                .texOffs(454, 444).addBox(5F, -24.3F, -8F, 1.4F, 1.4F, 1.4F)
                .texOffs(463, 444).addBox(-4.5F, -22.4F, 0F, 1.4F, 1.4F, 1.4F)
                .texOffs(472, 444).addBox(7F, -21.8F, 7F, 1.4F, 1.4F, 1.4F)
                .texOffs(481, 444).addBox(-1.5F, -20.8F, 14F, 1.4F, 1.4F, 1.4F)
                .texOffs(490, 444).addBox(3F, -24.3F, -12F, 1.4F, 1.4F, 1.4F)
                .texOffs(298, 421).addBox(15.6F, -4F, -8F, 3F, 1.2F, 5F)
                .texOffs(111, 412).addBox(15.6F, -6F, 9F, 3F, 1.2F, 6F)
                .texOffs(205, 437).addBox(15.6F, 0F, 14F, 2F, 1.2F, 3.5F)
                .texOffs(315, 421).addBox(-18.6F, -4F, -8F, 3F, 1.2F, 5F)
                .texOffs(130, 412).addBox(-18.6F, -6F, 9F, 3F, 1.2F, 6F)
                .texOffs(218, 437).addBox(-17.6F, 0F, 14F, 2F, 1.2F, 3.5F),
                PartPose.offset(0F, -8F, 0F));
        PartDefinition p_tree_0 = p_body.addOrReplaceChild("tree_0", CubeListBuilder.create()
                .texOffs(205, 391).addBox(-1.5F, -5.5F, -1.5F, 3F, 6.5F, 3F)
                .texOffs(218, 391).addBox(-1.1F, -11.5F, -1.1F, 2.2F, 6.05F, 2.2F)
                .texOffs(442, 365).addBox(-4.5F, -15.5F, -2.744F, 9F, 5.2F, 5.488F)
                .texOffs(234, 365).addBox(-4.42F, -14.044F, -4.2F, 8.84F, 2.288F, 8.4F)
                .texOffs(23, 412).addBox(-3F, -18.6F, -1.492F, 6.4F, 3.6F, 3.584F)
                .texOffs(371, 402).addBox(-2.92F, -17.592F, -2.5F, 6.24F, 1.584F, 5.6F)
                .texOffs(332, 421).addBox(1.5F, -14F, 0.008F, 5F, 3.6F, 2.984F)
                .texOffs(161, 421).addBox(1.58F, -12.992F, -1F, 4.84F, 1.584F, 5F)
                .texOffs(499, 444).addBox(-2.2F, -19.2F, -0.6F, 1.6F, 1.6F, 1.6F),
                PartPose.offsetAndRotation(-4F, -18F, -14F, -0.1745F, -0.3491F, 0F));
        PartDefinition p_tree_0_bough = p_tree_0.addOrReplaceChild("tree_0_bough", CubeListBuilder.create()
                .texOffs(434, 412).addBox(-0.8F, -5.5F, -0.8F, 1.6F, 6F, 1.6F)
                .texOffs(85, 402).addBox(-3.2F, -9F, -1.768F, 6.4F, 4.4F, 3.536F)
                .texOffs(398, 402).addBox(-3.12F, -7.768F, -3F, 6.24F, 1.936F, 6F),
                PartPose.offsetAndRotation(0F, -6.05F, 0F, 0F, 0F, 0.733F));
        PartDefinition p_tree_1 = p_body.addOrReplaceChild("tree_1", CubeListBuilder.create()
                .texOffs(283, 402).addBox(-1.5F, -4.5F, -1.5F, 3F, 5.5F, 3F)
                .texOffs(421, 412).addBox(-1.1F, -9.5F, -1.1F, 2.2F, 4.95F, 2.2F)
                .texOffs(473, 365).addBox(-4.5F, -13.5F, -2.744F, 9F, 5.2F, 5.488F)
                .texOffs(271, 365).addBox(-4.42F, -12.044F, -4.2F, 8.84F, 2.288F, 8.4F)
                .texOffs(46, 412).addBox(-3F, -16.6F, -1.492F, 6.4F, 3.6F, 3.584F)
                .texOffs(425, 402).addBox(-2.92F, -15.592F, -2.5F, 6.24F, 1.584F, 5.6F)
                .texOffs(349, 421).addBox(1.5F, -12F, 0.008F, 5F, 3.6F, 2.984F)
                .texOffs(182, 421).addBox(1.58F, -10.992F, -1F, 4.84F, 1.584F, 5F)
                .texOffs(0, 450).addBox(-2.2F, -17.2F, -0.6F, 1.6F, 1.6F, 1.6F),
                PartPose.offsetAndRotation(5F, -18F, 2F, -0.2793F, 0.4363F, 0F));
        PartDefinition p_tree_1_bough = p_tree_1.addOrReplaceChild("tree_1_bough", CubeListBuilder.create()
                .texOffs(443, 412).addBox(-0.8F, -5.5F, -0.8F, 1.6F, 6F, 1.6F)
                .texOffs(108, 402).addBox(-3.2F, -9F, -1.768F, 6.4F, 4.4F, 3.536F)
                .texOffs(452, 402).addBox(-3.12F, -7.768F, -3F, 6.24F, 1.936F, 6F),
                PartPose.offsetAndRotation(0F, -4.95F, 0F, 0F, 0F, -0.733F));
        PartDefinition p_deadwood = p_body.addOrReplaceChild("deadwood", CubeListBuilder.create()
                .texOffs(296, 402).addBox(-1.2F, -5F, -1.2F, 2.4F, 6F, 2.4F)
                .texOffs(191, 429).addBox(-0.9F, -9.2F, -0.9F, 1.8F, 4.6F, 1.8F),
                PartPose.offsetAndRotation(-1F, -17F, 13F, -0.5236F, -0.1396F, 0.2094F));
        PartDefinition p_deadwood_twig = p_deadwood.addOrReplaceChild("deadwood_twig", CubeListBuilder.create()
                .texOffs(452, 412).addBox(-0.6F, -5F, -0.6F, 1.2F, 5.5F, 1.2F),
                PartPose.offsetAndRotation(0F, -6F, 0F, 0.1745F, 0F, -0.7854F));
        PartDefinition p_neck1 = p_body.addOrReplaceChild("neck1", CubeListBuilder.create()
                .texOffs(373, 298).addBox(-8.5F, -3.2F, -8.1F, 17F, 6.4F, 8.7F)
                .texOffs(37, 205).addBox(-3.7F, -8F, -8.02F, 7.4F, 16F, 8.54F)
                .texOffs(0, 391).addBox(-3.7F, 7F, -7.5F, 7.4F, 1.6F, 7.5F)
                .texOffs(109, 378).addBox(-7F, -9.5F, -8F, 14F, 1.2F, 8.5F)
                .texOffs(275, 350).addBox(-6F, -10.5F, -7.92F, 12F, 3.2F, 8.34F)
                .texOffs(253, 175).addBox(8.8F, -5F, -7.5F, 0F, 18F, 7.5F)
                .texOffs(270, 175).addBox(-8.8F, -5F, -7.5F, 0F, 18F, 7.5F),
                PartPose.offsetAndRotation(0F, -7F, -22F, -0.4189F, 0F, 0F));
        PartDefinition p_neck2 = p_neck1.addOrReplaceChild("neck2", CubeListBuilder.create()
                .texOffs(304, 317).addBox(-7.75F, -3F, -7.6F, 15.5F, 6F, 8.2F)
                .texOffs(395, 205).addBox(-3.25F, -7.5F, -7.52F, 6.5F, 15F, 8.04F)
                .texOffs(29, 402).addBox(-3.25F, 6.5F, -7F, 6.5F, 1.6F, 7F)
                .texOffs(412, 378).addBox(-6.25F, -9F, -7.5F, 12.5F, 1.2F, 8F)
                .texOffs(41, 365).addBox(-5.25F, -10F, -7.42F, 10.5F, 3.2F, 7.84F)
                .texOffs(104, 231).addBox(8.05F, -4.5F, -7F, 0F, 16F, 7F)
                .texOffs(119, 231).addBox(-8.05F, -4.5F, -7F, 0F, 16F, 7F),
                PartPose.offsetAndRotation(0F, 0F, -7.5F, -0.1745F, 0F, 0F));
        PartDefinition p_neck3 = p_neck2.addOrReplaceChild("neck3", CubeListBuilder.create()
                .texOffs(370, 334).addBox(-7F, -2.8F, -7.1F, 14F, 5.6F, 7.7F)
                .texOffs(425, 231).addBox(-2.8F, -7F, -7.02F, 5.6F, 14F, 7.54F)
                .texOffs(58, 402).addBox(-2.8F, 6F, -6.5F, 5.6F, 1.6F, 6.5F)
                .texOffs(455, 378).addBox(-5.5F, -8.5F, -7F, 11F, 1.2F, 7.5F)
                .texOffs(308, 365).addBox(-4.5F, -9.5F, -6.92F, 9F, 3.2F, 7.34F)
                .texOffs(371, 255).addBox(7.3F, -4F, -6.5F, 0F, 14F, 6.5F)
                .texOffs(386, 255).addBox(-7.3F, -4F, -6.5F, 0F, 14F, 6.5F),
                PartPose.offsetAndRotation(0F, 0F, -7F, 0.0349F, 0F, 0F));
        PartDefinition p_neck4 = p_neck3.addOrReplaceChild("neck4", CubeListBuilder.create()
                .texOffs(460, 334).addBox(-6.5F, -2.6F, -6.6F, 13F, 5.2F, 7.2F)
                .texOffs(342, 255).addBox(-2.6F, -6.5F, -6.52F, 5.2F, 13F, 7.04F)
                .texOffs(479, 402).addBox(-2.6F, 5.5F, -6F, 5.2F, 1.6F, 6F)
                .texOffs(362, 391).addBox(-5F, -8F, -6.5F, 10F, 1.2F, 7F)
                .texOffs(189, 378).addBox(-4F, -9F, -6.42F, 8F, 3.2F, 6.84F)
                .texOffs(129, 298).addBox(6.8F, -3.5F, -6F, 0F, 12F, 6F)
                .texOffs(142, 298).addBox(-6.8F, -3.5F, -6F, 0F, 12F, 6F),
                PartPose.offsetAndRotation(0F, 0F, -6.5F, 0.1745F, 0F, 0F));
        PartDefinition p_head = p_neck4.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 231).addBox(-8F, -4.5F, -15.5F, 16F, 6.5F, 16F)
                .texOffs(391, 144).addBox(-5F, -7.5F, -15.42F, 10F, 12.5F, 15.84F)
                .texOffs(231, 391).addBox(-8.6F, -8F, -15.5F, 17.2F, 0.6F, 8F)
                .texOffs(60, 378).addBox(-7.6F, -9F, -15.42F, 15.2F, 2.6F, 7.84F)
                .texOffs(415, 334).addBox(-6.6F, -2.8F, -22F, 13.2F, 5.2F, 8F)
                .texOffs(94, 298).addBox(-4.4F, -5F, -21.92F, 8.8F, 9.6F, 7.84F)
                .texOffs(80, 365).addBox(-6F, -2.5F, -28.5F, 12F, 5F, 7F)
                .texOffs(223, 317).addBox(-4F, -4.5F, -28.42F, 8F, 9F, 6.84F)
                .texOffs(0, 365).addBox(-4.2F, -5.8F, -27F, 8.4F, 0.6F, 11F)
                .texOffs(41, 350).addBox(-3.4F, -6.6F, -26.92F, 6.8F, 2.2F, 10.84F)
                .texOffs(0, 421).addBox(-4.6F, -4F, -29.2F, 9.2F, 5.4F, 1F)
                .texOffs(309, 402).addBox(-5F, -8.5F, -8F, 10F, 0.6F, 7F)
                .texOffs(33, 391).addBox(-4.1F, -9.4F, -7.92F, 8.2F, 2.4F, 6.84F)
                .texOffs(252, 450).addBox(1.6F, -3.6F, -29.4F, 2F, 1.2F, 0.5F)
                .texOffs(165, 429).addBox(6.3F, -4.6F, -15.9F, 1.85F, 2.2F, 3.9F)
                .texOffs(254, 317).addBox(7.2F, -0.3F, -15F, 2F, 5.1F, 10F)
                .texOffs(327, 298).addBox(7.9F, -1F, -14.92F, 0.6F, 6.5F, 9.84F)
                .texOffs(9, 450).addBox(4.85F, 4.5F, -27.55F, 1.1F, 1.62F, 1.1F)
                .texOffs(18, 450).addBox(4.85F, 4.5F, -25.55F, 1.1F, 1.2F, 1.1F)
                .texOffs(27, 450).addBox(4.85F, 4.5F, -23.55F, 1.1F, 1.2F, 1.1F)
                .texOffs(36, 450).addBox(4.85F, 4.5F, -21.55F, 1.1F, 1.2F, 1.1F)
                .texOffs(45, 450).addBox(4.85F, 4.5F, -19.55F, 1.1F, 1.2F, 1.1F)
                .texOffs(54, 450).addBox(4.85F, 4.5F, -17.55F, 1.1F, 1.62F, 1.1F)
                .texOffs(259, 450).addBox(-3.6F, -3.6F, -29.4F, 2F, 1.2F, 0.5F)
                .texOffs(178, 429).addBox(-8.15F, -4.6F, -15.9F, 1.85F, 2.2F, 3.9F)
                .texOffs(279, 317).addBox(-9.2F, -0.3F, -15F, 2F, 5.1F, 10F)
                .texOffs(350, 298).addBox(-8.5F, -1F, -14.92F, 0.6F, 6.5F, 9.84F)
                .texOffs(63, 450).addBox(-5.95F, 4.5F, -27.55F, 1.1F, 1.62F, 1.1F)
                .texOffs(72, 450).addBox(-5.95F, 4.5F, -25.55F, 1.1F, 1.2F, 1.1F)
                .texOffs(81, 450).addBox(-5.95F, 4.5F, -23.55F, 1.1F, 1.2F, 1.1F)
                .texOffs(90, 450).addBox(-5.95F, 4.5F, -21.55F, 1.1F, 1.2F, 1.1F)
                .texOffs(99, 450).addBox(-5.95F, 4.5F, -19.55F, 1.1F, 1.2F, 1.1F)
                .texOffs(108, 450).addBox(-5.95F, 4.5F, -17.55F, 1.1F, 1.62F, 1.1F)
                .texOffs(181, 350).addBox(-5.4F, 4.52F, -28F, 10.8F, 0.3F, 12F),
                PartPose.offsetAndRotation(0F, 0F, -6F, 0.4887F, 0F, 0F));
        PartDefinition p_ear_l = p_head.addOrReplaceChild("ear_l", CubeListBuilder.create()
                .texOffs(185, 334).addBox(-0.1F, -3F, 0F, 0F, 6F, 9F),
                PartPose.offsetAndRotation(7.5F, -5F, -5F, -0.3491F, 0.6109F, 0F));
        PartDefinition p_ear_r = p_head.addOrReplaceChild("ear_r", CubeListBuilder.create()
                .texOffs(204, 334).addBox(-0.1F, -3F, 0F, 0F, 6F, 9F),
                PartPose.offsetAndRotation(-7.5F, -5F, -5F, -0.3491F, -0.6109F, 0F));
        PartDefinition p_jaw = p_head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(251, 277).addBox(-6F, 1.6F, -15.5F, 12F, 1.3F, 15.5F)
                .texOffs(258, 255).addBox(-4.4F, 0F, -15.42F, 8.8F, 4.5F, 15.34F)
                .texOffs(461, 412).addBox(-7F, 1.2F, -3F, 14F, 1.8F, 4.5F)
                .texOffs(66, 391).addBox(-5.4F, -0.4F, -2.92F, 10.8F, 5F, 4.34F)
                .texOffs(266, 450).addBox(4.5F, -1.385F, -13.95F, 1F, 1.485F, 1F)
                .texOffs(271, 450).addBox(4.5F, -1F, -11.85F, 1F, 1.1F, 1F)
                .texOffs(276, 450).addBox(4.5F, -1F, -9.75F, 1F, 1.1F, 1F)
                .texOffs(281, 450).addBox(4.5F, -1F, -7.65F, 1F, 1.1F, 1F)
                .texOffs(286, 450).addBox(4.5F, -1.385F, -5.55F, 1F, 1.485F, 1F)
                .texOffs(291, 450).addBox(-5.5F, -1.385F, -13.95F, 1F, 1.485F, 1F)
                .texOffs(296, 450).addBox(-5.5F, -1F, -11.85F, 1F, 1.1F, 1F)
                .texOffs(301, 450).addBox(-5.5F, -1F, -9.75F, 1F, 1.1F, 1F)
                .texOffs(306, 450).addBox(-5.5F, -1F, -7.65F, 1F, 1.1F, 1F)
                .texOffs(311, 450).addBox(-5.5F, -1.385F, -5.55F, 1F, 1.485F, 1F)
                .texOffs(426, 298).addBox(-5.2F, -0.15F, -15.2F, 10.4F, 0.3F, 15F)
                .texOffs(119, 365).addBox(-3.6F, -0.5F, -13.5F, 7.2F, 0.6F, 11F)
                .texOffs(203, 421).addBox(-4.5F, 4.5F, -11F, 9F, 7F, 0F)
                .texOffs(190, 437).addBox(-3.2F, 4.5F, -7F, 6.4F, 5.5F, 0F),
                PartPose.offsetAndRotation(0F, 4.8F, -13F, 0.0524F, 0F, 0F));
        PartDefinition p_tusk_l = p_jaw.addOrReplaceChild("tusk_l", CubeListBuilder.create()
                .texOffs(309, 437).addBox(-0.95F, -2.086F, -1.1875F, 1.9F, 2.686F, 2.375F)
                .texOffs(117, 450).addBox(-0.6175F, -3.768F, -0.7719F, 1.235F, 1.932F, 1.5437F)
                .texOffs(316, 450).addBox(-0.285F, -5.45F, -0.3563F, 0.57F, 1.932F, 0.7125F),
                PartPose.offsetAndRotation(6.3F, 0.8F, -12.5F, 0.2094F, 0F, 0.2793F));
        PartDefinition p_tusk_r = p_jaw.addOrReplaceChild("tusk_r", CubeListBuilder.create()
                .texOffs(320, 437).addBox(-0.95F, -2.086F, -1.1875F, 1.9F, 2.686F, 2.375F)
                .texOffs(126, 450).addBox(-0.6175F, -3.768F, -0.7719F, 1.235F, 1.932F, 1.5437F)
                .texOffs(321, 450).addBox(-0.285F, -5.45F, -0.3563F, 0.57F, 1.932F, 0.7125F),
                PartPose.offsetAndRotation(-6.3F, 0.8F, -12.5F, 0.2094F, 0F, -0.2793F));
        PartDefinition p_antler_l1 = p_head.addOrReplaceChild("antler_l1", CubeListBuilder.create()
                .texOffs(433, 350).addBox(-2.2F, -2.2F, -0.4F, 4.4F, 4.4F, 7.275F)
                .texOffs(274, 378).addBox(-1.804F, -1.804F, 6.25F, 3.608F, 3.608F, 6.85F),
                PartPose.offsetAndRotation(4.8F, -8.6F, -9F, 1.04F, 0.9828F, 0F));
        PartDefinition p_antler_l2 = p_antler_l1.addOrReplaceChild("antler_l2", CubeListBuilder.create()
                .texOffs(297, 378).addBox(-1.85F, -1.85F, -0.4F, 3.7F, 3.7F, 6.45F)
                .texOffs(320, 378).addBox(-1.517F, -1.517F, 5.5F, 3.034F, 3.034F, 6.1F)
                .texOffs(23, 421).addBox(-2.7F, -1.8F, 2F, 5.4F, 1.8F, 4.5F)
                .texOffs(131, 402).addBox(-1.8F, -2.7F, 2.08F, 3.6F, 3.6F, 4.34F),
                PartPose.offsetAndRotation(0F, 0F, 12.5F, -0.439F, -0.1975F, 0F));
        PartDefinition p_antler_l3 = p_antler_l2.addOrReplaceChild("antler_l3", CubeListBuilder.create()
                .texOffs(150, 402).addBox(-1.5F, -1.5F, -0.4F, 3F, 3F, 5.9F)
                .texOffs(169, 402).addBox(-1.23F, -1.23F, 5F, 2.46F, 2.46F, 5.6F),
                PartPose.offsetAndRotation(0F, 0F, 11F, 0.3354F, -0.0243F, 0F));
        PartDefinition p_antler_l4 = p_antler_l3.addOrReplaceChild("antler_l4", CubeListBuilder.create()
                .texOffs(149, 412).addBox(-1.2F, -1.2F, -0.4F, 2.4F, 2.4F, 4.8F)
                .texOffs(366, 421).addBox(-0.984F, -0.984F, 4F, 1.968F, 1.968F, 4.6F),
                PartPose.offsetAndRotation(0F, 0F, 10F, 0.3882F, 0.071F, 0F));
        PartDefinition p_antler_l_t4 = p_antler_l4.addOrReplaceChild("antler_l_t4", CubeListBuilder.create()
                .texOffs(381, 421).addBox(-1.15F, -1.15F, -0.3F, 2.3F, 2.3F, 3.6F)
                .texOffs(231, 437).addBox(-0.8F, -0.8F, 3F, 1.6F, 1.6F, 3.4F)
                .texOffs(222, 421).addBox(-2.6F, -2.6F, 4.92F, 5.2F, 4F, 2.76F)
                .texOffs(46, 421).addBox(-2.52F, -1.48F, 3.8F, 5.04F, 1.76F, 5F),
                PartPose.offsetAndRotation(0F, 0F, 7.5F, 0.7049F, 0.6512F, 0F));
        PartDefinition p_antler_l_t3 = p_antler_l3.addOrReplaceChild("antler_l_t3", CubeListBuilder.create()
                .texOffs(396, 421).addBox(-1.15F, -1.15F, -0.3F, 2.3F, 2.3F, 3.875F)
                .texOffs(244, 437).addBox(-0.8F, -0.8F, 3.25F, 1.6F, 1.6F, 3.65F)
                .texOffs(135, 450).addBox(-0.65F, -0.65F, 6.2F, 1.3F, 1.3F, 1.3F),
                PartPose.offsetAndRotation(0F, 0F, 9F, -0.0281F, 0.7206F, 0F));
        PartDefinition p_antler_l_t2 = p_antler_l2.addOrReplaceChild("antler_l_t2", CubeListBuilder.create()
                .texOffs(166, 412).addBox(-1.15F, -1.15F, -0.3F, 2.3F, 2.3F, 4.975F)
                .texOffs(411, 421).addBox(-0.8F, -0.8F, 4.25F, 1.6F, 1.6F, 4.65F)
                .texOffs(241, 421).addBox(-2.6F, -2.6F, 7.42F, 5.2F, 4F, 2.76F)
                .texOffs(69, 421).addBox(-2.52F, -1.48F, 6.3F, 5.04F, 1.76F, 5F),
                PartPose.offsetAndRotation(0F, 0F, 7F, 0.6437F, 1.0691F, 0F));
        PartDefinition p_antler_l_t0 = p_antler_l1.addOrReplaceChild("antler_l_t0", CubeListBuilder.create()
                .texOffs(188, 402).addBox(-1.15F, -1.15F, -0.3F, 2.3F, 2.3F, 5.25F)
                .texOffs(426, 421).addBox(-0.8F, -0.8F, 4.5F, 1.6F, 1.6F, 4.9F),
                PartPose.offsetAndRotation(0F, 0F, 3F, 0.5838F, 1.3974F, 0F));
        PartDefinition p_antler_l_t1 = p_antler_l1.addOrReplaceChild("antler_l_t1", CubeListBuilder.create()
                .texOffs(183, 412).addBox(-1.15F, -1.15F, -0.3F, 2.3F, 2.3F, 4.7F)
                .texOffs(441, 421).addBox(-0.8F, -0.8F, 4F, 1.6F, 1.6F, 4.4F),
                PartPose.offsetAndRotation(0F, 0F, 9F, 0.4607F, 1.0308F, 0F));
        PartDefinition p_antler_r1 = p_head.addOrReplaceChild("antler_r1", CubeListBuilder.create()
                .texOffs(460, 350).addBox(-2.2F, -2.2F, -0.4F, 4.4F, 4.4F, 7.275F)
                .texOffs(343, 378).addBox(-1.804F, -1.804F, 6.25F, 3.608F, 3.608F, 6.85F),
                PartPose.offsetAndRotation(-4.8F, -8.6F, -9F, 1.04F, -0.9828F, 0F));
        PartDefinition p_antler_r2 = p_antler_r1.addOrReplaceChild("antler_r2", CubeListBuilder.create()
                .texOffs(366, 378).addBox(-1.85F, -1.85F, -0.4F, 3.7F, 3.7F, 6.45F)
                .texOffs(389, 378).addBox(-1.517F, -1.517F, 5.5F, 3.034F, 3.034F, 6.1F)
                .texOffs(92, 421).addBox(-2.7F, -1.8F, 2F, 5.4F, 1.8F, 4.5F)
                .texOffs(207, 402).addBox(-1.8F, -2.7F, 2.08F, 3.6F, 3.6F, 4.34F),
                PartPose.offsetAndRotation(0F, 0F, 12.5F, -0.439F, 0.1975F, 0F));
        PartDefinition p_antler_r3 = p_antler_r2.addOrReplaceChild("antler_r3", CubeListBuilder.create()
                .texOffs(226, 402).addBox(-1.5F, -1.5F, -0.4F, 3F, 3F, 5.9F)
                .texOffs(245, 402).addBox(-1.23F, -1.23F, 5F, 2.46F, 2.46F, 5.6F),
                PartPose.offsetAndRotation(0F, 0F, 11F, 0.3354F, 0.0243F, 0F));
        PartDefinition p_antler_r4 = p_antler_r3.addOrReplaceChild("antler_r4", CubeListBuilder.create()
                .texOffs(200, 412).addBox(-1.2F, -1.2F, -0.4F, 2.4F, 2.4F, 4.8F)
                .texOffs(456, 421).addBox(-0.984F, -0.984F, 4F, 1.968F, 1.968F, 4.6F),
                PartPose.offsetAndRotation(0F, 0F, 10F, 0.3882F, -0.071F, 0F));
        PartDefinition p_antler_r_t4 = p_antler_r4.addOrReplaceChild("antler_r_t4", CubeListBuilder.create()
                .texOffs(471, 421).addBox(-1.15F, -1.15F, -0.3F, 2.3F, 2.3F, 3.6F)
                .texOffs(257, 437).addBox(-0.8F, -0.8F, 3F, 1.6F, 1.6F, 3.4F)
                .texOffs(260, 421).addBox(-2.6F, -2.6F, 4.92F, 5.2F, 4F, 2.76F)
                .texOffs(115, 421).addBox(-2.52F, -1.48F, 3.8F, 5.04F, 1.76F, 5F),
                PartPose.offsetAndRotation(0F, 0F, 7.5F, 0.7049F, -0.6512F, 0F));
        PartDefinition p_antler_r_t3 = p_antler_r3.addOrReplaceChild("antler_r_t3", CubeListBuilder.create()
                .texOffs(486, 421).addBox(-1.15F, -1.15F, -0.3F, 2.3F, 2.3F, 3.875F)
                .texOffs(270, 437).addBox(-0.8F, -0.8F, 3.25F, 1.6F, 1.6F, 3.65F)
                .texOffs(144, 450).addBox(-0.65F, -0.65F, 6.2F, 1.3F, 1.3F, 1.3F),
                PartPose.offsetAndRotation(0F, 0F, 9F, -0.0281F, -0.7206F, 0F));
        PartDefinition p_antler_r_t2 = p_antler_r2.addOrReplaceChild("antler_r_t2", CubeListBuilder.create()
                .texOffs(217, 412).addBox(-1.15F, -1.15F, -0.3F, 2.3F, 2.3F, 4.975F)
                .texOffs(0, 429).addBox(-0.8F, -0.8F, 4.25F, 1.6F, 1.6F, 4.65F)
                .texOffs(279, 421).addBox(-2.6F, -2.6F, 7.42F, 5.2F, 4F, 2.76F)
                .texOffs(138, 421).addBox(-2.52F, -1.48F, 6.3F, 5.04F, 1.76F, 5F),
                PartPose.offsetAndRotation(0F, 0F, 7F, 0.6437F, -1.0691F, 0F));
        PartDefinition p_antler_r_t0 = p_antler_r1.addOrReplaceChild("antler_r_t0", CubeListBuilder.create()
                .texOffs(264, 402).addBox(-1.15F, -1.15F, -0.3F, 2.3F, 2.3F, 5.25F)
                .texOffs(15, 429).addBox(-0.8F, -0.8F, 4.5F, 1.6F, 1.6F, 4.9F),
                PartPose.offsetAndRotation(0F, 0F, 3F, 0.5838F, -1.3974F, 0F));
        PartDefinition p_antler_r_t1 = p_antler_r1.addOrReplaceChild("antler_r_t1", CubeListBuilder.create()
                .texOffs(234, 412).addBox(-1.15F, -1.15F, -0.3F, 2.3F, 2.3F, 4.7F)
                .texOffs(30, 429).addBox(-0.8F, -0.8F, 4F, 1.6F, 1.6F, 4.4F),
                PartPose.offsetAndRotation(0F, 0F, 9F, 0.4607F, -1.0308F, 0F));
        PartDefinition p_tail1 = p_body.addOrReplaceChild("tail1", CubeListBuilder.create()
                .texOffs(401, 255).addBox(-7.5F, -2.6F, -0.6F, 15F, 5.2F, 13.2F)
                .texOffs(114, 175).addBox(-3.6F, -6.5F, -0.52F, 7.2F, 13F, 13.04F)
                .texOffs(0, 350).addBox(-3.6F, 5.5F, 0F, 7.2F, 1.6F, 12F)
                .texOffs(223, 334).addBox(-6.3F, -7.6F, -0.4F, 12.6F, 0.8F, 12.4F)
                .texOffs(0, 317).addBox(-5.4F, -8.5F, -0.32F, 10.8F, 2.6F, 12.24F),
                PartPose.offsetAndRotation(0F, -4F, 22F, -0.2094F, 0F, 0F));
        PartDefinition p_tail2 = p_tail1.addOrReplaceChild("tail2", CubeListBuilder.create()
                .texOffs(418, 277).addBox(-6.5F, -2.2F, -0.6F, 13F, 4.4F, 12.2F)
                .texOffs(354, 205).addBox(-3.2F, -5.5F, -0.52F, 6.4F, 11F, 12.04F)
                .texOffs(361, 350).addBox(-3.2F, 4.5F, 0F, 6.4F, 1.6F, 11F)
                .texOffs(228, 350).addBox(-5.3F, -6.6F, -0.4F, 10.6F, 0.8F, 11.4F)
                .texOffs(457, 317).addBox(-4.4F, -7.5F, -0.32F, 8.8F, 2.6F, 11.24F),
                PartPose.offsetAndRotation(0F, 0F, 12F, -0.1396F, 0F, 0F));
        PartDefinition p_tail3 = p_tail2.addOrReplaceChild("tail3", CubeListBuilder.create()
                .texOffs(155, 298).addBox(-5.5F, -1.9F, -0.6F, 11F, 3.8F, 12.2F)
                .texOffs(65, 231).addBox(-2.65F, -4.75F, -0.52F, 5.3F, 9.5F, 12.04F)
                .texOffs(398, 350).addBox(-2.65F, 3.75F, 0F, 5.3F, 1.6F, 11F)
                .texOffs(318, 350).addBox(-4.3F, -5.85F, -0.4F, 8.6F, 0.8F, 11.4F)
                .texOffs(41, 334).addBox(-3.4F, -6.75F, -0.32F, 6.8F, 2.6F, 11.24F),
                PartPose.offsetAndRotation(0F, 0F, 11F, -0.0698F, 0F, 0F));
        PartDefinition p_tail4 = p_tail3.addOrReplaceChild("tail4", CubeListBuilder.create()
                .texOffs(49, 317).addBox(-4.75F, -1.6F, -0.6F, 9.5F, 3.2F, 11.2F)
                .texOffs(59, 277).addBox(-2.35F, -4F, -0.52F, 4.7F, 8F, 11.04F)
                .texOffs(0, 378).addBox(-2.35F, 3F, 0F, 4.7F, 1.6F, 10F)
                .texOffs(158, 365).addBox(-3.55F, -5.1F, -0.4F, 7.1F, 0.8F, 10.4F)
                .texOffs(115, 350).addBox(-2.65F, -6F, -0.32F, 5.3F, 2.6F, 10.24F),
                PartPose.offset(0F, 0F, 11F));
        PartDefinition p_tail5 = p_tail4.addOrReplaceChild("tail5", CubeListBuilder.create()
                .texOffs(0, 334).addBox(-4F, -1.4F, -0.6F, 8F, 2.8F, 11.2F)
                .texOffs(160, 277).addBox(-1.9F, -3.5F, -0.52F, 3.8F, 7F, 11.04F)
                .texOffs(31, 378).addBox(-1.9F, 2.5F, 0F, 3.8F, 1.6F, 10F)
                .texOffs(343, 365).addBox(-2.8F, -4.6F, -0.4F, 5.6F, 0.8F, 10.4F)
                .texOffs(150, 350).addBox(-1.9F, -5.5F, -0.32F, 3.8F, 2.6F, 10.24F),
                PartPose.offsetAndRotation(0F, 0F, 10F, 0.0524F, 0F, 0F));
        PartDefinition p_tail6 = p_tail5.addOrReplaceChild("tail6", CubeListBuilder.create()
                .texOffs(78, 350).addBox(-3.25F, -1.2F, -0.6F, 6.5F, 2.4F, 10.2F)
                .texOffs(298, 298).addBox(-1.45F, -3F, -0.52F, 2.9F, 6F, 10.04F)
                .texOffs(249, 378).addBox(-1.45F, 2F, 0F, 2.9F, 1.6F, 9F),
                PartPose.offsetAndRotation(0F, 0F, 10F, 0.0698F, 0F, 0F));
        PartDefinition p_tail7 = p_tail6.addOrReplaceChild("tail7", CubeListBuilder.create()
                .texOffs(378, 365).addBox(-2.75F, -1F, -0.6F, 5.5F, 2F, 9.2F)
                .texOffs(158, 334).addBox(-1.25F, -2.5F, -0.52F, 2.5F, 5F, 9.04F)
                .texOffs(182, 391).addBox(-1.25F, 1.5F, 0F, 2.5F, 1.6F, 8F)
                .texOffs(94, 317).addBox(-5.5F, -2.9F, 4.5F, 11F, 5.8F, 10F)
                .texOffs(309, 255).addBox(-2.9F, -5.5F, 4.58F, 5.8F, 11F, 9.84F)
                .texOffs(467, 391).addBox(-4.2F, -6.8F, 6F, 8.4F, 1.8F, 7F)
                .texOffs(153, 450).addBox(-5.9F, -2F, 7F, 1.6F, 1.6F, 1.6F)
                .texOffs(162, 450).addBox(4.3F, 1F, 9F, 1.6F, 1.6F, 1.6F)
                .texOffs(171, 450).addBox(-1F, -7.4F, 10.5F, 1.6F, 1.6F, 1.6F),
                PartPose.offsetAndRotation(0F, 0F, 9F, 0.0524F, 0F, 0F));
        PartDefinition p_tailfrond_0 = p_tail7.addOrReplaceChild("tailfrond_0", CubeListBuilder.create()
                .texOffs(308, 277).addBox(-4.5F, 0F, 0F, 9F, 0F, 18F),
                PartPose.offsetAndRotation(0F, 0F, 13F, -0.2094F, -0.733F, 0F));
        PartDefinition p_tailfrond_1 = p_tail7.addOrReplaceChild("tailfrond_1", CubeListBuilder.create()
                .texOffs(221, 231).addBox(-4.5F, 0F, 0F, 9F, 0F, 22F),
                PartPose.offsetAndRotation(0F, 0F, 13F, -0.4887F, -0.2618F, 0F));
        PartDefinition p_tailfrond_2 = p_tail7.addOrReplaceChild("tailfrond_2", CubeListBuilder.create()
                .texOffs(284, 231).addBox(-4.5F, 0F, 0F, 9F, 0F, 22F),
                PartPose.offsetAndRotation(0F, 0F, 13F, -0.4887F, 0.2618F, 0F));
        PartDefinition p_tailfrond_3 = p_tail7.addOrReplaceChild("tailfrond_3", CubeListBuilder.create()
                .texOffs(363, 277).addBox(-4.5F, 0F, 0F, 9F, 0F, 18F),
                PartPose.offsetAndRotation(0F, 0F, 13F, -0.2094F, 0.733F, 0F));
        PartDefinition p_tailfrond_4 = p_tail7.addOrReplaceChild("tailfrond_4", CubeListBuilder.create()
                .texOffs(0, 277).addBox(-4.5F, 0F, 0F, 9F, 0F, 20F),
                PartPose.offsetAndRotation(0F, 0F, 13F, 0.1396F, 0F, 0F));
        PartDefinition p_front_leg_l = p_body.addOrReplaceChild("front_leg_l", CubeListBuilder.create()
                .texOffs(457, 175).addBox(-5.5F, -4.4F, -3.08F, 11F, 17.4F, 6.16F)
                .texOffs(444, 144).addBox(-3.08F, -4.32F, -5.5F, 6.16F, 17.24F, 11F)
                .texOffs(80, 334).addBox(-6.1F, -6.05F, -2.8F, 12.2F, 8.25F, 5.6F)
                .texOffs(347, 231).addBox(-2.8F, -5.97F, -6.1F, 5.6F, 8.09F, 12.2F),
                PartPose.offsetAndRotation(12.5F, 4F, -15F, 0.1396F, 0F, 0F));
        PartDefinition p_front_shin_l = p_front_leg_l.addOrReplaceChild("front_shin_l", CubeListBuilder.create()
                .texOffs(193, 277).addBox(-4.25F, -1.4F, -2.38F, 8.5F, 13.1F, 4.76F)
                .texOffs(454, 231).addBox(-2.38F, -1.32F, -4.25F, 4.76F, 12.94F, 8.5F),
                PartPose.offsetAndRotation(0F, 12.4F, 0F, -0.2443F, 0F, 0F));
        PartDefinition p_front_foot_l = p_front_shin_l.addOrReplaceChild("front_foot_l", CubeListBuilder.create()
                .texOffs(276, 334).addBox(-5.5F, 1.05F, -8.4F, 11F, 1.4F, 12F)
                .texOffs(137, 317).addBox(-4.45F, 0F, -8.32F, 8.9F, 3.5F, 11.84F)
                .texOffs(45, 429).addBox(-5.3F, 1.6F, -12.3F, 1.9F, 1.9F, 5F)
                .texOffs(180, 450).addBox(-5.015F, 2.075F, -13.9F, 1.33F, 1.71F, 1.9F)
                .texOffs(60, 429).addBox(-2.4F, 1.6F, -12.3F, 1.9F, 1.9F, 5F)
                .texOffs(189, 450).addBox(-2.115F, 2.075F, -13.9F, 1.33F, 1.71F, 1.9F)
                .texOffs(75, 429).addBox(0.5F, 1.6F, -12.3F, 1.9F, 1.9F, 5F)
                .texOffs(198, 450).addBox(0.785F, 2.075F, -13.9F, 1.33F, 1.71F, 1.9F)
                .texOffs(90, 429).addBox(3.4F, 1.6F, -12.3F, 1.9F, 1.9F, 5F)
                .texOffs(207, 450).addBox(3.685F, 2.075F, -13.9F, 1.33F, 1.71F, 1.9F),
                PartPose.offsetAndRotation(0F, 10.9F, 0F, 0.1047F, 0F, 0F));
        PartDefinition p_hind_leg_l = p_body.addOrReplaceChild("hind_leg_l", CubeListBuilder.create()
                .texOffs(204, 144).addBox(-6.5F, -5.2F, -4.14F, 13F, 20.2F, 8.28F)
                .texOffs(0, 108).addBox(-3.64F, -5.12F, -7F, 7.28F, 20.04F, 14F)
                .texOffs(0, 298).addBox(-7.1F, -7.15F, -3.7F, 14.2F, 9.75F, 7.4F)
                .texOffs(159, 175).addBox(-3.2F, -7.07F, -7.6F, 6.4F, 9.59F, 15.2F),
                PartPose.offsetAndRotation(12.5F, 2F, 14F, -0.2094F, 0F, 0F));
        PartDefinition p_hind_shin_l = p_hind_leg_l.addOrReplaceChild("hind_shin_l", CubeListBuilder.create()
                .texOffs(94, 277).addBox(-4.75F, -1.4F, -2.66F, 9.5F, 13.6F, 5.32F)
                .texOffs(428, 205).addBox(-2.66F, -1.32F, -4.75F, 5.32F, 13.44F, 9.5F),
                PartPose.offsetAndRotation(0F, 14.4F, 0F, 0.5934F, 0F, 0F));
        PartDefinition p_hind_foot_l = p_hind_shin_l.addOrReplaceChild("hind_foot_l", CubeListBuilder.create()
                .texOffs(355, 317).addBox(-5.75F, 1.05F, -9.1F, 11.5F, 1.4F, 13F)
                .texOffs(204, 298).addBox(-4.7F, 0F, -9.02F, 9.4F, 3.5F, 12.84F)
                .texOffs(251, 412).addBox(-5.4875F, 1.6F, -13.5F, 1.9F, 1.9F, 5.5F)
                .texOffs(475, 437).addBox(-5.2025F, 2.075F, -15.3F, 1.33F, 1.71F, 2.1F)
                .texOffs(268, 412).addBox(-2.4625F, 1.6F, -13.5F, 1.9F, 1.9F, 5.5F)
                .texOffs(486, 437).addBox(-2.1775F, 2.075F, -15.3F, 1.33F, 1.71F, 2.1F)
                .texOffs(285, 412).addBox(0.5625F, 1.6F, -13.5F, 1.9F, 1.9F, 5.5F)
                .texOffs(497, 437).addBox(0.8475F, 2.075F, -15.3F, 1.33F, 1.71F, 2.1F)
                .texOffs(302, 412).addBox(3.5875F, 1.6F, -13.5F, 1.9F, 1.9F, 5.5F)
                .texOffs(0, 444).addBox(3.8725F, 2.075F, -15.3F, 1.33F, 1.71F, 2.1F),
                PartPose.offsetAndRotation(0F, 11.4F, 0F, -0.384F, 0F, 0F));
        PartDefinition p_front_leg_r = p_body.addOrReplaceChild("front_leg_r", CubeListBuilder.create()
                .texOffs(0, 205).addBox(-5.5F, -4.4F, -3.08F, 11F, 17.4F, 6.16F)
                .texOffs(0, 175).addBox(-3.08F, -4.32F, -5.5F, 6.16F, 17.24F, 11F)
                .texOffs(119, 334).addBox(-6.1F, -6.05F, -2.8F, 12.2F, 8.25F, 5.6F)
                .texOffs(386, 231).addBox(-2.8F, -5.97F, -6.1F, 5.6F, 8.09F, 12.2F),
                PartPose.offsetAndRotation(-12.5F, 4F, -15F, 0.1396F, 0F, 0F));
        PartDefinition p_front_shin_r = p_front_leg_r.addOrReplaceChild("front_shin_r", CubeListBuilder.create()
                .texOffs(222, 277).addBox(-4.25F, -1.4F, -2.38F, 8.5F, 13.1F, 4.76F)
                .texOffs(483, 231).addBox(-2.38F, -1.32F, -4.25F, 4.76F, 12.94F, 8.5F),
                PartPose.offsetAndRotation(0F, 12.4F, 0F, -0.2443F, 0F, 0F));
        PartDefinition p_front_foot_r = p_front_shin_r.addOrReplaceChild("front_foot_r", CubeListBuilder.create()
                .texOffs(323, 334).addBox(-5.5F, 1.05F, -8.4F, 11F, 1.4F, 12F)
                .texOffs(180, 317).addBox(-4.45F, 0F, -8.32F, 8.9F, 3.5F, 11.84F)
                .texOffs(105, 429).addBox(-5.3F, 1.6F, -12.3F, 1.9F, 1.9F, 5F)
                .texOffs(216, 450).addBox(-5.015F, 2.075F, -13.9F, 1.33F, 1.71F, 1.9F)
                .texOffs(120, 429).addBox(-2.4F, 1.6F, -12.3F, 1.9F, 1.9F, 5F)
                .texOffs(225, 450).addBox(-2.115F, 2.075F, -13.9F, 1.33F, 1.71F, 1.9F)
                .texOffs(135, 429).addBox(0.5F, 1.6F, -12.3F, 1.9F, 1.9F, 5F)
                .texOffs(234, 450).addBox(0.785F, 2.075F, -13.9F, 1.33F, 1.71F, 1.9F)
                .texOffs(150, 429).addBox(3.4F, 1.6F, -12.3F, 1.9F, 1.9F, 5F)
                .texOffs(243, 450).addBox(3.685F, 2.075F, -13.9F, 1.33F, 1.71F, 1.9F),
                PartPose.offsetAndRotation(0F, 10.9F, 0F, 0.1047F, 0F, 0F));
        PartDefinition p_hind_leg_r = p_body.addOrReplaceChild("hind_leg_r", CubeListBuilder.create()
                .texOffs(249, 144).addBox(-6.5F, -5.2F, -4.14F, 13F, 20.2F, 8.28F)
                .texOffs(45, 108).addBox(-3.64F, -5.12F, -7F, 7.28F, 20.04F, 14F)
                .texOffs(47, 298).addBox(-7.1F, -7.15F, -3.7F, 14.2F, 9.75F, 7.4F)
                .texOffs(206, 175).addBox(-3.2F, -7.07F, -7.6F, 6.4F, 9.59F, 15.2F),
                PartPose.offsetAndRotation(-12.5F, 2F, 14F, -0.2094F, 0F, 0F));
        PartDefinition p_hind_shin_r = p_hind_leg_r.addOrReplaceChild("hind_shin_r", CubeListBuilder.create()
                .texOffs(127, 277).addBox(-4.75F, -1.4F, -2.66F, 9.5F, 13.6F, 5.32F)
                .texOffs(461, 205).addBox(-2.66F, -1.32F, -4.75F, 5.32F, 13.44F, 9.5F),
                PartPose.offsetAndRotation(0F, 14.4F, 0F, 0.5934F, 0F, 0F));
        PartDefinition p_hind_foot_r = p_hind_shin_r.addOrReplaceChild("hind_foot_r", CubeListBuilder.create()
                .texOffs(406, 317).addBox(-5.75F, 1.05F, -9.1F, 11.5F, 1.4F, 13F)
                .texOffs(251, 298).addBox(-4.7F, 0F, -9.02F, 9.4F, 3.5F, 12.84F)
                .texOffs(319, 412).addBox(-5.4875F, 1.6F, -13.5F, 1.9F, 1.9F, 5.5F)
                .texOffs(11, 444).addBox(-5.2025F, 2.075F, -15.3F, 1.33F, 1.71F, 2.1F)
                .texOffs(336, 412).addBox(-2.4625F, 1.6F, -13.5F, 1.9F, 1.9F, 5.5F)
                .texOffs(22, 444).addBox(-2.1775F, 2.075F, -15.3F, 1.33F, 1.71F, 2.1F)
                .texOffs(353, 412).addBox(0.5625F, 1.6F, -13.5F, 1.9F, 1.9F, 5.5F)
                .texOffs(33, 444).addBox(0.8475F, 2.075F, -15.3F, 1.33F, 1.71F, 2.1F)
                .texOffs(370, 412).addBox(3.5875F, 1.6F, -13.5F, 1.9F, 1.9F, 5.5F)
                .texOffs(44, 444).addBox(3.8725F, 2.075F, -15.3F, 1.33F, 1.71F, 2.1F),
                PartPose.offsetAndRotation(0F, 11.4F, 0F, -0.384F, 0F, 0F));
        PartDefinition p_wing_l = p_body.addOrReplaceChild("wing_l", CubeListBuilder.create()
                .texOffs(397, 391).addBox(-1F, -3F, -1.32F, 13.1F, 6F, 2.64F)
                .texOffs(284, 391).addBox(-0.92F, -1.32F, -3F, 12.94F, 2.64F, 6F)
                .texOffs(132, 437).addBox(11F, -2F, -0.88F, 11.8F, 4F, 1.76F)
                .texOffs(0, 437).addBox(11.08F, -0.88F, -2F, 11.64F, 1.76F, 4F)
                .texOffs(324, 108).addBox(0F, 0F, 1.5F, 22F, 0F, 30F),
                PartPose.offset(13F, -11F, -12F));
        PartDefinition p_wing_l_fore = p_wing_l.addOrReplaceChild("wing_l_fore", CubeListBuilder.create()
                .texOffs(33, 437).addBox(-0.6F, -1.95F, -0.858F, 13.6F, 3.9F, 1.716F)
                .texOffs(416, 429).addBox(-0.52F, -0.858F, -1.95F, 13.44F, 1.716F, 3.9F)
                .texOffs(405, 437).addBox(11.7F, -1.375F, -0.605F, 14.9F, 2.75F, 1.21F)
                .texOffs(331, 437).addBox(11.78F, -0.605F, -1.375F, 14.74F, 1.21F, 2.75F)
                .texOffs(90, 108).addBox(0F, 0F, 0.875F, 26F, 0F, 32F),
                PartPose.offset(22F, 0F, 0F));
        PartDefinition p_wing_l_hand = p_wing_l_fore.addOrReplaceChild("wing_l_hand", CubeListBuilder.create()
                .texOffs(387, 412).addBox(-2.375F, -2.375F, -1.1875F, 4.75F, 4.75F, 2.375F)
                .texOffs(69, 412).addBox(-2.295F, -1.1875F, -2.375F, 4.59F, 2.375F, 4.75F)
                .texOffs(283, 437).addBox(-0.9F, -0.9F, -5.575F, 1.8F, 1.8F, 3.4F)
                .texOffs(55, 444).addBox(-0.6F, -0.5F, -7.775F, 1.2F, 1.5F, 2.4F),
                PartPose.offset(26F, 0F, 0F));
        PartDefinition p_wing_l_f1 = p_wing_l_hand.addOrReplaceChild("wing_l_f1", CubeListBuilder.create()
                .texOffs(200, 429).addBox(0F, -1.3F, -1.3F, 23.92F, 2.6F, 2.6F)
                .texOffs(77, 444).addBox(23F, -0.78F, -0.78F, 23F, 1.56F, 1.56F)
                .texOffs(72, 205).addBox(0F, 0F, 0.48F, 46F, 0F, 24F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, -0.1745F, 0F));
        PartDefinition p_wing_l_f2 = p_wing_l_hand.addOrReplaceChild("wing_l_f2", CubeListBuilder.create()
                .texOffs(310, 429).addBox(0F, -1.15F, -1.15F, 22.36F, 2.3F, 2.3F)
                .texOffs(179, 444).addBox(21.5F, -0.69F, -0.69F, 21.5F, 1.38F, 1.38F)
                .texOffs(0, 255).addBox(0F, 0F, 0.39F, 43F, 0F, 21F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, -0.733F, 0F));
        PartDefinition p_wing_l_f3 = p_wing_l_hand.addOrReplaceChild("wing_l_f3", CubeListBuilder.create()
                .texOffs(277, 444).addBox(0F, -1F, -1F, 18.72F, 2F, 2F)
                .texOffs(363, 444).addBox(18F, -0.6F, -0.6F, 18F, 1.2F, 1.2F)
                .texOffs(121, 69).addBox(0F, 0F, 0.3F, 36F, 0F, 35F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, -1.2915F, 0F));
        PartDefinition p_wing_r = p_body.addOrReplaceChild("wing_r", CubeListBuilder.create()
                .texOffs(432, 391).addBox(-12.1F, -3F, -1.32F, 13.1F, 6F, 2.64F)
                .texOffs(323, 391).addBox(-12.02F, -1.32F, -3F, 12.94F, 2.64F, 6F)
                .texOffs(161, 437).addBox(-22.8F, -2F, -0.88F, 11.8F, 4F, 1.76F)
                .texOffs(66, 437).addBox(-22.72F, -0.88F, -2F, 11.64F, 1.76F, 4F)
                .texOffs(0, 144).addBox(-22F, 0F, 1.5F, 22F, 0F, 30F),
                PartPose.offset(-13F, -11F, -12F));
        PartDefinition p_wing_r_fore = p_wing_r.addOrReplaceChild("wing_r_fore", CubeListBuilder.create()
                .texOffs(99, 437).addBox(-13F, -1.95F, -0.858F, 13.6F, 3.9F, 1.716F)
                .texOffs(453, 429).addBox(-12.92F, -0.858F, -1.95F, 13.44F, 1.716F, 3.9F)
                .texOffs(440, 437).addBox(-26.6F, -1.375F, -0.605F, 14.9F, 2.75F, 1.21F)
                .texOffs(368, 437).addBox(-26.52F, -0.605F, -1.375F, 14.74F, 1.21F, 2.75F)
                .texOffs(207, 108).addBox(-26F, 0F, 0.875F, 26F, 0F, 32F),
                PartPose.offset(-22F, 0F, 0F));
        PartDefinition p_wing_r_hand = p_wing_r_fore.addOrReplaceChild("wing_r_hand", CubeListBuilder.create()
                .texOffs(404, 412).addBox(-2.375F, -2.375F, -1.1875F, 4.75F, 4.75F, 2.375F)
                .texOffs(90, 412).addBox(-2.295F, -1.1875F, -2.375F, 4.59F, 2.375F, 4.75F)
                .texOffs(296, 437).addBox(-0.9F, -0.9F, -5.575F, 1.8F, 1.8F, 3.4F)
                .texOffs(66, 444).addBox(-0.6F, -0.5F, -7.775F, 1.2F, 1.5F, 2.4F),
                PartPose.offset(-26F, 0F, 0F));
        PartDefinition p_wing_r_f1 = p_wing_r_hand.addOrReplaceChild("wing_r_f1", CubeListBuilder.create()
                .texOffs(255, 429).addBox(-23.92F, -1.3F, -1.3F, 23.92F, 2.6F, 2.6F)
                .texOffs(128, 444).addBox(-46F, -0.78F, -0.78F, 23F, 1.56F, 1.56F)
                .texOffs(213, 205).addBox(-46F, 0F, 0.48F, 46F, 0F, 24F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0.1745F, 0F));
        PartDefinition p_wing_r_f2 = p_wing_r_hand.addOrReplaceChild("wing_r_f2", CubeListBuilder.create()
                .texOffs(363, 429).addBox(-22.36F, -1.15F, -1.15F, 22.36F, 2.3F, 2.3F)
                .texOffs(228, 444).addBox(-43F, -0.69F, -0.69F, 21.5F, 1.38F, 1.38F)
                .texOffs(129, 255).addBox(-43F, 0F, 0.39F, 43F, 0F, 21F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0.733F, 0F));
        PartDefinition p_wing_r_f3 = p_wing_r_hand.addOrReplaceChild("wing_r_f3", CubeListBuilder.create()
                .texOffs(320, 444).addBox(-18.72F, -1F, -1F, 18.72F, 2F, 2F)
                .texOffs(404, 444).addBox(-36F, -0.6F, -0.6F, 18F, 1.2F, 1.2F)
                .texOffs(264, 69).addBox(-36F, 0F, 0.3F, 36F, 0F, 35F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 1.2915F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
