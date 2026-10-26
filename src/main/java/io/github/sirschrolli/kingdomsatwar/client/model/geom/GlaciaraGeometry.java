package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie von Glaciara, dem Eisdrachen-Boss (Grundgröße, im Spiel ×2,1).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class GlaciaraGeometry {
    public static final int TEXTURE_WIDTH = 512;
    public static final int TEXTURE_HEIGHT = 384;

    private GlaciaraGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-9F, -9F, -18F, 18F, 17F, 36F)
                .texOffs(383, 0).addBox(-7.5F, -12F, -15F, 15F, 3.5F, 28F)
                .texOffs(95, 85).addBox(-8F, 7F, -17F, 16F, 4F, 18F)
                .texOffs(389, 135).addBox(-7F, 7F, 1F, 14F, 3F, 14F)
                .texOffs(263, 85).addBox(-11F, -6F, -16F, 2.5F, 11F, 10F)
                .texOffs(290, 85).addBox(8.5F, -6F, -16F, 2.5F, 11F, 10F)
                .texOffs(164, 85).addBox(-10.5F, -5F, 6F, 2F, 11F, 11F)
                .texOffs(191, 85).addBox(8.5F, -5F, 6F, 2F, 11F, 11F)
                .texOffs(129, 187).addBox(-1.2F, -18F, -15F, 2.4F, 6.5F, 3.2F)
                .texOffs(408, 209).addBox(-0.6F, -20F, -14.4F, 1.2F, 2.2F, 1.8F)
                .texOffs(344, 172).addBox(-1.2F, -20F, -9F, 2.4F, 8.5F, 3.2F)
                .texOffs(417, 209).addBox(-0.6F, -22F, -8.4F, 1.2F, 2.2F, 1.8F)
                .texOffs(194, 172).addBox(-1.2F, -21F, -3F, 2.4F, 9.5F, 3.2F)
                .texOffs(426, 209).addBox(-0.6F, -23F, -2.4F, 1.2F, 2.2F, 1.8F)
                .texOffs(359, 172).addBox(-1.2F, -20F, 3F, 2.4F, 8.5F, 3.2F)
                .texOffs(435, 209).addBox(-0.6F, -22F, 3.6F, 1.2F, 2.2F, 1.8F)
                .texOffs(144, 187).addBox(-1.2F, -18.5F, 9F, 2.4F, 7F, 3.2F)
                .texOffs(444, 209).addBox(-0.6F, -20.5F, 9.6F, 1.2F, 2.2F, 1.8F),
                PartPose.offset(0F, -10F, 0F));
        PartDefinition p_neck1 = p_body.addOrReplaceChild("neck1", CubeListBuilder.create()
                .texOffs(285, 114).addBox(-5.5F, -5.5F, -7F, 11F, 11F, 7.5F)
                .texOffs(122, 200).addBox(-4.5F, 5F, -6F, 9F, 1.5F, 5.5F)
                .texOffs(406, 200).addBox(-0.8F, -8.8F, -4F, 1.6F, 3.9F, 2.4F),
                PartPose.offsetAndRotation(0F, -6F, -17F, -0.5934F, 0F, 0F));
        PartDefinition p_neck2 = p_neck1.addOrReplaceChild("neck2", CubeListBuilder.create()
                .texOffs(315, 135).addBox(-5F, -5F, -6.7F, 10F, 10F, 7.2F)
                .texOffs(153, 200).addBox(-4F, 4.5F, -5.7F, 8F, 1.5F, 5.2F),
                PartPose.offsetAndRotation(0F, 0F, -6.5F, -0.2793F, 0F, 0F));
        PartDefinition p_neck3 = p_neck2.addOrReplaceChild("neck3", CubeListBuilder.create()
                .texOffs(446, 135).addBox(-4.6F, -4.6F, -6.5F, 9.2F, 9.2F, 7F)
                .texOffs(236, 200).addBox(-3.6F, 4.1F, -5.5F, 7.2F, 1.5F, 5F)
                .texOffs(417, 200).addBox(-0.8F, -7.8F, -4F, 1.6F, 3.4F, 2.4F),
                PartPose.offsetAndRotation(0F, 0F, -6.2F, -0.1396F, 0F, 0F));
        PartDefinition p_neck4 = p_neck3.addOrReplaceChild("neck4", CubeListBuilder.create()
                .texOffs(35, 154).addBox(-4.25F, -4.25F, -6.5F, 8.5F, 8.5F, 7F)
                .texOffs(263, 200).addBox(-3.25F, 3.75F, -5.5F, 6.5F, 1.5F, 5F),
                PartPose.offsetAndRotation(0F, 0F, -6F, -0.0349F, 0F, 0F));
        PartDefinition p_neck5 = p_neck4.addOrReplaceChild("neck5", CubeListBuilder.create()
                .texOffs(398, 154).addBox(-4F, -4F, -6.3F, 8F, 8F, 6.8F)
                .texOffs(288, 200).addBox(-3F, 3.5F, -5.3F, 6F, 1.5F, 4.8F)
                .texOffs(320, 209).addBox(-0.8F, -6.8F, -4F, 1.6F, 2.9F, 2.4F),
                PartPose.offsetAndRotation(0F, 0F, -6F, 0.0698F, 0F, 0F));
        PartDefinition p_neck6 = p_neck5.addOrReplaceChild("neck6", CubeListBuilder.create()
                .texOffs(429, 154).addBox(-3.75F, -3.75F, -6.1F, 7.5F, 7.5F, 6.6F)
                .texOffs(311, 200).addBox(-2.75F, 3.25F, -5.1F, 5.5F, 1.5F, 4.6F),
                PartPose.offsetAndRotation(0F, 0F, -5.8F, 0.1571F, 0F, 0F));
        PartDefinition p_neck7 = p_neck6.addOrReplaceChild("neck7", CubeListBuilder.create()
                .texOffs(107, 172).addBox(-3.5F, -3.5F, -6F, 7F, 7F, 6.5F)
                .texOffs(334, 200).addBox(-2.5F, 3F, -5F, 5F, 1.5F, 4.5F)
                .texOffs(331, 209).addBox(-0.8F, -5.8F, -4F, 1.6F, 2.4F, 2.4F),
                PartPose.offsetAndRotation(0F, 0F, -5.6F, 0.2094F, 0F, 0F));
        PartDefinition p_neck8 = p_neck7.addOrReplaceChild("neck8", CubeListBuilder.create()
                .texOffs(136, 172).addBox(-3.3F, -3.3F, -5.9F, 6.6F, 6.6F, 6.4F)
                .texOffs(355, 200).addBox(-2.3F, 2.8F, -4.9F, 4.6F, 1.5F, 4.4F),
                PartPose.offsetAndRotation(0F, 0F, -5.5F, 0.1571F, 0F, 0F));
        PartDefinition p_head = p_neck8.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(218, 85).addBox(-5F, -4.5F, -12F, 10F, 9F, 12F)
                .texOffs(149, 114).addBox(-3.6F, -2.8F, -25F, 7.2F, 6F, 13.5F)
                .texOffs(0, 187).addBox(-2.4F, -4F, -23.5F, 4.8F, 1.5F, 10F)
                .texOffs(182, 221).addBox(-3F, -3.3F, -25.3F, 2F, 1.2F, 1.2F)
                .texOffs(191, 221).addBox(1F, -3.3F, -25.3F, 2F, 1.2F, 1.2F)
                .texOffs(0, 172).addBox(-3.2F, 3F, -24.6F, 6.4F, 1.2F, 11.5F)
                .texOffs(460, 154).addBox(-3.2F, 2.9F, -24.5F, 6.4F, 0.2F, 12.5F)
                .texOffs(342, 209).addBox(4.6F, -2.4F, -11.5F, 1F, 1.8F, 3.4F)
                .texOffs(353, 209).addBox(-5.6F, -2.4F, -11.5F, 1F, 1.8F, 3.4F)
                .texOffs(376, 200).addBox(4.6F, -4.8F, -11F, 1.4F, 1.2F, 5F)
                .texOffs(391, 200).addBox(-6F, -4.8F, -11F, 1.4F, 1.2F, 5F)
                .texOffs(306, 172).addBox(5F, -1.5F, -8F, 1.4F, 6F, 7F)
                .texOffs(325, 172).addBox(-6.4F, -1.5F, -8F, 1.4F, 6F, 7F),
                PartPose.offsetAndRotation(0F, 0F, -5.5F, 0.5585F, 0F, 0F));
        PartDefinition p_crown = p_head.addOrReplaceChild("crown", CubeListBuilder.create()
                .texOffs(447, 187).addBox(-5F, -7F, -0.8F, 1.6F, 7F, 1.92F)
                .texOffs(200, 221).addBox(-4.6F, -9.2F, -0.4F, 0.8F, 2.4F, 0.8F)
                .texOffs(374, 172).addBox(-3.15F, -10F, -0.95F, 1.9F, 10F, 2.28F)
                .texOffs(205, 221).addBox(-2.675F, -12.2F, -0.475F, 0.95F, 2.4F, 0.95F)
                .texOffs(118, 154).addBox(-1.1F, -13F, -1.1F, 2.2F, 13F, 2.64F)
                .texOffs(453, 209).addBox(-0.55F, -15.2F, -0.55F, 1.1F, 2.4F, 1.1F)
                .texOffs(385, 172).addBox(1.25F, -10F, -0.95F, 1.9F, 10F, 2.28F)
                .texOffs(210, 221).addBox(1.725F, -12.2F, -0.475F, 0.95F, 2.4F, 0.95F)
                .texOffs(456, 187).addBox(3.4F, -7F, -0.8F, 1.6F, 7F, 1.92F)
                .texOffs(215, 221).addBox(3.8F, -9.2F, -0.4F, 0.8F, 2.4F, 0.8F),
                PartPose.offsetAndRotation(0F, -4.5F, -6F, -0.4887F, 0F, 0F));
        PartDefinition p_jaw = p_head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(270, 135).addBox(-3.4F, 0F, -14.5F, 6.8F, 3F, 15F)
                .texOffs(396, 172).addBox(-2.8F, -1.1F, -14.2F, 5.6F, 1.1F, 10F)
                .texOffs(39, 172).addBox(-2.4F, -0.2F, -13.5F, 4.8F, 0.4F, 12.5F)
                .texOffs(364, 209).addBox(-0.7F, 2.6F, -8F, 1.4F, 2.2F, 2.4F),
                PartPose.offsetAndRotation(0F, 3.2F, -10F, 0.0524F, 0F, 0F));
        PartDefinition p_horn_l1 = p_head.addOrReplaceChild("horn_l1", CubeListBuilder.create()
                .texOffs(31, 187).addBox(-1.2F, -1.2F, 0F, 2.4F, 2.4F, 9F),
                PartPose.offsetAndRotation(3F, -4.2F, -4F, -0.3142F, 0.2443F, 0F));
        PartDefinition p_horn_l2 = p_horn_l1.addOrReplaceChild("horn_l2", CubeListBuilder.create()
                .texOffs(390, 187).addBox(-0.8F, -0.8F, 0F, 1.6F, 1.6F, 7F),
                PartPose.offsetAndRotation(0F, 0F, 9F, 0.1745F, 0.0698F, 0F));
        PartDefinition p_horn_r1 = p_head.addOrReplaceChild("horn_r1", CubeListBuilder.create()
                .texOffs(56, 187).addBox(-1.2F, -1.2F, 0F, 2.4F, 2.4F, 9F),
                PartPose.offsetAndRotation(-3F, -4.2F, -4F, -0.3142F, -0.2443F, 0F));
        PartDefinition p_horn_r2 = p_horn_r1.addOrReplaceChild("horn_r2", CubeListBuilder.create()
                .texOffs(409, 187).addBox(-0.8F, -0.8F, 0F, 1.6F, 1.6F, 7F),
                PartPose.offsetAndRotation(0F, 0F, 9F, 0.1745F, -0.0698F, 0F));
        PartDefinition p_tail1 = p_body.addOrReplaceChild("tail1", CubeListBuilder.create()
                .texOffs(194, 114).addBox(-5F, -4.75F, -0.5F, 10F, 9.5F, 10F)
                .texOffs(159, 187).addBox(-4F, 4.25F, 0.5F, 8F, 1.5F, 8F)
                .texOffs(214, 200).addBox(-0.7F, -8.95F, 2F, 1.4F, 4.7F, 2.6F),
                PartPose.offsetAndRotation(0F, -3F, 17F, -0.2094F, 0F, 0F));
        PartDefinition p_tail2 = p_tail1.addOrReplaceChild("tail2", CubeListBuilder.create()
                .texOffs(324, 114).addBox(-4.5F, -4.25F, -0.5F, 9F, 8.5F, 10F)
                .texOffs(192, 187).addBox(-3.5F, 3.75F, 0.5F, 7F, 1.5F, 8F)
                .texOffs(225, 200).addBox(-0.7F, -8F, 2F, 1.4F, 4.25F, 2.6F),
                PartPose.offsetAndRotation(0F, 0F, 9F, -0.1396F, 0F, 0F));
        PartDefinition p_tail3 = p_tail2.addOrReplaceChild("tail3", CubeListBuilder.create()
                .texOffs(352, 135).addBox(-4F, -3.75F, -0.5F, 8F, 7.5F, 9.5F)
                .texOffs(223, 187).addBox(-3F, 3.25F, 0.5F, 6F, 1.5F, 7.5F)
                .texOffs(428, 200).addBox(-0.7F, -7.05F, 2F, 1.4F, 3.8F, 2.6F),
                PartPose.offsetAndRotation(0F, 0F, 9F, -0.0873F, 0F, 0F));
        PartDefinition p_tail4 = p_tail3.addOrReplaceChild("tail4", CubeListBuilder.create()
                .texOffs(0, 154).addBox(-3.5F, -3.3F, -0.5F, 7F, 6.6F, 9.5F)
                .texOffs(252, 187).addBox(-2.5F, 2.8F, 0.5F, 5F, 1.5F, 7.5F)
                .texOffs(439, 200).addBox(-0.7F, -6.15F, 2F, 1.4F, 3.35F, 2.6F),
                PartPose.offsetAndRotation(0F, 0F, 8.5F, -0.0349F, 0F, 0F));
        PartDefinition p_tail5 = p_tail4.addOrReplaceChild("tail5", CubeListBuilder.create()
                .texOffs(365, 154).addBox(-3.1F, -2.9F, -0.5F, 6.2F, 5.8F, 9F)
                .texOffs(321, 187).addBox(-2.1F, 2.4F, 0.5F, 4.2F, 1.5F, 7F)
                .texOffs(375, 209).addBox(-0.7F, -5.3F, 2F, 1.4F, 2.9F, 2.6F),
                PartPose.offset(0F, 0F, 8.5F));
        PartDefinition p_tail6 = p_tail5.addOrReplaceChild("tail6", CubeListBuilder.create()
                .texOffs(76, 172).addBox(-2.7F, -2.5F, -0.5F, 5.4F, 5F, 9F)
                .texOffs(346, 187).addBox(-1.7F, 2F, 0.5F, 3.4F, 1.5F, 7F)
                .texOffs(386, 209).addBox(-0.7F, -4.45F, 2F, 1.4F, 2.45F, 2.6F),
                PartPose.offsetAndRotation(0F, 0F, 8F, 0.0349F, 0F, 0F));
        PartDefinition p_tail7 = p_tail6.addOrReplaceChild("tail7", CubeListBuilder.create()
                .texOffs(165, 172).addBox(-2.3F, -2.2F, -0.5F, 4.6F, 4.4F, 8.5F)
                .texOffs(369, 187).addBox(-1.3F, 1.7F, 0.5F, 2.6F, 1.5F, 6.5F)
                .texOffs(397, 209).addBox(-0.7F, -3.7F, 2F, 1.4F, 2F, 2.6F),
                PartPose.offsetAndRotation(0F, 0F, 8F, 0.0524F, 0F, 0F));
        PartDefinition p_tail8 = p_tail7.addOrReplaceChild("tail8", CubeListBuilder.create()
                .texOffs(279, 172).addBox(-1.9F, -1.8F, -0.5F, 3.8F, 3.6F, 8.5F)
                .texOffs(428, 187).addBox(-0.9F, 1.3F, 0.5F, 1.8F, 1.5F, 6.5F),
                PartPose.offsetAndRotation(0F, 0F, 7.5F, 0.0524F, 0F, 0F));
        PartDefinition p_tail9 = p_tail8.addOrReplaceChild("tail9", CubeListBuilder.create()
                .texOffs(81, 187).addBox(-1.6F, -1.5F, -0.5F, 3.2F, 3F, 8F)
                .texOffs(182, 200).addBox(-0.6F, 1F, 0.5F, 1.2F, 1.5F, 6F),
                PartPose.offsetAndRotation(0F, 0F, 7.5F, 0.0524F, 0F, 0F));
        PartDefinition p_tail10 = p_tail9.addOrReplaceChild("tail10", CubeListBuilder.create()
                .texOffs(106, 187).addBox(-1.3F, -1.2F, -0.5F, 2.6F, 2.4F, 7.5F)
                .texOffs(199, 200).addBox(-0.3F, 0.7F, 0.5F, 0.6F, 1.5F, 5.5F),
                PartPose.offsetAndRotation(0F, 0F, 7F, 0.0349F, 0F, 0F));
        PartDefinition p_tailfan_0 = p_tail10.addOrReplaceChild("tailfan_0", CubeListBuilder.create()
                .texOffs(68, 154).addBox(-0.4F, -2.5F, 0F, 0.8F, 5F, 11F),
                PartPose.offsetAndRotation(0F, 0F, 6F, 0F, -0.384F, 0F));
        PartDefinition p_tailfan_1 = p_tail10.addOrReplaceChild("tailfan_1", CubeListBuilder.create()
                .texOffs(363, 114).addBox(-0.4F, -2.5F, 0F, 0.8F, 5F, 14F),
                PartPose.offset(0F, 0F, 6F));
        PartDefinition p_tailfan_2 = p_tail10.addOrReplaceChild("tailfan_2", CubeListBuilder.create()
                .texOffs(93, 154).addBox(-0.4F, -2.5F, 0F, 0.8F, 5F, 11F),
                PartPose.offsetAndRotation(0F, 0F, 6F, 0F, 0.384F, 0F));
        PartDefinition p_front_leg_l = p_body.addOrReplaceChild("front_leg_l", CubeListBuilder.create()
                .texOffs(37, 85).addBox(-3.25F, -2.275F, -3.25F, 6.5F, 15.275F, 6.5F),
                PartPose.offsetAndRotation(8F, 3F, -12F, 0.1047F, 0F, 0F));
        PartDefinition p_front_shin_l = p_front_leg_l.addOrReplaceChild("front_shin_l", CubeListBuilder.create()
                .texOffs(394, 114).addBox(-2.5F, -1F, -2.5F, 5F, 13.5F, 5F),
                PartPose.offsetAndRotation(0F, 12.5F, 0F, -0.2094F, 0F, 0F));
        PartDefinition p_front_foot_l = p_front_shin_l.addOrReplaceChild("front_foot_l", CubeListBuilder.create()
                .texOffs(429, 172).addBox(-3.25F, 0F, -6.3F, 6.5F, 2.6F, 9F)
                .texOffs(138, 209).addBox(-2.9167F, 0.91F, -9.5F, 1.5F, 1.69F, 3.2F)
                .texOffs(151, 209).addBox(-0.75F, 0.91F, -9.5F, 1.5F, 1.69F, 3.2F)
                .texOffs(164, 209).addBox(1.4167F, 0.91F, -9.5F, 1.5F, 1.69F, 3.2F),
                PartPose.offsetAndRotation(0F, 12F, 0F, 0.1047F, 0F, 0F));
        PartDefinition p_hind_leg_l = p_body.addOrReplaceChild("hind_leg_l", CubeListBuilder.create()
                .texOffs(464, 54).addBox(-4F, -2.8F, -4.75F, 8F, 17.3F, 9.5F),
                PartPose.offsetAndRotation(8.5F, 2F, 11F, -0.1745F, 0F, 0F));
        PartDefinition p_hind_shin_l = p_hind_leg_l.addOrReplaceChild("hind_shin_l", CubeListBuilder.create()
                .texOffs(235, 114).addBox(-2.8F, -1F, -2.8F, 5.6F, 14F, 5.6F),
                PartPose.offsetAndRotation(0F, 14F, 0F, 0.5236F, 0F, 0F));
        PartDefinition p_hind_foot_l = p_hind_shin_l.addOrReplaceChild("hind_foot_l", CubeListBuilder.create()
                .texOffs(209, 172).addBox(-3.5F, 0F, -7F, 7F, 2.8F, 10F)
                .texOffs(177, 209).addBox(-3.0833F, 0.98F, -10.6F, 1.5F, 1.82F, 3.6F)
                .texOffs(190, 209).addBox(-0.75F, 0.98F, -10.6F, 1.5F, 1.82F, 3.6F)
                .texOffs(203, 209).addBox(1.5833F, 0.98F, -10.6F, 1.5F, 1.82F, 3.6F),
                PartPose.offsetAndRotation(0F, 12.5F, 0F, -0.3491F, 0F, 0F));
        PartDefinition p_front_leg_r = p_body.addOrReplaceChild("front_leg_r", CubeListBuilder.create()
                .texOffs(66, 85).addBox(-3.25F, -2.275F, -3.25F, 6.5F, 15.275F, 6.5F),
                PartPose.offsetAndRotation(-8F, 3F, -12F, 0.1047F, 0F, 0F));
        PartDefinition p_front_shin_r = p_front_leg_r.addOrReplaceChild("front_shin_r", CubeListBuilder.create()
                .texOffs(415, 114).addBox(-2.5F, -1F, -2.5F, 5F, 13.5F, 5F),
                PartPose.offsetAndRotation(0F, 12.5F, 0F, -0.2094F, 0F, 0F));
        PartDefinition p_front_foot_r = p_front_shin_r.addOrReplaceChild("front_foot_r", CubeListBuilder.create()
                .texOffs(462, 172).addBox(-3.25F, 0F, -6.3F, 6.5F, 2.6F, 9F)
                .texOffs(216, 209).addBox(-2.9167F, 0.91F, -9.5F, 1.5F, 1.69F, 3.2F)
                .texOffs(229, 209).addBox(-0.75F, 0.91F, -9.5F, 1.5F, 1.69F, 3.2F)
                .texOffs(242, 209).addBox(1.4167F, 0.91F, -9.5F, 1.5F, 1.69F, 3.2F),
                PartPose.offsetAndRotation(0F, 12F, 0F, 0.1047F, 0F, 0F));
        PartDefinition p_hind_leg_r = p_body.addOrReplaceChild("hind_leg_r", CubeListBuilder.create()
                .texOffs(0, 85).addBox(-4F, -2.8F, -4.75F, 8F, 17.3F, 9.5F),
                PartPose.offsetAndRotation(-8.5F, 2F, 11F, -0.1745F, 0F, 0F));
        PartDefinition p_hind_shin_r = p_hind_leg_r.addOrReplaceChild("hind_shin_r", CubeListBuilder.create()
                .texOffs(260, 114).addBox(-2.8F, -1F, -2.8F, 5.6F, 14F, 5.6F),
                PartPose.offsetAndRotation(0F, 14F, 0F, 0.5236F, 0F, 0F));
        PartDefinition p_hind_foot_r = p_hind_shin_r.addOrReplaceChild("hind_foot_r", CubeListBuilder.create()
                .texOffs(244, 172).addBox(-3.5F, 0F, -7F, 7F, 2.8F, 10F)
                .texOffs(255, 209).addBox(-3.0833F, 0.98F, -10.6F, 1.5F, 1.82F, 3.6F)
                .texOffs(268, 209).addBox(-0.75F, 0.98F, -10.6F, 1.5F, 1.82F, 3.6F)
                .texOffs(281, 209).addBox(1.5833F, 0.98F, -10.6F, 1.5F, 1.82F, 3.6F),
                PartPose.offsetAndRotation(0F, 12.5F, 0F, -0.3491F, 0F, 0F));
        PartDefinition p_wing_l = p_body.addOrReplaceChild("wing_l", CubeListBuilder.create()
                .texOffs(0, 200).addBox(0F, -1.8F, -1.8F, 26F, 3.6F, 3.6F)
                .texOffs(246, 54).addBox(0F, 0F, 1.3F, 26F, 0F, 28F),
                PartPose.offset(8.5F, -9F, -10F));
        PartDefinition p_wing_l_fore = p_wing_l.addOrReplaceChild("wing_l_fore", CubeListBuilder.create()
                .texOffs(0, 209).addBox(0F, -1.35F, -1.35F, 31F, 2.7F, 2.7F)
                .texOffs(0, 54).addBox(0F, 0F, 0.85F, 31F, 0F, 30F),
                PartPose.offset(26F, 0F, 0F));
        PartDefinition p_wing_l_hand = p_wing_l_fore.addOrReplaceChild("wing_l_hand", CubeListBuilder.create()
                .texOffs(279, 187).addBox(-2.5F, -2.5F, -2.5F, 5F, 5F, 5F)
                .texOffs(294, 209).addBox(-1F, -1F, -6F, 2F, 2F, 4F),
                PartPose.offset(31F, 0F, 0F));
        PartDefinition p_wing_l_f1 = p_wing_l_hand.addOrReplaceChild("wing_l_f1", CubeListBuilder.create()
                .texOffs(0, 216).addBox(0F, -0.925F, -0.925F, 54F, 1.85F, 1.85F)
                .texOffs(317, 85).addBox(0F, 0F, 0.525F, 54F, 0F, 20F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, -0.1396F, 0F));
        PartDefinition p_wing_l_f2 = p_wing_l_hand.addOrReplaceChild("wing_l_f2", CubeListBuilder.create()
                .texOffs(226, 216).addBox(0F, -0.75F, -0.75F, 49F, 1.5F, 1.5F)
                .texOffs(0, 135).addBox(0F, 0F, 0.35F, 49F, 0F, 18F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, -0.5236F, 0F));
        PartDefinition p_wing_l_f3 = p_wing_l_hand.addOrReplaceChild("wing_l_f3", CubeListBuilder.create()
                .texOffs(0, 221).addBox(0F, -0.575F, -0.575F, 43F, 1.15F, 1.15F)
                .texOffs(131, 154).addBox(0F, 0F, 0.175F, 43F, 0F, 15F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, -0.9076F, 0F));
        PartDefinition p_wing_l_f4 = p_wing_l_hand.addOrReplaceChild("wing_l_f4", CubeListBuilder.create()
                .texOffs(220, 221).addBox(0F, -0.5F, -0.5F, 35F, 1F, 1F)
                .texOffs(109, 0).addBox(0F, 0F, 0.1F, 35F, 0F, 33F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, -1.2915F, 0F));
        PartDefinition p_wing_r = p_body.addOrReplaceChild("wing_r", CubeListBuilder.create()
                .texOffs(61, 200).addBox(-26F, -1.8F, -1.8F, 26F, 3.6F, 3.6F)
                .texOffs(355, 54).addBox(-26F, 0F, 1.3F, 26F, 0F, 28F),
                PartPose.offset(-8.5F, -9F, -10F));
        PartDefinition p_wing_r_fore = p_wing_r.addOrReplaceChild("wing_r_fore", CubeListBuilder.create()
                .texOffs(69, 209).addBox(-31F, -1.35F, -1.35F, 31F, 2.7F, 2.7F)
                .texOffs(123, 54).addBox(-31F, 0F, 0.85F, 31F, 0F, 30F),
                PartPose.offset(-26F, 0F, 0F));
        PartDefinition p_wing_r_hand = p_wing_r_fore.addOrReplaceChild("wing_r_hand", CubeListBuilder.create()
                .texOffs(300, 187).addBox(-2.5F, -2.5F, -2.5F, 5F, 5F, 5F)
                .texOffs(307, 209).addBox(-1F, -1F, -6F, 2F, 2F, 4F),
                PartPose.offset(-31F, 0F, 0F));
        PartDefinition p_wing_r_f1 = p_wing_r_hand.addOrReplaceChild("wing_r_f1", CubeListBuilder.create()
                .texOffs(113, 216).addBox(-54F, -0.925F, -0.925F, 54F, 1.85F, 1.85F)
                .texOffs(0, 114).addBox(-54F, 0F, 0.525F, 54F, 0F, 20F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0.1396F, 0F));
        PartDefinition p_wing_r_f2 = p_wing_r_hand.addOrReplaceChild("wing_r_f2", CubeListBuilder.create()
                .texOffs(329, 216).addBox(-49F, -0.75F, -0.75F, 49F, 1.5F, 1.5F)
                .texOffs(135, 135).addBox(-49F, 0F, 0.35F, 49F, 0F, 18F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0.5236F, 0F));
        PartDefinition p_wing_r_f3 = p_wing_r_hand.addOrReplaceChild("wing_r_f3", CubeListBuilder.create()
                .texOffs(91, 221).addBox(-43F, -0.575F, -0.575F, 43F, 1.15F, 1.15F)
                .texOffs(248, 154).addBox(-43F, 0F, 0.175F, 43F, 0F, 15F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0.9076F, 0F));
        PartDefinition p_wing_r_f4 = p_wing_r_hand.addOrReplaceChild("wing_r_f4", CubeListBuilder.create()
                .texOffs(293, 221).addBox(-35F, -0.5F, -0.5F, 35F, 1F, 1F)
                .texOffs(246, 0).addBox(-35F, 0F, 0.1F, 35F, 0F, 33F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 1.2915F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
