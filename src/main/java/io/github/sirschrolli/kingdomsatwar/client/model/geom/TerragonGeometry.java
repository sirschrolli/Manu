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
    public static final int TEXTURE_HEIGHT = 384;

    private TerragonGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(131, 0).addBox(-16F, -5.5F, -22F, 32F, 9F, 44F)
                .texOffs(0, 0).addBox(-10.5F, -11F, -21.92F, 21F, 20F, 43.84F)
                .texOffs(103, 99).addBox(-17F, -6F, -22.5F, 34F, 10F, 16F)
                .texOffs(284, 0).addBox(-11F, -12F, -22.42F, 22F, 22F, 15.84F)
                .texOffs(228, 127).addBox(-16.5F, -6F, 8F, 33F, 9F, 13F)
                .texOffs(0, 65).addBox(-11F, -11.5F, 8.08F, 22F, 20F, 12.84F)
                .texOffs(0, 99).addBox(-13.5F, 9F, -20F, 27F, 3F, 24F)
                .texOffs(141, 153).addBox(-12.5F, 8.4F, 4F, 25F, 3F, 17F),
                PartPose.offset(0F, -6F, 0F));
        PartDefinition p_plate_0_0 = p_body.addOrReplaceChild("plate_0_0", CubeListBuilder.create()
                .texOffs(421, 245).addBox(-5F, -2.1F, -1F, 10F, 1.2F, 11F)
                .texOffs(442, 212).addBox(-3.8F, -3.3F, -0.92F, 7.6F, 3.6F, 10.84F)
                .texOffs(363, 287).addBox(-3F, -4.5F, 0.6F, 6F, 1.5F, 7.8F),
                PartPose.offsetAndRotation(-10.5F, -11.5F, -19F, -0.1047F, 0.0785F, -0.2793F));
        PartDefinition p_plate_0_1 = p_body.addOrReplaceChild("plate_0_1", CubeListBuilder.create()
                .texOffs(151, 245).addBox(-5.5F, -2.1F, -1F, 11F, 1.2F, 11F)
                .texOffs(278, 212).addBox(-4.3F, -3.3F, -0.92F, 8.6F, 3.6F, 10.84F)
                .texOffs(239, 287).addBox(-3.5F, -4.5F, 0.6F, 7F, 1.5F, 7.8F),
                PartPose.offsetAndRotation(0F, -13F, -19F, -0.1396F, -0.0785F, 0F));
        PartDefinition p_plate_0_2 = p_body.addOrReplaceChild("plate_0_2", CubeListBuilder.create()
                .texOffs(464, 245).addBox(-5F, -2.1F, -1F, 10F, 1.2F, 11F)
                .texOffs(0, 229).addBox(-3.8F, -3.3F, -0.92F, 7.6F, 3.6F, 10.84F)
                .texOffs(392, 287).addBox(-3F, -4.5F, 0.6F, 6F, 1.5F, 7.8F),
                PartPose.offsetAndRotation(10.5F, -11.5F, -19F, -0.1047F, -0.0785F, 0.2793F));
        PartDefinition p_plate_1_0 = p_body.addOrReplaceChild("plate_1_0", CubeListBuilder.create()
                .texOffs(0, 260).addBox(-5F, -2.1F, -1F, 10F, 1.2F, 11F)
                .texOffs(39, 229).addBox(-3.8F, -3.3F, -0.92F, 7.6F, 3.6F, 10.84F)
                .texOffs(421, 287).addBox(-3F, -4.5F, 0.6F, 6F, 1.5F, 7.8F),
                PartPose.offsetAndRotation(-10.5F, -11.5F, -9F, -0.0698F, 0.0262F, -0.2793F));
        PartDefinition p_plate_1_1 = p_body.addOrReplaceChild("plate_1_1", CubeListBuilder.create()
                .texOffs(196, 245).addBox(-5.5F, -2.1F, -1F, 11F, 1.2F, 11F)
                .texOffs(319, 212).addBox(-4.3F, -3.3F, -0.92F, 8.6F, 3.6F, 10.84F)
                .texOffs(270, 287).addBox(-3.5F, -4.5F, 0.6F, 7F, 1.5F, 7.8F),
                PartPose.offsetAndRotation(0F, -13F, -9F, -0.0873F, -0.0262F, 0F));
        PartDefinition p_plate_1_2 = p_body.addOrReplaceChild("plate_1_2", CubeListBuilder.create()
                .texOffs(43, 260).addBox(-5F, -2.1F, -1F, 10F, 1.2F, 11F)
                .texOffs(78, 229).addBox(-3.8F, -3.3F, -0.92F, 7.6F, 3.6F, 10.84F)
                .texOffs(450, 287).addBox(-3F, -4.5F, 0.6F, 6F, 1.5F, 7.8F),
                PartPose.offsetAndRotation(10.5F, -11.5F, -9F, -0.0698F, -0.0262F, 0.2793F));
        PartDefinition p_plate_2_0 = p_body.addOrReplaceChild("plate_2_0", CubeListBuilder.create()
                .texOffs(86, 260).addBox(-5F, -2.1F, -1F, 10F, 1.2F, 11F)
                .texOffs(117, 229).addBox(-3.8F, -3.3F, -0.92F, 7.6F, 3.6F, 10.84F)
                .texOffs(479, 287).addBox(-3F, -4.5F, 0.6F, 6F, 1.5F, 7.8F),
                PartPose.offsetAndRotation(-10.5F, -11.5F, 1F, -0.0349F, -0.0262F, -0.2793F));
        PartDefinition p_plate_2_1 = p_body.addOrReplaceChild("plate_2_1", CubeListBuilder.create()
                .texOffs(241, 245).addBox(-5.5F, -2.1F, -1F, 11F, 1.2F, 11F)
                .texOffs(360, 212).addBox(-4.3F, -3.3F, -0.92F, 8.6F, 3.6F, 10.84F)
                .texOffs(301, 287).addBox(-3.5F, -4.5F, 0.6F, 7F, 1.5F, 7.8F),
                PartPose.offsetAndRotation(0F, -13F, 1F, -0.0349F, 0.0262F, 0F));
        PartDefinition p_plate_2_2 = p_body.addOrReplaceChild("plate_2_2", CubeListBuilder.create()
                .texOffs(129, 260).addBox(-5F, -2.1F, -1F, 10F, 1.2F, 11F)
                .texOffs(156, 229).addBox(-3.8F, -3.3F, -0.92F, 7.6F, 3.6F, 10.84F)
                .texOffs(0, 299).addBox(-3F, -4.5F, 0.6F, 6F, 1.5F, 7.8F),
                PartPose.offsetAndRotation(10.5F, -11.5F, 1F, -0.0349F, 0.0262F, 0.2793F));
        PartDefinition p_plate_3_0 = p_body.addOrReplaceChild("plate_3_0", CubeListBuilder.create()
                .texOffs(172, 260).addBox(-5F, -2.1F, -1F, 10F, 1.2F, 11F)
                .texOffs(195, 229).addBox(-3.8F, -3.3F, -0.92F, 7.6F, 3.6F, 10.84F)
                .texOffs(29, 299).addBox(-3F, -4.5F, 0.6F, 6F, 1.5F, 7.8F),
                PartPose.offsetAndRotation(-10.5F, -11.5F, 11F, 0F, -0.0785F, -0.2793F));
        PartDefinition p_plate_3_1 = p_body.addOrReplaceChild("plate_3_1", CubeListBuilder.create()
                .texOffs(286, 245).addBox(-5.5F, -2.1F, -1F, 11F, 1.2F, 11F)
                .texOffs(401, 212).addBox(-4.3F, -3.3F, -0.92F, 8.6F, 3.6F, 10.84F)
                .texOffs(332, 287).addBox(-3.5F, -4.5F, 0.6F, 7F, 1.5F, 7.8F),
                PartPose.offsetAndRotation(0F, -13F, 11F, 0.0175F, 0.0785F, 0F));
        PartDefinition p_plate_3_2 = p_body.addOrReplaceChild("plate_3_2", CubeListBuilder.create()
                .texOffs(215, 260).addBox(-5F, -2.1F, -1F, 10F, 1.2F, 11F)
                .texOffs(234, 229).addBox(-3.8F, -3.3F, -0.92F, 7.6F, 3.6F, 10.84F)
                .texOffs(58, 299).addBox(-3F, -4.5F, 0.6F, 6F, 1.5F, 7.8F),
                PartPose.offsetAndRotation(10.5F, -11.5F, 11F, 0F, 0.0785F, 0.2793F));
        PartDefinition p_crystal_0_0 = p_body.addOrReplaceChild("crystal_0_0", CubeListBuilder.create()
                .texOffs(251, 310).addBox(-1.1F, -2.422F, -1.375F, 2.2F, 3.022F, 2.75F)
                .texOffs(412, 319).addBox(-0.715F, -4.336F, -0.8938F, 1.43F, 2.164F, 1.7875F)
                .texOffs(225, 326).addBox(-0.33F, -6.25F, -0.4125F, 0.66F, 2.164F, 0.825F),
                PartPose.offsetAndRotation(-7.7F, -14F, -15.5F, -0.3491F, 0F, -0.3491F));
        PartDefinition p_crystal_0_1 = p_body.addOrReplaceChild("crystal_0_1", CubeListBuilder.create()
                .texOffs(394, 299).addBox(-1.4F, -3.766F, -1.75F, 2.8F, 4.366F, 3.5F)
                .texOffs(329, 310).addBox(-0.91F, -6.608F, -1.1375F, 1.82F, 3.092F, 2.275F)
                .texOffs(232, 319).addBox(-0.42F, -9.45F, -0.525F, 0.84F, 3.092F, 1.05F),
                PartPose.offsetAndRotation(-6F, -14F, -14F, -0.3491F, 0F, 0.1745F));
        PartDefinition p_crystal_0_2 = p_body.addOrReplaceChild("crystal_0_2", CubeListBuilder.create()
                .texOffs(0, 287).addBox(-1.7F, -5.11F, -2.125F, 3.4F, 5.71F, 4.25F)
                .texOffs(30, 310).addBox(-1.105F, -8.88F, -1.3813F, 2.21F, 4.02F, 2.7625F)
                .texOffs(362, 310).addBox(-0.51F, -12.65F, -0.6375F, 1.02F, 4.02F, 1.275F),
                PartPose.offsetAndRotation(-4.3F, -14F, -12.5F, -0.3491F, 0F, -0.1745F));
        PartDefinition p_crystal_1_0 = p_body.addOrReplaceChild("crystal_1_0", CubeListBuilder.create()
                .texOffs(225, 299).addBox(-1.4F, -4.438F, -1.75F, 2.8F, 5.038F, 3.5F)
                .texOffs(340, 310).addBox(-0.91F, -7.744F, -1.1375F, 1.82F, 3.556F, 2.275F)
                .texOffs(239, 319).addBox(-0.42F, -11.05F, -0.525F, 0.84F, 3.556F, 1.05F),
                PartPose.offsetAndRotation(5.3F, -14.5F, -5.5F, -0.1745F, 0F, 0F));
        PartDefinition p_crystal_1_1 = p_body.addOrReplaceChild("crystal_1_1", CubeListBuilder.create()
                .texOffs(356, 299).addBox(-1.7F, -2.422F, -2.125F, 3.4F, 3.022F, 4.25F)
                .texOffs(396, 310).addBox(-1.105F, -4.336F, -1.3813F, 2.21F, 2.164F, 2.7625F)
                .texOffs(421, 319).addBox(-0.51F, -6.25F, -0.6375F, 1.02F, 2.164F, 1.275F),
                PartPose.offsetAndRotation(7F, -14.5F, -4F, -0.1745F, 0F, -0.3491F));
        PartDefinition p_crystal_1_2 = p_body.addOrReplaceChild("crystal_1_2", CubeListBuilder.create()
                .texOffs(43, 310).addBox(-1.1F, -3.766F, -1.375F, 2.2F, 4.366F, 2.75F)
                .texOffs(205, 319).addBox(-0.715F, -6.608F, -0.8938F, 1.43F, 3.092F, 1.7875F)
                .texOffs(499, 319).addBox(-0.33F, -9.45F, -0.4125F, 0.66F, 3.092F, 0.825F),
                PartPose.offsetAndRotation(8.7F, -14.5F, -2.5F, -0.1745F, 0F, 0.1745F));
        PartDefinition p_crystal_1_3 = p_body.addOrReplaceChild("crystal_1_3", CubeListBuilder.create()
                .texOffs(240, 299).addBox(-1.4F, -5.11F, -1.75F, 2.8F, 5.71F, 3.5F)
                .texOffs(69, 310).addBox(-0.91F, -8.88F, -1.1375F, 1.82F, 4.02F, 2.275F)
                .texOffs(389, 310).addBox(-0.42F, -12.65F, -0.525F, 0.84F, 4.02F, 1.05F),
                PartPose.offsetAndRotation(10.4F, -14.5F, -5.5F, -0.1745F, 0F, -0.1745F));
        PartDefinition p_crystal_2_0 = p_body.addOrReplaceChild("crystal_2_0", CubeListBuilder.create()
                .texOffs(375, 299).addBox(-1.7F, -3.094F, -2.125F, 3.4F, 3.694F, 4.25F)
                .texOffs(409, 310).addBox(-1.105F, -5.472F, -1.3813F, 2.21F, 2.628F, 2.7625F)
                .texOffs(430, 319).addBox(-0.51F, -7.85F, -0.6375F, 1.02F, 2.628F, 1.275F),
                PartPose.offsetAndRotation(-3.7F, -15F, 4.5F, 0F, 0F, 0.3491F));
        PartDefinition p_crystal_2_1 = p_body.addOrReplaceChild("crystal_2_1", CubeListBuilder.create()
                .texOffs(424, 299).addBox(-1.1F, -4.438F, -1.375F, 2.2F, 5.038F, 2.75F)
                .texOffs(214, 319).addBox(-0.715F, -7.744F, -0.8938F, 1.43F, 3.556F, 1.7875F)
                .texOffs(504, 319).addBox(-0.33F, -11.05F, -0.4125F, 0.66F, 3.556F, 0.825F),
                PartPose.offset(-2F, -15F, 6F));
        PartDefinition p_crystal_2_2 = p_body.addOrReplaceChild("crystal_2_2", CubeListBuilder.create()
                .texOffs(0, 310).addBox(-1.4F, -2.422F, -1.75F, 2.8F, 3.022F, 3.5F)
                .texOffs(117, 319).addBox(-0.91F, -4.336F, -1.1375F, 1.82F, 2.164F, 2.275F)
                .texOffs(457, 319).addBox(-0.42F, -6.25F, -0.525F, 0.84F, 2.164F, 1.05F),
                PartPose.offsetAndRotation(-0.3F, -15F, 7.5F, 0F, 0F, -0.3491F));
        PartDefinition p_crystal_3_0 = p_body.addOrReplaceChild("crystal_3_0", CubeListBuilder.create()
                .texOffs(437, 299).addBox(-1.1F, -5.11F, -1.375F, 2.2F, 5.71F, 2.75F)
                .texOffs(371, 310).addBox(-0.715F, -8.88F, -0.8938F, 1.43F, 4.02F, 1.7875F)
                .texOffs(253, 319).addBox(-0.33F, -12.65F, -0.4125F, 0.66F, 4.02F, 0.825F),
                PartPose.offsetAndRotation(7.3F, -13.5F, 12.5F, 0.1745F, 0F, -0.1745F));
        PartDefinition p_crystal_3_1 = p_body.addOrReplaceChild("crystal_3_1", CubeListBuilder.create()
                .texOffs(15, 310).addBox(-1.4F, -3.094F, -1.75F, 2.8F, 3.694F, 3.5F)
                .texOffs(128, 319).addBox(-0.91F, -5.472F, -1.1375F, 1.82F, 2.628F, 2.275F)
                .texOffs(464, 319).addBox(-0.42F, -7.85F, -0.525F, 0.84F, 2.628F, 1.05F),
                PartPose.offsetAndRotation(9F, -13.5F, 14F, 0.1745F, 0F, 0.3491F));
        PartDefinition p_crystal_3_2 = p_body.addOrReplaceChild("crystal_3_2", CubeListBuilder.create()
                .texOffs(19, 287).addBox(-1.7F, -4.438F, -2.125F, 3.4F, 5.038F, 4.25F)
                .texOffs(264, 310).addBox(-1.105F, -7.744F, -1.3813F, 2.21F, 3.556F, 2.7625F)
                .texOffs(223, 319).addBox(-0.51F, -11.05F, -0.6375F, 1.02F, 3.556F, 1.275F),
                PartPose.offsetAndRotation(10.7F, -13.5F, 15.5F, 0.1745F, 0F, 0F));
        PartDefinition p_crystal_3_3 = p_body.addOrReplaceChild("crystal_3_3", CubeListBuilder.create()
                .texOffs(277, 310).addBox(-1.1F, -2.422F, -1.375F, 2.2F, 3.022F, 2.75F)
                .texOffs(439, 319).addBox(-0.715F, -4.336F, -0.8938F, 1.43F, 2.164F, 1.7875F)
                .texOffs(230, 326).addBox(-0.33F, -6.25F, -0.4125F, 0.66F, 2.164F, 0.825F),
                PartPose.offsetAndRotation(12.4F, -13.5F, 12.5F, 0.1745F, 0F, -0.3491F));
        PartDefinition p_crystal_4_0 = p_body.addOrReplaceChild("crystal_4_0", CubeListBuilder.create()
                .texOffs(409, 299).addBox(-1.4F, -3.766F, -1.75F, 2.8F, 4.366F, 3.5F)
                .texOffs(351, 310).addBox(-0.91F, -6.608F, -1.1375F, 1.82F, 3.092F, 2.275F)
                .texOffs(246, 319).addBox(-0.42F, -9.45F, -0.525F, 0.84F, 3.092F, 1.05F),
                PartPose.offsetAndRotation(-11.7F, -13F, 13.5F, 0.3491F, 0F, 0.1745F));
        PartDefinition p_crystal_4_1 = p_body.addOrReplaceChild("crystal_4_1", CubeListBuilder.create()
                .texOffs(38, 287).addBox(-1.7F, -5.11F, -2.125F, 3.4F, 5.71F, 4.25F)
                .texOffs(56, 310).addBox(-1.105F, -8.88F, -1.3813F, 2.21F, 4.02F, 2.7625F)
                .texOffs(380, 310).addBox(-0.51F, -12.65F, -0.6375F, 1.02F, 4.02F, 1.275F),
                PartPose.offsetAndRotation(-10F, -13F, 15F, 0.3491F, 0F, -0.1745F));
        PartDefinition p_crystal_4_2 = p_body.addOrReplaceChild("crystal_4_2", CubeListBuilder.create()
                .texOffs(290, 310).addBox(-1.1F, -3.094F, -1.375F, 2.2F, 3.694F, 2.75F)
                .texOffs(448, 319).addBox(-0.715F, -5.472F, -0.8938F, 1.43F, 2.628F, 1.7875F)
                .texOffs(235, 326).addBox(-0.33F, -7.85F, -0.4125F, 0.66F, 2.628F, 0.825F),
                PartPose.offsetAndRotation(-8.3F, -13F, 16.5F, 0.3491F, 0F, 0.3491F));
        PartDefinition p_neck1 = p_body.addOrReplaceChild("neck1", CubeListBuilder.create()
                .texOffs(345, 194).addBox(-9F, -3.4F, -7.6F, 18F, 6.8F, 8.2F)
                .texOffs(408, 99).addBox(-3.9F, -8.5F, -7.52F, 7.8F, 17F, 8.04F)
                .texOffs(294, 299).addBox(-3.9F, 7.5F, -7F, 7.8F, 1.6F, 7F)
                .texOffs(108, 287).addBox(-7F, -10.1F, -7.3F, 14F, 1.2F, 7.6F)
                .texOffs(43, 274).addBox(-6F, -11.1F, -7.22F, 12F, 3.2F, 7.44F),
                PartPose.offsetAndRotation(0F, -5F, -21F, -0.2443F, 0F, 0F));
        PartDefinition p_neck2 = p_neck1.addOrReplaceChild("neck2", CubeListBuilder.create()
                .texOffs(182, 212).addBox(-8.5F, -3.2F, -7.1F, 17F, 6.4F, 7.7F)
                .texOffs(86, 127).addBox(-3.7F, -8F, -7.02F, 7.4F, 16F, 7.54F)
                .texOffs(325, 299).addBox(-3.7F, 7F, -6.5F, 7.4F, 1.6F, 6.5F)
                .texOffs(153, 287).addBox(-6.5F, -9.6F, -6.8F, 13F, 1.2F, 7.1F)
                .texOffs(253, 274).addBox(-5.5F, -10.6F, -6.72F, 11F, 3.2F, 6.94F),
                PartPose.offsetAndRotation(0F, 0F, -7F, -0.0698F, 0F, 0F));
        PartDefinition p_neck3 = p_neck2.addOrReplaceChild("neck3", CubeListBuilder.create()
                .texOffs(324, 229).addBox(-8F, -3F, -6.6F, 16F, 6F, 7.2F)
                .texOffs(197, 127).addBox(-3.5F, -7.5F, -6.52F, 7F, 15F, 7.04F)
                .texOffs(450, 299).addBox(-3.5F, 6.5F, -6F, 7F, 1.6F, 6F)
                .texOffs(255, 299).addBox(-6F, -9.1F, -6.3F, 12F, 1.2F, 6.6F)
                .texOffs(364, 274).addBox(-5F, -10.1F, -6.22F, 10F, 3.2F, 6.44F),
                PartPose.offsetAndRotation(0F, 0F, -6.5F, 0.1047F, 0F, 0F));
        PartDefinition p_head = p_neck3.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(204, 99).addBox(-9F, -4.6F, -17F, 18F, 8.2F, 17F)
                .texOffs(71, 65).addBox(-5.6F, -8F, -16.92F, 11.2F, 15F, 16.84F)
                .texOffs(39, 245).addBox(-10F, -9.1F, -17.5F, 20F, 1.2F, 11F)
                .texOffs(123, 212).addBox(-8.6F, -10.5F, -17.42F, 17.2F, 4F, 10.84F)
                .texOffs(57, 287).addBox(-8.5F, -11.6F, -16.5F, 17F, 1.6F, 8F)
                .texOffs(331, 245).addBox(-7.4F, -3F, -23F, 14.8F, 5.8F, 7F)
                .texOffs(321, 175).addBox(-5F, -5.4F, -22.92F, 10F, 10.6F, 6.84F)
                .texOffs(258, 260).addBox(-6.6F, -2.6F, -29.5F, 13.2F, 5.4F, 7F)
                .texOffs(255, 194).addBox(-4.4F, -4.8F, -29.42F, 8.8F, 9.8F, 6.84F)
                .texOffs(435, 260).addBox(-5.5F, -6.2F, -28F, 11F, 0.6F, 11F)
                .texOffs(373, 229).addBox(-4.5F, -7.2F, -27.92F, 9F, 2.6F, 10.84F)
                .texOffs(123, 310).addBox(-5F, -3.6F, -30.2F, 10F, 5.6F, 1F)
                .texOffs(240, 326).addBox(1.8F, -3F, -30.4F, 2.2F, 1.3F, 0.5F)
                .texOffs(303, 310).addBox(7F, -5.6F, -17.4F, 2F, 2.2F, 3.6F)
                .texOffs(389, 175).addBox(8.4F, -1.2F, -16F, 2.2F, 6.4F, 11F)
                .texOffs(346, 153).addBox(9.2F, -2F, -15.92F, 0.6F, 8F, 10.84F)
                .texOffs(0, 326).addBox(5.4F, 5F, -28.6F, 1.2F, 1.89F, 1.2F)
                .texOffs(9, 326).addBox(5.4F, 5F, -26.6F, 1.2F, 1.4F, 1.2F)
                .texOffs(18, 326).addBox(5.4F, 5F, -24.6F, 1.2F, 1.4F, 1.2F)
                .texOffs(27, 326).addBox(5.4F, 5F, -22.6F, 1.2F, 1.4F, 1.2F)
                .texOffs(36, 326).addBox(5.4F, 5F, -20.6F, 1.2F, 1.4F, 1.2F)
                .texOffs(45, 326).addBox(5.4F, 5F, -18.6F, 1.2F, 1.89F, 1.2F)
                .texOffs(249, 326).addBox(-4F, -3F, -30.4F, 2.2F, 1.3F, 0.5F)
                .texOffs(316, 310).addBox(-9F, -5.6F, -17.4F, 2F, 2.2F, 3.6F)
                .texOffs(418, 175).addBox(-10.6F, -1.2F, -16F, 2.2F, 6.4F, 11F)
                .texOffs(371, 153).addBox(-9.8F, -2F, -15.92F, 0.6F, 8F, 10.84F)
                .texOffs(54, 326).addBox(-6.6F, 5F, -28.6F, 1.2F, 1.89F, 1.2F)
                .texOffs(63, 326).addBox(-6.6F, 5F, -26.6F, 1.2F, 1.4F, 1.2F)
                .texOffs(72, 326).addBox(-6.6F, 5F, -24.6F, 1.2F, 1.4F, 1.2F)
                .texOffs(81, 326).addBox(-6.6F, 5F, -22.6F, 1.2F, 1.4F, 1.2F)
                .texOffs(90, 326).addBox(-6.6F, 5F, -20.6F, 1.2F, 1.4F, 1.2F)
                .texOffs(99, 326).addBox(-6.6F, 5F, -18.6F, 1.2F, 1.89F, 1.2F)
                .texOffs(258, 326).addBox(-3.5F, 5F, -29.6F, 1F, 1.2F, 1F)
                .texOffs(263, 326).addBox(-1.5F, 5F, -29.6F, 1F, 1.2F, 1F)
                .texOffs(268, 326).addBox(0.5F, 5F, -29.6F, 1F, 1.2F, 1F)
                .texOffs(273, 326).addBox(2.5F, 5F, -29.6F, 1F, 1.2F, 1F)
                .texOffs(273, 229).addBox(-6F, 5.02F, -29F, 12F, 0.3F, 12.5F),
                PartPose.offsetAndRotation(0F, 0F, -6F, 0.2094F, 0F, 0F));
        PartDefinition p_horn_l1 = p_head.addOrReplaceChild("horn_l1", CubeListBuilder.create()
                .texOffs(461, 274).addBox(-2.3F, -2.3F, -5.5F, 4.6F, 4.6F, 5.9F)
                .texOffs(141, 299).addBox(-1.84F, -1.84F, -10.2F, 3.68F, 3.68F, 5.2F),
                PartPose.offsetAndRotation(6F, -9F, -10F, -0.2793F, -0.4189F, 0F));
        PartDefinition p_horn_l2 = p_horn_l1.addOrReplaceChild("horn_l2", CubeListBuilder.create()
                .texOffs(162, 299).addBox(-1.7F, -1.7F, -4.675F, 3.4F, 3.4F, 5.075F)
                .texOffs(477, 299).addBox(-1.36F, -1.36F, -8.7F, 2.72F, 2.72F, 4.45F),
                PartPose.offsetAndRotation(0F, 0F, -10F, -0.2094F, 0.1396F, 0F));
        PartDefinition p_horn_l3 = p_horn_l2.addOrReplaceChild("horn_l3", CubeListBuilder.create()
                .texOffs(146, 310).addBox(-1.1F, -1.1F, -3.3F, 2.2F, 2.2F, 3.7F)
                .texOffs(422, 310).addBox(-0.88F, -0.88F, -6.2F, 1.76F, 1.76F, 3.2F),
                PartPose.offsetAndRotation(0F, 0F, -8.5F, -0.2443F, 0.1396F, 0F));
        PartDefinition p_horn_r1 = p_head.addOrReplaceChild("horn_r1", CubeListBuilder.create()
                .texOffs(484, 274).addBox(-2.3F, -2.3F, -5.5F, 4.6F, 4.6F, 5.9F)
                .texOffs(183, 299).addBox(-1.84F, -1.84F, -10.2F, 3.68F, 3.68F, 5.2F),
                PartPose.offsetAndRotation(-6F, -9F, -10F, -0.2793F, 0.4189F, 0F));
        PartDefinition p_horn_r2 = p_horn_r1.addOrReplaceChild("horn_r2", CubeListBuilder.create()
                .texOffs(204, 299).addBox(-1.7F, -1.7F, -4.675F, 3.4F, 3.4F, 5.075F)
                .texOffs(494, 299).addBox(-1.36F, -1.36F, -8.7F, 2.72F, 2.72F, 4.45F),
                PartPose.offsetAndRotation(0F, 0F, -10F, -0.2094F, -0.1396F, 0F));
        PartDefinition p_horn_r3 = p_horn_r2.addOrReplaceChild("horn_r3", CubeListBuilder.create()
                .texOffs(161, 310).addBox(-1.1F, -1.1F, -3.3F, 2.2F, 2.2F, 3.7F)
                .texOffs(435, 310).addBox(-0.88F, -0.88F, -6.2F, 1.76F, 1.76F, 3.2F),
                PartPose.offsetAndRotation(0F, 0F, -8.5F, -0.2443F, -0.1396F, 0F));
        PartDefinition p_nose_horn1 = p_head.addOrReplaceChild("nose_horn1", CubeListBuilder.create()
                .texOffs(176, 310).addBox(-1.8F, -1.8F, -2.475F, 3.6F, 3.6F, 2.875F)
                .texOffs(448, 310).addBox(-1.44F, -1.44F, -4.7F, 2.88F, 2.88F, 2.45F),
                PartPose.offsetAndRotation(0F, -7.6F, -25F, -2.0944F, 0F, 0F));
        PartDefinition p_nose_horn2 = p_nose_horn1.addOrReplaceChild("nose_horn2", CubeListBuilder.create()
                .texOffs(461, 310).addBox(-1.2F, -1.2F, -2.2F, 2.4F, 2.4F, 2.6F)
                .texOffs(258, 319).addBox(-0.96F, -0.96F, -4.2F, 1.92F, 1.92F, 2.2F),
                PartPose.offsetAndRotation(0F, 0F, -4.5F, -0.3142F, 0F, 0F));
        PartDefinition p_nose_horn3 = p_nose_horn2.addOrReplaceChild("nose_horn3", CubeListBuilder.create()
                .texOffs(269, 319).addBox(-0.65F, -0.65F, -1.65F, 1.3F, 1.3F, 2.05F)
                .texOffs(108, 326).addBox(-0.52F, -0.52F, -3.2F, 1.04F, 1.04F, 1.7F),
                PartPose.offsetAndRotation(0F, 0F, -4F, -0.3142F, 0F, 0F));
        PartDefinition p_jaw = p_head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(396, 153).addBox(-7F, 1.8F, -15.5F, 14F, 1.4F, 15.5F)
                .texOffs(432, 127).addBox(-5.2F, 0F, -15.42F, 10.4F, 5F, 15.34F)
                .texOffs(80, 310).addBox(-8F, 1.4F, -3F, 16F, 2F, 4.5F)
                .texOffs(290, 274).addBox(-6.2F, -0.4F, -2.92F, 12.4F, 5.6F, 4.34F)
                .texOffs(117, 326).addBox(5.05F, -1.52F, -14.45F, 1.1F, 1.62F, 1.1F)
                .texOffs(126, 326).addBox(5.05F, -1.1F, -12.25F, 1.1F, 1.2F, 1.1F)
                .texOffs(135, 326).addBox(5.05F, -1.1F, -10.05F, 1.1F, 1.2F, 1.1F)
                .texOffs(144, 326).addBox(5.05F, -1.1F, -7.85F, 1.1F, 1.2F, 1.1F)
                .texOffs(153, 326).addBox(5.05F, -1.52F, -5.65F, 1.1F, 1.62F, 1.1F)
                .texOffs(162, 326).addBox(-6.15F, -1.52F, -14.45F, 1.1F, 1.62F, 1.1F)
                .texOffs(171, 326).addBox(-6.15F, -1.1F, -12.25F, 1.1F, 1.2F, 1.1F)
                .texOffs(180, 326).addBox(-6.15F, -1.1F, -10.05F, 1.1F, 1.2F, 1.1F)
                .texOffs(189, 326).addBox(-6.15F, -1.1F, -7.85F, 1.1F, 1.2F, 1.1F)
                .texOffs(198, 326).addBox(-6.15F, -1.52F, -5.65F, 1.1F, 1.62F, 1.1F)
                .texOffs(288, 194).addBox(-6.2F, -0.15F, -15.2F, 12.4F, 0.3F, 15F)
                .texOffs(84, 274).addBox(-4.4F, -0.5F, -13.5F, 8.8F, 0.6F, 11F)
                .texOffs(376, 245).addBox(-5F, 4.6F, -12F, 10F, 0.9F, 12F),
                PartPose.offsetAndRotation(0F, 4.9F, -14F, 0.0524F, 0F, 0F));
        PartDefinition p_tusk_l = p_jaw.addOrReplaceChild("tusk_l", CubeListBuilder.create()
                .texOffs(139, 319).addBox(-1F, -2.002F, -1.25F, 2F, 2.602F, 2.5F)
                .texOffs(207, 326).addBox(-0.65F, -3.626F, -0.8125F, 1.3F, 1.874F, 1.625F)
                .texOffs(278, 326).addBox(-0.3F, -5.25F, -0.375F, 0.6F, 1.874F, 0.75F),
                PartPose.offsetAndRotation(6.8F, 0.6F, -13.5F, 0.1745F, 0F, 0.2443F));
        PartDefinition p_tusk_r = p_jaw.addOrReplaceChild("tusk_r", CubeListBuilder.create()
                .texOffs(150, 319).addBox(-1F, -2.002F, -1.25F, 2F, 2.602F, 2.5F)
                .texOffs(216, 326).addBox(-0.65F, -3.626F, -0.8125F, 1.3F, 1.874F, 1.625F)
                .texOffs(283, 326).addBox(-0.3F, -5.25F, -0.375F, 0.6F, 1.874F, 0.75F),
                PartPose.offsetAndRotation(-6.8F, 0.6F, -13.5F, 0.1745F, 0F, -0.2443F));
        PartDefinition p_tail1 = p_body.addOrReplaceChild("tail1", CubeListBuilder.create()
                .texOffs(226, 153).addBox(-8F, -2.8F, -0.6F, 16F, 5.6F, 13.2F)
                .texOffs(424, 65).addBox(-3.8F, -7F, -0.52F, 7.6F, 14F, 13.04F)
                .texOffs(414, 229).addBox(-3.8F, 6F, 0F, 7.6F, 1.6F, 12F)
                .texOffs(102, 245).addBox(-6.5F, -8.6F, 0.6F, 13F, 1.2F, 10.8F)
                .texOffs(233, 212).addBox(-5.5F, -9.6F, 0.68F, 11F, 3.2F, 10.64F),
                PartPose.offsetAndRotation(0F, -2F, 21F, -0.1396F, 0F, 0F));
        PartDefinition p_tail2 = p_tail1.addOrReplaceChild("tail2", CubeListBuilder.create()
                .texOffs(114, 175).addBox(-7F, -2.4F, -0.6F, 14F, 4.8F, 12.2F)
                .texOffs(45, 127).addBox(-3.4F, -6F, -0.52F, 6.8F, 12F, 12.04F)
                .texOffs(301, 260).addBox(-3.4F, 5F, 0F, 6.8F, 1.6F, 11F)
                .texOffs(0, 274).addBox(-5.5F, -7.6F, 0.6F, 11F, 1.2F, 9.8F)
                .texOffs(0, 245).addBox(-4.5F, -8.6F, 0.68F, 9F, 3.2F, 9.64F),
                PartPose.offsetAndRotation(0F, 0F, 12F, -0.0698F, 0F, 0F));
        PartDefinition p_tail3 = p_tail2.addOrReplaceChild("tail3", CubeListBuilder.create()
                .texOffs(0, 212).addBox(-6F, -2F, -0.6F, 12F, 4F, 11.2F)
                .texOffs(321, 127).addBox(-3F, -5F, -0.52F, 6F, 10F, 11.04F)
                .texOffs(164, 274).addBox(-3F, 4F, 0F, 6F, 1.6F, 10F)
                .texOffs(327, 274).addBox(-4.5F, -6.6F, 0.6F, 9F, 1.2F, 8.8F)
                .texOffs(373, 260).addBox(-3.5F, -7.6F, 0.68F, 7F, 3.2F, 8.64F),
                PartPose.offset(0F, 0F, 11F));
        PartDefinition p_tail4 = p_tail3.addOrReplaceChild("tail4", CubeListBuilder.create()
                .texOffs(49, 212).addBox(-5F, -1.7F, -0.6F, 10F, 3.4F, 11.2F)
                .texOffs(106, 153).addBox(-2.45F, -4.25F, -0.52F, 4.9F, 8.5F, 11.04F)
                .texOffs(197, 274).addBox(-2.45F, 3.25F, 0F, 4.9F, 1.6F, 10F)
                .texOffs(399, 274).addBox(-3.5F, -5.85F, 0.6F, 7F, 1.2F, 8.8F)
                .texOffs(406, 260).addBox(-2.5F, -6.85F, 0.68F, 5F, 3.2F, 8.64F),
                PartPose.offsetAndRotation(0F, 0F, 10F, 0.0349F, 0F, 0F));
        PartDefinition p_tail5 = p_tail4.addOrReplaceChild("tail5", CubeListBuilder.create()
                .texOffs(455, 229).addBox(-4.25F, -1.4F, -0.6F, 8.5F, 2.8F, 10.2F)
                .texOffs(356, 175).addBox(-2.15F, -3.5F, -0.52F, 4.3F, 7F, 10.04F)
                .texOffs(432, 274).addBox(-2.15F, 2.5F, 0F, 4.3F, 1.6F, 9F)
                .texOffs(87, 299).addBox(-2.75F, -5.1F, 0.6F, 5.5F, 1.2F, 7.8F)
                .texOffs(228, 274).addBox(-1.75F, -6.1F, 0.68F, 3.5F, 3.2F, 7.64F),
                PartPose.offsetAndRotation(0F, 0F, 10F, 0.0524F, 0F, 0F));
        PartDefinition p_tail6 = p_tail5.addOrReplaceChild("tail6", CubeListBuilder.create()
                .texOffs(338, 260).addBox(-3.5F, -1.2F, -0.6F, 7F, 2.4F, 9.2F)
                .texOffs(94, 212).addBox(-1.7F, -3F, -0.52F, 3.4F, 6F, 9.04F)
                .texOffs(116, 299).addBox(-1.7F, 2F, 0F, 3.4F, 1.6F, 8F)
                .texOffs(287, 153).addBox(-8F, -3.6F, 4F, 16F, 6.2F, 13F)
                .texOffs(275, 99).addBox(-4.6F, -7F, 4.08F, 9.2F, 13F, 12.84F)
                .texOffs(196, 287).addBox(-6F, -8.2F, 6F, 12F, 0.8F, 9F)
                .texOffs(125, 274).addBox(-5F, -9.2F, 6.08F, 10F, 2.8F, 8.84F),
                PartPose.offsetAndRotation(0F, 0F, 9F, 0.0349F, 0F, 0F));
        PartDefinition p_club_spike_0 = p_tail6.addOrReplaceChild("club_spike_0", CubeListBuilder.create()
                .texOffs(191, 310).addBox(-1.4F, -2.254F, -1.75F, 2.8F, 2.854F, 3.5F)
                .texOffs(161, 319).addBox(-0.91F, -4.052F, -1.1375F, 1.82F, 2.048F, 2.275F)
                .texOffs(471, 319).addBox(-0.42F, -5.85F, -0.525F, 0.84F, 2.048F, 1.05F),
                PartPose.offsetAndRotation(-7.5F, -5F, 10F, 0F, 0F, -1.0472F));
        PartDefinition p_club_spike_1 = p_tail6.addOrReplaceChild("club_spike_1", CubeListBuilder.create()
                .texOffs(206, 310).addBox(-1.4F, -2.254F, -1.75F, 2.8F, 2.854F, 3.5F)
                .texOffs(172, 319).addBox(-0.91F, -4.052F, -1.1375F, 1.82F, 2.048F, 2.275F)
                .texOffs(478, 319).addBox(-0.42F, -5.85F, -0.525F, 0.84F, 2.048F, 1.05F),
                PartPose.offsetAndRotation(7.5F, -5F, 10F, 0F, 0F, 1.0472F));
        PartDefinition p_club_spike_2 = p_tail6.addOrReplaceChild("club_spike_2", CubeListBuilder.create()
                .texOffs(221, 310).addBox(-1.4F, -2.254F, -1.75F, 2.8F, 2.854F, 3.5F)
                .texOffs(183, 319).addBox(-0.91F, -4.052F, -1.1375F, 1.82F, 2.048F, 2.275F)
                .texOffs(485, 319).addBox(-0.42F, -5.85F, -0.525F, 0.84F, 2.048F, 1.05F),
                PartPose.offsetAndRotation(0F, -9F, 10F, -0.3491F, 0F, 0F));
        PartDefinition p_club_spike_3 = p_tail6.addOrReplaceChild("club_spike_3", CubeListBuilder.create()
                .texOffs(236, 310).addBox(-1.4F, -2.254F, -1.75F, 2.8F, 2.854F, 3.5F)
                .texOffs(194, 319).addBox(-0.91F, -4.052F, -1.1375F, 1.82F, 2.048F, 2.275F)
                .texOffs(492, 319).addBox(-0.42F, -5.85F, -0.525F, 0.84F, 2.048F, 1.05F),
                PartPose.offsetAndRotation(0F, 5.5F, 10F, 3.1416F, 0F, 0F));
        PartDefinition p_front_leg_l = p_body.addOrReplaceChild("front_leg_l", CubeListBuilder.create()
                .texOffs(322, 99).addBox(-6.5F, -5.2F, -3.64F, 13F, 17.2F, 7.28F)
                .texOffs(130, 65).addBox(-3.64F, -5.12F, -6.5F, 7.28F, 17.04F, 13F)
                .texOffs(165, 194).addBox(-7.1F, -7.15F, -3.2F, 14.2F, 9.75F, 6.4F)
                .texOffs(443, 99).addBox(-3.2F, -7.07F, -7.1F, 6.4F, 9.59F, 14.2F),
                PartPose.offsetAndRotation(12.5F, 5F, -14F, 0.0698F, 0F, 0F));
        PartDefinition p_front_shin_l = p_front_leg_l.addOrReplaceChild("front_shin_l", CubeListBuilder.create()
                .texOffs(247, 175).addBox(-5.5F, -1.4F, -3.08F, 11F, 10.6F, 6.16F)
                .texOffs(358, 127).addBox(-3.08F, -1.32F, -5.5F, 6.16F, 10.44F, 11F),
                PartPose.offsetAndRotation(0F, 11.4F, 0F, -0.1047F, 0F, 0F));
        PartDefinition p_front_foot_l = p_front_shin_l.addOrReplaceChild("front_foot_l", CubeListBuilder.create()
                .texOffs(400, 194).addBox(-6.5F, 0.9F, -9.8F, 13F, 1.2F, 14F)
                .texOffs(59, 194).addBox(-5.6F, 0F, -9.72F, 11.2F, 3F, 13.84F)
                .texOffs(474, 310).addBox(-5.4833F, 1.1F, -12.2F, 1.9F, 1.9F, 3.5F)
                .texOffs(280, 319).addBox(-5.1983F, 1.575F, -14.2F, 1.33F, 1.71F, 2.3F)
                .texOffs(487, 310).addBox(-0.95F, 1.1F, -12.2F, 1.9F, 1.9F, 3.5F)
                .texOffs(291, 319).addBox(-0.665F, 1.575F, -14.2F, 1.33F, 1.71F, 2.3F)
                .texOffs(500, 310).addBox(3.5833F, 1.1F, -12.2F, 1.9F, 1.9F, 3.5F)
                .texOffs(302, 319).addBox(3.8683F, 1.575F, -14.2F, 1.33F, 1.71F, 2.3F),
                PartPose.offsetAndRotation(0F, 8.4F, 0F, 0.0349F, 0F, 0F));
        PartDefinition p_hind_leg_l = p_body.addOrReplaceChild("hind_leg_l", CubeListBuilder.create()
                .texOffs(322, 65).addBox(-7.5F, -6F, -4.7F, 15F, 19F, 9.4F)
                .texOffs(361, 0).addBox(-4.2F, -5.92F, -8F, 8.4F, 18.84F, 16F)
                .texOffs(0, 153).addBox(-8.1F, -8.25F, -4.1F, 16.2F, 11.25F, 8.2F)
                .texOffs(216, 65).addBox(-3.6F, -8.17F, -8.6F, 7.2F, 11.09F, 17.2F),
                PartPose.offsetAndRotation(12.5F, 4F, 13F, -0.1396F, 0F, 0F));
        PartDefinition p_hind_shin_l = p_hind_leg_l.addOrReplaceChild("hind_shin_l", CubeListBuilder.create()
                .texOffs(169, 175).addBox(-6F, -1.4F, -3.36F, 12F, 10.6F, 6.72F)
                .texOffs(119, 127).addBox(-3.36F, -1.32F, -6F, 6.72F, 10.44F, 12F),
                PartPose.offsetAndRotation(0F, 12.4F, 0F, 0.2793F, 0F, 0F));
        PartDefinition p_hind_foot_l = p_hind_shin_l.addOrReplaceChild("hind_foot_l", CubeListBuilder.create()
                .texOffs(447, 175).addBox(-7F, 0.9F, -10.5F, 14F, 1.2F, 15F)
                .texOffs(0, 175).addBox(-6.1F, 0F, -10.42F, 12.2F, 3F, 14.84F)
                .texOffs(0, 319).addBox(-5.8167F, 1.1F, -13.1F, 1.9F, 1.9F, 3.7F)
                .texOffs(313, 319).addBox(-5.5317F, 1.575F, -15.3F, 1.33F, 1.71F, 2.5F)
                .texOffs(13, 319).addBox(-0.95F, 1.1F, -13.1F, 1.9F, 1.9F, 3.7F)
                .texOffs(324, 319).addBox(-0.665F, 1.575F, -15.3F, 1.33F, 1.71F, 2.5F)
                .texOffs(26, 319).addBox(3.9167F, 1.1F, -13.1F, 1.9F, 1.9F, 3.7F)
                .texOffs(335, 319).addBox(4.2017F, 1.575F, -15.3F, 1.33F, 1.71F, 2.5F),
                PartPose.offsetAndRotation(0F, 8.4F, 0F, -0.1396F, 0F, 0F));
        PartDefinition p_front_leg_r = p_body.addOrReplaceChild("front_leg_r", CubeListBuilder.create()
                .texOffs(365, 99).addBox(-6.5F, -5.2F, -3.64F, 13F, 17.2F, 7.28F)
                .texOffs(173, 65).addBox(-3.64F, -5.12F, -6.5F, 7.28F, 17.04F, 13F)
                .texOffs(210, 194).addBox(-7.1F, -7.15F, -3.2F, 14.2F, 9.75F, 6.4F)
                .texOffs(0, 127).addBox(-3.2F, -7.07F, -7.1F, 6.4F, 9.59F, 14.2F),
                PartPose.offsetAndRotation(-12.5F, 5F, -14F, 0.0698F, 0F, 0F));
        PartDefinition p_front_shin_r = p_front_leg_r.addOrReplaceChild("front_shin_r", CubeListBuilder.create()
                .texOffs(284, 175).addBox(-5.5F, -1.4F, -3.08F, 11F, 10.6F, 6.16F)
                .texOffs(395, 127).addBox(-3.08F, -1.32F, -5.5F, 6.16F, 10.44F, 11F),
                PartPose.offsetAndRotation(0F, 11.4F, 0F, -0.1047F, 0F, 0F));
        PartDefinition p_front_foot_r = p_front_shin_r.addOrReplaceChild("front_foot_r", CubeListBuilder.create()
                .texOffs(455, 194).addBox(-6.5F, 0.9F, -9.8F, 13F, 1.2F, 14F)
                .texOffs(112, 194).addBox(-5.6F, 0F, -9.72F, 11.2F, 3F, 13.84F)
                .texOffs(39, 319).addBox(-5.4833F, 1.1F, -12.2F, 1.9F, 1.9F, 3.5F)
                .texOffs(346, 319).addBox(-5.1983F, 1.575F, -14.2F, 1.33F, 1.71F, 2.3F)
                .texOffs(52, 319).addBox(-0.95F, 1.1F, -12.2F, 1.9F, 1.9F, 3.5F)
                .texOffs(357, 319).addBox(-0.665F, 1.575F, -14.2F, 1.33F, 1.71F, 2.3F)
                .texOffs(65, 319).addBox(3.5833F, 1.1F, -12.2F, 1.9F, 1.9F, 3.5F)
                .texOffs(368, 319).addBox(3.8683F, 1.575F, -14.2F, 1.33F, 1.71F, 2.3F),
                PartPose.offsetAndRotation(0F, 8.4F, 0F, 0.0349F, 0F, 0F));
        PartDefinition p_hind_leg_r = p_body.addOrReplaceChild("hind_leg_r", CubeListBuilder.create()
                .texOffs(373, 65).addBox(-7.5F, -6F, -4.7F, 15F, 19F, 9.4F)
                .texOffs(412, 0).addBox(-4.2F, -5.92F, -8F, 8.4F, 18.84F, 16F)
                .texOffs(53, 153).addBox(-8.1F, -8.25F, -4.1F, 16.2F, 11.25F, 8.2F)
                .texOffs(269, 65).addBox(-3.6F, -8.17F, -8.6F, 7.2F, 11.09F, 17.2F),
                PartPose.offsetAndRotation(-12.5F, 4F, 13F, -0.1396F, 0F, 0F));
        PartDefinition p_hind_shin_r = p_hind_leg_r.addOrReplaceChild("hind_shin_r", CubeListBuilder.create()
                .texOffs(208, 175).addBox(-6F, -1.4F, -3.36F, 12F, 10.6F, 6.72F)
                .texOffs(158, 127).addBox(-3.36F, -1.32F, -6F, 6.72F, 10.44F, 12F),
                PartPose.offsetAndRotation(0F, 12.4F, 0F, 0.2793F, 0F, 0F));
        PartDefinition p_hind_foot_r = p_hind_shin_r.addOrReplaceChild("hind_foot_r", CubeListBuilder.create()
                .texOffs(0, 194).addBox(-7F, 0.9F, -10.5F, 14F, 1.2F, 15F)
                .texOffs(57, 175).addBox(-6.1F, 0F, -10.42F, 12.2F, 3F, 14.84F)
                .texOffs(78, 319).addBox(-5.8167F, 1.1F, -13.1F, 1.9F, 1.9F, 3.7F)
                .texOffs(379, 319).addBox(-5.5317F, 1.575F, -15.3F, 1.33F, 1.71F, 2.5F)
                .texOffs(91, 319).addBox(-0.95F, 1.1F, -13.1F, 1.9F, 1.9F, 3.7F)
                .texOffs(390, 319).addBox(-0.665F, 1.575F, -15.3F, 1.33F, 1.71F, 2.5F)
                .texOffs(104, 319).addBox(3.9167F, 1.1F, -13.1F, 1.9F, 1.9F, 3.7F)
                .texOffs(401, 319).addBox(4.2017F, 1.575F, -15.3F, 1.33F, 1.71F, 2.5F),
                PartPose.offsetAndRotation(0F, 8.4F, 0F, -0.1396F, 0F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
