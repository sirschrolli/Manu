package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie von Terragon, dem Erddrachen-Boss (Grundgröße, im Spiel ×2,2).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class TerragonGeometry {
    public static final int TEXTURE_WIDTH = 512;
    public static final int TEXTURE_HEIGHT = 512;

    private TerragonGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-16F, -11F, -22F, 32F, 20F, 44F)
                .texOffs(37, 65).addBox(-14F, 9F, -20F, 28F, 4F, 24F)
                .texOffs(200, 95).addBox(-13F, 9F, 4F, 26F, 3.5F, 17F)
                .texOffs(201, 65).addBox(-18F, -7F, -20F, 3F, 13F, 14F)
                .texOffs(236, 65).addBox(15F, -7F, -20F, 3F, 13F, 14F)
                .texOffs(456, 0).addBox(-17.5F, -6F, 7F, 2.5F, 14F, 15F)
                .texOffs(0, 65).addBox(15F, -6F, 7F, 2.5F, 14F, 15F),
                PartPose.offset(0F, -6F, 0F));
        PartDefinition p_plate_0_0 = p_body.addOrReplaceChild("plate_0_0", CubeListBuilder.create()
                .texOffs(351, 140).addBox(-5F, -3.2F, -1F, 10F, 3.5F, 11F)
                .texOffs(232, 186).addBox(-3.5F, -4.2F, 1F, 7F, 1.2F, 7F),
                PartPose.offsetAndRotation(-10.5F, -11.5F, -19F, -0.1047F, 0.0785F, -0.2443F));
        PartDefinition p_plate_0_1 = p_body.addOrReplaceChild("plate_0_1", CubeListBuilder.create()
                .texOffs(171, 140).addBox(-5.5F, -3.2F, -1F, 11F, 3.5F, 11F)
                .texOffs(108, 186).addBox(-4F, -4.2F, 1F, 8F, 1.2F, 7F),
                PartPose.offsetAndRotation(0F, -13F, -19F, -0.1396F, -0.0785F, 0F));
        PartDefinition p_plate_0_2 = p_body.addOrReplaceChild("plate_0_2", CubeListBuilder.create()
                .texOffs(394, 140).addBox(-5F, -3.2F, -1F, 10F, 3.5F, 11F)
                .texOffs(261, 186).addBox(-3.5F, -4.2F, 1F, 7F, 1.2F, 7F),
                PartPose.offsetAndRotation(10.5F, -11.5F, -19F, -0.1047F, -0.0785F, 0.2443F));
        PartDefinition p_plate_1_0 = p_body.addOrReplaceChild("plate_1_0", CubeListBuilder.create()
                .texOffs(437, 140).addBox(-5F, -3.2F, -1F, 10F, 3.5F, 11F)
                .texOffs(290, 186).addBox(-3.5F, -4.2F, 1F, 7F, 1.2F, 7F),
                PartPose.offsetAndRotation(-10.5F, -11.5F, -9F, -0.0698F, 0.0262F, -0.2443F));
        PartDefinition p_plate_1_1 = p_body.addOrReplaceChild("plate_1_1", CubeListBuilder.create()
                .texOffs(216, 140).addBox(-5.5F, -3.2F, -1F, 11F, 3.5F, 11F)
                .texOffs(139, 186).addBox(-4F, -4.2F, 1F, 8F, 1.2F, 7F),
                PartPose.offsetAndRotation(0F, -13F, -9F, -0.0873F, -0.0262F, 0F));
        PartDefinition p_plate_1_2 = p_body.addOrReplaceChild("plate_1_2", CubeListBuilder.create()
                .texOffs(0, 156).addBox(-5F, -3.2F, -1F, 10F, 3.5F, 11F)
                .texOffs(319, 186).addBox(-3.5F, -4.2F, 1F, 7F, 1.2F, 7F),
                PartPose.offsetAndRotation(10.5F, -11.5F, -9F, -0.0698F, -0.0262F, 0.2443F));
        PartDefinition p_plate_2_0 = p_body.addOrReplaceChild("plate_2_0", CubeListBuilder.create()
                .texOffs(43, 156).addBox(-5F, -3.2F, -1F, 10F, 3.5F, 11F)
                .texOffs(348, 186).addBox(-3.5F, -4.2F, 1F, 7F, 1.2F, 7F),
                PartPose.offsetAndRotation(-10.5F, -11.5F, 1F, -0.0349F, -0.0262F, -0.2443F));
        PartDefinition p_plate_2_1 = p_body.addOrReplaceChild("plate_2_1", CubeListBuilder.create()
                .texOffs(261, 140).addBox(-5.5F, -3.2F, -1F, 11F, 3.5F, 11F)
                .texOffs(170, 186).addBox(-4F, -4.2F, 1F, 8F, 1.2F, 7F),
                PartPose.offsetAndRotation(0F, -13F, 1F, -0.0349F, 0.0262F, 0F));
        PartDefinition p_plate_2_2 = p_body.addOrReplaceChild("plate_2_2", CubeListBuilder.create()
                .texOffs(86, 156).addBox(-5F, -3.2F, -1F, 10F, 3.5F, 11F)
                .texOffs(377, 186).addBox(-3.5F, -4.2F, 1F, 7F, 1.2F, 7F),
                PartPose.offsetAndRotation(10.5F, -11.5F, 1F, -0.0349F, 0.0262F, 0.2443F));
        PartDefinition p_plate_3_0 = p_body.addOrReplaceChild("plate_3_0", CubeListBuilder.create()
                .texOffs(129, 156).addBox(-5F, -3.2F, -1F, 10F, 3.5F, 11F)
                .texOffs(406, 186).addBox(-3.5F, -4.2F, 1F, 7F, 1.2F, 7F),
                PartPose.offsetAndRotation(-10.5F, -11.5F, 11F, 0F, -0.0785F, -0.2443F));
        PartDefinition p_plate_3_1 = p_body.addOrReplaceChild("plate_3_1", CubeListBuilder.create()
                .texOffs(306, 140).addBox(-5.5F, -3.2F, -1F, 11F, 3.5F, 11F)
                .texOffs(201, 186).addBox(-4F, -4.2F, 1F, 8F, 1.2F, 7F),
                PartPose.offsetAndRotation(0F, -13F, 11F, 0.0175F, 0.0785F, 0F));
        PartDefinition p_plate_3_2 = p_body.addOrReplaceChild("plate_3_2", CubeListBuilder.create()
                .texOffs(172, 156).addBox(-5F, -3.2F, -1F, 10F, 3.5F, 11F)
                .texOffs(435, 186).addBox(-3.5F, -4.2F, 1F, 7F, 1.2F, 7F),
                PartPose.offsetAndRotation(10.5F, -11.5F, 11F, 0F, 0.0785F, 0.2443F));
        PartDefinition p_crystal_0_0 = p_body.addOrReplaceChild("crystal_0_0", CubeListBuilder.create()
                .texOffs(52, 197).addBox(-1F, -6F, -1F, 2F, 7F, 2F)
                .texOffs(9, 207).addBox(-0.5F, -7.6F, -0.5F, 1F, 1.8F, 1F),
                PartPose.offsetAndRotation(-7.6F, -14F, -15.4F, -0.3142F, 0F, -0.3142F));
        PartDefinition p_crystal_0_1 = p_body.addOrReplaceChild("crystal_0_1", CubeListBuilder.create()
                .texOffs(355, 156).addBox(-1.3F, -9.2F, -1.3F, 2.6F, 10.2F, 2.6F)
                .texOffs(418, 197).addBox(-0.65F, -10.8F, -0.65F, 1.3F, 1.8F, 1.3F),
                PartPose.offsetAndRotation(-6F, -14F, -14F, -0.3142F, 0F, 0.1571F));
        PartDefinition p_crystal_0_2 = p_body.addOrReplaceChild("crystal_0_2", CubeListBuilder.create()
                .texOffs(215, 119).addBox(-1.6F, -12.4F, -1.6F, 3.2F, 13.4F, 3.2F)
                .texOffs(427, 197).addBox(-0.8F, -14F, -0.8F, 1.6F, 1.8F, 1.6F),
                PartPose.offsetAndRotation(-4.4F, -14F, -12.6F, -0.3142F, 0F, -0.1571F));
        PartDefinition p_crystal_1_0 = p_body.addOrReplaceChild("crystal_1_0", CubeListBuilder.create()
                .texOffs(248, 156).addBox(-1.3F, -10.8F, -1.3F, 2.6F, 11.8F, 2.6F)
                .texOffs(436, 197).addBox(-0.65F, -12.4F, -0.65F, 1.3F, 1.8F, 1.3F),
                PartPose.offsetAndRotation(5.4F, -14.5F, -5.4F, -0.1571F, 0F, 0F));
        PartDefinition p_crystal_1_1 = p_body.addOrReplaceChild("crystal_1_1", CubeListBuilder.create()
                .texOffs(386, 172).addBox(-1.6F, -6F, -1.6F, 3.2F, 7F, 3.2F)
                .texOffs(445, 197).addBox(-0.8F, -7.6F, -0.8F, 1.6F, 1.8F, 1.6F),
                PartPose.offsetAndRotation(7F, -14.5F, -4F, -0.1571F, 0F, -0.3142F));
        PartDefinition p_crystal_1_2 = p_body.addOrReplaceChild("crystal_1_2", CubeListBuilder.create()
                .texOffs(0, 172).addBox(-1F, -9.2F, -1F, 2F, 10.2F, 2F)
                .texOffs(14, 207).addBox(-0.5F, -10.8F, -0.5F, 1F, 1.8F, 1F),
                PartPose.offsetAndRotation(8.6F, -14.5F, -2.6F, -0.1571F, 0F, 0.1571F));
        PartDefinition p_crystal_1_3 = p_body.addOrReplaceChild("crystal_1_3", CubeListBuilder.create()
                .texOffs(398, 119).addBox(-1.3F, -12.4F, -1.3F, 2.6F, 13.4F, 2.6F)
                .texOffs(454, 197).addBox(-0.65F, -14F, -0.65F, 1.3F, 1.8F, 1.3F),
                PartPose.offsetAndRotation(10.2F, -14.5F, -5.4F, -0.1571F, 0F, -0.1571F));
        PartDefinition p_crystal_2_0 = p_body.addOrReplaceChild("crystal_2_0", CubeListBuilder.create()
                .texOffs(488, 156).addBox(-1.6F, -7.6F, -1.6F, 3.2F, 8.6F, 3.2F)
                .texOffs(463, 197).addBox(-0.8F, -9.2F, -0.8F, 1.6F, 1.8F, 1.6F),
                PartPose.offsetAndRotation(-3.6F, -15F, 4.6F, 0F, 0F, 0.3142F));
        PartDefinition p_crystal_2_1 = p_body.addOrReplaceChild("crystal_2_1", CubeListBuilder.create()
                .texOffs(381, 156).addBox(-1F, -10.8F, -1F, 2F, 11.8F, 2F)
                .texOffs(19, 207).addBox(-0.5F, -12.4F, -0.5F, 1F, 1.8F, 1F),
                PartPose.offset(-2F, -15F, 6F));
        PartDefinition p_crystal_2_2 = p_body.addOrReplaceChild("crystal_2_2", CubeListBuilder.create()
                .texOffs(58, 186).addBox(-1.3F, -6F, -1.3F, 2.6F, 7F, 2.6F)
                .texOffs(472, 197).addBox(-0.65F, -7.6F, -0.65F, 1.3F, 1.8F, 1.3F),
                PartPose.offsetAndRotation(-0.4F, -15F, 7.4F, 0F, 0F, -0.3142F));
        PartDefinition p_crystal_3_0 = p_body.addOrReplaceChild("crystal_3_0", CubeListBuilder.create()
                .texOffs(494, 119).addBox(-1F, -12.4F, -1F, 2F, 13.4F, 2F)
                .texOffs(24, 207).addBox(-0.5F, -14F, -0.5F, 1F, 1.8F, 1F),
                PartPose.offsetAndRotation(7.4F, -13.5F, 12.6F, 0.1571F, 0F, -0.1571F));
        PartDefinition p_crystal_3_1 = p_body.addOrReplaceChild("crystal_3_1", CubeListBuilder.create()
                .texOffs(233, 172).addBox(-1.3F, -7.6F, -1.3F, 2.6F, 8.6F, 2.6F)
                .texOffs(481, 197).addBox(-0.65F, -9.2F, -0.65F, 1.3F, 1.8F, 1.3F),
                PartPose.offsetAndRotation(9F, -13.5F, 14F, 0.1571F, 0F, 0.3142F));
        PartDefinition p_crystal_3_2 = p_body.addOrReplaceChild("crystal_3_2", CubeListBuilder.create()
                .texOffs(477, 119).addBox(-1.6F, -10.8F, -1.6F, 3.2F, 11.8F, 3.2F)
                .texOffs(490, 197).addBox(-0.8F, -12.4F, -0.8F, 1.6F, 1.8F, 1.6F),
                PartPose.offsetAndRotation(10.6F, -13.5F, 15.4F, 0.1571F, 0F, 0F));
        PartDefinition p_crystal_3_3 = p_body.addOrReplaceChild("crystal_3_3", CubeListBuilder.create()
                .texOffs(61, 197).addBox(-1F, -6F, -1F, 2F, 7F, 2F)
                .texOffs(29, 207).addBox(-0.5F, -7.6F, -0.5F, 1F, 1.8F, 1F),
                PartPose.offsetAndRotation(12.2F, -13.5F, 12.6F, 0.1571F, 0F, -0.3142F));
        PartDefinition p_crystal_4_0 = p_body.addOrReplaceChild("crystal_4_0", CubeListBuilder.create()
                .texOffs(368, 156).addBox(-1.3F, -9.2F, -1.3F, 2.6F, 10.2F, 2.6F)
                .texOffs(499, 197).addBox(-0.65F, -10.8F, -0.65F, 1.3F, 1.8F, 1.3F),
                PartPose.offsetAndRotation(-11.6F, -13F, 13.6F, 0.3142F, 0F, 0.1571F));
        PartDefinition p_crystal_4_1 = p_body.addOrReplaceChild("crystal_4_1", CubeListBuilder.create()
                .texOffs(232, 119).addBox(-1.6F, -12.4F, -1.6F, 3.2F, 13.4F, 3.2F)
                .texOffs(0, 207).addBox(-0.8F, -14F, -0.8F, 1.6F, 1.8F, 1.6F),
                PartPose.offsetAndRotation(-10F, -13F, 15F, 0.3142F, 0F, -0.1571F));
        PartDefinition p_crystal_4_2 = p_body.addOrReplaceChild("crystal_4_2", CubeListBuilder.create()
                .texOffs(403, 172).addBox(-1F, -7.6F, -1F, 2F, 8.6F, 2F)
                .texOffs(34, 207).addBox(-0.5F, -9.2F, -0.5F, 1F, 1.8F, 1F),
                PartPose.offsetAndRotation(-8.4F, -13F, 16.4F, 0.3142F, 0F, 0.3142F));
        PartDefinition p_neck1 = p_body.addOrReplaceChild("neck1", CubeListBuilder.create()
                .texOffs(330, 65).addBox(-9F, -8.5F, -7.5F, 18F, 17F, 8F)
                .texOffs(70, 197).addBox(-8F, 8F, -6.5F, 16F, 1.5F, 6F)
                .texOffs(412, 172).addBox(-7F, -11F, -7F, 14F, 3F, 7F),
                PartPose.offsetAndRotation(0F, -5F, -21F, -0.2443F, 0F, 0F));
        PartDefinition p_neck2 = p_neck1.addOrReplaceChild("neck2", CubeListBuilder.create()
                .texOffs(436, 65).addBox(-8.5F, -8F, -7F, 17F, 16F, 7.5F)
                .texOffs(115, 197).addBox(-7.5F, 7.5F, -6F, 15F, 1.5F, 5.5F)
                .texOffs(455, 172).addBox(-6.5F, -10.5F, -6.5F, 13F, 3F, 6.5F),
                PartPose.offsetAndRotation(0F, 0F, -7F, -0.0698F, 0F, 0F));
        PartDefinition p_neck3 = p_neck2.addOrReplaceChild("neck3", CubeListBuilder.create()
                .texOffs(153, 95).addBox(-8F, -7.5F, -6.5F, 16F, 15F, 7F)
                .texOffs(158, 197).addBox(-7F, 7F, -5.5F, 14F, 1.5F, 5F)
                .texOffs(71, 186).addBox(-6F, -10F, -6F, 12F, 3F, 6F),
                PartPose.offsetAndRotation(0F, 0F, -6.5F, 0.1047F, 0F, 0F));
        PartDefinition p_head = p_neck3.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(279, 0).addBox(-9F, -8F, -17F, 18F, 15F, 17F)
                .texOffs(0, 140).addBox(-10F, -10.5F, -17.5F, 20F, 4F, 11F)
                .texOffs(0, 95).addBox(-7F, -5F, -29.5F, 14F, 10F, 13F)
                .texOffs(310, 156).addBox(-5.5F, -7F, -28F, 11F, 2.5F, 11F)
                .texOffs(396, 197).addBox(-5F, -3.5F, -29.8F, 3F, 2F, 1.5F)
                .texOffs(407, 197).addBox(2F, -3.5F, -29.8F, 3F, 2F, 1.5F)
                .texOffs(118, 140).addBox(-6.5F, 4.5F, -29F, 13F, 1.5F, 12.5F)
                .texOffs(63, 140).addBox(-6.5F, 4.4F, -28.9F, 13F, 0.2F, 13.5F)
                .texOffs(308, 197).addBox(8.6F, -5.2F, -15.5F, 1F, 2F, 3.4F)
                .texOffs(319, 197).addBox(-9.6F, -5.2F, -15.5F, 1F, 2F, 3.4F)
                .texOffs(43, 119).addBox(8.8F, -2F, -16F, 1.8F, 8F, 11F)
                .texOffs(70, 119).addBox(-10.6F, -2F, -16F, 1.8F, 8F, 11F),
                PartPose.offsetAndRotation(0F, 0F, -6F, 0.2094F, 0F, 0F));
        PartDefinition p_nose_horn = p_head.addOrReplaceChild("nose_horn", CubeListBuilder.create()
                .texOffs(489, 186).addBox(-2F, -2F, -0.5F, 4F, 4F, 5F)
                .texOffs(197, 197).addBox(-1.2F, -1.2F, 4F, 2.4F, 2.4F, 4F),
                PartPose.offsetAndRotation(0F, -7.5F, -25F, 0.9599F, 0F, 0F));
        PartDefinition p_horn_l1 = p_head.addOrReplaceChild("horn_l1", CubeListBuilder.create()
                .texOffs(411, 119).addBox(-2.2F, -2.2F, -10F, 4.4F, 4.4F, 10.5F),
                PartPose.offsetAndRotation(6F, -9F, -10F, -0.3491F, 0.3142F, 0F));
        PartDefinition p_horn_l2 = p_horn_l1.addOrReplaceChild("horn_l2", CubeListBuilder.create()
                .texOffs(183, 172).addBox(-1.4F, -1.4F, -8F, 2.8F, 2.8F, 8.5F),
                PartPose.offsetAndRotation(0F, 0F, -10F, -0.2094F, -0.1047F, 0F));
        PartDefinition p_horn_r1 = p_head.addOrReplaceChild("horn_r1", CubeListBuilder.create()
                .texOffs(444, 119).addBox(-2.2F, -2.2F, -10F, 4.4F, 4.4F, 10.5F),
                PartPose.offsetAndRotation(-6F, -9F, -10F, -0.3491F, -0.3142F, 0F));
        PartDefinition p_horn_r2 = p_horn_r1.addOrReplaceChild("horn_r2", CubeListBuilder.create()
                .texOffs(208, 172).addBox(-1.4F, -1.4F, -8F, 2.8F, 2.8F, 8.5F),
                PartPose.offsetAndRotation(0F, 0F, -10F, -0.2094F, 0.1047F, 0F));
        PartDefinition p_jaw = p_head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(287, 95).addBox(-7F, 0F, -15.5F, 14F, 5F, 15.5F)
                .texOffs(9, 172).addBox(-6F, -1.3F, -15F, 12F, 1.3F, 10F)
                .texOffs(261, 156).addBox(-5.4F, -0.2F, -14.5F, 10.8F, 0.4F, 13F)
                .texOffs(212, 197).addBox(4.3F, -4.5F, -14.5F, 1.8F, 5F, 1.8F)
                .texOffs(221, 197).addBox(-6.1F, -4.5F, -14.5F, 1.8F, 5F, 1.8F),
                PartPose.offsetAndRotation(0F, 4.5F, -14F, 0.0524F, 0F, 0F));
        PartDefinition p_tail1 = p_body.addOrReplaceChild("tail1", CubeListBuilder.create()
                .texOffs(142, 65).addBox(-8F, -7F, -0.5F, 16F, 14F, 13F)
                .texOffs(390, 156).addBox(-7F, 6.5F, 0.5F, 14F, 1.5F, 11F)
                .texOffs(441, 156).addBox(-6.5F, -9.5F, 1F, 13F, 3F, 10F),
                PartPose.offsetAndRotation(0F, -2F, 21F, -0.1396F, 0F, 0F));
        PartDefinition p_tail2 = p_tail1.addOrReplaceChild("tail2", CubeListBuilder.create()
                .texOffs(383, 65).addBox(-7F, -6F, -0.5F, 14F, 12F, 12F)
                .texOffs(54, 172).addBox(-6F, 5.5F, 0.5F, 12F, 1.5F, 10F)
                .texOffs(142, 172).addBox(-5.5F, -8.5F, 1F, 11F, 3F, 9F),
                PartPose.offsetAndRotation(0F, 0F, 12F, -0.0698F, 0F, 0F));
        PartDefinition p_tail3 = p_tail2.addOrReplaceChild("tail3", CubeListBuilder.create()
                .texOffs(348, 95).addBox(-6F, -5F, -0.5F, 12F, 10F, 11F)
                .texOffs(246, 172).addBox(-5F, 4.5F, 0.5F, 10F, 1.5F, 9F)
                .texOffs(285, 172).addBox(-4.5F, -7.5F, 1F, 9F, 3F, 8F),
                PartPose.offset(0F, 0F, 11F));
        PartDefinition p_tail4 = p_tail3.addOrReplaceChild("tail4", CubeListBuilder.create()
                .texOffs(0, 119).addBox(-5F, -4.25F, -0.5F, 10F, 8.5F, 11F)
                .texOffs(320, 172).addBox(-4F, 3.75F, 0.5F, 8F, 1.5F, 9F)
                .texOffs(355, 172).addBox(-3.5F, -6.75F, 1F, 7F, 3F, 8F),
                PartPose.offsetAndRotation(0F, 0F, 10F, 0.0349F, 0F, 0F));
        PartDefinition p_tail5 = p_tail4.addOrReplaceChild("tail5", CubeListBuilder.create()
                .texOffs(359, 119).addBox(-4.25F, -3.5F, -0.5F, 8.5F, 7F, 10F)
                .texOffs(0, 186).addBox(-3.25F, 3F, 0.5F, 6.5F, 1.5F, 8F)
                .texOffs(31, 186).addBox(-2.75F, -6F, 1F, 5.5F, 3F, 7F),
                PartPose.offsetAndRotation(0F, 0F, 10F, 0.0524F, 0F, 0F));
        PartDefinition p_tail6 = p_tail5.addOrReplaceChild("tail6", CubeListBuilder.create()
                .texOffs(215, 156).addBox(-3.5F, -3F, -0.5F, 7F, 6F, 9F)
                .texOffs(464, 186).addBox(-2.5F, 2.5F, 0.5F, 5F, 1.5F, 7F)
                .texOffs(271, 65).addBox(-8F, -7F, 4F, 16F, 13F, 13F)
                .texOffs(99, 172).addBox(-6F, -9F, 6F, 12F, 2.5F, 9F),
                PartPose.offsetAndRotation(0F, 0F, 9F, 0.0349F, 0F, 0F));
        PartDefinition p_club_spike_0 = p_tail6.addOrReplaceChild("club_spike_0", CubeListBuilder.create()
                .texOffs(0, 197).addBox(-1.3F, -5.5F, -1.3F, 2.6F, 6F, 2.6F),
                PartPose.offsetAndRotation(-7F, -6F, 9F, 0F, 0F, 1.0472F));
        PartDefinition p_club_spike_1 = p_tail6.addOrReplaceChild("club_spike_1", CubeListBuilder.create()
                .texOffs(13, 197).addBox(-1.3F, -5.5F, -1.3F, 2.6F, 6F, 2.6F),
                PartPose.offsetAndRotation(7F, -6F, 9F, 0F, 0F, -1.0472F));
        PartDefinition p_club_spike_2 = p_tail6.addOrReplaceChild("club_spike_2", CubeListBuilder.create()
                .texOffs(26, 197).addBox(-1.3F, -5.5F, -1.3F, 2.6F, 6F, 2.6F),
                PartPose.offsetAndRotation(0F, -9F, 10F, -0.3491F, 0F, 0F));
        PartDefinition p_club_spike_3 = p_tail6.addOrReplaceChild("club_spike_3", CubeListBuilder.create()
                .texOffs(39, 197).addBox(-1.3F, -5.5F, -1.3F, 2.6F, 6F, 2.6F),
                PartPose.offsetAndRotation(0F, 4F, 10F, 3.4907F, 0F, 0F));
        PartDefinition p_front_leg_l = p_body.addOrReplaceChild("front_leg_l", CubeListBuilder.create()
                .texOffs(350, 0).addBox(-6.5F, -4.55F, -6.5F, 13F, 16.55F, 13F),
                PartPose.offsetAndRotation(12.5F, 5F, -14F, 0.0698F, 0F, 0F));
        PartDefinition p_front_shin_l = p_front_leg_l.addOrReplaceChild("front_shin_l", CubeListBuilder.create()
                .texOffs(395, 95).addBox(-5.5F, -1F, -5.5F, 11F, 10F, 11F),
                PartPose.offsetAndRotation(0F, 11.5F, 0F, -0.1047F, 0F, 0F));
        PartDefinition p_front_foot_l = p_front_shin_l.addOrReplaceChild("front_foot_l", CubeListBuilder.create()
                .texOffs(249, 119).addBox(-6.5F, 0F, -9.8F, 13F, 3F, 14F)
                .texOffs(330, 197).addBox(-5.0833F, 1.05F, -12.8F, 1.5F, 1.95F, 3F)
                .texOffs(341, 197).addBox(-0.75F, 1.05F, -12.8F, 1.5F, 1.95F, 3F)
                .texOffs(352, 197).addBox(3.5833F, 1.05F, -12.8F, 1.5F, 1.95F, 3F),
                PartPose.offsetAndRotation(0F, 8.5F, 0F, 0.0349F, 0F, 0F));
        PartDefinition p_hind_leg_l = p_body.addOrReplaceChild("hind_leg_l", CubeListBuilder.create()
                .texOffs(153, 0).addBox(-7.5F, -5.25F, -8F, 15F, 18.25F, 16F),
                PartPose.offsetAndRotation(12.5F, 4F, 13F, -0.1396F, 0F, 0F));
        PartDefinition p_hind_shin_l = p_hind_leg_l.addOrReplaceChild("hind_shin_l", CubeListBuilder.create()
                .texOffs(55, 95).addBox(-6F, -1F, -6F, 12F, 10F, 12F),
                PartPose.offsetAndRotation(0F, 12.5F, 0F, 0.2793F, 0F, 0F));
        PartDefinition p_hind_foot_l = p_hind_shin_l.addOrReplaceChild("hind_foot_l", CubeListBuilder.create()
                .texOffs(97, 119).addBox(-7F, 0F, -10.5F, 14F, 3F, 15F)
                .texOffs(230, 197).addBox(-5.4167F, 1.05F, -13.7F, 1.5F, 1.95F, 3.2F)
                .texOffs(243, 197).addBox(-0.75F, 1.05F, -13.7F, 1.5F, 1.95F, 3.2F)
                .texOffs(256, 197).addBox(3.9167F, 1.05F, -13.7F, 1.5F, 1.95F, 3.2F),
                PartPose.offsetAndRotation(0F, 8.5F, 0F, -0.1396F, 0F, 0F));
        PartDefinition p_front_leg_r = p_body.addOrReplaceChild("front_leg_r", CubeListBuilder.create()
                .texOffs(403, 0).addBox(-6.5F, -4.55F, -6.5F, 13F, 16.55F, 13F),
                PartPose.offsetAndRotation(-12.5F, 5F, -14F, 0.0698F, 0F, 0F));
        PartDefinition p_front_shin_r = p_front_leg_r.addOrReplaceChild("front_shin_r", CubeListBuilder.create()
                .texOffs(440, 95).addBox(-5.5F, -1F, -5.5F, 11F, 10F, 11F),
                PartPose.offsetAndRotation(0F, 11.5F, 0F, -0.1047F, 0F, 0F));
        PartDefinition p_front_foot_r = p_front_shin_r.addOrReplaceChild("front_foot_r", CubeListBuilder.create()
                .texOffs(304, 119).addBox(-6.5F, 0F, -9.8F, 13F, 3F, 14F)
                .texOffs(363, 197).addBox(-5.0833F, 1.05F, -12.8F, 1.5F, 1.95F, 3F)
                .texOffs(374, 197).addBox(-0.75F, 1.05F, -12.8F, 1.5F, 1.95F, 3F)
                .texOffs(385, 197).addBox(3.5833F, 1.05F, -12.8F, 1.5F, 1.95F, 3F),
                PartPose.offsetAndRotation(0F, 8.5F, 0F, 0.0349F, 0F, 0F));
        PartDefinition p_hind_leg_r = p_body.addOrReplaceChild("hind_leg_r", CubeListBuilder.create()
                .texOffs(216, 0).addBox(-7.5F, -5.25F, -8F, 15F, 18.25F, 16F),
                PartPose.offsetAndRotation(-12.5F, 4F, 13F, -0.1396F, 0F, 0F));
        PartDefinition p_hind_shin_r = p_hind_leg_r.addOrReplaceChild("hind_shin_r", CubeListBuilder.create()
                .texOffs(104, 95).addBox(-6F, -1F, -6F, 12F, 10F, 12F),
                PartPose.offsetAndRotation(0F, 12.5F, 0F, 0.2793F, 0F, 0F));
        PartDefinition p_hind_foot_r = p_hind_shin_r.addOrReplaceChild("hind_foot_r", CubeListBuilder.create()
                .texOffs(156, 119).addBox(-7F, 0F, -10.5F, 14F, 3F, 15F)
                .texOffs(269, 197).addBox(-5.4167F, 1.05F, -13.7F, 1.5F, 1.95F, 3.2F)
                .texOffs(282, 197).addBox(-0.75F, 1.05F, -13.7F, 1.5F, 1.95F, 3.2F)
                .texOffs(295, 197).addBox(3.9167F, 1.05F, -13.7F, 1.5F, 1.95F, 3.2F),
                PartPose.offsetAndRotation(0F, 8.5F, 0F, -0.1396F, 0F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
