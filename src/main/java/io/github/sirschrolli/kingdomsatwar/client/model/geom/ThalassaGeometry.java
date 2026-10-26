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
    public static final int TEXTURE_HEIGHT = 512;

    private ThalassaGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-8F, -7F, -14F, 16F, 14F, 27F)
                .texOffs(296, 0).addBox(-6.5F, -9F, -12F, 13F, 2.5F, 22F)
                .texOffs(120, 0).addBox(-7F, 6F, -13F, 14F, 2.5F, 25F)
                .texOffs(199, 0).addBox(-0.4F, -15F, -11F, 0.8F, 6.5F, 20F),
                PartPose.offset(0F, 14F, 0F));
        PartDefinition p_front_leg_l = p_body.addOrReplaceChild("front_leg_l", CubeListBuilder.create()
                .texOffs(76, 67).addBox(-2.6F, -1F, -3.6F, 5.2F, 9.5F, 7.2F),
                PartPose.offsetAndRotation(7.5F, 3.5F, -7.5F, 0.4363F, 0F, -1.0123F));
        PartDefinition p_front_shin_l = p_front_leg_l.addOrReplaceChild("front_shin_l", CubeListBuilder.create()
                .texOffs(50, 86).addBox(-2.1F, -0.5F, -3F, 4.2F, 8.5F, 6F),
                PartPose.offsetAndRotation(0F, 8.5F, 0F, 0.4887F, 0F, 0F));
        PartDefinition p_front_foot_l = p_front_shin_l.addOrReplaceChild("front_foot_l", CubeListBuilder.create()
                .texOffs(242, 0).addBox(-0.7F, -0.5F, -5.5F, 1.4F, 15F, 11F),
                PartPose.offsetAndRotation(0F, 7.8F, 0F, 0.384F, 0F, 0F));
        PartDefinition p_front_leg_r = p_body.addOrReplaceChild("front_leg_r", CubeListBuilder.create()
                .texOffs(105, 67).addBox(-2.6F, -1F, -3.6F, 5.2F, 9.5F, 7.2F),
                PartPose.offsetAndRotation(-7.5F, 3.5F, -7.5F, 0.4363F, 0F, 1.0123F));
        PartDefinition p_front_shin_r = p_front_leg_r.addOrReplaceChild("front_shin_r", CubeListBuilder.create()
                .texOffs(73, 86).addBox(-2.1F, -0.5F, -3F, 4.2F, 8.5F, 6F),
                PartPose.offsetAndRotation(0F, 8.5F, 0F, 0.4887F, 0F, 0F));
        PartDefinition p_front_foot_r = p_front_shin_r.addOrReplaceChild("front_foot_r", CubeListBuilder.create()
                .texOffs(269, 0).addBox(-0.7F, -0.5F, -5.5F, 1.4F, 15F, 11F),
                PartPose.offsetAndRotation(0F, 7.8F, 0F, 0.384F, 0F, 0F));
        PartDefinition p_neck1 = p_body.addOrReplaceChild("neck1", CubeListBuilder.create()
                .texOffs(288, 42).addBox(-6F, -6F, -8F, 12F, 12F, 8.5F)
                .texOffs(30, 115).addBox(-5F, 5.5F, -7F, 10F, 1.5F, 6.5F)
                .texOffs(59, 102).addBox(-0.35F, -10F, -7F, 0.7F, 4.5F, 6.5F),
                PartPose.offsetAndRotation(0F, -2F, -13.5F, -0.4887F, 0F, 0F));
        PartDefinition p_neck2 = p_neck1.addOrReplaceChild("neck2", CubeListBuilder.create()
                .texOffs(374, 42).addBox(-5.3F, -5.3F, -7.7F, 10.6F, 10.6F, 8.2F)
                .texOffs(65, 115).addBox(-4.3F, 4.8F, -6.7F, 8.6F, 1.5F, 6.2F)
                .texOffs(76, 102).addBox(-0.35F, -8.9F, -6.7F, 0.7F, 4.1F, 6.2F),
                PartPose.offsetAndRotation(0F, 0F, -7.5F, -0.2793F, 0F, 0F));
        PartDefinition p_neck3 = p_neck2.addOrReplaceChild("neck3", CubeListBuilder.create()
                .texOffs(39, 67).addBox(-4.8F, -4.8F, -7.5F, 9.6F, 9.6F, 8F)
                .texOffs(277, 115).addBox(-3.8F, 4.3F, -6.5F, 7.6F, 1.5F, 6F)
                .texOffs(0, 115).addBox(-0.35F, -8F, -6.5F, 0.7F, 3.7F, 6F),
                PartPose.offsetAndRotation(0F, 0F, -7.2F, -0.1047F, 0F, 0F));
        PartDefinition p_neck4 = p_neck3.addOrReplaceChild("neck4", CubeListBuilder.create()
                .texOffs(171, 67).addBox(-4.4F, -4.4F, -7.1F, 8.8F, 8.8F, 7.6F)
                .texOffs(306, 115).addBox(-3.4F, 3.9F, -6.1F, 6.8F, 1.5F, 5.6F)
                .texOffs(15, 115).addBox(-0.35F, -7.2F, -6.1F, 0.7F, 3.3F, 5.6F),
                PartPose.offsetAndRotation(0F, 0F, -7F, 0.1047F, 0F, 0F));
        PartDefinition p_neck5 = p_neck4.addOrReplaceChild("neck5", CubeListBuilder.create()
                .texOffs(206, 67).addBox(-4.1F, -4.1F, -6.7F, 8.2F, 8.2F, 7.2F)
                .texOffs(333, 115).addBox(-3.1F, 3.6F, -5.7F, 6.2F, 1.5F, 5.2F)
                .texOffs(252, 115).addBox(-0.35F, -6.5F, -5.7F, 0.7F, 2.9F, 5.2F),
                PartPose.offsetAndRotation(0F, 0F, -6.6F, 0.2094F, 0F, 0F));
        PartDefinition p_head = p_neck5.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(51, 42).addBox(-6F, -5F, -12F, 12F, 10F, 12F)
                .texOffs(149, 42).addBox(-4.5F, -3.2F, -25F, 9F, 6.6F, 13.5F)
                .texOffs(225, 86).addBox(-3.2F, -4.6F, -23.5F, 6.4F, 1.5F, 12F)
                .texOffs(161, 126).addBox(-3.4F, -3.6F, -25.3F, 2F, 1.2F, 1.2F)
                .texOffs(170, 126).addBox(1.4F, -3.6F, -25.3F, 2F, 1.2F, 1.2F)
                .texOffs(141, 86).addBox(-4.1F, 3.3F, -24.6F, 8.2F, 1.2F, 12F)
                .texOffs(96, 86).addBox(-4.1F, 3.2F, -24.5F, 8.2F, 0.2F, 12.5F)
                .texOffs(133, 126).addBox(-3.4F, 3.6F, -23.5F, 1F, 3.2F, 1.2F)
                .texOffs(140, 126).addBox(2.4F, 3.6F, -23.5F, 1F, 3.2F, 1.2F)
                .texOffs(461, 115).addBox(5.6F, -2.8F, -12F, 1F, 2.2F, 4F)
                .texOffs(472, 115).addBox(-6.6F, -2.8F, -12F, 1F, 2.2F, 4F)
                .texOffs(400, 115).addBox(5.2F, -5.6F, -12.5F, 1.6F, 1.4F, 6F)
                .texOffs(417, 115).addBox(-6.8F, -5.6F, -12.5F, 1.6F, 1.4F, 6F)
                .texOffs(264, 86).addBox(-0.4F, -9.5F, -11F, 0.8F, 4.8F, 9F),
                PartPose.offsetAndRotation(0F, 0F, -6F, 0.5585F, 0F, 0F));
        PartDefinition p_barbel_l1 = p_head.addOrReplaceChild("barbel_l1", CubeListBuilder.create()
                .texOffs(267, 115).addBox(-0.5F, 0F, -0.5F, 1F, 8F, 1F),
                PartPose.offsetAndRotation(3.8F, 1.5F, -23F, 0.3491F, 0F, 0.4363F));
        PartDefinition p_barbel_l2 = p_barbel_l1.addOrReplaceChild("barbel_l2", CubeListBuilder.create()
                .texOffs(451, 115).addBox(-0.4F, 0F, -0.4F, 0.8F, 7F, 0.8F),
                PartPose.offsetAndRotation(0F, 7.6F, 0F, 0.2618F, 0F, -0.1745F));
        PartDefinition p_barbel_r1 = p_head.addOrReplaceChild("barbel_r1", CubeListBuilder.create()
                .texOffs(272, 115).addBox(-0.5F, 0F, -0.5F, 1F, 8F, 1F),
                PartPose.offsetAndRotation(-3.8F, 1.5F, -23F, 0.3491F, 0F, -0.4363F));
        PartDefinition p_barbel_r2 = p_barbel_r1.addOrReplaceChild("barbel_r2", CubeListBuilder.create()
                .texOffs(456, 115).addBox(-0.4F, 0F, -0.4F, 0.8F, 7F, 0.8F),
                PartPose.offsetAndRotation(0F, 7.6F, 0F, 0.2618F, 0F, 0.1745F));
        PartDefinition p_frill_c0 = p_head.addOrReplaceChild("frill_c0", CubeListBuilder.create()
                .texOffs(330, 67).addBox(-5.5F, -15F, -0.5F, 11F, 15F, 1F),
                PartPose.offsetAndRotation(0F, -0.5F, -2.5F, -0.4189F, 0F, 0F));
        PartDefinition p_frill_l1 = p_head.addOrReplaceChild("frill_l1", CubeListBuilder.create()
                .texOffs(241, 67).addBox(-6F, -16F, -0.5F, 12F, 16F, 1F),
                PartPose.offsetAndRotation(0F, -0.5F, -2.5F, -0.4189F, 0F, 0.6981F));
        PartDefinition p_frill_r1 = p_head.addOrReplaceChild("frill_r1", CubeListBuilder.create()
                .texOffs(268, 67).addBox(-6F, -16F, -0.5F, 12F, 16F, 1F),
                PartPose.offsetAndRotation(0F, -0.5F, -2.5F, -0.4189F, 0F, -0.6981F));
        PartDefinition p_frill_l2 = p_head.addOrReplaceChild("frill_l2", CubeListBuilder.create()
                .texOffs(0, 86).addBox(-5.5F, -14F, -0.5F, 11F, 14F, 1F),
                PartPose.offsetAndRotation(0F, -0.5F, -2.5F, -0.4189F, 0F, 1.3963F));
        PartDefinition p_frill_r2 = p_head.addOrReplaceChild("frill_r2", CubeListBuilder.create()
                .texOffs(25, 86).addBox(-5.5F, -14F, -0.5F, 11F, 14F, 1F),
                PartPose.offsetAndRotation(0F, -0.5F, -2.5F, -0.4189F, 0F, -1.3963F));
        PartDefinition p_frill_l3 = p_head.addOrReplaceChild("frill_l3", CubeListBuilder.create()
                .texOffs(485, 86).addBox(-4.5F, -11F, -0.5F, 9F, 11F, 1F),
                PartPose.offsetAndRotation(0F, -0.5F, -2.5F, -0.4189F, 0F, 2.0246F));
        PartDefinition p_frill_r3 = p_head.addOrReplaceChild("frill_r3", CubeListBuilder.create()
                .texOffs(0, 102).addBox(-4.5F, -11F, -0.5F, 9F, 11F, 1F),
                PartPose.offsetAndRotation(0F, -0.5F, -2.5F, -0.4189F, 0F, -2.0246F));
        PartDefinition p_jaw = p_head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(415, 42).addBox(-4.2F, 0F, -14.5F, 8.4F, 3.2F, 15F)
                .texOffs(348, 86).addBox(-3.8F, -1.1F, -14.2F, 7.6F, 1.1F, 11F)
                .texOffs(184, 86).addBox(-3.4F, -0.2F, -13.6F, 6.8F, 0.4F, 12.6F)
                .texOffs(147, 126).addBox(-3F, -3.2F, -13.2F, 1F, 3.2F, 1.2F)
                .texOffs(154, 126).addBox(2F, -3.2F, -13.2F, 1F, 3.2F, 1.2F)
                .texOffs(308, 102).addBox(-0.4F, 3F, -11F, 0.8F, 3.5F, 7F),
                PartPose.offsetAndRotation(0F, 3.4F, -10F, 0.0524F, 0F, 0F));
        PartDefinition p_tail1 = p_body.addOrReplaceChild("tail1", CubeListBuilder.create()
                .texOffs(367, 0).addBox(-7.5F, -6.5F, -0.5F, 15F, 13F, 10.5F)
                .texOffs(93, 102).addBox(-6.5F, 6F, 0.5F, 13F, 1.5F, 8.5F)
                .texOffs(355, 67).addBox(-0.35F, -11.7F, 0.5F, 0.7F, 5.7F, 9.5F),
                PartPose.offsetAndRotation(0F, 0F, 12.5F, -0.0349F, 0F, 0F));
        PartDefinition p_tail2 = p_tail1.addOrReplaceChild("tail2", CubeListBuilder.create()
                .texOffs(420, 0).addBox(-7.245F, -6.28F, -0.5F, 14.49F, 12.56F, 10.3F)
                .texOffs(138, 102).addBox(-6.245F, 5.78F, 0.5F, 12.49F, 1.5F, 8.3F)
                .texOffs(378, 67).addBox(-0.35F, -11.18F, 0.5F, 0.7F, 5.4F, 9.3F),
                PartPose.offset(0F, 0F, 9.5F));
        PartDefinition p_tail3 = p_tail2.addOrReplaceChild("tail3", CubeListBuilder.create()
                .texOffs(0, 42).addBox(-6.935F, -6.005F, -0.5F, 13.87F, 12.01F, 10.1F)
                .texOffs(183, 102).addBox(-5.935F, 5.505F, 0.5F, 11.87F, 1.5F, 8.1F)
                .texOffs(401, 67).addBox(-0.35F, -10.605F, 0.5F, 0.7F, 5.1F, 9.1F),
                PartPose.offset(0F, 0F, 9.3F));
        PartDefinition p_tail4 = p_tail3.addOrReplaceChild("tail4", CubeListBuilder.create()
                .texOffs(100, 42).addBox(-6.595F, -5.715F, -0.5F, 13.19F, 11.43F, 9.9F)
                .texOffs(325, 102).addBox(-5.595F, 5.215F, 0.5F, 11.19F, 1.5F, 7.9F)
                .texOffs(285, 86).addBox(-0.35F, -10.015F, 0.5F, 0.7F, 4.8F, 8.9F)
                .texOffs(483, 115).addBox(6.095F, 3.715F, 2F, 4F, 0.8F, 5F)
                .texOffs(0, 126).addBox(-10.095F, 3.715F, 2F, 4F, 0.8F, 5F),
                PartPose.offset(0F, 0F, 9.1F));
        PartDefinition p_tail5 = p_tail4.addOrReplaceChild("tail5", CubeListBuilder.create()
                .texOffs(196, 42).addBox(-6.24F, -5.405F, -0.5F, 12.48F, 10.81F, 9.7F)
                .texOffs(366, 102).addBox(-5.24F, 4.905F, 0.5F, 10.48F, 1.5F, 7.7F)
                .texOffs(306, 86).addBox(-0.35F, -9.405F, 0.5F, 0.7F, 4.5F, 8.7F)
                .texOffs(19, 126).addBox(5.74F, 3.405F, 2F, 4F, 0.8F, 5F)
                .texOffs(38, 126).addBox(-9.74F, 3.405F, 2F, 4F, 0.8F, 5F),
                PartPose.offset(0F, 0F, 8.9F));
        PartDefinition p_tail6 = p_tail5.addOrReplaceChild("tail6", CubeListBuilder.create()
                .texOffs(243, 42).addBox(-5.875F, -5.085F, -0.5F, 11.75F, 10.17F, 9.5F)
                .texOffs(405, 102).addBox(-4.875F, 4.585F, 0.5F, 9.75F, 1.5F, 7.5F)
                .texOffs(327, 86).addBox(-0.35F, -8.785F, 0.5F, 0.7F, 4.2F, 8.5F)
                .texOffs(57, 126).addBox(5.375F, 3.085F, 2F, 4F, 0.8F, 5F)
                .texOffs(76, 126).addBox(-9.375F, 3.085F, 2F, 4F, 0.8F, 5F),
                PartPose.offset(0F, 0F, 8.7F));
        PartDefinition p_tail7 = p_tail6.addOrReplaceChild("tail7", CubeListBuilder.create()
                .texOffs(331, 42).addBox(-5.495F, -4.755F, -0.5F, 10.99F, 9.51F, 9.3F)
                .texOffs(442, 102).addBox(-4.495F, 4.255F, 0.5F, 8.99F, 1.5F, 7.3F)
                .texOffs(416, 86).addBox(-0.35F, -8.155F, 0.5F, 0.7F, 3.9F, 8.3F)
                .texOffs(95, 126).addBox(4.995F, 2.755F, 2F, 4F, 0.8F, 5F)
                .texOffs(114, 126).addBox(-8.995F, 2.755F, 2F, 4F, 0.8F, 5F),
                PartPose.offset(0F, 0F, 8.5F));
        PartDefinition p_tail8 = p_tail7.addOrReplaceChild("tail8", CubeListBuilder.create()
                .texOffs(464, 42).addBox(-5.105F, -4.42F, -0.5F, 10.21F, 8.84F, 9.1F)
                .texOffs(477, 102).addBox(-4.105F, 3.92F, 0.5F, 8.21F, 1.5F, 7.1F)
                .texOffs(437, 86).addBox(-0.35F, -7.52F, 0.5F, 0.7F, 3.6F, 8.1F),
                PartPose.offset(0F, 0F, 8.3F));
        PartDefinition p_tail9 = p_tail8.addOrReplaceChild("tail9", CubeListBuilder.create()
                .texOffs(0, 67).addBox(-4.71F, -4.075F, -0.5F, 9.42F, 8.15F, 8.9F)
                .texOffs(98, 115).addBox(-3.71F, 3.575F, 0.5F, 7.42F, 1.5F, 6.9F)
                .texOffs(21, 102).addBox(-0.35F, -6.875F, 0.5F, 0.7F, 3.3F, 7.9F),
                PartPose.offset(0F, 0F, 8.1F));
        PartDefinition p_tail10 = p_tail9.addOrReplaceChild("tail10", CubeListBuilder.create()
                .texOffs(134, 67).addBox(-4.305F, -3.72F, -0.5F, 8.61F, 7.44F, 8.7F)
                .texOffs(129, 115).addBox(-3.305F, 3.22F, 0.5F, 6.61F, 1.5F, 6.7F)
                .texOffs(40, 102).addBox(-0.35F, -6.22F, 0.5F, 0.7F, 3F, 7.7F),
                PartPose.offset(0F, 0F, 7.9F));
        PartDefinition p_tail11 = p_tail10.addOrReplaceChild("tail11", CubeListBuilder.create()
                .texOffs(295, 67).addBox(-3.895F, -3.365F, -0.5F, 7.79F, 6.73F, 8.5F)
                .texOffs(158, 115).addBox(-2.895F, 2.865F, 0.5F, 5.79F, 1.5F, 6.5F)
                .texOffs(251, 102).addBox(-0.35F, -5.565F, 0.5F, 0.7F, 2.7F, 7.5F),
                PartPose.offset(0F, 0F, 7.7F));
        PartDefinition p_tail12 = p_tail11.addOrReplaceChild("tail12", CubeListBuilder.create()
                .texOffs(424, 67).addBox(-3.475F, -3F, -0.5F, 6.95F, 6F, 8.3F)
                .texOffs(185, 115).addBox(-2.475F, 2.5F, 0.5F, 4.95F, 1.5F, 6.3F)
                .texOffs(270, 102).addBox(-0.35F, -4.9F, 0.5F, 0.7F, 2.4F, 7.3F),
                PartPose.offset(0F, 0F, 7.5F));
        PartDefinition p_tail13 = p_tail12.addOrReplaceChild("tail13", CubeListBuilder.create()
                .texOffs(457, 67).addBox(-3.05F, -2.63F, -0.5F, 6.1F, 5.26F, 8.1F)
                .texOffs(210, 115).addBox(-2.05F, 2.13F, 0.5F, 4.1F, 1.5F, 6.1F)
                .texOffs(289, 102).addBox(-0.35F, -4.23F, 0.5F, 0.7F, 2.1F, 7.1F),
                PartPose.offset(0F, 0F, 7.3F));
        PartDefinition p_tail14 = p_tail13.addOrReplaceChild("tail14", CubeListBuilder.create()
                .texOffs(387, 86).addBox(-2.62F, -2.26F, -0.5F, 5.24F, 4.52F, 7.9F)
                .texOffs(360, 115).addBox(-1.62F, 1.76F, 0.5F, 3.24F, 1.5F, 5.9F)
                .texOffs(235, 115).addBox(-0.35F, -3.56F, 0.5F, 0.7F, 1.8F, 6.9F),
                PartPose.offset(0F, 0F, 7.1F));
        PartDefinition p_tail15 = p_tail14.addOrReplaceChild("tail15", CubeListBuilder.create()
                .texOffs(458, 86).addBox(-2.19F, -1.88F, -0.5F, 4.38F, 3.76F, 7.7F)
                .texOffs(381, 115).addBox(-1.19F, 1.38F, 0.5F, 2.38F, 1.5F, 5.7F),
                PartPose.offset(0F, 0F, 6.9F));
        PartDefinition p_tail16 = p_tail15.addOrReplaceChild("tail16", CubeListBuilder.create()
                .texOffs(226, 102).addBox(-1.75F, -1.5F, -0.5F, 3.5F, 3F, 7.5F)
                .texOffs(434, 115).addBox(-0.75F, 1F, 0.5F, 1.5F, 1.5F, 5.5F)
                .texOffs(87, 0).addBox(-0.6F, -11F, 1.5F, 1.2F, 22F, 14F),
                PartPose.offset(0F, 0F, 6.7F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
