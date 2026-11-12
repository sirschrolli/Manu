package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie der 3D-Teile getragener Rüstungen (seit 0.95): je Rüstungssatz und Platz ein Knochen <satz>_<platz>_<knochen>.
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class ArmorPartsGeometry {
    public static final int TEXTURE_WIDTH = 256;
    public static final int TEXTURE_HEIGHT = 384;

    private ArmorPartsGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_steel_head_head = root.addOrReplaceChild("steel_head_head", CubeListBuilder.create()
                .texOffs(120, 21).addBox(-0.5F, -10.75F, -5F, 1F, 2F, 10F, new CubeDeformation(0F, -0.15F, -0.4F))
                .texOffs(116, 249).addBox(-1F, -11.5F, 1.8F, 2F, 2F, 3F, new CubeDeformation(0F, -0.3F, -0.4F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_steel_plume_0 = p_steel_head_head.addOrReplaceChild("steel_plume_0", CubeListBuilder.create()
                .texOffs(30, 222).addBox(-1.5F, -2.8F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.2F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(0F, -10.6F, 3.3F, -0.2094F, 0F, 0F));
        PartDefinition p_steel_plume_1 = p_steel_plume_0.addOrReplaceChild("steel_plume_1", CubeListBuilder.create()
                .texOffs(211, 198).addBox(-1.5F, -3.6F, -1.5F, 3F, 4F, 3F, new CubeDeformation(0F, -0.4F, -0.1F)),
                PartPose.offsetAndRotation(0F, -2.35F, 0F, -0.4887F, 0F, 0F));
        PartDefinition p_steel_plume_2 = p_steel_plume_1.addOrReplaceChild("steel_plume_2", CubeListBuilder.create()
                .texOffs(224, 198).addBox(-1.5F, -3.8F, -1.5F, 3F, 4F, 3F, new CubeDeformation(-0.05F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(0F, -2.95F, 0F, -0.5585F, 0F, 0F));
        PartDefinition p_steel_plume_3 = p_steel_plume_2.addOrReplaceChild("steel_plume_3", CubeListBuilder.create()
                .texOffs(237, 198).addBox(-1.5F, -3.8F, -1.5F, 3F, 4F, 3F, new CubeDeformation(-0.25F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(0F, -3.35F, 0F, -0.5236F, 0F, 0F));
        PartDefinition p_steel_plume_4 = p_steel_plume_3.addOrReplaceChild("steel_plume_4", CubeListBuilder.create()
                .texOffs(125, 236).addBox(-1F, -3.7F, -1F, 2F, 4F, 2F, new CubeDeformation(0F, -0.3F, -0.1F)),
                PartPose.offsetAndRotation(0F, -3.35F, 0F, -0.384F, 0F, 0F));
        PartDefinition p_steel_plume_5 = p_steel_plume_4.addOrReplaceChild("steel_plume_5", CubeListBuilder.create()
                .texOffs(44, 255).addBox(-1F, -3F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.3F, 0F, -0.35F)),
                PartPose.offsetAndRotation(0F, -3.15F, 0F, -0.2443F, 0F, 0F));
        PartDefinition p_steel_chest_body = root.addOrReplaceChild("steel_chest_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_steel_chest_left_arm = root.addOrReplaceChild("steel_chest_left_arm", CubeListBuilder.create()
                .texOffs(0, 198).addBox(3.6F, 3.8F, -2F, 1F, 4F, 4F, new CubeDeformation(0F, -0.4F, -0.3F))
                .texOffs(0, 298).addBox(4.2F, 4.8F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.2F, -0.4F, -0.3F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_steel_pauldron_left_arm = p_steel_chest_left_arm.addOrReplaceChild("steel_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(82, 35).addBox(-3.8F, -2.15F, -4F, 8F, 3F, 8F, new CubeDeformation(-0.4F, -0.45F, -0.3F))
                .texOffs(123, 59).addBox(-2.5F, 0.1F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.2F, -0.1F, -0.2F))
                .texOffs(62, 104).addBox(-1.3F, 1.6F, -4F, 6F, 2F, 8F, new CubeDeformation(-0.2F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.2443F));
        PartDefinition p_steel_chest_right_arm = root.addOrReplaceChild("steel_chest_right_arm", CubeListBuilder.create()
                .texOffs(11, 198).addBox(-4.6F, 3.8F, -2F, 1F, 4F, 4F, new CubeDeformation(0F, -0.4F, -0.3F))
                .texOffs(7, 298).addBox(-5.2F, 4.8F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.2F, -0.4F, -0.3F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_steel_pauldron_right_arm = p_steel_chest_right_arm.addOrReplaceChild("steel_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(115, 35).addBox(-4.2F, -2.15F, -4F, 8F, 3F, 8F, new CubeDeformation(-0.4F, -0.45F, -0.3F))
                .texOffs(154, 59).addBox(-4.5F, 0.1F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.2F, -0.1F, -0.2F))
                .texOffs(91, 104).addBox(-4.7F, 1.6F, -4F, 6F, 2F, 8F, new CubeDeformation(-0.2F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.2443F));
        PartDefinition p_steel_legs_body = root.addOrReplaceChild("steel_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_steel_legs_left_leg = root.addOrReplaceChild("steel_legs_left_leg", CubeListBuilder.create()
                .texOffs(130, 273).addBox(-2F, 5F, -3.3F, 4F, 3F, 1F, new CubeDeformation(0F, -0.1F, 0F))
                .texOffs(53, 255).addBox(2F, 5.5F, -3.2F, 1F, 2F, 3F, new CubeDeformation(-0.1F, 0F, -0.4F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_steel_tasset_left_leg = p_steel_legs_left_leg.addOrReplaceChild("steel_tasset_left_leg", CubeListBuilder.create()
                .texOffs(166, 215).addBox(-3F, -0.1F, -0.6F, 6F, 5F, 1F, new CubeDeformation(-0.2F, -0.1F, 0F)),
                PartPose.offsetAndRotation(0F, -0.8F, -2.8F, -0.1222F, 0F, 0F));
        PartDefinition p_steel_legs_right_leg = root.addOrReplaceChild("steel_legs_right_leg", CubeListBuilder.create()
                .texOffs(141, 273).addBox(-2F, 5F, -3.3F, 4F, 3F, 1F, new CubeDeformation(0F, -0.1F, 0F))
                .texOffs(62, 255).addBox(-3F, 5.5F, -3.2F, 1F, 2F, 3F, new CubeDeformation(-0.1F, 0F, -0.4F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_steel_tasset_right_leg = p_steel_legs_right_leg.addOrReplaceChild("steel_tasset_right_leg", CubeListBuilder.create()
                .texOffs(181, 215).addBox(-3F, -0.1F, -0.6F, 6F, 5F, 1F, new CubeDeformation(-0.2F, -0.1F, 0F)),
                PartPose.offsetAndRotation(0F, -0.8F, -2.8F, -0.1222F, 0F, 0F));
        PartDefinition p_steel_feet_left_leg = root.addOrReplaceChild("steel_feet_left_leg", CubeListBuilder.create()
                .texOffs(69, 243).addBox(-2.5F, 10.2F, -4.45F, 5F, 3F, 2F, new CubeDeformation(0F, -0.3F, -0.25F))
                .texOffs(196, 303).addBox(-0.5F, 10.45F, 2.75F, 1F, 1F, 2F, new CubeDeformation(-0.15F, -0.15F, -0.05F))
                .texOffs(203, 303).addBox(-1F, 9.95F, 4.25F, 2F, 2F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_steel_feet_right_leg = root.addOrReplaceChild("steel_feet_right_leg", CubeListBuilder.create()
                .texOffs(84, 243).addBox(-2.5F, 10.2F, -4.45F, 5F, 3F, 2F, new CubeDeformation(0F, -0.3F, -0.25F))
                .texOffs(210, 303).addBox(-0.5F, 10.45F, 2.75F, 1F, 1F, 2F, new CubeDeformation(-0.15F, -0.15F, -0.05F))
                .texOffs(217, 303).addBox(-1F, 9.95F, 4.25F, 2F, 2F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_konterit_head_head = root.addOrReplaceChild("konterit_head_head", CubeListBuilder.create()
                .texOffs(224, 303).addBox(-1F, -6.8F, -5.75F, 2F, 2F, 1F, new CubeDeformation(0F, 0F, -0.15F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_kon_wing_left = p_konterit_head_head.addOrReplaceChild("kon_wing_left", CubeListBuilder.create()
                .texOffs(130, 207).addBox(-1F, -2.58F, -1.79F, 2F, 4F, 3F, new CubeDeformation(-0.4F, -0.26F, -0.05F)),
                PartPose.offsetAndRotation(5.2F, -5.2F, -0.8F, 0.2793F, 0.2793F, 0F));
        PartDefinition p_kon_wing_left_feather_0 = p_kon_wing_left.addOrReplaceChild("kon_wing_left_feather_0", CubeListBuilder.create()
                .texOffs(221, 169).addBox(-0.5F, -1F, -0.165F, 1F, 2F, 7F, new CubeDeformation(-0.25F, -0.0575F, -0.165F)),
                PartPose.offsetAndRotation(0F, -0.58F, 0.58F, 0.0698F, 0F, 0F));
        PartDefinition p_kon_wing_left_feather_1 = p_kon_wing_left.addOrReplaceChild("kon_wing_left_feather_1", CubeListBuilder.create()
                .texOffs(191, 47).addBox(-0.5F, -1F, -0.44F, 1F, 2F, 9F, new CubeDeformation(-0.25F, -0.0575F, -0.44F)),
                PartPose.offsetAndRotation(0F, -0.9425F, 0.58F, 0.3491F, 0F, 0F));
        PartDefinition p_kon_wing_left_feather_2 = p_kon_wing_left.addOrReplaceChild("kon_wing_left_feather_2", CubeListBuilder.create()
                .texOffs(212, 47).addBox(-0.5F, -1F, -0.295F, 1F, 2F, 9F, new CubeDeformation(-0.25F, -0.13F, -0.295F)),
                PartPose.offsetAndRotation(0F, -1.305F, 0.58F, 0.6283F, 0F, 0F));
        PartDefinition p_kon_wing_left_feather_3 = p_kon_wing_left.addOrReplaceChild("kon_wing_left_feather_3", CubeListBuilder.create()
                .texOffs(185, 137).addBox(-0.5F, -1F, -0.375F, 1F, 2F, 8F, new CubeDeformation(-0.25F, -0.2025F, -0.375F)),
                PartPose.offsetAndRotation(0F, -1.6675F, 0.58F, 0.9076F, 0F, 0F));
        PartDefinition p_kon_wing_left_feather_4 = p_kon_wing_left.addOrReplaceChild("kon_wing_left_feather_4", CubeListBuilder.create()
                .texOffs(147, 189).addBox(-0.5F, -1F, -0.245F, 1F, 2F, 6F, new CubeDeformation(-0.25F, -0.275F, -0.245F)),
                PartPose.offsetAndRotation(0F, -2.03F, 0.58F, 1.1868F, 0F, 0F));
        PartDefinition p_kon_wing_right = p_konterit_head_head.addOrReplaceChild("kon_wing_right", CubeListBuilder.create()
                .texOffs(141, 207).addBox(-1F, -2.58F, -1.79F, 2F, 4F, 3F, new CubeDeformation(-0.4F, -0.26F, -0.05F)),
                PartPose.offsetAndRotation(-5.2F, -5.2F, -0.8F, 0.2793F, -0.2793F, 0F));
        PartDefinition p_kon_wing_right_feather_0 = p_kon_wing_right.addOrReplaceChild("kon_wing_right_feather_0", CubeListBuilder.create()
                .texOffs(238, 169).addBox(-0.5F, -1F, -0.165F, 1F, 2F, 7F, new CubeDeformation(-0.25F, -0.0575F, -0.165F)),
                PartPose.offsetAndRotation(0F, -0.58F, 0.58F, 0.0698F, 0F, 0F));
        PartDefinition p_kon_wing_right_feather_1 = p_kon_wing_right.addOrReplaceChild("kon_wing_right_feather_1", CubeListBuilder.create()
                .texOffs(233, 47).addBox(-0.5F, -1F, -0.44F, 1F, 2F, 9F, new CubeDeformation(-0.25F, -0.0575F, -0.44F)),
                PartPose.offsetAndRotation(0F, -0.9425F, 0.58F, 0.3491F, 0F, 0F));
        PartDefinition p_kon_wing_right_feather_2 = p_kon_wing_right.addOrReplaceChild("kon_wing_right_feather_2", CubeListBuilder.create()
                .texOffs(0, 59).addBox(-0.5F, -1F, -0.295F, 1F, 2F, 9F, new CubeDeformation(-0.25F, -0.13F, -0.295F)),
                PartPose.offsetAndRotation(0F, -1.305F, 0.58F, 0.6283F, 0F, 0F));
        PartDefinition p_kon_wing_right_feather_3 = p_kon_wing_right.addOrReplaceChild("kon_wing_right_feather_3", CubeListBuilder.create()
                .texOffs(204, 137).addBox(-0.5F, -1F, -0.375F, 1F, 2F, 8F, new CubeDeformation(-0.25F, -0.2025F, -0.375F)),
                PartPose.offsetAndRotation(0F, -1.6675F, 0.58F, 0.9076F, 0F, 0F));
        PartDefinition p_kon_wing_right_feather_4 = p_kon_wing_right.addOrReplaceChild("kon_wing_right_feather_4", CubeListBuilder.create()
                .texOffs(162, 189).addBox(-0.5F, -1F, -0.245F, 1F, 2F, 6F, new CubeDeformation(-0.25F, -0.275F, -0.245F)),
                PartPose.offsetAndRotation(0F, -2.03F, 0.58F, 1.1868F, 0F, 0F));
        PartDefinition p_konterit_chest_body = root.addOrReplaceChild("konterit_chest_body", CubeListBuilder.create()
                .texOffs(231, 303).addBox(-5.2F, -1.1F, -3.65F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.3F, -0.15F))
                .texOffs(238, 303).addBox(3.2F, -1.1F, -3.65F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.3F, -0.15F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_kon_cape = p_konterit_chest_body.addOrReplaceChild("kon_cape", CubeListBuilder.create()
                .texOffs(50, 189).addBox(-4.5F, -0.2F, -0.2F, 9F, 7F, 1F, new CubeDeformation(0F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(0F, 0.2F, 3F, 0.1047F, 0F, 0F));
        PartDefinition p_kon_cape_hem = p_kon_cape.addOrReplaceChild("kon_cape_hem", CubeListBuilder.create()
                .texOffs(173, 198).addBox(-4.5F, -0.4F, -0.2F, 9F, 6F, 1F, new CubeDeformation(-0.2F, -0.4F, -0.2F)),
                PartPose.offsetAndRotation(0F, 6.4F, 0.05F, 0.1396F, 0F, 0F));
        PartDefinition p_konterit_chest_left_arm = root.addOrReplaceChild("konterit_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_kon_pauldron_left_arm = p_konterit_chest_left_arm.addOrReplaceChild("kon_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(52, 148).addBox(-3.2F, -1.55F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.2F, -0.25F, 0F))
                .texOffs(92, 189).addBox(-1F, -2.05F, -3.5F, 2F, 1F, 7F, new CubeDeformation(-0.4F, -0.15F, 0F))
                .texOffs(70, 303).addBox(1F, -2.2F, -1F, 2F, 1F, 2F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offsetAndRotation(1F, -3.2F, 0F, 0F, 0F, 0.2094F));
        PartDefinition p_konterit_chest_right_arm = root.addOrReplaceChild("konterit_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_kon_pauldron_right_arm = p_konterit_chest_right_arm.addOrReplaceChild("kon_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(81, 148).addBox(-3.8F, -1.55F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.2F, -0.25F, 0F))
                .texOffs(111, 189).addBox(-1F, -2.05F, -3.5F, 2F, 1F, 7F, new CubeDeformation(-0.4F, -0.15F, 0F))
                .texOffs(79, 303).addBox(-3F, -2.2F, -1F, 2F, 1F, 2F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offsetAndRotation(-1F, -3.2F, 0F, 0F, 0F, -0.2094F));
        PartDefinition p_konterit_legs_body = root.addOrReplaceChild("konterit_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_konterit_legs_left_leg = root.addOrReplaceChild("konterit_legs_left_leg", CubeListBuilder.create()
                .texOffs(71, 255).addBox(-1.5F, 4.2F, -3.1F, 3F, 4F, 1F, new CubeDeformation(0F, -0.4F, -0.1F))
                .texOffs(245, 303).addBox(-1F, 5.2F, -3.65F, 2F, 2F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_konterit_legs_right_leg = root.addOrReplaceChild("konterit_legs_right_leg", CubeListBuilder.create()
                .texOffs(80, 255).addBox(-1.5F, 4.2F, -3.1F, 3F, 4F, 1F, new CubeDeformation(0F, -0.4F, -0.1F))
                .texOffs(0, 307).addBox(-1F, 5.2F, -3.65F, 2F, 2F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_konterit_feet_left_leg = root.addOrReplaceChild("konterit_feet_left_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_kon_heel_left_leg = p_konterit_feet_left_leg.addOrReplaceChild("kon_heel_left_leg", CubeListBuilder.create()
                .texOffs(89, 255).addBox(-1F, -1.84F, -1.17F, 2F, 3F, 2F, new CubeDeformation(-0.4F, -0.48F, -0.15F)),
                PartPose.offsetAndRotation(2.9F, 9.6F, 0.2F, 0.4189F, 0.2443F, 0F));
        PartDefinition p_kon_heel_left_leg_feather_0 = p_kon_heel_left_leg.addOrReplaceChild("kon_heel_left_leg_feather_0", CubeListBuilder.create()
                .texOffs(26, 236).addBox(-0.5F, -1F, -0.045F, 1F, 2F, 4F, new CubeDeformation(-0.25F, -0.4475F, -0.045F)),
                PartPose.offsetAndRotation(0F, -0.34F, 0.34F, 0.0698F, 0F, 0F));
        PartDefinition p_kon_heel_left_leg_feather_1 = p_kon_heel_left_leg.addOrReplaceChild("kon_heel_left_leg_feather_1", CubeListBuilder.create()
                .texOffs(0, 207).addBox(-0.5F, -1F, -0.12F, 1F, 2F, 5F, new CubeDeformation(-0.25F, -0.4475F, -0.12F)),
                PartPose.offsetAndRotation(0F, -0.5525F, 0.34F, 0.3491F, 0F, 0F));
        PartDefinition p_kon_heel_left_leg_feather_2 = p_kon_heel_left_leg.addOrReplaceChild("kon_heel_left_leg_feather_2", CubeListBuilder.create()
                .texOffs(13, 207).addBox(-0.5F, -1F, -0.035F, 1F, 2F, 5F, new CubeDeformation(-0.25F, -0.49F, -0.035F)),
                PartPose.offsetAndRotation(0F, -0.765F, 0.34F, 0.6283F, 0F, 0F));
        PartDefinition p_kon_heel_left_leg_feather_3 = p_kon_heel_left_leg.addOrReplaceChild("kon_heel_left_leg_feather_3", CubeListBuilder.create()
                .texOffs(43, 222).addBox(-0.5F, -0.5F, -0.375F, 1F, 1F, 5F, new CubeDeformation(-0.25F, -0.0325F, -0.375F)),
                PartPose.offsetAndRotation(0F, -0.9775F, 0.34F, 0.9076F, 0F, 0F));
        PartDefinition p_kon_heel_left_leg_feather_4 = p_kon_heel_left_leg.addOrReplaceChild("kon_heel_left_leg_feather_4", CubeListBuilder.create()
                .texOffs(127, 249).addBox(-0.5F, -0.5F, -0.385F, 1F, 1F, 4F, new CubeDeformation(-0.25F, -0.075F, -0.385F)),
                PartPose.offsetAndRotation(0F, -1.19F, 0.34F, 1.1868F, 0F, 0F));
        PartDefinition p_konterit_feet_right_leg = root.addOrReplaceChild("konterit_feet_right_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_kon_heel_right_leg = p_konterit_feet_right_leg.addOrReplaceChild("kon_heel_right_leg", CubeListBuilder.create()
                .texOffs(98, 255).addBox(-1F, -1.84F, -1.17F, 2F, 3F, 2F, new CubeDeformation(-0.4F, -0.48F, -0.15F)),
                PartPose.offsetAndRotation(-2.9F, 9.6F, 0.2F, 0.4189F, -0.2443F, 0F));
        PartDefinition p_kon_heel_right_leg_feather_0 = p_kon_heel_right_leg.addOrReplaceChild("kon_heel_right_leg_feather_0", CubeListBuilder.create()
                .texOffs(37, 236).addBox(-0.5F, -1F, -0.045F, 1F, 2F, 4F, new CubeDeformation(-0.25F, -0.4475F, -0.045F)),
                PartPose.offsetAndRotation(0F, -0.34F, 0.34F, 0.0698F, 0F, 0F));
        PartDefinition p_kon_heel_right_leg_feather_1 = p_kon_heel_right_leg.addOrReplaceChild("kon_heel_right_leg_feather_1", CubeListBuilder.create()
                .texOffs(26, 207).addBox(-0.5F, -1F, -0.12F, 1F, 2F, 5F, new CubeDeformation(-0.25F, -0.4475F, -0.12F)),
                PartPose.offsetAndRotation(0F, -0.5525F, 0.34F, 0.3491F, 0F, 0F));
        PartDefinition p_kon_heel_right_leg_feather_2 = p_kon_heel_right_leg.addOrReplaceChild("kon_heel_right_leg_feather_2", CubeListBuilder.create()
                .texOffs(39, 207).addBox(-0.5F, -1F, -0.035F, 1F, 2F, 5F, new CubeDeformation(-0.25F, -0.49F, -0.035F)),
                PartPose.offsetAndRotation(0F, -0.765F, 0.34F, 0.6283F, 0F, 0F));
        PartDefinition p_kon_heel_right_leg_feather_3 = p_kon_heel_right_leg.addOrReplaceChild("kon_heel_right_leg_feather_3", CubeListBuilder.create()
                .texOffs(56, 222).addBox(-0.5F, -0.5F, -0.375F, 1F, 1F, 5F, new CubeDeformation(-0.25F, -0.0325F, -0.375F)),
                PartPose.offsetAndRotation(0F, -0.9775F, 0.34F, 0.9076F, 0F, 0F));
        PartDefinition p_kon_heel_right_leg_feather_4 = p_kon_heel_right_leg.addOrReplaceChild("kon_heel_right_leg_feather_4", CubeListBuilder.create()
                .texOffs(138, 249).addBox(-0.5F, -0.5F, -0.385F, 1F, 1F, 4F, new CubeDeformation(-0.25F, -0.075F, -0.385F)),
                PartPose.offsetAndRotation(0F, -1.19F, 0.34F, 1.1868F, 0F, 0F));
        PartDefinition p_nightsteel_head_head = root.addOrReplaceChild("nightsteel_head_head", CubeListBuilder.create()
                .texOffs(169, 298).addBox(-5.5F, -9.5F, -3.9F, 11F, 1F, 2F, new CubeDeformation(-0.2F, 0F, -0.4F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_night_crown_0_0 = p_nightsteel_head_head.addOrReplaceChild("night_crown_0_0", CubeListBuilder.create()
                .texOffs(198, 278).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.25F, -0.285F, -0.25F)),
                PartPose.offsetAndRotation(-3.6F, -9.3F, -2.9F, -0.1396F, 0F, -0.377F));
        PartDefinition p_night_crown_0_1 = p_night_crown_0_0.addOrReplaceChild("night_crown_0_1", CubeListBuilder.create()
                .texOffs(140, 315).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.0875F, -0.35F, -0.0875F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_night_crown_1_0 = p_nightsteel_head_head.addOrReplaceChild("night_crown_1_0", CubeListBuilder.create()
                .texOffs(107, 255).addBox(-1F, -2.545F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.25F, -0.455F, -0.25F)),
                PartPose.offsetAndRotation(-1.8F, -9.3F, -2.9F, -0.1396F, 0F, -0.1885F));
        PartDefinition p_night_crown_1_1 = p_night_crown_1_0.addOrReplaceChild("night_crown_1_1", CubeListBuilder.create()
                .texOffs(145, 315).addBox(-0.5F, -1.95F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.0875F, -0.05F, -0.0875F)),
                PartPose.offset(0F, -1.84F, 0F));
        PartDefinition p_night_crown_2_0 = p_nightsteel_head_head.addOrReplaceChild("night_crown_2_0", CubeListBuilder.create()
                .texOffs(116, 255).addBox(-1F, -2.93F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.1F, -0.07F, -0.1F)),
                PartPose.offsetAndRotation(0F, -9.3F, -2.9F, -0.1396F, 0F, 0F));
        PartDefinition p_night_crown_2_1 = p_night_crown_2_0.addOrReplaceChild("night_crown_2_1", CubeListBuilder.create()
                .texOffs(84, 298).addBox(-0.5F, -2.8F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.005F, -0.2F, -0.005F)),
                PartPose.offset(0F, -2.61F, 0F));
        PartDefinition p_night_crown_3_0 = p_nightsteel_head_head.addOrReplaceChild("night_crown_3_0", CubeListBuilder.create()
                .texOffs(125, 255).addBox(-1F, -2.545F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.25F, -0.455F, -0.25F)),
                PartPose.offsetAndRotation(1.8F, -9.3F, -2.9F, -0.1396F, 0F, 0.1885F));
        PartDefinition p_night_crown_3_1 = p_night_crown_3_0.addOrReplaceChild("night_crown_3_1", CubeListBuilder.create()
                .texOffs(150, 315).addBox(-0.5F, -1.95F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.0875F, -0.05F, -0.0875F)),
                PartPose.offset(0F, -1.84F, 0F));
        PartDefinition p_night_crown_4_0 = p_nightsteel_head_head.addOrReplaceChild("night_crown_4_0", CubeListBuilder.create()
                .texOffs(207, 278).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.25F, -0.285F, -0.25F)),
                PartPose.offsetAndRotation(3.6F, -9.3F, -2.9F, -0.1396F, 0F, 0.377F));
        PartDefinition p_night_crown_4_1 = p_night_crown_4_0.addOrReplaceChild("night_crown_4_1", CubeListBuilder.create()
                .texOffs(155, 315).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.0875F, -0.35F, -0.0875F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_nightsteel_chest_body = root.addOrReplaceChild("nightsteel_chest_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_night_back_0_0 = p_nightsteel_chest_body.addOrReplaceChild("night_back_0_0", CubeListBuilder.create()
                .texOffs(134, 255).addBox(-1F, -2.6F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.2F, -0.4F, -0.2F)),
                PartPose.offsetAndRotation(0F, 1.6F, 2.9F, -0.9076F, 0F, 0F));
        PartDefinition p_night_back_0_1 = p_night_back_0_0.addOrReplaceChild("night_back_0_1", CubeListBuilder.create()
                .texOffs(160, 315).addBox(-0.5F, -2F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.06F, 0F, -0.06F)),
                PartPose.offset(0F, -1.95F, 0F));
        PartDefinition p_night_back_1_0 = p_nightsteel_chest_body.addOrReplaceChild("night_back_1_0", CubeListBuilder.create()
                .texOffs(216, 278).addBox(-1F, -1.88F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.2F, -0.12F, -0.2F)),
                PartPose.offsetAndRotation(0F, 4.6F, 2.9F, -0.9076F, 0F, 0F));
        PartDefinition p_night_back_1_1 = p_night_back_1_0.addOrReplaceChild("night_back_1_1", CubeListBuilder.create()
                .texOffs(165, 315).addBox(-0.5F, -1.8F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.06F, -0.2F, -0.06F)),
                PartPose.offset(0F, -1.51F, 0F));
        PartDefinition p_night_back_2_0 = p_nightsteel_chest_body.addOrReplaceChild("night_back_2_0", CubeListBuilder.create()
                .texOffs(225, 278).addBox(-1F, -1.66F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.2F, -0.34F, -0.2F)),
                PartPose.offsetAndRotation(0F, 7.6F, 2.9F, -0.9076F, 0F, 0F));
        PartDefinition p_night_back_2_1 = p_night_back_2_0.addOrReplaceChild("night_back_2_1", CubeListBuilder.create()
                .texOffs(170, 315).addBox(-0.5F, -1.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.06F, -0.4F, -0.06F)),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_nightsteel_chest_left_arm = root.addOrReplaceChild("nightsteel_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_night_pauldron_left_arm = p_nightsteel_chest_left_arm.addOrReplaceChild("night_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(185, 59).addBox(-3.3F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.1745F));
        PartDefinition p_night_shoulder_left_arm_0_0 = p_night_pauldron_left_arm.addOrReplaceChild("night_shoulder_left_arm_0_0", CubeListBuilder.create()
                .texOffs(234, 278).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.285F, -0.3F)),
                PartPose.offsetAndRotation(2.2F, -1.4F, -2.2F, 0F, 0F, 0.4887F));
        PartDefinition p_night_shoulder_left_arm_0_1 = p_night_shoulder_left_arm_0_0.addOrReplaceChild("night_shoulder_left_arm_0_1", CubeListBuilder.create()
                .texOffs(175, 315).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.35F, -0.115F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_night_shoulder_left_arm_1_0 = p_night_pauldron_left_arm.addOrReplaceChild("night_shoulder_left_arm_1_0", CubeListBuilder.create()
                .texOffs(243, 278).addBox(-1F, -1.99F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.01F, -0.3F)),
                PartPose.offsetAndRotation(2.2F, -1.4F, 0F, 0F, 0F, 0.4887F));
        PartDefinition p_night_shoulder_left_arm_1_1 = p_night_shoulder_left_arm_1_0.addOrReplaceChild("night_shoulder_left_arm_1_1", CubeListBuilder.create()
                .texOffs(180, 315).addBox(-0.5F, -1.9F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.1F, -0.115F)),
                PartPose.offset(0F, -1.73F, 0F));
        PartDefinition p_night_shoulder_left_arm_2_0 = p_night_pauldron_left_arm.addOrReplaceChild("night_shoulder_left_arm_2_0", CubeListBuilder.create()
                .texOffs(0, 283).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.285F, -0.3F)),
                PartPose.offsetAndRotation(2.2F, -1.4F, 2.2F, 0F, 0F, 0.4887F));
        PartDefinition p_night_shoulder_left_arm_2_1 = p_night_shoulder_left_arm_2_0.addOrReplaceChild("night_shoulder_left_arm_2_1", CubeListBuilder.create()
                .texOffs(185, 315).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.35F, -0.115F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_nightsteel_chest_right_arm = root.addOrReplaceChild("nightsteel_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_night_pauldron_right_arm = p_nightsteel_chest_right_arm.addOrReplaceChild("night_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(216, 59).addBox(-3.7F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.1745F));
        PartDefinition p_night_shoulder_right_arm_0_0 = p_night_pauldron_right_arm.addOrReplaceChild("night_shoulder_right_arm_0_0", CubeListBuilder.create()
                .texOffs(9, 283).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.285F, -0.3F)),
                PartPose.offsetAndRotation(-2.2F, -1.4F, -2.2F, 0F, 0F, -0.4887F));
        PartDefinition p_night_shoulder_right_arm_0_1 = p_night_shoulder_right_arm_0_0.addOrReplaceChild("night_shoulder_right_arm_0_1", CubeListBuilder.create()
                .texOffs(190, 315).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.35F, -0.115F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_night_shoulder_right_arm_1_0 = p_night_pauldron_right_arm.addOrReplaceChild("night_shoulder_right_arm_1_0", CubeListBuilder.create()
                .texOffs(18, 283).addBox(-1F, -1.99F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.01F, -0.3F)),
                PartPose.offsetAndRotation(-2.2F, -1.4F, 0F, 0F, 0F, -0.4887F));
        PartDefinition p_night_shoulder_right_arm_1_1 = p_night_shoulder_right_arm_1_0.addOrReplaceChild("night_shoulder_right_arm_1_1", CubeListBuilder.create()
                .texOffs(195, 315).addBox(-0.5F, -1.9F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.1F, -0.115F)),
                PartPose.offset(0F, -1.73F, 0F));
        PartDefinition p_night_shoulder_right_arm_2_0 = p_night_pauldron_right_arm.addOrReplaceChild("night_shoulder_right_arm_2_0", CubeListBuilder.create()
                .texOffs(27, 283).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.285F, -0.3F)),
                PartPose.offsetAndRotation(-2.2F, -1.4F, 2.2F, 0F, 0F, -0.4887F));
        PartDefinition p_night_shoulder_right_arm_2_1 = p_night_shoulder_right_arm_2_0.addOrReplaceChild("night_shoulder_right_arm_2_1", CubeListBuilder.create()
                .texOffs(200, 315).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.35F, -0.115F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_nightsteel_legs_body = root.addOrReplaceChild("nightsteel_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_nightsteel_legs_left_leg = root.addOrReplaceChild("nightsteel_legs_left_leg", CubeListBuilder.create()
                .texOffs(152, 273).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(-0.1F, 0F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_night_knee_left_leg_0 = p_nightsteel_legs_left_leg.addOrReplaceChild("night_knee_left_leg_0", CubeListBuilder.create()
                .texOffs(36, 283).addBox(-1F, -1.77F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.23F, -0.35F)),
                PartPose.offsetAndRotation(0F, 6F, -3F, 1.0821F, 0F, 0F));
        PartDefinition p_night_knee_left_leg_1 = p_night_knee_left_leg_0.addOrReplaceChild("night_knee_left_leg_1", CubeListBuilder.create()
                .texOffs(205, 315).addBox(-0.5F, -1.7F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.3F, -0.1425F)),
                PartPose.offset(0F, -1.29F, 0F));
        PartDefinition p_nightsteel_legs_right_leg = root.addOrReplaceChild("nightsteel_legs_right_leg", CubeListBuilder.create()
                .texOffs(163, 273).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(-0.1F, 0F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_night_knee_right_leg_0 = p_nightsteel_legs_right_leg.addOrReplaceChild("night_knee_right_leg_0", CubeListBuilder.create()
                .texOffs(45, 283).addBox(-1F, -1.77F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.23F, -0.35F)),
                PartPose.offsetAndRotation(0F, 6F, -3F, 1.0821F, 0F, 0F));
        PartDefinition p_night_knee_right_leg_1 = p_night_knee_right_leg_0.addOrReplaceChild("night_knee_right_leg_1", CubeListBuilder.create()
                .texOffs(210, 315).addBox(-0.5F, -1.7F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.3F, -0.1425F)),
                PartPose.offset(0F, -1.29F, 0F));
        PartDefinition p_nightsteel_feet_left_leg = root.addOrReplaceChild("nightsteel_feet_left_leg", CubeListBuilder.create()
                .texOffs(150, 267).addBox(-2.5F, 10.25F, -3.9F, 5F, 3F, 1F, new CubeDeformation(-0.1F, -0.45F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_night_heel_left_leg_0 = p_nightsteel_feet_left_leg.addOrReplaceChild("night_heel_left_leg_0", CubeListBuilder.create()
                .texOffs(54, 283).addBox(-1F, -1.66F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.34F, -0.4F)),
                PartPose.offsetAndRotation(0F, 11F, 2.8F, -1.0123F, 0F, 0F));
        PartDefinition p_night_heel_left_leg_1 = p_night_heel_left_leg_0.addOrReplaceChild("night_heel_left_leg_1", CubeListBuilder.create()
                .texOffs(215, 315).addBox(-0.5F, -1.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.17F, -0.4F, -0.17F)),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_nightsteel_feet_right_leg = root.addOrReplaceChild("nightsteel_feet_right_leg", CubeListBuilder.create()
                .texOffs(163, 267).addBox(-2.5F, 10.25F, -3.9F, 5F, 3F, 1F, new CubeDeformation(-0.1F, -0.45F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_night_heel_right_leg_0 = p_nightsteel_feet_right_leg.addOrReplaceChild("night_heel_right_leg_0", CubeListBuilder.create()
                .texOffs(63, 283).addBox(-1F, -1.66F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.34F, -0.4F)),
                PartPose.offsetAndRotation(0F, 11F, 2.8F, -1.0123F, 0F, 0F));
        PartDefinition p_night_heel_right_leg_1 = p_night_heel_right_leg_0.addOrReplaceChild("night_heel_right_leg_1", CubeListBuilder.create()
                .texOffs(220, 315).addBox(-0.5F, -1.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.17F, -0.4F, -0.17F)),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_abyssite_head_head = root.addOrReplaceChild("abyssite_head_head", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_aby_horn_left_0 = p_abyssite_head_head.addOrReplaceChild("aby_horn_left_0", CubeListBuilder.create()
                .texOffs(69, 222).addBox(-1.5F, -3F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offsetAndRotation(4F, -8.6F, -1.2F, -0.2094F, 0F, 0.5934F));
        PartDefinition p_aby_horn_left_1 = p_aby_horn_left_0.addOrReplaceChild("aby_horn_left_1", CubeListBuilder.create()
                .texOffs(82, 222).addBox(-1.5F, -3F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.45F, 0F, -0.45F)),
                PartPose.offsetAndRotation(0F, -2.75F, 0F, -0.4887F, 0F, -0.1745F));
        PartDefinition p_aby_horn_left_2 = p_aby_horn_left_1.addOrReplaceChild("aby_horn_left_2", CubeListBuilder.create()
                .texOffs(143, 255).addBox(-1F, -2.9F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.2F, -0.1F, -0.2F)),
                PartPose.offsetAndRotation(0F, -2.75F, 0F, -0.5934F, 0F, -0.2094F));
        PartDefinition p_aby_horn_left_3 = p_aby_horn_left_2.addOrReplaceChild("aby_horn_left_3", CubeListBuilder.create()
                .texOffs(152, 255).addBox(-1F, -2.7F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.45F, -0.3F, -0.45F)),
                PartPose.offsetAndRotation(0F, -2.55F, 0F, -0.6283F, 0F, -0.1047F));
        PartDefinition p_aby_horn_left_4 = p_aby_horn_left_3.addOrReplaceChild("aby_horn_left_4", CubeListBuilder.create()
                .texOffs(225, 315).addBox(-0.5F, -1.9F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.15F, -0.1F, -0.15F)),
                PartPose.offsetAndRotation(0F, -2.15F, 0F, -0.5236F, 0F, 0F));
        PartDefinition p_aby_horn_right_0 = p_abyssite_head_head.addOrReplaceChild("aby_horn_right_0", CubeListBuilder.create()
                .texOffs(95, 222).addBox(-1.5F, -3F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offsetAndRotation(-4F, -8.6F, -1.2F, -0.2094F, 0F, -0.5934F));
        PartDefinition p_aby_horn_right_1 = p_aby_horn_right_0.addOrReplaceChild("aby_horn_right_1", CubeListBuilder.create()
                .texOffs(108, 222).addBox(-1.5F, -3F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.45F, 0F, -0.45F)),
                PartPose.offsetAndRotation(0F, -2.75F, 0F, -0.4887F, 0F, 0.1745F));
        PartDefinition p_aby_horn_right_2 = p_aby_horn_right_1.addOrReplaceChild("aby_horn_right_2", CubeListBuilder.create()
                .texOffs(161, 255).addBox(-1F, -2.9F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.2F, -0.1F, -0.2F)),
                PartPose.offsetAndRotation(0F, -2.75F, 0F, -0.5934F, 0F, 0.2094F));
        PartDefinition p_aby_horn_right_3 = p_aby_horn_right_2.addOrReplaceChild("aby_horn_right_3", CubeListBuilder.create()
                .texOffs(170, 255).addBox(-1F, -2.7F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.45F, -0.3F, -0.45F)),
                PartPose.offsetAndRotation(0F, -2.55F, 0F, -0.6283F, 0F, 0.1047F));
        PartDefinition p_aby_horn_right_4 = p_aby_horn_right_3.addOrReplaceChild("aby_horn_right_4", CubeListBuilder.create()
                .texOffs(230, 315).addBox(-0.5F, -1.9F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.15F, -0.1F, -0.15F)),
                PartPose.offsetAndRotation(0F, -2.15F, 0F, -0.5236F, 0F, 0F));
        PartDefinition p_aby_brow_0 = p_abyssite_head_head.addOrReplaceChild("aby_brow_0", CubeListBuilder.create()
                .texOffs(72, 283).addBox(-1F, -1.88F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.2F, -0.12F, -0.2F)),
                PartPose.offsetAndRotation(0F, -6.2F, -5.2F, -0.2443F, 0F, 0F));
        PartDefinition p_aby_brow_1 = p_aby_brow_0.addOrReplaceChild("aby_brow_1", CubeListBuilder.create()
                .texOffs(235, 315).addBox(-0.5F, -1.8F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.06F, -0.2F, -0.06F)),
                PartPose.offset(0F, -1.51F, 0F));
        PartDefinition p_abyssite_chest_body = root.addOrReplaceChild("abyssite_chest_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_aby_back_0_0 = p_abyssite_chest_body.addOrReplaceChild("aby_back_0_0", CubeListBuilder.create()
                .texOffs(179, 255).addBox(-1F, -2.82F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.1F, -0.18F, -0.1F)),
                PartPose.offsetAndRotation(0F, 3F, 2.9F, -0.8029F, 0F, 0F));
        PartDefinition p_aby_back_0_1 = p_aby_back_0_0.addOrReplaceChild("aby_back_0_1", CubeListBuilder.create()
                .texOffs(89, 298).addBox(-0.5F, -2.7F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.005F, -0.3F, -0.005F)),
                PartPose.offset(0F, -2.39F, 0F));
        PartDefinition p_aby_back_1_0 = p_abyssite_chest_body.addOrReplaceChild("aby_back_1_0", CubeListBuilder.create()
                .texOffs(81, 283).addBox(-1F, -1.935F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.065F, -0.35F)),
                PartPose.offsetAndRotation(1.7F, 3.8F, 2.9F, -0.6981F, 0F, 0.384F));
        PartDefinition p_aby_back_1_1 = p_aby_back_1_0.addOrReplaceChild("aby_back_1_1", CubeListBuilder.create()
                .texOffs(240, 315).addBox(-0.5F, -1.85F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.15F, -0.1425F)),
                PartPose.offset(0F, -1.62F, 0F));
        PartDefinition p_aby_back_2_0 = p_abyssite_chest_body.addOrReplaceChild("aby_back_2_0", CubeListBuilder.create()
                .texOffs(90, 283).addBox(-1F, -1.935F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.065F, -0.35F)),
                PartPose.offsetAndRotation(-1.7F, 3.8F, 2.9F, -0.6981F, 0F, -0.384F));
        PartDefinition p_aby_back_2_1 = p_aby_back_2_0.addOrReplaceChild("aby_back_2_1", CubeListBuilder.create()
                .texOffs(245, 315).addBox(-0.5F, -1.85F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.15F, -0.1425F)),
                PartPose.offset(0F, -1.62F, 0F));
        PartDefinition p_abyssite_chest_left_arm = root.addOrReplaceChild("abyssite_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_aby_pauldron_left_arm = p_abyssite_chest_left_arm.addOrReplaceChild("aby_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(148, 35).addBox(-3.8F, -2.3F, -4F, 8F, 3F, 8F, new CubeDeformation(-0.2F, -0.3F, -0.1F))
                .texOffs(0, 71).addBox(-2.5F, 0.3F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, 0F, 0F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.2793F));
        PartDefinition p_aby_shoulder_left_arm_0_0 = p_aby_pauldron_left_arm.addOrReplaceChild("aby_shoulder_left_arm_0_0", CubeListBuilder.create()
                .texOffs(99, 283).addBox(-1F, -1.99F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.25F, -0.01F, -0.25F)),
                PartPose.offsetAndRotation(1.4F, -1.9F, -1.4F, 0.1745F, 0F, 0.4189F));
        PartDefinition p_aby_shoulder_left_arm_0_1 = p_aby_shoulder_left_arm_0_0.addOrReplaceChild("aby_shoulder_left_arm_0_1", CubeListBuilder.create()
                .texOffs(250, 315).addBox(-0.5F, -1.9F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.0875F, -0.1F, -0.0875F)),
                PartPose.offset(0F, -1.73F, 0F));
        PartDefinition p_aby_shoulder_left_arm_1_0 = p_aby_pauldron_left_arm.addOrReplaceChild("aby_shoulder_left_arm_1_0", CubeListBuilder.create()
                .texOffs(108, 283).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.285F, -0.45F)),
                PartPose.offsetAndRotation(2.2F, -1.9F, 1.5F, -0.2094F, 0F, 0.6283F));
        PartDefinition p_aby_shoulder_left_arm_1_1 = p_aby_shoulder_left_arm_1_0.addOrReplaceChild("aby_shoulder_left_arm_1_1", CubeListBuilder.create()
                .texOffs(0, 319).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1975F, -0.35F, -0.1975F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_abyssite_chest_right_arm = root.addOrReplaceChild("abyssite_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_aby_pauldron_right_arm = p_abyssite_chest_right_arm.addOrReplaceChild("aby_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(181, 35).addBox(-4.2F, -2.3F, -4F, 8F, 3F, 8F, new CubeDeformation(-0.2F, -0.3F, -0.1F))
                .texOffs(31, 71).addBox(-4.5F, 0.3F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, 0F, 0F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.2793F));
        PartDefinition p_aby_shoulder_right_arm_0_0 = p_aby_pauldron_right_arm.addOrReplaceChild("aby_shoulder_right_arm_0_0", CubeListBuilder.create()
                .texOffs(117, 283).addBox(-1F, -1.99F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.25F, -0.01F, -0.25F)),
                PartPose.offsetAndRotation(-1.4F, -1.9F, -1.4F, 0.1745F, 0F, -0.4189F));
        PartDefinition p_aby_shoulder_right_arm_0_1 = p_aby_shoulder_right_arm_0_0.addOrReplaceChild("aby_shoulder_right_arm_0_1", CubeListBuilder.create()
                .texOffs(5, 319).addBox(-0.5F, -1.9F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.0875F, -0.1F, -0.0875F)),
                PartPose.offset(0F, -1.73F, 0F));
        PartDefinition p_aby_shoulder_right_arm_1_0 = p_aby_pauldron_right_arm.addOrReplaceChild("aby_shoulder_right_arm_1_0", CubeListBuilder.create()
                .texOffs(126, 283).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.285F, -0.45F)),
                PartPose.offsetAndRotation(-2.2F, -1.9F, 1.5F, -0.2094F, 0F, -0.6283F));
        PartDefinition p_aby_shoulder_right_arm_1_1 = p_aby_shoulder_right_arm_1_0.addOrReplaceChild("aby_shoulder_right_arm_1_1", CubeListBuilder.create()
                .texOffs(10, 319).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1975F, -0.35F, -0.1975F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_abyssite_legs_body = root.addOrReplaceChild("abyssite_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_abyssite_legs_left_leg = root.addOrReplaceChild("abyssite_legs_left_leg", CubeListBuilder.create()
                .texOffs(196, 215).addBox(-3F, -0.9F, -3.3F, 6F, 5F, 1F, new CubeDeformation(-0.3F, -0.4F, 0F))
                .texOffs(14, 298).addBox(-1F, 0.1F, -4F, 2F, 3F, 1F, new CubeDeformation(-0.3F, -0.3F, -0.2F))
                .texOffs(174, 273).addBox(-2F, 4.9F, -3.3F, 4F, 3F, 1F, new CubeDeformation(0F, -0.1F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_abyssite_legs_right_leg = root.addOrReplaceChild("abyssite_legs_right_leg", CubeListBuilder.create()
                .texOffs(211, 215).addBox(-3F, -0.9F, -3.3F, 6F, 5F, 1F, new CubeDeformation(-0.3F, -0.4F, 0F))
                .texOffs(21, 298).addBox(-1F, 0.1F, -4F, 2F, 3F, 1F, new CubeDeformation(-0.3F, -0.3F, -0.2F))
                .texOffs(185, 273).addBox(-2F, 4.9F, -3.3F, 4F, 3F, 1F, new CubeDeformation(0F, -0.1F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_abyssite_feet_left_leg = root.addOrReplaceChild("abyssite_feet_left_leg", CubeListBuilder.create()
                .texOffs(99, 243).addBox(-2.5F, 10.25F, -4.45F, 5F, 3F, 2F, new CubeDeformation(0F, -0.45F, -0.45F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_aby_heel_left_leg_0_0 = p_abyssite_feet_left_leg.addOrReplaceChild("aby_heel_left_leg_0_0", CubeListBuilder.create()
                .texOffs(135, 283).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.285F, -0.45F)),
                PartPose.offsetAndRotation(0.9F, 10.8F, 2.8F, -0.9774F, 0F, 0.2094F));
        PartDefinition p_aby_heel_left_leg_0_1 = p_aby_heel_left_leg_0_0.addOrReplaceChild("aby_heel_left_leg_0_1", CubeListBuilder.create()
                .texOffs(15, 319).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1975F, -0.35F, -0.1975F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_aby_heel_left_leg_1_0 = p_abyssite_feet_left_leg.addOrReplaceChild("aby_heel_left_leg_1_0", CubeListBuilder.create()
                .texOffs(20, 319).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.05F, -0.45F, -0.05F)),
                PartPose.offsetAndRotation(-0.9F, 10.8F, 2.8F, -0.9774F, 0F, -0.2094F));
        PartDefinition p_aby_heel_left_leg_1_1 = p_aby_heel_left_leg_1_0.addOrReplaceChild("aby_heel_left_leg_1_1", CubeListBuilder.create()
                .texOffs(184, 327).addBox(-0.5F, -1F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.2525F, 0F, -0.2525F)),
                PartPose.offset(0F, -0.85F, 0F));
        PartDefinition p_abyssite_feet_right_leg = root.addOrReplaceChild("abyssite_feet_right_leg", CubeListBuilder.create()
                .texOffs(114, 243).addBox(-2.5F, 10.25F, -4.45F, 5F, 3F, 2F, new CubeDeformation(0F, -0.45F, -0.45F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_aby_heel_right_leg_0_0 = p_abyssite_feet_right_leg.addOrReplaceChild("aby_heel_right_leg_0_0", CubeListBuilder.create()
                .texOffs(144, 283).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.285F, -0.45F)),
                PartPose.offsetAndRotation(0.9F, 10.8F, 2.8F, -0.9774F, 0F, 0.2094F));
        PartDefinition p_aby_heel_right_leg_0_1 = p_aby_heel_right_leg_0_0.addOrReplaceChild("aby_heel_right_leg_0_1", CubeListBuilder.create()
                .texOffs(25, 319).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1975F, -0.35F, -0.1975F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_aby_heel_right_leg_1_0 = p_abyssite_feet_right_leg.addOrReplaceChild("aby_heel_right_leg_1_0", CubeListBuilder.create()
                .texOffs(30, 319).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.05F, -0.45F, -0.05F)),
                PartPose.offsetAndRotation(-0.9F, 10.8F, 2.8F, -0.9774F, 0F, -0.2094F));
        PartDefinition p_aby_heel_right_leg_1_1 = p_aby_heel_right_leg_1_0.addOrReplaceChild("aby_heel_right_leg_1_1", CubeListBuilder.create()
                .texOffs(189, 327).addBox(-0.5F, -1F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.2525F, 0F, -0.2525F)),
                PartPose.offset(0F, -0.85F, 0F));
        PartDefinition p_fire_head_head = root.addOrReplaceChild("fire_head_head", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_fire_horn_left_0 = p_fire_head_head.addOrReplaceChild("fire_horn_left_0", CubeListBuilder.create()
                .texOffs(121, 222).addBox(-1.5F, -2.9F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.1F, -0.4F)),
                PartPose.offsetAndRotation(3.2F, -8.7F, -1.6F, -0.4189F, 0F, 0.384F));
        PartDefinition p_fire_horn_left_1 = p_fire_horn_left_0.addOrReplaceChild("fire_horn_left_1", CubeListBuilder.create()
                .texOffs(188, 255).addBox(-1F, -2.9F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.1F, -0.1F, -0.1F)),
                PartPose.offsetAndRotation(0F, -2.55F, 0F, -0.5934F, 0F, -0.1047F));
        PartDefinition p_fire_horn_left_2 = p_fire_horn_left_1.addOrReplaceChild("fire_horn_left_2", CubeListBuilder.create()
                .texOffs(197, 255).addBox(-1F, -2.8F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.35F, -0.2F, -0.35F)),
                PartPose.offsetAndRotation(0F, -2.55F, 0F, -0.6632F, 0F, -0.1047F));
        PartDefinition p_fire_horn_left_3 = p_fire_horn_left_2.addOrReplaceChild("fire_horn_left_3", CubeListBuilder.create()
                .texOffs(94, 298).addBox(-0.5F, -2.6F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.1F)),
                PartPose.offsetAndRotation(0F, -2.35F, 0F, -0.5934F, 0F, 0F));
        PartDefinition p_fire_horn_right_0 = p_fire_head_head.addOrReplaceChild("fire_horn_right_0", CubeListBuilder.create()
                .texOffs(134, 222).addBox(-1.5F, -2.9F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.1F, -0.4F)),
                PartPose.offsetAndRotation(-3.2F, -8.7F, -1.6F, -0.4189F, 0F, -0.384F));
        PartDefinition p_fire_horn_right_1 = p_fire_horn_right_0.addOrReplaceChild("fire_horn_right_1", CubeListBuilder.create()
                .texOffs(206, 255).addBox(-1F, -2.9F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.1F, -0.1F, -0.1F)),
                PartPose.offsetAndRotation(0F, -2.55F, 0F, -0.5934F, 0F, 0.1047F));
        PartDefinition p_fire_horn_right_2 = p_fire_horn_right_1.addOrReplaceChild("fire_horn_right_2", CubeListBuilder.create()
                .texOffs(215, 255).addBox(-1F, -2.8F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.35F, -0.2F, -0.35F)),
                PartPose.offsetAndRotation(0F, -2.55F, 0F, -0.6632F, 0F, 0.1047F));
        PartDefinition p_fire_horn_right_3 = p_fire_horn_right_2.addOrReplaceChild("fire_horn_right_3", CubeListBuilder.create()
                .texOffs(99, 298).addBox(-0.5F, -2.6F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.1F)),
                PartPose.offsetAndRotation(0F, -2.35F, 0F, -0.5934F, 0F, 0F));
        PartDefinition p_fire_crest_0_0 = p_fire_head_head.addOrReplaceChild("fire_crest_0_0", CubeListBuilder.create()
                .texOffs(194, 327).addBox(-0.5F, -0.94F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.06F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, -3.2F, -0.4538F, 0F, 0F));
        PartDefinition p_fire_crest_0_1 = p_fire_crest_0_0.addOrReplaceChild("fire_crest_0_1", CubeListBuilder.create()
                .texOffs(199, 327).addBox(-0.5F, -0.9F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.1F, -0.225F)),
                PartPose.offset(0F, -0.63F, 0F));
        PartDefinition p_fire_crest_1_0 = p_fire_head_head.addOrReplaceChild("fire_crest_1_0", CubeListBuilder.create()
                .texOffs(35, 319).addBox(-0.5F, -1.605F, -0.5F, 1F, 2F, 1F, new CubeDeformation(0F, -0.395F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, -0.6F, -0.4538F, 0F, 0F));
        PartDefinition p_fire_crest_1_1 = p_fire_crest_1_0.addOrReplaceChild("fire_crest_1_1", CubeListBuilder.create()
                .texOffs(40, 319).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.225F, -0.45F, -0.225F)),
                PartPose.offset(0F, -0.96F, 0F));
        PartDefinition p_fire_crest_2_0 = p_fire_head_head.addOrReplaceChild("fire_crest_2_0", CubeListBuilder.create()
                .texOffs(204, 327).addBox(-0.5F, -0.995F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.005F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, 2F, -0.4538F, 0F, 0F));
        PartDefinition p_fire_crest_2_1 = p_fire_crest_2_0.addOrReplaceChild("fire_crest_2_1", CubeListBuilder.create()
                .texOffs(209, 327).addBox(-0.5F, -0.95F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.05F, -0.225F)),
                PartPose.offset(0F, -0.74F, 0F));
        PartDefinition p_fire_chest_body = root.addOrReplaceChild("fire_chest_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_fire_back_0_0 = p_fire_chest_body.addOrReplaceChild("fire_back_0_0", CubeListBuilder.create()
                .texOffs(153, 283).addBox(-1F, -1.935F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.065F, -0.3F)),
                PartPose.offsetAndRotation(0F, 0.8F, 2.9F, -0.8727F, 0F, 0F));
        PartDefinition p_fire_back_0_1 = p_fire_back_0_0.addOrReplaceChild("fire_back_0_1", CubeListBuilder.create()
                .texOffs(45, 319).addBox(-0.5F, -1.85F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.15F, -0.115F)),
                PartPose.offset(0F, -1.62F, 0F));
        PartDefinition p_fire_back_1_0 = p_fire_chest_body.addOrReplaceChild("fire_back_1_0", CubeListBuilder.create()
                .texOffs(224, 255).addBox(-1F, -2.545F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.3F, -0.455F, -0.3F)),
                PartPose.offsetAndRotation(0F, 3.6F, 2.9F, -0.8727F, 0F, 0F));
        PartDefinition p_fire_back_1_1 = p_fire_back_1_0.addOrReplaceChild("fire_back_1_1", CubeListBuilder.create()
                .texOffs(50, 319).addBox(-0.5F, -1.95F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.05F, -0.115F)),
                PartPose.offset(0F, -1.84F, 0F));
        PartDefinition p_fire_back_2_0 = p_fire_chest_body.addOrReplaceChild("fire_back_2_0", CubeListBuilder.create()
                .texOffs(162, 283).addBox(-1F, -1.88F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.12F, -0.3F)),
                PartPose.offsetAndRotation(0F, 6.6F, 2.9F, -0.8727F, 0F, 0F));
        PartDefinition p_fire_back_2_1 = p_fire_back_2_0.addOrReplaceChild("fire_back_2_1", CubeListBuilder.create()
                .texOffs(55, 319).addBox(-0.5F, -1.8F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.2F, -0.115F)),
                PartPose.offset(0F, -1.51F, 0F));
        PartDefinition p_fire_back_3_0 = p_fire_chest_body.addOrReplaceChild("fire_back_3_0", CubeListBuilder.create()
                .texOffs(171, 283).addBox(-1F, -1.66F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.34F, -0.3F)),
                PartPose.offsetAndRotation(0F, 9.4F, 2.9F, -0.8727F, 0F, 0F));
        PartDefinition p_fire_back_3_1 = p_fire_back_3_0.addOrReplaceChild("fire_back_3_1", CubeListBuilder.create()
                .texOffs(60, 319).addBox(-0.5F, -1.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.4F, -0.115F)),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_fire_chest_left_arm = root.addOrReplaceChild("fire_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_fire_pauldron_left_arm = p_fire_chest_left_arm.addOrReplaceChild("fire_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(62, 71).addBox(-3.3F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F))
                .texOffs(120, 104).addBox(-2F, -0.1F, -4F, 6F, 2F, 8F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.2094F));
        PartDefinition p_fire_claw_left_arm_0 = p_fire_pauldron_left_arm.addOrReplaceChild("fire_claw_left_arm_0", CubeListBuilder.create()
                .texOffs(180, 283).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.285F, -0.35F)),
                PartPose.offsetAndRotation(2.4F, -1.4F, 0F, 0F, 0F, 0.6981F));
        PartDefinition p_fire_claw_left_arm_1 = p_fire_claw_left_arm_0.addOrReplaceChild("fire_claw_left_arm_1", CubeListBuilder.create()
                .texOffs(65, 319).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.35F, -0.1425F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_fire_chest_right_arm = root.addOrReplaceChild("fire_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_fire_pauldron_right_arm = p_fire_chest_right_arm.addOrReplaceChild("fire_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(93, 71).addBox(-3.7F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F))
                .texOffs(149, 104).addBox(-4F, -0.1F, -4F, 6F, 2F, 8F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.2094F));
        PartDefinition p_fire_claw_right_arm_0 = p_fire_pauldron_right_arm.addOrReplaceChild("fire_claw_right_arm_0", CubeListBuilder.create()
                .texOffs(189, 283).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.285F, -0.35F)),
                PartPose.offsetAndRotation(-2.4F, -1.4F, 0F, 0F, 0F, -0.6981F));
        PartDefinition p_fire_claw_right_arm_1 = p_fire_claw_right_arm_0.addOrReplaceChild("fire_claw_right_arm_1", CubeListBuilder.create()
                .texOffs(70, 319).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.35F, -0.1425F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_fire_legs_body = root.addOrReplaceChild("fire_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_fire_tail_0 = p_fire_legs_body.addOrReplaceChild("fire_tail_0", CubeListBuilder.create()
                .texOffs(147, 222).addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F),
                PartPose.offsetAndRotation(0F, 10.2F, 2.2F, 0.5236F, 0F, 0F));
        PartDefinition p_fire_tail_1 = p_fire_tail_0.addOrReplaceChild("fire_tail_1", CubeListBuilder.create()
                .texOffs(160, 222).addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, 2.75F, 0F, 0.1745F, 0F, 0F));
        PartDefinition p_fire_tail_2 = p_fire_tail_1.addOrReplaceChild("fire_tail_2", CubeListBuilder.create()
                .texOffs(173, 222).addBox(-1.5F, -0.1F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.1F, -0.4F)),
                PartPose.offsetAndRotation(0F, 2.75F, 0F, 0.2094F, 0F, 0F));
        PartDefinition p_fire_tail_3 = p_fire_tail_2.addOrReplaceChild("fire_tail_3", CubeListBuilder.create()
                .texOffs(233, 255).addBox(-1F, -0.2F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.15F, -0.2F, -0.15F)),
                PartPose.offsetAndRotation(0F, 2.55F, 0F, 0.2094F, 0F, 0F));
        PartDefinition p_fire_tail_4 = p_fire_tail_3.addOrReplaceChild("fire_tail_4", CubeListBuilder.create()
                .texOffs(242, 255).addBox(-1F, -0.4F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.4F, -0.4F, -0.4F)),
                PartPose.offsetAndRotation(0F, 2.35F, 0F, 0.1745F, 0F, 0F));
        PartDefinition p_fire_tail_tip = p_fire_tail_4.addOrReplaceChild("fire_tail_tip", CubeListBuilder.create()
                .texOffs(149, 249).addBox(-1.5F, -0.3F, -1F, 3F, 3F, 2F, new CubeDeformation(-0.3F, -0.3F, -0.3F))
                .texOffs(7, 307).addBox(-1F, 2.2F, -0.5F, 2F, 2F, 1F, new CubeDeformation(-0.3F, 0F, -0.1F))
                .texOffs(75, 319).addBox(-0.5F, 3.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.2F, -0.4F, -0.3F)),
                PartPose.offsetAndRotation(0F, 2.4F, 0F, 0.1396F, 0F, 0F));
        PartDefinition p_fire_legs_left_leg = root.addOrReplaceChild("fire_legs_left_leg", CubeListBuilder.create()
                .texOffs(196, 273).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_fire_legs_right_leg = root.addOrReplaceChild("fire_legs_right_leg", CubeListBuilder.create()
                .texOffs(207, 273).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_fire_feet_left_leg = root.addOrReplaceChild("fire_feet_left_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_fire_talon_left_leg_0 = p_fire_feet_left_leg.addOrReplaceChild("fire_talon_left_leg_0", CubeListBuilder.create()
                .texOffs(14, 307).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(-1.6F, 11.6F, -2.8F, 0.4887F, -0.2234F, 0F));
        PartDefinition p_fire_talon_left_leg_1 = p_fire_feet_left_leg.addOrReplaceChild("fire_talon_left_leg_1", CubeListBuilder.create()
                .texOffs(21, 307).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(0F, 11.6F, -2.8F, 0.4887F, 0F, 0F));
        PartDefinition p_fire_talon_left_leg_2 = p_fire_feet_left_leg.addOrReplaceChild("fire_talon_left_leg_2", CubeListBuilder.create()
                .texOffs(28, 307).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(1.6F, 11.6F, -2.8F, 0.4887F, 0.2234F, 0F));
        PartDefinition p_fire_feet_right_leg = root.addOrReplaceChild("fire_feet_right_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_fire_talon_right_leg_0 = p_fire_feet_right_leg.addOrReplaceChild("fire_talon_right_leg_0", CubeListBuilder.create()
                .texOffs(35, 307).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(-1.6F, 11.6F, -2.8F, 0.4887F, -0.2234F, 0F));
        PartDefinition p_fire_talon_right_leg_1 = p_fire_feet_right_leg.addOrReplaceChild("fire_talon_right_leg_1", CubeListBuilder.create()
                .texOffs(42, 307).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(0F, 11.6F, -2.8F, 0.4887F, 0F, 0F));
        PartDefinition p_fire_talon_right_leg_2 = p_fire_feet_right_leg.addOrReplaceChild("fire_talon_right_leg_2", CubeListBuilder.create()
                .texOffs(49, 307).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(1.6F, 11.6F, -2.8F, 0.4887F, 0.2234F, 0F));
        PartDefinition p_ice_head_head = root.addOrReplaceChild("ice_head_head", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_ice_horn_left_0 = p_ice_head_head.addOrReplaceChild("ice_horn_left_0", CubeListBuilder.create()
                .texOffs(134, 236).addBox(-1F, -3.8F, -1F, 2F, 4F, 2F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offsetAndRotation(3F, -8.7F, -1F, -0.4887F, 0F, 0.2793F));
        PartDefinition p_ice_horn_left_1 = p_ice_horn_left_0.addOrReplaceChild("ice_horn_left_1", CubeListBuilder.create()
                .texOffs(143, 236).addBox(-1F, -3.7F, -1F, 2F, 4F, 2F, new CubeDeformation(-0.4F, -0.3F, -0.4F)),
                PartPose.offsetAndRotation(0F, -3.35F, 0F, -0.1047F, 0F, 0F));
        PartDefinition p_ice_side_left_0 = p_ice_head_head.addOrReplaceChild("ice_side_left_0", CubeListBuilder.create()
                .texOffs(198, 283).addBox(-1F, -1.66F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.34F, -0.45F)),
                PartPose.offsetAndRotation(4.4F, -7.4F, 0.6F, -0.6283F, 0F, 0.6981F));
        PartDefinition p_ice_side_left_1 = p_ice_side_left_0.addOrReplaceChild("ice_side_left_1", CubeListBuilder.create()
                .texOffs(80, 319).addBox(-0.5F, -1.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1975F, -0.4F, -0.1975F)),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_ice_horn_right_0 = p_ice_head_head.addOrReplaceChild("ice_horn_right_0", CubeListBuilder.create()
                .texOffs(152, 236).addBox(-1F, -3.8F, -1F, 2F, 4F, 2F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offsetAndRotation(-3F, -8.7F, -1F, -0.4887F, 0F, -0.2793F));
        PartDefinition p_ice_horn_right_1 = p_ice_horn_right_0.addOrReplaceChild("ice_horn_right_1", CubeListBuilder.create()
                .texOffs(161, 236).addBox(-1F, -3.7F, -1F, 2F, 4F, 2F, new CubeDeformation(-0.4F, -0.3F, -0.4F)),
                PartPose.offsetAndRotation(0F, -3.35F, 0F, -0.1047F, 0F, 0F));
        PartDefinition p_ice_side_right_0 = p_ice_head_head.addOrReplaceChild("ice_side_right_0", CubeListBuilder.create()
                .texOffs(207, 283).addBox(-1F, -1.66F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.34F, -0.45F)),
                PartPose.offsetAndRotation(-4.4F, -7.4F, 0.6F, -0.6283F, 0F, -0.6981F));
        PartDefinition p_ice_side_right_1 = p_ice_side_right_0.addOrReplaceChild("ice_side_right_1", CubeListBuilder.create()
                .texOffs(85, 319).addBox(-0.5F, -1.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1975F, -0.4F, -0.1975F)),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_ice_crest_0_0 = p_ice_head_head.addOrReplaceChild("ice_crest_0_0", CubeListBuilder.create()
                .texOffs(214, 327).addBox(-0.5F, -0.94F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.06F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, -3.2F, -0.4538F, 0F, 0F));
        PartDefinition p_ice_crest_0_1 = p_ice_crest_0_0.addOrReplaceChild("ice_crest_0_1", CubeListBuilder.create()
                .texOffs(219, 327).addBox(-0.5F, -0.9F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.1F, -0.225F)),
                PartPose.offset(0F, -0.63F, 0F));
        PartDefinition p_ice_crest_1_0 = p_ice_head_head.addOrReplaceChild("ice_crest_1_0", CubeListBuilder.create()
                .texOffs(90, 319).addBox(-0.5F, -1.605F, -0.5F, 1F, 2F, 1F, new CubeDeformation(0F, -0.395F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, -0.6F, -0.4538F, 0F, 0F));
        PartDefinition p_ice_crest_1_1 = p_ice_crest_1_0.addOrReplaceChild("ice_crest_1_1", CubeListBuilder.create()
                .texOffs(95, 319).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.225F, -0.45F, -0.225F)),
                PartPose.offset(0F, -0.96F, 0F));
        PartDefinition p_ice_crest_2_0 = p_ice_head_head.addOrReplaceChild("ice_crest_2_0", CubeListBuilder.create()
                .texOffs(224, 327).addBox(-0.5F, -0.995F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.005F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, 2F, -0.4538F, 0F, 0F));
        PartDefinition p_ice_crest_2_1 = p_ice_crest_2_0.addOrReplaceChild("ice_crest_2_1", CubeListBuilder.create()
                .texOffs(229, 327).addBox(-0.5F, -0.95F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.05F, -0.225F)),
                PartPose.offset(0F, -0.74F, 0F));
        PartDefinition p_ice_chest_body = root.addOrReplaceChild("ice_chest_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_ice_back_0_0 = p_ice_chest_body.addOrReplaceChild("ice_back_0_0", CubeListBuilder.create()
                .texOffs(216, 283).addBox(-1F, -1.935F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.065F, -0.3F)),
                PartPose.offsetAndRotation(0F, 0.8F, 2.9F, -0.8727F, 0F, 0F));
        PartDefinition p_ice_back_0_1 = p_ice_back_0_0.addOrReplaceChild("ice_back_0_1", CubeListBuilder.create()
                .texOffs(100, 319).addBox(-0.5F, -1.85F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.15F, -0.115F)),
                PartPose.offset(0F, -1.62F, 0F));
        PartDefinition p_ice_back_1_0 = p_ice_chest_body.addOrReplaceChild("ice_back_1_0", CubeListBuilder.create()
                .texOffs(0, 261).addBox(-1F, -2.545F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.3F, -0.455F, -0.3F)),
                PartPose.offsetAndRotation(0F, 3.6F, 2.9F, -0.8727F, 0F, 0F));
        PartDefinition p_ice_back_1_1 = p_ice_back_1_0.addOrReplaceChild("ice_back_1_1", CubeListBuilder.create()
                .texOffs(105, 319).addBox(-0.5F, -1.95F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.05F, -0.115F)),
                PartPose.offset(0F, -1.84F, 0F));
        PartDefinition p_ice_back_2_0 = p_ice_chest_body.addOrReplaceChild("ice_back_2_0", CubeListBuilder.create()
                .texOffs(225, 283).addBox(-1F, -1.88F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.12F, -0.3F)),
                PartPose.offsetAndRotation(0F, 6.6F, 2.9F, -0.8727F, 0F, 0F));
        PartDefinition p_ice_back_2_1 = p_ice_back_2_0.addOrReplaceChild("ice_back_2_1", CubeListBuilder.create()
                .texOffs(110, 319).addBox(-0.5F, -1.8F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.2F, -0.115F)),
                PartPose.offset(0F, -1.51F, 0F));
        PartDefinition p_ice_back_3_0 = p_ice_chest_body.addOrReplaceChild("ice_back_3_0", CubeListBuilder.create()
                .texOffs(234, 283).addBox(-1F, -1.66F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.34F, -0.3F)),
                PartPose.offsetAndRotation(0F, 9.4F, 2.9F, -0.8727F, 0F, 0F));
        PartDefinition p_ice_back_3_1 = p_ice_back_3_0.addOrReplaceChild("ice_back_3_1", CubeListBuilder.create()
                .texOffs(115, 319).addBox(-0.5F, -1.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.4F, -0.115F)),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_ice_chest_left_arm = root.addOrReplaceChild("ice_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_ice_pauldron_left_arm = p_ice_chest_left_arm.addOrReplaceChild("ice_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(124, 71).addBox(-3.3F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F))
                .texOffs(178, 104).addBox(-2F, -0.1F, -4F, 6F, 2F, 8F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.2094F));
        PartDefinition p_ice_claw_left_arm_0 = p_ice_pauldron_left_arm.addOrReplaceChild("ice_claw_left_arm_0", CubeListBuilder.create()
                .texOffs(243, 283).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.285F, -0.35F)),
                PartPose.offsetAndRotation(2.4F, -1.4F, 0F, 0F, 0F, 0.6981F));
        PartDefinition p_ice_claw_left_arm_1 = p_ice_claw_left_arm_0.addOrReplaceChild("ice_claw_left_arm_1", CubeListBuilder.create()
                .texOffs(120, 319).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.35F, -0.1425F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_ice_chest_right_arm = root.addOrReplaceChild("ice_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_ice_pauldron_right_arm = p_ice_chest_right_arm.addOrReplaceChild("ice_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(155, 71).addBox(-3.7F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F))
                .texOffs(207, 104).addBox(-4F, -0.1F, -4F, 6F, 2F, 8F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.2094F));
        PartDefinition p_ice_claw_right_arm_0 = p_ice_pauldron_right_arm.addOrReplaceChild("ice_claw_right_arm_0", CubeListBuilder.create()
                .texOffs(0, 288).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.285F, -0.35F)),
                PartPose.offsetAndRotation(-2.4F, -1.4F, 0F, 0F, 0F, -0.6981F));
        PartDefinition p_ice_claw_right_arm_1 = p_ice_claw_right_arm_0.addOrReplaceChild("ice_claw_right_arm_1", CubeListBuilder.create()
                .texOffs(125, 319).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.35F, -0.1425F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_ice_legs_body = root.addOrReplaceChild("ice_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_ice_tail_0 = p_ice_legs_body.addOrReplaceChild("ice_tail_0", CubeListBuilder.create()
                .texOffs(186, 222).addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F),
                PartPose.offsetAndRotation(0F, 10.2F, 2.2F, 0.5236F, 0F, 0F));
        PartDefinition p_ice_tail_1 = p_ice_tail_0.addOrReplaceChild("ice_tail_1", CubeListBuilder.create()
                .texOffs(199, 222).addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, 2.75F, 0F, 0.1745F, 0F, 0F));
        PartDefinition p_ice_tail_2 = p_ice_tail_1.addOrReplaceChild("ice_tail_2", CubeListBuilder.create()
                .texOffs(212, 222).addBox(-1.5F, -0.1F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.1F, -0.4F)),
                PartPose.offsetAndRotation(0F, 2.75F, 0F, 0.2094F, 0F, 0F));
        PartDefinition p_ice_tail_3 = p_ice_tail_2.addOrReplaceChild("ice_tail_3", CubeListBuilder.create()
                .texOffs(9, 261).addBox(-1F, -0.2F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.15F, -0.2F, -0.15F)),
                PartPose.offsetAndRotation(0F, 2.55F, 0F, 0.2094F, 0F, 0F));
        PartDefinition p_ice_tail_4 = p_ice_tail_3.addOrReplaceChild("ice_tail_4", CubeListBuilder.create()
                .texOffs(18, 261).addBox(-1F, -0.4F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.4F, -0.4F, -0.4F)),
                PartPose.offsetAndRotation(0F, 2.35F, 0F, 0.1745F, 0F, 0F));
        PartDefinition p_ice_tail_tip = p_ice_tail_4.addOrReplaceChild("ice_tail_tip", CubeListBuilder.create()
                .texOffs(27, 261).addBox(-1F, -0.3F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.1F, -0.3F, -0.1F))
                .texOffs(104, 298).addBox(-0.5F, 1.8F, -0.5F, 1F, 3F, 1F, new CubeDeformation(0F, -0.4F, 0F)),
                PartPose.offsetAndRotation(0F, 2.4F, 0F, 0.1396F, 0F, 0F));
        PartDefinition p_ice_legs_left_leg = root.addOrReplaceChild("ice_legs_left_leg", CubeListBuilder.create()
                .texOffs(218, 273).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_ice_legs_right_leg = root.addOrReplaceChild("ice_legs_right_leg", CubeListBuilder.create()
                .texOffs(229, 273).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_ice_feet_left_leg = root.addOrReplaceChild("ice_feet_left_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_ice_talon_left_leg_0 = p_ice_feet_left_leg.addOrReplaceChild("ice_talon_left_leg_0", CubeListBuilder.create()
                .texOffs(56, 307).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(-1.6F, 11.6F, -2.8F, 0.4887F, -0.2234F, 0F));
        PartDefinition p_ice_talon_left_leg_1 = p_ice_feet_left_leg.addOrReplaceChild("ice_talon_left_leg_1", CubeListBuilder.create()
                .texOffs(63, 307).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(0F, 11.6F, -2.8F, 0.4887F, 0F, 0F));
        PartDefinition p_ice_talon_left_leg_2 = p_ice_feet_left_leg.addOrReplaceChild("ice_talon_left_leg_2", CubeListBuilder.create()
                .texOffs(70, 307).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(1.6F, 11.6F, -2.8F, 0.4887F, 0.2234F, 0F));
        PartDefinition p_ice_feet_right_leg = root.addOrReplaceChild("ice_feet_right_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_ice_talon_right_leg_0 = p_ice_feet_right_leg.addOrReplaceChild("ice_talon_right_leg_0", CubeListBuilder.create()
                .texOffs(77, 307).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(-1.6F, 11.6F, -2.8F, 0.4887F, -0.2234F, 0F));
        PartDefinition p_ice_talon_right_leg_1 = p_ice_feet_right_leg.addOrReplaceChild("ice_talon_right_leg_1", CubeListBuilder.create()
                .texOffs(84, 307).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(0F, 11.6F, -2.8F, 0.4887F, 0F, 0F));
        PartDefinition p_ice_talon_right_leg_2 = p_ice_feet_right_leg.addOrReplaceChild("ice_talon_right_leg_2", CubeListBuilder.create()
                .texOffs(91, 307).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(1.6F, 11.6F, -2.8F, 0.4887F, 0.2234F, 0F));
        PartDefinition p_lightning_head_head = root.addOrReplaceChild("lightning_head_head", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_light_horn_left_0 = p_lightning_head_head.addOrReplaceChild("light_horn_left_0", CubeListBuilder.create()
                .texOffs(36, 261).addBox(-1F, -2.7F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.05F, -0.3F, -0.05F)),
                PartPose.offsetAndRotation(3.1F, -8.7F, -1.2F, -0.3491F, 0F, 0.4538F));
        PartDefinition p_light_horn_left_1 = p_light_horn_left_0.addOrReplaceChild("light_horn_left_1", CubeListBuilder.create()
                .texOffs(45, 261).addBox(-1F, -2.6F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.2F, -0.4F, -0.2F)),
                PartPose.offsetAndRotation(0F, -2.15F, 0F, -0.8727F, 0F, -0.3142F));
        PartDefinition p_light_horn_left_2 = p_light_horn_left_1.addOrReplaceChild("light_horn_left_2", CubeListBuilder.create()
                .texOffs(54, 261).addBox(-1F, -2.6F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.35F, -0.4F, -0.35F)),
                PartPose.offsetAndRotation(0F, -1.95F, 0F, 0.6981F, 0F, 0.1745F));
        PartDefinition p_light_horn_left_3 = p_light_horn_left_2.addOrReplaceChild("light_horn_left_3", CubeListBuilder.create()
                .texOffs(130, 319).addBox(-0.5F, -2F, -0.5F, 1F, 2F, 1F),
                PartPose.offsetAndRotation(0F, -1.95F, 0F, -0.9774F, 0F, -0.1047F));
        PartDefinition p_light_horn_left_4 = p_light_horn_left_3.addOrReplaceChild("light_horn_left_4", CubeListBuilder.create()
                .texOffs(135, 319).addBox(-0.5F, -1.9F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.15F, -0.1F, -0.15F)),
                PartPose.offsetAndRotation(0F, -1.75F, 0F, 0.5934F, 0F, 0F));
        PartDefinition p_light_horn_right_0 = p_lightning_head_head.addOrReplaceChild("light_horn_right_0", CubeListBuilder.create()
                .texOffs(63, 261).addBox(-1F, -2.7F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.05F, -0.3F, -0.05F)),
                PartPose.offsetAndRotation(-3.1F, -8.7F, -1.2F, -0.3491F, 0F, -0.4538F));
        PartDefinition p_light_horn_right_1 = p_light_horn_right_0.addOrReplaceChild("light_horn_right_1", CubeListBuilder.create()
                .texOffs(72, 261).addBox(-1F, -2.6F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.2F, -0.4F, -0.2F)),
                PartPose.offsetAndRotation(0F, -2.15F, 0F, -0.8727F, 0F, 0.3142F));
        PartDefinition p_light_horn_right_2 = p_light_horn_right_1.addOrReplaceChild("light_horn_right_2", CubeListBuilder.create()
                .texOffs(81, 261).addBox(-1F, -2.6F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.35F, -0.4F, -0.35F)),
                PartPose.offsetAndRotation(0F, -1.95F, 0F, 0.6981F, 0F, -0.1745F));
        PartDefinition p_light_horn_right_3 = p_light_horn_right_2.addOrReplaceChild("light_horn_right_3", CubeListBuilder.create()
                .texOffs(140, 319).addBox(-0.5F, -2F, -0.5F, 1F, 2F, 1F),
                PartPose.offsetAndRotation(0F, -1.95F, 0F, -0.9774F, 0F, 0.1047F));
        PartDefinition p_light_horn_right_4 = p_light_horn_right_3.addOrReplaceChild("light_horn_right_4", CubeListBuilder.create()
                .texOffs(145, 319).addBox(-0.5F, -1.9F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.15F, -0.1F, -0.15F)),
                PartPose.offsetAndRotation(0F, -1.75F, 0F, 0.5934F, 0F, 0F));
        PartDefinition p_lightning_crest_0_0 = p_lightning_head_head.addOrReplaceChild("lightning_crest_0_0", CubeListBuilder.create()
                .texOffs(234, 327).addBox(-0.5F, -0.94F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.06F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, -3.2F, -0.4538F, 0F, 0F));
        PartDefinition p_lightning_crest_0_1 = p_lightning_crest_0_0.addOrReplaceChild("lightning_crest_0_1", CubeListBuilder.create()
                .texOffs(239, 327).addBox(-0.5F, -0.9F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.1F, -0.225F)),
                PartPose.offset(0F, -0.63F, 0F));
        PartDefinition p_lightning_crest_1_0 = p_lightning_head_head.addOrReplaceChild("lightning_crest_1_0", CubeListBuilder.create()
                .texOffs(150, 319).addBox(-0.5F, -1.605F, -0.5F, 1F, 2F, 1F, new CubeDeformation(0F, -0.395F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, -0.6F, -0.4538F, 0F, 0F));
        PartDefinition p_lightning_crest_1_1 = p_lightning_crest_1_0.addOrReplaceChild("lightning_crest_1_1", CubeListBuilder.create()
                .texOffs(155, 319).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.225F, -0.45F, -0.225F)),
                PartPose.offset(0F, -0.96F, 0F));
        PartDefinition p_lightning_crest_2_0 = p_lightning_head_head.addOrReplaceChild("lightning_crest_2_0", CubeListBuilder.create()
                .texOffs(244, 327).addBox(-0.5F, -0.995F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.005F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, 2F, -0.4538F, 0F, 0F));
        PartDefinition p_lightning_crest_2_1 = p_lightning_crest_2_0.addOrReplaceChild("lightning_crest_2_1", CubeListBuilder.create()
                .texOffs(249, 327).addBox(-0.5F, -0.95F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.05F, -0.225F)),
                PartPose.offset(0F, -0.74F, 0F));
        PartDefinition p_lightning_chest_body = root.addOrReplaceChild("lightning_chest_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_lightning_back_0_0 = p_lightning_chest_body.addOrReplaceChild("lightning_back_0_0", CubeListBuilder.create()
                .texOffs(9, 288).addBox(-1F, -1.935F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.065F, -0.3F)),
                PartPose.offsetAndRotation(0F, 0.8F, 2.9F, -0.8727F, 0F, 0F));
        PartDefinition p_lightning_back_0_1 = p_lightning_back_0_0.addOrReplaceChild("lightning_back_0_1", CubeListBuilder.create()
                .texOffs(160, 319).addBox(-0.5F, -1.85F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.15F, -0.115F)),
                PartPose.offset(0F, -1.62F, 0F));
        PartDefinition p_lightning_back_1_0 = p_lightning_chest_body.addOrReplaceChild("lightning_back_1_0", CubeListBuilder.create()
                .texOffs(90, 261).addBox(-1F, -2.545F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.3F, -0.455F, -0.3F)),
                PartPose.offsetAndRotation(0F, 3.6F, 2.9F, -0.8727F, 0F, 0F));
        PartDefinition p_lightning_back_1_1 = p_lightning_back_1_0.addOrReplaceChild("lightning_back_1_1", CubeListBuilder.create()
                .texOffs(165, 319).addBox(-0.5F, -1.95F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.05F, -0.115F)),
                PartPose.offset(0F, -1.84F, 0F));
        PartDefinition p_lightning_back_2_0 = p_lightning_chest_body.addOrReplaceChild("lightning_back_2_0", CubeListBuilder.create()
                .texOffs(18, 288).addBox(-1F, -1.88F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.12F, -0.3F)),
                PartPose.offsetAndRotation(0F, 6.6F, 2.9F, -0.8727F, 0F, 0F));
        PartDefinition p_lightning_back_2_1 = p_lightning_back_2_0.addOrReplaceChild("lightning_back_2_1", CubeListBuilder.create()
                .texOffs(170, 319).addBox(-0.5F, -1.8F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.2F, -0.115F)),
                PartPose.offset(0F, -1.51F, 0F));
        PartDefinition p_lightning_back_3_0 = p_lightning_chest_body.addOrReplaceChild("lightning_back_3_0", CubeListBuilder.create()
                .texOffs(27, 288).addBox(-1F, -1.66F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.34F, -0.3F)),
                PartPose.offsetAndRotation(0F, 9.4F, 2.9F, -0.8727F, 0F, 0F));
        PartDefinition p_lightning_back_3_1 = p_lightning_back_3_0.addOrReplaceChild("lightning_back_3_1", CubeListBuilder.create()
                .texOffs(175, 319).addBox(-0.5F, -1.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.4F, -0.115F)),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_lightning_chest_left_arm = root.addOrReplaceChild("lightning_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_lightning_pauldron_left_arm = p_lightning_chest_left_arm.addOrReplaceChild("lightning_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(186, 71).addBox(-3.3F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F))
                .texOffs(0, 115).addBox(-2F, -0.1F, -4F, 6F, 2F, 8F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.2094F));
        PartDefinition p_lightning_claw_left_arm_0 = p_lightning_pauldron_left_arm.addOrReplaceChild("lightning_claw_left_arm_0", CubeListBuilder.create()
                .texOffs(36, 288).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.285F, -0.35F)),
                PartPose.offsetAndRotation(2.4F, -1.4F, 0F, 0F, 0F, 0.6981F));
        PartDefinition p_lightning_claw_left_arm_1 = p_lightning_claw_left_arm_0.addOrReplaceChild("lightning_claw_left_arm_1", CubeListBuilder.create()
                .texOffs(180, 319).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.35F, -0.1425F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_lightning_chest_right_arm = root.addOrReplaceChild("lightning_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_lightning_pauldron_right_arm = p_lightning_chest_right_arm.addOrReplaceChild("lightning_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(217, 71).addBox(-3.7F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F))
                .texOffs(29, 115).addBox(-4F, -0.1F, -4F, 6F, 2F, 8F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.2094F));
        PartDefinition p_lightning_claw_right_arm_0 = p_lightning_pauldron_right_arm.addOrReplaceChild("lightning_claw_right_arm_0", CubeListBuilder.create()
                .texOffs(45, 288).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.285F, -0.35F)),
                PartPose.offsetAndRotation(-2.4F, -1.4F, 0F, 0F, 0F, -0.6981F));
        PartDefinition p_lightning_claw_right_arm_1 = p_lightning_claw_right_arm_0.addOrReplaceChild("lightning_claw_right_arm_1", CubeListBuilder.create()
                .texOffs(185, 319).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.35F, -0.1425F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_lightning_legs_body = root.addOrReplaceChild("lightning_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_lightning_tail_0 = p_lightning_legs_body.addOrReplaceChild("lightning_tail_0", CubeListBuilder.create()
                .texOffs(225, 222).addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F),
                PartPose.offsetAndRotation(0F, 10.2F, 2.2F, 0.5236F, 0F, 0F));
        PartDefinition p_lightning_tail_1 = p_lightning_tail_0.addOrReplaceChild("lightning_tail_1", CubeListBuilder.create()
                .texOffs(238, 222).addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, 2.75F, 0F, 0.1745F, 0F, 0F));
        PartDefinition p_lightning_tail_2 = p_lightning_tail_1.addOrReplaceChild("lightning_tail_2", CubeListBuilder.create()
                .texOffs(0, 229).addBox(-1.5F, -0.1F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.1F, -0.4F)),
                PartPose.offsetAndRotation(0F, 2.75F, 0F, 0.2094F, 0F, 0F));
        PartDefinition p_lightning_tail_3 = p_lightning_tail_2.addOrReplaceChild("lightning_tail_3", CubeListBuilder.create()
                .texOffs(99, 261).addBox(-1F, -0.2F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.15F, -0.2F, -0.15F)),
                PartPose.offsetAndRotation(0F, 2.55F, 0F, 0.2094F, 0F, 0F));
        PartDefinition p_lightning_tail_4 = p_lightning_tail_3.addOrReplaceChild("lightning_tail_4", CubeListBuilder.create()
                .texOffs(108, 261).addBox(-1F, -0.4F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.4F, -0.4F, -0.4F)),
                PartPose.offsetAndRotation(0F, 2.35F, 0F, 0.1745F, 0F, 0F));
        PartDefinition p_lightning_tail_tip = p_lightning_tail_4.addOrReplaceChild("lightning_tail_tip", CubeListBuilder.create()
                .texOffs(152, 207).addBox(-0.5F, -0.4F, -2F, 1F, 3F, 4F, new CubeDeformation(-0.2F, -0.4F, -0.4F))
                .texOffs(117, 261).addBox(-0.5F, 2F, -0.8F, 1F, 2F, 3F, new CubeDeformation(-0.25F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, 2.4F, 0F, 0.1396F, 0F, 0F));
        PartDefinition p_lightning_legs_left_leg = root.addOrReplaceChild("lightning_legs_left_leg", CubeListBuilder.create()
                .texOffs(240, 273).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_lightning_legs_right_leg = root.addOrReplaceChild("lightning_legs_right_leg", CubeListBuilder.create()
                .texOffs(0, 278).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_lightning_feet_left_leg = root.addOrReplaceChild("lightning_feet_left_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_lightning_talon_left_leg_0 = p_lightning_feet_left_leg.addOrReplaceChild("lightning_talon_left_leg_0", CubeListBuilder.create()
                .texOffs(98, 307).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(-1.6F, 11.6F, -2.8F, 0.4887F, -0.2234F, 0F));
        PartDefinition p_lightning_talon_left_leg_1 = p_lightning_feet_left_leg.addOrReplaceChild("lightning_talon_left_leg_1", CubeListBuilder.create()
                .texOffs(105, 307).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(0F, 11.6F, -2.8F, 0.4887F, 0F, 0F));
        PartDefinition p_lightning_talon_left_leg_2 = p_lightning_feet_left_leg.addOrReplaceChild("lightning_talon_left_leg_2", CubeListBuilder.create()
                .texOffs(112, 307).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(1.6F, 11.6F, -2.8F, 0.4887F, 0.2234F, 0F));
        PartDefinition p_lightning_feet_right_leg = root.addOrReplaceChild("lightning_feet_right_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_lightning_talon_right_leg_0 = p_lightning_feet_right_leg.addOrReplaceChild("lightning_talon_right_leg_0", CubeListBuilder.create()
                .texOffs(119, 307).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(-1.6F, 11.6F, -2.8F, 0.4887F, -0.2234F, 0F));
        PartDefinition p_lightning_talon_right_leg_1 = p_lightning_feet_right_leg.addOrReplaceChild("lightning_talon_right_leg_1", CubeListBuilder.create()
                .texOffs(126, 307).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(0F, 11.6F, -2.8F, 0.4887F, 0F, 0F));
        PartDefinition p_lightning_talon_right_leg_2 = p_lightning_feet_right_leg.addOrReplaceChild("lightning_talon_right_leg_2", CubeListBuilder.create()
                .texOffs(133, 307).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(1.6F, 11.6F, -2.8F, 0.4887F, 0.2234F, 0F));
        PartDefinition p_nature_head_head = root.addOrReplaceChild("nature_head_head", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_antler_left_0 = p_nature_head_head.addOrReplaceChild("antler_left_0", CubeListBuilder.create()
                .texOffs(126, 261).addBox(-1F, -3F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.3F, 0F, -0.3F)),
                PartPose.offsetAndRotation(3F, -8.7F, -0.8F, -0.2443F, 0F, 0.5236F));
        PartDefinition p_antler_left_1 = p_antler_left_0.addOrReplaceChild("antler_left_1", CubeListBuilder.create()
                .texOffs(135, 261).addBox(-1F, -3F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.4F, 0F, -0.4F)),
                PartPose.offsetAndRotation(0F, -2.75F, 0F, -0.384F, 0F, -0.1745F));
        PartDefinition p_antler_left_2 = p_antler_left_1.addOrReplaceChild("antler_left_2", CubeListBuilder.create()
                .texOffs(109, 298).addBox(-0.5F, -2.8F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.05F, -0.2F, -0.05F)),
                PartPose.offsetAndRotation(0F, -2.75F, 0F, -0.3491F, 0F, -0.1396F));
        PartDefinition p_antler_tine_left_1 = p_antler_left_1.addOrReplaceChild("antler_tine_left_1", CubeListBuilder.create()
                .texOffs(190, 319).addBox(-0.5F, -1.9F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.15F, -0.1F, -0.15F)),
                PartPose.offsetAndRotation(0F, -2.2F, 0F, 0.8378F, 0F, -0.2443F));
        PartDefinition p_antler_tine_left_0 = p_antler_left_0.addOrReplaceChild("antler_tine_left_0", CubeListBuilder.create()
                .texOffs(195, 319).addBox(-0.5F, -2F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1F, 0F, -0.1F)),
                PartPose.offsetAndRotation(0F, -2.4F, 0F, 0.8727F, 0F, 0.1745F));
        PartDefinition p_antler_leaf_left = p_antler_left_0.addOrReplaceChild("antler_leaf_left", CubeListBuilder.create()
                .texOffs(63, 267).addBox(-0.5F, -2.8F, -1F, 1F, 3F, 2F, new CubeDeformation(-0.35F, -0.2F, -0.1F)),
                PartPose.offsetAndRotation(0.6F, -1.4F, 0F, 0F, 0F, 0.8727F));
        PartDefinition p_antler_right_0 = p_nature_head_head.addOrReplaceChild("antler_right_0", CubeListBuilder.create()
                .texOffs(144, 261).addBox(-1F, -3F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.3F, 0F, -0.3F)),
                PartPose.offsetAndRotation(-3F, -8.7F, -0.8F, -0.2443F, 0F, -0.5236F));
        PartDefinition p_antler_right_1 = p_antler_right_0.addOrReplaceChild("antler_right_1", CubeListBuilder.create()
                .texOffs(153, 261).addBox(-1F, -3F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.4F, 0F, -0.4F)),
                PartPose.offsetAndRotation(0F, -2.75F, 0F, -0.384F, 0F, 0.1745F));
        PartDefinition p_antler_right_2 = p_antler_right_1.addOrReplaceChild("antler_right_2", CubeListBuilder.create()
                .texOffs(114, 298).addBox(-0.5F, -2.8F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.05F, -0.2F, -0.05F)),
                PartPose.offsetAndRotation(0F, -2.75F, 0F, -0.3491F, 0F, 0.1396F));
        PartDefinition p_antler_tine_right_1 = p_antler_right_1.addOrReplaceChild("antler_tine_right_1", CubeListBuilder.create()
                .texOffs(200, 319).addBox(-0.5F, -1.9F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.15F, -0.1F, -0.15F)),
                PartPose.offsetAndRotation(0F, -2.2F, 0F, 0.8378F, 0F, 0.2443F));
        PartDefinition p_antler_tine_right_0 = p_antler_right_0.addOrReplaceChild("antler_tine_right_0", CubeListBuilder.create()
                .texOffs(205, 319).addBox(-0.5F, -2F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1F, 0F, -0.1F)),
                PartPose.offsetAndRotation(0F, -2.4F, 0F, 0.8727F, 0F, -0.1745F));
        PartDefinition p_antler_leaf_right = p_antler_right_0.addOrReplaceChild("antler_leaf_right", CubeListBuilder.create()
                .texOffs(70, 267).addBox(-0.5F, -2.8F, -1F, 1F, 3F, 2F, new CubeDeformation(-0.35F, -0.2F, -0.1F)),
                PartPose.offsetAndRotation(-0.6F, -1.4F, 0F, 0F, 0F, -0.8727F));
        PartDefinition p_nature_crest_0_0 = p_nature_head_head.addOrReplaceChild("nature_crest_0_0", CubeListBuilder.create()
                .texOffs(0, 331).addBox(-0.5F, -0.94F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.06F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, -3.2F, -0.4538F, 0F, 0F));
        PartDefinition p_nature_crest_0_1 = p_nature_crest_0_0.addOrReplaceChild("nature_crest_0_1", CubeListBuilder.create()
                .texOffs(5, 331).addBox(-0.5F, -0.9F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.1F, -0.225F)),
                PartPose.offset(0F, -0.63F, 0F));
        PartDefinition p_nature_crest_1_0 = p_nature_head_head.addOrReplaceChild("nature_crest_1_0", CubeListBuilder.create()
                .texOffs(210, 319).addBox(-0.5F, -1.605F, -0.5F, 1F, 2F, 1F, new CubeDeformation(0F, -0.395F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, -0.6F, -0.4538F, 0F, 0F));
        PartDefinition p_nature_crest_1_1 = p_nature_crest_1_0.addOrReplaceChild("nature_crest_1_1", CubeListBuilder.create()
                .texOffs(215, 319).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.225F, -0.45F, -0.225F)),
                PartPose.offset(0F, -0.96F, 0F));
        PartDefinition p_nature_crest_2_0 = p_nature_head_head.addOrReplaceChild("nature_crest_2_0", CubeListBuilder.create()
                .texOffs(10, 331).addBox(-0.5F, -0.995F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.005F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, 2F, -0.4538F, 0F, 0F));
        PartDefinition p_nature_crest_2_1 = p_nature_crest_2_0.addOrReplaceChild("nature_crest_2_1", CubeListBuilder.create()
                .texOffs(15, 331).addBox(-0.5F, -0.95F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.05F, -0.225F)),
                PartPose.offset(0F, -0.74F, 0F));
        PartDefinition p_nature_chest_body = root.addOrReplaceChild("nature_chest_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_nature_frond_0 = p_nature_chest_body.addOrReplaceChild("nature_frond_0", CubeListBuilder.create()
                .texOffs(170, 236).addBox(-1.5F, -4.6F, -0.5F, 3F, 5F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.35F)),
                PartPose.offsetAndRotation(0F, 1F, 2.9F, -0.8378F, 0F, 0F));
        PartDefinition p_nature_frond_1 = p_nature_chest_body.addOrReplaceChild("nature_frond_1", CubeListBuilder.create()
                .texOffs(162, 261).addBox(-1.5F, -3.8F, -0.5F, 3F, 4F, 1F, new CubeDeformation(-0.4F, -0.2F, -0.35F)),
                PartPose.offsetAndRotation(0F, 4F, 2.9F, -0.8378F, 0F, 0F));
        PartDefinition p_nature_frond_2 = p_nature_chest_body.addOrReplaceChild("nature_frond_2", CubeListBuilder.create()
                .texOffs(54, 288).addBox(-1.5F, -3F, -0.5F, 3F, 3F, 1F, new CubeDeformation(-0.4F, 0F, -0.35F)),
                PartPose.offsetAndRotation(0F, 7F, 2.9F, -0.8378F, 0F, 0F));
        PartDefinition p_nature_chest_left_arm = root.addOrReplaceChild("nature_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_nature_pauldron_left_arm = p_nature_chest_left_arm.addOrReplaceChild("nature_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(0, 82).addBox(-3.3F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F))
                .texOffs(58, 115).addBox(-2F, -0.1F, -4F, 6F, 2F, 8F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.2094F));
        PartDefinition p_nature_claw_left_arm_0 = p_nature_pauldron_left_arm.addOrReplaceChild("nature_claw_left_arm_0", CubeListBuilder.create()
                .texOffs(63, 288).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.285F, -0.35F)),
                PartPose.offsetAndRotation(2.4F, -1.4F, 0F, 0F, 0F, 0.6981F));
        PartDefinition p_nature_claw_left_arm_1 = p_nature_claw_left_arm_0.addOrReplaceChild("nature_claw_left_arm_1", CubeListBuilder.create()
                .texOffs(220, 319).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.35F, -0.1425F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_nature_chest_right_arm = root.addOrReplaceChild("nature_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_nature_pauldron_right_arm = p_nature_chest_right_arm.addOrReplaceChild("nature_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(31, 82).addBox(-3.7F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F))
                .texOffs(87, 115).addBox(-4F, -0.1F, -4F, 6F, 2F, 8F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.2094F));
        PartDefinition p_nature_claw_right_arm_0 = p_nature_pauldron_right_arm.addOrReplaceChild("nature_claw_right_arm_0", CubeListBuilder.create()
                .texOffs(72, 288).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.285F, -0.35F)),
                PartPose.offsetAndRotation(-2.4F, -1.4F, 0F, 0F, 0F, -0.6981F));
        PartDefinition p_nature_claw_right_arm_1 = p_nature_claw_right_arm_0.addOrReplaceChild("nature_claw_right_arm_1", CubeListBuilder.create()
                .texOffs(225, 319).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.35F, -0.1425F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_nature_legs_body = root.addOrReplaceChild("nature_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_nature_tail_0 = p_nature_legs_body.addOrReplaceChild("nature_tail_0", CubeListBuilder.create()
                .texOffs(13, 229).addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F),
                PartPose.offsetAndRotation(0F, 10.2F, 2.2F, 0.5236F, 0F, 0F));
        PartDefinition p_nature_tail_1 = p_nature_tail_0.addOrReplaceChild("nature_tail_1", CubeListBuilder.create()
                .texOffs(26, 229).addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, 2.75F, 0F, 0.1745F, 0F, 0F));
        PartDefinition p_nature_tail_2 = p_nature_tail_1.addOrReplaceChild("nature_tail_2", CubeListBuilder.create()
                .texOffs(39, 229).addBox(-1.5F, -0.1F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.1F, -0.4F)),
                PartPose.offsetAndRotation(0F, 2.75F, 0F, 0.2094F, 0F, 0F));
        PartDefinition p_nature_tail_3 = p_nature_tail_2.addOrReplaceChild("nature_tail_3", CubeListBuilder.create()
                .texOffs(171, 261).addBox(-1F, -0.2F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.15F, -0.2F, -0.15F)),
                PartPose.offsetAndRotation(0F, 2.55F, 0F, 0.2094F, 0F, 0F));
        PartDefinition p_nature_tail_4 = p_nature_tail_3.addOrReplaceChild("nature_tail_4", CubeListBuilder.create()
                .texOffs(180, 261).addBox(-1F, -0.4F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.4F, -0.4F, -0.4F)),
                PartPose.offsetAndRotation(0F, 2.35F, 0F, 0.1745F, 0F, 0F));
        PartDefinition p_nature_tail_tip = p_nature_tail_4.addOrReplaceChild("nature_tail_tip", CubeListBuilder.create()
                .texOffs(22, 198).addBox(-0.5F, -0.4F, -1.5F, 1F, 5F, 3F, new CubeDeformation(-0.35F, -0.4F, -0.1F)),
                PartPose.offsetAndRotation(0F, 2.4F, 0F, 0.1396F, 0F, 0F));
        PartDefinition p_nature_legs_left_leg = root.addOrReplaceChild("nature_legs_left_leg", CubeListBuilder.create()
                .texOffs(11, 278).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_nature_legs_right_leg = root.addOrReplaceChild("nature_legs_right_leg", CubeListBuilder.create()
                .texOffs(22, 278).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_nature_feet_left_leg = root.addOrReplaceChild("nature_feet_left_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_nature_talon_left_leg_0 = p_nature_feet_left_leg.addOrReplaceChild("nature_talon_left_leg_0", CubeListBuilder.create()
                .texOffs(140, 307).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(-1.6F, 11.6F, -2.8F, 0.4887F, -0.2234F, 0F));
        PartDefinition p_nature_talon_left_leg_1 = p_nature_feet_left_leg.addOrReplaceChild("nature_talon_left_leg_1", CubeListBuilder.create()
                .texOffs(147, 307).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(0F, 11.6F, -2.8F, 0.4887F, 0F, 0F));
        PartDefinition p_nature_talon_left_leg_2 = p_nature_feet_left_leg.addOrReplaceChild("nature_talon_left_leg_2", CubeListBuilder.create()
                .texOffs(154, 307).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(1.6F, 11.6F, -2.8F, 0.4887F, 0.2234F, 0F));
        PartDefinition p_nature_feet_right_leg = root.addOrReplaceChild("nature_feet_right_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_nature_talon_right_leg_0 = p_nature_feet_right_leg.addOrReplaceChild("nature_talon_right_leg_0", CubeListBuilder.create()
                .texOffs(161, 307).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(-1.6F, 11.6F, -2.8F, 0.4887F, -0.2234F, 0F));
        PartDefinition p_nature_talon_right_leg_1 = p_nature_feet_right_leg.addOrReplaceChild("nature_talon_right_leg_1", CubeListBuilder.create()
                .texOffs(168, 307).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(0F, 11.6F, -2.8F, 0.4887F, 0F, 0F));
        PartDefinition p_nature_talon_right_leg_2 = p_nature_feet_right_leg.addOrReplaceChild("nature_talon_right_leg_2", CubeListBuilder.create()
                .texOffs(175, 307).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(1.6F, 11.6F, -2.8F, 0.4887F, 0.2234F, 0F));
        PartDefinition p_earth_head_head = root.addOrReplaceChild("earth_head_head", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_ram_horn_left_0 = p_earth_head_head.addOrReplaceChild("ram_horn_left_0", CubeListBuilder.create()
                .texOffs(52, 229).addBox(-1.5F, -2.8F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.1F, -0.2F, -0.1F)),
                PartPose.offsetAndRotation(5F, -8F, -0.6F, -0.4189F, 0F, 0.4887F));
        PartDefinition p_ram_horn_left_1 = p_ram_horn_left_0.addOrReplaceChild("ram_horn_left_1", CubeListBuilder.create()
                .texOffs(65, 229).addBox(-1.5F, -2.8F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.25F, -0.2F, -0.25F)),
                PartPose.offsetAndRotation(0F, -2.35F, 0F, -0.9599F, 0F, 0F));
        PartDefinition p_ram_horn_left_2 = p_ram_horn_left_1.addOrReplaceChild("ram_horn_left_2", CubeListBuilder.create()
                .texOffs(78, 229).addBox(-1.5F, -2.8F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(0F, -2.35F, 0F, -0.9599F, 0F, 0F));
        PartDefinition p_ram_horn_left_3 = p_ram_horn_left_2.addOrReplaceChild("ram_horn_left_3", CubeListBuilder.create()
                .texOffs(189, 261).addBox(-1F, -2.7F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.05F, -0.3F, -0.05F)),
                PartPose.offsetAndRotation(0F, -2.35F, 0F, -0.9599F, 0F, 0F));
        PartDefinition p_ram_horn_left_4 = p_ram_horn_left_3.addOrReplaceChild("ram_horn_left_4", CubeListBuilder.create()
                .texOffs(198, 261).addBox(-1F, -2.6F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.25F, -0.4F, -0.25F)),
                PartPose.offsetAndRotation(0F, -2.15F, 0F, -0.9599F, 0F, 0F));
        PartDefinition p_ram_horn_left_5 = p_ram_horn_left_4.addOrReplaceChild("ram_horn_left_5", CubeListBuilder.create()
                .texOffs(230, 319).addBox(-0.5F, -1.9F, -0.5F, 1F, 2F, 1F, new CubeDeformation(0F, -0.1F, 0F)),
                PartPose.offsetAndRotation(0F, -1.95F, 0F, -0.7854F, 0F, 0F));
        PartDefinition p_ram_horn_right_0 = p_earth_head_head.addOrReplaceChild("ram_horn_right_0", CubeListBuilder.create()
                .texOffs(91, 229).addBox(-1.5F, -2.8F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.1F, -0.2F, -0.1F)),
                PartPose.offsetAndRotation(-5F, -8F, -0.6F, -0.4189F, 0F, -0.4887F));
        PartDefinition p_ram_horn_right_1 = p_ram_horn_right_0.addOrReplaceChild("ram_horn_right_1", CubeListBuilder.create()
                .texOffs(104, 229).addBox(-1.5F, -2.8F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.25F, -0.2F, -0.25F)),
                PartPose.offsetAndRotation(0F, -2.35F, 0F, -0.9599F, 0F, 0F));
        PartDefinition p_ram_horn_right_2 = p_ram_horn_right_1.addOrReplaceChild("ram_horn_right_2", CubeListBuilder.create()
                .texOffs(117, 229).addBox(-1.5F, -2.8F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(0F, -2.35F, 0F, -0.9599F, 0F, 0F));
        PartDefinition p_ram_horn_right_3 = p_ram_horn_right_2.addOrReplaceChild("ram_horn_right_3", CubeListBuilder.create()
                .texOffs(207, 261).addBox(-1F, -2.7F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.05F, -0.3F, -0.05F)),
                PartPose.offsetAndRotation(0F, -2.35F, 0F, -0.9599F, 0F, 0F));
        PartDefinition p_ram_horn_right_4 = p_ram_horn_right_3.addOrReplaceChild("ram_horn_right_4", CubeListBuilder.create()
                .texOffs(216, 261).addBox(-1F, -2.6F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.25F, -0.4F, -0.25F)),
                PartPose.offsetAndRotation(0F, -2.15F, 0F, -0.9599F, 0F, 0F));
        PartDefinition p_ram_horn_right_5 = p_ram_horn_right_4.addOrReplaceChild("ram_horn_right_5", CubeListBuilder.create()
                .texOffs(235, 319).addBox(-0.5F, -1.9F, -0.5F, 1F, 2F, 1F, new CubeDeformation(0F, -0.1F, 0F)),
                PartPose.offsetAndRotation(0F, -1.95F, 0F, -0.7854F, 0F, 0F));
        PartDefinition p_earth_crest_0_0 = p_earth_head_head.addOrReplaceChild("earth_crest_0_0", CubeListBuilder.create()
                .texOffs(20, 331).addBox(-0.5F, -0.94F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.06F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, -3.2F, -0.4538F, 0F, 0F));
        PartDefinition p_earth_crest_0_1 = p_earth_crest_0_0.addOrReplaceChild("earth_crest_0_1", CubeListBuilder.create()
                .texOffs(25, 331).addBox(-0.5F, -0.9F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.1F, -0.225F)),
                PartPose.offset(0F, -0.63F, 0F));
        PartDefinition p_earth_crest_1_0 = p_earth_head_head.addOrReplaceChild("earth_crest_1_0", CubeListBuilder.create()
                .texOffs(240, 319).addBox(-0.5F, -1.605F, -0.5F, 1F, 2F, 1F, new CubeDeformation(0F, -0.395F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, -0.6F, -0.4538F, 0F, 0F));
        PartDefinition p_earth_crest_1_1 = p_earth_crest_1_0.addOrReplaceChild("earth_crest_1_1", CubeListBuilder.create()
                .texOffs(245, 319).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.225F, -0.45F, -0.225F)),
                PartPose.offset(0F, -0.96F, 0F));
        PartDefinition p_earth_crest_2_0 = p_earth_head_head.addOrReplaceChild("earth_crest_2_0", CubeListBuilder.create()
                .texOffs(30, 331).addBox(-0.5F, -0.995F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.005F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, 2F, -0.4538F, 0F, 0F));
        PartDefinition p_earth_crest_2_1 = p_earth_crest_2_0.addOrReplaceChild("earth_crest_2_1", CubeListBuilder.create()
                .texOffs(35, 331).addBox(-0.5F, -0.95F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.05F, -0.225F)),
                PartPose.offset(0F, -0.74F, 0F));
        PartDefinition p_earth_chest_body = root.addOrReplaceChild("earth_chest_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_earth_plate_0 = p_earth_chest_body.addOrReplaceChild("earth_plate_0", CubeListBuilder.create()
                .texOffs(185, 207).addBox(-0.5F, -3.6F, -1.5F, 1F, 4F, 3F, new CubeDeformation(-0.1F, -0.4F, -0.3F))
                .texOffs(182, 307).addBox(-0.5F, -4.1F, -1F, 1F, 1F, 2F, new CubeDeformation(-0.2F, 0F, -0.4F)),
                PartPose.offsetAndRotation(0F, 0.8F, 2.9F, -0.6981F, 0F, 0F));
        PartDefinition p_earth_plate_1 = p_earth_chest_body.addOrReplaceChild("earth_plate_1", CubeListBuilder.create()
                .texOffs(194, 207).addBox(-0.5F, -3.8F, -1.5F, 1F, 4F, 3F, new CubeDeformation(-0.1F, -0.2F, -0.3F))
                .texOffs(189, 307).addBox(-0.5F, -4.5F, -1F, 1F, 1F, 2F, new CubeDeformation(-0.2F, 0F, -0.4F)),
                PartPose.offsetAndRotation(0F, 4F, 2.9F, -0.6981F, 0F, 0F));
        PartDefinition p_earth_plate_2 = p_earth_chest_body.addOrReplaceChild("earth_plate_2", CubeListBuilder.create()
                .texOffs(179, 236).addBox(-0.5F, -2.9F, -1.5F, 1F, 3F, 3F, new CubeDeformation(-0.1F, -0.1F, -0.3F))
                .texOffs(196, 307).addBox(-0.5F, -3.7F, -1F, 1F, 1F, 2F, new CubeDeformation(-0.2F, 0F, -0.4F)),
                PartPose.offsetAndRotation(0F, 7.4F, 2.9F, -0.6981F, 0F, 0F));
        PartDefinition p_earth_chest_left_arm = root.addOrReplaceChild("earth_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_earth_pauldron_left_arm = p_earth_chest_left_arm.addOrReplaceChild("earth_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(62, 82).addBox(-3.3F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F))
                .texOffs(116, 115).addBox(-2F, -0.1F, -4F, 6F, 2F, 8F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.2094F));
        PartDefinition p_earth_claw_left_arm_0 = p_earth_pauldron_left_arm.addOrReplaceChild("earth_claw_left_arm_0", CubeListBuilder.create()
                .texOffs(81, 288).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.285F, -0.35F)),
                PartPose.offsetAndRotation(2.4F, -1.4F, 0F, 0F, 0F, 0.6981F));
        PartDefinition p_earth_claw_left_arm_1 = p_earth_claw_left_arm_0.addOrReplaceChild("earth_claw_left_arm_1", CubeListBuilder.create()
                .texOffs(250, 319).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.35F, -0.1425F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_earth_chest_right_arm = root.addOrReplaceChild("earth_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_earth_pauldron_right_arm = p_earth_chest_right_arm.addOrReplaceChild("earth_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(93, 82).addBox(-3.7F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F))
                .texOffs(145, 115).addBox(-4F, -0.1F, -4F, 6F, 2F, 8F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.2094F));
        PartDefinition p_earth_claw_right_arm_0 = p_earth_pauldron_right_arm.addOrReplaceChild("earth_claw_right_arm_0", CubeListBuilder.create()
                .texOffs(90, 288).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.285F, -0.35F)),
                PartPose.offsetAndRotation(-2.4F, -1.4F, 0F, 0F, 0F, -0.6981F));
        PartDefinition p_earth_claw_right_arm_1 = p_earth_claw_right_arm_0.addOrReplaceChild("earth_claw_right_arm_1", CubeListBuilder.create()
                .texOffs(0, 323).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.35F, -0.1425F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_earth_legs_body = root.addOrReplaceChild("earth_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_earth_tail_0 = p_earth_legs_body.addOrReplaceChild("earth_tail_0", CubeListBuilder.create()
                .texOffs(130, 229).addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F),
                PartPose.offsetAndRotation(0F, 10.2F, 2.2F, 0.5236F, 0F, 0F));
        PartDefinition p_earth_tail_1 = p_earth_tail_0.addOrReplaceChild("earth_tail_1", CubeListBuilder.create()
                .texOffs(143, 229).addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, 2.75F, 0F, 0.1745F, 0F, 0F));
        PartDefinition p_earth_tail_2 = p_earth_tail_1.addOrReplaceChild("earth_tail_2", CubeListBuilder.create()
                .texOffs(156, 229).addBox(-1.5F, -0.1F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.1F, -0.4F)),
                PartPose.offsetAndRotation(0F, 2.75F, 0F, 0.2094F, 0F, 0F));
        PartDefinition p_earth_tail_3 = p_earth_tail_2.addOrReplaceChild("earth_tail_3", CubeListBuilder.create()
                .texOffs(225, 261).addBox(-1F, -0.2F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.15F, -0.2F, -0.15F)),
                PartPose.offsetAndRotation(0F, 2.55F, 0F, 0.2094F, 0F, 0F));
        PartDefinition p_earth_tail_4 = p_earth_tail_3.addOrReplaceChild("earth_tail_4", CubeListBuilder.create()
                .texOffs(234, 261).addBox(-1F, -0.4F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.4F, -0.4F, -0.4F)),
                PartPose.offsetAndRotation(0F, 2.35F, 0F, 0.1745F, 0F, 0F));
        PartDefinition p_earth_tail_tip = p_earth_tail_4.addOrReplaceChild("earth_tail_tip", CubeListBuilder.create()
                .texOffs(194, 198).addBox(-2F, -0.1F, -2F, 4F, 3F, 4F, new CubeDeformation(-0.4F, -0.1F, -0.4F))
                .texOffs(203, 307).addBox(-1F, 0.2F, -2.15F, 2F, 2F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.25F)),
                PartPose.offsetAndRotation(0F, 2.4F, 0F, 0.1396F, 0F, 0F));
        PartDefinition p_earth_legs_left_leg = root.addOrReplaceChild("earth_legs_left_leg", CubeListBuilder.create()
                .texOffs(33, 278).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_earth_legs_right_leg = root.addOrReplaceChild("earth_legs_right_leg", CubeListBuilder.create()
                .texOffs(44, 278).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_earth_feet_left_leg = root.addOrReplaceChild("earth_feet_left_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_earth_talon_left_leg_0 = p_earth_feet_left_leg.addOrReplaceChild("earth_talon_left_leg_0", CubeListBuilder.create()
                .texOffs(210, 307).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(-1.6F, 11.6F, -2.8F, 0.4887F, -0.2234F, 0F));
        PartDefinition p_earth_talon_left_leg_1 = p_earth_feet_left_leg.addOrReplaceChild("earth_talon_left_leg_1", CubeListBuilder.create()
                .texOffs(217, 307).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(0F, 11.6F, -2.8F, 0.4887F, 0F, 0F));
        PartDefinition p_earth_talon_left_leg_2 = p_earth_feet_left_leg.addOrReplaceChild("earth_talon_left_leg_2", CubeListBuilder.create()
                .texOffs(224, 307).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(1.6F, 11.6F, -2.8F, 0.4887F, 0.2234F, 0F));
        PartDefinition p_earth_feet_right_leg = root.addOrReplaceChild("earth_feet_right_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_earth_talon_right_leg_0 = p_earth_feet_right_leg.addOrReplaceChild("earth_talon_right_leg_0", CubeListBuilder.create()
                .texOffs(231, 307).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(-1.6F, 11.6F, -2.8F, 0.4887F, -0.2234F, 0F));
        PartDefinition p_earth_talon_right_leg_1 = p_earth_feet_right_leg.addOrReplaceChild("earth_talon_right_leg_1", CubeListBuilder.create()
                .texOffs(238, 307).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(0F, 11.6F, -2.8F, 0.4887F, 0F, 0F));
        PartDefinition p_earth_talon_right_leg_2 = p_earth_feet_right_leg.addOrReplaceChild("earth_talon_right_leg_2", CubeListBuilder.create()
                .texOffs(245, 307).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(1.6F, 11.6F, -2.8F, 0.4887F, 0.2234F, 0F));
        PartDefinition p_water_head_head = root.addOrReplaceChild("water_head_head", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_head_fin_left = p_water_head_head.addOrReplaceChild("head_fin_left", CubeListBuilder.create(),
                PartPose.offsetAndRotation(5.3F, -4.6F, -1F, 0F, 0.2443F, 0F));
        PartDefinition p_head_fin_left_ray_0 = p_head_fin_left.addOrReplaceChild("head_fin_left_ray_0", CubeListBuilder.create()
                .texOffs(52, 207).addBox(-0.5F, -1F, -0.2F, 1F, 2F, 5F, new CubeDeformation(-0.25F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.1047F, 0F, 0F));
        PartDefinition p_head_fin_left_ray_1 = p_head_fin_left.addOrReplaceChild("head_fin_left_ray_1", CubeListBuilder.create()
                .texOffs(177, 189).addBox(-0.5F, -1F, -0.3F, 1F, 2F, 6F, new CubeDeformation(-0.25F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0.2443F, 0F, 0F));
        PartDefinition p_head_fin_left_ray_2 = p_head_fin_left.addOrReplaceChild("head_fin_left_ray_2", CubeListBuilder.create()
                .texOffs(65, 207).addBox(-0.5F, -1F, 0F, 1F, 2F, 5F, new CubeDeformation(-0.25F, -0.25F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0.5934F, 0F, 0F));
        PartDefinition p_head_fin_left_ray_3 = p_head_fin_left.addOrReplaceChild("head_fin_left_ray_3", CubeListBuilder.create()
                .texOffs(48, 236).addBox(-0.5F, -1F, -0.1F, 1F, 2F, 4F, new CubeDeformation(-0.25F, -0.35F, -0.1F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0.9425F, 0F, 0F));
        PartDefinition p_head_fin_right = p_water_head_head.addOrReplaceChild("head_fin_right", CubeListBuilder.create(),
                PartPose.offsetAndRotation(-5.3F, -4.6F, -1F, 0F, -0.2443F, 0F));
        PartDefinition p_head_fin_right_ray_0 = p_head_fin_right.addOrReplaceChild("head_fin_right_ray_0", CubeListBuilder.create()
                .texOffs(78, 207).addBox(-0.5F, -1F, -0.2F, 1F, 2F, 5F, new CubeDeformation(-0.25F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.1047F, 0F, 0F));
        PartDefinition p_head_fin_right_ray_1 = p_head_fin_right.addOrReplaceChild("head_fin_right_ray_1", CubeListBuilder.create()
                .texOffs(192, 189).addBox(-0.5F, -1F, -0.3F, 1F, 2F, 6F, new CubeDeformation(-0.25F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0.2443F, 0F, 0F));
        PartDefinition p_head_fin_right_ray_2 = p_head_fin_right.addOrReplaceChild("head_fin_right_ray_2", CubeListBuilder.create()
                .texOffs(91, 207).addBox(-0.5F, -1F, 0F, 1F, 2F, 5F, new CubeDeformation(-0.25F, -0.25F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0.5934F, 0F, 0F));
        PartDefinition p_head_fin_right_ray_3 = p_head_fin_right.addOrReplaceChild("head_fin_right_ray_3", CubeListBuilder.create()
                .texOffs(59, 236).addBox(-0.5F, -1F, -0.1F, 1F, 2F, 4F, new CubeDeformation(-0.25F, -0.35F, -0.1F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0.9425F, 0F, 0F));
        PartDefinition p_water_crest_0_0 = p_water_head_head.addOrReplaceChild("water_crest_0_0", CubeListBuilder.create()
                .texOffs(40, 331).addBox(-0.5F, -0.94F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.06F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, -3.2F, -0.4538F, 0F, 0F));
        PartDefinition p_water_crest_0_1 = p_water_crest_0_0.addOrReplaceChild("water_crest_0_1", CubeListBuilder.create()
                .texOffs(45, 331).addBox(-0.5F, -0.9F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.1F, -0.225F)),
                PartPose.offset(0F, -0.63F, 0F));
        PartDefinition p_water_crest_1_0 = p_water_head_head.addOrReplaceChild("water_crest_1_0", CubeListBuilder.create()
                .texOffs(5, 323).addBox(-0.5F, -1.605F, -0.5F, 1F, 2F, 1F, new CubeDeformation(0F, -0.395F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, -0.6F, -0.4538F, 0F, 0F));
        PartDefinition p_water_crest_1_1 = p_water_crest_1_0.addOrReplaceChild("water_crest_1_1", CubeListBuilder.create()
                .texOffs(10, 323).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.225F, -0.45F, -0.225F)),
                PartPose.offset(0F, -0.96F, 0F));
        PartDefinition p_water_crest_2_0 = p_water_head_head.addOrReplaceChild("water_crest_2_0", CubeListBuilder.create()
                .texOffs(50, 331).addBox(-0.5F, -0.995F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.005F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, 2F, -0.4538F, 0F, 0F));
        PartDefinition p_water_crest_2_1 = p_water_crest_2_0.addOrReplaceChild("water_crest_2_1", CubeListBuilder.create()
                .texOffs(55, 331).addBox(-0.5F, -0.95F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.05F, -0.225F)),
                PartPose.offset(0F, -0.74F, 0F));
        PartDefinition p_water_chest_body = root.addOrReplaceChild("water_chest_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_water_back_fin_0 = p_water_chest_body.addOrReplaceChild("water_back_fin_0", CubeListBuilder.create(),
                PartPose.offset(0F, 0.6F, 2.9F));
        PartDefinition p_water_back_fin_0_ray_0 = p_water_back_fin_0.addOrReplaceChild("water_back_fin_0_ray_0", CubeListBuilder.create()
                .texOffs(237, 189).addBox(-0.5F, -1.5F, -0.3F, 1F, 3F, 5F, new CubeDeformation(-0.25F, -0.4F, -0.3F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.6632F, 0F, 0F));
        PartDefinition p_water_back_fin_0_ray_1 = p_water_back_fin_0.addOrReplaceChild("water_back_fin_0_ray_1", CubeListBuilder.create()
                .texOffs(70, 236).addBox(-0.5F, -1F, -0.24F, 1F, 2F, 4F, new CubeDeformation(-0.25F, 0F, -0.24F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.1396F, 0F, 0F));
        PartDefinition p_water_back_fin_1 = p_water_chest_body.addOrReplaceChild("water_back_fin_1", CubeListBuilder.create(),
                PartPose.offset(0F, 3.4F, 2.9F));
        PartDefinition p_water_back_fin_1_ray_0 = p_water_back_fin_1.addOrReplaceChild("water_back_fin_1_ray_0", CubeListBuilder.create()
                .texOffs(163, 207).addBox(-0.5F, -1.5F, 0F, 1F, 3F, 4F, new CubeDeformation(-0.25F, -0.4F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.6632F, 0F, 0F));
        PartDefinition p_water_back_fin_1_ray_1 = p_water_back_fin_1.addOrReplaceChild("water_back_fin_1_ray_1", CubeListBuilder.create()
                .texOffs(81, 236).addBox(-0.5F, -1F, -0.4F, 1F, 2F, 4F, new CubeDeformation(-0.25F, 0F, -0.4F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.1396F, 0F, 0F));
        PartDefinition p_water_back_fin_2 = p_water_chest_body.addOrReplaceChild("water_back_fin_2", CubeListBuilder.create(),
                PartPose.offset(0F, 6.2F, 2.9F));
        PartDefinition p_water_back_fin_2_ray_0 = p_water_back_fin_2.addOrReplaceChild("water_back_fin_2_ray_0", CubeListBuilder.create()
                .texOffs(174, 207).addBox(-0.5F, -1.5F, -0.3F, 1F, 3F, 4F, new CubeDeformation(-0.25F, -0.4F, -0.3F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.6632F, 0F, 0F));
        PartDefinition p_water_back_fin_2_ray_1 = p_water_back_fin_2.addOrReplaceChild("water_back_fin_2_ray_1", CubeListBuilder.create()
                .texOffs(243, 261).addBox(-0.5F, -1F, -0.14F, 1F, 2F, 3F, new CubeDeformation(-0.25F, 0F, -0.14F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.1396F, 0F, 0F));
        PartDefinition p_water_back_fin_3 = p_water_chest_body.addOrReplaceChild("water_back_fin_3", CubeListBuilder.create(),
                PartPose.offset(0F, 9F, 2.9F));
        PartDefinition p_water_back_fin_3_ray_0 = p_water_back_fin_3.addOrReplaceChild("water_back_fin_3_ray_0", CubeListBuilder.create()
                .texOffs(188, 236).addBox(-0.5F, -1.5F, -0.2F, 1F, 3F, 3F, new CubeDeformation(-0.25F, -0.4F, -0.2F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.6632F, 0F, 0F));
        PartDefinition p_water_back_fin_3_ray_1 = p_water_back_fin_3.addOrReplaceChild("water_back_fin_3_ray_1", CubeListBuilder.create()
                .texOffs(0, 267).addBox(-0.5F, -1F, -0.46F, 1F, 2F, 3F, new CubeDeformation(-0.25F, 0F, -0.46F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.1396F, 0F, 0F));
        PartDefinition p_water_chest_left_arm = root.addOrReplaceChild("water_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_water_pauldron_left_arm = p_water_chest_left_arm.addOrReplaceChild("water_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(124, 82).addBox(-3.3F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F))
                .texOffs(174, 115).addBox(-2F, -0.1F, -4F, 6F, 2F, 8F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.2094F));
        PartDefinition p_water_claw_left_arm_0 = p_water_pauldron_left_arm.addOrReplaceChild("water_claw_left_arm_0", CubeListBuilder.create()
                .texOffs(99, 288).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.285F, -0.35F)),
                PartPose.offsetAndRotation(2.4F, -1.4F, 0F, 0F, 0F, 0.6981F));
        PartDefinition p_water_claw_left_arm_1 = p_water_claw_left_arm_0.addOrReplaceChild("water_claw_left_arm_1", CubeListBuilder.create()
                .texOffs(15, 323).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.35F, -0.1425F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_water_chest_right_arm = root.addOrReplaceChild("water_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_water_pauldron_right_arm = p_water_chest_right_arm.addOrReplaceChild("water_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(155, 82).addBox(-3.7F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F))
                .texOffs(203, 115).addBox(-4F, -0.1F, -4F, 6F, 2F, 8F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.2094F));
        PartDefinition p_water_claw_right_arm_0 = p_water_pauldron_right_arm.addOrReplaceChild("water_claw_right_arm_0", CubeListBuilder.create()
                .texOffs(108, 288).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.285F, -0.35F)),
                PartPose.offsetAndRotation(-2.4F, -1.4F, 0F, 0F, 0F, -0.6981F));
        PartDefinition p_water_claw_right_arm_1 = p_water_claw_right_arm_0.addOrReplaceChild("water_claw_right_arm_1", CubeListBuilder.create()
                .texOffs(20, 323).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.35F, -0.1425F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_water_legs_body = root.addOrReplaceChild("water_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_water_tail_0 = p_water_legs_body.addOrReplaceChild("water_tail_0", CubeListBuilder.create()
                .texOffs(169, 229).addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F),
                PartPose.offsetAndRotation(0F, 10.2F, 2.2F, 0.5236F, 0F, 0F));
        PartDefinition p_water_tail_1 = p_water_tail_0.addOrReplaceChild("water_tail_1", CubeListBuilder.create()
                .texOffs(182, 229).addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, 2.75F, 0F, 0.1745F, 0F, 0F));
        PartDefinition p_water_tail_2 = p_water_tail_1.addOrReplaceChild("water_tail_2", CubeListBuilder.create()
                .texOffs(195, 229).addBox(-1.5F, -0.1F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.1F, -0.4F)),
                PartPose.offsetAndRotation(0F, 2.75F, 0F, 0.2094F, 0F, 0F));
        PartDefinition p_water_tail_3 = p_water_tail_2.addOrReplaceChild("water_tail_3", CubeListBuilder.create()
                .texOffs(9, 267).addBox(-1F, -0.2F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.15F, -0.2F, -0.15F)),
                PartPose.offsetAndRotation(0F, 2.55F, 0F, 0.2094F, 0F, 0F));
        PartDefinition p_water_tail_4 = p_water_tail_3.addOrReplaceChild("water_tail_4", CubeListBuilder.create()
                .texOffs(18, 267).addBox(-1F, -0.4F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.4F, -0.4F, -0.4F)),
                PartPose.offsetAndRotation(0F, 2.35F, 0F, 0.1745F, 0F, 0F));
        PartDefinition p_water_tail_tip = p_water_tail_4.addOrReplaceChild("water_tail_tip", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0F, 2.4F, 0F, 0.1396F, 0F, 0F));
        PartDefinition p_water_tail_fin = p_water_tail_tip.addOrReplaceChild("water_tail_fin", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0F, 0.6F, 0F, 1.5708F, 0F, 0F));
        PartDefinition p_water_tail_fin_ray_0 = p_water_tail_fin.addOrReplaceChild("water_tail_fin_ray_0", CubeListBuilder.create()
                .texOffs(92, 236).addBox(-0.5F, -1F, -0.2F, 1F, 2F, 4F, new CubeDeformation(-0.25F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.6981F, 0F, 0F));
        PartDefinition p_water_tail_fin_ray_1 = p_water_tail_fin.addOrReplaceChild("water_tail_fin_ray_1", CubeListBuilder.create()
                .texOffs(103, 236).addBox(-0.5F, -1F, 0F, 1F, 2F, 4F, new CubeDeformation(-0.25F, -0.2F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.1745F, 0F, 0F));
        PartDefinition p_water_tail_fin_ray_2 = p_water_tail_fin.addOrReplaceChild("water_tail_fin_ray_2", CubeListBuilder.create()
                .texOffs(114, 236).addBox(-0.5F, -1F, -0.2F, 1F, 2F, 4F, new CubeDeformation(-0.25F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0.3491F, 0F, 0F));
        PartDefinition p_water_legs_left_leg = root.addOrReplaceChild("water_legs_left_leg", CubeListBuilder.create()
                .texOffs(55, 278).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_water_legs_right_leg = root.addOrReplaceChild("water_legs_right_leg", CubeListBuilder.create()
                .texOffs(66, 278).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_water_feet_left_leg = root.addOrReplaceChild("water_feet_left_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_water_talon_left_leg_0 = p_water_feet_left_leg.addOrReplaceChild("water_talon_left_leg_0", CubeListBuilder.create()
                .texOffs(0, 311).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(-1.6F, 11.6F, -2.8F, 0.4887F, -0.2234F, 0F));
        PartDefinition p_water_talon_left_leg_1 = p_water_feet_left_leg.addOrReplaceChild("water_talon_left_leg_1", CubeListBuilder.create()
                .texOffs(7, 311).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(0F, 11.6F, -2.8F, 0.4887F, 0F, 0F));
        PartDefinition p_water_talon_left_leg_2 = p_water_feet_left_leg.addOrReplaceChild("water_talon_left_leg_2", CubeListBuilder.create()
                .texOffs(14, 311).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(1.6F, 11.6F, -2.8F, 0.4887F, 0.2234F, 0F));
        PartDefinition p_water_feet_right_leg = root.addOrReplaceChild("water_feet_right_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_water_talon_right_leg_0 = p_water_feet_right_leg.addOrReplaceChild("water_talon_right_leg_0", CubeListBuilder.create()
                .texOffs(21, 311).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(-1.6F, 11.6F, -2.8F, 0.4887F, -0.2234F, 0F));
        PartDefinition p_water_talon_right_leg_1 = p_water_feet_right_leg.addOrReplaceChild("water_talon_right_leg_1", CubeListBuilder.create()
                .texOffs(28, 311).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(0F, 11.6F, -2.8F, 0.4887F, 0F, 0F));
        PartDefinition p_water_talon_right_leg_2 = p_water_feet_right_leg.addOrReplaceChild("water_talon_right_leg_2", CubeListBuilder.create()
                .texOffs(35, 311).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(1.6F, 11.6F, -2.8F, 0.4887F, 0.2234F, 0F));
        PartDefinition p_crown_head_head = root.addOrReplaceChild("crown_head_head", CubeListBuilder.create()
                .texOffs(223, 298).addBox(-5.5F, -10F, -5.5F, 11F, 2F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.1F))
                .texOffs(0, 303).addBox(-5.5F, -10F, 4.5F, 11F, 2F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.1F))
                .texOffs(143, 21).addBox(-5.5F, -10F, -5F, 1F, 2F, 10F, new CubeDeformation(-0.1F, -0.1F, -0.4F))
                .texOffs(166, 21).addBox(4.5F, -10F, -5F, 1F, 2F, 10F, new CubeDeformation(-0.1F, -0.1F, -0.4F))
                .texOffs(42, 311).addBox(-1F, -10F, -6.1F, 2F, 2F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.2F))
                .texOffs(60, 331).addBox(-4F, -9.5F, -6.05F, 1F, 1F, 1F, new CubeDeformation(0F, -0.1F, -0.25F))
                .texOffs(65, 331).addBox(3F, -9.5F, -6.05F, 1F, 1F, 1F, new CubeDeformation(0F, -0.1F, -0.25F))
                .texOffs(49, 311).addBox(-6.1F, -9.5F, -1F, 1F, 1F, 2F, new CubeDeformation(-0.2F, 0F, -0.4F))
                .texOffs(56, 311).addBox(5.1F, -9.5F, -1F, 1F, 1F, 2F, new CubeDeformation(-0.2F, 0F, -0.4F))
                .texOffs(142, 327).addBox(-1F, -9.5F, 5.1F, 2F, 1F, 1F, new CubeDeformation(-0.4F, 0F, -0.2F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_crown_point_0_0 = p_crown_head_head.addOrReplaceChild("crown_point_0_0", CubeListBuilder.create()
                .texOffs(63, 311).addBox(-1F, -1.96F, -0.5F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.04F, -0.1F)),
                PartPose.offset(0F, -9.7F, -5.4F));
        PartDefinition p_crown_point_0_1 = p_crown_point_0_0.addOrReplaceChild("crown_point_0_1", CubeListBuilder.create()
                .texOffs(25, 323).addBox(-0.5F, -1.8F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.15F, -0.2F, -0.25F))
                .texOffs(70, 331).addBox(-0.5F, -2.4F, -0.5F, 1F, 1F, 1F),
                PartPose.offset(0F, -1.67F, 0F));
        PartDefinition p_crown_point_1_0 = p_crown_head_head.addOrReplaceChild("crown_point_1_0", CubeListBuilder.create()
                .texOffs(70, 311).addBox(-1F, -1.66F, -0.5F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.34F, -0.1F)),
                PartPose.offset(-3.2F, -9.7F, -5.4F));
        PartDefinition p_crown_point_1_1 = p_crown_point_1_0.addOrReplaceChild("crown_point_1_1", CubeListBuilder.create()
                .texOffs(30, 323).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.15F, -0.45F, -0.25F))
                .texOffs(75, 331).addBox(-0.5F, -1.9F, -0.5F, 1F, 1F, 1F),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_crown_point_2_0 = p_crown_head_head.addOrReplaceChild("crown_point_2_0", CubeListBuilder.create()
                .texOffs(77, 311).addBox(-1F, -1.66F, -0.5F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.34F, -0.1F)),
                PartPose.offset(3.2F, -9.7F, -5.4F));
        PartDefinition p_crown_point_2_1 = p_crown_point_2_0.addOrReplaceChild("crown_point_2_1", CubeListBuilder.create()
                .texOffs(35, 323).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.15F, -0.45F, -0.25F))
                .texOffs(80, 331).addBox(-0.5F, -1.9F, -0.5F, 1F, 1F, 1F),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_crown_point_3_0 = p_crown_head_head.addOrReplaceChild("crown_point_3_0", CubeListBuilder.create()
                .texOffs(84, 311).addBox(-1F, -1.72F, -0.5F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.28F, -0.1F)),
                PartPose.offset(0F, -9.7F, 5.4F));
        PartDefinition p_crown_point_3_1 = p_crown_point_3_0.addOrReplaceChild("crown_point_3_1", CubeListBuilder.create()
                .texOffs(40, 323).addBox(-0.5F, -1.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.15F, -0.4F, -0.25F))
                .texOffs(85, 331).addBox(-0.5F, -2F, -0.5F, 1F, 1F, 1F),
                PartPose.offset(0F, -1.19F, 0F));
        PartDefinition p_crown_point_4_0 = p_crown_head_head.addOrReplaceChild("crown_point_4_0", CubeListBuilder.create()
                .texOffs(91, 311).addBox(-1F, -1.54F, -0.5F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.46F, -0.1F)),
                PartPose.offset(-3.2F, -9.7F, 5.4F));
        PartDefinition p_crown_point_4_1 = p_crown_point_4_0.addOrReplaceChild("crown_point_4_1", CubeListBuilder.create()
                .texOffs(90, 331).addBox(-0.5F, -0.95F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.15F, -0.05F, -0.25F))
                .texOffs(95, 331).addBox(-0.5F, -1.7F, -0.5F, 1F, 1F, 1F),
                PartPose.offset(0F, -0.83F, 0F));
        PartDefinition p_crown_point_5_0 = p_crown_head_head.addOrReplaceChild("crown_point_5_0", CubeListBuilder.create()
                .texOffs(98, 311).addBox(-1F, -1.54F, -0.5F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.46F, -0.1F)),
                PartPose.offset(3.2F, -9.7F, 5.4F));
        PartDefinition p_crown_point_5_1 = p_crown_point_5_0.addOrReplaceChild("crown_point_5_1", CubeListBuilder.create()
                .texOffs(100, 331).addBox(-0.5F, -0.95F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.15F, -0.05F, -0.25F))
                .texOffs(105, 331).addBox(-0.5F, -1.7F, -0.5F, 1F, 1F, 1F),
                PartPose.offset(0F, -0.83F, 0F));
        PartDefinition p_crown_point_6_0 = p_crown_head_head.addOrReplaceChild("crown_point_6_0", CubeListBuilder.create()
                .texOffs(117, 288).addBox(-1F, -1.6F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.4F, -0.3F)),
                PartPose.offset(-5.4F, -9.7F, -1.8F));
        PartDefinition p_crown_point_6_1 = p_crown_point_6_0.addOrReplaceChild("crown_point_6_1", CubeListBuilder.create()
                .texOffs(110, 331).addBox(-0.5F, -1F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.15F, 0F, -0.15F))
                .texOffs(115, 331).addBox(-0.5F, -1.8F, -0.5F, 1F, 1F, 1F),
                PartPose.offset(0F, -0.95F, 0F));
        PartDefinition p_crown_point_7_0 = p_crown_head_head.addOrReplaceChild("crown_point_7_0", CubeListBuilder.create()
                .texOffs(126, 288).addBox(-1F, -1.6F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.4F, -0.3F)),
                PartPose.offset(-5.4F, -9.7F, 1.8F));
        PartDefinition p_crown_point_7_1 = p_crown_point_7_0.addOrReplaceChild("crown_point_7_1", CubeListBuilder.create()
                .texOffs(120, 331).addBox(-0.5F, -1F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.15F, 0F, -0.15F))
                .texOffs(125, 331).addBox(-0.5F, -1.8F, -0.5F, 1F, 1F, 1F),
                PartPose.offset(0F, -0.95F, 0F));
        PartDefinition p_crown_point_8_0 = p_crown_head_head.addOrReplaceChild("crown_point_8_0", CubeListBuilder.create()
                .texOffs(135, 288).addBox(-1F, -1.6F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.4F, -0.3F)),
                PartPose.offset(5.4F, -9.7F, -1.8F));
        PartDefinition p_crown_point_8_1 = p_crown_point_8_0.addOrReplaceChild("crown_point_8_1", CubeListBuilder.create()
                .texOffs(130, 331).addBox(-0.5F, -1F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.15F, 0F, -0.15F))
                .texOffs(135, 331).addBox(-0.5F, -1.8F, -0.5F, 1F, 1F, 1F),
                PartPose.offset(0F, -0.95F, 0F));
        PartDefinition p_crown_point_9_0 = p_crown_head_head.addOrReplaceChild("crown_point_9_0", CubeListBuilder.create()
                .texOffs(144, 288).addBox(-1F, -1.6F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.4F, -0.3F)),
                PartPose.offset(5.4F, -9.7F, 1.8F));
        PartDefinition p_crown_point_9_1 = p_crown_point_9_0.addOrReplaceChild("crown_point_9_1", CubeListBuilder.create()
                .texOffs(140, 331).addBox(-0.5F, -1F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.15F, 0F, -0.15F))
                .texOffs(145, 331).addBox(-0.5F, -1.8F, -0.5F, 1F, 1F, 1F),
                PartPose.offset(0F, -0.95F, 0F));
        PartDefinition p_leather_head_head = root.addOrReplaceChild("leather_head_head", CubeListBuilder.create()
                .texOffs(160, 249).addBox(4.2F, -7.5F, -0.1F, 2F, 2F, 3F, new CubeDeformation(-0.4F, -0.1F, -0.3F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_leather_feather = p_leather_head_head.addOrReplaceChild("leather_feather", CubeListBuilder.create()
                .texOffs(223, 21).addBox(-0.5F, -8.6F, -1.5F, 1F, 9F, 3F, new CubeDeformation(-0.2F, -0.4F, -0.4F)),
                PartPose.offsetAndRotation(5F, -6.4F, 1.4F, -0.5236F, 0F, 0.2793F));
        PartDefinition p_leather_chest_body = root.addOrReplaceChild("leather_chest_body", CubeListBuilder.create()
                .texOffs(0, 35).addBox(-6F, -1.9F, -4F, 12F, 3F, 8F, new CubeDeformation(-0.25F, 0.05F, -0.15F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_leather_chest_left_arm = root.addOrReplaceChild("leather_chest_left_arm", CubeListBuilder.create()
                .texOffs(0, 126).addBox(-2.45F, -4.4F, -3.5F, 7F, 3F, 7F, new CubeDeformation(0.05F, -0.3F, 0.1F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_leather_chest_right_arm = root.addOrReplaceChild("leather_chest_right_arm", CubeListBuilder.create()
                .texOffs(29, 126).addBox(-4.55F, -4.4F, -3.5F, 7F, 3F, 7F, new CubeDeformation(0.05F, -0.3F, 0.1F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_leather_legs_body = root.addOrReplaceChild("leather_legs_body", CubeListBuilder.create()
                .texOffs(26, 179).addBox(-5F, 10.2F, -3F, 10F, 2F, 6F, new CubeDeformation(-0.1F, -0.4F, -0.1F))
                .texOffs(171, 249).addBox(2.1F, 10.7F, -4F, 3F, 3F, 2F, new CubeDeformation(-0.3F, -0.2F, -0.4F))
                .texOffs(105, 311).addBox(-4.5F, 10.2F, -3.5F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.2F, -0.2F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_leather_legs_left_leg = root.addOrReplaceChild("leather_legs_left_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_leather_legs_right_leg = root.addOrReplaceChild("leather_legs_right_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_leather_feet_left_leg = root.addOrReplaceChild("leather_feet_left_leg", CubeListBuilder.create()
                .texOffs(214, 35).addBox(-4F, 6.6F, -4F, 8F, 3F, 8F, new CubeDeformation(-0.4F, 0F, -0.4F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_leather_feet_right_leg = root.addOrReplaceChild("leather_feet_right_leg", CubeListBuilder.create()
                .texOffs(0, 47).addBox(-4F, 6.6F, -4F, 8F, 3F, 8F, new CubeDeformation(-0.4F, 0F, -0.4F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_chainmail_head_head = root.addOrReplaceChild("chainmail_head_head", CubeListBuilder.create()
                .texOffs(50, 198).addBox(-5.5F, -3.9F, 4.6F, 11F, 6F, 1F, new CubeDeformation(-0.2F, -0.3F, -0.2F))
                .texOffs(99, 0).addBox(-5.8F, -3.9F, -4.7F, 1F, 6F, 10F, new CubeDeformation(-0.2F, -0.3F, -0.3F))
                .texOffs(122, 0).addBox(4.8F, -3.9F, -4.7F, 1F, 6F, 10F, new CubeDeformation(-0.2F, -0.3F, -0.3F))
                .texOffs(98, 267).addBox(-0.5F, -6F, -5.85F, 1F, 4F, 1F, new CubeDeformation(0F, 0F, -0.15F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_chainmail_chest_body = root.addOrReplaceChild("chainmail_chest_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_chainmail_chest_left_arm = root.addOrReplaceChild("chainmail_chest_left_arm", CubeListBuilder.create()
                .texOffs(59, 179).addBox(-2.4F, -3.7F, -3.5F, 7F, 1F, 7F, new CubeDeformation(-0.2F, 0F, 0F))
                .texOffs(189, 21).addBox(3.7F, -3.6F, -3.5F, 1F, 5F, 7F, new CubeDeformation(-0.2F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_chainmail_chest_right_arm = root.addOrReplaceChild("chainmail_chest_right_arm", CubeListBuilder.create()
                .texOffs(88, 179).addBox(-4.6F, -3.7F, -3.5F, 7F, 1F, 7F, new CubeDeformation(-0.2F, 0F, 0F))
                .texOffs(206, 21).addBox(-4.7F, -3.6F, -3.5F, 1F, 5F, 7F, new CubeDeformation(-0.2F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_chainmail_legs_body = root.addOrReplaceChild("chainmail_legs_body", CubeListBuilder.create()
                .texOffs(213, 207).addBox(-5F, 9.8F, -3.2F, 10F, 5F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(0, 215).addBox(-5F, 9.8F, 2.2F, 10F, 5F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.2F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_chainmail_legs_left_leg = root.addOrReplaceChild("chainmail_legs_left_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_chainmail_legs_right_leg = root.addOrReplaceChild("chainmail_legs_right_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_iron_head_head = root.addOrReplaceChild("iron_head_head", CubeListBuilder.create()
                .texOffs(233, 236).addBox(-1F, -6.7F, -5.9F, 2F, 5F, 1F, new CubeDeformation(-0.4F, -0.3F, -0.1F))
                .texOffs(99, 47).addBox(-1F, -10.45F, -4.5F, 2F, 2F, 9F, new CubeDeformation(-0.4F, -0.45F, -0.1F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_iron_chest_body = root.addOrReplaceChild("iron_chest_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_iron_chest_left_arm = root.addOrReplaceChild("iron_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_iron_pauldron_left_arm = p_iron_chest_left_arm.addOrReplaceChild("iron_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(110, 148).addBox(-3.2F, -1.5F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.2F, -0.1F, 0F))
                .texOffs(58, 126).addBox(-1.9F, 0.2F, -4F, 6F, 2F, 8F, new CubeDeformation(-0.1F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.2094F));
        PartDefinition p_iron_chest_right_arm = root.addOrReplaceChild("iron_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_iron_pauldron_right_arm = p_iron_chest_right_arm.addOrReplaceChild("iron_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(139, 148).addBox(-3.8F, -1.5F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.2F, -0.1F, 0F))
                .texOffs(87, 126).addBox(-4.1F, 0.2F, -4F, 6F, 2F, 8F, new CubeDeformation(-0.1F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.2094F));
        PartDefinition p_iron_legs_body = root.addOrReplaceChild("iron_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_iron_legs_left_leg = root.addOrReplaceChild("iron_legs_left_leg", CubeListBuilder.create()
                .texOffs(77, 278).addBox(-2F, 5F, -3.2F, 4F, 3F, 1F, new CubeDeformation(-0.1F, -0.1F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_iron_legs_right_leg = root.addOrReplaceChild("iron_legs_right_leg", CubeListBuilder.create()
                .texOffs(88, 278).addBox(-2F, 5F, -3.2F, 4F, 3F, 1F, new CubeDeformation(-0.1F, -0.1F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_iron_feet_left_leg = root.addOrReplaceChild("iron_feet_left_leg", CubeListBuilder.create()
                .texOffs(129, 243).addBox(-2.5F, 10.25F, -4.35F, 5F, 3F, 2F, new CubeDeformation(-0.1F, -0.35F, -0.35F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_iron_feet_right_leg = root.addOrReplaceChild("iron_feet_right_leg", CubeListBuilder.create()
                .texOffs(144, 243).addBox(-2.5F, 10.25F, -4.35F, 5F, 3F, 2F, new CubeDeformation(-0.1F, -0.35F, -0.35F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_gold_head_head = root.addOrReplaceChild("gold_head_head", CubeListBuilder.create()
                .texOffs(112, 311).addBox(-1F, -7.2F, -5.85F, 2F, 2F, 1F, new CubeDeformation(0F, 0F, -0.15F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_gold_ray_0_0 = p_gold_head_head.addOrReplaceChild("gold_ray_0_0", CubeListBuilder.create()
                .texOffs(153, 288).addBox(-1F, -1.935F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.065F, -0.35F)),
                PartPose.offsetAndRotation(0F, -9.2F, -1F, -0.384F, 0F, -0.5236F));
        PartDefinition p_gold_ray_0_1 = p_gold_ray_0_0.addOrReplaceChild("gold_ray_0_1", CubeListBuilder.create()
                .texOffs(45, 323).addBox(-0.5F, -1.85F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.15F, -0.1425F)),
                PartPose.offset(0F, -1.62F, 0F));
        PartDefinition p_gold_ray_1_0 = p_gold_head_head.addOrReplaceChild("gold_ray_1_0", CubeListBuilder.create()
                .texOffs(27, 267).addBox(-1F, -2.765F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.35F, -0.235F, -0.35F)),
                PartPose.offsetAndRotation(0F, -9.2F, -1F, -0.1745F, 0F, 0F));
        PartDefinition p_gold_ray_1_1 = p_gold_ray_1_0.addOrReplaceChild("gold_ray_1_1", CubeListBuilder.create()
                .texOffs(119, 298).addBox(-0.5F, -2.65F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.1425F, -0.35F, -0.1425F)),
                PartPose.offset(0F, -2.28F, 0F));
        PartDefinition p_gold_ray_2_0 = p_gold_head_head.addOrReplaceChild("gold_ray_2_0", CubeListBuilder.create()
                .texOffs(162, 288).addBox(-1F, -1.935F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.065F, -0.35F)),
                PartPose.offsetAndRotation(0F, -9.2F, -1F, 0.0349F, 0F, 0.5236F));
        PartDefinition p_gold_ray_2_1 = p_gold_ray_2_0.addOrReplaceChild("gold_ray_2_1", CubeListBuilder.create()
                .texOffs(50, 323).addBox(-0.5F, -1.85F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.15F, -0.1425F)),
                PartPose.offset(0F, -1.62F, 0F));
        PartDefinition p_gold_chest_body = root.addOrReplaceChild("gold_chest_body", CubeListBuilder.create()
                .texOffs(182, 249).addBox(-2F, 2.2F, -3.85F, 4F, 4F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.15F))
                .texOffs(119, 311).addBox(-1F, 3.2F, -4.35F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.3F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_gold_chest_left_arm = root.addOrReplaceChild("gold_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_gold_pauldron_left_arm = p_gold_chest_left_arm.addOrReplaceChild("gold_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(186, 82).addBox(-3.2F, -1.6F, -4F, 7F, 2F, 8F, new CubeDeformation(0F, 0F, -0.4F))
                .texOffs(217, 82).addBox(-2.4F, 0.1F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.4F, -0.2F, -0.3F))
                .texOffs(88, 303).addBox(1.1F, -2.3F, -1F, 2F, 1F, 2F, new CubeDeformation(-0.3F, -0.1F, -0.3F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.2443F));
        PartDefinition p_gold_chest_right_arm = root.addOrReplaceChild("gold_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_gold_pauldron_right_arm = p_gold_chest_right_arm.addOrReplaceChild("gold_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(0, 93).addBox(-3.8F, -1.6F, -4F, 7F, 2F, 8F, new CubeDeformation(0F, 0F, -0.4F))
                .texOffs(31, 93).addBox(-4.6F, 0.1F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.4F, -0.2F, -0.3F))
                .texOffs(97, 303).addBox(-3.1F, -2.3F, -1F, 2F, 1F, 2F, new CubeDeformation(-0.3F, -0.1F, -0.3F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.2443F));
        PartDefinition p_gold_legs_body = root.addOrReplaceChild("gold_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_gold_legs_left_leg = root.addOrReplaceChild("gold_legs_left_leg", CubeListBuilder.create()
                .texOffs(226, 215).addBox(-3F, -0.9F, -3.3F, 6F, 5F, 1F, new CubeDeformation(-0.3F, -0.3F, 0F))
                .texOffs(99, 278).addBox(-2F, 5F, -3.2F, 4F, 3F, 1F, new CubeDeformation(-0.1F, -0.1F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_gold_legs_right_leg = root.addOrReplaceChild("gold_legs_right_leg", CubeListBuilder.create()
                .texOffs(241, 215).addBox(-3F, -0.9F, -3.3F, 6F, 5F, 1F, new CubeDeformation(-0.3F, -0.3F, 0F))
                .texOffs(110, 278).addBox(-2F, 5F, -3.2F, 4F, 3F, 1F, new CubeDeformation(-0.1F, -0.1F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_gold_feet_left_leg = root.addOrReplaceChild("gold_feet_left_leg", CubeListBuilder.create()
                .texOffs(159, 243).addBox(-2.5F, 10.25F, -4.35F, 5F, 3F, 2F, new CubeDeformation(-0.1F, -0.35F, -0.35F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_gold_feet_right_leg = root.addOrReplaceChild("gold_feet_right_leg", CubeListBuilder.create()
                .texOffs(174, 243).addBox(-2.5F, 10.25F, -4.35F, 5F, 3F, 2F, new CubeDeformation(-0.1F, -0.35F, -0.35F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_diamond_head_head = root.addOrReplaceChild("diamond_head_head", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_diamond_crest_0_0 = p_diamond_head_head.addOrReplaceChild("diamond_crest_0_0", CubeListBuilder.create()
                .texOffs(171, 288).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.2F, -0.285F, -0.2F)),
                PartPose.offsetAndRotation(0F, -9.2F, -3F, -0.3491F, 0F, 0F));
        PartDefinition p_diamond_crest_0_1 = p_diamond_crest_0_0.addOrReplaceChild("diamond_crest_0_1", CubeListBuilder.create()
                .texOffs(55, 323).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.06F, -0.35F, -0.06F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_diamond_crest_1_0 = p_diamond_head_head.addOrReplaceChild("diamond_crest_1_0", CubeListBuilder.create()
                .texOffs(180, 288).addBox(-1F, -1.99F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.2F, -0.01F, -0.2F)),
                PartPose.offsetAndRotation(0F, -9.2F, -0.4F, -0.3491F, 0F, 0F));
        PartDefinition p_diamond_crest_1_1 = p_diamond_crest_1_0.addOrReplaceChild("diamond_crest_1_1", CubeListBuilder.create()
                .texOffs(60, 323).addBox(-0.5F, -1.9F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.06F, -0.1F, -0.06F)),
                PartPose.offset(0F, -1.73F, 0F));
        PartDefinition p_diamond_crest_2_0 = p_diamond_head_head.addOrReplaceChild("diamond_crest_2_0", CubeListBuilder.create()
                .texOffs(189, 288).addBox(-1F, -1.77F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.2F, -0.23F, -0.2F)),
                PartPose.offsetAndRotation(0F, -9.2F, 2.2F, -0.3491F, 0F, 0F));
        PartDefinition p_diamond_crest_2_1 = p_diamond_crest_2_0.addOrReplaceChild("diamond_crest_2_1", CubeListBuilder.create()
                .texOffs(65, 323).addBox(-0.5F, -1.7F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.06F, -0.3F, -0.06F)),
                PartPose.offset(0F, -1.29F, 0F));
        PartDefinition p_diamond_chest_body = root.addOrReplaceChild("diamond_chest_body", CubeListBuilder.create()
                .texOffs(28, 298).addBox(-1F, 1.8F, -3.9F, 2F, 3F, 1F, new CubeDeformation(0F, -0.2F, -0.1F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_diamond_chest_left_arm = root.addOrReplaceChild("diamond_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_diamond_pauldron_left_arm = p_diamond_chest_left_arm.addOrReplaceChild("diamond_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(168, 148).addBox(-3.2F, -1.6F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.2F, -0.2F, 0F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.1745F));
        PartDefinition p_diamond_shoulder_left_arm_0_0 = p_diamond_pauldron_left_arm.addOrReplaceChild("diamond_shoulder_left_arm_0_0", CubeListBuilder.create()
                .texOffs(198, 288).addBox(-1F, -1.88F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.25F, -0.12F, -0.25F)),
                PartPose.offsetAndRotation(1.8F, -1.3F, -1F, 0.1396F, 0F, 0.4189F));
        PartDefinition p_diamond_shoulder_left_arm_0_1 = p_diamond_shoulder_left_arm_0_0.addOrReplaceChild("diamond_shoulder_left_arm_0_1", CubeListBuilder.create()
                .texOffs(70, 323).addBox(-0.5F, -1.8F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.0875F, -0.2F, -0.0875F)),
                PartPose.offset(0F, -1.51F, 0F));
        PartDefinition p_diamond_shoulder_left_arm_1_0 = p_diamond_pauldron_left_arm.addOrReplaceChild("diamond_shoulder_left_arm_1_0", CubeListBuilder.create()
                .texOffs(207, 288).addBox(-1F, -1.605F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.395F, -0.45F)),
                PartPose.offsetAndRotation(2.6F, -1.3F, 1.6F, -0.1745F, 0F, 0.6632F));
        PartDefinition p_diamond_shoulder_left_arm_1_1 = p_diamond_shoulder_left_arm_1_0.addOrReplaceChild("diamond_shoulder_left_arm_1_1", CubeListBuilder.create()
                .texOffs(75, 323).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1975F, -0.45F, -0.1975F)),
                PartPose.offset(0F, -0.96F, 0F));
        PartDefinition p_diamond_chest_right_arm = root.addOrReplaceChild("diamond_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_diamond_pauldron_right_arm = p_diamond_chest_right_arm.addOrReplaceChild("diamond_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(197, 148).addBox(-3.8F, -1.6F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.2F, -0.2F, 0F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.1745F));
        PartDefinition p_diamond_shoulder_right_arm_0_0 = p_diamond_pauldron_right_arm.addOrReplaceChild("diamond_shoulder_right_arm_0_0", CubeListBuilder.create()
                .texOffs(216, 288).addBox(-1F, -1.88F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.25F, -0.12F, -0.25F)),
                PartPose.offsetAndRotation(-1.8F, -1.3F, -1F, 0.1396F, 0F, -0.4189F));
        PartDefinition p_diamond_shoulder_right_arm_0_1 = p_diamond_shoulder_right_arm_0_0.addOrReplaceChild("diamond_shoulder_right_arm_0_1", CubeListBuilder.create()
                .texOffs(80, 323).addBox(-0.5F, -1.8F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.0875F, -0.2F, -0.0875F)),
                PartPose.offset(0F, -1.51F, 0F));
        PartDefinition p_diamond_shoulder_right_arm_1_0 = p_diamond_pauldron_right_arm.addOrReplaceChild("diamond_shoulder_right_arm_1_0", CubeListBuilder.create()
                .texOffs(225, 288).addBox(-1F, -1.605F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.395F, -0.45F)),
                PartPose.offsetAndRotation(-2.6F, -1.3F, 1.6F, -0.1745F, 0F, -0.6632F));
        PartDefinition p_diamond_shoulder_right_arm_1_1 = p_diamond_shoulder_right_arm_1_0.addOrReplaceChild("diamond_shoulder_right_arm_1_1", CubeListBuilder.create()
                .texOffs(85, 323).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1975F, -0.45F, -0.1975F)),
                PartPose.offset(0F, -0.96F, 0F));
        PartDefinition p_diamond_legs_body = root.addOrReplaceChild("diamond_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_diamond_legs_left_leg = root.addOrReplaceChild("diamond_legs_left_leg", CubeListBuilder.create()
                .texOffs(126, 311).addBox(-1F, 5.4F, -3.5F, 2F, 2F, 1F, new CubeDeformation(0F, 0F, -0.1F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_diamond_legs_right_leg = root.addOrReplaceChild("diamond_legs_right_leg", CubeListBuilder.create()
                .texOffs(133, 311).addBox(-1F, 5.4F, -3.5F, 2F, 2F, 1F, new CubeDeformation(0F, 0F, -0.1F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_diamond_feet_left_leg = root.addOrReplaceChild("diamond_feet_left_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_diamond_heel_left_leg_0 = p_diamond_feet_left_leg.addOrReplaceChild("diamond_heel_left_leg_0", CubeListBuilder.create()
                .texOffs(234, 288).addBox(-1F, -1.605F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.395F, -0.4F)),
                PartPose.offsetAndRotation(0F, 10.9F, 2.8F, -0.9774F, 0F, 0F));
        PartDefinition p_diamond_heel_left_leg_1 = p_diamond_heel_left_leg_0.addOrReplaceChild("diamond_heel_left_leg_1", CubeListBuilder.create()
                .texOffs(90, 323).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.17F, -0.45F, -0.17F)),
                PartPose.offset(0F, -0.96F, 0F));
        PartDefinition p_diamond_feet_right_leg = root.addOrReplaceChild("diamond_feet_right_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_diamond_heel_right_leg_0 = p_diamond_feet_right_leg.addOrReplaceChild("diamond_heel_right_leg_0", CubeListBuilder.create()
                .texOffs(243, 288).addBox(-1F, -1.605F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.395F, -0.4F)),
                PartPose.offsetAndRotation(0F, 10.9F, 2.8F, -0.9774F, 0F, 0F));
        PartDefinition p_diamond_heel_right_leg_1 = p_diamond_heel_right_leg_0.addOrReplaceChild("diamond_heel_right_leg_1", CubeListBuilder.create()
                .texOffs(95, 323).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.17F, -0.45F, -0.17F)),
                PartPose.offset(0F, -0.96F, 0F));
        PartDefinition p_netherite_head_head = root.addOrReplaceChild("netherite_head_head", CubeListBuilder.create()
                .texOffs(55, 327).addBox(-5F, -6.3F, -5.85F, 10F, 1F, 1F, new CubeDeformation(-0.4F, -0.1F, -0.15F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_netherite_horn_left_0 = p_netherite_head_head.addOrReplaceChild("netherite_horn_left_0", CubeListBuilder.create()
                .texOffs(0, 293).addBox(-1F, -2F, -1F, 2F, 2F, 2F),
                PartPose.offsetAndRotation(4.4F, -7.6F, -2.6F, 0F, 0F, 1.2217F));
        PartDefinition p_netherite_horn_left_1 = p_netherite_horn_left_0.addOrReplaceChild("netherite_horn_left_1", CubeListBuilder.create()
                .texOffs(9, 293).addBox(-1F, -2F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, -1.75F, 0F, 0.5236F, 0F, -0.5236F));
        PartDefinition p_netherite_horn_left_2 = p_netherite_horn_left_1.addOrReplaceChild("netherite_horn_left_2", CubeListBuilder.create()
                .texOffs(18, 293).addBox(-1F, -1.9F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.1F, -0.45F)),
                PartPose.offsetAndRotation(0F, -1.75F, 0F, 0.5934F, 0F, -0.4189F));
        PartDefinition p_netherite_horn_right_0 = p_netherite_head_head.addOrReplaceChild("netherite_horn_right_0", CubeListBuilder.create()
                .texOffs(27, 293).addBox(-1F, -2F, -1F, 2F, 2F, 2F),
                PartPose.offsetAndRotation(-4.4F, -7.6F, -2.6F, 0F, 0F, -1.2217F));
        PartDefinition p_netherite_horn_right_1 = p_netherite_horn_right_0.addOrReplaceChild("netherite_horn_right_1", CubeListBuilder.create()
                .texOffs(36, 293).addBox(-1F, -2F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, -1.75F, 0F, 0.5236F, 0F, 0.5236F));
        PartDefinition p_netherite_horn_right_2 = p_netherite_horn_right_1.addOrReplaceChild("netherite_horn_right_2", CubeListBuilder.create()
                .texOffs(45, 293).addBox(-1F, -1.9F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.1F, -0.45F)),
                PartPose.offsetAndRotation(0F, -1.75F, 0F, 0.5934F, 0F, 0.4189F));
        PartDefinition p_netherite_chest_body = root.addOrReplaceChild("netherite_chest_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_netherite_chest_left_arm = root.addOrReplaceChild("netherite_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_netherite_pauldron_left_arm = p_netherite_chest_left_arm.addOrReplaceChild("netherite_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(33, 47).addBox(-3.7F, -2.25F, -4F, 8F, 3F, 8F, new CubeDeformation(-0.3F, -0.35F, -0.2F))
                .texOffs(62, 93).addBox(-2.4F, 0.35F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.2F, -0.05F, -0.1F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.2618F));
        PartDefinition p_netherite_spike_left_arm_0_0 = p_netherite_pauldron_left_arm.addOrReplaceChild("netherite_spike_left_arm_0_0", CubeListBuilder.create()
                .texOffs(54, 293).addBox(-1F, -1.66F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.34F, -0.4F)),
                PartPose.offsetAndRotation(2F, -1.8F, -1.6F, 0F, 0F, 0.5236F));
        PartDefinition p_netherite_spike_left_arm_0_1 = p_netherite_spike_left_arm_0_0.addOrReplaceChild("netherite_spike_left_arm_0_1", CubeListBuilder.create()
                .texOffs(100, 323).addBox(-0.5F, -1.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.17F, -0.4F, -0.17F)),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_netherite_spike_left_arm_1_0 = p_netherite_pauldron_left_arm.addOrReplaceChild("netherite_spike_left_arm_1_0", CubeListBuilder.create()
                .texOffs(63, 293).addBox(-1F, -1.66F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.34F, -0.4F)),
                PartPose.offsetAndRotation(2F, -1.8F, 1.6F, 0F, 0F, 0.5236F));
        PartDefinition p_netherite_spike_left_arm_1_1 = p_netherite_spike_left_arm_1_0.addOrReplaceChild("netherite_spike_left_arm_1_1", CubeListBuilder.create()
                .texOffs(105, 323).addBox(-0.5F, -1.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.17F, -0.4F, -0.17F)),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_netherite_chest_right_arm = root.addOrReplaceChild("netherite_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_netherite_pauldron_right_arm = p_netherite_chest_right_arm.addOrReplaceChild("netherite_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(66, 47).addBox(-4.3F, -2.25F, -4F, 8F, 3F, 8F, new CubeDeformation(-0.3F, -0.35F, -0.2F))
                .texOffs(93, 93).addBox(-4.6F, 0.35F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.2F, -0.05F, -0.1F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.2618F));
        PartDefinition p_netherite_spike_right_arm_0_0 = p_netherite_pauldron_right_arm.addOrReplaceChild("netherite_spike_right_arm_0_0", CubeListBuilder.create()
                .texOffs(72, 293).addBox(-1F, -1.66F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.34F, -0.4F)),
                PartPose.offsetAndRotation(-2F, -1.8F, -1.6F, 0F, 0F, -0.5236F));
        PartDefinition p_netherite_spike_right_arm_0_1 = p_netherite_spike_right_arm_0_0.addOrReplaceChild("netherite_spike_right_arm_0_1", CubeListBuilder.create()
                .texOffs(110, 323).addBox(-0.5F, -1.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.17F, -0.4F, -0.17F)),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_netherite_spike_right_arm_1_0 = p_netherite_pauldron_right_arm.addOrReplaceChild("netherite_spike_right_arm_1_0", CubeListBuilder.create()
                .texOffs(81, 293).addBox(-1F, -1.66F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.34F, -0.4F)),
                PartPose.offsetAndRotation(-2F, -1.8F, 1.6F, 0F, 0F, -0.5236F));
        PartDefinition p_netherite_spike_right_arm_1_1 = p_netherite_spike_right_arm_1_0.addOrReplaceChild("netherite_spike_right_arm_1_1", CubeListBuilder.create()
                .texOffs(115, 323).addBox(-0.5F, -1.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.17F, -0.4F, -0.17F)),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_netherite_legs_body = root.addOrReplaceChild("netherite_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_netherite_legs_left_leg = root.addOrReplaceChild("netherite_legs_left_leg", CubeListBuilder.create()
                .texOffs(0, 222).addBox(-3F, -0.9F, -3.3F, 6F, 5F, 1F, new CubeDeformation(-0.3F, -0.3F, 0F))
                .texOffs(121, 278).addBox(-2F, 5F, -3.2F, 4F, 3F, 1F, new CubeDeformation(-0.1F, -0.1F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_netherite_legs_right_leg = root.addOrReplaceChild("netherite_legs_right_leg", CubeListBuilder.create()
                .texOffs(15, 222).addBox(-3F, -0.9F, -3.3F, 6F, 5F, 1F, new CubeDeformation(-0.3F, -0.3F, 0F))
                .texOffs(132, 278).addBox(-2F, 5F, -3.2F, 4F, 3F, 1F, new CubeDeformation(-0.1F, -0.1F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_netherite_feet_left_leg = root.addOrReplaceChild("netherite_feet_left_leg", CubeListBuilder.create()
                .texOffs(189, 243).addBox(-2.5F, 10.15F, -4.45F, 5F, 3F, 2F, new CubeDeformation(0F, -0.25F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_netherite_toe_left_leg_0 = p_netherite_feet_left_leg.addOrReplaceChild("netherite_toe_left_leg_0", CubeListBuilder.create()
                .texOffs(150, 331).addBox(-0.5F, -0.94F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.06F, 0F)),
                PartPose.offsetAndRotation(0F, 11.2F, -4F, 1.0821F, 0F, 0F));
        PartDefinition p_netherite_toe_left_leg_1 = p_netherite_toe_left_leg_0.addOrReplaceChild("netherite_toe_left_leg_1", CubeListBuilder.create()
                .texOffs(155, 331).addBox(-0.5F, -0.9F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.1F, -0.225F)),
                PartPose.offset(0F, -0.63F, 0F));
        PartDefinition p_netherite_feet_right_leg = root.addOrReplaceChild("netherite_feet_right_leg", CubeListBuilder.create()
                .texOffs(204, 243).addBox(-2.5F, 10.15F, -4.45F, 5F, 3F, 2F, new CubeDeformation(0F, -0.25F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_netherite_toe_right_leg_0 = p_netherite_feet_right_leg.addOrReplaceChild("netherite_toe_right_leg_0", CubeListBuilder.create()
                .texOffs(160, 331).addBox(-0.5F, -0.94F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.06F, 0F)),
                PartPose.offsetAndRotation(0F, 11.2F, -4F, 1.0821F, 0F, 0F));
        PartDefinition p_netherite_toe_right_leg_1 = p_netherite_toe_right_leg_0.addOrReplaceChild("netherite_toe_right_leg_1", CubeListBuilder.create()
                .texOffs(165, 331).addBox(-0.5F, -0.9F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.1F, -0.225F)),
                PartPose.offset(0F, -0.63F, 0F));
        PartDefinition p_copper_head_head = root.addOrReplaceChild("copper_head_head", CubeListBuilder.create()
                .texOffs(149, 215).addBox(-2F, -10.4F, -2F, 4F, 2F, 4F, new CubeDeformation(-0.4F, -0.4F, -0.4F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_copper_spike_0 = p_copper_head_head.addOrReplaceChild("copper_spike_0", CubeListBuilder.create()
                .texOffs(90, 293).addBox(-1F, -2F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offset(0F, -9.8F, 0F));
        PartDefinition p_copper_spike_1 = p_copper_spike_0.addOrReplaceChild("copper_spike_1", CubeListBuilder.create()
                .texOffs(120, 323).addBox(-0.5F, -2F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1F, 0F, -0.1F)),
                PartPose.offset(0F, -1.75F, 0F));
        PartDefinition p_copper_chest_body = root.addOrReplaceChild("copper_chest_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_copper_chest_left_arm = root.addOrReplaceChild("copper_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_copper_pauldron_left_arm = p_copper_chest_left_arm.addOrReplaceChild("copper_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(226, 148).addBox(-3.2F, -1.55F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.2F, -0.05F, 0F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.2094F));
        PartDefinition p_copper_chest_right_arm = root.addOrReplaceChild("copper_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_copper_pauldron_right_arm = p_copper_chest_right_arm.addOrReplaceChild("copper_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(0, 159).addBox(-3.8F, -1.55F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.2F, -0.05F, 0F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.2094F));
        PartDefinition p_copper_legs_body = root.addOrReplaceChild("copper_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_copper_legs_left_leg = root.addOrReplaceChild("copper_legs_left_leg", CubeListBuilder.create()
                .texOffs(143, 278).addBox(-2F, 5F, -3.2F, 4F, 3F, 1F, new CubeDeformation(-0.1F, -0.1F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_copper_legs_right_leg = root.addOrReplaceChild("copper_legs_right_leg", CubeListBuilder.create()
                .texOffs(154, 278).addBox(-2F, 5F, -3.2F, 4F, 3F, 1F, new CubeDeformation(-0.1F, -0.1F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_copper_feet_left_leg = root.addOrReplaceChild("copper_feet_left_leg", CubeListBuilder.create()
                .texOffs(219, 243).addBox(-2.5F, 10.25F, -4.35F, 5F, 3F, 2F, new CubeDeformation(-0.1F, -0.35F, -0.35F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_copper_feet_right_leg = root.addOrReplaceChild("copper_feet_right_leg", CubeListBuilder.create()
                .texOffs(234, 243).addBox(-2.5F, 10.25F, -4.35F, 5F, 3F, 2F, new CubeDeformation(-0.1F, -0.35F, -0.35F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_turtle_head_head = root.addOrReplaceChild("turtle_head_head", CubeListBuilder.create()
                .texOffs(0, 21).addBox(-6F, -9.9F, -6F, 12F, 1F, 12F, new CubeDeformation(-0.4F, 0F, -0.4F))
                .texOffs(86, 59).addBox(-4.5F, -10.9F, -4.5F, 9F, 1F, 9F, new CubeDeformation(-0.1F, 0F, -0.1F))
                .texOffs(75, 198).addBox(-3F, -11.7F, -3F, 6F, 1F, 6F, new CubeDeformation(-0.2F, -0.1F, -0.2F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_moonstone_head_head = root.addOrReplaceChild("moonstone_head_head", CubeListBuilder.create()
                .texOffs(5, 327).addBox(-5.5F, -7.7F, -5.65F, 11F, 1F, 1F, new CubeDeformation(-0.05F, 0F, -0.2F))
                .texOffs(30, 327).addBox(-5.5F, -7.7F, 4.65F, 11F, 1F, 1F, new CubeDeformation(-0.05F, 0F, -0.2F))
                .texOffs(122, 47).addBox(-5.65F, -7.7F, -5F, 1F, 1F, 10F, new CubeDeformation(-0.2F, 0F, -0.15F))
                .texOffs(145, 47).addBox(4.65F, -7.7F, -5F, 1F, 1F, 10F, new CubeDeformation(-0.2F, 0F, -0.15F))
                .texOffs(99, 293).addBox(-1.5F, -8.8F, -6.05F, 3F, 3F, 1F, new CubeDeformation(-0.2F, -0.3F, -0.3F))
                .texOffs(140, 311).addBox(-1F, -8.3F, -6.35F, 2F, 2F, 1F, new CubeDeformation(-0.05F, -0.1F, -0.2F))
                .texOffs(35, 298).addBox(-6.175F, -8.2F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.225F, -0.3F, -0.3F))
                .texOffs(42, 298).addBox(5.175F, -8.2F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.225F, -0.3F, -0.3F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_moon_diadem = p_moonstone_head_head.addOrReplaceChild("moon_diadem", CubeListBuilder.create()
                .texOffs(59, 303).addBox(-2F, -1.5625F, -0.5F, 4F, 2F, 1F, new CubeDeformation(-0.375F, -0.4375F, -0.15F)),
                PartPose.offsetAndRotation(0F, -8.6F, -5.75F, -0.1396F, 0F, 0F));
        PartDefinition p_moon_diadem_left_0 = p_moon_diadem.addOrReplaceChild("moon_diadem_left_0", CubeListBuilder.create()
                .texOffs(147, 311).addBox(-1F, -1.9375F, -0.5F, 2F, 2F, 1F, new CubeDeformation(-0.4062F, -0.0625F, -0.175F)),
                PartPose.offsetAndRotation(1.3125F, -0.5625F, 0F, 0F, 0F, 1.0123F));
        PartDefinition p_moon_diadem_left_1 = p_moon_diadem_left_0.addOrReplaceChild("moon_diadem_left_1", CubeListBuilder.create()
                .texOffs(125, 323).addBox(-0.5F, -1.8438F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.0312F, -0.1562F, -0.2F)),
                PartPose.offsetAndRotation(0F, -1.625F, 0F, 0F, 0F, -0.6981F));
        PartDefinition p_moon_diadem_left_2 = p_moon_diadem_left_1.addOrReplaceChild("moon_diadem_left_2", CubeListBuilder.create()
                .texOffs(130, 323).addBox(-0.5F, -1.7188F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1875F, -0.2812F, -0.25F)),
                PartPose.offsetAndRotation(0F, -1.4375F, 0F, 0F, 0F, -0.6283F));
        PartDefinition p_moon_diadem_right_0 = p_moon_diadem.addOrReplaceChild("moon_diadem_right_0", CubeListBuilder.create()
                .texOffs(154, 311).addBox(-1F, -1.9375F, -0.5F, 2F, 2F, 1F, new CubeDeformation(-0.4062F, -0.0625F, -0.175F)),
                PartPose.offsetAndRotation(-1.3125F, -0.5625F, 0F, 0F, 0F, -1.0123F));
        PartDefinition p_moon_diadem_right_1 = p_moon_diadem_right_0.addOrReplaceChild("moon_diadem_right_1", CubeListBuilder.create()
                .texOffs(135, 323).addBox(-0.5F, -1.8438F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.0312F, -0.1562F, -0.2F)),
                PartPose.offsetAndRotation(0F, -1.625F, 0F, 0F, 0F, 0.6981F));
        PartDefinition p_moon_diadem_right_2 = p_moon_diadem_right_1.addOrReplaceChild("moon_diadem_right_2", CubeListBuilder.create()
                .texOffs(140, 323).addBox(-0.5F, -1.7188F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1875F, -0.2812F, -0.25F)),
                PartPose.offsetAndRotation(0F, -1.4375F, 0F, 0F, 0F, 0.6283F));
        PartDefinition p_moonstone_chest_body = root.addOrReplaceChild("moonstone_chest_body", CubeListBuilder.create()
                .texOffs(161, 311).addBox(-4.6F, -0.9F, -3.75F, 2F, 2F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.15F))
                .texOffs(168, 311).addBox(2.6F, -0.9F, -3.75F, 2F, 2F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.15F))
                .texOffs(193, 249).addBox(-2F, 1.2F, -3.7F, 4F, 4F, 1F, new CubeDeformation(-0.3F, -0.2F, -0.25F))
                .texOffs(108, 293).addBox(-1.5F, 1.7F, -4.05F, 3F, 3F, 1F, new CubeDeformation(-0.35F, -0.2F, -0.2F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_moon_cape = p_moonstone_chest_body.addOrReplaceChild("moon_cape", CubeListBuilder.create()
                .texOffs(116, 137).addBox(-5F, -0.2F, -0.2F, 10F, 9F, 1F, new CubeDeformation(-0.3F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(0F, -0.2F, 3.1F, 0.0873F, 0F, 0F));
        PartDefinition p_moon_cape_hem = p_moon_cape.addOrReplaceChild("moon_cape_hem", CubeListBuilder.create()
                .texOffs(139, 137).addBox(-5F, -0.3F, -0.2F, 10F, 9F, 1F, new CubeDeformation(-0.4F, -0.3F, -0.2F)),
                PartPose.offsetAndRotation(0F, 8.4F, 0.05F, 0.1396F, 0F, 0F));
        PartDefinition p_moonstone_chest_left_arm = root.addOrReplaceChild("moonstone_chest_left_arm", CubeListBuilder.create()
                .texOffs(116, 126).addBox(-2.5F, 5.15F, -3.5F, 7F, 3F, 7F, new CubeDeformation(-0.25F, -0.35F, -0.25F))
                .texOffs(175, 311).addBox(0F, 5.65F, -3.95F, 2F, 2F, 1F, new CubeDeformation(-0.4F, -0.35F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_moon_pauldron_left_arm = p_moonstone_chest_left_arm.addOrReplaceChild("moon_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(124, 93).addBox(-3.3F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.2F, -0.15F, -0.4F))
                .texOffs(145, 126).addBox(-2.1F, -0.15F, -4F, 6F, 2F, 8F, new CubeDeformation(-0.1F, -0.35F, -0.3F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.2094F));
        PartDefinition p_moon_shoulder_left_arm = p_moon_pauldron_left_arm.addOrReplaceChild("moon_shoulder_left_arm", CubeListBuilder.create()
                .texOffs(115, 327).addBox(-1.5F, -0.95F, -0.5F, 3F, 1F, 1F, new CubeDeformation(-0.2F, -0.05F, -0.15F)),
                PartPose.offsetAndRotation(1.4F, -1.4F, 0F, 0F, 1.5708F, 0F));
        PartDefinition p_moon_shoulder_left_arm_left_0 = p_moon_shoulder_left_arm.addOrReplaceChild("moon_shoulder_left_arm_left_0", CubeListBuilder.create()
                .texOffs(145, 323).addBox(-0.5F, -1.75F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.025F, -0.25F, -0.175F)),
                PartPose.offsetAndRotation(1.05F, -0.45F, 0F, 0F, 0F, 1.0123F));
        PartDefinition p_moon_shoulder_left_arm_left_1 = p_moon_shoulder_left_arm_left_0.addOrReplaceChild("moon_shoulder_left_arm_left_1", CubeListBuilder.create()
                .texOffs(150, 323).addBox(-0.5F, -1.675F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.125F, -0.325F, -0.2F)),
                PartPose.offsetAndRotation(0F, -1.25F, 0F, 0F, 0F, -0.6981F));
        PartDefinition p_moon_shoulder_left_arm_left_2 = p_moon_shoulder_left_arm_left_1.addOrReplaceChild("moon_shoulder_left_arm_left_2", CubeListBuilder.create()
                .texOffs(155, 323).addBox(-0.5F, -1.575F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.25F, -0.425F, -0.25F)),
                PartPose.offsetAndRotation(0F, -1.1F, 0F, 0F, 0F, -0.6283F));
        PartDefinition p_moon_shoulder_left_arm_right_0 = p_moon_shoulder_left_arm.addOrReplaceChild("moon_shoulder_left_arm_right_0", CubeListBuilder.create()
                .texOffs(160, 323).addBox(-0.5F, -1.75F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.025F, -0.25F, -0.175F)),
                PartPose.offsetAndRotation(-1.05F, -0.45F, 0F, 0F, 0F, -1.0123F));
        PartDefinition p_moon_shoulder_left_arm_right_1 = p_moon_shoulder_left_arm_right_0.addOrReplaceChild("moon_shoulder_left_arm_right_1", CubeListBuilder.create()
                .texOffs(165, 323).addBox(-0.5F, -1.675F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.125F, -0.325F, -0.2F)),
                PartPose.offsetAndRotation(0F, -1.25F, 0F, 0F, 0F, 0.6981F));
        PartDefinition p_moon_shoulder_left_arm_right_2 = p_moon_shoulder_left_arm_right_1.addOrReplaceChild("moon_shoulder_left_arm_right_2", CubeListBuilder.create()
                .texOffs(170, 323).addBox(-0.5F, -1.575F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.25F, -0.425F, -0.25F)),
                PartPose.offsetAndRotation(0F, -1.1F, 0F, 0F, 0F, 0.6283F));
        PartDefinition p_moonstone_chest_right_arm = root.addOrReplaceChild("moonstone_chest_right_arm", CubeListBuilder.create()
                .texOffs(174, 126).addBox(-4.5F, 5.15F, -3.5F, 7F, 3F, 7F, new CubeDeformation(-0.25F, -0.35F, -0.25F))
                .texOffs(182, 311).addBox(-2F, 5.65F, -3.95F, 2F, 2F, 1F, new CubeDeformation(-0.4F, -0.35F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_moon_pauldron_right_arm = p_moonstone_chest_right_arm.addOrReplaceChild("moon_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(155, 93).addBox(-3.7F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.2F, -0.15F, -0.4F))
                .texOffs(203, 126).addBox(-3.9F, -0.15F, -4F, 6F, 2F, 8F, new CubeDeformation(-0.1F, -0.35F, -0.3F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.2094F));
        PartDefinition p_moon_shoulder_right_arm = p_moon_pauldron_right_arm.addOrReplaceChild("moon_shoulder_right_arm", CubeListBuilder.create()
                .texOffs(124, 327).addBox(-1.5F, -0.95F, -0.5F, 3F, 1F, 1F, new CubeDeformation(-0.2F, -0.05F, -0.15F)),
                PartPose.offsetAndRotation(-1.4F, -1.4F, 0F, 0F, -1.5708F, 0F));
        PartDefinition p_moon_shoulder_right_arm_left_0 = p_moon_shoulder_right_arm.addOrReplaceChild("moon_shoulder_right_arm_left_0", CubeListBuilder.create()
                .texOffs(175, 323).addBox(-0.5F, -1.75F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.025F, -0.25F, -0.175F)),
                PartPose.offsetAndRotation(1.05F, -0.45F, 0F, 0F, 0F, 1.0123F));
        PartDefinition p_moon_shoulder_right_arm_left_1 = p_moon_shoulder_right_arm_left_0.addOrReplaceChild("moon_shoulder_right_arm_left_1", CubeListBuilder.create()
                .texOffs(180, 323).addBox(-0.5F, -1.675F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.125F, -0.325F, -0.2F)),
                PartPose.offsetAndRotation(0F, -1.25F, 0F, 0F, 0F, -0.6981F));
        PartDefinition p_moon_shoulder_right_arm_left_2 = p_moon_shoulder_right_arm_left_1.addOrReplaceChild("moon_shoulder_right_arm_left_2", CubeListBuilder.create()
                .texOffs(185, 323).addBox(-0.5F, -1.575F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.25F, -0.425F, -0.25F)),
                PartPose.offsetAndRotation(0F, -1.1F, 0F, 0F, 0F, -0.6283F));
        PartDefinition p_moon_shoulder_right_arm_right_0 = p_moon_shoulder_right_arm.addOrReplaceChild("moon_shoulder_right_arm_right_0", CubeListBuilder.create()
                .texOffs(190, 323).addBox(-0.5F, -1.75F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.025F, -0.25F, -0.175F)),
                PartPose.offsetAndRotation(-1.05F, -0.45F, 0F, 0F, 0F, -1.0123F));
        PartDefinition p_moon_shoulder_right_arm_right_1 = p_moon_shoulder_right_arm_right_0.addOrReplaceChild("moon_shoulder_right_arm_right_1", CubeListBuilder.create()
                .texOffs(195, 323).addBox(-0.5F, -1.675F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.125F, -0.325F, -0.2F)),
                PartPose.offsetAndRotation(0F, -1.25F, 0F, 0F, 0F, 0.6981F));
        PartDefinition p_moon_shoulder_right_arm_right_2 = p_moon_shoulder_right_arm_right_1.addOrReplaceChild("moon_shoulder_right_arm_right_2", CubeListBuilder.create()
                .texOffs(200, 323).addBox(-0.5F, -1.575F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.25F, -0.425F, -0.25F)),
                PartPose.offsetAndRotation(0F, -1.1F, 0F, 0F, 0F, 0.6283F));
        PartDefinition p_moonstone_legs_body = root.addOrReplaceChild("moonstone_legs_body", CubeListBuilder.create()
                .texOffs(189, 311).addBox(-1F, 10.15F, -3.8F, 2F, 2F, 1F, new CubeDeformation(-0.4F, -0.45F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_moon_buckle = p_moonstone_legs_body.addOrReplaceChild("moon_buckle", CubeListBuilder.create()
                .texOffs(149, 327).addBox(-1F, -0.8375F, -0.5F, 2F, 1F, 1F, new CubeDeformation(-0.025F, -0.1625F, -0.15F)),
                PartPose.offset(0F, 11.5F, -3.3F));
        PartDefinition p_moon_buckle_left_0 = p_moon_buckle.addOrReplaceChild("moon_buckle_left_0", CubeListBuilder.create()
                .texOffs(205, 323).addBox(-0.5F, -1.5625F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1438F, -0.4375F, -0.175F)),
                PartPose.offsetAndRotation(0.7875F, -0.3375F, 0F, 0F, 0F, 1.0123F));
        PartDefinition p_moon_buckle_left_1 = p_moon_buckle_left_0.addOrReplaceChild("moon_buckle_left_1", CubeListBuilder.create()
                .texOffs(210, 323).addBox(-0.5F, -1.5063F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.2188F, -0.4937F, -0.2F)),
                PartPose.offsetAndRotation(0F, -0.875F, 0F, 0F, 0F, -0.6981F));
        PartDefinition p_moon_buckle_left_2 = p_moon_buckle_left_1.addOrReplaceChild("moon_buckle_left_2", CubeListBuilder.create()
                .texOffs(170, 331).addBox(-0.5F, -0.9312F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.3125F, -0.0688F, -0.25F)),
                PartPose.offsetAndRotation(0F, -0.7625F, 0F, 0F, 0F, -0.6283F));
        PartDefinition p_moon_buckle_right_0 = p_moon_buckle.addOrReplaceChild("moon_buckle_right_0", CubeListBuilder.create()
                .texOffs(215, 323).addBox(-0.5F, -1.5625F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1438F, -0.4375F, -0.175F)),
                PartPose.offsetAndRotation(-0.7875F, -0.3375F, 0F, 0F, 0F, -1.0123F));
        PartDefinition p_moon_buckle_right_1 = p_moon_buckle_right_0.addOrReplaceChild("moon_buckle_right_1", CubeListBuilder.create()
                .texOffs(220, 323).addBox(-0.5F, -1.5063F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.2188F, -0.4937F, -0.2F)),
                PartPose.offsetAndRotation(0F, -0.875F, 0F, 0F, 0F, 0.6981F));
        PartDefinition p_moon_buckle_right_2 = p_moon_buckle_right_1.addOrReplaceChild("moon_buckle_right_2", CubeListBuilder.create()
                .texOffs(175, 331).addBox(-0.5F, -0.9312F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.3125F, -0.0688F, -0.25F)),
                PartPose.offsetAndRotation(0F, -0.7625F, 0F, 0F, 0F, 0.6283F));
        PartDefinition p_moonstone_legs_left_leg = root.addOrReplaceChild("moonstone_legs_left_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_moon_robe_left_leg = p_moonstone_legs_left_leg.addOrReplaceChild("moon_robe_left_leg", CubeListBuilder.create()
                .texOffs(0, 179).addBox(-2.5F, -0.1F, -0.55F, 5F, 8F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.25F)),
                PartPose.offsetAndRotation(0F, -0.6F, -2.75F, -0.1047F, 0F, 0F));
        PartDefinition p_moon_robe_side_left_leg = p_moonstone_legs_left_leg.addOrReplaceChild("moon_robe_side_left_leg", CubeListBuilder.create()
                .texOffs(94, 21).addBox(-0.25F, -0.1F, -2.5F, 1F, 8F, 5F, new CubeDeformation(-0.25F, -0.1F, -0.1F)),
                PartPose.offsetAndRotation(2.75F, -0.6F, 0F, 0F, 0F, -0.1047F));
        PartDefinition p_moonstone_legs_right_leg = root.addOrReplaceChild("moonstone_legs_right_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_moon_robe_right_leg = p_moonstone_legs_right_leg.addOrReplaceChild("moon_robe_right_leg", CubeListBuilder.create()
                .texOffs(13, 179).addBox(-2.5F, -0.1F, -0.55F, 5F, 8F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.25F)),
                PartPose.offsetAndRotation(0F, -0.6F, -2.75F, -0.1047F, 0F, 0F));
        PartDefinition p_moon_robe_side_right_leg = p_moonstone_legs_right_leg.addOrReplaceChild("moon_robe_side_right_leg", CubeListBuilder.create()
                .texOffs(107, 21).addBox(-0.75F, -0.1F, -2.5F, 1F, 8F, 5F, new CubeDeformation(-0.25F, -0.1F, -0.1F)),
                PartPose.offsetAndRotation(-2.75F, -0.6F, 0F, 0F, 0F, 0.1047F));
        PartDefinition p_moonstone_feet_left_leg = root.addOrReplaceChild("moonstone_feet_left_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_moon_toe_left_leg_0 = p_moonstone_feet_left_leg.addOrReplaceChild("moon_toe_left_leg_0", CubeListBuilder.create()
                .texOffs(176, 267).addBox(-2F, -1.85F, -1F, 4F, 2F, 2F, new CubeDeformation(-0.3F, -0.15F, -0.3F)),
                PartPose.offsetAndRotation(0F, 12F, -2.3F, 1.5708F, 0F, 0F));
        PartDefinition p_moon_toe_left_leg_1 = p_moon_toe_left_leg_0.addOrReplaceChild("moon_toe_left_leg_1", CubeListBuilder.create()
                .texOffs(165, 278).addBox(-1.5F, -1.75F, -1F, 3F, 2F, 2F, new CubeDeformation(-0.3F, -0.25F, -0.45F)),
                PartPose.offsetAndRotation(0F, -1.45F, 0F, -0.4887F, 0F, 0F));
        PartDefinition p_moon_toe_left_leg_2 = p_moon_toe_left_leg_1.addOrReplaceChild("moon_toe_left_leg_2", CubeListBuilder.create()
                .texOffs(196, 311).addBox(-1F, -1.65F, -0.5F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.35F, -0.1F)),
                PartPose.offsetAndRotation(0F, -1.25F, 0F, -0.733F, 0F, 0F));
        PartDefinition p_moon_toe_left_leg_3 = p_moon_toe_left_leg_2.addOrReplaceChild("moon_toe_left_leg_3", CubeListBuilder.create()
                .texOffs(180, 331).addBox(-0.5F, -1F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.15F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, -1.05F, 0F, -0.8029F, 0F, 0F));
        PartDefinition p_moon_ankle_left_leg = p_moonstone_feet_left_leg.addOrReplaceChild("moon_ankle_left_leg", CubeListBuilder.create()
                .texOffs(156, 327).addBox(-1F, -0.779F, -0.5F, 2F, 1F, 1F, new CubeDeformation(-0.194F, -0.221F, -0.15F)),
                PartPose.offsetAndRotation(3.35F, 9.8F, 0.4F, 0F, 1.5708F, 0F));
        PartDefinition p_moon_ankle_left_leg_left_0 = p_moon_ankle_left_leg.addOrReplaceChild("moon_ankle_left_leg_left_0", CubeListBuilder.create()
                .texOffs(185, 331).addBox(-0.5F, -0.965F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.2055F, -0.035F, -0.175F)),
                PartPose.offsetAndRotation(0.651F, -0.279F, 0F, 0F, 0F, 1.0123F));
        PartDefinition p_moon_ankle_left_leg_left_1 = p_moon_ankle_left_leg_left_0.addOrReplaceChild("moon_ankle_left_leg_left_1", CubeListBuilder.create()
                .texOffs(190, 331).addBox(-0.5F, -0.9185F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.2675F, -0.0815F, -0.2F)),
                PartPose.offsetAndRotation(0F, -0.68F, 0F, 0F, 0F, -0.6981F));
        PartDefinition p_moon_ankle_left_leg_left_2 = p_moon_ankle_left_leg_left_1.addOrReplaceChild("moon_ankle_left_leg_left_2", CubeListBuilder.create()
                .texOffs(195, 331).addBox(-0.5F, -0.8565F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.345F, -0.1435F, -0.25F)),
                PartPose.offsetAndRotation(0F, -0.587F, 0F, 0F, 0F, -0.6283F));
        PartDefinition p_moon_ankle_left_leg_right_0 = p_moon_ankle_left_leg.addOrReplaceChild("moon_ankle_left_leg_right_0", CubeListBuilder.create()
                .texOffs(200, 331).addBox(-0.5F, -0.965F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.2055F, -0.035F, -0.175F)),
                PartPose.offsetAndRotation(-0.651F, -0.279F, 0F, 0F, 0F, -1.0123F));
        PartDefinition p_moon_ankle_left_leg_right_1 = p_moon_ankle_left_leg_right_0.addOrReplaceChild("moon_ankle_left_leg_right_1", CubeListBuilder.create()
                .texOffs(205, 331).addBox(-0.5F, -0.9185F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.2675F, -0.0815F, -0.2F)),
                PartPose.offsetAndRotation(0F, -0.68F, 0F, 0F, 0F, 0.6981F));
        PartDefinition p_moon_ankle_left_leg_right_2 = p_moon_ankle_left_leg_right_1.addOrReplaceChild("moon_ankle_left_leg_right_2", CubeListBuilder.create()
                .texOffs(210, 331).addBox(-0.5F, -0.8565F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.345F, -0.1435F, -0.25F)),
                PartPose.offsetAndRotation(0F, -0.587F, 0F, 0F, 0F, 0.6283F));
        PartDefinition p_moonstone_feet_right_leg = root.addOrReplaceChild("moonstone_feet_right_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_moon_toe_right_leg_0 = p_moonstone_feet_right_leg.addOrReplaceChild("moon_toe_right_leg_0", CubeListBuilder.create()
                .texOffs(189, 267).addBox(-2F, -1.85F, -1F, 4F, 2F, 2F, new CubeDeformation(-0.3F, -0.15F, -0.3F)),
                PartPose.offsetAndRotation(0F, 12F, -2.3F, 1.5708F, 0F, 0F));
        PartDefinition p_moon_toe_right_leg_1 = p_moon_toe_right_leg_0.addOrReplaceChild("moon_toe_right_leg_1", CubeListBuilder.create()
                .texOffs(176, 278).addBox(-1.5F, -1.75F, -1F, 3F, 2F, 2F, new CubeDeformation(-0.3F, -0.25F, -0.45F)),
                PartPose.offsetAndRotation(0F, -1.45F, 0F, -0.4887F, 0F, 0F));
        PartDefinition p_moon_toe_right_leg_2 = p_moon_toe_right_leg_1.addOrReplaceChild("moon_toe_right_leg_2", CubeListBuilder.create()
                .texOffs(203, 311).addBox(-1F, -1.65F, -0.5F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.35F, -0.1F)),
                PartPose.offsetAndRotation(0F, -1.25F, 0F, -0.733F, 0F, 0F));
        PartDefinition p_moon_toe_right_leg_3 = p_moon_toe_right_leg_2.addOrReplaceChild("moon_toe_right_leg_3", CubeListBuilder.create()
                .texOffs(215, 331).addBox(-0.5F, -1F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.15F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, -1.05F, 0F, -0.8029F, 0F, 0F));
        PartDefinition p_moon_ankle_right_leg = p_moonstone_feet_right_leg.addOrReplaceChild("moon_ankle_right_leg", CubeListBuilder.create()
                .texOffs(163, 327).addBox(-1F, -0.779F, -0.5F, 2F, 1F, 1F, new CubeDeformation(-0.194F, -0.221F, -0.15F)),
                PartPose.offsetAndRotation(-3.35F, 9.8F, 0.4F, 0F, -1.5708F, 0F));
        PartDefinition p_moon_ankle_right_leg_left_0 = p_moon_ankle_right_leg.addOrReplaceChild("moon_ankle_right_leg_left_0", CubeListBuilder.create()
                .texOffs(220, 331).addBox(-0.5F, -0.965F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.2055F, -0.035F, -0.175F)),
                PartPose.offsetAndRotation(0.651F, -0.279F, 0F, 0F, 0F, 1.0123F));
        PartDefinition p_moon_ankle_right_leg_left_1 = p_moon_ankle_right_leg_left_0.addOrReplaceChild("moon_ankle_right_leg_left_1", CubeListBuilder.create()
                .texOffs(225, 331).addBox(-0.5F, -0.9185F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.2675F, -0.0815F, -0.2F)),
                PartPose.offsetAndRotation(0F, -0.68F, 0F, 0F, 0F, -0.6981F));
        PartDefinition p_moon_ankle_right_leg_left_2 = p_moon_ankle_right_leg_left_1.addOrReplaceChild("moon_ankle_right_leg_left_2", CubeListBuilder.create()
                .texOffs(230, 331).addBox(-0.5F, -0.8565F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.345F, -0.1435F, -0.25F)),
                PartPose.offsetAndRotation(0F, -0.587F, 0F, 0F, 0F, -0.6283F));
        PartDefinition p_moon_ankle_right_leg_right_0 = p_moon_ankle_right_leg.addOrReplaceChild("moon_ankle_right_leg_right_0", CubeListBuilder.create()
                .texOffs(235, 331).addBox(-0.5F, -0.965F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.2055F, -0.035F, -0.175F)),
                PartPose.offsetAndRotation(-0.651F, -0.279F, 0F, 0F, 0F, -1.0123F));
        PartDefinition p_moon_ankle_right_leg_right_1 = p_moon_ankle_right_leg_right_0.addOrReplaceChild("moon_ankle_right_leg_right_1", CubeListBuilder.create()
                .texOffs(240, 331).addBox(-0.5F, -0.9185F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.2675F, -0.0815F, -0.2F)),
                PartPose.offsetAndRotation(0F, -0.68F, 0F, 0F, 0F, 0.6981F));
        PartDefinition p_moon_ankle_right_leg_right_2 = p_moon_ankle_right_leg_right_1.addOrReplaceChild("moon_ankle_right_leg_right_2", CubeListBuilder.create()
                .texOffs(245, 331).addBox(-0.5F, -0.8565F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.345F, -0.1435F, -0.25F)),
                PartPose.offsetAndRotation(0F, -0.587F, 0F, 0F, 0F, 0.6283F));
        PartDefinition p_aquamarine_head_head = root.addOrReplaceChild("aquamarine_head_head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-6F, -8.6F, -6F, 12F, 8F, 12F, new CubeDeformation(-0.3F, 0F, -0.3F))
                .texOffs(41, 35).addBox(-5F, -9.6F, -5F, 10F, 1F, 10F)
                .texOffs(19, 148).addBox(-4F, -10.5F, -4F, 8F, 1F, 8F, new CubeDeformation(-0.3F, -0.1F, -0.3F))
                .texOffs(145, 0).addBox(5.45F, -8.1F, -4.5F, 1F, 7F, 9F, new CubeDeformation(-0.25F, -0.3F, -0.1F))
                .texOffs(166, 0).addBox(-6.45F, -8.1F, -4.5F, 1F, 7F, 9F, new CubeDeformation(-0.25F, -0.3F, -0.1F))
                .texOffs(71, 189).addBox(-4.5F, -8.1F, 5.45F, 9F, 7F, 1F, new CubeDeformation(-0.1F, -0.3F, -0.25F))
                .texOffs(106, 303).addBox(-1F, -11.4F, -1F, 2F, 1F, 2F, new CubeDeformation(-0.2F, 0F, -0.2F))
                .texOffs(104, 327).addBox(-2F, -12.15F, -0.5F, 4F, 1F, 1F, new CubeDeformation(-0.1F, -0.25F, -0.15F))
                .texOffs(187, 0).addBox(-6.5F, -1.35F, -6.5F, 13F, 2F, 13F, new CubeDeformation(-0.4F, -0.35F, -0.4F))
                .texOffs(78, 327).addBox(-2.5F, -8.2F, -6.6F, 5F, 1F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.1F))
                .texOffs(91, 327).addBox(-2.5F, -2.2F, -6.6F, 5F, 1F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.1F))
                .texOffs(203, 207).addBox(-3.3F, -7.7F, -6.6F, 1F, 6F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.1F))
                .texOffs(208, 207).addBox(2.3F, -7.7F, -6.6F, 1F, 6F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.1F))
                .texOffs(250, 331).addBox(-2.55F, -7.45F, -6.475F, 1F, 1F, 1F, new CubeDeformation(-0.15F, -0.15F, -0.225F))
                .texOffs(0, 334).addBox(1.55F, -7.45F, -6.475F, 1F, 1F, 1F, new CubeDeformation(-0.15F, -0.15F, -0.225F))
                .texOffs(5, 334).addBox(-2.55F, -2.95F, -6.475F, 1F, 1F, 1F, new CubeDeformation(-0.15F, -0.15F, -0.225F))
                .texOffs(10, 334).addBox(1.55F, -2.95F, -6.475F, 1F, 1F, 1F, new CubeDeformation(-0.15F, -0.15F, -0.225F))
                .texOffs(197, 236).addBox(5.85F, -6.3F, -1.5F, 1F, 3F, 3F, new CubeDeformation(-0.35F, 0F, 0F))
                .texOffs(208, 229).addBox(6F, -7.1F, -2.5F, 1F, 1F, 5F, new CubeDeformation(-0.2F, -0.2F, -0.4F))
                .texOffs(221, 229).addBox(6F, -3.5F, -2.5F, 1F, 1F, 5F, new CubeDeformation(-0.2F, -0.2F, -0.4F))
                .texOffs(124, 298).addBox(6F, -6.3F, -2.3F, 1F, 3F, 1F, new CubeDeformation(-0.2F, 0F, -0.2F))
                .texOffs(129, 298).addBox(6F, -6.3F, 1.3F, 1F, 3F, 1F, new CubeDeformation(-0.2F, 0F, -0.2F))
                .texOffs(206, 236).addBox(-6.85F, -6.3F, -1.5F, 1F, 3F, 3F, new CubeDeformation(-0.35F, 0F, 0F))
                .texOffs(234, 229).addBox(-7F, -7.1F, -2.5F, 1F, 1F, 5F, new CubeDeformation(-0.2F, -0.2F, -0.4F))
                .texOffs(0, 236).addBox(-7F, -3.5F, -2.5F, 1F, 1F, 5F, new CubeDeformation(-0.2F, -0.2F, -0.4F))
                .texOffs(134, 298).addBox(-7F, -6.3F, -2.3F, 1F, 3F, 1F, new CubeDeformation(-0.2F, 0F, -0.2F))
                .texOffs(139, 298).addBox(-7F, -6.3F, 1.3F, 1F, 3F, 1F, new CubeDeformation(-0.2F, 0F, -0.2F))
                .texOffs(210, 311).addBox(0.7F, -3.6F, 6.15F, 2F, 2F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.05F))
                .texOffs(36, 267).addBox(0.7F, -2.2F, 6F, 2F, 3F, 2F, new CubeDeformation(-0.45F, -0.2F, -0.45F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_aquamarine_chest_body = root.addOrReplaceChild("aquamarine_chest_body", CubeListBuilder.create()
                .texOffs(21, 59).addBox(0.6F, 1.2F, 3.1F, 3F, 8F, 3F, new CubeDeformation(0F, 0F, -0.1F))
                .texOffs(202, 267).addBox(0.6F, 0.4F, 3.1F, 3F, 1F, 3F, new CubeDeformation(-0.3F, -0.2F, -0.4F))
                .texOffs(215, 267).addBox(0.6F, 8.95F, 3.1F, 3F, 1F, 3F, new CubeDeformation(-0.3F, -0.25F, -0.4F))
                .texOffs(15, 334).addBox(1.6F, -0.25F, 4.1F, 1F, 1F, 1F, new CubeDeformation(0F, -0.15F, 0F))
                .texOffs(20, 334).addBox(3.2F, -0.2F, 4.1F, 1F, 1F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.05F))
                .texOffs(34, 59).addBox(-3.6F, 1.2F, 3.1F, 3F, 8F, 3F, new CubeDeformation(0F, 0F, -0.1F))
                .texOffs(228, 267).addBox(-3.6F, 0.4F, 3.1F, 3F, 1F, 3F, new CubeDeformation(-0.3F, -0.2F, -0.4F))
                .texOffs(241, 267).addBox(-3.6F, 8.95F, 3.1F, 3F, 1F, 3F, new CubeDeformation(-0.3F, -0.25F, -0.4F))
                .texOffs(25, 334).addBox(-2.6F, -0.25F, 4.1F, 1F, 1F, 1F, new CubeDeformation(0F, -0.15F, 0F))
                .texOffs(30, 334).addBox(-4.2F, -0.2F, 4.1F, 1F, 1F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.05F))
                .texOffs(0, 243).addBox(-4F, 2.05F, 2.6F, 8F, 1F, 4F, new CubeDeformation(-0.2F, -0.15F, -0.4F))
                .texOffs(25, 243).addBox(-4F, 7.05F, 2.6F, 8F, 1F, 4F, new CubeDeformation(-0.2F, -0.15F, -0.4F))
                .texOffs(115, 303).addBox(1.1F, -0.6F, 4.9F, 2F, 1F, 2F, new CubeDeformation(-0.45F, 0F, -0.1F))
                .texOffs(117, 293).addBox(1.1F, 1.6F, -3.95F, 3F, 3F, 1F, new CubeDeformation(0F, 0F, -0.2F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_aquamarine_chest_left_arm = root.addOrReplaceChild("aquamarine_chest_left_arm", CubeListBuilder.create()
                .texOffs(29, 159).addBox(-2.5F, 5.95F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.25F, -0.25F, -0.25F))
                .texOffs(217, 311).addBox(0F, 5.95F, -3.95F, 2F, 2F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_aqua_pauldron_left_arm = p_aquamarine_chest_left_arm.addOrReplaceChild("aqua_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(87, 169).addBox(-2.6F, -1.5F, -3.5F, 6F, 2F, 7F, new CubeDeformation(0F, -0.2F, -0.1F))
                .texOffs(175, 179).addBox(-1.6F, 0.3F, -3.5F, 5F, 1F, 7F),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.1745F));
        PartDefinition p_aquamarine_chest_right_arm = root.addOrReplaceChild("aquamarine_chest_right_arm", CubeListBuilder.create()
                .texOffs(58, 159).addBox(-4.5F, 5.95F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.25F, -0.25F, -0.25F))
                .texOffs(224, 311).addBox(-2F, 5.95F, -3.95F, 2F, 2F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_aqua_pauldron_right_arm = p_aquamarine_chest_right_arm.addOrReplaceChild("aqua_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(114, 169).addBox(-3.4F, -1.5F, -3.5F, 6F, 2F, 7F, new CubeDeformation(0F, -0.2F, -0.1F))
                .texOffs(200, 179).addBox(-3.4F, 0.3F, -3.5F, 5F, 1F, 7F),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.1745F));
        PartDefinition p_aquamarine_legs_body = root.addOrReplaceChild("aquamarine_legs_body", CubeListBuilder.create()
                .texOffs(124, 303).addBox(-4.9F, 10.25F, -3.675F, 3F, 2F, 1F, new CubeDeformation(-0.3F, -0.05F, -0.125F))
                .texOffs(133, 303).addBox(1.9F, 10.25F, -3.675F, 3F, 2F, 1F, new CubeDeformation(-0.3F, -0.05F, -0.125F))
                .texOffs(45, 267).addBox(4.475F, 10.25F, -1.5F, 1F, 2F, 3F, new CubeDeformation(-0.125F, -0.05F, -0.2F))
                .texOffs(54, 267).addBox(-5.475F, 10.25F, -1.5F, 1F, 2F, 3F, new CubeDeformation(-0.125F, -0.05F, -0.2F))
                .texOffs(142, 303).addBox(-1.5F, 10.25F, -3.8F, 3F, 2F, 1F, new CubeDeformation(-0.4F, -0.25F, -0.2F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_aquamarine_legs_left_leg = root.addOrReplaceChild("aquamarine_legs_left_leg", CubeListBuilder.create()
                .texOffs(0, 273).addBox(-2.5F, 3.4F, -3.25F, 5F, 3F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(231, 311).addBox(-1F, 3.9F, -3.7F, 2F, 2F, 1F, new CubeDeformation(-0.45F, -0.45F, -0.3F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_aquamarine_legs_right_leg = root.addOrReplaceChild("aquamarine_legs_right_leg", CubeListBuilder.create()
                .texOffs(13, 273).addBox(-2.5F, 3.4F, -3.25F, 5F, 3F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(238, 311).addBox(-1F, 3.9F, -3.7F, 2F, 2F, 1F, new CubeDeformation(-0.45F, -0.45F, -0.3F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_aquamarine_feet_left_leg = root.addOrReplaceChild("aquamarine_feet_left_leg", CubeListBuilder.create()
                .texOffs(100, 198).addBox(-3F, 9.25F, -3F, 6F, 1F, 6F, new CubeDeformation(-0.45F, -0.05F, -0.45F))
                .texOffs(49, 298).addBox(2.275F, 8.75F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.275F, -0.4F, -0.3F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_aqua_fin_left_leg_0 = p_aquamarine_feet_left_leg.addOrReplaceChild("aqua_fin_left_leg_0", CubeListBuilder.create()
                .texOffs(90, 249).addBox(-2.5F, -3.7F, -0.5F, 5F, 4F, 1F, new CubeDeformation(-0.2F, -0.3F, -0.275F)),
                PartPose.offsetAndRotation(0F, 11.75F, -2F, 1.5708F, 0F, 0F));
        PartDefinition p_aqua_fin_left_leg_1 = p_aqua_fin_left_leg_0.addOrReplaceChild("aqua_fin_left_leg_1", CubeListBuilder.create()
                .texOffs(0, 249).addBox(-3F, -3.6F, -0.5F, 6F, 4F, 1F, new CubeDeformation(-0.3F, -0.4F, -0.3F)),
                PartPose.offsetAndRotation(0F, -3.25F, 0F, -0.0873F, 0F, 0F));
        PartDefinition p_aqua_fin_left_leg_2 = p_aqua_fin_left_leg_1.addOrReplaceChild("aqua_fin_left_leg_2", CubeListBuilder.create()
                .texOffs(120, 267).addBox(-3F, -2.8F, -0.5F, 6F, 3F, 1F, new CubeDeformation(0F, -0.2F, -0.325F)),
                PartPose.offsetAndRotation(0F, -3.05F, 0F, -0.1571F, 0F, 0F));
        PartDefinition p_aquamarine_feet_right_leg = root.addOrReplaceChild("aquamarine_feet_right_leg", CubeListBuilder.create()
                .texOffs(125, 198).addBox(-3F, 9.25F, -3F, 6F, 1F, 6F, new CubeDeformation(-0.45F, -0.05F, -0.45F))
                .texOffs(56, 298).addBox(-3.275F, 8.75F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.275F, -0.4F, -0.3F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_aqua_fin_right_leg_0 = p_aquamarine_feet_right_leg.addOrReplaceChild("aqua_fin_right_leg_0", CubeListBuilder.create()
                .texOffs(103, 249).addBox(-2.5F, -3.7F, -0.5F, 5F, 4F, 1F, new CubeDeformation(-0.2F, -0.3F, -0.275F)),
                PartPose.offsetAndRotation(0F, 11.75F, -2F, 1.5708F, 0F, 0F));
        PartDefinition p_aqua_fin_right_leg_1 = p_aqua_fin_right_leg_0.addOrReplaceChild("aqua_fin_right_leg_1", CubeListBuilder.create()
                .texOffs(15, 249).addBox(-3F, -3.6F, -0.5F, 6F, 4F, 1F, new CubeDeformation(-0.3F, -0.4F, -0.3F)),
                PartPose.offsetAndRotation(0F, -3.25F, 0F, -0.0873F, 0F, 0F));
        PartDefinition p_aqua_fin_right_leg_2 = p_aqua_fin_right_leg_1.addOrReplaceChild("aqua_fin_right_leg_2", CubeListBuilder.create()
                .texOffs(135, 267).addBox(-3F, -2.8F, -0.5F, 6F, 3F, 1F, new CubeDeformation(0F, -0.2F, -0.325F)),
                PartPose.offsetAndRotation(0F, -3.05F, 0F, -0.1571F, 0F, 0F));
        PartDefinition p_dusk_head_head = root.addOrReplaceChild("dusk_head_head", CubeListBuilder.create()
                .texOffs(49, 21).addBox(-5.5F, -10.15F, -5.4F, 11F, 2F, 11F, new CubeDeformation(0F, -0.45F, -0.1F))
                .texOffs(49, 0).addBox(4.775F, -9.1F, -5.4F, 1F, 9F, 11F, new CubeDeformation(-0.225F, -0.3F, -0.1F))
                .texOffs(74, 0).addBox(-5.775F, -9.1F, -5.4F, 1F, 9F, 11F, new CubeDeformation(-0.225F, -0.3F, -0.1F))
                .texOffs(162, 137).addBox(-5F, -9F, 4.775F, 10F, 9F, 1F, new CubeDeformation(0F, -0.2F, -0.225F))
                .texOffs(196, 298).addBox(-6F, -9.45F, -6.05F, 12F, 2F, 1F, new CubeDeformation(-0.45F, -0.25F, -0.2F))
                .texOffs(31, 198).addBox(3.925F, -8.3F, -6.05F, 2F, 7F, 1F, new CubeDeformation(-0.375F, -0.3F, -0.2F))
                .texOffs(38, 198).addBox(-5.925F, -8.3F, -6.05F, 2F, 7F, 1F, new CubeDeformation(-0.375F, -0.3F, -0.2F))
                .texOffs(103, 267).addBox(-3.5F, -3.7F, -5.775F, 7F, 3F, 1F, new CubeDeformation(-0.2F, -0.1F, -0.225F))
                .texOffs(144, 298).addBox(-0.5F, -3.75F, -6.075F, 1F, 3F, 1F, new CubeDeformation(-0.05F, 0F, -0.325F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_dusk_hood_tip_0 = p_dusk_head_head.addOrReplaceChild("dusk_hood_tip_0", CubeListBuilder.create()
                .texOffs(26, 273).addBox(-2.5F, -0.2F, -0.5F, 5F, 3F, 1F, new CubeDeformation(-0.2F, -0.2F, 0F)),
                PartPose.offsetAndRotation(0F, -8.4F, 5.3F, 1.0821F, 0F, 0F));
        PartDefinition p_dusk_hood_tip_1 = p_dusk_hood_tip_0.addOrReplaceChild("dusk_hood_tip_1", CubeListBuilder.create()
                .texOffs(187, 278).addBox(-2F, -0.1F, -0.5F, 4F, 3F, 1F, new CubeDeformation(-0.3F, -0.1F, -0.05F)),
                PartPose.offsetAndRotation(0F, 2.35F, 0F, -0.5236F, 0F, 0F));
        PartDefinition p_dusk_hood_tip_2 = p_dusk_hood_tip_1.addOrReplaceChild("dusk_hood_tip_2", CubeListBuilder.create()
                .texOffs(126, 293).addBox(-1.5F, -0.1F, -0.5F, 3F, 3F, 1F, new CubeDeformation(-0.3F, -0.1F, -0.1F)),
                PartPose.offsetAndRotation(0F, 2.55F, 0F, -0.384F, 0F, 0F));
        PartDefinition p_dusk_hood_tip_3 = p_dusk_hood_tip_2.addOrReplaceChild("dusk_hood_tip_3", CubeListBuilder.create()
                .texOffs(63, 298).addBox(-1F, -0.3F, -0.5F, 2F, 3F, 1F, new CubeDeformation(-0.3F, -0.3F, -0.15F)),
                PartPose.offsetAndRotation(0F, 2.55F, 0F, -0.2094F, 0F, 0F));
        PartDefinition p_dusk_chest_body = root.addOrReplaceChild("dusk_chest_body", CubeListBuilder.create()
                .texOffs(47, 59).addBox(-5.5F, -1F, -4F, 11F, 2F, 8F, new CubeDeformation(-0.1F, -0.45F, -0.45F))
                .texOffs(151, 303).addBox(1.6F, 0.2F, -4F, 3F, 2F, 1F, new CubeDeformation(-0.4F, 0F, -0.25F))
                .texOffs(245, 311).addBox(-4.05F, 2F, -3.775F, 2F, 2F, 1F, new CubeDeformation(-0.15F, -0.4F, -0.325F))
                .texOffs(0, 315).addBox(-2.5F, 3.6F, -3.775F, 2F, 2F, 1F, new CubeDeformation(-0.15F, -0.4F, -0.325F))
                .texOffs(7, 315).addBox(-0.95F, 5.2F, -3.775F, 2F, 2F, 1F, new CubeDeformation(-0.15F, -0.4F, -0.325F))
                .texOffs(14, 315).addBox(0.6F, 6.8F, -3.775F, 2F, 2F, 1F, new CubeDeformation(-0.15F, -0.4F, -0.325F))
                .texOffs(21, 315).addBox(2.15F, 8.4F, -3.775F, 2F, 2F, 1F, new CubeDeformation(-0.15F, -0.4F, -0.325F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_dusk_cape = p_dusk_chest_body.addOrReplaceChild("dusk_cape", CubeListBuilder.create()
                .texOffs(225, 179).addBox(-5.5F, -0.1F, -0.2F, 11F, 7F, 1F, new CubeDeformation(-0.3F, -0.1F, -0.2F))
                .texOffs(135, 293).addBox(-5.6F, 6.35F, -0.2F, 3F, 3F, 1F, new CubeDeformation(-0.4F, -0.45F, -0.22F))
                .texOffs(160, 303).addBox(-3.1F, 6.4F, -0.2F, 3F, 2F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.22F))
                .texOffs(144, 293).addBox(-0.6F, 6.35F, -0.2F, 3F, 3F, 1F, new CubeDeformation(-0.4F, -0.45F, -0.22F))
                .texOffs(169, 303).addBox(1.9F, 6.4F, -0.2F, 3F, 2F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.22F)),
                PartPose.offsetAndRotation(0F, 0.2F, 3.25F, 0.1047F, 0F, 0F));
        PartDefinition p_dusk_clasp_a_0 = p_dusk_chest_body.addOrReplaceChild("dusk_clasp_a_0", CubeListBuilder.create()
                .texOffs(225, 323).addBox(-0.5F, -1.5445F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.05F, -0.4555F, -0.05F)),
                PartPose.offsetAndRotation(3F, 1.8F, -4F, -0.2094F, 0F, 0.2443F));
        PartDefinition p_dusk_clasp_a_1 = p_dusk_clasp_a_0.addOrReplaceChild("dusk_clasp_a_1", CubeListBuilder.create()
                .texOffs(35, 334).addBox(-0.5F, -0.995F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.2525F, -0.005F, -0.2525F)),
                PartPose.offset(0F, -0.839F, 0F));
        PartDefinition p_dusk_clasp_b_0 = p_dusk_chest_body.addOrReplaceChild("dusk_clasp_b_0", CubeListBuilder.create()
                .texOffs(40, 334).addBox(-0.5F, -0.8713F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.14F, -0.1287F, -0.14F)),
                PartPose.offsetAndRotation(3.63F, 2F, -4F, -0.2094F, 0F, -0.4189F));
        PartDefinition p_dusk_clasp_b_1 = p_dusk_clasp_b_0.addOrReplaceChild("dusk_clasp_b_1", CubeListBuilder.create()
                .texOffs(45, 334).addBox(-0.5F, -0.8375F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.302F, -0.1625F, -0.302F)),
                PartPose.offset(0F, -0.4925F, 0F));
        PartDefinition p_dusk_chest_left_arm = root.addOrReplaceChild("dusk_chest_left_arm", CubeListBuilder.create()
                .texOffs(87, 159).addBox(-2.5F, 0.85F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.15F, -0.35F, -0.15F))
                .texOffs(117, 179).addBox(-2.5F, 2.9F, -3.5F, 7F, 1F, 7F, new CubeDeformation(-0.15F, 0F, -0.15F))
                .texOffs(116, 159).addBox(-2.5F, 7.4F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.2F, -0.2F, -0.2F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_dusk_chest_right_arm = root.addOrReplaceChild("dusk_chest_right_arm", CubeListBuilder.create()
                .texOffs(145, 159).addBox(-4.5F, 0.85F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.15F, -0.35F, -0.15F))
                .texOffs(146, 179).addBox(-4.5F, 2.9F, -3.5F, 7F, 1F, 7F, new CubeDeformation(-0.15F, 0F, -0.15F))
                .texOffs(174, 159).addBox(-4.5F, 7.4F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.2F, -0.2F, -0.2F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_dusk_legs_body = root.addOrReplaceChild("dusk_legs_body", CubeListBuilder.create()
                .texOffs(153, 293).addBox(-4.8F, 9.9F, -3.45F, 3F, 3F, 1F, new CubeDeformation(-0.4F, -0.3F, 0F))
                .texOffs(77, 267).addBox(-4.65F, 9.7F, -1F, 1F, 3F, 2F, new CubeDeformation(-0.05F, -0.4F, 0F))
                .texOffs(178, 303).addBox(-4.5F, 9.7F, -4F, 2F, 1F, 2F, new CubeDeformation(-0.2F, -0.2F, -0.4F))
                .texOffs(28, 315).addBox(-1F, 10.1F, -3.875F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.3F, -0.275F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_dusk_dagger = p_dusk_legs_body.addOrReplaceChild("dusk_dagger", CubeListBuilder.create()
                .texOffs(149, 298).addBox(-0.5F, -2.5F, -0.15F, 1F, 3F, 1F, new CubeDeformation(-0.05F, 0F, -0.15F))
                .texOffs(170, 327).addBox(-1F, -3.25F, -0.15F, 2F, 1F, 1F, new CubeDeformation(-0.3F, -0.25F, -0.1F))
                .texOffs(230, 323).addBox(-0.5F, -4.8F, -0.15F, 1F, 2F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(50, 334).addBox(-0.5F, -5.3F, -0.15F, 1F, 1F, 1F, new CubeDeformation(-0.1F, -0.2F, -0.15F)),
                PartPose.offsetAndRotation(0F, 11F, 3.3F, 0F, 0F, 1.2915F));
        PartDefinition p_dusk_legs_left_leg = root.addOrReplaceChild("dusk_legs_left_leg", CubeListBuilder.create()
                .texOffs(39, 273).addBox(-2.5F, 3.3F, -3.25F, 5F, 3F, 1F, new CubeDeformation(-0.15F, -0.1F, -0.2F))
                .texOffs(35, 315).addBox(-1F, 3.8F, -3.675F, 2F, 2F, 1F, new CubeDeformation(-0.45F, -0.45F, -0.275F))
                .texOffs(23, 215).addBox(-2.5F, 6.95F, -2.5F, 5F, 1F, 5F, new CubeDeformation(-0.1F, -0.05F, -0.1F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_dusk_legs_right_leg = root.addOrReplaceChild("dusk_legs_right_leg", CubeListBuilder.create()
                .texOffs(52, 273).addBox(-2.5F, 3.3F, -3.25F, 5F, 3F, 1F, new CubeDeformation(-0.15F, -0.1F, -0.2F))
                .texOffs(42, 315).addBox(-1F, 3.8F, -3.675F, 2F, 2F, 1F, new CubeDeformation(-0.45F, -0.45F, -0.275F))
                .texOffs(44, 215).addBox(-2.5F, 6.95F, -2.5F, 5F, 1F, 5F, new CubeDeformation(-0.1F, -0.05F, -0.1F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_dusk_feet_left_leg = root.addOrReplaceChild("dusk_feet_left_leg", CubeListBuilder.create()
                .texOffs(0, 189).addBox(-3F, 6.15F, -3F, 6F, 2F, 6F, new CubeDeformation(-0.45F, -0.25F, -0.45F))
                .texOffs(65, 215).addBox(-2.5F, 8.5F, -2.5F, 5F, 1F, 5F, new CubeDeformation(-0.05F, -0.1F, -0.05F))
                .texOffs(86, 215).addBox(-2.5F, 9.9F, -2.5F, 5F, 1F, 5F, new CubeDeformation(-0.05F, -0.1F, -0.05F))
                .texOffs(70, 298).addBox(2.1F, 8F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.3F, -0.4F, -0.4F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_dusk_feet_right_leg = root.addOrReplaceChild("dusk_feet_right_leg", CubeListBuilder.create()
                .texOffs(25, 189).addBox(-3F, 6.15F, -3F, 6F, 2F, 6F, new CubeDeformation(-0.45F, -0.25F, -0.45F))
                .texOffs(107, 215).addBox(-2.5F, 8.5F, -2.5F, 5F, 1F, 5F, new CubeDeformation(-0.05F, -0.1F, -0.05F))
                .texOffs(128, 215).addBox(-2.5F, 9.9F, -2.5F, 5F, 1F, 5F, new CubeDeformation(-0.05F, -0.1F, -0.05F))
                .texOffs(77, 298).addBox(-3.1F, 8F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.3F, -0.4F, -0.4F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_starwalker_head_head = root.addOrReplaceChild("starwalker_head_head", CubeListBuilder.create()
                .texOffs(168, 47).addBox(-0.5F, -9.975F, -5F, 1F, 1F, 10F, new CubeDeformation(-0.05F, -0.025F, -0.1F))
                .texOffs(154, 298).addBox(-0.5F, -9.4F, -6F, 1F, 3F, 1F, new CubeDeformation(-0.125F, -0.25F, -0.25F))
                .texOffs(177, 327).addBox(-1F, -8.4F, -6F, 2F, 1F, 1F, new CubeDeformation(0F, -0.125F, -0.25F))
                .texOffs(49, 315).addBox(-1F, -8.9F, -6.075F, 2F, 2F, 1F, new CubeDeformation(-0.4375F, -0.4375F, -0.175F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_star_fin_left = p_starwalker_head_head.addOrReplaceChild("star_fin_left", CubeListBuilder.create()
                .texOffs(84, 267).addBox(-0.5F, -1.5F, -1F, 1F, 3F, 2F, new CubeDeformation(-0.1F, -0.465F, -0.08F)),
                PartPose.offsetAndRotation(5.25F, -5.6F, -1.6F, 0.2094F, 0.1745F, 0F));
        PartDefinition p_star_fin_left_blade_0 = p_star_fin_left.addOrReplaceChild("star_fin_left_blade_0", CubeListBuilder.create()
                .texOffs(223, 137).addBox(-0.5F, -1F, -0.435F, 1F, 2F, 8F, new CubeDeformation(-0.3F, -0.2525F, -0.435F)),
                PartPose.offsetAndRotation(0F, -0.345F, 0.23F, 0.1047F, 0F, 0F));
        PartDefinition p_star_fin_left_blade_1 = p_star_fin_left.addOrReplaceChild("star_fin_left_blade_1", CubeListBuilder.create()
                .texOffs(207, 189).addBox(-0.5F, -1F, -0.125F, 1F, 2F, 6F, new CubeDeformation(-0.3F, -0.3675F, -0.125F)),
                PartPose.offsetAndRotation(0F, -0.7475F, 0.23F, 0.384F, 0F, 0F));
        PartDefinition p_star_fin_left_blade_2 = p_star_fin_left.addOrReplaceChild("star_fin_left_blade_2", CubeListBuilder.create()
                .texOffs(104, 207).addBox(-0.5F, -1F, -0.43F, 1F, 2F, 5F, new CubeDeformation(-0.3F, -0.4825F, -0.43F)),
                PartPose.offsetAndRotation(0F, -1.15F, 0.23F, 0.6981F, 0F, 0F));
        PartDefinition p_star_fin_right = p_starwalker_head_head.addOrReplaceChild("star_fin_right", CubeListBuilder.create()
                .texOffs(91, 267).addBox(-0.5F, -1.5F, -1F, 1F, 3F, 2F, new CubeDeformation(-0.1F, -0.465F, -0.08F)),
                PartPose.offsetAndRotation(-5.25F, -5.6F, -1.6F, 0.2094F, -0.1745F, 0F));
        PartDefinition p_star_fin_right_blade_0 = p_star_fin_right.addOrReplaceChild("star_fin_right_blade_0", CubeListBuilder.create()
                .texOffs(0, 148).addBox(-0.5F, -1F, -0.435F, 1F, 2F, 8F, new CubeDeformation(-0.3F, -0.2525F, -0.435F)),
                PartPose.offsetAndRotation(0F, -0.345F, 0.23F, 0.1047F, 0F, 0F));
        PartDefinition p_star_fin_right_blade_1 = p_star_fin_right.addOrReplaceChild("star_fin_right_blade_1", CubeListBuilder.create()
                .texOffs(222, 189).addBox(-0.5F, -1F, -0.125F, 1F, 2F, 6F, new CubeDeformation(-0.3F, -0.3675F, -0.125F)),
                PartPose.offsetAndRotation(0F, -0.7475F, 0.23F, 0.384F, 0F, 0F));
        PartDefinition p_star_fin_right_blade_2 = p_star_fin_right.addOrReplaceChild("star_fin_right_blade_2", CubeListBuilder.create()
                .texOffs(117, 207).addBox(-0.5F, -1F, -0.43F, 1F, 2F, 5F, new CubeDeformation(-0.3F, -0.4825F, -0.43F)),
                PartPose.offsetAndRotation(0F, -1.15F, 0.23F, 0.6981F, 0F, 0F));
        PartDefinition p_starwalker_chest_body = root.addOrReplaceChild("starwalker_chest_body", CubeListBuilder.create()
                .texOffs(204, 249).addBox(-2F, 1.2F, -3.725F, 4F, 4F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.275F))
                .texOffs(159, 298).addBox(-0.5F, 1.7F, -4.2F, 1F, 3F, 1F, new CubeDeformation(-0.095F, -0.15F, -0.25F))
                .texOffs(133, 327).addBox(-1.5F, 2.7F, -4.2F, 3F, 1F, 1F, new CubeDeformation(-0.42F, -0.095F, -0.25F))
                .texOffs(56, 315).addBox(-1F, 2.2F, -4.275F, 2F, 2F, 1F, new CubeDeformation(-0.3925F, -0.3925F, -0.175F))
                .texOffs(63, 315).addBox(-4.9F, -1F, -3.75F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.3F, -0.15F))
                .texOffs(70, 315).addBox(2.9F, -1F, -3.75F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.3F, -0.15F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_star_cape = p_starwalker_chest_body.addOrReplaceChild("star_cape", CubeListBuilder.create()
                .texOffs(150, 198).addBox(-5F, -0.3F, -0.2F, 10F, 6F, 1F, new CubeDeformation(-0.4F, -0.3F, -0.2F)),
                PartPose.offsetAndRotation(0F, 0F, 3.1F, 0.0873F, 0F, 0F));
        PartDefinition p_star_cape_mid = p_star_cape.addOrReplaceChild("star_cape_mid", CubeListBuilder.create()
                .texOffs(50, 243).addBox(-4F, -0.1F, -0.2F, 8F, 4F, 1F, new CubeDeformation(-0.4F, -0.1F, -0.2F)),
                PartPose.offsetAndRotation(0F, 5.2F, 0.05F, 0.1047F, 0F, 0F));
        PartDefinition p_star_cape_tip = p_star_cape_mid.addOrReplaceChild("star_cape_tip", CubeListBuilder.create()
                .texOffs(65, 273).addBox(-2.5F, -0.1F, -0.2F, 5F, 3F, 1F, new CubeDeformation(-0.3F, -0.1F, -0.2F))
                .texOffs(77, 315).addBox(-1F, 2.5F, -0.2F, 2F, 2F, 1F, new CubeDeformation(0F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(0F, 3.6F, 0.05F, 0.1047F, 0F, 0F));
        PartDefinition p_starwalker_chest_left_arm = root.addOrReplaceChild("starwalker_chest_left_arm", CubeListBuilder.create()
                .texOffs(203, 159).addBox(-2.5F, 5.85F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.2F, -0.15F, -0.2F))
                .texOffs(55, 334).addBox(0.5F, 6.35F, -3.95F, 1F, 1F, 1F, new CubeDeformation(0F, 0F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_star_pauldron_left_arm = p_starwalker_chest_left_arm.addOrReplaceChild("star_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(186, 93).addBox(-3.2F, -1.7F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.2F, -0.4F))
                .texOffs(0, 137).addBox(-2.05F, -0.25F, -4F, 6F, 2F, 8F, new CubeDeformation(-0.05F, -0.35F, -0.3F))
                .texOffs(183, 169).addBox(-0.5F, -2.3F, -4F, 1F, 1F, 8F, new CubeDeformation(0F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(1F, -3.1F, 0F, 0F, 0F, 0.2094F));
        PartDefinition p_star_float_a_left_arm_0 = p_star_pauldron_left_arm.addOrReplaceChild("star_float_a_left_arm_0", CubeListBuilder.create()
                .texOffs(235, 323).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.05F, -0.45F, -0.05F)),
                PartPose.offsetAndRotation(1.6F, -3.6F, -1.2F, 0.1396F, 0F, 0.2443F));
        PartDefinition p_star_float_a_left_arm_1 = p_star_float_a_left_arm_0.addOrReplaceChild("star_float_a_left_arm_1", CubeListBuilder.create()
                .texOffs(60, 334).addBox(-0.5F, -1F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.2525F, 0F, -0.2525F)),
                PartPose.offset(0F, -0.85F, 0F));
        PartDefinition p_star_float_b_left_arm_0 = p_star_pauldron_left_arm.addOrReplaceChild("star_float_b_left_arm_0", CubeListBuilder.create()
                .texOffs(65, 334).addBox(-0.5F, -0.9125F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.15F, -0.0875F, -0.15F)),
                PartPose.offsetAndRotation(0.4F, -3F, 1.5F, -0.1745F, 0F, -0.1745F));
        PartDefinition p_star_float_b_left_arm_1 = p_star_float_b_left_arm_0.addOrReplaceChild("star_float_b_left_arm_1", CubeListBuilder.create()
                .texOffs(70, 334).addBox(-0.5F, -0.875F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.3075F, -0.125F, -0.3075F)),
                PartPose.offset(0F, -0.575F, 0F));
        PartDefinition p_starwalker_chest_right_arm = root.addOrReplaceChild("starwalker_chest_right_arm", CubeListBuilder.create()
                .texOffs(0, 169).addBox(-4.5F, 5.85F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.2F, -0.15F, -0.2F))
                .texOffs(75, 334).addBox(-1.5F, 6.35F, -3.95F, 1F, 1F, 1F, new CubeDeformation(0F, 0F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_star_pauldron_right_arm = p_starwalker_chest_right_arm.addOrReplaceChild("star_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(217, 93).addBox(-3.8F, -1.7F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.2F, -0.4F))
                .texOffs(29, 137).addBox(-3.95F, -0.25F, -4F, 6F, 2F, 8F, new CubeDeformation(-0.05F, -0.35F, -0.3F))
                .texOffs(202, 169).addBox(-0.5F, -2.3F, -4F, 1F, 1F, 8F, new CubeDeformation(0F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(-1F, -3.1F, 0F, 0F, 0F, -0.2094F));
        PartDefinition p_star_float_a_right_arm_0 = p_star_pauldron_right_arm.addOrReplaceChild("star_float_a_right_arm_0", CubeListBuilder.create()
                .texOffs(240, 323).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.05F, -0.45F, -0.05F)),
                PartPose.offsetAndRotation(-1.6F, -3.6F, -1.2F, 0.1396F, 0F, -0.2443F));
        PartDefinition p_star_float_a_right_arm_1 = p_star_float_a_right_arm_0.addOrReplaceChild("star_float_a_right_arm_1", CubeListBuilder.create()
                .texOffs(80, 334).addBox(-0.5F, -1F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.2525F, 0F, -0.2525F)),
                PartPose.offset(0F, -0.85F, 0F));
        PartDefinition p_star_float_b_right_arm_0 = p_star_pauldron_right_arm.addOrReplaceChild("star_float_b_right_arm_0", CubeListBuilder.create()
                .texOffs(85, 334).addBox(-0.5F, -0.9125F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.15F, -0.0875F, -0.15F)),
                PartPose.offsetAndRotation(-0.4F, -3F, 1.5F, -0.1745F, 0F, 0.1745F));
        PartDefinition p_star_float_b_right_arm_1 = p_star_float_b_right_arm_0.addOrReplaceChild("star_float_b_right_arm_1", CubeListBuilder.create()
                .texOffs(90, 334).addBox(-0.5F, -0.875F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.3075F, -0.125F, -0.3075F)),
                PartPose.offset(0F, -0.575F, 0F));
        PartDefinition p_starwalker_legs_body = root.addOrReplaceChild("starwalker_legs_body", CubeListBuilder.create()
                .texOffs(187, 303).addBox(-1.5F, 10.1F, -3.775F, 3F, 2F, 1F, new CubeDeformation(-0.4F, 0F, -0.275F))
                .texOffs(84, 315).addBox(-1F, 10.1F, -4.1F, 2F, 2F, 1F, new CubeDeformation(-0.35F, -0.35F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_starwalker_legs_left_leg = root.addOrReplaceChild("starwalker_legs_left_leg", CubeListBuilder.create()
                .texOffs(78, 273).addBox(-2.5F, 3.45F, -3.275F, 5F, 3F, 1F, new CubeDeformation(-0.3F, -0.05F, -0.125F))
                .texOffs(95, 334).addBox(-0.5F, 4.4F, -3.85F, 1F, 1F, 1F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(215, 236).addBox(2F, 3.45F, -3F, 1F, 3F, 3F, new CubeDeformation(-0.15F, -0.45F, -0.4F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_starwalker_legs_right_leg = root.addOrReplaceChild("starwalker_legs_right_leg", CubeListBuilder.create()
                .texOffs(91, 273).addBox(-2.5F, 3.45F, -3.275F, 5F, 3F, 1F, new CubeDeformation(-0.3F, -0.05F, -0.125F))
                .texOffs(100, 334).addBox(-0.5F, 4.4F, -3.85F, 1F, 1F, 1F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(224, 236).addBox(-3F, 3.45F, -3F, 1F, 3F, 3F, new CubeDeformation(-0.15F, -0.45F, -0.4F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_starwalker_feet_left_leg = root.addOrReplaceChild("starwalker_feet_left_leg", CubeListBuilder.create()
                .texOffs(30, 249).addBox(-2.5F, 10.1F, -4.4F, 5F, 3F, 2F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_star_heel_left_leg = p_starwalker_feet_left_leg.addOrReplaceChild("star_heel_left_leg", CubeListBuilder.create()
                .texOffs(245, 323).addBox(-0.5F, -1F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1F, -0.442F, -0.004F)),
                PartPose.offsetAndRotation(1.2F, 9.6F, 2.7F, 0.2443F, 0.1396F, 0F));
        PartDefinition p_star_heel_left_leg_blade_0 = p_star_heel_left_leg.addOrReplaceChild("star_heel_left_leg_blade_0", CubeListBuilder.create()
                .texOffs(215, 249).addBox(-0.5F, -0.5F, -0.078F, 1F, 1F, 4F, new CubeDeformation(-0.3F, -0.097F, -0.078F)),
                PartPose.offsetAndRotation(0F, -0.186F, 0.124F, 0.1047F, 0F, 0F));
        PartDefinition p_star_heel_left_leg_blade_1 = p_star_heel_left_leg.addOrReplaceChild("star_heel_left_leg_blade_1", CubeListBuilder.create()
                .texOffs(226, 249).addBox(-0.5F, -0.5F, -0.45F, 1F, 1F, 4F, new CubeDeformation(-0.3F, -0.159F, -0.45F)),
                PartPose.offsetAndRotation(0F, -0.403F, 0.124F, 0.384F, 0F, 0F));
        PartDefinition p_star_heel_left_leg_blade_2 = p_star_heel_left_leg.addOrReplaceChild("star_heel_left_leg_blade_2", CubeListBuilder.create()
                .texOffs(162, 293).addBox(-0.5F, -0.5F, -0.384F, 1F, 1F, 3F, new CubeDeformation(-0.3F, -0.221F, -0.384F)),
                PartPose.offsetAndRotation(0F, -0.62F, 0.124F, 0.6981F, 0F, 0F));
        PartDefinition p_starwalker_feet_right_leg = root.addOrReplaceChild("starwalker_feet_right_leg", CubeListBuilder.create()
                .texOffs(45, 249).addBox(-2.5F, 10.1F, -4.4F, 5F, 3F, 2F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_star_heel_right_leg = p_starwalker_feet_right_leg.addOrReplaceChild("star_heel_right_leg", CubeListBuilder.create()
                .texOffs(250, 323).addBox(-0.5F, -1F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1F, -0.442F, -0.004F)),
                PartPose.offsetAndRotation(-1.2F, 9.6F, 2.7F, 0.2443F, -0.1396F, 0F));
        PartDefinition p_star_heel_right_leg_blade_0 = p_star_heel_right_leg.addOrReplaceChild("star_heel_right_leg_blade_0", CubeListBuilder.create()
                .texOffs(237, 249).addBox(-0.5F, -0.5F, -0.078F, 1F, 1F, 4F, new CubeDeformation(-0.3F, -0.097F, -0.078F)),
                PartPose.offsetAndRotation(0F, -0.186F, 0.124F, 0.1047F, 0F, 0F));
        PartDefinition p_star_heel_right_leg_blade_1 = p_star_heel_right_leg.addOrReplaceChild("star_heel_right_leg_blade_1", CubeListBuilder.create()
                .texOffs(0, 255).addBox(-0.5F, -0.5F, -0.45F, 1F, 1F, 4F, new CubeDeformation(-0.3F, -0.159F, -0.45F)),
                PartPose.offsetAndRotation(0F, -0.403F, 0.124F, 0.384F, 0F, 0F));
        PartDefinition p_star_heel_right_leg_blade_2 = p_star_heel_right_leg.addOrReplaceChild("star_heel_right_leg_blade_2", CubeListBuilder.create()
                .texOffs(171, 293).addBox(-0.5F, -0.5F, -0.384F, 1F, 1F, 3F, new CubeDeformation(-0.3F, -0.221F, -0.384F)),
                PartPose.offsetAndRotation(0F, -0.62F, 0.124F, 0.6981F, 0F, 0F));
        PartDefinition p_timewarden_head_head = root.addOrReplaceChild("timewarden_head_head", CubeListBuilder.create()
                .texOffs(130, 189).addBox(-0.5F, -9.95F, -3.5F, 1F, 1F, 7F, new CubeDeformation(0F, -0.05F, -0.3F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_tw_hand = p_timewarden_head_head.addOrReplaceChild("tw_hand", CubeListBuilder.create()
                .texOffs(0, 327).addBox(-0.5F, -2F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.15F, 0F, -0.05F))
                .texOffs(180, 293).addBox(-0.5F, -3.2F, -1.5F, 1F, 1F, 3F, new CubeDeformation(-0.15F, -0.2F, -0.15F))
                .texOffs(105, 334).addBox(-0.5F, -2.6F, -1.55F, 1F, 1F, 1F, new CubeDeformation(-0.15F, -0.2F, -0.2F))
                .texOffs(110, 334).addBox(-0.5F, -2.6F, 0.55F, 1F, 1F, 1F, new CubeDeformation(-0.15F, -0.2F, -0.2F))
                .texOffs(189, 293).addBox(-0.5F, -2.15F, -1.5F, 1F, 1F, 3F, new CubeDeformation(-0.15F, -0.25F, -0.15F))
                .texOffs(164, 298).addBox(-0.5F, -5.75F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.2F, -0.15F, -0.1F))
                .texOffs(198, 293).addBox(-0.5F, -6.6F, -1.5F, 1F, 1F, 3F, new CubeDeformation(-0.2F, 0F, -0.3F))
                .texOffs(91, 315).addBox(-0.5F, -7.5F, -1F, 1F, 1F, 2F, new CubeDeformation(-0.2F, -0.1F, -0.25F))
                .texOffs(115, 334).addBox(-0.5F, -8.25F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.25F, -0.15F, -0.2F)),
                PartPose.offsetAndRotation(0F, -9.6F, -0.6F, -0.2443F, 0F, 0F));
        PartDefinition p_tw_gear_left = p_timewarden_head_head.addOrReplaceChild("tw_gear_left", CubeListBuilder.create()
                .texOffs(11, 255).addBox(-2F, -2F, -0.5F, 4F, 4F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.15F)),
                PartPose.offsetAndRotation(5.35F, -5.4F, 0.4F, 0F, 1.5708F, 0F));
        PartDefinition p_tw_gear_right = p_timewarden_head_head.addOrReplaceChild("tw_gear_right", CubeListBuilder.create()
                .texOffs(22, 255).addBox(-2F, -2F, -0.5F, 4F, 4F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.15F)),
                PartPose.offsetAndRotation(-5.35F, -5.4F, 0.4F, 0F, -1.5708F, 0F));
        PartDefinition p_tw_gear_left_small = p_timewarden_head_head.addOrReplaceChild("tw_gear_left_small", CubeListBuilder.create()
                .texOffs(98, 315).addBox(-1F, -1F, -0.5F, 2F, 2F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.2F)),
                PartPose.offsetAndRotation(5.25F, -3.4F, -2.2F, 0F, 1.5708F, 0F));
        PartDefinition p_tw_gear_right_small = p_timewarden_head_head.addOrReplaceChild("tw_gear_right_small", CubeListBuilder.create()
                .texOffs(105, 315).addBox(-1F, -1F, -0.5F, 2F, 2F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.2F)),
                PartPose.offsetAndRotation(-5.25F, -3.4F, -2.2F, 0F, -1.5708F, 0F));
        PartDefinition p_timewarden_chest_body = root.addOrReplaceChild("timewarden_chest_body", CubeListBuilder.create()
                .texOffs(45, 198).addBox(-0.5F, 0.6F, 2.8F, 1F, 7F, 1F, new CubeDeformation(-0.2F, 0F, -0.25F))
                .texOffs(112, 315).addBox(-4.8F, -1F, -3.75F, 2F, 2F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.2F))
                .texOffs(119, 315).addBox(2.8F, -1F, -3.75F, 2F, 2F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.2F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_tw_dial = p_timewarden_chest_body.addOrReplaceChild("tw_dial", CubeListBuilder.create()
                .texOffs(13, 236).addBox(-2.5F, -2.5F, -0.775F, 5F, 5F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.225F)),
                PartPose.offset(0F, 3.6F, -3.35F));
        PartDefinition p_tw_bob = p_timewarden_chest_body.addOrReplaceChild("tw_bob", CubeListBuilder.create()
                .texOffs(33, 255).addBox(-2F, -2F, -0.5F, 4F, 4F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.15F)),
                PartPose.offset(0F, 8.6F, 3.45F));
        PartDefinition p_timewarden_chest_left_arm = root.addOrReplaceChild("timewarden_chest_left_arm", CubeListBuilder.create()
                .texOffs(29, 169).addBox(-2.5F, 5.85F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.2F, -0.15F, -0.2F))
                .texOffs(120, 334).addBox(0.5F, 6.35F, -3.95F, 1F, 1F, 1F, new CubeDeformation(0F, 0F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_tw_pauldron_left_arm = p_timewarden_chest_left_arm.addOrReplaceChild("tw_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(0, 104).addBox(-3.2F, -1.75F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F))
                .texOffs(58, 137).addBox(-2.15F, -0.25F, -4F, 6F, 2F, 8F, new CubeDeformation(-0.05F, -0.35F, -0.3F))
                .texOffs(141, 169).addBox(-1F, -2.4F, -4F, 2F, 1F, 8F, new CubeDeformation(-0.4F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(1F, -3.1F, 0F, 0F, 0F, 0.2094F));
        PartDefinition p_tw_shoulder_gear_left_arm = p_tw_pauldron_left_arm.addOrReplaceChild("tw_shoulder_gear_left_arm", CubeListBuilder.create()
                .texOffs(207, 293).addBox(-1.5F, -1.5F, -0.5F, 3F, 3F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.2F)),
                PartPose.offsetAndRotation(3.9F, -0.6F, 0F, 0F, 1.5708F, 0F));
        PartDefinition p_timewarden_chest_right_arm = root.addOrReplaceChild("timewarden_chest_right_arm", CubeListBuilder.create()
                .texOffs(58, 169).addBox(-4.5F, 5.85F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.2F, -0.15F, -0.2F))
                .texOffs(125, 334).addBox(-1.5F, 6.35F, -3.95F, 1F, 1F, 1F, new CubeDeformation(0F, 0F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_tw_pauldron_right_arm = p_timewarden_chest_right_arm.addOrReplaceChild("tw_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(31, 104).addBox(-3.8F, -1.75F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F))
                .texOffs(87, 137).addBox(-3.85F, -0.25F, -4F, 6F, 2F, 8F, new CubeDeformation(-0.05F, -0.35F, -0.3F))
                .texOffs(162, 169).addBox(-1F, -2.4F, -4F, 2F, 1F, 8F, new CubeDeformation(-0.4F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(-1F, -3.1F, 0F, 0F, 0F, -0.2094F));
        PartDefinition p_tw_shoulder_gear_right_arm = p_tw_pauldron_right_arm.addOrReplaceChild("tw_shoulder_gear_right_arm", CubeListBuilder.create()
                .texOffs(216, 293).addBox(-1.5F, -1.5F, -0.5F, 3F, 3F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.2F)),
                PartPose.offsetAndRotation(-3.9F, -0.6F, 0F, 0F, -1.5708F, 0F));
        PartDefinition p_timewarden_legs_body = root.addOrReplaceChild("timewarden_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_tw_buckle = p_timewarden_legs_body.addOrReplaceChild("tw_buckle", CubeListBuilder.create()
                .texOffs(225, 293).addBox(-1.5F, -1.5F, -0.5F, 3F, 3F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.2F)),
                PartPose.offset(0F, 10.9F, -3.4F));
        PartDefinition p_timewarden_legs_left_leg = root.addOrReplaceChild("timewarden_legs_left_leg", CubeListBuilder.create()
                .texOffs(104, 273).addBox(-2.5F, 3.45F, -3.275F, 5F, 3F, 1F, new CubeDeformation(-0.3F, -0.05F, -0.125F))
                .texOffs(126, 315).addBox(-1F, 3.9F, -3.875F, 2F, 2F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.225F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_timewarden_legs_right_leg = root.addOrReplaceChild("timewarden_legs_right_leg", CubeListBuilder.create()
                .texOffs(117, 273).addBox(-2.5F, 3.45F, -3.275F, 5F, 3F, 1F, new CubeDeformation(-0.3F, -0.05F, -0.125F))
                .texOffs(133, 315).addBox(-1F, 3.9F, -3.875F, 2F, 2F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.225F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_timewarden_feet_left_leg = root.addOrReplaceChild("timewarden_feet_left_leg", CubeListBuilder.create()
                .texOffs(60, 249).addBox(-2.5F, 10.1F, -4.4F, 5F, 3F, 2F, new CubeDeformation(0F, -0.3F, -0.3F))
                .texOffs(25, 303).addBox(-3F, 11.9F, -4.4F, 6F, 1F, 2F, new CubeDeformation(-0.4F, -0.1F, -0.2F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_tw_ankle_left_leg = p_timewarden_feet_left_leg.addOrReplaceChild("tw_ankle_left_leg", CubeListBuilder.create()
                .texOffs(234, 293).addBox(-1.5F, -1.5F, -0.5F, 3F, 3F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.25F)),
                PartPose.offsetAndRotation(3.05F, 8.4F, 0.4F, 0F, 1.5708F, 0F));
        PartDefinition p_timewarden_feet_right_leg = root.addOrReplaceChild("timewarden_feet_right_leg", CubeListBuilder.create()
                .texOffs(75, 249).addBox(-2.5F, 10.1F, -4.4F, 5F, 3F, 2F, new CubeDeformation(0F, -0.3F, -0.3F))
                .texOffs(42, 303).addBox(-3F, 11.9F, -4.4F, 6F, 1F, 2F, new CubeDeformation(-0.4F, -0.1F, -0.2F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_tw_ankle_right_leg = p_timewarden_feet_right_leg.addOrReplaceChild("tw_ankle_right_leg", CubeListBuilder.create()
                .texOffs(243, 293).addBox(-1.5F, -1.5F, -0.5F, 3F, 3F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.25F)),
                PartPose.offsetAndRotation(-3.05F, 8.4F, 0.4F, 0F, -1.5708F, 0F));
        PartDefinition p_crown_chest_body = root.addOrReplaceChild("crown_chest_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_crown_chest_left_arm = root.addOrReplaceChild("crown_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_crown_chest_right_arm = root.addOrReplaceChild("crown_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_crown_legs_body = root.addOrReplaceChild("crown_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_crown_legs_left_leg = root.addOrReplaceChild("crown_legs_left_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_crown_legs_right_leg = root.addOrReplaceChild("crown_legs_right_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_crown_feet_left_leg = root.addOrReplaceChild("crown_feet_left_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_crown_feet_right_leg = root.addOrReplaceChild("crown_feet_right_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_chainmail_feet_left_leg = root.addOrReplaceChild("chainmail_feet_left_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_chainmail_feet_right_leg = root.addOrReplaceChild("chainmail_feet_right_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_turtle_chest_body = root.addOrReplaceChild("turtle_chest_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_turtle_chest_left_arm = root.addOrReplaceChild("turtle_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_turtle_chest_right_arm = root.addOrReplaceChild("turtle_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_turtle_legs_body = root.addOrReplaceChild("turtle_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_turtle_legs_left_leg = root.addOrReplaceChild("turtle_legs_left_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_turtle_legs_right_leg = root.addOrReplaceChild("turtle_legs_right_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_turtle_feet_left_leg = root.addOrReplaceChild("turtle_feet_left_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_turtle_feet_right_leg = root.addOrReplaceChild("turtle_feet_right_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
