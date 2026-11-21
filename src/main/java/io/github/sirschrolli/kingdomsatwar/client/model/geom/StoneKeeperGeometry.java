package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Steinhüters (im Spiel 1,4-fach vergrößert).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class StoneKeeperGeometry {
    public static final int TEXTURE_WIDTH = 256;
    public static final int TEXTURE_HEIGHT = 256;

    private StoneKeeperGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(111, 48).addBox(-4F, -1.1F, -4F, 8F, 8F, 8F, new CubeDeformation(-0.4F, -0.1F, -0.4F)),
                PartPose.offset(5.6F, 10F, 0.5F));
        PartDefinition p_left_shin = p_left_leg.addOrReplaceChild("left_shin", CubeListBuilder.create()
                .texOffs(86, 66).addBox(-4F, -0.8F, -3.8F, 8F, 7F, 8F, new CubeDeformation(-0.1F, -0.2F, -0.1F))
                .texOffs(207, 82).addBox(-4.5F, 5.4F, -6F, 9F, 2F, 10F, new CubeDeformation(-0.1F, 0F, -0.2F))
                .texOffs(171, 124).addBox(-3.7F, 5.5F, -7.2F, 3F, 2F, 2F, new CubeDeformation(-0.3F, -0.1F, -0.2F))
                .texOffs(182, 124).addBox(-0.7F, 5.5F, -7.2F, 3F, 2F, 2F, new CubeDeformation(-0.3F, -0.1F, -0.2F))
                .texOffs(193, 124).addBox(2.3F, 5.5F, -7.2F, 3F, 2F, 2F, new CubeDeformation(-0.3F, -0.1F, -0.2F))
                .texOffs(204, 124).addBox(-2F, 0.6F, -4.4F, 4F, 3F, 1F, new CubeDeformation(0F, 0F, -0.1F)),
                PartPose.offset(0F, 6.6F, -0.4F));
        PartDefinition p_armor_left_leg = p_left_leg.addOrReplaceChild("armor_left_leg", CubeListBuilder.create()
                .texOffs(217, 95).addBox(-3F, -1.6F, -1.2F, 6F, 6F, 2F, new CubeDeformation(0F, 0F, -0.4F)),
                PartPose.offset(0F, 2F, -3.6F));
        PartDefinition p_armor_left_leg_0 = p_armor_left_leg.addOrReplaceChild("armor_left_leg_0", CubeListBuilder.create()
                .texOffs(129, 108).addBox(-1.5F, -2.926F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.074F, -0.4F)),
                PartPose.offsetAndRotation(0F, 2.6F, -0.6F, -1.0123F, 0F, 0.2094F));
        PartDefinition p_armor_left_leg_0_tip = p_armor_left_leg_0.addOrReplaceChild("armor_left_leg_0_tip", CubeListBuilder.create()
                .texOffs(64, 117).addBox(-1F, -2.604F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.318F, -0.396F, -0.318F)),
                PartPose.offsetAndRotation(0F, -2.552F, 0F, 0F, 0.7854F, 0F));
        PartDefinition p_armor_left_leg_1 = p_armor_left_leg.addOrReplaceChild("armor_left_leg_1", CubeListBuilder.create()
                .texOffs(73, 117).addBox(-1F, -2.616F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.1F, -0.384F, -0.1F)),
                PartPose.offsetAndRotation(1.6F, 0.4F, 0.4F, -0.6981F, 0F, 0.6283F));
        PartDefinition p_armor_left_leg_1_tip = p_armor_left_leg_1.addOrReplaceChild("armor_left_leg_1_tip", CubeListBuilder.create()
                .texOffs(11, 130).addBox(-1F, -1.864F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.442F, -0.136F, -0.442F)),
                PartPose.offsetAndRotation(0F, -1.932F, 0F, 0F, 0.7854F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(144, 48).addBox(-4F, -1.1F, -4F, 8F, 8F, 8F, new CubeDeformation(-0.4F, -0.1F, -0.4F)),
                PartPose.offset(-5.6F, 10F, 0.5F));
        PartDefinition p_right_shin = p_right_leg.addOrReplaceChild("right_shin", CubeListBuilder.create()
                .texOffs(119, 66).addBox(-4F, -0.8F, -3.8F, 8F, 7F, 8F, new CubeDeformation(-0.1F, -0.2F, -0.1F))
                .texOffs(0, 95).addBox(-4.5F, 5.4F, -6F, 9F, 2F, 10F, new CubeDeformation(-0.1F, 0F, -0.2F))
                .texOffs(215, 124).addBox(0.7F, 5.5F, -7.2F, 3F, 2F, 2F, new CubeDeformation(-0.3F, -0.1F, -0.2F))
                .texOffs(226, 124).addBox(-2.3F, 5.5F, -7.2F, 3F, 2F, 2F, new CubeDeformation(-0.3F, -0.1F, -0.2F))
                .texOffs(237, 124).addBox(-5.3F, 5.5F, -7.2F, 3F, 2F, 2F, new CubeDeformation(-0.3F, -0.1F, -0.2F))
                .texOffs(0, 130).addBox(-2F, 0.6F, -4.4F, 4F, 3F, 1F, new CubeDeformation(0F, 0F, -0.1F)),
                PartPose.offset(0F, 6.6F, -0.4F));
        PartDefinition p_armor_right_leg = p_right_leg.addOrReplaceChild("armor_right_leg", CubeListBuilder.create()
                .texOffs(234, 95).addBox(-3F, -1.6F, -1.2F, 6F, 6F, 2F, new CubeDeformation(0F, 0F, -0.4F)),
                PartPose.offset(0F, 2F, -3.6F));
        PartDefinition p_armor_right_leg_0 = p_armor_right_leg.addOrReplaceChild("armor_right_leg_0", CubeListBuilder.create()
                .texOffs(142, 108).addBox(-1.5F, -2.926F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.074F, -0.4F)),
                PartPose.offsetAndRotation(0F, 2.6F, -0.6F, -1.0123F, 0F, -0.2094F));
        PartDefinition p_armor_right_leg_0_tip = p_armor_right_leg_0.addOrReplaceChild("armor_right_leg_0_tip", CubeListBuilder.create()
                .texOffs(82, 117).addBox(-1F, -2.604F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.318F, -0.396F, -0.318F)),
                PartPose.offsetAndRotation(0F, -2.552F, 0F, 0F, 0.7854F, 0F));
        PartDefinition p_armor_right_leg_1 = p_armor_right_leg.addOrReplaceChild("armor_right_leg_1", CubeListBuilder.create()
                .texOffs(91, 117).addBox(-1F, -2.616F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.1F, -0.384F, -0.1F)),
                PartPose.offsetAndRotation(-1.6F, 0.4F, 0.4F, -0.6981F, 0F, -0.6283F));
        PartDefinition p_armor_right_leg_1_tip = p_armor_right_leg_1.addOrReplaceChild("armor_right_leg_1_tip", CubeListBuilder.create()
                .texOffs(20, 130).addBox(-1F, -1.864F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.442F, -0.136F, -0.442F)),
                PartPose.offsetAndRotation(0F, -1.932F, 0F, 0F, 0.7854F, 0F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(58, 48).addBox(-8F, -4.6F, -5F, 16F, 6F, 10F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(74, 27).addBox(-9F, -11.8F, -5.5F, 18F, 8F, 11F, new CubeDeformation(-0.4F, -0.2F, -0.1F))
                .texOffs(152, 66).addBox(-9F, -6.35F, -6F, 18F, 2F, 12F, new CubeDeformation(-0.1F, -0.15F, -0.3F))
                .texOffs(0, 0).addBox(-11.5F, -23.1F, -7F, 23F, 12F, 14F, new CubeDeformation(0F, -0.1F, -0.2F))
                .texOffs(177, 48).addBox(-9F, -22F, 4.2F, 18F, 10F, 5F, new CubeDeformation(0F, 0F, -0.2F))
                .texOffs(74, 95).addBox(-6.5F, -24.8F, -2F, 13F, 3F, 8F, new CubeDeformation(-0.1F, -0.4F, 0F))
                .texOffs(156, 95).addBox(-7F, -20.6F, -8.2F, 14F, 9F, 2F, new CubeDeformation(-0.4F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(0F, 10F, 0F, 0.1745F, 0F, 0F));
        PartDefinition p_core = p_body.addOrReplaceChild("core", CubeListBuilder.create()
                .texOffs(74, 108).addBox(-2.5F, -2.5F, -1.1F, 5F, 5F, 2F, new CubeDeformation(-0.2F, -0.1F, -0.2F))
                .texOffs(206, 95).addBox(-1.5F, -3.5F, -1F, 3F, 7F, 2F, new CubeDeformation(-0.2F, -0.1F, -0.4F))
                .texOffs(23, 117).addBox(-3.5F, -1.5F, -1F, 7F, 3F, 2F, new CubeDeformation(-0.2F, -0.1F, -0.4F)),
                PartPose.offset(0F, -15.8F, -8F));
        PartDefinition p_back_crystal_0 = p_body.addOrReplaceChild("back_crystal_0", CubeListBuilder.create()
                .texOffs(189, 95).addBox(-2F, -5.79F, -2F, 4F, 6F, 4F, new CubeDeformation(-0.4F, -0.21F, -0.4F)),
                PartPose.offsetAndRotation(0F, -21.6F, 6.8F, -0.5934F, 0F, 0F));
        PartDefinition p_back_crystal_0_tip = p_back_crystal_0.addOrReplaceChild("back_crystal_0_tip", CubeListBuilder.create()
                .texOffs(115, 108).addBox(-1F, -4.66F, -1F, 2F, 5F, 2F, new CubeDeformation(-0.008F, -0.34F, -0.008F)),
                PartPose.offsetAndRotation(0F, -5.28F, 0F, 0F, 0.7854F, 0F));
        PartDefinition p_back_crystal_1 = p_body.addOrReplaceChild("back_crystal_1", CubeListBuilder.create()
                .texOffs(0, 108).addBox(-1.5F, -4.546F, -1.5F, 3F, 5F, 3F, new CubeDeformation(-0.2F, -0.454F, -0.2F)),
                PartPose.offsetAndRotation(-3.6F, -21.6F, 7.6F, -0.4887F, 0F, -0.4189F));
        PartDefinition p_back_crystal_1_tip = p_back_crystal_1.addOrReplaceChild("back_crystal_1_tip", CubeListBuilder.create()
                .texOffs(233, 108).addBox(-1F, -3.584F, -1F, 2F, 4F, 2F, new CubeDeformation(-0.194F, -0.416F, -0.194F)),
                PartPose.offsetAndRotation(0F, -3.792F, 0F, 0F, 0.7854F, 0F));
        PartDefinition p_back_crystal_2 = p_body.addOrReplaceChild("back_crystal_2", CubeListBuilder.create()
                .texOffs(13, 108).addBox(-1.5F, -4.732F, -1.5F, 3F, 5F, 3F, new CubeDeformation(-0.2F, -0.268F, -0.2F)),
                PartPose.offsetAndRotation(3.6F, -21.6F, 7.4F, -0.5236F, 0F, 0.384F));
        PartDefinition p_back_crystal_2_tip = p_back_crystal_2.addOrReplaceChild("back_crystal_2_tip", CubeListBuilder.create()
                .texOffs(242, 108).addBox(-1F, -3.728F, -1F, 2F, 4F, 2F, new CubeDeformation(-0.194F, -0.272F, -0.194F)),
                PartPose.offsetAndRotation(0F, -4.164F, 0F, 0F, 0.7854F, 0F));
        PartDefinition p_back_crystal_3 = p_body.addOrReplaceChild("back_crystal_3", CubeListBuilder.create()
                .texOffs(100, 117).addBox(-1F, -2.864F, -1F, 2F, 3F, 2F, new CubeDeformation(0F, -0.136F, 0F)),
                PartPose.offsetAndRotation(-6.4F, -21.6F, 8.4F, -0.3491F, 0F, -0.6981F));
        PartDefinition p_back_crystal_3_tip = p_back_crystal_3.addOrReplaceChild("back_crystal_3_tip", CubeListBuilder.create()
                .texOffs(109, 117).addBox(-1F, -2.556F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.38F, -0.444F, -0.38F)),
                PartPose.offsetAndRotation(0F, -2.428F, 0F, 0F, 0.7854F, 0F));
        PartDefinition p_back_crystal_4 = p_body.addOrReplaceChild("back_crystal_4", CubeListBuilder.create()
                .texOffs(118, 117).addBox(-1F, -2.988F, -1F, 2F, 3F, 2F, new CubeDeformation(0F, -0.012F, 0F)),
                PartPose.offsetAndRotation(6.2F, -21.6F, 8.2F, -0.4189F, 0F, 0.6632F));
        PartDefinition p_back_crystal_4_tip = p_back_crystal_4.addOrReplaceChild("back_crystal_4_tip", CubeListBuilder.create()
                .texOffs(127, 117).addBox(-1F, -2.652F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.38F, -0.348F, -0.38F)),
                PartPose.offsetAndRotation(0F, -2.676F, 0F, 0F, 0.7854F, 0F));
        PartDefinition p_back_crystal_5 = p_body.addOrReplaceChild("back_crystal_5", CubeListBuilder.create()
                .texOffs(136, 117).addBox(-1F, -2.926F, -1F, 2F, 3F, 2F, new CubeDeformation(0F, -0.074F, 0F)),
                PartPose.offsetAndRotation(1.6F, -21.6F, 10F, -1.0123F, 0F, 0.1745F));
        PartDefinition p_back_crystal_5_tip = p_back_crystal_5.addOrReplaceChild("back_crystal_5_tip", CubeListBuilder.create()
                .texOffs(145, 117).addBox(-1F, -2.604F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.38F, -0.396F, -0.38F)),
                PartPose.offsetAndRotation(0F, -2.552F, 0F, 0F, 0.7854F, 0F));
        PartDefinition p_back_crystal_6 = p_body.addOrReplaceChild("back_crystal_6", CubeListBuilder.create()
                .texOffs(154, 117).addBox(-1F, -2.678F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.1F, -0.322F, -0.1F)),
                PartPose.offsetAndRotation(-1.8F, -21.6F, 9.8F, -1.0821F, 0F, -0.2443F));
        PartDefinition p_back_crystal_6_tip = p_back_crystal_6.addOrReplaceChild("back_crystal_6_tip", CubeListBuilder.create()
                .texOffs(29, 130).addBox(-1F, -1.912F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.442F, -0.088F, -0.442F)),
                PartPose.offsetAndRotation(0F, -2.056F, 0F, 0F, 0.7854F, 0F));
        PartDefinition p_vine_back_0 = p_body.addOrReplaceChild("vine_back_0", CubeListBuilder.create()
                .texOffs(26, 108).addBox(-0.5F, 0F, -0.5F, 1F, 7F, 1F, new CubeDeformation(-0.1F, 0F, -0.1F)),
                PartPose.offset(-7.6F, -14F, 9F));
        PartDefinition p_vine_back_1 = p_body.addOrReplaceChild("vine_back_1", CubeListBuilder.create()
                .texOffs(18, 117).addBox(-0.5F, -0.4F, -0.5F, 1F, 5F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.1F)),
                PartPose.offset(-4.4F, -14F, 9F));
        PartDefinition p_vine_back_2 = p_body.addOrReplaceChild("vine_back_2", CubeListBuilder.create()
                .texOffs(124, 108).addBox(-0.5F, -0.2F, -0.5F, 1F, 6F, 1F, new CubeDeformation(-0.1F, -0.2F, -0.1F)),
                PartPose.offset(7.2F, -14F, 9F));
        PartDefinition p_armor_chest = p_body.addOrReplaceChild("armor_chest", CubeListBuilder.create()
                .texOffs(213, 66).addBox(-10.1F, -7.1F, -0.9F, 7F, 11F, 2F, new CubeDeformation(-0.1F, -0.3F, -0.3F))
                .texOffs(232, 66).addBox(3.1F, -7.1F, -0.9F, 7F, 11F, 2F, new CubeDeformation(-0.1F, -0.3F, -0.3F)),
                PartPose.offset(0F, -16F, -8.2F));
        PartDefinition p_armor_chest_0 = p_armor_chest.addOrReplaceChild("armor_chest_0", CubeListBuilder.create()
                .texOffs(155, 108).addBox(-1.5F, -2.864F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.136F, -0.4F)),
                PartPose.offsetAndRotation(-6.6F, -5.6F, -0.4F, -1.0821F, 0F, -0.384F));
        PartDefinition p_armor_chest_0_tip = p_armor_chest_0.addOrReplaceChild("armor_chest_0_tip", CubeListBuilder.create()
                .texOffs(163, 117).addBox(-1F, -2.556F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.318F, -0.444F, -0.318F)),
                PartPose.offsetAndRotation(0F, -2.428F, 0F, 0F, 0.7854F, 0F));
        PartDefinition p_armor_chest_1 = p_armor_chest.addOrReplaceChild("armor_chest_1", CubeListBuilder.create()
                .texOffs(168, 108).addBox(-1.5F, -2.864F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.136F, -0.4F)),
                PartPose.offsetAndRotation(6.6F, -5.6F, -0.4F, -1.0821F, 0F, 0.384F));
        PartDefinition p_armor_chest_1_tip = p_armor_chest_1.addOrReplaceChild("armor_chest_1_tip", CubeListBuilder.create()
                .texOffs(172, 117).addBox(-1F, -2.556F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.318F, -0.444F, -0.318F)),
                PartPose.offsetAndRotation(0F, -2.428F, 0F, 0F, 0.7854F, 0F));
        PartDefinition p_armor_chest_2 = p_armor_chest.addOrReplaceChild("armor_chest_2", CubeListBuilder.create()
                .texOffs(38, 130).addBox(-1F, -1.992F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.1F, -0.008F, -0.1F)),
                PartPose.offsetAndRotation(-6F, 2.8F, -0.4F, -1.3963F, 0F, -0.1745F));
        PartDefinition p_armor_chest_2_tip = p_armor_chest_2.addOrReplaceChild("armor_chest_2_tip", CubeListBuilder.create()
                .texOffs(47, 130).addBox(-1F, -1.768F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.442F, -0.232F, -0.442F)),
                PartPose.offsetAndRotation(0F, -1.684F, 0F, 0F, 0.7854F, 0F));
        PartDefinition p_armor_chest_3 = p_armor_chest.addOrReplaceChild("armor_chest_3", CubeListBuilder.create()
                .texOffs(56, 130).addBox(-1F, -1.992F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.1F, -0.008F, -0.1F)),
                PartPose.offsetAndRotation(6F, 2.8F, -0.4F, -1.3963F, 0F, 0.1745F));
        PartDefinition p_armor_chest_3_tip = p_armor_chest_3.addOrReplaceChild("armor_chest_3_tip", CubeListBuilder.create()
                .texOffs(65, 130).addBox(-1F, -1.768F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.442F, -0.232F, -0.442F)),
                PartPose.offsetAndRotation(0F, -1.684F, 0F, 0F, 0.7854F, 0F));
        PartDefinition p_boulder = p_body.addOrReplaceChild("boulder", CubeListBuilder.create()
                .texOffs(75, 0).addBox(-7F, -6F, -6.5F, 14F, 12F, 13F, new CubeDeformation(-0.2F, -0.4F, -0.3F))
                .texOffs(117, 95).addBox(-5F, -7.2F, -4.5F, 10F, 2F, 9F, new CubeDeformation(0F, 0F, -0.1F))
                .texOffs(0, 82).addBox(-5.5F, 5.1F, -5F, 11F, 2F, 10F, new CubeDeformation(-0.1F, -0.1F, -0.4F)),
                PartPose.offset(0F, -44F, -6F));
        PartDefinition p_boulder_crystal_0 = p_boulder.addOrReplaceChild("boulder_crystal_0", CubeListBuilder.create()
                .texOffs(181, 117).addBox(-1F, -2.802F, -1F, 2F, 3F, 2F, new CubeDeformation(0F, -0.198F, 0F)),
                PartPose.offsetAndRotation(3.4F, -5F, -2F, -0.3491F, 0F, 0.5236F));
        PartDefinition p_boulder_crystal_0_tip = p_boulder_crystal_0.addOrReplaceChild("boulder_crystal_0_tip", CubeListBuilder.create()
                .texOffs(190, 117).addBox(-1F, -2.508F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.38F, -0.492F, -0.38F)),
                PartPose.offsetAndRotation(0F, -2.304F, 0F, 0F, 0.7854F, 0F));
        PartDefinition p_boulder_crystal_1 = p_boulder.addOrReplaceChild("boulder_crystal_1", CubeListBuilder.create()
                .texOffs(74, 130).addBox(-1F, -1.992F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.2F, -0.008F, -0.2F)),
                PartPose.offsetAndRotation(-4F, -4.8F, 2.4F, 0.4189F, 0F, -0.4538F));
        PartDefinition p_boulder_crystal_1_tip = p_boulder_crystal_1.addOrReplaceChild("boulder_crystal_1_tip", CubeListBuilder.create()
                .texOffs(183, 130).addBox(-0.5F, -1.768F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.004F, -0.232F, -0.004F)),
                PartPose.offsetAndRotation(0F, -1.684F, 0F, 0F, 0.7854F, 0F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(133, 27).addBox(-5F, -8F, -5.7F, 10F, 9F, 9F, new CubeDeformation(-0.2F, -0.4F, -0.3F))
                .texOffs(41, 108).addBox(-6F, -6.8F, -7.4F, 12F, 3F, 4F, new CubeDeformation(-0.4F, 0F, -0.4F))
                .texOffs(42, 117).addBox(-1.5F, -4.5F, -7.5F, 3F, 3F, 2F, new CubeDeformation(-0.4F, -0.1F, -0.3F))
                .texOffs(165, 130).addBox(-4.1F, -4.05F, -6.3F, 3F, 2F, 1F, new CubeDeformation(-0.1F, -0.35F, -0.1F))
                .texOffs(174, 130).addBox(1.1F, -4.05F, -6.3F, 3F, 2F, 1F, new CubeDeformation(-0.1F, -0.35F, -0.1F))
                .texOffs(53, 117).addBox(-1.5F, -9.2F, -6.5F, 3F, 3F, 2F, new CubeDeformation(-0.2F, -0.3F, -0.4F)),
                PartPose.offsetAndRotation(0F, -22.6F, -6.6F, -0.1745F, 0F, 0F));
        PartDefinition p_head_crystal_0 = p_head.addOrReplaceChild("head_crystal_0", CubeListBuilder.create()
                .texOffs(199, 117).addBox(-1F, -2.864F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.1F, -0.136F, -0.1F)),
                PartPose.offsetAndRotation(-2.2F, -7.2F, 0.6F, -0.5236F, 0F, -0.3142F));
        PartDefinition p_head_crystal_0_tip = p_head_crystal_0.addOrReplaceChild("head_crystal_0_tip", CubeListBuilder.create()
                .texOffs(208, 117).addBox(-1F, -2.556F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.442F, -0.444F, -0.442F)),
                PartPose.offsetAndRotation(0F, -2.428F, 0F, 0F, 0.7854F, 0F));
        PartDefinition p_head_crystal_1 = p_head.addOrReplaceChild("head_crystal_1", CubeListBuilder.create()
                .texOffs(217, 117).addBox(-1F, -2.554F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.25F, -0.446F, -0.25F)),
                PartPose.offsetAndRotation(2F, -7.2F, 1.4F, -0.6632F, 0F, 0.3491F));
        PartDefinition p_head_crystal_1_tip = p_head_crystal_1.addOrReplaceChild("head_crystal_1_tip", CubeListBuilder.create()
                .texOffs(188, 130).addBox(-0.5F, -1.816F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.035F, -0.184F, -0.035F)),
                PartPose.offsetAndRotation(0F, -1.808F, 0F, 0F, 0.7854F, 0F));
        PartDefinition p_jaw = p_head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(39, 95).addBox(-4.5F, -0.8F, -4.8F, 9F, 4F, 8F, new CubeDeformation(-0.3F, -0.4F, -0.2F))
                .texOffs(213, 130).addBox(-3.5F, -1F, -5F, 7F, 1F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.1F)),
                PartPose.offsetAndRotation(0F, -1F, -2.6F, 0.0698F, 0F, 0F));
        PartDefinition p_armor_head = p_head.addOrReplaceChild("armor_head", CubeListBuilder.create(),
                PartPose.offset(0F, -7.6F, -1F));
        PartDefinition p_armor_head_0 = p_armor_head.addOrReplaceChild("armor_head_0", CubeListBuilder.create()
                .texOffs(226, 117).addBox(-1F, -2.554F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.2F, -0.446F, -0.2F)),
                PartPose.offsetAndRotation(-3.6F, 0.2F, -3.4F, -0.2094F, 0F, -0.4189F));
        PartDefinition p_armor_head_0_tip = p_armor_head_0.addOrReplaceChild("armor_head_0_tip", CubeListBuilder.create()
                .texOffs(193, 130).addBox(-0.5F, -1.816F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.004F, -0.184F, -0.004F)),
                PartPose.offsetAndRotation(0F, -1.808F, 0F, 0F, 0.7854F, 0F));
        PartDefinition p_armor_head_1 = p_armor_head.addOrReplaceChild("armor_head_1", CubeListBuilder.create()
                .texOffs(235, 117).addBox(-1F, -2.926F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.2F, -0.074F, -0.2F)),
                PartPose.offsetAndRotation(-1.2F, 0.2F, -3.4F, -0.2094F, 0F, -0.1396F));
        PartDefinition p_armor_head_1_tip = p_armor_head_1.addOrReplaceChild("armor_head_1_tip", CubeListBuilder.create()
                .texOffs(155, 130).addBox(-0.5F, -2.604F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.004F, -0.396F, -0.004F)),
                PartPose.offsetAndRotation(0F, -2.552F, 0F, 0F, 0.7854F, 0F));
        PartDefinition p_armor_head_2 = p_armor_head.addOrReplaceChild("armor_head_2", CubeListBuilder.create()
                .texOffs(244, 117).addBox(-1F, -2.926F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.2F, -0.074F, -0.2F)),
                PartPose.offsetAndRotation(1.2F, 0.2F, -3.4F, -0.2094F, 0F, 0.1396F));
        PartDefinition p_armor_head_2_tip = p_armor_head_2.addOrReplaceChild("armor_head_2_tip", CubeListBuilder.create()
                .texOffs(160, 130).addBox(-0.5F, -2.604F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.004F, -0.396F, -0.004F)),
                PartPose.offsetAndRotation(0F, -2.552F, 0F, 0F, 0.7854F, 0F));
        PartDefinition p_armor_head_3 = p_armor_head.addOrReplaceChild("armor_head_3", CubeListBuilder.create()
                .texOffs(0, 124).addBox(-1F, -2.554F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.2F, -0.446F, -0.2F)),
                PartPose.offsetAndRotation(3.6F, 0.2F, -3.4F, -0.2094F, 0F, 0.4189F));
        PartDefinition p_armor_head_3_tip = p_armor_head_3.addOrReplaceChild("armor_head_3_tip", CubeListBuilder.create()
                .texOffs(198, 130).addBox(-0.5F, -1.816F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.004F, -0.184F, -0.004F)),
                PartPose.offsetAndRotation(0F, -1.808F, 0F, 0F, 0.7854F, 0F));
        PartDefinition p_left_arm = p_body.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(130, 0).addBox(-5.4F, -6.2F, -6F, 12F, 10F, 12F, new CubeDeformation(-0.4F, -0.2F, 0F))
                .texOffs(0, 48).addBox(-3.5F, 2.5F, -3.5F, 7F, 10F, 7F, new CubeDeformation(-0.3F, -0.1F, -0.2F)),
                PartPose.offsetAndRotation(12.6F, -19.6F, 0.4F, 0F, 0F, -0.1571F));
        PartDefinition p_left_shoulder_crystal_0 = p_left_arm.addOrReplaceChild("left_shoulder_crystal_0", CubeListBuilder.create()
                .texOffs(89, 108).addBox(-1.5F, -3.984F, -1.5F, 3F, 4F, 3F, new CubeDeformation(-0.2F, -0.016F, -0.2F)),
                PartPose.offsetAndRotation(1.4F, -5.6F, 0.2F, -0.1396F, 0F, 0.384F));
        PartDefinition p_left_shoulder_crystal_0_tip = p_left_shoulder_crystal_0.addOrReplaceChild("left_shoulder_crystal_0_tip", CubeListBuilder.create()
                .texOffs(0, 117).addBox(-1F, -3.536F, -1F, 2F, 4F, 2F, new CubeDeformation(-0.194F, -0.464F, -0.194F)),
                PartPose.offsetAndRotation(0F, -3.668F, 0F, 0F, 0.7854F, 0F));
        PartDefinition p_left_shoulder_crystal_1 = p_left_arm.addOrReplaceChild("left_shoulder_crystal_1", CubeListBuilder.create()
                .texOffs(181, 108).addBox(-1.5F, -2.926F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.074F, -0.4F)),
                PartPose.offsetAndRotation(3F, -5.6F, 2.6F, -0.5585F, 0F, 0.6632F));
        PartDefinition p_left_shoulder_crystal_1_tip = p_left_shoulder_crystal_1.addOrReplaceChild("left_shoulder_crystal_1_tip", CubeListBuilder.create()
                .texOffs(9, 124).addBox(-1F, -2.604F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.318F, -0.396F, -0.318F)),
                PartPose.offsetAndRotation(0F, -2.552F, 0F, 0F, 0.7854F, 0F));
        PartDefinition p_left_shoulder_crystal_2 = p_left_arm.addOrReplaceChild("left_shoulder_crystal_2", CubeListBuilder.create()
                .texOffs(18, 124).addBox(-1F, -2.74F, -1F, 2F, 3F, 2F, new CubeDeformation(0F, -0.26F, 0F)),
                PartPose.offsetAndRotation(0.4F, -5.6F, -2.4F, 0.3142F, 0F, 0.2443F));
        PartDefinition p_left_shoulder_crystal_2_tip = p_left_shoulder_crystal_2.addOrReplaceChild("left_shoulder_crystal_2_tip", CubeListBuilder.create()
                .texOffs(83, 130).addBox(-1F, -1.96F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.38F, -0.04F, -0.38F)),
                PartPose.offsetAndRotation(0F, -2.18F, 0F, 0F, 0.7854F, 0F));
        PartDefinition p_left_shoulder_crystal_3 = p_left_arm.addOrReplaceChild("left_shoulder_crystal_3", CubeListBuilder.create()
                .texOffs(92, 130).addBox(-1F, -1.992F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.2F, -0.008F, -0.2F)),
                PartPose.offsetAndRotation(4.2F, -5.6F, -1F, 0.0698F, 0F, 0.9774F));
        PartDefinition p_left_shoulder_crystal_3_tip = p_left_shoulder_crystal_3.addOrReplaceChild("left_shoulder_crystal_3_tip", CubeListBuilder.create()
                .texOffs(203, 130).addBox(-0.5F, -1.768F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.004F, -0.232F, -0.004F)),
                PartPose.offsetAndRotation(0F, -1.684F, 0F, 0F, 0.7854F, 0F));
        PartDefinition p_vine_left = p_left_arm.addOrReplaceChild("vine_left", CubeListBuilder.create()
                .texOffs(31, 108).addBox(-0.5F, -0.3F, -0.5F, 1F, 7F, 1F, new CubeDeformation(-0.1F, -0.3F, -0.1F)),
                PartPose.offset(4.4F, 3.2F, -3F));
        PartDefinition p_left_forearm = p_left_arm.addOrReplaceChild("left_forearm", CubeListBuilder.create()
                .texOffs(0, 27).addBox(-4.5F, -0.9F, -4.5F, 9F, 11F, 9F, new CubeDeformation(-0.3F, -0.1F, -0.2F))
                .texOffs(43, 82).addBox(-5F, 2.1F, -5F, 10F, 2F, 10F, new CubeDeformation(-0.4F, -0.1F, -0.3F))
                .texOffs(84, 82).addBox(-5F, 5.7F, -5F, 10F, 2F, 10F, new CubeDeformation(-0.4F, -0.3F, -0.3F))
                .texOffs(172, 27).addBox(-5F, 9.5F, -5F, 10F, 7F, 10F, new CubeDeformation(-0.1F, -0.1F, 0F))
                .texOffs(27, 124).addBox(-4.4F, 13.3F, -6.2F, 2F, 3F, 2F, new CubeDeformation(0F, -0.3F, -0.3F))
                .texOffs(36, 124).addBox(-2.1F, 13.3F, -6.2F, 2F, 3F, 2F, new CubeDeformation(0F, -0.3F, -0.3F))
                .texOffs(45, 124).addBox(0.2F, 13.3F, -6.2F, 2F, 3F, 2F, new CubeDeformation(0F, -0.3F, -0.3F))
                .texOffs(54, 124).addBox(2.5F, 13.3F, -6.2F, 2F, 3F, 2F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(0F, 11.8F, 0.2F, -0.2443F, 0F, 0F));
        PartDefinition p_armor_left_arm = p_left_forearm.addOrReplaceChild("armor_left_arm", CubeListBuilder.create()
                .texOffs(0, 66).addBox(-5.3F, -3F, -5F, 11F, 5F, 10F, new CubeDeformation(-0.3F, 0F, 0F)),
                PartPose.offset(0F, 4F, 0F));
        PartDefinition p_armor_left_arm_0 = p_armor_left_arm.addOrReplaceChild("armor_left_arm_0", CubeListBuilder.create()
                .texOffs(194, 108).addBox(-1.5F, -2.988F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.012F, -0.4F)),
                PartPose.offsetAndRotation(4.6F, -1F, -1.6F, 0F, 0F, 1.2217F));
        PartDefinition p_armor_left_arm_0_tip = p_armor_left_arm_0.addOrReplaceChild("armor_left_arm_0_tip", CubeListBuilder.create()
                .texOffs(63, 124).addBox(-1F, -2.652F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.318F, -0.348F, -0.318F)),
                PartPose.offsetAndRotation(0F, -2.676F, 0F, 0F, 0.7854F, 0F));
        PartDefinition p_armor_left_arm_1 = p_armor_left_arm.addOrReplaceChild("armor_left_arm_1", CubeListBuilder.create()
                .texOffs(72, 124).addBox(-1F, -2.616F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.1F, -0.384F, -0.1F)),
                PartPose.offsetAndRotation(4.4F, 1F, 2.2F, 0.2443F, 0F, 1.3614F));
        PartDefinition p_armor_left_arm_1_tip = p_armor_left_arm_1.addOrReplaceChild("armor_left_arm_1_tip", CubeListBuilder.create()
                .texOffs(101, 130).addBox(-1F, -1.864F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.442F, -0.136F, -0.442F)),
                PartPose.offsetAndRotation(0F, -1.932F, 0F, 0F, 0.7854F, 0F));
        PartDefinition p_armor_left_arm_2 = p_armor_left_arm.addOrReplaceChild("armor_left_arm_2", CubeListBuilder.create()
                .texOffs(81, 124).addBox(-1F, -2.616F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.1F, -0.384F, -0.1F)),
                PartPose.offsetAndRotation(0F, -1.4F, 5F, -1.2217F, 0F, 0F));
        PartDefinition p_armor_left_arm_2_tip = p_armor_left_arm_2.addOrReplaceChild("armor_left_arm_2_tip", CubeListBuilder.create()
                .texOffs(110, 130).addBox(-1F, -1.864F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.442F, -0.136F, -0.442F)),
                PartPose.offsetAndRotation(0F, -1.932F, 0F, 0F, 0.7854F, 0F));
        PartDefinition p_right_arm = p_body.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(179, 0).addBox(-6.6F, -6.2F, -6F, 12F, 10F, 12F, new CubeDeformation(-0.4F, -0.2F, 0F))
                .texOffs(29, 48).addBox(-3.5F, 2.5F, -3.5F, 7F, 10F, 7F, new CubeDeformation(-0.3F, -0.1F, -0.2F)),
                PartPose.offsetAndRotation(-12.6F, -19.6F, 0.4F, 0F, 0F, 0.1571F));
        PartDefinition p_right_shoulder_crystal_0 = p_right_arm.addOrReplaceChild("right_shoulder_crystal_0", CubeListBuilder.create()
                .texOffs(102, 108).addBox(-1.5F, -3.984F, -1.5F, 3F, 4F, 3F, new CubeDeformation(-0.2F, -0.016F, -0.2F)),
                PartPose.offsetAndRotation(-1.4F, -5.6F, 0.2F, -0.1396F, 0F, -0.384F));
        PartDefinition p_right_shoulder_crystal_0_tip = p_right_shoulder_crystal_0.addOrReplaceChild("right_shoulder_crystal_0_tip", CubeListBuilder.create()
                .texOffs(9, 117).addBox(-1F, -3.536F, -1F, 2F, 4F, 2F, new CubeDeformation(-0.194F, -0.464F, -0.194F)),
                PartPose.offsetAndRotation(0F, -3.668F, 0F, 0F, 0.7854F, 0F));
        PartDefinition p_right_shoulder_crystal_1 = p_right_arm.addOrReplaceChild("right_shoulder_crystal_1", CubeListBuilder.create()
                .texOffs(207, 108).addBox(-1.5F, -2.926F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.074F, -0.4F)),
                PartPose.offsetAndRotation(-3F, -5.6F, 2.6F, -0.5585F, 0F, -0.6632F));
        PartDefinition p_right_shoulder_crystal_1_tip = p_right_shoulder_crystal_1.addOrReplaceChild("right_shoulder_crystal_1_tip", CubeListBuilder.create()
                .texOffs(90, 124).addBox(-1F, -2.604F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.318F, -0.396F, -0.318F)),
                PartPose.offsetAndRotation(0F, -2.552F, 0F, 0F, 0.7854F, 0F));
        PartDefinition p_right_shoulder_crystal_2 = p_right_arm.addOrReplaceChild("right_shoulder_crystal_2", CubeListBuilder.create()
                .texOffs(99, 124).addBox(-1F, -2.74F, -1F, 2F, 3F, 2F, new CubeDeformation(0F, -0.26F, 0F)),
                PartPose.offsetAndRotation(-0.4F, -5.6F, -2.4F, 0.3142F, 0F, -0.2443F));
        PartDefinition p_right_shoulder_crystal_2_tip = p_right_shoulder_crystal_2.addOrReplaceChild("right_shoulder_crystal_2_tip", CubeListBuilder.create()
                .texOffs(119, 130).addBox(-1F, -1.96F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.38F, -0.04F, -0.38F)),
                PartPose.offsetAndRotation(0F, -2.18F, 0F, 0F, 0.7854F, 0F));
        PartDefinition p_right_shoulder_crystal_3 = p_right_arm.addOrReplaceChild("right_shoulder_crystal_3", CubeListBuilder.create()
                .texOffs(128, 130).addBox(-1F, -1.992F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.2F, -0.008F, -0.2F)),
                PartPose.offsetAndRotation(-4.2F, -5.6F, -1F, 0.0698F, 0F, -0.9774F));
        PartDefinition p_right_shoulder_crystal_3_tip = p_right_shoulder_crystal_3.addOrReplaceChild("right_shoulder_crystal_3_tip", CubeListBuilder.create()
                .texOffs(208, 130).addBox(-0.5F, -1.768F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.004F, -0.232F, -0.004F)),
                PartPose.offsetAndRotation(0F, -1.684F, 0F, 0F, 0.7854F, 0F));
        PartDefinition p_vine_right = p_right_arm.addOrReplaceChild("vine_right", CubeListBuilder.create()
                .texOffs(36, 108).addBox(-0.5F, -0.3F, -0.5F, 1F, 7F, 1F, new CubeDeformation(-0.1F, -0.3F, -0.1F)),
                PartPose.offset(-4.4F, 3.2F, -3F));
        PartDefinition p_right_forearm = p_right_arm.addOrReplaceChild("right_forearm", CubeListBuilder.create()
                .texOffs(37, 27).addBox(-4.5F, -0.9F, -4.5F, 9F, 11F, 9F, new CubeDeformation(-0.3F, -0.1F, -0.2F))
                .texOffs(125, 82).addBox(-5F, 2.1F, -5F, 10F, 2F, 10F, new CubeDeformation(-0.4F, -0.1F, -0.3F))
                .texOffs(166, 82).addBox(-5F, 5.7F, -5F, 10F, 2F, 10F, new CubeDeformation(-0.4F, -0.3F, -0.3F))
                .texOffs(213, 27).addBox(-5F, 9.5F, -5F, 10F, 7F, 10F, new CubeDeformation(-0.1F, -0.1F, 0F))
                .texOffs(108, 124).addBox(-4.4F, 13.3F, -6.2F, 2F, 3F, 2F, new CubeDeformation(0F, -0.3F, -0.3F))
                .texOffs(117, 124).addBox(-2.1F, 13.3F, -6.2F, 2F, 3F, 2F, new CubeDeformation(0F, -0.3F, -0.3F))
                .texOffs(126, 124).addBox(0.2F, 13.3F, -6.2F, 2F, 3F, 2F, new CubeDeformation(0F, -0.3F, -0.3F))
                .texOffs(135, 124).addBox(2.5F, 13.3F, -6.2F, 2F, 3F, 2F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(0F, 11.8F, 0.2F, -0.2443F, 0F, 0F));
        PartDefinition p_armor_right_arm = p_right_forearm.addOrReplaceChild("armor_right_arm", CubeListBuilder.create()
                .texOffs(43, 66).addBox(-5.7F, -3F, -5F, 11F, 5F, 10F, new CubeDeformation(-0.3F, 0F, 0F)),
                PartPose.offset(0F, 4F, 0F));
        PartDefinition p_armor_right_arm_0 = p_armor_right_arm.addOrReplaceChild("armor_right_arm_0", CubeListBuilder.create()
                .texOffs(220, 108).addBox(-1.5F, -2.988F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.012F, -0.4F)),
                PartPose.offsetAndRotation(-4.6F, -1F, -1.6F, 0F, 0F, -1.2217F));
        PartDefinition p_armor_right_arm_0_tip = p_armor_right_arm_0.addOrReplaceChild("armor_right_arm_0_tip", CubeListBuilder.create()
                .texOffs(144, 124).addBox(-1F, -2.652F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.318F, -0.348F, -0.318F)),
                PartPose.offsetAndRotation(0F, -2.676F, 0F, 0F, 0.7854F, 0F));
        PartDefinition p_armor_right_arm_1 = p_armor_right_arm.addOrReplaceChild("armor_right_arm_1", CubeListBuilder.create()
                .texOffs(153, 124).addBox(-1F, -2.616F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.1F, -0.384F, -0.1F)),
                PartPose.offsetAndRotation(-4.4F, 1F, 2.2F, 0.2443F, 0F, -1.3614F));
        PartDefinition p_armor_right_arm_1_tip = p_armor_right_arm_1.addOrReplaceChild("armor_right_arm_1_tip", CubeListBuilder.create()
                .texOffs(137, 130).addBox(-1F, -1.864F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.442F, -0.136F, -0.442F)),
                PartPose.offsetAndRotation(0F, -1.932F, 0F, 0F, 0.7854F, 0F));
        PartDefinition p_armor_right_arm_2 = p_armor_right_arm.addOrReplaceChild("armor_right_arm_2", CubeListBuilder.create()
                .texOffs(162, 124).addBox(-1F, -2.616F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.1F, -0.384F, -0.1F)),
                PartPose.offsetAndRotation(0F, -1.4F, 5F, -1.2217F, 0F, 0F));
        PartDefinition p_armor_right_arm_2_tip = p_armor_right_arm_2.addOrReplaceChild("armor_right_arm_2_tip", CubeListBuilder.create()
                .texOffs(146, 130).addBox(-1F, -1.864F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.442F, -0.136F, -0.442F)),
                PartPose.offsetAndRotation(0F, -1.932F, 0F, 0F, 0.7854F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
