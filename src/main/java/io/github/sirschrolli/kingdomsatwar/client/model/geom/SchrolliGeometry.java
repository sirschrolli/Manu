package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie Schrollis des Schöpfers (im Spiel ×2, in der Wahren Gestalt ×2,6): Gewand, Brustpanzer, sechs Schwingen, Heiligenschein, Lichtkrone, Zepter und Weltkugel; nur in der Wahren Gestalt sichtbar: true_rings (zwei Räder mit Augen) und true_corona (Strahlenkranz); nur am Boden (seit 0.76): Beine (left_leg/right_leg) und die Klinge aus Licht (blade).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class SchrolliGeometry {
    public static final int TEXTURE_WIDTH = 320;
    public static final int TEXTURE_HEIGHT = 320;

    private SchrolliGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(25, 0).addBox(-5.5F, -15F, -3.5F, 11F, 15F, 7F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(265, 58).addBox(-6F, -15.9F, -3.8F, 12F, 8F, 1F, new CubeDeformation(-0.1F, -0.3F, 0F))
                .texOffs(292, 58).addBox(-6F, -15.9F, 2.85F, 12F, 8F, 1F, new CubeDeformation(-0.1F, -0.3F, -0.05F))
                .texOffs(272, 45).addBox(-7F, -16.6F, -4F, 14F, 2F, 8F, new CubeDeformation(-0.4F, 0F, -0.4F))
                .texOffs(83, 45).addBox(-6.5F, -2.6F, -4F, 13F, 3F, 8F, new CubeDeformation(-0.2F, -0.2F, -0.3F))
                .texOffs(232, 115).addBox(-1.5F, -2.6F, -4.45F, 3F, 3F, 1F, new CubeDeformation(-0.2F, -0.3F, -0.2F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(190, 0).addBox(-4F, -8F, -4F, 8F, 8F, 8F)
                .texOffs(167, 45).addBox(-4.5F, -9F, -4.5F, 9F, 2F, 9F, new CubeDeformation(-0.1F, -0.2F, -0.1F))
                .texOffs(200, 105).addBox(-4.5F, -8.7F, -4.8F, 9F, 3F, 1F, new CubeDeformation(-0.3F, -0.4F, -0.1F))
                .texOffs(0, 29).addBox(-4.8F, -7.9F, -3.5F, 1F, 8F, 7F, new CubeDeformation(0F, -0.3F, -0.1F))
                .texOffs(17, 29).addBox(3.8F, -7.9F, -3.5F, 1F, 8F, 7F, new CubeDeformation(0F, -0.3F, -0.1F))
                .texOffs(126, 45).addBox(-5F, -6.9F, -5F, 10F, 1F, 10F, new CubeDeformation(-0.45F, 0F, -0.45F))
                .texOffs(24, 120).addBox(-1F, -7.35F, -5.15F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.35F, -0.25F)),
                PartPose.offset(0F, -16.6F, -0.3F));
        PartDefinition p_hair_back = p_head.addOrReplaceChild("hair_back", CubeListBuilder.create()
                .texOffs(0, 45).addBox(-4.5F, -0.25F, -1F, 9F, 10F, 2F, new CubeDeformation(-0.1F, -0.25F, -0.1F)),
                PartPose.offsetAndRotation(0F, -7.6F, 3.6F, 0.1396F, 0F, 0F));
        PartDefinition p_hair_tip = p_hair_back.addOrReplaceChild("hair_tip", CubeListBuilder.create()
                .texOffs(258, 69).addBox(-4F, 0F, -1F, 8F, 5F, 2F, new CubeDeformation(0F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, 9.5F, 0F, 0.1047F, 0F, 0F));
        PartDefinition p_crown = p_head.addOrReplaceChild("crown", CubeListBuilder.create(),
                PartPose.offset(0F, -7.8F, -0.6F));
        PartDefinition p_crown_0 = p_crown.addOrReplaceChild("crown_0", CubeListBuilder.create()
                .texOffs(261, 99).addBox(-0.5F, -3.5F, -0.5F, 1F, 4F, 1F, new CubeDeformation(-0.05F, -0.5F, -0.05F)),
                PartPose.offsetAndRotation(-3.4931F, -0.6F, -0.1452F, 0F, 0F, -1.3265F));
        PartDefinition p_crown_1 = p_crown.addOrReplaceChild("crown_1", CubeListBuilder.create()
                .texOffs(266, 99).addBox(-0.5F, -3.925F, -0.5F, 1F, 4F, 1F, new CubeDeformation(-0.05F, -0.075F, -0.05F)),
                PartPose.offsetAndRotation(-3.0192F, -0.6F, -0.3268F, 0F, 0F, -0.9948F));
        PartDefinition p_crown_2 = p_crown.addOrReplaceChild("crown_2", CubeListBuilder.create()
                .texOffs(198, 79).addBox(-0.5F, -4.85F, -0.5F, 1F, 5F, 1F, new CubeDeformation(-0.05F, -0.15F, -0.05F)),
                PartPose.offsetAndRotation(-2.2164F, -0.6F, -0.4728F, 0F, 0F, -0.6632F));
        PartDefinition p_crown_3 = p_crown.addOrReplaceChild("crown_3", CubeListBuilder.create()
                .texOffs(21, 79).addBox(-0.5F, -5.775F, -0.5F, 1F, 6F, 1F, new CubeDeformation(-0.05F, -0.225F, -0.05F)),
                PartPose.offsetAndRotation(-1.172F, -0.6F, -0.5673F, 0F, 0F, -0.3316F));
        PartDefinition p_crown_4 = p_crown.addOrReplaceChild("crown_4", CubeListBuilder.create()
                .texOffs(228, 69).addBox(-0.5F, -6.7F, -0.5F, 1F, 7F, 1F, new CubeDeformation(-0.05F, -0.3F, -0.05F)),
                PartPose.offset(0F, -0.6F, -0.6F));
        PartDefinition p_crown_5 = p_crown.addOrReplaceChild("crown_5", CubeListBuilder.create()
                .texOffs(26, 79).addBox(-0.5F, -5.775F, -0.5F, 1F, 6F, 1F, new CubeDeformation(-0.05F, -0.225F, -0.05F)),
                PartPose.offsetAndRotation(1.172F, -0.6F, -0.5673F, 0F, 0F, 0.3316F));
        PartDefinition p_crown_6 = p_crown.addOrReplaceChild("crown_6", CubeListBuilder.create()
                .texOffs(203, 79).addBox(-0.5F, -4.85F, -0.5F, 1F, 5F, 1F, new CubeDeformation(-0.05F, -0.15F, -0.05F)),
                PartPose.offsetAndRotation(2.2164F, -0.6F, -0.4728F, 0F, 0F, 0.6632F));
        PartDefinition p_crown_7 = p_crown.addOrReplaceChild("crown_7", CubeListBuilder.create()
                .texOffs(271, 99).addBox(-0.5F, -3.925F, -0.5F, 1F, 4F, 1F, new CubeDeformation(-0.05F, -0.075F, -0.05F)),
                PartPose.offsetAndRotation(3.0192F, -0.6F, -0.3268F, 0F, 0F, 0.9948F));
        PartDefinition p_crown_8 = p_crown.addOrReplaceChild("crown_8", CubeListBuilder.create()
                .texOffs(276, 99).addBox(-0.5F, -3.5F, -0.5F, 1F, 4F, 1F, new CubeDeformation(-0.05F, -0.5F, -0.05F)),
                PartPose.offsetAndRotation(3.4931F, -0.6F, -0.1452F, 0F, 0F, 1.3265F));
        PartDefinition p_halo = p_head.addOrReplaceChild("halo", CubeListBuilder.create(),
                PartPose.offset(0F, -5F, 7F));
        PartDefinition p_halo_0 = p_halo.addOrReplaceChild("halo_0", CubeListBuilder.create()
                .texOffs(228, 110).addBox(-2.5F, -1F, -1F, 5F, 2F, 2F, new CubeDeformation(-0.1883F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(10.5F, 0F, 0F, 0F, 0F, 1.5708F));
        PartDefinition p_halo_1 = p_halo.addOrReplaceChild("halo_1", CubeListBuilder.create()
                .texOffs(243, 110).addBox(-2.5F, -1F, -1F, 5F, 2F, 2F, new CubeDeformation(-0.1883F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(9.7007F, 4.0182F, 0F, 0F, 0F, 1.9635F));
        PartDefinition p_halo_2 = p_halo.addOrReplaceChild("halo_2", CubeListBuilder.create()
                .texOffs(258, 110).addBox(-2.5F, -1F, -1F, 5F, 2F, 2F, new CubeDeformation(-0.1883F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(7.4246F, 7.4246F, 0F, 0F, 0F, 2.3562F));
        PartDefinition p_halo_3 = p_halo.addOrReplaceChild("halo_3", CubeListBuilder.create()
                .texOffs(273, 110).addBox(-2.5F, -1F, -1F, 5F, 2F, 2F, new CubeDeformation(-0.1883F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(4.0182F, 9.7007F, 0F, 0F, 0F, 2.7489F));
        PartDefinition p_halo_4 = p_halo.addOrReplaceChild("halo_4", CubeListBuilder.create()
                .texOffs(288, 110).addBox(-2.5F, -1F, -1F, 5F, 2F, 2F, new CubeDeformation(-0.1883F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(0F, 10.5F, 0F, 0F, 0F, 3.1416F));
        PartDefinition p_halo_5 = p_halo.addOrReplaceChild("halo_5", CubeListBuilder.create()
                .texOffs(303, 110).addBox(-2.5F, -1F, -1F, 5F, 2F, 2F, new CubeDeformation(-0.1883F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(-4.0182F, 9.7007F, 0F, 0F, 0F, 3.5343F));
        PartDefinition p_halo_6 = p_halo.addOrReplaceChild("halo_6", CubeListBuilder.create()
                .texOffs(0, 115).addBox(-2.5F, -1F, -1F, 5F, 2F, 2F, new CubeDeformation(-0.1883F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(-7.4246F, 7.4246F, 0F, 0F, 0F, 3.927F));
        PartDefinition p_halo_7 = p_halo.addOrReplaceChild("halo_7", CubeListBuilder.create()
                .texOffs(15, 115).addBox(-2.5F, -1F, -1F, 5F, 2F, 2F, new CubeDeformation(-0.1883F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(-9.7007F, 4.0182F, 0F, 0F, 0F, 4.3197F));
        PartDefinition p_halo_8 = p_halo.addOrReplaceChild("halo_8", CubeListBuilder.create()
                .texOffs(30, 115).addBox(-2.5F, -1F, -1F, 5F, 2F, 2F, new CubeDeformation(-0.1883F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(-10.5F, 0F, 0F, 0F, 0F, 4.7124F));
        PartDefinition p_halo_9 = p_halo.addOrReplaceChild("halo_9", CubeListBuilder.create()
                .texOffs(45, 115).addBox(-2.5F, -1F, -1F, 5F, 2F, 2F, new CubeDeformation(-0.1883F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(-9.7007F, -4.0182F, 0F, 0F, 0F, 5.1051F));
        PartDefinition p_halo_10 = p_halo.addOrReplaceChild("halo_10", CubeListBuilder.create()
                .texOffs(60, 115).addBox(-2.5F, -1F, -1F, 5F, 2F, 2F, new CubeDeformation(-0.1883F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(-7.4246F, -7.4246F, 0F, 0F, 0F, 5.4978F));
        PartDefinition p_halo_11 = p_halo.addOrReplaceChild("halo_11", CubeListBuilder.create()
                .texOffs(75, 115).addBox(-2.5F, -1F, -1F, 5F, 2F, 2F, new CubeDeformation(-0.1883F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(-4.0182F, -9.7007F, 0F, 0F, 0F, 5.8905F));
        PartDefinition p_halo_12 = p_halo.addOrReplaceChild("halo_12", CubeListBuilder.create()
                .texOffs(90, 115).addBox(-2.5F, -1F, -1F, 5F, 2F, 2F, new CubeDeformation(-0.1883F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(0F, -10.5F, 0F, 0F, 0F, 6.2832F));
        PartDefinition p_halo_13 = p_halo.addOrReplaceChild("halo_13", CubeListBuilder.create()
                .texOffs(105, 115).addBox(-2.5F, -1F, -1F, 5F, 2F, 2F, new CubeDeformation(-0.1883F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(4.0182F, -9.7007F, 0F, 0F, 0F, 6.6759F));
        PartDefinition p_halo_14 = p_halo.addOrReplaceChild("halo_14", CubeListBuilder.create()
                .texOffs(120, 115).addBox(-2.5F, -1F, -1F, 5F, 2F, 2F, new CubeDeformation(-0.1883F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(7.4246F, -7.4246F, 0F, 0F, 0F, 7.0686F));
        PartDefinition p_halo_15 = p_halo.addOrReplaceChild("halo_15", CubeListBuilder.create()
                .texOffs(135, 115).addBox(-2.5F, -1F, -1F, 5F, 2F, 2F, new CubeDeformation(-0.1883F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(9.7007F, -4.0182F, 0F, 0F, 0F, 7.4613F));
        PartDefinition p_halo_ray_0 = p_halo.addOrReplaceChild("halo_ray_0", CubeListBuilder.create()
                .texOffs(208, 79).addBox(-0.5F, -0.4F, -0.5F, 1F, 5F, 1F, new CubeDeformation(0F, -0.4F, -0.15F)),
                PartPose.offsetAndRotation(11.0116F, 2.9505F, 0.2F, 0F, 0F, -1.309F));
        PartDefinition p_halo_ray_1 = p_halo.addOrReplaceChild("halo_ray_1", CubeListBuilder.create()
                .texOffs(264, 115).addBox(-0.5F, -0.2F, -0.5F, 1F, 3F, 1F, new CubeDeformation(0F, -0.2F, -0.15F)),
                PartPose.offsetAndRotation(8.061F, 8.061F, 0.2F, 0F, 0F, -0.7854F));
        PartDefinition p_halo_ray_2 = p_halo.addOrReplaceChild("halo_ray_2", CubeListBuilder.create()
                .texOffs(213, 79).addBox(-0.5F, -0.4F, -0.5F, 1F, 5F, 1F, new CubeDeformation(0F, -0.4F, -0.15F)),
                PartPose.offsetAndRotation(2.9505F, 11.0116F, 0.2F, 0F, 0F, -0.2618F));
        PartDefinition p_halo_ray_3 = p_halo.addOrReplaceChild("halo_ray_3", CubeListBuilder.create()
                .texOffs(269, 115).addBox(-0.5F, -0.2F, -0.5F, 1F, 3F, 1F, new CubeDeformation(0F, -0.2F, -0.15F)),
                PartPose.offsetAndRotation(-2.9505F, 11.0116F, 0.2F, 0F, 0F, 0.2618F));
        PartDefinition p_halo_ray_4 = p_halo.addOrReplaceChild("halo_ray_4", CubeListBuilder.create()
                .texOffs(218, 79).addBox(-0.5F, -0.4F, -0.5F, 1F, 5F, 1F, new CubeDeformation(0F, -0.4F, -0.15F)),
                PartPose.offsetAndRotation(-8.061F, 8.061F, 0.2F, 0F, 0F, 0.7854F));
        PartDefinition p_halo_ray_5 = p_halo.addOrReplaceChild("halo_ray_5", CubeListBuilder.create()
                .texOffs(274, 115).addBox(-0.5F, -0.2F, -0.5F, 1F, 3F, 1F, new CubeDeformation(0F, -0.2F, -0.15F)),
                PartPose.offsetAndRotation(-11.0116F, 2.9505F, 0.2F, 0F, 0F, 1.309F));
        PartDefinition p_halo_ray_6 = p_halo.addOrReplaceChild("halo_ray_6", CubeListBuilder.create()
                .texOffs(223, 79).addBox(-0.5F, -0.4F, -0.5F, 1F, 5F, 1F, new CubeDeformation(0F, -0.4F, -0.15F)),
                PartPose.offsetAndRotation(-11.0116F, -2.9505F, 0.2F, 0F, 0F, 1.8326F));
        PartDefinition p_halo_ray_7 = p_halo.addOrReplaceChild("halo_ray_7", CubeListBuilder.create()
                .texOffs(279, 115).addBox(-0.5F, -0.2F, -0.5F, 1F, 3F, 1F, new CubeDeformation(0F, -0.2F, -0.15F)),
                PartPose.offsetAndRotation(-8.061F, -8.061F, 0.2F, 0F, 0F, 2.3562F));
        PartDefinition p_halo_ray_8 = p_halo.addOrReplaceChild("halo_ray_8", CubeListBuilder.create()
                .texOffs(228, 79).addBox(-0.5F, -0.4F, -0.5F, 1F, 5F, 1F, new CubeDeformation(0F, -0.4F, -0.15F)),
                PartPose.offsetAndRotation(-2.9505F, -11.0116F, 0.2F, 0F, 0F, 2.8798F));
        PartDefinition p_halo_ray_9 = p_halo.addOrReplaceChild("halo_ray_9", CubeListBuilder.create()
                .texOffs(284, 115).addBox(-0.5F, -0.2F, -0.5F, 1F, 3F, 1F, new CubeDeformation(0F, -0.2F, -0.15F)),
                PartPose.offsetAndRotation(2.9505F, -11.0116F, 0.2F, 0F, 0F, 3.4034F));
        PartDefinition p_halo_ray_10 = p_halo.addOrReplaceChild("halo_ray_10", CubeListBuilder.create()
                .texOffs(233, 79).addBox(-0.5F, -0.4F, -0.5F, 1F, 5F, 1F, new CubeDeformation(0F, -0.4F, -0.15F)),
                PartPose.offsetAndRotation(8.061F, -8.061F, 0.2F, 0F, 0F, 3.927F));
        PartDefinition p_halo_ray_11 = p_halo.addOrReplaceChild("halo_ray_11", CubeListBuilder.create()
                .texOffs(289, 115).addBox(-0.5F, -0.2F, -0.5F, 1F, 3F, 1F, new CubeDeformation(0F, -0.2F, -0.15F)),
                PartPose.offsetAndRotation(11.0116F, -2.9505F, 0.2F, 0F, 0F, 4.4506F));
        PartDefinition p_halo_inner = p_halo.addOrReplaceChild("halo_inner", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0.4F));
        PartDefinition p_halo_inner_0 = p_halo_inner.addOrReplaceChild("halo_inner_0", CubeListBuilder.create()
                .texOffs(111, 120).addBox(-2F, -0.5F, -0.5F, 4F, 1F, 1F, new CubeDeformation(-0.0721F, -0.1F, -0.2F)),
                PartPose.offsetAndRotation(6.6F, 0F, 0F, 0F, 0F, 1.5708F));
        PartDefinition p_halo_inner_1 = p_halo_inner.addOrReplaceChild("halo_inner_1", CubeListBuilder.create()
                .texOffs(122, 120).addBox(-2F, -0.5F, -0.5F, 4F, 1F, 1F, new CubeDeformation(-0.0721F, -0.1F, -0.2F)),
                PartPose.offsetAndRotation(5.7158F, 3.3F, 0F, 0F, 0F, 2.0944F));
        PartDefinition p_halo_inner_2 = p_halo_inner.addOrReplaceChild("halo_inner_2", CubeListBuilder.create()
                .texOffs(133, 120).addBox(-2F, -0.5F, -0.5F, 4F, 1F, 1F, new CubeDeformation(-0.0721F, -0.1F, -0.2F)),
                PartPose.offsetAndRotation(3.3F, 5.7158F, 0F, 0F, 0F, 2.618F));
        PartDefinition p_halo_inner_3 = p_halo_inner.addOrReplaceChild("halo_inner_3", CubeListBuilder.create()
                .texOffs(144, 120).addBox(-2F, -0.5F, -0.5F, 4F, 1F, 1F, new CubeDeformation(-0.0721F, -0.1F, -0.2F)),
                PartPose.offsetAndRotation(0F, 6.6F, 0F, 0F, 0F, 3.1416F));
        PartDefinition p_halo_inner_4 = p_halo_inner.addOrReplaceChild("halo_inner_4", CubeListBuilder.create()
                .texOffs(155, 120).addBox(-2F, -0.5F, -0.5F, 4F, 1F, 1F, new CubeDeformation(-0.0721F, -0.1F, -0.2F)),
                PartPose.offsetAndRotation(-3.3F, 5.7158F, 0F, 0F, 0F, 3.6652F));
        PartDefinition p_halo_inner_5 = p_halo_inner.addOrReplaceChild("halo_inner_5", CubeListBuilder.create()
                .texOffs(166, 120).addBox(-2F, -0.5F, -0.5F, 4F, 1F, 1F, new CubeDeformation(-0.0721F, -0.1F, -0.2F)),
                PartPose.offsetAndRotation(-5.7158F, 3.3F, 0F, 0F, 0F, 4.1888F));
        PartDefinition p_halo_inner_6 = p_halo_inner.addOrReplaceChild("halo_inner_6", CubeListBuilder.create()
                .texOffs(177, 120).addBox(-2F, -0.5F, -0.5F, 4F, 1F, 1F, new CubeDeformation(-0.0721F, -0.1F, -0.2F)),
                PartPose.offsetAndRotation(-6.6F, 0F, 0F, 0F, 0F, 4.7124F));
        PartDefinition p_halo_inner_7 = p_halo_inner.addOrReplaceChild("halo_inner_7", CubeListBuilder.create()
                .texOffs(188, 120).addBox(-2F, -0.5F, -0.5F, 4F, 1F, 1F, new CubeDeformation(-0.0721F, -0.1F, -0.2F)),
                PartPose.offsetAndRotation(-5.7158F, -3.3F, 0F, 0F, 0F, 5.236F));
        PartDefinition p_halo_inner_8 = p_halo_inner.addOrReplaceChild("halo_inner_8", CubeListBuilder.create()
                .texOffs(199, 120).addBox(-2F, -0.5F, -0.5F, 4F, 1F, 1F, new CubeDeformation(-0.0721F, -0.1F, -0.2F)),
                PartPose.offsetAndRotation(-3.3F, -5.7158F, 0F, 0F, 0F, 5.7596F));
        PartDefinition p_halo_inner_9 = p_halo_inner.addOrReplaceChild("halo_inner_9", CubeListBuilder.create()
                .texOffs(210, 120).addBox(-2F, -0.5F, -0.5F, 4F, 1F, 1F, new CubeDeformation(-0.0721F, -0.1F, -0.2F)),
                PartPose.offsetAndRotation(0F, -6.6F, 0F, 0F, 0F, 6.2832F));
        PartDefinition p_halo_inner_10 = p_halo_inner.addOrReplaceChild("halo_inner_10", CubeListBuilder.create()
                .texOffs(221, 120).addBox(-2F, -0.5F, -0.5F, 4F, 1F, 1F, new CubeDeformation(-0.0721F, -0.1F, -0.2F)),
                PartPose.offsetAndRotation(3.3F, -5.7158F, 0F, 0F, 0F, 6.8068F));
        PartDefinition p_halo_inner_11 = p_halo_inner.addOrReplaceChild("halo_inner_11", CubeListBuilder.create()
                .texOffs(232, 120).addBox(-2F, -0.5F, -0.5F, 4F, 1F, 1F, new CubeDeformation(-0.0721F, -0.1F, -0.2F)),
                PartPose.offsetAndRotation(5.7158F, -3.3F, 0F, 0F, 0F, 7.3304F));
        PartDefinition p_true_corona = p_halo.addOrReplaceChild("true_corona", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0.9F));
        PartDefinition p_true_corona_0 = p_true_corona.addOrReplaceChild("true_corona_0", CubeListBuilder.create()
                .texOffs(205, 58).addBox(-1F, -0.25F, 0F, 2F, 10F, 0F, new CubeDeformation(-0.1F, -0.25F, 0F)),
                PartPose.offsetAndRotation(13.4837F, 1.7752F, 0F, 0F, 0F, -1.4399F));
        PartDefinition p_true_corona_1 = p_true_corona.addOrReplaceChild("true_corona_1", CubeListBuilder.create()
                .texOffs(238, 79).addBox(-1F, -0.25F, 0F, 2F, 6F, 0F, new CubeDeformation(-0.1F, -0.25F, 0F)),
                PartPose.offsetAndRotation(12.5648F, 5.2045F, 0F, 0F, 0F, -1.1781F));
        PartDefinition p_true_corona_2 = p_true_corona.addOrReplaceChild("true_corona_2", CubeListBuilder.create()
                .texOffs(210, 58).addBox(-1F, -0.25F, 0F, 2F, 10F, 0F, new CubeDeformation(-0.1F, -0.25F, 0F)),
                PartPose.offsetAndRotation(10.7896F, 8.2792F, 0F, 0F, 0F, -0.9163F));
        PartDefinition p_true_corona_3 = p_true_corona.addOrReplaceChild("true_corona_3", CubeListBuilder.create()
                .texOffs(243, 79).addBox(-1F, -0.25F, 0F, 2F, 6F, 0F, new CubeDeformation(-0.1F, -0.25F, 0F)),
                PartPose.offsetAndRotation(8.2792F, 10.7896F, 0F, 0F, 0F, -0.6545F));
        PartDefinition p_true_corona_4 = p_true_corona.addOrReplaceChild("true_corona_4", CubeListBuilder.create()
                .texOffs(215, 58).addBox(-1F, -0.25F, 0F, 2F, 10F, 0F, new CubeDeformation(-0.1F, -0.25F, 0F)),
                PartPose.offsetAndRotation(5.2045F, 12.5648F, 0F, 0F, 0F, -0.3927F));
        PartDefinition p_true_corona_5 = p_true_corona.addOrReplaceChild("true_corona_5", CubeListBuilder.create()
                .texOffs(248, 79).addBox(-1F, -0.25F, 0F, 2F, 6F, 0F, new CubeDeformation(-0.1F, -0.25F, 0F)),
                PartPose.offsetAndRotation(1.7752F, 13.4837F, 0F, 0F, 0F, -0.1309F));
        PartDefinition p_true_corona_6 = p_true_corona.addOrReplaceChild("true_corona_6", CubeListBuilder.create()
                .texOffs(220, 58).addBox(-1F, -0.25F, 0F, 2F, 10F, 0F, new CubeDeformation(-0.1F, -0.25F, 0F)),
                PartPose.offsetAndRotation(-1.7752F, 13.4837F, 0F, 0F, 0F, 0.1309F));
        PartDefinition p_true_corona_7 = p_true_corona.addOrReplaceChild("true_corona_7", CubeListBuilder.create()
                .texOffs(253, 79).addBox(-1F, -0.25F, 0F, 2F, 6F, 0F, new CubeDeformation(-0.1F, -0.25F, 0F)),
                PartPose.offsetAndRotation(-5.2045F, 12.5648F, 0F, 0F, 0F, 0.3927F));
        PartDefinition p_true_corona_8 = p_true_corona.addOrReplaceChild("true_corona_8", CubeListBuilder.create()
                .texOffs(225, 58).addBox(-1F, -0.25F, 0F, 2F, 10F, 0F, new CubeDeformation(-0.1F, -0.25F, 0F)),
                PartPose.offsetAndRotation(-8.2792F, 10.7896F, 0F, 0F, 0F, 0.6545F));
        PartDefinition p_true_corona_9 = p_true_corona.addOrReplaceChild("true_corona_9", CubeListBuilder.create()
                .texOffs(258, 79).addBox(-1F, -0.25F, 0F, 2F, 6F, 0F, new CubeDeformation(-0.1F, -0.25F, 0F)),
                PartPose.offsetAndRotation(-10.7896F, 8.2792F, 0F, 0F, 0F, 0.9163F));
        PartDefinition p_true_corona_10 = p_true_corona.addOrReplaceChild("true_corona_10", CubeListBuilder.create()
                .texOffs(230, 58).addBox(-1F, -0.25F, 0F, 2F, 10F, 0F, new CubeDeformation(-0.1F, -0.25F, 0F)),
                PartPose.offsetAndRotation(-12.5648F, 5.2045F, 0F, 0F, 0F, 1.1781F));
        PartDefinition p_true_corona_11 = p_true_corona.addOrReplaceChild("true_corona_11", CubeListBuilder.create()
                .texOffs(263, 79).addBox(-1F, -0.25F, 0F, 2F, 6F, 0F, new CubeDeformation(-0.1F, -0.25F, 0F)),
                PartPose.offsetAndRotation(-13.4837F, 1.7752F, 0F, 0F, 0F, 1.4399F));
        PartDefinition p_true_corona_12 = p_true_corona.addOrReplaceChild("true_corona_12", CubeListBuilder.create()
                .texOffs(235, 58).addBox(-1F, -0.25F, 0F, 2F, 10F, 0F, new CubeDeformation(-0.1F, -0.25F, 0F)),
                PartPose.offsetAndRotation(-13.4837F, -1.7752F, 0F, 0F, 0F, 1.7017F));
        PartDefinition p_true_corona_13 = p_true_corona.addOrReplaceChild("true_corona_13", CubeListBuilder.create()
                .texOffs(268, 79).addBox(-1F, -0.25F, 0F, 2F, 6F, 0F, new CubeDeformation(-0.1F, -0.25F, 0F)),
                PartPose.offsetAndRotation(-12.5648F, -5.2045F, 0F, 0F, 0F, 1.9635F));
        PartDefinition p_true_corona_14 = p_true_corona.addOrReplaceChild("true_corona_14", CubeListBuilder.create()
                .texOffs(240, 58).addBox(-1F, -0.25F, 0F, 2F, 10F, 0F, new CubeDeformation(-0.1F, -0.25F, 0F)),
                PartPose.offsetAndRotation(-10.7896F, -8.2792F, 0F, 0F, 0F, 2.2253F));
        PartDefinition p_true_corona_15 = p_true_corona.addOrReplaceChild("true_corona_15", CubeListBuilder.create()
                .texOffs(273, 79).addBox(-1F, -0.25F, 0F, 2F, 6F, 0F, new CubeDeformation(-0.1F, -0.25F, 0F)),
                PartPose.offsetAndRotation(-8.2792F, -10.7896F, 0F, 0F, 0F, 2.4871F));
        PartDefinition p_true_corona_16 = p_true_corona.addOrReplaceChild("true_corona_16", CubeListBuilder.create()
                .texOffs(245, 58).addBox(-1F, -0.25F, 0F, 2F, 10F, 0F, new CubeDeformation(-0.1F, -0.25F, 0F)),
                PartPose.offsetAndRotation(-5.2045F, -12.5648F, 0F, 0F, 0F, 2.7489F));
        PartDefinition p_true_corona_17 = p_true_corona.addOrReplaceChild("true_corona_17", CubeListBuilder.create()
                .texOffs(278, 79).addBox(-1F, -0.25F, 0F, 2F, 6F, 0F, new CubeDeformation(-0.1F, -0.25F, 0F)),
                PartPose.offsetAndRotation(-1.7752F, -13.4837F, 0F, 0F, 0F, 3.0107F));
        PartDefinition p_true_corona_18 = p_true_corona.addOrReplaceChild("true_corona_18", CubeListBuilder.create()
                .texOffs(250, 58).addBox(-1F, -0.25F, 0F, 2F, 10F, 0F, new CubeDeformation(-0.1F, -0.25F, 0F)),
                PartPose.offsetAndRotation(1.7752F, -13.4837F, 0F, 0F, 0F, 3.2725F));
        PartDefinition p_true_corona_19 = p_true_corona.addOrReplaceChild("true_corona_19", CubeListBuilder.create()
                .texOffs(283, 79).addBox(-1F, -0.25F, 0F, 2F, 6F, 0F, new CubeDeformation(-0.1F, -0.25F, 0F)),
                PartPose.offsetAndRotation(5.2045F, -12.5648F, 0F, 0F, 0F, 3.5343F));
        PartDefinition p_true_corona_20 = p_true_corona.addOrReplaceChild("true_corona_20", CubeListBuilder.create()
                .texOffs(255, 58).addBox(-1F, -0.25F, 0F, 2F, 10F, 0F, new CubeDeformation(-0.1F, -0.25F, 0F)),
                PartPose.offsetAndRotation(8.2792F, -10.7896F, 0F, 0F, 0F, 3.7961F));
        PartDefinition p_true_corona_21 = p_true_corona.addOrReplaceChild("true_corona_21", CubeListBuilder.create()
                .texOffs(288, 79).addBox(-1F, -0.25F, 0F, 2F, 6F, 0F, new CubeDeformation(-0.1F, -0.25F, 0F)),
                PartPose.offsetAndRotation(10.7896F, -8.2792F, 0F, 0F, 0F, 4.0579F));
        PartDefinition p_true_corona_22 = p_true_corona.addOrReplaceChild("true_corona_22", CubeListBuilder.create()
                .texOffs(260, 58).addBox(-1F, -0.25F, 0F, 2F, 10F, 0F, new CubeDeformation(-0.1F, -0.25F, 0F)),
                PartPose.offsetAndRotation(12.5648F, -5.2045F, 0F, 0F, 0F, 4.3197F));
        PartDefinition p_true_corona_23 = p_true_corona.addOrReplaceChild("true_corona_23", CubeListBuilder.create()
                .texOffs(293, 79).addBox(-1F, -0.25F, 0F, 2F, 6F, 0F, new CubeDeformation(-0.1F, -0.25F, 0F)),
                PartPose.offsetAndRotation(13.4837F, -1.7752F, 0F, 0F, 0F, 4.5815F));
        PartDefinition p_left_arm = p_body.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(124, 29).addBox(-2.5F, -0.1F, -2.5F, 5F, 9F, 5F, new CubeDeformation(-0.2F, -0.3F, -0.1F))
                .texOffs(0, 58).addBox(-3F, -2.6F, -3F, 6F, 4F, 6F, new CubeDeformation(-0.1F, -0.2F, 0F))
                .texOffs(31, 79).addBox(-3.5F, -3.1F, -2.5F, 7F, 1F, 5F, new CubeDeformation(-0.4F, 0F, -0.3F)),
                PartPose.offsetAndRotation(7.2F, -14.4F, 0F, -0.3142F, 0F, -0.1396F));
        PartDefinition p_left_forearm = p_left_arm.addOrReplaceChild("left_forearm", CubeListBuilder.create()
                .texOffs(51, 69).addBox(-2.5F, -0.1F, -2.5F, 5F, 3F, 5F, new CubeDeformation(-0.3F, -0.1F, -0.3F))
                .texOffs(114, 69).addBox(-2F, 2.3F, -2F, 4F, 4F, 4F, new CubeDeformation(-0.1F, -0.3F, -0.1F))
                .texOffs(131, 69).addBox(-2F, 5.6F, -2F, 4F, 4F, 4F, new CubeDeformation(-0.4F, -0.4F, -0.3F)),
                PartPose.offsetAndRotation(0F, 8.4F, 0F, -1.0123F, 0F, 0F));
        PartDefinition p_orb = p_left_forearm.addOrReplaceChild("orb", CubeListBuilder.create()
                .texOffs(100, 58).addBox(-2.5F, -2.5F, -2.5F, 5F, 5F, 5F, new CubeDeformation(-0.1F, -0.1F, -0.1F))
                .texOffs(142, 58).addBox(-2F, -3F, -2F, 4F, 6F, 4F, new CubeDeformation(-0.2F, -0.1F, -0.2F))
                .texOffs(72, 69).addBox(-3F, -2F, -2F, 6F, 4F, 4F, new CubeDeformation(-0.1F, -0.2F, -0.2F))
                .texOffs(121, 58).addBox(-2F, -2F, -3F, 4F, 4F, 6F, new CubeDeformation(-0.2F, -0.2F, -0.1F))
                .texOffs(233, 69).addBox(-3F, -0.5F, -3F, 6F, 1F, 6F, new CubeDeformation(0F, -0.1F, 0F))
                .texOffs(294, 115).addBox(-0.5F, -5.4F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.15F, -0.4F, -0.15F))
                .texOffs(254, 120).addBox(-1F, -4.6F, -0.5F, 2F, 1F, 1F, new CubeDeformation(0F, -0.2F, -0.15F)),
                PartPose.offsetAndRotation(0F, 10.4F, -1.2F, 1.3265F, 0F, 0.1396F));
        PartDefinition p_right_arm = p_body.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(145, 29).addBox(-2.5F, -0.1F, -2.5F, 5F, 9F, 5F, new CubeDeformation(-0.2F, -0.3F, -0.1F))
                .texOffs(25, 58).addBox(-3F, -2.6F, -3F, 6F, 4F, 6F, new CubeDeformation(-0.1F, -0.2F, 0F))
                .texOffs(56, 79).addBox(-3.5F, -3.1F, -2.5F, 7F, 1F, 5F, new CubeDeformation(-0.4F, 0F, -0.3F)),
                PartPose.offsetAndRotation(-7.2F, -14.4F, 0F, -0.4538F, 0F, 0.1745F));
        PartDefinition p_right_forearm = p_right_arm.addOrReplaceChild("right_forearm", CubeListBuilder.create()
                .texOffs(93, 69).addBox(-2.5F, -0.1F, -2.5F, 5F, 3F, 5F, new CubeDeformation(-0.3F, -0.1F, -0.3F))
                .texOffs(148, 69).addBox(-2F, 2.3F, -2F, 4F, 4F, 4F, new CubeDeformation(-0.1F, -0.3F, -0.1F))
                .texOffs(165, 69).addBox(-2F, 5.6F, -2F, 4F, 4F, 4F, new CubeDeformation(-0.4F, -0.4F, -0.3F)),
                PartPose.offsetAndRotation(0F, 8.4F, 0F, -0.5934F, 0F, 0F));
        PartDefinition p_scepter = p_right_forearm.addOrReplaceChild("scepter", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-1F, -15F, -1F, 2F, 26F, 2F, new CubeDeformation(-0.4F, 0F, -0.4F))
                .texOffs(241, 115).addBox(-1F, 9.8F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.1F, -0.2F, -0.1F)),
                PartPose.offsetAndRotation(0F, 7.6F, -0.4F, 1.0472F, 0F, -0.1745F));
        PartDefinition p_scepter_head = p_scepter.addOrReplaceChild("scepter_head", CubeListBuilder.create()
                .texOffs(126, 79).addBox(-1.5F, -1.5F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.1F, -0.1F, -0.1F))
                .texOffs(243, 120).addBox(-2F, 1.3F, -0.5F, 4F, 1F, 1F, new CubeDeformation(0F, -0.1F, 0F)),
                PartPose.offset(0F, -16.8F, 0F));
        PartDefinition p_scepter_star_0 = p_scepter_head.addOrReplaceChild("scepter_star_0", CubeListBuilder.create()
                .texOffs(43, 120).addBox(-4F, -0.5F, -0.5F, 8F, 1F, 1F, new CubeDeformation(0F, -0.05F, -0.15F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_scepter_star_1 = p_scepter_head.addOrReplaceChild("scepter_star_1", CubeListBuilder.create()
                .texOffs(81, 120).addBox(-3F, -0.5F, -0.5F, 6F, 1F, 1F, new CubeDeformation(-0.2F, -0.05F, -0.15F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0.7854F));
        PartDefinition p_scepter_star_2 = p_scepter_head.addOrReplaceChild("scepter_star_2", CubeListBuilder.create()
                .texOffs(62, 120).addBox(-4F, -0.5F, -0.5F, 8F, 1F, 1F, new CubeDeformation(0F, -0.05F, -0.15F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 1.5708F));
        PartDefinition p_scepter_star_3 = p_scepter_head.addOrReplaceChild("scepter_star_3", CubeListBuilder.create()
                .texOffs(96, 120).addBox(-3F, -0.5F, -0.5F, 6F, 1F, 1F, new CubeDeformation(-0.2F, -0.05F, -0.15F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 2.3562F));
        PartDefinition p_blade = p_right_forearm.addOrReplaceChild("blade", CubeListBuilder.create()
                .texOffs(0, 69).addBox(-1F, -2.9F, -1F, 2F, 7F, 2F, new CubeDeformation(-0.25F, -0.3F, -0.25F))
                .texOffs(139, 79).addBox(-1.5F, 3.5F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.3F, -0.3F, -0.3F))
                .texOffs(281, 99).addBox(-5.5F, -4.3F, -1F, 11F, 2F, 2F, new CubeDeformation(-0.1F, -0.3F, -0.1F))
                .texOffs(81, 79).addBox(-2F, -4.9F, -1.5F, 4F, 3F, 3F, new CubeDeformation(-0.3F, -0.1F, -0.35F)),
                PartPose.offsetAndRotation(0F, 7.6F, -0.4F, 1.0472F, 0F, -0.1745F));
        PartDefinition p_blade_guard_r = p_blade.addOrReplaceChild("blade_guard_r", CubeListBuilder.create()
                .texOffs(297, 69).addBox(-0.5F, -4.8F, -1F, 1F, 5F, 2F, new CubeDeformation(0F, -0.2F, -0.45F)),
                PartPose.offsetAndRotation(-5F, -3.4F, 0F, 0F, 0F, 0.6632F));
        PartDefinition p_blade_guard_l = p_blade.addOrReplaceChild("blade_guard_l", CubeListBuilder.create()
                .texOffs(304, 69).addBox(-0.5F, -4.8F, -1F, 1F, 5F, 2F, new CubeDeformation(0F, -0.2F, -0.45F)),
                PartPose.offsetAndRotation(5F, -3.4F, 0F, 0F, 0F, -0.6632F));
        PartDefinition p_blade_edge = p_blade.addOrReplaceChild("blade_edge", CubeListBuilder.create()
                .texOffs(9, 0).addBox(-2F, -26F, -0.5F, 4F, 26F, 1F, new CubeDeformation(-0.3F, 0F, -0.1F))
                .texOffs(20, 0).addBox(-0.5F, -25.2F, -0.5F, 1F, 25F, 1F, new CubeDeformation(-0.05F, -0.2F, 0F))
                .texOffs(15, 120).addBox(-1.5F, -28F, -0.5F, 3F, 2F, 1F, new CubeDeformation(-0.25F, 0F, -0.15F))
                .texOffs(31, 120).addBox(-1F, -29.85F, -0.5F, 2F, 2F, 1F, new CubeDeformation(-0.25F, -0.15F, -0.2F))
                .texOffs(38, 120).addBox(-0.5F, -31.35F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.2F, -0.35F, -0.25F)),
                PartPose.offset(0F, -4.8F, 0F));
        PartDefinition p_robe = p_body.addOrReplaceChild("robe", CubeListBuilder.create()
                .texOffs(48, 29).addBox(-6.5F, 0F, -4F, 13F, 6F, 8F, new CubeDeformation(-0.3F, 0F, -0.2F)),
                PartPose.offset(0F, 0.4F, 0F));
        PartDefinition p_robe_front = p_robe.addOrReplaceChild("robe_front", CubeListBuilder.create()
                .texOffs(255, 29).addBox(-7F, 0F, -1F, 14F, 10F, 2F, new CubeDeformation(0F, 0F, -0.4F)),
                PartPose.offsetAndRotation(0F, 0F, -4F, -0.1745F, 0F, 0F));
        PartDefinition p_robe_front_low = p_robe_front.addOrReplaceChild("robe_front_low", CubeListBuilder.create()
                .texOffs(194, 29).addBox(-7F, 0F, -1F, 14F, 11F, 2F, new CubeDeformation(0F, 0F, -0.4F)),
                PartPose.offset(0F, 10F, 0F));
        PartDefinition p_robe_back = p_robe.addOrReplaceChild("robe_back", CubeListBuilder.create()
                .texOffs(288, 29).addBox(-7F, 0F, -1F, 14F, 10F, 2F, new CubeDeformation(0F, 0F, -0.4F)),
                PartPose.offsetAndRotation(0F, 0F, 4F, 0.1571F, 0F, 0F));
        PartDefinition p_robe_back_low = p_robe_back.addOrReplaceChild("robe_back_low", CubeListBuilder.create()
                .texOffs(91, 29).addBox(-7F, 0F, -1F, 14F, 12F, 2F, new CubeDeformation(0F, 0F, -0.4F)),
                PartPose.offset(0F, 10F, 0F));
        PartDefinition p_robe_left = p_robe.addOrReplaceChild("robe_left", CubeListBuilder.create()
                .texOffs(62, 0).addBox(-1F, 0F, -5F, 2F, 10F, 10F, new CubeDeformation(-0.4F, 0F, -0.4F)),
                PartPose.offsetAndRotation(6.6F, 0F, 0F, 0F, 0F, -0.1745F));
        PartDefinition p_robe_left_low = p_robe_left.addOrReplaceChild("robe_left_low", CubeListBuilder.create()
                .texOffs(87, 0).addBox(-1F, 0F, -5F, 2F, 10F, 10F, new CubeDeformation(-0.4F, 0F, -0.4F)),
                PartPose.offset(0F, 10F, 0F));
        PartDefinition p_robe_right = p_robe.addOrReplaceChild("robe_right", CubeListBuilder.create()
                .texOffs(112, 0).addBox(-1F, 0F, -5F, 2F, 10F, 10F, new CubeDeformation(-0.4F, 0F, -0.4F)),
                PartPose.offsetAndRotation(-6.6F, 0F, 0F, 0F, 0F, 0.1745F));
        PartDefinition p_robe_right_low = p_robe_right.addOrReplaceChild("robe_right_low", CubeListBuilder.create()
                .texOffs(137, 0).addBox(-1F, 0F, -5F, 2F, 10F, 10F, new CubeDeformation(-0.4F, 0F, -0.4F)),
                PartPose.offset(0F, 10F, 0F));
        PartDefinition p_wing_upper_left = p_body.addOrReplaceChild("wing_upper_left", CubeListBuilder.create()
                .texOffs(221, 105).addBox(0F, -1F, -1F, 8F, 2F, 2F, new CubeDeformation(0F, 0F, -0.2F))
                .texOffs(194, 110).addBox(0F, 0.148F, -0.35F, 8F, 4F, 0F, new CubeDeformation(0F, -0.452F, 0F)),
                PartPose.offsetAndRotation(2.6F, -13F, 3F, -0.1047F, -0.2793F, -0.8378F));
        PartDefinition p_wing_upper_left_f0 = p_wing_upper_left.addOrReplaceChild("wing_upper_left_f0", CubeListBuilder.create()
                .texOffs(205, 99).addBox(-1.5F, -0.35F, 0F, 3F, 5F, 0F, new CubeDeformation(-0.038F, -0.35F, 0F)),
                PartPose.offset(0.96F, 0.7F, 0.05F));
        PartDefinition p_wing_upper_left_f1 = p_wing_upper_left.addOrReplaceChild("wing_upper_left_f1", CubeListBuilder.create()
                .texOffs(212, 99).addBox(-1.5F, -0.0275F, 0F, 3F, 5F, 0F, new CubeDeformation(-0.038F, -0.0275F, 0F)),
                PartPose.offsetAndRotation(4.16F, 0.7F, 0.17F, 0F, 0F, -0.0524F));
        PartDefinition p_wing_upper_left_f2 = p_wing_upper_left.addOrReplaceChild("wing_upper_left_f2", CubeListBuilder.create()
                .texOffs(170, 79).addBox(-1.5F, -0.205F, 0F, 3F, 6F, 0F, new CubeDeformation(-0.038F, -0.205F, 0F)),
                PartPose.offsetAndRotation(7.36F, 0.7F, 0.29F, 0F, 0F, -0.1047F));
        PartDefinition p_wing_upper_left_mid = p_wing_upper_left.addOrReplaceChild("wing_upper_left_mid", CubeListBuilder.create()
                .texOffs(154, 105).addBox(0F, -1F, -1F, 9F, 2F, 2F, new CubeDeformation(0F, 0F, -0.2F))
                .texOffs(42, 110).addBox(0F, 0.448F, -0.35F, 9F, 4F, 0F, new CubeDeformation(0F, -0.152F, 0F)),
                PartPose.offsetAndRotation(8F, 0F, 0F, 0F, -0.1047F, -0.1745F));
        PartDefinition p_wing_upper_left_mid_f0 = p_wing_upper_left_mid.addOrReplaceChild("wing_upper_left_mid_f0", CubeListBuilder.create()
                .texOffs(311, 69).addBox(-1.5F, -0.06F, 0F, 3F, 7F, 0F, new CubeDeformation(-0.124F, -0.06F, 0F)),
                PartPose.offsetAndRotation(1.08F, 0.7F, 0.05F, 0F, 0F, -0.1396F));
        PartDefinition p_wing_upper_left_mid_f1 = p_wing_upper_left_mid.addOrReplaceChild("wing_upper_left_mid_f1", CubeListBuilder.create()
                .texOffs(200, 69).addBox(-1.5F, -0.0583F, 0F, 3F, 8F, 0F, new CubeDeformation(-0.124F, -0.0583F, 0F)),
                PartPose.offsetAndRotation(3.48F, 0.7F, 0.17F, 0F, 0F, -0.1978F));
        PartDefinition p_wing_upper_left_mid_f2 = p_wing_upper_left_mid.addOrReplaceChild("wing_upper_left_mid_f2", CubeListBuilder.create()
                .texOffs(9, 69).addBox(-1.5F, -0.0567F, 0F, 3F, 9F, 0F, new CubeDeformation(-0.124F, -0.0567F, 0F)),
                PartPose.offsetAndRotation(5.88F, 0.7F, 0.29F, 0F, 0F, -0.256F));
        PartDefinition p_wing_upper_left_mid_f3 = p_wing_upper_left_mid.addOrReplaceChild("wing_upper_left_mid_f3", CubeListBuilder.create()
                .texOffs(177, 58).addBox(-1.5F, -0.055F, 0F, 3F, 10F, 0F, new CubeDeformation(-0.124F, -0.055F, 0F)),
                PartPose.offsetAndRotation(8.28F, 0.7F, 0.41F, 0F, 0F, -0.3142F));
        PartDefinition p_wing_upper_left_tip = p_wing_upper_left_mid.addOrReplaceChild("wing_upper_left_tip", CubeListBuilder.create()
                .texOffs(242, 105).addBox(0F, -1F, -1F, 8F, 2F, 2F, new CubeDeformation(0F, 0F, -0.2F))
                .texOffs(65, 87).addBox(0F, 0.248F, -0.35F, 8F, 5F, 0F, new CubeDeformation(0F, -0.352F, 0F)),
                PartPose.offsetAndRotation(9F, 0F, 0F, 0F, -0.1047F, -0.1745F));
        PartDefinition p_wing_upper_left_tip_f0 = p_wing_upper_left_tip.addOrReplaceChild("wing_upper_left_tip_f0", CubeListBuilder.create()
                .texOffs(244, 45).addBox(-1.5F, -0.34F, 0F, 3F, 11F, 0F, new CubeDeformation(-0.21F, -0.34F, 0F)),
                PartPose.offsetAndRotation(0.96F, 0.7F, 0.05F, 0F, 0F, -0.4189F));
        PartDefinition p_wing_upper_left_tip_f1 = p_wing_upper_left_tip.addOrReplaceChild("wing_upper_left_tip_f1", CubeListBuilder.create()
                .texOffs(41, 45).addBox(-1.5F, -0.2488F, 0F, 3F, 12F, 0F, new CubeDeformation(-0.21F, -0.2488F, 0F)),
                PartPose.offsetAndRotation(2.56F, 0.7F, 0.17F, 0F, 0F, -0.5934F));
        PartDefinition p_wing_upper_left_tip_f2 = p_wing_upper_left_tip.addOrReplaceChild("wing_upper_left_tip_f2", CubeListBuilder.create()
                .texOffs(227, 29).addBox(-1.5F, -0.1575F, 0F, 3F, 13F, 0F, new CubeDeformation(-0.21F, -0.1575F, 0F)),
                PartPose.offsetAndRotation(4.16F, 0.7F, 0.29F, 0F, 0F, -0.7679F));
        PartDefinition p_wing_upper_left_tip_f3 = p_wing_upper_left_tip.addOrReplaceChild("wing_upper_left_tip_f3", CubeListBuilder.create()
                .texOffs(166, 29).addBox(-1.5F, -0.0663F, 0F, 3F, 14F, 0F, new CubeDeformation(-0.21F, -0.0663F, 0F)),
                PartPose.offsetAndRotation(5.76F, 0.7F, 0.41F, 0F, 0F, -0.9425F));
        PartDefinition p_wing_upper_left_tip_f4 = p_wing_upper_left_tip.addOrReplaceChild("wing_upper_left_tip_f4", CubeListBuilder.create()
                .texOffs(265, 0).addBox(-1.5F, -0.475F, 0F, 3F, 16F, 0F, new CubeDeformation(-0.21F, -0.475F, 0F)),
                PartPose.offsetAndRotation(7.36F, 0.7F, 0.53F, 0F, 0F, -1.117F));
        PartDefinition p_wing_upper_right = p_body.addOrReplaceChild("wing_upper_right", CubeListBuilder.create()
                .texOffs(263, 105).addBox(-8F, -1F, -1F, 8F, 2F, 2F, new CubeDeformation(0F, 0F, -0.2F))
                .texOffs(211, 110).addBox(-8F, 0.148F, -0.35F, 8F, 4F, 0F, new CubeDeformation(0F, -0.452F, 0F)),
                PartPose.offsetAndRotation(-2.6F, -13F, 3F, -0.1047F, 0.2793F, 0.8378F));
        PartDefinition p_wing_upper_right_f0 = p_wing_upper_right.addOrReplaceChild("wing_upper_right_f0", CubeListBuilder.create()
                .texOffs(219, 99).addBox(-1.5F, -0.35F, 0F, 3F, 5F, 0F, new CubeDeformation(-0.038F, -0.35F, 0F)),
                PartPose.offset(-0.96F, 0.7F, 0.05F));
        PartDefinition p_wing_upper_right_f1 = p_wing_upper_right.addOrReplaceChild("wing_upper_right_f1", CubeListBuilder.create()
                .texOffs(226, 99).addBox(-1.5F, -0.0275F, 0F, 3F, 5F, 0F, new CubeDeformation(-0.038F, -0.0275F, 0F)),
                PartPose.offsetAndRotation(-4.16F, 0.7F, 0.17F, 0F, 0F, 0.0524F));
        PartDefinition p_wing_upper_right_f2 = p_wing_upper_right.addOrReplaceChild("wing_upper_right_f2", CubeListBuilder.create()
                .texOffs(177, 79).addBox(-1.5F, -0.205F, 0F, 3F, 6F, 0F, new CubeDeformation(-0.038F, -0.205F, 0F)),
                PartPose.offsetAndRotation(-7.36F, 0.7F, 0.29F, 0F, 0F, 0.1047F));
        PartDefinition p_wing_upper_right_mid = p_wing_upper_right.addOrReplaceChild("wing_upper_right_mid", CubeListBuilder.create()
                .texOffs(177, 105).addBox(-9F, -1F, -1F, 9F, 2F, 2F, new CubeDeformation(0F, 0F, -0.2F))
                .texOffs(61, 110).addBox(-9F, 0.448F, -0.35F, 9F, 4F, 0F, new CubeDeformation(0F, -0.152F, 0F)),
                PartPose.offsetAndRotation(-8F, 0F, 0F, 0F, 0.1047F, 0.1745F));
        PartDefinition p_wing_upper_right_mid_f0 = p_wing_upper_right_mid.addOrReplaceChild("wing_upper_right_mid_f0", CubeListBuilder.create()
                .texOffs(0, 79).addBox(-1.5F, -0.06F, 0F, 3F, 7F, 0F, new CubeDeformation(-0.124F, -0.06F, 0F)),
                PartPose.offsetAndRotation(-1.08F, 0.7F, 0.05F, 0F, 0F, 0.1396F));
        PartDefinition p_wing_upper_right_mid_f1 = p_wing_upper_right_mid.addOrReplaceChild("wing_upper_right_mid_f1", CubeListBuilder.create()
                .texOffs(207, 69).addBox(-1.5F, -0.0583F, 0F, 3F, 8F, 0F, new CubeDeformation(-0.124F, -0.0583F, 0F)),
                PartPose.offsetAndRotation(-3.48F, 0.7F, 0.17F, 0F, 0F, 0.1978F));
        PartDefinition p_wing_upper_right_mid_f2 = p_wing_upper_right_mid.addOrReplaceChild("wing_upper_right_mid_f2", CubeListBuilder.create()
                .texOffs(16, 69).addBox(-1.5F, -0.0567F, 0F, 3F, 9F, 0F, new CubeDeformation(-0.124F, -0.0567F, 0F)),
                PartPose.offsetAndRotation(-5.88F, 0.7F, 0.29F, 0F, 0F, 0.256F));
        PartDefinition p_wing_upper_right_mid_f3 = p_wing_upper_right_mid.addOrReplaceChild("wing_upper_right_mid_f3", CubeListBuilder.create()
                .texOffs(184, 58).addBox(-1.5F, -0.055F, 0F, 3F, 10F, 0F, new CubeDeformation(-0.124F, -0.055F, 0F)),
                PartPose.offsetAndRotation(-8.28F, 0.7F, 0.41F, 0F, 0F, 0.3142F));
        PartDefinition p_wing_upper_right_tip = p_wing_upper_right_mid.addOrReplaceChild("wing_upper_right_tip", CubeListBuilder.create()
                .texOffs(284, 105).addBox(-8F, -1F, -1F, 8F, 2F, 2F, new CubeDeformation(0F, 0F, -0.2F))
                .texOffs(82, 87).addBox(-8F, 0.248F, -0.35F, 8F, 5F, 0F, new CubeDeformation(0F, -0.352F, 0F)),
                PartPose.offsetAndRotation(-9F, 0F, 0F, 0F, 0.1047F, 0.1745F));
        PartDefinition p_wing_upper_right_tip_f0 = p_wing_upper_right_tip.addOrReplaceChild("wing_upper_right_tip_f0", CubeListBuilder.create()
                .texOffs(251, 45).addBox(-1.5F, -0.34F, 0F, 3F, 11F, 0F, new CubeDeformation(-0.21F, -0.34F, 0F)),
                PartPose.offsetAndRotation(-0.96F, 0.7F, 0.05F, 0F, 0F, 0.4189F));
        PartDefinition p_wing_upper_right_tip_f1 = p_wing_upper_right_tip.addOrReplaceChild("wing_upper_right_tip_f1", CubeListBuilder.create()
                .texOffs(48, 45).addBox(-1.5F, -0.2488F, 0F, 3F, 12F, 0F, new CubeDeformation(-0.21F, -0.2488F, 0F)),
                PartPose.offsetAndRotation(-2.56F, 0.7F, 0.17F, 0F, 0F, 0.5934F));
        PartDefinition p_wing_upper_right_tip_f2 = p_wing_upper_right_tip.addOrReplaceChild("wing_upper_right_tip_f2", CubeListBuilder.create()
                .texOffs(234, 29).addBox(-1.5F, -0.1575F, 0F, 3F, 13F, 0F, new CubeDeformation(-0.21F, -0.1575F, 0F)),
                PartPose.offsetAndRotation(-4.16F, 0.7F, 0.29F, 0F, 0F, 0.7679F));
        PartDefinition p_wing_upper_right_tip_f3 = p_wing_upper_right_tip.addOrReplaceChild("wing_upper_right_tip_f3", CubeListBuilder.create()
                .texOffs(173, 29).addBox(-1.5F, -0.0663F, 0F, 3F, 14F, 0F, new CubeDeformation(-0.21F, -0.0663F, 0F)),
                PartPose.offsetAndRotation(-5.76F, 0.7F, 0.41F, 0F, 0F, 0.9425F));
        PartDefinition p_wing_upper_right_tip_f4 = p_wing_upper_right_tip.addOrReplaceChild("wing_upper_right_tip_f4", CubeListBuilder.create()
                .texOffs(272, 0).addBox(-1.5F, -0.475F, 0F, 3F, 16F, 0F, new CubeDeformation(-0.21F, -0.475F, 0F)),
                PartPose.offsetAndRotation(-7.36F, 0.7F, 0.53F, 0F, 0F, 1.117F));
        PartDefinition p_wing_mid_left = p_body.addOrReplaceChild("wing_mid_left", CubeListBuilder.create()
                .texOffs(54, 105).addBox(0F, -1F, -1F, 10F, 2F, 2F, new CubeDeformation(0F, 0F, -0.2F))
                .texOffs(0, 110).addBox(0F, 0.4F, -0.35F, 10F, 4F, 0F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offsetAndRotation(2.6F, -10.5F, 3.4F, 0F, -0.384F, -0.2094F));
        PartDefinition p_wing_mid_left_f0 = p_wing_mid_left.addOrReplaceChild("wing_mid_left_f0", CubeListBuilder.create()
                .texOffs(187, 99).addBox(-2F, 0F, 0F, 4F, 5F, 0F, new CubeDeformation(-0.3F, 0F, 0F)),
                PartPose.offset(1.2F, 0.7F, 0.05F));
        PartDefinition p_wing_mid_left_f1 = p_wing_mid_left.addOrReplaceChild("wing_mid_left_f1", CubeListBuilder.create()
                .texOffs(152, 79).addBox(-2F, -0.125F, 0F, 4F, 6F, 0F, new CubeDeformation(-0.3F, -0.125F, 0F)),
                PartPose.offsetAndRotation(5.2F, 0.7F, 0.17F, 0F, 0F, -0.0524F));
        PartDefinition p_wing_mid_left_f2 = p_wing_mid_left.addOrReplaceChild("wing_mid_left_f2", CubeListBuilder.create()
                .texOffs(279, 69).addBox(-2F, -0.25F, 0F, 4F, 7F, 0F, new CubeDeformation(-0.3F, -0.25F, 0F)),
                PartPose.offsetAndRotation(9.2F, 0.7F, 0.29F, 0F, 0F, -0.1047F));
        PartDefinition p_wing_mid_left_mid = p_wing_mid_left.addOrReplaceChild("wing_mid_left_mid", CubeListBuilder.create()
                .texOffs(0, 105).addBox(0F, -1F, -1F, 11F, 2F, 2F, new CubeDeformation(0F, 0F, -0.2F))
                .texOffs(298, 79).addBox(0F, 0.2F, -0.35F, 11F, 5F, 0F, new CubeDeformation(0F, -0.4F, 0F)),
                PartPose.offsetAndRotation(10F, 0F, 0F, 0F, -0.1047F, 0.1571F));
        PartDefinition p_wing_mid_left_mid_f0 = p_wing_mid_left_mid.addOrReplaceChild("wing_mid_left_mid_f0", CubeListBuilder.create()
                .texOffs(182, 69).addBox(-2F, 0F, 0F, 4F, 8F, 0F, new CubeDeformation(-0.4F, 0F, 0F)),
                PartPose.offsetAndRotation(1.32F, 0.7F, 0.05F, 0F, 0F, -0.1396F));
        PartDefinition p_wing_mid_left_mid_f1 = p_wing_mid_left_mid.addOrReplaceChild("wing_mid_left_mid_f1", CubeListBuilder.create()
                .texOffs(159, 58).addBox(-2F, -0.4167F, 0F, 4F, 10F, 0F, new CubeDeformation(-0.4F, -0.4167F, 0F)),
                PartPose.offsetAndRotation(4.2533F, 0.7F, 0.17F, 0F, 0F, -0.1978F));
        PartDefinition p_wing_mid_left_mid_f2 = p_wing_mid_left_mid.addOrReplaceChild("wing_mid_left_mid_f2", CubeListBuilder.create()
                .texOffs(226, 45).addBox(-2F, -0.3333F, 0F, 4F, 11F, 0F, new CubeDeformation(-0.4F, -0.3333F, 0F)),
                PartPose.offsetAndRotation(7.1867F, 0.7F, 0.29F, 0F, 0F, -0.256F));
        PartDefinition p_wing_mid_left_mid_f3 = p_wing_mid_left_mid.addOrReplaceChild("wing_mid_left_mid_f3", CubeListBuilder.create()
                .texOffs(23, 45).addBox(-2F, -0.25F, 0F, 4F, 12F, 0F, new CubeDeformation(-0.4F, -0.25F, 0F)),
                PartPose.offsetAndRotation(10.12F, 0.7F, 0.41F, 0F, 0F, -0.3142F));
        PartDefinition p_wing_mid_left_tip = p_wing_mid_left_mid.addOrReplaceChild("wing_mid_left_tip", CubeListBuilder.create()
                .texOffs(79, 105).addBox(0F, -1F, -1F, 10F, 2F, 2F, new CubeDeformation(0F, 0F, -0.2F))
                .texOffs(23, 87).addBox(0F, 0.5F, -0.35F, 10F, 5F, 0F, new CubeDeformation(0F, -0.1F, 0F)),
                PartPose.offsetAndRotation(11F, 0F, 0F, 0F, -0.1047F, 0.1571F));
        PartDefinition p_wing_mid_left_tip_f0 = p_wing_mid_left_tip.addOrReplaceChild("wing_mid_left_tip_f0", CubeListBuilder.create()
                .texOffs(55, 45).addBox(-1.5F, 0F, 0F, 3F, 12F, 0F),
                PartPose.offsetAndRotation(1.2F, 0.7F, 0.05F, 0F, 0F, -0.4189F));
        PartDefinition p_wing_mid_left_tip_f1 = p_wing_mid_left_tip.addOrReplaceChild("wing_mid_left_tip_f1", CubeListBuilder.create()
                .texOffs(180, 29).addBox(-1.5F, -0.3125F, 0F, 3F, 14F, 0F, new CubeDeformation(0F, -0.3125F, 0F)),
                PartPose.offsetAndRotation(3.2F, 0.7F, 0.17F, 0F, 0F, -0.5934F));
        PartDefinition p_wing_mid_left_tip_f2 = p_wing_mid_left_tip.addOrReplaceChild("wing_mid_left_tip_f2", CubeListBuilder.create()
                .texOffs(34, 29).addBox(-1.5F, -0.125F, 0F, 3F, 15F, 0F, new CubeDeformation(0F, -0.125F, 0F)),
                PartPose.offsetAndRotation(5.2F, 0.7F, 0.29F, 0F, 0F, -0.7679F));
        PartDefinition p_wing_mid_left_tip_f3 = p_wing_mid_left_tip.addOrReplaceChild("wing_mid_left_tip_f3", CubeListBuilder.create()
                .texOffs(176, 0).addBox(-1.5F, -0.4375F, 0F, 3F, 17F, 0F, new CubeDeformation(0F, -0.4375F, 0F)),
                PartPose.offsetAndRotation(7.2F, 0.7F, 0.41F, 0F, 0F, -0.9425F));
        PartDefinition p_wing_mid_left_tip_f4 = p_wing_mid_left_tip.addOrReplaceChild("wing_mid_left_tip_f4", CubeListBuilder.create()
                .texOffs(162, 0).addBox(-1.5F, -0.25F, 0F, 3F, 18F, 0F, new CubeDeformation(0F, -0.25F, 0F)),
                PartPose.offsetAndRotation(9.2F, 0.7F, 0.53F, 0F, 0F, -1.117F));
        PartDefinition p_wing_mid_right = p_body.addOrReplaceChild("wing_mid_right", CubeListBuilder.create()
                .texOffs(104, 105).addBox(-10F, -1F, -1F, 10F, 2F, 2F, new CubeDeformation(0F, 0F, -0.2F))
                .texOffs(21, 110).addBox(-10F, 0.4F, -0.35F, 10F, 4F, 0F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offsetAndRotation(-2.6F, -10.5F, 3.4F, 0F, 0.384F, 0.2094F));
        PartDefinition p_wing_mid_right_f0 = p_wing_mid_right.addOrReplaceChild("wing_mid_right_f0", CubeListBuilder.create()
                .texOffs(196, 99).addBox(-2F, 0F, 0F, 4F, 5F, 0F, new CubeDeformation(-0.3F, 0F, 0F)),
                PartPose.offset(-1.2F, 0.7F, 0.05F));
        PartDefinition p_wing_mid_right_f1 = p_wing_mid_right.addOrReplaceChild("wing_mid_right_f1", CubeListBuilder.create()
                .texOffs(161, 79).addBox(-2F, -0.125F, 0F, 4F, 6F, 0F, new CubeDeformation(-0.3F, -0.125F, 0F)),
                PartPose.offsetAndRotation(-5.2F, 0.7F, 0.17F, 0F, 0F, 0.0524F));
        PartDefinition p_wing_mid_right_f2 = p_wing_mid_right.addOrReplaceChild("wing_mid_right_f2", CubeListBuilder.create()
                .texOffs(288, 69).addBox(-2F, -0.25F, 0F, 4F, 7F, 0F, new CubeDeformation(-0.3F, -0.25F, 0F)),
                PartPose.offsetAndRotation(-9.2F, 0.7F, 0.29F, 0F, 0F, 0.1047F));
        PartDefinition p_wing_mid_right_mid = p_wing_mid_right.addOrReplaceChild("wing_mid_right_mid", CubeListBuilder.create()
                .texOffs(27, 105).addBox(-11F, -1F, -1F, 11F, 2F, 2F, new CubeDeformation(0F, 0F, -0.2F))
                .texOffs(0, 87).addBox(-11F, 0.2F, -0.35F, 11F, 5F, 0F, new CubeDeformation(0F, -0.4F, 0F)),
                PartPose.offsetAndRotation(-10F, 0F, 0F, 0F, 0.1047F, -0.1571F));
        PartDefinition p_wing_mid_right_mid_f0 = p_wing_mid_right_mid.addOrReplaceChild("wing_mid_right_mid_f0", CubeListBuilder.create()
                .texOffs(191, 69).addBox(-2F, 0F, 0F, 4F, 8F, 0F, new CubeDeformation(-0.4F, 0F, 0F)),
                PartPose.offsetAndRotation(-1.32F, 0.7F, 0.05F, 0F, 0F, 0.1396F));
        PartDefinition p_wing_mid_right_mid_f1 = p_wing_mid_right_mid.addOrReplaceChild("wing_mid_right_mid_f1", CubeListBuilder.create()
                .texOffs(168, 58).addBox(-2F, -0.4167F, 0F, 4F, 10F, 0F, new CubeDeformation(-0.4F, -0.4167F, 0F)),
                PartPose.offsetAndRotation(-4.2533F, 0.7F, 0.17F, 0F, 0F, 0.1978F));
        PartDefinition p_wing_mid_right_mid_f2 = p_wing_mid_right_mid.addOrReplaceChild("wing_mid_right_mid_f2", CubeListBuilder.create()
                .texOffs(235, 45).addBox(-2F, -0.3333F, 0F, 4F, 11F, 0F, new CubeDeformation(-0.4F, -0.3333F, 0F)),
                PartPose.offsetAndRotation(-7.1867F, 0.7F, 0.29F, 0F, 0F, 0.256F));
        PartDefinition p_wing_mid_right_mid_f3 = p_wing_mid_right_mid.addOrReplaceChild("wing_mid_right_mid_f3", CubeListBuilder.create()
                .texOffs(32, 45).addBox(-2F, -0.25F, 0F, 4F, 12F, 0F, new CubeDeformation(-0.4F, -0.25F, 0F)),
                PartPose.offsetAndRotation(-10.12F, 0.7F, 0.41F, 0F, 0F, 0.3142F));
        PartDefinition p_wing_mid_right_tip = p_wing_mid_right_mid.addOrReplaceChild("wing_mid_right_tip", CubeListBuilder.create()
                .texOffs(129, 105).addBox(-10F, -1F, -1F, 10F, 2F, 2F, new CubeDeformation(0F, 0F, -0.2F))
                .texOffs(44, 87).addBox(-10F, 0.5F, -0.35F, 10F, 5F, 0F, new CubeDeformation(0F, -0.1F, 0F)),
                PartPose.offsetAndRotation(-11F, 0F, 0F, 0F, 0.1047F, -0.1571F));
        PartDefinition p_wing_mid_right_tip_f0 = p_wing_mid_right_tip.addOrReplaceChild("wing_mid_right_tip_f0", CubeListBuilder.create()
                .texOffs(62, 45).addBox(-1.5F, 0F, 0F, 3F, 12F, 0F),
                PartPose.offsetAndRotation(-1.2F, 0.7F, 0.05F, 0F, 0F, 0.4189F));
        PartDefinition p_wing_mid_right_tip_f1 = p_wing_mid_right_tip.addOrReplaceChild("wing_mid_right_tip_f1", CubeListBuilder.create()
                .texOffs(187, 29).addBox(-1.5F, -0.3125F, 0F, 3F, 14F, 0F, new CubeDeformation(0F, -0.3125F, 0F)),
                PartPose.offsetAndRotation(-3.2F, 0.7F, 0.17F, 0F, 0F, 0.5934F));
        PartDefinition p_wing_mid_right_tip_f2 = p_wing_mid_right_tip.addOrReplaceChild("wing_mid_right_tip_f2", CubeListBuilder.create()
                .texOffs(41, 29).addBox(-1.5F, -0.125F, 0F, 3F, 15F, 0F, new CubeDeformation(0F, -0.125F, 0F)),
                PartPose.offsetAndRotation(-5.2F, 0.7F, 0.29F, 0F, 0F, 0.7679F));
        PartDefinition p_wing_mid_right_tip_f3 = p_wing_mid_right_tip.addOrReplaceChild("wing_mid_right_tip_f3", CubeListBuilder.create()
                .texOffs(183, 0).addBox(-1.5F, -0.4375F, 0F, 3F, 17F, 0F, new CubeDeformation(0F, -0.4375F, 0F)),
                PartPose.offsetAndRotation(-7.2F, 0.7F, 0.41F, 0F, 0F, 0.9425F));
        PartDefinition p_wing_mid_right_tip_f4 = p_wing_mid_right_tip.addOrReplaceChild("wing_mid_right_tip_f4", CubeListBuilder.create()
                .texOffs(169, 0).addBox(-1.5F, -0.25F, 0F, 3F, 18F, 0F, new CubeDeformation(0F, -0.25F, 0F)),
                PartPose.offsetAndRotation(-9.2F, 0.7F, 0.53F, 0F, 0F, 1.117F));
        PartDefinition p_wing_lower_left = p_body.addOrReplaceChild("wing_lower_left", CubeListBuilder.create()
                .texOffs(80, 110).addBox(-0.25F, -1F, -1F, 7F, 2F, 2F, new CubeDeformation(-0.25F, 0F, -0.2F))
                .texOffs(299, 115).addBox(-0.25F, 0.396F, -0.35F, 7F, 3F, 0F, new CubeDeformation(-0.25F, -0.204F, 0F)),
                PartPose.offsetAndRotation(2.6F, -7.5F, 3.2F, 0.1047F, -0.3142F, 0.4189F));
        PartDefinition p_wing_lower_left_f0 = p_wing_lower_left.addOrReplaceChild("wing_lower_left_f0", CubeListBuilder.create()
                .texOffs(250, 115).addBox(-1.5F, -0.2F, 0F, 3F, 4F, 0F, new CubeDeformation(-0.276F, -0.2F, 0F)),
                PartPose.offset(0.78F, 0.7F, 0.05F));
        PartDefinition p_wing_lower_left_f1 = p_wing_lower_left.addOrReplaceChild("wing_lower_left_f1", CubeListBuilder.create()
                .texOffs(233, 99).addBox(-1.5F, -0.43F, 0F, 3F, 5F, 0F, new CubeDeformation(-0.276F, -0.43F, 0F)),
                PartPose.offsetAndRotation(3.38F, 0.7F, 0.17F, 0F, 0F, -0.0524F));
        PartDefinition p_wing_lower_left_f2 = p_wing_lower_left.addOrReplaceChild("wing_lower_left_f2", CubeListBuilder.create()
                .texOffs(240, 99).addBox(-1.5F, -0.16F, 0F, 3F, 5F, 0F, new CubeDeformation(-0.276F, -0.16F, 0F)),
                PartPose.offsetAndRotation(5.98F, 0.7F, 0.29F, 0F, 0F, -0.1047F));
        PartDefinition p_wing_lower_left_mid = p_wing_lower_left.addOrReplaceChild("wing_lower_left_mid", CubeListBuilder.create()
                .texOffs(99, 110).addBox(0F, -1F, -1F, 7F, 2F, 2F, new CubeDeformation(0F, 0F, -0.2F))
                .texOffs(150, 115).addBox(0F, 0.196F, -0.35F, 7F, 4F, 0F, new CubeDeformation(0F, -0.404F, 0F)),
                PartPose.offsetAndRotation(6.5F, 0F, 0F, 0F, -0.1047F, 0.2094F));
        PartDefinition p_wing_lower_left_mid_f0 = p_wing_lower_left_mid.addOrReplaceChild("wing_lower_left_mid_f0", CubeListBuilder.create()
                .texOffs(184, 79).addBox(-1.5F, -0.12F, 0F, 3F, 6F, 0F, new CubeDeformation(-0.348F, -0.12F, 0F)),
                PartPose.offsetAndRotation(0.84F, 0.7F, 0.05F, 0F, 0F, -0.1396F));
        PartDefinition p_wing_lower_left_mid_f1 = p_wing_lower_left_mid.addOrReplaceChild("wing_lower_left_mid_f1", CubeListBuilder.create()
                .texOffs(7, 79).addBox(-1.5F, -0.2F, 0F, 3F, 7F, 0F, new CubeDeformation(-0.348F, -0.2F, 0F)),
                PartPose.offsetAndRotation(2.7067F, 0.7F, 0.17F, 0F, 0F, -0.1978F));
        PartDefinition p_wing_lower_left_mid_f2 = p_wing_lower_left_mid.addOrReplaceChild("wing_lower_left_mid_f2", CubeListBuilder.create()
                .texOffs(214, 69).addBox(-1.5F, -0.28F, 0F, 3F, 8F, 0F, new CubeDeformation(-0.348F, -0.28F, 0F)),
                PartPose.offsetAndRotation(4.5733F, 0.7F, 0.29F, 0F, 0F, -0.256F));
        PartDefinition p_wing_lower_left_mid_f3 = p_wing_lower_left_mid.addOrReplaceChild("wing_lower_left_mid_f3", CubeListBuilder.create()
                .texOffs(23, 69).addBox(-1.5F, -0.36F, 0F, 3F, 9F, 0F, new CubeDeformation(-0.348F, -0.36F, 0F)),
                PartPose.offsetAndRotation(6.44F, 0.7F, 0.41F, 0F, 0F, -0.3142F));
        PartDefinition p_wing_lower_left_tip = p_wing_lower_left_mid.addOrReplaceChild("wing_lower_left_tip", CubeListBuilder.create()
                .texOffs(118, 110).addBox(-0.25F, -1F, -1F, 7F, 2F, 2F, new CubeDeformation(-0.25F, 0F, -0.2F))
                .texOffs(165, 115).addBox(-0.25F, 0.496F, -0.35F, 7F, 4F, 0F, new CubeDeformation(-0.25F, -0.104F, 0F)),
                PartPose.offsetAndRotation(7F, 0F, 0F, 0F, -0.1047F, 0.2094F));
        PartDefinition p_wing_lower_left_tip_f0 = p_wing_lower_left_tip.addOrReplaceChild("wing_lower_left_tip_f0", CubeListBuilder.create()
                .texOffs(30, 69).addBox(-1.5F, -0.18F, 0F, 3F, 9F, 0F, new CubeDeformation(-0.42F, -0.18F, 0F)),
                PartPose.offsetAndRotation(0.78F, 0.7F, 0.05F, 0F, 0F, -0.4189F));
        PartDefinition p_wing_lower_left_tip_f1 = p_wing_lower_left_tip.addOrReplaceChild("wing_lower_left_tip_f1", CubeListBuilder.create()
                .texOffs(191, 58).addBox(-1.5F, -0.185F, 0F, 3F, 10F, 0F, new CubeDeformation(-0.42F, -0.185F, 0F)),
                PartPose.offsetAndRotation(2.08F, 0.7F, 0.17F, 0F, 0F, -0.5934F));
        PartDefinition p_wing_lower_left_tip_f2 = p_wing_lower_left_tip.addOrReplaceChild("wing_lower_left_tip_f2", CubeListBuilder.create()
                .texOffs(258, 45).addBox(-1.5F, -0.19F, 0F, 3F, 11F, 0F, new CubeDeformation(-0.42F, -0.19F, 0F)),
                PartPose.offsetAndRotation(3.38F, 0.7F, 0.29F, 0F, 0F, -0.7679F));
        PartDefinition p_wing_lower_left_tip_f3 = p_wing_lower_left_tip.addOrReplaceChild("wing_lower_left_tip_f3", CubeListBuilder.create()
                .texOffs(69, 45).addBox(-1.5F, -0.195F, 0F, 3F, 12F, 0F, new CubeDeformation(-0.42F, -0.195F, 0F)),
                PartPose.offsetAndRotation(4.68F, 0.7F, 0.41F, 0F, 0F, -0.9425F));
        PartDefinition p_wing_lower_left_tip_f4 = p_wing_lower_left_tip.addOrReplaceChild("wing_lower_left_tip_f4", CubeListBuilder.create()
                .texOffs(241, 29).addBox(-1.5F, -0.2F, 0F, 3F, 13F, 0F, new CubeDeformation(-0.42F, -0.2F, 0F)),
                PartPose.offsetAndRotation(5.98F, 0.7F, 0.53F, 0F, 0F, -1.117F));
        PartDefinition p_wing_lower_right = p_body.addOrReplaceChild("wing_lower_right", CubeListBuilder.create()
                .texOffs(137, 110).addBox(-6.75F, -1F, -1F, 7F, 2F, 2F, new CubeDeformation(-0.25F, 0F, -0.2F))
                .texOffs(0, 120).addBox(-6.75F, 0.396F, -0.35F, 7F, 3F, 0F, new CubeDeformation(-0.25F, -0.204F, 0F)),
                PartPose.offsetAndRotation(-2.6F, -7.5F, 3.2F, 0.1047F, 0.3142F, -0.4189F));
        PartDefinition p_wing_lower_right_f0 = p_wing_lower_right.addOrReplaceChild("wing_lower_right_f0", CubeListBuilder.create()
                .texOffs(257, 115).addBox(-1.5F, -0.2F, 0F, 3F, 4F, 0F, new CubeDeformation(-0.276F, -0.2F, 0F)),
                PartPose.offset(-0.78F, 0.7F, 0.05F));
        PartDefinition p_wing_lower_right_f1 = p_wing_lower_right.addOrReplaceChild("wing_lower_right_f1", CubeListBuilder.create()
                .texOffs(247, 99).addBox(-1.5F, -0.43F, 0F, 3F, 5F, 0F, new CubeDeformation(-0.276F, -0.43F, 0F)),
                PartPose.offsetAndRotation(-3.38F, 0.7F, 0.17F, 0F, 0F, 0.0524F));
        PartDefinition p_wing_lower_right_f2 = p_wing_lower_right.addOrReplaceChild("wing_lower_right_f2", CubeListBuilder.create()
                .texOffs(254, 99).addBox(-1.5F, -0.16F, 0F, 3F, 5F, 0F, new CubeDeformation(-0.276F, -0.16F, 0F)),
                PartPose.offsetAndRotation(-5.98F, 0.7F, 0.29F, 0F, 0F, 0.1047F));
        PartDefinition p_wing_lower_right_mid = p_wing_lower_right.addOrReplaceChild("wing_lower_right_mid", CubeListBuilder.create()
                .texOffs(156, 110).addBox(-7F, -1F, -1F, 7F, 2F, 2F, new CubeDeformation(0F, 0F, -0.2F))
                .texOffs(180, 115).addBox(-7F, 0.196F, -0.35F, 7F, 4F, 0F, new CubeDeformation(0F, -0.404F, 0F)),
                PartPose.offsetAndRotation(-6.5F, 0F, 0F, 0F, 0.1047F, -0.2094F));
        PartDefinition p_wing_lower_right_mid_f0 = p_wing_lower_right_mid.addOrReplaceChild("wing_lower_right_mid_f0", CubeListBuilder.create()
                .texOffs(191, 79).addBox(-1.5F, -0.12F, 0F, 3F, 6F, 0F, new CubeDeformation(-0.348F, -0.12F, 0F)),
                PartPose.offsetAndRotation(-0.84F, 0.7F, 0.05F, 0F, 0F, 0.1396F));
        PartDefinition p_wing_lower_right_mid_f1 = p_wing_lower_right_mid.addOrReplaceChild("wing_lower_right_mid_f1", CubeListBuilder.create()
                .texOffs(14, 79).addBox(-1.5F, -0.2F, 0F, 3F, 7F, 0F, new CubeDeformation(-0.348F, -0.2F, 0F)),
                PartPose.offsetAndRotation(-2.7067F, 0.7F, 0.17F, 0F, 0F, 0.1978F));
        PartDefinition p_wing_lower_right_mid_f2 = p_wing_lower_right_mid.addOrReplaceChild("wing_lower_right_mid_f2", CubeListBuilder.create()
                .texOffs(221, 69).addBox(-1.5F, -0.28F, 0F, 3F, 8F, 0F, new CubeDeformation(-0.348F, -0.28F, 0F)),
                PartPose.offsetAndRotation(-4.5733F, 0.7F, 0.29F, 0F, 0F, 0.256F));
        PartDefinition p_wing_lower_right_mid_f3 = p_wing_lower_right_mid.addOrReplaceChild("wing_lower_right_mid_f3", CubeListBuilder.create()
                .texOffs(37, 69).addBox(-1.5F, -0.36F, 0F, 3F, 9F, 0F, new CubeDeformation(-0.348F, -0.36F, 0F)),
                PartPose.offsetAndRotation(-6.44F, 0.7F, 0.41F, 0F, 0F, 0.3142F));
        PartDefinition p_wing_lower_right_tip = p_wing_lower_right_mid.addOrReplaceChild("wing_lower_right_tip", CubeListBuilder.create()
                .texOffs(175, 110).addBox(-6.75F, -1F, -1F, 7F, 2F, 2F, new CubeDeformation(-0.25F, 0F, -0.2F))
                .texOffs(195, 115).addBox(-6.75F, 0.496F, -0.35F, 7F, 4F, 0F, new CubeDeformation(-0.25F, -0.104F, 0F)),
                PartPose.offsetAndRotation(-7F, 0F, 0F, 0F, 0.1047F, -0.2094F));
        PartDefinition p_wing_lower_right_tip_f0 = p_wing_lower_right_tip.addOrReplaceChild("wing_lower_right_tip_f0", CubeListBuilder.create()
                .texOffs(44, 69).addBox(-1.5F, -0.18F, 0F, 3F, 9F, 0F, new CubeDeformation(-0.42F, -0.18F, 0F)),
                PartPose.offsetAndRotation(-0.78F, 0.7F, 0.05F, 0F, 0F, 0.4189F));
        PartDefinition p_wing_lower_right_tip_f1 = p_wing_lower_right_tip.addOrReplaceChild("wing_lower_right_tip_f1", CubeListBuilder.create()
                .texOffs(198, 58).addBox(-1.5F, -0.185F, 0F, 3F, 10F, 0F, new CubeDeformation(-0.42F, -0.185F, 0F)),
                PartPose.offsetAndRotation(-2.08F, 0.7F, 0.17F, 0F, 0F, 0.5934F));
        PartDefinition p_wing_lower_right_tip_f2 = p_wing_lower_right_tip.addOrReplaceChild("wing_lower_right_tip_f2", CubeListBuilder.create()
                .texOffs(265, 45).addBox(-1.5F, -0.19F, 0F, 3F, 11F, 0F, new CubeDeformation(-0.42F, -0.19F, 0F)),
                PartPose.offsetAndRotation(-3.38F, 0.7F, 0.29F, 0F, 0F, 0.7679F));
        PartDefinition p_wing_lower_right_tip_f3 = p_wing_lower_right_tip.addOrReplaceChild("wing_lower_right_tip_f3", CubeListBuilder.create()
                .texOffs(76, 45).addBox(-1.5F, -0.195F, 0F, 3F, 12F, 0F, new CubeDeformation(-0.42F, -0.195F, 0F)),
                PartPose.offsetAndRotation(-4.68F, 0.7F, 0.41F, 0F, 0F, 0.9425F));
        PartDefinition p_wing_lower_right_tip_f4 = p_wing_lower_right_tip.addOrReplaceChild("wing_lower_right_tip_f4", CubeListBuilder.create()
                .texOffs(248, 29).addBox(-1.5F, -0.2F, 0F, 3F, 13F, 0F, new CubeDeformation(-0.42F, -0.2F, 0F)),
                PartPose.offsetAndRotation(-5.98F, 0.7F, 0.53F, 0F, 0F, 1.117F));
        PartDefinition p_true_rings = p_body.addOrReplaceChild("true_rings", CubeListBuilder.create(),
                PartPose.offset(0F, -8F, 0F));
        PartDefinition p_true_ring_a = p_true_rings.addOrReplaceChild("true_ring_a", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_true_ring_a_0 = p_true_ring_a.addOrReplaceChild("true_ring_a_0", CubeListBuilder.create()
                .texOffs(99, 87).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.4188F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(15F, 0F, 0F, 0F, -1.5708F, 0F));
        PartDefinition p_true_ring_a_1 = p_true_ring_a.addOrReplaceChild("true_ring_a_1", CubeListBuilder.create()
                .texOffs(116, 87).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.4188F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(14.2658F, 0F, 4.6353F, 0F, -1.885F, 0F));
        PartDefinition p_true_ring_a_2 = p_true_ring_a.addOrReplaceChild("true_ring_a_2", CubeListBuilder.create()
                .texOffs(133, 87).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.4188F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(12.1353F, 0F, 8.8168F, 0F, -2.1991F, 0F));
        PartDefinition p_true_ring_a_3 = p_true_ring_a.addOrReplaceChild("true_ring_a_3", CubeListBuilder.create()
                .texOffs(150, 87).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.4188F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(8.8168F, 0F, 12.1353F, 0F, -2.5133F, 0F));
        PartDefinition p_true_ring_a_4 = p_true_ring_a.addOrReplaceChild("true_ring_a_4", CubeListBuilder.create()
                .texOffs(167, 87).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.4188F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(4.6353F, 0F, 14.2658F, 0F, -2.8274F, 0F));
        PartDefinition p_true_ring_a_5 = p_true_ring_a.addOrReplaceChild("true_ring_a_5", CubeListBuilder.create()
                .texOffs(184, 87).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.4188F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(0F, 0F, 15F, 0F, -3.1416F, 0F));
        PartDefinition p_true_ring_a_6 = p_true_ring_a.addOrReplaceChild("true_ring_a_6", CubeListBuilder.create()
                .texOffs(201, 87).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.4188F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(-4.6353F, 0F, 14.2658F, 0F, -3.4558F, 0F));
        PartDefinition p_true_ring_a_7 = p_true_ring_a.addOrReplaceChild("true_ring_a_7", CubeListBuilder.create()
                .texOffs(218, 87).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.4188F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(-8.8168F, 0F, 12.1353F, 0F, -3.7699F, 0F));
        PartDefinition p_true_ring_a_8 = p_true_ring_a.addOrReplaceChild("true_ring_a_8", CubeListBuilder.create()
                .texOffs(235, 87).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.4188F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(-12.1353F, 0F, 8.8168F, 0F, -4.0841F, 0F));
        PartDefinition p_true_ring_a_9 = p_true_ring_a.addOrReplaceChild("true_ring_a_9", CubeListBuilder.create()
                .texOffs(252, 87).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.4188F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(-14.2658F, 0F, 4.6353F, 0F, -4.3982F, 0F));
        PartDefinition p_true_ring_a_10 = p_true_ring_a.addOrReplaceChild("true_ring_a_10", CubeListBuilder.create()
                .texOffs(269, 87).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.4188F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(-15F, 0F, 0F, 0F, -4.7124F, 0F));
        PartDefinition p_true_ring_a_11 = p_true_ring_a.addOrReplaceChild("true_ring_a_11", CubeListBuilder.create()
                .texOffs(286, 87).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.4188F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(-14.2658F, 0F, -4.6353F, 0F, -5.0265F, 0F));
        PartDefinition p_true_ring_a_12 = p_true_ring_a.addOrReplaceChild("true_ring_a_12", CubeListBuilder.create()
                .texOffs(303, 87).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.4188F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(-12.1353F, 0F, -8.8168F, 0F, -5.3407F, 0F));
        PartDefinition p_true_ring_a_13 = p_true_ring_a.addOrReplaceChild("true_ring_a_13", CubeListBuilder.create()
                .texOffs(0, 93).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.4188F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(-8.8168F, 0F, -12.1353F, 0F, -5.6549F, 0F));
        PartDefinition p_true_ring_a_14 = p_true_ring_a.addOrReplaceChild("true_ring_a_14", CubeListBuilder.create()
                .texOffs(17, 93).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.4188F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(-4.6353F, 0F, -14.2658F, 0F, -5.969F, 0F));
        PartDefinition p_true_ring_a_15 = p_true_ring_a.addOrReplaceChild("true_ring_a_15", CubeListBuilder.create()
                .texOffs(34, 93).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.4188F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(0F, 0F, -15F, 0F, -6.2832F, 0F));
        PartDefinition p_true_ring_a_16 = p_true_ring_a.addOrReplaceChild("true_ring_a_16", CubeListBuilder.create()
                .texOffs(51, 93).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.4188F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(4.6353F, 0F, -14.2658F, 0F, -6.5973F, 0F));
        PartDefinition p_true_ring_a_17 = p_true_ring_a.addOrReplaceChild("true_ring_a_17", CubeListBuilder.create()
                .texOffs(68, 93).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.4188F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(8.8168F, 0F, -12.1353F, 0F, -6.9115F, 0F));
        PartDefinition p_true_ring_a_18 = p_true_ring_a.addOrReplaceChild("true_ring_a_18", CubeListBuilder.create()
                .texOffs(85, 93).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.4188F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(12.1353F, 0F, -8.8168F, 0F, -7.2257F, 0F));
        PartDefinition p_true_ring_a_19 = p_true_ring_a.addOrReplaceChild("true_ring_a_19", CubeListBuilder.create()
                .texOffs(102, 93).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.4188F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(14.2658F, 0F, -4.6353F, 0F, -7.5398F, 0F));
        PartDefinition p_true_ring_b = p_true_rings.addOrReplaceChild("true_ring_b", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0F, 0F, 0F, 1.5708F, 0F, 0F));
        PartDefinition p_true_ring_b_0 = p_true_ring_b.addOrReplaceChild("true_ring_b_0", CubeListBuilder.create()
                .texOffs(119, 93).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.276F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(17.5F, 0F, 0F, 0F, -1.5708F, 0F));
        PartDefinition p_true_ring_b_1 = p_true_ring_b.addOrReplaceChild("true_ring_b_1", CubeListBuilder.create()
                .texOffs(136, 93).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.276F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(16.7911F, 0F, 4.9303F, 0F, -1.8564F, 0F));
        PartDefinition p_true_ring_b_2 = p_true_ring_b.addOrReplaceChild("true_ring_b_2", CubeListBuilder.create()
                .texOffs(153, 93).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.276F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(14.7219F, 0F, 9.4612F, 0F, -2.142F, 0F));
        PartDefinition p_true_ring_b_3 = p_true_ring_b.addOrReplaceChild("true_ring_b_3", CubeListBuilder.create()
                .texOffs(170, 93).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.276F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(11.4601F, 0F, 13.2256F, 0F, -2.4276F, 0F));
        PartDefinition p_true_ring_b_4 = p_true_ring_b.addOrReplaceChild("true_ring_b_4", CubeListBuilder.create()
                .texOffs(187, 93).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.276F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(7.2698F, 0F, 15.9186F, 0F, -2.7132F, 0F));
        PartDefinition p_true_ring_b_5 = p_true_ring_b.addOrReplaceChild("true_ring_b_5", CubeListBuilder.create()
                .texOffs(204, 93).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.276F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(2.4905F, 0F, 17.3219F, 0F, -2.9988F, 0F));
        PartDefinition p_true_ring_b_6 = p_true_ring_b.addOrReplaceChild("true_ring_b_6", CubeListBuilder.create()
                .texOffs(221, 93).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.276F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(-2.4905F, 0F, 17.3219F, 0F, -3.2844F, 0F));
        PartDefinition p_true_ring_b_7 = p_true_ring_b.addOrReplaceChild("true_ring_b_7", CubeListBuilder.create()
                .texOffs(238, 93).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.276F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(-7.2698F, 0F, 15.9186F, 0F, -3.57F, 0F));
        PartDefinition p_true_ring_b_8 = p_true_ring_b.addOrReplaceChild("true_ring_b_8", CubeListBuilder.create()
                .texOffs(255, 93).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.276F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(-11.4601F, 0F, 13.2256F, 0F, -3.8556F, 0F));
        PartDefinition p_true_ring_b_9 = p_true_ring_b.addOrReplaceChild("true_ring_b_9", CubeListBuilder.create()
                .texOffs(272, 93).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.276F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(-14.7219F, 0F, 9.4612F, 0F, -4.1412F, 0F));
        PartDefinition p_true_ring_b_10 = p_true_ring_b.addOrReplaceChild("true_ring_b_10", CubeListBuilder.create()
                .texOffs(289, 93).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.276F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(-16.7911F, 0F, 4.9303F, 0F, -4.4268F, 0F));
        PartDefinition p_true_ring_b_11 = p_true_ring_b.addOrReplaceChild("true_ring_b_11", CubeListBuilder.create()
                .texOffs(0, 99).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.276F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(-17.5F, 0F, 0F, 0F, -4.7124F, 0F));
        PartDefinition p_true_ring_b_12 = p_true_ring_b.addOrReplaceChild("true_ring_b_12", CubeListBuilder.create()
                .texOffs(17, 99).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.276F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(-16.7911F, 0F, -4.9303F, 0F, -4.998F, 0F));
        PartDefinition p_true_ring_b_13 = p_true_ring_b.addOrReplaceChild("true_ring_b_13", CubeListBuilder.create()
                .texOffs(34, 99).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.276F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(-14.7219F, 0F, -9.4612F, 0F, -5.2836F, 0F));
        PartDefinition p_true_ring_b_14 = p_true_ring_b.addOrReplaceChild("true_ring_b_14", CubeListBuilder.create()
                .texOffs(51, 99).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.276F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(-11.4601F, 0F, -13.2256F, 0F, -5.5692F, 0F));
        PartDefinition p_true_ring_b_15 = p_true_ring_b.addOrReplaceChild("true_ring_b_15", CubeListBuilder.create()
                .texOffs(68, 99).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.276F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(-7.2698F, 0F, -15.9186F, 0F, -5.8548F, 0F));
        PartDefinition p_true_ring_b_16 = p_true_ring_b.addOrReplaceChild("true_ring_b_16", CubeListBuilder.create()
                .texOffs(85, 99).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.276F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(-2.4905F, 0F, -17.3219F, 0F, -6.1404F, 0F));
        PartDefinition p_true_ring_b_17 = p_true_ring_b.addOrReplaceChild("true_ring_b_17", CubeListBuilder.create()
                .texOffs(102, 99).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.276F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(2.4905F, 0F, -17.3219F, 0F, -6.426F, 0F));
        PartDefinition p_true_ring_b_18 = p_true_ring_b.addOrReplaceChild("true_ring_b_18", CubeListBuilder.create()
                .texOffs(119, 99).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.276F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(7.2698F, 0F, -15.9186F, 0F, -6.7116F, 0F));
        PartDefinition p_true_ring_b_19 = p_true_ring_b.addOrReplaceChild("true_ring_b_19", CubeListBuilder.create()
                .texOffs(136, 99).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.276F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(11.4601F, 0F, -13.2256F, 0F, -6.9972F, 0F));
        PartDefinition p_true_ring_b_20 = p_true_ring_b.addOrReplaceChild("true_ring_b_20", CubeListBuilder.create()
                .texOffs(153, 99).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.276F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(14.7219F, 0F, -9.4612F, 0F, -7.2828F, 0F));
        PartDefinition p_true_ring_b_21 = p_true_ring_b.addOrReplaceChild("true_ring_b_21", CubeListBuilder.create()
                .texOffs(170, 99).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.276F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(16.7911F, 0F, -4.9303F, 0F, -7.5684F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(223, 0).addBox(-2.5F, 0F, -2.5F, 5F, 11F, 5F, new CubeDeformation(-0.4F, 0F, -0.4F)),
                PartPose.offset(3F, 0.6F, 0.2F));
        PartDefinition p_left_shin = p_left_leg.addOrReplaceChild("left_shin", CubeListBuilder.create()
                .texOffs(279, 0).addBox(-2F, -0.2F, -2.5F, 4F, 10F, 5F, new CubeDeformation(0F, -0.2F, -0.4F))
                .texOffs(96, 79).addBox(-2.5F, -2.1F, -2.75F, 5F, 4F, 2F, new CubeDeformation(-0.15F, -0.4F, -0.05F))
                .texOffs(204, 45).addBox(2F, 2.9F, -1.8F, 0F, 6F, 5F, new CubeDeformation(0F, -0.3F, -0.2F)),
                PartPose.offset(0F, 11F, 0F));
        PartDefinition p_left_foot = p_left_shin.addOrReplaceChild("left_foot", CubeListBuilder.create()
                .texOffs(50, 58).addBox(-2.5F, -0.1F, -4.7F, 5F, 3F, 7F, new CubeDeformation(-0.35F, -0.1F, -0.4F))
                .texOffs(210, 115).addBox(-1.5F, 0.85F, -5.95F, 3F, 2F, 2F, new CubeDeformation(0F, -0.05F, -0.25F)),
                PartPose.offset(0F, 9.6F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(244, 0).addBox(-2.5F, 0F, -2.5F, 5F, 11F, 5F, new CubeDeformation(-0.4F, 0F, -0.4F)),
                PartPose.offset(-3F, 0.6F, 0.2F));
        PartDefinition p_right_shin = p_right_leg.addOrReplaceChild("right_shin", CubeListBuilder.create()
                .texOffs(298, 0).addBox(-2F, -0.2F, -2.5F, 4F, 10F, 5F, new CubeDeformation(0F, -0.2F, -0.4F))
                .texOffs(111, 79).addBox(-2.5F, -2.1F, -2.75F, 5F, 4F, 2F, new CubeDeformation(-0.15F, -0.4F, -0.05F))
                .texOffs(215, 45).addBox(-2F, 2.9F, -1.8F, 0F, 6F, 5F, new CubeDeformation(0F, -0.3F, -0.2F)),
                PartPose.offset(0F, 11F, 0F));
        PartDefinition p_right_foot = p_right_shin.addOrReplaceChild("right_foot", CubeListBuilder.create()
                .texOffs(75, 58).addBox(-2.5F, -0.1F, -4.7F, 5F, 3F, 7F, new CubeDeformation(-0.35F, -0.1F, -0.4F))
                .texOffs(221, 115).addBox(-1.5F, 0.85F, -5.95F, 3F, 2F, 2F, new CubeDeformation(0F, -0.05F, -0.25F)),
                PartPose.offset(0F, 9.6F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
