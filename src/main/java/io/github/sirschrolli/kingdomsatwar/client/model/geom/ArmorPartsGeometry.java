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
    public static final int TEXTURE_HEIGHT = 320;

    private ArmorPartsGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_steel_head_head = root.addOrReplaceChild("steel_head_head", CubeListBuilder.create()
                .texOffs(95, 0).addBox(-0.5F, -10.75F, -5F, 1F, 2F, 10F, new CubeDeformation(0F, -0.15F, -0.4F))
                .texOffs(60, 155).addBox(-1F, -11.5F, 1.8F, 2F, 2F, 3F, new CubeDeformation(0F, -0.3F, -0.4F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_steel_plume_0 = p_steel_head_head.addOrReplaceChild("steel_plume_0", CubeListBuilder.create()
                .texOffs(15, 134).addBox(-1.5F, -2.8F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.2F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(0F, -10.6F, 3.3F, -0.2094F, 0F, 0F));
        PartDefinition p_steel_plume_1 = p_steel_plume_0.addOrReplaceChild("steel_plume_1", CubeListBuilder.create()
                .texOffs(119, 117).addBox(-1.5F, -3.6F, -1.5F, 3F, 4F, 3F, new CubeDeformation(0F, -0.4F, -0.1F)),
                PartPose.offsetAndRotation(0F, -2.35F, 0F, -0.4887F, 0F, 0F));
        PartDefinition p_steel_plume_2 = p_steel_plume_1.addOrReplaceChild("steel_plume_2", CubeListBuilder.create()
                .texOffs(132, 117).addBox(-1.5F, -3.8F, -1.5F, 3F, 4F, 3F, new CubeDeformation(-0.05F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(0F, -2.95F, 0F, -0.5585F, 0F, 0F));
        PartDefinition p_steel_plume_3 = p_steel_plume_2.addOrReplaceChild("steel_plume_3", CubeListBuilder.create()
                .texOffs(145, 117).addBox(-1.5F, -3.8F, -1.5F, 3F, 4F, 3F, new CubeDeformation(-0.25F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(0F, -3.35F, 0F, -0.5236F, 0F, 0F));
        PartDefinition p_steel_plume_4 = p_steel_plume_3.addOrReplaceChild("steel_plume_4", CubeListBuilder.create()
                .texOffs(44, 148).addBox(-1F, -3.7F, -1F, 2F, 4F, 2F, new CubeDeformation(0F, -0.3F, -0.1F)),
                PartPose.offsetAndRotation(0F, -3.35F, 0F, -0.384F, 0F, 0F));
        PartDefinition p_steel_plume_5 = p_steel_plume_4.addOrReplaceChild("steel_plume_5", CubeListBuilder.create()
                .texOffs(137, 155).addBox(-1F, -3F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.3F, 0F, -0.35F)),
                PartPose.offsetAndRotation(0F, -3.15F, 0F, -0.2443F, 0F, 0F));
        PartDefinition p_steel_chest_body = root.addOrReplaceChild("steel_chest_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_steel_chest_left_arm = root.addOrReplaceChild("steel_chest_left_arm", CubeListBuilder.create()
                .texOffs(0, 117).addBox(3.6F, 3.8F, -2F, 1F, 4F, 4F, new CubeDeformation(0F, -0.4F, -0.3F))
                .texOffs(108, 188).addBox(4.2F, 4.8F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.2F, -0.4F, -0.3F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_steel_pauldron_left_arm = p_steel_chest_left_arm.addOrReplaceChild("steel_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(0, 17).addBox(-3.8F, -2.15F, -4F, 8F, 3F, 8F, new CubeDeformation(-0.4F, -0.45F, -0.3F))
                .texOffs(177, 29).addBox(-2.5F, 0.1F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.2F, -0.1F, -0.2F))
                .texOffs(186, 63).addBox(-1.3F, 1.6F, -4F, 6F, 2F, 8F, new CubeDeformation(-0.2F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.2443F));
        PartDefinition p_steel_chest_right_arm = root.addOrReplaceChild("steel_chest_right_arm", CubeListBuilder.create()
                .texOffs(11, 117).addBox(-4.6F, 3.8F, -2F, 1F, 4F, 4F, new CubeDeformation(0F, -0.4F, -0.3F))
                .texOffs(115, 188).addBox(-5.2F, 4.8F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.2F, -0.4F, -0.3F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_steel_pauldron_right_arm = p_steel_chest_right_arm.addOrReplaceChild("steel_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(33, 17).addBox(-4.2F, -2.15F, -4F, 8F, 3F, 8F, new CubeDeformation(-0.4F, -0.45F, -0.3F))
                .texOffs(208, 29).addBox(-4.5F, 0.1F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.2F, -0.1F, -0.2F))
                .texOffs(215, 63).addBox(-4.7F, 1.6F, -4F, 6F, 2F, 8F, new CubeDeformation(-0.2F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.2443F));
        PartDefinition p_steel_legs_body = root.addOrReplaceChild("steel_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_steel_legs_left_leg = root.addOrReplaceChild("steel_legs_left_leg", CubeListBuilder.create()
                .texOffs(171, 167).addBox(-2F, 5F, -3.3F, 4F, 3F, 1F, new CubeDeformation(0F, -0.1F, 0F))
                .texOffs(146, 155).addBox(2F, 5.5F, -3.2F, 1F, 2F, 3F, new CubeDeformation(-0.1F, 0F, -0.4F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_steel_tasset_left_leg = p_steel_legs_left_leg.addOrReplaceChild("steel_tasset_left_leg", CubeListBuilder.create()
                .texOffs(149, 126).addBox(-3F, -0.1F, -0.6F, 6F, 5F, 1F, new CubeDeformation(-0.2F, -0.1F, 0F)),
                PartPose.offsetAndRotation(0F, -0.8F, -2.8F, -0.1222F, 0F, 0F));
        PartDefinition p_steel_legs_right_leg = root.addOrReplaceChild("steel_legs_right_leg", CubeListBuilder.create()
                .texOffs(182, 167).addBox(-2F, 5F, -3.3F, 4F, 3F, 1F, new CubeDeformation(0F, -0.1F, 0F))
                .texOffs(155, 155).addBox(-3F, 5.5F, -3.2F, 1F, 2F, 3F, new CubeDeformation(-0.1F, 0F, -0.4F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_steel_tasset_right_leg = p_steel_legs_right_leg.addOrReplaceChild("steel_tasset_right_leg", CubeListBuilder.create()
                .texOffs(164, 126).addBox(-3F, -0.1F, -0.6F, 6F, 5F, 1F, new CubeDeformation(-0.2F, -0.1F, 0F)),
                PartPose.offsetAndRotation(0F, -0.8F, -2.8F, -0.1222F, 0F, 0F));
        PartDefinition p_steel_feet_left_leg = root.addOrReplaceChild("steel_feet_left_leg", CubeListBuilder.create()
                .texOffs(123, 148).addBox(-2.5F, 10.2F, -4.45F, 5F, 3F, 2F, new CubeDeformation(0F, -0.3F, -0.25F))
                .texOffs(61, 193).addBox(-0.5F, 10.45F, 2.75F, 1F, 1F, 2F, new CubeDeformation(-0.15F, -0.15F, -0.05F))
                .texOffs(68, 193).addBox(-1F, 9.95F, 4.25F, 2F, 2F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_steel_feet_right_leg = root.addOrReplaceChild("steel_feet_right_leg", CubeListBuilder.create()
                .texOffs(138, 148).addBox(-2.5F, 10.2F, -4.45F, 5F, 3F, 2F, new CubeDeformation(0F, -0.3F, -0.25F))
                .texOffs(75, 193).addBox(-0.5F, 10.45F, 2.75F, 1F, 1F, 2F, new CubeDeformation(-0.15F, -0.15F, -0.05F))
                .texOffs(82, 193).addBox(-1F, 9.95F, 4.25F, 2F, 2F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_konterit_head_head = root.addOrReplaceChild("konterit_head_head", CubeListBuilder.create()
                .texOffs(89, 193).addBox(-1F, -6.8F, -5.75F, 2F, 2F, 1F, new CubeDeformation(0F, 0F, -0.15F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_kon_wing_left = p_konterit_head_head.addOrReplaceChild("kon_wing_left", CubeListBuilder.create()
                .texOffs(13, 126).addBox(-1F, -2.58F, -1.79F, 2F, 4F, 3F, new CubeDeformation(-0.4F, -0.26F, -0.05F)),
                PartPose.offsetAndRotation(5.2F, -5.2F, -0.8F, 0.2793F, 0.2793F, 0F));
        PartDefinition p_kon_wing_left_feather_0 = p_kon_wing_left.addOrReplaceChild("kon_wing_left_feather_0", CubeListBuilder.create()
                .texOffs(0, 107).addBox(-0.5F, -1F, -0.165F, 1F, 2F, 7F, new CubeDeformation(-0.25F, -0.0575F, -0.165F)),
                PartPose.offsetAndRotation(0F, -0.58F, 0.58F, 0.0698F, 0F, 0F));
        PartDefinition p_kon_wing_left_feather_1 = p_kon_wing_left.addOrReplaceChild("kon_wing_left_feather_1", CubeListBuilder.create()
                .texOffs(56, 29).addBox(-0.5F, -1F, -0.44F, 1F, 2F, 9F, new CubeDeformation(-0.25F, -0.0575F, -0.44F)),
                PartPose.offsetAndRotation(0F, -0.9425F, 0.58F, 0.3491F, 0F, 0F));
        PartDefinition p_kon_wing_left_feather_2 = p_kon_wing_left.addOrReplaceChild("kon_wing_left_feather_2", CubeListBuilder.create()
                .texOffs(77, 29).addBox(-0.5F, -1F, -0.295F, 1F, 2F, 9F, new CubeDeformation(-0.25F, -0.13F, -0.295F)),
                PartPose.offsetAndRotation(0F, -1.305F, 0.58F, 0.6283F, 0F, 0F));
        PartDefinition p_kon_wing_left_feather_3 = p_kon_wing_left.addOrReplaceChild("kon_wing_left_feather_3", CubeListBuilder.create()
                .texOffs(232, 85).addBox(-0.5F, -1F, -0.375F, 1F, 2F, 8F, new CubeDeformation(-0.25F, -0.2025F, -0.375F)),
                PartPose.offsetAndRotation(0F, -1.6675F, 0.58F, 0.9076F, 0F, 0F));
        PartDefinition p_kon_wing_left_feather_4 = p_kon_wing_left.addOrReplaceChild("kon_wing_left_feather_4", CubeListBuilder.create()
                .texOffs(184, 107).addBox(-0.5F, -1F, -0.245F, 1F, 2F, 6F, new CubeDeformation(-0.25F, -0.275F, -0.245F)),
                PartPose.offsetAndRotation(0F, -2.03F, 0.58F, 1.1868F, 0F, 0F));
        PartDefinition p_kon_wing_right = p_konterit_head_head.addOrReplaceChild("kon_wing_right", CubeListBuilder.create()
                .texOffs(24, 126).addBox(-1F, -2.58F, -1.79F, 2F, 4F, 3F, new CubeDeformation(-0.4F, -0.26F, -0.05F)),
                PartPose.offsetAndRotation(-5.2F, -5.2F, -0.8F, 0.2793F, -0.2793F, 0F));
        PartDefinition p_kon_wing_right_feather_0 = p_kon_wing_right.addOrReplaceChild("kon_wing_right_feather_0", CubeListBuilder.create()
                .texOffs(17, 107).addBox(-0.5F, -1F, -0.165F, 1F, 2F, 7F, new CubeDeformation(-0.25F, -0.0575F, -0.165F)),
                PartPose.offsetAndRotation(0F, -0.58F, 0.58F, 0.0698F, 0F, 0F));
        PartDefinition p_kon_wing_right_feather_1 = p_kon_wing_right.addOrReplaceChild("kon_wing_right_feather_1", CubeListBuilder.create()
                .texOffs(98, 29).addBox(-0.5F, -1F, -0.44F, 1F, 2F, 9F, new CubeDeformation(-0.25F, -0.0575F, -0.44F)),
                PartPose.offsetAndRotation(0F, -0.9425F, 0.58F, 0.3491F, 0F, 0F));
        PartDefinition p_kon_wing_right_feather_2 = p_kon_wing_right.addOrReplaceChild("kon_wing_right_feather_2", CubeListBuilder.create()
                .texOffs(119, 29).addBox(-0.5F, -1F, -0.295F, 1F, 2F, 9F, new CubeDeformation(-0.25F, -0.13F, -0.295F)),
                PartPose.offsetAndRotation(0F, -1.305F, 0.58F, 0.6283F, 0F, 0F));
        PartDefinition p_kon_wing_right_feather_3 = p_kon_wing_right.addOrReplaceChild("kon_wing_right_feather_3", CubeListBuilder.create()
                .texOffs(0, 96).addBox(-0.5F, -1F, -0.375F, 1F, 2F, 8F, new CubeDeformation(-0.25F, -0.2025F, -0.375F)),
                PartPose.offsetAndRotation(0F, -1.6675F, 0.58F, 0.9076F, 0F, 0F));
        PartDefinition p_kon_wing_right_feather_4 = p_kon_wing_right.addOrReplaceChild("kon_wing_right_feather_4", CubeListBuilder.create()
                .texOffs(199, 107).addBox(-0.5F, -1F, -0.245F, 1F, 2F, 6F, new CubeDeformation(-0.25F, -0.275F, -0.245F)),
                PartPose.offsetAndRotation(0F, -2.03F, 0.58F, 1.1868F, 0F, 0F));
        PartDefinition p_konterit_chest_body = root.addOrReplaceChild("konterit_chest_body", CubeListBuilder.create()
                .texOffs(96, 193).addBox(-5.2F, -1.1F, -3.65F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.3F, -0.15F))
                .texOffs(103, 193).addBox(3.2F, -1.1F, -3.65F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.3F, -0.15F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_kon_cape = p_konterit_chest_body.addOrReplaceChild("kon_cape", CubeListBuilder.create()
                .texOffs(125, 107).addBox(-4.5F, -0.2F, -0.2F, 9F, 7F, 1F, new CubeDeformation(0F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(0F, 0.2F, 3F, 0.1047F, 0F, 0F));
        PartDefinition p_kon_cape_hem = p_kon_cape.addOrReplaceChild("kon_cape_hem", CubeListBuilder.create()
                .texOffs(81, 117).addBox(-4.5F, -0.4F, -0.2F, 9F, 6F, 1F, new CubeDeformation(-0.2F, -0.4F, -0.2F)),
                PartPose.offsetAndRotation(0F, 6.4F, 0.05F, 0.1396F, 0F, 0F));
        PartDefinition p_konterit_chest_left_arm = root.addOrReplaceChild("konterit_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_kon_pauldron_left_arm = p_konterit_chest_left_arm.addOrReplaceChild("kon_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(19, 96).addBox(-3.2F, -1.55F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.2F, -0.25F, 0F))
                .texOffs(146, 107).addBox(-1F, -2.05F, -3.5F, 2F, 1F, 7F, new CubeDeformation(-0.4F, -0.15F, 0F))
                .texOffs(25, 193).addBox(1F, -2.2F, -1F, 2F, 1F, 2F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offsetAndRotation(1F, -3.2F, 0F, 0F, 0F, 0.2094F));
        PartDefinition p_konterit_chest_right_arm = root.addOrReplaceChild("konterit_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_kon_pauldron_right_arm = p_konterit_chest_right_arm.addOrReplaceChild("kon_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(48, 96).addBox(-3.8F, -1.55F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.2F, -0.25F, 0F))
                .texOffs(165, 107).addBox(-1F, -2.05F, -3.5F, 2F, 1F, 7F, new CubeDeformation(-0.4F, -0.15F, 0F))
                .texOffs(34, 193).addBox(-3F, -2.2F, -1F, 2F, 1F, 2F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offsetAndRotation(-1F, -3.2F, 0F, 0F, 0F, -0.2094F));
        PartDefinition p_konterit_legs_body = root.addOrReplaceChild("konterit_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_konterit_legs_left_leg = root.addOrReplaceChild("konterit_legs_left_leg", CubeListBuilder.create()
                .texOffs(164, 155).addBox(-1.5F, 4.2F, -3.1F, 3F, 4F, 1F, new CubeDeformation(0F, -0.4F, -0.1F))
                .texOffs(110, 193).addBox(-1F, 5.2F, -3.65F, 2F, 2F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_konterit_legs_right_leg = root.addOrReplaceChild("konterit_legs_right_leg", CubeListBuilder.create()
                .texOffs(173, 155).addBox(-1.5F, 4.2F, -3.1F, 3F, 4F, 1F, new CubeDeformation(0F, -0.4F, -0.1F))
                .texOffs(117, 193).addBox(-1F, 5.2F, -3.65F, 2F, 2F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_konterit_feet_left_leg = root.addOrReplaceChild("konterit_feet_left_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_kon_heel_left_leg = p_konterit_feet_left_leg.addOrReplaceChild("kon_heel_left_leg", CubeListBuilder.create()
                .texOffs(182, 155).addBox(-1F, -1.84F, -1.17F, 2F, 3F, 2F, new CubeDeformation(-0.4F, -0.48F, -0.15F)),
                PartPose.offsetAndRotation(2.9F, 9.6F, 0.2F, 0.4189F, 0.2443F, 0F));
        PartDefinition p_kon_heel_left_leg_feather_0 = p_kon_heel_left_leg.addOrReplaceChild("kon_heel_left_leg_feather_0", CubeListBuilder.create()
                .texOffs(195, 141).addBox(-0.5F, -1F, -0.045F, 1F, 2F, 4F, new CubeDeformation(-0.25F, -0.4475F, -0.045F)),
                PartPose.offsetAndRotation(0F, -0.34F, 0.34F, 0.0698F, 0F, 0F));
        PartDefinition p_kon_heel_left_leg_feather_1 = p_kon_heel_left_leg.addOrReplaceChild("kon_heel_left_leg_feather_1", CubeListBuilder.create()
                .texOffs(158, 117).addBox(-0.5F, -1F, -0.12F, 1F, 2F, 5F, new CubeDeformation(-0.25F, -0.4475F, -0.12F)),
                PartPose.offsetAndRotation(0F, -0.5525F, 0.34F, 0.3491F, 0F, 0F));
        PartDefinition p_kon_heel_left_leg_feather_2 = p_kon_heel_left_leg.addOrReplaceChild("kon_heel_left_leg_feather_2", CubeListBuilder.create()
                .texOffs(171, 117).addBox(-0.5F, -1F, -0.035F, 1F, 2F, 5F, new CubeDeformation(-0.25F, -0.49F, -0.035F)),
                PartPose.offsetAndRotation(0F, -0.765F, 0.34F, 0.6283F, 0F, 0F));
        PartDefinition p_kon_heel_left_leg_feather_3 = p_kon_heel_left_leg.addOrReplaceChild("kon_heel_left_leg_feather_3", CubeListBuilder.create()
                .texOffs(28, 134).addBox(-0.5F, -0.5F, -0.375F, 1F, 1F, 5F, new CubeDeformation(-0.25F, -0.0325F, -0.375F)),
                PartPose.offsetAndRotation(0F, -0.9775F, 0.34F, 0.9076F, 0F, 0F));
        PartDefinition p_kon_heel_left_leg_feather_4 = p_kon_heel_left_leg.addOrReplaceChild("kon_heel_left_leg_feather_4", CubeListBuilder.create()
                .texOffs(71, 155).addBox(-0.5F, -0.5F, -0.385F, 1F, 1F, 4F, new CubeDeformation(-0.25F, -0.075F, -0.385F)),
                PartPose.offsetAndRotation(0F, -1.19F, 0.34F, 1.1868F, 0F, 0F));
        PartDefinition p_konterit_feet_right_leg = root.addOrReplaceChild("konterit_feet_right_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_kon_heel_right_leg = p_konterit_feet_right_leg.addOrReplaceChild("kon_heel_right_leg", CubeListBuilder.create()
                .texOffs(191, 155).addBox(-1F, -1.84F, -1.17F, 2F, 3F, 2F, new CubeDeformation(-0.4F, -0.48F, -0.15F)),
                PartPose.offsetAndRotation(-2.9F, 9.6F, 0.2F, 0.4189F, -0.2443F, 0F));
        PartDefinition p_kon_heel_right_leg_feather_0 = p_kon_heel_right_leg.addOrReplaceChild("kon_heel_right_leg_feather_0", CubeListBuilder.create()
                .texOffs(206, 141).addBox(-0.5F, -1F, -0.045F, 1F, 2F, 4F, new CubeDeformation(-0.25F, -0.4475F, -0.045F)),
                PartPose.offsetAndRotation(0F, -0.34F, 0.34F, 0.0698F, 0F, 0F));
        PartDefinition p_kon_heel_right_leg_feather_1 = p_kon_heel_right_leg.addOrReplaceChild("kon_heel_right_leg_feather_1", CubeListBuilder.create()
                .texOffs(184, 117).addBox(-0.5F, -1F, -0.12F, 1F, 2F, 5F, new CubeDeformation(-0.25F, -0.4475F, -0.12F)),
                PartPose.offsetAndRotation(0F, -0.5525F, 0.34F, 0.3491F, 0F, 0F));
        PartDefinition p_kon_heel_right_leg_feather_2 = p_kon_heel_right_leg.addOrReplaceChild("kon_heel_right_leg_feather_2", CubeListBuilder.create()
                .texOffs(197, 117).addBox(-0.5F, -1F, -0.035F, 1F, 2F, 5F, new CubeDeformation(-0.25F, -0.49F, -0.035F)),
                PartPose.offsetAndRotation(0F, -0.765F, 0.34F, 0.6283F, 0F, 0F));
        PartDefinition p_kon_heel_right_leg_feather_3 = p_kon_heel_right_leg.addOrReplaceChild("kon_heel_right_leg_feather_3", CubeListBuilder.create()
                .texOffs(41, 134).addBox(-0.5F, -0.5F, -0.375F, 1F, 1F, 5F, new CubeDeformation(-0.25F, -0.0325F, -0.375F)),
                PartPose.offsetAndRotation(0F, -0.9775F, 0.34F, 0.9076F, 0F, 0F));
        PartDefinition p_kon_heel_right_leg_feather_4 = p_kon_heel_right_leg.addOrReplaceChild("kon_heel_right_leg_feather_4", CubeListBuilder.create()
                .texOffs(82, 155).addBox(-0.5F, -0.5F, -0.385F, 1F, 1F, 4F, new CubeDeformation(-0.25F, -0.075F, -0.385F)),
                PartPose.offsetAndRotation(0F, -1.19F, 0.34F, 1.1868F, 0F, 0F));
        PartDefinition p_nightsteel_head_head = root.addOrReplaceChild("nightsteel_head_head", CubeListBuilder.create()
                .texOffs(183, 188).addBox(-5.5F, -9.5F, -3.9F, 11F, 1F, 2F, new CubeDeformation(-0.2F, 0F, -0.4F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_night_crown_0_0 = p_nightsteel_head_head.addOrReplaceChild("night_crown_0_0", CubeListBuilder.create()
                .texOffs(209, 173).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.25F, -0.285F, -0.25F)),
                PartPose.offsetAndRotation(-3.6F, -9.3F, -2.9F, -0.1396F, 0F, -0.377F));
        PartDefinition p_night_crown_0_1 = p_night_crown_0_0.addOrReplaceChild("night_crown_0_1", CubeListBuilder.create()
                .texOffs(252, 197).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.0875F, -0.35F, -0.0875F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_night_crown_1_0 = p_nightsteel_head_head.addOrReplaceChild("night_crown_1_0", CubeListBuilder.create()
                .texOffs(200, 155).addBox(-1F, -2.545F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.25F, -0.455F, -0.25F)),
                PartPose.offsetAndRotation(-1.8F, -9.3F, -2.9F, -0.1396F, 0F, -0.1885F));
        PartDefinition p_night_crown_1_1 = p_night_crown_1_0.addOrReplaceChild("night_crown_1_1", CubeListBuilder.create()
                .texOffs(0, 201).addBox(-0.5F, -1.95F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.0875F, -0.05F, -0.0875F)),
                PartPose.offset(0F, -1.84F, 0F));
        PartDefinition p_night_crown_2_0 = p_nightsteel_head_head.addOrReplaceChild("night_crown_2_0", CubeListBuilder.create()
                .texOffs(209, 155).addBox(-1F, -2.93F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.1F, -0.07F, -0.1F)),
                PartPose.offsetAndRotation(0F, -9.3F, -2.9F, -0.1396F, 0F, 0F));
        PartDefinition p_night_crown_2_1 = p_night_crown_2_0.addOrReplaceChild("night_crown_2_1", CubeListBuilder.create()
                .texOffs(143, 188).addBox(-0.5F, -2.8F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.005F, -0.2F, -0.005F)),
                PartPose.offset(0F, -2.61F, 0F));
        PartDefinition p_night_crown_3_0 = p_nightsteel_head_head.addOrReplaceChild("night_crown_3_0", CubeListBuilder.create()
                .texOffs(218, 155).addBox(-1F, -2.545F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.25F, -0.455F, -0.25F)),
                PartPose.offsetAndRotation(1.8F, -9.3F, -2.9F, -0.1396F, 0F, 0.1885F));
        PartDefinition p_night_crown_3_1 = p_night_crown_3_0.addOrReplaceChild("night_crown_3_1", CubeListBuilder.create()
                .texOffs(5, 201).addBox(-0.5F, -1.95F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.0875F, -0.05F, -0.0875F)),
                PartPose.offset(0F, -1.84F, 0F));
        PartDefinition p_night_crown_4_0 = p_nightsteel_head_head.addOrReplaceChild("night_crown_4_0", CubeListBuilder.create()
                .texOffs(218, 173).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.25F, -0.285F, -0.25F)),
                PartPose.offsetAndRotation(3.6F, -9.3F, -2.9F, -0.1396F, 0F, 0.377F));
        PartDefinition p_night_crown_4_1 = p_night_crown_4_0.addOrReplaceChild("night_crown_4_1", CubeListBuilder.create()
                .texOffs(10, 201).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.0875F, -0.35F, -0.0875F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_nightsteel_chest_body = root.addOrReplaceChild("nightsteel_chest_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_night_back_0_0 = p_nightsteel_chest_body.addOrReplaceChild("night_back_0_0", CubeListBuilder.create()
                .texOffs(227, 155).addBox(-1F, -2.6F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.2F, -0.4F, -0.2F)),
                PartPose.offsetAndRotation(0F, 1.6F, 2.9F, -0.9076F, 0F, 0F));
        PartDefinition p_night_back_0_1 = p_night_back_0_0.addOrReplaceChild("night_back_0_1", CubeListBuilder.create()
                .texOffs(15, 201).addBox(-0.5F, -2F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.06F, 0F, -0.06F)),
                PartPose.offset(0F, -1.95F, 0F));
        PartDefinition p_night_back_1_0 = p_nightsteel_chest_body.addOrReplaceChild("night_back_1_0", CubeListBuilder.create()
                .texOffs(227, 173).addBox(-1F, -1.88F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.2F, -0.12F, -0.2F)),
                PartPose.offsetAndRotation(0F, 4.6F, 2.9F, -0.9076F, 0F, 0F));
        PartDefinition p_night_back_1_1 = p_night_back_1_0.addOrReplaceChild("night_back_1_1", CubeListBuilder.create()
                .texOffs(20, 201).addBox(-0.5F, -1.8F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.06F, -0.2F, -0.06F)),
                PartPose.offset(0F, -1.51F, 0F));
        PartDefinition p_night_back_2_0 = p_nightsteel_chest_body.addOrReplaceChild("night_back_2_0", CubeListBuilder.create()
                .texOffs(236, 173).addBox(-1F, -1.66F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.2F, -0.34F, -0.2F)),
                PartPose.offsetAndRotation(0F, 7.6F, 2.9F, -0.9076F, 0F, 0F));
        PartDefinition p_night_back_2_1 = p_night_back_2_0.addOrReplaceChild("night_back_2_1", CubeListBuilder.create()
                .texOffs(25, 201).addBox(-0.5F, -1.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.06F, -0.4F, -0.06F)),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_nightsteel_chest_left_arm = root.addOrReplaceChild("nightsteel_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_night_pauldron_left_arm = p_nightsteel_chest_left_arm.addOrReplaceChild("night_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(0, 41).addBox(-3.3F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.1745F));
        PartDefinition p_night_shoulder_left_arm_0_0 = p_night_pauldron_left_arm.addOrReplaceChild("night_shoulder_left_arm_0_0", CubeListBuilder.create()
                .texOffs(245, 173).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.285F, -0.3F)),
                PartPose.offsetAndRotation(2.2F, -1.4F, -2.2F, 0F, 0F, 0.4887F));
        PartDefinition p_night_shoulder_left_arm_0_1 = p_night_shoulder_left_arm_0_0.addOrReplaceChild("night_shoulder_left_arm_0_1", CubeListBuilder.create()
                .texOffs(30, 201).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.35F, -0.115F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_night_shoulder_left_arm_1_0 = p_night_pauldron_left_arm.addOrReplaceChild("night_shoulder_left_arm_1_0", CubeListBuilder.create()
                .texOffs(0, 178).addBox(-1F, -1.99F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.01F, -0.3F)),
                PartPose.offsetAndRotation(2.2F, -1.4F, 0F, 0F, 0F, 0.4887F));
        PartDefinition p_night_shoulder_left_arm_1_1 = p_night_shoulder_left_arm_1_0.addOrReplaceChild("night_shoulder_left_arm_1_1", CubeListBuilder.create()
                .texOffs(35, 201).addBox(-0.5F, -1.9F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.1F, -0.115F)),
                PartPose.offset(0F, -1.73F, 0F));
        PartDefinition p_night_shoulder_left_arm_2_0 = p_night_pauldron_left_arm.addOrReplaceChild("night_shoulder_left_arm_2_0", CubeListBuilder.create()
                .texOffs(9, 178).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.285F, -0.3F)),
                PartPose.offsetAndRotation(2.2F, -1.4F, 2.2F, 0F, 0F, 0.4887F));
        PartDefinition p_night_shoulder_left_arm_2_1 = p_night_shoulder_left_arm_2_0.addOrReplaceChild("night_shoulder_left_arm_2_1", CubeListBuilder.create()
                .texOffs(40, 201).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.35F, -0.115F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_nightsteel_chest_right_arm = root.addOrReplaceChild("nightsteel_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_night_pauldron_right_arm = p_nightsteel_chest_right_arm.addOrReplaceChild("night_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(31, 41).addBox(-3.7F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.1745F));
        PartDefinition p_night_shoulder_right_arm_0_0 = p_night_pauldron_right_arm.addOrReplaceChild("night_shoulder_right_arm_0_0", CubeListBuilder.create()
                .texOffs(18, 178).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.285F, -0.3F)),
                PartPose.offsetAndRotation(-2.2F, -1.4F, -2.2F, 0F, 0F, -0.4887F));
        PartDefinition p_night_shoulder_right_arm_0_1 = p_night_shoulder_right_arm_0_0.addOrReplaceChild("night_shoulder_right_arm_0_1", CubeListBuilder.create()
                .texOffs(45, 201).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.35F, -0.115F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_night_shoulder_right_arm_1_0 = p_night_pauldron_right_arm.addOrReplaceChild("night_shoulder_right_arm_1_0", CubeListBuilder.create()
                .texOffs(27, 178).addBox(-1F, -1.99F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.01F, -0.3F)),
                PartPose.offsetAndRotation(-2.2F, -1.4F, 0F, 0F, 0F, -0.4887F));
        PartDefinition p_night_shoulder_right_arm_1_1 = p_night_shoulder_right_arm_1_0.addOrReplaceChild("night_shoulder_right_arm_1_1", CubeListBuilder.create()
                .texOffs(50, 201).addBox(-0.5F, -1.9F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.1F, -0.115F)),
                PartPose.offset(0F, -1.73F, 0F));
        PartDefinition p_night_shoulder_right_arm_2_0 = p_night_pauldron_right_arm.addOrReplaceChild("night_shoulder_right_arm_2_0", CubeListBuilder.create()
                .texOffs(36, 178).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.285F, -0.3F)),
                PartPose.offsetAndRotation(-2.2F, -1.4F, 2.2F, 0F, 0F, -0.4887F));
        PartDefinition p_night_shoulder_right_arm_2_1 = p_night_shoulder_right_arm_2_0.addOrReplaceChild("night_shoulder_right_arm_2_1", CubeListBuilder.create()
                .texOffs(55, 201).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.35F, -0.115F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_nightsteel_legs_body = root.addOrReplaceChild("nightsteel_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_nightsteel_legs_left_leg = root.addOrReplaceChild("nightsteel_legs_left_leg", CubeListBuilder.create()
                .texOffs(193, 167).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(-0.1F, 0F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_night_knee_left_leg_0 = p_nightsteel_legs_left_leg.addOrReplaceChild("night_knee_left_leg_0", CubeListBuilder.create()
                .texOffs(45, 178).addBox(-1F, -1.77F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.23F, -0.35F)),
                PartPose.offsetAndRotation(0F, 6F, -3F, 1.0821F, 0F, 0F));
        PartDefinition p_night_knee_left_leg_1 = p_night_knee_left_leg_0.addOrReplaceChild("night_knee_left_leg_1", CubeListBuilder.create()
                .texOffs(60, 201).addBox(-0.5F, -1.7F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.3F, -0.1425F)),
                PartPose.offset(0F, -1.29F, 0F));
        PartDefinition p_nightsteel_legs_right_leg = root.addOrReplaceChild("nightsteel_legs_right_leg", CubeListBuilder.create()
                .texOffs(204, 167).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(-0.1F, 0F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_night_knee_right_leg_0 = p_nightsteel_legs_right_leg.addOrReplaceChild("night_knee_right_leg_0", CubeListBuilder.create()
                .texOffs(54, 178).addBox(-1F, -1.77F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.23F, -0.35F)),
                PartPose.offsetAndRotation(0F, 6F, -3F, 1.0821F, 0F, 0F));
        PartDefinition p_night_knee_right_leg_1 = p_night_knee_right_leg_0.addOrReplaceChild("night_knee_right_leg_1", CubeListBuilder.create()
                .texOffs(65, 201).addBox(-0.5F, -1.7F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.3F, -0.1425F)),
                PartPose.offset(0F, -1.29F, 0F));
        PartDefinition p_nightsteel_feet_left_leg = root.addOrReplaceChild("nightsteel_feet_left_leg", CubeListBuilder.create()
                .texOffs(145, 167).addBox(-2.5F, 10.25F, -3.9F, 5F, 3F, 1F, new CubeDeformation(-0.1F, -0.45F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_night_heel_left_leg_0 = p_nightsteel_feet_left_leg.addOrReplaceChild("night_heel_left_leg_0", CubeListBuilder.create()
                .texOffs(63, 178).addBox(-1F, -1.66F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.34F, -0.4F)),
                PartPose.offsetAndRotation(0F, 11F, 2.8F, -1.0123F, 0F, 0F));
        PartDefinition p_night_heel_left_leg_1 = p_night_heel_left_leg_0.addOrReplaceChild("night_heel_left_leg_1", CubeListBuilder.create()
                .texOffs(70, 201).addBox(-0.5F, -1.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.17F, -0.4F, -0.17F)),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_nightsteel_feet_right_leg = root.addOrReplaceChild("nightsteel_feet_right_leg", CubeListBuilder.create()
                .texOffs(158, 167).addBox(-2.5F, 10.25F, -3.9F, 5F, 3F, 1F, new CubeDeformation(-0.1F, -0.45F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_night_heel_right_leg_0 = p_nightsteel_feet_right_leg.addOrReplaceChild("night_heel_right_leg_0", CubeListBuilder.create()
                .texOffs(72, 178).addBox(-1F, -1.66F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.34F, -0.4F)),
                PartPose.offsetAndRotation(0F, 11F, 2.8F, -1.0123F, 0F, 0F));
        PartDefinition p_night_heel_right_leg_1 = p_night_heel_right_leg_0.addOrReplaceChild("night_heel_right_leg_1", CubeListBuilder.create()
                .texOffs(75, 201).addBox(-0.5F, -1.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.17F, -0.4F, -0.17F)),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_abyssite_head_head = root.addOrReplaceChild("abyssite_head_head", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_aby_horn_left_0 = p_abyssite_head_head.addOrReplaceChild("aby_horn_left_0", CubeListBuilder.create()
                .texOffs(54, 134).addBox(-1.5F, -3F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offsetAndRotation(4F, -8.6F, -1.2F, -0.2094F, 0F, 0.5934F));
        PartDefinition p_aby_horn_left_1 = p_aby_horn_left_0.addOrReplaceChild("aby_horn_left_1", CubeListBuilder.create()
                .texOffs(67, 134).addBox(-1.5F, -3F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.45F, 0F, -0.45F)),
                PartPose.offsetAndRotation(0F, -2.75F, 0F, -0.4887F, 0F, -0.1745F));
        PartDefinition p_aby_horn_left_2 = p_aby_horn_left_1.addOrReplaceChild("aby_horn_left_2", CubeListBuilder.create()
                .texOffs(236, 155).addBox(-1F, -2.9F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.2F, -0.1F, -0.2F)),
                PartPose.offsetAndRotation(0F, -2.75F, 0F, -0.5934F, 0F, -0.2094F));
        PartDefinition p_aby_horn_left_3 = p_aby_horn_left_2.addOrReplaceChild("aby_horn_left_3", CubeListBuilder.create()
                .texOffs(245, 155).addBox(-1F, -2.7F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.45F, -0.3F, -0.45F)),
                PartPose.offsetAndRotation(0F, -2.55F, 0F, -0.6283F, 0F, -0.1047F));
        PartDefinition p_aby_horn_left_4 = p_aby_horn_left_3.addOrReplaceChild("aby_horn_left_4", CubeListBuilder.create()
                .texOffs(80, 201).addBox(-0.5F, -1.9F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.15F, -0.1F, -0.15F)),
                PartPose.offsetAndRotation(0F, -2.15F, 0F, -0.5236F, 0F, 0F));
        PartDefinition p_aby_horn_right_0 = p_abyssite_head_head.addOrReplaceChild("aby_horn_right_0", CubeListBuilder.create()
                .texOffs(80, 134).addBox(-1.5F, -3F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offsetAndRotation(-4F, -8.6F, -1.2F, -0.2094F, 0F, -0.5934F));
        PartDefinition p_aby_horn_right_1 = p_aby_horn_right_0.addOrReplaceChild("aby_horn_right_1", CubeListBuilder.create()
                .texOffs(93, 134).addBox(-1.5F, -3F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.45F, 0F, -0.45F)),
                PartPose.offsetAndRotation(0F, -2.75F, 0F, -0.4887F, 0F, 0.1745F));
        PartDefinition p_aby_horn_right_2 = p_aby_horn_right_1.addOrReplaceChild("aby_horn_right_2", CubeListBuilder.create()
                .texOffs(0, 161).addBox(-1F, -2.9F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.2F, -0.1F, -0.2F)),
                PartPose.offsetAndRotation(0F, -2.75F, 0F, -0.5934F, 0F, 0.2094F));
        PartDefinition p_aby_horn_right_3 = p_aby_horn_right_2.addOrReplaceChild("aby_horn_right_3", CubeListBuilder.create()
                .texOffs(9, 161).addBox(-1F, -2.7F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.45F, -0.3F, -0.45F)),
                PartPose.offsetAndRotation(0F, -2.55F, 0F, -0.6283F, 0F, 0.1047F));
        PartDefinition p_aby_horn_right_4 = p_aby_horn_right_3.addOrReplaceChild("aby_horn_right_4", CubeListBuilder.create()
                .texOffs(85, 201).addBox(-0.5F, -1.9F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.15F, -0.1F, -0.15F)),
                PartPose.offsetAndRotation(0F, -2.15F, 0F, -0.5236F, 0F, 0F));
        PartDefinition p_aby_brow_0 = p_abyssite_head_head.addOrReplaceChild("aby_brow_0", CubeListBuilder.create()
                .texOffs(81, 178).addBox(-1F, -1.88F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.2F, -0.12F, -0.2F)),
                PartPose.offsetAndRotation(0F, -6.2F, -5.2F, -0.2443F, 0F, 0F));
        PartDefinition p_aby_brow_1 = p_aby_brow_0.addOrReplaceChild("aby_brow_1", CubeListBuilder.create()
                .texOffs(90, 201).addBox(-0.5F, -1.8F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.06F, -0.2F, -0.06F)),
                PartPose.offset(0F, -1.51F, 0F));
        PartDefinition p_abyssite_chest_body = root.addOrReplaceChild("abyssite_chest_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_aby_back_0_0 = p_abyssite_chest_body.addOrReplaceChild("aby_back_0_0", CubeListBuilder.create()
                .texOffs(18, 161).addBox(-1F, -2.82F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.1F, -0.18F, -0.1F)),
                PartPose.offsetAndRotation(0F, 3F, 2.9F, -0.8029F, 0F, 0F));
        PartDefinition p_aby_back_0_1 = p_aby_back_0_0.addOrReplaceChild("aby_back_0_1", CubeListBuilder.create()
                .texOffs(148, 188).addBox(-0.5F, -2.7F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.005F, -0.3F, -0.005F)),
                PartPose.offset(0F, -2.39F, 0F));
        PartDefinition p_aby_back_1_0 = p_abyssite_chest_body.addOrReplaceChild("aby_back_1_0", CubeListBuilder.create()
                .texOffs(90, 178).addBox(-1F, -1.935F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.065F, -0.35F)),
                PartPose.offsetAndRotation(1.7F, 3.8F, 2.9F, -0.6981F, 0F, 0.384F));
        PartDefinition p_aby_back_1_1 = p_aby_back_1_0.addOrReplaceChild("aby_back_1_1", CubeListBuilder.create()
                .texOffs(95, 201).addBox(-0.5F, -1.85F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.15F, -0.1425F)),
                PartPose.offset(0F, -1.62F, 0F));
        PartDefinition p_aby_back_2_0 = p_abyssite_chest_body.addOrReplaceChild("aby_back_2_0", CubeListBuilder.create()
                .texOffs(99, 178).addBox(-1F, -1.935F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.065F, -0.35F)),
                PartPose.offsetAndRotation(-1.7F, 3.8F, 2.9F, -0.6981F, 0F, -0.384F));
        PartDefinition p_aby_back_2_1 = p_aby_back_2_0.addOrReplaceChild("aby_back_2_1", CubeListBuilder.create()
                .texOffs(100, 201).addBox(-0.5F, -1.85F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.15F, -0.1425F)),
                PartPose.offset(0F, -1.62F, 0F));
        PartDefinition p_abyssite_chest_left_arm = root.addOrReplaceChild("abyssite_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_aby_pauldron_left_arm = p_abyssite_chest_left_arm.addOrReplaceChild("aby_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(66, 17).addBox(-3.8F, -2.3F, -4F, 8F, 3F, 8F, new CubeDeformation(-0.2F, -0.3F, -0.1F))
                .texOffs(62, 41).addBox(-2.5F, 0.3F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, 0F, 0F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.2793F));
        PartDefinition p_aby_shoulder_left_arm_0_0 = p_aby_pauldron_left_arm.addOrReplaceChild("aby_shoulder_left_arm_0_0", CubeListBuilder.create()
                .texOffs(108, 178).addBox(-1F, -1.99F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.25F, -0.01F, -0.25F)),
                PartPose.offsetAndRotation(1.4F, -1.9F, -1.4F, 0.1745F, 0F, 0.4189F));
        PartDefinition p_aby_shoulder_left_arm_0_1 = p_aby_shoulder_left_arm_0_0.addOrReplaceChild("aby_shoulder_left_arm_0_1", CubeListBuilder.create()
                .texOffs(105, 201).addBox(-0.5F, -1.9F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.0875F, -0.1F, -0.0875F)),
                PartPose.offset(0F, -1.73F, 0F));
        PartDefinition p_aby_shoulder_left_arm_1_0 = p_aby_pauldron_left_arm.addOrReplaceChild("aby_shoulder_left_arm_1_0", CubeListBuilder.create()
                .texOffs(117, 178).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.285F, -0.45F)),
                PartPose.offsetAndRotation(2.2F, -1.9F, 1.5F, -0.2094F, 0F, 0.6283F));
        PartDefinition p_aby_shoulder_left_arm_1_1 = p_aby_shoulder_left_arm_1_0.addOrReplaceChild("aby_shoulder_left_arm_1_1", CubeListBuilder.create()
                .texOffs(110, 201).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1975F, -0.35F, -0.1975F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_abyssite_chest_right_arm = root.addOrReplaceChild("abyssite_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_aby_pauldron_right_arm = p_abyssite_chest_right_arm.addOrReplaceChild("aby_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(99, 17).addBox(-4.2F, -2.3F, -4F, 8F, 3F, 8F, new CubeDeformation(-0.2F, -0.3F, -0.1F))
                .texOffs(93, 41).addBox(-4.5F, 0.3F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, 0F, 0F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.2793F));
        PartDefinition p_aby_shoulder_right_arm_0_0 = p_aby_pauldron_right_arm.addOrReplaceChild("aby_shoulder_right_arm_0_0", CubeListBuilder.create()
                .texOffs(126, 178).addBox(-1F, -1.99F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.25F, -0.01F, -0.25F)),
                PartPose.offsetAndRotation(-1.4F, -1.9F, -1.4F, 0.1745F, 0F, -0.4189F));
        PartDefinition p_aby_shoulder_right_arm_0_1 = p_aby_shoulder_right_arm_0_0.addOrReplaceChild("aby_shoulder_right_arm_0_1", CubeListBuilder.create()
                .texOffs(115, 201).addBox(-0.5F, -1.9F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.0875F, -0.1F, -0.0875F)),
                PartPose.offset(0F, -1.73F, 0F));
        PartDefinition p_aby_shoulder_right_arm_1_0 = p_aby_pauldron_right_arm.addOrReplaceChild("aby_shoulder_right_arm_1_0", CubeListBuilder.create()
                .texOffs(135, 178).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.285F, -0.45F)),
                PartPose.offsetAndRotation(-2.2F, -1.9F, 1.5F, -0.2094F, 0F, -0.6283F));
        PartDefinition p_aby_shoulder_right_arm_1_1 = p_aby_shoulder_right_arm_1_0.addOrReplaceChild("aby_shoulder_right_arm_1_1", CubeListBuilder.create()
                .texOffs(120, 201).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1975F, -0.35F, -0.1975F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_abyssite_legs_body = root.addOrReplaceChild("abyssite_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_abyssite_legs_left_leg = root.addOrReplaceChild("abyssite_legs_left_leg", CubeListBuilder.create()
                .texOffs(179, 126).addBox(-3F, -0.9F, -3.3F, 6F, 5F, 1F, new CubeDeformation(-0.3F, -0.4F, 0F))
                .texOffs(122, 188).addBox(-1F, 0.1F, -4F, 2F, 3F, 1F, new CubeDeformation(-0.3F, -0.3F, -0.2F))
                .texOffs(215, 167).addBox(-2F, 4.9F, -3.3F, 4F, 3F, 1F, new CubeDeformation(0F, -0.1F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_abyssite_legs_right_leg = root.addOrReplaceChild("abyssite_legs_right_leg", CubeListBuilder.create()
                .texOffs(194, 126).addBox(-3F, -0.9F, -3.3F, 6F, 5F, 1F, new CubeDeformation(-0.3F, -0.4F, 0F))
                .texOffs(129, 188).addBox(-1F, 0.1F, -4F, 2F, 3F, 1F, new CubeDeformation(-0.3F, -0.3F, -0.2F))
                .texOffs(226, 167).addBox(-2F, 4.9F, -3.3F, 4F, 3F, 1F, new CubeDeformation(0F, -0.1F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_abyssite_feet_left_leg = root.addOrReplaceChild("abyssite_feet_left_leg", CubeListBuilder.create()
                .texOffs(153, 148).addBox(-2.5F, 10.25F, -4.45F, 5F, 3F, 2F, new CubeDeformation(0F, -0.45F, -0.45F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_aby_heel_left_leg_0_0 = p_abyssite_feet_left_leg.addOrReplaceChild("aby_heel_left_leg_0_0", CubeListBuilder.create()
                .texOffs(144, 178).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.285F, -0.45F)),
                PartPose.offsetAndRotation(0.9F, 10.8F, 2.8F, -0.9774F, 0F, 0.2094F));
        PartDefinition p_aby_heel_left_leg_0_1 = p_aby_heel_left_leg_0_0.addOrReplaceChild("aby_heel_left_leg_0_1", CubeListBuilder.create()
                .texOffs(125, 201).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1975F, -0.35F, -0.1975F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_aby_heel_left_leg_1_0 = p_abyssite_feet_left_leg.addOrReplaceChild("aby_heel_left_leg_1_0", CubeListBuilder.create()
                .texOffs(130, 201).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.05F, -0.45F, -0.05F)),
                PartPose.offsetAndRotation(-0.9F, 10.8F, 2.8F, -0.9774F, 0F, -0.2094F));
        PartDefinition p_aby_heel_left_leg_1_1 = p_aby_heel_left_leg_1_0.addOrReplaceChild("aby_heel_left_leg_1_1", CubeListBuilder.create()
                .texOffs(30, 209).addBox(-0.5F, -1F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.2525F, 0F, -0.2525F)),
                PartPose.offset(0F, -0.85F, 0F));
        PartDefinition p_abyssite_feet_right_leg = root.addOrReplaceChild("abyssite_feet_right_leg", CubeListBuilder.create()
                .texOffs(168, 148).addBox(-2.5F, 10.25F, -4.45F, 5F, 3F, 2F, new CubeDeformation(0F, -0.45F, -0.45F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_aby_heel_right_leg_0_0 = p_abyssite_feet_right_leg.addOrReplaceChild("aby_heel_right_leg_0_0", CubeListBuilder.create()
                .texOffs(153, 178).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.285F, -0.45F)),
                PartPose.offsetAndRotation(0.9F, 10.8F, 2.8F, -0.9774F, 0F, 0.2094F));
        PartDefinition p_aby_heel_right_leg_0_1 = p_aby_heel_right_leg_0_0.addOrReplaceChild("aby_heel_right_leg_0_1", CubeListBuilder.create()
                .texOffs(135, 201).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1975F, -0.35F, -0.1975F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_aby_heel_right_leg_1_0 = p_abyssite_feet_right_leg.addOrReplaceChild("aby_heel_right_leg_1_0", CubeListBuilder.create()
                .texOffs(140, 201).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.05F, -0.45F, -0.05F)),
                PartPose.offsetAndRotation(-0.9F, 10.8F, 2.8F, -0.9774F, 0F, -0.2094F));
        PartDefinition p_aby_heel_right_leg_1_1 = p_aby_heel_right_leg_1_0.addOrReplaceChild("aby_heel_right_leg_1_1", CubeListBuilder.create()
                .texOffs(35, 209).addBox(-0.5F, -1F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.2525F, 0F, -0.2525F)),
                PartPose.offset(0F, -0.85F, 0F));
        PartDefinition p_fire_head_head = root.addOrReplaceChild("fire_head_head", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_fire_horn_left_0 = p_fire_head_head.addOrReplaceChild("fire_horn_left_0", CubeListBuilder.create()
                .texOffs(106, 134).addBox(-1.5F, -2.9F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.1F, -0.4F)),
                PartPose.offsetAndRotation(3.2F, -8.7F, -1.6F, -0.4189F, 0F, 0.384F));
        PartDefinition p_fire_horn_left_1 = p_fire_horn_left_0.addOrReplaceChild("fire_horn_left_1", CubeListBuilder.create()
                .texOffs(27, 161).addBox(-1F, -2.9F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.1F, -0.1F, -0.1F)),
                PartPose.offsetAndRotation(0F, -2.55F, 0F, -0.5934F, 0F, -0.1047F));
        PartDefinition p_fire_horn_left_2 = p_fire_horn_left_1.addOrReplaceChild("fire_horn_left_2", CubeListBuilder.create()
                .texOffs(36, 161).addBox(-1F, -2.8F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.35F, -0.2F, -0.35F)),
                PartPose.offsetAndRotation(0F, -2.55F, 0F, -0.6632F, 0F, -0.1047F));
        PartDefinition p_fire_horn_left_3 = p_fire_horn_left_2.addOrReplaceChild("fire_horn_left_3", CubeListBuilder.create()
                .texOffs(153, 188).addBox(-0.5F, -2.6F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.1F)),
                PartPose.offsetAndRotation(0F, -2.35F, 0F, -0.5934F, 0F, 0F));
        PartDefinition p_fire_horn_right_0 = p_fire_head_head.addOrReplaceChild("fire_horn_right_0", CubeListBuilder.create()
                .texOffs(119, 134).addBox(-1.5F, -2.9F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.1F, -0.4F)),
                PartPose.offsetAndRotation(-3.2F, -8.7F, -1.6F, -0.4189F, 0F, -0.384F));
        PartDefinition p_fire_horn_right_1 = p_fire_horn_right_0.addOrReplaceChild("fire_horn_right_1", CubeListBuilder.create()
                .texOffs(45, 161).addBox(-1F, -2.9F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.1F, -0.1F, -0.1F)),
                PartPose.offsetAndRotation(0F, -2.55F, 0F, -0.5934F, 0F, 0.1047F));
        PartDefinition p_fire_horn_right_2 = p_fire_horn_right_1.addOrReplaceChild("fire_horn_right_2", CubeListBuilder.create()
                .texOffs(54, 161).addBox(-1F, -2.8F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.35F, -0.2F, -0.35F)),
                PartPose.offsetAndRotation(0F, -2.55F, 0F, -0.6632F, 0F, 0.1047F));
        PartDefinition p_fire_horn_right_3 = p_fire_horn_right_2.addOrReplaceChild("fire_horn_right_3", CubeListBuilder.create()
                .texOffs(158, 188).addBox(-0.5F, -2.6F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.1F)),
                PartPose.offsetAndRotation(0F, -2.35F, 0F, -0.5934F, 0F, 0F));
        PartDefinition p_fire_crest_0_0 = p_fire_head_head.addOrReplaceChild("fire_crest_0_0", CubeListBuilder.create()
                .texOffs(40, 209).addBox(-0.5F, -0.94F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.06F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, -3.2F, -0.4538F, 0F, 0F));
        PartDefinition p_fire_crest_0_1 = p_fire_crest_0_0.addOrReplaceChild("fire_crest_0_1", CubeListBuilder.create()
                .texOffs(45, 209).addBox(-0.5F, -0.9F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.1F, -0.225F)),
                PartPose.offset(0F, -0.63F, 0F));
        PartDefinition p_fire_crest_1_0 = p_fire_head_head.addOrReplaceChild("fire_crest_1_0", CubeListBuilder.create()
                .texOffs(145, 201).addBox(-0.5F, -1.605F, -0.5F, 1F, 2F, 1F, new CubeDeformation(0F, -0.395F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, -0.6F, -0.4538F, 0F, 0F));
        PartDefinition p_fire_crest_1_1 = p_fire_crest_1_0.addOrReplaceChild("fire_crest_1_1", CubeListBuilder.create()
                .texOffs(150, 201).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.225F, -0.45F, -0.225F)),
                PartPose.offset(0F, -0.96F, 0F));
        PartDefinition p_fire_crest_2_0 = p_fire_head_head.addOrReplaceChild("fire_crest_2_0", CubeListBuilder.create()
                .texOffs(50, 209).addBox(-0.5F, -0.995F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.005F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, 2F, -0.4538F, 0F, 0F));
        PartDefinition p_fire_crest_2_1 = p_fire_crest_2_0.addOrReplaceChild("fire_crest_2_1", CubeListBuilder.create()
                .texOffs(55, 209).addBox(-0.5F, -0.95F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.05F, -0.225F)),
                PartPose.offset(0F, -0.74F, 0F));
        PartDefinition p_fire_chest_body = root.addOrReplaceChild("fire_chest_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_fire_back_0_0 = p_fire_chest_body.addOrReplaceChild("fire_back_0_0", CubeListBuilder.create()
                .texOffs(162, 178).addBox(-1F, -1.935F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.065F, -0.3F)),
                PartPose.offsetAndRotation(0F, 0.8F, 2.9F, -0.8727F, 0F, 0F));
        PartDefinition p_fire_back_0_1 = p_fire_back_0_0.addOrReplaceChild("fire_back_0_1", CubeListBuilder.create()
                .texOffs(155, 201).addBox(-0.5F, -1.85F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.15F, -0.115F)),
                PartPose.offset(0F, -1.62F, 0F));
        PartDefinition p_fire_back_1_0 = p_fire_chest_body.addOrReplaceChild("fire_back_1_0", CubeListBuilder.create()
                .texOffs(63, 161).addBox(-1F, -2.545F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.3F, -0.455F, -0.3F)),
                PartPose.offsetAndRotation(0F, 3.6F, 2.9F, -0.8727F, 0F, 0F));
        PartDefinition p_fire_back_1_1 = p_fire_back_1_0.addOrReplaceChild("fire_back_1_1", CubeListBuilder.create()
                .texOffs(160, 201).addBox(-0.5F, -1.95F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.05F, -0.115F)),
                PartPose.offset(0F, -1.84F, 0F));
        PartDefinition p_fire_back_2_0 = p_fire_chest_body.addOrReplaceChild("fire_back_2_0", CubeListBuilder.create()
                .texOffs(171, 178).addBox(-1F, -1.88F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.12F, -0.3F)),
                PartPose.offsetAndRotation(0F, 6.6F, 2.9F, -0.8727F, 0F, 0F));
        PartDefinition p_fire_back_2_1 = p_fire_back_2_0.addOrReplaceChild("fire_back_2_1", CubeListBuilder.create()
                .texOffs(165, 201).addBox(-0.5F, -1.8F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.2F, -0.115F)),
                PartPose.offset(0F, -1.51F, 0F));
        PartDefinition p_fire_back_3_0 = p_fire_chest_body.addOrReplaceChild("fire_back_3_0", CubeListBuilder.create()
                .texOffs(180, 178).addBox(-1F, -1.66F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.34F, -0.3F)),
                PartPose.offsetAndRotation(0F, 9.4F, 2.9F, -0.8727F, 0F, 0F));
        PartDefinition p_fire_back_3_1 = p_fire_back_3_0.addOrReplaceChild("fire_back_3_1", CubeListBuilder.create()
                .texOffs(170, 201).addBox(-0.5F, -1.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.4F, -0.115F)),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_fire_chest_left_arm = root.addOrReplaceChild("fire_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_fire_pauldron_left_arm = p_fire_chest_left_arm.addOrReplaceChild("fire_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(124, 41).addBox(-3.3F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F))
                .texOffs(0, 74).addBox(-2F, -0.1F, -4F, 6F, 2F, 8F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.2094F));
        PartDefinition p_fire_claw_left_arm_0 = p_fire_pauldron_left_arm.addOrReplaceChild("fire_claw_left_arm_0", CubeListBuilder.create()
                .texOffs(189, 178).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.285F, -0.35F)),
                PartPose.offsetAndRotation(2.4F, -1.4F, 0F, 0F, 0F, 0.6981F));
        PartDefinition p_fire_claw_left_arm_1 = p_fire_claw_left_arm_0.addOrReplaceChild("fire_claw_left_arm_1", CubeListBuilder.create()
                .texOffs(175, 201).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.35F, -0.1425F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_fire_chest_right_arm = root.addOrReplaceChild("fire_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_fire_pauldron_right_arm = p_fire_chest_right_arm.addOrReplaceChild("fire_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(155, 41).addBox(-3.7F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F))
                .texOffs(29, 74).addBox(-4F, -0.1F, -4F, 6F, 2F, 8F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.2094F));
        PartDefinition p_fire_claw_right_arm_0 = p_fire_pauldron_right_arm.addOrReplaceChild("fire_claw_right_arm_0", CubeListBuilder.create()
                .texOffs(198, 178).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.285F, -0.35F)),
                PartPose.offsetAndRotation(-2.4F, -1.4F, 0F, 0F, 0F, -0.6981F));
        PartDefinition p_fire_claw_right_arm_1 = p_fire_claw_right_arm_0.addOrReplaceChild("fire_claw_right_arm_1", CubeListBuilder.create()
                .texOffs(180, 201).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.35F, -0.1425F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_fire_legs_body = root.addOrReplaceChild("fire_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_fire_tail_0 = p_fire_legs_body.addOrReplaceChild("fire_tail_0", CubeListBuilder.create()
                .texOffs(132, 134).addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F),
                PartPose.offsetAndRotation(0F, 10.2F, 2.2F, 0.5236F, 0F, 0F));
        PartDefinition p_fire_tail_1 = p_fire_tail_0.addOrReplaceChild("fire_tail_1", CubeListBuilder.create()
                .texOffs(145, 134).addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, 2.75F, 0F, 0.1745F, 0F, 0F));
        PartDefinition p_fire_tail_2 = p_fire_tail_1.addOrReplaceChild("fire_tail_2", CubeListBuilder.create()
                .texOffs(158, 134).addBox(-1.5F, -0.1F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.1F, -0.4F)),
                PartPose.offsetAndRotation(0F, 2.75F, 0F, 0.2094F, 0F, 0F));
        PartDefinition p_fire_tail_3 = p_fire_tail_2.addOrReplaceChild("fire_tail_3", CubeListBuilder.create()
                .texOffs(72, 161).addBox(-1F, -0.2F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.15F, -0.2F, -0.15F)),
                PartPose.offsetAndRotation(0F, 2.55F, 0F, 0.2094F, 0F, 0F));
        PartDefinition p_fire_tail_4 = p_fire_tail_3.addOrReplaceChild("fire_tail_4", CubeListBuilder.create()
                .texOffs(81, 161).addBox(-1F, -0.4F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.4F, -0.4F, -0.4F)),
                PartPose.offsetAndRotation(0F, 2.35F, 0F, 0.1745F, 0F, 0F));
        PartDefinition p_fire_tail_tip = p_fire_tail_4.addOrReplaceChild("fire_tail_tip", CubeListBuilder.create()
                .texOffs(93, 155).addBox(-1.5F, -0.3F, -1F, 3F, 3F, 2F, new CubeDeformation(-0.3F, -0.3F, -0.3F))
                .texOffs(124, 193).addBox(-1F, 2.2F, -0.5F, 2F, 2F, 1F, new CubeDeformation(-0.3F, 0F, -0.1F))
                .texOffs(185, 201).addBox(-0.5F, 3.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.2F, -0.4F, -0.3F)),
                PartPose.offsetAndRotation(0F, 2.4F, 0F, 0.1396F, 0F, 0F));
        PartDefinition p_fire_legs_left_leg = root.addOrReplaceChild("fire_legs_left_leg", CubeListBuilder.create()
                .texOffs(237, 167).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_fire_legs_right_leg = root.addOrReplaceChild("fire_legs_right_leg", CubeListBuilder.create()
                .texOffs(0, 173).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_fire_feet_left_leg = root.addOrReplaceChild("fire_feet_left_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_fire_talon_left_leg_0 = p_fire_feet_left_leg.addOrReplaceChild("fire_talon_left_leg_0", CubeListBuilder.create()
                .texOffs(131, 193).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(-1.6F, 11.6F, -2.8F, 0.4887F, -0.2234F, 0F));
        PartDefinition p_fire_talon_left_leg_1 = p_fire_feet_left_leg.addOrReplaceChild("fire_talon_left_leg_1", CubeListBuilder.create()
                .texOffs(138, 193).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(0F, 11.6F, -2.8F, 0.4887F, 0F, 0F));
        PartDefinition p_fire_talon_left_leg_2 = p_fire_feet_left_leg.addOrReplaceChild("fire_talon_left_leg_2", CubeListBuilder.create()
                .texOffs(145, 193).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(1.6F, 11.6F, -2.8F, 0.4887F, 0.2234F, 0F));
        PartDefinition p_fire_feet_right_leg = root.addOrReplaceChild("fire_feet_right_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_fire_talon_right_leg_0 = p_fire_feet_right_leg.addOrReplaceChild("fire_talon_right_leg_0", CubeListBuilder.create()
                .texOffs(152, 193).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(-1.6F, 11.6F, -2.8F, 0.4887F, -0.2234F, 0F));
        PartDefinition p_fire_talon_right_leg_1 = p_fire_feet_right_leg.addOrReplaceChild("fire_talon_right_leg_1", CubeListBuilder.create()
                .texOffs(159, 193).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(0F, 11.6F, -2.8F, 0.4887F, 0F, 0F));
        PartDefinition p_fire_talon_right_leg_2 = p_fire_feet_right_leg.addOrReplaceChild("fire_talon_right_leg_2", CubeListBuilder.create()
                .texOffs(166, 193).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(1.6F, 11.6F, -2.8F, 0.4887F, 0.2234F, 0F));
        PartDefinition p_ice_head_head = root.addOrReplaceChild("ice_head_head", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_ice_horn_left_0 = p_ice_head_head.addOrReplaceChild("ice_horn_left_0", CubeListBuilder.create()
                .texOffs(53, 148).addBox(-1F, -3.8F, -1F, 2F, 4F, 2F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offsetAndRotation(3F, -8.7F, -1F, -0.4887F, 0F, 0.2793F));
        PartDefinition p_ice_horn_left_1 = p_ice_horn_left_0.addOrReplaceChild("ice_horn_left_1", CubeListBuilder.create()
                .texOffs(62, 148).addBox(-1F, -3.7F, -1F, 2F, 4F, 2F, new CubeDeformation(-0.4F, -0.3F, -0.4F)),
                PartPose.offsetAndRotation(0F, -3.35F, 0F, -0.1047F, 0F, 0F));
        PartDefinition p_ice_side_left_0 = p_ice_head_head.addOrReplaceChild("ice_side_left_0", CubeListBuilder.create()
                .texOffs(207, 178).addBox(-1F, -1.66F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.34F, -0.45F)),
                PartPose.offsetAndRotation(4.4F, -7.4F, 0.6F, -0.6283F, 0F, 0.6981F));
        PartDefinition p_ice_side_left_1 = p_ice_side_left_0.addOrReplaceChild("ice_side_left_1", CubeListBuilder.create()
                .texOffs(190, 201).addBox(-0.5F, -1.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1975F, -0.4F, -0.1975F)),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_ice_horn_right_0 = p_ice_head_head.addOrReplaceChild("ice_horn_right_0", CubeListBuilder.create()
                .texOffs(71, 148).addBox(-1F, -3.8F, -1F, 2F, 4F, 2F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offsetAndRotation(-3F, -8.7F, -1F, -0.4887F, 0F, -0.2793F));
        PartDefinition p_ice_horn_right_1 = p_ice_horn_right_0.addOrReplaceChild("ice_horn_right_1", CubeListBuilder.create()
                .texOffs(80, 148).addBox(-1F, -3.7F, -1F, 2F, 4F, 2F, new CubeDeformation(-0.4F, -0.3F, -0.4F)),
                PartPose.offsetAndRotation(0F, -3.35F, 0F, -0.1047F, 0F, 0F));
        PartDefinition p_ice_side_right_0 = p_ice_head_head.addOrReplaceChild("ice_side_right_0", CubeListBuilder.create()
                .texOffs(216, 178).addBox(-1F, -1.66F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.34F, -0.45F)),
                PartPose.offsetAndRotation(-4.4F, -7.4F, 0.6F, -0.6283F, 0F, -0.6981F));
        PartDefinition p_ice_side_right_1 = p_ice_side_right_0.addOrReplaceChild("ice_side_right_1", CubeListBuilder.create()
                .texOffs(195, 201).addBox(-0.5F, -1.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1975F, -0.4F, -0.1975F)),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_ice_crest_0_0 = p_ice_head_head.addOrReplaceChild("ice_crest_0_0", CubeListBuilder.create()
                .texOffs(60, 209).addBox(-0.5F, -0.94F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.06F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, -3.2F, -0.4538F, 0F, 0F));
        PartDefinition p_ice_crest_0_1 = p_ice_crest_0_0.addOrReplaceChild("ice_crest_0_1", CubeListBuilder.create()
                .texOffs(65, 209).addBox(-0.5F, -0.9F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.1F, -0.225F)),
                PartPose.offset(0F, -0.63F, 0F));
        PartDefinition p_ice_crest_1_0 = p_ice_head_head.addOrReplaceChild("ice_crest_1_0", CubeListBuilder.create()
                .texOffs(200, 201).addBox(-0.5F, -1.605F, -0.5F, 1F, 2F, 1F, new CubeDeformation(0F, -0.395F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, -0.6F, -0.4538F, 0F, 0F));
        PartDefinition p_ice_crest_1_1 = p_ice_crest_1_0.addOrReplaceChild("ice_crest_1_1", CubeListBuilder.create()
                .texOffs(205, 201).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.225F, -0.45F, -0.225F)),
                PartPose.offset(0F, -0.96F, 0F));
        PartDefinition p_ice_crest_2_0 = p_ice_head_head.addOrReplaceChild("ice_crest_2_0", CubeListBuilder.create()
                .texOffs(70, 209).addBox(-0.5F, -0.995F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.005F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, 2F, -0.4538F, 0F, 0F));
        PartDefinition p_ice_crest_2_1 = p_ice_crest_2_0.addOrReplaceChild("ice_crest_2_1", CubeListBuilder.create()
                .texOffs(75, 209).addBox(-0.5F, -0.95F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.05F, -0.225F)),
                PartPose.offset(0F, -0.74F, 0F));
        PartDefinition p_ice_chest_body = root.addOrReplaceChild("ice_chest_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_ice_back_0_0 = p_ice_chest_body.addOrReplaceChild("ice_back_0_0", CubeListBuilder.create()
                .texOffs(225, 178).addBox(-1F, -1.935F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.065F, -0.3F)),
                PartPose.offsetAndRotation(0F, 0.8F, 2.9F, -0.8727F, 0F, 0F));
        PartDefinition p_ice_back_0_1 = p_ice_back_0_0.addOrReplaceChild("ice_back_0_1", CubeListBuilder.create()
                .texOffs(210, 201).addBox(-0.5F, -1.85F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.15F, -0.115F)),
                PartPose.offset(0F, -1.62F, 0F));
        PartDefinition p_ice_back_1_0 = p_ice_chest_body.addOrReplaceChild("ice_back_1_0", CubeListBuilder.create()
                .texOffs(90, 161).addBox(-1F, -2.545F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.3F, -0.455F, -0.3F)),
                PartPose.offsetAndRotation(0F, 3.6F, 2.9F, -0.8727F, 0F, 0F));
        PartDefinition p_ice_back_1_1 = p_ice_back_1_0.addOrReplaceChild("ice_back_1_1", CubeListBuilder.create()
                .texOffs(215, 201).addBox(-0.5F, -1.95F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.05F, -0.115F)),
                PartPose.offset(0F, -1.84F, 0F));
        PartDefinition p_ice_back_2_0 = p_ice_chest_body.addOrReplaceChild("ice_back_2_0", CubeListBuilder.create()
                .texOffs(234, 178).addBox(-1F, -1.88F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.12F, -0.3F)),
                PartPose.offsetAndRotation(0F, 6.6F, 2.9F, -0.8727F, 0F, 0F));
        PartDefinition p_ice_back_2_1 = p_ice_back_2_0.addOrReplaceChild("ice_back_2_1", CubeListBuilder.create()
                .texOffs(220, 201).addBox(-0.5F, -1.8F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.2F, -0.115F)),
                PartPose.offset(0F, -1.51F, 0F));
        PartDefinition p_ice_back_3_0 = p_ice_chest_body.addOrReplaceChild("ice_back_3_0", CubeListBuilder.create()
                .texOffs(243, 178).addBox(-1F, -1.66F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.34F, -0.3F)),
                PartPose.offsetAndRotation(0F, 9.4F, 2.9F, -0.8727F, 0F, 0F));
        PartDefinition p_ice_back_3_1 = p_ice_back_3_0.addOrReplaceChild("ice_back_3_1", CubeListBuilder.create()
                .texOffs(225, 201).addBox(-0.5F, -1.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.4F, -0.115F)),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_ice_chest_left_arm = root.addOrReplaceChild("ice_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_ice_pauldron_left_arm = p_ice_chest_left_arm.addOrReplaceChild("ice_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(186, 41).addBox(-3.3F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F))
                .texOffs(58, 74).addBox(-2F, -0.1F, -4F, 6F, 2F, 8F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.2094F));
        PartDefinition p_ice_claw_left_arm_0 = p_ice_pauldron_left_arm.addOrReplaceChild("ice_claw_left_arm_0", CubeListBuilder.create()
                .texOffs(0, 183).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.285F, -0.35F)),
                PartPose.offsetAndRotation(2.4F, -1.4F, 0F, 0F, 0F, 0.6981F));
        PartDefinition p_ice_claw_left_arm_1 = p_ice_claw_left_arm_0.addOrReplaceChild("ice_claw_left_arm_1", CubeListBuilder.create()
                .texOffs(230, 201).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.35F, -0.1425F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_ice_chest_right_arm = root.addOrReplaceChild("ice_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_ice_pauldron_right_arm = p_ice_chest_right_arm.addOrReplaceChild("ice_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(217, 41).addBox(-3.7F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F))
                .texOffs(87, 74).addBox(-4F, -0.1F, -4F, 6F, 2F, 8F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.2094F));
        PartDefinition p_ice_claw_right_arm_0 = p_ice_pauldron_right_arm.addOrReplaceChild("ice_claw_right_arm_0", CubeListBuilder.create()
                .texOffs(9, 183).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.285F, -0.35F)),
                PartPose.offsetAndRotation(-2.4F, -1.4F, 0F, 0F, 0F, -0.6981F));
        PartDefinition p_ice_claw_right_arm_1 = p_ice_claw_right_arm_0.addOrReplaceChild("ice_claw_right_arm_1", CubeListBuilder.create()
                .texOffs(235, 201).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.35F, -0.1425F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_ice_legs_body = root.addOrReplaceChild("ice_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_ice_tail_0 = p_ice_legs_body.addOrReplaceChild("ice_tail_0", CubeListBuilder.create()
                .texOffs(171, 134).addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F),
                PartPose.offsetAndRotation(0F, 10.2F, 2.2F, 0.5236F, 0F, 0F));
        PartDefinition p_ice_tail_1 = p_ice_tail_0.addOrReplaceChild("ice_tail_1", CubeListBuilder.create()
                .texOffs(184, 134).addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, 2.75F, 0F, 0.1745F, 0F, 0F));
        PartDefinition p_ice_tail_2 = p_ice_tail_1.addOrReplaceChild("ice_tail_2", CubeListBuilder.create()
                .texOffs(197, 134).addBox(-1.5F, -0.1F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.1F, -0.4F)),
                PartPose.offsetAndRotation(0F, 2.75F, 0F, 0.2094F, 0F, 0F));
        PartDefinition p_ice_tail_3 = p_ice_tail_2.addOrReplaceChild("ice_tail_3", CubeListBuilder.create()
                .texOffs(99, 161).addBox(-1F, -0.2F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.15F, -0.2F, -0.15F)),
                PartPose.offsetAndRotation(0F, 2.55F, 0F, 0.2094F, 0F, 0F));
        PartDefinition p_ice_tail_4 = p_ice_tail_3.addOrReplaceChild("ice_tail_4", CubeListBuilder.create()
                .texOffs(108, 161).addBox(-1F, -0.4F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.4F, -0.4F, -0.4F)),
                PartPose.offsetAndRotation(0F, 2.35F, 0F, 0.1745F, 0F, 0F));
        PartDefinition p_ice_tail_tip = p_ice_tail_4.addOrReplaceChild("ice_tail_tip", CubeListBuilder.create()
                .texOffs(117, 161).addBox(-1F, -0.3F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.1F, -0.3F, -0.1F))
                .texOffs(163, 188).addBox(-0.5F, 1.8F, -0.5F, 1F, 3F, 1F, new CubeDeformation(0F, -0.4F, 0F)),
                PartPose.offsetAndRotation(0F, 2.4F, 0F, 0.1396F, 0F, 0F));
        PartDefinition p_ice_legs_left_leg = root.addOrReplaceChild("ice_legs_left_leg", CubeListBuilder.create()
                .texOffs(11, 173).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_ice_legs_right_leg = root.addOrReplaceChild("ice_legs_right_leg", CubeListBuilder.create()
                .texOffs(22, 173).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_ice_feet_left_leg = root.addOrReplaceChild("ice_feet_left_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_ice_talon_left_leg_0 = p_ice_feet_left_leg.addOrReplaceChild("ice_talon_left_leg_0", CubeListBuilder.create()
                .texOffs(173, 193).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(-1.6F, 11.6F, -2.8F, 0.4887F, -0.2234F, 0F));
        PartDefinition p_ice_talon_left_leg_1 = p_ice_feet_left_leg.addOrReplaceChild("ice_talon_left_leg_1", CubeListBuilder.create()
                .texOffs(180, 193).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(0F, 11.6F, -2.8F, 0.4887F, 0F, 0F));
        PartDefinition p_ice_talon_left_leg_2 = p_ice_feet_left_leg.addOrReplaceChild("ice_talon_left_leg_2", CubeListBuilder.create()
                .texOffs(187, 193).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(1.6F, 11.6F, -2.8F, 0.4887F, 0.2234F, 0F));
        PartDefinition p_ice_feet_right_leg = root.addOrReplaceChild("ice_feet_right_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_ice_talon_right_leg_0 = p_ice_feet_right_leg.addOrReplaceChild("ice_talon_right_leg_0", CubeListBuilder.create()
                .texOffs(194, 193).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(-1.6F, 11.6F, -2.8F, 0.4887F, -0.2234F, 0F));
        PartDefinition p_ice_talon_right_leg_1 = p_ice_feet_right_leg.addOrReplaceChild("ice_talon_right_leg_1", CubeListBuilder.create()
                .texOffs(201, 193).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(0F, 11.6F, -2.8F, 0.4887F, 0F, 0F));
        PartDefinition p_ice_talon_right_leg_2 = p_ice_feet_right_leg.addOrReplaceChild("ice_talon_right_leg_2", CubeListBuilder.create()
                .texOffs(208, 193).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(1.6F, 11.6F, -2.8F, 0.4887F, 0.2234F, 0F));
        PartDefinition p_lightning_head_head = root.addOrReplaceChild("lightning_head_head", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_light_horn_left_0 = p_lightning_head_head.addOrReplaceChild("light_horn_left_0", CubeListBuilder.create()
                .texOffs(126, 161).addBox(-1F, -2.7F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.05F, -0.3F, -0.05F)),
                PartPose.offsetAndRotation(3.1F, -8.7F, -1.2F, -0.3491F, 0F, 0.4538F));
        PartDefinition p_light_horn_left_1 = p_light_horn_left_0.addOrReplaceChild("light_horn_left_1", CubeListBuilder.create()
                .texOffs(135, 161).addBox(-1F, -2.6F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.2F, -0.4F, -0.2F)),
                PartPose.offsetAndRotation(0F, -2.15F, 0F, -0.8727F, 0F, -0.3142F));
        PartDefinition p_light_horn_left_2 = p_light_horn_left_1.addOrReplaceChild("light_horn_left_2", CubeListBuilder.create()
                .texOffs(144, 161).addBox(-1F, -2.6F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.35F, -0.4F, -0.35F)),
                PartPose.offsetAndRotation(0F, -1.95F, 0F, 0.6981F, 0F, 0.1745F));
        PartDefinition p_light_horn_left_3 = p_light_horn_left_2.addOrReplaceChild("light_horn_left_3", CubeListBuilder.create()
                .texOffs(240, 201).addBox(-0.5F, -2F, -0.5F, 1F, 2F, 1F),
                PartPose.offsetAndRotation(0F, -1.95F, 0F, -0.9774F, 0F, -0.1047F));
        PartDefinition p_light_horn_left_4 = p_light_horn_left_3.addOrReplaceChild("light_horn_left_4", CubeListBuilder.create()
                .texOffs(245, 201).addBox(-0.5F, -1.9F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.15F, -0.1F, -0.15F)),
                PartPose.offsetAndRotation(0F, -1.75F, 0F, 0.5934F, 0F, 0F));
        PartDefinition p_light_horn_right_0 = p_lightning_head_head.addOrReplaceChild("light_horn_right_0", CubeListBuilder.create()
                .texOffs(153, 161).addBox(-1F, -2.7F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.05F, -0.3F, -0.05F)),
                PartPose.offsetAndRotation(-3.1F, -8.7F, -1.2F, -0.3491F, 0F, -0.4538F));
        PartDefinition p_light_horn_right_1 = p_light_horn_right_0.addOrReplaceChild("light_horn_right_1", CubeListBuilder.create()
                .texOffs(162, 161).addBox(-1F, -2.6F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.2F, -0.4F, -0.2F)),
                PartPose.offsetAndRotation(0F, -2.15F, 0F, -0.8727F, 0F, 0.3142F));
        PartDefinition p_light_horn_right_2 = p_light_horn_right_1.addOrReplaceChild("light_horn_right_2", CubeListBuilder.create()
                .texOffs(171, 161).addBox(-1F, -2.6F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.35F, -0.4F, -0.35F)),
                PartPose.offsetAndRotation(0F, -1.95F, 0F, 0.6981F, 0F, -0.1745F));
        PartDefinition p_light_horn_right_3 = p_light_horn_right_2.addOrReplaceChild("light_horn_right_3", CubeListBuilder.create()
                .texOffs(250, 201).addBox(-0.5F, -2F, -0.5F, 1F, 2F, 1F),
                PartPose.offsetAndRotation(0F, -1.95F, 0F, -0.9774F, 0F, 0.1047F));
        PartDefinition p_light_horn_right_4 = p_light_horn_right_3.addOrReplaceChild("light_horn_right_4", CubeListBuilder.create()
                .texOffs(0, 205).addBox(-0.5F, -1.9F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.15F, -0.1F, -0.15F)),
                PartPose.offsetAndRotation(0F, -1.75F, 0F, 0.5934F, 0F, 0F));
        PartDefinition p_lightning_crest_0_0 = p_lightning_head_head.addOrReplaceChild("lightning_crest_0_0", CubeListBuilder.create()
                .texOffs(80, 209).addBox(-0.5F, -0.94F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.06F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, -3.2F, -0.4538F, 0F, 0F));
        PartDefinition p_lightning_crest_0_1 = p_lightning_crest_0_0.addOrReplaceChild("lightning_crest_0_1", CubeListBuilder.create()
                .texOffs(85, 209).addBox(-0.5F, -0.9F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.1F, -0.225F)),
                PartPose.offset(0F, -0.63F, 0F));
        PartDefinition p_lightning_crest_1_0 = p_lightning_head_head.addOrReplaceChild("lightning_crest_1_0", CubeListBuilder.create()
                .texOffs(5, 205).addBox(-0.5F, -1.605F, -0.5F, 1F, 2F, 1F, new CubeDeformation(0F, -0.395F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, -0.6F, -0.4538F, 0F, 0F));
        PartDefinition p_lightning_crest_1_1 = p_lightning_crest_1_0.addOrReplaceChild("lightning_crest_1_1", CubeListBuilder.create()
                .texOffs(10, 205).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.225F, -0.45F, -0.225F)),
                PartPose.offset(0F, -0.96F, 0F));
        PartDefinition p_lightning_crest_2_0 = p_lightning_head_head.addOrReplaceChild("lightning_crest_2_0", CubeListBuilder.create()
                .texOffs(90, 209).addBox(-0.5F, -0.995F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.005F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, 2F, -0.4538F, 0F, 0F));
        PartDefinition p_lightning_crest_2_1 = p_lightning_crest_2_0.addOrReplaceChild("lightning_crest_2_1", CubeListBuilder.create()
                .texOffs(95, 209).addBox(-0.5F, -0.95F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.05F, -0.225F)),
                PartPose.offset(0F, -0.74F, 0F));
        PartDefinition p_lightning_chest_body = root.addOrReplaceChild("lightning_chest_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_lightning_back_0_0 = p_lightning_chest_body.addOrReplaceChild("lightning_back_0_0", CubeListBuilder.create()
                .texOffs(18, 183).addBox(-1F, -1.935F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.065F, -0.3F)),
                PartPose.offsetAndRotation(0F, 0.8F, 2.9F, -0.8727F, 0F, 0F));
        PartDefinition p_lightning_back_0_1 = p_lightning_back_0_0.addOrReplaceChild("lightning_back_0_1", CubeListBuilder.create()
                .texOffs(15, 205).addBox(-0.5F, -1.85F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.15F, -0.115F)),
                PartPose.offset(0F, -1.62F, 0F));
        PartDefinition p_lightning_back_1_0 = p_lightning_chest_body.addOrReplaceChild("lightning_back_1_0", CubeListBuilder.create()
                .texOffs(180, 161).addBox(-1F, -2.545F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.3F, -0.455F, -0.3F)),
                PartPose.offsetAndRotation(0F, 3.6F, 2.9F, -0.8727F, 0F, 0F));
        PartDefinition p_lightning_back_1_1 = p_lightning_back_1_0.addOrReplaceChild("lightning_back_1_1", CubeListBuilder.create()
                .texOffs(20, 205).addBox(-0.5F, -1.95F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.05F, -0.115F)),
                PartPose.offset(0F, -1.84F, 0F));
        PartDefinition p_lightning_back_2_0 = p_lightning_chest_body.addOrReplaceChild("lightning_back_2_0", CubeListBuilder.create()
                .texOffs(27, 183).addBox(-1F, -1.88F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.12F, -0.3F)),
                PartPose.offsetAndRotation(0F, 6.6F, 2.9F, -0.8727F, 0F, 0F));
        PartDefinition p_lightning_back_2_1 = p_lightning_back_2_0.addOrReplaceChild("lightning_back_2_1", CubeListBuilder.create()
                .texOffs(25, 205).addBox(-0.5F, -1.8F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.2F, -0.115F)),
                PartPose.offset(0F, -1.51F, 0F));
        PartDefinition p_lightning_back_3_0 = p_lightning_chest_body.addOrReplaceChild("lightning_back_3_0", CubeListBuilder.create()
                .texOffs(36, 183).addBox(-1F, -1.66F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.34F, -0.3F)),
                PartPose.offsetAndRotation(0F, 9.4F, 2.9F, -0.8727F, 0F, 0F));
        PartDefinition p_lightning_back_3_1 = p_lightning_back_3_0.addOrReplaceChild("lightning_back_3_1", CubeListBuilder.create()
                .texOffs(30, 205).addBox(-0.5F, -1.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.4F, -0.115F)),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_lightning_chest_left_arm = root.addOrReplaceChild("lightning_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_lightning_pauldron_left_arm = p_lightning_chest_left_arm.addOrReplaceChild("lightning_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(0, 52).addBox(-3.3F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F))
                .texOffs(116, 74).addBox(-2F, -0.1F, -4F, 6F, 2F, 8F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.2094F));
        PartDefinition p_lightning_claw_left_arm_0 = p_lightning_pauldron_left_arm.addOrReplaceChild("lightning_claw_left_arm_0", CubeListBuilder.create()
                .texOffs(45, 183).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.285F, -0.35F)),
                PartPose.offsetAndRotation(2.4F, -1.4F, 0F, 0F, 0F, 0.6981F));
        PartDefinition p_lightning_claw_left_arm_1 = p_lightning_claw_left_arm_0.addOrReplaceChild("lightning_claw_left_arm_1", CubeListBuilder.create()
                .texOffs(35, 205).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.35F, -0.1425F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_lightning_chest_right_arm = root.addOrReplaceChild("lightning_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_lightning_pauldron_right_arm = p_lightning_chest_right_arm.addOrReplaceChild("lightning_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(31, 52).addBox(-3.7F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F))
                .texOffs(145, 74).addBox(-4F, -0.1F, -4F, 6F, 2F, 8F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.2094F));
        PartDefinition p_lightning_claw_right_arm_0 = p_lightning_pauldron_right_arm.addOrReplaceChild("lightning_claw_right_arm_0", CubeListBuilder.create()
                .texOffs(54, 183).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.285F, -0.35F)),
                PartPose.offsetAndRotation(-2.4F, -1.4F, 0F, 0F, 0F, -0.6981F));
        PartDefinition p_lightning_claw_right_arm_1 = p_lightning_claw_right_arm_0.addOrReplaceChild("lightning_claw_right_arm_1", CubeListBuilder.create()
                .texOffs(40, 205).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.35F, -0.1425F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_lightning_legs_body = root.addOrReplaceChild("lightning_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_lightning_tail_0 = p_lightning_legs_body.addOrReplaceChild("lightning_tail_0", CubeListBuilder.create()
                .texOffs(210, 134).addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F),
                PartPose.offsetAndRotation(0F, 10.2F, 2.2F, 0.5236F, 0F, 0F));
        PartDefinition p_lightning_tail_1 = p_lightning_tail_0.addOrReplaceChild("lightning_tail_1", CubeListBuilder.create()
                .texOffs(223, 134).addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, 2.75F, 0F, 0.1745F, 0F, 0F));
        PartDefinition p_lightning_tail_2 = p_lightning_tail_1.addOrReplaceChild("lightning_tail_2", CubeListBuilder.create()
                .texOffs(236, 134).addBox(-1.5F, -0.1F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.1F, -0.4F)),
                PartPose.offsetAndRotation(0F, 2.75F, 0F, 0.2094F, 0F, 0F));
        PartDefinition p_lightning_tail_3 = p_lightning_tail_2.addOrReplaceChild("lightning_tail_3", CubeListBuilder.create()
                .texOffs(189, 161).addBox(-1F, -0.2F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.15F, -0.2F, -0.15F)),
                PartPose.offsetAndRotation(0F, 2.55F, 0F, 0.2094F, 0F, 0F));
        PartDefinition p_lightning_tail_4 = p_lightning_tail_3.addOrReplaceChild("lightning_tail_4", CubeListBuilder.create()
                .texOffs(198, 161).addBox(-1F, -0.4F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.4F, -0.4F, -0.4F)),
                PartPose.offsetAndRotation(0F, 2.35F, 0F, 0.1745F, 0F, 0F));
        PartDefinition p_lightning_tail_tip = p_lightning_tail_4.addOrReplaceChild("lightning_tail_tip", CubeListBuilder.create()
                .texOffs(35, 126).addBox(-0.5F, -0.4F, -2F, 1F, 3F, 4F, new CubeDeformation(-0.2F, -0.4F, -0.4F))
                .texOffs(207, 161).addBox(-0.5F, 2F, -0.8F, 1F, 2F, 3F, new CubeDeformation(-0.25F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, 2.4F, 0F, 0.1396F, 0F, 0F));
        PartDefinition p_lightning_legs_left_leg = root.addOrReplaceChild("lightning_legs_left_leg", CubeListBuilder.create()
                .texOffs(33, 173).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_lightning_legs_right_leg = root.addOrReplaceChild("lightning_legs_right_leg", CubeListBuilder.create()
                .texOffs(44, 173).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_lightning_feet_left_leg = root.addOrReplaceChild("lightning_feet_left_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_lightning_talon_left_leg_0 = p_lightning_feet_left_leg.addOrReplaceChild("lightning_talon_left_leg_0", CubeListBuilder.create()
                .texOffs(215, 193).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(-1.6F, 11.6F, -2.8F, 0.4887F, -0.2234F, 0F));
        PartDefinition p_lightning_talon_left_leg_1 = p_lightning_feet_left_leg.addOrReplaceChild("lightning_talon_left_leg_1", CubeListBuilder.create()
                .texOffs(222, 193).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(0F, 11.6F, -2.8F, 0.4887F, 0F, 0F));
        PartDefinition p_lightning_talon_left_leg_2 = p_lightning_feet_left_leg.addOrReplaceChild("lightning_talon_left_leg_2", CubeListBuilder.create()
                .texOffs(229, 193).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(1.6F, 11.6F, -2.8F, 0.4887F, 0.2234F, 0F));
        PartDefinition p_lightning_feet_right_leg = root.addOrReplaceChild("lightning_feet_right_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_lightning_talon_right_leg_0 = p_lightning_feet_right_leg.addOrReplaceChild("lightning_talon_right_leg_0", CubeListBuilder.create()
                .texOffs(236, 193).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(-1.6F, 11.6F, -2.8F, 0.4887F, -0.2234F, 0F));
        PartDefinition p_lightning_talon_right_leg_1 = p_lightning_feet_right_leg.addOrReplaceChild("lightning_talon_right_leg_1", CubeListBuilder.create()
                .texOffs(243, 193).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(0F, 11.6F, -2.8F, 0.4887F, 0F, 0F));
        PartDefinition p_lightning_talon_right_leg_2 = p_lightning_feet_right_leg.addOrReplaceChild("lightning_talon_right_leg_2", CubeListBuilder.create()
                .texOffs(250, 193).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(1.6F, 11.6F, -2.8F, 0.4887F, 0.2234F, 0F));
        PartDefinition p_nature_head_head = root.addOrReplaceChild("nature_head_head", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_antler_left_0 = p_nature_head_head.addOrReplaceChild("antler_left_0", CubeListBuilder.create()
                .texOffs(216, 161).addBox(-1F, -3F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.3F, 0F, -0.3F)),
                PartPose.offsetAndRotation(3F, -8.7F, -0.8F, -0.2443F, 0F, 0.5236F));
        PartDefinition p_antler_left_1 = p_antler_left_0.addOrReplaceChild("antler_left_1", CubeListBuilder.create()
                .texOffs(225, 161).addBox(-1F, -3F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.4F, 0F, -0.4F)),
                PartPose.offsetAndRotation(0F, -2.75F, 0F, -0.384F, 0F, -0.1745F));
        PartDefinition p_antler_left_2 = p_antler_left_1.addOrReplaceChild("antler_left_2", CubeListBuilder.create()
                .texOffs(168, 188).addBox(-0.5F, -2.8F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.05F, -0.2F, -0.05F)),
                PartPose.offsetAndRotation(0F, -2.75F, 0F, -0.3491F, 0F, -0.1396F));
        PartDefinition p_antler_tine_left_1 = p_antler_left_1.addOrReplaceChild("antler_tine_left_1", CubeListBuilder.create()
                .texOffs(45, 205).addBox(-0.5F, -1.9F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.15F, -0.1F, -0.15F)),
                PartPose.offsetAndRotation(0F, -2.2F, 0F, 0.8378F, 0F, -0.2443F));
        PartDefinition p_antler_tine_left_0 = p_antler_left_0.addOrReplaceChild("antler_tine_left_0", CubeListBuilder.create()
                .texOffs(50, 205).addBox(-0.5F, -2F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1F, 0F, -0.1F)),
                PartPose.offsetAndRotation(0F, -2.4F, 0F, 0.8727F, 0F, 0.1745F));
        PartDefinition p_antler_leaf_left = p_antler_left_0.addOrReplaceChild("antler_leaf_left", CubeListBuilder.create()
                .texOffs(126, 167).addBox(-0.5F, -2.8F, -1F, 1F, 3F, 2F, new CubeDeformation(-0.35F, -0.2F, -0.1F)),
                PartPose.offsetAndRotation(0.6F, -1.4F, 0F, 0F, 0F, 0.8727F));
        PartDefinition p_antler_right_0 = p_nature_head_head.addOrReplaceChild("antler_right_0", CubeListBuilder.create()
                .texOffs(234, 161).addBox(-1F, -3F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.3F, 0F, -0.3F)),
                PartPose.offsetAndRotation(-3F, -8.7F, -0.8F, -0.2443F, 0F, -0.5236F));
        PartDefinition p_antler_right_1 = p_antler_right_0.addOrReplaceChild("antler_right_1", CubeListBuilder.create()
                .texOffs(243, 161).addBox(-1F, -3F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.4F, 0F, -0.4F)),
                PartPose.offsetAndRotation(0F, -2.75F, 0F, -0.384F, 0F, 0.1745F));
        PartDefinition p_antler_right_2 = p_antler_right_1.addOrReplaceChild("antler_right_2", CubeListBuilder.create()
                .texOffs(173, 188).addBox(-0.5F, -2.8F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.05F, -0.2F, -0.05F)),
                PartPose.offsetAndRotation(0F, -2.75F, 0F, -0.3491F, 0F, 0.1396F));
        PartDefinition p_antler_tine_right_1 = p_antler_right_1.addOrReplaceChild("antler_tine_right_1", CubeListBuilder.create()
                .texOffs(55, 205).addBox(-0.5F, -1.9F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.15F, -0.1F, -0.15F)),
                PartPose.offsetAndRotation(0F, -2.2F, 0F, 0.8378F, 0F, 0.2443F));
        PartDefinition p_antler_tine_right_0 = p_antler_right_0.addOrReplaceChild("antler_tine_right_0", CubeListBuilder.create()
                .texOffs(60, 205).addBox(-0.5F, -2F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1F, 0F, -0.1F)),
                PartPose.offsetAndRotation(0F, -2.4F, 0F, 0.8727F, 0F, -0.1745F));
        PartDefinition p_antler_leaf_right = p_antler_right_0.addOrReplaceChild("antler_leaf_right", CubeListBuilder.create()
                .texOffs(133, 167).addBox(-0.5F, -2.8F, -1F, 1F, 3F, 2F, new CubeDeformation(-0.35F, -0.2F, -0.1F)),
                PartPose.offsetAndRotation(-0.6F, -1.4F, 0F, 0F, 0F, -0.8727F));
        PartDefinition p_nature_crest_0_0 = p_nature_head_head.addOrReplaceChild("nature_crest_0_0", CubeListBuilder.create()
                .texOffs(100, 209).addBox(-0.5F, -0.94F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.06F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, -3.2F, -0.4538F, 0F, 0F));
        PartDefinition p_nature_crest_0_1 = p_nature_crest_0_0.addOrReplaceChild("nature_crest_0_1", CubeListBuilder.create()
                .texOffs(105, 209).addBox(-0.5F, -0.9F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.1F, -0.225F)),
                PartPose.offset(0F, -0.63F, 0F));
        PartDefinition p_nature_crest_1_0 = p_nature_head_head.addOrReplaceChild("nature_crest_1_0", CubeListBuilder.create()
                .texOffs(65, 205).addBox(-0.5F, -1.605F, -0.5F, 1F, 2F, 1F, new CubeDeformation(0F, -0.395F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, -0.6F, -0.4538F, 0F, 0F));
        PartDefinition p_nature_crest_1_1 = p_nature_crest_1_0.addOrReplaceChild("nature_crest_1_1", CubeListBuilder.create()
                .texOffs(70, 205).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.225F, -0.45F, -0.225F)),
                PartPose.offset(0F, -0.96F, 0F));
        PartDefinition p_nature_crest_2_0 = p_nature_head_head.addOrReplaceChild("nature_crest_2_0", CubeListBuilder.create()
                .texOffs(110, 209).addBox(-0.5F, -0.995F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.005F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, 2F, -0.4538F, 0F, 0F));
        PartDefinition p_nature_crest_2_1 = p_nature_crest_2_0.addOrReplaceChild("nature_crest_2_1", CubeListBuilder.create()
                .texOffs(115, 209).addBox(-0.5F, -0.95F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.05F, -0.225F)),
                PartPose.offset(0F, -0.74F, 0F));
        PartDefinition p_nature_chest_body = root.addOrReplaceChild("nature_chest_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_nature_frond_0 = p_nature_chest_body.addOrReplaceChild("nature_frond_0", CubeListBuilder.create()
                .texOffs(89, 148).addBox(-1.5F, -4.6F, -0.5F, 3F, 5F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.35F)),
                PartPose.offsetAndRotation(0F, 1F, 2.9F, -0.8378F, 0F, 0F));
        PartDefinition p_nature_frond_1 = p_nature_chest_body.addOrReplaceChild("nature_frond_1", CubeListBuilder.create()
                .texOffs(0, 167).addBox(-1.5F, -3.8F, -0.5F, 3F, 4F, 1F, new CubeDeformation(-0.4F, -0.2F, -0.35F)),
                PartPose.offsetAndRotation(0F, 4F, 2.9F, -0.8378F, 0F, 0F));
        PartDefinition p_nature_frond_2 = p_nature_chest_body.addOrReplaceChild("nature_frond_2", CubeListBuilder.create()
                .texOffs(63, 183).addBox(-1.5F, -3F, -0.5F, 3F, 3F, 1F, new CubeDeformation(-0.4F, 0F, -0.35F)),
                PartPose.offsetAndRotation(0F, 7F, 2.9F, -0.8378F, 0F, 0F));
        PartDefinition p_nature_chest_left_arm = root.addOrReplaceChild("nature_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_nature_pauldron_left_arm = p_nature_chest_left_arm.addOrReplaceChild("nature_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(62, 52).addBox(-3.3F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F))
                .texOffs(174, 74).addBox(-2F, -0.1F, -4F, 6F, 2F, 8F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.2094F));
        PartDefinition p_nature_claw_left_arm_0 = p_nature_pauldron_left_arm.addOrReplaceChild("nature_claw_left_arm_0", CubeListBuilder.create()
                .texOffs(72, 183).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.285F, -0.35F)),
                PartPose.offsetAndRotation(2.4F, -1.4F, 0F, 0F, 0F, 0.6981F));
        PartDefinition p_nature_claw_left_arm_1 = p_nature_claw_left_arm_0.addOrReplaceChild("nature_claw_left_arm_1", CubeListBuilder.create()
                .texOffs(75, 205).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.35F, -0.1425F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_nature_chest_right_arm = root.addOrReplaceChild("nature_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_nature_pauldron_right_arm = p_nature_chest_right_arm.addOrReplaceChild("nature_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(93, 52).addBox(-3.7F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F))
                .texOffs(203, 74).addBox(-4F, -0.1F, -4F, 6F, 2F, 8F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.2094F));
        PartDefinition p_nature_claw_right_arm_0 = p_nature_pauldron_right_arm.addOrReplaceChild("nature_claw_right_arm_0", CubeListBuilder.create()
                .texOffs(81, 183).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.285F, -0.35F)),
                PartPose.offsetAndRotation(-2.4F, -1.4F, 0F, 0F, 0F, -0.6981F));
        PartDefinition p_nature_claw_right_arm_1 = p_nature_claw_right_arm_0.addOrReplaceChild("nature_claw_right_arm_1", CubeListBuilder.create()
                .texOffs(80, 205).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.35F, -0.1425F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_nature_legs_body = root.addOrReplaceChild("nature_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_nature_tail_0 = p_nature_legs_body.addOrReplaceChild("nature_tail_0", CubeListBuilder.create()
                .texOffs(0, 141).addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F),
                PartPose.offsetAndRotation(0F, 10.2F, 2.2F, 0.5236F, 0F, 0F));
        PartDefinition p_nature_tail_1 = p_nature_tail_0.addOrReplaceChild("nature_tail_1", CubeListBuilder.create()
                .texOffs(13, 141).addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, 2.75F, 0F, 0.1745F, 0F, 0F));
        PartDefinition p_nature_tail_2 = p_nature_tail_1.addOrReplaceChild("nature_tail_2", CubeListBuilder.create()
                .texOffs(26, 141).addBox(-1.5F, -0.1F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.1F, -0.4F)),
                PartPose.offsetAndRotation(0F, 2.75F, 0F, 0.2094F, 0F, 0F));
        PartDefinition p_nature_tail_3 = p_nature_tail_2.addOrReplaceChild("nature_tail_3", CubeListBuilder.create()
                .texOffs(9, 167).addBox(-1F, -0.2F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.15F, -0.2F, -0.15F)),
                PartPose.offsetAndRotation(0F, 2.55F, 0F, 0.2094F, 0F, 0F));
        PartDefinition p_nature_tail_4 = p_nature_tail_3.addOrReplaceChild("nature_tail_4", CubeListBuilder.create()
                .texOffs(18, 167).addBox(-1F, -0.4F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.4F, -0.4F, -0.4F)),
                PartPose.offsetAndRotation(0F, 2.35F, 0F, 0.1745F, 0F, 0F));
        PartDefinition p_nature_tail_tip = p_nature_tail_4.addOrReplaceChild("nature_tail_tip", CubeListBuilder.create()
                .texOffs(22, 117).addBox(-0.5F, -0.4F, -1.5F, 1F, 5F, 3F, new CubeDeformation(-0.35F, -0.4F, -0.1F)),
                PartPose.offsetAndRotation(0F, 2.4F, 0F, 0.1396F, 0F, 0F));
        PartDefinition p_nature_legs_left_leg = root.addOrReplaceChild("nature_legs_left_leg", CubeListBuilder.create()
                .texOffs(55, 173).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_nature_legs_right_leg = root.addOrReplaceChild("nature_legs_right_leg", CubeListBuilder.create()
                .texOffs(66, 173).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_nature_feet_left_leg = root.addOrReplaceChild("nature_feet_left_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_nature_talon_left_leg_0 = p_nature_feet_left_leg.addOrReplaceChild("nature_talon_left_leg_0", CubeListBuilder.create()
                .texOffs(0, 197).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(-1.6F, 11.6F, -2.8F, 0.4887F, -0.2234F, 0F));
        PartDefinition p_nature_talon_left_leg_1 = p_nature_feet_left_leg.addOrReplaceChild("nature_talon_left_leg_1", CubeListBuilder.create()
                .texOffs(7, 197).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(0F, 11.6F, -2.8F, 0.4887F, 0F, 0F));
        PartDefinition p_nature_talon_left_leg_2 = p_nature_feet_left_leg.addOrReplaceChild("nature_talon_left_leg_2", CubeListBuilder.create()
                .texOffs(14, 197).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(1.6F, 11.6F, -2.8F, 0.4887F, 0.2234F, 0F));
        PartDefinition p_nature_feet_right_leg = root.addOrReplaceChild("nature_feet_right_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_nature_talon_right_leg_0 = p_nature_feet_right_leg.addOrReplaceChild("nature_talon_right_leg_0", CubeListBuilder.create()
                .texOffs(21, 197).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(-1.6F, 11.6F, -2.8F, 0.4887F, -0.2234F, 0F));
        PartDefinition p_nature_talon_right_leg_1 = p_nature_feet_right_leg.addOrReplaceChild("nature_talon_right_leg_1", CubeListBuilder.create()
                .texOffs(28, 197).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(0F, 11.6F, -2.8F, 0.4887F, 0F, 0F));
        PartDefinition p_nature_talon_right_leg_2 = p_nature_feet_right_leg.addOrReplaceChild("nature_talon_right_leg_2", CubeListBuilder.create()
                .texOffs(35, 197).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(1.6F, 11.6F, -2.8F, 0.4887F, 0.2234F, 0F));
        PartDefinition p_earth_head_head = root.addOrReplaceChild("earth_head_head", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_ram_horn_left_0 = p_earth_head_head.addOrReplaceChild("ram_horn_left_0", CubeListBuilder.create()
                .texOffs(39, 141).addBox(-1.5F, -2.8F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.1F, -0.2F, -0.1F)),
                PartPose.offsetAndRotation(5F, -8F, -0.6F, -0.4189F, 0F, 0.4887F));
        PartDefinition p_ram_horn_left_1 = p_ram_horn_left_0.addOrReplaceChild("ram_horn_left_1", CubeListBuilder.create()
                .texOffs(52, 141).addBox(-1.5F, -2.8F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.25F, -0.2F, -0.25F)),
                PartPose.offsetAndRotation(0F, -2.35F, 0F, -0.9599F, 0F, 0F));
        PartDefinition p_ram_horn_left_2 = p_ram_horn_left_1.addOrReplaceChild("ram_horn_left_2", CubeListBuilder.create()
                .texOffs(65, 141).addBox(-1.5F, -2.8F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(0F, -2.35F, 0F, -0.9599F, 0F, 0F));
        PartDefinition p_ram_horn_left_3 = p_ram_horn_left_2.addOrReplaceChild("ram_horn_left_3", CubeListBuilder.create()
                .texOffs(27, 167).addBox(-1F, -2.7F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.05F, -0.3F, -0.05F)),
                PartPose.offsetAndRotation(0F, -2.35F, 0F, -0.9599F, 0F, 0F));
        PartDefinition p_ram_horn_left_4 = p_ram_horn_left_3.addOrReplaceChild("ram_horn_left_4", CubeListBuilder.create()
                .texOffs(36, 167).addBox(-1F, -2.6F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.25F, -0.4F, -0.25F)),
                PartPose.offsetAndRotation(0F, -2.15F, 0F, -0.9599F, 0F, 0F));
        PartDefinition p_ram_horn_left_5 = p_ram_horn_left_4.addOrReplaceChild("ram_horn_left_5", CubeListBuilder.create()
                .texOffs(85, 205).addBox(-0.5F, -1.9F, -0.5F, 1F, 2F, 1F, new CubeDeformation(0F, -0.1F, 0F)),
                PartPose.offsetAndRotation(0F, -1.95F, 0F, -0.7854F, 0F, 0F));
        PartDefinition p_ram_horn_right_0 = p_earth_head_head.addOrReplaceChild("ram_horn_right_0", CubeListBuilder.create()
                .texOffs(78, 141).addBox(-1.5F, -2.8F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.1F, -0.2F, -0.1F)),
                PartPose.offsetAndRotation(-5F, -8F, -0.6F, -0.4189F, 0F, -0.4887F));
        PartDefinition p_ram_horn_right_1 = p_ram_horn_right_0.addOrReplaceChild("ram_horn_right_1", CubeListBuilder.create()
                .texOffs(91, 141).addBox(-1.5F, -2.8F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.25F, -0.2F, -0.25F)),
                PartPose.offsetAndRotation(0F, -2.35F, 0F, -0.9599F, 0F, 0F));
        PartDefinition p_ram_horn_right_2 = p_ram_horn_right_1.addOrReplaceChild("ram_horn_right_2", CubeListBuilder.create()
                .texOffs(104, 141).addBox(-1.5F, -2.8F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(0F, -2.35F, 0F, -0.9599F, 0F, 0F));
        PartDefinition p_ram_horn_right_3 = p_ram_horn_right_2.addOrReplaceChild("ram_horn_right_3", CubeListBuilder.create()
                .texOffs(45, 167).addBox(-1F, -2.7F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.05F, -0.3F, -0.05F)),
                PartPose.offsetAndRotation(0F, -2.35F, 0F, -0.9599F, 0F, 0F));
        PartDefinition p_ram_horn_right_4 = p_ram_horn_right_3.addOrReplaceChild("ram_horn_right_4", CubeListBuilder.create()
                .texOffs(54, 167).addBox(-1F, -2.6F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.25F, -0.4F, -0.25F)),
                PartPose.offsetAndRotation(0F, -2.15F, 0F, -0.9599F, 0F, 0F));
        PartDefinition p_ram_horn_right_5 = p_ram_horn_right_4.addOrReplaceChild("ram_horn_right_5", CubeListBuilder.create()
                .texOffs(90, 205).addBox(-0.5F, -1.9F, -0.5F, 1F, 2F, 1F, new CubeDeformation(0F, -0.1F, 0F)),
                PartPose.offsetAndRotation(0F, -1.95F, 0F, -0.7854F, 0F, 0F));
        PartDefinition p_earth_crest_0_0 = p_earth_head_head.addOrReplaceChild("earth_crest_0_0", CubeListBuilder.create()
                .texOffs(120, 209).addBox(-0.5F, -0.94F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.06F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, -3.2F, -0.4538F, 0F, 0F));
        PartDefinition p_earth_crest_0_1 = p_earth_crest_0_0.addOrReplaceChild("earth_crest_0_1", CubeListBuilder.create()
                .texOffs(125, 209).addBox(-0.5F, -0.9F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.1F, -0.225F)),
                PartPose.offset(0F, -0.63F, 0F));
        PartDefinition p_earth_crest_1_0 = p_earth_head_head.addOrReplaceChild("earth_crest_1_0", CubeListBuilder.create()
                .texOffs(95, 205).addBox(-0.5F, -1.605F, -0.5F, 1F, 2F, 1F, new CubeDeformation(0F, -0.395F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, -0.6F, -0.4538F, 0F, 0F));
        PartDefinition p_earth_crest_1_1 = p_earth_crest_1_0.addOrReplaceChild("earth_crest_1_1", CubeListBuilder.create()
                .texOffs(100, 205).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.225F, -0.45F, -0.225F)),
                PartPose.offset(0F, -0.96F, 0F));
        PartDefinition p_earth_crest_2_0 = p_earth_head_head.addOrReplaceChild("earth_crest_2_0", CubeListBuilder.create()
                .texOffs(130, 209).addBox(-0.5F, -0.995F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.005F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, 2F, -0.4538F, 0F, 0F));
        PartDefinition p_earth_crest_2_1 = p_earth_crest_2_0.addOrReplaceChild("earth_crest_2_1", CubeListBuilder.create()
                .texOffs(135, 209).addBox(-0.5F, -0.95F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.05F, -0.225F)),
                PartPose.offset(0F, -0.74F, 0F));
        PartDefinition p_earth_chest_body = root.addOrReplaceChild("earth_chest_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_earth_plate_0 = p_earth_chest_body.addOrReplaceChild("earth_plate_0", CubeListBuilder.create()
                .texOffs(68, 126).addBox(-0.5F, -3.6F, -1.5F, 1F, 4F, 3F, new CubeDeformation(-0.1F, -0.4F, -0.3F))
                .texOffs(42, 197).addBox(-0.5F, -4.1F, -1F, 1F, 1F, 2F, new CubeDeformation(-0.2F, 0F, -0.4F)),
                PartPose.offsetAndRotation(0F, 0.8F, 2.9F, -0.6981F, 0F, 0F));
        PartDefinition p_earth_plate_1 = p_earth_chest_body.addOrReplaceChild("earth_plate_1", CubeListBuilder.create()
                .texOffs(77, 126).addBox(-0.5F, -3.8F, -1.5F, 1F, 4F, 3F, new CubeDeformation(-0.1F, -0.2F, -0.3F))
                .texOffs(49, 197).addBox(-0.5F, -4.5F, -1F, 1F, 1F, 2F, new CubeDeformation(-0.2F, 0F, -0.4F)),
                PartPose.offsetAndRotation(0F, 4F, 2.9F, -0.6981F, 0F, 0F));
        PartDefinition p_earth_plate_2 = p_earth_chest_body.addOrReplaceChild("earth_plate_2", CubeListBuilder.create()
                .texOffs(98, 148).addBox(-0.5F, -2.9F, -1.5F, 1F, 3F, 3F, new CubeDeformation(-0.1F, -0.1F, -0.3F))
                .texOffs(56, 197).addBox(-0.5F, -3.7F, -1F, 1F, 1F, 2F, new CubeDeformation(-0.2F, 0F, -0.4F)),
                PartPose.offsetAndRotation(0F, 7.4F, 2.9F, -0.6981F, 0F, 0F));
        PartDefinition p_earth_chest_left_arm = root.addOrReplaceChild("earth_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_earth_pauldron_left_arm = p_earth_chest_left_arm.addOrReplaceChild("earth_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(124, 52).addBox(-3.3F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F))
                .texOffs(0, 85).addBox(-2F, -0.1F, -4F, 6F, 2F, 8F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.2094F));
        PartDefinition p_earth_claw_left_arm_0 = p_earth_pauldron_left_arm.addOrReplaceChild("earth_claw_left_arm_0", CubeListBuilder.create()
                .texOffs(90, 183).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.285F, -0.35F)),
                PartPose.offsetAndRotation(2.4F, -1.4F, 0F, 0F, 0F, 0.6981F));
        PartDefinition p_earth_claw_left_arm_1 = p_earth_claw_left_arm_0.addOrReplaceChild("earth_claw_left_arm_1", CubeListBuilder.create()
                .texOffs(105, 205).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.35F, -0.1425F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_earth_chest_right_arm = root.addOrReplaceChild("earth_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_earth_pauldron_right_arm = p_earth_chest_right_arm.addOrReplaceChild("earth_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(155, 52).addBox(-3.7F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F))
                .texOffs(29, 85).addBox(-4F, -0.1F, -4F, 6F, 2F, 8F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.2094F));
        PartDefinition p_earth_claw_right_arm_0 = p_earth_pauldron_right_arm.addOrReplaceChild("earth_claw_right_arm_0", CubeListBuilder.create()
                .texOffs(99, 183).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.285F, -0.35F)),
                PartPose.offsetAndRotation(-2.4F, -1.4F, 0F, 0F, 0F, -0.6981F));
        PartDefinition p_earth_claw_right_arm_1 = p_earth_claw_right_arm_0.addOrReplaceChild("earth_claw_right_arm_1", CubeListBuilder.create()
                .texOffs(110, 205).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.35F, -0.1425F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_earth_legs_body = root.addOrReplaceChild("earth_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_earth_tail_0 = p_earth_legs_body.addOrReplaceChild("earth_tail_0", CubeListBuilder.create()
                .texOffs(117, 141).addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F),
                PartPose.offsetAndRotation(0F, 10.2F, 2.2F, 0.5236F, 0F, 0F));
        PartDefinition p_earth_tail_1 = p_earth_tail_0.addOrReplaceChild("earth_tail_1", CubeListBuilder.create()
                .texOffs(130, 141).addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, 2.75F, 0F, 0.1745F, 0F, 0F));
        PartDefinition p_earth_tail_2 = p_earth_tail_1.addOrReplaceChild("earth_tail_2", CubeListBuilder.create()
                .texOffs(143, 141).addBox(-1.5F, -0.1F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.1F, -0.4F)),
                PartPose.offsetAndRotation(0F, 2.75F, 0F, 0.2094F, 0F, 0F));
        PartDefinition p_earth_tail_3 = p_earth_tail_2.addOrReplaceChild("earth_tail_3", CubeListBuilder.create()
                .texOffs(63, 167).addBox(-1F, -0.2F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.15F, -0.2F, -0.15F)),
                PartPose.offsetAndRotation(0F, 2.55F, 0F, 0.2094F, 0F, 0F));
        PartDefinition p_earth_tail_4 = p_earth_tail_3.addOrReplaceChild("earth_tail_4", CubeListBuilder.create()
                .texOffs(72, 167).addBox(-1F, -0.4F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.4F, -0.4F, -0.4F)),
                PartPose.offsetAndRotation(0F, 2.35F, 0F, 0.1745F, 0F, 0F));
        PartDefinition p_earth_tail_tip = p_earth_tail_4.addOrReplaceChild("earth_tail_tip", CubeListBuilder.create()
                .texOffs(102, 117).addBox(-2F, -0.1F, -2F, 4F, 3F, 4F, new CubeDeformation(-0.4F, -0.1F, -0.4F))
                .texOffs(63, 197).addBox(-1F, 0.2F, -2.15F, 2F, 2F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.25F)),
                PartPose.offsetAndRotation(0F, 2.4F, 0F, 0.1396F, 0F, 0F));
        PartDefinition p_earth_legs_left_leg = root.addOrReplaceChild("earth_legs_left_leg", CubeListBuilder.create()
                .texOffs(77, 173).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_earth_legs_right_leg = root.addOrReplaceChild("earth_legs_right_leg", CubeListBuilder.create()
                .texOffs(88, 173).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_earth_feet_left_leg = root.addOrReplaceChild("earth_feet_left_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_earth_talon_left_leg_0 = p_earth_feet_left_leg.addOrReplaceChild("earth_talon_left_leg_0", CubeListBuilder.create()
                .texOffs(70, 197).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(-1.6F, 11.6F, -2.8F, 0.4887F, -0.2234F, 0F));
        PartDefinition p_earth_talon_left_leg_1 = p_earth_feet_left_leg.addOrReplaceChild("earth_talon_left_leg_1", CubeListBuilder.create()
                .texOffs(77, 197).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(0F, 11.6F, -2.8F, 0.4887F, 0F, 0F));
        PartDefinition p_earth_talon_left_leg_2 = p_earth_feet_left_leg.addOrReplaceChild("earth_talon_left_leg_2", CubeListBuilder.create()
                .texOffs(84, 197).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(1.6F, 11.6F, -2.8F, 0.4887F, 0.2234F, 0F));
        PartDefinition p_earth_feet_right_leg = root.addOrReplaceChild("earth_feet_right_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_earth_talon_right_leg_0 = p_earth_feet_right_leg.addOrReplaceChild("earth_talon_right_leg_0", CubeListBuilder.create()
                .texOffs(91, 197).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(-1.6F, 11.6F, -2.8F, 0.4887F, -0.2234F, 0F));
        PartDefinition p_earth_talon_right_leg_1 = p_earth_feet_right_leg.addOrReplaceChild("earth_talon_right_leg_1", CubeListBuilder.create()
                .texOffs(98, 197).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(0F, 11.6F, -2.8F, 0.4887F, 0F, 0F));
        PartDefinition p_earth_talon_right_leg_2 = p_earth_feet_right_leg.addOrReplaceChild("earth_talon_right_leg_2", CubeListBuilder.create()
                .texOffs(105, 197).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(1.6F, 11.6F, -2.8F, 0.4887F, 0.2234F, 0F));
        PartDefinition p_water_head_head = root.addOrReplaceChild("water_head_head", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_head_fin_left = p_water_head_head.addOrReplaceChild("head_fin_left", CubeListBuilder.create(),
                PartPose.offsetAndRotation(5.3F, -4.6F, -1F, 0F, 0.2443F, 0F));
        PartDefinition p_head_fin_left_ray_0 = p_head_fin_left.addOrReplaceChild("head_fin_left_ray_0", CubeListBuilder.create()
                .texOffs(210, 117).addBox(-0.5F, -1F, -0.2F, 1F, 2F, 5F, new CubeDeformation(-0.25F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.1047F, 0F, 0F));
        PartDefinition p_head_fin_left_ray_1 = p_head_fin_left.addOrReplaceChild("head_fin_left_ray_1", CubeListBuilder.create()
                .texOffs(214, 107).addBox(-0.5F, -1F, -0.3F, 1F, 2F, 6F, new CubeDeformation(-0.25F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0.2443F, 0F, 0F));
        PartDefinition p_head_fin_left_ray_2 = p_head_fin_left.addOrReplaceChild("head_fin_left_ray_2", CubeListBuilder.create()
                .texOffs(223, 117).addBox(-0.5F, -1F, 0F, 1F, 2F, 5F, new CubeDeformation(-0.25F, -0.25F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0.5934F, 0F, 0F));
        PartDefinition p_head_fin_left_ray_3 = p_head_fin_left.addOrReplaceChild("head_fin_left_ray_3", CubeListBuilder.create()
                .texOffs(217, 141).addBox(-0.5F, -1F, -0.1F, 1F, 2F, 4F, new CubeDeformation(-0.25F, -0.35F, -0.1F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0.9425F, 0F, 0F));
        PartDefinition p_head_fin_right = p_water_head_head.addOrReplaceChild("head_fin_right", CubeListBuilder.create(),
                PartPose.offsetAndRotation(-5.3F, -4.6F, -1F, 0F, -0.2443F, 0F));
        PartDefinition p_head_fin_right_ray_0 = p_head_fin_right.addOrReplaceChild("head_fin_right_ray_0", CubeListBuilder.create()
                .texOffs(236, 117).addBox(-0.5F, -1F, -0.2F, 1F, 2F, 5F, new CubeDeformation(-0.25F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.1047F, 0F, 0F));
        PartDefinition p_head_fin_right_ray_1 = p_head_fin_right.addOrReplaceChild("head_fin_right_ray_1", CubeListBuilder.create()
                .texOffs(229, 107).addBox(-0.5F, -1F, -0.3F, 1F, 2F, 6F, new CubeDeformation(-0.25F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0.2443F, 0F, 0F));
        PartDefinition p_head_fin_right_ray_2 = p_head_fin_right.addOrReplaceChild("head_fin_right_ray_2", CubeListBuilder.create()
                .texOffs(0, 126).addBox(-0.5F, -1F, 0F, 1F, 2F, 5F, new CubeDeformation(-0.25F, -0.25F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0.5934F, 0F, 0F));
        PartDefinition p_head_fin_right_ray_3 = p_head_fin_right.addOrReplaceChild("head_fin_right_ray_3", CubeListBuilder.create()
                .texOffs(228, 141).addBox(-0.5F, -1F, -0.1F, 1F, 2F, 4F, new CubeDeformation(-0.25F, -0.35F, -0.1F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0.9425F, 0F, 0F));
        PartDefinition p_water_crest_0_0 = p_water_head_head.addOrReplaceChild("water_crest_0_0", CubeListBuilder.create()
                .texOffs(140, 209).addBox(-0.5F, -0.94F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.06F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, -3.2F, -0.4538F, 0F, 0F));
        PartDefinition p_water_crest_0_1 = p_water_crest_0_0.addOrReplaceChild("water_crest_0_1", CubeListBuilder.create()
                .texOffs(145, 209).addBox(-0.5F, -0.9F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.1F, -0.225F)),
                PartPose.offset(0F, -0.63F, 0F));
        PartDefinition p_water_crest_1_0 = p_water_head_head.addOrReplaceChild("water_crest_1_0", CubeListBuilder.create()
                .texOffs(115, 205).addBox(-0.5F, -1.605F, -0.5F, 1F, 2F, 1F, new CubeDeformation(0F, -0.395F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, -0.6F, -0.4538F, 0F, 0F));
        PartDefinition p_water_crest_1_1 = p_water_crest_1_0.addOrReplaceChild("water_crest_1_1", CubeListBuilder.create()
                .texOffs(120, 205).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.225F, -0.45F, -0.225F)),
                PartPose.offset(0F, -0.96F, 0F));
        PartDefinition p_water_crest_2_0 = p_water_head_head.addOrReplaceChild("water_crest_2_0", CubeListBuilder.create()
                .texOffs(150, 209).addBox(-0.5F, -0.995F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.005F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, 2F, -0.4538F, 0F, 0F));
        PartDefinition p_water_crest_2_1 = p_water_crest_2_0.addOrReplaceChild("water_crest_2_1", CubeListBuilder.create()
                .texOffs(155, 209).addBox(-0.5F, -0.95F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.05F, -0.225F)),
                PartPose.offset(0F, -0.74F, 0F));
        PartDefinition p_water_chest_body = root.addOrReplaceChild("water_chest_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_water_back_fin_0 = p_water_chest_body.addOrReplaceChild("water_back_fin_0", CubeListBuilder.create(),
                PartPose.offset(0F, 0.6F, 2.9F));
        PartDefinition p_water_back_fin_0_ray_0 = p_water_back_fin_0.addOrReplaceChild("water_back_fin_0_ray_0", CubeListBuilder.create()
                .texOffs(244, 107).addBox(-0.5F, -1.5F, -0.3F, 1F, 3F, 5F, new CubeDeformation(-0.25F, -0.4F, -0.3F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.6632F, 0F, 0F));
        PartDefinition p_water_back_fin_0_ray_1 = p_water_back_fin_0.addOrReplaceChild("water_back_fin_0_ray_1", CubeListBuilder.create()
                .texOffs(239, 141).addBox(-0.5F, -1F, -0.24F, 1F, 2F, 4F, new CubeDeformation(-0.25F, 0F, -0.24F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.1396F, 0F, 0F));
        PartDefinition p_water_back_fin_1 = p_water_chest_body.addOrReplaceChild("water_back_fin_1", CubeListBuilder.create(),
                PartPose.offset(0F, 3.4F, 2.9F));
        PartDefinition p_water_back_fin_1_ray_0 = p_water_back_fin_1.addOrReplaceChild("water_back_fin_1_ray_0", CubeListBuilder.create()
                .texOffs(46, 126).addBox(-0.5F, -1.5F, 0F, 1F, 3F, 4F, new CubeDeformation(-0.25F, -0.4F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.6632F, 0F, 0F));
        PartDefinition p_water_back_fin_1_ray_1 = p_water_back_fin_1.addOrReplaceChild("water_back_fin_1_ray_1", CubeListBuilder.create()
                .texOffs(0, 148).addBox(-0.5F, -1F, -0.4F, 1F, 2F, 4F, new CubeDeformation(-0.25F, 0F, -0.4F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.1396F, 0F, 0F));
        PartDefinition p_water_back_fin_2 = p_water_chest_body.addOrReplaceChild("water_back_fin_2", CubeListBuilder.create(),
                PartPose.offset(0F, 6.2F, 2.9F));
        PartDefinition p_water_back_fin_2_ray_0 = p_water_back_fin_2.addOrReplaceChild("water_back_fin_2_ray_0", CubeListBuilder.create()
                .texOffs(57, 126).addBox(-0.5F, -1.5F, -0.3F, 1F, 3F, 4F, new CubeDeformation(-0.25F, -0.4F, -0.3F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.6632F, 0F, 0F));
        PartDefinition p_water_back_fin_2_ray_1 = p_water_back_fin_2.addOrReplaceChild("water_back_fin_2_ray_1", CubeListBuilder.create()
                .texOffs(81, 167).addBox(-0.5F, -1F, -0.14F, 1F, 2F, 3F, new CubeDeformation(-0.25F, 0F, -0.14F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.1396F, 0F, 0F));
        PartDefinition p_water_back_fin_3 = p_water_chest_body.addOrReplaceChild("water_back_fin_3", CubeListBuilder.create(),
                PartPose.offset(0F, 9F, 2.9F));
        PartDefinition p_water_back_fin_3_ray_0 = p_water_back_fin_3.addOrReplaceChild("water_back_fin_3_ray_0", CubeListBuilder.create()
                .texOffs(107, 148).addBox(-0.5F, -1.5F, -0.2F, 1F, 3F, 3F, new CubeDeformation(-0.25F, -0.4F, -0.2F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.6632F, 0F, 0F));
        PartDefinition p_water_back_fin_3_ray_1 = p_water_back_fin_3.addOrReplaceChild("water_back_fin_3_ray_1", CubeListBuilder.create()
                .texOffs(90, 167).addBox(-0.5F, -1F, -0.46F, 1F, 2F, 3F, new CubeDeformation(-0.25F, 0F, -0.46F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.1396F, 0F, 0F));
        PartDefinition p_water_chest_left_arm = root.addOrReplaceChild("water_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_water_pauldron_left_arm = p_water_chest_left_arm.addOrReplaceChild("water_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(186, 52).addBox(-3.3F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F))
                .texOffs(58, 85).addBox(-2F, -0.1F, -4F, 6F, 2F, 8F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.2094F));
        PartDefinition p_water_claw_left_arm_0 = p_water_pauldron_left_arm.addOrReplaceChild("water_claw_left_arm_0", CubeListBuilder.create()
                .texOffs(108, 183).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.285F, -0.35F)),
                PartPose.offsetAndRotation(2.4F, -1.4F, 0F, 0F, 0F, 0.6981F));
        PartDefinition p_water_claw_left_arm_1 = p_water_claw_left_arm_0.addOrReplaceChild("water_claw_left_arm_1", CubeListBuilder.create()
                .texOffs(125, 205).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.35F, -0.1425F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_water_chest_right_arm = root.addOrReplaceChild("water_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_water_pauldron_right_arm = p_water_chest_right_arm.addOrReplaceChild("water_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(217, 52).addBox(-3.7F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F))
                .texOffs(87, 85).addBox(-4F, -0.1F, -4F, 6F, 2F, 8F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.2094F));
        PartDefinition p_water_claw_right_arm_0 = p_water_pauldron_right_arm.addOrReplaceChild("water_claw_right_arm_0", CubeListBuilder.create()
                .texOffs(117, 183).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.285F, -0.35F)),
                PartPose.offsetAndRotation(-2.4F, -1.4F, 0F, 0F, 0F, -0.6981F));
        PartDefinition p_water_claw_right_arm_1 = p_water_claw_right_arm_0.addOrReplaceChild("water_claw_right_arm_1", CubeListBuilder.create()
                .texOffs(130, 205).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.35F, -0.1425F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_water_legs_body = root.addOrReplaceChild("water_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_water_tail_0 = p_water_legs_body.addOrReplaceChild("water_tail_0", CubeListBuilder.create()
                .texOffs(156, 141).addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F),
                PartPose.offsetAndRotation(0F, 10.2F, 2.2F, 0.5236F, 0F, 0F));
        PartDefinition p_water_tail_1 = p_water_tail_0.addOrReplaceChild("water_tail_1", CubeListBuilder.create()
                .texOffs(169, 141).addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, 2.75F, 0F, 0.1745F, 0F, 0F));
        PartDefinition p_water_tail_2 = p_water_tail_1.addOrReplaceChild("water_tail_2", CubeListBuilder.create()
                .texOffs(182, 141).addBox(-1.5F, -0.1F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.1F, -0.4F)),
                PartPose.offsetAndRotation(0F, 2.75F, 0F, 0.2094F, 0F, 0F));
        PartDefinition p_water_tail_3 = p_water_tail_2.addOrReplaceChild("water_tail_3", CubeListBuilder.create()
                .texOffs(99, 167).addBox(-1F, -0.2F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.15F, -0.2F, -0.15F)),
                PartPose.offsetAndRotation(0F, 2.55F, 0F, 0.2094F, 0F, 0F));
        PartDefinition p_water_tail_4 = p_water_tail_3.addOrReplaceChild("water_tail_4", CubeListBuilder.create()
                .texOffs(108, 167).addBox(-1F, -0.4F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.4F, -0.4F, -0.4F)),
                PartPose.offsetAndRotation(0F, 2.35F, 0F, 0.1745F, 0F, 0F));
        PartDefinition p_water_tail_tip = p_water_tail_4.addOrReplaceChild("water_tail_tip", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0F, 2.4F, 0F, 0.1396F, 0F, 0F));
        PartDefinition p_water_tail_fin = p_water_tail_tip.addOrReplaceChild("water_tail_fin", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0F, 0.6F, 0F, 1.5708F, 0F, 0F));
        PartDefinition p_water_tail_fin_ray_0 = p_water_tail_fin.addOrReplaceChild("water_tail_fin_ray_0", CubeListBuilder.create()
                .texOffs(11, 148).addBox(-0.5F, -1F, -0.2F, 1F, 2F, 4F, new CubeDeformation(-0.25F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.6981F, 0F, 0F));
        PartDefinition p_water_tail_fin_ray_1 = p_water_tail_fin.addOrReplaceChild("water_tail_fin_ray_1", CubeListBuilder.create()
                .texOffs(22, 148).addBox(-0.5F, -1F, 0F, 1F, 2F, 4F, new CubeDeformation(-0.25F, -0.2F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.1745F, 0F, 0F));
        PartDefinition p_water_tail_fin_ray_2 = p_water_tail_fin.addOrReplaceChild("water_tail_fin_ray_2", CubeListBuilder.create()
                .texOffs(33, 148).addBox(-0.5F, -1F, -0.2F, 1F, 2F, 4F, new CubeDeformation(-0.25F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0.3491F, 0F, 0F));
        PartDefinition p_water_legs_left_leg = root.addOrReplaceChild("water_legs_left_leg", CubeListBuilder.create()
                .texOffs(99, 173).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_water_legs_right_leg = root.addOrReplaceChild("water_legs_right_leg", CubeListBuilder.create()
                .texOffs(110, 173).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_water_feet_left_leg = root.addOrReplaceChild("water_feet_left_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_water_talon_left_leg_0 = p_water_feet_left_leg.addOrReplaceChild("water_talon_left_leg_0", CubeListBuilder.create()
                .texOffs(112, 197).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(-1.6F, 11.6F, -2.8F, 0.4887F, -0.2234F, 0F));
        PartDefinition p_water_talon_left_leg_1 = p_water_feet_left_leg.addOrReplaceChild("water_talon_left_leg_1", CubeListBuilder.create()
                .texOffs(119, 197).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(0F, 11.6F, -2.8F, 0.4887F, 0F, 0F));
        PartDefinition p_water_talon_left_leg_2 = p_water_feet_left_leg.addOrReplaceChild("water_talon_left_leg_2", CubeListBuilder.create()
                .texOffs(126, 197).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(1.6F, 11.6F, -2.8F, 0.4887F, 0.2234F, 0F));
        PartDefinition p_water_feet_right_leg = root.addOrReplaceChild("water_feet_right_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_water_talon_right_leg_0 = p_water_feet_right_leg.addOrReplaceChild("water_talon_right_leg_0", CubeListBuilder.create()
                .texOffs(133, 197).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(-1.6F, 11.6F, -2.8F, 0.4887F, -0.2234F, 0F));
        PartDefinition p_water_talon_right_leg_1 = p_water_feet_right_leg.addOrReplaceChild("water_talon_right_leg_1", CubeListBuilder.create()
                .texOffs(140, 197).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(0F, 11.6F, -2.8F, 0.4887F, 0F, 0F));
        PartDefinition p_water_talon_right_leg_2 = p_water_feet_right_leg.addOrReplaceChild("water_talon_right_leg_2", CubeListBuilder.create()
                .texOffs(147, 197).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(1.6F, 11.6F, -2.8F, 0.4887F, 0.2234F, 0F));
        PartDefinition p_crown_head_head = root.addOrReplaceChild("crown_head_head", CubeListBuilder.create()
                .texOffs(210, 188).addBox(-5.5F, -10F, -5.5F, 11F, 2F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.1F))
                .texOffs(0, 193).addBox(-5.5F, -10F, 4.5F, 11F, 2F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.1F))
                .texOffs(118, 0).addBox(-5.5F, -10F, -5F, 1F, 2F, 10F, new CubeDeformation(-0.1F, -0.1F, -0.4F))
                .texOffs(141, 0).addBox(4.5F, -10F, -5F, 1F, 2F, 10F, new CubeDeformation(-0.1F, -0.1F, -0.4F))
                .texOffs(154, 197).addBox(-1F, -10F, -6.1F, 2F, 2F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.2F))
                .texOffs(160, 209).addBox(-4F, -9.5F, -6.05F, 1F, 1F, 1F, new CubeDeformation(0F, -0.1F, -0.25F))
                .texOffs(165, 209).addBox(3F, -9.5F, -6.05F, 1F, 1F, 1F, new CubeDeformation(0F, -0.1F, -0.25F))
                .texOffs(161, 197).addBox(-6.1F, -9.5F, -1F, 1F, 1F, 2F, new CubeDeformation(-0.2F, 0F, -0.4F))
                .texOffs(168, 197).addBox(5.1F, -9.5F, -1F, 1F, 1F, 2F, new CubeDeformation(-0.2F, 0F, -0.4F))
                .texOffs(23, 209).addBox(-1F, -9.5F, 5.1F, 2F, 1F, 1F, new CubeDeformation(-0.4F, 0F, -0.2F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_crown_point_0_0 = p_crown_head_head.addOrReplaceChild("crown_point_0_0", CubeListBuilder.create()
                .texOffs(175, 197).addBox(-1F, -1.96F, -0.5F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.04F, -0.1F)),
                PartPose.offset(0F, -9.7F, -5.4F));
        PartDefinition p_crown_point_0_1 = p_crown_point_0_0.addOrReplaceChild("crown_point_0_1", CubeListBuilder.create()
                .texOffs(135, 205).addBox(-0.5F, -1.8F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.15F, -0.2F, -0.25F))
                .texOffs(170, 209).addBox(-0.5F, -2.4F, -0.5F, 1F, 1F, 1F),
                PartPose.offset(0F, -1.67F, 0F));
        PartDefinition p_crown_point_1_0 = p_crown_head_head.addOrReplaceChild("crown_point_1_0", CubeListBuilder.create()
                .texOffs(182, 197).addBox(-1F, -1.66F, -0.5F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.34F, -0.1F)),
                PartPose.offset(-3.2F, -9.7F, -5.4F));
        PartDefinition p_crown_point_1_1 = p_crown_point_1_0.addOrReplaceChild("crown_point_1_1", CubeListBuilder.create()
                .texOffs(140, 205).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.15F, -0.45F, -0.25F))
                .texOffs(175, 209).addBox(-0.5F, -1.9F, -0.5F, 1F, 1F, 1F),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_crown_point_2_0 = p_crown_head_head.addOrReplaceChild("crown_point_2_0", CubeListBuilder.create()
                .texOffs(189, 197).addBox(-1F, -1.66F, -0.5F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.34F, -0.1F)),
                PartPose.offset(3.2F, -9.7F, -5.4F));
        PartDefinition p_crown_point_2_1 = p_crown_point_2_0.addOrReplaceChild("crown_point_2_1", CubeListBuilder.create()
                .texOffs(145, 205).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.15F, -0.45F, -0.25F))
                .texOffs(180, 209).addBox(-0.5F, -1.9F, -0.5F, 1F, 1F, 1F),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_crown_point_3_0 = p_crown_head_head.addOrReplaceChild("crown_point_3_0", CubeListBuilder.create()
                .texOffs(196, 197).addBox(-1F, -1.72F, -0.5F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.28F, -0.1F)),
                PartPose.offset(0F, -9.7F, 5.4F));
        PartDefinition p_crown_point_3_1 = p_crown_point_3_0.addOrReplaceChild("crown_point_3_1", CubeListBuilder.create()
                .texOffs(150, 205).addBox(-0.5F, -1.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.15F, -0.4F, -0.25F))
                .texOffs(185, 209).addBox(-0.5F, -2F, -0.5F, 1F, 1F, 1F),
                PartPose.offset(0F, -1.19F, 0F));
        PartDefinition p_crown_point_4_0 = p_crown_head_head.addOrReplaceChild("crown_point_4_0", CubeListBuilder.create()
                .texOffs(203, 197).addBox(-1F, -1.54F, -0.5F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.46F, -0.1F)),
                PartPose.offset(-3.2F, -9.7F, 5.4F));
        PartDefinition p_crown_point_4_1 = p_crown_point_4_0.addOrReplaceChild("crown_point_4_1", CubeListBuilder.create()
                .texOffs(190, 209).addBox(-0.5F, -0.95F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.15F, -0.05F, -0.25F))
                .texOffs(195, 209).addBox(-0.5F, -1.7F, -0.5F, 1F, 1F, 1F),
                PartPose.offset(0F, -0.83F, 0F));
        PartDefinition p_crown_point_5_0 = p_crown_head_head.addOrReplaceChild("crown_point_5_0", CubeListBuilder.create()
                .texOffs(210, 197).addBox(-1F, -1.54F, -0.5F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.46F, -0.1F)),
                PartPose.offset(3.2F, -9.7F, 5.4F));
        PartDefinition p_crown_point_5_1 = p_crown_point_5_0.addOrReplaceChild("crown_point_5_1", CubeListBuilder.create()
                .texOffs(200, 209).addBox(-0.5F, -0.95F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.15F, -0.05F, -0.25F))
                .texOffs(205, 209).addBox(-0.5F, -1.7F, -0.5F, 1F, 1F, 1F),
                PartPose.offset(0F, -0.83F, 0F));
        PartDefinition p_crown_point_6_0 = p_crown_head_head.addOrReplaceChild("crown_point_6_0", CubeListBuilder.create()
                .texOffs(126, 183).addBox(-1F, -1.6F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.4F, -0.3F)),
                PartPose.offset(-5.4F, -9.7F, -1.8F));
        PartDefinition p_crown_point_6_1 = p_crown_point_6_0.addOrReplaceChild("crown_point_6_1", CubeListBuilder.create()
                .texOffs(210, 209).addBox(-0.5F, -1F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.15F, 0F, -0.15F))
                .texOffs(215, 209).addBox(-0.5F, -1.8F, -0.5F, 1F, 1F, 1F),
                PartPose.offset(0F, -0.95F, 0F));
        PartDefinition p_crown_point_7_0 = p_crown_head_head.addOrReplaceChild("crown_point_7_0", CubeListBuilder.create()
                .texOffs(135, 183).addBox(-1F, -1.6F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.4F, -0.3F)),
                PartPose.offset(-5.4F, -9.7F, 1.8F));
        PartDefinition p_crown_point_7_1 = p_crown_point_7_0.addOrReplaceChild("crown_point_7_1", CubeListBuilder.create()
                .texOffs(220, 209).addBox(-0.5F, -1F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.15F, 0F, -0.15F))
                .texOffs(225, 209).addBox(-0.5F, -1.8F, -0.5F, 1F, 1F, 1F),
                PartPose.offset(0F, -0.95F, 0F));
        PartDefinition p_crown_point_8_0 = p_crown_head_head.addOrReplaceChild("crown_point_8_0", CubeListBuilder.create()
                .texOffs(144, 183).addBox(-1F, -1.6F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.4F, -0.3F)),
                PartPose.offset(5.4F, -9.7F, -1.8F));
        PartDefinition p_crown_point_8_1 = p_crown_point_8_0.addOrReplaceChild("crown_point_8_1", CubeListBuilder.create()
                .texOffs(230, 209).addBox(-0.5F, -1F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.15F, 0F, -0.15F))
                .texOffs(235, 209).addBox(-0.5F, -1.8F, -0.5F, 1F, 1F, 1F),
                PartPose.offset(0F, -0.95F, 0F));
        PartDefinition p_crown_point_9_0 = p_crown_head_head.addOrReplaceChild("crown_point_9_0", CubeListBuilder.create()
                .texOffs(153, 183).addBox(-1F, -1.6F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.4F, -0.3F)),
                PartPose.offset(5.4F, -9.7F, 1.8F));
        PartDefinition p_crown_point_9_1 = p_crown_point_9_0.addOrReplaceChild("crown_point_9_1", CubeListBuilder.create()
                .texOffs(240, 209).addBox(-0.5F, -1F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.15F, 0F, -0.15F))
                .texOffs(245, 209).addBox(-0.5F, -1.8F, -0.5F, 1F, 1F, 1F),
                PartPose.offset(0F, -0.95F, 0F));
        PartDefinition p_leather_head_head = root.addOrReplaceChild("leather_head_head", CubeListBuilder.create()
                .texOffs(104, 155).addBox(4.2F, -7.5F, -0.1F, 2F, 2F, 3F, new CubeDeformation(-0.4F, -0.1F, -0.3F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_leather_feather = p_leather_head_head.addOrReplaceChild("leather_feather", CubeListBuilder.create()
                .texOffs(198, 0).addBox(-0.5F, -8.6F, -1.5F, 1F, 9F, 3F, new CubeDeformation(-0.2F, -0.4F, -0.4F)),
                PartPose.offsetAndRotation(5F, -6.4F, 1.4F, -0.5236F, 0F, 0.2793F));
        PartDefinition p_leather_chest_body = root.addOrReplaceChild("leather_chest_body", CubeListBuilder.create()
                .texOffs(207, 0).addBox(-6F, -1.9F, -4F, 12F, 3F, 8F, new CubeDeformation(-0.25F, 0.05F, -0.15F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_leather_chest_left_arm = root.addOrReplaceChild("leather_chest_left_arm", CubeListBuilder.create()
                .texOffs(116, 85).addBox(-2.45F, -4.4F, -3.5F, 7F, 3F, 7F, new CubeDeformation(0.05F, -0.3F, 0.1F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_leather_chest_right_arm = root.addOrReplaceChild("leather_chest_right_arm", CubeListBuilder.create()
                .texOffs(145, 85).addBox(-4.55F, -4.4F, -3.5F, 7F, 3F, 7F, new CubeDeformation(0.05F, -0.3F, 0.1F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_leather_legs_body = root.addOrReplaceChild("leather_legs_body", CubeListBuilder.create()
                .texOffs(34, 107).addBox(-5F, 10.2F, -3F, 10F, 2F, 6F, new CubeDeformation(-0.1F, -0.4F, -0.1F))
                .texOffs(115, 155).addBox(2.1F, 10.7F, -4F, 3F, 3F, 2F, new CubeDeformation(-0.3F, -0.2F, -0.4F))
                .texOffs(217, 197).addBox(-4.5F, 10.2F, -3.5F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.2F, -0.2F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_leather_legs_left_leg = root.addOrReplaceChild("leather_legs_left_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_leather_legs_right_leg = root.addOrReplaceChild("leather_legs_right_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_leather_feet_left_leg = root.addOrReplaceChild("leather_feet_left_leg", CubeListBuilder.create()
                .texOffs(132, 17).addBox(-4F, 6.6F, -4F, 8F, 3F, 8F, new CubeDeformation(-0.4F, 0F, -0.4F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_leather_feet_right_leg = root.addOrReplaceChild("leather_feet_right_leg", CubeListBuilder.create()
                .texOffs(165, 17).addBox(-4F, 6.6F, -4F, 8F, 3F, 8F, new CubeDeformation(-0.4F, 0F, -0.4F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_chainmail_head_head = root.addOrReplaceChild("chainmail_head_head", CubeListBuilder.create()
                .texOffs(31, 117).addBox(-5.5F, -3.9F, 4.6F, 11F, 6F, 1F, new CubeDeformation(-0.2F, -0.3F, -0.2F))
                .texOffs(0, 0).addBox(-5.8F, -3.9F, -4.7F, 1F, 6F, 10F, new CubeDeformation(-0.2F, -0.3F, -0.3F))
                .texOffs(23, 0).addBox(4.8F, -3.9F, -4.7F, 1F, 6F, 10F, new CubeDeformation(-0.2F, -0.3F, -0.3F))
                .texOffs(140, 167).addBox(-0.5F, -6F, -5.85F, 1F, 4F, 1F, new CubeDeformation(0F, 0F, -0.15F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_chainmail_chest_body = root.addOrReplaceChild("chainmail_chest_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_chainmail_chest_left_arm = root.addOrReplaceChild("chainmail_chest_left_arm", CubeListBuilder.create()
                .texOffs(67, 107).addBox(-2.4F, -3.7F, -3.5F, 7F, 1F, 7F, new CubeDeformation(-0.2F, 0F, 0F))
                .texOffs(164, 0).addBox(3.7F, -3.6F, -3.5F, 1F, 5F, 7F, new CubeDeformation(-0.2F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_chainmail_chest_right_arm = root.addOrReplaceChild("chainmail_chest_right_arm", CubeListBuilder.create()
                .texOffs(96, 107).addBox(-4.6F, -3.7F, -3.5F, 7F, 1F, 7F, new CubeDeformation(-0.2F, 0F, 0F))
                .texOffs(181, 0).addBox(-4.7F, -3.6F, -3.5F, 1F, 5F, 7F, new CubeDeformation(-0.2F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_chainmail_legs_body = root.addOrReplaceChild("chainmail_legs_body", CubeListBuilder.create()
                .texOffs(86, 126).addBox(-5F, 9.8F, -3.2F, 10F, 5F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(109, 126).addBox(-5F, 9.8F, 2.2F, 10F, 5F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.2F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_chainmail_legs_left_leg = root.addOrReplaceChild("chainmail_legs_left_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_chainmail_legs_right_leg = root.addOrReplaceChild("chainmail_legs_right_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_iron_head_head = root.addOrReplaceChild("iron_head_head", CubeListBuilder.create()
                .texOffs(116, 148).addBox(-1F, -6.7F, -5.9F, 2F, 5F, 1F, new CubeDeformation(-0.4F, -0.3F, -0.1F))
                .texOffs(33, 29).addBox(-1F, -10.45F, -4.5F, 2F, 2F, 9F, new CubeDeformation(-0.4F, -0.45F, -0.1F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_iron_chest_body = root.addOrReplaceChild("iron_chest_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_iron_chest_left_arm = root.addOrReplaceChild("iron_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_iron_pauldron_left_arm = p_iron_chest_left_arm.addOrReplaceChild("iron_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(77, 96).addBox(-3.2F, -1.5F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.2F, -0.1F, 0F))
                .texOffs(174, 85).addBox(-1.9F, 0.2F, -4F, 6F, 2F, 8F, new CubeDeformation(-0.1F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.2094F));
        PartDefinition p_iron_chest_right_arm = root.addOrReplaceChild("iron_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_iron_pauldron_right_arm = p_iron_chest_right_arm.addOrReplaceChild("iron_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(106, 96).addBox(-3.8F, -1.5F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.2F, -0.1F, 0F))
                .texOffs(203, 85).addBox(-4.1F, 0.2F, -4F, 6F, 2F, 8F, new CubeDeformation(-0.1F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.2094F));
        PartDefinition p_iron_legs_body = root.addOrReplaceChild("iron_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_iron_legs_left_leg = root.addOrReplaceChild("iron_legs_left_leg", CubeListBuilder.create()
                .texOffs(121, 173).addBox(-2F, 5F, -3.2F, 4F, 3F, 1F, new CubeDeformation(-0.1F, -0.1F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_iron_legs_right_leg = root.addOrReplaceChild("iron_legs_right_leg", CubeListBuilder.create()
                .texOffs(132, 173).addBox(-2F, 5F, -3.2F, 4F, 3F, 1F, new CubeDeformation(-0.1F, -0.1F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_iron_feet_left_leg = root.addOrReplaceChild("iron_feet_left_leg", CubeListBuilder.create()
                .texOffs(183, 148).addBox(-2.5F, 10.25F, -4.35F, 5F, 3F, 2F, new CubeDeformation(-0.1F, -0.35F, -0.35F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_iron_feet_right_leg = root.addOrReplaceChild("iron_feet_right_leg", CubeListBuilder.create()
                .texOffs(198, 148).addBox(-2.5F, 10.25F, -4.35F, 5F, 3F, 2F, new CubeDeformation(-0.1F, -0.35F, -0.35F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_gold_head_head = root.addOrReplaceChild("gold_head_head", CubeListBuilder.create()
                .texOffs(224, 197).addBox(-1F, -7.2F, -5.85F, 2F, 2F, 1F, new CubeDeformation(0F, 0F, -0.15F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_gold_ray_0_0 = p_gold_head_head.addOrReplaceChild("gold_ray_0_0", CubeListBuilder.create()
                .texOffs(162, 183).addBox(-1F, -1.935F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.065F, -0.35F)),
                PartPose.offsetAndRotation(0F, -9.2F, -1F, -0.384F, 0F, -0.5236F));
        PartDefinition p_gold_ray_0_1 = p_gold_ray_0_0.addOrReplaceChild("gold_ray_0_1", CubeListBuilder.create()
                .texOffs(155, 205).addBox(-0.5F, -1.85F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.15F, -0.1425F)),
                PartPose.offset(0F, -1.62F, 0F));
        PartDefinition p_gold_ray_1_0 = p_gold_head_head.addOrReplaceChild("gold_ray_1_0", CubeListBuilder.create()
                .texOffs(117, 167).addBox(-1F, -2.765F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.35F, -0.235F, -0.35F)),
                PartPose.offsetAndRotation(0F, -9.2F, -1F, -0.1745F, 0F, 0F));
        PartDefinition p_gold_ray_1_1 = p_gold_ray_1_0.addOrReplaceChild("gold_ray_1_1", CubeListBuilder.create()
                .texOffs(178, 188).addBox(-0.5F, -2.65F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.1425F, -0.35F, -0.1425F)),
                PartPose.offset(0F, -2.28F, 0F));
        PartDefinition p_gold_ray_2_0 = p_gold_head_head.addOrReplaceChild("gold_ray_2_0", CubeListBuilder.create()
                .texOffs(171, 183).addBox(-1F, -1.935F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.065F, -0.35F)),
                PartPose.offsetAndRotation(0F, -9.2F, -1F, 0.0349F, 0F, 0.5236F));
        PartDefinition p_gold_ray_2_1 = p_gold_ray_2_0.addOrReplaceChild("gold_ray_2_1", CubeListBuilder.create()
                .texOffs(160, 205).addBox(-0.5F, -1.85F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.15F, -0.1425F)),
                PartPose.offset(0F, -1.62F, 0F));
        PartDefinition p_gold_chest_body = root.addOrReplaceChild("gold_chest_body", CubeListBuilder.create()
                .texOffs(126, 155).addBox(-2F, 2.2F, -3.85F, 4F, 4F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.15F))
                .texOffs(231, 197).addBox(-1F, 3.2F, -4.35F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.3F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_gold_chest_left_arm = root.addOrReplaceChild("gold_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_gold_pauldron_left_arm = p_gold_chest_left_arm.addOrReplaceChild("gold_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(0, 63).addBox(-3.2F, -1.6F, -4F, 7F, 2F, 8F, new CubeDeformation(0F, 0F, -0.4F))
                .texOffs(31, 63).addBox(-2.4F, 0.1F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.4F, -0.2F, -0.3F))
                .texOffs(43, 193).addBox(1.1F, -2.3F, -1F, 2F, 1F, 2F, new CubeDeformation(-0.3F, -0.1F, -0.3F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.2443F));
        PartDefinition p_gold_chest_right_arm = root.addOrReplaceChild("gold_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_gold_pauldron_right_arm = p_gold_chest_right_arm.addOrReplaceChild("gold_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(62, 63).addBox(-3.8F, -1.6F, -4F, 7F, 2F, 8F, new CubeDeformation(0F, 0F, -0.4F))
                .texOffs(93, 63).addBox(-4.6F, 0.1F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.4F, -0.2F, -0.3F))
                .texOffs(52, 193).addBox(-3.1F, -2.3F, -1F, 2F, 1F, 2F, new CubeDeformation(-0.3F, -0.1F, -0.3F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.2443F));
        PartDefinition p_gold_legs_body = root.addOrReplaceChild("gold_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_gold_legs_left_leg = root.addOrReplaceChild("gold_legs_left_leg", CubeListBuilder.create()
                .texOffs(209, 126).addBox(-3F, -0.9F, -3.3F, 6F, 5F, 1F, new CubeDeformation(-0.3F, -0.3F, 0F))
                .texOffs(143, 173).addBox(-2F, 5F, -3.2F, 4F, 3F, 1F, new CubeDeformation(-0.1F, -0.1F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_gold_legs_right_leg = root.addOrReplaceChild("gold_legs_right_leg", CubeListBuilder.create()
                .texOffs(224, 126).addBox(-3F, -0.9F, -3.3F, 6F, 5F, 1F, new CubeDeformation(-0.3F, -0.3F, 0F))
                .texOffs(154, 173).addBox(-2F, 5F, -3.2F, 4F, 3F, 1F, new CubeDeformation(-0.1F, -0.1F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_gold_feet_left_leg = root.addOrReplaceChild("gold_feet_left_leg", CubeListBuilder.create()
                .texOffs(213, 148).addBox(-2.5F, 10.25F, -4.35F, 5F, 3F, 2F, new CubeDeformation(-0.1F, -0.35F, -0.35F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_gold_feet_right_leg = root.addOrReplaceChild("gold_feet_right_leg", CubeListBuilder.create()
                .texOffs(228, 148).addBox(-2.5F, 10.25F, -4.35F, 5F, 3F, 2F, new CubeDeformation(-0.1F, -0.35F, -0.35F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_diamond_head_head = root.addOrReplaceChild("diamond_head_head", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_diamond_crest_0_0 = p_diamond_head_head.addOrReplaceChild("diamond_crest_0_0", CubeListBuilder.create()
                .texOffs(180, 183).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.2F, -0.285F, -0.2F)),
                PartPose.offsetAndRotation(0F, -9.2F, -3F, -0.3491F, 0F, 0F));
        PartDefinition p_diamond_crest_0_1 = p_diamond_crest_0_0.addOrReplaceChild("diamond_crest_0_1", CubeListBuilder.create()
                .texOffs(165, 205).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.06F, -0.35F, -0.06F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_diamond_crest_1_0 = p_diamond_head_head.addOrReplaceChild("diamond_crest_1_0", CubeListBuilder.create()
                .texOffs(189, 183).addBox(-1F, -1.99F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.2F, -0.01F, -0.2F)),
                PartPose.offsetAndRotation(0F, -9.2F, -0.4F, -0.3491F, 0F, 0F));
        PartDefinition p_diamond_crest_1_1 = p_diamond_crest_1_0.addOrReplaceChild("diamond_crest_1_1", CubeListBuilder.create()
                .texOffs(170, 205).addBox(-0.5F, -1.9F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.06F, -0.1F, -0.06F)),
                PartPose.offset(0F, -1.73F, 0F));
        PartDefinition p_diamond_crest_2_0 = p_diamond_head_head.addOrReplaceChild("diamond_crest_2_0", CubeListBuilder.create()
                .texOffs(198, 183).addBox(-1F, -1.77F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.2F, -0.23F, -0.2F)),
                PartPose.offsetAndRotation(0F, -9.2F, 2.2F, -0.3491F, 0F, 0F));
        PartDefinition p_diamond_crest_2_1 = p_diamond_crest_2_0.addOrReplaceChild("diamond_crest_2_1", CubeListBuilder.create()
                .texOffs(175, 205).addBox(-0.5F, -1.7F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.06F, -0.3F, -0.06F)),
                PartPose.offset(0F, -1.29F, 0F));
        PartDefinition p_diamond_chest_body = root.addOrReplaceChild("diamond_chest_body", CubeListBuilder.create()
                .texOffs(136, 188).addBox(-1F, 1.8F, -3.9F, 2F, 3F, 1F, new CubeDeformation(0F, -0.2F, -0.1F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_diamond_chest_left_arm = root.addOrReplaceChild("diamond_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_diamond_pauldron_left_arm = p_diamond_chest_left_arm.addOrReplaceChild("diamond_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(135, 96).addBox(-3.2F, -1.6F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.2F, -0.2F, 0F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.1745F));
        PartDefinition p_diamond_shoulder_left_arm_0_0 = p_diamond_pauldron_left_arm.addOrReplaceChild("diamond_shoulder_left_arm_0_0", CubeListBuilder.create()
                .texOffs(207, 183).addBox(-1F, -1.88F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.25F, -0.12F, -0.25F)),
                PartPose.offsetAndRotation(1.8F, -1.3F, -1F, 0.1396F, 0F, 0.4189F));
        PartDefinition p_diamond_shoulder_left_arm_0_1 = p_diamond_shoulder_left_arm_0_0.addOrReplaceChild("diamond_shoulder_left_arm_0_1", CubeListBuilder.create()
                .texOffs(180, 205).addBox(-0.5F, -1.8F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.0875F, -0.2F, -0.0875F)),
                PartPose.offset(0F, -1.51F, 0F));
        PartDefinition p_diamond_shoulder_left_arm_1_0 = p_diamond_pauldron_left_arm.addOrReplaceChild("diamond_shoulder_left_arm_1_0", CubeListBuilder.create()
                .texOffs(216, 183).addBox(-1F, -1.605F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.395F, -0.45F)),
                PartPose.offsetAndRotation(2.6F, -1.3F, 1.6F, -0.1745F, 0F, 0.6632F));
        PartDefinition p_diamond_shoulder_left_arm_1_1 = p_diamond_shoulder_left_arm_1_0.addOrReplaceChild("diamond_shoulder_left_arm_1_1", CubeListBuilder.create()
                .texOffs(185, 205).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1975F, -0.45F, -0.1975F)),
                PartPose.offset(0F, -0.96F, 0F));
        PartDefinition p_diamond_chest_right_arm = root.addOrReplaceChild("diamond_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_diamond_pauldron_right_arm = p_diamond_chest_right_arm.addOrReplaceChild("diamond_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(164, 96).addBox(-3.8F, -1.6F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.2F, -0.2F, 0F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.1745F));
        PartDefinition p_diamond_shoulder_right_arm_0_0 = p_diamond_pauldron_right_arm.addOrReplaceChild("diamond_shoulder_right_arm_0_0", CubeListBuilder.create()
                .texOffs(225, 183).addBox(-1F, -1.88F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.25F, -0.12F, -0.25F)),
                PartPose.offsetAndRotation(-1.8F, -1.3F, -1F, 0.1396F, 0F, -0.4189F));
        PartDefinition p_diamond_shoulder_right_arm_0_1 = p_diamond_shoulder_right_arm_0_0.addOrReplaceChild("diamond_shoulder_right_arm_0_1", CubeListBuilder.create()
                .texOffs(190, 205).addBox(-0.5F, -1.8F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.0875F, -0.2F, -0.0875F)),
                PartPose.offset(0F, -1.51F, 0F));
        PartDefinition p_diamond_shoulder_right_arm_1_0 = p_diamond_pauldron_right_arm.addOrReplaceChild("diamond_shoulder_right_arm_1_0", CubeListBuilder.create()
                .texOffs(234, 183).addBox(-1F, -1.605F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.395F, -0.45F)),
                PartPose.offsetAndRotation(-2.6F, -1.3F, 1.6F, -0.1745F, 0F, -0.6632F));
        PartDefinition p_diamond_shoulder_right_arm_1_1 = p_diamond_shoulder_right_arm_1_0.addOrReplaceChild("diamond_shoulder_right_arm_1_1", CubeListBuilder.create()
                .texOffs(195, 205).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1975F, -0.45F, -0.1975F)),
                PartPose.offset(0F, -0.96F, 0F));
        PartDefinition p_diamond_legs_body = root.addOrReplaceChild("diamond_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_diamond_legs_left_leg = root.addOrReplaceChild("diamond_legs_left_leg", CubeListBuilder.create()
                .texOffs(238, 197).addBox(-1F, 5.4F, -3.5F, 2F, 2F, 1F, new CubeDeformation(0F, 0F, -0.1F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_diamond_legs_right_leg = root.addOrReplaceChild("diamond_legs_right_leg", CubeListBuilder.create()
                .texOffs(245, 197).addBox(-1F, 5.4F, -3.5F, 2F, 2F, 1F, new CubeDeformation(0F, 0F, -0.1F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_diamond_feet_left_leg = root.addOrReplaceChild("diamond_feet_left_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_diamond_heel_left_leg_0 = p_diamond_feet_left_leg.addOrReplaceChild("diamond_heel_left_leg_0", CubeListBuilder.create()
                .texOffs(243, 183).addBox(-1F, -1.605F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.395F, -0.4F)),
                PartPose.offsetAndRotation(0F, 10.9F, 2.8F, -0.9774F, 0F, 0F));
        PartDefinition p_diamond_heel_left_leg_1 = p_diamond_heel_left_leg_0.addOrReplaceChild("diamond_heel_left_leg_1", CubeListBuilder.create()
                .texOffs(200, 205).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.17F, -0.45F, -0.17F)),
                PartPose.offset(0F, -0.96F, 0F));
        PartDefinition p_diamond_feet_right_leg = root.addOrReplaceChild("diamond_feet_right_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_diamond_heel_right_leg_0 = p_diamond_feet_right_leg.addOrReplaceChild("diamond_heel_right_leg_0", CubeListBuilder.create()
                .texOffs(0, 188).addBox(-1F, -1.605F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.395F, -0.4F)),
                PartPose.offsetAndRotation(0F, 10.9F, 2.8F, -0.9774F, 0F, 0F));
        PartDefinition p_diamond_heel_right_leg_1 = p_diamond_heel_right_leg_0.addOrReplaceChild("diamond_heel_right_leg_1", CubeListBuilder.create()
                .texOffs(205, 205).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.17F, -0.45F, -0.17F)),
                PartPose.offset(0F, -0.96F, 0F));
        PartDefinition p_netherite_head_head = root.addOrReplaceChild("netherite_head_head", CubeListBuilder.create()
                .texOffs(0, 209).addBox(-5F, -6.3F, -5.85F, 10F, 1F, 1F, new CubeDeformation(-0.4F, -0.1F, -0.15F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_netherite_horn_left_0 = p_netherite_head_head.addOrReplaceChild("netherite_horn_left_0", CubeListBuilder.create()
                .texOffs(9, 188).addBox(-1F, -2F, -1F, 2F, 2F, 2F),
                PartPose.offsetAndRotation(4.4F, -7.6F, -2.6F, 0F, 0F, 1.2217F));
        PartDefinition p_netherite_horn_left_1 = p_netherite_horn_left_0.addOrReplaceChild("netherite_horn_left_1", CubeListBuilder.create()
                .texOffs(18, 188).addBox(-1F, -2F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, -1.75F, 0F, 0.5236F, 0F, -0.5236F));
        PartDefinition p_netherite_horn_left_2 = p_netherite_horn_left_1.addOrReplaceChild("netherite_horn_left_2", CubeListBuilder.create()
                .texOffs(27, 188).addBox(-1F, -1.9F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.1F, -0.45F)),
                PartPose.offsetAndRotation(0F, -1.75F, 0F, 0.5934F, 0F, -0.4189F));
        PartDefinition p_netherite_horn_right_0 = p_netherite_head_head.addOrReplaceChild("netherite_horn_right_0", CubeListBuilder.create()
                .texOffs(36, 188).addBox(-1F, -2F, -1F, 2F, 2F, 2F),
                PartPose.offsetAndRotation(-4.4F, -7.6F, -2.6F, 0F, 0F, -1.2217F));
        PartDefinition p_netherite_horn_right_1 = p_netherite_horn_right_0.addOrReplaceChild("netherite_horn_right_1", CubeListBuilder.create()
                .texOffs(45, 188).addBox(-1F, -2F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, -1.75F, 0F, 0.5236F, 0F, 0.5236F));
        PartDefinition p_netherite_horn_right_2 = p_netherite_horn_right_1.addOrReplaceChild("netherite_horn_right_2", CubeListBuilder.create()
                .texOffs(54, 188).addBox(-1F, -1.9F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.1F, -0.45F)),
                PartPose.offsetAndRotation(0F, -1.75F, 0F, 0.5934F, 0F, 0.4189F));
        PartDefinition p_netherite_chest_body = root.addOrReplaceChild("netherite_chest_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_netherite_chest_left_arm = root.addOrReplaceChild("netherite_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_netherite_pauldron_left_arm = p_netherite_chest_left_arm.addOrReplaceChild("netherite_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(198, 17).addBox(-3.7F, -2.25F, -4F, 8F, 3F, 8F, new CubeDeformation(-0.3F, -0.35F, -0.2F))
                .texOffs(124, 63).addBox(-2.4F, 0.35F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.2F, -0.05F, -0.1F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.2618F));
        PartDefinition p_netherite_spike_left_arm_0_0 = p_netherite_pauldron_left_arm.addOrReplaceChild("netherite_spike_left_arm_0_0", CubeListBuilder.create()
                .texOffs(63, 188).addBox(-1F, -1.66F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.34F, -0.4F)),
                PartPose.offsetAndRotation(2F, -1.8F, -1.6F, 0F, 0F, 0.5236F));
        PartDefinition p_netherite_spike_left_arm_0_1 = p_netherite_spike_left_arm_0_0.addOrReplaceChild("netherite_spike_left_arm_0_1", CubeListBuilder.create()
                .texOffs(210, 205).addBox(-0.5F, -1.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.17F, -0.4F, -0.17F)),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_netherite_spike_left_arm_1_0 = p_netherite_pauldron_left_arm.addOrReplaceChild("netherite_spike_left_arm_1_0", CubeListBuilder.create()
                .texOffs(72, 188).addBox(-1F, -1.66F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.34F, -0.4F)),
                PartPose.offsetAndRotation(2F, -1.8F, 1.6F, 0F, 0F, 0.5236F));
        PartDefinition p_netherite_spike_left_arm_1_1 = p_netherite_spike_left_arm_1_0.addOrReplaceChild("netherite_spike_left_arm_1_1", CubeListBuilder.create()
                .texOffs(215, 205).addBox(-0.5F, -1.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.17F, -0.4F, -0.17F)),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_netherite_chest_right_arm = root.addOrReplaceChild("netherite_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_netherite_pauldron_right_arm = p_netherite_chest_right_arm.addOrReplaceChild("netherite_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(0, 29).addBox(-4.3F, -2.25F, -4F, 8F, 3F, 8F, new CubeDeformation(-0.3F, -0.35F, -0.2F))
                .texOffs(155, 63).addBox(-4.6F, 0.35F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.2F, -0.05F, -0.1F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.2618F));
        PartDefinition p_netherite_spike_right_arm_0_0 = p_netherite_pauldron_right_arm.addOrReplaceChild("netherite_spike_right_arm_0_0", CubeListBuilder.create()
                .texOffs(81, 188).addBox(-1F, -1.66F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.34F, -0.4F)),
                PartPose.offsetAndRotation(-2F, -1.8F, -1.6F, 0F, 0F, -0.5236F));
        PartDefinition p_netherite_spike_right_arm_0_1 = p_netherite_spike_right_arm_0_0.addOrReplaceChild("netherite_spike_right_arm_0_1", CubeListBuilder.create()
                .texOffs(220, 205).addBox(-0.5F, -1.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.17F, -0.4F, -0.17F)),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_netherite_spike_right_arm_1_0 = p_netherite_pauldron_right_arm.addOrReplaceChild("netherite_spike_right_arm_1_0", CubeListBuilder.create()
                .texOffs(90, 188).addBox(-1F, -1.66F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.34F, -0.4F)),
                PartPose.offsetAndRotation(-2F, -1.8F, 1.6F, 0F, 0F, -0.5236F));
        PartDefinition p_netherite_spike_right_arm_1_1 = p_netherite_spike_right_arm_1_0.addOrReplaceChild("netherite_spike_right_arm_1_1", CubeListBuilder.create()
                .texOffs(225, 205).addBox(-0.5F, -1.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.17F, -0.4F, -0.17F)),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_netherite_legs_body = root.addOrReplaceChild("netherite_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_netherite_legs_left_leg = root.addOrReplaceChild("netherite_legs_left_leg", CubeListBuilder.create()
                .texOffs(239, 126).addBox(-3F, -0.9F, -3.3F, 6F, 5F, 1F, new CubeDeformation(-0.3F, -0.3F, 0F))
                .texOffs(165, 173).addBox(-2F, 5F, -3.2F, 4F, 3F, 1F, new CubeDeformation(-0.1F, -0.1F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_netherite_legs_right_leg = root.addOrReplaceChild("netherite_legs_right_leg", CubeListBuilder.create()
                .texOffs(0, 134).addBox(-3F, -0.9F, -3.3F, 6F, 5F, 1F, new CubeDeformation(-0.3F, -0.3F, 0F))
                .texOffs(176, 173).addBox(-2F, 5F, -3.2F, 4F, 3F, 1F, new CubeDeformation(-0.1F, -0.1F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_netherite_feet_left_leg = root.addOrReplaceChild("netherite_feet_left_leg", CubeListBuilder.create()
                .texOffs(0, 155).addBox(-2.5F, 10.15F, -4.45F, 5F, 3F, 2F, new CubeDeformation(0F, -0.25F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_netherite_toe_left_leg_0 = p_netherite_feet_left_leg.addOrReplaceChild("netherite_toe_left_leg_0", CubeListBuilder.create()
                .texOffs(250, 209).addBox(-0.5F, -0.94F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.06F, 0F)),
                PartPose.offsetAndRotation(0F, 11.2F, -4F, 1.0821F, 0F, 0F));
        PartDefinition p_netherite_toe_left_leg_1 = p_netherite_toe_left_leg_0.addOrReplaceChild("netherite_toe_left_leg_1", CubeListBuilder.create()
                .texOffs(0, 212).addBox(-0.5F, -0.9F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.1F, -0.225F)),
                PartPose.offset(0F, -0.63F, 0F));
        PartDefinition p_netherite_feet_right_leg = root.addOrReplaceChild("netherite_feet_right_leg", CubeListBuilder.create()
                .texOffs(15, 155).addBox(-2.5F, 10.15F, -4.45F, 5F, 3F, 2F, new CubeDeformation(0F, -0.25F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_netherite_toe_right_leg_0 = p_netherite_feet_right_leg.addOrReplaceChild("netherite_toe_right_leg_0", CubeListBuilder.create()
                .texOffs(5, 212).addBox(-0.5F, -0.94F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.06F, 0F)),
                PartPose.offsetAndRotation(0F, 11.2F, -4F, 1.0821F, 0F, 0F));
        PartDefinition p_netherite_toe_right_leg_1 = p_netherite_toe_right_leg_0.addOrReplaceChild("netherite_toe_right_leg_1", CubeListBuilder.create()
                .texOffs(10, 212).addBox(-0.5F, -0.9F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.1F, -0.225F)),
                PartPose.offset(0F, -0.63F, 0F));
        PartDefinition p_copper_head_head = root.addOrReplaceChild("copper_head_head", CubeListBuilder.create()
                .texOffs(132, 126).addBox(-2F, -10.4F, -2F, 4F, 2F, 4F, new CubeDeformation(-0.4F, -0.4F, -0.4F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_copper_spike_0 = p_copper_head_head.addOrReplaceChild("copper_spike_0", CubeListBuilder.create()
                .texOffs(99, 188).addBox(-1F, -2F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offset(0F, -9.8F, 0F));
        PartDefinition p_copper_spike_1 = p_copper_spike_0.addOrReplaceChild("copper_spike_1", CubeListBuilder.create()
                .texOffs(230, 205).addBox(-0.5F, -2F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1F, 0F, -0.1F)),
                PartPose.offset(0F, -1.75F, 0F));
        PartDefinition p_copper_chest_body = root.addOrReplaceChild("copper_chest_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_copper_chest_left_arm = root.addOrReplaceChild("copper_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_copper_pauldron_left_arm = p_copper_chest_left_arm.addOrReplaceChild("copper_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(193, 96).addBox(-3.2F, -1.55F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.2F, -0.05F, 0F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.2094F));
        PartDefinition p_copper_chest_right_arm = root.addOrReplaceChild("copper_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_copper_pauldron_right_arm = p_copper_chest_right_arm.addOrReplaceChild("copper_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(222, 96).addBox(-3.8F, -1.55F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.2F, -0.05F, 0F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.2094F));
        PartDefinition p_copper_legs_body = root.addOrReplaceChild("copper_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_copper_legs_left_leg = root.addOrReplaceChild("copper_legs_left_leg", CubeListBuilder.create()
                .texOffs(187, 173).addBox(-2F, 5F, -3.2F, 4F, 3F, 1F, new CubeDeformation(-0.1F, -0.1F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_copper_legs_right_leg = root.addOrReplaceChild("copper_legs_right_leg", CubeListBuilder.create()
                .texOffs(198, 173).addBox(-2F, 5F, -3.2F, 4F, 3F, 1F, new CubeDeformation(-0.1F, -0.1F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_copper_feet_left_leg = root.addOrReplaceChild("copper_feet_left_leg", CubeListBuilder.create()
                .texOffs(30, 155).addBox(-2.5F, 10.25F, -4.35F, 5F, 3F, 2F, new CubeDeformation(-0.1F, -0.35F, -0.35F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_copper_feet_right_leg = root.addOrReplaceChild("copper_feet_right_leg", CubeListBuilder.create()
                .texOffs(45, 155).addBox(-2.5F, 10.25F, -4.35F, 5F, 3F, 2F, new CubeDeformation(-0.1F, -0.35F, -0.35F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_turtle_head_head = root.addOrReplaceChild("turtle_head_head", CubeListBuilder.create()
                .texOffs(46, 0).addBox(-6F, -9.9F, -6F, 12F, 1F, 12F, new CubeDeformation(-0.4F, 0F, -0.4F))
                .texOffs(140, 29).addBox(-4.5F, -10.9F, -4.5F, 9F, 1F, 9F, new CubeDeformation(-0.1F, 0F, -0.1F))
                .texOffs(56, 117).addBox(-3F, -11.7F, -3F, 6F, 1F, 6F, new CubeDeformation(-0.2F, -0.1F, -0.2F)),
                PartPose.offset(0F, 0F, 0F));
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
