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
    public static final int TEXTURE_HEIGHT = 448;

    private VoltarionGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-10F, -10F, -18F, 20F, 19F, 34F)
                .texOffs(125, 89).addBox(-8F, -13F, -15F, 16F, 3.5F, 27F)
                .texOffs(0, 149).addBox(-8.5F, 8F, -17F, 17F, 4F, 20F)
                .texOffs(157, 174).addBox(-7.5F, 8F, 3F, 15F, 3F, 12F)
                .texOffs(124, 149).addBox(-11.5F, -6F, 4F, 2F, 12F, 11F)
                .texOffs(151, 149).addBox(9.5F, -6F, 4F, 2F, 12F, 11F)
                .texOffs(37, 174).addBox(-12F, -9F, -16F, 3F, 9F, 9F)
                .texOffs(62, 174).addBox(9F, -9F, -16F, 3F, 9F, 9F)
                .texOffs(346, 193).addBox(-0.4F, -19F, -13F, 0.8F, 6.5F, 4.5F)
                .texOffs(273, 193).addBox(-0.4F, -20.5F, -7F, 0.8F, 8F, 4.5F)
                .texOffs(142, 193).addBox(-0.4F, -21F, -1F, 0.8F, 8.5F, 4.5F)
                .texOffs(359, 193).addBox(-0.4F, -19.5F, 5F, 0.8F, 7F, 4.5F)
                .texOffs(478, 193).addBox(-0.4F, -18F, 11F, 0.8F, 5.5F, 4.5F),
                PartPose.offset(0F, -10F, 0F));
        PartDefinition p_neck1 = p_body.addOrReplaceChild("neck1", CubeListBuilder.create()
                .texOffs(314, 149).addBox(-5.5F, -5.5F, -7.5F, 11F, 11F, 8F)
                .texOffs(238, 219).addBox(-4.5F, 5F, -6.5F, 9F, 1.5F, 6F)
                .texOffs(418, 193).addBox(-0.35F, -10F, -6.5F, 0.7F, 5F, 6F),
                PartPose.offsetAndRotation(0F, -6F, -17F, -0.5236F, 0F, 0F));
        PartDefinition p_neck2 = p_neck1.addOrReplaceChild("neck2", CubeListBuilder.create()
                .texOffs(439, 149).addBox(-5F, -5F, -7.3F, 10F, 10F, 7.8F)
                .texOffs(269, 219).addBox(-4F, 4.5F, -6.3F, 8F, 1.5F, 5.8F)
                .texOffs(433, 193).addBox(-0.35F, -9.05F, -6.3F, 0.7F, 4.55F, 5.8F),
                PartPose.offsetAndRotation(0F, 0F, -7F, -0.2443F, 0F, 0F));
        PartDefinition p_neck3 = p_neck2.addOrReplaceChild("neck3", CubeListBuilder.create()
                .texOffs(476, 149).addBox(-4.6F, -4.6F, -7F, 9.2F, 9.2F, 7.5F)
                .texOffs(298, 219).addBox(-3.6F, 4.1F, -6F, 7.2F, 1.5F, 5.5F)
                .texOffs(448, 193).addBox(-0.35F, -8.2F, -6F, 0.7F, 4.1F, 5.5F),
                PartPose.offsetAndRotation(0F, 0F, -6.8F, -0.0698F, 0F, 0F));
        PartDefinition p_neck4 = p_neck3.addOrReplaceChild("neck4", CubeListBuilder.create()
                .texOffs(87, 174).addBox(-4.3F, -4.3F, -6.7F, 8.6F, 8.6F, 7.2F)
                .texOffs(327, 219).addBox(-3.3F, 3.8F, -5.7F, 6.6F, 1.5F, 5.2F)
                .texOffs(388, 208).addBox(-0.35F, -7.45F, -5.7F, 0.7F, 3.65F, 5.2F),
                PartPose.offsetAndRotation(0F, 0F, -6.5F, 0.0698F, 0F, 0F));
        PartDefinition p_neck5 = p_neck4.addOrReplaceChild("neck5", CubeListBuilder.create()
                .texOffs(370, 174).addBox(-4F, -4F, -6.5F, 8F, 8F, 7F)
                .texOffs(26, 229).addBox(-3F, 3.5F, -5.5F, 6F, 1.5F, 5F)
                .texOffs(63, 219).addBox(-0.35F, -6.7F, -5.5F, 0.7F, 3.2F, 5F),
                PartPose.offsetAndRotation(0F, 0F, -6.2F, 0.1745F, 0F, 0F));
        PartDefinition p_neck6 = p_neck5.addOrReplaceChild("neck6", CubeListBuilder.create()
                .texOffs(401, 174).addBox(-3.7F, -3.7F, -6.3F, 7.4F, 7.4F, 6.8F)
                .texOffs(49, 229).addBox(-2.7F, 3.2F, -5.3F, 5.4F, 1.5F, 4.8F)
                .texOffs(469, 219).addBox(-0.35F, -5.95F, -5.3F, 0.7F, 2.75F, 4.8F),
                PartPose.offsetAndRotation(0F, 0F, -6F, 0.1745F, 0F, 0F));
        PartDefinition p_head = p_neck6.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(75, 149).addBox(-5.5F, -5F, -13F, 11F, 10F, 13F)
                .texOffs(228, 149).addBox(-4F, -3.2F, -26F, 8F, 6.5F, 13.5F)
                .texOffs(155, 193).addBox(-2.8F, -4.5F, -24.5F, 5.6F, 1.5F, 11F)
                .texOffs(432, 238).addBox(-3.2F, -3.5F, -26.3F, 2F, 1.2F, 1.2F)
                .texOffs(441, 238).addBox(1.2F, -3.5F, -26.3F, 2F, 1.2F, 1.2F)
                .texOffs(212, 174).addBox(-3.6F, 3.3F, -25.5F, 7.2F, 1.3F, 12.5F)
                .texOffs(0, 193).addBox(-3.6F, 3.2F, -25.4F, 7.2F, 0.2F, 13F)
                .texOffs(214, 229).addBox(5.1F, -2.6F, -12.5F, 1F, 1.8F, 3.6F)
                .texOffs(225, 229).addBox(-6.1F, -2.6F, -12.5F, 1F, 1.8F, 3.6F)
                .texOffs(354, 219).addBox(5F, -5.4F, -12.5F, 1.6F, 1.4F, 6F)
                .texOffs(371, 219).addBox(-6.6F, -5.4F, -12.5F, 1.6F, 1.4F, 6F),
                PartPose.offsetAndRotation(0F, 0F, -5.8F, 0.5236F, 0F, 0F));
        PartDefinition p_horn_l0a = p_head.addOrReplaceChild("horn_l0a", CubeListBuilder.create()
                .texOffs(219, 193).addBox(-1.3F, -1.3F, 0F, 2.6F, 2.6F, 10F),
                PartPose.offsetAndRotation(2.8F, -5F, -6F, -0.4887F, 0.2094F, 0F));
        PartDefinition p_horn_l0b = p_horn_l0a.addOrReplaceChild("horn_l0b", CubeListBuilder.create()
                .texOffs(262, 208).addBox(-0.91F, -0.91F, 0F, 1.82F, 1.82F, 8F),
                PartPose.offsetAndRotation(0F, 0F, 10F, -0.3142F, 0.0698F, 0F));
        PartDefinition p_horn_l1a = p_head.addOrReplaceChild("horn_l1a", CubeListBuilder.create()
                .texOffs(283, 208).addBox(-1F, -1F, 0F, 2F, 2F, 8F),
                PartPose.offsetAndRotation(4.6F, -3.5F, -4F, -0.1745F, 0.5236F, 0F));
        PartDefinition p_horn_l1b = p_horn_l1a.addOrReplaceChild("horn_l1b", CubeListBuilder.create()
                .texOffs(388, 219).addBox(-0.7F, -0.7F, 0F, 1.4F, 1.4F, 6F),
                PartPose.offsetAndRotation(0F, 0F, 8F, -0.3142F, 0.0698F, 0F));
        PartDefinition p_horn_r0a = p_head.addOrReplaceChild("horn_r0a", CubeListBuilder.create()
                .texOffs(246, 193).addBox(-1.3F, -1.3F, 0F, 2.6F, 2.6F, 10F),
                PartPose.offsetAndRotation(-2.8F, -5F, -6F, -0.4887F, -0.2094F, 0F));
        PartDefinition p_horn_r0b = p_horn_r0a.addOrReplaceChild("horn_r0b", CubeListBuilder.create()
                .texOffs(304, 208).addBox(-0.91F, -0.91F, 0F, 1.82F, 1.82F, 8F),
                PartPose.offsetAndRotation(0F, 0F, 10F, -0.3142F, -0.0698F, 0F));
        PartDefinition p_horn_r1a = p_head.addOrReplaceChild("horn_r1a", CubeListBuilder.create()
                .texOffs(325, 208).addBox(-1F, -1F, 0F, 2F, 2F, 8F),
                PartPose.offsetAndRotation(-4.6F, -3.5F, -4F, -0.1745F, -0.5236F, 0F));
        PartDefinition p_horn_r1b = p_horn_r1a.addOrReplaceChild("horn_r1b", CubeListBuilder.create()
                .texOffs(405, 219).addBox(-0.7F, -0.7F, 0F, 1.4F, 1.4F, 6F),
                PartPose.offsetAndRotation(0F, 0F, 8F, -0.3142F, -0.0698F, 0F));
        PartDefinition p_jaw = p_head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(353, 149).addBox(-3.8F, 0F, -14.5F, 7.6F, 3F, 15F)
                .texOffs(286, 193).addBox(-3.2F, -1.1F, -14.2F, 6.4F, 1.1F, 10F)
                .texOffs(43, 193).addBox(-2.8F, -0.2F, -13.5F, 5.6F, 0.4F, 12.5F)
                .texOffs(482, 219).addBox(-0.5F, 2.5F, -10F, 1F, 3F, 5F),
                PartPose.offsetAndRotation(0F, 3.3F, -11F, 0.0524F, 0F, 0F));
        PartDefinition p_tail1 = p_body.addOrReplaceChild("tail1", CubeListBuilder.create()
                .texOffs(273, 149).addBox(-5F, -4.5F, -0.5F, 10F, 9F, 10F)
                .texOffs(142, 208).addBox(-4F, 4F, 0.5F, 8F, 1.5F, 8F)
                .texOffs(463, 193).addBox(-0.3F, -8.1F, 1.5F, 0.6F, 4.1F, 6F),
                PartPose.offsetAndRotation(0F, -3F, 15F, -0.1047F, 0F, 0F));
        PartDefinition p_tail2 = p_tail1.addOrReplaceChild("tail2", CubeListBuilder.create()
                .texOffs(400, 149).addBox(-4.5F, -4F, -0.5F, 9F, 8F, 10F)
                .texOffs(175, 208).addBox(-3.5F, 3.5F, 0.5F, 7F, 1.5F, 8F)
                .texOffs(403, 208).addBox(-0.3F, -7.25F, 1.5F, 0.6F, 3.75F, 6F),
                PartPose.offsetAndRotation(0F, 0F, 9F, -0.0698F, 0F, 0F));
        PartDefinition p_tail3 = p_tail2.addOrReplaceChild("tail3", CubeListBuilder.create()
                .texOffs(0, 174).addBox(-4F, -3.6F, -0.5F, 8F, 7.2F, 9.5F)
                .texOffs(206, 208).addBox(-3F, 3.1F, 0.5F, 6F, 1.5F, 7.5F)
                .texOffs(418, 208).addBox(-0.3F, -6.5F, 1.5F, 0.6F, 3.4F, 5.5F),
                PartPose.offsetAndRotation(0F, 0F, 9F, -0.0349F, 0F, 0F));
        PartDefinition p_tail4 = p_tail3.addOrReplaceChild("tail4", CubeListBuilder.create()
                .texOffs(122, 174).addBox(-3.5F, -3.2F, -0.5F, 7F, 6.4F, 9.5F)
                .texOffs(235, 208).addBox(-2.5F, 2.7F, 0.5F, 5F, 1.5F, 7.5F)
                .texOffs(433, 208).addBox(-0.3F, -5.75F, 1.5F, 0.6F, 3.05F, 5.5F),
                PartPose.offset(0F, 0F, 8.5F));
        PartDefinition p_tail5 = p_tail4.addOrReplaceChild("tail5", CubeListBuilder.create()
                .texOffs(337, 174).addBox(-3.1F, -2.8F, -0.5F, 6.2F, 5.6F, 9F)
                .texOffs(448, 208).addBox(-2.1F, 2.3F, 0.5F, 4.2F, 1.5F, 7F)
                .texOffs(495, 219).addBox(-0.3F, -5F, 1.5F, 0.6F, 2.7F, 5F),
                PartPose.offsetAndRotation(0F, 0F, 8.5F, 0.0175F, 0F, 0F));
        PartDefinition p_tail6 = p_tail5.addOrReplaceChild("tail6", CubeListBuilder.create()
                .texOffs(82, 193).addBox(-2.7F, -2.5F, -0.5F, 5.4F, 5F, 9F)
                .texOffs(473, 208).addBox(-1.7F, 2F, 0.5F, 3.4F, 1.5F, 7F)
                .texOffs(0, 229).addBox(-0.3F, -4.35F, 1.5F, 0.6F, 2.35F, 5F),
                PartPose.offsetAndRotation(0F, 0F, 8F, 0.0349F, 0F, 0F));
        PartDefinition p_tail7 = p_tail6.addOrReplaceChild("tail7", CubeListBuilder.create()
                .texOffs(113, 193).addBox(-2.4F, -2.2F, -0.5F, 4.8F, 4.4F, 8.5F)
                .texOffs(0, 219).addBox(-1.4F, 1.7F, 0.5F, 2.8F, 1.5F, 6.5F)
                .texOffs(13, 229).addBox(-0.3F, -3.7F, 1.5F, 0.6F, 2F, 4.5F),
                PartPose.offsetAndRotation(0F, 0F, 8F, 0.0349F, 0F, 0F));
        PartDefinition p_tail8 = p_tail7.addOrReplaceChild("tail8", CubeListBuilder.create()
                .texOffs(190, 193).addBox(-2.1F, -1.9F, -0.5F, 4.2F, 3.8F, 8.5F)
                .texOffs(21, 219).addBox(-1.1F, 1.4F, 0.5F, 2.2F, 1.5F, 6.5F)
                .texOffs(162, 229).addBox(-0.3F, -3.05F, 1.5F, 0.6F, 1.65F, 4.5F),
                PartPose.offsetAndRotation(0F, 0F, 7.5F, 0.0349F, 0F, 0F));
        PartDefinition p_tail9 = p_tail8.addOrReplaceChild("tail9", CubeListBuilder.create()
                .texOffs(321, 193).addBox(-1.8F, -1.6F, -0.5F, 3.6F, 3.2F, 8F)
                .texOffs(422, 219).addBox(-0.8F, 1.1F, 0.5F, 1.6F, 1.5F, 6F)
                .texOffs(236, 229).addBox(-0.3F, -2.4F, 1.5F, 0.6F, 1.3F, 4F),
                PartPose.offsetAndRotation(0F, 0F, 7.5F, 0.0349F, 0F, 0F));
        PartDefinition p_tail10 = p_tail9.addOrReplaceChild("tail10", CubeListBuilder.create()
                .texOffs(372, 193).addBox(-1.5F, -1.4F, -0.5F, 3F, 2.8F, 8F)
                .texOffs(439, 219).addBox(-0.5F, 0.9F, 0.5F, 1F, 1.5F, 6F),
                PartPose.offsetAndRotation(0F, 0F, 7F, 0.0349F, 0F, 0F));
        PartDefinition p_tail11 = p_tail10.addOrReplaceChild("tail11", CubeListBuilder.create()
                .texOffs(395, 193).addBox(-1.3F, -1.2F, -0.5F, 2.6F, 2.4F, 7.5F)
                .texOffs(454, 219).addBox(-0.3F, 0.7F, 0.5F, 0.6F, 1.5F, 5.5F),
                PartPose.offsetAndRotation(0F, 0F, 7F, 0.0175F, 0F, 0F));
        PartDefinition p_tail12 = p_tail11.addOrReplaceChild("tail12", CubeListBuilder.create()
                .texOffs(42, 219).addBox(-1.1F, -1F, -0.5F, 2.2F, 2F, 7F)
                .texOffs(175, 229).addBox(-0.1F, 0.5F, 0.5F, 0.2F, 1.5F, 5F)
                .texOffs(432, 174).addBox(-5.5F, -0.5F, 4.5F, 11F, 1F, 13F),
                PartPose.offsetAndRotation(0F, 0F, 6.5F, 0.0175F, 0F, 0F));
        PartDefinition p_hind_leg_l = p_body.addOrReplaceChild("hind_leg_l", CubeListBuilder.create()
                .texOffs(212, 89).addBox(-4.5F, -3.15F, -5.5F, 9F, 19.15F, 11F),
                PartPose.offsetAndRotation(9.5F, 3F, 8F, -0.3142F, 0F, 0F));
        PartDefinition p_hind_shin_l = p_hind_leg_l.addOrReplaceChild("hind_shin_l", CubeListBuilder.create()
                .texOffs(178, 149).addBox(-3F, -1F, -3F, 6F, 16F, 6F),
                PartPose.offsetAndRotation(0F, 15.5F, 0F, 0.733F, 0F, 0F));
        PartDefinition p_hind_foot_l = p_hind_shin_l.addOrReplaceChild("hind_foot_l", CubeListBuilder.create()
                .texOffs(255, 174).addBox(-4F, 0F, -8.4F, 8F, 3F, 12F)
                .texOffs(72, 229).addBox(-3.4167F, 1.05F, -12.6F, 1.5F, 1.95F, 4.2F)
                .texOffs(87, 229).addBox(-0.75F, 1.05F, -12.6F, 1.5F, 1.95F, 4.2F)
                .texOffs(102, 229).addBox(1.9167F, 1.05F, -12.6F, 1.5F, 1.95F, 4.2F),
                PartPose.offsetAndRotation(0F, 14.5F, 0F, -0.4189F, 0F, 0F));
        PartDefinition p_hind_leg_r = p_body.addOrReplaceChild("hind_leg_r", CubeListBuilder.create()
                .texOffs(253, 89).addBox(-4.5F, -3.15F, -5.5F, 9F, 19.15F, 11F),
                PartPose.offsetAndRotation(-9.5F, 3F, 8F, -0.3142F, 0F, 0F));
        PartDefinition p_hind_shin_r = p_hind_leg_r.addOrReplaceChild("hind_shin_r", CubeListBuilder.create()
                .texOffs(203, 149).addBox(-3F, -1F, -3F, 6F, 16F, 6F),
                PartPose.offsetAndRotation(0F, 15.5F, 0F, 0.733F, 0F, 0F));
        PartDefinition p_hind_foot_r = p_hind_shin_r.addOrReplaceChild("hind_foot_r", CubeListBuilder.create()
                .texOffs(296, 174).addBox(-4F, 0F, -8.4F, 8F, 3F, 12F)
                .texOffs(117, 229).addBox(-3.4167F, 1.05F, -12.6F, 1.5F, 1.95F, 4.2F)
                .texOffs(132, 229).addBox(-0.75F, 1.05F, -12.6F, 1.5F, 1.95F, 4.2F)
                .texOffs(147, 229).addBox(1.9167F, 1.05F, -12.6F, 1.5F, 1.95F, 4.2F),
                PartPose.offsetAndRotation(0F, 14.5F, 0F, -0.4189F, 0F, 0F));
        PartDefinition p_wing_l = p_body.addOrReplaceChild("wing_l", CubeListBuilder.create()
                .texOffs(0, 208).addBox(0F, -2.3F, -2.3F, 30F, 4.6F, 4.6F),
                PartPose.offset(10.5F, -7F, -12F));
        PartDefinition p_wing_l_web = p_wing_l.addOrReplaceChild("wing_l_web", CubeListBuilder.create()
                .texOffs(282, 54).addBox(0F, 0F, 1.8F, 30F, 0F, 32F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_wing_l_fore = p_wing_l.addOrReplaceChild("wing_l_fore", CubeListBuilder.create()
                .texOffs(76, 219).addBox(0F, -1.725F, -1.725F, 36F, 3.45F, 3.45F),
                PartPose.offset(30F, 0F, 0F));
        PartDefinition p_wing_l_fore_web = p_wing_l_fore.addOrReplaceChild("wing_l_fore_web", CubeListBuilder.create()
                .texOffs(0, 54).addBox(0F, 0F, 1.225F, 36F, 0F, 34F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_wing_l_hand = p_wing_l_fore.addOrReplaceChild("wing_l_hand", CubeListBuilder.create()
                .texOffs(346, 208).addBox(-2.5F, -2.5F, -2.5F, 5F, 5F, 5F)
                .texOffs(188, 229).addBox(-1F, -1F, -6F, 2F, 2F, 4F),
                PartPose.offset(36F, 0F, 0F));
        PartDefinition p_wing_l_f1 = p_wing_l_hand.addOrReplaceChild("wing_l_f1", CubeListBuilder.create()
                .texOffs(247, 229).addBox(0F, -0.925F, -0.925F, 62F, 1.85F, 1.85F)
                .texOffs(294, 89).addBox(0F, 0F, 0.525F, 62F, 0F, 26F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, -0.1396F, 0F));
        PartDefinition p_wing_l_f2 = p_wing_l_hand.addOrReplaceChild("wing_l_f2", CubeListBuilder.create()
                .texOffs(0, 238).addBox(0F, -0.75F, -0.75F, 56F, 1.5F, 1.5F)
                .texOffs(177, 122).addBox(0F, 0F, 0.35F, 56F, 0F, 24F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, -0.5934F, 0F));
        PartDefinition p_wing_l_f3 = p_wing_l_hand.addOrReplaceChild("wing_l_f3", CubeListBuilder.create()
                .texOffs(234, 238).addBox(0F, -0.575F, -0.575F, 47F, 1.15F, 1.15F)
                .texOffs(109, 0).addBox(0F, 0F, 0.175F, 47F, 0F, 37F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, -1.0821F, 0F));
        PartDefinition p_wing_r = p_body.addOrReplaceChild("wing_r", CubeListBuilder.create()
                .texOffs(71, 208).addBox(-30F, -2.3F, -2.3F, 30F, 4.6F, 4.6F),
                PartPose.offset(-10.5F, -7F, -12F));
        PartDefinition p_wing_r_web = p_wing_r.addOrReplaceChild("wing_r_web", CubeListBuilder.create()
                .texOffs(0, 89).addBox(-30F, 0F, 1.8F, 30F, 0F, 32F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_wing_r_fore = p_wing_r.addOrReplaceChild("wing_r_fore", CubeListBuilder.create()
                .texOffs(157, 219).addBox(-36F, -1.725F, -1.725F, 36F, 3.45F, 3.45F),
                PartPose.offset(-30F, 0F, 0F));
        PartDefinition p_wing_r_fore_web = p_wing_r_fore.addOrReplaceChild("wing_r_fore_web", CubeListBuilder.create()
                .texOffs(141, 54).addBox(-36F, 0F, 1.225F, 36F, 0F, 34F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_wing_r_hand = p_wing_r_fore.addOrReplaceChild("wing_r_hand", CubeListBuilder.create()
                .texOffs(367, 208).addBox(-2.5F, -2.5F, -2.5F, 5F, 5F, 5F)
                .texOffs(201, 229).addBox(-1F, -1F, -6F, 2F, 2F, 4F),
                PartPose.offset(-36F, 0F, 0F));
        PartDefinition p_wing_r_f1 = p_wing_r_hand.addOrReplaceChild("wing_r_f1", CubeListBuilder.create()
                .texOffs(376, 229).addBox(-62F, -0.925F, -0.925F, 62F, 1.85F, 1.85F)
                .texOffs(0, 122).addBox(-62F, 0F, 0.525F, 62F, 0F, 26F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0.1396F, 0F));
        PartDefinition p_wing_r_f2 = p_wing_r_hand.addOrReplaceChild("wing_r_f2", CubeListBuilder.create()
                .texOffs(117, 238).addBox(-56F, -0.75F, -0.75F, 56F, 1.5F, 1.5F)
                .texOffs(338, 122).addBox(-56F, 0F, 0.35F, 56F, 0F, 24F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0.5934F, 0F));
        PartDefinition p_wing_r_f3 = p_wing_r_hand.addOrReplaceChild("wing_r_f3", CubeListBuilder.create()
                .texOffs(333, 238).addBox(-47F, -0.575F, -0.575F, 47F, 1.15F, 1.15F)
                .texOffs(278, 0).addBox(-47F, 0F, 0.175F, 47F, 0F, 37F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 1.0821F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
