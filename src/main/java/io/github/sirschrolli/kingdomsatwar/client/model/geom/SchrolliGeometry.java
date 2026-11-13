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
                .texOffs(97, 71).addBox(-6F, -15.9F, -3.8F, 12F, 8F, 1F, new CubeDeformation(-0.1F, -0.3F, 0F))
                .texOffs(124, 71).addBox(-6F, -15.9F, 2.85F, 12F, 8F, 1F, new CubeDeformation(-0.1F, -0.3F, -0.05F))
                .texOffs(105, 59).addBox(-7F, -16.6F, -4F, 14F, 2F, 8F, new CubeDeformation(-0.4F, 0F, -0.4F))
                .texOffs(228, 45).addBox(-6.5F, -2.6F, -4F, 13F, 3F, 8F, new CubeDeformation(-0.2F, -0.2F, -0.3F))
                .texOffs(97, 139).addBox(-1.5F, -2.6F, -4.45F, 3F, 3F, 1F, new CubeDeformation(-0.2F, -0.3F, -0.2F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(190, 0).addBox(-4F, -8F, -4F, 8F, 8F, 8F)
                .texOffs(0, 59).addBox(-4.5F, -9F, -4.5F, 9F, 2F, 9F, new CubeDeformation(-0.1F, -0.2F, -0.1F))
                .texOffs(71, 129).addBox(-4.5F, -8.7F, -4.8F, 9F, 3F, 1F, new CubeDeformation(-0.3F, -0.4F, -0.1F))
                .texOffs(0, 29).addBox(-4.8F, -7.9F, -3.5F, 1F, 8F, 7F, new CubeDeformation(0F, -0.3F, -0.1F))
                .texOffs(17, 29).addBox(3.8F, -7.9F, -3.5F, 1F, 8F, 7F, new CubeDeformation(0F, -0.3F, -0.1F))
                .texOffs(271, 45).addBox(-5F, -6.9F, -5F, 10F, 1F, 10F, new CubeDeformation(-0.45F, 0F, -0.45F))
                .texOffs(203, 139).addBox(-1F, -7.35F, -5.15F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.35F, -0.25F)),
                PartPose.offset(0F, -16.6F, -0.3F));
        PartDefinition p_hair_back = p_head.addOrReplaceChild("hair_back", CubeListBuilder.create()
                .texOffs(73, 45).addBox(-4.5F, -0.25F, -1F, 9F, 10F, 2F, new CubeDeformation(-0.1F, -0.25F, -0.1F)),
                PartPose.offsetAndRotation(0F, -7.6F, 3.6F, 0.1396F, 0F, 0F));
        PartDefinition p_hair_tip = p_hair_back.addOrReplaceChild("hair_tip", CubeListBuilder.create()
                .texOffs(237, 82).addBox(-4F, 0F, -1F, 8F, 5F, 2F, new CubeDeformation(0F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, 9.5F, 0F, 0.1047F, 0F, 0F));
        PartDefinition p_crown = p_head.addOrReplaceChild("crown", CubeListBuilder.create(),
                PartPose.offset(0F, -7.8F, -0.6F));
        PartDefinition p_crown_0 = p_crown.addOrReplaceChild("crown_0", CubeListBuilder.create()
                .texOffs(125, 123).addBox(-0.5F, -3.5F, -0.5F, 1F, 4F, 1F, new CubeDeformation(-0.05F, -0.5F, -0.05F)),
                PartPose.offsetAndRotation(-3.4931F, -0.6F, -0.1452F, 0F, 0F, -1.3265F));
        PartDefinition p_crown_1 = p_crown.addOrReplaceChild("crown_1", CubeListBuilder.create()
                .texOffs(130, 123).addBox(-0.5F, -3.925F, -0.5F, 1F, 4F, 1F, new CubeDeformation(-0.05F, -0.075F, -0.05F)),
                PartPose.offsetAndRotation(-3.0192F, -0.6F, -0.3268F, 0F, 0F, -0.9948F));
        PartDefinition p_crown_2 = p_crown.addOrReplaceChild("crown_2", CubeListBuilder.create()
                .texOffs(177, 91).addBox(-0.5F, -4.85F, -0.5F, 1F, 5F, 1F, new CubeDeformation(-0.05F, -0.15F, -0.05F)),
                PartPose.offsetAndRotation(-2.2164F, -0.6F, -0.4728F, 0F, 0F, -0.6632F));
        PartDefinition p_crown_3 = p_crown.addOrReplaceChild("crown_3", CubeListBuilder.create()
                .texOffs(0, 91).addBox(-0.5F, -5.775F, -0.5F, 1F, 6F, 1F, new CubeDeformation(-0.05F, -0.225F, -0.05F)),
                PartPose.offsetAndRotation(-1.172F, -0.6F, -0.5673F, 0F, 0F, -0.3316F));
        PartDefinition p_crown_4 = p_crown.addOrReplaceChild("crown_4", CubeListBuilder.create()
                .texOffs(207, 82).addBox(-0.5F, -6.7F, -0.5F, 1F, 7F, 1F, new CubeDeformation(-0.05F, -0.3F, -0.05F)),
                PartPose.offset(0F, -0.6F, -0.6F));
        PartDefinition p_crown_5 = p_crown.addOrReplaceChild("crown_5", CubeListBuilder.create()
                .texOffs(5, 91).addBox(-0.5F, -5.775F, -0.5F, 1F, 6F, 1F, new CubeDeformation(-0.05F, -0.225F, -0.05F)),
                PartPose.offsetAndRotation(1.172F, -0.6F, -0.5673F, 0F, 0F, 0.3316F));
        PartDefinition p_crown_6 = p_crown.addOrReplaceChild("crown_6", CubeListBuilder.create()
                .texOffs(182, 91).addBox(-0.5F, -4.85F, -0.5F, 1F, 5F, 1F, new CubeDeformation(-0.05F, -0.15F, -0.05F)),
                PartPose.offsetAndRotation(2.2164F, -0.6F, -0.4728F, 0F, 0F, 0.6632F));
        PartDefinition p_crown_7 = p_crown.addOrReplaceChild("crown_7", CubeListBuilder.create()
                .texOffs(135, 123).addBox(-0.5F, -3.925F, -0.5F, 1F, 4F, 1F, new CubeDeformation(-0.05F, -0.075F, -0.05F)),
                PartPose.offsetAndRotation(3.0192F, -0.6F, -0.3268F, 0F, 0F, 0.9948F));
        PartDefinition p_crown_8 = p_crown.addOrReplaceChild("crown_8", CubeListBuilder.create()
                .texOffs(140, 123).addBox(-0.5F, -3.5F, -0.5F, 1F, 4F, 1F, new CubeDeformation(-0.05F, -0.5F, -0.05F)),
                PartPose.offsetAndRotation(3.4931F, -0.6F, -0.1452F, 0F, 0F, 1.3265F));
        PartDefinition p_halo = p_head.addOrReplaceChild("halo", CubeListBuilder.create(),
                PartPose.offset(0F, -5F, 7F));
        PartDefinition p_halo_0 = p_halo.addOrReplaceChild("halo_0", CubeListBuilder.create()
                .texOffs(91, 134).addBox(-2.5F, -1F, -1F, 5F, 2F, 2F, new CubeDeformation(-0.1883F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(10.5F, 0F, 0F, 0F, 0F, 1.5708F));
        PartDefinition p_halo_1 = p_halo.addOrReplaceChild("halo_1", CubeListBuilder.create()
                .texOffs(106, 134).addBox(-2.5F, -1F, -1F, 5F, 2F, 2F, new CubeDeformation(-0.1883F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(9.7007F, 4.0182F, 0F, 0F, 0F, 1.9635F));
        PartDefinition p_halo_2 = p_halo.addOrReplaceChild("halo_2", CubeListBuilder.create()
                .texOffs(121, 134).addBox(-2.5F, -1F, -1F, 5F, 2F, 2F, new CubeDeformation(-0.1883F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(7.4246F, 7.4246F, 0F, 0F, 0F, 2.3562F));
        PartDefinition p_halo_3 = p_halo.addOrReplaceChild("halo_3", CubeListBuilder.create()
                .texOffs(136, 134).addBox(-2.5F, -1F, -1F, 5F, 2F, 2F, new CubeDeformation(-0.1883F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(4.0182F, 9.7007F, 0F, 0F, 0F, 2.7489F));
        PartDefinition p_halo_4 = p_halo.addOrReplaceChild("halo_4", CubeListBuilder.create()
                .texOffs(151, 134).addBox(-2.5F, -1F, -1F, 5F, 2F, 2F, new CubeDeformation(-0.1883F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(0F, 10.5F, 0F, 0F, 0F, 3.1416F));
        PartDefinition p_halo_5 = p_halo.addOrReplaceChild("halo_5", CubeListBuilder.create()
                .texOffs(166, 134).addBox(-2.5F, -1F, -1F, 5F, 2F, 2F, new CubeDeformation(-0.1883F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(-4.0182F, 9.7007F, 0F, 0F, 0F, 3.5343F));
        PartDefinition p_halo_6 = p_halo.addOrReplaceChild("halo_6", CubeListBuilder.create()
                .texOffs(181, 134).addBox(-2.5F, -1F, -1F, 5F, 2F, 2F, new CubeDeformation(-0.1883F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(-7.4246F, 7.4246F, 0F, 0F, 0F, 3.927F));
        PartDefinition p_halo_7 = p_halo.addOrReplaceChild("halo_7", CubeListBuilder.create()
                .texOffs(196, 134).addBox(-2.5F, -1F, -1F, 5F, 2F, 2F, new CubeDeformation(-0.1883F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(-9.7007F, 4.0182F, 0F, 0F, 0F, 4.3197F));
        PartDefinition p_halo_8 = p_halo.addOrReplaceChild("halo_8", CubeListBuilder.create()
                .texOffs(211, 134).addBox(-2.5F, -1F, -1F, 5F, 2F, 2F, new CubeDeformation(-0.1883F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(-10.5F, 0F, 0F, 0F, 0F, 4.7124F));
        PartDefinition p_halo_9 = p_halo.addOrReplaceChild("halo_9", CubeListBuilder.create()
                .texOffs(226, 134).addBox(-2.5F, -1F, -1F, 5F, 2F, 2F, new CubeDeformation(-0.1883F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(-9.7007F, -4.0182F, 0F, 0F, 0F, 5.1051F));
        PartDefinition p_halo_10 = p_halo.addOrReplaceChild("halo_10", CubeListBuilder.create()
                .texOffs(241, 134).addBox(-2.5F, -1F, -1F, 5F, 2F, 2F, new CubeDeformation(-0.1883F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(-7.4246F, -7.4246F, 0F, 0F, 0F, 5.4978F));
        PartDefinition p_halo_11 = p_halo.addOrReplaceChild("halo_11", CubeListBuilder.create()
                .texOffs(256, 134).addBox(-2.5F, -1F, -1F, 5F, 2F, 2F, new CubeDeformation(-0.1883F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(-4.0182F, -9.7007F, 0F, 0F, 0F, 5.8905F));
        PartDefinition p_halo_12 = p_halo.addOrReplaceChild("halo_12", CubeListBuilder.create()
                .texOffs(271, 134).addBox(-2.5F, -1F, -1F, 5F, 2F, 2F, new CubeDeformation(-0.1883F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(0F, -10.5F, 0F, 0F, 0F, 6.2832F));
        PartDefinition p_halo_13 = p_halo.addOrReplaceChild("halo_13", CubeListBuilder.create()
                .texOffs(286, 134).addBox(-2.5F, -1F, -1F, 5F, 2F, 2F, new CubeDeformation(-0.1883F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(4.0182F, -9.7007F, 0F, 0F, 0F, 6.6759F));
        PartDefinition p_halo_14 = p_halo.addOrReplaceChild("halo_14", CubeListBuilder.create()
                .texOffs(301, 134).addBox(-2.5F, -1F, -1F, 5F, 2F, 2F, new CubeDeformation(-0.1883F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(7.4246F, -7.4246F, 0F, 0F, 0F, 7.0686F));
        PartDefinition p_halo_15 = p_halo.addOrReplaceChild("halo_15", CubeListBuilder.create()
                .texOffs(0, 139).addBox(-2.5F, -1F, -1F, 5F, 2F, 2F, new CubeDeformation(-0.1883F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(9.7007F, -4.0182F, 0F, 0F, 0F, 7.4613F));
        PartDefinition p_halo_ray_0 = p_halo.addOrReplaceChild("halo_ray_0", CubeListBuilder.create()
                .texOffs(187, 91).addBox(-0.5F, -0.4F, -0.5F, 1F, 5F, 1F, new CubeDeformation(0F, -0.4F, -0.15F)),
                PartPose.offsetAndRotation(11.0116F, 2.9505F, 0.2F, 0F, 0F, -1.309F));
        PartDefinition p_halo_ray_1 = p_halo.addOrReplaceChild("halo_ray_1", CubeListBuilder.create()
                .texOffs(129, 139).addBox(-0.5F, -0.2F, -0.5F, 1F, 3F, 1F, new CubeDeformation(0F, -0.2F, -0.15F)),
                PartPose.offsetAndRotation(8.061F, 8.061F, 0.2F, 0F, 0F, -0.7854F));
        PartDefinition p_halo_ray_2 = p_halo.addOrReplaceChild("halo_ray_2", CubeListBuilder.create()
                .texOffs(192, 91).addBox(-0.5F, -0.4F, -0.5F, 1F, 5F, 1F, new CubeDeformation(0F, -0.4F, -0.15F)),
                PartPose.offsetAndRotation(2.9505F, 11.0116F, 0.2F, 0F, 0F, -0.2618F));
        PartDefinition p_halo_ray_3 = p_halo.addOrReplaceChild("halo_ray_3", CubeListBuilder.create()
                .texOffs(134, 139).addBox(-0.5F, -0.2F, -0.5F, 1F, 3F, 1F, new CubeDeformation(0F, -0.2F, -0.15F)),
                PartPose.offsetAndRotation(-2.9505F, 11.0116F, 0.2F, 0F, 0F, 0.2618F));
        PartDefinition p_halo_ray_4 = p_halo.addOrReplaceChild("halo_ray_4", CubeListBuilder.create()
                .texOffs(197, 91).addBox(-0.5F, -0.4F, -0.5F, 1F, 5F, 1F, new CubeDeformation(0F, -0.4F, -0.15F)),
                PartPose.offsetAndRotation(-8.061F, 8.061F, 0.2F, 0F, 0F, 0.7854F));
        PartDefinition p_halo_ray_5 = p_halo.addOrReplaceChild("halo_ray_5", CubeListBuilder.create()
                .texOffs(139, 139).addBox(-0.5F, -0.2F, -0.5F, 1F, 3F, 1F, new CubeDeformation(0F, -0.2F, -0.15F)),
                PartPose.offsetAndRotation(-11.0116F, 2.9505F, 0.2F, 0F, 0F, 1.309F));
        PartDefinition p_halo_ray_6 = p_halo.addOrReplaceChild("halo_ray_6", CubeListBuilder.create()
                .texOffs(202, 91).addBox(-0.5F, -0.4F, -0.5F, 1F, 5F, 1F, new CubeDeformation(0F, -0.4F, -0.15F)),
                PartPose.offsetAndRotation(-11.0116F, -2.9505F, 0.2F, 0F, 0F, 1.8326F));
        PartDefinition p_halo_ray_7 = p_halo.addOrReplaceChild("halo_ray_7", CubeListBuilder.create()
                .texOffs(144, 139).addBox(-0.5F, -0.2F, -0.5F, 1F, 3F, 1F, new CubeDeformation(0F, -0.2F, -0.15F)),
                PartPose.offsetAndRotation(-8.061F, -8.061F, 0.2F, 0F, 0F, 2.3562F));
        PartDefinition p_halo_ray_8 = p_halo.addOrReplaceChild("halo_ray_8", CubeListBuilder.create()
                .texOffs(207, 91).addBox(-0.5F, -0.4F, -0.5F, 1F, 5F, 1F, new CubeDeformation(0F, -0.4F, -0.15F)),
                PartPose.offsetAndRotation(-2.9505F, -11.0116F, 0.2F, 0F, 0F, 2.8798F));
        PartDefinition p_halo_ray_9 = p_halo.addOrReplaceChild("halo_ray_9", CubeListBuilder.create()
                .texOffs(149, 139).addBox(-0.5F, -0.2F, -0.5F, 1F, 3F, 1F, new CubeDeformation(0F, -0.2F, -0.15F)),
                PartPose.offsetAndRotation(2.9505F, -11.0116F, 0.2F, 0F, 0F, 3.4034F));
        PartDefinition p_halo_ray_10 = p_halo.addOrReplaceChild("halo_ray_10", CubeListBuilder.create()
                .texOffs(212, 91).addBox(-0.5F, -0.4F, -0.5F, 1F, 5F, 1F, new CubeDeformation(0F, -0.4F, -0.15F)),
                PartPose.offsetAndRotation(8.061F, -8.061F, 0.2F, 0F, 0F, 3.927F));
        PartDefinition p_halo_ray_11 = p_halo.addOrReplaceChild("halo_ray_11", CubeListBuilder.create()
                .texOffs(154, 139).addBox(-0.5F, -0.2F, -0.5F, 1F, 3F, 1F, new CubeDeformation(0F, -0.2F, -0.15F)),
                PartPose.offsetAndRotation(11.0116F, -2.9505F, 0.2F, 0F, 0F, 4.4506F));
        PartDefinition p_halo_inner = p_halo.addOrReplaceChild("halo_inner", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0.4F));
        PartDefinition p_halo_inner_0 = p_halo_inner.addOrReplaceChild("halo_inner_0", CubeListBuilder.create()
                .texOffs(290, 139).addBox(-2F, -0.5F, -0.5F, 4F, 1F, 1F, new CubeDeformation(-0.0721F, -0.1F, -0.2F)),
                PartPose.offsetAndRotation(6.6F, 0F, 0F, 0F, 0F, 1.5708F));
        PartDefinition p_halo_inner_1 = p_halo_inner.addOrReplaceChild("halo_inner_1", CubeListBuilder.create()
                .texOffs(301, 139).addBox(-2F, -0.5F, -0.5F, 4F, 1F, 1F, new CubeDeformation(-0.0721F, -0.1F, -0.2F)),
                PartPose.offsetAndRotation(5.7158F, 3.3F, 0F, 0F, 0F, 2.0944F));
        PartDefinition p_halo_inner_2 = p_halo_inner.addOrReplaceChild("halo_inner_2", CubeListBuilder.create()
                .texOffs(0, 144).addBox(-2F, -0.5F, -0.5F, 4F, 1F, 1F, new CubeDeformation(-0.0721F, -0.1F, -0.2F)),
                PartPose.offsetAndRotation(3.3F, 5.7158F, 0F, 0F, 0F, 2.618F));
        PartDefinition p_halo_inner_3 = p_halo_inner.addOrReplaceChild("halo_inner_3", CubeListBuilder.create()
                .texOffs(11, 144).addBox(-2F, -0.5F, -0.5F, 4F, 1F, 1F, new CubeDeformation(-0.0721F, -0.1F, -0.2F)),
                PartPose.offsetAndRotation(0F, 6.6F, 0F, 0F, 0F, 3.1416F));
        PartDefinition p_halo_inner_4 = p_halo_inner.addOrReplaceChild("halo_inner_4", CubeListBuilder.create()
                .texOffs(22, 144).addBox(-2F, -0.5F, -0.5F, 4F, 1F, 1F, new CubeDeformation(-0.0721F, -0.1F, -0.2F)),
                PartPose.offsetAndRotation(-3.3F, 5.7158F, 0F, 0F, 0F, 3.6652F));
        PartDefinition p_halo_inner_5 = p_halo_inner.addOrReplaceChild("halo_inner_5", CubeListBuilder.create()
                .texOffs(33, 144).addBox(-2F, -0.5F, -0.5F, 4F, 1F, 1F, new CubeDeformation(-0.0721F, -0.1F, -0.2F)),
                PartPose.offsetAndRotation(-5.7158F, 3.3F, 0F, 0F, 0F, 4.1888F));
        PartDefinition p_halo_inner_6 = p_halo_inner.addOrReplaceChild("halo_inner_6", CubeListBuilder.create()
                .texOffs(44, 144).addBox(-2F, -0.5F, -0.5F, 4F, 1F, 1F, new CubeDeformation(-0.0721F, -0.1F, -0.2F)),
                PartPose.offsetAndRotation(-6.6F, 0F, 0F, 0F, 0F, 4.7124F));
        PartDefinition p_halo_inner_7 = p_halo_inner.addOrReplaceChild("halo_inner_7", CubeListBuilder.create()
                .texOffs(55, 144).addBox(-2F, -0.5F, -0.5F, 4F, 1F, 1F, new CubeDeformation(-0.0721F, -0.1F, -0.2F)),
                PartPose.offsetAndRotation(-5.7158F, -3.3F, 0F, 0F, 0F, 5.236F));
        PartDefinition p_halo_inner_8 = p_halo_inner.addOrReplaceChild("halo_inner_8", CubeListBuilder.create()
                .texOffs(66, 144).addBox(-2F, -0.5F, -0.5F, 4F, 1F, 1F, new CubeDeformation(-0.0721F, -0.1F, -0.2F)),
                PartPose.offsetAndRotation(-3.3F, -5.7158F, 0F, 0F, 0F, 5.7596F));
        PartDefinition p_halo_inner_9 = p_halo_inner.addOrReplaceChild("halo_inner_9", CubeListBuilder.create()
                .texOffs(77, 144).addBox(-2F, -0.5F, -0.5F, 4F, 1F, 1F, new CubeDeformation(-0.0721F, -0.1F, -0.2F)),
                PartPose.offsetAndRotation(0F, -6.6F, 0F, 0F, 0F, 6.2832F));
        PartDefinition p_halo_inner_10 = p_halo_inner.addOrReplaceChild("halo_inner_10", CubeListBuilder.create()
                .texOffs(88, 144).addBox(-2F, -0.5F, -0.5F, 4F, 1F, 1F, new CubeDeformation(-0.0721F, -0.1F, -0.2F)),
                PartPose.offsetAndRotation(3.3F, -5.7158F, 0F, 0F, 0F, 6.8068F));
        PartDefinition p_halo_inner_11 = p_halo_inner.addOrReplaceChild("halo_inner_11", CubeListBuilder.create()
                .texOffs(99, 144).addBox(-2F, -0.5F, -0.5F, 4F, 1F, 1F, new CubeDeformation(-0.0721F, -0.1F, -0.2F)),
                PartPose.offsetAndRotation(5.7158F, -3.3F, 0F, 0F, 0F, 7.3304F));
        PartDefinition p_true_corona = p_halo.addOrReplaceChild("true_corona", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0.9F));
        PartDefinition p_true_corona_0 = p_true_corona.addOrReplaceChild("true_corona_0", CubeListBuilder.create()
                .texOffs(37, 71).addBox(-1F, -0.25F, 0F, 2F, 10F, 0F, new CubeDeformation(-0.1F, -0.25F, 0F)),
                PartPose.offsetAndRotation(13.4837F, 1.7752F, 0F, 0F, 0F, -1.4399F));
        PartDefinition p_true_corona_1 = p_true_corona.addOrReplaceChild("true_corona_1", CubeListBuilder.create()
                .texOffs(217, 91).addBox(-1F, -0.25F, 0F, 2F, 6F, 0F, new CubeDeformation(-0.1F, -0.25F, 0F)),
                PartPose.offsetAndRotation(12.5648F, 5.2045F, 0F, 0F, 0F, -1.1781F));
        PartDefinition p_true_corona_2 = p_true_corona.addOrReplaceChild("true_corona_2", CubeListBuilder.create()
                .texOffs(42, 71).addBox(-1F, -0.25F, 0F, 2F, 10F, 0F, new CubeDeformation(-0.1F, -0.25F, 0F)),
                PartPose.offsetAndRotation(10.7896F, 8.2792F, 0F, 0F, 0F, -0.9163F));
        PartDefinition p_true_corona_3 = p_true_corona.addOrReplaceChild("true_corona_3", CubeListBuilder.create()
                .texOffs(222, 91).addBox(-1F, -0.25F, 0F, 2F, 6F, 0F, new CubeDeformation(-0.1F, -0.25F, 0F)),
                PartPose.offsetAndRotation(8.2792F, 10.7896F, 0F, 0F, 0F, -0.6545F));
        PartDefinition p_true_corona_4 = p_true_corona.addOrReplaceChild("true_corona_4", CubeListBuilder.create()
                .texOffs(47, 71).addBox(-1F, -0.25F, 0F, 2F, 10F, 0F, new CubeDeformation(-0.1F, -0.25F, 0F)),
                PartPose.offsetAndRotation(5.2045F, 12.5648F, 0F, 0F, 0F, -0.3927F));
        PartDefinition p_true_corona_5 = p_true_corona.addOrReplaceChild("true_corona_5", CubeListBuilder.create()
                .texOffs(227, 91).addBox(-1F, -0.25F, 0F, 2F, 6F, 0F, new CubeDeformation(-0.1F, -0.25F, 0F)),
                PartPose.offsetAndRotation(1.7752F, 13.4837F, 0F, 0F, 0F, -0.1309F));
        PartDefinition p_true_corona_6 = p_true_corona.addOrReplaceChild("true_corona_6", CubeListBuilder.create()
                .texOffs(52, 71).addBox(-1F, -0.25F, 0F, 2F, 10F, 0F, new CubeDeformation(-0.1F, -0.25F, 0F)),
                PartPose.offsetAndRotation(-1.7752F, 13.4837F, 0F, 0F, 0F, 0.1309F));
        PartDefinition p_true_corona_7 = p_true_corona.addOrReplaceChild("true_corona_7", CubeListBuilder.create()
                .texOffs(232, 91).addBox(-1F, -0.25F, 0F, 2F, 6F, 0F, new CubeDeformation(-0.1F, -0.25F, 0F)),
                PartPose.offsetAndRotation(-5.2045F, 12.5648F, 0F, 0F, 0F, 0.3927F));
        PartDefinition p_true_corona_8 = p_true_corona.addOrReplaceChild("true_corona_8", CubeListBuilder.create()
                .texOffs(57, 71).addBox(-1F, -0.25F, 0F, 2F, 10F, 0F, new CubeDeformation(-0.1F, -0.25F, 0F)),
                PartPose.offsetAndRotation(-8.2792F, 10.7896F, 0F, 0F, 0F, 0.6545F));
        PartDefinition p_true_corona_9 = p_true_corona.addOrReplaceChild("true_corona_9", CubeListBuilder.create()
                .texOffs(237, 91).addBox(-1F, -0.25F, 0F, 2F, 6F, 0F, new CubeDeformation(-0.1F, -0.25F, 0F)),
                PartPose.offsetAndRotation(-10.7896F, 8.2792F, 0F, 0F, 0F, 0.9163F));
        PartDefinition p_true_corona_10 = p_true_corona.addOrReplaceChild("true_corona_10", CubeListBuilder.create()
                .texOffs(62, 71).addBox(-1F, -0.25F, 0F, 2F, 10F, 0F, new CubeDeformation(-0.1F, -0.25F, 0F)),
                PartPose.offsetAndRotation(-12.5648F, 5.2045F, 0F, 0F, 0F, 1.1781F));
        PartDefinition p_true_corona_11 = p_true_corona.addOrReplaceChild("true_corona_11", CubeListBuilder.create()
                .texOffs(242, 91).addBox(-1F, -0.25F, 0F, 2F, 6F, 0F, new CubeDeformation(-0.1F, -0.25F, 0F)),
                PartPose.offsetAndRotation(-13.4837F, 1.7752F, 0F, 0F, 0F, 1.4399F));
        PartDefinition p_true_corona_12 = p_true_corona.addOrReplaceChild("true_corona_12", CubeListBuilder.create()
                .texOffs(67, 71).addBox(-1F, -0.25F, 0F, 2F, 10F, 0F, new CubeDeformation(-0.1F, -0.25F, 0F)),
                PartPose.offsetAndRotation(-13.4837F, -1.7752F, 0F, 0F, 0F, 1.7017F));
        PartDefinition p_true_corona_13 = p_true_corona.addOrReplaceChild("true_corona_13", CubeListBuilder.create()
                .texOffs(247, 91).addBox(-1F, -0.25F, 0F, 2F, 6F, 0F, new CubeDeformation(-0.1F, -0.25F, 0F)),
                PartPose.offsetAndRotation(-12.5648F, -5.2045F, 0F, 0F, 0F, 1.9635F));
        PartDefinition p_true_corona_14 = p_true_corona.addOrReplaceChild("true_corona_14", CubeListBuilder.create()
                .texOffs(72, 71).addBox(-1F, -0.25F, 0F, 2F, 10F, 0F, new CubeDeformation(-0.1F, -0.25F, 0F)),
                PartPose.offsetAndRotation(-10.7896F, -8.2792F, 0F, 0F, 0F, 2.2253F));
        PartDefinition p_true_corona_15 = p_true_corona.addOrReplaceChild("true_corona_15", CubeListBuilder.create()
                .texOffs(252, 91).addBox(-1F, -0.25F, 0F, 2F, 6F, 0F, new CubeDeformation(-0.1F, -0.25F, 0F)),
                PartPose.offsetAndRotation(-8.2792F, -10.7896F, 0F, 0F, 0F, 2.4871F));
        PartDefinition p_true_corona_16 = p_true_corona.addOrReplaceChild("true_corona_16", CubeListBuilder.create()
                .texOffs(77, 71).addBox(-1F, -0.25F, 0F, 2F, 10F, 0F, new CubeDeformation(-0.1F, -0.25F, 0F)),
                PartPose.offsetAndRotation(-5.2045F, -12.5648F, 0F, 0F, 0F, 2.7489F));
        PartDefinition p_true_corona_17 = p_true_corona.addOrReplaceChild("true_corona_17", CubeListBuilder.create()
                .texOffs(257, 91).addBox(-1F, -0.25F, 0F, 2F, 6F, 0F, new CubeDeformation(-0.1F, -0.25F, 0F)),
                PartPose.offsetAndRotation(-1.7752F, -13.4837F, 0F, 0F, 0F, 3.0107F));
        PartDefinition p_true_corona_18 = p_true_corona.addOrReplaceChild("true_corona_18", CubeListBuilder.create()
                .texOffs(82, 71).addBox(-1F, -0.25F, 0F, 2F, 10F, 0F, new CubeDeformation(-0.1F, -0.25F, 0F)),
                PartPose.offsetAndRotation(1.7752F, -13.4837F, 0F, 0F, 0F, 3.2725F));
        PartDefinition p_true_corona_19 = p_true_corona.addOrReplaceChild("true_corona_19", CubeListBuilder.create()
                .texOffs(262, 91).addBox(-1F, -0.25F, 0F, 2F, 6F, 0F, new CubeDeformation(-0.1F, -0.25F, 0F)),
                PartPose.offsetAndRotation(5.2045F, -12.5648F, 0F, 0F, 0F, 3.5343F));
        PartDefinition p_true_corona_20 = p_true_corona.addOrReplaceChild("true_corona_20", CubeListBuilder.create()
                .texOffs(87, 71).addBox(-1F, -0.25F, 0F, 2F, 10F, 0F, new CubeDeformation(-0.1F, -0.25F, 0F)),
                PartPose.offsetAndRotation(8.2792F, -10.7896F, 0F, 0F, 0F, 3.7961F));
        PartDefinition p_true_corona_21 = p_true_corona.addOrReplaceChild("true_corona_21", CubeListBuilder.create()
                .texOffs(267, 91).addBox(-1F, -0.25F, 0F, 2F, 6F, 0F, new CubeDeformation(-0.1F, -0.25F, 0F)),
                PartPose.offsetAndRotation(10.7896F, -8.2792F, 0F, 0F, 0F, 4.0579F));
        PartDefinition p_true_corona_22 = p_true_corona.addOrReplaceChild("true_corona_22", CubeListBuilder.create()
                .texOffs(92, 71).addBox(-1F, -0.25F, 0F, 2F, 10F, 0F, new CubeDeformation(-0.1F, -0.25F, 0F)),
                PartPose.offsetAndRotation(12.5648F, -5.2045F, 0F, 0F, 0F, 4.3197F));
        PartDefinition p_true_corona_23 = p_true_corona.addOrReplaceChild("true_corona_23", CubeListBuilder.create()
                .texOffs(272, 91).addBox(-1F, -0.25F, 0F, 2F, 6F, 0F, new CubeDeformation(-0.1F, -0.25F, 0F)),
                PartPose.offsetAndRotation(13.4837F, -1.7752F, 0F, 0F, 0F, 4.5815F));
        PartDefinition p_almighty_flames = p_halo.addOrReplaceChild("almighty_flames", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 1.4F));
        PartDefinition p_almighty_flame_0 = p_almighty_flames.addOrReplaceChild("almighty_flame_0", CubeListBuilder.create()
                .texOffs(34, 29).addBox(-2F, 0F, 0F, 4F, 15F, 0F, new CubeDeformation(-0.4F, 0F, 0F)),
                PartPose.offsetAndRotation(17F, 0F, 0F, 0F, 0F, -1.5708F));
        PartDefinition p_almighty_flame_1 = p_almighty_flames.addOrReplaceChild("almighty_flame_1", CubeListBuilder.create()
                .texOffs(151, 71).addBox(-2F, 0F, 0F, 4F, 9F, 0F, new CubeDeformation(-0.4F, 0F, 0F)),
                PartPose.offsetAndRotation(16.6733F, 3.3165F, 0F, 0F, 0F, -1.3744F));
        PartDefinition p_almighty_flame_2 = p_almighty_flames.addOrReplaceChild("almighty_flame_2", CubeListBuilder.create()
                .texOffs(96, 45).addBox(-2F, 0F, 0F, 4F, 12F, 0F, new CubeDeformation(-0.4F, 0F, 0F)),
                PartPose.offsetAndRotation(15.706F, 6.5056F, 0F, 0F, 0F, -1.1781F));
        PartDefinition p_almighty_flame_3 = p_almighty_flames.addOrReplaceChild("almighty_flame_3", CubeListBuilder.create()
                .texOffs(89, 82).addBox(-2F, 0F, 0F, 4F, 8F, 0F, new CubeDeformation(-0.4F, 0F, 0F)),
                PartPose.offsetAndRotation(14.135F, 9.4447F, 0F, 0F, 0F, -0.9817F));
        PartDefinition p_almighty_flame_4 = p_almighty_flames.addOrReplaceChild("almighty_flame_4", CubeListBuilder.create()
                .texOffs(43, 29).addBox(-2F, 0F, 0F, 4F, 15F, 0F, new CubeDeformation(-0.4F, 0F, 0F)),
                PartPose.offsetAndRotation(12.0208F, 12.0208F, 0F, 0F, 0F, -0.7854F));
        PartDefinition p_almighty_flame_5 = p_almighty_flames.addOrReplaceChild("almighty_flame_5", CubeListBuilder.create()
                .texOffs(160, 71).addBox(-2F, 0F, 0F, 4F, 9F, 0F, new CubeDeformation(-0.4F, 0F, 0F)),
                PartPose.offsetAndRotation(9.4447F, 14.135F, 0F, 0F, 0F, -0.589F));
        PartDefinition p_almighty_flame_6 = p_almighty_flames.addOrReplaceChild("almighty_flame_6", CubeListBuilder.create()
                .texOffs(105, 45).addBox(-2F, 0F, 0F, 4F, 12F, 0F, new CubeDeformation(-0.4F, 0F, 0F)),
                PartPose.offsetAndRotation(6.5056F, 15.706F, 0F, 0F, 0F, -0.3927F));
        PartDefinition p_almighty_flame_7 = p_almighty_flames.addOrReplaceChild("almighty_flame_7", CubeListBuilder.create()
                .texOffs(98, 82).addBox(-2F, 0F, 0F, 4F, 8F, 0F, new CubeDeformation(-0.4F, 0F, 0F)),
                PartPose.offsetAndRotation(3.3165F, 16.6733F, 0F, 0F, 0F, -0.1963F));
        PartDefinition p_almighty_flame_8 = p_almighty_flames.addOrReplaceChild("almighty_flame_8", CubeListBuilder.create()
                .texOffs(52, 29).addBox(-2F, 0F, 0F, 4F, 15F, 0F, new CubeDeformation(-0.4F, 0F, 0F)),
                PartPose.offset(0F, 17F, 0F));
        PartDefinition p_almighty_flame_9 = p_almighty_flames.addOrReplaceChild("almighty_flame_9", CubeListBuilder.create()
                .texOffs(169, 71).addBox(-2F, 0F, 0F, 4F, 9F, 0F, new CubeDeformation(-0.4F, 0F, 0F)),
                PartPose.offsetAndRotation(-3.3165F, 16.6733F, 0F, 0F, 0F, 0.1963F));
        PartDefinition p_almighty_flame_10 = p_almighty_flames.addOrReplaceChild("almighty_flame_10", CubeListBuilder.create()
                .texOffs(114, 45).addBox(-2F, 0F, 0F, 4F, 12F, 0F, new CubeDeformation(-0.4F, 0F, 0F)),
                PartPose.offsetAndRotation(-6.5056F, 15.706F, 0F, 0F, 0F, 0.3927F));
        PartDefinition p_almighty_flame_11 = p_almighty_flames.addOrReplaceChild("almighty_flame_11", CubeListBuilder.create()
                .texOffs(107, 82).addBox(-2F, 0F, 0F, 4F, 8F, 0F, new CubeDeformation(-0.4F, 0F, 0F)),
                PartPose.offsetAndRotation(-9.4447F, 14.135F, 0F, 0F, 0F, 0.589F));
        PartDefinition p_almighty_flame_12 = p_almighty_flames.addOrReplaceChild("almighty_flame_12", CubeListBuilder.create()
                .texOffs(61, 29).addBox(-2F, 0F, 0F, 4F, 15F, 0F, new CubeDeformation(-0.4F, 0F, 0F)),
                PartPose.offsetAndRotation(-12.0208F, 12.0208F, 0F, 0F, 0F, 0.7854F));
        PartDefinition p_almighty_flame_13 = p_almighty_flames.addOrReplaceChild("almighty_flame_13", CubeListBuilder.create()
                .texOffs(178, 71).addBox(-2F, 0F, 0F, 4F, 9F, 0F, new CubeDeformation(-0.4F, 0F, 0F)),
                PartPose.offsetAndRotation(-14.135F, 9.4447F, 0F, 0F, 0F, 0.9817F));
        PartDefinition p_almighty_flame_14 = p_almighty_flames.addOrReplaceChild("almighty_flame_14", CubeListBuilder.create()
                .texOffs(123, 45).addBox(-2F, 0F, 0F, 4F, 12F, 0F, new CubeDeformation(-0.4F, 0F, 0F)),
                PartPose.offsetAndRotation(-15.706F, 6.5056F, 0F, 0F, 0F, 1.1781F));
        PartDefinition p_almighty_flame_15 = p_almighty_flames.addOrReplaceChild("almighty_flame_15", CubeListBuilder.create()
                .texOffs(116, 82).addBox(-2F, 0F, 0F, 4F, 8F, 0F, new CubeDeformation(-0.4F, 0F, 0F)),
                PartPose.offsetAndRotation(-16.6733F, 3.3165F, 0F, 0F, 0F, 1.3744F));
        PartDefinition p_almighty_flame_16 = p_almighty_flames.addOrReplaceChild("almighty_flame_16", CubeListBuilder.create()
                .texOffs(70, 29).addBox(-2F, 0F, 0F, 4F, 15F, 0F, new CubeDeformation(-0.4F, 0F, 0F)),
                PartPose.offsetAndRotation(-17F, 0F, 0F, 0F, 0F, 1.5708F));
        PartDefinition p_almighty_flame_17 = p_almighty_flames.addOrReplaceChild("almighty_flame_17", CubeListBuilder.create()
                .texOffs(187, 71).addBox(-2F, 0F, 0F, 4F, 9F, 0F, new CubeDeformation(-0.4F, 0F, 0F)),
                PartPose.offsetAndRotation(-16.6733F, -3.3165F, 0F, 0F, 0F, 1.7671F));
        PartDefinition p_almighty_flame_18 = p_almighty_flames.addOrReplaceChild("almighty_flame_18", CubeListBuilder.create()
                .texOffs(132, 45).addBox(-2F, 0F, 0F, 4F, 12F, 0F, new CubeDeformation(-0.4F, 0F, 0F)),
                PartPose.offsetAndRotation(-15.706F, -6.5056F, 0F, 0F, 0F, 1.9635F));
        PartDefinition p_almighty_flame_19 = p_almighty_flames.addOrReplaceChild("almighty_flame_19", CubeListBuilder.create()
                .texOffs(125, 82).addBox(-2F, 0F, 0F, 4F, 8F, 0F, new CubeDeformation(-0.4F, 0F, 0F)),
                PartPose.offsetAndRotation(-14.135F, -9.4447F, 0F, 0F, 0F, 2.1598F));
        PartDefinition p_almighty_flame_20 = p_almighty_flames.addOrReplaceChild("almighty_flame_20", CubeListBuilder.create()
                .texOffs(79, 29).addBox(-2F, 0F, 0F, 4F, 15F, 0F, new CubeDeformation(-0.4F, 0F, 0F)),
                PartPose.offsetAndRotation(-12.0208F, -12.0208F, 0F, 0F, 0F, 2.3562F));
        PartDefinition p_almighty_flame_21 = p_almighty_flames.addOrReplaceChild("almighty_flame_21", CubeListBuilder.create()
                .texOffs(196, 71).addBox(-2F, 0F, 0F, 4F, 9F, 0F, new CubeDeformation(-0.4F, 0F, 0F)),
                PartPose.offsetAndRotation(-9.4447F, -14.135F, 0F, 0F, 0F, 2.5525F));
        PartDefinition p_almighty_flame_22 = p_almighty_flames.addOrReplaceChild("almighty_flame_22", CubeListBuilder.create()
                .texOffs(141, 45).addBox(-2F, 0F, 0F, 4F, 12F, 0F, new CubeDeformation(-0.4F, 0F, 0F)),
                PartPose.offsetAndRotation(-6.5056F, -15.706F, 0F, 0F, 0F, 2.7489F));
        PartDefinition p_almighty_flame_23 = p_almighty_flames.addOrReplaceChild("almighty_flame_23", CubeListBuilder.create()
                .texOffs(134, 82).addBox(-2F, 0F, 0F, 4F, 8F, 0F, new CubeDeformation(-0.4F, 0F, 0F)),
                PartPose.offsetAndRotation(-3.3165F, -16.6733F, 0F, 0F, 0F, 2.9452F));
        PartDefinition p_almighty_flame_24 = p_almighty_flames.addOrReplaceChild("almighty_flame_24", CubeListBuilder.create()
                .texOffs(88, 29).addBox(-2F, 0F, 0F, 4F, 15F, 0F, new CubeDeformation(-0.4F, 0F, 0F)),
                PartPose.offsetAndRotation(0F, -17F, 0F, 0F, 0F, 3.1416F));
        PartDefinition p_almighty_flame_25 = p_almighty_flames.addOrReplaceChild("almighty_flame_25", CubeListBuilder.create()
                .texOffs(205, 71).addBox(-2F, 0F, 0F, 4F, 9F, 0F, new CubeDeformation(-0.4F, 0F, 0F)),
                PartPose.offsetAndRotation(3.3165F, -16.6733F, 0F, 0F, 0F, 3.3379F));
        PartDefinition p_almighty_flame_26 = p_almighty_flames.addOrReplaceChild("almighty_flame_26", CubeListBuilder.create()
                .texOffs(150, 45).addBox(-2F, 0F, 0F, 4F, 12F, 0F, new CubeDeformation(-0.4F, 0F, 0F)),
                PartPose.offsetAndRotation(6.5056F, -15.706F, 0F, 0F, 0F, 3.5343F));
        PartDefinition p_almighty_flame_27 = p_almighty_flames.addOrReplaceChild("almighty_flame_27", CubeListBuilder.create()
                .texOffs(143, 82).addBox(-2F, 0F, 0F, 4F, 8F, 0F, new CubeDeformation(-0.4F, 0F, 0F)),
                PartPose.offsetAndRotation(9.4447F, -14.135F, 0F, 0F, 0F, 3.7306F));
        PartDefinition p_almighty_flame_28 = p_almighty_flames.addOrReplaceChild("almighty_flame_28", CubeListBuilder.create()
                .texOffs(97, 29).addBox(-2F, 0F, 0F, 4F, 15F, 0F, new CubeDeformation(-0.4F, 0F, 0F)),
                PartPose.offsetAndRotation(12.0208F, -12.0208F, 0F, 0F, 0F, 3.927F));
        PartDefinition p_almighty_flame_29 = p_almighty_flames.addOrReplaceChild("almighty_flame_29", CubeListBuilder.create()
                .texOffs(214, 71).addBox(-2F, 0F, 0F, 4F, 9F, 0F, new CubeDeformation(-0.4F, 0F, 0F)),
                PartPose.offsetAndRotation(14.135F, -9.4447F, 0F, 0F, 0F, 4.1233F));
        PartDefinition p_almighty_flame_30 = p_almighty_flames.addOrReplaceChild("almighty_flame_30", CubeListBuilder.create()
                .texOffs(159, 45).addBox(-2F, 0F, 0F, 4F, 12F, 0F, new CubeDeformation(-0.4F, 0F, 0F)),
                PartPose.offsetAndRotation(15.706F, -6.5056F, 0F, 0F, 0F, 4.3197F));
        PartDefinition p_almighty_flame_31 = p_almighty_flames.addOrReplaceChild("almighty_flame_31", CubeListBuilder.create()
                .texOffs(152, 82).addBox(-2F, 0F, 0F, 4F, 8F, 0F, new CubeDeformation(-0.4F, 0F, 0F)),
                PartPose.offsetAndRotation(16.6733F, -3.3165F, 0F, 0F, 0F, 4.516F));
        PartDefinition p_left_arm = p_body.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(196, 29).addBox(-2.5F, -0.1F, -2.5F, 5F, 9F, 5F, new CubeDeformation(-0.2F, -0.3F, -0.1F))
                .texOffs(150, 59).addBox(-3F, -2.6F, -3F, 6F, 4F, 6F, new CubeDeformation(-0.1F, -0.2F, 0F))
                .texOffs(10, 91).addBox(-3.5F, -3.1F, -2.5F, 7F, 1F, 5F, new CubeDeformation(-0.4F, 0F, -0.3F)),
                PartPose.offsetAndRotation(7.2F, -14.4F, 0F, -0.3142F, 0F, -0.1396F));
        PartDefinition p_left_forearm = p_left_arm.addOrReplaceChild("left_forearm", CubeListBuilder.create()
                .texOffs(274, 71).addBox(-2.5F, -0.1F, -2.5F, 5F, 3F, 5F, new CubeDeformation(-0.3F, -0.1F, -0.3F))
                .texOffs(21, 82).addBox(-2F, 2.3F, -2F, 4F, 4F, 4F, new CubeDeformation(-0.1F, -0.3F, -0.1F))
                .texOffs(38, 82).addBox(-2F, 5.6F, -2F, 4F, 4F, 4F, new CubeDeformation(-0.4F, -0.4F, -0.3F)),
                PartPose.offsetAndRotation(0F, 8.4F, 0F, -1.0123F, 0F, 0F));
        PartDefinition p_orb = p_left_forearm.addOrReplaceChild("orb", CubeListBuilder.create()
                .texOffs(250, 59).addBox(-2.5F, -2.5F, -2.5F, 5F, 5F, 5F, new CubeDeformation(-0.1F, -0.1F, -0.1F))
                .texOffs(292, 59).addBox(-2F, -3F, -2F, 4F, 6F, 4F, new CubeDeformation(-0.2F, -0.1F, -0.2F))
                .texOffs(295, 71).addBox(-3F, -2F, -2F, 6F, 4F, 4F, new CubeDeformation(-0.1F, -0.2F, -0.2F))
                .texOffs(271, 59).addBox(-2F, -2F, -3F, 4F, 4F, 6F, new CubeDeformation(-0.2F, -0.2F, -0.1F))
                .texOffs(212, 82).addBox(-3F, -0.5F, -3F, 6F, 1F, 6F, new CubeDeformation(0F, -0.1F, 0F))
                .texOffs(159, 139).addBox(-0.5F, -5.4F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.15F, -0.4F, -0.15F))
                .texOffs(121, 144).addBox(-1F, -4.6F, -0.5F, 2F, 1F, 1F, new CubeDeformation(0F, -0.2F, -0.15F)),
                PartPose.offsetAndRotation(0F, 10.4F, -1.2F, 1.3265F, 0F, 0.1396F));
        PartDefinition p_right_arm = p_body.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(217, 29).addBox(-2.5F, -0.1F, -2.5F, 5F, 9F, 5F, new CubeDeformation(-0.2F, -0.3F, -0.1F))
                .texOffs(175, 59).addBox(-3F, -2.6F, -3F, 6F, 4F, 6F, new CubeDeformation(-0.1F, -0.2F, 0F))
                .texOffs(35, 91).addBox(-3.5F, -3.1F, -2.5F, 7F, 1F, 5F, new CubeDeformation(-0.4F, 0F, -0.3F)),
                PartPose.offsetAndRotation(-7.2F, -14.4F, 0F, -0.4538F, 0F, 0.1745F));
        PartDefinition p_right_forearm = p_right_arm.addOrReplaceChild("right_forearm", CubeListBuilder.create()
                .texOffs(0, 82).addBox(-2.5F, -0.1F, -2.5F, 5F, 3F, 5F, new CubeDeformation(-0.3F, -0.1F, -0.3F))
                .texOffs(55, 82).addBox(-2F, 2.3F, -2F, 4F, 4F, 4F, new CubeDeformation(-0.1F, -0.3F, -0.1F))
                .texOffs(72, 82).addBox(-2F, 5.6F, -2F, 4F, 4F, 4F, new CubeDeformation(-0.4F, -0.4F, -0.3F)),
                PartPose.offsetAndRotation(0F, 8.4F, 0F, -0.5934F, 0F, 0F));
        PartDefinition p_scepter = p_right_forearm.addOrReplaceChild("scepter", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-1F, -15F, -1F, 2F, 26F, 2F, new CubeDeformation(-0.4F, 0F, -0.4F))
                .texOffs(106, 139).addBox(-1F, 9.8F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.1F, -0.2F, -0.1F)),
                PartPose.offsetAndRotation(0F, 7.6F, -0.4F, 1.0472F, 0F, -0.1745F));
        PartDefinition p_scepter_head = p_scepter.addOrReplaceChild("scepter_head", CubeListBuilder.create()
                .texOffs(105, 91).addBox(-1.5F, -1.5F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.1F, -0.1F, -0.1F))
                .texOffs(110, 144).addBox(-2F, 1.3F, -0.5F, 4F, 1F, 1F, new CubeDeformation(0F, -0.1F, 0F)),
                PartPose.offset(0F, -16.8F, 0F));
        PartDefinition p_scepter_star_0 = p_scepter_head.addOrReplaceChild("scepter_star_0", CubeListBuilder.create()
                .texOffs(222, 139).addBox(-4F, -0.5F, -0.5F, 8F, 1F, 1F, new CubeDeformation(0F, -0.05F, -0.15F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_scepter_star_1 = p_scepter_head.addOrReplaceChild("scepter_star_1", CubeListBuilder.create()
                .texOffs(260, 139).addBox(-3F, -0.5F, -0.5F, 6F, 1F, 1F, new CubeDeformation(-0.2F, -0.05F, -0.15F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0.7854F));
        PartDefinition p_scepter_star_2 = p_scepter_head.addOrReplaceChild("scepter_star_2", CubeListBuilder.create()
                .texOffs(241, 139).addBox(-4F, -0.5F, -0.5F, 8F, 1F, 1F, new CubeDeformation(0F, -0.05F, -0.15F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 1.5708F));
        PartDefinition p_scepter_star_3 = p_scepter_head.addOrReplaceChild("scepter_star_3", CubeListBuilder.create()
                .texOffs(275, 139).addBox(-3F, -0.5F, -0.5F, 6F, 1F, 1F, new CubeDeformation(-0.2F, -0.05F, -0.15F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 2.3562F));
        PartDefinition p_blade = p_right_forearm.addOrReplaceChild("blade", CubeListBuilder.create()
                .texOffs(223, 71).addBox(-1F, -2.9F, -1F, 2F, 7F, 2F, new CubeDeformation(-0.25F, -0.3F, -0.25F))
                .texOffs(118, 91).addBox(-1.5F, 3.5F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.3F, -0.3F, -0.3F))
                .texOffs(145, 123).addBox(-5.5F, -4.3F, -1F, 11F, 2F, 2F, new CubeDeformation(-0.1F, -0.3F, -0.1F))
                .texOffs(60, 91).addBox(-2F, -4.9F, -1.5F, 4F, 3F, 3F, new CubeDeformation(-0.3F, -0.1F, -0.35F)),
                PartPose.offsetAndRotation(0F, 7.6F, -0.4F, 1.0472F, 0F, -0.1745F));
        PartDefinition p_blade_guard_r = p_blade.addOrReplaceChild("blade_guard_r", CubeListBuilder.create()
                .texOffs(276, 82).addBox(-0.5F, -4.8F, -1F, 1F, 5F, 2F, new CubeDeformation(0F, -0.2F, -0.45F)),
                PartPose.offsetAndRotation(-5F, -3.4F, 0F, 0F, 0F, 0.6632F));
        PartDefinition p_blade_guard_l = p_blade.addOrReplaceChild("blade_guard_l", CubeListBuilder.create()
                .texOffs(283, 82).addBox(-0.5F, -4.8F, -1F, 1F, 5F, 2F, new CubeDeformation(0F, -0.2F, -0.45F)),
                PartPose.offsetAndRotation(5F, -3.4F, 0F, 0F, 0F, -0.6632F));
        PartDefinition p_blade_edge = p_blade.addOrReplaceChild("blade_edge", CubeListBuilder.create()
                .texOffs(9, 0).addBox(-2F, -26F, -0.5F, 4F, 26F, 1F, new CubeDeformation(-0.3F, 0F, -0.1F))
                .texOffs(20, 0).addBox(-0.5F, -25.2F, -0.5F, 1F, 25F, 1F, new CubeDeformation(-0.05F, -0.2F, 0F))
                .texOffs(194, 139).addBox(-1.5F, -28F, -0.5F, 3F, 2F, 1F, new CubeDeformation(-0.25F, 0F, -0.15F))
                .texOffs(210, 139).addBox(-1F, -29.85F, -0.5F, 2F, 2F, 1F, new CubeDeformation(-0.25F, -0.15F, -0.2F))
                .texOffs(217, 139).addBox(-0.5F, -31.35F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.2F, -0.35F, -0.25F)),
                PartPose.offset(0F, -4.8F, 0F));
        PartDefinition p_robe = p_body.addOrReplaceChild("robe", CubeListBuilder.create()
                .texOffs(120, 29).addBox(-6.5F, 0F, -4F, 13F, 6F, 8F, new CubeDeformation(-0.3F, 0F, -0.2F)),
                PartPose.offset(0F, 0.4F, 0F));
        PartDefinition p_robe_front = p_robe.addOrReplaceChild("robe_front", CubeListBuilder.create()
                .texOffs(7, 45).addBox(-7F, 0F, -1F, 14F, 10F, 2F, new CubeDeformation(0F, 0F, -0.4F)),
                PartPose.offsetAndRotation(0F, 0F, -4F, -0.1745F, 0F, 0F));
        PartDefinition p_robe_front_low = p_robe_front.addOrReplaceChild("robe_front_low", CubeListBuilder.create()
                .texOffs(266, 29).addBox(-7F, 0F, -1F, 14F, 11F, 2F, new CubeDeformation(0F, 0F, -0.4F)),
                PartPose.offset(0F, 10F, 0F));
        PartDefinition p_robe_back = p_robe.addOrReplaceChild("robe_back", CubeListBuilder.create()
                .texOffs(40, 45).addBox(-7F, 0F, -1F, 14F, 10F, 2F, new CubeDeformation(0F, 0F, -0.4F)),
                PartPose.offsetAndRotation(0F, 0F, 4F, 0.1571F, 0F, 0F));
        PartDefinition p_robe_back_low = p_robe_back.addOrReplaceChild("robe_back_low", CubeListBuilder.create()
                .texOffs(163, 29).addBox(-7F, 0F, -1F, 14F, 12F, 2F, new CubeDeformation(0F, 0F, -0.4F)),
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
                .texOffs(92, 129).addBox(0F, -1F, -1F, 8F, 2F, 2F, new CubeDeformation(0F, 0F, -0.2F))
                .texOffs(57, 134).addBox(0F, 0.148F, -0.35F, 8F, 4F, 0F, new CubeDeformation(0F, -0.452F, 0F)),
                PartPose.offsetAndRotation(2.6F, -13F, 3F, -0.1047F, -0.2793F, -0.8378F));
        PartDefinition p_wing_upper_left_f0 = p_wing_upper_left.addOrReplaceChild("wing_upper_left_f0", CubeListBuilder.create()
                .texOffs(69, 123).addBox(-1.5F, -0.35F, 0F, 3F, 5F, 0F, new CubeDeformation(-0.038F, -0.35F, 0F)),
                PartPose.offset(0.96F, 0.7F, 0.05F));
        PartDefinition p_wing_upper_left_f1 = p_wing_upper_left.addOrReplaceChild("wing_upper_left_f1", CubeListBuilder.create()
                .texOffs(76, 123).addBox(-1.5F, -0.0275F, 0F, 3F, 5F, 0F, new CubeDeformation(-0.038F, -0.0275F, 0F)),
                PartPose.offsetAndRotation(4.16F, 0.7F, 0.17F, 0F, 0F, -0.0524F));
        PartDefinition p_wing_upper_left_f2 = p_wing_upper_left.addOrReplaceChild("wing_upper_left_f2", CubeListBuilder.create()
                .texOffs(149, 91).addBox(-1.5F, -0.205F, 0F, 3F, 6F, 0F, new CubeDeformation(-0.038F, -0.205F, 0F)),
                PartPose.offsetAndRotation(7.36F, 0.7F, 0.29F, 0F, 0F, -0.1047F));
        PartDefinition p_wing_upper_left_mid = p_wing_upper_left.addOrReplaceChild("wing_upper_left_mid", CubeListBuilder.create()
                .texOffs(25, 129).addBox(0F, -1F, -1F, 9F, 2F, 2F, new CubeDeformation(0F, 0F, -0.2F))
                .texOffs(218, 129).addBox(0F, 0.448F, -0.35F, 9F, 4F, 0F, new CubeDeformation(0F, -0.152F, 0F)),
                PartPose.offsetAndRotation(8F, 0F, 0F, 0F, -0.1047F, -0.1745F));
        PartDefinition p_wing_upper_left_mid_f0 = p_wing_upper_left_mid.addOrReplaceChild("wing_upper_left_mid_f0", CubeListBuilder.create()
                .texOffs(290, 82).addBox(-1.5F, -0.06F, 0F, 3F, 7F, 0F, new CubeDeformation(-0.124F, -0.06F, 0F)),
                PartPose.offsetAndRotation(1.08F, 0.7F, 0.05F, 0F, 0F, -0.1396F));
        PartDefinition p_wing_upper_left_mid_f1 = p_wing_upper_left_mid.addOrReplaceChild("wing_upper_left_mid_f1", CubeListBuilder.create()
                .texOffs(179, 82).addBox(-1.5F, -0.0583F, 0F, 3F, 8F, 0F, new CubeDeformation(-0.124F, -0.0583F, 0F)),
                PartPose.offsetAndRotation(3.48F, 0.7F, 0.17F, 0F, 0F, -0.1978F));
        PartDefinition p_wing_upper_left_mid_f2 = p_wing_upper_left_mid.addOrReplaceChild("wing_upper_left_mid_f2", CubeListBuilder.create()
                .texOffs(232, 71).addBox(-1.5F, -0.0567F, 0F, 3F, 9F, 0F, new CubeDeformation(-0.124F, -0.0567F, 0F)),
                PartPose.offsetAndRotation(5.88F, 0.7F, 0.29F, 0F, 0F, -0.256F));
        PartDefinition p_wing_upper_left_mid_f3 = p_wing_upper_left_mid.addOrReplaceChild("wing_upper_left_mid_f3", CubeListBuilder.create()
                .texOffs(9, 71).addBox(-1.5F, -0.055F, 0F, 3F, 10F, 0F, new CubeDeformation(-0.124F, -0.055F, 0F)),
                PartPose.offsetAndRotation(8.28F, 0.7F, 0.41F, 0F, 0F, -0.3142F));
        PartDefinition p_wing_upper_left_tip = p_wing_upper_left_mid.addOrReplaceChild("wing_upper_left_tip", CubeListBuilder.create()
                .texOffs(113, 129).addBox(0F, -1F, -1F, 8F, 2F, 2F, new CubeDeformation(0F, 0F, -0.2F))
                .texOffs(65, 99).addBox(0F, 0.248F, -0.35F, 8F, 5F, 0F, new CubeDeformation(0F, -0.352F, 0F)),
                PartPose.offsetAndRotation(9F, 0F, 0F, 0F, -0.1047F, -0.1745F));
        PartDefinition p_wing_upper_left_tip_f0 = p_wing_upper_left_tip.addOrReplaceChild("wing_upper_left_tip_f0", CubeListBuilder.create()
                .texOffs(77, 59).addBox(-1.5F, -0.34F, 0F, 3F, 11F, 0F, new CubeDeformation(-0.21F, -0.34F, 0F)),
                PartPose.offsetAndRotation(0.96F, 0.7F, 0.05F, 0F, 0F, -0.4189F));
        PartDefinition p_wing_upper_left_tip_f1 = p_wing_upper_left_tip.addOrReplaceChild("wing_upper_left_tip_f1", CubeListBuilder.create()
                .texOffs(186, 45).addBox(-1.5F, -0.2488F, 0F, 3F, 12F, 0F, new CubeDeformation(-0.21F, -0.2488F, 0F)),
                PartPose.offsetAndRotation(2.56F, 0.7F, 0.17F, 0F, 0F, -0.5934F));
        PartDefinition p_wing_upper_left_tip_f2 = p_wing_upper_left_tip.addOrReplaceChild("wing_upper_left_tip_f2", CubeListBuilder.create()
                .texOffs(299, 29).addBox(-1.5F, -0.1575F, 0F, 3F, 13F, 0F, new CubeDeformation(-0.21F, -0.1575F, 0F)),
                PartPose.offsetAndRotation(4.16F, 0.7F, 0.29F, 0F, 0F, -0.7679F));
        PartDefinition p_wing_upper_left_tip_f3 = p_wing_upper_left_tip.addOrReplaceChild("wing_upper_left_tip_f3", CubeListBuilder.create()
                .texOffs(238, 29).addBox(-1.5F, -0.0663F, 0F, 3F, 14F, 0F, new CubeDeformation(-0.21F, -0.0663F, 0F)),
                PartPose.offsetAndRotation(5.76F, 0.7F, 0.41F, 0F, 0F, -0.9425F));
        PartDefinition p_wing_upper_left_tip_f4 = p_wing_upper_left_tip.addOrReplaceChild("wing_upper_left_tip_f4", CubeListBuilder.create()
                .texOffs(265, 0).addBox(-1.5F, -0.475F, 0F, 3F, 16F, 0F, new CubeDeformation(-0.21F, -0.475F, 0F)),
                PartPose.offsetAndRotation(7.36F, 0.7F, 0.53F, 0F, 0F, -1.117F));
        PartDefinition p_wing_upper_right = p_body.addOrReplaceChild("wing_upper_right", CubeListBuilder.create()
                .texOffs(134, 129).addBox(-8F, -1F, -1F, 8F, 2F, 2F, new CubeDeformation(0F, 0F, -0.2F))
                .texOffs(74, 134).addBox(-8F, 0.148F, -0.35F, 8F, 4F, 0F, new CubeDeformation(0F, -0.452F, 0F)),
                PartPose.offsetAndRotation(-2.6F, -13F, 3F, -0.1047F, 0.2793F, 0.8378F));
        PartDefinition p_wing_upper_right_f0 = p_wing_upper_right.addOrReplaceChild("wing_upper_right_f0", CubeListBuilder.create()
                .texOffs(83, 123).addBox(-1.5F, -0.35F, 0F, 3F, 5F, 0F, new CubeDeformation(-0.038F, -0.35F, 0F)),
                PartPose.offset(-0.96F, 0.7F, 0.05F));
        PartDefinition p_wing_upper_right_f1 = p_wing_upper_right.addOrReplaceChild("wing_upper_right_f1", CubeListBuilder.create()
                .texOffs(90, 123).addBox(-1.5F, -0.0275F, 0F, 3F, 5F, 0F, new CubeDeformation(-0.038F, -0.0275F, 0F)),
                PartPose.offsetAndRotation(-4.16F, 0.7F, 0.17F, 0F, 0F, 0.0524F));
        PartDefinition p_wing_upper_right_f2 = p_wing_upper_right.addOrReplaceChild("wing_upper_right_f2", CubeListBuilder.create()
                .texOffs(156, 91).addBox(-1.5F, -0.205F, 0F, 3F, 6F, 0F, new CubeDeformation(-0.038F, -0.205F, 0F)),
                PartPose.offsetAndRotation(-7.36F, 0.7F, 0.29F, 0F, 0F, 0.1047F));
        PartDefinition p_wing_upper_right_mid = p_wing_upper_right.addOrReplaceChild("wing_upper_right_mid", CubeListBuilder.create()
                .texOffs(48, 129).addBox(-9F, -1F, -1F, 9F, 2F, 2F, new CubeDeformation(0F, 0F, -0.2F))
                .texOffs(237, 129).addBox(-9F, 0.448F, -0.35F, 9F, 4F, 0F, new CubeDeformation(0F, -0.152F, 0F)),
                PartPose.offsetAndRotation(-8F, 0F, 0F, 0F, 0.1047F, 0.1745F));
        PartDefinition p_wing_upper_right_mid_f0 = p_wing_upper_right_mid.addOrReplaceChild("wing_upper_right_mid_f0", CubeListBuilder.create()
                .texOffs(297, 82).addBox(-1.5F, -0.06F, 0F, 3F, 7F, 0F, new CubeDeformation(-0.124F, -0.06F, 0F)),
                PartPose.offsetAndRotation(-1.08F, 0.7F, 0.05F, 0F, 0F, 0.1396F));
        PartDefinition p_wing_upper_right_mid_f1 = p_wing_upper_right_mid.addOrReplaceChild("wing_upper_right_mid_f1", CubeListBuilder.create()
                .texOffs(186, 82).addBox(-1.5F, -0.0583F, 0F, 3F, 8F, 0F, new CubeDeformation(-0.124F, -0.0583F, 0F)),
                PartPose.offsetAndRotation(-3.48F, 0.7F, 0.17F, 0F, 0F, 0.1978F));
        PartDefinition p_wing_upper_right_mid_f2 = p_wing_upper_right_mid.addOrReplaceChild("wing_upper_right_mid_f2", CubeListBuilder.create()
                .texOffs(239, 71).addBox(-1.5F, -0.0567F, 0F, 3F, 9F, 0F, new CubeDeformation(-0.124F, -0.0567F, 0F)),
                PartPose.offsetAndRotation(-5.88F, 0.7F, 0.29F, 0F, 0F, 0.256F));
        PartDefinition p_wing_upper_right_mid_f3 = p_wing_upper_right_mid.addOrReplaceChild("wing_upper_right_mid_f3", CubeListBuilder.create()
                .texOffs(16, 71).addBox(-1.5F, -0.055F, 0F, 3F, 10F, 0F, new CubeDeformation(-0.124F, -0.055F, 0F)),
                PartPose.offsetAndRotation(-8.28F, 0.7F, 0.41F, 0F, 0F, 0.3142F));
        PartDefinition p_wing_upper_right_tip = p_wing_upper_right_mid.addOrReplaceChild("wing_upper_right_tip", CubeListBuilder.create()
                .texOffs(155, 129).addBox(-8F, -1F, -1F, 8F, 2F, 2F, new CubeDeformation(0F, 0F, -0.2F))
                .texOffs(82, 99).addBox(-8F, 0.248F, -0.35F, 8F, 5F, 0F, new CubeDeformation(0F, -0.352F, 0F)),
                PartPose.offsetAndRotation(-9F, 0F, 0F, 0F, 0.1047F, 0.1745F));
        PartDefinition p_wing_upper_right_tip_f0 = p_wing_upper_right_tip.addOrReplaceChild("wing_upper_right_tip_f0", CubeListBuilder.create()
                .texOffs(84, 59).addBox(-1.5F, -0.34F, 0F, 3F, 11F, 0F, new CubeDeformation(-0.21F, -0.34F, 0F)),
                PartPose.offsetAndRotation(-0.96F, 0.7F, 0.05F, 0F, 0F, 0.4189F));
        PartDefinition p_wing_upper_right_tip_f1 = p_wing_upper_right_tip.addOrReplaceChild("wing_upper_right_tip_f1", CubeListBuilder.create()
                .texOffs(193, 45).addBox(-1.5F, -0.2488F, 0F, 3F, 12F, 0F, new CubeDeformation(-0.21F, -0.2488F, 0F)),
                PartPose.offsetAndRotation(-2.56F, 0.7F, 0.17F, 0F, 0F, 0.5934F));
        PartDefinition p_wing_upper_right_tip_f2 = p_wing_upper_right_tip.addOrReplaceChild("wing_upper_right_tip_f2", CubeListBuilder.create()
                .texOffs(306, 29).addBox(-1.5F, -0.1575F, 0F, 3F, 13F, 0F, new CubeDeformation(-0.21F, -0.1575F, 0F)),
                PartPose.offsetAndRotation(-4.16F, 0.7F, 0.29F, 0F, 0F, 0.7679F));
        PartDefinition p_wing_upper_right_tip_f3 = p_wing_upper_right_tip.addOrReplaceChild("wing_upper_right_tip_f3", CubeListBuilder.create()
                .texOffs(245, 29).addBox(-1.5F, -0.0663F, 0F, 3F, 14F, 0F, new CubeDeformation(-0.21F, -0.0663F, 0F)),
                PartPose.offsetAndRotation(-5.76F, 0.7F, 0.41F, 0F, 0F, 0.9425F));
        PartDefinition p_wing_upper_right_tip_f4 = p_wing_upper_right_tip.addOrReplaceChild("wing_upper_right_tip_f4", CubeListBuilder.create()
                .texOffs(272, 0).addBox(-1.5F, -0.475F, 0F, 3F, 16F, 0F, new CubeDeformation(-0.21F, -0.475F, 0F)),
                PartPose.offsetAndRotation(-7.36F, 0.7F, 0.53F, 0F, 0F, 1.117F));
        PartDefinition p_wing_mid_left = p_body.addOrReplaceChild("wing_mid_left", CubeListBuilder.create()
                .texOffs(226, 123).addBox(0F, -1F, -1F, 10F, 2F, 2F, new CubeDeformation(0F, 0F, -0.2F))
                .texOffs(176, 129).addBox(0F, 0.4F, -0.35F, 10F, 4F, 0F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offsetAndRotation(2.6F, -10.5F, 3.4F, 0F, -0.384F, -0.2094F));
        PartDefinition p_wing_mid_left_f0 = p_wing_mid_left.addOrReplaceChild("wing_mid_left_f0", CubeListBuilder.create()
                .texOffs(51, 123).addBox(-2F, 0F, 0F, 4F, 5F, 0F, new CubeDeformation(-0.3F, 0F, 0F)),
                PartPose.offset(1.2F, 0.7F, 0.05F));
        PartDefinition p_wing_mid_left_f1 = p_wing_mid_left.addOrReplaceChild("wing_mid_left_f1", CubeListBuilder.create()
                .texOffs(131, 91).addBox(-2F, -0.125F, 0F, 4F, 6F, 0F, new CubeDeformation(-0.3F, -0.125F, 0F)),
                PartPose.offsetAndRotation(5.2F, 0.7F, 0.17F, 0F, 0F, -0.0524F));
        PartDefinition p_wing_mid_left_f2 = p_wing_mid_left.addOrReplaceChild("wing_mid_left_f2", CubeListBuilder.create()
                .texOffs(258, 82).addBox(-2F, -0.25F, 0F, 4F, 7F, 0F, new CubeDeformation(-0.3F, -0.25F, 0F)),
                PartPose.offsetAndRotation(9.2F, 0.7F, 0.29F, 0F, 0F, -0.1047F));
        PartDefinition p_wing_mid_left_mid = p_wing_mid_left.addOrReplaceChild("wing_mid_left_mid", CubeListBuilder.create()
                .texOffs(172, 123).addBox(0F, -1F, -1F, 11F, 2F, 2F, new CubeDeformation(0F, 0F, -0.2F))
                .texOffs(277, 91).addBox(0F, 0.2F, -0.35F, 11F, 5F, 0F, new CubeDeformation(0F, -0.4F, 0F)),
                PartPose.offsetAndRotation(10F, 0F, 0F, 0F, -0.1047F, 0.1571F));
        PartDefinition p_wing_mid_left_mid_f0 = p_wing_mid_left_mid.addOrReplaceChild("wing_mid_left_mid_f0", CubeListBuilder.create()
                .texOffs(161, 82).addBox(-2F, 0F, 0F, 4F, 8F, 0F, new CubeDeformation(-0.4F, 0F, 0F)),
                PartPose.offsetAndRotation(1.32F, 0.7F, 0.05F, 0F, 0F, -0.1396F));
        PartDefinition p_wing_mid_left_mid_f1 = p_wing_mid_left_mid.addOrReplaceChild("wing_mid_left_mid_f1", CubeListBuilder.create()
                .texOffs(309, 59).addBox(-2F, -0.4167F, 0F, 4F, 10F, 0F, new CubeDeformation(-0.4F, -0.4167F, 0F)),
                PartPose.offsetAndRotation(4.2533F, 0.7F, 0.17F, 0F, 0F, -0.1978F));
        PartDefinition p_wing_mid_left_mid_f2 = p_wing_mid_left_mid.addOrReplaceChild("wing_mid_left_mid_f2", CubeListBuilder.create()
                .texOffs(59, 59).addBox(-2F, -0.3333F, 0F, 4F, 11F, 0F, new CubeDeformation(-0.4F, -0.3333F, 0F)),
                PartPose.offsetAndRotation(7.1867F, 0.7F, 0.29F, 0F, 0F, -0.256F));
        PartDefinition p_wing_mid_left_mid_f3 = p_wing_mid_left_mid.addOrReplaceChild("wing_mid_left_mid_f3", CubeListBuilder.create()
                .texOffs(168, 45).addBox(-2F, -0.25F, 0F, 4F, 12F, 0F, new CubeDeformation(-0.4F, -0.25F, 0F)),
                PartPose.offsetAndRotation(10.12F, 0.7F, 0.41F, 0F, 0F, -0.3142F));
        PartDefinition p_wing_mid_left_tip = p_wing_mid_left_mid.addOrReplaceChild("wing_mid_left_tip", CubeListBuilder.create()
                .texOffs(251, 123).addBox(0F, -1F, -1F, 10F, 2F, 2F, new CubeDeformation(0F, 0F, -0.2F))
                .texOffs(23, 99).addBox(0F, 0.5F, -0.35F, 10F, 5F, 0F, new CubeDeformation(0F, -0.1F, 0F)),
                PartPose.offsetAndRotation(11F, 0F, 0F, 0F, -0.1047F, 0.1571F));
        PartDefinition p_wing_mid_left_tip_f0 = p_wing_mid_left_tip.addOrReplaceChild("wing_mid_left_tip_f0", CubeListBuilder.create()
                .texOffs(200, 45).addBox(-1.5F, 0F, 0F, 3F, 12F, 0F),
                PartPose.offsetAndRotation(1.2F, 0.7F, 0.05F, 0F, 0F, -0.4189F));
        PartDefinition p_wing_mid_left_tip_f1 = p_wing_mid_left_tip.addOrReplaceChild("wing_mid_left_tip_f1", CubeListBuilder.create()
                .texOffs(252, 29).addBox(-1.5F, -0.3125F, 0F, 3F, 14F, 0F, new CubeDeformation(0F, -0.3125F, 0F)),
                PartPose.offsetAndRotation(3.2F, 0.7F, 0.17F, 0F, 0F, -0.5934F));
        PartDefinition p_wing_mid_left_tip_f2 = p_wing_mid_left_tip.addOrReplaceChild("wing_mid_left_tip_f2", CubeListBuilder.create()
                .texOffs(106, 29).addBox(-1.5F, -0.125F, 0F, 3F, 15F, 0F, new CubeDeformation(0F, -0.125F, 0F)),
                PartPose.offsetAndRotation(5.2F, 0.7F, 0.29F, 0F, 0F, -0.7679F));
        PartDefinition p_wing_mid_left_tip_f3 = p_wing_mid_left_tip.addOrReplaceChild("wing_mid_left_tip_f3", CubeListBuilder.create()
                .texOffs(176, 0).addBox(-1.5F, -0.4375F, 0F, 3F, 17F, 0F, new CubeDeformation(0F, -0.4375F, 0F)),
                PartPose.offsetAndRotation(7.2F, 0.7F, 0.41F, 0F, 0F, -0.9425F));
        PartDefinition p_wing_mid_left_tip_f4 = p_wing_mid_left_tip.addOrReplaceChild("wing_mid_left_tip_f4", CubeListBuilder.create()
                .texOffs(162, 0).addBox(-1.5F, -0.25F, 0F, 3F, 18F, 0F, new CubeDeformation(0F, -0.25F, 0F)),
                PartPose.offsetAndRotation(9.2F, 0.7F, 0.53F, 0F, 0F, -1.117F));
        PartDefinition p_wing_mid_right = p_body.addOrReplaceChild("wing_mid_right", CubeListBuilder.create()
                .texOffs(276, 123).addBox(-10F, -1F, -1F, 10F, 2F, 2F, new CubeDeformation(0F, 0F, -0.2F))
                .texOffs(197, 129).addBox(-10F, 0.4F, -0.35F, 10F, 4F, 0F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offsetAndRotation(-2.6F, -10.5F, 3.4F, 0F, 0.384F, 0.2094F));
        PartDefinition p_wing_mid_right_f0 = p_wing_mid_right.addOrReplaceChild("wing_mid_right_f0", CubeListBuilder.create()
                .texOffs(60, 123).addBox(-2F, 0F, 0F, 4F, 5F, 0F, new CubeDeformation(-0.3F, 0F, 0F)),
                PartPose.offset(-1.2F, 0.7F, 0.05F));
        PartDefinition p_wing_mid_right_f1 = p_wing_mid_right.addOrReplaceChild("wing_mid_right_f1", CubeListBuilder.create()
                .texOffs(140, 91).addBox(-2F, -0.125F, 0F, 4F, 6F, 0F, new CubeDeformation(-0.3F, -0.125F, 0F)),
                PartPose.offsetAndRotation(-5.2F, 0.7F, 0.17F, 0F, 0F, 0.0524F));
        PartDefinition p_wing_mid_right_f2 = p_wing_mid_right.addOrReplaceChild("wing_mid_right_f2", CubeListBuilder.create()
                .texOffs(267, 82).addBox(-2F, -0.25F, 0F, 4F, 7F, 0F, new CubeDeformation(-0.3F, -0.25F, 0F)),
                PartPose.offsetAndRotation(-9.2F, 0.7F, 0.29F, 0F, 0F, 0.1047F));
        PartDefinition p_wing_mid_right_mid = p_wing_mid_right.addOrReplaceChild("wing_mid_right_mid", CubeListBuilder.create()
                .texOffs(199, 123).addBox(-11F, -1F, -1F, 11F, 2F, 2F, new CubeDeformation(0F, 0F, -0.2F))
                .texOffs(0, 99).addBox(-11F, 0.2F, -0.35F, 11F, 5F, 0F, new CubeDeformation(0F, -0.4F, 0F)),
                PartPose.offsetAndRotation(-10F, 0F, 0F, 0F, 0.1047F, -0.1571F));
        PartDefinition p_wing_mid_right_mid_f0 = p_wing_mid_right_mid.addOrReplaceChild("wing_mid_right_mid_f0", CubeListBuilder.create()
                .texOffs(170, 82).addBox(-2F, 0F, 0F, 4F, 8F, 0F, new CubeDeformation(-0.4F, 0F, 0F)),
                PartPose.offsetAndRotation(-1.32F, 0.7F, 0.05F, 0F, 0F, 0.1396F));
        PartDefinition p_wing_mid_right_mid_f1 = p_wing_mid_right_mid.addOrReplaceChild("wing_mid_right_mid_f1", CubeListBuilder.create()
                .texOffs(0, 71).addBox(-2F, -0.4167F, 0F, 4F, 10F, 0F, new CubeDeformation(-0.4F, -0.4167F, 0F)),
                PartPose.offsetAndRotation(-4.2533F, 0.7F, 0.17F, 0F, 0F, 0.1978F));
        PartDefinition p_wing_mid_right_mid_f2 = p_wing_mid_right_mid.addOrReplaceChild("wing_mid_right_mid_f2", CubeListBuilder.create()
                .texOffs(68, 59).addBox(-2F, -0.3333F, 0F, 4F, 11F, 0F, new CubeDeformation(-0.4F, -0.3333F, 0F)),
                PartPose.offsetAndRotation(-7.1867F, 0.7F, 0.29F, 0F, 0F, 0.256F));
        PartDefinition p_wing_mid_right_mid_f3 = p_wing_mid_right_mid.addOrReplaceChild("wing_mid_right_mid_f3", CubeListBuilder.create()
                .texOffs(177, 45).addBox(-2F, -0.25F, 0F, 4F, 12F, 0F, new CubeDeformation(-0.4F, -0.25F, 0F)),
                PartPose.offsetAndRotation(-10.12F, 0.7F, 0.41F, 0F, 0F, 0.3142F));
        PartDefinition p_wing_mid_right_tip = p_wing_mid_right_mid.addOrReplaceChild("wing_mid_right_tip", CubeListBuilder.create()
                .texOffs(0, 129).addBox(-10F, -1F, -1F, 10F, 2F, 2F, new CubeDeformation(0F, 0F, -0.2F))
                .texOffs(44, 99).addBox(-10F, 0.5F, -0.35F, 10F, 5F, 0F, new CubeDeformation(0F, -0.1F, 0F)),
                PartPose.offsetAndRotation(-11F, 0F, 0F, 0F, 0.1047F, -0.1571F));
        PartDefinition p_wing_mid_right_tip_f0 = p_wing_mid_right_tip.addOrReplaceChild("wing_mid_right_tip_f0", CubeListBuilder.create()
                .texOffs(207, 45).addBox(-1.5F, 0F, 0F, 3F, 12F, 0F),
                PartPose.offsetAndRotation(-1.2F, 0.7F, 0.05F, 0F, 0F, 0.4189F));
        PartDefinition p_wing_mid_right_tip_f1 = p_wing_mid_right_tip.addOrReplaceChild("wing_mid_right_tip_f1", CubeListBuilder.create()
                .texOffs(259, 29).addBox(-1.5F, -0.3125F, 0F, 3F, 14F, 0F, new CubeDeformation(0F, -0.3125F, 0F)),
                PartPose.offsetAndRotation(-3.2F, 0.7F, 0.17F, 0F, 0F, 0.5934F));
        PartDefinition p_wing_mid_right_tip_f2 = p_wing_mid_right_tip.addOrReplaceChild("wing_mid_right_tip_f2", CubeListBuilder.create()
                .texOffs(113, 29).addBox(-1.5F, -0.125F, 0F, 3F, 15F, 0F, new CubeDeformation(0F, -0.125F, 0F)),
                PartPose.offsetAndRotation(-5.2F, 0.7F, 0.29F, 0F, 0F, 0.7679F));
        PartDefinition p_wing_mid_right_tip_f3 = p_wing_mid_right_tip.addOrReplaceChild("wing_mid_right_tip_f3", CubeListBuilder.create()
                .texOffs(183, 0).addBox(-1.5F, -0.4375F, 0F, 3F, 17F, 0F, new CubeDeformation(0F, -0.4375F, 0F)),
                PartPose.offsetAndRotation(-7.2F, 0.7F, 0.41F, 0F, 0F, 0.9425F));
        PartDefinition p_wing_mid_right_tip_f4 = p_wing_mid_right_tip.addOrReplaceChild("wing_mid_right_tip_f4", CubeListBuilder.create()
                .texOffs(169, 0).addBox(-1.5F, -0.25F, 0F, 3F, 18F, 0F, new CubeDeformation(0F, -0.25F, 0F)),
                PartPose.offsetAndRotation(-9.2F, 0.7F, 0.53F, 0F, 0F, 1.117F));
        PartDefinition p_wing_lower_left = p_body.addOrReplaceChild("wing_lower_left", CubeListBuilder.create()
                .texOffs(256, 129).addBox(-0.25F, -1F, -1F, 7F, 2F, 2F, new CubeDeformation(-0.25F, 0F, -0.2F))
                .texOffs(164, 139).addBox(-0.25F, 0.396F, -0.35F, 7F, 3F, 0F, new CubeDeformation(-0.25F, -0.204F, 0F)),
                PartPose.offsetAndRotation(2.6F, -7.5F, 3.2F, 0.1047F, -0.3142F, 0.4189F));
        PartDefinition p_wing_lower_left_f0 = p_wing_lower_left.addOrReplaceChild("wing_lower_left_f0", CubeListBuilder.create()
                .texOffs(115, 139).addBox(-1.5F, -0.2F, 0F, 3F, 4F, 0F, new CubeDeformation(-0.276F, -0.2F, 0F)),
                PartPose.offset(0.78F, 0.7F, 0.05F));
        PartDefinition p_wing_lower_left_f1 = p_wing_lower_left.addOrReplaceChild("wing_lower_left_f1", CubeListBuilder.create()
                .texOffs(97, 123).addBox(-1.5F, -0.43F, 0F, 3F, 5F, 0F, new CubeDeformation(-0.276F, -0.43F, 0F)),
                PartPose.offsetAndRotation(3.38F, 0.7F, 0.17F, 0F, 0F, -0.0524F));
        PartDefinition p_wing_lower_left_f2 = p_wing_lower_left.addOrReplaceChild("wing_lower_left_f2", CubeListBuilder.create()
                .texOffs(104, 123).addBox(-1.5F, -0.16F, 0F, 3F, 5F, 0F, new CubeDeformation(-0.276F, -0.16F, 0F)),
                PartPose.offsetAndRotation(5.98F, 0.7F, 0.29F, 0F, 0F, -0.1047F));
        PartDefinition p_wing_lower_left_mid = p_wing_lower_left.addOrReplaceChild("wing_lower_left_mid", CubeListBuilder.create()
                .texOffs(275, 129).addBox(0F, -1F, -1F, 7F, 2F, 2F, new CubeDeformation(0F, 0F, -0.2F))
                .texOffs(15, 139).addBox(0F, 0.196F, -0.35F, 7F, 4F, 0F, new CubeDeformation(0F, -0.404F, 0F)),
                PartPose.offsetAndRotation(6.5F, 0F, 0F, 0F, -0.1047F, 0.2094F));
        PartDefinition p_wing_lower_left_mid_f0 = p_wing_lower_left_mid.addOrReplaceChild("wing_lower_left_mid_f0", CubeListBuilder.create()
                .texOffs(163, 91).addBox(-1.5F, -0.12F, 0F, 3F, 6F, 0F, new CubeDeformation(-0.348F, -0.12F, 0F)),
                PartPose.offsetAndRotation(0.84F, 0.7F, 0.05F, 0F, 0F, -0.1396F));
        PartDefinition p_wing_lower_left_mid_f1 = p_wing_lower_left_mid.addOrReplaceChild("wing_lower_left_mid_f1", CubeListBuilder.create()
                .texOffs(304, 82).addBox(-1.5F, -0.2F, 0F, 3F, 7F, 0F, new CubeDeformation(-0.348F, -0.2F, 0F)),
                PartPose.offsetAndRotation(2.7067F, 0.7F, 0.17F, 0F, 0F, -0.1978F));
        PartDefinition p_wing_lower_left_mid_f2 = p_wing_lower_left_mid.addOrReplaceChild("wing_lower_left_mid_f2", CubeListBuilder.create()
                .texOffs(193, 82).addBox(-1.5F, -0.28F, 0F, 3F, 8F, 0F, new CubeDeformation(-0.348F, -0.28F, 0F)),
                PartPose.offsetAndRotation(4.5733F, 0.7F, 0.29F, 0F, 0F, -0.256F));
        PartDefinition p_wing_lower_left_mid_f3 = p_wing_lower_left_mid.addOrReplaceChild("wing_lower_left_mid_f3", CubeListBuilder.create()
                .texOffs(246, 71).addBox(-1.5F, -0.36F, 0F, 3F, 9F, 0F, new CubeDeformation(-0.348F, -0.36F, 0F)),
                PartPose.offsetAndRotation(6.44F, 0.7F, 0.41F, 0F, 0F, -0.3142F));
        PartDefinition p_wing_lower_left_tip = p_wing_lower_left_mid.addOrReplaceChild("wing_lower_left_tip", CubeListBuilder.create()
                .texOffs(294, 129).addBox(-0.25F, -1F, -1F, 7F, 2F, 2F, new CubeDeformation(-0.25F, 0F, -0.2F))
                .texOffs(30, 139).addBox(-0.25F, 0.496F, -0.35F, 7F, 4F, 0F, new CubeDeformation(-0.25F, -0.104F, 0F)),
                PartPose.offsetAndRotation(7F, 0F, 0F, 0F, -0.1047F, 0.2094F));
        PartDefinition p_wing_lower_left_tip_f0 = p_wing_lower_left_tip.addOrReplaceChild("wing_lower_left_tip_f0", CubeListBuilder.create()
                .texOffs(253, 71).addBox(-1.5F, -0.18F, 0F, 3F, 9F, 0F, new CubeDeformation(-0.42F, -0.18F, 0F)),
                PartPose.offsetAndRotation(0.78F, 0.7F, 0.05F, 0F, 0F, -0.4189F));
        PartDefinition p_wing_lower_left_tip_f1 = p_wing_lower_left_tip.addOrReplaceChild("wing_lower_left_tip_f1", CubeListBuilder.create()
                .texOffs(23, 71).addBox(-1.5F, -0.185F, 0F, 3F, 10F, 0F, new CubeDeformation(-0.42F, -0.185F, 0F)),
                PartPose.offsetAndRotation(2.08F, 0.7F, 0.17F, 0F, 0F, -0.5934F));
        PartDefinition p_wing_lower_left_tip_f2 = p_wing_lower_left_tip.addOrReplaceChild("wing_lower_left_tip_f2", CubeListBuilder.create()
                .texOffs(91, 59).addBox(-1.5F, -0.19F, 0F, 3F, 11F, 0F, new CubeDeformation(-0.42F, -0.19F, 0F)),
                PartPose.offsetAndRotation(3.38F, 0.7F, 0.29F, 0F, 0F, -0.7679F));
        PartDefinition p_wing_lower_left_tip_f3 = p_wing_lower_left_tip.addOrReplaceChild("wing_lower_left_tip_f3", CubeListBuilder.create()
                .texOffs(214, 45).addBox(-1.5F, -0.195F, 0F, 3F, 12F, 0F, new CubeDeformation(-0.42F, -0.195F, 0F)),
                PartPose.offsetAndRotation(4.68F, 0.7F, 0.41F, 0F, 0F, -0.9425F));
        PartDefinition p_wing_lower_left_tip_f4 = p_wing_lower_left_tip.addOrReplaceChild("wing_lower_left_tip_f4", CubeListBuilder.create()
                .texOffs(313, 29).addBox(-1.5F, -0.2F, 0F, 3F, 13F, 0F, new CubeDeformation(-0.42F, -0.2F, 0F)),
                PartPose.offsetAndRotation(5.98F, 0.7F, 0.53F, 0F, 0F, -1.117F));
        PartDefinition p_wing_lower_right = p_body.addOrReplaceChild("wing_lower_right", CubeListBuilder.create()
                .texOffs(0, 134).addBox(-6.75F, -1F, -1F, 7F, 2F, 2F, new CubeDeformation(-0.25F, 0F, -0.2F))
                .texOffs(179, 139).addBox(-6.75F, 0.396F, -0.35F, 7F, 3F, 0F, new CubeDeformation(-0.25F, -0.204F, 0F)),
                PartPose.offsetAndRotation(-2.6F, -7.5F, 3.2F, 0.1047F, 0.3142F, -0.4189F));
        PartDefinition p_wing_lower_right_f0 = p_wing_lower_right.addOrReplaceChild("wing_lower_right_f0", CubeListBuilder.create()
                .texOffs(122, 139).addBox(-1.5F, -0.2F, 0F, 3F, 4F, 0F, new CubeDeformation(-0.276F, -0.2F, 0F)),
                PartPose.offset(-0.78F, 0.7F, 0.05F));
        PartDefinition p_wing_lower_right_f1 = p_wing_lower_right.addOrReplaceChild("wing_lower_right_f1", CubeListBuilder.create()
                .texOffs(111, 123).addBox(-1.5F, -0.43F, 0F, 3F, 5F, 0F, new CubeDeformation(-0.276F, -0.43F, 0F)),
                PartPose.offsetAndRotation(-3.38F, 0.7F, 0.17F, 0F, 0F, 0.0524F));
        PartDefinition p_wing_lower_right_f2 = p_wing_lower_right.addOrReplaceChild("wing_lower_right_f2", CubeListBuilder.create()
                .texOffs(118, 123).addBox(-1.5F, -0.16F, 0F, 3F, 5F, 0F, new CubeDeformation(-0.276F, -0.16F, 0F)),
                PartPose.offsetAndRotation(-5.98F, 0.7F, 0.29F, 0F, 0F, 0.1047F));
        PartDefinition p_wing_lower_right_mid = p_wing_lower_right.addOrReplaceChild("wing_lower_right_mid", CubeListBuilder.create()
                .texOffs(19, 134).addBox(-7F, -1F, -1F, 7F, 2F, 2F, new CubeDeformation(0F, 0F, -0.2F))
                .texOffs(45, 139).addBox(-7F, 0.196F, -0.35F, 7F, 4F, 0F, new CubeDeformation(0F, -0.404F, 0F)),
                PartPose.offsetAndRotation(-6.5F, 0F, 0F, 0F, 0.1047F, -0.2094F));
        PartDefinition p_wing_lower_right_mid_f0 = p_wing_lower_right_mid.addOrReplaceChild("wing_lower_right_mid_f0", CubeListBuilder.create()
                .texOffs(170, 91).addBox(-1.5F, -0.12F, 0F, 3F, 6F, 0F, new CubeDeformation(-0.348F, -0.12F, 0F)),
                PartPose.offsetAndRotation(-0.84F, 0.7F, 0.05F, 0F, 0F, 0.1396F));
        PartDefinition p_wing_lower_right_mid_f1 = p_wing_lower_right_mid.addOrReplaceChild("wing_lower_right_mid_f1", CubeListBuilder.create()
                .texOffs(311, 82).addBox(-1.5F, -0.2F, 0F, 3F, 7F, 0F, new CubeDeformation(-0.348F, -0.2F, 0F)),
                PartPose.offsetAndRotation(-2.7067F, 0.7F, 0.17F, 0F, 0F, 0.1978F));
        PartDefinition p_wing_lower_right_mid_f2 = p_wing_lower_right_mid.addOrReplaceChild("wing_lower_right_mid_f2", CubeListBuilder.create()
                .texOffs(200, 82).addBox(-1.5F, -0.28F, 0F, 3F, 8F, 0F, new CubeDeformation(-0.348F, -0.28F, 0F)),
                PartPose.offsetAndRotation(-4.5733F, 0.7F, 0.29F, 0F, 0F, 0.256F));
        PartDefinition p_wing_lower_right_mid_f3 = p_wing_lower_right_mid.addOrReplaceChild("wing_lower_right_mid_f3", CubeListBuilder.create()
                .texOffs(260, 71).addBox(-1.5F, -0.36F, 0F, 3F, 9F, 0F, new CubeDeformation(-0.348F, -0.36F, 0F)),
                PartPose.offsetAndRotation(-6.44F, 0.7F, 0.41F, 0F, 0F, 0.3142F));
        PartDefinition p_wing_lower_right_tip = p_wing_lower_right_mid.addOrReplaceChild("wing_lower_right_tip", CubeListBuilder.create()
                .texOffs(38, 134).addBox(-6.75F, -1F, -1F, 7F, 2F, 2F, new CubeDeformation(-0.25F, 0F, -0.2F))
                .texOffs(60, 139).addBox(-6.75F, 0.496F, -0.35F, 7F, 4F, 0F, new CubeDeformation(-0.25F, -0.104F, 0F)),
                PartPose.offsetAndRotation(-7F, 0F, 0F, 0F, 0.1047F, -0.2094F));
        PartDefinition p_wing_lower_right_tip_f0 = p_wing_lower_right_tip.addOrReplaceChild("wing_lower_right_tip_f0", CubeListBuilder.create()
                .texOffs(267, 71).addBox(-1.5F, -0.18F, 0F, 3F, 9F, 0F, new CubeDeformation(-0.42F, -0.18F, 0F)),
                PartPose.offsetAndRotation(-0.78F, 0.7F, 0.05F, 0F, 0F, 0.4189F));
        PartDefinition p_wing_lower_right_tip_f1 = p_wing_lower_right_tip.addOrReplaceChild("wing_lower_right_tip_f1", CubeListBuilder.create()
                .texOffs(30, 71).addBox(-1.5F, -0.185F, 0F, 3F, 10F, 0F, new CubeDeformation(-0.42F, -0.185F, 0F)),
                PartPose.offsetAndRotation(-2.08F, 0.7F, 0.17F, 0F, 0F, 0.5934F));
        PartDefinition p_wing_lower_right_tip_f2 = p_wing_lower_right_tip.addOrReplaceChild("wing_lower_right_tip_f2", CubeListBuilder.create()
                .texOffs(98, 59).addBox(-1.5F, -0.19F, 0F, 3F, 11F, 0F, new CubeDeformation(-0.42F, -0.19F, 0F)),
                PartPose.offsetAndRotation(-3.38F, 0.7F, 0.29F, 0F, 0F, 0.7679F));
        PartDefinition p_wing_lower_right_tip_f3 = p_wing_lower_right_tip.addOrReplaceChild("wing_lower_right_tip_f3", CubeListBuilder.create()
                .texOffs(221, 45).addBox(-1.5F, -0.195F, 0F, 3F, 12F, 0F, new CubeDeformation(-0.42F, -0.195F, 0F)),
                PartPose.offsetAndRotation(-4.68F, 0.7F, 0.41F, 0F, 0F, 0.9425F));
        PartDefinition p_wing_lower_right_tip_f4 = p_wing_lower_right_tip.addOrReplaceChild("wing_lower_right_tip_f4", CubeListBuilder.create()
                .texOffs(0, 45).addBox(-1.5F, -0.2F, 0F, 3F, 13F, 0F, new CubeDeformation(-0.42F, -0.2F, 0F)),
                PartPose.offsetAndRotation(-5.98F, 0.7F, 0.53F, 0F, 0F, 1.117F));
        PartDefinition p_true_rings = p_body.addOrReplaceChild("true_rings", CubeListBuilder.create(),
                PartPose.offset(0F, -8F, 0F));
        PartDefinition p_true_ring_a = p_true_rings.addOrReplaceChild("true_ring_a", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_true_ring_a_0 = p_true_ring_a.addOrReplaceChild("true_ring_a_0", CubeListBuilder.create()
                .texOffs(99, 99).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.4188F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(15F, 0F, 0F, 0F, -1.5708F, 0F));
        PartDefinition p_true_ring_a_1 = p_true_ring_a.addOrReplaceChild("true_ring_a_1", CubeListBuilder.create()
                .texOffs(116, 99).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.4188F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(14.2658F, 0F, 4.6353F, 0F, -1.885F, 0F));
        PartDefinition p_true_ring_a_2 = p_true_ring_a.addOrReplaceChild("true_ring_a_2", CubeListBuilder.create()
                .texOffs(133, 99).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.4188F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(12.1353F, 0F, 8.8168F, 0F, -2.1991F, 0F));
        PartDefinition p_true_ring_a_3 = p_true_ring_a.addOrReplaceChild("true_ring_a_3", CubeListBuilder.create()
                .texOffs(150, 99).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.4188F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(8.8168F, 0F, 12.1353F, 0F, -2.5133F, 0F));
        PartDefinition p_true_ring_a_4 = p_true_ring_a.addOrReplaceChild("true_ring_a_4", CubeListBuilder.create()
                .texOffs(167, 99).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.4188F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(4.6353F, 0F, 14.2658F, 0F, -2.8274F, 0F));
        PartDefinition p_true_ring_a_5 = p_true_ring_a.addOrReplaceChild("true_ring_a_5", CubeListBuilder.create()
                .texOffs(184, 99).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.4188F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(0F, 0F, 15F, 0F, -3.1416F, 0F));
        PartDefinition p_true_ring_a_6 = p_true_ring_a.addOrReplaceChild("true_ring_a_6", CubeListBuilder.create()
                .texOffs(201, 99).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.4188F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(-4.6353F, 0F, 14.2658F, 0F, -3.4558F, 0F));
        PartDefinition p_true_ring_a_7 = p_true_ring_a.addOrReplaceChild("true_ring_a_7", CubeListBuilder.create()
                .texOffs(218, 99).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.4188F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(-8.8168F, 0F, 12.1353F, 0F, -3.7699F, 0F));
        PartDefinition p_true_ring_a_8 = p_true_ring_a.addOrReplaceChild("true_ring_a_8", CubeListBuilder.create()
                .texOffs(235, 99).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.4188F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(-12.1353F, 0F, 8.8168F, 0F, -4.0841F, 0F));
        PartDefinition p_true_ring_a_9 = p_true_ring_a.addOrReplaceChild("true_ring_a_9", CubeListBuilder.create()
                .texOffs(252, 99).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.4188F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(-14.2658F, 0F, 4.6353F, 0F, -4.3982F, 0F));
        PartDefinition p_true_ring_a_10 = p_true_ring_a.addOrReplaceChild("true_ring_a_10", CubeListBuilder.create()
                .texOffs(269, 99).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.4188F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(-15F, 0F, 0F, 0F, -4.7124F, 0F));
        PartDefinition p_true_ring_a_11 = p_true_ring_a.addOrReplaceChild("true_ring_a_11", CubeListBuilder.create()
                .texOffs(286, 99).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.4188F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(-14.2658F, 0F, -4.6353F, 0F, -5.0265F, 0F));
        PartDefinition p_true_ring_a_12 = p_true_ring_a.addOrReplaceChild("true_ring_a_12", CubeListBuilder.create()
                .texOffs(303, 99).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.4188F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(-12.1353F, 0F, -8.8168F, 0F, -5.3407F, 0F));
        PartDefinition p_true_ring_a_13 = p_true_ring_a.addOrReplaceChild("true_ring_a_13", CubeListBuilder.create()
                .texOffs(0, 105).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.4188F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(-8.8168F, 0F, -12.1353F, 0F, -5.6549F, 0F));
        PartDefinition p_true_ring_a_14 = p_true_ring_a.addOrReplaceChild("true_ring_a_14", CubeListBuilder.create()
                .texOffs(17, 105).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.4188F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(-4.6353F, 0F, -14.2658F, 0F, -5.969F, 0F));
        PartDefinition p_true_ring_a_15 = p_true_ring_a.addOrReplaceChild("true_ring_a_15", CubeListBuilder.create()
                .texOffs(34, 105).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.4188F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(0F, 0F, -15F, 0F, -6.2832F, 0F));
        PartDefinition p_true_ring_a_16 = p_true_ring_a.addOrReplaceChild("true_ring_a_16", CubeListBuilder.create()
                .texOffs(51, 105).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.4188F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(4.6353F, 0F, -14.2658F, 0F, -6.5973F, 0F));
        PartDefinition p_true_ring_a_17 = p_true_ring_a.addOrReplaceChild("true_ring_a_17", CubeListBuilder.create()
                .texOffs(68, 105).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.4188F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(8.8168F, 0F, -12.1353F, 0F, -6.9115F, 0F));
        PartDefinition p_true_ring_a_18 = p_true_ring_a.addOrReplaceChild("true_ring_a_18", CubeListBuilder.create()
                .texOffs(85, 105).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.4188F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(12.1353F, 0F, -8.8168F, 0F, -7.2257F, 0F));
        PartDefinition p_true_ring_a_19 = p_true_ring_a.addOrReplaceChild("true_ring_a_19", CubeListBuilder.create()
                .texOffs(102, 105).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.4188F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(14.2658F, 0F, -4.6353F, 0F, -7.5398F, 0F));
        PartDefinition p_true_ring_b = p_true_rings.addOrReplaceChild("true_ring_b", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0F, 0F, 0F, 1.5708F, 0F, 0F));
        PartDefinition p_true_ring_b_0 = p_true_ring_b.addOrReplaceChild("true_ring_b_0", CubeListBuilder.create()
                .texOffs(119, 105).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.276F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(17.5F, 0F, 0F, 0F, -1.5708F, 0F));
        PartDefinition p_true_ring_b_1 = p_true_ring_b.addOrReplaceChild("true_ring_b_1", CubeListBuilder.create()
                .texOffs(136, 105).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.276F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(16.7911F, 0F, 4.9303F, 0F, -1.8564F, 0F));
        PartDefinition p_true_ring_b_2 = p_true_ring_b.addOrReplaceChild("true_ring_b_2", CubeListBuilder.create()
                .texOffs(153, 105).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.276F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(14.7219F, 0F, 9.4612F, 0F, -2.142F, 0F));
        PartDefinition p_true_ring_b_3 = p_true_ring_b.addOrReplaceChild("true_ring_b_3", CubeListBuilder.create()
                .texOffs(170, 105).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.276F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(11.4601F, 0F, 13.2256F, 0F, -2.4276F, 0F));
        PartDefinition p_true_ring_b_4 = p_true_ring_b.addOrReplaceChild("true_ring_b_4", CubeListBuilder.create()
                .texOffs(187, 105).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.276F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(7.2698F, 0F, 15.9186F, 0F, -2.7132F, 0F));
        PartDefinition p_true_ring_b_5 = p_true_ring_b.addOrReplaceChild("true_ring_b_5", CubeListBuilder.create()
                .texOffs(204, 105).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.276F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(2.4905F, 0F, 17.3219F, 0F, -2.9988F, 0F));
        PartDefinition p_true_ring_b_6 = p_true_ring_b.addOrReplaceChild("true_ring_b_6", CubeListBuilder.create()
                .texOffs(221, 105).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.276F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(-2.4905F, 0F, 17.3219F, 0F, -3.2844F, 0F));
        PartDefinition p_true_ring_b_7 = p_true_ring_b.addOrReplaceChild("true_ring_b_7", CubeListBuilder.create()
                .texOffs(238, 105).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.276F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(-7.2698F, 0F, 15.9186F, 0F, -3.57F, 0F));
        PartDefinition p_true_ring_b_8 = p_true_ring_b.addOrReplaceChild("true_ring_b_8", CubeListBuilder.create()
                .texOffs(255, 105).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.276F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(-11.4601F, 0F, 13.2256F, 0F, -3.8556F, 0F));
        PartDefinition p_true_ring_b_9 = p_true_ring_b.addOrReplaceChild("true_ring_b_9", CubeListBuilder.create()
                .texOffs(272, 105).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.276F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(-14.7219F, 0F, 9.4612F, 0F, -4.1412F, 0F));
        PartDefinition p_true_ring_b_10 = p_true_ring_b.addOrReplaceChild("true_ring_b_10", CubeListBuilder.create()
                .texOffs(289, 105).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.276F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(-16.7911F, 0F, 4.9303F, 0F, -4.4268F, 0F));
        PartDefinition p_true_ring_b_11 = p_true_ring_b.addOrReplaceChild("true_ring_b_11", CubeListBuilder.create()
                .texOffs(0, 111).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.276F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(-17.5F, 0F, 0F, 0F, -4.7124F, 0F));
        PartDefinition p_true_ring_b_12 = p_true_ring_b.addOrReplaceChild("true_ring_b_12", CubeListBuilder.create()
                .texOffs(17, 111).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.276F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(-16.7911F, 0F, -4.9303F, 0F, -4.998F, 0F));
        PartDefinition p_true_ring_b_13 = p_true_ring_b.addOrReplaceChild("true_ring_b_13", CubeListBuilder.create()
                .texOffs(34, 111).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.276F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(-14.7219F, 0F, -9.4612F, 0F, -5.2836F, 0F));
        PartDefinition p_true_ring_b_14 = p_true_ring_b.addOrReplaceChild("true_ring_b_14", CubeListBuilder.create()
                .texOffs(51, 111).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.276F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(-11.4601F, 0F, -13.2256F, 0F, -5.5692F, 0F));
        PartDefinition p_true_ring_b_15 = p_true_ring_b.addOrReplaceChild("true_ring_b_15", CubeListBuilder.create()
                .texOffs(68, 111).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.276F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(-7.2698F, 0F, -15.9186F, 0F, -5.8548F, 0F));
        PartDefinition p_true_ring_b_16 = p_true_ring_b.addOrReplaceChild("true_ring_b_16", CubeListBuilder.create()
                .texOffs(85, 111).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.276F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(-2.4905F, 0F, -17.3219F, 0F, -6.1404F, 0F));
        PartDefinition p_true_ring_b_17 = p_true_ring_b.addOrReplaceChild("true_ring_b_17", CubeListBuilder.create()
                .texOffs(102, 111).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.276F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(2.4905F, 0F, -17.3219F, 0F, -6.426F, 0F));
        PartDefinition p_true_ring_b_18 = p_true_ring_b.addOrReplaceChild("true_ring_b_18", CubeListBuilder.create()
                .texOffs(119, 111).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.276F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(7.2698F, 0F, -15.9186F, 0F, -6.7116F, 0F));
        PartDefinition p_true_ring_b_19 = p_true_ring_b.addOrReplaceChild("true_ring_b_19", CubeListBuilder.create()
                .texOffs(136, 111).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.276F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(11.4601F, 0F, -13.2256F, 0F, -6.9972F, 0F));
        PartDefinition p_true_ring_b_20 = p_true_ring_b.addOrReplaceChild("true_ring_b_20", CubeListBuilder.create()
                .texOffs(153, 111).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.276F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(14.7219F, 0F, -9.4612F, 0F, -7.2828F, 0F));
        PartDefinition p_true_ring_b_21 = p_true_ring_b.addOrReplaceChild("true_ring_b_21", CubeListBuilder.create()
                .texOffs(170, 111).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.276F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(16.7911F, 0F, -4.9303F, 0F, -7.5684F, 0F));
        PartDefinition p_almighty_ring = p_true_rings.addOrReplaceChild("almighty_ring", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0F, -1F, 12F, 1.5708F, 0F, 0F));
        PartDefinition p_almighty_ring_spin = p_almighty_ring.addOrReplaceChild("almighty_ring_spin", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_almighty_ring_0 = p_almighty_ring_spin.addOrReplaceChild("almighty_ring_0", CubeListBuilder.create()
                .texOffs(187, 111).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.1944F, -0.1F, -0.25F)),
                PartPose.offsetAndRotation(23F, 0F, 0F, 0F, -1.5708F, 0F));
        PartDefinition p_almighty_ring_1 = p_almighty_ring_spin.addOrReplaceChild("almighty_ring_1", CubeListBuilder.create()
                .texOffs(204, 111).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.1944F, -0.1F, -0.25F)),
                PartPose.offsetAndRotation(22.4233F, 0F, 5.118F, 0F, -1.7952F, 0F));
        PartDefinition p_almighty_ring_2 = p_almighty_ring_spin.addOrReplaceChild("almighty_ring_2", CubeListBuilder.create()
                .texOffs(221, 111).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.1944F, -0.1F, -0.25F)),
                PartPose.offsetAndRotation(20.7223F, 0F, 9.9793F, 0F, -2.0196F, 0F));
        PartDefinition p_almighty_ring_3 = p_almighty_ring_spin.addOrReplaceChild("almighty_ring_3", CubeListBuilder.create()
                .texOffs(238, 111).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.1944F, -0.1F, -0.25F)),
                PartPose.offsetAndRotation(17.9821F, 0F, 14.3403F, 0F, -2.244F, 0F));
        PartDefinition p_almighty_ring_4 = p_almighty_ring_spin.addOrReplaceChild("almighty_ring_4", CubeListBuilder.create()
                .texOffs(255, 111).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.1944F, -0.1F, -0.25F)),
                PartPose.offsetAndRotation(14.3403F, 0F, 17.9821F, 0F, -2.4684F, 0F));
        PartDefinition p_almighty_ring_5 = p_almighty_ring_spin.addOrReplaceChild("almighty_ring_5", CubeListBuilder.create()
                .texOffs(272, 111).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.1944F, -0.1F, -0.25F)),
                PartPose.offsetAndRotation(9.9793F, 0F, 20.7223F, 0F, -2.6928F, 0F));
        PartDefinition p_almighty_ring_6 = p_almighty_ring_spin.addOrReplaceChild("almighty_ring_6", CubeListBuilder.create()
                .texOffs(289, 111).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.1944F, -0.1F, -0.25F)),
                PartPose.offsetAndRotation(5.118F, 0F, 22.4233F, 0F, -2.9172F, 0F));
        PartDefinition p_almighty_ring_7 = p_almighty_ring_spin.addOrReplaceChild("almighty_ring_7", CubeListBuilder.create()
                .texOffs(0, 117).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.1944F, -0.1F, -0.25F)),
                PartPose.offsetAndRotation(0F, 0F, 23F, 0F, -3.1416F, 0F));
        PartDefinition p_almighty_ring_8 = p_almighty_ring_spin.addOrReplaceChild("almighty_ring_8", CubeListBuilder.create()
                .texOffs(17, 117).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.1944F, -0.1F, -0.25F)),
                PartPose.offsetAndRotation(-5.118F, 0F, 22.4233F, 0F, -3.366F, 0F));
        PartDefinition p_almighty_ring_9 = p_almighty_ring_spin.addOrReplaceChild("almighty_ring_9", CubeListBuilder.create()
                .texOffs(34, 117).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.1944F, -0.1F, -0.25F)),
                PartPose.offsetAndRotation(-9.9793F, 0F, 20.7223F, 0F, -3.5904F, 0F));
        PartDefinition p_almighty_ring_10 = p_almighty_ring_spin.addOrReplaceChild("almighty_ring_10", CubeListBuilder.create()
                .texOffs(51, 117).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.1944F, -0.1F, -0.25F)),
                PartPose.offsetAndRotation(-14.3403F, 0F, 17.9821F, 0F, -3.8148F, 0F));
        PartDefinition p_almighty_ring_11 = p_almighty_ring_spin.addOrReplaceChild("almighty_ring_11", CubeListBuilder.create()
                .texOffs(68, 117).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.1944F, -0.1F, -0.25F)),
                PartPose.offsetAndRotation(-17.9821F, 0F, 14.3403F, 0F, -4.0392F, 0F));
        PartDefinition p_almighty_ring_12 = p_almighty_ring_spin.addOrReplaceChild("almighty_ring_12", CubeListBuilder.create()
                .texOffs(85, 117).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.1944F, -0.1F, -0.25F)),
                PartPose.offsetAndRotation(-20.7223F, 0F, 9.9793F, 0F, -4.2636F, 0F));
        PartDefinition p_almighty_ring_13 = p_almighty_ring_spin.addOrReplaceChild("almighty_ring_13", CubeListBuilder.create()
                .texOffs(102, 117).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.1944F, -0.1F, -0.25F)),
                PartPose.offsetAndRotation(-22.4233F, 0F, 5.118F, 0F, -4.488F, 0F));
        PartDefinition p_almighty_ring_14 = p_almighty_ring_spin.addOrReplaceChild("almighty_ring_14", CubeListBuilder.create()
                .texOffs(119, 117).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.1944F, -0.1F, -0.25F)),
                PartPose.offsetAndRotation(-23F, 0F, 0F, 0F, -4.7124F, 0F));
        PartDefinition p_almighty_ring_15 = p_almighty_ring_spin.addOrReplaceChild("almighty_ring_15", CubeListBuilder.create()
                .texOffs(136, 117).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.1944F, -0.1F, -0.25F)),
                PartPose.offsetAndRotation(-22.4233F, 0F, -5.118F, 0F, -4.9368F, 0F));
        PartDefinition p_almighty_ring_16 = p_almighty_ring_spin.addOrReplaceChild("almighty_ring_16", CubeListBuilder.create()
                .texOffs(153, 117).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.1944F, -0.1F, -0.25F)),
                PartPose.offsetAndRotation(-20.7223F, 0F, -9.9793F, 0F, -5.1612F, 0F));
        PartDefinition p_almighty_ring_17 = p_almighty_ring_spin.addOrReplaceChild("almighty_ring_17", CubeListBuilder.create()
                .texOffs(170, 117).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.1944F, -0.1F, -0.25F)),
                PartPose.offsetAndRotation(-17.9821F, 0F, -14.3403F, 0F, -5.3856F, 0F));
        PartDefinition p_almighty_ring_18 = p_almighty_ring_spin.addOrReplaceChild("almighty_ring_18", CubeListBuilder.create()
                .texOffs(187, 117).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.1944F, -0.1F, -0.25F)),
                PartPose.offsetAndRotation(-14.3403F, 0F, -17.9821F, 0F, -5.61F, 0F));
        PartDefinition p_almighty_ring_19 = p_almighty_ring_spin.addOrReplaceChild("almighty_ring_19", CubeListBuilder.create()
                .texOffs(204, 117).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.1944F, -0.1F, -0.25F)),
                PartPose.offsetAndRotation(-9.9793F, 0F, -20.7223F, 0F, -5.8344F, 0F));
        PartDefinition p_almighty_ring_20 = p_almighty_ring_spin.addOrReplaceChild("almighty_ring_20", CubeListBuilder.create()
                .texOffs(221, 117).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.1944F, -0.1F, -0.25F)),
                PartPose.offsetAndRotation(-5.118F, 0F, -22.4233F, 0F, -6.0588F, 0F));
        PartDefinition p_almighty_ring_21 = p_almighty_ring_spin.addOrReplaceChild("almighty_ring_21", CubeListBuilder.create()
                .texOffs(238, 117).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.1944F, -0.1F, -0.25F)),
                PartPose.offsetAndRotation(0F, 0F, -23F, 0F, -6.2832F, 0F));
        PartDefinition p_almighty_ring_22 = p_almighty_ring_spin.addOrReplaceChild("almighty_ring_22", CubeListBuilder.create()
                .texOffs(255, 117).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.1944F, -0.1F, -0.25F)),
                PartPose.offsetAndRotation(5.118F, 0F, -22.4233F, 0F, -6.5076F, 0F));
        PartDefinition p_almighty_ring_23 = p_almighty_ring_spin.addOrReplaceChild("almighty_ring_23", CubeListBuilder.create()
                .texOffs(272, 117).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.1944F, -0.1F, -0.25F)),
                PartPose.offsetAndRotation(9.9793F, 0F, -20.7223F, 0F, -6.732F, 0F));
        PartDefinition p_almighty_ring_24 = p_almighty_ring_spin.addOrReplaceChild("almighty_ring_24", CubeListBuilder.create()
                .texOffs(289, 117).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.1944F, -0.1F, -0.25F)),
                PartPose.offsetAndRotation(14.3403F, 0F, -17.9821F, 0F, -6.9564F, 0F));
        PartDefinition p_almighty_ring_25 = p_almighty_ring_spin.addOrReplaceChild("almighty_ring_25", CubeListBuilder.create()
                .texOffs(0, 123).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.1944F, -0.1F, -0.25F)),
                PartPose.offsetAndRotation(17.9821F, 0F, -14.3403F, 0F, -7.1808F, 0F));
        PartDefinition p_almighty_ring_26 = p_almighty_ring_spin.addOrReplaceChild("almighty_ring_26", CubeListBuilder.create()
                .texOffs(17, 123).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.1944F, -0.1F, -0.25F)),
                PartPose.offsetAndRotation(20.7223F, 0F, -9.9793F, 0F, -7.4052F, 0F));
        PartDefinition p_almighty_ring_27 = p_almighty_ring_spin.addOrReplaceChild("almighty_ring_27", CubeListBuilder.create()
                .texOffs(34, 123).addBox(-3F, -1.5F, -1F, 6F, 3F, 2F, new CubeDeformation(-0.1944F, -0.1F, -0.25F)),
                PartPose.offsetAndRotation(22.4233F, 0F, -5.118F, 0F, -7.6296F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(223, 0).addBox(-2.5F, 0F, -2.5F, 5F, 11F, 5F, new CubeDeformation(-0.4F, 0F, -0.4F)),
                PartPose.offset(3F, 0.6F, 0.2F));
        PartDefinition p_left_shin = p_left_leg.addOrReplaceChild("left_shin", CubeListBuilder.create()
                .texOffs(279, 0).addBox(-2F, -0.2F, -2.5F, 4F, 10F, 5F, new CubeDeformation(0F, -0.2F, -0.4F))
                .texOffs(75, 91).addBox(-2.5F, -2.1F, -2.75F, 5F, 4F, 2F, new CubeDeformation(-0.15F, -0.4F, -0.05F))
                .texOffs(37, 59).addBox(2F, 2.9F, -1.8F, 0F, 6F, 5F, new CubeDeformation(0F, -0.3F, -0.2F)),
                PartPose.offset(0F, 11F, 0F));
        PartDefinition p_left_foot = p_left_shin.addOrReplaceChild("left_foot", CubeListBuilder.create()
                .texOffs(200, 59).addBox(-2.5F, -0.1F, -4.7F, 5F, 3F, 7F, new CubeDeformation(-0.35F, -0.1F, -0.4F))
                .texOffs(75, 139).addBox(-1.5F, 0.85F, -5.95F, 3F, 2F, 2F, new CubeDeformation(0F, -0.05F, -0.25F)),
                PartPose.offset(0F, 9.6F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(244, 0).addBox(-2.5F, 0F, -2.5F, 5F, 11F, 5F, new CubeDeformation(-0.4F, 0F, -0.4F)),
                PartPose.offset(-3F, 0.6F, 0.2F));
        PartDefinition p_right_shin = p_right_leg.addOrReplaceChild("right_shin", CubeListBuilder.create()
                .texOffs(298, 0).addBox(-2F, -0.2F, -2.5F, 4F, 10F, 5F, new CubeDeformation(0F, -0.2F, -0.4F))
                .texOffs(90, 91).addBox(-2.5F, -2.1F, -2.75F, 5F, 4F, 2F, new CubeDeformation(-0.15F, -0.4F, -0.05F))
                .texOffs(48, 59).addBox(-2F, 2.9F, -1.8F, 0F, 6F, 5F, new CubeDeformation(0F, -0.3F, -0.2F)),
                PartPose.offset(0F, 11F, 0F));
        PartDefinition p_right_foot = p_right_shin.addOrReplaceChild("right_foot", CubeListBuilder.create()
                .texOffs(225, 59).addBox(-2.5F, -0.1F, -4.7F, 5F, 3F, 7F, new CubeDeformation(-0.35F, -0.1F, -0.4F))
                .texOffs(86, 139).addBox(-1.5F, 0.85F, -5.95F, 3F, 2F, 2F, new CubeDeformation(0F, -0.05F, -0.25F)),
                PartPose.offset(0F, 9.6F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
