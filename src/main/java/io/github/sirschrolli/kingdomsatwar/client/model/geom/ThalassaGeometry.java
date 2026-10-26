package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie von Thalassa, dem Wasserdrachen-Boss (Grundgröße, im Spiel ×2,0).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class ThalassaGeometry {
    public static final int TEXTURE_WIDTH = 512;
    public static final int TEXTURE_HEIGHT = 256;

    private ThalassaGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(106, 0).addBox(-8F, -3.4F, -14F, 16F, 6.8F, 27F)
                .texOffs(0, 0).addBox(-4.4F, -7F, -13.92F, 8.8F, 14F, 26.84F)
                .texOffs(367, 0).addBox(-6F, -9F, -12F, 12F, 3F, 22F)
                .texOffs(193, 0).addBox(-6.5F, 6F, -13F, 13F, 2.2F, 25F)
                .texOffs(270, 0).addBox(-0.4F, -15F, -11F, 0.8F, 6.5F, 20F),
                PartPose.offset(0F, 14F, 0F));
        PartDefinition p_front_leg_l = p_body.addOrReplaceChild("front_leg_l", CubeListBuilder.create()
                .texOffs(177, 85).addBox(-2.6F, -1F, -2.2F, 5.2F, 9.5F, 4.4F)
                .texOffs(449, 42).addBox(-1.2F, -0.92F, -3.6F, 2.4F, 9.34F, 7.2F),
                PartPose.offsetAndRotation(7.5F, 3.5F, -7.5F, 0.4363F, 0F, -1.0123F));
        PartDefinition p_front_shin_l = p_front_leg_l.addOrReplaceChild("front_shin_l", CubeListBuilder.create()
                .texOffs(371, 102).addBox(-2.1F, -0.5F, -1.9F, 4.2F, 8.5F, 3.8F)
                .texOffs(223, 85).addBox(-1F, -0.42F, -3F, 2F, 8.34F, 6F),
                PartPose.offsetAndRotation(0F, 8.5F, 0F, 0.4887F, 0F, 0F));
        PartDefinition p_front_foot_l = p_front_shin_l.addOrReplaceChild("front_foot_l", CubeListBuilder.create()
                .texOffs(313, 0).addBox(-0.7F, -0.5F, -5.5F, 1.4F, 15F, 11F),
                PartPose.offsetAndRotation(0F, 7.8F, 0F, 0.384F, 0F, 0F));
        PartDefinition p_front_leg_r = p_body.addOrReplaceChild("front_leg_r", CubeListBuilder.create()
                .texOffs(200, 85).addBox(-2.6F, -1F, -2.2F, 5.2F, 9.5F, 4.4F)
                .texOffs(472, 42).addBox(-1.2F, -0.92F, -3.6F, 2.4F, 9.34F, 7.2F),
                PartPose.offsetAndRotation(-7.5F, 3.5F, -7.5F, 0.4363F, 0F, 1.0123F));
        PartDefinition p_front_shin_r = p_front_leg_r.addOrReplaceChild("front_shin_r", CubeListBuilder.create()
                .texOffs(390, 102).addBox(-2.1F, -0.5F, -1.9F, 4.2F, 8.5F, 3.8F)
                .texOffs(240, 85).addBox(-1F, -0.42F, -3F, 2F, 8.34F, 6F),
                PartPose.offsetAndRotation(0F, 8.5F, 0F, 0.4887F, 0F, 0F));
        PartDefinition p_front_foot_r = p_front_shin_r.addOrReplaceChild("front_foot_r", CubeListBuilder.create()
                .texOffs(340, 0).addBox(-0.7F, -0.5F, -5.5F, 1.4F, 15F, 11F),
                PartPose.offsetAndRotation(0F, 7.8F, 0F, 0.384F, 0F, 0F));
        PartDefinition p_neck1 = p_body.addOrReplaceChild("neck1", CubeListBuilder.create()
                .texOffs(349, 85).addBox(-6F, -2.16F, -8.1F, 12F, 4.32F, 8.7F)
                .texOffs(179, 42).addBox(-2.16F, -6F, -8.02F, 4.32F, 12F, 8.54F)
                .texOffs(277, 130).addBox(-2.16F, 5F, -7.5F, 4.32F, 1.6F, 7.5F)
                .texOffs(342, 117).addBox(-0.35F, -9.6F, -7F, 0.7F, 4.5F, 6.5F),
                PartPose.offsetAndRotation(0F, -2F, -13.5F, -0.4887F, 0F, 0F));
        PartDefinition p_neck2 = p_neck1.addOrReplaceChild("neck2", CubeListBuilder.create()
                .texOffs(146, 102).addBox(-5.3F, -1.908F, -7.8F, 10.6F, 3.816F, 8.4F)
                .texOffs(239, 42).addBox(-1.908F, -5.3F, -7.72F, 3.816F, 10.6F, 8.24F)
                .texOffs(358, 130).addBox(-1.908F, 4.3F, -7.2F, 3.816F, 1.6F, 7.2F)
                .texOffs(359, 117).addBox(-0.35F, -8.5F, -6.7F, 0.7F, 4.1F, 6.2F),
                PartPose.offsetAndRotation(0F, 0F, -7.5F, -0.2793F, 0F, 0F));
        PartDefinition p_neck3 = p_neck2.addOrReplaceChild("neck3", CubeListBuilder.create()
                .texOffs(228, 102).addBox(-4.8F, -1.728F, -7.6F, 9.6F, 3.456F, 8.2F)
                .texOffs(342, 42).addBox(-1.728F, -4.8F, -7.52F, 3.456F, 9.6F, 8.04F)
                .texOffs(105, 142).addBox(-1.728F, 3.8F, -7F, 3.456F, 1.6F, 7F)
                .texOffs(48, 142).addBox(-0.35F, -7.6F, -6.5F, 0.7F, 3.7F, 6F),
                PartPose.offsetAndRotation(0F, 0F, -7.2F, -0.1047F, 0F, 0F));
        PartDefinition p_neck4 = p_neck3.addOrReplaceChild("neck4", CubeListBuilder.create()
                .texOffs(0, 117).addBox(-4.4F, -1.584F, -7.2F, 8.8F, 3.168F, 7.8F)
                .texOffs(130, 67).addBox(-1.584F, -4.4F, -7.12F, 3.168F, 8.8F, 7.64F)
                .texOffs(128, 142).addBox(-1.584F, 3.4F, -6.6F, 3.168F, 1.6F, 6.6F)
                .texOffs(63, 142).addBox(-0.35F, -6.8F, -6.1F, 0.7F, 3.3F, 5.6F),
                PartPose.offsetAndRotation(0F, 0F, -7F, 0.1047F, 0F, 0F));
        PartDefinition p_neck5 = p_neck4.addOrReplaceChild("neck5", CubeListBuilder.create()
                .texOffs(376, 117).addBox(-4.1F, -1.476F, -6.8F, 8.2F, 2.952F, 7.4F)
                .texOffs(155, 67).addBox(-1.476F, -4.1F, -6.72F, 2.952F, 8.2F, 7.24F)
                .texOffs(151, 142).addBox(-1.476F, 3.1F, -6.2F, 2.952F, 1.6F, 6.2F)
                .texOffs(248, 142).addBox(-0.35F, -6.1F, -5.7F, 0.7F, 2.9F, 5.2F),
                PartPose.offsetAndRotation(0F, 0F, -6.6F, 0.2094F, 0F, 0F));
        PartDefinition p_head = p_neck5.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(369, 42).addBox(-6F, -2.6F, -12F, 12F, 4.7F, 12.5F)
                .texOffs(37, 42).addBox(-3.6F, -5F, -11.92F, 7.2F, 9.5F, 12.34F)
                .texOffs(371, 142).addBox(-4.8F, -3.6F, -15F, 9.6F, 0.6F, 5F)
                .texOffs(273, 142).addBox(-3.8F, -4.6F, -14.92F, 7.6F, 2.6F, 4.84F)
                .texOffs(409, 102).addBox(-4.6F, -1.6F, -19F, 9.2F, 3.4F, 8F)
                .texOffs(46, 85).addBox(-2.8F, -3.4F, -18.92F, 5.6F, 7F, 7.84F)
                .texOffs(477, 117).addBox(-4F, -1.4F, -25.5F, 8F, 3.4F, 7F)
                .texOffs(37, 102).addBox(-2.4F, -3F, -25.42F, 4.8F, 6.6F, 6.84F)
                .texOffs(0, 102).addBox(-2.6F, -4.4F, -24F, 5.2F, 1.6F, 12F)
                .texOffs(85, 153).addBox(-3.2F, -2.6F, -26.3F, 6.4F, 3.8F, 1F)
                .texOffs(242, 153).addBox(1.2F, -2.4F, -26.5F, 1.4F, 0.8F, 0.5F)
                .texOffs(300, 142).addBox(4.2F, -3.2F, -12.4F, 1.95F, 2.2F, 4.2F)
                .texOffs(102, 153).addBox(3.2F, 3.6F, -24.5786F, 0.8F, 1.62F, 0.8F)
                .texOffs(107, 153).addBox(3.2F, 3.6F, -22.9357F, 0.8F, 1.2F, 0.8F)
                .texOffs(112, 153).addBox(3.2F, 3.6F, -21.2929F, 0.8F, 1.2F, 0.8F)
                .texOffs(117, 153).addBox(3.2F, 3.6F, -19.65F, 0.8F, 1.2F, 0.8F)
                .texOffs(122, 153).addBox(3.2F, 3.6F, -18.0071F, 0.8F, 1.2F, 0.8F)
                .texOffs(127, 153).addBox(3.2F, 3.6F, -16.3643F, 0.8F, 1.2F, 0.8F)
                .texOffs(132, 153).addBox(3.2F, 3.6F, -14.7214F, 0.8F, 1.62F, 0.8F)
                .texOffs(57, 153).addBox(2.7F, 3.6F, -23.5F, 1F, 3.2F, 1.1F)
                .texOffs(249, 153).addBox(-2.6F, -2.4F, -26.5F, 1.4F, 0.8F, 0.5F)
                .texOffs(315, 142).addBox(-6.15F, -3.2F, -12.4F, 1.95F, 2.2F, 4.2F)
                .texOffs(137, 153).addBox(-4F, 3.6F, -24.5786F, 0.8F, 1.62F, 0.8F)
                .texOffs(142, 153).addBox(-4F, 3.6F, -22.9357F, 0.8F, 1.2F, 0.8F)
                .texOffs(147, 153).addBox(-4F, 3.6F, -21.2929F, 0.8F, 1.2F, 0.8F)
                .texOffs(152, 153).addBox(-4F, 3.6F, -19.65F, 0.8F, 1.2F, 0.8F)
                .texOffs(157, 153).addBox(-4F, 3.6F, -18.0071F, 0.8F, 1.2F, 0.8F)
                .texOffs(162, 153).addBox(-4F, 3.6F, -16.3643F, 0.8F, 1.2F, 0.8F)
                .texOffs(167, 153).addBox(-4F, 3.6F, -14.7214F, 0.8F, 1.62F, 0.8F)
                .texOffs(64, 153).addBox(-3.7F, 3.6F, -23.5F, 1F, 3.2F, 1.1F)
                .texOffs(267, 102).addBox(-3.4F, 3.62F, -24.8F, 6.8F, 0.3F, 12F)
                .texOffs(62, 102).addBox(-0.4F, -9.5F, -11F, 0.8F, 4.8F, 9F),
                PartPose.offsetAndRotation(0F, 0F, -6F, 0.5585F, 0F, 0F));
        PartDefinition p_brow_l = p_head.addOrReplaceChild("brow_l", CubeListBuilder.create()
                .texOffs(383, 130).addBox(-2F, -1F, -0.6F, 4F, 1.9F, 7.5F),
                PartPose.offsetAndRotation(4F, -4.4F, -12.4F, 0.1047F, 0F, -0.1396F));
        PartDefinition p_barbel_l1 = p_head.addOrReplaceChild("barbel_l1", CubeListBuilder.create()
                .texOffs(263, 142).addBox(-0.5F, 0F, -0.5F, 1F, 8F, 1F),
                PartPose.offsetAndRotation(3.8F, 1.5F, -23F, 0.3491F, 0F, 0.4363F));
        PartDefinition p_barbel_l2 = p_barbel_l1.addOrReplaceChild("barbel_l2", CubeListBuilder.create()
                .texOffs(330, 142).addBox(-0.35F, 0F, -0.35F, 0.7F, 7F, 0.7F),
                PartPose.offsetAndRotation(0F, 7.6F, 0F, 0.2618F, 0F, -0.1745F));
        PartDefinition p_brow_r = p_head.addOrReplaceChild("brow_r", CubeListBuilder.create()
                .texOffs(408, 130).addBox(-2F, -1F, -0.6F, 4F, 1.9F, 7.5F),
                PartPose.offsetAndRotation(-4F, -4.4F, -12.4F, 0.1047F, 0F, 0.1396F));
        PartDefinition p_barbel_r1 = p_head.addOrReplaceChild("barbel_r1", CubeListBuilder.create()
                .texOffs(268, 142).addBox(-0.5F, 0F, -0.5F, 1F, 8F, 1F),
                PartPose.offsetAndRotation(-3.8F, 1.5F, -23F, 0.3491F, 0F, -0.4363F));
        PartDefinition p_barbel_r2 = p_barbel_r1.addOrReplaceChild("barbel_r2", CubeListBuilder.create()
                .texOffs(335, 142).addBox(-0.35F, 0F, -0.35F, 0.7F, 7F, 0.7F),
                PartPose.offsetAndRotation(0F, 7.6F, 0F, 0.2618F, 0F, 0.1745F));
        PartDefinition p_frill_c0 = p_head.addOrReplaceChild("frill_c0", CubeListBuilder.create()
                .texOffs(458, 67).addBox(-5.5F, -15F, -0.5F, 11F, 15F, 1F),
                PartPose.offsetAndRotation(0F, -0.5F, -2.5F, -0.4189F, 0F, 0F));
        PartDefinition p_frill_l1 = p_head.addOrReplaceChild("frill_l1", CubeListBuilder.create()
                .texOffs(49, 67).addBox(-6F, -16F, -0.5F, 12F, 16F, 1F),
                PartPose.offsetAndRotation(0F, -0.5F, -2.5F, -0.4189F, 0F, 0.6981F));
        PartDefinition p_frill_r1 = p_head.addOrReplaceChild("frill_r1", CubeListBuilder.create()
                .texOffs(76, 67).addBox(-6F, -16F, -0.5F, 12F, 16F, 1F),
                PartPose.offsetAndRotation(0F, -0.5F, -2.5F, -0.4189F, 0F, -0.6981F));
        PartDefinition p_frill_l2 = p_head.addOrReplaceChild("frill_l2", CubeListBuilder.create()
                .texOffs(102, 85).addBox(-5.5F, -14F, -0.5F, 11F, 14F, 1F),
                PartPose.offsetAndRotation(0F, -0.5F, -2.5F, -0.4189F, 0F, 1.3963F));
        PartDefinition p_frill_r2 = p_head.addOrReplaceChild("frill_r2", CubeListBuilder.create()
                .texOffs(127, 85).addBox(-5.5F, -14F, -0.5F, 11F, 14F, 1F),
                PartPose.offsetAndRotation(0F, -0.5F, -2.5F, -0.4189F, 0F, -1.3963F));
        PartDefinition p_frill_l3 = p_head.addOrReplaceChild("frill_l3", CubeListBuilder.create()
                .texOffs(241, 117).addBox(-4.5F, -11F, -0.5F, 9F, 11F, 1F),
                PartPose.offsetAndRotation(0F, -0.5F, -2.5F, -0.4189F, 0F, 2.0246F));
        PartDefinition p_frill_r3 = p_head.addOrReplaceChild("frill_r3", CubeListBuilder.create()
                .texOffs(262, 117).addBox(-4.5F, -11F, -0.5F, 9F, 11F, 1F),
                PartPose.offsetAndRotation(0F, -0.5F, -2.5F, -0.4189F, 0F, -2.0246F));
        PartDefinition p_jaw = p_head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(0, 67).addBox(-4F, 1.1F, -15F, 8F, 0.8F, 15.4F)
                .texOffs(266, 42).addBox(-2.9F, 0F, -14.92F, 5.8F, 3F, 15.24F)
                .texOffs(340, 142).addBox(-4.8F, 0.8F, -3F, 9.6F, 1.4F, 4.5F)
                .texOffs(78, 142).addBox(-3.6F, -0.4F, -2.92F, 7.2F, 3.8F, 4.34F)
                .texOffs(172, 153).addBox(2.925F, -1.385F, -14.1821F, 0.75F, 1.485F, 0.75F)
                .texOffs(177, 153).addBox(2.925F, -1F, -12.5964F, 0.75F, 1.1F, 0.75F)
                .texOffs(182, 153).addBox(2.925F, -1F, -11.0107F, 0.75F, 1.1F, 0.75F)
                .texOffs(187, 153).addBox(2.925F, -1F, -9.425F, 0.75F, 1.1F, 0.75F)
                .texOffs(192, 153).addBox(2.925F, -1F, -7.8393F, 0.75F, 1.1F, 0.75F)
                .texOffs(197, 153).addBox(2.925F, -1F, -6.2536F, 0.75F, 1.1F, 0.75F)
                .texOffs(202, 153).addBox(2.925F, -1.385F, -4.6679F, 0.75F, 1.485F, 0.75F)
                .texOffs(71, 153).addBox(2.4F, -3F, -13F, 1F, 3.1F, 1.1F)
                .texOffs(207, 153).addBox(-3.675F, -1.385F, -14.1821F, 0.75F, 1.485F, 0.75F)
                .texOffs(212, 153).addBox(-3.675F, -1F, -12.5964F, 0.75F, 1.1F, 0.75F)
                .texOffs(217, 153).addBox(-3.675F, -1F, -11.0107F, 0.75F, 1.1F, 0.75F)
                .texOffs(222, 153).addBox(-3.675F, -1F, -9.425F, 0.75F, 1.1F, 0.75F)
                .texOffs(227, 153).addBox(-3.675F, -1F, -7.8393F, 0.75F, 1.1F, 0.75F)
                .texOffs(232, 153).addBox(-3.675F, -1F, -6.2536F, 0.75F, 1.1F, 0.75F)
                .texOffs(237, 153).addBox(-3.675F, -1.385F, -4.6679F, 0.75F, 1.485F, 0.75F)
                .texOffs(78, 153).addBox(-3.4F, -3F, -13F, 1F, 3.1F, 1.1F)
                .texOffs(386, 67).addBox(-3.4F, -0.15F, -14.8F, 6.8F, 0.3F, 14.6F)
                .texOffs(175, 117).addBox(-2.3F, -0.45F, -13.5F, 4.6F, 0.5F, 11F)
                .texOffs(411, 117).addBox(-2.8F, 2.8F, -10F, 5.6F, 0.7F, 10F)
                .texOffs(260, 130).addBox(-0.4F, 3.2F, -11F, 0.8F, 3.5F, 7F),
                PartPose.offsetAndRotation(0F, 3.4F, -10F, 0.0524F, 0F, 0F));
        PartDefinition p_tail1 = p_body.addOrReplaceChild("tail1", CubeListBuilder.create()
                .texOffs(178, 67).addBox(-7.5F, -2.34F, -0.6F, 15F, 4.68F, 10.7F)
                .texOffs(436, 0).addBox(-3.34F, -6.5F, -0.52F, 6.68F, 13F, 10.54F)
                .texOffs(35, 117).addBox(-3.34F, 5.5F, 0F, 6.68F, 1.6F, 9.5F)
                .texOffs(483, 67).addBox(-0.35F, -11.3F, 0.5F, 0.7F, 5.7F, 9.5F),
                PartPose.offsetAndRotation(0F, 0F, 12.5F, -0.0349F, 0F, 0F));
        PartDefinition p_tail2 = p_tail1.addOrReplaceChild("tail2", CubeListBuilder.create()
                .texOffs(231, 67).addBox(-7.245F, -2.2608F, -0.6F, 14.49F, 4.5216F, 10.5F)
                .texOffs(473, 0).addBox(-3.2258F, -6.28F, -0.52F, 6.4516F, 12.56F, 10.34F)
                .texOffs(70, 117).addBox(-3.2258F, 5.28F, 0F, 6.4516F, 1.6F, 9.3F)
                .texOffs(0, 85).addBox(-0.35F, -10.78F, 0.5F, 0.7F, 5.4F, 9.3F),
                PartPose.offset(0F, 0F, 9.5F));
        PartDefinition p_tail3 = p_tail2.addOrReplaceChild("tail3", CubeListBuilder.create()
                .texOffs(284, 67).addBox(-6.935F, -2.1618F, -0.6F, 13.87F, 4.3236F, 10.3F)
                .texOffs(0, 42).addBox(-3.0918F, -6.005F, -0.52F, 6.1836F, 12.01F, 10.14F)
                .texOffs(105, 117).addBox(-3.0918F, 5.005F, 0F, 6.1836F, 1.6F, 9.1F)
                .texOffs(23, 85).addBox(-0.35F, -10.205F, 0.5F, 0.7F, 5.1F, 9.1F),
                PartPose.offset(0F, 0F, 9.3F));
        PartDefinition p_tail4 = p_tail3.addOrReplaceChild("tail4", CubeListBuilder.create()
                .texOffs(335, 67).addBox(-6.595F, -2.0574F, -0.6F, 13.19F, 4.1148F, 10.1F)
                .texOffs(80, 42).addBox(-2.9374F, -5.715F, -0.52F, 5.8748F, 11.43F, 9.94F)
                .texOffs(0, 130).addBox(-2.9374F, 4.715F, 0F, 5.8748F, 1.6F, 8.9F)
                .texOffs(83, 102).addBox(-0.35F, -9.615F, 0.5F, 0.7F, 4.8F, 8.9F)
                .texOffs(402, 142).addBox(5.795F, 3.715F, 2F, 4F, 0.8F, 5F)
                .texOffs(421, 142).addBox(-9.795F, 3.715F, 2F, 4F, 0.8F, 5F),
                PartPose.offset(0F, 0F, 9.1F));
        PartDefinition p_tail5 = p_tail4.addOrReplaceChild("tail5", CubeListBuilder.create()
                .texOffs(257, 85).addBox(-6.24F, -1.9458F, -0.6F, 12.48F, 3.8916F, 9.9F)
                .texOffs(113, 42).addBox(-2.7808F, -5.405F, -0.52F, 5.5616F, 10.81F, 9.74F)
                .texOffs(31, 130).addBox(-2.7808F, 4.405F, 0F, 5.5616F, 1.6F, 8.7F)
                .texOffs(104, 102).addBox(-0.35F, -9.005F, 0.5F, 0.7F, 4.5F, 8.7F)
                .texOffs(440, 142).addBox(5.44F, 3.405F, 2F, 4F, 0.8F, 5F)
                .texOffs(459, 142).addBox(-9.44F, 3.405F, 2F, 4F, 0.8F, 5F),
                PartPose.offset(0F, 0F, 8.9F));
        PartDefinition p_tail6 = p_tail5.addOrReplaceChild("tail6", CubeListBuilder.create()
                .texOffs(304, 85).addBox(-5.875F, -1.8306F, -0.6F, 11.75F, 3.6612F, 9.7F)
                .texOffs(146, 42).addBox(-2.6206F, -5.085F, -0.52F, 5.2412F, 10.17F, 9.54F)
                .texOffs(62, 130).addBox(-2.6206F, 4.085F, 0F, 5.2412F, 1.6F, 8.5F)
                .texOffs(125, 102).addBox(-0.35F, -8.385F, 0.5F, 0.7F, 4.2F, 8.5F)
                .texOffs(478, 142).addBox(5.075F, 3.085F, 2F, 4F, 0.8F, 5F)
                .texOffs(0, 153).addBox(-9.075F, 3.085F, 2F, 4F, 0.8F, 5F),
                PartPose.offset(0F, 0F, 8.7F));
        PartDefinition p_tail7 = p_tail6.addOrReplaceChild("tail7", CubeListBuilder.create()
                .texOffs(392, 85).addBox(-5.495F, -1.7118F, -0.6F, 10.99F, 3.4236F, 9.5F)
                .texOffs(208, 42).addBox(-2.4518F, -4.755F, -0.52F, 4.9036F, 9.51F, 9.34F)
                .texOffs(124, 130).addBox(-2.4518F, 3.755F, 0F, 4.9036F, 1.6F, 8.3F)
                .texOffs(329, 102).addBox(-0.35F, -7.755F, 0.5F, 0.7F, 3.9F, 8.3F)
                .texOffs(19, 153).addBox(4.695F, 2.755F, 2F, 4F, 0.8F, 5F)
                .texOffs(38, 153).addBox(-8.695F, 2.755F, 2F, 4F, 0.8F, 5F),
                PartPose.offset(0F, 0F, 8.5F));
        PartDefinition p_tail8 = p_tail7.addOrReplaceChild("tail8", CubeListBuilder.create()
                .texOffs(435, 85).addBox(-5.105F, -1.5912F, -0.6F, 10.21F, 3.1824F, 9.3F)
                .texOffs(311, 42).addBox(-2.2762F, -4.42F, -0.52F, 4.5524F, 8.84F, 9.14F)
                .texOffs(153, 130).addBox(-2.2762F, 3.42F, 0F, 4.5524F, 1.6F, 8.1F)
                .texOffs(350, 102).addBox(-0.35F, -7.12F, 0.5F, 0.7F, 3.6F, 8.1F),
                PartPose.offset(0F, 0F, 8.3F));
        PartDefinition p_tail9 = p_tail8.addOrReplaceChild("tail9", CubeListBuilder.create()
                .texOffs(187, 102).addBox(-4.71F, -1.467F, -0.6F, 9.42F, 2.934F, 9.1F)
                .texOffs(420, 42).addBox(-2.102F, -4.075F, -0.52F, 4.204F, 8.15F, 8.94F)
                .texOffs(304, 130).addBox(-2.102F, 3.075F, 0F, 4.204F, 1.6F, 7.9F)
                .texOffs(304, 117).addBox(-0.35F, -6.475F, 0.5F, 0.7F, 3.3F, 7.9F),
                PartPose.offset(0F, 0F, 8.1F));
        PartDefinition p_tail10 = p_tail9.addOrReplaceChild("tail10", CubeListBuilder.create()
                .texOffs(446, 102).addBox(-4.305F, -1.3392F, -0.6F, 8.61F, 2.6784F, 8.9F)
                .texOffs(103, 67).addBox(-1.9242F, -3.72F, -0.52F, 3.8484F, 7.44F, 8.74F)
                .texOffs(433, 130).addBox(-1.9242F, 2.72F, 0F, 3.8484F, 1.6F, 7.7F)
                .texOffs(323, 117).addBox(-0.35F, -5.82F, 0.5F, 0.7F, 3F, 7.7F),
                PartPose.offset(0F, 0F, 7.9F));
        PartDefinition p_tail11 = p_tail10.addOrReplaceChild("tail11", CubeListBuilder.create()
                .texOffs(140, 117).addBox(-3.895F, -1.2114F, -0.6F, 7.79F, 2.4228F, 8.7F)
                .texOffs(431, 67).addBox(-1.7414F, -3.365F, -0.52F, 3.4828F, 6.73F, 8.54F)
                .texOffs(458, 130).addBox(-1.7414F, 2.365F, 0F, 3.4828F, 1.6F, 7.5F)
                .texOffs(203, 130).addBox(-0.35F, -5.165F, 0.5F, 0.7F, 2.7F, 7.5F),
                PartPose.offset(0F, 0F, 7.7F));
        PartDefinition p_tail12 = p_tail11.addOrReplaceChild("tail12", CubeListBuilder.create()
                .texOffs(208, 117).addBox(-3.475F, -1.08F, -0.6F, 6.95F, 2.16F, 8.5F)
                .texOffs(75, 85).addBox(-1.555F, -3F, -0.52F, 3.11F, 6F, 8.34F)
                .texOffs(483, 130).addBox(-1.555F, 2F, 0F, 3.11F, 1.6F, 7.3F)
                .texOffs(222, 130).addBox(-0.35F, -4.5F, 0.5F, 0.7F, 2.4F, 7.3F),
                PartPose.offset(0F, 0F, 7.5F));
        PartDefinition p_tail13 = p_tail12.addOrReplaceChild("tail13", CubeListBuilder.create()
                .texOffs(444, 117).addBox(-3.05F, -0.9468F, -0.6F, 6.1F, 1.8936F, 8.3F)
                .texOffs(152, 85).addBox(-1.3668F, -2.63F, -0.52F, 2.7336F, 5.26F, 8.14F)
                .texOffs(25, 142).addBox(-1.3668F, 1.63F, 0F, 2.7336F, 1.6F, 7.1F)
                .texOffs(241, 130).addBox(-0.35F, -3.83F, 0.5F, 0.7F, 2.1F, 7.1F),
                PartPose.offset(0F, 0F, 7.3F));
        PartDefinition p_tail14 = p_tail13.addOrReplaceChild("tail14", CubeListBuilder.create()
                .texOffs(93, 130).addBox(-2.62F, -0.8136F, -0.6F, 5.24F, 1.6272F, 8.1F)
                .texOffs(306, 102).addBox(-1.1736F, -2.26F, -0.52F, 2.3472F, 4.52F, 7.94F)
                .texOffs(172, 142).addBox(-1.1736F, 1.26F, 0F, 2.3472F, 1.6F, 6.9F)
                .texOffs(231, 142).addBox(-0.35F, -3.16F, 0.5F, 0.7F, 1.8F, 6.9F),
                PartPose.offset(0F, 0F, 7.1F));
        PartDefinition p_tail15 = p_tail14.addOrReplaceChild("tail15", CubeListBuilder.create()
                .texOffs(331, 130).addBox(-2.19F, -0.6768F, -0.6F, 4.38F, 1.3536F, 7.9F)
                .texOffs(283, 117).addBox(-0.9868F, -1.88F, -0.52F, 1.9736F, 3.76F, 7.74F)
                .texOffs(193, 142).addBox(-0.9868F, 0.88F, 0F, 1.9736F, 1.6F, 6.7F),
                PartPose.offset(0F, 0F, 6.9F));
        PartDefinition p_tail16 = p_tail15.addOrReplaceChild("tail16", CubeListBuilder.create()
                .texOffs(0, 142).addBox(-1.75F, -0.54F, -0.6F, 3.5F, 1.08F, 7.7F)
                .texOffs(182, 130).addBox(-0.79F, -1.5F, -0.52F, 1.58F, 3F, 7.54F)
                .texOffs(212, 142).addBox(-0.79F, 0.5F, 0F, 1.58F, 1.6F, 6.5F)
                .texOffs(73, 0).addBox(-0.6F, -11F, 1.5F, 1.2F, 22F, 14F),
                PartPose.offset(0F, 0F, 6.7F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
