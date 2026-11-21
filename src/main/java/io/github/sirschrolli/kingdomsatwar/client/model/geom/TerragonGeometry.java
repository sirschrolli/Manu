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
                .texOffs(0, 0).addBox(-10.5F, -11F, -22F, 21F, 20F, 44F, new CubeDeformation(0F, 0F, -0.08F))
                .texOffs(103, 99).addBox(-17F, -6F, -22.5F, 34F, 10F, 16F)
                .texOffs(284, 0).addBox(-11F, -12F, -22.5F, 22F, 22F, 16F, new CubeDeformation(0F, 0F, -0.08F))
                .texOffs(228, 127).addBox(-16.5F, -6F, 8F, 33F, 9F, 13F)
                .texOffs(0, 65).addBox(-11F, -11.5F, 8F, 22F, 20F, 13F, new CubeDeformation(0F, 0F, -0.08F))
                .texOffs(0, 99).addBox(-13.5F, 9F, -20F, 27F, 3F, 24F)
                .texOffs(141, 153).addBox(-12.5F, 8.4F, 4F, 25F, 3F, 17F),
                PartPose.offset(0F, -6F, 0F));
        PartDefinition p_plate_0_0 = p_body.addOrReplaceChild("plate_0_0", CubeListBuilder.create()
                .texOffs(421, 245).addBox(-5F, -2.5F, -1F, 10F, 2F, 11F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(442, 212).addBox(-4F, -3.5F, -1F, 8F, 4F, 11F, new CubeDeformation(-0.2F, -0.2F, -0.08F))
                .texOffs(363, 287).addBox(-3F, -4.75F, 0.5F, 6F, 2F, 8F, new CubeDeformation(0F, -0.25F, -0.1F)),
                PartPose.offsetAndRotation(-10.5F, -11.5F, -19F, -0.1047F, 0.0785F, -0.2793F));
        PartDefinition p_plate_0_1 = p_body.addOrReplaceChild("plate_0_1", CubeListBuilder.create()
                .texOffs(151, 245).addBox(-5.5F, -2.5F, -1F, 11F, 2F, 11F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(278, 212).addBox(-4.5F, -3.5F, -1F, 9F, 4F, 11F, new CubeDeformation(-0.2F, -0.2F, -0.08F))
                .texOffs(239, 287).addBox(-3.5F, -4.75F, 0.5F, 7F, 2F, 8F, new CubeDeformation(0F, -0.25F, -0.1F)),
                PartPose.offsetAndRotation(0F, -13F, -19F, -0.1396F, -0.0785F, 0F));
        PartDefinition p_plate_0_2 = p_body.addOrReplaceChild("plate_0_2", CubeListBuilder.create()
                .texOffs(464, 245).addBox(-5F, -2.5F, -1F, 10F, 2F, 11F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(0, 229).addBox(-4F, -3.5F, -1F, 8F, 4F, 11F, new CubeDeformation(-0.2F, -0.2F, -0.08F))
                .texOffs(392, 287).addBox(-3F, -4.75F, 0.5F, 6F, 2F, 8F, new CubeDeformation(0F, -0.25F, -0.1F)),
                PartPose.offsetAndRotation(10.5F, -11.5F, -19F, -0.1047F, -0.0785F, 0.2793F));
        PartDefinition p_plate_1_0 = p_body.addOrReplaceChild("plate_1_0", CubeListBuilder.create()
                .texOffs(0, 260).addBox(-5F, -2.5F, -1F, 10F, 2F, 11F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(39, 229).addBox(-4F, -3.5F, -1F, 8F, 4F, 11F, new CubeDeformation(-0.2F, -0.2F, -0.08F))
                .texOffs(421, 287).addBox(-3F, -4.75F, 0.5F, 6F, 2F, 8F, new CubeDeformation(0F, -0.25F, -0.1F)),
                PartPose.offsetAndRotation(-10.5F, -11.5F, -9F, -0.0698F, 0.0262F, -0.2793F));
        PartDefinition p_plate_1_1 = p_body.addOrReplaceChild("plate_1_1", CubeListBuilder.create()
                .texOffs(196, 245).addBox(-5.5F, -2.5F, -1F, 11F, 2F, 11F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(319, 212).addBox(-4.5F, -3.5F, -1F, 9F, 4F, 11F, new CubeDeformation(-0.2F, -0.2F, -0.08F))
                .texOffs(270, 287).addBox(-3.5F, -4.75F, 0.5F, 7F, 2F, 8F, new CubeDeformation(0F, -0.25F, -0.1F)),
                PartPose.offsetAndRotation(0F, -13F, -9F, -0.0873F, -0.0262F, 0F));
        PartDefinition p_plate_1_2 = p_body.addOrReplaceChild("plate_1_2", CubeListBuilder.create()
                .texOffs(43, 260).addBox(-5F, -2.5F, -1F, 10F, 2F, 11F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(78, 229).addBox(-4F, -3.5F, -1F, 8F, 4F, 11F, new CubeDeformation(-0.2F, -0.2F, -0.08F))
                .texOffs(450, 287).addBox(-3F, -4.75F, 0.5F, 6F, 2F, 8F, new CubeDeformation(0F, -0.25F, -0.1F)),
                PartPose.offsetAndRotation(10.5F, -11.5F, -9F, -0.0698F, -0.0262F, 0.2793F));
        PartDefinition p_plate_2_0 = p_body.addOrReplaceChild("plate_2_0", CubeListBuilder.create()
                .texOffs(86, 260).addBox(-5F, -2.5F, -1F, 10F, 2F, 11F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(117, 229).addBox(-4F, -3.5F, -1F, 8F, 4F, 11F, new CubeDeformation(-0.2F, -0.2F, -0.08F))
                .texOffs(479, 287).addBox(-3F, -4.75F, 0.5F, 6F, 2F, 8F, new CubeDeformation(0F, -0.25F, -0.1F)),
                PartPose.offsetAndRotation(-10.5F, -11.5F, 1F, -0.0349F, -0.0262F, -0.2793F));
        PartDefinition p_plate_2_1 = p_body.addOrReplaceChild("plate_2_1", CubeListBuilder.create()
                .texOffs(241, 245).addBox(-5.5F, -2.5F, -1F, 11F, 2F, 11F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(360, 212).addBox(-4.5F, -3.5F, -1F, 9F, 4F, 11F, new CubeDeformation(-0.2F, -0.2F, -0.08F))
                .texOffs(301, 287).addBox(-3.5F, -4.75F, 0.5F, 7F, 2F, 8F, new CubeDeformation(0F, -0.25F, -0.1F)),
                PartPose.offsetAndRotation(0F, -13F, 1F, -0.0349F, 0.0262F, 0F));
        PartDefinition p_plate_2_2 = p_body.addOrReplaceChild("plate_2_2", CubeListBuilder.create()
                .texOffs(129, 260).addBox(-5F, -2.5F, -1F, 10F, 2F, 11F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(156, 229).addBox(-4F, -3.5F, -1F, 8F, 4F, 11F, new CubeDeformation(-0.2F, -0.2F, -0.08F))
                .texOffs(0, 299).addBox(-3F, -4.75F, 0.5F, 6F, 2F, 8F, new CubeDeformation(0F, -0.25F, -0.1F)),
                PartPose.offsetAndRotation(10.5F, -11.5F, 1F, -0.0349F, 0.0262F, 0.2793F));
        PartDefinition p_plate_3_0 = p_body.addOrReplaceChild("plate_3_0", CubeListBuilder.create()
                .texOffs(172, 260).addBox(-5F, -2.5F, -1F, 10F, 2F, 11F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(195, 229).addBox(-4F, -3.5F, -1F, 8F, 4F, 11F, new CubeDeformation(-0.2F, -0.2F, -0.08F))
                .texOffs(29, 299).addBox(-3F, -4.75F, 0.5F, 6F, 2F, 8F, new CubeDeformation(0F, -0.25F, -0.1F)),
                PartPose.offsetAndRotation(-10.5F, -11.5F, 11F, 0F, -0.0785F, -0.2793F));
        PartDefinition p_plate_3_1 = p_body.addOrReplaceChild("plate_3_1", CubeListBuilder.create()
                .texOffs(286, 245).addBox(-5.5F, -2.5F, -1F, 11F, 2F, 11F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(401, 212).addBox(-4.5F, -3.5F, -1F, 9F, 4F, 11F, new CubeDeformation(-0.2F, -0.2F, -0.08F))
                .texOffs(332, 287).addBox(-3.5F, -4.75F, 0.5F, 7F, 2F, 8F, new CubeDeformation(0F, -0.25F, -0.1F)),
                PartPose.offsetAndRotation(0F, -13F, 11F, 0.0175F, 0.0785F, 0F));
        PartDefinition p_plate_3_2 = p_body.addOrReplaceChild("plate_3_2", CubeListBuilder.create()
                .texOffs(215, 260).addBox(-5F, -2.5F, -1F, 10F, 2F, 11F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(234, 229).addBox(-4F, -3.5F, -1F, 8F, 4F, 11F, new CubeDeformation(-0.2F, -0.2F, -0.08F))
                .texOffs(58, 299).addBox(-3F, -4.75F, 0.5F, 6F, 2F, 8F, new CubeDeformation(0F, -0.25F, -0.1F)),
                PartPose.offsetAndRotation(10.5F, -11.5F, 11F, 0F, 0.0785F, 0.2793F));
        PartDefinition p_crystal_0_0 = p_body.addOrReplaceChild("crystal_0_0", CubeListBuilder.create()
                .texOffs(251, 310).addBox(-1.5F, -2.911F, -1.5F, 3F, 4F, 3F, new CubeDeformation(-0.4F, -0.489F, -0.125F))
                .texOffs(412, 319).addBox(-1F, -4.754F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.285F, -0.418F, -0.1062F))
                .texOffs(225, 326).addBox(-0.5F, -6.668F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.17F, -0.418F, -0.0875F)),
                PartPose.offsetAndRotation(-7.7F, -14F, -15.5F, -0.3491F, 0F, -0.3491F));
        PartDefinition p_crystal_0_1 = p_body.addOrReplaceChild("crystal_0_1", CubeListBuilder.create()
                .texOffs(394, 299).addBox(-1.5F, -4.083F, -2F, 3F, 5F, 4F, new CubeDeformation(-0.1F, -0.317F, -0.25F))
                .texOffs(329, 310).addBox(-1F, -7.062F, -1.5F, 2F, 4F, 3F, new CubeDeformation(-0.09F, -0.454F, -0.3625F))
                .texOffs(232, 319).addBox(-0.5F, -9.904F, -1F, 1F, 4F, 2F, new CubeDeformation(-0.08F, -0.454F, -0.475F)),
                PartPose.offsetAndRotation(-6F, -14F, -14F, -0.3491F, 0F, 0.1745F));
        PartDefinition p_crystal_0_2 = p_body.addOrReplaceChild("crystal_0_2", CubeListBuilder.create()
                .texOffs(0, 287).addBox(-2F, -5.255F, -2.5F, 4F, 6F, 5F, new CubeDeformation(-0.3F, -0.145F, -0.375F))
                .texOffs(30, 310).addBox(-1.5F, -9.37F, -1.5F, 3F, 5F, 3F, new CubeDeformation(-0.395F, -0.49F, -0.1187F))
                .texOffs(362, 310).addBox(-1F, -13.14F, -1F, 2F, 5F, 2F, new CubeDeformation(-0.49F, -0.49F, -0.3625F)),
                PartPose.offsetAndRotation(-4.3F, -14F, -12.5F, -0.3491F, 0F, -0.1745F));
        PartDefinition p_crystal_1_0 = p_body.addOrReplaceChild("crystal_1_0", CubeListBuilder.create()
                .texOffs(225, 299).addBox(-1.5F, -4.919F, -2F, 3F, 6F, 4F, new CubeDeformation(-0.1F, -0.481F, -0.25F))
                .texOffs(340, 310).addBox(-1F, -7.966F, -1.5F, 2F, 4F, 3F, new CubeDeformation(-0.09F, -0.222F, -0.3625F))
                .texOffs(239, 319).addBox(-0.5F, -11.272F, -1F, 1F, 4F, 2F, new CubeDeformation(-0.08F, -0.222F, -0.475F)),
                PartPose.offsetAndRotation(5.3F, -14.5F, -5.5F, -0.1745F, 0F, 0F));
        PartDefinition p_crystal_1_1 = p_body.addOrReplaceChild("crystal_1_1", CubeListBuilder.create()
                .texOffs(356, 299).addBox(-2F, -2.911F, -2.5F, 4F, 4F, 5F, new CubeDeformation(-0.3F, -0.489F, -0.375F))
                .texOffs(396, 310).addBox(-1.5F, -4.754F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.395F, -0.418F, -0.1187F))
                .texOffs(421, 319).addBox(-1F, -6.668F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.49F, -0.418F, -0.3625F)),
                PartPose.offsetAndRotation(7F, -14.5F, -4F, -0.1745F, 0F, -0.3491F));
        PartDefinition p_crystal_1_2 = p_body.addOrReplaceChild("crystal_1_2", CubeListBuilder.create()
                .texOffs(43, 310).addBox(-1.5F, -4.083F, -1.5F, 3F, 5F, 3F, new CubeDeformation(-0.4F, -0.317F, -0.125F))
                .texOffs(205, 319).addBox(-1F, -7.062F, -1F, 2F, 4F, 2F, new CubeDeformation(-0.285F, -0.454F, -0.1062F))
                .texOffs(499, 319).addBox(-0.5F, -9.904F, -0.5F, 1F, 4F, 1F, new CubeDeformation(-0.17F, -0.454F, -0.0875F)),
                PartPose.offsetAndRotation(8.7F, -14.5F, -2.5F, -0.1745F, 0F, 0.1745F));
        PartDefinition p_crystal_1_3 = p_body.addOrReplaceChild("crystal_1_3", CubeListBuilder.create()
                .texOffs(240, 299).addBox(-1.5F, -5.255F, -2F, 3F, 6F, 4F, new CubeDeformation(-0.1F, -0.145F, -0.25F))
                .texOffs(69, 310).addBox(-1F, -9.37F, -1.5F, 2F, 5F, 3F, new CubeDeformation(-0.09F, -0.49F, -0.3625F))
                .texOffs(389, 310).addBox(-0.5F, -13.14F, -1F, 1F, 5F, 2F, new CubeDeformation(-0.08F, -0.49F, -0.475F)),
                PartPose.offsetAndRotation(10.4F, -14.5F, -5.5F, -0.1745F, 0F, -0.1745F));
        PartDefinition p_crystal_2_0 = p_body.addOrReplaceChild("crystal_2_0", CubeListBuilder.create()
                .texOffs(375, 299).addBox(-2F, -3.247F, -2.5F, 4F, 4F, 5F, new CubeDeformation(-0.3F, -0.153F, -0.375F))
                .texOffs(409, 310).addBox(-1.5F, -5.658F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.395F, -0.186F, -0.1187F))
                .texOffs(430, 319).addBox(-1F, -8.036F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.49F, -0.186F, -0.3625F)),
                PartPose.offsetAndRotation(-3.7F, -15F, 4.5F, 0F, 0F, 0.3491F));
        PartDefinition p_crystal_2_1 = p_body.addOrReplaceChild("crystal_2_1", CubeListBuilder.create()
                .texOffs(424, 299).addBox(-1.5F, -4.919F, -1.5F, 3F, 6F, 3F, new CubeDeformation(-0.4F, -0.481F, -0.125F))
                .texOffs(214, 319).addBox(-1F, -7.966F, -1F, 2F, 4F, 2F, new CubeDeformation(-0.285F, -0.222F, -0.1062F))
                .texOffs(504, 319).addBox(-0.5F, -11.272F, -0.5F, 1F, 4F, 1F, new CubeDeformation(-0.17F, -0.222F, -0.0875F)),
                PartPose.offset(-2F, -15F, 6F));
        PartDefinition p_crystal_2_2 = p_body.addOrReplaceChild("crystal_2_2", CubeListBuilder.create()
                .texOffs(0, 310).addBox(-1.5F, -2.911F, -2F, 3F, 4F, 4F, new CubeDeformation(-0.1F, -0.489F, -0.25F))
                .texOffs(117, 319).addBox(-1F, -4.754F, -1.5F, 2F, 3F, 3F, new CubeDeformation(-0.09F, -0.418F, -0.3625F))
                .texOffs(457, 319).addBox(-0.5F, -6.668F, -1F, 1F, 3F, 2F, new CubeDeformation(-0.08F, -0.418F, -0.475F)),
                PartPose.offsetAndRotation(-0.3F, -15F, 7.5F, 0F, 0F, -0.3491F));
        PartDefinition p_crystal_3_0 = p_body.addOrReplaceChild("crystal_3_0", CubeListBuilder.create()
                .texOffs(437, 299).addBox(-1.5F, -5.255F, -1.5F, 3F, 6F, 3F, new CubeDeformation(-0.4F, -0.145F, -0.125F))
                .texOffs(371, 310).addBox(-1F, -9.37F, -1F, 2F, 5F, 2F, new CubeDeformation(-0.285F, -0.49F, -0.1062F))
                .texOffs(253, 319).addBox(-0.5F, -13.14F, -0.5F, 1F, 5F, 1F, new CubeDeformation(-0.17F, -0.49F, -0.0875F)),
                PartPose.offsetAndRotation(7.3F, -13.5F, 12.5F, 0.1745F, 0F, -0.1745F));
        PartDefinition p_crystal_3_1 = p_body.addOrReplaceChild("crystal_3_1", CubeListBuilder.create()
                .texOffs(15, 310).addBox(-1.5F, -3.247F, -2F, 3F, 4F, 4F, new CubeDeformation(-0.1F, -0.153F, -0.25F))
                .texOffs(128, 319).addBox(-1F, -5.658F, -1.5F, 2F, 3F, 3F, new CubeDeformation(-0.09F, -0.186F, -0.3625F))
                .texOffs(464, 319).addBox(-0.5F, -8.036F, -1F, 1F, 3F, 2F, new CubeDeformation(-0.08F, -0.186F, -0.475F)),
                PartPose.offsetAndRotation(9F, -13.5F, 14F, 0.1745F, 0F, 0.3491F));
        PartDefinition p_crystal_3_2 = p_body.addOrReplaceChild("crystal_3_2", CubeListBuilder.create()
                .texOffs(19, 287).addBox(-2F, -4.919F, -2.5F, 4F, 6F, 5F, new CubeDeformation(-0.3F, -0.481F, -0.375F))
                .texOffs(264, 310).addBox(-1.5F, -7.966F, -1.5F, 3F, 4F, 3F, new CubeDeformation(-0.395F, -0.222F, -0.1187F))
                .texOffs(223, 319).addBox(-1F, -11.272F, -1F, 2F, 4F, 2F, new CubeDeformation(-0.49F, -0.222F, -0.3625F)),
                PartPose.offsetAndRotation(10.7F, -13.5F, 15.5F, 0.1745F, 0F, 0F));
        PartDefinition p_crystal_3_3 = p_body.addOrReplaceChild("crystal_3_3", CubeListBuilder.create()
                .texOffs(277, 310).addBox(-1.5F, -2.911F, -1.5F, 3F, 4F, 3F, new CubeDeformation(-0.4F, -0.489F, -0.125F))
                .texOffs(439, 319).addBox(-1F, -4.754F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.285F, -0.418F, -0.1062F))
                .texOffs(230, 326).addBox(-0.5F, -6.668F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.17F, -0.418F, -0.0875F)),
                PartPose.offsetAndRotation(12.4F, -13.5F, 12.5F, 0.1745F, 0F, -0.3491F));
        PartDefinition p_crystal_4_0 = p_body.addOrReplaceChild("crystal_4_0", CubeListBuilder.create()
                .texOffs(409, 299).addBox(-1.5F, -4.083F, -2F, 3F, 5F, 4F, new CubeDeformation(-0.1F, -0.317F, -0.25F))
                .texOffs(351, 310).addBox(-1F, -7.062F, -1.5F, 2F, 4F, 3F, new CubeDeformation(-0.09F, -0.454F, -0.3625F))
                .texOffs(246, 319).addBox(-0.5F, -9.904F, -1F, 1F, 4F, 2F, new CubeDeformation(-0.08F, -0.454F, -0.475F)),
                PartPose.offsetAndRotation(-11.7F, -13F, 13.5F, 0.3491F, 0F, 0.1745F));
        PartDefinition p_crystal_4_1 = p_body.addOrReplaceChild("crystal_4_1", CubeListBuilder.create()
                .texOffs(38, 287).addBox(-2F, -5.255F, -2.5F, 4F, 6F, 5F, new CubeDeformation(-0.3F, -0.145F, -0.375F))
                .texOffs(56, 310).addBox(-1.5F, -9.37F, -1.5F, 3F, 5F, 3F, new CubeDeformation(-0.395F, -0.49F, -0.1187F))
                .texOffs(380, 310).addBox(-1F, -13.14F, -1F, 2F, 5F, 2F, new CubeDeformation(-0.49F, -0.49F, -0.3625F)),
                PartPose.offsetAndRotation(-10F, -13F, 15F, 0.3491F, 0F, -0.1745F));
        PartDefinition p_crystal_4_2 = p_body.addOrReplaceChild("crystal_4_2", CubeListBuilder.create()
                .texOffs(290, 310).addBox(-1.5F, -3.247F, -1.5F, 3F, 4F, 3F, new CubeDeformation(-0.4F, -0.153F, -0.125F))
                .texOffs(448, 319).addBox(-1F, -5.658F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.285F, -0.186F, -0.1062F))
                .texOffs(235, 326).addBox(-0.5F, -8.036F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.17F, -0.186F, -0.0875F)),
                PartPose.offsetAndRotation(-8.3F, -13F, 16.5F, 0.3491F, 0F, 0.3491F));
        PartDefinition p_neck1 = p_body.addOrReplaceChild("neck1", CubeListBuilder.create()
                .texOffs(345, 194).addBox(-9F, -3.5F, -8F, 18F, 7F, 9F, new CubeDeformation(0F, -0.1F, -0.4F))
                .texOffs(408, 99).addBox(-4F, -8.5F, -8F, 8F, 17F, 9F, new CubeDeformation(-0.1F, 0F, -0.48F))
                .texOffs(294, 299).addBox(-4F, 7.3F, -7F, 8F, 2F, 7F, new CubeDeformation(-0.1F, -0.2F, 0F))
                .texOffs(108, 287).addBox(-7F, -10.5F, -7.5F, 14F, 2F, 8F, new CubeDeformation(0F, -0.4F, -0.2F))
                .texOffs(43, 274).addBox(-6F, -11.5F, -7.5F, 12F, 4F, 8F, new CubeDeformation(0F, -0.4F, -0.28F)),
                PartPose.offsetAndRotation(0F, -5F, -21F, -0.2443F, 0F, 0F));
        PartDefinition p_neck2 = p_neck1.addOrReplaceChild("neck2", CubeListBuilder.create()
                .texOffs(182, 212).addBox(-8.5F, -3.5F, -7.25F, 17F, 7F, 8F, new CubeDeformation(0F, -0.3F, -0.15F))
                .texOffs(86, 127).addBox(-4F, -8F, -7.25F, 8F, 16F, 8F, new CubeDeformation(-0.3F, 0F, -0.23F))
                .texOffs(325, 299).addBox(-4F, 6.8F, -6.75F, 8F, 2F, 7F, new CubeDeformation(-0.3F, -0.2F, -0.25F))
                .texOffs(153, 287).addBox(-6.5F, -10F, -7.25F, 13F, 2F, 8F, new CubeDeformation(0F, -0.4F, -0.45F))
                .texOffs(253, 274).addBox(-5.5F, -11F, -6.75F, 11F, 4F, 7F, new CubeDeformation(0F, -0.4F, -0.03F)),
                PartPose.offsetAndRotation(0F, 0F, -7F, -0.0698F, 0F, 0F));
        PartDefinition p_neck3 = p_neck2.addOrReplaceChild("neck3", CubeListBuilder.create()
                .texOffs(324, 229).addBox(-8F, -3F, -7F, 16F, 6F, 8F, new CubeDeformation(0F, 0F, -0.4F))
                .texOffs(197, 127).addBox(-3.5F, -7.5F, -7F, 7F, 15F, 8F, new CubeDeformation(0F, 0F, -0.48F))
                .texOffs(450, 299).addBox(-3.5F, 6.3F, -6F, 7F, 2F, 6F, new CubeDeformation(0F, -0.2F, 0F))
                .texOffs(255, 299).addBox(-6F, -9.5F, -6.5F, 12F, 2F, 7F, new CubeDeformation(0F, -0.4F, -0.2F))
                .texOffs(364, 274).addBox(-5F, -10.5F, -6.5F, 10F, 4F, 7F, new CubeDeformation(0F, -0.4F, -0.28F)),
                PartPose.offsetAndRotation(0F, 0F, -6.5F, 0.1047F, 0F, 0F));
        PartDefinition p_head = p_neck3.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(204, 99).addBox(-9F, -5F, -17F, 18F, 9F, 17F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(71, 65).addBox(-6F, -8F, -17F, 12F, 15F, 17F, new CubeDeformation(-0.4F, 0F, -0.08F))
                .texOffs(39, 245).addBox(-10F, -9.5F, -17.5F, 20F, 2F, 11F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(123, 212).addBox(-9F, -10.5F, -17.5F, 18F, 4F, 11F, new CubeDeformation(-0.4F, 0F, -0.08F))
                .texOffs(57, 287).addBox(-8.5F, -11.8F, -16.5F, 17F, 2F, 8F, new CubeDeformation(0F, -0.2F, 0F))
                .texOffs(331, 245).addBox(-7.5F, -3.1F, -23F, 15F, 6F, 7F, new CubeDeformation(-0.1F, -0.1F, 0F))
                .texOffs(321, 175).addBox(-5F, -5.6F, -23F, 10F, 11F, 7F, new CubeDeformation(0F, -0.2F, -0.08F))
                .texOffs(258, 260).addBox(-7F, -2.9F, -29.5F, 14F, 6F, 7F, new CubeDeformation(-0.4F, -0.3F, 0F))
                .texOffs(255, 194).addBox(-4.5F, -4.9F, -29.5F, 9F, 10F, 7F, new CubeDeformation(-0.1F, -0.1F, -0.08F))
                .texOffs(435, 260).addBox(-5.5F, -6.4F, -28F, 11F, 1F, 11F, new CubeDeformation(0F, -0.2F, 0F))
                .texOffs(373, 229).addBox(-4.5F, -7.4F, -28F, 9F, 3F, 11F, new CubeDeformation(0F, -0.2F, -0.08F))
                .texOffs(123, 310).addBox(-5F, -3.8F, -30.2F, 10F, 6F, 1F, new CubeDeformation(0F, -0.2F, 0F))
                .texOffs(240, 326).addBox(1.4F, -3.35F, -30.65F, 3F, 2F, 1F, new CubeDeformation(-0.4F, -0.35F, -0.25F))
                .texOffs(303, 310).addBox(7F, -6F, -17.6F, 2F, 3F, 4F, new CubeDeformation(0F, -0.4F, -0.2F))
                .texOffs(389, 175).addBox(8F, -1.5F, -16F, 3F, 7F, 11F, new CubeDeformation(-0.4F, -0.3F, 0F))
                .texOffs(346, 153).addBox(9F, -2F, -16F, 1F, 8F, 11F, new CubeDeformation(-0.2F, 0F, -0.08F))
                .texOffs(0, 326).addBox(5F, 4.945F, -29F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.055F, -0.4F))
                .texOffs(9, 326).addBox(5F, 4.7F, -27F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.3F, -0.4F))
                .texOffs(18, 326).addBox(5F, 4.7F, -25F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.3F, -0.4F))
                .texOffs(27, 326).addBox(5F, 4.7F, -23F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.3F, -0.4F))
                .texOffs(36, 326).addBox(5F, 4.7F, -21F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.3F, -0.4F))
                .texOffs(45, 326).addBox(5F, 4.945F, -19F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.055F, -0.4F))
                .texOffs(249, 326).addBox(-4.4F, -3.35F, -30.65F, 3F, 2F, 1F, new CubeDeformation(-0.4F, -0.35F, -0.25F))
                .texOffs(316, 310).addBox(-9F, -6F, -17.6F, 2F, 3F, 4F, new CubeDeformation(0F, -0.4F, -0.2F))
                .texOffs(418, 175).addBox(-11F, -1.5F, -16F, 3F, 7F, 11F, new CubeDeformation(-0.4F, -0.3F, 0F))
                .texOffs(371, 153).addBox(-10F, -2F, -16F, 1F, 8F, 11F, new CubeDeformation(-0.2F, 0F, -0.08F))
                .texOffs(54, 326).addBox(-7F, 4.945F, -29F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.055F, -0.4F))
                .texOffs(63, 326).addBox(-7F, 4.7F, -27F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.3F, -0.4F))
                .texOffs(72, 326).addBox(-7F, 4.7F, -25F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.3F, -0.4F))
                .texOffs(81, 326).addBox(-7F, 4.7F, -23F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.3F, -0.4F))
                .texOffs(90, 326).addBox(-7F, 4.7F, -21F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.3F, -0.4F))
                .texOffs(99, 326).addBox(-7F, 4.945F, -19F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.055F, -0.4F))
                .texOffs(258, 326).addBox(-3.5F, 4.6F, -29.6F, 1F, 2F, 1F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(263, 326).addBox(-1.5F, 4.6F, -29.6F, 1F, 2F, 1F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(268, 326).addBox(0.5F, 4.6F, -29.6F, 1F, 2F, 1F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(273, 326).addBox(2.5F, 4.6F, -29.6F, 1F, 2F, 1F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(273, 229).addBox(-6F, 4.67F, -29.25F, 12F, 1F, 13F, new CubeDeformation(0F, -0.35F, -0.25F)),
                PartPose.offsetAndRotation(0F, 0F, -6F, 0.2094F, 0F, 0F));
        PartDefinition p_horn_l1 = p_head.addOrReplaceChild("horn_l1", CubeListBuilder.create()
                .texOffs(461, 274).addBox(-2.5F, -2.5F, -5.55F, 5F, 5F, 6F, new CubeDeformation(-0.2F, -0.2F, -0.05F))
                .texOffs(141, 299).addBox(-2F, -2F, -10.6F, 4F, 4F, 6F, new CubeDeformation(-0.16F, -0.16F, -0.4F)),
                PartPose.offsetAndRotation(6F, -9F, -10F, -0.2793F, -0.4189F, 0F));
        PartDefinition p_horn_l2 = p_horn_l1.addOrReplaceChild("horn_l2", CubeListBuilder.create()
                .texOffs(162, 299).addBox(-2F, -2F, -5.1375F, 4F, 4F, 6F, new CubeDeformation(-0.3F, -0.3F, -0.4625F))
                .texOffs(477, 299).addBox(-1.5F, -1.5F, -8.975F, 3F, 3F, 5F, new CubeDeformation(-0.14F, -0.14F, -0.275F)),
                PartPose.offsetAndRotation(0F, 0F, -10F, -0.2094F, 0.1396F, 0F));
        PartDefinition p_horn_l3 = p_horn_l2.addOrReplaceChild("horn_l3", CubeListBuilder.create()
                .texOffs(146, 310).addBox(-1.5F, -1.5F, -3.45F, 3F, 3F, 4F, new CubeDeformation(-0.4F, -0.4F, -0.15F))
                .texOffs(422, 310).addBox(-1F, -1F, -6.6F, 2F, 2F, 4F, new CubeDeformation(-0.12F, -0.12F, -0.4F)),
                PartPose.offsetAndRotation(0F, 0F, -8.5F, -0.2443F, 0.1396F, 0F));
        PartDefinition p_horn_r1 = p_head.addOrReplaceChild("horn_r1", CubeListBuilder.create()
                .texOffs(484, 274).addBox(-2.5F, -2.5F, -5.55F, 5F, 5F, 6F, new CubeDeformation(-0.2F, -0.2F, -0.05F))
                .texOffs(183, 299).addBox(-2F, -2F, -10.6F, 4F, 4F, 6F, new CubeDeformation(-0.16F, -0.16F, -0.4F)),
                PartPose.offsetAndRotation(-6F, -9F, -10F, -0.2793F, 0.4189F, 0F));
        PartDefinition p_horn_r2 = p_horn_r1.addOrReplaceChild("horn_r2", CubeListBuilder.create()
                .texOffs(204, 299).addBox(-2F, -2F, -5.1375F, 4F, 4F, 6F, new CubeDeformation(-0.3F, -0.3F, -0.4625F))
                .texOffs(494, 299).addBox(-1.5F, -1.5F, -8.975F, 3F, 3F, 5F, new CubeDeformation(-0.14F, -0.14F, -0.275F)),
                PartPose.offsetAndRotation(0F, 0F, -10F, -0.2094F, -0.1396F, 0F));
        PartDefinition p_horn_r3 = p_horn_r2.addOrReplaceChild("horn_r3", CubeListBuilder.create()
                .texOffs(161, 310).addBox(-1.5F, -1.5F, -3.45F, 3F, 3F, 4F, new CubeDeformation(-0.4F, -0.4F, -0.15F))
                .texOffs(435, 310).addBox(-1F, -1F, -6.6F, 2F, 2F, 4F, new CubeDeformation(-0.12F, -0.12F, -0.4F)),
                PartPose.offsetAndRotation(0F, 0F, -8.5F, -0.2443F, -0.1396F, 0F));
        PartDefinition p_nose_horn1 = p_head.addOrReplaceChild("nose_horn1", CubeListBuilder.create()
                .texOffs(176, 310).addBox(-2F, -2F, -2.5375F, 4F, 4F, 3F, new CubeDeformation(-0.2F, -0.2F, -0.0625F))
                .texOffs(448, 310).addBox(-1.5F, -1.5F, -4.975F, 3F, 3F, 3F, new CubeDeformation(-0.06F, -0.06F, -0.275F)),
                PartPose.offsetAndRotation(0F, -7.6F, -25F, -2.0944F, 0F, 0F));
        PartDefinition p_nose_horn2 = p_nose_horn1.addOrReplaceChild("nose_horn2", CubeListBuilder.create()
                .texOffs(461, 310).addBox(-1.5F, -1.5F, -2.4F, 3F, 3F, 3F, new CubeDeformation(-0.3F, -0.3F, -0.2F))
                .texOffs(258, 319).addBox(-1F, -1F, -4.6F, 2F, 2F, 3F, new CubeDeformation(-0.04F, -0.04F, -0.4F)),
                PartPose.offsetAndRotation(0F, 0F, -4.5F, -0.3142F, 0F, 0F));
        PartDefinition p_nose_horn3 = p_nose_horn2.addOrReplaceChild("nose_horn3", CubeListBuilder.create()
                .texOffs(269, 319).addBox(-1F, -1F, -2.125F, 2F, 2F, 3F, new CubeDeformation(-0.35F, -0.35F, -0.475F))
                .texOffs(108, 326).addBox(-1F, -1F, -3.35F, 2F, 2F, 2F, new CubeDeformation(-0.48F, -0.48F, -0.15F)),
                PartPose.offsetAndRotation(0F, 0F, -4F, -0.3142F, 0F, 0F));
        PartDefinition p_jaw = p_head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(396, 153).addBox(-7F, 1.5F, -15.75F, 14F, 2F, 16F, new CubeDeformation(0F, -0.3F, -0.25F))
                .texOffs(432, 127).addBox(-5.5F, 0F, -15.75F, 11F, 5F, 16F, new CubeDeformation(-0.3F, 0F, -0.33F))
                .texOffs(80, 310).addBox(-8F, 1.4F, -3.25F, 16F, 2F, 5F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(290, 274).addBox(-6.5F, -0.6F, -3.25F, 13F, 6F, 5F, new CubeDeformation(-0.3F, -0.2F, -0.33F))
                .texOffs(117, 326).addBox(4.6F, -1.71F, -14.9F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.19F, -0.45F))
                .texOffs(126, 326).addBox(4.6F, -1.5F, -12.7F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.4F, -0.45F))
                .texOffs(135, 326).addBox(4.6F, -1.5F, -10.5F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.4F, -0.45F))
                .texOffs(144, 326).addBox(4.6F, -1.5F, -8.3F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.4F, -0.45F))
                .texOffs(153, 326).addBox(4.6F, -1.71F, -6.1F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.19F, -0.45F))
                .texOffs(162, 326).addBox(-6.6F, -1.71F, -14.9F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.19F, -0.45F))
                .texOffs(171, 326).addBox(-6.6F, -1.5F, -12.7F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.4F, -0.45F))
                .texOffs(180, 326).addBox(-6.6F, -1.5F, -10.5F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.4F, -0.45F))
                .texOffs(189, 326).addBox(-6.6F, -1.5F, -8.3F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.4F, -0.45F))
                .texOffs(198, 326).addBox(-6.6F, -1.71F, -6.1F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.19F, -0.45F))
                .texOffs(288, 194).addBox(-6.5F, -0.5F, -15.2F, 13F, 1F, 15F, new CubeDeformation(-0.3F, -0.35F, 0F))
                .texOffs(84, 274).addBox(-4.5F, -0.7F, -13.5F, 9F, 1F, 11F, new CubeDeformation(-0.1F, -0.2F, 0F))
                .texOffs(376, 245).addBox(-5F, 4.55F, -12F, 10F, 1F, 12F, new CubeDeformation(0F, -0.05F, 0F)),
                PartPose.offsetAndRotation(0F, 4.9F, -14F, 0.0524F, 0F, 0F));
        PartDefinition p_tusk_l = p_jaw.addOrReplaceChild("tusk_l", CubeListBuilder.create()
                .texOffs(139, 319).addBox(-1F, -2.201F, -1.5F, 2F, 3F, 3F, new CubeDeformation(0F, -0.199F, -0.25F))
                .texOffs(207, 326).addBox(-1F, -3.689F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.063F, -0.1875F))
                .texOffs(278, 326).addBox(-0.5F, -5.313F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.2F, -0.063F, -0.125F)),
                PartPose.offsetAndRotation(6.8F, 0.6F, -13.5F, 0.1745F, 0F, 0.2443F));
        PartDefinition p_tusk_r = p_jaw.addOrReplaceChild("tusk_r", CubeListBuilder.create()
                .texOffs(150, 319).addBox(-1F, -2.201F, -1.5F, 2F, 3F, 3F, new CubeDeformation(0F, -0.199F, -0.25F))
                .texOffs(216, 326).addBox(-1F, -3.689F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.063F, -0.1875F))
                .texOffs(283, 326).addBox(-0.5F, -5.313F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.2F, -0.063F, -0.125F)),
                PartPose.offsetAndRotation(-6.8F, 0.6F, -13.5F, 0.1745F, 0F, -0.2443F));
        PartDefinition p_tail1 = p_body.addOrReplaceChild("tail1", CubeListBuilder.create()
                .texOffs(226, 153).addBox(-8F, -3F, -1F, 16F, 6F, 14F, new CubeDeformation(0F, -0.2F, -0.4F))
                .texOffs(424, 65).addBox(-4F, -7F, -1F, 8F, 14F, 14F, new CubeDeformation(-0.2F, 0F, -0.48F))
                .texOffs(414, 229).addBox(-4F, 5.8F, 0F, 8F, 2F, 12F, new CubeDeformation(-0.2F, -0.2F, 0F))
                .texOffs(102, 245).addBox(-6.5F, -9F, 0.5F, 13F, 2F, 11F, new CubeDeformation(0F, -0.4F, -0.1F))
                .texOffs(233, 212).addBox(-5.5F, -10F, 0.5F, 11F, 4F, 11F, new CubeDeformation(0F, -0.4F, -0.18F)),
                PartPose.offsetAndRotation(0F, -2F, 21F, -0.1396F, 0F, 0F));
        PartDefinition p_tail2 = p_tail1.addOrReplaceChild("tail2", CubeListBuilder.create()
                .texOffs(114, 175).addBox(-7F, -2.5F, -1F, 14F, 5F, 13F, new CubeDeformation(0F, -0.1F, -0.4F))
                .texOffs(45, 127).addBox(-3.5F, -6F, -1F, 7F, 12F, 13F, new CubeDeformation(-0.1F, 0F, -0.48F))
                .texOffs(301, 260).addBox(-3.5F, 4.8F, 0F, 7F, 2F, 11F, new CubeDeformation(-0.1F, -0.2F, 0F))
                .texOffs(0, 274).addBox(-5.5F, -8F, 0.5F, 11F, 2F, 10F, new CubeDeformation(0F, -0.4F, -0.1F))
                .texOffs(0, 245).addBox(-4.5F, -9F, 0.5F, 9F, 4F, 10F, new CubeDeformation(0F, -0.4F, -0.18F)),
                PartPose.offsetAndRotation(0F, 0F, 12F, -0.0698F, 0F, 0F));
        PartDefinition p_tail3 = p_tail2.addOrReplaceChild("tail3", CubeListBuilder.create()
                .texOffs(0, 212).addBox(-6F, -2F, -1F, 12F, 4F, 12F, new CubeDeformation(0F, 0F, -0.4F))
                .texOffs(321, 127).addBox(-3F, -5F, -1F, 6F, 10F, 12F, new CubeDeformation(0F, 0F, -0.48F))
                .texOffs(164, 274).addBox(-3F, 3.8F, 0F, 6F, 2F, 10F, new CubeDeformation(0F, -0.2F, 0F))
                .texOffs(327, 274).addBox(-4.5F, -7F, 0.5F, 9F, 2F, 9F, new CubeDeformation(0F, -0.4F, -0.1F))
                .texOffs(373, 260).addBox(-3.5F, -8F, 0.5F, 7F, 4F, 9F, new CubeDeformation(0F, -0.4F, -0.18F)),
                PartPose.offset(0F, 0F, 11F));
        PartDefinition p_tail4 = p_tail3.addOrReplaceChild("tail4", CubeListBuilder.create()
                .texOffs(49, 212).addBox(-5F, -2F, -1F, 10F, 4F, 12F, new CubeDeformation(0F, -0.3F, -0.4F))
                .texOffs(106, 153).addBox(-2.5F, -4.5F, -1F, 5F, 9F, 12F, new CubeDeformation(-0.05F, -0.25F, -0.48F))
                .texOffs(197, 274).addBox(-2.5F, 3.05F, 0F, 5F, 2F, 10F, new CubeDeformation(-0.05F, -0.2F, 0F))
                .texOffs(399, 274).addBox(-3.5F, -6.25F, 0.5F, 7F, 2F, 9F, new CubeDeformation(0F, -0.4F, -0.1F))
                .texOffs(406, 260).addBox(-2.5F, -7.25F, 0.5F, 5F, 4F, 9F, new CubeDeformation(0F, -0.4F, -0.18F)),
                PartPose.offsetAndRotation(0F, 0F, 10F, 0.0349F, 0F, 0F));
        PartDefinition p_tail5 = p_tail4.addOrReplaceChild("tail5", CubeListBuilder.create()
                .texOffs(455, 229).addBox(-4.5F, -1.5F, -1F, 9F, 3F, 11F, new CubeDeformation(-0.25F, -0.1F, -0.4F))
                .texOffs(356, 175).addBox(-2.5F, -3.5F, -1F, 5F, 7F, 11F, new CubeDeformation(-0.35F, 0F, -0.48F))
                .texOffs(432, 274).addBox(-2.5F, 2.3F, 0F, 5F, 2F, 9F, new CubeDeformation(-0.35F, -0.2F, 0F))
                .texOffs(87, 299).addBox(-3F, -5.5F, 0.5F, 6F, 2F, 8F, new CubeDeformation(-0.25F, -0.4F, -0.1F))
                .texOffs(228, 274).addBox(-2F, -6.5F, 0.5F, 4F, 4F, 8F, new CubeDeformation(-0.25F, -0.4F, -0.18F)),
                PartPose.offsetAndRotation(0F, 0F, 10F, 0.0524F, 0F, 0F));
        PartDefinition p_tail6 = p_tail5.addOrReplaceChild("tail6", CubeListBuilder.create()
                .texOffs(338, 260).addBox(-3.5F, -1.5F, -1F, 7F, 3F, 10F, new CubeDeformation(0F, -0.3F, -0.4F))
                .texOffs(94, 212).addBox(-2F, -3F, -1F, 4F, 6F, 10F, new CubeDeformation(-0.3F, 0F, -0.48F))
                .texOffs(116, 299).addBox(-2F, 1.8F, 0F, 4F, 2F, 8F, new CubeDeformation(-0.3F, -0.2F, 0F))
                .texOffs(287, 153).addBox(-8F, -4F, 4F, 16F, 7F, 13F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(275, 99).addBox(-5F, -7F, 4F, 10F, 13F, 13F, new CubeDeformation(-0.4F, 0F, -0.08F))
                .texOffs(196, 287).addBox(-6F, -8.3F, 6F, 12F, 1F, 9F, new CubeDeformation(0F, -0.1F, 0F))
                .texOffs(125, 274).addBox(-5F, -9.3F, 6F, 10F, 3F, 9F, new CubeDeformation(0F, -0.1F, -0.08F)),
                PartPose.offsetAndRotation(0F, 0F, 9F, 0.0349F, 0F, 0F));
        PartDefinition p_club_spike_0 = p_tail6.addOrReplaceChild("club_spike_0", CubeListBuilder.create()
                .texOffs(191, 310).addBox(-1.5F, -2.327F, -2F, 3F, 3F, 4F, new CubeDeformation(-0.1F, -0.073F, -0.25F))
                .texOffs(161, 319).addBox(-1F, -4.528F, -1.5F, 2F, 3F, 3F, new CubeDeformation(-0.09F, -0.476F, -0.3625F))
                .texOffs(471, 319).addBox(-0.5F, -6.326F, -1F, 1F, 3F, 2F, new CubeDeformation(-0.08F, -0.476F, -0.475F)),
                PartPose.offsetAndRotation(-7.5F, -5F, 10F, 0F, 0F, -1.0472F));
        PartDefinition p_club_spike_1 = p_tail6.addOrReplaceChild("club_spike_1", CubeListBuilder.create()
                .texOffs(206, 310).addBox(-1.5F, -2.327F, -2F, 3F, 3F, 4F, new CubeDeformation(-0.1F, -0.073F, -0.25F))
                .texOffs(172, 319).addBox(-1F, -4.528F, -1.5F, 2F, 3F, 3F, new CubeDeformation(-0.09F, -0.476F, -0.3625F))
                .texOffs(478, 319).addBox(-0.5F, -6.326F, -1F, 1F, 3F, 2F, new CubeDeformation(-0.08F, -0.476F, -0.475F)),
                PartPose.offsetAndRotation(7.5F, -5F, 10F, 0F, 0F, 1.0472F));
        PartDefinition p_club_spike_2 = p_tail6.addOrReplaceChild("club_spike_2", CubeListBuilder.create()
                .texOffs(221, 310).addBox(-1.5F, -2.327F, -2F, 3F, 3F, 4F, new CubeDeformation(-0.1F, -0.073F, -0.25F))
                .texOffs(183, 319).addBox(-1F, -4.528F, -1.5F, 2F, 3F, 3F, new CubeDeformation(-0.09F, -0.476F, -0.3625F))
                .texOffs(485, 319).addBox(-0.5F, -6.326F, -1F, 1F, 3F, 2F, new CubeDeformation(-0.08F, -0.476F, -0.475F)),
                PartPose.offsetAndRotation(0F, -9F, 10F, -0.3491F, 0F, 0F));
        PartDefinition p_club_spike_3 = p_tail6.addOrReplaceChild("club_spike_3", CubeListBuilder.create()
                .texOffs(236, 310).addBox(-1.5F, -2.327F, -2F, 3F, 3F, 4F, new CubeDeformation(-0.1F, -0.073F, -0.25F))
                .texOffs(194, 319).addBox(-1F, -4.528F, -1.5F, 2F, 3F, 3F, new CubeDeformation(-0.09F, -0.476F, -0.3625F))
                .texOffs(492, 319).addBox(-0.5F, -6.326F, -1F, 1F, 3F, 2F, new CubeDeformation(-0.08F, -0.476F, -0.475F)),
                PartPose.offsetAndRotation(0F, 5.5F, 10F, 3.1416F, 0F, 0F));
        PartDefinition p_front_leg_l = p_body.addOrReplaceChild("front_leg_l", CubeListBuilder.create()
                .texOffs(322, 99).addBox(-6.5F, -5.6F, -4F, 13F, 18F, 8F, new CubeDeformation(0F, -0.4F, -0.36F))
                .texOffs(130, 65).addBox(-4F, -5.6F, -6.5F, 8F, 18F, 13F, new CubeDeformation(-0.36F, -0.48F, 0F))
                .texOffs(165, 194).addBox(-7.5F, -7.275F, -3.5F, 15F, 10F, 7F, new CubeDeformation(-0.4F, -0.125F, -0.3F))
                .texOffs(443, 99).addBox(-3.5F, -7.275F, -7.5F, 7F, 10F, 15F, new CubeDeformation(-0.3F, -0.205F, -0.4F)),
                PartPose.offsetAndRotation(12.5F, 5F, -14F, 0.0698F, 0F, 0F));
        PartDefinition p_front_shin_l = p_front_leg_l.addOrReplaceChild("front_shin_l", CubeListBuilder.create()
                .texOffs(247, 175).addBox(-5.5F, -1.6F, -3.5F, 11F, 11F, 7F, new CubeDeformation(0F, -0.2F, -0.42F))
                .texOffs(358, 127).addBox(-3.5F, -1.6F, -5.5F, 7F, 11F, 11F, new CubeDeformation(-0.42F, -0.28F, 0F)),
                PartPose.offsetAndRotation(0F, 11.4F, 0F, -0.1047F, 0F, 0F));
        PartDefinition p_front_foot_l = p_front_shin_l.addOrReplaceChild("front_foot_l", CubeListBuilder.create()
                .texOffs(400, 194).addBox(-6.5F, 0.5F, -9.8F, 13F, 2F, 14F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(59, 194).addBox(-6F, 0F, -9.8F, 12F, 3F, 14F, new CubeDeformation(-0.4F, 0F, -0.08F))
                .texOffs(474, 310).addBox(-5.5333F, 1.05F, -12.45F, 2F, 2F, 4F, new CubeDeformation(-0.05F, -0.05F, -0.25F))
                .texOffs(280, 319).addBox(-5.5333F, 1.43F, -14.55F, 2F, 2F, 3F, new CubeDeformation(-0.335F, -0.145F, -0.35F))
                .texOffs(487, 310).addBox(-1F, 1.05F, -12.45F, 2F, 2F, 4F, new CubeDeformation(-0.05F, -0.05F, -0.25F))
                .texOffs(291, 319).addBox(-1F, 1.43F, -14.55F, 2F, 2F, 3F, new CubeDeformation(-0.335F, -0.145F, -0.35F))
                .texOffs(500, 310).addBox(3.5333F, 1.05F, -12.45F, 2F, 2F, 4F, new CubeDeformation(-0.05F, -0.05F, -0.25F))
                .texOffs(302, 319).addBox(3.5333F, 1.43F, -14.55F, 2F, 2F, 3F, new CubeDeformation(-0.335F, -0.145F, -0.35F)),
                PartPose.offsetAndRotation(0F, 8.4F, 0F, 0.0349F, 0F, 0F));
        PartDefinition p_hind_leg_l = p_body.addOrReplaceChild("hind_leg_l", CubeListBuilder.create()
                .texOffs(322, 65).addBox(-7.5F, -6F, -5F, 15F, 19F, 10F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(361, 0).addBox(-4.5F, -6F, -8F, 9F, 19F, 16F, new CubeDeformation(-0.3F, -0.08F, 0F))
                .texOffs(0, 153).addBox(-8.5F, -8.625F, -4.5F, 17F, 12F, 9F, new CubeDeformation(-0.4F, -0.375F, -0.4F))
                .texOffs(216, 65).addBox(-4F, -8.625F, -9F, 8F, 12F, 18F, new CubeDeformation(-0.4F, -0.455F, -0.4F)),
                PartPose.offsetAndRotation(12.5F, 4F, 13F, -0.1396F, 0F, 0F));
        PartDefinition p_hind_shin_l = p_hind_leg_l.addOrReplaceChild("hind_shin_l", CubeListBuilder.create()
                .texOffs(169, 175).addBox(-6F, -1.6F, -3.5F, 12F, 11F, 7F, new CubeDeformation(0F, -0.2F, -0.14F))
                .texOffs(119, 127).addBox(-3.5F, -1.6F, -6F, 7F, 11F, 12F, new CubeDeformation(-0.14F, -0.28F, 0F)),
                PartPose.offsetAndRotation(0F, 12.4F, 0F, 0.2793F, 0F, 0F));
        PartDefinition p_hind_foot_l = p_hind_shin_l.addOrReplaceChild("hind_foot_l", CubeListBuilder.create()
                .texOffs(447, 175).addBox(-7F, 0.5F, -10.5F, 14F, 2F, 15F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(0, 175).addBox(-6.5F, 0F, -10.5F, 13F, 3F, 15F, new CubeDeformation(-0.4F, 0F, -0.08F))
                .texOffs(0, 319).addBox(-5.8667F, 1.05F, -13.25F, 2F, 2F, 4F, new CubeDeformation(-0.05F, -0.05F, -0.15F))
                .texOffs(313, 319).addBox(-5.8667F, 1.43F, -15.55F, 2F, 2F, 3F, new CubeDeformation(-0.335F, -0.145F, -0.25F))
                .texOffs(13, 319).addBox(-1F, 1.05F, -13.25F, 2F, 2F, 4F, new CubeDeformation(-0.05F, -0.05F, -0.15F))
                .texOffs(324, 319).addBox(-1F, 1.43F, -15.55F, 2F, 2F, 3F, new CubeDeformation(-0.335F, -0.145F, -0.25F))
                .texOffs(26, 319).addBox(3.8667F, 1.05F, -13.25F, 2F, 2F, 4F, new CubeDeformation(-0.05F, -0.05F, -0.15F))
                .texOffs(335, 319).addBox(3.8667F, 1.43F, -15.55F, 2F, 2F, 3F, new CubeDeformation(-0.335F, -0.145F, -0.25F)),
                PartPose.offsetAndRotation(0F, 8.4F, 0F, -0.1396F, 0F, 0F));
        PartDefinition p_front_leg_r = p_body.addOrReplaceChild("front_leg_r", CubeListBuilder.create()
                .texOffs(365, 99).addBox(-6.5F, -5.6F, -4F, 13F, 18F, 8F, new CubeDeformation(0F, -0.4F, -0.36F))
                .texOffs(173, 65).addBox(-4F, -5.6F, -6.5F, 8F, 18F, 13F, new CubeDeformation(-0.36F, -0.48F, 0F))
                .texOffs(210, 194).addBox(-7.5F, -7.275F, -3.5F, 15F, 10F, 7F, new CubeDeformation(-0.4F, -0.125F, -0.3F))
                .texOffs(0, 127).addBox(-3.5F, -7.275F, -7.5F, 7F, 10F, 15F, new CubeDeformation(-0.3F, -0.205F, -0.4F)),
                PartPose.offsetAndRotation(-12.5F, 5F, -14F, 0.0698F, 0F, 0F));
        PartDefinition p_front_shin_r = p_front_leg_r.addOrReplaceChild("front_shin_r", CubeListBuilder.create()
                .texOffs(284, 175).addBox(-5.5F, -1.6F, -3.5F, 11F, 11F, 7F, new CubeDeformation(0F, -0.2F, -0.42F))
                .texOffs(395, 127).addBox(-3.5F, -1.6F, -5.5F, 7F, 11F, 11F, new CubeDeformation(-0.42F, -0.28F, 0F)),
                PartPose.offsetAndRotation(0F, 11.4F, 0F, -0.1047F, 0F, 0F));
        PartDefinition p_front_foot_r = p_front_shin_r.addOrReplaceChild("front_foot_r", CubeListBuilder.create()
                .texOffs(455, 194).addBox(-6.5F, 0.5F, -9.8F, 13F, 2F, 14F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(112, 194).addBox(-6F, 0F, -9.8F, 12F, 3F, 14F, new CubeDeformation(-0.4F, 0F, -0.08F))
                .texOffs(39, 319).addBox(-5.5333F, 1.05F, -12.45F, 2F, 2F, 4F, new CubeDeformation(-0.05F, -0.05F, -0.25F))
                .texOffs(346, 319).addBox(-5.5333F, 1.43F, -14.55F, 2F, 2F, 3F, new CubeDeformation(-0.335F, -0.145F, -0.35F))
                .texOffs(52, 319).addBox(-1F, 1.05F, -12.45F, 2F, 2F, 4F, new CubeDeformation(-0.05F, -0.05F, -0.25F))
                .texOffs(357, 319).addBox(-1F, 1.43F, -14.55F, 2F, 2F, 3F, new CubeDeformation(-0.335F, -0.145F, -0.35F))
                .texOffs(65, 319).addBox(3.5333F, 1.05F, -12.45F, 2F, 2F, 4F, new CubeDeformation(-0.05F, -0.05F, -0.25F))
                .texOffs(368, 319).addBox(3.5333F, 1.43F, -14.55F, 2F, 2F, 3F, new CubeDeformation(-0.335F, -0.145F, -0.35F)),
                PartPose.offsetAndRotation(0F, 8.4F, 0F, 0.0349F, 0F, 0F));
        PartDefinition p_hind_leg_r = p_body.addOrReplaceChild("hind_leg_r", CubeListBuilder.create()
                .texOffs(373, 65).addBox(-7.5F, -6F, -5F, 15F, 19F, 10F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(412, 0).addBox(-4.5F, -6F, -8F, 9F, 19F, 16F, new CubeDeformation(-0.3F, -0.08F, 0F))
                .texOffs(53, 153).addBox(-8.5F, -8.625F, -4.5F, 17F, 12F, 9F, new CubeDeformation(-0.4F, -0.375F, -0.4F))
                .texOffs(269, 65).addBox(-4F, -8.625F, -9F, 8F, 12F, 18F, new CubeDeformation(-0.4F, -0.455F, -0.4F)),
                PartPose.offsetAndRotation(-12.5F, 4F, 13F, -0.1396F, 0F, 0F));
        PartDefinition p_hind_shin_r = p_hind_leg_r.addOrReplaceChild("hind_shin_r", CubeListBuilder.create()
                .texOffs(208, 175).addBox(-6F, -1.6F, -3.5F, 12F, 11F, 7F, new CubeDeformation(0F, -0.2F, -0.14F))
                .texOffs(158, 127).addBox(-3.5F, -1.6F, -6F, 7F, 11F, 12F, new CubeDeformation(-0.14F, -0.28F, 0F)),
                PartPose.offsetAndRotation(0F, 12.4F, 0F, 0.2793F, 0F, 0F));
        PartDefinition p_hind_foot_r = p_hind_shin_r.addOrReplaceChild("hind_foot_r", CubeListBuilder.create()
                .texOffs(0, 194).addBox(-7F, 0.5F, -10.5F, 14F, 2F, 15F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(57, 175).addBox(-6.5F, 0F, -10.5F, 13F, 3F, 15F, new CubeDeformation(-0.4F, 0F, -0.08F))
                .texOffs(78, 319).addBox(-5.8667F, 1.05F, -13.25F, 2F, 2F, 4F, new CubeDeformation(-0.05F, -0.05F, -0.15F))
                .texOffs(379, 319).addBox(-5.8667F, 1.43F, -15.55F, 2F, 2F, 3F, new CubeDeformation(-0.335F, -0.145F, -0.25F))
                .texOffs(91, 319).addBox(-1F, 1.05F, -13.25F, 2F, 2F, 4F, new CubeDeformation(-0.05F, -0.05F, -0.15F))
                .texOffs(390, 319).addBox(-1F, 1.43F, -15.55F, 2F, 2F, 3F, new CubeDeformation(-0.335F, -0.145F, -0.25F))
                .texOffs(104, 319).addBox(3.8667F, 1.05F, -13.25F, 2F, 2F, 4F, new CubeDeformation(-0.05F, -0.05F, -0.15F))
                .texOffs(401, 319).addBox(3.8667F, 1.43F, -15.55F, 2F, 2F, 3F, new CubeDeformation(-0.335F, -0.145F, -0.25F)),
                PartPose.offsetAndRotation(0F, 8.4F, 0F, -0.1396F, 0F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
