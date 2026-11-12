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
                .texOffs(52, 267).addBox(-1F, -11.5F, 1.8F, 2F, 2F, 3F, new CubeDeformation(0F, -0.3F, -0.4F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_steel_plume_0 = p_steel_head_head.addOrReplaceChild("steel_plume_0", CubeListBuilder.create()
                .texOffs(200, 233).addBox(-1.5F, -2.8F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.2F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(0F, -10.6F, 3.3F, -0.2094F, 0F, 0F));
        PartDefinition p_steel_plume_1 = p_steel_plume_0.addOrReplaceChild("steel_plume_1", CubeListBuilder.create()
                .texOffs(136, 217).addBox(-1.5F, -3.6F, -1.5F, 3F, 4F, 3F, new CubeDeformation(0F, -0.4F, -0.1F)),
                PartPose.offsetAndRotation(0F, -2.35F, 0F, -0.4887F, 0F, 0F));
        PartDefinition p_steel_plume_2 = p_steel_plume_1.addOrReplaceChild("steel_plume_2", CubeListBuilder.create()
                .texOffs(149, 217).addBox(-1.5F, -3.8F, -1.5F, 3F, 4F, 3F, new CubeDeformation(-0.05F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(0F, -2.95F, 0F, -0.5585F, 0F, 0F));
        PartDefinition p_steel_plume_3 = p_steel_plume_2.addOrReplaceChild("steel_plume_3", CubeListBuilder.create()
                .texOffs(162, 217).addBox(-1.5F, -3.8F, -1.5F, 3F, 4F, 3F, new CubeDeformation(-0.25F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(0F, -3.35F, 0F, -0.5236F, 0F, 0F));
        PartDefinition p_steel_plume_4 = p_steel_plume_3.addOrReplaceChild("steel_plume_4", CubeListBuilder.create()
                .texOffs(55, 254).addBox(-1F, -3.7F, -1F, 2F, 4F, 2F, new CubeDeformation(0F, -0.3F, -0.1F)),
                PartPose.offsetAndRotation(0F, -3.35F, 0F, -0.384F, 0F, 0F));
        PartDefinition p_steel_plume_5 = p_steel_plume_4.addOrReplaceChild("steel_plume_5", CubeListBuilder.create()
                .texOffs(239, 267).addBox(-1F, -3F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.3F, 0F, -0.35F)),
                PartPose.offsetAndRotation(0F, -3.15F, 0F, -0.2443F, 0F, 0F));
        PartDefinition p_steel_chest_body = root.addOrReplaceChild("steel_chest_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_steel_chest_left_arm = root.addOrReplaceChild("steel_chest_left_arm", CubeListBuilder.create()
                .texOffs(158, 208).addBox(3.6F, 3.8F, -2F, 1F, 4F, 4F, new CubeDeformation(0F, -0.4F, -0.3F))
                .texOffs(90, 316).addBox(4.2F, 4.8F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.2F, -0.4F, -0.3F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_steel_pauldron_left_arm = p_steel_chest_left_arm.addOrReplaceChild("steel_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(82, 35).addBox(-3.8F, -2.15F, -4F, 8F, 3F, 8F, new CubeDeformation(-0.4F, -0.45F, -0.3F))
                .texOffs(123, 59).addBox(-2.5F, 0.1F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.2F, -0.1F, -0.2F))
                .texOffs(124, 104).addBox(-1.3F, 1.6F, -4F, 6F, 2F, 8F, new CubeDeformation(-0.2F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.2443F));
        PartDefinition p_steel_chest_right_arm = root.addOrReplaceChild("steel_chest_right_arm", CubeListBuilder.create()
                .texOffs(169, 208).addBox(-4.6F, 3.8F, -2F, 1F, 4F, 4F, new CubeDeformation(0F, -0.4F, -0.3F))
                .texOffs(97, 316).addBox(-5.2F, 4.8F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.2F, -0.4F, -0.3F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_steel_pauldron_right_arm = p_steel_chest_right_arm.addOrReplaceChild("steel_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(115, 35).addBox(-4.2F, -2.15F, -4F, 8F, 3F, 8F, new CubeDeformation(-0.4F, -0.45F, -0.3F))
                .texOffs(154, 59).addBox(-4.5F, 0.1F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.2F, -0.1F, -0.2F))
                .texOffs(153, 104).addBox(-4.7F, 1.6F, -4F, 6F, 2F, 8F, new CubeDeformation(-0.2F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.2443F));
        PartDefinition p_steel_legs_body = root.addOrReplaceChild("steel_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_steel_legs_left_leg = root.addOrReplaceChild("steel_legs_left_leg", CubeListBuilder.create()
                .texOffs(130, 291).addBox(-2F, 5F, -3.3F, 4F, 3F, 1F, new CubeDeformation(0F, -0.1F, 0F))
                .texOffs(248, 267).addBox(2F, 5.5F, -3.2F, 1F, 2F, 3F, new CubeDeformation(-0.1F, 0F, -0.4F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_steel_tasset_left_leg = p_steel_legs_left_leg.addOrReplaceChild("steel_tasset_left_leg", CubeListBuilder.create()
                .texOffs(80, 233).addBox(-3F, -0.1F, -0.6F, 6F, 5F, 1F, new CubeDeformation(-0.2F, -0.1F, 0F)),
                PartPose.offsetAndRotation(0F, -0.8F, -2.8F, -0.1222F, 0F, 0F));
        PartDefinition p_steel_legs_right_leg = root.addOrReplaceChild("steel_legs_right_leg", CubeListBuilder.create()
                .texOffs(141, 291).addBox(-2F, 5F, -3.3F, 4F, 3F, 1F, new CubeDeformation(0F, -0.1F, 0F))
                .texOffs(0, 273).addBox(-3F, 5.5F, -3.2F, 1F, 2F, 3F, new CubeDeformation(-0.1F, 0F, -0.4F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_steel_tasset_right_leg = p_steel_legs_right_leg.addOrReplaceChild("steel_tasset_right_leg", CubeListBuilder.create()
                .texOffs(95, 233).addBox(-3F, -0.1F, -0.6F, 6F, 5F, 1F, new CubeDeformation(-0.2F, -0.1F, 0F)),
                PartPose.offsetAndRotation(0F, -0.8F, -2.8F, -0.1222F, 0F, 0F));
        PartDefinition p_steel_feet_left_leg = root.addOrReplaceChild("steel_feet_left_leg", CubeListBuilder.create()
                .texOffs(239, 254).addBox(-2.5F, 10.2F, -4.45F, 5F, 3F, 2F, new CubeDeformation(0F, -0.3F, -0.25F))
                .texOffs(45, 326).addBox(-0.5F, 10.45F, 2.75F, 1F, 1F, 2F, new CubeDeformation(-0.15F, -0.15F, -0.05F))
                .texOffs(52, 326).addBox(-1F, 9.95F, 4.25F, 2F, 2F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_steel_feet_right_leg = root.addOrReplaceChild("steel_feet_right_leg", CubeListBuilder.create()
                .texOffs(0, 261).addBox(-2.5F, 10.2F, -4.45F, 5F, 3F, 2F, new CubeDeformation(0F, -0.3F, -0.25F))
                .texOffs(59, 326).addBox(-0.5F, 10.45F, 2.75F, 1F, 1F, 2F, new CubeDeformation(-0.15F, -0.15F, -0.05F))
                .texOffs(66, 326).addBox(-1F, 9.95F, 4.25F, 2F, 2F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_konterit_head_head = root.addOrReplaceChild("konterit_head_head", CubeListBuilder.create()
                .texOffs(73, 326).addBox(-1F, -6.8F, -5.75F, 2F, 2F, 1F, new CubeDeformation(0F, 0F, -0.15F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_kon_wing_left = p_konterit_head_head.addOrReplaceChild("kon_wing_left", CubeListBuilder.create()
                .texOffs(52, 225).addBox(-1F, -2.58F, -1.79F, 2F, 4F, 3F, new CubeDeformation(-0.4F, -0.26F, -0.05F)),
                PartPose.offsetAndRotation(5.2F, -5.2F, -0.8F, 0.2793F, 0.2793F, 0F));
        PartDefinition p_kon_wing_left_feather_0 = p_kon_wing_left.addOrReplaceChild("kon_wing_left_feather_0", CubeListBuilder.create()
                .texOffs(0, 189).addBox(-0.5F, -1F, -0.165F, 1F, 2F, 7F, new CubeDeformation(-0.25F, -0.0575F, -0.165F)),
                PartPose.offsetAndRotation(0F, -0.58F, 0.58F, 0.0698F, 0F, 0F));
        PartDefinition p_kon_wing_left_feather_1 = p_kon_wing_left.addOrReplaceChild("kon_wing_left_feather_1", CubeListBuilder.create()
                .texOffs(191, 47).addBox(-0.5F, -1F, -0.44F, 1F, 2F, 9F, new CubeDeformation(-0.25F, -0.0575F, -0.44F)),
                PartPose.offsetAndRotation(0F, -0.9425F, 0.58F, 0.3491F, 0F, 0F));
        PartDefinition p_kon_wing_left_feather_2 = p_kon_wing_left.addOrReplaceChild("kon_wing_left_feather_2", CubeListBuilder.create()
                .texOffs(212, 47).addBox(-0.5F, -1F, -0.295F, 1F, 2F, 9F, new CubeDeformation(-0.25F, -0.13F, -0.295F)),
                PartPose.offsetAndRotation(0F, -1.305F, 0.58F, 0.6283F, 0F, 0F));
        PartDefinition p_kon_wing_left_feather_3 = p_kon_wing_left.addOrReplaceChild("kon_wing_left_feather_3", CubeListBuilder.create()
                .texOffs(69, 148).addBox(-0.5F, -1F, -0.375F, 1F, 2F, 8F, new CubeDeformation(-0.25F, -0.2025F, -0.375F)),
                PartPose.offsetAndRotation(0F, -1.6675F, 0.58F, 0.9076F, 0F, 0F));
        PartDefinition p_kon_wing_left_feather_4 = p_kon_wing_left.addOrReplaceChild("kon_wing_left_feather_4", CubeListBuilder.create()
                .texOffs(55, 208).addBox(-0.5F, -1F, -0.245F, 1F, 2F, 6F, new CubeDeformation(-0.25F, -0.275F, -0.245F)),
                PartPose.offsetAndRotation(0F, -2.03F, 0.58F, 1.1868F, 0F, 0F));
        PartDefinition p_kon_wing_right = p_konterit_head_head.addOrReplaceChild("kon_wing_right", CubeListBuilder.create()
                .texOffs(63, 225).addBox(-1F, -2.58F, -1.79F, 2F, 4F, 3F, new CubeDeformation(-0.4F, -0.26F, -0.05F)),
                PartPose.offsetAndRotation(-5.2F, -5.2F, -0.8F, 0.2793F, -0.2793F, 0F));
        PartDefinition p_kon_wing_right_feather_0 = p_kon_wing_right.addOrReplaceChild("kon_wing_right_feather_0", CubeListBuilder.create()
                .texOffs(17, 189).addBox(-0.5F, -1F, -0.165F, 1F, 2F, 7F, new CubeDeformation(-0.25F, -0.0575F, -0.165F)),
                PartPose.offsetAndRotation(0F, -0.58F, 0.58F, 0.0698F, 0F, 0F));
        PartDefinition p_kon_wing_right_feather_1 = p_kon_wing_right.addOrReplaceChild("kon_wing_right_feather_1", CubeListBuilder.create()
                .texOffs(233, 47).addBox(-0.5F, -1F, -0.44F, 1F, 2F, 9F, new CubeDeformation(-0.25F, -0.0575F, -0.44F)),
                PartPose.offsetAndRotation(0F, -0.9425F, 0.58F, 0.3491F, 0F, 0F));
        PartDefinition p_kon_wing_right_feather_2 = p_kon_wing_right.addOrReplaceChild("kon_wing_right_feather_2", CubeListBuilder.create()
                .texOffs(0, 59).addBox(-0.5F, -1F, -0.295F, 1F, 2F, 9F, new CubeDeformation(-0.25F, -0.13F, -0.295F)),
                PartPose.offsetAndRotation(0F, -1.305F, 0.58F, 0.6283F, 0F, 0F));
        PartDefinition p_kon_wing_right_feather_3 = p_kon_wing_right.addOrReplaceChild("kon_wing_right_feather_3", CubeListBuilder.create()
                .texOffs(88, 148).addBox(-0.5F, -1F, -0.375F, 1F, 2F, 8F, new CubeDeformation(-0.25F, -0.2025F, -0.375F)),
                PartPose.offsetAndRotation(0F, -1.6675F, 0.58F, 0.9076F, 0F, 0F));
        PartDefinition p_kon_wing_right_feather_4 = p_kon_wing_right.addOrReplaceChild("kon_wing_right_feather_4", CubeListBuilder.create()
                .texOffs(70, 208).addBox(-0.5F, -1F, -0.245F, 1F, 2F, 6F, new CubeDeformation(-0.25F, -0.275F, -0.245F)),
                PartPose.offsetAndRotation(0F, -2.03F, 0.58F, 1.1868F, 0F, 0F));
        PartDefinition p_konterit_chest_body = root.addOrReplaceChild("konterit_chest_body", CubeListBuilder.create()
                .texOffs(80, 326).addBox(-5.2F, -1.1F, -3.65F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.3F, -0.15F))
                .texOffs(87, 326).addBox(3.2F, -1.1F, -3.65F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.3F, -0.15F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_kon_cape = p_konterit_chest_body.addOrReplaceChild("kon_cape", CubeListBuilder.create()
                .texOffs(208, 199).addBox(-4.5F, -0.2F, -0.2F, 9F, 7F, 1F, new CubeDeformation(0F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(0F, 0.2F, 3F, 0.1047F, 0F, 0F));
        PartDefinition p_kon_cape_hem = p_kon_cape.addOrReplaceChild("kon_cape_hem", CubeListBuilder.create()
                .texOffs(98, 217).addBox(-4.5F, -0.4F, -0.2F, 9F, 6F, 1F, new CubeDeformation(-0.2F, -0.4F, -0.2F)),
                PartPose.offsetAndRotation(0F, 6.4F, 0.05F, 0.1396F, 0F, 0F));
        PartDefinition p_konterit_chest_left_arm = root.addOrReplaceChild("konterit_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_kon_pauldron_left_arm = p_konterit_chest_left_arm.addOrReplaceChild("kon_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(0, 159).addBox(-3.2F, -1.55F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.2F, -0.25F, 0F))
                .texOffs(0, 208).addBox(-1F, -2.05F, -3.5F, 2F, 1F, 7F, new CubeDeformation(-0.4F, -0.15F, 0F))
                .texOffs(154, 321).addBox(1F, -2.2F, -1F, 2F, 1F, 2F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offsetAndRotation(1F, -3.2F, 0F, 0F, 0F, 0.2094F));
        PartDefinition p_konterit_chest_right_arm = root.addOrReplaceChild("konterit_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_kon_pauldron_right_arm = p_konterit_chest_right_arm.addOrReplaceChild("kon_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(29, 159).addBox(-3.8F, -1.55F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.2F, -0.25F, 0F))
                .texOffs(19, 208).addBox(-1F, -2.05F, -3.5F, 2F, 1F, 7F, new CubeDeformation(-0.4F, -0.15F, 0F))
                .texOffs(163, 321).addBox(-3F, -2.2F, -1F, 2F, 1F, 2F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offsetAndRotation(-1F, -3.2F, 0F, 0F, 0F, -0.2094F));
        PartDefinition p_konterit_legs_body = root.addOrReplaceChild("konterit_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_konterit_legs_left_leg = root.addOrReplaceChild("konterit_legs_left_leg", CubeListBuilder.create()
                .texOffs(9, 273).addBox(-1.5F, 4.2F, -3.1F, 3F, 4F, 1F, new CubeDeformation(0F, -0.4F, -0.1F))
                .texOffs(94, 326).addBox(-1F, 5.2F, -3.65F, 2F, 2F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_konterit_legs_right_leg = root.addOrReplaceChild("konterit_legs_right_leg", CubeListBuilder.create()
                .texOffs(18, 273).addBox(-1.5F, 4.2F, -3.1F, 3F, 4F, 1F, new CubeDeformation(0F, -0.4F, -0.1F))
                .texOffs(101, 326).addBox(-1F, 5.2F, -3.65F, 2F, 2F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_konterit_feet_left_leg = root.addOrReplaceChild("konterit_feet_left_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_kon_heel_left_leg = p_konterit_feet_left_leg.addOrReplaceChild("kon_heel_left_leg", CubeListBuilder.create()
                .texOffs(27, 273).addBox(-1F, -1.84F, -1.17F, 2F, 3F, 2F, new CubeDeformation(-0.4F, -0.48F, -0.15F)),
                PartPose.offsetAndRotation(2.9F, 9.6F, 0.2F, 0.4189F, 0.2443F, 0F));
        PartDefinition p_kon_heel_left_leg_feather_0 = p_kon_heel_left_leg.addOrReplaceChild("kon_heel_left_leg_feather_0", CubeListBuilder.create()
                .texOffs(208, 247).addBox(-0.5F, -1F, -0.045F, 1F, 2F, 4F, new CubeDeformation(-0.25F, -0.4475F, -0.045F)),
                PartPose.offsetAndRotation(0F, -0.34F, 0.34F, 0.0698F, 0F, 0F));
        PartDefinition p_kon_heel_left_leg_feather_1 = p_kon_heel_left_leg.addOrReplaceChild("kon_heel_left_leg_feather_1", CubeListBuilder.create()
                .texOffs(175, 217).addBox(-0.5F, -1F, -0.12F, 1F, 2F, 5F, new CubeDeformation(-0.25F, -0.4475F, -0.12F)),
                PartPose.offsetAndRotation(0F, -0.5525F, 0.34F, 0.3491F, 0F, 0F));
        PartDefinition p_kon_heel_left_leg_feather_2 = p_kon_heel_left_leg.addOrReplaceChild("kon_heel_left_leg_feather_2", CubeListBuilder.create()
                .texOffs(188, 217).addBox(-0.5F, -1F, -0.035F, 1F, 2F, 5F, new CubeDeformation(-0.25F, -0.49F, -0.035F)),
                PartPose.offsetAndRotation(0F, -0.765F, 0.34F, 0.6283F, 0F, 0F));
        PartDefinition p_kon_heel_left_leg_feather_3 = p_kon_heel_left_leg.addOrReplaceChild("kon_heel_left_leg_feather_3", CubeListBuilder.create()
                .texOffs(213, 233).addBox(-0.5F, -0.5F, -0.375F, 1F, 1F, 5F, new CubeDeformation(-0.25F, -0.0325F, -0.375F)),
                PartPose.offsetAndRotation(0F, -0.9775F, 0.34F, 0.9076F, 0F, 0F));
        PartDefinition p_kon_heel_left_leg_feather_4 = p_kon_heel_left_leg.addOrReplaceChild("kon_heel_left_leg_feather_4", CubeListBuilder.create()
                .texOffs(63, 267).addBox(-0.5F, -0.5F, -0.385F, 1F, 1F, 4F, new CubeDeformation(-0.25F, -0.075F, -0.385F)),
                PartPose.offsetAndRotation(0F, -1.19F, 0.34F, 1.1868F, 0F, 0F));
        PartDefinition p_konterit_feet_right_leg = root.addOrReplaceChild("konterit_feet_right_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_kon_heel_right_leg = p_konterit_feet_right_leg.addOrReplaceChild("kon_heel_right_leg", CubeListBuilder.create()
                .texOffs(36, 273).addBox(-1F, -1.84F, -1.17F, 2F, 3F, 2F, new CubeDeformation(-0.4F, -0.48F, -0.15F)),
                PartPose.offsetAndRotation(-2.9F, 9.6F, 0.2F, 0.4189F, -0.2443F, 0F));
        PartDefinition p_kon_heel_right_leg_feather_0 = p_kon_heel_right_leg.addOrReplaceChild("kon_heel_right_leg_feather_0", CubeListBuilder.create()
                .texOffs(219, 247).addBox(-0.5F, -1F, -0.045F, 1F, 2F, 4F, new CubeDeformation(-0.25F, -0.4475F, -0.045F)),
                PartPose.offsetAndRotation(0F, -0.34F, 0.34F, 0.0698F, 0F, 0F));
        PartDefinition p_kon_heel_right_leg_feather_1 = p_kon_heel_right_leg.addOrReplaceChild("kon_heel_right_leg_feather_1", CubeListBuilder.create()
                .texOffs(201, 217).addBox(-0.5F, -1F, -0.12F, 1F, 2F, 5F, new CubeDeformation(-0.25F, -0.4475F, -0.12F)),
                PartPose.offsetAndRotation(0F, -0.5525F, 0.34F, 0.3491F, 0F, 0F));
        PartDefinition p_kon_heel_right_leg_feather_2 = p_kon_heel_right_leg.addOrReplaceChild("kon_heel_right_leg_feather_2", CubeListBuilder.create()
                .texOffs(214, 217).addBox(-0.5F, -1F, -0.035F, 1F, 2F, 5F, new CubeDeformation(-0.25F, -0.49F, -0.035F)),
                PartPose.offsetAndRotation(0F, -0.765F, 0.34F, 0.6283F, 0F, 0F));
        PartDefinition p_kon_heel_right_leg_feather_3 = p_kon_heel_right_leg.addOrReplaceChild("kon_heel_right_leg_feather_3", CubeListBuilder.create()
                .texOffs(226, 233).addBox(-0.5F, -0.5F, -0.375F, 1F, 1F, 5F, new CubeDeformation(-0.25F, -0.0325F, -0.375F)),
                PartPose.offsetAndRotation(0F, -0.9775F, 0.34F, 0.9076F, 0F, 0F));
        PartDefinition p_kon_heel_right_leg_feather_4 = p_kon_heel_right_leg.addOrReplaceChild("kon_heel_right_leg_feather_4", CubeListBuilder.create()
                .texOffs(74, 267).addBox(-0.5F, -0.5F, -0.385F, 1F, 1F, 4F, new CubeDeformation(-0.25F, -0.075F, -0.385F)),
                PartPose.offsetAndRotation(0F, -1.19F, 0.34F, 1.1868F, 0F, 0F));
        PartDefinition p_nightsteel_head_head = root.addOrReplaceChild("nightsteel_head_head", CubeListBuilder.create()
                .texOffs(5, 321).addBox(-5.5F, -9.5F, -3.9F, 11F, 1F, 2F, new CubeDeformation(-0.2F, 0F, -0.4F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_night_crown_0_0 = p_nightsteel_head_head.addOrReplaceChild("night_crown_0_0", CubeListBuilder.create()
                .texOffs(198, 296).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.25F, -0.285F, -0.25F)),
                PartPose.offsetAndRotation(-3.6F, -9.3F, -2.9F, -0.1396F, 0F, -0.377F));
        PartDefinition p_night_crown_0_1 = p_night_crown_0_0.addOrReplaceChild("night_crown_0_1", CubeListBuilder.create()
                .texOffs(14, 338).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.0875F, -0.35F, -0.0875F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_night_crown_1_0 = p_nightsteel_head_head.addOrReplaceChild("night_crown_1_0", CubeListBuilder.create()
                .texOffs(45, 273).addBox(-1F, -2.545F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.25F, -0.455F, -0.25F)),
                PartPose.offsetAndRotation(-1.8F, -9.3F, -2.9F, -0.1396F, 0F, -0.1885F));
        PartDefinition p_night_crown_1_1 = p_night_crown_1_0.addOrReplaceChild("night_crown_1_1", CubeListBuilder.create()
                .texOffs(19, 338).addBox(-0.5F, -1.95F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.0875F, -0.05F, -0.0875F)),
                PartPose.offset(0F, -1.84F, 0F));
        PartDefinition p_night_crown_2_0 = p_nightsteel_head_head.addOrReplaceChild("night_crown_2_0", CubeListBuilder.create()
                .texOffs(54, 273).addBox(-1F, -2.93F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.1F, -0.07F, -0.1F)),
                PartPose.offsetAndRotation(0F, -9.3F, -2.9F, -0.1396F, 0F, 0F));
        PartDefinition p_night_crown_2_1 = p_night_crown_2_0.addOrReplaceChild("night_crown_2_1", CubeListBuilder.create()
                .texOffs(174, 316).addBox(-0.5F, -2.8F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.005F, -0.2F, -0.005F)),
                PartPose.offset(0F, -2.61F, 0F));
        PartDefinition p_night_crown_3_0 = p_nightsteel_head_head.addOrReplaceChild("night_crown_3_0", CubeListBuilder.create()
                .texOffs(63, 273).addBox(-1F, -2.545F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.25F, -0.455F, -0.25F)),
                PartPose.offsetAndRotation(1.8F, -9.3F, -2.9F, -0.1396F, 0F, 0.1885F));
        PartDefinition p_night_crown_3_1 = p_night_crown_3_0.addOrReplaceChild("night_crown_3_1", CubeListBuilder.create()
                .texOffs(24, 338).addBox(-0.5F, -1.95F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.0875F, -0.05F, -0.0875F)),
                PartPose.offset(0F, -1.84F, 0F));
        PartDefinition p_night_crown_4_0 = p_nightsteel_head_head.addOrReplaceChild("night_crown_4_0", CubeListBuilder.create()
                .texOffs(207, 296).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.25F, -0.285F, -0.25F)),
                PartPose.offsetAndRotation(3.6F, -9.3F, -2.9F, -0.1396F, 0F, 0.377F));
        PartDefinition p_night_crown_4_1 = p_night_crown_4_0.addOrReplaceChild("night_crown_4_1", CubeListBuilder.create()
                .texOffs(29, 338).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.0875F, -0.35F, -0.0875F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_nightsteel_chest_body = root.addOrReplaceChild("nightsteel_chest_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_night_back_0_0 = p_nightsteel_chest_body.addOrReplaceChild("night_back_0_0", CubeListBuilder.create()
                .texOffs(72, 273).addBox(-1F, -2.6F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.2F, -0.4F, -0.2F)),
                PartPose.offsetAndRotation(0F, 1.6F, 2.9F, -0.9076F, 0F, 0F));
        PartDefinition p_night_back_0_1 = p_night_back_0_0.addOrReplaceChild("night_back_0_1", CubeListBuilder.create()
                .texOffs(34, 338).addBox(-0.5F, -2F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.06F, 0F, -0.06F)),
                PartPose.offset(0F, -1.95F, 0F));
        PartDefinition p_night_back_1_0 = p_nightsteel_chest_body.addOrReplaceChild("night_back_1_0", CubeListBuilder.create()
                .texOffs(216, 296).addBox(-1F, -1.88F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.2F, -0.12F, -0.2F)),
                PartPose.offsetAndRotation(0F, 4.6F, 2.9F, -0.9076F, 0F, 0F));
        PartDefinition p_night_back_1_1 = p_night_back_1_0.addOrReplaceChild("night_back_1_1", CubeListBuilder.create()
                .texOffs(39, 338).addBox(-0.5F, -1.8F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.06F, -0.2F, -0.06F)),
                PartPose.offset(0F, -1.51F, 0F));
        PartDefinition p_night_back_2_0 = p_nightsteel_chest_body.addOrReplaceChild("night_back_2_0", CubeListBuilder.create()
                .texOffs(225, 296).addBox(-1F, -1.66F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.2F, -0.34F, -0.2F)),
                PartPose.offsetAndRotation(0F, 7.6F, 2.9F, -0.9076F, 0F, 0F));
        PartDefinition p_night_back_2_1 = p_night_back_2_0.addOrReplaceChild("night_back_2_1", CubeListBuilder.create()
                .texOffs(44, 338).addBox(-0.5F, -1.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.06F, -0.4F, -0.06F)),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_nightsteel_chest_left_arm = root.addOrReplaceChild("nightsteel_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_night_pauldron_left_arm = p_nightsteel_chest_left_arm.addOrReplaceChild("night_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(185, 59).addBox(-3.3F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.1745F));
        PartDefinition p_night_shoulder_left_arm_0_0 = p_night_pauldron_left_arm.addOrReplaceChild("night_shoulder_left_arm_0_0", CubeListBuilder.create()
                .texOffs(234, 296).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.285F, -0.3F)),
                PartPose.offsetAndRotation(2.2F, -1.4F, -2.2F, 0F, 0F, 0.4887F));
        PartDefinition p_night_shoulder_left_arm_0_1 = p_night_shoulder_left_arm_0_0.addOrReplaceChild("night_shoulder_left_arm_0_1", CubeListBuilder.create()
                .texOffs(49, 338).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.35F, -0.115F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_night_shoulder_left_arm_1_0 = p_night_pauldron_left_arm.addOrReplaceChild("night_shoulder_left_arm_1_0", CubeListBuilder.create()
                .texOffs(243, 296).addBox(-1F, -1.99F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.01F, -0.3F)),
                PartPose.offsetAndRotation(2.2F, -1.4F, 0F, 0F, 0F, 0.4887F));
        PartDefinition p_night_shoulder_left_arm_1_1 = p_night_shoulder_left_arm_1_0.addOrReplaceChild("night_shoulder_left_arm_1_1", CubeListBuilder.create()
                .texOffs(54, 338).addBox(-0.5F, -1.9F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.1F, -0.115F)),
                PartPose.offset(0F, -1.73F, 0F));
        PartDefinition p_night_shoulder_left_arm_2_0 = p_night_pauldron_left_arm.addOrReplaceChild("night_shoulder_left_arm_2_0", CubeListBuilder.create()
                .texOffs(0, 301).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.285F, -0.3F)),
                PartPose.offsetAndRotation(2.2F, -1.4F, 2.2F, 0F, 0F, 0.4887F));
        PartDefinition p_night_shoulder_left_arm_2_1 = p_night_shoulder_left_arm_2_0.addOrReplaceChild("night_shoulder_left_arm_2_1", CubeListBuilder.create()
                .texOffs(59, 338).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.35F, -0.115F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_nightsteel_chest_right_arm = root.addOrReplaceChild("nightsteel_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_night_pauldron_right_arm = p_nightsteel_chest_right_arm.addOrReplaceChild("night_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(216, 59).addBox(-3.7F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.1745F));
        PartDefinition p_night_shoulder_right_arm_0_0 = p_night_pauldron_right_arm.addOrReplaceChild("night_shoulder_right_arm_0_0", CubeListBuilder.create()
                .texOffs(9, 301).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.285F, -0.3F)),
                PartPose.offsetAndRotation(-2.2F, -1.4F, -2.2F, 0F, 0F, -0.4887F));
        PartDefinition p_night_shoulder_right_arm_0_1 = p_night_shoulder_right_arm_0_0.addOrReplaceChild("night_shoulder_right_arm_0_1", CubeListBuilder.create()
                .texOffs(64, 338).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.35F, -0.115F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_night_shoulder_right_arm_1_0 = p_night_pauldron_right_arm.addOrReplaceChild("night_shoulder_right_arm_1_0", CubeListBuilder.create()
                .texOffs(18, 301).addBox(-1F, -1.99F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.01F, -0.3F)),
                PartPose.offsetAndRotation(-2.2F, -1.4F, 0F, 0F, 0F, -0.4887F));
        PartDefinition p_night_shoulder_right_arm_1_1 = p_night_shoulder_right_arm_1_0.addOrReplaceChild("night_shoulder_right_arm_1_1", CubeListBuilder.create()
                .texOffs(69, 338).addBox(-0.5F, -1.9F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.1F, -0.115F)),
                PartPose.offset(0F, -1.73F, 0F));
        PartDefinition p_night_shoulder_right_arm_2_0 = p_night_pauldron_right_arm.addOrReplaceChild("night_shoulder_right_arm_2_0", CubeListBuilder.create()
                .texOffs(27, 301).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.285F, -0.3F)),
                PartPose.offsetAndRotation(-2.2F, -1.4F, 2.2F, 0F, 0F, -0.4887F));
        PartDefinition p_night_shoulder_right_arm_2_1 = p_night_shoulder_right_arm_2_0.addOrReplaceChild("night_shoulder_right_arm_2_1", CubeListBuilder.create()
                .texOffs(74, 338).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.35F, -0.115F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_nightsteel_legs_body = root.addOrReplaceChild("nightsteel_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_nightsteel_legs_left_leg = root.addOrReplaceChild("nightsteel_legs_left_leg", CubeListBuilder.create()
                .texOffs(152, 291).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(-0.1F, 0F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_night_knee_left_leg_0 = p_nightsteel_legs_left_leg.addOrReplaceChild("night_knee_left_leg_0", CubeListBuilder.create()
                .texOffs(36, 301).addBox(-1F, -1.77F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.23F, -0.35F)),
                PartPose.offsetAndRotation(0F, 6F, -3F, 1.0821F, 0F, 0F));
        PartDefinition p_night_knee_left_leg_1 = p_night_knee_left_leg_0.addOrReplaceChild("night_knee_left_leg_1", CubeListBuilder.create()
                .texOffs(79, 338).addBox(-0.5F, -1.7F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.3F, -0.1425F)),
                PartPose.offset(0F, -1.29F, 0F));
        PartDefinition p_nightsteel_legs_right_leg = root.addOrReplaceChild("nightsteel_legs_right_leg", CubeListBuilder.create()
                .texOffs(163, 291).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(-0.1F, 0F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_night_knee_right_leg_0 = p_nightsteel_legs_right_leg.addOrReplaceChild("night_knee_right_leg_0", CubeListBuilder.create()
                .texOffs(45, 301).addBox(-1F, -1.77F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.23F, -0.35F)),
                PartPose.offsetAndRotation(0F, 6F, -3F, 1.0821F, 0F, 0F));
        PartDefinition p_night_knee_right_leg_1 = p_night_knee_right_leg_0.addOrReplaceChild("night_knee_right_leg_1", CubeListBuilder.create()
                .texOffs(84, 338).addBox(-0.5F, -1.7F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.3F, -0.1425F)),
                PartPose.offset(0F, -1.29F, 0F));
        PartDefinition p_nightsteel_feet_left_leg = root.addOrReplaceChild("nightsteel_feet_left_leg", CubeListBuilder.create()
                .texOffs(117, 285).addBox(-2.5F, 10.25F, -3.9F, 5F, 3F, 1F, new CubeDeformation(-0.1F, -0.45F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_night_heel_left_leg_0 = p_nightsteel_feet_left_leg.addOrReplaceChild("night_heel_left_leg_0", CubeListBuilder.create()
                .texOffs(54, 301).addBox(-1F, -1.66F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.34F, -0.4F)),
                PartPose.offsetAndRotation(0F, 11F, 2.8F, -1.0123F, 0F, 0F));
        PartDefinition p_night_heel_left_leg_1 = p_night_heel_left_leg_0.addOrReplaceChild("night_heel_left_leg_1", CubeListBuilder.create()
                .texOffs(89, 338).addBox(-0.5F, -1.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.17F, -0.4F, -0.17F)),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_nightsteel_feet_right_leg = root.addOrReplaceChild("nightsteel_feet_right_leg", CubeListBuilder.create()
                .texOffs(130, 285).addBox(-2.5F, 10.25F, -3.9F, 5F, 3F, 1F, new CubeDeformation(-0.1F, -0.45F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_night_heel_right_leg_0 = p_nightsteel_feet_right_leg.addOrReplaceChild("night_heel_right_leg_0", CubeListBuilder.create()
                .texOffs(63, 301).addBox(-1F, -1.66F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.34F, -0.4F)),
                PartPose.offsetAndRotation(0F, 11F, 2.8F, -1.0123F, 0F, 0F));
        PartDefinition p_night_heel_right_leg_1 = p_night_heel_right_leg_0.addOrReplaceChild("night_heel_right_leg_1", CubeListBuilder.create()
                .texOffs(94, 338).addBox(-0.5F, -1.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.17F, -0.4F, -0.17F)),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_abyssite_head_head = root.addOrReplaceChild("abyssite_head_head", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_aby_horn_left_0 = p_abyssite_head_head.addOrReplaceChild("aby_horn_left_0", CubeListBuilder.create()
                .texOffs(239, 233).addBox(-1.5F, -3F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offsetAndRotation(4F, -8.6F, -1.2F, -0.2094F, 0F, 0.5934F));
        PartDefinition p_aby_horn_left_1 = p_aby_horn_left_0.addOrReplaceChild("aby_horn_left_1", CubeListBuilder.create()
                .texOffs(0, 240).addBox(-1.5F, -3F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.45F, 0F, -0.45F)),
                PartPose.offsetAndRotation(0F, -2.75F, 0F, -0.4887F, 0F, -0.1745F));
        PartDefinition p_aby_horn_left_2 = p_aby_horn_left_1.addOrReplaceChild("aby_horn_left_2", CubeListBuilder.create()
                .texOffs(81, 273).addBox(-1F, -2.9F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.2F, -0.1F, -0.2F)),
                PartPose.offsetAndRotation(0F, -2.75F, 0F, -0.5934F, 0F, -0.2094F));
        PartDefinition p_aby_horn_left_3 = p_aby_horn_left_2.addOrReplaceChild("aby_horn_left_3", CubeListBuilder.create()
                .texOffs(90, 273).addBox(-1F, -2.7F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.45F, -0.3F, -0.45F)),
                PartPose.offsetAndRotation(0F, -2.55F, 0F, -0.6283F, 0F, -0.1047F));
        PartDefinition p_aby_horn_left_4 = p_aby_horn_left_3.addOrReplaceChild("aby_horn_left_4", CubeListBuilder.create()
                .texOffs(99, 338).addBox(-0.5F, -1.9F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.15F, -0.1F, -0.15F)),
                PartPose.offsetAndRotation(0F, -2.15F, 0F, -0.5236F, 0F, 0F));
        PartDefinition p_aby_horn_right_0 = p_abyssite_head_head.addOrReplaceChild("aby_horn_right_0", CubeListBuilder.create()
                .texOffs(13, 240).addBox(-1.5F, -3F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offsetAndRotation(-4F, -8.6F, -1.2F, -0.2094F, 0F, -0.5934F));
        PartDefinition p_aby_horn_right_1 = p_aby_horn_right_0.addOrReplaceChild("aby_horn_right_1", CubeListBuilder.create()
                .texOffs(26, 240).addBox(-1.5F, -3F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.45F, 0F, -0.45F)),
                PartPose.offsetAndRotation(0F, -2.75F, 0F, -0.4887F, 0F, 0.1745F));
        PartDefinition p_aby_horn_right_2 = p_aby_horn_right_1.addOrReplaceChild("aby_horn_right_2", CubeListBuilder.create()
                .texOffs(99, 273).addBox(-1F, -2.9F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.2F, -0.1F, -0.2F)),
                PartPose.offsetAndRotation(0F, -2.75F, 0F, -0.5934F, 0F, 0.2094F));
        PartDefinition p_aby_horn_right_3 = p_aby_horn_right_2.addOrReplaceChild("aby_horn_right_3", CubeListBuilder.create()
                .texOffs(108, 273).addBox(-1F, -2.7F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.45F, -0.3F, -0.45F)),
                PartPose.offsetAndRotation(0F, -2.55F, 0F, -0.6283F, 0F, 0.1047F));
        PartDefinition p_aby_horn_right_4 = p_aby_horn_right_3.addOrReplaceChild("aby_horn_right_4", CubeListBuilder.create()
                .texOffs(104, 338).addBox(-0.5F, -1.9F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.15F, -0.1F, -0.15F)),
                PartPose.offsetAndRotation(0F, -2.15F, 0F, -0.5236F, 0F, 0F));
        PartDefinition p_aby_brow_0 = p_abyssite_head_head.addOrReplaceChild("aby_brow_0", CubeListBuilder.create()
                .texOffs(72, 301).addBox(-1F, -1.88F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.2F, -0.12F, -0.2F)),
                PartPose.offsetAndRotation(0F, -6.2F, -5.2F, -0.2443F, 0F, 0F));
        PartDefinition p_aby_brow_1 = p_aby_brow_0.addOrReplaceChild("aby_brow_1", CubeListBuilder.create()
                .texOffs(109, 338).addBox(-0.5F, -1.8F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.06F, -0.2F, -0.06F)),
                PartPose.offset(0F, -1.51F, 0F));
        PartDefinition p_abyssite_chest_body = root.addOrReplaceChild("abyssite_chest_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_aby_back_0_0 = p_abyssite_chest_body.addOrReplaceChild("aby_back_0_0", CubeListBuilder.create()
                .texOffs(117, 273).addBox(-1F, -2.82F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.1F, -0.18F, -0.1F)),
                PartPose.offsetAndRotation(0F, 3F, 2.9F, -0.8029F, 0F, 0F));
        PartDefinition p_aby_back_0_1 = p_aby_back_0_0.addOrReplaceChild("aby_back_0_1", CubeListBuilder.create()
                .texOffs(179, 316).addBox(-0.5F, -2.7F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.005F, -0.3F, -0.005F)),
                PartPose.offset(0F, -2.39F, 0F));
        PartDefinition p_aby_back_1_0 = p_abyssite_chest_body.addOrReplaceChild("aby_back_1_0", CubeListBuilder.create()
                .texOffs(81, 301).addBox(-1F, -1.935F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.065F, -0.35F)),
                PartPose.offsetAndRotation(1.7F, 3.8F, 2.9F, -0.6981F, 0F, 0.384F));
        PartDefinition p_aby_back_1_1 = p_aby_back_1_0.addOrReplaceChild("aby_back_1_1", CubeListBuilder.create()
                .texOffs(114, 338).addBox(-0.5F, -1.85F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.15F, -0.1425F)),
                PartPose.offset(0F, -1.62F, 0F));
        PartDefinition p_aby_back_2_0 = p_abyssite_chest_body.addOrReplaceChild("aby_back_2_0", CubeListBuilder.create()
                .texOffs(90, 301).addBox(-1F, -1.935F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.065F, -0.35F)),
                PartPose.offsetAndRotation(-1.7F, 3.8F, 2.9F, -0.6981F, 0F, -0.384F));
        PartDefinition p_aby_back_2_1 = p_aby_back_2_0.addOrReplaceChild("aby_back_2_1", CubeListBuilder.create()
                .texOffs(119, 338).addBox(-0.5F, -1.85F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.15F, -0.1425F)),
                PartPose.offset(0F, -1.62F, 0F));
        PartDefinition p_abyssite_chest_left_arm = root.addOrReplaceChild("abyssite_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_aby_pauldron_left_arm = p_abyssite_chest_left_arm.addOrReplaceChild("aby_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(148, 35).addBox(-3.8F, -2.3F, -4F, 8F, 3F, 8F, new CubeDeformation(-0.2F, -0.3F, -0.1F))
                .texOffs(0, 71).addBox(-2.5F, 0.3F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, 0F, 0F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.2793F));
        PartDefinition p_aby_shoulder_left_arm_0_0 = p_aby_pauldron_left_arm.addOrReplaceChild("aby_shoulder_left_arm_0_0", CubeListBuilder.create()
                .texOffs(99, 301).addBox(-1F, -1.99F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.25F, -0.01F, -0.25F)),
                PartPose.offsetAndRotation(1.4F, -1.9F, -1.4F, 0.1745F, 0F, 0.4189F));
        PartDefinition p_aby_shoulder_left_arm_0_1 = p_aby_shoulder_left_arm_0_0.addOrReplaceChild("aby_shoulder_left_arm_0_1", CubeListBuilder.create()
                .texOffs(124, 338).addBox(-0.5F, -1.9F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.0875F, -0.1F, -0.0875F)),
                PartPose.offset(0F, -1.73F, 0F));
        PartDefinition p_aby_shoulder_left_arm_1_0 = p_aby_pauldron_left_arm.addOrReplaceChild("aby_shoulder_left_arm_1_0", CubeListBuilder.create()
                .texOffs(108, 301).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.285F, -0.45F)),
                PartPose.offsetAndRotation(2.2F, -1.9F, 1.5F, -0.2094F, 0F, 0.6283F));
        PartDefinition p_aby_shoulder_left_arm_1_1 = p_aby_shoulder_left_arm_1_0.addOrReplaceChild("aby_shoulder_left_arm_1_1", CubeListBuilder.create()
                .texOffs(129, 338).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1975F, -0.35F, -0.1975F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_abyssite_chest_right_arm = root.addOrReplaceChild("abyssite_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_aby_pauldron_right_arm = p_abyssite_chest_right_arm.addOrReplaceChild("aby_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(181, 35).addBox(-4.2F, -2.3F, -4F, 8F, 3F, 8F, new CubeDeformation(-0.2F, -0.3F, -0.1F))
                .texOffs(31, 71).addBox(-4.5F, 0.3F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, 0F, 0F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.2793F));
        PartDefinition p_aby_shoulder_right_arm_0_0 = p_aby_pauldron_right_arm.addOrReplaceChild("aby_shoulder_right_arm_0_0", CubeListBuilder.create()
                .texOffs(117, 301).addBox(-1F, -1.99F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.25F, -0.01F, -0.25F)),
                PartPose.offsetAndRotation(-1.4F, -1.9F, -1.4F, 0.1745F, 0F, -0.4189F));
        PartDefinition p_aby_shoulder_right_arm_0_1 = p_aby_shoulder_right_arm_0_0.addOrReplaceChild("aby_shoulder_right_arm_0_1", CubeListBuilder.create()
                .texOffs(134, 338).addBox(-0.5F, -1.9F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.0875F, -0.1F, -0.0875F)),
                PartPose.offset(0F, -1.73F, 0F));
        PartDefinition p_aby_shoulder_right_arm_1_0 = p_aby_pauldron_right_arm.addOrReplaceChild("aby_shoulder_right_arm_1_0", CubeListBuilder.create()
                .texOffs(126, 301).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.285F, -0.45F)),
                PartPose.offsetAndRotation(-2.2F, -1.9F, 1.5F, -0.2094F, 0F, -0.6283F));
        PartDefinition p_aby_shoulder_right_arm_1_1 = p_aby_shoulder_right_arm_1_0.addOrReplaceChild("aby_shoulder_right_arm_1_1", CubeListBuilder.create()
                .texOffs(139, 338).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1975F, -0.35F, -0.1975F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_abyssite_legs_body = root.addOrReplaceChild("abyssite_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_abyssite_legs_left_leg = root.addOrReplaceChild("abyssite_legs_left_leg", CubeListBuilder.create()
                .texOffs(110, 233).addBox(-3F, -0.9F, -3.3F, 6F, 5F, 1F, new CubeDeformation(-0.3F, -0.4F, 0F))
                .texOffs(104, 316).addBox(-1F, 0.1F, -4F, 2F, 3F, 1F, new CubeDeformation(-0.3F, -0.3F, -0.2F))
                .texOffs(174, 291).addBox(-2F, 4.9F, -3.3F, 4F, 3F, 1F, new CubeDeformation(0F, -0.1F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_abyssite_legs_right_leg = root.addOrReplaceChild("abyssite_legs_right_leg", CubeListBuilder.create()
                .texOffs(125, 233).addBox(-3F, -0.9F, -3.3F, 6F, 5F, 1F, new CubeDeformation(-0.3F, -0.4F, 0F))
                .texOffs(111, 316).addBox(-1F, 0.1F, -4F, 2F, 3F, 1F, new CubeDeformation(-0.3F, -0.3F, -0.2F))
                .texOffs(185, 291).addBox(-2F, 4.9F, -3.3F, 4F, 3F, 1F, new CubeDeformation(0F, -0.1F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_abyssite_feet_left_leg = root.addOrReplaceChild("abyssite_feet_left_leg", CubeListBuilder.create()
                .texOffs(15, 261).addBox(-2.5F, 10.25F, -4.45F, 5F, 3F, 2F, new CubeDeformation(0F, -0.45F, -0.45F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_aby_heel_left_leg_0_0 = p_abyssite_feet_left_leg.addOrReplaceChild("aby_heel_left_leg_0_0", CubeListBuilder.create()
                .texOffs(135, 301).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.285F, -0.45F)),
                PartPose.offsetAndRotation(0.9F, 10.8F, 2.8F, -0.9774F, 0F, 0.2094F));
        PartDefinition p_aby_heel_left_leg_0_1 = p_aby_heel_left_leg_0_0.addOrReplaceChild("aby_heel_left_leg_0_1", CubeListBuilder.create()
                .texOffs(144, 338).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1975F, -0.35F, -0.1975F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_aby_heel_left_leg_1_0 = p_abyssite_feet_left_leg.addOrReplaceChild("aby_heel_left_leg_1_0", CubeListBuilder.create()
                .texOffs(149, 338).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.05F, -0.45F, -0.05F)),
                PartPose.offsetAndRotation(-0.9F, 10.8F, 2.8F, -0.9774F, 0F, -0.2094F));
        PartDefinition p_aby_heel_left_leg_1_1 = p_aby_heel_left_leg_1_0.addOrReplaceChild("aby_heel_left_leg_1_1", CubeListBuilder.create()
                .texOffs(69, 350).addBox(-0.5F, -1F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.2525F, 0F, -0.2525F)),
                PartPose.offset(0F, -0.85F, 0F));
        PartDefinition p_abyssite_feet_right_leg = root.addOrReplaceChild("abyssite_feet_right_leg", CubeListBuilder.create()
                .texOffs(30, 261).addBox(-2.5F, 10.25F, -4.45F, 5F, 3F, 2F, new CubeDeformation(0F, -0.45F, -0.45F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_aby_heel_right_leg_0_0 = p_abyssite_feet_right_leg.addOrReplaceChild("aby_heel_right_leg_0_0", CubeListBuilder.create()
                .texOffs(144, 301).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.285F, -0.45F)),
                PartPose.offsetAndRotation(0.9F, 10.8F, 2.8F, -0.9774F, 0F, 0.2094F));
        PartDefinition p_aby_heel_right_leg_0_1 = p_aby_heel_right_leg_0_0.addOrReplaceChild("aby_heel_right_leg_0_1", CubeListBuilder.create()
                .texOffs(154, 338).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1975F, -0.35F, -0.1975F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_aby_heel_right_leg_1_0 = p_abyssite_feet_right_leg.addOrReplaceChild("aby_heel_right_leg_1_0", CubeListBuilder.create()
                .texOffs(159, 338).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.05F, -0.45F, -0.05F)),
                PartPose.offsetAndRotation(-0.9F, 10.8F, 2.8F, -0.9774F, 0F, -0.2094F));
        PartDefinition p_aby_heel_right_leg_1_1 = p_aby_heel_right_leg_1_0.addOrReplaceChild("aby_heel_right_leg_1_1", CubeListBuilder.create()
                .texOffs(74, 350).addBox(-0.5F, -1F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.2525F, 0F, -0.2525F)),
                PartPose.offset(0F, -0.85F, 0F));
        PartDefinition p_fire_head_head = root.addOrReplaceChild("fire_head_head", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_fire_horn_left_0 = p_fire_head_head.addOrReplaceChild("fire_horn_left_0", CubeListBuilder.create()
                .texOffs(39, 240).addBox(-1.5F, -2.9F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.1F, -0.4F)),
                PartPose.offsetAndRotation(3.2F, -8.7F, -1.6F, -0.4189F, 0F, 0.384F));
        PartDefinition p_fire_horn_left_1 = p_fire_horn_left_0.addOrReplaceChild("fire_horn_left_1", CubeListBuilder.create()
                .texOffs(126, 273).addBox(-1F, -2.9F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.1F, -0.1F, -0.1F)),
                PartPose.offsetAndRotation(0F, -2.55F, 0F, -0.5934F, 0F, -0.1047F));
        PartDefinition p_fire_horn_left_2 = p_fire_horn_left_1.addOrReplaceChild("fire_horn_left_2", CubeListBuilder.create()
                .texOffs(135, 273).addBox(-1F, -2.8F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.35F, -0.2F, -0.35F)),
                PartPose.offsetAndRotation(0F, -2.55F, 0F, -0.6632F, 0F, -0.1047F));
        PartDefinition p_fire_horn_left_3 = p_fire_horn_left_2.addOrReplaceChild("fire_horn_left_3", CubeListBuilder.create()
                .texOffs(184, 316).addBox(-0.5F, -2.6F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.1F)),
                PartPose.offsetAndRotation(0F, -2.35F, 0F, -0.5934F, 0F, 0F));
        PartDefinition p_fire_horn_right_0 = p_fire_head_head.addOrReplaceChild("fire_horn_right_0", CubeListBuilder.create()
                .texOffs(52, 240).addBox(-1.5F, -2.9F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.1F, -0.4F)),
                PartPose.offsetAndRotation(-3.2F, -8.7F, -1.6F, -0.4189F, 0F, -0.384F));
        PartDefinition p_fire_horn_right_1 = p_fire_horn_right_0.addOrReplaceChild("fire_horn_right_1", CubeListBuilder.create()
                .texOffs(144, 273).addBox(-1F, -2.9F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.1F, -0.1F, -0.1F)),
                PartPose.offsetAndRotation(0F, -2.55F, 0F, -0.5934F, 0F, 0.1047F));
        PartDefinition p_fire_horn_right_2 = p_fire_horn_right_1.addOrReplaceChild("fire_horn_right_2", CubeListBuilder.create()
                .texOffs(153, 273).addBox(-1F, -2.8F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.35F, -0.2F, -0.35F)),
                PartPose.offsetAndRotation(0F, -2.55F, 0F, -0.6632F, 0F, 0.1047F));
        PartDefinition p_fire_horn_right_3 = p_fire_horn_right_2.addOrReplaceChild("fire_horn_right_3", CubeListBuilder.create()
                .texOffs(189, 316).addBox(-0.5F, -2.6F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.1F)),
                PartPose.offsetAndRotation(0F, -2.35F, 0F, -0.5934F, 0F, 0F));
        PartDefinition p_fire_crest_0_0 = p_fire_head_head.addOrReplaceChild("fire_crest_0_0", CubeListBuilder.create()
                .texOffs(79, 350).addBox(-0.5F, -0.94F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.06F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, -3.2F, -0.4538F, 0F, 0F));
        PartDefinition p_fire_crest_0_1 = p_fire_crest_0_0.addOrReplaceChild("fire_crest_0_1", CubeListBuilder.create()
                .texOffs(84, 350).addBox(-0.5F, -0.9F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.1F, -0.225F)),
                PartPose.offset(0F, -0.63F, 0F));
        PartDefinition p_fire_crest_1_0 = p_fire_head_head.addOrReplaceChild("fire_crest_1_0", CubeListBuilder.create()
                .texOffs(164, 338).addBox(-0.5F, -1.605F, -0.5F, 1F, 2F, 1F, new CubeDeformation(0F, -0.395F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, -0.6F, -0.4538F, 0F, 0F));
        PartDefinition p_fire_crest_1_1 = p_fire_crest_1_0.addOrReplaceChild("fire_crest_1_1", CubeListBuilder.create()
                .texOffs(169, 338).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.225F, -0.45F, -0.225F)),
                PartPose.offset(0F, -0.96F, 0F));
        PartDefinition p_fire_crest_2_0 = p_fire_head_head.addOrReplaceChild("fire_crest_2_0", CubeListBuilder.create()
                .texOffs(89, 350).addBox(-0.5F, -0.995F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.005F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, 2F, -0.4538F, 0F, 0F));
        PartDefinition p_fire_crest_2_1 = p_fire_crest_2_0.addOrReplaceChild("fire_crest_2_1", CubeListBuilder.create()
                .texOffs(94, 350).addBox(-0.5F, -0.95F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.05F, -0.225F)),
                PartPose.offset(0F, -0.74F, 0F));
        PartDefinition p_fire_chest_body = root.addOrReplaceChild("fire_chest_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_fire_back_0_0 = p_fire_chest_body.addOrReplaceChild("fire_back_0_0", CubeListBuilder.create()
                .texOffs(153, 301).addBox(-1F, -1.935F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.065F, -0.3F)),
                PartPose.offsetAndRotation(0F, 0.8F, 2.9F, -0.8727F, 0F, 0F));
        PartDefinition p_fire_back_0_1 = p_fire_back_0_0.addOrReplaceChild("fire_back_0_1", CubeListBuilder.create()
                .texOffs(174, 338).addBox(-0.5F, -1.85F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.15F, -0.115F)),
                PartPose.offset(0F, -1.62F, 0F));
        PartDefinition p_fire_back_1_0 = p_fire_chest_body.addOrReplaceChild("fire_back_1_0", CubeListBuilder.create()
                .texOffs(162, 273).addBox(-1F, -2.545F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.3F, -0.455F, -0.3F)),
                PartPose.offsetAndRotation(0F, 3.6F, 2.9F, -0.8727F, 0F, 0F));
        PartDefinition p_fire_back_1_1 = p_fire_back_1_0.addOrReplaceChild("fire_back_1_1", CubeListBuilder.create()
                .texOffs(179, 338).addBox(-0.5F, -1.95F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.05F, -0.115F)),
                PartPose.offset(0F, -1.84F, 0F));
        PartDefinition p_fire_back_2_0 = p_fire_chest_body.addOrReplaceChild("fire_back_2_0", CubeListBuilder.create()
                .texOffs(162, 301).addBox(-1F, -1.88F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.12F, -0.3F)),
                PartPose.offsetAndRotation(0F, 6.6F, 2.9F, -0.8727F, 0F, 0F));
        PartDefinition p_fire_back_2_1 = p_fire_back_2_0.addOrReplaceChild("fire_back_2_1", CubeListBuilder.create()
                .texOffs(184, 338).addBox(-0.5F, -1.8F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.2F, -0.115F)),
                PartPose.offset(0F, -1.51F, 0F));
        PartDefinition p_fire_back_3_0 = p_fire_chest_body.addOrReplaceChild("fire_back_3_0", CubeListBuilder.create()
                .texOffs(171, 301).addBox(-1F, -1.66F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.34F, -0.3F)),
                PartPose.offsetAndRotation(0F, 9.4F, 2.9F, -0.8727F, 0F, 0F));
        PartDefinition p_fire_back_3_1 = p_fire_back_3_0.addOrReplaceChild("fire_back_3_1", CubeListBuilder.create()
                .texOffs(189, 338).addBox(-0.5F, -1.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.4F, -0.115F)),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_fire_chest_left_arm = root.addOrReplaceChild("fire_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_fire_pauldron_left_arm = p_fire_chest_left_arm.addOrReplaceChild("fire_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(62, 71).addBox(-3.3F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F))
                .texOffs(182, 104).addBox(-2F, -0.1F, -4F, 6F, 2F, 8F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.2094F));
        PartDefinition p_fire_claw_left_arm_0 = p_fire_pauldron_left_arm.addOrReplaceChild("fire_claw_left_arm_0", CubeListBuilder.create()
                .texOffs(180, 301).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.285F, -0.35F)),
                PartPose.offsetAndRotation(2.4F, -1.4F, 0F, 0F, 0F, 0.6981F));
        PartDefinition p_fire_claw_left_arm_1 = p_fire_claw_left_arm_0.addOrReplaceChild("fire_claw_left_arm_1", CubeListBuilder.create()
                .texOffs(194, 338).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.35F, -0.1425F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_fire_chest_right_arm = root.addOrReplaceChild("fire_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_fire_pauldron_right_arm = p_fire_chest_right_arm.addOrReplaceChild("fire_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(93, 71).addBox(-3.7F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F))
                .texOffs(211, 104).addBox(-4F, -0.1F, -4F, 6F, 2F, 8F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.2094F));
        PartDefinition p_fire_claw_right_arm_0 = p_fire_pauldron_right_arm.addOrReplaceChild("fire_claw_right_arm_0", CubeListBuilder.create()
                .texOffs(189, 301).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.285F, -0.35F)),
                PartPose.offsetAndRotation(-2.4F, -1.4F, 0F, 0F, 0F, -0.6981F));
        PartDefinition p_fire_claw_right_arm_1 = p_fire_claw_right_arm_0.addOrReplaceChild("fire_claw_right_arm_1", CubeListBuilder.create()
                .texOffs(199, 338).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.35F, -0.1425F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_fire_legs_body = root.addOrReplaceChild("fire_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_fire_tail_0 = p_fire_legs_body.addOrReplaceChild("fire_tail_0", CubeListBuilder.create()
                .texOffs(65, 240).addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F),
                PartPose.offsetAndRotation(0F, 10.2F, 2.2F, 0.5236F, 0F, 0F));
        PartDefinition p_fire_tail_1 = p_fire_tail_0.addOrReplaceChild("fire_tail_1", CubeListBuilder.create()
                .texOffs(78, 240).addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, 2.75F, 0F, 0.1745F, 0F, 0F));
        PartDefinition p_fire_tail_2 = p_fire_tail_1.addOrReplaceChild("fire_tail_2", CubeListBuilder.create()
                .texOffs(91, 240).addBox(-1.5F, -0.1F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.1F, -0.4F)),
                PartPose.offsetAndRotation(0F, 2.75F, 0F, 0.2094F, 0F, 0F));
        PartDefinition p_fire_tail_3 = p_fire_tail_2.addOrReplaceChild("fire_tail_3", CubeListBuilder.create()
                .texOffs(171, 273).addBox(-1F, -0.2F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.15F, -0.2F, -0.15F)),
                PartPose.offsetAndRotation(0F, 2.55F, 0F, 0.2094F, 0F, 0F));
        PartDefinition p_fire_tail_4 = p_fire_tail_3.addOrReplaceChild("fire_tail_4", CubeListBuilder.create()
                .texOffs(180, 273).addBox(-1F, -0.4F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.4F, -0.4F, -0.4F)),
                PartPose.offsetAndRotation(0F, 2.35F, 0F, 0.1745F, 0F, 0F));
        PartDefinition p_fire_tail_tip = p_fire_tail_4.addOrReplaceChild("fire_tail_tip", CubeListBuilder.create()
                .texOffs(85, 267).addBox(-1.5F, -0.3F, -1F, 3F, 3F, 2F, new CubeDeformation(-0.3F, -0.3F, -0.3F))
                .texOffs(108, 326).addBox(-1F, 2.2F, -0.5F, 2F, 2F, 1F, new CubeDeformation(-0.3F, 0F, -0.1F))
                .texOffs(204, 338).addBox(-0.5F, 3.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.2F, -0.4F, -0.3F)),
                PartPose.offsetAndRotation(0F, 2.4F, 0F, 0.1396F, 0F, 0F));
        PartDefinition p_fire_legs_left_leg = root.addOrReplaceChild("fire_legs_left_leg", CubeListBuilder.create()
                .texOffs(196, 291).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_fire_legs_right_leg = root.addOrReplaceChild("fire_legs_right_leg", CubeListBuilder.create()
                .texOffs(207, 291).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_fire_feet_left_leg = root.addOrReplaceChild("fire_feet_left_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_fire_talon_left_leg_0 = p_fire_feet_left_leg.addOrReplaceChild("fire_talon_left_leg_0", CubeListBuilder.create()
                .texOffs(115, 326).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(-1.6F, 11.6F, -2.8F, 0.4887F, -0.2234F, 0F));
        PartDefinition p_fire_talon_left_leg_1 = p_fire_feet_left_leg.addOrReplaceChild("fire_talon_left_leg_1", CubeListBuilder.create()
                .texOffs(122, 326).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(0F, 11.6F, -2.8F, 0.4887F, 0F, 0F));
        PartDefinition p_fire_talon_left_leg_2 = p_fire_feet_left_leg.addOrReplaceChild("fire_talon_left_leg_2", CubeListBuilder.create()
                .texOffs(129, 326).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(1.6F, 11.6F, -2.8F, 0.4887F, 0.2234F, 0F));
        PartDefinition p_fire_feet_right_leg = root.addOrReplaceChild("fire_feet_right_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_fire_talon_right_leg_0 = p_fire_feet_right_leg.addOrReplaceChild("fire_talon_right_leg_0", CubeListBuilder.create()
                .texOffs(136, 326).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(-1.6F, 11.6F, -2.8F, 0.4887F, -0.2234F, 0F));
        PartDefinition p_fire_talon_right_leg_1 = p_fire_feet_right_leg.addOrReplaceChild("fire_talon_right_leg_1", CubeListBuilder.create()
                .texOffs(143, 326).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(0F, 11.6F, -2.8F, 0.4887F, 0F, 0F));
        PartDefinition p_fire_talon_right_leg_2 = p_fire_feet_right_leg.addOrReplaceChild("fire_talon_right_leg_2", CubeListBuilder.create()
                .texOffs(150, 326).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(1.6F, 11.6F, -2.8F, 0.4887F, 0.2234F, 0F));
        PartDefinition p_ice_head_head = root.addOrReplaceChild("ice_head_head", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_ice_horn_left_0 = p_ice_head_head.addOrReplaceChild("ice_horn_left_0", CubeListBuilder.create()
                .texOffs(64, 254).addBox(-1F, -3.8F, -1F, 2F, 4F, 2F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offsetAndRotation(3F, -8.7F, -1F, -0.4887F, 0F, 0.2793F));
        PartDefinition p_ice_horn_left_1 = p_ice_horn_left_0.addOrReplaceChild("ice_horn_left_1", CubeListBuilder.create()
                .texOffs(73, 254).addBox(-1F, -3.7F, -1F, 2F, 4F, 2F, new CubeDeformation(-0.4F, -0.3F, -0.4F)),
                PartPose.offsetAndRotation(0F, -3.35F, 0F, -0.1047F, 0F, 0F));
        PartDefinition p_ice_side_left_0 = p_ice_head_head.addOrReplaceChild("ice_side_left_0", CubeListBuilder.create()
                .texOffs(198, 301).addBox(-1F, -1.66F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.34F, -0.45F)),
                PartPose.offsetAndRotation(4.4F, -7.4F, 0.6F, -0.6283F, 0F, 0.6981F));
        PartDefinition p_ice_side_left_1 = p_ice_side_left_0.addOrReplaceChild("ice_side_left_1", CubeListBuilder.create()
                .texOffs(209, 338).addBox(-0.5F, -1.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1975F, -0.4F, -0.1975F)),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_ice_horn_right_0 = p_ice_head_head.addOrReplaceChild("ice_horn_right_0", CubeListBuilder.create()
                .texOffs(82, 254).addBox(-1F, -3.8F, -1F, 2F, 4F, 2F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offsetAndRotation(-3F, -8.7F, -1F, -0.4887F, 0F, -0.2793F));
        PartDefinition p_ice_horn_right_1 = p_ice_horn_right_0.addOrReplaceChild("ice_horn_right_1", CubeListBuilder.create()
                .texOffs(91, 254).addBox(-1F, -3.7F, -1F, 2F, 4F, 2F, new CubeDeformation(-0.4F, -0.3F, -0.4F)),
                PartPose.offsetAndRotation(0F, -3.35F, 0F, -0.1047F, 0F, 0F));
        PartDefinition p_ice_side_right_0 = p_ice_head_head.addOrReplaceChild("ice_side_right_0", CubeListBuilder.create()
                .texOffs(207, 301).addBox(-1F, -1.66F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.34F, -0.45F)),
                PartPose.offsetAndRotation(-4.4F, -7.4F, 0.6F, -0.6283F, 0F, -0.6981F));
        PartDefinition p_ice_side_right_1 = p_ice_side_right_0.addOrReplaceChild("ice_side_right_1", CubeListBuilder.create()
                .texOffs(214, 338).addBox(-0.5F, -1.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1975F, -0.4F, -0.1975F)),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_ice_crest_0_0 = p_ice_head_head.addOrReplaceChild("ice_crest_0_0", CubeListBuilder.create()
                .texOffs(99, 350).addBox(-0.5F, -0.94F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.06F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, -3.2F, -0.4538F, 0F, 0F));
        PartDefinition p_ice_crest_0_1 = p_ice_crest_0_0.addOrReplaceChild("ice_crest_0_1", CubeListBuilder.create()
                .texOffs(104, 350).addBox(-0.5F, -0.9F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.1F, -0.225F)),
                PartPose.offset(0F, -0.63F, 0F));
        PartDefinition p_ice_crest_1_0 = p_ice_head_head.addOrReplaceChild("ice_crest_1_0", CubeListBuilder.create()
                .texOffs(219, 338).addBox(-0.5F, -1.605F, -0.5F, 1F, 2F, 1F, new CubeDeformation(0F, -0.395F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, -0.6F, -0.4538F, 0F, 0F));
        PartDefinition p_ice_crest_1_1 = p_ice_crest_1_0.addOrReplaceChild("ice_crest_1_1", CubeListBuilder.create()
                .texOffs(224, 338).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.225F, -0.45F, -0.225F)),
                PartPose.offset(0F, -0.96F, 0F));
        PartDefinition p_ice_crest_2_0 = p_ice_head_head.addOrReplaceChild("ice_crest_2_0", CubeListBuilder.create()
                .texOffs(109, 350).addBox(-0.5F, -0.995F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.005F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, 2F, -0.4538F, 0F, 0F));
        PartDefinition p_ice_crest_2_1 = p_ice_crest_2_0.addOrReplaceChild("ice_crest_2_1", CubeListBuilder.create()
                .texOffs(114, 350).addBox(-0.5F, -0.95F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.05F, -0.225F)),
                PartPose.offset(0F, -0.74F, 0F));
        PartDefinition p_ice_chest_body = root.addOrReplaceChild("ice_chest_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_ice_back_0_0 = p_ice_chest_body.addOrReplaceChild("ice_back_0_0", CubeListBuilder.create()
                .texOffs(216, 301).addBox(-1F, -1.935F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.065F, -0.3F)),
                PartPose.offsetAndRotation(0F, 0.8F, 2.9F, -0.8727F, 0F, 0F));
        PartDefinition p_ice_back_0_1 = p_ice_back_0_0.addOrReplaceChild("ice_back_0_1", CubeListBuilder.create()
                .texOffs(229, 338).addBox(-0.5F, -1.85F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.15F, -0.115F)),
                PartPose.offset(0F, -1.62F, 0F));
        PartDefinition p_ice_back_1_0 = p_ice_chest_body.addOrReplaceChild("ice_back_1_0", CubeListBuilder.create()
                .texOffs(189, 273).addBox(-1F, -2.545F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.3F, -0.455F, -0.3F)),
                PartPose.offsetAndRotation(0F, 3.6F, 2.9F, -0.8727F, 0F, 0F));
        PartDefinition p_ice_back_1_1 = p_ice_back_1_0.addOrReplaceChild("ice_back_1_1", CubeListBuilder.create()
                .texOffs(234, 338).addBox(-0.5F, -1.95F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.05F, -0.115F)),
                PartPose.offset(0F, -1.84F, 0F));
        PartDefinition p_ice_back_2_0 = p_ice_chest_body.addOrReplaceChild("ice_back_2_0", CubeListBuilder.create()
                .texOffs(225, 301).addBox(-1F, -1.88F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.12F, -0.3F)),
                PartPose.offsetAndRotation(0F, 6.6F, 2.9F, -0.8727F, 0F, 0F));
        PartDefinition p_ice_back_2_1 = p_ice_back_2_0.addOrReplaceChild("ice_back_2_1", CubeListBuilder.create()
                .texOffs(239, 338).addBox(-0.5F, -1.8F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.2F, -0.115F)),
                PartPose.offset(0F, -1.51F, 0F));
        PartDefinition p_ice_back_3_0 = p_ice_chest_body.addOrReplaceChild("ice_back_3_0", CubeListBuilder.create()
                .texOffs(234, 301).addBox(-1F, -1.66F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.34F, -0.3F)),
                PartPose.offsetAndRotation(0F, 9.4F, 2.9F, -0.8727F, 0F, 0F));
        PartDefinition p_ice_back_3_1 = p_ice_back_3_0.addOrReplaceChild("ice_back_3_1", CubeListBuilder.create()
                .texOffs(244, 338).addBox(-0.5F, -1.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.4F, -0.115F)),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_ice_chest_left_arm = root.addOrReplaceChild("ice_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_ice_pauldron_left_arm = p_ice_chest_left_arm.addOrReplaceChild("ice_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(124, 71).addBox(-3.3F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F))
                .texOffs(0, 115).addBox(-2F, -0.1F, -4F, 6F, 2F, 8F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.2094F));
        PartDefinition p_ice_claw_left_arm_0 = p_ice_pauldron_left_arm.addOrReplaceChild("ice_claw_left_arm_0", CubeListBuilder.create()
                .texOffs(243, 301).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.285F, -0.35F)),
                PartPose.offsetAndRotation(2.4F, -1.4F, 0F, 0F, 0F, 0.6981F));
        PartDefinition p_ice_claw_left_arm_1 = p_ice_claw_left_arm_0.addOrReplaceChild("ice_claw_left_arm_1", CubeListBuilder.create()
                .texOffs(249, 338).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.35F, -0.1425F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_ice_chest_right_arm = root.addOrReplaceChild("ice_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_ice_pauldron_right_arm = p_ice_chest_right_arm.addOrReplaceChild("ice_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(155, 71).addBox(-3.7F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F))
                .texOffs(29, 115).addBox(-4F, -0.1F, -4F, 6F, 2F, 8F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.2094F));
        PartDefinition p_ice_claw_right_arm_0 = p_ice_pauldron_right_arm.addOrReplaceChild("ice_claw_right_arm_0", CubeListBuilder.create()
                .texOffs(0, 306).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.285F, -0.35F)),
                PartPose.offsetAndRotation(-2.4F, -1.4F, 0F, 0F, 0F, -0.6981F));
        PartDefinition p_ice_claw_right_arm_1 = p_ice_claw_right_arm_0.addOrReplaceChild("ice_claw_right_arm_1", CubeListBuilder.create()
                .texOffs(0, 342).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.35F, -0.1425F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_ice_legs_body = root.addOrReplaceChild("ice_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_ice_tail_0 = p_ice_legs_body.addOrReplaceChild("ice_tail_0", CubeListBuilder.create()
                .texOffs(104, 240).addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F),
                PartPose.offsetAndRotation(0F, 10.2F, 2.2F, 0.5236F, 0F, 0F));
        PartDefinition p_ice_tail_1 = p_ice_tail_0.addOrReplaceChild("ice_tail_1", CubeListBuilder.create()
                .texOffs(117, 240).addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, 2.75F, 0F, 0.1745F, 0F, 0F));
        PartDefinition p_ice_tail_2 = p_ice_tail_1.addOrReplaceChild("ice_tail_2", CubeListBuilder.create()
                .texOffs(130, 240).addBox(-1.5F, -0.1F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.1F, -0.4F)),
                PartPose.offsetAndRotation(0F, 2.75F, 0F, 0.2094F, 0F, 0F));
        PartDefinition p_ice_tail_3 = p_ice_tail_2.addOrReplaceChild("ice_tail_3", CubeListBuilder.create()
                .texOffs(198, 273).addBox(-1F, -0.2F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.15F, -0.2F, -0.15F)),
                PartPose.offsetAndRotation(0F, 2.55F, 0F, 0.2094F, 0F, 0F));
        PartDefinition p_ice_tail_4 = p_ice_tail_3.addOrReplaceChild("ice_tail_4", CubeListBuilder.create()
                .texOffs(207, 273).addBox(-1F, -0.4F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.4F, -0.4F, -0.4F)),
                PartPose.offsetAndRotation(0F, 2.35F, 0F, 0.1745F, 0F, 0F));
        PartDefinition p_ice_tail_tip = p_ice_tail_4.addOrReplaceChild("ice_tail_tip", CubeListBuilder.create()
                .texOffs(216, 273).addBox(-1F, -0.3F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.1F, -0.3F, -0.1F))
                .texOffs(194, 316).addBox(-0.5F, 1.8F, -0.5F, 1F, 3F, 1F, new CubeDeformation(0F, -0.4F, 0F)),
                PartPose.offsetAndRotation(0F, 2.4F, 0F, 0.1396F, 0F, 0F));
        PartDefinition p_ice_legs_left_leg = root.addOrReplaceChild("ice_legs_left_leg", CubeListBuilder.create()
                .texOffs(218, 291).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_ice_legs_right_leg = root.addOrReplaceChild("ice_legs_right_leg", CubeListBuilder.create()
                .texOffs(229, 291).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_ice_feet_left_leg = root.addOrReplaceChild("ice_feet_left_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_ice_talon_left_leg_0 = p_ice_feet_left_leg.addOrReplaceChild("ice_talon_left_leg_0", CubeListBuilder.create()
                .texOffs(157, 326).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(-1.6F, 11.6F, -2.8F, 0.4887F, -0.2234F, 0F));
        PartDefinition p_ice_talon_left_leg_1 = p_ice_feet_left_leg.addOrReplaceChild("ice_talon_left_leg_1", CubeListBuilder.create()
                .texOffs(164, 326).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(0F, 11.6F, -2.8F, 0.4887F, 0F, 0F));
        PartDefinition p_ice_talon_left_leg_2 = p_ice_feet_left_leg.addOrReplaceChild("ice_talon_left_leg_2", CubeListBuilder.create()
                .texOffs(171, 326).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(1.6F, 11.6F, -2.8F, 0.4887F, 0.2234F, 0F));
        PartDefinition p_ice_feet_right_leg = root.addOrReplaceChild("ice_feet_right_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_ice_talon_right_leg_0 = p_ice_feet_right_leg.addOrReplaceChild("ice_talon_right_leg_0", CubeListBuilder.create()
                .texOffs(178, 326).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(-1.6F, 11.6F, -2.8F, 0.4887F, -0.2234F, 0F));
        PartDefinition p_ice_talon_right_leg_1 = p_ice_feet_right_leg.addOrReplaceChild("ice_talon_right_leg_1", CubeListBuilder.create()
                .texOffs(185, 326).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(0F, 11.6F, -2.8F, 0.4887F, 0F, 0F));
        PartDefinition p_ice_talon_right_leg_2 = p_ice_feet_right_leg.addOrReplaceChild("ice_talon_right_leg_2", CubeListBuilder.create()
                .texOffs(192, 326).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(1.6F, 11.6F, -2.8F, 0.4887F, 0.2234F, 0F));
        PartDefinition p_lightning_head_head = root.addOrReplaceChild("lightning_head_head", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_light_horn_left_0 = p_lightning_head_head.addOrReplaceChild("light_horn_left_0", CubeListBuilder.create()
                .texOffs(225, 273).addBox(-1F, -2.7F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.05F, -0.3F, -0.05F)),
                PartPose.offsetAndRotation(3.1F, -8.7F, -1.2F, -0.3491F, 0F, 0.4538F));
        PartDefinition p_light_horn_left_1 = p_light_horn_left_0.addOrReplaceChild("light_horn_left_1", CubeListBuilder.create()
                .texOffs(234, 273).addBox(-1F, -2.6F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.2F, -0.4F, -0.2F)),
                PartPose.offsetAndRotation(0F, -2.15F, 0F, -0.8727F, 0F, -0.3142F));
        PartDefinition p_light_horn_left_2 = p_light_horn_left_1.addOrReplaceChild("light_horn_left_2", CubeListBuilder.create()
                .texOffs(243, 273).addBox(-1F, -2.6F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.35F, -0.4F, -0.35F)),
                PartPose.offsetAndRotation(0F, -1.95F, 0F, 0.6981F, 0F, 0.1745F));
        PartDefinition p_light_horn_left_3 = p_light_horn_left_2.addOrReplaceChild("light_horn_left_3", CubeListBuilder.create()
                .texOffs(5, 342).addBox(-0.5F, -2F, -0.5F, 1F, 2F, 1F),
                PartPose.offsetAndRotation(0F, -1.95F, 0F, -0.9774F, 0F, -0.1047F));
        PartDefinition p_light_horn_left_4 = p_light_horn_left_3.addOrReplaceChild("light_horn_left_4", CubeListBuilder.create()
                .texOffs(10, 342).addBox(-0.5F, -1.9F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.15F, -0.1F, -0.15F)),
                PartPose.offsetAndRotation(0F, -1.75F, 0F, 0.5934F, 0F, 0F));
        PartDefinition p_light_horn_right_0 = p_lightning_head_head.addOrReplaceChild("light_horn_right_0", CubeListBuilder.create()
                .texOffs(0, 279).addBox(-1F, -2.7F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.05F, -0.3F, -0.05F)),
                PartPose.offsetAndRotation(-3.1F, -8.7F, -1.2F, -0.3491F, 0F, -0.4538F));
        PartDefinition p_light_horn_right_1 = p_light_horn_right_0.addOrReplaceChild("light_horn_right_1", CubeListBuilder.create()
                .texOffs(9, 279).addBox(-1F, -2.6F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.2F, -0.4F, -0.2F)),
                PartPose.offsetAndRotation(0F, -2.15F, 0F, -0.8727F, 0F, 0.3142F));
        PartDefinition p_light_horn_right_2 = p_light_horn_right_1.addOrReplaceChild("light_horn_right_2", CubeListBuilder.create()
                .texOffs(18, 279).addBox(-1F, -2.6F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.35F, -0.4F, -0.35F)),
                PartPose.offsetAndRotation(0F, -1.95F, 0F, 0.6981F, 0F, -0.1745F));
        PartDefinition p_light_horn_right_3 = p_light_horn_right_2.addOrReplaceChild("light_horn_right_3", CubeListBuilder.create()
                .texOffs(15, 342).addBox(-0.5F, -2F, -0.5F, 1F, 2F, 1F),
                PartPose.offsetAndRotation(0F, -1.95F, 0F, -0.9774F, 0F, 0.1047F));
        PartDefinition p_light_horn_right_4 = p_light_horn_right_3.addOrReplaceChild("light_horn_right_4", CubeListBuilder.create()
                .texOffs(20, 342).addBox(-0.5F, -1.9F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.15F, -0.1F, -0.15F)),
                PartPose.offsetAndRotation(0F, -1.75F, 0F, 0.5934F, 0F, 0F));
        PartDefinition p_lightning_crest_0_0 = p_lightning_head_head.addOrReplaceChild("lightning_crest_0_0", CubeListBuilder.create()
                .texOffs(119, 350).addBox(-0.5F, -0.94F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.06F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, -3.2F, -0.4538F, 0F, 0F));
        PartDefinition p_lightning_crest_0_1 = p_lightning_crest_0_0.addOrReplaceChild("lightning_crest_0_1", CubeListBuilder.create()
                .texOffs(124, 350).addBox(-0.5F, -0.9F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.1F, -0.225F)),
                PartPose.offset(0F, -0.63F, 0F));
        PartDefinition p_lightning_crest_1_0 = p_lightning_head_head.addOrReplaceChild("lightning_crest_1_0", CubeListBuilder.create()
                .texOffs(25, 342).addBox(-0.5F, -1.605F, -0.5F, 1F, 2F, 1F, new CubeDeformation(0F, -0.395F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, -0.6F, -0.4538F, 0F, 0F));
        PartDefinition p_lightning_crest_1_1 = p_lightning_crest_1_0.addOrReplaceChild("lightning_crest_1_1", CubeListBuilder.create()
                .texOffs(30, 342).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.225F, -0.45F, -0.225F)),
                PartPose.offset(0F, -0.96F, 0F));
        PartDefinition p_lightning_crest_2_0 = p_lightning_head_head.addOrReplaceChild("lightning_crest_2_0", CubeListBuilder.create()
                .texOffs(129, 350).addBox(-0.5F, -0.995F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.005F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, 2F, -0.4538F, 0F, 0F));
        PartDefinition p_lightning_crest_2_1 = p_lightning_crest_2_0.addOrReplaceChild("lightning_crest_2_1", CubeListBuilder.create()
                .texOffs(134, 350).addBox(-0.5F, -0.95F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.05F, -0.225F)),
                PartPose.offset(0F, -0.74F, 0F));
        PartDefinition p_lightning_chest_body = root.addOrReplaceChild("lightning_chest_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_lightning_back_0_0 = p_lightning_chest_body.addOrReplaceChild("lightning_back_0_0", CubeListBuilder.create()
                .texOffs(9, 306).addBox(-1F, -1.935F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.065F, -0.3F)),
                PartPose.offsetAndRotation(0F, 0.8F, 2.9F, -0.8727F, 0F, 0F));
        PartDefinition p_lightning_back_0_1 = p_lightning_back_0_0.addOrReplaceChild("lightning_back_0_1", CubeListBuilder.create()
                .texOffs(35, 342).addBox(-0.5F, -1.85F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.15F, -0.115F)),
                PartPose.offset(0F, -1.62F, 0F));
        PartDefinition p_lightning_back_1_0 = p_lightning_chest_body.addOrReplaceChild("lightning_back_1_0", CubeListBuilder.create()
                .texOffs(27, 279).addBox(-1F, -2.545F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.3F, -0.455F, -0.3F)),
                PartPose.offsetAndRotation(0F, 3.6F, 2.9F, -0.8727F, 0F, 0F));
        PartDefinition p_lightning_back_1_1 = p_lightning_back_1_0.addOrReplaceChild("lightning_back_1_1", CubeListBuilder.create()
                .texOffs(40, 342).addBox(-0.5F, -1.95F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.05F, -0.115F)),
                PartPose.offset(0F, -1.84F, 0F));
        PartDefinition p_lightning_back_2_0 = p_lightning_chest_body.addOrReplaceChild("lightning_back_2_0", CubeListBuilder.create()
                .texOffs(18, 306).addBox(-1F, -1.88F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.12F, -0.3F)),
                PartPose.offsetAndRotation(0F, 6.6F, 2.9F, -0.8727F, 0F, 0F));
        PartDefinition p_lightning_back_2_1 = p_lightning_back_2_0.addOrReplaceChild("lightning_back_2_1", CubeListBuilder.create()
                .texOffs(45, 342).addBox(-0.5F, -1.8F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.2F, -0.115F)),
                PartPose.offset(0F, -1.51F, 0F));
        PartDefinition p_lightning_back_3_0 = p_lightning_chest_body.addOrReplaceChild("lightning_back_3_0", CubeListBuilder.create()
                .texOffs(27, 306).addBox(-1F, -1.66F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.34F, -0.3F)),
                PartPose.offsetAndRotation(0F, 9.4F, 2.9F, -0.8727F, 0F, 0F));
        PartDefinition p_lightning_back_3_1 = p_lightning_back_3_0.addOrReplaceChild("lightning_back_3_1", CubeListBuilder.create()
                .texOffs(50, 342).addBox(-0.5F, -1.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.4F, -0.115F)),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_lightning_chest_left_arm = root.addOrReplaceChild("lightning_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_lightning_pauldron_left_arm = p_lightning_chest_left_arm.addOrReplaceChild("lightning_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(186, 71).addBox(-3.3F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F))
                .texOffs(58, 115).addBox(-2F, -0.1F, -4F, 6F, 2F, 8F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.2094F));
        PartDefinition p_lightning_claw_left_arm_0 = p_lightning_pauldron_left_arm.addOrReplaceChild("lightning_claw_left_arm_0", CubeListBuilder.create()
                .texOffs(36, 306).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.285F, -0.35F)),
                PartPose.offsetAndRotation(2.4F, -1.4F, 0F, 0F, 0F, 0.6981F));
        PartDefinition p_lightning_claw_left_arm_1 = p_lightning_claw_left_arm_0.addOrReplaceChild("lightning_claw_left_arm_1", CubeListBuilder.create()
                .texOffs(55, 342).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.35F, -0.1425F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_lightning_chest_right_arm = root.addOrReplaceChild("lightning_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_lightning_pauldron_right_arm = p_lightning_chest_right_arm.addOrReplaceChild("lightning_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(217, 71).addBox(-3.7F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F))
                .texOffs(87, 115).addBox(-4F, -0.1F, -4F, 6F, 2F, 8F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.2094F));
        PartDefinition p_lightning_claw_right_arm_0 = p_lightning_pauldron_right_arm.addOrReplaceChild("lightning_claw_right_arm_0", CubeListBuilder.create()
                .texOffs(45, 306).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.285F, -0.35F)),
                PartPose.offsetAndRotation(-2.4F, -1.4F, 0F, 0F, 0F, -0.6981F));
        PartDefinition p_lightning_claw_right_arm_1 = p_lightning_claw_right_arm_0.addOrReplaceChild("lightning_claw_right_arm_1", CubeListBuilder.create()
                .texOffs(60, 342).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.35F, -0.1425F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_lightning_legs_body = root.addOrReplaceChild("lightning_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_lightning_tail_0 = p_lightning_legs_body.addOrReplaceChild("lightning_tail_0", CubeListBuilder.create()
                .texOffs(143, 240).addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F),
                PartPose.offsetAndRotation(0F, 10.2F, 2.2F, 0.5236F, 0F, 0F));
        PartDefinition p_lightning_tail_1 = p_lightning_tail_0.addOrReplaceChild("lightning_tail_1", CubeListBuilder.create()
                .texOffs(156, 240).addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, 2.75F, 0F, 0.1745F, 0F, 0F));
        PartDefinition p_lightning_tail_2 = p_lightning_tail_1.addOrReplaceChild("lightning_tail_2", CubeListBuilder.create()
                .texOffs(169, 240).addBox(-1.5F, -0.1F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.1F, -0.4F)),
                PartPose.offsetAndRotation(0F, 2.75F, 0F, 0.2094F, 0F, 0F));
        PartDefinition p_lightning_tail_3 = p_lightning_tail_2.addOrReplaceChild("lightning_tail_3", CubeListBuilder.create()
                .texOffs(36, 279).addBox(-1F, -0.2F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.15F, -0.2F, -0.15F)),
                PartPose.offsetAndRotation(0F, 2.55F, 0F, 0.2094F, 0F, 0F));
        PartDefinition p_lightning_tail_4 = p_lightning_tail_3.addOrReplaceChild("lightning_tail_4", CubeListBuilder.create()
                .texOffs(45, 279).addBox(-1F, -0.4F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.4F, -0.4F, -0.4F)),
                PartPose.offsetAndRotation(0F, 2.35F, 0F, 0.1745F, 0F, 0F));
        PartDefinition p_lightning_tail_tip = p_lightning_tail_4.addOrReplaceChild("lightning_tail_tip", CubeListBuilder.create()
                .texOffs(74, 225).addBox(-0.5F, -0.4F, -2F, 1F, 3F, 4F, new CubeDeformation(-0.2F, -0.4F, -0.4F))
                .texOffs(54, 279).addBox(-0.5F, 2F, -0.8F, 1F, 2F, 3F, new CubeDeformation(-0.25F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, 2.4F, 0F, 0.1396F, 0F, 0F));
        PartDefinition p_lightning_legs_left_leg = root.addOrReplaceChild("lightning_legs_left_leg", CubeListBuilder.create()
                .texOffs(240, 291).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_lightning_legs_right_leg = root.addOrReplaceChild("lightning_legs_right_leg", CubeListBuilder.create()
                .texOffs(0, 296).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_lightning_feet_left_leg = root.addOrReplaceChild("lightning_feet_left_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_lightning_talon_left_leg_0 = p_lightning_feet_left_leg.addOrReplaceChild("lightning_talon_left_leg_0", CubeListBuilder.create()
                .texOffs(199, 326).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(-1.6F, 11.6F, -2.8F, 0.4887F, -0.2234F, 0F));
        PartDefinition p_lightning_talon_left_leg_1 = p_lightning_feet_left_leg.addOrReplaceChild("lightning_talon_left_leg_1", CubeListBuilder.create()
                .texOffs(206, 326).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(0F, 11.6F, -2.8F, 0.4887F, 0F, 0F));
        PartDefinition p_lightning_talon_left_leg_2 = p_lightning_feet_left_leg.addOrReplaceChild("lightning_talon_left_leg_2", CubeListBuilder.create()
                .texOffs(213, 326).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(1.6F, 11.6F, -2.8F, 0.4887F, 0.2234F, 0F));
        PartDefinition p_lightning_feet_right_leg = root.addOrReplaceChild("lightning_feet_right_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_lightning_talon_right_leg_0 = p_lightning_feet_right_leg.addOrReplaceChild("lightning_talon_right_leg_0", CubeListBuilder.create()
                .texOffs(220, 326).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(-1.6F, 11.6F, -2.8F, 0.4887F, -0.2234F, 0F));
        PartDefinition p_lightning_talon_right_leg_1 = p_lightning_feet_right_leg.addOrReplaceChild("lightning_talon_right_leg_1", CubeListBuilder.create()
                .texOffs(227, 326).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(0F, 11.6F, -2.8F, 0.4887F, 0F, 0F));
        PartDefinition p_lightning_talon_right_leg_2 = p_lightning_feet_right_leg.addOrReplaceChild("lightning_talon_right_leg_2", CubeListBuilder.create()
                .texOffs(234, 326).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(1.6F, 11.6F, -2.8F, 0.4887F, 0.2234F, 0F));
        PartDefinition p_nature_head_head = root.addOrReplaceChild("nature_head_head", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_antler_left_0 = p_nature_head_head.addOrReplaceChild("antler_left_0", CubeListBuilder.create()
                .texOffs(63, 279).addBox(-1F, -3F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.3F, 0F, -0.3F)),
                PartPose.offsetAndRotation(3F, -8.7F, -0.8F, -0.2443F, 0F, 0.5236F));
        PartDefinition p_antler_left_1 = p_antler_left_0.addOrReplaceChild("antler_left_1", CubeListBuilder.create()
                .texOffs(72, 279).addBox(-1F, -3F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.4F, 0F, -0.4F)),
                PartPose.offsetAndRotation(0F, -2.75F, 0F, -0.384F, 0F, -0.1745F));
        PartDefinition p_antler_left_2 = p_antler_left_1.addOrReplaceChild("antler_left_2", CubeListBuilder.create()
                .texOffs(199, 316).addBox(-0.5F, -2.8F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.05F, -0.2F, -0.05F)),
                PartPose.offsetAndRotation(0F, -2.75F, 0F, -0.3491F, 0F, -0.1396F));
        PartDefinition p_antler_tine_left_1 = p_antler_left_1.addOrReplaceChild("antler_tine_left_1", CubeListBuilder.create()
                .texOffs(65, 342).addBox(-0.5F, -1.9F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.15F, -0.1F, -0.15F)),
                PartPose.offsetAndRotation(0F, -2.2F, 0F, 0.8378F, 0F, -0.2443F));
        PartDefinition p_antler_tine_left_0 = p_antler_left_0.addOrReplaceChild("antler_tine_left_0", CubeListBuilder.create()
                .texOffs(70, 342).addBox(-0.5F, -2F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1F, 0F, -0.1F)),
                PartPose.offsetAndRotation(0F, -2.4F, 0F, 0.8727F, 0F, 0.1745F));
        PartDefinition p_antler_leaf_left = p_antler_left_0.addOrReplaceChild("antler_leaf_left", CubeListBuilder.create()
                .texOffs(0, 285).addBox(-0.5F, -2.8F, -1F, 1F, 3F, 2F, new CubeDeformation(-0.35F, -0.2F, -0.1F)),
                PartPose.offsetAndRotation(0.6F, -1.4F, 0F, 0F, 0F, 0.8727F));
        PartDefinition p_antler_right_0 = p_nature_head_head.addOrReplaceChild("antler_right_0", CubeListBuilder.create()
                .texOffs(81, 279).addBox(-1F, -3F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.3F, 0F, -0.3F)),
                PartPose.offsetAndRotation(-3F, -8.7F, -0.8F, -0.2443F, 0F, -0.5236F));
        PartDefinition p_antler_right_1 = p_antler_right_0.addOrReplaceChild("antler_right_1", CubeListBuilder.create()
                .texOffs(90, 279).addBox(-1F, -3F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.4F, 0F, -0.4F)),
                PartPose.offsetAndRotation(0F, -2.75F, 0F, -0.384F, 0F, 0.1745F));
        PartDefinition p_antler_right_2 = p_antler_right_1.addOrReplaceChild("antler_right_2", CubeListBuilder.create()
                .texOffs(204, 316).addBox(-0.5F, -2.8F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.05F, -0.2F, -0.05F)),
                PartPose.offsetAndRotation(0F, -2.75F, 0F, -0.3491F, 0F, 0.1396F));
        PartDefinition p_antler_tine_right_1 = p_antler_right_1.addOrReplaceChild("antler_tine_right_1", CubeListBuilder.create()
                .texOffs(75, 342).addBox(-0.5F, -1.9F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.15F, -0.1F, -0.15F)),
                PartPose.offsetAndRotation(0F, -2.2F, 0F, 0.8378F, 0F, 0.2443F));
        PartDefinition p_antler_tine_right_0 = p_antler_right_0.addOrReplaceChild("antler_tine_right_0", CubeListBuilder.create()
                .texOffs(80, 342).addBox(-0.5F, -2F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1F, 0F, -0.1F)),
                PartPose.offsetAndRotation(0F, -2.4F, 0F, 0.8727F, 0F, -0.1745F));
        PartDefinition p_antler_leaf_right = p_antler_right_0.addOrReplaceChild("antler_leaf_right", CubeListBuilder.create()
                .texOffs(7, 285).addBox(-0.5F, -2.8F, -1F, 1F, 3F, 2F, new CubeDeformation(-0.35F, -0.2F, -0.1F)),
                PartPose.offsetAndRotation(-0.6F, -1.4F, 0F, 0F, 0F, -0.8727F));
        PartDefinition p_nature_crest_0_0 = p_nature_head_head.addOrReplaceChild("nature_crest_0_0", CubeListBuilder.create()
                .texOffs(139, 350).addBox(-0.5F, -0.94F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.06F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, -3.2F, -0.4538F, 0F, 0F));
        PartDefinition p_nature_crest_0_1 = p_nature_crest_0_0.addOrReplaceChild("nature_crest_0_1", CubeListBuilder.create()
                .texOffs(144, 350).addBox(-0.5F, -0.9F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.1F, -0.225F)),
                PartPose.offset(0F, -0.63F, 0F));
        PartDefinition p_nature_crest_1_0 = p_nature_head_head.addOrReplaceChild("nature_crest_1_0", CubeListBuilder.create()
                .texOffs(85, 342).addBox(-0.5F, -1.605F, -0.5F, 1F, 2F, 1F, new CubeDeformation(0F, -0.395F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, -0.6F, -0.4538F, 0F, 0F));
        PartDefinition p_nature_crest_1_1 = p_nature_crest_1_0.addOrReplaceChild("nature_crest_1_1", CubeListBuilder.create()
                .texOffs(90, 342).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.225F, -0.45F, -0.225F)),
                PartPose.offset(0F, -0.96F, 0F));
        PartDefinition p_nature_crest_2_0 = p_nature_head_head.addOrReplaceChild("nature_crest_2_0", CubeListBuilder.create()
                .texOffs(149, 350).addBox(-0.5F, -0.995F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.005F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, 2F, -0.4538F, 0F, 0F));
        PartDefinition p_nature_crest_2_1 = p_nature_crest_2_0.addOrReplaceChild("nature_crest_2_1", CubeListBuilder.create()
                .texOffs(154, 350).addBox(-0.5F, -0.95F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.05F, -0.225F)),
                PartPose.offset(0F, -0.74F, 0F));
        PartDefinition p_nature_chest_body = root.addOrReplaceChild("nature_chest_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_nature_frond_0 = p_nature_chest_body.addOrReplaceChild("nature_frond_0", CubeListBuilder.create()
                .texOffs(100, 254).addBox(-1.5F, -4.6F, -0.5F, 3F, 5F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.35F)),
                PartPose.offsetAndRotation(0F, 1F, 2.9F, -0.8378F, 0F, 0F));
        PartDefinition p_nature_frond_1 = p_nature_chest_body.addOrReplaceChild("nature_frond_1", CubeListBuilder.create()
                .texOffs(99, 279).addBox(-1.5F, -3.8F, -0.5F, 3F, 4F, 1F, new CubeDeformation(-0.4F, -0.2F, -0.35F)),
                PartPose.offsetAndRotation(0F, 4F, 2.9F, -0.8378F, 0F, 0F));
        PartDefinition p_nature_frond_2 = p_nature_chest_body.addOrReplaceChild("nature_frond_2", CubeListBuilder.create()
                .texOffs(54, 306).addBox(-1.5F, -3F, -0.5F, 3F, 3F, 1F, new CubeDeformation(-0.4F, 0F, -0.35F)),
                PartPose.offsetAndRotation(0F, 7F, 2.9F, -0.8378F, 0F, 0F));
        PartDefinition p_nature_chest_left_arm = root.addOrReplaceChild("nature_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_nature_pauldron_left_arm = p_nature_chest_left_arm.addOrReplaceChild("nature_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(0, 82).addBox(-3.3F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F))
                .texOffs(116, 115).addBox(-2F, -0.1F, -4F, 6F, 2F, 8F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.2094F));
        PartDefinition p_nature_claw_left_arm_0 = p_nature_pauldron_left_arm.addOrReplaceChild("nature_claw_left_arm_0", CubeListBuilder.create()
                .texOffs(63, 306).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.285F, -0.35F)),
                PartPose.offsetAndRotation(2.4F, -1.4F, 0F, 0F, 0F, 0.6981F));
        PartDefinition p_nature_claw_left_arm_1 = p_nature_claw_left_arm_0.addOrReplaceChild("nature_claw_left_arm_1", CubeListBuilder.create()
                .texOffs(95, 342).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.35F, -0.1425F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_nature_chest_right_arm = root.addOrReplaceChild("nature_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_nature_pauldron_right_arm = p_nature_chest_right_arm.addOrReplaceChild("nature_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(31, 82).addBox(-3.7F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F))
                .texOffs(145, 115).addBox(-4F, -0.1F, -4F, 6F, 2F, 8F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.2094F));
        PartDefinition p_nature_claw_right_arm_0 = p_nature_pauldron_right_arm.addOrReplaceChild("nature_claw_right_arm_0", CubeListBuilder.create()
                .texOffs(72, 306).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.285F, -0.35F)),
                PartPose.offsetAndRotation(-2.4F, -1.4F, 0F, 0F, 0F, -0.6981F));
        PartDefinition p_nature_claw_right_arm_1 = p_nature_claw_right_arm_0.addOrReplaceChild("nature_claw_right_arm_1", CubeListBuilder.create()
                .texOffs(100, 342).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.35F, -0.1425F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_nature_legs_body = root.addOrReplaceChild("nature_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_nature_tail_0 = p_nature_legs_body.addOrReplaceChild("nature_tail_0", CubeListBuilder.create()
                .texOffs(182, 240).addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F),
                PartPose.offsetAndRotation(0F, 10.2F, 2.2F, 0.5236F, 0F, 0F));
        PartDefinition p_nature_tail_1 = p_nature_tail_0.addOrReplaceChild("nature_tail_1", CubeListBuilder.create()
                .texOffs(195, 240).addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, 2.75F, 0F, 0.1745F, 0F, 0F));
        PartDefinition p_nature_tail_2 = p_nature_tail_1.addOrReplaceChild("nature_tail_2", CubeListBuilder.create()
                .texOffs(208, 240).addBox(-1.5F, -0.1F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.1F, -0.4F)),
                PartPose.offsetAndRotation(0F, 2.75F, 0F, 0.2094F, 0F, 0F));
        PartDefinition p_nature_tail_3 = p_nature_tail_2.addOrReplaceChild("nature_tail_3", CubeListBuilder.create()
                .texOffs(108, 279).addBox(-1F, -0.2F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.15F, -0.2F, -0.15F)),
                PartPose.offsetAndRotation(0F, 2.55F, 0F, 0.2094F, 0F, 0F));
        PartDefinition p_nature_tail_4 = p_nature_tail_3.addOrReplaceChild("nature_tail_4", CubeListBuilder.create()
                .texOffs(117, 279).addBox(-1F, -0.4F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.4F, -0.4F, -0.4F)),
                PartPose.offsetAndRotation(0F, 2.35F, 0F, 0.1745F, 0F, 0F));
        PartDefinition p_nature_tail_tip = p_nature_tail_4.addOrReplaceChild("nature_tail_tip", CubeListBuilder.create()
                .texOffs(180, 208).addBox(-0.5F, -0.4F, -1.5F, 1F, 5F, 3F, new CubeDeformation(-0.35F, -0.4F, -0.1F)),
                PartPose.offsetAndRotation(0F, 2.4F, 0F, 0.1396F, 0F, 0F));
        PartDefinition p_nature_legs_left_leg = root.addOrReplaceChild("nature_legs_left_leg", CubeListBuilder.create()
                .texOffs(11, 296).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_nature_legs_right_leg = root.addOrReplaceChild("nature_legs_right_leg", CubeListBuilder.create()
                .texOffs(22, 296).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_nature_feet_left_leg = root.addOrReplaceChild("nature_feet_left_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_nature_talon_left_leg_0 = p_nature_feet_left_leg.addOrReplaceChild("nature_talon_left_leg_0", CubeListBuilder.create()
                .texOffs(241, 326).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(-1.6F, 11.6F, -2.8F, 0.4887F, -0.2234F, 0F));
        PartDefinition p_nature_talon_left_leg_1 = p_nature_feet_left_leg.addOrReplaceChild("nature_talon_left_leg_1", CubeListBuilder.create()
                .texOffs(248, 326).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(0F, 11.6F, -2.8F, 0.4887F, 0F, 0F));
        PartDefinition p_nature_talon_left_leg_2 = p_nature_feet_left_leg.addOrReplaceChild("nature_talon_left_leg_2", CubeListBuilder.create()
                .texOffs(0, 330).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(1.6F, 11.6F, -2.8F, 0.4887F, 0.2234F, 0F));
        PartDefinition p_nature_feet_right_leg = root.addOrReplaceChild("nature_feet_right_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_nature_talon_right_leg_0 = p_nature_feet_right_leg.addOrReplaceChild("nature_talon_right_leg_0", CubeListBuilder.create()
                .texOffs(7, 330).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(-1.6F, 11.6F, -2.8F, 0.4887F, -0.2234F, 0F));
        PartDefinition p_nature_talon_right_leg_1 = p_nature_feet_right_leg.addOrReplaceChild("nature_talon_right_leg_1", CubeListBuilder.create()
                .texOffs(14, 330).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(0F, 11.6F, -2.8F, 0.4887F, 0F, 0F));
        PartDefinition p_nature_talon_right_leg_2 = p_nature_feet_right_leg.addOrReplaceChild("nature_talon_right_leg_2", CubeListBuilder.create()
                .texOffs(21, 330).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(1.6F, 11.6F, -2.8F, 0.4887F, 0.2234F, 0F));
        PartDefinition p_earth_head_head = root.addOrReplaceChild("earth_head_head", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_ram_horn_left_0 = p_earth_head_head.addOrReplaceChild("ram_horn_left_0", CubeListBuilder.create()
                .texOffs(221, 240).addBox(-1.5F, -2.8F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.1F, -0.2F, -0.1F)),
                PartPose.offsetAndRotation(5F, -8F, -0.6F, -0.4189F, 0F, 0.4887F));
        PartDefinition p_ram_horn_left_1 = p_ram_horn_left_0.addOrReplaceChild("ram_horn_left_1", CubeListBuilder.create()
                .texOffs(234, 240).addBox(-1.5F, -2.8F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.25F, -0.2F, -0.25F)),
                PartPose.offsetAndRotation(0F, -2.35F, 0F, -0.9599F, 0F, 0F));
        PartDefinition p_ram_horn_left_2 = p_ram_horn_left_1.addOrReplaceChild("ram_horn_left_2", CubeListBuilder.create()
                .texOffs(0, 247).addBox(-1.5F, -2.8F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(0F, -2.35F, 0F, -0.9599F, 0F, 0F));
        PartDefinition p_ram_horn_left_3 = p_ram_horn_left_2.addOrReplaceChild("ram_horn_left_3", CubeListBuilder.create()
                .texOffs(126, 279).addBox(-1F, -2.7F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.05F, -0.3F, -0.05F)),
                PartPose.offsetAndRotation(0F, -2.35F, 0F, -0.9599F, 0F, 0F));
        PartDefinition p_ram_horn_left_4 = p_ram_horn_left_3.addOrReplaceChild("ram_horn_left_4", CubeListBuilder.create()
                .texOffs(135, 279).addBox(-1F, -2.6F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.25F, -0.4F, -0.25F)),
                PartPose.offsetAndRotation(0F, -2.15F, 0F, -0.9599F, 0F, 0F));
        PartDefinition p_ram_horn_left_5 = p_ram_horn_left_4.addOrReplaceChild("ram_horn_left_5", CubeListBuilder.create()
                .texOffs(105, 342).addBox(-0.5F, -1.9F, -0.5F, 1F, 2F, 1F, new CubeDeformation(0F, -0.1F, 0F)),
                PartPose.offsetAndRotation(0F, -1.95F, 0F, -0.7854F, 0F, 0F));
        PartDefinition p_ram_horn_right_0 = p_earth_head_head.addOrReplaceChild("ram_horn_right_0", CubeListBuilder.create()
                .texOffs(13, 247).addBox(-1.5F, -2.8F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.1F, -0.2F, -0.1F)),
                PartPose.offsetAndRotation(-5F, -8F, -0.6F, -0.4189F, 0F, -0.4887F));
        PartDefinition p_ram_horn_right_1 = p_ram_horn_right_0.addOrReplaceChild("ram_horn_right_1", CubeListBuilder.create()
                .texOffs(26, 247).addBox(-1.5F, -2.8F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.25F, -0.2F, -0.25F)),
                PartPose.offsetAndRotation(0F, -2.35F, 0F, -0.9599F, 0F, 0F));
        PartDefinition p_ram_horn_right_2 = p_ram_horn_right_1.addOrReplaceChild("ram_horn_right_2", CubeListBuilder.create()
                .texOffs(39, 247).addBox(-1.5F, -2.8F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(0F, -2.35F, 0F, -0.9599F, 0F, 0F));
        PartDefinition p_ram_horn_right_3 = p_ram_horn_right_2.addOrReplaceChild("ram_horn_right_3", CubeListBuilder.create()
                .texOffs(144, 279).addBox(-1F, -2.7F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.05F, -0.3F, -0.05F)),
                PartPose.offsetAndRotation(0F, -2.35F, 0F, -0.9599F, 0F, 0F));
        PartDefinition p_ram_horn_right_4 = p_ram_horn_right_3.addOrReplaceChild("ram_horn_right_4", CubeListBuilder.create()
                .texOffs(153, 279).addBox(-1F, -2.6F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.25F, -0.4F, -0.25F)),
                PartPose.offsetAndRotation(0F, -2.15F, 0F, -0.9599F, 0F, 0F));
        PartDefinition p_ram_horn_right_5 = p_ram_horn_right_4.addOrReplaceChild("ram_horn_right_5", CubeListBuilder.create()
                .texOffs(110, 342).addBox(-0.5F, -1.9F, -0.5F, 1F, 2F, 1F, new CubeDeformation(0F, -0.1F, 0F)),
                PartPose.offsetAndRotation(0F, -1.95F, 0F, -0.7854F, 0F, 0F));
        PartDefinition p_earth_crest_0_0 = p_earth_head_head.addOrReplaceChild("earth_crest_0_0", CubeListBuilder.create()
                .texOffs(159, 350).addBox(-0.5F, -0.94F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.06F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, -3.2F, -0.4538F, 0F, 0F));
        PartDefinition p_earth_crest_0_1 = p_earth_crest_0_0.addOrReplaceChild("earth_crest_0_1", CubeListBuilder.create()
                .texOffs(164, 350).addBox(-0.5F, -0.9F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.1F, -0.225F)),
                PartPose.offset(0F, -0.63F, 0F));
        PartDefinition p_earth_crest_1_0 = p_earth_head_head.addOrReplaceChild("earth_crest_1_0", CubeListBuilder.create()
                .texOffs(115, 342).addBox(-0.5F, -1.605F, -0.5F, 1F, 2F, 1F, new CubeDeformation(0F, -0.395F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, -0.6F, -0.4538F, 0F, 0F));
        PartDefinition p_earth_crest_1_1 = p_earth_crest_1_0.addOrReplaceChild("earth_crest_1_1", CubeListBuilder.create()
                .texOffs(120, 342).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.225F, -0.45F, -0.225F)),
                PartPose.offset(0F, -0.96F, 0F));
        PartDefinition p_earth_crest_2_0 = p_earth_head_head.addOrReplaceChild("earth_crest_2_0", CubeListBuilder.create()
                .texOffs(169, 350).addBox(-0.5F, -0.995F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.005F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, 2F, -0.4538F, 0F, 0F));
        PartDefinition p_earth_crest_2_1 = p_earth_crest_2_0.addOrReplaceChild("earth_crest_2_1", CubeListBuilder.create()
                .texOffs(174, 350).addBox(-0.5F, -0.95F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.05F, -0.225F)),
                PartPose.offset(0F, -0.74F, 0F));
        PartDefinition p_earth_chest_body = root.addOrReplaceChild("earth_chest_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_earth_plate_0 = p_earth_chest_body.addOrReplaceChild("earth_plate_0", CubeListBuilder.create()
                .texOffs(107, 225).addBox(-0.5F, -3.6F, -1.5F, 1F, 4F, 3F, new CubeDeformation(-0.1F, -0.4F, -0.3F))
                .texOffs(28, 330).addBox(-0.5F, -4.1F, -1F, 1F, 1F, 2F, new CubeDeformation(-0.2F, 0F, -0.4F)),
                PartPose.offsetAndRotation(0F, 0.8F, 2.9F, -0.6981F, 0F, 0F));
        PartDefinition p_earth_plate_1 = p_earth_chest_body.addOrReplaceChild("earth_plate_1", CubeListBuilder.create()
                .texOffs(116, 225).addBox(-0.5F, -3.8F, -1.5F, 1F, 4F, 3F, new CubeDeformation(-0.1F, -0.2F, -0.3F))
                .texOffs(35, 330).addBox(-0.5F, -4.5F, -1F, 1F, 1F, 2F, new CubeDeformation(-0.2F, 0F, -0.4F)),
                PartPose.offsetAndRotation(0F, 4F, 2.9F, -0.6981F, 0F, 0F));
        PartDefinition p_earth_plate_2 = p_earth_chest_body.addOrReplaceChild("earth_plate_2", CubeListBuilder.create()
                .texOffs(109, 254).addBox(-0.5F, -2.9F, -1.5F, 1F, 3F, 3F, new CubeDeformation(-0.1F, -0.1F, -0.3F))
                .texOffs(42, 330).addBox(-0.5F, -3.7F, -1F, 1F, 1F, 2F, new CubeDeformation(-0.2F, 0F, -0.4F)),
                PartPose.offsetAndRotation(0F, 7.4F, 2.9F, -0.6981F, 0F, 0F));
        PartDefinition p_earth_chest_left_arm = root.addOrReplaceChild("earth_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_earth_pauldron_left_arm = p_earth_chest_left_arm.addOrReplaceChild("earth_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(62, 82).addBox(-3.3F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F))
                .texOffs(174, 115).addBox(-2F, -0.1F, -4F, 6F, 2F, 8F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.2094F));
        PartDefinition p_earth_claw_left_arm_0 = p_earth_pauldron_left_arm.addOrReplaceChild("earth_claw_left_arm_0", CubeListBuilder.create()
                .texOffs(81, 306).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.285F, -0.35F)),
                PartPose.offsetAndRotation(2.4F, -1.4F, 0F, 0F, 0F, 0.6981F));
        PartDefinition p_earth_claw_left_arm_1 = p_earth_claw_left_arm_0.addOrReplaceChild("earth_claw_left_arm_1", CubeListBuilder.create()
                .texOffs(125, 342).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.35F, -0.1425F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_earth_chest_right_arm = root.addOrReplaceChild("earth_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_earth_pauldron_right_arm = p_earth_chest_right_arm.addOrReplaceChild("earth_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(93, 82).addBox(-3.7F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F))
                .texOffs(203, 115).addBox(-4F, -0.1F, -4F, 6F, 2F, 8F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.2094F));
        PartDefinition p_earth_claw_right_arm_0 = p_earth_pauldron_right_arm.addOrReplaceChild("earth_claw_right_arm_0", CubeListBuilder.create()
                .texOffs(90, 306).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.285F, -0.35F)),
                PartPose.offsetAndRotation(-2.4F, -1.4F, 0F, 0F, 0F, -0.6981F));
        PartDefinition p_earth_claw_right_arm_1 = p_earth_claw_right_arm_0.addOrReplaceChild("earth_claw_right_arm_1", CubeListBuilder.create()
                .texOffs(130, 342).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.35F, -0.1425F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_earth_legs_body = root.addOrReplaceChild("earth_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_earth_tail_0 = p_earth_legs_body.addOrReplaceChild("earth_tail_0", CubeListBuilder.create()
                .texOffs(52, 247).addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F),
                PartPose.offsetAndRotation(0F, 10.2F, 2.2F, 0.5236F, 0F, 0F));
        PartDefinition p_earth_tail_1 = p_earth_tail_0.addOrReplaceChild("earth_tail_1", CubeListBuilder.create()
                .texOffs(65, 247).addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, 2.75F, 0F, 0.1745F, 0F, 0F));
        PartDefinition p_earth_tail_2 = p_earth_tail_1.addOrReplaceChild("earth_tail_2", CubeListBuilder.create()
                .texOffs(78, 247).addBox(-1.5F, -0.1F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.1F, -0.4F)),
                PartPose.offsetAndRotation(0F, 2.75F, 0F, 0.2094F, 0F, 0F));
        PartDefinition p_earth_tail_3 = p_earth_tail_2.addOrReplaceChild("earth_tail_3", CubeListBuilder.create()
                .texOffs(162, 279).addBox(-1F, -0.2F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.15F, -0.2F, -0.15F)),
                PartPose.offsetAndRotation(0F, 2.55F, 0F, 0.2094F, 0F, 0F));
        PartDefinition p_earth_tail_4 = p_earth_tail_3.addOrReplaceChild("earth_tail_4", CubeListBuilder.create()
                .texOffs(171, 279).addBox(-1F, -0.4F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.4F, -0.4F, -0.4F)),
                PartPose.offsetAndRotation(0F, 2.35F, 0F, 0.1745F, 0F, 0F));
        PartDefinition p_earth_tail_tip = p_earth_tail_4.addOrReplaceChild("earth_tail_tip", CubeListBuilder.create()
                .texOffs(119, 217).addBox(-2F, -0.1F, -2F, 4F, 3F, 4F, new CubeDeformation(-0.4F, -0.1F, -0.4F))
                .texOffs(49, 330).addBox(-1F, 0.2F, -2.15F, 2F, 2F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.25F)),
                PartPose.offsetAndRotation(0F, 2.4F, 0F, 0.1396F, 0F, 0F));
        PartDefinition p_earth_legs_left_leg = root.addOrReplaceChild("earth_legs_left_leg", CubeListBuilder.create()
                .texOffs(33, 296).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_earth_legs_right_leg = root.addOrReplaceChild("earth_legs_right_leg", CubeListBuilder.create()
                .texOffs(44, 296).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_earth_feet_left_leg = root.addOrReplaceChild("earth_feet_left_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_earth_talon_left_leg_0 = p_earth_feet_left_leg.addOrReplaceChild("earth_talon_left_leg_0", CubeListBuilder.create()
                .texOffs(56, 330).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(-1.6F, 11.6F, -2.8F, 0.4887F, -0.2234F, 0F));
        PartDefinition p_earth_talon_left_leg_1 = p_earth_feet_left_leg.addOrReplaceChild("earth_talon_left_leg_1", CubeListBuilder.create()
                .texOffs(63, 330).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(0F, 11.6F, -2.8F, 0.4887F, 0F, 0F));
        PartDefinition p_earth_talon_left_leg_2 = p_earth_feet_left_leg.addOrReplaceChild("earth_talon_left_leg_2", CubeListBuilder.create()
                .texOffs(70, 330).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(1.6F, 11.6F, -2.8F, 0.4887F, 0.2234F, 0F));
        PartDefinition p_earth_feet_right_leg = root.addOrReplaceChild("earth_feet_right_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_earth_talon_right_leg_0 = p_earth_feet_right_leg.addOrReplaceChild("earth_talon_right_leg_0", CubeListBuilder.create()
                .texOffs(77, 330).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(-1.6F, 11.6F, -2.8F, 0.4887F, -0.2234F, 0F));
        PartDefinition p_earth_talon_right_leg_1 = p_earth_feet_right_leg.addOrReplaceChild("earth_talon_right_leg_1", CubeListBuilder.create()
                .texOffs(84, 330).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(0F, 11.6F, -2.8F, 0.4887F, 0F, 0F));
        PartDefinition p_earth_talon_right_leg_2 = p_earth_feet_right_leg.addOrReplaceChild("earth_talon_right_leg_2", CubeListBuilder.create()
                .texOffs(91, 330).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(1.6F, 11.6F, -2.8F, 0.4887F, 0.2234F, 0F));
        PartDefinition p_water_head_head = root.addOrReplaceChild("water_head_head", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_head_fin_left = p_water_head_head.addOrReplaceChild("head_fin_left", CubeListBuilder.create(),
                PartPose.offsetAndRotation(5.3F, -4.6F, -1F, 0F, 0.2443F, 0F));
        PartDefinition p_head_fin_left_ray_0 = p_head_fin_left.addOrReplaceChild("head_fin_left_ray_0", CubeListBuilder.create()
                .texOffs(227, 217).addBox(-0.5F, -1F, -0.2F, 1F, 2F, 5F, new CubeDeformation(-0.25F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.1047F, 0F, 0F));
        PartDefinition p_head_fin_left_ray_1 = p_head_fin_left.addOrReplaceChild("head_fin_left_ray_1", CubeListBuilder.create()
                .texOffs(85, 208).addBox(-0.5F, -1F, -0.3F, 1F, 2F, 6F, new CubeDeformation(-0.25F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0.2443F, 0F, 0F));
        PartDefinition p_head_fin_left_ray_2 = p_head_fin_left.addOrReplaceChild("head_fin_left_ray_2", CubeListBuilder.create()
                .texOffs(240, 217).addBox(-0.5F, -1F, 0F, 1F, 2F, 5F, new CubeDeformation(-0.25F, -0.25F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0.5934F, 0F, 0F));
        PartDefinition p_head_fin_left_ray_3 = p_head_fin_left.addOrReplaceChild("head_fin_left_ray_3", CubeListBuilder.create()
                .texOffs(230, 247).addBox(-0.5F, -1F, -0.1F, 1F, 2F, 4F, new CubeDeformation(-0.25F, -0.35F, -0.1F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0.9425F, 0F, 0F));
        PartDefinition p_head_fin_right = p_water_head_head.addOrReplaceChild("head_fin_right", CubeListBuilder.create(),
                PartPose.offsetAndRotation(-5.3F, -4.6F, -1F, 0F, -0.2443F, 0F));
        PartDefinition p_head_fin_right_ray_0 = p_head_fin_right.addOrReplaceChild("head_fin_right_ray_0", CubeListBuilder.create()
                .texOffs(0, 225).addBox(-0.5F, -1F, -0.2F, 1F, 2F, 5F, new CubeDeformation(-0.25F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.1047F, 0F, 0F));
        PartDefinition p_head_fin_right_ray_1 = p_head_fin_right.addOrReplaceChild("head_fin_right_ray_1", CubeListBuilder.create()
                .texOffs(100, 208).addBox(-0.5F, -1F, -0.3F, 1F, 2F, 6F, new CubeDeformation(-0.25F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0.2443F, 0F, 0F));
        PartDefinition p_head_fin_right_ray_2 = p_head_fin_right.addOrReplaceChild("head_fin_right_ray_2", CubeListBuilder.create()
                .texOffs(13, 225).addBox(-0.5F, -1F, 0F, 1F, 2F, 5F, new CubeDeformation(-0.25F, -0.25F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0.5934F, 0F, 0F));
        PartDefinition p_head_fin_right_ray_3 = p_head_fin_right.addOrReplaceChild("head_fin_right_ray_3", CubeListBuilder.create()
                .texOffs(241, 247).addBox(-0.5F, -1F, -0.1F, 1F, 2F, 4F, new CubeDeformation(-0.25F, -0.35F, -0.1F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0.9425F, 0F, 0F));
        PartDefinition p_water_crest_0_0 = p_water_head_head.addOrReplaceChild("water_crest_0_0", CubeListBuilder.create()
                .texOffs(179, 350).addBox(-0.5F, -0.94F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.06F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, -3.2F, -0.4538F, 0F, 0F));
        PartDefinition p_water_crest_0_1 = p_water_crest_0_0.addOrReplaceChild("water_crest_0_1", CubeListBuilder.create()
                .texOffs(184, 350).addBox(-0.5F, -0.9F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.1F, -0.225F)),
                PartPose.offset(0F, -0.63F, 0F));
        PartDefinition p_water_crest_1_0 = p_water_head_head.addOrReplaceChild("water_crest_1_0", CubeListBuilder.create()
                .texOffs(135, 342).addBox(-0.5F, -1.605F, -0.5F, 1F, 2F, 1F, new CubeDeformation(0F, -0.395F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, -0.6F, -0.4538F, 0F, 0F));
        PartDefinition p_water_crest_1_1 = p_water_crest_1_0.addOrReplaceChild("water_crest_1_1", CubeListBuilder.create()
                .texOffs(140, 342).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.225F, -0.45F, -0.225F)),
                PartPose.offset(0F, -0.96F, 0F));
        PartDefinition p_water_crest_2_0 = p_water_head_head.addOrReplaceChild("water_crest_2_0", CubeListBuilder.create()
                .texOffs(189, 350).addBox(-0.5F, -0.995F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.005F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, 2F, -0.4538F, 0F, 0F));
        PartDefinition p_water_crest_2_1 = p_water_crest_2_0.addOrReplaceChild("water_crest_2_1", CubeListBuilder.create()
                .texOffs(194, 350).addBox(-0.5F, -0.95F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.05F, -0.225F)),
                PartPose.offset(0F, -0.74F, 0F));
        PartDefinition p_water_chest_body = root.addOrReplaceChild("water_chest_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_water_back_fin_0 = p_water_chest_body.addOrReplaceChild("water_back_fin_0", CubeListBuilder.create(),
                PartPose.offset(0F, 0.6F, 2.9F));
        PartDefinition p_water_back_fin_0_ray_0 = p_water_back_fin_0.addOrReplaceChild("water_back_fin_0_ray_0", CubeListBuilder.create()
                .texOffs(145, 208).addBox(-0.5F, -1.5F, -0.3F, 1F, 3F, 5F, new CubeDeformation(-0.25F, -0.4F, -0.3F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.6632F, 0F, 0F));
        PartDefinition p_water_back_fin_0_ray_1 = p_water_back_fin_0.addOrReplaceChild("water_back_fin_0_ray_1", CubeListBuilder.create()
                .texOffs(0, 254).addBox(-0.5F, -1F, -0.24F, 1F, 2F, 4F, new CubeDeformation(-0.25F, 0F, -0.24F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.1396F, 0F, 0F));
        PartDefinition p_water_back_fin_1 = p_water_chest_body.addOrReplaceChild("water_back_fin_1", CubeListBuilder.create(),
                PartPose.offset(0F, 3.4F, 2.9F));
        PartDefinition p_water_back_fin_1_ray_0 = p_water_back_fin_1.addOrReplaceChild("water_back_fin_1_ray_0", CubeListBuilder.create()
                .texOffs(85, 225).addBox(-0.5F, -1.5F, 0F, 1F, 3F, 4F, new CubeDeformation(-0.25F, -0.4F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.6632F, 0F, 0F));
        PartDefinition p_water_back_fin_1_ray_1 = p_water_back_fin_1.addOrReplaceChild("water_back_fin_1_ray_1", CubeListBuilder.create()
                .texOffs(11, 254).addBox(-0.5F, -1F, -0.4F, 1F, 2F, 4F, new CubeDeformation(-0.25F, 0F, -0.4F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.1396F, 0F, 0F));
        PartDefinition p_water_back_fin_2 = p_water_chest_body.addOrReplaceChild("water_back_fin_2", CubeListBuilder.create(),
                PartPose.offset(0F, 6.2F, 2.9F));
        PartDefinition p_water_back_fin_2_ray_0 = p_water_back_fin_2.addOrReplaceChild("water_back_fin_2_ray_0", CubeListBuilder.create()
                .texOffs(96, 225).addBox(-0.5F, -1.5F, -0.3F, 1F, 3F, 4F, new CubeDeformation(-0.25F, -0.4F, -0.3F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.6632F, 0F, 0F));
        PartDefinition p_water_back_fin_2_ray_1 = p_water_back_fin_2.addOrReplaceChild("water_back_fin_2_ray_1", CubeListBuilder.create()
                .texOffs(180, 279).addBox(-0.5F, -1F, -0.14F, 1F, 2F, 3F, new CubeDeformation(-0.25F, 0F, -0.14F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.1396F, 0F, 0F));
        PartDefinition p_water_back_fin_3 = p_water_chest_body.addOrReplaceChild("water_back_fin_3", CubeListBuilder.create(),
                PartPose.offset(0F, 9F, 2.9F));
        PartDefinition p_water_back_fin_3_ray_0 = p_water_back_fin_3.addOrReplaceChild("water_back_fin_3_ray_0", CubeListBuilder.create()
                .texOffs(118, 254).addBox(-0.5F, -1.5F, -0.2F, 1F, 3F, 3F, new CubeDeformation(-0.25F, -0.4F, -0.2F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.6632F, 0F, 0F));
        PartDefinition p_water_back_fin_3_ray_1 = p_water_back_fin_3.addOrReplaceChild("water_back_fin_3_ray_1", CubeListBuilder.create()
                .texOffs(189, 279).addBox(-0.5F, -1F, -0.46F, 1F, 2F, 3F, new CubeDeformation(-0.25F, 0F, -0.46F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.1396F, 0F, 0F));
        PartDefinition p_water_chest_left_arm = root.addOrReplaceChild("water_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_water_pauldron_left_arm = p_water_chest_left_arm.addOrReplaceChild("water_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(124, 82).addBox(-3.3F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F))
                .texOffs(0, 126).addBox(-2F, -0.1F, -4F, 6F, 2F, 8F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.2094F));
        PartDefinition p_water_claw_left_arm_0 = p_water_pauldron_left_arm.addOrReplaceChild("water_claw_left_arm_0", CubeListBuilder.create()
                .texOffs(99, 306).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.285F, -0.35F)),
                PartPose.offsetAndRotation(2.4F, -1.4F, 0F, 0F, 0F, 0.6981F));
        PartDefinition p_water_claw_left_arm_1 = p_water_claw_left_arm_0.addOrReplaceChild("water_claw_left_arm_1", CubeListBuilder.create()
                .texOffs(145, 342).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.35F, -0.1425F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_water_chest_right_arm = root.addOrReplaceChild("water_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_water_pauldron_right_arm = p_water_chest_right_arm.addOrReplaceChild("water_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(155, 82).addBox(-3.7F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F))
                .texOffs(29, 126).addBox(-4F, -0.1F, -4F, 6F, 2F, 8F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.2094F));
        PartDefinition p_water_claw_right_arm_0 = p_water_pauldron_right_arm.addOrReplaceChild("water_claw_right_arm_0", CubeListBuilder.create()
                .texOffs(108, 306).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.285F, -0.35F)),
                PartPose.offsetAndRotation(-2.4F, -1.4F, 0F, 0F, 0F, -0.6981F));
        PartDefinition p_water_claw_right_arm_1 = p_water_claw_right_arm_0.addOrReplaceChild("water_claw_right_arm_1", CubeListBuilder.create()
                .texOffs(150, 342).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.35F, -0.1425F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_water_legs_body = root.addOrReplaceChild("water_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_water_tail_0 = p_water_legs_body.addOrReplaceChild("water_tail_0", CubeListBuilder.create()
                .texOffs(91, 247).addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F),
                PartPose.offsetAndRotation(0F, 10.2F, 2.2F, 0.5236F, 0F, 0F));
        PartDefinition p_water_tail_1 = p_water_tail_0.addOrReplaceChild("water_tail_1", CubeListBuilder.create()
                .texOffs(104, 247).addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, 2.75F, 0F, 0.1745F, 0F, 0F));
        PartDefinition p_water_tail_2 = p_water_tail_1.addOrReplaceChild("water_tail_2", CubeListBuilder.create()
                .texOffs(117, 247).addBox(-1.5F, -0.1F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.1F, -0.4F)),
                PartPose.offsetAndRotation(0F, 2.75F, 0F, 0.2094F, 0F, 0F));
        PartDefinition p_water_tail_3 = p_water_tail_2.addOrReplaceChild("water_tail_3", CubeListBuilder.create()
                .texOffs(198, 279).addBox(-1F, -0.2F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.15F, -0.2F, -0.15F)),
                PartPose.offsetAndRotation(0F, 2.55F, 0F, 0.2094F, 0F, 0F));
        PartDefinition p_water_tail_4 = p_water_tail_3.addOrReplaceChild("water_tail_4", CubeListBuilder.create()
                .texOffs(207, 279).addBox(-1F, -0.4F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.4F, -0.4F, -0.4F)),
                PartPose.offsetAndRotation(0F, 2.35F, 0F, 0.1745F, 0F, 0F));
        PartDefinition p_water_tail_tip = p_water_tail_4.addOrReplaceChild("water_tail_tip", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0F, 2.4F, 0F, 0.1396F, 0F, 0F));
        PartDefinition p_water_tail_fin = p_water_tail_tip.addOrReplaceChild("water_tail_fin", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0F, 0.6F, 0F, 1.5708F, 0F, 0F));
        PartDefinition p_water_tail_fin_ray_0 = p_water_tail_fin.addOrReplaceChild("water_tail_fin_ray_0", CubeListBuilder.create()
                .texOffs(22, 254).addBox(-0.5F, -1F, -0.2F, 1F, 2F, 4F, new CubeDeformation(-0.25F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.6981F, 0F, 0F));
        PartDefinition p_water_tail_fin_ray_1 = p_water_tail_fin.addOrReplaceChild("water_tail_fin_ray_1", CubeListBuilder.create()
                .texOffs(33, 254).addBox(-0.5F, -1F, 0F, 1F, 2F, 4F, new CubeDeformation(-0.25F, -0.2F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.1745F, 0F, 0F));
        PartDefinition p_water_tail_fin_ray_2 = p_water_tail_fin.addOrReplaceChild("water_tail_fin_ray_2", CubeListBuilder.create()
                .texOffs(44, 254).addBox(-0.5F, -1F, -0.2F, 1F, 2F, 4F, new CubeDeformation(-0.25F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0.3491F, 0F, 0F));
        PartDefinition p_water_legs_left_leg = root.addOrReplaceChild("water_legs_left_leg", CubeListBuilder.create()
                .texOffs(55, 296).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_water_legs_right_leg = root.addOrReplaceChild("water_legs_right_leg", CubeListBuilder.create()
                .texOffs(66, 296).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_water_feet_left_leg = root.addOrReplaceChild("water_feet_left_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_water_talon_left_leg_0 = p_water_feet_left_leg.addOrReplaceChild("water_talon_left_leg_0", CubeListBuilder.create()
                .texOffs(98, 330).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(-1.6F, 11.6F, -2.8F, 0.4887F, -0.2234F, 0F));
        PartDefinition p_water_talon_left_leg_1 = p_water_feet_left_leg.addOrReplaceChild("water_talon_left_leg_1", CubeListBuilder.create()
                .texOffs(105, 330).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(0F, 11.6F, -2.8F, 0.4887F, 0F, 0F));
        PartDefinition p_water_talon_left_leg_2 = p_water_feet_left_leg.addOrReplaceChild("water_talon_left_leg_2", CubeListBuilder.create()
                .texOffs(112, 330).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(1.6F, 11.6F, -2.8F, 0.4887F, 0.2234F, 0F));
        PartDefinition p_water_feet_right_leg = root.addOrReplaceChild("water_feet_right_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_water_talon_right_leg_0 = p_water_feet_right_leg.addOrReplaceChild("water_talon_right_leg_0", CubeListBuilder.create()
                .texOffs(119, 330).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(-1.6F, 11.6F, -2.8F, 0.4887F, -0.2234F, 0F));
        PartDefinition p_water_talon_right_leg_1 = p_water_feet_right_leg.addOrReplaceChild("water_talon_right_leg_1", CubeListBuilder.create()
                .texOffs(126, 330).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(0F, 11.6F, -2.8F, 0.4887F, 0F, 0F));
        PartDefinition p_water_talon_right_leg_2 = p_water_feet_right_leg.addOrReplaceChild("water_talon_right_leg_2", CubeListBuilder.create()
                .texOffs(133, 330).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(1.6F, 11.6F, -2.8F, 0.4887F, 0.2234F, 0F));
        PartDefinition p_crown_head_head = root.addOrReplaceChild("crown_head_head", CubeListBuilder.create()
                .texOffs(59, 321).addBox(-5.5F, -10F, -5.5F, 11F, 2F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.1F))
                .texOffs(84, 321).addBox(-5.5F, -10F, 4.5F, 11F, 2F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.1F))
                .texOffs(143, 21).addBox(-5.5F, -10F, -5F, 1F, 2F, 10F, new CubeDeformation(-0.1F, -0.1F, -0.4F))
                .texOffs(166, 21).addBox(4.5F, -10F, -5F, 1F, 2F, 10F, new CubeDeformation(-0.1F, -0.1F, -0.4F))
                .texOffs(140, 330).addBox(-1F, -10F, -6.1F, 2F, 2F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.2F))
                .texOffs(199, 350).addBox(-4F, -9.5F, -6.05F, 1F, 1F, 1F, new CubeDeformation(0F, -0.1F, -0.25F))
                .texOffs(204, 350).addBox(3F, -9.5F, -6.05F, 1F, 1F, 1F, new CubeDeformation(0F, -0.1F, -0.25F))
                .texOffs(147, 330).addBox(-6.1F, -9.5F, -1F, 1F, 1F, 2F, new CubeDeformation(-0.2F, 0F, -0.4F))
                .texOffs(154, 330).addBox(5.1F, -9.5F, -1F, 1F, 1F, 2F, new CubeDeformation(-0.2F, 0F, -0.4F))
                .texOffs(27, 350).addBox(-1F, -9.5F, 5.1F, 2F, 1F, 1F, new CubeDeformation(-0.4F, 0F, -0.2F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_crown_point_0_0 = p_crown_head_head.addOrReplaceChild("crown_point_0_0", CubeListBuilder.create()
                .texOffs(161, 330).addBox(-1F, -1.96F, -0.5F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.04F, -0.1F)),
                PartPose.offset(0F, -9.7F, -5.4F));
        PartDefinition p_crown_point_0_1 = p_crown_point_0_0.addOrReplaceChild("crown_point_0_1", CubeListBuilder.create()
                .texOffs(155, 342).addBox(-0.5F, -1.8F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.15F, -0.2F, -0.25F))
                .texOffs(209, 350).addBox(-0.5F, -2.4F, -0.5F, 1F, 1F, 1F),
                PartPose.offset(0F, -1.67F, 0F));
        PartDefinition p_crown_point_1_0 = p_crown_head_head.addOrReplaceChild("crown_point_1_0", CubeListBuilder.create()
                .texOffs(168, 330).addBox(-1F, -1.66F, -0.5F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.34F, -0.1F)),
                PartPose.offset(-3.2F, -9.7F, -5.4F));
        PartDefinition p_crown_point_1_1 = p_crown_point_1_0.addOrReplaceChild("crown_point_1_1", CubeListBuilder.create()
                .texOffs(160, 342).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.15F, -0.45F, -0.25F))
                .texOffs(214, 350).addBox(-0.5F, -1.9F, -0.5F, 1F, 1F, 1F),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_crown_point_2_0 = p_crown_head_head.addOrReplaceChild("crown_point_2_0", CubeListBuilder.create()
                .texOffs(175, 330).addBox(-1F, -1.66F, -0.5F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.34F, -0.1F)),
                PartPose.offset(3.2F, -9.7F, -5.4F));
        PartDefinition p_crown_point_2_1 = p_crown_point_2_0.addOrReplaceChild("crown_point_2_1", CubeListBuilder.create()
                .texOffs(165, 342).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.15F, -0.45F, -0.25F))
                .texOffs(219, 350).addBox(-0.5F, -1.9F, -0.5F, 1F, 1F, 1F),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_crown_point_3_0 = p_crown_head_head.addOrReplaceChild("crown_point_3_0", CubeListBuilder.create()
                .texOffs(182, 330).addBox(-1F, -1.72F, -0.5F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.28F, -0.1F)),
                PartPose.offset(0F, -9.7F, 5.4F));
        PartDefinition p_crown_point_3_1 = p_crown_point_3_0.addOrReplaceChild("crown_point_3_1", CubeListBuilder.create()
                .texOffs(170, 342).addBox(-0.5F, -1.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.15F, -0.4F, -0.25F))
                .texOffs(224, 350).addBox(-0.5F, -2F, -0.5F, 1F, 1F, 1F),
                PartPose.offset(0F, -1.19F, 0F));
        PartDefinition p_crown_point_4_0 = p_crown_head_head.addOrReplaceChild("crown_point_4_0", CubeListBuilder.create()
                .texOffs(189, 330).addBox(-1F, -1.54F, -0.5F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.46F, -0.1F)),
                PartPose.offset(-3.2F, -9.7F, 5.4F));
        PartDefinition p_crown_point_4_1 = p_crown_point_4_0.addOrReplaceChild("crown_point_4_1", CubeListBuilder.create()
                .texOffs(229, 350).addBox(-0.5F, -0.95F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.15F, -0.05F, -0.25F))
                .texOffs(234, 350).addBox(-0.5F, -1.7F, -0.5F, 1F, 1F, 1F),
                PartPose.offset(0F, -0.83F, 0F));
        PartDefinition p_crown_point_5_0 = p_crown_head_head.addOrReplaceChild("crown_point_5_0", CubeListBuilder.create()
                .texOffs(196, 330).addBox(-1F, -1.54F, -0.5F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.46F, -0.1F)),
                PartPose.offset(3.2F, -9.7F, 5.4F));
        PartDefinition p_crown_point_5_1 = p_crown_point_5_0.addOrReplaceChild("crown_point_5_1", CubeListBuilder.create()
                .texOffs(239, 350).addBox(-0.5F, -0.95F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.15F, -0.05F, -0.25F))
                .texOffs(244, 350).addBox(-0.5F, -1.7F, -0.5F, 1F, 1F, 1F),
                PartPose.offset(0F, -0.83F, 0F));
        PartDefinition p_crown_point_6_0 = p_crown_head_head.addOrReplaceChild("crown_point_6_0", CubeListBuilder.create()
                .texOffs(117, 306).addBox(-1F, -1.6F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.4F, -0.3F)),
                PartPose.offset(-5.4F, -9.7F, -1.8F));
        PartDefinition p_crown_point_6_1 = p_crown_point_6_0.addOrReplaceChild("crown_point_6_1", CubeListBuilder.create()
                .texOffs(249, 350).addBox(-0.5F, -1F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.15F, 0F, -0.15F))
                .texOffs(0, 353).addBox(-0.5F, -1.8F, -0.5F, 1F, 1F, 1F),
                PartPose.offset(0F, -0.95F, 0F));
        PartDefinition p_crown_point_7_0 = p_crown_head_head.addOrReplaceChild("crown_point_7_0", CubeListBuilder.create()
                .texOffs(126, 306).addBox(-1F, -1.6F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.4F, -0.3F)),
                PartPose.offset(-5.4F, -9.7F, 1.8F));
        PartDefinition p_crown_point_7_1 = p_crown_point_7_0.addOrReplaceChild("crown_point_7_1", CubeListBuilder.create()
                .texOffs(5, 353).addBox(-0.5F, -1F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.15F, 0F, -0.15F))
                .texOffs(10, 353).addBox(-0.5F, -1.8F, -0.5F, 1F, 1F, 1F),
                PartPose.offset(0F, -0.95F, 0F));
        PartDefinition p_crown_point_8_0 = p_crown_head_head.addOrReplaceChild("crown_point_8_0", CubeListBuilder.create()
                .texOffs(135, 306).addBox(-1F, -1.6F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.4F, -0.3F)),
                PartPose.offset(5.4F, -9.7F, -1.8F));
        PartDefinition p_crown_point_8_1 = p_crown_point_8_0.addOrReplaceChild("crown_point_8_1", CubeListBuilder.create()
                .texOffs(15, 353).addBox(-0.5F, -1F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.15F, 0F, -0.15F))
                .texOffs(20, 353).addBox(-0.5F, -1.8F, -0.5F, 1F, 1F, 1F),
                PartPose.offset(0F, -0.95F, 0F));
        PartDefinition p_crown_point_9_0 = p_crown_head_head.addOrReplaceChild("crown_point_9_0", CubeListBuilder.create()
                .texOffs(144, 306).addBox(-1F, -1.6F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.4F, -0.3F)),
                PartPose.offset(5.4F, -9.7F, 1.8F));
        PartDefinition p_crown_point_9_1 = p_crown_point_9_0.addOrReplaceChild("crown_point_9_1", CubeListBuilder.create()
                .texOffs(25, 353).addBox(-0.5F, -1F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.15F, 0F, -0.15F))
                .texOffs(30, 353).addBox(-0.5F, -1.8F, -0.5F, 1F, 1F, 1F),
                PartPose.offset(0F, -0.95F, 0F));
        PartDefinition p_leather_head_head = root.addOrReplaceChild("leather_head_head", CubeListBuilder.create()
                .texOffs(96, 267).addBox(4.2F, -7.5F, -0.1F, 2F, 2F, 3F, new CubeDeformation(-0.4F, -0.1F, -0.3F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_leather_feather = p_leather_head_head.addOrReplaceChild("leather_feather", CubeListBuilder.create()
                .texOffs(223, 21).addBox(-0.5F, -8.6F, -1.5F, 1F, 9F, 3F, new CubeDeformation(-0.2F, -0.4F, -0.4F)),
                PartPose.offsetAndRotation(5F, -6.4F, 1.4F, -0.5236F, 0F, 0.2793F));
        PartDefinition p_leather_chest_body = root.addOrReplaceChild("leather_chest_body", CubeListBuilder.create()
                .texOffs(0, 35).addBox(-6F, -1.9F, -4F, 12F, 3F, 8F, new CubeDeformation(-0.25F, 0.05F, -0.15F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_leather_chest_left_arm = root.addOrReplaceChild("leather_chest_left_arm", CubeListBuilder.create()
                .texOffs(58, 126).addBox(-2.45F, -4.4F, -3.5F, 7F, 3F, 7F, new CubeDeformation(0.05F, -0.3F, 0.1F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_leather_chest_right_arm = root.addOrReplaceChild("leather_chest_right_arm", CubeListBuilder.create()
                .texOffs(87, 126).addBox(-4.55F, -4.4F, -3.5F, 7F, 3F, 7F, new CubeDeformation(0.05F, -0.3F, 0.1F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_leather_legs_body = root.addOrReplaceChild("leather_legs_body", CubeListBuilder.create()
                .texOffs(60, 189).addBox(-5F, 10.2F, -3F, 10F, 2F, 6F, new CubeDeformation(-0.1F, -0.4F, -0.1F))
                .texOffs(107, 267).addBox(2.1F, 10.7F, -4F, 3F, 3F, 2F, new CubeDeformation(-0.3F, -0.2F, -0.4F))
                .texOffs(203, 330).addBox(-4.5F, 10.2F, -3.5F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.2F, -0.2F)),
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
                .texOffs(208, 208).addBox(-5.5F, -3.9F, 4.6F, 11F, 6F, 1F, new CubeDeformation(-0.2F, -0.3F, -0.2F))
                .texOffs(99, 0).addBox(-5.8F, -3.9F, -4.7F, 1F, 6F, 10F, new CubeDeformation(-0.2F, -0.3F, -0.3F))
                .texOffs(122, 0).addBox(4.8F, -3.9F, -4.7F, 1F, 6F, 10F, new CubeDeformation(-0.2F, -0.3F, -0.3F))
                .texOffs(35, 285).addBox(-0.5F, -6F, -5.85F, 1F, 4F, 1F, new CubeDeformation(0F, 0F, -0.15F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_chainmail_chest_body = root.addOrReplaceChild("chainmail_chest_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_chainmail_chest_left_arm = root.addOrReplaceChild("chainmail_chest_left_arm", CubeListBuilder.create()
                .texOffs(93, 189).addBox(-2.4F, -3.7F, -3.5F, 7F, 1F, 7F, new CubeDeformation(-0.2F, 0F, 0F))
                .texOffs(189, 21).addBox(3.7F, -3.6F, -3.5F, 1F, 5F, 7F, new CubeDeformation(-0.2F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_chainmail_chest_right_arm = root.addOrReplaceChild("chainmail_chest_right_arm", CubeListBuilder.create()
                .texOffs(122, 189).addBox(-4.6F, -3.7F, -3.5F, 7F, 1F, 7F, new CubeDeformation(-0.2F, 0F, 0F))
                .texOffs(206, 21).addBox(-4.7F, -3.6F, -3.5F, 1F, 5F, 7F, new CubeDeformation(-0.2F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_chainmail_legs_body = root.addOrReplaceChild("chainmail_legs_body", CubeListBuilder.create()
                .texOffs(135, 225).addBox(-5F, 9.8F, -3.2F, 10F, 5F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(158, 225).addBox(-5F, 9.8F, 2.2F, 10F, 5F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.2F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_chainmail_legs_left_leg = root.addOrReplaceChild("chainmail_legs_left_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_chainmail_legs_right_leg = root.addOrReplaceChild("chainmail_legs_right_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_iron_head_head = root.addOrReplaceChild("iron_head_head", CubeListBuilder.create()
                .texOffs(163, 254).addBox(-1F, -6.7F, -5.9F, 2F, 5F, 1F, new CubeDeformation(-0.4F, -0.3F, -0.1F))
                .texOffs(99, 47).addBox(-1F, -10.45F, -4.5F, 2F, 2F, 9F, new CubeDeformation(-0.4F, -0.45F, -0.1F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_iron_chest_body = root.addOrReplaceChild("iron_chest_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_iron_chest_left_arm = root.addOrReplaceChild("iron_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_iron_pauldron_left_arm = p_iron_chest_left_arm.addOrReplaceChild("iron_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(58, 159).addBox(-3.2F, -1.5F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.2F, -0.1F, 0F))
                .texOffs(116, 126).addBox(-1.9F, 0.2F, -4F, 6F, 2F, 8F, new CubeDeformation(-0.1F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.2094F));
        PartDefinition p_iron_chest_right_arm = root.addOrReplaceChild("iron_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_iron_pauldron_right_arm = p_iron_chest_right_arm.addOrReplaceChild("iron_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(87, 159).addBox(-3.8F, -1.5F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.2F, -0.1F, 0F))
                .texOffs(145, 126).addBox(-4.1F, 0.2F, -4F, 6F, 2F, 8F, new CubeDeformation(-0.1F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.2094F));
        PartDefinition p_iron_legs_body = root.addOrReplaceChild("iron_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_iron_legs_left_leg = root.addOrReplaceChild("iron_legs_left_leg", CubeListBuilder.create()
                .texOffs(77, 296).addBox(-2F, 5F, -3.2F, 4F, 3F, 1F, new CubeDeformation(-0.1F, -0.1F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_iron_legs_right_leg = root.addOrReplaceChild("iron_legs_right_leg", CubeListBuilder.create()
                .texOffs(88, 296).addBox(-2F, 5F, -3.2F, 4F, 3F, 1F, new CubeDeformation(-0.1F, -0.1F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_iron_feet_left_leg = root.addOrReplaceChild("iron_feet_left_leg", CubeListBuilder.create()
                .texOffs(45, 261).addBox(-2.5F, 10.25F, -4.35F, 5F, 3F, 2F, new CubeDeformation(-0.1F, -0.35F, -0.35F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_iron_feet_right_leg = root.addOrReplaceChild("iron_feet_right_leg", CubeListBuilder.create()
                .texOffs(60, 261).addBox(-2.5F, 10.25F, -4.35F, 5F, 3F, 2F, new CubeDeformation(-0.1F, -0.35F, -0.35F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_gold_head_head = root.addOrReplaceChild("gold_head_head", CubeListBuilder.create()
                .texOffs(210, 330).addBox(-1F, -7.2F, -5.85F, 2F, 2F, 1F, new CubeDeformation(0F, 0F, -0.15F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_gold_ray_0_0 = p_gold_head_head.addOrReplaceChild("gold_ray_0_0", CubeListBuilder.create()
                .texOffs(153, 306).addBox(-1F, -1.935F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.065F, -0.35F)),
                PartPose.offsetAndRotation(0F, -9.2F, -1F, -0.384F, 0F, -0.5236F));
        PartDefinition p_gold_ray_0_1 = p_gold_ray_0_0.addOrReplaceChild("gold_ray_0_1", CubeListBuilder.create()
                .texOffs(175, 342).addBox(-0.5F, -1.85F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.15F, -0.1425F)),
                PartPose.offset(0F, -1.62F, 0F));
        PartDefinition p_gold_ray_1_0 = p_gold_head_head.addOrReplaceChild("gold_ray_1_0", CubeListBuilder.create()
                .texOffs(216, 279).addBox(-1F, -2.765F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.35F, -0.235F, -0.35F)),
                PartPose.offsetAndRotation(0F, -9.2F, -1F, -0.1745F, 0F, 0F));
        PartDefinition p_gold_ray_1_1 = p_gold_ray_1_0.addOrReplaceChild("gold_ray_1_1", CubeListBuilder.create()
                .texOffs(209, 316).addBox(-0.5F, -2.65F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.1425F, -0.35F, -0.1425F)),
                PartPose.offset(0F, -2.28F, 0F));
        PartDefinition p_gold_ray_2_0 = p_gold_head_head.addOrReplaceChild("gold_ray_2_0", CubeListBuilder.create()
                .texOffs(162, 306).addBox(-1F, -1.935F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.065F, -0.35F)),
                PartPose.offsetAndRotation(0F, -9.2F, -1F, 0.0349F, 0F, 0.5236F));
        PartDefinition p_gold_ray_2_1 = p_gold_ray_2_0.addOrReplaceChild("gold_ray_2_1", CubeListBuilder.create()
                .texOffs(180, 342).addBox(-0.5F, -1.85F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.15F, -0.1425F)),
                PartPose.offset(0F, -1.62F, 0F));
        PartDefinition p_gold_chest_body = root.addOrReplaceChild("gold_chest_body", CubeListBuilder.create()
                .texOffs(118, 267).addBox(-2F, 2.2F, -3.85F, 4F, 4F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.15F))
                .texOffs(217, 330).addBox(-1F, 3.2F, -4.35F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.3F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_gold_chest_left_arm = root.addOrReplaceChild("gold_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_gold_pauldron_left_arm = p_gold_chest_left_arm.addOrReplaceChild("gold_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(186, 82).addBox(-3.2F, -1.6F, -4F, 7F, 2F, 8F, new CubeDeformation(0F, 0F, -0.4F))
                .texOffs(217, 82).addBox(-2.4F, 0.1F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.4F, -0.2F, -0.3F))
                .texOffs(172, 321).addBox(1.1F, -2.3F, -1F, 2F, 1F, 2F, new CubeDeformation(-0.3F, -0.1F, -0.3F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.2443F));
        PartDefinition p_gold_chest_right_arm = root.addOrReplaceChild("gold_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_gold_pauldron_right_arm = p_gold_chest_right_arm.addOrReplaceChild("gold_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(0, 93).addBox(-3.8F, -1.6F, -4F, 7F, 2F, 8F, new CubeDeformation(0F, 0F, -0.4F))
                .texOffs(31, 93).addBox(-4.6F, 0.1F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.4F, -0.2F, -0.3F))
                .texOffs(181, 321).addBox(-3.1F, -2.3F, -1F, 2F, 1F, 2F, new CubeDeformation(-0.3F, -0.1F, -0.3F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.2443F));
        PartDefinition p_gold_legs_body = root.addOrReplaceChild("gold_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_gold_legs_left_leg = root.addOrReplaceChild("gold_legs_left_leg", CubeListBuilder.create()
                .texOffs(140, 233).addBox(-3F, -0.9F, -3.3F, 6F, 5F, 1F, new CubeDeformation(-0.3F, -0.3F, 0F))
                .texOffs(99, 296).addBox(-2F, 5F, -3.2F, 4F, 3F, 1F, new CubeDeformation(-0.1F, -0.1F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_gold_legs_right_leg = root.addOrReplaceChild("gold_legs_right_leg", CubeListBuilder.create()
                .texOffs(155, 233).addBox(-3F, -0.9F, -3.3F, 6F, 5F, 1F, new CubeDeformation(-0.3F, -0.3F, 0F))
                .texOffs(110, 296).addBox(-2F, 5F, -3.2F, 4F, 3F, 1F, new CubeDeformation(-0.1F, -0.1F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_gold_feet_left_leg = root.addOrReplaceChild("gold_feet_left_leg", CubeListBuilder.create()
                .texOffs(75, 261).addBox(-2.5F, 10.25F, -4.35F, 5F, 3F, 2F, new CubeDeformation(-0.1F, -0.35F, -0.35F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_gold_feet_right_leg = root.addOrReplaceChild("gold_feet_right_leg", CubeListBuilder.create()
                .texOffs(90, 261).addBox(-2.5F, 10.25F, -4.35F, 5F, 3F, 2F, new CubeDeformation(-0.1F, -0.35F, -0.35F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_diamond_head_head = root.addOrReplaceChild("diamond_head_head", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_diamond_crest_0_0 = p_diamond_head_head.addOrReplaceChild("diamond_crest_0_0", CubeListBuilder.create()
                .texOffs(171, 306).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.2F, -0.285F, -0.2F)),
                PartPose.offsetAndRotation(0F, -9.2F, -3F, -0.3491F, 0F, 0F));
        PartDefinition p_diamond_crest_0_1 = p_diamond_crest_0_0.addOrReplaceChild("diamond_crest_0_1", CubeListBuilder.create()
                .texOffs(185, 342).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.06F, -0.35F, -0.06F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_diamond_crest_1_0 = p_diamond_head_head.addOrReplaceChild("diamond_crest_1_0", CubeListBuilder.create()
                .texOffs(180, 306).addBox(-1F, -1.99F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.2F, -0.01F, -0.2F)),
                PartPose.offsetAndRotation(0F, -9.2F, -0.4F, -0.3491F, 0F, 0F));
        PartDefinition p_diamond_crest_1_1 = p_diamond_crest_1_0.addOrReplaceChild("diamond_crest_1_1", CubeListBuilder.create()
                .texOffs(190, 342).addBox(-0.5F, -1.9F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.06F, -0.1F, -0.06F)),
                PartPose.offset(0F, -1.73F, 0F));
        PartDefinition p_diamond_crest_2_0 = p_diamond_head_head.addOrReplaceChild("diamond_crest_2_0", CubeListBuilder.create()
                .texOffs(189, 306).addBox(-1F, -1.77F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.2F, -0.23F, -0.2F)),
                PartPose.offsetAndRotation(0F, -9.2F, 2.2F, -0.3491F, 0F, 0F));
        PartDefinition p_diamond_crest_2_1 = p_diamond_crest_2_0.addOrReplaceChild("diamond_crest_2_1", CubeListBuilder.create()
                .texOffs(195, 342).addBox(-0.5F, -1.7F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.06F, -0.3F, -0.06F)),
                PartPose.offset(0F, -1.29F, 0F));
        PartDefinition p_diamond_chest_body = root.addOrReplaceChild("diamond_chest_body", CubeListBuilder.create()
                .texOffs(118, 316).addBox(-1F, 1.8F, -3.9F, 2F, 3F, 1F, new CubeDeformation(0F, -0.2F, -0.1F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_diamond_chest_left_arm = root.addOrReplaceChild("diamond_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_diamond_pauldron_left_arm = p_diamond_chest_left_arm.addOrReplaceChild("diamond_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(116, 159).addBox(-3.2F, -1.6F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.2F, -0.2F, 0F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.1745F));
        PartDefinition p_diamond_shoulder_left_arm_0_0 = p_diamond_pauldron_left_arm.addOrReplaceChild("diamond_shoulder_left_arm_0_0", CubeListBuilder.create()
                .texOffs(198, 306).addBox(-1F, -1.88F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.25F, -0.12F, -0.25F)),
                PartPose.offsetAndRotation(1.8F, -1.3F, -1F, 0.1396F, 0F, 0.4189F));
        PartDefinition p_diamond_shoulder_left_arm_0_1 = p_diamond_shoulder_left_arm_0_0.addOrReplaceChild("diamond_shoulder_left_arm_0_1", CubeListBuilder.create()
                .texOffs(200, 342).addBox(-0.5F, -1.8F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.0875F, -0.2F, -0.0875F)),
                PartPose.offset(0F, -1.51F, 0F));
        PartDefinition p_diamond_shoulder_left_arm_1_0 = p_diamond_pauldron_left_arm.addOrReplaceChild("diamond_shoulder_left_arm_1_0", CubeListBuilder.create()
                .texOffs(207, 306).addBox(-1F, -1.605F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.395F, -0.45F)),
                PartPose.offsetAndRotation(2.6F, -1.3F, 1.6F, -0.1745F, 0F, 0.6632F));
        PartDefinition p_diamond_shoulder_left_arm_1_1 = p_diamond_shoulder_left_arm_1_0.addOrReplaceChild("diamond_shoulder_left_arm_1_1", CubeListBuilder.create()
                .texOffs(205, 342).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1975F, -0.45F, -0.1975F)),
                PartPose.offset(0F, -0.96F, 0F));
        PartDefinition p_diamond_chest_right_arm = root.addOrReplaceChild("diamond_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_diamond_pauldron_right_arm = p_diamond_chest_right_arm.addOrReplaceChild("diamond_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(145, 159).addBox(-3.8F, -1.6F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.2F, -0.2F, 0F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.1745F));
        PartDefinition p_diamond_shoulder_right_arm_0_0 = p_diamond_pauldron_right_arm.addOrReplaceChild("diamond_shoulder_right_arm_0_0", CubeListBuilder.create()
                .texOffs(216, 306).addBox(-1F, -1.88F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.25F, -0.12F, -0.25F)),
                PartPose.offsetAndRotation(-1.8F, -1.3F, -1F, 0.1396F, 0F, -0.4189F));
        PartDefinition p_diamond_shoulder_right_arm_0_1 = p_diamond_shoulder_right_arm_0_0.addOrReplaceChild("diamond_shoulder_right_arm_0_1", CubeListBuilder.create()
                .texOffs(210, 342).addBox(-0.5F, -1.8F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.0875F, -0.2F, -0.0875F)),
                PartPose.offset(0F, -1.51F, 0F));
        PartDefinition p_diamond_shoulder_right_arm_1_0 = p_diamond_pauldron_right_arm.addOrReplaceChild("diamond_shoulder_right_arm_1_0", CubeListBuilder.create()
                .texOffs(225, 306).addBox(-1F, -1.605F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.395F, -0.45F)),
                PartPose.offsetAndRotation(-2.6F, -1.3F, 1.6F, -0.1745F, 0F, -0.6632F));
        PartDefinition p_diamond_shoulder_right_arm_1_1 = p_diamond_shoulder_right_arm_1_0.addOrReplaceChild("diamond_shoulder_right_arm_1_1", CubeListBuilder.create()
                .texOffs(215, 342).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1975F, -0.45F, -0.1975F)),
                PartPose.offset(0F, -0.96F, 0F));
        PartDefinition p_diamond_legs_body = root.addOrReplaceChild("diamond_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_diamond_legs_left_leg = root.addOrReplaceChild("diamond_legs_left_leg", CubeListBuilder.create()
                .texOffs(224, 330).addBox(-1F, 5.4F, -3.5F, 2F, 2F, 1F, new CubeDeformation(0F, 0F, -0.1F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_diamond_legs_right_leg = root.addOrReplaceChild("diamond_legs_right_leg", CubeListBuilder.create()
                .texOffs(231, 330).addBox(-1F, 5.4F, -3.5F, 2F, 2F, 1F, new CubeDeformation(0F, 0F, -0.1F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_diamond_feet_left_leg = root.addOrReplaceChild("diamond_feet_left_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_diamond_heel_left_leg_0 = p_diamond_feet_left_leg.addOrReplaceChild("diamond_heel_left_leg_0", CubeListBuilder.create()
                .texOffs(234, 306).addBox(-1F, -1.605F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.395F, -0.4F)),
                PartPose.offsetAndRotation(0F, 10.9F, 2.8F, -0.9774F, 0F, 0F));
        PartDefinition p_diamond_heel_left_leg_1 = p_diamond_heel_left_leg_0.addOrReplaceChild("diamond_heel_left_leg_1", CubeListBuilder.create()
                .texOffs(220, 342).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.17F, -0.45F, -0.17F)),
                PartPose.offset(0F, -0.96F, 0F));
        PartDefinition p_diamond_feet_right_leg = root.addOrReplaceChild("diamond_feet_right_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_diamond_heel_right_leg_0 = p_diamond_feet_right_leg.addOrReplaceChild("diamond_heel_right_leg_0", CubeListBuilder.create()
                .texOffs(243, 306).addBox(-1F, -1.605F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.395F, -0.4F)),
                PartPose.offsetAndRotation(0F, 10.9F, 2.8F, -0.9774F, 0F, 0F));
        PartDefinition p_diamond_heel_right_leg_1 = p_diamond_heel_right_leg_0.addOrReplaceChild("diamond_heel_right_leg_1", CubeListBuilder.create()
                .texOffs(225, 342).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.17F, -0.45F, -0.17F)),
                PartPose.offset(0F, -0.96F, 0F));
        PartDefinition p_netherite_head_head = root.addOrReplaceChild("netherite_head_head", CubeListBuilder.create()
                .texOffs(195, 346).addBox(-5F, -6.3F, -5.85F, 10F, 1F, 1F, new CubeDeformation(-0.4F, -0.1F, -0.15F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_netherite_horn_left_0 = p_netherite_head_head.addOrReplaceChild("netherite_horn_left_0", CubeListBuilder.create()
                .texOffs(0, 311).addBox(-1F, -2F, -1F, 2F, 2F, 2F),
                PartPose.offsetAndRotation(4.4F, -7.6F, -2.6F, 0F, 0F, 1.2217F));
        PartDefinition p_netherite_horn_left_1 = p_netherite_horn_left_0.addOrReplaceChild("netherite_horn_left_1", CubeListBuilder.create()
                .texOffs(9, 311).addBox(-1F, -2F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, -1.75F, 0F, 0.5236F, 0F, -0.5236F));
        PartDefinition p_netherite_horn_left_2 = p_netherite_horn_left_1.addOrReplaceChild("netherite_horn_left_2", CubeListBuilder.create()
                .texOffs(18, 311).addBox(-1F, -1.9F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.1F, -0.45F)),
                PartPose.offsetAndRotation(0F, -1.75F, 0F, 0.5934F, 0F, -0.4189F));
        PartDefinition p_netherite_horn_right_0 = p_netherite_head_head.addOrReplaceChild("netherite_horn_right_0", CubeListBuilder.create()
                .texOffs(27, 311).addBox(-1F, -2F, -1F, 2F, 2F, 2F),
                PartPose.offsetAndRotation(-4.4F, -7.6F, -2.6F, 0F, 0F, -1.2217F));
        PartDefinition p_netherite_horn_right_1 = p_netherite_horn_right_0.addOrReplaceChild("netherite_horn_right_1", CubeListBuilder.create()
                .texOffs(36, 311).addBox(-1F, -2F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, -1.75F, 0F, 0.5236F, 0F, 0.5236F));
        PartDefinition p_netherite_horn_right_2 = p_netherite_horn_right_1.addOrReplaceChild("netherite_horn_right_2", CubeListBuilder.create()
                .texOffs(45, 311).addBox(-1F, -1.9F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.1F, -0.45F)),
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
                .texOffs(54, 311).addBox(-1F, -1.66F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.34F, -0.4F)),
                PartPose.offsetAndRotation(2F, -1.8F, -1.6F, 0F, 0F, 0.5236F));
        PartDefinition p_netherite_spike_left_arm_0_1 = p_netherite_spike_left_arm_0_0.addOrReplaceChild("netherite_spike_left_arm_0_1", CubeListBuilder.create()
                .texOffs(230, 342).addBox(-0.5F, -1.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.17F, -0.4F, -0.17F)),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_netherite_spike_left_arm_1_0 = p_netherite_pauldron_left_arm.addOrReplaceChild("netherite_spike_left_arm_1_0", CubeListBuilder.create()
                .texOffs(63, 311).addBox(-1F, -1.66F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.34F, -0.4F)),
                PartPose.offsetAndRotation(2F, -1.8F, 1.6F, 0F, 0F, 0.5236F));
        PartDefinition p_netherite_spike_left_arm_1_1 = p_netherite_spike_left_arm_1_0.addOrReplaceChild("netherite_spike_left_arm_1_1", CubeListBuilder.create()
                .texOffs(235, 342).addBox(-0.5F, -1.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.17F, -0.4F, -0.17F)),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_netherite_chest_right_arm = root.addOrReplaceChild("netherite_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_netherite_pauldron_right_arm = p_netherite_chest_right_arm.addOrReplaceChild("netherite_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(66, 47).addBox(-4.3F, -2.25F, -4F, 8F, 3F, 8F, new CubeDeformation(-0.3F, -0.35F, -0.2F))
                .texOffs(93, 93).addBox(-4.6F, 0.35F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.2F, -0.05F, -0.1F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.2618F));
        PartDefinition p_netherite_spike_right_arm_0_0 = p_netherite_pauldron_right_arm.addOrReplaceChild("netherite_spike_right_arm_0_0", CubeListBuilder.create()
                .texOffs(72, 311).addBox(-1F, -1.66F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.34F, -0.4F)),
                PartPose.offsetAndRotation(-2F, -1.8F, -1.6F, 0F, 0F, -0.5236F));
        PartDefinition p_netherite_spike_right_arm_0_1 = p_netherite_spike_right_arm_0_0.addOrReplaceChild("netherite_spike_right_arm_0_1", CubeListBuilder.create()
                .texOffs(240, 342).addBox(-0.5F, -1.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.17F, -0.4F, -0.17F)),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_netherite_spike_right_arm_1_0 = p_netherite_pauldron_right_arm.addOrReplaceChild("netherite_spike_right_arm_1_0", CubeListBuilder.create()
                .texOffs(81, 311).addBox(-1F, -1.66F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.34F, -0.4F)),
                PartPose.offsetAndRotation(-2F, -1.8F, 1.6F, 0F, 0F, -0.5236F));
        PartDefinition p_netherite_spike_right_arm_1_1 = p_netherite_spike_right_arm_1_0.addOrReplaceChild("netherite_spike_right_arm_1_1", CubeListBuilder.create()
                .texOffs(245, 342).addBox(-0.5F, -1.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.17F, -0.4F, -0.17F)),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_netherite_legs_body = root.addOrReplaceChild("netherite_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_netherite_legs_left_leg = root.addOrReplaceChild("netherite_legs_left_leg", CubeListBuilder.create()
                .texOffs(170, 233).addBox(-3F, -0.9F, -3.3F, 6F, 5F, 1F, new CubeDeformation(-0.3F, -0.3F, 0F))
                .texOffs(121, 296).addBox(-2F, 5F, -3.2F, 4F, 3F, 1F, new CubeDeformation(-0.1F, -0.1F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_netherite_legs_right_leg = root.addOrReplaceChild("netherite_legs_right_leg", CubeListBuilder.create()
                .texOffs(185, 233).addBox(-3F, -0.9F, -3.3F, 6F, 5F, 1F, new CubeDeformation(-0.3F, -0.3F, 0F))
                .texOffs(132, 296).addBox(-2F, 5F, -3.2F, 4F, 3F, 1F, new CubeDeformation(-0.1F, -0.1F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_netherite_feet_left_leg = root.addOrReplaceChild("netherite_feet_left_leg", CubeListBuilder.create()
                .texOffs(105, 261).addBox(-2.5F, 10.15F, -4.45F, 5F, 3F, 2F, new CubeDeformation(0F, -0.25F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_netherite_toe_left_leg_0 = p_netherite_feet_left_leg.addOrReplaceChild("netherite_toe_left_leg_0", CubeListBuilder.create()
                .texOffs(35, 353).addBox(-0.5F, -0.94F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.06F, 0F)),
                PartPose.offsetAndRotation(0F, 11.2F, -4F, 1.0821F, 0F, 0F));
        PartDefinition p_netherite_toe_left_leg_1 = p_netherite_toe_left_leg_0.addOrReplaceChild("netherite_toe_left_leg_1", CubeListBuilder.create()
                .texOffs(40, 353).addBox(-0.5F, -0.9F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.1F, -0.225F)),
                PartPose.offset(0F, -0.63F, 0F));
        PartDefinition p_netherite_feet_right_leg = root.addOrReplaceChild("netherite_feet_right_leg", CubeListBuilder.create()
                .texOffs(120, 261).addBox(-2.5F, 10.15F, -4.45F, 5F, 3F, 2F, new CubeDeformation(0F, -0.25F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_netherite_toe_right_leg_0 = p_netherite_feet_right_leg.addOrReplaceChild("netherite_toe_right_leg_0", CubeListBuilder.create()
                .texOffs(45, 353).addBox(-0.5F, -0.94F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.06F, 0F)),
                PartPose.offsetAndRotation(0F, 11.2F, -4F, 1.0821F, 0F, 0F));
        PartDefinition p_netherite_toe_right_leg_1 = p_netherite_toe_right_leg_0.addOrReplaceChild("netherite_toe_right_leg_1", CubeListBuilder.create()
                .texOffs(50, 353).addBox(-0.5F, -0.9F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.1F, -0.225F)),
                PartPose.offset(0F, -0.63F, 0F));
        PartDefinition p_copper_head_head = root.addOrReplaceChild("copper_head_head", CubeListBuilder.create()
                .texOffs(63, 233).addBox(-2F, -10.4F, -2F, 4F, 2F, 4F, new CubeDeformation(-0.4F, -0.4F, -0.4F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_copper_spike_0 = p_copper_head_head.addOrReplaceChild("copper_spike_0", CubeListBuilder.create()
                .texOffs(90, 311).addBox(-1F, -2F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offset(0F, -9.8F, 0F));
        PartDefinition p_copper_spike_1 = p_copper_spike_0.addOrReplaceChild("copper_spike_1", CubeListBuilder.create()
                .texOffs(250, 342).addBox(-0.5F, -2F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1F, 0F, -0.1F)),
                PartPose.offset(0F, -1.75F, 0F));
        PartDefinition p_copper_chest_body = root.addOrReplaceChild("copper_chest_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_copper_chest_left_arm = root.addOrReplaceChild("copper_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_copper_pauldron_left_arm = p_copper_chest_left_arm.addOrReplaceChild("copper_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(174, 159).addBox(-3.2F, -1.55F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.2F, -0.05F, 0F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.2094F));
        PartDefinition p_copper_chest_right_arm = root.addOrReplaceChild("copper_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_copper_pauldron_right_arm = p_copper_chest_right_arm.addOrReplaceChild("copper_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(203, 159).addBox(-3.8F, -1.55F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.2F, -0.05F, 0F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.2094F));
        PartDefinition p_copper_legs_body = root.addOrReplaceChild("copper_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_copper_legs_left_leg = root.addOrReplaceChild("copper_legs_left_leg", CubeListBuilder.create()
                .texOffs(143, 296).addBox(-2F, 5F, -3.2F, 4F, 3F, 1F, new CubeDeformation(-0.1F, -0.1F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_copper_legs_right_leg = root.addOrReplaceChild("copper_legs_right_leg", CubeListBuilder.create()
                .texOffs(154, 296).addBox(-2F, 5F, -3.2F, 4F, 3F, 1F, new CubeDeformation(-0.1F, -0.1F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_copper_feet_left_leg = root.addOrReplaceChild("copper_feet_left_leg", CubeListBuilder.create()
                .texOffs(135, 261).addBox(-2.5F, 10.25F, -4.35F, 5F, 3F, 2F, new CubeDeformation(-0.1F, -0.35F, -0.35F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_copper_feet_right_leg = root.addOrReplaceChild("copper_feet_right_leg", CubeListBuilder.create()
                .texOffs(150, 261).addBox(-2.5F, 10.25F, -4.35F, 5F, 3F, 2F, new CubeDeformation(-0.1F, -0.35F, -0.35F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_turtle_head_head = root.addOrReplaceChild("turtle_head_head", CubeListBuilder.create()
                .texOffs(0, 21).addBox(-6F, -9.9F, -6F, 12F, 1F, 12F, new CubeDeformation(-0.4F, 0F, -0.4F))
                .texOffs(86, 59).addBox(-4.5F, -10.9F, -4.5F, 9F, 1F, 9F, new CubeDeformation(-0.1F, 0F, -0.1F))
                .texOffs(0, 217).addBox(-3F, -11.7F, -3F, 6F, 1F, 6F, new CubeDeformation(-0.2F, -0.1F, -0.2F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_moonstone_head_head = root.addOrReplaceChild("moonstone_head_head", CubeListBuilder.create()
                .texOffs(145, 346).addBox(-5.5F, -7.7F, -5.65F, 11F, 1F, 1F, new CubeDeformation(-0.05F, 0F, -0.2F))
                .texOffs(170, 346).addBox(-5.5F, -7.7F, 4.65F, 11F, 1F, 1F, new CubeDeformation(-0.05F, 0F, -0.2F))
                .texOffs(122, 47).addBox(-5.65F, -7.7F, -5F, 1F, 1F, 10F, new CubeDeformation(-0.2F, 0F, -0.15F))
                .texOffs(145, 47).addBox(4.65F, -7.7F, -5F, 1F, 1F, 10F, new CubeDeformation(-0.2F, 0F, -0.15F))
                .texOffs(99, 311).addBox(-1.5F, -8.8F, -6.05F, 3F, 3F, 1F, new CubeDeformation(-0.2F, -0.3F, -0.3F))
                .texOffs(238, 330).addBox(-1F, -8.3F, -6.35F, 2F, 2F, 1F, new CubeDeformation(-0.05F, -0.1F, -0.2F))
                .texOffs(125, 316).addBox(-6.175F, -8.2F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.225F, -0.3F, -0.3F))
                .texOffs(132, 316).addBox(5.175F, -8.2F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.225F, -0.3F, -0.3F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_moon_diadem = p_moonstone_head_head.addOrReplaceChild("moon_diadem", CubeListBuilder.create()
                .texOffs(143, 321).addBox(-2F, -1.5625F, -0.5F, 4F, 2F, 1F, new CubeDeformation(-0.375F, -0.4375F, -0.15F)),
                PartPose.offsetAndRotation(0F, -8.6F, -5.75F, -0.1396F, 0F, 0F));
        PartDefinition p_moon_diadem_left_0 = p_moon_diadem.addOrReplaceChild("moon_diadem_left_0", CubeListBuilder.create()
                .texOffs(245, 330).addBox(-1F, -1.9375F, -0.5F, 2F, 2F, 1F, new CubeDeformation(-0.4062F, -0.0625F, -0.175F)),
                PartPose.offsetAndRotation(1.3125F, -0.5625F, 0F, 0F, 0F, 1.0123F));
        PartDefinition p_moon_diadem_left_1 = p_moon_diadem_left_0.addOrReplaceChild("moon_diadem_left_1", CubeListBuilder.create()
                .texOffs(0, 346).addBox(-0.5F, -1.8438F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.0312F, -0.1562F, -0.2F)),
                PartPose.offsetAndRotation(0F, -1.625F, 0F, 0F, 0F, -0.6981F));
        PartDefinition p_moon_diadem_left_2 = p_moon_diadem_left_1.addOrReplaceChild("moon_diadem_left_2", CubeListBuilder.create()
                .texOffs(5, 346).addBox(-0.5F, -1.7188F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1875F, -0.2812F, -0.25F)),
                PartPose.offsetAndRotation(0F, -1.4375F, 0F, 0F, 0F, -0.6283F));
        PartDefinition p_moon_diadem_right_0 = p_moon_diadem.addOrReplaceChild("moon_diadem_right_0", CubeListBuilder.create()
                .texOffs(0, 334).addBox(-1F, -1.9375F, -0.5F, 2F, 2F, 1F, new CubeDeformation(-0.4062F, -0.0625F, -0.175F)),
                PartPose.offsetAndRotation(-1.3125F, -0.5625F, 0F, 0F, 0F, -1.0123F));
        PartDefinition p_moon_diadem_right_1 = p_moon_diadem_right_0.addOrReplaceChild("moon_diadem_right_1", CubeListBuilder.create()
                .texOffs(10, 346).addBox(-0.5F, -1.8438F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.0312F, -0.1562F, -0.2F)),
                PartPose.offsetAndRotation(0F, -1.625F, 0F, 0F, 0F, 0.6981F));
        PartDefinition p_moon_diadem_right_2 = p_moon_diadem_right_1.addOrReplaceChild("moon_diadem_right_2", CubeListBuilder.create()
                .texOffs(15, 346).addBox(-0.5F, -1.7188F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1875F, -0.2812F, -0.25F)),
                PartPose.offsetAndRotation(0F, -1.4375F, 0F, 0F, 0F, 0.6283F));
        PartDefinition p_moonstone_chest_body = root.addOrReplaceChild("moonstone_chest_body", CubeListBuilder.create()
                .texOffs(7, 334).addBox(-4.6F, -0.9F, -3.75F, 2F, 2F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.15F))
                .texOffs(14, 334).addBox(2.6F, -0.9F, -3.75F, 2F, 2F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.15F))
                .texOffs(129, 267).addBox(-2F, 1.2F, -3.7F, 4F, 4F, 1F, new CubeDeformation(-0.3F, -0.2F, -0.25F))
                .texOffs(108, 311).addBox(-1.5F, 1.7F, -4.05F, 3F, 3F, 1F, new CubeDeformation(-0.35F, -0.2F, -0.2F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_moon_cape = p_moonstone_chest_body.addOrReplaceChild("moon_cape", CubeListBuilder.create()
                .texOffs(232, 137).addBox(-5F, -0.2F, -0.2F, 10F, 9F, 1F, new CubeDeformation(-0.3F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(0F, -0.2F, 3.1F, 0.0873F, 0F, 0F));
        PartDefinition p_moon_cape_hem = p_moon_cape.addOrReplaceChild("moon_cape_hem", CubeListBuilder.create()
                .texOffs(0, 148).addBox(-5F, -0.3F, -0.2F, 10F, 9F, 1F, new CubeDeformation(-0.4F, -0.3F, -0.2F)),
                PartPose.offsetAndRotation(0F, 8.4F, 0.05F, 0.1396F, 0F, 0F));
        PartDefinition p_moonstone_chest_left_arm = root.addOrReplaceChild("moonstone_chest_left_arm", CubeListBuilder.create()
                .texOffs(174, 126).addBox(-2.5F, 5.15F, -3.5F, 7F, 3F, 7F, new CubeDeformation(-0.25F, -0.35F, -0.25F))
                .texOffs(21, 334).addBox(0F, 5.65F, -3.95F, 2F, 2F, 1F, new CubeDeformation(-0.4F, -0.35F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_moon_pauldron_left_arm = p_moonstone_chest_left_arm.addOrReplaceChild("moon_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(124, 93).addBox(-3.3F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.2F, -0.15F, -0.4F))
                .texOffs(203, 126).addBox(-2.1F, -0.15F, -4F, 6F, 2F, 8F, new CubeDeformation(-0.1F, -0.35F, -0.3F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.2094F));
        PartDefinition p_moon_shoulder_left_arm = p_moon_pauldron_left_arm.addOrReplaceChild("moon_shoulder_left_arm", CubeListBuilder.create()
                .texOffs(0, 350).addBox(-1.5F, -0.95F, -0.5F, 3F, 1F, 1F, new CubeDeformation(-0.2F, -0.05F, -0.15F)),
                PartPose.offsetAndRotation(1.4F, -1.4F, 0F, 0F, 1.5708F, 0F));
        PartDefinition p_moon_shoulder_left_arm_left_0 = p_moon_shoulder_left_arm.addOrReplaceChild("moon_shoulder_left_arm_left_0", CubeListBuilder.create()
                .texOffs(20, 346).addBox(-0.5F, -1.75F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.025F, -0.25F, -0.175F)),
                PartPose.offsetAndRotation(1.05F, -0.45F, 0F, 0F, 0F, 1.0123F));
        PartDefinition p_moon_shoulder_left_arm_left_1 = p_moon_shoulder_left_arm_left_0.addOrReplaceChild("moon_shoulder_left_arm_left_1", CubeListBuilder.create()
                .texOffs(25, 346).addBox(-0.5F, -1.675F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.125F, -0.325F, -0.2F)),
                PartPose.offsetAndRotation(0F, -1.25F, 0F, 0F, 0F, -0.6981F));
        PartDefinition p_moon_shoulder_left_arm_left_2 = p_moon_shoulder_left_arm_left_1.addOrReplaceChild("moon_shoulder_left_arm_left_2", CubeListBuilder.create()
                .texOffs(30, 346).addBox(-0.5F, -1.575F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.25F, -0.425F, -0.25F)),
                PartPose.offsetAndRotation(0F, -1.1F, 0F, 0F, 0F, -0.6283F));
        PartDefinition p_moon_shoulder_left_arm_right_0 = p_moon_shoulder_left_arm.addOrReplaceChild("moon_shoulder_left_arm_right_0", CubeListBuilder.create()
                .texOffs(35, 346).addBox(-0.5F, -1.75F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.025F, -0.25F, -0.175F)),
                PartPose.offsetAndRotation(-1.05F, -0.45F, 0F, 0F, 0F, -1.0123F));
        PartDefinition p_moon_shoulder_left_arm_right_1 = p_moon_shoulder_left_arm_right_0.addOrReplaceChild("moon_shoulder_left_arm_right_1", CubeListBuilder.create()
                .texOffs(40, 346).addBox(-0.5F, -1.675F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.125F, -0.325F, -0.2F)),
                PartPose.offsetAndRotation(0F, -1.25F, 0F, 0F, 0F, 0.6981F));
        PartDefinition p_moon_shoulder_left_arm_right_2 = p_moon_shoulder_left_arm_right_1.addOrReplaceChild("moon_shoulder_left_arm_right_2", CubeListBuilder.create()
                .texOffs(45, 346).addBox(-0.5F, -1.575F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.25F, -0.425F, -0.25F)),
                PartPose.offsetAndRotation(0F, -1.1F, 0F, 0F, 0F, 0.6283F));
        PartDefinition p_moonstone_chest_right_arm = root.addOrReplaceChild("moonstone_chest_right_arm", CubeListBuilder.create()
                .texOffs(0, 137).addBox(-4.5F, 5.15F, -3.5F, 7F, 3F, 7F, new CubeDeformation(-0.25F, -0.35F, -0.25F))
                .texOffs(28, 334).addBox(-2F, 5.65F, -3.95F, 2F, 2F, 1F, new CubeDeformation(-0.4F, -0.35F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_moon_pauldron_right_arm = p_moonstone_chest_right_arm.addOrReplaceChild("moon_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(155, 93).addBox(-3.7F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.2F, -0.15F, -0.4F))
                .texOffs(29, 137).addBox(-3.9F, -0.15F, -4F, 6F, 2F, 8F, new CubeDeformation(-0.1F, -0.35F, -0.3F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.2094F));
        PartDefinition p_moon_shoulder_right_arm = p_moon_pauldron_right_arm.addOrReplaceChild("moon_shoulder_right_arm", CubeListBuilder.create()
                .texOffs(9, 350).addBox(-1.5F, -0.95F, -0.5F, 3F, 1F, 1F, new CubeDeformation(-0.2F, -0.05F, -0.15F)),
                PartPose.offsetAndRotation(-1.4F, -1.4F, 0F, 0F, -1.5708F, 0F));
        PartDefinition p_moon_shoulder_right_arm_left_0 = p_moon_shoulder_right_arm.addOrReplaceChild("moon_shoulder_right_arm_left_0", CubeListBuilder.create()
                .texOffs(50, 346).addBox(-0.5F, -1.75F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.025F, -0.25F, -0.175F)),
                PartPose.offsetAndRotation(1.05F, -0.45F, 0F, 0F, 0F, 1.0123F));
        PartDefinition p_moon_shoulder_right_arm_left_1 = p_moon_shoulder_right_arm_left_0.addOrReplaceChild("moon_shoulder_right_arm_left_1", CubeListBuilder.create()
                .texOffs(55, 346).addBox(-0.5F, -1.675F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.125F, -0.325F, -0.2F)),
                PartPose.offsetAndRotation(0F, -1.25F, 0F, 0F, 0F, -0.6981F));
        PartDefinition p_moon_shoulder_right_arm_left_2 = p_moon_shoulder_right_arm_left_1.addOrReplaceChild("moon_shoulder_right_arm_left_2", CubeListBuilder.create()
                .texOffs(60, 346).addBox(-0.5F, -1.575F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.25F, -0.425F, -0.25F)),
                PartPose.offsetAndRotation(0F, -1.1F, 0F, 0F, 0F, -0.6283F));
        PartDefinition p_moon_shoulder_right_arm_right_0 = p_moon_shoulder_right_arm.addOrReplaceChild("moon_shoulder_right_arm_right_0", CubeListBuilder.create()
                .texOffs(65, 346).addBox(-0.5F, -1.75F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.025F, -0.25F, -0.175F)),
                PartPose.offsetAndRotation(-1.05F, -0.45F, 0F, 0F, 0F, -1.0123F));
        PartDefinition p_moon_shoulder_right_arm_right_1 = p_moon_shoulder_right_arm_right_0.addOrReplaceChild("moon_shoulder_right_arm_right_1", CubeListBuilder.create()
                .texOffs(70, 346).addBox(-0.5F, -1.675F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.125F, -0.325F, -0.2F)),
                PartPose.offsetAndRotation(0F, -1.25F, 0F, 0F, 0F, 0.6981F));
        PartDefinition p_moon_shoulder_right_arm_right_2 = p_moon_shoulder_right_arm_right_1.addOrReplaceChild("moon_shoulder_right_arm_right_2", CubeListBuilder.create()
                .texOffs(75, 346).addBox(-0.5F, -1.575F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.25F, -0.425F, -0.25F)),
                PartPose.offsetAndRotation(0F, -1.1F, 0F, 0F, 0F, 0.6283F));
        PartDefinition p_moonstone_legs_body = root.addOrReplaceChild("moonstone_legs_body", CubeListBuilder.create()
                .texOffs(35, 334).addBox(-1F, 10.15F, -3.8F, 2F, 2F, 1F, new CubeDeformation(-0.4F, -0.45F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_moon_buckle = p_moonstone_legs_body.addOrReplaceChild("moon_buckle", CubeListBuilder.create()
                .texOffs(34, 350).addBox(-1F, -0.8375F, -0.5F, 2F, 1F, 1F, new CubeDeformation(-0.025F, -0.1625F, -0.15F)),
                PartPose.offset(0F, 11.5F, -3.3F));
        PartDefinition p_moon_buckle_left_0 = p_moon_buckle.addOrReplaceChild("moon_buckle_left_0", CubeListBuilder.create()
                .texOffs(80, 346).addBox(-0.5F, -1.5625F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1438F, -0.4375F, -0.175F)),
                PartPose.offsetAndRotation(0.7875F, -0.3375F, 0F, 0F, 0F, 1.0123F));
        PartDefinition p_moon_buckle_left_1 = p_moon_buckle_left_0.addOrReplaceChild("moon_buckle_left_1", CubeListBuilder.create()
                .texOffs(85, 346).addBox(-0.5F, -1.5063F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.2188F, -0.4937F, -0.2F)),
                PartPose.offsetAndRotation(0F, -0.875F, 0F, 0F, 0F, -0.6981F));
        PartDefinition p_moon_buckle_left_2 = p_moon_buckle_left_1.addOrReplaceChild("moon_buckle_left_2", CubeListBuilder.create()
                .texOffs(55, 353).addBox(-0.5F, -0.9312F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.3125F, -0.0688F, -0.25F)),
                PartPose.offsetAndRotation(0F, -0.7625F, 0F, 0F, 0F, -0.6283F));
        PartDefinition p_moon_buckle_right_0 = p_moon_buckle.addOrReplaceChild("moon_buckle_right_0", CubeListBuilder.create()
                .texOffs(90, 346).addBox(-0.5F, -1.5625F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1438F, -0.4375F, -0.175F)),
                PartPose.offsetAndRotation(-0.7875F, -0.3375F, 0F, 0F, 0F, -1.0123F));
        PartDefinition p_moon_buckle_right_1 = p_moon_buckle_right_0.addOrReplaceChild("moon_buckle_right_1", CubeListBuilder.create()
                .texOffs(95, 346).addBox(-0.5F, -1.5063F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.2188F, -0.4937F, -0.2F)),
                PartPose.offsetAndRotation(0F, -0.875F, 0F, 0F, 0F, 0.6981F));
        PartDefinition p_moon_buckle_right_2 = p_moon_buckle_right_1.addOrReplaceChild("moon_buckle_right_2", CubeListBuilder.create()
                .texOffs(60, 353).addBox(-0.5F, -0.9312F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.3125F, -0.0688F, -0.25F)),
                PartPose.offsetAndRotation(0F, -0.7625F, 0F, 0F, 0F, 0.6283F));
        PartDefinition p_moonstone_legs_left_leg = root.addOrReplaceChild("moonstone_legs_left_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_moon_robe_left_leg = p_moonstone_legs_left_leg.addOrReplaceChild("moon_robe_left_leg", CubeListBuilder.create()
                .texOffs(34, 189).addBox(-2.5F, -0.1F, -0.55F, 5F, 8F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.25F)),
                PartPose.offsetAndRotation(0F, -0.6F, -2.75F, -0.1047F, 0F, 0F));
        PartDefinition p_moon_robe_side_left_leg = p_moonstone_legs_left_leg.addOrReplaceChild("moon_robe_side_left_leg", CubeListBuilder.create()
                .texOffs(94, 21).addBox(-0.25F, -0.1F, -2.5F, 1F, 8F, 5F, new CubeDeformation(-0.25F, -0.1F, -0.1F)),
                PartPose.offsetAndRotation(2.75F, -0.6F, 0F, 0F, 0F, -0.1047F));
        PartDefinition p_moonstone_legs_right_leg = root.addOrReplaceChild("moonstone_legs_right_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_moon_robe_right_leg = p_moonstone_legs_right_leg.addOrReplaceChild("moon_robe_right_leg", CubeListBuilder.create()
                .texOffs(47, 189).addBox(-2.5F, -0.1F, -0.55F, 5F, 8F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.25F)),
                PartPose.offsetAndRotation(0F, -0.6F, -2.75F, -0.1047F, 0F, 0F));
        PartDefinition p_moon_robe_side_right_leg = p_moonstone_legs_right_leg.addOrReplaceChild("moon_robe_side_right_leg", CubeListBuilder.create()
                .texOffs(107, 21).addBox(-0.75F, -0.1F, -2.5F, 1F, 8F, 5F, new CubeDeformation(-0.25F, -0.1F, -0.1F)),
                PartPose.offsetAndRotation(-2.75F, -0.6F, 0F, 0F, 0F, 0.1047F));
        PartDefinition p_moonstone_feet_left_leg = root.addOrReplaceChild("moonstone_feet_left_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_moon_toe_left_leg_0 = p_moonstone_feet_left_leg.addOrReplaceChild("moon_toe_left_leg_0", CubeListBuilder.create()
                .texOffs(143, 285).addBox(-2F, -1.85F, -1F, 4F, 2F, 2F, new CubeDeformation(-0.3F, -0.15F, -0.3F)),
                PartPose.offsetAndRotation(0F, 12F, -2.3F, 1.5708F, 0F, 0F));
        PartDefinition p_moon_toe_left_leg_1 = p_moon_toe_left_leg_0.addOrReplaceChild("moon_toe_left_leg_1", CubeListBuilder.create()
                .texOffs(165, 296).addBox(-1.5F, -1.75F, -1F, 3F, 2F, 2F, new CubeDeformation(-0.3F, -0.25F, -0.45F)),
                PartPose.offsetAndRotation(0F, -1.45F, 0F, -0.4887F, 0F, 0F));
        PartDefinition p_moon_toe_left_leg_2 = p_moon_toe_left_leg_1.addOrReplaceChild("moon_toe_left_leg_2", CubeListBuilder.create()
                .texOffs(42, 334).addBox(-1F, -1.65F, -0.5F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.35F, -0.1F)),
                PartPose.offsetAndRotation(0F, -1.25F, 0F, -0.733F, 0F, 0F));
        PartDefinition p_moon_toe_left_leg_3 = p_moon_toe_left_leg_2.addOrReplaceChild("moon_toe_left_leg_3", CubeListBuilder.create()
                .texOffs(65, 353).addBox(-0.5F, -1F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.15F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, -1.05F, 0F, -0.8029F, 0F, 0F));
        PartDefinition p_moon_ankle_left_leg = p_moonstone_feet_left_leg.addOrReplaceChild("moon_ankle_left_leg", CubeListBuilder.create()
                .texOffs(41, 350).addBox(-1F, -0.779F, -0.5F, 2F, 1F, 1F, new CubeDeformation(-0.194F, -0.221F, -0.15F)),
                PartPose.offsetAndRotation(3.35F, 9.8F, 0.4F, 0F, 1.5708F, 0F));
        PartDefinition p_moon_ankle_left_leg_left_0 = p_moon_ankle_left_leg.addOrReplaceChild("moon_ankle_left_leg_left_0", CubeListBuilder.create()
                .texOffs(70, 353).addBox(-0.5F, -0.965F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.2055F, -0.035F, -0.175F)),
                PartPose.offsetAndRotation(0.651F, -0.279F, 0F, 0F, 0F, 1.0123F));
        PartDefinition p_moon_ankle_left_leg_left_1 = p_moon_ankle_left_leg_left_0.addOrReplaceChild("moon_ankle_left_leg_left_1", CubeListBuilder.create()
                .texOffs(75, 353).addBox(-0.5F, -0.9185F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.2675F, -0.0815F, -0.2F)),
                PartPose.offsetAndRotation(0F, -0.68F, 0F, 0F, 0F, -0.6981F));
        PartDefinition p_moon_ankle_left_leg_left_2 = p_moon_ankle_left_leg_left_1.addOrReplaceChild("moon_ankle_left_leg_left_2", CubeListBuilder.create()
                .texOffs(80, 353).addBox(-0.5F, -0.8565F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.345F, -0.1435F, -0.25F)),
                PartPose.offsetAndRotation(0F, -0.587F, 0F, 0F, 0F, -0.6283F));
        PartDefinition p_moon_ankle_left_leg_right_0 = p_moon_ankle_left_leg.addOrReplaceChild("moon_ankle_left_leg_right_0", CubeListBuilder.create()
                .texOffs(85, 353).addBox(-0.5F, -0.965F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.2055F, -0.035F, -0.175F)),
                PartPose.offsetAndRotation(-0.651F, -0.279F, 0F, 0F, 0F, -1.0123F));
        PartDefinition p_moon_ankle_left_leg_right_1 = p_moon_ankle_left_leg_right_0.addOrReplaceChild("moon_ankle_left_leg_right_1", CubeListBuilder.create()
                .texOffs(90, 353).addBox(-0.5F, -0.9185F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.2675F, -0.0815F, -0.2F)),
                PartPose.offsetAndRotation(0F, -0.68F, 0F, 0F, 0F, 0.6981F));
        PartDefinition p_moon_ankle_left_leg_right_2 = p_moon_ankle_left_leg_right_1.addOrReplaceChild("moon_ankle_left_leg_right_2", CubeListBuilder.create()
                .texOffs(95, 353).addBox(-0.5F, -0.8565F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.345F, -0.1435F, -0.25F)),
                PartPose.offsetAndRotation(0F, -0.587F, 0F, 0F, 0F, 0.6283F));
        PartDefinition p_moonstone_feet_right_leg = root.addOrReplaceChild("moonstone_feet_right_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_moon_toe_right_leg_0 = p_moonstone_feet_right_leg.addOrReplaceChild("moon_toe_right_leg_0", CubeListBuilder.create()
                .texOffs(156, 285).addBox(-2F, -1.85F, -1F, 4F, 2F, 2F, new CubeDeformation(-0.3F, -0.15F, -0.3F)),
                PartPose.offsetAndRotation(0F, 12F, -2.3F, 1.5708F, 0F, 0F));
        PartDefinition p_moon_toe_right_leg_1 = p_moon_toe_right_leg_0.addOrReplaceChild("moon_toe_right_leg_1", CubeListBuilder.create()
                .texOffs(176, 296).addBox(-1.5F, -1.75F, -1F, 3F, 2F, 2F, new CubeDeformation(-0.3F, -0.25F, -0.45F)),
                PartPose.offsetAndRotation(0F, -1.45F, 0F, -0.4887F, 0F, 0F));
        PartDefinition p_moon_toe_right_leg_2 = p_moon_toe_right_leg_1.addOrReplaceChild("moon_toe_right_leg_2", CubeListBuilder.create()
                .texOffs(49, 334).addBox(-1F, -1.65F, -0.5F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.35F, -0.1F)),
                PartPose.offsetAndRotation(0F, -1.25F, 0F, -0.733F, 0F, 0F));
        PartDefinition p_moon_toe_right_leg_3 = p_moon_toe_right_leg_2.addOrReplaceChild("moon_toe_right_leg_3", CubeListBuilder.create()
                .texOffs(100, 353).addBox(-0.5F, -1F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.15F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, -1.05F, 0F, -0.8029F, 0F, 0F));
        PartDefinition p_moon_ankle_right_leg = p_moonstone_feet_right_leg.addOrReplaceChild("moon_ankle_right_leg", CubeListBuilder.create()
                .texOffs(48, 350).addBox(-1F, -0.779F, -0.5F, 2F, 1F, 1F, new CubeDeformation(-0.194F, -0.221F, -0.15F)),
                PartPose.offsetAndRotation(-3.35F, 9.8F, 0.4F, 0F, -1.5708F, 0F));
        PartDefinition p_moon_ankle_right_leg_left_0 = p_moon_ankle_right_leg.addOrReplaceChild("moon_ankle_right_leg_left_0", CubeListBuilder.create()
                .texOffs(105, 353).addBox(-0.5F, -0.965F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.2055F, -0.035F, -0.175F)),
                PartPose.offsetAndRotation(0.651F, -0.279F, 0F, 0F, 0F, 1.0123F));
        PartDefinition p_moon_ankle_right_leg_left_1 = p_moon_ankle_right_leg_left_0.addOrReplaceChild("moon_ankle_right_leg_left_1", CubeListBuilder.create()
                .texOffs(110, 353).addBox(-0.5F, -0.9185F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.2675F, -0.0815F, -0.2F)),
                PartPose.offsetAndRotation(0F, -0.68F, 0F, 0F, 0F, -0.6981F));
        PartDefinition p_moon_ankle_right_leg_left_2 = p_moon_ankle_right_leg_left_1.addOrReplaceChild("moon_ankle_right_leg_left_2", CubeListBuilder.create()
                .texOffs(115, 353).addBox(-0.5F, -0.8565F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.345F, -0.1435F, -0.25F)),
                PartPose.offsetAndRotation(0F, -0.587F, 0F, 0F, 0F, -0.6283F));
        PartDefinition p_moon_ankle_right_leg_right_0 = p_moon_ankle_right_leg.addOrReplaceChild("moon_ankle_right_leg_right_0", CubeListBuilder.create()
                .texOffs(120, 353).addBox(-0.5F, -0.965F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.2055F, -0.035F, -0.175F)),
                PartPose.offsetAndRotation(-0.651F, -0.279F, 0F, 0F, 0F, -1.0123F));
        PartDefinition p_moon_ankle_right_leg_right_1 = p_moon_ankle_right_leg_right_0.addOrReplaceChild("moon_ankle_right_leg_right_1", CubeListBuilder.create()
                .texOffs(125, 353).addBox(-0.5F, -0.9185F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.2675F, -0.0815F, -0.2F)),
                PartPose.offsetAndRotation(0F, -0.68F, 0F, 0F, 0F, 0.6981F));
        PartDefinition p_moon_ankle_right_leg_right_2 = p_moon_ankle_right_leg_right_1.addOrReplaceChild("moon_ankle_right_leg_right_2", CubeListBuilder.create()
                .texOffs(130, 353).addBox(-0.5F, -0.8565F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.345F, -0.1435F, -0.25F)),
                PartPose.offsetAndRotation(0F, -0.587F, 0F, 0F, 0F, 0.6283F));
        PartDefinition p_aquamarine_head_head = root.addOrReplaceChild("aquamarine_head_head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-6F, -8.6F, -6F, 12F, 8F, 12F, new CubeDeformation(-0.3F, 0F, -0.3F))
                .texOffs(41, 35).addBox(-5F, -9.6F, -5F, 10F, 1F, 10F)
                .texOffs(145, 148).addBox(-4F, -10.5F, -4F, 8F, 1F, 8F, new CubeDeformation(-0.3F, -0.1F, -0.3F))
                .texOffs(145, 0).addBox(5.45F, -8.1F, -4.5F, 1F, 7F, 9F, new CubeDeformation(-0.25F, -0.3F, -0.1F))
                .texOffs(166, 0).addBox(-6.45F, -8.1F, -4.5F, 1F, 7F, 9F, new CubeDeformation(-0.25F, -0.3F, -0.1F))
                .texOffs(229, 199).addBox(-4.5F, -8.1F, 5.45F, 9F, 7F, 1F, new CubeDeformation(-0.1F, -0.3F, -0.25F))
                .texOffs(190, 321).addBox(-1F, -11.4F, -1F, 2F, 1F, 2F, new CubeDeformation(-0.2F, 0F, -0.2F))
                .texOffs(244, 346).addBox(-2F, -12.15F, -0.5F, 4F, 1F, 1F, new CubeDeformation(-0.1F, -0.25F, -0.15F))
                .texOffs(187, 0).addBox(-6.5F, -1.35F, -6.5F, 13F, 2F, 13F, new CubeDeformation(-0.4F, -0.35F, -0.4F))
                .texOffs(218, 346).addBox(-2.5F, -8.2F, -6.6F, 5F, 1F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.1F))
                .texOffs(231, 346).addBox(-2.5F, -2.2F, -6.6F, 5F, 1F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.1F))
                .texOffs(125, 225).addBox(-3.3F, -7.7F, -6.6F, 1F, 6F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.1F))
                .texOffs(130, 225).addBox(2.3F, -7.7F, -6.6F, 1F, 6F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.1F))
                .texOffs(135, 353).addBox(-2.55F, -7.45F, -6.475F, 1F, 1F, 1F, new CubeDeformation(-0.15F, -0.15F, -0.225F))
                .texOffs(140, 353).addBox(1.55F, -7.45F, -6.475F, 1F, 1F, 1F, new CubeDeformation(-0.15F, -0.15F, -0.225F))
                .texOffs(145, 353).addBox(-2.55F, -2.95F, -6.475F, 1F, 1F, 1F, new CubeDeformation(-0.15F, -0.15F, -0.225F))
                .texOffs(150, 353).addBox(1.55F, -2.95F, -6.475F, 1F, 1F, 1F, new CubeDeformation(-0.15F, -0.15F, -0.225F))
                .texOffs(127, 254).addBox(5.85F, -6.3F, -1.5F, 1F, 3F, 3F, new CubeDeformation(-0.35F, 0F, 0F))
                .texOffs(130, 247).addBox(6F, -7.1F, -2.5F, 1F, 1F, 5F, new CubeDeformation(-0.2F, -0.2F, -0.4F))
                .texOffs(143, 247).addBox(6F, -3.5F, -2.5F, 1F, 1F, 5F, new CubeDeformation(-0.2F, -0.2F, -0.4F))
                .texOffs(214, 316).addBox(6F, -6.3F, -2.3F, 1F, 3F, 1F, new CubeDeformation(-0.2F, 0F, -0.2F))
                .texOffs(219, 316).addBox(6F, -6.3F, 1.3F, 1F, 3F, 1F, new CubeDeformation(-0.2F, 0F, -0.2F))
                .texOffs(136, 254).addBox(-6.85F, -6.3F, -1.5F, 1F, 3F, 3F, new CubeDeformation(-0.35F, 0F, 0F))
                .texOffs(156, 247).addBox(-7F, -7.1F, -2.5F, 1F, 1F, 5F, new CubeDeformation(-0.2F, -0.2F, -0.4F))
                .texOffs(169, 247).addBox(-7F, -3.5F, -2.5F, 1F, 1F, 5F, new CubeDeformation(-0.2F, -0.2F, -0.4F))
                .texOffs(224, 316).addBox(-7F, -6.3F, -2.3F, 1F, 3F, 1F, new CubeDeformation(-0.2F, 0F, -0.2F))
                .texOffs(229, 316).addBox(-7F, -6.3F, 1.3F, 1F, 3F, 1F, new CubeDeformation(-0.2F, 0F, -0.2F))
                .texOffs(56, 334).addBox(0.7F, -3.6F, 6.15F, 2F, 2F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.05F))
                .texOffs(225, 279).addBox(0.7F, -2.2F, 6F, 2F, 3F, 2F, new CubeDeformation(-0.45F, -0.2F, -0.45F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_aquamarine_chest_body = root.addOrReplaceChild("aquamarine_chest_body", CubeListBuilder.create()
                .texOffs(21, 59).addBox(0.6F, 1.2F, 3.1F, 3F, 8F, 3F, new CubeDeformation(0F, 0F, -0.1F))
                .texOffs(169, 285).addBox(0.6F, 0.4F, 3.1F, 3F, 1F, 3F, new CubeDeformation(-0.3F, -0.2F, -0.4F))
                .texOffs(182, 285).addBox(0.6F, 8.95F, 3.1F, 3F, 1F, 3F, new CubeDeformation(-0.3F, -0.25F, -0.4F))
                .texOffs(155, 353).addBox(1.6F, -0.25F, 4.1F, 1F, 1F, 1F, new CubeDeformation(0F, -0.15F, 0F))
                .texOffs(160, 353).addBox(3.2F, -0.2F, 4.1F, 1F, 1F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.05F))
                .texOffs(34, 59).addBox(-3.6F, 1.2F, 3.1F, 3F, 8F, 3F, new CubeDeformation(0F, 0F, -0.1F))
                .texOffs(195, 285).addBox(-3.6F, 0.4F, 3.1F, 3F, 1F, 3F, new CubeDeformation(-0.3F, -0.2F, -0.4F))
                .texOffs(208, 285).addBox(-3.6F, 8.95F, 3.1F, 3F, 1F, 3F, new CubeDeformation(-0.3F, -0.25F, -0.4F))
                .texOffs(165, 353).addBox(-2.6F, -0.25F, 4.1F, 1F, 1F, 1F, new CubeDeformation(0F, -0.15F, 0F))
                .texOffs(170, 353).addBox(-4.2F, -0.2F, 4.1F, 1F, 1F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.05F))
                .texOffs(170, 254).addBox(-4F, 2.05F, 2.6F, 8F, 1F, 4F, new CubeDeformation(-0.2F, -0.15F, -0.4F))
                .texOffs(195, 254).addBox(-4F, 7.05F, 2.6F, 8F, 1F, 4F, new CubeDeformation(-0.2F, -0.15F, -0.4F))
                .texOffs(199, 321).addBox(1.1F, -0.6F, 4.9F, 2F, 1F, 2F, new CubeDeformation(-0.45F, 0F, -0.1F))
                .texOffs(117, 311).addBox(1.1F, 1.6F, -3.95F, 3F, 3F, 1F, new CubeDeformation(0F, 0F, -0.2F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_aquamarine_chest_left_arm = root.addOrReplaceChild("aquamarine_chest_left_arm", CubeListBuilder.create()
                .texOffs(0, 169).addBox(-2.5F, 5.95F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.25F, -0.25F, -0.25F))
                .texOffs(63, 334).addBox(0F, 5.95F, -3.95F, 2F, 2F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_aqua_pauldron_left_arm = p_aquamarine_chest_left_arm.addOrReplaceChild("aqua_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(116, 179).addBox(-2.6F, -1.5F, -3.5F, 6F, 2F, 7F, new CubeDeformation(0F, -0.2F, -0.1F))
                .texOffs(83, 199).addBox(-1.6F, 0.3F, -3.5F, 5F, 1F, 7F),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.1745F));
        PartDefinition p_aquamarine_chest_right_arm = root.addOrReplaceChild("aquamarine_chest_right_arm", CubeListBuilder.create()
                .texOffs(29, 169).addBox(-4.5F, 5.95F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.25F, -0.25F, -0.25F))
                .texOffs(70, 334).addBox(-2F, 5.95F, -3.95F, 2F, 2F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_aqua_pauldron_right_arm = p_aquamarine_chest_right_arm.addOrReplaceChild("aqua_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(143, 179).addBox(-3.4F, -1.5F, -3.5F, 6F, 2F, 7F, new CubeDeformation(0F, -0.2F, -0.1F))
                .texOffs(108, 199).addBox(-3.4F, 0.3F, -3.5F, 5F, 1F, 7F),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.1745F));
        PartDefinition p_aquamarine_legs_body = root.addOrReplaceChild("aquamarine_legs_body", CubeListBuilder.create()
                .texOffs(208, 321).addBox(-4.9F, 10.25F, -3.675F, 3F, 2F, 1F, new CubeDeformation(-0.3F, -0.05F, -0.125F))
                .texOffs(217, 321).addBox(1.9F, 10.25F, -3.675F, 3F, 2F, 1F, new CubeDeformation(-0.3F, -0.05F, -0.125F))
                .texOffs(234, 279).addBox(4.475F, 10.25F, -1.5F, 1F, 2F, 3F, new CubeDeformation(-0.125F, -0.05F, -0.2F))
                .texOffs(243, 279).addBox(-5.475F, 10.25F, -1.5F, 1F, 2F, 3F, new CubeDeformation(-0.125F, -0.05F, -0.2F))
                .texOffs(226, 321).addBox(-1.5F, 10.25F, -3.8F, 3F, 2F, 1F, new CubeDeformation(-0.4F, -0.25F, -0.2F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_aquamarine_legs_left_leg = root.addOrReplaceChild("aquamarine_legs_left_leg", CubeListBuilder.create()
                .texOffs(221, 285).addBox(-2.5F, 3.4F, -3.25F, 5F, 3F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(77, 334).addBox(-1F, 3.9F, -3.7F, 2F, 2F, 1F, new CubeDeformation(-0.45F, -0.45F, -0.3F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_aquamarine_legs_right_leg = root.addOrReplaceChild("aquamarine_legs_right_leg", CubeListBuilder.create()
                .texOffs(234, 285).addBox(-2.5F, 3.4F, -3.25F, 5F, 3F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(84, 334).addBox(-1F, 3.9F, -3.7F, 2F, 2F, 1F, new CubeDeformation(-0.45F, -0.45F, -0.3F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_aquamarine_feet_left_leg = root.addOrReplaceChild("aquamarine_feet_left_leg", CubeListBuilder.create()
                .texOffs(25, 217).addBox(-3F, 9.25F, -3F, 6F, 1F, 6F, new CubeDeformation(-0.45F, -0.05F, -0.45F))
                .texOffs(139, 316).addBox(2.275F, 8.75F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.275F, -0.4F, -0.3F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_aqua_fin_left_leg_0 = p_aquamarine_feet_left_leg.addOrReplaceChild("aqua_fin_left_leg_0", CubeListBuilder.create()
                .texOffs(0, 267).addBox(-2.5F, -3.7F, -0.5F, 5F, 4F, 1F, new CubeDeformation(-0.2F, -0.3F, -0.275F)),
                PartPose.offsetAndRotation(0F, 11.75F, -2F, 1.5708F, 0F, 0F));
        PartDefinition p_aqua_fin_left_leg_1 = p_aqua_fin_left_leg_0.addOrReplaceChild("aqua_fin_left_leg_1", CubeListBuilder.create()
                .texOffs(165, 261).addBox(-3F, -3.6F, -0.5F, 6F, 4F, 1F, new CubeDeformation(-0.3F, -0.4F, -0.3F)),
                PartPose.offsetAndRotation(0F, -3.25F, 0F, -0.0873F, 0F, 0F));
        PartDefinition p_aqua_fin_left_leg_2 = p_aqua_fin_left_leg_1.addOrReplaceChild("aqua_fin_left_leg_2", CubeListBuilder.create()
                .texOffs(57, 285).addBox(-3F, -2.8F, -0.5F, 6F, 3F, 1F, new CubeDeformation(0F, -0.2F, -0.325F)),
                PartPose.offsetAndRotation(0F, -3.05F, 0F, -0.1571F, 0F, 0F));
        PartDefinition p_aquamarine_feet_right_leg = root.addOrReplaceChild("aquamarine_feet_right_leg", CubeListBuilder.create()
                .texOffs(50, 217).addBox(-3F, 9.25F, -3F, 6F, 1F, 6F, new CubeDeformation(-0.45F, -0.05F, -0.45F))
                .texOffs(146, 316).addBox(-3.275F, 8.75F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.275F, -0.4F, -0.3F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_aqua_fin_right_leg_0 = p_aquamarine_feet_right_leg.addOrReplaceChild("aqua_fin_right_leg_0", CubeListBuilder.create()
                .texOffs(13, 267).addBox(-2.5F, -3.7F, -0.5F, 5F, 4F, 1F, new CubeDeformation(-0.2F, -0.3F, -0.275F)),
                PartPose.offsetAndRotation(0F, 11.75F, -2F, 1.5708F, 0F, 0F));
        PartDefinition p_aqua_fin_right_leg_1 = p_aqua_fin_right_leg_0.addOrReplaceChild("aqua_fin_right_leg_1", CubeListBuilder.create()
                .texOffs(180, 261).addBox(-3F, -3.6F, -0.5F, 6F, 4F, 1F, new CubeDeformation(-0.3F, -0.4F, -0.3F)),
                PartPose.offsetAndRotation(0F, -3.25F, 0F, -0.0873F, 0F, 0F));
        PartDefinition p_aqua_fin_right_leg_2 = p_aqua_fin_right_leg_1.addOrReplaceChild("aqua_fin_right_leg_2", CubeListBuilder.create()
                .texOffs(72, 285).addBox(-3F, -2.8F, -0.5F, 6F, 3F, 1F, new CubeDeformation(0F, -0.2F, -0.325F)),
                PartPose.offsetAndRotation(0F, -3.05F, 0F, -0.1571F, 0F, 0F));
        PartDefinition p_dusk_head_head = root.addOrReplaceChild("dusk_head_head", CubeListBuilder.create()
                .texOffs(49, 21).addBox(-5.5F, -10.15F, -5.4F, 11F, 2F, 11F, new CubeDeformation(0F, -0.45F, -0.1F))
                .texOffs(49, 0).addBox(4.775F, -9.1F, -5.4F, 1F, 9F, 11F, new CubeDeformation(-0.225F, -0.3F, -0.1F))
                .texOffs(74, 0).addBox(-5.775F, -9.1F, -5.4F, 1F, 9F, 11F, new CubeDeformation(-0.225F, -0.3F, -0.1F))
                .texOffs(23, 148).addBox(-5F, -9F, 4.775F, 10F, 9F, 1F, new CubeDeformation(0F, -0.2F, -0.225F))
                .texOffs(32, 321).addBox(-6F, -9.45F, -6.05F, 12F, 2F, 1F, new CubeDeformation(-0.45F, -0.25F, -0.2F))
                .texOffs(189, 208).addBox(3.925F, -8.3F, -6.05F, 2F, 7F, 1F, new CubeDeformation(-0.375F, -0.3F, -0.2F))
                .texOffs(196, 208).addBox(-5.925F, -8.3F, -6.05F, 2F, 7F, 1F, new CubeDeformation(-0.375F, -0.3F, -0.2F))
                .texOffs(40, 285).addBox(-3.5F, -3.7F, -5.775F, 7F, 3F, 1F, new CubeDeformation(-0.2F, -0.1F, -0.225F))
                .texOffs(234, 316).addBox(-0.5F, -3.75F, -6.075F, 1F, 3F, 1F, new CubeDeformation(-0.05F, 0F, -0.325F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_dusk_hood_tip_0 = p_dusk_head_head.addOrReplaceChild("dusk_hood_tip_0", CubeListBuilder.create()
                .texOffs(0, 291).addBox(-2.5F, -0.2F, -0.5F, 5F, 3F, 1F, new CubeDeformation(-0.2F, -0.2F, 0F)),
                PartPose.offsetAndRotation(0F, -8.4F, 5.3F, 1.0821F, 0F, 0F));
        PartDefinition p_dusk_hood_tip_1 = p_dusk_hood_tip_0.addOrReplaceChild("dusk_hood_tip_1", CubeListBuilder.create()
                .texOffs(187, 296).addBox(-2F, -0.1F, -0.5F, 4F, 3F, 1F, new CubeDeformation(-0.3F, -0.1F, -0.05F)),
                PartPose.offsetAndRotation(0F, 2.35F, 0F, -0.5236F, 0F, 0F));
        PartDefinition p_dusk_hood_tip_2 = p_dusk_hood_tip_1.addOrReplaceChild("dusk_hood_tip_2", CubeListBuilder.create()
                .texOffs(126, 311).addBox(-1.5F, -0.1F, -0.5F, 3F, 3F, 1F, new CubeDeformation(-0.3F, -0.1F, -0.1F)),
                PartPose.offsetAndRotation(0F, 2.55F, 0F, -0.384F, 0F, 0F));
        PartDefinition p_dusk_hood_tip_3 = p_dusk_hood_tip_2.addOrReplaceChild("dusk_hood_tip_3", CubeListBuilder.create()
                .texOffs(153, 316).addBox(-1F, -0.3F, -0.5F, 2F, 3F, 1F, new CubeDeformation(-0.3F, -0.3F, -0.15F)),
                PartPose.offsetAndRotation(0F, 2.55F, 0F, -0.2094F, 0F, 0F));
        PartDefinition p_dusk_chest_body = root.addOrReplaceChild("dusk_chest_body", CubeListBuilder.create()
                .texOffs(47, 59).addBox(-5.5F, -1F, -4F, 11F, 2F, 8F, new CubeDeformation(-0.1F, -0.45F, -0.45F))
                .texOffs(235, 321).addBox(1.6F, 0.2F, -4F, 3F, 2F, 1F, new CubeDeformation(-0.4F, 0F, -0.25F))
                .texOffs(91, 334).addBox(-4.05F, 2F, -3.775F, 2F, 2F, 1F, new CubeDeformation(-0.15F, -0.4F, -0.325F))
                .texOffs(98, 334).addBox(-2.5F, 3.6F, -3.775F, 2F, 2F, 1F, new CubeDeformation(-0.15F, -0.4F, -0.325F))
                .texOffs(105, 334).addBox(-0.95F, 5.2F, -3.775F, 2F, 2F, 1F, new CubeDeformation(-0.15F, -0.4F, -0.325F))
                .texOffs(112, 334).addBox(0.6F, 6.8F, -3.775F, 2F, 2F, 1F, new CubeDeformation(-0.15F, -0.4F, -0.325F))
                .texOffs(119, 334).addBox(2.15F, 8.4F, -3.775F, 2F, 2F, 1F, new CubeDeformation(-0.15F, -0.4F, -0.325F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_dusk_cape = p_dusk_chest_body.addOrReplaceChild("dusk_cape", CubeListBuilder.create()
                .texOffs(133, 199).addBox(-5.5F, -0.1F, -0.2F, 11F, 7F, 1F, new CubeDeformation(-0.3F, -0.1F, -0.2F))
                .texOffs(135, 311).addBox(-5.6F, 6.35F, -0.2F, 3F, 3F, 1F, new CubeDeformation(-0.4F, -0.45F, -0.22F))
                .texOffs(244, 321).addBox(-3.1F, 6.4F, -0.2F, 3F, 2F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.22F))
                .texOffs(144, 311).addBox(-0.6F, 6.35F, -0.2F, 3F, 3F, 1F, new CubeDeformation(-0.4F, -0.45F, -0.22F))
                .texOffs(0, 326).addBox(1.9F, 6.4F, -0.2F, 3F, 2F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.22F)),
                PartPose.offsetAndRotation(0F, 0.2F, 3.25F, 0.1047F, 0F, 0F));
        PartDefinition p_dusk_clasp_a_0 = p_dusk_chest_body.addOrReplaceChild("dusk_clasp_a_0", CubeListBuilder.create()
                .texOffs(100, 346).addBox(-0.5F, -1.5445F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.05F, -0.4555F, -0.05F)),
                PartPose.offsetAndRotation(3F, 1.8F, -4F, -0.2094F, 0F, 0.2443F));
        PartDefinition p_dusk_clasp_a_1 = p_dusk_clasp_a_0.addOrReplaceChild("dusk_clasp_a_1", CubeListBuilder.create()
                .texOffs(175, 353).addBox(-0.5F, -0.995F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.2525F, -0.005F, -0.2525F)),
                PartPose.offset(0F, -0.839F, 0F));
        PartDefinition p_dusk_clasp_b_0 = p_dusk_chest_body.addOrReplaceChild("dusk_clasp_b_0", CubeListBuilder.create()
                .texOffs(180, 353).addBox(-0.5F, -0.8713F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.14F, -0.1287F, -0.14F)),
                PartPose.offsetAndRotation(3.63F, 2F, -4F, -0.2094F, 0F, -0.4189F));
        PartDefinition p_dusk_clasp_b_1 = p_dusk_clasp_b_0.addOrReplaceChild("dusk_clasp_b_1", CubeListBuilder.create()
                .texOffs(185, 353).addBox(-0.5F, -0.8375F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.302F, -0.1625F, -0.302F)),
                PartPose.offset(0F, -0.4925F, 0F));
        PartDefinition p_dusk_chest_left_arm = root.addOrReplaceChild("dusk_chest_left_arm", CubeListBuilder.create()
                .texOffs(58, 169).addBox(-2.5F, 0.85F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.15F, -0.35F, -0.15F))
                .texOffs(151, 189).addBox(-2.5F, 2.9F, -3.5F, 7F, 1F, 7F, new CubeDeformation(-0.15F, 0F, -0.15F))
                .texOffs(87, 169).addBox(-2.5F, 7.4F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.2F, -0.2F, -0.2F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_dusk_chest_right_arm = root.addOrReplaceChild("dusk_chest_right_arm", CubeListBuilder.create()
                .texOffs(116, 169).addBox(-4.5F, 0.85F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.15F, -0.35F, -0.15F))
                .texOffs(180, 189).addBox(-4.5F, 2.9F, -3.5F, 7F, 1F, 7F, new CubeDeformation(-0.15F, 0F, -0.15F))
                .texOffs(145, 169).addBox(-4.5F, 7.4F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.2F, -0.2F, -0.2F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_dusk_legs_body = root.addOrReplaceChild("dusk_legs_body", CubeListBuilder.create()
                .texOffs(153, 311).addBox(-4.8F, 9.9F, -3.45F, 3F, 3F, 1F, new CubeDeformation(-0.4F, -0.3F, 0F))
                .texOffs(14, 285).addBox(-4.65F, 9.7F, -1F, 1F, 3F, 2F, new CubeDeformation(-0.05F, -0.4F, 0F))
                .texOffs(9, 326).addBox(-4.5F, 9.7F, -4F, 2F, 1F, 2F, new CubeDeformation(-0.2F, -0.2F, -0.4F))
                .texOffs(126, 334).addBox(-1F, 10.1F, -3.875F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.3F, -0.275F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_dusk_dagger = p_dusk_legs_body.addOrReplaceChild("dusk_dagger", CubeListBuilder.create()
                .texOffs(239, 316).addBox(-0.5F, -2.5F, -0.15F, 1F, 3F, 1F, new CubeDeformation(-0.05F, 0F, -0.15F))
                .texOffs(55, 350).addBox(-1F, -3.25F, -0.15F, 2F, 1F, 1F, new CubeDeformation(-0.3F, -0.25F, -0.1F))
                .texOffs(105, 346).addBox(-0.5F, -4.8F, -0.15F, 1F, 2F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(190, 353).addBox(-0.5F, -5.3F, -0.15F, 1F, 1F, 1F, new CubeDeformation(-0.1F, -0.2F, -0.15F)),
                PartPose.offsetAndRotation(0F, 11F, 3.3F, 0F, 0F, 1.2915F));
        PartDefinition p_dusk_legs_left_leg = root.addOrReplaceChild("dusk_legs_left_leg", CubeListBuilder.create()
                .texOffs(13, 291).addBox(-2.5F, 3.3F, -3.25F, 5F, 3F, 1F, new CubeDeformation(-0.15F, -0.1F, -0.2F))
                .texOffs(133, 334).addBox(-1F, 3.8F, -3.675F, 2F, 2F, 1F, new CubeDeformation(-0.45F, -0.45F, -0.275F))
                .texOffs(181, 225).addBox(-2.5F, 6.95F, -2.5F, 5F, 1F, 5F, new CubeDeformation(-0.1F, -0.05F, -0.1F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_dusk_legs_right_leg = root.addOrReplaceChild("dusk_legs_right_leg", CubeListBuilder.create()
                .texOffs(26, 291).addBox(-2.5F, 3.3F, -3.25F, 5F, 3F, 1F, new CubeDeformation(-0.15F, -0.1F, -0.2F))
                .texOffs(140, 334).addBox(-1F, 3.8F, -3.675F, 2F, 2F, 1F, new CubeDeformation(-0.45F, -0.45F, -0.275F))
                .texOffs(202, 225).addBox(-2.5F, 6.95F, -2.5F, 5F, 1F, 5F, new CubeDeformation(-0.1F, -0.05F, -0.1F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_dusk_feet_left_leg = root.addOrReplaceChild("dusk_feet_left_leg", CubeListBuilder.create()
                .texOffs(158, 199).addBox(-3F, 6.15F, -3F, 6F, 2F, 6F, new CubeDeformation(-0.45F, -0.25F, -0.45F))
                .texOffs(223, 225).addBox(-2.5F, 8.5F, -2.5F, 5F, 1F, 5F, new CubeDeformation(-0.05F, -0.1F, -0.05F))
                .texOffs(0, 233).addBox(-2.5F, 9.9F, -2.5F, 5F, 1F, 5F, new CubeDeformation(-0.05F, -0.1F, -0.05F))
                .texOffs(160, 316).addBox(2.1F, 8F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.3F, -0.4F, -0.4F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_dusk_feet_right_leg = root.addOrReplaceChild("dusk_feet_right_leg", CubeListBuilder.create()
                .texOffs(183, 199).addBox(-3F, 6.15F, -3F, 6F, 2F, 6F, new CubeDeformation(-0.45F, -0.25F, -0.45F))
                .texOffs(21, 233).addBox(-2.5F, 8.5F, -2.5F, 5F, 1F, 5F, new CubeDeformation(-0.05F, -0.1F, -0.05F))
                .texOffs(42, 233).addBox(-2.5F, 9.9F, -2.5F, 5F, 1F, 5F, new CubeDeformation(-0.05F, -0.1F, -0.05F))
                .texOffs(167, 316).addBox(-3.1F, 8F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.3F, -0.4F, -0.4F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_starwalker_head_head = root.addOrReplaceChild("starwalker_head_head", CubeListBuilder.create()
                .texOffs(168, 47).addBox(-0.5F, -9.975F, -5F, 1F, 1F, 10F, new CubeDeformation(-0.05F, -0.025F, -0.1F))
                .texOffs(244, 316).addBox(-0.5F, -9.4F, -6F, 1F, 3F, 1F, new CubeDeformation(-0.125F, -0.25F, -0.25F))
                .texOffs(62, 350).addBox(-1F, -8.4F, -6F, 2F, 1F, 1F, new CubeDeformation(0F, -0.125F, -0.25F))
                .texOffs(147, 334).addBox(-1F, -8.9F, -6.075F, 2F, 2F, 1F, new CubeDeformation(-0.4375F, -0.4375F, -0.175F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_star_fin_left = p_starwalker_head_head.addOrReplaceChild("star_fin_left", CubeListBuilder.create()
                .texOffs(21, 285).addBox(-0.5F, -1.5F, -1F, 1F, 3F, 2F, new CubeDeformation(-0.1F, -0.465F, -0.08F)),
                PartPose.offsetAndRotation(5.25F, -5.6F, -1.6F, 0.2094F, 0.1745F, 0F));
        PartDefinition p_star_fin_left_blade_0 = p_star_fin_left.addOrReplaceChild("star_fin_left_blade_0", CubeListBuilder.create()
                .texOffs(107, 148).addBox(-0.5F, -1F, -0.435F, 1F, 2F, 8F, new CubeDeformation(-0.3F, -0.2525F, -0.435F)),
                PartPose.offsetAndRotation(0F, -0.345F, 0.23F, 0.1047F, 0F, 0F));
        PartDefinition p_star_fin_left_blade_1 = p_star_fin_left.addOrReplaceChild("star_fin_left_blade_1", CubeListBuilder.create()
                .texOffs(115, 208).addBox(-0.5F, -1F, -0.125F, 1F, 2F, 6F, new CubeDeformation(-0.3F, -0.3675F, -0.125F)),
                PartPose.offsetAndRotation(0F, -0.7475F, 0.23F, 0.384F, 0F, 0F));
        PartDefinition p_star_fin_left_blade_2 = p_star_fin_left.addOrReplaceChild("star_fin_left_blade_2", CubeListBuilder.create()
                .texOffs(26, 225).addBox(-0.5F, -1F, -0.43F, 1F, 2F, 5F, new CubeDeformation(-0.3F, -0.4825F, -0.43F)),
                PartPose.offsetAndRotation(0F, -1.15F, 0.23F, 0.6981F, 0F, 0F));
        PartDefinition p_star_fin_right = p_starwalker_head_head.addOrReplaceChild("star_fin_right", CubeListBuilder.create()
                .texOffs(28, 285).addBox(-0.5F, -1.5F, -1F, 1F, 3F, 2F, new CubeDeformation(-0.1F, -0.465F, -0.08F)),
                PartPose.offsetAndRotation(-5.25F, -5.6F, -1.6F, 0.2094F, -0.1745F, 0F));
        PartDefinition p_star_fin_right_blade_0 = p_star_fin_right.addOrReplaceChild("star_fin_right_blade_0", CubeListBuilder.create()
                .texOffs(126, 148).addBox(-0.5F, -1F, -0.435F, 1F, 2F, 8F, new CubeDeformation(-0.3F, -0.2525F, -0.435F)),
                PartPose.offsetAndRotation(0F, -0.345F, 0.23F, 0.1047F, 0F, 0F));
        PartDefinition p_star_fin_right_blade_1 = p_star_fin_right.addOrReplaceChild("star_fin_right_blade_1", CubeListBuilder.create()
                .texOffs(130, 208).addBox(-0.5F, -1F, -0.125F, 1F, 2F, 6F, new CubeDeformation(-0.3F, -0.3675F, -0.125F)),
                PartPose.offsetAndRotation(0F, -0.7475F, 0.23F, 0.384F, 0F, 0F));
        PartDefinition p_star_fin_right_blade_2 = p_star_fin_right.addOrReplaceChild("star_fin_right_blade_2", CubeListBuilder.create()
                .texOffs(39, 225).addBox(-0.5F, -1F, -0.43F, 1F, 2F, 5F, new CubeDeformation(-0.3F, -0.4825F, -0.43F)),
                PartPose.offsetAndRotation(0F, -1.15F, 0.23F, 0.6981F, 0F, 0F));
        PartDefinition p_starwalker_chest_body = root.addOrReplaceChild("starwalker_chest_body", CubeListBuilder.create()
                .texOffs(140, 267).addBox(-2F, 1.2F, -3.725F, 4F, 4F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.275F))
                .texOffs(249, 316).addBox(-0.5F, 1.7F, -4.2F, 1F, 3F, 1F, new CubeDeformation(-0.095F, -0.15F, -0.25F))
                .texOffs(18, 350).addBox(-1.5F, 2.7F, -4.2F, 3F, 1F, 1F, new CubeDeformation(-0.42F, -0.095F, -0.25F))
                .texOffs(154, 334).addBox(-1F, 2.2F, -4.275F, 2F, 2F, 1F, new CubeDeformation(-0.3925F, -0.3925F, -0.175F))
                .texOffs(161, 334).addBox(-4.9F, -1F, -3.75F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.3F, -0.15F))
                .texOffs(168, 334).addBox(2.9F, -1F, -3.75F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.3F, -0.15F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_star_cape = p_starwalker_chest_body.addOrReplaceChild("star_cape", CubeListBuilder.create()
                .texOffs(75, 217).addBox(-5F, -0.3F, -0.2F, 10F, 6F, 1F, new CubeDeformation(-0.4F, -0.3F, -0.2F)),
                PartPose.offsetAndRotation(0F, 0F, 3.1F, 0.0873F, 0F, 0F));
        PartDefinition p_star_cape_mid = p_star_cape.addOrReplaceChild("star_cape_mid", CubeListBuilder.create()
                .texOffs(220, 254).addBox(-4F, -0.1F, -0.2F, 8F, 4F, 1F, new CubeDeformation(-0.4F, -0.1F, -0.2F)),
                PartPose.offsetAndRotation(0F, 5.2F, 0.05F, 0.1047F, 0F, 0F));
        PartDefinition p_star_cape_tip = p_star_cape_mid.addOrReplaceChild("star_cape_tip", CubeListBuilder.create()
                .texOffs(39, 291).addBox(-2.5F, -0.1F, -0.2F, 5F, 3F, 1F, new CubeDeformation(-0.3F, -0.1F, -0.2F))
                .texOffs(175, 334).addBox(-1F, 2.5F, -0.2F, 2F, 2F, 1F, new CubeDeformation(0F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(0F, 3.6F, 0.05F, 0.1047F, 0F, 0F));
        PartDefinition p_starwalker_chest_left_arm = root.addOrReplaceChild("starwalker_chest_left_arm", CubeListBuilder.create()
                .texOffs(174, 169).addBox(-2.5F, 5.85F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.2F, -0.15F, -0.2F))
                .texOffs(195, 353).addBox(0.5F, 6.35F, -3.95F, 1F, 1F, 1F, new CubeDeformation(0F, 0F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_star_pauldron_left_arm = p_starwalker_chest_left_arm.addOrReplaceChild("star_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(186, 93).addBox(-3.2F, -1.7F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.2F, -0.4F))
                .texOffs(58, 137).addBox(-2.05F, -0.25F, -4F, 6F, 2F, 8F, new CubeDeformation(-0.05F, -0.35F, -0.3F))
                .texOffs(212, 179).addBox(-0.5F, -2.3F, -4F, 1F, 1F, 8F, new CubeDeformation(0F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(1F, -3.1F, 0F, 0F, 0F, 0.2094F));
        PartDefinition p_star_float_a_left_arm_0 = p_star_pauldron_left_arm.addOrReplaceChild("star_float_a_left_arm_0", CubeListBuilder.create()
                .texOffs(110, 346).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.05F, -0.45F, -0.05F)),
                PartPose.offsetAndRotation(1.6F, -3.6F, -1.2F, 0.1396F, 0F, 0.2443F));
        PartDefinition p_star_float_a_left_arm_1 = p_star_float_a_left_arm_0.addOrReplaceChild("star_float_a_left_arm_1", CubeListBuilder.create()
                .texOffs(200, 353).addBox(-0.5F, -1F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.2525F, 0F, -0.2525F)),
                PartPose.offset(0F, -0.85F, 0F));
        PartDefinition p_star_float_b_left_arm_0 = p_star_pauldron_left_arm.addOrReplaceChild("star_float_b_left_arm_0", CubeListBuilder.create()
                .texOffs(205, 353).addBox(-0.5F, -0.9125F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.15F, -0.0875F, -0.15F)),
                PartPose.offsetAndRotation(0.4F, -3F, 1.5F, -0.1745F, 0F, -0.1745F));
        PartDefinition p_star_float_b_left_arm_1 = p_star_float_b_left_arm_0.addOrReplaceChild("star_float_b_left_arm_1", CubeListBuilder.create()
                .texOffs(210, 353).addBox(-0.5F, -0.875F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.3075F, -0.125F, -0.3075F)),
                PartPose.offset(0F, -0.575F, 0F));
        PartDefinition p_starwalker_chest_right_arm = root.addOrReplaceChild("starwalker_chest_right_arm", CubeListBuilder.create()
                .texOffs(203, 169).addBox(-4.5F, 5.85F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.2F, -0.15F, -0.2F))
                .texOffs(215, 353).addBox(-1.5F, 6.35F, -3.95F, 1F, 1F, 1F, new CubeDeformation(0F, 0F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_star_pauldron_right_arm = p_starwalker_chest_right_arm.addOrReplaceChild("star_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(217, 93).addBox(-3.8F, -1.7F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.2F, -0.4F))
                .texOffs(87, 137).addBox(-3.95F, -0.25F, -4F, 6F, 2F, 8F, new CubeDeformation(-0.05F, -0.35F, -0.3F))
                .texOffs(231, 179).addBox(-0.5F, -2.3F, -4F, 1F, 1F, 8F, new CubeDeformation(0F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(-1F, -3.1F, 0F, 0F, 0F, -0.2094F));
        PartDefinition p_star_float_a_right_arm_0 = p_star_pauldron_right_arm.addOrReplaceChild("star_float_a_right_arm_0", CubeListBuilder.create()
                .texOffs(115, 346).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.05F, -0.45F, -0.05F)),
                PartPose.offsetAndRotation(-1.6F, -3.6F, -1.2F, 0.1396F, 0F, -0.2443F));
        PartDefinition p_star_float_a_right_arm_1 = p_star_float_a_right_arm_0.addOrReplaceChild("star_float_a_right_arm_1", CubeListBuilder.create()
                .texOffs(220, 353).addBox(-0.5F, -1F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.2525F, 0F, -0.2525F)),
                PartPose.offset(0F, -0.85F, 0F));
        PartDefinition p_star_float_b_right_arm_0 = p_star_pauldron_right_arm.addOrReplaceChild("star_float_b_right_arm_0", CubeListBuilder.create()
                .texOffs(225, 353).addBox(-0.5F, -0.9125F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.15F, -0.0875F, -0.15F)),
                PartPose.offsetAndRotation(-0.4F, -3F, 1.5F, -0.1745F, 0F, 0.1745F));
        PartDefinition p_star_float_b_right_arm_1 = p_star_float_b_right_arm_0.addOrReplaceChild("star_float_b_right_arm_1", CubeListBuilder.create()
                .texOffs(230, 353).addBox(-0.5F, -0.875F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.3075F, -0.125F, -0.3075F)),
                PartPose.offset(0F, -0.575F, 0F));
        PartDefinition p_starwalker_legs_body = root.addOrReplaceChild("starwalker_legs_body", CubeListBuilder.create()
                .texOffs(18, 326).addBox(-1.5F, 10.1F, -3.775F, 3F, 2F, 1F, new CubeDeformation(-0.4F, 0F, -0.275F))
                .texOffs(182, 334).addBox(-1F, 10.1F, -4.1F, 2F, 2F, 1F, new CubeDeformation(-0.35F, -0.35F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_starwalker_legs_left_leg = root.addOrReplaceChild("starwalker_legs_left_leg", CubeListBuilder.create()
                .texOffs(52, 291).addBox(-2.5F, 3.45F, -3.275F, 5F, 3F, 1F, new CubeDeformation(-0.3F, -0.05F, -0.125F))
                .texOffs(235, 353).addBox(-0.5F, 4.4F, -3.85F, 1F, 1F, 1F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(145, 254).addBox(2F, 3.45F, -3F, 1F, 3F, 3F, new CubeDeformation(-0.15F, -0.45F, -0.4F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_starwalker_legs_right_leg = root.addOrReplaceChild("starwalker_legs_right_leg", CubeListBuilder.create()
                .texOffs(65, 291).addBox(-2.5F, 3.45F, -3.275F, 5F, 3F, 1F, new CubeDeformation(-0.3F, -0.05F, -0.125F))
                .texOffs(240, 353).addBox(-0.5F, 4.4F, -3.85F, 1F, 1F, 1F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(154, 254).addBox(-3F, 3.45F, -3F, 1F, 3F, 3F, new CubeDeformation(-0.15F, -0.45F, -0.4F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_starwalker_feet_left_leg = root.addOrReplaceChild("starwalker_feet_left_leg", CubeListBuilder.create()
                .texOffs(195, 261).addBox(-2.5F, 10.1F, -4.4F, 5F, 3F, 2F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_star_heel_left_leg = p_starwalker_feet_left_leg.addOrReplaceChild("star_heel_left_leg", CubeListBuilder.create()
                .texOffs(120, 346).addBox(-0.5F, -1F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1F, -0.442F, -0.004F)),
                PartPose.offsetAndRotation(1.2F, 9.6F, 2.7F, 0.2443F, 0.1396F, 0F));
        PartDefinition p_star_heel_left_leg_blade_0 = p_star_heel_left_leg.addOrReplaceChild("star_heel_left_leg_blade_0", CubeListBuilder.create()
                .texOffs(151, 267).addBox(-0.5F, -0.5F, -0.078F, 1F, 1F, 4F, new CubeDeformation(-0.3F, -0.097F, -0.078F)),
                PartPose.offsetAndRotation(0F, -0.186F, 0.124F, 0.1047F, 0F, 0F));
        PartDefinition p_star_heel_left_leg_blade_1 = p_star_heel_left_leg.addOrReplaceChild("star_heel_left_leg_blade_1", CubeListBuilder.create()
                .texOffs(162, 267).addBox(-0.5F, -0.5F, -0.45F, 1F, 1F, 4F, new CubeDeformation(-0.3F, -0.159F, -0.45F)),
                PartPose.offsetAndRotation(0F, -0.403F, 0.124F, 0.384F, 0F, 0F));
        PartDefinition p_star_heel_left_leg_blade_2 = p_star_heel_left_leg.addOrReplaceChild("star_heel_left_leg_blade_2", CubeListBuilder.create()
                .texOffs(162, 311).addBox(-0.5F, -0.5F, -0.384F, 1F, 1F, 3F, new CubeDeformation(-0.3F, -0.221F, -0.384F)),
                PartPose.offsetAndRotation(0F, -0.62F, 0.124F, 0.6981F, 0F, 0F));
        PartDefinition p_starwalker_feet_right_leg = root.addOrReplaceChild("starwalker_feet_right_leg", CubeListBuilder.create()
                .texOffs(210, 261).addBox(-2.5F, 10.1F, -4.4F, 5F, 3F, 2F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_star_heel_right_leg = p_starwalker_feet_right_leg.addOrReplaceChild("star_heel_right_leg", CubeListBuilder.create()
                .texOffs(125, 346).addBox(-0.5F, -1F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1F, -0.442F, -0.004F)),
                PartPose.offsetAndRotation(-1.2F, 9.6F, 2.7F, 0.2443F, -0.1396F, 0F));
        PartDefinition p_star_heel_right_leg_blade_0 = p_star_heel_right_leg.addOrReplaceChild("star_heel_right_leg_blade_0", CubeListBuilder.create()
                .texOffs(173, 267).addBox(-0.5F, -0.5F, -0.078F, 1F, 1F, 4F, new CubeDeformation(-0.3F, -0.097F, -0.078F)),
                PartPose.offsetAndRotation(0F, -0.186F, 0.124F, 0.1047F, 0F, 0F));
        PartDefinition p_star_heel_right_leg_blade_1 = p_star_heel_right_leg.addOrReplaceChild("star_heel_right_leg_blade_1", CubeListBuilder.create()
                .texOffs(184, 267).addBox(-0.5F, -0.5F, -0.45F, 1F, 1F, 4F, new CubeDeformation(-0.3F, -0.159F, -0.45F)),
                PartPose.offsetAndRotation(0F, -0.403F, 0.124F, 0.384F, 0F, 0F));
        PartDefinition p_star_heel_right_leg_blade_2 = p_star_heel_right_leg.addOrReplaceChild("star_heel_right_leg_blade_2", CubeListBuilder.create()
                .texOffs(171, 311).addBox(-0.5F, -0.5F, -0.384F, 1F, 1F, 3F, new CubeDeformation(-0.3F, -0.221F, -0.384F)),
                PartPose.offsetAndRotation(0F, -0.62F, 0.124F, 0.6981F, 0F, 0F));
        PartDefinition p_timewarden_head_head = root.addOrReplaceChild("timewarden_head_head", CubeListBuilder.create()
                .texOffs(38, 208).addBox(-0.5F, -9.95F, -3.5F, 1F, 1F, 7F, new CubeDeformation(0F, -0.05F, -0.3F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_tw_hand = p_timewarden_head_head.addOrReplaceChild("tw_hand", CubeListBuilder.create()
                .texOffs(130, 346).addBox(-0.5F, -2F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.15F, 0F, -0.05F))
                .texOffs(180, 311).addBox(-0.5F, -3.2F, -1.5F, 1F, 1F, 3F, new CubeDeformation(-0.15F, -0.2F, -0.15F))
                .texOffs(245, 353).addBox(-0.5F, -2.6F, -1.55F, 1F, 1F, 1F, new CubeDeformation(-0.15F, -0.2F, -0.2F))
                .texOffs(250, 353).addBox(-0.5F, -2.6F, 0.55F, 1F, 1F, 1F, new CubeDeformation(-0.15F, -0.2F, -0.2F))
                .texOffs(189, 311).addBox(-0.5F, -2.15F, -1.5F, 1F, 1F, 3F, new CubeDeformation(-0.15F, -0.25F, -0.15F))
                .texOffs(0, 321).addBox(-0.5F, -5.75F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.2F, -0.15F, -0.1F))
                .texOffs(198, 311).addBox(-0.5F, -6.6F, -1.5F, 1F, 1F, 3F, new CubeDeformation(-0.2F, 0F, -0.3F))
                .texOffs(189, 334).addBox(-0.5F, -7.5F, -1F, 1F, 1F, 2F, new CubeDeformation(-0.2F, -0.1F, -0.25F))
                .texOffs(0, 356).addBox(-0.5F, -8.25F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.25F, -0.15F, -0.2F)),
                PartPose.offsetAndRotation(0F, -9.6F, -0.6F, -0.2443F, 0F, 0F));
        PartDefinition p_tw_gear_left = p_timewarden_head_head.addOrReplaceChild("tw_gear_left", CubeListBuilder.create()
                .texOffs(195, 267).addBox(-2F, -2F, -0.5F, 4F, 4F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.15F)),
                PartPose.offsetAndRotation(5.35F, -5.4F, 0.4F, 0F, 1.5708F, 0F));
        PartDefinition p_tw_gear_right = p_timewarden_head_head.addOrReplaceChild("tw_gear_right", CubeListBuilder.create()
                .texOffs(206, 267).addBox(-2F, -2F, -0.5F, 4F, 4F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.15F)),
                PartPose.offsetAndRotation(-5.35F, -5.4F, 0.4F, 0F, -1.5708F, 0F));
        PartDefinition p_tw_gear_left_small = p_timewarden_head_head.addOrReplaceChild("tw_gear_left_small", CubeListBuilder.create()
                .texOffs(196, 334).addBox(-1F, -1F, -0.5F, 2F, 2F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.2F)),
                PartPose.offsetAndRotation(5.25F, -3.4F, -2.2F, 0F, 1.5708F, 0F));
        PartDefinition p_tw_gear_right_small = p_timewarden_head_head.addOrReplaceChild("tw_gear_right_small", CubeListBuilder.create()
                .texOffs(203, 334).addBox(-1F, -1F, -0.5F, 2F, 2F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.2F)),
                PartPose.offsetAndRotation(-5.25F, -3.4F, -2.2F, 0F, -1.5708F, 0F));
        PartDefinition p_timewarden_chest_body = root.addOrReplaceChild("timewarden_chest_body", CubeListBuilder.create()
                .texOffs(203, 208).addBox(-0.5F, 0.6F, 2.8F, 1F, 7F, 1F, new CubeDeformation(-0.2F, 0F, -0.25F))
                .texOffs(210, 334).addBox(-4.8F, -1F, -3.75F, 2F, 2F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.2F))
                .texOffs(217, 334).addBox(2.8F, -1F, -3.75F, 2F, 2F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.2F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_tw_dial = p_timewarden_chest_body.addOrReplaceChild("tw_dial", CubeListBuilder.create()
                .texOffs(182, 247).addBox(-2.5F, -2.5F, -0.775F, 5F, 5F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.225F)),
                PartPose.offset(0F, 3.6F, -3.35F));
        PartDefinition p_tw_bob = p_timewarden_chest_body.addOrReplaceChild("tw_bob", CubeListBuilder.create()
                .texOffs(217, 267).addBox(-2F, -2F, -0.5F, 4F, 4F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.15F)),
                PartPose.offset(0F, 8.6F, 3.45F));
        PartDefinition p_timewarden_chest_left_arm = root.addOrReplaceChild("timewarden_chest_left_arm", CubeListBuilder.create()
                .texOffs(0, 179).addBox(-2.5F, 5.85F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.2F, -0.15F, -0.2F))
                .texOffs(5, 356).addBox(0.5F, 6.35F, -3.95F, 1F, 1F, 1F, new CubeDeformation(0F, 0F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_tw_pauldron_left_arm = p_timewarden_chest_left_arm.addOrReplaceChild("tw_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(0, 104).addBox(-3.2F, -1.75F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F))
                .texOffs(116, 137).addBox(-2.15F, -0.25F, -4F, 6F, 2F, 8F, new CubeDeformation(-0.05F, -0.35F, -0.3F))
                .texOffs(170, 179).addBox(-1F, -2.4F, -4F, 2F, 1F, 8F, new CubeDeformation(-0.4F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(1F, -3.1F, 0F, 0F, 0F, 0.2094F));
        PartDefinition p_tw_shoulder_gear_left_arm = p_tw_pauldron_left_arm.addOrReplaceChild("tw_shoulder_gear_left_arm", CubeListBuilder.create()
                .texOffs(207, 311).addBox(-1.5F, -1.5F, -0.5F, 3F, 3F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.2F)),
                PartPose.offsetAndRotation(3.9F, -0.6F, 0F, 0F, 1.5708F, 0F));
        PartDefinition p_timewarden_chest_right_arm = root.addOrReplaceChild("timewarden_chest_right_arm", CubeListBuilder.create()
                .texOffs(29, 179).addBox(-4.5F, 5.85F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.2F, -0.15F, -0.2F))
                .texOffs(10, 356).addBox(-1.5F, 6.35F, -3.95F, 1F, 1F, 1F, new CubeDeformation(0F, 0F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_tw_pauldron_right_arm = p_timewarden_chest_right_arm.addOrReplaceChild("tw_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(31, 104).addBox(-3.8F, -1.75F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F))
                .texOffs(145, 137).addBox(-3.85F, -0.25F, -4F, 6F, 2F, 8F, new CubeDeformation(-0.05F, -0.35F, -0.3F))
                .texOffs(191, 179).addBox(-1F, -2.4F, -4F, 2F, 1F, 8F, new CubeDeformation(-0.4F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(-1F, -3.1F, 0F, 0F, 0F, -0.2094F));
        PartDefinition p_tw_shoulder_gear_right_arm = p_tw_pauldron_right_arm.addOrReplaceChild("tw_shoulder_gear_right_arm", CubeListBuilder.create()
                .texOffs(216, 311).addBox(-1.5F, -1.5F, -0.5F, 3F, 3F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.2F)),
                PartPose.offsetAndRotation(-3.9F, -0.6F, 0F, 0F, -1.5708F, 0F));
        PartDefinition p_timewarden_legs_body = root.addOrReplaceChild("timewarden_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_tw_buckle = p_timewarden_legs_body.addOrReplaceChild("tw_buckle", CubeListBuilder.create()
                .texOffs(225, 311).addBox(-1.5F, -1.5F, -0.5F, 3F, 3F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.2F)),
                PartPose.offset(0F, 10.9F, -3.4F));
        PartDefinition p_timewarden_legs_left_leg = root.addOrReplaceChild("timewarden_legs_left_leg", CubeListBuilder.create()
                .texOffs(78, 291).addBox(-2.5F, 3.45F, -3.275F, 5F, 3F, 1F, new CubeDeformation(-0.3F, -0.05F, -0.125F))
                .texOffs(224, 334).addBox(-1F, 3.9F, -3.875F, 2F, 2F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.225F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_timewarden_legs_right_leg = root.addOrReplaceChild("timewarden_legs_right_leg", CubeListBuilder.create()
                .texOffs(91, 291).addBox(-2.5F, 3.45F, -3.275F, 5F, 3F, 1F, new CubeDeformation(-0.3F, -0.05F, -0.125F))
                .texOffs(231, 334).addBox(-1F, 3.9F, -3.875F, 2F, 2F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.225F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_timewarden_feet_left_leg = root.addOrReplaceChild("timewarden_feet_left_leg", CubeListBuilder.create()
                .texOffs(225, 261).addBox(-2.5F, 10.1F, -4.4F, 5F, 3F, 2F, new CubeDeformation(0F, -0.3F, -0.3F))
                .texOffs(109, 321).addBox(-3F, 11.9F, -4.4F, 6F, 1F, 2F, new CubeDeformation(-0.4F, -0.1F, -0.2F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_tw_ankle_left_leg = p_timewarden_feet_left_leg.addOrReplaceChild("tw_ankle_left_leg", CubeListBuilder.create()
                .texOffs(234, 311).addBox(-1.5F, -1.5F, -0.5F, 3F, 3F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.25F)),
                PartPose.offsetAndRotation(3.05F, 8.4F, 0.4F, 0F, 1.5708F, 0F));
        PartDefinition p_timewarden_feet_right_leg = root.addOrReplaceChild("timewarden_feet_right_leg", CubeListBuilder.create()
                .texOffs(240, 261).addBox(-2.5F, 10.1F, -4.4F, 5F, 3F, 2F, new CubeDeformation(0F, -0.3F, -0.3F))
                .texOffs(126, 321).addBox(-3F, 11.9F, -4.4F, 6F, 1F, 2F, new CubeDeformation(-0.4F, -0.1F, -0.2F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_tw_ankle_right_leg = p_timewarden_feet_right_leg.addOrReplaceChild("tw_ankle_right_leg", CubeListBuilder.create()
                .texOffs(243, 311).addBox(-1.5F, -1.5F, -0.5F, 3F, 3F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.25F)),
                PartPose.offsetAndRotation(-3.05F, 8.4F, 0.4F, 0F, -1.5708F, 0F));
        PartDefinition p_emberwalker_head_head = root.addOrReplaceChild("emberwalker_head_head", CubeListBuilder.create()
                .texOffs(46, 148).addBox(-1F, -9.875F, -4.5F, 2F, 1F, 9F, new CubeDeformation(-0.3F, -0.075F, -0.1F))
                .texOffs(0, 316).addBox(-1.5F, -8.7F, -5.725F, 3F, 3F, 1F, new CubeDeformation(0F, -0.4F, -0.225F))
                .texOffs(238, 334).addBox(-1F, -8.2F, -6.2F, 2F, 2F, 1F, new CubeDeformation(0F, -0.2F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_ew_crest_front = p_emberwalker_head_head.addOrReplaceChild("ew_crest_front", CubeListBuilder.create()
                .texOffs(26, 267).addBox(-1.5F, -1.855F, -1.5F, 3F, 2F, 3F, new CubeDeformation(-0.45F, -0.145F, -0.45F))
                .texOffs(9, 316).addBox(-1F, -3.28F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.244F, -0.316F, -0.244F))
                .texOffs(15, 356).addBox(-0.5F, -3.806F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.08F, -0.006F, -0.08F)),
                PartPose.offsetAndRotation(0F, -9.6F, -2.9F, -0.1745F, 0F, 0F));
        PartDefinition p_ew_crest_mid = p_emberwalker_head_head.addOrReplaceChild("ew_crest_mid", CubeListBuilder.create()
                .texOffs(195, 247).addBox(-1.5F, -2.715F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.25F, -0.285F, -0.25F))
                .texOffs(18, 316).addBox(-1F, -4.24F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.1F, -0.028F, -0.1F))
                .texOffs(135, 346).addBox(-0.5F, -5.698F, -0.5F, 1F, 2F, 1F, new CubeDeformation(0F, -0.298F, 0F)),
                PartPose.offsetAndRotation(0F, -9.6F, -0.1F, -0.3491F, 0F, 0F));
        PartDefinition p_ew_crest_back = p_emberwalker_head_head.addOrReplaceChild("ew_crest_back", CubeListBuilder.create()
                .texOffs(39, 267).addBox(-1.5F, -1.945F, -1.5F, 3F, 2F, 3F, new CubeDeformation(-0.45F, -0.055F, -0.45F))
                .texOffs(27, 316).addBox(-1F, -3.52F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.244F, -0.244F, -0.244F))
                .texOffs(140, 346).addBox(-0.5F, -4.654F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.08F, -0.454F, -0.08F)),
                PartPose.offsetAndRotation(0F, -9.6F, 2.7F, -0.5585F, 0F, 0F));
        PartDefinition p_emberwalker_chest_body = root.addOrReplaceChild("emberwalker_chest_body", CubeListBuilder.create()
                .texOffs(228, 267).addBox(-2F, 2F, -3.725F, 4F, 4F, 1F, new CubeDeformation(0F, -0.2F, -0.225F))
                .texOffs(36, 316).addBox(-1.5F, 2.5F, -4.2F, 3F, 3F, 1F, new CubeDeformation(-0.05F, -0.15F, -0.2F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_emberwalker_chest_left_arm = root.addOrReplaceChild("emberwalker_chest_left_arm", CubeListBuilder.create()
                .texOffs(58, 179).addBox(-2.5F, 5.8F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.2F, -0.1F, -0.2F))
                .texOffs(209, 189).addBox(-2.5F, 6.325F, -3.5F, 7F, 1F, 7F, new CubeDeformation(-0.15F, -0.325F, -0.15F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_ew_pauldron_left_arm = p_emberwalker_chest_left_arm.addOrReplaceChild("ew_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(62, 104).addBox(-3.15F, -1.95F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.15F, -0.15F, -0.4F))
                .texOffs(174, 137).addBox(-2.1F, -0.1F, -4F, 6F, 2F, 8F, new CubeDeformation(0F, -0.3F, -0.3F))
                .texOffs(178, 148).addBox(-2.7F, -0.45F, -4F, 7F, 1F, 8F, new CubeDeformation(-0.4F, -0.3F, -0.35F))
                .texOffs(27, 326).addBox(0.35F, -2.625F, -1F, 2F, 1F, 2F, new CubeDeformation(-0.25F, -0.125F, -0.25F)),
                PartPose.offsetAndRotation(1F, -3.1F, 0F, 0F, 0F, 0.2443F));
        PartDefinition p_emberwalker_chest_right_arm = root.addOrReplaceChild("emberwalker_chest_right_arm", CubeListBuilder.create()
                .texOffs(87, 179).addBox(-4.5F, 5.8F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.2F, -0.1F, -0.2F))
                .texOffs(0, 199).addBox(-4.5F, 6.325F, -3.5F, 7F, 1F, 7F, new CubeDeformation(-0.15F, -0.325F, -0.15F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_ew_pauldron_right_arm = p_emberwalker_chest_right_arm.addOrReplaceChild("ew_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(93, 104).addBox(-3.85F, -1.95F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.15F, -0.15F, -0.4F))
                .texOffs(203, 137).addBox(-3.9F, -0.1F, -4F, 6F, 2F, 8F, new CubeDeformation(0F, -0.3F, -0.3F))
                .texOffs(209, 148).addBox(-4.3F, -0.45F, -4F, 7F, 1F, 8F, new CubeDeformation(-0.4F, -0.3F, -0.35F))
                .texOffs(36, 326).addBox(-2.35F, -2.625F, -1F, 2F, 1F, 2F, new CubeDeformation(-0.25F, -0.125F, -0.25F)),
                PartPose.offsetAndRotation(-1F, -3.1F, 0F, 0F, 0F, -0.2443F));
        PartDefinition p_emberwalker_legs_body = root.addOrReplaceChild("emberwalker_legs_body", CubeListBuilder.create()
                .texOffs(45, 316).addBox(-1.5F, 9.6F, -3.775F, 3F, 3F, 1F, new CubeDeformation(-0.2F, -0.4F, -0.275F))
                .texOffs(245, 334).addBox(-1F, 10.1F, -4.15F, 2F, 2F, 1F, new CubeDeformation(-0.2F, -0.25F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_emberwalker_legs_left_leg = root.addOrReplaceChild("emberwalker_legs_left_leg", CubeListBuilder.create()
                .texOffs(104, 291).addBox(-2.5F, 3.45F, -3.275F, 5F, 3F, 1F, new CubeDeformation(-0.3F, -0.05F, -0.125F))
                .texOffs(0, 338).addBox(-1F, 3.85F, -3.875F, 2F, 2F, 1F, new CubeDeformation(-0.35F, -0.35F, -0.225F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_emberwalker_legs_right_leg = root.addOrReplaceChild("emberwalker_legs_right_leg", CubeListBuilder.create()
                .texOffs(117, 291).addBox(-2.5F, 3.45F, -3.275F, 5F, 3F, 1F, new CubeDeformation(-0.3F, -0.05F, -0.125F))
                .texOffs(7, 338).addBox(-1F, 3.85F, -3.875F, 2F, 2F, 1F, new CubeDeformation(-0.35F, -0.35F, -0.225F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_emberwalker_feet_left_leg = root.addOrReplaceChild("emberwalker_feet_left_leg", CubeListBuilder.create()
                .texOffs(29, 199).addBox(-3F, 11.6F, -3.7F, 6F, 1F, 7F, new CubeDeformation(-0.25F, 0F, -0.3F))
                .texOffs(87, 285).addBox(-2.5F, 10F, -4.45F, 5F, 2F, 2F, new CubeDeformation(0F, -0.2F, -0.35F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_ew_heel_left_leg = p_emberwalker_feet_left_leg.addOrReplaceChild("ew_heel_left_leg", CubeListBuilder.create()
                .texOffs(54, 316).addBox(-1F, -1.675F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.2F, -0.325F, -0.2F))
                .texOffs(63, 316).addBox(-1F, -2.8F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.424F, -0.46F, -0.424F))
                .texOffs(20, 356).addBox(-0.5F, -3.11F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.18F, -0.11F, -0.18F)),
                PartPose.offsetAndRotation(0.9F, 11.2F, 3.1F, -0.9599F, 0F, 0F));
        PartDefinition p_emberwalker_feet_right_leg = root.addOrReplaceChild("emberwalker_feet_right_leg", CubeListBuilder.create()
                .texOffs(56, 199).addBox(-3F, 11.6F, -3.7F, 6F, 1F, 7F, new CubeDeformation(-0.25F, 0F, -0.3F))
                .texOffs(102, 285).addBox(-2.5F, 10F, -4.45F, 5F, 2F, 2F, new CubeDeformation(0F, -0.2F, -0.35F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_ew_heel_right_leg = p_emberwalker_feet_right_leg.addOrReplaceChild("ew_heel_right_leg", CubeListBuilder.create()
                .texOffs(72, 316).addBox(-1F, -1.675F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.2F, -0.325F, -0.2F))
                .texOffs(81, 316).addBox(-1F, -2.8F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.424F, -0.46F, -0.424F))
                .texOffs(25, 356).addBox(-0.5F, -3.11F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.18F, -0.11F, -0.18F)),
                PartPose.offsetAndRotation(-0.9F, 11.2F, 3.1F, -0.9599F, 0F, 0F));
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
