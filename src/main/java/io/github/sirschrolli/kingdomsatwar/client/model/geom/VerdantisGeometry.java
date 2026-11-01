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
                .texOffs(0, 0).addBox(-9.5F, -12F, -23F, 19F, 22F, 46F, new CubeDeformation(0F, 0F, -0.08F))
                .texOffs(105, 144).addBox(-16F, -7.5F, -24F, 32F, 13F, 17F)
                .texOffs(395, 0).addBox(-10.5F, -13F, -24F, 21F, 24F, 17F, new CubeDeformation(0F, 0F, -0.08F))
                .texOffs(287, 175).addBox(-15F, -7.5F, 9F, 30F, 11F, 14F)
                .texOffs(407, 69).addBox(-10F, -12.5F, 9F, 20F, 21F, 14F, new CubeDeformation(0F, 0F, -0.08F))
                .texOffs(0, 69).addBox(-11.5F, -14F, -20F, 23F, 1F, 37F)
                .texOffs(282, 0).addBox(-9.5F, -16F, -20F, 19F, 5F, 37F, new CubeDeformation(0F, 0F, -0.08F))
                .texOffs(376, 175).addBox(-8F, -17.2F, -14F, 16F, 1F, 24F, new CubeDeformation(0F, -0.2F, 0F))
                .texOffs(37, 175).addBox(-7F, -18.2F, -14F, 14F, 3F, 24F, new CubeDeformation(-0.2F, 0F, -0.08F))
                .texOffs(134, 231).addBox(-12F, 9.3F, -23F, 24F, 3F, 19F, new CubeDeformation(0F, -0.2F, 0F))
                .texOffs(294, 144).addBox(-11F, 8.35F, -4F, 22F, 3F, 26F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(220, 378).addBox(-10F, -21.5F, -16F, 8F, 5F, 6F, new CubeDeformation(0F, 0F, -0.4F))
                .texOffs(156, 378).addBox(-10F, -20.5F, -17F, 8F, 3F, 8F, new CubeDeformation(-0.08F, -0.4F, 0F))
                .texOffs(411, 365).addBox(2F, -22.5F, -9.5F, 9F, 6F, 6F, new CubeDeformation(0F, 0F, -0.18F))
                .texOffs(197, 365).addBox(2F, -21F, -11F, 9F, 3F, 9F, new CubeDeformation(-0.08F, -0.18F, 0F))
                .texOffs(157, 391).addBox(-7F, -21.25F, -1F, 7F, 5F, 5F, new CubeDeformation(0F, -0.25F, -0.26F))
                .texOffs(0, 402).addBox(-7F, -19.75F, -2F, 7F, 2F, 7F, new CubeDeformation(-0.08F, -0.01F, 0F))
                .texOffs(130, 391).addBox(4F, -20.5F, 6F, 8F, 5F, 5F, new CubeDeformation(0F, 0F, -0.4F))
                .texOffs(99, 391).addBox(4F, -19.5F, 5F, 8F, 3F, 7F, new CubeDeformation(-0.08F, -0.4F, 0F))
                .texOffs(0, 412).addBox(-4F, -19.5F, 13F, 7F, 4F, 4F, new CubeDeformation(0F, 0F, -0.12F))
                .texOffs(344, 402).addBox(-4F, -18.5F, 12F, 7F, 2F, 6F, new CubeDeformation(-0.08F, -0.12F, 0F))
                .texOffs(445, 444).addBox(-7.3F, -22.9F, -15.3F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.3F, -0.3F))
                .texOffs(454, 444).addBox(4.7F, -24.6F, -8.3F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.3F, -0.3F))
                .texOffs(463, 444).addBox(-4.8F, -22.7F, -0.3F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.3F, -0.3F))
                .texOffs(472, 444).addBox(6.7F, -22.1F, 6.7F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.3F, -0.3F))
                .texOffs(481, 444).addBox(-1.8F, -21.1F, 13.7F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.3F, -0.3F))
                .texOffs(490, 444).addBox(2.7F, -24.6F, -12.3F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.3F, -0.3F))
                .texOffs(298, 421).addBox(15.6F, -4.4F, -8F, 3F, 2F, 5F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(111, 412).addBox(15.6F, -6.4F, 9F, 3F, 2F, 6F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(205, 437).addBox(15.6F, -0.4F, 13.75F, 2F, 2F, 4F, new CubeDeformation(0F, -0.4F, -0.25F))
                .texOffs(315, 421).addBox(-18.6F, -4.4F, -8F, 3F, 2F, 5F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(130, 412).addBox(-18.6F, -6.4F, 9F, 3F, 2F, 6F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(218, 437).addBox(-17.6F, -0.4F, 13.75F, 2F, 2F, 4F, new CubeDeformation(0F, -0.4F, -0.25F)),
                PartPose.offset(0F, -8F, 0F));
        PartDefinition p_tree_0 = p_body.addOrReplaceChild("tree_0", CubeListBuilder.create()
                .texOffs(205, 391).addBox(-1.5F, -5.75F, -1.5F, 3F, 7F, 3F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(218, 391).addBox(-1.5F, -11.975F, -1.5F, 3F, 7F, 3F, new CubeDeformation(-0.4F, -0.475F, -0.4F))
                .texOffs(442, 365).addBox(-4.5F, -15.9F, -3F, 9F, 6F, 6F, new CubeDeformation(0F, -0.4F, -0.256F))
                .texOffs(234, 365).addBox(-4.5F, -14.4F, -4.5F, 9F, 3F, 9F, new CubeDeformation(-0.08F, -0.356F, -0.3F))
                .texOffs(23, 412).addBox(-3.3F, -18.8F, -1.7F, 7F, 4F, 4F, new CubeDeformation(-0.3F, -0.2F, -0.208F))
                .texOffs(371, 402).addBox(-3.3F, -17.8F, -2.7F, 7F, 2F, 6F, new CubeDeformation(-0.38F, -0.208F, -0.2F))
                .texOffs(332, 421).addBox(1.5F, -14.2F, 0F, 5F, 4F, 3F, new CubeDeformation(0F, -0.2F, -0.008F))
                .texOffs(161, 421).addBox(1.5F, -13.2F, -1F, 5F, 2F, 5F, new CubeDeformation(-0.08F, -0.208F, 0F))
                .texOffs(499, 444).addBox(-2.4F, -19.4F, -0.8F, 2F, 2F, 2F, new CubeDeformation(-0.2F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(-4F, -18F, -14F, -0.1745F, -0.3491F, 0F));
        PartDefinition p_tree_0_bough = p_tree_0.addOrReplaceChild("tree_0_bough", CubeListBuilder.create()
                .texOffs(434, 412).addBox(-1F, -5.5F, -1F, 2F, 6F, 2F, new CubeDeformation(-0.2F, 0F, -0.2F))
                .texOffs(85, 402).addBox(-3.5F, -9.3F, -2F, 7F, 5F, 4F, new CubeDeformation(-0.3F, -0.3F, -0.232F))
                .texOffs(398, 402).addBox(-3.5F, -7.8F, -3F, 7F, 2F, 6F, new CubeDeformation(-0.38F, -0.032F, 0F)),
                PartPose.offsetAndRotation(0F, -6.05F, 0F, 0F, 0F, 0.733F));
        PartDefinition p_tree_1 = p_body.addOrReplaceChild("tree_1", CubeListBuilder.create()
                .texOffs(283, 402).addBox(-1.5F, -4.75F, -1.5F, 3F, 6F, 3F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(421, 412).addBox(-1.5F, -9.525F, -1.5F, 3F, 5F, 3F, new CubeDeformation(-0.4F, -0.025F, -0.4F))
                .texOffs(473, 365).addBox(-4.5F, -13.9F, -3F, 9F, 6F, 6F, new CubeDeformation(0F, -0.4F, -0.256F))
                .texOffs(271, 365).addBox(-4.5F, -12.4F, -4.5F, 9F, 3F, 9F, new CubeDeformation(-0.08F, -0.356F, -0.3F))
                .texOffs(46, 412).addBox(-3.3F, -16.8F, -1.7F, 7F, 4F, 4F, new CubeDeformation(-0.3F, -0.2F, -0.208F))
                .texOffs(425, 402).addBox(-3.3F, -15.8F, -2.7F, 7F, 2F, 6F, new CubeDeformation(-0.38F, -0.208F, -0.2F))
                .texOffs(349, 421).addBox(1.5F, -12.2F, 0F, 5F, 4F, 3F, new CubeDeformation(0F, -0.2F, -0.008F))
                .texOffs(182, 421).addBox(1.5F, -11.2F, -1F, 5F, 2F, 5F, new CubeDeformation(-0.08F, -0.208F, 0F))
                .texOffs(0, 450).addBox(-2.4F, -17.4F, -0.8F, 2F, 2F, 2F, new CubeDeformation(-0.2F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(5F, -18F, 2F, -0.2793F, 0.4363F, 0F));
        PartDefinition p_tree_1_bough = p_tree_1.addOrReplaceChild("tree_1_bough", CubeListBuilder.create()
                .texOffs(443, 412).addBox(-1F, -5.5F, -1F, 2F, 6F, 2F, new CubeDeformation(-0.2F, 0F, -0.2F))
                .texOffs(108, 402).addBox(-3.5F, -9.3F, -2F, 7F, 5F, 4F, new CubeDeformation(-0.3F, -0.3F, -0.232F))
                .texOffs(452, 402).addBox(-3.5F, -7.8F, -3F, 7F, 2F, 6F, new CubeDeformation(-0.38F, -0.032F, 0F)),
                PartPose.offsetAndRotation(0F, -4.95F, 0F, 0F, 0F, -0.733F));
        PartDefinition p_deadwood = p_body.addOrReplaceChild("deadwood", CubeListBuilder.create()
                .texOffs(296, 402).addBox(-1.5F, -5F, -1.5F, 3F, 6F, 3F, new CubeDeformation(-0.3F, 0F, -0.3F))
                .texOffs(191, 429).addBox(-1F, -9.4F, -1F, 2F, 5F, 2F, new CubeDeformation(-0.1F, -0.2F, -0.1F)),
                PartPose.offsetAndRotation(-1F, -17F, 13F, -0.5236F, -0.1396F, 0.2094F));
        PartDefinition p_deadwood_twig = p_deadwood.addOrReplaceChild("deadwood_twig", CubeListBuilder.create()
                .texOffs(452, 412).addBox(-1F, -5.25F, -1F, 2F, 6F, 2F, new CubeDeformation(-0.4F, -0.25F, -0.4F)),
                PartPose.offsetAndRotation(0F, -6F, 0F, 0.1745F, 0F, -0.7854F));
        PartDefinition p_neck1 = p_body.addOrReplaceChild("neck1", CubeListBuilder.create()
                .texOffs(373, 298).addBox(-8.5F, -3.5F, -8.25F, 17F, 7F, 9F, new CubeDeformation(0F, -0.3F, -0.15F))
                .texOffs(37, 205).addBox(-4F, -8F, -8.25F, 8F, 16F, 9F, new CubeDeformation(-0.3F, 0F, -0.23F))
                .texOffs(0, 391).addBox(-4F, 6.8F, -7.75F, 8F, 2F, 8F, new CubeDeformation(-0.3F, -0.2F, -0.25F))
                .texOffs(109, 378).addBox(-7F, -9.9F, -8.25F, 14F, 2F, 9F, new CubeDeformation(0F, -0.4F, -0.25F))
                .texOffs(275, 350).addBox(-6F, -10.9F, -8.25F, 12F, 4F, 9F, new CubeDeformation(0F, -0.4F, -0.33F))
                .texOffs(253, 175).addBox(8.8F, -5F, -7.75F, 0F, 18F, 8F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(270, 175).addBox(-8.8F, -5F, -7.75F, 0F, 18F, 8F, new CubeDeformation(0F, 0F, -0.25F)),
                PartPose.offsetAndRotation(0F, -7F, -22F, -0.4189F, 0F, 0F));
        PartDefinition p_neck2 = p_neck1.addOrReplaceChild("neck2", CubeListBuilder.create()
                .texOffs(304, 317).addBox(-8F, -3F, -8F, 16F, 6F, 9F, new CubeDeformation(-0.25F, 0F, -0.4F))
                .texOffs(395, 205).addBox(-3.5F, -7.5F, -8F, 7F, 15F, 9F, new CubeDeformation(-0.25F, 0F, -0.48F))
                .texOffs(29, 402).addBox(-3.5F, 6.3F, -7F, 7F, 2F, 7F, new CubeDeformation(-0.25F, -0.2F, 0F))
                .texOffs(412, 378).addBox(-6.5F, -9.4F, -7.5F, 13F, 2F, 8F, new CubeDeformation(-0.25F, -0.4F, 0F))
                .texOffs(41, 365).addBox(-5.5F, -10.4F, -7.5F, 11F, 4F, 8F, new CubeDeformation(-0.25F, -0.4F, -0.08F))
                .texOffs(104, 231).addBox(8.05F, -4.5F, -7F, 0F, 16F, 7F)
                .texOffs(119, 231).addBox(-8.05F, -4.5F, -7F, 0F, 16F, 7F),
                PartPose.offsetAndRotation(0F, 0F, -7.5F, -0.1745F, 0F, 0F));
        PartDefinition p_neck3 = p_neck2.addOrReplaceChild("neck3", CubeListBuilder.create()
                .texOffs(370, 334).addBox(-7F, -3F, -7.25F, 14F, 6F, 8F, new CubeDeformation(0F, -0.2F, -0.15F))
                .texOffs(425, 231).addBox(-3F, -7F, -7.25F, 6F, 14F, 8F, new CubeDeformation(-0.2F, 0F, -0.23F))
                .texOffs(58, 402).addBox(-3F, 5.8F, -6.75F, 6F, 2F, 7F, new CubeDeformation(-0.2F, -0.2F, -0.25F))
                .texOffs(455, 378).addBox(-5.5F, -8.9F, -7.25F, 11F, 2F, 8F, new CubeDeformation(0F, -0.4F, -0.25F))
                .texOffs(308, 365).addBox(-4.5F, -9.9F, -7.25F, 9F, 4F, 8F, new CubeDeformation(0F, -0.4F, -0.33F))
                .texOffs(371, 255).addBox(7.3F, -4F, -6.75F, 0F, 14F, 7F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(386, 255).addBox(-7.3F, -4F, -6.75F, 0F, 14F, 7F, new CubeDeformation(0F, 0F, -0.25F)),
                PartPose.offsetAndRotation(0F, 0F, -7F, 0.0349F, 0F, 0F));
        PartDefinition p_neck4 = p_neck3.addOrReplaceChild("neck4", CubeListBuilder.create()
                .texOffs(460, 334).addBox(-6.5F, -3F, -7F, 13F, 6F, 8F, new CubeDeformation(0F, -0.4F, -0.4F))
                .texOffs(342, 255).addBox(-3F, -6.5F, -7F, 6F, 13F, 8F, new CubeDeformation(-0.4F, 0F, -0.48F))
                .texOffs(479, 402).addBox(-3F, 5.3F, -6F, 6F, 2F, 6F, new CubeDeformation(-0.4F, -0.2F, 0F))
                .texOffs(362, 391).addBox(-5F, -8.4F, -6.5F, 10F, 2F, 7F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(189, 378).addBox(-4F, -9.4F, -6.5F, 8F, 4F, 7F, new CubeDeformation(0F, -0.4F, -0.08F))
                .texOffs(129, 298).addBox(6.8F, -3.5F, -6F, 0F, 12F, 6F)
                .texOffs(142, 298).addBox(-6.8F, -3.5F, -6F, 0F, 12F, 6F),
                PartPose.offsetAndRotation(0F, 0F, -6.5F, 0.1745F, 0F, 0F));
        PartDefinition p_head = p_neck4.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 231).addBox(-8F, -4.75F, -15.5F, 16F, 7F, 16F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(391, 144).addBox(-5F, -7.75F, -15.5F, 10F, 13F, 16F, new CubeDeformation(0F, -0.25F, -0.08F))
                .texOffs(231, 391).addBox(-9F, -8.2F, -15.5F, 18F, 1F, 8F, new CubeDeformation(-0.4F, -0.2F, 0F))
                .texOffs(60, 378).addBox(-8F, -9.2F, -15.5F, 16F, 3F, 8F, new CubeDeformation(-0.4F, -0.2F, -0.08F))
                .texOffs(415, 334).addBox(-7F, -3.2F, -22F, 14F, 6F, 8F, new CubeDeformation(-0.4F, -0.4F, 0F))
                .texOffs(94, 298).addBox(-4.5F, -5.2F, -22F, 9F, 10F, 8F, new CubeDeformation(-0.1F, -0.2F, -0.08F))
                .texOffs(80, 365).addBox(-6F, -2.5F, -28.5F, 12F, 5F, 7F)
                .texOffs(223, 317).addBox(-4F, -4.5F, -28.5F, 8F, 9F, 7F, new CubeDeformation(0F, 0F, -0.08F))
                .texOffs(0, 365).addBox(-4.5F, -6F, -27F, 9F, 1F, 11F, new CubeDeformation(-0.3F, -0.2F, 0F))
                .texOffs(41, 350).addBox(-3.5F, -7F, -27F, 7F, 3F, 11F, new CubeDeformation(-0.1F, -0.4F, -0.08F))
                .texOffs(0, 421).addBox(-5F, -4.3F, -29.2F, 10F, 6F, 1F, new CubeDeformation(-0.4F, -0.3F, 0F))
                .texOffs(309, 402).addBox(-5F, -8.7F, -8F, 10F, 1F, 7F, new CubeDeformation(0F, -0.2F, 0F))
                .texOffs(33, 391).addBox(-4.5F, -9.7F, -8F, 9F, 3F, 7F, new CubeDeformation(-0.4F, -0.3F, -0.08F))
                .texOffs(252, 450).addBox(1.6F, -4F, -29.65F, 2F, 2F, 1F, new CubeDeformation(0F, -0.4F, -0.25F))
                .texOffs(165, 429).addBox(6.225F, -5F, -15.95F, 2F, 3F, 4F, new CubeDeformation(-0.075F, -0.4F, -0.05F))
                .texOffs(254, 317).addBox(7.2F, -0.75F, -15F, 2F, 6F, 10F, new CubeDeformation(0F, -0.45F, 0F))
                .texOffs(327, 298).addBox(7.7F, -1.25F, -15F, 1F, 7F, 10F, new CubeDeformation(-0.2F, -0.25F, -0.08F))
                .texOffs(9, 450).addBox(4.4F, 4.31F, -28F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.19F, -0.45F))
                .texOffs(18, 450).addBox(4.4F, 4.1F, -26F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.4F, -0.45F))
                .texOffs(27, 450).addBox(4.4F, 4.1F, -24F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.4F, -0.45F))
                .texOffs(36, 450).addBox(4.4F, 4.1F, -22F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.4F, -0.45F))
                .texOffs(45, 450).addBox(4.4F, 4.1F, -20F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.4F, -0.45F))
                .texOffs(54, 450).addBox(4.4F, 4.31F, -18F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.19F, -0.45F))
                .texOffs(259, 450).addBox(-3.6F, -4F, -29.65F, 2F, 2F, 1F, new CubeDeformation(0F, -0.4F, -0.25F))
                .texOffs(178, 429).addBox(-8.225F, -5F, -15.95F, 2F, 3F, 4F, new CubeDeformation(-0.075F, -0.4F, -0.05F))
                .texOffs(279, 317).addBox(-9.2F, -0.75F, -15F, 2F, 6F, 10F, new CubeDeformation(0F, -0.45F, 0F))
                .texOffs(350, 298).addBox(-8.7F, -1.25F, -15F, 1F, 7F, 10F, new CubeDeformation(-0.2F, -0.25F, -0.08F))
                .texOffs(63, 450).addBox(-6.4F, 4.31F, -28F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.19F, -0.45F))
                .texOffs(72, 450).addBox(-6.4F, 4.1F, -26F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.4F, -0.45F))
                .texOffs(81, 450).addBox(-6.4F, 4.1F, -24F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.4F, -0.45F))
                .texOffs(90, 450).addBox(-6.4F, 4.1F, -22F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.4F, -0.45F))
                .texOffs(99, 450).addBox(-6.4F, 4.1F, -20F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.4F, -0.45F))
                .texOffs(108, 450).addBox(-6.4F, 4.31F, -18F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.19F, -0.45F))
                .texOffs(181, 350).addBox(-5.5F, 4.17F, -28F, 11F, 1F, 12F, new CubeDeformation(-0.1F, -0.35F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, -6F, 0.4887F, 0F, 0F));
        PartDefinition p_ear_l = p_head.addOrReplaceChild("ear_l", CubeListBuilder.create()
                .texOffs(185, 334).addBox(-0.1F, -3F, 0F, 0F, 6F, 9F),
                PartPose.offsetAndRotation(7.5F, -5F, -5F, -0.3491F, 0.6109F, 0F));
        PartDefinition p_ear_r = p_head.addOrReplaceChild("ear_r", CubeListBuilder.create()
                .texOffs(204, 334).addBox(-0.1F, -3F, 0F, 0F, 6F, 9F),
                PartPose.offsetAndRotation(-7.5F, -5F, -5F, -0.3491F, -0.6109F, 0F));
        PartDefinition p_jaw = p_head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(251, 277).addBox(-6F, 1.25F, -15.75F, 12F, 2F, 16F, new CubeDeformation(0F, -0.35F, -0.25F))
                .texOffs(258, 255).addBox(-4.5F, -0.25F, -15.75F, 9F, 5F, 16F, new CubeDeformation(-0.1F, -0.25F, -0.33F))
                .texOffs(461, 412).addBox(-7F, 1.1F, -3.25F, 14F, 2F, 5F, new CubeDeformation(0F, -0.1F, -0.25F))
                .texOffs(66, 391).addBox(-5.5F, -0.4F, -3.25F, 11F, 5F, 5F, new CubeDeformation(-0.1F, 0F, -0.33F))
                .texOffs(266, 450).addBox(4.5F, -1.6425F, -13.95F, 1F, 2F, 1F, new CubeDeformation(0F, -0.2575F, 0F))
                .texOffs(271, 450).addBox(4.5F, -1.45F, -11.85F, 1F, 2F, 1F, new CubeDeformation(0F, -0.45F, 0F))
                .texOffs(276, 450).addBox(4.5F, -1.45F, -9.75F, 1F, 2F, 1F, new CubeDeformation(0F, -0.45F, 0F))
                .texOffs(281, 450).addBox(4.5F, -1.45F, -7.65F, 1F, 2F, 1F, new CubeDeformation(0F, -0.45F, 0F))
                .texOffs(286, 450).addBox(4.5F, -1.6425F, -5.55F, 1F, 2F, 1F, new CubeDeformation(0F, -0.2575F, 0F))
                .texOffs(291, 450).addBox(-5.5F, -1.6425F, -13.95F, 1F, 2F, 1F, new CubeDeformation(0F, -0.2575F, 0F))
                .texOffs(296, 450).addBox(-5.5F, -1.45F, -11.85F, 1F, 2F, 1F, new CubeDeformation(0F, -0.45F, 0F))
                .texOffs(301, 450).addBox(-5.5F, -1.45F, -9.75F, 1F, 2F, 1F, new CubeDeformation(0F, -0.45F, 0F))
                .texOffs(306, 450).addBox(-5.5F, -1.45F, -7.65F, 1F, 2F, 1F, new CubeDeformation(0F, -0.45F, 0F))
                .texOffs(311, 450).addBox(-5.5F, -1.6425F, -5.55F, 1F, 2F, 1F, new CubeDeformation(0F, -0.2575F, 0F))
                .texOffs(426, 298).addBox(-5.5F, -0.5F, -15.2F, 11F, 1F, 15F, new CubeDeformation(-0.3F, -0.35F, 0F))
                .texOffs(119, 365).addBox(-4F, -0.7F, -13.5F, 8F, 1F, 11F, new CubeDeformation(-0.4F, -0.2F, 0F))
                .texOffs(203, 421).addBox(-4.5F, 4.5F, -11F, 9F, 7F, 0F)
                .texOffs(190, 437).addBox(-3.5F, 4.25F, -7F, 7F, 6F, 0F, new CubeDeformation(-0.3F, -0.25F, 0F)),
                PartPose.offsetAndRotation(0F, 4.8F, -13F, 0.0524F, 0F, 0F));
        PartDefinition p_tusk_l = p_jaw.addOrReplaceChild("tusk_l", CubeListBuilder.create()
                .texOffs(309, 437).addBox(-1F, -2.243F, -1.5F, 2F, 3F, 3F, new CubeDeformation(-0.05F, -0.157F, -0.3125F))
                .texOffs(117, 450).addBox(-1F, -3.802F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3825F, -0.034F, -0.2281F))
                .texOffs(316, 450).addBox(-0.5F, -5.484F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.215F, -0.034F, -0.1437F)),
                PartPose.offsetAndRotation(6.3F, 0.8F, -12.5F, 0.2094F, 0F, 0.2793F));
        PartDefinition p_tusk_r = p_jaw.addOrReplaceChild("tusk_r", CubeListBuilder.create()
                .texOffs(320, 437).addBox(-1F, -2.243F, -1.5F, 2F, 3F, 3F, new CubeDeformation(-0.05F, -0.157F, -0.3125F))
                .texOffs(126, 450).addBox(-1F, -3.802F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3825F, -0.034F, -0.2281F))
                .texOffs(321, 450).addBox(-0.5F, -5.484F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.215F, -0.034F, -0.1437F)),
                PartPose.offsetAndRotation(-6.3F, 0.8F, -12.5F, 0.2094F, 0F, -0.2793F));
        PartDefinition p_antler_l1 = p_head.addOrReplaceChild("antler_l1", CubeListBuilder.create()
                .texOffs(433, 350).addBox(-2.5F, -2.5F, -0.7625F, 5F, 5F, 8F, new CubeDeformation(-0.3F, -0.3F, -0.3625F))
                .texOffs(274, 378).addBox(-2F, -2F, 6.175F, 4F, 4F, 7F, new CubeDeformation(-0.196F, -0.196F, -0.075F)),
                PartPose.offsetAndRotation(4.8F, -8.6F, -9F, 1.04F, 0.9828F, 0F));
        PartDefinition p_antler_l2 = p_antler_l1.addOrReplaceChild("antler_l2", CubeListBuilder.create()
                .texOffs(297, 378).addBox(-2F, -2F, -0.675F, 4F, 4F, 7F, new CubeDeformation(-0.15F, -0.15F, -0.275F))
                .texOffs(320, 378).addBox(-2F, -2F, 5.05F, 4F, 4F, 7F, new CubeDeformation(-0.483F, -0.483F, -0.45F))
                .texOffs(23, 421).addBox(-3F, -1.9F, 1.75F, 6F, 2F, 5F, new CubeDeformation(-0.3F, -0.1F, -0.25F))
                .texOffs(131, 402).addBox(-2F, -2.9F, 1.75F, 4F, 4F, 5F, new CubeDeformation(-0.2F, -0.2F, -0.33F)),
                PartPose.offsetAndRotation(0F, 0F, 12.5F, -0.439F, -0.1975F, 0F));
        PartDefinition p_antler_l3 = p_antler_l2.addOrReplaceChild("antler_l3", CubeListBuilder.create()
                .texOffs(150, 402).addBox(-1.5F, -1.5F, -0.45F, 3F, 3F, 6F, new CubeDeformation(0F, 0F, -0.05F))
                .texOffs(169, 402).addBox(-1.5F, -1.5F, 4.8F, 3F, 3F, 6F, new CubeDeformation(-0.27F, -0.27F, -0.2F)),
                PartPose.offsetAndRotation(0F, 0F, 11F, 0.3354F, -0.0243F, 0F));
        PartDefinition p_antler_l4 = p_antler_l3.addOrReplaceChild("antler_l4", CubeListBuilder.create()
                .texOffs(149, 412).addBox(-1.5F, -1.5F, -0.5F, 3F, 3F, 5F, new CubeDeformation(-0.3F, -0.3F, -0.1F))
                .texOffs(366, 421).addBox(-1F, -1F, 3.8F, 2F, 2F, 5F, new CubeDeformation(-0.016F, -0.016F, -0.2F)),
                PartPose.offsetAndRotation(0F, 0F, 10F, 0.3882F, 0.071F, 0F));
        PartDefinition p_antler_l_t4 = p_antler_l4.addOrReplaceChild("antler_l_t4", CubeListBuilder.create()
                .texOffs(381, 421).addBox(-1.5F, -1.5F, -0.5F, 3F, 3F, 4F, new CubeDeformation(-0.35F, -0.35F, -0.2F))
                .texOffs(231, 437).addBox(-1F, -1F, 2.7F, 2F, 2F, 4F, new CubeDeformation(-0.2F, -0.2F, -0.3F))
                .texOffs(222, 421).addBox(-3F, -2.6F, 4.8F, 6F, 4F, 3F, new CubeDeformation(-0.4F, 0F, -0.12F))
                .texOffs(46, 421).addBox(-3F, -1.6F, 3.8F, 6F, 2F, 5F, new CubeDeformation(-0.48F, -0.12F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 7.5F, 0.7049F, 0.6512F, 0F));
        PartDefinition p_antler_l_t3 = p_antler_l3.addOrReplaceChild("antler_l_t3", CubeListBuilder.create()
                .texOffs(396, 421).addBox(-1.5F, -1.5F, -0.3625F, 3F, 3F, 4F, new CubeDeformation(-0.35F, -0.35F, -0.0625F))
                .texOffs(244, 437).addBox(-1F, -1F, 3.075F, 2F, 2F, 4F, new CubeDeformation(-0.2F, -0.2F, -0.175F))
                .texOffs(135, 450).addBox(-1F, -1F, 5.85F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.35F, -0.35F)),
                PartPose.offsetAndRotation(0F, 0F, 9F, -0.0281F, 0.7206F, 0F));
        PartDefinition p_antler_l_t2 = p_antler_l2.addOrReplaceChild("antler_l_t2", CubeListBuilder.create()
                .texOffs(166, 412).addBox(-1.5F, -1.5F, -0.3125F, 3F, 3F, 5F, new CubeDeformation(-0.35F, -0.35F, -0.0125F))
                .texOffs(411, 421).addBox(-1F, -1F, 4.075F, 2F, 2F, 5F, new CubeDeformation(-0.2F, -0.2F, -0.175F))
                .texOffs(241, 421).addBox(-3F, -2.6F, 7.3F, 6F, 4F, 3F, new CubeDeformation(-0.4F, 0F, -0.12F))
                .texOffs(69, 421).addBox(-3F, -1.6F, 6.3F, 6F, 2F, 5F, new CubeDeformation(-0.48F, -0.12F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 7F, 0.6437F, 1.0691F, 0F));
        PartDefinition p_antler_l_t0 = p_antler_l1.addOrReplaceChild("antler_l_t0", CubeListBuilder.create()
                .texOffs(188, 402).addBox(-1.5F, -1.5F, -0.675F, 3F, 3F, 6F, new CubeDeformation(-0.35F, -0.35F, -0.375F))
                .texOffs(426, 421).addBox(-1F, -1F, 4.45F, 2F, 2F, 5F, new CubeDeformation(-0.2F, -0.2F, -0.05F)),
                PartPose.offsetAndRotation(0F, 0F, 3F, 0.5838F, 1.3974F, 0F));
        PartDefinition p_antler_l_t1 = p_antler_l1.addOrReplaceChild("antler_l_t1", CubeListBuilder.create()
                .texOffs(183, 412).addBox(-1.5F, -1.5F, -0.45F, 3F, 3F, 5F, new CubeDeformation(-0.35F, -0.35F, -0.15F))
                .texOffs(441, 421).addBox(-1F, -1F, 3.7F, 2F, 2F, 5F, new CubeDeformation(-0.2F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(0F, 0F, 9F, 0.4607F, 1.0308F, 0F));
        PartDefinition p_antler_r1 = p_head.addOrReplaceChild("antler_r1", CubeListBuilder.create()
                .texOffs(460, 350).addBox(-2.5F, -2.5F, -0.7625F, 5F, 5F, 8F, new CubeDeformation(-0.3F, -0.3F, -0.3625F))
                .texOffs(343, 378).addBox(-2F, -2F, 6.175F, 4F, 4F, 7F, new CubeDeformation(-0.196F, -0.196F, -0.075F)),
                PartPose.offsetAndRotation(-4.8F, -8.6F, -9F, 1.04F, -0.9828F, 0F));
        PartDefinition p_antler_r2 = p_antler_r1.addOrReplaceChild("antler_r2", CubeListBuilder.create()
                .texOffs(366, 378).addBox(-2F, -2F, -0.675F, 4F, 4F, 7F, new CubeDeformation(-0.15F, -0.15F, -0.275F))
                .texOffs(389, 378).addBox(-2F, -2F, 5.05F, 4F, 4F, 7F, new CubeDeformation(-0.483F, -0.483F, -0.45F))
                .texOffs(92, 421).addBox(-3F, -1.9F, 1.75F, 6F, 2F, 5F, new CubeDeformation(-0.3F, -0.1F, -0.25F))
                .texOffs(207, 402).addBox(-2F, -2.9F, 1.75F, 4F, 4F, 5F, new CubeDeformation(-0.2F, -0.2F, -0.33F)),
                PartPose.offsetAndRotation(0F, 0F, 12.5F, -0.439F, 0.1975F, 0F));
        PartDefinition p_antler_r3 = p_antler_r2.addOrReplaceChild("antler_r3", CubeListBuilder.create()
                .texOffs(226, 402).addBox(-1.5F, -1.5F, -0.45F, 3F, 3F, 6F, new CubeDeformation(0F, 0F, -0.05F))
                .texOffs(245, 402).addBox(-1.5F, -1.5F, 4.8F, 3F, 3F, 6F, new CubeDeformation(-0.27F, -0.27F, -0.2F)),
                PartPose.offsetAndRotation(0F, 0F, 11F, 0.3354F, 0.0243F, 0F));
        PartDefinition p_antler_r4 = p_antler_r3.addOrReplaceChild("antler_r4", CubeListBuilder.create()
                .texOffs(200, 412).addBox(-1.5F, -1.5F, -0.5F, 3F, 3F, 5F, new CubeDeformation(-0.3F, -0.3F, -0.1F))
                .texOffs(456, 421).addBox(-1F, -1F, 3.8F, 2F, 2F, 5F, new CubeDeformation(-0.016F, -0.016F, -0.2F)),
                PartPose.offsetAndRotation(0F, 0F, 10F, 0.3882F, -0.071F, 0F));
        PartDefinition p_antler_r_t4 = p_antler_r4.addOrReplaceChild("antler_r_t4", CubeListBuilder.create()
                .texOffs(471, 421).addBox(-1.5F, -1.5F, -0.5F, 3F, 3F, 4F, new CubeDeformation(-0.35F, -0.35F, -0.2F))
                .texOffs(257, 437).addBox(-1F, -1F, 2.7F, 2F, 2F, 4F, new CubeDeformation(-0.2F, -0.2F, -0.3F))
                .texOffs(260, 421).addBox(-3F, -2.6F, 4.8F, 6F, 4F, 3F, new CubeDeformation(-0.4F, 0F, -0.12F))
                .texOffs(115, 421).addBox(-3F, -1.6F, 3.8F, 6F, 2F, 5F, new CubeDeformation(-0.48F, -0.12F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 7.5F, 0.7049F, -0.6512F, 0F));
        PartDefinition p_antler_r_t3 = p_antler_r3.addOrReplaceChild("antler_r_t3", CubeListBuilder.create()
                .texOffs(486, 421).addBox(-1.5F, -1.5F, -0.3625F, 3F, 3F, 4F, new CubeDeformation(-0.35F, -0.35F, -0.0625F))
                .texOffs(270, 437).addBox(-1F, -1F, 3.075F, 2F, 2F, 4F, new CubeDeformation(-0.2F, -0.2F, -0.175F))
                .texOffs(144, 450).addBox(-1F, -1F, 5.85F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.35F, -0.35F)),
                PartPose.offsetAndRotation(0F, 0F, 9F, -0.0281F, -0.7206F, 0F));
        PartDefinition p_antler_r_t2 = p_antler_r2.addOrReplaceChild("antler_r_t2", CubeListBuilder.create()
                .texOffs(217, 412).addBox(-1.5F, -1.5F, -0.3125F, 3F, 3F, 5F, new CubeDeformation(-0.35F, -0.35F, -0.0125F))
                .texOffs(0, 429).addBox(-1F, -1F, 4.075F, 2F, 2F, 5F, new CubeDeformation(-0.2F, -0.2F, -0.175F))
                .texOffs(279, 421).addBox(-3F, -2.6F, 7.3F, 6F, 4F, 3F, new CubeDeformation(-0.4F, 0F, -0.12F))
                .texOffs(138, 421).addBox(-3F, -1.6F, 6.3F, 6F, 2F, 5F, new CubeDeformation(-0.48F, -0.12F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 7F, 0.6437F, -1.0691F, 0F));
        PartDefinition p_antler_r_t0 = p_antler_r1.addOrReplaceChild("antler_r_t0", CubeListBuilder.create()
                .texOffs(264, 402).addBox(-1.5F, -1.5F, -0.675F, 3F, 3F, 6F, new CubeDeformation(-0.35F, -0.35F, -0.375F))
                .texOffs(15, 429).addBox(-1F, -1F, 4.45F, 2F, 2F, 5F, new CubeDeformation(-0.2F, -0.2F, -0.05F)),
                PartPose.offsetAndRotation(0F, 0F, 3F, 0.5838F, -1.3974F, 0F));
        PartDefinition p_antler_r_t1 = p_antler_r1.addOrReplaceChild("antler_r_t1", CubeListBuilder.create()
                .texOffs(234, 412).addBox(-1.5F, -1.5F, -0.45F, 3F, 3F, 5F, new CubeDeformation(-0.35F, -0.35F, -0.15F))
                .texOffs(30, 429).addBox(-1F, -1F, 3.7F, 2F, 2F, 5F, new CubeDeformation(-0.2F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(0F, 0F, 9F, 0.4607F, -1.0308F, 0F));
        PartDefinition p_tail1 = p_body.addOrReplaceChild("tail1", CubeListBuilder.create()
                .texOffs(401, 255).addBox(-7.5F, -3F, -1F, 15F, 6F, 14F, new CubeDeformation(0F, -0.4F, -0.4F))
                .texOffs(114, 175).addBox(-4F, -6.5F, -1F, 8F, 13F, 14F, new CubeDeformation(-0.4F, 0F, -0.48F))
                .texOffs(0, 350).addBox(-4F, 5.3F, 0F, 8F, 2F, 12F, new CubeDeformation(-0.4F, -0.2F, 0F))
                .texOffs(223, 334).addBox(-6.5F, -7.7F, -0.7F, 13F, 1F, 13F, new CubeDeformation(-0.2F, -0.1F, -0.3F))
                .texOffs(0, 317).addBox(-5.5F, -8.7F, -0.7F, 11F, 3F, 13F, new CubeDeformation(-0.1F, -0.2F, -0.38F)),
                PartPose.offsetAndRotation(0F, -4F, 22F, -0.2094F, 0F, 0F));
        PartDefinition p_tail2 = p_tail1.addOrReplaceChild("tail2", CubeListBuilder.create()
                .texOffs(418, 277).addBox(-6.5F, -2.5F, -1F, 13F, 5F, 13F, new CubeDeformation(0F, -0.3F, -0.4F))
                .texOffs(354, 205).addBox(-3.5F, -5.5F, -1F, 7F, 11F, 13F, new CubeDeformation(-0.3F, 0F, -0.48F))
                .texOffs(361, 350).addBox(-3.5F, 4.3F, 0F, 7F, 2F, 11F, new CubeDeformation(-0.3F, -0.2F, 0F))
                .texOffs(228, 350).addBox(-5.5F, -6.7F, -0.7F, 11F, 1F, 12F, new CubeDeformation(-0.2F, -0.1F, -0.3F))
                .texOffs(457, 317).addBox(-4.5F, -7.7F, -0.7F, 9F, 3F, 12F, new CubeDeformation(-0.1F, -0.2F, -0.38F)),
                PartPose.offsetAndRotation(0F, 0F, 12F, -0.1396F, 0F, 0F));
        PartDefinition p_tail3 = p_tail2.addOrReplaceChild("tail3", CubeListBuilder.create()
                .texOffs(155, 298).addBox(-5.5F, -2F, -1F, 11F, 4F, 13F, new CubeDeformation(0F, -0.1F, -0.4F))
                .texOffs(65, 231).addBox(-3F, -5F, -1F, 6F, 10F, 13F, new CubeDeformation(-0.35F, -0.25F, -0.48F))
                .texOffs(398, 350).addBox(-3F, 3.55F, 0F, 6F, 2F, 11F, new CubeDeformation(-0.35F, -0.2F, 0F))
                .texOffs(318, 350).addBox(-4.5F, -5.95F, -0.7F, 9F, 1F, 12F, new CubeDeformation(-0.2F, -0.1F, -0.3F))
                .texOffs(41, 334).addBox(-3.5F, -6.95F, -0.7F, 7F, 3F, 12F, new CubeDeformation(-0.1F, -0.2F, -0.38F)),
                PartPose.offsetAndRotation(0F, 0F, 11F, -0.0698F, 0F, 0F));
        PartDefinition p_tail4 = p_tail3.addOrReplaceChild("tail4", CubeListBuilder.create()
                .texOffs(49, 317).addBox(-5F, -2F, -1F, 10F, 4F, 12F, new CubeDeformation(-0.25F, -0.4F, -0.4F))
                .texOffs(59, 277).addBox(-2.5F, -4F, -1F, 5F, 8F, 12F, new CubeDeformation(-0.15F, 0F, -0.48F))
                .texOffs(0, 378).addBox(-2.5F, 2.8F, 0F, 5F, 2F, 10F, new CubeDeformation(-0.15F, -0.2F, 0F))
                .texOffs(158, 365).addBox(-4F, -5.2F, -0.7F, 8F, 1F, 11F, new CubeDeformation(-0.45F, -0.1F, -0.3F))
                .texOffs(115, 350).addBox(-3F, -6.2F, -0.7F, 6F, 3F, 11F, new CubeDeformation(-0.35F, -0.2F, -0.38F)),
                PartPose.offset(0F, 0F, 11F));
        PartDefinition p_tail5 = p_tail4.addOrReplaceChild("tail5", CubeListBuilder.create()
                .texOffs(0, 334).addBox(-4F, -1.5F, -1F, 8F, 3F, 12F, new CubeDeformation(0F, -0.1F, -0.4F))
                .texOffs(160, 277).addBox(-2F, -3.5F, -1F, 4F, 7F, 12F, new CubeDeformation(-0.1F, 0F, -0.48F))
                .texOffs(31, 378).addBox(-2F, 2.3F, 0F, 4F, 2F, 10F, new CubeDeformation(-0.1F, -0.2F, 0F))
                .texOffs(343, 365).addBox(-3F, -4.7F, -0.7F, 6F, 1F, 11F, new CubeDeformation(-0.2F, -0.1F, -0.3F))
                .texOffs(150, 350).addBox(-2F, -5.7F, -0.7F, 4F, 3F, 11F, new CubeDeformation(-0.1F, -0.2F, -0.38F)),
                PartPose.offsetAndRotation(0F, 0F, 10F, 0.0524F, 0F, 0F));
        PartDefinition p_tail6 = p_tail5.addOrReplaceChild("tail6", CubeListBuilder.create()
                .texOffs(78, 350).addBox(-3.5F, -1.5F, -1F, 7F, 3F, 11F, new CubeDeformation(-0.25F, -0.3F, -0.4F))
                .texOffs(298, 298).addBox(-1.5F, -3F, -1F, 3F, 6F, 11F, new CubeDeformation(-0.05F, 0F, -0.48F))
                .texOffs(249, 378).addBox(-1.5F, 1.8F, 0F, 3F, 2F, 9F, new CubeDeformation(-0.05F, -0.2F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 10F, 0.0698F, 0F, 0F));
        PartDefinition p_tail7 = p_tail6.addOrReplaceChild("tail7", CubeListBuilder.create()
                .texOffs(378, 365).addBox(-3F, -1F, -1F, 6F, 2F, 10F, new CubeDeformation(-0.25F, 0F, -0.4F))
                .texOffs(158, 334).addBox(-1.5F, -2.5F, -1F, 3F, 5F, 10F, new CubeDeformation(-0.25F, 0F, -0.48F))
                .texOffs(182, 391).addBox(-1.5F, 1.3F, 0F, 3F, 2F, 8F, new CubeDeformation(-0.25F, -0.2F, 0F))
                .texOffs(94, 317).addBox(-5.5F, -3F, 4.5F, 11F, 6F, 10F, new CubeDeformation(0F, -0.1F, 0F))
                .texOffs(309, 255).addBox(-3F, -5.5F, 4.5F, 6F, 11F, 10F, new CubeDeformation(-0.1F, 0F, -0.08F))
                .texOffs(467, 391).addBox(-4.5F, -6.9F, 6F, 9F, 2F, 7F, new CubeDeformation(-0.3F, -0.1F, 0F))
                .texOffs(153, 450).addBox(-6.1F, -2.2F, 6.8F, 2F, 2F, 2F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(162, 450).addBox(4.1F, 0.8F, 8.8F, 2F, 2F, 2F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(171, 450).addBox(-1.2F, -7.6F, 10.3F, 2F, 2F, 2F, new CubeDeformation(-0.2F, -0.2F, -0.2F)),
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
                .texOffs(457, 175).addBox(-5.5F, -4.7F, -3.5F, 11F, 18F, 7F, new CubeDeformation(0F, -0.3F, -0.42F))
                .texOffs(444, 144).addBox(-3.5F, -4.7F, -5.5F, 7F, 18F, 11F, new CubeDeformation(-0.42F, -0.38F, 0F))
                .texOffs(80, 334).addBox(-6.5F, -6.425F, -3F, 13F, 9F, 6F, new CubeDeformation(-0.4F, -0.375F, -0.2F))
                .texOffs(347, 231).addBox(-3F, -6.425F, -6.5F, 6F, 9F, 13F, new CubeDeformation(-0.2F, -0.455F, -0.4F)),
                PartPose.offsetAndRotation(12.5F, 4F, -15F, 0.1396F, 0F, 0F));
        PartDefinition p_front_shin_l = p_front_leg_l.addOrReplaceChild("front_shin_l", CubeListBuilder.create()
                .texOffs(193, 277).addBox(-4.5F, -1.85F, -2.5F, 9F, 14F, 5F, new CubeDeformation(-0.25F, -0.45F, -0.12F))
                .texOffs(454, 231).addBox(-2.5F, -1.35F, -4.5F, 5F, 13F, 9F, new CubeDeformation(-0.12F, -0.03F, -0.25F)),
                PartPose.offsetAndRotation(0F, 12.4F, 0F, -0.2443F, 0F, 0F));
        PartDefinition p_front_foot_l = p_front_shin_l.addOrReplaceChild("front_foot_l", CubeListBuilder.create()
                .texOffs(276, 334).addBox(-5.5F, 0.75F, -8.4F, 11F, 2F, 12F, new CubeDeformation(0F, -0.3F, 0F))
                .texOffs(137, 317).addBox(-4.5F, -0.25F, -8.4F, 9F, 4F, 12F, new CubeDeformation(-0.05F, -0.25F, -0.08F))
                .texOffs(45, 429).addBox(-5.35F, 1.55F, -12.3F, 2F, 2F, 5F, new CubeDeformation(-0.05F, -0.05F, 0F))
                .texOffs(180, 450).addBox(-5.35F, 1.93F, -13.95F, 2F, 2F, 2F, new CubeDeformation(-0.335F, -0.145F, -0.05F))
                .texOffs(60, 429).addBox(-2.45F, 1.55F, -12.3F, 2F, 2F, 5F, new CubeDeformation(-0.05F, -0.05F, 0F))
                .texOffs(189, 450).addBox(-2.45F, 1.93F, -13.95F, 2F, 2F, 2F, new CubeDeformation(-0.335F, -0.145F, -0.05F))
                .texOffs(75, 429).addBox(0.45F, 1.55F, -12.3F, 2F, 2F, 5F, new CubeDeformation(-0.05F, -0.05F, 0F))
                .texOffs(198, 450).addBox(0.45F, 1.93F, -13.95F, 2F, 2F, 2F, new CubeDeformation(-0.335F, -0.145F, -0.05F))
                .texOffs(90, 429).addBox(3.35F, 1.55F, -12.3F, 2F, 2F, 5F, new CubeDeformation(-0.05F, -0.05F, 0F))
                .texOffs(207, 450).addBox(3.35F, 1.93F, -13.95F, 2F, 2F, 2F, new CubeDeformation(-0.335F, -0.145F, -0.05F)),
                PartPose.offsetAndRotation(0F, 10.9F, 0F, 0.1047F, 0F, 0F));
        PartDefinition p_hind_leg_l = p_body.addOrReplaceChild("hind_leg_l", CubeListBuilder.create()
                .texOffs(204, 144).addBox(-6.5F, -5.6F, -4.5F, 13F, 21F, 9F, new CubeDeformation(0F, -0.4F, -0.36F))
                .texOffs(0, 108).addBox(-4F, -5.6F, -7F, 8F, 21F, 14F, new CubeDeformation(-0.36F, -0.48F, 0F))
                .texOffs(0, 298).addBox(-7.5F, -7.275F, -4F, 15F, 10F, 8F, new CubeDeformation(-0.4F, -0.125F, -0.3F))
                .texOffs(159, 175).addBox(-3.5F, -7.275F, -8F, 7F, 10F, 16F, new CubeDeformation(-0.3F, -0.205F, -0.4F)),
                PartPose.offsetAndRotation(12.5F, 2F, 14F, -0.2094F, 0F, 0F));
        PartDefinition p_hind_shin_l = p_hind_leg_l.addOrReplaceChild("hind_shin_l", CubeListBuilder.create()
                .texOffs(94, 277).addBox(-5F, -1.6F, -3F, 10F, 14F, 6F, new CubeDeformation(-0.25F, -0.2F, -0.34F))
                .texOffs(428, 205).addBox(-3F, -1.6F, -5F, 6F, 14F, 10F, new CubeDeformation(-0.34F, -0.28F, -0.25F)),
                PartPose.offsetAndRotation(0F, 14.4F, 0F, 0.5934F, 0F, 0F));
        PartDefinition p_hind_foot_l = p_hind_shin_l.addOrReplaceChild("hind_foot_l", CubeListBuilder.create()
                .texOffs(355, 317).addBox(-6F, 0.75F, -9.1F, 12F, 2F, 13F, new CubeDeformation(-0.25F, -0.3F, 0F))
                .texOffs(204, 298).addBox(-5F, -0.25F, -9.1F, 10F, 4F, 13F, new CubeDeformation(-0.3F, -0.25F, -0.08F))
                .texOffs(251, 412).addBox(-5.5375F, 1.55F, -13.75F, 2F, 2F, 6F, new CubeDeformation(-0.05F, -0.05F, -0.25F))
                .texOffs(475, 437).addBox(-5.5375F, 1.93F, -15.75F, 2F, 2F, 3F, new CubeDeformation(-0.335F, -0.145F, -0.45F))
                .texOffs(268, 412).addBox(-2.5125F, 1.55F, -13.75F, 2F, 2F, 6F, new CubeDeformation(-0.05F, -0.05F, -0.25F))
                .texOffs(486, 437).addBox(-2.5125F, 1.93F, -15.75F, 2F, 2F, 3F, new CubeDeformation(-0.335F, -0.145F, -0.45F))
                .texOffs(285, 412).addBox(0.5125F, 1.55F, -13.75F, 2F, 2F, 6F, new CubeDeformation(-0.05F, -0.05F, -0.25F))
                .texOffs(497, 437).addBox(0.5125F, 1.93F, -15.75F, 2F, 2F, 3F, new CubeDeformation(-0.335F, -0.145F, -0.45F))
                .texOffs(302, 412).addBox(3.5375F, 1.55F, -13.75F, 2F, 2F, 6F, new CubeDeformation(-0.05F, -0.05F, -0.25F))
                .texOffs(0, 444).addBox(3.5375F, 1.93F, -15.75F, 2F, 2F, 3F, new CubeDeformation(-0.335F, -0.145F, -0.45F)),
                PartPose.offsetAndRotation(0F, 11.4F, 0F, -0.384F, 0F, 0F));
        PartDefinition p_front_leg_r = p_body.addOrReplaceChild("front_leg_r", CubeListBuilder.create()
                .texOffs(0, 205).addBox(-5.5F, -4.7F, -3.5F, 11F, 18F, 7F, new CubeDeformation(0F, -0.3F, -0.42F))
                .texOffs(0, 175).addBox(-3.5F, -4.7F, -5.5F, 7F, 18F, 11F, new CubeDeformation(-0.42F, -0.38F, 0F))
                .texOffs(119, 334).addBox(-6.5F, -6.425F, -3F, 13F, 9F, 6F, new CubeDeformation(-0.4F, -0.375F, -0.2F))
                .texOffs(386, 231).addBox(-3F, -6.425F, -6.5F, 6F, 9F, 13F, new CubeDeformation(-0.2F, -0.455F, -0.4F)),
                PartPose.offsetAndRotation(-12.5F, 4F, -15F, 0.1396F, 0F, 0F));
        PartDefinition p_front_shin_r = p_front_leg_r.addOrReplaceChild("front_shin_r", CubeListBuilder.create()
                .texOffs(222, 277).addBox(-4.5F, -1.85F, -2.5F, 9F, 14F, 5F, new CubeDeformation(-0.25F, -0.45F, -0.12F))
                .texOffs(483, 231).addBox(-2.5F, -1.35F, -4.5F, 5F, 13F, 9F, new CubeDeformation(-0.12F, -0.03F, -0.25F)),
                PartPose.offsetAndRotation(0F, 12.4F, 0F, -0.2443F, 0F, 0F));
        PartDefinition p_front_foot_r = p_front_shin_r.addOrReplaceChild("front_foot_r", CubeListBuilder.create()
                .texOffs(323, 334).addBox(-5.5F, 0.75F, -8.4F, 11F, 2F, 12F, new CubeDeformation(0F, -0.3F, 0F))
                .texOffs(180, 317).addBox(-4.5F, -0.25F, -8.4F, 9F, 4F, 12F, new CubeDeformation(-0.05F, -0.25F, -0.08F))
                .texOffs(105, 429).addBox(-5.35F, 1.55F, -12.3F, 2F, 2F, 5F, new CubeDeformation(-0.05F, -0.05F, 0F))
                .texOffs(216, 450).addBox(-5.35F, 1.93F, -13.95F, 2F, 2F, 2F, new CubeDeformation(-0.335F, -0.145F, -0.05F))
                .texOffs(120, 429).addBox(-2.45F, 1.55F, -12.3F, 2F, 2F, 5F, new CubeDeformation(-0.05F, -0.05F, 0F))
                .texOffs(225, 450).addBox(-2.45F, 1.93F, -13.95F, 2F, 2F, 2F, new CubeDeformation(-0.335F, -0.145F, -0.05F))
                .texOffs(135, 429).addBox(0.45F, 1.55F, -12.3F, 2F, 2F, 5F, new CubeDeformation(-0.05F, -0.05F, 0F))
                .texOffs(234, 450).addBox(0.45F, 1.93F, -13.95F, 2F, 2F, 2F, new CubeDeformation(-0.335F, -0.145F, -0.05F))
                .texOffs(150, 429).addBox(3.35F, 1.55F, -12.3F, 2F, 2F, 5F, new CubeDeformation(-0.05F, -0.05F, 0F))
                .texOffs(243, 450).addBox(3.35F, 1.93F, -13.95F, 2F, 2F, 2F, new CubeDeformation(-0.335F, -0.145F, -0.05F)),
                PartPose.offsetAndRotation(0F, 10.9F, 0F, 0.1047F, 0F, 0F));
        PartDefinition p_hind_leg_r = p_body.addOrReplaceChild("hind_leg_r", CubeListBuilder.create()
                .texOffs(249, 144).addBox(-6.5F, -5.6F, -4.5F, 13F, 21F, 9F, new CubeDeformation(0F, -0.4F, -0.36F))
                .texOffs(45, 108).addBox(-4F, -5.6F, -7F, 8F, 21F, 14F, new CubeDeformation(-0.36F, -0.48F, 0F))
                .texOffs(47, 298).addBox(-7.5F, -7.275F, -4F, 15F, 10F, 8F, new CubeDeformation(-0.4F, -0.125F, -0.3F))
                .texOffs(206, 175).addBox(-3.5F, -7.275F, -8F, 7F, 10F, 16F, new CubeDeformation(-0.3F, -0.205F, -0.4F)),
                PartPose.offsetAndRotation(-12.5F, 2F, 14F, -0.2094F, 0F, 0F));
        PartDefinition p_hind_shin_r = p_hind_leg_r.addOrReplaceChild("hind_shin_r", CubeListBuilder.create()
                .texOffs(127, 277).addBox(-5F, -1.6F, -3F, 10F, 14F, 6F, new CubeDeformation(-0.25F, -0.2F, -0.34F))
                .texOffs(461, 205).addBox(-3F, -1.6F, -5F, 6F, 14F, 10F, new CubeDeformation(-0.34F, -0.28F, -0.25F)),
                PartPose.offsetAndRotation(0F, 14.4F, 0F, 0.5934F, 0F, 0F));
        PartDefinition p_hind_foot_r = p_hind_shin_r.addOrReplaceChild("hind_foot_r", CubeListBuilder.create()
                .texOffs(406, 317).addBox(-6F, 0.75F, -9.1F, 12F, 2F, 13F, new CubeDeformation(-0.25F, -0.3F, 0F))
                .texOffs(251, 298).addBox(-5F, -0.25F, -9.1F, 10F, 4F, 13F, new CubeDeformation(-0.3F, -0.25F, -0.08F))
                .texOffs(319, 412).addBox(-5.5375F, 1.55F, -13.75F, 2F, 2F, 6F, new CubeDeformation(-0.05F, -0.05F, -0.25F))
                .texOffs(11, 444).addBox(-5.5375F, 1.93F, -15.75F, 2F, 2F, 3F, new CubeDeformation(-0.335F, -0.145F, -0.45F))
                .texOffs(336, 412).addBox(-2.5125F, 1.55F, -13.75F, 2F, 2F, 6F, new CubeDeformation(-0.05F, -0.05F, -0.25F))
                .texOffs(22, 444).addBox(-2.5125F, 1.93F, -15.75F, 2F, 2F, 3F, new CubeDeformation(-0.335F, -0.145F, -0.45F))
                .texOffs(353, 412).addBox(0.5125F, 1.55F, -13.75F, 2F, 2F, 6F, new CubeDeformation(-0.05F, -0.05F, -0.25F))
                .texOffs(33, 444).addBox(0.5125F, 1.93F, -15.75F, 2F, 2F, 3F, new CubeDeformation(-0.335F, -0.145F, -0.45F))
                .texOffs(370, 412).addBox(3.5375F, 1.55F, -13.75F, 2F, 2F, 6F, new CubeDeformation(-0.05F, -0.05F, -0.25F))
                .texOffs(44, 444).addBox(3.5375F, 1.93F, -15.75F, 2F, 2F, 3F, new CubeDeformation(-0.335F, -0.145F, -0.45F)),
                PartPose.offsetAndRotation(0F, 11.4F, 0F, -0.384F, 0F, 0F));
        PartDefinition p_wing_l = p_body.addOrReplaceChild("wing_l", CubeListBuilder.create()
                .texOffs(397, 391).addBox(-1.45F, -3F, -1.5F, 14F, 6F, 3F, new CubeDeformation(-0.45F, 0F, -0.18F))
                .texOffs(284, 391).addBox(-0.95F, -1.5F, -3F, 13F, 3F, 6F, new CubeDeformation(-0.03F, -0.18F, 0F))
                .texOffs(132, 437).addBox(10.9F, -2F, -1F, 12F, 4F, 2F, new CubeDeformation(-0.1F, 0F, -0.12F))
                .texOffs(0, 437).addBox(10.9F, -1F, -2F, 12F, 2F, 4F, new CubeDeformation(-0.18F, -0.12F, 0F))
                .texOffs(324, 108).addBox(0F, 0F, 1.5F, 22F, 0F, 30F),
                PartPose.offset(13F, -11F, -12F));
        PartDefinition p_wing_l_fore = p_wing_l.addOrReplaceChild("wing_l_fore", CubeListBuilder.create()
                .texOffs(33, 437).addBox(-0.8F, -2F, -1F, 14F, 4F, 2F, new CubeDeformation(-0.2F, -0.05F, -0.142F))
                .texOffs(416, 429).addBox(-0.8F, -1F, -2F, 14F, 2F, 4F, new CubeDeformation(-0.28F, -0.142F, -0.05F))
                .texOffs(405, 437).addBox(11.65F, -1.5F, -1F, 15F, 3F, 2F, new CubeDeformation(-0.05F, -0.125F, -0.395F))
                .texOffs(331, 437).addBox(11.65F, -1F, -1.5F, 15F, 2F, 3F, new CubeDeformation(-0.13F, -0.395F, -0.125F))
                .texOffs(90, 108).addBox(0F, 0F, 0.875F, 26F, 0F, 32F),
                PartPose.offset(22F, 0F, 0F));
        PartDefinition p_wing_l_hand = p_wing_l_fore.addOrReplaceChild("wing_l_hand", CubeListBuilder.create()
                .texOffs(387, 412).addBox(-2.5F, -2.5F, -1.5F, 5F, 5F, 3F, new CubeDeformation(-0.125F, -0.125F, -0.3125F))
                .texOffs(69, 412).addBox(-2.5F, -1.5F, -2.5F, 5F, 3F, 5F, new CubeDeformation(-0.205F, -0.3125F, -0.125F))
                .texOffs(283, 437).addBox(-1F, -1F, -5.875F, 2F, 2F, 4F, new CubeDeformation(-0.1F, -0.1F, -0.3F))
                .texOffs(55, 444).addBox(-1F, -0.75F, -8.075F, 2F, 2F, 3F, new CubeDeformation(-0.4F, -0.25F, -0.3F)),
                PartPose.offset(26F, 0F, 0F));
        PartDefinition p_wing_l_f1 = p_wing_l_hand.addOrReplaceChild("wing_l_f1", CubeListBuilder.create()
                .texOffs(200, 429).addBox(-0.04F, -1.5F, -1.5F, 24F, 3F, 3F, new CubeDeformation(-0.04F, -0.2F, -0.2F))
                .texOffs(77, 444).addBox(23F, -1F, -1F, 23F, 2F, 2F, new CubeDeformation(0F, -0.22F, -0.22F))
                .texOffs(72, 205).addBox(0F, 0F, 0.48F, 46F, 0F, 24F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, -0.1745F, 0F));
        PartDefinition p_wing_l_f2 = p_wing_l_hand.addOrReplaceChild("wing_l_f2", CubeListBuilder.create()
                .texOffs(310, 429).addBox(-0.32F, -1.5F, -1.5F, 23F, 3F, 3F, new CubeDeformation(-0.32F, -0.35F, -0.35F))
                .texOffs(179, 444).addBox(21.25F, -1F, -1F, 22F, 2F, 2F, new CubeDeformation(-0.25F, -0.31F, -0.31F))
                .texOffs(0, 255).addBox(0F, 0F, 0.39F, 43F, 0F, 21F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, -0.733F, 0F));
        PartDefinition p_wing_l_f3 = p_wing_l_hand.addOrReplaceChild("wing_l_f3", CubeListBuilder.create()
                .texOffs(277, 444).addBox(-0.14F, -1F, -1F, 19F, 2F, 2F, new CubeDeformation(-0.14F, 0F, 0F))
                .texOffs(363, 444).addBox(18F, -1F, -1F, 18F, 2F, 2F, new CubeDeformation(0F, -0.4F, -0.4F))
                .texOffs(121, 69).addBox(0F, 0F, 0.3F, 36F, 0F, 35F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, -1.2915F, 0F));
        PartDefinition p_wing_r = p_body.addOrReplaceChild("wing_r", CubeListBuilder.create()
                .texOffs(432, 391).addBox(-12.55F, -3F, -1.5F, 14F, 6F, 3F, new CubeDeformation(-0.45F, 0F, -0.18F))
                .texOffs(323, 391).addBox(-12.05F, -1.5F, -3F, 13F, 3F, 6F, new CubeDeformation(-0.03F, -0.18F, 0F))
                .texOffs(161, 437).addBox(-22.9F, -2F, -1F, 12F, 4F, 2F, new CubeDeformation(-0.1F, 0F, -0.12F))
                .texOffs(66, 437).addBox(-22.9F, -1F, -2F, 12F, 2F, 4F, new CubeDeformation(-0.18F, -0.12F, 0F))
                .texOffs(0, 144).addBox(-22F, 0F, 1.5F, 22F, 0F, 30F),
                PartPose.offset(-13F, -11F, -12F));
        PartDefinition p_wing_r_fore = p_wing_r.addOrReplaceChild("wing_r_fore", CubeListBuilder.create()
                .texOffs(99, 437).addBox(-13.2F, -2F, -1F, 14F, 4F, 2F, new CubeDeformation(-0.2F, -0.05F, -0.142F))
                .texOffs(453, 429).addBox(-13.2F, -1F, -2F, 14F, 2F, 4F, new CubeDeformation(-0.28F, -0.142F, -0.05F))
                .texOffs(440, 437).addBox(-26.65F, -1.5F, -1F, 15F, 3F, 2F, new CubeDeformation(-0.05F, -0.125F, -0.395F))
                .texOffs(368, 437).addBox(-26.65F, -1F, -1.5F, 15F, 2F, 3F, new CubeDeformation(-0.13F, -0.395F, -0.125F))
                .texOffs(207, 108).addBox(-26F, 0F, 0.875F, 26F, 0F, 32F),
                PartPose.offset(-22F, 0F, 0F));
        PartDefinition p_wing_r_hand = p_wing_r_fore.addOrReplaceChild("wing_r_hand", CubeListBuilder.create()
                .texOffs(404, 412).addBox(-2.5F, -2.5F, -1.5F, 5F, 5F, 3F, new CubeDeformation(-0.125F, -0.125F, -0.3125F))
                .texOffs(90, 412).addBox(-2.5F, -1.5F, -2.5F, 5F, 3F, 5F, new CubeDeformation(-0.205F, -0.3125F, -0.125F))
                .texOffs(296, 437).addBox(-1F, -1F, -5.875F, 2F, 2F, 4F, new CubeDeformation(-0.1F, -0.1F, -0.3F))
                .texOffs(66, 444).addBox(-1F, -0.75F, -8.075F, 2F, 2F, 3F, new CubeDeformation(-0.4F, -0.25F, -0.3F)),
                PartPose.offset(-26F, 0F, 0F));
        PartDefinition p_wing_r_f1 = p_wing_r_hand.addOrReplaceChild("wing_r_f1", CubeListBuilder.create()
                .texOffs(255, 429).addBox(-23.96F, -1.5F, -1.5F, 24F, 3F, 3F, new CubeDeformation(-0.04F, -0.2F, -0.2F))
                .texOffs(128, 444).addBox(-46F, -1F, -1F, 23F, 2F, 2F, new CubeDeformation(0F, -0.22F, -0.22F))
                .texOffs(213, 205).addBox(-46F, 0F, 0.48F, 46F, 0F, 24F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0.1745F, 0F));
        PartDefinition p_wing_r_f2 = p_wing_r_hand.addOrReplaceChild("wing_r_f2", CubeListBuilder.create()
                .texOffs(363, 429).addBox(-22.68F, -1.5F, -1.5F, 23F, 3F, 3F, new CubeDeformation(-0.32F, -0.35F, -0.35F))
                .texOffs(228, 444).addBox(-43.25F, -1F, -1F, 22F, 2F, 2F, new CubeDeformation(-0.25F, -0.31F, -0.31F))
                .texOffs(129, 255).addBox(-43F, 0F, 0.39F, 43F, 0F, 21F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0.733F, 0F));
        PartDefinition p_wing_r_f3 = p_wing_r_hand.addOrReplaceChild("wing_r_f3", CubeListBuilder.create()
                .texOffs(320, 444).addBox(-18.86F, -1F, -1F, 19F, 2F, 2F, new CubeDeformation(-0.14F, 0F, 0F))
                .texOffs(404, 444).addBox(-36F, -1F, -1F, 18F, 2F, 2F, new CubeDeformation(0F, -0.4F, -0.4F))
                .texOffs(264, 69).addBox(-36F, 0F, 0.3F, 36F, 0F, 35F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 1.2915F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
