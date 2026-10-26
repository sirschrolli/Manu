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
                .texOffs(0, 0).addBox(-15F, -12F, -23F, 30F, 22F, 46F)
                .texOffs(153, 0).addBox(-12.5F, -16F, -20F, 25F, 4.5F, 37F)
                .texOffs(149, 137).addBox(-9F, -18.5F, -14F, 18F, 3F, 23F)
                .texOffs(0, 105).addBox(-13F, 8F, -22F, 26F, 5F, 26F)
                .texOffs(0, 214).addBox(-12F, 8F, 4F, 24F, 3.5F, 17F)
                .texOffs(45, 137).addBox(-17F, -9F, -21F, 3F, 14F, 14F)
                .texOffs(80, 137).addBox(14F, -9F, -21F, 3F, 14F, 14F)
                .texOffs(380, 105).addBox(-16.5F, -7F, 8F, 2.5F, 14F, 15F)
                .texOffs(417, 105).addBox(14F, -7F, 8F, 2.5F, 14F, 15F)
                .texOffs(115, 271).addBox(-10F, -22F, -17F, 8F, 5F, 8F)
                .texOffs(0, 255).addBox(2F, -23.5F, -11F, 9F, 6F, 9F)
                .texOffs(464, 271).addBox(-7F, -22F, -2F, 7F, 4.5F, 7F)
                .texOffs(433, 271).addBox(4F, -21F, 5F, 8F, 5F, 7F)
                .texOffs(90, 299).addBox(-4F, -20F, 12F, 7F, 4F, 6F)
                .texOffs(0, 310).addBox(8F, -18.5F, -19F, 5F, 3.5F, 5F)
                .texOffs(99, 334).addBox(-7F, -23F, -15F, 1.6F, 1.6F, 1.6F)
                .texOffs(108, 334).addBox(5F, -24.8F, -8F, 1.6F, 1.6F, 1.6F)
                .texOffs(117, 334).addBox(-4.5F, -23F, 0F, 1.6F, 1.6F, 1.6F)
                .texOffs(126, 334).addBox(7F, -22.2F, 7F, 1.6F, 1.6F, 1.6F)
                .texOffs(135, 334).addBox(-1.5F, -21.2F, 14F, 1.6F, 1.6F, 1.6F)
                .texOffs(144, 334).addBox(-9F, -23F, -12F, 1.6F, 1.6F, 1.6F)
                .texOffs(153, 334).addBox(3F, -24.8F, -12F, 1.6F, 1.6F, 1.6F)
                .texOffs(162, 334).addBox(9.5F, -19.8F, -17F, 1.6F, 1.6F, 1.6F)
                .texOffs(171, 334).addBox(-2F, -22.6F, -6F, 1.6F, 1.6F, 1.6F)
                .texOffs(180, 334).addBox(10F, -22.2F, 3F, 1.6F, 1.6F, 1.6F)
                .texOffs(189, 334).addBox(-8F, -21.5F, 6F, 1.6F, 1.6F, 1.6F)
                .texOffs(199, 320).addBox(15F, -4F, -8F, 3F, 1.2F, 5F)
                .texOffs(323, 320).addBox(15F, -1F, -3F, 2.5F, 1.2F, 4F)
                .texOffs(347, 310).addBox(15F, -6F, 9F, 3F, 1.2F, 6F)
                .texOffs(368, 320).addBox(15F, 0F, 14F, 2F, 1.2F, 3.5F)
                .texOffs(216, 320).addBox(-18F, -4F, -8F, 3F, 1.2F, 5F)
                .texOffs(338, 320).addBox(-17.5F, -1F, -3F, 2.5F, 1.2F, 4F)
                .texOffs(366, 310).addBox(-18F, -6F, 9F, 3F, 1.2F, 6F)
                .texOffs(381, 320).addBox(-17F, 0F, 14F, 2F, 1.2F, 3.5F),
                PartPose.offset(0F, -8F, 0F));
        PartDefinition p_tree_0 = p_body.addOrReplaceChild("tree_0", CubeListBuilder.create()
                .texOffs(75, 255).addBox(-1.3F, -11F, -1.3F, 2.6F, 12F, 2.6F)
                .texOffs(362, 255).addBox(-4.5F, -15.5F, -4.2F, 9F, 5F, 8.4F)
                .texOffs(117, 299).addBox(-3F, -18.5F, -2.5F, 6.4F, 3.4F, 5.6F)
                .texOffs(21, 310).addBox(1.5F, -14F, -1F, 5F, 3.5F, 5F)
                .texOffs(198, 334).addBox(-2.2F, -19.2F, -0.6F, 1.6F, 1.6F, 1.6F),
                PartPose.offsetAndRotation(-4F, -18F, -14F, -0.1745F, -0.3491F, 0F));
        PartDefinition p_tree_0_bough = p_tree_0.addOrReplaceChild("tree_0_bough", CubeListBuilder.create()
                .texOffs(120, 320).addBox(-0.8F, -5.5F, -0.8F, 1.6F, 6F, 1.6F)
                .texOffs(351, 286).addBox(-3.2F, -9F, -3F, 6.4F, 4.2F, 6F),
                PartPose.offsetAndRotation(0F, -6.05F, 0F, 0F, 0F, 0.733F));
        PartDefinition p_tree_1 = p_body.addOrReplaceChild("tree_1", CubeListBuilder.create()
                .texOffs(235, 271).addBox(-1.3F, -9F, -1.3F, 2.6F, 10F, 2.6F)
                .texOffs(399, 255).addBox(-4.5F, -13.5F, -4.2F, 9F, 5F, 8.4F)
                .texOffs(144, 299).addBox(-3F, -16.5F, -2.5F, 6.4F, 3.4F, 5.6F)
                .texOffs(42, 310).addBox(1.5F, -12F, -1F, 5F, 3.5F, 5F)
                .texOffs(207, 334).addBox(-2.2F, -17.2F, -0.6F, 1.6F, 1.6F, 1.6F),
                PartPose.offsetAndRotation(5F, -18F, 2F, -0.2793F, 0.4363F, 0F));
        PartDefinition p_tree_1_bough = p_tree_1.addOrReplaceChild("tree_1_bough", CubeListBuilder.create()
                .texOffs(129, 320).addBox(-0.8F, -5.5F, -0.8F, 1.6F, 6F, 1.6F)
                .texOffs(378, 286).addBox(-3.2F, -9F, -3F, 6.4F, 4.2F, 6F),
                PartPose.offsetAndRotation(0F, -4.95F, 0F, 0F, 0F, -0.733F));
        PartDefinition p_deadwood = p_body.addOrReplaceChild("deadwood", CubeListBuilder.create()
                .texOffs(248, 271).addBox(-1.1F, -9F, -1.1F, 2.2F, 10F, 2.2F),
                PartPose.offsetAndRotation(-1F, -17F, 13F, -0.5236F, -0.1396F, 0.2094F));
        PartDefinition p_deadwood_twig = p_deadwood.addOrReplaceChild("deadwood_twig", CubeListBuilder.create()
                .texOffs(138, 320).addBox(-0.6F, -5F, -0.6F, 1.2F, 5.5F, 1.2F),
                PartPose.offsetAndRotation(0F, -6F, 0F, 0.1745F, 0F, -0.7854F));
        PartDefinition p_neck1 = p_body.addOrReplaceChild("neck1", CubeListBuilder.create()
                .texOffs(289, 137).addBox(-8.5F, -8F, -8F, 17F, 16F, 8.5F)
                .texOffs(324, 299).addBox(-7.5F, 7.5F, -7F, 15F, 1.5F, 6.5F)
                .texOffs(261, 271).addBox(-7F, -10.5F, -8F, 14F, 3F, 8.5F)
                .texOffs(115, 137).addBox(8.8F, -5F, -7.5F, 0F, 20F, 7.5F)
                .texOffs(132, 137).addBox(-8.8F, -5F, -7.5F, 0F, 20F, 7.5F),
                PartPose.offsetAndRotation(0F, -7F, -22F, -0.4189F, 0F, 0F));
        PartDefinition p_neck2 = p_neck1.addOrReplaceChild("neck2", CubeListBuilder.create()
                .texOffs(192, 166).addBox(-7.75F, -7.5F, -7.5F, 15.5F, 15F, 8F)
                .texOffs(269, 310).addBox(-6.75F, 7F, -6.5F, 13.5F, 1.5F, 6F)
                .texOffs(203, 286).addBox(-6.25F, -10F, -7.5F, 12.5F, 3F, 8F)
                .texOffs(342, 137).addBox(8.05F, -4.5F, -7F, 0F, 18F, 7F)
                .texOffs(357, 137).addBox(-8.05F, -4.5F, -7F, 0F, 18F, 7F),
                PartPose.offsetAndRotation(0F, 0F, -7.5F, -0.1745F, 0F, 0F));
        PartDefinition p_neck3 = p_neck2.addOrReplaceChild("neck3", CubeListBuilder.create()
                .texOffs(100, 191).addBox(-7F, -7F, -7F, 14F, 14F, 7.5F)
                .texOffs(310, 310).addBox(-6F, 6.5F, -6F, 12F, 1.5F, 5.5F)
                .texOffs(246, 286).addBox(-5.5F, -9.5F, -7F, 11F, 3F, 7.5F)
                .texOffs(323, 166).addBox(7.3F, -4F, -6.5F, 0F, 16F, 6.5F)
                .texOffs(338, 166).addBox(-7.3F, -4F, -6.5F, 0F, 16F, 6.5F),
                PartPose.offsetAndRotation(0F, 0F, -7F, 0.0349F, 0F, 0F));
        PartDefinition p_neck4 = p_neck3.addOrReplaceChild("neck4", CubeListBuilder.create()
                .texOffs(242, 214).addBox(-6.5F, -6.5F, -6.5F, 13F, 13F, 7F)
                .texOffs(147, 320).addBox(-5.5F, 6F, -5.5F, 11F, 1.5F, 5F)
                .texOffs(55, 299).addBox(-5F, -9F, -6.5F, 10F, 3F, 7F)
                .texOffs(283, 214).addBox(6.8F, -3.5F, -6F, 0F, 14F, 6F)
                .texOffs(296, 214).addBox(-6.8F, -3.5F, -6F, 0F, 14F, 6F),
                PartPose.offsetAndRotation(0F, 0F, -6.5F, 0.1745F, 0F, 0F));
        PartDefinition p_head = p_neck4.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(315, 105).addBox(-8F, -7.5F, -16F, 16F, 13F, 16F)
                .texOffs(150, 286).addBox(-8.6F, -9F, -15.5F, 17.2F, 2.5F, 8F)
                .texOffs(0, 191).addBox(-6.5F, -4.5F, -28.5F, 13F, 9F, 13F)
                .texOffs(280, 255).addBox(-4.5F, -6.5F, -27F, 9F, 2.5F, 10.5F)
                .texOffs(77, 334).addBox(-5F, -4.2F, -28.8F, 3F, 2F, 1.5F)
                .texOffs(88, 334).addBox(2F, -4.2F, -28.8F, 3F, 2F, 1.5F)
                .texOffs(139, 255).addBox(-6F, 4F, -28F, 12F, 1.5F, 12F)
                .texOffs(88, 255).addBox(-6F, 3.9F, -27.9F, 12F, 0.2F, 13F)
                .texOffs(233, 320).addBox(7.6F, -4.2F, -14.5F, 1F, 2.4F, 4F)
                .texOffs(244, 320).addBox(-8.6F, -4.2F, -14.5F, 1F, 2.4F, 4F)
                .texOffs(196, 236).addBox(7.8F, -1F, -15F, 1.6F, 7F, 10F)
                .texOffs(221, 236).addBox(-9.4F, -1F, -15F, 1.6F, 7F, 10F)
                .texOffs(369, 299).addBox(-5F, -8.8F, -8F, 10F, 2F, 7F),
                PartPose.offsetAndRotation(0F, 0F, -6F, 0.4887F, 0F, 0F));
        PartDefinition p_ear_l = p_head.addOrReplaceChild("ear_l", CubeListBuilder.create()
                .texOffs(37, 255).addBox(-0.1F, -3F, 0F, 0F, 6F, 9F),
                PartPose.offsetAndRotation(7.5F, -5F, -5F, -0.3491F, 0.6109F, 0F));
        PartDefinition p_ear_r = p_head.addOrReplaceChild("ear_r", CubeListBuilder.create()
                .texOffs(56, 255).addBox(-0.1F, -3F, 0F, 0F, 6F, 9F),
                PartPose.offsetAndRotation(-7.5F, -5F, -5F, -0.3491F, -0.6109F, 0F));
        PartDefinition p_jaw = p_head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(83, 214).addBox(-6F, 0F, -15.5F, 12F, 4.5F, 15.5F)
                .texOffs(308, 271).addBox(-5.2F, -1.3F, -15F, 10.4F, 1.3F, 10F)
                .texOffs(188, 255).addBox(-4.6F, -0.2F, -14.5F, 9.2F, 0.4F, 13F)
                .texOffs(180, 320).addBox(-4.5F, 4.5F, -11F, 9F, 7F, 0F)
                .texOffs(353, 320).addBox(-3.2F, 4.5F, -7F, 6.4F, 5.5F, 0F),
                PartPose.offsetAndRotation(0F, 4.5F, -13F, 0.0524F, 0F, 0F));
        PartDefinition p_antler_l1 = p_head.addOrReplaceChild("antler_l1", CubeListBuilder.create()
                .texOffs(352, 214).addBox(-2.2F, -2.2F, -0.4F, 4.4F, 4.4F, 13.3F),
                PartPose.offsetAndRotation(4.8F, -8.6F, -9F, 1.04F, 0.9828F, 0F));
        PartDefinition p_antler_l2 = p_antler_l1.addOrReplaceChild("antler_l2", CubeListBuilder.create()
                .texOffs(375, 236).addBox(-1.85F, -1.85F, -0.4F, 3.7F, 3.7F, 11.8F)
                .texOffs(437, 299).addBox(-2.7F, -2.7F, 2F, 5.4F, 3.6F, 4.5F),
                PartPose.offsetAndRotation(0F, 0F, 12.5F, -0.439F, -0.1975F, 0F));
        PartDefinition p_antler_l3 = p_antler_l2.addOrReplaceChild("antler_l3", CubeListBuilder.create()
                .texOffs(467, 255).addBox(-1.5F, -1.5F, -0.4F, 3F, 3F, 10.8F),
                PartPose.offsetAndRotation(0F, 0F, 11F, 0.3354F, -0.0243F, 0F));
        PartDefinition p_antler_l4 = p_antler_l3.addOrReplaceChild("antler_l4", CubeListBuilder.create()
                .texOffs(0, 286).addBox(-1.2F, -1.2F, -0.4F, 2.4F, 2.4F, 8.8F),
                PartPose.offsetAndRotation(0F, 0F, 10F, 0.3882F, 0.071F, 0F));
        PartDefinition p_antler_l_t4 = p_antler_l4.addOrReplaceChild("antler_l_t4", CubeListBuilder.create()
                .texOffs(198, 299).addBox(-1.1F, -1.1F, -0.3F, 2.2F, 2.2F, 6.5F)
                .texOffs(63, 310).addBox(-2.4F, -2.4F, 4F, 4.8F, 3.6F, 4.6F)
                .texOffs(255, 320).addBox(-1.6F, -3.6F, 5.4F, 3.2F, 2F, 3.2F),
                PartPose.offsetAndRotation(0F, 0F, 7.5F, 0.7049F, 0.6512F, 0F));
        PartDefinition p_antler_l_t3 = p_antler_l3.addOrReplaceChild("antler_l_t3", CubeListBuilder.create()
                .texOffs(219, 299).addBox(-1.1F, -1.1F, -0.3F, 2.2F, 2.2F, 7F)
                .texOffs(216, 334).addBox(-0.9F, -0.9F, 6.1F, 1.8F, 1.8F, 1.8F),
                PartPose.offsetAndRotation(0F, 0F, 9F, -0.0281F, 0.7206F, 0F));
        PartDefinition p_antler_l_t2 = p_antler_l2.addOrReplaceChild("antler_l_t2", CubeListBuilder.create()
                .texOffs(25, 286).addBox(-1.1F, -1.1F, -0.3F, 2.2F, 2.2F, 9F)
                .texOffs(84, 310).addBox(-2.4F, -2.4F, 6.5F, 4.8F, 3.6F, 4.6F)
                .texOffs(272, 320).addBox(-1.6F, -3.6F, 7.9F, 3.2F, 2F, 3.2F),
                PartPose.offsetAndRotation(0F, 0F, 7F, 0.6437F, 1.0691F, 0F));
        PartDefinition p_antler_l_t0 = p_antler_l1.addOrReplaceChild("antler_l_t0", CubeListBuilder.create()
                .texOffs(181, 271).addBox(-1.1F, -1.1F, -0.3F, 2.2F, 2.2F, 9.5F),
                PartPose.offsetAndRotation(0F, 0F, 3F, 0.5838F, 1.3974F, 0F));
        PartDefinition p_antler_l_t1 = p_antler_l1.addOrReplaceChild("antler_l_t1", CubeListBuilder.create()
                .texOffs(50, 286).addBox(-1.1F, -1.1F, -0.3F, 2.2F, 2.2F, 8.5F),
                PartPose.offsetAndRotation(0F, 0F, 9F, 0.4607F, 1.0308F, 0F));
        PartDefinition p_antler_r1 = p_head.addOrReplaceChild("antler_r1", CubeListBuilder.create()
                .texOffs(391, 214).addBox(-2.2F, -2.2F, -0.4F, 4.4F, 4.4F, 13.3F),
                PartPose.offsetAndRotation(-4.8F, -8.6F, -9F, 1.04F, -0.9828F, 0F));
        PartDefinition p_antler_r2 = p_antler_r1.addOrReplaceChild("antler_r2", CubeListBuilder.create()
                .texOffs(408, 236).addBox(-1.85F, -1.85F, -0.4F, 3.7F, 3.7F, 11.8F)
                .texOffs(460, 299).addBox(-2.7F, -2.7F, 2F, 5.4F, 3.6F, 4.5F),
                PartPose.offsetAndRotation(0F, 0F, 12.5F, -0.439F, 0.1975F, 0F));
        PartDefinition p_antler_r3 = p_antler_r2.addOrReplaceChild("antler_r3", CubeListBuilder.create()
                .texOffs(0, 271).addBox(-1.5F, -1.5F, -0.4F, 3F, 3F, 10.8F),
                PartPose.offsetAndRotation(0F, 0F, 11F, 0.3354F, 0.0243F, 0F));
        PartDefinition p_antler_r4 = p_antler_r3.addOrReplaceChild("antler_r4", CubeListBuilder.create()
                .texOffs(75, 286).addBox(-1.2F, -1.2F, -0.4F, 2.4F, 2.4F, 8.8F),
                PartPose.offsetAndRotation(0F, 0F, 10F, 0.3882F, -0.071F, 0F));
        PartDefinition p_antler_r_t4 = p_antler_r4.addOrReplaceChild("antler_r_t4", CubeListBuilder.create()
                .texOffs(240, 299).addBox(-1.1F, -1.1F, -0.3F, 2.2F, 2.2F, 6.5F)
                .texOffs(105, 310).addBox(-2.4F, -2.4F, 4F, 4.8F, 3.6F, 4.6F)
                .texOffs(289, 320).addBox(-1.6F, -3.6F, 5.4F, 3.2F, 2F, 3.2F),
                PartPose.offsetAndRotation(0F, 0F, 7.5F, 0.7049F, -0.6512F, 0F));
        PartDefinition p_antler_r_t3 = p_antler_r3.addOrReplaceChild("antler_r_t3", CubeListBuilder.create()
                .texOffs(261, 299).addBox(-1.1F, -1.1F, -0.3F, 2.2F, 2.2F, 7F)
                .texOffs(225, 334).addBox(-0.9F, -0.9F, 6.1F, 1.8F, 1.8F, 1.8F),
                PartPose.offsetAndRotation(0F, 0F, 9F, -0.0281F, -0.7206F, 0F));
        PartDefinition p_antler_r_t2 = p_antler_r2.addOrReplaceChild("antler_r_t2", CubeListBuilder.create()
                .texOffs(100, 286).addBox(-1.1F, -1.1F, -0.3F, 2.2F, 2.2F, 9F)
                .texOffs(126, 310).addBox(-2.4F, -2.4F, 6.5F, 4.8F, 3.6F, 4.6F)
                .texOffs(306, 320).addBox(-1.6F, -3.6F, 7.9F, 3.2F, 2F, 3.2F),
                PartPose.offsetAndRotation(0F, 0F, 7F, 0.6437F, -1.0691F, 0F));
        PartDefinition p_antler_r_t0 = p_antler_r1.addOrReplaceChild("antler_r_t0", CubeListBuilder.create()
                .texOffs(208, 271).addBox(-1.1F, -1.1F, -0.3F, 2.2F, 2.2F, 9.5F),
                PartPose.offsetAndRotation(0F, 0F, 3F, 0.5838F, -1.3974F, 0F));
        PartDefinition p_antler_r_t1 = p_antler_r1.addOrReplaceChild("antler_r_t1", CubeListBuilder.create()
                .texOffs(125, 286).addBox(-1.1F, -1.1F, -0.3F, 2.2F, 2.2F, 8.5F),
                PartPose.offsetAndRotation(0F, 0F, 9F, 0.4607F, -1.0308F, 0F));
        PartDefinition p_tail1 = p_body.addOrReplaceChild("tail1", CubeListBuilder.create()
                .texOffs(232, 137).addBox(-7.5F, -6.5F, -0.5F, 15F, 13F, 13F)
                .texOffs(29, 271).addBox(-6.5F, 6F, 0.5F, 13F, 1.5F, 11F)
                .texOffs(441, 236).addBox(-6.3F, -8.5F, 0F, 12.6F, 2.5F, 12F),
                PartPose.offsetAndRotation(0F, -4F, 22F, -0.2094F, 0F, 0F));
        PartDefinition p_tail2 = p_tail1.addOrReplaceChild("tail2", CubeListBuilder.create()
                .texOffs(141, 166).addBox(-6.5F, -5.5F, -0.5F, 13F, 11F, 12F)
                .texOffs(351, 271).addBox(-5.5F, 5F, 0.5F, 11F, 1.5F, 10F)
                .texOffs(235, 255).addBox(-5.3F, -7.5F, 0F, 10.6F, 2.5F, 11F),
                PartPose.offsetAndRotation(0F, 0F, 12F, -0.1396F, 0F, 0F));
        PartDefinition p_tail3 = p_tail2.addOrReplaceChild("tail3", CubeListBuilder.create()
                .texOffs(53, 191).addBox(-5.5F, -4.75F, -0.5F, 11F, 9.5F, 12F)
                .texOffs(394, 271).addBox(-4.5F, 4.25F, 0.5F, 9F, 1.5F, 10F)
                .texOffs(321, 255).addBox(-4.3F, -6.75F, 0F, 8.6F, 2.5F, 11F),
                PartPose.offsetAndRotation(0F, 0F, 11F, -0.0698F, 0F, 0F));
        PartDefinition p_tail4 = p_tail3.addOrReplaceChild("tail4", CubeListBuilder.create()
                .texOffs(309, 214).addBox(-4.75F, -4F, -0.5F, 9.5F, 8F, 11F)
                .texOffs(285, 286).addBox(-3.75F, 3.5F, 0.5F, 7.5F, 1.5F, 9F)
                .texOffs(78, 271).addBox(-3.55F, -6F, 0F, 7.1F, 2.5F, 10F),
                PartPose.offset(0F, 0F, 11F));
        PartDefinition p_tail5 = p_tail4.addOrReplaceChild("tail5", CubeListBuilder.create()
                .texOffs(55, 236).addBox(-4F, -3.5F, -0.5F, 8F, 7F, 11F)
                .texOffs(320, 286).addBox(-3F, 3F, 0.5F, 6F, 1.5F, 9F)
                .texOffs(148, 271).addBox(-2.8F, -5.5F, 0F, 5.6F, 2.5F, 10F),
                PartPose.offsetAndRotation(0F, 0F, 10F, 0.0524F, 0F, 0F));
        PartDefinition p_tail6 = p_tail5.addOrReplaceChild("tail6", CubeListBuilder.create()
                .texOffs(340, 236).addBox(-3.25F, -3F, -0.5F, 6.5F, 6F, 10F)
                .texOffs(171, 299).addBox(-2.25F, 2.5F, 0.5F, 4.5F, 1.5F, 8F),
                PartPose.offsetAndRotation(0F, 0F, 10F, 0.0698F, 0F, 0F));
        PartDefinition p_tail7 = p_tail6.addOrReplaceChild("tail7", CubeListBuilder.create()
                .texOffs(436, 255).addBox(-2.75F, -2.5F, -0.5F, 5.5F, 5F, 9F)
                .texOffs(483, 299).addBox(-1.75F, 2F, 0.5F, 3.5F, 1.5F, 7F)
                .texOffs(140, 214).addBox(-5.5F, -5.5F, 4.5F, 11F, 11F, 10F)
                .texOffs(404, 299).addBox(-4.2F, -6.8F, 6F, 8.4F, 1.5F, 7F)
                .texOffs(234, 334).addBox(-5.9F, -2F, 7F, 1.6F, 1.6F, 1.6F)
                .texOffs(243, 334).addBox(4.3F, 1F, 9F, 1.6F, 1.6F, 1.6F)
                .texOffs(252, 334).addBox(-1F, -7F, 10.5F, 1.6F, 1.6F, 1.6F),
                PartPose.offsetAndRotation(0F, 0F, 9F, 0.0524F, 0F, 0F));
        PartDefinition p_tailfrond_0 = p_tail7.addOrReplaceChild("tailfrond_0", CubeListBuilder.create()
                .texOffs(430, 214).addBox(-4.5F, 0F, 0F, 9F, 0F, 18F),
                PartPose.offsetAndRotation(0F, 0F, 13F, -0.2094F, -0.733F, 0F));
        PartDefinition p_tailfrond_1 = p_tail7.addOrReplaceChild("tailfrond_1", CubeListBuilder.create()
                .texOffs(353, 166).addBox(-4.5F, 0F, 0F, 9F, 0F, 22F),
                PartPose.offsetAndRotation(0F, 0F, 13F, -0.4887F, -0.2618F, 0F));
        PartDefinition p_tailfrond_2 = p_tail7.addOrReplaceChild("tailfrond_2", CubeListBuilder.create()
                .texOffs(416, 166).addBox(-4.5F, 0F, 0F, 9F, 0F, 22F),
                PartPose.offsetAndRotation(0F, 0F, 13F, -0.4887F, 0.2618F, 0F));
        PartDefinition p_tailfrond_3 = p_tail7.addOrReplaceChild("tailfrond_3", CubeListBuilder.create()
                .texOffs(0, 236).addBox(-4.5F, 0F, 0F, 9F, 0F, 18F),
                PartPose.offsetAndRotation(0F, 0F, 13F, -0.2094F, 0.733F, 0F));
        PartDefinition p_tailfrond_4 = p_tail7.addOrReplaceChild("tailfrond_4", CubeListBuilder.create()
                .texOffs(183, 214).addBox(-4.5F, 0F, 0F, 9F, 0F, 20F),
                PartPose.offsetAndRotation(0F, 0F, 13F, 0.1396F, 0F, 0F));
        PartDefinition p_front_leg_l = p_body.addOrReplaceChild("front_leg_l", CubeListBuilder.create()
                .texOffs(454, 105).addBox(-5.5F, -3.85F, -5.5F, 11F, 16.85F, 11F),
                PartPose.offsetAndRotation(12.5F, 4F, -15F, 0.1396F, 0F, 0F));
        PartDefinition p_front_shin_l = p_front_leg_l.addOrReplaceChild("front_shin_l", CubeListBuilder.create()
                .texOffs(145, 191).addBox(-4.25F, -1F, -4.25F, 8.5F, 12.5F, 8.5F),
                PartPose.offsetAndRotation(0F, 12.5F, 0F, -0.2443F, 0F, 0F));
        PartDefinition p_front_foot_l = p_front_shin_l.addOrReplaceChild("front_foot_l", CubeListBuilder.create()
                .texOffs(246, 236).addBox(-5.5F, 0F, -8.4F, 11F, 3.5F, 12F)
                .texOffs(385, 310).addBox(-4.875F, 1.225F, -12.9F, 1.5F, 2.275F, 4.5F)
                .texOffs(400, 310).addBox(-2.125F, 1.225F, -12.9F, 1.5F, 2.275F, 4.5F)
                .texOffs(415, 310).addBox(0.625F, 1.225F, -12.9F, 1.5F, 2.275F, 4.5F)
                .texOffs(430, 310).addBox(3.375F, 1.225F, -12.9F, 1.5F, 2.275F, 4.5F),
                PartPose.offsetAndRotation(0F, 11F, 0F, 0.1047F, 0F, 0F));
        PartDefinition p_hind_leg_l = p_body.addOrReplaceChild("hind_leg_l", CubeListBuilder.create()
                .texOffs(143, 69).addBox(-6.5F, -4.55F, -7F, 13F, 19.55F, 14F),
                PartPose.offsetAndRotation(12.5F, 2F, 14F, -0.2094F, 0F, 0F));
        PartDefinition p_hind_shin_l = p_hind_leg_l.addOrReplaceChild("hind_shin_l", CubeListBuilder.create()
                .texOffs(241, 166).addBox(-4.75F, -1F, -4.75F, 9.5F, 13F, 9.5F),
                PartPose.offsetAndRotation(0F, 14.5F, 0F, 0.5934F, 0F, 0F));
        PartDefinition p_hind_foot_l = p_hind_shin_l.addOrReplaceChild("hind_foot_l", CubeListBuilder.create()
                .texOffs(94, 236).addBox(-5.75F, 0F, -9.1F, 11.5F, 3.5F, 13F)
                .texOffs(445, 310).addBox(-5.0625F, 1.225F, -14.1F, 1.5F, 2.275F, 5F)
                .texOffs(460, 310).addBox(-2.1875F, 1.225F, -14.1F, 1.5F, 2.275F, 5F)
                .texOffs(475, 310).addBox(0.6875F, 1.225F, -14.1F, 1.5F, 2.275F, 5F)
                .texOffs(490, 310).addBox(3.5625F, 1.225F, -14.1F, 1.5F, 2.275F, 5F),
                PartPose.offsetAndRotation(0F, 11.5F, 0F, -0.384F, 0F, 0F));
        PartDefinition p_front_leg_r = p_body.addOrReplaceChild("front_leg_r", CubeListBuilder.create()
                .texOffs(0, 137).addBox(-5.5F, -3.85F, -5.5F, 11F, 16.85F, 11F),
                PartPose.offsetAndRotation(-12.5F, 4F, -15F, 0.1396F, 0F, 0F));
        PartDefinition p_front_shin_r = p_front_leg_r.addOrReplaceChild("front_shin_r", CubeListBuilder.create()
                .texOffs(182, 191).addBox(-4.25F, -1F, -4.25F, 8.5F, 12.5F, 8.5F),
                PartPose.offsetAndRotation(0F, 12.5F, 0F, -0.2443F, 0F, 0F));
        PartDefinition p_front_foot_r = p_front_shin_r.addOrReplaceChild("front_foot_r", CubeListBuilder.create()
                .texOffs(293, 236).addBox(-5.5F, 0F, -8.4F, 11F, 3.5F, 12F)
                .texOffs(0, 320).addBox(-4.875F, 1.225F, -12.9F, 1.5F, 2.275F, 4.5F)
                .texOffs(15, 320).addBox(-2.125F, 1.225F, -12.9F, 1.5F, 2.275F, 4.5F)
                .texOffs(30, 320).addBox(0.625F, 1.225F, -12.9F, 1.5F, 2.275F, 4.5F)
                .texOffs(45, 320).addBox(3.375F, 1.225F, -12.9F, 1.5F, 2.275F, 4.5F),
                PartPose.offsetAndRotation(0F, 11F, 0F, 0.1047F, 0F, 0F));
        PartDefinition p_hind_leg_r = p_body.addOrReplaceChild("hind_leg_r", CubeListBuilder.create()
                .texOffs(198, 69).addBox(-6.5F, -4.55F, -7F, 13F, 19.55F, 14F),
                PartPose.offsetAndRotation(-12.5F, 2F, 14F, -0.2094F, 0F, 0F));
        PartDefinition p_hind_shin_r = p_hind_leg_r.addOrReplaceChild("hind_shin_r", CubeListBuilder.create()
                .texOffs(282, 166).addBox(-4.75F, -1F, -4.75F, 9.5F, 13F, 9.5F),
                PartPose.offsetAndRotation(0F, 14.5F, 0F, 0.5934F, 0F, 0F));
        PartDefinition p_hind_foot_r = p_hind_shin_r.addOrReplaceChild("hind_foot_r", CubeListBuilder.create()
                .texOffs(145, 236).addBox(-5.75F, 0F, -9.1F, 11.5F, 3.5F, 13F)
                .texOffs(60, 320).addBox(-5.0625F, 1.225F, -14.1F, 1.5F, 2.275F, 5F)
                .texOffs(75, 320).addBox(-2.1875F, 1.225F, -14.1F, 1.5F, 2.275F, 5F)
                .texOffs(90, 320).addBox(0.6875F, 1.225F, -14.1F, 1.5F, 2.275F, 5F)
                .texOffs(105, 320).addBox(3.5625F, 1.225F, -14.1F, 1.5F, 2.275F, 5F),
                PartPose.offsetAndRotation(0F, 11.5F, 0F, -0.384F, 0F, 0F));
        PartDefinition p_wing_l = p_body.addOrReplaceChild("wing_l", CubeListBuilder.create()
                .texOffs(405, 286).addBox(0F, -2.5F, -2.5F, 22F, 5F, 5F)
                .texOffs(105, 105).addBox(0F, 0F, 2F, 22F, 0F, 30F),
                PartPose.offset(13F, -11F, -12F));
        PartDefinition p_wing_l_fore = p_wing_l.addOrReplaceChild("wing_l_fore", CubeListBuilder.create()
                .texOffs(147, 310).addBox(0F, -1.875F, -1.875F, 26F, 3.75F, 3.75F)
                .texOffs(253, 69).addBox(0F, 0F, 1.375F, 26F, 0F, 32F),
                PartPose.offset(22F, 0F, 0F));
        PartDefinition p_wing_l_hand = p_wing_l_fore.addOrReplaceChild("wing_l_hand", CubeListBuilder.create()
                .texOffs(282, 299).addBox(-2.5F, -2.5F, -2.5F, 5F, 5F, 5F)
                .texOffs(394, 320).addBox(-1F, -1F, -6F, 2F, 2F, 4F),
                PartPose.offset(26F, 0F, 0F));
        PartDefinition p_wing_l_f1 = p_wing_l_hand.addOrReplaceChild("wing_l_f1", CubeListBuilder.create()
                .texOffs(0, 329).addBox(0F, -0.925F, -0.925F, 46F, 1.85F, 1.85F)
                .texOffs(372, 137).addBox(0F, 0F, 0.525F, 46F, 0F, 24F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, -0.1745F, 0F));
        PartDefinition p_wing_l_f2 = p_wing_l_hand.addOrReplaceChild("wing_l_f2", CubeListBuilder.create()
                .texOffs(194, 329).addBox(0F, -0.75F, -0.75F, 43F, 1.5F, 1.5F)
                .texOffs(219, 191).addBox(0F, 0F, 0.35F, 43F, 0F, 21F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, -0.733F, 0F));
        PartDefinition p_wing_l_f3 = p_wing_l_hand.addOrReplaceChild("wing_l_f3", CubeListBuilder.create()
                .texOffs(376, 329).addBox(0F, -0.575F, -0.575F, 36F, 1.15F, 1.15F)
                .texOffs(278, 0).addBox(0F, 0F, 0.175F, 36F, 0F, 35F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, -1.2915F, 0F));
        PartDefinition p_wing_r = p_body.addOrReplaceChild("wing_r", CubeListBuilder.create()
                .texOffs(0, 299).addBox(-22F, -2.5F, -2.5F, 22F, 5F, 5F)
                .texOffs(210, 105).addBox(-22F, 0F, 2F, 22F, 0F, 30F),
                PartPose.offset(-13F, -11F, -12F));
        PartDefinition p_wing_r_fore = p_wing_r.addOrReplaceChild("wing_r_fore", CubeListBuilder.create()
                .texOffs(208, 310).addBox(-26F, -1.875F, -1.875F, 26F, 3.75F, 3.75F)
                .texOffs(370, 69).addBox(-26F, 0F, 1.375F, 26F, 0F, 32F),
                PartPose.offset(-22F, 0F, 0F));
        PartDefinition p_wing_r_hand = p_wing_r_fore.addOrReplaceChild("wing_r_hand", CubeListBuilder.create()
                .texOffs(303, 299).addBox(-2.5F, -2.5F, -2.5F, 5F, 5F, 5F)
                .texOffs(407, 320).addBox(-1F, -1F, -6F, 2F, 2F, 4F),
                PartPose.offset(-26F, 0F, 0F));
        PartDefinition p_wing_r_f1 = p_wing_r_hand.addOrReplaceChild("wing_r_f1", CubeListBuilder.create()
                .texOffs(97, 329).addBox(-46F, -0.925F, -0.925F, 46F, 1.85F, 1.85F)
                .texOffs(0, 166).addBox(-46F, 0F, 0.525F, 46F, 0F, 24F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0.1745F, 0F));
        PartDefinition p_wing_r_f2 = p_wing_r_hand.addOrReplaceChild("wing_r_f2", CubeListBuilder.create()
                .texOffs(285, 329).addBox(-43F, -0.75F, -0.75F, 43F, 1.5F, 1.5F)
                .texOffs(348, 191).addBox(-43F, 0F, 0.35F, 43F, 0F, 21F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0.733F, 0F));
        PartDefinition p_wing_r_f3 = p_wing_r_hand.addOrReplaceChild("wing_r_f3", CubeListBuilder.create()
                .texOffs(0, 334).addBox(-36F, -0.575F, -0.575F, 36F, 1.15F, 1.15F)
                .texOffs(0, 69).addBox(-36F, 0F, 0.175F, 36F, 0F, 35F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 1.2915F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
