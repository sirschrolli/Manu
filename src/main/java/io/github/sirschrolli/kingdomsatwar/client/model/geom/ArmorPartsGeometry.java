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
                .texOffs(120, 21).addBox(-0.5F, -10.75F, -5F, 1F, 2F, 10F, new CubeDeformation(0F, -0.15F, -0.4F))
                .texOffs(56, 238).addBox(-1F, -11.5F, 1.8F, 2F, 2F, 3F, new CubeDeformation(0F, -0.3F, -0.4F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_steel_plume_0 = p_steel_head_head.addOrReplaceChild("steel_plume_0", CubeListBuilder.create()
                .texOffs(30, 211).addBox(-1.5F, -2.8F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.2F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(0F, -10.6F, 3.3F, -0.2094F, 0F, 0F));
        PartDefinition p_steel_plume_1 = p_steel_plume_0.addOrReplaceChild("steel_plume_1", CubeListBuilder.create()
                .texOffs(219, 187).addBox(-1.5F, -3.6F, -1.5F, 3F, 4F, 3F, new CubeDeformation(0F, -0.4F, -0.1F)),
                PartPose.offsetAndRotation(0F, -2.35F, 0F, -0.4887F, 0F, 0F));
        PartDefinition p_steel_plume_2 = p_steel_plume_1.addOrReplaceChild("steel_plume_2", CubeListBuilder.create()
                .texOffs(232, 187).addBox(-1.5F, -3.8F, -1.5F, 3F, 4F, 3F, new CubeDeformation(-0.05F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(0F, -2.95F, 0F, -0.5585F, 0F, 0F));
        PartDefinition p_steel_plume_3 = p_steel_plume_2.addOrReplaceChild("steel_plume_3", CubeListBuilder.create()
                .texOffs(0, 196).addBox(-1.5F, -3.8F, -1.5F, 3F, 4F, 3F, new CubeDeformation(-0.25F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(0F, -3.35F, 0F, -0.5236F, 0F, 0F));
        PartDefinition p_steel_plume_4 = p_steel_plume_3.addOrReplaceChild("steel_plume_4", CubeListBuilder.create()
                .texOffs(112, 225).addBox(-1F, -3.7F, -1F, 2F, 4F, 2F, new CubeDeformation(0F, -0.3F, -0.1F)),
                PartPose.offsetAndRotation(0F, -3.35F, 0F, -0.384F, 0F, 0F));
        PartDefinition p_steel_plume_5 = p_steel_plume_4.addOrReplaceChild("steel_plume_5", CubeListBuilder.create()
                .texOffs(199, 238).addBox(-1F, -3F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.3F, 0F, -0.35F)),
                PartPose.offsetAndRotation(0F, -3.15F, 0F, -0.2443F, 0F, 0F));
        PartDefinition p_steel_chest_body = root.addOrReplaceChild("steel_chest_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_steel_chest_left_arm = root.addOrReplaceChild("steel_chest_left_arm", CubeListBuilder.create()
                .texOffs(13, 187).addBox(3.6F, 3.8F, -2F, 1F, 4F, 4F, new CubeDeformation(0F, -0.4F, -0.3F))
                .texOffs(45, 281).addBox(4.2F, 4.8F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.2F, -0.4F, -0.3F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_steel_pauldron_left_arm = p_steel_chest_left_arm.addOrReplaceChild("steel_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(82, 35).addBox(-3.8F, -2.15F, -4F, 8F, 3F, 8F, new CubeDeformation(-0.4F, -0.45F, -0.3F))
                .texOffs(123, 59).addBox(-2.5F, 0.1F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.2F, -0.1F, -0.2F))
                .texOffs(0, 104).addBox(-1.3F, 1.6F, -4F, 6F, 2F, 8F, new CubeDeformation(-0.2F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.2443F));
        PartDefinition p_steel_chest_right_arm = root.addOrReplaceChild("steel_chest_right_arm", CubeListBuilder.create()
                .texOffs(24, 187).addBox(-4.6F, 3.8F, -2F, 1F, 4F, 4F, new CubeDeformation(0F, -0.4F, -0.3F))
                .texOffs(52, 281).addBox(-5.2F, 4.8F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.2F, -0.4F, -0.3F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_steel_pauldron_right_arm = p_steel_chest_right_arm.addOrReplaceChild("steel_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(115, 35).addBox(-4.2F, -2.15F, -4F, 8F, 3F, 8F, new CubeDeformation(-0.4F, -0.45F, -0.3F))
                .texOffs(154, 59).addBox(-4.5F, 0.1F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.2F, -0.1F, -0.2F))
                .texOffs(29, 104).addBox(-4.7F, 1.6F, -4F, 6F, 2F, 8F, new CubeDeformation(-0.2F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.2443F));
        PartDefinition p_steel_legs_body = root.addOrReplaceChild("steel_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_steel_legs_left_leg = root.addOrReplaceChild("steel_legs_left_leg", CubeListBuilder.create()
                .texOffs(0, 261).addBox(-2F, 5F, -3.3F, 4F, 3F, 1F, new CubeDeformation(0F, -0.1F, 0F))
                .texOffs(208, 238).addBox(2F, 5.5F, -3.2F, 1F, 2F, 3F, new CubeDeformation(-0.1F, 0F, -0.4F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_steel_tasset_left_leg = p_steel_legs_left_leg.addOrReplaceChild("steel_tasset_left_leg", CubeListBuilder.create()
                .texOffs(166, 204).addBox(-3F, -0.1F, -0.6F, 6F, 5F, 1F, new CubeDeformation(-0.2F, -0.1F, 0F)),
                PartPose.offsetAndRotation(0F, -0.8F, -2.8F, -0.1222F, 0F, 0F));
        PartDefinition p_steel_legs_right_leg = root.addOrReplaceChild("steel_legs_right_leg", CubeListBuilder.create()
                .texOffs(11, 261).addBox(-2F, 5F, -3.3F, 4F, 3F, 1F, new CubeDeformation(0F, -0.1F, 0F))
                .texOffs(217, 238).addBox(-3F, 5.5F, -3.2F, 1F, 2F, 3F, new CubeDeformation(-0.1F, 0F, -0.4F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_steel_tasset_right_leg = p_steel_legs_right_leg.addOrReplaceChild("steel_tasset_right_leg", CubeListBuilder.create()
                .texOffs(181, 204).addBox(-3F, -0.1F, -0.6F, 6F, 5F, 1F, new CubeDeformation(-0.2F, -0.1F, 0F)),
                PartPose.offsetAndRotation(0F, -0.8F, -2.8F, -0.1222F, 0F, 0F));
        PartDefinition p_steel_feet_left_leg = root.addOrReplaceChild("steel_feet_left_leg", CubeListBuilder.create()
                .texOffs(44, 232).addBox(-2.5F, 10.2F, -4.45F, 5F, 3F, 2F, new CubeDeformation(0F, -0.3F, -0.25F))
                .texOffs(214, 286).addBox(-0.5F, 10.45F, 2.75F, 1F, 1F, 2F, new CubeDeformation(-0.15F, -0.15F, -0.05F))
                .texOffs(221, 286).addBox(-1F, 9.95F, 4.25F, 2F, 2F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_steel_feet_right_leg = root.addOrReplaceChild("steel_feet_right_leg", CubeListBuilder.create()
                .texOffs(59, 232).addBox(-2.5F, 10.2F, -4.45F, 5F, 3F, 2F, new CubeDeformation(0F, -0.3F, -0.25F))
                .texOffs(228, 286).addBox(-0.5F, 10.45F, 2.75F, 1F, 1F, 2F, new CubeDeformation(-0.15F, -0.15F, -0.05F))
                .texOffs(235, 286).addBox(-1F, 9.95F, 4.25F, 2F, 2F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_konterit_head_head = root.addOrReplaceChild("konterit_head_head", CubeListBuilder.create()
                .texOffs(242, 286).addBox(-1F, -6.8F, -5.75F, 2F, 2F, 1F, new CubeDeformation(0F, 0F, -0.15F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_kon_wing_left = p_konterit_head_head.addOrReplaceChild("kon_wing_left", CubeListBuilder.create()
                .texOffs(143, 196).addBox(-1F, -2.58F, -1.79F, 2F, 4F, 3F, new CubeDeformation(-0.4F, -0.26F, -0.05F)),
                PartPose.offsetAndRotation(5.2F, -5.2F, -0.8F, 0.2793F, 0.2793F, 0F));
        PartDefinition p_kon_wing_left_feather_0 = p_kon_wing_left.addOrReplaceChild("kon_wing_left_feather_0", CubeListBuilder.create()
                .texOffs(237, 158).addBox(-0.5F, -1F, -0.165F, 1F, 2F, 7F, new CubeDeformation(-0.25F, -0.0575F, -0.165F)),
                PartPose.offsetAndRotation(0F, -0.58F, 0.58F, 0.0698F, 0F, 0F));
        PartDefinition p_kon_wing_left_feather_1 = p_kon_wing_left.addOrReplaceChild("kon_wing_left_feather_1", CubeListBuilder.create()
                .texOffs(191, 47).addBox(-0.5F, -1F, -0.44F, 1F, 2F, 9F, new CubeDeformation(-0.25F, -0.0575F, -0.44F)),
                PartPose.offsetAndRotation(0F, -0.9425F, 0.58F, 0.3491F, 0F, 0F));
        PartDefinition p_kon_wing_left_feather_2 = p_kon_wing_left.addOrReplaceChild("kon_wing_left_feather_2", CubeListBuilder.create()
                .texOffs(212, 47).addBox(-0.5F, -1F, -0.295F, 1F, 2F, 9F, new CubeDeformation(-0.25F, -0.13F, -0.295F)),
                PartPose.offsetAndRotation(0F, -1.305F, 0.58F, 0.6283F, 0F, 0F));
        PartDefinition p_kon_wing_left_feather_3 = p_kon_wing_left.addOrReplaceChild("kon_wing_left_feather_3", CubeListBuilder.create()
                .texOffs(46, 137).addBox(-0.5F, -1F, -0.375F, 1F, 2F, 8F, new CubeDeformation(-0.25F, -0.2025F, -0.375F)),
                PartPose.offsetAndRotation(0F, -1.6675F, 0.58F, 0.9076F, 0F, 0F));
        PartDefinition p_kon_wing_left_feather_4 = p_kon_wing_left.addOrReplaceChild("kon_wing_left_feather_4", CubeListBuilder.create()
                .texOffs(155, 178).addBox(-0.5F, -1F, -0.245F, 1F, 2F, 6F, new CubeDeformation(-0.25F, -0.275F, -0.245F)),
                PartPose.offsetAndRotation(0F, -2.03F, 0.58F, 1.1868F, 0F, 0F));
        PartDefinition p_kon_wing_right = p_konterit_head_head.addOrReplaceChild("kon_wing_right", CubeListBuilder.create()
                .texOffs(154, 196).addBox(-1F, -2.58F, -1.79F, 2F, 4F, 3F, new CubeDeformation(-0.4F, -0.26F, -0.05F)),
                PartPose.offsetAndRotation(-5.2F, -5.2F, -0.8F, 0.2793F, -0.2793F, 0F));
        PartDefinition p_kon_wing_right_feather_0 = p_kon_wing_right.addOrReplaceChild("kon_wing_right_feather_0", CubeListBuilder.create()
                .texOffs(0, 168).addBox(-0.5F, -1F, -0.165F, 1F, 2F, 7F, new CubeDeformation(-0.25F, -0.0575F, -0.165F)),
                PartPose.offsetAndRotation(0F, -0.58F, 0.58F, 0.0698F, 0F, 0F));
        PartDefinition p_kon_wing_right_feather_1 = p_kon_wing_right.addOrReplaceChild("kon_wing_right_feather_1", CubeListBuilder.create()
                .texOffs(233, 47).addBox(-0.5F, -1F, -0.44F, 1F, 2F, 9F, new CubeDeformation(-0.25F, -0.0575F, -0.44F)),
                PartPose.offsetAndRotation(0F, -0.9425F, 0.58F, 0.3491F, 0F, 0F));
        PartDefinition p_kon_wing_right_feather_2 = p_kon_wing_right.addOrReplaceChild("kon_wing_right_feather_2", CubeListBuilder.create()
                .texOffs(0, 59).addBox(-0.5F, -1F, -0.295F, 1F, 2F, 9F, new CubeDeformation(-0.25F, -0.13F, -0.295F)),
                PartPose.offsetAndRotation(0F, -1.305F, 0.58F, 0.6283F, 0F, 0F));
        PartDefinition p_kon_wing_right_feather_3 = p_kon_wing_right.addOrReplaceChild("kon_wing_right_feather_3", CubeListBuilder.create()
                .texOffs(65, 137).addBox(-0.5F, -1F, -0.375F, 1F, 2F, 8F, new CubeDeformation(-0.25F, -0.2025F, -0.375F)),
                PartPose.offsetAndRotation(0F, -1.6675F, 0.58F, 0.9076F, 0F, 0F));
        PartDefinition p_kon_wing_right_feather_4 = p_kon_wing_right.addOrReplaceChild("kon_wing_right_feather_4", CubeListBuilder.create()
                .texOffs(170, 178).addBox(-0.5F, -1F, -0.245F, 1F, 2F, 6F, new CubeDeformation(-0.25F, -0.275F, -0.245F)),
                PartPose.offsetAndRotation(0F, -2.03F, 0.58F, 1.1868F, 0F, 0F));
        PartDefinition p_konterit_chest_body = root.addOrReplaceChild("konterit_chest_body", CubeListBuilder.create()
                .texOffs(249, 286).addBox(-5.2F, -1.1F, -3.65F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.3F, -0.15F))
                .texOffs(0, 290).addBox(3.2F, -1.1F, -3.65F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.3F, -0.15F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_kon_cape = p_konterit_chest_body.addOrReplaceChild("kon_cape", CubeListBuilder.create()
                .texOffs(75, 178).addBox(-4.5F, -0.2F, -0.2F, 9F, 7F, 1F, new CubeDeformation(0F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(0F, 0.2F, 3F, 0.1047F, 0F, 0F));
        PartDefinition p_kon_cape_hem = p_kon_cape.addOrReplaceChild("kon_cape_hem", CubeListBuilder.create()
                .texOffs(181, 187).addBox(-4.5F, -0.4F, -0.2F, 9F, 6F, 1F, new CubeDeformation(-0.2F, -0.4F, -0.2F)),
                PartPose.offsetAndRotation(0F, 6.4F, 0.05F, 0.1396F, 0F, 0F));
        PartDefinition p_konterit_chest_left_arm = root.addOrReplaceChild("konterit_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_kon_pauldron_left_arm = p_konterit_chest_left_arm.addOrReplaceChild("kon_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(155, 137).addBox(-3.2F, -1.55F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.2F, -0.25F, 0F))
                .texOffs(117, 178).addBox(-1F, -2.05F, -3.5F, 2F, 1F, 7F, new CubeDeformation(-0.4F, -0.15F, 0F))
                .texOffs(88, 286).addBox(1F, -2.2F, -1F, 2F, 1F, 2F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offsetAndRotation(1F, -3.2F, 0F, 0F, 0F, 0.2094F));
        PartDefinition p_konterit_chest_right_arm = root.addOrReplaceChild("konterit_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_kon_pauldron_right_arm = p_konterit_chest_right_arm.addOrReplaceChild("kon_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(184, 137).addBox(-3.8F, -1.55F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.2F, -0.25F, 0F))
                .texOffs(136, 178).addBox(-1F, -2.05F, -3.5F, 2F, 1F, 7F, new CubeDeformation(-0.4F, -0.15F, 0F))
                .texOffs(97, 286).addBox(-3F, -2.2F, -1F, 2F, 1F, 2F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offsetAndRotation(-1F, -3.2F, 0F, 0F, 0F, -0.2094F));
        PartDefinition p_konterit_legs_body = root.addOrReplaceChild("konterit_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_konterit_legs_left_leg = root.addOrReplaceChild("konterit_legs_left_leg", CubeListBuilder.create()
                .texOffs(226, 238).addBox(-1.5F, 4.2F, -3.1F, 3F, 4F, 1F, new CubeDeformation(0F, -0.4F, -0.1F))
                .texOffs(7, 290).addBox(-1F, 5.2F, -3.65F, 2F, 2F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_konterit_legs_right_leg = root.addOrReplaceChild("konterit_legs_right_leg", CubeListBuilder.create()
                .texOffs(235, 238).addBox(-1.5F, 4.2F, -3.1F, 3F, 4F, 1F, new CubeDeformation(0F, -0.4F, -0.1F))
                .texOffs(14, 290).addBox(-1F, 5.2F, -3.65F, 2F, 2F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_konterit_feet_left_leg = root.addOrReplaceChild("konterit_feet_left_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_kon_heel_left_leg = p_konterit_feet_left_leg.addOrReplaceChild("kon_heel_left_leg", CubeListBuilder.create()
                .texOffs(244, 238).addBox(-1F, -1.84F, -1.17F, 2F, 3F, 2F, new CubeDeformation(-0.4F, -0.48F, -0.15F)),
                PartPose.offsetAndRotation(2.9F, 9.6F, 0.2F, 0.4189F, 0.2443F, 0F));
        PartDefinition p_kon_heel_left_leg_feather_0 = p_kon_heel_left_leg.addOrReplaceChild("kon_heel_left_leg_feather_0", CubeListBuilder.create()
                .texOffs(13, 225).addBox(-0.5F, -1F, -0.045F, 1F, 2F, 4F, new CubeDeformation(-0.25F, -0.4475F, -0.045F)),
                PartPose.offsetAndRotation(0F, -0.34F, 0.34F, 0.0698F, 0F, 0F));
        PartDefinition p_kon_heel_left_leg_feather_1 = p_kon_heel_left_leg.addOrReplaceChild("kon_heel_left_leg_feather_1", CubeListBuilder.create()
                .texOffs(13, 196).addBox(-0.5F, -1F, -0.12F, 1F, 2F, 5F, new CubeDeformation(-0.25F, -0.4475F, -0.12F)),
                PartPose.offsetAndRotation(0F, -0.5525F, 0.34F, 0.3491F, 0F, 0F));
        PartDefinition p_kon_heel_left_leg_feather_2 = p_kon_heel_left_leg.addOrReplaceChild("kon_heel_left_leg_feather_2", CubeListBuilder.create()
                .texOffs(26, 196).addBox(-0.5F, -1F, -0.035F, 1F, 2F, 5F, new CubeDeformation(-0.25F, -0.49F, -0.035F)),
                PartPose.offsetAndRotation(0F, -0.765F, 0.34F, 0.6283F, 0F, 0F));
        PartDefinition p_kon_heel_left_leg_feather_3 = p_kon_heel_left_leg.addOrReplaceChild("kon_heel_left_leg_feather_3", CubeListBuilder.create()
                .texOffs(43, 211).addBox(-0.5F, -0.5F, -0.375F, 1F, 1F, 5F, new CubeDeformation(-0.25F, -0.0325F, -0.375F)),
                PartPose.offsetAndRotation(0F, -0.9775F, 0.34F, 0.9076F, 0F, 0F));
        PartDefinition p_kon_heel_left_leg_feather_4 = p_kon_heel_left_leg.addOrReplaceChild("kon_heel_left_leg_feather_4", CubeListBuilder.create()
                .texOffs(67, 238).addBox(-0.5F, -0.5F, -0.385F, 1F, 1F, 4F, new CubeDeformation(-0.25F, -0.075F, -0.385F)),
                PartPose.offsetAndRotation(0F, -1.19F, 0.34F, 1.1868F, 0F, 0F));
        PartDefinition p_konterit_feet_right_leg = root.addOrReplaceChild("konterit_feet_right_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_kon_heel_right_leg = p_konterit_feet_right_leg.addOrReplaceChild("kon_heel_right_leg", CubeListBuilder.create()
                .texOffs(0, 244).addBox(-1F, -1.84F, -1.17F, 2F, 3F, 2F, new CubeDeformation(-0.4F, -0.48F, -0.15F)),
                PartPose.offsetAndRotation(-2.9F, 9.6F, 0.2F, 0.4189F, -0.2443F, 0F));
        PartDefinition p_kon_heel_right_leg_feather_0 = p_kon_heel_right_leg.addOrReplaceChild("kon_heel_right_leg_feather_0", CubeListBuilder.create()
                .texOffs(24, 225).addBox(-0.5F, -1F, -0.045F, 1F, 2F, 4F, new CubeDeformation(-0.25F, -0.4475F, -0.045F)),
                PartPose.offsetAndRotation(0F, -0.34F, 0.34F, 0.0698F, 0F, 0F));
        PartDefinition p_kon_heel_right_leg_feather_1 = p_kon_heel_right_leg.addOrReplaceChild("kon_heel_right_leg_feather_1", CubeListBuilder.create()
                .texOffs(39, 196).addBox(-0.5F, -1F, -0.12F, 1F, 2F, 5F, new CubeDeformation(-0.25F, -0.4475F, -0.12F)),
                PartPose.offsetAndRotation(0F, -0.5525F, 0.34F, 0.3491F, 0F, 0F));
        PartDefinition p_kon_heel_right_leg_feather_2 = p_kon_heel_right_leg.addOrReplaceChild("kon_heel_right_leg_feather_2", CubeListBuilder.create()
                .texOffs(52, 196).addBox(-0.5F, -1F, -0.035F, 1F, 2F, 5F, new CubeDeformation(-0.25F, -0.49F, -0.035F)),
                PartPose.offsetAndRotation(0F, -0.765F, 0.34F, 0.6283F, 0F, 0F));
        PartDefinition p_kon_heel_right_leg_feather_3 = p_kon_heel_right_leg.addOrReplaceChild("kon_heel_right_leg_feather_3", CubeListBuilder.create()
                .texOffs(56, 211).addBox(-0.5F, -0.5F, -0.375F, 1F, 1F, 5F, new CubeDeformation(-0.25F, -0.0325F, -0.375F)),
                PartPose.offsetAndRotation(0F, -0.9775F, 0.34F, 0.9076F, 0F, 0F));
        PartDefinition p_kon_heel_right_leg_feather_4 = p_kon_heel_right_leg.addOrReplaceChild("kon_heel_right_leg_feather_4", CubeListBuilder.create()
                .texOffs(78, 238).addBox(-0.5F, -0.5F, -0.385F, 1F, 1F, 4F, new CubeDeformation(-0.25F, -0.075F, -0.385F)),
                PartPose.offsetAndRotation(0F, -1.19F, 0.34F, 1.1868F, 0F, 0F));
        PartDefinition p_nightsteel_head_head = root.addOrReplaceChild("nightsteel_head_head", CubeListBuilder.create()
                .texOffs(209, 281).addBox(-5.5F, -9.5F, -3.9F, 11F, 1F, 2F, new CubeDeformation(-0.2F, 0F, -0.4F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_night_crown_0_0 = p_nightsteel_head_head.addOrReplaceChild("night_crown_0_0", CubeListBuilder.create()
                .texOffs(66, 266).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.25F, -0.285F, -0.25F)),
                PartPose.offsetAndRotation(-3.6F, -9.3F, -2.9F, -0.1396F, 0F, -0.377F));
        PartDefinition p_night_crown_0_1 = p_night_crown_0_0.addOrReplaceChild("night_crown_0_1", CubeListBuilder.create()
                .texOffs(105, 298).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.0875F, -0.35F, -0.0875F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_night_crown_1_0 = p_nightsteel_head_head.addOrReplaceChild("night_crown_1_0", CubeListBuilder.create()
                .texOffs(9, 244).addBox(-1F, -2.545F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.25F, -0.455F, -0.25F)),
                PartPose.offsetAndRotation(-1.8F, -9.3F, -2.9F, -0.1396F, 0F, -0.1885F));
        PartDefinition p_night_crown_1_1 = p_night_crown_1_0.addOrReplaceChild("night_crown_1_1", CubeListBuilder.create()
                .texOffs(110, 298).addBox(-0.5F, -1.95F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.0875F, -0.05F, -0.0875F)),
                PartPose.offset(0F, -1.84F, 0F));
        PartDefinition p_night_crown_2_0 = p_nightsteel_head_head.addOrReplaceChild("night_crown_2_0", CubeListBuilder.create()
                .texOffs(18, 244).addBox(-1F, -2.93F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.1F, -0.07F, -0.1F)),
                PartPose.offsetAndRotation(0F, -9.3F, -2.9F, -0.1396F, 0F, 0F));
        PartDefinition p_night_crown_2_1 = p_night_crown_2_0.addOrReplaceChild("night_crown_2_1", CubeListBuilder.create()
                .texOffs(129, 281).addBox(-0.5F, -2.8F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.005F, -0.2F, -0.005F)),
                PartPose.offset(0F, -2.61F, 0F));
        PartDefinition p_night_crown_3_0 = p_nightsteel_head_head.addOrReplaceChild("night_crown_3_0", CubeListBuilder.create()
                .texOffs(27, 244).addBox(-1F, -2.545F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.25F, -0.455F, -0.25F)),
                PartPose.offsetAndRotation(1.8F, -9.3F, -2.9F, -0.1396F, 0F, 0.1885F));
        PartDefinition p_night_crown_3_1 = p_night_crown_3_0.addOrReplaceChild("night_crown_3_1", CubeListBuilder.create()
                .texOffs(115, 298).addBox(-0.5F, -1.95F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.0875F, -0.05F, -0.0875F)),
                PartPose.offset(0F, -1.84F, 0F));
        PartDefinition p_night_crown_4_0 = p_nightsteel_head_head.addOrReplaceChild("night_crown_4_0", CubeListBuilder.create()
                .texOffs(75, 266).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.25F, -0.285F, -0.25F)),
                PartPose.offsetAndRotation(3.6F, -9.3F, -2.9F, -0.1396F, 0F, 0.377F));
        PartDefinition p_night_crown_4_1 = p_night_crown_4_0.addOrReplaceChild("night_crown_4_1", CubeListBuilder.create()
                .texOffs(120, 298).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.0875F, -0.35F, -0.0875F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_nightsteel_chest_body = root.addOrReplaceChild("nightsteel_chest_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_night_back_0_0 = p_nightsteel_chest_body.addOrReplaceChild("night_back_0_0", CubeListBuilder.create()
                .texOffs(36, 244).addBox(-1F, -2.6F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.2F, -0.4F, -0.2F)),
                PartPose.offsetAndRotation(0F, 1.6F, 2.9F, -0.9076F, 0F, 0F));
        PartDefinition p_night_back_0_1 = p_night_back_0_0.addOrReplaceChild("night_back_0_1", CubeListBuilder.create()
                .texOffs(125, 298).addBox(-0.5F, -2F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.06F, 0F, -0.06F)),
                PartPose.offset(0F, -1.95F, 0F));
        PartDefinition p_night_back_1_0 = p_nightsteel_chest_body.addOrReplaceChild("night_back_1_0", CubeListBuilder.create()
                .texOffs(84, 266).addBox(-1F, -1.88F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.2F, -0.12F, -0.2F)),
                PartPose.offsetAndRotation(0F, 4.6F, 2.9F, -0.9076F, 0F, 0F));
        PartDefinition p_night_back_1_1 = p_night_back_1_0.addOrReplaceChild("night_back_1_1", CubeListBuilder.create()
                .texOffs(130, 298).addBox(-0.5F, -1.8F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.06F, -0.2F, -0.06F)),
                PartPose.offset(0F, -1.51F, 0F));
        PartDefinition p_night_back_2_0 = p_nightsteel_chest_body.addOrReplaceChild("night_back_2_0", CubeListBuilder.create()
                .texOffs(93, 266).addBox(-1F, -1.66F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.2F, -0.34F, -0.2F)),
                PartPose.offsetAndRotation(0F, 7.6F, 2.9F, -0.9076F, 0F, 0F));
        PartDefinition p_night_back_2_1 = p_night_back_2_0.addOrReplaceChild("night_back_2_1", CubeListBuilder.create()
                .texOffs(135, 298).addBox(-0.5F, -1.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.06F, -0.4F, -0.06F)),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_nightsteel_chest_left_arm = root.addOrReplaceChild("nightsteel_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_night_pauldron_left_arm = p_nightsteel_chest_left_arm.addOrReplaceChild("night_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(185, 59).addBox(-3.3F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.1745F));
        PartDefinition p_night_shoulder_left_arm_0_0 = p_night_pauldron_left_arm.addOrReplaceChild("night_shoulder_left_arm_0_0", CubeListBuilder.create()
                .texOffs(102, 266).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.285F, -0.3F)),
                PartPose.offsetAndRotation(2.2F, -1.4F, -2.2F, 0F, 0F, 0.4887F));
        PartDefinition p_night_shoulder_left_arm_0_1 = p_night_shoulder_left_arm_0_0.addOrReplaceChild("night_shoulder_left_arm_0_1", CubeListBuilder.create()
                .texOffs(140, 298).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.35F, -0.115F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_night_shoulder_left_arm_1_0 = p_night_pauldron_left_arm.addOrReplaceChild("night_shoulder_left_arm_1_0", CubeListBuilder.create()
                .texOffs(111, 266).addBox(-1F, -1.99F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.01F, -0.3F)),
                PartPose.offsetAndRotation(2.2F, -1.4F, 0F, 0F, 0F, 0.4887F));
        PartDefinition p_night_shoulder_left_arm_1_1 = p_night_shoulder_left_arm_1_0.addOrReplaceChild("night_shoulder_left_arm_1_1", CubeListBuilder.create()
                .texOffs(145, 298).addBox(-0.5F, -1.9F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.1F, -0.115F)),
                PartPose.offset(0F, -1.73F, 0F));
        PartDefinition p_night_shoulder_left_arm_2_0 = p_night_pauldron_left_arm.addOrReplaceChild("night_shoulder_left_arm_2_0", CubeListBuilder.create()
                .texOffs(120, 266).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.285F, -0.3F)),
                PartPose.offsetAndRotation(2.2F, -1.4F, 2.2F, 0F, 0F, 0.4887F));
        PartDefinition p_night_shoulder_left_arm_2_1 = p_night_shoulder_left_arm_2_0.addOrReplaceChild("night_shoulder_left_arm_2_1", CubeListBuilder.create()
                .texOffs(150, 298).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.35F, -0.115F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_nightsteel_chest_right_arm = root.addOrReplaceChild("nightsteel_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_night_pauldron_right_arm = p_nightsteel_chest_right_arm.addOrReplaceChild("night_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(216, 59).addBox(-3.7F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.1745F));
        PartDefinition p_night_shoulder_right_arm_0_0 = p_night_pauldron_right_arm.addOrReplaceChild("night_shoulder_right_arm_0_0", CubeListBuilder.create()
                .texOffs(129, 266).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.285F, -0.3F)),
                PartPose.offsetAndRotation(-2.2F, -1.4F, -2.2F, 0F, 0F, -0.4887F));
        PartDefinition p_night_shoulder_right_arm_0_1 = p_night_shoulder_right_arm_0_0.addOrReplaceChild("night_shoulder_right_arm_0_1", CubeListBuilder.create()
                .texOffs(155, 298).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.35F, -0.115F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_night_shoulder_right_arm_1_0 = p_night_pauldron_right_arm.addOrReplaceChild("night_shoulder_right_arm_1_0", CubeListBuilder.create()
                .texOffs(138, 266).addBox(-1F, -1.99F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.01F, -0.3F)),
                PartPose.offsetAndRotation(-2.2F, -1.4F, 0F, 0F, 0F, -0.4887F));
        PartDefinition p_night_shoulder_right_arm_1_1 = p_night_shoulder_right_arm_1_0.addOrReplaceChild("night_shoulder_right_arm_1_1", CubeListBuilder.create()
                .texOffs(160, 298).addBox(-0.5F, -1.9F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.1F, -0.115F)),
                PartPose.offset(0F, -1.73F, 0F));
        PartDefinition p_night_shoulder_right_arm_2_0 = p_night_pauldron_right_arm.addOrReplaceChild("night_shoulder_right_arm_2_0", CubeListBuilder.create()
                .texOffs(147, 266).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.285F, -0.3F)),
                PartPose.offsetAndRotation(-2.2F, -1.4F, 2.2F, 0F, 0F, -0.4887F));
        PartDefinition p_night_shoulder_right_arm_2_1 = p_night_shoulder_right_arm_2_0.addOrReplaceChild("night_shoulder_right_arm_2_1", CubeListBuilder.create()
                .texOffs(165, 298).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.35F, -0.115F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_nightsteel_legs_body = root.addOrReplaceChild("nightsteel_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_nightsteel_legs_left_leg = root.addOrReplaceChild("nightsteel_legs_left_leg", CubeListBuilder.create()
                .texOffs(22, 261).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(-0.1F, 0F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_night_knee_left_leg_0 = p_nightsteel_legs_left_leg.addOrReplaceChild("night_knee_left_leg_0", CubeListBuilder.create()
                .texOffs(156, 266).addBox(-1F, -1.77F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.23F, -0.35F)),
                PartPose.offsetAndRotation(0F, 6F, -3F, 1.0821F, 0F, 0F));
        PartDefinition p_night_knee_left_leg_1 = p_night_knee_left_leg_0.addOrReplaceChild("night_knee_left_leg_1", CubeListBuilder.create()
                .texOffs(170, 298).addBox(-0.5F, -1.7F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.3F, -0.1425F)),
                PartPose.offset(0F, -1.29F, 0F));
        PartDefinition p_nightsteel_legs_right_leg = root.addOrReplaceChild("nightsteel_legs_right_leg", CubeListBuilder.create()
                .texOffs(33, 261).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(-0.1F, 0F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_night_knee_right_leg_0 = p_nightsteel_legs_right_leg.addOrReplaceChild("night_knee_right_leg_0", CubeListBuilder.create()
                .texOffs(165, 266).addBox(-1F, -1.77F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.23F, -0.35F)),
                PartPose.offsetAndRotation(0F, 6F, -3F, 1.0821F, 0F, 0F));
        PartDefinition p_night_knee_right_leg_1 = p_night_knee_right_leg_0.addOrReplaceChild("night_knee_right_leg_1", CubeListBuilder.create()
                .texOffs(175, 298).addBox(-0.5F, -1.7F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.3F, -0.1425F)),
                PartPose.offset(0F, -1.29F, 0F));
        PartDefinition p_nightsteel_feet_left_leg = root.addOrReplaceChild("nightsteel_feet_left_leg", CubeListBuilder.create()
                .texOffs(47, 256).addBox(-2.5F, 10.25F, -3.9F, 5F, 3F, 1F, new CubeDeformation(-0.1F, -0.45F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_night_heel_left_leg_0 = p_nightsteel_feet_left_leg.addOrReplaceChild("night_heel_left_leg_0", CubeListBuilder.create()
                .texOffs(174, 266).addBox(-1F, -1.66F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.34F, -0.4F)),
                PartPose.offsetAndRotation(0F, 11F, 2.8F, -1.0123F, 0F, 0F));
        PartDefinition p_night_heel_left_leg_1 = p_night_heel_left_leg_0.addOrReplaceChild("night_heel_left_leg_1", CubeListBuilder.create()
                .texOffs(180, 298).addBox(-0.5F, -1.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.17F, -0.4F, -0.17F)),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_nightsteel_feet_right_leg = root.addOrReplaceChild("nightsteel_feet_right_leg", CubeListBuilder.create()
                .texOffs(60, 256).addBox(-2.5F, 10.25F, -3.9F, 5F, 3F, 1F, new CubeDeformation(-0.1F, -0.45F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_night_heel_right_leg_0 = p_nightsteel_feet_right_leg.addOrReplaceChild("night_heel_right_leg_0", CubeListBuilder.create()
                .texOffs(183, 266).addBox(-1F, -1.66F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.34F, -0.4F)),
                PartPose.offsetAndRotation(0F, 11F, 2.8F, -1.0123F, 0F, 0F));
        PartDefinition p_night_heel_right_leg_1 = p_night_heel_right_leg_0.addOrReplaceChild("night_heel_right_leg_1", CubeListBuilder.create()
                .texOffs(185, 298).addBox(-0.5F, -1.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.17F, -0.4F, -0.17F)),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_abyssite_head_head = root.addOrReplaceChild("abyssite_head_head", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_aby_horn_left_0 = p_abyssite_head_head.addOrReplaceChild("aby_horn_left_0", CubeListBuilder.create()
                .texOffs(69, 211).addBox(-1.5F, -3F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offsetAndRotation(4F, -8.6F, -1.2F, -0.2094F, 0F, 0.5934F));
        PartDefinition p_aby_horn_left_1 = p_aby_horn_left_0.addOrReplaceChild("aby_horn_left_1", CubeListBuilder.create()
                .texOffs(82, 211).addBox(-1.5F, -3F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.45F, 0F, -0.45F)),
                PartPose.offsetAndRotation(0F, -2.75F, 0F, -0.4887F, 0F, -0.1745F));
        PartDefinition p_aby_horn_left_2 = p_aby_horn_left_1.addOrReplaceChild("aby_horn_left_2", CubeListBuilder.create()
                .texOffs(45, 244).addBox(-1F, -2.9F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.2F, -0.1F, -0.2F)),
                PartPose.offsetAndRotation(0F, -2.75F, 0F, -0.5934F, 0F, -0.2094F));
        PartDefinition p_aby_horn_left_3 = p_aby_horn_left_2.addOrReplaceChild("aby_horn_left_3", CubeListBuilder.create()
                .texOffs(54, 244).addBox(-1F, -2.7F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.45F, -0.3F, -0.45F)),
                PartPose.offsetAndRotation(0F, -2.55F, 0F, -0.6283F, 0F, -0.1047F));
        PartDefinition p_aby_horn_left_4 = p_aby_horn_left_3.addOrReplaceChild("aby_horn_left_4", CubeListBuilder.create()
                .texOffs(190, 298).addBox(-0.5F, -1.9F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.15F, -0.1F, -0.15F)),
                PartPose.offsetAndRotation(0F, -2.15F, 0F, -0.5236F, 0F, 0F));
        PartDefinition p_aby_horn_right_0 = p_abyssite_head_head.addOrReplaceChild("aby_horn_right_0", CubeListBuilder.create()
                .texOffs(95, 211).addBox(-1.5F, -3F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offsetAndRotation(-4F, -8.6F, -1.2F, -0.2094F, 0F, -0.5934F));
        PartDefinition p_aby_horn_right_1 = p_aby_horn_right_0.addOrReplaceChild("aby_horn_right_1", CubeListBuilder.create()
                .texOffs(108, 211).addBox(-1.5F, -3F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.45F, 0F, -0.45F)),
                PartPose.offsetAndRotation(0F, -2.75F, 0F, -0.4887F, 0F, 0.1745F));
        PartDefinition p_aby_horn_right_2 = p_aby_horn_right_1.addOrReplaceChild("aby_horn_right_2", CubeListBuilder.create()
                .texOffs(63, 244).addBox(-1F, -2.9F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.2F, -0.1F, -0.2F)),
                PartPose.offsetAndRotation(0F, -2.75F, 0F, -0.5934F, 0F, 0.2094F));
        PartDefinition p_aby_horn_right_3 = p_aby_horn_right_2.addOrReplaceChild("aby_horn_right_3", CubeListBuilder.create()
                .texOffs(72, 244).addBox(-1F, -2.7F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.45F, -0.3F, -0.45F)),
                PartPose.offsetAndRotation(0F, -2.55F, 0F, -0.6283F, 0F, 0.1047F));
        PartDefinition p_aby_horn_right_4 = p_aby_horn_right_3.addOrReplaceChild("aby_horn_right_4", CubeListBuilder.create()
                .texOffs(195, 298).addBox(-0.5F, -1.9F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.15F, -0.1F, -0.15F)),
                PartPose.offsetAndRotation(0F, -2.15F, 0F, -0.5236F, 0F, 0F));
        PartDefinition p_aby_brow_0 = p_abyssite_head_head.addOrReplaceChild("aby_brow_0", CubeListBuilder.create()
                .texOffs(192, 266).addBox(-1F, -1.88F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.2F, -0.12F, -0.2F)),
                PartPose.offsetAndRotation(0F, -6.2F, -5.2F, -0.2443F, 0F, 0F));
        PartDefinition p_aby_brow_1 = p_aby_brow_0.addOrReplaceChild("aby_brow_1", CubeListBuilder.create()
                .texOffs(200, 298).addBox(-0.5F, -1.8F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.06F, -0.2F, -0.06F)),
                PartPose.offset(0F, -1.51F, 0F));
        PartDefinition p_abyssite_chest_body = root.addOrReplaceChild("abyssite_chest_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_aby_back_0_0 = p_abyssite_chest_body.addOrReplaceChild("aby_back_0_0", CubeListBuilder.create()
                .texOffs(81, 244).addBox(-1F, -2.82F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.1F, -0.18F, -0.1F)),
                PartPose.offsetAndRotation(0F, 3F, 2.9F, -0.8029F, 0F, 0F));
        PartDefinition p_aby_back_0_1 = p_aby_back_0_0.addOrReplaceChild("aby_back_0_1", CubeListBuilder.create()
                .texOffs(134, 281).addBox(-0.5F, -2.7F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.005F, -0.3F, -0.005F)),
                PartPose.offset(0F, -2.39F, 0F));
        PartDefinition p_aby_back_1_0 = p_abyssite_chest_body.addOrReplaceChild("aby_back_1_0", CubeListBuilder.create()
                .texOffs(201, 266).addBox(-1F, -1.935F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.065F, -0.35F)),
                PartPose.offsetAndRotation(1.7F, 3.8F, 2.9F, -0.6981F, 0F, 0.384F));
        PartDefinition p_aby_back_1_1 = p_aby_back_1_0.addOrReplaceChild("aby_back_1_1", CubeListBuilder.create()
                .texOffs(205, 298).addBox(-0.5F, -1.85F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.15F, -0.1425F)),
                PartPose.offset(0F, -1.62F, 0F));
        PartDefinition p_aby_back_2_0 = p_abyssite_chest_body.addOrReplaceChild("aby_back_2_0", CubeListBuilder.create()
                .texOffs(210, 266).addBox(-1F, -1.935F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.065F, -0.35F)),
                PartPose.offsetAndRotation(-1.7F, 3.8F, 2.9F, -0.6981F, 0F, -0.384F));
        PartDefinition p_aby_back_2_1 = p_aby_back_2_0.addOrReplaceChild("aby_back_2_1", CubeListBuilder.create()
                .texOffs(210, 298).addBox(-0.5F, -1.85F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.15F, -0.1425F)),
                PartPose.offset(0F, -1.62F, 0F));
        PartDefinition p_abyssite_chest_left_arm = root.addOrReplaceChild("abyssite_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_aby_pauldron_left_arm = p_abyssite_chest_left_arm.addOrReplaceChild("aby_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(148, 35).addBox(-3.8F, -2.3F, -4F, 8F, 3F, 8F, new CubeDeformation(-0.2F, -0.3F, -0.1F))
                .texOffs(0, 71).addBox(-2.5F, 0.3F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, 0F, 0F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.2793F));
        PartDefinition p_aby_shoulder_left_arm_0_0 = p_aby_pauldron_left_arm.addOrReplaceChild("aby_shoulder_left_arm_0_0", CubeListBuilder.create()
                .texOffs(219, 266).addBox(-1F, -1.99F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.25F, -0.01F, -0.25F)),
                PartPose.offsetAndRotation(1.4F, -1.9F, -1.4F, 0.1745F, 0F, 0.4189F));
        PartDefinition p_aby_shoulder_left_arm_0_1 = p_aby_shoulder_left_arm_0_0.addOrReplaceChild("aby_shoulder_left_arm_0_1", CubeListBuilder.create()
                .texOffs(215, 298).addBox(-0.5F, -1.9F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.0875F, -0.1F, -0.0875F)),
                PartPose.offset(0F, -1.73F, 0F));
        PartDefinition p_aby_shoulder_left_arm_1_0 = p_aby_pauldron_left_arm.addOrReplaceChild("aby_shoulder_left_arm_1_0", CubeListBuilder.create()
                .texOffs(228, 266).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.285F, -0.45F)),
                PartPose.offsetAndRotation(2.2F, -1.9F, 1.5F, -0.2094F, 0F, 0.6283F));
        PartDefinition p_aby_shoulder_left_arm_1_1 = p_aby_shoulder_left_arm_1_0.addOrReplaceChild("aby_shoulder_left_arm_1_1", CubeListBuilder.create()
                .texOffs(220, 298).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1975F, -0.35F, -0.1975F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_abyssite_chest_right_arm = root.addOrReplaceChild("abyssite_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_aby_pauldron_right_arm = p_abyssite_chest_right_arm.addOrReplaceChild("aby_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(181, 35).addBox(-4.2F, -2.3F, -4F, 8F, 3F, 8F, new CubeDeformation(-0.2F, -0.3F, -0.1F))
                .texOffs(31, 71).addBox(-4.5F, 0.3F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, 0F, 0F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.2793F));
        PartDefinition p_aby_shoulder_right_arm_0_0 = p_aby_pauldron_right_arm.addOrReplaceChild("aby_shoulder_right_arm_0_0", CubeListBuilder.create()
                .texOffs(237, 266).addBox(-1F, -1.99F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.25F, -0.01F, -0.25F)),
                PartPose.offsetAndRotation(-1.4F, -1.9F, -1.4F, 0.1745F, 0F, -0.4189F));
        PartDefinition p_aby_shoulder_right_arm_0_1 = p_aby_shoulder_right_arm_0_0.addOrReplaceChild("aby_shoulder_right_arm_0_1", CubeListBuilder.create()
                .texOffs(225, 298).addBox(-0.5F, -1.9F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.0875F, -0.1F, -0.0875F)),
                PartPose.offset(0F, -1.73F, 0F));
        PartDefinition p_aby_shoulder_right_arm_1_0 = p_aby_pauldron_right_arm.addOrReplaceChild("aby_shoulder_right_arm_1_0", CubeListBuilder.create()
                .texOffs(246, 266).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.285F, -0.45F)),
                PartPose.offsetAndRotation(-2.2F, -1.9F, 1.5F, -0.2094F, 0F, -0.6283F));
        PartDefinition p_aby_shoulder_right_arm_1_1 = p_aby_shoulder_right_arm_1_0.addOrReplaceChild("aby_shoulder_right_arm_1_1", CubeListBuilder.create()
                .texOffs(230, 298).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1975F, -0.35F, -0.1975F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_abyssite_legs_body = root.addOrReplaceChild("abyssite_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_abyssite_legs_left_leg = root.addOrReplaceChild("abyssite_legs_left_leg", CubeListBuilder.create()
                .texOffs(196, 204).addBox(-3F, -0.9F, -3.3F, 6F, 5F, 1F, new CubeDeformation(-0.3F, -0.4F, 0F))
                .texOffs(59, 281).addBox(-1F, 0.1F, -4F, 2F, 3F, 1F, new CubeDeformation(-0.3F, -0.3F, -0.2F))
                .texOffs(44, 261).addBox(-2F, 4.9F, -3.3F, 4F, 3F, 1F, new CubeDeformation(0F, -0.1F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_abyssite_legs_right_leg = root.addOrReplaceChild("abyssite_legs_right_leg", CubeListBuilder.create()
                .texOffs(211, 204).addBox(-3F, -0.9F, -3.3F, 6F, 5F, 1F, new CubeDeformation(-0.3F, -0.4F, 0F))
                .texOffs(66, 281).addBox(-1F, 0.1F, -4F, 2F, 3F, 1F, new CubeDeformation(-0.3F, -0.3F, -0.2F))
                .texOffs(55, 261).addBox(-2F, 4.9F, -3.3F, 4F, 3F, 1F, new CubeDeformation(0F, -0.1F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_abyssite_feet_left_leg = root.addOrReplaceChild("abyssite_feet_left_leg", CubeListBuilder.create()
                .texOffs(74, 232).addBox(-2.5F, 10.25F, -4.45F, 5F, 3F, 2F, new CubeDeformation(0F, -0.45F, -0.45F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_aby_heel_left_leg_0_0 = p_abyssite_feet_left_leg.addOrReplaceChild("aby_heel_left_leg_0_0", CubeListBuilder.create()
                .texOffs(0, 271).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.285F, -0.45F)),
                PartPose.offsetAndRotation(0.9F, 10.8F, 2.8F, -0.9774F, 0F, 0.2094F));
        PartDefinition p_aby_heel_left_leg_0_1 = p_aby_heel_left_leg_0_0.addOrReplaceChild("aby_heel_left_leg_0_1", CubeListBuilder.create()
                .texOffs(235, 298).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1975F, -0.35F, -0.1975F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_aby_heel_left_leg_1_0 = p_abyssite_feet_left_leg.addOrReplaceChild("aby_heel_left_leg_1_0", CubeListBuilder.create()
                .texOffs(240, 298).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.05F, -0.45F, -0.05F)),
                PartPose.offsetAndRotation(-0.9F, 10.8F, 2.8F, -0.9774F, 0F, -0.2094F));
        PartDefinition p_aby_heel_left_leg_1_1 = p_aby_heel_left_leg_1_0.addOrReplaceChild("aby_heel_left_leg_1_1", CubeListBuilder.create()
                .texOffs(154, 310).addBox(-0.5F, -1F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.2525F, 0F, -0.2525F)),
                PartPose.offset(0F, -0.85F, 0F));
        PartDefinition p_abyssite_feet_right_leg = root.addOrReplaceChild("abyssite_feet_right_leg", CubeListBuilder.create()
                .texOffs(89, 232).addBox(-2.5F, 10.25F, -4.45F, 5F, 3F, 2F, new CubeDeformation(0F, -0.45F, -0.45F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_aby_heel_right_leg_0_0 = p_abyssite_feet_right_leg.addOrReplaceChild("aby_heel_right_leg_0_0", CubeListBuilder.create()
                .texOffs(9, 271).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.285F, -0.45F)),
                PartPose.offsetAndRotation(0.9F, 10.8F, 2.8F, -0.9774F, 0F, 0.2094F));
        PartDefinition p_aby_heel_right_leg_0_1 = p_aby_heel_right_leg_0_0.addOrReplaceChild("aby_heel_right_leg_0_1", CubeListBuilder.create()
                .texOffs(245, 298).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1975F, -0.35F, -0.1975F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_aby_heel_right_leg_1_0 = p_abyssite_feet_right_leg.addOrReplaceChild("aby_heel_right_leg_1_0", CubeListBuilder.create()
                .texOffs(250, 298).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.05F, -0.45F, -0.05F)),
                PartPose.offsetAndRotation(-0.9F, 10.8F, 2.8F, -0.9774F, 0F, -0.2094F));
        PartDefinition p_aby_heel_right_leg_1_1 = p_aby_heel_right_leg_1_0.addOrReplaceChild("aby_heel_right_leg_1_1", CubeListBuilder.create()
                .texOffs(159, 310).addBox(-0.5F, -1F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.2525F, 0F, -0.2525F)),
                PartPose.offset(0F, -0.85F, 0F));
        PartDefinition p_fire_head_head = root.addOrReplaceChild("fire_head_head", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_fire_horn_left_0 = p_fire_head_head.addOrReplaceChild("fire_horn_left_0", CubeListBuilder.create()
                .texOffs(121, 211).addBox(-1.5F, -2.9F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.1F, -0.4F)),
                PartPose.offsetAndRotation(3.2F, -8.7F, -1.6F, -0.4189F, 0F, 0.384F));
        PartDefinition p_fire_horn_left_1 = p_fire_horn_left_0.addOrReplaceChild("fire_horn_left_1", CubeListBuilder.create()
                .texOffs(90, 244).addBox(-1F, -2.9F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.1F, -0.1F, -0.1F)),
                PartPose.offsetAndRotation(0F, -2.55F, 0F, -0.5934F, 0F, -0.1047F));
        PartDefinition p_fire_horn_left_2 = p_fire_horn_left_1.addOrReplaceChild("fire_horn_left_2", CubeListBuilder.create()
                .texOffs(99, 244).addBox(-1F, -2.8F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.35F, -0.2F, -0.35F)),
                PartPose.offsetAndRotation(0F, -2.55F, 0F, -0.6632F, 0F, -0.1047F));
        PartDefinition p_fire_horn_left_3 = p_fire_horn_left_2.addOrReplaceChild("fire_horn_left_3", CubeListBuilder.create()
                .texOffs(139, 281).addBox(-0.5F, -2.6F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.1F)),
                PartPose.offsetAndRotation(0F, -2.35F, 0F, -0.5934F, 0F, 0F));
        PartDefinition p_fire_horn_right_0 = p_fire_head_head.addOrReplaceChild("fire_horn_right_0", CubeListBuilder.create()
                .texOffs(134, 211).addBox(-1.5F, -2.9F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.1F, -0.4F)),
                PartPose.offsetAndRotation(-3.2F, -8.7F, -1.6F, -0.4189F, 0F, -0.384F));
        PartDefinition p_fire_horn_right_1 = p_fire_horn_right_0.addOrReplaceChild("fire_horn_right_1", CubeListBuilder.create()
                .texOffs(108, 244).addBox(-1F, -2.9F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.1F, -0.1F, -0.1F)),
                PartPose.offsetAndRotation(0F, -2.55F, 0F, -0.5934F, 0F, 0.1047F));
        PartDefinition p_fire_horn_right_2 = p_fire_horn_right_1.addOrReplaceChild("fire_horn_right_2", CubeListBuilder.create()
                .texOffs(117, 244).addBox(-1F, -2.8F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.35F, -0.2F, -0.35F)),
                PartPose.offsetAndRotation(0F, -2.55F, 0F, -0.6632F, 0F, 0.1047F));
        PartDefinition p_fire_horn_right_3 = p_fire_horn_right_2.addOrReplaceChild("fire_horn_right_3", CubeListBuilder.create()
                .texOffs(144, 281).addBox(-0.5F, -2.6F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.1F)),
                PartPose.offsetAndRotation(0F, -2.35F, 0F, -0.5934F, 0F, 0F));
        PartDefinition p_fire_crest_0_0 = p_fire_head_head.addOrReplaceChild("fire_crest_0_0", CubeListBuilder.create()
                .texOffs(164, 310).addBox(-0.5F, -0.94F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.06F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, -3.2F, -0.4538F, 0F, 0F));
        PartDefinition p_fire_crest_0_1 = p_fire_crest_0_0.addOrReplaceChild("fire_crest_0_1", CubeListBuilder.create()
                .texOffs(169, 310).addBox(-0.5F, -0.9F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.1F, -0.225F)),
                PartPose.offset(0F, -0.63F, 0F));
        PartDefinition p_fire_crest_1_0 = p_fire_head_head.addOrReplaceChild("fire_crest_1_0", CubeListBuilder.create()
                .texOffs(0, 302).addBox(-0.5F, -1.605F, -0.5F, 1F, 2F, 1F, new CubeDeformation(0F, -0.395F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, -0.6F, -0.4538F, 0F, 0F));
        PartDefinition p_fire_crest_1_1 = p_fire_crest_1_0.addOrReplaceChild("fire_crest_1_1", CubeListBuilder.create()
                .texOffs(5, 302).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.225F, -0.45F, -0.225F)),
                PartPose.offset(0F, -0.96F, 0F));
        PartDefinition p_fire_crest_2_0 = p_fire_head_head.addOrReplaceChild("fire_crest_2_0", CubeListBuilder.create()
                .texOffs(174, 310).addBox(-0.5F, -0.995F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.005F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, 2F, -0.4538F, 0F, 0F));
        PartDefinition p_fire_crest_2_1 = p_fire_crest_2_0.addOrReplaceChild("fire_crest_2_1", CubeListBuilder.create()
                .texOffs(179, 310).addBox(-0.5F, -0.95F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.05F, -0.225F)),
                PartPose.offset(0F, -0.74F, 0F));
        PartDefinition p_fire_chest_body = root.addOrReplaceChild("fire_chest_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_fire_back_0_0 = p_fire_chest_body.addOrReplaceChild("fire_back_0_0", CubeListBuilder.create()
                .texOffs(18, 271).addBox(-1F, -1.935F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.065F, -0.3F)),
                PartPose.offsetAndRotation(0F, 0.8F, 2.9F, -0.8727F, 0F, 0F));
        PartDefinition p_fire_back_0_1 = p_fire_back_0_0.addOrReplaceChild("fire_back_0_1", CubeListBuilder.create()
                .texOffs(10, 302).addBox(-0.5F, -1.85F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.15F, -0.115F)),
                PartPose.offset(0F, -1.62F, 0F));
        PartDefinition p_fire_back_1_0 = p_fire_chest_body.addOrReplaceChild("fire_back_1_0", CubeListBuilder.create()
                .texOffs(126, 244).addBox(-1F, -2.545F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.3F, -0.455F, -0.3F)),
                PartPose.offsetAndRotation(0F, 3.6F, 2.9F, -0.8727F, 0F, 0F));
        PartDefinition p_fire_back_1_1 = p_fire_back_1_0.addOrReplaceChild("fire_back_1_1", CubeListBuilder.create()
                .texOffs(15, 302).addBox(-0.5F, -1.95F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.05F, -0.115F)),
                PartPose.offset(0F, -1.84F, 0F));
        PartDefinition p_fire_back_2_0 = p_fire_chest_body.addOrReplaceChild("fire_back_2_0", CubeListBuilder.create()
                .texOffs(27, 271).addBox(-1F, -1.88F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.12F, -0.3F)),
                PartPose.offsetAndRotation(0F, 6.6F, 2.9F, -0.8727F, 0F, 0F));
        PartDefinition p_fire_back_2_1 = p_fire_back_2_0.addOrReplaceChild("fire_back_2_1", CubeListBuilder.create()
                .texOffs(20, 302).addBox(-0.5F, -1.8F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.2F, -0.115F)),
                PartPose.offset(0F, -1.51F, 0F));
        PartDefinition p_fire_back_3_0 = p_fire_chest_body.addOrReplaceChild("fire_back_3_0", CubeListBuilder.create()
                .texOffs(36, 271).addBox(-1F, -1.66F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.34F, -0.3F)),
                PartPose.offsetAndRotation(0F, 9.4F, 2.9F, -0.8727F, 0F, 0F));
        PartDefinition p_fire_back_3_1 = p_fire_back_3_0.addOrReplaceChild("fire_back_3_1", CubeListBuilder.create()
                .texOffs(25, 302).addBox(-0.5F, -1.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.4F, -0.115F)),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_fire_chest_left_arm = root.addOrReplaceChild("fire_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_fire_pauldron_left_arm = p_fire_chest_left_arm.addOrReplaceChild("fire_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(62, 71).addBox(-3.3F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F))
                .texOffs(58, 104).addBox(-2F, -0.1F, -4F, 6F, 2F, 8F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.2094F));
        PartDefinition p_fire_claw_left_arm_0 = p_fire_pauldron_left_arm.addOrReplaceChild("fire_claw_left_arm_0", CubeListBuilder.create()
                .texOffs(45, 271).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.285F, -0.35F)),
                PartPose.offsetAndRotation(2.4F, -1.4F, 0F, 0F, 0F, 0.6981F));
        PartDefinition p_fire_claw_left_arm_1 = p_fire_claw_left_arm_0.addOrReplaceChild("fire_claw_left_arm_1", CubeListBuilder.create()
                .texOffs(30, 302).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.35F, -0.1425F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_fire_chest_right_arm = root.addOrReplaceChild("fire_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_fire_pauldron_right_arm = p_fire_chest_right_arm.addOrReplaceChild("fire_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(93, 71).addBox(-3.7F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F))
                .texOffs(87, 104).addBox(-4F, -0.1F, -4F, 6F, 2F, 8F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.2094F));
        PartDefinition p_fire_claw_right_arm_0 = p_fire_pauldron_right_arm.addOrReplaceChild("fire_claw_right_arm_0", CubeListBuilder.create()
                .texOffs(54, 271).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.285F, -0.35F)),
                PartPose.offsetAndRotation(-2.4F, -1.4F, 0F, 0F, 0F, -0.6981F));
        PartDefinition p_fire_claw_right_arm_1 = p_fire_claw_right_arm_0.addOrReplaceChild("fire_claw_right_arm_1", CubeListBuilder.create()
                .texOffs(35, 302).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.35F, -0.1425F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_fire_legs_body = root.addOrReplaceChild("fire_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_fire_tail_0 = p_fire_legs_body.addOrReplaceChild("fire_tail_0", CubeListBuilder.create()
                .texOffs(147, 211).addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F),
                PartPose.offsetAndRotation(0F, 10.2F, 2.2F, 0.5236F, 0F, 0F));
        PartDefinition p_fire_tail_1 = p_fire_tail_0.addOrReplaceChild("fire_tail_1", CubeListBuilder.create()
                .texOffs(160, 211).addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, 2.75F, 0F, 0.1745F, 0F, 0F));
        PartDefinition p_fire_tail_2 = p_fire_tail_1.addOrReplaceChild("fire_tail_2", CubeListBuilder.create()
                .texOffs(173, 211).addBox(-1.5F, -0.1F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.1F, -0.4F)),
                PartPose.offsetAndRotation(0F, 2.75F, 0F, 0.2094F, 0F, 0F));
        PartDefinition p_fire_tail_3 = p_fire_tail_2.addOrReplaceChild("fire_tail_3", CubeListBuilder.create()
                .texOffs(135, 244).addBox(-1F, -0.2F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.15F, -0.2F, -0.15F)),
                PartPose.offsetAndRotation(0F, 2.55F, 0F, 0.2094F, 0F, 0F));
        PartDefinition p_fire_tail_4 = p_fire_tail_3.addOrReplaceChild("fire_tail_4", CubeListBuilder.create()
                .texOffs(144, 244).addBox(-1F, -0.4F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.4F, -0.4F, -0.4F)),
                PartPose.offsetAndRotation(0F, 2.35F, 0F, 0.1745F, 0F, 0F));
        PartDefinition p_fire_tail_tip = p_fire_tail_4.addOrReplaceChild("fire_tail_tip", CubeListBuilder.create()
                .texOffs(89, 238).addBox(-1.5F, -0.3F, -1F, 3F, 3F, 2F, new CubeDeformation(-0.3F, -0.3F, -0.3F))
                .texOffs(21, 290).addBox(-1F, 2.2F, -0.5F, 2F, 2F, 1F, new CubeDeformation(-0.3F, 0F, -0.1F))
                .texOffs(40, 302).addBox(-0.5F, 3.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.2F, -0.4F, -0.3F)),
                PartPose.offsetAndRotation(0F, 2.4F, 0F, 0.1396F, 0F, 0F));
        PartDefinition p_fire_legs_left_leg = root.addOrReplaceChild("fire_legs_left_leg", CubeListBuilder.create()
                .texOffs(66, 261).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_fire_legs_right_leg = root.addOrReplaceChild("fire_legs_right_leg", CubeListBuilder.create()
                .texOffs(77, 261).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_fire_feet_left_leg = root.addOrReplaceChild("fire_feet_left_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_fire_talon_left_leg_0 = p_fire_feet_left_leg.addOrReplaceChild("fire_talon_left_leg_0", CubeListBuilder.create()
                .texOffs(28, 290).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(-1.6F, 11.6F, -2.8F, 0.4887F, -0.2234F, 0F));
        PartDefinition p_fire_talon_left_leg_1 = p_fire_feet_left_leg.addOrReplaceChild("fire_talon_left_leg_1", CubeListBuilder.create()
                .texOffs(35, 290).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(0F, 11.6F, -2.8F, 0.4887F, 0F, 0F));
        PartDefinition p_fire_talon_left_leg_2 = p_fire_feet_left_leg.addOrReplaceChild("fire_talon_left_leg_2", CubeListBuilder.create()
                .texOffs(42, 290).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(1.6F, 11.6F, -2.8F, 0.4887F, 0.2234F, 0F));
        PartDefinition p_fire_feet_right_leg = root.addOrReplaceChild("fire_feet_right_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_fire_talon_right_leg_0 = p_fire_feet_right_leg.addOrReplaceChild("fire_talon_right_leg_0", CubeListBuilder.create()
                .texOffs(49, 290).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(-1.6F, 11.6F, -2.8F, 0.4887F, -0.2234F, 0F));
        PartDefinition p_fire_talon_right_leg_1 = p_fire_feet_right_leg.addOrReplaceChild("fire_talon_right_leg_1", CubeListBuilder.create()
                .texOffs(56, 290).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(0F, 11.6F, -2.8F, 0.4887F, 0F, 0F));
        PartDefinition p_fire_talon_right_leg_2 = p_fire_feet_right_leg.addOrReplaceChild("fire_talon_right_leg_2", CubeListBuilder.create()
                .texOffs(63, 290).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(1.6F, 11.6F, -2.8F, 0.4887F, 0.2234F, 0F));
        PartDefinition p_ice_head_head = root.addOrReplaceChild("ice_head_head", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_ice_horn_left_0 = p_ice_head_head.addOrReplaceChild("ice_horn_left_0", CubeListBuilder.create()
                .texOffs(121, 225).addBox(-1F, -3.8F, -1F, 2F, 4F, 2F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offsetAndRotation(3F, -8.7F, -1F, -0.4887F, 0F, 0.2793F));
        PartDefinition p_ice_horn_left_1 = p_ice_horn_left_0.addOrReplaceChild("ice_horn_left_1", CubeListBuilder.create()
                .texOffs(130, 225).addBox(-1F, -3.7F, -1F, 2F, 4F, 2F, new CubeDeformation(-0.4F, -0.3F, -0.4F)),
                PartPose.offsetAndRotation(0F, -3.35F, 0F, -0.1047F, 0F, 0F));
        PartDefinition p_ice_side_left_0 = p_ice_head_head.addOrReplaceChild("ice_side_left_0", CubeListBuilder.create()
                .texOffs(63, 271).addBox(-1F, -1.66F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.34F, -0.45F)),
                PartPose.offsetAndRotation(4.4F, -7.4F, 0.6F, -0.6283F, 0F, 0.6981F));
        PartDefinition p_ice_side_left_1 = p_ice_side_left_0.addOrReplaceChild("ice_side_left_1", CubeListBuilder.create()
                .texOffs(45, 302).addBox(-0.5F, -1.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1975F, -0.4F, -0.1975F)),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_ice_horn_right_0 = p_ice_head_head.addOrReplaceChild("ice_horn_right_0", CubeListBuilder.create()
                .texOffs(139, 225).addBox(-1F, -3.8F, -1F, 2F, 4F, 2F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offsetAndRotation(-3F, -8.7F, -1F, -0.4887F, 0F, -0.2793F));
        PartDefinition p_ice_horn_right_1 = p_ice_horn_right_0.addOrReplaceChild("ice_horn_right_1", CubeListBuilder.create()
                .texOffs(148, 225).addBox(-1F, -3.7F, -1F, 2F, 4F, 2F, new CubeDeformation(-0.4F, -0.3F, -0.4F)),
                PartPose.offsetAndRotation(0F, -3.35F, 0F, -0.1047F, 0F, 0F));
        PartDefinition p_ice_side_right_0 = p_ice_head_head.addOrReplaceChild("ice_side_right_0", CubeListBuilder.create()
                .texOffs(72, 271).addBox(-1F, -1.66F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.34F, -0.45F)),
                PartPose.offsetAndRotation(-4.4F, -7.4F, 0.6F, -0.6283F, 0F, -0.6981F));
        PartDefinition p_ice_side_right_1 = p_ice_side_right_0.addOrReplaceChild("ice_side_right_1", CubeListBuilder.create()
                .texOffs(50, 302).addBox(-0.5F, -1.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1975F, -0.4F, -0.1975F)),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_ice_crest_0_0 = p_ice_head_head.addOrReplaceChild("ice_crest_0_0", CubeListBuilder.create()
                .texOffs(184, 310).addBox(-0.5F, -0.94F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.06F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, -3.2F, -0.4538F, 0F, 0F));
        PartDefinition p_ice_crest_0_1 = p_ice_crest_0_0.addOrReplaceChild("ice_crest_0_1", CubeListBuilder.create()
                .texOffs(189, 310).addBox(-0.5F, -0.9F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.1F, -0.225F)),
                PartPose.offset(0F, -0.63F, 0F));
        PartDefinition p_ice_crest_1_0 = p_ice_head_head.addOrReplaceChild("ice_crest_1_0", CubeListBuilder.create()
                .texOffs(55, 302).addBox(-0.5F, -1.605F, -0.5F, 1F, 2F, 1F, new CubeDeformation(0F, -0.395F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, -0.6F, -0.4538F, 0F, 0F));
        PartDefinition p_ice_crest_1_1 = p_ice_crest_1_0.addOrReplaceChild("ice_crest_1_1", CubeListBuilder.create()
                .texOffs(60, 302).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.225F, -0.45F, -0.225F)),
                PartPose.offset(0F, -0.96F, 0F));
        PartDefinition p_ice_crest_2_0 = p_ice_head_head.addOrReplaceChild("ice_crest_2_0", CubeListBuilder.create()
                .texOffs(194, 310).addBox(-0.5F, -0.995F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.005F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, 2F, -0.4538F, 0F, 0F));
        PartDefinition p_ice_crest_2_1 = p_ice_crest_2_0.addOrReplaceChild("ice_crest_2_1", CubeListBuilder.create()
                .texOffs(199, 310).addBox(-0.5F, -0.95F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.05F, -0.225F)),
                PartPose.offset(0F, -0.74F, 0F));
        PartDefinition p_ice_chest_body = root.addOrReplaceChild("ice_chest_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_ice_back_0_0 = p_ice_chest_body.addOrReplaceChild("ice_back_0_0", CubeListBuilder.create()
                .texOffs(81, 271).addBox(-1F, -1.935F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.065F, -0.3F)),
                PartPose.offsetAndRotation(0F, 0.8F, 2.9F, -0.8727F, 0F, 0F));
        PartDefinition p_ice_back_0_1 = p_ice_back_0_0.addOrReplaceChild("ice_back_0_1", CubeListBuilder.create()
                .texOffs(65, 302).addBox(-0.5F, -1.85F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.15F, -0.115F)),
                PartPose.offset(0F, -1.62F, 0F));
        PartDefinition p_ice_back_1_0 = p_ice_chest_body.addOrReplaceChild("ice_back_1_0", CubeListBuilder.create()
                .texOffs(153, 244).addBox(-1F, -2.545F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.3F, -0.455F, -0.3F)),
                PartPose.offsetAndRotation(0F, 3.6F, 2.9F, -0.8727F, 0F, 0F));
        PartDefinition p_ice_back_1_1 = p_ice_back_1_0.addOrReplaceChild("ice_back_1_1", CubeListBuilder.create()
                .texOffs(70, 302).addBox(-0.5F, -1.95F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.05F, -0.115F)),
                PartPose.offset(0F, -1.84F, 0F));
        PartDefinition p_ice_back_2_0 = p_ice_chest_body.addOrReplaceChild("ice_back_2_0", CubeListBuilder.create()
                .texOffs(90, 271).addBox(-1F, -1.88F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.12F, -0.3F)),
                PartPose.offsetAndRotation(0F, 6.6F, 2.9F, -0.8727F, 0F, 0F));
        PartDefinition p_ice_back_2_1 = p_ice_back_2_0.addOrReplaceChild("ice_back_2_1", CubeListBuilder.create()
                .texOffs(75, 302).addBox(-0.5F, -1.8F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.2F, -0.115F)),
                PartPose.offset(0F, -1.51F, 0F));
        PartDefinition p_ice_back_3_0 = p_ice_chest_body.addOrReplaceChild("ice_back_3_0", CubeListBuilder.create()
                .texOffs(99, 271).addBox(-1F, -1.66F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.34F, -0.3F)),
                PartPose.offsetAndRotation(0F, 9.4F, 2.9F, -0.8727F, 0F, 0F));
        PartDefinition p_ice_back_3_1 = p_ice_back_3_0.addOrReplaceChild("ice_back_3_1", CubeListBuilder.create()
                .texOffs(80, 302).addBox(-0.5F, -1.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.4F, -0.115F)),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_ice_chest_left_arm = root.addOrReplaceChild("ice_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_ice_pauldron_left_arm = p_ice_chest_left_arm.addOrReplaceChild("ice_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(124, 71).addBox(-3.3F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F))
                .texOffs(116, 104).addBox(-2F, -0.1F, -4F, 6F, 2F, 8F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.2094F));
        PartDefinition p_ice_claw_left_arm_0 = p_ice_pauldron_left_arm.addOrReplaceChild("ice_claw_left_arm_0", CubeListBuilder.create()
                .texOffs(108, 271).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.285F, -0.35F)),
                PartPose.offsetAndRotation(2.4F, -1.4F, 0F, 0F, 0F, 0.6981F));
        PartDefinition p_ice_claw_left_arm_1 = p_ice_claw_left_arm_0.addOrReplaceChild("ice_claw_left_arm_1", CubeListBuilder.create()
                .texOffs(85, 302).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.35F, -0.1425F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_ice_chest_right_arm = root.addOrReplaceChild("ice_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_ice_pauldron_right_arm = p_ice_chest_right_arm.addOrReplaceChild("ice_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(155, 71).addBox(-3.7F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F))
                .texOffs(145, 104).addBox(-4F, -0.1F, -4F, 6F, 2F, 8F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.2094F));
        PartDefinition p_ice_claw_right_arm_0 = p_ice_pauldron_right_arm.addOrReplaceChild("ice_claw_right_arm_0", CubeListBuilder.create()
                .texOffs(117, 271).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.285F, -0.35F)),
                PartPose.offsetAndRotation(-2.4F, -1.4F, 0F, 0F, 0F, -0.6981F));
        PartDefinition p_ice_claw_right_arm_1 = p_ice_claw_right_arm_0.addOrReplaceChild("ice_claw_right_arm_1", CubeListBuilder.create()
                .texOffs(90, 302).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.35F, -0.1425F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_ice_legs_body = root.addOrReplaceChild("ice_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_ice_tail_0 = p_ice_legs_body.addOrReplaceChild("ice_tail_0", CubeListBuilder.create()
                .texOffs(186, 211).addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F),
                PartPose.offsetAndRotation(0F, 10.2F, 2.2F, 0.5236F, 0F, 0F));
        PartDefinition p_ice_tail_1 = p_ice_tail_0.addOrReplaceChild("ice_tail_1", CubeListBuilder.create()
                .texOffs(199, 211).addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, 2.75F, 0F, 0.1745F, 0F, 0F));
        PartDefinition p_ice_tail_2 = p_ice_tail_1.addOrReplaceChild("ice_tail_2", CubeListBuilder.create()
                .texOffs(212, 211).addBox(-1.5F, -0.1F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.1F, -0.4F)),
                PartPose.offsetAndRotation(0F, 2.75F, 0F, 0.2094F, 0F, 0F));
        PartDefinition p_ice_tail_3 = p_ice_tail_2.addOrReplaceChild("ice_tail_3", CubeListBuilder.create()
                .texOffs(162, 244).addBox(-1F, -0.2F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.15F, -0.2F, -0.15F)),
                PartPose.offsetAndRotation(0F, 2.55F, 0F, 0.2094F, 0F, 0F));
        PartDefinition p_ice_tail_4 = p_ice_tail_3.addOrReplaceChild("ice_tail_4", CubeListBuilder.create()
                .texOffs(171, 244).addBox(-1F, -0.4F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.4F, -0.4F, -0.4F)),
                PartPose.offsetAndRotation(0F, 2.35F, 0F, 0.1745F, 0F, 0F));
        PartDefinition p_ice_tail_tip = p_ice_tail_4.addOrReplaceChild("ice_tail_tip", CubeListBuilder.create()
                .texOffs(180, 244).addBox(-1F, -0.3F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.1F, -0.3F, -0.1F))
                .texOffs(149, 281).addBox(-0.5F, 1.8F, -0.5F, 1F, 3F, 1F, new CubeDeformation(0F, -0.4F, 0F)),
                PartPose.offsetAndRotation(0F, 2.4F, 0F, 0.1396F, 0F, 0F));
        PartDefinition p_ice_legs_left_leg = root.addOrReplaceChild("ice_legs_left_leg", CubeListBuilder.create()
                .texOffs(88, 261).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_ice_legs_right_leg = root.addOrReplaceChild("ice_legs_right_leg", CubeListBuilder.create()
                .texOffs(99, 261).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_ice_feet_left_leg = root.addOrReplaceChild("ice_feet_left_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_ice_talon_left_leg_0 = p_ice_feet_left_leg.addOrReplaceChild("ice_talon_left_leg_0", CubeListBuilder.create()
                .texOffs(70, 290).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(-1.6F, 11.6F, -2.8F, 0.4887F, -0.2234F, 0F));
        PartDefinition p_ice_talon_left_leg_1 = p_ice_feet_left_leg.addOrReplaceChild("ice_talon_left_leg_1", CubeListBuilder.create()
                .texOffs(77, 290).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(0F, 11.6F, -2.8F, 0.4887F, 0F, 0F));
        PartDefinition p_ice_talon_left_leg_2 = p_ice_feet_left_leg.addOrReplaceChild("ice_talon_left_leg_2", CubeListBuilder.create()
                .texOffs(84, 290).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(1.6F, 11.6F, -2.8F, 0.4887F, 0.2234F, 0F));
        PartDefinition p_ice_feet_right_leg = root.addOrReplaceChild("ice_feet_right_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_ice_talon_right_leg_0 = p_ice_feet_right_leg.addOrReplaceChild("ice_talon_right_leg_0", CubeListBuilder.create()
                .texOffs(91, 290).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(-1.6F, 11.6F, -2.8F, 0.4887F, -0.2234F, 0F));
        PartDefinition p_ice_talon_right_leg_1 = p_ice_feet_right_leg.addOrReplaceChild("ice_talon_right_leg_1", CubeListBuilder.create()
                .texOffs(98, 290).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(0F, 11.6F, -2.8F, 0.4887F, 0F, 0F));
        PartDefinition p_ice_talon_right_leg_2 = p_ice_feet_right_leg.addOrReplaceChild("ice_talon_right_leg_2", CubeListBuilder.create()
                .texOffs(105, 290).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(1.6F, 11.6F, -2.8F, 0.4887F, 0.2234F, 0F));
        PartDefinition p_lightning_head_head = root.addOrReplaceChild("lightning_head_head", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_light_horn_left_0 = p_lightning_head_head.addOrReplaceChild("light_horn_left_0", CubeListBuilder.create()
                .texOffs(189, 244).addBox(-1F, -2.7F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.05F, -0.3F, -0.05F)),
                PartPose.offsetAndRotation(3.1F, -8.7F, -1.2F, -0.3491F, 0F, 0.4538F));
        PartDefinition p_light_horn_left_1 = p_light_horn_left_0.addOrReplaceChild("light_horn_left_1", CubeListBuilder.create()
                .texOffs(198, 244).addBox(-1F, -2.6F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.2F, -0.4F, -0.2F)),
                PartPose.offsetAndRotation(0F, -2.15F, 0F, -0.8727F, 0F, -0.3142F));
        PartDefinition p_light_horn_left_2 = p_light_horn_left_1.addOrReplaceChild("light_horn_left_2", CubeListBuilder.create()
                .texOffs(207, 244).addBox(-1F, -2.6F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.35F, -0.4F, -0.35F)),
                PartPose.offsetAndRotation(0F, -1.95F, 0F, 0.6981F, 0F, 0.1745F));
        PartDefinition p_light_horn_left_3 = p_light_horn_left_2.addOrReplaceChild("light_horn_left_3", CubeListBuilder.create()
                .texOffs(95, 302).addBox(-0.5F, -2F, -0.5F, 1F, 2F, 1F),
                PartPose.offsetAndRotation(0F, -1.95F, 0F, -0.9774F, 0F, -0.1047F));
        PartDefinition p_light_horn_left_4 = p_light_horn_left_3.addOrReplaceChild("light_horn_left_4", CubeListBuilder.create()
                .texOffs(100, 302).addBox(-0.5F, -1.9F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.15F, -0.1F, -0.15F)),
                PartPose.offsetAndRotation(0F, -1.75F, 0F, 0.5934F, 0F, 0F));
        PartDefinition p_light_horn_right_0 = p_lightning_head_head.addOrReplaceChild("light_horn_right_0", CubeListBuilder.create()
                .texOffs(216, 244).addBox(-1F, -2.7F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.05F, -0.3F, -0.05F)),
                PartPose.offsetAndRotation(-3.1F, -8.7F, -1.2F, -0.3491F, 0F, -0.4538F));
        PartDefinition p_light_horn_right_1 = p_light_horn_right_0.addOrReplaceChild("light_horn_right_1", CubeListBuilder.create()
                .texOffs(225, 244).addBox(-1F, -2.6F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.2F, -0.4F, -0.2F)),
                PartPose.offsetAndRotation(0F, -2.15F, 0F, -0.8727F, 0F, 0.3142F));
        PartDefinition p_light_horn_right_2 = p_light_horn_right_1.addOrReplaceChild("light_horn_right_2", CubeListBuilder.create()
                .texOffs(234, 244).addBox(-1F, -2.6F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.35F, -0.4F, -0.35F)),
                PartPose.offsetAndRotation(0F, -1.95F, 0F, 0.6981F, 0F, -0.1745F));
        PartDefinition p_light_horn_right_3 = p_light_horn_right_2.addOrReplaceChild("light_horn_right_3", CubeListBuilder.create()
                .texOffs(105, 302).addBox(-0.5F, -2F, -0.5F, 1F, 2F, 1F),
                PartPose.offsetAndRotation(0F, -1.95F, 0F, -0.9774F, 0F, 0.1047F));
        PartDefinition p_light_horn_right_4 = p_light_horn_right_3.addOrReplaceChild("light_horn_right_4", CubeListBuilder.create()
                .texOffs(110, 302).addBox(-0.5F, -1.9F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.15F, -0.1F, -0.15F)),
                PartPose.offsetAndRotation(0F, -1.75F, 0F, 0.5934F, 0F, 0F));
        PartDefinition p_lightning_crest_0_0 = p_lightning_head_head.addOrReplaceChild("lightning_crest_0_0", CubeListBuilder.create()
                .texOffs(204, 310).addBox(-0.5F, -0.94F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.06F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, -3.2F, -0.4538F, 0F, 0F));
        PartDefinition p_lightning_crest_0_1 = p_lightning_crest_0_0.addOrReplaceChild("lightning_crest_0_1", CubeListBuilder.create()
                .texOffs(209, 310).addBox(-0.5F, -0.9F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.1F, -0.225F)),
                PartPose.offset(0F, -0.63F, 0F));
        PartDefinition p_lightning_crest_1_0 = p_lightning_head_head.addOrReplaceChild("lightning_crest_1_0", CubeListBuilder.create()
                .texOffs(115, 302).addBox(-0.5F, -1.605F, -0.5F, 1F, 2F, 1F, new CubeDeformation(0F, -0.395F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, -0.6F, -0.4538F, 0F, 0F));
        PartDefinition p_lightning_crest_1_1 = p_lightning_crest_1_0.addOrReplaceChild("lightning_crest_1_1", CubeListBuilder.create()
                .texOffs(120, 302).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.225F, -0.45F, -0.225F)),
                PartPose.offset(0F, -0.96F, 0F));
        PartDefinition p_lightning_crest_2_0 = p_lightning_head_head.addOrReplaceChild("lightning_crest_2_0", CubeListBuilder.create()
                .texOffs(214, 310).addBox(-0.5F, -0.995F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.005F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, 2F, -0.4538F, 0F, 0F));
        PartDefinition p_lightning_crest_2_1 = p_lightning_crest_2_0.addOrReplaceChild("lightning_crest_2_1", CubeListBuilder.create()
                .texOffs(219, 310).addBox(-0.5F, -0.95F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.05F, -0.225F)),
                PartPose.offset(0F, -0.74F, 0F));
        PartDefinition p_lightning_chest_body = root.addOrReplaceChild("lightning_chest_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_lightning_back_0_0 = p_lightning_chest_body.addOrReplaceChild("lightning_back_0_0", CubeListBuilder.create()
                .texOffs(126, 271).addBox(-1F, -1.935F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.065F, -0.3F)),
                PartPose.offsetAndRotation(0F, 0.8F, 2.9F, -0.8727F, 0F, 0F));
        PartDefinition p_lightning_back_0_1 = p_lightning_back_0_0.addOrReplaceChild("lightning_back_0_1", CubeListBuilder.create()
                .texOffs(125, 302).addBox(-0.5F, -1.85F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.15F, -0.115F)),
                PartPose.offset(0F, -1.62F, 0F));
        PartDefinition p_lightning_back_1_0 = p_lightning_chest_body.addOrReplaceChild("lightning_back_1_0", CubeListBuilder.create()
                .texOffs(243, 244).addBox(-1F, -2.545F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.3F, -0.455F, -0.3F)),
                PartPose.offsetAndRotation(0F, 3.6F, 2.9F, -0.8727F, 0F, 0F));
        PartDefinition p_lightning_back_1_1 = p_lightning_back_1_0.addOrReplaceChild("lightning_back_1_1", CubeListBuilder.create()
                .texOffs(130, 302).addBox(-0.5F, -1.95F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.05F, -0.115F)),
                PartPose.offset(0F, -1.84F, 0F));
        PartDefinition p_lightning_back_2_0 = p_lightning_chest_body.addOrReplaceChild("lightning_back_2_0", CubeListBuilder.create()
                .texOffs(135, 271).addBox(-1F, -1.88F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.12F, -0.3F)),
                PartPose.offsetAndRotation(0F, 6.6F, 2.9F, -0.8727F, 0F, 0F));
        PartDefinition p_lightning_back_2_1 = p_lightning_back_2_0.addOrReplaceChild("lightning_back_2_1", CubeListBuilder.create()
                .texOffs(135, 302).addBox(-0.5F, -1.8F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.2F, -0.115F)),
                PartPose.offset(0F, -1.51F, 0F));
        PartDefinition p_lightning_back_3_0 = p_lightning_chest_body.addOrReplaceChild("lightning_back_3_0", CubeListBuilder.create()
                .texOffs(144, 271).addBox(-1F, -1.66F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.34F, -0.3F)),
                PartPose.offsetAndRotation(0F, 9.4F, 2.9F, -0.8727F, 0F, 0F));
        PartDefinition p_lightning_back_3_1 = p_lightning_back_3_0.addOrReplaceChild("lightning_back_3_1", CubeListBuilder.create()
                .texOffs(140, 302).addBox(-0.5F, -1.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.4F, -0.115F)),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_lightning_chest_left_arm = root.addOrReplaceChild("lightning_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_lightning_pauldron_left_arm = p_lightning_chest_left_arm.addOrReplaceChild("lightning_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(186, 71).addBox(-3.3F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F))
                .texOffs(174, 104).addBox(-2F, -0.1F, -4F, 6F, 2F, 8F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.2094F));
        PartDefinition p_lightning_claw_left_arm_0 = p_lightning_pauldron_left_arm.addOrReplaceChild("lightning_claw_left_arm_0", CubeListBuilder.create()
                .texOffs(153, 271).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.285F, -0.35F)),
                PartPose.offsetAndRotation(2.4F, -1.4F, 0F, 0F, 0F, 0.6981F));
        PartDefinition p_lightning_claw_left_arm_1 = p_lightning_claw_left_arm_0.addOrReplaceChild("lightning_claw_left_arm_1", CubeListBuilder.create()
                .texOffs(145, 302).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.35F, -0.1425F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_lightning_chest_right_arm = root.addOrReplaceChild("lightning_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_lightning_pauldron_right_arm = p_lightning_chest_right_arm.addOrReplaceChild("lightning_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(217, 71).addBox(-3.7F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F))
                .texOffs(203, 104).addBox(-4F, -0.1F, -4F, 6F, 2F, 8F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.2094F));
        PartDefinition p_lightning_claw_right_arm_0 = p_lightning_pauldron_right_arm.addOrReplaceChild("lightning_claw_right_arm_0", CubeListBuilder.create()
                .texOffs(162, 271).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.285F, -0.35F)),
                PartPose.offsetAndRotation(-2.4F, -1.4F, 0F, 0F, 0F, -0.6981F));
        PartDefinition p_lightning_claw_right_arm_1 = p_lightning_claw_right_arm_0.addOrReplaceChild("lightning_claw_right_arm_1", CubeListBuilder.create()
                .texOffs(150, 302).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.35F, -0.1425F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_lightning_legs_body = root.addOrReplaceChild("lightning_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_lightning_tail_0 = p_lightning_legs_body.addOrReplaceChild("lightning_tail_0", CubeListBuilder.create()
                .texOffs(225, 211).addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F),
                PartPose.offsetAndRotation(0F, 10.2F, 2.2F, 0.5236F, 0F, 0F));
        PartDefinition p_lightning_tail_1 = p_lightning_tail_0.addOrReplaceChild("lightning_tail_1", CubeListBuilder.create()
                .texOffs(238, 211).addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, 2.75F, 0F, 0.1745F, 0F, 0F));
        PartDefinition p_lightning_tail_2 = p_lightning_tail_1.addOrReplaceChild("lightning_tail_2", CubeListBuilder.create()
                .texOffs(0, 218).addBox(-1.5F, -0.1F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.1F, -0.4F)),
                PartPose.offsetAndRotation(0F, 2.75F, 0F, 0.2094F, 0F, 0F));
        PartDefinition p_lightning_tail_3 = p_lightning_tail_2.addOrReplaceChild("lightning_tail_3", CubeListBuilder.create()
                .texOffs(0, 250).addBox(-1F, -0.2F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.15F, -0.2F, -0.15F)),
                PartPose.offsetAndRotation(0F, 2.55F, 0F, 0.2094F, 0F, 0F));
        PartDefinition p_lightning_tail_4 = p_lightning_tail_3.addOrReplaceChild("lightning_tail_4", CubeListBuilder.create()
                .texOffs(9, 250).addBox(-1F, -0.4F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.4F, -0.4F, -0.4F)),
                PartPose.offsetAndRotation(0F, 2.35F, 0F, 0.1745F, 0F, 0F));
        PartDefinition p_lightning_tail_tip = p_lightning_tail_4.addOrReplaceChild("lightning_tail_tip", CubeListBuilder.create()
                .texOffs(165, 196).addBox(-0.5F, -0.4F, -2F, 1F, 3F, 4F, new CubeDeformation(-0.2F, -0.4F, -0.4F))
                .texOffs(18, 250).addBox(-0.5F, 2F, -0.8F, 1F, 2F, 3F, new CubeDeformation(-0.25F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, 2.4F, 0F, 0.1396F, 0F, 0F));
        PartDefinition p_lightning_legs_left_leg = root.addOrReplaceChild("lightning_legs_left_leg", CubeListBuilder.create()
                .texOffs(110, 261).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_lightning_legs_right_leg = root.addOrReplaceChild("lightning_legs_right_leg", CubeListBuilder.create()
                .texOffs(121, 261).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_lightning_feet_left_leg = root.addOrReplaceChild("lightning_feet_left_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_lightning_talon_left_leg_0 = p_lightning_feet_left_leg.addOrReplaceChild("lightning_talon_left_leg_0", CubeListBuilder.create()
                .texOffs(112, 290).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(-1.6F, 11.6F, -2.8F, 0.4887F, -0.2234F, 0F));
        PartDefinition p_lightning_talon_left_leg_1 = p_lightning_feet_left_leg.addOrReplaceChild("lightning_talon_left_leg_1", CubeListBuilder.create()
                .texOffs(119, 290).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(0F, 11.6F, -2.8F, 0.4887F, 0F, 0F));
        PartDefinition p_lightning_talon_left_leg_2 = p_lightning_feet_left_leg.addOrReplaceChild("lightning_talon_left_leg_2", CubeListBuilder.create()
                .texOffs(126, 290).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(1.6F, 11.6F, -2.8F, 0.4887F, 0.2234F, 0F));
        PartDefinition p_lightning_feet_right_leg = root.addOrReplaceChild("lightning_feet_right_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_lightning_talon_right_leg_0 = p_lightning_feet_right_leg.addOrReplaceChild("lightning_talon_right_leg_0", CubeListBuilder.create()
                .texOffs(133, 290).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(-1.6F, 11.6F, -2.8F, 0.4887F, -0.2234F, 0F));
        PartDefinition p_lightning_talon_right_leg_1 = p_lightning_feet_right_leg.addOrReplaceChild("lightning_talon_right_leg_1", CubeListBuilder.create()
                .texOffs(140, 290).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(0F, 11.6F, -2.8F, 0.4887F, 0F, 0F));
        PartDefinition p_lightning_talon_right_leg_2 = p_lightning_feet_right_leg.addOrReplaceChild("lightning_talon_right_leg_2", CubeListBuilder.create()
                .texOffs(147, 290).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(1.6F, 11.6F, -2.8F, 0.4887F, 0.2234F, 0F));
        PartDefinition p_nature_head_head = root.addOrReplaceChild("nature_head_head", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_antler_left_0 = p_nature_head_head.addOrReplaceChild("antler_left_0", CubeListBuilder.create()
                .texOffs(27, 250).addBox(-1F, -3F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.3F, 0F, -0.3F)),
                PartPose.offsetAndRotation(3F, -8.7F, -0.8F, -0.2443F, 0F, 0.5236F));
        PartDefinition p_antler_left_1 = p_antler_left_0.addOrReplaceChild("antler_left_1", CubeListBuilder.create()
                .texOffs(36, 250).addBox(-1F, -3F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.4F, 0F, -0.4F)),
                PartPose.offsetAndRotation(0F, -2.75F, 0F, -0.384F, 0F, -0.1745F));
        PartDefinition p_antler_left_2 = p_antler_left_1.addOrReplaceChild("antler_left_2", CubeListBuilder.create()
                .texOffs(154, 281).addBox(-0.5F, -2.8F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.05F, -0.2F, -0.05F)),
                PartPose.offsetAndRotation(0F, -2.75F, 0F, -0.3491F, 0F, -0.1396F));
        PartDefinition p_antler_tine_left_1 = p_antler_left_1.addOrReplaceChild("antler_tine_left_1", CubeListBuilder.create()
                .texOffs(155, 302).addBox(-0.5F, -1.9F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.15F, -0.1F, -0.15F)),
                PartPose.offsetAndRotation(0F, -2.2F, 0F, 0.8378F, 0F, -0.2443F));
        PartDefinition p_antler_tine_left_0 = p_antler_left_0.addOrReplaceChild("antler_tine_left_0", CubeListBuilder.create()
                .texOffs(160, 302).addBox(-0.5F, -2F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1F, 0F, -0.1F)),
                PartPose.offsetAndRotation(0F, -2.4F, 0F, 0.8727F, 0F, 0.1745F));
        PartDefinition p_antler_leaf_left = p_antler_left_0.addOrReplaceChild("antler_leaf_left", CubeListBuilder.create()
                .texOffs(216, 250).addBox(-0.5F, -2.8F, -1F, 1F, 3F, 2F, new CubeDeformation(-0.35F, -0.2F, -0.1F)),
                PartPose.offsetAndRotation(0.6F, -1.4F, 0F, 0F, 0F, 0.8727F));
        PartDefinition p_antler_right_0 = p_nature_head_head.addOrReplaceChild("antler_right_0", CubeListBuilder.create()
                .texOffs(45, 250).addBox(-1F, -3F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.3F, 0F, -0.3F)),
                PartPose.offsetAndRotation(-3F, -8.7F, -0.8F, -0.2443F, 0F, -0.5236F));
        PartDefinition p_antler_right_1 = p_antler_right_0.addOrReplaceChild("antler_right_1", CubeListBuilder.create()
                .texOffs(54, 250).addBox(-1F, -3F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.4F, 0F, -0.4F)),
                PartPose.offsetAndRotation(0F, -2.75F, 0F, -0.384F, 0F, 0.1745F));
        PartDefinition p_antler_right_2 = p_antler_right_1.addOrReplaceChild("antler_right_2", CubeListBuilder.create()
                .texOffs(159, 281).addBox(-0.5F, -2.8F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.05F, -0.2F, -0.05F)),
                PartPose.offsetAndRotation(0F, -2.75F, 0F, -0.3491F, 0F, 0.1396F));
        PartDefinition p_antler_tine_right_1 = p_antler_right_1.addOrReplaceChild("antler_tine_right_1", CubeListBuilder.create()
                .texOffs(165, 302).addBox(-0.5F, -1.9F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.15F, -0.1F, -0.15F)),
                PartPose.offsetAndRotation(0F, -2.2F, 0F, 0.8378F, 0F, 0.2443F));
        PartDefinition p_antler_tine_right_0 = p_antler_right_0.addOrReplaceChild("antler_tine_right_0", CubeListBuilder.create()
                .texOffs(170, 302).addBox(-0.5F, -2F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1F, 0F, -0.1F)),
                PartPose.offsetAndRotation(0F, -2.4F, 0F, 0.8727F, 0F, -0.1745F));
        PartDefinition p_antler_leaf_right = p_antler_right_0.addOrReplaceChild("antler_leaf_right", CubeListBuilder.create()
                .texOffs(223, 250).addBox(-0.5F, -2.8F, -1F, 1F, 3F, 2F, new CubeDeformation(-0.35F, -0.2F, -0.1F)),
                PartPose.offsetAndRotation(-0.6F, -1.4F, 0F, 0F, 0F, -0.8727F));
        PartDefinition p_nature_crest_0_0 = p_nature_head_head.addOrReplaceChild("nature_crest_0_0", CubeListBuilder.create()
                .texOffs(224, 310).addBox(-0.5F, -0.94F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.06F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, -3.2F, -0.4538F, 0F, 0F));
        PartDefinition p_nature_crest_0_1 = p_nature_crest_0_0.addOrReplaceChild("nature_crest_0_1", CubeListBuilder.create()
                .texOffs(229, 310).addBox(-0.5F, -0.9F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.1F, -0.225F)),
                PartPose.offset(0F, -0.63F, 0F));
        PartDefinition p_nature_crest_1_0 = p_nature_head_head.addOrReplaceChild("nature_crest_1_0", CubeListBuilder.create()
                .texOffs(175, 302).addBox(-0.5F, -1.605F, -0.5F, 1F, 2F, 1F, new CubeDeformation(0F, -0.395F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, -0.6F, -0.4538F, 0F, 0F));
        PartDefinition p_nature_crest_1_1 = p_nature_crest_1_0.addOrReplaceChild("nature_crest_1_1", CubeListBuilder.create()
                .texOffs(180, 302).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.225F, -0.45F, -0.225F)),
                PartPose.offset(0F, -0.96F, 0F));
        PartDefinition p_nature_crest_2_0 = p_nature_head_head.addOrReplaceChild("nature_crest_2_0", CubeListBuilder.create()
                .texOffs(234, 310).addBox(-0.5F, -0.995F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.005F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, 2F, -0.4538F, 0F, 0F));
        PartDefinition p_nature_crest_2_1 = p_nature_crest_2_0.addOrReplaceChild("nature_crest_2_1", CubeListBuilder.create()
                .texOffs(239, 310).addBox(-0.5F, -0.95F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.05F, -0.225F)),
                PartPose.offset(0F, -0.74F, 0F));
        PartDefinition p_nature_chest_body = root.addOrReplaceChild("nature_chest_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_nature_frond_0 = p_nature_chest_body.addOrReplaceChild("nature_frond_0", CubeListBuilder.create()
                .texOffs(157, 225).addBox(-1.5F, -4.6F, -0.5F, 3F, 5F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.35F)),
                PartPose.offsetAndRotation(0F, 1F, 2.9F, -0.8378F, 0F, 0F));
        PartDefinition p_nature_frond_1 = p_nature_chest_body.addOrReplaceChild("nature_frond_1", CubeListBuilder.create()
                .texOffs(63, 250).addBox(-1.5F, -3.8F, -0.5F, 3F, 4F, 1F, new CubeDeformation(-0.4F, -0.2F, -0.35F)),
                PartPose.offsetAndRotation(0F, 4F, 2.9F, -0.8378F, 0F, 0F));
        PartDefinition p_nature_frond_2 = p_nature_chest_body.addOrReplaceChild("nature_frond_2", CubeListBuilder.create()
                .texOffs(171, 271).addBox(-1.5F, -3F, -0.5F, 3F, 3F, 1F, new CubeDeformation(-0.4F, 0F, -0.35F)),
                PartPose.offsetAndRotation(0F, 7F, 2.9F, -0.8378F, 0F, 0F));
        PartDefinition p_nature_chest_left_arm = root.addOrReplaceChild("nature_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_nature_pauldron_left_arm = p_nature_chest_left_arm.addOrReplaceChild("nature_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(0, 82).addBox(-3.3F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F))
                .texOffs(0, 115).addBox(-2F, -0.1F, -4F, 6F, 2F, 8F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.2094F));
        PartDefinition p_nature_claw_left_arm_0 = p_nature_pauldron_left_arm.addOrReplaceChild("nature_claw_left_arm_0", CubeListBuilder.create()
                .texOffs(180, 271).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.285F, -0.35F)),
                PartPose.offsetAndRotation(2.4F, -1.4F, 0F, 0F, 0F, 0.6981F));
        PartDefinition p_nature_claw_left_arm_1 = p_nature_claw_left_arm_0.addOrReplaceChild("nature_claw_left_arm_1", CubeListBuilder.create()
                .texOffs(185, 302).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.35F, -0.1425F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_nature_chest_right_arm = root.addOrReplaceChild("nature_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_nature_pauldron_right_arm = p_nature_chest_right_arm.addOrReplaceChild("nature_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(31, 82).addBox(-3.7F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F))
                .texOffs(29, 115).addBox(-4F, -0.1F, -4F, 6F, 2F, 8F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.2094F));
        PartDefinition p_nature_claw_right_arm_0 = p_nature_pauldron_right_arm.addOrReplaceChild("nature_claw_right_arm_0", CubeListBuilder.create()
                .texOffs(189, 271).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.285F, -0.35F)),
                PartPose.offsetAndRotation(-2.4F, -1.4F, 0F, 0F, 0F, -0.6981F));
        PartDefinition p_nature_claw_right_arm_1 = p_nature_claw_right_arm_0.addOrReplaceChild("nature_claw_right_arm_1", CubeListBuilder.create()
                .texOffs(190, 302).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.35F, -0.1425F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_nature_legs_body = root.addOrReplaceChild("nature_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_nature_tail_0 = p_nature_legs_body.addOrReplaceChild("nature_tail_0", CubeListBuilder.create()
                .texOffs(13, 218).addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F),
                PartPose.offsetAndRotation(0F, 10.2F, 2.2F, 0.5236F, 0F, 0F));
        PartDefinition p_nature_tail_1 = p_nature_tail_0.addOrReplaceChild("nature_tail_1", CubeListBuilder.create()
                .texOffs(26, 218).addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, 2.75F, 0F, 0.1745F, 0F, 0F));
        PartDefinition p_nature_tail_2 = p_nature_tail_1.addOrReplaceChild("nature_tail_2", CubeListBuilder.create()
                .texOffs(39, 218).addBox(-1.5F, -0.1F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.1F, -0.4F)),
                PartPose.offsetAndRotation(0F, 2.75F, 0F, 0.2094F, 0F, 0F));
        PartDefinition p_nature_tail_3 = p_nature_tail_2.addOrReplaceChild("nature_tail_3", CubeListBuilder.create()
                .texOffs(72, 250).addBox(-1F, -0.2F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.15F, -0.2F, -0.15F)),
                PartPose.offsetAndRotation(0F, 2.55F, 0F, 0.2094F, 0F, 0F));
        PartDefinition p_nature_tail_4 = p_nature_tail_3.addOrReplaceChild("nature_tail_4", CubeListBuilder.create()
                .texOffs(81, 250).addBox(-1F, -0.4F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.4F, -0.4F, -0.4F)),
                PartPose.offsetAndRotation(0F, 2.35F, 0F, 0.1745F, 0F, 0F));
        PartDefinition p_nature_tail_tip = p_nature_tail_4.addOrReplaceChild("nature_tail_tip", CubeListBuilder.create()
                .texOffs(35, 187).addBox(-0.5F, -0.4F, -1.5F, 1F, 5F, 3F, new CubeDeformation(-0.35F, -0.4F, -0.1F)),
                PartPose.offsetAndRotation(0F, 2.4F, 0F, 0.1396F, 0F, 0F));
        PartDefinition p_nature_legs_left_leg = root.addOrReplaceChild("nature_legs_left_leg", CubeListBuilder.create()
                .texOffs(132, 261).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_nature_legs_right_leg = root.addOrReplaceChild("nature_legs_right_leg", CubeListBuilder.create()
                .texOffs(143, 261).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_nature_feet_left_leg = root.addOrReplaceChild("nature_feet_left_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_nature_talon_left_leg_0 = p_nature_feet_left_leg.addOrReplaceChild("nature_talon_left_leg_0", CubeListBuilder.create()
                .texOffs(154, 290).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(-1.6F, 11.6F, -2.8F, 0.4887F, -0.2234F, 0F));
        PartDefinition p_nature_talon_left_leg_1 = p_nature_feet_left_leg.addOrReplaceChild("nature_talon_left_leg_1", CubeListBuilder.create()
                .texOffs(161, 290).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(0F, 11.6F, -2.8F, 0.4887F, 0F, 0F));
        PartDefinition p_nature_talon_left_leg_2 = p_nature_feet_left_leg.addOrReplaceChild("nature_talon_left_leg_2", CubeListBuilder.create()
                .texOffs(168, 290).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(1.6F, 11.6F, -2.8F, 0.4887F, 0.2234F, 0F));
        PartDefinition p_nature_feet_right_leg = root.addOrReplaceChild("nature_feet_right_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_nature_talon_right_leg_0 = p_nature_feet_right_leg.addOrReplaceChild("nature_talon_right_leg_0", CubeListBuilder.create()
                .texOffs(175, 290).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(-1.6F, 11.6F, -2.8F, 0.4887F, -0.2234F, 0F));
        PartDefinition p_nature_talon_right_leg_1 = p_nature_feet_right_leg.addOrReplaceChild("nature_talon_right_leg_1", CubeListBuilder.create()
                .texOffs(182, 290).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(0F, 11.6F, -2.8F, 0.4887F, 0F, 0F));
        PartDefinition p_nature_talon_right_leg_2 = p_nature_feet_right_leg.addOrReplaceChild("nature_talon_right_leg_2", CubeListBuilder.create()
                .texOffs(189, 290).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(1.6F, 11.6F, -2.8F, 0.4887F, 0.2234F, 0F));
        PartDefinition p_earth_head_head = root.addOrReplaceChild("earth_head_head", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_ram_horn_left_0 = p_earth_head_head.addOrReplaceChild("ram_horn_left_0", CubeListBuilder.create()
                .texOffs(52, 218).addBox(-1.5F, -2.8F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.1F, -0.2F, -0.1F)),
                PartPose.offsetAndRotation(5F, -8F, -0.6F, -0.4189F, 0F, 0.4887F));
        PartDefinition p_ram_horn_left_1 = p_ram_horn_left_0.addOrReplaceChild("ram_horn_left_1", CubeListBuilder.create()
                .texOffs(65, 218).addBox(-1.5F, -2.8F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.25F, -0.2F, -0.25F)),
                PartPose.offsetAndRotation(0F, -2.35F, 0F, -0.9599F, 0F, 0F));
        PartDefinition p_ram_horn_left_2 = p_ram_horn_left_1.addOrReplaceChild("ram_horn_left_2", CubeListBuilder.create()
                .texOffs(78, 218).addBox(-1.5F, -2.8F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(0F, -2.35F, 0F, -0.9599F, 0F, 0F));
        PartDefinition p_ram_horn_left_3 = p_ram_horn_left_2.addOrReplaceChild("ram_horn_left_3", CubeListBuilder.create()
                .texOffs(90, 250).addBox(-1F, -2.7F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.05F, -0.3F, -0.05F)),
                PartPose.offsetAndRotation(0F, -2.35F, 0F, -0.9599F, 0F, 0F));
        PartDefinition p_ram_horn_left_4 = p_ram_horn_left_3.addOrReplaceChild("ram_horn_left_4", CubeListBuilder.create()
                .texOffs(99, 250).addBox(-1F, -2.6F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.25F, -0.4F, -0.25F)),
                PartPose.offsetAndRotation(0F, -2.15F, 0F, -0.9599F, 0F, 0F));
        PartDefinition p_ram_horn_left_5 = p_ram_horn_left_4.addOrReplaceChild("ram_horn_left_5", CubeListBuilder.create()
                .texOffs(195, 302).addBox(-0.5F, -1.9F, -0.5F, 1F, 2F, 1F, new CubeDeformation(0F, -0.1F, 0F)),
                PartPose.offsetAndRotation(0F, -1.95F, 0F, -0.7854F, 0F, 0F));
        PartDefinition p_ram_horn_right_0 = p_earth_head_head.addOrReplaceChild("ram_horn_right_0", CubeListBuilder.create()
                .texOffs(91, 218).addBox(-1.5F, -2.8F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.1F, -0.2F, -0.1F)),
                PartPose.offsetAndRotation(-5F, -8F, -0.6F, -0.4189F, 0F, -0.4887F));
        PartDefinition p_ram_horn_right_1 = p_ram_horn_right_0.addOrReplaceChild("ram_horn_right_1", CubeListBuilder.create()
                .texOffs(104, 218).addBox(-1.5F, -2.8F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.25F, -0.2F, -0.25F)),
                PartPose.offsetAndRotation(0F, -2.35F, 0F, -0.9599F, 0F, 0F));
        PartDefinition p_ram_horn_right_2 = p_ram_horn_right_1.addOrReplaceChild("ram_horn_right_2", CubeListBuilder.create()
                .texOffs(117, 218).addBox(-1.5F, -2.8F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(0F, -2.35F, 0F, -0.9599F, 0F, 0F));
        PartDefinition p_ram_horn_right_3 = p_ram_horn_right_2.addOrReplaceChild("ram_horn_right_3", CubeListBuilder.create()
                .texOffs(108, 250).addBox(-1F, -2.7F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.05F, -0.3F, -0.05F)),
                PartPose.offsetAndRotation(0F, -2.35F, 0F, -0.9599F, 0F, 0F));
        PartDefinition p_ram_horn_right_4 = p_ram_horn_right_3.addOrReplaceChild("ram_horn_right_4", CubeListBuilder.create()
                .texOffs(117, 250).addBox(-1F, -2.6F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.25F, -0.4F, -0.25F)),
                PartPose.offsetAndRotation(0F, -2.15F, 0F, -0.9599F, 0F, 0F));
        PartDefinition p_ram_horn_right_5 = p_ram_horn_right_4.addOrReplaceChild("ram_horn_right_5", CubeListBuilder.create()
                .texOffs(200, 302).addBox(-0.5F, -1.9F, -0.5F, 1F, 2F, 1F, new CubeDeformation(0F, -0.1F, 0F)),
                PartPose.offsetAndRotation(0F, -1.95F, 0F, -0.7854F, 0F, 0F));
        PartDefinition p_earth_crest_0_0 = p_earth_head_head.addOrReplaceChild("earth_crest_0_0", CubeListBuilder.create()
                .texOffs(244, 310).addBox(-0.5F, -0.94F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.06F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, -3.2F, -0.4538F, 0F, 0F));
        PartDefinition p_earth_crest_0_1 = p_earth_crest_0_0.addOrReplaceChild("earth_crest_0_1", CubeListBuilder.create()
                .texOffs(249, 310).addBox(-0.5F, -0.9F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.1F, -0.225F)),
                PartPose.offset(0F, -0.63F, 0F));
        PartDefinition p_earth_crest_1_0 = p_earth_head_head.addOrReplaceChild("earth_crest_1_0", CubeListBuilder.create()
                .texOffs(205, 302).addBox(-0.5F, -1.605F, -0.5F, 1F, 2F, 1F, new CubeDeformation(0F, -0.395F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, -0.6F, -0.4538F, 0F, 0F));
        PartDefinition p_earth_crest_1_1 = p_earth_crest_1_0.addOrReplaceChild("earth_crest_1_1", CubeListBuilder.create()
                .texOffs(210, 302).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.225F, -0.45F, -0.225F)),
                PartPose.offset(0F, -0.96F, 0F));
        PartDefinition p_earth_crest_2_0 = p_earth_head_head.addOrReplaceChild("earth_crest_2_0", CubeListBuilder.create()
                .texOffs(0, 313).addBox(-0.5F, -0.995F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.005F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, 2F, -0.4538F, 0F, 0F));
        PartDefinition p_earth_crest_2_1 = p_earth_crest_2_0.addOrReplaceChild("earth_crest_2_1", CubeListBuilder.create()
                .texOffs(5, 313).addBox(-0.5F, -0.95F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.05F, -0.225F)),
                PartPose.offset(0F, -0.74F, 0F));
        PartDefinition p_earth_chest_body = root.addOrReplaceChild("earth_chest_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_earth_plate_0 = p_earth_chest_body.addOrReplaceChild("earth_plate_0", CubeListBuilder.create()
                .texOffs(198, 196).addBox(-0.5F, -3.6F, -1.5F, 1F, 4F, 3F, new CubeDeformation(-0.1F, -0.4F, -0.3F))
                .texOffs(196, 290).addBox(-0.5F, -4.1F, -1F, 1F, 1F, 2F, new CubeDeformation(-0.2F, 0F, -0.4F)),
                PartPose.offsetAndRotation(0F, 0.8F, 2.9F, -0.6981F, 0F, 0F));
        PartDefinition p_earth_plate_1 = p_earth_chest_body.addOrReplaceChild("earth_plate_1", CubeListBuilder.create()
                .texOffs(207, 196).addBox(-0.5F, -3.8F, -1.5F, 1F, 4F, 3F, new CubeDeformation(-0.1F, -0.2F, -0.3F))
                .texOffs(203, 290).addBox(-0.5F, -4.5F, -1F, 1F, 1F, 2F, new CubeDeformation(-0.2F, 0F, -0.4F)),
                PartPose.offsetAndRotation(0F, 4F, 2.9F, -0.6981F, 0F, 0F));
        PartDefinition p_earth_plate_2 = p_earth_chest_body.addOrReplaceChild("earth_plate_2", CubeListBuilder.create()
                .texOffs(166, 225).addBox(-0.5F, -2.9F, -1.5F, 1F, 3F, 3F, new CubeDeformation(-0.1F, -0.1F, -0.3F))
                .texOffs(210, 290).addBox(-0.5F, -3.7F, -1F, 1F, 1F, 2F, new CubeDeformation(-0.2F, 0F, -0.4F)),
                PartPose.offsetAndRotation(0F, 7.4F, 2.9F, -0.6981F, 0F, 0F));
        PartDefinition p_earth_chest_left_arm = root.addOrReplaceChild("earth_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_earth_pauldron_left_arm = p_earth_chest_left_arm.addOrReplaceChild("earth_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(62, 82).addBox(-3.3F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F))
                .texOffs(58, 115).addBox(-2F, -0.1F, -4F, 6F, 2F, 8F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.2094F));
        PartDefinition p_earth_claw_left_arm_0 = p_earth_pauldron_left_arm.addOrReplaceChild("earth_claw_left_arm_0", CubeListBuilder.create()
                .texOffs(198, 271).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.285F, -0.35F)),
                PartPose.offsetAndRotation(2.4F, -1.4F, 0F, 0F, 0F, 0.6981F));
        PartDefinition p_earth_claw_left_arm_1 = p_earth_claw_left_arm_0.addOrReplaceChild("earth_claw_left_arm_1", CubeListBuilder.create()
                .texOffs(215, 302).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.35F, -0.1425F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_earth_chest_right_arm = root.addOrReplaceChild("earth_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_earth_pauldron_right_arm = p_earth_chest_right_arm.addOrReplaceChild("earth_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(93, 82).addBox(-3.7F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F))
                .texOffs(87, 115).addBox(-4F, -0.1F, -4F, 6F, 2F, 8F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.2094F));
        PartDefinition p_earth_claw_right_arm_0 = p_earth_pauldron_right_arm.addOrReplaceChild("earth_claw_right_arm_0", CubeListBuilder.create()
                .texOffs(207, 271).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.285F, -0.35F)),
                PartPose.offsetAndRotation(-2.4F, -1.4F, 0F, 0F, 0F, -0.6981F));
        PartDefinition p_earth_claw_right_arm_1 = p_earth_claw_right_arm_0.addOrReplaceChild("earth_claw_right_arm_1", CubeListBuilder.create()
                .texOffs(220, 302).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.35F, -0.1425F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_earth_legs_body = root.addOrReplaceChild("earth_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_earth_tail_0 = p_earth_legs_body.addOrReplaceChild("earth_tail_0", CubeListBuilder.create()
                .texOffs(130, 218).addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F),
                PartPose.offsetAndRotation(0F, 10.2F, 2.2F, 0.5236F, 0F, 0F));
        PartDefinition p_earth_tail_1 = p_earth_tail_0.addOrReplaceChild("earth_tail_1", CubeListBuilder.create()
                .texOffs(143, 218).addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, 2.75F, 0F, 0.1745F, 0F, 0F));
        PartDefinition p_earth_tail_2 = p_earth_tail_1.addOrReplaceChild("earth_tail_2", CubeListBuilder.create()
                .texOffs(156, 218).addBox(-1.5F, -0.1F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.1F, -0.4F)),
                PartPose.offsetAndRotation(0F, 2.75F, 0F, 0.2094F, 0F, 0F));
        PartDefinition p_earth_tail_3 = p_earth_tail_2.addOrReplaceChild("earth_tail_3", CubeListBuilder.create()
                .texOffs(126, 250).addBox(-1F, -0.2F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.15F, -0.2F, -0.15F)),
                PartPose.offsetAndRotation(0F, 2.55F, 0F, 0.2094F, 0F, 0F));
        PartDefinition p_earth_tail_4 = p_earth_tail_3.addOrReplaceChild("earth_tail_4", CubeListBuilder.create()
                .texOffs(135, 250).addBox(-1F, -0.4F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.4F, -0.4F, -0.4F)),
                PartPose.offsetAndRotation(0F, 2.35F, 0F, 0.1745F, 0F, 0F));
        PartDefinition p_earth_tail_tip = p_earth_tail_4.addOrReplaceChild("earth_tail_tip", CubeListBuilder.create()
                .texOffs(202, 187).addBox(-2F, -0.1F, -2F, 4F, 3F, 4F, new CubeDeformation(-0.4F, -0.1F, -0.4F))
                .texOffs(217, 290).addBox(-1F, 0.2F, -2.15F, 2F, 2F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.25F)),
                PartPose.offsetAndRotation(0F, 2.4F, 0F, 0.1396F, 0F, 0F));
        PartDefinition p_earth_legs_left_leg = root.addOrReplaceChild("earth_legs_left_leg", CubeListBuilder.create()
                .texOffs(154, 261).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_earth_legs_right_leg = root.addOrReplaceChild("earth_legs_right_leg", CubeListBuilder.create()
                .texOffs(165, 261).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_earth_feet_left_leg = root.addOrReplaceChild("earth_feet_left_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_earth_talon_left_leg_0 = p_earth_feet_left_leg.addOrReplaceChild("earth_talon_left_leg_0", CubeListBuilder.create()
                .texOffs(224, 290).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(-1.6F, 11.6F, -2.8F, 0.4887F, -0.2234F, 0F));
        PartDefinition p_earth_talon_left_leg_1 = p_earth_feet_left_leg.addOrReplaceChild("earth_talon_left_leg_1", CubeListBuilder.create()
                .texOffs(231, 290).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(0F, 11.6F, -2.8F, 0.4887F, 0F, 0F));
        PartDefinition p_earth_talon_left_leg_2 = p_earth_feet_left_leg.addOrReplaceChild("earth_talon_left_leg_2", CubeListBuilder.create()
                .texOffs(238, 290).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(1.6F, 11.6F, -2.8F, 0.4887F, 0.2234F, 0F));
        PartDefinition p_earth_feet_right_leg = root.addOrReplaceChild("earth_feet_right_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_earth_talon_right_leg_0 = p_earth_feet_right_leg.addOrReplaceChild("earth_talon_right_leg_0", CubeListBuilder.create()
                .texOffs(245, 290).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(-1.6F, 11.6F, -2.8F, 0.4887F, -0.2234F, 0F));
        PartDefinition p_earth_talon_right_leg_1 = p_earth_feet_right_leg.addOrReplaceChild("earth_talon_right_leg_1", CubeListBuilder.create()
                .texOffs(0, 294).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(0F, 11.6F, -2.8F, 0.4887F, 0F, 0F));
        PartDefinition p_earth_talon_right_leg_2 = p_earth_feet_right_leg.addOrReplaceChild("earth_talon_right_leg_2", CubeListBuilder.create()
                .texOffs(7, 294).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(1.6F, 11.6F, -2.8F, 0.4887F, 0.2234F, 0F));
        PartDefinition p_water_head_head = root.addOrReplaceChild("water_head_head", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_head_fin_left = p_water_head_head.addOrReplaceChild("head_fin_left", CubeListBuilder.create(),
                PartPose.offsetAndRotation(5.3F, -4.6F, -1F, 0F, 0.2443F, 0F));
        PartDefinition p_head_fin_left_ray_0 = p_head_fin_left.addOrReplaceChild("head_fin_left_ray_0", CubeListBuilder.create()
                .texOffs(65, 196).addBox(-0.5F, -1F, -0.2F, 1F, 2F, 5F, new CubeDeformation(-0.25F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.1047F, 0F, 0F));
        PartDefinition p_head_fin_left_ray_1 = p_head_fin_left.addOrReplaceChild("head_fin_left_ray_1", CubeListBuilder.create()
                .texOffs(185, 178).addBox(-0.5F, -1F, -0.3F, 1F, 2F, 6F, new CubeDeformation(-0.25F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0.2443F, 0F, 0F));
        PartDefinition p_head_fin_left_ray_2 = p_head_fin_left.addOrReplaceChild("head_fin_left_ray_2", CubeListBuilder.create()
                .texOffs(78, 196).addBox(-0.5F, -1F, 0F, 1F, 2F, 5F, new CubeDeformation(-0.25F, -0.25F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0.5934F, 0F, 0F));
        PartDefinition p_head_fin_left_ray_3 = p_head_fin_left.addOrReplaceChild("head_fin_left_ray_3", CubeListBuilder.create()
                .texOffs(35, 225).addBox(-0.5F, -1F, -0.1F, 1F, 2F, 4F, new CubeDeformation(-0.25F, -0.35F, -0.1F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0.9425F, 0F, 0F));
        PartDefinition p_head_fin_right = p_water_head_head.addOrReplaceChild("head_fin_right", CubeListBuilder.create(),
                PartPose.offsetAndRotation(-5.3F, -4.6F, -1F, 0F, -0.2443F, 0F));
        PartDefinition p_head_fin_right_ray_0 = p_head_fin_right.addOrReplaceChild("head_fin_right_ray_0", CubeListBuilder.create()
                .texOffs(91, 196).addBox(-0.5F, -1F, -0.2F, 1F, 2F, 5F, new CubeDeformation(-0.25F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.1047F, 0F, 0F));
        PartDefinition p_head_fin_right_ray_1 = p_head_fin_right.addOrReplaceChild("head_fin_right_ray_1", CubeListBuilder.create()
                .texOffs(200, 178).addBox(-0.5F, -1F, -0.3F, 1F, 2F, 6F, new CubeDeformation(-0.25F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0.2443F, 0F, 0F));
        PartDefinition p_head_fin_right_ray_2 = p_head_fin_right.addOrReplaceChild("head_fin_right_ray_2", CubeListBuilder.create()
                .texOffs(104, 196).addBox(-0.5F, -1F, 0F, 1F, 2F, 5F, new CubeDeformation(-0.25F, -0.25F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0.5934F, 0F, 0F));
        PartDefinition p_head_fin_right_ray_3 = p_head_fin_right.addOrReplaceChild("head_fin_right_ray_3", CubeListBuilder.create()
                .texOffs(46, 225).addBox(-0.5F, -1F, -0.1F, 1F, 2F, 4F, new CubeDeformation(-0.25F, -0.35F, -0.1F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0.9425F, 0F, 0F));
        PartDefinition p_water_crest_0_0 = p_water_head_head.addOrReplaceChild("water_crest_0_0", CubeListBuilder.create()
                .texOffs(10, 313).addBox(-0.5F, -0.94F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.06F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, -3.2F, -0.4538F, 0F, 0F));
        PartDefinition p_water_crest_0_1 = p_water_crest_0_0.addOrReplaceChild("water_crest_0_1", CubeListBuilder.create()
                .texOffs(15, 313).addBox(-0.5F, -0.9F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.1F, -0.225F)),
                PartPose.offset(0F, -0.63F, 0F));
        PartDefinition p_water_crest_1_0 = p_water_head_head.addOrReplaceChild("water_crest_1_0", CubeListBuilder.create()
                .texOffs(225, 302).addBox(-0.5F, -1.605F, -0.5F, 1F, 2F, 1F, new CubeDeformation(0F, -0.395F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, -0.6F, -0.4538F, 0F, 0F));
        PartDefinition p_water_crest_1_1 = p_water_crest_1_0.addOrReplaceChild("water_crest_1_1", CubeListBuilder.create()
                .texOffs(230, 302).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.225F, -0.45F, -0.225F)),
                PartPose.offset(0F, -0.96F, 0F));
        PartDefinition p_water_crest_2_0 = p_water_head_head.addOrReplaceChild("water_crest_2_0", CubeListBuilder.create()
                .texOffs(20, 313).addBox(-0.5F, -0.995F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.005F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, 2F, -0.4538F, 0F, 0F));
        PartDefinition p_water_crest_2_1 = p_water_crest_2_0.addOrReplaceChild("water_crest_2_1", CubeListBuilder.create()
                .texOffs(25, 313).addBox(-0.5F, -0.95F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.05F, -0.225F)),
                PartPose.offset(0F, -0.74F, 0F));
        PartDefinition p_water_chest_body = root.addOrReplaceChild("water_chest_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_water_back_fin_0 = p_water_chest_body.addOrReplaceChild("water_back_fin_0", CubeListBuilder.create(),
                PartPose.offset(0F, 0.6F, 2.9F));
        PartDefinition p_water_back_fin_0_ray_0 = p_water_back_fin_0.addOrReplaceChild("water_back_fin_0_ray_0", CubeListBuilder.create()
                .texOffs(0, 187).addBox(-0.5F, -1.5F, -0.3F, 1F, 3F, 5F, new CubeDeformation(-0.25F, -0.4F, -0.3F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.6632F, 0F, 0F));
        PartDefinition p_water_back_fin_0_ray_1 = p_water_back_fin_0.addOrReplaceChild("water_back_fin_0_ray_1", CubeListBuilder.create()
                .texOffs(57, 225).addBox(-0.5F, -1F, -0.24F, 1F, 2F, 4F, new CubeDeformation(-0.25F, 0F, -0.24F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.1396F, 0F, 0F));
        PartDefinition p_water_back_fin_1 = p_water_chest_body.addOrReplaceChild("water_back_fin_1", CubeListBuilder.create(),
                PartPose.offset(0F, 3.4F, 2.9F));
        PartDefinition p_water_back_fin_1_ray_0 = p_water_back_fin_1.addOrReplaceChild("water_back_fin_1_ray_0", CubeListBuilder.create()
                .texOffs(176, 196).addBox(-0.5F, -1.5F, 0F, 1F, 3F, 4F, new CubeDeformation(-0.25F, -0.4F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.6632F, 0F, 0F));
        PartDefinition p_water_back_fin_1_ray_1 = p_water_back_fin_1.addOrReplaceChild("water_back_fin_1_ray_1", CubeListBuilder.create()
                .texOffs(68, 225).addBox(-0.5F, -1F, -0.4F, 1F, 2F, 4F, new CubeDeformation(-0.25F, 0F, -0.4F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.1396F, 0F, 0F));
        PartDefinition p_water_back_fin_2 = p_water_chest_body.addOrReplaceChild("water_back_fin_2", CubeListBuilder.create(),
                PartPose.offset(0F, 6.2F, 2.9F));
        PartDefinition p_water_back_fin_2_ray_0 = p_water_back_fin_2.addOrReplaceChild("water_back_fin_2_ray_0", CubeListBuilder.create()
                .texOffs(187, 196).addBox(-0.5F, -1.5F, -0.3F, 1F, 3F, 4F, new CubeDeformation(-0.25F, -0.4F, -0.3F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.6632F, 0F, 0F));
        PartDefinition p_water_back_fin_2_ray_1 = p_water_back_fin_2.addOrReplaceChild("water_back_fin_2_ray_1", CubeListBuilder.create()
                .texOffs(144, 250).addBox(-0.5F, -1F, -0.14F, 1F, 2F, 3F, new CubeDeformation(-0.25F, 0F, -0.14F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.1396F, 0F, 0F));
        PartDefinition p_water_back_fin_3 = p_water_chest_body.addOrReplaceChild("water_back_fin_3", CubeListBuilder.create(),
                PartPose.offset(0F, 9F, 2.9F));
        PartDefinition p_water_back_fin_3_ray_0 = p_water_back_fin_3.addOrReplaceChild("water_back_fin_3_ray_0", CubeListBuilder.create()
                .texOffs(175, 225).addBox(-0.5F, -1.5F, -0.2F, 1F, 3F, 3F, new CubeDeformation(-0.25F, -0.4F, -0.2F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.6632F, 0F, 0F));
        PartDefinition p_water_back_fin_3_ray_1 = p_water_back_fin_3.addOrReplaceChild("water_back_fin_3_ray_1", CubeListBuilder.create()
                .texOffs(153, 250).addBox(-0.5F, -1F, -0.46F, 1F, 2F, 3F, new CubeDeformation(-0.25F, 0F, -0.46F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.1396F, 0F, 0F));
        PartDefinition p_water_chest_left_arm = root.addOrReplaceChild("water_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_water_pauldron_left_arm = p_water_chest_left_arm.addOrReplaceChild("water_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(124, 82).addBox(-3.3F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F))
                .texOffs(116, 115).addBox(-2F, -0.1F, -4F, 6F, 2F, 8F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.2094F));
        PartDefinition p_water_claw_left_arm_0 = p_water_pauldron_left_arm.addOrReplaceChild("water_claw_left_arm_0", CubeListBuilder.create()
                .texOffs(216, 271).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.285F, -0.35F)),
                PartPose.offsetAndRotation(2.4F, -1.4F, 0F, 0F, 0F, 0.6981F));
        PartDefinition p_water_claw_left_arm_1 = p_water_claw_left_arm_0.addOrReplaceChild("water_claw_left_arm_1", CubeListBuilder.create()
                .texOffs(235, 302).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.35F, -0.1425F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_water_chest_right_arm = root.addOrReplaceChild("water_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_water_pauldron_right_arm = p_water_chest_right_arm.addOrReplaceChild("water_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(155, 82).addBox(-3.7F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F))
                .texOffs(145, 115).addBox(-4F, -0.1F, -4F, 6F, 2F, 8F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.2094F));
        PartDefinition p_water_claw_right_arm_0 = p_water_pauldron_right_arm.addOrReplaceChild("water_claw_right_arm_0", CubeListBuilder.create()
                .texOffs(225, 271).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.285F, -0.35F)),
                PartPose.offsetAndRotation(-2.4F, -1.4F, 0F, 0F, 0F, -0.6981F));
        PartDefinition p_water_claw_right_arm_1 = p_water_claw_right_arm_0.addOrReplaceChild("water_claw_right_arm_1", CubeListBuilder.create()
                .texOffs(240, 302).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.35F, -0.1425F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_water_legs_body = root.addOrReplaceChild("water_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_water_tail_0 = p_water_legs_body.addOrReplaceChild("water_tail_0", CubeListBuilder.create()
                .texOffs(169, 218).addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F),
                PartPose.offsetAndRotation(0F, 10.2F, 2.2F, 0.5236F, 0F, 0F));
        PartDefinition p_water_tail_1 = p_water_tail_0.addOrReplaceChild("water_tail_1", CubeListBuilder.create()
                .texOffs(182, 218).addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, 2.75F, 0F, 0.1745F, 0F, 0F));
        PartDefinition p_water_tail_2 = p_water_tail_1.addOrReplaceChild("water_tail_2", CubeListBuilder.create()
                .texOffs(195, 218).addBox(-1.5F, -0.1F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.1F, -0.4F)),
                PartPose.offsetAndRotation(0F, 2.75F, 0F, 0.2094F, 0F, 0F));
        PartDefinition p_water_tail_3 = p_water_tail_2.addOrReplaceChild("water_tail_3", CubeListBuilder.create()
                .texOffs(162, 250).addBox(-1F, -0.2F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.15F, -0.2F, -0.15F)),
                PartPose.offsetAndRotation(0F, 2.55F, 0F, 0.2094F, 0F, 0F));
        PartDefinition p_water_tail_4 = p_water_tail_3.addOrReplaceChild("water_tail_4", CubeListBuilder.create()
                .texOffs(171, 250).addBox(-1F, -0.4F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.4F, -0.4F, -0.4F)),
                PartPose.offsetAndRotation(0F, 2.35F, 0F, 0.1745F, 0F, 0F));
        PartDefinition p_water_tail_tip = p_water_tail_4.addOrReplaceChild("water_tail_tip", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0F, 2.4F, 0F, 0.1396F, 0F, 0F));
        PartDefinition p_water_tail_fin = p_water_tail_tip.addOrReplaceChild("water_tail_fin", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0F, 0.6F, 0F, 1.5708F, 0F, 0F));
        PartDefinition p_water_tail_fin_ray_0 = p_water_tail_fin.addOrReplaceChild("water_tail_fin_ray_0", CubeListBuilder.create()
                .texOffs(79, 225).addBox(-0.5F, -1F, -0.2F, 1F, 2F, 4F, new CubeDeformation(-0.25F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.6981F, 0F, 0F));
        PartDefinition p_water_tail_fin_ray_1 = p_water_tail_fin.addOrReplaceChild("water_tail_fin_ray_1", CubeListBuilder.create()
                .texOffs(90, 225).addBox(-0.5F, -1F, 0F, 1F, 2F, 4F, new CubeDeformation(-0.25F, -0.2F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.1745F, 0F, 0F));
        PartDefinition p_water_tail_fin_ray_2 = p_water_tail_fin.addOrReplaceChild("water_tail_fin_ray_2", CubeListBuilder.create()
                .texOffs(101, 225).addBox(-0.5F, -1F, -0.2F, 1F, 2F, 4F, new CubeDeformation(-0.25F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0.3491F, 0F, 0F));
        PartDefinition p_water_legs_left_leg = root.addOrReplaceChild("water_legs_left_leg", CubeListBuilder.create()
                .texOffs(176, 261).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_water_legs_right_leg = root.addOrReplaceChild("water_legs_right_leg", CubeListBuilder.create()
                .texOffs(187, 261).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_water_feet_left_leg = root.addOrReplaceChild("water_feet_left_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_water_talon_left_leg_0 = p_water_feet_left_leg.addOrReplaceChild("water_talon_left_leg_0", CubeListBuilder.create()
                .texOffs(14, 294).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(-1.6F, 11.6F, -2.8F, 0.4887F, -0.2234F, 0F));
        PartDefinition p_water_talon_left_leg_1 = p_water_feet_left_leg.addOrReplaceChild("water_talon_left_leg_1", CubeListBuilder.create()
                .texOffs(21, 294).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(0F, 11.6F, -2.8F, 0.4887F, 0F, 0F));
        PartDefinition p_water_talon_left_leg_2 = p_water_feet_left_leg.addOrReplaceChild("water_talon_left_leg_2", CubeListBuilder.create()
                .texOffs(28, 294).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(1.6F, 11.6F, -2.8F, 0.4887F, 0.2234F, 0F));
        PartDefinition p_water_feet_right_leg = root.addOrReplaceChild("water_feet_right_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_water_talon_right_leg_0 = p_water_feet_right_leg.addOrReplaceChild("water_talon_right_leg_0", CubeListBuilder.create()
                .texOffs(35, 294).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(-1.6F, 11.6F, -2.8F, 0.4887F, -0.2234F, 0F));
        PartDefinition p_water_talon_right_leg_1 = p_water_feet_right_leg.addOrReplaceChild("water_talon_right_leg_1", CubeListBuilder.create()
                .texOffs(42, 294).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(0F, 11.6F, -2.8F, 0.4887F, 0F, 0F));
        PartDefinition p_water_talon_right_leg_2 = p_water_feet_right_leg.addOrReplaceChild("water_talon_right_leg_2", CubeListBuilder.create()
                .texOffs(49, 294).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(1.6F, 11.6F, -2.8F, 0.4887F, 0.2234F, 0F));
        PartDefinition p_crown_head_head = root.addOrReplaceChild("crown_head_head", CubeListBuilder.create()
                .texOffs(27, 286).addBox(-5.5F, -10F, -5.5F, 11F, 2F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.1F))
                .texOffs(52, 286).addBox(-5.5F, -10F, 4.5F, 11F, 2F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.1F))
                .texOffs(143, 21).addBox(-5.5F, -10F, -5F, 1F, 2F, 10F, new CubeDeformation(-0.1F, -0.1F, -0.4F))
                .texOffs(166, 21).addBox(4.5F, -10F, -5F, 1F, 2F, 10F, new CubeDeformation(-0.1F, -0.1F, -0.4F))
                .texOffs(56, 294).addBox(-1F, -10F, -6.1F, 2F, 2F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.2F))
                .texOffs(30, 313).addBox(-4F, -9.5F, -6.05F, 1F, 1F, 1F, new CubeDeformation(0F, -0.1F, -0.25F))
                .texOffs(35, 313).addBox(3F, -9.5F, -6.05F, 1F, 1F, 1F, new CubeDeformation(0F, -0.1F, -0.25F))
                .texOffs(63, 294).addBox(-6.1F, -9.5F, -1F, 1F, 1F, 2F, new CubeDeformation(-0.2F, 0F, -0.4F))
                .texOffs(70, 294).addBox(5.1F, -9.5F, -1F, 1F, 1F, 2F, new CubeDeformation(-0.2F, 0F, -0.4F))
                .texOffs(112, 310).addBox(-1F, -9.5F, 5.1F, 2F, 1F, 1F, new CubeDeformation(-0.4F, 0F, -0.2F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_crown_point_0_0 = p_crown_head_head.addOrReplaceChild("crown_point_0_0", CubeListBuilder.create()
                .texOffs(77, 294).addBox(-1F, -1.96F, -0.5F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.04F, -0.1F)),
                PartPose.offset(0F, -9.7F, -5.4F));
        PartDefinition p_crown_point_0_1 = p_crown_point_0_0.addOrReplaceChild("crown_point_0_1", CubeListBuilder.create()
                .texOffs(245, 302).addBox(-0.5F, -1.8F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.15F, -0.2F, -0.25F))
                .texOffs(40, 313).addBox(-0.5F, -2.4F, -0.5F, 1F, 1F, 1F),
                PartPose.offset(0F, -1.67F, 0F));
        PartDefinition p_crown_point_1_0 = p_crown_head_head.addOrReplaceChild("crown_point_1_0", CubeListBuilder.create()
                .texOffs(84, 294).addBox(-1F, -1.66F, -0.5F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.34F, -0.1F)),
                PartPose.offset(-3.2F, -9.7F, -5.4F));
        PartDefinition p_crown_point_1_1 = p_crown_point_1_0.addOrReplaceChild("crown_point_1_1", CubeListBuilder.create()
                .texOffs(250, 302).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.15F, -0.45F, -0.25F))
                .texOffs(45, 313).addBox(-0.5F, -1.9F, -0.5F, 1F, 1F, 1F),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_crown_point_2_0 = p_crown_head_head.addOrReplaceChild("crown_point_2_0", CubeListBuilder.create()
                .texOffs(91, 294).addBox(-1F, -1.66F, -0.5F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.34F, -0.1F)),
                PartPose.offset(3.2F, -9.7F, -5.4F));
        PartDefinition p_crown_point_2_1 = p_crown_point_2_0.addOrReplaceChild("crown_point_2_1", CubeListBuilder.create()
                .texOffs(0, 306).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.15F, -0.45F, -0.25F))
                .texOffs(50, 313).addBox(-0.5F, -1.9F, -0.5F, 1F, 1F, 1F),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_crown_point_3_0 = p_crown_head_head.addOrReplaceChild("crown_point_3_0", CubeListBuilder.create()
                .texOffs(98, 294).addBox(-1F, -1.72F, -0.5F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.28F, -0.1F)),
                PartPose.offset(0F, -9.7F, 5.4F));
        PartDefinition p_crown_point_3_1 = p_crown_point_3_0.addOrReplaceChild("crown_point_3_1", CubeListBuilder.create()
                .texOffs(5, 306).addBox(-0.5F, -1.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.15F, -0.4F, -0.25F))
                .texOffs(55, 313).addBox(-0.5F, -2F, -0.5F, 1F, 1F, 1F),
                PartPose.offset(0F, -1.19F, 0F));
        PartDefinition p_crown_point_4_0 = p_crown_head_head.addOrReplaceChild("crown_point_4_0", CubeListBuilder.create()
                .texOffs(105, 294).addBox(-1F, -1.54F, -0.5F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.46F, -0.1F)),
                PartPose.offset(-3.2F, -9.7F, 5.4F));
        PartDefinition p_crown_point_4_1 = p_crown_point_4_0.addOrReplaceChild("crown_point_4_1", CubeListBuilder.create()
                .texOffs(60, 313).addBox(-0.5F, -0.95F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.15F, -0.05F, -0.25F))
                .texOffs(65, 313).addBox(-0.5F, -1.7F, -0.5F, 1F, 1F, 1F),
                PartPose.offset(0F, -0.83F, 0F));
        PartDefinition p_crown_point_5_0 = p_crown_head_head.addOrReplaceChild("crown_point_5_0", CubeListBuilder.create()
                .texOffs(112, 294).addBox(-1F, -1.54F, -0.5F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.46F, -0.1F)),
                PartPose.offset(3.2F, -9.7F, 5.4F));
        PartDefinition p_crown_point_5_1 = p_crown_point_5_0.addOrReplaceChild("crown_point_5_1", CubeListBuilder.create()
                .texOffs(70, 313).addBox(-0.5F, -0.95F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.15F, -0.05F, -0.25F))
                .texOffs(75, 313).addBox(-0.5F, -1.7F, -0.5F, 1F, 1F, 1F),
                PartPose.offset(0F, -0.83F, 0F));
        PartDefinition p_crown_point_6_0 = p_crown_head_head.addOrReplaceChild("crown_point_6_0", CubeListBuilder.create()
                .texOffs(234, 271).addBox(-1F, -1.6F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.4F, -0.3F)),
                PartPose.offset(-5.4F, -9.7F, -1.8F));
        PartDefinition p_crown_point_6_1 = p_crown_point_6_0.addOrReplaceChild("crown_point_6_1", CubeListBuilder.create()
                .texOffs(80, 313).addBox(-0.5F, -1F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.15F, 0F, -0.15F))
                .texOffs(85, 313).addBox(-0.5F, -1.8F, -0.5F, 1F, 1F, 1F),
                PartPose.offset(0F, -0.95F, 0F));
        PartDefinition p_crown_point_7_0 = p_crown_head_head.addOrReplaceChild("crown_point_7_0", CubeListBuilder.create()
                .texOffs(243, 271).addBox(-1F, -1.6F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.4F, -0.3F)),
                PartPose.offset(-5.4F, -9.7F, 1.8F));
        PartDefinition p_crown_point_7_1 = p_crown_point_7_0.addOrReplaceChild("crown_point_7_1", CubeListBuilder.create()
                .texOffs(90, 313).addBox(-0.5F, -1F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.15F, 0F, -0.15F))
                .texOffs(95, 313).addBox(-0.5F, -1.8F, -0.5F, 1F, 1F, 1F),
                PartPose.offset(0F, -0.95F, 0F));
        PartDefinition p_crown_point_8_0 = p_crown_head_head.addOrReplaceChild("crown_point_8_0", CubeListBuilder.create()
                .texOffs(0, 276).addBox(-1F, -1.6F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.4F, -0.3F)),
                PartPose.offset(5.4F, -9.7F, -1.8F));
        PartDefinition p_crown_point_8_1 = p_crown_point_8_0.addOrReplaceChild("crown_point_8_1", CubeListBuilder.create()
                .texOffs(100, 313).addBox(-0.5F, -1F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.15F, 0F, -0.15F))
                .texOffs(105, 313).addBox(-0.5F, -1.8F, -0.5F, 1F, 1F, 1F),
                PartPose.offset(0F, -0.95F, 0F));
        PartDefinition p_crown_point_9_0 = p_crown_head_head.addOrReplaceChild("crown_point_9_0", CubeListBuilder.create()
                .texOffs(9, 276).addBox(-1F, -1.6F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.4F, -0.3F)),
                PartPose.offset(5.4F, -9.7F, 1.8F));
        PartDefinition p_crown_point_9_1 = p_crown_point_9_0.addOrReplaceChild("crown_point_9_1", CubeListBuilder.create()
                .texOffs(110, 313).addBox(-0.5F, -1F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.15F, 0F, -0.15F))
                .texOffs(115, 313).addBox(-0.5F, -1.8F, -0.5F, 1F, 1F, 1F),
                PartPose.offset(0F, -0.95F, 0F));
        PartDefinition p_leather_head_head = root.addOrReplaceChild("leather_head_head", CubeListBuilder.create()
                .texOffs(100, 238).addBox(4.2F, -7.5F, -0.1F, 2F, 2F, 3F, new CubeDeformation(-0.4F, -0.1F, -0.3F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_leather_feather = p_leather_head_head.addOrReplaceChild("leather_feather", CubeListBuilder.create()
                .texOffs(223, 21).addBox(-0.5F, -8.6F, -1.5F, 1F, 9F, 3F, new CubeDeformation(-0.2F, -0.4F, -0.4F)),
                PartPose.offsetAndRotation(5F, -6.4F, 1.4F, -0.5236F, 0F, 0.2793F));
        PartDefinition p_leather_chest_body = root.addOrReplaceChild("leather_chest_body", CubeListBuilder.create()
                .texOffs(0, 35).addBox(-6F, -1.9F, -4F, 12F, 3F, 8F, new CubeDeformation(-0.25F, 0.05F, -0.15F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_leather_chest_left_arm = root.addOrReplaceChild("leather_chest_left_arm", CubeListBuilder.create()
                .texOffs(174, 115).addBox(-2.45F, -4.4F, -3.5F, 7F, 3F, 7F, new CubeDeformation(0.05F, -0.3F, 0.1F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_leather_chest_right_arm = root.addOrReplaceChild("leather_chest_right_arm", CubeListBuilder.create()
                .texOffs(203, 115).addBox(-4.55F, -4.4F, -3.5F, 7F, 3F, 7F, new CubeDeformation(0.05F, -0.3F, 0.1F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_leather_legs_body = root.addOrReplaceChild("leather_legs_body", CubeListBuilder.create()
                .texOffs(43, 168).addBox(-5F, 10.2F, -3F, 10F, 2F, 6F, new CubeDeformation(-0.1F, -0.4F, -0.1F))
                .texOffs(111, 238).addBox(2.1F, 10.7F, -4F, 3F, 3F, 2F, new CubeDeformation(-0.3F, -0.2F, -0.4F))
                .texOffs(119, 294).addBox(-4.5F, 10.2F, -3.5F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.2F, -0.2F)),
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
                .texOffs(58, 187).addBox(-5.5F, -3.9F, 4.6F, 11F, 6F, 1F, new CubeDeformation(-0.2F, -0.3F, -0.2F))
                .texOffs(99, 0).addBox(-5.8F, -3.9F, -4.7F, 1F, 6F, 10F, new CubeDeformation(-0.2F, -0.3F, -0.3F))
                .texOffs(122, 0).addBox(4.8F, -3.9F, -4.7F, 1F, 6F, 10F, new CubeDeformation(-0.2F, -0.3F, -0.3F))
                .texOffs(251, 250).addBox(-0.5F, -6F, -5.85F, 1F, 4F, 1F, new CubeDeformation(0F, 0F, -0.15F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_chainmail_chest_body = root.addOrReplaceChild("chainmail_chest_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_chainmail_chest_left_arm = root.addOrReplaceChild("chainmail_chest_left_arm", CubeListBuilder.create()
                .texOffs(76, 168).addBox(-2.4F, -3.7F, -3.5F, 7F, 1F, 7F, new CubeDeformation(-0.2F, 0F, 0F))
                .texOffs(189, 21).addBox(3.7F, -3.6F, -3.5F, 1F, 5F, 7F, new CubeDeformation(-0.2F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_chainmail_chest_right_arm = root.addOrReplaceChild("chainmail_chest_right_arm", CubeListBuilder.create()
                .texOffs(105, 168).addBox(-4.6F, -3.7F, -3.5F, 7F, 1F, 7F, new CubeDeformation(-0.2F, 0F, 0F))
                .texOffs(206, 21).addBox(-4.7F, -3.6F, -3.5F, 1F, 5F, 7F, new CubeDeformation(-0.2F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_chainmail_legs_body = root.addOrReplaceChild("chainmail_legs_body", CubeListBuilder.create()
                .texOffs(226, 196).addBox(-5F, 9.8F, -3.2F, 10F, 5F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(0, 204).addBox(-5F, 9.8F, 2.2F, 10F, 5F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.2F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_chainmail_legs_left_leg = root.addOrReplaceChild("chainmail_legs_left_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_chainmail_legs_right_leg = root.addOrReplaceChild("chainmail_legs_right_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_iron_head_head = root.addOrReplaceChild("iron_head_head", CubeListBuilder.create()
                .texOffs(220, 225).addBox(-1F, -6.7F, -5.9F, 2F, 5F, 1F, new CubeDeformation(-0.4F, -0.3F, -0.1F))
                .texOffs(99, 47).addBox(-1F, -10.45F, -4.5F, 2F, 2F, 9F, new CubeDeformation(-0.4F, -0.45F, -0.1F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_iron_chest_body = root.addOrReplaceChild("iron_chest_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_iron_chest_left_arm = root.addOrReplaceChild("iron_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_iron_pauldron_left_arm = p_iron_chest_left_arm.addOrReplaceChild("iron_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(213, 137).addBox(-3.2F, -1.5F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.2F, -0.1F, 0F))
                .texOffs(0, 126).addBox(-1.9F, 0.2F, -4F, 6F, 2F, 8F, new CubeDeformation(-0.1F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.2094F));
        PartDefinition p_iron_chest_right_arm = root.addOrReplaceChild("iron_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_iron_pauldron_right_arm = p_iron_chest_right_arm.addOrReplaceChild("iron_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(0, 148).addBox(-3.8F, -1.5F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.2F, -0.1F, 0F))
                .texOffs(29, 126).addBox(-4.1F, 0.2F, -4F, 6F, 2F, 8F, new CubeDeformation(-0.1F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.2094F));
        PartDefinition p_iron_legs_body = root.addOrReplaceChild("iron_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_iron_legs_left_leg = root.addOrReplaceChild("iron_legs_left_leg", CubeListBuilder.create()
                .texOffs(198, 261).addBox(-2F, 5F, -3.2F, 4F, 3F, 1F, new CubeDeformation(-0.1F, -0.1F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_iron_legs_right_leg = root.addOrReplaceChild("iron_legs_right_leg", CubeListBuilder.create()
                .texOffs(209, 261).addBox(-2F, 5F, -3.2F, 4F, 3F, 1F, new CubeDeformation(-0.1F, -0.1F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_iron_feet_left_leg = root.addOrReplaceChild("iron_feet_left_leg", CubeListBuilder.create()
                .texOffs(104, 232).addBox(-2.5F, 10.25F, -4.35F, 5F, 3F, 2F, new CubeDeformation(-0.1F, -0.35F, -0.35F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_iron_feet_right_leg = root.addOrReplaceChild("iron_feet_right_leg", CubeListBuilder.create()
                .texOffs(119, 232).addBox(-2.5F, 10.25F, -4.35F, 5F, 3F, 2F, new CubeDeformation(-0.1F, -0.35F, -0.35F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_gold_head_head = root.addOrReplaceChild("gold_head_head", CubeListBuilder.create()
                .texOffs(126, 294).addBox(-1F, -7.2F, -5.85F, 2F, 2F, 1F, new CubeDeformation(0F, 0F, -0.15F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_gold_ray_0_0 = p_gold_head_head.addOrReplaceChild("gold_ray_0_0", CubeListBuilder.create()
                .texOffs(18, 276).addBox(-1F, -1.935F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.065F, -0.35F)),
                PartPose.offsetAndRotation(0F, -9.2F, -1F, -0.384F, 0F, -0.5236F));
        PartDefinition p_gold_ray_0_1 = p_gold_ray_0_0.addOrReplaceChild("gold_ray_0_1", CubeListBuilder.create()
                .texOffs(10, 306).addBox(-0.5F, -1.85F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.15F, -0.1425F)),
                PartPose.offset(0F, -1.62F, 0F));
        PartDefinition p_gold_ray_1_0 = p_gold_head_head.addOrReplaceChild("gold_ray_1_0", CubeListBuilder.create()
                .texOffs(180, 250).addBox(-1F, -2.765F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.35F, -0.235F, -0.35F)),
                PartPose.offsetAndRotation(0F, -9.2F, -1F, -0.1745F, 0F, 0F));
        PartDefinition p_gold_ray_1_1 = p_gold_ray_1_0.addOrReplaceChild("gold_ray_1_1", CubeListBuilder.create()
                .texOffs(164, 281).addBox(-0.5F, -2.65F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.1425F, -0.35F, -0.1425F)),
                PartPose.offset(0F, -2.28F, 0F));
        PartDefinition p_gold_ray_2_0 = p_gold_head_head.addOrReplaceChild("gold_ray_2_0", CubeListBuilder.create()
                .texOffs(27, 276).addBox(-1F, -1.935F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.065F, -0.35F)),
                PartPose.offsetAndRotation(0F, -9.2F, -1F, 0.0349F, 0F, 0.5236F));
        PartDefinition p_gold_ray_2_1 = p_gold_ray_2_0.addOrReplaceChild("gold_ray_2_1", CubeListBuilder.create()
                .texOffs(15, 306).addBox(-0.5F, -1.85F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.15F, -0.1425F)),
                PartPose.offset(0F, -1.62F, 0F));
        PartDefinition p_gold_chest_body = root.addOrReplaceChild("gold_chest_body", CubeListBuilder.create()
                .texOffs(122, 238).addBox(-2F, 2.2F, -3.85F, 4F, 4F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.15F))
                .texOffs(133, 294).addBox(-1F, 3.2F, -4.35F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.3F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_gold_chest_left_arm = root.addOrReplaceChild("gold_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_gold_pauldron_left_arm = p_gold_chest_left_arm.addOrReplaceChild("gold_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(186, 82).addBox(-3.2F, -1.6F, -4F, 7F, 2F, 8F, new CubeDeformation(0F, 0F, -0.4F))
                .texOffs(217, 82).addBox(-2.4F, 0.1F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.4F, -0.2F, -0.3F))
                .texOffs(106, 286).addBox(1.1F, -2.3F, -1F, 2F, 1F, 2F, new CubeDeformation(-0.3F, -0.1F, -0.3F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.2443F));
        PartDefinition p_gold_chest_right_arm = root.addOrReplaceChild("gold_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_gold_pauldron_right_arm = p_gold_chest_right_arm.addOrReplaceChild("gold_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(0, 93).addBox(-3.8F, -1.6F, -4F, 7F, 2F, 8F, new CubeDeformation(0F, 0F, -0.4F))
                .texOffs(31, 93).addBox(-4.6F, 0.1F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.4F, -0.2F, -0.3F))
                .texOffs(115, 286).addBox(-3.1F, -2.3F, -1F, 2F, 1F, 2F, new CubeDeformation(-0.3F, -0.1F, -0.3F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.2443F));
        PartDefinition p_gold_legs_body = root.addOrReplaceChild("gold_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_gold_legs_left_leg = root.addOrReplaceChild("gold_legs_left_leg", CubeListBuilder.create()
                .texOffs(226, 204).addBox(-3F, -0.9F, -3.3F, 6F, 5F, 1F, new CubeDeformation(-0.3F, -0.3F, 0F))
                .texOffs(220, 261).addBox(-2F, 5F, -3.2F, 4F, 3F, 1F, new CubeDeformation(-0.1F, -0.1F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_gold_legs_right_leg = root.addOrReplaceChild("gold_legs_right_leg", CubeListBuilder.create()
                .texOffs(241, 204).addBox(-3F, -0.9F, -3.3F, 6F, 5F, 1F, new CubeDeformation(-0.3F, -0.3F, 0F))
                .texOffs(231, 261).addBox(-2F, 5F, -3.2F, 4F, 3F, 1F, new CubeDeformation(-0.1F, -0.1F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_gold_feet_left_leg = root.addOrReplaceChild("gold_feet_left_leg", CubeListBuilder.create()
                .texOffs(134, 232).addBox(-2.5F, 10.25F, -4.35F, 5F, 3F, 2F, new CubeDeformation(-0.1F, -0.35F, -0.35F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_gold_feet_right_leg = root.addOrReplaceChild("gold_feet_right_leg", CubeListBuilder.create()
                .texOffs(149, 232).addBox(-2.5F, 10.25F, -4.35F, 5F, 3F, 2F, new CubeDeformation(-0.1F, -0.35F, -0.35F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_diamond_head_head = root.addOrReplaceChild("diamond_head_head", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_diamond_crest_0_0 = p_diamond_head_head.addOrReplaceChild("diamond_crest_0_0", CubeListBuilder.create()
                .texOffs(36, 276).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.2F, -0.285F, -0.2F)),
                PartPose.offsetAndRotation(0F, -9.2F, -3F, -0.3491F, 0F, 0F));
        PartDefinition p_diamond_crest_0_1 = p_diamond_crest_0_0.addOrReplaceChild("diamond_crest_0_1", CubeListBuilder.create()
                .texOffs(20, 306).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.06F, -0.35F, -0.06F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_diamond_crest_1_0 = p_diamond_head_head.addOrReplaceChild("diamond_crest_1_0", CubeListBuilder.create()
                .texOffs(45, 276).addBox(-1F, -1.99F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.2F, -0.01F, -0.2F)),
                PartPose.offsetAndRotation(0F, -9.2F, -0.4F, -0.3491F, 0F, 0F));
        PartDefinition p_diamond_crest_1_1 = p_diamond_crest_1_0.addOrReplaceChild("diamond_crest_1_1", CubeListBuilder.create()
                .texOffs(25, 306).addBox(-0.5F, -1.9F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.06F, -0.1F, -0.06F)),
                PartPose.offset(0F, -1.73F, 0F));
        PartDefinition p_diamond_crest_2_0 = p_diamond_head_head.addOrReplaceChild("diamond_crest_2_0", CubeListBuilder.create()
                .texOffs(54, 276).addBox(-1F, -1.77F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.2F, -0.23F, -0.2F)),
                PartPose.offsetAndRotation(0F, -9.2F, 2.2F, -0.3491F, 0F, 0F));
        PartDefinition p_diamond_crest_2_1 = p_diamond_crest_2_0.addOrReplaceChild("diamond_crest_2_1", CubeListBuilder.create()
                .texOffs(30, 306).addBox(-0.5F, -1.7F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.06F, -0.3F, -0.06F)),
                PartPose.offset(0F, -1.29F, 0F));
        PartDefinition p_diamond_chest_body = root.addOrReplaceChild("diamond_chest_body", CubeListBuilder.create()
                .texOffs(73, 281).addBox(-1F, 1.8F, -3.9F, 2F, 3F, 1F, new CubeDeformation(0F, -0.2F, -0.1F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_diamond_chest_left_arm = root.addOrReplaceChild("diamond_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_diamond_pauldron_left_arm = p_diamond_chest_left_arm.addOrReplaceChild("diamond_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(29, 148).addBox(-3.2F, -1.6F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.2F, -0.2F, 0F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.1745F));
        PartDefinition p_diamond_shoulder_left_arm_0_0 = p_diamond_pauldron_left_arm.addOrReplaceChild("diamond_shoulder_left_arm_0_0", CubeListBuilder.create()
                .texOffs(63, 276).addBox(-1F, -1.88F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.25F, -0.12F, -0.25F)),
                PartPose.offsetAndRotation(1.8F, -1.3F, -1F, 0.1396F, 0F, 0.4189F));
        PartDefinition p_diamond_shoulder_left_arm_0_1 = p_diamond_shoulder_left_arm_0_0.addOrReplaceChild("diamond_shoulder_left_arm_0_1", CubeListBuilder.create()
                .texOffs(35, 306).addBox(-0.5F, -1.8F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.0875F, -0.2F, -0.0875F)),
                PartPose.offset(0F, -1.51F, 0F));
        PartDefinition p_diamond_shoulder_left_arm_1_0 = p_diamond_pauldron_left_arm.addOrReplaceChild("diamond_shoulder_left_arm_1_0", CubeListBuilder.create()
                .texOffs(72, 276).addBox(-1F, -1.605F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.395F, -0.45F)),
                PartPose.offsetAndRotation(2.6F, -1.3F, 1.6F, -0.1745F, 0F, 0.6632F));
        PartDefinition p_diamond_shoulder_left_arm_1_1 = p_diamond_shoulder_left_arm_1_0.addOrReplaceChild("diamond_shoulder_left_arm_1_1", CubeListBuilder.create()
                .texOffs(40, 306).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1975F, -0.45F, -0.1975F)),
                PartPose.offset(0F, -0.96F, 0F));
        PartDefinition p_diamond_chest_right_arm = root.addOrReplaceChild("diamond_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_diamond_pauldron_right_arm = p_diamond_chest_right_arm.addOrReplaceChild("diamond_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(58, 148).addBox(-3.8F, -1.6F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.2F, -0.2F, 0F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.1745F));
        PartDefinition p_diamond_shoulder_right_arm_0_0 = p_diamond_pauldron_right_arm.addOrReplaceChild("diamond_shoulder_right_arm_0_0", CubeListBuilder.create()
                .texOffs(81, 276).addBox(-1F, -1.88F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.25F, -0.12F, -0.25F)),
                PartPose.offsetAndRotation(-1.8F, -1.3F, -1F, 0.1396F, 0F, -0.4189F));
        PartDefinition p_diamond_shoulder_right_arm_0_1 = p_diamond_shoulder_right_arm_0_0.addOrReplaceChild("diamond_shoulder_right_arm_0_1", CubeListBuilder.create()
                .texOffs(45, 306).addBox(-0.5F, -1.8F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.0875F, -0.2F, -0.0875F)),
                PartPose.offset(0F, -1.51F, 0F));
        PartDefinition p_diamond_shoulder_right_arm_1_0 = p_diamond_pauldron_right_arm.addOrReplaceChild("diamond_shoulder_right_arm_1_0", CubeListBuilder.create()
                .texOffs(90, 276).addBox(-1F, -1.605F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.395F, -0.45F)),
                PartPose.offsetAndRotation(-2.6F, -1.3F, 1.6F, -0.1745F, 0F, -0.6632F));
        PartDefinition p_diamond_shoulder_right_arm_1_1 = p_diamond_shoulder_right_arm_1_0.addOrReplaceChild("diamond_shoulder_right_arm_1_1", CubeListBuilder.create()
                .texOffs(50, 306).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1975F, -0.45F, -0.1975F)),
                PartPose.offset(0F, -0.96F, 0F));
        PartDefinition p_diamond_legs_body = root.addOrReplaceChild("diamond_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_diamond_legs_left_leg = root.addOrReplaceChild("diamond_legs_left_leg", CubeListBuilder.create()
                .texOffs(140, 294).addBox(-1F, 5.4F, -3.5F, 2F, 2F, 1F, new CubeDeformation(0F, 0F, -0.1F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_diamond_legs_right_leg = root.addOrReplaceChild("diamond_legs_right_leg", CubeListBuilder.create()
                .texOffs(147, 294).addBox(-1F, 5.4F, -3.5F, 2F, 2F, 1F, new CubeDeformation(0F, 0F, -0.1F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_diamond_feet_left_leg = root.addOrReplaceChild("diamond_feet_left_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_diamond_heel_left_leg_0 = p_diamond_feet_left_leg.addOrReplaceChild("diamond_heel_left_leg_0", CubeListBuilder.create()
                .texOffs(99, 276).addBox(-1F, -1.605F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.395F, -0.4F)),
                PartPose.offsetAndRotation(0F, 10.9F, 2.8F, -0.9774F, 0F, 0F));
        PartDefinition p_diamond_heel_left_leg_1 = p_diamond_heel_left_leg_0.addOrReplaceChild("diamond_heel_left_leg_1", CubeListBuilder.create()
                .texOffs(55, 306).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.17F, -0.45F, -0.17F)),
                PartPose.offset(0F, -0.96F, 0F));
        PartDefinition p_diamond_feet_right_leg = root.addOrReplaceChild("diamond_feet_right_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_diamond_heel_right_leg_0 = p_diamond_feet_right_leg.addOrReplaceChild("diamond_heel_right_leg_0", CubeListBuilder.create()
                .texOffs(108, 276).addBox(-1F, -1.605F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.395F, -0.4F)),
                PartPose.offsetAndRotation(0F, 10.9F, 2.8F, -0.9774F, 0F, 0F));
        PartDefinition p_diamond_heel_right_leg_1 = p_diamond_heel_right_leg_0.addOrReplaceChild("diamond_heel_right_leg_1", CubeListBuilder.create()
                .texOffs(60, 306).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.17F, -0.45F, -0.17F)),
                PartPose.offset(0F, -0.96F, 0F));
        PartDefinition p_netherite_head_head = root.addOrReplaceChild("netherite_head_head", CubeListBuilder.create()
                .texOffs(25, 310).addBox(-5F, -6.3F, -5.85F, 10F, 1F, 1F, new CubeDeformation(-0.4F, -0.1F, -0.15F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_netherite_horn_left_0 = p_netherite_head_head.addOrReplaceChild("netherite_horn_left_0", CubeListBuilder.create()
                .texOffs(117, 276).addBox(-1F, -2F, -1F, 2F, 2F, 2F),
                PartPose.offsetAndRotation(4.4F, -7.6F, -2.6F, 0F, 0F, 1.2217F));
        PartDefinition p_netherite_horn_left_1 = p_netherite_horn_left_0.addOrReplaceChild("netherite_horn_left_1", CubeListBuilder.create()
                .texOffs(126, 276).addBox(-1F, -2F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, -1.75F, 0F, 0.5236F, 0F, -0.5236F));
        PartDefinition p_netherite_horn_left_2 = p_netherite_horn_left_1.addOrReplaceChild("netherite_horn_left_2", CubeListBuilder.create()
                .texOffs(135, 276).addBox(-1F, -1.9F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.1F, -0.45F)),
                PartPose.offsetAndRotation(0F, -1.75F, 0F, 0.5934F, 0F, -0.4189F));
        PartDefinition p_netherite_horn_right_0 = p_netherite_head_head.addOrReplaceChild("netherite_horn_right_0", CubeListBuilder.create()
                .texOffs(144, 276).addBox(-1F, -2F, -1F, 2F, 2F, 2F),
                PartPose.offsetAndRotation(-4.4F, -7.6F, -2.6F, 0F, 0F, -1.2217F));
        PartDefinition p_netherite_horn_right_1 = p_netherite_horn_right_0.addOrReplaceChild("netherite_horn_right_1", CubeListBuilder.create()
                .texOffs(153, 276).addBox(-1F, -2F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, -1.75F, 0F, 0.5236F, 0F, 0.5236F));
        PartDefinition p_netherite_horn_right_2 = p_netherite_horn_right_1.addOrReplaceChild("netherite_horn_right_2", CubeListBuilder.create()
                .texOffs(162, 276).addBox(-1F, -1.9F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.1F, -0.45F)),
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
                .texOffs(171, 276).addBox(-1F, -1.66F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.34F, -0.4F)),
                PartPose.offsetAndRotation(2F, -1.8F, -1.6F, 0F, 0F, 0.5236F));
        PartDefinition p_netherite_spike_left_arm_0_1 = p_netherite_spike_left_arm_0_0.addOrReplaceChild("netherite_spike_left_arm_0_1", CubeListBuilder.create()
                .texOffs(65, 306).addBox(-0.5F, -1.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.17F, -0.4F, -0.17F)),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_netherite_spike_left_arm_1_0 = p_netherite_pauldron_left_arm.addOrReplaceChild("netherite_spike_left_arm_1_0", CubeListBuilder.create()
                .texOffs(180, 276).addBox(-1F, -1.66F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.34F, -0.4F)),
                PartPose.offsetAndRotation(2F, -1.8F, 1.6F, 0F, 0F, 0.5236F));
        PartDefinition p_netherite_spike_left_arm_1_1 = p_netherite_spike_left_arm_1_0.addOrReplaceChild("netherite_spike_left_arm_1_1", CubeListBuilder.create()
                .texOffs(70, 306).addBox(-0.5F, -1.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.17F, -0.4F, -0.17F)),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_netherite_chest_right_arm = root.addOrReplaceChild("netherite_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_netherite_pauldron_right_arm = p_netherite_chest_right_arm.addOrReplaceChild("netherite_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(66, 47).addBox(-4.3F, -2.25F, -4F, 8F, 3F, 8F, new CubeDeformation(-0.3F, -0.35F, -0.2F))
                .texOffs(93, 93).addBox(-4.6F, 0.35F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.2F, -0.05F, -0.1F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.2618F));
        PartDefinition p_netherite_spike_right_arm_0_0 = p_netherite_pauldron_right_arm.addOrReplaceChild("netherite_spike_right_arm_0_0", CubeListBuilder.create()
                .texOffs(189, 276).addBox(-1F, -1.66F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.34F, -0.4F)),
                PartPose.offsetAndRotation(-2F, -1.8F, -1.6F, 0F, 0F, -0.5236F));
        PartDefinition p_netherite_spike_right_arm_0_1 = p_netherite_spike_right_arm_0_0.addOrReplaceChild("netherite_spike_right_arm_0_1", CubeListBuilder.create()
                .texOffs(75, 306).addBox(-0.5F, -1.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.17F, -0.4F, -0.17F)),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_netherite_spike_right_arm_1_0 = p_netherite_pauldron_right_arm.addOrReplaceChild("netherite_spike_right_arm_1_0", CubeListBuilder.create()
                .texOffs(198, 276).addBox(-1F, -1.66F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.34F, -0.4F)),
                PartPose.offsetAndRotation(-2F, -1.8F, 1.6F, 0F, 0F, -0.5236F));
        PartDefinition p_netherite_spike_right_arm_1_1 = p_netherite_spike_right_arm_1_0.addOrReplaceChild("netherite_spike_right_arm_1_1", CubeListBuilder.create()
                .texOffs(80, 306).addBox(-0.5F, -1.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.17F, -0.4F, -0.17F)),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_netherite_legs_body = root.addOrReplaceChild("netherite_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_netherite_legs_left_leg = root.addOrReplaceChild("netherite_legs_left_leg", CubeListBuilder.create()
                .texOffs(0, 211).addBox(-3F, -0.9F, -3.3F, 6F, 5F, 1F, new CubeDeformation(-0.3F, -0.3F, 0F))
                .texOffs(242, 261).addBox(-2F, 5F, -3.2F, 4F, 3F, 1F, new CubeDeformation(-0.1F, -0.1F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_netherite_legs_right_leg = root.addOrReplaceChild("netherite_legs_right_leg", CubeListBuilder.create()
                .texOffs(15, 211).addBox(-3F, -0.9F, -3.3F, 6F, 5F, 1F, new CubeDeformation(-0.3F, -0.3F, 0F))
                .texOffs(0, 266).addBox(-2F, 5F, -3.2F, 4F, 3F, 1F, new CubeDeformation(-0.1F, -0.1F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_netherite_feet_left_leg = root.addOrReplaceChild("netherite_feet_left_leg", CubeListBuilder.create()
                .texOffs(164, 232).addBox(-2.5F, 10.15F, -4.45F, 5F, 3F, 2F, new CubeDeformation(0F, -0.25F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_netherite_toe_left_leg_0 = p_netherite_feet_left_leg.addOrReplaceChild("netherite_toe_left_leg_0", CubeListBuilder.create()
                .texOffs(120, 313).addBox(-0.5F, -0.94F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.06F, 0F)),
                PartPose.offsetAndRotation(0F, 11.2F, -4F, 1.0821F, 0F, 0F));
        PartDefinition p_netherite_toe_left_leg_1 = p_netherite_toe_left_leg_0.addOrReplaceChild("netherite_toe_left_leg_1", CubeListBuilder.create()
                .texOffs(125, 313).addBox(-0.5F, -0.9F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.1F, -0.225F)),
                PartPose.offset(0F, -0.63F, 0F));
        PartDefinition p_netherite_feet_right_leg = root.addOrReplaceChild("netherite_feet_right_leg", CubeListBuilder.create()
                .texOffs(179, 232).addBox(-2.5F, 10.15F, -4.45F, 5F, 3F, 2F, new CubeDeformation(0F, -0.25F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_netherite_toe_right_leg_0 = p_netherite_feet_right_leg.addOrReplaceChild("netherite_toe_right_leg_0", CubeListBuilder.create()
                .texOffs(130, 313).addBox(-0.5F, -0.94F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.06F, 0F)),
                PartPose.offsetAndRotation(0F, 11.2F, -4F, 1.0821F, 0F, 0F));
        PartDefinition p_netherite_toe_right_leg_1 = p_netherite_toe_right_leg_0.addOrReplaceChild("netherite_toe_right_leg_1", CubeListBuilder.create()
                .texOffs(135, 313).addBox(-0.5F, -0.9F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.1F, -0.225F)),
                PartPose.offset(0F, -0.63F, 0F));
        PartDefinition p_copper_head_head = root.addOrReplaceChild("copper_head_head", CubeListBuilder.create()
                .texOffs(149, 204).addBox(-2F, -10.4F, -2F, 4F, 2F, 4F, new CubeDeformation(-0.4F, -0.4F, -0.4F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_copper_spike_0 = p_copper_head_head.addOrReplaceChild("copper_spike_0", CubeListBuilder.create()
                .texOffs(207, 276).addBox(-1F, -2F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offset(0F, -9.8F, 0F));
        PartDefinition p_copper_spike_1 = p_copper_spike_0.addOrReplaceChild("copper_spike_1", CubeListBuilder.create()
                .texOffs(85, 306).addBox(-0.5F, -2F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1F, 0F, -0.1F)),
                PartPose.offset(0F, -1.75F, 0F));
        PartDefinition p_copper_chest_body = root.addOrReplaceChild("copper_chest_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_copper_chest_left_arm = root.addOrReplaceChild("copper_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_copper_pauldron_left_arm = p_copper_chest_left_arm.addOrReplaceChild("copper_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(87, 148).addBox(-3.2F, -1.55F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.2F, -0.05F, 0F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.2094F));
        PartDefinition p_copper_chest_right_arm = root.addOrReplaceChild("copper_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_copper_pauldron_right_arm = p_copper_chest_right_arm.addOrReplaceChild("copper_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(116, 148).addBox(-3.8F, -1.55F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.2F, -0.05F, 0F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.2094F));
        PartDefinition p_copper_legs_body = root.addOrReplaceChild("copper_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_copper_legs_left_leg = root.addOrReplaceChild("copper_legs_left_leg", CubeListBuilder.create()
                .texOffs(11, 266).addBox(-2F, 5F, -3.2F, 4F, 3F, 1F, new CubeDeformation(-0.1F, -0.1F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_copper_legs_right_leg = root.addOrReplaceChild("copper_legs_right_leg", CubeListBuilder.create()
                .texOffs(22, 266).addBox(-2F, 5F, -3.2F, 4F, 3F, 1F, new CubeDeformation(-0.1F, -0.1F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_copper_feet_left_leg = root.addOrReplaceChild("copper_feet_left_leg", CubeListBuilder.create()
                .texOffs(194, 232).addBox(-2.5F, 10.25F, -4.35F, 5F, 3F, 2F, new CubeDeformation(-0.1F, -0.35F, -0.35F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_copper_feet_right_leg = root.addOrReplaceChild("copper_feet_right_leg", CubeListBuilder.create()
                .texOffs(209, 232).addBox(-2.5F, 10.25F, -4.35F, 5F, 3F, 2F, new CubeDeformation(-0.1F, -0.35F, -0.35F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_turtle_head_head = root.addOrReplaceChild("turtle_head_head", CubeListBuilder.create()
                .texOffs(0, 21).addBox(-6F, -9.9F, -6F, 12F, 1F, 12F, new CubeDeformation(-0.4F, 0F, -0.4F))
                .texOffs(86, 59).addBox(-4.5F, -10.9F, -4.5F, 9F, 1F, 9F, new CubeDeformation(-0.1F, 0F, -0.1F))
                .texOffs(83, 187).addBox(-3F, -11.7F, -3F, 6F, 1F, 6F, new CubeDeformation(-0.2F, -0.1F, -0.2F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_moonstone_head_head = root.addOrReplaceChild("moonstone_head_head", CubeListBuilder.create()
                .texOffs(220, 306).addBox(-5.5F, -7.7F, -5.65F, 11F, 1F, 1F, new CubeDeformation(-0.05F, 0F, -0.2F))
                .texOffs(0, 310).addBox(-5.5F, -7.7F, 4.65F, 11F, 1F, 1F, new CubeDeformation(-0.05F, 0F, -0.2F))
                .texOffs(122, 47).addBox(-5.65F, -7.7F, -5F, 1F, 1F, 10F, new CubeDeformation(-0.2F, 0F, -0.15F))
                .texOffs(145, 47).addBox(4.65F, -7.7F, -5F, 1F, 1F, 10F, new CubeDeformation(-0.2F, 0F, -0.15F))
                .texOffs(216, 276).addBox(-1.5F, -8.8F, -6.05F, 3F, 3F, 1F, new CubeDeformation(-0.2F, -0.3F, -0.3F))
                .texOffs(154, 294).addBox(-1F, -8.3F, -6.35F, 2F, 2F, 1F, new CubeDeformation(-0.05F, -0.1F, -0.2F))
                .texOffs(80, 281).addBox(-6.175F, -8.2F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.225F, -0.3F, -0.3F))
                .texOffs(87, 281).addBox(5.175F, -8.2F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.225F, -0.3F, -0.3F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_moon_diadem = p_moonstone_head_head.addOrReplaceChild("moon_diadem", CubeListBuilder.create()
                .texOffs(77, 286).addBox(-2F, -1.5625F, -0.5F, 4F, 2F, 1F, new CubeDeformation(-0.375F, -0.4375F, -0.15F)),
                PartPose.offsetAndRotation(0F, -8.6F, -5.75F, -0.1396F, 0F, 0F));
        PartDefinition p_moon_diadem_left_0 = p_moon_diadem.addOrReplaceChild("moon_diadem_left_0", CubeListBuilder.create()
                .texOffs(161, 294).addBox(-1F, -1.9375F, -0.5F, 2F, 2F, 1F, new CubeDeformation(-0.4062F, -0.0625F, -0.175F)),
                PartPose.offsetAndRotation(1.3125F, -0.5625F, 0F, 0F, 0F, 1.0123F));
        PartDefinition p_moon_diadem_left_1 = p_moon_diadem_left_0.addOrReplaceChild("moon_diadem_left_1", CubeListBuilder.create()
                .texOffs(90, 306).addBox(-0.5F, -1.8438F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.0312F, -0.1562F, -0.2F)),
                PartPose.offsetAndRotation(0F, -1.625F, 0F, 0F, 0F, -0.6981F));
        PartDefinition p_moon_diadem_left_2 = p_moon_diadem_left_1.addOrReplaceChild("moon_diadem_left_2", CubeListBuilder.create()
                .texOffs(95, 306).addBox(-0.5F, -1.7188F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1875F, -0.2812F, -0.25F)),
                PartPose.offsetAndRotation(0F, -1.4375F, 0F, 0F, 0F, -0.6283F));
        PartDefinition p_moon_diadem_right_0 = p_moon_diadem.addOrReplaceChild("moon_diadem_right_0", CubeListBuilder.create()
                .texOffs(168, 294).addBox(-1F, -1.9375F, -0.5F, 2F, 2F, 1F, new CubeDeformation(-0.4062F, -0.0625F, -0.175F)),
                PartPose.offsetAndRotation(-1.3125F, -0.5625F, 0F, 0F, 0F, -1.0123F));
        PartDefinition p_moon_diadem_right_1 = p_moon_diadem_right_0.addOrReplaceChild("moon_diadem_right_1", CubeListBuilder.create()
                .texOffs(100, 306).addBox(-0.5F, -1.8438F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.0312F, -0.1562F, -0.2F)),
                PartPose.offsetAndRotation(0F, -1.625F, 0F, 0F, 0F, 0.6981F));
        PartDefinition p_moon_diadem_right_2 = p_moon_diadem_right_1.addOrReplaceChild("moon_diadem_right_2", CubeListBuilder.create()
                .texOffs(105, 306).addBox(-0.5F, -1.7188F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1875F, -0.2812F, -0.25F)),
                PartPose.offsetAndRotation(0F, -1.4375F, 0F, 0F, 0F, 0.6283F));
        PartDefinition p_moonstone_chest_body = root.addOrReplaceChild("moonstone_chest_body", CubeListBuilder.create()
                .texOffs(175, 294).addBox(-4.6F, -0.9F, -3.75F, 2F, 2F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.15F))
                .texOffs(182, 294).addBox(2.6F, -0.9F, -3.75F, 2F, 2F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.15F))
                .texOffs(133, 238).addBox(-2F, 1.2F, -3.7F, 4F, 4F, 1F, new CubeDeformation(-0.3F, -0.2F, -0.25F))
                .texOffs(225, 276).addBox(-1.5F, 1.7F, -4.05F, 3F, 3F, 1F, new CubeDeformation(-0.35F, -0.2F, -0.2F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_moon_cape = p_moonstone_chest_body.addOrReplaceChild("moon_cape", CubeListBuilder.create()
                .texOffs(232, 126).addBox(-5F, -0.2F, -0.2F, 10F, 9F, 1F, new CubeDeformation(-0.3F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(0F, -0.2F, 3.1F, 0.0873F, 0F, 0F));
        PartDefinition p_moon_cape_hem = p_moon_cape.addOrReplaceChild("moon_cape_hem", CubeListBuilder.create()
                .texOffs(0, 137).addBox(-5F, -0.3F, -0.2F, 10F, 9F, 1F, new CubeDeformation(-0.4F, -0.3F, -0.2F)),
                PartPose.offsetAndRotation(0F, 8.4F, 0.05F, 0.1396F, 0F, 0F));
        PartDefinition p_moonstone_chest_left_arm = root.addOrReplaceChild("moonstone_chest_left_arm", CubeListBuilder.create()
                .texOffs(58, 126).addBox(-2.5F, 5.15F, -3.5F, 7F, 3F, 7F, new CubeDeformation(-0.25F, -0.35F, -0.25F))
                .texOffs(189, 294).addBox(0F, 5.65F, -3.95F, 2F, 2F, 1F, new CubeDeformation(-0.4F, -0.35F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_moon_pauldron_left_arm = p_moonstone_chest_left_arm.addOrReplaceChild("moon_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(124, 93).addBox(-3.3F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.2F, -0.15F, -0.4F))
                .texOffs(87, 126).addBox(-2.1F, -0.15F, -4F, 6F, 2F, 8F, new CubeDeformation(-0.1F, -0.35F, -0.3F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.2094F));
        PartDefinition p_moon_shoulder_left_arm = p_moon_pauldron_left_arm.addOrReplaceChild("moon_shoulder_left_arm", CubeListBuilder.create()
                .texOffs(85, 310).addBox(-1.5F, -0.95F, -0.5F, 3F, 1F, 1F, new CubeDeformation(-0.2F, -0.05F, -0.15F)),
                PartPose.offsetAndRotation(1.4F, -1.4F, 0F, 0F, 1.5708F, 0F));
        PartDefinition p_moon_shoulder_left_arm_left_0 = p_moon_shoulder_left_arm.addOrReplaceChild("moon_shoulder_left_arm_left_0", CubeListBuilder.create()
                .texOffs(110, 306).addBox(-0.5F, -1.75F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.025F, -0.25F, -0.175F)),
                PartPose.offsetAndRotation(1.05F, -0.45F, 0F, 0F, 0F, 1.0123F));
        PartDefinition p_moon_shoulder_left_arm_left_1 = p_moon_shoulder_left_arm_left_0.addOrReplaceChild("moon_shoulder_left_arm_left_1", CubeListBuilder.create()
                .texOffs(115, 306).addBox(-0.5F, -1.675F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.125F, -0.325F, -0.2F)),
                PartPose.offsetAndRotation(0F, -1.25F, 0F, 0F, 0F, -0.6981F));
        PartDefinition p_moon_shoulder_left_arm_left_2 = p_moon_shoulder_left_arm_left_1.addOrReplaceChild("moon_shoulder_left_arm_left_2", CubeListBuilder.create()
                .texOffs(120, 306).addBox(-0.5F, -1.575F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.25F, -0.425F, -0.25F)),
                PartPose.offsetAndRotation(0F, -1.1F, 0F, 0F, 0F, -0.6283F));
        PartDefinition p_moon_shoulder_left_arm_right_0 = p_moon_shoulder_left_arm.addOrReplaceChild("moon_shoulder_left_arm_right_0", CubeListBuilder.create()
                .texOffs(125, 306).addBox(-0.5F, -1.75F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.025F, -0.25F, -0.175F)),
                PartPose.offsetAndRotation(-1.05F, -0.45F, 0F, 0F, 0F, -1.0123F));
        PartDefinition p_moon_shoulder_left_arm_right_1 = p_moon_shoulder_left_arm_right_0.addOrReplaceChild("moon_shoulder_left_arm_right_1", CubeListBuilder.create()
                .texOffs(130, 306).addBox(-0.5F, -1.675F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.125F, -0.325F, -0.2F)),
                PartPose.offsetAndRotation(0F, -1.25F, 0F, 0F, 0F, 0.6981F));
        PartDefinition p_moon_shoulder_left_arm_right_2 = p_moon_shoulder_left_arm_right_1.addOrReplaceChild("moon_shoulder_left_arm_right_2", CubeListBuilder.create()
                .texOffs(135, 306).addBox(-0.5F, -1.575F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.25F, -0.425F, -0.25F)),
                PartPose.offsetAndRotation(0F, -1.1F, 0F, 0F, 0F, 0.6283F));
        PartDefinition p_moonstone_chest_right_arm = root.addOrReplaceChild("moonstone_chest_right_arm", CubeListBuilder.create()
                .texOffs(116, 126).addBox(-4.5F, 5.15F, -3.5F, 7F, 3F, 7F, new CubeDeformation(-0.25F, -0.35F, -0.25F))
                .texOffs(196, 294).addBox(-2F, 5.65F, -3.95F, 2F, 2F, 1F, new CubeDeformation(-0.4F, -0.35F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_moon_pauldron_right_arm = p_moonstone_chest_right_arm.addOrReplaceChild("moon_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(155, 93).addBox(-3.7F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.2F, -0.15F, -0.4F))
                .texOffs(145, 126).addBox(-3.9F, -0.15F, -4F, 6F, 2F, 8F, new CubeDeformation(-0.1F, -0.35F, -0.3F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.2094F));
        PartDefinition p_moon_shoulder_right_arm = p_moon_pauldron_right_arm.addOrReplaceChild("moon_shoulder_right_arm", CubeListBuilder.create()
                .texOffs(94, 310).addBox(-1.5F, -0.95F, -0.5F, 3F, 1F, 1F, new CubeDeformation(-0.2F, -0.05F, -0.15F)),
                PartPose.offsetAndRotation(-1.4F, -1.4F, 0F, 0F, -1.5708F, 0F));
        PartDefinition p_moon_shoulder_right_arm_left_0 = p_moon_shoulder_right_arm.addOrReplaceChild("moon_shoulder_right_arm_left_0", CubeListBuilder.create()
                .texOffs(140, 306).addBox(-0.5F, -1.75F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.025F, -0.25F, -0.175F)),
                PartPose.offsetAndRotation(1.05F, -0.45F, 0F, 0F, 0F, 1.0123F));
        PartDefinition p_moon_shoulder_right_arm_left_1 = p_moon_shoulder_right_arm_left_0.addOrReplaceChild("moon_shoulder_right_arm_left_1", CubeListBuilder.create()
                .texOffs(145, 306).addBox(-0.5F, -1.675F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.125F, -0.325F, -0.2F)),
                PartPose.offsetAndRotation(0F, -1.25F, 0F, 0F, 0F, -0.6981F));
        PartDefinition p_moon_shoulder_right_arm_left_2 = p_moon_shoulder_right_arm_left_1.addOrReplaceChild("moon_shoulder_right_arm_left_2", CubeListBuilder.create()
                .texOffs(150, 306).addBox(-0.5F, -1.575F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.25F, -0.425F, -0.25F)),
                PartPose.offsetAndRotation(0F, -1.1F, 0F, 0F, 0F, -0.6283F));
        PartDefinition p_moon_shoulder_right_arm_right_0 = p_moon_shoulder_right_arm.addOrReplaceChild("moon_shoulder_right_arm_right_0", CubeListBuilder.create()
                .texOffs(155, 306).addBox(-0.5F, -1.75F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.025F, -0.25F, -0.175F)),
                PartPose.offsetAndRotation(-1.05F, -0.45F, 0F, 0F, 0F, -1.0123F));
        PartDefinition p_moon_shoulder_right_arm_right_1 = p_moon_shoulder_right_arm_right_0.addOrReplaceChild("moon_shoulder_right_arm_right_1", CubeListBuilder.create()
                .texOffs(160, 306).addBox(-0.5F, -1.675F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.125F, -0.325F, -0.2F)),
                PartPose.offsetAndRotation(0F, -1.25F, 0F, 0F, 0F, 0.6981F));
        PartDefinition p_moon_shoulder_right_arm_right_2 = p_moon_shoulder_right_arm_right_1.addOrReplaceChild("moon_shoulder_right_arm_right_2", CubeListBuilder.create()
                .texOffs(165, 306).addBox(-0.5F, -1.575F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.25F, -0.425F, -0.25F)),
                PartPose.offsetAndRotation(0F, -1.1F, 0F, 0F, 0F, 0.6283F));
        PartDefinition p_moonstone_legs_body = root.addOrReplaceChild("moonstone_legs_body", CubeListBuilder.create()
                .texOffs(203, 294).addBox(-1F, 10.15F, -3.8F, 2F, 2F, 1F, new CubeDeformation(-0.4F, -0.45F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_moon_buckle = p_moonstone_legs_body.addOrReplaceChild("moon_buckle", CubeListBuilder.create()
                .texOffs(119, 310).addBox(-1F, -0.8375F, -0.5F, 2F, 1F, 1F, new CubeDeformation(-0.025F, -0.1625F, -0.15F)),
                PartPose.offset(0F, 11.5F, -3.3F));
        PartDefinition p_moon_buckle_left_0 = p_moon_buckle.addOrReplaceChild("moon_buckle_left_0", CubeListBuilder.create()
                .texOffs(170, 306).addBox(-0.5F, -1.5625F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1438F, -0.4375F, -0.175F)),
                PartPose.offsetAndRotation(0.7875F, -0.3375F, 0F, 0F, 0F, 1.0123F));
        PartDefinition p_moon_buckle_left_1 = p_moon_buckle_left_0.addOrReplaceChild("moon_buckle_left_1", CubeListBuilder.create()
                .texOffs(175, 306).addBox(-0.5F, -1.5063F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.2188F, -0.4937F, -0.2F)),
                PartPose.offsetAndRotation(0F, -0.875F, 0F, 0F, 0F, -0.6981F));
        PartDefinition p_moon_buckle_left_2 = p_moon_buckle_left_1.addOrReplaceChild("moon_buckle_left_2", CubeListBuilder.create()
                .texOffs(140, 313).addBox(-0.5F, -0.9312F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.3125F, -0.0688F, -0.25F)),
                PartPose.offsetAndRotation(0F, -0.7625F, 0F, 0F, 0F, -0.6283F));
        PartDefinition p_moon_buckle_right_0 = p_moon_buckle.addOrReplaceChild("moon_buckle_right_0", CubeListBuilder.create()
                .texOffs(180, 306).addBox(-0.5F, -1.5625F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1438F, -0.4375F, -0.175F)),
                PartPose.offsetAndRotation(-0.7875F, -0.3375F, 0F, 0F, 0F, -1.0123F));
        PartDefinition p_moon_buckle_right_1 = p_moon_buckle_right_0.addOrReplaceChild("moon_buckle_right_1", CubeListBuilder.create()
                .texOffs(185, 306).addBox(-0.5F, -1.5063F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.2188F, -0.4937F, -0.2F)),
                PartPose.offsetAndRotation(0F, -0.875F, 0F, 0F, 0F, 0.6981F));
        PartDefinition p_moon_buckle_right_2 = p_moon_buckle_right_1.addOrReplaceChild("moon_buckle_right_2", CubeListBuilder.create()
                .texOffs(145, 313).addBox(-0.5F, -0.9312F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.3125F, -0.0688F, -0.25F)),
                PartPose.offsetAndRotation(0F, -0.7625F, 0F, 0F, 0F, 0.6283F));
        PartDefinition p_moonstone_legs_left_leg = root.addOrReplaceChild("moonstone_legs_left_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_moon_robe_left_leg = p_moonstone_legs_left_leg.addOrReplaceChild("moon_robe_left_leg", CubeListBuilder.create()
                .texOffs(17, 168).addBox(-2.5F, -0.1F, -0.55F, 5F, 8F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.25F)),
                PartPose.offsetAndRotation(0F, -0.6F, -2.75F, -0.1047F, 0F, 0F));
        PartDefinition p_moon_robe_side_left_leg = p_moonstone_legs_left_leg.addOrReplaceChild("moon_robe_side_left_leg", CubeListBuilder.create()
                .texOffs(94, 21).addBox(-0.25F, -0.1F, -2.5F, 1F, 8F, 5F, new CubeDeformation(-0.25F, -0.1F, -0.1F)),
                PartPose.offsetAndRotation(2.75F, -0.6F, 0F, 0F, 0F, -0.1047F));
        PartDefinition p_moonstone_legs_right_leg = root.addOrReplaceChild("moonstone_legs_right_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_moon_robe_right_leg = p_moonstone_legs_right_leg.addOrReplaceChild("moon_robe_right_leg", CubeListBuilder.create()
                .texOffs(30, 168).addBox(-2.5F, -0.1F, -0.55F, 5F, 8F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.25F)),
                PartPose.offsetAndRotation(0F, -0.6F, -2.75F, -0.1047F, 0F, 0F));
        PartDefinition p_moon_robe_side_right_leg = p_moonstone_legs_right_leg.addOrReplaceChild("moon_robe_side_right_leg", CubeListBuilder.create()
                .texOffs(107, 21).addBox(-0.75F, -0.1F, -2.5F, 1F, 8F, 5F, new CubeDeformation(-0.25F, -0.1F, -0.1F)),
                PartPose.offsetAndRotation(-2.75F, -0.6F, 0F, 0F, 0F, 0.1047F));
        PartDefinition p_moonstone_feet_left_leg = root.addOrReplaceChild("moonstone_feet_left_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_moon_toe_left_leg_0 = p_moonstone_feet_left_leg.addOrReplaceChild("moon_toe_left_leg_0", CubeListBuilder.create()
                .texOffs(73, 256).addBox(-2F, -1.85F, -1F, 4F, 2F, 2F, new CubeDeformation(-0.3F, -0.15F, -0.3F)),
                PartPose.offsetAndRotation(0F, 12F, -2.3F, 1.5708F, 0F, 0F));
        PartDefinition p_moon_toe_left_leg_1 = p_moon_toe_left_leg_0.addOrReplaceChild("moon_toe_left_leg_1", CubeListBuilder.create()
                .texOffs(33, 266).addBox(-1.5F, -1.75F, -1F, 3F, 2F, 2F, new CubeDeformation(-0.3F, -0.25F, -0.45F)),
                PartPose.offsetAndRotation(0F, -1.45F, 0F, -0.4887F, 0F, 0F));
        PartDefinition p_moon_toe_left_leg_2 = p_moon_toe_left_leg_1.addOrReplaceChild("moon_toe_left_leg_2", CubeListBuilder.create()
                .texOffs(210, 294).addBox(-1F, -1.65F, -0.5F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.35F, -0.1F)),
                PartPose.offsetAndRotation(0F, -1.25F, 0F, -0.733F, 0F, 0F));
        PartDefinition p_moon_toe_left_leg_3 = p_moon_toe_left_leg_2.addOrReplaceChild("moon_toe_left_leg_3", CubeListBuilder.create()
                .texOffs(150, 313).addBox(-0.5F, -1F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.15F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, -1.05F, 0F, -0.8029F, 0F, 0F));
        PartDefinition p_moon_ankle_left_leg = p_moonstone_feet_left_leg.addOrReplaceChild("moon_ankle_left_leg", CubeListBuilder.create()
                .texOffs(126, 310).addBox(-1F, -0.779F, -0.5F, 2F, 1F, 1F, new CubeDeformation(-0.194F, -0.221F, -0.15F)),
                PartPose.offsetAndRotation(3.35F, 9.8F, 0.4F, 0F, 1.5708F, 0F));
        PartDefinition p_moon_ankle_left_leg_left_0 = p_moon_ankle_left_leg.addOrReplaceChild("moon_ankle_left_leg_left_0", CubeListBuilder.create()
                .texOffs(155, 313).addBox(-0.5F, -0.965F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.2055F, -0.035F, -0.175F)),
                PartPose.offsetAndRotation(0.651F, -0.279F, 0F, 0F, 0F, 1.0123F));
        PartDefinition p_moon_ankle_left_leg_left_1 = p_moon_ankle_left_leg_left_0.addOrReplaceChild("moon_ankle_left_leg_left_1", CubeListBuilder.create()
                .texOffs(160, 313).addBox(-0.5F, -0.9185F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.2675F, -0.0815F, -0.2F)),
                PartPose.offsetAndRotation(0F, -0.68F, 0F, 0F, 0F, -0.6981F));
        PartDefinition p_moon_ankle_left_leg_left_2 = p_moon_ankle_left_leg_left_1.addOrReplaceChild("moon_ankle_left_leg_left_2", CubeListBuilder.create()
                .texOffs(165, 313).addBox(-0.5F, -0.8565F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.345F, -0.1435F, -0.25F)),
                PartPose.offsetAndRotation(0F, -0.587F, 0F, 0F, 0F, -0.6283F));
        PartDefinition p_moon_ankle_left_leg_right_0 = p_moon_ankle_left_leg.addOrReplaceChild("moon_ankle_left_leg_right_0", CubeListBuilder.create()
                .texOffs(170, 313).addBox(-0.5F, -0.965F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.2055F, -0.035F, -0.175F)),
                PartPose.offsetAndRotation(-0.651F, -0.279F, 0F, 0F, 0F, -1.0123F));
        PartDefinition p_moon_ankle_left_leg_right_1 = p_moon_ankle_left_leg_right_0.addOrReplaceChild("moon_ankle_left_leg_right_1", CubeListBuilder.create()
                .texOffs(175, 313).addBox(-0.5F, -0.9185F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.2675F, -0.0815F, -0.2F)),
                PartPose.offsetAndRotation(0F, -0.68F, 0F, 0F, 0F, 0.6981F));
        PartDefinition p_moon_ankle_left_leg_right_2 = p_moon_ankle_left_leg_right_1.addOrReplaceChild("moon_ankle_left_leg_right_2", CubeListBuilder.create()
                .texOffs(180, 313).addBox(-0.5F, -0.8565F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.345F, -0.1435F, -0.25F)),
                PartPose.offsetAndRotation(0F, -0.587F, 0F, 0F, 0F, 0.6283F));
        PartDefinition p_moonstone_feet_right_leg = root.addOrReplaceChild("moonstone_feet_right_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_moon_toe_right_leg_0 = p_moonstone_feet_right_leg.addOrReplaceChild("moon_toe_right_leg_0", CubeListBuilder.create()
                .texOffs(86, 256).addBox(-2F, -1.85F, -1F, 4F, 2F, 2F, new CubeDeformation(-0.3F, -0.15F, -0.3F)),
                PartPose.offsetAndRotation(0F, 12F, -2.3F, 1.5708F, 0F, 0F));
        PartDefinition p_moon_toe_right_leg_1 = p_moon_toe_right_leg_0.addOrReplaceChild("moon_toe_right_leg_1", CubeListBuilder.create()
                .texOffs(44, 266).addBox(-1.5F, -1.75F, -1F, 3F, 2F, 2F, new CubeDeformation(-0.3F, -0.25F, -0.45F)),
                PartPose.offsetAndRotation(0F, -1.45F, 0F, -0.4887F, 0F, 0F));
        PartDefinition p_moon_toe_right_leg_2 = p_moon_toe_right_leg_1.addOrReplaceChild("moon_toe_right_leg_2", CubeListBuilder.create()
                .texOffs(217, 294).addBox(-1F, -1.65F, -0.5F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.35F, -0.1F)),
                PartPose.offsetAndRotation(0F, -1.25F, 0F, -0.733F, 0F, 0F));
        PartDefinition p_moon_toe_right_leg_3 = p_moon_toe_right_leg_2.addOrReplaceChild("moon_toe_right_leg_3", CubeListBuilder.create()
                .texOffs(185, 313).addBox(-0.5F, -1F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.15F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, -1.05F, 0F, -0.8029F, 0F, 0F));
        PartDefinition p_moon_ankle_right_leg = p_moonstone_feet_right_leg.addOrReplaceChild("moon_ankle_right_leg", CubeListBuilder.create()
                .texOffs(133, 310).addBox(-1F, -0.779F, -0.5F, 2F, 1F, 1F, new CubeDeformation(-0.194F, -0.221F, -0.15F)),
                PartPose.offsetAndRotation(-3.35F, 9.8F, 0.4F, 0F, -1.5708F, 0F));
        PartDefinition p_moon_ankle_right_leg_left_0 = p_moon_ankle_right_leg.addOrReplaceChild("moon_ankle_right_leg_left_0", CubeListBuilder.create()
                .texOffs(190, 313).addBox(-0.5F, -0.965F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.2055F, -0.035F, -0.175F)),
                PartPose.offsetAndRotation(0.651F, -0.279F, 0F, 0F, 0F, 1.0123F));
        PartDefinition p_moon_ankle_right_leg_left_1 = p_moon_ankle_right_leg_left_0.addOrReplaceChild("moon_ankle_right_leg_left_1", CubeListBuilder.create()
                .texOffs(195, 313).addBox(-0.5F, -0.9185F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.2675F, -0.0815F, -0.2F)),
                PartPose.offsetAndRotation(0F, -0.68F, 0F, 0F, 0F, -0.6981F));
        PartDefinition p_moon_ankle_right_leg_left_2 = p_moon_ankle_right_leg_left_1.addOrReplaceChild("moon_ankle_right_leg_left_2", CubeListBuilder.create()
                .texOffs(200, 313).addBox(-0.5F, -0.8565F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.345F, -0.1435F, -0.25F)),
                PartPose.offsetAndRotation(0F, -0.587F, 0F, 0F, 0F, -0.6283F));
        PartDefinition p_moon_ankle_right_leg_right_0 = p_moon_ankle_right_leg.addOrReplaceChild("moon_ankle_right_leg_right_0", CubeListBuilder.create()
                .texOffs(205, 313).addBox(-0.5F, -0.965F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.2055F, -0.035F, -0.175F)),
                PartPose.offsetAndRotation(-0.651F, -0.279F, 0F, 0F, 0F, -1.0123F));
        PartDefinition p_moon_ankle_right_leg_right_1 = p_moon_ankle_right_leg_right_0.addOrReplaceChild("moon_ankle_right_leg_right_1", CubeListBuilder.create()
                .texOffs(210, 313).addBox(-0.5F, -0.9185F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.2675F, -0.0815F, -0.2F)),
                PartPose.offsetAndRotation(0F, -0.68F, 0F, 0F, 0F, 0.6981F));
        PartDefinition p_moon_ankle_right_leg_right_2 = p_moon_ankle_right_leg_right_1.addOrReplaceChild("moon_ankle_right_leg_right_2", CubeListBuilder.create()
                .texOffs(215, 313).addBox(-0.5F, -0.8565F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.345F, -0.1435F, -0.25F)),
                PartPose.offsetAndRotation(0F, -0.587F, 0F, 0F, 0F, 0.6283F));
        PartDefinition p_aquamarine_head_head = root.addOrReplaceChild("aquamarine_head_head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-6F, -8.6F, -6F, 12F, 8F, 12F, new CubeDeformation(-0.3F, 0F, -0.3F))
                .texOffs(41, 35).addBox(-5F, -9.6F, -5F, 10F, 1F, 10F)
                .texOffs(122, 137).addBox(-4F, -10.5F, -4F, 8F, 1F, 8F, new CubeDeformation(-0.3F, -0.1F, -0.3F))
                .texOffs(145, 0).addBox(5.45F, -8.1F, -4.5F, 1F, 7F, 9F, new CubeDeformation(-0.25F, -0.3F, -0.1F))
                .texOffs(166, 0).addBox(-6.45F, -8.1F, -4.5F, 1F, 7F, 9F, new CubeDeformation(-0.25F, -0.3F, -0.1F))
                .texOffs(96, 178).addBox(-4.5F, -8.1F, 5.45F, 9F, 7F, 1F, new CubeDeformation(-0.1F, -0.3F, -0.25F))
                .texOffs(124, 286).addBox(-1F, -11.4F, -1F, 2F, 1F, 2F, new CubeDeformation(-0.2F, 0F, -0.2F))
                .texOffs(74, 310).addBox(-2F, -12.15F, -0.5F, 4F, 1F, 1F, new CubeDeformation(-0.1F, -0.25F, -0.15F))
                .texOffs(187, 0).addBox(-6.5F, -1.35F, -6.5F, 13F, 2F, 13F, new CubeDeformation(-0.4F, -0.35F, -0.4F))
                .texOffs(48, 310).addBox(-2.5F, -8.2F, -6.6F, 5F, 1F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.1F))
                .texOffs(61, 310).addBox(-2.5F, -2.2F, -6.6F, 5F, 1F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.1F))
                .texOffs(216, 196).addBox(-3.3F, -7.7F, -6.6F, 1F, 6F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.1F))
                .texOffs(221, 196).addBox(2.3F, -7.7F, -6.6F, 1F, 6F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.1F))
                .texOffs(220, 313).addBox(-2.55F, -7.45F, -6.475F, 1F, 1F, 1F, new CubeDeformation(-0.15F, -0.15F, -0.225F))
                .texOffs(225, 313).addBox(1.55F, -7.45F, -6.475F, 1F, 1F, 1F, new CubeDeformation(-0.15F, -0.15F, -0.225F))
                .texOffs(230, 313).addBox(-2.55F, -2.95F, -6.475F, 1F, 1F, 1F, new CubeDeformation(-0.15F, -0.15F, -0.225F))
                .texOffs(235, 313).addBox(1.55F, -2.95F, -6.475F, 1F, 1F, 1F, new CubeDeformation(-0.15F, -0.15F, -0.225F))
                .texOffs(184, 225).addBox(5.85F, -6.3F, -1.5F, 1F, 3F, 3F, new CubeDeformation(-0.35F, 0F, 0F))
                .texOffs(208, 218).addBox(6F, -7.1F, -2.5F, 1F, 1F, 5F, new CubeDeformation(-0.2F, -0.2F, -0.4F))
                .texOffs(221, 218).addBox(6F, -3.5F, -2.5F, 1F, 1F, 5F, new CubeDeformation(-0.2F, -0.2F, -0.4F))
                .texOffs(169, 281).addBox(6F, -6.3F, -2.3F, 1F, 3F, 1F, new CubeDeformation(-0.2F, 0F, -0.2F))
                .texOffs(174, 281).addBox(6F, -6.3F, 1.3F, 1F, 3F, 1F, new CubeDeformation(-0.2F, 0F, -0.2F))
                .texOffs(193, 225).addBox(-6.85F, -6.3F, -1.5F, 1F, 3F, 3F, new CubeDeformation(-0.35F, 0F, 0F))
                .texOffs(234, 218).addBox(-7F, -7.1F, -2.5F, 1F, 1F, 5F, new CubeDeformation(-0.2F, -0.2F, -0.4F))
                .texOffs(0, 225).addBox(-7F, -3.5F, -2.5F, 1F, 1F, 5F, new CubeDeformation(-0.2F, -0.2F, -0.4F))
                .texOffs(179, 281).addBox(-7F, -6.3F, -2.3F, 1F, 3F, 1F, new CubeDeformation(-0.2F, 0F, -0.2F))
                .texOffs(184, 281).addBox(-7F, -6.3F, 1.3F, 1F, 3F, 1F, new CubeDeformation(-0.2F, 0F, -0.2F))
                .texOffs(224, 294).addBox(0.7F, -3.6F, 6.15F, 2F, 2F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.05F))
                .texOffs(189, 250).addBox(0.7F, -2.2F, 6F, 2F, 3F, 2F, new CubeDeformation(-0.45F, -0.2F, -0.45F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_aquamarine_chest_body = root.addOrReplaceChild("aquamarine_chest_body", CubeListBuilder.create()
                .texOffs(21, 59).addBox(0.6F, 1.2F, 3.1F, 3F, 8F, 3F, new CubeDeformation(0F, 0F, -0.1F))
                .texOffs(99, 256).addBox(0.6F, 0.4F, 3.1F, 3F, 1F, 3F, new CubeDeformation(-0.3F, -0.2F, -0.4F))
                .texOffs(112, 256).addBox(0.6F, 8.95F, 3.1F, 3F, 1F, 3F, new CubeDeformation(-0.3F, -0.25F, -0.4F))
                .texOffs(240, 313).addBox(1.6F, -0.25F, 4.1F, 1F, 1F, 1F, new CubeDeformation(0F, -0.15F, 0F))
                .texOffs(245, 313).addBox(3.2F, -0.2F, 4.1F, 1F, 1F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.05F))
                .texOffs(34, 59).addBox(-3.6F, 1.2F, 3.1F, 3F, 8F, 3F, new CubeDeformation(0F, 0F, -0.1F))
                .texOffs(125, 256).addBox(-3.6F, 0.4F, 3.1F, 3F, 1F, 3F, new CubeDeformation(-0.3F, -0.2F, -0.4F))
                .texOffs(138, 256).addBox(-3.6F, 8.95F, 3.1F, 3F, 1F, 3F, new CubeDeformation(-0.3F, -0.25F, -0.4F))
                .texOffs(250, 313).addBox(-2.6F, -0.25F, 4.1F, 1F, 1F, 1F, new CubeDeformation(0F, -0.15F, 0F))
                .texOffs(0, 316).addBox(-4.2F, -0.2F, 4.1F, 1F, 1F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.05F))
                .texOffs(227, 225).addBox(-4F, 2.05F, 2.6F, 8F, 1F, 4F, new CubeDeformation(-0.2F, -0.15F, -0.4F))
                .texOffs(0, 232).addBox(-4F, 7.05F, 2.6F, 8F, 1F, 4F, new CubeDeformation(-0.2F, -0.15F, -0.4F))
                .texOffs(133, 286).addBox(1.1F, -0.6F, 4.9F, 2F, 1F, 2F, new CubeDeformation(-0.45F, 0F, -0.1F))
                .texOffs(234, 276).addBox(1.1F, 1.6F, -3.95F, 3F, 3F, 1F, new CubeDeformation(0F, 0F, -0.2F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_aquamarine_chest_left_arm = root.addOrReplaceChild("aquamarine_chest_left_arm", CubeListBuilder.create()
                .texOffs(145, 148).addBox(-2.5F, 5.95F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.25F, -0.25F, -0.25F))
                .texOffs(231, 294).addBox(0F, 5.95F, -3.95F, 2F, 2F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_aqua_pauldron_left_arm = p_aquamarine_chest_left_arm.addOrReplaceChild("aqua_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(145, 158).addBox(-2.6F, -1.5F, -3.5F, 6F, 2F, 7F, new CubeDeformation(0F, -0.2F, -0.1F))
                .texOffs(192, 168).addBox(-1.6F, 0.3F, -3.5F, 5F, 1F, 7F),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.1745F));
        PartDefinition p_aquamarine_chest_right_arm = root.addOrReplaceChild("aquamarine_chest_right_arm", CubeListBuilder.create()
                .texOffs(174, 148).addBox(-4.5F, 5.95F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.25F, -0.25F, -0.25F))
                .texOffs(238, 294).addBox(-2F, 5.95F, -3.95F, 2F, 2F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_aqua_pauldron_right_arm = p_aquamarine_chest_right_arm.addOrReplaceChild("aqua_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(172, 158).addBox(-3.4F, -1.5F, -3.5F, 6F, 2F, 7F, new CubeDeformation(0F, -0.2F, -0.1F))
                .texOffs(217, 168).addBox(-3.4F, 0.3F, -3.5F, 5F, 1F, 7F),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.1745F));
        PartDefinition p_aquamarine_legs_body = root.addOrReplaceChild("aquamarine_legs_body", CubeListBuilder.create()
                .texOffs(142, 286).addBox(-4.9F, 10.25F, -3.675F, 3F, 2F, 1F, new CubeDeformation(-0.3F, -0.05F, -0.125F))
                .texOffs(151, 286).addBox(1.9F, 10.25F, -3.675F, 3F, 2F, 1F, new CubeDeformation(-0.3F, -0.05F, -0.125F))
                .texOffs(198, 250).addBox(4.475F, 10.25F, -1.5F, 1F, 2F, 3F, new CubeDeformation(-0.125F, -0.05F, -0.2F))
                .texOffs(207, 250).addBox(-5.475F, 10.25F, -1.5F, 1F, 2F, 3F, new CubeDeformation(-0.125F, -0.05F, -0.2F))
                .texOffs(160, 286).addBox(-1.5F, 10.25F, -3.8F, 3F, 2F, 1F, new CubeDeformation(-0.4F, -0.25F, -0.2F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_aquamarine_legs_left_leg = root.addOrReplaceChild("aquamarine_legs_left_leg", CubeListBuilder.create()
                .texOffs(151, 256).addBox(-2.5F, 3.4F, -3.25F, 5F, 3F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(245, 294).addBox(-1F, 3.9F, -3.7F, 2F, 2F, 1F, new CubeDeformation(-0.45F, -0.45F, -0.3F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_aquamarine_legs_right_leg = root.addOrReplaceChild("aquamarine_legs_right_leg", CubeListBuilder.create()
                .texOffs(164, 256).addBox(-2.5F, 3.4F, -3.25F, 5F, 3F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(0, 298).addBox(-1F, 3.9F, -3.7F, 2F, 2F, 1F, new CubeDeformation(-0.45F, -0.45F, -0.3F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_aquamarine_feet_left_leg = root.addOrReplaceChild("aquamarine_feet_left_leg", CubeListBuilder.create()
                .texOffs(108, 187).addBox(-3F, 9.25F, -3F, 6F, 1F, 6F, new CubeDeformation(-0.45F, -0.05F, -0.45F))
                .texOffs(94, 281).addBox(2.275F, 8.75F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.275F, -0.4F, -0.3F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_aqua_fin_left_leg_0 = p_aquamarine_feet_left_leg.addOrReplaceChild("aqua_fin_left_leg_0", CubeListBuilder.create()
                .texOffs(30, 238).addBox(-2.5F, -3.7F, -0.5F, 5F, 4F, 1F, new CubeDeformation(-0.2F, -0.3F, -0.275F)),
                PartPose.offsetAndRotation(0F, 11.75F, -2F, 1.5708F, 0F, 0F));
        PartDefinition p_aqua_fin_left_leg_1 = p_aqua_fin_left_leg_0.addOrReplaceChild("aqua_fin_left_leg_1", CubeListBuilder.create()
                .texOffs(224, 232).addBox(-3F, -3.6F, -0.5F, 6F, 4F, 1F, new CubeDeformation(-0.3F, -0.4F, -0.3F)),
                PartPose.offsetAndRotation(0F, -3.25F, 0F, -0.0873F, 0F, 0F));
        PartDefinition p_aqua_fin_left_leg_2 = p_aqua_fin_left_leg_1.addOrReplaceChild("aqua_fin_left_leg_2", CubeListBuilder.create()
                .texOffs(17, 256).addBox(-3F, -2.8F, -0.5F, 6F, 3F, 1F, new CubeDeformation(0F, -0.2F, -0.325F)),
                PartPose.offsetAndRotation(0F, -3.05F, 0F, -0.1571F, 0F, 0F));
        PartDefinition p_aquamarine_feet_right_leg = root.addOrReplaceChild("aquamarine_feet_right_leg", CubeListBuilder.create()
                .texOffs(133, 187).addBox(-3F, 9.25F, -3F, 6F, 1F, 6F, new CubeDeformation(-0.45F, -0.05F, -0.45F))
                .texOffs(101, 281).addBox(-3.275F, 8.75F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.275F, -0.4F, -0.3F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_aqua_fin_right_leg_0 = p_aquamarine_feet_right_leg.addOrReplaceChild("aqua_fin_right_leg_0", CubeListBuilder.create()
                .texOffs(43, 238).addBox(-2.5F, -3.7F, -0.5F, 5F, 4F, 1F, new CubeDeformation(-0.2F, -0.3F, -0.275F)),
                PartPose.offsetAndRotation(0F, 11.75F, -2F, 1.5708F, 0F, 0F));
        PartDefinition p_aqua_fin_right_leg_1 = p_aqua_fin_right_leg_0.addOrReplaceChild("aqua_fin_right_leg_1", CubeListBuilder.create()
                .texOffs(239, 232).addBox(-3F, -3.6F, -0.5F, 6F, 4F, 1F, new CubeDeformation(-0.3F, -0.4F, -0.3F)),
                PartPose.offsetAndRotation(0F, -3.25F, 0F, -0.0873F, 0F, 0F));
        PartDefinition p_aqua_fin_right_leg_2 = p_aqua_fin_right_leg_1.addOrReplaceChild("aqua_fin_right_leg_2", CubeListBuilder.create()
                .texOffs(32, 256).addBox(-3F, -2.8F, -0.5F, 6F, 3F, 1F, new CubeDeformation(0F, -0.2F, -0.325F)),
                PartPose.offsetAndRotation(0F, -3.05F, 0F, -0.1571F, 0F, 0F));
        PartDefinition p_dusk_head_head = root.addOrReplaceChild("dusk_head_head", CubeListBuilder.create()
                .texOffs(49, 21).addBox(-5.5F, -10.15F, -5.4F, 11F, 2F, 11F, new CubeDeformation(0F, -0.45F, -0.1F))
                .texOffs(49, 0).addBox(4.775F, -9.1F, -5.4F, 1F, 9F, 11F, new CubeDeformation(-0.225F, -0.3F, -0.1F))
                .texOffs(74, 0).addBox(-5.775F, -9.1F, -5.4F, 1F, 9F, 11F, new CubeDeformation(-0.225F, -0.3F, -0.1F))
                .texOffs(23, 137).addBox(-5F, -9F, 4.775F, 10F, 9F, 1F, new CubeDeformation(0F, -0.2F, -0.225F))
                .texOffs(0, 286).addBox(-6F, -9.45F, -6.05F, 12F, 2F, 1F, new CubeDeformation(-0.45F, -0.25F, -0.2F))
                .texOffs(44, 187).addBox(3.925F, -8.3F, -6.05F, 2F, 7F, 1F, new CubeDeformation(-0.375F, -0.3F, -0.2F))
                .texOffs(51, 187).addBox(-5.925F, -8.3F, -6.05F, 2F, 7F, 1F, new CubeDeformation(-0.375F, -0.3F, -0.2F))
                .texOffs(0, 256).addBox(-3.5F, -3.7F, -5.775F, 7F, 3F, 1F, new CubeDeformation(-0.2F, -0.1F, -0.225F))
                .texOffs(189, 281).addBox(-0.5F, -3.75F, -6.075F, 1F, 3F, 1F, new CubeDeformation(-0.05F, 0F, -0.325F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_dusk_hood_tip_0 = p_dusk_head_head.addOrReplaceChild("dusk_hood_tip_0", CubeListBuilder.create()
                .texOffs(177, 256).addBox(-2.5F, -0.2F, -0.5F, 5F, 3F, 1F, new CubeDeformation(-0.2F, -0.2F, 0F)),
                PartPose.offsetAndRotation(0F, -8.4F, 5.3F, 1.0821F, 0F, 0F));
        PartDefinition p_dusk_hood_tip_1 = p_dusk_hood_tip_0.addOrReplaceChild("dusk_hood_tip_1", CubeListBuilder.create()
                .texOffs(55, 266).addBox(-2F, -0.1F, -0.5F, 4F, 3F, 1F, new CubeDeformation(-0.3F, -0.1F, -0.05F)),
                PartPose.offsetAndRotation(0F, 2.35F, 0F, -0.5236F, 0F, 0F));
        PartDefinition p_dusk_hood_tip_2 = p_dusk_hood_tip_1.addOrReplaceChild("dusk_hood_tip_2", CubeListBuilder.create()
                .texOffs(243, 276).addBox(-1.5F, -0.1F, -0.5F, 3F, 3F, 1F, new CubeDeformation(-0.3F, -0.1F, -0.1F)),
                PartPose.offsetAndRotation(0F, 2.55F, 0F, -0.384F, 0F, 0F));
        PartDefinition p_dusk_hood_tip_3 = p_dusk_hood_tip_2.addOrReplaceChild("dusk_hood_tip_3", CubeListBuilder.create()
                .texOffs(108, 281).addBox(-1F, -0.3F, -0.5F, 2F, 3F, 1F, new CubeDeformation(-0.3F, -0.3F, -0.15F)),
                PartPose.offsetAndRotation(0F, 2.55F, 0F, -0.2094F, 0F, 0F));
        PartDefinition p_dusk_chest_body = root.addOrReplaceChild("dusk_chest_body", CubeListBuilder.create()
                .texOffs(47, 59).addBox(-5.5F, -1F, -4F, 11F, 2F, 8F, new CubeDeformation(-0.1F, -0.45F, -0.45F))
                .texOffs(169, 286).addBox(1.6F, 0.2F, -4F, 3F, 2F, 1F, new CubeDeformation(-0.4F, 0F, -0.25F))
                .texOffs(7, 298).addBox(-4.05F, 2F, -3.775F, 2F, 2F, 1F, new CubeDeformation(-0.15F, -0.4F, -0.325F))
                .texOffs(14, 298).addBox(-2.5F, 3.6F, -3.775F, 2F, 2F, 1F, new CubeDeformation(-0.15F, -0.4F, -0.325F))
                .texOffs(21, 298).addBox(-0.95F, 5.2F, -3.775F, 2F, 2F, 1F, new CubeDeformation(-0.15F, -0.4F, -0.325F))
                .texOffs(28, 298).addBox(0.6F, 6.8F, -3.775F, 2F, 2F, 1F, new CubeDeformation(-0.15F, -0.4F, -0.325F))
                .texOffs(35, 298).addBox(2.15F, 8.4F, -3.775F, 2F, 2F, 1F, new CubeDeformation(-0.15F, -0.4F, -0.325F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_dusk_cape = p_dusk_chest_body.addOrReplaceChild("dusk_cape", CubeListBuilder.create()
                .texOffs(0, 178).addBox(-5.5F, -0.1F, -0.2F, 11F, 7F, 1F, new CubeDeformation(-0.3F, -0.1F, -0.2F))
                .texOffs(0, 281).addBox(-5.6F, 6.35F, -0.2F, 3F, 3F, 1F, new CubeDeformation(-0.4F, -0.45F, -0.22F))
                .texOffs(178, 286).addBox(-3.1F, 6.4F, -0.2F, 3F, 2F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.22F))
                .texOffs(9, 281).addBox(-0.6F, 6.35F, -0.2F, 3F, 3F, 1F, new CubeDeformation(-0.4F, -0.45F, -0.22F))
                .texOffs(187, 286).addBox(1.9F, 6.4F, -0.2F, 3F, 2F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.22F)),
                PartPose.offsetAndRotation(0F, 0.2F, 3.25F, 0.1047F, 0F, 0F));
        PartDefinition p_dusk_clasp_a_0 = p_dusk_chest_body.addOrReplaceChild("dusk_clasp_a_0", CubeListBuilder.create()
                .texOffs(190, 306).addBox(-0.5F, -1.5445F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.05F, -0.4555F, -0.05F)),
                PartPose.offsetAndRotation(3F, 1.8F, -4F, -0.2094F, 0F, 0.2443F));
        PartDefinition p_dusk_clasp_a_1 = p_dusk_clasp_a_0.addOrReplaceChild("dusk_clasp_a_1", CubeListBuilder.create()
                .texOffs(5, 316).addBox(-0.5F, -0.995F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.2525F, -0.005F, -0.2525F)),
                PartPose.offset(0F, -0.839F, 0F));
        PartDefinition p_dusk_clasp_b_0 = p_dusk_chest_body.addOrReplaceChild("dusk_clasp_b_0", CubeListBuilder.create()
                .texOffs(10, 316).addBox(-0.5F, -0.8713F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.14F, -0.1287F, -0.14F)),
                PartPose.offsetAndRotation(3.63F, 2F, -4F, -0.2094F, 0F, -0.4189F));
        PartDefinition p_dusk_clasp_b_1 = p_dusk_clasp_b_0.addOrReplaceChild("dusk_clasp_b_1", CubeListBuilder.create()
                .texOffs(15, 316).addBox(-0.5F, -0.8375F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.302F, -0.1625F, -0.302F)),
                PartPose.offset(0F, -0.4925F, 0F));
        PartDefinition p_dusk_chest_left_arm = root.addOrReplaceChild("dusk_chest_left_arm", CubeListBuilder.create()
                .texOffs(203, 148).addBox(-2.5F, 0.85F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.15F, -0.35F, -0.15F))
                .texOffs(134, 168).addBox(-2.5F, 2.9F, -3.5F, 7F, 1F, 7F, new CubeDeformation(-0.15F, 0F, -0.15F))
                .texOffs(0, 158).addBox(-2.5F, 7.4F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.2F, -0.2F, -0.2F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_dusk_chest_right_arm = root.addOrReplaceChild("dusk_chest_right_arm", CubeListBuilder.create()
                .texOffs(29, 158).addBox(-4.5F, 0.85F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.15F, -0.35F, -0.15F))
                .texOffs(163, 168).addBox(-4.5F, 2.9F, -3.5F, 7F, 1F, 7F, new CubeDeformation(-0.15F, 0F, -0.15F))
                .texOffs(58, 158).addBox(-4.5F, 7.4F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.2F, -0.2F, -0.2F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_dusk_legs_body = root.addOrReplaceChild("dusk_legs_body", CubeListBuilder.create()
                .texOffs(18, 281).addBox(-4.8F, 9.9F, -3.45F, 3F, 3F, 1F, new CubeDeformation(-0.4F, -0.3F, 0F))
                .texOffs(230, 250).addBox(-4.65F, 9.7F, -1F, 1F, 3F, 2F, new CubeDeformation(-0.05F, -0.4F, 0F))
                .texOffs(196, 286).addBox(-4.5F, 9.7F, -4F, 2F, 1F, 2F, new CubeDeformation(-0.2F, -0.2F, -0.4F))
                .texOffs(42, 298).addBox(-1F, 10.1F, -3.875F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.3F, -0.275F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_dusk_dagger = p_dusk_legs_body.addOrReplaceChild("dusk_dagger", CubeListBuilder.create()
                .texOffs(194, 281).addBox(-0.5F, -2.5F, -0.15F, 1F, 3F, 1F, new CubeDeformation(-0.05F, 0F, -0.15F))
                .texOffs(140, 310).addBox(-1F, -3.25F, -0.15F, 2F, 1F, 1F, new CubeDeformation(-0.3F, -0.25F, -0.1F))
                .texOffs(195, 306).addBox(-0.5F, -4.8F, -0.15F, 1F, 2F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(20, 316).addBox(-0.5F, -5.3F, -0.15F, 1F, 1F, 1F, new CubeDeformation(-0.1F, -0.2F, -0.15F)),
                PartPose.offsetAndRotation(0F, 11F, 3.3F, 0F, 0F, 1.2915F));
        PartDefinition p_dusk_legs_left_leg = root.addOrReplaceChild("dusk_legs_left_leg", CubeListBuilder.create()
                .texOffs(190, 256).addBox(-2.5F, 3.3F, -3.25F, 5F, 3F, 1F, new CubeDeformation(-0.15F, -0.1F, -0.2F))
                .texOffs(49, 298).addBox(-1F, 3.8F, -3.675F, 2F, 2F, 1F, new CubeDeformation(-0.45F, -0.45F, -0.275F))
                .texOffs(23, 204).addBox(-2.5F, 6.95F, -2.5F, 5F, 1F, 5F, new CubeDeformation(-0.1F, -0.05F, -0.1F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_dusk_legs_right_leg = root.addOrReplaceChild("dusk_legs_right_leg", CubeListBuilder.create()
                .texOffs(203, 256).addBox(-2.5F, 3.3F, -3.25F, 5F, 3F, 1F, new CubeDeformation(-0.15F, -0.1F, -0.2F))
                .texOffs(56, 298).addBox(-1F, 3.8F, -3.675F, 2F, 2F, 1F, new CubeDeformation(-0.45F, -0.45F, -0.275F))
                .texOffs(44, 204).addBox(-2.5F, 6.95F, -2.5F, 5F, 1F, 5F, new CubeDeformation(-0.1F, -0.05F, -0.1F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_dusk_feet_left_leg = root.addOrReplaceChild("dusk_feet_left_leg", CubeListBuilder.create()
                .texOffs(25, 178).addBox(-3F, 6.15F, -3F, 6F, 2F, 6F, new CubeDeformation(-0.45F, -0.25F, -0.45F))
                .texOffs(65, 204).addBox(-2.5F, 8.5F, -2.5F, 5F, 1F, 5F, new CubeDeformation(-0.05F, -0.1F, -0.05F))
                .texOffs(86, 204).addBox(-2.5F, 9.9F, -2.5F, 5F, 1F, 5F, new CubeDeformation(-0.05F, -0.1F, -0.05F))
                .texOffs(115, 281).addBox(2.1F, 8F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.3F, -0.4F, -0.4F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_dusk_feet_right_leg = root.addOrReplaceChild("dusk_feet_right_leg", CubeListBuilder.create()
                .texOffs(50, 178).addBox(-3F, 6.15F, -3F, 6F, 2F, 6F, new CubeDeformation(-0.45F, -0.25F, -0.45F))
                .texOffs(107, 204).addBox(-2.5F, 8.5F, -2.5F, 5F, 1F, 5F, new CubeDeformation(-0.05F, -0.1F, -0.05F))
                .texOffs(128, 204).addBox(-2.5F, 9.9F, -2.5F, 5F, 1F, 5F, new CubeDeformation(-0.05F, -0.1F, -0.05F))
                .texOffs(122, 281).addBox(-3.1F, 8F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.3F, -0.4F, -0.4F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_starwalker_head_head = root.addOrReplaceChild("starwalker_head_head", CubeListBuilder.create()
                .texOffs(168, 47).addBox(-0.5F, -9.975F, -5F, 1F, 1F, 10F, new CubeDeformation(-0.05F, -0.025F, -0.1F))
                .texOffs(199, 281).addBox(-0.5F, -9.4F, -6F, 1F, 3F, 1F, new CubeDeformation(-0.125F, -0.25F, -0.25F))
                .texOffs(147, 310).addBox(-1F, -8.4F, -6F, 2F, 1F, 1F, new CubeDeformation(0F, -0.125F, -0.25F))
                .texOffs(63, 298).addBox(-1F, -8.9F, -6.075F, 2F, 2F, 1F, new CubeDeformation(-0.4375F, -0.4375F, -0.175F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_star_fin_left = p_starwalker_head_head.addOrReplaceChild("star_fin_left", CubeListBuilder.create()
                .texOffs(237, 250).addBox(-0.5F, -1.5F, -1F, 1F, 3F, 2F, new CubeDeformation(-0.1F, -0.465F, -0.08F)),
                PartPose.offsetAndRotation(5.25F, -5.6F, -1.6F, 0.2094F, 0.1745F, 0F));
        PartDefinition p_star_fin_left_blade_0 = p_star_fin_left.addOrReplaceChild("star_fin_left_blade_0", CubeListBuilder.create()
                .texOffs(84, 137).addBox(-0.5F, -1F, -0.435F, 1F, 2F, 8F, new CubeDeformation(-0.3F, -0.2525F, -0.435F)),
                PartPose.offsetAndRotation(0F, -0.345F, 0.23F, 0.1047F, 0F, 0F));
        PartDefinition p_star_fin_left_blade_1 = p_star_fin_left.addOrReplaceChild("star_fin_left_blade_1", CubeListBuilder.create()
                .texOffs(215, 178).addBox(-0.5F, -1F, -0.125F, 1F, 2F, 6F, new CubeDeformation(-0.3F, -0.3675F, -0.125F)),
                PartPose.offsetAndRotation(0F, -0.7475F, 0.23F, 0.384F, 0F, 0F));
        PartDefinition p_star_fin_left_blade_2 = p_star_fin_left.addOrReplaceChild("star_fin_left_blade_2", CubeListBuilder.create()
                .texOffs(117, 196).addBox(-0.5F, -1F, -0.43F, 1F, 2F, 5F, new CubeDeformation(-0.3F, -0.4825F, -0.43F)),
                PartPose.offsetAndRotation(0F, -1.15F, 0.23F, 0.6981F, 0F, 0F));
        PartDefinition p_star_fin_right = p_starwalker_head_head.addOrReplaceChild("star_fin_right", CubeListBuilder.create()
                .texOffs(244, 250).addBox(-0.5F, -1.5F, -1F, 1F, 3F, 2F, new CubeDeformation(-0.1F, -0.465F, -0.08F)),
                PartPose.offsetAndRotation(-5.25F, -5.6F, -1.6F, 0.2094F, -0.1745F, 0F));
        PartDefinition p_star_fin_right_blade_0 = p_star_fin_right.addOrReplaceChild("star_fin_right_blade_0", CubeListBuilder.create()
                .texOffs(103, 137).addBox(-0.5F, -1F, -0.435F, 1F, 2F, 8F, new CubeDeformation(-0.3F, -0.2525F, -0.435F)),
                PartPose.offsetAndRotation(0F, -0.345F, 0.23F, 0.1047F, 0F, 0F));
        PartDefinition p_star_fin_right_blade_1 = p_star_fin_right.addOrReplaceChild("star_fin_right_blade_1", CubeListBuilder.create()
                .texOffs(230, 178).addBox(-0.5F, -1F, -0.125F, 1F, 2F, 6F, new CubeDeformation(-0.3F, -0.3675F, -0.125F)),
                PartPose.offsetAndRotation(0F, -0.7475F, 0.23F, 0.384F, 0F, 0F));
        PartDefinition p_star_fin_right_blade_2 = p_star_fin_right.addOrReplaceChild("star_fin_right_blade_2", CubeListBuilder.create()
                .texOffs(130, 196).addBox(-0.5F, -1F, -0.43F, 1F, 2F, 5F, new CubeDeformation(-0.3F, -0.4825F, -0.43F)),
                PartPose.offsetAndRotation(0F, -1.15F, 0.23F, 0.6981F, 0F, 0F));
        PartDefinition p_starwalker_chest_body = root.addOrReplaceChild("starwalker_chest_body", CubeListBuilder.create()
                .texOffs(144, 238).addBox(-2F, 1.2F, -3.725F, 4F, 4F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.275F))
                .texOffs(204, 281).addBox(-0.5F, 1.7F, -4.2F, 1F, 3F, 1F, new CubeDeformation(-0.095F, -0.15F, -0.25F))
                .texOffs(103, 310).addBox(-1.5F, 2.7F, -4.2F, 3F, 1F, 1F, new CubeDeformation(-0.42F, -0.095F, -0.25F))
                .texOffs(70, 298).addBox(-1F, 2.2F, -4.275F, 2F, 2F, 1F, new CubeDeformation(-0.3925F, -0.3925F, -0.175F))
                .texOffs(77, 298).addBox(-4.9F, -1F, -3.75F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.3F, -0.15F))
                .texOffs(84, 298).addBox(2.9F, -1F, -3.75F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.3F, -0.15F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_star_cape = p_starwalker_chest_body.addOrReplaceChild("star_cape", CubeListBuilder.create()
                .texOffs(158, 187).addBox(-5F, -0.3F, -0.2F, 10F, 6F, 1F, new CubeDeformation(-0.4F, -0.3F, -0.2F)),
                PartPose.offsetAndRotation(0F, 0F, 3.1F, 0.0873F, 0F, 0F));
        PartDefinition p_star_cape_mid = p_star_cape.addOrReplaceChild("star_cape_mid", CubeListBuilder.create()
                .texOffs(25, 232).addBox(-4F, -0.1F, -0.2F, 8F, 4F, 1F, new CubeDeformation(-0.4F, -0.1F, -0.2F)),
                PartPose.offsetAndRotation(0F, 5.2F, 0.05F, 0.1047F, 0F, 0F));
        PartDefinition p_star_cape_tip = p_star_cape_mid.addOrReplaceChild("star_cape_tip", CubeListBuilder.create()
                .texOffs(216, 256).addBox(-2.5F, -0.1F, -0.2F, 5F, 3F, 1F, new CubeDeformation(-0.3F, -0.1F, -0.2F))
                .texOffs(91, 298).addBox(-1F, 2.5F, -0.2F, 2F, 2F, 1F, new CubeDeformation(0F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(0F, 3.6F, 0.05F, 0.1047F, 0F, 0F));
        PartDefinition p_starwalker_chest_left_arm = root.addOrReplaceChild("starwalker_chest_left_arm", CubeListBuilder.create()
                .texOffs(87, 158).addBox(-2.5F, 5.85F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.2F, -0.15F, -0.2F))
                .texOffs(25, 316).addBox(0.5F, 6.35F, -3.95F, 1F, 1F, 1F, new CubeDeformation(0F, 0F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_star_pauldron_left_arm = p_starwalker_chest_left_arm.addOrReplaceChild("star_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(186, 93).addBox(-3.2F, -1.7F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.2F, -0.4F))
                .texOffs(174, 126).addBox(-2.05F, -0.25F, -4F, 6F, 2F, 8F, new CubeDeformation(-0.05F, -0.35F, -0.3F))
                .texOffs(199, 158).addBox(-0.5F, -2.3F, -4F, 1F, 1F, 8F, new CubeDeformation(0F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(1F, -3.1F, 0F, 0F, 0F, 0.2094F));
        PartDefinition p_star_float_a_left_arm_0 = p_star_pauldron_left_arm.addOrReplaceChild("star_float_a_left_arm_0", CubeListBuilder.create()
                .texOffs(200, 306).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.05F, -0.45F, -0.05F)),
                PartPose.offsetAndRotation(1.6F, -3.6F, -1.2F, 0.1396F, 0F, 0.2443F));
        PartDefinition p_star_float_a_left_arm_1 = p_star_float_a_left_arm_0.addOrReplaceChild("star_float_a_left_arm_1", CubeListBuilder.create()
                .texOffs(30, 316).addBox(-0.5F, -1F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.2525F, 0F, -0.2525F)),
                PartPose.offset(0F, -0.85F, 0F));
        PartDefinition p_star_float_b_left_arm_0 = p_star_pauldron_left_arm.addOrReplaceChild("star_float_b_left_arm_0", CubeListBuilder.create()
                .texOffs(35, 316).addBox(-0.5F, -0.9125F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.15F, -0.0875F, -0.15F)),
                PartPose.offsetAndRotation(0.4F, -3F, 1.5F, -0.1745F, 0F, -0.1745F));
        PartDefinition p_star_float_b_left_arm_1 = p_star_float_b_left_arm_0.addOrReplaceChild("star_float_b_left_arm_1", CubeListBuilder.create()
                .texOffs(40, 316).addBox(-0.5F, -0.875F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.3075F, -0.125F, -0.3075F)),
                PartPose.offset(0F, -0.575F, 0F));
        PartDefinition p_starwalker_chest_right_arm = root.addOrReplaceChild("starwalker_chest_right_arm", CubeListBuilder.create()
                .texOffs(116, 158).addBox(-4.5F, 5.85F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.2F, -0.15F, -0.2F))
                .texOffs(45, 316).addBox(-1.5F, 6.35F, -3.95F, 1F, 1F, 1F, new CubeDeformation(0F, 0F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_star_pauldron_right_arm = p_starwalker_chest_right_arm.addOrReplaceChild("star_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(217, 93).addBox(-3.8F, -1.7F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.2F, -0.4F))
                .texOffs(203, 126).addBox(-3.95F, -0.25F, -4F, 6F, 2F, 8F, new CubeDeformation(-0.05F, -0.35F, -0.3F))
                .texOffs(218, 158).addBox(-0.5F, -2.3F, -4F, 1F, 1F, 8F, new CubeDeformation(0F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(-1F, -3.1F, 0F, 0F, 0F, -0.2094F));
        PartDefinition p_star_float_a_right_arm_0 = p_star_pauldron_right_arm.addOrReplaceChild("star_float_a_right_arm_0", CubeListBuilder.create()
                .texOffs(205, 306).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.05F, -0.45F, -0.05F)),
                PartPose.offsetAndRotation(-1.6F, -3.6F, -1.2F, 0.1396F, 0F, -0.2443F));
        PartDefinition p_star_float_a_right_arm_1 = p_star_float_a_right_arm_0.addOrReplaceChild("star_float_a_right_arm_1", CubeListBuilder.create()
                .texOffs(50, 316).addBox(-0.5F, -1F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.2525F, 0F, -0.2525F)),
                PartPose.offset(0F, -0.85F, 0F));
        PartDefinition p_star_float_b_right_arm_0 = p_star_pauldron_right_arm.addOrReplaceChild("star_float_b_right_arm_0", CubeListBuilder.create()
                .texOffs(55, 316).addBox(-0.5F, -0.9125F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.15F, -0.0875F, -0.15F)),
                PartPose.offsetAndRotation(-0.4F, -3F, 1.5F, -0.1745F, 0F, 0.1745F));
        PartDefinition p_star_float_b_right_arm_1 = p_star_float_b_right_arm_0.addOrReplaceChild("star_float_b_right_arm_1", CubeListBuilder.create()
                .texOffs(60, 316).addBox(-0.5F, -0.875F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.3075F, -0.125F, -0.3075F)),
                PartPose.offset(0F, -0.575F, 0F));
        PartDefinition p_starwalker_legs_body = root.addOrReplaceChild("starwalker_legs_body", CubeListBuilder.create()
                .texOffs(205, 286).addBox(-1.5F, 10.1F, -3.775F, 3F, 2F, 1F, new CubeDeformation(-0.4F, 0F, -0.275F))
                .texOffs(98, 298).addBox(-1F, 10.1F, -4.1F, 2F, 2F, 1F, new CubeDeformation(-0.35F, -0.35F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_starwalker_legs_left_leg = root.addOrReplaceChild("starwalker_legs_left_leg", CubeListBuilder.create()
                .texOffs(229, 256).addBox(-2.5F, 3.45F, -3.275F, 5F, 3F, 1F, new CubeDeformation(-0.3F, -0.05F, -0.125F))
                .texOffs(65, 316).addBox(-0.5F, 4.4F, -3.85F, 1F, 1F, 1F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(202, 225).addBox(2F, 3.45F, -3F, 1F, 3F, 3F, new CubeDeformation(-0.15F, -0.45F, -0.4F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_starwalker_legs_right_leg = root.addOrReplaceChild("starwalker_legs_right_leg", CubeListBuilder.create()
                .texOffs(242, 256).addBox(-2.5F, 3.45F, -3.275F, 5F, 3F, 1F, new CubeDeformation(-0.3F, -0.05F, -0.125F))
                .texOffs(70, 316).addBox(-0.5F, 4.4F, -3.85F, 1F, 1F, 1F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(211, 225).addBox(-3F, 3.45F, -3F, 1F, 3F, 3F, new CubeDeformation(-0.15F, -0.45F, -0.4F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_starwalker_feet_left_leg = root.addOrReplaceChild("starwalker_feet_left_leg", CubeListBuilder.create()
                .texOffs(0, 238).addBox(-2.5F, 10.1F, -4.4F, 5F, 3F, 2F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_star_heel_left_leg = p_starwalker_feet_left_leg.addOrReplaceChild("star_heel_left_leg", CubeListBuilder.create()
                .texOffs(210, 306).addBox(-0.5F, -1F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1F, -0.442F, -0.004F)),
                PartPose.offsetAndRotation(1.2F, 9.6F, 2.7F, 0.2443F, 0.1396F, 0F));
        PartDefinition p_star_heel_left_leg_blade_0 = p_star_heel_left_leg.addOrReplaceChild("star_heel_left_leg_blade_0", CubeListBuilder.create()
                .texOffs(155, 238).addBox(-0.5F, -0.5F, -0.078F, 1F, 1F, 4F, new CubeDeformation(-0.3F, -0.097F, -0.078F)),
                PartPose.offsetAndRotation(0F, -0.186F, 0.124F, 0.1047F, 0F, 0F));
        PartDefinition p_star_heel_left_leg_blade_1 = p_star_heel_left_leg.addOrReplaceChild("star_heel_left_leg_blade_1", CubeListBuilder.create()
                .texOffs(166, 238).addBox(-0.5F, -0.5F, -0.45F, 1F, 1F, 4F, new CubeDeformation(-0.3F, -0.159F, -0.45F)),
                PartPose.offsetAndRotation(0F, -0.403F, 0.124F, 0.384F, 0F, 0F));
        PartDefinition p_star_heel_left_leg_blade_2 = p_star_heel_left_leg.addOrReplaceChild("star_heel_left_leg_blade_2", CubeListBuilder.create()
                .texOffs(27, 281).addBox(-0.5F, -0.5F, -0.384F, 1F, 1F, 3F, new CubeDeformation(-0.3F, -0.221F, -0.384F)),
                PartPose.offsetAndRotation(0F, -0.62F, 0.124F, 0.6981F, 0F, 0F));
        PartDefinition p_starwalker_feet_right_leg = root.addOrReplaceChild("starwalker_feet_right_leg", CubeListBuilder.create()
                .texOffs(15, 238).addBox(-2.5F, 10.1F, -4.4F, 5F, 3F, 2F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_star_heel_right_leg = p_starwalker_feet_right_leg.addOrReplaceChild("star_heel_right_leg", CubeListBuilder.create()
                .texOffs(215, 306).addBox(-0.5F, -1F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1F, -0.442F, -0.004F)),
                PartPose.offsetAndRotation(-1.2F, 9.6F, 2.7F, 0.2443F, -0.1396F, 0F));
        PartDefinition p_star_heel_right_leg_blade_0 = p_star_heel_right_leg.addOrReplaceChild("star_heel_right_leg_blade_0", CubeListBuilder.create()
                .texOffs(177, 238).addBox(-0.5F, -0.5F, -0.078F, 1F, 1F, 4F, new CubeDeformation(-0.3F, -0.097F, -0.078F)),
                PartPose.offsetAndRotation(0F, -0.186F, 0.124F, 0.1047F, 0F, 0F));
        PartDefinition p_star_heel_right_leg_blade_1 = p_star_heel_right_leg.addOrReplaceChild("star_heel_right_leg_blade_1", CubeListBuilder.create()
                .texOffs(188, 238).addBox(-0.5F, -0.5F, -0.45F, 1F, 1F, 4F, new CubeDeformation(-0.3F, -0.159F, -0.45F)),
                PartPose.offsetAndRotation(0F, -0.403F, 0.124F, 0.384F, 0F, 0F));
        PartDefinition p_star_heel_right_leg_blade_2 = p_star_heel_right_leg.addOrReplaceChild("star_heel_right_leg_blade_2", CubeListBuilder.create()
                .texOffs(36, 281).addBox(-0.5F, -0.5F, -0.384F, 1F, 1F, 3F, new CubeDeformation(-0.3F, -0.221F, -0.384F)),
                PartPose.offsetAndRotation(0F, -0.62F, 0.124F, 0.6981F, 0F, 0F));
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
