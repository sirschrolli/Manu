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
                .texOffs(121, 0).addBox(-0.5F, -10.75F, -5F, 1F, 2F, 10F, new CubeDeformation(0F, -0.15F, -0.4F))
                .texOffs(150, 166).addBox(-1F, -11.5F, 1.8F, 2F, 2F, 3F, new CubeDeformation(0F, -0.3F, -0.4F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_steel_plume_0 = p_steel_head_head.addOrReplaceChild("steel_plume_0", CubeListBuilder.create()
                .texOffs(105, 145).addBox(-1.5F, -2.8F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.2F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(0F, -10.6F, 3.3F, -0.2094F, 0F, 0F));
        PartDefinition p_steel_plume_1 = p_steel_plume_0.addOrReplaceChild("steel_plume_1", CubeListBuilder.create()
                .texOffs(211, 128).addBox(-1.5F, -3.6F, -1.5F, 3F, 4F, 3F, new CubeDeformation(0F, -0.4F, -0.1F)),
                PartPose.offsetAndRotation(0F, -2.35F, 0F, -0.4887F, 0F, 0F));
        PartDefinition p_steel_plume_2 = p_steel_plume_1.addOrReplaceChild("steel_plume_2", CubeListBuilder.create()
                .texOffs(224, 128).addBox(-1.5F, -3.8F, -1.5F, 3F, 4F, 3F, new CubeDeformation(-0.05F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(0F, -2.95F, 0F, -0.5585F, 0F, 0F));
        PartDefinition p_steel_plume_3 = p_steel_plume_2.addOrReplaceChild("steel_plume_3", CubeListBuilder.create()
                .texOffs(237, 128).addBox(-1.5F, -3.8F, -1.5F, 3F, 4F, 3F, new CubeDeformation(-0.25F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(0F, -3.35F, 0F, -0.5236F, 0F, 0F));
        PartDefinition p_steel_plume_4 = p_steel_plume_3.addOrReplaceChild("steel_plume_4", CubeListBuilder.create()
                .texOffs(138, 159).addBox(-1F, -3.7F, -1F, 2F, 4F, 2F, new CubeDeformation(0F, -0.3F, -0.1F)),
                PartPose.offsetAndRotation(0F, -3.35F, 0F, -0.384F, 0F, 0F));
        PartDefinition p_steel_plume_5 = p_steel_plume_4.addOrReplaceChild("steel_plume_5", CubeListBuilder.create()
                .texOffs(238, 166).addBox(-1F, -3F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.3F, 0F, -0.35F)),
                PartPose.offsetAndRotation(0F, -3.15F, 0F, -0.2443F, 0F, 0F));
        PartDefinition p_steel_chest_body = root.addOrReplaceChild("steel_chest_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_steel_chest_left_arm = root.addOrReplaceChild("steel_chest_left_arm", CubeListBuilder.create()
                .texOffs(92, 128).addBox(3.6F, 3.8F, -2F, 1F, 4F, 4F, new CubeDeformation(0F, -0.4F, -0.3F))
                .texOffs(18, 204).addBox(4.2F, 4.8F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.2F, -0.4F, -0.3F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_steel_pauldron_left_arm = p_steel_chest_left_arm.addOrReplaceChild("steel_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(41, 17).addBox(-3.8F, -2.15F, -4F, 8F, 3F, 8F, new CubeDeformation(-0.4F, -0.45F, -0.3F))
                .texOffs(0, 41).addBox(-2.5F, 0.1F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.2F, -0.1F, -0.2F))
                .texOffs(62, 74).addBox(-1.3F, 1.6F, -4F, 6F, 2F, 8F, new CubeDeformation(-0.2F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.2443F));
        PartDefinition p_steel_chest_right_arm = root.addOrReplaceChild("steel_chest_right_arm", CubeListBuilder.create()
                .texOffs(103, 128).addBox(-4.6F, 3.8F, -2F, 1F, 4F, 4F, new CubeDeformation(0F, -0.4F, -0.3F))
                .texOffs(25, 204).addBox(-5.2F, 4.8F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.2F, -0.4F, -0.3F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_steel_pauldron_right_arm = p_steel_chest_right_arm.addOrReplaceChild("steel_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(74, 17).addBox(-4.2F, -2.15F, -4F, 8F, 3F, 8F, new CubeDeformation(-0.4F, -0.45F, -0.3F))
                .texOffs(31, 41).addBox(-4.5F, 0.1F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.2F, -0.1F, -0.2F))
                .texOffs(91, 74).addBox(-4.7F, 1.6F, -4F, 6F, 2F, 8F, new CubeDeformation(-0.2F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.2443F));
        PartDefinition p_steel_legs_body = root.addOrReplaceChild("steel_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_steel_legs_left_leg = root.addOrReplaceChild("steel_legs_left_leg", CubeListBuilder.create()
                .texOffs(39, 184).addBox(-2F, 5F, -3.3F, 4F, 3F, 1F, new CubeDeformation(0F, -0.1F, 0F))
                .texOffs(247, 166).addBox(2F, 5.5F, -3.2F, 1F, 2F, 3F, new CubeDeformation(-0.1F, 0F, -0.4F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_steel_tasset_left_leg = p_steel_legs_left_leg.addOrReplaceChild("steel_tasset_left_leg", CubeListBuilder.create()
                .texOffs(240, 137).addBox(-3F, -0.1F, -0.6F, 6F, 5F, 1F, new CubeDeformation(-0.2F, -0.1F, 0F)),
                PartPose.offsetAndRotation(0F, -0.8F, -2.8F, -0.1222F, 0F, 0F));
        PartDefinition p_steel_legs_right_leg = root.addOrReplaceChild("steel_legs_right_leg", CubeListBuilder.create()
                .texOffs(50, 184).addBox(-2F, 5F, -3.3F, 4F, 3F, 1F, new CubeDeformation(0F, -0.1F, 0F))
                .texOffs(0, 172).addBox(-3F, 5.5F, -3.2F, 1F, 2F, 3F, new CubeDeformation(-0.1F, 0F, -0.4F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_steel_tasset_right_leg = p_steel_legs_right_leg.addOrReplaceChild("steel_tasset_right_leg", CubeListBuilder.create()
                .texOffs(0, 145).addBox(-3F, -0.1F, -0.6F, 6F, 5F, 1F, new CubeDeformation(-0.2F, -0.1F, 0F)),
                PartPose.offsetAndRotation(0F, -0.8F, -2.8F, -0.1222F, 0F, 0F));
        PartDefinition p_steel_feet_left_leg = root.addOrReplaceChild("steel_feet_left_leg", CubeListBuilder.create()
                .texOffs(217, 159).addBox(-2.5F, 10.2F, -4.45F, 5F, 3F, 2F, new CubeDeformation(0F, -0.3F, -0.25F))
                .texOffs(231, 204).addBox(-0.5F, 10.45F, 2.75F, 1F, 1F, 2F, new CubeDeformation(-0.15F, -0.15F, -0.05F))
                .texOffs(238, 204).addBox(-1F, 9.95F, 4.25F, 2F, 2F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_steel_feet_right_leg = root.addOrReplaceChild("steel_feet_right_leg", CubeListBuilder.create()
                .texOffs(232, 159).addBox(-2.5F, 10.2F, -4.45F, 5F, 3F, 2F, new CubeDeformation(0F, -0.3F, -0.25F))
                .texOffs(245, 204).addBox(-0.5F, 10.45F, 2.75F, 1F, 1F, 2F, new CubeDeformation(-0.15F, -0.15F, -0.05F))
                .texOffs(0, 209).addBox(-1F, 9.95F, 4.25F, 2F, 2F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_konterit_head_head = root.addOrReplaceChild("konterit_head_head", CubeListBuilder.create()
                .texOffs(7, 209).addBox(-1F, -6.8F, -5.75F, 2F, 2F, 1F, new CubeDeformation(0F, 0F, -0.15F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_kon_wing_left = p_konterit_head_head.addOrReplaceChild("kon_wing_left", CubeListBuilder.create()
                .texOffs(104, 137).addBox(-1F, -2.58F, -1.79F, 2F, 4F, 3F, new CubeDeformation(-0.4F, -0.26F, -0.05F)),
                PartPose.offsetAndRotation(5.2F, -5.2F, -0.8F, 0.2793F, 0.2793F, 0F));
        PartDefinition p_kon_wing_left_feather_0 = p_kon_wing_left.addOrReplaceChild("kon_wing_left_feather_0", CubeListBuilder.create()
                .texOffs(58, 118).addBox(-0.5F, -1F, -0.165F, 1F, 2F, 7F, new CubeDeformation(-0.25F, -0.0575F, -0.165F)),
                PartPose.offsetAndRotation(0F, -0.58F, 0.58F, 0.0698F, 0F, 0F));
        PartDefinition p_kon_wing_left_feather_1 = p_kon_wing_left.addOrReplaceChild("kon_wing_left_feather_1", CubeListBuilder.create()
                .texOffs(135, 29).addBox(-0.5F, -1F, -0.44F, 1F, 2F, 9F, new CubeDeformation(-0.25F, -0.0575F, -0.44F)),
                PartPose.offsetAndRotation(0F, -0.9425F, 0.58F, 0.3491F, 0F, 0F));
        PartDefinition p_kon_wing_left_feather_2 = p_kon_wing_left.addOrReplaceChild("kon_wing_left_feather_2", CubeListBuilder.create()
                .texOffs(156, 29).addBox(-0.5F, -1F, -0.295F, 1F, 2F, 9F, new CubeDeformation(-0.25F, -0.13F, -0.295F)),
                PartPose.offsetAndRotation(0F, -1.305F, 0.58F, 0.6283F, 0F, 0F));
        PartDefinition p_kon_wing_left_feather_3 = p_kon_wing_left.addOrReplaceChild("kon_wing_left_feather_3", CubeListBuilder.create()
                .texOffs(23, 107).addBox(-0.5F, -1F, -0.375F, 1F, 2F, 8F, new CubeDeformation(-0.25F, -0.2025F, -0.375F)),
                PartPose.offsetAndRotation(0F, -1.6675F, 0.58F, 0.9076F, 0F, 0F));
        PartDefinition p_kon_wing_left_feather_4 = p_kon_wing_left.addOrReplaceChild("kon_wing_left_feather_4", CubeListBuilder.create()
                .texOffs(19, 128).addBox(-0.5F, -1F, -0.245F, 1F, 2F, 6F, new CubeDeformation(-0.25F, -0.275F, -0.245F)),
                PartPose.offsetAndRotation(0F, -2.03F, 0.58F, 1.1868F, 0F, 0F));
        PartDefinition p_kon_wing_right = p_konterit_head_head.addOrReplaceChild("kon_wing_right", CubeListBuilder.create()
                .texOffs(115, 137).addBox(-1F, -2.58F, -1.79F, 2F, 4F, 3F, new CubeDeformation(-0.4F, -0.26F, -0.05F)),
                PartPose.offsetAndRotation(-5.2F, -5.2F, -0.8F, 0.2793F, -0.2793F, 0F));
        PartDefinition p_kon_wing_right_feather_0 = p_kon_wing_right.addOrReplaceChild("kon_wing_right_feather_0", CubeListBuilder.create()
                .texOffs(75, 118).addBox(-0.5F, -1F, -0.165F, 1F, 2F, 7F, new CubeDeformation(-0.25F, -0.0575F, -0.165F)),
                PartPose.offsetAndRotation(0F, -0.58F, 0.58F, 0.0698F, 0F, 0F));
        PartDefinition p_kon_wing_right_feather_1 = p_kon_wing_right.addOrReplaceChild("kon_wing_right_feather_1", CubeListBuilder.create()
                .texOffs(177, 29).addBox(-0.5F, -1F, -0.44F, 1F, 2F, 9F, new CubeDeformation(-0.25F, -0.0575F, -0.44F)),
                PartPose.offsetAndRotation(0F, -0.9425F, 0.58F, 0.3491F, 0F, 0F));
        PartDefinition p_kon_wing_right_feather_2 = p_kon_wing_right.addOrReplaceChild("kon_wing_right_feather_2", CubeListBuilder.create()
                .texOffs(198, 29).addBox(-0.5F, -1F, -0.295F, 1F, 2F, 9F, new CubeDeformation(-0.25F, -0.13F, -0.295F)),
                PartPose.offsetAndRotation(0F, -1.305F, 0.58F, 0.6283F, 0F, 0F));
        PartDefinition p_kon_wing_right_feather_3 = p_kon_wing_right.addOrReplaceChild("kon_wing_right_feather_3", CubeListBuilder.create()
                .texOffs(42, 107).addBox(-0.5F, -1F, -0.375F, 1F, 2F, 8F, new CubeDeformation(-0.25F, -0.2025F, -0.375F)),
                PartPose.offsetAndRotation(0F, -1.6675F, 0.58F, 0.9076F, 0F, 0F));
        PartDefinition p_kon_wing_right_feather_4 = p_kon_wing_right.addOrReplaceChild("kon_wing_right_feather_4", CubeListBuilder.create()
                .texOffs(34, 128).addBox(-0.5F, -1F, -0.245F, 1F, 2F, 6F, new CubeDeformation(-0.25F, -0.275F, -0.245F)),
                PartPose.offsetAndRotation(0F, -2.03F, 0.58F, 1.1868F, 0F, 0F));
        PartDefinition p_konterit_chest_body = root.addOrReplaceChild("konterit_chest_body", CubeListBuilder.create()
                .texOffs(14, 209).addBox(-5.2F, -1.1F, -3.65F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.3F, -0.15F))
                .texOffs(21, 209).addBox(3.2F, -1.1F, -3.65F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.3F, -0.15F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_kon_cape = p_konterit_chest_body.addOrReplaceChild("kon_cape", CubeListBuilder.create()
                .texOffs(209, 118).addBox(-4.5F, -0.2F, -0.2F, 9F, 7F, 1F, new CubeDeformation(0F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(0F, 0.2F, 3F, 0.1047F, 0F, 0F));
        PartDefinition p_kon_cape_hem = p_kon_cape.addOrReplaceChild("kon_cape_hem", CubeListBuilder.create()
                .texOffs(173, 128).addBox(-4.5F, -0.4F, -0.2F, 9F, 6F, 1F, new CubeDeformation(-0.2F, -0.4F, -0.2F)),
                PartPose.offsetAndRotation(0F, 6.4F, 0.05F, 0.1396F, 0F, 0F));
        PartDefinition p_konterit_chest_left_arm = root.addOrReplaceChild("konterit_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_kon_pauldron_left_arm = p_konterit_chest_left_arm.addOrReplaceChild("kon_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(61, 107).addBox(-3.2F, -1.55F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.2F, -0.25F, 0F))
                .texOffs(230, 118).addBox(-1F, -2.05F, -3.5F, 2F, 1F, 7F, new CubeDeformation(-0.4F, -0.15F, 0F))
                .texOffs(195, 204).addBox(1F, -2.2F, -1F, 2F, 1F, 2F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offsetAndRotation(1F, -3.2F, 0F, 0F, 0F, 0.2094F));
        PartDefinition p_konterit_chest_right_arm = root.addOrReplaceChild("konterit_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_kon_pauldron_right_arm = p_konterit_chest_right_arm.addOrReplaceChild("kon_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(90, 107).addBox(-3.8F, -1.55F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.2F, -0.25F, 0F))
                .texOffs(0, 128).addBox(-1F, -2.05F, -3.5F, 2F, 1F, 7F, new CubeDeformation(-0.4F, -0.15F, 0F))
                .texOffs(204, 204).addBox(-3F, -2.2F, -1F, 2F, 1F, 2F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offsetAndRotation(-1F, -3.2F, 0F, 0F, 0F, -0.2094F));
        PartDefinition p_konterit_legs_body = root.addOrReplaceChild("konterit_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_konterit_legs_left_leg = root.addOrReplaceChild("konterit_legs_left_leg", CubeListBuilder.create()
                .texOffs(9, 172).addBox(-1.5F, 4.2F, -3.1F, 3F, 4F, 1F, new CubeDeformation(0F, -0.4F, -0.1F))
                .texOffs(28, 209).addBox(-1F, 5.2F, -3.65F, 2F, 2F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_konterit_legs_right_leg = root.addOrReplaceChild("konterit_legs_right_leg", CubeListBuilder.create()
                .texOffs(18, 172).addBox(-1.5F, 4.2F, -3.1F, 3F, 4F, 1F, new CubeDeformation(0F, -0.4F, -0.1F))
                .texOffs(35, 209).addBox(-1F, 5.2F, -3.65F, 2F, 2F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_konterit_feet_left_leg = root.addOrReplaceChild("konterit_feet_left_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_kon_heel_left_leg = p_konterit_feet_left_leg.addOrReplaceChild("kon_heel_left_leg", CubeListBuilder.create()
                .texOffs(27, 172).addBox(-1F, -1.84F, -1.17F, 2F, 3F, 2F, new CubeDeformation(-0.4F, -0.48F, -0.15F)),
                PartPose.offsetAndRotation(2.9F, 9.6F, 0.2F, 0.4189F, 0.2443F, 0F));
        PartDefinition p_kon_heel_left_leg_feather_0 = p_kon_heel_left_leg.addOrReplaceChild("kon_heel_left_leg_feather_0", CubeListBuilder.create()
                .texOffs(39, 159).addBox(-0.5F, -1F, -0.045F, 1F, 2F, 4F, new CubeDeformation(-0.25F, -0.4475F, -0.045F)),
                PartPose.offsetAndRotation(0F, -0.34F, 0.34F, 0.0698F, 0F, 0F));
        PartDefinition p_kon_heel_left_leg_feather_1 = p_kon_heel_left_leg.addOrReplaceChild("kon_heel_left_leg_feather_1", CubeListBuilder.create()
                .texOffs(0, 137).addBox(-0.5F, -1F, -0.12F, 1F, 2F, 5F, new CubeDeformation(-0.25F, -0.4475F, -0.12F)),
                PartPose.offsetAndRotation(0F, -0.5525F, 0.34F, 0.3491F, 0F, 0F));
        PartDefinition p_kon_heel_left_leg_feather_2 = p_kon_heel_left_leg.addOrReplaceChild("kon_heel_left_leg_feather_2", CubeListBuilder.create()
                .texOffs(13, 137).addBox(-0.5F, -1F, -0.035F, 1F, 2F, 5F, new CubeDeformation(-0.25F, -0.49F, -0.035F)),
                PartPose.offsetAndRotation(0F, -0.765F, 0.34F, 0.6283F, 0F, 0F));
        PartDefinition p_kon_heel_left_leg_feather_3 = p_kon_heel_left_leg.addOrReplaceChild("kon_heel_left_leg_feather_3", CubeListBuilder.create()
                .texOffs(118, 145).addBox(-0.5F, -0.5F, -0.375F, 1F, 1F, 5F, new CubeDeformation(-0.25F, -0.0325F, -0.375F)),
                PartPose.offsetAndRotation(0F, -0.9775F, 0.34F, 0.9076F, 0F, 0F));
        PartDefinition p_kon_heel_left_leg_feather_4 = p_kon_heel_left_leg.addOrReplaceChild("kon_heel_left_leg_feather_4", CubeListBuilder.create()
                .texOffs(161, 166).addBox(-0.5F, -0.5F, -0.385F, 1F, 1F, 4F, new CubeDeformation(-0.25F, -0.075F, -0.385F)),
                PartPose.offsetAndRotation(0F, -1.19F, 0.34F, 1.1868F, 0F, 0F));
        PartDefinition p_konterit_feet_right_leg = root.addOrReplaceChild("konterit_feet_right_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_kon_heel_right_leg = p_konterit_feet_right_leg.addOrReplaceChild("kon_heel_right_leg", CubeListBuilder.create()
                .texOffs(36, 172).addBox(-1F, -1.84F, -1.17F, 2F, 3F, 2F, new CubeDeformation(-0.4F, -0.48F, -0.15F)),
                PartPose.offsetAndRotation(-2.9F, 9.6F, 0.2F, 0.4189F, -0.2443F, 0F));
        PartDefinition p_kon_heel_right_leg_feather_0 = p_kon_heel_right_leg.addOrReplaceChild("kon_heel_right_leg_feather_0", CubeListBuilder.create()
                .texOffs(50, 159).addBox(-0.5F, -1F, -0.045F, 1F, 2F, 4F, new CubeDeformation(-0.25F, -0.4475F, -0.045F)),
                PartPose.offsetAndRotation(0F, -0.34F, 0.34F, 0.0698F, 0F, 0F));
        PartDefinition p_kon_heel_right_leg_feather_1 = p_kon_heel_right_leg.addOrReplaceChild("kon_heel_right_leg_feather_1", CubeListBuilder.create()
                .texOffs(26, 137).addBox(-0.5F, -1F, -0.12F, 1F, 2F, 5F, new CubeDeformation(-0.25F, -0.4475F, -0.12F)),
                PartPose.offsetAndRotation(0F, -0.5525F, 0.34F, 0.3491F, 0F, 0F));
        PartDefinition p_kon_heel_right_leg_feather_2 = p_kon_heel_right_leg.addOrReplaceChild("kon_heel_right_leg_feather_2", CubeListBuilder.create()
                .texOffs(39, 137).addBox(-0.5F, -1F, -0.035F, 1F, 2F, 5F, new CubeDeformation(-0.25F, -0.49F, -0.035F)),
                PartPose.offsetAndRotation(0F, -0.765F, 0.34F, 0.6283F, 0F, 0F));
        PartDefinition p_kon_heel_right_leg_feather_3 = p_kon_heel_right_leg.addOrReplaceChild("kon_heel_right_leg_feather_3", CubeListBuilder.create()
                .texOffs(131, 145).addBox(-0.5F, -0.5F, -0.375F, 1F, 1F, 5F, new CubeDeformation(-0.25F, -0.0325F, -0.375F)),
                PartPose.offsetAndRotation(0F, -0.9775F, 0.34F, 0.9076F, 0F, 0F));
        PartDefinition p_kon_heel_right_leg_feather_4 = p_kon_heel_right_leg.addOrReplaceChild("kon_heel_right_leg_feather_4", CubeListBuilder.create()
                .texOffs(172, 166).addBox(-0.5F, -0.5F, -0.385F, 1F, 1F, 4F, new CubeDeformation(-0.25F, -0.075F, -0.385F)),
                PartPose.offsetAndRotation(0F, -1.19F, 0.34F, 1.1868F, 0F, 0F));
        PartDefinition p_nightsteel_head_head = root.addOrReplaceChild("nightsteel_head_head", CubeListBuilder.create()
                .texOffs(107, 204).addBox(-5.5F, -9.5F, -3.9F, 11F, 1F, 2F, new CubeDeformation(-0.2F, 0F, -0.4F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_night_crown_0_0 = p_nightsteel_head_head.addOrReplaceChild("night_crown_0_0", CubeListBuilder.create()
                .texOffs(99, 189).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.25F, -0.285F, -0.25F)),
                PartPose.offsetAndRotation(-3.6F, -9.3F, -2.9F, -0.1396F, 0F, -0.377F));
        PartDefinition p_night_crown_0_1 = p_night_crown_0_0.addOrReplaceChild("night_crown_0_1", CubeListBuilder.create()
                .texOffs(245, 213).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.0875F, -0.35F, -0.0875F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_night_crown_1_0 = p_nightsteel_head_head.addOrReplaceChild("night_crown_1_0", CubeListBuilder.create()
                .texOffs(45, 172).addBox(-1F, -2.545F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.25F, -0.455F, -0.25F)),
                PartPose.offsetAndRotation(-1.8F, -9.3F, -2.9F, -0.1396F, 0F, -0.1885F));
        PartDefinition p_night_crown_1_1 = p_night_crown_1_0.addOrReplaceChild("night_crown_1_1", CubeListBuilder.create()
                .texOffs(250, 213).addBox(-0.5F, -1.95F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.0875F, -0.05F, -0.0875F)),
                PartPose.offset(0F, -1.84F, 0F));
        PartDefinition p_night_crown_2_0 = p_nightsteel_head_head.addOrReplaceChild("night_crown_2_0", CubeListBuilder.create()
                .texOffs(54, 172).addBox(-1F, -2.93F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.1F, -0.07F, -0.1F)),
                PartPose.offsetAndRotation(0F, -9.3F, -2.9F, -0.1396F, 0F, 0F));
        PartDefinition p_night_crown_2_1 = p_night_crown_2_0.addOrReplaceChild("night_crown_2_1", CubeListBuilder.create()
                .texOffs(67, 204).addBox(-0.5F, -2.8F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.005F, -0.2F, -0.005F)),
                PartPose.offset(0F, -2.61F, 0F));
        PartDefinition p_night_crown_3_0 = p_nightsteel_head_head.addOrReplaceChild("night_crown_3_0", CubeListBuilder.create()
                .texOffs(63, 172).addBox(-1F, -2.545F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.25F, -0.455F, -0.25F)),
                PartPose.offsetAndRotation(1.8F, -9.3F, -2.9F, -0.1396F, 0F, 0.1885F));
        PartDefinition p_night_crown_3_1 = p_night_crown_3_0.addOrReplaceChild("night_crown_3_1", CubeListBuilder.create()
                .texOffs(0, 217).addBox(-0.5F, -1.95F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.0875F, -0.05F, -0.0875F)),
                PartPose.offset(0F, -1.84F, 0F));
        PartDefinition p_night_crown_4_0 = p_nightsteel_head_head.addOrReplaceChild("night_crown_4_0", CubeListBuilder.create()
                .texOffs(108, 189).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.25F, -0.285F, -0.25F)),
                PartPose.offsetAndRotation(3.6F, -9.3F, -2.9F, -0.1396F, 0F, 0.377F));
        PartDefinition p_night_crown_4_1 = p_night_crown_4_0.addOrReplaceChild("night_crown_4_1", CubeListBuilder.create()
                .texOffs(5, 217).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.0875F, -0.35F, -0.0875F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_nightsteel_chest_body = root.addOrReplaceChild("nightsteel_chest_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_night_back_0_0 = p_nightsteel_chest_body.addOrReplaceChild("night_back_0_0", CubeListBuilder.create()
                .texOffs(72, 172).addBox(-1F, -2.6F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.2F, -0.4F, -0.2F)),
                PartPose.offsetAndRotation(0F, 1.6F, 2.9F, -0.9076F, 0F, 0F));
        PartDefinition p_night_back_0_1 = p_night_back_0_0.addOrReplaceChild("night_back_0_1", CubeListBuilder.create()
                .texOffs(10, 217).addBox(-0.5F, -2F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.06F, 0F, -0.06F)),
                PartPose.offset(0F, -1.95F, 0F));
        PartDefinition p_night_back_1_0 = p_nightsteel_chest_body.addOrReplaceChild("night_back_1_0", CubeListBuilder.create()
                .texOffs(117, 189).addBox(-1F, -1.88F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.2F, -0.12F, -0.2F)),
                PartPose.offsetAndRotation(0F, 4.6F, 2.9F, -0.9076F, 0F, 0F));
        PartDefinition p_night_back_1_1 = p_night_back_1_0.addOrReplaceChild("night_back_1_1", CubeListBuilder.create()
                .texOffs(15, 217).addBox(-0.5F, -1.8F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.06F, -0.2F, -0.06F)),
                PartPose.offset(0F, -1.51F, 0F));
        PartDefinition p_night_back_2_0 = p_nightsteel_chest_body.addOrReplaceChild("night_back_2_0", CubeListBuilder.create()
                .texOffs(126, 189).addBox(-1F, -1.66F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.2F, -0.34F, -0.2F)),
                PartPose.offsetAndRotation(0F, 7.6F, 2.9F, -0.9076F, 0F, 0F));
        PartDefinition p_night_back_2_1 = p_night_back_2_0.addOrReplaceChild("night_back_2_1", CubeListBuilder.create()
                .texOffs(20, 217).addBox(-0.5F, -1.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.06F, -0.4F, -0.06F)),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_nightsteel_chest_left_arm = root.addOrReplaceChild("nightsteel_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_night_pauldron_left_arm = p_nightsteel_chest_left_arm.addOrReplaceChild("night_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(62, 41).addBox(-3.3F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.1745F));
        PartDefinition p_night_shoulder_left_arm_0_0 = p_night_pauldron_left_arm.addOrReplaceChild("night_shoulder_left_arm_0_0", CubeListBuilder.create()
                .texOffs(135, 189).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.285F, -0.3F)),
                PartPose.offsetAndRotation(2.2F, -1.4F, -2.2F, 0F, 0F, 0.4887F));
        PartDefinition p_night_shoulder_left_arm_0_1 = p_night_shoulder_left_arm_0_0.addOrReplaceChild("night_shoulder_left_arm_0_1", CubeListBuilder.create()
                .texOffs(25, 217).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.35F, -0.115F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_night_shoulder_left_arm_1_0 = p_night_pauldron_left_arm.addOrReplaceChild("night_shoulder_left_arm_1_0", CubeListBuilder.create()
                .texOffs(144, 189).addBox(-1F, -1.99F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.01F, -0.3F)),
                PartPose.offsetAndRotation(2.2F, -1.4F, 0F, 0F, 0F, 0.4887F));
        PartDefinition p_night_shoulder_left_arm_1_1 = p_night_shoulder_left_arm_1_0.addOrReplaceChild("night_shoulder_left_arm_1_1", CubeListBuilder.create()
                .texOffs(30, 217).addBox(-0.5F, -1.9F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.1F, -0.115F)),
                PartPose.offset(0F, -1.73F, 0F));
        PartDefinition p_night_shoulder_left_arm_2_0 = p_night_pauldron_left_arm.addOrReplaceChild("night_shoulder_left_arm_2_0", CubeListBuilder.create()
                .texOffs(153, 189).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.285F, -0.3F)),
                PartPose.offsetAndRotation(2.2F, -1.4F, 2.2F, 0F, 0F, 0.4887F));
        PartDefinition p_night_shoulder_left_arm_2_1 = p_night_shoulder_left_arm_2_0.addOrReplaceChild("night_shoulder_left_arm_2_1", CubeListBuilder.create()
                .texOffs(35, 217).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.35F, -0.115F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_nightsteel_chest_right_arm = root.addOrReplaceChild("nightsteel_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_night_pauldron_right_arm = p_nightsteel_chest_right_arm.addOrReplaceChild("night_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(93, 41).addBox(-3.7F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.1745F));
        PartDefinition p_night_shoulder_right_arm_0_0 = p_night_pauldron_right_arm.addOrReplaceChild("night_shoulder_right_arm_0_0", CubeListBuilder.create()
                .texOffs(162, 189).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.285F, -0.3F)),
                PartPose.offsetAndRotation(-2.2F, -1.4F, -2.2F, 0F, 0F, -0.4887F));
        PartDefinition p_night_shoulder_right_arm_0_1 = p_night_shoulder_right_arm_0_0.addOrReplaceChild("night_shoulder_right_arm_0_1", CubeListBuilder.create()
                .texOffs(40, 217).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.35F, -0.115F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_night_shoulder_right_arm_1_0 = p_night_pauldron_right_arm.addOrReplaceChild("night_shoulder_right_arm_1_0", CubeListBuilder.create()
                .texOffs(171, 189).addBox(-1F, -1.99F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.01F, -0.3F)),
                PartPose.offsetAndRotation(-2.2F, -1.4F, 0F, 0F, 0F, -0.4887F));
        PartDefinition p_night_shoulder_right_arm_1_1 = p_night_shoulder_right_arm_1_0.addOrReplaceChild("night_shoulder_right_arm_1_1", CubeListBuilder.create()
                .texOffs(45, 217).addBox(-0.5F, -1.9F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.1F, -0.115F)),
                PartPose.offset(0F, -1.73F, 0F));
        PartDefinition p_night_shoulder_right_arm_2_0 = p_night_pauldron_right_arm.addOrReplaceChild("night_shoulder_right_arm_2_0", CubeListBuilder.create()
                .texOffs(180, 189).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.285F, -0.3F)),
                PartPose.offsetAndRotation(-2.2F, -1.4F, 2.2F, 0F, 0F, -0.4887F));
        PartDefinition p_night_shoulder_right_arm_2_1 = p_night_shoulder_right_arm_2_0.addOrReplaceChild("night_shoulder_right_arm_2_1", CubeListBuilder.create()
                .texOffs(50, 217).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.35F, -0.115F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_nightsteel_legs_body = root.addOrReplaceChild("nightsteel_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_nightsteel_legs_left_leg = root.addOrReplaceChild("nightsteel_legs_left_leg", CubeListBuilder.create()
                .texOffs(61, 184).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(-0.1F, 0F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_night_knee_left_leg_0 = p_nightsteel_legs_left_leg.addOrReplaceChild("night_knee_left_leg_0", CubeListBuilder.create()
                .texOffs(189, 189).addBox(-1F, -1.77F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.23F, -0.35F)),
                PartPose.offsetAndRotation(0F, 6F, -3F, 1.0821F, 0F, 0F));
        PartDefinition p_night_knee_left_leg_1 = p_night_knee_left_leg_0.addOrReplaceChild("night_knee_left_leg_1", CubeListBuilder.create()
                .texOffs(55, 217).addBox(-0.5F, -1.7F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.3F, -0.1425F)),
                PartPose.offset(0F, -1.29F, 0F));
        PartDefinition p_nightsteel_legs_right_leg = root.addOrReplaceChild("nightsteel_legs_right_leg", CubeListBuilder.create()
                .texOffs(72, 184).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(-0.1F, 0F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_night_knee_right_leg_0 = p_nightsteel_legs_right_leg.addOrReplaceChild("night_knee_right_leg_0", CubeListBuilder.create()
                .texOffs(198, 189).addBox(-1F, -1.77F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.23F, -0.35F)),
                PartPose.offsetAndRotation(0F, 6F, -3F, 1.0821F, 0F, 0F));
        PartDefinition p_night_knee_right_leg_1 = p_night_knee_right_leg_0.addOrReplaceChild("night_knee_right_leg_1", CubeListBuilder.create()
                .texOffs(60, 217).addBox(-0.5F, -1.7F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.3F, -0.1425F)),
                PartPose.offset(0F, -1.29F, 0F));
        PartDefinition p_nightsteel_feet_left_leg = root.addOrReplaceChild("nightsteel_feet_left_leg", CubeListBuilder.create()
                .texOffs(244, 178).addBox(-2.5F, 10.25F, -3.9F, 5F, 3F, 1F, new CubeDeformation(-0.1F, -0.45F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_night_heel_left_leg_0 = p_nightsteel_feet_left_leg.addOrReplaceChild("night_heel_left_leg_0", CubeListBuilder.create()
                .texOffs(207, 189).addBox(-1F, -1.66F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.34F, -0.4F)),
                PartPose.offsetAndRotation(0F, 11F, 2.8F, -1.0123F, 0F, 0F));
        PartDefinition p_night_heel_left_leg_1 = p_night_heel_left_leg_0.addOrReplaceChild("night_heel_left_leg_1", CubeListBuilder.create()
                .texOffs(65, 217).addBox(-0.5F, -1.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.17F, -0.4F, -0.17F)),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_nightsteel_feet_right_leg = root.addOrReplaceChild("nightsteel_feet_right_leg", CubeListBuilder.create()
                .texOffs(0, 184).addBox(-2.5F, 10.25F, -3.9F, 5F, 3F, 1F, new CubeDeformation(-0.1F, -0.45F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_night_heel_right_leg_0 = p_nightsteel_feet_right_leg.addOrReplaceChild("night_heel_right_leg_0", CubeListBuilder.create()
                .texOffs(216, 189).addBox(-1F, -1.66F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.34F, -0.4F)),
                PartPose.offsetAndRotation(0F, 11F, 2.8F, -1.0123F, 0F, 0F));
        PartDefinition p_night_heel_right_leg_1 = p_night_heel_right_leg_0.addOrReplaceChild("night_heel_right_leg_1", CubeListBuilder.create()
                .texOffs(70, 217).addBox(-0.5F, -1.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.17F, -0.4F, -0.17F)),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_abyssite_head_head = root.addOrReplaceChild("abyssite_head_head", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_aby_horn_left_0 = p_abyssite_head_head.addOrReplaceChild("aby_horn_left_0", CubeListBuilder.create()
                .texOffs(144, 145).addBox(-1.5F, -3F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offsetAndRotation(4F, -8.6F, -1.2F, -0.2094F, 0F, 0.5934F));
        PartDefinition p_aby_horn_left_1 = p_aby_horn_left_0.addOrReplaceChild("aby_horn_left_1", CubeListBuilder.create()
                .texOffs(157, 145).addBox(-1.5F, -3F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.45F, 0F, -0.45F)),
                PartPose.offsetAndRotation(0F, -2.75F, 0F, -0.4887F, 0F, -0.1745F));
        PartDefinition p_aby_horn_left_2 = p_aby_horn_left_1.addOrReplaceChild("aby_horn_left_2", CubeListBuilder.create()
                .texOffs(81, 172).addBox(-1F, -2.9F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.2F, -0.1F, -0.2F)),
                PartPose.offsetAndRotation(0F, -2.75F, 0F, -0.5934F, 0F, -0.2094F));
        PartDefinition p_aby_horn_left_3 = p_aby_horn_left_2.addOrReplaceChild("aby_horn_left_3", CubeListBuilder.create()
                .texOffs(90, 172).addBox(-1F, -2.7F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.45F, -0.3F, -0.45F)),
                PartPose.offsetAndRotation(0F, -2.55F, 0F, -0.6283F, 0F, -0.1047F));
        PartDefinition p_aby_horn_left_4 = p_aby_horn_left_3.addOrReplaceChild("aby_horn_left_4", CubeListBuilder.create()
                .texOffs(75, 217).addBox(-0.5F, -1.9F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.15F, -0.1F, -0.15F)),
                PartPose.offsetAndRotation(0F, -2.15F, 0F, -0.5236F, 0F, 0F));
        PartDefinition p_aby_horn_right_0 = p_abyssite_head_head.addOrReplaceChild("aby_horn_right_0", CubeListBuilder.create()
                .texOffs(170, 145).addBox(-1.5F, -3F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offsetAndRotation(-4F, -8.6F, -1.2F, -0.2094F, 0F, -0.5934F));
        PartDefinition p_aby_horn_right_1 = p_aby_horn_right_0.addOrReplaceChild("aby_horn_right_1", CubeListBuilder.create()
                .texOffs(183, 145).addBox(-1.5F, -3F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.45F, 0F, -0.45F)),
                PartPose.offsetAndRotation(0F, -2.75F, 0F, -0.4887F, 0F, 0.1745F));
        PartDefinition p_aby_horn_right_2 = p_aby_horn_right_1.addOrReplaceChild("aby_horn_right_2", CubeListBuilder.create()
                .texOffs(99, 172).addBox(-1F, -2.9F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.2F, -0.1F, -0.2F)),
                PartPose.offsetAndRotation(0F, -2.75F, 0F, -0.5934F, 0F, 0.2094F));
        PartDefinition p_aby_horn_right_3 = p_aby_horn_right_2.addOrReplaceChild("aby_horn_right_3", CubeListBuilder.create()
                .texOffs(108, 172).addBox(-1F, -2.7F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.45F, -0.3F, -0.45F)),
                PartPose.offsetAndRotation(0F, -2.55F, 0F, -0.6283F, 0F, 0.1047F));
        PartDefinition p_aby_horn_right_4 = p_aby_horn_right_3.addOrReplaceChild("aby_horn_right_4", CubeListBuilder.create()
                .texOffs(80, 217).addBox(-0.5F, -1.9F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.15F, -0.1F, -0.15F)),
                PartPose.offsetAndRotation(0F, -2.15F, 0F, -0.5236F, 0F, 0F));
        PartDefinition p_aby_brow_0 = p_abyssite_head_head.addOrReplaceChild("aby_brow_0", CubeListBuilder.create()
                .texOffs(225, 189).addBox(-1F, -1.88F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.2F, -0.12F, -0.2F)),
                PartPose.offsetAndRotation(0F, -6.2F, -5.2F, -0.2443F, 0F, 0F));
        PartDefinition p_aby_brow_1 = p_aby_brow_0.addOrReplaceChild("aby_brow_1", CubeListBuilder.create()
                .texOffs(85, 217).addBox(-0.5F, -1.8F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.06F, -0.2F, -0.06F)),
                PartPose.offset(0F, -1.51F, 0F));
        PartDefinition p_abyssite_chest_body = root.addOrReplaceChild("abyssite_chest_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_aby_back_0_0 = p_abyssite_chest_body.addOrReplaceChild("aby_back_0_0", CubeListBuilder.create()
                .texOffs(117, 172).addBox(-1F, -2.82F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.1F, -0.18F, -0.1F)),
                PartPose.offsetAndRotation(0F, 3F, 2.9F, -0.8029F, 0F, 0F));
        PartDefinition p_aby_back_0_1 = p_aby_back_0_0.addOrReplaceChild("aby_back_0_1", CubeListBuilder.create()
                .texOffs(72, 204).addBox(-0.5F, -2.7F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.005F, -0.3F, -0.005F)),
                PartPose.offset(0F, -2.39F, 0F));
        PartDefinition p_aby_back_1_0 = p_abyssite_chest_body.addOrReplaceChild("aby_back_1_0", CubeListBuilder.create()
                .texOffs(234, 189).addBox(-1F, -1.935F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.065F, -0.35F)),
                PartPose.offsetAndRotation(1.7F, 3.8F, 2.9F, -0.6981F, 0F, 0.384F));
        PartDefinition p_aby_back_1_1 = p_aby_back_1_0.addOrReplaceChild("aby_back_1_1", CubeListBuilder.create()
                .texOffs(90, 217).addBox(-0.5F, -1.85F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.15F, -0.1425F)),
                PartPose.offset(0F, -1.62F, 0F));
        PartDefinition p_aby_back_2_0 = p_abyssite_chest_body.addOrReplaceChild("aby_back_2_0", CubeListBuilder.create()
                .texOffs(243, 189).addBox(-1F, -1.935F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.065F, -0.35F)),
                PartPose.offsetAndRotation(-1.7F, 3.8F, 2.9F, -0.6981F, 0F, -0.384F));
        PartDefinition p_aby_back_2_1 = p_aby_back_2_0.addOrReplaceChild("aby_back_2_1", CubeListBuilder.create()
                .texOffs(95, 217).addBox(-0.5F, -1.85F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.15F, -0.1425F)),
                PartPose.offset(0F, -1.62F, 0F));
        PartDefinition p_abyssite_chest_left_arm = root.addOrReplaceChild("abyssite_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_aby_pauldron_left_arm = p_abyssite_chest_left_arm.addOrReplaceChild("aby_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(107, 17).addBox(-3.8F, -2.3F, -4F, 8F, 3F, 8F, new CubeDeformation(-0.2F, -0.3F, -0.1F))
                .texOffs(124, 41).addBox(-2.5F, 0.3F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, 0F, 0F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.2793F));
        PartDefinition p_aby_shoulder_left_arm_0_0 = p_aby_pauldron_left_arm.addOrReplaceChild("aby_shoulder_left_arm_0_0", CubeListBuilder.create()
                .texOffs(0, 194).addBox(-1F, -1.99F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.25F, -0.01F, -0.25F)),
                PartPose.offsetAndRotation(1.4F, -1.9F, -1.4F, 0.1745F, 0F, 0.4189F));
        PartDefinition p_aby_shoulder_left_arm_0_1 = p_aby_shoulder_left_arm_0_0.addOrReplaceChild("aby_shoulder_left_arm_0_1", CubeListBuilder.create()
                .texOffs(100, 217).addBox(-0.5F, -1.9F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.0875F, -0.1F, -0.0875F)),
                PartPose.offset(0F, -1.73F, 0F));
        PartDefinition p_aby_shoulder_left_arm_1_0 = p_aby_pauldron_left_arm.addOrReplaceChild("aby_shoulder_left_arm_1_0", CubeListBuilder.create()
                .texOffs(9, 194).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.285F, -0.45F)),
                PartPose.offsetAndRotation(2.2F, -1.9F, 1.5F, -0.2094F, 0F, 0.6283F));
        PartDefinition p_aby_shoulder_left_arm_1_1 = p_aby_shoulder_left_arm_1_0.addOrReplaceChild("aby_shoulder_left_arm_1_1", CubeListBuilder.create()
                .texOffs(105, 217).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1975F, -0.35F, -0.1975F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_abyssite_chest_right_arm = root.addOrReplaceChild("abyssite_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_aby_pauldron_right_arm = p_abyssite_chest_right_arm.addOrReplaceChild("aby_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(140, 17).addBox(-4.2F, -2.3F, -4F, 8F, 3F, 8F, new CubeDeformation(-0.2F, -0.3F, -0.1F))
                .texOffs(155, 41).addBox(-4.5F, 0.3F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, 0F, 0F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.2793F));
        PartDefinition p_aby_shoulder_right_arm_0_0 = p_aby_pauldron_right_arm.addOrReplaceChild("aby_shoulder_right_arm_0_0", CubeListBuilder.create()
                .texOffs(18, 194).addBox(-1F, -1.99F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.25F, -0.01F, -0.25F)),
                PartPose.offsetAndRotation(-1.4F, -1.9F, -1.4F, 0.1745F, 0F, -0.4189F));
        PartDefinition p_aby_shoulder_right_arm_0_1 = p_aby_shoulder_right_arm_0_0.addOrReplaceChild("aby_shoulder_right_arm_0_1", CubeListBuilder.create()
                .texOffs(110, 217).addBox(-0.5F, -1.9F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.0875F, -0.1F, -0.0875F)),
                PartPose.offset(0F, -1.73F, 0F));
        PartDefinition p_aby_shoulder_right_arm_1_0 = p_aby_pauldron_right_arm.addOrReplaceChild("aby_shoulder_right_arm_1_0", CubeListBuilder.create()
                .texOffs(27, 194).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.285F, -0.45F)),
                PartPose.offsetAndRotation(-2.2F, -1.9F, 1.5F, -0.2094F, 0F, -0.6283F));
        PartDefinition p_aby_shoulder_right_arm_1_1 = p_aby_shoulder_right_arm_1_0.addOrReplaceChild("aby_shoulder_right_arm_1_1", CubeListBuilder.create()
                .texOffs(115, 217).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1975F, -0.35F, -0.1975F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_abyssite_legs_body = root.addOrReplaceChild("abyssite_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_abyssite_legs_left_leg = root.addOrReplaceChild("abyssite_legs_left_leg", CubeListBuilder.create()
                .texOffs(15, 145).addBox(-3F, -0.9F, -3.3F, 6F, 5F, 1F, new CubeDeformation(-0.3F, -0.4F, 0F))
                .texOffs(32, 204).addBox(-1F, 0.1F, -4F, 2F, 3F, 1F, new CubeDeformation(-0.3F, -0.3F, -0.2F))
                .texOffs(83, 184).addBox(-2F, 4.9F, -3.3F, 4F, 3F, 1F, new CubeDeformation(0F, -0.1F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_abyssite_legs_right_leg = root.addOrReplaceChild("abyssite_legs_right_leg", CubeListBuilder.create()
                .texOffs(30, 145).addBox(-3F, -0.9F, -3.3F, 6F, 5F, 1F, new CubeDeformation(-0.3F, -0.4F, 0F))
                .texOffs(39, 204).addBox(-1F, 0.1F, -4F, 2F, 3F, 1F, new CubeDeformation(-0.3F, -0.3F, -0.2F))
                .texOffs(94, 184).addBox(-2F, 4.9F, -3.3F, 4F, 3F, 1F, new CubeDeformation(0F, -0.1F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_abyssite_feet_left_leg = root.addOrReplaceChild("abyssite_feet_left_leg", CubeListBuilder.create()
                .texOffs(0, 166).addBox(-2.5F, 10.25F, -4.45F, 5F, 3F, 2F, new CubeDeformation(0F, -0.45F, -0.45F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_aby_heel_left_leg_0_0 = p_abyssite_feet_left_leg.addOrReplaceChild("aby_heel_left_leg_0_0", CubeListBuilder.create()
                .texOffs(36, 194).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.285F, -0.45F)),
                PartPose.offsetAndRotation(0.9F, 10.8F, 2.8F, -0.9774F, 0F, 0.2094F));
        PartDefinition p_aby_heel_left_leg_0_1 = p_aby_heel_left_leg_0_0.addOrReplaceChild("aby_heel_left_leg_0_1", CubeListBuilder.create()
                .texOffs(120, 217).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1975F, -0.35F, -0.1975F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_aby_heel_left_leg_1_0 = p_abyssite_feet_left_leg.addOrReplaceChild("aby_heel_left_leg_1_0", CubeListBuilder.create()
                .texOffs(125, 217).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.05F, -0.45F, -0.05F)),
                PartPose.offsetAndRotation(-0.9F, 10.8F, 2.8F, -0.9774F, 0F, -0.2094F));
        PartDefinition p_aby_heel_left_leg_1_1 = p_aby_heel_left_leg_1_0.addOrReplaceChild("aby_heel_left_leg_1_1", CubeListBuilder.create()
                .texOffs(194, 225).addBox(-0.5F, -1F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.2525F, 0F, -0.2525F)),
                PartPose.offset(0F, -0.85F, 0F));
        PartDefinition p_abyssite_feet_right_leg = root.addOrReplaceChild("abyssite_feet_right_leg", CubeListBuilder.create()
                .texOffs(15, 166).addBox(-2.5F, 10.25F, -4.45F, 5F, 3F, 2F, new CubeDeformation(0F, -0.45F, -0.45F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_aby_heel_right_leg_0_0 = p_abyssite_feet_right_leg.addOrReplaceChild("aby_heel_right_leg_0_0", CubeListBuilder.create()
                .texOffs(45, 194).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.285F, -0.45F)),
                PartPose.offsetAndRotation(0.9F, 10.8F, 2.8F, -0.9774F, 0F, 0.2094F));
        PartDefinition p_aby_heel_right_leg_0_1 = p_aby_heel_right_leg_0_0.addOrReplaceChild("aby_heel_right_leg_0_1", CubeListBuilder.create()
                .texOffs(130, 217).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1975F, -0.35F, -0.1975F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_aby_heel_right_leg_1_0 = p_abyssite_feet_right_leg.addOrReplaceChild("aby_heel_right_leg_1_0", CubeListBuilder.create()
                .texOffs(135, 217).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.05F, -0.45F, -0.05F)),
                PartPose.offsetAndRotation(-0.9F, 10.8F, 2.8F, -0.9774F, 0F, -0.2094F));
        PartDefinition p_aby_heel_right_leg_1_1 = p_aby_heel_right_leg_1_0.addOrReplaceChild("aby_heel_right_leg_1_1", CubeListBuilder.create()
                .texOffs(199, 225).addBox(-0.5F, -1F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.2525F, 0F, -0.2525F)),
                PartPose.offset(0F, -0.85F, 0F));
        PartDefinition p_fire_head_head = root.addOrReplaceChild("fire_head_head", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_fire_horn_left_0 = p_fire_head_head.addOrReplaceChild("fire_horn_left_0", CubeListBuilder.create()
                .texOffs(196, 145).addBox(-1.5F, -2.9F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.1F, -0.4F)),
                PartPose.offsetAndRotation(3.2F, -8.7F, -1.6F, -0.4189F, 0F, 0.384F));
        PartDefinition p_fire_horn_left_1 = p_fire_horn_left_0.addOrReplaceChild("fire_horn_left_1", CubeListBuilder.create()
                .texOffs(126, 172).addBox(-1F, -2.9F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.1F, -0.1F, -0.1F)),
                PartPose.offsetAndRotation(0F, -2.55F, 0F, -0.5934F, 0F, -0.1047F));
        PartDefinition p_fire_horn_left_2 = p_fire_horn_left_1.addOrReplaceChild("fire_horn_left_2", CubeListBuilder.create()
                .texOffs(135, 172).addBox(-1F, -2.8F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.35F, -0.2F, -0.35F)),
                PartPose.offsetAndRotation(0F, -2.55F, 0F, -0.6632F, 0F, -0.1047F));
        PartDefinition p_fire_horn_left_3 = p_fire_horn_left_2.addOrReplaceChild("fire_horn_left_3", CubeListBuilder.create()
                .texOffs(77, 204).addBox(-0.5F, -2.6F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.1F)),
                PartPose.offsetAndRotation(0F, -2.35F, 0F, -0.5934F, 0F, 0F));
        PartDefinition p_fire_horn_right_0 = p_fire_head_head.addOrReplaceChild("fire_horn_right_0", CubeListBuilder.create()
                .texOffs(209, 145).addBox(-1.5F, -2.9F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.1F, -0.4F)),
                PartPose.offsetAndRotation(-3.2F, -8.7F, -1.6F, -0.4189F, 0F, -0.384F));
        PartDefinition p_fire_horn_right_1 = p_fire_horn_right_0.addOrReplaceChild("fire_horn_right_1", CubeListBuilder.create()
                .texOffs(144, 172).addBox(-1F, -2.9F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.1F, -0.1F, -0.1F)),
                PartPose.offsetAndRotation(0F, -2.55F, 0F, -0.5934F, 0F, 0.1047F));
        PartDefinition p_fire_horn_right_2 = p_fire_horn_right_1.addOrReplaceChild("fire_horn_right_2", CubeListBuilder.create()
                .texOffs(153, 172).addBox(-1F, -2.8F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.35F, -0.2F, -0.35F)),
                PartPose.offsetAndRotation(0F, -2.55F, 0F, -0.6632F, 0F, 0.1047F));
        PartDefinition p_fire_horn_right_3 = p_fire_horn_right_2.addOrReplaceChild("fire_horn_right_3", CubeListBuilder.create()
                .texOffs(82, 204).addBox(-0.5F, -2.6F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.1F)),
                PartPose.offsetAndRotation(0F, -2.35F, 0F, -0.5934F, 0F, 0F));
        PartDefinition p_fire_crest_0_0 = p_fire_head_head.addOrReplaceChild("fire_crest_0_0", CubeListBuilder.create()
                .texOffs(204, 225).addBox(-0.5F, -0.94F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.06F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, -3.2F, -0.4538F, 0F, 0F));
        PartDefinition p_fire_crest_0_1 = p_fire_crest_0_0.addOrReplaceChild("fire_crest_0_1", CubeListBuilder.create()
                .texOffs(209, 225).addBox(-0.5F, -0.9F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.1F, -0.225F)),
                PartPose.offset(0F, -0.63F, 0F));
        PartDefinition p_fire_crest_1_0 = p_fire_head_head.addOrReplaceChild("fire_crest_1_0", CubeListBuilder.create()
                .texOffs(140, 217).addBox(-0.5F, -1.605F, -0.5F, 1F, 2F, 1F, new CubeDeformation(0F, -0.395F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, -0.6F, -0.4538F, 0F, 0F));
        PartDefinition p_fire_crest_1_1 = p_fire_crest_1_0.addOrReplaceChild("fire_crest_1_1", CubeListBuilder.create()
                .texOffs(145, 217).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.225F, -0.45F, -0.225F)),
                PartPose.offset(0F, -0.96F, 0F));
        PartDefinition p_fire_crest_2_0 = p_fire_head_head.addOrReplaceChild("fire_crest_2_0", CubeListBuilder.create()
                .texOffs(214, 225).addBox(-0.5F, -0.995F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.005F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, 2F, -0.4538F, 0F, 0F));
        PartDefinition p_fire_crest_2_1 = p_fire_crest_2_0.addOrReplaceChild("fire_crest_2_1", CubeListBuilder.create()
                .texOffs(219, 225).addBox(-0.5F, -0.95F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.05F, -0.225F)),
                PartPose.offset(0F, -0.74F, 0F));
        PartDefinition p_fire_chest_body = root.addOrReplaceChild("fire_chest_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_fire_back_0_0 = p_fire_chest_body.addOrReplaceChild("fire_back_0_0", CubeListBuilder.create()
                .texOffs(54, 194).addBox(-1F, -1.935F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.065F, -0.3F)),
                PartPose.offsetAndRotation(0F, 0.8F, 2.9F, -0.8727F, 0F, 0F));
        PartDefinition p_fire_back_0_1 = p_fire_back_0_0.addOrReplaceChild("fire_back_0_1", CubeListBuilder.create()
                .texOffs(150, 217).addBox(-0.5F, -1.85F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.15F, -0.115F)),
                PartPose.offset(0F, -1.62F, 0F));
        PartDefinition p_fire_back_1_0 = p_fire_chest_body.addOrReplaceChild("fire_back_1_0", CubeListBuilder.create()
                .texOffs(162, 172).addBox(-1F, -2.545F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.3F, -0.455F, -0.3F)),
                PartPose.offsetAndRotation(0F, 3.6F, 2.9F, -0.8727F, 0F, 0F));
        PartDefinition p_fire_back_1_1 = p_fire_back_1_0.addOrReplaceChild("fire_back_1_1", CubeListBuilder.create()
                .texOffs(155, 217).addBox(-0.5F, -1.95F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.05F, -0.115F)),
                PartPose.offset(0F, -1.84F, 0F));
        PartDefinition p_fire_back_2_0 = p_fire_chest_body.addOrReplaceChild("fire_back_2_0", CubeListBuilder.create()
                .texOffs(63, 194).addBox(-1F, -1.88F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.12F, -0.3F)),
                PartPose.offsetAndRotation(0F, 6.6F, 2.9F, -0.8727F, 0F, 0F));
        PartDefinition p_fire_back_2_1 = p_fire_back_2_0.addOrReplaceChild("fire_back_2_1", CubeListBuilder.create()
                .texOffs(160, 217).addBox(-0.5F, -1.8F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.2F, -0.115F)),
                PartPose.offset(0F, -1.51F, 0F));
        PartDefinition p_fire_back_3_0 = p_fire_chest_body.addOrReplaceChild("fire_back_3_0", CubeListBuilder.create()
                .texOffs(72, 194).addBox(-1F, -1.66F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.34F, -0.3F)),
                PartPose.offsetAndRotation(0F, 9.4F, 2.9F, -0.8727F, 0F, 0F));
        PartDefinition p_fire_back_3_1 = p_fire_back_3_0.addOrReplaceChild("fire_back_3_1", CubeListBuilder.create()
                .texOffs(165, 217).addBox(-0.5F, -1.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.4F, -0.115F)),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_fire_chest_left_arm = root.addOrReplaceChild("fire_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_fire_pauldron_left_arm = p_fire_chest_left_arm.addOrReplaceChild("fire_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(186, 41).addBox(-3.3F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F))
                .texOffs(120, 74).addBox(-2F, -0.1F, -4F, 6F, 2F, 8F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.2094F));
        PartDefinition p_fire_claw_left_arm_0 = p_fire_pauldron_left_arm.addOrReplaceChild("fire_claw_left_arm_0", CubeListBuilder.create()
                .texOffs(81, 194).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.285F, -0.35F)),
                PartPose.offsetAndRotation(2.4F, -1.4F, 0F, 0F, 0F, 0.6981F));
        PartDefinition p_fire_claw_left_arm_1 = p_fire_claw_left_arm_0.addOrReplaceChild("fire_claw_left_arm_1", CubeListBuilder.create()
                .texOffs(170, 217).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.35F, -0.1425F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_fire_chest_right_arm = root.addOrReplaceChild("fire_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_fire_pauldron_right_arm = p_fire_chest_right_arm.addOrReplaceChild("fire_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(217, 41).addBox(-3.7F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F))
                .texOffs(149, 74).addBox(-4F, -0.1F, -4F, 6F, 2F, 8F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.2094F));
        PartDefinition p_fire_claw_right_arm_0 = p_fire_pauldron_right_arm.addOrReplaceChild("fire_claw_right_arm_0", CubeListBuilder.create()
                .texOffs(90, 194).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.285F, -0.35F)),
                PartPose.offsetAndRotation(-2.4F, -1.4F, 0F, 0F, 0F, -0.6981F));
        PartDefinition p_fire_claw_right_arm_1 = p_fire_claw_right_arm_0.addOrReplaceChild("fire_claw_right_arm_1", CubeListBuilder.create()
                .texOffs(175, 217).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.35F, -0.1425F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_fire_legs_body = root.addOrReplaceChild("fire_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_fire_tail_0 = p_fire_legs_body.addOrReplaceChild("fire_tail_0", CubeListBuilder.create()
                .texOffs(222, 145).addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F),
                PartPose.offsetAndRotation(0F, 10.2F, 2.2F, 0.5236F, 0F, 0F));
        PartDefinition p_fire_tail_1 = p_fire_tail_0.addOrReplaceChild("fire_tail_1", CubeListBuilder.create()
                .texOffs(235, 145).addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, 2.75F, 0F, 0.1745F, 0F, 0F));
        PartDefinition p_fire_tail_2 = p_fire_tail_1.addOrReplaceChild("fire_tail_2", CubeListBuilder.create()
                .texOffs(0, 152).addBox(-1.5F, -0.1F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.1F, -0.4F)),
                PartPose.offsetAndRotation(0F, 2.75F, 0F, 0.2094F, 0F, 0F));
        PartDefinition p_fire_tail_3 = p_fire_tail_2.addOrReplaceChild("fire_tail_3", CubeListBuilder.create()
                .texOffs(171, 172).addBox(-1F, -0.2F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.15F, -0.2F, -0.15F)),
                PartPose.offsetAndRotation(0F, 2.55F, 0F, 0.2094F, 0F, 0F));
        PartDefinition p_fire_tail_4 = p_fire_tail_3.addOrReplaceChild("fire_tail_4", CubeListBuilder.create()
                .texOffs(180, 172).addBox(-1F, -0.4F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.4F, -0.4F, -0.4F)),
                PartPose.offsetAndRotation(0F, 2.35F, 0F, 0.1745F, 0F, 0F));
        PartDefinition p_fire_tail_tip = p_fire_tail_4.addOrReplaceChild("fire_tail_tip", CubeListBuilder.create()
                .texOffs(183, 166).addBox(-1.5F, -0.3F, -1F, 3F, 3F, 2F, new CubeDeformation(-0.3F, -0.3F, -0.3F))
                .texOffs(42, 209).addBox(-1F, 2.2F, -0.5F, 2F, 2F, 1F, new CubeDeformation(-0.3F, 0F, -0.1F))
                .texOffs(180, 217).addBox(-0.5F, 3.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.2F, -0.4F, -0.3F)),
                PartPose.offsetAndRotation(0F, 2.4F, 0F, 0.1396F, 0F, 0F));
        PartDefinition p_fire_legs_left_leg = root.addOrReplaceChild("fire_legs_left_leg", CubeListBuilder.create()
                .texOffs(105, 184).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_fire_legs_right_leg = root.addOrReplaceChild("fire_legs_right_leg", CubeListBuilder.create()
                .texOffs(116, 184).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_fire_feet_left_leg = root.addOrReplaceChild("fire_feet_left_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_fire_talon_left_leg_0 = p_fire_feet_left_leg.addOrReplaceChild("fire_talon_left_leg_0", CubeListBuilder.create()
                .texOffs(49, 209).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(-1.6F, 11.6F, -2.8F, 0.4887F, -0.2234F, 0F));
        PartDefinition p_fire_talon_left_leg_1 = p_fire_feet_left_leg.addOrReplaceChild("fire_talon_left_leg_1", CubeListBuilder.create()
                .texOffs(56, 209).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(0F, 11.6F, -2.8F, 0.4887F, 0F, 0F));
        PartDefinition p_fire_talon_left_leg_2 = p_fire_feet_left_leg.addOrReplaceChild("fire_talon_left_leg_2", CubeListBuilder.create()
                .texOffs(63, 209).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(1.6F, 11.6F, -2.8F, 0.4887F, 0.2234F, 0F));
        PartDefinition p_fire_feet_right_leg = root.addOrReplaceChild("fire_feet_right_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_fire_talon_right_leg_0 = p_fire_feet_right_leg.addOrReplaceChild("fire_talon_right_leg_0", CubeListBuilder.create()
                .texOffs(70, 209).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(-1.6F, 11.6F, -2.8F, 0.4887F, -0.2234F, 0F));
        PartDefinition p_fire_talon_right_leg_1 = p_fire_feet_right_leg.addOrReplaceChild("fire_talon_right_leg_1", CubeListBuilder.create()
                .texOffs(77, 209).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(0F, 11.6F, -2.8F, 0.4887F, 0F, 0F));
        PartDefinition p_fire_talon_right_leg_2 = p_fire_feet_right_leg.addOrReplaceChild("fire_talon_right_leg_2", CubeListBuilder.create()
                .texOffs(84, 209).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(1.6F, 11.6F, -2.8F, 0.4887F, 0.2234F, 0F));
        PartDefinition p_ice_head_head = root.addOrReplaceChild("ice_head_head", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_ice_horn_left_0 = p_ice_head_head.addOrReplaceChild("ice_horn_left_0", CubeListBuilder.create()
                .texOffs(147, 159).addBox(-1F, -3.8F, -1F, 2F, 4F, 2F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offsetAndRotation(3F, -8.7F, -1F, -0.4887F, 0F, 0.2793F));
        PartDefinition p_ice_horn_left_1 = p_ice_horn_left_0.addOrReplaceChild("ice_horn_left_1", CubeListBuilder.create()
                .texOffs(156, 159).addBox(-1F, -3.7F, -1F, 2F, 4F, 2F, new CubeDeformation(-0.4F, -0.3F, -0.4F)),
                PartPose.offsetAndRotation(0F, -3.35F, 0F, -0.1047F, 0F, 0F));
        PartDefinition p_ice_side_left_0 = p_ice_head_head.addOrReplaceChild("ice_side_left_0", CubeListBuilder.create()
                .texOffs(99, 194).addBox(-1F, -1.66F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.34F, -0.45F)),
                PartPose.offsetAndRotation(4.4F, -7.4F, 0.6F, -0.6283F, 0F, 0.6981F));
        PartDefinition p_ice_side_left_1 = p_ice_side_left_0.addOrReplaceChild("ice_side_left_1", CubeListBuilder.create()
                .texOffs(185, 217).addBox(-0.5F, -1.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1975F, -0.4F, -0.1975F)),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_ice_horn_right_0 = p_ice_head_head.addOrReplaceChild("ice_horn_right_0", CubeListBuilder.create()
                .texOffs(165, 159).addBox(-1F, -3.8F, -1F, 2F, 4F, 2F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offsetAndRotation(-3F, -8.7F, -1F, -0.4887F, 0F, -0.2793F));
        PartDefinition p_ice_horn_right_1 = p_ice_horn_right_0.addOrReplaceChild("ice_horn_right_1", CubeListBuilder.create()
                .texOffs(174, 159).addBox(-1F, -3.7F, -1F, 2F, 4F, 2F, new CubeDeformation(-0.4F, -0.3F, -0.4F)),
                PartPose.offsetAndRotation(0F, -3.35F, 0F, -0.1047F, 0F, 0F));
        PartDefinition p_ice_side_right_0 = p_ice_head_head.addOrReplaceChild("ice_side_right_0", CubeListBuilder.create()
                .texOffs(108, 194).addBox(-1F, -1.66F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.34F, -0.45F)),
                PartPose.offsetAndRotation(-4.4F, -7.4F, 0.6F, -0.6283F, 0F, -0.6981F));
        PartDefinition p_ice_side_right_1 = p_ice_side_right_0.addOrReplaceChild("ice_side_right_1", CubeListBuilder.create()
                .texOffs(190, 217).addBox(-0.5F, -1.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1975F, -0.4F, -0.1975F)),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_ice_crest_0_0 = p_ice_head_head.addOrReplaceChild("ice_crest_0_0", CubeListBuilder.create()
                .texOffs(224, 225).addBox(-0.5F, -0.94F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.06F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, -3.2F, -0.4538F, 0F, 0F));
        PartDefinition p_ice_crest_0_1 = p_ice_crest_0_0.addOrReplaceChild("ice_crest_0_1", CubeListBuilder.create()
                .texOffs(229, 225).addBox(-0.5F, -0.9F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.1F, -0.225F)),
                PartPose.offset(0F, -0.63F, 0F));
        PartDefinition p_ice_crest_1_0 = p_ice_head_head.addOrReplaceChild("ice_crest_1_0", CubeListBuilder.create()
                .texOffs(195, 217).addBox(-0.5F, -1.605F, -0.5F, 1F, 2F, 1F, new CubeDeformation(0F, -0.395F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, -0.6F, -0.4538F, 0F, 0F));
        PartDefinition p_ice_crest_1_1 = p_ice_crest_1_0.addOrReplaceChild("ice_crest_1_1", CubeListBuilder.create()
                .texOffs(200, 217).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.225F, -0.45F, -0.225F)),
                PartPose.offset(0F, -0.96F, 0F));
        PartDefinition p_ice_crest_2_0 = p_ice_head_head.addOrReplaceChild("ice_crest_2_0", CubeListBuilder.create()
                .texOffs(234, 225).addBox(-0.5F, -0.995F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.005F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, 2F, -0.4538F, 0F, 0F));
        PartDefinition p_ice_crest_2_1 = p_ice_crest_2_0.addOrReplaceChild("ice_crest_2_1", CubeListBuilder.create()
                .texOffs(239, 225).addBox(-0.5F, -0.95F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.05F, -0.225F)),
                PartPose.offset(0F, -0.74F, 0F));
        PartDefinition p_ice_chest_body = root.addOrReplaceChild("ice_chest_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_ice_back_0_0 = p_ice_chest_body.addOrReplaceChild("ice_back_0_0", CubeListBuilder.create()
                .texOffs(117, 194).addBox(-1F, -1.935F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.065F, -0.3F)),
                PartPose.offsetAndRotation(0F, 0.8F, 2.9F, -0.8727F, 0F, 0F));
        PartDefinition p_ice_back_0_1 = p_ice_back_0_0.addOrReplaceChild("ice_back_0_1", CubeListBuilder.create()
                .texOffs(205, 217).addBox(-0.5F, -1.85F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.15F, -0.115F)),
                PartPose.offset(0F, -1.62F, 0F));
        PartDefinition p_ice_back_1_0 = p_ice_chest_body.addOrReplaceChild("ice_back_1_0", CubeListBuilder.create()
                .texOffs(189, 172).addBox(-1F, -2.545F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.3F, -0.455F, -0.3F)),
                PartPose.offsetAndRotation(0F, 3.6F, 2.9F, -0.8727F, 0F, 0F));
        PartDefinition p_ice_back_1_1 = p_ice_back_1_0.addOrReplaceChild("ice_back_1_1", CubeListBuilder.create()
                .texOffs(210, 217).addBox(-0.5F, -1.95F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.05F, -0.115F)),
                PartPose.offset(0F, -1.84F, 0F));
        PartDefinition p_ice_back_2_0 = p_ice_chest_body.addOrReplaceChild("ice_back_2_0", CubeListBuilder.create()
                .texOffs(126, 194).addBox(-1F, -1.88F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.12F, -0.3F)),
                PartPose.offsetAndRotation(0F, 6.6F, 2.9F, -0.8727F, 0F, 0F));
        PartDefinition p_ice_back_2_1 = p_ice_back_2_0.addOrReplaceChild("ice_back_2_1", CubeListBuilder.create()
                .texOffs(215, 217).addBox(-0.5F, -1.8F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.2F, -0.115F)),
                PartPose.offset(0F, -1.51F, 0F));
        PartDefinition p_ice_back_3_0 = p_ice_chest_body.addOrReplaceChild("ice_back_3_0", CubeListBuilder.create()
                .texOffs(135, 194).addBox(-1F, -1.66F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.34F, -0.3F)),
                PartPose.offsetAndRotation(0F, 9.4F, 2.9F, -0.8727F, 0F, 0F));
        PartDefinition p_ice_back_3_1 = p_ice_back_3_0.addOrReplaceChild("ice_back_3_1", CubeListBuilder.create()
                .texOffs(220, 217).addBox(-0.5F, -1.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.4F, -0.115F)),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_ice_chest_left_arm = root.addOrReplaceChild("ice_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_ice_pauldron_left_arm = p_ice_chest_left_arm.addOrReplaceChild("ice_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(0, 52).addBox(-3.3F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F))
                .texOffs(178, 74).addBox(-2F, -0.1F, -4F, 6F, 2F, 8F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.2094F));
        PartDefinition p_ice_claw_left_arm_0 = p_ice_pauldron_left_arm.addOrReplaceChild("ice_claw_left_arm_0", CubeListBuilder.create()
                .texOffs(144, 194).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.285F, -0.35F)),
                PartPose.offsetAndRotation(2.4F, -1.4F, 0F, 0F, 0F, 0.6981F));
        PartDefinition p_ice_claw_left_arm_1 = p_ice_claw_left_arm_0.addOrReplaceChild("ice_claw_left_arm_1", CubeListBuilder.create()
                .texOffs(225, 217).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.35F, -0.1425F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_ice_chest_right_arm = root.addOrReplaceChild("ice_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_ice_pauldron_right_arm = p_ice_chest_right_arm.addOrReplaceChild("ice_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(31, 52).addBox(-3.7F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F))
                .texOffs(207, 74).addBox(-4F, -0.1F, -4F, 6F, 2F, 8F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.2094F));
        PartDefinition p_ice_claw_right_arm_0 = p_ice_pauldron_right_arm.addOrReplaceChild("ice_claw_right_arm_0", CubeListBuilder.create()
                .texOffs(153, 194).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.285F, -0.35F)),
                PartPose.offsetAndRotation(-2.4F, -1.4F, 0F, 0F, 0F, -0.6981F));
        PartDefinition p_ice_claw_right_arm_1 = p_ice_claw_right_arm_0.addOrReplaceChild("ice_claw_right_arm_1", CubeListBuilder.create()
                .texOffs(230, 217).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.35F, -0.1425F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_ice_legs_body = root.addOrReplaceChild("ice_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_ice_tail_0 = p_ice_legs_body.addOrReplaceChild("ice_tail_0", CubeListBuilder.create()
                .texOffs(13, 152).addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F),
                PartPose.offsetAndRotation(0F, 10.2F, 2.2F, 0.5236F, 0F, 0F));
        PartDefinition p_ice_tail_1 = p_ice_tail_0.addOrReplaceChild("ice_tail_1", CubeListBuilder.create()
                .texOffs(26, 152).addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, 2.75F, 0F, 0.1745F, 0F, 0F));
        PartDefinition p_ice_tail_2 = p_ice_tail_1.addOrReplaceChild("ice_tail_2", CubeListBuilder.create()
                .texOffs(39, 152).addBox(-1.5F, -0.1F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.1F, -0.4F)),
                PartPose.offsetAndRotation(0F, 2.75F, 0F, 0.2094F, 0F, 0F));
        PartDefinition p_ice_tail_3 = p_ice_tail_2.addOrReplaceChild("ice_tail_3", CubeListBuilder.create()
                .texOffs(198, 172).addBox(-1F, -0.2F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.15F, -0.2F, -0.15F)),
                PartPose.offsetAndRotation(0F, 2.55F, 0F, 0.2094F, 0F, 0F));
        PartDefinition p_ice_tail_4 = p_ice_tail_3.addOrReplaceChild("ice_tail_4", CubeListBuilder.create()
                .texOffs(207, 172).addBox(-1F, -0.4F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.4F, -0.4F, -0.4F)),
                PartPose.offsetAndRotation(0F, 2.35F, 0F, 0.1745F, 0F, 0F));
        PartDefinition p_ice_tail_tip = p_ice_tail_4.addOrReplaceChild("ice_tail_tip", CubeListBuilder.create()
                .texOffs(216, 172).addBox(-1F, -0.3F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.1F, -0.3F, -0.1F))
                .texOffs(87, 204).addBox(-0.5F, 1.8F, -0.5F, 1F, 3F, 1F, new CubeDeformation(0F, -0.4F, 0F)),
                PartPose.offsetAndRotation(0F, 2.4F, 0F, 0.1396F, 0F, 0F));
        PartDefinition p_ice_legs_left_leg = root.addOrReplaceChild("ice_legs_left_leg", CubeListBuilder.create()
                .texOffs(127, 184).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_ice_legs_right_leg = root.addOrReplaceChild("ice_legs_right_leg", CubeListBuilder.create()
                .texOffs(138, 184).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_ice_feet_left_leg = root.addOrReplaceChild("ice_feet_left_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_ice_talon_left_leg_0 = p_ice_feet_left_leg.addOrReplaceChild("ice_talon_left_leg_0", CubeListBuilder.create()
                .texOffs(91, 209).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(-1.6F, 11.6F, -2.8F, 0.4887F, -0.2234F, 0F));
        PartDefinition p_ice_talon_left_leg_1 = p_ice_feet_left_leg.addOrReplaceChild("ice_talon_left_leg_1", CubeListBuilder.create()
                .texOffs(98, 209).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(0F, 11.6F, -2.8F, 0.4887F, 0F, 0F));
        PartDefinition p_ice_talon_left_leg_2 = p_ice_feet_left_leg.addOrReplaceChild("ice_talon_left_leg_2", CubeListBuilder.create()
                .texOffs(105, 209).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(1.6F, 11.6F, -2.8F, 0.4887F, 0.2234F, 0F));
        PartDefinition p_ice_feet_right_leg = root.addOrReplaceChild("ice_feet_right_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_ice_talon_right_leg_0 = p_ice_feet_right_leg.addOrReplaceChild("ice_talon_right_leg_0", CubeListBuilder.create()
                .texOffs(112, 209).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(-1.6F, 11.6F, -2.8F, 0.4887F, -0.2234F, 0F));
        PartDefinition p_ice_talon_right_leg_1 = p_ice_feet_right_leg.addOrReplaceChild("ice_talon_right_leg_1", CubeListBuilder.create()
                .texOffs(119, 209).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(0F, 11.6F, -2.8F, 0.4887F, 0F, 0F));
        PartDefinition p_ice_talon_right_leg_2 = p_ice_feet_right_leg.addOrReplaceChild("ice_talon_right_leg_2", CubeListBuilder.create()
                .texOffs(126, 209).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(1.6F, 11.6F, -2.8F, 0.4887F, 0.2234F, 0F));
        PartDefinition p_lightning_head_head = root.addOrReplaceChild("lightning_head_head", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_light_horn_left_0 = p_lightning_head_head.addOrReplaceChild("light_horn_left_0", CubeListBuilder.create()
                .texOffs(225, 172).addBox(-1F, -2.7F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.05F, -0.3F, -0.05F)),
                PartPose.offsetAndRotation(3.1F, -8.7F, -1.2F, -0.3491F, 0F, 0.4538F));
        PartDefinition p_light_horn_left_1 = p_light_horn_left_0.addOrReplaceChild("light_horn_left_1", CubeListBuilder.create()
                .texOffs(234, 172).addBox(-1F, -2.6F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.2F, -0.4F, -0.2F)),
                PartPose.offsetAndRotation(0F, -2.15F, 0F, -0.8727F, 0F, -0.3142F));
        PartDefinition p_light_horn_left_2 = p_light_horn_left_1.addOrReplaceChild("light_horn_left_2", CubeListBuilder.create()
                .texOffs(243, 172).addBox(-1F, -2.6F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.35F, -0.4F, -0.35F)),
                PartPose.offsetAndRotation(0F, -1.95F, 0F, 0.6981F, 0F, 0.1745F));
        PartDefinition p_light_horn_left_3 = p_light_horn_left_2.addOrReplaceChild("light_horn_left_3", CubeListBuilder.create()
                .texOffs(235, 217).addBox(-0.5F, -2F, -0.5F, 1F, 2F, 1F),
                PartPose.offsetAndRotation(0F, -1.95F, 0F, -0.9774F, 0F, -0.1047F));
        PartDefinition p_light_horn_left_4 = p_light_horn_left_3.addOrReplaceChild("light_horn_left_4", CubeListBuilder.create()
                .texOffs(240, 217).addBox(-0.5F, -1.9F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.15F, -0.1F, -0.15F)),
                PartPose.offsetAndRotation(0F, -1.75F, 0F, 0.5934F, 0F, 0F));
        PartDefinition p_light_horn_right_0 = p_lightning_head_head.addOrReplaceChild("light_horn_right_0", CubeListBuilder.create()
                .texOffs(0, 178).addBox(-1F, -2.7F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.05F, -0.3F, -0.05F)),
                PartPose.offsetAndRotation(-3.1F, -8.7F, -1.2F, -0.3491F, 0F, -0.4538F));
        PartDefinition p_light_horn_right_1 = p_light_horn_right_0.addOrReplaceChild("light_horn_right_1", CubeListBuilder.create()
                .texOffs(9, 178).addBox(-1F, -2.6F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.2F, -0.4F, -0.2F)),
                PartPose.offsetAndRotation(0F, -2.15F, 0F, -0.8727F, 0F, 0.3142F));
        PartDefinition p_light_horn_right_2 = p_light_horn_right_1.addOrReplaceChild("light_horn_right_2", CubeListBuilder.create()
                .texOffs(18, 178).addBox(-1F, -2.6F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.35F, -0.4F, -0.35F)),
                PartPose.offsetAndRotation(0F, -1.95F, 0F, 0.6981F, 0F, -0.1745F));
        PartDefinition p_light_horn_right_3 = p_light_horn_right_2.addOrReplaceChild("light_horn_right_3", CubeListBuilder.create()
                .texOffs(245, 217).addBox(-0.5F, -2F, -0.5F, 1F, 2F, 1F),
                PartPose.offsetAndRotation(0F, -1.95F, 0F, -0.9774F, 0F, 0.1047F));
        PartDefinition p_light_horn_right_4 = p_light_horn_right_3.addOrReplaceChild("light_horn_right_4", CubeListBuilder.create()
                .texOffs(250, 217).addBox(-0.5F, -1.9F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.15F, -0.1F, -0.15F)),
                PartPose.offsetAndRotation(0F, -1.75F, 0F, 0.5934F, 0F, 0F));
        PartDefinition p_lightning_crest_0_0 = p_lightning_head_head.addOrReplaceChild("lightning_crest_0_0", CubeListBuilder.create()
                .texOffs(244, 225).addBox(-0.5F, -0.94F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.06F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, -3.2F, -0.4538F, 0F, 0F));
        PartDefinition p_lightning_crest_0_1 = p_lightning_crest_0_0.addOrReplaceChild("lightning_crest_0_1", CubeListBuilder.create()
                .texOffs(249, 225).addBox(-0.5F, -0.9F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.1F, -0.225F)),
                PartPose.offset(0F, -0.63F, 0F));
        PartDefinition p_lightning_crest_1_0 = p_lightning_head_head.addOrReplaceChild("lightning_crest_1_0", CubeListBuilder.create()
                .texOffs(0, 221).addBox(-0.5F, -1.605F, -0.5F, 1F, 2F, 1F, new CubeDeformation(0F, -0.395F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, -0.6F, -0.4538F, 0F, 0F));
        PartDefinition p_lightning_crest_1_1 = p_lightning_crest_1_0.addOrReplaceChild("lightning_crest_1_1", CubeListBuilder.create()
                .texOffs(5, 221).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.225F, -0.45F, -0.225F)),
                PartPose.offset(0F, -0.96F, 0F));
        PartDefinition p_lightning_crest_2_0 = p_lightning_head_head.addOrReplaceChild("lightning_crest_2_0", CubeListBuilder.create()
                .texOffs(0, 229).addBox(-0.5F, -0.995F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.005F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, 2F, -0.4538F, 0F, 0F));
        PartDefinition p_lightning_crest_2_1 = p_lightning_crest_2_0.addOrReplaceChild("lightning_crest_2_1", CubeListBuilder.create()
                .texOffs(5, 229).addBox(-0.5F, -0.95F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.05F, -0.225F)),
                PartPose.offset(0F, -0.74F, 0F));
        PartDefinition p_lightning_chest_body = root.addOrReplaceChild("lightning_chest_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_lightning_back_0_0 = p_lightning_chest_body.addOrReplaceChild("lightning_back_0_0", CubeListBuilder.create()
                .texOffs(162, 194).addBox(-1F, -1.935F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.065F, -0.3F)),
                PartPose.offsetAndRotation(0F, 0.8F, 2.9F, -0.8727F, 0F, 0F));
        PartDefinition p_lightning_back_0_1 = p_lightning_back_0_0.addOrReplaceChild("lightning_back_0_1", CubeListBuilder.create()
                .texOffs(10, 221).addBox(-0.5F, -1.85F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.15F, -0.115F)),
                PartPose.offset(0F, -1.62F, 0F));
        PartDefinition p_lightning_back_1_0 = p_lightning_chest_body.addOrReplaceChild("lightning_back_1_0", CubeListBuilder.create()
                .texOffs(27, 178).addBox(-1F, -2.545F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.3F, -0.455F, -0.3F)),
                PartPose.offsetAndRotation(0F, 3.6F, 2.9F, -0.8727F, 0F, 0F));
        PartDefinition p_lightning_back_1_1 = p_lightning_back_1_0.addOrReplaceChild("lightning_back_1_1", CubeListBuilder.create()
                .texOffs(15, 221).addBox(-0.5F, -1.95F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.05F, -0.115F)),
                PartPose.offset(0F, -1.84F, 0F));
        PartDefinition p_lightning_back_2_0 = p_lightning_chest_body.addOrReplaceChild("lightning_back_2_0", CubeListBuilder.create()
                .texOffs(171, 194).addBox(-1F, -1.88F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.12F, -0.3F)),
                PartPose.offsetAndRotation(0F, 6.6F, 2.9F, -0.8727F, 0F, 0F));
        PartDefinition p_lightning_back_2_1 = p_lightning_back_2_0.addOrReplaceChild("lightning_back_2_1", CubeListBuilder.create()
                .texOffs(20, 221).addBox(-0.5F, -1.8F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.2F, -0.115F)),
                PartPose.offset(0F, -1.51F, 0F));
        PartDefinition p_lightning_back_3_0 = p_lightning_chest_body.addOrReplaceChild("lightning_back_3_0", CubeListBuilder.create()
                .texOffs(180, 194).addBox(-1F, -1.66F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.34F, -0.3F)),
                PartPose.offsetAndRotation(0F, 9.4F, 2.9F, -0.8727F, 0F, 0F));
        PartDefinition p_lightning_back_3_1 = p_lightning_back_3_0.addOrReplaceChild("lightning_back_3_1", CubeListBuilder.create()
                .texOffs(25, 221).addBox(-0.5F, -1.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.115F, -0.4F, -0.115F)),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_lightning_chest_left_arm = root.addOrReplaceChild("lightning_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_lightning_pauldron_left_arm = p_lightning_chest_left_arm.addOrReplaceChild("lightning_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(62, 52).addBox(-3.3F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F))
                .texOffs(0, 85).addBox(-2F, -0.1F, -4F, 6F, 2F, 8F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.2094F));
        PartDefinition p_lightning_claw_left_arm_0 = p_lightning_pauldron_left_arm.addOrReplaceChild("lightning_claw_left_arm_0", CubeListBuilder.create()
                .texOffs(189, 194).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.285F, -0.35F)),
                PartPose.offsetAndRotation(2.4F, -1.4F, 0F, 0F, 0F, 0.6981F));
        PartDefinition p_lightning_claw_left_arm_1 = p_lightning_claw_left_arm_0.addOrReplaceChild("lightning_claw_left_arm_1", CubeListBuilder.create()
                .texOffs(30, 221).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.35F, -0.1425F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_lightning_chest_right_arm = root.addOrReplaceChild("lightning_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_lightning_pauldron_right_arm = p_lightning_chest_right_arm.addOrReplaceChild("lightning_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(93, 52).addBox(-3.7F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F))
                .texOffs(29, 85).addBox(-4F, -0.1F, -4F, 6F, 2F, 8F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.2094F));
        PartDefinition p_lightning_claw_right_arm_0 = p_lightning_pauldron_right_arm.addOrReplaceChild("lightning_claw_right_arm_0", CubeListBuilder.create()
                .texOffs(198, 194).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.285F, -0.35F)),
                PartPose.offsetAndRotation(-2.4F, -1.4F, 0F, 0F, 0F, -0.6981F));
        PartDefinition p_lightning_claw_right_arm_1 = p_lightning_claw_right_arm_0.addOrReplaceChild("lightning_claw_right_arm_1", CubeListBuilder.create()
                .texOffs(35, 221).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.35F, -0.1425F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_lightning_legs_body = root.addOrReplaceChild("lightning_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_lightning_tail_0 = p_lightning_legs_body.addOrReplaceChild("lightning_tail_0", CubeListBuilder.create()
                .texOffs(52, 152).addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F),
                PartPose.offsetAndRotation(0F, 10.2F, 2.2F, 0.5236F, 0F, 0F));
        PartDefinition p_lightning_tail_1 = p_lightning_tail_0.addOrReplaceChild("lightning_tail_1", CubeListBuilder.create()
                .texOffs(65, 152).addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, 2.75F, 0F, 0.1745F, 0F, 0F));
        PartDefinition p_lightning_tail_2 = p_lightning_tail_1.addOrReplaceChild("lightning_tail_2", CubeListBuilder.create()
                .texOffs(78, 152).addBox(-1.5F, -0.1F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.1F, -0.4F)),
                PartPose.offsetAndRotation(0F, 2.75F, 0F, 0.2094F, 0F, 0F));
        PartDefinition p_lightning_tail_3 = p_lightning_tail_2.addOrReplaceChild("lightning_tail_3", CubeListBuilder.create()
                .texOffs(36, 178).addBox(-1F, -0.2F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.15F, -0.2F, -0.15F)),
                PartPose.offsetAndRotation(0F, 2.55F, 0F, 0.2094F, 0F, 0F));
        PartDefinition p_lightning_tail_4 = p_lightning_tail_3.addOrReplaceChild("lightning_tail_4", CubeListBuilder.create()
                .texOffs(45, 178).addBox(-1F, -0.4F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.4F, -0.4F, -0.4F)),
                PartPose.offsetAndRotation(0F, 2.35F, 0F, 0.1745F, 0F, 0F));
        PartDefinition p_lightning_tail_tip = p_lightning_tail_4.addOrReplaceChild("lightning_tail_tip", CubeListBuilder.create()
                .texOffs(126, 137).addBox(-0.5F, -0.4F, -2F, 1F, 3F, 4F, new CubeDeformation(-0.2F, -0.4F, -0.4F))
                .texOffs(54, 178).addBox(-0.5F, 2F, -0.8F, 1F, 2F, 3F, new CubeDeformation(-0.25F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, 2.4F, 0F, 0.1396F, 0F, 0F));
        PartDefinition p_lightning_legs_left_leg = root.addOrReplaceChild("lightning_legs_left_leg", CubeListBuilder.create()
                .texOffs(149, 184).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_lightning_legs_right_leg = root.addOrReplaceChild("lightning_legs_right_leg", CubeListBuilder.create()
                .texOffs(160, 184).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_lightning_feet_left_leg = root.addOrReplaceChild("lightning_feet_left_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_lightning_talon_left_leg_0 = p_lightning_feet_left_leg.addOrReplaceChild("lightning_talon_left_leg_0", CubeListBuilder.create()
                .texOffs(133, 209).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(-1.6F, 11.6F, -2.8F, 0.4887F, -0.2234F, 0F));
        PartDefinition p_lightning_talon_left_leg_1 = p_lightning_feet_left_leg.addOrReplaceChild("lightning_talon_left_leg_1", CubeListBuilder.create()
                .texOffs(140, 209).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(0F, 11.6F, -2.8F, 0.4887F, 0F, 0F));
        PartDefinition p_lightning_talon_left_leg_2 = p_lightning_feet_left_leg.addOrReplaceChild("lightning_talon_left_leg_2", CubeListBuilder.create()
                .texOffs(147, 209).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(1.6F, 11.6F, -2.8F, 0.4887F, 0.2234F, 0F));
        PartDefinition p_lightning_feet_right_leg = root.addOrReplaceChild("lightning_feet_right_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_lightning_talon_right_leg_0 = p_lightning_feet_right_leg.addOrReplaceChild("lightning_talon_right_leg_0", CubeListBuilder.create()
                .texOffs(154, 209).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(-1.6F, 11.6F, -2.8F, 0.4887F, -0.2234F, 0F));
        PartDefinition p_lightning_talon_right_leg_1 = p_lightning_feet_right_leg.addOrReplaceChild("lightning_talon_right_leg_1", CubeListBuilder.create()
                .texOffs(161, 209).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(0F, 11.6F, -2.8F, 0.4887F, 0F, 0F));
        PartDefinition p_lightning_talon_right_leg_2 = p_lightning_feet_right_leg.addOrReplaceChild("lightning_talon_right_leg_2", CubeListBuilder.create()
                .texOffs(168, 209).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(1.6F, 11.6F, -2.8F, 0.4887F, 0.2234F, 0F));
        PartDefinition p_nature_head_head = root.addOrReplaceChild("nature_head_head", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_antler_left_0 = p_nature_head_head.addOrReplaceChild("antler_left_0", CubeListBuilder.create()
                .texOffs(63, 178).addBox(-1F, -3F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.3F, 0F, -0.3F)),
                PartPose.offsetAndRotation(3F, -8.7F, -0.8F, -0.2443F, 0F, 0.5236F));
        PartDefinition p_antler_left_1 = p_antler_left_0.addOrReplaceChild("antler_left_1", CubeListBuilder.create()
                .texOffs(72, 178).addBox(-1F, -3F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.4F, 0F, -0.4F)),
                PartPose.offsetAndRotation(0F, -2.75F, 0F, -0.384F, 0F, -0.1745F));
        PartDefinition p_antler_left_2 = p_antler_left_1.addOrReplaceChild("antler_left_2", CubeListBuilder.create()
                .texOffs(92, 204).addBox(-0.5F, -2.8F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.05F, -0.2F, -0.05F)),
                PartPose.offsetAndRotation(0F, -2.75F, 0F, -0.3491F, 0F, -0.1396F));
        PartDefinition p_antler_tine_left_1 = p_antler_left_1.addOrReplaceChild("antler_tine_left_1", CubeListBuilder.create()
                .texOffs(40, 221).addBox(-0.5F, -1.9F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.15F, -0.1F, -0.15F)),
                PartPose.offsetAndRotation(0F, -2.2F, 0F, 0.8378F, 0F, -0.2443F));
        PartDefinition p_antler_tine_left_0 = p_antler_left_0.addOrReplaceChild("antler_tine_left_0", CubeListBuilder.create()
                .texOffs(45, 221).addBox(-0.5F, -2F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1F, 0F, -0.1F)),
                PartPose.offsetAndRotation(0F, -2.4F, 0F, 0.8727F, 0F, 0.1745F));
        PartDefinition p_antler_leaf_left = p_antler_left_0.addOrReplaceChild("antler_leaf_left", CubeListBuilder.create()
                .texOffs(225, 178).addBox(-0.5F, -2.8F, -1F, 1F, 3F, 2F, new CubeDeformation(-0.35F, -0.2F, -0.1F)),
                PartPose.offsetAndRotation(0.6F, -1.4F, 0F, 0F, 0F, 0.8727F));
        PartDefinition p_antler_right_0 = p_nature_head_head.addOrReplaceChild("antler_right_0", CubeListBuilder.create()
                .texOffs(81, 178).addBox(-1F, -3F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.3F, 0F, -0.3F)),
                PartPose.offsetAndRotation(-3F, -8.7F, -0.8F, -0.2443F, 0F, -0.5236F));
        PartDefinition p_antler_right_1 = p_antler_right_0.addOrReplaceChild("antler_right_1", CubeListBuilder.create()
                .texOffs(90, 178).addBox(-1F, -3F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.4F, 0F, -0.4F)),
                PartPose.offsetAndRotation(0F, -2.75F, 0F, -0.384F, 0F, 0.1745F));
        PartDefinition p_antler_right_2 = p_antler_right_1.addOrReplaceChild("antler_right_2", CubeListBuilder.create()
                .texOffs(97, 204).addBox(-0.5F, -2.8F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.05F, -0.2F, -0.05F)),
                PartPose.offsetAndRotation(0F, -2.75F, 0F, -0.3491F, 0F, 0.1396F));
        PartDefinition p_antler_tine_right_1 = p_antler_right_1.addOrReplaceChild("antler_tine_right_1", CubeListBuilder.create()
                .texOffs(50, 221).addBox(-0.5F, -1.9F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.15F, -0.1F, -0.15F)),
                PartPose.offsetAndRotation(0F, -2.2F, 0F, 0.8378F, 0F, 0.2443F));
        PartDefinition p_antler_tine_right_0 = p_antler_right_0.addOrReplaceChild("antler_tine_right_0", CubeListBuilder.create()
                .texOffs(55, 221).addBox(-0.5F, -2F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1F, 0F, -0.1F)),
                PartPose.offsetAndRotation(0F, -2.4F, 0F, 0.8727F, 0F, -0.1745F));
        PartDefinition p_antler_leaf_right = p_antler_right_0.addOrReplaceChild("antler_leaf_right", CubeListBuilder.create()
                .texOffs(232, 178).addBox(-0.5F, -2.8F, -1F, 1F, 3F, 2F, new CubeDeformation(-0.35F, -0.2F, -0.1F)),
                PartPose.offsetAndRotation(-0.6F, -1.4F, 0F, 0F, 0F, -0.8727F));
        PartDefinition p_nature_crest_0_0 = p_nature_head_head.addOrReplaceChild("nature_crest_0_0", CubeListBuilder.create()
                .texOffs(10, 229).addBox(-0.5F, -0.94F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.06F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, -3.2F, -0.4538F, 0F, 0F));
        PartDefinition p_nature_crest_0_1 = p_nature_crest_0_0.addOrReplaceChild("nature_crest_0_1", CubeListBuilder.create()
                .texOffs(15, 229).addBox(-0.5F, -0.9F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.1F, -0.225F)),
                PartPose.offset(0F, -0.63F, 0F));
        PartDefinition p_nature_crest_1_0 = p_nature_head_head.addOrReplaceChild("nature_crest_1_0", CubeListBuilder.create()
                .texOffs(60, 221).addBox(-0.5F, -1.605F, -0.5F, 1F, 2F, 1F, new CubeDeformation(0F, -0.395F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, -0.6F, -0.4538F, 0F, 0F));
        PartDefinition p_nature_crest_1_1 = p_nature_crest_1_0.addOrReplaceChild("nature_crest_1_1", CubeListBuilder.create()
                .texOffs(65, 221).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.225F, -0.45F, -0.225F)),
                PartPose.offset(0F, -0.96F, 0F));
        PartDefinition p_nature_crest_2_0 = p_nature_head_head.addOrReplaceChild("nature_crest_2_0", CubeListBuilder.create()
                .texOffs(20, 229).addBox(-0.5F, -0.995F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.005F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, 2F, -0.4538F, 0F, 0F));
        PartDefinition p_nature_crest_2_1 = p_nature_crest_2_0.addOrReplaceChild("nature_crest_2_1", CubeListBuilder.create()
                .texOffs(25, 229).addBox(-0.5F, -0.95F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.05F, -0.225F)),
                PartPose.offset(0F, -0.74F, 0F));
        PartDefinition p_nature_chest_body = root.addOrReplaceChild("nature_chest_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_nature_frond_0 = p_nature_chest_body.addOrReplaceChild("nature_frond_0", CubeListBuilder.create()
                .texOffs(183, 159).addBox(-1.5F, -4.6F, -0.5F, 3F, 5F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.35F)),
                PartPose.offsetAndRotation(0F, 1F, 2.9F, -0.8378F, 0F, 0F));
        PartDefinition p_nature_frond_1 = p_nature_chest_body.addOrReplaceChild("nature_frond_1", CubeListBuilder.create()
                .texOffs(99, 178).addBox(-1.5F, -3.8F, -0.5F, 3F, 4F, 1F, new CubeDeformation(-0.4F, -0.2F, -0.35F)),
                PartPose.offsetAndRotation(0F, 4F, 2.9F, -0.8378F, 0F, 0F));
        PartDefinition p_nature_frond_2 = p_nature_chest_body.addOrReplaceChild("nature_frond_2", CubeListBuilder.create()
                .texOffs(207, 194).addBox(-1.5F, -3F, -0.5F, 3F, 3F, 1F, new CubeDeformation(-0.4F, 0F, -0.35F)),
                PartPose.offsetAndRotation(0F, 7F, 2.9F, -0.8378F, 0F, 0F));
        PartDefinition p_nature_chest_left_arm = root.addOrReplaceChild("nature_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_nature_pauldron_left_arm = p_nature_chest_left_arm.addOrReplaceChild("nature_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(124, 52).addBox(-3.3F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F))
                .texOffs(58, 85).addBox(-2F, -0.1F, -4F, 6F, 2F, 8F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.2094F));
        PartDefinition p_nature_claw_left_arm_0 = p_nature_pauldron_left_arm.addOrReplaceChild("nature_claw_left_arm_0", CubeListBuilder.create()
                .texOffs(216, 194).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.285F, -0.35F)),
                PartPose.offsetAndRotation(2.4F, -1.4F, 0F, 0F, 0F, 0.6981F));
        PartDefinition p_nature_claw_left_arm_1 = p_nature_claw_left_arm_0.addOrReplaceChild("nature_claw_left_arm_1", CubeListBuilder.create()
                .texOffs(70, 221).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.35F, -0.1425F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_nature_chest_right_arm = root.addOrReplaceChild("nature_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_nature_pauldron_right_arm = p_nature_chest_right_arm.addOrReplaceChild("nature_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(155, 52).addBox(-3.7F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F))
                .texOffs(87, 85).addBox(-4F, -0.1F, -4F, 6F, 2F, 8F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.2094F));
        PartDefinition p_nature_claw_right_arm_0 = p_nature_pauldron_right_arm.addOrReplaceChild("nature_claw_right_arm_0", CubeListBuilder.create()
                .texOffs(225, 194).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.285F, -0.35F)),
                PartPose.offsetAndRotation(-2.4F, -1.4F, 0F, 0F, 0F, -0.6981F));
        PartDefinition p_nature_claw_right_arm_1 = p_nature_claw_right_arm_0.addOrReplaceChild("nature_claw_right_arm_1", CubeListBuilder.create()
                .texOffs(75, 221).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.35F, -0.1425F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_nature_legs_body = root.addOrReplaceChild("nature_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_nature_tail_0 = p_nature_legs_body.addOrReplaceChild("nature_tail_0", CubeListBuilder.create()
                .texOffs(91, 152).addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F),
                PartPose.offsetAndRotation(0F, 10.2F, 2.2F, 0.5236F, 0F, 0F));
        PartDefinition p_nature_tail_1 = p_nature_tail_0.addOrReplaceChild("nature_tail_1", CubeListBuilder.create()
                .texOffs(104, 152).addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, 2.75F, 0F, 0.1745F, 0F, 0F));
        PartDefinition p_nature_tail_2 = p_nature_tail_1.addOrReplaceChild("nature_tail_2", CubeListBuilder.create()
                .texOffs(117, 152).addBox(-1.5F, -0.1F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.1F, -0.4F)),
                PartPose.offsetAndRotation(0F, 2.75F, 0F, 0.2094F, 0F, 0F));
        PartDefinition p_nature_tail_3 = p_nature_tail_2.addOrReplaceChild("nature_tail_3", CubeListBuilder.create()
                .texOffs(108, 178).addBox(-1F, -0.2F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.15F, -0.2F, -0.15F)),
                PartPose.offsetAndRotation(0F, 2.55F, 0F, 0.2094F, 0F, 0F));
        PartDefinition p_nature_tail_4 = p_nature_tail_3.addOrReplaceChild("nature_tail_4", CubeListBuilder.create()
                .texOffs(117, 178).addBox(-1F, -0.4F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.4F, -0.4F, -0.4F)),
                PartPose.offsetAndRotation(0F, 2.35F, 0F, 0.1745F, 0F, 0F));
        PartDefinition p_nature_tail_tip = p_nature_tail_4.addOrReplaceChild("nature_tail_tip", CubeListBuilder.create()
                .texOffs(114, 128).addBox(-0.5F, -0.4F, -1.5F, 1F, 5F, 3F, new CubeDeformation(-0.35F, -0.4F, -0.1F)),
                PartPose.offsetAndRotation(0F, 2.4F, 0F, 0.1396F, 0F, 0F));
        PartDefinition p_nature_legs_left_leg = root.addOrReplaceChild("nature_legs_left_leg", CubeListBuilder.create()
                .texOffs(171, 184).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_nature_legs_right_leg = root.addOrReplaceChild("nature_legs_right_leg", CubeListBuilder.create()
                .texOffs(182, 184).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_nature_feet_left_leg = root.addOrReplaceChild("nature_feet_left_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_nature_talon_left_leg_0 = p_nature_feet_left_leg.addOrReplaceChild("nature_talon_left_leg_0", CubeListBuilder.create()
                .texOffs(175, 209).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(-1.6F, 11.6F, -2.8F, 0.4887F, -0.2234F, 0F));
        PartDefinition p_nature_talon_left_leg_1 = p_nature_feet_left_leg.addOrReplaceChild("nature_talon_left_leg_1", CubeListBuilder.create()
                .texOffs(182, 209).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(0F, 11.6F, -2.8F, 0.4887F, 0F, 0F));
        PartDefinition p_nature_talon_left_leg_2 = p_nature_feet_left_leg.addOrReplaceChild("nature_talon_left_leg_2", CubeListBuilder.create()
                .texOffs(189, 209).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(1.6F, 11.6F, -2.8F, 0.4887F, 0.2234F, 0F));
        PartDefinition p_nature_feet_right_leg = root.addOrReplaceChild("nature_feet_right_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_nature_talon_right_leg_0 = p_nature_feet_right_leg.addOrReplaceChild("nature_talon_right_leg_0", CubeListBuilder.create()
                .texOffs(196, 209).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(-1.6F, 11.6F, -2.8F, 0.4887F, -0.2234F, 0F));
        PartDefinition p_nature_talon_right_leg_1 = p_nature_feet_right_leg.addOrReplaceChild("nature_talon_right_leg_1", CubeListBuilder.create()
                .texOffs(203, 209).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(0F, 11.6F, -2.8F, 0.4887F, 0F, 0F));
        PartDefinition p_nature_talon_right_leg_2 = p_nature_feet_right_leg.addOrReplaceChild("nature_talon_right_leg_2", CubeListBuilder.create()
                .texOffs(210, 209).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(1.6F, 11.6F, -2.8F, 0.4887F, 0.2234F, 0F));
        PartDefinition p_earth_head_head = root.addOrReplaceChild("earth_head_head", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_ram_horn_left_0 = p_earth_head_head.addOrReplaceChild("ram_horn_left_0", CubeListBuilder.create()
                .texOffs(130, 152).addBox(-1.5F, -2.8F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.1F, -0.2F, -0.1F)),
                PartPose.offsetAndRotation(5F, -8F, -0.6F, -0.4189F, 0F, 0.4887F));
        PartDefinition p_ram_horn_left_1 = p_ram_horn_left_0.addOrReplaceChild("ram_horn_left_1", CubeListBuilder.create()
                .texOffs(143, 152).addBox(-1.5F, -2.8F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.25F, -0.2F, -0.25F)),
                PartPose.offsetAndRotation(0F, -2.35F, 0F, -0.9599F, 0F, 0F));
        PartDefinition p_ram_horn_left_2 = p_ram_horn_left_1.addOrReplaceChild("ram_horn_left_2", CubeListBuilder.create()
                .texOffs(156, 152).addBox(-1.5F, -2.8F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(0F, -2.35F, 0F, -0.9599F, 0F, 0F));
        PartDefinition p_ram_horn_left_3 = p_ram_horn_left_2.addOrReplaceChild("ram_horn_left_3", CubeListBuilder.create()
                .texOffs(126, 178).addBox(-1F, -2.7F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.05F, -0.3F, -0.05F)),
                PartPose.offsetAndRotation(0F, -2.35F, 0F, -0.9599F, 0F, 0F));
        PartDefinition p_ram_horn_left_4 = p_ram_horn_left_3.addOrReplaceChild("ram_horn_left_4", CubeListBuilder.create()
                .texOffs(135, 178).addBox(-1F, -2.6F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.25F, -0.4F, -0.25F)),
                PartPose.offsetAndRotation(0F, -2.15F, 0F, -0.9599F, 0F, 0F));
        PartDefinition p_ram_horn_left_5 = p_ram_horn_left_4.addOrReplaceChild("ram_horn_left_5", CubeListBuilder.create()
                .texOffs(80, 221).addBox(-0.5F, -1.9F, -0.5F, 1F, 2F, 1F, new CubeDeformation(0F, -0.1F, 0F)),
                PartPose.offsetAndRotation(0F, -1.95F, 0F, -0.7854F, 0F, 0F));
        PartDefinition p_ram_horn_right_0 = p_earth_head_head.addOrReplaceChild("ram_horn_right_0", CubeListBuilder.create()
                .texOffs(169, 152).addBox(-1.5F, -2.8F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.1F, -0.2F, -0.1F)),
                PartPose.offsetAndRotation(-5F, -8F, -0.6F, -0.4189F, 0F, -0.4887F));
        PartDefinition p_ram_horn_right_1 = p_ram_horn_right_0.addOrReplaceChild("ram_horn_right_1", CubeListBuilder.create()
                .texOffs(182, 152).addBox(-1.5F, -2.8F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.25F, -0.2F, -0.25F)),
                PartPose.offsetAndRotation(0F, -2.35F, 0F, -0.9599F, 0F, 0F));
        PartDefinition p_ram_horn_right_2 = p_ram_horn_right_1.addOrReplaceChild("ram_horn_right_2", CubeListBuilder.create()
                .texOffs(195, 152).addBox(-1.5F, -2.8F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(0F, -2.35F, 0F, -0.9599F, 0F, 0F));
        PartDefinition p_ram_horn_right_3 = p_ram_horn_right_2.addOrReplaceChild("ram_horn_right_3", CubeListBuilder.create()
                .texOffs(144, 178).addBox(-1F, -2.7F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.05F, -0.3F, -0.05F)),
                PartPose.offsetAndRotation(0F, -2.35F, 0F, -0.9599F, 0F, 0F));
        PartDefinition p_ram_horn_right_4 = p_ram_horn_right_3.addOrReplaceChild("ram_horn_right_4", CubeListBuilder.create()
                .texOffs(153, 178).addBox(-1F, -2.6F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.25F, -0.4F, -0.25F)),
                PartPose.offsetAndRotation(0F, -2.15F, 0F, -0.9599F, 0F, 0F));
        PartDefinition p_ram_horn_right_5 = p_ram_horn_right_4.addOrReplaceChild("ram_horn_right_5", CubeListBuilder.create()
                .texOffs(85, 221).addBox(-0.5F, -1.9F, -0.5F, 1F, 2F, 1F, new CubeDeformation(0F, -0.1F, 0F)),
                PartPose.offsetAndRotation(0F, -1.95F, 0F, -0.7854F, 0F, 0F));
        PartDefinition p_earth_crest_0_0 = p_earth_head_head.addOrReplaceChild("earth_crest_0_0", CubeListBuilder.create()
                .texOffs(30, 229).addBox(-0.5F, -0.94F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.06F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, -3.2F, -0.4538F, 0F, 0F));
        PartDefinition p_earth_crest_0_1 = p_earth_crest_0_0.addOrReplaceChild("earth_crest_0_1", CubeListBuilder.create()
                .texOffs(35, 229).addBox(-0.5F, -0.9F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.1F, -0.225F)),
                PartPose.offset(0F, -0.63F, 0F));
        PartDefinition p_earth_crest_1_0 = p_earth_head_head.addOrReplaceChild("earth_crest_1_0", CubeListBuilder.create()
                .texOffs(90, 221).addBox(-0.5F, -1.605F, -0.5F, 1F, 2F, 1F, new CubeDeformation(0F, -0.395F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, -0.6F, -0.4538F, 0F, 0F));
        PartDefinition p_earth_crest_1_1 = p_earth_crest_1_0.addOrReplaceChild("earth_crest_1_1", CubeListBuilder.create()
                .texOffs(95, 221).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.225F, -0.45F, -0.225F)),
                PartPose.offset(0F, -0.96F, 0F));
        PartDefinition p_earth_crest_2_0 = p_earth_head_head.addOrReplaceChild("earth_crest_2_0", CubeListBuilder.create()
                .texOffs(40, 229).addBox(-0.5F, -0.995F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.005F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, 2F, -0.4538F, 0F, 0F));
        PartDefinition p_earth_crest_2_1 = p_earth_crest_2_0.addOrReplaceChild("earth_crest_2_1", CubeListBuilder.create()
                .texOffs(45, 229).addBox(-0.5F, -0.95F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.05F, -0.225F)),
                PartPose.offset(0F, -0.74F, 0F));
        PartDefinition p_earth_chest_body = root.addOrReplaceChild("earth_chest_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_earth_plate_0 = p_earth_chest_body.addOrReplaceChild("earth_plate_0", CubeListBuilder.create()
                .texOffs(159, 137).addBox(-0.5F, -3.6F, -1.5F, 1F, 4F, 3F, new CubeDeformation(-0.1F, -0.4F, -0.3F))
                .texOffs(217, 209).addBox(-0.5F, -4.1F, -1F, 1F, 1F, 2F, new CubeDeformation(-0.2F, 0F, -0.4F)),
                PartPose.offsetAndRotation(0F, 0.8F, 2.9F, -0.6981F, 0F, 0F));
        PartDefinition p_earth_plate_1 = p_earth_chest_body.addOrReplaceChild("earth_plate_1", CubeListBuilder.create()
                .texOffs(168, 137).addBox(-0.5F, -3.8F, -1.5F, 1F, 4F, 3F, new CubeDeformation(-0.1F, -0.2F, -0.3F))
                .texOffs(224, 209).addBox(-0.5F, -4.5F, -1F, 1F, 1F, 2F, new CubeDeformation(-0.2F, 0F, -0.4F)),
                PartPose.offsetAndRotation(0F, 4F, 2.9F, -0.6981F, 0F, 0F));
        PartDefinition p_earth_plate_2 = p_earth_chest_body.addOrReplaceChild("earth_plate_2", CubeListBuilder.create()
                .texOffs(192, 159).addBox(-0.5F, -2.9F, -1.5F, 1F, 3F, 3F, new CubeDeformation(-0.1F, -0.1F, -0.3F))
                .texOffs(231, 209).addBox(-0.5F, -3.7F, -1F, 1F, 1F, 2F, new CubeDeformation(-0.2F, 0F, -0.4F)),
                PartPose.offsetAndRotation(0F, 7.4F, 2.9F, -0.6981F, 0F, 0F));
        PartDefinition p_earth_chest_left_arm = root.addOrReplaceChild("earth_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_earth_pauldron_left_arm = p_earth_chest_left_arm.addOrReplaceChild("earth_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(186, 52).addBox(-3.3F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F))
                .texOffs(116, 85).addBox(-2F, -0.1F, -4F, 6F, 2F, 8F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.2094F));
        PartDefinition p_earth_claw_left_arm_0 = p_earth_pauldron_left_arm.addOrReplaceChild("earth_claw_left_arm_0", CubeListBuilder.create()
                .texOffs(234, 194).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.285F, -0.35F)),
                PartPose.offsetAndRotation(2.4F, -1.4F, 0F, 0F, 0F, 0.6981F));
        PartDefinition p_earth_claw_left_arm_1 = p_earth_claw_left_arm_0.addOrReplaceChild("earth_claw_left_arm_1", CubeListBuilder.create()
                .texOffs(100, 221).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.35F, -0.1425F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_earth_chest_right_arm = root.addOrReplaceChild("earth_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_earth_pauldron_right_arm = p_earth_chest_right_arm.addOrReplaceChild("earth_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(217, 52).addBox(-3.7F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F))
                .texOffs(145, 85).addBox(-4F, -0.1F, -4F, 6F, 2F, 8F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.2094F));
        PartDefinition p_earth_claw_right_arm_0 = p_earth_pauldron_right_arm.addOrReplaceChild("earth_claw_right_arm_0", CubeListBuilder.create()
                .texOffs(243, 194).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.285F, -0.35F)),
                PartPose.offsetAndRotation(-2.4F, -1.4F, 0F, 0F, 0F, -0.6981F));
        PartDefinition p_earth_claw_right_arm_1 = p_earth_claw_right_arm_0.addOrReplaceChild("earth_claw_right_arm_1", CubeListBuilder.create()
                .texOffs(105, 221).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.35F, -0.1425F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_earth_legs_body = root.addOrReplaceChild("earth_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_earth_tail_0 = p_earth_legs_body.addOrReplaceChild("earth_tail_0", CubeListBuilder.create()
                .texOffs(208, 152).addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F),
                PartPose.offsetAndRotation(0F, 10.2F, 2.2F, 0.5236F, 0F, 0F));
        PartDefinition p_earth_tail_1 = p_earth_tail_0.addOrReplaceChild("earth_tail_1", CubeListBuilder.create()
                .texOffs(221, 152).addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, 2.75F, 0F, 0.1745F, 0F, 0F));
        PartDefinition p_earth_tail_2 = p_earth_tail_1.addOrReplaceChild("earth_tail_2", CubeListBuilder.create()
                .texOffs(234, 152).addBox(-1.5F, -0.1F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.1F, -0.4F)),
                PartPose.offsetAndRotation(0F, 2.75F, 0F, 0.2094F, 0F, 0F));
        PartDefinition p_earth_tail_3 = p_earth_tail_2.addOrReplaceChild("earth_tail_3", CubeListBuilder.create()
                .texOffs(162, 178).addBox(-1F, -0.2F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.15F, -0.2F, -0.15F)),
                PartPose.offsetAndRotation(0F, 2.55F, 0F, 0.2094F, 0F, 0F));
        PartDefinition p_earth_tail_4 = p_earth_tail_3.addOrReplaceChild("earth_tail_4", CubeListBuilder.create()
                .texOffs(171, 178).addBox(-1F, -0.4F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.4F, -0.4F, -0.4F)),
                PartPose.offsetAndRotation(0F, 2.35F, 0F, 0.1745F, 0F, 0F));
        PartDefinition p_earth_tail_tip = p_earth_tail_4.addOrReplaceChild("earth_tail_tip", CubeListBuilder.create()
                .texOffs(194, 128).addBox(-2F, -0.1F, -2F, 4F, 3F, 4F, new CubeDeformation(-0.4F, -0.1F, -0.4F))
                .texOffs(238, 209).addBox(-1F, 0.2F, -2.15F, 2F, 2F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.25F)),
                PartPose.offsetAndRotation(0F, 2.4F, 0F, 0.1396F, 0F, 0F));
        PartDefinition p_earth_legs_left_leg = root.addOrReplaceChild("earth_legs_left_leg", CubeListBuilder.create()
                .texOffs(193, 184).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_earth_legs_right_leg = root.addOrReplaceChild("earth_legs_right_leg", CubeListBuilder.create()
                .texOffs(204, 184).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_earth_feet_left_leg = root.addOrReplaceChild("earth_feet_left_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_earth_talon_left_leg_0 = p_earth_feet_left_leg.addOrReplaceChild("earth_talon_left_leg_0", CubeListBuilder.create()
                .texOffs(245, 209).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(-1.6F, 11.6F, -2.8F, 0.4887F, -0.2234F, 0F));
        PartDefinition p_earth_talon_left_leg_1 = p_earth_feet_left_leg.addOrReplaceChild("earth_talon_left_leg_1", CubeListBuilder.create()
                .texOffs(0, 213).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(0F, 11.6F, -2.8F, 0.4887F, 0F, 0F));
        PartDefinition p_earth_talon_left_leg_2 = p_earth_feet_left_leg.addOrReplaceChild("earth_talon_left_leg_2", CubeListBuilder.create()
                .texOffs(7, 213).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(1.6F, 11.6F, -2.8F, 0.4887F, 0.2234F, 0F));
        PartDefinition p_earth_feet_right_leg = root.addOrReplaceChild("earth_feet_right_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_earth_talon_right_leg_0 = p_earth_feet_right_leg.addOrReplaceChild("earth_talon_right_leg_0", CubeListBuilder.create()
                .texOffs(14, 213).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(-1.6F, 11.6F, -2.8F, 0.4887F, -0.2234F, 0F));
        PartDefinition p_earth_talon_right_leg_1 = p_earth_feet_right_leg.addOrReplaceChild("earth_talon_right_leg_1", CubeListBuilder.create()
                .texOffs(21, 213).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(0F, 11.6F, -2.8F, 0.4887F, 0F, 0F));
        PartDefinition p_earth_talon_right_leg_2 = p_earth_feet_right_leg.addOrReplaceChild("earth_talon_right_leg_2", CubeListBuilder.create()
                .texOffs(28, 213).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(1.6F, 11.6F, -2.8F, 0.4887F, 0.2234F, 0F));
        PartDefinition p_water_head_head = root.addOrReplaceChild("water_head_head", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_head_fin_left = p_water_head_head.addOrReplaceChild("head_fin_left", CubeListBuilder.create(),
                PartPose.offsetAndRotation(5.3F, -4.6F, -1F, 0F, 0.2443F, 0F));
        PartDefinition p_head_fin_left_ray_0 = p_head_fin_left.addOrReplaceChild("head_fin_left_ray_0", CubeListBuilder.create()
                .texOffs(52, 137).addBox(-0.5F, -1F, -0.2F, 1F, 2F, 5F, new CubeDeformation(-0.25F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.1047F, 0F, 0F));
        PartDefinition p_head_fin_left_ray_1 = p_head_fin_left.addOrReplaceChild("head_fin_left_ray_1", CubeListBuilder.create()
                .texOffs(49, 128).addBox(-0.5F, -1F, -0.3F, 1F, 2F, 6F, new CubeDeformation(-0.25F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0.2443F, 0F, 0F));
        PartDefinition p_head_fin_left_ray_2 = p_head_fin_left.addOrReplaceChild("head_fin_left_ray_2", CubeListBuilder.create()
                .texOffs(65, 137).addBox(-0.5F, -1F, 0F, 1F, 2F, 5F, new CubeDeformation(-0.25F, -0.25F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0.5934F, 0F, 0F));
        PartDefinition p_head_fin_left_ray_3 = p_head_fin_left.addOrReplaceChild("head_fin_left_ray_3", CubeListBuilder.create()
                .texOffs(61, 159).addBox(-0.5F, -1F, -0.1F, 1F, 2F, 4F, new CubeDeformation(-0.25F, -0.35F, -0.1F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0.9425F, 0F, 0F));
        PartDefinition p_head_fin_right = p_water_head_head.addOrReplaceChild("head_fin_right", CubeListBuilder.create(),
                PartPose.offsetAndRotation(-5.3F, -4.6F, -1F, 0F, -0.2443F, 0F));
        PartDefinition p_head_fin_right_ray_0 = p_head_fin_right.addOrReplaceChild("head_fin_right_ray_0", CubeListBuilder.create()
                .texOffs(78, 137).addBox(-0.5F, -1F, -0.2F, 1F, 2F, 5F, new CubeDeformation(-0.25F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.1047F, 0F, 0F));
        PartDefinition p_head_fin_right_ray_1 = p_head_fin_right.addOrReplaceChild("head_fin_right_ray_1", CubeListBuilder.create()
                .texOffs(64, 128).addBox(-0.5F, -1F, -0.3F, 1F, 2F, 6F, new CubeDeformation(-0.25F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0.2443F, 0F, 0F));
        PartDefinition p_head_fin_right_ray_2 = p_head_fin_right.addOrReplaceChild("head_fin_right_ray_2", CubeListBuilder.create()
                .texOffs(91, 137).addBox(-0.5F, -1F, 0F, 1F, 2F, 5F, new CubeDeformation(-0.25F, -0.25F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0.5934F, 0F, 0F));
        PartDefinition p_head_fin_right_ray_3 = p_head_fin_right.addOrReplaceChild("head_fin_right_ray_3", CubeListBuilder.create()
                .texOffs(72, 159).addBox(-0.5F, -1F, -0.1F, 1F, 2F, 4F, new CubeDeformation(-0.25F, -0.35F, -0.1F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0.9425F, 0F, 0F));
        PartDefinition p_water_crest_0_0 = p_water_head_head.addOrReplaceChild("water_crest_0_0", CubeListBuilder.create()
                .texOffs(50, 229).addBox(-0.5F, -0.94F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.06F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, -3.2F, -0.4538F, 0F, 0F));
        PartDefinition p_water_crest_0_1 = p_water_crest_0_0.addOrReplaceChild("water_crest_0_1", CubeListBuilder.create()
                .texOffs(55, 229).addBox(-0.5F, -0.9F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.1F, -0.225F)),
                PartPose.offset(0F, -0.63F, 0F));
        PartDefinition p_water_crest_1_0 = p_water_head_head.addOrReplaceChild("water_crest_1_0", CubeListBuilder.create()
                .texOffs(110, 221).addBox(-0.5F, -1.605F, -0.5F, 1F, 2F, 1F, new CubeDeformation(0F, -0.395F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, -0.6F, -0.4538F, 0F, 0F));
        PartDefinition p_water_crest_1_1 = p_water_crest_1_0.addOrReplaceChild("water_crest_1_1", CubeListBuilder.create()
                .texOffs(115, 221).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.225F, -0.45F, -0.225F)),
                PartPose.offset(0F, -0.96F, 0F));
        PartDefinition p_water_crest_2_0 = p_water_head_head.addOrReplaceChild("water_crest_2_0", CubeListBuilder.create()
                .texOffs(60, 229).addBox(-0.5F, -0.995F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.005F, 0F)),
                PartPose.offsetAndRotation(0F, -9.2F, 2F, -0.4538F, 0F, 0F));
        PartDefinition p_water_crest_2_1 = p_water_crest_2_0.addOrReplaceChild("water_crest_2_1", CubeListBuilder.create()
                .texOffs(65, 229).addBox(-0.5F, -0.95F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.05F, -0.225F)),
                PartPose.offset(0F, -0.74F, 0F));
        PartDefinition p_water_chest_body = root.addOrReplaceChild("water_chest_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_water_back_fin_0 = p_water_chest_body.addOrReplaceChild("water_back_fin_0", CubeListBuilder.create(),
                PartPose.offset(0F, 0.6F, 2.9F));
        PartDefinition p_water_back_fin_0_ray_0 = p_water_back_fin_0.addOrReplaceChild("water_back_fin_0_ray_0", CubeListBuilder.create()
                .texOffs(79, 128).addBox(-0.5F, -1.5F, -0.3F, 1F, 3F, 5F, new CubeDeformation(-0.25F, -0.4F, -0.3F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.6632F, 0F, 0F));
        PartDefinition p_water_back_fin_0_ray_1 = p_water_back_fin_0.addOrReplaceChild("water_back_fin_0_ray_1", CubeListBuilder.create()
                .texOffs(83, 159).addBox(-0.5F, -1F, -0.24F, 1F, 2F, 4F, new CubeDeformation(-0.25F, 0F, -0.24F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.1396F, 0F, 0F));
        PartDefinition p_water_back_fin_1 = p_water_chest_body.addOrReplaceChild("water_back_fin_1", CubeListBuilder.create(),
                PartPose.offset(0F, 3.4F, 2.9F));
        PartDefinition p_water_back_fin_1_ray_0 = p_water_back_fin_1.addOrReplaceChild("water_back_fin_1_ray_0", CubeListBuilder.create()
                .texOffs(137, 137).addBox(-0.5F, -1.5F, 0F, 1F, 3F, 4F, new CubeDeformation(-0.25F, -0.4F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.6632F, 0F, 0F));
        PartDefinition p_water_back_fin_1_ray_1 = p_water_back_fin_1.addOrReplaceChild("water_back_fin_1_ray_1", CubeListBuilder.create()
                .texOffs(94, 159).addBox(-0.5F, -1F, -0.4F, 1F, 2F, 4F, new CubeDeformation(-0.25F, 0F, -0.4F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.1396F, 0F, 0F));
        PartDefinition p_water_back_fin_2 = p_water_chest_body.addOrReplaceChild("water_back_fin_2", CubeListBuilder.create(),
                PartPose.offset(0F, 6.2F, 2.9F));
        PartDefinition p_water_back_fin_2_ray_0 = p_water_back_fin_2.addOrReplaceChild("water_back_fin_2_ray_0", CubeListBuilder.create()
                .texOffs(148, 137).addBox(-0.5F, -1.5F, -0.3F, 1F, 3F, 4F, new CubeDeformation(-0.25F, -0.4F, -0.3F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.6632F, 0F, 0F));
        PartDefinition p_water_back_fin_2_ray_1 = p_water_back_fin_2.addOrReplaceChild("water_back_fin_2_ray_1", CubeListBuilder.create()
                .texOffs(180, 178).addBox(-0.5F, -1F, -0.14F, 1F, 2F, 3F, new CubeDeformation(-0.25F, 0F, -0.14F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.1396F, 0F, 0F));
        PartDefinition p_water_back_fin_3 = p_water_chest_body.addOrReplaceChild("water_back_fin_3", CubeListBuilder.create(),
                PartPose.offset(0F, 9F, 2.9F));
        PartDefinition p_water_back_fin_3_ray_0 = p_water_back_fin_3.addOrReplaceChild("water_back_fin_3_ray_0", CubeListBuilder.create()
                .texOffs(201, 159).addBox(-0.5F, -1.5F, -0.2F, 1F, 3F, 3F, new CubeDeformation(-0.25F, -0.4F, -0.2F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.6632F, 0F, 0F));
        PartDefinition p_water_back_fin_3_ray_1 = p_water_back_fin_3.addOrReplaceChild("water_back_fin_3_ray_1", CubeListBuilder.create()
                .texOffs(189, 178).addBox(-0.5F, -1F, -0.46F, 1F, 2F, 3F, new CubeDeformation(-0.25F, 0F, -0.46F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.1396F, 0F, 0F));
        PartDefinition p_water_chest_left_arm = root.addOrReplaceChild("water_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_water_pauldron_left_arm = p_water_chest_left_arm.addOrReplaceChild("water_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(0, 63).addBox(-3.3F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F))
                .texOffs(174, 85).addBox(-2F, -0.1F, -4F, 6F, 2F, 8F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.2094F));
        PartDefinition p_water_claw_left_arm_0 = p_water_pauldron_left_arm.addOrReplaceChild("water_claw_left_arm_0", CubeListBuilder.create()
                .texOffs(0, 199).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.285F, -0.35F)),
                PartPose.offsetAndRotation(2.4F, -1.4F, 0F, 0F, 0F, 0.6981F));
        PartDefinition p_water_claw_left_arm_1 = p_water_claw_left_arm_0.addOrReplaceChild("water_claw_left_arm_1", CubeListBuilder.create()
                .texOffs(120, 221).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.35F, -0.1425F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_water_chest_right_arm = root.addOrReplaceChild("water_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_water_pauldron_right_arm = p_water_chest_right_arm.addOrReplaceChild("water_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(31, 63).addBox(-3.7F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.1F, -0.15F, -0.4F))
                .texOffs(203, 85).addBox(-4F, -0.1F, -4F, 6F, 2F, 8F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.2094F));
        PartDefinition p_water_claw_right_arm_0 = p_water_pauldron_right_arm.addOrReplaceChild("water_claw_right_arm_0", CubeListBuilder.create()
                .texOffs(9, 199).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.285F, -0.35F)),
                PartPose.offsetAndRotation(-2.4F, -1.4F, 0F, 0F, 0F, -0.6981F));
        PartDefinition p_water_claw_right_arm_1 = p_water_claw_right_arm_0.addOrReplaceChild("water_claw_right_arm_1", CubeListBuilder.create()
                .texOffs(125, 221).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.35F, -0.1425F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_water_legs_body = root.addOrReplaceChild("water_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_water_tail_0 = p_water_legs_body.addOrReplaceChild("water_tail_0", CubeListBuilder.create()
                .texOffs(0, 159).addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F),
                PartPose.offsetAndRotation(0F, 10.2F, 2.2F, 0.5236F, 0F, 0F));
        PartDefinition p_water_tail_1 = p_water_tail_0.addOrReplaceChild("water_tail_1", CubeListBuilder.create()
                .texOffs(13, 159).addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, 2.75F, 0F, 0.1745F, 0F, 0F));
        PartDefinition p_water_tail_2 = p_water_tail_1.addOrReplaceChild("water_tail_2", CubeListBuilder.create()
                .texOffs(26, 159).addBox(-1.5F, -0.1F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.1F, -0.4F)),
                PartPose.offsetAndRotation(0F, 2.75F, 0F, 0.2094F, 0F, 0F));
        PartDefinition p_water_tail_3 = p_water_tail_2.addOrReplaceChild("water_tail_3", CubeListBuilder.create()
                .texOffs(198, 178).addBox(-1F, -0.2F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.15F, -0.2F, -0.15F)),
                PartPose.offsetAndRotation(0F, 2.55F, 0F, 0.2094F, 0F, 0F));
        PartDefinition p_water_tail_4 = p_water_tail_3.addOrReplaceChild("water_tail_4", CubeListBuilder.create()
                .texOffs(207, 178).addBox(-1F, -0.4F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.4F, -0.4F, -0.4F)),
                PartPose.offsetAndRotation(0F, 2.35F, 0F, 0.1745F, 0F, 0F));
        PartDefinition p_water_tail_tip = p_water_tail_4.addOrReplaceChild("water_tail_tip", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0F, 2.4F, 0F, 0.1396F, 0F, 0F));
        PartDefinition p_water_tail_fin = p_water_tail_tip.addOrReplaceChild("water_tail_fin", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0F, 0.6F, 0F, 1.5708F, 0F, 0F));
        PartDefinition p_water_tail_fin_ray_0 = p_water_tail_fin.addOrReplaceChild("water_tail_fin_ray_0", CubeListBuilder.create()
                .texOffs(105, 159).addBox(-0.5F, -1F, -0.2F, 1F, 2F, 4F, new CubeDeformation(-0.25F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.6981F, 0F, 0F));
        PartDefinition p_water_tail_fin_ray_1 = p_water_tail_fin.addOrReplaceChild("water_tail_fin_ray_1", CubeListBuilder.create()
                .texOffs(116, 159).addBox(-0.5F, -1F, 0F, 1F, 2F, 4F, new CubeDeformation(-0.25F, -0.2F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.1745F, 0F, 0F));
        PartDefinition p_water_tail_fin_ray_2 = p_water_tail_fin.addOrReplaceChild("water_tail_fin_ray_2", CubeListBuilder.create()
                .texOffs(127, 159).addBox(-0.5F, -1F, -0.2F, 1F, 2F, 4F, new CubeDeformation(-0.25F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0.3491F, 0F, 0F));
        PartDefinition p_water_legs_left_leg = root.addOrReplaceChild("water_legs_left_leg", CubeListBuilder.create()
                .texOffs(215, 184).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_water_legs_right_leg = root.addOrReplaceChild("water_legs_right_leg", CubeListBuilder.create()
                .texOffs(226, 184).addBox(-2F, 4.8F, -3.2F, 4F, 3F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_water_feet_left_leg = root.addOrReplaceChild("water_feet_left_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_water_talon_left_leg_0 = p_water_feet_left_leg.addOrReplaceChild("water_talon_left_leg_0", CubeListBuilder.create()
                .texOffs(35, 213).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(-1.6F, 11.6F, -2.8F, 0.4887F, -0.2234F, 0F));
        PartDefinition p_water_talon_left_leg_1 = p_water_feet_left_leg.addOrReplaceChild("water_talon_left_leg_1", CubeListBuilder.create()
                .texOffs(42, 213).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(0F, 11.6F, -2.8F, 0.4887F, 0F, 0F));
        PartDefinition p_water_talon_left_leg_2 = p_water_feet_left_leg.addOrReplaceChild("water_talon_left_leg_2", CubeListBuilder.create()
                .texOffs(49, 213).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(1.6F, 11.6F, -2.8F, 0.4887F, 0.2234F, 0F));
        PartDefinition p_water_feet_right_leg = root.addOrReplaceChild("water_feet_right_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_water_talon_right_leg_0 = p_water_feet_right_leg.addOrReplaceChild("water_talon_right_leg_0", CubeListBuilder.create()
                .texOffs(56, 213).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(-1.6F, 11.6F, -2.8F, 0.4887F, -0.2234F, 0F));
        PartDefinition p_water_talon_right_leg_1 = p_water_feet_right_leg.addOrReplaceChild("water_talon_right_leg_1", CubeListBuilder.create()
                .texOffs(63, 213).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(0F, 11.6F, -2.8F, 0.4887F, 0F, 0F));
        PartDefinition p_water_talon_right_leg_2 = p_water_feet_right_leg.addOrReplaceChild("water_talon_right_leg_2", CubeListBuilder.create()
                .texOffs(70, 213).addBox(-0.5F, -0.5F, -2F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(1.6F, 11.6F, -2.8F, 0.4887F, 0.2234F, 0F));
        PartDefinition p_crown_head_head = root.addOrReplaceChild("crown_head_head", CubeListBuilder.create()
                .texOffs(134, 204).addBox(-5.5F, -10F, -5.5F, 11F, 2F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.1F))
                .texOffs(159, 204).addBox(-5.5F, -10F, 4.5F, 11F, 2F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.1F))
                .texOffs(144, 0).addBox(-5.5F, -10F, -5F, 1F, 2F, 10F, new CubeDeformation(-0.1F, -0.1F, -0.4F))
                .texOffs(167, 0).addBox(4.5F, -10F, -5F, 1F, 2F, 10F, new CubeDeformation(-0.1F, -0.1F, -0.4F))
                .texOffs(77, 213).addBox(-1F, -10F, -6.1F, 2F, 2F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.2F))
                .texOffs(70, 229).addBox(-4F, -9.5F, -6.05F, 1F, 1F, 1F, new CubeDeformation(0F, -0.1F, -0.25F))
                .texOffs(75, 229).addBox(3F, -9.5F, -6.05F, 1F, 1F, 1F, new CubeDeformation(0F, -0.1F, -0.25F))
                .texOffs(84, 213).addBox(-6.1F, -9.5F, -1F, 1F, 1F, 2F, new CubeDeformation(-0.2F, 0F, -0.4F))
                .texOffs(91, 213).addBox(5.1F, -9.5F, -1F, 1F, 1F, 2F, new CubeDeformation(-0.2F, 0F, -0.4F))
                .texOffs(166, 225).addBox(-1F, -9.5F, 5.1F, 2F, 1F, 1F, new CubeDeformation(-0.4F, 0F, -0.2F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_crown_point_0_0 = p_crown_head_head.addOrReplaceChild("crown_point_0_0", CubeListBuilder.create()
                .texOffs(98, 213).addBox(-1F, -1.96F, -0.5F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.04F, -0.1F)),
                PartPose.offset(0F, -9.7F, -5.4F));
        PartDefinition p_crown_point_0_1 = p_crown_point_0_0.addOrReplaceChild("crown_point_0_1", CubeListBuilder.create()
                .texOffs(130, 221).addBox(-0.5F, -1.8F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.15F, -0.2F, -0.25F))
                .texOffs(80, 229).addBox(-0.5F, -2.4F, -0.5F, 1F, 1F, 1F),
                PartPose.offset(0F, -1.67F, 0F));
        PartDefinition p_crown_point_1_0 = p_crown_head_head.addOrReplaceChild("crown_point_1_0", CubeListBuilder.create()
                .texOffs(105, 213).addBox(-1F, -1.66F, -0.5F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.34F, -0.1F)),
                PartPose.offset(-3.2F, -9.7F, -5.4F));
        PartDefinition p_crown_point_1_1 = p_crown_point_1_0.addOrReplaceChild("crown_point_1_1", CubeListBuilder.create()
                .texOffs(135, 221).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.15F, -0.45F, -0.25F))
                .texOffs(85, 229).addBox(-0.5F, -1.9F, -0.5F, 1F, 1F, 1F),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_crown_point_2_0 = p_crown_head_head.addOrReplaceChild("crown_point_2_0", CubeListBuilder.create()
                .texOffs(112, 213).addBox(-1F, -1.66F, -0.5F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.34F, -0.1F)),
                PartPose.offset(3.2F, -9.7F, -5.4F));
        PartDefinition p_crown_point_2_1 = p_crown_point_2_0.addOrReplaceChild("crown_point_2_1", CubeListBuilder.create()
                .texOffs(140, 221).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.15F, -0.45F, -0.25F))
                .texOffs(90, 229).addBox(-0.5F, -1.9F, -0.5F, 1F, 1F, 1F),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_crown_point_3_0 = p_crown_head_head.addOrReplaceChild("crown_point_3_0", CubeListBuilder.create()
                .texOffs(119, 213).addBox(-1F, -1.72F, -0.5F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.28F, -0.1F)),
                PartPose.offset(0F, -9.7F, 5.4F));
        PartDefinition p_crown_point_3_1 = p_crown_point_3_0.addOrReplaceChild("crown_point_3_1", CubeListBuilder.create()
                .texOffs(145, 221).addBox(-0.5F, -1.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.15F, -0.4F, -0.25F))
                .texOffs(95, 229).addBox(-0.5F, -2F, -0.5F, 1F, 1F, 1F),
                PartPose.offset(0F, -1.19F, 0F));
        PartDefinition p_crown_point_4_0 = p_crown_head_head.addOrReplaceChild("crown_point_4_0", CubeListBuilder.create()
                .texOffs(126, 213).addBox(-1F, -1.54F, -0.5F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.46F, -0.1F)),
                PartPose.offset(-3.2F, -9.7F, 5.4F));
        PartDefinition p_crown_point_4_1 = p_crown_point_4_0.addOrReplaceChild("crown_point_4_1", CubeListBuilder.create()
                .texOffs(100, 229).addBox(-0.5F, -0.95F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.15F, -0.05F, -0.25F))
                .texOffs(105, 229).addBox(-0.5F, -1.7F, -0.5F, 1F, 1F, 1F),
                PartPose.offset(0F, -0.83F, 0F));
        PartDefinition p_crown_point_5_0 = p_crown_head_head.addOrReplaceChild("crown_point_5_0", CubeListBuilder.create()
                .texOffs(133, 213).addBox(-1F, -1.54F, -0.5F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.46F, -0.1F)),
                PartPose.offset(3.2F, -9.7F, 5.4F));
        PartDefinition p_crown_point_5_1 = p_crown_point_5_0.addOrReplaceChild("crown_point_5_1", CubeListBuilder.create()
                .texOffs(110, 229).addBox(-0.5F, -0.95F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.15F, -0.05F, -0.25F))
                .texOffs(115, 229).addBox(-0.5F, -1.7F, -0.5F, 1F, 1F, 1F),
                PartPose.offset(0F, -0.83F, 0F));
        PartDefinition p_crown_point_6_0 = p_crown_head_head.addOrReplaceChild("crown_point_6_0", CubeListBuilder.create()
                .texOffs(18, 199).addBox(-1F, -1.6F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.4F, -0.3F)),
                PartPose.offset(-5.4F, -9.7F, -1.8F));
        PartDefinition p_crown_point_6_1 = p_crown_point_6_0.addOrReplaceChild("crown_point_6_1", CubeListBuilder.create()
                .texOffs(120, 229).addBox(-0.5F, -1F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.15F, 0F, -0.15F))
                .texOffs(125, 229).addBox(-0.5F, -1.8F, -0.5F, 1F, 1F, 1F),
                PartPose.offset(0F, -0.95F, 0F));
        PartDefinition p_crown_point_7_0 = p_crown_head_head.addOrReplaceChild("crown_point_7_0", CubeListBuilder.create()
                .texOffs(27, 199).addBox(-1F, -1.6F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.4F, -0.3F)),
                PartPose.offset(-5.4F, -9.7F, 1.8F));
        PartDefinition p_crown_point_7_1 = p_crown_point_7_0.addOrReplaceChild("crown_point_7_1", CubeListBuilder.create()
                .texOffs(130, 229).addBox(-0.5F, -1F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.15F, 0F, -0.15F))
                .texOffs(135, 229).addBox(-0.5F, -1.8F, -0.5F, 1F, 1F, 1F),
                PartPose.offset(0F, -0.95F, 0F));
        PartDefinition p_crown_point_8_0 = p_crown_head_head.addOrReplaceChild("crown_point_8_0", CubeListBuilder.create()
                .texOffs(36, 199).addBox(-1F, -1.6F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.4F, -0.3F)),
                PartPose.offset(5.4F, -9.7F, -1.8F));
        PartDefinition p_crown_point_8_1 = p_crown_point_8_0.addOrReplaceChild("crown_point_8_1", CubeListBuilder.create()
                .texOffs(140, 229).addBox(-0.5F, -1F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.15F, 0F, -0.15F))
                .texOffs(145, 229).addBox(-0.5F, -1.8F, -0.5F, 1F, 1F, 1F),
                PartPose.offset(0F, -0.95F, 0F));
        PartDefinition p_crown_point_9_0 = p_crown_head_head.addOrReplaceChild("crown_point_9_0", CubeListBuilder.create()
                .texOffs(45, 199).addBox(-1F, -1.6F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.4F, -0.3F)),
                PartPose.offset(5.4F, -9.7F, 1.8F));
        PartDefinition p_crown_point_9_1 = p_crown_point_9_0.addOrReplaceChild("crown_point_9_1", CubeListBuilder.create()
                .texOffs(150, 229).addBox(-0.5F, -1F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.15F, 0F, -0.15F))
                .texOffs(155, 229).addBox(-0.5F, -1.8F, -0.5F, 1F, 1F, 1F),
                PartPose.offset(0F, -0.95F, 0F));
        PartDefinition p_leather_head_head = root.addOrReplaceChild("leather_head_head", CubeListBuilder.create()
                .texOffs(194, 166).addBox(4.2F, -7.5F, -0.1F, 2F, 2F, 3F, new CubeDeformation(-0.4F, -0.1F, -0.3F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_leather_feather = p_leather_head_head.addOrReplaceChild("leather_feather", CubeListBuilder.create()
                .texOffs(224, 0).addBox(-0.5F, -8.6F, -1.5F, 1F, 9F, 3F, new CubeDeformation(-0.2F, -0.4F, -0.4F)),
                PartPose.offsetAndRotation(5F, -6.4F, 1.4F, -0.5236F, 0F, 0.2793F));
        PartDefinition p_leather_chest_body = root.addOrReplaceChild("leather_chest_body", CubeListBuilder.create()
                .texOffs(0, 17).addBox(-6F, -1.9F, -4F, 12F, 3F, 8F, new CubeDeformation(-0.25F, 0.05F, -0.15F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_leather_chest_left_arm = root.addOrReplaceChild("leather_chest_left_arm", CubeListBuilder.create()
                .texOffs(0, 96).addBox(-2.45F, -4.4F, -3.5F, 7F, 3F, 7F, new CubeDeformation(0.05F, -0.3F, 0.1F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_leather_chest_right_arm = root.addOrReplaceChild("leather_chest_right_arm", CubeListBuilder.create()
                .texOffs(29, 96).addBox(-4.55F, -4.4F, -3.5F, 7F, 3F, 7F, new CubeDeformation(0.05F, -0.3F, 0.1F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_leather_legs_body = root.addOrReplaceChild("leather_legs_body", CubeListBuilder.create()
                .texOffs(118, 118).addBox(-5F, 10.2F, -3F, 10F, 2F, 6F, new CubeDeformation(-0.1F, -0.4F, -0.1F))
                .texOffs(205, 166).addBox(2.1F, 10.7F, -4F, 3F, 3F, 2F, new CubeDeformation(-0.3F, -0.2F, -0.4F))
                .texOffs(140, 213).addBox(-4.5F, 10.2F, -3.5F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.2F, -0.2F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_leather_legs_left_leg = root.addOrReplaceChild("leather_legs_left_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_leather_legs_right_leg = root.addOrReplaceChild("leather_legs_right_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_leather_feet_left_leg = root.addOrReplaceChild("leather_feet_left_leg", CubeListBuilder.create()
                .texOffs(173, 17).addBox(-4F, 6.6F, -4F, 8F, 3F, 8F, new CubeDeformation(-0.4F, 0F, -0.4F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_leather_feet_right_leg = root.addOrReplaceChild("leather_feet_right_leg", CubeListBuilder.create()
                .texOffs(206, 17).addBox(-4F, 6.6F, -4F, 8F, 3F, 8F, new CubeDeformation(-0.4F, 0F, -0.4F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_chainmail_head_head = root.addOrReplaceChild("chainmail_head_head", CubeListBuilder.create()
                .texOffs(123, 128).addBox(-5.5F, -3.9F, 4.6F, 11F, 6F, 1F, new CubeDeformation(-0.2F, -0.3F, -0.2F))
                .texOffs(0, 0).addBox(-5.8F, -3.9F, -4.7F, 1F, 6F, 10F, new CubeDeformation(-0.2F, -0.3F, -0.3F))
                .texOffs(23, 0).addBox(4.8F, -3.9F, -4.7F, 1F, 6F, 10F, new CubeDeformation(-0.2F, -0.3F, -0.3F))
                .texOffs(239, 178).addBox(-0.5F, -6F, -5.85F, 1F, 4F, 1F, new CubeDeformation(0F, 0F, -0.15F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_chainmail_chest_body = root.addOrReplaceChild("chainmail_chest_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_chainmail_chest_left_arm = root.addOrReplaceChild("chainmail_chest_left_arm", CubeListBuilder.create()
                .texOffs(151, 118).addBox(-2.4F, -3.7F, -3.5F, 7F, 1F, 7F, new CubeDeformation(-0.2F, 0F, 0F))
                .texOffs(190, 0).addBox(3.7F, -3.6F, -3.5F, 1F, 5F, 7F, new CubeDeformation(-0.2F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_chainmail_chest_right_arm = root.addOrReplaceChild("chainmail_chest_right_arm", CubeListBuilder.create()
                .texOffs(180, 118).addBox(-4.6F, -3.7F, -3.5F, 7F, 1F, 7F, new CubeDeformation(-0.2F, 0F, 0F))
                .texOffs(207, 0).addBox(-4.7F, -3.6F, -3.5F, 1F, 5F, 7F, new CubeDeformation(-0.2F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_chainmail_legs_body = root.addOrReplaceChild("chainmail_legs_body", CubeListBuilder.create()
                .texOffs(177, 137).addBox(-5F, 9.8F, -3.2F, 10F, 5F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(200, 137).addBox(-5F, 9.8F, 2.2F, 10F, 5F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.2F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_chainmail_legs_left_leg = root.addOrReplaceChild("chainmail_legs_left_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_chainmail_legs_right_leg = root.addOrReplaceChild("chainmail_legs_right_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_iron_head_head = root.addOrReplaceChild("iron_head_head", CubeListBuilder.create()
                .texOffs(210, 159).addBox(-1F, -6.7F, -5.9F, 2F, 5F, 1F, new CubeDeformation(-0.4F, -0.3F, -0.1F))
                .texOffs(66, 29).addBox(-1F, -10.45F, -4.5F, 2F, 2F, 9F, new CubeDeformation(-0.4F, -0.45F, -0.1F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_iron_chest_body = root.addOrReplaceChild("iron_chest_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_iron_chest_left_arm = root.addOrReplaceChild("iron_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_iron_pauldron_left_arm = p_iron_chest_left_arm.addOrReplaceChild("iron_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(119, 107).addBox(-3.2F, -1.5F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.2F, -0.1F, 0F))
                .texOffs(58, 96).addBox(-1.9F, 0.2F, -4F, 6F, 2F, 8F, new CubeDeformation(-0.1F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.2094F));
        PartDefinition p_iron_chest_right_arm = root.addOrReplaceChild("iron_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_iron_pauldron_right_arm = p_iron_chest_right_arm.addOrReplaceChild("iron_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(148, 107).addBox(-3.8F, -1.5F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.2F, -0.1F, 0F))
                .texOffs(87, 96).addBox(-4.1F, 0.2F, -4F, 6F, 2F, 8F, new CubeDeformation(-0.1F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.2094F));
        PartDefinition p_iron_legs_body = root.addOrReplaceChild("iron_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_iron_legs_left_leg = root.addOrReplaceChild("iron_legs_left_leg", CubeListBuilder.create()
                .texOffs(237, 184).addBox(-2F, 5F, -3.2F, 4F, 3F, 1F, new CubeDeformation(-0.1F, -0.1F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_iron_legs_right_leg = root.addOrReplaceChild("iron_legs_right_leg", CubeListBuilder.create()
                .texOffs(0, 189).addBox(-2F, 5F, -3.2F, 4F, 3F, 1F, new CubeDeformation(-0.1F, -0.1F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_iron_feet_left_leg = root.addOrReplaceChild("iron_feet_left_leg", CubeListBuilder.create()
                .texOffs(30, 166).addBox(-2.5F, 10.25F, -4.35F, 5F, 3F, 2F, new CubeDeformation(-0.1F, -0.35F, -0.35F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_iron_feet_right_leg = root.addOrReplaceChild("iron_feet_right_leg", CubeListBuilder.create()
                .texOffs(45, 166).addBox(-2.5F, 10.25F, -4.35F, 5F, 3F, 2F, new CubeDeformation(-0.1F, -0.35F, -0.35F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_gold_head_head = root.addOrReplaceChild("gold_head_head", CubeListBuilder.create()
                .texOffs(147, 213).addBox(-1F, -7.2F, -5.85F, 2F, 2F, 1F, new CubeDeformation(0F, 0F, -0.15F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_gold_ray_0_0 = p_gold_head_head.addOrReplaceChild("gold_ray_0_0", CubeListBuilder.create()
                .texOffs(54, 199).addBox(-1F, -1.935F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.065F, -0.35F)),
                PartPose.offsetAndRotation(0F, -9.2F, -1F, -0.384F, 0F, -0.5236F));
        PartDefinition p_gold_ray_0_1 = p_gold_ray_0_0.addOrReplaceChild("gold_ray_0_1", CubeListBuilder.create()
                .texOffs(150, 221).addBox(-0.5F, -1.85F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.15F, -0.1425F)),
                PartPose.offset(0F, -1.62F, 0F));
        PartDefinition p_gold_ray_1_0 = p_gold_head_head.addOrReplaceChild("gold_ray_1_0", CubeListBuilder.create()
                .texOffs(216, 178).addBox(-1F, -2.765F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.35F, -0.235F, -0.35F)),
                PartPose.offsetAndRotation(0F, -9.2F, -1F, -0.1745F, 0F, 0F));
        PartDefinition p_gold_ray_1_1 = p_gold_ray_1_0.addOrReplaceChild("gold_ray_1_1", CubeListBuilder.create()
                .texOffs(102, 204).addBox(-0.5F, -2.65F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.1425F, -0.35F, -0.1425F)),
                PartPose.offset(0F, -2.28F, 0F));
        PartDefinition p_gold_ray_2_0 = p_gold_head_head.addOrReplaceChild("gold_ray_2_0", CubeListBuilder.create()
                .texOffs(63, 199).addBox(-1F, -1.935F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.065F, -0.35F)),
                PartPose.offsetAndRotation(0F, -9.2F, -1F, 0.0349F, 0F, 0.5236F));
        PartDefinition p_gold_ray_2_1 = p_gold_ray_2_0.addOrReplaceChild("gold_ray_2_1", CubeListBuilder.create()
                .texOffs(155, 221).addBox(-0.5F, -1.85F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1425F, -0.15F, -0.1425F)),
                PartPose.offset(0F, -1.62F, 0F));
        PartDefinition p_gold_chest_body = root.addOrReplaceChild("gold_chest_body", CubeListBuilder.create()
                .texOffs(216, 166).addBox(-2F, 2.2F, -3.85F, 4F, 4F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.15F))
                .texOffs(154, 213).addBox(-1F, 3.2F, -4.35F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.3F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_gold_chest_left_arm = root.addOrReplaceChild("gold_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_gold_pauldron_left_arm = p_gold_chest_left_arm.addOrReplaceChild("gold_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(62, 63).addBox(-3.2F, -1.6F, -4F, 7F, 2F, 8F, new CubeDeformation(0F, 0F, -0.4F))
                .texOffs(93, 63).addBox(-2.4F, 0.1F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.4F, -0.2F, -0.3F))
                .texOffs(213, 204).addBox(1.1F, -2.3F, -1F, 2F, 1F, 2F, new CubeDeformation(-0.3F, -0.1F, -0.3F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.2443F));
        PartDefinition p_gold_chest_right_arm = root.addOrReplaceChild("gold_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_gold_pauldron_right_arm = p_gold_chest_right_arm.addOrReplaceChild("gold_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(124, 63).addBox(-3.8F, -1.6F, -4F, 7F, 2F, 8F, new CubeDeformation(0F, 0F, -0.4F))
                .texOffs(155, 63).addBox(-4.6F, 0.1F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.4F, -0.2F, -0.3F))
                .texOffs(222, 204).addBox(-3.1F, -2.3F, -1F, 2F, 1F, 2F, new CubeDeformation(-0.3F, -0.1F, -0.3F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.2443F));
        PartDefinition p_gold_legs_body = root.addOrReplaceChild("gold_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_gold_legs_left_leg = root.addOrReplaceChild("gold_legs_left_leg", CubeListBuilder.create()
                .texOffs(45, 145).addBox(-3F, -0.9F, -3.3F, 6F, 5F, 1F, new CubeDeformation(-0.3F, -0.3F, 0F))
                .texOffs(11, 189).addBox(-2F, 5F, -3.2F, 4F, 3F, 1F, new CubeDeformation(-0.1F, -0.1F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_gold_legs_right_leg = root.addOrReplaceChild("gold_legs_right_leg", CubeListBuilder.create()
                .texOffs(60, 145).addBox(-3F, -0.9F, -3.3F, 6F, 5F, 1F, new CubeDeformation(-0.3F, -0.3F, 0F))
                .texOffs(22, 189).addBox(-2F, 5F, -3.2F, 4F, 3F, 1F, new CubeDeformation(-0.1F, -0.1F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_gold_feet_left_leg = root.addOrReplaceChild("gold_feet_left_leg", CubeListBuilder.create()
                .texOffs(60, 166).addBox(-2.5F, 10.25F, -4.35F, 5F, 3F, 2F, new CubeDeformation(-0.1F, -0.35F, -0.35F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_gold_feet_right_leg = root.addOrReplaceChild("gold_feet_right_leg", CubeListBuilder.create()
                .texOffs(75, 166).addBox(-2.5F, 10.25F, -4.35F, 5F, 3F, 2F, new CubeDeformation(-0.1F, -0.35F, -0.35F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_diamond_head_head = root.addOrReplaceChild("diamond_head_head", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_diamond_crest_0_0 = p_diamond_head_head.addOrReplaceChild("diamond_crest_0_0", CubeListBuilder.create()
                .texOffs(72, 199).addBox(-1F, -1.715F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.2F, -0.285F, -0.2F)),
                PartPose.offsetAndRotation(0F, -9.2F, -3F, -0.3491F, 0F, 0F));
        PartDefinition p_diamond_crest_0_1 = p_diamond_crest_0_0.addOrReplaceChild("diamond_crest_0_1", CubeListBuilder.create()
                .texOffs(160, 221).addBox(-0.5F, -1.65F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.06F, -0.35F, -0.06F)),
                PartPose.offset(0F, -1.18F, 0F));
        PartDefinition p_diamond_crest_1_0 = p_diamond_head_head.addOrReplaceChild("diamond_crest_1_0", CubeListBuilder.create()
                .texOffs(81, 199).addBox(-1F, -1.99F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.2F, -0.01F, -0.2F)),
                PartPose.offsetAndRotation(0F, -9.2F, -0.4F, -0.3491F, 0F, 0F));
        PartDefinition p_diamond_crest_1_1 = p_diamond_crest_1_0.addOrReplaceChild("diamond_crest_1_1", CubeListBuilder.create()
                .texOffs(165, 221).addBox(-0.5F, -1.9F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.06F, -0.1F, -0.06F)),
                PartPose.offset(0F, -1.73F, 0F));
        PartDefinition p_diamond_crest_2_0 = p_diamond_head_head.addOrReplaceChild("diamond_crest_2_0", CubeListBuilder.create()
                .texOffs(90, 199).addBox(-1F, -1.77F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.2F, -0.23F, -0.2F)),
                PartPose.offsetAndRotation(0F, -9.2F, 2.2F, -0.3491F, 0F, 0F));
        PartDefinition p_diamond_crest_2_1 = p_diamond_crest_2_0.addOrReplaceChild("diamond_crest_2_1", CubeListBuilder.create()
                .texOffs(170, 221).addBox(-0.5F, -1.7F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.06F, -0.3F, -0.06F)),
                PartPose.offset(0F, -1.29F, 0F));
        PartDefinition p_diamond_chest_body = root.addOrReplaceChild("diamond_chest_body", CubeListBuilder.create()
                .texOffs(46, 204).addBox(-1F, 1.8F, -3.9F, 2F, 3F, 1F, new CubeDeformation(0F, -0.2F, -0.1F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_diamond_chest_left_arm = root.addOrReplaceChild("diamond_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_diamond_pauldron_left_arm = p_diamond_chest_left_arm.addOrReplaceChild("diamond_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(177, 107).addBox(-3.2F, -1.6F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.2F, -0.2F, 0F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.1745F));
        PartDefinition p_diamond_shoulder_left_arm_0_0 = p_diamond_pauldron_left_arm.addOrReplaceChild("diamond_shoulder_left_arm_0_0", CubeListBuilder.create()
                .texOffs(99, 199).addBox(-1F, -1.88F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.25F, -0.12F, -0.25F)),
                PartPose.offsetAndRotation(1.8F, -1.3F, -1F, 0.1396F, 0F, 0.4189F));
        PartDefinition p_diamond_shoulder_left_arm_0_1 = p_diamond_shoulder_left_arm_0_0.addOrReplaceChild("diamond_shoulder_left_arm_0_1", CubeListBuilder.create()
                .texOffs(175, 221).addBox(-0.5F, -1.8F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.0875F, -0.2F, -0.0875F)),
                PartPose.offset(0F, -1.51F, 0F));
        PartDefinition p_diamond_shoulder_left_arm_1_0 = p_diamond_pauldron_left_arm.addOrReplaceChild("diamond_shoulder_left_arm_1_0", CubeListBuilder.create()
                .texOffs(108, 199).addBox(-1F, -1.605F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.395F, -0.45F)),
                PartPose.offsetAndRotation(2.6F, -1.3F, 1.6F, -0.1745F, 0F, 0.6632F));
        PartDefinition p_diamond_shoulder_left_arm_1_1 = p_diamond_shoulder_left_arm_1_0.addOrReplaceChild("diamond_shoulder_left_arm_1_1", CubeListBuilder.create()
                .texOffs(180, 221).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1975F, -0.45F, -0.1975F)),
                PartPose.offset(0F, -0.96F, 0F));
        PartDefinition p_diamond_chest_right_arm = root.addOrReplaceChild("diamond_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_diamond_pauldron_right_arm = p_diamond_chest_right_arm.addOrReplaceChild("diamond_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(206, 107).addBox(-3.8F, -1.6F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.2F, -0.2F, 0F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.1745F));
        PartDefinition p_diamond_shoulder_right_arm_0_0 = p_diamond_pauldron_right_arm.addOrReplaceChild("diamond_shoulder_right_arm_0_0", CubeListBuilder.create()
                .texOffs(117, 199).addBox(-1F, -1.88F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.25F, -0.12F, -0.25F)),
                PartPose.offsetAndRotation(-1.8F, -1.3F, -1F, 0.1396F, 0F, -0.4189F));
        PartDefinition p_diamond_shoulder_right_arm_0_1 = p_diamond_shoulder_right_arm_0_0.addOrReplaceChild("diamond_shoulder_right_arm_0_1", CubeListBuilder.create()
                .texOffs(185, 221).addBox(-0.5F, -1.8F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.0875F, -0.2F, -0.0875F)),
                PartPose.offset(0F, -1.51F, 0F));
        PartDefinition p_diamond_shoulder_right_arm_1_0 = p_diamond_pauldron_right_arm.addOrReplaceChild("diamond_shoulder_right_arm_1_0", CubeListBuilder.create()
                .texOffs(126, 199).addBox(-1F, -1.605F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.395F, -0.45F)),
                PartPose.offsetAndRotation(-2.6F, -1.3F, 1.6F, -0.1745F, 0F, -0.6632F));
        PartDefinition p_diamond_shoulder_right_arm_1_1 = p_diamond_shoulder_right_arm_1_0.addOrReplaceChild("diamond_shoulder_right_arm_1_1", CubeListBuilder.create()
                .texOffs(190, 221).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1975F, -0.45F, -0.1975F)),
                PartPose.offset(0F, -0.96F, 0F));
        PartDefinition p_diamond_legs_body = root.addOrReplaceChild("diamond_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_diamond_legs_left_leg = root.addOrReplaceChild("diamond_legs_left_leg", CubeListBuilder.create()
                .texOffs(161, 213).addBox(-1F, 5.4F, -3.5F, 2F, 2F, 1F, new CubeDeformation(0F, 0F, -0.1F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_diamond_legs_right_leg = root.addOrReplaceChild("diamond_legs_right_leg", CubeListBuilder.create()
                .texOffs(168, 213).addBox(-1F, 5.4F, -3.5F, 2F, 2F, 1F, new CubeDeformation(0F, 0F, -0.1F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_diamond_feet_left_leg = root.addOrReplaceChild("diamond_feet_left_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_diamond_heel_left_leg_0 = p_diamond_feet_left_leg.addOrReplaceChild("diamond_heel_left_leg_0", CubeListBuilder.create()
                .texOffs(135, 199).addBox(-1F, -1.605F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.395F, -0.4F)),
                PartPose.offsetAndRotation(0F, 10.9F, 2.8F, -0.9774F, 0F, 0F));
        PartDefinition p_diamond_heel_left_leg_1 = p_diamond_heel_left_leg_0.addOrReplaceChild("diamond_heel_left_leg_1", CubeListBuilder.create()
                .texOffs(195, 221).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.17F, -0.45F, -0.17F)),
                PartPose.offset(0F, -0.96F, 0F));
        PartDefinition p_diamond_feet_right_leg = root.addOrReplaceChild("diamond_feet_right_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_diamond_heel_right_leg_0 = p_diamond_feet_right_leg.addOrReplaceChild("diamond_heel_right_leg_0", CubeListBuilder.create()
                .texOffs(144, 199).addBox(-1F, -1.605F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.395F, -0.4F)),
                PartPose.offsetAndRotation(0F, 10.9F, 2.8F, -0.9774F, 0F, 0F));
        PartDefinition p_diamond_heel_right_leg_1 = p_diamond_heel_right_leg_0.addOrReplaceChild("diamond_heel_right_leg_1", CubeListBuilder.create()
                .texOffs(200, 221).addBox(-0.5F, -1.55F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.17F, -0.45F, -0.17F)),
                PartPose.offset(0F, -0.96F, 0F));
        PartDefinition p_netherite_head_head = root.addOrReplaceChild("netherite_head_head", CubeListBuilder.create()
                .texOffs(125, 225).addBox(-5F, -6.3F, -5.85F, 10F, 1F, 1F, new CubeDeformation(-0.4F, -0.1F, -0.15F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_netherite_horn_left_0 = p_netherite_head_head.addOrReplaceChild("netherite_horn_left_0", CubeListBuilder.create()
                .texOffs(153, 199).addBox(-1F, -2F, -1F, 2F, 2F, 2F),
                PartPose.offsetAndRotation(4.4F, -7.6F, -2.6F, 0F, 0F, 1.2217F));
        PartDefinition p_netherite_horn_left_1 = p_netherite_horn_left_0.addOrReplaceChild("netherite_horn_left_1", CubeListBuilder.create()
                .texOffs(162, 199).addBox(-1F, -2F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, -1.75F, 0F, 0.5236F, 0F, -0.5236F));
        PartDefinition p_netherite_horn_left_2 = p_netherite_horn_left_1.addOrReplaceChild("netherite_horn_left_2", CubeListBuilder.create()
                .texOffs(171, 199).addBox(-1F, -1.9F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.1F, -0.45F)),
                PartPose.offsetAndRotation(0F, -1.75F, 0F, 0.5934F, 0F, -0.4189F));
        PartDefinition p_netherite_horn_right_0 = p_netherite_head_head.addOrReplaceChild("netherite_horn_right_0", CubeListBuilder.create()
                .texOffs(180, 199).addBox(-1F, -2F, -1F, 2F, 2F, 2F),
                PartPose.offsetAndRotation(-4.4F, -7.6F, -2.6F, 0F, 0F, -1.2217F));
        PartDefinition p_netherite_horn_right_1 = p_netherite_horn_right_0.addOrReplaceChild("netherite_horn_right_1", CubeListBuilder.create()
                .texOffs(189, 199).addBox(-1F, -2F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, -1.75F, 0F, 0.5236F, 0F, 0.5236F));
        PartDefinition p_netherite_horn_right_2 = p_netherite_horn_right_1.addOrReplaceChild("netherite_horn_right_2", CubeListBuilder.create()
                .texOffs(198, 199).addBox(-1F, -1.9F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.1F, -0.45F)),
                PartPose.offsetAndRotation(0F, -1.75F, 0F, 0.5934F, 0F, 0.4189F));
        PartDefinition p_netherite_chest_body = root.addOrReplaceChild("netherite_chest_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_netherite_chest_left_arm = root.addOrReplaceChild("netherite_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_netherite_pauldron_left_arm = p_netherite_chest_left_arm.addOrReplaceChild("netherite_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(0, 29).addBox(-3.7F, -2.25F, -4F, 8F, 3F, 8F, new CubeDeformation(-0.3F, -0.35F, -0.2F))
                .texOffs(186, 63).addBox(-2.4F, 0.35F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.2F, -0.05F, -0.1F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.2618F));
        PartDefinition p_netherite_spike_left_arm_0_0 = p_netherite_pauldron_left_arm.addOrReplaceChild("netherite_spike_left_arm_0_0", CubeListBuilder.create()
                .texOffs(207, 199).addBox(-1F, -1.66F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.34F, -0.4F)),
                PartPose.offsetAndRotation(2F, -1.8F, -1.6F, 0F, 0F, 0.5236F));
        PartDefinition p_netherite_spike_left_arm_0_1 = p_netherite_spike_left_arm_0_0.addOrReplaceChild("netherite_spike_left_arm_0_1", CubeListBuilder.create()
                .texOffs(205, 221).addBox(-0.5F, -1.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.17F, -0.4F, -0.17F)),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_netherite_spike_left_arm_1_0 = p_netherite_pauldron_left_arm.addOrReplaceChild("netherite_spike_left_arm_1_0", CubeListBuilder.create()
                .texOffs(216, 199).addBox(-1F, -1.66F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.34F, -0.4F)),
                PartPose.offsetAndRotation(2F, -1.8F, 1.6F, 0F, 0F, 0.5236F));
        PartDefinition p_netherite_spike_left_arm_1_1 = p_netherite_spike_left_arm_1_0.addOrReplaceChild("netherite_spike_left_arm_1_1", CubeListBuilder.create()
                .texOffs(210, 221).addBox(-0.5F, -1.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.17F, -0.4F, -0.17F)),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_netherite_chest_right_arm = root.addOrReplaceChild("netherite_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_netherite_pauldron_right_arm = p_netherite_chest_right_arm.addOrReplaceChild("netherite_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(33, 29).addBox(-4.3F, -2.25F, -4F, 8F, 3F, 8F, new CubeDeformation(-0.3F, -0.35F, -0.2F))
                .texOffs(217, 63).addBox(-4.6F, 0.35F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.2F, -0.05F, -0.1F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.2618F));
        PartDefinition p_netherite_spike_right_arm_0_0 = p_netherite_pauldron_right_arm.addOrReplaceChild("netherite_spike_right_arm_0_0", CubeListBuilder.create()
                .texOffs(225, 199).addBox(-1F, -1.66F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.34F, -0.4F)),
                PartPose.offsetAndRotation(-2F, -1.8F, -1.6F, 0F, 0F, -0.5236F));
        PartDefinition p_netherite_spike_right_arm_0_1 = p_netherite_spike_right_arm_0_0.addOrReplaceChild("netherite_spike_right_arm_0_1", CubeListBuilder.create()
                .texOffs(215, 221).addBox(-0.5F, -1.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.17F, -0.4F, -0.17F)),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_netherite_spike_right_arm_1_0 = p_netherite_pauldron_right_arm.addOrReplaceChild("netherite_spike_right_arm_1_0", CubeListBuilder.create()
                .texOffs(234, 199).addBox(-1F, -1.66F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.34F, -0.4F)),
                PartPose.offsetAndRotation(-2F, -1.8F, 1.6F, 0F, 0F, -0.5236F));
        PartDefinition p_netherite_spike_right_arm_1_1 = p_netherite_spike_right_arm_1_0.addOrReplaceChild("netherite_spike_right_arm_1_1", CubeListBuilder.create()
                .texOffs(220, 221).addBox(-0.5F, -1.6F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.17F, -0.4F, -0.17F)),
                PartPose.offset(0F, -1.07F, 0F));
        PartDefinition p_netherite_legs_body = root.addOrReplaceChild("netherite_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_netherite_legs_left_leg = root.addOrReplaceChild("netherite_legs_left_leg", CubeListBuilder.create()
                .texOffs(75, 145).addBox(-3F, -0.9F, -3.3F, 6F, 5F, 1F, new CubeDeformation(-0.3F, -0.3F, 0F))
                .texOffs(33, 189).addBox(-2F, 5F, -3.2F, 4F, 3F, 1F, new CubeDeformation(-0.1F, -0.1F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_netherite_legs_right_leg = root.addOrReplaceChild("netherite_legs_right_leg", CubeListBuilder.create()
                .texOffs(90, 145).addBox(-3F, -0.9F, -3.3F, 6F, 5F, 1F, new CubeDeformation(-0.3F, -0.3F, 0F))
                .texOffs(44, 189).addBox(-2F, 5F, -3.2F, 4F, 3F, 1F, new CubeDeformation(-0.1F, -0.1F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_netherite_feet_left_leg = root.addOrReplaceChild("netherite_feet_left_leg", CubeListBuilder.create()
                .texOffs(90, 166).addBox(-2.5F, 10.15F, -4.45F, 5F, 3F, 2F, new CubeDeformation(0F, -0.25F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_netherite_toe_left_leg_0 = p_netherite_feet_left_leg.addOrReplaceChild("netherite_toe_left_leg_0", CubeListBuilder.create()
                .texOffs(160, 229).addBox(-0.5F, -0.94F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.06F, 0F)),
                PartPose.offsetAndRotation(0F, 11.2F, -4F, 1.0821F, 0F, 0F));
        PartDefinition p_netherite_toe_left_leg_1 = p_netherite_toe_left_leg_0.addOrReplaceChild("netherite_toe_left_leg_1", CubeListBuilder.create()
                .texOffs(165, 229).addBox(-0.5F, -0.9F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.1F, -0.225F)),
                PartPose.offset(0F, -0.63F, 0F));
        PartDefinition p_netherite_feet_right_leg = root.addOrReplaceChild("netherite_feet_right_leg", CubeListBuilder.create()
                .texOffs(105, 166).addBox(-2.5F, 10.15F, -4.45F, 5F, 3F, 2F, new CubeDeformation(0F, -0.25F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_netherite_toe_right_leg_0 = p_netherite_feet_right_leg.addOrReplaceChild("netherite_toe_right_leg_0", CubeListBuilder.create()
                .texOffs(170, 229).addBox(-0.5F, -0.94F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.06F, 0F)),
                PartPose.offsetAndRotation(0F, 11.2F, -4F, 1.0821F, 0F, 0F));
        PartDefinition p_netherite_toe_right_leg_1 = p_netherite_toe_right_leg_0.addOrReplaceChild("netherite_toe_right_leg_1", CubeListBuilder.create()
                .texOffs(175, 229).addBox(-0.5F, -0.9F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.1F, -0.225F)),
                PartPose.offset(0F, -0.63F, 0F));
        PartDefinition p_copper_head_head = root.addOrReplaceChild("copper_head_head", CubeListBuilder.create()
                .texOffs(223, 137).addBox(-2F, -10.4F, -2F, 4F, 2F, 4F, new CubeDeformation(-0.4F, -0.4F, -0.4F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_copper_spike_0 = p_copper_head_head.addOrReplaceChild("copper_spike_0", CubeListBuilder.create()
                .texOffs(243, 199).addBox(-1F, -2F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offset(0F, -9.8F, 0F));
        PartDefinition p_copper_spike_1 = p_copper_spike_0.addOrReplaceChild("copper_spike_1", CubeListBuilder.create()
                .texOffs(225, 221).addBox(-0.5F, -2F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1F, 0F, -0.1F)),
                PartPose.offset(0F, -1.75F, 0F));
        PartDefinition p_copper_chest_body = root.addOrReplaceChild("copper_chest_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_copper_chest_left_arm = root.addOrReplaceChild("copper_chest_left_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_copper_pauldron_left_arm = p_copper_chest_left_arm.addOrReplaceChild("copper_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(0, 118).addBox(-3.2F, -1.55F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.2F, -0.05F, 0F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.2094F));
        PartDefinition p_copper_chest_right_arm = root.addOrReplaceChild("copper_chest_right_arm", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_copper_pauldron_right_arm = p_copper_chest_right_arm.addOrReplaceChild("copper_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(29, 118).addBox(-3.8F, -1.55F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.2F, -0.05F, 0F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.2094F));
        PartDefinition p_copper_legs_body = root.addOrReplaceChild("copper_legs_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_copper_legs_left_leg = root.addOrReplaceChild("copper_legs_left_leg", CubeListBuilder.create()
                .texOffs(55, 189).addBox(-2F, 5F, -3.2F, 4F, 3F, 1F, new CubeDeformation(-0.1F, -0.1F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_copper_legs_right_leg = root.addOrReplaceChild("copper_legs_right_leg", CubeListBuilder.create()
                .texOffs(66, 189).addBox(-2F, 5F, -3.2F, 4F, 3F, 1F, new CubeDeformation(-0.1F, -0.1F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_copper_feet_left_leg = root.addOrReplaceChild("copper_feet_left_leg", CubeListBuilder.create()
                .texOffs(120, 166).addBox(-2.5F, 10.25F, -4.35F, 5F, 3F, 2F, new CubeDeformation(-0.1F, -0.35F, -0.35F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_copper_feet_right_leg = root.addOrReplaceChild("copper_feet_right_leg", CubeListBuilder.create()
                .texOffs(135, 166).addBox(-2.5F, 10.25F, -4.35F, 5F, 3F, 2F, new CubeDeformation(-0.1F, -0.35F, -0.35F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_turtle_head_head = root.addOrReplaceChild("turtle_head_head", CubeListBuilder.create()
                .texOffs(46, 0).addBox(-6F, -9.9F, -6F, 12F, 1F, 12F, new CubeDeformation(-0.4F, 0F, -0.4F))
                .texOffs(219, 29).addBox(-4.5F, -10.9F, -4.5F, 9F, 1F, 9F, new CubeDeformation(-0.1F, 0F, -0.1F))
                .texOffs(148, 128).addBox(-3F, -11.7F, -3F, 6F, 1F, 6F, new CubeDeformation(-0.2F, -0.1F, -0.2F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_moonstone_head_head = root.addOrReplaceChild("moonstone_head_head", CubeListBuilder.create()
                .texOffs(75, 225).addBox(-5.5F, -7.7F, -5.65F, 11F, 1F, 1F, new CubeDeformation(-0.05F, 0F, -0.2F))
                .texOffs(100, 225).addBox(-5.5F, -7.7F, 4.65F, 11F, 1F, 1F, new CubeDeformation(-0.05F, 0F, -0.2F))
                .texOffs(89, 29).addBox(-5.65F, -7.7F, -5F, 1F, 1F, 10F, new CubeDeformation(-0.2F, 0F, -0.15F))
                .texOffs(112, 29).addBox(4.65F, -7.7F, -5F, 1F, 1F, 10F, new CubeDeformation(-0.2F, 0F, -0.15F))
                .texOffs(0, 204).addBox(-1.5F, -8.8F, -6.05F, 3F, 3F, 1F, new CubeDeformation(-0.2F, -0.3F, -0.3F))
                .texOffs(175, 213).addBox(-1F, -8.3F, -6.35F, 2F, 2F, 1F, new CubeDeformation(-0.05F, -0.1F, -0.2F))
                .texOffs(53, 204).addBox(-6.175F, -8.2F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.225F, -0.3F, -0.3F))
                .texOffs(60, 204).addBox(5.175F, -8.2F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.225F, -0.3F, -0.3F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_moon_diadem = p_moonstone_head_head.addOrReplaceChild("moon_diadem", CubeListBuilder.create()
                .texOffs(184, 204).addBox(-2F, -1.5625F, -0.5F, 4F, 2F, 1F, new CubeDeformation(-0.375F, -0.4375F, -0.15F)),
                PartPose.offsetAndRotation(0F, -8.6F, -5.75F, -0.1396F, 0F, 0F));
        PartDefinition p_moon_diadem_left_0 = p_moon_diadem.addOrReplaceChild("moon_diadem_left_0", CubeListBuilder.create()
                .texOffs(182, 213).addBox(-1F, -1.9375F, -0.5F, 2F, 2F, 1F, new CubeDeformation(-0.4062F, -0.0625F, -0.175F)),
                PartPose.offsetAndRotation(1.3125F, -0.5625F, 0F, 0F, 0F, 1.0123F));
        PartDefinition p_moon_diadem_left_1 = p_moon_diadem_left_0.addOrReplaceChild("moon_diadem_left_1", CubeListBuilder.create()
                .texOffs(230, 221).addBox(-0.5F, -1.8438F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.0312F, -0.1562F, -0.2F)),
                PartPose.offsetAndRotation(0F, -1.625F, 0F, 0F, 0F, -0.6981F));
        PartDefinition p_moon_diadem_left_2 = p_moon_diadem_left_1.addOrReplaceChild("moon_diadem_left_2", CubeListBuilder.create()
                .texOffs(235, 221).addBox(-0.5F, -1.7188F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1875F, -0.2812F, -0.25F)),
                PartPose.offsetAndRotation(0F, -1.4375F, 0F, 0F, 0F, -0.6283F));
        PartDefinition p_moon_diadem_right_0 = p_moon_diadem.addOrReplaceChild("moon_diadem_right_0", CubeListBuilder.create()
                .texOffs(189, 213).addBox(-1F, -1.9375F, -0.5F, 2F, 2F, 1F, new CubeDeformation(-0.4062F, -0.0625F, -0.175F)),
                PartPose.offsetAndRotation(-1.3125F, -0.5625F, 0F, 0F, 0F, -1.0123F));
        PartDefinition p_moon_diadem_right_1 = p_moon_diadem_right_0.addOrReplaceChild("moon_diadem_right_1", CubeListBuilder.create()
                .texOffs(240, 221).addBox(-0.5F, -1.8438F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.0312F, -0.1562F, -0.2F)),
                PartPose.offsetAndRotation(0F, -1.625F, 0F, 0F, 0F, 0.6981F));
        PartDefinition p_moon_diadem_right_2 = p_moon_diadem_right_1.addOrReplaceChild("moon_diadem_right_2", CubeListBuilder.create()
                .texOffs(245, 221).addBox(-0.5F, -1.7188F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1875F, -0.2812F, -0.25F)),
                PartPose.offsetAndRotation(0F, -1.4375F, 0F, 0F, 0F, 0.6283F));
        PartDefinition p_moonstone_chest_body = root.addOrReplaceChild("moonstone_chest_body", CubeListBuilder.create()
                .texOffs(196, 213).addBox(-4.6F, -0.9F, -3.75F, 2F, 2F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.15F))
                .texOffs(203, 213).addBox(2.6F, -0.9F, -3.75F, 2F, 2F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.15F))
                .texOffs(227, 166).addBox(-2F, 1.2F, -3.7F, 4F, 4F, 1F, new CubeDeformation(-0.3F, -0.2F, -0.25F))
                .texOffs(9, 204).addBox(-1.5F, 1.7F, -4.05F, 3F, 3F, 1F, new CubeDeformation(-0.35F, -0.2F, -0.2F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_moon_cape = p_moonstone_chest_body.addOrReplaceChild("moon_cape", CubeListBuilder.create()
                .texOffs(232, 96).addBox(-5F, -0.2F, -0.2F, 10F, 9F, 1F, new CubeDeformation(-0.3F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(0F, -0.2F, 3.1F, 0.0873F, 0F, 0F));
        PartDefinition p_moon_cape_hem = p_moon_cape.addOrReplaceChild("moon_cape_hem", CubeListBuilder.create()
                .texOffs(0, 107).addBox(-5F, -0.3F, -0.2F, 10F, 9F, 1F, new CubeDeformation(-0.4F, -0.3F, -0.2F)),
                PartPose.offsetAndRotation(0F, 8.4F, 0.05F, 0.1396F, 0F, 0F));
        PartDefinition p_moonstone_chest_left_arm = root.addOrReplaceChild("moonstone_chest_left_arm", CubeListBuilder.create()
                .texOffs(116, 96).addBox(-2.5F, 5.15F, -3.5F, 7F, 3F, 7F, new CubeDeformation(-0.25F, -0.35F, -0.25F))
                .texOffs(210, 213).addBox(0F, 5.65F, -3.95F, 2F, 2F, 1F, new CubeDeformation(-0.4F, -0.35F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_moon_pauldron_left_arm = p_moonstone_chest_left_arm.addOrReplaceChild("moon_pauldron_left_arm", CubeListBuilder.create()
                .texOffs(0, 74).addBox(-3.3F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.2F, -0.15F, -0.4F))
                .texOffs(145, 96).addBox(-2.1F, -0.15F, -4F, 6F, 2F, 8F, new CubeDeformation(-0.1F, -0.35F, -0.3F)),
                PartPose.offsetAndRotation(1F, -3F, 0F, 0F, 0F, 0.2094F));
        PartDefinition p_moon_shoulder_left_arm = p_moon_pauldron_left_arm.addOrReplaceChild("moon_shoulder_left_arm", CubeListBuilder.create()
                .texOffs(148, 225).addBox(-1.5F, -0.95F, -0.5F, 3F, 1F, 1F, new CubeDeformation(-0.2F, -0.05F, -0.15F)),
                PartPose.offsetAndRotation(1.4F, -1.4F, 0F, 0F, 1.5708F, 0F));
        PartDefinition p_moon_shoulder_left_arm_left_0 = p_moon_shoulder_left_arm.addOrReplaceChild("moon_shoulder_left_arm_left_0", CubeListBuilder.create()
                .texOffs(250, 221).addBox(-0.5F, -1.75F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.025F, -0.25F, -0.175F)),
                PartPose.offsetAndRotation(1.05F, -0.45F, 0F, 0F, 0F, 1.0123F));
        PartDefinition p_moon_shoulder_left_arm_left_1 = p_moon_shoulder_left_arm_left_0.addOrReplaceChild("moon_shoulder_left_arm_left_1", CubeListBuilder.create()
                .texOffs(0, 225).addBox(-0.5F, -1.675F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.125F, -0.325F, -0.2F)),
                PartPose.offsetAndRotation(0F, -1.25F, 0F, 0F, 0F, -0.6981F));
        PartDefinition p_moon_shoulder_left_arm_left_2 = p_moon_shoulder_left_arm_left_1.addOrReplaceChild("moon_shoulder_left_arm_left_2", CubeListBuilder.create()
                .texOffs(5, 225).addBox(-0.5F, -1.575F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.25F, -0.425F, -0.25F)),
                PartPose.offsetAndRotation(0F, -1.1F, 0F, 0F, 0F, -0.6283F));
        PartDefinition p_moon_shoulder_left_arm_right_0 = p_moon_shoulder_left_arm.addOrReplaceChild("moon_shoulder_left_arm_right_0", CubeListBuilder.create()
                .texOffs(10, 225).addBox(-0.5F, -1.75F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.025F, -0.25F, -0.175F)),
                PartPose.offsetAndRotation(-1.05F, -0.45F, 0F, 0F, 0F, -1.0123F));
        PartDefinition p_moon_shoulder_left_arm_right_1 = p_moon_shoulder_left_arm_right_0.addOrReplaceChild("moon_shoulder_left_arm_right_1", CubeListBuilder.create()
                .texOffs(15, 225).addBox(-0.5F, -1.675F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.125F, -0.325F, -0.2F)),
                PartPose.offsetAndRotation(0F, -1.25F, 0F, 0F, 0F, 0.6981F));
        PartDefinition p_moon_shoulder_left_arm_right_2 = p_moon_shoulder_left_arm_right_1.addOrReplaceChild("moon_shoulder_left_arm_right_2", CubeListBuilder.create()
                .texOffs(20, 225).addBox(-0.5F, -1.575F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.25F, -0.425F, -0.25F)),
                PartPose.offsetAndRotation(0F, -1.1F, 0F, 0F, 0F, 0.6283F));
        PartDefinition p_moonstone_chest_right_arm = root.addOrReplaceChild("moonstone_chest_right_arm", CubeListBuilder.create()
                .texOffs(174, 96).addBox(-4.5F, 5.15F, -3.5F, 7F, 3F, 7F, new CubeDeformation(-0.25F, -0.35F, -0.25F))
                .texOffs(217, 213).addBox(-2F, 5.65F, -3.95F, 2F, 2F, 1F, new CubeDeformation(-0.4F, -0.35F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_moon_pauldron_right_arm = p_moonstone_chest_right_arm.addOrReplaceChild("moon_pauldron_right_arm", CubeListBuilder.create()
                .texOffs(31, 74).addBox(-3.7F, -1.65F, -4F, 7F, 2F, 8F, new CubeDeformation(-0.2F, -0.15F, -0.4F))
                .texOffs(203, 96).addBox(-3.9F, -0.15F, -4F, 6F, 2F, 8F, new CubeDeformation(-0.1F, -0.35F, -0.3F)),
                PartPose.offsetAndRotation(-1F, -3F, 0F, 0F, 0F, -0.2094F));
        PartDefinition p_moon_shoulder_right_arm = p_moon_pauldron_right_arm.addOrReplaceChild("moon_shoulder_right_arm", CubeListBuilder.create()
                .texOffs(157, 225).addBox(-1.5F, -0.95F, -0.5F, 3F, 1F, 1F, new CubeDeformation(-0.2F, -0.05F, -0.15F)),
                PartPose.offsetAndRotation(-1.4F, -1.4F, 0F, 0F, -1.5708F, 0F));
        PartDefinition p_moon_shoulder_right_arm_left_0 = p_moon_shoulder_right_arm.addOrReplaceChild("moon_shoulder_right_arm_left_0", CubeListBuilder.create()
                .texOffs(25, 225).addBox(-0.5F, -1.75F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.025F, -0.25F, -0.175F)),
                PartPose.offsetAndRotation(1.05F, -0.45F, 0F, 0F, 0F, 1.0123F));
        PartDefinition p_moon_shoulder_right_arm_left_1 = p_moon_shoulder_right_arm_left_0.addOrReplaceChild("moon_shoulder_right_arm_left_1", CubeListBuilder.create()
                .texOffs(30, 225).addBox(-0.5F, -1.675F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.125F, -0.325F, -0.2F)),
                PartPose.offsetAndRotation(0F, -1.25F, 0F, 0F, 0F, -0.6981F));
        PartDefinition p_moon_shoulder_right_arm_left_2 = p_moon_shoulder_right_arm_left_1.addOrReplaceChild("moon_shoulder_right_arm_left_2", CubeListBuilder.create()
                .texOffs(35, 225).addBox(-0.5F, -1.575F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.25F, -0.425F, -0.25F)),
                PartPose.offsetAndRotation(0F, -1.1F, 0F, 0F, 0F, -0.6283F));
        PartDefinition p_moon_shoulder_right_arm_right_0 = p_moon_shoulder_right_arm.addOrReplaceChild("moon_shoulder_right_arm_right_0", CubeListBuilder.create()
                .texOffs(40, 225).addBox(-0.5F, -1.75F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.025F, -0.25F, -0.175F)),
                PartPose.offsetAndRotation(-1.05F, -0.45F, 0F, 0F, 0F, -1.0123F));
        PartDefinition p_moon_shoulder_right_arm_right_1 = p_moon_shoulder_right_arm_right_0.addOrReplaceChild("moon_shoulder_right_arm_right_1", CubeListBuilder.create()
                .texOffs(45, 225).addBox(-0.5F, -1.675F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.125F, -0.325F, -0.2F)),
                PartPose.offsetAndRotation(0F, -1.25F, 0F, 0F, 0F, 0.6981F));
        PartDefinition p_moon_shoulder_right_arm_right_2 = p_moon_shoulder_right_arm_right_1.addOrReplaceChild("moon_shoulder_right_arm_right_2", CubeListBuilder.create()
                .texOffs(50, 225).addBox(-0.5F, -1.575F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.25F, -0.425F, -0.25F)),
                PartPose.offsetAndRotation(0F, -1.1F, 0F, 0F, 0F, 0.6283F));
        PartDefinition p_moonstone_legs_body = root.addOrReplaceChild("moonstone_legs_body", CubeListBuilder.create()
                .texOffs(224, 213).addBox(-1F, 10.15F, -3.8F, 2F, 2F, 1F, new CubeDeformation(-0.4F, -0.45F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_moon_buckle = p_moonstone_legs_body.addOrReplaceChild("moon_buckle", CubeListBuilder.create()
                .texOffs(173, 225).addBox(-1F, -0.8375F, -0.5F, 2F, 1F, 1F, new CubeDeformation(-0.025F, -0.1625F, -0.15F)),
                PartPose.offset(0F, 11.5F, -3.3F));
        PartDefinition p_moon_buckle_left_0 = p_moon_buckle.addOrReplaceChild("moon_buckle_left_0", CubeListBuilder.create()
                .texOffs(55, 225).addBox(-0.5F, -1.5625F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1438F, -0.4375F, -0.175F)),
                PartPose.offsetAndRotation(0.7875F, -0.3375F, 0F, 0F, 0F, 1.0123F));
        PartDefinition p_moon_buckle_left_1 = p_moon_buckle_left_0.addOrReplaceChild("moon_buckle_left_1", CubeListBuilder.create()
                .texOffs(60, 225).addBox(-0.5F, -1.5063F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.2188F, -0.4937F, -0.2F)),
                PartPose.offsetAndRotation(0F, -0.875F, 0F, 0F, 0F, -0.6981F));
        PartDefinition p_moon_buckle_left_2 = p_moon_buckle_left_1.addOrReplaceChild("moon_buckle_left_2", CubeListBuilder.create()
                .texOffs(180, 229).addBox(-0.5F, -0.9312F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.3125F, -0.0688F, -0.25F)),
                PartPose.offsetAndRotation(0F, -0.7625F, 0F, 0F, 0F, -0.6283F));
        PartDefinition p_moon_buckle_right_0 = p_moon_buckle.addOrReplaceChild("moon_buckle_right_0", CubeListBuilder.create()
                .texOffs(65, 225).addBox(-0.5F, -1.5625F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1438F, -0.4375F, -0.175F)),
                PartPose.offsetAndRotation(-0.7875F, -0.3375F, 0F, 0F, 0F, -1.0123F));
        PartDefinition p_moon_buckle_right_1 = p_moon_buckle_right_0.addOrReplaceChild("moon_buckle_right_1", CubeListBuilder.create()
                .texOffs(70, 225).addBox(-0.5F, -1.5063F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.2188F, -0.4937F, -0.2F)),
                PartPose.offsetAndRotation(0F, -0.875F, 0F, 0F, 0F, 0.6981F));
        PartDefinition p_moon_buckle_right_2 = p_moon_buckle_right_1.addOrReplaceChild("moon_buckle_right_2", CubeListBuilder.create()
                .texOffs(185, 229).addBox(-0.5F, -0.9312F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.3125F, -0.0688F, -0.25F)),
                PartPose.offsetAndRotation(0F, -0.7625F, 0F, 0F, 0F, 0.6283F));
        PartDefinition p_moonstone_legs_left_leg = root.addOrReplaceChild("moonstone_legs_left_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_moon_robe_left_leg = p_moonstone_legs_left_leg.addOrReplaceChild("moon_robe_left_leg", CubeListBuilder.create()
                .texOffs(92, 118).addBox(-2.5F, -0.1F, -0.55F, 5F, 8F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.25F)),
                PartPose.offsetAndRotation(0F, -0.6F, -2.75F, -0.1047F, 0F, 0F));
        PartDefinition p_moon_robe_side_left_leg = p_moonstone_legs_left_leg.addOrReplaceChild("moon_robe_side_left_leg", CubeListBuilder.create()
                .texOffs(95, 0).addBox(-0.25F, -0.1F, -2.5F, 1F, 8F, 5F, new CubeDeformation(-0.25F, -0.1F, -0.1F)),
                PartPose.offsetAndRotation(2.75F, -0.6F, 0F, 0F, 0F, -0.1047F));
        PartDefinition p_moonstone_legs_right_leg = root.addOrReplaceChild("moonstone_legs_right_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_moon_robe_right_leg = p_moonstone_legs_right_leg.addOrReplaceChild("moon_robe_right_leg", CubeListBuilder.create()
                .texOffs(105, 118).addBox(-2.5F, -0.1F, -0.55F, 5F, 8F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.25F)),
                PartPose.offsetAndRotation(0F, -0.6F, -2.75F, -0.1047F, 0F, 0F));
        PartDefinition p_moon_robe_side_right_leg = p_moonstone_legs_right_leg.addOrReplaceChild("moon_robe_side_right_leg", CubeListBuilder.create()
                .texOffs(108, 0).addBox(-0.75F, -0.1F, -2.5F, 1F, 8F, 5F, new CubeDeformation(-0.25F, -0.1F, -0.1F)),
                PartPose.offsetAndRotation(-2.75F, -0.6F, 0F, 0F, 0F, 0.1047F));
        PartDefinition p_moonstone_feet_left_leg = root.addOrReplaceChild("moonstone_feet_left_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_moon_toe_left_leg_0 = p_moonstone_feet_left_leg.addOrReplaceChild("moon_toe_left_leg_0", CubeListBuilder.create()
                .texOffs(13, 184).addBox(-2F, -1.85F, -1F, 4F, 2F, 2F, new CubeDeformation(-0.3F, -0.15F, -0.3F)),
                PartPose.offsetAndRotation(0F, 12F, -2.3F, 1.5708F, 0F, 0F));
        PartDefinition p_moon_toe_left_leg_1 = p_moon_toe_left_leg_0.addOrReplaceChild("moon_toe_left_leg_1", CubeListBuilder.create()
                .texOffs(77, 189).addBox(-1.5F, -1.75F, -1F, 3F, 2F, 2F, new CubeDeformation(-0.3F, -0.25F, -0.45F)),
                PartPose.offsetAndRotation(0F, -1.45F, 0F, -0.4887F, 0F, 0F));
        PartDefinition p_moon_toe_left_leg_2 = p_moon_toe_left_leg_1.addOrReplaceChild("moon_toe_left_leg_2", CubeListBuilder.create()
                .texOffs(231, 213).addBox(-1F, -1.65F, -0.5F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.35F, -0.1F)),
                PartPose.offsetAndRotation(0F, -1.25F, 0F, -0.733F, 0F, 0F));
        PartDefinition p_moon_toe_left_leg_3 = p_moon_toe_left_leg_2.addOrReplaceChild("moon_toe_left_leg_3", CubeListBuilder.create()
                .texOffs(190, 229).addBox(-0.5F, -1F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.15F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, -1.05F, 0F, -0.8029F, 0F, 0F));
        PartDefinition p_moon_ankle_left_leg = p_moonstone_feet_left_leg.addOrReplaceChild("moon_ankle_left_leg", CubeListBuilder.create()
                .texOffs(180, 225).addBox(-1F, -0.779F, -0.5F, 2F, 1F, 1F, new CubeDeformation(-0.194F, -0.221F, -0.15F)),
                PartPose.offsetAndRotation(3.35F, 9.8F, 0.4F, 0F, 1.5708F, 0F));
        PartDefinition p_moon_ankle_left_leg_left_0 = p_moon_ankle_left_leg.addOrReplaceChild("moon_ankle_left_leg_left_0", CubeListBuilder.create()
                .texOffs(195, 229).addBox(-0.5F, -0.965F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.2055F, -0.035F, -0.175F)),
                PartPose.offsetAndRotation(0.651F, -0.279F, 0F, 0F, 0F, 1.0123F));
        PartDefinition p_moon_ankle_left_leg_left_1 = p_moon_ankle_left_leg_left_0.addOrReplaceChild("moon_ankle_left_leg_left_1", CubeListBuilder.create()
                .texOffs(200, 229).addBox(-0.5F, -0.9185F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.2675F, -0.0815F, -0.2F)),
                PartPose.offsetAndRotation(0F, -0.68F, 0F, 0F, 0F, -0.6981F));
        PartDefinition p_moon_ankle_left_leg_left_2 = p_moon_ankle_left_leg_left_1.addOrReplaceChild("moon_ankle_left_leg_left_2", CubeListBuilder.create()
                .texOffs(205, 229).addBox(-0.5F, -0.8565F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.345F, -0.1435F, -0.25F)),
                PartPose.offsetAndRotation(0F, -0.587F, 0F, 0F, 0F, -0.6283F));
        PartDefinition p_moon_ankle_left_leg_right_0 = p_moon_ankle_left_leg.addOrReplaceChild("moon_ankle_left_leg_right_0", CubeListBuilder.create()
                .texOffs(210, 229).addBox(-0.5F, -0.965F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.2055F, -0.035F, -0.175F)),
                PartPose.offsetAndRotation(-0.651F, -0.279F, 0F, 0F, 0F, -1.0123F));
        PartDefinition p_moon_ankle_left_leg_right_1 = p_moon_ankle_left_leg_right_0.addOrReplaceChild("moon_ankle_left_leg_right_1", CubeListBuilder.create()
                .texOffs(215, 229).addBox(-0.5F, -0.9185F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.2675F, -0.0815F, -0.2F)),
                PartPose.offsetAndRotation(0F, -0.68F, 0F, 0F, 0F, 0.6981F));
        PartDefinition p_moon_ankle_left_leg_right_2 = p_moon_ankle_left_leg_right_1.addOrReplaceChild("moon_ankle_left_leg_right_2", CubeListBuilder.create()
                .texOffs(220, 229).addBox(-0.5F, -0.8565F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.345F, -0.1435F, -0.25F)),
                PartPose.offsetAndRotation(0F, -0.587F, 0F, 0F, 0F, 0.6283F));
        PartDefinition p_moonstone_feet_right_leg = root.addOrReplaceChild("moonstone_feet_right_leg", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_moon_toe_right_leg_0 = p_moonstone_feet_right_leg.addOrReplaceChild("moon_toe_right_leg_0", CubeListBuilder.create()
                .texOffs(26, 184).addBox(-2F, -1.85F, -1F, 4F, 2F, 2F, new CubeDeformation(-0.3F, -0.15F, -0.3F)),
                PartPose.offsetAndRotation(0F, 12F, -2.3F, 1.5708F, 0F, 0F));
        PartDefinition p_moon_toe_right_leg_1 = p_moon_toe_right_leg_0.addOrReplaceChild("moon_toe_right_leg_1", CubeListBuilder.create()
                .texOffs(88, 189).addBox(-1.5F, -1.75F, -1F, 3F, 2F, 2F, new CubeDeformation(-0.3F, -0.25F, -0.45F)),
                PartPose.offsetAndRotation(0F, -1.45F, 0F, -0.4887F, 0F, 0F));
        PartDefinition p_moon_toe_right_leg_2 = p_moon_toe_right_leg_1.addOrReplaceChild("moon_toe_right_leg_2", CubeListBuilder.create()
                .texOffs(238, 213).addBox(-1F, -1.65F, -0.5F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.35F, -0.1F)),
                PartPose.offsetAndRotation(0F, -1.25F, 0F, -0.733F, 0F, 0F));
        PartDefinition p_moon_toe_right_leg_3 = p_moon_toe_right_leg_2.addOrReplaceChild("moon_toe_right_leg_3", CubeListBuilder.create()
                .texOffs(225, 229).addBox(-0.5F, -1F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.15F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, -1.05F, 0F, -0.8029F, 0F, 0F));
        PartDefinition p_moon_ankle_right_leg = p_moonstone_feet_right_leg.addOrReplaceChild("moon_ankle_right_leg", CubeListBuilder.create()
                .texOffs(187, 225).addBox(-1F, -0.779F, -0.5F, 2F, 1F, 1F, new CubeDeformation(-0.194F, -0.221F, -0.15F)),
                PartPose.offsetAndRotation(-3.35F, 9.8F, 0.4F, 0F, -1.5708F, 0F));
        PartDefinition p_moon_ankle_right_leg_left_0 = p_moon_ankle_right_leg.addOrReplaceChild("moon_ankle_right_leg_left_0", CubeListBuilder.create()
                .texOffs(230, 229).addBox(-0.5F, -0.965F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.2055F, -0.035F, -0.175F)),
                PartPose.offsetAndRotation(0.651F, -0.279F, 0F, 0F, 0F, 1.0123F));
        PartDefinition p_moon_ankle_right_leg_left_1 = p_moon_ankle_right_leg_left_0.addOrReplaceChild("moon_ankle_right_leg_left_1", CubeListBuilder.create()
                .texOffs(235, 229).addBox(-0.5F, -0.9185F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.2675F, -0.0815F, -0.2F)),
                PartPose.offsetAndRotation(0F, -0.68F, 0F, 0F, 0F, -0.6981F));
        PartDefinition p_moon_ankle_right_leg_left_2 = p_moon_ankle_right_leg_left_1.addOrReplaceChild("moon_ankle_right_leg_left_2", CubeListBuilder.create()
                .texOffs(240, 229).addBox(-0.5F, -0.8565F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.345F, -0.1435F, -0.25F)),
                PartPose.offsetAndRotation(0F, -0.587F, 0F, 0F, 0F, -0.6283F));
        PartDefinition p_moon_ankle_right_leg_right_0 = p_moon_ankle_right_leg.addOrReplaceChild("moon_ankle_right_leg_right_0", CubeListBuilder.create()
                .texOffs(245, 229).addBox(-0.5F, -0.965F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.2055F, -0.035F, -0.175F)),
                PartPose.offsetAndRotation(-0.651F, -0.279F, 0F, 0F, 0F, -1.0123F));
        PartDefinition p_moon_ankle_right_leg_right_1 = p_moon_ankle_right_leg_right_0.addOrReplaceChild("moon_ankle_right_leg_right_1", CubeListBuilder.create()
                .texOffs(250, 229).addBox(-0.5F, -0.9185F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.2675F, -0.0815F, -0.2F)),
                PartPose.offsetAndRotation(0F, -0.68F, 0F, 0F, 0F, 0.6981F));
        PartDefinition p_moon_ankle_right_leg_right_2 = p_moon_ankle_right_leg_right_1.addOrReplaceChild("moon_ankle_right_leg_right_2", CubeListBuilder.create()
                .texOffs(0, 232).addBox(-0.5F, -0.8565F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.345F, -0.1435F, -0.25F)),
                PartPose.offsetAndRotation(0F, -0.587F, 0F, 0F, 0F, 0.6283F));
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
